package dvbviewertimerimport.tvheadend.binobjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class HtspString extends Htsp<HtspString> implements Cloneable {

	private static Charset charSet = Charset.forName("UTF-8");

	private byte[] bytes = null;
	private String text = null;

	public HtspString() {
	}

	public HtspString(String text) {
		this.text = text;
	}

	@Override
	public HtspString clone() {
		return new HtspString(this.text);
	}

	@Override
	public String toString() {
		return this.text;
	}

	@Override
	public void write(OutputStream s) throws IOException {
		if (this.bytes == null) {
			this.bytes = this.text.getBytes(charSet);
		}
		s.write(this.bytes);
	}

	@Override
	public HtspString create(InputStream s, long length) throws IOException, ClassNotFoundException {

		byte[] buffer = new byte[(int) length];

		s.read(buffer);

		HtspString string = new HtspString();

		string.bytes = buffer;
		string.text = new String(buffer, charSet);

		return string;
	}

	@Override
	public long getNetLength() {
		if (this.bytes == null) {
			this.bytes = this.text.getBytes(charSet);
		}
		// TODO Auto-generated method stub
		return this.bytes.length;
	}

	public static HtspString getInstance() {
		return HtsStringHolder.INSTANCE;
	}

	private static class HtsStringHolder {
		private static final HtspString INSTANCE = new HtspString();
	}

}
