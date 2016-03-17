// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.misc.*;
import dvbviewertimerimport.provider.Provider;

public class ClickFinder extends Provider {

	private static final String[] reg_pathes = new String[] {
			"HKCU\\Software\\Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\EWE\\TVGhost",
			"HKCU\\Software\\Classes\\VirtualStore\\MACHINE\\SOFTWARE\\EWE\\TVGhost",
			"HKLM\\SOFTWARE\\Wow6432Node\\EWE\\TVGhost",
			"HKLM\\SOFTWARE\\EWE\\TVGhost" };

	private DVBViewer dvbViewer = null;
	private final SimpleDateFormat dateFormat;

	public ClickFinder(Control control) {
		super(control, false, false, "ClickFinder", false, false, false, true,
				false, false);
		this.canImport = true;
		this.canModify = false;
		this.canAddChannel = false;
		this.dvbViewer = control.getDVBViewer();
		this.timeZone = TimeZone.getTimeZone("Europe/Berlin");
		this.dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		this.dateFormat.setTimeZone(timeZone);
		this.isFunctional = this.getDbasePath() != null ;
	}

	private String getParaInfo() {
		return ", necessary parameters:\n   -ClickFinder [-path dataPath] Sender=ccc Begin=yyyyMMddHHmm Dauer=nnn Sendung=cccccc";
	}

	@Override
	public boolean processEntry(Object args, DVBViewer.Command command) {
		String channel = null;
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
				channel = value;
			else if (key.equalsIgnoreCase("Pos"))
				providerID = value;
			else if (key.equalsIgnoreCase("Beginn"))
				startTime = value;
			else if (key.equalsIgnoreCase("Dauer")) {
				if (!value.matches("\\d+"))
					throw new ErrorClass(
							"Undefined value of parameter \"Dauer\".");
				milliSeconds = Long.valueOf(value) * 60000;
			} else if (key.equalsIgnoreCase("Sendung"))
				title = value;
		}
		if (channel == null || startTime == null || milliSeconds < 0
				|| title == null) {
			String errorString = this.getParaInfo();
			throw new ErrorClass("Missing parameter" + errorString);
		}
		long start = 0;
		try {
			start = timeToLong(startTime);
		} catch (ParseException e) {
			String errorString = this.getParaInfo();
			throw new ErrorClass(e, "Syntax error in the parameter \"Begin\""
					+ errorString);
		}
		long end = start + milliSeconds;
		this.dvbViewer
				.addNewEntry(this, providerID, channel, start, end, title);

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
				Registry.setValue(path + "\\TVGhost", "REG_SZ", "AddOns",
						regContents);
			}
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ",
					"AddOnName", "DVBViewer");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_DWORD",
					"EinbindungsModus", "2");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ",
					"KurzBeschreibung", "DVBViewer programmieren");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ",
					"LangBeschreibung",
					"Übergeben Sie diese Sendung an den DVBViewer");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_DWORD",
					"ParameterZusatz", "2");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ",
					"SpezialButtonGrafikName", "AddOn");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ",
					"SpezialButtonToolTiptext", "SpezialButtonToolTiptext");
			
			String javaExe = System.getProperty("java.home");
			javaExe = "\"" + javaExe + "\\javaw.exe\"" ;

			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ",
					"ExeDateiname", javaExe );
//			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ",
//					"ExeDateiname", /*"\"javaw\"" ) ;*/"\"" + javaExe + "\"");
			Registry.setValue(path + "\\TVGhost\\AddOn_DVBViewer", "REG_SZ",
					"ParameterFest",
					"-jar \"\"\"" + this.dvbViewer.getExePath()
							+ File.separator + this.dvbViewer.getExeName()
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
			if (regContents.contains(","))
				temps = regContents.split(",DVBViewer");
			else
				temps = regContents.split("DVBViewer");
			regContents = "";
			for (int i = 0; i < temps.length; i++)
				regContents += temps[i];
			Registry.setValue(path + "\\TVGhost", "REG_SZ", "AddOns",
					regContents);

			Registry.delete(path + "\\TVGhost\\AddOn_DVBViewer", "");
		}
		return true;
	}

	private long timeToLong(String time) throws ParseException {
		Date d = new Date(dateFormat.parse(time).getTime());
		// System.out.println(d.toString()) ;
		return d.getTime();
	}

	@Override
	public boolean mustInstall() {
		return (Constants.IS_WINDOWS);
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

	public ArrayList<Channel> readChannels() {
		Database db = null;
		try {
			db = Database.open(new File(this.getDbasePath()), true);
		} catch (IOException e) {
			Log.out("Database not found");
			return null;
		}
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
		ArrayList<Channel> result = new ArrayList<Channel>();
		for (Map<String, Object> row : table) {
			String name = (String) row.get("SenderKennung");
			if (name == null) {
				continue;
			}
			Channel channel = this.createChannel(name, null);
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
	};

}
