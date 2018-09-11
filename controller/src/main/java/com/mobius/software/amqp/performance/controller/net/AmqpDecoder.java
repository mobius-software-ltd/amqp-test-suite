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

import java.util.List;

import com.mobius.software.amqp.parser.AMQPParser;
import com.mobius.software.amqp.parser.header.api.AMQPHeader;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class AmqpDecoder extends ByteToMessageDecoder
{
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out)
	{
		ByteBuf nextHeader = null;
		do
		{
			if (buf.readableBytes() >= 8)
			{
				try
				{
					nextHeader = AMQPParser.next(buf);
				}
				catch (IndexOutOfBoundsException ex)
				{
					ex.printStackTrace();
					buf.resetReaderIndex();
					if (nextHeader != null)
						nextHeader = null;
				}
			}

			if (nextHeader != null)
			{
				try
				{
					if (buf.readableBytes() >= nextHeader.capacity())
					{
						buf.readBytes(nextHeader, nextHeader.capacity());

						AMQPHeader header = AMQPParser.decode(nextHeader);
						out.add(header);
					}
					else
						break;
				}
				catch (Exception e)
				{
					buf.resetReaderIndex();
					ctx.channel().pipeline().remove(this);
					throw e;
				}
				finally
				{
					nextHeader.release();
				}
			}
		}
		while (buf.readableBytes() >= 8 && nextHeader != null);
	}
}
