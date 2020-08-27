// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.control;

import javax.xml.stream.XMLStreamException;

import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;

public class Channel {

	private final int type;			// TVinfo :=0, TVGenial := 1, ClickFinder := 2, TVBrowser := 3

	private final String name;
	private String id;				//ID if provider supports ID (TVGenial) otherwise equal name

	public Channel(int type, String name, String id) {
		this.type = type;
		this.name = name;
		this.id = id;
	}

	public Channel(final Channel channel) {
		this(channel.type, channel.name, channel.id);
	}

	public void setID(long id) {
		this.id = id >= 0 ? Long.toString(id) : null;
	}

	public void setID(String id) {
		this.id = id;
	}

	public void setID(Object id) {
		if (id instanceof Long)
			this.setID(id.toString());
		else
			this.setID((String) id);
	}

	public int getType() {
		return this.type;
	};

	public String getName() {
		return this.name;
	};

	public String getTextID() {
		return this.id;
	};

	public long getNumID() {
		return Long.valueOf(this.id);
	};

	public String getTypeName() {
		return dvbviewertimerimport.provider.Provider.getProviderName(this.type);
	};

	public void writeXML(IndentingXMLStreamWriter sw) throws XMLStreamException {
		sw.writeStartElement("Provider");
		sw.writeAttribute("name", this.getTypeName());
		if (this.id != null)
			sw.writeAttribute("channelID", this.id);
		sw.writeCData(this.name);
		sw.writeEndElement();
	}

	@Override
	public String toString() {
		String out = this.getTypeName() + " channel: Name = \"" + this.name + "\"";
		if (this.id != null)
			out += "  Id = \"" + this.id + "\"";
		return out;
	}

	public Object getIDKey() {
		return null;
	}; // ID of the provider, type is provider dependent

	public Object getIDKey(final Channel c) {
		return c.getIDKey();
	}; // ID of the provider, type is provider dependent
}
