// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;


public class ResourceManager {

	private static final String RESOURCE_BASE_PATH = "/dvbviewertimerimport/resources/" ;
	private static final String DATA_PATH = "datafiles/" ;

	private static ResourceBundle resourceBundleLang = null ;

	private static ResourceManager rsc = new ResourceManager() ;
	
	private static boolean isMsgInProcess = false ;



	public static java.net.URL getURL( String path )
	{
		java.net.URL u = rsc.getClass().getResource( RESOURCE_BASE_PATH + path ) ;
		//System.out.println( "path = " + RESOURCE_BASE_PATH + path + "   URL = " + u ) ;
		return u ;
	}

	public static ImageIcon createImageIcon(String path, String description)
	{
		java.net.URL imgURL = getURL( path );
		if ( imgURL != null )
			return new ImageIcon(imgURL, description);
		else
			throw new ErrorClass( "Package error: Icon file \"" + path + "\" not found" ) ;
	}
	public static InputStream createInputStream( String path )
	{
		return createInputStream( path, true ) ;
	}
	public static InputStream createInputStream( String path, boolean errorMessage )
	{
		InputStream inputStream = null;
		try
		{
			inputStream = rsc.getClass().getResourceAsStream( RESOURCE_BASE_PATH + path ) ;
		} catch (Exception e) {
			e.printStackTrace();
		}

		if ( inputStream == null && errorMessage )
			throw new ErrorClass( "Package error: File \"" + path + "\" not found" ) ;

		return inputStream ;
	}
	public static void copyFile( String destinationPath, String source )
	{
		ResourceManager.copyFile( destinationPath, source, null, false ) ;
	}

	public static void copyFile( String destinationPath, String source, ArrayList< String[] > keywords, boolean removeComment )
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
				{
					for ( String [] keyPair : keywords )
					{
						int pos = 0 ;
						while ( ( pos = line.indexOf( keyPair[0], pos ) ) >= 0 )
						{
							line = line.substring( 0, pos) + keyPair[ 1 ] + line.substring( pos + keyPair[0].length() ) ;
							pos += keyPair[1].length() ;
						}
					}
					int pos = 0 ;
					if ( (pos = line.indexOf( "//", pos )) >= 0 )
					{
						if ( line.contains( "////" ) )
							line = line.substring( pos + 2 ) ;
						else
							line = line.substring( 0, pos ) ;
					}
				}
				if ( line.length() == 0 )
					continue ;
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
			throw new ErrorClass( e1, "Unexpected error on writing file \""
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


	public class LanguageClassLoader extends ClassLoader
	{
		@Override
		public InputStream 	getResourceAsStream(String name)
		{
			String path = DATA_PATH + name ;
			return ResourceManager.createInputStream( path, false  ) ;
		}
		@Override
		public URL getResource(String name)
		{
			String path = DATA_PATH + name ;
			return ResourceManager.getURL( path ) ;
		}
	}


	public static Locale[] getAvailableLocales( String baseName )
	{
		ArrayList< Locale > res = new ArrayList< Locale >() ;

		for ( Locale l : Locale.getAvailableLocales() )
		{
			StringBuilder filename = new StringBuilder( DATA_PATH ) ;

			filename.append( baseName ) ;
			filename.append( "_" ) ;
			filename.append( l.getLanguage() ) ;

			if ( l.getCountry().length() != 0 || l.getVariant().length() != 0 )
			{
				filename.append( "_" ) ;
				filename.append( l.getCountry() ) ;
			}

			if ( l.getVariant().length() != 0 )
			{
				filename.append( "_" ) ;
				filename.append( l.getVariant() ) ;
			}

			filename.append( ".properties" ) ;

			if ( getURL( filename.toString() ) != null )
				res.add( l ) ;
		}
		res.add( new Locale( "en", "", "" )) ;
		return res.toArray( new Locale[ 0 ] ) ;
	}

	public static ResourceBundle getResourceBundle( String baseName )
	{
		return ResourceBundle.getBundle( baseName, Locale.getDefault(), rsc.new LanguageClassLoader() ) ;
	}

	public static String msg( String key, String ... strings )
	{
		if ( isMsgInProcess  )
			return "Error in ResourceManager.msg, message can't be generated" ;
		isMsgInProcess = true ;
		if ( resourceBundleLang == null )
			resourceBundleLang = getResourceBundle( "lang" ) ;
		String msg = null ;
		try
		{
			msg = resourceBundleLang.getString( key ) ;
		} catch ( MissingResourceException e ) {
			Log.error( "Keyword \"" + key + "\" is not assigned to any message string in the language files" ) ;
			throw new TerminateClass( 1 ) ;
		}
		msg = msg.replace( "'", "''") ;
		String ret = MessageFormat.format( msg, (Object[]) strings ) ;
		isMsgInProcess = false ;
		return ret ;
	}
}