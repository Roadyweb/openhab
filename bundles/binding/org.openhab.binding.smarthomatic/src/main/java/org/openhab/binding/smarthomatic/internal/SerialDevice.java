package org.openhab.binding.smarthomatic.internal;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialDevice implements SerialPortEventListener {

	private static final Logger logger = LoggerFactory.getLogger(SerialDevice.class);

	private String port;
	private int baud;
	private SerialEventWorker eventWorker;
	
	private CommPortIdentifier portId;
	private SerialPort serialPort;

	private InputStream inputStream;

	private OutputStream outputStream;

	public SerialDevice(String port, int baud) {
		this.port = port;
		this.baud = baud;
	}

	public void setEventWorker(SerialEventWorker eventWorker) {
		this.eventWorker = eventWorker;
	}

	public void unsetEventWorker() {
		this.eventWorker = null;
	}

	public String getPort() {
		return port;
	}

	/**
	 * Initialize this device and open the serial port
	 * 
	 * @throws InitializationException if port can not be opened
	 */
	@SuppressWarnings("rawtypes")
	public void initialize() throws InitializationException {
		// parse ports and if the default port is found, initialized the reader
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
			if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (id.getName().equals(port)) {
					logger.debug("Serial port '{}' has been found.", port);
					portId = id;
				}
			}
		}
		if (portId != null) {
			// initialize serial port
			try {
				serialPort = (SerialPort) portId.open("openHAB-Smarthomatic", 2000);
			} catch (PortInUseException e) {
				throw new InitializationException(e);
			}

			try {
				inputStream = serialPort.getInputStream();
			} catch (IOException e) {
				throw new InitializationException(e);
			}

			try {
				serialPort.addEventListener(this);
			} catch (TooManyListenersException e) {
				throw new InitializationException(e);
			}

			// activate the DATA_AVAILABLE notifier
			serialPort.notifyOnDataAvailable(true);

			try {
				// set port parameters
				serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);
			} catch (UnsupportedCommOperationException e) {
				throw new InitializationException(e);
			}

			try {
				// get the output stream
				outputStream = serialPort.getOutputStream();
			} catch (IOException e) {
				throw new InitializationException(e);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			portList = CommPortIdentifier.getPortIdentifiers();
			while (portList.hasMoreElements()) {
				CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
				if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					sb.append(id.getName() + "\n");
				}
			}
			throw new InitializationException("Serial port '" + port + "' could not be found. Available ports are:\n" + sb.toString());
		}
	}

	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			// we get here if data has been received
			StringBuilder sb = new StringBuilder();
			byte[] readBuffer = new byte[100];
			try {
				do {
					// read data from serial device
					while (inputStream.available() > 0) {
						int bytes = inputStream.read(readBuffer);
						sb.append(new String(readBuffer, 0, bytes));
					}
					try {
						// add wait states around reading the stream, so that interrupted transmissions are merged
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// ignore interruption
					}
				} while (inputStream.available() > 0);
				// sent data
				String result = sb.toString();

				// send data to the bus
				logger.debug("Received message '{}' on serial port {}", new String[] { result, port });
				
				if (eventWorker != null) {
					eventWorker.eventOccured(result);
				}
			} catch (IOException e) {
				logger.debug("Error receiving data on serial port {}: {}", new String[] { port, e.getMessage() });
			}
			break;
		}
	}

	/**
	 * Sends a string to the serial port of this device
	 * 
	 * @param msg the string to send
	 */
	public void writeString(String msg) {
		logger.debug("Writing '{}' to serial port {}", new String[] { msg, port });
		try {
			// write string to serial port
			outputStream.write(msg.getBytes());
			outputStream.flush();
		} catch (IOException e) {
			logger.error("Error writing '{}' to serial port {}: {}", new String[] { msg, port, e.getMessage() });
		}
	}

	/**
	 * Close this serial device
	 */
	public void close() {
		serialPort.removeEventListener();
		IOUtils.closeQuietly(inputStream);
		IOUtils.closeQuietly(outputStream);
		serialPort.close();
	}

}