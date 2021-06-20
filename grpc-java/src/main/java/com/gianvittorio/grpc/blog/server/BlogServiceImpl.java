package com.gianvittorio.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");

    private final MongoDatabase mongoDatabase = mongoClient.getDatabase("mydb");

    private final MongoCollection<Document> collection = mongoDatabase.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
        System.out.println("Received create blog request");
        Blog blog = request.getBlog();

        Document document = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        System.out.println("Inserting blog...");
        collection.insertOne(document);

        String id = document.getObjectId("_id")
                .toString();
        System.out.println("Inserted blog: " + id);

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id))
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
        System.out.println("Received read blog request");
        String blogId = request.getBlogId();

        System.out.println("Searching for a blog");
        Document result = null;
        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result == null) {
            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException()
            );

            return;
        }

        System.out.println("Blog found, sending response");
        Blog blog = getBlog(result);

        responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(blog).build());

        responseObserver.onCompleted();
    }

    private Blog getBlog(Document document) {
        Blog blog = Blog.newBuilder()
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .setId(document.getObjectId("_id").toString())
                .build();

        return blog;
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        System.out.println("Received update blog request");
        String blogId = request.getBlog().getId();

        System.out.println("Searching for a blog, so we can update it");
        Document result = null;
        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(String.format("The blog with the corresponding blogId: %d was not found", blogId))
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(String.format("The blog with the corresponding blogId: %d was not found", blogId))
                            .asRuntimeException()
            );

            return;
        }

        Document replacement = new Document("author_id", blogId)
                .append("title", request.getBlog().getTitle())
                .append("content", request.getBlog().getContent())
                .append("_id", new ObjectId(blogId));

        System.out.println("Replacing blog in database");
        collection.replaceOne(eq("_id", result.getObjectId("_id")), replacement);


        System.out.println("Replaced!");

        responseObserver.onNext(UpdateBlogResponse.newBuilder().setBlog(getBlog(replacement)).build());

        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {

        System.out.println("Received Delete Blog Response");
        String blogId = request.getBlogId();

        DeleteResult result = null;
        try {
            result = collection.deleteOne(eq("_id", new ObjectId(blogId)));
        } catch (Exception e) {

            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result == null || result.getDeletedCount() == 0) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException()
            );

            return;
        }


        System.out.println("Blog was deleted");
        responseObserver.onNext(DeleteBlogResponse.newBuilder().setBlogId(blogId).build());

        responseObserver.onCompleted();
    }

    @Override
    public void listBlog(ListBlogRequest request, StreamObserver<ListBlogResponse> responseObserver) {

        System.out.println("Received List Blog request");

        collection.find()
                .iterator()
                .forEachRemaining(
                        document -> {
                            responseObserver.onNext(
                                    ListBlogResponse.newBuilder()
                                            .setBlog(getBlog(document))
                                            .build()
                            );
                        });

        responseObserver.onCompleted();
    }
}
