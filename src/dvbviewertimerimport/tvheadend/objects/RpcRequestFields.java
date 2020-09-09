package dvbviewertimerimport.tvheadend.objects;

import dvbviewertimerimport.tvheadend.binobjects.HtspBody;
import dvbviewertimerimport.tvheadend.binobjects.HtspObject;

public class RpcRequestFields {
	/**
	 * required Method. This field contains the requested method
	 */
	private final String method;
	/**
	 * optional Sequence number. This field will be echoed back by the server in the
	 * reply.
	 */
	private final Integer seq;
	/**
	 * optional Username, in combination with 'digest' this can be used to raise the
	 * privileges for the session in combination with invocation of a method.
	 */
	private final String username;
	/**
	 * digest optional Used to raise privileges.
	 */
	private final byte[] digest;

	public RpcRequestFields(String method, int seq, String username, byte[] digest) {
		this.method = method;
		this.seq = seq;
		this.username = username;
		this.digest = digest;
	}

	public HtspBody getBody() {
		return HtspBody.create( //
				new HtspObject("method", this.method), //
				new HtspObject("seq", this.seq), //
				new HtspObject("username", this.username), //
				new HtspObject("digest", this.digest));
	}

}
