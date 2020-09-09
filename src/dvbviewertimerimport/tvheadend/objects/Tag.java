package dvbviewertimerimport.tvheadend.objects;

import java.util.ArrayList;
import java.util.Collection;

import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;

/**
 * tagAdd¶
 * 
 * A new tag has been created on the server.
 * 
 * 
 * tagUpdate
 * 
 * Same as tagAdd, but all fields (except tagId) are optional. tagDelete
 * 
 * A tag has been deleted from the server.
 * 
 * Message fields:
 * 
 * tagId u32 required ID of tag.
 * 
 * @author stefa_000
 *
 */

public class Tag extends MainObject<Tag> implements Cloneable {

	/**
	 * required ID of tag.
	 */
	private long tagId;

	/**
	 * required Name of tag.
	 */
	private String tagName;

	/**
	 * optional Index value for sorting (default by from min to max) (Added in
	 * version 18).
	 */
	Long tagIndex = null;

	/**
	 * optional URL to an icon representative for the channel.
	 */
	private String tagIcon = null;

	/**
	 * optional Icon includes a title
	 */
	private Long tagTitledIcon = null;

	/**
	 * optional Channel IDs of those that belong to the tag
	 */
	private Collection<Long> members = null;

	@Override
	public Tag clone() throws CloneNotSupportedException {
		Tag tag = (Tag) super.clone();
		if (this.members != null) {
			tag.members = new ArrayList<Long>(this.members);
		}
		return tag;
	}

	@Override
	public void setByReceivedBody(HtspComplexObject<?> body) throws CloneNotSupportedException {
		this.tagId = body.getReceived("tagId").getLong();
		this.tagName = body.getReceived("tagName").getString(this.tagName);
		this.tagIndex = body.getReceived("tagIndex").getLong(this.tagIndex);
		this.tagIcon = body.getReceived("tagIcon").getString(this.tagIcon);
		this.tagTitledIcon = body.getReceived("tagIndex").getLong(this.tagTitledIcon);
		this.members = body.getReceived("members").getCollection(new Long(0), this.members);

	}

	@Override
	public Tag create() {
		return null;
	}

	public long getTagId() {
		return this.tagId;
	}

	public String getTagName() {
		return this.tagName;
	}

	public Long getTagIndex() {
		return this.tagIndex;
	}

	public String getTagIcon() {
		return this.tagIcon;
	}

	public Long getTagTitledIcon() {
		return this.tagTitledIcon;
	}

	public Collection<Long> getMembers() {
		return this.members;
	}

}
