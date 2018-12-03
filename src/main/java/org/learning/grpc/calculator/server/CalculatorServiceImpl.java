package org.learning.grpc.calculator.server;

import com.proto.calculator.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vishantshah on 08/09/18.
 */
public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        SumResponse sumResponse = SumResponse.newBuilder()
                .setResult(request.getFirstNumber() + request.getSecondNumber())
                .build();
        responseObserver.onNext(sumResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
        Long number = request.getNumber();
        Long divisor = 2L;
        while(number > 1) {
            if(number % divisor == 0) {
                number = number / divisor;
                PrimeNumberDecompositionResponse response = PrimeNumberDecompositionResponse.newBuilder().setPrimeFactor(divisor).build();
                responseObserver.onNext(response);
            }else {
                divisor = divisor + 1;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        StreamObserver<ComputeAverageRequest> requestObserver = new StreamObserver<ComputeAverageRequest>() {
            int sum = 0;
            int count = 0;
            @Override
            public void onNext(ComputeAverageRequest computeAverageRequest) {
                //When client sends message
                sum = sum + computeAverageRequest.getNumber();
                count += 1;
            }

            @Override
            public void onError(Throwable throwable) {
                //Client sends error
            }

            @Override
            public void onCompleted() {
                //Client is done with data
                double average = (double)sum/count;
                responseObserver.onNext(ComputeAverageResponse.newBuilder().setAverage(average).build());
                responseObserver.onCompleted();
            }
        };
        return requestObserver;
    }

    @Override
    public StreamObserver<FindMaxRequest> findMax(StreamObserver<FindMaxResponse> responseObserver) {
        StreamObserver<FindMaxRequest> requestObservers = new StreamObserver<FindMaxRequest>() {
            int max = 0;
            @Override
            public void onNext(FindMaxRequest findMaxRequest) {
                int value = findMaxRequest.getNumber();
                if(value > max) {
                    max = value;
                    responseObserver.onNext(FindMaxResponse.newBuilder().setMax(max).build());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                //do nothing as of now
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
        return requestObservers;
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        Integer number = request.getNumber();
        double numberRoot = Math.sqrt(number);
        if(number >= 0) {
            responseObserver.onNext(SquareRootResponse.newBuilder().setNumberRoot(numberRoot).build());
        }else {
            //Construct exception
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("Number is negative.!")
                .augmentDescription("Number Sent: " + number)
                .asRuntimeException());
        }
    }
}
