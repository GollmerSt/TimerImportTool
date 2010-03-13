// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.gui;

public class GUIStrings {
	private enum Language { INVALID, ENGLISH, GERMAN } ;
	private static Language languageEnum = Language.INVALID ;
	private static final String[] languagesInternal= { "", "en", "de" } ;
	public static final String[][] languageStrings= {
														{ "System", "English",  "German"  } ,
														{ "System", "Englisch", "Deutsch" }
													} ;
	
	private static final String[] add = { "Add", "Hinzufügen" } ;
	private static final String[] allTimers = { "All timers", "Alle Timer" } ;
	private static final String[] apply = { "Apply", "Übernehmen" } ;
	private static final String[] assignedError = { "is assigned to be reassigned?", "ist zugeordnet, soll neu zugeordnet werden?" } ;
	private static final String[] broadCastAddress = { "Broadast address", "Broadcast-Adresse" } ;
	private static final String[] cancel = { "Cancel", "Abbruch" } ;
	private static final String[] cannotDeleted = { "Cannot be deleted in case of assignment", "Ist zugeordnet, kann daher nicht gelöscht werden" } ;
	private static final String[] channel = { "Channel", "Kanal" } ;
	private static final String[] check = { "Check", "Teste" } ;
	private static final String[] copyDefaultControlFile = { "Control file doesn't exist, file will be recreated?",
		                                                     "Steuerdatei existiert nicht, soll Datei neu erstellt werden?" } ;
	private static final String[] delete = { "Delete", "Löschen" } ;
	private static final String[] dvbViewer = { "DVBViewer" } ;
	private static final String[] dvbViewerAssignment = { "DVBViewer assignment",
											       "DVBViewer-Zuordnung" } ;
	private static final String[] dvbViewerService = { "DVBViewerService" } ;
	private static final String[] enable = { "Enable", "Aktivieren" } ;
	private static final String[] end = { "End", "Ende" } ;
	private static final String[] execute = { "Execute", "Ausführen" } ;
	private static final String[] executing = { "Executing", "Ausführend" } ;
	private static final String[] failed = { "Failed", "Fehler" } ;
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
	
	private static final String[] uninstall = { "Uninstall", "Deinstallieren" } ;
	private static final String[] unlock = { "Unlock", "Freigeben" } ;
	private static final String[] url = { "URL" } ;
	private static final String[] userName = { "Username", "Kennung" } ;
	private static final String[] weekDays = { "Mo  Tu   We  Th   Fr   Sa   Su", "Mo   Di    Mi   Do   Fr   Sa   So" } ;
	private static final String[] verbose = { "Verbose", "Ausführlich" } ;
	private static final String[] wol = { "WOL" } ;
	private static final String[] waitTime = { "Wait time after WOL", "Warte-Zeit nach WOL" } ;
	

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
	public static String[] languageStrings()
	{
		if ( languageStrings.length < GUIStrings.languageEnum.ordinal() )
			return languageStrings[ 0 ] ;
		else
			return languageStrings[ GUIStrings.languageEnum.ordinal() - 1 ] ;
	}
	public static String toInternalLanguage( String language )
	{
		for ( int ix = 0 ; ix < GUIStrings.languageStrings().length ; ix++ )
			if ( GUIStrings.languageStrings()[ ix ].equals( language ) )
				return  GUIStrings.languagesInternal[ ix ] ;
		return( "" ) ;
	}
	public static String getLanguage( String internalLanguage )
	{
		for ( int ix = 0 ; ix < GUIStrings.languagesInternal.length ; ix++ )
			if ( GUIStrings.languagesInternal[ ix ].equals( internalLanguage ) )
				return  GUIStrings.languageStrings()[ ix ] ;
		return( "" ) ;
	}
	
	private static String get( String[] strings )
	{
		if ( strings.length < GUIStrings.languageEnum.ordinal() )
			return strings[ 0 ] ;
		else
			return strings[ GUIStrings.languageEnum.ordinal() - 1 ] ;
	}
	public static String add() { return get( add ) ; } ;
	public static String allTimers() { return get( allTimers ) ; } ;
	public static String apply() { return get( apply ) ; } ;
	public static String assignedError() { return get( assignedError ) ; } ;
	public static String broadCastAddress() { return get( broadCastAddress ) ; } ;
	public static String cancel() { return get( cancel ) ; } ;
	public static String cannotDeleted() { return get( cannotDeleted ) ; } ;
	public static String channel() { return get( channel ) ; } ;
	public static String check() { return get( check ) ; } ;
	public static String copyDefaultControlFile() { return get( copyDefaultControlFile ) ; } ;
	public static String delete() { return get( delete ) ; } ;
	public static String dvbViewer() { return get( dvbViewer ) ; } ;
	public static String dvbViewerAssignment() { return get( dvbViewerAssignment ) ; } ;
	public static String dvbViewerService() { return get( dvbViewerService ) ; } ;
	public static String enable() { return get( enable ) ; } ;
	public static String end() { return get( end ) ; } ;
	public static String execute() { return get( execute ) ; } ;
	public static String executing() { return get( executing ) ; } ;
	public static String failed() { return get( failed ) ; } ;
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
	public static String url() { return get( url ) ; } ;
	public static String userName() { return get( userName ) ; } ;
	public static String verbose() { return get( verbose ) ; } ;
	public static String waitTime() { return get( waitTime ) ; } ;
	public static String weekDays() { return get( weekDays ) ; } ;
	public static String wol() { return get( wol ) ; } ;
}
