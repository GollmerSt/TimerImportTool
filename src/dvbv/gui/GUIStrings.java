// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.gui;

import dvbv.misc.Log;

public enum GUIStrings {
	ACTION_AFTER             ( "Post record action", "Nach Aufnahme" ) ,
	ADD                      ( "Add", "Hinzuf�gen" ) ,
	ALL_TIMERS               ( "All timers", "Alle Timer" ) ,
	APPLY                    ( "Apply", "�bernehmen" ) ,
	ASSIGNED_ERROR           ( "is assigned to be reassigned?", "ist zugeordnet, soll neu zugeordnet werden?" ) ,
	BROAD_CAST_ADDRESS       ( "Broadast address", "Broadcast-Adresse" ) ,
	CANCEL                   ( "Cancel", "Abbruch" ) ,
	CHANGE_EFFECT            ( "The change will take effect after restarting the program ",
	                           "Die �nderung ist erst nach einem Neustart des Programms wirksam" ) ,
	CANNOT_DELETED           ( "Cannot be deleted in case of assignment", "Ist zugeordnet, kann daher nicht gel�scht werden" ) ,
	CHANNEL                  ( "Channel", "Kanal" ) ,
	CHANNELS_IMPORTED        ( " channels added", " Kan�le hinzugef�gt" ) ,
	CHECK                    ( "Check", "Teste" ) ,
	COPY_DEFAULT_CONTROL_FILE( "Control file doesn't exist, file will be recreated?",
                               "Steuerdatei existiert nicht, soll Datei neu erstellt werden?" ) ,
    DELETE                   ( "Delete", "L�schen" ) ,
	DATA_PATH                ( "Data path", "Daten-Pfad" ) ,
	DVBVIEWER                ( "DVBViewer" ) ,
	DVBVIEWER_ASSIGNMENT     ( "DVBViewer assignment", "DVBViewer-Zuordnung" ) ,
	DVBVIEWER_SERVICE        ( "DVBViewerService" ) ,
	ENABLE                   ( "Enable", "Aktivieren" ) ,
	END                      ( "End", "Ende" ) ,
	EXECUTE                  ( "Execute", "Ausf�hren" ) ,
	EXECUTING                ( "Executing", "Ausf�hrend" ) ,
	FAILED                   ( "Failed", "Fehler" ) ,
	FILTER                   ( "Filter", "Filter" ) ,
	GLOBAL                   ( "Global" ) ,
	GLOBAL_OFFSETS           ( "Global lead / lag times ...", "Globale Vor- / Nachlaufzeiten ..." ) ,
	GUI                      ( "GUI" ) ,
	IMPORT_TV                ( "Import ", "Importiere " ) ,
	INSTALL                  ( "Install", "Installieren" ) ,
	LANGUAGE                 ( "Language", "Sprache" ) ,
	MAC_ADDRESS              ( "MAC address", "MAC-Adresse" ) ,
	MERGE                    ( "Merge", "Verbinden" ) ,
	MESSAGE                  ( "Message", "Meldung" ) ,
	MISCELLANEOUS            ( "Miscellaneous", "Verschiedenes" ) ,
	MISSING_SINCE            ( "Missing since", "Fehlend seit" ) ,
	MISSING_SINCE_SYNC       ( "Missing since Sync.", "Fehlend seit Synchr." ) ,
	MODIFY                   ( "Modify", "Modifiziere" ) ,
	MORE_NOFO_LOG            ( "More information in log file \"" + Log.getFile() + "\"",
			                   "Mehr Information im Log-File \"" + Log.getFile() + "\"" ) ,
	NEW_ENTRY                ( "New entry", "Neuer Eintrag" ) ,
	NO                       ( "No", "Nein" ) ,
	OFFSET_DIALOG            ( "Lead / Lag times", "Vor- / Nachlaufzeiten" ) ,
	OFFSETS                  ( "Lead / Lag times ...", "Vor- / Nachlaufzeiten ..." ) ,
	OK                       ( "OK" ) ,
	PASS                     ( "Pass", "Bestanden" ) ,
	PASSWORD                 ( "Password", "Passwort" ) ,
	PROVIDER                 ( "Provider", "Anbieter" ) ,
	PROVIDER_SERVICE         ( "Provider / Service", "Anbieter / Service" ) ,
	PROVIDER_ASSIGNMENT      ( "Provider assignment", "Anbieter-Zuordnung" ) ,
	SELECT                   ( "Select", "Ausw�hlen" ) ,
	SELECT_CHANNEL_FILE      ( "Select the DVBViewer channel file",
			                   "W�hle das DVBViewer-Channel-File aus" ) ,
	SETUP_CHANGED            ( "Setup is changed!\nTerminating without saving?",
	                           "Einstellungen wurden ver�ndert!\nBeenden ohne Speichern?" ) ,
	SETUP_SAVE               ( "Setup is changed!\nShould the setup saved??",
			                   "Einstellungen wurden ver�ndert!\nSollen sie gespeichert werden?" ) ,
	SEPARATOR                ( "Separator", "Trennzeichen" ) ,
	START                    ( "Start" ) ,
	SUCCESSFULL              ( "Successful", "Erfolgreich" ) ,
	TIME_BEFORE              ( "Lead time", "Vorlauf" ) ,
	TIME_AFTER               ( "Lag time", "Nachlauf" ) ,
	TRIGGER_ACTION           ( "Trigger action" ) ,
	UNINSTALL                ( "Uninstall", "Deinstallieren" ) ,
	UNLOCK                   ( "Unlock", "Freigeben" ) ,
	UPDATE_CHANNELS          ( "Update channels from new version", "Aktualisiere Kan�le aus neuer Version" ) ,
	UPDATE_LIST              ( "Update timer list", "Aktualisiere Timer-Liste" ) ,
	URL                      ( "URL" ),
	USER_NAME                ( "Username", "Kennung" ) ,
	VERBOSE                  ( "Verbose", "Ausf�hrlich" ) ,
	VIEW                     ( "View", "Ansicht" ) ,
	WAIT_TIME                ( "Wait time after WOL", "Warte-Zeit nach WOL" ) ,
	WARNING                  ( "Warnings appears", "Warnungen vorhanden" ) ,
	WEEKDAYS                 ( "Mo  Tu   We  Th   Fr   Sa   Su", 
			                   "Mo   Di    Mi   Do   Fr   Sa   So" ) ,
	WOL                      ( "WOL" ) ;
	
	String [] strings = null ;

	private GUIStrings( String ... strings )
	{
		this.strings = strings ;
	}
	public String toString()
	{
		int ix =  GUIStrings.languageEnum.getAssigned().ordinal() ; 
		if ( strings.length < ix )
			return strings[ 0 ] ;
		else
			return strings[ ix - 1 ] ;
	}

	public enum ActionAfterItems
	{
		NONE     (  0, "Nothing", "Keine Aktion" ) ,
		POWER_OFF(  1, "Shutdown", "Herunterfahren" ) ,
		STANDBY  (  2, "Standby" ) ,
		HIBERNATE(  3, "Hibernate", "Ruhemodus" ),
		DEFAULT  ( -1, "Default", "Voreinstellung" );

		private final int id ;
		private final String [] items ;

		private ActionAfterItems( int id, String... strings )
		{
			this.id = id ;
			this.items = strings ;
		}
		@Override
		public String toString() { return GUIStrings.get( this.items ) ; } ;
		public int getID() { return this.id ; } ;
	}
	public enum Language
	{
		SYSTEM( "", "System", "System" ),
		ENGLISH( "en", "English", "Englisch" ),
		GERMAN( "de", "German", "Deutsch" ) ;

		private final String shortForm ;
		private final String [] strings ;
		Language assigned = this ;

		Language( String shortForm, String ... strings )
		{
			this.shortForm = shortForm ;
			this.strings = strings ;
		}
		@Override
		public String toString() { return GUIStrings.get( this.strings ) ; } ;
		public String getShort() { return this.shortForm ; } ;
		public Language getAssigned() { return this.assigned ; } ;
		public void setAssigned( Language assigned ) { this.assigned = assigned ; } ;
		
	}
	public static Language languageEnum = Language.SYSTEM ;

	public static void setLanguage( final String language )
	{
		String l = language ;
		if ( l.length() == 0 )
		{
			l = System.getProperty( "user.language") ;
		}
		GUIStrings.Language t = null ;
		if (    l.equals( "de" )
				 || l.equals( "at" )
				 || l.equals( "ch" )
				 || l.equals( "li" )
				 || l.equals( "lu" ) )
			t = Language.GERMAN ;
		else
			t = Language.ENGLISH ;
		if ( language.length() == 0 )
		{
			GUIStrings.languageEnum = Language.SYSTEM ;
			GUIStrings.languageEnum.setAssigned( t ) ;
		}
		else
			GUIStrings.languageEnum = t ;
	} ;

	private static String get( String... strings )
	{
		int ix =  GUIStrings.languageEnum.getAssigned().ordinal() ; 
		if ( strings.length < ix )
			return strings[ 0 ] ;
		else
			return strings[ ix - 1 ] ;
	}
}
