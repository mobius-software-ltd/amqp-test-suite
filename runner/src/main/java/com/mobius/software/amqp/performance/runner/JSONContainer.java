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

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.mobius.software.amqp.performance.api.json.GenericJsonRequest;
import com.mobius.software.amqp.performance.api.json.ReportResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class JSONContainer
{
	private ClientConfig config;
	private Client client;
	private WebResource target;
	private ObjectMapper mapper;

	public JSONContainer(String url)
	{
		config = new DefaultClientConfig();
		config.getClasses().add(JacksonJsonProvider.class);
		client = Client.create(config);
		target = client.resource(url);
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public void updateURL(String url)
	{
		config = new DefaultClientConfig();
		config.getClasses().add(JacksonJsonProvider.class);
		client = Client.create(config);
		target = client.resource(url);
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public GenericJsonRequest post(Object request) throws Exception
	{
		return getResource()//
				.getRequestBuilder()//
				.type(MediaType.APPLICATION_JSON)//
				.accept(MediaType.APPLICATION_JSON)//
				.post(GenericJsonRequest.class, request);
	}

	public ReportResponse requestReport(Object request) throws Exception
	{
		Builder builder = getResource().getRequestBuilder();
		builder.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		return builder.post(ReportResponse.class, request);
	}

	public GenericJsonRequest get() throws Exception
	{
		Builder builder = getResource().getRequestBuilder().type(MediaType.APPLICATION_JSON);
		builder = builder.accept(MediaType.APPLICATION_JSON);
		getMapper();
		GenericJsonRequest response = getMapper().readValue(builder.get(String.class), new TypeReference<GenericJsonRequest>()
		{
		});
		return response;
	}

	public WebResource getResource()
	{
		return target;
	}

	public ObjectMapper getMapper()
	{
		return mapper;
	}

	public void release()
	{
		client.destroy();
	}
}