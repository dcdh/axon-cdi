package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public interface AxonCdiConfigurer {

	void setUp(Configurer configurer, BeanManager beanManager, ExecutionContext executionContext, FileConfiguration fileConfiguration) throws RuntimeException;

}
