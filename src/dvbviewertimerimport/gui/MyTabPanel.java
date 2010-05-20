// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.gui;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import dvbviewertimerimport.control.Control;

public abstract class MyTabPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5527812753090468381L;
	protected final Control control ;
	protected final GUIPanel guiPanel ;
	public MyTabPanel( GUIPanel gui )
	{
		super( new GridBagLayout() ) ;
		
		this.guiPanel = gui ;
		this.control = gui.getControl() ;
	}
	public abstract void update( boolean active ) ;
	public GUIPanel getGUIPanel() { return guiPanel ; } ;
	public abstract void init() ;
}
