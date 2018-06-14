package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.commandhandling.disruptor.DisruptorCommandBus;
import org.axonframework.config.Configurer;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

/**
 * cf. AggregateConfiguration
 * cf. org.axonframework.config.AggregateConfigurer<A>
 * 
 * The problematic code is
 * if (c.commandBus() instanceof DisruptorCommandBus) {
 * 
 * by using a proxy to lazy load the command bean produced the previous line is not working anymore.
 * So to avoid to redefined this code I used a yaml conf to setup which implementation to used.
 * The configuration scoped is defined to the whole application.
 * 
 * @author damien
 *
 */
public class CommandBusCdiConfigurer extends AbstractCdiConfiguration {

	public CommandBusCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
		switch (fileConfiguration.commandBus()) {
		case SIMPLE:
			// nothing to do. By default SimpleCommandBus is used.
			// cf. org.axonframework.config.DefaultConfigurer.DefaultConfigurer()
			// components.put(CommandBus.class, new Component<>(config, "commandBus", this::defaultCommandBus));
			break;
		case DISRUPTOR:
			configurer.configureCommandBus(c -> new DisruptorCommandBus());
			break;
		default:
			throw new IllegalStateException(String.format("Unsupported type '%s'", fileConfiguration.commandBus().name()));
		}
	}

}
