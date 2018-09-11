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

import com.mobius.software.amqp.parser.header.api.AMQPHeader;
import com.mobius.software.amqp.performance.controller.api.ConnectionListener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class AmqpHandler extends SimpleChannelInboundHandler<AMQPHeader>
{
	private ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners;

	public AmqpHandler(ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners)
	{
		this.clientListeners = clientListeners;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, AMQPHeader message) throws Exception
	{
		SocketAddress address = ctx.channel().localAddress();
		ConnectionListener currListener = clientListeners.get(address);
		if (currListener != null)
			currListener.packetReceived(address, message);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		SocketAddress address = ctx.channel().localAddress();
		ConnectionListener currListener = clientListeners.remove(address);
		if (currListener != null)
			currListener.connectionDown(address);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx)
	{
		ctx.flush();
	}
}
