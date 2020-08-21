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
		String path = this.dvbViewer.getDVBViewerPath() ;
		if ( path != null )
			pathFile = new File( path ) ;
		this.chooser.setCurrentDirectory( pathFile ) ;
		this.chooser.setDialogTitle( ResourceManager.msg( "SELECT_DVBVIEWER_DIRECTORY" ) ) ;
		this.chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY ) ;
		int returnVal = this.chooser.showDialog( this.window, ResourceManager.msg( "SELECT" ) ) ;
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = this.chooser.getSelectedFile() ;

			this.dvbViewer.setDVBViewerPath( file.getAbsolutePath() ) ;
			return true ;
		}
		else
			return false ;
	}
}
