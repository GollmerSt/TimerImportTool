// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.Resources;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    public static void copyFile( File destination, String source )
    {
		InputStream is = ResourceManager.createInputStream( source ) ;
		
		BufferedReader bufR = new BufferedReader( new InputStreamReader( is ) ) ;
		
		FileWriter fstream = null ;
		try {
			fstream = new FileWriter( destination, true );
			BufferedWriter bufW = new BufferedWriter( fstream ) ;
		
			String line = null ;
		
			while ( ( line = bufR.readLine() ) != null )
				bufW.write( line ) ;
			bufR.close() ;
			bufW.close() ;
		} catch (IOException e) {
			throw new ErrorClass(   "Unexpected error on writing file \"" 
					              + destination.getAbsolutePath()
					              + "\"." ) ;
		}

    }
}