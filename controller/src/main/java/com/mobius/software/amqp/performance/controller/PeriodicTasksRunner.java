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

package com.mobius.software.amqp.performance.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobius.software.amqp.performance.controller.task.TimedTask;

public class PeriodicTasksRunner implements Runnable
{
	private static final Log logger = LogFactory.getLog(PeriodicTasksRunner.class);

	private PeriodicQueuedTasks<TimedTask> timers;

	public PeriodicTasksRunner(PeriodicQueuedTasks<TimedTask> timers)
	{
		this.timers = timers;
	}

	@Override
	public void run()
	{
		try
		{
			timers.executePreviousPool(System.currentTimeMillis());
		}
		catch (Exception e)
		{
			logger.error("An error occured while executing periodic task:" + e.getMessage());
		}
	}
}
