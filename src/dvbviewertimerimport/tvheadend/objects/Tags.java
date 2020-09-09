package dvbviewertimerimport.tvheadend.objects;

import java.util.HashMap;
import java.util.Map;

import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;

public class Tags {
	private Map<Long, Tag> tags = new HashMap<>();

	public class TagAdd extends MainObject<Tag> {

		@Override
		public void setByReceivedBody(HtspComplexObject<?> body) throws CloneNotSupportedException {
			Tag tag = new Tag();
			tag.setByReceivedBody(body);
			synchronized (Tags.this) {
				Tags.this.tags.put(tag.getTagId(), tag);
			}
		}

		@Override
		public Tag create() {
			return null;
		}

	}

	public class TagUpdate extends MainObject<Tag> {

		@Override
		public void setByReceivedBody(HtspComplexObject<?> body) throws CloneNotSupportedException {
			Long tagId = body.getReceived("tagId").getLong();
			synchronized (Tags.this) {
				Tag tag = Tags.this.tags.get(tagId).clone();
				tag.setByReceivedBody(body);
				Tags.this.tags.put(tag.getTagId(), tag);
			}
		}

		@Override
		public Tag create() {
			return null;
		}

	}

	public class TagDelete extends MainObject<Tag> {

		@Override
		public void setByReceivedBody(HtspComplexObject<?> body) {
			Long tagId = body.getReceived("tagId").getLong();
			synchronized (Tags.this) {
				Tags.this.tags.remove(tagId);
			}
		}

		@Override
		public Tag create() {
			return null;
		}

	}
}
