// $LastChangedDate: 2010-03-13 10:38:41 +0100 (Sa, 13. Mrz 2010) $
// $LastChangedRevision: 197 $
// $LastChangedBy: Stefan Gollmer $

package dvbviewertimerimport.xml;

import java.util.Collections;
import java.util.Stack;
;

public class StackXML<T> extends Stack<T> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2022446046561115687L;

	public StackXML( T... objects )
	{
		super() ;
		Collections.addAll( this, objects ) ;
	}
}
