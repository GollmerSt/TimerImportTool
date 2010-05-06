package dvbviewertimerimport.tvbrowser;

import java.util.HashMap;

import dvbviewertimerimport.DVBViewerTimerImport;
import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.provider.Provider;

public class TVBrowser extends Provider {
  
  

  public TVBrowser(Control control ) {
    super(control, false, false, "TV-Browser", false, false, false, false, true, false);
  }
  @Override
  public int importChannels( boolean check )
  {
    if ( check )
      return 0 ;
    
    String [] channels = DVBViewerTimerImport.getTVBChannelNames() ;
    
    int count = 0 ;
    
    HashMap< String, ChannelSet > mapByName = new HashMap< String, ChannelSet >() ;
    
    int pid = this.getID() ;
    
    for ( ChannelSet cs : this.control.getChannelSets() )
    {
      Channel c = cs.getChannel( pid ) ;
      if ( c == null )
        continue ;
      mapByName.put( c.getName(), cs ) ;
    }
    
    for ( String channel : channels )
    {
      if ( ! mapByName.containsKey( channel ) )
      {
        ChannelSet cs = new ChannelSet() ;
        cs.add( pid, channel, -1 ) ;
        control.getChannelSets().add( cs ) ;
        mapByName.put( channel, cs ) ;
        count++ ;
      }
    }
    return count ;
  }
  private String getParaInfo()
  {
    return 
    ", necessary parameters:\n   -TVGenial TVUID=ccc Beginn=yyyyMMddHHmm Dauer=nnn Sendung=cccccc" ;
  }

}
