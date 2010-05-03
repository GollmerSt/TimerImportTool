// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer.channels;

import java.nio.MappedByteBuffer;

public class Channel {
	public static final int ENTRY_LENGTH = Tuner.ENTRY_LENGTH + 26 * 3 + 2 ;
	private final Channels channels ;
	private Tuner tuner = null ;
	private String channelName = null ;
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
		this.tuner = new Tuner( channels ) ;
		this.tuner.read();
		
		channels.readString( 26 );
		this.channelName = channels.readString( 26 ) ;
		channels.readString( 26 );

		MappedByteBuffer buffer = channels.getMappedByteBuffer() ;

		buffer.get();
		buffer.get();
	}
	public String getChannelName() { return this.channelName ; } ;
	public String toString() { return this.channelName ; } ;
	public String getChannelID()
	{
		if ( this.channels == null )
			return null ;
		long id = ( this.tuner.getType() + 1 ) << 29 ;
		id |= this.tuner.getAudioPID() << 16 ;
		id |= this.tuner.getServiceID() ;
		
		return Long.toString( id ) + "|" + this.channelName ;
	}
	public boolean isVideo() { return this.tuner.isVideo() ; } ;
}
