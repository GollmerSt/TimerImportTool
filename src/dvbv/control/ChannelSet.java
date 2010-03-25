// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.control ;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import dvbv.javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

import dvbv.misc.Enums.Merge;

public class ChannelSet {
	private ArrayList< Channel > channels = new ArrayList< Channel >() ;
	private String dvbViewerChannel = null ;
	private TimeOffsets timeOffsets = new TimeOffsets() ;
	private Merge merge = Merge.INVALID ;
	
	public Channel add( int type, String name, long id )
	{
		Channel channel = new Channel( type, name, id ) ;
		channels.add( channel ) ;
		return channel ;
	}
	public void remove( int type )
	{
		for ( Iterator< Channel > it = channels.iterator() ; it.hasNext() ; )
		{
			Channel channel = it.next() ;
			if ( channel.getType() == type )
				it.remove() ;
		}
	}
	public void setTimeOffsets( TimeOffsets timeOffsets ) { this.timeOffsets = timeOffsets ; }
	public TimeOffsets getTimeOffsets() { return timeOffsets ; } ;
	public void setMerge( Merge merge ) { this.merge = merge ; } ;
	public void setMerge( boolean merge )
	{
		if ( merge )
			this.merge = Merge.TRUE ;
		else
			this.merge = Merge.FALSE ;
	} ;
	public Merge getMerge() { return this.merge ; } ;
	public void setDVBViewerChannel( String channelName ) { this.dvbViewerChannel = channelName ; } ;
	public String getDVBViewerChannel() { return this.dvbViewerChannel ; } ;
	public ArrayList< Channel > getChannels() { return channels ; } ;
	public Channel getChannel( int providerID )
	{
		for ( Channel c : channels )
		{
			if ( c.getType() == providerID )
				return c ;
		}
		return null ;
	}
	public void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException, ParseException
	{
		sw.writeStartElement( "Channel" ) ;
		  for ( Channel c : this.channels )
			  c.writeXML( sw ) ;
		
		  if ( this.dvbViewerChannel != null )
		  {
			  sw.writeStartElement( "DVBViewer" ) ;
			  sw.writeCharacters( this.dvbViewerChannel ) ;
			  sw.writeEndElement() ;
		  }
		  this.timeOffsets.writeXML( sw ) ;
		  if ( this.merge != Merge.INVALID )
		  {
			  sw.writeStartElement( "Merge" ) ;
			  if ( this.merge == Merge.FALSE )
				  sw.writeCharacters( "false" ) ;
			  else
				  sw.writeCharacters( "true" ) ;
			  sw.writeEndElement() ;
		  }
		sw.writeEndElement() ;
	}
}
