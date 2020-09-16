// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer;

import java.io.File;
import java.util.ArrayList;

import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.misc.TerminateClass;

public class DVBViewerCOM {

	public static DVBViewerCOM getInstance() {
		return DVBViewerCOMHolder.INSTANCE;
	}

	private static class DVBViewerCOMHolder {

		private static final DVBViewerCOM INSTANCE = new DVBViewerCOM();
	}

	public static final String NAME_DVBVIEWER_COM_DLL = "DVBViewerTimerImport";

	/**
	 * 
	 * @return The version of the dll
	 */
	private static native String getVersion();

	/**
	 * 
	 * @param force DVBViewer will be started
	 * 
	 * @return true: Connection successfull
	 */
	private static native boolean connect(boolean force);

	/**
	 * Disconnecting from the DVBViewer
	 */
	private static native void disconnect();

	/**
	 * Get timer items from the DVBViewer
	 * 
	 * @return Timer items
	 */
	private static native DVBViewerEntryCOM[] getItems();

	/**
	 * Send timer items to the viewer
	 * 
	 * @param Timer items
	 */
	private static native void setItems(DVBViewerEntryCOM[] entries);

	/**
	 * Gets a value from the setup.xml of the DVBViewer.
	 * 
	 * @param section A string with the name of the section of the setup.xml.
	 * @param name    A string with the value's name.
	 * @param deflt   A string with the default value returned if the section/name
	 *                is not found.
	 * @return A string with the value
	 */
	private static native String getSetupValue(String section, String name, String deflt);

	private static native int setCurrentChannel(String channelID);

	private static native int getCurrentChannelNo();

	private static native void initLog();

	private boolean connect() {
		return DVBViewerCOM.connect(false);

	}

	public ArrayList<DVBViewerEntry> readTimers() {
		if (!this.dllLoaded || !this.connect())
			return null;
		DVBViewerEntryCOM[] items = DVBViewerCOM.getItems();
		DVBViewerCOM.disconnect();

		ArrayList<DVBViewerEntry> result = new ArrayList<DVBViewerEntry>();

		for (DVBViewerEntryCOM item : items) {
			result.add(item.createDVBViewerEntry());
		}
		return result;
	}

	public boolean setTimers(ArrayList<DVBViewerEntry> entries) {

		ArrayList<DVBViewerEntryCOM> items = new ArrayList<DVBViewerEntryCOM>();

		for (DVBViewerEntry entry : entries) {
			if (entry.mustDVBViewerDeleted())
				items.add(new DVBViewerEntryCOM(entry, true));
			if (entry.mustUpdated())
				items.add(new DVBViewerEntryCOM(entry, false));
			if (entry.mustDVBViewerCreated())
				items.add(new DVBViewerEntryCOM(entry, false));
		}
		DVBViewerEntryCOM[] array = (DVBViewerEntryCOM[]) items.toArray(new DVBViewerEntryCOM[0]);

		if (array.length == 0)
			return true;

		if (!this.dllLoaded || !this.connect()) {
			return false;
		}

		DVBViewerCOM.setItems(array);

		DVBViewerCOM.disconnect();
		return true;
	}

	private boolean dllLoaded = false;
	private boolean initialized = false;

	private DVBViewerCOM() {
	}

	/**
	 * The dll will be copied to the user directory and will be loades
	 * 
	 * @param dllPath	Path of the user directory
	 * @param mustCopy Copyying of the dll is forced
	 * 
	 * @return true if loading of the dll was successfull
	 */
	public boolean init(File dllPath, boolean mustCopy) {

		if (this.initialized) {
			return this.dllLoaded;
		}
		this.initialized = true;

		String osName = System.getProperty("os.name", "").toLowerCase();
		if (!osName.contains("windows")) {
			return false;
		}

		String jvmArch = System.getProperty("os.arch");

		if (jvmArch.equals("amd64")) {
			jvmArch = "x64";
		} else if (jvmArch.equals("x86")) {
			jvmArch = "x32";
		}

		String name = NAME_DVBVIEWER_COM_DLL + "_" + jvmArch + ".dll";

		File dll = new File(dllPath, name);

		if (!dll.canExecute() || mustCopy) {

			if (!dllPath.canWrite()) {
				Log.error(ResourceManager.msg("ADMINISTRATOR"));
				throw new TerminateClass(1);
			}
			ResourceManager.copyBinaryFile(dll, "datafiles/" + name);
		}

		try {
			System.load(dll.getAbsolutePath());
		} catch (Throwable t) {
			Log.out("Error on loading DLL, access to DVBViewer not possible: " + t.getMessage());
			return false;
		}

		this.dllLoaded = true ;
		
		DVBViewerCOM.initLog();

		Log.out("Loading of the DLL successful");
		return true;

	}
	
	public boolean isAvailable() {
		return this.dllLoaded;
	}

	public String getDLLVersion() {
		return this.dllLoaded ? DVBViewerCOM.getVersion() : null;
	}

	public boolean connectViewer(boolean force) {
		return this.dllLoaded ? DVBViewerCOM.connect(force) : false;
	}

	public void disconnectViewer() {
		if (this.dllLoaded) {
			DVBViewerCOM.disconnect();
		}
	}

	public int setViewerChannel(String channelID) {
		return this.dllLoaded ? DVBViewerCOM.setCurrentChannel(channelID) : -1;
	}

	public int getViewerChannel() {
		return this.dllLoaded ? DVBViewerCOM.getCurrentChannelNo() : -1;
	}

}
