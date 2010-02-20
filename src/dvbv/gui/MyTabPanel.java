// $LastChangedDate: 2010-02-02 20:15:15 +0100 (Di, 02. Feb 2010) $
// $LastChangedRevision: 79 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.gui;

import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import dvbv.control.Control;

public abstract class MyTabPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5527812753090468381L;
	protected final Control control ;
	protected final JFrame frame ;
	public MyTabPanel( Control control, JFrame frame )
	{
		super( new GridBagLayout() ) ;
		this.control = control ;
		this.frame = frame ;
	}
	public void update() {} ;
}
