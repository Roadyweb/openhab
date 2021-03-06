package org.openhab.binding.smarthomatic.internal;

import java.util.StringTokenizer;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseStation implements SerialEventWorker {

	private static final Logger logger = LoggerFactory
			.getLogger(BaseStation.class);

	private SerialDevice serialDevice;

	private String[] versionInfo;

	private SerialEventWorker bindingEventWorker;

	/* always 0 for base station */
	// private final int deviceType = 0;
	/* can be 0..4095, normally 0 */
	// private int deviceId = 0;

	/*
	 * supported items are: StringItem SwitchItem
	 */

	public BaseStation(String port, int baud, SerialEventWorker sew) {
		bindingEventWorker = sew;
		serialDevice = new SerialDevice(port, baud);
		serialDevice.setEventWorker(this);
		try {
			serialDevice.initialize();
		} catch (InitializationException e) {
			e.printStackTrace();
		}
	}

	public void closeSerialPort() {
		serialDevice.close();
	}

	private String genHexString(int deviceID, int length) {
		String s = Integer.toHexString(deviceID);
		int len = s.length();
		for (int i = len; i < length; i++) {
			s = "0" + s;
		}
		return s;
	}

	private String getToggleTime(int toggleTime, boolean on) {
		// shift left the on bit, so that the on-bit stands on position 17 from
		// right
		long temp = (on ? 1 : 0) << 16;
		// patch toggletime into the last 16 bit and be sure to prevent to
		// override on-bit
		temp |= (toggleTime & 0x0001FFFF);
		// shift temp to left by 15 bits (the on bit should stand at msb
		// position)
		temp <<= 15;

		// write hex representation of temp to s
		String s = Long.toHexString(temp);

		// fill String with leading zeros until it's minimum 2 chars long
		for (int i = s.length(); i < 2; i++)
			s = "0" + s;

		return s;
	}

	public void sendCommand(int deviceID, int messageGroupId, int messageId,
			int toggleTime, Command command) {
		String cmd = "";
		String messageData = "";
		// RGB Dimmer Color Message
		if (messageGroupId == 60 && messageId == 10
				&& command instanceof HSBType) {
			ShcColor translateColor = translateColor((HSBType) command);
			messageData = translateColor.toString();
		} else
		// Dimmer Brightness Message
		if (messageGroupId == 60 && messageId == 1
				&& command instanceof DecimalType) {
			int brightness = ((DecimalType) command).intValue();
			brightness = brightness << 1;
			messageData = Integer.toHexString(brightness);
		} else
		// Powerswitch
		if (messageId == 20) {
			cmd = "s0002"
					+ genHexString(deviceID, 4)
					+ "14"
					+ "01"
					+ getToggleTime(toggleTime, command.toString().equals("ON"))
					+ "\r";
		}
		if (!"".equals(messageData)) {
			cmd = "s0002" + genHexString(deviceID, 4)
					+ genHexString(messageGroupId, 2)
					+ genHexString(messageId, 2) + messageData + "\r";
		}

		if (cmd != "") {
			logger.debug("send to serial port:" + cmd);
			serialDevice.writeString(cmd);
		}
	}

	private ShcColor translateColor(HSBType color) {
		int red = (int) (color.getRed().intValue() * 16 / 100);
		int green = color.getGreen().intValue() * 16 / 100;
		int blue = color.getBlue().intValue() * 16 / 100;
		ShcColor result = new ShcColor((byte) red, (byte) green, (byte) blue);
		return result;
	}

	@Override
	public void eventOccured(String message) {

		// normally there comes only one line to the baseStation
		// except when station is restarted
		
		// Filter out the lines that contain garbage data
		if (message.contains("(CRC wrong after decryption)")) {
			logger.debug("BaseStation eventOccured: CRC wrong after decryption");
			return;
		}
		
		if (message.contains("Base Station")) {
			StringTokenizer strTok = new StringTokenizer(message, "\n");
			String data = null;
			versionInfo = new String[strTok.countTokens()];
			logger.debug("BaseStation eventOccured - initial message ( "
					+ strTok.countTokens() + " )");
			int i = 0;
			while (strTok.hasMoreTokens()) {
				versionInfo[i] = strTok.nextToken();
				logger.debug("<BaseStation>[" + i + "]: " + versionInfo[i]);
				i++;
			}
		} else {
			String logResult = message.replaceAll("\n", "\\\\n")
	                                  .replaceAll("\r", "\\\\r")
	                                  .substring(0, 40);

			logger.debug("BaseStation eventOccured - giving to Binding "
					+ logResult);
			if (bindingEventWorker != null) {
				bindingEventWorker.eventOccured(message);
			}
		}
	}

	public String toString() {
		return "BaseStation listening on port " + serialDevice.getPort();
	}

	private class ShcColor {
		byte red;
		byte green;
		byte blue;

		public ShcColor(byte red, byte green, byte blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
			logger.debug("red: {}, green: {}, blue: {}", red, green, blue);
		}

		@Override
		public String toString() {
			// calculating the translation from rgb values to SHC color table
			int colorNumber = (((red - 1) / 4) * 16) + (((green - 1) / 4) * 4)
					+ ((blue - 1) / 4);
			colorNumber = colorNumber << 2;
			String hexString = Integer.toHexString(colorNumber);
			while (hexString.length() < 2) {
				hexString = "0" + hexString;
			}
			return hexString;
		}
	}

}
