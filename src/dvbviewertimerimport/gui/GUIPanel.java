package dvbviewertimerimport.gui;

import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.provider.Provider;

public class GUIPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 461797875135545150L;
	private final Control control ;
	private final dvbviewertimerimport.dvbviewer.channels.Channels dChannels ;
	private final GUI gui ;

	private boolean isChanged = false ;
	private MyTabPanel previousTab = null ;

	private final JTabbedPane tabbedPane = new JTabbedPane() ;
    
    private ProviderAssignment providerAssignment = null ;
    private Miscellaneous miscellaneous = null ;
    
	public GUIPanel( Control control, dvbviewertimerimport.dvbviewer.channels.Channels dChannels )
	{
		this( null, control, dChannels ) ;
	}

	
	public GUIPanel( GUI gui, Control control, dvbviewertimerimport.dvbviewer.channels.Channels dChannels )
	{
		this.gui = gui ;
		this.control = control ;
		this.dChannels = dChannels ;
		this.isChanged = false ;
	}
	
    private class TabChanged implements ChangeListener
    {
		@Override
		public void stateChanged(ChangeEvent e) {
			updateExecuteButton() ;
			MyTabPanel source = (MyTabPanel)((JTabbedPane)e.getSource()).getSelectedComponent() ;
			if ( source == null )
				return ;
			previousTab.update( false );
			source.update( true ) ;
			previousTab = source ;
		}
    	
    }

	public Control getControl() { return this.control ; } ;
	public Window getWindow()
	{
	  Window w = null ;
		try
		{
			w = (Window) this.getRootPane().getParent() ;
		} catch ( Exception e ) {
			Log.error( "Illegal GUI class hierarchy " ) ;
			System.exit( 1 ) ;
		}
		return w ;
	}
	public void setChanged() { this.isChanged = true ; } ;
	public boolean getChanged() { return this.isChanged ; } ;
	public void paint()
	{ 
		this.add( this.tabbedPane ) ;

	    
	    DVBViewerAssignment tab1 = new DVBViewerAssignment( this, dChannels ) ;
	    ProviderService tab2 = new ProviderService( this ) ;
	    this.miscellaneous = new Miscellaneous( this ) ;
	    this.providerAssignment = new ProviderAssignment( this ) ;
	    	    
	    this.tabbedPane.add( ResourceManager.msg( "DVBVIEWER_ASSIGNMENT" ), tab1); 
	    this.tabbedPane.add( ResourceManager.msg( "PROVIDER_SERVICE" ), tab2);
	    this.tabbedPane.add( ResourceManager.msg( "MISCELLANEOUS" ), this.miscellaneous);
	    this.tabbedPane.add( ResourceManager.msg( "PROVIDER_ASSIGNMENT" ), providerAssignment);
	    this.tabbedPane.addChangeListener( new TabChanged() ) ;
	    
	    this.tabbedPane.setSelectedComponent( tab1 ) ;
	    this.previousTab = (MyTabPanel)tab1 ;
	    tab1.paint() ;
	    tab2.paint() ;
	    this.miscellaneous.paint() ;
	    this.providerAssignment.paint() ;

        this.updateExecuteButton() ;
	}
	public MyTabPanel getSelectedComponent()
	{
		return (MyTabPanel)this.tabbedPane.getSelectedComponent() ;
	}
	
	public void updateExecuteButton()
	{
		if ( gui == null )
			return ;
		Object o = this.tabbedPane.getSelectedComponent() ;
		if ( o != null && o.getClass() == DVBViewerAssignment.class )
			gui.updateExecuteButton( true ) ;
		else
			gui.updateExecuteButton( false ) ;
	}
	public ArrayList< String > getLookAndFeelNames()
	{
		if ( gui == null )
			return null ;
		else
			return gui.getLookAndFeelNames() ;
	} ;

	public void setLookAndFeel( String name )
	{
		if ( this.gui == null )
			return ;
		this.gui.setLookAndFeel( name ) ;
		this.isChanged = true ;
	}

	public void updateIfChannelSetsChanged( Provider p )
	{
	 	this.providerAssignment.updateIfChannelSetsChanged( p ) ;
	}
	public void updateIfServiceChanged()
	{
		this.miscellaneous.updateDVBViewerActions() ;
	}
}
