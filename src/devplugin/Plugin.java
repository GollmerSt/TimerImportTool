package devplugin;

import java.util.Iterator;
import java.util.Properties;

import javax.swing.Icon;

public abstract class Plugin implements Marker, ProgramReceiveIf {
	
	public static String BIG_ICON = "BigIcon";

	public abstract void loadSettings(Properties settings);

	public abstract void handleTvDataUpdateFinished();

	public abstract PluginInfo getInfo();

	final public static PluginManager getPluginManager() {
		return new PluginManager() {

			@Override
			public Channel[] getSubscribedChannels() {
				return null;
			}

			@Override
			public Program getProgram(Date date, String progID) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Program getProgram(String uniqueID) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Iterator<Program> getChannelDayProgram(Date date, Channel channel) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Program[] getMarkedPrograms() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	public abstract Icon[] getMarkIconsForProgram(Program p);

	public ActionMenu getButtonAction() {
		return null;
	}

	@Override
	public boolean canReceiveProgramsWithTarget() {
		return false;
	}

	@Override
	public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
		return false;
	}

	@Override
	public ProgramReceiveTarget[] getProgramReceiveTargets() {
		return null;
	}
	
	@Override
	public String getId() {
		return null;
	}

	@Override
	public Icon getMarkIcon() {
		return null;
	}

	@Override
	public Icon[] getMarkIcons(Program p) {
		return null;
	}

	@Override
	public int getMarkPriorityForProgram(Program p) {
		return 0;
	}

	@Override
	public boolean receiveValues(String[] values, ProgramReceiveTarget receiveTarget) {
		return false;
	}

	@Override
	public int compareTo(ProgramReceiveIf o) {
		return 0;
	}

}
