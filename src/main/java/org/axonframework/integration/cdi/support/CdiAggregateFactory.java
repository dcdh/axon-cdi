package org.axonframework.integration.cdi.support;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.common.annotation.ClasspathParameterResolverFactory;
import org.axonframework.common.annotation.MultiParameterResolverFactory;
import org.axonframework.common.annotation.ParameterResolverFactory;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.GenericAggregateFactory;

/**
 * AggregateFactory implementation that inject CDI beans to create new uninitialized
 * instances of Aggregates.
 *
 * @param <T>
 *            The type of aggregate generated by this aggregate factory
 */
public class CdiAggregateFactory<T extends EventSourcedAggregateRoot<?>>
		extends GenericAggregateFactory<T> {

	private final BeanManager beanManager;

	public CdiAggregateFactory(BeanManager beanManager, Class<T> aggregateType,
			ParameterResolverFactory parameterResolverFactory) {
		super(aggregateType, parameterResolverFactory == null
				? createDefaultParameterResolverFactory(beanManager, aggregateType)
				: parameterResolverFactory);
		this.beanManager = beanManager;
	}

	public CdiAggregateFactory(BeanManager beanManager, Class<T> aggregateType) {
		this(beanManager, aggregateType, null);
	}

	private static <T extends EventSourcedAggregateRoot<?>> ParameterResolverFactory createDefaultParameterResolverFactory(
			BeanManager beanManager, Class<T> aggregateType) {
		return MultiParameterResolverFactory.ordered(
				ClasspathParameterResolverFactory.forClass(aggregateType),
				new CdiParameterResolverFactory(beanManager));
	}

	@Override
	protected T postProcessInstance(T aggregate) {
		return CdiUtils.injectFields(beanManager, super.postProcessInstance(aggregate));
	}
}
