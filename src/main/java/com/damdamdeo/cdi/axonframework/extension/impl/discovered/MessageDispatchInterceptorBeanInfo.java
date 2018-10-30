package com.damdamdeo.cdi.axonframework.extension.impl.discovered;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.lang3.Validate;

import com.damdamdeo.cdi.axonframework.support.AxonUtils;
import com.damdamdeo.cdi.axonframework.support.CdiUtils;

public class MessageDispatchInterceptorBeanInfo extends AbstractAnnotatedTypeInfo {

	private final Set<Annotation> qualifiers;

	private final Class<?> type;

	public MessageDispatchInterceptorBeanInfo(final AnnotatedType<?> annotatedType, final Set<Annotation> qualifiers) {
		super(annotatedType);
		Validate.validState(AxonUtils.isMessageDispatchInterceptor(annotatedType.getJavaClass()),
				"Bean is not a message dispatch interceptor: " + annotatedType.getJavaClass().getName());
		this.type = annotatedType.getJavaClass();
		this.qualifiers = Collections.unmodifiableSet(qualifiers);
	}

	public Bean<?> resolveBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return CdiUtils.getBean(beanManager, type, qualifiers);
	}

	public static MessageDispatchInterceptorBeanInfo of(final BeanManager beanManager, final AnnotatedType<?> annotatedType) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(annotatedType);
		Set<Annotation> qualifiers = extractQualifiers(beanManager, annotatedType);
		return new MessageDispatchInterceptorBeanInfo(annotatedType, qualifiers);
	}

	private static Set<Annotation> extractQualifiers(final BeanManager beanManager,
			final AnnotatedType<?> annotatedType) {
		Set<Annotation> defaultQualifiers = CdiUtils.qualifiers(beanManager, annotatedType);
		return CdiUtils.normalizedQualifiers(defaultQualifiers);
	}

}
