package com.damdamdeo.cdi.axonframework.extension.impl.bean.validation;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Dependent;

public class DependentScopedBeanValidator extends AbstractScopedBeanValidator {

	public DependentScopedBeanValidator(final Class<?> targetClazz) {
		super(targetClazz);
	}

	@Override
	protected Class<? extends Annotation> targetScoped() {
		return Dependent.class;
	}

}
