package dvbviewertimerimport.tvheadend;

import java.util.Collection;

public class Helper {
	public static byte[] tobyte( Collection< Byte > bytes ) {
		int size = 0 ;
		for ( Byte b : bytes ) {
			if ( b != null ) {
				++size ;
			}
		}
		byte[] result = new byte[size] ;
		int i = 0 ;
		for ( Byte b : bytes ) {
			result[i++] = b ;
		}
		return result ;
	}
}
