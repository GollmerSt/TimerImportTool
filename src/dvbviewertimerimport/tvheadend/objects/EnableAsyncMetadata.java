package dvbviewertimerimport.tvheadend.objects;

import dvbviewertimerimport.tvheadend.binobjects.HtspBody;
import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;
import dvbviewertimerimport.tvheadend.binobjects.HtspObject;

/**
 * When this is enabled the client will get continuous updates from the server
 * about added, update or deleted channels, tags, dvr and epg entries.
 * 
 * An interactive application that presents the user with information about
 * these things should probably enable this and the implement the various server
 * to client methods.
 * 
 * Once the reply as been sent the initial data set will be provided, and then
 * updates will arrive asynchronously after that. The initial data dump is sent
 * using the following messages:
 * 
 * tagAdd optional channelAdd optional tagUpdate optional dvrEntryAdd optional
 * eventAdd optional (Added in version 6) initialSyncComplete required
 * 
 * @author stefa_000
 *
 */

public class EnableAsyncMetadata extends Command<EnableAsyncMetadata> {

	/**
	 * optional Set to 1, to include EPG data in async, implied by epgMaxTime (Added
	 * in version 6).
	 */
	private final Long epg;

	/**
	 * optional Only provide metadata that has changed since this time (Added in
	 * version 6).
	 */
	private final Long lastUpdate;

	/**
	 * optional Maximum time to return EPG data up to (Added in version 6)
	 */
	private final Long epgMaxTime;

	/**
	 * optional RFC 2616 compatible language list (Added in version 6)
	 */
	private final String language;

	public EnableAsyncMetadata(boolean epg, Long lastUpdate, Long epgMaxTime, String language) {
		this.epg = epg ? 1L : null;
		this.lastUpdate = lastUpdate;
		this.epgMaxTime = epgMaxTime;
		this.language = language;
	}

	@Override
	public HtspBody getSendBody() {
		return HtspBody.create(//
				new HtspObject("epg", this.epg), //
				new HtspObject("lastUpdate", this.lastUpdate), //
				new HtspObject("epgMaxTime", this.epgMaxTime), //
				new HtspObject("language", this.language) //
		);
	}

	@Override
	public void setByReceivedBody(HtspComplexObject<?> body) {
		// TODO Auto-generated method stub

	}

	@Override
	public EnableAsyncMetadata create() {
		return new EnableAsyncMetadata(false, null, null, null);
	}

}
