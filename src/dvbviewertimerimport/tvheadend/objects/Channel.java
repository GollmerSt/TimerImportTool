package dvbviewertimerimport.tvheadend.objects;

import java.util.ArrayList;
import java.util.Collection;

import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;

/**
 * 
 * channelAdd
 * 
 * A new channel has been created on the server.
 * 
 * 
 * channelUpdate
 * 
 * Same as channelAdd, but all fields (except channelId) are optional.
 * channelDelete
 * 
 * A channel has been deleted on the server.
 * 
 * Message fields:
 * 
 * channelId u32 required ID of channel.
 * 
 * 
 * @author stefa_000
 *
 */

public class Channel extends MainObject<Channel> implements Cloneable{

	/**
	 * required ID of channel.
	 */
	long channelId;

	/**
	 * required Channel number, 0 means unconfigured.
	 */
	long channelNumber;

	/**
	 * optional Minor channel number (Added in version 13).
	 */
	Long channelNumberMinor = null;

	/**
	 * required Name of channel.
	 */
	private String channelName;

	/**
	 * optional URL to an icon representative for the channel (For v8+ clients this
	 * could be a relative /imagecache/ID URL intended to be fed to fileOpen() or
	 * HTTP server) (For v15+ clients this could be a relative imagecache/ID URL
	 * intended to be fed to fileOpen() or HTTP server)
	 */
	private String channelIconUrl = null;

	/**
	 * optional ID of the current event on this channel.
	 */
	private Long eventId = null;

	/**
	 * optional ID of the next event on the channel.
	 */
	private Long nextEventId = null;

	/**
	 * optional Tags this channel is mapped to.
	 */
	private Collection<Long> tags = null;

	/**
	 * optional List of available services (Added in version 5)
	 */
	private Collection<Service> services = null;

	public static class Service extends SubObject<Service> {

		/**
		 * required Service name
		 */
		private String name;

		/**
		 * required Service type
		 */
		private String type;

		/**
		 * optional Encryption CA ID
		 */
		private Long caId = null;

		/*
		 * optional Encryption CA name
		 */
		private String caName = null;

		@Override
		public void setByReceivedBody(HtspComplexObject<?> body) {
			this.name = body.getReceived("name").getString();
			this.type = body.getReceived("type").getString();
			this.caId = body.getReceived("caid").getLong();
			this.caName = body.getReceived("caname").getString();
		}

		@Override
		public Service create() {
			return new Service();
		}

		public String getName() {
			return this.name;
		}

		public String getType() {
			return this.type;
		}

		public Long getCaId() {
			return this.caId;
		}

		public String getCaName() {
			return this.caName;
		}
	}
	
	@Override
	public Channel clone() throws CloneNotSupportedException {
		Channel channel = (Channel) super.clone();
		channel.tags = new ArrayList<Long>( this.tags);
		channel.services = new ArrayList<Channel.Service>(this.services);
		return channel;
	}

	@Override
	public void setByReceivedBody(HtspComplexObject<?> body) throws CloneNotSupportedException {
		this.channelId = body.getReceived("channelId").getLong();
		this.channelNumber = body.getReceived("channelNumber").getLong(this.channelNumber);
		this.channelNumberMinor = body.getReceived("channelNumberMinor").getLong(this.channelNumberMinor);
		this.channelName = body.getReceived("channelName").getString(this.channelName);
		this.channelIconUrl = body.getReceived("channelIcon").getString(this.channelIconUrl);
		this.eventId = body.getReceived("eventId").getLong(this.eventId);
		this.nextEventId = body.getReceived("nextEventId").getLong(this.nextEventId);
		this.tags = body.getReceived("tags").getCollection(new Long(0), this.tags);
		this.services = body.getReceived("services").getCollection(new Service(), this.services);

//		/**
//		 * optional   List of available services (Added in version 5)
//		 */
//		private Collection< Service > services = null;

	}

	@Override
	public Channel create() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getChannelId() {
		return this.channelId;
	}

	public long getChannelNumber() {
		return this.channelNumber;
	}

	public Long getChannelNumberMinor() {
		return this.channelNumberMinor;
	}

	public String getChannelName() {
		return this.channelName;
	}

	public String getChannelIconUrl() {
		return this.channelIconUrl;
	}

	public Long getEventId() {
		return this.eventId;
	}

	public Long getNextEventId() {
		return this.nextEventId;
	}

	public Collection<Long> getTags() {
		return this.tags;
	}

	public Collection<Service> getServices() {
		return this.services;
	}

}
