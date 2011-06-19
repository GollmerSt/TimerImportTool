// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.control;

import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

public class Channel {
	private final int type ;
	private final String name ;
	private String id ;
	public Channel( int type, String name, String id )
	{
		this.type = type ;
		this.name = name ;
		this.id = id ;
	}
/*	public Channel( int type, String name, long id )
	{
		this( type, name, id >= 0 ? Long.toString( id ) : null ) ;
	}
*/	public Channel( final Channel channel )
	{
		this( channel.type, channel.name, channel.id ) ;
	}
	public void setID( long id ) { this.id = id >= 0 ? Long.toString( id ) : null ; }
	public void setID( String id ) { this.id = id ; }
	public void setID( Object id )
	{ 
		if ( id.getClass() == long.class )
			this.setID( (Long)id ) ;
		else
			this.setID( (String)id ) ;
	  }
	public int  getType() { return this.type ; } ;
	public String getName() { return this.name ; } ;
	public String getTextID() { return this.id ; } ;
	public long getNumID() { return Long.valueOf( this.id ) ; } ;
	public String getTypeName(){ return dvbviewertimerimport.provider.Provider.getProviderName( this.type) ; } ;
	public void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException
	{
		sw.writeStartElement( "Provider" ) ;
		sw.writeAttribute( "name", this.getTypeName() ) ;
		if ( this.id != null )
			sw.writeAttribute( "channelID",id ) ;
		sw.writeCharacters( name ) ;
		sw.writeEndElement() ;
	}
	@Override
	public String toString()
	{
		String out = this.getTypeName() + " channel: Name = \"" + name + "\"" ;
		if ( id != null )
			out += "  Id = \"" + id + "\"" ;
		return out ;
	}
	public Object getIDKey() { return null ; } ;  // ID of the provider, type is provider dependent
	public Object getIDKey( final Channel c ) { return c.getIDKey() ; } ;  // ID of the provider, type is provider dependent
}
