package dvbviewertimerimport.tvheadend.binobjects;

public class HtspObject {
	private final String name;
	private final Object obj;

	public HtspObject(String name, Object obj) {
		this.name = name;
		this.obj = obj;
	}

	public String getName() {
		return this.name;
	}

	public Object getObj() {
		return this.obj;
	}

}
