// $LastChangedDate: 2010-02-02 20:15:15 +0100 (Di, 02. Feb 2010) $
// $LastChangedRevision: 79 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.dvbviewer.channels;

import java.nio.MappedByteBuffer;

public class Channel {
	public static final int ENTRY_LENGTH = Tuner.ENTRY_LENGTH + 26 * 3 + 2 ;
	private final Channels channels ;
	private Tuner tuner = null ;
	private String root ;
	private String channelName = null ;
	private String category ;
    private byte encrypted ;			// deprecated! Only set for compatibility. Same as TTuner.Flags.
    private byte reserved ;
	public Channel( Channels channels )
	{
		this.channels = channels ;
	}
	public Channel()
	{
		this.channels = null ;
	}
	public void read()
	{
		this.tuner = new Tuner( channels ) ;
		this.tuner.read();
		
		this.root        = channels.readString( 26 ) ;
		this.channelName = channels.readString( 26 ) ;
		this.category    = channels.readString( 26 ) ;

		MappedByteBuffer buffer = channels.getMappedByteBuffer() ;

		this.encrypted = buffer.get() ;
		this.reserved  = buffer.get();
	}
	public String getChannelName() { return this.channelName ; } ;
	public String toString() { return this.channelName ; } ;
	public String getChannelID()
	{
		if ( channelName == null )
			return null ;
		long id = ( this.tuner.getType() + 1 ) << 29 ;
		id |= this.tuner.getAudioPID() << 16 ;
		id |= this.tuner.getServiceID() ;
		
		return Long.toString( id ) + "|" + this.channelName ;
	}
	public boolean isVideo() { return this.tuner.isVideo() ; } ;
}
