package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.CommandHandlerBeanInfo;
import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

public class CommandHandlersCdiConfigurer implements AxonCdiConfigurer {

	@Override
	public void setUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws RuntimeException {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
		try {
			// can't use lambda because of the checked exceptions thrown by *proxyCommandHandler.newInstance()*
			for (final CommandHandlerBeanInfo commandHandlerBeanInfo: executionContext.commandHandlerBeanInfos()) {
				// Use a proxy to get reference (because it is not created yet)
				// I can't use Proxy from jdk because a CommandHandler doesn't implement an interface.
				// Go for byte-buddy :)
				final Class<?> proxyCommandHandler = new ByteBuddy()
						.subclass(commandHandlerBeanInfo.type())
						.method(ElementMatchers.any())
						.intercept(InvocationHandlerAdapter.of(new CommandHandlerInvocationHandler(beanManager, commandHandlerBeanInfo)))
						.make()
						.load(commandHandlerBeanInfo.type().getClassLoader())
						.getLoaded();
				Object instanceCommandHandler = proxyCommandHandler.newInstance();
				configurer.registerCommandHandler(c -> instanceCommandHandler);
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

	}

	private class CommandHandlerInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final CommandHandlerBeanInfo commandHandlerBeanInfo;
		private Object commandHandler;

		public CommandHandlerInvocationHandler(final BeanManager beanManager, final CommandHandlerBeanInfo commandHandlerBeanInfo) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.commandHandlerBeanInfo = Objects.requireNonNull(commandHandlerBeanInfo);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (commandHandler == null) {
				commandHandler = commandHandlerBeanInfo.getReference(beanManager);
			}
			try {
				return method.invoke(commandHandler, args);
			} catch (final InvocationTargetException e) {
				throw e.getCause();
			}
		}

	}

}
