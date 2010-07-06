package dvbviewertimerimport.gui ;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.swing.JTree;

import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;
import dvbviewertimerimport.gui.treetable.AbstractTreeTableModel;
import dvbviewertimerimport.gui.treetable.TreeTableModel;
import dvbviewertimerimport.misc.ResourceManager;

public class TimersTreeTableModel extends AbstractTreeTableModel implements TreeTableModel {

	private static SimpleDateFormat dayFormat = null ;
	private static SimpleDateFormat timeFormat = null ;

	// Types of the columns.
	@SuppressWarnings("unchecked")
	static protected Class[]  cTypes =
	{
		TreeTableModel.class, 						// tree
		String.class,								// Program
		String.class,									// Start date
		String.class,									// Start time
		String.class,									// End time
		String.class,									// title
		String.class,									// provider
		String.class,									// Original start date
		String.class,									// Original start time
		String.class									// Original end time
	} ;
	static protected String[]  cNames =
	{
		ResourceManager.msg( "TIMERS_TREE" ), 				// tree
		ResourceManager.msg( "TIMERS_PROGRAM" ),			// Program
		ResourceManager.msg( "TIMERS_START_DATE" ),			// Start date
		ResourceManager.msg( "TIMERS_START_TIME" ),			// Start time
		ResourceManager.msg( "TIMERS_END_TIME" ),			// End time
		ResourceManager.msg( "TIMERS_TITLE" ),				// title
		ResourceManager.msg( "TIMERS_PROVIDER" ),			// provider
		ResourceManager.msg( "TIMERS_ORIGINAL_START_DATE" ),// Original start date
		ResourceManager.msg( "TIMERS_ORIGINAL_START_TIME" ),// Original start time
		ResourceManager.msg( "TIMERS_ORIGINAL_END_TIME" )	// Original end time
	} ;
	
	static
	{
		dayFormat =  new SimpleDateFormat("dd.MM");
		timeFormat =  new SimpleDateFormat("HH:mm");
	}
	
	private final DVBViewer dvbViewer ;
	private JTree tree = null ;
	
	public TimersTreeTableModel( final DVBViewer dvbViewer )
	{
		super( null );
		this.root = new DVBViewerEntry() ;    // Dummy entry for root node
		this. dvbViewer  =  dvbViewer  ;
	}
	
	public void setTree( JTree tree ) { this.tree = tree ; } ;
	
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
		
		for ( DVBViewerEntry entry : list )
		{
			if ( entry.isDeleted() )
				continue ;
			entry.setRoot( (DVBViewerEntry) this.root ) ;
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
		if ( tree != null )
		{
			for ( DVBViewerEntry entry : getChildren( this.root ) )
			{
				if ( entry.isCollapsed() )
					tree.collapsePath( entry.getPath() ) ;
				else
					tree.expandPath( entry.getPath() ) ;
			}
		}
	}

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
	@SuppressWarnings("unchecked")
	@Override
	public Class getColumnClass(int column)
	{
		return TimersTreeTableModel.cTypes[ column ];
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
				return dayFormat.format( new Date( entry.getStart() ) ) ;
			case 3 :
				return timeFormat.format( new Date( entry.getStart() ) ) ;
			case 4 :
				return timeFormat.format( new Date( entry.getEnd() ) ) ;
			case 5 :
				return entry.getTitle() ;
			case 6 :
				if ( entry.getProvider() == null )
					return "DVBViewer" ;
				else
					return entry.getProvider().getName() ;
			case 7 :
				return dayFormat.format( new Date( entry.getStartOrg() ) ) ;
			case 8 :
				return timeFormat.format( new Date( entry.getStartOrg() ) ) ;
			case 9 :
				return timeFormat.format( new Date( entry.getEndOrg() ) ) ;
		}
		return null;
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
