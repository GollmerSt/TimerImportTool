// $LastChangedDate: 2010-03-22 14:41:19 +0100 (Mo, 22. Mrz 2010) $
// $LastChangedRevision: 201 $
// $LastChangedBy: Stefan Gollmer $

package dvbviewertimerimport.gui;

import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.filechooser.FileFilter;

import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.misc.ResourceManager;

//import JFileChooser;

public class WorkPathSelector
{
	private final static String CHANNEL_FILE = "channels.dat" ;
	
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
		chooser.setCurrentDirectory( null ) ;
		chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.getName().equalsIgnoreCase( CHANNEL_FILE ) || f.isDirectory();
            }
            public String getDescription() {
                return "DVBViewer channel file";
            }
		}
		);
		chooser.setDialogTitle( ResourceManager.msg( "SELECT_CHANNEL_FILE" ) ) ;
		int returnVal = chooser.showDialog( window, ResourceManager.msg( "SELECT" ) ) ;
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File file = chooser.getSelectedFile() ;
            
            dvbViewer.setDataPath( file.getParent() ) ;
            return true ;
        }
        else
        	return false ;
	}
}
