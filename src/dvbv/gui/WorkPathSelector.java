// $LastChangedDate: 2010-03-22 14:41:19 +0100 (Mo, 22. Mrz 2010) $
// $LastChangedRevision: 201 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import dvbv.dvbviewer.DVBViewer;

//import JFileChooser;

public class WorkPathSelector
{
	private final static String CHANNEL_FILE = "channels.dat" ;
	
	JFileChooser chooser = new JFileChooser() ;
	
	private final DVBViewer dvbViewer ;
	private final JFrame frame ;
	
	public WorkPathSelector( final DVBViewer dvbViewer, final JFrame frame )
	{
		this.dvbViewer = dvbViewer ;
		this.frame = frame ;
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
		chooser.setDialogTitle( GUIStrings.SELECT_CHANNEL_FILE.toString() ) ;
		int returnVal = chooser.showDialog( frame, GUIStrings.SELECT.toString() ) ;
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
