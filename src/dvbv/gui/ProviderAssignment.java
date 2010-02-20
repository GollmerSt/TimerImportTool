package dvbv.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import dvbv.control.Channel;
import dvbv.control.ChannelSet;
import dvbv.control.Control;
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
			if ( o1.length() == 0 || o2.length() == 0 )
			{
				if ( o1.length() == 0 && o1.length() == 0 )
					return 0 ;
				else if ( o1.length() == 0 )
					return 1 ;
				else
					return -1 ;
			}
			if ( o1.equalsIgnoreCase(o2) )
				return o1.compareTo( o2 ) ;
			return o1.compareToIgnoreCase(o2) ;
		}
	}
	public ProviderAssignment( Control control )
	{
		super( control ) ;
		
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
	    
	    sorter.setComparator(  1, new MyComparator() ) ;
	    sorter.setComparator(  2, new MyComparator() ) ;
	    
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
			column = this.table.getColumnModel().getColumn( pid + 1 ) ;
			column.setPreferredWidth( 150 ) ;
			column.setCellEditor(new DefaultCellEditor(comboBox) ) ;
		}
		updateTableComboBoxes() ;
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
			comboBox.removeAllItems() ;
		    for ( Iterator< String > itCS = this.providerMaps.get( pid ).keySet().iterator() ; itCS.hasNext() ; )
		    {
		    	comboBox.addItem( itCS.next() ) ;
		    }
		}
	}
	public class MyTableModel extends AbstractTableModel
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
					return channel.getName() ;
				else
					return "" ;
			}
			else
			{
				if ( col == 0 )
					return inactive ;
				else
					return "" ;
			}
		}
	}
	@Override
	public void update()
	{
		this.unlockBox.setSelected( true ) ;
		this.unlockBox.doClick() ;
	}
}
