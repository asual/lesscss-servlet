/*
 * Copyright 2009 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rostislav Hristov
 */
public class Resource {

    protected String path;
    protected Object resource;
    protected String charset;
    protected boolean compress;
    protected byte[] content;
    protected long lastModified;

    protected final Log logger = LogFactory.getLog(getClass());

    public Resource(String path, Object resource, String charset, boolean compress) {
        this.path = path;
        this.resource = resource;
        this.charset = charset;
        this.compress = compress;
    }
    
    public byte[] getContent() throws Exception {
        if (content == null || (content != null && lastModified < getLastModified())) {
        	content = resource instanceof URL ? ResourceUtils.readBinaryUrl((URL) resource) : ResourceUtils.readBinaryFile((File) resource);
        	lastModified = getLastModified();
        }
        return content;
    }

    public long getLastModified() throws IOException {
        return resource instanceof URL ? ((URL) resource).openConnection().getLastModified() : ((File) resource).lastModified();        
    }
    
}