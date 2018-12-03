package org.learning.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by vishantshah on 08/09/18.
 */
public class CalculatorClient {
    public static void main(String[] args) {
        CalculatorClient main = new CalculatorClient();
        main.run();


    }

    private void run() {
        Channel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();
        doUnaryCalls(channel);
        //doServerStreamingCalls(channel);
        //doClientStreamingCalls(channel);
        //doBiDirectionalStreamingCalls(channel);
        //doErrorCalls(channel);
    }



    /**
     * Unary Calls
     * @param channel
     */
    private void doUnaryCalls(Channel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub unaryStub = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest sumRequest = SumRequest.newBuilder()
                .setFirstNumber(10)
                .setSecondNumber(5)
                .build();

        SumResponse sumResponse = unaryStub.sum(sumRequest);
        System.out.println(sumRequest.getFirstNumber() + "+" + sumRequest.getSecondNumber() + "=" + sumResponse.getResult());
    }

    /**
     * Server Streaming Calls
     * @param channel
     */
    private void doServerStreamingCalls(Channel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub unaryStub = CalculatorServiceGrpc.newBlockingStub(channel);

        Long number = 567890458495849692L;

        Iterator<PrimeNumberDecompositionResponse> response = unaryStub.primeNumberDecomposition(PrimeNumberDecompositionRequest.newBuilder().setNumber(number).build());
        response.forEachRemaining(primeNumberDecompositionResponse -> {
            System.out.println(primeNumberDecompositionResponse.getPrimeFactor());
        });
    }


    /**
     * Client Streaming Calls
     * @param channel
     */
    private void doClientStreamingCalls(Channel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ComputeAverageRequest> requestObserver = asyncClient.computeAverage(new StreamObserver<ComputeAverageResponse>() {
            @Override
            public void onNext(ComputeAverageResponse computeAverageResponse) {
                //Server sends data
                System.out.println("Average is: " + computeAverageResponse.getAverage());
            }

            @Override
            public void onError(Throwable throwable) {
                //Server sends error
            }

            @Override
            public void onCompleted() {
                //Server says done with something
                latch.countDown();
            }
        });

        for (int i = 0; i < 10000; i++) {
            System.out.println("Sending Message " + i);
            requestObserver.onNext(ComputeAverageRequest.newBuilder().setNumber(i).build());
        }

        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Bi-Directional Streaming Calls
     */
    private void doBiDirectionalStreamingCalls(Channel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<FindMaxRequest> requestObserver = asyncClient.findMax(new StreamObserver<FindMaxResponse>() {
            @Override
            public void onNext(FindMaxResponse findMaxResponse) {
                System.out.println("Got new max number from server : " + findMaxResponse.getMax());
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending messages.!");
                latch.countDown();
            }
        });

        requestObserver.onNext(FindMaxRequest.newBuilder().setNumber(3).build());
        requestObserver.onNext(FindMaxRequest.newBuilder().setNumber(10).build());
        requestObserver.onNext(FindMaxRequest.newBuilder().setNumber(4).build());
        requestObserver.onNext(FindMaxRequest.newBuilder().setNumber(5).build());
        requestObserver.onNext(FindMaxRequest.newBuilder().setNumber(20).build());

        requestObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Error Channel
     * @param channel
     */
    private void doErrorCalls(Channel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub unaryStub = CalculatorServiceGrpc.newBlockingStub(channel);
        int number = -1;
        try {
            unaryStub.squareRoot(SquareRootRequest.newBuilder().setNumber(number).build());
        }catch(StatusRuntimeException e) {
            System.out.println("Got an exception for sqrt: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
