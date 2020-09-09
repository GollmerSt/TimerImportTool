package dvbviewertimerimport.tvheadend.binobjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class HtspS64 extends Htsp<HtspS64> implements Cloneable {

	private long s64 = 0;
	private byte[] bytes = null;

	public HtspS64() {
	}

	public HtspS64(long value) {
		this.s64 = value;
	}
	
	
	@Override
	public HtspS64 clone() {
		return new HtspS64( this.s64);
	}
	
	private void fromValue() {
		byte [] bytes = new byte[8] ;
		long s64 = this.s64 ;
		int length = 0;
		while( s64 != 0) {
			byte b = (byte) (s64 & 0xff) ;
			s64 = s64 >> 8 & 0x00ffffffffffffffL;
			bytes[length++] = b;
		}
		this.bytes = Arrays.copyOf(bytes, length);
	}
	
	private void toValue() {
		int shift = 0;
		this.s64 = 0;
		for ( byte b : this.bytes) {
			this.s64 |= (long)b << shift;
			shift +=8;
		}
	}

	public void set(long value) {
		this.s64 = value;
		this.bytes = null;
	}

	public void set(HtspS64 s64) {
		this.s64 = s64.get();
		this.bytes = null;
	}

	public long get() {
		return this.s64;
	}

	@Override
	public void write(OutputStream s) throws IOException {
		if ( this.bytes ==null) {
			this.fromValue();
		}
		s.write(this.bytes);
	}

	@Override
	public HtspS64 create(InputStream s, long dataLength ) throws IOException, ClassNotFoundException {
		HtspS64 s64 = new HtspS64();
		s64.bytes = new byte[(int) dataLength];
		s.read(s64.bytes);
		s64.toValue();
		return s64;
	}

	@Override
	public long getNetLength() {
		if ( this.bytes == null ) {
			this.fromValue();
		}
		return this.bytes.length;
	}

	public static HtspS64 getInstance() {
		return S64Holder.INSTANCE;
	}

	private static class S64Holder {

		private static final HtspS64 INSTANCE = new HtspS64();
	}
	
	@Override
	public String toString() {
		return Long.toString(this.get());
	}

}
