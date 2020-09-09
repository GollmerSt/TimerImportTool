package dvbviewertimerimport.tvheadend.binobjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Htsp<T> implements Cloneable{
	public abstract void write(OutputStream s) throws IOException;

	public T create(InputStream s, long length) throws IOException, ClassNotFoundException, CloneNotSupportedException {
		return null;
	}

	public T create(InputStream s) throws IOException, ClassNotFoundException, CloneNotSupportedException {
		return null;
	}

	public abstract long getNetLength();
	
	@SuppressWarnings("unchecked")
	@Override
	public Htsp<T> clone() throws CloneNotSupportedException {
		return (Htsp<T>) super.clone();
	}
}