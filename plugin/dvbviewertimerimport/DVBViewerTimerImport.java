// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport;

import java.awt.event.ActionEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import dvbviewertimerimport.provider.ProviderChannel;

/**
 * @author Stefan Gollmer
 *
 */
public class DVBViewerTimerImport extends Plugin implements DVBViewerProvider
{
  private static Version version = null ;
  
  private static DVBViewerTimerImport plugin = null ;
  
  private static String ADD_TIMER    = ResourceManager.msg( "ADD_TIMER" ) ;
  private static String DELETE_TIMER = ResourceManager.msg( "DELETE_TIMER" ) ;

  private PluginInfo pluginInfo = null ;
  private boolean isInitialized = false ;
  private Control control = null ;
  private DVBViewer dvbViewer = null ;
  private GUIPanel settingsPanel = null ;

  private Icon channelChooseIcon = ResourceManager.createImageIcon( "icons/dvbViewer16.png", "DVBViewer icon" ) ;
  private Icon timerAddIcon = ResourceManager.createImageIcon( "icons/dvbViewer Timer16.png", "Timer icon" ) ;
  private Icon timerDeleteIcon = ResourceManager.createImageIcon( "icons/dvbViewer TimerDisabled 16.png", "Timer delete icon" ) ;

  private Icon menuIcon = null ;
  private Icon[] markIcons = null ;
  private String mainMenue = null ;
  
  private DVBViewerChannelChooseAction chooseChannelAction = new DVBViewerChannelChooseAction() ;
  private DVBViewerTimerAction timerAction = new DVBViewerTimerAction() ;
  

  private DVBViewerProvider dvbViewerProvider = this ;
  private int providerID ;
  private Provider provider ;

  private GregorianCalendar calendar ;

  private ProviderChannel< String >[] tvbChannelNames = null ;
  private Map< String, Channel > uniqueAssignment = null ;

  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);
  
  /**
   * 
   */
  public DVBViewerTimerImport()
  {
    DVBViewerTimerImport.plugin = this ;
  }
  protected void finalize() 
  {
    DVBViewerTimerImport.plugin = null ;
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
    if ( ! init() )
      return ;
    handleTvDataUpdateFinished() ;
  }

  @Override
  public void handleTvDataUpdateFinished()
  {
    DVBViewerTimerImport.plugin.tvbChannelNames = null ;
    try {
      this.control.getDVBViewer().process( this.dvbViewerProvider, false, (Object) null, Command.UPDATE_TVBROWSER ) ;
    } catch ( ErrorClass e ) {
      this.errorMessage(  e ) ;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (TerminateClass e) {
    }
  }
  
  private static Map< String, Channel > getUniqueAssignmentMap()
  {
    if ( DVBViewerTimerImport.plugin == null )
      return null ;
    if ( DVBViewerTimerImport.plugin.uniqueAssignment == null )
      getTVBChannelNames() ;
    return DVBViewerTimerImport.plugin.uniqueAssignment ;
  }
  


  /**
   * @return Array containing the channel names of the TV-Browser
   */
  public static ProviderChannel<String>[] getTVBChannelNames()
  {
    if ( DVBViewerTimerImport.plugin == null )
      return null ;
    
    if ( DVBViewerTimerImport.plugin.tvbChannelNames != null )
      return DVBViewerTimerImport.plugin.tvbChannelNames ;

    devplugin.Channel[] channels = devplugin.Plugin.getPluginManager().getSubscribedChannels();
    
    DVBViewerTimerImport.plugin.uniqueAssignment = new HashMap< String, Channel >() ;

    @SuppressWarnings("unchecked")
    ProviderChannel<String>[] newInstance = (ProviderChannel<String>[]) Array.newInstance( new ProviderChannel< String >("","").getClass(), channels.length );
    DVBViewerTimerImport.plugin.tvbChannelNames = newInstance ;

    int idx = -1 ;
    Map< String, List< String > > checkMap = new HashMap< String, List< String > >() ;
    for ( devplugin.Channel c : channels )
    {
      String key = c.getName() ;
      if ( ! checkMap.containsKey( key ) )
        checkMap.put( key, new ArrayList< String >() ) ;
      checkMap.get( key ).add( c.getUniqueId() ) ;
      DVBViewerTimerImport.plugin.uniqueAssignment.put( c.getUniqueId(), c ) ;
    }
    for ( Map.Entry< String, List< String > > mapEntry : checkMap.entrySet() )
    {
      List<String> list = mapEntry.getValue() ;
      DVBViewerTimerImport.plugin.tvbChannelNames[ ++idx ] = new ProviderChannel< String >( mapEntry.getKey(), mapEntry.getValue().get(0) ) ;
      if ( list.size() > 1 )
      {
        int cnt = 1 ;
        for ( cnt = 1 ; cnt < list.size() ; ++cnt )
          DVBViewerTimerImport.plugin.tvbChannelNames[ ++idx ] = new ProviderChannel< String >( mapEntry.getKey() + "(" + Integer.toString( cnt ) + ")", list.get( cnt ) ) ;
      }
    }
    return DVBViewerTimerImport.plugin.tvbChannelNames ;
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
      version = new Version(v[0], v[1], v[2], v[3]==0 ) ;
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
        dvbChannel = dvbViewer.getDVBViewerChannel( provider.getID(), tvBChannel.getName() ) ;
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

    public void update( final Program program, final Command command )
    {
      this.program = program ;
      this.command = command ;
      if ( command == Command.SET )
      {
        putValue(Action.NAME, DVBViewerTimerImport.ADD_TIMER ) ;
        putValue(Action.SMALL_ICON, timerAddIcon ) ;
      }
      else
      {
        putValue(Action.NAME, DVBViewerTimerImport.DELETE_TIMER ) ;
        putValue(Action.SMALL_ICON, timerDeleteIcon ) ;
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
      markProgram( program, command == Command.SET ) ;

     program.validateMarking() ;
     updateTreeNode();
    }
    
  }

  
  public ActionMenu getContextMenuActions( final Program program)
  {
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
    
    return new ActionMenu( mainMenue, menuIcon, subActions ) ;
    }

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
      settingsPanel.updateTab() ;
      control.write( null ) ;
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
    
    if ( command == Command.UPDATE_UNRESOLVED_ENTRIES )
    {
      int end = this.control.getDVBViewer().getRecordEntries().size() ;
      for ( int i = 0 ; i < end ; ++i )
      {
        DVBViewerEntry co = this.control.getDVBViewer().getRecordEntries().get(i) ;
        if (    co.getProvider() == provider && co.isProgramEntry()  && co.getProviderCID() != null )
        {
          Program program = Plugin.getPluginManager().getProgram( co.getProviderCID() ) ;
          if ( program != null )
            continue ;
          Program pgm = this.searchBestFit( co ) ;
          if ( pgm == null )
            continue ;
          long [] times = calcRecordTimes( pgm ) ;
          String channel = pgm.getChannel().getName() ;
          dvbViewer.shiftEntry( co, provider, pgm.getUniqueID(), channel, times[0], times[1], pgm.getTitle() ) ;
          this.markProgram( pgm, true ) ;
          pgm.validateMarking() ;
          continue ;
        }
      }
      return true ;
    }

    Program program = (Program)arg ;

    switch ( command )
    {
    case SET :
    {
      long [] times = calcRecordTimes( program ) ;
      String channel = program.getChannel().getName() ;
      dvbViewer.addNewEntry( provider, program.getUniqueID(), channel, times[0], times[1], program.getTitle() ) ;
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
       dvbViewer.deleteEntry( entry ) ;
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
      this.markProgram( program, false ) ;
    }
    
    boolean unresolvedEntries = false ;
    for ( DVBViewerEntry co : this.control.getDVBViewer().getRecordEntries() )
    {
      if (    co.getProvider() == provider && co.isProgramEntry()  && co.getProviderCID() != null )
      {
        Program program = Plugin.getPluginManager().getProgram( co.getProviderCID() ) ;
        if ( program == null )
        {
          unresolvedEntries = true ;
          continue ;
        }
        this.markProgram( program, true ) ;
        program.validateMarking() ;
      }
    }
    
    if ( unresolvedEntries )
    {
      try {
        dvbViewer.process( dvbViewerProvider, false, null, Command.UPDATE_UNRESOLVED_ENTRIES ) ;
      } catch ( ErrorClass err ) {
        errorMessage( err ) ;
        return;
      } catch (Exception err ) {
          errorMessage( err ) ;
          err.printStackTrace();
          return ;
      } catch (TerminateClass err) {
          return ;
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
  /**
   * @param entries
   */
  public static void updateRecordings( ArrayList< DVBViewerEntry > entries )
  {
    if ( DVBViewerTimerImport.plugin != null)
      DVBViewerTimerImport.plugin.updateMarks() ;
  }
  @Override
  public boolean canReceiveProgramsWithTarget()
  {
      return getProgramReceiveTargets().length >0;
  }
  @Override
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
      if (receiveTarget == null || receiveTarget.getTargetId() == null )
        return false;

      String id = receiveTarget.getTargetId();
      
       try {
         if ( id.equals( "RECORD" ) )
           dvbViewer.process( dvbViewerProvider, false, programArr, DVBViewer.Command.SET ) ;
         else if ( id.equals( "REMOVE" ) )
           dvbViewer.process( dvbViewerProvider, false, programArr, DVBViewer.Command.DELETE ) ;
         else
           return false ;
      } catch ( ErrorClass e ) {
         errorMessage( e ) ;
         return false ;
       }
       catch (Exception e ) {
         errorMessage( e ) ;
         e.printStackTrace();
         return false ;
       } catch (TerminateClass e) {
         return false ;
       }
       updateMarks() ;
       return true ;
}

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets()
  {
      ProgramReceiveTarget ADD_TARGET    = new ProgramReceiveTarget( this, DVBViewerTimerImport.ADD_TIMER,    "RECORD" ) ;
      ProgramReceiveTarget REMOVE_TARGET = new ProgramReceiveTarget( this, DVBViewerTimerImport.DELETE_TIMER, "REMOVE" ) ;

      ProgramReceiveTarget [] targets = { ADD_TARGET, REMOVE_TARGET } ;
      
      return targets ;
  }

  public Program searchBestFit( DVBViewerEntry entry )
  {
    Program result = null ;
    Date [] interval = new Date[ 2 ] ;
    
    long startTimeOrg = entry.getStartOrg() ;
    
    long startSearch = startTimeOrg - 1000 * 60 * 60 * 3 ;
    long endSearch = startTimeOrg + 1000 * 60 * 60 * 3 ;
    
    calendar.clear() ;
    calendar.setTimeInMillis( startSearch ) ;
    interval[ 0 ] = new Date( calendar ) ;
    
    calendar.clear() ;
    calendar.setTimeInMillis( endSearch ) ;
    interval[ 1 ] = new Date( calendar ) ;
        
    int end = 2 ;
    
    if ( interval[ 0 ].getDayOfMonth() == interval[ 1 ].getDayOfMonth() )
      end = 1 ;
    
    String uniqueName = (String) entry.getChannelSet().getChannel( this.providerID ).getIDKey() ;
    Channel channel = DVBViewerTimerImport.getUniqueAssignmentMap().get(uniqueName) ;
    
    if ( channel == null )
      return null ;
    
    Collection< Program > possibilities = new ArrayList< Program >() ;
    
    for ( int i = 0 ; i < end ; ++i )
    {
      Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram( interval[ i ], channel) ;
      for ( ; it.hasNext() ; )
      {
        Program pgm = it.next() ;
        if ( pgm.getTitle().equals( entry.getTitle() ) )
          possibilities.add( pgm ) ;
      }
    }
    if ( possibilities.size() == 0 )
      return null ;
    
    long deltaMin = startTimeOrg ;
    
    for ( Iterator< Program > it = possibilities.iterator() ; it.hasNext() ; )
    {
      Program pgm = it.next() ;
      Date dd = pgm.getDate() ;
      calendar.clear() ;
      calendar.set(
          dd.getYear(),
          dd.getMonth()-1,
          dd.getDayOfMonth(),
          pgm.getHours(),
          pgm.getMinutes() ) ;

      long delta  = Math.abs( calendar.getTimeInMillis() - startTimeOrg ) ;
      if ( result == null || deltaMin > delta )
      {
        result = pgm ;
        deltaMin = delta ;
      }
    }
    return result ;
  }
}
