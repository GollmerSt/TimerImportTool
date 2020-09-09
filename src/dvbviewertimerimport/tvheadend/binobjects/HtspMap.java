package dvbviewertimerimport.tvheadend.binobjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HtspMap extends HtspComplexObject<HtspMap> implements HtspCollection, Cloneable {

	protected Map<String, HtspMsg> map = new HashMap<>();
	protected long length = 0L;

	public HtspMap() {

	}

	public HtspMap(Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			HtspMsg msg = HtspMsg.create(entry.getValue());
			msg.setName(entry.getKey());
			this.add(msg);
		}
	}

	@Override
	public HtspMap clone() throws CloneNotSupportedException {
		HtspMap map = (HtspMap) super.clone();
		map.map = new HashMap<>();
		for (Map.Entry<String, HtspMsg> entry : this.map.entrySet()) {
			map.map.put(entry.getKey(), entry.getValue());
		}
		return map;

	}

	public int size() {
		return this.map.size();
	}

	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return this.map.containsValue(value);
	}

	public HtspMsg get(Object key) {
		return this.map.get(key);
	}

	public HtspMsg add(HtspMsg value) {
		HtspMsg former = this.map.put(value.getName(), value);
		this.length += value.getNetLength();
		if (former != null) {
			this.length -= former.getNetLength();
		}
		return former;
	}

	public HtspMsg remove(Object key) {
		HtspMsg former = this.map.remove(key);
		if (former != null) {
			this.length -= former.getNetLength();
		}
		return former;
	}

	public void putAll(Collection<? extends HtspMsg> m) {
		for (HtspMsg msg : m) {
			this.add(msg);
		}

	}

	public void clear() {
		this.map.clear();
		this.length = 0L;
	}

	public Set<String> keySet() {
		return this.map.keySet();
	}

	@Override
	public Collection<HtspMsg> values() {
		return this.map.values();
	}

	public Set<Entry<String, HtspMsg>> entrySet() {
		return this.map.entrySet();
	}

	@Override
	public void write(OutputStream s) throws IOException {
		for (HtspMsg msg : this.map.values()) {
			msg.write(s);
		}

	}

	@Override
	public HtspMap create(InputStream s, long length)
			throws IOException, ClassNotFoundException, CloneNotSupportedException {
		HtspMap map = new HtspMap();
		map.load(s, length);
		return map;
	}

	protected void load(InputStream s, long length)
			throws ClassNotFoundException, IOException, CloneNotSupportedException {
		while (length > 0) {
			HtspMsg msg = HtspMsg.getInstance().create(s);
			this.add(msg);
			length -= msg.getNetLength();
		}

	}

	@Override
	public long getNetLength() {
		return this.length;
	}

	public static HtspMap getInstance() {
		return HtsMapHolder.INSTANCE;
	}

	private static class HtsMapHolder {

		private static final HtspMap INSTANCE = new HtspMap();
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	@Override
	public HtspMsg getReceived(String name) {
		HtspMsg msg = this.get(name);
		if (msg == null) {
			return HtspMsg.NULL_MESSAGE;
		} else {
			return msg;
		}
	}
}
