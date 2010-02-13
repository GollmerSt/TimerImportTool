package XML;

import java.io.File;

import javax.xml.stream.events.XMLEvent;

import Misc.ErrorClass;

public class Conversions {
	public static boolean getBoolean( String text, XMLEvent ev, File f )
	{
		if      ( text.equalsIgnoreCase( "true" ) )
			return true ;
		else if ( text.equalsIgnoreCase( "false" ) )
			return false ;
		else
			throw new ErrorClass( ev, "Illegal boolean error in file \"" + f.getName() + "\"" ) ;
	}
}
