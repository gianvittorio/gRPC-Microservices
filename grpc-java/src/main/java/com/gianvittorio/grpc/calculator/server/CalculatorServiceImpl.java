package com.gianvittorio.grpc.calculator.server;

import com.proto.calculator.*;
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
}
