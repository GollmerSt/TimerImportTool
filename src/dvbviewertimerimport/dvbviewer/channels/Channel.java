// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer.channels;

import java.nio.MappedByteBuffer;

import dvbviewertimerimport.misc.Log;

public class Channel {
	public static final int ENTRY_LENGTH = Tuner.ENTRY_LENGTH + 26 * 3 + 2 ;
	private final Channels channels ;
	private boolean isVideo = true ;
	private String channelName = null ;
	private String channelID = null ;
	public Channel( Channels channels )
	{
		this.channels = channels ;
	}
	public Channel()
	{
		this.channels = null ;
		this.channelName = "<none>" ;
	}
	public void read()
	{
		Tuner tuner = new Tuner( channels ) ;
		tuner.read();
		
		this.isVideo = tuner.isVideo() ;
		
		channels.readString( 26 );
		this.channelName = channels.readString( 26 ) ;
		channels.readString( 26 );

		MappedByteBuffer buffer = channels.getMappedByteBuffer() ;

		buffer.get();
		buffer.get();
		
		long id = ( tuner.getType() + 1 ) << 29 ;
		id |= tuner.getAudioPID() << 16 ;
		id |= tuner.getServiceID() ;
		
		this.channelID = Long.toString( id ) + "|" + this.channelName ;
	}
	public String getChannelName() { return this.channelName ; } ;
	public String toString() { return this.channelName ; } ;
	public String getChannelID() { return this.channelID ; }
	public boolean isVideo() { return this.isVideo ; } ;
	public static Channel createByChannelID( String channelID )
	{
		String [] parts = channelID.split( "[|]" ) ;
		if ( parts.length != 2 )
		{
			Log.error( "Illegal format of the channelID: " + channelID ) ;
			System.exit( 1 ) ;
		}
		Channel c = new Channel() ;
		c.channelName = parts[1] ;
		c.channelID = channelID ;
		return c ;
	}
}
