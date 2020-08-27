package dvbviewertimerimport.tvheadend;

public class HtsExceptions {
	public static class SequenceNumberException extends Exception {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1695634925503657253L;

		public SequenceNumberException() {
			super() ;
		}
		
		public SequenceNumberException( long target, long current ) {
			super("Wrong sequence number, target: " + target + ", current: " + current ) ;
		}
		
	}
	
	public static class ReceiveException extends Exception {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3277266130379899890L;

		public ReceiveException( String errorText ) {
			super(errorText) ;
		}
		
	}

	public static class AccessProhibitedException extends Exception {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8684974966232906072L;

		public AccessProhibitedException() {
			super() ;
		}
		
	}

}
