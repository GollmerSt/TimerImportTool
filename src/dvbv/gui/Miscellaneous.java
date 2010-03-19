// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.gui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import dvbv.gui.GUIStrings.ActionAfterItems;
import dvbv.gui.GUIStrings.Language;
import dvbv.importer.TVInfoDVBV;

public class Miscellaneous extends MyTabPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6271528915024437692L;
	
	private final JComboBox languageBox = new JComboBox() ;
	private final JComboBox actionAfterBox = new JComboBox() ;
	private final JTextField separatorBox = new JTextField() ;
	private final JButton tvinfoDVBVButton = new JButton() ;
		
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
				}
			}
			else if ( c == actionAfterBox )
			{
				control.getDVBViewer().setAfterRecordingAction( (ActionAfterItems)o ) ;
				gui.setChanged() ;
			}
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
		}
		
	}
	public void paint()
	{		
		Insets i = new Insets( 5, 5, 5, 5 );
		GridBagConstraints c = null ;

		

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel languageLabel = new JLabel( GUIStrings.language() ) ;
		this.add( languageLabel, c ) ;

		
		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		for ( Language l : Language.values() )
			this.languageBox.addItem( l ) ;
		this.languageBox.addActionListener( new ComboSelected() ) ;
		this.languageBox.setSelectedItem( GUIStrings.languageEnum ) ;
		this.add( this.languageBox, c ) ;

		

		
		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel actionAfterLabel = new JLabel( GUIStrings.actionAfter() ) ;
		this.add( actionAfterLabel, c ) ;

		
		
		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		for (ActionAfterItems a : ActionAfterItems.values())
			this.actionAfterBox.addItem( a ) ;
		this.actionAfterBox.setSelectedItem( this.control.getDVBViewer().getAfterRecordingAction() ) ;
		this.actionAfterBox.addActionListener( new ComboSelected() ) ;
		this.add( this.actionAfterBox, c ) ;

		

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		JLabel separatorLabel = new JLabel( GUIStrings.separator() ) ;
		this.add( separatorLabel, c ) ;

		

		
		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 1 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;

		this.tvinfoDVBVButton.setText(   GUIStrings.importTV()
				                       + "\"" + TVInfoDVBV.NAME_IMPORTFILE + "\"" ) ;
		this.tvinfoDVBVButton.addActionListener( new ButtonsPressed() ) ;
		this.add( tvinfoDVBVButton, c ) ;

		
		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 1 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.separatorBox.setText( control.getSeparator() ) ;
		this.add( separatorBox, c ) ;
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
