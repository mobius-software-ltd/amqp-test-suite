package controller;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.mobius.software.amqp.performance.controller.IdentifierStorage;

public class CheckTest
{
	@Test
	public void test()
	{
		try
		{
			IdentifierStorage identifierStorage = new IdentifierStorage();
			long start = System.currentTimeMillis();
			for (int i = 1; i < 100001; i++)
				identifierStorage.countIdentity("regex", i);
			
			System.out.println(System.currentTimeMillis() - start);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}
}
