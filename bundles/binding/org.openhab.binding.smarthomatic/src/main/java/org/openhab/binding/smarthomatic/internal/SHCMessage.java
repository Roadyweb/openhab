package org.openhab.binding.smarthomatic.internal;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.DatatypeConverter;

import org.openhab.binding.smarthomatic.internal.packetData.Array;
import org.openhab.binding.smarthomatic.internal.packetData.BoolValue;
import org.openhab.binding.smarthomatic.internal.packetData.EnumValue;
import org.openhab.binding.smarthomatic.internal.packetData.IntValue;
import org.openhab.binding.smarthomatic.internal.packetData.Packet;
import org.openhab.binding.smarthomatic.internal.packetData.Packet.MessageGroup;
import org.openhab.binding.smarthomatic.internal.packetData.Packet.MessageGroup.Message;
import org.openhab.binding.smarthomatic.internal.packetData.UIntValue;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SHCMessage {

	private static final Logger logger = LoggerFactory
			.getLogger(SHCMessage.class);

	public static final String DATA_FLAG = "Packet Data:";
	public static final int DATA_FLAG_SIZE = DATA_FLAG.length();

	// MessageGroupIDs
	public static final int GRP_Generic = 0;
	public static final int GRP_GPIO = 1;
	public static final int GRP_Weather = 10;
	public static final int GRP_Environment = 11;
	public static final int GRP_Powerswitch = 20;
	public static final int GRP_Dimmer = 60;

	@SuppressWarnings("unused")
	private String originalMessage;
	private String editedMessage;
	private SHCHeader header;
	private Packet packet;
	public static Charset charset = Charset.forName("UTF-8");
	public static CharsetEncoder encoder = charset.newEncoder();

	public class SHCHeader {
		private int SenderID; // =
								// tokens[0].substring(tokens[0].indexOf("=")+1);
		private int MessageType; // =
									// tokens[2].substring(tokens[2].indexOf("=")+1);
		private long MessageGroupID;// =
									// tokens[3].substring(tokens[3].indexOf("=")+1);
		private int MessageID; // =
								// tokens[4].substring(tokens[4].indexOf("=")+1);
		private byte[] MessageData; // =

		// tokens[5].substring(tokens[5].indexOf("=")+1);

		public int getSenderID() {
			return SenderID;
		}

		public int getMessageType() {
			return MessageType;
		}

		public long getMessageGroupID() {
			return MessageGroupID;
		}

		public int getMessageID() {
			return MessageID;
		}

		public byte[] getMessageData() {
			return MessageData;
		}

		public SHCHeader(String data) {
			StringTokenizer dataTok = new StringTokenizer(data, ";");
			String[] tokens = new String[dataTok.countTokens()];
			Map<String, String> tokensMap = new HashMap<String, String>();
			int i = 0;
			while (dataTok.hasMoreTokens()) {
				String nextToken = dataTok.nextToken();
				tokens[i++] = nextToken;
				String[] split = nextToken.split("=");
				if (split.length == 2) {
					tokensMap.put(split[0].trim(), split[1].trim());
				}
			}
			SenderID = Integer.parseInt(tokensMap.get("SenderID"));
			MessageType = Integer.parseInt(tokensMap.get("MessageType"));
			if (MessageType == 0 || MessageType == 1 || MessageType == 2
					|| MessageType == 8 || MessageType == 10) {
				MessageGroupID = Integer.parseInt(tokensMap
						.get("MessageGroupID"));
				MessageID = Integer.parseInt(tokensMap.get("MessageID"));
			}
			if (MessageType == 1 || MessageType == 2 || MessageType == 8
					|| MessageType == 10) {
				MessageData = DatatypeConverter.parseHexBinary(tokensMap
						.get("MessageData"));
			}

			/*
			 * logger.debug("BaseStation SenderID:      "+SenderID);
			 * logger.debug("BaseStation MessageType:   "+MessageType);
			 * logger.debug("BaseStation MessageGroupID:"+MessageGroupID);
			 * logger.debug("BaseStation MessageID:     "+MessageID);
			 * logger.debug("BaseStation MessageData:   "+MessageData);
			 */
		}
	}

	public class SHCData {
		private List<Type> openHABTypes = new ArrayList<Type>();

		public SHCData(SHCHeader header) {
			byte[] data = header.getMessageData();
			MessageGroup group = null;
			Message message = null;
			int messageType = header.getMessageType();
			if (messageType == 8 || messageType == 10) {
				for (MessageGroup messageGroup : packet.getMessageGroup()) {
					if (messageGroup.getMessageGroupID() == header
							.getMessageGroupID()) {
						group = messageGroup;
						break;
					}
				}
				for (Message message1 : group.getMessage()) {
					if (message1.getMessageID() == header.getMessageID()) {
						message = message1;
						break;
					}
				}

				int startBit = 0;
				startBit = getDataValues(startBit, message.getDataValue(), data);
			}
		}

		private int getDataValues(int startBit, List<Object> dataValues,
				byte[] data) {
			for (Object object : dataValues) {
				if (object instanceof UIntValue) {
					UIntValue value = (UIntValue) object;
					Integer result = parseData(data, value.getBits(), startBit,
							false);
					openHABTypes.add(new DecimalType(result));
					startBit += value.getBits();
				} else if (object instanceof IntValue) {
					IntValue value = (IntValue) object;
					Integer result = parseData(data, value.getBits(), startBit,
							true);
					openHABTypes.add(new DecimalType(result));
					startBit += value.getBits();
				} else if (object instanceof BoolValue) {
					Integer value = parseData(data, 1, startBit, false);
					if (value > 0) {
						openHABTypes.add(OnOffType.ON);
					} else {
						openHABTypes.add(OnOffType.OFF);
					}
					startBit += 1;
				} else if (object instanceof EnumValue) {
					EnumValue value = (EnumValue) object;
					Integer result = parseData(data, value.getBits(), startBit,
							false);
					openHABTypes.add(new DecimalType(result));
					startBit += value.getBits();
				} else if (object instanceof Array) {
					Array value = (Array) object;
					Object object2 = value.getArrayDataValue();
					for (int i = 0; i < value.getLength(); i++) {
						startBit = getDataValues(startBit,
								value.getArrayDataValue(), data);
					}
				} else {
					logger.warn("TODO: Unsupported datatype");
				}
			}
			return startBit;
		}

		private Integer parseData(byte[] data, long bits, int startBit,
				boolean signed) {
			int bits2skip = startBit;
			long bits2add = bits;
			int result = 0;
			// Iterate over bytes and bits
			for (byte b : data) {
				for (int mask = 0x80; mask != 0x00; mask >>= 1) {
					boolean bitvalue = (b & mask) != 0;
					// no more bits2add, do nothing
					if (bits2add < 0) {
					}
					// skip until start bit is reached
					else if (bits2skip > 0) {
						bits2skip--;
					} else {
						if (bitvalue) {
							result = result + (int) Math.pow(2, bits2add - 1);
						}
						bits2add--;
					}
				}
			}
			if (signed) {
				if (result >= (int) Math.pow(2, bits - 1)) {
					result = result - (int) Math.pow(2, bits);
				}
			}
			return result;
		}

		public List<Type> getOpenHABTypes() {
			return openHABTypes;
		}

		@Override
		public String toString() {
			String res = "SHCData [";
			for (Type type : openHABTypes) {
				res += type.toString();
			}
			res += "]";
			return res;
		}

	}

	public SHCMessage(String message, Packet packet) {
		originalMessage = message;
		editedMessage = message.substring(message.indexOf(DATA_FLAG)
				+ DATA_FLAG_SIZE);
		header = new SHCHeader(editedMessage);
		this.packet = packet;
	}

	public SHCHeader getHeader() {
		return header;
	}

	public SHCData getData() {
		return new SHCData(header);
	}

	public List<Type> openHABStateFromSHCMessage(Item object) {
		return getData().getOpenHABTypes();
	}
}
