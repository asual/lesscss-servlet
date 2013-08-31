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

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rostislav Hristov
 */
public class LessServlet extends ResourceServlet {

	private static final long serialVersionUID = 413708886190444579L;
	private final Log logger = LogFactory.getLog(getClass());

	protected LessEngine engine;
	protected boolean css;
	protected String lineNumbers;

	public void init() {
		if (getServletConfig() != null) {
			if (getInitParameter("charset") != null) {
				charset = getInitParameter("charset");
			}
			if (getInitParameter("cache") != null) {
				cache = Boolean.valueOf(getInitParameter("cache"));
			}
			if (getInitParameter("compress") != null) {
				compress = Boolean.valueOf(getInitParameter("compress"));
			}
			if (getInitParameter("css") != null) {
				css = Boolean.valueOf(getInitParameter("css"));
			}
			if (getInitParameter("lineNumbers") != null) {
				lineNumbers = getInitParameter("lineNumbers");
			}
		}
		try {
			initialContext = new javax.naming.InitialContext();
		} catch (NamingException e) {
		} catch (NoClassDefFoundError e) {
		}
		if (initialContext != null) {
			if (getJndiParameter("/less/Charset") != null) {
				charset = (String) getJndiParameter("/less/Charset");
			}
			if (getJndiParameter("/less/Cache") != null) {
				cache = (Boolean) getJndiParameter("/less/Cache");
			}
			if (getJndiParameter("/less/Compress") != null) {
				compress = (Boolean) getJndiParameter("/less/Compress");
			}
			if (getJndiParameter("/less/Css") != null) {
				css = (Boolean) getJndiParameter("/less/Css");
			}
			if (getJndiParameter("/less/LineNumbers") != null) {
				lineNumbers = (String) getJndiParameter("/less/LineNumbers");
			}
		}
		LessOptions options = new LessOptions();
		options.setCharset(charset);
		options.setCss(css);
		options.setLineNumbers(lineNumbers);
		options.setOptimization(cache ? 3 : 0);
		engine = new LessEngine(options);
	}

	protected Resource getResource(String uri) throws ResourceNotFoundException {
		String mimeType = getResourceMimeType(uri);
		if (!resources.containsKey(uri)) {
			logger.debug("Using new LessResource for uri " + uri);
			if ("text/css".equals(mimeType)) {
				resources.put(uri, new LessResource(engine,
						getServletContext(), uri, charset, cache, compress));
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