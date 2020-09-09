package dvbviewertimerimport.tvheadend.binobjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HtspBody extends HtspMap {

	public static int MIN_LENGTH = 4;

//	private long length;
//	private final Collection<HtspMsg> body;
//	private Map<String, HtspMsg> receiveMap = new HashMap<>();

	public HtspBody() {
//		this.length = 0;
//		this.body = new ArrayList<>();
	}

//	public void add(HtspMsg htsMsg) {
//		this.body.add(htsMsg);
//		this.length += htsMsg.getNetLength();
//	}
//
	@Override
	public synchronized void write(OutputStream s) throws IOException {
		new HtspU32(this.length).write(s);
		super.write(s);
	}

	@Override
	public HtspBody create(InputStream s) throws IOException, ClassNotFoundException, CloneNotSupportedException {

		long length = HtspU32.getInstance().create(s).get();

		HtspBody body = new HtspBody();
		
		body.load(s, length);

		return body;
	}

	@Override
	public long getNetLength() {
		return HtspU32.getInstance().getNetLength() + this.length;
	}

//	@Override
//	public HtspMsg getReceived(String name) {
//		HtspMsg received = this.receiveMap.get(name);
//		if (received == null) {
//			return HtspMsg.NULL_MESSAGE;
//		} else {
//			return received;
//		}
//	}
//
	public static HtspBody getInstance() {
		return RootBodyHolder.INSTANCE;
	}

	private static class RootBodyHolder {

		private static final HtspBody INSTANCE = new HtspBody();
	}

	public static HtspBody create(Object... objects) {
		HtspBody body = new HtspBody();
		for (Object obj : objects) {
			if (obj == null) {
				continue;
			}
			if (obj instanceof HtspBody) {
				for (HtspMsg msg : ((HtspBody) obj).map.values()) {
					body.add(msg);
				}
			} else {
				HtspMsg msg = HtspMsg.create(obj);
				if (msg != null) {
					body.add(msg);
				}
			}
		}
		return body;
	}


}
