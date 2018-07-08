package com.damdamdeo.cdi.axonframework.extension.impl.discovered;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.serialization.Serializer;

public enum QualifierType {

	DEFAULT(Object.class),
	COMMAND_GATEWAY(CommandGateway.class),
	EVENT_BUS(EventBus.class),
	TOKEN_STORE(TokenStore.class),
	TRANSACTION_MANAGER(TransactionManager.class),
	SERIALIZER(Serializer.class),
	EVENT_STORAGE_ENGINE(EventStorageEngine.class),
	EVENT_SCHEDULER(EventScheduler.class),
	CORRELATION_DATA_PROVIDER(CorrelationDataProvider.class);

	Class<?> clazz;

	private QualifierType(final Class<?> clazz) {
		this.clazz = clazz;
	}

}
