/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smarthomatic.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.smarthomatic.SmarthomaticBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author arohde
 * @since 0.1.0
 */
public class SmarthomaticGenericBindingProvider extends
		AbstractGenericBindingProvider implements SmarthomaticBindingProvider {

	private static final Pattern TRANSFORMATION_PATTERN = Pattern
			.compile("(.*):(.*)");
	// We find the id of an deviceId in this map
	// Therefore this map is static
	private static HashMap<String, Integer> devices = new HashMap<String, Integer>();
	private static final Pattern CONFIG_PATTERN = Pattern.compile(".\\[(.*)]");

	public static void addDevice(String name, int deviceID) {
		devices.put(name, deviceID);
	}

	public static int getDevice(String name) {
		return devices.get(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "smarthomatic";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		// if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
		// throw new BindingConfigParseException("item '" + item.getName()
		// + "' is of type '" + item.getClass().getSimpleName()
		// +
		// "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
		// }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);

		if (bindingConfig.startsWith("<")) {
			SmarthomaticBindingConfig config = parseIncomingBindingConfig(item,
					bindingConfig);
			addBindingConfig(item, config);
		} else if (bindingConfig.startsWith(">")) {
			SmarthomaticBindingConfig config = parseOutgoingBindingConfig(item,
					bindingConfig);
			addBindingConfig(item, config);
		} else if (bindingConfig.startsWith("=")) {
			SmarthomaticBindingConfig config = parseBidirectionalBindingConfig(
					item, bindingConfig);
			addBindingConfig(item, config);
		} else {
			throw new BindingConfigParseException("Item '" + item.getName()
					+ "' does not start with <, > or =.");
		}

	}

	private SmarthomaticBindingConfig parseBidirectionalBindingConfig(
			Item item, String bindingConfig) throws BindingConfigParseException {
		SmarthomaticBindingConfig config = parseConfig(item, bindingConfig);

		return config;
	}

	private SmarthomaticBindingConfig parseOutgoingBindingConfig(Item item,
			String bindingConfig) throws BindingConfigParseException {
		SmarthomaticBindingConfig config = parseConfig(item, bindingConfig);

		return config;
	}

	private SmarthomaticBindingConfig parseIncomingBindingConfig(Item item,
			String bindingConfig) throws BindingConfigParseException {
		SmarthomaticBindingConfig config = parseConfig(item, bindingConfig);

		return config;
	}

	private SmarthomaticBindingConfig parseConfig(Item item,
			String bindingConfig) throws BindingConfigParseException {
		SmarthomaticBindingConfig config = new SmarthomaticBindingConfig();
		Matcher matcher = CONFIG_PATTERN.matcher(bindingConfig);

		if (!matcher.matches())
			throw new BindingConfigParseException("Config for item '"
					+ item.getName() + "' could not be parsed.");

		bindingConfig = matcher.group(1);
		config.setItemName(item.getName());
		config.setItem(item);

		matcher = TRANSFORMATION_PATTERN.matcher(bindingConfig);
		if (matcher.matches()) {
			bindingConfig = matcher.group(1);
			String transformation = matcher.group(2);
			config.getConfigParams().put("transformation", transformation);
		}

		// parse bindingconfig here ...
		StringTokenizer confItems = new StringTokenizer(bindingConfig, ",");
		while (confItems.hasMoreTokens()) {
			String[] token = confItems.nextToken().split("=");
			String key = token[0];
			String value = token[1];
			config.getConfigParams().put(key, value);
			// Strip all whitespaces from token[0]
			key = key.replaceAll("\\s", "");
			switch (key) {
			case "deviceId":
				config.setDeviceId(value.replaceAll("\\s", ""));
				break;
			case "messageGroupId":
				config.setMessageGroupId(value.replaceAll("\\s", ""));
				break;
			case "messageId":
				config.setMessageId(value.replaceAll("\\s", ""));
				break;
			case "messagePart":
				config.setMessagePartId(value.replaceAll("\\s", ""));
				break;
			}

		}
		return config;
	}

	@Override
	public int getDeviceId(String itemName) {
		SmarthomaticBindingConfig config = (SmarthomaticBindingConfig) bindingConfigs
				.get(itemName);

		return config.getDeviceId();
	}

	@Override
	public int getMessageId(String itemName) {
		SmarthomaticBindingConfig config = (SmarthomaticBindingConfig) bindingConfigs
				.get(itemName);

		return config.getMessageId();
	}

	@Override
	public int getMessageGroupId(String itemName) {
		SmarthomaticBindingConfig config = (SmarthomaticBindingConfig) bindingConfigs
				.get(itemName);

		return config.getMessageGroupId();
	}

	@Override
	public int getMessagePartId(String itemName) {
		SmarthomaticBindingConfig config = (SmarthomaticBindingConfig) bindingConfigs
				.get(itemName);

		return config.getMessagePartId();
	}

	@Override
	public Item getItem(String itemName) {
		SmarthomaticBindingConfig config = (SmarthomaticBindingConfig) bindingConfigs
				.get(itemName);

		return config.getItem();
	}

	@Override
	public String getConfigParam(String itemName, String paramName) {
		SmarthomaticBindingConfig config = (SmarthomaticBindingConfig) bindingConfigs
				.get(itemName);

		return config.getConfigParams().get(paramName);
	}

	class SmarthomaticBindingConfig implements BindingConfig {
		// put member fields here which holds the parsed values
		private Map<String, String> configParams = new HashMap<String, String>();
		private String itemName;
		private int deviceId = -1;
		private int messagePart = 0;
		private int messageGroupId = 0;
		private int messageId = 0;
		private Item item;

		public Map<String, String> getConfigParams() {
			return configParams;
		}

		public void setConfigParams(Map<String, String> configParams) {
			this.configParams = configParams;
		}

		public int getDeviceId() {
			return deviceId;
		}

		public void setDeviceId(String device) {
			// now there are two possibilities:
			// 1. we have a number in deviceId => store it directly
			try {
				this.deviceId = Integer.parseInt(device);
			} catch (NumberFormatException e) {
				this.deviceId = SmarthomaticGenericBindingProvider
						.getDevice(device);
			}
		}

		public int getMessageId() {
			return messageId;
		}

		public void setMessageId(String toggleTime) {
			try {
				this.messageId = Integer.parseInt(toggleTime);
			} catch (NumberFormatException e) {
				this.messageId = 0;
			}
		}

		public String getItemName() {
			return itemName;
		}

		public void setItemName(String itemName) {
			this.itemName = itemName;
		}

		public int getMessagePartId() {
			return messagePart;
		}

		public void setMessagePartId(String port) {
			try {
				this.messagePart = Integer.parseInt(port);
			} catch (NumberFormatException e) {
				this.messagePart = 0;
			}
		}

		/**
		 * 
		 * @return the MessageGroupID of the item
		 */
		public int getMessageGroupId() {
			return this.messageGroupId;
		}

		/**
		 * 
		 * @param type
		 *            is a MessageGroupID - number or a corresponding type like
		 *            Generic, GPIO, Weather, Environment, Powerswitch, Dimmer
		 *            or Digiboard For more information see the smarthomatic
		 *            homepage
		 */
		public void setMessageGroupId(String type) {
			try {
				this.messageGroupId = Integer.parseInt(type);
			} catch (NumberFormatException e) {
				switch (type.toUpperCase()) {
				case "GENERIC":
					this.messageGroupId = 0;
					break;
				case "GPIO":
					this.messageGroupId = 1;
					break;
				case "WEATHER":
					this.messageGroupId = 10;
					break;
				case "ENVIRONMENT":
					this.messageGroupId = 11;
					break;
				case "POWERSWITCH":
					this.messageGroupId = 20;
					break;
				case "DIMMER":
					this.messageGroupId = 60;
					break;
				case "DIGIBOARD":
					this.messageGroupId = 99;
					break;
				}

			}
		}

		public Item getItem() {
			return item;
		}

		public void setItem(Item item) {
			this.item = item;
		}

	}

}
