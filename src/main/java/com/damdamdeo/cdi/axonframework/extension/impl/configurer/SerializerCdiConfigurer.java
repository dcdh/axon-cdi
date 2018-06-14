package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.serialization.Serializer;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class SerializerCdiConfigurer extends AbstractCdiConfiguration {

	private static final Logger LOGGER = Logger.getLogger(SerializerCdiConfigurer.class.getName());

	public SerializerCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
		if (executionContext.hasASerializerBean(beanManager)) {
			Serializer serializer = (Serializer) Proxy.newProxyInstance(
				Serializer.class.getClassLoader(),
				new Class[] { Serializer.class },
				new SerializerInvocationHandler(beanManager, executionContext));
			// only one can be registered per configurer
			configurer.configureSerializer(c -> serializer);
		} else {
			LOGGER.log(Level.INFO, "Serializer - none defined, using XStreamSerializer as default one");
		}
	}

	private class SerializerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final ExecutionContext executionContext;
		private Serializer serializer;

		public SerializerInvocationHandler(final BeanManager beanManager, final ExecutionContext executionContext) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.executionContext = Objects.requireNonNull(executionContext);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (serializer == null) {
				serializer = executionContext.getSerializerReference(beanManager);
			}
			return method.invoke(serializer, args);
		}

	}

}
