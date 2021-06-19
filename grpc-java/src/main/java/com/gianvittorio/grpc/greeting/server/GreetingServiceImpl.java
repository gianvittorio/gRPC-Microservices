package com.gianvittorio.grpc.greeting.server;

import com.proto.greet.*;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class GreetingServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        // Extract the fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();

        // Create the response
        String result = String.format("Hello %s", firstName);
        GreetResponse response = GreetResponse.newBuilder()
                .setResult(result)
                .build();

        // Send the response
        responseObserver.onNext(response);

        // Complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {

        String firstName = request.getGreeting().getFirstName();

        IntStream.range(0, 10)
                .forEach(i -> {
                    String result = String.format("Hello %s; response number: %d", firstName, i);

                    GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                            .setResult(result)
                            .build();

                    responseObserver.onNext(response);
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {

        StreamObserver<LongGreetRequest> requestObserver = new StreamObserver<LongGreetRequest>() {

            private StringBuilder stringBuilder = new StringBuilder();

            @Override
            public void onNext(LongGreetRequest value) {
                // client sends a message
                stringBuilder.append("Hello ")
                        .append(value.getGreeting().getFirstName())
                        .append('!');
            }

            @Override
            public void onError(Throwable t) {
                // client sends an error
            }

            @Override
            public void onCompleted() {
                // client is done
                responseObserver.onNext(LongGreetResponse.newBuilder().setResult(stringBuilder.toString()).build());

                responseObserver.onCompleted();
                // This is when we tant to return a response
            }
        };

        return requestObserver;
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        StreamObserver<GreetEveryoneRequest> requestStreamObserver = new StreamObserver<GreetEveryoneRequest>() {
            @Override
            public void onNext(GreetEveryoneRequest value) {
                String result = "Hello "
                        .concat(value.getGreeting().getFirstName());

                GreetEveryoneResponse greetEveryoneResponse = GreetEveryoneResponse.newBuilder()
                        .setResult(result)
                        .build();

                responseObserver.onNext(greetEveryoneResponse);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

        return requestStreamObserver;
    }

    @Override
    public void greetWithDeadline(GreetWithDeadlineRequest request, StreamObserver<GreetWithDeadlineResponse> responseObserver) {

        Context current = Context.current();

        try {
            for (int i = 0; i < 3; ++i) {
                if (!current.isCancelled()) {

                    System.out.println("sleep for 100 ms");
                    TimeUnit.MILLISECONDS.sleep(100l);

                    continue;
                }

                return;
            }

            System.out.println("send response");
            responseObserver.onNext(
                    GreetWithDeadlineResponse.newBuilder()
                            .setResult("Hello " + request.getGreeting().getFirstName())
                            .build()
            );

            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
