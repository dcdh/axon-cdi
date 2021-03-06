package com.damdamdeo.cdi.axonframework.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.serialization.Serializer;

import com.codahale.metrics.MetricRegistry;
import com.damdamdeo.cdi.axonframework.extension.impl.bean.AggregatesRootRepositoriesBeansCreation;
import com.damdamdeo.cdi.axonframework.extension.impl.bean.CommandGatewayBeanCreation;
import com.damdamdeo.cdi.axonframework.extension.impl.bean.EventSchedulerBeanCreation;
import com.damdamdeo.cdi.axonframework.extension.impl.bean.EventStoreBeanCreation;
import com.damdamdeo.cdi.axonframework.extension.impl.bean.MetricRegistryBeanCreation;
import com.damdamdeo.cdi.axonframework.extension.impl.bean.validation.ApplicationScopedBeanValidator;
import com.damdamdeo.cdi.axonframework.extension.impl.bean.validation.BeanScopeNotValidException;
import com.damdamdeo.cdi.axonframework.extension.impl.bean.validation.BeanScopeValidator;
import com.damdamdeo.cdi.axonframework.extension.impl.bean.validation.DependentScopedBeanValidator;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.AggregatesCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.CommandBusCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.CommandHandlersCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.CorrelationDataProviderCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.EmbeddedEventStoreCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.EventHandlersCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.FileConfiguration;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.MetricRegistryCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.ParameterResolverCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.PlatformTransactionManagerCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.ResourceInjectorCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.SagaConfigurationsCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.SerializerCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.TokenStoreCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.TransactionManagerCdiConfigurer;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.UnavailableConfiguration;
import com.damdamdeo.cdi.axonframework.extension.impl.configurer.YamlConfiguration;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.AggregateExecutionContext;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.AggregateRootBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.CommandHandlerBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.EventHandlerBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.MessageDispatchInterceptorBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.MessageHandlerInterceptorBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.NoAggregateExecutionContext;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.SagaBeanInfo;
import com.damdamdeo.cdi.axonframework.support.AxonUtils;
import com.damdamdeo.cdi.axonframework.support.BeforeStartingAxon;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Original: SpringAxonAutoConfigurer
 */
/**
 * TODO explain why bean reference are proxified
 * @author damien
 *
 */
public class CdiAxonAutoConfigurerExtension implements Extension {

	private static final Logger LOGGER = Logger.getLogger(CdiAxonAutoConfigurerExtension.class.getName());

	private final List<ExecutionContext> executionContexts = new ArrayList<>();

	private final List<SagaBeanInfo> sagaBeanInfos = new ArrayList<>();

	private final List<CommandHandlerBeanInfo> commandHandlerBeanInfos = new ArrayList<>();

	private final List<MessageDispatchInterceptorBeanInfo> messageDispatchInterceptorBeanInfos = new ArrayList<>();

	private final List<MessageHandlerInterceptorBeanInfo> messageHandlerInterceptorBeanInfos = new ArrayList<>();

	private final List<EventHandlerBeanInfo> eventHandlerBeanInfos = new ArrayList<>();

	private final List<Configuration> configurations = new ArrayList<>();

	// lookup types bean
	<X> void processAggregateRootBeanAnnotatedType(
			@Observes final ProcessAnnotatedType<X> processAnnotatedType,
			final BeanManager beanManager) {
		AnnotatedType<X> annotatedType = processAnnotatedType.getAnnotatedType();
		boolean isAggregateRoot = AxonUtils.isAnnotatedAggregateRoot(annotatedType.getJavaClass());
		if (isAggregateRoot) {
			AggregateRootBeanInfo aggregateRootBeanInfo = AggregateRootBeanInfo.of(beanManager, annotatedType);
			boolean hasRegisteredDiscoveredAggregateRootBean = false;
			for (ExecutionContext executionContext : executionContexts) {
				if (executionContext.registerIfSameContext(aggregateRootBeanInfo)) {
					hasRegisteredDiscoveredAggregateRootBean = true;
				}
			}
			if (!hasRegisteredDiscoveredAggregateRootBean) {
				executionContexts.add(new AggregateExecutionContext(aggregateRootBeanInfo));
			}
			processAnnotatedType.veto();
		}
	}

	<X> void processMessageDispatchInterceptorBeanAnnotatedType(
			@Observes final ProcessAnnotatedType<X> processAnnotatedType, final BeanManager beanManager) {
		AnnotatedType<X> annotatedType = processAnnotatedType.getAnnotatedType();
		if (AxonUtils.isMessageDispatchInterceptor(annotatedType.getJavaClass())) {
			messageDispatchInterceptorBeanInfos.add(MessageDispatchInterceptorBeanInfo.of(beanManager, annotatedType));
		}
	}
	
	<X> void processMessageHandlerInterceptorBeanAnnotatedType(
			@Observes final ProcessAnnotatedType<X> processAnnotatedType, final BeanManager beanManager) {
		AnnotatedType<X> annotatedType = processAnnotatedType.getAnnotatedType();
		if (AxonUtils.isMessageHandlerInterceptor(annotatedType.getJavaClass())) {
			messageHandlerInterceptorBeanInfos.add(MessageHandlerInterceptorBeanInfo.of(beanManager, annotatedType));
		}
	}

	<X> void processSagaBeanAnnotatedType(
			@Observes final ProcessAnnotatedType<X> processAnnotatedType, final BeanManager beanManager) {
		AnnotatedType<X> annotatedType = processAnnotatedType.getAnnotatedType();
		if (AxonUtils.isAnnotatedSaga(annotatedType.getJavaClass())) {
			sagaBeanInfos.add(SagaBeanInfo.of(beanManager, annotatedType));
			// pat.veto(); // don't veto this bean. Because we need it to discover EventScheduler injected beans
		}
	}

	<X> void processCommandsAndEventsHandlerBeanAnnotatedTypes(
			@Observes final ProcessAnnotatedType<X> processAnnotatedType, final BeanManager beanManager) {
		AnnotatedType<X> annotatedType = processAnnotatedType.getAnnotatedType();
		boolean isCommandHandler = AxonUtils.isCommandHandlerBean(annotatedType.getJavaClass());
		boolean isEventHandler = AxonUtils.isEventHandlerBean(annotatedType.getJavaClass());
		Validate.validState((isEventHandler && isCommandHandler) != true,
				"Provided type cannot be both event and command handler: %s", annotatedType);
		if (isCommandHandler) {
			commandHandlerBeanInfos.add(new CommandHandlerBeanInfo(annotatedType));
		} else if (isEventHandler) {
			eventHandlerBeanInfos.add(new EventHandlerBeanInfo(annotatedType));
		}
	}

	/**
	 * Create and add in CDI context axon injected objects
	 * @param afterBeanDiscovery
	 * @param beanManager
	 * @throws Exception 
	 */
	<T> void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) throws Exception {
		LOGGER.log(Level.INFO, "Axon CDI Extension - Activated");
		// load yaml configuration
		final FileConfiguration fileConfiguration = readFileConfiguration();

		// Scoped validations
		final List<BeanScopeValidator> beanScopeValidators = new ArrayList<>(Arrays.asList(new ApplicationScopedBeanValidator(CommandBus.class),
				new ApplicationScopedBeanValidator(CommandGateway.class),
				new ApplicationScopedBeanValidator(EventBus.class),
				new ApplicationScopedBeanValidator(SnapshotTriggerDefinition.class),
				new ApplicationScopedBeanValidator(TokenStore.class),
				new ApplicationScopedBeanValidator(TransactionManager.class),
				new ApplicationScopedBeanValidator(Serializer.class),
				new ApplicationScopedBeanValidator(EventStorageEngine.class),
				new ApplicationScopedBeanValidator(EventScheduler.class),
				new ApplicationScopedBeanValidator(CorrelationDataProvider.class),
				new ApplicationScopedBeanValidator(MetricRegistry.class)));
		Stream.of(messageDispatchInterceptorBeanInfos, messageHandlerInterceptorBeanInfos)
			.flatMap(annotatedTypeInfos -> annotatedTypeInfos.stream())
			.map(annotatedTypeInfo -> new ApplicationScopedBeanValidator(annotatedTypeInfo.type()))
			.forEachOrdered(beanScopeValidators::add);
		Stream.of(sagaBeanInfos, commandHandlerBeanInfos, eventHandlerBeanInfos)
			.flatMap(annotatedTypeInfos -> annotatedTypeInfos.stream())
			.map(annotatedTypeInfo -> new DependentScopedBeanValidator(annotatedTypeInfo.type()))
			.forEachOrdered(beanScopeValidators::add);
		beanScopeValidators.stream()
			.forEach(beanScopeValidator -> {
				try {
					beanScopeValidator.validate(beanManager);
				} catch (final BeanScopeNotValidException beanScopeNotValidException) {
					afterBeanDiscovery.addDefinitionError(
							new InjectionException(
								String.format("'%s' must be defined as '%s' to avoid clashes between instances",
									beanScopeNotValidException.bean().toString(),
									beanScopeNotValidException.expectedBeanScoped().getSimpleName())));
				}
			});
		if (executionContexts.isEmpty()) {
			// no aggregate root has been defined.
			// we only want to listen for events for the write part !
			executionContexts.add(new NoAggregateExecutionContext());
		}
		// Context assemblers
		messageDispatchInterceptorBeanInfos.forEach(messageDispatchInterceptorBeanInfo -> {
			executionContexts.stream().forEach(executionContext -> executionContext.registerIfSameContext(messageDispatchInterceptorBeanInfo));
		});
		messageHandlerInterceptorBeanInfos.forEach(messageHandlerInterceptorBeanInfo -> {
			executionContexts.stream().forEach(executionContext -> executionContext.registerIfSameContext(messageHandlerInterceptorBeanInfo));
		});
		sagaBeanInfos.forEach(sagaBeanInfo -> {
			executionContexts.stream().forEach(executionContext -> executionContext.registerIfSameContext(sagaBeanInfo));
		});
		commandHandlerBeanInfos.forEach(commandHandlerBeanInfo -> {
			executionContexts.stream().forEach(executionContext -> executionContext.registerIfSameContext(commandHandlerBeanInfo));
		});
		eventHandlerBeanInfos.forEach(eventHandlerBeanInfo -> {
			executionContexts.stream().forEach(executionContext -> executionContext.registerIfSameContext(eventHandlerBeanInfo));
		});
		// Create and setup configurer for each context
		for (ExecutionContext executionContext : executionContexts) {
			final Configurer configurer = DefaultConfigurer.defaultConfiguration();
			Arrays.asList(new ParameterResolverCdiConfigurer(),
					new CorrelationDataProviderCdiConfigurer(),
					new EmbeddedEventStoreCdiConfigurer(),
					new CommandBusCdiConfigurer(),
					new SerializerCdiConfigurer(),
					new TokenStoreCdiConfigurer(),
					new PlatformTransactionManagerCdiConfigurer(),
					new TransactionManagerCdiConfigurer(),
					new ResourceInjectorCdiConfigurer(),
					new EventHandlersCdiConfigurer(),
					new SagaConfigurationsCdiConfigurer(),
					new CommandHandlersCdiConfigurer(),
					new AggregatesCdiConfigurer(),
					new MetricRegistryCdiConfigurer())
				.stream()
				.forEach(cdiConfiguration -> cdiConfiguration.setUp(configurer, beanManager, executionContext, fileConfiguration));
			// create *configuration* from previous setup configurer
			final Configuration configuration = configurer.buildConfiguration();
			// Create cdi bean from configuration (repositories, event handlers, command handlers, event schedulers, command gateway)
			Arrays.asList(new EventStoreBeanCreation(),
					new AggregatesRootRepositoriesBeansCreation(),
					new EventSchedulerBeanCreation(),
					new CommandGatewayBeanCreation(),
					new MetricRegistryBeanCreation())
				.stream()
				.forEach(beansCreationHandler -> beansCreationHandler.create(afterBeanDiscovery, beanManager, executionContext, configuration));
			configurations.add(configuration);
		}
	}

	private FileConfiguration readFileConfiguration() {
		final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		FileConfiguration fileConfiguration;
		try {
			fileConfiguration = mapper.readValue(this.getClass().getResourceAsStream("/configuration.yaml"), YamlConfiguration.class);
			LOGGER.log(Level.INFO, "Loaded conf:");
			LOGGER.log(Level.INFO, ReflectionToStringBuilder.toString(fileConfiguration, ToStringStyle.MULTI_LINE_STYLE));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unable to load conf - using default configuration: " + e.getMessage());
			fileConfiguration = new UnavailableConfiguration();
		}
		return fileConfiguration;
	}

	/**
	 * Start everythings
	 * @param afterDeploymentValidation
	 * @param beanManager
	 */
	void afterDeploymentValidation(@Observes final AfterDeploymentValidation afterDeploymentValidation, final BeanManager beanManager) {
		LOGGER.log(Level.INFO, "Axon CDI Extension - Starting");
		beanManager.fireEvent(new BeforeStartingAxon());
		configurations.stream().forEach(configuration -> configuration.start());
		LOGGER.log(Level.INFO, "Axon CDI Extension - Started");
	}

}
