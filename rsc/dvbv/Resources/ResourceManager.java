// $LastChangedDate: 2010-02-02 20:15:15 +0100 (Di, 02. Feb 2010) $
// $LastChangedRevision: 79 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.gui;

import javax.swing.ImageIcon;

public class ResourceManager {
	private static ResourceManager rsc = new ResourceManager() ;
    public static ImageIcon createImageIcon(String path,
            String description)
    {
    	java.net.URL imgURL = rsc.getClass().getResource(path);
    	if ( imgURL != null )
    		return new ImageIcon(imgURL, description);
    	else
    	{
    		System.err.println("Couldn't find file: " + path);
    		return null;
    	}
    }
}
