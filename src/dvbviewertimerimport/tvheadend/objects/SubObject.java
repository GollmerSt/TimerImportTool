package dvbviewertimerimport.tvheadend.objects;

import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;

public abstract class SubObject< T extends SubObject<T>> {

	public abstract void setByReceivedBody(HtspComplexObject<?> body) throws CloneNotSupportedException;
	public abstract T create();

}
