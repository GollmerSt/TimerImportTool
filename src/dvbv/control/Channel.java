// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

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
	public Channel( final Channel channel )
	{
		this.type = channel.type ;
		this.name = channel.name ;
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
