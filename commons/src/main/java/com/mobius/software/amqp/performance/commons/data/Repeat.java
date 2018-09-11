package com.mobius.software.amqp.performance.commons.data;

public class Repeat
{
	private Long interval;
	private Integer count;

	public Repeat()
	{

	}

	public Long getInterval()
	{
		return interval;
	}

	public void setInterval(Long interval)
	{
		this.interval = interval;
	}

	public Integer getCount()
	{
		return count;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public boolean validate()
	{
		return interval != null && interval >= 0 && count != null && count >= 0;
	}

	@Override
	public String toString()
	{
		return "Repeat [interval=" + interval + ", count=" + count + "]";
	}
}
