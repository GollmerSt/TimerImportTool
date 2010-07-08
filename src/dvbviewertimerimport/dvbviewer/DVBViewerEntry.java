// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer ;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.tree.TreePath;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import dvbviewertimerimport.misc.Enums.ActionAfterItems;
import dvbviewertimerimport.misc.Enums.TimerActionItems;
import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;
import dvbviewertimerimport.misc.* ;
import dvbviewertimerimport.provider.OutDatedInfo;
import dvbviewertimerimport.provider.Provider;
import dvbviewertimerimport.xml.StackXML;


public final class DVBViewerEntry  implements Cloneable{
	private enum CompStatus { DIFFER, EQUAL, IN_RANGE } ;
	public enum MergeStatus
	{
		//               UNKNOWN DISABLED ENABLED MERGE  JUST_SEPARATED SEPARATED_FIXED
		UNKNOWN        ( false,  false,   false,  false, false,         false ),
		DISABLED       ( false,  false,   false,  false, false,         false ),
		ENABLED        ( false,  false,   true,   true,  false,         false ),
		MERGE          ( false,  false,   true,   true,  true,          false ), 
		JUST_SEPARATED ( false,  false,   false,  true,  true,          false ), 
		SEPARATED_FIXED( false,  false,   false,  false, false,         false ) ;
		private ArrayList< Boolean > list = new ArrayList< Boolean >() ;
		private MergeStatus( boolean ... bb )
		{
			for ( boolean b : bb )
				list.add( b ) ;
		}
		public boolean canMerge( MergeStatus s )
		{
			return this.list.get( s.ordinal() ) ;
		}
		public static MergeStatus post( MergeStatus s )
		{
			switch ( s )
			{
				case MERGE :
					return ENABLED ;
				case JUST_SEPARATED :
					return SEPARATED_FIXED ;
				default :
					return s ;
			}
		}
		public MergeStatus post() { return post( this ) ; } ;
	} ;
	public enum StatusService { ENABLED, DISABLED, REMOVED } ;				    				// applied to Service 
	public enum ToDo          { NONE, NEW, UPDATE, DELETE, DELETE_BY_PROVIDER } ;	//toDo by Service

	private static final StackXML< String > entryXML  = new StackXML< String >( "Entry" ) ;
	private static final StackXML< String > titleXML  = new StackXML< String >( "Entry", "Title") ;
	private static final StackXML< String > mergedXML = new StackXML< String >( "Entry", "MergedEntries", "Id" ) ;

	
	private long id ;
	private String providerID ;
	private boolean isFilterElement ;
	private StatusService statusService ;
	private long serviceID ;
	private final String channel ;
	private String program = null ;
	private String channelID = null ;
	private long start ;
	private long end ;
	private long startOrg ;
	private long endOrg ;
	private String days ;
	private String title ;
	private TimerActionItems timerAction ;
	private ActionAfterItems actionAfter ;
	private MergeStatus mergeStatus ;
	private Provider provider ;
	private OutDatedInfo outDatedInfo = null ;
	private ArrayList< Long > mergedIDs = new ArrayList< Long >() ;
	private ArrayList< DVBViewerEntry > mergedEntries = null ;
	private boolean mergingChanged = false ;
	private long mergeID = -1 ;
	private DVBViewerEntry mergeElement = null ;
	private DVBViewerEntry root = null ;
	private ToDo toDo ;
	private boolean isCollapsed = false ;


	public DVBViewerEntry( 	long id ,
							boolean isFilterElement ,
							StatusService statusService ,
							long serviceID , 
							String providerID,
							String channel ,
							long start ,
							long end ,
							long startOrg ,
							long endOrg ,
							String days ,
							String title ,
							TimerActionItems timerAction, 
							ActionAfterItems actionAfter, 
							MergeStatus mergeStatus, 
							long mergeID,
							Provider provider ,
							OutDatedInfo outDatedInfo ,
				            boolean isCollapsed,
							ToDo toDo )
	{
		this.id = id ;
		this.isFilterElement = isFilterElement ;
		this.statusService = statusService ;
		this.serviceID = serviceID ; 
		this.providerID = providerID ;
		this.channel = channel ;
		this.start = start ;
		this.end = end ;
		this.startOrg = startOrg ;
		this.endOrg = endOrg ;
		this.days = days ;
		this.title =  title ;
		this.timerAction = timerAction ;
		this.actionAfter = actionAfter ;
		this.mergeStatus = mergeStatus ;
		this.mergeID = mergeID ;
		this.provider = provider ;
		this.outDatedInfo = outDatedInfo.clone() ;
		this.isCollapsed = isCollapsed ;
		this.toDo = toDo ;
	}
	public DVBViewerEntry()    // only for root node
	{
		this.id = -1 ;
		this.isFilterElement = false ;
		this.statusService = StatusService.DISABLED ;
		this.serviceID = -1 ; 
		this.providerID = null ;
		this.channel = null ;
		this.start = -1 ;
		this.end = -1 ;
		this.startOrg = -1 ;
		this.endOrg = -1 ;
		this.days = null ;
		this.title =  null ;
		this.timerAction = TimerActionItems.DEFAULT ;
		this.actionAfter = ActionAfterItems.DEFAULT ;
		this.mergeStatus = MergeStatus.DISABLED ;
		this.mergeID = -1 ;
		this.provider = null ;
		this.outDatedInfo = null ;
		this.isCollapsed = false ;
		this.toDo = ToDo.NONE ;
	}

	public DVBViewerEntry( boolean enable, long serviceID, String channel, long start, long end, String days,
                           String title, TimerActionItems timerAction, ActionAfterItems actionAfter )
	{
		this( -1, false, StatusService.ENABLED , serviceID, null, channel, start ,end ,
				         start, end, days, title, timerAction, actionAfter, MergeStatus.UNKNOWN,
				         -1, null, new OutDatedInfo(), false, ToDo.NONE ) ;

		if ( this.days.equals( "-------" ) )
			this.mergeStatus = MergeStatus.DISABLED ;

		this.statusService = enable? StatusService.ENABLED : StatusService.DISABLED ;
		
	}
	public DVBViewerEntry( String channel,
                           String providerID,
			               long start,
			               long end,
			               long startOrg,
			               long endOrg,
			               String days,
			               String title,
			               TimerActionItems timerAction,
			               ActionAfterItems actionAfter,
			               boolean merge,
			               Provider provider  )
	{
		this( -1, false, StatusService.ENABLED , -1, providerID, channel, start ,end ,
				  startOrg, endOrg, days, title, timerAction, actionAfter, MergeStatus.UNKNOWN,
				  -1, provider, new OutDatedInfo(), false, ToDo.NEW ) ;
		
		boolean isFilterElement = false ;
		if ( provider != null )
		{
			isFilterElement = provider.isFiltered() ;
		}
		this.isFilterElement = isFilterElement ;
		
		this.mergeStatus = ( merge && this.days.equals( "-------" ) )? MergeStatus.ENABLED : MergeStatus.DISABLED ;
	}
	@Override
	public DVBViewerEntry clone()
	{
		DVBViewerEntry entry = new DVBViewerEntry( 	-1, this.isFilterElement, this.statusService,
													this.serviceID, this.providerID, this.channel ,
													this.start, this.end, this.startOrg,
													this.endOrg, this.days, this.title, 
													this.timerAction, this.actionAfter,
													this.mergeStatus, this.mergeID,
													this.provider, this.outDatedInfo.clone(),
													this.isCollapsed,this.toDo ) ;
		
		if ( this.mergedEntries != null )
		{
			entry.mergedEntries = new ArrayList< DVBViewerEntry >() ;
			entry.mergedEntries.addAll( this.mergedEntries ) ;
		}
		
		entry.mergedEntries = this.mergedEntries ;
		entry.program = this.program ;
		
		return entry ;
	}
	private void splitChannel()
	{
		String [] parts = this.channel.split("\\|") ;
		if ( parts.length == 2 )
		{
			this.channelID = parts[0] ;
			this.program = parts[1] ;
		}
		else
		{
			this.channelID = "" ;
			this.program = "" ;
		}
	}
	public String getProgram()
	{
		if ( this.program == null )
			this.splitChannel() ;
		return this.program ;
	}
	public String getChannelID()
	{
		if ( this.channelID == null )
			this.splitChannel() ;
		return this.channelID ;
	}
	private void setMergeStatus( MergeStatus m )
	{
		if ( this.days.equals( "-------" ) )
			this.mergeStatus = MergeStatus.DISABLED ;
		else
			this.mergeStatus = m ;
	}
	private void createTitle( String separator )
	{
		if ( ! this.isMergeElement() )
			return ;
		
		boolean first = true ;
		
		String title = "" ;
		
		for ( DVBViewerEntry e : this.mergedEntries )
		{
			if ( first )
				first = false ;
			else
				title += separator ;
			title += e.title ;
		}
		this.title = title;
	}
	public void calcStartEnd()
	{
		if ( ! this.isMergeElement() )
			return ;
		
		long start = this.mergedEntries.get(0).getStart() ;
		long end   = -1 ;
		long startOrg = this.mergedEntries.get(0).getStartOrg() ;
		long endOrg   = -1 ;
				
		for ( DVBViewerEntry e : this.mergedEntries )
		{
			if ( start > e.getStart() )
				start = e.getStart() ;
			if ( end < e.getEnd() )
				end = e.getEnd() ;
			if ( startOrg > e.getStartOrg() )
				startOrg = e.getStartOrg() ;
			if ( endOrg < e.getEndOrg() )
				endOrg = e.getEndOrg() ;
		}
		this.start = start;
		this.end   = end;
		this.startOrg = startOrg ;
		this.endOrg   = endOrg ;
	}
	private CompStatus compareWithService( final DVBViewerEntry service )
	{
		if ( ! this.getChannelID().equals( service.getChannelID() ) )
			return CompStatus.DIFFER ;
		
		if ( ! this.days.equals( service.days ) )
			return CompStatus.DIFFER ;
		
		if ( this.start == service.start && this.end == service.end )
			return CompStatus.EQUAL ;
		
		if (    this.startOrg >= service.start && this.endOrg <= service.end )
			return CompStatus.IN_RANGE ;
			
		return CompStatus.DIFFER ;
	}
	public boolean isOrgEqual( final DVBViewerEntry e )
	{
		if ( ! this.channel.equals( e.channel ) )
			return false ;
		
		if ( this.provider != e.provider )
			return false ;
		
		if ( ! this.title.equals( e.title ) )
			return false ;
		
		if ( this.startOrg == e.startOrg && this.endOrg == e.endOrg )
			return true ;
		return false ;
	}
	public static void updateXMLDataByServiceDataFuzzy(
	          final ArrayList<DVBViewerEntry> xml, 
	          final ArrayList<DVBViewerEntry> service,
	          boolean allElements)
	{
// Find and assign all easy to assign service entries, remove the entries from
// the merge elements if entry is enabled

		for ( DVBViewerEntry x : xml )
		{
			if ( x.serviceID >= 0 || ( x.mergedEntries != null && ! allElements ) )
				continue ;
			
			ArrayList<DVBViewerEntry> list = new ArrayList< DVBViewerEntry >() ;
			
			for ( DVBViewerEntry s : service )
			{
				CompStatus co = x.compareWithService( s ) ;
				if ( co == CompStatus.EQUAL || co == CompStatus.IN_RANGE )
					list.add( s ) ;
			}
			
			if ( list.size() == 0 )
				continue ;
			
			if ( list.size() > 1 )
			{
				ArrayList<DVBViewerEntry> choices = Conversions.getTheBestChoices(
						x.getTitle(),
						list,
						2, 3,
						new Function(),
						new Function()
						{
							@Override
							public int arrayIntToInt( final ArrayList< Integer > list, final int integer, final String search, final String array )
							{
								return this.arrayIntToInt3( list, integer, search, array ) ;
							}
						});
				list = choices ;
			}
			
			if ( list.size() > 1 )
			{
				ArrayList<DVBViewerEntry> choices = new ArrayList<DVBViewerEntry>() ;
				long minDiff = 999999999999999L ;
				
				for ( DVBViewerEntry s : list ) 
				{
					long diff = s.start - x.start ;
					diff = diff < 0 ? 0 : diff ;
					long diff1 = x.end - s.end ;
					diff1 = diff1 < 0 ? 0 : diff1 ;
					diff += diff1 ;
					if ( diff < minDiff )
					{
						choices.clear() ;
						choices.add( s ) ;
						minDiff = diff ;
					}
				}
				list = choices ;
			}
			if ( list.size() > 1 )
			{
				ArrayList<DVBViewerEntry> choices = new ArrayList<DVBViewerEntry>() ;
				for ( DVBViewerEntry s : list ) 
				{
					if ( s.isEnabled() == x.isEnabled() )
					{
						choices.add( s ) ;
					}
				}
				if ( choices.size() > 0 )
					list = choices ;
			}
			DVBViewerEntry s = list.get( 0 ) ;  // get first choice
						
			x.serviceID = s.serviceID ;
			
			x.start = s.start ;
			x.end   = s.end ;
			
			x.timerAction = s.timerAction ;
			x.actionAfter = s.actionAfter ;
			
			if ( s.isEnabled() )
			{
				if ( x.mergeElement != null )
				{
					//x.canMerge = false ;
					x.mergeElement.mergedEntries.remove( x ) ;
					x.mergeElement.mergingChanged = true ;
					x.mergeElement = null ;
				}
				if ( x.statusService == StatusService.DISABLED )
				{
					x.mergeStatus = MergeStatus.JUST_SEPARATED ;
					x.toDo        = ToDo.UPDATE ;
				}
				x.statusService = StatusService.ENABLED ;
			}
			else
			{
				x.statusService = StatusService.DISABLED ;
			}
			service.remove( s ) ;
		}
	}
	public static void reworkMergeElements( final ArrayList<DVBViewerEntry> xml,
											final String separator, DVBViewer.MaxID maxID )
	{
		ArrayList< DVBViewerEntry > newMergeEntries = new ArrayList< DVBViewerEntry >() ;
		
		for ( Iterator<DVBViewerEntry> itX = xml.iterator() ; itX.hasNext() ; )
		{
			DVBViewerEntry x = itX.next() ;

			if ( ! x.mergingChanged )
				continue ;
						
			long nStart = -1 ;
			long nEnd = -1 ;
			long nStartOrg = -1 ;
			long nEndOrg = -1 ;

			ArrayList< DVBViewerEntry > nMergedEntries = null ;
			
			while ( true )
			{
				boolean isIn = true ;
				boolean isChanged = false ;
				
				for ( Iterator<DVBViewerEntry> itM = x.mergedEntries.iterator() ; itM.hasNext() ; )
				{
					DVBViewerEntry m = itM.next() ;
					
					if ( nStart < 0 )
					{
						nStart = m.start ;
						nEnd = m.end ;
						nStartOrg = m.startOrg ;
						nEndOrg = m.endOrg ;
						nMergedEntries = new ArrayList< DVBViewerEntry >() ;
						nMergedEntries.add( m ) ;
						itM.remove() ;
						isChanged = true ;
						continue ;
					}
					if ( nStart <= m.start && nEnd >= m.start )
					{
						if ( m.end > nEnd )
						{
							nEnd = m.end ;
							isChanged = true ;
						}
					}
					else if ( nStart <= m.end && nEnd >= m.end )
					{
						if ( m.start < nEnd )
						{
							nStart = m.start ;
							isChanged = true ;
						}
					}
					else
						isIn = false ;
					
					if ( isIn )
					{
						nStartOrg = Math.min( m.startOrg, nStartOrg ) ;
						nEndOrg = Math.max( m.endOrg, nEndOrg ) ;
						nMergedEntries.add( m ) ;
						itM.remove() ;
					}
				}
				if ( isChanged )
					continue ;
				if ( ! isIn && nMergedEntries.size() > 1 )
				{
					DVBViewerEntry n = x.clone() ;
					n.id = x.id ;
					n.toDo = ToDo.UPDATE ;
					n.start = nStart ;
					n.end   = nEnd ;
					n.startOrg = nStartOrg ;
					n.endOrg = nEndOrg ;
					n.mergedEntries = nMergedEntries ;
					for ( DVBViewerEntry nE : n.mergedEntries )
						nE.mergeElement = n ;
					n.createTitle( separator ) ;
					if ( n.mergeElement != null )
						n.mergeElement.mergedEntries.add( n ) ;
					if ( n.serviceID >= 0 )
						n.toDo = ToDo.UPDATE ;
					newMergeEntries.add( n ) ;
					n.mergingChanged = false ;
					x.id = maxID.increment() ;
					x.serviceID = -1 ;
					x.toDo = ToDo.NEW ;
					nStart = -1 ;
				}
				else if ( ! isIn )
				{
					if ( nMergedEntries.size() == 1 )
					{
						DVBViewerEntry modify = nMergedEntries.get( 0 ) ;
						modify.statusService = StatusService.ENABLED ;
						modify.mergeElement = null ;
						modify.toDo = ToDo.UPDATE ; // evtl. anders wenn modify = merge elemenet, aber kommt das vor?
					}
					nStart = -1 ;
				}
				else 
				{
					if ( nStart < 0 || nMergedEntries.size() <= 1)
					{
						if ( nStart > 0 )
						{
							DVBViewerEntry modify = nMergedEntries.get( 0 ) ;
							modify.statusService = StatusService.ENABLED ;
							modify.mergeElement = null ;
							modify.toDo = ToDo.UPDATE ; // evtl. anders wenn modify = merge elemenet, aber kommt das vor?
						}
						if ( x.serviceID >= 0)
							x.toDo = ToDo.DELETE ;
						else
							itX.remove() ;
						break ;
					}
					x.mergedEntries = nMergedEntries ;
					String titleOld = x.title ;
					x.createTitle( separator ) ;
					if (    x.toDo == ToDo.NONE  && x.serviceID >= 0
						 && ( x.start != nStart || x.end != nEnd || ! titleOld.equals( x.title ) ) )
						x.toDo = ToDo.UPDATE ;
					x.start = nStart ;
					x.end   = nEnd ;
					x.startOrg = nStartOrg ;
					x.endOrg = nEndOrg ;
					nStart = -1 ;
					x.mergingChanged = false ;
					break ;
				}
			}
		}
		xml.addAll( newMergeEntries ) ;
	}
	public static void setToRemovedOrDeleteUnassignedXMLEntries( final ArrayList<DVBViewerEntry> xml )
	{
		for ( Iterator<DVBViewerEntry> itX = xml.iterator() ; itX.hasNext() ; )
		{
			DVBViewerEntry x = itX.next() ;

			if ( x.serviceID >= 0 )
				continue ;
			
			if ( x.isFilterElement || x.mergeElement != null )
			{
				if ( x.statusService == StatusService.REMOVED )
					continue ;
				Log.out( true, "The xml title \"" + x.title + "\" is set to REMOVED" ) ;
				x.statusService = StatusService.REMOVED ;
			}
			else
			{
				Log.out( true, "The xml title \"" + x.title + "\" is deleted" ) ;
				x.prepareRemove() ;
				itX.remove() ;
			}
		}
	}
	public static void updateXMLDataByServiceData(
			          final ArrayList<DVBViewerEntry> xml, 
			          final ArrayList<DVBViewerEntry> service,
			          final String separator,
			          final DVBViewer.MaxID maxID )
	{
		// Fist pass:  Find and assign all easy to assign service entries,
		//             remove the entries from the merge elements
		//             if entry is enabled
		
		updateXMLDataByServiceDataFuzzy( xml, service, true ) ;
		
		
		// Second pass:  Rework merge elements
		
		reworkMergeElements( xml, separator, maxID ) ;
			
		// Third pass:  Try to assign the modifies merge entries again,
		//              which aren't assigned
		
		updateXMLDataByServiceDataFuzzy( xml, service, true ) ;

		// Fourth pass: Set the rest of unassigned XML entries to REMOVED

		setToRemovedOrDeleteUnassignedXMLEntries( xml ) ;

		for ( DVBViewerEntry e : service )
		{
			e.setMergeStatus( MergeStatus.MERGE ) ;
			e.id = maxID.increment() ;
			xml.add( e ) ;
		}
	}
	private static void removeOutdatedProviderEntries( final ArrayList<DVBViewerEntry> xml )
	{		
		for ( DVBViewerEntry e : xml )
		{
			if ( e.isOutdatedByProvider() )
				e.setToDelete() ;
		}
	}
	
	public static void beforeRecordingSettingProcces( final ArrayList<DVBViewerEntry> xml,
													  final String separator, DVBViewer.MaxID maxID )
	{
		DVBViewerEntry.removeOutdatedProviderEntries( xml ) ;
		DVBViewerEntry.reworkMergeElements( xml, separator, maxID ) ;
	}
	public static void afterRecordingSettingProcces( final ArrayList<DVBViewerEntry> xml )
	{
		for ( Iterator< DVBViewerEntry > it = xml.iterator() ; it.hasNext() ; )
		{
			DVBViewerEntry e = it.next() ;
			
			if ( e.mustDeleted() )
			{
				it.remove() ;
				continue ;
			}
			e.mergeStatus = e.mergeStatus.post() ;
//			e.serviceID = -1 ;
			e.toDo = ToDo.NONE ;
		}
	}
	
	private boolean addMergedEntry( DVBViewerEntry entry )
	{
		if ( this.mergedEntries == null )
			this.mergedEntries = new ArrayList< DVBViewerEntry >() ;
		boolean isMergeElement = entry.isMergeElement() ;
		boolean isIncluded = false ;
		if ( ! isMergeElement )
		{
			entry.mergedEntries = new ArrayList< DVBViewerEntry >() ;
			entry.mergedEntries.add( entry ) ;
		}
		for ( DVBViewerEntry dE : entry.mergedEntries )
		{
			dE.mergeElement = this ;
			for ( int ix = 0 ; ix < this.mergedEntries.size() ; ix++ )
			{
				if ( this.mergedEntries.get( ix ).start > dE.start )
				{
					this.mergedEntries.add( ix, dE ) ;
					isIncluded = true ;
					break ;
				} 
			}
			if ( isIncluded == false )
			this.mergedEntries.add( dE ) ;
		}
		entry.mergedEntries = null ;
		return isMergeElement ;
	}
	public DVBViewerEntry update( DVBViewerEntry dE, String separator )
	{
		DVBViewerEntry result = null ;
		
		if ( this.mergedEntries == null )
		{
			result = this.clone() ;
			result.statusService = StatusService.DISABLED ;
			if ( result.toDo != ToDo.NEW )
				result.toDo = ToDo.UPDATE ;
			result.mergeElement = this ;
			result.mergeStatus = MergeStatus.ENABLED ;
			this.addMergedEntry( result ) ;
			this.isFilterElement = false ;
			this.providerID = null ;
			this.toDo = ToDo.NEW ;
		}
		if ( this.addMergedEntry( dE ) )
			dE.toDo = ToDo.DELETE ;
		else
		{
			dE.disable() ;
			dE.mergeElement = this ;
			dE.mergeStatus = MergeStatus.ENABLED ;
		}
		  
		this.mergeStatus = MergeStatus.ENABLED ;
		this.start    = Math.min(this.start,    dE.start ) ;
		this.startOrg = Math.min(this.startOrg, dE.startOrg ) ;
		this.end      = Math.max( this.end,  dE.getEnd() ) ;
		this.endOrg   = Math.max( this.endOrg,  dE.endOrg ) ;

		this.createTitle( separator ) ;
		if ( this.toDo == ToDo.NONE )
			this.toDo = ToDo.UPDATE ;
		else if ( this.toDo == ToDo.DELETE )
			throw new ErrorClass( "Unexpected error in DVBViewerEntry.update" ) ;
		
		return result ;
	}
	public void disable()
	{
		if ( this.toDo == ToDo.DELETE )
			throw new ErrorClass( "Unexpected error in DVBViewerEntry.disable" ) ;
		
		this.statusService = StatusService.DISABLED ;

		if ( this.toDo == ToDo.NONE )
			this.toDo = ToDo.UPDATE ;
	}
	public long getID() { return this.id ; } ;
	public void setID( long id ) { this.id = id ; } ;
	public long getServiceID() { return this.serviceID ; } ;
	public void setServiceID( long id ) { this.serviceID = id ; } ;
	public String getProviderCID() { return this.providerID ; } ;
	public void clearServiceID() { this.serviceID = -1 ; } ;
	public String getChannel() { return this.channel ; } ;
	public String getTitle() {return this.title ; } ;
	public ActionAfterItems getActionAfter() { return actionAfter ; } ;
	public TimerActionItems getTimerAction() { return timerAction ; } ;
	public String toString() {return "" ; } ; //this.title ; } ;
	public DVBViewerEntry getMergeEntry() { return this.mergeElement ; } ;
	public long getStart() { return this.start ; } ;
	public long getEnd()   { return this.end ; } ;
	public void setStart( long start ) { this.start = start ; } ;
	public void setEnd  ( long end )   { this.end   = end ; } ;
	public long getStartOrg() { return this.startOrg ; } ;
	public long getEndOrg() { return this.endOrg ; } ;
	public String getDays() { return this.days ; } ;
	public ArrayList< DVBViewerEntry > getMergedEntries() { return this.mergedEntries ; } ;
	public void setMergedEntries( ArrayList< DVBViewerEntry > entries) { this.mergedEntries = entries; } ;
	public 	boolean isInRange( long start, long end )
	{
		if ( this.start <= start && this.end >= start )
			return true ;
		if ( this.start <= end && this.end >= end )
			return true ;
		if ( start <= this.start && end >= this.end )
			return true ;
		if ( start <= this.end && end >= this.end )
			return true ;
		return false ;
	}
	public 	boolean isInRange( DVBViewerEntry e )
	{
		return this.isInRange( e.start, e.end ) ;
	}
	public boolean isOutdated( long now)
	{
		return this.end + Constants.DAYMILLSEC < now ;
	}
	public boolean toMerge()
	{
		return    this.mergeStatus == MergeStatus.ENABLED
		       || this.mergeStatus == MergeStatus.JUST_SEPARATED ; } ;
	public boolean mustMerge( DVBViewerEntry dE )
	{
		if ( ! this.channel.equals( dE.channel ) )
			return false ;
		if ( this.toDo == ToDo.NONE && dE.toDo == ToDo.NONE )
			return false ;			//TODO
		if ( this.isDisabled() || dE.isDisabled() )
			return false ;
		if ( ! this.mergeStatus.canMerge( dE.mergeStatus ) )
			return false ;
		if ( this.toDo == ToDo.DELETE )
			return false ;
		if ( dE.toDo == ToDo.DELETE )
			return false ;
		return this.isInRange( dE ) ;
	}
	public void setToDo( ToDo t ) { this.toDo = t ; } ;
	public ToDo getToDo() { return this.toDo ; } ;
	public Provider getProvider() { return this.provider ; } ;
	public void setMissing()
	{
		this.outDatedInfo.setMissing() ;
	}
	public void resetMissing()
	{
		this.outDatedInfo.resetMissing() ;
	}
	public boolean isDisabled() { return this.statusService != StatusService.ENABLED ; } ;
	public boolean isEnabled()
	{
		return    this.statusService == StatusService.ENABLED ;
	}
	public boolean isDeleted() { return this.toDo == ToDo.DELETE || this.toDo == ToDo.DELETE_BY_PROVIDER ; } ;
	public boolean isMerged() { return this.mergeElement != null ; } ;
	public boolean isMergeElement()
	{
		return this.mergedEntries != null && this.mergedEntries.size() != 0 ;
	} ;
	public boolean isFilterElement()
	{ 
		return this.isFilterElement && this.toDo != ToDo.DELETE ;
	}
	public boolean isProgramEntry()
	{
		if ( this.isMergeElement() )
			return false ;
		if ( this.mergeElement != null )
			return true ;
		if ( this.isEnabled() && ( this.toDo == ToDo.NONE || this.toDo == ToDo.NEW ) )
			return true ;
		return false ;
	}
	public static void assignMergedElements( HashMap< Long, DVBViewerEntry> map )
	{
		for ( DVBViewerEntry entry : map.values() )
		{
			if ( entry.mergedIDs != null && entry.mergedIDs.size() > 0 )
			{
				for ( long id : entry.mergedIDs )
				{
					if ( ! map.containsKey( id ))
						throw new ErrorClass( "Unexpected error on search for IDs" ) ;
					if ( entry.mergedEntries == null )
						entry.mergedEntries = new ArrayList< DVBViewerEntry >() ;
					entry.mergedEntries.add( map.get( id ) ) ;
				}
				entry.mergedIDs = null ;
			}
			if ( entry.mergeID >= 0 )
			{
				if ( ! map.containsKey( entry.mergeID ) )
					throw new ErrorClass( "Unexpected error on search for IDs" ) ;
				entry.mergeElement =  map.get( entry.mergeID ) ;
			}
		}
	}
	public boolean mustIgnored()
	{
		if ( this.toDo == ToDo.NONE )
			return true ;
		return false ;
	} ;
	public boolean mustUpdated() { return this.toDo == ToDo.UPDATE ; } ;
	public boolean mustDeleted() { return this.toDo == ToDo.DELETE ; } ;
	public boolean mustWrite()
	{
		if ( this.toDo == ToDo.DELETE || this.toDo == ToDo.DELETE_BY_PROVIDER )
			return false ;
		else if ( this.statusService == StatusService.REMOVED )
			return false ;
		return true ;
	}
	public void setToDelete()
	{
		this.toDo = ToDo.DELETE ;
		this.prepareRemove() ;
	} ;
	public boolean exists()
	{
		return this.toDo != ToDo.DELETE ;
	}
	public void prepareRemove()
	{
		if ( this.mergedEntries != null )
			for ( DVBViewerEntry e : this.mergedEntries )
				e.mergeElement = null ;
		this.mergedEntries = null ;
		if ( this.mergeElement != null )
		{
			this.mergeElement.mergedEntries.remove( this ) ;
			this.mergeElement.mergingChanged = true ;
		}
		this.mergeElement = null ;
	}
	public boolean isOutdatedByProvider() { return this.outDatedInfo.isOutdated( this.provider ) ; } ;
	public static void removeOutdatedEntries( final ArrayList<DVBViewerEntry> list )
	{
		long now = System.currentTimeMillis() ;
		Iterator< DVBViewerEntry > itE ;
		for ( itE = list.iterator() ; itE.hasNext() ;)
		{
			DVBViewerEntry e = itE.next() ;
			if ( ! e.isOutdated(now))
				continue ;
			if ( ! e.isMergeElement() )
				continue ;
			e.prepareRemove() ;
			itE.remove() ;
		}
		for ( itE = list.iterator() ; itE.hasNext() ;)
		{
			DVBViewerEntry e = itE.next() ;
			if ( ! e.isOutdated(now) )
				continue ;
			if ( e.mergeElement != null )
				continue ;			// assigned merge element is still valid
			itE.remove() ;
		}
	}
	public void setRoot( final DVBViewerEntry root ) { this.root = root ; } ;
	public DVBViewerEntry getParent()
	{
		if ( this.mergeElement == null )
			return this.root ;
		else
			return this.mergeElement ;
	}
	public TreePath getPath()
	{
		if ( this.mergeElement == null )
		{
			DVBViewerEntry[] path = { this.root, this } ;
			return new TreePath( path ) ;
		}
		else
		{
			DVBViewerEntry[] path = { this.mergeElement.root, this.mergeElement, this } ;
			return new TreePath( path ) ;
		}
	}
	public boolean 	isCollapsed() { return this.isCollapsed ; } ;
	public void setIsCollapsed( boolean isCollapsed ) { this.isCollapsed = isCollapsed ; } ;
	public static boolean isMergePossible( final DVBViewerEntry [] entries )
	{
		if ( entries.length < 2 )
			return false ;
		
		String program = null ;
		
		long start = -1 ;
		long end = -1 ;
		
		for ( DVBViewerEntry entry : entries )
		{
			DVBViewerEntry act = entry ;
			if ( entry.isMerged() )
				act = entry.mergeElement ;
			if ( program == null )
				program = entry.getChannel() ;
			if ( ! program.equals( entry.getChannel() ) )
				return false ;
			if ( start < 0 || act.start < start )
				start = act.start ;
			if ( end < 0 || act.end > end )
				end = act.end ;
		}
		return ( end - start ) < Constants.DAYMILLSEC ;
	}
	public static void mergeEntries( final DVBViewer dvbViewer,  final DVBViewerEntry [] entries )
	{
		if ( entries.length == 0 )
			return ;
		
		DVBViewerEntry mergeElement = null ;
		
		for ( DVBViewerEntry entry : entries )
			if ( entry.isMergeElement() )
			{
				mergeElement = entry ;
				break ;
			}
			else if ( entry.isMerged() )
			{
				mergeElement = entry.mergeElement ;
				break ;
			}
		if ( mergeElement == null )
			mergeElement = entries[ 0 ] ;
		
		for ( int ix = 0 ; ix < entries.length ; ix++ )
		{
			DVBViewerEntry entry = entries[ ix ] ;
			if ( entry == mergeElement )
				continue ;
			if ( entry.mergeElement != null && mergeElement.mergedEntries.contains( entry ))
				continue ;
			entry.mergeStatus = MergeStatus.DISABLED ;
			DVBViewerEntry newEntry = mergeElement.update( entry, dvbViewer.getSeparator() ) ;
			if ( newEntry != null )
			{
				newEntry.mergeStatus = MergeStatus.DISABLED ;
				dvbViewer.addRecordingEntry( newEntry ) ;
			}
			
		}
	}
	public static boolean isSplittingPossible( final DVBViewerEntry [] entries )
	{
		boolean isMerged = false ; ;
		boolean isMergeElement = false ;
		for ( DVBViewerEntry entry : entries )
		{
			if ( entry.isMerged() )
				isMerged = true ;
			else if ( entry.isMergeElement() )
				isMergeElement = true ;
			else
				return false ;
		}
		return isMerged ^ isMergeElement ;
	}
	public static void splitEntries( final DVBViewer dvbViewer, final DVBViewerEntry [] entries )
	{
		for ( DVBViewerEntry entryBase : entries )
		{
			if ( entryBase.isMergeElement() )
			{
				for ( DVBViewerEntry entry : entryBase.mergedEntries )
				{
					entry.mergeElement = null ;
					entry.statusService = StatusService.ENABLED ;
					entry.mergeStatus = MergeStatus.DISABLED ;
					entry.toDo = ToDo.UPDATE ;
				}
				entryBase.mergedEntries = null ;
				entryBase.mergeStatus = MergeStatus.DISABLED ;
				entryBase.toDo = ToDo.DELETE ;
			}
			else
			{
				if( entryBase.mergeElement == null )
					continue ;
				
				if ( entryBase.mergeElement.mergedEntries.size() <= 2 )
				{
					DVBViewerEntry mergeElement = entryBase.mergeElement ;
					for ( DVBViewerEntry entry : mergeElement.mergedEntries )
					{
						entry.mergeElement = null ;
						entry.statusService = StatusService.ENABLED ;
						entry.mergeStatus = MergeStatus.DISABLED ;
						entry.toDo = ToDo.UPDATE ;
					}
					mergeElement.mergedEntries = null ;
					if ( mergeElement.toDo == ToDo.NEW )
						dvbViewer.getRecordEntries().remove( mergeElement ) ;
					mergeElement.toDo = ToDo.DELETE ;
				}
				else
				{
					DVBViewerEntry mergeElement = entryBase.mergeElement ;
					mergeElement.mergedEntries.remove( entryBase ) ;
					mergeElement.createTitle( dvbViewer.getSeparator() ) ;
					mergeElement.calcStartEnd() ;
					if ( mergeElement.toDo != ToDo.NEW )
						mergeElement.toDo = ToDo.UPDATE ;
					entryBase.statusService = StatusService.ENABLED ;
					entryBase.mergeElement = null ;
					entryBase.mergeStatus = MergeStatus.DISABLED ;
					entryBase.toDo = ToDo.UPDATE ;
				}
			}
		}
	}
	public static boolean isDeletePossible( final DVBViewerEntry [] entries )
	{
		
		for ( DVBViewerEntry entry : entries )
		{
			if ( entry.isMergeElement() )
				return false ;
		}
		return entries.length > 0 ;
	}
	public static void deleteEntries( final DVBViewerEntry [] entries )
	{
		for ( DVBViewerEntry entry : entries )
			entry.setToDelete() ;
	}
	public static DVBViewerEntry readXML( final XMLEventReader  reader, XMLEvent ev, String name )
	{
		Stack< String > stack = new Stack< String >() ;
		DVBViewerEntry entry = null ;
		while ( true )  {
			if( ev.isStartElement() )
			{
				stack.push( ev.asStartElement().getName().getLocalPart() );
				if ( stack.equals( DVBViewerEntry.entryXML ) )
				{
					int id = -1 ;
					String providerID = null ;
					boolean isFilterElement = false ;
					StatusService statusService = null ;
					String channel = null ;
					long start = -1 ;
					long end = -1 ;
					long startOrg = -1 ;
					long endOrg = -1 ;
					String days = "-------" ;
					MergeStatus mergeStatus = MergeStatus.UNKNOWN ;
					long mergeID = -1 ;
					Provider provider = null ;
					boolean isCollapsed = false ;
					OutDatedInfo outDatedInfo = new OutDatedInfo() ;
					ActionAfterItems actionAfter = ActionAfterItems.NONE ;
					TimerActionItems timerAction = TimerActionItems.RECORD ;

					@SuppressWarnings("unchecked")
					Iterator<Attribute> iter = ev.asStartElement().getAttributes();
					while( iter.hasNext() )
					{
						Attribute a = iter.next();
						String attributeName = a.getName().getLocalPart() ;
						String value = a.getValue() ;
						if (      attributeName.equals( "id" ) )
							id = Integer.valueOf( value ) ;
						else if ( attributeName.equals( "providerID" ) )
							providerID = value ;
						else if ( attributeName.equals( "isFilterElement" ) )
							isFilterElement = dvbviewertimerimport.xml.Conversions.getBoolean( value, ev, name ) ;
						else if ( attributeName.equals( "statusService" ) )
							statusService = StatusService.valueOf( value ) ;
						else if ( attributeName.equals( "channel" ) )
							channel =value ;
						else if ( attributeName.equals( "start" ) )
							start = Long.valueOf( value ) ;
						else if ( attributeName.equals( "end" ) )
							end = Long.valueOf( value ) ;
						else if ( attributeName.equals( "startOrg" ) )
							startOrg = Long.valueOf( value ) ;
						else if ( attributeName.equals( "endOrg" ) )
							endOrg = Long.valueOf( value ) ;
						else if ( attributeName.equals( "days" ) )
							days = value ;
						else if ( attributeName.equals( "timerAction" ) )
							timerAction = TimerActionItems.valueOf( value ) ;
						else if ( attributeName.equals( "actionAfter" ) )
							actionAfter = ActionAfterItems.valueOf( value ) ;
						else if ( attributeName.equals( "mergeStatus" ) )
							mergeStatus = MergeStatus.valueOf( value ) ;
						else if ( attributeName.equals( "mergeID" ) )
							mergeID = Integer.valueOf( value ) ;
						else if ( attributeName.equals( "provider" ) )
							provider = Provider.getProvider( value ) ;
						else if ( attributeName.equals( "isCollapsed" ) )
							isCollapsed = dvbviewertimerimport.xml.Conversions.getBoolean( value, ev, name ) ;
						else
							outDatedInfo.readXML( attributeName, value) ;
					}
			        entry = new DVBViewerEntry( id, isFilterElement, statusService,
												-1, providerID, channel,
												start, end, startOrg, endOrg,
												days, "", timerAction, actionAfter, 
												mergeStatus, mergeID, provider,
												outDatedInfo, isCollapsed,
												ToDo.NONE ) ;
				}
			}
			if ( ev.isCharacters() )
			{
				String data = ev.asCharacters().getData() ;
				if ( data.length() > 0 )
				{
					if      ( stack.equals( DVBViewerEntry.titleXML ) )
						entry.title += data ;
					else if ( stack.equals( DVBViewerEntry.mergedXML ) )
					{
						entry.mergedIDs.add( Long.valueOf( data.trim() ) ) ;
					}
				}
			}
			
		    if ( ev.isEndElement() )
		    {
		    	stack.pop();
			    if ( stack.size() == 0 )
			    	break ;
		    }
		    if ( ! reader.hasNext() )
		    	break ;
			try {
				ev = reader.nextEvent();
			} catch (XMLStreamException e) {
				throw new ErrorClass( e, "Unexpected error on closing the file \"" + name + "\"" );
			}
		}
		return entry ;
	}
	public void writeXML( IndentingXMLStreamWriter sw, File f )
	{
		try {
			sw.writeStartElement( "Entry" ) ;
			
			  sw.writeAttribute( "id",               Long.toString( this.id ) ) ;
			  if ( this.providerID != null )
				  sw.writeAttribute( "providerID",   this.providerID ) ;
			  if ( this.serviceID >= 0 )
				  sw.writeAttribute( "dvbID",         Long.toString( this.serviceID ) ) ;
			  sw.writeAttribute( "isFilterElement",  this.isFilterElement ) ;
			  sw.writeAttribute( "statusService",    this.statusService.toString() ) ;

			  sw.writeAttribute( "channel",          this.channel ) ;
			  sw.writeAttribute( "start",            Long.toString( this.start ) ) ;
			  sw.writeAttribute( "end",              Long.toString( this.end ) ) ;
			  sw.writeAttribute( "startOrg",         Long.toString( this.startOrg ) ) ;
			  sw.writeAttribute( "endOrg",           Long.toString( this.endOrg ) ) ;
			  sw.writeAttribute( "days",             this.days ) ;
			  sw.writeAttribute( "timerAction",      this.timerAction.name() ) ;
			  sw.writeAttribute( "actionAfter",      this.actionAfter.name() ) ;
			  sw.writeAttribute( "mergeStatus",      this.mergeStatus.name() ) ;
			  if ( this.isCollapsed )
			    sw.writeAttribute( "isCollapsed",    true ) ;
			  if ( this.mergeElement != null )
			  {
				  long mergeID = this.mergeElement.id ;
				  sw.writeAttribute( "mergeID",      Long.toString( mergeID ) ) ;
			  }
			  if ( this.provider != null )
				  sw.writeAttribute( "provider",     this.provider.getName() ) ;
			  this.outDatedInfo.writeXML( sw ) ;
	  
			  sw.writeStartElement( "Title" ) ;
			    sw.writeCharacters( this.title ) ;
			  sw.writeEndElement() ;
			  
			  if (mergedEntries != null )
			  {
				  sw.writeStartElement( "MergedEntries" ) ;
				  for ( DVBViewerEntry e : this.mergedEntries )
				  {
					  sw.writeStartElement( "Id" ) ;
					  sw.writeCharacters( Long.toString( e.id ) ) ;
					  sw.writeEndElement() ;
				  }
				  sw.writeEndElement() ;
			  }
			sw.writeEndElement() ;
			
		} catch (XMLStreamException e) {
			throw new ErrorClass( e, "Unexpected error on writing the file \"" + f.getName() + "\"" );
		}
	}
}
