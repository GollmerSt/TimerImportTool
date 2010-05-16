// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.gui;

import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;

import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.misc.ResourceManager;

//import JFileChooser;

public class WorkPathSelector
{
	JFileChooser chooser = new JFileChooser() ;
	
	private final DVBViewer dvbViewer ;
	private final Window window ;
	
	public WorkPathSelector( final DVBViewer dvbViewer, final Window window )
	{
		this.dvbViewer = dvbViewer ;
		this.window = window ;
	}
	public boolean show()
	{
		File pathFile = null ;
		String path = dvbViewer.getDVBViewerPath() ;
		if ( path != null )
			pathFile = new File( path ) ;
		chooser.setCurrentDirectory( pathFile ) ;
		chooser.setDialogTitle( ResourceManager.msg( "SELECT_DVBVIEWER_DIRECTORY" ) ) ;
		chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY ) ;
		int returnVal = chooser.showDialog( window, ResourceManager.msg( "SELECT" ) ) ;
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile() ;

			dvbViewer.setDVBViewerPath( file.getAbsolutePath() ) ;
			return true ;
		}
		else
			return false ;
	}
}
