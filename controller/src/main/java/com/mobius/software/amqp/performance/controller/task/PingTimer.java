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

package com.mobius.software.amqp.performance.controller.task;

import java.util.concurrent.atomic.AtomicLong;

import com.mobius.software.amqp.parser.avps.HeaderCode;
import com.mobius.software.amqp.parser.header.impl.AMQPPing;
import com.mobius.software.amqp.performance.controller.PeriodicQueuedTasks;
import com.mobius.software.amqp.performance.controller.api.NetworkHandler;
import com.mobius.software.amqp.performance.controller.client.ConnectionContext;
import com.mobius.software.amqp.performance.controller.client.IdentityReport;

public class PingTimer implements TimedTask
{
	private ConnectionContext ctx;
	private PeriodicQueuedTasks<TimedTask> scheduler;
	private NetworkHandler listener;
	private AtomicLong timestamp = new AtomicLong();
	private Long resendInterval;
	private IdentityReport report;

	public PingTimer(ConnectionContext ctx, PeriodicQueuedTasks<TimedTask> scheduler, NetworkHandler listener, Long resendInterval, IdentityReport report)
	{
		this.ctx = ctx;
		this.scheduler = scheduler;
		this.listener = listener;
		this.resendInterval = resendInterval;
		this.timestamp.set(System.currentTimeMillis() + resendInterval);;
		this.report = report;
	}

	@Override
	public Boolean execute()
	{
		if (timestamp.get() != Long.MAX_VALUE)
		{
			report.countOut(HeaderCode.PING);
			listener.send(ctx.localAddress(), AMQPPing.instance);
			timestamp.set(System.currentTimeMillis() + resendInterval);
			scheduler.store(timestamp.get(), this);
		}
		return true;
	}

	@Override
	public Long getRealTimestamp()
	{
		return timestamp.get();
	}

	@Override
	public void stop()
	{
		this.timestamp.set(Long.MAX_VALUE);
	}
}
