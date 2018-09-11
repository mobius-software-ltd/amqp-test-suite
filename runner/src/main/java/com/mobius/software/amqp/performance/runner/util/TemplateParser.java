package com.mobius.software.amqp.performance.runner.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

public class TemplateParser
{
	private Map<String, String> templates = new HashMap<>();

	public void addTemplate(String name, String value)
	{
		templates.put(name, value);
	}

	public String fileToStringProcessTemplates(File jsonFile) throws IOException
	{
		String textual = FileUtils.readFileToString(jsonFile);
		for (Entry<String, String> entry : templates.entrySet())
		{
			while (textual.contains(entry.getKey()))
				textual = textual.replace(entry.getKey(), entry.getValue());
		}
		return textual;
	}
}
