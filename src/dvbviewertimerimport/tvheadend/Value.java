package dvbviewertimerimport.tvheadend;

import java.util.ArrayList;
import java.util.Collection;

import dvbviewertimerimport.tvheadend.HtsMessage.Bin;
import dvbviewertimerimport.tvheadend.HtsMessage.Format;
import dvbviewertimerimport.tvheadend.HtsMessage.HString;


public abstract class Value< T > {
	
	private HString name = null ;
	protected Collection< Byte > cache = null ;
	protected T value ; 

	public Collection<Byte> getCache() {
		return cache;
	}

	public void setCache(Collection<Byte> cache) {
		this.cache = cache;
	}

	public String getName() {
		if ( this.name == null ) {
			return null ;
		}
		return name.toString() ;
	}

	public void setName(String name) {
		HString n = new HString() ;
		n.setContens( name );
		this.name = n ;
	}
	
	public void setContens( T contents ) {
		this.value = contents ;
		this.clearCache();
	}
	
	public T getContents() {
		return this.value ;
	}

	public abstract Value< T > createValue();

	public abstract Format getFormat();
	
	public abstract Collection< Byte > getBytesUncached() ;
	
	protected void clearCache() {
		this.cache = null ;
	}

	public Collection<Byte> getBytes() {
		if ( this.cache == null ) {
			this.cache = this.getBytesUncached() ;
		}
		return this.cache ;
	}
	
	protected int getDataSize() {
		return this.getBytes().size() ;
	}

	
	public int getSerializesSize() {
		int size = 1 + 1 + 4 + name.getDataSize() + this.getDataSize() ;
		return size ;
	}
	

	public abstract void set(Binary bytes, int start, int length);

	public Collection< Byte > serialize() {
		Collection< Byte > result = new ArrayList<Byte>() ;
		result.add(this.getFormat().getId());
		Collection<Byte> nameBytes = new HString(this.name.toString()).getBytes();
		Collection<Byte> dataBytes = this.getBytes();
		result.add((byte) nameBytes.size());
		Bin length = new Bin(dataBytes.size());
		result.addAll(length.getBytes());
		result.addAll(nameBytes);
		result.addAll(dataBytes);
		return result;
	}

	public static Value<?> deserialize(Binary serialized, Pointer.Integer intPointer) {
		int pointer = intPointer.getValue();
		Byte formatByte = serialized.get(pointer++);
		Format format = Format.getById(formatByte);
		Value<?> value = format.createValue();
		Byte nameLength = serialized.get(pointer++);
		Bin lengthBin = new Bin();
		lengthBin.set(serialized, pointer, 4);
		pointer += 4;
		int size = lengthBin.toInt();
		HString hName = new HString();
		hName.set(serialized, pointer, nameLength);
		pointer += nameLength;
		value.setName(hName.toString());
		value.set(serialized, pointer, size);
		pointer += size;
		intPointer.setValue(pointer);
		return value;
	}

	public Value(String name) {
		this.setName( name ) ;
	}

	public Value() {
	}
}