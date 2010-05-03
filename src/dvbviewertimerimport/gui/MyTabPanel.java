// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.gui;

import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import dvbviewertimerimport.control.Control;

public abstract class MyTabPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5527812753090468381L;
	protected final Control control ;
	protected final GUI gui ;
	protected final JFrame frame ;
	public MyTabPanel( GUI gui, JFrame frame )
	{
		super( new GridBagLayout() ) ;
		
		this.gui = gui ;
		this.control = gui.getControl() ;
		this.frame = frame ;
	}
	public void update( boolean active ) {} ;
}
