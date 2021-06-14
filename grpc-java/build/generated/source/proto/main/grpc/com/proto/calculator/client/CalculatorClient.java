package com.proto.calculator.client;

import com.proto.calculator.server.CalculatorServiceGrpc;
import com.proto.calculator.server.SumRequest;
import com.proto.calculator.server.SumResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClient {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50053)
                .usePlaintext()
                .build();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest sumRequest = SumRequest.newBuilder()
                .setFirstNumber(10)
                .setSecondNumber(25)
                .build();

        SumResponse response = stub.sum(sumRequest);

        System.out.format("%d + %d = %d", sumRequest.getFirstNumber(), sumRequest.getSecondNumber(), response.getSumResult());

        channel.shutdown();
    }
}
