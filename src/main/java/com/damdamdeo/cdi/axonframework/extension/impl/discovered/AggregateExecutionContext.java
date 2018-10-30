package com.damdamdeo.cdi.axonframework.extension.impl.discovered;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.lang3.Validate;
import org.axonframework.commandhandling.model.AggregateRoot;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.serialization.Serializer;

import com.codahale.metrics.MetricRegistry;

public class AggregateExecutionContext implements ExecutionContext {

	private final List<AggregateRootBeanInfo> aggregateRootBeanInfos = new ArrayList<>();

	private final List<SagaBeanInfo> sagaBeanInfos = new ArrayList<>();

	private final List<MessageDispatchInterceptorBeanInfo> messageDispatchInterceptorBeanInfos = new ArrayList<>();

	private final List<MessageHandlerInterceptorBeanInfo> messageHandlerInterceptorBeanInfos = new ArrayList<>();

	private final List<CommandHandlerBeanInfo> commandHandlerBeanInfos = new ArrayList<>();

	private final List<EventHandlerBeanInfo> eventHandlerBeanInfos = new ArrayList<>();

	private final MetricRegistry metricRegistry;

	public AggregateExecutionContext(final AggregateRootBeanInfo aggregateRootBeanInfo) {
		this.aggregateRootBeanInfos.add(aggregateRootBeanInfo);
		this.metricRegistry = new MetricRegistry();
	}
	
	@Override
	public boolean registerIfSameContext(AggregateRootBeanInfo aggregateRootBeanInfo) {
		Objects.requireNonNull(aggregateRootBeanInfo);
		if (aggregateRootBeanInfos.get(0).isSameContext(aggregateRootBeanInfo)) {
			aggregateRootBeanInfos.add(aggregateRootBeanInfo);
			return true;
		}
		return false;
	}

	@Override
	public boolean registerIfSameContext(final SagaBeanInfo sagaBeanInfo) {
		Objects.requireNonNull(sagaBeanInfo);
		if (aggregateRootBeanInfos.get(0).isSameContext(sagaBeanInfo)) {
			sagaBeanInfos.add(sagaBeanInfo);
			return true;
		}
		return false;
	}

	@Override
	public boolean registerIfSameContext(final MessageDispatchInterceptorBeanInfo messageDispatchInterceptorBeanInfo) {
		Objects.requireNonNull(messageDispatchInterceptorBeanInfo);
		if (aggregateRootBeanInfos.get(0).isSameContext(messageDispatchInterceptorBeanInfo)) {
			messageDispatchInterceptorBeanInfos.add(messageDispatchInterceptorBeanInfo);
			return true;
		}
		return false;
	}

	@Override
	public boolean registerIfSameContext(final MessageHandlerInterceptorBeanInfo messageHandlerInterceptorBeanInfo) {
		Objects.requireNonNull(messageHandlerInterceptorBeanInfo);
		if (aggregateRootBeanInfos.get(0).isSameContext(messageHandlerInterceptorBeanInfo)) {
			messageHandlerInterceptorBeanInfos.add(messageHandlerInterceptorBeanInfo);
			return true;
		}
		return false;
	}

	@Override
	public boolean registerIfSameContext(final CommandHandlerBeanInfo commandHandlerBeanInfo) {
		Objects.requireNonNull(commandHandlerBeanInfo);
		Validate.validState(!commandHandlerBeanInfo.annotatedType().isAnnotationPresent(AggregateRoot.class), "Un aggregat n'est pas un bean command handler !!!");
		if (aggregateRootBeanInfos.get(0).isSameContext(commandHandlerBeanInfo)) {
			commandHandlerBeanInfos.add(commandHandlerBeanInfo);
			return true;
		}
		return false;
	}

	@Override
	public boolean registerIfSameContext(final EventHandlerBeanInfo eventHandlerBeanInfo) {
		Objects.requireNonNull(eventHandlerBeanInfo);
		if (aggregateRootBeanInfos.get(0).isSameContext(eventHandlerBeanInfo)) {
			eventHandlerBeanInfos.add(eventHandlerBeanInfo);
			return true;
		}
		return false;
	}

	@Override
	public List<AggregateRootBeanInfo> aggregateRootBeanInfos() {
		return Collections.unmodifiableList(aggregateRootBeanInfos);
	}

	@Override
	public List<SagaBeanInfo> sagaBeanInfos() {
		return Collections.unmodifiableList(sagaBeanInfos);
	}

	@Override
	public List<MessageDispatchInterceptorBeanInfo> messageDispatchInterceptorBeanInfos() {
		return Collections.unmodifiableList(messageDispatchInterceptorBeanInfos);
	}

	@Override
	public List<MessageHandlerInterceptorBeanInfo> messageHandlerInterceptorBeanInfos() {
		return Collections.unmodifiableList(messageHandlerInterceptorBeanInfos);
	}

	@Override
	public List<CommandHandlerBeanInfo> commandHandlerBeanInfos() {
		return Collections.unmodifiableList(commandHandlerBeanInfos);
	}

	@Override
	public List<EventHandlerBeanInfo> eventHandlerBeanInfos() {
		return Collections.unmodifiableList(eventHandlerBeanInfos);
	}

	@Override
	public Set<Annotation> commandGatewayQualifiers() {
		return aggregateRootBeanInfos.get(0).qualifiers(QualifierType.COMMAND_GATEWAY);
	}

	@Override
	public Set<Annotation> eventSchedulerQualifiers() {
		return aggregateRootBeanInfos.get(0).qualifiers(QualifierType.EVENT_SCHEDULER);
	}

	@Override
	public boolean hasAnEventSchedulerBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.EVENT_SCHEDULER);
	}

	@Override
	public boolean hasACommandGatewayBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.COMMAND_GATEWAY);
	}

	@Override
	public boolean hasAnEventStoreBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.EVENT_BUS);
	}

	@Override
	public boolean hasAnEventStorageEngineBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.EVENT_STORAGE_ENGINE);
	}

	@Override
	public boolean hasATransactionManagerBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.TRANSACTION_MANAGER);
	}

	@Override
	public boolean hasATokenStoreBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.TOKEN_STORE);
	}

	@Override
	public boolean hasASerializerBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.SERIALIZER);
	}

	@Override
	public boolean hasACorrelationDataProviderBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return aggregateRootBeanInfos.get(0).hasBean(beanManager, QualifierType.CORRELATION_DATA_PROVIDER);
	}

	// EventStore extends EventBus
	@Override
	public EventStore getEventStoreReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (EventStore) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.EVENT_BUS);
	}

	@Override
	public EventStorageEngine getEventStorageEngineReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (EventStorageEngine) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.EVENT_STORAGE_ENGINE);
	}

	@Override
	public TransactionManager getTransactionManagerReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (TransactionManager) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.TRANSACTION_MANAGER);
	}

	@Override
	public TokenStore getTokenStoreReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (TokenStore) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.TOKEN_STORE);
	}

	@Override
	public Serializer getSerializerReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (Serializer) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.SERIALIZER);
	}

	@Override
	public CorrelationDataProvider getCorrelationDataProviderReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (CorrelationDataProvider) aggregateRootBeanInfos.get(0).getReference(beanManager, QualifierType.CORRELATION_DATA_PROVIDER);
	}

	@Override
	public MetricRegistry metricRegistry() {
		return metricRegistry;
	}

}
