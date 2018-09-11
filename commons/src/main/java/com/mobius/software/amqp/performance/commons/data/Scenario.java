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

import java.util.List;
import java.util.UUID;

import com.mobius.software.amqp.performance.api.json.GenericJsonRequest;

@SuppressWarnings("serial")
public class Scenario extends GenericJsonRequest
{
	private UUID id;
	private ScenarioProperties properties;
	private Integer count;
	private Integer threshold;
	private Integer startThreshold;
	private Boolean continueOnError;
	private List<Command> commands;

	public Scenario()
	{
	}

	public boolean validate()
	{
		if (commands != null)
		{
			for (Command command : commands)
			{
				if (!command.validate())
				{
					System.out.println("invalid:" + command.getCode());
					return false;
				}
			}
		}

		return properties != null && properties.validate() && count != null && threshold != null && continueOnError != null && commands != null && !commands.isEmpty() && startThreshold != null;
	}

	@Override
	public String toString()
	{
		return "Scenario [id=" + id + ", properties=" + properties + ", count=" + count + ", threshold=" + threshold + ", startThreshold=" + startThreshold + ", continueOnError=" + continueOnError + ", commands=" + commands + "]";
	}

	public UUID getId()
	{
		return id;
	}

	public void setId(UUID id)
	{
		this.id = id;
	}

	public ScenarioProperties getProperties()
	{
		return properties;
	}

	public void setProperties(ScenarioProperties properties)
	{
		this.properties = properties;
	}

	public Integer getCount()
	{
		return count;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public Integer getThreshold()
	{
		return threshold;
	}

	public void setThreshold(Integer threshold)
	{
		this.threshold = threshold;
	}

	public Integer getStartThreshold()
	{
		return startThreshold;
	}

	public void setStartThreshold(Integer startThreshold)
	{
		this.startThreshold = startThreshold;
	}

	public List<Command> getCommands()
	{
		return commands;
	}

	public void setCommands(List<Command> commands)
	{
		this.commands = commands;
	}

	public Boolean getContinueOnError()
	{
		return continueOnError;
	}

	public void setContinueOnError(Boolean continueOnError)
	{
		this.continueOnError = continueOnError;
	}
}
