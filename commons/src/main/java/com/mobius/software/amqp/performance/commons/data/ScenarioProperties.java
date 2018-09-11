/**
 * Mobius Software LTD
 * Copyright 2015-2016, Mobius Software LTD
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.mobius.software.amqp.performance.commons.data;

public class ScenarioProperties
{
	private String serverHostname;
	private Integer serverPort;
	private Integer scenarioDelay;
	private Long resendInterval;
	private Long minPingInterval;
	private String identifierRegex;
	private Integer startIdentifier;
	private Repeat repeat;
	private String username;
	private String password;

	public ScenarioProperties()
	{

	}

	public ScenarioProperties(String serverHostname, Integer serverPort, Integer scenarioDelay, Long resendInterval, Long minPingInterval, String identifierRegex, Integer startIdentifer, Repeat repeat, String username, String password)
	{
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.scenarioDelay = scenarioDelay;
		this.resendInterval = resendInterval;
		this.minPingInterval = minPingInterval;
		this.identifierRegex = identifierRegex;
		this.startIdentifier = startIdentifer;
		this.repeat = repeat;
	}

	@Override
	public String toString()
	{
		return "ScenarioProperties [serverHostname=" + serverHostname + ", serverPort=" + serverPort + ", scenarioDelay=" + scenarioDelay + ", resendInterval=" + resendInterval + ", minPingInterval=" + minPingInterval + ", identifierRegex=" + identifierRegex + ", startIdentifier=" + startIdentifier + ", repeat=" + repeat + ", username=" + username + ", password=" + password + "]";
	}

	public boolean validate()
	{
		if (repeat != null && !repeat.validate())
			return false;

		return serverHostname != null && serverPort != null && resendInterval != null && minPingInterval != null && identifierRegex != null && startIdentifier != null && scenarioDelay != null && username != null && password != null;
	}

	public String getServerHostname()
	{
		return serverHostname;
	}

	public void setServerHostname(String serverHostname)
	{
		this.serverHostname = serverHostname;
	}

	public Integer getServerPort()
	{
		return serverPort;
	}

	public void setServerPort(Integer serverPort)
	{
		this.serverPort = serverPort;
	}

	public Integer getScenarioDelay()
	{
		return scenarioDelay;
	}

	public void setScenarioDelay(Integer scenarioDelay)
	{
		this.scenarioDelay = scenarioDelay;
	}

	public Long getResendInterval()
	{
		return resendInterval;
	}

	public void setResendInterval(Long resendInterval)
	{
		this.resendInterval = resendInterval;
	}

	public Long getMinPingInterval()
	{
		return minPingInterval;
	}

	public void setMinPingInterval(Long minPingInterval)
	{
		this.minPingInterval = minPingInterval;
	}

	public String getIdentifierRegex()
	{
		return identifierRegex;
	}

	public void setIdentifierRegex(String identifierRegex)
	{
		this.identifierRegex = identifierRegex;
	}

	public Integer getStartIdentifier()
	{
		return startIdentifier;
	}

	public void setStartIdentifier(Integer startIdentifier)
	{
		this.startIdentifier = startIdentifier;
	}

	public Repeat getRepeat()
	{
		return repeat;
	}

	public void setRepeat(Repeat repeat)
	{
		this.repeat = repeat;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
}
