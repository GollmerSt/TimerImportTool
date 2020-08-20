package dvbviewertimerimport.tvheadend;

public class Pointer< T > {
	private T value ;
	
	public Pointer( T init ) {
		this.value = init ;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
	
	public static class Integer extends Pointer< java.lang.Integer > {

		public Integer(java.lang.Integer init) {
			super(init);
		}
		
		public Integer() {
			super(0) ;
		}
		
	}
	@Override
	public String toString() {
		return this.value.toString() ;
	}
}