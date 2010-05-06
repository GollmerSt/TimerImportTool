// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import dvbviewertimerimport.xml.Conversions;
import dvbviewertimerimport.xml.StackXML;

import dvbviewertimerimport.misc.ErrorClass;

public abstract class Provider {

	private static final StackXML<String> pathProvider = new StackXML<String>( "Providers", "Provider" ) ;
	private static final StackXML<String> pathURL = new StackXML<String>( "Providers", "Provider", "Url" ) ;
	private static final StackXML<String> pathMissing = new StackXML<String>( "Providers", "Provider", "Missing" ) ;

	private enum XMLStatus { UKNOWN, MISSING, PROVIDER } ;
	private static ArrayList< String > names = new ArrayList< String >() ;
	private static ArrayList< Provider > providers = new ArrayList< Provider >() ;
	
	private static Stack< ArrayList< String > >  nameStack = new Stack< ArrayList< String > >() ;
	private static Stack< ArrayList< Provider > > providerStack = new Stack< ArrayList< Provider > >() ;
	
	public static void push()
	{
		Provider.nameStack.push( Provider.names ) ;
		Provider.providerStack.push( Provider.providers ) ;
		Provider.names = new ArrayList< String >() ;
		Provider.providers = new ArrayList< Provider >() ;
	}
	public static void pop()
	{
		Provider.names     = Provider.nameStack.pop() ;
		Provider.providers = Provider.providerStack.pop() ;
	}
	
	private final int id ;
	protected final Control control ;
	private final boolean hasAccount ;
	private final boolean hasURL ;
	private final boolean canExecute ;
	private final boolean canTest ;
	private final boolean mustInstall ;
	private final String name ;
	protected String url = "" ;
	protected String username = null ;
	protected String password = null ;
	private int triggerAction = -1 ;
	private boolean merge = false ;
	private boolean verbose = false ;
	private boolean message = false ;
	private boolean filter = false ;
	private boolean isFilterEnabled = true ;
	private boolean isPrepared = false ;
	private OutDatedInfo outDatedLimits = null ;
	public Provider( Control control,
					 boolean hasAccount,
			         boolean hasURL,
			         String name,
			         boolean canExecute,
			         boolean canTest,
			         boolean filter,
			         boolean mustInstall,
			         boolean canImport,
			         boolean isOutDatedLimitsEnabled )
	{
		this.control      = control ;
		this.hasAccount   = hasAccount ;
		this.hasURL       = hasURL ;
		this.name = name ;
		this.id = Provider.providers.size() ;
		this.canExecute = canExecute ;
		this.canTest = canTest ;
		this.filter = filter ;
		this.mustInstall = mustInstall ;
		this.isPrepared = ! isOutDatedLimitsEnabled ;
		if ( isOutDatedLimitsEnabled)
		{
			outDatedLimits = new OutDatedInfo( true ) ;
		}
		Provider.names.add( name ) ;
		Provider.providers.add( this ) ;
	}
	public int getID() { return this.id ; } ;
	public String getName() { return this.name ; } ;
	public String toString() { return this.name ; } ;
	public String getURL() { return this.url ; } ;
	public void setURL( final String url) { this.url = url; } ;
	public String getUserName() { return this.username ; } ;
	public void setUserName( final String name ) { this.username = name; } ;
	public String getPassword() { return this.password ; } ;
	public void setPassword( final String password) { this.password = password ; } ;
	public int getTriggerAction() { return this.triggerAction ; } ;
	public void setTriggerAction( int triggerAction ) { this.triggerAction = triggerAction ; } ;
	public boolean isFiltered() { return this.filter ; } ;
	public void setFilter( boolean filter ) { this.filter = filter ; } ;
	public boolean isFilterEnabled() { return this.isFilterEnabled ; } ;
	public void setFilterEnabled( boolean enable ) { this.isFilterEnabled = enable ; } ;
	public boolean getMerge()   { return this.merge ; } ;
	public void setMerge( boolean m)   { this.merge = m ; } ;
	public boolean getVerbose() { return this.verbose ; } ;
	public void setVerbose( boolean verbose) { this.verbose = verbose ; } ;
	public boolean getMessage() { return this.message ; } ;
	public void setMessage( boolean message) { this.message = message ; } ;
	public boolean hasURL() { return this.hasURL ; } ;
	public boolean hasAccount() { return this.hasAccount ; } ;
	public boolean canExecute() { return this.canExecute ; } ;
	public boolean canTest()   { return this.canTest ; } ;
	public boolean canImport() { return this.importChannels( true ) >= 0 ; } ;
	public boolean mustInstall() { return this.mustInstall ; } ;
	public boolean isPrepared() { return this.isPrepared ; } ;
	public void setPrepared( boolean prepared ) { this.isPrepared = prepared ; } ;
	public OutDatedInfo getOutDatedLimits() { return this.outDatedLimits ; } ;
	public boolean install()   { return true ; } ;
	public boolean uninstall() { return true ; } ;
	public int importChannels( boolean check ) { return -1 ; } ;
	public int importChannels() { return this.importChannels( false ) ; } ;
	public void process( boolean getAll ) {} ;
	public void processEntry( String[] args ) {} ;
	public void check()
	{
		if ( this.hasAccount && ( this.username == null || this.password == null ) )
			throw new ErrorClass( "Username or password is missing" ) ;
		
		if ( this.hasURL && this.url == null )
			throw new ErrorClass( "URL is missing" ) ;
	}
	public boolean test() { return false ; } ;
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
	public static void readXML( XMLEventReader reader, String fileName ) throws XMLStreamException
	{
		StackXML<String> stack = new StackXML<String>() ;
		stack.push( "Providers" ) ;
		XMLEvent ev = null ;

		Provider provider = null ;
				
		String name = null;
		String username = null ;
		String password = null ;
		int triggerAction = -1 ;
		boolean verbose = false ;
		boolean message = false ;
		boolean merge = false ;
		boolean filter = false ;
		OutDatedInfo info = new OutDatedInfo( true ) ;
		String url = "" ;
		
		XMLStatus xmlStatus = XMLStatus.UKNOWN ;

		while( reader.hasNext() )
		{
			try {
				ev = reader.nextEvent();
			} catch (XMLStreamException e1) {
				throw new ErrorClass( e1, "XML syntax error in file \"" + fileName + "\"" );
			}

			if ( ev.isStartElement() )
			{
				stack.push( ev.asStartElement().getName().getLocalPart() ) ;
				

				if      ( stack.equals( pathProvider ) )
				{
					name = null;
					username = null ;
					password = null ;
					triggerAction = -1 ;
					verbose = false ;
					message = false ;
					merge = false ;
					filter = false ;
					
					xmlStatus = XMLStatus.PROVIDER ;
				}
				else if ( stack.equals( pathMissing ))
				{
					info = new OutDatedInfo( true ) ;
					
					xmlStatus = XMLStatus.MISSING ;
				}
					
				@SuppressWarnings("unchecked")
				Iterator<Attribute> iter = ev.asStartElement().getAttributes();
					
				while ( iter.hasNext() )
				{
		            Attribute a = iter.next();
		            String attributeName = a.getName().getLocalPart() ;
		            String value = a.getValue().trim() ;
		            
		            switch ( xmlStatus)
		            {
		            	case  PROVIDER :
		            		if      ( attributeName.equals( "name") )
		            			name = value ;
		            		else if ( attributeName.equals( "username") )
		            			username = value ;
		            		else if ( attributeName.equals( "password") )
		            			password = value ;
		            		else if ( attributeName.equals( "triggeraction") )
			            	{
			            		if ( !value.matches("\\d+") )
			            			throw new ErrorClass ( ev, "Wrong triggeraction format in file \"" + fileName + "\"" ) ;
			            		triggerAction = Integer.valueOf( value ) ;
			            	}
		            		else if ( attributeName.equals( "merge") )
		            			merge = Conversions.getBoolean( value, ev, fileName ) ;
		            		else if ( attributeName.equals( "verbose") )
		            			verbose = Conversions.getBoolean( value, ev, fileName ) ;
			            	else if ( attributeName.equals( "message") )
		            			message = Conversions.getBoolean( value, ev, fileName ) ;
			            	else if ( attributeName.equals( "filter") )
		            			filter = Conversions.getBoolean( value, ev, fileName ) ;
		            		break ;
		            	
		            	case MISSING :
		            		try {
		            			info.readXML( attributeName, value ) ;
		            		} catch ( ErrorClass e ) {
		            			throw new ErrorClass ( ev, e.getErrorString() + " in file \"" + fileName + "\"" ) ;
		            		}
		            		break ;
					}
				}
			}
			if ( ev.isCharacters() )
			{
				if ( stack.equals( pathURL ) )
					url += ev.asCharacters().getData().trim() ;
			}
			if ( ev.isEndElement() )
			{
				if ( stack.equals( pathProvider ))
				{
					provider = Provider.getProvider( name ) ;
					if ( provider == null )
            			throw new ErrorClass ( ev, "Unknown provider name in file \"" + fileName + "\"" ) ;
					provider.username = username ;
					provider.password = password ;
					provider.triggerAction = triggerAction ;
					provider.verbose = verbose ;
					provider.message = message ;
					provider.merge = merge ;
					provider.filter = filter ;
					if ( provider.outDatedLimits != null )
					{
						provider.outDatedLimits = info ;
						info = null ;
					}
					provider.url = url ;
					url = "" ;
				}
				stack.pop() ;
				if ( stack.size() == 1 )
					try {
						provider.check() ;
					} catch( ErrorClass e ) {
						throw new ErrorClass( ev, e.getErrorString() + " in file \"" + fileName + "\""  ) ;
					}
					else if ( stack.size() == 0 )
					break ;
			}
		}
	}
	public static void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException
	{
		sw.writeStartElement( "Providers" ) ;
		  for ( Provider provider : Provider.providers )
		  {
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
			  sw.writeAttribute( "filter", provider.filter ) ;
			  if ( provider.outDatedLimits != null )
			  {
				  sw.writeStartElement( "Missing" ) ;
				  provider.outDatedLimits.writeXML( sw ) ;
				  sw.writeEndElement() ;
			  }
				  
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