package com.mobius.software.amqp.performance.commons.data;

import com.mobius.software.amqp.parser.avps.ReceiveCode;
import com.mobius.software.amqp.parser.avps.SendCode;

public class Link
{
	private Long handle;
	private String name;
	private boolean settleRequired;

	private Link(Long handle, String name, boolean requireSettle)
	{
		this.handle = handle;
		this.name = name;
		this.settleRequired = requireSettle;
	}

	public static Link withSettleRequired(Long handle, String name)
	{
		return new Link(handle, name, true);
	}

	public static Link withoutSettleRequired(Long handle, String name)
	{
		return new Link(handle, name, false);
	}

	public static Link key(String name)
	{
		return new Link(null, name, false);
	}

	public int retrieveQos()
	{
		return settleRequired ? 1 : 0;
	}

	public SendCode retrieveSendCode()
	{
		return settleRequired ? SendCode.MIXED : SendCode.SETTLED;
	}

	public ReceiveCode retrieveReceiveCode()
	{
		return settleRequired ? ReceiveCode.SECOND : ReceiveCode.FIRST;
	}

	@Override
	public String toString()
	{
		return "Link [handle=" + handle + ", name=" + name + ", settleRequired=" + settleRequired + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Link other = (Link) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Long getHandle()
	{
		return handle;
	}

	public void setHandle(Long handle)
	{
		this.handle = handle;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isSettleRequired()
	{
		return settleRequired;
	}
}
