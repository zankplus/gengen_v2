package gengenv2;

public class Test
{
	public static void main(String[] args)
	{
		int count = 1;
		for (int i = 0; i < 11; i++)
		{
			for (int j = 0; j < 16; j++)
			{
				System.out.print("$" + ++count);
				if (j < 15)
					System.out.print(",");
			}
			System.out.println("\\r\\n");
		}
	}
}
