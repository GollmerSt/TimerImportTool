// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

import java.util.ArrayList;
import java.util.Collection;

import dvbviewertimerimport.DVBViewerTimerImport;
import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;

public class TVBrowser extends Provider {

	public TVBrowser(Control control) {
		super(control, false, false, "TV-Browser", false, false, false, false, false, false);
		this.canImport = true;
		this.canModify = true;
		this.canAddChannel = false;
		this.isFunctional = false;

	}

	@Override
	protected Collection<Channel> readChannels() {
		return DVBViewerTimerImport.getTVBChannelNames();
	};

	@Override
	public boolean isChannelMapAvailable() {
		return isFunctional();
	}

	@Override
	public void updateRecordings(ArrayList<DVBViewerEntry> entries) {
		DVBViewerTimerImport.updateRecordings(entries);
	}
}
