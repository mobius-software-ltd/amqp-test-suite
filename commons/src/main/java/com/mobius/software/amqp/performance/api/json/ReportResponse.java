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

package com.mobius.software.amqp.performance.api.json;

import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

import com.mobius.software.amqp.performance.commons.data.ClientReport;

@SuppressWarnings("serial")
@XmlRootElement
public class ReportResponse extends GenericJsonRequest
{
	private UUID scenarioID;
	private long startTime;
	private long finishTime;
	private List<ClientReport> reports;

	public ReportResponse()
	{

	}

	public ReportResponse(String message)
	{
		super(ResponseData.ERROR, message);
	}

	public ReportResponse(String status, UUID scenarioID, long startTime, long finishTime, List<ClientReport> reports)
	{
		super(status, null);
		this.scenarioID = scenarioID;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.reports = reports;
	}

	public Boolean retrieveResult()
	{
		if (reports != null)
		{
			for (ClientReport report : reports)
			{
				if (report.getErrors() != null && !report.getErrors().isEmpty())
					return false;
			}
		}
		return true;
	}

	public UUID getScenarioID()
	{
		return scenarioID;
	}

	public void setScenarioID(UUID scenarioID)
	{
		this.scenarioID = scenarioID;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public long getFinishTime()
	{
		return finishTime;
	}

	public void setFinishTime(long finishTime)
	{
		this.finishTime = finishTime;
	}

	public List<ClientReport> getReports()
	{
		return reports;
	}

	public void setReports(List<ClientReport> reports)
	{
		this.reports = reports;
	}

	public boolean successful()
	{
		return getStatus().equals(ResponseData.SUCCESS);
	}
}
