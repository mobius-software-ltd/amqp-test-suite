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

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.mobius.software.amqp.performance.api.json.ReportResponse;
import com.mobius.software.amqp.performance.api.json.ResponseData;
import com.mobius.software.amqp.performance.commons.data.ClientReport;
import com.mobius.software.amqp.performance.controller.client.Client;
import com.mobius.software.amqp.performance.controller.task.TimedTask;

public class Orchestrator
{
	private OrchestratorProperties properties;
	private PeriodicQueuedTasks<TimedTask> scheduler;
	private List<Client> clientList;

	private AtomicInteger startingCount = new AtomicInteger(0);
	private ConcurrentLinkedDeque<Client> pendingQueue = new ConcurrentLinkedDeque<>();
	private long startTime;
	private long finishTime;

	private Set<String> pendingClientIDs = new HashSet<>();

	public Orchestrator(OrchestratorProperties properties, PeriodicQueuedTasks<TimedTask> scheduler, List<Client> clientList)
	{
		this.properties = properties;
		this.scheduler = scheduler;
		this.clientList = clientList;
		this.pendingClientIDs.addAll(clientList.stream().map(c -> c.getClientID()).collect(Collectors.toSet()));
	}

	public void start()
	{
		long currTime = System.currentTimeMillis();
		this.startTime = currTime + properties.getScenarioDelay();

		Queue<Client> tempQueue = new LinkedList<>(clientList);
		Map<Client, Long> toStore = new LinkedHashMap<>();
		for (int i = 0; i < properties.getStartThreashold(); i++)
		{
			Client c = tempQueue.poll();
			if (c != null)
				toStore.put(c, currTime + properties.getInitialDelay() + properties.getScenarioDelay());
			else
				break;
		}
		pendingQueue.addAll(tempQueue);
		for (Entry<Client, Long> entry : toStore.entrySet())
		{
			startingCount.incrementAndGet();
			scheduler.store(entry.getValue(), entry.getKey());
		}
	}

	public void notifyOnStarted(Client currClient)
	{
		if (!currClient.hasFinished())
			scheduler.store(currClient.getRealTimestamp(), currClient);

		if (startingCount.decrementAndGet() < properties.getStartThreashold())
		{
			Client newClient = pendingQueue.poll();
			if (newClient != null)
				scheduler.store(newClient.getRealTimestamp(), newClient);
		}
	}

	public void notifyOnFinished(String clientID)
	{
		this.pendingClientIDs.remove(clientID);
		if (this.pendingClientIDs.isEmpty())
			this.finishTime = System.currentTimeMillis();
	}

	public void terminate()
	{
		for (Client client : clientList)
			client.stop();
	}

	public ReportResponse report()
	{
		List<ClientReport> reports = new ArrayList<>();
		for (Client client : clientList)
			reports.add(client.retrieveReport().translate());
		return new ReportResponse(ResponseData.SUCCESS, properties.getScenarioID(), startTime, finishTime, reports);
	}

	public OrchestratorProperties getProperties()
	{
		return properties;
	}

	public PeriodicQueuedTasks<TimedTask> getScheduler()
	{
		return scheduler;
	}
}
