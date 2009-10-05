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
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rostislav Hristov
 */
public class LessServlet extends HttpServlet {

    private static final long serialVersionUID = 413708886190444579L;
    
    private final Log logger = LogFactory.getLog(getClass());
    private int maxAge = 31556926;
    private int milliseconds = 1000;
    private LessEngine engine;
    private LessCache cache;
    
    private Map<String, String> mimeTypes = new HashMap<String, String>();
    
    {
        mimeTypes.put(".css", "text/css");
        mimeTypes.put(".gif", "image/gif");
        mimeTypes.put(".ico", "image/vnd.microsoft.icon");
        mimeTypes.put(".jpeg", "image/jpeg");
        mimeTypes.put(".jpg", "image/jpeg");
        mimeTypes.put(".js", "text/javascript");
        mimeTypes.put(".png", "image/png");
    }
    
    @PostConstruct
    public void init() {
    	if (engine == null) {
			engine = new LessEngine();
    	}
    	if (cache == null) {
    	    cache = new LessCache();    	    
    	}
    	if (getServletConfig() != null && getInitParameter("charset") != null) {
    	    engine.setCharset(getInitParameter("charset"));
    	}
	}

    @PreDestroy
    public void destroy() {
    	if (engine != null) {
    		engine.destroy();
    	}
	}
    
    public final void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        URL url = getServletContext().getResource("/META-INF" + request.getRequestURI());
        
        if (url == null) {
            url = getServletContext().getResource("/META-INF/resources" + request.getRequestURI());
        }        
        if (url == null) {
            url = getClass().getClassLoader().getResource("META-INF" + request.getRequestURI());
        }
        if (url == null) {
            url = getClass().getClassLoader().getResource("META-INF/resources" + request.getRequestURI());
        }

        File file = null;
        try {
            file = new File(getServletContext().getRealPath(request.getRequestURI()));
        } catch (Exception e) {}
        
        if (url != null || (file != null && file.exists())) {
            
            Long lastModified = url != null ? url.openConnection().getLastModified() : file.lastModified();
            Long ifModifiedSince = request.getDateHeader("If-Modified-Since");
            
            if (ifModifiedSince != null && ifModifiedSince/milliseconds == lastModified/milliseconds) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            
            String path = url != null ? url.getPath() : file.getAbsolutePath();
            String extension = path.substring(path.lastIndexOf("."));
            String mimeType = (mimeTypes.containsKey(extension)) ? mimeTypes.get(extension) : getServletContext().getMimeType(path);

            byte[] content = null;
                        
            try {
                if ("text/css".equals(mimeType)) {
                    content = (url != null ? engine.compile(url) : engine.compile(file)).getBytes(engine.getCharset());
                } else {
                    if (!cache.contains(path) || (cache.contains(path) && cache.getLastModified(path) < lastModified)) {                    
                        cache.setContent(path, url != null ? LessUtils.readURL(url) : LessUtils.readFile(file)).setLastModified(path, lastModified);
                    }
                    content = (byte[]) cache.getContent(path);
                }
            } catch (LessException ce) {
                logger.error("Error processing: " + request.getRequestURI());
                throw new ServletException(ce.getMessage(), ce);
            }
            
            response.setContentType(mimeType);
            response.setDateHeader("Expires", new Date().getTime() + maxAge*milliseconds);
            response.addDateHeader("Cache-control: max-age=", maxAge);
            response.addDateHeader("Last-Modified", lastModified);
            
            boolean gzip = false;
            String encoding = request.getHeader("Accept-Encoding");
            if (encoding != null) {
                encoding = encoding.replaceAll("\\s+", "").toLowerCase();
                gzip = encoding.indexOf("gzip") != -1 || request.getHeader("---------------") != null;
                encoding = encoding.indexOf("x-gzip") != -1 ? "x-gzip" : "gzip";
            }
            
            if (gzip && "true".equals(getInitParameter("gzip"))) {
                
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