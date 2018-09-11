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

package com.mobius.software.amqp.performance.runner.tests;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobius.software.amqp.performance.api.json.MultiScenarioData;

public class JsonParseTests
{
	private static final String RESOURCES_FOLDER = "src/main/resources";

	@Test
	public void testParseFiles()
	{
		try
		{
			File resourcesDirectory = new File(RESOURCES_FOLDER);
			File[] files = resourcesDirectory.listFiles();
			for (File json : files)
			{
				System.out.println(json.getAbsolutePath());
				ObjectMapper mapper = new ObjectMapper();
				MultiScenarioData controllersScenarioRequests = null;
				controllersScenarioRequests = mapper.readValue(json, MultiScenarioData.class);
				if (!controllersScenarioRequests.validate())
				{
					System.out.println("an error occured while parsing " + json.getName());
					fail();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
			fail();
		}
	}
}