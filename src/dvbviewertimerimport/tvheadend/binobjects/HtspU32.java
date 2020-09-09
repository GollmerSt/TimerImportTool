package dvbviewertimerimport.tvheadend.binobjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HtspU32 extends Htsp<HtspU32> implements Cloneable {

	private long u32;

	public HtspU32() {
		this.u32 = 0;
	}

	public HtspU32(long value) {
		this.u32 = value;
	}
	
	@Override
	public HtspU32 clone() {
		return new HtspU32(this.u32);
	}

	public void set(long value) {
		this.u32 = value;
	}

	public void set(HtspU32 u32) {
		this.u32 = u32.get();
	}

	public long get() {
		return this.u32;
	}

	@Override
	public void write(OutputStream s) throws IOException {
		byte[] buf = new byte[] { //
				(byte) (this.u32 >> 24 & 0xff), //
				(byte) (this.u32 >> 16 & 0xff), //
				(byte) (this.u32 >> 8 & 0xff), //
				(byte) (this.u32 & 0xff) };
		s.write(buf);
	}

	@Override
	public HtspU32 create(InputStream s) throws IOException {
		byte[] buf = new byte[4];
		s.read(buf);
		return new HtspU32( //
				((long) buf[0] & 0xff) << 24 //
						| ((long) buf[1] & 0xff) << 16 //
						| ((long) buf[2] & 0xff) << 8 //
						| ((long) buf[3] & 0xff) //
		);
	}

	@Override
	public long getNetLength() {
		return 4;
	}

	public static HtspU32 getInstance() {
		return U32Holder.INSTANCE;
	}

	private static class U32Holder {

		private static final HtspU32 INSTANCE = new HtspU32();
	}
}
