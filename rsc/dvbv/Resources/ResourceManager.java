// $LastChangedDate: 2010-02-02 20:15:15 +0100 (Di, 02. Feb 2010) $
// $LastChangedRevision: 79 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.Resources;

import java.io.InputStream;

import javax.swing.ImageIcon;

import dvbv.misc.ErrorClass;

public class ResourceManager {
	private static ResourceManager rsc = new ResourceManager() ;
    public static ImageIcon createImageIcon(String path,
            String description)
    {
    	java.net.URL imgURL = rsc.getClass().getResource(path);
    	if ( imgURL != null )
    		return new ImageIcon(imgURL, description);
    	else
    		throw new ErrorClass( "Package error: Icon file \"" + path + "\" not found" ) ;
    }
    public static InputStream createInputStream( String path )
    {
    	InputStream inputStream = null;
    	try
    	{
    		inputStream = rsc.getClass().getResourceAsStream( path ) ;
    	} catch (Exception e) {
    	      e.printStackTrace();
        }
    	
    	if ( inputStream == null )
    		throw new ErrorClass( "Package error: File \"" + path + "\" not found" ) ;

    	return inputStream ;
    }
}