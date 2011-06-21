// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.misc;

import java.util.ArrayList;


public class Enums {
	public enum Merge { INVALID, TRUE, FALSE } ;

	
	public enum ActionAfterItems
	{
		NONE       (  0,  0, true ) ,
		POWER_OFF  (  1,  1, true ) ,
		STANDBY    (  2,  2, true ) ,
		HIBERNATE  (  3,  3, true ),
		CLOSE      (  4,  0, false ),
		PLAYLIST   (  5,  0, false ),
		SLUMBERMODE(  6,  2, false ),
		DEFAULT    ( -1, -1, true );

		private final int id ;
		private final int serviceID ;
		private final boolean hasService ;
		private String message ;
		
		private static ActionAfterItems [] serviceValues = null ;

		private ActionAfterItems( int id, int serviceID, boolean hasService )
		{
			this.id = id ;
			this.serviceID = serviceID ;
			this.message = null ;
			
			this.hasService = hasService ;
		}
		@Override
		public String toString()
		{
			if ( message == null )
				this.message = ResourceManager.msg( "ActionAfterItems." + this.name() ) ;
			return message ;
		} ;
		public ActionAfterItems get( boolean isService )
		{
			if ( ! isService || this.hasService )
				return this ;
			return ActionAfterItems.get( this.serviceID ) ;
		}
		public int getID() { return this.id ; } ;
		public int getServiceID() { return this.serviceID ; } ;
		public static ActionAfterItems get( final int id )
		{
			for ( ActionAfterItems i : ActionAfterItems.values() )
			{
				if ( id == i.id )
					return i ;
			}
			return ActionAfterItems.NONE ;
		}
		public static ActionAfterItems [] values( boolean isService )
		{
			if ( ! isService )
				return ActionAfterItems.values() ;
			
			if ( ActionAfterItems.serviceValues != null )
				return ActionAfterItems.serviceValues ;
			
			ArrayList< ActionAfterItems > list = new ArrayList< ActionAfterItems >() ;
			for ( ActionAfterItems i : ActionAfterItems.values() )
				if ( !isService || i.hasService )
					list.add( i ) ;
			ActionAfterItems.serviceValues = new ActionAfterItems []{ ActionAfterItems.NONE } ;
			ActionAfterItems.serviceValues = list.toArray( ActionAfterItems.serviceValues ) ;
			return ActionAfterItems.serviceValues ;
		}
	}
	public enum TimerActionItems
	{
		RECORD      (  0,  0, true ) ,
		TUNE        (  1,  1, true ) ,
		AUDIO_PLUGIN(  2,  0, false ) ,
		VIDEO_PLUGIN(  3,  0, false ),
		DEFAULT     ( -1, -1, true );

		private final int id ;
		private final int serviceID ;
		private final boolean hasService ;
		private String message ;
		
		private static TimerActionItems [] serviceValues = null ;

		private TimerActionItems( int id, int serviceID, boolean hasService )
		{
			this.id = id ;
			this.serviceID = serviceID ;
			this.message = null ;
			this.hasService = hasService ;
		}
		@Override
		public String toString()
		{
			if ( message == null )
				this.message = ResourceManager.msg( "TimerActionItems." + this.name() ) ;
			return message ;
		} ;
		public TimerActionItems get( boolean isService )
		{
			if ( ! isService || this.hasService )
				return this ;
			return TimerActionItems.get( this.serviceID ) ;
		}
		public int getID() { return this.id ; } ;
		public int getServiceID() { return this.serviceID ; } ;
		public static TimerActionItems get( final int id )
		{
			for ( TimerActionItems i : TimerActionItems.values() )
			{
				if ( id == i.id )
					return i ;
			}
			return TimerActionItems.RECORD ;
		}
		public static TimerActionItems [] values( boolean isService )
		{
			if ( ! isService )
				return TimerActionItems.values() ;
			
			if ( TimerActionItems.serviceValues != null )
				return TimerActionItems.serviceValues ;
			
			ArrayList< TimerActionItems > list = new ArrayList< TimerActionItems >() ;
			for ( TimerActionItems i : TimerActionItems.values() )
				if ( !isService || i.hasService )
					list.add( i ) ;
			TimerActionItems.serviceValues = new TimerActionItems []{ TimerActionItems.RECORD } ;
			TimerActionItems.serviceValues = list.toArray( TimerActionItems.serviceValues ) ;
			return TimerActionItems.serviceValues ;
		}
	}

}
