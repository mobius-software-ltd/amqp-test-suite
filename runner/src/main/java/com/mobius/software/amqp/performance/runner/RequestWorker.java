package com.mobius.software.amqp.performance.runner;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobius.software.amqp.performance.api.json.GenericJsonRequest;
import com.mobius.software.amqp.performance.api.json.ReportResponse;
import com.mobius.software.amqp.performance.commons.data.ScenarioRequest;
import com.mobius.software.amqp.performance.runner.util.FileUtil;
import com.mobius.software.amqp.performance.runner.util.ReportBuilder;
import com.mobius.software.amqp.performance.runner.util.Requester;

public class RequestWorker implements Runnable
{
	private static final Log logger = LogFactory.getLog(RequestWorker.class);

	private ScenarioRequest request;
	private CountDownLatch latch;

	public RequestWorker(ScenarioRequest request, CountDownLatch latch)
	{
		this.request = request;
		this.latch = latch;
	}

	@Override
	public void run()
	{
		try
		{
			GenericJsonRequest response = Requester.sendScenario(request.retrieveBaseURL(), request.getScenario());
			if (response.successful())
			{
				logger.info("estimated scenario finishtime: " + new Date(System.currentTimeMillis() + request.getRequestTimeout()));
				Thread.sleep(request.getRequestTimeout());

				ReportResponse report = Requester.requestReport(request.retrieveBaseURL(), request.getScenario().getId());
				if (report.successful())
				{
					ScenarioData data = ScenarioData.translate(request.getScenario(), report);
					System.out.println(ReportBuilder.buildSummary(data));
					FileUtil.logErrors(data.getScenarioID(), report.getReports());
					response = Requester.requestClear(request.retrieveBaseURL(), request.getScenario().getId());
					if (!response.successful())
						logger.error("Controller returned an error for clear scenario request:" + response.getMessage());
				}
				else
					logger.error("Controller returned an error for report request:" + response.getMessage());
			}
			else
				logger.error("Controller returned an error for scenario request:" + response.getMessage());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}
		finally
		{
			latch.countDown();
		}
	}
}
