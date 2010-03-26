// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.xml;

import javax.xml.stream.events.XMLEvent;

import dvbv.misc.ErrorClass;

public class Conversions {
	public static boolean getBoolean( String text, XMLEvent ev, String name )
	{
		if      ( text.equalsIgnoreCase( "true" ) )
			return true ;
		else if ( text.equalsIgnoreCase( "false" ) )
			return false ;
		else
			throw new ErrorClass( ev, "Illegal boolean error in file \"" + name + "\"" ) ;
	}
}
