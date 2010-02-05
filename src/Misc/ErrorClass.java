// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package Misc ;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;


public class ErrorClass extends Error
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5094608651279342617L;
	private static boolean warnings = false ;
	private final Throwable mainError ;
	private final String errorString ;
	public ErrorClass( String errorString )
	{
		super( errorString ) ;
		mainError = null ;
		this.errorString = errorString ;
	}
	public ErrorClass( Throwable e, String errorString )
	{
		super( errorString ) ;
		mainError = e ;
		this.errorString = errorString ;
	}
	public ErrorClass( XMLEvent ev, String errorString )
	{
		super( errorString + " near line "
                + Integer.toString( ev.getLocation().getLineNumber() ) + "." ) ;
		mainError = null ;
		this.errorString = errorString + " near line "
                           + Integer.toString( ev.getLocation().getLineNumber() ) + "." ;
	}
	
	public ErrorClass( XMLStreamException e, String errorString )
	{
		super( errorString ) ;
		mainError = e ;
		this.errorString = errorString ;
	}
	
	public String getLocalizedMessage()
	{
		String res = this.getMessage() ;
		if ( this.mainError.getClass() == javax.xml.stream.XMLStreamException.class )
		{
			XMLStreamException ex = (XMLStreamException) this.mainError ;
			if ( ex.getLocation() != null )
				res += " near line " + Integer.toString( ex.getLocation().getLineNumber() ) + "." ;
			else
				res += "." ;
		}
		if ( this.mainError != null)
			res += "\nDetailed error message:\n\n" +
					  this.mainError.getLocalizedMessage() ;
		return( res ) ;
	}
	public String getErrorString() { return this.errorString ; } ;
	static public void setWarníng() { ErrorClass.warnings = true ; } ;
	static public boolean isWarning() { return ErrorClass.warnings ; } ;
}
