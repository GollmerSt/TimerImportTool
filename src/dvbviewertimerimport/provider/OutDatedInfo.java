// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

import javax.xml.stream.XMLStreamException;

import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;
import dvbviewertimerimport.misc.Constants;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.xml.Conversions;

public class OutDatedInfo implements Cloneable
{
	private static long now = System.currentTimeMillis() ;
	
	private boolean enabled = false ;
	private long missingSince ;
	private int missingSyncSince ;
	public OutDatedInfo( boolean enable, long missingSince, int missingSyncSince)
	{
		this.enabled          = enable ;
		this.missingSince     = missingSince ;
		this.missingSyncSince = missingSyncSince ;
	}
	public OutDatedInfo() { this( false ) ; } ;
	public OutDatedInfo( boolean isProvider )
	{
		if ( isProvider )
			this.missingSince     = 0 ;
		else
			this.missingSince     = -1 ;
		this.missingSyncSince = 0 ;
	}
	@Override
	public OutDatedInfo clone()
	{
		OutDatedInfo result = new OutDatedInfo( this.enabled, this.missingSince, this.missingSyncSince) ;
		return result ;
	}
	public void setMissing()
	{
		if ( this.missingSince < 0 )
				this.missingSince = OutDatedInfo.now ;
			this.missingSyncSince++ ;
		}
	public void resetMissing()
	{
		this.missingSince = -1 ;
		this.missingSyncSince = 0 ;
	}
	public boolean isValid()
	{
		if ( this.missingSince < 0 && this.missingSyncSince == 0 )
			return false ;
		return true ;
	}
	public boolean isOutdated( Provider provider)
	{
		if ( provider == null )
			return false ;
		OutDatedInfo provInfo = provider.getOutDatedLimits() ;
		if ( provInfo == null )
			return false ;
		if ( provInfo.enabled == false )
			return false ;
		if ( provInfo.missingSince + OutDatedInfo.now >= this.missingSince )
			return false ;
		if ( provInfo.missingSyncSince >= this.missingSyncSince )
			return false ;
		return true ;
	}
	public boolean isEnabled() { return this.enabled ; } ;
	public void setEnabled( boolean e ) { this.enabled = e ; } ;
	public int getMissingSince() { return (int)(this.missingSince / Constants.DAYMILLSEC) ; } ;
	public void setMissingSince( int m ) { this.missingSince = (long) m * Constants.DAYMILLSEC; } ;
	public int getMissingSyncSince() { return (int) this.missingSyncSince ; } ;
	public void setMissingSyncSince( int m ) { this.missingSyncSince = m ; } ;
	public void readXML( String key, String value )
	{
		if ( key.equals( "missingSince" ) || key.equals( "missingSyncSince" ) )
		{
        	if ( !value.matches("-*\\d+") )
        		throw new ErrorClass ( "Wrong " + key + " format" ) ;
        	long l = Long.valueOf( value ) ;
        	
        	if ( key.equals( "missingSince" ) )
        		missingSince = l ;
        	else if ( key.equals( "missingSyncSince" ) )
        		missingSyncSince = (int) l ;
		}
		else if ( key.equals( "enable" ) )
		{
			this.enabled = Conversions.getBoolean( value, null, null ) ;
		}
	}
	public void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException
	{
		sw.writeAttribute( "enable",           this.enabled  ) ;
		sw.writeAttribute( "missingSince",     Long.toString( this.missingSince ) ) ;
		sw.writeAttribute( "missingSyncSince", Integer.toString( this.missingSyncSince ) ) ;
	}
}

