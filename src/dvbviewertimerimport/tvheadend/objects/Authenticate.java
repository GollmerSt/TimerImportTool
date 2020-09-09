package dvbviewertimerimport.tvheadend.objects;

import dvbviewertimerimport.tvheadend.binobjects.HtspBody;
import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;

public class Authenticate extends Command<Authenticate> {
	
	/**
	 * optional   If set to 1, no privileges were granted.
	 */
	private boolean access;


	@Override
	public HtspBody getSendBody() {

		// Request message fields:
		//
		// None.
		//

		return null;
	}

	@Override
	public void setByReceivedBody(HtspComplexObject<?> body) {
		Long value = body.getReceived("noaccess").getLong();
		this.access = value == null? true:value != 1;
	}

	public boolean isAccess() {
		return this.access;
	}

	@Override
	public Authenticate create() {
		return new Authenticate();
	}

}
