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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.mobius.software.amqp.performance.controller.task.PingTimer;
import com.mobius.software.amqp.performance.controller.task.TimedTask;

public class PeriodicQueuedTasks<T extends TimedTask>
{
	private ConcurrentHashMap<Long, ConcurrentLinkedQueue<T>> queues = new ConcurrentHashMap<Long, ConcurrentLinkedQueue<T>>();
	private LinkedBlockingQueue<T> mainQueue;
	private ConcurrentLinkedQueue<T> passAwayQueue = new ConcurrentLinkedQueue<T>();

	private long period;
	private AtomicLong previousRun = new AtomicLong(0);

	public PeriodicQueuedTasks(long period, LinkedBlockingQueue<T> mainQueue)
	{
		this.mainQueue = mainQueue;
		this.period = period;
	}

	public long getPeriod()
	{
		return period;
	}

	public long getPreviousRun()
	{
		return previousRun.get();
	}

	public void store(long timestamp, T task)
	{
		Long periodTime = timestamp - timestamp % period;
		ConcurrentLinkedQueue<T> queue;
		if (previousRun.get() >= periodTime)
		{
			queue = passAwayQueue;
			queue.offer(task);
		}
		else
		{
			queue = queues.get(periodTime);
			if (queue == null)
			{
				queue = new ConcurrentLinkedQueue<T>();
				ConcurrentLinkedQueue<T> oldQueue = queues.putIfAbsent(periodTime, queue);
				if (oldQueue != null)
					queue = oldQueue;
			}

			if (previousRun.get() >= periodTime)
			{
				passAwayQueue.offer(task);
				queues.remove(periodTime);
			}
			else
				queue.offer(task);
		}
	}

	public void executePreviousPool(long timestamp)
	{
		Long originalTime = (timestamp - timestamp % period - period);
		Long periodTime = originalTime;

		ConcurrentLinkedQueue<T> queue = null;
		T current;

		do
		{
			if (!previousRun.compareAndSet(0, periodTime))
				periodTime = previousRun.addAndGet(period);

			queue = queues.remove(periodTime);
			if (queue != null)
			{
				while ((current = queue.poll()) != null)
				{
					if (current.getRealTimestamp() < (periodTime + period))
					{
						if (current instanceof PingTimer)
							current.execute();
						else
							mainQueue.offer(current);
					}
				}
			}
		}
		while (periodTime.longValue() < originalTime.longValue());

		while ((current = passAwayQueue.poll()) != null)
		{
			if (current.getRealTimestamp() < (periodTime + period))
			{
				if (current instanceof PingTimer)
					current.execute();
				else
					mainQueue.offer(current);
			}
		}
	}

	public ConcurrentHashMap<Long, ConcurrentLinkedQueue<T>> getQueues()
	{
		return queues;
	}

	public LinkedBlockingQueue<T> getMainQueue()
	{
		return mainQueue;
	}
}