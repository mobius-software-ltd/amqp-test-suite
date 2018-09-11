package com.mobius.software.amqp.performance.commons.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class AmqpInitialResponse
{
	private final String username;
	private final String password;

	public AmqpInitialResponse(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	public static final byte[] delimiter = new byte[]
	{ 0x00 };

	public static AmqpInitialResponse parse(byte[] initialResponse)
	{
		AmqpInitialResponse response = null;
		if (!ArrayUtils.isEmpty(initialResponse))
		{
			List<byte[]> segments = split(delimiter, initialResponse);
			if (segments.size() != 3)
				return null;

			String username = new String(segments.get(0));
			String password = new String(segments.get(2));
			if (!username.isEmpty() && !password.isEmpty())
				response = new AmqpInitialResponse(username, password);
		}
		return response;
	}

	public byte[] encodeChallenge()
	{
		return joinArray(username.getBytes(), delimiter, username.getBytes(), delimiter, password.getBytes());
	}

	private byte[] joinArray(byte[]... arrays)
	{
		int length = 0;
		for (byte[] array : arrays)
			length += array.length;

		final byte[] result = new byte[length];

		int offset = 0;
		for (byte[] array : arrays)
		{
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}

		return result;
	}

	public static List<byte[]> split(byte[] pattern, byte[] input)
	{
		List<byte[]> l = new LinkedList<byte[]>();
		int blockStart = 0;
		for (int i = 0; i < input.length; i++)
		{
			if (isMatch(pattern, input, i))
			{
				l.add(Arrays.copyOfRange(input, blockStart, i));
				blockStart = i + pattern.length;
				i = blockStart;
			}
		}
		l.add(Arrays.copyOfRange(input, blockStart, input.length));
		return l;
	}

	public static boolean isMatch(byte[] pattern, byte[] input, int pos)
	{
		for (int i = 0; i < pattern.length; i++)
		{
			if (pattern[i] != input[pos + i])
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "AmqpInitialResponse [username=" + username + ", password=" + password + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		AmqpInitialResponse other = (AmqpInitialResponse) obj;
		if (password == null)
		{
			if (other.password != null)
				return false;
		}
		else if (!password.equals(other.password))
			return false;
		if (username == null)
		{
			if (other.username != null)
				return false;
		}
		else if (!username.equals(other.username))
			return false;
		return true;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}
}
