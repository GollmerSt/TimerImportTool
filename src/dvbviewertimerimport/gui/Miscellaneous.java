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
import java.io.File;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;
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

	private final JTextField executablePathText = new JTextField() ;
	private final JButton exeSelectorButton = new JButton() ;
	private final JSpinner channelChangeTimeSpinner = new JSpinner() ;
	private final JSpinner channelWaitTimeSpinner = new JSpinner() ;

	private final JTextField viewParameters = new JTextField() ;
	private final JTextField recordingParameters = new JTextField() ;
	private final JCheckBox startRecording = new JCheckBox() ;
	
	private final JComboBox languageBox    = new JComboBox() ;
	private final JComboBox lookAndFeelBox = new JComboBox() ;
	
	private final JButton exportButton = new JButton() ;
	private final JButton importButton = new JButton() ;
	
	private final JComboBox actionAfterBox = new JComboBox() ;
	private final JComboBox actionTimerBox = new JComboBox() ;
	private final JComboBox timeZoneBox    = new JComboBox() ;
	private final JTextField separatorBox  = new JTextField() ;
	private final JCheckBox maxTitleLengthBox = new JCheckBox() ;
	private final JSpinner maxTitleLengthSpinner = new JSpinner() ;

	private final JButton tvinfoDVBVButton = new JButton() ;
	private final JButton updateToNewVersionButton = new JButton() ;
	private final JCheckBox inActiveIfMerged = new JCheckBox() ;
	
	private final JLabel textInfoLabel = new JLabel() ;
	
	private final ComboSelected comboSelectedAction = new ComboSelected() ;
	
		
	public Miscellaneous( GUIPanel guiPanel )
	{
		super( guiPanel );
	}
	
	class CheckBoxAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox c = (JCheckBox)e.getSource() ;
			
			if ( c == maxTitleLengthBox )
			{
				maxTitleLengthSpinner.setEnabled( maxTitleLengthBox.isSelected() ) ;
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
			else if ( source == exeSelectorButton )
			{
				boolean aborted = ! ( new ExePathSelector( control.getDVBViewer() , guiPanel.getWindow())).show() ;
				if ( aborted )
					return ;
				setInfoText( ResourceManager.msg( "CHANGE_EFFECT" ) ) ;
				executablePathText.setText( control.getDVBViewer().getDVBExePath() ) ;
			}
			else if ( source == exportButton )
			{
				File file = chooseImExportFile( false ) ;
				if ( file == null )
					return ;
				exportButton.setText( ResourceManager.msg( "SUCCESSFULL" ) ) ;
				control.write( file ) ;
			}
			else if ( source == importButton )
			{
				File file = chooseImExportFile( true ) ;
				if ( file == null )
					return ;
				control.setIsImported() ;
				control.copyControlFile( file, true ) ;
				setInfoText( ResourceManager.msg( "CHANGE_EFFECT" ) ) ;
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
		private String string = null ;
		@Override
		public String toString()
		{
			if ( this.string == null)
				this.string = String.format( "(GMT%+03d:00) %s", timeZone.getRawOffset()/1000/60/60,timeZone.getID()) ;
			return string ;
		}
		public String getTimeZoneString() { return timeZone.getID() ; } ;
	} ;

	public void init()
	{		
		Insets i = new Insets( 5, 5, 5, 5 );
		
		TitledBorder  tB = null ;
		GridBagConstraints c = null ;

		JPanel parameterPanel = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( ResourceManager.msg( "DVBVIEWER_PARAS" ) ) ;
		parameterPanel.setBorder( tB ) ;


		JPanel pathPanel = new JPanel( new GridBagLayout() ) ;
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.EAST ;
		c.insets     = i ;

		JLabel pathLabel = new JLabel( ResourceManager.msg( "PATH" ) ) ;
		pathPanel.add( pathLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.EAST ;
		c.insets     = i ;

		this.directoryPathText.setText( control.getDVBViewer().getDVBViewerPath() ) ;
		this.directoryPathText.setPreferredSize( new Dimension( 400, this.directoryPathText.getPreferredSize().height ) ) ;
		this.directoryPathText.setMinimumSize( new Dimension( 400, this.directoryPathText.getPreferredSize().height ) ) ;
		this.directoryPathText.setEditable( false ) ;
		pathPanel.add( directoryPathText, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.EAST ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		Icon directoryIcon = UIManager.getIcon("FileView.directoryIcon") ;
		if ( directoryIcon == null )
			directoryIcon = ResourceManager.createImageIcon( "icons/directoryIcon.png", "directoryIcon" ) ;
		this.fileSelectorButton.setIcon( directoryIcon ) ;
		this.fileSelectorButton.addActionListener( new ButtonsPressed() ) ;
		pathPanel.add( fileSelectorButton, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.EAST ;
		c.insets     = i ;

		JLabel waitCOMLabel = new JLabel( ResourceManager.msg( "WAIT_TIME_COM" ) ) ;
		pathPanel.add( waitCOMLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 4 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		SpinnerModel lengthModel = new SpinnerNumberModel(
				control.getDVBViewer().getWaitCOMTime(), //initial value
				0, 300, 1 ) ;
		this.channelWaitTimeSpinner.setModel( lengthModel );
		pathPanel.add( this.channelWaitTimeSpinner, c ) ;		

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.EAST ;
		c.insets     = i ;

		JLabel exePathLabel = new JLabel( ResourceManager.msg( "EXEC_PATH" ) ) ;
		pathPanel.add( exePathLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.EAST ;
		c.insets     = i ;

		this.executablePathText.setText( control.getDVBViewer().getDVBExePath() ) ;
		this.executablePathText.setPreferredSize( new Dimension( 400, this.directoryPathText.getPreferredSize().height ) ) ;
		this.executablePathText.setMinimumSize( new Dimension( 400, this.directoryPathText.getPreferredSize().height ) ) ;
		this.executablePathText.setEditable( false ) ;
		pathPanel.add( executablePathText, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.EAST ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		directoryIcon = UIManager.getIcon("FileView.directoryIcon") ;
		if ( directoryIcon == null )
			directoryIcon = ResourceManager.createImageIcon( "icons/directoryIcon.png", "directoryIcon" ) ;
		this.exeSelectorButton.setIcon( directoryIcon ) ;
		this.exeSelectorButton.addActionListener( new ButtonsPressed() ) ;
		pathPanel.add( exeSelectorButton, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.EAST ;
		c.insets     = i ;

		JLabel waitChannelLabel = new JLabel( ResourceManager.msg( "CHANNEL_WAIT_TIME" ) ) ;
		pathPanel.add( waitChannelLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 4 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		lengthModel = new SpinnerNumberModel(
				control.getDVBViewer().getChannelChangeTime(), //initial value
				0, 300, 1 ) ;
		this.channelChangeTimeSpinner.setModel( lengthModel );
		pathPanel.add( this.channelChangeTimeSpinner, c ) ;		

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.weightx    = 1.0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = new Insets( 0, 0, 0, 0 ) ;
		
		parameterPanel.add( pathPanel, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 2 ;
		//c.weightx    = 0.25 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.EAST ;
		c.insets     = i ;

		JLabel viewParaLabel = new JLabel( ResourceManager.msg( "VIEW_PARAS" ) ) ;
		parameterPanel.add( viewParaLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 2 ;
		c.weightx    = 1.0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		//c.anchor     = GridBagConstraints.NORTHEAST ;
		c.insets     = i ;

		this.viewParameters.setText( control.getDVBViewer().getViewParameters() ) ;
		//this.viewParameters.setPreferredSize( new Dimension( 400, this.directoryPathText.getPreferredSize().height ) ) ;
		parameterPanel.add( viewParameters, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 2 ;
		//c.weightx    = 0.25 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.EAST ;
		c.insets     = i ;

		JLabel recordingParaLabel = new JLabel( ResourceManager.msg( "RECORDING_PARAS" ) ) ;
		parameterPanel.add( recordingParaLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 2 ;
		c.weightx    = 1.0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor	 = GridBagConstraints.EAST ;
		c.insets     = i ;

		this.recordingParameters.setText( control.getDVBViewer().getRecordingParameters() ) ;
		//this.recordingParameters.setPreferredSize( new Dimension( 400, this.directoryPathText.getPreferredSize().height ) ) ;
		parameterPanel.add( this.recordingParameters, c ) ;
		
		
		c = new GridBagConstraints();
		c.gridx      = 4 ;
		c.gridy      = 2 ;
		//c.weightx    = 1.0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor	 = GridBagConstraints.EAST ;
		c.insets     = i ;

		
		this.startRecording.setText( ResourceManager.msg( "START_DVBVIEWER_ON_RECORDING" ) ) ;
		this.startRecording.setSelected( this.control.getDVBViewer().getStartIfRecording() ) ;
		parameterPanel.add( this.startRecording, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		this.add( parameterPanel, c ) ;

		
		
		if ( this.guiPanel.getLookAndFeelNames() != null && true)
		{

			JPanel guiPanel = new JPanel( new GridBagLayout() ) ;
		
			tB = BorderFactory.createTitledBorder( ResourceManager.msg( "GUI" ) ) ;
			guiPanel.setBorder( tB ) ;


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
			
			this.lookAndFeelBox.addActionListener( comboSelectedAction ) ;
			
			guiPanel.add( this.lookAndFeelBox, c ) ;

			c = new GridBagConstraints();
			c.gridx      = 0 ;
			c.gridy      = 1 ;
			//c.gridwidth  = GridBagConstraints.REMAINDER ;
			c.weightx    = 0.5 ;
			c.weighty    = 1.0 ;
			c.anchor     = GridBagConstraints.NORTHEAST ;
			c.fill       = GridBagConstraints.BOTH ;
			this.add( guiPanel, c ) ;
		}

		
		

		JPanel controlFilePanel = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( ResourceManager.msg( "CONTROL_FILE" ) ) ;
		controlFilePanel.setBorder( tB ) ;
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weightx    = 0.5 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.exportButton.addActionListener( new ButtonsPressed() ) ;
		this.exportButton.setText( ResourceManager.msg( "EXPORT" ) ) ;
		controlFilePanel.add( exportButton, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weightx    = 0.5 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.importButton.addActionListener( new ButtonsPressed() ) ;
		this.importButton.setText( ResourceManager.msg( "IMPORT" ) ) ;
		controlFilePanel.add( importButton, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 2 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.gridheight = GridBagConstraints.REMAINDER ;
		c.weightx    = 0.5 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.BOTH ;
		this.add( controlFilePanel, c ) ;

		
		
		
		
		JPanel dvbViewerPanel = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( ResourceManager.msg( "DVBVIEWER" ) ) ;
		dvbViewerPanel.setBorder( tB ) ;

		
		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weightx    = 0.5 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.tvinfoDVBVButton.addActionListener( new ButtonsPressed() ) ;
		dvbViewerPanel.add( this.tvinfoDVBVButton, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 2 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.updateToNewVersionButton.addActionListener( new ButtonsPressed() ) ;
		dvbViewerPanel.add( this.updateToNewVersionButton, c ) ;


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
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		dvbViewerPanel.add( this.timeZoneBox, c ) ;
		this.timeZoneBox.addActionListener( comboSelectedAction ) ;

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
		c.gridwidth  = GridBagConstraints.REMAINDER ;
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
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.updateDVBViewerActions() ;
		dvbViewerPanel.add( this.actionTimerBox, c ) ;

		JPanel titleLengthPanel = new JPanel( new GridBagLayout() ) ;
		
		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 6 ;
		c.weightx    = 0.0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		c.anchor     = GridBagConstraints.EAST ;
		
		this.maxTitleLengthBox.setText( ResourceManager.msg( "LIMIT_TITLE_LENGTH" ) ) ;
		this.maxTitleLengthBox.addActionListener( new CheckBoxAction() ) ;
		this.maxTitleLengthBox.setSelected( control.getMaxTitleLength() >= 0 ) ;
		titleLengthPanel.add( this.maxTitleLengthBox, c ) ;
		
		


		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 6 ;
		c.weightx    = 1.0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		lengthModel = new SpinnerNumberModel(
				control.getMaxTitleLength()< 20?20:control.getMaxTitleLength(), //initial value
				20, 256, 1 ) ;
		this.maxTitleLengthSpinner.setModel( lengthModel );
		this.maxTitleLengthSpinner.setEnabled( control.getMaxTitleLength() >= 0 ) ;
		titleLengthPanel.add( this.maxTitleLengthSpinner, c ) ;		

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 6 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = new Insets( 0, 0, 0, 0 ); ;
		dvbViewerPanel.add( titleLengthPanel, c ) ;

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 7 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.EAST ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel separatorLabel = new JLabel( ResourceManager.msg( "SEPARATOR" ) ) ;
		dvbViewerPanel.add( separatorLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 7 ;
		c.weightx    = 1.0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.separatorBox.setText( control.getSeparator() ) ;
		dvbViewerPanel.add( this.separatorBox, c ) ;
		
		


		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 7 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		JLabel inActiveIfMegedLabel = new JLabel( ResourceManager.msg( "INACTIVEIFMERGED" ) ) ;
		dvbViewerPanel.add( inActiveIfMegedLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 7 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.inActiveIfMerged.setSelected( DVBViewerEntry.getInActiveIfMerged() ) ;
		dvbViewerPanel.add( this.inActiveIfMerged, c ) ;
		

	
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		c.gridheight = 2 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.gridheight = GridBagConstraints.REMAINDER ;
		//c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.BOTH ;
		this.add( dvbViewerPanel, c ) ;
		

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 3 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.gridheight = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.textInfoLabel.setForeground( SystemColor.RED ) ;
		this.textInfoLabel.setPreferredSize( new Dimension( 1,20 ) ) ;

		this.add( textInfoLabel, c ) ;
		
		this.createCombosContent() ;	
	}
	public void createCombosContent()
	{
		MyComboBoxItem deflt = null ;
		for ( Locale l : ResourceManager.getAvailableLocales( "lang" ) )
		{
			MyComboBoxItem mc  = new MyComboBoxItem( l ) ;
			this.languageBox.addItem( mc ) ;
			if ( l.equals( control.getLanguage() ) )
				deflt = mc;
		}
		this.languageBox.setSelectedItem( deflt ) ;
		this.languageBox.addActionListener( comboSelectedAction ) ;

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
		this.updateToNewVersionButton.setText(   ResourceManager.msg( "UPDATE_CHANNELS" ) ) ;
		this.exportButton.setText( ResourceManager.msg( "EXPORT" ) ) ;
		this.setInfoText( "" ) ;
	}
	@Override
	public void update( boolean active )
	{
		if ( active )
		{
			if ( this.control.getSeparator() == null || ( ! this.control.getSeparator().equals( this.separatorBox.getText() ) ) )
			{
				this.control.setSeparator( this.separatorBox.getText() ) ;
				this.guiPanel.setChanged() ;
			}
			int length = (Integer) this.maxTitleLengthSpinner.getModel().getValue() ;
			if ( ! this.maxTitleLengthBox.isSelected() )
				length = -1 ;
			if ( length != this.control.getMaxTitleLength() )
			{
				this.control.setMaxTitleLength( length ) ;
				this.guiPanel.setChanged() ;
			}
			if ( ! this.control.getDVBViewer().getViewParameters().equals( this.viewParameters.getText() ))
			{
				this.control.getDVBViewer().setViewParameters( this.viewParameters.getText() ) ;
				this.guiPanel.setChanged() ;
			}
			if ( ! this.control.getDVBViewer().getRecordingParameters().equals( this.recordingParameters.getText() ))
			{
				this.control.getDVBViewer().setRecordingParameters( this.recordingParameters.getText() ) ;
				this.guiPanel.setChanged() ;
			}
			if ( this.control.getDVBViewer().getStartIfRecording() != ( this.startRecording.isSelected() ) )
			{
				this.control.getDVBViewer().setStartIfRecording( this.startRecording.isSelected() ) ;
				this.guiPanel.setChanged() ;
			}
			if ( DVBViewerEntry.getInActiveIfMerged() != this.inActiveIfMerged.isSelected() )
			{
				DVBViewerEntry.setInActiveIfMerged( this.inActiveIfMerged.isSelected() ) ;
				this.guiPanel.setChanged() ;
			}
			
			int channelWaitTime = (Integer) this.channelChangeTimeSpinner.getModel().getValue() ;
			if ( this.control.getDVBViewer().getChannelChangeTime() != channelWaitTime )
				this.control.getDVBViewer().setChannelChangeTime( channelWaitTime ) ;
			
			int waitCOMTime = (Integer) this.channelWaitTimeSpinner.getModel().getValue() ;
			if ( this.control.getDVBViewer().getWaitCOMTime() != waitCOMTime )
				this.control.getDVBViewer().setWaitCOMTime( waitCOMTime ) ;
		}
		this.updateDVBViewerActions() ;
		this.updateText() ;
	}
	private File chooseImExportFile( boolean isImport)
	{
		final String description ;
		if ( isImport )
			description = ResourceManager.msg( "IMPORT_FILE_SEL_HEADER" ) ;
		else
			description = ResourceManager.msg( "EXPORT_FILE_SEL_HEADER" ) ;
		
		final JFileChooser chooser = new JFileChooser( (File)null ) ;
		
		chooser.setFileFilter(new FileFilter()
		{
			public boolean accept(File f)
			{
				String parts[] = f.getName().split( "\\." ) ;
				if ( parts.length < 2 )
					return false ;
				return parts[ parts.length - 1 ].equals( "xml" ) ;
			}
			public String getDescription()
			{
				return ResourceManager.msg( "EXPORT_FILE_DESCRIPTION" ) ;
			}
		} ) ;

		chooser.setDialogTitle( description ) ;
		
		int returnVal = chooser.showDialog( this.guiPanel.getWindow(), ResourceManager.msg( "SELECT" ) ) ;
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile() ;
			return file ;
		}
		else
			return null ;
	}
}
