// $LastChangedDate: 2010-02-02 20:15:15 +0100 (Di, 02. Feb 2010) $
// $LastChangedRevision: 79 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import dvbv.control.Channel;
import dvbv.control.ChannelSet;
import dvbv.control.Control;
import dvbv.misc.Constants;
import dvbv.provider.Provider;

public class ProviderAssignment  extends MyTabPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -646786132995736132L;
	
	private final ArrayList< TreeMap< String, Integer > > providerMaps = new ArrayList< TreeMap< String, Integer > >();
	private String lastSelectedChannel = null ;
	private final String[] columnNames ;
	
	private final JCheckBox unlockBox = new JCheckBox() ;
	private final JComboBox providerCombo = new JComboBox() ;
	private final JComboBox channelCombo = new JComboBox() ;
	private final JButton addChannelButton = new JButton() ;
	private final JButton modifyChannelButton = new JButton() ;
	private final JButton deleteChannelButton = new JButton() ;
	private final JTable table ;
    private TableRowSorter<TableModel> sorter = null ;

	private final JLabel messageLabel = new JLabel() ;

	public class LockBoxChanged implements ActionListener
	{
	    public void actionPerformed(ActionEvent e)
	    {
			messageLabel.setText( "") ;
			boolean enable = unlockBox.isSelected() ;
			providerCombo.setEnabled( enable ) ;
			channelCombo.setEnabled( enable ) ;
			channelCombo.setEditable( enable ) ;
			addChannelButton.setEnabled( enable ) ;
			modifyChannelButton.setEnabled( enable ) ;
			deleteChannelButton.setEnabled( enable ) ;
			updateTable() ;
			table.setEnabled( enable ) ;
			
	    }
	}
	public class ProviderSelected implements ActionListener
	{
	    public void actionPerformed(ActionEvent e)
	    {
			messageLabel.setText( "") ;
	    	updateChannelComboBox() ;
	    }
	}
	public class ChannelSelected implements ActionListener
	{
	    public void actionPerformed(ActionEvent e)
	    {
			messageLabel.setText( "") ;
			
			int ix = channelCombo.getSelectedIndex() ;
			
			if ( ix >= 0 )
			{
				lastSelectedChannel = (String) channelCombo.getItemAt( ix ) ;
				Provider p = (Provider) providerCombo.getSelectedItem() ;
				if ( p == null )
					return ;
				if ( ix > 0 )
				{
					int ics = providerMaps.get( p.getID() ).get( lastSelectedChannel ) ;
					if ( ics >= 0 )
					{
						int tableLine = sorter.convertRowIndexToView( ics ) ;
						table.setRowSelectionInterval( tableLine, tableLine ) ;
						table.scrollRectToVisible(table.getCellRect(tableLine,0,true));
					}
				}
			}
	    }
	}
	public class ChannelButtonsPressed implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			messageLabel.setText( "") ;

			Provider p = (Provider) providerCombo.getSelectedItem() ;
			if ( p == null )
				return ;
			
			TreeMap< String, Integer > map = providerMaps.get( p.getID() ) ;
			
			JButton button = (JButton) e.getSource() ;
			
			String channel = (String) channelCombo.getEditor().getItem() ;

			if ( button == addChannelButton )
			{
				if ( ! map.containsKey( channel ) )
				{
					map.put( channel, -1 ) ;
					updateChannelComboBox() ;
					updateTableComboBoxes() ;	
				}
			}
			else if ( button == deleteChannelButton )
			{
				if ( map.containsKey( channel ) )
				{
					if ( map.get( channel ) < 0 )
					{
						map.remove( channel ) ;
						updateChannelComboBox() ;
						updateTableComboBoxes() ;	
					}
					else
						messageLabel.setText( GUIStrings.cannotDeleted() ) ;
				}
			}
			else if ( button == modifyChannelButton )
			{
				if (   lastSelectedChannel != null
					 && lastSelectedChannel.length() != 0 )
				{
					if ( ! map.containsKey( channel ) )
					{
						int ix = map.get( lastSelectedChannel ) ;
						if ( ix >= 0 )
						{
							ChannelSet cs = control.getChannelSets().get( ix ) ;
							cs.remove( p.getID() ) ;
							cs.add( p.getID(), channel ) ;
							map.remove( lastSelectedChannel ) ;
							map.put( channel, ix ) ;
							updateChannelComboBox() ;
							updateTableComboBoxes() ;
							updateTable() ;
						}
					}
				}
					
			}
		}
	}
	public class MyComparator implements Comparator< String>
	{
		@Override
		public int compare(String o1, String o2) {
			if ( o1.equalsIgnoreCase(o2) )
				return o1.compareTo( o2 ) ;
			return o1.compareToIgnoreCase(o2) ;
		}
	}
	public ProviderAssignment( Control control, JFrame frame )
	{
		super( control, frame ) ;
		
		this.columnNames  = new String[ Provider.getProviders().size() + 1 ];
		this. columnNames[ 0 ] = "" ;
		
		int ip = 0 ;
		for ( Iterator< Provider> itP = Provider.getProviders().iterator() ; itP.hasNext() ; )
		{
			Provider provider = itP.next() ;
			TreeMap< String, Integer > map = new TreeMap< String, Integer >( new MyComparator() ) ;
			for ( int ix = 0 ; ix < control.getChannelSets().size() ; ix++ )
			{
				ChannelSet cs = control.getChannelSets().get( ix ) ;
				for ( Iterator< Channel > itC = cs.getChannels().iterator() ; itC.hasNext() ; )
				{
					Channel c = itC.next() ;
					if ( c.getType() != provider.getID() )
						continue ;
					map.put( c.getName(), ix ) ;
				}
			}
			for ( Iterator< String > it = provider.getChannels().iterator() ; it.hasNext() ; )
			{
				String channel = it.next() ;
				if ( map.containsKey( channel ) )
					continue ;
				map.put( channel, -1 ) ;
			}
			this.providerMaps.add( map ) ;
						
			//this. columnNames[ 0 ] = "" ;
			
			this.columnNames[ ip++ + 1 ] = provider.getName() ;			
		}
		this.table = new JTable( new MyTableModel() );

	}
	public void paint()
	{
		Provider defaultProvider = Provider.getProvider( control.getDefaultProvider() ) ;
		Insets i = new Insets( 5, 5, 5, 5 );
		GridBagConstraints c = null ;

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.unlockBox.addActionListener( new LockBoxChanged() ) ;
		unlockBox.setText( GUIStrings.unlock() ) ;
		this.add( this.unlockBox, c ) ;

		

		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		for ( Iterator< Provider > it = Provider.getProviders().iterator() ; it.hasNext() ; )
		{
			this.providerCombo.addItem( it.next() ) ;
		}
		this.providerCombo.addActionListener( new ProviderSelected() ) ;
		this.providerCombo.setSelectedItem( defaultProvider ) ;
		this.add( this.providerCombo, c ) ;

		

		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.channelCombo.addActionListener( new ChannelSelected() ) ;
		this.channelCombo.setEditable( true ) ;
		this.add( this.channelCombo, c ) ;

		

		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.addChannelButton.addActionListener( new ChannelButtonsPressed() ) ;
		addChannelButton.setText( GUIStrings.add() ) ;
		this.add( this.addChannelButton, c ) ;

		

		c = new GridBagConstraints();
		c.gridx      = 4 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.modifyChannelButton.addActionListener( new ChannelButtonsPressed() ) ;
		modifyChannelButton.setText( GUIStrings.modify() ) ;
		this.add( this.modifyChannelButton, c ) ;

		

		c = new GridBagConstraints();
		c.gridx      = 5 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.deleteChannelButton.addActionListener( new ChannelButtonsPressed() ) ;
		deleteChannelButton.setText( GUIStrings.delete() ) ;
		this.add( this.deleteChannelButton, c ) ;

		

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 2 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		//this.deleteChannelButton.addActionListener( new ChannelButtonsPressed() ) ;
		JScrollPane scrollPane = new JScrollPane( this.table );
		scrollPane.setPreferredSize( new Dimension( 16+150*Provider.getProviders().size(),200 ) ) ;
		scrollPane.setMinimumSize( new Dimension( 16+150*Provider.getProviders().size(),200 ) ) ;
		this.table.setFillsViewportHeight(false);
		this.setupTable() ;
		this.add( scrollPane, c ) ;

		
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 4 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		messageLabel.setForeground( SystemColor.RED ) ;
		messageLabel.setPreferredSize( new Dimension( 200,16 ) ) ;
		this.add( this.messageLabel, c ) ;
}
	private void updateChannelComboBox()
	{
		Provider provider = (Provider)this.providerCombo.getSelectedItem() ;
		int pid = provider.getID() ;
		this.channelCombo.removeAllItems() ;
		this.channelCombo.addItem( "" ) ;
		for ( Iterator< String > it = this.providerMaps.get( pid).keySet().iterator() ; it.hasNext() ; )
			this.channelCombo.addItem( it.next() ) ;
		this.channelCombo.setSelectedIndex( 0 ) ;
	}
	private void setupTable()
	{
	    this.sorter = new TableRowSorter<TableModel>(table.getModel());
	    
	    sorter.setComparator(  1, new MyTableComparator() ) ;
	    sorter.setComparator(  2, new MyTableComparator() ) ;
	    
	    table.setRowSorter(sorter);
	    
	    table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ) ;

		
		TableColumn column = null ;
		
		this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		column = this.table.getColumnModel().getColumn( 0 ) ;
		column.setPreferredWidth( 16 );
		for ( Iterator< Provider > itP = Provider.getProviders().iterator() ; itP.hasNext() ; )
		{
			Provider p = itP.next() ;
			int pid = p.getID() ;
		    JComboBox comboBox = new JComboBox();
		    comboBox.addPopupMenuListener( new PopupCellChanged() ) ;
			column = this.table.getColumnModel().getColumn( pid + 1 ) ;
			column.setPreferredWidth( 150 ) ;
			DefaultCellEditor cellEditor = new DefaultCellEditor(comboBox)
			{
				/**
				 * 
				 */
				private static final long serialVersionUID = -5273432664600113545L;

				
				@Override
		        public Component getTableCellEditorComponent(
		              JTable table,
		              Object value,
		              boolean isSelected,
		              int row,
		              int col)
		        {
					String channel = value.toString() ;
					((JComboBox)getComponent()).setSelectedItem( channel ) ;
					return super.getTableCellEditorComponent(table, value,
		                isSelected, row, col);
		        }
			} ;
			column.setCellEditor( cellEditor ) ;
		}
		updateTableComboBoxes() ;
	}
	private void cancelCellEditing()
	{
		for ( Iterator< Provider > itP = Provider.getProviders().iterator() ; itP.hasNext() ; )
		{
			int pid = itP.next().getID() ;
			TableColumn column = this.table.getColumnModel().getColumn( pid + 1 ) ;
			DefaultCellEditor editor = (DefaultCellEditor) column.getCellEditor() ;
			if ( editor == null )
				return ;
			editor.cancelCellEditing() ;
		}
	}
	
	private void updateTable()
	{
		((MyTableModel)this.table.getModel()).fireTableDataChanged() ;
	}
	private void updateTableComboBoxes()
	{
		for ( Iterator< Provider > itP = Provider.getProviders().iterator() ; itP.hasNext() ; )
		{
			Provider p = itP.next() ;
			int pid = p.getID() ;
			TableColumn column = this.table.getColumnModel().getColumn( pid + 1 ) ;
			DefaultCellEditor editor = (DefaultCellEditor) column.getCellEditor() ;
			JComboBox comboBox = (JComboBox) editor.getComponent() ;
			comboBox.hidePopup() ;
			comboBox.removeAllItems() ;
			comboBox.addItem( "" ) ;
		    for ( Iterator< String > itCS = this.providerMaps.get( pid ).keySet().iterator() ; itCS.hasNext() ; )
		    {
		    	comboBox.addItem( itCS.next() ) ;
		    }
		}
	}
	private void showTableLine( int line )
	{
        int tableLine = sorter.convertRowIndexToView( line ) ;
		table.setRowSelectionInterval( tableLine, tableLine ) ;
		table.scrollRectToVisible(table.getCellRect(tableLine,0,true));
	}
	
	public class PopupCellChanged implements PopupMenuListener
	{

		@Override
		public void popupMenuCanceled(PopupMenuEvent arg0)
		{
			messageLabel.setText( "") ;
		}
		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0)
		{
			cancelCellEditing() ;
			messageLabel.setText( "") ;
		}

		//@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e)
		{
			messageLabel.setText( "") ;
		}
	}

	private class MyTableObject extends Object
	{
		private final String channel ;
		private final boolean isEmpty ;
		private final boolean isNullEntry ;
		MyTableObject( String string, boolean isNullEntry )
		{
			this.channel = string ;
			this.isEmpty = ( string.length() == 0 ) ;
			this.isNullEntry = isNullEntry ;
		}
		@Override
		public String toString() { return channel ; } ;
	}
	public class MyTableComparator implements Comparator< MyTableObject>
	{
		@Override
		public int compare(MyTableObject o1, MyTableObject o2) {
			if ( o1.isNullEntry || o2.isNullEntry )
			{
				if ( o1.isNullEntry )
					return 1 ;
				return -1 ;
			}
			if ( o1.isEmpty || o2.isEmpty )
			{
				if ( o1.isEmpty && o2.isEmpty )
					return 0 ;
				if ( o1.isEmpty )
					return 1 ;
				return -1 ;
			}
			else if ( o1.channel.equalsIgnoreCase( o2.channel ) )
				return o1.channel.compareTo( o2.channel ) ;
			return o1.channel.compareToIgnoreCase(o2.channel) ;
		}
	}
	private class MyTableModel extends AbstractTableModel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -4564280852966603471L;
		private ImageIcon active   = ResourceManager.createImageIcon( "Icons/dvbViewer16.png", "DVBViewer icon" ) ;
	    private ImageIcon inactive   = ResourceManager.createImageIcon( "Icons/dvbViewerEmpty16.png", "DVBViewer empty icon" ) ;
	    @Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return Provider.getProviders().size() + 1 ;
		}
	    public String getColumnName(int col) {
	        return columnNames[col];
	    }
		@SuppressWarnings("unchecked")
		@Override
	    public Class getColumnClass(int col)
		{
			if ( col == 0 )
				return ImageIcon.class ;
			return Object.class ;
		}
		@Override
		public boolean isCellEditable(int row, int col)
		{
			return ( col != 0 ) ;
		}
		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return control.getChannelSets().size()+1;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if ( row < control.getChannelSets().size() )
			{
				ChannelSet cs = control.getChannelSets().get(row) ;
				if ( col == 0)
				{
					if ( cs.getDVBViewerChannel() != null )
						return active ;
					else
						return inactive ;
				}
				Channel channel = cs.getChannel( col - 1 ) ;
				if ( channel != null )
					return new MyTableObject( channel.getName(), false ) ;
				else
					return new MyTableObject( "", false ) ;
			}
			else
			{
				if ( col == 0 )
					return inactive ;
				else
					return new MyTableObject( "", true ) ;
			}
		}
		@Override
		public void setValueAt(Object value, int row, int col)
		{
			messageLabel.setText( "") ;
			cancelCellEditing() ;
			
			String channel = (String) value ;
			
            int csid = row ;
            int pid = col - 1 ;

            TreeMap< String, Integer> channelMap = providerMaps.get( pid ) ;
            ChannelSet cs ;
            if ( csid < control.getChannelSets().size() )
            	cs = control.getChannelSets().get( csid ) ;
            else
            {
            	cs = new ChannelSet() ;
            	control.getChannelSets().add( cs ) ;
            }
            if ( channel.length() > 0 )
            {
                int pcsid = channelMap.get( channel ) ;
                if ( pcsid >= 0 && csid != pcsid )
                {
                	showTableLine( pcsid ) ;
    				int answer = JOptionPane.showConfirmDialog(
    						        frame, 
    						        "\"" + channel + "\" " + GUIStrings.assignedError(), 
    						        Constants.PROGRAM_NAME, 
    						        JOptionPane.OK_CANCEL_OPTION );
    				if ( answer == JOptionPane.CANCEL_OPTION )
    				{
                    	showTableLine( csid ) ;
    					return ;
    				}
                	control.getChannelSets().get( pcsid ).remove( pid ) ;
                	channelMap.put( channel, -1 ) ;
                	boolean isDeleted = removeChannelSetIfEmpty( pcsid ) ;
                	if ( isDeleted && pcsid < csid )
                		csid-- ;
                }
            }
            Channel pc = cs.getChannel( pid ) ;
            if ( pc != null )
            	channelMap.put( pc.getName(), -1 ) ;
            cs.remove( pid ) ;
            
            if ( channel.length() > 0 )
            {
            	cs.add( pid, channel ) ;
            	channelMap.put( channel, csid ) ;
            }
            
            removeChannelSetIfEmpty( csid ) ;
            updateTable() ;
        	showTableLine( csid ) ;
		}
	}
	private boolean removeChannelSetIfEmpty( int csid )
	{
		if ( control.getChannelSets().get( csid ).getChannels().size() != 0 )
			return false ;
		control.getChannelSets().remove( csid ) ;
		for ( Iterator< TreeMap< String, Integer > > itP = this.providerMaps.iterator() ; itP.hasNext() ; )
		{
			for ( Iterator< Map.Entry< String, Integer > > itV = itP.next().entrySet().iterator(); itV.hasNext() ; )
			{
				Map.Entry< String, Integer > entry = itV.next() ;
				int value = entry.getValue() ;
				if ( value == csid )
					entry.setValue( -1 ) ;
				else if ( value > csid )
					entry.setValue( value - 1 ) ;
			}
		}
		return true ;
	}
	@Override
	public void update()
	{
		this.unlockBox.setSelected( true ) ;
		this.unlockBox.doClick() ;
	}
}
