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

package com.mobius.software.amqp.performance.commons.util;

import java.net.URI;

import com.mobius.software.amqp.performance.commons.data.PathSegment;

public class URLBuilder
{
	private static final String SEPARATOR = "/";
	private static final String PROTOCOL_HTTP = "http://";
	private static final String ADDRESS_SEPRATOR = ":";

	public static String build(URI baseURL, PathSegment... segments)
	{
		if (segments == null || segments.length == 0)
			throw new IllegalArgumentException("please specify valid url");
		StringBuilder sb = new StringBuilder();
		sb.append(baseURL);
		for (int i = 0; i < segments.length; i++)
		{
			sb.append(segments[i].getPath());
			if (i < segments.length - 1)
				sb.append(SEPARATOR);
		}
		return sb.toString();
	}

	public static String build(String hostname, Integer port, PathSegment... segments)
	{
		if (segments == null || segments.length == 0)
			throw new IllegalArgumentException("please specify valid url");

		StringBuilder sb = new StringBuilder();
		String baseURI = buildBaseURL(hostname, port);
		sb.append(baseURI);
		for (int i = 0; i < segments.length; i++)
		{
			sb.append(segments[i].getPath());
			if (i < segments.length - 1)
				sb.append(SEPARATOR);
		}
		return sb.toString();
	}

	public static String build(String baseURL, PathSegment... segments)
	{
		if (baseURL == null)
			throw new IllegalArgumentException("please specify valid controller baseURI");

		if (segments == null || segments.length == 0)
			throw new IllegalArgumentException("please specify valid url");

		StringBuilder sb = new StringBuilder();
		sb.append(baseURL);
		for (int i = 0; i < segments.length; i++)
		{
			sb.append(segments[i].getPath());
			if (i < segments.length - 1)
				sb.append(SEPARATOR);
		}
		return sb.toString();
	}

	public static String retriveBaseURL(String requestURL)
	{
		if (!requestURL.contains(PathSegment.CONTROLLER.getPath()))
			throw new IllegalArgumentException("invalid request URL:" + requestURL + ". Expected to contain root segment " + PathSegment.CONTROLLER.getPath());
		return requestURL.substring(0, requestURL.indexOf(PathSegment.CONTROLLER.getPath()));
	}

	public static String buildBaseURL(String hostname, Integer port)
	{
		if (hostname == null || port == null || port < 0 || port > 65535)
			throw new IllegalArgumentException("please specify valid controller hostname and port");

		return PROTOCOL_HTTP + hostname + ADDRESS_SEPRATOR + port + SEPARATOR;
	}
}
