package org.learning.grpc.greeting.server;

import com.proto.greet.*;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

/**
 * Created by vishantshah on 08/09/18.
 */
public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        // extract the field we need
        Greeting greeting = request.getGreeeting();
        String firstName = greeting.getFirstName();

        // create the response
        String result = "Hello, " + firstName;
        GreetResponse greetResponse = GreetResponse.newBuilder()
                .setResult(result)
                .build();

        // send the response
        responseObserver.onNext(greetResponse);

        // complete the rpc
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        // extract the field we need
        Greeting greeting = request.getGreeeting();
        String firstName = greeting.getFirstName();
        try {
            for (int i = 0; i < 10; i++) {
                // create the response
                String result = "Hello," + firstName + ", response no " + i;
                GreetManyTimesResponse greetManyTimesResponse = GreetManyTimesResponse.newBuilder()
                        .setResult(result)
                        .build();
                // send the response
                responseObserver.onNext(greetManyTimesResponse);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // complete the rpc
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {
        StreamObserver<LongGreetRequest> requestObserver = new StreamObserver<LongGreetRequest>() {

            String result = "";

            @Override
            public void onNext(LongGreetRequest longGreetRequest) {
                //Client send a message
                result += "Hello " + longGreetRequest.getGreeting().getFirstName() + "! ";
            }

            @Override
            public void onError(Throwable throwable) {
                //Client send an error
            }

            @Override
            public void onCompleted() {
                //Client says done.
                responseObserver.onNext(LongGreetResponse.newBuilder().setResult(result).build());

                //this is when we want to return the response using responseObserver
                responseObserver.onCompleted();
            }
        };
        return requestObserver;
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        StreamObserver<GreetEveryoneRequest> requestObserver = new StreamObserver<GreetEveryoneRequest>() {
            @Override
            public void onNext(GreetEveryoneRequest greetEveryoneRequest) {
                String result = "Hello, " + greetEveryoneRequest.getGreeting().getFirstName();
                GreetEveryoneResponse greetEveryoneResponse = GreetEveryoneResponse.newBuilder().setResult(result).build();
                responseObserver.onNext(greetEveryoneResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                //Do nothing for now
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
        return requestObserver;
    }

    @Override
    public void greetWithDeadlines(GreetWithDeadlinesRequest request, StreamObserver<GreetWithDeadlinesResponse> responseObserver) {
        Context current = Context.current();
        try {
            for (int i = 0; i < 3; i++) {
                if(!current.isCancelled()) {
                    Thread.sleep(100);
                }else {
                    return;
                }
            }
            String result = "Hello, " + request.getGreeting().getFirstName();
            responseObserver.onNext(GreetWithDeadlinesResponse.newBuilder().setResult(result).build());
        }catch(Exception e) {
            e.printStackTrace();
        }
        responseObserver.onCompleted();
    }
}
