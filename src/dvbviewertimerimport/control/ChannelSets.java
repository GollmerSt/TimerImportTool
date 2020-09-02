package dvbviewertimerimport.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dvbviewertimerimport.provider.Provider;

public class ChannelSets extends ArrayList<ChannelSet> {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -7439525926536395052L;
	private final List< Map< String, ChannelSet >> channelKeysMaps ;
	
	public ChannelSets() {
		super();
		
		this.channelKeysMaps = new ArrayList<>(Provider.getProviders().size());
		
		this.clearSets();
		
	}
	
	private final void clearSets() {
		for ( int i = 0; i < Provider.getProviders().size();++i) {
			this.channelKeysMaps.add(null);
		}
	}
	
	private Map< String, ChannelSet > getMap( int providerId)  {
		Map< String, ChannelSet > channelKeyMap = this.channelKeysMaps.get(providerId);
		if ( channelKeyMap == null ) {
			channelKeyMap = new HashMap<>();
			this.channelKeysMaps.set(providerId, channelKeyMap);
			for ( ChannelSet set : this) {
				Channel channel = set.getChannel(providerId);
				if (channel != null) {
					channelKeyMap.put(channel.getIDKey(),set);
//					Log.out(channel.getIDKey());
				}
			}
		}
		return channelKeyMap;
	}
	
	public boolean contains( int providerId, String channelId) {
		return this.getMap(providerId).containsKey(channelId);
		
	}
	
	public ChannelSet get( int providerId, String channelId) {
		ChannelSet set = this.getMap(providerId).get(channelId);
//		if ( set == null ) {
//			Log.out( "Provider: " + providerId + " ChannelId: " + channelId);
//		}
		return set;
	}
	

	
	@Override
	public boolean add( ChannelSet set ) {
		this.clearSets();
		return super.add(set);
	}
}
