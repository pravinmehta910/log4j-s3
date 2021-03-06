package com.log4js3.logging.log4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import com.log4js3.logging.LoggingEventCache.ICachePublisher;
import com.log4js3.logging.PublishContext;

/**
 * Implementation to standardize on a cache name and aggregate and coordinate
 * multiple IPublishHelpers to publish content to different destinations.
 *
 * @author Van Ly (vancly@hotmail.com)
 *
 */
public class CachePublisher implements ICachePublisher {
	private final Layout layout;
	private final String hostName;
	private final String[] tags;
	
	private List<IPublishHelper> helpers = 
		new LinkedList<IPublishHelper>();
	
	public CachePublisher(Layout layout, String hostName, String[] tags) {
		this.layout = layout;	
		this.hostName = hostName;
		this.tags = tags;
	}
	
	public PublishContext startPublish(String cacheName) {
		String namespacedCacheName = composeNamespacedCacheName(cacheName);
		System.out.println(String.format("BEGIN publishing to %s...",
			namespacedCacheName));
		PublishContext context = new PublishContext(namespacedCacheName,
			hostName, tags, layout);
		for (IPublishHelper helper: helpers) {
			helper.start(context);
		}
		return context;
	}
	
	String composeNamespacedCacheName(String rawCacheName) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		return String.format("%s_%s_%s", df.format(new Date()),
			hostName, rawCacheName);
	}

	public void publish(PublishContext context, int sequence, LoggingEvent event) {
		for (IPublishHelper helper: helpers) {
			helper.publish(context, sequence, event);
		}
		System.out.println(String.format("%d:%s", sequence,
			layout.format(event)));
	}

	public void endPublish(PublishContext context) {
		for (IPublishHelper helper: helpers) {
			helper.end(context);
		}
		System.out.println(String.format("END publishing to %s",
			context.getCacheName()));
	}

	/**
	 * Add an IPublishHelper implementation to the list of helpers to invoke
	 * when publishing is performed.
	 * 
	 * @param helper helper to add to the list
	 */
	public void addHelper(IPublishHelper helper) {
		helpers.add(helper);
	}
}
