package dvbviewertimerimport.tvheadend.objects;

import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;
import dvbviewertimerimport.tvheadend.binobjects.HtspMsg;

/**
 * A recording
 * 
 * 
 * Valid values for state:
 * 
 * TODO
 * 
 * 
 * 
 * 
 * @author stefa_000
 *
 */

public class DvrEntry extends MainObject<DvrEntry> implements Cloneable {

	/**
	 * required ID of dvrEntry.
	 */
	private long id;

	/**
	 * optional Channel of dvrEntry.
	 */
	private Long channel;

	/**
	 * required Time of when this entry was scheduled to start recording.
	 */
	private long start;

	/**
	 * required Time of when this entry was scheduled to stop recording.
	 */
	private long stop;

	/**
	 * required Extra start time (pre-time) in minutes (Added in version 13).
	 */
	private long startExtra;

	/**
	 * required required Extra stop time (post-time) in minutes (Added in version
	 * 13).
	 */
	private long stopExtra;

	/**
	 * required DVR Entry retention time in days (Added in version 13).
	 */
	private long retention;

	/**
	 * required Priority (0 = Important, 1 = High, 2 = Normal, 3 = Low, 4 =
	 * Unimportant, 5 = Not set) (Added in version 13).
	 */
	private long priority;

	/**
	 * optional Associated EPG Event ID (Added in version 13).
	 */
	private Long eventId;

	/**
	 * optional Associated Autorec UUID (Added in version 13).
	 */
	private String autorecId;

	/**
	 * optional Associated Timerec UUID (Added in version 18).
	 */
	private String timerecId;

	/**
	 * optional Content Type (like in the DVB standard) (Added in version 13).
	 */
	private Long contentType;

	/**
	 * str optional Title of recording
	 */
	private String title;

	/**
	 * optional Subtitle of recording (Added in version 20).
	 */
	private String subtitle;

	/**
	 * optional Short description of the recording (Added in version 6).
	 */
	private String summary;

	/**
	 * optional Long description of the recording.
	 */
	private String description;

	/**
	 * required Recording state
	 */
	private String state;

	/**
	 * optional Plain english error description (e.g. "Aborted by user").
	 */
	private String error;

	/**
	 * optional Name of the entry owner (Added in version 18).
	 */
	private String owner;

	/**
	 * str optional Name of the entry creator (Added in version 18).
	 */
	private String creator;

	/**
	 * optional Subscription error string (Added in version 20).
	 * 
	 * Valid values for subscriptionError:
	 * 
	 * noFreeAdapter: No free adapter for this service. scrambled Service is
	 * scrambled.
	 * 
	 * badSignal: Bad signal status. tuningFailed Tuning of this service failed.
	 * 
	 * subscriptionOverridden: Subscription overridden by another one.
	 * 
	 * muxNotEnabled: No mux enabled for this service.
	 * 
	 * invalidTarget: Recording/livestream cannot be saved to filesystem or
	 * recording/streaming configuration is incorrect.
	 * 
	 * userAccess: User does not have access rights for this service.
	 * 
	 * userLimit: Maximum number of streaming connections set in user's profile
	 * reached.
	 * 
	 */
	private String subscriptionError;

	/**
	 * optional Number of recording errors (Added in version 20).
	 */
	private String streamErrors;

	/**
	 * optional Number of stream data errors (Added in version 20).
	 */
	private String dataErrors;

	/**
	 * optional Recording path for playback.
	 */
	private String path;

	/**
	 * msg optional All recorded files for playback (Added in version 21).
	 */
	private HtspMsg files; // TODO

	/**
	 * optional Actual file size of the last recordings (Added in version 21).
	 */
	private Long dataSize;

	/**
	 * optional Enabled flag (Added in version 23).
	 */
	private Long enabled;
	
	@Override
	public DvrEntry clone() throws CloneNotSupportedException {
		DvrEntry entry = (DvrEntry) super.clone();
		entry.files = (HtspMsg) this.files.clone();
		return entry;
	}

	@Override
	public void setByReceivedBody(HtspComplexObject<?> body) {
		this.id = body.getReceived("id").getLong();
		this.channel = body.getReceived("channel").getLong(this.channel);
		this.start = body.getReceived("start").getLong(this.start);
		this.stop = body.getReceived("stop").getLong(this.stop);
		this.startExtra = body.getReceived("startExtra").getLong(this.startExtra);
		this.stopExtra = body.getReceived("stopExtra").getLong(this.stopExtra);
		this.retention = body.getReceived("retention").getLong(this.retention);
		this.priority = body.getReceived("priority").getLong(this.priority);
		this.eventId = body.getReceived("eventId").getLong(this.eventId);
		this.autorecId = body.getReceived("autorecId").getString(this.autorecId);
		this.timerecId = body.getReceived("timerecId").getString(this.timerecId);
		this.contentType = body.getReceived("contentType").getLong(this.contentType);
		this.title = body.getReceived("title").getString(this.title);
		this.subtitle = body.getReceived("subtitle").getString(this.subtitle);
		this.summary = body.getReceived("summary").getString(this.summary);
		this.description = body.getReceived("description").getString(this.description);
		this.state = body.getReceived("state").getString(this.state);
		this.error = body.getReceived("error").getString(this.error);
		this.owner = body.getReceived("owner").getString(this.owner);
		this.creator = body.getReceived("creator").getString(this.creator);
		this.subscriptionError = body.getReceived("subscriptionError").getString(this.subscriptionError);
		this.streamErrors = body.getReceived("streamErrors").getString(this.streamErrors);
		this.dataErrors = body.getReceived("dataErrors").getString(this.dataErrors);
		this.path = body.getReceived("path").getString(this.path);
		HtspMsg msg = body.getReceived("files");
		if (msg != HtspMsg.NULL_MESSAGE ) {
			this.files = msg;
		}
		this.dataSize = body.getReceived("dataSize").getLong(this.dataSize);
		this.enabled = body.getReceived("enabled").getLong(this.enabled);

	}

	@Override
	public DvrEntry create() {
		return null;
	}

	public long getId() {
		return this.id;
	}

	public Long getChannel() {
		return this.channel;
	}

	public long getStart() {
		return this.start;
	}

	public long getStop() {
		return this.stop;
	}

	public long getStartExtra() {
		return this.startExtra;
	}

	public long getStopExtra() {
		return this.stopExtra;
	}

	public long getRetention() {
		return this.retention;
	}

	public long getPriority() {
		return this.priority;
	}

	public Long getEventId() {
		return this.eventId;
	}

	public String getAutorecId() {
		return this.autorecId;
	}

	public String getTimerecId() {
		return this.timerecId;
	}

	public Long getContentType() {
		return this.contentType;
	}

	public String getTitle() {
		return this.title;
	}

	public String getSubtitle() {
		return this.subtitle;
	}

	public String getSummary() {
		return this.summary;
	}

	public String getDescription() {
		return this.description;
	}

	public String getState() {
		return this.state;
	}

	public String getError() {
		return this.error;
	}

	public String getOwner() {
		return this.owner;
	}

	public String getCreator() {
		return this.creator;
	}

	public String getSubscriptionError() {
		return this.subscriptionError;
	}

	public String getStreamErrors() {
		return this.streamErrors;
	}

	public String getDataErrors() {
		return this.dataErrors;
	}

	public String getPath() {
		return this.path;
	}

	public HtspMsg getFiles() {
		return this.files;
	}

	public Long getDataSize() {
		return this.dataSize;
	}

	public Long getEnabled() {
		return this.enabled;
	}

}
