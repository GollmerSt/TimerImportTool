// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.control;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;

import dvbviewertimerimport.misc.Enums.Merge;
import dvbviewertimerimport.provider.Provider;

public class ChannelSet {

	public static void createIDs(Collection<ChannelSet> channelSets) {
		Collection<ChannelSet> unresolvedEntries = new ArrayList<ChannelSet>();
		long maxID = -1L;
		for (Iterator<ChannelSet> it = channelSets.iterator(); it.hasNext();) {
			ChannelSet cs = it.next();
			if (cs.id < 0)
				unresolvedEntries.add(cs);
			else
				maxID = Math.max(maxID, cs.id);
		}
		for (Iterator<ChannelSet> it = unresolvedEntries.iterator(); it.hasNext();) {
			ChannelSet cs = it.next();
			cs.id = ++maxID;
		}
	}

	private ArrayList<Channel> channels = new ArrayList<Channel>();
	private String dvbViewerChannelName = null;
	private String dvbViewerChannelId = null;
	private TimeOffsets timeOffsets = new TimeOffsets();
	private Merge merge = Merge.INVALID;
	private boolean isAutomaticAssigned = false;
	private long id = -1;

	public Channel add(int type, String name, String userName, String id, boolean user) {
		Channel channel = Provider.getProvider(type).createChannel(name, userName, id, user);
		this.channels.add(channel);
		return channel;
	}

	public Channel add(final Channel channel) {
		this.channels.add(channel);
		return channel;
	}

	public void remove(int type) {
		for (Iterator<Channel> it = this.channels.iterator(); it.hasNext();) {
			Channel channel = it.next();
			if (channel.getType() == type)
				it.remove();
		}
	}

	public boolean isAutomaticAssigned() {
		return this.isAutomaticAssigned;
	};

	public void setAutomaticAssigned(boolean val) {
		this.isAutomaticAssigned = val;
	};

	public void setTimeOffsets(TimeOffsets timeOffsets) {
		this.timeOffsets = timeOffsets;
	}

	public TimeOffsets getTimeOffsets() {
		return this.timeOffsets;
	};

	public void setMerge(Merge merge) {
		this.merge = merge;
	};

	public void setMerge(boolean merge) {
		if (merge)
			this.merge = Merge.TRUE;
		else
			this.merge = Merge.FALSE;
	};

	public Merge getMerge() {
		return this.merge;
	};

	public void setDVBViewerChannel(String channelName) {
		String [] parts;
		if (channelName == null) {
			parts = new String[] { null, null };
		} else {
			parts = channelName.split("\\|");
		}
		this.dvbViewerChannelName = parts[1];
		this.dvbViewerChannelId = parts[0];
	};

	public String getDVBViewerChannel() {
		if (this.dvbViewerChannelId == null) {
			return null;
		}
		return this.dvbViewerChannelId + '|' + this.dvbViewerChannelName;
	};

	public void setID(final long id) {
		this.id = id;
	};

	public long getID() {
		return this.id;
	};

	public ArrayList<Channel> getChannels() {
		return this.channels;
	};

	public Channel getChannel(int providerID) {
		for (Channel c : this.channels) {
			if (c.getType() == providerID)
				return c;
		}
		return null;
	}

	public void writeXML(IndentingXMLStreamWriter sw) throws XMLStreamException, ParseException {
		if (this.dvbViewerChannelId == null && this.channels.size() == 0) {
			return;
		}
		sw.writeStartElement("Channel");
		if (this.id >= 0)
			sw.writeAttribute("id", Long.toString(this.id));
		for (Channel c : this.channels)
			c.writeXML(sw);

		String dvbChannel = this.getDVBViewerChannel();

		if (dvbChannel != null) {
			sw.writeStartElement("DVBViewer");
			sw.writeCharacters(dvbChannel);
			sw.writeEndElement();
		}
		this.timeOffsets.writeXML(sw);
		if (this.merge != Merge.INVALID) {
			sw.writeStartElement("Merge");
			if (this.merge == Merge.FALSE)
				sw.writeCharacters("false");
			else
				sw.writeCharacters("true");
			sw.writeEndElement();
		}
		sw.writeEndElement();
	}

	@Override
	public String toString() {
		String out = "";

		if (this.dvbViewerChannelId != null)
			out += "DVBViewer channel: " + this.getDVBViewerChannel() + "\n";
		for (Channel c : this.channels)
			out += c.toString() + "\n";
		return out;
	}

	public String getDvbViewerChannelName() {
		return this.dvbViewerChannelName;
	}

	public String getDvbViewerChannelId() {
		return this.dvbViewerChannelId;
	}
}
