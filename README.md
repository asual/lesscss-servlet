LESS Servlet
============

LESS Servlet is a Maven 2 artifact that enables the power of LESS in 
Java based web applications. In addition it provides optimized access
to static classpath resources and compresses the CSS and JavaScript 
output.

Usage
-----

The following sample demonstrates how the Servlet can be configured 
in the web.xml descriptor file.

    <servlet>
        <servlet-name>lesscss</servlet-name>
        <servlet-class>com.asual.lesscss.LessServlet</servlet-class>
        <init-param>
            <param-name>gzip</param-name>
            <param-value>true</param-value>
        </init-param>
        <init-param>
            <param-name>compress</param-name>
            <param-value>true</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    
    <servlet-mapping>
        <servlet-name>lesscss</servlet-name>
        <url-pattern>*.css</url-pattern>
    </servlet-mapping>
