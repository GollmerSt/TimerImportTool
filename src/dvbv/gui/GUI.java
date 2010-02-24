// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.Semaphore;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dvbv.Resources.ResourceManager;
import dvbv.control.Control;
import dvbv.misc.Constants;
import dvbv.provider.Provider;

public class GUI {
	public enum GUIStatus { INVALID, OK, CANCEL, APPLY, EXECUTE, SAVE_EXECUTE } ;
	private final Control control ;
	private final dvbv.dvbviewer.channels.Channels dChannels ;

	private ImageIcon programIcon   = ResourceManager.createImageIcon( "icons/dvbViewer Programm16.png", "DVBViewer icon" ) ;

	private final Semaphore guiBusy = new Semaphore(1) ;
	private final Semaphore appBusy = new Semaphore(1) ;
	
	private GUIStatus status = GUIStatus.INVALID ;
	private boolean isChanged = false ;
	private MyTabPanel previousTab = null ;

	private final JFrame frame = new JFrame() ;
	private final JTabbedPane tabbedPane = new JTabbedPane() ;
	private final JCheckBox forceBox = new JCheckBox() ;
    private final JButton executeButton = new JButton() ;
    private final JButton okButton = new JButton() ;
    private final JButton cancelButton = new JButton() ;
    private final JButton applyButton = new JButton() ;
    
    private class TabChanged implements ChangeListener
    {
		@Override
		public void stateChanged(ChangeEvent e) {
			updateExecuteButton() ;
			MyTabPanel source = (MyTabPanel)((JTabbedPane)e.getSource()).getSelectedComponent() ;
			if ( source == null )
				return ;
			previousTab.update( false );
			source.update( true ) ;
			previousTab = source ;
		}
    	
    }
	public class ButtonPressed implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			MyTabPanel tabPanel = (MyTabPanel)tabbedPane.getSelectedComponent() ;
			tabPanel.update( false ) ;

			JButton button = (JButton) e.getSource() ;
			
			if ( button == okButton )
			{
				status = GUIStatus.OK ;
				waitAPP() ;
			}
			else if ( button == cancelButton && messageIsChanged( GUIStrings.setupChanged()) )
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
				if ( messageIsChanged( GUIStrings.setupSave() ))
					status = GUIStatus.SAVE_EXECUTE ;
				else
					status = GUIStatus.EXECUTE ;
				waitAPP() ;
			}
		}
	}
	public class AllTimersChanged implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e) {
			Provider p = Provider.getProvider( control.getDefaultProvider() ) ;
			p.setHistorie( ! forceBox.isSelected() ) ;
		}
}
	public GUI( Control control, dvbv.dvbviewer.channels.Channels dChannels )
	{
		this.control = control ;
		this.dChannels = dChannels ;
	}
	public Control getControl() { return this.control ; } ;
	public JFrame getFrame() { return frame ; } ;
	public void setChanged() { this.isChanged = true ; } ;
	public void execute()
	{ 
        try {
			this.guiBusy.acquire() ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    this.frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	    this.frame.setIconImage( this.programIcon.getImage() ) ;
	    this.frame.setTitle( dvbv.misc.Constants.PROGRAM_NAME ) ;
	    this.frame.setLayout(  new GridBagLayout() ) ;
        
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
	    
		this.frame.add( this.tabbedPane, c ) ;

	    
	    DVBViewerAssignment tab1 = new DVBViewerAssignment( this, dChannels ) ;
	    ProviderService tab2 = new ProviderService( this, frame ) ;
	    Miscellaneous tab3 = new Miscellaneous( this, frame ) ;
	    ProviderAssignment tab4 = new ProviderAssignment( this, frame ) ;
	    	    
	    this.tabbedPane.add( GUIStrings.dvbViewerAssignment(), tab1); 
	    this.tabbedPane.add( GUIStrings.providerService(), tab2);
	    this.tabbedPane.add( GUIStrings.miscellaneous(), tab3);
	    this.tabbedPane.add( GUIStrings.providerAssignment(), tab4);
	    this.tabbedPane.addChangeListener( new TabChanged() ) ;
	    
	    this.tabbedPane.setSelectedComponent( tab1 ) ;
	    this.previousTab = (MyTabPanel)tab1 ;
	    tab1.paint() ;
	    tab2.paint() ;
	    tab3.paint() ;
	    tab4.paint() ;

	 	    

		c = new GridBagConstraints();
		i = new Insets( 5, 5, 5, 5 );
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
	    
	    this.executeButton.setText( GUIStrings.execute() ) ;
	    this.executeButton.addActionListener( new ButtonPressed() ) ;
	    this.frame.add( executeButton, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
	    
	    this.forceBox.setText( GUIStrings.allTimers() ) ;
	    this.forceBox.addItemListener( new AllTimersChanged() ) ;
	    this.frame.add( forceBox, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 1 ;
		c.weightx    = 1.0 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
	    
	    this.okButton.setText( GUIStrings.ok() ) ;
	    this.okButton.addActionListener( new ButtonPressed() ) ;
	    this.frame.add( this.okButton, c ) ;

		

		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 1 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
	    
		this.cancelButton.setText( GUIStrings.cancel() ) ;
		this.cancelButton.addActionListener( new ButtonPressed() ) ;
		this.frame.add( cancelButton, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 4 ;
		c.gridy      = 1 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;
	    
		this.applyButton.setText( GUIStrings.apply() ) ;
		this.applyButton.addActionListener( new ButtonPressed() ) ;
		this.frame.add( this.applyButton, c ) ;

		this.frame.pack(); 
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.frame.setLocation( (d.width  - this.frame.getSize().width  ) / 2, 
        		                (d.height - this.frame.getSize().height ) / 2 ) ;
		this.frame.setVisible( true );         
	}
	public void updateExecuteButton()
	{
		Object o = this.tabbedPane.getSelectedComponent() ;
		if ( o != null && o.getClass() == DVBViewerAssignment.class )
		{
			Provider p = Provider.getProvider( control.getDefaultProvider() ) ;
			this.executeButton.setEnabled( p.canExecute() ) ;
			this.executeButton.setVisible( true ) ;
			this.forceBox.setEnabled( p.hasHistory() ) ;
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
		this.frame.setEnabled( true ) ;
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
		this.frame.setEnabled( false ) ;
		this.guiBusy.release() ;
	}
	private boolean messageIsChanged( String message )
	{
		if ( ! this.isChanged )
			return true ;
		
		int answer = JOptionPane.showConfirmDialog(
		        frame, message, 
		        Constants.PROGRAM_NAME, 
		        JOptionPane.YES_NO_OPTION );
		return( answer == JOptionPane.YES_OPTION ) ;
	}
	
}
