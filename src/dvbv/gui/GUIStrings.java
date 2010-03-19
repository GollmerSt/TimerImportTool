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
		INVALID( "", "System", "System" ),
		ENGLISH( "en", "English", "Englisch" ),
		GERMAN( "de", "German", "Deutsch" ) ;
		
		private final String shortForm ;
		private final String [] strings ;
		
		Language( String shortForm, String ... strings )
		{
			this.shortForm = shortForm ;
			this.strings = strings ;
		}
		@Override
		public String toString() { return GUIStrings.get( this.strings ) ; } ;
		public String getShort() { return this.shortForm ; } ;
	}
	public static Language languageEnum = Language.INVALID ;

	public static void setLanguage( final String language )
	{
		String l = language ;
		if ( l.length() == 0 )
			l = System.getProperty( "user.language") ;
		
		if (    l.equals( "de" )
				 || l.equals( "at" )
				 || l.equals( "ch" )
				 || l.equals( "li" )
				 || l.equals( "lu" ) )
			GUIStrings.languageEnum = Language.GERMAN ;
		else
			GUIStrings.languageEnum = Language.ENGLISH ;
	} ;


	private static final String[] filter = { "Filter", "Filter" } ;
	private static final String[] global = { "Global" } ;
	private static final String[] globalOffsets = { "Global lead / lag times ...", "Globale Vor- / Nachlaufzeiten ..." } ;
	private static final String[] importTV = { "Import ", "Importiere " } ;
	private static final String[] install = { "Install", "Installieren" } ;
	private static final String[] language = { "Language", "Sprache" } ;
	private static final String[] macAddress = { "MAC address", "MAC-Adresse" } ;
	private static final String[] merge = { "Merge", "Verbinden" } ;
	private static final String[] message = { "Message", "Meldung" } ;
	private static final String[] miscellaneous = { "Miscellaneous", "Verschiedenes" } ;
	private static final String[] missingSince = { "Missing since", "Fehlend seit" } ;
	private static final String[] missingSinceSync = { "Missing since Sync.", "Fehlend seit Synchr." } ;
	private static final String[] modify = { "Modify", "Modifiziere" } ;
	private static final String[] newEntry = { "New entry", "Neuer Eintrag" } ;
	private static final String[] no = { "No", "Nein" } ;
	private static final String[] offsetDialog = { "Lead / Lag times", "Vor- / Nachlaufzeiten" } ;
	private static final String[] offsets = { "Lead / Lag times ...", "Vor- / Nachlaufzeiten ..." } ;
	private static final String[] ok = { "OK" } ;
	private static final String[] pass = { "Pass", "Bestanden" } ;
	private static final String[] password = { "Password", "Passwort" } ;
	private static final String[] provider = { "Provider", "Anbieter" } ;
	private static final String[] providerService = { "Provider / Service",
    										   "Anbieter / Service" } ;
	private static final String[] providerAssignment = { "Provider assignment",
	   "Anbieter-Zuordnung" } ;
	private static final String[] setupChanged = { "Setup is changed!\nTerminating without saving?", "Einstellungen wurden verändert!\nBeenden ohne Speichern?" } ;
	private static final String[] setupSave = { "Setup is changed!\nShould the setup saved??", "Einstellungen wurden verändert!\nSollen sie gespeichert werden?" } ;
	private static final String[] separator = { "Separator", "Trennzeichen" } ;
	private static final String[] start = { "Start", "Start" } ;
	private static final String[] successful = { "Successful", "Erfolgreich" } ;
	private static final String[] timeBefore = { "Lead time", "Vorlauf" } ;
	private static final String[] timeAfter = { "Lag time", "Nachlauf" } ;
	private static final String[] triggerAction = { "Trigger action" } ;
	private static final String[] updateList = { "update timer list", "aktualisiere Timer-Liste" } ;
	private static final String[] uninstall = { "Uninstall", "Deinstallieren" } ;
	private static final String[] unlock = { "Unlock", "Freigeben" } ;
	private static final String[] url = { "URL" } ;
	private static final String[] userName = { "Username", "Kennung" } ;
	private static final String[] weekDays = { "Mo  Tu   We  Th   Fr   Sa   Su", "Mo   Di    Mi   Do   Fr   Sa   So" } ;
	private static final String[] verbose = { "Verbose", "Ausführlich" } ;
	private static final String[] wol = { "WOL" } ;
	private static final String[] waitTime = { "Wait time after WOL", "Warte-Zeit nach WOL" } ;
	

	private static String get( String... strings )
	{
		if ( strings.length < GUIStrings.languageEnum.ordinal() )
			return strings[ 0 ] ;
		else
			return strings[ GUIStrings.languageEnum.ordinal() - 1 ] ;
	}
	public static String actionAfter()            { return get( "Post record action", "Nach Aufnahme" ) ; } ;
	public static String add()                    { return get( "Add", "Hinzufügen" ) ; } ;
	public static String allTimers()              { return get( "All timers", "Alle Timer" ) ; } ;
	public static String apply()                  { return get( "Apply", "Übernehmen" ) ; } ;
	public static String assignedError()          { return get( "is assigned to be reassigned?", "ist zugeordnet, soll neu zugeordnet werden?" ) ; } ;
	public static String broadCastAddress()       { return get( "Broadast address", "Broadcast-Adresse" ) ; } ;
	public static String cancel()                 { return get( "Cancel", "Abbruch" ) ; } ;
	public static String cannotDeleted()          { return get( "Cannot be deleted in case of assignment", "Ist zugeordnet, kann daher nicht gelöscht werden" ) ; } ;
	public static String channel()                { return get( "Channel", "Kanal" ) ; } ;
	public static String check()                  { return get( "Check", "Teste" ) ; } ;
	public static String copyDefaultControlFile() { return get( "Control file doesn't exist, file will be recreated?",
                                                                "Steuerdatei existiert nicht, soll Datei neu erstellt werden?" ) ; } ;
	public static String delete()                 { return get( "Delete", "Löschen" ) ; } ;
	public static String dvbViewer()              { return get( "DVBViewer" ) ; } ;
	public static String dvbViewerAssignment()    { return get( "DVBViewer assignment",
		                                                        "DVBViewer-Zuordnung" ) ; } ;
	public static String dvbViewerService()       { return get( "DVBViewerService" ) ; } ;
	public static String enable()                 { return get( "Enable", "Aktivieren" ) ; } ;
	public static String end()                    { return get( "End", "Ende" ) ; } ;
	public static String execute()                { return get( "Execute", "Ausführen" ) ; } ;
	public static String executing()              { return get( "Executing", "Ausführend" ) ; } ;
	public static String failed()                 { return get( "Failed", "Fehler" ) ; } ;
	public static String filter() { return get( filter ) ; } ;
	public static String global() { return get( global ) ; } ;
	public static String globalOffsets() { return get( globalOffsets ) ; } ;
	public static String importTV() { return get( importTV ) ; } ;
	public static String install() { return get( install ) ; } ;
	public static String language() { return get( language ) ; } ;
	public static String macAddress() { return get( macAddress ) ; } ;
	public static String merge() { return get( merge ) ; } ;
	public static String message() { return get( message ) ; } ;
	public static String miscellaneous() { return get( miscellaneous ) ; } ;
	public static String missingSince() { return get( missingSince ) ; } ;
	public static String missingSinceSync() { return get( missingSinceSync ) ; } ;
	public static String modify() { return get( modify ) ; } ;
	public static String newEntry() { return get( newEntry ) ; } ;
	public static String no() { return get( no ) ; } ;
	public static String offsetDialog() { return get( offsetDialog ) ; } ;
	public static String offsets() { return get( offsets ) ; } ;
	public static String pass() { return get( pass ) ; } ;
	public static String password() { return get( password ) ; } ;
	public static String ok() { return get( ok ) ; } ;
	public static String provider() { return get( provider ) ; } ;
	public static String providerService() { return get( providerService ) ; } ;
	public static String providerAssignment() { return get( providerAssignment ) ; } ;
	public static String setupChanged() { return get( setupChanged ) ; } ;
	public static String setupSave() { return get( setupSave ) ; } ;
	public static String start() { return get( start ) ; } ;
	public static String separator() { return get( separator ) ; } ;
	public static String successful() { return get( successful ) ; } ;
	public static String timeBefore() { return get( timeBefore ) ; } ;
	public static String timeAfter() { return get( timeAfter ) ; } ;
	public static String triggerAction() { return get( triggerAction ) ; } ;
	public static String uninstall() { return get( uninstall ) ; } ;
	public static String unlock() { return get( unlock ) ; } ;
	public static String updateList() { return get( updateList ) ; } ;
	public static String url() { return get( url ) ; } ;
	public static String userName() { return get( userName ) ; } ;
	public static String verbose() { return get( verbose ) ; } ;
	public static String waitTime() { return get( waitTime ) ; } ;
	public static String weekDays() { return get( weekDays ) ; } ;
	public static String wol() { return get( wol ) ; } ;
}
