// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.misc ;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.swing.JOptionPane;


public class Log {
	private static final String NAME_LOG_FILE                 = "DVBVTimerImportTool" ;	

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); ;
	private static String logFileName = NAME_LOG_FILE ;
	private static File file = null ;
	private static boolean toDisplay = false ;
	private static boolean verbose = false ;
	public static String getFile()
	{
		if ( file != null )
			return file.getPath() ;
		else
			return "" ;
	}
	public static void setFile( String dataPath )
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
	public static String out( boolean verbose, boolean error, String out, boolean toDisplay )
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
	public static String out( String out )
	{
		return( Log.out( false, false, out, false ) ) ;
	}
	public static String out( boolean verbose, String out )
	{
		return( Log.out( verbose, false, out, false ) ) ;
	}
	public static String out( String out, boolean toDisplay )
	{
		return( Log.out( false, false, out, toDisplay ) ) ;
	}
	public static String error( String out )
	{
			return( Log.out(false, true, out, false) ) ;
	}
	public static void setVerbose( boolean b ) { Log.verbose = b ; } ;
	public static boolean isVerbose() { return Log.verbose ; } ; 
	public static void setToDisplay( boolean d ) { Log.toDisplay = d ; } ;
	public static boolean toDisplay() { return Log.toDisplay ; } ;
	public static void ErrorBox( String errorText )
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
