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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rostislav Hristov
 */
public class ResourceUtils {
    
    private static final Log logger = LogFactory.getLog(ResourceUtils.class);
    
    public static byte[] readTextUrl(URL source, String encoding) throws IOException {
        
        URLConnection urlc = source.openConnection();
        StringBuffer sb = new StringBuffer(1024);
        try {
            UnicodeReader input = new UnicodeReader(urlc.getInputStream(), encoding);
            try {
                char[] cbuf = new char[32];
                int r;
                while ((r = input.read(cbuf, 0, 32)) != -1) {
                    sb.append(cbuf, 0, r);
                }
                return sb.toString().getBytes(input.getEncoding());
            } finally {
                input.close();
            }
        } catch (IOException e) {
            logger.error("Can't read '" + source.getFile() + "'.");
            throw e;
        }
    }
    
    public static byte[] readTextFile(File source, String encoding) throws IOException {
        
        StringBuffer sb = new StringBuffer(1024);
        try {
            UnicodeReader input = new UnicodeReader(new FileInputStream(source), encoding);
            try {
                char[] cbuf = new char[32];
                int r;
                while ((r = input.read(cbuf, 0, 32)) != -1) {
                    sb.append(cbuf, 0, r);
                }
                return sb.toString().getBytes(input.getEncoding());
            } finally {
                input.close();
            }
        } catch (IOException e) {
            logger.error("Can't read '" + source.getAbsolutePath() + "'.");
            throw e;
        }
    }
    
    public static byte[] readBinaryUrl(URL source) throws IOException {
        
        URLConnection urlc = source.openConnection();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            InputStream input = urlc.getInputStream();
            try {
                byte[] buffer = new byte[1024];
                int bytesRead = -1;
                while ((bytesRead = input.read(buffer)) != -1) {
                    byteStream.write(buffer, 0, bytesRead);
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            logger.error("Can't read '" + source.getFile() + "'.");
            throw e;
        }
        return byteStream.toByteArray();        
    }
    
    public static byte[] readBinaryFile(File source) throws IOException {
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            FileInputStream input = new FileInputStream(source);
            try {
                byte[] buffer = new byte[1024];
                int bytesRead = -1;
                while ((bytesRead = input.read(buffer)) != -1) {
                    byteStream.write(buffer, 0, bytesRead);
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            logger.error("Can't read '" + source.getAbsolutePath() + "'.");
            throw e;
        }
        return byteStream.toByteArray();
    }    
    
}