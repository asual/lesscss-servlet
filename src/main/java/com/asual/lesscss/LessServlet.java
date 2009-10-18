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

/**
 * @author Rostislav Hristov
 */
public class LessServlet extends ResourceServlet {

    private static final long serialVersionUID = 413708886190444579L;
    private LessEngine engine;
    
    public void init() {
        super.init();
        engine = new LessEngine();
    }

    public void destroy() {
    	engine.destroy();
    }

    protected Resource getResource(String path, Object object, String mimeType) {
    	if (!resources.containsKey(path)) {
	    	if ("text/css".equals(mimeType)) {
	    		resources.put(path, new LessResource(engine, path, object, charset, compress));
	    		return resources.get(path);
	    	} else {
	        	return super.getResource(path, object, mimeType);
	        }
    	} else {
    		return resources.get(path);
    	}
    }
    
}