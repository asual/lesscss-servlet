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

/**
 * @author Rostislav Hristov
 */
public class LessServlet extends ResourceServlet {

	private static final long serialVersionUID = 413708886190444579L;
	private LessEngine engine;
	private final Log logger = LogFactory.getLog(getClass());
	
	public void init() {
		super.init();
		engine = new LessEngine();
	}

	protected Resource getResource(String uri) throws ResourceNotFoundException {
		String mimeType = getResourceMimeType(uri);
		if (!resources.containsKey(uri)) {
			logger.debug("Using new LessResource for uri " + uri);
			if ("text/css".equals(mimeType)) {
				resources.put(uri, new LessResource(engine, getServletContext(), uri, charset, cache, compress));
				return resources.get(uri);
			} else {
				return super.getResource(uri);
			}
		} else {
			logger.debug("Using existing LessResource for uri " + uri);
			return resources.get(uri);
		}
	}
	
}