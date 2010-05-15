// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.importer.TVInfoDVBV;
import dvbviewertimerimport.importer.UpdateImporter;
import dvbviewertimerimport.misc.Enums.ActionAfterItems;
import dvbviewertimerimport.misc.Enums.TimerActionItems;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.misc.TerminateClass;

public class Miscellaneous extends MyTabPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6271528915024437692L;
	
	private final JTextField directoryPathText = new JTextField() ;
	private final JButton fileSelectorButton = new JButton() ;
	
	private final JComboBox languageBox    = new JComboBox() ;
	private final JComboBox lookAndFeelBox = new JComboBox() ;
	private final JComboBox actionAfterBox = new JComboBox() ;
	private final JComboBox actionTimerBox = new JComboBox() ;
	private final JComboBox timeZoneBox    = new JComboBox() ;
	private final JTextField separatorBox  = new JTextField() ;
	private final JButton tvinfoDVBVButton = new JButton() ;
	private final JButton updateToNewVersionButton = new JButton() ;
	private final JButton updateChannelsFromDVBViewer = new JButton() ;
	
	private boolean wasSelected = false ;
	
	private final JLabel textInfoLabel = new JLabel() ;
	
	private final ComboSelected comboSelectedAction = new ComboSelected() ;
	
		
	public Miscellaneous( GUIPanel guiPanel )
	{
		super( guiPanel );
	}
	class ComboCompChanged implements ComponentListener
	{
		@Override
		public void componentHidden(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void componentMoved(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void componentResized(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void componentShown(ComponentEvent e)
		{
			if ( ! wasSelected )
			{
				MyComboBoxItem deflt = null ;
				for ( Locale l : ResourceManager.getAvailableLocales( "lang" ) )
				{
					MyComboBoxItem mc  = new MyComboBoxItem( l ) ;
					languageBox.addItem( mc ) ;
					if ( l.equals( control.getLanguage() ) )
						deflt = mc;
				}
				languageBox.setSelectedItem( deflt ) ;

				wasSelected = true ;
				Object oDefault = null ;
				TimeZone timeZoneDefault = DVBViewer.getTimeZone() ;
				String defaultTimeZoneString = timeZoneDefault.getID() ;
				for ( final String tzS : TimeZone.getAvailableIDs() )
				{
					MyTimeZoneObject o = new MyTimeZoneObject( TimeZone.getTimeZone( tzS ) ) ;
					if ( tzS.equals( defaultTimeZoneString ) )
						oDefault = o ;
					timeZoneBox.addItem( o ) ;
				}
				timeZoneBox.setSelectedItem( oDefault ) ;
			}
		}
	}
	class ComboSelected implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox c = (JComboBox)e.getSource() ;
			
			Object o = c.getSelectedItem() ;
			
			if ( o == null )
				return ;
			
			if ( c == languageBox )
			{
				Locale internal = ((MyComboBoxItem)o).getLocale() ;
				if ( internal != control.getLanguage() )
				{
					control.setLanguage( internal ) ;
					guiPanel.setChanged() ;
					setInfoText( ResourceManager.msg( "CHANGE_EFFECT" ) ) ;
					
				}
			}
			
			else if ( c == actionAfterBox )
			{
				control.getDVBViewer().setAfterRecordingAction( (ActionAfterItems)o ) ;
				guiPanel.setChanged() ;
			}
			else if ( c == actionTimerBox )
			{
				control.getDVBViewer().setTimerAction( (TimerActionItems)o ) ;
				guiPanel.setChanged() ;
			}
			else if ( c == lookAndFeelBox )
				guiPanel.setLookAndFeel( (String)o ) ;
			else if ( c == timeZoneBox )
				DVBViewer.setTimeZone( TimeZone.getTimeZone( ((MyTimeZoneObject) timeZoneBox.getSelectedItem()).getTimeZoneString() )) ;
		}
	}
	class ButtonsPressed implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e )
		{
			updateText() ;

			JButton source = (JButton)e.getSource() ;
			if ( source == tvinfoDVBVButton )
			{
				try
				{
					new TVInfoDVBV( control ) ;
					tvinfoDVBVButton.setText( ResourceManager.msg( "SUCCESSFULL" ) ) ;
					guiPanel.setChanged() ;
					guiPanel.updateIfChannelSetsChanged( null ) ;
				} catch ( ErrorClass er ) {
					tvinfoDVBVButton.setText( ResourceManager.msg( "FAILED" ) ) ;
					setInfoText( ResourceManager.msg( "ERROR_READING_FILE" ) + "\"" + er.getErrorString() + "\"." ) ;
				}
			}
			else if ( source == updateToNewVersionButton )
			{
				UpdateImporter importer = new UpdateImporter( control ) ;
				boolean isImported = false ;
				try {
					isImported = importer.importXML() ;
				} catch (TerminateClass e1) {
				}
				if ( ! isImported )
					updateToNewVersionButton.setText( ResourceManager.msg( "SUCCESSFULL" ) ) ;
				else
				{
					setInfoText( ResourceManager.msg( "MORE_NOFO_LOG" ) + "\"" + Log.getFile() + "\"" ) ;
					updateToNewVersionButton.setText( ResourceManager.msg( "WARNING" ) ) ;
				}
				guiPanel.updateIfChannelSetsChanged( null ) ;
				guiPanel.setChanged() ;
			}
			else if ( source == fileSelectorButton )
			{
				boolean aborted = ! ( new WorkPathSelector( control.getDVBViewer() , guiPanel.getWindow())).show() ;
				if ( aborted )
					return ;
				setInfoText( ResourceManager.msg( "CHANGE_EFFECT" ) ) ;
				directoryPathText.setText( control.getDVBViewer().getDVBViewerPath() ) ;
			}
			else if ( source == updateChannelsFromDVBViewer )
			{
				try
				{
					control.getDVBViewer().getChannels().read() ;
					guiPanel.updateDVBViewerChannels() ;
					updateChannelsFromDVBViewer.setText( ResourceManager.msg( "SUCCESSFULL" ) ) ;
					guiPanel.updateIfChannelSetsChanged( null ) ;
				} catch ( ErrorClass er )
				{
					setInfoText( ResourceManager.msg( "ERROR_READING_FILE_NNNN", "channel.dat" ) ) ;
					updateChannelsFromDVBViewer.setText( ResourceManager.msg( "ERROR_READING_FILE" ) ) ;
				}
			}
		}
		
	}
	private void setInfoText( String info )
	{
		this.textInfoLabel.setText( info ) ;
	}
	class MyComboBoxItem
	{
		private final Locale locale ;
		
		MyComboBoxItem( final Locale l )
		{
			this.locale = l ;
		}
		
		@Override
		public String toString() { return locale.getDisplayName() ; } ;
		
		public Locale getLocale() { return locale ; } ;
	}
	class MyTimeZoneObject
	{
		MyTimeZoneObject( final TimeZone timeZone )
		{
			this.timeZone = timeZone ;
		}
		private final TimeZone timeZone ;
		@Override
		public String toString()
		{
			return String.format( "(GMT%+03d:00) %s", timeZone.getRawOffset()/1000/60/60,timeZone.getID()) ;
		}
		public String getTimeZoneString() { return timeZone.getID() ; } ;
	} ;

	public void paint()
	{		
		Insets i = new Insets( 5, 5, 5, 5 );
		
		TitledBorder  tB = null ;
		GridBagConstraints c = null ;

		JPanel pathPanel = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( ResourceManager.msg( "DVBVIEWER_PATH" ) ) ;
		pathPanel.setBorder( tB ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.directoryPathText.setText( control.getDVBViewer().getDVBViewerPath() ) ;
		this.directoryPathText.setPreferredSize( new Dimension( 400,
				this.directoryPathText.getPreferredSize().height ) ) ;
		this.directoryPathText.setEditable( false ) ;
		pathPanel.add( directoryPathText, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		Icon directoryIcon = UIManager.getIcon("FileView.directoryIcon") ;
		if ( directoryIcon == null )
			directoryIcon = ResourceManager.createImageIcon( "icons/directoryIcon.png", "directoryIcon" ) ;
		this.fileSelectorButton.setIcon( directoryIcon ) ;
		this.fileSelectorButton.addActionListener( new ButtonsPressed() ) ;
		pathPanel.add( fileSelectorButton, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		this.add( pathPanel, c ) ;


		
		
		JPanel guiPanel = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( ResourceManager.msg( "GUI" ) ) ;
		guiPanel.setBorder( tB ) ;


		if ( this.guiPanel.getLookAndFeelNames() != null && true )
		{

			c = new GridBagConstraints();
			c.gridx      = 0 ;
			c.gridy      = 0 ;
			//c.gridwidth  = GridBagConstraints.REMAINDER ;
			//c.fill       = GridBagConstraints.HORIZONTAL ;
			c.insets     = i ;

			JLabel languageLabel = new JLabel( ResourceManager.msg( "LANGUAGE" ) ) ;
			guiPanel.add( languageLabel, c ) ;


			c = new GridBagConstraints();
			c.gridx      = 1 ;
			c.gridy      = 0 ;
			//c.gridwidth  = GridBagConstraints.REMAINDER ;
			c.fill       = GridBagConstraints.HORIZONTAL ;
			c.insets     = i ;

			this.languageBox.addActionListener( new ComboSelected() ) ;
			guiPanel.add( this.languageBox, c ) ;

		
			c = new GridBagConstraints();
			c.gridx      = 0 ;
			c.gridy      = 1 ;
			//c.gridwidth  = GridBagConstraints.REMAINDER ;
			//c.fill       = GridBagConstraints.HORIZONTAL ;
			c.insets     = i ;

			JLabel lookAndFeelLabel = new JLabel( ResourceManager.msg( "VIEW" ) ) ;
			guiPanel.add( lookAndFeelLabel, c ) ;


			c = new GridBagConstraints();
			c.gridx      = 1 ;
			c.gridy      = 1 ;
			//c.gridwidth  = GridBagConstraints.REMAINDER ;
			c.fill       = GridBagConstraints.HORIZONTAL ;
			c.insets     = i ;
			
			for ( String lf : this.guiPanel.getLookAndFeelNames() )
				this.lookAndFeelBox.addItem( lf ) ;
			
			this.lookAndFeelBox.setSelectedItem( this.control.getLookAndFeelName() ) ;
			
			this.lookAndFeelBox.addActionListener( new ComboSelected() ) ;
			
			guiPanel.add( this.lookAndFeelBox, c ) ;
		}

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weightx    = 0.5 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.BOTH ;
		this.add( guiPanel, c ) ;


		
		
		JPanel dvbViewerPanel = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( ResourceManager.msg( "DVBVIEWER" ) ) ;
		dvbViewerPanel.setBorder( tB ) ;

		
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weightx    = 0.5 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.tvinfoDVBVButton.addActionListener( new ButtonsPressed() ) ;
		dvbViewerPanel.add( tvinfoDVBVButton, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weightx    = 0.5 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.updateChannelsFromDVBViewer.addActionListener( new ButtonsPressed() ) ;
		dvbViewerPanel.add( updateChannelsFromDVBViewer, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 2 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.updateToNewVersionButton.addActionListener( new ButtonsPressed() ) ;
		dvbViewerPanel.add( updateToNewVersionButton, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 3 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.EAST ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel timeZoneLabel = new JLabel( ResourceManager.msg( "TIMEZONE" ) ) ;
		dvbViewerPanel.add( timeZoneLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 3 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		dvbViewerPanel.add( this.timeZoneBox, c ) ;
		this.timeZoneBox.addActionListener( this.comboSelectedAction ) ;
		this.addComponentListener( new ComboCompChanged() ) ;

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 4 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.EAST ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel actionAfterLabel = new JLabel( ResourceManager.msg( "ACTION_AFTER" ) ) ;
		dvbViewerPanel.add( actionAfterLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 4 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
//		this.updateDVBViewerActions() ;
		dvbViewerPanel.add( this.actionAfterBox, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 5 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.EAST ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel actionTimerLabel = new JLabel( ResourceManager.msg( "ACTION_TIMER" ) ) ;
		dvbViewerPanel.add( actionTimerLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 5 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.updateDVBViewerActions() ;
		dvbViewerPanel.add( this.actionTimerBox, c ) ;

		

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 6 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.EAST ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel separatorLabel = new JLabel( ResourceManager.msg( "SEPARATOR" ) ) ;
		dvbViewerPanel.add( separatorLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 6 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.separatorBox.setText( control.getSeparator() ) ;
		dvbViewerPanel.add( separatorBox, c ) ;

	
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		c.weightx    = 0.5 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		this.add( dvbViewerPanel, c ) ;
		

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 2 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		textInfoLabel.setForeground( SystemColor.RED ) ;
		textInfoLabel.setPreferredSize( new Dimension( 600,16 ) ) ;

		this.add( textInfoLabel, c ) ;
}
	public void updateDVBViewerActions()
	{
		boolean isService = this.control.getDVBViewer().getService().isEnabled() ;
		this.actionAfterBox.removeActionListener( comboSelectedAction ) ;
		this.actionAfterBox.removeAllItems() ;
		for (ActionAfterItems a : ActionAfterItems.values( isService ) )
			this.actionAfterBox.addItem( a ) ;
		
		ActionAfterItems after = this.control.getDVBViewer().getAfterRecordingAction() ;
		if ( after != after.get( isService ) )
		{
			this.actionAfterBox.addActionListener( comboSelectedAction ) ;
			this.actionAfterBox.setSelectedItem( after.get( isService ) ) ;
		}
		else
		{
			this.actionAfterBox.setSelectedItem( after.get( isService ) ) ;
			this.actionAfterBox.addActionListener( comboSelectedAction ) ;
		}

		this.actionTimerBox.removeActionListener( comboSelectedAction ) ;
		this.actionTimerBox.removeAllItems() ;
		for (TimerActionItems a : TimerActionItems.values( isService ) )
			this.actionTimerBox.addItem( a ) ;
		TimerActionItems timer = this.control.getDVBViewer().getTimerAction() ;
		if ( timer != timer.get( isService ) )
		{
			this.actionTimerBox.addActionListener( comboSelectedAction ) ;
			this.actionTimerBox.setSelectedItem( timer.get( isService ) ) ;
		}
		else
		{
			this.actionTimerBox.setSelectedItem( timer.get( isService ) ) ;
			this.actionTimerBox.addActionListener( comboSelectedAction ) ;
		}
	}
	private void updateText()
	{
		this.tvinfoDVBVButton.setText(   ResourceManager.msg( "IMPORT_TV" )
                + "\"" + TVInfoDVBV.NAME_IMPORTFILE + "\"" ) ;
		this.updateChannelsFromDVBViewer.setText( ResourceManager.msg( "UPDATE_DVBV_CHANNELS" ) ) ;
		this.updateToNewVersionButton.setText(   ResourceManager.msg( "UPDATE_CHANNELS" ) ) ;
		this.setInfoText( "" ) ;
	}
	@Override
	public void update( boolean active )
	{
		if ( ! active )
		{
			if ( ! this.control.getSeparator().equals( this.separatorBox.getText() ) )
			{
				this.control.setSeparator( this.separatorBox.getText() ) ;
				this.guiPanel.setChanged() ;
			}
		}
		this.updateDVBViewerActions() ;
		this.updateText() ;
	}
}
