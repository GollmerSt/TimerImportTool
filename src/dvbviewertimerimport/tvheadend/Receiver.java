package dvbviewertimerimport.tvheadend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

import dvbviewertimerimport.tvheadend.HtsExceptions.AccessProhibitedException;
import dvbviewertimerimport.tvheadend.HtsExceptions.ReceiveException;
import dvbviewertimerimport.tvheadend.HtsExceptions.SequenceNumberException;
import dvbviewertimerimport.tvheadend.HtsMessage.HMap;
import dvbviewertimerimport.tvheadend.HtsMessage.HString;
import dvbviewertimerimport.tvheadend.HtsMessage.Signed64;

public class Receiver {
	private final Thread thread;
	private final Client client;
	private final Observable asyncListener = new Observable();
	private final Observable syncListener = new Observable();
	private HMap syncReceived = null;
	private Thread syncThread = null;

	public synchronized HMap getSyncReceived() {
		HMap received = this.syncReceived;
		this.syncReceived = null;
		return received;
	}

	public synchronized void setSyncReceived(HMap syncReceived) {
		this.syncReceived = syncReceived;
	}

	public Receiver(Client client) {
		this.client = client;
		thread = new Thread(new Runnable());
	}

	public void start() {
		this.thread.start();
	}

	private class Runnable implements java.lang.Runnable {

		@Override
		public void run() {
			while (true) {
				InputStream in;
				try {
					in = client.getSocket().getInputStream();
					if (in.available() > 0) {
						HMap received = client.receive();
						System.out.println(received);
						HString method = (HString) received.get("method");
						if (method == null) {
							setSyncReceived(received);
							synchronized (Receiver.this) {
								if (syncThread != null) {
									syncThread.interrupt();
								}
							}
						} else {
							Received r = new Received(method.getContents(), received);
							asyncListener.hasChanged();
							asyncListener.notifyObservers(r);
						}
					} else {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	public void addAsyncListener(AsyncListener listener) {
		this.asyncListener.addObserver(listener);
	}

	public void addSyncListener(SyncListener listener) {
		this.syncListener.addObserver(listener);
	}

	public abstract static class AsyncListener implements Observer {

		public abstract void received(Received received);

		@Override
		public void update(Observable arg0, Object arg1) {
			if (!(arg1 instanceof Received)) {
				throw new Error("Wrong parameter");
			}
			this.received((Received) arg1);
		}

	}

	public abstract static class SyncListener implements Observer {

		public abstract void received(HMap received);

		@Override
		public void update(Observable arg0, Object arg1) {
			if (!(arg1 instanceof HMap)) {
				throw new Error("Wrong parameter");
			}
			this.received((HMap) arg1);
		}

	}

	public static class Received {
		private final HMap map;
		private final String method;

		public Received(String method, HMap map) {
			this.method = method;
			this.map = map;
		}

		public HMap getMap() {
			return map;
		}

		public String getMethod() {
			return method;
		}
	}

	public HMap receive(int seqNo) throws SequenceNumberException, ReceiveException, AccessProhibitedException {
		HMap received;
		synchronized (this) {
			received = this.getSyncReceived();
			if (received == null) {
				this.syncThread = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(100000);
						} catch (InterruptedException e) {
						}
					}
				});
			}
			this.syncThread.start();
		}
		if (received == null) {
			try {
				this.syncThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			received = this.getSyncReceived();
			if (received == null) {
				// timeout
			}
		}
		this.syncThread = null ;
		Long receivedSeq = received.getSequenceNo();
		if (receivedSeq != null && receivedSeq != ((long) seqNo & 0xffffffffL)) {
			throw new HtsExceptions.SequenceNumberException(seqNo, receivedSeq);
		}

		HString errorText = (HString) received.get("error");
		if (errorText != null) {
			throw new HtsExceptions.ReceiveException(errorText.getContents());
		}

		Signed64 noaccess = (Signed64) received.get("noaccess");
		if (noaccess != null && noaccess.getContents() != 0) {
			throw new HtsExceptions.AccessProhibitedException();
		}
		return received;
	}
}
