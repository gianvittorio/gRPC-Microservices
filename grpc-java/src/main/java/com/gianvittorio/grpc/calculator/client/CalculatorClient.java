package com.gianvittorio.grpc.calculator.client;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.PrimeNumberDecompositionRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClient {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50053)
                .usePlaintext()
                .build();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        // Unary
//        SumRequest request = SumRequest.newBuilder()
//                .setFirstNumber(10)
//                .setSecondNumber(25)
//                .build();
//
//        SumResponse response = stub.sum(request);
//
//        System.out.format("%d + %d = %d", request.getFirstNumber(), request.getSecondNumber(), response.getSumResult());

        int number = 567890;
        stub.primeNumberDecomposition(PrimeNumberDecompositionRequest.newBuilder().setNumber(number)
                .build())
                .forEachRemaining(primeNumberDecompositionResponse -> {
                    System.out.println(primeNumberDecompositionResponse.getPrimeFactor());
                });

        channel.shutdown();
    }
}
