package com.mobius.software.amqp.performance.runner.tests;

import static org.junit.Assert.fail;

import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mobius.software.amqp.performance.runner.util.TemplateParser;

public class RunnerTests
{
	private static final String MULTI_PUBLISHERS_QOS0 = "json/publishers_qos0.json";
	private static final String MULTI_PUBLISHERS_QOS1 = "json/publishers_qos1.json";
	private static final String MULTI_SUBSCRIBERS_QOS0 = "json/subscribers_qos0.json";
	private static final String MULTI_SUBSCRIBERS_QOS1 = "json/subscribers_qos1.json";
	private static final String PINGERS = "json/pingers.json";

	private static TemplateParser templateParser = new TemplateParser();

	static 
	{
		BasicConfigurator.configure();
	}
	
	@BeforeClass
	public static void beforeClass()
	{
		fillTemplateRemote();
		//fillTemplateLocal();
	}

	@Test
	public void test1000Publishers_qos0()
	{
		checkScenario(MULTI_PUBLISHERS_QOS0);
	}

	@Test
	public void test1000Publishers_qos1()
	{
		checkScenario(MULTI_PUBLISHERS_QOS1);
	}

	@Test
	public void test1000Subscribers_qos0()
	{
		checkScenario(MULTI_SUBSCRIBERS_QOS0);
	}

	@Test
	public void test1000Subscribers_qos1()
	{
		checkScenario(MULTI_SUBSCRIBERS_QOS1);
	}

	@Test
	public void test100kConnections()
	{
		checkScenario(PINGERS);
	}

	@Test
	public void testAll() throws InterruptedException
	{
		checkScenario(MULTI_PUBLISHERS_QOS0);
		sleepAndLog(10000);
		checkScenario(MULTI_PUBLISHERS_QOS1);
		sleepAndLog(10000);
		checkScenario(MULTI_SUBSCRIBERS_QOS0);
		sleepAndLog(10000);
		checkScenario(MULTI_SUBSCRIBERS_QOS1);
		sleepAndLog(10000);
		checkScenario(PINGERS);
	}

	private void sleepAndLog(long interval) throws InterruptedException
	{
		System.out.println("sleeping for " + interval);
		Thread.sleep(interval);
	}

	private void checkScenario(String filename)
	{
		System.out.println("starting scenario " + filename);
		try
		{
			runScenario(filename);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}

	private TestRunner runScenario(String scenarioFilename) throws InterruptedException
	{
		TestRunner runner = new TestRunner(scenarioFilename, templateParser);
		runner.start();
		runner.awaitFinished();
		return runner;
	}

	private static void fillTemplateRemote()
	{
		templateParser.addTemplate("{controller.1.ip}", "137.117.254.35");
		templateParser.addTemplate("{controller.1.port}", "9998");
		templateParser.addTemplate("{controller.2.ip}", "137.117.225.70");
		templateParser.addTemplate("{controller.2.port}", "9998");
		templateParser.addTemplate("{controller.3.ip}", "137.117.225.249");
		templateParser.addTemplate("{controller.3.port}", "9998");
		templateParser.addTemplate("{lb.ip}", "13.94.158.185");
		templateParser.addTemplate("{lb.port}", "5672");
		templateParser.addTemplate("{account.username}", "first@foo.bar");
		templateParser.addTemplate("{account.password}", "hash");
	}

	private static void fillTemplateLocal()
	{
		templateParser.addTemplate("{controller.1.ip}", "127.0.0.1");
		templateParser.addTemplate("{controller.1.port}", "9998");
		templateParser.addTemplate("{controller.2.ip}", "127.0.0.1");
		templateParser.addTemplate("{controller.2.port}", "9998");
		templateParser.addTemplate("{lb.ip}", "127.0.1.1");
		templateParser.addTemplate("{lb.port}", "5672");
		templateParser.addTemplate("{account.username}", "firstTestAccount");
		templateParser.addTemplate("{account.password}", "firstTestAccountMqttPassword111");
	}
}
