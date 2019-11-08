package gengenv2.structures;

public class Root extends Morpheme
{
	private boolean isBound;
	
	public Root (boolean isBound)
	{
		this.isBound = isBound;
	}
	
	public boolean isBound()
	{
		return isBound;
	}
	
	public String toString()
	{
		String result = "*" + super.toString();
		if (isBound)
			result += "-";
		return result;
	}
}
