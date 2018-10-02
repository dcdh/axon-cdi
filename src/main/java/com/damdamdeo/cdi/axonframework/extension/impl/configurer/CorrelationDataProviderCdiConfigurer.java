package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.messaging.correlation.CorrelationDataProvider;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class CorrelationDataProviderCdiConfigurer extends AbstractCdiConfiguration {

	private static final Logger LOGGER = Logger.getLogger(CorrelationDataProviderCdiConfigurer.class.getName());

	public CorrelationDataProviderCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
		if (executionContext.hasACorrelationDataProviderBean(beanManager)) {
			CorrelationDataProvider correlationDataProvider = (CorrelationDataProvider) Proxy.newProxyInstance(
				CorrelationDataProvider.class.getClassLoader(),
				new Class[] { CorrelationDataProvider.class },
				new CorrelationDataProviderInvocationHandler(beanManager, executionContext));
			// only one can be registered per configurer
			configurer.configureCorrelationDataProviders(c -> Collections.singletonList(correlationDataProvider));
		} else {
			LOGGER.log(Level.INFO, "CorrelationDataProvider - none defined, using default one");
		}
	}

	private class CorrelationDataProviderInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final ExecutionContext executionContext;
		private CorrelationDataProvider correlationDataProvider;

		public CorrelationDataProviderInvocationHandler(final BeanManager beanManager, final ExecutionContext executionContext) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.executionContext = Objects.requireNonNull(executionContext);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (correlationDataProvider == null) {
				correlationDataProvider = executionContext.getCorrelationDataProviderReference(beanManager);
			}
			try {
				return method.invoke(correlationDataProvider, args);
			} catch (final InvocationTargetException e) {
				throw e.getCause();
			}
		}

	}

}
