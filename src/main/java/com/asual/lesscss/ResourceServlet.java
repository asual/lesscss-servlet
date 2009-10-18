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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

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
    protected boolean compress = false;
    protected boolean gzip = false;
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
            if (getInitParameter("compress") != null) {
                compress = Boolean.valueOf(getInitParameter("compress"));
            }
            if (getInitParameter("gzip") != null) {
                gzip = Boolean.valueOf(getInitParameter("gzip"));
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
        } catch (Exception e) {}
        return null;
    }

    protected Resource getResource(String path, Object resource, String mimeType) {
    	if (!resources.containsKey(path)) {
    		if ("text/css".equals(mimeType)) {
	        	resources.put(path, new StyleResource(path, resource, charset, compress));
	        } else if ("text/javascript".equals(mimeType)) {
	        	resources.put(path, new ScriptResource(path, resource, charset, compress));
	        } else {
	        	resources.put(path, new Resource(path, resource, charset, compress));
	        }
    	}
    	return resources.get(path);
    }
    
    public void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        URL url = getUrl(request.getRequestURI());
        File file = getFile(request.getRequestURI());
        
        if (url != null || (file != null && file.exists())) {
            
            String path = url != null ? url.getPath() : file.getAbsolutePath();
            String extension = path.substring(path.lastIndexOf(".") + 1);
            String mimeType = (mimeTypes.containsKey(extension)) ? mimeTypes.get(extension) : getServletContext().getMimeType(path);
            Resource resource = getResource(path, url != null ? url : file, mimeType);

            long ifModifiedSince = request.getDateHeader("If-Modified-Since");
            long lastModified;
            
            try {
                lastModified = resource.getLastModified();
            } catch (Exception e) {
                logger.error("Error processing: " + request.getRequestURI());
                throw new ServletException(e.getMessage(), e);
            }            
            
            if (ifModifiedSince != 0 && ifModifiedSince/milliseconds == lastModified/milliseconds) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            
            byte[] content = null;
                        
            try {
                content = resource.getContent();
            } catch (Exception e) {
                logger.error("Error processing: " + request.getRequestURI());
                throw new ServletException(e.getMessage(), e);
            }
            
            response.setContentType(mimeType);
            response.setDateHeader("Expires", new Date().getTime() + maxAge*milliseconds);
            response.addDateHeader("Cache-control: max-age=", maxAge);
            response.addDateHeader("Last-Modified", lastModified);
            
            boolean shouldGzip = false;
            String encoding = request.getHeader("Accept-Encoding");
            if (encoding != null) {
                encoding = encoding.replaceAll("\\s+", "").toLowerCase();
                shouldGzip = encoding.indexOf("gzip") != -1 || request.getHeader("---------------") != null;
                encoding = encoding.indexOf("x-gzip") != -1 ? "x-gzip" : "gzip";
            }
            
            if (gzip && shouldGzip) {
                response.setHeader("Content-Encoding", encoding);
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                GZIPOutputStream gz = new GZIPOutputStream(byteStream);
                gz.write(content);
                gz.finish();
                byte[] bytes = byteStream.toByteArray();
                response.setContentLength(bytes.length);
                response.getOutputStream().write(bytes);
            } else {
                response.setContentLength(content.length);
                response.getOutputStream().write(content);
            }
            response.getOutputStream().flush();
            response.getOutputStream().close();
            
        } else {
            logger.error("Error processing: " + request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
}