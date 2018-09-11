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
import java.util.List;

public class TableBuilder
{
	static final int DEFAULT_WIDTH = 120;
	static final int DEFAULT_INDENT = 1;
	static final String MINUS = "-";
	static final String PLUS = "+";
	static final String SPACE = " ";
	static final String PIPE = "|";
	static final String NEW_LINE = "\r\n";
	static final String EMPTY_STRING = "";

	private int width = DEFAULT_WIDTH;
	private List<Row> rows = new ArrayList<>();

	public TableBuilder width(int width)
	{
		this.width = width;
		return this;
	}

	public TableBuilder addHeader(Cell... cells)
	{
		this.rows.add(Row.header(width, cells));
		return this;
	}

	public TableBuilder addRow(Cell... cells)
	{
		Row row = Row.next(width, cells);
		this.rows.add(row);
		return this;
	}

	public TableBuilder addFooter(int cellCount)
	{
		this.rows.add(Row.footer(width, cellCount));
		return this;
	}

	public String build()
	{
		StringBuilder sb = new StringBuilder();
		for (Row row : rows)
			row.append(sb);
		return sb.toString();
	}
}