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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
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
		byte[] result;
		try {
			URLConnection urlc = source.openConnection();
			StringBuffer sb = new StringBuffer(1024);
			InputStream input = urlc.getInputStream();
			UnicodeReader reader = new UnicodeReader(input, encoding);
			try {
				char[] cbuf = new char[32];
				int r;
				while ((r = reader.read(cbuf, 0, 32)) != -1) {
					sb.append(cbuf, 0, r);
				}
				result = sb.toString().getBytes(reader.getEncoding());
			} finally {
				reader.close();
				input.close();
			}
		} catch (IOException e) {
			logger.error("Can't read '" + source.getFile() + "'.");
			throw e;
		}
		return result;
	}
	
	public static byte[] readTextFile(File source, String encoding) throws IOException {
		byte[] result;
		try {
			StringBuffer sb = new StringBuffer(1024);
			FileInputStream input = new FileInputStream(source);
			UnicodeReader reader = new UnicodeReader(input, encoding);
			try {
				char[] cbuf = new char[32];
				int r;
				while ((r = reader.read(cbuf, 0, 32)) != -1) {
					sb.append(cbuf, 0, r);
				}
				result = sb.toString().getBytes(reader.getEncoding());
			} finally {
				reader.close();
				input.close();
			}
		} catch (IOException e) {
			logger.error("Can't read '" + source.getAbsolutePath() + "'.");
			throw e;
		}
		return result;
	}
	
	public static byte[] readBinaryUrl(URL source) throws IOException {
		byte[] result;
		try {
			URLConnection urlc = source.openConnection();
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			InputStream input = urlc.getInputStream();
			try {
				byte[] buffer = new byte[1024];
				int bytesRead = -1;
				while ((bytesRead = input.read(buffer)) != -1) {
					byteStream.write(buffer, 0, bytesRead);
				}
				result = byteStream.toByteArray();
			} finally {
				byteStream.close();
				input.close();
			}
		} catch (IOException e) {
			logger.error("Can't read '" + source.getFile() + "'.");
			throw e;
		}
		return result;
	}
	
	public static byte[] readBinaryFile(File source) throws IOException {
		byte[] result;
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			FileInputStream input = new FileInputStream(source);
			try {
				byte[] buffer = new byte[1024];
				int bytesRead = -1;
				while ((bytesRead = input.read(buffer)) != -1) {
					byteStream.write(buffer, 0, bytesRead);
				}
				result = byteStream.toByteArray();
			} finally {
				byteStream.close();
				input.close();
			}
		} catch (IOException e) {
			logger.error("Can't read '" + source.getAbsolutePath() + "'.");
			throw e;
		}
		return result;
	}
	
	/**
	 * Generic unicode textreader, which will use BOM mark
	 * to identify the encoding to be used. If BOM is not found
	 * then use a given default or system encoding.
	 * 
	 * @author Thomas Weidenfeller
	 * @author Aki Nieminen
	 */
	private static class UnicodeReader extends Reader {
		
		PushbackInputStream internalIn;
		InputStreamReader internalIn2 = null;
		String defaultEnc;

		private static final int BOM_SIZE = 4;

		/**
		 * @param in inputstream to be read
		 * @param defaultEnc default encoding if stream does not have 
		 *				   BOM marker. Give NULL to use system-level default.
		 */
		public UnicodeReader(InputStream in, String defaultEnc) {
			internalIn = new PushbackInputStream(in, BOM_SIZE);
			this.defaultEnc = defaultEnc;
		}
		
		/**
		 * Get stream encoding or NULL if stream is uninitialized.
		 * Call init() or read() method to initialize it.
		 */
		public String getEncoding() {
			if (internalIn2 == null) return null;
			return internalIn2.getEncoding();
		}

		/**
		 * Read-ahead four bytes and check for BOM marks. Extra bytes are
		 * unread back to the stream, only BOM bytes are skipped.
		 */
		protected void init() throws IOException {
			
			if (internalIn2 != null) return;

			String encoding;
			byte bom[] = new byte[BOM_SIZE];
			int n, unread;
			n = internalIn.read(bom, 0, bom.length);

			if ( (bom[0] == (byte)0x00) && (bom[1] == (byte)0x00) &&
					(bom[2] == (byte)0xFE) && (bom[3] == (byte)0xFF) ) {
				encoding = "UTF-32BE";
				unread = n - 4;
			} else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) &&
					(bom[2] == (byte)0x00) && (bom[3] == (byte)0x00) ) {
				encoding = "UTF-32LE";
				unread = n - 4;
			} else if (  (bom[0] == (byte)0xEF) && (bom[1] == (byte)0xBB) &&
					(bom[2] == (byte)0xBF) ) {
				encoding = "UTF-8";
				unread = n - 3;
			} else if ( (bom[0] == (byte)0xFE) && (bom[1] == (byte)0xFF) ) {
				encoding = "UTF-16BE";
				unread = n - 2;
			} else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) ) {
				encoding = "UTF-16LE";
				unread = n - 2;
			} else {
				// Unicode BOM mark not found, unread all bytes
				encoding = defaultEnc;
				unread = n;
			}

			if (unread > 0) internalIn.unread(bom, (n - unread), unread);

			// Use given encoding
			if (encoding == null) {
				internalIn2 = new InputStreamReader(internalIn);
			} else {
				internalIn2 = new InputStreamReader(internalIn, encoding);
			}
		}

		public void close() throws IOException {
			init();
			internalIn2.close();
		}

		public int read(char[] cbuf, int off, int len) throws IOException {
			init();
			return internalIn2.read(cbuf, off, len);
		}

	}	
}