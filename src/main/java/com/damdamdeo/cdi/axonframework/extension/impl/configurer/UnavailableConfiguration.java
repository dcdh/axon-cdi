package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import com.damdamdeo.cdi.axonframework.extension.impl.bean.commandbus.CommandBusToUse;

public class UnavailableConfiguration implements FileConfiguration {

	@Override
	public CommandBusToUse commandBus() {
		return CommandBusToUse.SIMPLE;
	}

}
