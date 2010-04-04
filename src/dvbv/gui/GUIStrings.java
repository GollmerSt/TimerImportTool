// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.gui;

import java.util.ArrayList;

public enum GUIStrings {
	ACTION_AFTER             ( "After recording", "Nach Aufnahme" ) ,
	ACTION_TIMER             ( "Timer action", "Timer Aktion" ) ,
	ADD                      ( "Add", "Hinzufügen" ) ,
	ALL_TIMERS               ( "All timers", "Alle Timer" ) ,
	APPLY                    ( "Apply", "Übernehmen" ) ,
	ASSIGNED_ERROR           ( "is assigned to be reassigned?", "ist zugeordnet, soll neu zugeordnet werden?" ) ,
	BROAD_CAST_ADDRESS       ( "Broadast address", "Broadcast-Adresse" ) ,
	CANCEL                   ( "Cancel", "Abbruch" ) ,
	CHANGE_EFFECT            ( "The change will take effect after restarting the program ",
	                           "Die Änderung ist erst nach einem Neustart des Programms wirksam" ) ,
	CANNOT_DELETED           ( "Cannot be deleted in case of assignment", "Ist zugeordnet, kann daher nicht gelöscht werden" ) ,
	CHANNEL                  ( "Channel", "Kanal" ) ,
	CHANNELS_IMPORTED        ( " channels added", " Kanäle hinzugefügt" ) ,
	CHECK                    ( "Check", "Teste" ) ,
	COPY_DEFAULT_CONTROL_FILE( "Control file doesn't exist, file will be recreated?",
                               "Steuerdatei existiert nicht, soll Datei neu erstellt werden?" ) ,
    DELETE                   ( "Delete", "Löschen" ) ,
	DATA_PATH                ( "Data path", "Daten-Pfad" ) ,
	DVBVIEWER                ( "DVBViewer" ) ,
	DVBVIEWER_ASSIGNMENT     ( "DVBViewer assignment", "DVBViewer-Zuordnung" ) ,
	DVBVIEWER_SERVICE        ( "DVBViewerService" ) ,
	ENABLE                   ( "Enable", "Aktivieren" ) ,
	END                      ( "End", "Ende" ) ,
	ERROR_READING_FILE       ( "Error on reading file ", "Fehler beim Lesen der Datei " ) ,
	EXECUTE                  ( "Execute", "Ausführen" ) ,
	EXECUTING                ( "Executing", "Ausführend" ) ,
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
	MORE_NOFO_LOG            ( "More information in log file ",
			                   "Mehr Information im Log-File " ) ,
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
	SELECT                   ( "Select", "Auswählen" ) ,
	SELECT_CHANNEL_FILE      ( "Select the DVBViewer channel file",
			                   "Wähle das DVBViewer-Channel-File aus" ) ,
	SETUP_CHANGED            ( "Setup is changed!\nTerminating without saving?",
	                           "Einstellungen wurden verändert!\nBeenden ohne Speichern?" ) ,
	SETUP_SAVE               ( "Setup is changed!\nShould the setup saved??",
			                   "Einstellungen wurden verändert!\nSollen sie gespeichert werden?" ) ,
	SEPARATOR                ( "Separator", "Trennzeichen" ) ,
	START                    ( "Start" ) ,
	SUCCESSFULL              ( "Successful", "Erfolgreich" ) ,
	TIME_BEFORE              ( "Lead time", "Vorlauf" ) ,
	TIME_AFTER               ( "Lag time", "Nachlauf" ) ,
	TRIGGER_ACTION           ( "Trigger action" ) ,
	UNINSTALL                ( "Uninstall", "Deinstallieren" ) ,
	UNLOCK                   ( "Unlock", "Freigeben" ) ,
	UPDATE_CHANNELS          ( "Update channels from new version", "Aktualisiere Kanäle aus neuer Version" ) ,
	UPDATE_LIST              ( "Update timer list", "Aktualisiere Timer-Liste" ) ,
	URL                      ( "URL" ),
	USER_NAME                ( "Username", "Kennung" ) ,
	VERBOSE                  ( "Verbose", "Ausführlich" ) ,
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
		NONE       (  0,  0, true,  "Nothing", "Keine Aktion" ) ,
		POWER_OFF  (  1,  1, true,  "Shutdown", "Herunterfahren" ) ,
		STANDBY    (  2,  2, true,  "Standby" ) ,
		HIBERNATE  (  3,  3, true,  "Hibernate", "Ruhemodus" ),
		CLOSE      (  4,  0, false, "Close DVBViewer", "Schließe DVBViewer" ),
		PLAYLIST   (  5,  0, false, "Playlist", "Starte Playlist" ),
		SLUMBERMODE(  6,  2, false, "Sumbermode", "DVBViewer-Standby" ),
		DEFAULT    ( -1, -1, true,  "Default", "Voreinstellung" );

		private final int id ;
		private final int serviceID ;
		private final boolean hasService ;
		private final String [] items ;
		
		private static ActionAfterItems [] serviceValues = null ;

		private ActionAfterItems( int id, int serviceID, boolean hasService, String... strings )
		{
			this.id = id ;
			this.serviceID = serviceID ;
			this.items = strings ;
			this.hasService = hasService ;
		}
		@Override
		public String toString() { return GUIStrings.get( this.items ) ; } ;
		public ActionAfterItems get( boolean isService )
		{
			if ( ! isService || this.hasService )
				return this ;
			return ActionAfterItems.get( this.serviceID ) ;
		}
		public int getID() { return this.id ; } ;
		public int getServiceID() { return this.serviceID ; } ;
		public static ActionAfterItems get( final int id )
		{
			for ( ActionAfterItems i : ActionAfterItems.values() )
			{
				if ( id == i.id )
					return i ;
			}
			return ActionAfterItems.NONE ;
		}
		public static ActionAfterItems [] values( boolean isService )
		{
			if ( ! isService )
				return ActionAfterItems.values() ;
			
			if ( ActionAfterItems.serviceValues != null )
				return ActionAfterItems.serviceValues ;
			
			ArrayList< ActionAfterItems > list = new ArrayList< ActionAfterItems >() ;
			for ( ActionAfterItems i : ActionAfterItems.values() )
				if ( !isService || i.hasService )
					list.add( i ) ;
			ActionAfterItems.serviceValues = new ActionAfterItems []{ ActionAfterItems.NONE } ;
			ActionAfterItems.serviceValues = list.toArray( ActionAfterItems.serviceValues ) ;
 			return ActionAfterItems.serviceValues ;
		}
	}
	public enum TimerActionItems
	{
		RECORD      (  0,  0, true,  "Record", "Aufnahme" ) ,
		TUNE        (  1,  1, true,  "Tune channel", "Sender einstellen" ) ,
		AUDIO_PLUGIN(  2,  0, false, "Audiorecorder plugin", "Audiorecorder Plugin" ) ,
		VIDEO_PLUGIN(  3,  0, false, "Videorecorder plugin", "Videorecorder Plugin" ),
		DEFAULT     ( -1, -1, true,  "Default", "Voreinstellung" );

		private final int id ;
		private final int serviceID ;
		private final boolean hasService ;
		private final String [] items ;
		
		private static TimerActionItems [] serviceValues = null ;

		private TimerActionItems( int id, int serviceID, boolean hasService, String... strings )
		{
			this.id = id ;
			this.serviceID = serviceID ;
			this.items = strings ;
			this.hasService = hasService ;
		}
		@Override
		public String toString() { return GUIStrings.get( this.items ) ; } ;
		public TimerActionItems get( boolean isService )
		{
			if ( ! isService || this.hasService )
				return this ;
			return TimerActionItems.get( this.serviceID ) ;
		}
		public int getID() { return this.id ; } ;
		public int getServiceID() { return this.serviceID ; } ;
		public static TimerActionItems get( final int id )
		{
			for ( TimerActionItems i : TimerActionItems.values() )
			{
				if ( id == i.id )
					return i ;
			}
			return TimerActionItems.RECORD ;
		}
		public static TimerActionItems [] values( boolean isService )
		{
			if ( ! isService )
				return TimerActionItems.values() ;
			
			if ( TimerActionItems.serviceValues != null )
				return TimerActionItems.serviceValues ;
			
			ArrayList< TimerActionItems > list = new ArrayList< TimerActionItems >() ;
			for ( TimerActionItems i : TimerActionItems.values() )
				if ( !isService || i.hasService )
					list.add( i ) ;
			TimerActionItems.serviceValues = new TimerActionItems []{ TimerActionItems.RECORD } ;
			TimerActionItems.serviceValues = list.toArray( TimerActionItems.serviceValues ) ;
 			return TimerActionItems.serviceValues ;
		}
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
