package org.openhab.binding.smarthomatic;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.smarthomatic.internal.SHCMessage;
import org.openhab.binding.smarthomatic.internal.packetData.Packet;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Type;

public class TestSHCMessage {

	private Packet packet;

	@Before
	public void setUp() throws Exception {
		File file = new File("xml/packet_layout.xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(Packet.class);

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		packet = (Packet) jaxbUnmarshaller.unmarshal(file);
	}

	/**
	 * Test Daten sind generic version: 0.0.0-0
	 */
	@Test
	public void testGenericVersionMin() {
		String message = " Packet Data: SenderID=10;PacketCounter=165;MessageType=8;MessageGroupID=0;MessageID=1;MessageData=000000000000;Major=0;Minor=0;Patch=0;Hash=000004d6;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals("Major", 0,
				((DecimalType) values.get(0)).intValue());
		Assert.assertEquals("Minor", 0,
				((DecimalType) values.get(1)).intValue());
		Assert.assertEquals("Patch", 0,
				((DecimalType) values.get(2)).intValue());
		Assert.assertEquals("Hash", 0, ((DecimalType) values.get(3)).intValue());
	}

	/**
	 * Test Daten sind generic version: 255.255.255-255 TODO: Hash parsing isn't
	 * working with the max hast value of 4294967295
	 */
	@Test
	public void testGenericVersionMax() {
		String message = " Packet Data: SenderID=10;PacketCounter=165;MessageType=8;MessageGroupID=0;MessageID=1;MessageData=FFFFFF000000FF;Major=0;Minor=0;Patch=0;Hash=000004d6;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals("Major", 255,
				((DecimalType) values.get(0)).intValue());
		Assert.assertEquals("Minor", 255,
				((DecimalType) values.get(1)).intValue());
		Assert.assertEquals("Patch", 255,
				((DecimalType) values.get(2)).intValue());
		Assert.assertEquals("Hash", 255,
				((DecimalType) values.get(3)).intValue());
	}

	/**
	 * Test Daten sind generic version: 0.255.0-255 TODO: Hash parsing isn't
	 * working with the max hast value of 4294967295
	 */
	@Test
	public void testGenericVersionMinMax1() {
		String message = " Packet Data: SenderID=10;PacketCounter=165;MessageType=8;MessageGroupID=0;MessageID=1;MessageData=00FF00000000FF;Major=0;Minor=0;Patch=0;Hash=000004d6;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals("Major", 0,
				((DecimalType) values.get(0)).intValue());
		Assert.assertEquals("Minor", 255,
				((DecimalType) values.get(1)).intValue());
		Assert.assertEquals("Patch", 0,
				((DecimalType) values.get(2)).intValue());
		Assert.assertEquals("Hash", 255,
				((DecimalType) values.get(3)).intValue());
	}

	/**
	 * Test Daten sind generic version: 255.0.255-0
	 */
	@Test
	public void testGenericVersionMinMax2() {
		String message = " Packet Data: SenderID=10;PacketCounter=165;MessageType=8;MessageGroupID=0;MessageID=1;MessageData=FF00FF00000000;Major=0;Minor=0;Patch=0;Hash=000004d6;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals("Major", 255,
				((DecimalType) values.get(0)).intValue());
		Assert.assertEquals("Minor", 0,
				((DecimalType) values.get(1)).intValue());
		Assert.assertEquals("Patch", 255,
				((DecimalType) values.get(2)).intValue());
		Assert.assertEquals("Hash", 0, ((DecimalType) values.get(3)).intValue());
	}

	/**
	 * Test Daten sind generic battery: 66 %
	 */
	@Test
	public void testGenericBattTyp() {
		String message = " Packet Data: SenderID=10;PacketCounter=164;MessageType=8;MessageGroupID=0;MessageID=5;MessageData=840000000004;Percentage=66;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(66, ((DecimalType) values.get(0)).intValue());
	}

	/**
	 * Test Daten sind generic battery: 0 %
	 */
	@Test
	public void testGenericBattMin() {
		String message = " Packet Data: SenderID=10;PacketCounter=164;MessageType=8;MessageGroupID=0;MessageID=5;MessageData=000000000004;Percentage=66;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(0, ((DecimalType) values.get(0)).intValue());
	}

	/**
	 * Test Daten sind generic battery: 127
	 */
	@Test
	public void testGPIODigitalPin() {
		String message = " Packet Data: SenderID=10;PacketCounter=164;MessageType=8;MessageGroupID=0;MessageID=5;MessageData=FE0000000004;Percentage=66;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(127, ((DecimalType) values.get(0)).intValue());
	}

	/**
	 * Test Daten sind GPIO DigitalPort: 00000000b
	 */
	@Test
	public void testGPIOAllZero() {
		String message = " Packet Data: SenderID=150;PacketCounter=1688;MessageType=8;MessageGroupID=1;MessageID=1;MessageData=000000000004;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(8, values.size());
		for (Type type : values) {
			Assert.assertEquals(OnOffType.OFF, type);
		}
	}

	/**
	 * Test Daten sind GPIO DigitalPort: 11111111b
	 */
	@Test
	public void testGPIOAllOn() {
		String message = " Packet Data: SenderID=150;PacketCounter=1688;MessageType=8;MessageGroupID=1;MessageID=1;MessageData=ff0000000004;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(8, values.size());
		for (Type type : values) {
			Assert.assertEquals(OnOffType.ON, type);
		}
	}

	/**
	 * Test Daten sind GPIO DigitalPort: 10100101b
	 */
	@Test
	public void testGPIOAllMixed() {
		String message = " Packet Data: SenderID=150;PacketCounter=1688;MessageType=8;MessageGroupID=1;MessageID=1;MessageData=a50000000004;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(8, values.size());
		Assert.assertEquals(OnOffType.ON, values.get(0));
		Assert.assertEquals(OnOffType.OFF, values.get(1));
		Assert.assertEquals(OnOffType.ON, values.get(2));
		Assert.assertEquals(OnOffType.OFF, values.get(3));
		Assert.assertEquals(OnOffType.OFF, values.get(4));
		Assert.assertEquals(OnOffType.ON, values.get(5));
		Assert.assertEquals(OnOffType.OFF, values.get(6));
		Assert.assertEquals(OnOffType.ON, values.get(7));
	}

	/**
	 * Test Daten sind weather temperature: 22.10
	 */
	@Test
	public void testWeatherTempTyp() {
		String message = " Packet Data: SenderID=11;PacketCounter=68026;MessageType=8;MessageGroupID=10;MessageID=1;MessageData=08a200000000;Temperature=22.10;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(2210, ((DecimalType) values.get(0)).intValue());
	}

	/**
	 * Test Daten sind weather temperature: -32768 = 0x8000 (min)
	 */
	@Test
	public void testWeatherTempMin() {
		String message = " Packet Data: SenderID=11;PacketCounter=68026;MessageType=8;MessageGroupID=10;MessageID=1;MessageData=800000000000;Temperature=22.10;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(-32768, ((DecimalType) values.get(0)).intValue());
	}

	/**
	 * Test Daten sind weather temperature: 32767 = 0x7FFF (max)
	 */
	@Test
	public void testWeatherTempMax() {
		String message = " Packet Data: SenderID=11;PacketCounter=68026;MessageType=8;MessageGroupID=10;MessageID=1;MessageData=7FFF00000000;Temperature=22.10;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(32767, ((DecimalType) values.get(0)).intValue());
	}

	/**
	 * Test Daten sind weather temperature & humidity: hum: 53.4 temperatur:
	 * 22.40
	 */
	@Test
	public void testWeatherTempHumTyp() {
		String message = " Packet Data: SenderID=10;PacketCounter=17531;MessageType=8;MessageGroupID=10;MessageID=2;MessageData=858230000000;Humidity=53.4;Temperature=22.40;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(534, ((DecimalType) values.get(0)).intValue());
		Assert.assertEquals(2240, ((DecimalType) values.get(1)).intValue());
	}

	/**
	 * Test Daten sind weather temperature & humidity: 102.3 (0x3FF) temperatur:
	 * -327.68 (0x8000)
	 */
	@Test
	public void testWeatherTempHumMinMax1() {
		String message = " Packet Data: SenderID=10;PacketCounter=17531;MessageType=8;MessageGroupID=10;MessageID=2;MessageData=FFE000000000;Humidity=53.4;Temperature=22.40;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(1023, ((DecimalType) values.get(0)).intValue());
		Assert.assertEquals(-32768, ((DecimalType) values.get(1)).intValue());
	}

	/**
	 * Test Daten sind weather temperature & humidity: 0 (0x000) temperatur:
	 * 32767 (0x7FFF)
	 */
	@Test
	public void testWeatherTempHumMinMax2() {
		String message = " Packet Data: SenderID=10;PacketCounter=17531;MessageType=8;MessageGroupID=10;MessageID=2;MessageData=001FFFC00000;Humidity=53.4;Temperature=22.40;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(0, ((DecimalType) values.get(0)).intValue());
		Assert.assertEquals(32767, ((DecimalType) values.get(1)).intValue());
	}

	/**
	 * Test Daten sind weather barometric pressure: 96461 temp: -1
	 */
	@Test
	public void testWeatherBaroTempTyp() {
		String message = " Packet Data: SenderID=11;PacketCounter=68029;MessageType=8;MessageGroupID=10;MessageID=3;MessageData=bc75ffff8000;Pressure=96491;Temperature=-0.01;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(96491, ((DecimalType) values.get(0)).intValue());
		Assert.assertEquals(-1, ((DecimalType) values.get(1)).intValue());
	}

	/**
	 * Test Daten sind weather barometric pressure: 0 temp: -32768
	 */
	@Test
	public void testWeatherBaroTempMin() {
		String message = " Packet Data: SenderID=11;PacketCounter=68029;MessageType=8;MessageGroupID=10;MessageID=3;MessageData=000040000000;Pressure=0;Temperature=-0.01;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(0, ((DecimalType) values.get(0)).intValue());
		Assert.assertEquals(-32768, ((DecimalType) values.get(1)).intValue());
	}

	/**
	 * Test Daten sind weather barometric pressure: 131071 temp: 32767
	 */
	@Test
	public void testWeatherBaroTempMax() {
		String message = " Packet Data: SenderID=11;PacketCounter=68029;MessageType=8;MessageGroupID=10;MessageID=3;MessageData=FFFFBFFFFFFF;Pressure=0;Temperature=-0.01;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(131071, ((DecimalType) values.get(0)).intValue());
		Assert.assertEquals(32767, ((DecimalType) values.get(1)).intValue());
	}

	/**
	 * Test Daten sind environment brightness: 37 % (typ)
	 */
	@Test
	public void testEnvBrightnessTyp() {
		String message = " Packet Data: SenderID=10;PacketCounter=123;MessageType=8;MessageGroupID=11;MessageID=1;MessageData=4a0000000006;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(37, ((DecimalType) values.get(0)).intValue());
	}

	/**
	 * Test Daten sind environment brightness: 0 % (min)
	 */
	@Test
	public void testEnvBrightnessMin() {
		String message = " Packet Data: SenderID=10;PacketCounter=123;MessageType=8;MessageGroupID=11;MessageID=1;MessageData=000000000006;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(0, ((DecimalType) values.get(0)).intValue());
	}

	/**
	 * Test Daten sind environment brightness: 100 % (max)
	 */
	@Test
	public void testEnvBrightnessMax() {
		String message = " Packet Data: SenderID=10;PacketCounter=123;MessageType=8;MessageGroupID=11;MessageID=1;MessageData=FE0000000006;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(127, ((DecimalType) values.get(0)).intValue());
	}

	/**
	 * Test Daten sind environment distance: 117 cm (typ)
	 */
	@Test
	public void testEnvDistanceTyp() {
		String message = " Packet Data: SenderID=11;PacketCounter=68047;MessageType=8;MessageGroupID=11;MessageID=2;MessageData=01d400000000;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(117, ((DecimalType) values.get(0)).intValue());
	}

	/**
	 * Test Daten sind enviroment distance: 0 cm (min)
	 */
	@Test
	public void testEnvDistanceMin() {
		String message = " Packet Data: SenderID=11;PacketCounter=68047;MessageType=8;MessageGroupID=11;MessageID=2;MessageData=000000000000;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(0, ((DecimalType) values.get(0)).intValue());
	}

	/**
	 * Test Daten sind enviroment distance: 16383 cm (max)
	 */
	@Test
	public void testEnvDistanceMax() {
		String message = " Packet Data: SenderID=11;PacketCounter=68047;MessageType=8;MessageGroupID=11;MessageID=2;MessageData=FFFC00000000;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		List<Type> values = shcMessage.getData().getOpenHABTypes();
		Assert.assertEquals(16383, ((DecimalType) values.get(0)).intValue());
	}
}
