// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Comparator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;
import dvbviewertimerimport.dvbviewer.DVBViewerProvider;
import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.xml.Conversions;
import dvbviewertimerimport.xml.StackXML;

public abstract class Provider implements DVBViewerProvider {

	private static final boolean DEBUG = false;

	private static final StackXML<String> pathProvider = new StackXML<String>("Providers", "Provider");
	private static final StackXML<String> pathURL = new StackXML<String>("Providers", "Provider", "Url");
	private static final StackXML<String> pathSenderURL = new StackXML<String>("Providers", "Provider", "SenderUrl");
	private static final StackXML<String> pathMissing = new StackXML<String>("Providers", "Provider", "Missing");
	private static final StackXML<String> pathRegularExpression = new StackXML<String>("Providers", "Provider",
			"RegularExpression");

	private enum XMLStatus {
		UKNOWN, MISSING, PROVIDER, REGEX
	}

	private static ArrayList<String> names = new ArrayList<String>();
	private static ArrayList<Provider> providers = new ArrayList<Provider>();
	private static boolean isPlugin = false;

	private static Stack<ArrayList<String>> nameStack = new Stack<ArrayList<String>>();
	private static Stack<ArrayList<Provider>> providerStack = new Stack<ArrayList<Provider>>();

	public Channel createChannel(String name, String userName, String id, boolean user) {
		return new Channel(this.pid, name, userName, id, user);
	}

	public static void push() {
		Provider.nameStack.push(Provider.names);
		Provider.providerStack.push(Provider.providers);
		Provider.names = new ArrayList<String>();
		Provider.providers = new ArrayList<Provider>();
	}

	public static void pop() {
		Provider.names = Provider.nameStack.pop();
		Provider.providers = Provider.providerStack.pop();
	}

	public static void setIsPlugin() {
		isPlugin = true;
	}

	public static boolean isPlugin() {
		return isPlugin;
	}

	public static int size() {
		return Provider.providers.size();
	}

	public static Provider processingProvider = null;

	public static void updateRecordingsAllProviders(ArrayList<DVBViewerEntry> entries) {
		for (Provider provider : Provider.providers) {
			if (!isPlugin && provider.name.equals("TV-Browser"))
				continue;
			provider.updateRecordings(entries);
		}
	}

	private final int pid;
	protected final Control control;
	private final boolean hasAccount;
	private final boolean hasURL;
	private final boolean canExecute;
	private final boolean canTest;
	private final boolean mustInstall;
	protected boolean canImport = false;
	protected boolean canModify = true;
	protected boolean canAddChannel = true;
	private final String name;
	protected String url = "";
	protected String senderURL = "";
	protected String username = null;
	protected String password = null;
	private int triggerAction = -1;
	private boolean merge = false;
	private boolean verbose = false;
	private boolean message = false;
	private boolean filter = false;
	private boolean isFilterEnabled = true;
	private boolean isPrepared = false;
	protected boolean isFunctional = true;
	protected boolean isSilent;
	private OutDatedInfo outDatedLimits = null;
	protected TimeZone timeZone = TimeZone.getDefault();

	private Collection<Channel> allChannels = null;
	private Set<String> userChannelNames = null;

	private static class RegularExpression {
		private final String id;
		private final String regString;

		public RegularExpression(String id, String regString) {
			this.id = id;
			this.regString = regString;
		}
	}

	protected Collection<RegularExpression> regularExpressions = new ArrayList<>();

	public Provider(Control control, boolean hasAccount, boolean hasURL, String name, boolean canExecute,
			boolean canTest, boolean filter, boolean mustInstall, boolean silent, boolean isOutDatedLimitsEnabled) {
		this.control = control;
		this.hasAccount = hasAccount;
		this.hasURL = hasURL;
		this.name = name;
		this.pid = Provider.providers.size();
		this.canExecute = canExecute;
		this.canTest = canTest;
		this.filter = filter;
		this.mustInstall = mustInstall;
		this.isPrepared = !isOutDatedLimitsEnabled;
		this.isSilent = silent;
		if (isOutDatedLimitsEnabled) {
			this.outDatedLimits = new OutDatedInfo(true);
		}
		Provider.names.add(name);
		Provider.providers.add(this);
	}

	public int getID() {
		return this.pid;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public String getURL() {
		return this.url;
	}

	public void setURL(final String url) {
		this.url = url;
	}

	public String getUserName() {
		return this.username;
	}

	public void setUserName(final String name) {
		this.username = name;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public int getTriggerAction() {
		return this.triggerAction;
	}

	public void setTriggerAction(int triggerAction) {
		this.triggerAction = triggerAction;
	}

	public boolean isFiltered() {
		return this.filter;
	}

	public void setFilter(boolean filter) {
		this.filter = filter;
	}

	public boolean isFilterEnabled() {
		return this.isFilterEnabled;
	}

	public void setFilterEnabled(boolean enable) {
		this.isFilterEnabled = enable;
	}

	public boolean getMerge() {
		return this.merge;
	}

	public void setMerge(boolean m) {
		this.merge = m;
	}

	public boolean getVerbose() {
		return this.verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean getMessage() {
		return this.message;
	}

	public void setMessage(boolean message) {
		this.message = message;
	}

	public boolean hasURL() {
		return this.hasURL;
	}

	public boolean hasAccount() {
		return this.hasAccount;
	}

	public boolean canExecute() {
		return this.canExecute;
	}

	public boolean canTest() {
		return this.canTest;
	}

	public boolean canImport() {
		return this.canImport && this.isFunctional;
	}

	public boolean canModify() {
		return this.canModify;
	}

	public boolean canAddChannel() {
		return this.canAddChannel;
	}

	public boolean mustInstall() {
		return this.mustInstall;
	}

	public boolean isPrepared() {
		return this.isPrepared;
	}

	public boolean isSilent() {
		return this.isSilent;
	}

	public boolean isFunctional() {
		return this.isFunctional;
	}

	public void setIsFunctional(final boolean status) {
		this.isFunctional = status;
	}

	public void setPrepared(boolean prepared) {
		this.isPrepared = prepared;
	}

	public OutDatedInfo getOutDatedLimits() {
		return this.outDatedLimits;
	}

	public boolean install() {
		return true;
	}

	public boolean uninstall() {
		return true;
	}

	/**
	 * 
	 * @param channel
	 * @param userChannels true: user search over user channels
	 * @return True if channel is supported by the provider
	 */

	public boolean containsChannel(final Channel channel, boolean userChannels) {
		if (!userChannels) {
			return true;
		}
		if (this.userChannelNames == null) {
			if (this.allChannels == null) {
				this.allChannels = this.readChannels();
			}
			this.userChannelNames = new HashSet<>();
			for (Channel c : this.allChannels) {
				if (c.isUser()) {
					this.userChannelNames.add(c.getName());
				}
			}
		}
		if (this.userChannelNames.size() != 0) {
			return this.userChannelNames.contains(channel.getName());
		} else {
			return true;
		}
	}

	public boolean isChannelMapAvailable() {
		return false;
	}

	public int importChannels() {
		int changes = this.assignChannels();
		if (changes > 0) {
			this.userChannelNames = null;
		}
		return changes;
	}

	/**
	 * 
	 * @return true, if importer get all possible channels of the provider
	 */
	public boolean isAllChannelsImport() {
		return false;
	}

	protected Collection<Channel> readChannels() {
		return null;
	}

	public void updateRecordings(ArrayList<DVBViewerEntry> entries) {
	}

	@Override
	public boolean process(boolean getAll, DVBViewer.Command command) {
		return true;
	}

	@Override
	public boolean processEntry(Object args, DVBViewer.Command command) {
		return true;
	}

	public void check() {
		if (this.hasAccount && (this.username == null || this.password == null))
			throw new ErrorClass("Username or password is missing");

		if (this.hasURL && this.url == null)
			throw new ErrorClass("URL is missing");
	}

	public String test() {
		return null;
	}

	public static boolean isSilentProcessing() {
		if (Provider.processingProvider == null)
			return false;
		else
			return Provider.processingProvider.isSilent;
	}

	public static Provider getProcessingProvider() {
		return Provider.processingProvider;
	}

	public static void setProcessingProvider(final Provider p) {
		Provider.processingProvider = p;
	}

	public static ArrayList<Provider> getProviders() {
		return Provider.providers;
	}

	public static boolean contains(String provider) {
		return Provider.names.contains(provider);
	}

	public static Provider getProvider(String providerName) {
		int pos = Provider.names.indexOf(providerName);
		if (pos < 0)
			return null;
		return Provider.providers.get(pos);
	}

	public static Provider getProvider(int id) {
		return Provider.providers.get(id);
	}

	public static String getProviderName(int id) {
		return Provider.names.get(id);
	}

	public static int getProviderID(String provider) {
		return Provider.names.indexOf(provider);
	}

	public static void readXML(XMLEventReader reader, String fileName) throws XMLStreamException {
		StackXML<String> stack = new StackXML<String>();
		stack.push("Providers");
		XMLEvent ev = null;

		Provider provider = null;

		String name = null;
		String username = null;
		String password = null;
		int triggerAction = -1;
		boolean verbose = false;
		boolean message = false;
		boolean merge = false;
		boolean filter = false;
		OutDatedInfo info = new OutDatedInfo(true);
		String url = "";
		String senderURL = "";

		String regularId = null;
		String regularExpression = null;

		XMLStatus xmlStatus = XMLStatus.UKNOWN;

		while (reader.hasNext()) {
			try {
				ev = reader.nextEvent();
			} catch (XMLStreamException e1) {
				throw new ErrorClass(e1, "XML syntax error in file \"" + fileName + "\"");
			}

			if (ev.isStartElement()) {
				stack.push(ev.asStartElement().getName().getLocalPart());

				if (stack.equals(pathProvider)) {
					name = null;
					username = null;
					password = null;
					triggerAction = -1;
					verbose = false;
					message = false;
					merge = false;
					filter = false;
					senderURL = "";
					url = "";

					xmlStatus = XMLStatus.PROVIDER;
				} else if (stack.equals(pathMissing)) {
					info = new OutDatedInfo(true);

					xmlStatus = XMLStatus.MISSING;
				} else if (stack.equals(pathRegularExpression)) {
					regularId = null;
					regularExpression = "";
					xmlStatus = XMLStatus.REGEX;
				}

				@SuppressWarnings("unchecked")
				Iterator<Attribute> iter = ev.asStartElement().getAttributes();

				while (iter.hasNext()) {
					Attribute a = iter.next();
					String attributeName = a.getName().getLocalPart();
					String value = a.getValue().trim();

					switch (xmlStatus) {
						case PROVIDER:
							if (attributeName.equals("name"))
								name = value;
							else if (attributeName.equals("username"))
								username = value;
							else if (attributeName.equals("password"))
								password = value;
							else if (attributeName.equals("triggeraction")) {
								if (!value.matches("\\d+"))
									throw new ErrorClass(ev, "Wrong triggeraction format in file \"" + fileName + "\"");
								triggerAction = Integer.valueOf(value);
							} else if (attributeName.equals("merge"))
								merge = Conversions.getBoolean(value, ev, fileName);
							else if (attributeName.equals("verbose"))
								verbose = Conversions.getBoolean(value, ev, fileName);
							else if (attributeName.equals("message"))
								message = Conversions.getBoolean(value, ev, fileName);
							else if (attributeName.equals("filter"))
								filter = Conversions.getBoolean(value, ev, fileName);
							break;

						case MISSING:
							try {
								info.readXML(attributeName, value);
							} catch (ErrorClass e) {
								throw new ErrorClass(ev, e.getErrorString() + " in file \"" + fileName + "\"");
							}
							break;
						case REGEX:
							if (attributeName.equals("id")) {
								regularId = value;
							}
						default:
							break;
					}
				}
			}
			if (ev.isCharacters()) {
				String data = ev.asCharacters().getData();
				if (data.startsWith("null")) {
					data = data.substring(4);
				}
				if (stack.equals(pathURL)) {
					url += data.trim();
				} else if (stack.equals(pathSenderURL)) {
					senderURL += data.trim();
				} else if (stack.equals(pathRegularExpression)) {
					regularExpression += data.trim();
				}
			}
			if (ev.isEndElement()) {
				if (stack.equals(pathProvider)) {
					provider = Provider.getProvider(name);
					if (provider == null)
						throw new ErrorClass(ev, "Unknown provider name in file \"" + fileName + "\"");
					provider.username = username;
					provider.password = password;
					provider.triggerAction = triggerAction;
					provider.verbose = verbose;
					provider.message = message;
					provider.merge = merge;
					provider.filter = filter;
					if (provider.outDatedLimits != null) {
						provider.outDatedLimits = info;
						info = null;
					}
					provider.senderURL = senderURL;
					senderURL = "";
					provider.url = url;
					url = "";
				} else if (stack.equals(pathRegularExpression)) {
					provider = Provider.getProvider(name);
					provider.regularExpressions.add(new RegularExpression(regularId, regularExpression));
				}
				stack.pop();
				if (stack.size() == 1)
					try {
						provider.check();
					} catch (ErrorClass e) {
						throw new ErrorClass(ev, e.getErrorString() + " in file \"" + fileName + "\"");
					}
				else if (stack.size() == 0)
					break;
			}
		}
	}

	public static void writeXML(IndentingXMLStreamWriter sw) throws XMLStreamException {
		sw.writeStartElement("Providers");
		for (Provider provider : Provider.providers) {
			sw.writeStartElement("Provider");
			sw.writeAttribute("name", provider.name);
			if (provider.hasAccount) {
				sw.writeAttribute("username", provider.username);
				sw.writeAttribute("password", provider.password);
			}
			if (provider.triggerAction >= 0)
				sw.writeAttribute("triggeraction", Integer.toString(provider.triggerAction));
			sw.writeAttribute("merge", provider.merge);
			sw.writeAttribute("message", provider.message);
			sw.writeAttribute("verbose", provider.verbose);
			sw.writeAttribute("filter", provider.filter);
			if (provider.hasURL) {
				sw.writeStartElement(pathURL.lastElement());
				sw.writeCharacters(provider.url);
				sw.writeEndElement();
			}
			if (!provider.senderURL.isEmpty()) {
				sw.writeStartElement(pathSenderURL.lastElement());
				sw.writeCharacters(provider.senderURL);
				sw.writeEndElement();
			}
			for (RegularExpression expr : provider.regularExpressions) {
				sw.writeStartElement(pathRegularExpression.lastElement());
				sw.writeAttribute("id", expr.id);
				sw.writeCData(expr.regString);
				sw.writeEndElement();
			}
			if (provider.outDatedLimits != null) {
				sw.writeStartElement("Missing");
				provider.outDatedLimits.writeXML(sw);
				sw.writeEndElement();
			}

			sw.writeEndElement();
		}
		sw.writeEndElement();
	}

	public int assignChannels() {
		if (!this.isFunctional) {
			return 0;
		}
		Collection<Channel> channels = readChannels();

		if (channels == null)
			return -1;

		Map<String, List<Channel>> buildMap = new HashMap<>();

		for (Channel channel : channels) {
			String name = channel.getName();
			List<Channel> list = buildMap.get(name);
			if (list == null) {
				list = new ArrayList<>();
				buildMap.put(name, list);
			}
			list.add(channel);
		}

		Comparator<Channel> comparator = new Comparator<Channel>() {
			@Override
			public int compare(Channel o1, Channel o2) {
				return o1.getIDKey().compareTo(o2.getIDKey());
			}
		};

		for (List<Channel> list : buildMap.values()) {
			if (list.size() > 1) {
				list.sort(comparator);
			}
		}

		HashMap<Object, ChannelSet> mapByID = new HashMap<Object, ChannelSet>();
		HashMap<String, ChannelSet> mapByName = new HashMap<String, ChannelSet>();

		int pid = this.getID();

		// Channel channelProv = (Channel) channels.toArray()[0];

		// Creating of maps sorted by name and pid based on old data

		for (ChannelSet cs : this.control.getChannelSets()) {
			Channel c = cs.getChannel(pid);
			if (c == null)
				continue;
			mapByName.put(c.getName(), cs);
			Object key = c.getIDKey();
			if (c.hasId())
				mapByID.put(key, cs);
		}

		int count = 0;

		for (List<Channel> list : buildMap.values()) {
			for (int ix = 0; ix < list.size(); ++ix) {
				Channel c = list.get(ix);
				if (DEBUG) {
					Log.out("Channel processing: \"" + c.getName() + "\" of provider \"" + this.getName()
							+ "\" with provider key \"" + c.getIDKey() + "\"");
				}

				String channelName = c.getName();

				if (ix > 0) {
					channelName = c.getName() + "(" + ix + ")";
					c = new Channel(this.getID(), channelName, c.getUserName(), c.getIDKey(), c.isUser());
				}

				String key = c.getIDKey();
				ChannelSet former = mapByID.get(key);
				if (former != null) {
					mapByID.remove(key);
					mapByName.remove(former.getChannel(pid).getName());
					if (!former.getChannel(pid).getName().equals(c.getName())) {
						former.remove(pid);
						former.add(c);
					}
				} else {
					former = mapByName.get(channelName);
					if (former == null) {
						former = mapByName.get(c.getIDKey());
					}
					if (former != null) {
						mapByName.remove(former.getChannel(pid).getName());
						String id = former.getChannel(pid).getIDKey();
						if (id != null) {
							mapByID.remove(former.getChannel(pid).getIDKey());
						}
						former.remove(pid);
						former.add(c);
					}
				}

				if (former == null ) {
					ChannelSet cs = new ChannelSet();
					cs.add(c);
					this.control.getChannelSets().add(cs);
					Log.out("Channel \"" + c.getName() + "\" added to provider \"" + this.getName() + "\"");
					count++;
				}
			}
		}
		if (isAllChannelsImport() || DEBUG) {
			for (ChannelSet cs : mapByID.values()) {
				Log.out("Channel \"" + cs.getChannel(getID()).getName() + "\" removed from provider \"" + this.getName()
						+ "\"");
				mapByName.remove(cs.getChannel(pid).getName());
				cs.remove(getID());
			}
			for (ChannelSet cs : mapByName.values()) {
				Log.out("Channel \"" + cs.getChannel(getID()).getName() + "\" removed from provider \"" + this.getName()
						+ "\"");
				cs.remove(getID());
			}
		}
		return count;
	}

	public TimeZone getTimeZone() {
		return this.timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	protected static class ChannelsResult {
		Collection<String> allChannels = new ArrayList<String>();
		Collection<String> userChannels = new ArrayList<String>();
	}

	protected String getRegularExpression(String id) {
		for (RegularExpression expr : this.regularExpressions) {
			if (id.equals(expr.id)) {
				return expr.regString;
			}
		}
		return null;
	}

}
