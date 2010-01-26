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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rostislav Hristov
 */
public class ResourceServlet extends HttpServlet {

    private static final long serialVersionUID = 413708886190444579L;
    
    protected final Log logger = LogFactory.getLog(getClass());
    protected int maxAge = 31556926;
    protected long milliseconds = 1000L;
    protected boolean cache;
    protected boolean compress;
    protected String charset = "UTF-8";
    protected String separator = ";";
    protected Map<String, Resource> resources;
    protected Map<String, String> mimeTypes = new HashMap<String, String>();
    {
        mimeTypes.put("css", "text/css");
        mimeTypes.put("html", "text/html");
        mimeTypes.put("htm", "text/html");
        mimeTypes.put("js", "text/javascript");
        mimeTypes.put("txt", "text/plain");
        mimeTypes.put("xml", "text/xml");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("ico", "image/vnd.microsoft.icon");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("png", "image/png");
    }
    
    public void init() {
        if (getServletConfig() != null) {
            if (getInitParameter("charset") != null) {
                charset = getInitParameter("charset");
            }
            if (getInitParameter("separator") != null) {
                separator = getInitParameter("separator");
            }
            if (getInitParameter("cache") != null) {
                cache = Boolean.valueOf(getInitParameter("cache"));
            }
            if (getInitParameter("compress") != null) {
                compress = Boolean.valueOf(getInitParameter("compress"));
            }
        }
    	resources = new HashMap<String, Resource>();
    }

    protected Resource getResource(String uri) throws ResourceNotFoundException {
        String mimeType = getResorceMimeType(uri);
    	if (!resources.containsKey(uri)) {
    		if ("text/css".equals(mimeType)) {
	        	resources.put(uri, new StyleResource(getServletContext(), uri, charset, cache, compress));
	        } else if ("text/javascript".equals(mimeType)) {
	        	resources.put(uri, new ScriptResource(getServletContext(), uri, charset, cache, compress));
	        } else {
	        	resources.put(uri, new Resource(getServletContext(), uri, charset, cache));
	        }
    	}
    	return resources.get(uri);
    }
    
    protected byte[] getResorceContent(String uri) throws Exception {
        Resource resource = getResource(uri);
        return resource.getContent();
    }
    
    protected long getResorceLastModified(String uri) throws ResourceNotFoundException, IOException {
        Resource resource = getResource(uri);
        return resource.getLastModified();
    }
    
    protected String getResorceMimeType(String uri) {
        
        String extension = uri.substring(uri.lastIndexOf(".") + 1);
        String mimeType = mimeTypes.containsKey(extension) ? mimeTypes.get(extension) : getServletContext().getMimeType(uri);
        return mimeType != null ? mimeType : "application/octet-stream";
    }
    
    protected byte[] mergeContent(byte[] c1, byte[] c2) throws UnsupportedEncodingException {
        
        byte[] line = "\n".getBytes(charset);
        int l1 = c1.length;
        int l2 = l1 != 0 ? line.length : 0;
        int l3 = c2.length;
        byte[] result = new byte[l1 + l2 + l3];
        
        for (int i = 0; i < l1; i++) {
            result[i] = c1[i];
        }
        for (int i = 0; i < l2; i++) {
            result[i + l1] = line[i];
        }
        for (int i = 0; i < l3; i++) {
            result[i + l1 + l2] = c2[i];
        }
        return result;
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        try {
            
            String uri = request.getRequestURI().replaceAll("/+", "/");
            String mimeType = getResorceMimeType(uri);
            
            long lastModified = 0;
            byte[] content = new byte[0];
            
            for (String resource : uri.split(separator)) {
                content = mergeContent(content, getResorceContent(resource));
                lastModified = Math.max(lastModified, getResorceLastModified(resource));
            }
            
            long ifModifiedSince = request.getDateHeader("If-Modified-Since");
            if (ifModifiedSince != 0 && ifModifiedSince/milliseconds == lastModified/milliseconds) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            
            response.setContentType(mimeType + (mimeType.startsWith("text/") ? ";charset=" + charset : ""));
            response.setDateHeader("Expires", System.currentTimeMillis() + maxAge*milliseconds);
            response.setDateHeader("Last-Modified", lastModified);
            response.setHeader("Cache-control", "max-age=" + maxAge);
            response.setContentLength(content.length);
            response.getOutputStream().write(content);
            response.getOutputStream().flush();
            response.getOutputStream().close();
            
        } catch (Exception e) {
            throw new ServletException(e.getMessage(), e);
        }
    }
}