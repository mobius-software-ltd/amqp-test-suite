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

package com.mobius.software.amqp.performance.controller.api;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public abstract class AbstractClient implements NetworkHandler
{
	protected final Log logger = LogFactory.getLog(this.getClass());

	protected ConcurrentHashMap<SocketAddress, ClientBootstrap> bootstraps = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<SocketAddress, Channel> clientChannels = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners = new ConcurrentHashMap<>();

	public abstract void init(SocketAddress serverAddress);

	public ChannelFuture connect(SocketAddress serverAddress)
	{
		ClientBootstrap bootstrap = bootstraps.get(serverAddress);
		if (bootstrap != null)
			return bootstrap.createConnection();

		return null;
	}

	public SocketAddress finishConnection(ChannelFuture future, ConnectionListener listener)
	{
		if (future != null)
		{
			Channel channel = future.channel();
			SocketAddress localAddress = channel.localAddress();
			clientListeners.put(localAddress, listener);
			clientChannels.put(localAddress, channel);
			return localAddress;
		}

		return null;
	}

	public void shutdown() throws InterruptedException
	{
		Iterator<Entry<SocketAddress, Channel>> iterator = clientChannels.entrySet().iterator();
		while (iterator.hasNext())
		{
			try
			{
				iterator.next().getValue().close().sync();
				iterator.remove();
			}
			catch (Exception e)
			{
				logger.error("An error occured while performing shutdown: client channel close failed - " + e.getMessage());
			}
		}
	}

	@Override
	public void close(SocketAddress address)
	{
		Channel channel = clientChannels.remove(address);
		if (channel != null)
		{
			try
			{
				channel.close().sync();
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	@Override
	public void releaseLocalPort(SocketAddress serverAddress, int localPort)
	{
		ClientBootstrap bootstrap = this.bootstraps.get(serverAddress);
		if (bootstrap != null)
			bootstrap.releaseLocalPort(localPort);
	}
}
