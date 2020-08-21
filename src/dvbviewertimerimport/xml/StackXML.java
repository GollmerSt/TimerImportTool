// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.xml;

import java.util.Collections;
import java.util.Stack;
;

public class StackXML<T> extends Stack<T> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2022446046561115687L;

	public StackXML( @SuppressWarnings("unchecked") T... objects )
	{
		super() ;
		Collections.addAll( this, objects ) ;
	}
}
