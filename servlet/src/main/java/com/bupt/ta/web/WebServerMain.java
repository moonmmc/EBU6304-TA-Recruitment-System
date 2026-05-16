package com.bupt.ta.web;

import jakarta.servlet.ServletContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class WebServerMain {
    public static void main(String[] args) throws Exception {
        AppPaths.init();
        int port = 8080;
        Server server = new Server(port);

        WebAppContext ctx = new WebAppContext();
        ctx.setContextPath("/");
        Path webappPath = AppPaths.webappDir();
        ctx.setBaseResource(Resource.newResource(webappPath.toUri()));
        ctx.setWelcomeFiles(new String[]{"index.html"});
        ctx.setDescriptor(null);

        File jspTmp = Files.createTempDirectory("jetty-jsp").toFile();
        jspTmp.deleteOnExit();
        ctx.setTempDirectory(jspTmp);
        ctx.setAttribute(ServletContext.TEMPDIR, jspTmp);

        ctx.addServlet(new ServletHolder("api", new ApiServlet()), "/api/*");

        ServletHolder defaultHolder = new ServletHolder("default", DefaultServlet.class);
        defaultHolder.setInitParameter("dirAllowed", "false");
        ctx.addServlet(defaultHolder, "/");

        server.setHandler(ctx);
        server.start();
        System.out.println("=================================================");
        System.out.println("  BUPT TA Recruitment System started!");
        System.out.println("  Webapp: " + webappPath);
        System.out.println("  Data:   " + AppPaths.dataDir());
        System.out.println("  Open http://localhost:" + port + " in your browser");
        System.out.println("  JSP example: http://localhost:" + port + "/jsp/open-positions.jsp");
        System.out.println("=================================================");
        server.join();
    }
}
