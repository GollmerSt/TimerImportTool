// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.main.Versions;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.Registry;
import dvbviewertimerimport.misc.ResourceManager;

public class TVGenial extends Provider {

	private static final String NAME_PLUGIN_PATH = "DVBViewer";
	private static final String NAME_CHANNEL_FILE = "tvuid.txt";
	private static final String PATH_SCRIPT_FILE = "TVGenial/DVBViewer.txt";
	private static final String PATH_LOGO_FILE = "TVGenial/Logo.png";
	private static final String PATH_RECORDER_FILE = "TVGenial/recorder.ini";
	private static final String PATH_SETUP_FILE = "TVGenial/Setup.ini";
	private static final String[] REG_ROOT = { "HKEY_CURRENT_USER\\Software\\ARAKON-Systems\\TVgenial",
			"HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\ARAKON-Systems\\TVgenial" };
	private static final String REG_ROOT5 = "5";

	private final SimpleDateFormat dateFormat;

	private static String getRegKey(String key) {
		String installDir = null;
		for (String path : REG_ROOT) {
			installDir = Registry.getValue(path, "InstallDir");
			if (installDir == null) {
				installDir = Registry.getValue(path + REG_ROOT5, key);
			}
			if (installDir != null) {
				return installDir;
			}
		}
		return null;
	}

	public TVGenial(Control control) {
		super(control, false, false, "TVGenial", false, false, false, true, false, false);
		this.timeZone = TimeZone.getTimeZone("Europe/Berlin");
		this.dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		this.dateFormat.setTimeZone(this.timeZone);
		this.canAddChannel = false;
		this.canImport = true;
		this.canModify = true;

		String installDir = getRegKey("InstallDir");

		this.isFunctional = null != installDir;
	}

	@Override
	public boolean install() {
		String programDir = getRegKey("InstallDir");

		if (programDir == null) {
			Log.out("Registry entry of TVGenial not found. \nIt seems to be that installation of TVGenial is failed.");
			return false;
		}

		File file = new File(programDir + File.separator + "Interfaces" + File.separator + TVGenial.NAME_PLUGIN_PATH);
		file.mkdir();

		String jarFile = this.control.getDVBViewer().getExePath() + File.separator
				+ this.control.getDVBViewer().getExeName();

		ArrayList<String[]> keyList = new ArrayList<String[]>();

		String javaHome = System.getProperty("java.home");

		String[] stringSet = new String[] { "%JAR_File%", jarFile };
		keyList.add(stringSet);

		stringSet = new String[] { "%JAVA_Home%", javaHome };
		keyList.add(stringSet);

		stringSet = new String[] { "%PLUGIN_version%", Versions.getVersion() };
		keyList.add(stringSet);

		ResourceManager.copyFile(file.getPath(), PATH_SCRIPT_FILE, keyList, true);
		ResourceManager.copyBinaryFile(file.getPath(), PATH_LOGO_FILE);
		ResourceManager.copyFile(file.getPath(), PATH_RECORDER_FILE, keyList, true);
		ResourceManager.copyFile(file.getPath(), PATH_SETUP_FILE);

		return true;
	}

	@Override
	public boolean uninstall() {
		String programDir = getRegKey("InstallDir");

		if (programDir == null) {
			Log.out("Registry entry of TVGenial not found. \nIt seems to be that installation of TVGenial is failed.");
			return false;
		}

		File dir = new File(programDir + File.separator + "Interfaces" + File.separator + TVGenial.NAME_PLUGIN_PATH);

		String[] files = { PATH_SCRIPT_FILE, PATH_LOGO_FILE, PATH_RECORDER_FILE, PATH_SETUP_FILE };

		for (String fs : files) {
			File f = new File(dir.getPath() + File.separator + fs.split("/")[1]);
			f.delete();
		}
		dir.delete();

		return true;
	}

	@Override
	protected Collection<Channel> readChannels() {
		String dataPath = getRegKey("PublicDataRoot");
		if (dataPath == null)
			return null;

		Map<Long, Channel> map = new HashMap<>();

		File f = new File(dataPath + File.separator + NAME_CHANNEL_FILE);

		if (!f.canRead())
			throw new ErrorClass("File \"" + f.getAbsolutePath() + "\" not found");

		FileReader fr;
		try {
			fr = new FileReader(f);
		} catch (FileNotFoundException e) {
			return null;
		}

		BufferedReader br = new BufferedReader(fr);

		String line;

		try {
			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (line.substring(0, 2).equals("//"))
					continue;
				String[] parts = line.split("\\|");

				if (parts.length < 2)
					continue;
				long tvuid = Long.valueOf(parts[0]);
				String cName = parts[2];

				Channel c = this.createChannel(cName, null, Long.toString(tvuid), false);
				Channel former = map.get(tvuid);
				if (former != null) {
					if (cName.length() > former.getName().length()) {
						map.put(tvuid, c);
					}
				} else {
					map.put(tvuid, c);
				}
			}
			br.close();
		} catch (IOException e) {
			return null;
		}
		return map.values();
	}

	private String getParaInfo() {
		return ", necessary parameters:\n   -TVGenial TVUID=ccc Beginn=yyyyMMddHHmm Dauer=nnn Sendung=cccccc";
	}

	@Override
	public boolean processEntry(Object args, DVBViewer.Command command) {
		long tvuid = -1;
		String startTime = null;
		long milliSeconds = -1;
		String title = null;

		boolean mustDelete = false;
		boolean selectChannel = false;

		for (String p : (String[]) args) {
			int pos = p.indexOf('=');
			if (pos < 0) {
				if (p.trim().equalsIgnoreCase("-delete")) {
					mustDelete = true;
				} else if (p.trim().equalsIgnoreCase("-remind")) {
					selectChannel = true;
				}
				continue;
			}
			String key = p.substring(0, pos).trim();
			String value = p.substring(pos + 1).trim();

			if (key.equalsIgnoreCase("TVUID")) {
				if (!value.matches("\\d+")) {
					String errorString = this.getParaInfo();
					throw new ErrorClass("Invalid parameter TVUID" + errorString);
				}
				tvuid = Long.valueOf(value);
			} else if (key.equalsIgnoreCase("Beginn"))
				startTime = value;
			else if (key.equalsIgnoreCase("Dauer")) {
				if (!value.matches("\\d+"))
					throw new ErrorClass("Undefined value of parameter \"Dauer\".");
				milliSeconds = Long.valueOf(value) * 60000;
			} else if (key.equalsIgnoreCase("Sendung"))
				title = value;
		}
		if (tvuid < 0 || !selectChannel && (startTime == null || milliSeconds < 0 || title == null)) {
			String errorString = this.getParaInfo();
			throw new ErrorClass("Missing parameter" + errorString);
		}

		String id = Long.toString(tvuid);

		if (!selectChannel) {
			long start = 0;
			try {
				start = timeToLong(startTime);
			} catch (ParseException e) {
				String errorString = this.getParaInfo();
				throw new ErrorClass(e, "Syntax error in the parameter \"Begin\"" + errorString);
			}
			long end = start + milliSeconds;

			if (mustDelete)
				this.control.getDVBViewer().deleteEntry(this, id, start, end, title);
			else
				this.control.getDVBViewer().addNewEntry(this, null, id, start, end, title);
		} else {
			this.control.getDVBViewer().selectChannel(this, id);			
		}
		return true;
	}

	private long timeToLong(String time) throws ParseException {
		Date d = new Date(this.dateFormat.parse(time).getTime());
		// System.out.println(d.toString()) ;
		return d.getTime();
	}

	@Override
	public boolean isAllChannelsImport() {
		return true;
	}

	@Override
	public boolean isChannelMapAvailable() {
		return false;
	}
}