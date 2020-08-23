package dvbviewertimerimport.tvheadend;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import dvbviewertimerimport.tvheadend.HtsMessage.Bin;
import dvbviewertimerimport.tvheadend.HtsMessage.Format;
import dvbviewertimerimport.tvheadend.HtsMessage.Signed64;

//#!/usr/bin/env python
//#
//# Copyright (C) 2012 Adam Sutton <dev@adamsutton.me.uk>
//#
//# This program is free software: you can redistribute it and/or modify
//# it under the terms of the GNU General Public License as published by
//# the Free Software Foundation, version 3 of the License.
//#
//# This program is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//# GNU General Public License for more details.
//#
//# You should have received a copy of the GNU General Public License
//# along with this program.  If not, see <http://www.gnu.org/licenses/>.
//#

/**
 * This class is based on the python code of Adam Sutton
 * <dev@adamsutton.me.uk> https://github.com/adamsutton/tvheadend/blob/master/
 * lib/py/tvh/htsmsg.py
 * @param <BodyValue>
 */

public class Body<T extends Body.BodyValue> extends Value< T > implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8168223235211683386L;

	public interface BodyValue extends Iterable<Value<?>> {
		public BodyValue create();

		public Value<?> put(String key, Value<?> value);

		public Format getFormat();

		public Value<?> get(Object name);
	}

	private static final long TIMEOUT_TIME = 10000; // 10s Timeout

	private final T values;

	@SuppressWarnings("unchecked")
	public Body( T data) {
		this.values = (T) data.create();
	}

	public Long getSequenceNo() {
		Value<?> value = this.values.get("seq");
		if (value == null || value.getFormat() != Format.S64) {
			return null;
		}
		return ((Signed64) value).getContents();
	}

	public void read(java.io.InputStream in) throws IOException, ClassNotFoundException {
		Binary bytes = readBytes(in, 4);
		Bin lengthBin = new Bin();
		lengthBin.set(bytes, 0, 4);
		int length = lengthBin.toInt();
		bytes = readBytes(in, length);
		Pointer.Integer pointer = new Pointer.Integer();
		while (pointer.getValue() < length) {
			Value<?> value = Value.deserialize(bytes, pointer);
			this.values.put(value.getName(), value);
		}
	}

	private static Binary readBytes(java.io.InputStream in, int length) throws IOException {
		Binary result = new Binary(length);
		long endTime = System.currentTimeMillis() + TIMEOUT_TIME;
		while (length > result.size()) {
			byte[] array = new byte[length-result.size()];
			int readed = in.read(array);
			if (readed < 0) {
				throw new IOException("EOF reached");
			}
			for (int i = 0; i < readed; ++i) {
				result.add(array[i]);
			}
			if (length < result.size()) {
				if (System.currentTimeMillis() > endTime) {
					throw new IOException("Timeout");
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
		}
		return result;
	}

	public void write(java.io.OutputStream out) throws IOException {

		int size = 4;

		for( Iterator<Value<?>> it = this.values.iterator(); it.hasNext();) {
			Value<?> value = it.next();
			size += value.serialize().size();
		}

		Binary bytes = new Binary(size);

		Bin bin = new Bin(size - 4);

		bytes.addAll(bin.getBytes());

		for ( Iterator<Value<?>> it = this.values.iterator(); it.hasNext();) {
			Value<?> value = it.next();
			bytes.addAll(value.serialize());
		}
		out.write(bytes.bytes);
		out.flush();
	}

	@Override
	public Body<T> createValue() {
		return new Body<T>(this.values);
	}

	@Override
	public Format getFormat() {
		return this.values.getFormat();
	}

	@Override
	public Collection<Byte> getBytesUncached() {
		List<Byte> result = new ArrayList<Byte>();
//		for (int i = 0; i < 4; ++i) {
//			result.add(null);
//		}
//		int size = 0;

		for ( Iterator<Value<?>> it = this.values.iterator(); it.hasNext();) {
			Value<?> value = it.next();
//			size += value.getDataSize();
			result.addAll(value.getBytes());
		}

//		Binary bytes = new Binary(size);
//
//		Bin bin = new Bin(bytes.size());
//
//		int i = 0;
//		for (Byte b : bin.getBytes()) {
//			result.set(i, b);
//		}
		return result;
	}

	@Override
	public void set(Binary bytes, int start, int length) {
//		Bin lengthBin = new Bin();
//		lengthBin.set(bytes, start, 4);
		Pointer.Integer pointer = new Pointer.Integer(start);
		while (pointer.getValue() < start+length) {
			Value<?> value = Value.deserialize(bytes, pointer);
			this.values.put(value.getName(), value);
		}
	}

	public T  getValues() {
		return this.values;
	}

}
