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
import java.util.concurrent.ConcurrentSkipListSet;

import com.mobius.software.amqp.performance.commons.util.IdentifierParser;

public class IdentifierStorage
{
	private static final Integer START_VALUE = 0;
	private ConcurrentHashMap<String, ConcurrentSkipListSet<Integer>> usedRegexIdentifiers = new ConcurrentHashMap<>();

	public int countIdentity(String regex, Integer startIdentifier)
	{
		ConcurrentSkipListSet<Integer> usedIdentifiers = usedRegexIdentifiers.get(regex);
		if (usedIdentifiers == null)
		{
			usedIdentifiers = new ConcurrentSkipListSet<Integer>();
			usedIdentifiers.add(START_VALUE);
			usedRegexIdentifiers.putIfAbsent(regex, usedIdentifiers);
		}

		Integer identifier = startIdentifier;
		if (usedIdentifiers.add(identifier))
			return identifier;

		identifier = usedIdentifiers.last();
		do
		{
			if (identifier == Integer.MAX_VALUE)
				identifier = START_VALUE;
			identifier++;
		}
		while (!usedIdentifiers.add(identifier));
		return identifier;
	}

	public void releaseIdentifier(String regex, String clientID)
	{
		ConcurrentSkipListSet<Integer> usedIdentifiers = usedRegexIdentifiers.get(regex);
		if (usedIdentifiers != null)
		{
			Integer identifierCounter = IdentifierParser.parseIdentifierCounter(clientID);
			if (identifierCounter != null)
				usedIdentifiers.remove(identifierCounter);
		}
	}
}
