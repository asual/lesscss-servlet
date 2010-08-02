/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

/**
 * @author Rostislav Hristov
 */
public class LessResource extends StyleResource {
    
    private LessEngine engine;
	
    public LessResource(LessEngine engine, ServletContext servletContext, String uri, String charset, boolean cache, boolean compress) throws ResourceNotFoundException {
        super(servletContext, uri, charset, cache, compress);
        this.engine = engine;
    }
    
    public byte[] getContent() throws LessException, IOException {
        if (content == null || (content != null && !cache && lastModified < getLastModified())) {
        	if (engine != null) {
                content = (resource instanceof URL ? 
                        engine.compile((URL) resource) : engine.compile((File) resource))
                        .replaceAll("\\\\n", "\n").getBytes(charset);
        	} else {
        		content = resource instanceof URL ? ResourceUtils.readTextUrl((URL) resource, charset) : ResourceUtils.readTextFile((File) resource, charset);
        	}
        	lastModified = getLastModified();
        	if (compress) {
        		compress();
        	}
        }
        return content;
    }

    public long getLastModified() throws IOException {
        if (lastModified == null || !cache) {        
            lastModified = super.getLastModified();
            String content = new String(resource instanceof URL ? ResourceUtils.readTextUrl((URL) resource, charset) : ResourceUtils.readTextFile((File) resource, charset));
            String folder = path.substring(0, path.lastIndexOf(System.getProperty("file.separator")) + 1);
            Pattern p = Pattern.compile("@import\\s+(\"[^\"]*\"|'[^']*')");
            Matcher m = p.matcher(content);
            while (m.find()) {
                String path = folder + m.group(1).replaceAll("\"|'", "");
                long importLastModified = (new File(path)).lastModified();
                if (importLastModified > lastModified) {
                    lastModified = importLastModified;
                }
            }
        }
        return lastModified;
    }
    
}