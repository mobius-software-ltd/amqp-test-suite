package com.mobius.software.amqp.performance.controller.net;

import java.net.SocketAddress;

import com.mobius.software.amqp.parser.header.api.AMQPHeader;
import com.mobius.software.amqp.performance.controller.api.AbstractClient;
import com.mobius.software.amqp.performance.controller.api.ClientBootstrap;
import com.mobius.software.amqp.performance.controller.client.TestsuiteException;
import com.mobius.software.mqtt.parser.avps.MessageType;

import io.netty.channel.Channel;

public class AmqpClient extends AbstractClient
{
	public void init(SocketAddress serverAddress)
	{
		try
		{
			ClientBootstrap bootstrap = bootstraps.get(serverAddress);
			if (bootstrap == null)
			{
				bootstrap = new AmqpClientBootstrap(clientListeners);
				ClientBootstrap oldBootstrap = bootstraps.putIfAbsent(serverAddress, bootstrap);
				if (oldBootstrap != null)
					bootstrap = oldBootstrap;
				else
					bootstrap.init(serverAddress);
			}

			bootstrap.clearPorts();
		}
		catch (InterruptedException e)
		{
			throw new TestsuiteException(MessageType.CONNECT, "An error occured while establishing network connection: " + e.getMessage());
		}
	}

	@Override
	public void send(SocketAddress address, AMQPHeader message)
	{
		Channel channel = clientChannels.get(address);
		if (channel != null && channel.isOpen())
			channel.writeAndFlush(message);
	}
}
