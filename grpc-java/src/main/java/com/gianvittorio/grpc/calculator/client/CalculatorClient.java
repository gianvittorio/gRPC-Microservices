package com.gianvittorio.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class CalculatorClient implements Runnable {

    private ManagedChannel channel;

    @Override
    public void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50053)
                .usePlaintext()
                .build();

        //doUnaryCall(channel);
        //doStreamingServerCall(channel);
        try {
            doStreamingClientCall(channel);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        channel.shutdown();
    }

    public static void main(String[] args) {
        new CalculatorClient()
                .run();
    }

    private void doUnaryCall(ManagedChannel channel) {

        // Unary
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest request = SumRequest.newBuilder()
                .setFirstNumber(10)
                .setSecondNumber(25)
                .build();

        SumResponse response = stub.sum(request);

        System.out.format("%d + %d = %d", request.getFirstNumber(), request.getSecondNumber(), response.getSumResult());
    }

    private void doStreamingServerCall(ManagedChannel channel) {

        // Streaming server

        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        int number = 567890;
        stub.primeNumberDecomposition(PrimeNumberDecompositionRequest.newBuilder().setNumber(number)
                .build())
                .forEachRemaining(primeNumberDecompositionResponse -> {
                    System.out.println(primeNumberDecompositionResponse.getPrimeFactor());
                });
    }


    private void doStreamingClientCall(ManagedChannel channel) throws InterruptedException {

        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<ComputeAverageRequest> computeAverageRequestStreamObserver = asyncClient.computeAverage(new StreamObserver<ComputeAverageResponse>() {
            @Override
            public void onNext(ComputeAverageResponse value) {
                System.out.println("Received a response from the server");
                System.out.println(value.getAverage());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending us data");
                latch.countDown();
            }
        });

        IntStream.range(0, 10_000)
                .forEach(i ->  computeAverageRequestStreamObserver.onNext(ComputeAverageRequest.newBuilder().setNumber(i).build()));

        computeAverageRequestStreamObserver.onCompleted();

        latch.await(3, TimeUnit.SECONDS);
    }
}
