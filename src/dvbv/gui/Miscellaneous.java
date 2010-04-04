// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import dvbv.Resources.ResourceManager;
import dvbv.dvbviewer.DVBViewer;
import dvbv.gui.GUIStrings.ActionAfterItems;
import dvbv.gui.GUIStrings.Language;
import dvbv.gui.GUIStrings.TimerActionItems;
import dvbv.importer.TVInfoDVBV;
import dvbv.importer.UpdateImporter;
import dvbv.misc.ErrorClass;
import dvbv.misc.Log;

public class Miscellaneous extends MyTabPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6271528915024437692L;
	
	private final JCheckBox defaultDirectoryBox = new JCheckBox() ;
	private final JTextField directoryPathText = new JTextField() ;
	private final JButton fileSelectorButton = new JButton() ;
	
	private final JComboBox languageBox = new JComboBox() ;
	private final JComboBox lookAndFeelBox = new JComboBox() ;
	private final JComboBox actionAfterBox = new JComboBox() ;
	private final JComboBox actionTimerBox = new JComboBox() ;
	private final JTextField separatorBox = new JTextField() ;
	private final JButton tvinfoDVBVButton = new JButton() ;
	private final JButton updateToNewVersionButton = new JButton() ;
	
	private final JLabel textInfoLabel = new JLabel() ;
	
	private final ComboSelected comboSelectedAction = new ComboSelected() ;
	
		
	public Miscellaneous( GUI gui, JFrame frame)
	{
		super( gui, frame );
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
				String internal = ((Language)o).getShort() ;
				if ( ! internal.equals( control.getLanguage() ) )
				{
					control.setLanguage( internal ) ;
					gui.setChanged() ;
					setInfoText( GUIStrings.CHANGE_EFFECT.toString() ) ;
					
				}
			}
			
			else if ( c == actionAfterBox )
			{
				control.getDVBViewer().setAfterRecordingAction( (ActionAfterItems)o ) ;
				gui.setChanged() ;
			}
			else if ( c == actionTimerBox )
			{
				control.getDVBViewer().setTimerAction( (TimerActionItems)o ) ;
				gui.setChanged() ;
			}
			else if ( c == lookAndFeelBox )
				gui.setLookAndFeel( (String)o ) ;
		}
	}
	class ButtonsPressed implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e )
		{
			JButton source = (JButton)e.getSource() ;
			if ( source == tvinfoDVBVButton )
			{
				try
				{
					new TVInfoDVBV( control ) ;
					tvinfoDVBVButton.setText( GUIStrings.SUCCESSFULL.toString() ) ;
					gui.setChanged() ;
					gui.updateIfChannelSetsChanged( null ) ;
				} catch ( ErrorClass er ) {
					tvinfoDVBVButton.setText( GUIStrings.FAILED.toString() ) ;
					setInfoText( GUIStrings.ERROR_READING_FILE + "\"" + er.getErrorString() + "\"." ) ;
				}
			}
			if ( source == updateToNewVersionButton )
			{
				UpdateImporter importer = new UpdateImporter( control ) ;
				if ( ! importer.importXML() )
					updateToNewVersionButton.setText( GUIStrings.SUCCESSFULL.toString() ) ;
				else
				{
					setInfoText( GUIStrings.MORE_NOFO_LOG.toString() + "\"" + Log.getFile() + "\"" ) ;
					updateToNewVersionButton.setText( GUIStrings.WARNING.toString() ) ;
				}
				gui.updateIfChannelSetsChanged( null ) ;
				gui.setChanged() ;
			}
			else if ( source == fileSelectorButton )
			{
				boolean aborted = ! ( new WorkPathSelector( control.getDVBViewer() , gui.getFrame())).show() ;
				if ( aborted )
					return ;
				setInfoText( GUIStrings.CHANGE_EFFECT.toString() ) ;
				directoryPathText.setText( control.getDVBViewer().getDataPath() ) ;
			}
		}
		
	}
	public class CheckBoxesChanged implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e) {
			JCheckBox source = (JCheckBox)e.getSource() ;
			boolean isSelected = source.isSelected() ;
			
			if ( source == defaultDirectoryBox )
			{
				DVBViewer dvbViewer = control.getDVBViewer() ;
				fileSelectorButton.setEnabled( ! isSelected ) ;

				if ( dvbViewer.setPathFileIsUsed( !isSelected ) )
				{
					directoryPathText.setText( dvbViewer.getDataPath() ) ;
					setInfoText( GUIStrings.CHANGE_EFFECT.toString() ) ;
					gui.setChanged() ;
				}
				else
					source.setSelected( ! dvbViewer.isPathFileUsed() ) ;
			}

		}
		
	}
	private void setInfoText( String info )
	{
		this.textInfoLabel.setText( info ) ;
	}
	public void paint()
	{		
		Insets i = new Insets( 5, 5, 5, 5 );
		
		TitledBorder  tB = null ;
		GridBagConstraints c = null ;

		JPanel pathPanel = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( GUIStrings.DATA_PATH.toString() ) ;
		pathPanel.setBorder( tB ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.defaultDirectoryBox.setText( GUIStrings.DVBVIEWER.toString() ) ;
		this.defaultDirectoryBox.addItemListener( new CheckBoxesChanged() ) ;
		this.defaultDirectoryBox.setSelected( ! control.getDVBViewer().isPathFileUsed() ) ;
		pathPanel.add( defaultDirectoryBox, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.directoryPathText.setText( control.getDVBViewer().getDataPath() ) ;
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
		
		tB = BorderFactory.createTitledBorder( GUIStrings.GUI.toString() ) ;
		guiPanel.setBorder( tB ) ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel languageLabel = new JLabel( GUIStrings.LANGUAGE.toString() ) ;
		guiPanel.add( languageLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		for ( Language l : Language.values() )
			this.languageBox.addItem( l ) ;
		this.languageBox.setSelectedItem( GUIStrings.languageEnum ) ;
		this.languageBox.addActionListener( new ComboSelected() ) ;
		guiPanel.add( this.languageBox, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel lookAndFeelLabel = new JLabel( GUIStrings.VIEW.toString() ) ;
		guiPanel.add( lookAndFeelLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		for ( String lf : gui.getLookAndFeelNames() )
			this.lookAndFeelBox.addItem( lf ) ;
		
		this.lookAndFeelBox.setSelectedItem( this.control.getLookAndFeelName() ) ;
		
		this.lookAndFeelBox.addActionListener( new ComboSelected() ) ;
		
		this.lookAndFeelBox.setSelectedItem( GUIStrings.languageEnum ) ;
		guiPanel.add( this.lookAndFeelBox, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.weightx    = 0.5 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.BOTH ;
		this.add( guiPanel, c ) ;


		
		
		JPanel dvbViewerPanel = new JPanel( new GridBagLayout() ) ;
		
		tB = BorderFactory.createTitledBorder( GUIStrings.DVBVIEWER.toString() ) ;
		dvbViewerPanel.setBorder( tB ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.tvinfoDVBVButton.addActionListener( new ButtonsPressed() ) ;
		dvbViewerPanel.add( tvinfoDVBVButton, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.updateToNewVersionButton.addActionListener( new ButtonsPressed() ) ;
		dvbViewerPanel.add( updateToNewVersionButton, c ) ;


				c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 2 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel actionAfterLabel = new JLabel( GUIStrings.ACTION_AFTER.toString() ) ;
		dvbViewerPanel.add( actionAfterLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 2 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.updateDVBViewerActions() ;
		dvbViewerPanel.add( this.actionAfterBox, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 3 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel actionTimerLabel = new JLabel( GUIStrings.ACTION_TIMER.toString() ) ;
		dvbViewerPanel.add( actionTimerLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 3 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.updateDVBViewerActions() ;
		dvbViewerPanel.add( this.actionTimerBox, c ) ;

		

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 4 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel separatorLabel = new JLabel( GUIStrings.SEPARATOR.toString() ) ;
		dvbViewerPanel.add( separatorLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 4 ;
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
		this.actionAfterBox.addActionListener( comboSelectedAction ) ;
		this.actionAfterBox.setSelectedItem( this.control.getDVBViewer().getAfterRecordingAction().get( isService ) ) ;

		this.actionTimerBox.removeActionListener( comboSelectedAction ) ;
		this.actionTimerBox.removeAllItems() ;
		for (TimerActionItems a : TimerActionItems.values( isService ) )
			this.actionTimerBox.addItem( a ) ;
		this.actionTimerBox.addActionListener( comboSelectedAction ) ;
		this.actionTimerBox.setSelectedItem( this.control.getDVBViewer().getTimerAction().get( isService ) ) ;

	}
	private void updateText()
	{
		this.tvinfoDVBVButton.setText(   GUIStrings.IMPORT_TV
                + "\"" + TVInfoDVBV.NAME_IMPORTFILE + "\"" ) ;
		this.updateToNewVersionButton.setText(   GUIStrings.UPDATE_CHANNELS.toString() ) ;
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
				this.gui.setChanged() ;
			}
		}
		this.updateDVBViewerActions() ;
		this.updateText() ;
	}
}
