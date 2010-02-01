// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$


import java.io.File;
import java.text.ParseException;


public class ClickFinder {
	private DVBViewer dvbViewer = null ;
	ClickFinder( DVBViewer dvbViewer )
	{
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
		this.dvbViewer.addNewClickFinderEntry(channel, start, end, title) ;
		this.dvbViewer.combine() ;
	}
	
	public void putToRegistry( boolean setDataDir, String additionalParas )
	{
		String dataPathPara = "" ;
		if ( setDataDir )
			dataPathPara = " -path \"\"\"" + this.dvbViewer.getDataPath() + "\"\"\"";
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
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "LangBeschreibung", "�bergeben Sie diese Sendung an den DVBViewer" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_DWORD", "ParameterZusatz", "2" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "SpezialButtonGrafikName", "AddOn" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "SpezialButtonToolTiptext", "SpezialButtonToolTiptext" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "ExeDateiname", "javaw" ) ;
		Registry.setValue("HKLM\\SOFTWARE\\EWE\\TVGhost\\TVGhost\\AddOn_DVBViewer", "REG_SZ", "ParameterFest", "-jar \"\"\""
				           + this.dvbViewer.getExePath() + File.separator + this.dvbViewer.getExeName()
				           + "\"\"\" -ClickFinder" + dataPathPara + " " + additionalParas ) ;
	}
	 public void removeFromRegistry()
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
	}}