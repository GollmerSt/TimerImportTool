package dvbviewertimerimport.provider;

public interface ProviderProperties {
	
	String getName();

 	boolean hasAccount();
	boolean hasURL();
	boolean canExecute() ;
	boolean canTest() ;
	boolean filter() ;
	boolean mustInstall() ;
	boolean silent() ;
	boolean isOutDatedLimitsEnabled() ;
	boolean canImport() ;
	boolean canModify() ;
	boolean isFunctional() ;


}
