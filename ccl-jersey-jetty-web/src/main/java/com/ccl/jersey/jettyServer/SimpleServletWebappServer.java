package com.ccl.jersey.jettyServer;

/**
 * Created by ccl on 17/8/9.
 */

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class SimpleServletWebappServer {
    public static void main(String[] args) throws Exception {
        try {
            Server server = new Server(8088);//1.建立server，设置端口
            // Create JAX-RS application.
            final ResourceConfig application = new ResourceConfig()
                    .packages("com.ccl.jersey.action")
                    .register(JacksonFeature.class);

            ServletHolder sh = new ServletHolder(new ServletContainer(application));//2.servlet

            ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
            servletContext.setContextPath("/rest");
            servletContext.addServlet(sh,"/*");
//            servletContext.addServlet(new ServletHolder(new HelloServlet("Buongiorno Mondo")),"/it/*");
//            servletContext.addServlet(new ServletHolder(new HelloServlet("Bonjour le Monde")),"/fr/*");


            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setContextPath("/"); //4.上下文路径  http://localhost:8088/
            webAppContext.setResourceBase("ccl-jersey-jetty-web/src/main/webapp"); // 你的资源文件所在的路径
            //webAppContext.setDefaultsDescriptor("ccl-jersey-jetty-web/src/main/webapp/WEB-INF/web.xml");
//            webAppContext.setDescriptor("ccl-jersey-jetty-web/src/main/webapp/WEB-INF/web.xml");
//            webAppContext.setWelcomeFiles(new String[] {"ccl-jersey-jetty-web/src/main/webapp/index.html"});
//            webAppContext.setWelcomeFiles(new String[] {"index.html"});
            webAppContext.setClassLoader(Thread.currentThread().getContextClassLoader());
            webAppContext.setConfigurationDiscovered(true);
            webAppContext.setParentLoaderPriority(true);

            ContextHandlerCollection contexts = new ContextHandlerCollection();

            contexts.setHandlers(new Handler[] { servletContext, webAppContext });

            server.setHandler(contexts);//6.server添加上下文

            System.out.println("ContextPath: " + webAppContext.getContextPath());
            System.out.println("Descriptor: " + webAppContext.getDescriptor());
            System.out.println("DefaultsDescriptor: " + webAppContext.getDefaultsDescriptor());
            System.out.println("ResourceBase: " + webAppContext.getResourceBase());
            System.out.println("BaseResource: " + webAppContext.getBaseResource());
            System.out.println("WelcomFiles: " + webAppContext.getWelcomeFiles());
            System.out.println("servletContext ContextPath: " + servletContext.getContextPath());
            System.out.println("servletContext ContextPath: " + servletContext.getServletContext().getContextPath());

            server.start();
            server.join();

        } catch (Exception e) {

        }
    }

}