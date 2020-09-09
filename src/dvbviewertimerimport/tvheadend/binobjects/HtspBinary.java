package dvbviewertimerimport.tvheadend.binobjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class HtspBinary extends Htsp<HtspBinary> implements Cloneable {
	
	private byte [] bytes ;
	
	public HtspBinary( byte [] bytes ) { 
		this.bytes = bytes;
	}
	
	public byte [] get() {
		return this.bytes;
	}
	
	public HtspBinary() {
		this.bytes=null;
	}
	
	@Override
	public HtspBinary clone() throws CloneNotSupportedException {
		HtspBinary binary = (HtspBinary) super.clone();
		binary.bytes = Arrays.copyOf(this.bytes, this.bytes.length);
		return binary;
		
	}

	@Override
	public void write(OutputStream s) throws IOException {
		s.write(this.bytes);

	}

	@Override
	public HtspBinary create(InputStream s, long length) throws IOException, ClassNotFoundException {
		HtspBinary binary = new HtspBinary(new byte[(int) length]);
		s.read(binary.bytes);
		return binary;
	}

	@Override
	public long getNetLength() {
		return this.bytes.length;
	}

	public static HtspBinary getInstance() {
		return HtsBinaryHolder.INSTANCE;
	}

	private static class HtsBinaryHolder {

		private static final HtspBinary INSTANCE = new HtspBinary();
	}
	
	@Override
	public String toString() {
		return this.bytes.toString();
	}

}
