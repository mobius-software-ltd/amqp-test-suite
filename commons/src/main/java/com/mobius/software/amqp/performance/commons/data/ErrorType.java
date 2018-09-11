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

import java.util.HashMap;
import java.util.Map;

import com.mobius.software.mqtt.parser.avps.MessageType;

public enum ErrorType
{
	CONNECT(0), CONNACK(1), SUBSCRIBE(2), SUBACK(3), UNSUBSCRIBE(4), UNSUBACK(5), PUBLISH(6), PUBACK(7), PUBREC(8), PUBREL(9), PUBCOMP(10), PINGREQ(11), PINGRESP(12), DISCONNECT(13), CONNECTION_LOST(14), PREVIOUS_COMMAND_FAILED(15), DUPLICATE(16);

	private static final Map<Integer, ErrorType> intToTypeMap = new HashMap<Integer, ErrorType>();
	private static final Map<String, ErrorType> strToTypeMap = new HashMap<String, ErrorType>();

	static
	{
		for (ErrorType type : ErrorType.values())
		{
			intToTypeMap.put(type.value, type);
			strToTypeMap.put(type.name(), type);
		}
	}

	public static ErrorType fromInt(int i)
	{
		return intToTypeMap.get(Integer.valueOf(i));
	}

	int value;

	private ErrorType(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}

	public static ErrorType forValue(String value)
	{
		Integer intValue = null;
		try
		{
			intValue = Integer.parseInt(value);
		}
		catch (Exception ex)
		{

		}

		if (intValue != null)
			return intToTypeMap.get(intValue);
		else
			return strToTypeMap.get(value);
	}

	public static ErrorType forMessageType(MessageType messageType)
	{
		return strToTypeMap.get(messageType.toString());
	}
}
