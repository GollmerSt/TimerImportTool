package dvbv.gui;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import dvbv.control.Control;

public abstract class MyTabPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5527812753090468381L;
	protected final Control control ;
	public MyTabPanel( Control control )
	{
		super( new GridBagLayout() ) ;
		this.control = control ;
	}
	public void update() {} ;
}
