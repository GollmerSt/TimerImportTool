// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import dvbviewertimerimport.xml.StackXML;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.TimeOffsets;
import dvbviewertimerimport.misc.Enums.ActionAfterItems;
import dvbviewertimerimport.misc.Enums.TimerActionItems;
import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;
import dvbviewertimerimport.main.Versions;
import dvbviewertimerimport.misc.*;
import dvbviewertimerimport.provider.Provider;

public class DVBViewer {

	private static TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");

	public static enum Command {
		SET, DELETE, FIND, UPDATE, UPDATE_TVBROWSER, UPDATE_UNRESOLVED_ENTRIES
	};

	private static final int TIMEOUT_S = 120; // DVBViewer waiting time
	private static final int DVBVIEWER_SCAN_TIME_MS = 100; // Scan time for
															// waiting DVBViewer
	private static final int DVBVIEWER_CHANNEL_TIME_MS = 200; // Selecting
																// channel
																// period time

	private static final String NAME_USERMODE_FILE = "usermode.ini";
	private static final String NAME_CONFIG_PATH = "Plugins";
	private static final String NAME_PATH_REMOVE = "\\Roaming";
	public static final String NAME_XML_PROCESSED_RECORDINGS = "DVBVTimerImportPrcd.xml";
	private static final String NAME_DVBVIEWER_EXE = "dvbviewer.exe";

	public static final String NAME_DVBVIEWER_COM_DLL = "DVBViewerTimerImport";
	private static String PATH_PLUGIN_DATA;

	private static boolean isDLLloaded = false;

	private static final StackXML<String> xmlPath = new StackXML<String>(
			"Processed", "Entry");

	public static String reworkChannelID(final String channelID) {
		String channel = channelID;
		String[] channelParts = channel.split("\\|");
		if (channelParts.length != 2)
			return channelID;
		Long no = Long.parseLong(channelParts[0]);
		no &= 0xffffffffL;
		channel = no.toString() + "|" + channelParts[1];
		return channel;
	}

	public class MaxID {
		long maxID = -1;

		public long increment() {
			maxID++;
			if (maxID < 0L)
				maxID = 0; // wrap around
			return (maxID);
		}

		private void reset() {
			maxID = -1;
		};
	}

	public static TimeZone getTimeZone() {
		return timeZone;
	};

	public static void setTimeZone(TimeZone timeZone) {
		DVBViewer.timeZone = timeZone;
	};

	private DVBViewerService service = null;
	private final DVBViewerTimerXML timersXML;

	private boolean isDVBViewerConnected = false;

	private dvbviewertimerimport.dvbviewer.channels.Channels channels = new dvbviewertimerimport.dvbviewer.channels.Channels(
			this);

	private ArrayList<DVBViewerEntry> recordEntries = null;
	private MaxID maxID = new MaxID();
	private ArrayList<HashMap<String, Channel>> channelsLists = new ArrayList<HashMap<String, Channel>>(
			dvbviewertimerimport.provider.Provider.getProviders().size());
	private Map<Long, ChannelSet> channelSetMap = new HashMap<Long, ChannelSet>();

	private final String exePath;
	private String dvbViewerPath = null;
	private String dvbExePath = null;
	private boolean isDVBViewerPathSetExternal = false;
	private String dvbViewerDataPath = null;
	private String dvbViewerPluginDataPath = null;
	private String viewParameters = "";
	private String recordingParameters = "";
	private boolean startIfRecording = false;
	private String xmlFilePath = null;

	private final String exeName;
	private String separator = ",,";
	private int maxTitleLength = -1;
	private ActionAfterItems afterRecordingAction = ActionAfterItems.NONE;
	private TimerActionItems timerAction = TimerActionItems.RECORD;

	private boolean isThreadListening = false;
	private String selectedChannel = null;

	private int channelChangeTime = 0; // seconds
	private int waitCOMTime = 0; // seconds

	static {
		String path = System.getenv("APPDATA");

		if (path == null)
			path = DVBViewer.determineExePath();
		else {
			path += File.separator + Constants.PROGRAM_NAME;

			File dir = new File(path);

			if (!dir.exists())
				dir.mkdirs();
		}
		Log.setFile(path);
		DVBViewer.PATH_PLUGIN_DATA = path;
	}

	public DVBViewer() {
		this(null);
	}

	public DVBViewer(String xmlPath) {
		this.dvbViewerPath = Registry
				.getValue(
						"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\dvbviewer.exe",
						"");
		if (this.dvbViewerPath != null) {
			this.dvbViewerPath = this.dvbViewerPath.substring(0,
					this.dvbViewerPath.lastIndexOf('\\'));
			this.determineDataPath();
		}
		this.exeName = Constants.PROGRAM_NAME + ".jar";
		this.exePath = determineExePath();
		this.timersXML = new DVBViewerTimerXML(this);
		this.xmlFilePath = xmlPath;

		DVBViewer.checkAndGetDVBViewerCOMDllIfNecessary(false);

	}

	public void setProvider() {
		if (this.channelsLists.size() == 0)
			for (int ix = 0; ix < dvbviewertimerimport.provider.Provider
					.getProviders().size(); ix++)
				channelsLists.add(new HashMap<String, Channel>());
	}

	public static String determineExePath() {
		String exePath = System.getProperty("user.dir");

		File jarFile = new File(exePath + File.separator
				+ Constants.PROGRAM_NAME + ".jar");

		if (!jarFile.exists()) {
			jarFile = null;
			try {
				jarFile = new File(
						dvbviewertimerimport.main.TimerImportTool.class
								.getProtectionDomain().getCodeSource()
								.getLocation().toURI());
				exePath = jarFile.getParent();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// System.out.println( exePath ) ;
		return exePath;
	}

	public String getXMLFilePath() {
		if (this.xmlFilePath != null)
			return this.xmlFilePath;
		else
			this.xmlFilePath = DVBViewer.PATH_PLUGIN_DATA;
		return this.xmlFilePath;
	}

	private void determineDataPath() {
		String iniFile = this.dvbViewerPath + File.separator
				+ NAME_USERMODE_FILE;
		File f = new File(iniFile);
		BufferedReader bR = null;
		try {
			bR = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			this.dvbViewerDataPath = null;
			this.dvbViewerPluginDataPath = null;
			return;
		}
		String line = null;
		boolean modeBlock = false;
		boolean root = false;
		int userMode = -1;

		String path = "";
		try {
			while ((line = bR.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("[")) {
					if (line.equalsIgnoreCase("[Mode]"))
						modeBlock = true;
					else if (modeBlock)
						break;
				} else {
					int p = line.indexOf('=');
					if (p >= 0 && modeBlock) {
						String key = line.substring(0, p).trim();
						String value = line.substring(p + 1).trim();
						if (key.equalsIgnoreCase("UserMode")) {
							if (value.equals("0")) {
								path = dvbViewerPath + path;
								userMode = 0;
							} else if (value.equals("1")) {
								path = System.getenv("APPDATA") + path;
								userMode = 1;
							} else if (value.equals("2")) {
								String temp = System.getenv("APPDATA");
								temp = temp.substring(temp
										.lastIndexOf(File.separator));
								if (temp.equalsIgnoreCase(DVBViewer.NAME_PATH_REMOVE))
									temp = "";
								path = System.getenv("ALLUSERSPROFILE") + temp
										+ path;
								userMode = 2;
							} else
								throw new ErrorClass("The file \"" + iniFile
										+ "\" contains an illegal user mode.");
						} else if (key.equalsIgnoreCase("Root")
								&& userMode != 0) {
							root = true;
							path += File.separator + value;
						}
					}
				}
			}
			bR.close();
		} catch (IOException e) {
			throw new ErrorClass(e, "Error on reading the file \"" + iniFile
					+ "\".");
		}
		if (userMode == -1 || (!root && userMode != 0))
			throw new ErrorClass("Illegal format of the file \"" + iniFile
					+ "\".");
		File directory = new File(path);
		if (!directory.isDirectory())
			throw new ErrorClass("Directory \"" + path
					+ "\" not found. The File \"" + iniFile
					+ "\" should be checked.");
		this.dvbViewerDataPath = path;
		this.dvbViewerPluginDataPath = path + File.separator + NAME_CONFIG_PATH;
	}

	private void connectDVBViewerIfNecessary() {
		if ((this.service == null || !this.service.isEnabled())
				&& !isDVBViewerConnected)
			isDVBViewerConnected = DVBViewerCOM.connect();
	}

	private void disconnectDVBViewer() {
		if (isDVBViewerConnected)
			DVBViewerCOM.disconnect();
		isDVBViewerConnected = false;
	}

	private void readDVBViewerTimers() {
		this.recordEntries = null;
		if (this.service != null && this.service.isEnabled())
			this.recordEntries = this.service.readTimers();
		else {
			if (DVBViewerCOM.connect()) {
				this.recordEntries = DVBViewerCOM.readTimers();
				DVBViewerCOM.disconnect();
			} else
				this.recordEntries = this.timersXML.readTimers();
		}
	}

	public void updateDVBViewer(boolean readXML) {

		ArrayList<DVBViewerEntry> previousXML;

		if (readXML || this.recordEntries == null)
			previousXML = null;
		else {
			previousXML = this.recordEntries;
			for (DVBViewerEntry entry : previousXML)
				entry.clearDVBViewerID();
		}
		this.connectDVBViewerIfNecessary();
		readDVBViewerTimers();
		this.mergeXMLWithDVBViewerData(previousXML);
		try {
			this.setDVBViewerTimers();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.disconnectDVBViewer();
		this.writeXML();
	}

	public boolean process(DVBViewerProvider provider, boolean getAll,
			Object args) throws Exception {
		return process(provider, getAll, args, Command.SET);
	}

	public boolean process(DVBViewerProvider provider, boolean getAll,
			Object args, Command command) throws Exception {
		Object[] argArray = new Object[1];
		argArray[0] = args;

		return process(provider, getAll, argArray, command);
	}

	public boolean process(DVBViewerProvider provider, boolean getAll,
			Object[] args, Command command) throws Exception {
		return process(null, provider, getAll, args, command);
	}

	public boolean process(DVBViewerEntry updateEntry,
			DVBViewerProvider provider, boolean getAll, Object[] args,
			Command command) throws Exception {
		boolean result = true;
		if (command == Command.FIND && this.recordEntries != null) {
			result = provider.process(getAll, command);
			for (Object o : args)
				result &= provider.processEntry(o, command);
			return result;
		}
		this.connectDVBViewerIfNecessary();
		try {
			this.readDVBViewerTimers();
			this.mergeXMLWithDVBViewerData(null);

			result = provider.process(getAll, command);
			if (command == Command.UPDATE_UNRESOLVED_ENTRIES)
				result &= provider.processEntry(null, command);
			else
				for (Object o : args)
					result &= provider.processEntry(o, command);
			if (command != Command.FIND && command != Command.UPDATE_TVBROWSER)
				this.setDVBViewerTimers();
		} catch (Exception e) {
			this.disconnectDVBViewer();
			throw e;
		}
		this.disconnectDVBViewer();
		this.writeXML();
		return result;
	}

	public void addRecordingEntry(DVBViewerEntry entry) {
		if (entry.getID() < 0L) {
			entry.setID(this.maxID.increment());
		}
		this.recordEntries.add(entry);
	}

	private void prepareProvider() {
		Provider provider = Provider.getProcessingProvider();

		if (provider == null)
			return;

		if (provider.isPrepared())
			return;

		provider.setPrepared(true);

		for (DVBViewerEntry e : this.recordEntries) {
			if (e.getProvider() == provider && !e.isMergeElement()) {
				e.setMissing();
			}
		}
	}

	public Channel getDVBViewerChannel(final int providerID,
			final String providerChannel) {
		this.prepareProvider();
		HashMap<String, Channel> channelMap = this.channelsLists
				.get(providerID);
		if (!channelMap.containsKey(providerChannel)) {
			ErrorClass.setWarníng();
			throw new ErrorClass(ResourceManager.msg(
					"MISSING_PROVIDER_CHANNEL_ENTRY", providerChannel));
		}
		Channel c = channelMap.get(providerChannel);
		String dvbViewerChannel = c.getDVBViewer();
		if (dvbViewerChannel == null || dvbViewerChannel.length() == 0) {
			ErrorClass.setWarníng();
			throw new ErrorClass(ResourceManager.msg(
					"MISSING_DVBVIEWER_CHANNEL_ENTRY", providerChannel));
		}
		return c;
	}

	public boolean addNewEntry(Provider provider, String providerID,
			String channel, long start, long end, String title) {
		Channel c = this.getDVBViewerChannel(provider.getID(), channel);
		TimeOffsets o = c.getOffsets();
		long startOrg = start;
		start -= o.getPreOffset(start) * 60000;
		long endOrg = end;
		end += o.getPostOffset(end) * 60000;

		if (end < System.currentTimeMillis())
			return false;

		DVBViewerEntry e = new DVBViewerEntry(c.getDVBViewer(),
				c.getChannelSet(), providerID, start, end, startOrg, endOrg,
				"-------", title, this.timerAction, this.afterRecordingAction,
				c.getMerge(provider.getMerge()), provider);

		for (DVBViewerEntry co : this.recordEntries) {
			if (co.isFilterElement() && e.isOrgEqual(co)) {
				co.resetMissing();
				if (provider.isFilterEnabled())
					return false;
				else {
					co.setToDelete();
					break;
				}
			}
		}
		this.addRecordingEntry(e);
		return true;
	}

	public boolean shiftEntry(DVBViewerEntry shiftEntry, Provider provider,
			String providerID, String channel, long start, long end,
			String title) {
		Channel c = this.getDVBViewerChannel(provider.getID(), channel);
		TimeOffsets o = c.getOffsets();
		long startOrg = start;
		start -= o.getPreOffset(start) * 60000;
		long endOrg = end;
		end += o.getPostOffset(end) * 60000;

		if (end < System.currentTimeMillis()) {
			shiftEntry.setToDelete();
			return false;
		}
		DVBViewerEntry e = new DVBViewerEntry(c.getDVBViewer(),
				c.getChannelSet(), providerID, start, end, startOrg, endOrg,
				"-------", title, this.timerAction, this.afterRecordingAction,
				c.getMerge(provider.getMerge()), provider);
		e = shiftEntry.shift(e);
		if (e != null) {
			this.addRecordingEntry(e);
		}
		return true;
	}

	public void deleteEntry(Provider provider, String channel, long start,
			long end, String title) {
		Channel c = this.getDVBViewerChannel(provider.getID(), channel);

		DVBViewerEntry e = new DVBViewerEntry(c.getDVBViewer(),
				c.getChannelSet(), null, start, end, start, end, "-------",
				title, this.timerAction, this.afterRecordingAction,
				c.getMerge(provider.getMerge()), provider);

		for (DVBViewerEntry co : this.recordEntries) {
			if (e.isOrgEqual(co)) {
				co.setToDelete();
				break;
			}
		}
	}

	public void deleteEntry(DVBViewerEntry entry) {
		entry.setToDelete();
	}

	public ArrayList<DVBViewerEntry> getRecordEntries() {
		return this.recordEntries;
	};

	public void resetRecordEntries() {
		this.recordEntries = null;
	};

	public String getExePath() {
		return this.exePath;
	};

	public String getExeName() {
		return this.exeName;
	};

	public String getPluginConfPath() {
		return this.dvbViewerPluginDataPath;
	};

	public String getDVBViewerDataPath() {
		return this.dvbViewerDataPath;
	};

	public dvbviewertimerimport.dvbviewer.channels.Channels getChannels() {
		return channels;
	}

	public String getDVBViewerPath() {
		return this.dvbViewerPath;
	};

	public void setDVBViewerPath(final String dvbViewerPath) {
		this.isDVBViewerPathSetExternal = true;
		this.dvbViewerPath = dvbViewerPath;
		if (dvbViewerPath != null)
			this.determineDataPath();
		else
			dvbViewerDataPath = null;
	};

	public boolean isDVBViewerPathSetExternal() {
		return this.isDVBViewerPathSetExternal;
	};

	public String getDVBExePath() {
		if (this.dvbExePath != null)
			return this.dvbExePath;
		else
			return this.dvbViewerPath + File.separator + NAME_DVBVIEWER_EXE;
	};

	public void setChannelChangeTime(final int waitTime) {
		this.channelChangeTime = waitTime;
	};

	public int getChannelChangeTime() {
		return this.channelChangeTime;
	};

	public void setWaitCOMTime(final int waitTime) {
		this.waitCOMTime = waitTime;
	};

	public int getWaitCOMTime() {
		return this.waitCOMTime;
	};

	public void setDVBExePath(final String dvbExePath) {
		this.dvbExePath = dvbExePath;
	};

	public boolean isDVBExePathSetExternal() {
		return this.dvbExePath != null;
	};

	public String getViewParameters() {
		return this.viewParameters;
	};

	public void setViewParameters(final String viewParameters) {
		this.viewParameters = viewParameters;
	};

	public String getRecordingParameters() {
		return this.recordingParameters;
	};

	public void setRecordingParameters(final String recordingParameters) {
		this.recordingParameters = recordingParameters;
	};

	public boolean getStartIfRecording() {
		return this.startIfRecording;
	};

	public void setStartIfRecording(boolean startIfRecording) {
		this.startIfRecording = startIfRecording;
	};

	public void setService(DVBViewerService s) {
		this.service = s;
	}

	public DVBViewerService getService() {
		return this.service;
	};

	public void setEnableWOL(boolean e) {
		this.service.setEnableWOL(e);
	};

	public void setBroadCastAddress(String b) {
		this.service.setBroadCastAddress(b);
	};

	public void setMacAddress(String m) {
		this.service.setMacAddress(m);
	};

	public void setWaitTimeAfterWOL(int w) {
		this.service.setWaitTimeAfterWOL(w);
	};

	public void setAfterRecordingAction(ActionAfterItems dvbViewerActionAfter) {
		this.afterRecordingAction = dvbViewerActionAfter;
	};

	public ActionAfterItems getAfterRecordingAction() {
		return this.afterRecordingAction;
	};

	public void setTimerAction(TimerActionItems timerAction) {
		this.timerAction = timerAction;
	};

	public TimerActionItems getTimerAction() {
		return this.timerAction;
	};

	public void clearChannelLists() {
		this.channelsLists = new ArrayList<HashMap<String, Channel>>(
				dvbviewertimerimport.provider.Provider.getProviders().size());
		this.channelSetMap = new HashMap<Long, ChannelSet>();
		this.setProvider();
	}

	private void addChannel(HashMap<String, Channel> channels,
			String channelName, Channel channel, String channelGroupName) {
		if (channelName == null)
			return;
		if (channels.containsKey(channelName)
				&& !channelGroupName.equals("DVBViewer")) // TODO workarround,
															// solution:
															// TimerEntry must
															// contain the
															// channelSetID
			throw new ErrorClass("The " + channelGroupName + " channel \""
					+ channelName + "\" is not unique");
		channels.put(new String(channelName), channel);
	}

	public void addChannel(ChannelSet channelSet) {
		Channel c = new Channel(channelSet, channelSet.getTimeOffsets(),
				channelSet.getMerge());
		for (dvbviewertimerimport.control.Channel cC : channelSet.getChannels()) {
			int type = cC.getType();
			this.addChannel(this.channelsLists.get(type), cC.getName(), c,
					cC.getTypeName());
		}
		this.channelSetMap.put(channelSet.getID(), channelSet);
	}

	public void merge() {
		for (int iO = 0; iO < this.recordEntries.size(); iO++) {
			if (!this.recordEntries.get(iO).toMerge())
				continue;
			boolean changed = true;
			while (changed) {
				changed = false;
				DVBViewerEntry o = this.recordEntries.get(iO);

				for (int iI = iO + 1; iI < this.recordEntries.size(); iI++) {
					DVBViewerEntry i = this.recordEntries.get(iI);
					if (o.mustMerge(i)) {
						DVBViewerEntry newEntry = o.update(i, true);
						if (newEntry != null)
							this.addRecordingEntry(newEntry);
						changed = true;
						break;
					}
				}
			}
		}
	}

	public void reworkMergeElements() {
		DVBViewerEntry.reworkMergeElements(this.recordEntries, this.separator,
				this.maxID, this.maxTitleLength);
	}

	public void setDVBViewerTimers() throws InterruptedException {
		// DVBViewerEntry.removeOutdatedProviderEntries( this.recordEntries );

		this.merge();
		DVBViewerEntry.beforeRecordingSettingProcces(this.recordEntries,
				this.separator, this.maxID, this.maxTitleLength);

		int updatedEntries = 0;
		int newEntries = 0;
		int deletedEntries = 0;

		for (DVBViewerEntry d : this.recordEntries) {
			if (d.mustDVBViewerDeleted())
				deletedEntries++;
			if (d.mustUpdated())
				updatedEntries++;
			if (d.mustDVBViewerCreated())
				newEntries++;
		}

		if (this.service != null && this.service.isEnabled())
			this.service.setTimers(this.recordEntries);
		else if (!DVBViewerCOM.setTimers(this.recordEntries)) {
			timersXML.setTimers(this.recordEntries);
			if (this.startIfRecording)
				this.startDVBViewerIfRecording();
		}

		Log.out(false,
				"Number of new entries:     " + Integer.toString(newEntries)
						+ "\nNumber of deleted entries: "
						+ Integer.toString(deletedEntries)
						+ "\nNumber of updated entries: "
						+ Integer.toString(updatedEntries));

		DVBViewerEntry.afterRecordingSettingProcces(this.recordEntries);
	}

	public void setSeparator(String s) {
		this.separator = s;
	};

	public String getSeparator() {
		return this.separator;
	};

	public void setMaxTitleLength(int t) {
		this.maxTitleLength = t;
	};

	public int getMaxTitleLength() {
		return this.maxTitleLength;
	};

	public ArrayList<DVBViewerEntry> readXML() {
		maxID.reset();

		ArrayList<DVBViewerEntry> result = new ArrayList<DVBViewerEntry>();

		HashMap<Long, DVBViewerEntry> idMap = new HashMap<Long, DVBViewerEntry>();

		File f = new File(this.xmlFilePath + File.separator
				+ NAME_XML_PROCESSED_RECORDINGS);
		if (!f.exists())
			return result;

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader reader = inputFactory
					.createXMLEventReader(new StreamSource(f));
			StackXML<String> stack = new StackXML<String>();

			while (reader.hasNext()) {
				XMLEvent ev = reader.nextEvent();
				if (ev.isStartElement()) {
					stack.push(ev.asStartElement().getName().getLocalPart());
					if (!stack.equals(DVBViewer.xmlPath))
						continue;
					DVBViewerEntry entry = DVBViewerEntry.readXML(reader, ev,
							f.getName(), this.channelSetMap);
					idMap.put(entry.getID(), entry);
					entry.setID(this.maxID.increment());
					result.add(entry);
					stack.pop();
					continue;
				}
				if (ev.isEndElement())
					stack.pop();
			}
			reader.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass(e, "Error on readin XML file \"" + f.getName()
					+ ". Position: Line = "
					+ Integer.toString(e.getLocation().getLineNumber())
					+ ", column = "
					+ Integer.toString(e.getLocation().getColumnNumber()));
		} catch (Exception e) {
			throw new ErrorClass(e, "Unexpected error on read XML file \""
					+ f.getName());
		}

		DVBViewerEntry.assignMergedElements(idMap);

		DVBViewerEntry.removeOutdatedEntries(result);

		return result;
	}

	public void writeXML() {
		XMLOutputFactory output = XMLOutputFactory.newInstance();

		XMLStreamWriter writer = null;
		File file = new File(this.xmlFilePath + File.separator
				+ NAME_XML_PROCESSED_RECORDINGS);

		try {
			FileOutputStream os = null;
			try {
				writer = output.createXMLStreamWriter(
						(os = new FileOutputStream(file)), "ISO-8859-1");
			} catch (FileNotFoundException e) {
				throw new ErrorClass(e,
						"Unexpecting error on writing to file \""
								+ file.getPath() + "\". Write protected?");
			}
			IndentingXMLStreamWriter sw = new IndentingXMLStreamWriter(writer);
			sw.setIndent("    ");
			sw.writeStartDocument("ISO-8859-1", "1.0");
			sw.writeStartElement("Processed");
			sw.writeNamespace("xsi",
					"http://www.w3.org/2001/XMLSchema-instance");
			sw.writeAttribute("xsi:noNamespaceSchemaLocation",
					"DVBVTimerImportPrcd.xsd");
			for (DVBViewerEntry e : recordEntries) {
				e.writeXML(sw, file);
			}
			sw.writeEndElement();
			sw.writeEndDocument();
			sw.flush();
			sw.close();
			os.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass(e, "Error on writing XML file \""
					+ file.getAbsolutePath() + ". Position: Line = "
					+ Integer.toString(e.getLocation().getLineNumber())
					+ ", column = "
					+ Integer.toString(e.getLocation().getColumnNumber()));
		} catch (IOException e) {
			throw new ErrorClass(e, "Error on writing XML file \""
					+ file.getAbsolutePath());
		}
	}

	public void mergeXMLWithDVBViewerData(ArrayList<DVBViewerEntry> lastTimers) {
		if (lastTimers == null)
			lastTimers = readXML();

		DVBViewerEntry.updateXMLDataByDVBViewerData(lastTimers,
				this.recordEntries, this.separator, this.maxID,
				this.maxTitleLength);
		this.recordEntries = lastTimers;
	}

	public static boolean loadDVBViewerCOMDll() {
		if (!checkAndGetDVBViewerCOMDllIfNecessary(false))
			return false;

		if (DVBViewer.isDLLloaded)
			return true;

		File f = new File(DVBViewer.PATH_PLUGIN_DATA + File.separator
				+ DVBViewer.NAME_DVBVIEWER_COM_DLL + ".dll");

		System.load(f.getAbsolutePath());
		DVBViewer.setDLLisLoaded();
		return true;
	}

	public static boolean checkAndGetDVBViewerCOMDllIfNecessary(boolean force) {
		if (DVBViewer.isDLLloaded)
			return true;

		if (!Constants.IS_WINDOWS)
			return false;

		String jvmArch = System.getProperty("os.arch");

		if (!jvmArch.equals("x86")) {
			Log.out(false, true, ResourceManager.msg("64_JRE_NOT_SUPPORTED"),
					true);
			throw new Error(ResourceManager.msg("64_JRE_NOT_SUPPORTED"));
		}

		String exePath = DVBViewer.PATH_PLUGIN_DATA;

		File f = new File(exePath + File.separator
				+ DVBViewer.NAME_DVBVIEWER_COM_DLL + ".dll");

		if (!f.canExecute() || force) {
			if (!new File(exePath).canWrite()) {
				Log.error(ResourceManager.msg("ADMINISTRATOR"));
				throw new TerminateClass(1);
			}
			ResourceManager.copyBinaryFile(exePath, "datafiles/"
					+ NAME_DVBVIEWER_COM_DLL + ".dll");
		}
		return true;

	}

	public static void getDVBViewerCOMDllAndCheckVersion(boolean load) {
		if (!Constants.IS_WINDOWS)
			return;

		if (load)
			DVBViewer.checkAndGetDVBViewerCOMDllIfNecessary(true);
		if (!DVBViewerCOM.getVersion()
				.equals(Versions.getDVBViewerCOMVersion())) {
			Log.error(ResourceManager.msg("PACKAGE"));
			throw new TerminateClass(1);
		}
	}

	public boolean isDVBViewerExecuting() {
		if (DVBViewerCOM.connect()) // actual running
		{
			DVBViewerCOM.disconnect();
			return true;
		}
		return false;
	}

	public static void setDLLisLoaded() {
		DVBViewer.isDLLloaded = true;
	}

	public boolean startDVBViewer() {
		if (isDVBViewerExecuting())
			return true;
		File f = new File(this.getDVBExePath());
		if (!f.canExecute())
			return false;
		try {
			ProcessBuilder process = new ProcessBuilder(f.getAbsolutePath());
			process.start();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean selectChannel(String channelID, final boolean wait) {
		if (channelID == null)
			return false;

		final DVBViewer dvbViewer = this;

		final String[] parts = channelID.split("\\|");
		if (parts.length != 2)
			return false;

		synchronized (this) {
			this.selectedChannel = parts[0];
			if (this.isThreadListening == true) {
				return true;
			}
		}

		Thread thread = new Thread("DVBViewer select channel") {
			private final boolean repeatChannelSelection = true;

			public void run() {
				long timeOutTime = System.currentTimeMillis() + TIMEOUT_S
						* 1000;
				while (!DVBViewerCOM.connect()) {
					try {
						Thread.sleep(DVBVIEWER_SCAN_TIME_MS);
					} catch (InterruptedException e) {
					}
					if (System.currentTimeMillis() > timeOutTime) {
						timeOutTime = -1;
						break;
					}
				}

				if (timeOutTime < 0) {
					Log.out("Timeout error occured while DVBViewer channel selection");
					return;
				}

				DVBViewerCOM.disconnect();

				if (wait) {
					try {
						Thread.sleep(waitCOMTime * 1000);
					} catch (InterruptedException e) {
					}
				}

				String selectedChannel = "";
				int channelNo = -99;
				timeOutTime = System.currentTimeMillis()
						+ dvbViewer.channelChangeTime * 1000
						+ DVBVIEWER_CHANNEL_TIME_MS / 2;

				boolean waitingFinished = !wait;

				while (true) {
					if (waitingFinished || repeatChannelSelection) {

						if (!DVBViewerCOM.connect()) {
							dvbViewer.isThreadListening = false;
							break;
						}

						if (channelNo != DVBViewerCOM.getCurrentChannelNo()
								|| selectedChannel != dvbViewer.selectedChannel) {
							selectedChannel = dvbViewer.selectedChannel;
							channelNo = DVBViewerCOM
									.setCurrentChannel(selectedChannel);
						}

						// System.err.println( channelNo ) ;

						DVBViewerCOM.disconnect();
					}
					if (waitingFinished || !wait) {
						synchronized (dvbViewer) {
							if (selectedChannel
									.equals(dvbViewer.selectedChannel)) {
								dvbViewer.isThreadListening = false;
								break;
							}
						}
					}
					if (wait && timeOutTime >= System.currentTimeMillis()) {
						try {
							Thread.sleep(DVBVIEWER_CHANNEL_TIME_MS);
						} catch (InterruptedException e) {
						}
					} else {
						waitingFinished = true;
					}
				}
			}
		};

		this.isThreadListening = true;
		thread.start();
		return true;
	}

	public boolean startDVBViewerAndSelectChannel(String channelID) {
		if (DVBViewerCOM.connect()) // actual running
		{
			DVBViewerCOM.disconnect();
			return selectChannel(channelID, false);
		}
		File f = new File(this.getDVBExePath());
		if (!f.canExecute())
			return false;
		try {
			// Runtime.getRuntime().exec( f.getAbsolutePath() + " -c\"" +
			// parts[1] + ":" + parts[0] + "\"" ) ;
			String[] tokens = getTokens(f.getAbsolutePath(),
					this.getViewParameters());
			ProcessBuilder process = new ProcessBuilder(tokens);
			process.start();
		} catch (IOException e) {
			return false;
		}

		return selectChannel(channelID, true);
	}

	public boolean startDVBViewerIfRecording() {
		boolean isRecording = false;
		long now = System.currentTimeMillis();
		for (DVBViewerEntry entry : this.recordEntries) {
			if (entry.isDisabled())
				continue;
			if (!entry.isInRange(now, now))
				continue;
			isRecording = true;
			break;
		}

		if (!isRecording)
			return true;

		File f = new File(this.getDVBExePath());
		if (!f.canExecute())
			return false;
		try {
			String[] tokens = getTokens(f.getAbsolutePath(),
					this.getViewParameters());
			ProcessBuilder process = new ProcessBuilder(tokens);
			process.start();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	private static String[] getTokens(String command, String paras) {
		// Log.out(paras) ;
		StringTokenizer tokenizer = new StringTokenizer(paras);
		String[] result = new String[tokenizer.countTokens() + 1];
		result[0] = command;
		for (int i = 0; i < tokenizer.countTokens(); ++i) {
			String token = tokenizer.nextToken();
			result[i + 1] = token;
			// Log.out(token) ;
		}
		return result;
	}
}
