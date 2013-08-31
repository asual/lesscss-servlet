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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;

/**
 * @author Rostislav Hristov
 */
public class LessResource extends StyleResource {

	private final Log logger = LogFactory.getLog(getClass());
	private LessEngine engine;

	public LessResource(LessEngine engine, ServletContext servletContext,
			String uri, String charset, boolean cache, boolean compress)
			throws ResourceNotFoundException {
		super(servletContext, uri, charset, cache, compress);
		this.engine = engine;
	}

	public byte[] getContent() throws LessException, IOException {
		if (content == null || !cache) {
			logger.debug("Not using cache.");
			if (engine != null) {
				logger.debug("LessEngine available, compiling.");
				content = (resource instanceof URL ? engine
						.compile((URL) resource) : engine
						.compile((File) resource)).replaceAll("\\\\n", "\n")
						.getBytes(charset);
			} else {
				logger.debug("LessEngine not available, treating as regular resource.");
				content = resource instanceof URL ? ResourceUtils.readTextUrl(
						(URL) resource, charset) : ResourceUtils.readTextFile(
						(File) resource, charset);
			}
			lastModified = super.getLastModified();
			if (compress) {
				logger.debug("Compressing resource.");
				compress();
			}
		} else {
			logger.debug("Using cache, since lastModified: " + lastModified
					+ " and getLastModified: " + getLastModified());
		}
		return content;
	}

}