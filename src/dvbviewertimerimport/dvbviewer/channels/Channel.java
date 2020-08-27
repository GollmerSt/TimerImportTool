// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer.channels;

import java.nio.MappedByteBuffer;

import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.misc.TerminateClass;

public class Channel {
	public static final int ENTRY_LENGTH = Tuner.ENTRY_LENGTH + 26 * 3 + 2;
	private final Channels channels;
	private boolean isVideo = true;
	private String channelName = null;
	private String channelID = null;

	public Channel(Channels channels) {
		this.channels = channels;
	}

	public Channel() {
		this.channels = null;
		this.channelName = "<none>";
	}

	public void read() {
		Tuner tuner = new Tuner(this.channels);
		tuner.read();

		this.isVideo = tuner.isVideo();

		this.channels.readString(26);
		this.channelName = this.channels.readString(26);
		this.channels.readString(26);

		MappedByteBuffer buffer = this.channels.getMappedByteBuffer();

		buffer.get();
		buffer.get();

		long id = (tuner.getType() + 1) << 29;
		id |= tuner.getAudioPID() << 16;
		id |= tuner.getServiceID();

		if (this.channelName == null) {
			return;
		}
		this.channelID = Long.toString(id) + "|" + this.channelName;
	}

	public String getChannelName() {
		return this.channelName;
	};

	@Override
	public String toString() {
		return this.channelName;
	};

	public String getChannelID() {
		return this.channelID;
	}

	public boolean isVideo() {
		return this.isVideo;
	};

	public boolean isFailed() {
		return this.channelName == null;
	};

	public static Channel createByChannelID(String channelID) {
		String[] parts = channelID.split("[|]");
		if (parts.length != 2) {
			Log.error(ResourceManager.msg("ILLEGAL_FORMAT_DVBVIEWER_CHANNELID", channelID));
			throw new TerminateClass(1);
		}
		Channel c = new Channel();
		c.channelName = parts[1];
		c.channelID = channelID;
		return c;
	}
}
