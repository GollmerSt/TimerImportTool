// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.gui;

public class GUIStrings {
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
	public static String actionAfter()            { return get( "Post record action", "Nach Aufnahme" ) ; } ;
	public static String add()                    { return get( "Add", "Hinzufügen" ) ; } ;
	public static String allTimers()              { return get( "All timers", "Alle Timer" ) ; } ;
	public static String apply()                  { return get( "Apply", "Übernehmen" ) ; } ;
	public static String assignedError()          { return get( "is assigned to be reassigned?", "ist zugeordnet, soll neu zugeordnet werden?" ) ; } ;
	public static String broadCastAddress()       { return get( "Broadast address", "Broadcast-Adresse" ) ; } ;
	public static String cancel()                 { return get( "Cancel", "Abbruch" ) ; } ;
	public static String changeEffect()           { return get( "The change will take effect after restarting the program ",
			                                                    "Die Änderung ist erst nach einem Neustart des Programms wirksam" ) ; } ;
	public static String cannotDeleted()          { return get( "Cannot be deleted in case of assignment", "Ist zugeordnet, kann daher nicht gelöscht werden" ) ; } ;
	public static String channel()                { return get( "Channel", "Kanal" ) ; } ;
	public static String channelsImported()       { return get( " channels added", " Kanäle hinzugefügt" ) ; } ;

	public static String check()                  { return get( "Check", "Teste" ) ; } ;
	public static String copyDefaultControlFile() { return get( "Control file doesn't exist, file will be recreated?",
                                                                "Steuerdatei existiert nicht, soll Datei neu erstellt werden?" ) ; } ;
    public static String delete()                 { return get( "Delete", "Löschen" ) ; } ;
	public static String dataPath()               { return get( "Data path", "Daten-Pfad" ) ; } ;
	public static String dvbViewer()              { return get( "DVBViewer" ) ; } ;
	public static String dvbViewerAssignment()    { return get( "DVBViewer assignment",
		                                                        "DVBViewer-Zuordnung" ) ; } ;
	public static String dvbViewerService()       { return get( "DVBViewerService" ) ; } ;
	public static String enable()                 { return get( "Enable", "Aktivieren" ) ; } ;
	public static String end()                    { return get( "End", "Ende" ) ; } ;
	public static String execute()                { return get( "Execute", "Ausführen" ) ; } ;
	public static String executing()              { return get( "Executing", "Ausführend" ) ; } ;
	public static String failed()                 { return get( "Failed", "Fehler" ) ; } ;
	public static String filter()                 { return get( "Filter", "Filter" ) ; } ;
	public static String global()                 { return get( "Global" ) ; } ;
	public static String globalOffsets()          { return get( "Global lead / lag times ...", "Globale Vor- / Nachlaufzeiten ..." ) ; } ;
	public static String gui()                    { return get( "GUI" ) ; } ;
	public static String importTV()               { return get( "Import ", "Importiere " ) ; } ;
	public static String install()                { return get( "Install", "Installieren" ) ; } ;
	public static String language()               { return get( "Language", "Sprache" ) ; } ;
	public static String macAddress()             { return get( "MAC address", "MAC-Adresse" ) ; } ;
	public static String merge()                  { return get( "Merge", "Verbinden" ) ; } ;
	public static String message()                { return get( "Message", "Meldung" ) ; } ;
	public static String miscellaneous()          { return get( "Miscellaneous", "Verschiedenes" ) ; } ;
	public static String missingSince()           { return get( "Missing since", "Fehlend seit" ) ; } ;
	public static String missingSinceSync()       { return get( "Missing since Sync.", "Fehlend seit Synchr." ) ; } ;
	public static String modify()                 { return get( "Modify", "Modifiziere" ) ; } ;
	public static String newEntry()               { return get( "New entry", "Neuer Eintrag" ) ; } ;
	public static String no()                     { return get( "No", "Nein" ) ; } ;
	public static String offsetDialog()           { return get( "Lead / Lag times", "Vor- / Nachlaufzeiten" ) ; } ;
	public static String offsets()                { return get( "Lead / Lag times ...", "Vor- / Nachlaufzeiten ..." ) ; } ;
	public static String ok()                     { return get( "OK" ) ; } ;
	public static String pass()                   { return get( "Pass", "Bestanden" ) ; } ;
	public static String password()               { return get( "Password", "Passwort" ) ; } ;
	public static String provider()               { return get( "Provider", "Anbieter" ) ; } ;
	public static String providerService()        { return get( "Provider / Service", "Anbieter / Service" ) ; } ;
	public static String providerAssignment()     { return get( "Provider assignment", "Anbieter-Zuordnung" ) ; } ;
	public static String select()                 { return get( "Select", "Auswählen" ) ; } ;
	public static String selectChannelFile()      { return get( "Select the DVBViewer channel file", "Wähle das DVBViewer-Channel-File aus" ) ; } ;
	public static String setupChanged()           { return get( "Setup is changed!\nTerminating without saving?",
	                                                            "Einstellungen wurden verändert!\nBeenden ohne Speichern?" ) ; } ;
	public static String setupSave()              { return get( "Setup is changed!\nShould the setup saved??",
	                                                            "Einstellungen wurden verändert!\nSollen sie gespeichert werden?" ) ; } ;
	public static String separator()              { return get( "Separator", "Trennzeichen" ) ; } ;
	public static String start()                  { return get( "Start" ) ; } ;
	public static String successful()             { return get( "Successful", "Erfolgreich" ) ; } ;
	public static String timeBefore()             { return get( "Lead time", "Vorlauf" ) ; } ;
	public static String timeAfter()              { return get( "Lag time", "Nachlauf" ) ; } ;
	public static String triggerAction()          { return get( "Trigger action" ) ; } ;
	public static String uninstall()              { return get( "Uninstall", "Deinstallieren" ) ; } ;
	public static String unlock()                 { return get( "Unlock", "Freigeben" ) ; } ;
	public static String updateList()             { return get( "update timer list", "aktualisiere Timer-Liste" ) ; } ;
	public static String url()                    { return get( "URL" ) ; } ;
	public static String userName()               { return get( "Username", "Kennung" ) ; } ;
	public static String verbose()                { return get( "Verbose", "Ausführlich" ) ; } ;
	public static String view()                   { return get( "View", "Ansicht" ) ; } ;
	public static String waitTime()               { return get( "Wait time after WOL", "Warte-Zeit nach WOL" ) ; } ;
	public static String weekDays()               { return get( "Mo  Tu   We  Th   Fr   Sa   Su", "Mo   Di    Mi   Do   Fr   Sa   So" ) ; } ;
	public static String wol()                    { return get( "WOL" ) ; } ;
}
