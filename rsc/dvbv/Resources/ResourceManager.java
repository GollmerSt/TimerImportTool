// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.Resources;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

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
    public static void copyFile( String destinationPath, String source )
    {
    	ResourceManager.copyFile( destinationPath, source, null ) ;
    }

        public static void copyFile( String destinationPath, String source, ArrayList< String[] > keywords )
    {
		InputStream is = ResourceManager.createInputStream( source ) ;
		
		String[] parts = source.split( "\\/" ) ;
		
		String destination = destinationPath + File.separator + parts[ parts.length-1 ] ;
		
		File file = new File( destination ) ;
		
		BufferedReader bufR = new BufferedReader( new InputStreamReader( is ) ) ;
		
		FileWriter fstream = null ;
		try {
			fstream = new FileWriter( destination, false );
			BufferedWriter bufW = new BufferedWriter( fstream ) ;
		
			String line = null ;
			
			String lineSeparator = System.getProperty("line.separator") ;
		
			while ( ( line = bufR.readLine() ) != null )
			{
				if ( keywords != null )
					for ( Iterator< String[] > it = keywords.iterator() ; it.hasNext() ; )
					{
						String [] keyPair = it.next() ;
						int pos = 0 ;
						while ( ( pos = line.indexOf( keyPair[0], pos ) ) >= 0 )
						{
							line = line.substring( 0, pos) + keyPair[ 1 ] + line.substring( pos + keyPair[0].length() ) ;
							pos += keyPair[1].length() ;
						}
					}
				line += lineSeparator ;
				bufW.write( line ) ;
			}
			bufR.close() ;
			bufW.close() ;
		} catch (IOException e) {
			throw new ErrorClass(   "Unexpected error on writing file \"" 
					              + file.getAbsolutePath()
					              + "\"." ) ;
		}
    }
    public static void copyBinaryFile( String destinationPath, String source )
    {
		InputStream istream = ResourceManager.createInputStream( source ) ;
		
		String[] parts = source.split( "\\/" ) ;
		
		String destination = destinationPath + File.separator + parts[ parts.length-1 ] ;
		
		File file = new File( destination ) ;
		
		
		FileOutputStream ostream = null ;
		try {
			ostream = new FileOutputStream( file );
		} catch (FileNotFoundException e1) {
			throw new ErrorClass(   "Unexpected error on writing file \"" 
		              + file.getAbsolutePath()
		              + "\"." ) ;
		}
		try {
			byte [] buffer = new byte[1024] ;
			
			int length = 0 ;
			
			while ( ( length = istream.read( buffer ) ) > 0 )
				ostream.write( buffer, 0, length ) ;
			
			istream.close() ;
			ostream.close() ;
		} catch (IOException e) {
			throw new ErrorClass(   "Unexpected error on writing file \"" 
					              + file.getAbsolutePath()
					              + "\"." ) ;
		}

    }
}