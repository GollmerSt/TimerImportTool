// $LastChangedDate: 2010-02-02 20:15:15 +0100 (Di, 02. Feb 2010) $
// $LastChangedRevision: 79 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.gui;

public class GUIStrings {
	public enum Language { INVALID, ENGLISH, GERMAN } ;
	private static Language language = Language.INVALID ;
	private static final String[] languageStrings= { "", "English", "German" } ;
	
	private static final String[] add = { "Add", "Hinzufügen" } ;
	private static final String[] allTimers = { "All timers", "Alle Timer" } ;
	private static final String[] apply = { "Apply", "Übernehmen" } ;
	private static final String[] assignedError = { "is assigned to be reassigned?", "ist zugeordnet, soll neu zugeordnet werden?" } ;
	private static final String[] broadCastAddress = { "Broadast address", "Broadcast-Adresse" } ;
	private static final String[] cancel = { "Cancel", "Abbruch" } ;
	private static final String[] cannotDeleted = { "Cannot be deleted in case of assignment", "Ist zugeordnet, kann daher nicht gelöscht werden" } ;
	private static final String[] channel = { "Channel", "Kanal" } ;
	private static final String[] check = { "Check", "Teste" } ;
	private static final String[] delete = { "Delete", "Löschen" } ;
	private static final String[] dvbViewer = { "DVBViewer" } ;
	private static final String[] dvbViewerAssignment = { "DVBViewer assignment",
											       "DVBViewer-Zuordnung" } ;
	private static final String[] dvbViewerService = { "DVBViewerService" } ;
	private static final String[] enable = { "Enable", "Aktivieren" } ;
	private static final String[] execute = { "Execute", "Ausführen" } ;
	private static final String[] executing = { "Executing", "Ausführend" } ;
	private static final String[] failed = { "Failed", "Fehler" } ;
	private static final String[] global = { "Global" } ;
	private static final String[] globalOffsets = { "Global offsets ...", "Globale Vor-/Nachlaufzeiten ..." } ;
	private static final String[] install = { "Install", "Installieren" } ;
	private static final String[] unlock = { "Unlock", "Freigeben" } ;
	private static final String[] macAddress = { "MAC address", "MAC-Adresse" } ;
	private static final String[] merge = { "Merge", "Verbinden" } ;
	private static final String[] message = { "Message", "Meldung" } ;
	private static final String[] modify = { "Modify", "Modifiziere" } ;
	private static final String[] no = { "No", "Nein" } ;
	private static final String[] offsets = { "Offsets ...", "Vor-/Nachlaufzeiten ..." } ;
	private static final String[] ok = { "OK" } ;
	private static final String[] pass = { "Pass", "Bestanden" } ;
	private static final String[] password = { "Password", "Passwort" } ;
	private static final String[] provider = { "Provider", "Anbieter" } ;
	private static final String[] providerService = { "Provider / Service",
    										   "Anbieter / Service" } ;
	private static final String[] providerAssignment = { "Provider assignment",
	   "Anbieter-Zuordnung" } ;
	private static final String[] successful = { "Successful", "Erfolgreich" } ;
	private static final String[] triggerAction = { "Trigger action" } ;
	private static final String[] uninstall = { "Uninstall", "Deinstallieren" } ;
	private static final String[] url = { "URL" } ;
	private static final String[] userName = { "Username", "Kennung" } ;
	private static final String[] verbose = { "Verbose", "Ausführlich" } ;
	private static final String[] wol = { "WOL" } ;
	private static final String[] waitTime = { "Wait time after WOL", "Warte-Zeit nach WOL" } ;
	
	public static void setLanguage(  Language language )
	{
		GUIStrings.language = language ;
	} ;
	private static String get( String[] strings )
	{
		if ( strings.length < GUIStrings.language.ordinal() )
			return strings[ 0 ] ;
		else
			return strings[ GUIStrings.language.ordinal() - 1 ] ;
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
	public static String delete() { return get( delete ) ; } ;
	public static String dvbViewer() { return get( dvbViewer ) ; } ;
	public static String dvbViewerAssignment() { return get( dvbViewerAssignment ) ; } ;
	public static String dvbViewerService() { return get( dvbViewerService ) ; } ;
	public static String enable() { return get( enable ) ; } ;
	public static String execute() { return get( execute ) ; } ;
	public static String executing() { return get( executing ) ; } ;
	public static String failed() { return get( failed ) ; } ;
	public static String global() { return get( global ) ; } ;
	public static String globalOffsets() { return get( globalOffsets ) ; } ;
	public static String install() { return get( install ) ; } ;
	public static String macAddress() { return get( macAddress ) ; } ;
	public static String merge() { return get( merge ) ; } ;
	public static String message() { return get( message ) ; } ;
	public static String modify() { return get( modify ) ; } ;
	public static String no() { return get( no ) ; } ;
	public static String offsets() { return get( offsets ) ; } ;
	public static String pass() { return get( pass ) ; } ;
	public static String password() { return get( password ) ; } ;
	public static String ok() { return get( ok ) ; } ;
	public static String provider() { return get( provider ) ; } ;
	public static String providerService() { return get( providerService ) ; } ;
	public static String providerAssignment() { return get( providerAssignment ) ; } ;
	public static String successful  () { return get( successful ) ; } ;
	public static String triggerAction() { return get( triggerAction ) ; } ;
	public static String uninstall() { return get( uninstall ) ; } ;
	public static String unlock() { return get( unlock ) ; } ;
	public static String url() { return get( url ) ; } ;
	public static String userName() { return get( userName ) ; } ;
	public static String verbose() { return get( verbose ) ; } ;
	public static String waitTime() { return get( waitTime ) ; } ;
	public static String wol() { return get( wol ) ; } ;
}
