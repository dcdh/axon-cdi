package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

/**
 * Used by AggregateConfigurer 
 * cf. 
 * snapshotTriggerDefinition = new Component<>(() -> parent, name("snapshotTriggerDefinition"),
 *                                                  c -> NoSnapshotTriggerDefinition.INSTANCE);
 * => Register as a component
 * @author damien
 *
 * cf. org.axonframework.config.AggregateConfigurer<A>
 */
public class SnapshotterTriggerDefinitionCdiConfigurer extends AbstractCdiConfiguration {

	private static final Logger LOGGER = Logger.getLogger(SnapshotterTriggerDefinitionCdiConfigurer.class.getName());

	public SnapshotterTriggerDefinitionCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration)
			throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
		if (executionContext.hasASnapshotteTriggerDefinitionBean(beanManager)) {
			SnapshotTriggerDefinition snapshotTriggerDefinition = (SnapshotTriggerDefinition) Proxy.newProxyInstance(
					SnapshotTriggerDefinition.class.getClassLoader(),
					new Class[] { SnapshotTriggerDefinition.class },
					new SnapshotTriggerDefinitionInvocationHandler(beanManager, executionContext));
			// only one can be registered per configurer
			configurer.registerComponent(SnapshotTriggerDefinition.class, c -> snapshotTriggerDefinition);
		} else {
			LOGGER.log(Level.WARNING, "SnapshotterTriggerDefinition - none defined, using NoSnapshotTriggerDefinition");
		}
	}

	private class SnapshotTriggerDefinitionInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final ExecutionContext executionContext;
		private SnapshotTriggerDefinition snapshotTriggerDefinition;

		public SnapshotTriggerDefinitionInvocationHandler(final BeanManager beanManager, final ExecutionContext executionContext) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.executionContext = Objects.requireNonNull(executionContext);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (snapshotTriggerDefinition == null) {
				snapshotTriggerDefinition = executionContext.getSnapshotTriggerDefinitionReference(beanManager);
			}
			return method.invoke(snapshotTriggerDefinition, args);
		}

	}

}
