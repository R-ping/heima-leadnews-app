package com.heima.content.controller.v1.comment;

import com.heima.content.service.comment.ApCommentService;
import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comment")
public class CommentController {

    @Autowired
    private ApCommentService apCommentService;

    @PostMapping("/list")
    public ResponseResult getCommentList(@RequestBody CommentDto dto) {
        return apCommentService.getCommentList(dto);
    }

    @PostMapping
    public ResponseResult addComment(@RequestBody CommentDto dto) {
        return apCommentService.addComment(dto);
    }

    @PostMapping("/like")
    public ResponseResult likeComment(@RequestBody CommentDto dto) {
        return apCommentService.likeComment(dto);
    }

    @GetMapping("/{id}")
    public ResponseResult getCommentById(@PathVariable Long id) {
        return apCommentService.getCommentById(id);
    }
}