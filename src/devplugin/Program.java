package devplugin;

public abstract class Program {

	public abstract Channel getChannel();

	public abstract void validateMarking();

	public abstract String getTitle();

	public abstract String getUniqueID();

	public abstract Date getDate();

	public abstract int getMinutes();

	public abstract int getHours();

	public abstract int getLength();

}
