// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.main ;


import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.TerminateClass;
import dvbviewertimerimport.provider.Provider;
import dvbviewertimerimport.dvbviewer.DVBViewer ;
import dvbviewertimerimport.control.Control ;
import dvbviewertimerimport.gui.GUI;
import dvbviewertimerimport.gui.WorkPathSelector;
import dvbviewertimerimport.gui.GUI.GUIStatus;

public final class TimerImportTool {
	static private final String exeName = "TimerImportTool" ;
	private enum ImportType{ GUI, TVINFO, CLICKFINDER, TVGENIAL, UPDATE } ;
	public static void main(String[] args) {

		DVBViewer dvbViewer = null ;
		
        try {
        	//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        	//UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        	//UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel" ) ;
        	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ImportType type    = ImportType.GUI ;
		boolean showMessageBox = false ;

		Provider provider = null ;

		try {
			boolean getAll = false ;

			String paras = "" ;
			String iniPath = null ;

			for ( int i = 0 ; i < args.length ; i++ )
			{
				paras += args[i] + " " ;
				if (      args[i].equalsIgnoreCase("-getAll") )
					getAll = true ;
				else if ( args[i].equalsIgnoreCase("-verbose") )
					Log.setVerbose( true ) ;
				else if ( args[i].equalsIgnoreCase("-ClickFinder") )
					type = ImportType.CLICKFINDER ;
				else if ( args[i].equalsIgnoreCase("-TVInfo") )
					type = ImportType.TVINFO ;
				else if ( args[i].equalsIgnoreCase("-TVGenial") )
					type = ImportType.TVGENIAL ;
				else if ( args[i].equalsIgnoreCase("-message") )
					showMessageBox = true ;
				else if ( args[i].equalsIgnoreCase("-iniPath") )
				{
					i++ ;
					if ( i >= args.length )
						throw new ErrorClass( "A directory path is necessary after the flag -path" ) ;
					iniPath = args[ i ] ;
				}
				else if ( args[i].equalsIgnoreCase("-update") )
					type = ImportType.UPDATE ;
			}

			Log.setToDisplay(true);

			dvbViewer = new DVBViewer( iniPath, TimerImportTool.exeName ) ;

			while ( ! dvbViewer.initDataPath() )
			{
				boolean aborted = ! ( new WorkPathSelector( dvbViewer , null)).show() ;
				if ( aborted )
					System.exit( 0 ) ;
				dvbViewer.setPathFileIsUsed( true ) ;
			}

			Control control = new Control(dvbViewer);

			//Log.setVerbose( true ) ;

			if ( type == ImportType.GUI )
			{
				Log.out( "Open configuration" ) ;
				GUI gui = new GUI( control ) ;
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
						dvbViewer.writeDataPathToProperties() ;
						break ;
					case OK :
						control.write() ;
						Log.out( "Configuration saved and terminated" ) ;
						dvbViewer.writeDataPathToProperties() ;
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
						dvbViewer.writeDataPathToProperties() ;
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
		} catch ( TerminateClass e ) {
			System.exit( e.getExitCode() );
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

}
