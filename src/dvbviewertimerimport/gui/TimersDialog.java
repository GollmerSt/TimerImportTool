// $LastChangedDate: 2010-06-18 11:50:16 +0200 (Fr, 18 Jun 2010) $
// $LastChangedRevision: 444 $
// $LastChangedBy: Stefan Gollmer $

package dvbviewertimerimport.gui;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import dvbviewertimerimport.gui.treetable.JTreeTable ; 
import dvbviewertimerimport.misc.ResourceManager;

import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;

public class TimersDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1269656564974135595L;
	
	private final GUI gui ; 
	private final Control control ; 
	private boolean isChanged = false ;
		
	private MyTreeTable recordingTable = null ;
	private TimersTreeTableModel treeTableModel = null ;
	
	private TimerstablePopUpMenu tablePopUp = null ;
	
	public TimersDialog( Control control )
	{
		this( null, control ) ;
	}

	
	public TimersDialog( GUI gui, Control control )
	{
		super( gui , java.awt.Dialog.ModalityType.APPLICATION_MODAL  ) ;
		this.gui = gui ;
		this.control = control ;
		//this.setLayout( new BorderLayout() )  ;
		this.setLayout( new GridBagLayout() )  ;
	}
	
	
	static class MyTreeTable extends JTreeTable
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5883200778909176606L;
		public MyTreeTable(TimersTreeTableModel treeTableModel) {
			super(treeTableModel);
			treeTableModel.setTree( this.getTree() ) ;
			treeTableModel.updateRoot() ;
			this.getTree().addTreeExpansionListener( new ExpandListener() ) ;
		}
		public DVBViewerEntry [] getSelectedEntries()
		{
			JTree tree = this.getTree();
			int count = tree.getSelectionCount() ;
			DVBViewerEntry [] result = new DVBViewerEntry [ count ] ;
			
			if ( count == 0 )
				return result ;

			int ix = 0 ;
						
			for ( TreePath p : tree.getSelectionPaths() )
			{
				result[ ix++ ] = (DVBViewerEntry) p.getLastPathComponent() ;
			}
			return result;
		}
		class ExpandListener implements TreeExpansionListener
		{

			@Override
			public void treeCollapsed(TreeExpansionEvent evt)
			{
				TreePath treepath = evt.getPath() ;
				DVBViewerEntry entry = (DVBViewerEntry) treepath.getLastPathComponent() ;
				entry.setIsCollapsed( true ) ;
			}

			@Override
			public void treeExpanded(TreeExpansionEvent evt)
			{
				TreePath treepath = evt.getPath() ;
				DVBViewerEntry entry = (DVBViewerEntry) treepath.getLastPathComponent() ;
				entry.setIsCollapsed( false ) ;
			}
		}
	}
	
	void init()
	{
		if ( this.control.getDVBViewer().getRecordEntries() == null )
			this.control.getDVBViewer().updateDVBViewer() ;
		
		this.treeTableModel = new TimersTreeTableModel( this.control.getDVBViewer() ) ;
		
		this.recordingTable = new MyTreeTable( this.treeTableModel ) ;
		
		JTree tree = this.recordingTable.getTree() ;
		tree.setRootVisible( false ) ;
		ImageIcon leafIcon = ResourceManager.createImageIcon( "icons/dvbViewer Timer14.png", "Timer icon" ) ;

	    DefaultTreeCellRenderer renderer = 
	    	new DefaultTreeCellRenderer();
	        renderer.setLeafIcon(leafIcon);
	        tree.setCellRenderer(renderer);

		this.recordingTable.addMouseListener( new MyTableMouseListener() ) ;

	    this.setTitle( ResourceManager.msg( "TIMER_TABLE" ) ) ;
		
		Insets i = new Insets( 5, 5, 5, 5 );
		GridBagConstraints c = null ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.weightx    = 1.0 ;
		c.insets     = i ;
		
		this.recordingTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		this.recordingTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 50 ) ;
		this.recordingTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 100 ) ;
		this.recordingTable.getColumnModel().getColumn( 2 ).setPreferredWidth( 50 ) ;
		this.recordingTable.getColumnModel().getColumn( 3 ).setPreferredWidth( 50 ) ;
		this.recordingTable.getColumnModel().getColumn( 4 ).setPreferredWidth( 50 ) ;
		this.recordingTable.getColumnModel().getColumn( 5 ).setPreferredWidth( 180 ) ;
		this.recordingTable.getColumnModel().getColumn( 6 ).setPreferredWidth( 80 ) ;
		this.recordingTable.getColumnModel().getColumn( 7 ).setPreferredWidth( 100 ) ;
		this.recordingTable.getColumnModel().getColumn( 8 ).setPreferredWidth( 100 ) ;
		this.recordingTable.getColumnModel().getColumn( 9 ).setPreferredWidth( 100 ) ;

		
		JScrollPane scrollPane = new JScrollPane( this.recordingTable );
		scrollPane.setPreferredSize( new Dimension( 150+150*5, 230 ) ) ;
		scrollPane.setMinimumSize( new Dimension( 150+150*2, 230 ) ) ;
		this.recordingTable.setFillsViewportHeight(false);
		//this.getContentPane().add( scrollPane, BorderLayout.CENTER ) ;
		this.getContentPane().add( scrollPane, c ) ;

		this.tablePopUp = new TimerstablePopUpMenu ( this.control, this.recordingTable ) ;

		this.pack(); 
        this.setLocationRelativeTo(null);
		this.setVisible( true );
	}
	
	class MyTableMouseListener implements MouseListener
	{
		boolean rightButtonPressed = false ;
		@Override
		public void mouseClicked(MouseEvent evt)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent evt) {
			mayShowTablePopUp( evt ) ;
		}

		@Override
		public void mouseReleased(MouseEvent evt) {
			mayShowTablePopUp( evt ) ;
		}
		private void mayShowTablePopUp( MouseEvent evt )
		{
            if (evt.getButton() == MouseEvent.BUTTON3 && evt.isPopupTrigger())
            {
                int row = recordingTable.rowAtPoint( evt.getPoint() );
                if ( !recordingTable.isRowSelected( row ))
                	recordingTable.changeSelection( row, 0, false, false );
                this.rightButtonPressed = true ;
                tablePopUp.show(evt.getComponent(), evt.getX(), evt.getY());
                
            }
		}
	}
}
