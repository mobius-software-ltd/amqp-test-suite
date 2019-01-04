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

package com.mobius.software.amqp.performance.commons.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mobius.software.amqp.parser.avps.HeaderCode;
import com.mobius.software.amqp.parser.avps.ReceiveCode;
import com.mobius.software.amqp.parser.avps.RoleCode;
import com.mobius.software.amqp.parser.avps.SendCode;
import com.mobius.software.amqp.parser.avps.TerminusDurability;
import com.mobius.software.amqp.parser.header.api.AMQPHeader;
import com.mobius.software.amqp.parser.header.impl.*;
import com.mobius.software.amqp.parser.terminus.AMQPSource;
import com.mobius.software.amqp.parser.terminus.AMQPTarget;
import com.mobius.software.amqp.performance.commons.data.Command;
import com.mobius.software.amqp.performance.commons.data.Property;
import com.mobius.software.amqp.performance.commons.data.PropertyType;

public class CommandParser
{
	public static boolean validate(Command command)
	{
		if (command == null || command.getCode() == null)
			return false;

		Map<PropertyType, String> propertyMap = command.getCommandProperties() != null ? command.getCommandProperties().stream()//
				.collect(Collectors.toMap(p -> p.getType(), p -> p.getValue())) : Collections.emptyMap();
		try
		{
			switch (command.getCode())
			{
			case OPEN:
				String idleTimeoutVal = propertyMap.get(PropertyType.IDLE_TIMEOUT);
				if (idleTimeoutVal == null || Long.parseLong(idleTimeoutVal) < 0)
					return false;
				return true;

			case ATTACH:
				String attachRole = propertyMap.get(PropertyType.ROLE);
				if (attachRole == null || RoleCode.valueOf(attachRole) == null)
					return false;
				if (StringUtils.isEmpty(propertyMap.get(PropertyType.ADDRESS)))
					return false;
				String attachRcvMode = propertyMap.get(PropertyType.RCV_MODE);
				if (StringUtils.isEmpty(attachRcvMode) || ReceiveCode.valueOf(attachRcvMode) == null)
					return false;
				String attachSndMode = propertyMap.get(PropertyType.SND_MODE);
				if (StringUtils.isEmpty(attachSndMode) || SendCode.valueOf(attachSndMode) == null)
					return false;
				return true;

			case DETACH:
				if (StringUtils.isEmpty(propertyMap.get(PropertyType.ADDRESS)))
					return false;
				return true;

			case TRANSFER:
				if (StringUtils.isEmpty(propertyMap.get(PropertyType.ADDRESS)))
					return false;
				String resendTimeValue = propertyMap.get(PropertyType.RESEND_TIME);
				if (StringUtils.isEmpty(resendTimeValue) || Integer.parseInt(resendTimeValue) < 0)
					return false;
				String count = propertyMap.get(PropertyType.COUNT);
				if (StringUtils.isEmpty(count) || Integer.parseInt(count) < 1)
					return false;
				String messageLengthValue = propertyMap.get(PropertyType.MESSAGE_LENGTH);
				if (StringUtils.isEmpty(messageLengthValue) || Integer.parseInt(messageLengthValue) < 0)
					return false;
				return true;

			case PROTO:
				String protoVersion = propertyMap.get(PropertyType.VERSION);
				if (StringUtils.isEmpty(protoVersion) || (Integer.parseInt(protoVersion) != 0 && Integer.parseInt(protoVersion) != 3))
					return false;
				return true;
				
			case END:
			case CLOSE:
			case PING:
			case BEGIN:
			case INIT:
				return true;

			default:
				return false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static AMQPHeader toMessage(Command command, String clientID)
	{
		Map<PropertyType, String> propertyMap = command.getCommandProperties() != null ? command.getCommandProperties().stream()//
				.collect(Collectors.toMap(p -> p.getType(), p -> p.getValue())) : Collections.emptyMap();

		switch (command.getCode())
		{
		case PROTO:
			return new AMQPProtoHeader(Integer.parseInt(propertyMap.get(PropertyType.VERSION)));

		case INIT:
			return SASLInit.builder()//
					.mechanism("PLAIN").build();

		case OPEN:
			return AMQPOpen.builder()//
					.containerId(clientID)//
					.idleTimeout(Long.parseLong(propertyMap.get(PropertyType.IDLE_TIMEOUT)))//
					.build();

		case BEGIN:
			return AMQPBegin.builder()//
					.channel(1)//
					.handleMax(1)//
					.nextOutgoingId(0)//
					.incomingWindow(2147483647)//
					.outgoingWindow(0)//
					.build();

		case PING:
			return AMQPPing.instance;

		case ATTACH:
			if (RoleCode.valueOf(propertyMap.get(PropertyType.ROLE)) == RoleCode.RECEIVER)
				return AMQPAttach.builder()//
						.channel(1)//
						.name(propertyMap.get(PropertyType.ADDRESS))//
						.role(RoleCode.valueOf(propertyMap.get(PropertyType.ROLE)))//
						.sndSettleMode(SendCode.valueOf(propertyMap.get(PropertyType.SND_MODE)))//
						.rcvSettleMode(ReceiveCode.valueOf(propertyMap.get(PropertyType.RCV_MODE)))//
						.target(AMQPTarget.builder()//
								.address(propertyMap.get(PropertyType.ADDRESS))//
								.durable(TerminusDurability.NONE)//
								.timeout(0L)//
								.dynamic(false)//
								.build())//
						.build();
			else 
				return AMQPAttach.builder()//
						.channel(1)//
						.name(propertyMap.get(PropertyType.ADDRESS))//
						.role(RoleCode.valueOf(propertyMap.get(PropertyType.ROLE)))//
						.sndSettleMode(SendCode.valueOf(propertyMap.get(PropertyType.SND_MODE)))//
						.rcvSettleMode(ReceiveCode.valueOf(propertyMap.get(PropertyType.RCV_MODE)))//
						.initialDeliveryCount(1L)//
						.source(AMQPSource.builder()//
								.address(propertyMap.get(PropertyType.ADDRESS))//
								.durable(TerminusDurability.NONE)//
								.timeout(0L)//
								.dynamic(false)//
								.build())//
						.build();

		case DETACH:
			return AMQPDetach.builder()//
					.channel(1)//
					.closed(true)//
					.build();

		case TRANSFER:
			return AMQPTransfer.builder()//
					.channel(1).build();
		case END:
			return AMQPEnd.builder().channel(1).build();

		case CLOSE:
			return AMQPClose.builder().build();

		default:
			return null;
		}
	}

	public static ConcurrentLinkedQueue<Command> retrieveCommands(List<Command> commands, int repeatCount, long repeatInterval)
	{
		ConcurrentLinkedQueue<Command> queue = new ConcurrentLinkedQueue<>();
		long currInterval = 0L;
		while (repeatCount-- > 0)
		{
			for (int i = 0; i < commands.size(); i++)
			{
				Command command = commands.get(i);
				if (i == 0)
					command = new Command(command.getCode(), command.getSendTime() + currInterval, command.getCommandProperties());
				queue.offer(command);
				if (command.getCode() == HeaderCode.TRANSFER)
				{
					long resendTime = retrieveIntegerProperty(command, PropertyType.RESEND_TIME);
					Integer count = retrieveIntegerProperty(command, PropertyType.COUNT);
					while (count-- > 1)
					{
						Command publish = new Command(command.getCode(), resendTime, command.getCommandProperties());
						queue.offer(publish);
					}
				}
			}
			currInterval = repeatInterval;
		}
		return queue;
	}

	private static Integer retrieveIntegerProperty(Command command, PropertyType type)
	{
		Integer value = null;
		for (Property property : command.getCommandProperties())
		{
			if (property.getType() == type)
			{
				value = Integer.parseInt(property.getValue());
				break;
			}
		}
		return value;
	}
}
