// $LastChangedDate: 2010-05-03 09:11:15 +0200 (Mo, 03. Mai 2010) $
// $LastChangedRevision: 238 $
// $LastChangedBy: Stefan Gollmer $

package dvbviewertimerimport.misc ;

public class TerminateClass extends Error
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4617207761538088601L;
	
	private final int exitCode ;
	
	public TerminateClass()
	{
		this( 0 ) ;
	}
	
	public TerminateClass( int exitCode)
	{
//		super( "Terminated" ) ;
		this.exitCode = exitCode ;
	}
	public int getExitCode()
	{
		return this.exitCode ;
	}
}
