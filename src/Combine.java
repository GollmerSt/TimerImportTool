// $LastChangedDate: 2010-01-28 09:11:48 +0100 (Do, 28 Jan 2010) $
// $LastChangedRevision: 15 $
// $LastChangedBy: Stefan Gollmer $


public class Combine {
	private boolean combine = false ;
	private boolean valid ;
	Combine( boolean valid )
	{
		this.valid = valid ;
	}
	public void set( boolean c ) { this.combine = c ; } ;
	public boolean toCombine() { return this.combine ; } ;
	public void setValid() { this.valid = true ; } ;
	public boolean isValid() { return this.valid ; } ;

}
