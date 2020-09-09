package dvbviewertimerimport.tvheadend.binobjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.tvheadend.objects.SubObject;

public class HtspMsg extends HtspComplexObject<HtspMsg> implements Cloneable{

	public static HtspMsg NULL_MESSAGE = new HtspMsg();

	public enum Type {
		MAP(1, HtspMap.getInstance(), Map.class), // Sub message of type map
		S64(2, HtspS64.getInstance(), Long.class), // Signed 64bit integer
		STR(3, HtspString.getInstance(), String.class), // UTF-8 encoded string
		BIN(4, HtspBinary.getInstance(), byte[].class), // HtspBinary blob
		LIST(5, HtspList.getInstance(), Collection.class), // Sub message of type list
		S32(2, HtspS64.getInstance(), Integer.class), // Signed 32bit integer)
		NULL(null, null, null); // Null msg;

		private final Integer id;
		private final Htsp<?> instance;
		private final Class<?> clazz;

		private Type(Integer id, Htsp<?> instance, Class<?> clazz) {
			this.id = id;
			this.instance = instance;
			this.clazz = clazz;
		}

		public int getId() {
			return this.id;
		}

		public static Type get(int id) {
			for (Type type : values()) {
				if (type.id != null && id == type.getId()) {
					return type;
				}
			}
			return null;
		}

		public static Type get(Object obj) {
			for (Type type : values()) {
				if (type.clazz != null && type.clazz.isInstance(obj)) {
					return type;
				}
			}
			return null;
		}

		public Htsp<?> getInstance() {
			return this.instance;
		}
	}

	private Type type;
	private String name;
	private HtspString htspString = null;
	private Htsp<?> data;
	private Long length = null;

	public HtspMsg() {
		this.type = Type.NULL;
	}
	
	@Override
	public HtspMsg clone() throws CloneNotSupportedException {
		HtspMsg msg = (HtspMsg) super.clone();
		this.data = (Htsp<?>) this.data.clone();
		this.htspString = null;
		return msg;
	}

	@Override
	public void write(OutputStream s) throws IOException {
		new HtspU8(this.type.getId()).write(s);
		new HtspU8((int) this.getHtspName().getNetLength()).write(s);
		new HtspU32(this.data.getNetLength()).write(s);
		this.getHtspName().write(s);
		this.data.write(s);
	}
	
	private HtspString getHtspName() {
		if ( this.htspString == null ) {
			this.htspString = new HtspString(this.name);
		}
		return this.htspString;
	}

	@Override
	public HtspMsg create(InputStream s) throws IOException, ClassNotFoundException, CloneNotSupportedException {
		HtspMsg msg = new HtspMsg();
		msg.type = Type.get(HtspU8.getInstance().create(s).get());
		int nameLength = HtspU8.getInstance().create(s).get();
		long dataLength = HtspU32.getInstance().create(s).get();
		HtspString name = HtspString.getInstance().create(s, nameLength);
		msg.name = name.toString();
		msg.data = (Htsp<?>) msg.type.getInstance().create(s, dataLength);
		msg.length = 2 * HtspU8.getInstance().getNetLength() + HtspU32.getInstance().getNetLength() + nameLength
				+ dataLength;
		return msg;
	}

	@Override
	public long getNetLength() {
		if (this.length == null) {
			this.length = 2 * HtspU8.getInstance().getNetLength() + HtspU32.getInstance().getNetLength() + this.getHtspName().getNetLength()
			+ this.data.getNetLength();
		}
		return this.length;
	}

	public static HtspMsg getInstance() {
		return HtsMsgHolder.INSTANCE;
	}

	private static class HtsMsgHolder {
		private static final HtspMsg INSTANCE = new HtspMsg();
	}

	public String getName() {
		return this.name.toString();
	}

	@SuppressWarnings("unchecked")
	public static HtspMsg create(Object arg) {
		if (arg instanceof HtspMsg) {
			return (HtspMsg) arg;
		}
		if (!(arg instanceof HtspObject)) {
			throw new ErrorClass("HtspMsg creator arguments mus be of types HtspMsg or HTsObject");
		}
		HtspObject htsObject = (HtspObject) arg;
		Object obj = htsObject.getObj();
		if (obj == null) {
			return null;
		}
		HtspMsg msg = new HtspMsg();
		msg.setName(htsObject.getName());
		msg.type = Type.get(obj);
		switch (msg.type) {
			case BIN:
				msg.data = new HtspBinary((byte[]) obj);
				break;
			case STR:
				msg.data = new HtspString((String) obj);
				break;
			case S64:
				msg.data = new HtspS64(((Long) obj).longValue());
				break;
			case S32:
				msg.data = new HtspS64(((Integer) obj).longValue());
				break;
			case LIST:
				msg.data = new HtspList((Collection<Object>) obj);
				break;
			case MAP:
				msg.data = new HtspMap((Map<String, Object>) obj);
				break;
			default:
				throw new ErrorClass("HtspMsg creator of class" + obj.getClass().toString() + " not defined.");
		}
		return msg;
	}

	public void setName(String name) {
		this.name = name;
		this.htspString = null;
	}

	public Type getType() {
		return this.type;
	}

	public String getString() {
		if (this.type == Type.STR) {
			return ((HtspString) this.data).toString();
		}
		return null;
	}

	public String getString( String ifNullString ) {
		String string = this.getString();
		return string == null?ifNullString:string;
	}

	public Long getLong() {
		if (this.type == Type.S64) {
			HtspS64 s64 = (HtspS64) this.data;
			if (s64 != null) {
				return ((HtspS64) this.data).get();
			}
		}
		return null;
	}
	
	public Long getLong( Long ifNullValue ) {
		Long value = this.getLong();
		return value == null?ifNullValue:value;
	}

	public byte[] getBinary() {
		if (this.type == Type.BIN) {
			return ((HtspBinary) this.data).get();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T getObject(T base) throws CloneNotSupportedException {
		Type type = Type.get(base);
		if (type != null) {
			switch (type) {
				case BIN:
					return (T) this.getBinary();
				case S64:
					return (T) this.getLong();
				case STR:
					return (T) getString();
				default:
					return null;
			}
		} else if (base instanceof SubObject) {
			SubObject<?> out = ((SubObject<?>) base).create();
			out.setByReceivedBody(this);
			return (T) out;
		}
		return null;
	}

//	MAP(1, HtspMap.getInstance()), // Sub message of type map
//	LIST(5, HtspList.getInstance()); // Sub message of type list

	public <T> Collection<T> getCollection(T base) throws CloneNotSupportedException {
		Collection<HtspMsg> source;
		Collection<T> destin = new ArrayList<>();
		switch (this.type) {
			case LIST:
			case MAP:
				source = ((HtspCollection) this.data).values();
				break;
			case BIN:
			case S64:
			case STR:
				if ( this.type == Type.get(base)) {
					T element = this.getObject(base);
					if (element == null) {
						return null;
					}
					destin.add(element);
					return destin ;
				}
			default:
				return null;
		}
		for (HtspMsg msg : source) {
			T element = msg.getObject(base);
			if (element == null) {
				return null;
			}
			destin.add(element);
		}
		return destin;

	}
	
	public <T> Collection<T> getCollection(T base, Collection<T> ifNullCollection ) throws CloneNotSupportedException {
		Collection<T> collection = getCollection(base);
		return collection == null? ifNullCollection:collection;
	}


	public boolean isNull() {
		return this.type == Type.NULL;
	}

	@Override
	public String toString() {
		return "Name: " + this.getName() + ", Data: " + this.data == null ? "<null>:" : this.data.toString();
	}

	@Override
	public HtspMsg getReceived(String name) {
		switch (this.type) {
			case MAP:
				HtspMsg msg = ((HtspMap) this.data).get(name);
				if (msg == null) {
					return NULL_MESSAGE;
				} else {
					return msg;
				}

			default:
				break;
		}
		return null;
	}

}
