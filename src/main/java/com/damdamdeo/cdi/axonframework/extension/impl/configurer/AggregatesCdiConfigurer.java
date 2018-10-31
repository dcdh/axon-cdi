package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.AggregateSnapshotter;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.GenericAggregateFactory;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.AggregateRootBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

// order of call is very important
/**
 * Settings the EventCountSnapshotTriggerDefinition is a pain because we need an AggregateSnapshotter
 * which needs an EventStore and an AggregateFactory...
 * So instead of exposing them as cdi bean I have prefered to defined the EventCountSnapshotTriggerDefinition by using
 * a yaml file. Other dependencies (eventStore, AggregateFactory,...) are retrieved by using the configurer (already setup by previous configuration).
 * 
 * The following code is equivalent to this:
 * @Bean
 * public AggregateFactory<Account> aggregateFactory(){
 *     return new GenericAggregateFactory<Account>(Account.class);
 * }
 * 
 * @Bean
 * public EventCountSnapshotTriggerDefinition countSnapshotTriggerDefinition(){
 *     return new EventCountSnapshotTriggerDefinition(snapShotter(), 3);
 * }
 * 
 * @Bean
 * public Snapshotter snapShotter(){
 *     return new AggregateSnapshotter(eventStore, aggregateFactory());
 * }
 * 
 * @Bean
 * public EventSourcingRepository<Account> accountRepository(){
 *     return new EventSourcingRepository<>(aggregateFactory(), eventStore, countSnapshotTriggerDefinition());
 * }
 * 
 * 
 * @author damien
 *
 */

public class AggregatesCdiConfigurer implements AxonCdiConfigurer {

	private static final Logger LOGGER = Logger.getLogger(AggregatesCdiConfigurer.class.getName());

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws RuntimeException {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
		LOGGER.log(Level.INFO, String.format("EventCountSnapshotTriggerDefinition - using threshold '%d'", fileConfiguration.eventCountSnapshotTriggerDefinition().threshold()));

		for (AggregateRootBeanInfo aggregateRootBeanInfo : executionContext.aggregateRootBeanInfos()) {
			final AggregateConfigurer aggregateConf = AggregateConfigurer.defaultConfiguration(aggregateRootBeanInfo.type());
			// I found no way to retrieved the 'aggregateFactory' setup by the 'AggregateConfigurer'.
			// So I decided to create a new one and setup it at needed places.
			final AggregateFactory<?> aggregateFactory = new GenericAggregateFactory<>(aggregateRootBeanInfo.type());
			aggregateConf.configureAggregateFactory(c -> aggregateFactory);

			aggregateConf.configureSnapshotTrigger(new Function<Configuration, SnapshotTriggerDefinition>() {

				@Override
				public SnapshotTriggerDefinition apply(final Configuration c) {
					final Snapshotter snapshotter = new AggregateSnapshotter(c.eventStore(), aggregateFactory);
					return new EventCountSnapshotTriggerDefinition(snapshotter,
							fileConfiguration.eventCountSnapshotTriggerDefinition().threshold());
				}

			});
			configurer.configureAggregate(aggregateConf);
		}
	}

}
