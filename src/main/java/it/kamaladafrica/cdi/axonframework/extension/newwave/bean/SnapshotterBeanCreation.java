package it.kamaladafrica.cdi.axonframework.extension.newwave.bean;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.axonframework.commandhandling.model.Repository;
import org.axonframework.config.Configuration;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.AggregateSnapshotter;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.Snapshotter;

import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo;
import it.kamaladafrica.cdi.axonframework.extension.newwave.discovered.AggregateRootBeanInfo.QualifierType;

// code coming from SpringAggregateSnapshotterFactoryBean and SpringAggregateSnapshotter
public class SnapshotterBeanCreation {

	// create AggregateSnapshotter bean
	// this bean is used by SnapshotterTriggerDefinitionCdiConfigurer
	// I can't used a producer because It would be too complicated to create it (ie I need the list of AggregateRepository)

	private final Set<AggregateRootBeanInfo> aggregateRootBeanInfo;

	private final Configuration configuration;

	public SnapshotterBeanCreation(final Set<AggregateRootBeanInfo> aggregateRootBeanInfo,
			final Configuration configuration) {
		this.aggregateRootBeanInfo = Objects.requireNonNull(aggregateRootBeanInfo);
		this.configuration = Objects.requireNonNull(configuration);
	}

	public void createBeans(final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
		Objects.requireNonNull(afterBeanDiscovery);
		Objects.requireNonNull(beanManager);
		classer les repository par qualifier
		qualifier, list<Repository>
		puis creer les AggregateSnapshotter
	}

	protected Set<Bean<?>> concreateCreateBean(final BeanManager beanManager, final AggregateRootBeanInfo aggregateRootBeanInfo,
			final Configuration configuration) {
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(aggregateRootBeanInfo);
		Objects.requireNonNull(configuration);

		BeanBuilder<Snapshotter> builder = new BeanBuilder<Snapshotter>(beanManager)
			.beanClass(Snapshotter.class)
			.qualifiers(aggregateRootBeanInfo.qualifiers(QualifierType.SNAPSHOTTER_TRIGGER_DEFINITION))
			.types(Snapshotter.class)
			.beanLifecycle(
				new AggregateSnapshotterContextualLifecycle<Snapshotter>(configuration));
		Bean<?> snapshotterBean = builder.create();
		return Collections.singleton(snapshotterBean);

	}

	private class AggregateSnapshotterContextualLifecycle<T extends Snapshotter> implements ContextualLifecycle<T> {

		private final Configuration configuration;

		public AggregateSnapshotterContextualLifecycle(final Configuration configuration) {
			this.configuration = Objects.requireNonNull(configuration);
		}

		@Override
		@SuppressWarnings("unchecked")
		public T create(final Bean<T> bean, final CreationalContext<T> creationalContext) {
			// putain, pourquoi dans la conf il n'expose pas les differents repository disponibles ... ce serait tellement plus simple...

			List<AggregateFactory<?>> aggregateFactories = aggregateRootBeanInfos.stream().filter(new Predicate<AggregateRootBeanInfo>() {

				@Override
				public boolean test(final AggregateRootBeanInfo aggregateRootInfo) {
					return CdiUtils.qualifiersMatch(aggregateRootInfo.normalizedQualifiers(), normalizedQualifiers);
				}

			}).map(new Function<AggregateRootBeanInfo, AggregateFactory<?>>() {

				@Override
				public AggregateFactory<?> apply(final AggregateRootBeanInfo aggregateRootBeanInfo) {
					Repository<?> repository = configuration.repository(aggregateRootBeanInfo.type());
					return ((EventSourcingRepository<?>) repository).getAggregateFactory();
				}

			}).collect(Collectors.toList());
			return (T) new AggregateSnapshotter(configuration.eventStore(),
					aggregateFactories);
		}

		@Override
		public void destroy(final Bean<T> bean, final T instance, final CreationalContext<T> creationalContext) {
			creationalContext.release();
		}

	}

}
