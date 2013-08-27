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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

	private final static Map<String, String> cache = new ConcurrentHashMap<String, String>();
	private final static Log logger = LogFactory.getLog(ResourcePackage.class);
	private final static List<String> extensions = Arrays.asList("css", "less",
			"js");

	private static int NAME_FLAG = 1;
	private static int VERSION_FLAG = 2;
	private static int DEFLATE = 32;
	private static String ENCODING = "UTF-8";
	private static String NEW_LINE = "\n";
	private static String SEPARATOR = "-";

	private String[] resources;
	private String version;
	private String name;
	private String extension;

	public ResourcePackage(String[] resources) {
		this.resources = resources;
	}

	public static ResourcePackage fromString(String source) {
		if (!StringUtils.isEmpty(source)) {
			try {
				String key;
				String path = null;
				String extension = null;
				int slashIndex = source.lastIndexOf("/");
				int dotIndex = source.lastIndexOf(".");
				if (dotIndex != -1 && slashIndex < dotIndex) {
					extension = source.substring(dotIndex + 1);
					path = source.substring(0, dotIndex);
				} else {
					path = source;
				}
				if (extension != null && !extensions.contains(extension)) {
					return null;
				}
				String[] parts = path.replaceFirst("^/", "").split(SEPARATOR);
				if (cache.containsValue(source)) {
					key = getKeyFromValue(cache, source);
				} else {
					key = parts[parts.length - 1];
					byte[] bytes = null;
					try {
						bytes = Base64.decodeBase64(key.getBytes(ENCODING));
						bytes = inflate(bytes);
					} catch (Exception e) {
					}
					key = new String(bytes, ENCODING);
				}
				String[] data = key.split(NEW_LINE);
				ResourcePackage rp = new ResourcePackage(
						(String[]) ArrayUtils.subarray(data, 1, data.length));
				int mask = Integer.valueOf(data[0]);
				if ((mask & NAME_FLAG) != 0) {
					rp.setName(parts[0]);
				}
				if ((mask & VERSION_FLAG) != 0) {
					rp.setVersion(parts[rp.getName() != null ? 1 : 0]);
				}
				rp.setExtension(extension);
				return rp;
			} catch (Exception e) {
			}
		}
		return null;
	}

	public String toString() {
		try {
			if (extension != null && !extensions.contains(extension)) {
				throw new Exception("Unsupported extension: " + extension);
			}
			int mask = 0;
			if (name != null) {
				mask = mask | NAME_FLAG;
			}
			if (version != null) {
				mask = mask | VERSION_FLAG;
			}
			String key = mask + NEW_LINE
					+ StringUtils.join(resources, NEW_LINE);
			if (!cache.containsKey(key)) {
				byte[] bytes = key.getBytes(ENCODING);
				StringBuilder sb = new StringBuilder();
				sb.append("/");
				sb.append(name == null ? "" : name + SEPARATOR);
				sb.append(version == null ? "" : version + SEPARATOR);
				sb.append(Base64.encodeBase64URLSafeString(
						bytes.length < DEFLATE ? bytes : deflate(bytes))
						.replaceAll("-", "+"));
				sb.append(extension == null ? "" : "." + extension);
				cache.put(key, sb.toString());
			}
			return cache.get(key);
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

	public static <K, V> K getKeyFromValue(Map<K, V> m, V value) {
		for (K o : m.keySet()) {
			if (m.get(o).equals(value)) {
				return o;
			}
		}
		return null;
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

	private static byte[] inflate(byte[] output) throws DataFormatException,
			IOException {
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