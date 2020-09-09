package dvbviewertimerimport.tvheadend.objects;

import dvbviewertimerimport.tvheadend.TvHeadend;
import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;

public class InitialSyncCompleted extends MainObject<InitialSyncCompleted> {

	private final TvHeadend tvHeadend;

	public InitialSyncCompleted(TvHeadend tvHeadend) {
		this.tvHeadend = tvHeadend;
	}

	@Override
	public void setByReceivedBody(HtspComplexObject<?> body) {
		this.tvHeadend.setInitialSyncCompleted();

	}

	@Override
	public InitialSyncCompleted create() {
		return null;
	}

}
