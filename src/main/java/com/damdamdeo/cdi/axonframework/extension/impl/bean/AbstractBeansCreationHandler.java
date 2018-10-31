package com.damdamdeo.cdi.axonframework.extension.impl.bean;

import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configuration;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

public abstract class AbstractBeansCreationHandler implements BeansCreationHandler {

	@Override
	public void create(final AfterBeanDiscovery afterBeanDiscovery,
				final BeanManager beanManager, final ExecutionContext executionContext,
				final Configuration configuration) {
		Objects.requireNonNull(afterBeanDiscovery);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Set<Bean<?>> newBeansToAdd = concreateCreateBean(beanManager, executionContext, configuration);
		newBeansToAdd.stream().forEach(newBeanToAdd -> afterBeanDiscovery.addBean(newBeanToAdd));
	}

	protected abstract Set<Bean<?>> concreateCreateBean(BeanManager beanManager, ExecutionContext executionContext, Configuration configuration);

}
