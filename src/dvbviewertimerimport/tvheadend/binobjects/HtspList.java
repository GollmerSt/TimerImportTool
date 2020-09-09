package dvbviewertimerimport.tvheadend.binobjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

public class HtspList extends Htsp<HtspList> implements HtspCollection, Cloneable {

	private Collection<HtspMsg> list = new ArrayList<>();
	private long length = 0L;

	public HtspList() {
	}

	public HtspList(Collection<Object> col) {
		for (Object obj : col) {
			this.add(HtspMsg.create(obj));
		}
	}

	@Override
	public HtspList clone() throws CloneNotSupportedException {
		HtspList list = (HtspList) super.clone();
		list.list = new ArrayList<>();
		for (HtspMsg msg : this.list) {
			list.list.add(msg.clone());
		}
		return list;
	}

	public int size() {
		return this.list.size();
	}

	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	public boolean contains(Object key) {
		return this.list.contains(key);
	}

	public boolean add(HtspMsg value) {
		boolean replaced = this.list.add(value);
		if (replaced) {
			this.length += value.getNetLength();
		}
		return replaced;
	}

	public boolean remove(Object key) {
		boolean removed = this.list.remove(key);
		if (removed) {
			this.length -= ((HtspMsg) key).getNetLength();
		}
		return removed;
	}

	public boolean addAll(Collection<? extends HtspMsg> m) {
		boolean changed = false;
		for (HtspMsg msg : m) {
			changed |= this.list.add(msg);
		}
		return changed;

	}

	public void clear() {
		this.list.clear();
		this.length = 0;
	}

	@Override
	public void write(OutputStream s) throws IOException {
		for (HtspMsg msg : this.list) {
			msg.write(s);
		}

	}

	@Override
	public HtspList create(InputStream s, long length)
			throws IOException, ClassNotFoundException, CloneNotSupportedException {
		HtspList list = new HtspList();
		while (length > 0) {
			HtspMsg msg = HtspMsg.getInstance().create(s);
			list.add(msg);
			length -= msg.getNetLength();
		}
		return list;
	}

	@Override
	public long getNetLength() {
		return this.length;
	}

	public static HtspList getInstance() {
		return HtsListHolder.INSTANCE;
	}

	private static class HtsListHolder {

		private static final HtspList INSTANCE = new HtspList();
	}

	public boolean containsAll(Collection<?> c) {
		return this.list.containsAll(c);
	}

	public boolean removeAll(Collection<? extends HtspMsg> c) {
		boolean changed = false;
		for (HtspMsg msg : c) {
			changed |= this.list.remove(msg);
		}
		return changed;
	}

	@Override
	public Collection<HtspMsg> values() {
		return this.list;
	}

	@Override
	public String toString() {
		return this.list.toString();
	}

}
