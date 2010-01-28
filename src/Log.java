// $LastChangedDate: 2010-01-28 09:11:48 +0100 (Do, 28 Jan 2010) $
// $LastChangedRevision: 15 $
// $LastChangedBy: Stefan Gollmer $


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.swing.JOptionPane;


public class Log {
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); ;
	static private String logFileName = Constants.NAME_LOG_FILE ;
	static private File file = null ;
	static private boolean toDisplay = false ;
	static private boolean verbose = false ;
	static void setFile( String dataPath )
	{
		File dir = new File( dataPath ) ;
		if ( ! dir.isDirectory() )
		{
			System.err.println( "Directory \"" + dataPath + "\" is not available") ;
			return ;
		}
		Log.file = new File(dataPath + File.separator + Log.logFileName + ".log" ) ;
		if ( ! Log.file.isFile() )
		try
			{
			Log.file.createNewFile() ;
		} catch ( Exception e ) {
			System.err.println( "File \"" + Log.file.getPath() + "\" can't be created.") ;
			return ;
		}
		Log.out( "Import started" ) ;
	}
	static String out( boolean verbose, boolean error, String out, boolean toDisplay )
	{
		if ( verbose && ! Log.isVerbose() )
			return out ;
		if ( file == null )
		{
			Log.ErrorBox( out ) ;
			System.err.println( out ) ;
			return out ;
		}
		FileWriter fstream = null ;
		try {
			long now = System.currentTimeMillis() ;
			String dateString = Log.dateFormat.format( now ) + "  ";
			fstream = new FileWriter( Log.file, true );
			BufferedWriter bf = new BufferedWriter( fstream ) ;
			String[] strings = out.split( "\n" ) ;
			for ( int i = 0 ; i< strings.length ; i++ )
			{
				if ( i != 0 )
					bf.write( "                     " ) ;
				else
					bf.write( dateString ) ;
				bf.write( strings[i] + '\n' );
			}
			bf.close();
			if ( error )
				System.err.println( out ) ;
		} catch (IOException e) {
			System.err.println( out ) ;
			Log.ErrorBox( out ) ;
			return out ;
		}
		if ( ( error && Log.toDisplay ) || toDisplay )
			Log.ErrorBox( out ) ;
		return out ;
	}
	static String out( String out )
	{
		return( Log.out( false, false, out, false ) ) ;
	}
	static String out( boolean verbose, String out )
	{
		return( Log.out( verbose, false, out, false ) ) ;
	}
	static String out( String out, boolean toDisplay )
	{
		return( Log.out( false, false, out, toDisplay ) ) ;
	}
	static String error( String out )
	{
			return( Log.out(false, true, out, false) ) ;
	}
	static public void setVerbose( boolean b ) { Log.verbose = b ; } ;
	static public boolean isVerbose() { return Log.verbose ; } ; 
	static public void setToDisplay( boolean d ) { Log.toDisplay = d ; } ;
	static public boolean toDisplay() { return Log.toDisplay ; } ;
	static void ErrorBox( String errorText )
	{
	    String lines = "" ;
	    int pos0 = 0 ;
	    int i = 0 ;
	    for ( i = 0 ; i < errorText.length(); i++)
	    {
	    	if ( errorText.charAt(i) == '\n' )
	    	{
	    		lines += errorText.substring(pos0, i) + "\n" ;
	    		pos0 = i+1 ;
	    	}
	    	if ( ( i - pos0 ) > 40 )
	    	{
	    		if ( errorText.charAt(i) == ' ' )
	    		{
	    			if ( pos0 != 0 )
	    				lines += "\n" ;
	    			lines += errorText.substring(pos0, i) ;
	    			pos0 = i+1 ;
	    		}
	    	}
	    }
	    if ( pos0 != 0 )
	    	lines += "\n" ;
	    lines += errorText.substring(pos0) + "\n" ;
	    
		JOptionPane.showMessageDialog(null, lines, "Error occured", JOptionPane.OK_OPTION);
	}
}
