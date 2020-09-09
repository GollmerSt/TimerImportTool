package dvbviewertimerimport.tvheadend.objects;

import java.util.HashMap;
import java.util.Map;

import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;

public class Channels {
	private Map<Long, Channel> channels = new HashMap<>();

	public class ChannelAdd extends MainObject<Channel> {

		@Override
		public void setByReceivedBody(HtspComplexObject<?> body) throws CloneNotSupportedException {
			Channel channel = new Channel();
			channel.setByReceivedBody(body);
			synchronized (Channels.this) {
				Channels.this.channels.put(channel.getChannelId(), channel);
			}
		}

		@Override
		public Channel create() {
			return null;
		}

	}

	public class ChannelUpdate extends MainObject<Channel> {

		@Override
		public void setByReceivedBody(HtspComplexObject<?> body) throws CloneNotSupportedException {
			Long id = body.getReceived("channelId").getLong();
			synchronized (Channels.this) {
				Channel channel = Channels.this.channels.get(id).clone();
				channel.setByReceivedBody(body);
				Channels.this.channels.put(channel.getChannelId(), channel);
			}
		}

		@Override
		public Channel create() {
			return null;
		}

	}

	public class ChannelDelete extends MainObject<Channel> {

		@Override
		public void setByReceivedBody(HtspComplexObject<?> body) {
			Long id = body.getReceived("channelId").getLong();
			synchronized (Channels.this) {
				Channels.this.channels.remove(id);
			}
		}

		@Override
		public Channel create() {
			return null;
		}

	}

}
