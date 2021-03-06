package com.ccl.jersey.jettyServer;

/**
 * Created by ccl on 17/8/9.
 */

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class SimpleServletServer {
    public static void main(String[] args) throws Exception {
        try {
            Server server = new Server(8088);//1.建立server，设置端口
            // Create JAX-RS application.
            final ResourceConfig application = new ResourceConfig()
                    .packages("com.ccl.jersey.action")
                    .register(JacksonFeature.class);

            ServletHolder sh = new ServletHolder(new ServletContainer(application));//2.servlet

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS); //4.上下文
            //项目入口、若改为"/"，则为http://8080/加resource路径，而不是http://8080/jettyDemo/+resource
            context.setContextPath("/");
            //在项目入口下，添加servlet的路径，此处即处理/jettyDemo/下的resource
            context.addServlet(sh, "/*"); //5.上下文添加servlet

            server.setHandler(context);//6.server添加上下文

            System.out.println("ContextPath: " + context.getContextPath());
            System.out.println("Descriptor: " + context.getInitParameter("com.sun.jersey.config.property.packages"));

            //join的作用是当前线程阻塞，当server执行完毕后启动阻塞进程
            //当application较重的时侯用join，保证启动成功，这里没有也可以
            server.start();
            server.join();

        } catch (Exception e) {

        }
    }

}