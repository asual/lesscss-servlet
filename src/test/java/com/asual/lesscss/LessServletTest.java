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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Rostislav Hristov
 */
public class LessServletTest  {

	private static ServletTester tester;
 
	@BeforeClass
	public static void before() throws Exception {
		tester = new ServletTester();
		tester.setClassLoader(LessServletTest.class.getClassLoader());
		tester.setContextPath("/");
		tester.addServlet(LessServlet.class, "*.css");
		tester.start();
	}
  
	@Test
	public void css() throws IOException, Exception {
		HttpTester request = new HttpTester();
		request.setMethod("GET");
		request.setHeader("Host", "tester");
		request.setVersion("HTTP/1.1");
		request.setURI("/css/test.css");
		HttpTester response = new HttpTester();
		response.parse(tester.getResponses(request.generate()));
		assertEquals("body{color:#f0f0f0;}", response.getContent());
	}
	
	@AfterClass
	public static void after() throws Exception {
		tester.stop();
	}
	
}