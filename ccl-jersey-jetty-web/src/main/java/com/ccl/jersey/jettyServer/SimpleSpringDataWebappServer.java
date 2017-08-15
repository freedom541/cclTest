package com.ccl.jersey.jettyServer;

/**
 * Created by ccl on 17/8/9.
 */

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class SimpleSpringDataWebappServer {
    public static void main(String[] args) throws Exception {
        try {
            Server server = new Server(8088);//1.建立server，设置端口
            //3.请求处理资源
            ApplicationConfig applicationConfig = new ApplicationConfig();
            ServletHolder sh = new ServletHolder(new ServletContainer(applicationConfig));

            ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
            servletContext.setContextPath("/rest");
            servletContext.addServlet(sh,"/*");
            servletContext.addEventListener(new ContextLoaderListener());
            servletContext.addEventListener(new RequestContextListener());
            servletContext.setInitParameter("contextClass", AnnotationConfigWebApplicationContext.class.getName());
            servletContext.setInitParameter("contextConfigLocation", SpringRootConfiguration.class.getName());

            //添加乱码过滤
            FilterHolder filterHolder = new FilterHolder(new CharacterEncodingFilter());
            Map<String, String> initParams = new HashMap<>();
            initParams.put("encoding", "UTF-8");
            initParams.put("forceEncoding", "true");
            if (null != initParams) {
                for (Map.Entry<String, String> entry : initParams.entrySet()) {
                    filterHolder.setInitParameter(entry.getKey(), entry.getValue());
                }
            }
            servletContext.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));

            //web层设置
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

    @Configuration
    @ComponentScan("com.ccl.jersey")
    @ImportResource({"classpath*:META-INF/spring/*.xml"})
    public static class SpringRootConfiguration {

    }
}