package org.learning.grpc.calculator.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Created by vishantshah on 08/09/18.
 */
public class CalculatorServer {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50052)
                .addService(new CalculatorServiceImpl())
                .build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
        }));

        server.awaitTermination();
    }
}
