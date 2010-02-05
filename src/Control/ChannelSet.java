// $LastChangedDate: 2010-02-02 20:15:15 +0100 (Di, 02. Feb 2010) $
// $LastChangedRevision: 79 $
// $LastChangedBy: Stefan Gollmer $

package Control ;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

public class ChannelSet {
	private ArrayList< Channel > channels = new ArrayList< Channel >() ;
	private String dvbViewerChannel = null ;
	private TimeOffsets timeOffsets = new TimeOffsets() ;
	private Merge merge = new Merge( false );
	
	public void add( Channel.Type type, String name )
	{
		Channel channel = new Channel( type, name ) ;
		channels.add( channel ) ;
	}
	public void setTimeOffsets( TimeOffsets timeOffsets ) { this.timeOffsets = timeOffsets ; }
	public TimeOffsets getTimeOffsets() { return timeOffsets ; } ;
	public void setMerge( Merge merge ) { this.merge = merge ; } ;
	public Merge getMerge() { return this.merge ; } ;
	public void setDVBViewerChannel( String channelName ) { this.dvbViewerChannel = channelName ; } ;
	public String getDVBViewerChannel() { return this.dvbViewerChannel ; } ;
	public ArrayList< Channel > getChannels() { return channels ; } ;
	public void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException, ParseException
	{
		sw.writeStartElement( "Channel" ) ;
		  for ( Iterator<Channel> it = this.channels.iterator() ; it.hasNext() ; )
			  it.next().writeXML( sw ) ;
		
		  if ( this.dvbViewerChannel != null )
		  {
			  sw.writeStartElement( "DVBViewer" ) ;
			  sw.writeCharacters( this.dvbViewerChannel ) ;
			  sw.writeEndElement() ;
		  }
		  this.timeOffsets.writeXML( sw ) ;
		  this.merge.writeXML( sw ) ;
		sw.writeEndElement() ;
	}
}
