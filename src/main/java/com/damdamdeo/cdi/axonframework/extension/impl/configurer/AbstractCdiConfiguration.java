package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public abstract class AbstractCdiConfiguration implements AxonCdiConfigurer {

	private final AxonCdiConfigurer original;

	public AbstractCdiConfiguration(final AxonCdiConfigurer original) {
		this.original = Objects.requireNonNull(original);
	}

	public Configurer setUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
		final Configurer originalConfigurer = original.setUp(configurer, beanManager, executionContext, fileConfiguration);
		concreateCdiSetUp(originalConfigurer, beanManager, executionContext, fileConfiguration);
		return originalConfigurer;
	};

	protected abstract void concreateCdiSetUp(Configurer configurer, BeanManager beanManager, ExecutionContext executionContext, FileConfiguration fileConfiguration) throws Exception;

}
