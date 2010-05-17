// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.misc ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;




public class Registry {
		
	static public String getValue( String path, String key )
	{
		String command = "reg query \"" + path + "\" /v \"" + key + "\"" ;
		//System.out.println( command ) ;
		String out = Registry.exec( command ) ;
		if ( out == null )
			return null ;
		String[] temps = out.split("REG_[A-Z][A-Z]\t");
		if ( temps.length != 2)
			return null ;
		out = temps[1].trim() ;
		//System.out.println( out ) ;
		return out ;
	}
	static public boolean setValue( String path, String type, String key, String value )
	{
		String command = "reg add \"" + path + "\" /v \"" + key + "\" /t " + type + " /d \"" + value + "\" /f";
		//System.out.println( command ) ;
		if ( Registry.exec( command ) == null )
			return false ;
		//System.out.print( out ) ;
		return true ;
	}
	static public boolean delete( String path, String key )
	{
		String command = "reg delete \"" + path + "\" /f" ;
		if ( key.length() != 0 )
			command += " /v " + key ;
		//System.out.println( command ) ;
		if ( Registry.exec( command ) == null )
			return false ;
		return true ;
	}
	private static String exec( String command )
	{
		if ( ! Constants.IS_WINDOWS )
			return null ;
		
		String result = "" ;
		
		Process p;
		try {
			p = Runtime.getRuntime().exec( command );
		} catch (IOException e) {
			throw new ErrorClass( e, "Error on acces to registry.") ;
		}
		
		InputStreamReader ir = new InputStreamReader( p.getInputStream() ) ;
		BufferedReader input = new BufferedReader( ir ) ;
		String line = null ;
		try {
			while ((line = input.readLine()) != null)
			{
				result += line + '\n' ;
			}
			try {
				p.waitFor() ;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			throw new ErrorClass( e, "Error on reading the output of a command." ) ;
		}
		return result ;
	}
}
