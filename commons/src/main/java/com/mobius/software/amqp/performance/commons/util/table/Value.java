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

package com.mobius.software.amqp.performance.commons.util.table;

public class Value implements Appendable
{
	private int indent = TableBuilder.DEFAULT_INDENT;
	private int distance;
	private Object value;
	private String filler = TableBuilder.SPACE;

	private Value(int distance, Object value)
	{
		this.distance = distance;
		this.value = value;
	}

	public Value distance(int distance)
	{
		this.distance = distance;
		return this;
	}

	public Value filler(String filler)
	{
		this.filler = filler;
		return this;
	}

	public Value indent(int indent)
	{
		this.indent = indent;
		return this;
	}

	public int length()
	{
		return distance * filler.length() + value.toString().length() + indent * TableBuilder.SPACE.length() * 2;
	}

	public static Value first(Object value)
	{
		return new Value(0, value);
	}

	public static Value next(int distance, Object value)
	{
		return new Value(distance, value);
	}

	public static Value last(Object value)
	{
		return new Value(-1, value);
	}

	public boolean isLast()
	{
		return distance == -1;
	}

	@Override
	public StringBuilder append(StringBuilder sb)
	{
		for (int i = 0; i < distance; i++)
			sb.append(filler);
		for (int i = 0; i < indent; i++)
			sb.append(TableBuilder.SPACE);
		sb.append(value);
		for (int i = 0; i < indent; i++)
			sb.append(TableBuilder.SPACE);
		return sb;
	}
}
