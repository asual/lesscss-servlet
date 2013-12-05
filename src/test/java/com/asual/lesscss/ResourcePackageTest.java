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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

/**
 * @author Rostislav Hristov
 */
public class ResourcePackageTest {

	@Test
	public void css() throws IOException, Exception {
		ResourcePackage rp = new ResourcePackage(new String[] {
				"/css/test1.css", "/css/test2.css" });
		rp.setName("package");
		rp.setVersion("1.0.0");
		rp.setExtension("css");
		assertEquals(
				"/package-1.0.0-MwovY3NzL3Rlc3QxLmNzcwovY3NzL3Rlc3QyLmNzcw.css",
				rp.toString());
	}

	@Test
	public void js() throws Exception {
		String name = "test";
		String[] resources = new String[] { "/js/test1.js", "/js/test2.js",
				"/js/test3.js", "/js/test4.js", "/js/test5.js" };
		String version = "1.0.0";
		ResourcePackage rp = new ResourcePackage(resources);
		rp.setName(name);
		rp.setVersion(version);
		rp.setExtension("js");
		rp = ResourcePackage.fromString(rp.toString());
		assertEquals(name, rp.getName());
		assertArrayEquals(resources, rp.getResources());
		assertEquals(version, rp.getVersion());
	}
	
	@Test
	public void js2() {
		String name = "jquery.caret-range";
		String version = "1.0";
		String[] resources = new String[] {"/js/jquery.caret-range-1.0.js" };
		
		ResourcePackage rp = new ResourcePackage(resources);
		rp.setName(name);
		rp.setVersion(version);
		rp.setExtension("js");
		rp = ResourcePackage.fromString(rp.toString());
		assertEquals(name, rp.getName());
		assertArrayEquals(resources, rp.getResources());
		assertEquals(version, rp.getVersion());
	}
}
