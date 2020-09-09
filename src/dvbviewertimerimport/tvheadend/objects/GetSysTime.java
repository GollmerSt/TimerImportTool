package dvbviewertimerimport.tvheadend.objects;

import dvbviewertimerimport.tvheadend.binobjects.HtspBody;
import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;

public class GetSysTime extends Command<GetSysTime> {
	
	/**
	 * required   UNIX time.
	 */
	long time ;
	
	/**
	 * required   Hours west of GMT. (deprecated, does not work reliable, use gmtoffset instead)
	 */
	long timeZone;
	
	/**
	 * optional   Minutes east of GMT.
	 */
	Long gmtOffset;

	@Override
	public HtspBody getSendBody() {
		return null;
	}

	@Override
	public void setByReceivedBody(HtspComplexObject<?> body) {
		this.time = body.getReceived("time").getLong();
		this.timeZone = body.getReceived("timezone").getLong();
		this.gmtOffset = body.getReceived("gmtoffset").getLong();
	}

	@Override
	public GetSysTime create() {
		return new GetSysTime();
	}

}
