package dvbviewertimerimport.tvheadend;

public class HtsExceptions {
	public static class SequenceNumberException extends Exception {
		
		public SequenceNumberException() {
			super() ;
		}
		
		public SequenceNumberException( long target, long current ) {
			super("Wrong sequence number, target: " + target + ", current: " + current ) ;
		}
		
	}
	
	public static class ReceiveException extends Exception {
		
		public ReceiveException( String errorText ) {
			super(errorText) ;
		}
		
	}

	public static class AccessProhibitedException extends Exception {
		
		public AccessProhibitedException() {
			super() ;
		}
		
	}

}
