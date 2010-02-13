// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.tvinfo ;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dvbv.misc.* ;

public final class TVInfoRecording {
	public enum DeleteMode { KEEP, REMOVE, DELETE } ; // REMOVE: Delete from XML and DVBViewer
	private final long tvInfoID ;
	private final String channel ;
	private final long start ;
	private final long end ;
	private final String md5 ;
	private long missingSince    = 0 ;
	private int missingSyncSince = 0 ;
	private boolean updated      = false ;
	
	public TVInfoRecording( String tvInfoID,
			          String channel,
			          long start,
			          long end)
	{
		this.tvInfoID = Long.valueOf(tvInfoID) ;
		this.channel  = channel ;
		this.start    = start ;
		this.end      = end ;
		this.updated  = false ;
		this.md5 = calculateMD5() ;
	}
	public TVInfoRecording( String tvInfoID,
			          String channel,
			          String start,
			          String end,
			          String missingSince,
			          String missingSyncSince )
	{
			this.tvInfoID         = Long.valueOf(tvInfoID) ;
			this.channel          = channel ;
			this.start            = Long.valueOf(start) ;
			this.end              = Long.valueOf(end) ;
			this.missingSince     = Long.valueOf(missingSince) ;
			this.missingSyncSince = Integer.valueOf(missingSyncSince) ;
			this.updated  = false ;
			this.md5 = calculateMD5() ;
	}
	private String calculateMD5()
	{
		MessageDigest md = null ;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		md.update( Conversions.longToBytes( this.tvInfoID ) ) ;
		md.update( this.channel.getBytes() ) ;
		md.update( Conversions.longToBytes( this.start ) ) ;
		md.update( Conversions.longToBytes( this.end ) ) ;
		return Conversions.bytesToString( md.digest() ) ;
	}
	public DeleteMode toDelete( int days,
			                    int syncs,
			                    long now )
	{
		DeleteMode result = DeleteMode.KEEP ;
		if ( ! this.updated )
		{
			result = DeleteMode.REMOVE ;
			this.missingSyncSince++ ;
			if ( this.missingSyncSince < syncs )
				result = DeleteMode.KEEP ;
			if ( this.missingSince == 0 ) this.missingSince = now ;
			if ( days > 0 )
				if ( now - this.missingSince < days * 86400000 )
					result = DeleteMode.KEEP ;
		}
		if ( this.end + 86400000 < now )
			result = DeleteMode.DELETE ;
		return result ;
	}
	public String getHash() { return this.md5 ; } ;
	public boolean isUpdated(){ return updated ; } ;
	public void setUpdated(){
		this.updated          = true ;
		this.missingSince     = 0 ;
		this.missingSyncSince = 0 ;
	} ;
	public String getID() { return Long.toString(this.tvInfoID) ; } ;
	public String getChannel() { return this.channel ; } ;
	public String getStart() { return Long.toString(this.start) ; } ;
	public String getEnd() { return Long.toString(this.end) ; } ;
	public String getMissingSince() { return Long.toString(this.missingSince) ; } ;
	public String getMissingSyncSince() { return Long.toString(this.missingSyncSince) ; } ;

}
