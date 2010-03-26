// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import dvbv.dvbviewer.DVBViewerService;
import dvbv.misc.ErrorClass;
import dvbv.provider.OutDatedInfo;
import dvbv.provider.Provider;



public class ProviderService extends MyTabPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 460262656692083356L;
	
	private final JComboBox providerCombo = new JComboBox() ;
	private final JCheckBox lockBox = new JCheckBox( ) ;
	private final JTextField urlBox = new JTextField() ;
	private final JLabel userNameLabel = new JLabel() ;
	private final JTextField userNameBox = new JTextField() ;
	private final JLabel passwordLabel = new JLabel() ;
	private final JTextField passwordBox = new JTextField() ;
	private final JSpinner triggerBox = new JSpinner();
	
	private final JCheckBox enableSinceBox = new JCheckBox() ;
	private final JLabel sinceLabel = new JLabel() ;
	private final JSpinner sinceBox = new JSpinner();
	private final JLabel sinceSyncLabel = new JLabel() ;
	private final JSpinner sinceSyncBox = new JSpinner();
	
	private final JCheckBox verboseBox = new JCheckBox() ;
	private final JCheckBox messageBox = new JCheckBox() ;
	private final JCheckBox mergeBox = new JCheckBox() ;
	private final JCheckBox filterBox = new JCheckBox() ;
	private final JButton providerButton = new JButton() ;
	private final JButton installButton = new JButton() ;
	private final JButton uninstallButton = new JButton() ;
	
	private final JCheckBox serviceEnableBox = new JCheckBox() ;
	private final JLabel urlServiceLabel = new JLabel() ;
	private final JTextField urlServiceBox = new JTextField() ;
	private final JLabel userServiceLabel = new JLabel() ;
	private final JTextField userServiceBox = new JTextField() ;
	private final JLabel passwordServiceLabel = new JLabel() ;
	private final JTextField passwordServiceBox = new JTextField() ;
	private final JCheckBox wolEnableBox = new JCheckBox() ;
	
	private final JLabel waitTimeLabel = new JLabel() ;
	private final JSpinner waitTimeBox = new JSpinner() ;
	private final JLabel broadCastLabel = new JLabel() ;
	private final JTextField broadCastBox = new JTextField() ;
	private final JLabel macLabel = new JLabel() ;
	private final JTextField macBox = new JTextField() ;
	private final JButton serviceButton = new JButton() ;
	
	private ProviderCheckBoxesChanged providerCheckBoxesListener = null ;
	private boolean providerCheckBoxesListenerEnabled = false ;
	
	private Provider lastProvider = null ;
	
	private void setOutDated( final OutDatedInfo o )
	{
		OutDatedInfo i = o ;
		boolean inhibit = false ;
		boolean enable = true ;
		if ( i == null  )
		{
			i = new OutDatedInfo() ;
			inhibit = true ;
			enable  = false ;
		}
		enable = i.isEnabled() ?  enable : false ;
		
		this.enableSinceBox.setEnabled( ! inhibit ) ;
		this.sinceLabel.    setEnabled( ! inhibit ) ;
		this.sinceBox.      setEnabled( enable ) ;
		this.sinceSyncLabel.setEnabled( ! inhibit ) ;
		this.sinceSyncBox.  setEnabled( enable ) ;
		
		this.enableSinceBox.setSelected( i.isEnabled() ) ;
		this.sinceBox.setValue( i.getMissingSince() ) ;
		this.sinceSyncBox.setValue( i.getMissingSyncSince() ) ;
	}

	public class ProviderSelected implements ActionListener
	{
	    public void actionPerformed(ActionEvent e) {
	    	lockBox.setSelected( false ) ;
	        JComboBox cb = (JComboBox)e.getSource();
	        Object o = cb.getSelectedItem() ;
	        if ( o == null )
	        	return ;
	        setListenerProviderCheckBoxes( false ) ;
	        updateProvider() ;
	        Provider p = (Provider)cb.getSelectedItem() ;
	        lastProvider = p ;
	        boolean hasURL = p.hasURL() ;
	        boolean hasAccount = p.hasAccount() ;
	        lockBox.setEnabled( hasURL ) ;
	        if ( lockBox.isSelected() )
	        	urlBox.setEnabled( hasURL ) ;
	        else
	        	urlBox.setEnabled( false ) ;
	        userNameLabel.setEnabled( hasAccount ) ;
	        userNameBox.setEnabled( hasAccount ) ;
	        passwordLabel.setEnabled( hasAccount ) ;
	        passwordBox.setEnabled( hasAccount ) ;
	        triggerBox.setValue( p.getTriggerAction() ) ;
	        providerButton.setEnabled( p.canTest() ) ;
	        providerButton.setText( GUIStrings.CHECK.toString() ) ;
	        if ( hasURL )
	        	urlBox.setText( p.getURL() ) ;
	        else
	        	urlBox.setText( "" ) ;
	        if ( hasAccount )
	        {
	        	userNameBox.setText( p.getUserName() ) ;
	        	passwordBox.setText( p.getPassword() ) ;
	        }
	        else
	        {
	        	userNameBox.setText( "" ) ;
	        	passwordBox.setText( "" ) ;
	        	
	        }
	        setOutDated( p.getOutDatedLimits() ) ;
	        verboseBox.setSelected( p.getVerbose() ) ;
	        messageBox.setSelected( p.getMessage() ) ;
	        mergeBox.setSelected( p.getMerge() ) ;
	        filterBox.setSelected( p.isFiltered() ) ;
    		installButton.setText( GUIStrings.INSTALL.toString() ) ;
    		uninstallButton.setText( GUIStrings.UNINSTALL.toString() ) ;
	        installButton.setEnabled( p.mustInstall() ) ;
	        uninstallButton.setEnabled( p.mustInstall() ) ;
	        setListenerProviderCheckBoxes( true ) ;

	    }
	}
	public class LockBoxChanged implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e )
		{
			Provider p = (Provider) providerCombo.getSelectedItem() ;
			if ( p.hasURL() )
				urlBox.setEnabled( lockBox.isSelected() ) ;
		}
	}
	public void setListenerProviderCheckBoxes( boolean enable )
	{
		if ( this.providerCheckBoxesListener == null )
			providerCheckBoxesListener = new ProviderCheckBoxesChanged() ;
		
		if ( providerCheckBoxesListenerEnabled == enable )
			return ;

		providerCheckBoxesListenerEnabled = enable ;
		
		if ( enable )
		{
			this.verboseBox.addItemListener( providerCheckBoxesListener ) ;
			this.messageBox.addItemListener( providerCheckBoxesListener ) ;
			this.mergeBox.addItemListener(   providerCheckBoxesListener ) ;
			this.filterBox.addItemListener(  providerCheckBoxesListener ) ;
		}
		else
		{
			this.verboseBox.removeItemListener( providerCheckBoxesListener ) ;
			this.messageBox.removeItemListener( providerCheckBoxesListener ) ;
			this.mergeBox.removeItemListener(   providerCheckBoxesListener ) ;
			this.filterBox.removeItemListener(  providerCheckBoxesListener ) ;
		}
	}
	public class ProviderCheckBoxesChanged implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e) {
			Provider p = (Provider) providerCombo.getSelectedItem() ;
			JCheckBox source = (JCheckBox) e.getItemSelectable();
			if( source == verboseBox )
				p.setVerbose( source.isSelected() ) ;
			else if ( source == messageBox )
				p.setMessage( source.isSelected() ) ;
			else if ( source == mergeBox )
				p.setMerge( source.isSelected() ) ;
			else if ( source == filterBox )
				p.setFilter( source.isSelected() ) ;
			else if ( source == enableSinceBox && p.getOutDatedLimits() != null )
			{
				p.getOutDatedLimits().setEnabled( source.isSelected() ) ;
				setOutDated( p.getOutDatedLimits() ) ;
			}
			gui.setChanged() ;
		}
	}
	

	
	public class ProviderTestButton implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			updateProvider() ;
	        Provider p = (Provider)providerCombo.getSelectedItem() ;
	        if ( p.canTest() )
	        {
				String buttonText = GUIStrings.FAILED.toString() ;
	        	if ( p.test() ) 
	        		buttonText = GUIStrings.PASS.toString() ;
	        	providerButton.setText( buttonText ) ;
	        }
		}
	}
	public class ProviderInstallButtons implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			updateProvider() ;
	        Provider p = (Provider)providerCombo.getSelectedItem() ;
	        if ( p.mustInstall() )
	        {
	        	JButton button = (JButton)e.getSource() ;
	        	if ( button == installButton )
	        	{
	        		if ( p.install() )
	        			installButton.setText( GUIStrings.SUCCESSFULL.toString() ) ;
	        		else
	        			installButton.setText( GUIStrings.FAILED.toString() ) ;
	        		uninstallButton.setText( GUIStrings.UNINSTALL.toString() ) ;
	        	}
	        	else if ( button == uninstallButton )
	        	{
	        		if ( p.uninstall() )
	        			uninstallButton.setText( GUIStrings.SUCCESSFULL.toString() ) ;
	        		else
	        			uninstallButton.setText( GUIStrings.FAILED.toString() ) ;
	        		installButton.setText( GUIStrings.INSTALL.toString() ) ;
	        	}
	        }
		}
	}
	public class ServiceCheckBoxesChanged implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e) {
			DVBViewerService dvbs = control.getDVBViewer().getService() ;
			Object source = e.getItemSelectable();
			boolean serviceEnabled = serviceEnableBox.isSelected() ;
			if( source == serviceEnableBox )
			{
				urlServiceLabel.setEnabled( serviceEnabled ) ;
				urlServiceBox.setEnabled( serviceEnabled ) ;
				userServiceLabel.setEnabled( serviceEnabled ) ;
				userServiceBox.setEnabled( serviceEnabled ) ;
				passwordServiceLabel.setEnabled( serviceEnabled ) ;
				passwordServiceBox.setEnabled( serviceEnabled ) ;
				serviceButton.setEnabled( serviceEnabled ) ;
				wolEnableBox.setEnabled( serviceEnabled ) ;
				if ( serviceEnabled != dvbs.isEnabled() )
				{
					dvbs.setEnabled( serviceEnabled ) ;
					gui.setChanged() ;
				}
			}
			if ( source == wolEnableBox || source == serviceEnableBox )
			{
				boolean enabled = wolEnableBox.isSelected() ;
				if ( enabled != dvbs.getEnableWOL() )
				{
					dvbs.setEnableWOL( enabled ) ;
					gui.setChanged() ;
				}
				enabled = enabled && serviceEnabled ;
				waitTimeLabel.setEnabled( enabled ) ;
				waitTimeBox.setEnabled( enabled ) ;
				broadCastLabel.setEnabled( enabled ) ;
				broadCastBox.setEnabled( enabled ) ;
				macLabel.setEnabled( enabled ) ;
				macBox.setEnabled( enabled ) ;
			}
		}
	}

	public class ServiceTestButton implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			updateService() ;
			long version ;
			try
			{
				version = control.getDVBViewer().getService().readVersion( true ) ;
			} catch ( ErrorClass e1 ) {
				version = -1 ;
			}
			String buttonText = null ;
			if ( version < 0 )
				buttonText = GUIStrings.FAILED.toString() ;
			else
			{
				long high = version / 10000000 ;
				version %= 10000000 ;
				long mid = version / 100000 ;
				version %= 100000 ;
				long low = version / 1000 ;
				version %= 1000 ;
				buttonText = "Version: " + Long.toString( high ) + "."
				                         + Long.toString( mid )  + "."
				                         + Long.toString( low )  + "."
				                         + Long.toString( version ) ;
			}
			((JButton)e.getSource()).setText( buttonText ) ;
		}
		
	}
	public ProviderService( GUI gui, JFrame frame )
	{
		super( gui, frame ) ;
	}
	public void paint()
	{
		Provider defaultProvider = Provider.getProvider( control.getDefaultProvider() ) ;
		
		TitledBorder  tB = null ;
		GridBagConstraints c = null ;
		
		JPanel providerBox = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( GUIStrings.PROVIDER.toString() ) ;
		providerBox.setBorder( tB ) ;

		Insets i = new Insets( 5, 5, 5, 5 );
		
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.providerCombo.addActionListener( new ProviderSelected() ) ;
		for ( Provider p : Provider.getProviders() )
			this.providerCombo.addItem( p ) ;
		this.providerCombo.setSelectedItem( defaultProvider ) ;
		this.lastProvider = defaultProvider ;
		providerBox.add( this.providerCombo, c ) ;

		
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.lockBox.setText( GUIStrings.URL.toString() ) ;
		this.lockBox.setHorizontalAlignment( SwingConstants.RIGHT ) ;
		this.lockBox.addItemListener( new LockBoxChanged() ) ;
		providerBox.add( this.lockBox, c ) ;
		
		

		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		c.weightx    = 0.5 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		providerBox.add( this.urlBox, c ) ;
		
		

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 2 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.userNameLabel.setText( GUIStrings.USER_NAME.toString() ) ;
		providerBox.add( this.userNameLabel, c ) ;
		
		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 2 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		providerBox.add( this.userNameBox, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 3 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.passwordLabel.setText( GUIStrings.PASSWORD.toString() ) ;
		providerBox.add( this.passwordLabel, c ) ;
		
				
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 3 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		providerBox.add( passwordBox, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 4 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		JLabel triggerLabel = new JLabel( GUIStrings.TRIGGER_ACTION.toString() ) ;
		providerBox.add( triggerLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 4 ;
//		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
		
		SpinnerModel model = new SpinnerNumberModel(
                defaultProvider.getTriggerAction(), //initial value
                -1, 1 << 15, 1 ) ;
		this.triggerBox.setModel( model );
		providerBox.add( triggerBox, c ) ;


		
		JPanel checkBoxPanel = new JPanel( new GridBagLayout() ) ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.enableSinceBox.addItemListener( new ProviderCheckBoxesChanged() ) ;
		checkBoxPanel.add( this.enableSinceBox, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.sinceLabel.setText( GUIStrings.MISSING_SINCE.toString() ) ;
		checkBoxPanel.add( this.sinceLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 0 ;
//		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
		
		model = new SpinnerNumberModel(
                0, //initial value
                0, 99, 1 ) ;
		this.sinceBox.setModel( model );
		checkBoxPanel.add( this.sinceBox, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 0 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.sinceSyncLabel.setText( GUIStrings.MISSING_SINCE_SYNC.toString() ) ;
		checkBoxPanel.add( this.sinceSyncLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 4 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
		
		model = new SpinnerNumberModel(
                0, //initial value
                0, 99, 1 ) ;
		this.sinceSyncBox.setModel( model );
		checkBoxPanel.add( this.sinceSyncBox, c ) ;

		
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		//c.weightx    = 0.5 ;
		c.gridwidth  = 2 ;
		c.insets     = i ;
		
		this.verboseBox.setText( GUIStrings.VERBOSE.toString() ) ;
		checkBoxPanel.add( this.verboseBox, c ) ;
		

		
		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 1 ;
		c.weightx    = 0.5 ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.messageBox.setText( GUIStrings.MESSAGE.toString() ) ;
		checkBoxPanel.add( this.messageBox, c ) ;
		

		
		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 1 ;
		c.weightx    = 0.5 ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.mergeBox.setText( GUIStrings.MERGE.toString() ) ;
		checkBoxPanel.add( this.mergeBox, c ) ;
		

		
		c = new GridBagConstraints();
		c.gridx      = 4 ;
		c.gridy      = 1 ;
		c.weightx    = 0.5 ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.filterBox.setText( GUIStrings.FILTER.toString() ) ;
		checkBoxPanel.add( this.filterBox, c ) ;
		

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 5 ;
		c.weightx    = 0.5 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.insets     = new Insets( 0, 0, 0, 0 );
		c.fill       = GridBagConstraints.HORIZONTAL ;
		
		providerBox.add( checkBoxPanel, c ) ;

		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 0 ;
		c.gridy      = 6 ;
		c.insets     = i ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;

		this.providerButton.setText( GUIStrings.CHECK.toString() ) ;
		providerButton.addActionListener( new ProviderTestButton() ) ;
		providerBox.add( providerButton, c ) ;
		
		
		
		JPanel installPanel = new JPanel( new GridBagLayout() ) ;

		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.insets     = new Insets( 0, 0, 0, 0 ); ;

		this.installButton.setText( GUIStrings.INSTALL.toString() ) ;
		installButton.addActionListener( new ProviderInstallButtons() ) ;
		installPanel.add( installButton, c ) ;

		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		c.weightx    = 1.0 ;
		c.insets     = new Insets( 0, 0, 0, 0 ); ;

		this.uninstallButton.setText( GUIStrings.UNINSTALL.toString() ) ;
		uninstallButton.addActionListener( new ProviderInstallButtons() ) ;
		installPanel.add( uninstallButton, c ) ;

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 7 ;
		c.insets     = i ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;

		providerBox.add( installPanel, c ) ;
		
		
		
		
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.weightx    = 1 ;
		//c.weighty    = 0.5 ;
		c.fill       = GridBagConstraints.BOTH ;
		c.anchor     = GridBagConstraints.NORTHWEST ;

		//providerBox.setPreferredSize( new Dimension( 380, 350 ) ) ;
		this.add( providerBox, c ) ;
        this.setOutDated( defaultProvider.getOutDatedLimits() ) ;

	
	

		
		DVBViewerService dvbs = this.control.getDVBViewer().getService() ;
	

		JPanel serviceBox = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( GUIStrings.DVBVIEWER_SERVICE.toString() ) ;
		serviceBox.setBorder( tB ) ;

		
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
		
		this.serviceEnableBox.setText( GUIStrings.ENABLE.toString() ) ;
		this.serviceEnableBox.setSelected( ! dvbs.isEnabled() ) ;
		this.serviceEnableBox.addItemListener( new ServiceCheckBoxesChanged() ) ;
		this.serviceEnableBox.setSelected( dvbs.isEnabled() ) ;
		serviceBox.add( this.serviceEnableBox, c ) ;



		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.urlServiceLabel.setText( GUIStrings.URL.toString() ) ;
		serviceBox.add( this.urlServiceLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		c.weightx    = 0.5 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.urlServiceBox.setText( dvbs.getURL() ) ;
		serviceBox.add( this.urlServiceBox, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 2 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.userServiceLabel.setText( GUIStrings.USER_NAME.toString() ) ;
		serviceBox.add( this.userServiceLabel, c ) ;
		
		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 2 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.userServiceBox.setText( dvbs.getUserName() ) ;
		serviceBox.add( this.userServiceBox, c ) ;
		
		

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 3 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.passwordServiceLabel.setText( GUIStrings.PASSWORD.toString() ) ;
		serviceBox.add( this.passwordServiceLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 3 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.passwordServiceBox.setText( dvbs.getPassword() ) ;
		serviceBox.add( this.passwordServiceBox, c ) ;

		
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 4 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
		
		this.wolEnableBox.setText( GUIStrings.WOL.toString() ) ;
		this.wolEnableBox.setSelected( ! dvbs.getEnableWOL() ) ;
		this.wolEnableBox.addItemListener( new ServiceCheckBoxesChanged() ) ;
		this.wolEnableBox.setSelected( dvbs.getEnableWOL() ) ;
		serviceBox.add( this.wolEnableBox, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 4 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.waitTimeLabel.setText( GUIStrings.WAIT_TIME.toString() ) ;
		serviceBox.add( this.waitTimeLabel, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 4 ;
//		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
		
		SpinnerModel waitModel = new SpinnerNumberModel(
                dvbs.getWaitTimeAfterWOL(), //initial value
                -1, 60, 1 ) ;
		this.waitTimeBox.setModel( waitModel );
		serviceBox.add( waitTimeBox, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 5 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.broadCastLabel.setText( GUIStrings.BROAD_CAST_ADDRESS.toString() ) ;
		serviceBox.add( this.broadCastLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 5 ;
		c.weightx    = 0.5 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.broadCastBox.setText( dvbs.getBroadCastAddress() ) ;
		serviceBox.add( this.broadCastBox, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 6 ;
		c.fill       = GridBagConstraints.VERTICAL ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
		
		this.macLabel.setText( GUIStrings.MAC_ADDRESS.toString() ) ;
		serviceBox.add( this.macLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 6 ;
		c.weightx    = 0.5 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.macBox.setText( dvbs.getMacAddress() ) ;
		serviceBox.add( this.macBox, c ) ;

		
		
		c = new GridBagConstraints();
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.gridx      = 0 ;
		c.gridy      = 7 ;
		c.insets     = i ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;

		this.serviceButton.setText( GUIStrings.CHECK.toString() ) ;
		serviceButton.addActionListener( new ServiceTestButton() ) ;
		serviceBox.add( serviceButton, c ) ;

		
		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		c.weightx    = 0.5 ;
		//c.weighty    = 0.5 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.BOTH ;
		c.anchor     = GridBagConstraints.NORTHWEST ;

		serviceBox.setPreferredSize( new Dimension( 300, 300 ) ) ;
		this.add( serviceBox, c ) ;

		this.setListenerProviderCheckBoxes( true ) ;
}
	public void updateLockBox()
	{
		this.lockBox.setSelected( false ) ;
	}
	public void updateProvider()
	{
		if ( this.lastProvider != null )
		{
			if ( ! this.lastProvider.getURL().equals( this.urlBox.getText() ) )
			{
				this.lastProvider.setURL( this.urlBox.getText() ) ;
				this.gui.setChanged() ;
			}
			if (    this.lastProvider.getUserName() != null
				 && ! this.lastProvider.getUserName().equals( this.userNameBox.getText() ) )
			{
				this.lastProvider.setUserName( this.userNameBox.getText() ) ;
				this.gui.setChanged() ;
			}
			if (    this.lastProvider.getPassword() != null
				 && ! this.lastProvider.getPassword().equals( this.passwordBox.getText() ) )
			{
				this.lastProvider.setPassword( this.passwordBox.getText() ) ;
				this.gui.setChanged() ;
			}
			int trigger = ((Integer)this.triggerBox.getValue()).intValue() ;
			if ( this.lastProvider.getTriggerAction() != trigger )
			{
				this.lastProvider.setTriggerAction( trigger ) ;
				this.gui.setChanged() ;
			}
			if ( this.lastProvider.getOutDatedLimits() != null )
			{
				OutDatedInfo i = this.lastProvider.getOutDatedLimits() ;
				int missingSince = ((Integer)this.sinceBox.getValue()).intValue() ;
				if ( missingSince != i.getMissingSince() )
				{
					i.setMissingSince( missingSince ) ;
					this.gui.setChanged() ;
				}
				int missingSyncSince = ((Integer)this.sinceSyncBox.getValue()).intValue() ;
				if ( missingSyncSince != i.getMissingSyncSince() )
				{
					i.setMissingSyncSince( missingSyncSince ) ;
					this.gui.setChanged() ;
				}
			}
		}
	}
	public void updateService()
	{
		DVBViewerService dvbs  = this.control.getDVBViewer().getService() ;
		if ( ! dvbs.getURL().equals( this.urlServiceBox.getText() ) )
		{
			dvbs.setURL( this.urlServiceBox.getText() ) ;
			this.gui.setChanged() ;
		}
		if ( ! dvbs.getUserName().equals( this.userServiceBox.getText() ) )
		{
			dvbs.setUserName( this.userServiceBox.getText() ) ;
			this.gui.setChanged() ;
		}
		if ( ! dvbs.getPassword().equals( this.passwordServiceBox.getText() ) )
		{
			dvbs.setPassword( this.passwordServiceBox.getText() ) ;
			this.gui.setChanged() ;
		}
		int waitTime = (Integer)this.waitTimeBox.getValue() ;
		if ( dvbs.getWaitTimeAfterWOL() != waitTime )
		{
			dvbs.setWaitTimeAfterWOL( waitTime ) ;
			this.gui.setChanged() ;
		}
		if ( ! dvbs.getBroadCastAddress().equals( this.broadCastBox.getText() ) )
		{
			dvbs.setBroadCastAddress( this.broadCastBox.getText() ) ;
			this.gui.setChanged() ;
		}
		if ( ! dvbs.getMacAddress().equals( this.macBox.getText() ) )
		{
			dvbs.setMacAddress( this.macBox.getText() ) ;
			this.gui.setChanged() ;
		}
	}
	@Override
	public void update( boolean active )
	{
		if ( active )
			this.updateLockBox() ;
		else
		{
			this.updateProvider() ;
			this.updateService() ;
		}
	}
	
}
