// $LastChangedDate: 2010-03-13 10:38:41 +0100 (Sa, 13. Mrz 2010) $
// $LastChangedRevision: 197 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.provider;

import javax.xml.stream.XMLStreamException;

import dvbv.javanet.staxutils.IndentingXMLStreamWriter;
import dvbv.misc.Constants;
import dvbv.misc.ErrorClass;

public class OutDatedInfo implements Cloneable
{
	private static long now = System.currentTimeMillis() ;
	
	private long missingSince ;
	private int missingSyncSince ;
	public OutDatedInfo( long missingSince, int missingSyncSince)
	{
		this.missingSince     = missingSince ;
		this.missingSyncSince = missingSyncSince ;
	}
	public OutDatedInfo()
	{
		this.missingSince     = -1 ;
		this.missingSyncSince = 0 ;
	}
	@Override
	public OutDatedInfo clone()
	{
		OutDatedInfo result = new OutDatedInfo( this.missingSince, this.missingSyncSince) ;
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
		this.missingSince = 0 ;
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
		if ( provInfo == null )
			return false ;
		if ( provInfo.missingSince + OutDatedInfo.now >= this.missingSince )
			return false ;
		if ( provInfo.missingSyncSince >= this.missingSyncSince )
			return false ;
		return true ;
	}
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
	}
	public void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException
	{
		sw.writeAttribute( "missingSince",     Long.toString( this.missingSince ) ) ;
		sw.writeAttribute( "missingSyncSince", Integer.toString( this.missingSyncSince ) ) ;
	}
}

