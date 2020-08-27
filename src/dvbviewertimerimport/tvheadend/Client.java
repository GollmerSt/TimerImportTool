package dvbviewertimerimport.tvheadend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;

import dvbviewertimerimport.main.Versions;
import dvbviewertimerimport.tvheadend.HtsExceptions.AccessProhibitedException;
import dvbviewertimerimport.tvheadend.HtsExceptions.ReceiveException;
import dvbviewertimerimport.tvheadend.HtsExceptions.SequenceNumberException;
import dvbviewertimerimport.tvheadend.HtsMessage.Bin;
import dvbviewertimerimport.tvheadend.HtsMessage.HMap;
import dvbviewertimerimport.tvheadend.HtsMessage.HString;
import dvbviewertimerimport.tvheadend.HtsMessage.Signed64;;

public class Client {

	private static final Signed64 HTSP_PROTO_VERSION = new Signed64("htspversion", 25);
	private static final HString CLIENT_NAME = new HString("clientname", "TimerImportTool");
	private static final HString CLIENT_VERSION = new HString("clientversion", Versions.getVersion());
	private static final String IP_ADDRESS = "192.168.0.51";
	private static final String USERNAME = "StefanTvheadend";
	private static final String PASSWORD = "e$am1kro";
	private static int PORT_NUMBER = 9982;

	private int sequenceNo = 0;
	private HString user = null;
	private HString password = null;
	private Bin challenge = null;
	private Bin digest = null;
	private final Socket socket;
	private final Receiver receiver;

	private final InputStream inputStream;

	@SuppressWarnings("unused")
	public Client(String ipAddress, int port) {
		Socket socketTemp = null;
		InputStream inTemp = null;
		try {
			socketTemp = new Socket(ipAddress, port);
			inTemp = socketTemp.getInputStream();
			OutputStream outTemp = socketTemp.getOutputStream();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.socket = socketTemp;
			this.inputStream = inTemp;
		}
		this.setPassword(new HString(PASSWORD));
		this.user = new HString("username", USERNAME);
		this.receiver = new Receiver(this);
	}

	public void startReceiving() {
		this.receiver.start();
	}

	public int send(String command, Value<?>... values) throws IOException {
		HtsMessage.HList message = new HtsMessage.HList();

		int result = this.sequenceNo;
		message.add(new Signed64("seq", (long) this.sequenceNo & 0xffffffffL));
		message.add(new HString("method", command));
		if (this.user != null) {
			message.add(this.user);
		}
		if (this.digest != null) {
			message.add(this.digest);
		}
		for (Value<?> value : values) {
			message.add(value);
		}

		message.write(this.socket.getOutputStream());

		++this.sequenceNo;

		return result;
	}

	public HMap receive() throws ClassNotFoundException, IOException {

		HMap map = new HMap();

		map.read(this.inputStream);

		return map;
	}

	public void updateDigest() {
		if (this.challenge == null || this.password == null) {
			this.digest = null;
			return;
		}
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		Collection<Byte> col = new ArrayList<Byte>(this.password.getBytes());
		col.addAll(this.challenge.getBytes());
		Binary binary = new Binary(col);
		md.update(binary.bytes);

		Binary dig = new Binary();
		dig.setBytes(md.digest());
		this.digest = new Bin("digest", dig);
	}

	public HString getPassword() {
		return this.password;
	}

	public void setPassword(HString password) {
		this.password = password;
		this.updateDigest();
	}

	public Bin getChallenge() {
		return this.challenge;
	}

	public void setChallenge(Bin challenge) {
		this.challenge = challenge;
		this.updateDigest();
	}

	public HMap hello() throws IOException, ClassNotFoundException, ReceiveException, SequenceNumberException,
			AccessProhibitedException {
		int sequenceNo = this.send("hello", HTSP_PROTO_VERSION, CLIENT_NAME, CLIENT_VERSION);
		HMap map = this.receiver.receive(sequenceNo);
		this.setChallenge((Bin) map.get("challenge"));
		return map;
	}

	@SuppressWarnings("unused")
	public void enableAsyncMetadata() throws IOException, ClassNotFoundException, ReceiveException,
			SequenceNumberException, AccessProhibitedException {
		Signed64 epgEnable = new Signed64("epg", 0);
		int sequenceNo = this.send("enableAsyncMetadata");
		HMap received = this.receiver.receive(sequenceNo);
		System.out.println(received);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws ClassNotFoundException, IOException, ReceiveException,
			SequenceNumberException, AccessProhibitedException {
		Client client = new Client(IP_ADDRESS, PORT_NUMBER);
		client.startReceiving();
		;
		client.hello();
		client.enableAsyncMetadata();

		HMap map;
		while (true) {
			try {
				map = client.hello();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
			}
		}
	}

	public Socket getSocket() {
		return this.socket;
	}
}
