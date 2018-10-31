package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.config.SagaConfiguration;
import org.axonframework.eventhandling.saga.repository.SagaStore;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.SagaBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.SagaBeanInfo.QualifierType;

// cf. https://docs.axonframework.org/part-ii-domain-logic/sagas
public class SagaConfigurationsCdiConfigurer implements AxonCdiConfigurer {

	private static final Logger LOGGER = Logger.getLogger(SagaConfigurationsCdiConfigurer.class.getName());

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws RuntimeException {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
		executionContext.sagaBeanInfos()
			.stream()
			.forEach(sagaBeanInfo -> {
				// can have multiple
				SagaConfiguration sagaConfiguration = SagaConfiguration.subscribingSagaManager(sagaBeanInfo.type());
				Bean<?> sagaStoreBean = sagaBeanInfo.resolveBean(beanManager, QualifierType.SAGA_STORE);
				if (sagaStoreBean != null) {
					sagaConfiguration.configureSagaStore(c -> (SagaStore) Proxy.newProxyInstance(
						SagaStore.class.getClassLoader(),
						new Class[] { SagaStore.class },
						new SagaStoreInvocationHandler(beanManager, sagaBeanInfo)));
				} else {
					LOGGER.log(Level.INFO, "Serializer - none defined, using InMemorySagaStore as default one");
				}
				SagaConfiguration.trackingSagaManager(sagaBeanInfo.type());
				configurer.registerModule(sagaConfiguration);
			});
	}

	private class SagaStoreInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final SagaBeanInfo sagaBeanInfo;
		private SagaStore<?> sagaStore;

		public SagaStoreInvocationHandler(final BeanManager beanManager, final SagaBeanInfo sagaBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.sagaBeanInfo = Objects.requireNonNull(sagaBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (sagaStore == null) {
				sagaStore = (SagaStore<?>) sagaBeanInfo.getReference(beanManager, QualifierType.SAGA_STORE);
			}
			try {
				return method.invoke(sagaStore, args);
			} catch (final InvocationTargetException e) {
				throw e.getCause();
			}
		}

	}

}
