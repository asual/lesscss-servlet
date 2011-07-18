/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.lesscss;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rostislav Hristov
 */
public class Resource {

	protected ServletContext servletContext;
	protected String path;
	protected Object resource;
	protected String charset;
	protected boolean cache;
	protected byte[] content;
	protected Long lastModified;

	protected final Log logger = LogFactory.getLog(getClass());

	public Resource(ServletContext servletContext, String uri, String charset, boolean cache) throws ResourceNotFoundException {

		this.servletContext = servletContext;
		this.charset = charset;
		this.cache = cache;

		URL url = getUrl(uri);
		File file = getFile(uri);
		
		if (url != null || (file != null && file.exists())) {
			path = url != null ? url.getPath() : file.getAbsolutePath();
			resource = url != null ? url : file;
		} else {
			logger.error("Error processing " + uri + ".");
			throw new ResourceNotFoundException("Error processing " + uri + ".");			
		}
	}
	
	public byte[] getContent() throws Exception {
		if (content == null || (!cache && lastModified < getLastModified())) {
			content = resource instanceof URL ? ResourceUtils.readBinaryUrl((URL) resource) : ResourceUtils.readBinaryFile((File) resource);
			lastModified = getLastModified();
		}
		return content;
	}

	public long getLastModified() throws IOException {
		if (lastModified == null || !cache) {
			if (resource instanceof URL) {
				lastModified = ((URL) resource).openConnection().getLastModified();
				logger.debug("getLastModified(), URL resource: " + lastModified + " - for resource: " + resource);
			}
			else {
				lastModified = ((File) resource).lastModified();
				logger.debug("getLastModified(), File resource: " + lastModified + " - for resource: " + resource);
			}
		}
		logger.debug("getLastModified(): " + lastModified);
		return lastModified;
	}
	
	protected URL getUrl(String path) {
		try {
			URL url = servletContext.getResource("/META-INF" + path);
			if (url != null) {
				return url;
			}
			url = servletContext.getResource("/META-INF/resources" + path);
			if (url != null) {
				return url;
			}
			url = getClass().getClassLoader().getResource("META-INF" + path);
			if (url != null) {
				return url;
			}
			url = getClass().getClassLoader().getResource("META-INF/resources" + path);
			if (url != null) {
				return url;
			}
		} catch (MalformedURLException e) {
		}
		return null;
	}
	
	protected File getFile(String path) {
		try {
			return new File(servletContext.getRealPath(path));
		} catch (Exception e) {
		}
		return null;
	}
}