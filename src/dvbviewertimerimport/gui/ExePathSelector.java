// $LastChangedDate: 2010-05-16 16:44:29 +0200 (So, 16 Mai 2010) $
// $LastChangedRevision: 308 $
// $LastChangedBy: Stefan Gollmer $

package dvbviewertimerimport.gui;

import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.misc.ResourceManager;

//import JFileChooser;

public class ExePathSelector
{
	JFileChooser chooser = new JFileChooser() ;
	
	private final DVBViewer dvbViewer ;
	private final Window window ;
	
	public ExePathSelector( final DVBViewer dvbViewer, final Window window )
	{
		this.dvbViewer = dvbViewer ;
		this.window = window ;
	}
	public boolean show()
	{
		File exeFile = null ;
		String path = this.dvbViewer.getDVBExePath() ;
		exeFile = new File( path ) ;
		this.chooser.setCurrentDirectory( exeFile ) ;
		this.chooser.setDialogTitle( ResourceManager.msg( "SELECT_EXECUTABLLE" ) ) ;
		this.chooser.setFileSelectionMode( JFileChooser.FILES_ONLY ) ;
		this.chooser.setFileFilter( new FileFilter(){

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".exe") || f.isDirectory() ;
			}

			@Override
			public String getDescription() {
				// TODO Auto-generated method stub
				return ResourceManager.msg( "PROGRAMS" ) ;
			}} ) ;
		int returnVal = this.chooser.showDialog( this.window, ResourceManager.msg( "SELECT" ) ) ;
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = this.chooser.getSelectedFile() ;

			this.dvbViewer.setDVBExePath( file.getAbsolutePath() ) ;
			return true ;
		}
		else
			return false ;
	}
}
