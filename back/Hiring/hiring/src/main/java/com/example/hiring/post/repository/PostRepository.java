package com.example.hiring.post.repository;

import com.example.hiring.post.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post, String> {
}
