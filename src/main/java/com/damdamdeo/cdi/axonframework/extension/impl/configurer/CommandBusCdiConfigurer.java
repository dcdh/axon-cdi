package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.config.Configurer;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.MessageHandlerInterceptor;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.MessageDispatchInterceptorBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.MessageHandlerInterceptorBeanInfo;

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

	private static final Logger LOGGER = Logger.getLogger(CommandBusCdiConfigurer.class.getName());

	public CommandBusCdiConfigurer(final AxonCdiConfigurer original) {
		super(original);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	protected void concreateCdiSetUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws Exception {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
		LOGGER.log(Level.INFO, String.format("CommandBus - using '%s'", fileConfiguration.commandBus()));
		final CommandBus commandBus;
		switch (fileConfiguration.commandBus()) {
		case SIMPLE:
			// nothing to do. By default SimpleCommandBus is used.
			// cf. org.axonframework.config.DefaultConfigurer.DefaultConfigurer()
			// components.put(CommandBus.class, new Component<>(config, "commandBus", this::defaultCommandBus));
// TODO final SimpleCommandBus simpleCommandBus = new SimpleCommandBus(transactionManager, messageMonitor);
			final SimpleCommandBus simpleCommandBus = new SimpleCommandBus();
			LOGGER.log(Level.INFO, String.format("'%d' messageHandlerInterceptorBeans to configure", executionContext.messageDispatchInterceptorBeanInfos().size()));
			executionContext.messageDispatchInterceptorBeanInfos()
				.stream()
				.forEach(messageDispatchInterceptorBeanInfo -> {
					final Bean<?> messageDispatchInterceptorBean = messageDispatchInterceptorBeanInfo.resolveBean(beanManager);
					if (messageDispatchInterceptorBean != null) {
						LOGGER.log(Level.INFO, String.format("Registering command handler message dispatch interceptor '%s'", messageDispatchInterceptorBean.getClass().toString()));
						simpleCommandBus.registerDispatchInterceptor((MessageDispatchInterceptor<Message<?>>) Proxy.newProxyInstance(
								MessageDispatchInterceptor.class.getClassLoader(),
								new Class[] { MessageDispatchInterceptor.class },
								new MessageDispatchInterceptorHandler(beanManager, messageDispatchInterceptorBeanInfo)));
					}
				});
			executionContext.messageHandlerInterceptorBeanInfos()
				.stream()
				.forEach(messageHandlerInterceptorBeanInfo -> {
					final Bean<?> messageHandlerInterceptorBean = messageHandlerInterceptorBeanInfo.resolveBean(beanManager);
					if (messageHandlerInterceptorBean != null) {
						LOGGER.log(Level.INFO, String.format("Registering command handler message handler interceptor '%s'", messageHandlerInterceptorBean.getClass().toString()));
						simpleCommandBus.registerHandlerInterceptor((MessageHandlerInterceptor<Message<?>>) Proxy.newProxyInstance(
								MessageHandlerInterceptor.class.getClassLoader(),
								new Class[] { MessageHandlerInterceptor.class },
								new MessageHandlerInterceptorHandler(beanManager, messageHandlerInterceptorBeanInfo)));
					}
				});
			commandBus = simpleCommandBus;
			break;
		case DISRUPTOR:
			throw new UnsupportedOperationException("not yet !");
		default:
			throw new IllegalStateException(String.format("Unsupported type '%s'", fileConfiguration.commandBus().name()));
		}
		configurer.configureCommandBus(c -> commandBus);
	}

	private class MessageDispatchInterceptorHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final MessageDispatchInterceptorBeanInfo messageDispatchInterceptorBeanInfo;
		private MessageDispatchInterceptor<Message<?>> messageDispatchInterceptor;

		public MessageDispatchInterceptorHandler(final BeanManager beanManager, final MessageDispatchInterceptorBeanInfo messageDispatchInterceptorBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.messageDispatchInterceptorBeanInfo = Objects.requireNonNull(messageDispatchInterceptorBeanInfo);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (messageDispatchInterceptor == null) {
				messageDispatchInterceptor = (MessageDispatchInterceptor<Message<?>>) messageDispatchInterceptorBeanInfo.getReference(beanManager);
			}
			try {
				return method.invoke(messageDispatchInterceptor, args);
			} catch (final InvocationTargetException e) {
				throw e.getCause();
			}
		}

	}

	private class MessageHandlerInterceptorHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final MessageHandlerInterceptorBeanInfo messageHandlerInterceptorBeanInfo;
		private MessageHandlerInterceptor<Message<?>> messageHandlerInterceptor;

		public MessageHandlerInterceptorHandler(final BeanManager beanManager, final MessageHandlerInterceptorBeanInfo messageHandlerInterceptorBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.messageHandlerInterceptorBeanInfo = Objects.requireNonNull(messageHandlerInterceptorBeanInfo);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (messageHandlerInterceptor == null) {
				messageHandlerInterceptor = (MessageHandlerInterceptor<Message<?>>) messageHandlerInterceptorBeanInfo.getReference(beanManager);
			}
			try {
				return method.invoke(messageHandlerInterceptor, args);
			} catch (final InvocationTargetException e) {
				throw e.getCause();
			}
		}

	}

}
