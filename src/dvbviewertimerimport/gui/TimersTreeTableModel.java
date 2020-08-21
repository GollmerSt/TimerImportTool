package dvbviewertimerimport.gui ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;


import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry.ToDo;
import dvbviewertimerimport.gui.treetable.AbstractTreeTableModel;
import dvbviewertimerimport.gui.treetable.JTreeTable;
import dvbviewertimerimport.gui.treetable.TreeTableModel;
import dvbviewertimerimport.misc.Constants;
import dvbviewertimerimport.misc.ResourceManager;

public class TimersTreeTableModel extends AbstractTreeTableModel implements TreeTableModel {

	private static GregorianCalendar calendarD  = new GregorianCalendar() ;
	private static GregorianCalendar calendarT  = new GregorianCalendar() ;
	private static GregorianCalendar calendarW  = new GregorianCalendar() ;

	// Types of the columns.
	static protected Class<?>[]  cTypes =
	{
		TreeTableModel.class, 						// tree
		String.class,								// Program
		String.class,								// title
		Long.class,									// Start date
		Long.class,									// Start time
		Long.class,									// End time
		String.class,								// provider
		Long.class,									// Original start date
		Long.class,									// Original start time
		Long.class									// Original end time
	} ;
	static protected String[]  cNames =
	{
		ResourceManager.msg( "TIMERS_TREE" ), 				// tree
		ResourceManager.msg( "TIMERS_PROGRAM" ),			// Program
		ResourceManager.msg( "TIMERS_TITLE" ),				// title
		ResourceManager.msg( "TIMERS_START_DATE" ),			// Start date
		ResourceManager.msg( "TIMERS_START_TIME" ),			// Start time
		ResourceManager.msg( "TIMERS_END_TIME" ),			// End time
		ResourceManager.msg( "TIMERS_PROVIDER" ),			// provider
		ResourceManager.msg( "TIMERS_ORIGINAL_START_DATE" ),// Original start date
		ResourceManager.msg( "TIMERS_ORIGINAL_START_TIME" ),// Original start time
		ResourceManager.msg( "TIMERS_ORIGINAL_END_TIME" )	// Original end time
	} ;
	
	private final DVBViewer dvbViewer ;
	private TimersDialog dialog = null ;
	private JTreeTable treeTable = null ;
	private boolean isChanged = false ;
	
	public TimersTreeTableModel( final DVBViewer dvbViewer )
	{
		super( null );
		this.root = new DVBViewerEntry() ;    // Dummy entry for root node
		this. dvbViewer  =  dvbViewer  ;
	}
	
	public void setTreeTable( JTreeTable treeTable ) { this.treeTable = treeTable ; } ;
	public void setTimersDialog( TimersDialog dialog ){ this.dialog = dialog ; } ;
	
	class MyComparator implements Comparator< DVBViewerEntry >
	{
		@Override
		public int compare(DVBViewerEntry arg0, DVBViewerEntry arg1)
		{
			return (int) (arg0.getStart() - arg1.getStart()) ;
		}
	}
	
	public void updateRoot()
	{
		DVBViewerEntry [] list = this.dvbViewer.getRecordEntries().toArray( new DVBViewerEntry[0] ) ;
		
		Arrays.sort( list, new MyComparator() ) ;
		
		ArrayList< DVBViewerEntry > children = new ArrayList< DVBViewerEntry >() ;
		
		long now = System.currentTimeMillis() ;
		
		for ( DVBViewerEntry entry : list )
		{
			if ( ! entry.canDisplayed( now ) )
				continue ;
			
			entry.setRoot( (DVBViewerEntry) this.root ) ;
			//if ( entry.isRemoved() )
			//	continue ;
			if ( entry.isMerged() )
				continue ;
			children.add( entry ) ;
		}
		
		for ( int workIx = 0 ; workIx < children.size() ; workIx++ )
		{
			DVBViewerEntry workEntry = children.get( workIx ) ;
						
			long start = workEntry.getStart() ;
			long end   = workEntry.getEnd() ;
			String channel = workEntry.getChannel() ;
			
			ArrayList< DVBViewerEntry > group = new ArrayList< DVBViewerEntry >() ;
			
			int compIx ;

			for ( compIx = workIx + 1 ; compIx < children.size() ; compIx++ )
			{
				DVBViewerEntry compEntry = children.get( compIx ) ;
				
				if ( compEntry.isInRange( start, end ))
				{
					if ( channel.equals( compEntry.getChannel() ) )
					{
						group.add( compEntry ) ;
						if ( end < compEntry.getEnd() )
							end = compEntry.getEnd() ;
					}						
				}
				else
					break ;
			}
			compIx-- ;
			int toIx = compIx ;
			for ( ; compIx > workIx ; compIx-- )
			{
				DVBViewerEntry compEntry = children.get( compIx ) ;
				if ( ! channel.equals( compEntry.getChannel() ) )
				{
					children.set( toIx, compEntry ) ;
					toIx-- ;
				}
			}
			for ( int gIx = 0 ; gIx < group.size(); gIx++ )
				children.set( workIx + gIx + 1, group.get( gIx ) ) ;
		}

		((DVBViewerEntry)this.root).setMergedEntries( children ) ;
		Object [] path = { this.root } ;
		this.fireTreeStructureChanged( TimersTreeTableModel.this, path, null, null);
		if ( this.treeTable != null )
		{
			for ( DVBViewerEntry entry : getChildren( this.root ) )
			{
				if ( entry.isCollapsed() )
					this.treeTable.getTree().collapsePath( entry.getPath() ) ;
				else
					this.treeTable.getTree().expandPath( entry.getPath() ) ;
			}
		}
	}
	
	public boolean isChanged() { return this.isChanged ; } ;
	public void setIsChanged( boolean isChanged )
	{
		this.isChanged = isChanged;
		this.dialog.updateButtons() ;
	} ;

	@Override
	public int getColumnCount()
	{
		return TimersTreeTableModel.cTypes.length ;
	}

	@Override
	public String getColumnName(int column)
	{
		return cNames[ column ] ;
	}
	@Override
	public Class<?> getColumnClass(int column)
	{
		return TimersTreeTableModel.cTypes[ column ];
	}
	@Override
	public boolean isCellEditable(Object node, int column)
	{
		return column == 0 || ( column >= 3 && column <= 5 && ! ((DVBViewerEntry)node).isMergeElement() ) ;
	}
	@Override
	public Object getValueAt(Object node, int column)
	{
		DVBViewerEntry entry = ( DVBViewerEntry )node ;
		if ( entry.getParent() == null )			// Root
			return null ;
		switch ( column )
		{
			case 0 :
				return new String("") ;
			case 1 :
				return entry.getProgram() ;
			case 2 :
				return entry.getTitle() ;
			case 3 :
				return entry.getStart() ;
			case 4 :
				return entry.getStart() ;
			case 5 :
				return entry.getEnd() ;
			case 6 :
				if ( entry.getProvider() == null )
					return "DVBViewer" ;
				else
					return entry.getProvider().getName() ;
			case 7 :
				return entry.getStartOrg() ;
			case 8 :
				return entry.getStartOrg() ;
			case 9 :
				return entry.getEndOrg() ;
		}
		return null;
	}
	@Override
	public void setValueAt(Object aValue, Object node, int column)
	{
		DVBViewerEntry entry = ( DVBViewerEntry )node ;
		if ( entry.getParent() == null )			// Root
			return ;
		switch (column)
		{
			case 3 :
				TimersTreeTableModel.setStartEnd( this, entry, (Long)aValue, entry.getStart(), entry.getEnd() ) ;
				this.setIsChanged( true ) ;
				break ;
			case 4 :
				TimersTreeTableModel.setStartEnd( this, entry, entry.getStart(), (Long)aValue, entry.getEnd() ) ;
				this.setIsChanged( true ) ;
				break ;
			case 5 :
				TimersTreeTableModel.setStartEnd( this, entry, entry.getStart(), entry.getStart(), (Long)aValue ) ;
				this.setIsChanged( true ) ;
				break ;
		}
		
	}
	private static void setStartEnd( TimersTreeTableModel siht, DVBViewerEntry entry, long date, long start, long end )
	{
		long newStart = TimersTreeTableModel.calcTime( date, start ) ; 
		long newEnd   = TimersTreeTableModel.calcTime( date, end ) ; 
		if ( newStart > newEnd )
			newEnd += Constants.DAYMILLSEC ;
		if ( entry.isMerged() )
		{
			DVBViewerEntry merge = entry.getMergeEntry() ;
			long mergeStart = merge.getStart() ;
			long mergeEnd   = merge.getEnd() ;
			if ( mergeStart > newStart )
				mergeStart = newStart ;
			if ( mergeEnd < newEnd )
				mergeEnd = newEnd ;
			if ( mergeEnd - mergeStart > Constants.DAYMILLSEC )
				return ;
			entry.setStart( newStart ) ;
			entry.setEnd( newEnd ) ;
			merge.calcStartEnd() ;
			Object[] path = merge.getPath().getPath() ;
			// int[]        index = { getIndexOfChild(parent, this) };
	        // Object[]     children = { this };
			siht.fireTreeNodesChanged(siht, path,  null, null );
		}
		else
		{
			entry.setStart( newStart ) ;
			entry.setEnd( newEnd ) ;
		}
		if ( entry.getToDo() != ToDo.NEW )
			entry.setToDo( ToDo.UPDATE ) ;
	}
	private static long calcTime( long dateDate, long timeDate )
	{
		calendarD.clear() ;
		calendarD.setTimeInMillis( dateDate ) ;
		calendarT.clear() ;
		calendarT.setTimeInMillis( timeDate ) ;
		calendarW.clear() ;
		calendarW.set(
				calendarD.get( Calendar.YEAR ),
				calendarD.get( Calendar.MONTH ),
				calendarD.get( Calendar.DAY_OF_MONTH ),
				calendarT.get( Calendar.HOUR_OF_DAY ),
				calendarT.get( Calendar.MINUTE ) ) ;
		return calendarW.getTimeInMillis() ;
	}

	@Override
	public Object getChild(Object parent, int index) {
		ArrayList< DVBViewerEntry > entries = this.getChildren( parent ) ;
		if ( entries == null )
			return null;
		else if ( index < 0 || index >= entries.size() )
			return null ;
		else
			return entries.get( index ) ;
	}
	@Override
	public int getChildCount(Object parent)
	{
		ArrayList< DVBViewerEntry > entries = this.getChildren( parent ) ;
		if ( entries == null )
			return 0 ;
		else
			return entries.size() ;
	}
	private ArrayList< DVBViewerEntry > getChildren( Object parent )
	{
		DVBViewerEntry entry = (DVBViewerEntry) parent ;
		return entry.getMergedEntries() ;
	}
}
