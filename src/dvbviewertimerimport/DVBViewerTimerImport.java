// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.dvbviewer.DVBViewer.Command;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;
import dvbviewertimerimport.dvbviewer.DVBViewerProvider;
import dvbviewertimerimport.gui.GUIPanel;
import dvbviewertimerimport.gui.TimersDialog;
import dvbviewertimerimport.main.Versions;
import dvbviewertimerimport.misc.Constants;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.misc.TerminateClass;
import dvbviewertimerimport.provider.Provider;

/**
 * @author Stefan Gollmer
 *
 */
public class DVBViewerTimerImport extends Plugin implements DVBViewerProvider {

	private static final boolean DEBUG = false;

	private static Version version = null;

	private static DVBViewerTimerImport plugin = null;

	private static String ADD_TIMER = ResourceManager.msg("ADD_TIMER");
	private static String DELETE_TIMER = ResourceManager.msg("DELETE_TIMER");

	private PluginInfo pluginInfo = null;
	private boolean isInitialized = false;
	private Control control = null;
	private DVBViewer dvbViewer = null;
	private GUIPanel settingsPanel = null;

	private Icon channelChooseIcon = ResourceManager.createImageIcon("icons/dvbViewer16.png", "DVBViewer icon");
	private Icon timerAddIcon = ResourceManager.createImageIcon("icons/dvbViewer Timer16.png", "Timer icon");
	private Icon timerDeleteIcon = ResourceManager.createImageIcon("icons/dvbViewer TimerDisabled 16.png",
			"Timer delete icon");

	private Icon menuIcon = null;
	private Icon[] markIcons = null;
	private String mainMenue = null;

	private DVBViewerChannelChooseAction chooseChannelAction = new DVBViewerChannelChooseAction();
	private DVBViewerTimerAction timerAction = new DVBViewerTimerAction();

	private int providerID;
	private Provider provider;

	private GregorianCalendar calendar;

	private Collection<dvbviewertimerimport.control.Channel> tvbChannelNames = null;
	private Map<String, Channel> uniqueAssignment = null;

	private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

	/**
	 * 
	 */
	public DVBViewerTimerImport() {
		DVBViewerTimerImport.plugin = this;
	}

	@Override
	protected void finalize() {
		DVBViewerTimerImport.plugin = null;
	}

	/**
	 * Called by the host-application during start-up.
	 * <p>
	 * Override this method to load your plugins settings from the file system.
	 *
	 * @param settings The settings for this plugin (May be empty).
	 */
	@Override
	public void loadSettings(Properties settings) {
		try {
			if (!init()) {
				return;
			}
			handleTvDataUpdateFinished();
		} catch (Throwable ee) {
			Log.out("Throws on loadSettings, Message: " + ee.getMessage());
			throw ee;
		}
	}

	@Override
	public void handleTvDataUpdateFinished() {
		try {
			DVBViewerTimerImport.plugin.tvbChannelNames = null;
			try {
				this.control.getDVBViewer().process(this, false, (Object) null, Command.UPDATE_TVBROWSER);
			} catch (ErrorClass e) {
				this.errorMessage(e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TerminateClass e) {
			}
		} catch (Throwable ee) {
			Log.out("Throws on handleTvDataUpdateFinished, Message: " + ee.getMessage());
			throw ee;
		}
	}

	private static Map<String, Channel> getUniqueAssignmentMap() {
		if (DVBViewerTimerImport.plugin == null) {
			return null;
		}
		if (DVBViewerTimerImport.plugin.uniqueAssignment == null) {
			getTVBChannelNames();
		}
		return DVBViewerTimerImport.plugin.uniqueAssignment;
	}

	/**
	 * @return Array containing the channel names of the TV-Browser
	 */
	public static Collection<dvbviewertimerimport.control.Channel> getTVBChannelNames() {
		if (DVBViewerTimerImport.plugin == null) {
			return null;
		}
		if (DVBViewerTimerImport.plugin.tvbChannelNames != null) {
			return DVBViewerTimerImport.plugin.tvbChannelNames;
		}
		devplugin.Channel[] channels = devplugin.Plugin.getPluginManager().getSubscribedChannels();

		DVBViewerTimerImport.plugin.uniqueAssignment = new HashMap<String, Channel>();

		DVBViewerTimerImport.plugin.tvbChannelNames = new ArrayList<>();

		// create a map, containing for each channel name the possible unique IDs

		for (devplugin.Channel c : channels) {
			if (DEBUG) {
				Log.out("Channel processing: \"" + c.getName() + "\" with provider key \"" + c.getUniqueId() + "\"");
			}
			DVBViewerTimerImport.plugin.uniqueAssignment.put(c.getUniqueId(), c);
			DVBViewerTimerImport.plugin.tvbChannelNames
					.add(new dvbviewertimerimport.control.Channel(3, c.getName(), null, c.getUniqueId(), true));
		}

		return DVBViewerTimerImport.plugin.tvbChannelNames;

	}

	private boolean init() {
		if (this.isInitialized) {
			return true;
		}
		Provider.setIsPlugin();

		try {
			Log.setToDisplay(true);

			this.dvbViewer = new DVBViewer();

			this.control = new Control(this.dvbViewer);
		} catch (ErrorClass e) {
			this.errorMessage(e);
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (TerminateClass e) {
			return false;
		}
		this.provider = Provider.getProvider("TV-Browser");
		this.provider.setIsFunctional(true);
		this.providerID = this.provider.getID();

		this.calendar = new GregorianCalendar(this.provider.getTimeZone());

		this.menuIcon = ResourceManager.createImageIcon("icons/dvbViewer Programm16.png", "DVBViewerTimer icon");

		this.mainMenue = ResourceManager.msg("DVBVIEWER");

		if (this.provider.getVerbose())
			Log.setVerbose(true);

		Log.setToDisplay(this.provider.getMessage());

		this.control.setDVBViewerEntries();

		this.control.setDefaultProvider(this.provider.getName());

		this.isInitialized = true;
		Log.out("TVBrowser plugin initialized");
		return true;
	}

	/**
	 * @param e Throwable of the last exception/error ....
	 */
	public void errorMessage(Throwable e) {
		if (e instanceof ErrorClass) {
			Log.error(e.getLocalizedMessage());
			Log.out("Import terminated with errors");
		}
		return;
	}

	public static Version getVersion() {
		if (version == null) {
			int[] v = Versions.getIntVersion();
			version = new Version(v[0], v[1], v[2], v[3] == 0);
		}
		return version;
	}

	@Override
	public PluginInfo getInfo() {
		try {
			if (this.pluginInfo == null)
				this.pluginInfo = new PluginInfo(DVBViewerTimerImport.class, ResourceManager.msg("PLUGIN_NAME"),
						ResourceManager.msg("DESCRIPTION"), "Gollmer, Stefan");
			return this.pluginInfo;
		} catch (Throwable ee) {
			Log.out("Throws on getInfo, Message: " + ee.getMessage());
			throw ee;
		}
	}

	@Override
	public Icon[] getMarkIconsForProgram(Program p) {
		try {
			if (this.markIcons != null) {
				return this.markIcons;
			}
			Icon i = ResourceManager.createImageIcon("icons/dvbViewer Programm16.png", "DVBViewer icon");
			this.markIcons = new Icon[] { i };
			return this.markIcons;
		} catch (Throwable ee) {
			Log.out("Throws on getMarkIconsForProgram, Message: " + ee.getMessage());
			throw ee;
		}
	}

	private class DVBViewerChannelChooseAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6919317872001002341L;
		private Program program = null;

		public DVBViewerChannelChooseAction() {
			super();
			putValue(Action.NAME, ResourceManager.msg("SELECT_CHANNEL"));
			putValue(Action.SMALL_ICON, DVBViewerTimerImport.this.channelChooseIcon);
		}

		public void update(final Program program) {
			this.program = program;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if (DVBViewerTimerImport.this.dvbViewer == null) {
					return;
				}

				if (this.program != null) {

					DVBViewerTimerImport.this.dvbViewer.selectChannel(DVBViewerTimerImport.this.provider,
							this.program.getChannel().getUniqueId());
				}
			} catch (Throwable ee) {
				Log.out("Throws on DVBViewerChannelChooseAction.actionPerformed, Message: " + ee.getMessage());
				throw ee;
			}
		}
	}

	private class DVBViewerTimerAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Program program = null;
		private Command command = null;

		public void update(final Program program, final Command command) {
			this.program = program;
			this.command = command;
			if (command == Command.SET) {
				putValue(Action.NAME, DVBViewerTimerImport.ADD_TIMER);
				putValue(Action.SMALL_ICON, DVBViewerTimerImport.this.timerAddIcon);
			} else {
				putValue(Action.NAME, DVBViewerTimerImport.DELETE_TIMER);
				putValue(Action.SMALL_ICON, DVBViewerTimerImport.this.timerDeleteIcon);
			}
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				try {
					DVBViewerTimerImport.this.control.getDVBViewer().process(DVBViewerTimerImport.this, false,
							this.program, this.command);
				} catch (ErrorClass e) {
					errorMessage(e);
					return;
				} catch (Exception e) {
					errorMessage(e);
					e.printStackTrace();
					return;
				} catch (TerminateClass e) {
					return;
				}
				markProgram(this.program, this.command == Command.SET);

				this.program.validateMarking();
				updateTreeNode();
			} catch (Throwable ee) {
				Log.out("Throws on DVBViewerTimerAction.actionPerformed, Message: " + ee.getMessage());
				throw ee;
			}

		}

	}

	@Override
	public ActionMenu getContextMenuActions(final Program program) {

		Program example = getPluginManager().getExampleProgram();

		Action[] subActions = new AbstractAction[2];

		Command temp = Command.SET;
		try {
			if (!program.equals(example)) {
				if (!this.control.getDVBViewer().process(this, false, program, Command.FIND_SENDER)) {
					if (DEBUG) {
						Log.out("Channel \"" + program.getChannel().getName() + "\" not available on DVBViewer");
					}
					return null;
				}
			}

			if (this.control.getDVBViewer().process(this, false, program, Command.FIND))
				temp = Command.DELETE;
		} catch (ErrorClass e) {
			this.errorMessage(e);
			return null;
		} catch (Exception e) {
			this.errorMessage(e);
			e.printStackTrace();
			return null;
		} catch (TerminateClass e) {
			return null;
		}

		subActions[0] = this.timerAction;
		this.timerAction.update(program, temp);

		subActions[1] = this.chooseChannelAction;
		this.chooseChannelAction.update(program);

		return new ActionMenu(this.mainMenue, this.menuIcon, subActions);
	}

	private void markProgram(final Program program, boolean mark) {
		if (mark) {
			this.mRootNode.addProgram(program);
			// program.mark( this ) ;
		} else {
			this.mRootNode.removeProgram(program);
			// program.unmark( this ) ;
		}
	}

	/**
	 * Get the Root-Node. The CapturePlugin handles all Programs for itself. Some
	 * Devices can remove Programs externaly
	 */
	@Override
	public PluginTreeNode getRootNode() {
		return this.mRootNode;
	}

	@Override
	public boolean canUseProgramTree() {
		return true;
	}

	private void updateTreeNode() {
		this.mRootNode.update();
	}

	class DVBVSettingsTab implements SettingsTab {

		@Override
		public JPanel createSettingsPanel() {
			try {
				if (!init())
					return null;
				if (DVBViewerTimerImport.this.settingsPanel == null) {
					DVBViewerTimerImport.this.settingsPanel = new GUIPanel(DVBViewerTimerImport.this.control);
					DVBViewerTimerImport.this.settingsPanel.init();
				}
				return DVBViewerTimerImport.this.settingsPanel;
			} catch (Throwable ee) {
				Log.out("Throws on DVBVSettingsTab.createSettingsPanel, Message: " + ee.getMessage());
				throw ee;
			}
		}

		@Override
		public Icon getIcon() {
			try {
				ImageIcon pluginIcon = ResourceManager.createImageIcon("icons/dvbViewer Programm16.png",
						"DVBViewer icon");
				return pluginIcon;
			} catch (Throwable ee) {
				Log.out("Throws on DVBVSettingsTab.getIcon, Message: " + ee.getMessage());
				throw ee;
			}
		}

		@Override
		public String getTitle() {
			try {
				return ResourceManager.msg("PLUGIN_NAME");
			} catch (Throwable ee) {
				Log.out("Throws on DVBVSettingsTab.getTitle, Message: " + ee.getMessage());
				throw ee;
			}
		}

		@Override
		public void saveSettings() {
			try {
				if (!init())
					return;
				DVBViewerTimerImport.this.control.setDVBViewerEntries();
				Log.out("Configuration saved");
				DVBViewerTimerImport.this.control.renameImportedFile();
				DVBViewerTimerImport.this.settingsPanel.updateTab();
				DVBViewerTimerImport.this.control.write(null);
			} catch (Throwable ee) {
				Log.out("Throws on DVBVSettingsTab.saveSettings, Message: " + ee.getMessage());
				throw ee;
			}
		}

	}

	public SettingsTab getSettingsTab() {
		return new DVBVSettingsTab();
	}

	@Override
	public boolean processEntry(Object arg, DVBViewer.Command command) {
		try {
			boolean result = true;

			if (command == Command.UPDATE_TVBROWSER) {
				this.updateMarks();
				return true;
			}

			if (command == Command.FIND_SENDER) {
				if (!(arg instanceof Program)) {
					Log.out("Warning: wrong call of FIND_SENDER");
					return false;
				}
				Channel channel = ((Program) arg).getChannel();
				ChannelSet set = this.control.getChannelSets().get(this.providerID, channel.getUniqueId());
				if (set == null) {
					return false;
				}
				return set.isDefinedDVBViewerChannel();
			}

			if (command == Command.UPDATE_UNRESOLVED_ENTRIES) {
				int end = this.control.getDVBViewer().getRecordEntries().size();
				for (int i = 0; i < end; ++i) {
					DVBViewerEntry co = this.control.getDVBViewer().getRecordEntries().get(i);
					if (co.getProvider() == this.provider && co.isProgramEntry() && co.getProviderCID() != null) {
						Program program = Plugin.getPluginManager().getProgram(co.getProviderCID());
						if (program != null)
							continue;
						Program pgm = this.searchBestFit(co);
						if (pgm == null)
							continue;
						long[] times = calcRecordTimes(pgm);
						String channelId = pgm.getChannel().getUniqueId();
						this.dvbViewer.shiftEntry(co, this.provider, pgm.getUniqueID(), channelId, times[0], times[1],
								pgm.getTitle());
						this.markProgram(pgm, true);
						pgm.validateMarking();
						continue;
					}
				}
				return true;
			}

			Program program = (Program) arg;

			switch (command) {
				case SET: {
					long[] times = calcRecordTimes(program);
					String id = program.getChannel().getUniqueId();
					this.dvbViewer.addNewEntry(this.provider, program.getUniqueID(), id, times[0], times[1],
							program.getTitle());
					break;
				}
				case FIND:
					result = findProgram(program) != null;
					break;
				case DELETE: {
					DVBViewerEntry entry = findProgram(program);
					if (entry == null)
						return false;
					this.dvbViewer.deleteEntry(entry);
					break;
				}
				default:
					break;
			}
			return result;
		} catch (ErrorClass ee) {
			throw ee;
		} catch (Throwable ee) {
			Log.out("Throws on processEntry, Message: " + ee.getMessage());
			Log.outStackTrace(ee);
			throw ee;
		}
	}

	/**
	 * @param program program which will seach in the DVBViewer recording list
	 * @return null if not found otherwise the DVBViewer entry oft the recording
	 */
	public DVBViewerEntry findProgram(final Program program) {
		DVBViewerEntry entry = null;

		for (DVBViewerEntry co : this.control.getDVBViewer().getRecordEntries()) {
			if (co.getProvider() == this.provider && co.getProviderCID() != null
					&& co.getProviderCID().equals(program.getUniqueID()) && co.isProgramEntry()) {
				entry = co;
				break;
			}
		}
		return entry;
	}

	/**
	 * 
	 */
	public void updateMarks() {
		Program[] programs = Plugin.getPluginManager().getMarkedPrograms();
		for (Program program : programs) {
			this.markProgram(program, false);
		}

		boolean unresolvedEntries = false;
		for (DVBViewerEntry co : this.control.getDVBViewer().getRecordEntries()) {
			if (co.getProvider() == this.provider && co.isProgramEntry() && co.getProviderCID() != null) {
				Program program = Plugin.getPluginManager().getProgram(co.getProviderCID());
				if (program == null) {
					unresolvedEntries = true;
					continue;
				}
				this.markProgram(program, true);
				program.validateMarking();
			}
		}

		if (unresolvedEntries) {
			try {
				this.dvbViewer.process(this, false, null, Command.UPDATE_UNRESOLVED_ENTRIES);
			} catch (ErrorClass err) {
				errorMessage(err);
				return;
			} catch (Exception err) {
				errorMessage(err);
				err.printStackTrace();
				return;
			} catch (TerminateClass err) {
				return;
			}
		}
		this.updateTreeNode();
	}

	private long[] calcRecordTimes(final Program program) {
		this.calendar.clear();
		Date d = program.getDate();
		this.calendar.set(d.getYear(), d.getMonth() - 1, d.getDayOfMonth(), program.getHours(), program.getMinutes());
		long startTime = this.calendar.getTimeInMillis();
		long length = program.getLength() * 1000 * 60;
		// System.out.println( "Date: " + new java.util.Date( startTime ) + calendar ) ;
		long endTime = startTime;
		if (length >= 0)
			endTime += length;
		else {
			endTime += Constants.DAYMILLSEC;
			// Workaround if length not defined!!!
			boolean finished = false;
			for (int t = 0; t < 2 && !finished; t++) {
				this.calendar.clear();
				this.calendar.set(d.getYear(), d.getMonth() - 1, d.getDayOfMonth() + t);
				Date nd = new Date(this.calendar);

				nd.addDays(t);
				Iterator<Program> pIt = Plugin.getPluginManager().getChannelDayProgram(nd, program.getChannel());
				while (pIt.hasNext()) {
					Program p = pIt.next();
					Date dd = p.getDate();
					this.calendar.clear();
					this.calendar.set(dd.getYear(), dd.getMonth() - 1, dd.getDayOfMonth(), p.getHours(),
							p.getMinutes());
					long tmp = this.calendar.getTimeInMillis();
					if (tmp > startTime && tmp < endTime) {
						endTime = tmp;
						if (t == 0)
							finished = true;
					}
				}
			}
		}
		return new long[] { startTime, endTime };
	}

	@Override
	public boolean process(boolean getAll, Command command) {
		return true;
	}

	/*
	 * @return the action to use for the menu and the toolbar or <code>null</code>
	 * if the plugin does not provide this feature.
	 */
	@Override
	public ActionMenu getButtonAction() {
		AbstractAction action = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new TimersDialog(null, DVBViewerTimerImport.this.control).init();
				} catch (Throwable ee) {
					Log.out("Throws on getButtonAction.actionPerformed, Message: " + ee.getMessage());
					throw ee;
				}
			}
		};

		action.putValue(Action.NAME, ResourceManager.msg("DVBVIEWER"));
		action.putValue(Action.SMALL_ICON,
				ResourceManager.createImageIcon("icons/dvbViewer Programm16.png", "DVBViewerTimer icon"));
		action.putValue(BIG_ICON,
				ResourceManager.createImageIcon("icons/dvbViewer Programm24.png", "DVBViewerTimer icon"));
		action.putValue(Action.SHORT_DESCRIPTION, getInfo().getDescription());

		return new ActionMenu(action);
	}

	/**
	 * @param entries
	 */
	public static void updateRecordings(ArrayList<DVBViewerEntry> entries) {
		if (DVBViewerTimerImport.plugin != null)
			DVBViewerTimerImport.plugin.updateMarks();
	}

	@Override
	public boolean canReceiveProgramsWithTarget() {
		try {
			return getProgramReceiveTargets().length > 0;
		} catch (Throwable ee) {
			Log.out("Throws on canReceiveProgramsWithTarget, Message: " + ee.getMessage());
			throw ee;
		}
	}

	@Override
	public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
		try {
			if (receiveTarget == null || receiveTarget.getTargetId() == null)
				return false;

			String id = receiveTarget.getTargetId();

			try {
				if (id.equals("RECORD"))
					this.dvbViewer.process(this, false, programArr, DVBViewer.Command.SET);
				else if (id.equals("REMOVE"))
					this.dvbViewer.process(this, false, programArr, DVBViewer.Command.DELETE);
				else
					return false;
			} catch (ErrorClass e) {
				errorMessage(e);
				return false;
			} catch (Exception e) {
				errorMessage(e);
				e.printStackTrace();
				return false;
			} catch (TerminateClass e) {
				return false;
			}
			updateMarks();
			return true;
		} catch (Throwable ee) {
			Log.out("Throws on receivePrograms, Message: " + ee.getMessage());
			throw ee;
		}
	}

	@Override
	public ProgramReceiveTarget[] getProgramReceiveTargets() {
		try {
			ProgramReceiveTarget ADD_TARGET = new ProgramReceiveTarget(this, DVBViewerTimerImport.ADD_TIMER, "RECORD");
			ProgramReceiveTarget REMOVE_TARGET = new ProgramReceiveTarget(this, DVBViewerTimerImport.DELETE_TIMER,
					"REMOVE");

			ProgramReceiveTarget[] targets = { ADD_TARGET, REMOVE_TARGET };

			return targets;
		} catch (Throwable ee) {
			Log.out("Throws on getProgramReceiveTargets, Message: " + ee.getMessage());
			throw ee;
		}
	}

	public Program searchBestFit(DVBViewerEntry entry) {
		Program result = null;
		Date[] interval = new Date[2];

		long startTimeOrg = entry.getStartOrg();

		long startSearch = startTimeOrg - 1000 * 60 * 60 * 3;
		long endSearch = startTimeOrg + 1000 * 60 * 60 * 3;

		this.calendar.clear();
		this.calendar.setTimeInMillis(startSearch);
		interval[0] = new Date(this.calendar);

		this.calendar.clear();
		this.calendar.setTimeInMillis(endSearch);
		interval[1] = new Date(this.calendar);

		int end = 2;

		if (interval[0].getDayOfMonth() == interval[1].getDayOfMonth())
			end = 1;

		String uniqueName = (String) entry.getChannelSet().getChannel(this.providerID).getIDKey();
		Channel channel = DVBViewerTimerImport.getUniqueAssignmentMap().get(uniqueName);

		if (channel == null)
			return null;

		Collection<Program> possibilities = new ArrayList<Program>();

		for (int i = 0; i < end; ++i) {
			Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(interval[i], channel);
			for (; it.hasNext();) {
				Program pgm = it.next();
				if (pgm.getTitle().equals(entry.getTitle()))
					possibilities.add(pgm);
			}
		}
		if (possibilities.size() == 0)
			return null;

		long deltaMin = startTimeOrg;

		for (Iterator<Program> it = possibilities.iterator(); it.hasNext();) {
			Program pgm = it.next();
			Date dd = pgm.getDate();
			this.calendar.clear();
			this.calendar.set(dd.getYear(), dd.getMonth() - 1, dd.getDayOfMonth(), pgm.getHours(), pgm.getMinutes());

			long delta = Math.abs(this.calendar.getTimeInMillis() - startTimeOrg);
			if (result == null || deltaMin > delta) {
				result = pgm;
				deltaMin = delta;
			}
		}
		return result;
	}

}
