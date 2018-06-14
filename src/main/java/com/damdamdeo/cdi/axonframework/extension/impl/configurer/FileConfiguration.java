package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import com.damdamdeo.cdi.axonframework.extension.impl.bean.commandbus.CommandBusToUse;

public interface FileConfiguration {

	CommandBusToUse commandBus();

}
