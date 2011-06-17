// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer ;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.swing.tree.TreePath;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import dvbviewertimerimport.misc.Enums.ActionAfterItems;
import dvbviewertimerimport.misc.Enums.TimerActionItems;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;
import dvbviewertimerimport.misc.* ;
import dvbviewertimerimport.provider.OutDatedInfo;
import dvbviewertimerimport.provider.Provider;
import dvbviewertimerimport.xml.StackXML;


public final class DVBViewerEntry  implements Cloneable
{
	private static final long searchIntervallOrg = 3 * 60 * 60 * 1000 ;	// search Intervall of 3 hours before original start and after original end of programm
	private static final long searchIntervallReal = 30 * 60 * 1000 ;	// search Intervall of 0.5 hours before start and after end of programm

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
	public enum StatusTimer	{ ENABLED, DISABLED, REMOVED, RECORDING } ;				    			// applied to Service
	public enum ToDo       	{ NONE, NEW, UPDATE, DELETE, DELETE_DVBVIEWER, DELETE_BY_PROVIDER } ;	//toDo by Service

	private static final StackXML< String > entryXML  = new StackXML< String >( "Entry" ) ;
	private static final StackXML< String > titleXML  = new StackXML< String >( "Entry", "Title") ;
	private static final StackXML< String > mergedXML = new StackXML< String >( "Entry", "MergedEntries", "Id" ) ;

	private static final SimpleDateFormat timeFormat = new SimpleDateFormat(" yyyy.MM.dd HH:mm" );
	private static StatusTimer timerStatusIfMerged = StatusTimer.DISABLED ;

	public static void setInActiveIfMerged( boolean set )
	{
		if ( set )
			timerStatusIfMerged = StatusTimer.DISABLED ;
		else
			timerStatusIfMerged = StatusTimer.REMOVED ;
	} ;
	public static boolean getInActiveIfMerged() { return timerStatusIfMerged == StatusTimer.DISABLED ; } ;

	private long id ;
	private String providerID ;
	private boolean isFilterElement ;
	private StatusTimer statusTimer ;
	private long dvbViewerID ;
	private final String channel ;
	private String program = null ;
	private String channelID = null ;
	private ChannelSet channelSet = null ;
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
							StatusTimer statusTimer ,
							long dvbViewerID ,
							String providerID,
							String channel ,
							ChannelSet channelSet ,
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
		this.statusTimer = statusTimer ;
		this.dvbViewerID = dvbViewerID ;
		this.providerID = providerID ;
		this.channel = channel ;
		this.channelSet = channelSet ;
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
		this.statusTimer = StatusTimer.DISABLED ;
		this.dvbViewerID = -1 ;
		this.providerID = null ;
		this.channel = null ;
		this.channelSet = null ;
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

	public DVBViewerEntry( StatusTimer status, long dvbViewerID, String channel, long start, long end, String days,
						   String title, TimerActionItems timerAction, ActionAfterItems actionAfter )
	{
		this( -1, false, status , dvbViewerID, null, channel, null, start ,end ,
				         start, end, days, title, timerAction, actionAfter, MergeStatus.UNKNOWN,
				         -1, null, new OutDatedInfo(), false, ToDo.NONE ) ;

		this.setMergeStatus( this.mergeStatus ) ;
	}
	public DVBViewerEntry( String channel,
			   			   ChannelSet channelSet,
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
		this( -1, false, StatusTimer.ENABLED , -1, providerID, channel, channelSet, start ,end ,
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
		DVBViewerEntry entry = new DVBViewerEntry( 	-1, this.isFilterElement, this.statusTimer,
													this.dvbViewerID, this.providerID, this.channel , this.channelSet,
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
	public void setMergeStatus( MergeStatus m )
	{
		if ( ! this.days.equals( "-------" ) )
			this.mergeStatus = MergeStatus.DISABLED ;
		else
			this.mergeStatus = m ;
	}
	private void setStatusTimerToMerged()
	{
		if ( this.isRecording() )
			throw new ErrorClass( "Error: Try to merge a recording entry" ) ;
		
		if ( this.statusTimer != timerStatusIfMerged )
		{
			this.statusTimer = timerStatusIfMerged ;

			if ( this.statusTimer == StatusTimer.REMOVED )
			{
				if ( this.toDo == ToDo.NONE || this.toDo == ToDo.UPDATE )
					this.toDo = ToDo.DELETE_DVBVIEWER ;
				else if ( this.toDo == ToDo.NEW )
					this.toDo = ToDo.NONE ;
			}
			else
			{
				if ( this.toDo == ToDo.NONE )
					this.toDo = ToDo.UPDATE ;
			}
		}
	}
	private class PartialTitle
	{
		final static int MIN_LENGTH = 5 ;

		private final StringBuilder part ;
		private PartialTitle left = null ;
		int prefixLength = 0 ;
		boolean isPrefixSet = false ;
		boolean isReduced = false ;
		int lengthParenthesis = -1 ;
		int startParenthesis = 0 ;
		
		public PartialTitle( String s )
		{
			part = new StringBuilder( s ) ;
			getInparenthesis() ;
			part.delete(startParenthesis, startParenthesis + lengthParenthesis ) ;
			
		}
		public void deletePrefix( int n )
		{
			isReduced = true ;
			setPrefixLength( n ) ;
		}
		private void setPrefixLength( int n )
		{
			if ( isReduced )
			{
				left.setPrefixLength( n ) ;
				prefixLength = left.getPrefixLength( false ) ;
				isPrefixSet = true ;
			}
			else if ( isPrefixSet )
				Math.min(n, prefixLength) ;
			else
			{
				prefixLength = n ;
				isPrefixSet = true ;
			}
		}
		public int getPrefixLength()
		{
			return getPrefixLength( true ) ;
		}
		public int getPrefixLength( boolean isOutput )
		{
			if ( isReduced && isOutput )
				return 0 ;
			if ( isReduced && ! isPrefixSet )
			{
				prefixLength = left.getPrefixLength( false ) ;
				isPrefixSet = true ;
			}
			return prefixLength ;
		}
		public int mainPartLength()
		{
			return part.length() - getPrefixLength( false ) ;
		}
		public int length()
		{
			int length = part.length() ;
			if ( isReduced )
			{
				length -= getPrefixLength( false ) ;
			}
			return length ;
		}
		public char charAt( int i )
		{
			return part.charAt( i ) ;
		}
		public String get( int i )
		{
			if ( isReduced )
			{
				int pl = getPrefixLength( false ) ;
				return part.substring( pl, pl + i ) ;
			}
			else
				return part.substring( 0, i ) ;
		}
		public String toString()
		{
			return part.toString() ;
		}
		public String getOriginal()
		{
			return part.toString() ;
		}
		public int lengthOrg()
		{
			return part.length() ;
		}
		public void searchAndSetPrefix( PartialTitle left )
		{
			this.left = left ;
			for ( int c = 0 ; c < lengthOrg() && c < left.lengthOrg() ; ++c )
			{
				String leftOrg = left.getOriginal() ;
				if ( ( leftOrg.charAt( c ) != charAt( c ) ) || ( length() - c <= MIN_LENGTH ) )
				{
					for ( --c ; c >= 0 ; --c )
						if ( ! Character.isLetterOrDigit( (int) charAt(c) ) )
							break ;
					++c ;
					
					if ( c == 0 )
						break ;

					deletePrefix( c ) ;
					break ;
				}
			}
		}
		public int getInparenthesis()
		{
			if ( lengthParenthesis < 0 )
			{
				int level = 0 ;
				lengthParenthesis = 0 ;
				
				for ( int i = 0 ; i < part.length() ; ++i  )
				{
					if ( level > 0 )
						++lengthParenthesis ;
					char c = part.charAt( i ) ;
					if ( c == '(')
					{
						++level ;
						startParenthesis = i ;
					}
					else if ( c == ')' )
					{
						--level ;
						if ( level <= 0 )
						{
							++lengthParenthesis ;
							if ( level < 0 )
								startParenthesis = i ;
							break ;
						}
					}
				}
			}
			return lengthParenthesis ;
		}
	}

	private void createTitle( String separator, final int maxLength )
	{
		final int WEIGHT_PREFIX = 2 ;
		final int WEIGHT_MAIN = 1 ;
		
		if ( ! this.isMergeElement() )
			return ;
		
		int length = 0 ;

		StringBuilder title = new StringBuilder() ;

		for ( DVBViewerEntry e : this.mergedEntries )
		{
			title.append( separator ) ;
			title.append( e.title ) ;
			length += e.title.length() ;
		}
				
		if ( title.length() <= maxLength || maxLength < 0 )
		{
			this.title = title.substring( separator.length() ) ;
			return ;
		}

		int lengthMax = maxLength - (this.mergedEntries.size()-1) * separator.length() ;
		
		ArrayList< PartialTitle > parts = new ArrayList< PartialTitle >() ;
		
		for ( DVBViewerEntry e : this.mergedEntries )
			parts.add( new PartialTitle( e.title ) ) ;
		
		
		for ( int i = 1 ; i < parts.size() ; ++i )
			parts.get(i).searchAndSetPrefix( parts.get(i-1) ) ;
		
		int prefixLength = 0 ;
		int mainLength = 0 ;
		
		for ( PartialTitle part : parts )
		{
			prefixLength += part.getPrefixLength() ;
			mainLength += part.mainPartLength() ;
			
		}
		
		length = mainLength + prefixLength ;
		int mainLenMax = mainLength ;
		
		int prefixLenMax = prefixLength ;
		
		if ( length > lengthMax )
		{
			int weight = WEIGHT_PREFIX * prefixLength + WEIGHT_MAIN * mainLength ;
			prefixLenMax = ( WEIGHT_PREFIX * prefixLength * lengthMax / weight + 1 ) / WEIGHT_PREFIX ;
			mainLenMax = lengthMax - prefixLenMax ;
		}
		
		
/*		if ( mainLength < lengthMax )
			lengthMax = mainLength ;
		
		if ( prefixLenMax > lengthMax )
			prefixLenMax = lengthMax ;
*/				
		title =  new StringBuilder() ;

		for ( PartialTitle p : parts )
		{
			title.append( separator ) ;
			int pPrefixLength = 0 ;
			if ( prefixLength != 0 )
			{
				pPrefixLength = p.getPrefixLength() * prefixLenMax / prefixLength ;

				prefixLenMax  -= pPrefixLength ;
				prefixLength  -= p.getPrefixLength() ;
			}
			int pLength = p.mainPartLength() * mainLenMax / mainLength ;
			title.append( p.get( pPrefixLength + pLength) ) ;

			mainLength     -= p.mainPartLength() ;
			mainLenMax  -= pLength ;
		}

		this.title = title.substring( separator.length() ) ;
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
	private boolean inRange( String channelID, long start, long end, String days )
	{
		if ( ! this.getChannelID().equals( channelID ) )
			return false ;

		if ( ! this.days.equals( days ) )
			return false ;

		if ( this.start >= start && this.end <= end )
			return true ;

		return false ;
	}
	private CompStatus compareWithDVBViewer( final DVBViewerEntry dvbViewer )
	{
		if ( ! this.getChannelID().equals( dvbViewer.getChannelID() ) )
			return CompStatus.DIFFER ;

		if ( ! this.days.equals( dvbViewer.days ) )
			return CompStatus.DIFFER ;

		if ( this.start == dvbViewer.start && this.end == dvbViewer.end )
			return CompStatus.EQUAL ;

		if (    this.startOrg >= dvbViewer.start && this.endOrg <= dvbViewer.end )
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
	
	static ArrayList<DVBViewerEntry> searchSurroundedEntries( final DVBViewerEntry entry,  final ArrayList<DVBViewerEntry> list )
	{
		ArrayList<DVBViewerEntry> result = new ArrayList< DVBViewerEntry >() ;

		String channelID = entry.getChannelID() ;
		long start = entry.startOrg - DVBViewerEntry.searchIntervallOrg ;
		if ( start > entry.start - DVBViewerEntry.searchIntervallReal)
			start = entry.start - DVBViewerEntry.searchIntervallReal ;
		long end = entry.endOrg + DVBViewerEntry.searchIntervallOrg ;
		if ( end < entry.end + DVBViewerEntry.searchIntervallReal)
			end = entry.end + DVBViewerEntry.searchIntervallReal ;
		String days = entry.days ;


		for ( DVBViewerEntry e : list )
		{
			if ( e.inRange( channelID, start, end, days) )
				result.add( e ) ;
		}
		return result ;
	}
	
	private static interface SearchAlgorithm
	{
		public ArrayList< DVBViewerEntry > execute( final DVBViewerEntry entry,
											  final ArrayList< DVBViewerEntry > entries ) ;
	}
	
	private class SearchBiDirectional
	{
		HashMap< DVBViewerEntry, ArrayList < DVBViewerEntry > > choicesInRangeLists = new HashMap< DVBViewerEntry, ArrayList < DVBViewerEntry > >() ;
		
		public class Result
		{
			private final boolean isSure ;
			private final ArrayList<DVBViewerEntry> entries ;
			
			public boolean isSure() { return isSure ; } ;
			public ArrayList<DVBViewerEntry> get() { return entries ; } ;
			public int size() { return entries.size(); } ; 
			
			private Result( final boolean isSure, final ArrayList<DVBViewerEntry> entries )
			{
				this.isSure  = isSure ;
				this.entries = entries ;
			}
		}

		public Result searchBiDirectional( final DVBViewerEntry xEntry,
											  final ArrayList< DVBViewerEntry > choices,
											  final ArrayList< DVBViewerEntry > xml,
											  final SearchAlgorithm algo )
		{
			ArrayList<DVBViewerEntry> result = algo.execute( xEntry, choices ) ;
	
			ArrayList<DVBViewerEntry> cChoices = null ;

			for ( Iterator< DVBViewerEntry > it = result.iterator() ; it.hasNext() ; )
			{
				ArrayList<DVBViewerEntry> cList = null ;
					
				DVBViewerEntry cS = it.next() ;
					
				if ( !choicesInRangeLists.containsKey( cS ))
				{
					cList = searchSurroundedEntries( cS, xml ) ;
	
					choicesInRangeLists.put(cS, cList ) ;
				}
				else
					cList = choicesInRangeLists.get( cS ) ;
					
				cChoices = algo.execute( cS, cList ) ;
	
				boolean found = false ;
				
				for ( DVBViewerEntry e : cChoices )
				{
					if ( xEntry == e )
					{
						found = true ;
						break ;
					}
				}
				if ( ! found )
					it.remove() ;
			}
			return new Result( cChoices.size() <= 1 && result.size() <= 1, result) ;
		}
	}

	public static void updateXMLDataByDVBViewerDataFuzzy(
	          final ArrayList<DVBViewerEntry> xml,
	          final ArrayList<DVBViewerEntry> dvbViewer,
	          boolean allElements)
	{
		SearchBiDirectional searchBi = new DVBViewerEntry().new SearchBiDirectional() ;

		
		// Find and assign all easy to assign service entries, remove the entries from
		// the merge elements if entry is enabled
		for ( DVBViewerEntry x : xml )
		{
			if ( x.dvbViewerID >= 0 || ( x.mergedEntries != null && ! allElements ) )
				continue ;

			if ( x.isRemoved() )
				continue ;

			// Find all programs in the range of searchIntervall
			
			SearchBiDirectional.Result list = searchBi.new Result( true, searchSurroundedEntries( x, dvbViewer ) ) ;

			if ( list.size() == 0 )
				continue ;

			// find the best match of title
			{
				list = searchBi.searchBiDirectional(x, list.get(), xml, new SearchAlgorithm(){

					@Override
					public ArrayList<DVBViewerEntry> execute(
							DVBViewerEntry entry,
							ArrayList<DVBViewerEntry> entries) {
						return Helper.getTheBestChoices( entry.getTitle(), entries, 2, 3, new Function(), 
							new Function(){
								@Override
								public int arrayIntToInt( final ArrayList< Integer > list, final int integer, final String search, final String array )
								{
									return this.arrayIntToInt3( list, integer, search, array ) ;
								}
						} ) ;
					}} ) ;
				if ( list.size() == 0 )
					continue ;
			}


			if ( ! list.isSure() )
			{
				ArrayList<DVBViewerEntry> choices = new ArrayList< DVBViewerEntry >() ;

				for ( DVBViewerEntry s : list.get() )
				{
					CompStatus co = x.compareWithDVBViewer( s ) ;
					if ( co == CompStatus.EQUAL || co == CompStatus.IN_RANGE )
						choices.add( s ) ;
				}
				if ( choices.size() > 0 )
					list = searchBi.new Result( choices.size() <= 1, choices ) ;
			}

			if ( ! list.isSure() )
			{
				list = searchBi.searchBiDirectional(x, list.get(), xml, new SearchAlgorithm(){

					@Override
					public ArrayList<DVBViewerEntry> execute(
							DVBViewerEntry entry,
							ArrayList<DVBViewerEntry> entries) {
						// TODO Auto-generated method stub
						ArrayList<DVBViewerEntry> choices = new ArrayList<DVBViewerEntry>() ;
						long minDiff = 999999999999999L ;

						for ( DVBViewerEntry e : entries )
						{
							long diff = e.start - entry.start ;
							diff = diff < -diff ? -diff : diff ;
							long diff1 = entry.end - e.end ;
							diff1 = diff1 < 0 ? -diff1 : diff1 ;
							diff += diff1/2 ;			// the weight of the post offset is lower than the pre offset
							if ( diff < minDiff )
							{
								choices.clear() ;
								choices.add( e ) ;
								minDiff = diff ;
							}
						}
						return choices ;
					}} ) ;

				if ( list.size() == 0 )
					continue ;
			}
			if ( list.size() > 1 )
			{
				ArrayList<DVBViewerEntry> choices = new ArrayList<DVBViewerEntry>() ;
				for ( DVBViewerEntry s : list.get() )
				{
					if ( s.isEnabled() == x.isEnabled() )
					{
						choices.add( s ) ;
					}
				}
				if ( choices.size() > 0 )
					list = searchBi.new Result( true, choices ) ;
			}
			
			if ( list.size() < 1 )
				continue ;
			
			DVBViewerEntry s = list.get().get( 0 ) ;  // get first choice

			x.dvbViewerID = s.dvbViewerID ;

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
				if ( x.statusTimer == StatusTimer.DISABLED )
				{
					x.setMergeStatus( MergeStatus.JUST_SEPARATED ) ;
					x.toDo        = ToDo.UPDATE ;
				}
				x.statusTimer = s.statusTimer ;
			}
			else
			{
				x.statusTimer = StatusTimer.DISABLED ;
			}
			dvbViewer.remove( s ) ;
		}
	}
	public static void reworkMergeElements( final ArrayList<DVBViewerEntry> xml,
											final String separator, DVBViewer.MaxID maxID,
											final int maxTitleLength )
	{
		ArrayList< DVBViewerEntry > newMergeEntries = new ArrayList< DVBViewerEntry >() ;

		for ( Iterator<DVBViewerEntry> itX = xml.iterator() ; itX.hasNext() ; )
		{
			DVBViewerEntry x = itX.next() ;

			if ( ! x.mergingChanged || x.isDeleted() )
				continue ;
			
			if ( x.isRecording() )
				throw new ErrorClass( "Error: Try to merge a recording entry" ) ;

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

					if ( m.isRecording() )
						throw new ErrorClass( "Error: Try to merge a recording entry" ) ;

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
					if ( nStart <= m.start && nEnd >= m.start || m.mergeStatus == MergeStatus.DISABLED )
					{
						if ( m.end > nEnd )
						{
							nEnd = m.end ;
							isChanged = true ;
						}
					}
					else if ( nStart <= m.end && nEnd >= m.end || m.mergeStatus == MergeStatus.DISABLED )
					{
						if ( m.start < nEnd )
						{
							nStart = m.start ;
							isChanged = true ;
						}
					}
					else if ( m.mergeStatus != MergeStatus.DISABLED )
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
					n.createTitle( separator, maxTitleLength ) ;
					if ( n.mergeElement != null )
						n.mergeElement.mergedEntries.add( n ) ;
					if ( n.dvbViewerID >= 0 )
						n.toDo = ToDo.UPDATE ;
					newMergeEntries.add( n ) ;
					n.mergingChanged = false ;
					x.id = maxID.increment() ;
					x.dvbViewerID = -1 ;
					x.toDo = ToDo.NEW ;
					nStart = -1 ;
				}
				else if ( ! isIn )
				{
					if ( nMergedEntries.size() == 1 )
					{
						DVBViewerEntry modify = nMergedEntries.get( 0 ) ;
						modify.statusTimer = StatusTimer.ENABLED ;
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
							modify.statusTimer = StatusTimer.ENABLED ;
							modify.mergeElement = null ;
							modify.toDo = ToDo.UPDATE ; // evtl. anders wenn modify = merge elemenet, aber kommt das vor?
						}
						if ( x.dvbViewerID >= 0)
							x.toDo = ToDo.DELETE ;
						else
							itX.remove() ;
						break ;
					}
					x.mergedEntries = nMergedEntries ;
					String titleOld = x.title ;
					x.createTitle( separator, maxTitleLength ) ;
					if (    x.toDo == ToDo.NONE  && x.dvbViewerID >= 0
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

			if ( x.dvbViewerID >= 0 )
				continue ;

			if ( x.isFilterElement || x.mergeElement != null )
			{
				if ( x.statusTimer == StatusTimer.REMOVED )
					continue ;
				Log.out( true, "The xml title \"" + x.title + "\" is set to REMOVED" ) ;
				x.statusTimer = StatusTimer.REMOVED ;
			}
			else
			{
				Log.out( true, "The xml title \"" + x.title + "\" is deleted" ) ;
				x.prepareRemove() ;
				itX.remove() ;
			}
		}
	}
	public static void updateXMLDataByDVBViewerData(
			          final ArrayList<DVBViewerEntry> xml,
			          final ArrayList<DVBViewerEntry> dvbViewer,
			          final String separator,
			          final DVBViewer.MaxID maxID,
			          final int maxTitleLength)
	{
		// Fist pass:  Find and assign all easy to assign service entries,
		//             remove the entries from the merge elements
		//             if entry is enabled

		updateXMLDataByDVBViewerDataFuzzy( xml, dvbViewer, true ) ;


		// Second pass:  Rework merge elements

		reworkMergeElements( xml, separator, maxID, maxTitleLength ) ;

		// Third pass:  Try to assign the modifies merge entries again,
		//              which aren't assigned

		updateXMLDataByDVBViewerDataFuzzy( xml, dvbViewer, true ) ;

		// Fourth pass: Set the rest of unassigned XML entries to REMOVED

		setToRemovedOrDeleteUnassignedXMLEntries( xml ) ;

		for ( DVBViewerEntry e : dvbViewer )
		{
			e.setMergeStatus( MergeStatus.DISABLED ) ;
			e.id = maxID.increment() ;
			xml.add( e ) ;
		}
	}
	public static void removeOutdatedProviderEntries( final ArrayList<DVBViewerEntry> xml )
	{
		for ( DVBViewerEntry e : xml )
		{
			if ( e.isOutdatedByProvider() )
				e.setToDelete() ;
		}
	}

	public static void beforeRecordingSettingProcces( final ArrayList<DVBViewerEntry> xml,
													  final String separator, DVBViewer.MaxID maxID,
													  final int maxTitleLength)
	{
		DVBViewerEntry.removeOutdatedProviderEntries( xml ) ;
		DVBViewerEntry.reworkMergeElements( xml, separator, maxID, maxTitleLength ) ;
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
			e.setMergeStatus( e.mergeStatus.post() ) ;
			if ( e.isRemoved() )
				e.dvbViewerID = -1 ;
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
	public DVBViewerEntry update( DVBViewerEntry dE, boolean setMergeEnable )
	{
		DVBViewerEntry result = null ;

		if ( this.mergedEntries == null )
		{
			result = this.clone() ;
			if ( result.toDo != ToDo.NEW )
				result.toDo = ToDo.UPDATE ;
			result.setStatusTimerToMerged() ;
			result.mergeElement = this ;
			if ( setMergeEnable)
				result.setMergeStatus( MergeStatus.ENABLED ) ;
			this.addMergedEntry( result ) ;
			this.isFilterElement = false ;
			this.providerID = null ;
			this.toDo = ToDo.NEW ;
		}
		if ( this.addMergedEntry( dE ) )
			dE.toDo = ToDo.DELETE ;
		else
		{
			dE.setStatusTimerToMerged() ;
			dE.mergeElement = this ;
			if ( setMergeEnable)
				dE.setMergeStatus( MergeStatus.ENABLED ) ;
		}

		if ( setMergeEnable)
			this.setMergeStatus( MergeStatus.ENABLED ) ;
		this.start    = Math.min(this.start,    dE.start ) ;
		this.startOrg = Math.min(this.startOrg, dE.startOrg ) ;
		this.end      = Math.max( this.end,  dE.getEnd() ) ;
		this.endOrg   = Math.max( this.endOrg,  dE.endOrg ) ;

		//this.createTitle( separator ) ;
		this.mergingChanged = true ;
		if ( this.toDo == ToDo.NONE )
			this.toDo = ToDo.UPDATE ;
		else if ( this.toDo == ToDo.DELETE )
			throw new ErrorClass( "Unexpected error in DVBViewerEntry.update" ) ;

		return result ;
	}
	public long getID() { return this.id ; } ;
	public void setID( long id ) { this.id = id ; } ;
	public long getDVBViewerID() { return this.dvbViewerID ; } ;
	public void setDVBViewerID( long id ) { this.dvbViewerID = id ; } ;
	public String getProviderCID() { return this.providerID ; } ;
	public void clearDVBViewerID() { this.dvbViewerID = -1 ; } ;
	public String getChannel() { return this.channel ; } ;
	public String getTitle() {return this.title ; } ;
	public ActionAfterItems getActionAfter() { return actionAfter ; } ;
	public TimerActionItems getTimerAction() { return timerAction ; } ;
	public String toString()
	{
		return this.title + timeFormat.format( new Date( this.start ) ) + timeFormat.format( new Date( this.end ) ) ;
	}
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
		
		return ! this.isRecording() && (   this.mergeStatus == MergeStatus.ENABLED
		                              || this.mergeStatus == MergeStatus.JUST_SEPARATED ) ;
	} ;
	public boolean mustMerge( DVBViewerEntry dE )
	{
		if ( this.isRecording() )
			return false ;
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
	public boolean isDisabled() { return this.statusTimer == StatusTimer.DISABLED || this.statusTimer == StatusTimer.REMOVED ; } ;
	public boolean isEnabled() { return this.statusTimer == StatusTimer.ENABLED || this.statusTimer == StatusTimer.RECORDING ; }
	public boolean isRecording() { return this.statusTimer == StatusTimer.RECORDING ; }

	public boolean isRemoved() { return this.statusTimer == StatusTimer.REMOVED ; }
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
	public boolean canDisplayed( long now )
	{
		if ( this.isDeleted() )
			return false ;
		if ( this.isMerged() )
		{
			if ( this.getMergeEntry().end < now )
				return false ;
		}
		else if ( this.end < now )
			return false ;
		return true ;
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
	public boolean mustDVBViewerDeleted() { return this.toDo == ToDo.DELETE || this.toDo == ToDo.DELETE_DVBVIEWER ; } ;
	public boolean mustWrite()
	{
		if ( this.toDo == ToDo.DELETE || this.toDo == ToDo.DELETE_BY_PROVIDER )
			return false ;
		else if ( this.statusTimer == StatusTimer.REMOVED )
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
	public boolean isOutdatedByProvider()
	{
		if ( this.provider != Provider.getProcessingProvider() )
			return false ;
		return this.outDatedInfo.isOutdated( this.provider ) ;
	} ;
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

		boolean differ = false ;

		DVBViewerEntry mergeElement = null ;

		for ( DVBViewerEntry entry : entries )
		{
			if ( entry.isRemoved() && ! entry.isMerged() || entry.isRecording() )
				return false ;
			DVBViewerEntry act = entry ;
			if ( entry.isMerged() )
			{
				act = entry.mergeElement ;
				if ( mergeElement == null )
					mergeElement = act ;
				else if ( act != mergeElement )
					differ = true ;
			}
			else if ( entry.isMergeElement() )
			{
				if ( mergeElement == null )
					mergeElement = entry ;
				else if ( entry != mergeElement )
					differ = true ;
			}
			else
				differ = true ;
			if ( program == null )
				program = entry.getChannel() ;
			if ( ! program.equals( entry.getChannel() ) )
				return false ;
			if ( start < 0 || act.start < start )
				start = act.start ;
			if ( end < 0 || act.end > end )
				end = act.end ;
		}
		if ( ( end - start ) >= Constants.DAYMILLSEC )
			return false ;
		return differ ;
	}
	public static void mergeEntries( final DVBViewer dvbViewer,  final DVBViewerEntry [] entries )
	{
		if ( entries.length == 0 )
			return ;

		for ( DVBViewerEntry entryBase : entries )
		{
			if ( entryBase.isMergeElement() )
			{
			for ( DVBViewerEntry entry : entryBase.mergedEntries )
				entry.setMergeStatus( MergeStatus.DISABLED ) ;
			}
			entryBase.setMergeStatus( MergeStatus.DISABLED ) ;
		}
		DVBViewerEntry mergeElement = entries[ 0 ] ;

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

		for ( DVBViewerEntry entry : entries )
		{
			if ( entry == mergeElement )
				continue ;
			if ( entry.mergeElement != null && mergeElement.mergedEntries.contains( entry ))
				continue ;
			//entry.mergeStatus = MergeStatus.DISABLED ;   //TODO
			DVBViewerEntry newEntry = mergeElement.update( entry, false ) ;
			if ( newEntry != null )
			{
				newEntry.setMergeStatus( MergeStatus.DISABLED ) ;
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
			if ( entry.isRecording() )
				return false ;
			if ( entry.isMerged() )
			{
				if ( entry.getMergeEntry().isRecording() )
					return false ;
				isMerged = true ;
			}
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
					entry.setActiveAndClearMerge() ;
					entry.setMergeStatus( MergeStatus.DISABLED ) ;
				}
				entryBase.mergedEntries = null ;
				entryBase.setMergeStatus( MergeStatus.DISABLED ) ;
				if ( entryBase.toDo != ToDo.NEW )
					entryBase.toDo = ToDo.DELETE ;
				else
					dvbViewer.getRecordEntries().remove( entryBase ) ;
			}
			else
			{
				if( entryBase.mergeElement == null )
					continue ;

				DVBViewerEntry mergeElement = entryBase.mergeElement ;
				mergeElement.mergedEntries.remove( entryBase ) ;
				mergeElement.mergingChanged = true ;
				if ( mergeElement.toDo != ToDo.NEW )
					mergeElement.toDo = ToDo.UPDATE ;
				entryBase.setActiveAndClearMerge() ;
				entryBase.setMergeStatus( MergeStatus.DISABLED ) ;
				entryBase.toDo = ToDo.UPDATE ;
			}
		}
	}
	private void setActiveAndClearMerge()
	{
		if ( this.toDo != ToDo.NEW )
		{
			if ( this.isRemoved() && ! ( this.isDeleted() || this.toDo == ToDo.DELETE_DVBVIEWER ) )
				this.toDo = ToDo.NEW ;
			else
				this.toDo = ToDo.UPDATE ;
		}
		this.statusTimer = StatusTimer.ENABLED ;
		this.mergeElement = null ;
	}
	public static boolean isDeletePossible( final DVBViewerEntry [] entries )
	{

		for ( DVBViewerEntry entry : entries )
		{
			if ( entry.isMergeElement() && !entry.isRecording() )
				return false ;
			else if ( entry.isFilterElement() && entry.isRemoved() )
				return false ;
			else if ( entry.isMerged() && entry.getMergeEntry().isRecording() )
				return false ;
		}
		return entries.length > 0 ;
	}
	public static void deleteEntries( final DVBViewerEntry [] entries )
	{
		for ( DVBViewerEntry entry : entries )
		{
			if ( entry.isMergeElement() && entry.isRecording() )
			{
				deleteEntries( entry.mergedEntries.toArray( entries )) ;
				entry.toDo = ToDo.DELETE ;
			}
			if ( entry.isFilterElement && ! entry.isRemoved() )
			{
				entry.toDo = ToDo.DELETE_DVBVIEWER ;
				entry.statusTimer = StatusTimer.REMOVED ;
			}
			else if ( ! entry.isFilterElement )
				entry.toDo = ToDo.DELETE ;
			entry.prepareRemove() ;
		}
	}
	public static boolean isRecoverPossible( final DVBViewerEntry [] entries )
	{

		for ( DVBViewerEntry entry : entries )
		{
			if ( entry.isFilterElement() && entry.isRemoved() )
				return true ;
		}
		return false ;
	}
	public static void recoverEntries( final DVBViewerEntry [] entries )
	{
		for ( DVBViewerEntry entry : entries )
		{
			if ( entry.isFilterElement && entry.isRemoved() )
			{
				if ( entry.toDo == ToDo.DELETE_DVBVIEWER )
					entry.toDo = ToDo.UPDATE ;
				else
					entry.toDo = ToDo.NEW ;
				entry.statusTimer = StatusTimer.ENABLED ;
				entry.setMergeStatus( MergeStatus.DISABLED ) ;
			}
		}
	}
	public static DVBViewerEntry readXML( final XMLEventReader  reader, XMLEvent ev, String name, Map< Long, ChannelSet> channelSets )
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
					StatusTimer statusDVBViewer = null ;
					String channel = null ;
					long channelSetID = -1 ;
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
							statusDVBViewer = StatusTimer.valueOf( value ) ;
						else if ( attributeName.equals( "statusTimer" ) )
							statusDVBViewer = StatusTimer.valueOf( value ) ;
						else if ( attributeName.equals( "channel" ) )
							channel =value ;
						else if ( attributeName.equals( "channelSetID" ) )
							channelSetID = Long.valueOf( value ) ;
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
					ChannelSet channelSet = channelSets.get(channelSetID ) ;
					if ( channelSet != null )
						channel = channelSet.getDVBViewerChannel() ;
			        entry = new DVBViewerEntry( id, isFilterElement, statusDVBViewer,
												-1, providerID, channel, channelSet,
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
			  if ( this.dvbViewerID >= 0 )
				  sw.writeAttribute( "dvbID",         Long.toString( this.dvbViewerID ) ) ;
			  sw.writeAttribute( "isFilterElement",  this.isFilterElement ) ;
			  if( this.statusTimer == StatusTimer.RECORDING )
				  sw.writeAttribute( "statusTimer",    	 StatusTimer.ENABLED.toString() ) ;
			  else
				  sw.writeAttribute( "statusTimer",    	 this.statusTimer.toString() ) ;

			  if ( channelSet != null )
				sw.writeAttribute( "channelSetID", Long.toString( channelSet.getID() ) ) ;
			  else
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
