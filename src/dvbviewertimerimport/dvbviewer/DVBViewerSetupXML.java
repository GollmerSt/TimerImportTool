package dvbviewertimerimport.dvbviewer;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.xml.StackXML;

public class DVBViewerSetupXML {
	
	private static final String NAME_XML_DVBVIEWER_SETUP = "setup.xml" ;

	private  static final StackXML< String > sectionPath = new StackXML< String >( "settings", "section" ) ;
	private  static final StackXML< String > entryPath   = new StackXML< String >( "settings", "section", "entry" ) ;

	private HashMap< String, HashMap< String, String > > map = null ;
	
	DVBViewer dvbViewer ;
	
	class StringRef
	{
		public String string = "" ;
	}

	DVBViewerSetupXML( DVBViewer dvbViewer )
	{
		this.dvbViewer = dvbViewer ;
	}
	
	void clear()
	{
		map = null ;
	}
	
	void readXML()
	{
		map = new HashMap< String, HashMap< String, String > >() ;
		
		File f = new File( dvbViewer.getDataPath() + File.separator + NAME_XML_DVBVIEWER_SETUP ) ;
		if ( ! f.exists() )
			return ;
			
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader  reader = inputFactory.createXMLEventReader( new StreamSource( f ) );
			StackXML<String>   stack = new StackXML<String>();
				
			String data = "" ;
			
			StringRef sectionName = new StringRef() ;
			StringRef entryName = new StringRef() ;
			StringRef ref = null ;
			
			HashMap< String, String > entryMap = null ;
				
			while( reader.hasNext() ) {
				XMLEvent ev = reader.nextEvent();
				if( ev.isStartElement() )
				{
					stack.push( ev.asStartElement().getName().getLocalPart() );
					if      ( stack.equals( DVBViewerSetupXML.sectionPath ) )
						ref = sectionName ;
					else if ( stack.equals( DVBViewerSetupXML.entryPath ) )
						ref = entryName ;
					@SuppressWarnings("unchecked")
					Iterator<Attribute> iter = ev.asStartElement().getAttributes();
					while( iter.hasNext() )
					{
						Attribute attr = iter.next() ;
						String value = attr.getValue() ;
						if ( attr.getName().getLocalPart().equals( "name" ) )
							ref.string = value ;
					}
					if ( stack.equals( DVBViewerSetupXML.sectionPath ) )
					{
						if ( ! map.containsKey( sectionName.string ) )
							map.put(  sectionName.string, new HashMap< String, String >() ) ;
						entryMap = map.get( sectionName.string ) ;
					}
				}
				else if( ev.isEndElement() )
				{
					if ( stack.equals( DVBViewerSetupXML.entryPath ) )
					{
						entryMap.put( entryName.string, data ) ;
						data = "" ;
					}
				    stack.pop();
				}

				else if ( ev.isCharacters() )
				{
					if ( stack.equals( DVBViewerSetupXML.entryPath ) )
							data += ev.asCharacters().getData() ;
				}
			}
			reader.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass( e,   "Error on readin XML file \"" + f.getName() 
						                + ". Position: Line = " + Integer.toString( e.getLocation().getLineNumber() )
						                +         ", column = " + Integer.toString(e.getLocation().getColumnNumber()) ) ;
		}
		catch (Exception  e) {
			throw new ErrorClass( e, "Unexpected error on read XML file \"" + f.getName() ) ;
		}
		return ;
	}
	public String getSetupValue( String section, String name, String deflt )
	{
		if ( map == null )
			this.readXML() ;
		if ( ! map.containsKey( section ) )
			return deflt ;
		HashMap< String, String > entryMap = map.get( section ) ;
		if ( ! entryMap.containsKey( name ) )
			return deflt ;
		return entryMap.get( name ) ;
		
	}

}
