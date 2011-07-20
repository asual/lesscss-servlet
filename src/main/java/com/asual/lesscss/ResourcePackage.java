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
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rostislav Hristov
 */
public class ResourcePackage {
	
	private static int NAME_FLAG = 1;
	private static int VERSION_FLAG = 2;
	private static int DEFLATE = 32;
	private static String ENCODING = "UTF-8";
	private static String NEW_LINE = "\n";
	private static String SEPARATOR = "-";
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private String[] resources;
	private String version;
	private String name;
	private String extension;
	
	public ResourcePackage(String[] resources) {
		this.resources = resources;
	}
	
	public static ResourcePackage fromString(String source) {
		
		if (!StringUtils.isEmpty(source)) {
			
			String extension = null;
			int slashIndex = source.lastIndexOf("/");
			int dotIndex = source.lastIndexOf(".");
			if (dotIndex != -1 && slashIndex < dotIndex) {
				extension = source.substring(dotIndex);
				source = source.substring(0, dotIndex);
			}
			
			String[] parts = source.replaceFirst("^/", "").split(SEPARATOR);
			try {
				byte[] output = parts[parts.length - 1].getBytes(ENCODING);
				try {
					output = inflate(Base64.decodeBase64(output));
				} catch (Exception e) {
					output = Base64.decodeBase64(output);
				}
				String[] data = new String(output, ENCODING).split(NEW_LINE);
				ResourcePackage vp = new ResourcePackage((String[]) ArrayUtils.subarray(data, 1, data.length));
				int mask = Integer.valueOf(data[0]);
				if ((mask & NAME_FLAG) != 0) {
					vp.setName(parts[0]);
				}
				if ((mask & VERSION_FLAG) != 0) {
					vp.setVersion(parts[vp.getName() != null ? 1 : 0]);
				}
				vp.setExtension(extension);
				return vp;
			} catch (Exception e) {}
		}
		return null;
	}
	
	public String toString() {
		try {
			int mask = 0;
			if (name != null) {
				mask = mask | NAME_FLAG;
			}
			if (version != null) {
				mask = mask | VERSION_FLAG;
			}
			byte[] bytes = (mask + NEW_LINE + StringUtils.join(resources, NEW_LINE)).getBytes(ENCODING);
			StringBuilder sb = new StringBuilder();
			sb.append("/");
			sb.append(name == null ? "" : name + SEPARATOR);
			sb.append(version == null ? "" : version + SEPARATOR);
			sb.append(Base64.encodeBase64URLSafeString(bytes.length < DEFLATE ? bytes : deflate(bytes)).replaceAll("-", "+"));
			sb.append(extension == null ? "" : "." + extension);
			return sb.toString();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public String[] getResources() {
		return resources;
	}

	private static byte[] deflate(byte[] input) throws IOException {
		Deflater deflater = new Deflater();
		deflater.setLevel(Deflater.BEST_COMPRESSION);
		deflater.setInput(input);
		deflater.finish();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
		byte[] buf = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buf);
			baos.write(buf, 0, count);
		}
		baos.close();
		return baos.toByteArray();
	}
	
	private static byte[] inflate(byte[] output) throws DataFormatException, IOException {
		Inflater inflater = new Inflater();
		inflater.setInput(output);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(output.length);
		byte[] buf = new byte[1024];
		while (!inflater.finished()) {
			int count = inflater.inflate(buf);
			baos.write(buf, 0, count);
		}
		baos.close();
		return baos.toByteArray();
	}
}