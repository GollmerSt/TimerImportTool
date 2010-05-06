package dvbviewertimerimport;

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.dvbviewer.channels.Channels;
import dvbviewertimerimport.gui.GUI;
import dvbviewertimerimport.gui.GUIPanel;
import dvbviewertimerimport.gui.WorkPathSelector;
import dvbviewertimerimport.gui.GUI.GUIStatus;
import dvbviewertimerimport.main.TimerImportTool;
import dvbviewertimerimport.main.Versions;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.provider.Provider;
import dvbviewertimerimport.tvbrowser.TVBrowser;

/**
 * @author Stefan Gollmer
 *
 */
public class DVBViewerTimerImport extends Plugin
{
  private static Version version = null ;
  static private final String exeName = "TimerImportTool" ;

  private PluginInfo pluginInfo = null ;
  private boolean isInitialized = false ;
  private Control control = null ;
  private DVBViewer dvbViewer = null ;
  private Channels channels = null ;
  private GUIPanel settingsPanel = null ;
  
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

    Provider provider = null ;

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

      this.channels = new Channels( dvbViewer ) ;
      this.channels.read() ;

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

  control.setDVBViewerEntries() ;

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
if ( dvbViewer != null )
  dvbViewer.writeXML() ;
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
  
  class DVBVSettingsTab implements SettingsTab
  {

    @Override
    public JPanel createSettingsPanel()
    {
      init() ;
      if ( settingsPanel == null)
      {
        settingsPanel = new GUIPanel( control, channels ) ;
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
    }    
  }
  public SettingsTab getSettingsTab()
  {
    return new DVBVSettingsTab() ;
  }
}
