package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.AggregateRootBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

// order of call is very important
public class AggregatesCdiConfigurer extends AbstractCdiConfiguration {

	public AggregatesCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);

		for (AggregateRootBeanInfo aggregateRootBeanInfo : executionContext.aggregateRootBeanInfos()) {
			AggregateConfigurer aggregateConf = AggregateConfigurer.defaultConfiguration(aggregateRootBeanInfo.type());
			if (executionContext.hasAnEventStoreBean(beanManager)) {
				SnapshotTriggerDefinition snapshotTriggerDefinition = (SnapshotTriggerDefinition) Proxy.newProxyInstance(
					SnapshotTriggerDefinition.class.getClassLoader(),
					new Class[] { SnapshotTriggerDefinition.class },
					new SnapshotTriggerDefinitionInvocationHandler(beanManager, executionContext));
				aggregateConf.configureSnapshotTrigger(c -> snapshotTriggerDefinition);
			}
			configurer.configureAggregate(aggregateConf);
		}
	}

	private class SnapshotTriggerDefinitionInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final ExecutionContext executionContext;
		private SnapshotTriggerDefinition snapshotTriggerDefinition;

		public SnapshotTriggerDefinitionInvocationHandler(final BeanManager beanManager, final ExecutionContext executionContext) throws NoSuchMethodException, SecurityException {
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
