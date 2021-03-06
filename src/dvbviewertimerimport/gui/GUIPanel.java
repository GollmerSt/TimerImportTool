// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.misc.TerminateClass;
import dvbviewertimerimport.provider.Provider;

public class GUIPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 461797875135545150L;
	private final Control control ;
	private final GUI gui ;

	private boolean isChanged = false ;
	private MyTabPanel previousTab = null ;

	private final JTabbedPane tabbedPane = new JTabbedPane() ;
	
	private DVBViewerAssignment dvbViewerAssignment = null ;
	private ProviderAssignment providerAssignment = null ;
	private Miscellaneous miscellaneous = null ;
	
	public GUIPanel( Control control )
	{
		this( null, control ) ;
	}

	
	public GUIPanel( GUI gui, Control control )
	{
		super( new BorderLayout() ) ;
		this.gui = gui ;
		this.control = control ;
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
			source.update( true ) ;
			if ( GUIPanel.this.previousTab != null )
				GUIPanel.this.previousTab.update( false );
			GUIPanel.this.previousTab = source ;
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
			throw new TerminateClass( 1 ) ;
		}
		return w ;
	}
	public void setChanged() { this.isChanged = true ; } ;
	public boolean getChanged() { return this.isChanged ; } ;
	public void init()
	{ 

		
		this.dvbViewerAssignment = new DVBViewerAssignment( this ) ;
		ProviderService tab2 = new ProviderService( this ) ;
		this.miscellaneous = new Miscellaneous( this ) ;
		this.providerAssignment = new ProviderAssignment( this ) ;
				
		this.tabbedPane.add( ResourceManager.msg( "DVBVIEWER_ASSIGNMENT" ), this.dvbViewerAssignment); 
		this.tabbedPane.add( ResourceManager.msg( "PROVIDER_SERVICE" ), tab2);
		this.tabbedPane.add( ResourceManager.msg( "MISCELLANEOUS" ), this.miscellaneous);
		this.tabbedPane.add( ResourceManager.msg( "PROVIDER_ASSIGNMENT" ), this.providerAssignment);
		
		this.tabbedPane.setSelectedComponent( this.miscellaneous ) ;

		for ( Component tp : this.tabbedPane.getComponents() )
			((MyTabPanel)tp).init() ;
		
		this.tabbedPane.setSelectedComponent( this.dvbViewerAssignment ) ;
		
		this.add( this.tabbedPane, BorderLayout.CENTER ) ;
		this.tabbedPane.addChangeListener( new TabChanged() ) ;

		this.updateExecuteButton() ;
}
	public MyTabPanel getSelectedComponent()
	{
		return (MyTabPanel)this.tabbedPane.getSelectedComponent() ;
	}
	public void updateDVBViewerChannels()
	{
		this.dvbViewerAssignment.updateDVBViewerChannels() ;
	}
	public void updateExecuteButton()
	{
		if ( this.gui == null )
			return ;
		Object o = this.tabbedPane.getSelectedComponent() ;
		if ( o != null && o.getClass() == DVBViewerAssignment.class )
			this.gui.updateExecuteButton( true ) ;
		else
			this.gui.updateExecuteButton( false ) ;
	}
	public ArrayList< String > getLookAndFeelNames()
	{
		if ( this.gui == null )
			return null ;
		else
			return this.gui.getLookAndFeelNames() ;
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
	public void updateTab()
	{
		this.getSelectedComponent().update( true ) ;
		}
}
