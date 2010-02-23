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

import dvbv.importer.TVInfoDVBV;

public class Miscellaneous extends MyTabPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6271528915024437692L;
	
	private final JComboBox languageBox = new JComboBox() ;
	private final JTextField separatorBox = new JTextField() ;
	private final JButton tvinfoDVBVButton = new JButton() ;
		
	public Miscellaneous( GUI gui, JFrame frame)
	{
		super( gui, frame );
	}
	class LanguageSelected implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			String language = (String) languageBox.getSelectedItem() ;
			if ( language == null )
				return ;
			String internal = GUIStrings.toInternalLanguage( language ) ;
			if ( ! internal.equals( control.getLanguage() ) )
			{
				control.setLanguage( GUIStrings.toInternalLanguage( language ) ) ;
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
		
		for ( int ix = 0 ; ix < GUIStrings.languageStrings().length ; ix++ )
		{
			this.languageBox.addItem( GUIStrings.languageStrings()[ ix ] ) ;
		}
		this.languageBox.addActionListener( new LanguageSelected() ) ;
		this.languageBox.setSelectedItem( GUIStrings.getLanguage( this.control.getLanguage() ) ) ;
		this.add( this.languageBox, c ) ;

		

		
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
		c.gridy      = 0 ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
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
