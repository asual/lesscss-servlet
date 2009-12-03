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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
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
    protected int milliseconds = 1000;
    protected String charset = "UTF-8";
    protected String separator = ";";
    protected boolean compress = false;
    protected Map<String, Resource> resources;
    protected Map<String, String> mimeTypes = new HashMap<String, String>();
    {
        mimeTypes.put("css", "text/css");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("ico", "image/vnd.microsoft.icon");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("js", "text/javascript");
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
            if (getInitParameter("compress") != null) {
                compress = Boolean.valueOf(getInitParameter("compress"));
            }
        }
    	resources = new HashMap<String, Resource>();
    }
    
    protected URL getUrl(String path) throws MalformedURLException {
        URL url = getServletContext().getResource("/META-INF" + path);
        if (url != null) {
            return url;
        }
        url = getServletContext().getResource("/META-INF/resources" + path);
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
        return null;
    }
    
    protected File getFile(String path) {
        try {
            return new File(getServletContext().getRealPath(path));
        } catch (Exception e) {
            
        }
        return null;
    }

    protected Resource getResource(String uri, Object resource) {
        String mimeType = getResorceMimeType(uri);
    	if (!resources.containsKey(uri)) {
    		if ("text/css".equals(mimeType)) {
	        	resources.put(uri, new StyleResource(uri, resource, charset, compress));
	        } else if ("text/javascript".equals(mimeType)) {
	        	resources.put(uri, new ScriptResource(uri, resource, charset, compress));
	        } else {
	        	resources.put(uri, new Resource(uri, resource, charset, compress));
	        }
    	}
    	return resources.get(uri);
    }
    
    protected byte[] getResorceContent(String uri) 
        throws MalformedURLException, ResourceNotFoundException, ServletException {
        
        URL url = getUrl(uri);
        File file = getFile(uri);
        
        if (url != null || (file != null && file.exists())) {
            
            String path = url != null ? url.getPath() : file.getAbsolutePath();
            Resource resource = getResource(path, url != null ? url : file);
                        
            try {
                return resource.getContent();
            } catch (Exception e) {
                logger.error("Error processing: " + uri);
                throw new ServletException(e.getMessage(), e);
            }

        } else {
            
            logger.error("Error processing: " + uri);
            throw new ResourceNotFoundException("Error processing: " + uri);
        }        
    }
    
    protected long getResorceLastModified(String uri)
        throws MalformedURLException, ResourceNotFoundException, ServletException {
        
        URL url = getUrl(uri);
        File file = getFile(uri);
        
        if (url != null || (file != null && file.exists())) {
            
            String path = url != null ? url.getPath() : file.getAbsolutePath();
            Resource resource = getResource(path, url != null ? url : file);
            
            try {
                return resource.getLastModified();
            } catch (Exception e) {
                logger.error("Error processing: " + uri);
                throw new ServletException(e.getMessage(), e);
            }            
            
        } else {
            
            logger.error("Error processing: " + uri);
            throw new ResourceNotFoundException("Error processing: " + uri);
        }         
    }
    
    protected String getResorceMimeType(String uri) {
        
        String extension = uri.substring(uri.lastIndexOf(".") + 1);
        return mimeTypes.containsKey(extension) ? mimeTypes.get(extension) : getServletContext().getMimeType(uri);
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

    public void service(HttpServletRequest request, HttpServletResponse response) 
        throws IOException, MalformedURLException, ResourceNotFoundException, ServletException {

        String mimeType = getResorceMimeType(request.getRequestURI());
        
        long lastModified = 0;
        byte[] content = new byte[0];
        
        for (String uri : request.getRequestURI().split(separator)) {
            content = mergeContent(content, getResorceContent(uri));
            lastModified = Math.max(lastModified, getResorceLastModified(uri));
        }
        
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        if (ifModifiedSince != 0 && ifModifiedSince/milliseconds == lastModified/milliseconds) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        
        response.setContentType(mimeType + (mimeType.startsWith("text/") ? ";charset=" + charset : ""));
        response.setDateHeader("Expires", new Date().getTime() + maxAge*milliseconds);
        response.addDateHeader("Cache-control: max-age=", maxAge);
        response.addDateHeader("Last-Modified", lastModified);
        response.setContentLength(content.length);
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }
    
}