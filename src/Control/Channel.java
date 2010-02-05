// $LastChangedDate: 2010-02-02 20:15:15 +0100 (Di, 02. Feb 2010) $
// $LastChangedRevision: 79 $
// $LastChangedBy: Stefan Gollmer $

package Control;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

public class Channel {
	public enum Type { TVINFO, CLICKFINDER, SIZE } ;
	private static final String[] typeStrings = { "TVInfo", "ClickFinder" } ;
	private final Type type ;
	private final String name ;
	public Channel( Type type, String name )
	{
		this.type = type ;
		this.name = name ;
	}
	public Type getType() { return this.type ; } ;
	public int getIndex() { return this.type.ordinal() ; } ;
	public String getName() { return this.name ; } ;
	public String getTypeName(){ return Channel.typeStrings[ this.type.ordinal() ] ; } ;
	public void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException
	{
		sw.writeStartElement( typeStrings[ type.ordinal() ] ) ;
		sw.writeCharacters( name ) ;
		sw.writeEndElement() ;
	}
}
