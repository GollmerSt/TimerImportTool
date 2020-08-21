// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer.channels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeMap;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.xml.StackXML;

public class Channels {

	private static final int SUPPORTED_HEADER_LENGTH = 7;
	private static final String SUPPORTED_FILE_ID = "B2C2";
	private static final int SUPPORTED_MAX_VERSION_HIGH = 1;
	private static final int SUPPORTED_MAX_VERSION_LOW = 9;
	private static final int SUPPORTED_MIN_VERSION_HIGH = 1;
	private static final int SUPPORTED_MIN_VERSION_LOW = 8;
	private static final int SUPPORTED_MAX_VERSION = SUPPORTED_MAX_VERSION_HIGH * 256 + SUPPORTED_MAX_VERSION_LOW;
	private static final int SUPPORTED_MIN_VERSION = SUPPORTED_MIN_VERSION_HIGH * 256 + SUPPORTED_MIN_VERSION_LOW;
	public static final String CHANNEL_FILE_NAME = "channels.dat";
	private static final int SUPPORTED_CHANNEL_ENTRY_LENGTH = Channel.ENTRY_LENGTH;
	private static final FileChannel.MapMode READ_ONLY = FileChannel.MapMode.READ_ONLY;

	private static final StackXML<String> entryPath = new StackXML<String>("Channels", "Entry");

	private final DVBViewer dvbViewer;
	private File file;
	private FileChannel fileChannel;
	private MappedByteBuffer buffer = null;
	private long headerLength = 0;
	private long channelEntryLength = 0;

	private static CharsetDecoder decoder;

	static {
		Charset charset = Charset.forName("Windows-1252");
		Channels.decoder = charset.newDecoder();
	}

	public class MyComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			if (o1.equalsIgnoreCase(o2))
				return o1.compareTo(o2);
			return o1.compareToIgnoreCase(o2);
		}
	}

	private TreeMap<String, Channel> channelMap = null; // new TreeMap< String,
														// Channel >( new
														// MyComparator() ) ;

	public Channels(DVBViewer dvbViewer) {
		this.dvbViewer = dvbViewer;
	}

	private void throwErrorWrongVersion() {
		throw new ErrorClass("Error on reading \"" + CHANNEL_FILE_NAME + "\". Version changed?");
	}

	String readString(int fieldLength) {
		byte[] buffer = new byte[fieldLength - 1];

		int stringLength = 0;

		try {
			stringLength = this.buffer.get();

			if (stringLength >= fieldLength)
				this.throwErrorWrongVersion();

			this.buffer.get(buffer, 0, fieldLength - 1);

		} catch (BufferUnderflowException e) {
			throwErrorWrongVersion();
		}

		CharBuffer cBuf = null;
		try {
			cBuf = Channels.decoder.decode(ByteBuffer.wrap(buffer, 0, stringLength));
		} catch (CharacterCodingException e) {
			Log.out("Illegal format: " + new String(buffer, 0, stringLength));
			return null;
		}
		return new String(cBuf.toString());
	}

	private byte readByte() {
		byte result = 0;

		try {
			result = this.buffer.get();
		} catch (BufferUnderflowException e) {
			throwErrorWrongVersion();
		}
		return result;
	}

	public void openFileAndCheckHeader() {
		this.file = new File(this.dvbViewer.getDVBViewerDataPath() + File.separator + CHANNEL_FILE_NAME);

		try {
			this.fileChannel = new FileInputStream(this.file).getChannel();
		} catch (FileNotFoundException e) {
			try {
				this.fileChannel.close();
			} catch (IOException e1) {
			}
			throw new ErrorClass("Error on opening \"" + this.file.getAbsolutePath() + "\". File exists?");
		}

		try {
			this.buffer = this.fileChannel.map(READ_ONLY, 0, SUPPORTED_HEADER_LENGTH);
		} catch (IOException e) {
			try {
				this.fileChannel.close();
			} catch (IOException e1) {
			}
			this.throwErrorWrongVersion();
		}
		if (!this.readString(5).equals(Channels.SUPPORTED_FILE_ID))
			this.throwErrorWrongVersion();
		byte versionHigh = this.readByte();
		byte versionLow = this.readByte();
		int version = (int) (versionHigh & 0xFF) * 256 + (int) (versionLow & 0xff);
		if (version < SUPPORTED_MIN_VERSION && version > SUPPORTED_MAX_VERSION) {
			this.throwErrorWrongVersion();
		}

		this.headerLength = SUPPORTED_HEADER_LENGTH;
		this.channelEntryLength = SUPPORTED_CHANNEL_ENTRY_LENGTH;
	}

	public void read(boolean onlyTV) {
		this.openFileAndCheckHeader();

		this.channelMap = new TreeMap<String, Channel>(new MyComparator());

		try {
			long size = this.fileChannel.size();
			boolean wasMessageFired = false;

			int numEntries = (int) ((size - this.headerLength) / this.channelEntryLength);
			for (int n = 0; n < numEntries; n++) {
				this.buffer = this.fileChannel.map(READ_ONLY, this.channelEntryLength * n + this.headerLength,
						this.channelEntryLength);

				this.buffer.order(ByteOrder.LITTLE_ENDIAN);

				Channel channel = new Channel(this);
				channel.read();

				if (!channel.isFailed() && (!onlyTV || channel.isVideo()))
					this.channelMap.put(channel.getChannelName(), channel);
				if (channel.isFailed() && !wasMessageFired) {
					Log.error(ResourceManager.msg("ILLEGAL_FORMAT_DVBVIEWER_CHANNEL"));
					wasMessageFired = true;
				}
			}
		} catch (IOException e1) {
			throw new ErrorClass("Unexpected error on reading \"" + this.file.getAbsolutePath());
		}

		boolean debug = false;
		if (debug) {
			Collection<Channel> values = this.channelMap.values();

			for (Channel c : values) {
				System.out.println(" ChannelID: " + c.getChannelID());
			}
		}
		try {
			this.fileChannel.close();
		} catch (IOException e) {
			throw new ErrorClass("Unexpected error on closing \"" + this.file.getAbsolutePath());
		}
	}

	public MappedByteBuffer getMappedByteBuffer() {
		return this.buffer;
	};

	public TreeMap<String, Channel> getChannels() {
		return this.channelMap;
	};

	public boolean containsChannelID(String channelID) {
		String parts[] = channelID.split("\\|");
		String channelName = parts[parts.length - 1];
		if (!this.channelMap.containsKey(channelName))
			return false;
		Channel channel = this.channelMap.get(channelName);
		if (!channel.getChannelID().equals(channelID))
			return false;
		return true;
	}

	public void readXML(final XMLEventReader reader, XMLEvent ev, String name) {
		this.channelMap = new TreeMap<String, Channel>(new MyComparator());

		Stack<String> stack = new Stack<String>();
		Channel entry = null;
		while (true) {
			if (ev.isStartElement()) {
				stack.push(ev.asStartElement().getName().getLocalPart());
				if (stack.equals(Channels.entryPath)) {
					String channelID = null;

					@SuppressWarnings("unchecked")
					Iterator<Attribute> iter = ev.asStartElement().getAttributes();
					while (iter.hasNext()) {
						Attribute a = iter.next();
						String attributeName = a.getName().getLocalPart();
						String value;
						try {
							value = a.getValue();
						} catch (Exception e1) {
							value = null; // channels.dat could contain invalid
											// entries
						}
						if (attributeName.equals("id"))
							channelID = value;
					}
					if (channelID != null) {
						entry = Channel.createByChannelID(channelID);
						this.channelMap.put(entry.getChannelName(), entry);
					}
				}
			} else if (ev.isEndElement()) {
				stack.pop();
				if (stack.size() == 0)
					break;
			}
			if (!reader.hasNext())
				break;
			try {
				ev = reader.nextEvent();
			} catch (XMLStreamException e) {
				throw new ErrorClass(e, "Unexpected error on reading the file \"" + name + "\"");
			}
		}
	}

	public void writeXML(IndentingXMLStreamWriter sw, File f) {
		if (this.channelMap == null)
			return;
		try {
			for (Channel c : this.channelMap.values()) {
				sw.writeStartElement("Entry");
				sw.writeAttribute("id", c.getChannelID());
				sw.writeEndElement();
			}
		} catch (XMLStreamException e) {
			throw new ErrorClass(e, "Unexpected error on writing the file \"" + f.getName() + "\"");
		}
	}
}
