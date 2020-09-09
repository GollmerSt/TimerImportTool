package dvbviewertimerimport.tvheadend.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dvbviewertimerimport.misc.Helper;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.tvheadend.Constants;
import dvbviewertimerimport.tvheadend.TvHeadend;
import dvbviewertimerimport.tvheadend.binobjects.HtspBody;
import dvbviewertimerimport.tvheadend.binobjects.HtspMsg;
import dvbviewertimerimport.tvheadend.objects.Authenticate;
import dvbviewertimerimport.tvheadend.objects.Command;
import dvbviewertimerimport.tvheadend.objects.Hello;
import dvbviewertimerimport.tvheadend.objects.HtspDigest;
import dvbviewertimerimport.tvheadend.objects.MainObject;
import dvbviewertimerimport.tvheadend.objects.RpcRequestFields;

public class HtsClient {

	private static Set<String> FATAL_ERROR_MESSAGES = new HashSet<>(
			Arrays.asList("Invalid arguments", "Method not found"));

	private Map<Integer, ReceiveObject> receiveObjects = new HashMap<>();

	private final TvHeadend tvHeadend;

	private HtspDigest digest = null;

	private Socket socket = null;
	private InputStream in = null;
	private OutputStream out = null;
	private int seqNo;

	private Helper.ThreadPoolRunnable inputThread;

	public HtsClient(TvHeadend tvHeadend) {
		this.tvHeadend = tvHeadend;
	}

	public boolean connect() throws IOException, InterruptedException, CloneNotSupportedException {
		if (this.socket != null) {
			if (this.socket.isConnected()) {
				return true;
			}
		}
		InetAddress address = InetAddress.getByName(this.tvHeadend.getUrl());
		this.socket = new Socket(address, this.tvHeadend.getPort());

		this.in = this.socket.getInputStream();
		this.out = this.socket.getOutputStream();

		this.inputThread = new InputThread();
		this.inputThread.submit();

		Hello hello = new Hello();
		this.execute(hello);
		this.digest = new HtspDigest(hello.getChallenge());

		Authenticate authenticate = new Authenticate();
		this.execute(authenticate);
		if (authenticate.isAccess()) {
			return true;
		} else {
			this.socket.close();
			return true;
		}
	}

	private class InputThread extends Helper.ThreadPoolRunnable {

		public InputThread() {
			super("InputThread");
		}

		@Override
		public void run() {
			try {
				while (true) {
					int count;
					synchronized (HtsClient.this) {
						if (HtsClient.this.socket == null || !HtsClient.this.socket.isConnected()) {
							return;
						}
						count = HtsClient.this.in.available();
					}
					if (count >= HtspBody.MIN_LENGTH) {
						HtspBody receivedBody = HtspBody.getInstance().create(HtsClient.this.in);
						HtspMsg errorMsg = receivedBody.getReceived("error");
						if (!errorMsg.isNull() && FATAL_ERROR_MESSAGES.contains(errorMsg.getString())) {
							Log.error("Transfer error occurs: " + errorMsg.getString());
						}
						HtspMsg seqMsg = receivedBody.getReceived("seq");
						if (seqMsg.isNull()) {
							processServerToClient(receivedBody);
						} else {
							processClientToServer(receivedBody);
						}

					} else {
						synchronized (this) {
							try {
								this.wait(10); // TODO
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} // TODO
						}
					}
				}
			} catch (IOException | ClassNotFoundException | TransferFormatException e) {
				Log.error("Error on TVHeadend connection: " + e.getMessage());
				close();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void processServerToClient(HtspBody receivedBody) throws CloneNotSupportedException {
		String objectName = receivedBody.getReceived("method").getString();
		MainObject<?> base = HtsClient.this.tvHeadend.getServerToClientObjects().get(objectName);
		if (base == null) {
			System.out.println("Server to client: Received object \"" + objectName + "\" NOT supported.");
		} else {
//			System.out.println("Server to client: Received object \"" + objectName + "\" supported.");
			MainObject<?> current = (MainObject<?>) base.create();
			if (current == null) {
				current = base;
			}
			current.setByReceivedBody(receivedBody);
		}

	}
	
	private void processClientToServer( HtspBody receivedBody ) throws TransferFormatException {
		HtspMsg seqMsg = receivedBody.getReceived("seq");
		Long seq = seqMsg.getLong();
		if (seq == null) {
			throw new TransferFormatException("Wrong type of seq");
		}
		ReceiveObject receiveObject;
		synchronized (HtsClient.this) {
			receiveObject = HtsClient.this.receiveObjects.remove(seq.intValue());
		}
		receiveObject.setReceived(receivedBody);
		synchronized (receiveObject) {
			receiveObject.notifyAll();
		}

	}

	public synchronized void close() {
		try {
			this.socket.close();
		} catch (IOException e) {
		}
		this.socket = null;
	}

	private class ReceiveObject {
		private final int seqNo;
		private HtspBody received;

		public ReceiveObject(int seqNo) {
			this.seqNo = seqNo;
		}

		public HtspBody getReceived() {
			return this.received;
		}

		public void setReceived(HtspBody body) {
			this.received = body;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Integer) {
				return this.seqNo == (Integer) obj;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Integer.hashCode(this.seqNo);
		}

	}

	public void send(HtspBody body) throws IOException, InterruptedException, CloneNotSupportedException {
		this.connect();
		body.write(this.out);
	}

	public HtspBody sendAndReceive(String method, HtspBody send) throws InterruptedException, IOException, CloneNotSupportedException {
		String userName = this.digest == null ? null : this.tvHeadend.getUserName();
		byte[] digest = this.digest == null ? null : this.digest.getHtspDigest(userName, this.tvHeadend.getPassword());
		int seqNo;
		ReceiveObject receiveObject;
		synchronized (this) {
			seqNo = ++this.seqNo;
			receiveObject = new ReceiveObject(seqNo);
			this.receiveObjects.put(seqNo, receiveObject);
		}
		RpcRequestFields requestFields = new RpcRequestFields(method, seqNo, userName, digest);

		HtspBody sendBody = HtspBody.create(requestFields.getBody(), send);

		synchronized (receiveObject) {
			this.send(sendBody);

			receiveObject.wait(Constants.RECEIVE_TIMEOUT); // TODO
			if (receiveObject.received == null) {
				throw new IOException("Timeout while wait for receive package of method \"" + method + "\".");
			}
		}

		return receiveObject.getReceived();
	}

	public void execute(Command<?> command) throws InterruptedException, IOException, CloneNotSupportedException {

		HtspBody body = this.sendAndReceive(command.getObjectName(), command.getSendBody());

		command.setByReceivedBody(body);
	}

}
