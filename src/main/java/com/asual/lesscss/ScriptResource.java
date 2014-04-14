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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;

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
		InputStream is = new ByteArrayInputStream(content);
		Writer out = new OutputStreamWriter(baos, charset);
		CompilerOptions options = new CompilerOptions();
		CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
		Compiler.setLoggingLevel(Level.OFF);
		Compiler compiler = new Compiler();
		compiler.disableThreads();
		Result result = compiler.compile(new JSSourceFile[] {}, 
			new JSSourceFile[] { JSSourceFile.fromInputStream("is", is) }, options);
		if (result.success) {
			Pattern pattern = Pattern.compile("^/\\*.*?\\*/\\s?", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(new String(content, charset));
			while (matcher.find()) {
				out.write(matcher.group());
			}
			out.write(compiler.toSource());
			out.flush();
			content = baos.toByteArray();
		}
		is.close();
		out.close();
	}
}