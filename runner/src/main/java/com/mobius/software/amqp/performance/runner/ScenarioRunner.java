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

package com.mobius.software.amqp.performance.runner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mobius.software.amqp.performance.commons.data.ScenarioRequest;
import com.mobius.software.amqp.performance.runner.util.FileUtil;
import com.mobius.software.amqp.performance.runner.util.RequestFormatter;
import com.mobius.software.amqp.performance.runner.util.TemplateParser;

public class ScenarioRunner
{
	private static final Log logger = LogFactory.getLog(ScenarioRunner.class);

	private List<ScenarioRequest> requests;

	public ScenarioRunner(List<ScenarioRequest> requests)
	{
		this.requests = requests;
	}

	public static void main(String[] args)
	{
		try
		{
			File json = FileUtil.readFile(args[0]);
			List<ScenarioRequest> requests = parseRequests(json);
			ScenarioRunner runner = new ScenarioRunner(requests);
			runner.start();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			System.exit(0);
		}
	}

	public void start() throws InterruptedException
	{
		List<RequestWorker> workers = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(requests.size());
		for (ScenarioRequest request : requests)
			workers.add(new RequestWorker(request, latch));

		ExecutorService service = Executors.newFixedThreadPool(workers.size());
		for (RequestWorker worker : workers)
			service.submit(worker);
		latch.await();

		service.shutdownNow();
	}

	public static List<ScenarioRequest> parseRequests(File json) throws IllegalArgumentException, URISyntaxException, JsonParseException, JsonMappingException, IOException
	{
		TemplateParser tempateParser = new TemplateParser();
		String textual = tempateParser.fileToStringProcessTemplates(json);
		return RequestFormatter.parseScenarioRequests(textual);
	}
	
	public static List<ScenarioRequest> parseRequests(File json, TemplateParser tempateParser) throws IllegalArgumentException, URISyntaxException, JsonParseException, JsonMappingException, IOException
	{
		String textual = tempateParser.fileToStringProcessTemplates(json);
		return RequestFormatter.parseScenarioRequests(textual);
	}
}
