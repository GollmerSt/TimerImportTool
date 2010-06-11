// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.TimeOffsets;
import dvbviewertimerimport.misc.Conversions;
import dvbviewertimerimport.misc.Enums;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Function;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.provider.Provider;


public class DVBViewerAssignment extends MyTabPanel{
	private static final long serialVersionUID = 124706451716532907L;
	private final JComboBox providerCombo = new JComboBox() ;
	private final JList providerChannelList = new JList( new DefaultListModel() ) ;
	private final JComboBox dvbViewerCombo = new JComboBox() ;
	private final JButton channelOffsetButton = new JButton() ;
	private final JButton globalOffsetButton = new JButton() ;
	private final JButton updateChannelsFromDVBViewer = new JButton() ;
	private final JCheckBox onlyTVCheckBox = new JCheckBox( ResourceManager.msg( "ONLY_TV" ) ) ;
	private final JButton automaticallyAssignButton = new JButton() ;
	private final JComboBox mergeCombo = new JComboBox() ;
	
	private final JLabel textInfoLabel = new JLabel() ;
	
	private final DVBViewerChannelSelected dvbViewerChannelSelected = new DVBViewerChannelSelected() ;

	private TreeMap< String, Integer > dvbViewerLongChannelAssignment = new TreeMap< String, Integer >( new MyComparator() );
	private HashMap< String, Integer > dvbViewerShortChannelAssignment = new HashMap< String, Integer >();
	private boolean ignoreNextDVBViewerChannelChange = false ;

	public DVBViewerAssignment( GUIPanel parent )
	{
		super( parent ) ;
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
		private ImageIcon offsets   = ResourceManager.createImageIcon( "icons/dvbViewer Red16.png", "DVBViewer icon" ) ;
		private ImageIcon active    = ResourceManager.createImageIcon( "icons/dvbViewer16.png", "DVBViewer icon" ) ;
	    private ImageIcon inactive  = ResourceManager.createImageIcon( "icons/dvbViewerEmpty16.png", "DVBViewer empty icon" ) ;
	    private ImageIcon unknown   = ResourceManager.createImageIcon( "icons/dvbViewer Grey16.png", "DVBViewer grey icon" ) ;
	    private ImageIcon automatic = ResourceManager.createImageIcon( "icons/dvbViewer Yellow16.png", "DVBViewer yellow icon" ) ;
	    
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
	        	if ( dvbViewerLongChannelAssignment.containsKey( channelSet.getDVBViewerChannel() ) )
	        		if ( channelSet.isAutomaticAssigned() )
	        			this.setIcon( this.automatic ) ;
	        		else if ( channelSet.getTimeOffsets().size() > 0 )
	        			this.setIcon( this.offsets ) ;
	        		else
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
			updateText() ;
	        JComboBox cb = (JComboBox)e.getSource();
	        Provider p = (Provider)cb.getSelectedItem() ;
	        fillProviderChannelList( p.getName() ) ;
	        automaticallyAssignButton.setEnabled( p.isChannelMapAvailable() ) ;
	        if ( ! p.getName().equals( control.getDefaultProvider() ) )
	        {
	        	control.setDefaultProvider( p.getName() ) ;
	        	getGUIPanel().setChanged() ;
	        	getGUIPanel().updateExecuteButton() ;
	        }
	    }
	}
	public class DeleteAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6583528140071524896L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			updateText() ;
	        int ix = providerChannelList.getSelectedIndex() ;
	        if ( ix >= 0 )
	        {
	        	getGUIPanel().setChanged() ;
	        	ChannelSetAssignment csa = (ChannelSetAssignment)providerChannelList.getSelectedValue() ;
	        	csa.channelSet.setDVBViewerChannel( null ) ;
	        	csa.channelSet.setAutomaticAssigned( false ) ;
	        	DefaultListModel model = (DefaultListModel) providerChannelList.getModel() ;
	        	model.set( ix, csa) ;
	        } 
		}
	}
	private class FunctionChannelChoice extends Function
	{
		public String stringToString( String in )
		{
			String out = in.split( "\\(" )[0].trim() ;
			out = out.toLowerCase() ;
			out = Conversions.replaceDiacritical( out ) ;
			out = out.replaceAll("[-_\\.\\s]", "") ;
			if ( out.equals( "rtl"))
				out = "rtltelevision" ;
			else if ( out.equals( "ard"))
				out = "daserste" ;
			else if ( out.equals( "b3"))
				out = "bayer" ;
			else if ( out.equals( "srtl" ))
				out = "superrtl" ;
			else if ( out.equals( "br" ))
				out = "bayerisches" ;
			return out ;
		}
	}
	public class ProviderChannelSelected implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e)
	    {
			updateText() ;
	    	ChannelSetAssignment csa = (ChannelSetAssignment)((JList)e.getSource()).getSelectedValue() ;
	    	if ( csa == null )
	    		return ;
	    	ChannelSet cs = csa.channelSet ;
	    	String dvbViewerChannel = cs.getDVBViewerChannel() ;
	    	int ix = 0 ;
	    	if ( dvbViewerChannel != null && dvbViewerLongChannelAssignment.containsKey( dvbViewerChannel ))
	    	{
	    		ix = dvbViewerLongChannelAssignment.get( dvbViewerChannel ) ;
	    	}
	    	else
	    	{
	    		String name = cs.getChannel( providerCombo.getSelectedIndex() ).getName() ;
	    	    @SuppressWarnings("unchecked")
				Object o = Conversions.getTheBestChoice( name, (Collection) control.getDVBViewer().getChannels().getChannels().values(),
						3, 2, new FunctionChannelChoice() ) ;
	    	    if ( o == null )
	    	    	ix = 0 ;
	    	    else
	    	    	ix = dvbViewerShortChannelAssignment.get( o.toString() ) ;
	    	}
	    	ignoreNextDVBViewerChannelChange = true ;
    		dvbViewerCombo.setSelectedIndex( ix ) ;
	    	mergeCombo.setSelectedIndex( cs.getMerge().ordinal() ) ;
	    }
	}
	public class DVBViewerChannelSelected implements ActionListener
	{
	    public void actionPerformed(ActionEvent e) {
			updateText() ;
	    	if ( ignoreNextDVBViewerChannelChange )
	    	{
		    	ignoreNextDVBViewerChannelChange = false ;
		    	return ;
	    	}

	        JComboBox cb = (JComboBox)e.getSource();
	        dvbviewertimerimport.dvbviewer.channels.Channel c = (dvbviewertimerimport.dvbviewer.channels.Channel)cb.getSelectedItem() ;
	        if ( c == null )
	        	return ;
	        int ix = providerChannelList.getSelectedIndex() ;
	        if ( ix >= 0 )
	        {
	        	getGUIPanel().setChanged() ;
	        	ChannelSetAssignment csa = (ChannelSetAssignment)providerChannelList.getSelectedValue() ;
	        	csa.channelSet.setDVBViewerChannel( c.getChannelID() ) ;
	        	csa.channelSet.setAutomaticAssigned( false ) ;
	        	providerChannelList.setSelectedValue(csa, false) ;
	        }
	    }
	}
	public class MergeChanged implements ActionListener
	{
	    public void actionPerformed(ActionEvent e) {
			updateText() ;
	    	ChannelSetAssignment csa = (ChannelSetAssignment)providerChannelList.getSelectedValue() ;
	    	if ( csa == null )
	    		return ;
	        JComboBox cb = (JComboBox)e.getSource();
	        
	        Enums.Merge res = Enums.Merge.values()[ cb.getSelectedIndex() ] ;
	        
	        if ( res != csa.channelSet.getMerge() )
	        {
	        	csa.channelSet.setMerge( res ) ;
	        	getGUIPanel().setChanged() ;
	        }
	    }
	}
	public class ButtonsPressed implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e ) {
			updateText() ;
			JButton source = (JButton)e.getSource() ;
			if ( source == channelOffsetButton || ( source == globalOffsetButton ) )
			{
				TimeOffsets offsets = null ;
				int line = -1 ;
				DefaultListModel model = (DefaultListModel)providerChannelList.getModel() ;
				ChannelSetAssignment csa = null ;
				if ( source == channelOffsetButton )
				{
					line = providerChannelList.getSelectedIndex() ;
					if ( line < 0 )
						return ;
					csa = (ChannelSetAssignment)model.getElementAt( line ) ;
					offsets = csa.channelSet.getTimeOffsets() ;
				}
				else
					offsets = TimeOffsets.getGeneralTimeOffsets() ;
				new OffsetsDialog( getGUIPanel(), offsets ) ;
				if ( csa != null )
					model.setElementAt( csa, line ) ;
			}
			else if ( source == automaticallyAssignButton )
			{
				Provider p = (Provider)providerCombo.getSelectedItem() ;
				assignAutomatically( p ) ;
			}
			else if ( source == updateChannelsFromDVBViewer )
			{
				try
				{
					control.getDVBViewer().getChannels().read( onlyTVCheckBox.isSelected() ) ;
					guiPanel.updateDVBViewerChannels() ;
					updateChannelsFromDVBViewer.setText( ResourceManager.msg( "SUCCESSFULL" ) ) ;
					guiPanel.updateIfChannelSetsChanged( null ) ;
				} catch ( ErrorClass er )
				{
					textInfoLabel.setText( ResourceManager.msg( "ERROR_READING_FILE_NNNN", "channels.dat" ) ) ;
					updateChannelsFromDVBViewer.setText( ResourceManager.msg( "ERROR_READING_FILE" ) ) ;
				}
			}
		}
		
	}
	public void init()
	{		
		TitledBorder  tB = null ;
		GridBagConstraints c = null ;
		
		JPanel provider = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( ResourceManager.msg( "PROVIDER" ) ) ;
		provider.setBorder( tB ) ;

		Insets i = new Insets( 5, 5, 5, 5 );
		
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		for ( Provider p : Provider.getProviders() )
		{
			this.providerCombo.addItem( p ) ;
		}
		this.providerCombo.addActionListener( new ProviderSelected() ) ;
		provider.add( this.providerCombo, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weighty    = 1.0 ;
		c.fill       = GridBagConstraints.BOTH ;
		c.insets     = i ;

		this.providerChannelList.setCellRenderer( new SpecialCellRenderer() ) ;
		this.providerChannelList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ) ;
		this.providerChannelList.addListSelectionListener(  new ProviderChannelSelected() ) ;
		this.providerChannelList.getInputMap().put( KeyStroke.getKeyStroke("DELETE"),"Del pressed") ;
		this.providerChannelList.getActionMap().put("Del pressed", new DeleteAction() ) ;

		this.fillProviderChannelList( control.getDefaultProvider() ) ;
	    JScrollPane listScroller = new JScrollPane( this.providerChannelList );

		provider.add( listScroller, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = 1 ;
		c.weightx    = 0.5 ;
		c.gridheight = 5 ;
		c.fill       = GridBagConstraints.BOTH ;

		this.add( provider, c ) ;


		
		
		
		JPanel dvbViewer = new JPanel() ;
		
		tB = BorderFactory.createTitledBorder( ResourceManager.msg( "DVBVIEWER" ) ) ;
		dvbViewer.setBorder( tB ) ;
		
		this.dvbViewerCombo.addActionListener( dvbViewerChannelSelected ) ;
		
		this.updateDVBViewerChannels() ;
		
		dvbViewer.add( this.dvbViewerCombo ) ;
		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		c.weightx    = 0.5 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;

		this.add( dvbViewer, c ) ;


		
		JPanel channelPane = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( ResourceManager.msg( "CHANNEL" ) ) ;
		channelPane.setBorder( tB ) ;
		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weighty    = 0.5 ;
		c.insets     = i ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;

		this.channelOffsetButton.setText( ResourceManager.msg( "OFFSETS" ) ) ;
		this.channelOffsetButton.addActionListener( new ButtonsPressed() ) ;
		channelPane.add( this.channelOffsetButton, c ) ;
		

		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.weighty    = 0.5 ;
		c.insets     = i ;
		c.fill       = GridBagConstraints.BOTH ;

		JLabel mergeTxt = new JLabel( ResourceManager.msg( "MERGE" ) ) ;
		channelPane.add( mergeTxt, c ) ;

		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weighty    = 0.5 ;
		c.insets     = i ;
		c.fill       = GridBagConstraints.HORIZONTAL ;

		this.mergeCombo.addItem( ResourceManager.msg( "GLOBAL" ) ) ;
		this.mergeCombo.addItem( ResourceManager.msg( "EXECUTE" ) ) ;
		this.mergeCombo.addItem( ResourceManager.msg( "NO" ) ) ;
		this.mergeCombo.addActionListener( new MergeChanged() ) ;
		channelPane.add( this.mergeCombo, c ) ;
		
		
		
		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.insets     = i ;
		c.fill       = GridBagConstraints.HORIZONTAL ;

		this.add( channelPane, c ) ;
		
		
		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 1 ;
		c.gridy      = 2 ;
		c.weighty    = 0.5 ;
		c.anchor     = GridBagConstraints.NORTH ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.insets     = i ;
//		c.insets     = new Insets( 40, 5, 5, 5 ); ;

		this.globalOffsetButton.setText( ResourceManager.msg( "GLOBAL_OFFSETS" ) ) ;
		this.globalOffsetButton.addActionListener( new ButtonsPressed() ) ;
		this.add( this.globalOffsetButton, c ) ;
		
		
		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 3 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.weighty    = 1.0 ;
		c.anchor     = GridBagConstraints.SOUTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.add( this.onlyTVCheckBox, c ) ;
		this.onlyTVCheckBox.setSelected( true ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 3 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.weightx    = 0.5 ;
		c.anchor     = GridBagConstraints.SOUTHWEST ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.updateChannelsFromDVBViewer.addActionListener( new ButtonsPressed() ) ;
		this.add( this.updateChannelsFromDVBViewer, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 4 ;
		//c.weighty    = 0.5 ;
		c.anchor     = GridBagConstraints.SOUTHWEST ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
//		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.insets     = i ;
//		c.insets     = new Insets( 40, 5, 5, 5 ); ;

		this.automaticallyAssignButton.setText( ResourceManager.msg( "ASSIGN_AUTOMATICALLY" ) ) ;
		this.automaticallyAssignButton.addActionListener( new ButtonsPressed() ) ;
		this.add( this.automaticallyAssignButton, c ) ;
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 5 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.gridheight = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.textInfoLabel.setForeground( SystemColor.RED ) ;
		this.textInfoLabel.setPreferredSize( new Dimension( 1,20 ) ) ;
		
		this.add( this.textInfoLabel, c ) ;
		
		this.providerCombo.setSelectedItem( Provider.getProvider( control.getDefaultProvider() ) ) ;
		this.updateText() ;
	}
	private void fillProviderChannelList( String providerName )
	{
		Provider provider = Provider.getProvider( providerName ) ;
		int providerID = provider.getID() ;
		
		ArrayList<ChannelSet> sets = control.getChannelSets() ;
		
		TreeMap< String, ChannelSet > channelMap = new TreeMap< String, ChannelSet >( new MyComparator() ) ;
		
		for ( ChannelSet channelSet : sets )
		{
			Channel c = channelSet.getChannel( providerID ) ;
			if ( c == null || ! provider.containsChannel( c, true ) )
				continue ;
			channelMap.put( c.getName(), channelSet ) ;
		}
		
		DefaultListModel map = (DefaultListModel) this.providerChannelList.getModel() ;
		map.clear() ;
				
		for ( Map.Entry<String, ChannelSet > e : channelMap.entrySet() )
			map.addElement( new ChannelSetAssignment( e.getKey(), e.getValue()) ) ;
	}
	private void assignAutomatically( Provider provider )
	{
		if ( ! provider.isChannelMapAvailable() )
			return ;
		
		int providerID = provider.getID() ;
		
		ArrayList<ChannelSet> sets = control.getChannelSets() ;
				
		for ( ChannelSet cs : sets )
		{
			if ( cs.getDVBViewerChannel() != null )
				continue ;
    		Channel channel = cs.getChannel( providerID ) ;
			if ( channel == null || ! provider.containsChannel( channel, false ) )
				continue ;
    		String name = channel.getName() ;
    	    @SuppressWarnings("unchecked")
			Object o = Conversions.getTheBestChoice( name, (Collection) control.getDVBViewer().getChannels().getChannels().values(),
					3, 2, new FunctionChannelChoice() ) ;
    	    if ( o == null )
    	    	continue ;
    	   
    	    int ix = dvbViewerShortChannelAssignment.get( o.toString() ) ;
    	    
    	    cs.setDVBViewerChannel( ( (dvbviewertimerimport.dvbviewer.channels.Channel)this.dvbViewerCombo.getItemAt( ix )).getChannelID() ) ;
    	    cs.setAutomaticAssigned( true ) ;
    	}
		this.update( true );
    	this.getGUIPanel().setChanged() ;

	}
	@Override
	public void update( boolean active )
	{
		if ( active )
		{
			Provider p = (Provider)providerCombo.getSelectedItem() ;
			if ( p == null )
				return ;
			this.fillProviderChannelList( p.getName() ) ;
			updateText() ;
		}
	}
	public void updateDVBViewerChannels()
	{
		int channelCount = 0 ;
		
		this.dvbViewerLongChannelAssignment  = new TreeMap< String, Integer >( new MyComparator() );
		this.dvbViewerShortChannelAssignment = new HashMap< String, Integer >();
		
		this.providerChannelList.clearSelection() ;

		this.dvbViewerCombo.removeAllItems() ;

		dvbviewertimerimport.dvbviewer.channels.Channel emptyChannel = new dvbviewertimerimport.dvbviewer.channels.Channel() ;
//		this.ignoreNextDVBViewerChannelChange = true ;
		this.dvbViewerCombo.addItem( emptyChannel ) ;
		
		if ( control.getDVBViewer().getChannels().getChannels() != null )
		{
			for ( dvbviewertimerimport.dvbviewer.channels.Channel channel : control.getDVBViewer().getChannels().getChannels().values() )
			{
				channelCount++ ;
				this.dvbViewerLongChannelAssignment.put( channel.getChannelID(), channelCount) ;
				this.dvbViewerShortChannelAssignment.put( channel.getChannelName(), channelCount) ;
				this.dvbViewerCombo.addItem( channel ) ;
			}
		}
		
		this.dvbViewerCombo.updateUI() ;
	}
	private void updateText()
	{
		this.updateChannelsFromDVBViewer.setText( ResourceManager.msg( "UPDATE_DVBV_CHANNELS" ) ) ;
		this.textInfoLabel.setText( "" ) ;
	}
}
