// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$


import javax.swing.JOptionPane;


public final class TimerImportTool {
	static private final String exeName = "TimerImportTool" ;
	private enum ImportType{ TVINFO, CLICKFINDER } ;
	private enum InstallMode{ NONE, INSTALL, DEINSTALL } ;
	public static void main(String[] args) {
		
		ImportType type    = ImportType.TVINFO ;
		InstallMode install = InstallMode.NONE ;
		boolean showMessageBox = false ;
		String additionalParas = "" ;
		//WakeOnLan.execute("192.168.0.255", "00:1F:D0:20:CB:3A" ) ;
		try {
			boolean allNew = false ;
			
			String paras = "" ;
			String dataPath = null ;
			
			for ( int i = 0 ; i < args.length ; i++ )
			{
				paras += args[i] + " " ;
				if (      args[i].equalsIgnoreCase("-new") )
					allNew = true ;
				else if ( args[i].equalsIgnoreCase("-verbose") )
					Log.setVerbose( true ) ;
				else if ( args[i].equalsIgnoreCase("-ClickFinder") )
					type = ImportType.CLICKFINDER ;
				else if ( args[i].equalsIgnoreCase("-install") )
					install = InstallMode.INSTALL ;
				else if ( args[i].equalsIgnoreCase("-deinstall") )
					install = InstallMode.DEINSTALL ;
				else if ( args[i].equalsIgnoreCase("-message") )
				{
					showMessageBox = true ;
					additionalParas += "-message" ;
				}
				else if ( args[i].equalsIgnoreCase("-path") )
				{
					i++ ;
					if ( i >= args.length )
						throw new ErrorClass( "A directory path is necessary after the flag -path" ) ;
					dataPath = args[ i ] ;
				}
			}
			
			Log.setToDisplay(true);

			DVBViewer dvbViewer = new DVBViewer( dataPath, TimerImportTool.exeName ) ;
			Control control = new Control(dvbViewer);
			
			Log.setToDisplay(showMessageBox);
			
			if ( paras.length() != 0)
				Log.out( "Parameters: " + paras ) ;

			switch ( type )
			{
				case TVINFO :
					AllTVInfoRecordings aR = new AllTVInfoRecordings(control, dvbViewer, 3, 3);
					aR.processTVInfo(allNew);
					dvbViewer.setDVBViewerTimers();
					aR.write() ;
					break ;
				case CLICKFINDER :
				{
					ClickFinder click = new ClickFinder( dvbViewer ) ;

					switch ( install )
					{
						case NONE :
							click.processEntry( args ) ;
							dvbViewer.setDVBViewerTimers();
							break ;
						case INSTALL :
							click.putToRegistry( dataPath != null, additionalParas ) ;
							break ;
						case DEINSTALL :
							click.removeFromRegistry() ;
							break ;
					}
					break ;
				}
			}
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
			if ( showMessageBox )
				JOptionPane.showMessageDialog(null, "Warnings occurs", TimerImportTool.exeName + "-Status", JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			Log.out( "Import successfull finished" ) ;
			if ( showMessageBox )
				JOptionPane.showMessageDialog(null, "Successfull finished", TimerImportTool.exeName + "-Status", JOptionPane.INFORMATION_MESSAGE);
		}
		System.exit(0);
	}

}
