package com.ccl.jersey.jettyServer;

/**
 * Created by ccl on 17/8/10.
 */

import com.ccl.jersey.SpringInit;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class ServletContextServer {

    public static void main(String[] args) throws Exception {
        init();
    }
    public static  void init() throws Exception{
        Server server = new Server(8080);
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");
        // http://localhost:8080/hello
//        context.addServlet(new ServletHolder(new HelloServlet()), "/hello.do");
        // http://localhost:8080/hello/kongxx
//        context.addServlet(new ServletHolder(new HelloServlet("Hello Kongxx!")), "/hello/kongxx");

        // http://localhost:8080/goodbye
//        context.addServlet(new ServletHolder(new GoodbyeServlet()), "/goodbye");
        // http://localhost:8080/goodbye/kongxx
//        context.addServlet(new ServletHolder(new GoodbyeServlet("Goodbye kongxx!")), "/goodbye/kongxx");

        /**
         * 当 jersey 不集成spring 时，使用的是 ServletContainer.class
         */
        ServletHolder holder1 = new ServletHolder(ServletContainer.class);
        holder1.setInitParameter("com.sun.jersey.config.property.packages", "jersey");
        holder1.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        contextHandler.addServlet(holder1, "/test/*");

        /**
         * 当 jersey 集成spring 时，使用的是 SpringServlet.class
         */
        ServletHolder holder2 = new ServletHolder(SpringServlet.class);
        /**
         * 加载spring 配置文件
         */
        ApplicationContext ctx = new ClassPathXmlApplicationContext( "classpath:application.xml");

        SpringInit.setApplicationContext(ctx);
        System.out.println(SpringInit.getConfigLoad().getWebPort());
        holder2.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        /**
         * 设置处理器（servlet ）所在的包,如果不设置，会找不到那个处理的servlet
         */
        holder2.setInitParameter("com.sun.jersey.config.property.packages", "jersey");
        /**
         * 使 jersey 设置的返回类型 （json。。。）有效
         */
        holder2.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        /**
         * 为jersey的ServletContextHandler 设置  ApplicationContext
         */
        contextHandler.setClassLoader(ctx.getClassLoader());
        XmlWebApplicationContext xmlWebAppContext = new XmlWebApplicationContext();
        xmlWebAppContext.setParent(ctx);
        xmlWebAppContext.setConfigLocation("");
        xmlWebAppContext.setServletContext(contextHandler.getServletContext());
        xmlWebAppContext.refresh();
        contextHandler.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,xmlWebAppContext);
        /**
         * 设置  servlet对应的访问路径
         */
        contextHandler.addServlet(holder2, "/springTest/*");

        server.setHandler(contextHandler);
        server.start();
        System.out.println("**********************************");
        System.out.println("**                              **");
        System.out.println("**             启动成功                                        **");
        System.out.println("**                              **");
        System.out.println("**********************************");
        server.join();
    }
}