package com.example.hiring.post.service;

import com.example.hiring.post.entity.Post;
import com.example.hiring.post.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public Post updatePost(String id, Post post) {
        Optional<Post> existingPost = postRepository.findById(id);
        if (existingPost.isPresent()) {
            Post updatedPost = existingPost.get();
            updatedPost.setTitle(post.getTitle());
            updatedPost.setDescription(post.getDescription());
            updatedPost.setContractType(post.getContractType());
            updatedPost.setQuizId(post.getQuizId());
            return postRepository.save(updatedPost);
        } else {
            throw new RuntimeException("Post not found with id: " + id);
        }
    }

    public void deletePost(String id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
        } else {
            throw new RuntimeException("Post not found with id: " + id);
        }
    }
}
