package org.learning.grpc.greeting.client;

import com.proto.dummy.DummyServiceGrpc;
import com.proto.greet.*;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by vishantshah on 08/09/18.
 */
public class GreetingClient {

    public void run() throws SSLException {
        System.out.println("Creating Stub");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() //Disable SSL for Development
                .build();

        ManagedChannel secureChannel = NettyChannelBuilder.forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
                .build();

        //doDummyServiceCalls(channel);
        //doUnaryCalls(channel);
        //doServerStreamCalls(channel);
        //doClientStreamingCalls(channel);
        //doBiDirectionalStreamingCalls(channel);
        //doUnaryCallsWithDeadlines(channel);
        doUnaryCalls(secureChannel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }




    /**
     * Dummy Service Calls
     * @param channel
     */
    private void doDummyServiceCalls(ManagedChannel channel) {
        //sync client
        DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        //async client
        DummyServiceGrpc.DummyServiceStub asyncClient = DummyServiceGrpc.newStub(channel);

    }

    /**
     * Unary Calls
     * @param channel
     */
    private void doUnaryCalls(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        //Do something with Client
        Greeting greeting = Greeting.newBuilder().setFirstName("Vishant").setLastName("Shah").build();
        GreetRequest greetRequest = GreetRequest.newBuilder().setGreeeting(greeting).build();

        GreetResponse greetResponse = greetClient.greet(greetRequest);
        System.out.println(greetResponse.getResult());
    }

    /**
     * Server Stream Calls
     * @param channel
     */
    private void doServerStreamCalls(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder().setFirstName("Vishant").setLastName("Shah").build();
        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder().setGreeeting(greeting).build();

        // we stream the responses (in blocking manner)
        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });

    }

    /**
     * Client Streaming Calls
     * @param channel
     */
    private void doClientStreamingCalls(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse longGreetResponse) {
                // We get response from server
                System.out.println("Received response from server");
                System.out.println(longGreetResponse.getResult());
                // onNext will be called only one (Client Streaming and server will respond once on multiple request)

            }

            @Override
            public void onError(Throwable throwable) {
                // We get error from server
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                // the server is done sending the data
                // onCompleted() will be called right after the onNext() method
                System.out.println("Server has completed sending us data");
                latch.countDown();  //Reset latch to 0

            }
        });

        System.out.println("Sending Message 1");
        requestObserver.onNext(LongGreetRequest.newBuilder().setGreeting(Greeting.newBuilder().setFirstName("Vishant").setLastName("Shah").build()).build());
        System.out.println("Sending Message 2");
        requestObserver.onNext(LongGreetRequest.newBuilder().setGreeting(Greeting.newBuilder().setFirstName("John").setLastName("Doe").build()).build());
        System.out.println("Sending Message 3");
        requestObserver.onNext(LongGreetRequest.newBuilder().setGreeting(Greeting.newBuilder().setFirstName("Marc").setLastName("Tylor").build()).build());
        System.out.println("Sending Message 4");
        requestObserver.onNext(LongGreetRequest.newBuilder().setGreeting(Greeting.newBuilder().setFirstName("Sachin").setLastName("Tendulkar").build()).build());
        System.out.println("Sending Message 5");
        requestObserver.onNext(LongGreetRequest.newBuilder().setGreeting(Greeting.newBuilder().setFirstName("Sourav").setLastName("Ganguly").build()).build());
        System.out.println("Sending Message 6");
        requestObserver.onNext(LongGreetRequest.newBuilder().setGreeting(Greeting.newBuilder().setFirstName("Rahul").setLastName("Dravid").build()).build());

        //We tell the server client is done with data
        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Bi-Directional Streaming Calls
     * @param channel
     */
    private void doBiDirectionalStreamingCalls(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestObserver = asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse greetEveryoneResponse) {
                System.out.println("Response received from server: " + greetEveryoneResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending something");
                latch.countDown();
            }
        });

        List<String> names = Arrays.asList("Vishant", "John", "Marc", "Stephane");
        names.forEach(name -> {
            System.out.println("Sending: " + name);
            requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                    .setGreeting(Greeting.newBuilder().setFirstName(name).build())
                    .build());
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unary calls with Deadlines
     * @param channel
     */
    public void doUnaryCallsWithDeadlines(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel);

        //first call with 500ms as deadlines
        try {
            System.out.println("First Call");
            GreetWithDeadlinesResponse response = syncClient.withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS))
                    .greetWithDeadlines(GreetWithDeadlinesRequest.newBuilder()
                    .setGreeting(Greeting.newBuilder().setFirstName("Vishant").build())
                    .build());
            System.out.println(response);
        }catch (StatusRuntimeException e) {
            if(e.getStatus().getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
                System.err.println("Deadline has been exceeded, so we don't want the response");
            }else {
                e.printStackTrace();
            }
        }

        //second call with 100ms as deadlines
        try {
            System.out.println("Second Call");
            GreetWithDeadlinesResponse response = syncClient.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadlines(GreetWithDeadlinesRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder().setFirstName("Vishant").build()).build());
            System.out.println(response);
        }catch (StatusRuntimeException e) {
            if(e.getStatus().getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
                System.err.println("Deadline has been exceeded, so we don't want the response");
            }else {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws SSLException {
        System.out.println("Hello I am gRPC client");
        GreetingClient main = new GreetingClient();
        main.run();

    }
}
