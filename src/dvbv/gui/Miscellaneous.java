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
import dvbv.importer.TVInfoDVBV;

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
	private final JTextField separatorBox = new JTextField() ;
	private final JButton tvinfoDVBVButton = new JButton() ;
	
	private final JLabel textInfoLabel = new JLabel() ;
	
		
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
					setInfoText( GUIStrings.changeEffect() ) ;
					
				}
			}
			else if ( c == actionAfterBox )
			{
				control.getDVBViewer().setAfterRecordingAction( (ActionAfterItems)o ) ;
				gui.setChanged() ;
				setInfoText( GUIStrings.changeEffect() ) ;
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
				new TVInfoDVBV( control ) ;
				tvinfoDVBVButton.setText( GUIStrings.successful() ) ;
				gui.setChanged() ;
			}
			else if ( source == fileSelectorButton )
			{
				boolean aborted = ! ( new WorkPathSelector( control.getDVBViewer() , gui.getFrame())).show() ;
				if ( aborted )
					return ;
				setInfoText( GUIStrings.changeEffect() ) ;
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
					setInfoText( GUIStrings.changeEffect() ) ;
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
		
		tB = BorderFactory.createTitledBorder( GUIStrings.dataPath() ) ;
		pathPanel.setBorder( tB ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.defaultDirectoryBox.setText( GUIStrings.dvbViewer() ) ;
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
		
		tB = BorderFactory.createTitledBorder( GUIStrings.gui() ) ;
		guiPanel.setBorder( tB ) ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel languageLabel = new JLabel( GUIStrings.language() ) ;
		guiPanel.add( languageLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		for ( Language l : Language.values() )
			this.languageBox.addItem( l ) ;
		this.languageBox.addActionListener( new ComboSelected() ) ;
		this.languageBox.setSelectedItem( GUIStrings.languageEnum ) ;
		guiPanel.add( this.languageBox, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel lookAndFeelLabel = new JLabel( GUIStrings.view() ) ;
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
		
		tB = BorderFactory.createTitledBorder( GUIStrings.dvbViewer() ) ;
		dvbViewerPanel.setBorder( tB ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.tvinfoDVBVButton.setText(   GUIStrings.importTV()
				                       + "\"" + TVInfoDVBV.NAME_IMPORTFILE + "\"" ) ;
		this.tvinfoDVBVButton.addActionListener( new ButtonsPressed() ) ;
		dvbViewerPanel.add( tvinfoDVBVButton, c ) ;


		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel actionAfterLabel = new JLabel( GUIStrings.actionAfter() ) ;
		dvbViewerPanel.add( actionAfterLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		for (ActionAfterItems a : ActionAfterItems.values())
			this.actionAfterBox.addItem( a ) ;
		this.actionAfterBox.setSelectedItem( this.control.getDVBViewer().getAfterRecordingAction() ) ;
		this.actionAfterBox.addActionListener( new ComboSelected() ) ;
		dvbViewerPanel.add( this.actionAfterBox, c ) ;

		

		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 2 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel separatorLabel = new JLabel( GUIStrings.separator() ) ;
		dvbViewerPanel.add( separatorLabel, c ) ;


		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 2 ;
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
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		textInfoLabel.setForeground( SystemColor.RED ) ;
		textInfoLabel.setPreferredSize( new Dimension( 400,16 ) ) ;

		this.add( textInfoLabel, c ) ;
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
	}
}
