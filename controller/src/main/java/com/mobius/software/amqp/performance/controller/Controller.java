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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobius.software.amqp.performance.api.json.GenericJsonRequest;
import com.mobius.software.amqp.performance.api.json.ReportResponse;
import com.mobius.software.amqp.performance.api.json.ResponseData;
import com.mobius.software.amqp.performance.api.json.UniqueIdentifierRequest;
import com.mobius.software.amqp.performance.commons.data.Command;
import com.mobius.software.amqp.performance.commons.data.Repeat;
import com.mobius.software.amqp.performance.commons.data.Scenario;
import com.mobius.software.amqp.performance.commons.util.CommandParser;
import com.mobius.software.amqp.performance.commons.util.IdentifierParser;
import com.mobius.software.amqp.performance.controller.api.NetworkHandler;
import com.mobius.software.amqp.performance.controller.client.Client;
import com.mobius.software.amqp.performance.controller.net.AmqpClient;
import com.mobius.software.amqp.performance.controller.task.TimedTask;

@Path("controller")
@Singleton
public class Controller
{
	private static final Log logger = LogFactory.getLog(Controller.class);

	private static final long TERMINATION_TIMEOUT = 1000;

	private List<Worker> workers = new ArrayList<>();
	private ExecutorService workersExecutor;
	private ScheduledExecutorService timersExecutor;
	private PeriodicQueuedTasks<TimedTask> scheduler;
	private LinkedBlockingQueue<TimedTask> mainQueue = new LinkedBlockingQueue<>();
	private IdentifierStorage identifierStorage = new IdentifierStorage();
	private ConcurrentHashMap<UUID, Orchestrator> scenarioMap = new ConcurrentHashMap<>();
	private NetworkHandler networkHandler = new AmqpClient();

	public Controller() throws Exception
	{
		start();
	}

	public void start() throws Exception
	{
		initConfig();
		initTaskExecutor();
	}

	private Config initConfig() throws IOException
	{
		Properties properties = new Properties();
		properties.load(new FileInputStream(ControllerRunner.configFile));
		return Config.parse(properties);
	}

	private void initTaskExecutor()
	{
		scheduler = new PeriodicQueuedTasks<TimedTask>(Config.getInstance().getTimersInterval(), mainQueue);
		workersExecutor = Executors.newFixedThreadPool(Config.getInstance().getWorkers());
		for (int i = 0; i < Config.getInstance().getWorkers(); i++)
		{
			Worker worker = new Worker(mainQueue, scheduler);
			workers.add(worker);
			workersExecutor.submit(worker);
		}
		timersExecutor = Executors.newScheduledThreadPool(2);
		timersExecutor.scheduleAtFixedRate(new PeriodicTasksRunner(scheduler), 0, Config.getInstance().getTimersInterval(), TimeUnit.MILLISECONDS);

	}

	@POST
	@Path("scenario")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public GenericJsonRequest scenario(Scenario json)
	{
		if (json == null || !json.validate())
			return new GenericJsonRequest(ResponseData.ERROR, ResponseData.INVALID_PARAMETERS);

		try
		{
			List<Client> clientList = new ArrayList<>();
			OrchestratorProperties properties = OrchestratorProperties.fromScenarioProperties(json.getId(), json.getProperties(), json.getThreshold(), json.getStartThreshold(), json.getContinueOnError(), Config.getInstance().getInitialDelay());
			Orchestrator orchestrator = new Orchestrator(properties, scheduler, clientList);
			networkHandler.init(orchestrator.getProperties().getServerAddress());

			Repeat repeat = json.getProperties().getRepeat();
			for (int i = 0; i < json.getCount(); i++)
			{
				int identityCounter = identifierStorage.countIdentity(properties.getIdentifierRegex(), properties.getStartIdentifier());
				String clientID = IdentifierParser.parseIdentifier(properties.getIdentifierRegex(), json.getProperties().getUsername(), properties.getServerHostname(), identityCounter);
				int repeatCount = 1;
				long repeatInterval = 0L;
				if (repeat != null)
				{
					repeatCount = repeat.getCount();
					repeatInterval = repeat.getInterval();
				}
				ConcurrentLinkedQueue<Command> commands = CommandParser.retrieveCommands(json.getCommands(), repeatCount, repeatInterval);
				Client client = new Client(clientID, json.getProperties().getUsername(), json.getProperties().getPassword(), orchestrator, networkHandler, commands);
				clientList.add(client);
			}
			orchestrator.start();
			scenarioMap.put(json.getId(), orchestrator);
			return new GenericJsonRequest(ResponseData.SUCCESS, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new GenericJsonRequest(ResponseData.ERROR, ResponseData.INTERNAL_SERVER_ERROR + e.getMessage());
		}
	}

	@POST
	@Path("report")
	@Produces(MediaType.APPLICATION_JSON)
	public ReportResponse report(UniqueIdentifierRequest json)
	{
		Orchestrator orchestrator = scenarioMap.get(json.getId());
		if (orchestrator == null)
			return new ReportResponse(ResponseData.NOT_FOUND);
		return orchestrator.report();
	}

	@POST
	@Path("clear")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public GenericJsonRequest clear(UniqueIdentifierRequest json)
	{
		if (!json.validate())
			return new GenericJsonRequest(ResponseData.ERROR, ResponseData.INVALID_PARAMETERS);

		Orchestrator orchestrator = scenarioMap.get(json.getId());
		if (orchestrator == null)
			return new GenericJsonRequest(ResponseData.ERROR, ResponseData.NOT_FOUND);

		orchestrator.terminate();

		return new GenericJsonRequest(ResponseData.SUCCESS, null);
	}

	public void shutdownGracefully()
	{
		shutdown(false);
	}

	public void shutdownNow()
	{
		shutdown(true);
	}

	private void shutdown(boolean mayInterrupt)
	{
		for (Worker worker : workers)
			worker.terminate();

		if (mayInterrupt)
			workersExecutor.shutdownNow();
		else
			workersExecutor.shutdown();

		if (mayInterrupt)
			timersExecutor.shutdownNow();
		else
			timersExecutor.shutdown();

		try
		{
			workersExecutor.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
			timersExecutor.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
		}

		try
		{
			networkHandler.shutdown();
		}
		catch (Exception e)
		{
		}
	}
}
