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

package com.mobius.software.amqp.performance.controller.net;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import com.mobius.software.amqp.performance.controller.api.ClientBootstrap;
import com.mobius.software.amqp.performance.controller.api.ConnectionListener;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class AmqpClientBootstrap extends ClientBootstrap
{
	public AmqpClientBootstrap(ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners)
	{
		super(clientListeners);
	}

	public void init(SocketAddress serverAddress) throws InterruptedException
	{
		this.serverAddress = serverAddress;
		if (pipelineInitialized.compareAndSet(false, true))
		{
			bootstrap.group(loopGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.TCP_NODELAY, true);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>()
			{
				@Override
				protected void initChannel(SocketChannel socketChannel) throws Exception
				{
					socketChannel.pipeline().addLast(new AmqpDecoder());
					socketChannel.pipeline().addLast(new AmqpEncoder());
					socketChannel.pipeline().addLast("handler", new AmqpHandler(clientListeners));
					socketChannel.pipeline().addLast(new ExceptionHandler());
				}
			});
			bootstrap.remoteAddress(serverAddress);
		}
	}
}
