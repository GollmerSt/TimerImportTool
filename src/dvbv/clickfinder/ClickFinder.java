// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.clickfinder ;

import java.io.File;
import java.text.ParseException;

import dvbv.dvbviewer.DVBViewer ;
import dvbv.misc.* ;
import dvbv.provider.Provider;


public class ClickFinder extends Provider {
	private DVBViewer dvbViewer = null ;
	public ClickFinder( DVBViewer dvbViewer )
	{
		super( false, false, "ClickFinder", false, false, false, true, false ) ;
		this.dvbViewer = dvbViewer ;
	}
	private String getParaInfo()
	{
		return 
		", necessary parameters:\n   -ClickFinder [-path dataPath] Sender=ccc Begin=yyyyMMddHHmm Dauer=nnn Sendung=cccccc" ;
	}
	public void processEntry( String[] args )
	{		
		String channel = null ;
		String startTime = null ;
		long milliSeconds = -1 ;
		String title = null ;
		
		for ( int i = 0 ; i < args.length ; i++ )
		{
			String p = args[i] ;
			int pos = p.indexOf('=') ;
			if ( pos < 0 )
				continue ;
			String key   = p.substring(0, pos).trim() ;
			String value = p.substring(pos+1).trim() ;
			
			if      ( key.equalsIgnoreCase("Sender"))
				channel = value ;
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
			start = Conversions.clickFinderTimeToLong( startTime ) ;
		} catch (ParseException e) {
			String errorString = this.getParaInfo() ;
			throw new ErrorClass( e, "Syntax error in the parameter \"Begin\"" + errorString ) ;
		}
		long end = start + milliSeconds ;
		this.dvbViewer.addNewEntry( this, channel, start, end, title ) ;
		this.dvbViewer.merge() ;
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
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "ParameterFest", "-jar \"\"\""
				           + this.dvbViewer.getExePath() + File.separator + this.dvbViewer.getExeName()
				           + "\"\"\" -ClickFinder" + dataPathPara ) ;
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
}
