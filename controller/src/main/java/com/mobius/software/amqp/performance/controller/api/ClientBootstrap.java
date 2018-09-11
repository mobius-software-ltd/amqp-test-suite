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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobius.software.amqp.performance.controller.Config;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;

public abstract class ClientBootstrap
{
	protected static final Log logger = LogFactory.getLog(ClientBootstrap.class);

	protected Bootstrap bootstrap = new Bootstrap();
	protected AtomicBoolean pipelineInitialized = new AtomicBoolean(false);
	protected NioEventLoopGroup loopGroup = new NioEventLoopGroup(16);
	protected ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners;
	protected SocketAddress serverAddress;

	private ConcurrentHashMap<Integer, Boolean> usedPorts = new ConcurrentHashMap<>();
	private AtomicInteger currLocalPort = new AtomicInteger(10000);

	public ClientBootstrap(ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners)
	{
		this.clientListeners = clientListeners;
	}

	public abstract void init(SocketAddress serverAddress) throws InterruptedException;

	public ChannelFuture createConnection()
	{
		if (this.serverAddress == null)
			throw new IllegalStateException("bootstrap not initialized...");

		InetSocketAddress localAddress = nextLocalAddress();
		return bootstrap.connect(this.serverAddress, localAddress);
	}

	protected InetSocketAddress nextLocalAddress()
	{
		int port = 0;
		do
		{
			if (usedPorts.size() == 65535)
				throw new IllegalStateException("reached limit for number of connected clients");

			currLocalPort.compareAndSet(65535, 10000);
			port = currLocalPort.incrementAndGet();
			if (port == 21883)
				continue;
		}
		while (!available(Config.getInstance().getHostname(), port) || usedPorts.put(port, true) != null);

		return new InetSocketAddress(Config.getInstance().getHostname(), port);
	}

	public static boolean available(String host, int port)
	{
		try (ServerSocket ss = new ServerSocket(port, 50, InetAddress.getByName(host)))
		{
			ss.setReuseAddress(true);
			return true;
		}
		catch (IOException e)
		{
		}

		return false;
	}

	public void shutdown()
	{
		clearPorts();
		if (loopGroup != null)
		{
			Future<?> future = loopGroup.shutdownGracefully();
			try
			{
				future.await();
			}
			catch (InterruptedException e)
			{
				logger.error("An error occured while performing shutdown: interrupted loopGroup shutdown");
			}
		}
	}

	public void clearPorts()
	{
		this.usedPorts.clear();
		this.currLocalPort.set(10000);
	}

	public void releaseLocalPort(int port)
	{
		this.usedPorts.remove(port);
	}
}
