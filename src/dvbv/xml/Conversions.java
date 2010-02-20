// $LastChangedDate: 2010-02-02 20:15:15 +0100 (Di, 02. Feb 2010) $
// $LastChangedRevision: 79 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.xml;

import java.io.File;

import javax.xml.stream.events.XMLEvent;

import dvbv.misc.ErrorClass;

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
