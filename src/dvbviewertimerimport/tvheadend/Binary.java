package dvbviewertimerimport.tvheadend;

import java.util.Collection;

public class Binary {

	byte[] bytes;
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public byte[] getBytes() {
		return bytes;
	}

	private int pointer ;

	public Binary(Collection<Byte> bytes) {
		this.bytes = new byte[bytes.size()];
		int i = 0;
		for (Byte b : bytes) {
			this.bytes[i++] = b;
		}
		this.pointer = bytes.size();
	}

	public Binary() {
		bytes = new byte[0] ;
		this.pointer = 0 ;
	}

	public Binary(int size) {
		this.bytes = new byte[size];
		this.pointer = 0 ;
	}
	
	public void add( byte b ) {
		if ( pointer >= bytes.length ) {
			throw new Error( "Length of Binary to great") ;
		}
		this.bytes[ pointer++ ] = b ;
	}
	
	public void addAll( Collection< Byte > bytes ) { 
		if ( pointer + bytes.size() > this.bytes.length ) {
			throw new Error( "Length of Binary to great") ;
		}
		for ( Byte b : bytes ) {
			this.bytes[ pointer++] = b ; 
		}
	}
	
	public byte get( int i ) {
		return bytes[i] ;
	}
	
	public int size() {
		return pointer ;
	}
}