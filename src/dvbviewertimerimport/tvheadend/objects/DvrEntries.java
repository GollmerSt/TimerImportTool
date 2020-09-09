package dvbviewertimerimport.tvheadend.objects;

import java.util.HashMap;
import java.util.Map;

import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;

public class DvrEntries {
	private Map< Long, DvrEntry > dvrEntries = new HashMap<>();
	
	public class DvrEntryAdd extends MainObject< DvrEntry> {

		@Override
		public void setByReceivedBody(HtspComplexObject<?> body) {
			DvrEntry entry = new DvrEntry() ;
			entry.setByReceivedBody(body);
			DvrEntries.this.dvrEntries.put( entry.getId(), entry);
		}

		@Override
		public DvrEntry create() {
			return null;
		}
		
	}
	
	public class DvrEntryUpdate extends MainObject< DvrEntry> {

		@Override
		public void setByReceivedBody(HtspComplexObject<?> body) {
			Long id = body.getReceived("id").getLong();
			DvrEntry entry = DvrEntries.this.dvrEntries.get(id);
			entry.setByReceivedBody(body);
		}

		@Override
		public DvrEntry create() {
			return null;
		}
		
	}
	
	public class DvrEntryDelete extends MainObject< DvrEntry> {

		@Override
		public void setByReceivedBody(HtspComplexObject<?> body) {
			Long id = body.getReceived("id").getLong();
			DvrEntries.this.dvrEntries.remove(id);
		}

		@Override
		public DvrEntry create() {
			return null;
		}
		
	}

}
