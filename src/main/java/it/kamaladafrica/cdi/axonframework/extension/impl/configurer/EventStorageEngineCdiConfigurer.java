package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class EventStorageEngineCdiConfigurer extends AbstractCdiConfiguration {

	public EventStorageEngineCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		if (executionContext.hasAnEventStorageEngineBean(beanManager)) {
			EventStorageEngine eventStorageEngine = (EventStorageEngine) Proxy.newProxyInstance(
				EventStorageEngine.class.getClassLoader(),
				new Class[] { EventStorageEngine.class },
				new EventStorageEngineInvocationHandler(beanManager, executionContext));
			// only one can be registered by configurer
			configurer.configureEmbeddedEventStore(c -> eventStorageEngine);			
		}
	}

	private class EventStorageEngineInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final ExecutionContext executionContext;
		private EventStorageEngine eventStorageEngine;

		public EventStorageEngineInvocationHandler(final BeanManager beanManager, final ExecutionContext executionContext) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.executionContext = Objects.requireNonNull(executionContext);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (eventStorageEngine == null) {
				eventStorageEngine = executionContext.getEventStorageEngineReference(beanManager);
			}
			return method.invoke(eventStorageEngine, args);
		}

	}

}