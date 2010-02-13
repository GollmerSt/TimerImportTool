package Provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import XML.Conversions;

import Misc.ErrorClass;

public class Provider {
	private static ArrayList< String > names = new ArrayList< String >() ;
	private static ArrayList< Provider > providers = new ArrayList< Provider >() ;
	private final int id ;
	private final boolean hasAccount ;
	private final boolean hasURL ;
	private final String name ;
	protected String url = "" ;
	protected String username = null ;
	protected String password = null ;
	private int triggerAction = -1 ;
	private boolean merge = false ;
	private boolean verbose = false ;
	private boolean message = false ;
	public Provider( boolean hasAccount, boolean hasURL, String name )
	{
		this.hasAccount   = hasAccount ;
		this.hasURL       = hasURL ;
		this.name = name ;
		this.id = Provider.providers.size() ;
		Provider.names.add( name ) ;
		Provider.providers.add( this ) ;
	}
	public int getID() { return this.id ; } ;
	public String getName() { return this.name ; } ;
	protected String getUserName() { return this.username ; } ;
	protected String getPassword() { return this.password ; } ;
	protected String getURL() { return this.url ; } ;
	public boolean getMerge()   { return this.merge ; } ;
	public boolean getVerbose() { return this.verbose ; } ;
	public boolean getMessage() { return this.message ; } ;
	public static ArrayList< Provider > getProviders() { return Provider.providers ; } ;
	public static boolean contains( String provider ){ return Provider.names.contains( provider ) ; } ;
	public static Provider getProvider( String providerName )
	{
		int pos = Provider.names.indexOf( providerName ) ;
		if ( pos < 0 )
			return null ;
		return Provider.providers.get( pos ) ;
	}
	public static String getProviderName( int id)
	{
		return Provider.names.get( id ) ;
	}
	public static int getProviderID( String provider )
	{
		return Provider.names.indexOf( provider ) ;
	}
	public static void readXML( XMLEventReader reader, File f ) throws XMLStreamException
	{
		Stack<String> pathProvider = new Stack<String>() ;
		Collections.addAll( pathProvider, "Providers", "Provider" ) ;
		
		Stack<String> pathURL = new Stack<String>() ;
		Collections.addAll( pathURL, "Providers", "Provider", "Url" ) ;

		Stack<String> stack = new Stack<String>() ;
		stack.push( "Providers" ) ;
		XMLEvent ev = null ;

		String url = "" ;
		Provider provider = null ;
				
		while( reader.hasNext() )
		{
			try {
				ev = reader.nextEvent();
			} catch (XMLStreamException e1) {
				throw new ErrorClass( e1, "XML syntax error in file \"" + f.getName() + "\"" );
			}

			if ( ev.isStartElement() )
			{
				stack.push( ev.asStartElement().getName().getLocalPart() ) ;
				
				if      ( stack.equals( pathProvider ) )
				{
					String name = null;
					String username = null ;
					String password = null ;
					int triggerAction = -1 ;
					boolean verbose = false ;
					boolean message = false ;
					boolean merge = false ;
					url = "" ;
					
					Iterator<Attribute> iter = ev.asStartElement().getAttributes();
					
					while ( iter.hasNext() )
					{
		            	Attribute a = iter.next();
		            	String attributeName = a.getName().getLocalPart() ;
		            	String value = a.getValue().trim() ;
		            	{
		            		if      ( attributeName.equals( "name") )
		            			name = value ;
		            		else if ( attributeName.equals( "username") )
		            			username = value ;
		            		else if ( attributeName.equals( "password") )
		            			password = value ;
		            		else if ( attributeName.equals( "triggeraction") )
			            	{
			            		if ( !value.matches("\\d+") )
			            			throw new ErrorClass ( ev, "Wrong triggeraction format in file \"" + f.getName() + "\"" ) ;
			            		triggerAction = Integer.valueOf( value ) ;
			            	}
		            		else if ( attributeName.equals( "merge") )
		            			merge = Conversions.getBoolean( value, ev, f ) ;
		            		else if ( attributeName.equals( "verbose") )
		            			verbose = Conversions.getBoolean( value, ev, f ) ;
			            	else if ( attributeName.equals( "message") )
		            			message = Conversions.getBoolean( value, ev, f ) ;
		            	}
					}
					provider = Provider.getProvider( name ) ;
					if ( provider == null )
            			throw new ErrorClass ( ev, "Unknown provider name in file \"" + f.getName() + "\"" ) ;
					provider.username = username ;
					provider.password = password ;
					provider.triggerAction = triggerAction ;
					provider.verbose = verbose ;
					provider.message = message ;
					provider.merge = merge ;
				}
			}
			if ( ev.isCharacters() )
			{
				if ( stack.equals( pathURL ) )
					provider.url += ev.asCharacters().getData().trim() ;
			}
			if ( ev.isEndElement() )
			{
				stack.pop() ;
				if ( stack.size() == 0 )
					break ;
			}
		}
	}
	public static void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException
	{
		sw.writeStartElement( "Providers" ) ;
		  for ( Iterator<Provider> it = Provider.providers.iterator() ; it.hasNext() ;)
		  {
			  Provider provider = it.next();
			  sw.writeStartElement( "Provider" ) ;
			  sw.writeAttribute( "name", provider.name) ;
			  if ( provider.hasAccount )
			  {
				  sw.writeAttribute( "username", provider.username ) ;
				  sw.writeAttribute( "password", provider.password ) ;
			  }
			  if ( provider.triggerAction >= 0 )
				  sw.writeAttribute( "triggeraction", Integer.toString( provider.triggerAction  ) ) ;
			  sw.writeAttribute( "merge",   provider.merge ) ;
			  sw.writeAttribute( "message", provider.message ) ;
			  sw.writeAttribute( "verbose", provider.verbose ) ;
			  if ( provider.hasURL )
			  {
				  sw.writeStartElement( "Url") ;
				    sw.writeCharacters( provider.url ) ;
				  sw.writeEndElement() ;
			  }
			  sw.writeEndElement() ;
		  }
		sw.writeEndElement() ;
	}
}
