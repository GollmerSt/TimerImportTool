// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package DVBViewer ;

public class Merge {
	private boolean merge = false ;
	private boolean valid ;
	public Merge( boolean valid )
	{
		this.valid = valid ;
	}
	public void set( boolean c ) { this.merge = c ; } ;
	public boolean toMerge() { return this.merge ; } ;
	public void setValid() { this.valid = true ; } ;
	public boolean isValid() { return this.valid ; } ;

}
