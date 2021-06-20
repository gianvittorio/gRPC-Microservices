package com.gianvittorio.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient implements Runnable {

    private ManagedChannel channel;

    @Override
    public void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50061)
                .usePlaintext()
                .build();

        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setAuthorId("Stephane")
                .setTitle("New blog!")
                .setContent("Hello world, this is my first blog!")
                .build();

        CreateBlogResponse createResponse = blogClient.createBlog(
                CreateBlogRequest.newBuilder()
                        .setBlog(blog)
                        .build()
        );

        System.out.println("Received create blog createResponse");
        System.out.println(createResponse.toString());

        String blogId = createResponse.getBlog().getId();
        ReadBlogResponse readBlogResponse = blogClient.readBlog(ReadBlogRequest.newBuilder().setBlogId(blogId).build());

        System.out.println(readBlogResponse);

//        ReadBlogResponse readBlogResponseNotFound = blogClient.readBlog(ReadBlogRequest.newBuilder().setBlogId("60ce9a3cf842b908a190e2b0").build());
//
//        System.out.println(readBlogResponseNotFound);

        Blog newBlog = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("Changed Author")
                .setTitle("Brand New blog!")
                .setContent("Hello world, this is my changed blog!")
                .build();

        System.out.println("Updating blog...");
        UpdateBlogResponse updateBlogResponse = blogClient.updateBlog(
                UpdateBlogRequest.newBuilder()
                .setBlog(newBlog)
                .build());

        System.out.println("Updated blog");
        System.out.println(updateBlogResponse.toString());

        channel.shutdown();
    }

    public static void main(String[] args) {
        new BlogClient()
                .run();
    }
}
