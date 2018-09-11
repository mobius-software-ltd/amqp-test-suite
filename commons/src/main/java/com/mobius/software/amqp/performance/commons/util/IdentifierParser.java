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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mobius.software.amqp.performance.commons.data.Template;

public class IdentifierParser
{
	private static final String REGEX_SEPARATOR = "%";
	private static final String IDENTIFIER_SEPARATOR = "_";

	public static String parseIdentifier(String regex, String username, String server, Integer identifier)
	{
		StringBuilder sb = new StringBuilder();
		List<String> segments = Arrays.asList(regex.split(REGEX_SEPARATOR));
		for (String segment : segments)
		{
			segment.replaceAll(REGEX_SEPARATOR, "");
			if (segment.equals(Template.IDENTITY.getTemplate()))
				sb.append(identifier).append(IDENTIFIER_SEPARATOR);
			else if (segment.equals(Template.ACCOUNT.getTemplate()))
				sb.append(username).append(IDENTIFIER_SEPARATOR);
			else if (segment.equals(Template.SERVER.getTemplate()))
				sb.append(server).append(IDENTIFIER_SEPARATOR);
			else if (!segment.isEmpty())
				throw new IllegalArgumentException("invalid regex expression");
		}
		String result = sb.toString();
		return result.substring(0, result.length() - 1);
	}

	public static Integer parseIdentifierCounter(String clientID)
	{
		Integer counter = null;
		String[] regexSegmants = clientID.split(IDENTIFIER_SEPARATOR);
		for (String segment : regexSegmants)
		{
			if (StringUtils.isNumeric(segment))
				counter = Integer.parseInt(segment);
		}
		return counter;
	}
}
