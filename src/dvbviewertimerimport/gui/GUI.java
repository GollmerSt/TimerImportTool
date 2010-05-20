// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.misc.Constants;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.provider.Provider;

public class GUI extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1912798357166099713L;
	public enum GUIStatus { INVALID, OK, CANCEL, APPLY, EXECUTE, SAVE_EXECUTE, UPDATE } ;
	private final Control control ;

	private ImageIcon programIcon   = ResourceManager.createImageIcon( "icons/dvbViewer Programm16.png", "DVBViewer icon" ) ;

	private final Semaphore guiBusy = new Semaphore(1) ;
	private final Semaphore appBusy = new Semaphore(1) ;
	
	private GUIStatus status = GUIStatus.INVALID ;
	private boolean isChanged = false ;
	
	private final GUIPanel guiPanel ;

	private final JCheckBox forceBox ;
    private final JButton executeButton ;
    private final JButton updateButton ;
    private final JButton okButton ;
    private final JButton cancelButton ;
    private final JButton applyButton ;
    
    private ProviderAssignment providerAssignment = null ;
    private Miscellaneous miscellaneous = null ;
    
	private final TreeMap< String, LookAndFeelInfo > lookAndFeelAssignment  
    = new TreeMap< String, LookAndFeelInfo >() ;
	private final ArrayList< String > lookAndFeelNames = new ArrayList< String >() ;
	
	public GUI( Control control )
	{
		this.control = control ;
		Locale.setDefault( control.getLanguage() ) ;
		String className = UIManager.getSystemLookAndFeelClassName() ;
		LookAndFeelInfo systemLookAndFeelInfo = new LookAndFeelInfo( Constants.SYSTEM_LOOK_AND_FEEL_NAME, className ) ;
		this.lookAndFeelAssignment.put( Constants.SYSTEM_LOOK_AND_FEEL_NAME, systemLookAndFeelInfo ) ;
		this.lookAndFeelNames.add( Constants.SYSTEM_LOOK_AND_FEEL_NAME ) ;
		for ( LookAndFeelInfo lf : UIManager.getInstalledLookAndFeels() )
		{
			this.lookAndFeelAssignment.put( lf.getName(), lf ) ;
			this.lookAndFeelNames.add( lf.getName() ) ;
		}
		
		this.setLookAndFeel( control.getLookAndFeelName() ) ;
		this.isChanged = false ;
		
		this.guiPanel = new GUIPanel( this, control ) ;
		this.forceBox = new JCheckBox() ;
		this.executeButton = new JButton() ;
		this.updateButton = new JButton() ;
		this.okButton = new JButton() ;
		this.cancelButton = new JButton() ;
		this.applyButton = new JButton() ;
	
	}

	private class MyWindowListener implements WindowListener
    {

		@Override
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			// TODO Auto-generated method stub
			MyTabPanel tabPanel = guiPanel.getSelectedComponent() ;
			tabPanel.update( false ) ;
			if ( messageIsChanged( ResourceManager.msg( "SETUP_CHANGED" ) ) )
			{
				status = GUIStatus.CANCEL ;
				waitAPP() ;
			}
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}
    	
    }
    
	public class ButtonPressed implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			MyTabPanel tabPanel = guiPanel.getSelectedComponent() ;
			tabPanel.update( false ) ;

			JButton button = (JButton) e.getSource() ;
			
			if ( button == okButton )
			{
				status = GUIStatus.OK ;
				waitAPP() ;
			}
			else if ( button == cancelButton && messageIsChanged( ResourceManager.msg( "SETUP_CHANGED" ) ) )
			{
				status = GUIStatus.CANCEL ;
				waitAPP() ;
			}
			if ( button == applyButton )
			{
				status = GUIStatus.APPLY ;
				waitAPP() ;
				isChanged = false ;
			}
			if ( button == executeButton )
			{
				if ( messageIsChanged( ResourceManager.msg( "SETUP_SAVE" ) ))
					status = GUIStatus.SAVE_EXECUTE ;
				else
					status = GUIStatus.EXECUTE ;
				waitAPP() ;
			}
			if ( button == updateButton )
			{
				status = GUIStatus.UPDATE ;
				waitAPP() ;
			}
		}
	}
	public class AllTimersChanged implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e) {
			Provider p = Provider.getProvider( control.getDefaultProvider() ) ;
			p.setFilterEnabled( ! forceBox.isSelected() ) ;
		}
}
	public Control getControl() { return this.control ; } ;
	public JFrame getFrame() { return this ; } ;
	public void setChanged() { this.isChanged = true ; } ;
	public void execute()
	{ 
        try {
			this.guiBusy.acquire() ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
	    this.setIconImage( this.programIcon.getImage() ) ;
	    this.setTitle( dvbviewertimerimport.misc.Constants.PROGRAM_NAME ) ;
	    this.setLayout(  new GridBagLayout() ) ;
        
		GridBagConstraints c = null ;
		Insets i = new Insets( 0, 0, 0, 0 );

	    //TitledBorder b = BorderFactory.createTitledBorder("") ;
	    //tabbedPane.setBorder( b ) ;

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weighty    = 1.0 ;
		c.fill       = GridBagConstraints.BOTH ;
		//c.insets     = i ;
	    
		this.getContentPane().add( this.guiPanel, c ) ;
		
		this.guiPanel.init() ;
		

	    

	 	    

		c = new GridBagConstraints();
		i = new Insets( 5, 5, 5, 5 );
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
	    
	    this.executeButton.setText( ResourceManager.msg( "EXECUTE" ) ) ;
	    this.executeButton.addActionListener( new ButtonPressed() ) ;
	    this.getContentPane().add( executeButton, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
	    
	    this.forceBox.setText( ResourceManager.msg( "ALL_TIMERS" ) ) ;
	    this.forceBox.addItemListener( new AllTimersChanged() ) ;
	    this.getContentPane().add( forceBox, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 1 ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
	    
	    this.updateButton.setText( ResourceManager.msg( "UPDATE_LIST" ) ) ;
	    this.updateButton.addActionListener( new ButtonPressed() ) ;
	    this.getContentPane().add( this.updateButton, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 1 ;
		c.weightx    = 1.0 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
	    
	    this.okButton.setText( ResourceManager.msg( "OK" ) ) ;
	    this.okButton.addActionListener( new ButtonPressed() ) ;
	    this.getContentPane().add( this.okButton, c ) ;

		

		c = new GridBagConstraints();
		c.gridx      = 4 ;
		c.gridy      = 1 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
	    
		this.cancelButton.setText( ResourceManager.msg( "CANCEL" ) ) ;
		this.cancelButton.addActionListener( new ButtonPressed() ) ;
		this.getContentPane().add( cancelButton, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 5 ;
		c.gridy      = 1 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
	    
		this.applyButton.setText( ResourceManager.msg( "APPLY" ) ) ;
		this.applyButton.addActionListener( new ButtonPressed() ) ;
		this.getContentPane().add( this.applyButton, c ) ;

        //this.setPreferredSize( new Dimension( 800,500 ) ) ;
		this.pack(); 
		this.setVisible( true );         
        this.setLocationRelativeTo(null);
        
        this.addWindowListener( new MyWindowListener() ) ;
        
        
        this.guiPanel.updateExecuteButton() ;
        
	}
	public void updateExecuteButton( boolean enableExecute )
	{
		if ( enableExecute )
		{
			Provider p = Provider.getProvider( control.getDefaultProvider() ) ;
			this.executeButton.setEnabled( p.canExecute() ) ;
			this.executeButton.setVisible( true ) ;
			this.forceBox.setEnabled( p.isFiltered() && p.canExecute() ) ;
			this.forceBox.setVisible( true ) ;
		}
		else
		{
			//this.executeBottom.setEnabled( false ) ;
			this.executeButton.setVisible( false ) ;
			this.forceBox.setVisible( false ) ;
		}
	}
	public GUIStatus waitGUI()
	{
		this.appBusy.release() ;
		this.setEnabled( true ) ;
		try {
			this.guiBusy.acquire() ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.status ;
	}
	public void waitAPP()
	{
		try {
			this.appBusy.acquire() ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setEnabled( false ) ;
		this.guiBusy.release() ;
	}
	private boolean messageIsChanged( String message )
	{
		if ( ! this.isChanged )
			return true ;
		
		int answer = JOptionPane.showConfirmDialog(
		        this, message, 
		        Constants.PROGRAM_NAME, 
		        JOptionPane.YES_NO_OPTION );
		return( answer == JOptionPane.YES_OPTION ) ;
	}
	public ArrayList< String > getLookAndFeelNames() { return lookAndFeelNames ; } ;
	public void setLookAndFeel( String name )
	{
		if ( ! this.lookAndFeelAssignment.containsKey( name ) )
			name = Constants.SYSTEM_LOOK_AND_FEEL_NAME ;
		this.control.setLookAndFeelName( name ) ;
		
		LookAndFeelInfo lfi = this.lookAndFeelAssignment.get( name ) ;
		try {
			UIManager.setLookAndFeel( lfi.getClassName() ) ;
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI( this );
		this.pack();
		this.isChanged = true ;
	}
	public void updateIfChannelSetsChanged( Provider p )
	{
	 	this.providerAssignment.updateIfChannelSetsChanged( p ) ;
	}
	public void updateIfServiceChanged()
	{
		this.miscellaneous.updateDVBViewerActions() ;
	}
}
