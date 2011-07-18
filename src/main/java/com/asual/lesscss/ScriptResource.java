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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;

import javax.servlet.ServletContext;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * @author Rostislav Hristov
 */
public class ScriptResource extends Resource {
	
	protected boolean compress;

	public ScriptResource(ServletContext servletContext, String uri, String charset, boolean cache, boolean compress) throws ResourceNotFoundException {
		super(servletContext, uri, charset, cache);
		this.compress = compress;
	}

	public byte[] getContent() throws IOException {
		if (content == null || (content != null && !cache && lastModified < getLastModified())) {
			content = resource instanceof URL ? ResourceUtils.readTextUrl((URL) resource, charset) : ResourceUtils.readTextFile((File) resource, charset);
			lastModified = getLastModified();
			if (compress) {
				compress();
			}
		}
		return content;
	}
	
	private void compress() throws UnsupportedEncodingException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Reader in = new InputStreamReader(
				new ByteArrayInputStream(
						(new String(content, charset))
							.replaceFirst("^/\\*", "/*!")
							.getBytes(charset)), 
							charset);
		Writer out = new OutputStreamWriter(baos, charset);
		JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {
			public void warning(String message, String sourceName,
					int line, String lineSource, int lineOffset) {
				logger.error("Message: " + message + (lineSource != null ? ", Line: " + line + ", Column: " + lineOffset + ", Source: " + lineSource : "") + ".");
			}
			public void error(String message, String sourceName,
					int line, String lineSource, int lineOffset) {
				logger.error("Message: " + message + (lineSource != null ? ", Line: " + line + ", Column: " + lineOffset + ", Source: " + lineSource : "") + ".");
			}
			public EvaluatorException runtimeError(String message, String sourceName,
					int line, String lineSource, int lineOffset) {
				error(message, sourceName, line, lineSource, lineOffset);
				return new EvaluatorException(message);
			}
		});
		in.close();
		compressor.compress(out, -1, true, false, true, false);
		out.flush();
		content = baos.toByteArray();
		out.close();
	}
	
}