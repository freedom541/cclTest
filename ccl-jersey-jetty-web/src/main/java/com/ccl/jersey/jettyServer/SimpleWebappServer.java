package com.ccl.jersey.jettyServer;

/**
 * Created by ccl on 17/8/9.
 */

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class SimpleWebappServer {
    public static void main(String[] args) throws Exception {
        try {
            Server server = new Server(8088);//1.建立server，设置端口

            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setContextPath("/"); //4.上下文路径  http://localhost:8088/
            webAppContext.setResourceBase("ccl-jersey-jetty-web/src/main/webapp"); // 你的资源文件所在的路径
            //webAppContext.setDefaultsDescriptor("ccl-jersey-jetty-web/src/main/webapp/WEB-INF/web.xml");
            webAppContext.setDescriptor("ccl-jersey-jetty-web/src/main/webapp/WEB-INF/web.xml");
            webAppContext.setWelcomeFiles(new String[] {"ccl-jersey-jetty-web/src/main/webapp/index.html"});
            webAppContext.setClassLoader(Thread.currentThread().getContextClassLoader());
            webAppContext.setConfigurationDiscovered(true);
            webAppContext.setParentLoaderPriority(true);

            server.setHandler(webAppContext);//6.server添加上下文

            System.out.println("ContextPath: " + webAppContext.getContextPath());
            System.out.println("Descriptor: " + webAppContext.getDescriptor());
            System.out.println("DefaultsDescriptor: " + webAppContext.getDefaultsDescriptor());
            System.out.println("ResourceBase: " + webAppContext.getResourceBase());
            System.out.println("BaseResource: " + webAppContext.getBaseResource());
            System.out.println("WelcomFiles: " + webAppContext.getWelcomeFiles());

            server.start();
            server.join();

        } catch (Exception e) {

        }
    }

}