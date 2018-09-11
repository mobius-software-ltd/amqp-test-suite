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

package com.mobius.software.amqp.performance.controller.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ConnectionContext
{
	private Boolean cleanSession;
	private Integer keepalive;
	private String clientID;

	private SocketAddress clientAddress;
	private SocketAddress serverAddress;
	private Long resendInterval;

	private String username;
	private String password;

	public ConnectionContext(InetSocketAddress serverAddress, Long resendInterval, String username, String password)
	{
		this.serverAddress = serverAddress;
		this.resendInterval = resendInterval;
		this.username = username;
		this.password = password;
	}

	public Boolean getCleanSession()
	{
		return cleanSession;
	}

	public Integer getKeepalive()
	{
		return keepalive;
	}

	public Long getResendInterval()
	{
		return resendInterval;
	}

	public String getClientID()
	{
		return clientID;
	}

	public SocketAddress localAddress()
	{
		return clientAddress;
	}

	public void updateLocalAddress(SocketAddress clientAddress)
	{
		this.clientAddress = clientAddress;
	}

	public SocketAddress remoteAddress()
	{
		return serverAddress;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
}
