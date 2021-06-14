package com.gianvittorio.grpc.greeting.client;

import com.proto.greet.GreetManyTimesRequest;
import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.Greeting;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient {

    public static void main(String[] args) {
        System.out.println("Hello, I'm a gRPC client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        System.out.println("Creating stub");
//        DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);

//        DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);

        // Unary
        // Created a service client
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
//
//        // Created a protocol buffer greeting message
//        Greeting greeting = Greeting.newBuilder()
//                .setFirstName("Gianvittorio")
//                .setLastName("Castellano")
//                .build();
//
//        // do the same for greet request
//        GreetRequest request = GreetRequest.newBuilder()
//                .setGreeting(greeting)
//                .build();
//
//        // call rpc and get response back
//        GreetResponse response = greetClient.greet(request);
//        System.out.println(response.getResult());

        // Server streaming
        // We prepare the request
        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Gianvittorio").build())
                .build();

        // Stream the responses (in a blocking manner)
        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });

        // do something
        System.out.println("Shutting down channel");
        channel.shutdown();
    }
}
