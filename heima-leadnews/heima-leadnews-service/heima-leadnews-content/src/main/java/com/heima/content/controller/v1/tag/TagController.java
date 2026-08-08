
package com.heima.content.controller.v1.tag;

import com.heima.content.service.tag.TagService;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping("/list")
    public ResponseResult findList(@RequestParam(required = false) String keyword) {
        return ResponseResult.okResult(tagService.findList(keyword));
    }

    @GetMapping("/by-category")
    public ResponseResult findTagsByCategory(@RequestParam Integer categoryId) {
        return ResponseResult.okResult(tagService.findTagsByCategory(categoryId));
    }
}
