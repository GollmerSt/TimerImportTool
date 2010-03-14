// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.dvbviewer ;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import dvbv.javanet.staxutils.IndentingXMLStreamWriter;
import dvbv.misc.* ;
import dvbv.provider.OutDatedInfo;
import dvbv.provider.Provider;
import dvbv.xml.StackXML;


public final class DVBViewerEntry{
	private enum CompStatus { DIFFER, EQUAL, IN_RANGE } ;
	public enum StatusService { ENABLED, DISABLED, REMOVED } ;				    				// applied to Service 
	public enum ToDo          { NONE, NEW, NEW_UPDATED, UPDATE, DELETE, MERGE } ;	//toDo by Service

	private static final StackXML< String > entryXML  = new StackXML< String >( "Entry" ) ;
	private static final StackXML< String > titleXML  = new StackXML< String >( "Entry", "Title") ;
	private static final StackXML< String > mergedXML = new StackXML< String >( "Entry", "MergedWith", "Id" ) ;

	
	private long id ;
	private boolean isFilterElement ;
	private StatusService statusService ;
	private long serviceID ;
	private final String channel ;
	private long start ;
	private long end ;
	private long startOrg ;
	private long endOrg ;
	private String title ;
	private boolean canMerge;
	private Provider provider ;
	private OutDatedInfo outDatedInfo = null ;
	private ArrayList< Long > mergedIDs = new ArrayList< Long >() ;
	private ArrayList< DVBViewerEntry > mergedEntries = null ;
	private boolean mergingChanged = false ;
	private long mergeID = -1 ;
	private DVBViewerEntry mergeElement = null ;
	private ToDo toDo ;


	public DVBViewerEntry( 	long id ,
							boolean isFilterElement ,
							StatusService statusService ,
							long serviceID , 
							String channel ,
							long start ,
							long end ,
							long startOrg ,
							long endOrg ,
							String title ,
							boolean canMerge ,
							long mergeID, 
							Provider provider ,
							OutDatedInfo outDatedInfo ,
							ToDo toDo )
	{
		this.id = id ;
		this.isFilterElement = isFilterElement ;
		this.statusService = statusService ;
		this.serviceID = serviceID ; 
		this.channel = channel ;
		this.start = start ;
		this.end = end ;
		this.startOrg = startOrg ;
		this.endOrg = endOrg ;
		this.title =  title ;
		this.canMerge = canMerge ;
		this.mergeID = mergeID ;
		this.provider = provider ;
		this.outDatedInfo = outDatedInfo.clone() ;
		this.toDo = toDo ;
	}

	public DVBViewerEntry( boolean enable, String channel, long start, long end, String title )
	{
		this( -1, /*Type.NEW, */false, StatusService.ENABLED , -1, channel,
			  start ,end , start, end, title, false, -1, null, new OutDatedInfo(), ToDo.NEW ) ;

		this.statusService = enable? StatusService.ENABLED : StatusService.DISABLED ;

	}
	public DVBViewerEntry( boolean enable, long serviceID, String channel, long start, long end, String title, boolean merge )
	{
		this( -1, /*Type.NEW, */false, StatusService.ENABLED , serviceID, channel,
				  start ,end , start, end, title, merge, -1, null, new OutDatedInfo(), ToDo.NONE ) ;

		this.statusService = enable? StatusService.ENABLED : StatusService.DISABLED ;
		
	}
	public DVBViewerEntry( String channel,
			               long start,
			               long end,
			               long startOrg,
			               long endOrg,
			               String title,
			               boolean merge,
			               Provider provider  )
	{
		this( -1, false, StatusService.ENABLED , -1, channel,
				  start ,end , startOrg, endOrg, title, merge,
				  -1, provider, new OutDatedInfo(), ToDo.NEW ) ;
		
		boolean isFilterElement = false ;
		if ( provider != null )
		{
			isFilterElement = provider.isFiltered() ;
		}
		this.isFilterElement = isFilterElement ;
	}
	public DVBViewerEntry clone()
	{
		DVBViewerEntry entry = new DVBViewerEntry( 	-1, this.isFilterElement,
													this.statusService, this.serviceID, this.channel ,
													this.start, this.end, this.startOrg,
													this.endOrg, this.title, this.canMerge, this.mergeID,
													this.provider, this.outDatedInfo.clone(), this.toDo ) ;
		
		if ( this.mergedEntries != null )
		{
			entry.mergedEntries = new ArrayList< DVBViewerEntry >() ;
			entry.mergedEntries.addAll( this.mergedEntries ) ;
		}
		
		entry.mergedEntries = this.mergedEntries ;
		
		return entry ;
	}
	private String createTitle( String separator )
	{
		boolean first = true ;
		
		String title = "" ;
		
		for ( Iterator< DVBViewerEntry > it = this.mergedEntries.iterator() ; it.hasNext() ; )
		{
			if ( first )
				first = false ;
			else
				title += separator ;
			title += it.next().title ;
		}
		return title;
	}
	private CompStatus compareWithService( final DVBViewerEntry service )
	{
		if ( ! this.channel.equals( service.channel ) )
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
		
		if ( this.startOrg == e.startOrg && this.endOrg == e.endOrg )
			return true ;
		return false ;
	}
	public static void updateXMLDataByServiceDataEqual(
	          final ArrayList<DVBViewerEntry> xml, 
	          final ArrayList<DVBViewerEntry> service )
	{
// Find and assign all easy to assign service entries, remove the entries from
// the merge elements if entry is enabled

		for ( Iterator<DVBViewerEntry> itX = xml.iterator() ; itX.hasNext() ; )
		{
			DVBViewerEntry x = itX.next() ;

			if ( x.serviceID >= 0 )
				continue ;
			
			ArrayList<DVBViewerEntry> list = new ArrayList< DVBViewerEntry >() ;
			
			for ( Iterator<DVBViewerEntry> itS = service.iterator() ; itS.hasNext() ; )
			{
				DVBViewerEntry s = itS.next() ;
				if ( x.compareWithService( s ) == CompStatus.EQUAL )
					list.add( s ) ;
			}
			
			if ( list.size() == 0 )
				continue ;
			
			if ( list.size() > 1 )
			{
				ArrayList<DVBViewerEntry> choices = Conversions.getTheBestChoices(
                    x.getTitle(), list, 2, 3, new Function() ) ;
				list = choices ;
			}
			
			if ( list.size() > 1 )
				for ( Iterator<DVBViewerEntry> itS = list.iterator() ; itS.hasNext() ; ) 
				{
					DVBViewerEntry s = itS.next() ;
					if ( s.isEnabled() == x.isEnabled() )
					{
						list.set( 0, s ) ;
						break ;
					}
				}
			
			DVBViewerEntry s = list.get( 0 ) ;  // get first choice
						
			x.serviceID = s.serviceID ;
			
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
					x.toDo = ToDo.MERGE ;
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
				if ( ! isIn )
				{
					DVBViewerEntry n = x.clone() ;
					n.toDo = ToDo.UPDATE ;
					n.start = nStart ;
					n.end   = nEnd ;
					n.startOrg = nStartOrg ;
					n.endOrg = nEndOrg ;
					n.mergedEntries = nMergedEntries ;
					n.title = n.createTitle( separator ) ;
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
					x.title = x.createTitle( separator ) ;
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
	public static void updateXMLDataByServiceDataFuzzy(
	          final ArrayList<DVBViewerEntry> xml, 
	          final ArrayList<DVBViewerEntry> service,
	          final String separator,
	          boolean allElements)
	{
		for ( Iterator<DVBViewerEntry> itX = xml.iterator() ; itX.hasNext() ; )
		{
			DVBViewerEntry x = itX.next() ;

			if ( x.serviceID >= 0 )
				continue ;
			
			if ( !allElements && x.isMergeElement() )
				continue ;
			
			ArrayList< DVBViewerEntry > list = new ArrayList< DVBViewerEntry >() ;
			
			for ( Iterator<DVBViewerEntry> itS = service.iterator() ; itS.hasNext() ; )
			{
				DVBViewerEntry s = itS.next() ;
				
				if ( x.compareWithService( s ) != CompStatus.IN_RANGE )
					continue ;
				
				list.add( s ) ;
			}
			
			if ( list.size() == 0 )
				continue ;
			
			DVBViewerEntry s  = (DVBViewerEntry) Conversions.getTheBestChoice(
					                       x.toString(), list, 2, 3, new Function() ) ;
						
			x.start     = s.start ;
			x.end       = s.end ;
			Log.out( true, "The xml title \"" + x.title 
					        + "\" is assigned to the service title\"" + s.title + "\"" ) ; 
			x.title     = s.title ;
			x.serviceID = s.serviceID ;
			service.remove( s ) ;
		}
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
		
		updateXMLDataByServiceDataEqual( xml, service ) ;
		
		
		// Second pass:  Rework merge elements
		
		reworkMergeElements( xml, separator, maxID ) ;
			
		// Third pass:  Try to assign the modifies merge entries again,
		//              which aren't assigned
		
		updateXMLDataByServiceDataEqual( xml, service ) ;

		// Fourth pass: Try to assign the service entries in a fuzzy way (title, time .....)
		
		updateXMLDataByServiceDataFuzzy( xml, service, separator, false ) ;
		
		// Fifth pass:  Rework merge elements
		
		reworkMergeElements( xml, separator, maxID ) ;

		// Sixth pass: Try to assign the service entries in a fuzzy way (title, time .....)
		
		updateXMLDataByServiceDataFuzzy( xml, service, separator, true ) ;

	
		// Seventh pass: Set the rest of unassigned XML entries to REMOVED

		setToRemovedOrDeleteUnassignedXMLEntries( xml ) ;

		for ( Iterator< DVBViewerEntry > it = service.iterator() ; it.hasNext() ; )
		{
			DVBViewerEntry e = it.next() ;
			e.toDo = ToDo.MERGE ;
			e.id = maxID.increment() ;
			xml.add( e ) ;
		}

}
	
	private void addMergedEntry( DVBViewerEntry entry )
	{
		if ( this.mergedEntries == null )
			this.mergedEntries = new ArrayList< DVBViewerEntry >() ;
		boolean isIncluded = false ;
		for ( int ix = 0 ; ix < this.mergedEntries.size() ; ix++ )
		{
			if ( this.mergedEntries.get( ix ).start > entry.start )
			{
				this.mergedEntries.add( ix, entry ) ;
				isIncluded = true ;
				break ;
			}	
		}
		if ( isIncluded == false )
			this.mergedEntries.add( entry ) ;
	}
	public DVBViewerEntry update( DVBViewerEntry dE, String separator )
	{
		DVBViewerEntry result = null ;
		
		if ( this.mergedEntries == null )
		{
			result = this.clone() ;
			result.statusService = StatusService.DISABLED ;
			if ( this.toDo == ToDo.NONE || this.toDo == ToDo.MERGE )
				result.toDo = ToDo.UPDATE ;				
			result.mergeElement = this ;
			this.addMergedEntry( result ) ;
			this.isFilterElement = false ;
		}
		this.addMergedEntry( dE ) ;
		this.start = Math.min(this.start, dE.getStart() ) ;
		this.end   = Math.max( this.end,  dE.getEnd() ) ;

		this.title = this.createTitle( separator ) ;
		if ( this.toDo == ToDo.NEW )
			this.toDo = ToDo.NEW_UPDATED ;
		else if ( this.toDo == ToDo.NONE )
			this.toDo = ToDo.UPDATE ;
		else if ( this.toDo == ToDo.DELETE )
			throw new ErrorClass( "Unexpected error in DVBViewerEntry.update" ) ;

		dE.disable() ;
		dE.mergeElement = this ;
		
		return result ;
	}
	public void disable()
	{
		if ( this.toDo == ToDo.DELETE )
			throw new ErrorClass( "Unexpected error in DVBViewerEntry.disable" ) ;
		
		this.statusService = StatusService.DISABLED ;

		if ( this.toDo == ToDo.NONE )
		{
			this.toDo = ToDo.UPDATE ;
		}
		else if ( this.toDo == ToDo.MERGE )
		{
			this.statusService = StatusService.DISABLED ;
			this.toDo = ToDo.UPDATE ;
		}
	}
	public long getID() { return this.id ; } ;
	public void setID( long id ) { this.id = id ; } ;
	public long getServiceID() { return this.serviceID ; } ;
	public String getChannel() { return this.channel ; } ;
	public String getTitle() {return this.title ; } ;
	public String toString() {return this.title ; } ;
	public long getStart() { return this.start ; } ;
	public long getEnd()   { return this.end ; } ;
	public 	boolean isInRange( long start, long end )
	{
		if ( this.start <= start && this.end >= start )
			return true ;
		if ( this.start <= end && this.end >= end )
			return true ;
		return false ;
	}
	public boolean isOutdated( long now)
	{
		return this.end + Constants.DAYMILLSEC < now ;
	}
	public boolean toMerge() { return this.canMerge ; } ;
	public boolean mustMerge( DVBViewerEntry dE )
	{
		if ( ! this.channel.equals( dE.channel ) )
			return false ;
		if ( this.toDo == ToDo.NONE && dE.toDo == ToDo.NONE )
			return false ;
		if ( this.statusService != StatusService.ENABLED && this.statusService != StatusService.ENABLED )
			return false ;
		if ( this.toDo == ToDo.DELETE || dE.toDo == ToDo.DELETE )
			return false ;
		if ( ( this.toDo == ToDo.MERGE || dE.toDo == ToDo.MERGE ) && this.toDo != dE.toDo )
			return false ;
		if ( ! this.canMerge || ! dE.canMerge )
			return false ;
		if ( this.isDisabled() || dE.isDisabled() )
			return false ;
		return this.isInRange( dE.start, dE.end ) ;
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
	public boolean isDisabled() { return this.statusService == StatusService.DISABLED ; } ;
	public boolean isEnabled()
	{
		return    this.statusService == StatusService.ENABLED ;
	}
	public boolean isMergeElement()
	{
		return this.mergedEntries != null && this.mergedEntries.size() != 0 ;
	} ;
	public boolean isFilterElement()
	{ 
		return this.isFilterElement && this.toDo == ToDo.NONE ;
	}
	public static void assignMergedElements( HashMap< Long, DVBViewerEntry> map )
	{
		for ( Iterator< DVBViewerEntry > itE = map.values().iterator() ; itE.hasNext() ; )
		{
			DVBViewerEntry entry = itE.next() ;
			
			if ( entry.mergedIDs != null && entry.mergedIDs.size() > 0 )
			{
				for ( Iterator< Long > itID = entry.mergedIDs.iterator() ; itID.hasNext() ; )
				{
					long id = itID.next() ;
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
		if (    this.toDo == ToDo.MERGE && this.mergedEntries == null )
			return true ;
		return false ;
	} ;
	public boolean mustUpdated() { return this.toDo == ToDo.UPDATE ; } ;
	public boolean mustDeleted() { return this.toDo == ToDo.DELETE ; } ;
	public void setToDelete() { this.toDo = ToDo.DELETE ; } ;
	public void prepareRemove()
	{
		if ( this.mergedEntries != null )
			for ( Iterator< DVBViewerEntry > it = this.mergedEntries.iterator() ; it.hasNext() ; )
				it.next().mergeElement = null ;
		this.mergedEntries = null ;
		if ( this.mergeElement != null )
			this.mergeElement.mergedEntries.remove( this ) ;	
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
	
	public static DVBViewerEntry readXML( final XMLEventReader  reader, XMLEvent ev, File f )
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
					boolean isFilterElement = false ;
					StatusService statusService = null ;
					String channel = null ;
					long start = -1 ;
					long end = -1 ;
					long startOrg = -1 ;
					long endOrg = -1 ;
					boolean canMerge = false ;
					long mergeID = -1 ;
					Provider provider = null ;
					OutDatedInfo outDatedInfo = new OutDatedInfo() ;

					@SuppressWarnings("unchecked")
					Iterator<Attribute> iter = ev.asStartElement().getAttributes();
			        while( iter.hasNext() )
			        {
			        	Attribute a = iter.next();
			        	String attributeName = a.getName().getLocalPart() ;
			        	String value = a.getValue() ;
			        	if (      attributeName.equals( "id" ) )
			        		id = Integer.valueOf( value ) ;
			        	else if ( attributeName.equals( "isFilterElement" ) )
			        		isFilterElement = dvbv.xml.Conversions.getBoolean( value, ev, f ) ;
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
			        	else if ( attributeName.equals( "canMerge" ) )
			        		canMerge = dvbv.xml.Conversions.getBoolean( value, ev, f ) ;
			        	else if ( attributeName.equals( "mergeID" ) )
			        		mergeID = Integer.valueOf( value ) ;
				        else if ( attributeName.equals( "provider" ) )
				        	provider = Provider.getProvider( value ) ;
			        	else
			        		outDatedInfo.readXML( attributeName, value) ;
			        }
			        entry = new DVBViewerEntry( id , isFilterElement,
												statusService , -1, channel,
												start, end, startOrg, endOrg,
												"", canMerge, mergeID, provider,
												outDatedInfo,
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
				throw new ErrorClass( e, "Unexpected error on closing the file \"" + f.getName() + "\"" );
			}
		}
		return entry ;
	}
	public void writeXML( IndentingXMLStreamWriter sw, File f )
	{
		try {
			sw.writeStartElement( "Entry" ) ;
			
			  sw.writeAttribute( "id",               Long.toString( this.id ) ) ;
			  sw.writeAttribute( "isFilterElement",  this.isFilterElement ) ;
			  sw.writeAttribute( "statusService",    this.statusService.toString() ) ;

			  sw.writeAttribute( "channel",          this.channel ) ;
			  sw.writeAttribute( "start",            Long.toString( this.start ) ) ;
			  sw.writeAttribute( "end",              Long.toString( this.end ) ) ;
			  sw.writeAttribute( "startOrg",         Long.toString( this.startOrg ) ) ;
			  sw.writeAttribute( "endOrg",           Long.toString( this.endOrg ) ) ;
			  sw.writeAttribute( "canMerge",         this.canMerge ) ;
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
				  sw.writeStartElement( "MergedWith" ) ;
				  for ( Iterator< DVBViewerEntry > it = this.mergedEntries.iterator() ; it.hasNext() ; )
				  {
					  sw.writeStartElement( "Id" ) ;
					  sw.writeCharacters( Long.toString( it.next().id ) ) ;
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
