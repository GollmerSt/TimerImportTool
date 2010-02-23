// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.dvbviewer.channels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

import dvbv.dvbviewer.DVBViewer;
import dvbv.misc.ErrorClass;


public class Channels {
	
	private static final int SUPPORTED_HEADER_LENGTH       = 7;
	private static final String SUPPORTED_FILE_ID          = "B2C2";
	private static final byte SUPPORTED_VERSION_HIGH       = 1 ;
	private static final byte SUPPORTED_VERSION_LOW        = 8 ;
	private static final String CHANNEL_FILE_NAME           = "channels.dat" ;
	private static final int SUPPORTED_CHANNEL_ENTRY_LENGTH = Channel.ENTRY_LENGTH ;
	private static final FileChannel.MapMode READ_ONLY      = FileChannel.MapMode.READ_ONLY ;

	private final DVBViewer dvbViewer ;
	private File file ;
	private FileChannel      fileChannel ;
	private MappedByteBuffer buffer = null ;
	private long headerLength = 0 ;
	private long channelEntryLength = 0 ;
	
	public class MyComparator implements Comparator< String>
	{
		@Override
		public int compare(String o1, String o2) {
			if ( o1.equalsIgnoreCase(o2) )
				return o1.compareTo( o2 ) ;
			return o1.compareToIgnoreCase(o2) ;
		}
	}
	private TreeMap< String, Channel > channelMap = new TreeMap< String, Channel >( new MyComparator() ) ;
	
	public Channels( DVBViewer dvbViewer )
	{
		this.dvbViewer = dvbViewer ;
	}
	private void throwErrorWrongVersion()
	{
		throw new ErrorClass("Error on reading \"" 
	                   + CHANNEL_FILE_NAME + "\". Version changed?" ) ;
	}
	String readString( int fieldLength )
	{
		byte[] buffer = new byte[ fieldLength - 1 ] ;
		
		int stringLength = 0 ;
		
		try
		{
			stringLength = this.buffer.get() ;
			
			if ( stringLength >= fieldLength )
				this.throwErrorWrongVersion() ;
						
			this.buffer.get( buffer, 0, fieldLength - 1 ) ;
			
		} catch ( BufferUnderflowException e ) {
			throwErrorWrongVersion() ;
		}
		return new String( buffer, 0, stringLength ) ;
	}
	private byte readByte()
	{
		byte result = 0;
		
		try
		{
			result = this.buffer.get() ;
		} catch ( BufferUnderflowException e ) {
			throwErrorWrongVersion() ;
		}
		return result ;
	}
	public void openFileAndCheckHeader()
	{
		this.file = new File( this.dvbViewer.getDataPath()
				              + File.separator + CHANNEL_FILE_NAME ) ;
		
		try {
			this.fileChannel = new FileInputStream( this.file ).getChannel() ;
		} catch (FileNotFoundException e) {
			throw new ErrorClass( "Error on opening \"" + this.file.getAbsolutePath()
					          + "\". File exists?" ) ;
		}
		
		try {
			this.buffer = fileChannel.map(READ_ONLY , 0, SUPPORTED_HEADER_LENGTH) ;
		} catch (IOException e) {
			this.throwErrorWrongVersion() ;
		}
		if ( ! this.readString(5).equals( Channels.SUPPORTED_FILE_ID ) )
			this.throwErrorWrongVersion() ;
		if ( this.readByte() != SUPPORTED_VERSION_HIGH )
			this.throwErrorWrongVersion() ;
		if ( this.readByte() != SUPPORTED_VERSION_LOW )
			this.throwErrorWrongVersion() ;

		this.headerLength       = SUPPORTED_HEADER_LENGTH ;
		this.channelEntryLength = SUPPORTED_CHANNEL_ENTRY_LENGTH ;
}
	public void read()
	{
		this.openFileAndCheckHeader() ;
		
		try {
			long size = this.fileChannel.size() ;
			
			int numEntries = (int) (( size - this.headerLength ) / this.channelEntryLength) ;
			for ( int n = 0 ; n < numEntries ; n++ )
			{
				this.buffer = fileChannel.map( READ_ONLY ,
						                       this.channelEntryLength *n + this.headerLength ,
						                       this.channelEntryLength ) ;

				this.buffer.order(ByteOrder.LITTLE_ENDIAN) ;
				
				Channel channel = new Channel( this ) ;
				channel.read();
				
				if ( channel.isVideo() )
					this.channelMap.put( channel.getChannelName(), channel ) ;
				
			}
		} catch (IOException e1) {
			throw new ErrorClass( "Unexpected error on reading \"" + this.file.getAbsolutePath() );
		}
		
		boolean debug = false ;
		if ( debug )
		{
			Collection<Channel> values = this.channelMap.values() ;
			
			for ( Iterator<Channel> it = values.iterator() ; it.hasNext(); )
			{
				System.out.println(" ChannelID: " + it.next().getChannelID() ) ;
			}
		}
		try {
			this.fileChannel.close() ;
		} catch (IOException e) {
			throw new ErrorClass( "Unexpected error on closing \"" + this.file.getAbsolutePath() );
		}
	}
	public MappedByteBuffer getMappedByteBuffer() { return this.buffer ; } ;
	public TreeMap< String, Channel > getChannels() { return this.channelMap ; } ;
}
	
