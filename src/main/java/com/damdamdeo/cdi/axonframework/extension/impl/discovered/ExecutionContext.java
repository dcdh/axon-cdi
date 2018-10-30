package com.damdamdeo.cdi.axonframework.extension.impl.discovered;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.serialization.Serializer;

import com.codahale.metrics.MetricRegistry;

public interface ExecutionContext {

	boolean registerIfSameContext(final AggregateRootBeanInfo aggregateRootBeanInfo);

	boolean registerIfSameContext(SagaBeanInfo sagaBeanInfo);

	boolean registerIfSameContext(MessageDispatchInterceptorBeanInfo messageDispatchInterceptorBeanInfo);

	boolean registerIfSameContext(MessageHandlerInterceptorBeanInfo messageHandlerInterceptorBeanInfo);

	boolean registerIfSameContext(CommandHandlerBeanInfo commandHandlerBeanInfo);

	boolean registerIfSameContext(EventHandlerBeanInfo eventHandlerBeanInfo);

	List<AggregateRootBeanInfo> aggregateRootBeanInfos();

	List<SagaBeanInfo> sagaBeanInfos();

	List<CommandHandlerBeanInfo> commandHandlerBeanInfos();

	List<EventHandlerBeanInfo> eventHandlerBeanInfos();

	List<MessageDispatchInterceptorBeanInfo> messageDispatchInterceptorBeanInfos();

	List<MessageHandlerInterceptorBeanInfo> messageHandlerInterceptorBeanInfos();

	Set<Annotation> commandGatewayQualifiers();

	Set<Annotation> eventSchedulerQualifiers();

	boolean hasAnEventSchedulerBean(BeanManager beanManager);

	boolean hasACommandGatewayBean(BeanManager beanManager);

	boolean hasAnEventStoreBean(BeanManager beanManager);

	boolean hasAnEventStorageEngineBean(BeanManager beanManager);

	boolean hasATransactionManagerBean(BeanManager beanManager);

	boolean hasATokenStoreBean(BeanManager beanManager);

	boolean hasASerializerBean(BeanManager beanManager);

	boolean hasACorrelationDataProviderBean(BeanManager beanManager);

	EventStore getEventStoreReference(BeanManager beanManager);

	EventStorageEngine getEventStorageEngineReference(BeanManager beanManager);

	TransactionManager getTransactionManagerReference(BeanManager beanManager);

	TokenStore getTokenStoreReference(BeanManager beanManager);

	Serializer getSerializerReference(BeanManager beanManager);

	CorrelationDataProvider getCorrelationDataProviderReference(BeanManager beanManager);

	MetricRegistry metricRegistry();

}
