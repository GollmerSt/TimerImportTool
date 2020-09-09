package dvbviewertimerimport.tvheadend;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import dvbviewertimerimport.misc.Helper;
import dvbviewertimerimport.tvheadend.client.HtsClient;
import dvbviewertimerimport.tvheadend.objects.Channels;
import dvbviewertimerimport.tvheadend.objects.DvrEntries;
import dvbviewertimerimport.tvheadend.objects.EnableAsyncMetadata;
import dvbviewertimerimport.tvheadend.objects.GetSysTime;
import dvbviewertimerimport.tvheadend.objects.InitialSyncCompleted;
import dvbviewertimerimport.tvheadend.objects.MainObject;
import dvbviewertimerimport.tvheadend.objects.Tags;

public class TvHeadend {
	
	private final String url = "192.168.0.51";
	private final int port = 9982;
	private final String userName = "StefanTvheadend";
	private final String password = "e$am1kro";

	private final Tags tags = new Tags();
	private final Channels channels = new Channels();
	private final DvrEntries dvrEntries = new DvrEntries();
	private Boolean initialSyncCompleted = false;
	
	private final Map< String, MainObject<?>> serverToClientObjects = new HashMap<>();
	
	public TvHeadend() {
		this.add(this.tags.new TagAdd());
		this.add(this.tags.new TagUpdate());
		this.add(this.tags.new TagDelete());
		this.add( this.channels.new ChannelAdd());
		this.add( this.channels.new ChannelDelete());
		this.add( this.channels.new ChannelUpdate());
		this.add( this.dvrEntries.new DvrEntryAdd());
		this.add( this.dvrEntries.new DvrEntryDelete());
		this.add( this.dvrEntries.new DvrEntryUpdate());
		this.add( new InitialSyncCompleted(this));
	}
	
	private final void add( MainObject<?> mainObject ) {
		this.serverToClientObjects.put(mainObject.getObjectName(), mainObject );
	}

	public Channels getChannels() {
		return this.channels;
	}

	public String getUrl() {
		return this.url;
	}

	public int getPort() {
		return this.port;
	}

	public String getUserName() {
		return this.userName;
	}

	public String getPassword() {
		return this.password;
	}

	public Map<String, MainObject<?>> getServerToClientObjects() {
		return this.serverToClientObjects;
	}

	public void setInitialSyncCompleted() {
		synchronized (this.initialSyncCompleted) {
			this.initialSyncCompleted.notifyAll();
			this.initialSyncCompleted = true;
		}
	}

	public Boolean getInitialSyncCompleted() {
		return this.initialSyncCompleted;
	}
	
	public void waitForInitialSyncCompleted() {
		synchronized (this.initialSyncCompleted) {
			if ( this.initialSyncCompleted ) {
				return;
			} else {
				try {
					this.initialSyncCompleted.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		return;
	}

	public static void main(String[] args) throws IOException, InterruptedException, CloneNotSupportedException {
		TvHeadend tvHeadend = new TvHeadend();
		HtsClient client = new HtsClient(tvHeadend);
		if (client.connect()) {
			System.out.println("Connection and authentication successfull.");
		} else {
			return;
		}
		GetSysTime time = new GetSysTime();
		client.execute(time);
		EnableAsyncMetadata enableUpdate = new EnableAsyncMetadata(false, null, null, null);
		client.execute(enableUpdate);
		tvHeadend.waitForInitialSyncCompleted();
		System.out.println("Initial synchronisation with TvHeadend completed.");
		client.close();
		Helper.ThreadPoolRunnable.abort();
	}

}
