package dvbviewertimerimport.tvheadend.binobjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HtspU8 extends Htsp<HtspU8> implements Cloneable {

	private int u8;

	public HtspU8() {
		this.u8 = 0;
	}

	public HtspU8(int value) {
		this.u8 = value;
	}
	
	@Override
	public HtspU8 clone() {
		return new HtspU8(this.u8);
	}

	public void set(int value) {
		this.u8 = value;
	}

	public void set(HtspU8 u8) {
		this.u8 = u8.get();
	}

	public int get() {
		return this.u8;
	}

	@Override
	public void write(OutputStream s) throws IOException {
		s.write((byte) this.u8);
	}

	@Override
	public HtspU8 create(InputStream s) throws IOException, ClassNotFoundException {
		byte[] buf = new byte[1];
		s.read(buf);
		return new HtspU8(((int) buf[0]) & 0xff);
	}

	@Override
	public long getNetLength() {
		return 1;
	}

	public static HtspU8 getInstance() {
		return U32Holder.INSTANCE;
	}

	private static class U32Holder {

		private static final HtspU8 INSTANCE = new HtspU8();
	}
}
