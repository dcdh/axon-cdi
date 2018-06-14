package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import com.damdamdeo.cdi.axonframework.extension.impl.bean.commandbus.CommandBusToUse;

public class YamlConfiguration implements FileConfiguration {

	private CommandBusToUse commandBus;

	public CommandBusToUse getCommandBus() {
		return commandBus;
	}

	public void setCommandBus(final CommandBusToUse commandBus) {
		this.commandBus = commandBus;
	}

	@Override
	public CommandBusToUse commandBus() {
		return commandBus;
	}

}
