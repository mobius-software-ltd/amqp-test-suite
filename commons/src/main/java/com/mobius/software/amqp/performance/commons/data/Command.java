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

import com.mobius.software.amqp.parser.avps.HeaderCode;
import com.mobius.software.amqp.performance.commons.util.CommandParser;

public class Command
{
	private HeaderCode code;
	private Long sendTime;
	private List<Property> commandProperties;

	public Command()
	{
	}

	public Command(HeaderCode code, Long sendTime, List<Property> commandProperties)
	{
		this.code = code;
		this.sendTime = sendTime;
		this.commandProperties = commandProperties;
	}

	public HeaderCode getCode()
	{
		return code;
	}

	public void setCode(HeaderCode code)
	{
		this.code = code;
	}

	public Long getSendTime()
	{
		return sendTime;
	}

	public void setSendTime(Long sendTime)
	{
		this.sendTime = sendTime;
	}

	public List<Property> getCommandProperties()
	{
		return commandProperties;
	}

	public void setCommandProperties(List<Property> commandProperties)
	{
		this.commandProperties = commandProperties;
	}

	public boolean validate()
	{
		return code != null && sendTime != null && CommandParser.validate(this);
	}

	@Override
	public String toString()
	{
		return "Command [code=" + code + ", sendTime=" + sendTime + ", commandProperties=" + commandProperties + "]";
	}
}
