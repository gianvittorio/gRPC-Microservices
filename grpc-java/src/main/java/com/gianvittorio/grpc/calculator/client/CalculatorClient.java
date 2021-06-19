package com.gianvittorio.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class CalculatorClient implements Runnable {

    private ManagedChannel channel;

    @Override
    public void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50056)
                .usePlaintext()
                .build();

        //doUnaryCall(channel);
        //doStreamingServerCall(channel);
//        try {
//            doStreamingClientCall(channel);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        doStreamingBiDiCall(channel);

        doErrorCall(channel);

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
                .forEach(i -> computeAverageRequestStreamObserver.onNext(ComputeAverageRequest.newBuilder().setNumber(i).build()));

        computeAverageRequestStreamObserver.onCompleted();

        latch.await(3, TimeUnit.SECONDS);
    }

    private void doStreamingBiDiCall(ManagedChannel channel) {

        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<FindMaximumRequest> requestStreamObserver = asyncClient.findMaximum(new StreamObserver<FindMaximumResponse>() {

            @Override
            public void onNext(FindMaximumResponse value) {
                System.out.println("Got new maximum from server:");
                System.out.println(value.getMaximum());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending messages");
                latch.countDown();
            }
        });

        final Random random = new Random();
        IntStream.generate(() -> random.nextInt(100))
                .limit(20)
                .forEach(number -> {
                    System.out.println("Sending number: " + number);

                    requestStreamObserver.onNext(FindMaximumRequest
                            .newBuilder()
                            .setNumber(number)
                            .build()
                    );

                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        requestStreamObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doErrorCall(ManagedChannel channel) {

        CalculatorServiceGrpc.CalculatorServiceBlockingStub blockingStub = CalculatorServiceGrpc.newBlockingStub(channel);

        int number = -1;

        try {
            blockingStub.squareRoot(SquareRootRequest.newBuilder().setNumber(number).build());
        } catch (StatusRuntimeException e) {
            System.out.println("Got an exception for square root!");
            e.printStackTrace();
        }

    }
}
