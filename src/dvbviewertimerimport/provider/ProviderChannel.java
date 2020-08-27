package dvbviewertimerimport.provider;

public class ProviderChannel< T >
{
	String name ;
	T key ;
	
	public ProviderChannel( final String name, final T key )
	{
		this.name = name ;
		this.key = key ;
	}
	public void setName( final String name )
	{
		this.name = name ;
	}
	public void setKey( final T key)
	{
		this.key = key ;
	}
	public String getName()
	{
		return this.name ;
	}
	public T getKey()
	{
		return this.key ;
	}
	@Override
	public String toString()
	{
		return "Name: " + this.name + ", Key: " + this.key.toString() ;
	}
}
