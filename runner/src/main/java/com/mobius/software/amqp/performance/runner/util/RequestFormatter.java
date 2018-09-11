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

package com.mobius.software.amqp.performance.runner.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobius.software.amqp.performance.api.json.MultiScenarioData;
import com.mobius.software.amqp.performance.commons.data.ClientController;
import com.mobius.software.amqp.performance.commons.data.Scenario;
import com.mobius.software.amqp.performance.commons.data.ScenarioRequest;
import com.mobius.software.amqp.performance.commons.util.URLBuilder;

public class RequestFormatter
{
	public static List<ScenarioRequest> parseScenarioRequests(File file) throws JsonParseException, JsonMappingException, IOException
	{
		return parseScenarioRequests(FileUtils.readFileToString(file, "UTF-8"));
	}

	public static List<ScenarioRequest> parseScenarioRequests(String json) throws JsonParseException, JsonMappingException, IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		MultiScenarioData multiScenarioData = mapper.readValue(json, MultiScenarioData.class);
		if (!multiScenarioData.validate())
			throw new IllegalArgumentException("JSON file: one of the required fields is missing or invalid");

		List<ScenarioRequest> requests = new ArrayList<>();
		List<ClientController> controllers = multiScenarioData.getControllers();
		for (ClientController controller : controllers)
		{
			String baseURL = URLBuilder.buildBaseURL(controller.getHostname(), controller.getPort());
			for (ScenarioRequest request : controller.getRequests())
			{
				request.updateBaseURL(baseURL);
				Scenario scenario = request.getScenario();
				if (scenario.getId() == null)
					scenario.setId(UUID.randomUUID());
				scenario.getProperties().setIdentifierRegex(controller.getIdentifierRegex());
				scenario.getProperties().setStartIdentifier(controller.getStartIdentifier());
				requests.add(request);
			}
		}
		return requests;
	}
}
