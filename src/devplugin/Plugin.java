package devplugin;

import java.awt.event.ActionEvent;
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

			@Override
			public Program getExampleProgram() {
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

	  /**
	   * Gets the actions for the context menu of a program.
	   * <p>
	   * Override this method to provide context menu items for programs (e.g. in
	   * the program table). If your plugin shows a context menu only for some
	   * programs, but not for all, then you should explicitly return a non-<code>null</code>
	   * menu for the example program. Otherwise your context menu will not be shown
	   * in the settings dialog for the context menu order.
	   * <p>
	   * The following action values will be used:
	   * <ul>
	   * <li><code>Action.NAME</code>: The text for the context menu item.</li>
	   * <li><code>Action.SMALL_ICON</code>: The icon for the context menu item.
	   * Should be 16x16.</li>
	   * </ul>
	   *
	   * @param program
	   *          The program the context menu will be shown for.
	   * @return the actions this plugin provides for the given program or
	   *         <code>null</code> if the plugin does not provide this feature.
	   *
	   * @see #getProgramFromContextMenuActionEvent(ActionEvent)
	   */
	  	public ActionMenu getContextMenuActions(final Program program) {
		// This plugin supports no context menus for programs
		return null;
	}
	
	  /**
	   * Gets the root node of the plugin for the plugin tree.
	   * @see #canUseProgramTree()
	   *
	   * @return The root node.
	   */
	  public PluginTreeNode getRootNode() {
	    return null;
	  }

	  /**
	   * Signal whether this plugin participates in the plugin tree view or not.
	   * @see #getRootNode()
	   * @return true, if the programs of this plugin are handled by the plugin
	   *      tree view
	   * @since 1.1
	   */
	  public boolean canUseProgramTree() {
	    return false;
	  }
}
