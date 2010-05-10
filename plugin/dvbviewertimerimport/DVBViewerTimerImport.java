package dvbviewertimerimport;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import devplugin.ActionMenu;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;
import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;
import dvbviewertimerimport.dvbviewer.DVBViewerProvider;
import dvbviewertimerimport.dvbviewer.DVBViewer.Command;
import dvbviewertimerimport.dvbviewer.channels.Channels;
import dvbviewertimerimport.gui.GUI;
import dvbviewertimerimport.gui.GUIPanel;
import dvbviewertimerimport.gui.WorkPathSelector;
import dvbviewertimerimport.gui.GUI.GUIStatus;
import dvbviewertimerimport.main.TimerImportTool;
import dvbviewertimerimport.main.Versions;
import dvbviewertimerimport.misc.Constants;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.provider.Provider;
import dvbviewertimerimport.tvbrowser.TVBrowser;

/**
 * @author Stefan Gollmer
 *
 */
public class DVBViewerTimerImport extends Plugin implements DVBViewerProvider
{
  private static Version version = null ;
  static private final String exeName = "TimerImportTool" ;

  private PluginInfo pluginInfo = null ;
  private boolean isInitialized = false ;
  private Control control = null ;
  private DVBViewer dvbViewer = null ;
  private Channels channels = null ;
  private GUIPanel settingsPanel = null ;
  
  private Icon menuIcon = null ;
  private String deleteTimer = null ;
  private String addTimer = null ;
  
  private DVBViewerProvider dvbViewerProvider = this ;
  private int providerID ;
  private Provider provider ;
  
  private GregorianCalendar calendar ;
  
  private HashMap< String, String > channelAssignmentDvbVToTvB = null ;
  
  public DVBViewerTimerImport()
  {
//    init() ;
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
    init() ;
    handleTvDataUpdateFinished() ;
  }
  
  @Override
  public void handleTvDataUpdateFinished()
  {
    try {
      this.control.getDVBViewer().process( this.dvbViewerProvider, false, null, Command.UPDATE ) ;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


  
  public static String[] getTVBChannelNames()
  {
    ArrayList< String > res = new ArrayList< String >() ;
    devplugin.Channel[] channels = devplugin.Plugin.getPluginManager().getSubscribedChannels();
    for ( devplugin.Channel c : channels )
      res.add( c.getName() ) ;
    return res.toArray( new String[0] ) ;
  }
  
  
  
  private void init()
  {
    if ( isInitialized )
      return ;

    isInitialized = true ;
    boolean showMessageBox = false ;

    try {
      Log.setToDisplay(true);

      dvbViewer = new DVBViewer( "C:\\Programme\\AudioVideo\\DVBViewer", exeName ) ;

      while ( ! dvbViewer.initDataPath() )
      {
        boolean aborted = ! ( new WorkPathSelector( dvbViewer , null)).show() ;
        if ( aborted )
          System.exit( 0 ) ;
        dvbViewer.setPathFileIsUsed( true ) ;
      }

     control = new Control(dvbViewer) ;
    } catch (ErrorClass e) {
      Log.error(e.getLocalizedMessage());
      Log.out("Import terminated with errors" ) ;
      System.exit(1);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      System.exit(2);
    }
    this.provider = Provider.getProvider( "TV-Browser" ) ;
    this.providerID = provider.getID() ;
    this.calendar = new GregorianCalendar( this.provider.getTimeZone() ) ;

    this.menuIcon = ResourceManager.createImageIcon( "icons/dvbViewer Programm16.png", "DVBViewer icon" ) ;
    this.deleteTimer = ResourceManager.msg( "DELETE_TIMER" ) ;
    this.addTimer = ResourceManager.msg( "ADD_TIMER" ) ;

    ((TVBrowser)this.provider).setIsTVBrowserPlugin() ;
   }
    /*
      //Log.setVerbose( true ) ;

      Log.out( "Open configuration" ) ;
      GUI gui = new GUI( control, channels ) ;
      gui.execute() ;
      boolean finished = false ;
      while ( ! finished )
    {
      GUIStatus status = gui.waitGUI() ;

      switch (status)
      {
      case APPLY :
        Log.out( "Configuration saved" ) ;
        control.write() ;
        dvbViewer.writeDataPathToIni() ;
        break ;
      case OK :
        control.write() ;
        Log.out( "Configuration saved and terminated" ) ;
        dvbViewer.writeDataPathToIni() ;
        System.exit( 0 ) ;
        break ;
      case CANCEL :
        Log.out( "Configuration aborted" ) ;
        System.exit( 0 ) ;
        break ;
      case SAVE_EXECUTE :
        Log.out( "Configuration saved" ) ;
        control.write() ;
      case EXECUTE :
        Log.out( "Execute import started" ) ;
        provider = Provider.getProvider( control.getDefaultProvider() ) ;
        dvbViewer.writeDataPathToIni() ;
        finished = true ;
        break ;
      case UPDATE :
        Log.out( "Timer update started" ) ;
        dvbViewer.updateDVBViewer() ;
      }
    }
  }

  switch ( type )
  {
    case TVINFO :
      provider = Provider.getProvider( "TVInfo" ) ;
      provider.setFilter( ! getAll ) ;
      break ;
    case CLICKFINDER :
      provider = Provider.getProvider( "ClickFinder" ) ;
      break ;
    case TVGENIAL :
      provider = Provider.getProvider( "TVGenial" ) ;
      break ;
    case UPDATE :
      dvbViewer.updateDVBViewer() ;
  }


  if ( provider != null )
  {
    showMessageBox |= provider.getMessage() ;
    if ( provider.getVerbose() )
      Log.setVerbose( true ) ;
  }

  Log.setToDisplay(showMessageBox || type == ImportType.CLICKFINDER );

  if ( paras.length() != 0)
    Log.out( "Parameters: " + paras ) ;

  dvbViewer.process( provider, getAll, args ) ;

} catch (ErrorClass e) {
  Log.error(e.getLocalizedMessage());
  Log.out("Import terminated with errors" ) ;
  System.exit(1);
} catch (Exception e) {
  // TODO Auto-generated catch block
  e.printStackTrace();
  System.exit(2);
}
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
  public static Version getVersion()
  {
    if ( version == null )
    {
      int[] v = Versions.getIntVersion() ;
      version = new Version(v[0], v[1], v[2], false) ;
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
    Icon i = ResourceManager.createImageIcon( "icons/dvbViewer Programm16.png", "DVBViewer icon" ) ;
    return new Icon[] {i} ;
  }
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
  
  
  
  @Override
  public ActionMenu getContextMenuActions( final Program program)
  {
    init() ;
    // Eine Aktion erzeugen, die die Methode sendMail(Program) aufruft, sobald sie aktiviert wird.
    Command temp = Command.SET ;
    try {
      if ( control.getDVBViewer().process( dvbViewerProvider, false, program, Command.FIND ) )
        temp = Command.DELETE ;
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    final Command command = temp ;
    AbstractAction action = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        control.setDVBViewerEntries() ;
        try {
          control.getDVBViewer().process( dvbViewerProvider, false, program, command ) ;
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        if ( dvbViewer != null )
          dvbViewer.writeXML() ;
        if ( command == Command.SET )
          markProgram( program, true ) ;
        else
          markProgram( program, false ) ;
        program.validateMarking() ;
      }
    };
    
    // Der Aktion einen Namen geben. Dieser Name wird dann im Kontextmen� gezeigt
    if ( command == Command.SET )
        action.putValue(Action.NAME, addTimer );
      else
        action.putValue(Action.NAME, deleteTimer );

    // Der Aktion ein Icon geben. Dieses Icon wird mit dem Namen im Kontextmen� gezeigt
    // Das Icon sollte 16x16 Pixel gro� sein
    action.putValue(Action.SMALL_ICON, menuIcon );

    // Das Aktions-Men� erzeugen und zur�ckgeben
    return new ActionMenu(action); 
  }
  
  private void markProgram( final Program program, boolean mark )
  {
    if ( mark )
      program.mark( this ) ;
    else
      program.unmark( this ) ;
  }
  
  class DVBVSettingsTab implements SettingsTab
  {

    @Override
    public JPanel createSettingsPanel()
    {
      init() ;
      if ( settingsPanel == null)
      {
        settingsPanel = new GUIPanel( control ) ;
        settingsPanel.paint() ;
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
      init() ;
      Log.out( "Configuration saved" ) ;
      control.write() ;
      dvbViewer.writeDataPathToIni() ;
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
    
    if ( command == Command.UPDATE )
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
       long [] times = calcRecordTimes( program ) ;
       DVBViewerEntry entry = findProgram( program ) ;
       if ( entry == null )
         return false ;
       control.getDVBViewer().deleteEntry( entry ) ;
       break ;
     }
    }

    return result ;
  }
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
  public void updateMarks()
  {
    Program [] programs = this.getPluginManager().getMarkedPrograms() ;
    for ( Program program : programs )
    {
      program.unmark( this ) ;
    }
    for ( DVBViewerEntry co : this.control.getDVBViewer().getRecordEntries() )
    {
      if (    co.getProvider() == provider && co.isProgramEntry()  && co.getProviderCID() != null )
      {
        Program program = this.getPluginManager().getProgram( co.getProviderCID() ) ;
        program.mark( this ) ;
      }
    }
  }
  private long [] calcRecordTimes( final Program program )
  {
    long [] result = new long[ 2 ] ;
    calendar.clear() ;
    Date d = program.getDate() ;
    calendar.set(
        d.getYear(),
        d.getMonth()-1,
        d.getDayOfMonth(),
        program.getHours(),
        program.getMinutes() ) ;
    result[0]= calendar.getTimeInMillis() ;
    long length = program.getLength() * 1000 * 60 ;
    System.out.println( "Date: " + new java.util.Date( result[0] ) + calendar ) ;
    result[1] = result[0] ;
    if ( length >= 0 )
      result[1] += length ;
    else
      result[1] += Constants.DAYMILLSEC ;
    return result ;
  }

  @Override
  public boolean process(boolean getAll, Command command)
  {
    return true ;
  }
}
