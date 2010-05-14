// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.clickfinder ;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer ;
import dvbviewertimerimport.misc.* ;
import dvbviewertimerimport.provider.Provider;


public class ClickFinder extends Provider {

	private DVBViewer dvbViewer = null ;
	private final SimpleDateFormat dateFormat ;

	public ClickFinder( Control control )
	{
		super( control, false, false, "ClickFinder", false, false, false, true, false ) ;
		this.dvbViewer = control.getDVBViewer() ;
		this.timeZone = TimeZone.getTimeZone("Europe/Berlin") ;
		this.dateFormat = new SimpleDateFormat("yyyyMMddHHmm") ;
		this.dateFormat.setTimeZone( timeZone ) ;
	}
	private String getParaInfo()
	{
		return 
		", necessary parameters:\n   -ClickFinder [-path dataPath] Sender=ccc Begin=yyyyMMddHHmm Dauer=nnn Sendung=cccccc" ;
	}
	@Override
	public boolean processEntry( Object args, DVBViewer.Command command )
	{		
		String channel = null ;
		String providerID = null ;
		String startTime = null ;
		long milliSeconds = -1 ;
		String title = null ;
		
		for ( String p : (String[])args )
		{
			int pos = p.indexOf('=') ;
			if ( pos < 0 )
				continue ;
			String key   = p.substring(0, pos).trim() ;
			String value = p.substring(pos+1).trim() ;
			
			if      ( key.equalsIgnoreCase("Sender"))
				channel = value ;
			else if ( key.equalsIgnoreCase("Pos"))
				providerID = value ;
			else if ( key.equalsIgnoreCase("Beginn"))
				startTime = value ;
			else if ( key.equalsIgnoreCase("Dauer"))
			{
				if ( ! value.matches("\\d+") )
					throw new ErrorClass( "Undefined value of parameter \"Dauer\".") ;
				milliSeconds = Long.valueOf(value) * 60000 ;
			}
			else if ( key.equalsIgnoreCase("Sendung"))
				title = value ;
		}
		if ( channel == null || startTime == null || milliSeconds < 0 || title == null )
		{
			String errorString = this.getParaInfo() ;
			throw new ErrorClass( "Missing parameter" + errorString ) ;
		}
		long start = 0 ;
		try {
			start = timeToLong( startTime ) ;
		} catch (ParseException e) {
			String errorString = this.getParaInfo() ;
			throw new ErrorClass( e, "Syntax error in the parameter \"Begin\"" + errorString ) ;
		}
		long end = start + milliSeconds ;
		this.dvbViewer.addNewEntry( this, providerID, channel, start, end, title ) ;
		
		return true ;
	}
	
	public boolean install() // boolean setDataDir )
	{
		String dataPathPara = "" ;
		//if ( setDataDir )
		//	dataPathPara = " -path \"\"\"" + this.dvbViewer.getDataPath() + "\"\"\"";
		String regContents = Registry.getValue( "HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost", "AddOns" ) ;
		if ( regContents == null || ! regContents.contains("DVBViewer") )
		{
			if ( regContents != null && regContents.length() != 0 )
				regContents += ",DVBViewer" ;
			else
				regContents = "DVBViewer" ;
			Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost", "REG_SZ", "AddOns", regContents ) ;
		}
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "AddOnName", "DVBViewer" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_DWORD", "EinbindungsModus", "2" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "KurzBeschreibung", "DVBViewer programmieren" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "LangBeschreibung", "Übergeben Sie diese Sendung an den DVBViewer" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_DWORD", "ParameterZusatz", "2" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "SpezialButtonGrafikName", "AddOn" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "SpezialButtonToolTiptext", "SpezialButtonToolTiptext" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "ExeDateiname", "javaw" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "ParameterFest",
				             "-Djava.library.path=\"\"\"" + this.dvbViewer.getExePath() + "\"\"\" -jar \"\"\""
				           + this.dvbViewer.getExePath() + File.separator + this.dvbViewer.getExeName()
				           + "\"\"\" -ClickFinder" + " -iniPath \"\"\"" + this.dvbViewer.getExePath() + "\"\"\" " + dataPathPara ) ;
		return true ;
	}
	 public boolean uninstall()
	{
		String regContents = Registry.getValue( "HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost", "AddOns" ) ;
		if ( regContents != null && regContents.contains("DVBViewer") )
		{
			String[] temps ;
			if ( regContents.contains(",") )
				temps = regContents.split(",DVBViewer") ;
			else
				temps = regContents.split("DVBViewer") ;
			regContents = "" ;
			for ( int i = 0 ; i < temps.length ; i++ )
				regContents += temps[ i ] ;
			Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost", "REG_SZ", "AddOns", regContents ) ;

			Registry.delete("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "" ) ;
		}
		return true ;
	}
	private long timeToLong( String time ) throws ParseException
	{
		Date d = new Date( dateFormat.parse(time).getTime()) ;
		//System.out.println(d.toString()) ;
		return d.getTime() ;
	}	
}
