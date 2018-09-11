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

package com.mobius.software.amqp.performance.controller;

import java.net.InetSocketAddress;
import java.util.UUID;

import com.mobius.software.amqp.performance.commons.data.ScenarioProperties;

public class OrchestratorProperties
{
	private UUID scenarioID;
	private String serverHostname;
	private int scenarioDelay;
	private int serverPort;
	private int threashold;
	private int startThreashold;
	private long resendInterval;
	private long minPingInterval;
	private boolean continueOnError;
	private int initialDelay;
	private String identifierRegex;
	private int startIdentifier;

	private InetSocketAddress serverAddress;

	private OrchestratorProperties(UUID scenarioID, String serverHostname, int serverPort, int scenarioDelay, int threashold, int startThreashold, long resendInterval, long minPingInterval, boolean continueOnError, int initialDelay, String identifierRegex, int startIdentifier, InetSocketAddress serverAddress)
	{
		this.scenarioID = scenarioID;
		this.serverHostname = serverHostname;
		this.scenarioDelay = scenarioDelay;
		this.serverPort = serverPort;
		this.threashold = threashold;
		this.startThreashold = startThreashold;
		this.resendInterval = resendInterval;
		this.minPingInterval = minPingInterval;
		this.continueOnError = continueOnError;
		this.initialDelay = initialDelay;
		this.identifierRegex = identifierRegex;
		this.startIdentifier = startIdentifier;
		this.serverAddress = serverAddress;
	}

	public static OrchestratorProperties fromScenarioProperties(UUID scenarioID, ScenarioProperties properties, Integer threashold, Integer startThreashold, boolean continueOnError, Integer initialDelay)
	{
		InetSocketAddress serverAddress = new InetSocketAddress(properties.getServerHostname(), properties.getServerPort());
		return new OrchestratorProperties(scenarioID, properties.getServerHostname(), properties.getServerPort(), properties.getScenarioDelay(), threashold, startThreashold, properties.getResendInterval(), properties.getMinPingInterval(), continueOnError, initialDelay, properties.getIdentifierRegex(), properties.getStartIdentifier(), serverAddress);
	}

	public UUID getScenarioID()
	{
		return scenarioID;
	}

	public String getServerHostname()
	{
		return serverHostname;
	}

	public int getServerPort()
	{
		return serverPort;
	}

	public int getThreashold()
	{
		return threashold;
	}

	public int getScenarioDelay()
	{
		return scenarioDelay;
	}

	public int getStartThreashold()
	{
		return startThreashold;
	}

	public long getResendInterval()
	{
		return resendInterval;
	}

	public long getMinPingInterval()
	{
		return minPingInterval;
	}

	public boolean isContinueOnError()
	{
		return continueOnError;
	}

	public int getInitialDelay()
	{
		return initialDelay;
	}

	public String getIdentifierRegex()
	{
		return identifierRegex;
	}

	public int getStartIdentifier()
	{
		return startIdentifier;
	}

	public InetSocketAddress getServerAddress()
	{
		return serverAddress;
	}
}
