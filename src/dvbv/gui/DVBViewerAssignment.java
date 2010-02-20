// $LastChangedDate: 2010-02-02 20:15:15 +0100 (Di, 02. Feb 2010) $
// $LastChangedRevision: 79 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dvbv.control.ChannelSet;
import dvbv.control.Channel;
import dvbv.provider.Provider;


public class DVBViewerAssignment extends MyTabPanel{
	private static final long serialVersionUID = 124706451716532907L;
	private final GUI parent ;
	private final dvbv.dvbviewer.channels.Channels dvbViewerChannels ;
	private JComboBox providerCombo = new JComboBox() ;
	private JList providerChannelList = new JList( new DefaultListModel() ) ;
	private JComboBox dvbViewerCombo = new JComboBox() ;
	private final JComboBox mergeCombo = new JComboBox() ;
	private TreeMap< String, Integer > dvbViewerChannelAssignment = new TreeMap< String, Integer >( new MyComparator() );
	private boolean ignoreNextDVBViewerChannelChange = false ;

	public class MyComparator implements Comparator< String>
	{
		@Override
		public int compare(String o1, String o2) {
			if ( o1.equalsIgnoreCase(o2) )
				return o1.compareTo( o2 ) ;
			return o1.compareToIgnoreCase(o2) ;
		}
	}
	private class ChannelSetAssignment
	{
		private final ChannelSet channelSet ;
		private final String name ;

		public ChannelSetAssignment( String name, ChannelSet channelSet )
		{
			this.channelSet = channelSet ;
			this.name = name ;
		}
		public String toString() {return this.name ; } ;
	}
	class SpecialCellRenderer extends JLabel implements ListCellRenderer
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -224601712240775345L;
		private ImageIcon active   = ResourceManager.createImageIcon( "Icons/dvbViewer16.png", "DVBViewer icon" ) ;
	    private ImageIcon inactive   = ResourceManager.createImageIcon( "Icons/dvbViewerEmpty16.png", "DVBViewer empty icon" ) ;
	    private ImageIcon unknown   = ResourceManager.createImageIcon( "Icons/dvbViewer Grey16.png", "DVBViewer grey icon" ) ;
	    
	    public SpecialCellRenderer()
	    {
	    	this.setOpaque( true) ;
	    	this.setIconTextGap(5) ;
	    }

	    public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
	        if (isSelected) {
	            this.setBackground( list.getSelectionBackground() );
	            this.setForeground( list.getSelectionForeground() );
	        }
	        else {
	            this.setBackground( list.getBackground() );
	            this.setForeground( list.getForeground() );
	        }
	        ChannelSet channelSet = ((ChannelSetAssignment)value).channelSet ;
	        if ( channelSet.getDVBViewerChannel() != null )
	        {
	        	if ( dvbViewerChannelAssignment.containsKey( channelSet.getDVBViewerChannel() ) )
	        			this.setIcon( this.active ) ;
	        	else 
        			this.setIcon( this.unknown ) ;
	        }
	        else
	        	this.setIcon( this.inactive ) ;
	        this.setText( ((ChannelSetAssignment)value).name );
	        return this;
	    }
	}
	public class ProviderSelected implements ActionListener
	{
	    public void actionPerformed(ActionEvent e) {
	        JComboBox cb = (JComboBox)e.getSource();
	        Provider p = (Provider)cb.getSelectedItem() ;
	        fillProviderChannelList( p.getName() ) ;
	        control.setDefaultProvider( p.getName() ) ;
	        parent.updateExecuteButton() ;
	    }
	}
	public class ProviderChannelSelected implements ListSelectionListener {
	    public void valueChanged(ListSelectionEvent e)
	    {
	    	ChannelSetAssignment csa = (ChannelSetAssignment)((JList)e.getSource()).getSelectedValue() ;
	    	if ( csa == null )
	    		return ;
	    	ChannelSet cs = csa.channelSet ;
	    	String dvbViewerChannel = cs.getDVBViewerChannel() ;
	    	int ix = 0 ;
	    	if ( dvbViewerChannel != null && dvbViewerChannelAssignment.containsKey( dvbViewerChannel ))
	    		ix = dvbViewerChannelAssignment.get( dvbViewerChannel ) ;
	    	ignoreNextDVBViewerChannelChange = true ;
	    	dvbViewerCombo.setSelectedIndex( ix ) ;
	    	mergeCombo.setSelectedIndex( cs.getMerge().ordinal() ) ;
	    }
	}
	public class DVBViewerChannelSelected implements ActionListener
	{
	    public void actionPerformed(ActionEvent e) {
	    	if ( ignoreNextDVBViewerChannelChange )
	    	{
		    	ignoreNextDVBViewerChannelChange = false ;
		    	return ;
	    	}
	        JComboBox cb = (JComboBox)e.getSource();
	        dvbv.dvbviewer.channels.Channel c = (dvbv.dvbviewer.channels.Channel)cb.getSelectedItem() ;
	        int ix = providerChannelList.getSelectedIndex() ;
	        if ( ix >= 0 )
	        {
	        	ChannelSetAssignment csa = (ChannelSetAssignment)providerChannelList.getSelectedValue() ;
	        	csa.channelSet.setDVBViewerChannel( c.getChannelID() ) ;
	        	providerChannelList.setSelectedValue(csa, false) ;
	        }
	    }
	}
	public class MergeChanged implements ActionListener
	{
	    public void actionPerformed(ActionEvent e) {
	    	ChannelSetAssignment csa = (ChannelSetAssignment)providerChannelList.getSelectedValue() ;
	    	if ( csa == null )
	    		return ;
	        JComboBox cb = (JComboBox)e.getSource();
	        csa.channelSet.setMerge( dvbv.misc.Enums.Merge.values()[ cb.getSelectedIndex() ] ) ;
	    }
	}
	public DVBViewerAssignment( GUI parent, dvbv.dvbviewer.channels.Channels dChannels )
	{
		super( parent.getControl(), parent.getFrame() ) ;
		
		this.parent = parent ;
		this.dvbViewerChannels = dChannels ;
	}
	public void paint()
	{
		TitledBorder  tB = null ;
		GridBagConstraints c = null ;
		
		JPanel provider = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( GUIStrings.provider() ) ;
		provider.setBorder( tB ) ;

		Insets i = new Insets( 5, 5, 5, 5 );
		
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		for ( Iterator< Provider > it = Provider.getProviders().iterator() ; it.hasNext() ; )
		{
			this.providerCombo.addItem( it.next() ) ;
		}
		this.providerCombo.addActionListener( new ProviderSelected() ) ;
		provider.add( this.providerCombo, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.gridwidth  = 2 ;
		c.weighty    = 1.0 ;
		c.fill       = GridBagConstraints.BOTH ;
		c.insets     = i ;

		this.providerChannelList.setCellRenderer( new SpecialCellRenderer() ) ;
		this.providerChannelList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ) ;
		this.providerChannelList.addListSelectionListener(  new ProviderChannelSelected() ) ;
		this.fillProviderChannelList( control.getDefaultProvider() ) ;
	    JScrollPane listScroller = new JScrollPane( this.providerChannelList );

		provider.add( listScroller, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = 1 ;
		c.weightx    = 0.5 ;
		c.gridheight = 4 ;
		c.fill       = GridBagConstraints.BOTH ;

		this.add( provider, c ) ;


		
		
		
		JPanel dvbViewer = new JPanel() ;
		
		tB = BorderFactory.createTitledBorder( GUIStrings.dvbViewer() ) ;
		dvbViewer.setBorder( tB ) ;
		
		dvbv.dvbviewer.channels.Channel emptyChannel = new dvbv.dvbviewer.channels.Channel() ;

		this.dvbViewerCombo.addItem( emptyChannel ) ;
		this.dvbViewerCombo.addActionListener( new DVBViewerChannelSelected() ) ;
		
		int channelCount = 0 ;

		for ( Iterator<dvbv.dvbviewer.channels.Channel> it = dvbViewerChannels.getChannels().values().iterator() ; it.hasNext() ; )
		{
			channelCount++ ;
			dvbv.dvbviewer.channels.Channel channel = it.next() ;
			this.dvbViewerChannelAssignment.put( channel.getChannelID(), channelCount) ;
			this.dvbViewerCombo.addItem( channel ) ;
		}
		dvbViewer.add( this.dvbViewerCombo ) ;
		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		c.weightx    = 0.5 ;
		c.fill       = GridBagConstraints.HORIZONTAL ;

		this.add( dvbViewer, c ) ;


		
		JPanel channelPane = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( GUIStrings.channel() ) ;
		channelPane.setBorder( tB ) ;
		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weighty    = 0.5 ;
		c.insets     = i ;
		c.fill       = GridBagConstraints.HORIZONTAL ;

		JButton offsets = new JButton( GUIStrings.offsets() ) ;
		channelPane.add( offsets, c ) ;
		

		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.weighty    = 0.5 ;
		c.insets     = i ;
		c.fill       = GridBagConstraints.BOTH ;

		JLabel mergeTxt = new JLabel( GUIStrings.merge() ) ;
		channelPane.add( mergeTxt, c ) ;

		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weighty    = 0.5 ;
		c.insets     = i ;
		c.fill       = GridBagConstraints.HORIZONTAL ;

		this.mergeCombo.addItem( GUIStrings.global() ) ;
		this.mergeCombo.addItem( GUIStrings.execute() ) ;
		this.mergeCombo.addItem( GUIStrings.no() ) ;
		this.mergeCombo.addActionListener( new MergeChanged() ) ;
		channelPane.add( this.mergeCombo, c ) ;
		
		
		
		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.insets     = i ;
		c.fill       = GridBagConstraints.HORIZONTAL ;

		this.add( channelPane, c ) ;
		
		
		JButton globalOffsets = new JButton( GUIStrings.globalOffsets() ) ;
		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 1 ;
		c.gridy      = 2 ;
		c.weighty    = 0.5 ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
//		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.insets     = i ;
//		c.insets     = new Insets( 40, 5, 5, 5 ); ;

		this.add( globalOffsets, c ) ;
		
		this.providerCombo.setSelectedItem( Provider.getProvider( control.getDefaultProvider() ) ) ;
		
	}
	private void fillProviderChannelList( String providerName )
	{
		int providerID = Provider.getProviderID( providerName ) ;
		
		ArrayList<ChannelSet> sets = control.getChannelSets() ;
		
		TreeMap< String, ChannelSet > channelMap = new TreeMap< String, ChannelSet >( new MyComparator() ) ;
		
		for ( Iterator< ChannelSet > itS = sets.iterator() ; itS.hasNext(); )
		{
			ChannelSet channelSet = itS.next() ;
			ArrayList<Channel> channels = channelSet.getChannels() ;
			for ( Iterator< Channel> itC = channels.iterator() ; itC.hasNext() ; )
			{
				Channel c = itC.next();
				if ( c.getType() != providerID )
					continue ;
				channelMap.put( c.getName(), channelSet ) ;
			}
		}
		
		DefaultListModel map = (DefaultListModel) this.providerChannelList.getModel() ;
		map.clear() ;
				
		for ( Iterator< Map.Entry<String, ChannelSet > > it = channelMap.entrySet().iterator() ; it.hasNext() ; )
		{
			Map.Entry<String, ChannelSet > e = it.next() ;
			map.addElement( new ChannelSetAssignment( e.getKey(), e.getValue()) ) ;
		}
	}
	@Override
	public void update()
	{
		Provider p = (Provider)providerCombo.getSelectedItem() ;
		this.fillProviderChannelList( p.getName() ) ;
	}
}
