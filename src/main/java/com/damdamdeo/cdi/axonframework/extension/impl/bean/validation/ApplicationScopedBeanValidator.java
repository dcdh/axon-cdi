package com.damdamdeo.cdi.axonframework.extension.impl.bean.validation;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;

public class ApplicationScopedBeanValidator extends AbstractScopedBeanValidator {

	public ApplicationScopedBeanValidator(final Class<?> targetClazz) {
		super(targetClazz);
	}

	@Override
	protected Class<? extends Annotation> targetScoped() {
		return ApplicationScoped.class;
	}

}
