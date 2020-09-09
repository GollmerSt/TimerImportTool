package dvbviewertimerimport.tvheadend.objects;

public abstract class MainObject<T extends SubObject<T>> extends SubObject<T>{
	
	public String getObjectName() {
		String method = this.getClass().getSimpleName();
		return Character.toLowerCase(method.charAt(0)) + method.substring(1);
	}
}
