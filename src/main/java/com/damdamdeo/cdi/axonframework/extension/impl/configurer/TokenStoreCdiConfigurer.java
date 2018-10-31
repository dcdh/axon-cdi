package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;

import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;

import com.damdamdeo.cdi.axonframework.extension.impl.discovered.ExecutionContext;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

public class TokenStoreCdiConfigurer implements AxonCdiConfigurer {

	private static final Logger LOGGER = Logger.getLogger(TokenStoreCdiConfigurer.class.getName());

	@Override
	public void setUp(final Configurer configurer, final BeanManager beanManager, final ExecutionContext executionContext, final FileConfiguration fileConfiguration) throws RuntimeException {
		Objects.requireNonNull(configurer);
		Objects.requireNonNull(beanManager);
		Objects.requireNonNull(executionContext);
		Objects.requireNonNull(fileConfiguration);
		try {
			if (executionContext.hasATokenStoreBean(beanManager)) {
				Class<? extends TokenStore> proxyTokenStore = new ByteBuddy()
						.subclass(TokenStore.class)
						.method(ElementMatchers.any())
						.intercept(InvocationHandlerAdapter.of(new TokenStoreInvocationHandler(beanManager, executionContext)))
						.make()
						.load(TokenStore.class.getClassLoader())
						.getLoaded();
				TokenStore instanceTokenStore = proxyTokenStore.newInstance();
				configurer.registerComponent(TokenStore.class, c -> instanceTokenStore);
			} else {
				// default in memory
				LOGGER.log(Level.WARNING, "TokenStore - none defined, using InMemoryTokenStore");
				configurer.registerComponent(TokenStore.class, c -> new InMemoryTokenStore());
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private class TokenStoreInvocationHandler implements InvocationHandler {

		private final BeanManager beanManager;
		private final ExecutionContext executionContext;
		private TokenStore tokenStore;

		public TokenStoreInvocationHandler(final BeanManager beanManager, final ExecutionContext executionContext) {
			this.beanManager = Objects.requireNonNull(beanManager);
			this.executionContext = Objects.requireNonNull(executionContext);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (tokenStore == null) {
				tokenStore = executionContext.getTokenStoreReference(beanManager);
			}
			try {
				return method.invoke(tokenStore, args);
			} catch (final InvocationTargetException e) {
				throw e.getCause();
			}
		}

	}

}
