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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cell implements Appendable
{
	private String border = TableBuilder.PIPE;
	private String filler = TableBuilder.SPACE;
	private int indent = TableBuilder.DEFAULT_INDENT;
	private int width;
	private List<Value> values;
	private final Alignment alignment;

	private boolean hasLast = false;

	private Cell()
	{
		this.values = Collections.<Value>emptyList();
		this.alignment = Alignment.CENTER;
	}

	private Cell(List<Value> values, Alignment alignment)
	{
		this.values = values;
		this.alignment = alignment;
	}

	public Cell border(String border)
	{
		this.border = border;
		return this;
	}

	public Cell filler(String filler)
	{
		this.filler = filler;
		return this;
	}

	public static Cell empty()
	{
		return new Cell();
	}

	public static Cell left(Object value)
	{
		List<Value> values = new ArrayList<>();
		values.add(Value.first(value));
		return new Cell(values, Alignment.LEFT);
	}

	public static Cell right(Object value)
	{
		List<Value> values = new ArrayList<>();
		values.add(Value.first(value));
		return new Cell(values, Alignment.RIGHT);
	}

	public static Cell center(Object value)
	{
		List<Value> values = new ArrayList<>();
		values.add(Value.first(value));
		return new Cell(values, Alignment.CENTER);
	}

	public Cell addValue(int distance, Object value)
	{
		this.values.add(Value.next(distance, value));
		return this;
	}

	public Cell addLast(Object value)
	{
		if (hasLast)
			throw new IllegalStateException("can't set multiple last values to one cell");

		this.values.add(Value.last(value));
		this.hasLast = true;
		return this;
	}

	public Cell indent(int indent)
	{
		this.indent = indent;
		return this;
	}

	@Override
	public StringBuilder append(StringBuilder sb)
	{
		int contentlength = 0;
		for (Value value : values)
		{
			value.filler(filler).indent(indent);
			contentlength += value.length();
		}
		int fillLength = width - contentlength - border.length();

		int leftFillLength = 0;
		int rightFillLength = 0;
		switch (alignment)
		{
		case CENTER:
			leftFillLength = fillLength / 2;
			rightFillLength = fillLength / 2;
			int remainer = fillLength % 2;
			while (remainer > 0)
			{
				leftFillLength += 1;
				if (--remainer > 0)
					rightFillLength += 1;
			}
			break;
		case LEFT:
			rightFillLength = fillLength - leftFillLength;
			break;
		case RIGHT:
			leftFillLength = fillLength - leftFillLength;
			break;
		}

		for (int i = 0; i < leftFillLength; i++)
			sb.append(filler);
		for (Value value : values)
			value.append(sb);
		for (int i = 0; i < rightFillLength; i++)
			sb.append(filler);
		sb.append(border);
		return sb;
	}

	public Cell width(int width)
	{
		this.width = width;
		int remaining = width;
		for (Value value : values)
		{
			remaining -= value.length() + border.length();
			if (value.isLast())
				value.distance(remaining);
		}
		return this;
	}
}
