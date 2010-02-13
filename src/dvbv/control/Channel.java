// $LastChangedDate: 2010-02-02 20:15:15 +0100 (Di, 02. Feb 2010) $
// $LastChangedRevision: 79 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.control;

import dvbv.javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

public class Channel {
	private final int type ;
	private final String name ;
	public Channel( int type, String name )
	{
		this.type = type ;
		this.name = name ;
	}
	public int  getType() { return this.type ; } ;
	public String getName() { return this.name ; } ;
	public String getTypeName(){ return dvbv.provider.Provider.getProviderName( this.type) ; } ;
	public void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException
	{
		sw.writeStartElement( "Provider" ) ;
		sw.writeAttribute( "name", this.getTypeName() ) ;
		sw.writeCharacters( name ) ;
		sw.writeEndElement() ;
	}
}
