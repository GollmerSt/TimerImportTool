package dvbv.gui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {
	
	public void execute()
	  { 
	    JFrame frame = new JFrame(); 
	    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); 
	 
	    JLabel l = new JLabel( "Lebe immer First-Class, sonst tun es deine Erben!" ); 
	    l.setForeground( Color.BLUE ); 
	 
	    frame.add( l ); 
	 
	    l.addMouseListener( new MouseAdapter() { 
	      @Override public void mouseClicked( MouseEvent e ) { 
	        if ( e.getClickCount() > 1 ) 
	          System.exit( 0 ); 
	      } 
	    } ); 
	 
	    frame.pack(); 
	    frame.setVisible( true ); 
	  } 
}
