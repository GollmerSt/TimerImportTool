// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TimeZone;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.misc.*;
import dvbviewertimerimport.provider.Provider;
import dvbviewertimerimport.xml.StackXML;

public final class TVInfo extends Provider {
	
	private static final boolean DEBUG = false ;

	private static final String senderURL = "http://www.tvinfo.de/system/_editSender.php";
	private static final String merkzettelURL = "http://www.tvinfo.de/merkzettel?LIMIT=200";

	private static final StackXML<String> xmlPathTVinfoEntry = new StackXML<String>(
			"epg_schedule", "epg_schedule_entry");
	private static final StackXML<String> xmlPathTVinfoTitle = new StackXML<String>(
			"epg_schedule", "epg_schedule_entry", "title");

	private final DVBViewer dvbViewer;
	private final SimpleDateFormat dateFormat;
	private final SimpleDateFormat htmlDateFormat;

	private ArrayList<MyEntry> unresolvedEntries = null;
	private HashSet<String> solvedChannels = new HashSet<String>();

	private HashSet<String> userSender = null;
	private ArrayList<Channel> allSender = null;

	public Provider getTVInfo() {
		return this;
	};

	public TVInfo(Control control) {
		super(control, true, true, "TVInfo", true, true, true, false, true,
				true);
		this.canModify = false;
		this.canAddChannel = false;
		this.canImport = true;
		this.dvbViewer = this.control.getDVBViewer();
		this.timeZone = TimeZone.getTimeZone("Europe/Berlin");
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.dateFormat.setTimeZone(timeZone);
		this.htmlDateFormat = new SimpleDateFormat("EE d.M.y H:m");
		this.htmlDateFormat.setTimeZone(timeZone);
	}

	@Override
	public Channel createChannel(String name, String id) {
		return new Channel(this.getID(), name, id) {
			@Override
			public Object getIDKey() {
				return this.getName();
			}

			@Override
			public Object getIDKey(final Channel c) {
				return c.getName();
			}; // ID of the provider, type is provider dependent
		};
	}

	private String getMD5() {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		md.update(this.password.getBytes());
		return Helper.bytesToString(md.digest());
	}

	public InputStream connect() throws DigestException,
			NoSuchAlgorithmException {
		String completeURL = this.url + "?username=" + this.username
				+ "&password=" + this.getMD5();
		return Html.getStream(completeURL, "TVInfo XML");
	}

	@Override
	public boolean test() {
		InputStream i = null;
		try {
			i = this.connect();
		} catch (ErrorClass e) {
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String comp = "<?xml version=\"1.0";
		byte[] buffer = new byte[100];
		try {
			i.read(buffer);
			i.close();
		} catch (IOException e) {
			return false;
		}
		String content = new String(buffer, 0, comp.length());
		if (content.length() < comp.length())
			return false;
		if (!content.equals(comp))
			return false;
		return true;
	}

	private class MyEntry {
		String channel = null;
		String providerID = null;
		long start = 0;
		long end = 0;
		String title = null;

		public void setTitle( String title) {
			String [] array = title.split("\\r|\\n") ;
			title= "" ;
			for ( String comp : array ) {
				title += comp ;
			}
			if ( this.title == null ) {
				this.title = title;
			} else {
				this.title += " " + title ;
			}
		}

		public boolean add() {
			if (channel == null || channel.length() == 0)
				return false;
			dvbViewer.addNewEntry(getTVInfo(), providerID, channel, start, end,
					title);
			solvedChannels.add(channel);
			return true;
		}

		public void readAttributes(XMLEvent ev) {
			@SuppressWarnings("unchecked")
			Iterator<Attribute> iter = ev.asStartElement().getAttributes();
			while (iter.hasNext()) {
				Attribute a = iter.next();
				String attributeName = a.getName().getLocalPart();
				String value = a.getValue();
				try {
					if (attributeName.equals("channel") && ! DEBUG)
						channel = value;
					else if (attributeName.equals("uid"))
						providerID = value;
					else if (attributeName.equals("starttime"))
						start = timeToLong(value);
					else if (attributeName.equals("endtime"))
						end = timeToLong(value);
					else if (attributeName.equals("title"))
						title = value;
				} catch (ParseException e) {
					throw new ErrorClass(ev, "Illegal TVinfo time");
				}
			}
			if ((start & end) == 0)
				throw new ErrorClass(ev,
						"Error in  TVinfo data, start or end time not given");
		}

		public long getKey() {
			return start;
		}; // Changed from start to end in case of a started recording

		@Override
		public String toString() {
			return this.title;
		};
	}

	@Override
	public boolean process(boolean all, DVBViewer.Command command) {
		this.unresolvedEntries = null;
		InputStream iS = null;
		try {
			iS = this.connect();
		} catch (DigestException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader reader = inputFactory
					.createXMLEventReader(new StreamSource(iS));
			StackXML<String> stack = new StackXML<String>();
			MyEntry entry = null;
			while (reader.hasNext()) {
				XMLEvent ev = reader.nextEvent();
				if (ev.isStartElement()) {
					stack.push(ev.asStartElement().getName().getLocalPart());
					if (!stack.equals(TVInfo.xmlPathTVinfoEntry))
						continue;
					entry = new MyEntry();
					entry.readAttributes(ev);
				}
				if (ev.isCharacters()) {
					if (stack.equals(TVInfo.xmlPathTVinfoTitle)) {
						String title = ev.asCharacters().getData() ;
						title.trim() ;
						entry.setTitle(title);
						boolean isAdded = false;

						try {
							isAdded = entry.add();
						} catch (ErrorClass e) {
							Log.out(e.getErrorString() + " Entry ignored");
						}

						if (!isAdded) {
							if (this.unresolvedEntries == null)
								this.unresolvedEntries = new ArrayList<MyEntry>();
							this.unresolvedEntries.add(entry);
							Log.out("Empty channelname in TVInfo data at line "
									+ Integer.toString(ev.getLocation()
											.getLineNumber()) + ", title: "
									+ entry.title);
						}
					}
				}
				if (ev.isEndElement())
					stack.pop();
			}
			reader.close();
		} catch (XMLStreamException e) {
			if (e.getLocation().getLineNumber() == 1
					&& e.getLocation().getColumnNumber() == 1)
				throw new ErrorClass(
						"No data available from TVInfo, account data should be checked.");
			else
				throw new ErrorClass(e, "Error on reading TVInfo data."
						+ " Position: Line = "
						+ Integer.toString(e.getLocation().getLineNumber())
						+ ", column = "
						+ Integer.toString(e.getLocation().getColumnNumber()));

		}
		if (this.unresolvedEntries != null )
			this.readMerklisteAndAddUnresolverEntries();
		return true;
	}

	public class MerkzettelParserCallback extends HTMLEditorKit.ParserCallback {
		private boolean isTableRead = false;
		private boolean isFinished = false;
		private boolean isHeader = false;
		private boolean isData = false;
		private int columnSender = -1;
		private int columnDatum = -1;
		private int columnUhrzeit = -1;
		private int columnTitel = -1;
		private int colCount = -1;
		private int rowCount = -1;
		private HashMap<Long, ArrayList<MyEntry>> entries = null;
		private MyEntry actualEntry = null;
		private String dateString = null;
		private String timeString = null;

		public HashMap<Long, ArrayList<MyEntry>> getResult() {
			return this.entries;
		};
		
		public Boolean isOK() {
			return true ;
		}

		@Override
		public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
			if (isFinished) {
				return;
			}
			if (t == HTML.Tag.TABLE) {
				if (a.containsAttribute(HTML.Attribute.CLASS, "list")) {
					isTableRead = true;
				}
			} else if (this.isTableRead) {
				if (t == HTML.Tag.TR) {
					colCount = -1;
					++rowCount;
					if (rowCount > 0) {
						this.actualEntry = new MyEntry();
						this.dateString = null;
						this.timeString = null;
					}
				} else if (t == HTML.Tag.TH) {
					++colCount;
					isHeader = true;
				} else if (t == HTML.Tag.TD) {
					++colCount;
					isData = true;
				}
			}
		}

		@Override
		public void handleEndTag(HTML.Tag t, int pos) {
			if (isFinished) {
				return;
			}
			if (isTableRead) {
				if (t == HTML.Tag.TABLE) {
					isFinished = true;
				} else if (t == HTML.Tag.TH) {
					isHeader = false;
				} else if (t == HTML.Tag.TD) {
					isData = false;
				} else if (t == HTML.Tag.TR) {
					if (rowCount > 0) {
						if (entries == null) {
							entries = new HashMap<Long, ArrayList<MyEntry>>();
						}
						if (this.dateString != null && this.timeString != null) {
							try {
								actualEntry.start = htmlTimeToLong( this.dateString, this.timeString ) ;
								ArrayList<MyEntry> list = new ArrayList<MyEntry>();
								list.add(actualEntry);
								this.entries.put(actualEntry.getKey(),
										list);
							} catch (ParseException e) {
							}
						}
					}
				}
			}
		}

		@Override
		public void handleText(char[] data, int pos) {
			if (isFinished) {
				return;
			}
			if (this.isHeader) {
				String dataString = new String(data) ;
				if (dataString.equals("Sender")) {
					this.columnSender = colCount;
				} else if (dataString.equals("Datum")) {
					this.columnDatum = colCount;
				} else if (dataString.equals("Uhrzeit")) {
					this.columnUhrzeit = colCount;
				} else if (dataString.equals(" ")) {
					this.columnTitel = colCount;
				}
			} else if (this.isData) {
				String dataString = new String(data);

				if (colCount == columnSender) {
					this.actualEntry.channel = dataString;
				} else if (colCount == columnTitel) {
					this.actualEntry.title = dataString;
				} else if (colCount == columnDatum) {
					this.dateString = dataString;
				} else if (colCount == columnUhrzeit) {
					this.timeString = dataString;
				}
			}
		}

	}

	public void readMerklisteAndAddUnresolverEntries() {
		String md5 = this.getMD5();
		String completeURL = TVInfo.merkzettelURL + "&user=" + this.username
				+ "&pass=" + md5;

		InputStream stream = null;

		try {
			stream = Html.getStream(completeURL, "TVInfo Merkzettel",
					"tvusername=" + this.username + "; tvuserpass=" + md5);
		} catch (ErrorClass e) {
			Log.out(e.getErrorString());
			return;
		}

		MerkzettelParserCallback myCallBack = new MerkzettelParserCallback();

		try {
			new ParserDelegator().parse(new InputStreamReader(stream),
					myCallBack, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HashMap<Long, ArrayList<MyEntry>> hashMap = myCallBack.getResult();

		if (hashMap == null || !myCallBack.isOK()) {
			Log.out("Empty channelnames can't assigned. Merkzettel format changed? Entries ignored");
			return;
		}

		for (MyEntry e : this.unresolvedEntries) {
			if (hashMap.containsKey(e.getKey())) {
				ArrayList<MyEntry> entries = hashMap.get(e.getKey());

				entries = dvbviewertimerimport.misc.Helper.getTheBestChoices(
						e.toString(), entries, 0, 1, null);
				if (entries.size() > 1) {
					Log.out("Empty channelnname can't assigned to a entry of the Merkzettel (not unique), entry ignored");
					continue;
				}
				e.channel = entries.get(0).channel;
				Log.out(true,
						"The entry with the empty channel name containig the title \""
								+ e.title + "\" is assigned to: " + e.channel);
				try {
					e.add();
				} catch (ErrorClass e1) {
					Log.out(e1.getErrorString() + " Entry ignored");
				}
			} else {
				Log.out("Empty channelname can't assigned. Entry ignored");
			}
		}
	}

	public long timeToLong(String time) throws ParseException {
		// Workaround in case of a wrong time zone of the TVInfo output
		// must be checked on summer time
		Date d = new Date(dateFormat.parse(time).getTime()); // + 60 *60 * 1000)
																// ;
		// System.out.println(d.toString()) ;
		return d.getTime();
	}

	public long htmlTimeToLong(String date, String time) throws ParseException {
		int year = new GregorianCalendar(this.timeZone).get(Calendar.YEAR);
		long now = new Date().getTime();
		long d1 = htmlDateFormat.parse(
				date + Integer.toString(year) + " " + time).getTime();

		if (d1 > now)
			return d1;
		else
			return new Date(htmlDateFormat.parse(
					date + Integer.toString(year + 1) + " " + time).getTime())
					.getTime();
	}

	public class System_EditSenderParserCallback extends
			HTMLEditorKit.ParserCallback {
		private boolean isAllSenderRead = false;
		private boolean isOK = true;

		private boolean isH1 = false;
		private boolean isSenderAuswahl = false;
		private boolean isTitle = false;
		private boolean isChecked = false;

		public boolean isOK() {
			return this.isOK;
		};

		public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
			if (this.isAllSenderRead)
				return;
			if (t == HTML.Tag.H1)
				this.isH1 = true;
			else if (t == HTML.Tag.TITLE)
				this.isTitle = true;
		}

		public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
			if (!this.isSenderAuswahl)
				return;
			if (t == HTML.Tag.IMG) {
				String channelName = (String) a
						.getAttribute(HTML.Attribute.ALT);
				Channel c = createChannel(channelName, "");
				allSender.add(c);
				if (this.isChecked)
					userSender.add(channelName);
			}
			if (t == HTML.Tag.INPUT) {
				if (a.containsAttribute(HTML.Attribute.VALUE,
						"Änderungen speichern")) {
					this.isSenderAuswahl = false;
					this.isAllSenderRead = true;
				} else if (this.isSenderAuswahl
						&& a.containsAttribute(HTML.Attribute.TYPE, "checkbox")) {
					if (a.isDefined(HTML.Attribute.CHECKED))
						this.isChecked = true;
					else
						this.isChecked = false;
				}
			}
		}

		public void handleEndTag(HTML.Tag t, int pos) {
			if (this.isAllSenderRead)
				return;
			if (t == HTML.Tag.H1)
				this.isH1 = false;
			else if (t == HTML.Tag.TITLE)
				this.isTitle = false;
		}

		public void handleText(char[] data, int pos) {
			if (isTitle) {
				String title = new String(data);
				if (!title.contains("Sender konfigurieren"))
					this.isOK = false;
			}
			if (this.isH1) {
				if (new String(data).equals("Senderauswahl"))
					this.isSenderAuswahl = true;
			}
		}
	}

	public void readSystem_EditSenderPage() {
		this.userSender = new HashSet<String>();
		this.allSender = new ArrayList<Channel>();

		String md5 = this.getMD5();

		String completeURL = TVInfo.senderURL + "?user=" + this.username
				+ "&pass=" + this.getMD5();

		InputStream stream = null;

		try {
			stream = Html.getStream(completeURL, "TVInfo Sender/Bearbeiten",
					"tvusername=" + this.username + "; tvuserpass=" + md5);
		} catch (ErrorClass e) {
			Log.out(e.getErrorString());
			return;
		}

		System_EditSenderParserCallback myCallBack = new System_EditSenderParserCallback();

		try {
			new ParserDelegator().parse(new InputStreamReader(stream),
					myCallBack, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!myCallBack.isOK()) {
			this.allSender = null;
			Log.out("TVInfo pages are modified. Get a new version if available");

		}
	}

	@Override
	public int importChannels(boolean check) {
		if (check)
			return 0;

		return this.assignChannels();
	}

	@Override
	public boolean isAllChannelsImport() {
		return true;
	};

	@Override
	protected ArrayList<Channel> readChannels() {
		if (this.allSender == null)
			this.readSystem_EditSenderPage();
		return this.allSender;
	};

	@Override
	public boolean containsChannel(final Channel channel, boolean ifList) {
		if (ifList)
			return true;

		if (this.userSender == null)
			this.readSystem_EditSenderPage();
		return this.userSender.contains(channel.getName());
	}

	@Override
	public boolean isChannelMapAvailable() {
		return true;
	}
}
