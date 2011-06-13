// $LastChangedDate: 2010-06-18 11:50:16 +0200 (Fr, 18 Jun 2010) $
// $LastChangedRevision: 444 $
// $LastChangedBy: Stefan Gollmer $

package dvbviewertimerimport.gui;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import dvbviewertimerimport.gui.treetable.JTreeTable.TreeTableCellRenderer;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.provider.Provider;

import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;

public class TimersDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1269656564974135595L;


	private final Control control ; 
		
	private MyTreeTable recordingTable = null ;
	private TimersTreeTableModel treeTableModel = null ;
		
    private final JButton reloadButton = new JButton() ;
    private final JButton okButton = new JButton() ;
    private final JButton cancelButton = new JButton() ;
    private final JButton applyButton = new JButton() ;
		
	private TimerstablePopUpMenu tablePopUp = null ;
	private static ImageIcon programIcon   = ResourceManager.createImageIcon( "icons/dvbViewer Programm16.png", "DVBViewer icon" ) ;
	
	public TimersDialog( GUI gui, Control control )
	{
		super( gui , java.awt.Dialog.ModalityType.APPLICATION_MODAL  ) ;
		this.control = control ;
		//this.setLayout( new BorderLayout() )  ;
		this.setLayout( new GridBagLayout() )  ;
		this.setIconImage( TimersDialog.programIcon.getImage() ) ;
	}
	
	
	static class MyTreeTable extends JTreeTable
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5883200778909176606L;
		public MyTreeTable(TimersTreeTableModel treeTableModel) {
			super(treeTableModel);
			treeTableModel.setTreeTable( this ) ;
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
		static class MyTreeRenderer extends DefaultTreeCellRenderer
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 2282592846369354672L;
			
			private ImageIcon activeTimerIcon   = ResourceManager.createImageIcon("icons/dvbViewer Timer14.png", "Timer icon" ) ;
			private ImageIcon disabledTimerIcon = ResourceManager.createImageIcon("icons/dvbViewer TimerDisabled 14.png", "Timer icon deleted" ) ;
			private ImageIcon filterTimerIcon   = ResourceManager.createImageIcon("icons/dvbViewer TimerFilter 14.png", "Filter icon" ) ;
			private ImageIcon recordingIcon     = ResourceManager.createImageIcon("icons/dvbViewer Recording 14.png", "Recording icon" ) ;
			public MyTreeRenderer()
			{
				super() ;
				
				this.setLeafIcon( activeTimerIcon ) ;
				
				
			}
		    public Component getTreeCellRendererComponent(
                    JTree tree,
                    Object value,
                    boolean sel,
                    boolean expanded,
                    boolean leaf,
                    int row,
                    boolean hasFocus)
		    {
		    	super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);
		    	DVBViewerEntry entry = (DVBViewerEntry)value ;
		    	setText("") ;
		    	if ( ! leaf )
		    	{
		    		if ( entry.isRecording())
			    		setIcon( recordingIcon );
		    		return this ;
		    	}
		    	if (entry.isRemoved() && ! entry.isMerged() )
		    		setIcon(filterTimerIcon) ;
		    	else if ( entry.isDisabled() )
		    	{
		    		if ( entry.isMerged() )
		    		{
		    			if ( entry.getMergeEntry().isDisabled() )
		    				setIcon( disabledTimerIcon );
		    		}
		    		else 
	    				setIcon( disabledTimerIcon );
		    	}
		    	else if ( entry.isRecording() )
		    		setIcon( recordingIcon );
			    return this;
		    } 
		}
	}
	
	public void init()
	{
		if ( this.control.getDVBViewer().getRecordEntries() == null )
			this.control.getDVBViewer().updateDVBViewer( true ) ;
		
		this.treeTableModel = new TimersTreeTableModel( this.control.getDVBViewer() ) ;
		this.treeTableModel.setTimersDialog( this ) ;
		
		this.recordingTable = new MyTreeTable( this.treeTableModel ) ;
		
		JTree tree = this.recordingTable.getTree() ;
		tree.setRootVisible( false ) ;

		MyTreeTable.MyTreeRenderer renderer = 
	    	new MyTreeTable.MyTreeRenderer();
	        tree.setCellRenderer(renderer);

		this.recordingTable.addMouseListener( new MyTableMouseListener() ) ;

	    this.setTitle( ResourceManager.msg( "TIMER_TABLE" ) ) ;
		
		Insets i = new Insets( 5, 5, 5, 5 );
		GridBagConstraints c = null ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.BOTH ;
		c.weightx    = 1.0 ;
		c.weighty    = 1.0 ;
		c.insets     = i ;
		
		this.setupTable() ;
		
		JScrollPane scrollPane = new JScrollPane( this.recordingTable );
		scrollPane.setPreferredSize( new Dimension( 150+150*5, 230 ) ) ;
		scrollPane.setMinimumSize( new Dimension( 150+150*2, 230 ) ) ;
		this.recordingTable.setFillsViewportHeight(false);
		//this.getContentPane().add( scrollPane, BorderLayout.CENTER ) ;
		this.getContentPane().add( scrollPane, c ) ;

		this.tablePopUp = new TimerstablePopUpMenu ( this.control, this.recordingTable ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		//c.fill       = GridBagConstraints.BOTH ;
		//c.weightx    = 1.0 ;
		//c.weighty    = 1.0 ;
		c.insets     = i ;
		
		this.reloadButton.addActionListener( new ButtonsPressed() ) ;
		this.reloadButton.setText( ResourceManager.msg( "RELOAD" ) ) ;
		this.add( this.reloadButton, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		//c.fill       = GridBagConstraints.BOTH ;
		c.weightx    = 1.0 ;
		//c.weighty    = 1.0 ;
		c.insets     = i ;
		
		this.okButton.addActionListener( new ButtonsPressed() ) ;
		this.okButton.setText( ResourceManager.msg( "OK" ) ) ;
		this.add( this.okButton, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		//c.fill       = GridBagConstraints.BOTH ;
		//c.weightx    = 1.0 ;
		//c.weighty    = 1.0 ;
		c.insets     = i ;
		
		this.cancelButton.addActionListener( new ButtonsPressed() ) ;
		this.cancelButton.setText( ResourceManager.msg( "CANCEL" ) ) ;
		this.add( this.cancelButton, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		//c.fill       = GridBagConstraints.BOTH ;
		//c.weightx    = 1.0 ;
		//c.weighty    = 1.0 ;
		c.insets     = i ;
		
		this.applyButton.addActionListener( new ButtonsPressed() ) ;
		this.applyButton.setText( ResourceManager.msg( "APPLY" ) ) ;
		this.add( this.applyButton, c ) ;
		
		this.updateButtons() ;
		
		this.pack(); 
        this.setLocationRelativeTo(null);
		this.setVisible( true );
	}

	private void setupTable()
	{
		this.recordingTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		this.recordingTable.setRowHeight(21) ;
		
		TableColumn column = null ;
		column = this.recordingTable.getColumnModel().getColumn( 0 ) ;
		column.setPreferredWidth( 50 ) ;
		
		column = this.recordingTable.getColumnModel().getColumn( 1 ) ;
		column.setPreferredWidth( 100 ) ;
		
		column = this.recordingTable.getColumnModel().getColumn( 2 ) ;
		column.setPreferredWidth( 180 ) ;
		
		column = this.recordingTable.getColumnModel().getColumn( 3 ) ;
		column.setCellRenderer( new TimeGUIHelper.DateRenderer() ) ;
		column.setCellEditor( new TimeGUIHelper.DateEditor() ) ;
		column.setPreferredWidth( 80 ) ;
		
		column = this.recordingTable.getColumnModel().getColumn( 4 ) ;
		column.setCellRenderer( new TimeGUIHelper.DayTimeRenderer() ) ;
		column.setCellEditor( new TimeGUIHelper.DayTimeEditor() ) ;
		column.setPreferredWidth( 60 ) ;
		
		column = this.recordingTable.getColumnModel().getColumn( 5 ) ;
		column.setCellRenderer( new TimeGUIHelper.DayTimeRenderer() ) ;
		column.setCellEditor( new TimeGUIHelper.DayTimeEditor() ) ;
		column.setPreferredWidth( 60 ) ;
		
		this.recordingTable.getColumnModel().getColumn( 6 ).setPreferredWidth( 80 ) ;
		
		column = this.recordingTable.getColumnModel().getColumn( 7 ) ;
		column.setCellRenderer( new TimeGUIHelper.DateRenderer() ) ;
		column.setPreferredWidth( 95 ) ;
		
		column = this.recordingTable.getColumnModel().getColumn( 8 ) ;
		column.setCellRenderer( new TimeGUIHelper.DayTimeRenderer() ) ;
		column.setPreferredWidth( 85 ) ;
		
		column = this.recordingTable.getColumnModel().getColumn( 9 ) ;
		column.setCellRenderer( new TimeGUIHelper.DayTimeRenderer() ) ;
		column.setPreferredWidth( 85 ) ;
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
	private class ButtonsPressed implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e) {
			recordingTable.editingStopped( null ) ;
			JButton source = (JButton)e.getSource() ;
			
			TreeTableCellRenderer jTree = (TreeTableCellRenderer) recordingTable.getTree() ;
			TimersTreeTableModel treeModel = (TimersTreeTableModel) jTree.getModel() ; 

			if ( source == okButton )
			{
				if ( treeModel.isChanged() )
				{
					try {
						control.getDVBViewer().setDVBViewerTimers() ;
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					control.getDVBViewer().updateDVBViewer( false ) ;
					control.getDVBViewer().writeXML() ;
					Provider.updateRecordingsAllProviders( control.getDVBViewer().getRecordEntries() ) ;
					treeModel.setIsChanged( false ) ;
				}
				dispose() ;
			}
			else if ( source == cancelButton )
			{
				control.getDVBViewer().resetRecordEntries() ;
				treeModel.setIsChanged( false ) ;
				dispose() ;
			}
			else if ( source == applyButton )
			{
				recordingTable.editingStopped( null ) ;
				if ( treeModel.isChanged() )
				{
					try {
						control.getDVBViewer().setDVBViewerTimers() ;
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					control.getDVBViewer().updateDVBViewer( false ) ;
					control.getDVBViewer().writeXML() ;
					Provider.updateRecordingsAllProviders( control.getDVBViewer().getRecordEntries() ) ;
					treeModel.setIsChanged( false ) ;
				}
			}
			else if ( source == reloadButton )
			{
				control.getDVBViewer().updateDVBViewer( true) ;
				Provider.updateRecordingsAllProviders( control.getDVBViewer().getRecordEntries() ) ;
				treeModel.setIsChanged( false ) ;
				treeModel.updateRoot() ;
			}
		}
	}
	public void updateButtons()
	{
		if ( this.treeTableModel == null )
			return ;
		
		this.applyButton.setEnabled( this.treeTableModel.isChanged() ) ;
	}
}
