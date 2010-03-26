// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.control;

import dvbv.javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

public class Channel {
	private final int type ;
	private final String name ;
	private final long id ;
	public Channel( int type, String name, long id )
	{
		this.type = type ;
		this.name = name ;
		this.id = id ;
	}
	public Channel( final Channel channel )
	{
		this( channel.type, channel.name, channel.id ) ;
	}
	public int  getType() { return this.type ; } ;
	public String getName() { return this.name ; } ;
	public long getID() { return this.id ; } ;
	public String getTypeName(){ return dvbv.provider.Provider.getProviderName( this.type) ; } ;
	public void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException
	{
		sw.writeStartElement( "Provider" ) ;
		sw.writeAttribute( "name", this.getTypeName() ) ;
		if ( this.id >= 0 )
			sw.writeAttribute( "channelID", Long.toString( id ) ) ;
		sw.writeCharacters( name ) ;
		sw.writeEndElement() ;
	}
	@Override
	public String toString()
	{
		String out = this.getTypeName() + " channel: Name = \"" + name + "\"" ;
		if ( id >= 0L )
			out += "  Id = \"" + id + "\"" ;
		return out ;
	}
}
