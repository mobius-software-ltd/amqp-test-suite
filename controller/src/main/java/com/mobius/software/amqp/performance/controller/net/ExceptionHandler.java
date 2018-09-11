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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

@Sharable
public class ExceptionHandler extends ChannelDuplexHandler
{
	private static final Log logger = LogFactory.getLog(ExceptionHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		exceptionCaught(ctx.channel(), ctx.channel().localAddress(), cause);
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
	{
		try
		{
			ctx.connect(remoteAddress, localAddress, promise.addListener(new ChannelFutureListener()
			{
				@Override
				public void operationComplete(ChannelFuture future)
				{
					if (!future.isSuccess())
						exceptionCaught(ctx.channel(), localAddress, future.cause());
				}
			}));
		}
		catch (Exception e)
		{
			exceptionCaught(ctx, e);
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
	{
		ctx.write(msg, promise.addListener(new ChannelFutureListener()
		{
			@Override
			public void operationComplete(ChannelFuture future)
			{
				if (!future.isSuccess())
					exceptionCaught(ctx.channel(), ctx.channel().localAddress(), future.cause());
			}
		}));
	}

	private void exceptionCaught(Channel channel, SocketAddress localAddress, Throwable cause)
	{
		cause.printStackTrace();
		logger.warn(cause.getMessage() + "," + localAddress, cause);

		if (channel != null && channel.isOpen())
			channel.close();
	}
}