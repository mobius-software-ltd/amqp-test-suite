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

import java.util.UUID;

import com.mobius.software.amqp.performance.api.json.GenericJsonRequest;
import com.mobius.software.amqp.performance.api.json.ReportResponse;
import com.mobius.software.amqp.performance.api.json.UniqueIdentifierRequest;
import com.mobius.software.amqp.performance.commons.data.PathSegment;
import com.mobius.software.amqp.performance.commons.data.Scenario;
import com.mobius.software.amqp.performance.commons.util.URLBuilder;
import com.mobius.software.amqp.performance.runner.JSONContainer;

public class Requester
{
	public static GenericJsonRequest sendScenario(String baseURL, Scenario request) throws Exception
	{
		String requestURL = URLBuilder.build(baseURL, PathSegment.CONTROLLER, PathSegment.SCENARIO);
		JSONContainer container = new JSONContainer(requestURL);
		GenericJsonRequest response = null;
		try
		{
			response = container.post(request);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			container.release();
		}
		return response;
	}

	public static ReportResponse requestReport(String baseURL, UUID id) throws Exception
	{
		String requestURL = URLBuilder.build(baseURL, PathSegment.CONTROLLER, PathSegment.REPORT);
		UniqueIdentifierRequest request = new UniqueIdentifierRequest(id);
		JSONContainer container = new JSONContainer(requestURL);
		ReportResponse report = null;
		try
		{
			report = container.requestReport(request);
		}
		finally
		{
			container.release();
		}
		return report;
	}

	public static GenericJsonRequest requestClear(String baseURL, UUID id) throws Exception
	{
		String requestURL = URLBuilder.build(baseURL, PathSegment.CONTROLLER, PathSegment.CLEAR);
		UniqueIdentifierRequest request = new UniqueIdentifierRequest(id);
		JSONContainer container = new JSONContainer(requestURL);
		GenericJsonRequest response = null;
		try
		{
			response = container.post(request);
		}
		finally
		{
			container.release();
		}
		return response;
	}
}
