// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import devplugin.ActionMenu;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;
import dvbviewertimerimport.dvbviewer.DVBViewerProvider;
import dvbviewertimerimport.dvbviewer.DVBViewer.Command;
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
public class DVBViewerTimerImport extends Plugin implements DVBViewerProvider
{
  private static Version version = null ;

  private PluginInfo pluginInfo = null ;
  private boolean isInitialized = false ;
  private Control control = null ;
  private DVBViewer dvbViewer = null ;
  private GUIPanel settingsPanel = null ;

  private Icon channelChooseIcon = ResourceManager.createImageIcon( "icons/dvbViewer16.png", "DVBViewer icon" ) ;
  private Icon timerIcon = ResourceManager.createImageIcon( "icons/dvbViewer Timer16.png", "Timer icon" ) ;

  private Icon menuIcon = null ;
  private Icon[] markIcons = null ;
  private String mainMenue = null ;
  
  private DVBViewerChannelChooseAction chooseChannelAction = new DVBViewerChannelChooseAction() ;
  private DVBViewerTimerAction timerAction = new DVBViewerTimerAction() ;

  private DVBViewerProvider dvbViewerProvider = this ;
  private int providerID ;
  private Provider provider ;

  private GregorianCalendar calendar ;

  private HashMap< String, String > channelAssignmentDvbVToTvB = null ;

  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);


 /**
   * Called by the host-application during start-up.
   * <p>
   * Override this method to load your plugins settings from the file system.
   *
   * @param settings The settings for this plugin (May be empty).
   */
  @Override
  public void loadSettings(Properties settings) {
    if ( ! init() )
      return ;
    handleTvDataUpdateFinished() ;
  }

  @Override
  public void handleTvDataUpdateFinished()
  {
    try {
      this.control.getDVBViewer().process( this.dvbViewerProvider, false, null, Command.UPDATE_TVBROWSER ) ;
    } catch ( ErrorClass e ) {
      this.errorMessage(  e ) ;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (TerminateClass e) {
    }
 }



  /**
   * @return Array containing the channel names of the TV-Browser
   */
  public static String[] getTVBChannelNames()
  {
    ArrayList< String > res = new ArrayList< String >() ;
    devplugin.Channel[] channels = devplugin.Plugin.getPluginManager().getSubscribedChannels();
    for ( devplugin.Channel c : channels )
      res.add( c.getName() ) ;
    return res.toArray( new String[0] ) ;
  }



  private boolean init()
  {
    if ( isInitialized )
      return true ;

    Provider.setIsPlugin() ;

    try {
      Log.setToDisplay(true);

      dvbViewer = new DVBViewer() ;

     control = new Control(dvbViewer) ;
    } catch (ErrorClass e) {
        this.errorMessage(  e ) ;
        return false ;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false ;
    } catch (TerminateClass e) {
      return false ;
    }
    this.provider = Provider.getProvider( "TV-Browser" ) ;
    this.provider.setIsFunctional( true ) ;
    this.providerID = provider.getID() ;

    this.calendar = new GregorianCalendar( this.provider.getTimeZone() ) ;

    this.menuIcon = ResourceManager.createImageIcon( "icons/dvbViewer Programm16.png", "DVBViewerTimer icon" ) ;
   
    this.mainMenue   = ResourceManager.msg( "DVBVIEWER" ) ;



    if ( provider.getVerbose() )
      Log.setVerbose( true ) ;

    Log.setToDisplay(provider.getMessage() );
    
    control.setDVBViewerEntries() ;
    
    control.setDefaultProvider( provider.getName() ) ;

    isInitialized = true ;
    return true ;
  }
/*        dvbViewer.writeDataPathToIni() ;

  if ( paras.length() != 0)
    Log.out( "Parameters: " + paras ) ;

  dvbViewer.process( provider, getAll, args ) ;

if ( ErrorClass.isWarning() )
{
  Log.out( "Import finished, but warnings occurs. The messages should be checked"  ) ;
  if ( showMessageBox|| provider.getID() == Provider.getProviderID("ClickFinder") )
    JOptionPane.showMessageDialog(null, "Warnings occurs", provider.getName() + " status", JOptionPane.WARNING_MESSAGE);
}
else
{
  Log.out( "Import successfull finished" ) ;
  if ( showMessageBox )
    JOptionPane.showMessageDialog(null, "Successfull finished", provider.getName() + " status", JOptionPane.INFORMATION_MESSAGE);
}
System.exit(0);
  }
*/

  /**
   * @param e Throwable of the last exception/error ....
   */
  public void errorMessage( Throwable e )
  {
    if  ( e.getClass().equals(  ErrorClass.class ) )
    {
      Log.error(e.getLocalizedMessage());
      Log.out("Import terminated with errors" ) ;
    }
    return ;
  }
  public static Version getVersion()
  {
    if ( version == null )
    {
      int[] v = Versions.getIntVersion() ;
      version = new Version(v[0], v[1], v[2], true ) ;
    }
    return version ;
  }
  @Override
  public PluginInfo getInfo()
  {
    if ( pluginInfo == null )
      pluginInfo = new PluginInfo( DVBViewerTimerImport.class,
          ResourceManager.msg( "PLUGIN_NAME" ),
          ResourceManager.msg( "DESCRIPTION" ),
          "Gollmer, Stefan" );
    return pluginInfo ;
  }

  @Override
  public Icon[] getMarkIconsForProgram(Program p)
  {
    if ( this.markIcons != null )
      return this.markIcons ;
    Icon i = ResourceManager.createImageIcon( "icons/dvbViewer Programm16.png", "DVBViewer icon" ) ;
    this.markIcons = new Icon[] {i} ;
    return this.markIcons ;
  }

  /**
   * @param dvbVChannelName   DVBViewer channel name
   * @return  assigned TV-Browser name, null if not assigned
   */
  public String getTvBChannelName( String dvbVChannelName )
  {
    if ( channelAssignmentDvbVToTvB == null )
    {
      channelAssignmentDvbVToTvB = new HashMap< String, String >() ;
      for ( ChannelSet cs : this.control.getChannelSets() )
      {
        dvbviewertimerimport.control.Channel providerChannel = cs.getChannel( this.providerID ) ;
        String dvbViewerChannel = cs.getDVBViewerChannel() ;
        if ( providerChannel != null && dvbViewerChannel != null )
          channelAssignmentDvbVToTvB.put( cs.getDVBViewerChannel().split("[|]")[0], providerChannel.getName() ) ;
      }
    }
    if ( ! channelAssignmentDvbVToTvB.containsKey( dvbVChannelName.split("[|]") ) )
      return null ;
    return channelAssignmentDvbVToTvB.get( dvbVChannelName ) ;
  }
  
  
  private class DVBViewerChannelChooseAction extends AbstractAction
  {
    private Program program = null ;
    
    public DVBViewerChannelChooseAction()
    {
      super() ;
      putValue(Action.NAME, ResourceManager.msg( "SELECT_CHANNEL" )  ) ;
      putValue(Action.SMALL_ICON, channelChooseIcon ) ;
   }

    public void update( final Program program )
    {
      this.program = program ;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      if ( dvbViewer == null )
        return ;
      devplugin.Channel tvBChannel = this.program.getChannel() ;
      
      dvbviewertimerimport.dvbviewer.Channel dvbChannel = null ;
      
      try {
        dvbChannel = dvbViewer.getDVBViewerChannel( provider, tvBChannel.getName() ) ;
      } catch ( ErrorClass e1 ) {
        errorMessage( e1 ) ;
        return;
      }
      
      dvbViewer.startDVBViewerAndSelectChannel( dvbChannel.getDVBViewer() ) ;
    }
  }

  private class DVBViewerTimerAction extends AbstractAction
  {
    private Program program = null ;
    private Command command = null ;
    private String addTimer    = ResourceManager.msg( "ADD_TIMER" ) ;
    private String deleteTimer = ResourceManager.msg( "DELETE_TIMER" ) ;

    public void update( final Program program, final Command command )
    {
      this.program = program ;
      this.command = command ;
      if ( command == Command.SET )
      {
        putValue(Action.NAME, addTimer ) ;
        putValue(Action.SMALL_ICON, timerIcon ) ;
      }
      else
      {
        putValue(Action.NAME, deleteTimer ) ;
        //putValue(Action.SMALL_ICON, menuIcon ) ;
      }
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
      try {
        control.getDVBViewer().process( dvbViewerProvider, false, this.program, this.command ) ;
      } catch ( ErrorClass e ) {
        errorMessage( e ) ;
        return;
      }
      catch (Exception e ) {
        errorMessage( e ) ;
        e.printStackTrace();
        return ;
      } catch (TerminateClass e) {
        return ;
      }
      if ( dvbViewer != null )
        dvbViewer.writeXML() ;
      markProgram( program, command == Command.SET ) ;

     program.validateMarking() ;
     updateTreeNode();
    }
    
  }

  
  public ActionMenu getContextMenuActions( final Program program)
  {
    Action mainAction = new devplugin.ContextMenuAction() ;
    mainAction.putValue(Action.NAME, mainMenue ) ;
    mainAction.putValue(Action.SMALL_ICON, menuIcon ) ;
    
    Action [] subActions = new AbstractAction[ 2 ] ;
    
    Command temp = Command.SET ;
    try {
      if ( control.getDVBViewer().process( dvbViewerProvider, false, program, Command.FIND ) )
        temp = Command.DELETE ;
    } catch ( ErrorClass e ) {
      this.errorMessage( e ) ;
      return null ;
    }
    catch (Exception e ) {
      this.errorMessage( e ) ;
      e.printStackTrace();
      return null ;
    } catch (TerminateClass e) {
      return null ;
    }
    
    subActions[ 0 ] = timerAction ;
    timerAction.update( program, temp ) ;
    
    subActions[ 1 ] = chooseChannelAction ;
    chooseChannelAction.update( program ) ;
    
    return new ActionMenu(mainAction, subActions );
  }


/*  @Override
  public ActionMenu getContextMenuActions( final Program program)
  {
    if ( ! init() )
      return null ;
    // Eine Aktion erzeugen, die die Methode sendMail(Program) aufruft, sobald sie aktiviert wird.
    Command temp = Command.SET ;
    try {
      if ( control.getDVBViewer().process( dvbViewerProvider, false, program, Command.FIND ) )
        temp = Command.DELETE ;
    } catch ( ErrorClass e ) {
      this.errorMessage( e ) ;
      return null ;
    }
    catch (Exception e ) {
      this.errorMessage( e ) ;
      e.printStackTrace();
      return null ;
    } catch (TerminateClass e) {
    	return null ;
    }
    final Command command = temp ;
    AbstractAction action = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        try {
          control.getDVBViewer().process( dvbViewerProvider, false, program, command ) ;
        } catch ( ErrorClass e ) {
          errorMessage( e ) ;
          return;
        }
        catch (Exception e ) {
          errorMessage( e ) ;
          e.printStackTrace();
          return ;
        } catch (TerminateClass e) {
        	return ;
        }
        if ( dvbViewer != null )
          dvbViewer.writeXML() ;
        if ( command == Command.SET )
          markProgram( program, true ) ;
        else
          markProgram( program, false ) ;
       program.validateMarking() ;
       updateTreeNode();
      }
    };

    // Der Aktion einen Namen geben. Dieser Name wird dann im Kontextmenü gezeigt
    if ( command == Command.SET )
        action.putValue(Action.NAME, addTimer );
      else
        action.putValue(Action.NAME, deleteTimer );

    // Der Aktion ein Icon geben. Dieses Icon wird mit dem Namen im Kontextmenü gezeigt
    // Das Icon sollte 16x16 Pixel groß sein
    action.putValue(Action.SMALL_ICON, menuIcon );

    // Das Aktions-Menü erzeugen und zurückgeben
    return new ActionMenu(action);
  }

*/
  private void markProgram( final Program program, boolean mark )
  {
    if ( mark )
    {
      this.mRootNode.addProgram( program ) ;
      //program.mark( this ) ;
    }
    else
    {
      this.mRootNode.removeProgram( program ) ;
      //program.unmark( this ) ;
    }
  }
  /**
   * Get the Root-Node.
   * The CapturePlugin handles all Programs for itself. Some
   * Devices can remove Programs externaly
   */
  public PluginTreeNode getRootNode() {
      return mRootNode;
  }

  public boolean canUseProgramTree() {
    return true;
}

  private void updateTreeNode() {
    mRootNode.update();
}




  class DVBVSettingsTab implements SettingsTab
  {

    @Override
    public JPanel createSettingsPanel()
    {
      if ( ! init() )
        return null ;
      if ( settingsPanel == null)
      {
        settingsPanel = new GUIPanel( control ) ;
        settingsPanel.init() ;
      }
      return settingsPanel ;
    }

    @Override
    public Icon getIcon() {
      ImageIcon pluginIcon   = ResourceManager.createImageIcon( "icons/dvbViewer Programm16.png", "DVBViewer icon" ) ;

      // TODO Auto-generated method stub
      return pluginIcon;
    }

    @Override
    public String getTitle() {
      return ResourceManager.msg( "PLUGIN_NAME" ) ;
    }

    @Override
    public void saveSettings()
    {
      if ( ! init() )
        return ;
      control.setDVBViewerEntries() ;
      Log.out( "Configuration saved" ) ;
      control.renameImportedFile() ;
      control.write( null ) ;
      channelAssignmentDvbVToTvB = null ;
    }

  }
  public SettingsTab getSettingsTab()
  {
    return new DVBVSettingsTab() ;
  }

  @Override
  public boolean processEntry(Object arg, DVBViewer.Command command)
  {
    boolean result = true ;

    if ( command == Command.UPDATE_TVBROWSER )
    {
      this.updateMarks() ;
      return true ;
    }

    Program program = (Program)arg ;

    switch ( command )
    {
    case SET :
    {
      long [] times = calcRecordTimes( program ) ;
      String channel = program.getChannel().getName() ;
      control.getDVBViewer().addNewEntry( provider, program.getUniqueID(), channel, times[0], times[1], program.getTitle() ) ;
      break ;
    }
    case FIND:
      result = findProgram( program ) != null ;
      break ;
    case DELETE :
     {
       DVBViewerEntry entry = findProgram( program ) ;
       if ( entry == null )
         return false ;
       control.getDVBViewer().deleteEntry( entry ) ;
       break ;
     }
    }

    return result ;
  }
  /**
   * @param program program which will seach in the DVBViewer recording list
   * @return null if not found otherwise the DVBViewer entry oft the recording
   */
  public DVBViewerEntry findProgram( final Program program )
  {
    DVBViewerEntry entry = null ;

    for ( DVBViewerEntry co : this.control.getDVBViewer().getRecordEntries() )
    {
      if (    co.getProvider() == provider && co.getProviderCID() != null
           && co.getProviderCID().equals( program.getUniqueID() )
           && co.isProgramEntry() )
      {
        entry = co ;
        break ;
      }
    }
    return entry ;
  }
  /**
   * 
   */
  public void updateMarks()
  {
    Program [] programs = Plugin.getPluginManager().getMarkedPrograms() ;
    for ( Program program : programs )
    {
      program.unmark( this ) ;
    }
    for ( DVBViewerEntry co : this.control.getDVBViewer().getRecordEntries() )
    {
      if (    co.getProvider() == provider && co.isProgramEntry()  && co.getProviderCID() != null )
      {
        Program program = Plugin.getPluginManager().getProgram( co.getProviderCID() ) ;
        this.mRootNode.addProgram( program ) ;
        program.validateMarking() ;
      }
    }
    this.updateTreeNode() ;
  }
  private long [] calcRecordTimes( final Program program )
  {
    calendar.clear() ;
    Date d = program.getDate() ;
    calendar.set(
        d.getYear(),
        d.getMonth()-1,
        d.getDayOfMonth(),
        program.getHours(),
        program.getMinutes() ) ;
    long startTime = calendar.getTimeInMillis() ;
    long length = program.getLength() * 1000 * 60 ;
    //System.out.println( "Date: " + new java.util.Date( startTime ) + calendar ) ;
    long endTime = startTime ;
    if ( length >= 0 )
      endTime += length ;
    else
    {
      endTime += Constants.DAYMILLSEC ;
      //Workaround if length not defined!!!
      boolean finished = false ;
      for ( int t = 0 ; t < 2  && ! finished ; t++ )
      {
        calendar.clear();
        calendar.set( d.getYear(), d.getMonth() - 1, d.getDayOfMonth() + t ) ;
        Date nd = new Date( calendar ) ;

        nd.addDays( t ) ;
        Iterator<Program> pIt = Plugin.getPluginManager().getChannelDayProgram( nd,program.getChannel() ) ;
        while ( pIt.hasNext() )
        {
          Program p = pIt.next() ;
          Date dd = p.getDate() ;
          calendar.clear() ;
          calendar.set(
              dd.getYear(),
              dd.getMonth()-1,
              dd.getDayOfMonth(),
              p.getHours(),
              p.getMinutes() ) ;
          long tmp = calendar.getTimeInMillis() ;
          if ( tmp > startTime && tmp < endTime )
          {
            endTime = tmp ;
            if ( t == 0 )
              finished = true ;
          }
        }
      }
    }
    return new long[] { startTime, endTime };
  }

  @Override
  public boolean process(boolean getAll, Command command)
  {
    return true ;
  }
  /*
  * @return the action to use for the menu and the toolbar or <code>null</code>
  *         if the plugin does not provide this feature.
  */
  @Override
  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        new TimersDialog( null, control ).init() ;
      }};

    action.putValue(Action.NAME, ResourceManager.msg( "DVBVIEWER" ) );
    action.putValue(Action.SMALL_ICON, ResourceManager.createImageIcon( "icons/dvbViewer Programm16.png", "DVBViewerTimer icon" ));
    action.putValue(BIG_ICON, ResourceManager.createImageIcon( "icons/dvbViewer Programm24.png", "DVBViewerTimer icon" ));
    action.putValue(Action.SHORT_DESCRIPTION, getInfo().getDescription());

    return new ActionMenu(action);
  }

}
