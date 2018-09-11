/**
 * Mobius Software LTD Copyright 2015-2016, Mobius Software LTD
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package com.mobius.software.amqp.performance.commons.data;

import java.util.List;

public class ClientController
{
	private String hostname;
	private Integer port;
	private String identifierRegex;
	private Integer startIdentifier;
	private List<ScenarioRequest> requests;

	public ClientController()
	{

	}

	public String getHostname()
	{
		return hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public Integer getPort()
	{
		return port;
	}

	public void setPort(Integer port)
	{
		this.port = port;
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

	public List<ScenarioRequest> getRequests()
	{
		return requests;
	}

	public void setRequests(List<ScenarioRequest> requests)
	{
		this.requests = requests;
	}

	public boolean validate()
	{
		if (port == null || port < 1 || port > 65535)
			return false;

		if (requests == null || requests.isEmpty())
			return false;

		for (ScenarioRequest request : requests)
		{
			if (!request.validate())
				return false;
		}

		return hostname != null && identifierRegex != null && startIdentifier != null;
	}
}
