package com.mobius.software.amqp.performance.commons.data;

public class ScenarioRequest
{
	private String baseURL;
	private Long requestTimeout;
	private Scenario scenario;

	public ScenarioRequest()
	{

	}

	public ScenarioRequest(Long requestTimeout, Scenario scenario)
	{
		this.requestTimeout = requestTimeout;
		this.scenario = scenario;
	}

	public String retrieveBaseURL()
	{
		return baseURL;
	}

	public void updateBaseURL(String baseURL)
	{
		this.baseURL = baseURL;
	}

	public Scenario getScenario()
	{
		return scenario;
	}

	public void setScenario(Scenario scenario)
	{
		this.scenario = scenario;
	}

	public Long getRequestTimeout()
	{
		return requestTimeout;
	}

	public void setRequestTimeout(Long requestTimeout)
	{
		this.requestTimeout = requestTimeout;
	}

	public boolean validate()
	{
		return requestTimeout != null && requestTimeout >= 0 && scenario != null && scenario.validate();
	}

	@Override
	public String toString()
	{
		return "ScenarioRequest [baseURL=" + baseURL + ", requestTimeout=" + requestTimeout + ", scenario=" + scenario + "]";
	}

}
