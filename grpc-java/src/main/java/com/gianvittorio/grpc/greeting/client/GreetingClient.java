package com.gianvittorio.grpc.greeting.client;

import com.proto.greet.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class GreetingClient implements Runnable {

    private ManagedChannel channel;

    public static void main(String[] args) {
        System.out.println("Hello, I'm a gRPC client");

        GreetingClient greetingClient = new GreetingClient();
        greetingClient.run();
    }

    @Override
    public void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50054)
                .usePlaintext()
                .build();

//        ManagedChannel secureChannel = null;
//        try {
//            secureChannel = NettyChannelBuilder.forAddress("localhost", 50005)
//                    .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build()).build();
//        } catch (SSLException e) {
//            e.printStackTrace();
//        }

        System.out.println("Creating stub");
        doUnaryCall(channel);
//        doServerStreamingCall(channel);
        //doClientStreamingCall(channel);
        //doBiDiStreamingCall(channel);
        //doUnaryCallWithDeadline(channel);

//        doUnaryCall(secureChannel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {

        // Server streaming
        // Created a service client
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
        // We prepare the request
        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Gianvittorio").build())
                .build();

        // Stream the responses (in a blocking manner)
        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });
    }

    private void doServerStreamingCall(ManagedChannel channel) {
        // Unary
        // Created a service client
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // Created a protocol buffer greeting message
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Gianvittorio")
                .setLastName("Castellano")
                .build();

        // do the same for greet request
        GreetRequest request = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        // call rpc and get response back
        GreetResponse response = greetClient.greet(request);

        System.out.println(response.getResult());
    }

    private void doClientStreamingCall(ManagedChannel channel) {

        // Create a async client stub
        final GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestStreamObserver = asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                System.out.println("Received a response from server");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                // we get error from server
            }

            @Override
            public void onCompleted() {
                // server is done sending us data
                System.out.println("server has completed sending us something");

                // onCompleted will be called
                countDownLatch.countDown();
            }
        });

        System.out.println("Sending message 1");
        requestStreamObserver.onNext(
                LongGreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                                .setFirstName("Gianvittorio")
                                .build())
                        .build()
        );

        System.out.println("Sending message 2");
        requestStreamObserver.onNext(
                LongGreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                                .setFirstName("John")
                                .build())
                        .build()
        );

        System.out.println("Sending message 3");
        requestStreamObserver.onNext(
                LongGreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                                .setFirstName("Mark")
                                .build())
                        .build()
        );

        // we tell the server the client is done sending data
        requestStreamObserver.onCompleted();

        try {
            countDownLatch.await(3l, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doBiDiStreamingCall(ManagedChannel channel) {

        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestStreamObserver = asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Response from server:");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");

                latch.countDown();
            }
        });

        Stream.of("Stephane", "Mark", "John", "Patricia")
                .forEach(
                        name -> {
                            System.out.println("Sending " + name);
                            requestStreamObserver.onNext(
                                    GreetEveryoneRequest.newBuilder()
                                            .setGreeting(Greeting.newBuilder().setFirstName(name).build())
                                            .build()
                            );

                            try {
                                TimeUnit.MILLISECONDS.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                );

        requestStreamObserver.onCompleted();

        try {
            latch.await(3l, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void doUnaryCallWithDeadline(ManagedChannel channel) {

        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        // first call (3000 ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 3000ms");
            GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(
                            GreetWithDeadlineRequest.newBuilder()
                                    .setGreeting(Greeting.newBuilder()
                                            .setFirstName("Stephane")
                                            .getDefaultInstanceForType()
                                    )
                                    .build()
                    );

            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has beeen exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }

        // first call (100 ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 500ms");
            GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(
                            GreetWithDeadlineRequest.newBuilder()
                                    .setGreeting(Greeting.newBuilder()
                                            .setFirstName("Stephane")
                                            .getDefaultInstanceForType()
                                    )
                                    .build()
                    );

            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has beeen exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }
    }
}
