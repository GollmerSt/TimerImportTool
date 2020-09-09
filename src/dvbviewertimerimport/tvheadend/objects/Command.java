package dvbviewertimerimport.tvheadend.objects;

import dvbviewertimerimport.tvheadend.binobjects.HtspBody;

public abstract class Command<T extends SubObject<T>> extends MainObject<T> {

	public abstract HtspBody getSendBody();

}
