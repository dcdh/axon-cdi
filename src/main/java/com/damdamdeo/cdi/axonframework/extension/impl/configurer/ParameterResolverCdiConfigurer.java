package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public class ParameterResolverCdiConfigurer implements AxonCdiConfigurer {

	@Override
	public void setUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws RuntimeException {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
// Registering this component lead to an exception:
// org.axonframework.messaging.annotation.UnsupportedHandlerException: Unable to resolver parameter 0
// So I don't use it
//		configurer.registerComponent(ParameterResolverFactory.class, c -> new CdiParameterResolverFactory(beanManager));
	}

}
