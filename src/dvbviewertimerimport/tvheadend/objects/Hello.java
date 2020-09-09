package dvbviewertimerimport.tvheadend.objects;

import java.util.Collection;

import dvbviewertimerimport.main.Versions;
import dvbviewertimerimport.tvheadend.Constants;
import dvbviewertimerimport.tvheadend.binobjects.HtspBody;
import dvbviewertimerimport.tvheadend.binobjects.HtspComplexObject;
import dvbviewertimerimport.tvheadend.binobjects.HtspMsg;
import dvbviewertimerimport.tvheadend.binobjects.HtspObject;

public class Hello extends Command<Hello> {

	/**
	 * required The server supports all versions of the protocol up to and including
	 * this number.
	 */
	private long htspVersion = -1;

	/**
	 * required Server software name.
	 */
	private String serverName = null;

	/**
	 * required Server software version.
	 */
	private String serverversion = null;

	/**
	 * [] required Server capabilities (Added in version 6)
	 * 
	 * 
	 */
	// Note: possible values for serverCapability[]:
	//
	// cwc Descrambling available
	// v4l Analogue TV available
	// linuxdvb Linux DVB API available
	// imagecache Image caching available
	// timeshift Timeshifting available (Added in version 9).

	private Collection<String> serverCapability = null;

	/**
	 * required 32 bytes randomized data used to generate authentication digests
	 */
	private byte[] challenge = null;

	/**
	 * optional Server HTTP webroot (Added in version 8)
	 */
	private String webRoot;

	@Override
	public HtspBody getSendBody() {

		// Request message fields:
		//
		// htspVersion u32 required Client preferred HTSP version.
		// clientname str required Client software name.
		// clientversion str required Client software version.
		//

		return HtspBody.create( //
				new HtspObject("htspversion", new Long(Constants.HTSP_VERSION)), //
				new HtspObject("clientname", "TimerImportTool"), //
				new HtspObject("clientversion", Versions.getVersion()) //
		);
	}

	@Override
	public void setByReceivedBody(HtspComplexObject<?> body) throws CloneNotSupportedException {
		this.htspVersion = body.getReceived("htspversion").getLong();
		this.serverName = body.getReceived("servername").getString();
		this.serverCapability = body.getReceived("servercapability").getCollection(new String());
		this.challenge = body.getReceived("challenge").getBinary();
		HtspMsg webRootMsg = body.getReceived("webroot");
		if (webRootMsg == null) {
			this.webRoot = null;
		} else {
			this.webRoot = webRootMsg.getString();
		}
	}

	public long getHtspVersion() {
		return this.htspVersion;
	}

	public String getServerName() {
		return this.serverName;
	}

	public String getServerversion() {
		return this.serverversion;
	}

	public Collection<String> getServerCapability() {
		return this.serverCapability;
	}

	public byte[] getChallenge() {
		return this.challenge;
	}

	public String getWebRoot() {
		return this.webRoot;
	}

	@Override
	public Hello create() {
		return new Hello();
	}
	
//	@Override
//	public String getMethod() {
//		return "sfjksafjksahjkfhasjdkfhjsavgjsakgsbdanjkl";
//	}

}
