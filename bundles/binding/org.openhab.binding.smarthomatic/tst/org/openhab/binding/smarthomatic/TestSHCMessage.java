package org.openhab.binding.smarthomatic;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.smarthomatic.internal.SHCMessage;
import org.openhab.binding.smarthomatic.internal.packetData.Packet;

public class TestSHCMessage {

	private Packet packet;

	@Before
	public void setUp() throws Exception {
		File file = new File(
				"C:\\jorg\\src\\openhab\\bundles\\binding\\org.openhab.binding.smarthomatic\\xml\\packet_layout.xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(Packet.class);

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		packet = (Packet) jaxbUnmarshaller.unmarshal(file);
	}

	/**
	 * Test Daten sind env sensor humidity: 53.4 temperatur: 22.40
	 */
	@Test
	public void testEnvSensorData() {

		String message = " Packet Data: SenderID=10;PacketCounter=17531;MessageType=8;MessageGroupID=10;MessageID=2;MessageData=858230000000;Humidity=53.4;Temperature=22.40;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		int intValue1 = shcMessage.getData().getIntValue(0);
		int intValue2 = shcMessage.getData().getIntValue(1);
		Assert.assertEquals(534, intValue1);
		Assert.assertEquals(2240, intValue2);
	}

	/**
	 * Test Daten sind env sensor humidity: 102.3 (0x3FF) temperatur: -327.68
	 * (0x8000)
	 */
	@Test
	public void testEnvSensorDataMinMax1() {

		String message = " Packet Data: SenderID=10;PacketCounter=17531;MessageType=8;MessageGroupID=10;MessageID=2;MessageData=FFE000000000;Humidity=53.4;Temperature=22.40;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		int intValue1 = shcMessage.getData().getIntValue(0);
		int intValue2 = shcMessage.getData().getIntValue(1);
		Assert.assertEquals(1023, intValue1);
		Assert.assertEquals(-32768, intValue2);
	}

	/**
	 * Test Daten sind env sensor humidity: 0 (0x000) temperatur: 32767 (0x7FFF)
	 */
	@Test
	public void testEnvSensorDataMinMax2() {

		String message = " Packet Data: SenderID=10;PacketCounter=17531;MessageType=8;MessageGroupID=10;MessageID=2;MessageData=001FFFC00000;Humidity=53.4;Temperature=22.40;";
		SHCMessage shcMessage = new SHCMessage(message, packet);
		int intValue1 = shcMessage.getData().getIntValue(0);
		int intValue2 = shcMessage.getData().getIntValue(1);
		Assert.assertEquals(0, intValue1);
		Assert.assertEquals(32767, intValue2);
	}

}
