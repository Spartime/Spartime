package com.sparta.spartime.service;

import com.sparta.spartime.dto.request.CommentRequestDto;
import com.sparta.spartime.dto.response.CommentResponseDto;
import com.sparta.spartime.entity.Comment;
import com.sparta.spartime.entity.Post;
import com.sparta.spartime.entity.User;
import com.sparta.spartime.exception.BusinessException;
import com.sparta.spartime.exception.ErrorCode;
import com.sparta.spartime.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PostService postService;
    private final CommentRepository commentRepository;

    public CommentResponseDto createComment(User user, Long postId, CommentRequestDto requestDto) {
        Post post = postService.getPost(postId);

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .contents(requestDto.getContents())
                .build();

        return new CommentResponseDto(commentRepository.save(comment));
    }

    public List<CommentResponseDto> getComments(Long postId) {
        List<Comment> comments = commentRepository.findAllByPost_Id(postId).orElseThrow(
                () -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND)
        );
        return comments.stream()
                .map(CommentResponseDto::new)
                .toList();
    }

    @Transactional
    public CommentResponseDto updateComment(User user, Long postId, Long commentId, CommentRequestDto requestDto) {
        Comment comment = findComment(postId, commentId);
        checkUser(user.getId(), comment.getUser().getId());
        comment.updateComment(requestDto.getContents());
        return new CommentResponseDto(comment);
    }

    public void deleteComment(User user, Long postId, Long commentId) {
        Comment comment = findComment(postId, commentId);
        checkUser(user.getId(), comment.getUser().getId());
        commentRepository.delete(comment);
    }

    public Comment findComment(Long postId, Long commentId) {
        return commentRepository.findByIdAndPost_Id(commentId, postId).orElseThrow(
                () -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND)
        );
    }

    private void checkUser(Long inputUserId, Long userId) {
        if (!Objects.equals(inputUserId, userId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_USER);
        }
    }
}
