package dvbviewertimerimport.tvheadend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import dvbviewertimerimport.tvheadend.Body.BodyValue;
import dvbviewertimerimport.tvheadend.HtsMessage.Format;

public class HtsMessage {
	public enum Format {
		MAP(1, new Body<BodyMap>(new BodyMap())), S64(2, new Signed64()), STR(3, new HString()), BIN(4,
				new Bin()), LIST(5, new Body<BodyCollection>(new BodyCollection()));

		private final byte id;
		private final Value<?> valueBase;

		private Format(int id, Value<?> valueBase) {
			this.id = (byte) id;
			this.valueBase = valueBase;
		}

		public byte getId() {
			return id;
		}

		public Value<?> createValue() {
			return this.valueBase.createValue();
		}

		public static Format getById(byte id) {
			for (Format format : Format.values()) {
				if (format.getId() == id) {
					return format;
				}
			}
			System.out.println("Unknown format id: " + id);
			return null;
		}
	}

	public static class Bin extends Value<Collection<Byte>> {

		public void add(byte b) {
			this.value.add(b);
			this.clearCache();
		}

		public Bin(String name, Binary bin) {
			super( name );
			this.value = new ArrayList<Byte>();
			for (byte b : bin.bytes) {
				this.value.add(b);
			}
		}

		public Bin(String name, int value) {
			this(value);
			this.setName(name);
		}

		public Bin(int in) {
			super();
			this.value = new ArrayList<Byte>(4);
			for (int i = 3; i >= 0; --i) {
				this.value.add((byte) ((in >> (8 * i)) & 0xff));
			}
		}

		public Bin() {
			super();
			this.value = new ArrayList<Byte>();
		}

		public int toInt() {
			int result = 0;
			for (byte b : value) {
				result = ( result << 8 ) + ((int)b & 0xff);
			}
			return result;
		}

		@Override
		public Format getFormat() {
			return Format.BIN;
		}

		@Override
		public Collection<Byte> getBytesUncached() {
			return this.value;
		}

		@Override
		public void set(Binary bytes, int start, int length) {
			this.value = new ArrayList<Byte>(length);
			for (int i = start; i < start + length; ++i) {
				this.value.add(bytes.get(i));
			}
			this.clearCache();
		}

		public void add(Byte b) {
			this.value.add(b);
			this.clearCache();
		}

		@Override
		public Value<Collection<Byte>> createValue() {
			return new Bin();
		}

		@Override
		public Collection<Byte> getContents() {
			return this.value;
		}

		@Override
		protected int getDataSize() {
			return this.value.size();
		}

		@Override
		public Collection<Byte> getBytes() {
			return this.value;
		}
		
		
		@Override
		public String toString() {
			return this.getContents().toString() ;
		}

	}

	public static class HString extends Value<String> {

		private static final Charset CHARSET = Charset.forName("UTF-8");

		public HString(String name, String value) {
			super(name);
			this.value = value;
		}

		public HString(String value) {
			super();
			this.value = value;
		}

		public HString() {
			super();
		}

		@Override
		public Format getFormat() {
			return Format.STR;
		}

		@Override
		public Collection<Byte> getBytesUncached() {
			ByteBuffer buffer = CHARSET.encode(value);
			Collection<Byte> out = new ArrayList<Byte>(buffer.remaining());
			while (buffer.hasRemaining()) {
				out.add(buffer.get());
			}
			return out;
		}

		@Override
		public Value<String> createValue() {
			return new HString();
		}

		@Override
		public String toString() {
			return this.value;
		}

		@Override
		public void set(Binary bytes, int start, int length) {
			ByteBuffer buffer = ByteBuffer.wrap(bytes.bytes, start, length);
			this.value = CHARSET.decode(buffer).toString();
			this.clearCache();
		}
	}

	private static class BodyCollection extends ArrayList<Value<?>> implements BodyValue {

		@Override
		public BodyValue create() {
			return new BodyCollection();
		}

		@Override
		public Value put(String key, Value value) {
			super.add(value);
			return value;
		}

		@Override
		public boolean add(Value arg0) {
			return super.add(arg0);
		}

		@Override
		public Format getFormat() {
			return Format.LIST;
		}

		@Override
		public Value get(Object name) {
			if (!(name instanceof String) || name == null) {
				return null;
			}
			for (Value value : this) {
				if (name.equals((String) name)) {
					return value;
				}
			}
			return null;
		}
	}

	public static class BodyMap extends HashMap<String, Value<?>> implements BodyValue {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6505746167524773091L;

		@Override
		public BodyValue create() {
			return new BodyMap();
		}

		@Override
		public Iterator<Value<?>> iterator() {
			return new Iterator<Value<?>>() {

				private Iterator<Value<?>> iterator = values().iterator();

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public Value<?> next() {
					return iterator.next();
				}
			};
		}

		@Override
		public Format getFormat() {
			return Format.MAP;
		}
	}

	public static class Signed64 extends Value<Long> {

		public Signed64() {
			super();
		}

		public Signed64(long value) {
			super();
			this.value = value;
		}

		public Signed64(String name, long value) {
			super(name);
			this.value = value;
		}

		@Override
		public Value<Long> createValue() {
			return new Signed64();
		}

		@Override
		public Format getFormat() {
			return Format.S64;
		}

		@Override
		public Collection<Byte> getBytesUncached() {
			Collection<Byte> result = new ArrayList<Byte>(4);
			long val = value;
			while (val != 0) {
				result.add((byte) (val & 0xff));
				val >>>= 8;
			}
			return result;
		}

		@Override
		public void set(Binary bytes, int start, int length) {
			this.value = 0L;
			for ( int i = start; i < start + length; ++i) {
				this.value = (this.value << 8) + ( (long) bytes.get(i) & 0xffL );
			}
		}
		
		
		@Override
		public String toString() {
			return Long.toString( this.getContents() ) ;
		}

	}

	public static class HMap extends Body<BodyMap> implements Map< String, Value<?>> {

		public HMap() {
			super(new BodyMap());
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -8131746996766667649L;

		@Override
		public void clear() {
			this.getValues().clear();
			
		}

		@Override
		public boolean containsKey(Object arg0) {
			return this.getValues().containsKey(arg0);
		}

		@Override
		public boolean containsValue(Object arg0) {
			return this.getValues().containsValue(arg0);
		}

		@Override
		public Set<java.util.Map.Entry<String, Value<?>>> entrySet() {
			return this.getValues().entrySet();
		}

		@Override
		public Value<?> get(Object arg0) {
			return this.getValues().get(arg0);
		}

		@Override
		public boolean isEmpty() {
			return this.getValues().isEmpty();
		}

		@Override
		public Set<String> keySet() {
			return this.getValues().keySet();
		}

		@Override
		public Value<?> put(String arg0, Value<?> arg1) {
			return this.getValues().put(arg0, arg1);
		}

		@Override
		public void putAll(Map<? extends String, ? extends Value<?>> arg0) {
			this.getValues().putAll(arg0);
			
		}

		@Override
		public Value<?> remove(Object arg0) {
			return this.getValues().remove(arg0);
		}

		@Override
		public int size() {
			return this.getValues().size() ;
		}

		@Override
		public Collection<Value<?>> values() {
			return this.getValues().values();
		}
		
		@Override
		public String toString() {
			return this.getValues().toString() ;
		}

	}

	public static class HList extends Body<BodyCollection> implements List< Value<?> > {

		public HList() {
			super(new BodyCollection());
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 3629828214660152913L;

		@Override
		public boolean add(Value arg0) {
			return this.getValues().add(arg0);
		}

		@Override
		public void add(int arg0, Value arg1) {
			this.getValues().add(arg0, arg1);
			
		}

		@Override
		public boolean addAll(Collection<? extends Value<?>> arg0) {
			return this.getValues().addAll( arg0);
		}

		@Override
		public boolean addAll(int arg0, Collection<? extends Value<?>> arg1) {
			return this.getValues().addAll(arg0, arg1);
		}

		@Override
		public void clear() {
			this.getValues().clear();
		}

		@Override
		public boolean contains(Object arg0) {
			return this.getValues().contains(arg0);
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			return this.getValues().containsAll(arg0);
		}

		@Override
		public Value get(int arg0) {
			return this.getValues().get(arg0);
		}

		@Override
		public int indexOf(Object arg0) {
			return this.getValues().indexOf(arg0);
		}

		@Override
		public boolean isEmpty() {
			return this.getValues().isEmpty();
		}

		@Override
		public Iterator<Value<?>> iterator() {
			return this.getValues().iterator() ;
		}

		@Override
		public int lastIndexOf(Object arg0) {
			return this.getValues().lastIndexOf(arg0);
		}

		@Override
		public ListIterator<Value<?>> listIterator() {
			return this.getValues().listIterator() ;
		}

		@Override
		public ListIterator<Value<?>> listIterator(int arg0) {
			return this.getValues().listIterator(arg0);
		}

		@Override
		public boolean remove(Object arg0) {
			return this.getValues().remove(arg0);
		}

		@Override
		public Value remove(int arg0) {
			return this.getValues().remove(arg0);
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			return this.getValues().removeAll(arg0);
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			return this.getValues().retainAll(arg0);
		}

		@Override
		public Value set(int arg0, Value arg1) {
			return this.getValues().set(arg0, arg1);
		}

		@Override
		public int size() {
			return this.getValues().size();
		}

		@Override
		public List<Value<?>> subList(int arg0, int arg1) {
			return this.getValues().subList(arg0, arg1);
		}

		@Override
		public Object[] toArray() {
			return this.getValues().toArray() ;
		}

		@Override
		public <T> T[] toArray(T[] arg0) {
			return this.getValues().toArray(arg0);
		}
		
		@Override
		public String toString() {
			return this.getValues().toString() ;
		}

	}

}
