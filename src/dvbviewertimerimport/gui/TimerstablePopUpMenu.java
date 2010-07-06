package dvbviewertimerimport.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;
import dvbviewertimerimport.gui.TimersDialog.MyTreeTable;
import dvbviewertimerimport.gui.treetable.JTreeTable.TreeTableCellRenderer;
import dvbviewertimerimport.misc.ResourceManager;

public class TimerstablePopUpMenu extends JPopupMenu
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6216645223241611598L;
	private final Control control ;
	private final TimerstablePopUpMenu myInstance = this ;
	private final MyTreeTable table ;
	
	private final static Merge merge = new Merge() ;
	private final static Split split = new Split() ;
	private final static Delete delete = new Delete() ;
	
	private final MyActionListener listener = new MyActionListener() ;
	
	class MyActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent evt) {
			JMenuItem item = (JMenuItem) evt.getSource() ;
			
			for ( MenuEnum e : MenuEnum.values() )
			{
				if ( item == e.getItem() )
				{
					e.method.choosedListener( myInstance ) ;
					break ;
				}
			}
		}
	}
	interface Method
	{
		public boolean isEnabled( final MyTreeTable table ) ;
		public void choosedListener( final TimerstablePopUpMenu men ) ;
	}
	static class Merge implements Method
	{
		@Override
		public boolean isEnabled( final MyTreeTable table )
		{
			return DVBViewerEntry.isMergePossible( table.getSelectedEntries() ) ;
		}

		@Override
		public void choosedListener( final TimerstablePopUpMenu menu )
		{
			DVBViewerEntry.mergeEntries( menu.control.getDVBViewer(), menu.table.getSelectedEntries() ) ;
			menu.control.getDVBViewer().reworkMergeElements() ;
			TreeTableCellRenderer jTree = (TreeTableCellRenderer) menu.table.getTree() ;
			TimersTreeTableModel treeModel = (TimersTreeTableModel) jTree.getModel() ; 
			treeModel.updateRoot() ;
		}
	}
	static class Split implements Method
	{
		@Override
		public boolean isEnabled( final MyTreeTable table )
		{
			return DVBViewerEntry.isSplittingPossible( table.getSelectedEntries() ) ;
		}

		@Override
		public void choosedListener( final TimerstablePopUpMenu menu )
		{
			DVBViewerEntry.splitEntries( menu.control.getDVBViewer(), menu.table.getSelectedEntries() ) ;
			menu.control.getDVBViewer().reworkMergeElements() ;
			TreeTableCellRenderer jTree = (TreeTableCellRenderer) menu.table.getTree() ;
			TimersTreeTableModel treeModel = (TimersTreeTableModel) jTree.getModel() ; 
			treeModel.updateRoot() ;
		}
	}
	static class Delete implements Method
	{
		@Override
		public boolean isEnabled( final MyTreeTable table ) {
			return DVBViewerEntry.isDeletePossible( table.getSelectedEntries() );
		}

		@Override
		public void choosedListener( final TimerstablePopUpMenu menu )
		{
			DVBViewerEntry.deleteEntries( menu.table.getSelectedEntries() ) ;
			menu.control.getDVBViewer().reworkMergeElements() ;
			TreeTableCellRenderer jTree = (TreeTableCellRenderer) menu.table.getTree() ;
			TimersTreeTableModel treeModel = (TimersTreeTableModel) jTree.getModel() ; 
			treeModel.updateRoot() ;
		}
	}

	private enum MenuEnum
	{
		MERGE( ResourceManager.msg( "TIMERS_MERGE" ), merge ),
		SPLIT( ResourceManager.msg( "TIMERS_SPLIT" ), split ),
		SEPARATOR(),
		DELETE( ResourceManager.msg( "TIMERS_DELETE" ), delete ) ;
		
		private final Method method;
		private final JMenuItem menuItem ;
		
		MenuEnum( String mText, final Method method )
		{
			this.menuItem = new JMenuItem( mText ) ;
			this.method = method ;
		}
		MenuEnum()
		{
			this.menuItem = null ;
			this.method = null ;
		}
		public boolean isSeparator() { return this.menuItem == null ; } ;
		public boolean isEnabled( final MyTreeTable table ) { return method == null ? true : method.isEnabled( table ) ; } ;
		public JMenuItem getItem() { return this.menuItem ; } ;
	}
	
	public TimerstablePopUpMenu( final Control control, final MyTreeTable table )
	{
		this.control = control ;
		this.table   = table ;

		for ( MenuEnum e : MenuEnum.values() )
		{
			if ( e.isSeparator() )
		        this.addSeparator() ;
			else
			{
				this.add( e.getItem() ) ;
				e.getItem().addActionListener( listener ) ;
			}
		}
	}
	@Override
	public void show(Component invoker, int x, int y) 
	{
		for ( MenuEnum e : MenuEnum.values() )
		{
			this.getComponent( e.ordinal() ).setEnabled( e.isEnabled( this.table ) ) ;
		}
		super.show(invoker, x, y) ;
	}
	public TimerstablePopUpMenu()
	{
		this.control = null ;
		this.table   = null ;
	}
}
