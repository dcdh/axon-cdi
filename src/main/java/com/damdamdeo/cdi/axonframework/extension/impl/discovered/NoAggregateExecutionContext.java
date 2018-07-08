package com.damdamdeo.cdi.axonframework.extension.impl.discovered;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.serialization.Serializer;

import com.codahale.metrics.MetricRegistry;
import com.damdamdeo.cdi.axonframework.support.CdiUtils;

public class NoAggregateExecutionContext implements ExecutionContext {

	private final List<EventHandlerBeanInfo> eventHandlerBeanInfos = new ArrayList<>();

	private final MetricRegistry metricRegistry;

	public NoAggregateExecutionContext() {
		this.metricRegistry = new MetricRegistry();
	}

	@Override
	public boolean registerIfSameContext(AggregateRootBeanInfo aggregateRootBeanInfo) {
		throw new UnsupportedOperationException("No Aggregate defined !");
	}

	@Override
	public boolean registerIfSameContext(final SagaBeanInfo sagaBeanInfo) {
		throw new UnsupportedOperationException("No Aggregate defined !");
	}

	@Override
	public boolean registerIfSameContext(final CommandHandlerBeanInfo commandHandlerBeanInfo) {
		throw new UnsupportedOperationException("No Aggregate defined !");
	}

	@Override
	public boolean registerIfSameContext(final EventHandlerBeanInfo eventHandlerBeanInfo) {
		Objects.requireNonNull(eventHandlerBeanInfo);
		return eventHandlerBeanInfos.add(eventHandlerBeanInfo);
	}

	@Override
	public List<AggregateRootBeanInfo> aggregateRootBeanInfos() {
		return Collections.emptyList();
	}

	@Override
	public List<SagaBeanInfo> sagaBeanInfos() {
		return Collections.emptyList();
	}

	@Override
	public List<CommandHandlerBeanInfo> commandHandlerBeanInfos() {
		return Collections.emptyList();
	}

	@Override
	public List<EventHandlerBeanInfo> eventHandlerBeanInfos() {
		return Collections.unmodifiableList(eventHandlerBeanInfos);
	}

	@Override
	public Set<Annotation> commandGatewayQualifiers() {
		return Collections.emptySet();
	}

	@Override
	public Set<Annotation> eventSchedulerQualifiers() {
		return Collections.emptySet();
	}

	@Override
	public boolean hasAnEventSchedulerBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return Optional.ofNullable(CdiUtils.getBean(beanManager,
				QualifierType.EVENT_SCHEDULER.clazz)).isPresent();
	}

	@Override
	public boolean hasACommandGatewayBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return Optional.ofNullable(CdiUtils.getBean(beanManager,
				QualifierType.COMMAND_GATEWAY.clazz)).isPresent();
	}

	@Override
	public boolean hasAnEventStoreBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return Optional.ofNullable(CdiUtils.getBean(beanManager,
				QualifierType.EVENT_BUS.clazz)).isPresent();
	}

	@Override
	public boolean hasAnEventStorageEngineBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return Optional.ofNullable(CdiUtils.getBean(beanManager,
				QualifierType.EVENT_STORAGE_ENGINE.clazz)).isPresent();
	}

	@Override
	public boolean hasATransactionManagerBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return Optional.ofNullable(CdiUtils.getBean(beanManager,
				QualifierType.TRANSACTION_MANAGER.clazz)).isPresent();
	}

	@Override
	public boolean hasATokenStoreBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return Optional.ofNullable(CdiUtils.getBean(beanManager,
				QualifierType.TOKEN_STORE.clazz)).isPresent();
	}

	@Override
	public boolean hasASerializerBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return Optional.ofNullable(CdiUtils.getBean(beanManager,
				QualifierType.SERIALIZER.clazz)).isPresent();
	}

	@Override
	public boolean hasACorrelationDataProviderBean(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return Optional.ofNullable(CdiUtils.getBean(beanManager,
				QualifierType.CORRELATION_DATA_PROVIDER.clazz)).isPresent();
	}

	@Override
	public EventStore getEventStoreReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (EventStore) CdiUtils.getReference(beanManager,
				QualifierType.EVENT_BUS.clazz,
				Collections.emptySet());
	}

	@Override
	public EventStorageEngine getEventStorageEngineReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (EventStorageEngine) CdiUtils.getReference(beanManager,
				QualifierType.EVENT_STORAGE_ENGINE.clazz,
				Collections.emptySet());
	}

	@Override
	public TransactionManager getTransactionManagerReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (TransactionManager) CdiUtils.getReference(beanManager,
				QualifierType.TRANSACTION_MANAGER.clazz,
				Collections.emptySet());
	}

	@Override
	public TokenStore getTokenStoreReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (TokenStore) CdiUtils.getReference(beanManager,
				QualifierType.TOKEN_STORE.clazz,
				Collections.emptySet());
	}

	@Override
	public Serializer getSerializerReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (Serializer) CdiUtils.getReference(beanManager,
				QualifierType.SERIALIZER.clazz,
				Collections.emptySet());
	}

	@Override
	public CorrelationDataProvider getCorrelationDataProviderReference(final BeanManager beanManager) {
		Objects.requireNonNull(beanManager);
		return (CorrelationDataProvider) CdiUtils.getReference(beanManager,
				QualifierType.CORRELATION_DATA_PROVIDER.clazz,
				Collections.emptySet());
	}

	@Override
	public MetricRegistry metricRegistry() {
		return this.metricRegistry;
	}

}
