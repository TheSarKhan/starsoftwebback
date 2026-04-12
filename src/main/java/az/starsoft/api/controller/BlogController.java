package az.starsoft.api.controller;

import az.starsoft.api.entity.BlogPost;
import az.starsoft.api.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BlogController {

    private final BlogPostRepository blogPostRepository;

    @GetMapping("/blog")
    public Page<BlogPost> getPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        return blogPostRepository.findByPublishedTrueOrderByPublishedAtDesc(PageRequest.of(page, size));
    }

    @GetMapping("/blog/{slug}")
    public ResponseEntity<BlogPost> getPostBySlug(@PathVariable String slug) {
        return blogPostRepository.findBySlug(slug)
                .map(post -> {
                    post.setViewCount(post.getViewCount() + 1);
                    blogPostRepository.save(post);
                    return ResponseEntity.ok(post);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/blog")
    public Page<BlogPost> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return blogPostRepository.findAll(PageRequest.of(page, size));
    }

    @PostMapping("/admin/blog")
    @CacheEvict(value = "blogPosts", allEntries = true)
    public BlogPost createPost(@RequestBody BlogPost post) {
        if (post.getPublished() != null && post.getPublished()) {
            post.setPublishedAt(LocalDateTime.now());
        }
        return blogPostRepository.save(post);
    }

    @PutMapping("/admin/blog/{id}")
    @CacheEvict(value = "blogPosts", allEntries = true)
    public ResponseEntity<BlogPost> updatePost(@PathVariable Long id, @RequestBody BlogPost post) {
        return blogPostRepository.findById(id)
                .map(existing -> {
                    post.setId(id);
                    post.setCreatedAt(existing.getCreatedAt());
                    post.setViewCount(existing.getViewCount());
                    if (post.getPublished() && existing.getPublishedAt() == null) {
                        post.setPublishedAt(LocalDateTime.now());
                    }
                    return ResponseEntity.ok(blogPostRepository.save(post));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/blog/{id}")
    @CacheEvict(value = "blogPosts", allEntries = true)
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        blogPostRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
