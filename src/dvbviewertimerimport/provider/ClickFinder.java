// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;

import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.misc.Constants;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.Registry;

public class ClickFinder extends Provider {

	private static final String[] reg_pathes = new String[] {
			"HKCU\\Software\\Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\EWE\\TVGhost",
			"HKCU\\Software\\Classes\\VirtualStore\\MACHINE\\SOFTWARE\\EWE\\TVGhost",
			"HKLM\\SOFTWARE\\Wow6432Node\\EWE\\TVGhost", "HKLM\\SOFTWARE\\EWE\\TVGhost" };

	private static final boolean INDIVIDUAL_CHANNELLIST_AVAILABLE = true;

	private DVBViewer dvbViewer = null;
	private final SimpleDateFormat dateFormat;

	public ClickFinder(Control control) {
		super(control, false, false, "ClickFinder", false, false, false, true, false, false);
		this.canImport = true;
		this.canModify = true;
		this.canAddChannel = false;
		this.dvbViewer = control.getDVBViewer();
		this.timeZone = TimeZone.getTimeZone("Europe/Berlin");
		this.dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		this.dateFormat.setTimeZone(this.timeZone);
		this.isFunctional = this.getDbasePath() != null;
	}

	private String getParaInfo() {
		return ", necessary parameters:\n   -ClickFinder [-path dataPath] Sender=ccc Begin=yyyyMMddHHmm Dauer=nnn Sendung=cccccc";
	}

	@Override
	public boolean processEntry(Object args, DVBViewer.Command command) {
		String id = null;
		String providerID = null;
		String startTime = null;
		long milliSeconds = -1;
		String title = null;

		for (String p : (String[]) args) {
			int pos = p.indexOf('=');
			if (pos < 0)
				continue;
			String key = p.substring(0, pos).trim();
			String value = p.substring(pos + 1).trim();

			if (key.equalsIgnoreCase("Sender"))
				id = value;
			else if (key.equalsIgnoreCase("Pos"))
				providerID = value;
			else if (key.equalsIgnoreCase("Beginn"))
				startTime = value;
			else if (key.equalsIgnoreCase("Dauer")) {
				if (!value.matches("\\d+"))
					throw new ErrorClass("Undefined value of parameter \"Dauer\".");
				milliSeconds = Long.valueOf(value) * 60000;
			} else if (key.equalsIgnoreCase("Sendung"))
				title = value;
		}
		if (id == null || startTime == null || milliSeconds < 0 || title == null) {
			String errorString = this.getParaInfo();
			throw new ErrorClass("Missing parameter" + errorString);
		}
		long start = 0;
		try {
			start = timeToLong(startTime);
		} catch (ParseException e) {
			String errorString = this.getParaInfo();
			throw new ErrorClass(e, "Syntax error in the parameter \"Begin\"" + errorString);
		}
		long end = start + milliSeconds;

		String channel = null;

		for (ChannelSet cs : this.control.getChannelSets()) {
			Channel c = cs.getChannel(this.getID());
			if (c == null)
				continue;
			if (id.contentEquals((String) c.getIDKey())) {
				channel = c.getName();
				break;
			}
		}

		this.dvbViewer.addNewEntry(this, providerID, channel, start, end, title);

		return true;
	}

	private String getDbasePath() {
		String dbPath = null;
		for (String path : reg_pathes) {
			String rPath = path + "\\Gemeinsames";
			dbPath = Registry.getValue(rPath, "DBDatei");
			if (dbPath != null) {
				break;
			}
		}
		return dbPath;
	}

	@Override
	public boolean install() // boolean setDataDir )
	{
		boolean successfull = false;
		if (!Constants.IS_WINDOWS)
			return false;
		// if ( setDataDir )
		// dataPathPara = " -path \"\"\"" + this.dvbViewer.getDataPath() +
		// "\"\"\"";
		for (String path : reg_pathes) {
			String rPath = path + "\\Gemeinsames";
			String regContents = Registry.getValue(rPath, "DBDatei");
			if (regContents == null) {
				continue;
			}
			Log.out("Registry entry found at " + rPath);
			successfull = true;
			regContents = Registry.getValue(path + "\\TVGhost", "AddOns");
			if (regContents == null || !regContents.contains("DVBViewer")) {
				if (regContents != null && regContents.length() != 0)
					regContents += ",DVBViewer";
				else
					regContents = "DVBViewer";
				Registry.setValue(path + "\\TVGhost", "REG_SZ", "AddOns", regContents);
			}
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "AddOnName", "DVBViewer");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_DWORD", "EinbindungsModus", "2");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "KurzBeschreibung",
					"DVBViewer programmieren");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "LangBeschreibung",
					"Übergeben Sie diese Sendung an den DVBViewer");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_DWORD", "ParameterZusatz", "2");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "SpezialButtonGrafikName", "AddOn");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "SpezialButtonToolTiptext",
					"SpezialButtonToolTiptext");

			String javaExe = System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw";

			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "ExeDateiname", javaExe);
//			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ",
//					"ExeDateiname", /*"\"javaw\"" ) ;*/"\"" + javaExe + "\"");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "ParameterFest",
					"-jar \"\"\"" + this.dvbViewer.getExePath() + File.separator + this.dvbViewer.getExeName()
							+ "\"\"\" -ClickFinder ");
		}
		return successfull;
	}

	@Override
	public boolean uninstall() {
		for (String path : reg_pathes) {
			String rPath = path + "\\TVGhost";
			String regContents = Registry.getValue(rPath, "AddOns");

			if (regContents == null || !regContents.contains("DVBViewer")) {
				continue;
			}
			Log.out("Registry entry found at " + rPath);

			String[] temps;
			temps = regContents.split(",");
			regContents = "5";
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for (String addOn : temps) {
				if (!addOn.trim().equals("DVBViewer")) {
					if (!first) {
						builder.append(',');
					} else {
						first = false;
					}
					builder.append(addOn);
				}
			}
			Registry.delete(path + "\\TVGhost", "AddOns");
			Registry.setValue(path + "\\TVGhost", "REG_SZ", "AddOns", builder.toString());

			Registry.delete(path + "\\TVGhost\\AddOn_DVBViewer", "");
		}
		return true;
	}

	private long timeToLong(String time) throws ParseException {
		Date d = new Date(this.dateFormat.parse(time).getTime());
		// System.out.println(d.toString()) ;
		return d.getTime();
	}

	@Override
	public boolean mustInstall() {
		return (Constants.IS_WINDOWS);
	}

	@Override
	public boolean isAllChannelsImport() {
		return true;
	}

	@Override
	public Collection<Channel> readChannels() {
		
		Database db = null;

		// open data base
		try {
			db = DatabaseBuilder.open(new File(this.getDbasePath()));
		} catch (IOException e) {
			Log.out("Database not found");
			return null;
		}

		// Read channel table (complete list of channels)
		Table table = null;
		try {
			table = db.getTable("Sender");
		} catch (IOException e) {
			Log.out("Table \"Sender\" not found");
			try {
				db.close();
			} catch (IOException e1) {
			}
			return null;
		}
		Collection<Channel> result = new ArrayList<>();
		for (Map<String, Object> row : table) {
			String name = (String) row.get("SenderKennung");
			if (name == null) {
				continue;
			}
			boolean favorit = (boolean) row.get("Favorit");
			String bezeichnung = (String) row.get("Bezeichnung");
			Channel channel = this.createChannel(bezeichnung, null, name, favorit);
			result.add(channel);
		}

		try {
			db.close();
		} catch (IOException e) {
		}
		if (result.isEmpty()) {
			Log.out("Column or entry of \"SenderKennung\" not found");
			return null;
		}

		return result;
	}


	@Override
	public boolean isChannelMapAvailable() {
//		return true;
		return INDIVIDUAL_CHANNELLIST_AVAILABLE;
	}
}
