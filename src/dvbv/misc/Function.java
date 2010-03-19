// $LastChangedDate: 2010-03-13 10:38:41 +0100 (Sa, 13. Mrz 2010) $
// $LastChangedRevision: 197 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.misc;

import java.util.ArrayList;
import java.util.Iterator;

public class Function {
	public String stringToString( String in )
	{
		return in ;
	}
	public int arrayIntToInt( final ArrayList< Integer > list, final int integer, final String search, final String array )
	{
		int weight = 0 ;
		int sLength = search.length() ;
		int aLength = array.length() ;
		for ( Iterator< Integer > it = list.iterator() ; it.hasNext() ; )
		{
			int val = it.next();
			weight += (int) Math.pow( val, 1.5) ;
		}
		weight *= 500 ;
		weight =   weight/(int)Math.pow( sLength, 1.5 )
		         + weight/(int)Math.pow( aLength, 1.5 ) ;
		return integer*500/sLength + integer*500/aLength + weight ;
	}
	public int arrayIntToInt2( final ArrayList< Integer > list, final int integer, final String search, final String array )
	{
		int max = 0 ;
		for ( Iterator< Integer > it = list.iterator() ; it.hasNext() ; )
			max = Math.max( max, it.next().intValue() ) ;
		return max + integer ;
	}
	public int arrayIntToInt3( final ArrayList< Integer > list, final int integer, final String search, final String array )
	{
		int weight = 0 ;
		int sLength = search.length() ;
		for ( Iterator< Integer > it = list.iterator() ; it.hasNext() ; )
		{
			int val = it.next();
			weight += (int) Math.pow( val, 1.5) ;
		}
		weight *= 1000 ;
		weight =   weight/(int)Math.pow( sLength, 1.5 ) ;
		return integer*1000/sLength + weight ;
	}
}
