package com.cloudcare.web.server;

import com.cloudcare.common.lang.Configs;

public class StartWorkerServer {

    public static void main(String[] args) throws Exception {
        JettyServer webServer = new JettyServer(Configs.getString("worker.server.host"),
                Configs.getInt("worker.server.port"), Configs.getString("worker.server.webContext"), "/");
        WorkerServer server = new WorkerServer(Configs.getString("worker.name"), webServer);
        server.start();
    }
}
