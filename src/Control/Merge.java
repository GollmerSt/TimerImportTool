// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package Control ;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

public class Merge {
	private boolean merge = false ;
	private boolean valid ;
	public Merge( boolean valid )
	{
		this.valid = valid ;
	}
	public void set( boolean c ) { this.merge = c ; } ;
	public boolean toMerge() { return this.merge ; } ;
	public void setValid() { this.valid = true ; } ;
	public boolean isValid() { return this.valid ; } ;
	public void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException
	{
		if ( ! this.valid )
			return ;
		sw.writeStartElement( "Combine" ) ;
		if ( merge )
			sw.writeCharacters( "true" ) ;
		else
			sw.writeCharacters( "false" ) ;
		sw.writeEndElement() ;
}
}
