// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import dvbviewertimerimport.dvbviewer.DVBViewerEntry.StatusTimer;
import dvbviewertimerimport.misc.Constants;
import dvbviewertimerimport.misc.Enums.ActionAfterItems;
import dvbviewertimerimport.misc.Enums.TimerActionItems;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Helper;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.WakeOnLan;
import dvbviewertimerimport.xml.StackXML;

public class DVBViewerService {

	private static GregorianCalendar calendar = null;
	private static TimeZone timeZone = null;
	private static SimpleDateFormat dayTimeFormat = null;

	private boolean enable;
	private String url;
	private String userName;
	private String password;
	private boolean enableWOL = false;
	private String broadCastAddress;
	private String macAddress;
	private int waitTimeAfterWOL;
	private String lastURL;
	private final StackXML<String> pathTimer = new StackXML<String>("Timers", "Timer");
	private final StackXML<String> pathChannel = new StackXML<String>("Timers", "Timer", "Channel");
	private final StackXML<String> pathID = new StackXML<String>("Timers", "Timer", "ID");
	private final StackXML<String> pathDescr = new StackXML<String>("Timers", "Timer", "Descr");
	private final StackXML<String> pathRecording = new StackXML<String>("Timers", "Timer", "Recording");
	private long version = -1;

	static {
		timeZone = DVBViewer.getTimeZone();
		calendar = new GregorianCalendar();
		calendar.setTimeZone(timeZone);
		dayTimeFormat = new SimpleDateFormat("dd.MM.yyyyHH:mm");
		dayTimeFormat.setTimeZone(timeZone);
	}

	public DVBViewerService(boolean enable, String url, String name, String password) {
		this.enable = enable;
		this.url = url;
		this.userName = name;
		this.password = password;
		Authenticator.setDefault(new DVBViewerService.MyAuthenticator());

		// this.version = this.readVersion() ;
	}

	private class MyAuthenticator extends Authenticator {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			// System.out.println( "Hier erfolgt die Authentifizierung" ) ;
			// System.out.printf( "url=%s, host=%s, ip=%s, port=%s%n",
			// getRequestingURL(), getRequestingHost(),
			// getRequestingSite(), getRequestingPort() );

			return new PasswordAuthentication(DVBViewerService.this.userName,
					DVBViewerService.this.password.toCharArray());
		}
	}

	public boolean getEnableWOL() {
		return this.enableWOL;
	};

	public void setEnableWOL(boolean e) {
		this.enableWOL = e;
	};

	public String getBroadCastAddress() {
		return this.broadCastAddress;
	};

	public void setBroadCastAddress(String b) {
		this.broadCastAddress = b;
	};

	public String getMacAddress() {
		return this.macAddress;
	};

	public void setMacAddress(String m) {
		this.macAddress = m;
	};

	public int getWaitTimeAfterWOL() {
		return this.waitTimeAfterWOL;
	};

	public void setWaitTimeAfterWOL(int w) {
		this.waitTimeAfterWOL = w;
	};

	private InputStream connect(String command, String query) {
		return this.connect(command, query, false);
	}

	private InputStream connect(String command, String query, boolean check) {
		String completeURL = "http://" + this.url;
		completeURL += "/API/" + command + ".html";
		if (query.length() != 0)
			completeURL += "?" + query;

		this.lastURL = completeURL;
		Log.out(true, completeURL);

		URL dvbViewerServiceURL = null;
		try {
			dvbViewerServiceURL = new URL(completeURL);
		} catch (MalformedURLException e2) {
			throw new ErrorClass(e2,
					"Error on building the URL of the service, check the format of the service-ip address.");
		}
		InputStream input = null;
		int repeat = 0;
		while (true) {
			try {
				HttpURLConnection conn = (HttpURLConnection) dvbViewerServiceURL.openConnection();
				if (check) {
					conn.setUseCaches(false); // Cachen ausschalten
				}
//				if (this.userName.length() > 0 && this.password.length() > 0) {
//					// Daten für HTTP-Authentifizierung festlegen
//					conn.setRequestProperty("Authorization",
//							"Basic " + Base64.encodeBytes(new String(this.userName + ":" + this.password).getBytes()));
//					conn.setRequestProperty("Authorization", "Basic " + Base64.
//							.encodeToString(new String(this.userName + ":" + this.password).getBytes()));
//				}
				input = conn.getInputStream();
			} catch (ProtocolException e1) {
				throw new ErrorClass(
						"Authenticator error on access to the DVBViewerService. Username/password should be checked.");
			} catch (IOException e) {
				if (this.enableWOL && repeat < 1) {
					if (repeat == 0) {
						WakeOnLan.execute(this.broadCastAddress, this.macAddress);
						try {
							Thread.sleep(this.waitTimeAfterWOL * 1000);
						} catch (InterruptedException e1) {
						}
					}
					repeat++;
					continue;
				}
				throw new ErrorClass(e,
						"DVBViewerService not responding, DVBViewerService not alive or URL not correct?");
			}
			break;
		}
		return input;
	}

	private InputStream connect(String command) {
		return this.connect(command, "", false);
	};

	private InputStream connect(String command, boolean check) {
		return this.connect(command, "", check);
	};

	public long getVersion() {
		if (this.version < 0)
			this.version = this.readVersion();
		return this.version;
	}

	private long readVersion() {
		return this.readVersion(false);
	};

	public long readVersion(boolean check) {
		InputStream input = connect("version", check);

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();

		String version = null;
		try {
			XMLEventReader reader = inputFactory.createXMLEventReader(input);
			String actKey = "";
			while (reader.hasNext()) {
				XMLEvent ev = reader.nextEvent();
				if (ev.isStartElement())
					actKey = ev.asStartElement().getName().getLocalPart();
				if (ev.isCharacters())
					if (actKey.equals("version"))
						version = ev.asCharacters().getData();
			}
			reader.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass(e,
					"Error on reading the Service data." + " Position: Line = "
							+ Integer.toString(e.getLocation().getLineNumber()) + ", column = "
							+ Integer.toString(e.getLocation().getColumnNumber()));
		}
		Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)");
		Matcher matcher = pattern.matcher(version);

		if (matcher.find())
			version = matcher.group(0);
		String splitted[] = version.split("\\.");
		long result = 0;
		for (int i = 0; i < splitted.length - 1; i++)
			result = result * 100 + Long.valueOf(splitted[i]);
		result = result * 1000 + Long.valueOf(splitted[splitted.length - 1]);

		return result;
	}

	public void setTimerEntry(DVBViewerEntry e, boolean toDelete) {
		e.prepareTimerSetting();
		String query = "";
		try {
			query = "ch=" + URLEncoder.encode(e.getChannel(), "UTF-8");
			query += "&dor=" + longToDate(e.getStart());
			query += "&encoding=255";
			query += "&start=" + longToMinutes(e.getStart());
			query += "&stop=" + longToMinutes(e.getEnd());
			if (!e.getDays().equals("-------"))
				query += "&days=" + e.getDays();
			String title = e.getTitle();
			if (e.getActionAfter() != ActionAfterItems.DEFAULT)
				query += "&endact=" + e.getActionAfter().getServiceID();
			if (e.getTimerAction() != TimerActionItems.DEFAULT)
				query += "&action=" + e.getTimerAction().getServiceID();
			if (this.version <= 10500077)
				title = Helper.replaceDiacritical(title);

			query += "&title=" + URLEncoder.encode(title, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			throw new ErrorClass(e1, "Error on creating the DVBViewerService URL");
		}

		String command = null;

		if (e.isEnabled())
			query += "&enable=1";
		else
			query += "&enable=0";

		if (toDelete) {
			command = "timerdelete";
			query += "&id=" + Long.toString(e.getDVBViewerID());
			if (e.getDVBViewerID() < 0) {
				Log.out("Unexpected serviceID on deleting an entry. Query: " + query);
				return;
			}
		} else {
			if (e.mustUpdated() && !toDelete) {
				command = "timeredit";
				query += "&id=" + Long.toString(e.getDVBViewerID());
				if (e.getDVBViewerID() < 0) {
					Log.out("Unexpected serviceID on editing an entry. Query: " + query);
					return;
				}
			}
			if (e.mustDVBViewerCreated() && !toDelete)
				command = "timeradd";
		}

		InputStream input = connect(command, query);

		BufferedReader b = new BufferedReader(new InputStreamReader(input));
		try {
			String line = b.readLine();
			if (line != null) {
				Log.out("Unexpected response on access to URL \"" + this.lastURL + "\": ");
				ErrorClass.setWarníng();
				{
					Log.out(line);
				}
				while ((line = b.readLine()) != null)
					;
			}
		} catch (IOException e1) {
			throw new ErrorClass(e1, "Unexpected error on acces to the DVBViewerService");
		}
	};

	public void setTimers(ArrayList<DVBViewerEntry> entries) {
		for (DVBViewerEntry d : entries)
			if (d.mustDVBViewerDeleted())
				this.setTimerEntry(d, true);

		for (DVBViewerEntry d : entries)
			if (d.mustDVBViewerCreated() || d.mustUpdated())
				this.setTimerEntry(d, false);
	}

	public ArrayList<DVBViewerEntry> readTimers() {
		this.getVersion();

		ArrayList<DVBViewerEntry> result = new ArrayList<DVBViewerEntry>();
		InputStream iS = connect("timerlist");
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader reader = inputFactory.createXMLEventReader(iS);
			StackXML<String> stack = new StackXML<String>();

			boolean enable = true;
			boolean recording = false;
			String channel = null;
			String dateString = null;
			String startString = null;
			String endString = null;
			String days = "-------";
			String title = null;
			ActionAfterItems actionAfter = ActionAfterItems.NONE;
			TimerActionItems timerAction = TimerActionItems.RECORD;
			long id = -1;

			while (reader.hasNext()) {
				XMLEvent ev = reader.nextEvent();
				if (ev.isStartElement()) {
					int type = -1;
					stack.push(ev.asStartElement().getName().getLocalPart());
					if (stack.equals(this.pathTimer)) {
						enable = true;
						recording = false;
						channel = null;
						dateString = null;
						startString = null;
						endString = null;
						days = "-------";
						actionAfter = ActionAfterItems.NONE;
						title = "";
						id = -1;
						type = 1;
					} else if (stack.equals(this.pathChannel))
						type = 2;

					@SuppressWarnings("unchecked")
					Iterator<Attribute> iter = ev.asStartElement().getAttributes();
					while (iter.hasNext()) {
						Attribute a = iter.next();
						String attributeName = a.getName().getLocalPart();
						String value = a.getValue();

						switch (type) {
							case 1:
								if (attributeName.equals("Enabled")) {
									if (value.equals("0"))
										enable = false;
									else if (!value.equals("-1"))
										throw new ErrorClass(ev,
												"Format error: Unexpected enable bit format from service");
								} else if (attributeName.equals("Date"))
									dateString = value;
								else if (attributeName.equals("Start"))
									startString = value;
								else if (attributeName.equals("End"))
									endString = value;
								else if (attributeName.equals("Days"))
									days = value;
								else if (attributeName.equals("ShutDown"))
									actionAfter = ActionAfterItems.get(Integer.valueOf(value));
								else if (attributeName.equals("Action"))
									timerAction = TimerActionItems.get(Integer.valueOf(value));
								else if (attributeName.equals("Day")) {
									if (!value.equals("-------"))
										enable = false; // ignore periodic timer
														// entry
								}
								break;

							case 2:
								if (attributeName.equals("ID")) {
									channel = DVBViewer.reworkChannelID(value);
								}
								break;
							default:
								break;
						}
					}
				}
				if (ev.isCharacters()) {
					String value = ev.asCharacters().getData();
					if (stack.equals(this.pathID)) {
						if (!value.matches("\\d+"))
							throw new ErrorClass(ev, "Format error: Unexpected ID format from service");
						id = Long.valueOf(value);
					} else if (stack.equals(this.pathDescr)) {
						title += value;
						// System.out.println(title) ;
					} else if (stack.equals(this.pathRecording)) {
						if (value.equals("-1"))
							recording = true;
						else if (!value.equals("0"))
							throw new ErrorClass(ev, "Format error: Unexpected recording bit format from service");
					}
				}
				if (ev.isEndElement()) {
					if (stack.equals(this.pathTimer)) {
						if (id < 0 || channel == null || dateString == null || startString == null || endString == null
								|| title == null)
							throw new ErrorClass(ev, "Incomplete timer entry from service");
						long start;
						long end;
						try {
							start = timeToLong(startString, dateString);
							end = timeToLong(endString, dateString);
						} catch (ParseException e) {
							throw new ErrorClass(ev, "Format error: Unexpected time format from service");
						}
						if (start > end)
							end += Constants.DAYMILLSEC;
						StatusTimer status = StatusTimer.ENABLED;
						if (recording)
							status = StatusTimer.RECORDING;
						else if (!enable)
							status = StatusTimer.DISABLED;
						DVBViewerEntry entry = new DVBViewerEntry(status, id, channel, start, end, days, title,
								timerAction, actionAfter);
						result.add(entry);
					}
					stack.pop();
				}
			}
			reader.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass(e,
					"Error on reading the Service data." + " Position: Line = "
							+ Integer.toString(e.getLocation().getLineNumber()) + ", column = "
							+ Integer.toString(e.getLocation().getColumnNumber()));
		}
		return result;

		/*
		 * <?xml version="1.0" encoding="iso-8859-1"?> <Timers> <Timer Type="1"
		 * Enabled="0" Priority="50" Date="05.07.2999" Start="23:39:00" End="00:09:00"
		 * Days="TT-----" Action="0"> <Descr>Bayerisches FS Süd (deu)</Descr> <Options
		 * AdjustPAT="-1" AllAudio="-1" DVBSubs="-1" Teletext="-1"/> <Format>2</Format>
		 * <Folder>Auto</Folder> <NameScheme>%event_%date_%time</NameScheme> <Log
		 * Enabled="-1" Extended="0"/> <Channel
		 * ID="550137291|Bayerisches FS Süd (deu)"/> <Executeable>0</Executeable>
		 * <Recording>0</Recording> <ID>0</ID> </Timer> <Timer Type="1" Enabled="-1"
		 * Priority="50" Date="05.07.2009" Start="18:35:00" End="19:50:00" Action="0">
		 * <Descr>Lindenstrasse</Descr> <Options AdjustPAT="-1"/> <Format>2</Format>
		 * <Folder>Auto</Folder> <NameScheme>%event_%date_%time</NameScheme> <Log
		 * Enabled="-1" Extended="0"/> <Channel ID="543583690|Das Erste (deu)"/>
		 * <Executeable>-1</Executeable> <Recording>-1</Recording> <ID>1</ID>
		 * <Recordstat StartTime="05.07.2009 18:40:33">G:\Neue
		 * Aufnahmen\Lindenstrasse_07-05_18-40-33.ts</Recordstat> </Timer> </Timers>
		 */
	}

	public boolean isEnabled() {
		return this.enable;
	};

	public void setEnabled(boolean e) {
		this.enable = e;
	};

	public String getURL() {
		return this.url;
	};

	public String getUserName() {
		return this.userName;
	};

	public String getPassword() {
		return this.password;
	};

	public void setURL(String url) {
		this.url = url;
	};

	public void setUserName(String userName) {
		this.userName = userName;
	};

	public void setPassword(String password) {
		this.password = password;
	};

	private static void checkAndUpdateTimeZone() {
		if (timeZone != DVBViewer.getTimeZone()) {
			timeZone = DVBViewer.getTimeZone();
			calendar.setTimeZone(timeZone);
			dayTimeFormat.setTimeZone(timeZone);
		}
	}

	public static String longToDate(long d) {
		checkAndUpdateTimeZone();
		long t = d + (long) timeZone.getOffset(d);
		// System.out.println(t%(1000*60*60*24) ) ;
		return Long.toString(t / 1000 / 60 / 60 / 24 + 25569);
	}

	public static String longToMinutes(long d) {
		checkAndUpdateTimeZone();
		calendar.setTime(new Date(d));
		int minutes = 60 * calendar.get(java.util.Calendar.HOUR_OF_DAY) + calendar.get(java.util.Calendar.MINUTE);
		return Integer.toString(minutes);
	}

	public static long timeToLong(String time, String date) throws ParseException {
		checkAndUpdateTimeZone();
		return dayTimeFormat.parse(date + time).getTime();
	}
}
