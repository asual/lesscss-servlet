LESS Servlet
============

LESS Servlet enables optimized runtime processing of LESS resources. It can be used with 
any static file or classpath resource and performs minification using the YUI Compressor.

Usage
-----

The following sample demonstrates how the two provided servlets can be configured 
in the web.xml descriptor file.

    <servlet>
        <servlet-name>less</servlet-name>
        <servlet-class>com.asual.lesscss.LessServlet</servlet-class>
        <init-param>
            <param-name>compress</param-name>
            <param-value>true</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    
    <servlet>
        <servlet-name>resource</servlet-name>
        <servlet-class>com.asual.lesscss.ResourceServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>
        
    <servlet-mapping>
        <servlet-name>less</servlet-name>
        <url-pattern>*.css</url-pattern>
    </servlet-mapping>
    
    <servlet-mapping>
        <servlet-name>less</servlet-name>
        <url-pattern>/resources/js/*</url-pattern>
    </servlet-mapping>
    
    <servlet-mapping>
        <servlet-name>resource</servlet-name>
        <url-pattern>*.jpg</url-pattern>
    </servlet-mapping>    