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
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.ServletContext;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

/**
 * @author Rostislav Hristov
 */
public class StyleResource extends Resource {

	protected boolean compress;

	public StyleResource(ServletContext servletContext, String uri,
			String charset, boolean cache, boolean compress)
			throws ResourceNotFoundException {
		super(servletContext, uri, charset, cache);
		this.compress = compress;
	}

	public byte[] getContent(String path) throws IOException {
		if (content == null || (!cache && lastModified < getLastModified())) {
			content = resource instanceof URL ? ResourceUtils.readTextUrl(
					(URL) resource, charset) : ResourceUtils.readTextFile(
					(File) resource, charset);
			lastModified = getLastModified();
			if (compress) {
				compress();
			}
		}
		return content;
	}

	protected void compress() throws IOException {
		URL cssmin = getClass().getClassLoader().getResource(
				"META-INF/cssmin.js");
		Context cx = Context.enter();
		cx.setOptimizationLevel(9);
		Global global = new Global();
		global.init(cx);
		Scriptable scope = cx.initStandardObjects(global);
		cx.evaluateString(scope, "var exports = {};", "exports", 1, null);
		cx.evaluateReader(scope, new InputStreamReader(cssmin.openConnection()
				.getInputStream()), cssmin.getFile(), 1, null);
		Scriptable exports = (Scriptable) scope.get("exports", scope);
		Scriptable compressor = (Scriptable) exports.get("compressor", exports);
		Function fn = (Function) compressor.get("cssmin", compressor);
		content = ((String) Context.call(null, fn, compressor, compressor,
				new Object[] { new String(content, charset).replaceFirst(
						"^/\\*", "/*!") })).getBytes(charset);
		Context.exit();
	}

}