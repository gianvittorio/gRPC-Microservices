package com.gianvittorio.grpc.calculator.server;

import com.proto.calculator.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        SumResponse sumResponse = SumResponse.newBuilder()
                .setSumResult(request.getFirstNumber() + request.getSecondNumber())
                .build();

        responseObserver.onNext(sumResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {

        long number = request.getNumber();
        long divisor = 2l;

        while (number > 1l) {
            if (number % divisor == 0l) {
                number = number / divisor;

                responseObserver.onNext(PrimeNumberDecompositionResponse.newBuilder().setPrimeFactor(divisor).build());
                continue;
            }

            ++divisor;
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        return new StreamObserver<ComputeAverageRequest>() {

            private int sum = 0,
                    count = 0;

            @Override
            public void onNext(ComputeAverageRequest value) {
                sum += value.getNumber();

                ++count;
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                double average = (double) (sum) / count;

                responseObserver.onNext(ComputeAverageResponse.newBuilder().setAverage(average).build());

                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        StreamObserver<FindMaximumRequest> requestStreamObserver = new StreamObserver<FindMaximumRequest>() {

            private int max = Integer.MIN_VALUE;

            @Override
            public void onNext(FindMaximumRequest value) {
                max = Math.max(max, value.getNumber());

                responseObserver.onNext(
                        FindMaximumResponse.newBuilder()
                                .setMaximum(max)
                                .build()
                );
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(
                        FindMaximumResponse.newBuilder()
                                .setMaximum(max)
                                .build()
                );

                responseObserver.onCompleted();
            }
        };

        return requestStreamObserver;
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {

        int number = request.getNumber();
        if (number < 0) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("The number being sent is not positive")
                            .augmentDescription("Number sent: " + number)
                            .asRuntimeException()
            );

            return;
        }

        double numberRoot = Math.sqrt(number);
        responseObserver.onNext(
                SquareRootResponse.newBuilder()
                        .setNumberRoot(numberRoot)
                        .build()
        );

        responseObserver.onCompleted();
    }
}
