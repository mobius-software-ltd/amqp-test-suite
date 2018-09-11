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
import java.util.Arrays;
import java.util.List;

public class Row implements Appendable
{
	private static final int HEADER_INDENT = 1;

	private String border = TableBuilder.PIPE;
	private final List<Cell> cells;

	private Row()
	{
		this.cells = new ArrayList<>();
		this.cells.add(Cell.empty());
	}

	private Row(int width, List<Cell> cells)
	{
		this.cells = cells;
		int cellWidth = width / cells.size();
		int remainer = width % cells.size();
		for (int i = 0; i < cells.size(); i++, remainer--)
		{
			int realWidth = cellWidth;
			if (remainer > 0)
				realWidth++;
			cells.get(i).width(realWidth);
		}
	}

	public List<Cell> getCells()
	{
		return cells;
	}

	public Row border(String border)
	{
		this.border = border;
		return this;
	}

	public static Row empty()
	{
		return new Row();
	}

	public static Row header(int width, Cell... cells)
	{
		List<Cell> list = Arrays.asList(cells);
		for (Cell cell : list)
			cell.border(TableBuilder.PLUS).filler(TableBuilder.MINUS).indent(HEADER_INDENT);
		return new Row(width, list).border(TableBuilder.PLUS);
	}

	public static Row next(int width, Cell... cells)
	{
		List<Cell> list = Arrays.asList(cells);
		for (Cell cell : list)
			cell.border(TableBuilder.PIPE).filler(TableBuilder.SPACE);
		return new Row(width, list).border(TableBuilder.PIPE);
	}

	public static Row footer(int width, int cellCount)
	{
		List<Cell> cells = new ArrayList<>(cellCount);
		for (int i = 0; i < cellCount; i++)
			cells.add(Cell.empty().border(TableBuilder.PLUS).filler(TableBuilder.MINUS).indent(0));
		return new Row(width, cells).border(TableBuilder.PLUS);
	}

	@Override
	public StringBuilder append(StringBuilder sb)
	{
		if (cells.isEmpty())
			return sb;

		sb.append(border);
		for (Cell cell : cells)
			cell.append(sb);
		sb.append(TableBuilder.NEW_LINE);
		return sb;
	}
}
