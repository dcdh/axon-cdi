package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.config.EventHandlingConfiguration;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.EventHandlerBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

// cf. AxonAutoConfiguration : configureEventHandling
// also DefaultConfigurerTest : testRegisterSeveralModules
public class EventHandlersCdiConfigurer implements AxonCdiConfigurer {

	@Override
	public void setUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws RuntimeException {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);

		EventHandlingConfiguration eventHandlingConfiguration = new EventHandlingConfiguration();
		try {
			for (final EventHandlerBeanInfo eventHandlerBeanInfo : executionContext.eventHandlerBeanInfos()) {
				// use byte-buddy
				// cf. CommandHandlersCdiConfigurer for more information
				Class<?> proxyEventHandler = new ByteBuddy()
						.subclass(eventHandlerBeanInfo.type())
						.method(ElementMatchers.any())
						.intercept(InvocationHandlerAdapter.of(new EventHandlerInvocationHandler(beanManager, eventHandlerBeanInfo)))
						.make()
						.load(eventHandlerBeanInfo.type().getClassLoader())
						.getLoaded();
				Object instanceEventHandler = proxyEventHandler.newInstance();
				eventHandlingConfiguration.registerEventHandler(c -> instanceEventHandler);
				// By default we consider that we are tracking event handlers
				eventHandlingConfiguration.usingTrackingProcessors();
				// By default a tracking event processor is linked with the full qualified Aggregate
				// However when replaying event it can failed because order in the store is not respected.
				// By assigning the same event processor name on all aggregate order will be respected :)
				//			eventHandlingConfiguration.byDefaultAssignTo("SAME_AGGREGATE_GROUP");
				// passage par @ProcessingGroup
			}
			configurer.registerModule(eventHandlingConfiguration);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private class EventHandlerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final EventHandlerBeanInfo eventHandlerBeanInfo;
		private Object eventHandler;

		public EventHandlerInvocationHandler(final BeanManager beanManager, final EventHandlerBeanInfo eventHandlerBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.eventHandlerBeanInfo = Objects.requireNonNull(eventHandlerBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (eventHandler == null) {
				eventHandler = eventHandlerBeanInfo.getReference(beanManager);
			}
			try {
				return method.invoke(eventHandler, args);
			} catch (final InvocationTargetException e) {
				throw e.getCause();
			}
		}

	}

}
