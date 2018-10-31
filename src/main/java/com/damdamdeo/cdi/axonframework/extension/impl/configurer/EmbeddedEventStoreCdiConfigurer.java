package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class EmbeddedEventStoreCdiConfigurer implements AxonCdiConfigurer {

	private static final Logger LOGGER = Logger.getLogger(EmbeddedEventStoreCdiConfigurer.class.getName());

	@Override
	public void setUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws RuntimeException {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
		try {
			if (executionContext.hasAnEventStorageEngineBean(beanManager)) {
				EventStorageEngine eventStorageEngine = (EventStorageEngine) Proxy.newProxyInstance(
						EventStorageEngine.class.getClassLoader(),
						new Class[] { EventStorageEngine.class },
						new EventStorageEngineInvocationHandler(beanManager, executionContext));
				// only one can be registered by configurer
				configurer.configureEmbeddedEventStore(c -> eventStorageEngine);
			} else {
				// default InMemoryEventStorageEngine...
				LOGGER.log(Level.WARNING, "EventStore - none defined, using EmbeddedEventStore with InMemoryEventStorageEngine");
				configurer.configureEmbeddedEventStore(c -> new InMemoryEventStorageEngine());
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private class EventStorageEngineInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final ExecutionContext executionContext;
		private EventStorageEngine eventStorageEngine;

		public EventStorageEngineInvocationHandler(final BeanManager beanManager, final ExecutionContext executionContext) throws NoSuchMethodException, SecurityException {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.executionContext = Objects.requireNonNull(executionContext);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (eventStorageEngine == null) {
				eventStorageEngine = executionContext.getEventStorageEngineReference(beanManager);
			}
			try {
				return method.invoke(eventStorageEngine, args);
			} catch (final InvocationTargetException e) {
				throw e.getCause();
			}
		}

	}

}
