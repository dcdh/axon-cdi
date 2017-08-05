package it.kamaladafrica.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.serialization.Serializer;

import it.kamaladafrica.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class CorrelationDataProviderCdiConfigurer extends AbstractCdiConfiguration {

	public CorrelationDataProviderCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		if (executionContext.hasACorrelationDataProviderBean(beanManager)) {
			Serializer serializer = (Serializer) Proxy.newProxyInstance(
				Serializer.class.getClassLoader(),
				new Class[] { Serializer.class },
				new CorrelationDataProviderInvocationHandler(beanManager, executionContext));
			// only one can be registered per configurer
			configurer.configureSerializer(c -> serializer);
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
			return method.invoke(correlationDataProvider, args);
		}

	}

}