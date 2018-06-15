package com.damdamdeo.cdi.axonframework.extension.impl.configurer;

import com.damdamdeo.cdi.axonframework.extension.impl.bean.commandbus.CommandBusToUse;

public class YamlConfiguration implements FileConfiguration {

	public static class YamlEventCountSnapshotTriggerDefinition implements EventCountSnapshotTriggerDefinition {

		private Integer threshold;

		@Override
		public Integer threshold() {
			return threshold;
		}

		public Integer getThreshold() {
			return threshold;
		}

		public void setThreshold(Integer threshold) {
			this.threshold = threshold;
		}

	}

	private CommandBusToUse commandBus;

	private YamlEventCountSnapshotTriggerDefinition eventCountSnapshotTriggerDefinition;

	public CommandBusToUse getCommandBus() {
		return commandBus;
	}

	public void setCommandBus(final CommandBusToUse commandBus) {
		this.commandBus = commandBus;
	}

	public YamlEventCountSnapshotTriggerDefinition getEventCountSnapshotTriggerDefinition() {
		return eventCountSnapshotTriggerDefinition;
	}

	public void setEventCountSnapshotTriggerDefinition(
			YamlEventCountSnapshotTriggerDefinition eventCountSnapshotTriggerDefinition) {
		this.eventCountSnapshotTriggerDefinition = eventCountSnapshotTriggerDefinition;
	}

	@Override
	public CommandBusToUse commandBus() {
		return commandBus;
	}

	@Override
	public EventCountSnapshotTriggerDefinition eventCountSnapshotTriggerDefinition() {

		return eventCountSnapshotTriggerDefinition;

	}

}
