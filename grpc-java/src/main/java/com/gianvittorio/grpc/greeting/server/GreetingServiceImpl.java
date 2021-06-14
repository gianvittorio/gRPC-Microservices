package com.gianvittorio.grpc.greeting.server;

import com.proto.greet.*;
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
                stringBuilder.setLength(0);
                // This is when we tant to return a response
            }
        };

        return requestObserver;
    }
}
