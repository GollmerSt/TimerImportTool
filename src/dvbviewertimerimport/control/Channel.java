// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.control;

import javax.xml.stream.XMLStreamException;

import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;

public class Channel {

	private final int type; // TVinfo :=0, TVGenial := 1, ClickFinder := 2, TVBrowser := 3

	private final String name;
	private final String id; // ID if provider supports ID (TVGenial) otherwise equal name
	private String userName;
	private boolean user;

	/**
	 * 
	 * @param type     TVinfo :=0, TVGenial := 1, ClickFinder := 2, TVBrowser := 3
	 * @param name     Provider name
	 * @param userName User name
	 * @param id       unique id
	 * @param user     Favorite channel
	 */
	public Channel(int type, String name, String userName, String id, boolean user) {
		this.type = type;
		this.name = name;

		this.id = id;
		this.user = user;
		this.setUserName(userName);

	}

	public Channel(final Channel channel) {
		this(channel.type, channel.name, channel.userName, channel.id, channel.user);
	}

	public Channel(int type, String name, String userName, long id, boolean user) {
		this(type, name, userName, id >= 0 ? Long.toString(id) : null, user);
	}

	public void setID(Object id) {
		if (id instanceof Long)
			this.setID(id.toString());
		else
			this.setID((String) id);
	}

	public int getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public String getTextID() {
		if (this.id != null) {
			return this.id;
		} else {
			return this.name;
		}
	}

	public long getNumID() {
		return Long.valueOf(this.id);
	}

	public String getTypeName() {
		return dvbviewertimerimport.provider.Provider.getProviderName(this.type);
	}

	public void writeXML(IndentingXMLStreamWriter sw) throws XMLStreamException {
		sw.writeStartElement("Provider");
		sw.writeAttribute("name", this.getTypeName());
		if (this.id != null)
			sw.writeAttribute("channelID", this.id);
		if (hasUserName()) {
			sw.writeAttribute("userName", this.userName);
		}
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

	public String getIDKey() {
		if (this.id != null) {
			return this.id;
		} else {
			return this.name;
		}
	}

	public boolean isUser() {
		return this.user;
	}

	public void setUser(boolean user) {
		this.user = user;
	}

	public final void setUserName(String userName) {
		this.userName = this.name.equals(userName) ? null : userName;
	}

	public String getUserName() {
		if (this.userName == null) {
			return this.name;
		} else {
			return this.userName;
		}
	}

	public boolean hasUserName() {
		return this.userName != null && !this.userName.equals(this.name);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Channel)) {
			return false;
		}
		Channel cmp = (Channel) o;

		return this.getTextID().equals(cmp.getTextID());
	}

	@Override
	public int hashCode() {
		return this.getTextID().hashCode();
	}

	public boolean hasId() {
		return this.id != null;
	}
}
