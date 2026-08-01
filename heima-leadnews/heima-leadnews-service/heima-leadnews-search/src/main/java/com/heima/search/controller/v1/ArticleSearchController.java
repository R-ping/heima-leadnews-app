package com.heima.search.controller.v1;


import com.heima.common.annotation.RateLimit;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ArticleSearchService;
import java.io.IOException;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/article/search")
public class ArticleSearchController {

    @Autowired
    private ArticleSearchService articleSearchService;

    @PostMapping("/search")
    @RateLimit(dimension = RateLimit.Dimension.GLOBAL, count = 300, interval = 1, timeUnit = RateLimit.TimeUnit.MINUTES)
    @RateLimit(dimension = RateLimit.Dimension.IP, count = 20, interval = 1, timeUnit = RateLimit.TimeUnit.MINUTES)
    public ResponseResult search(@RequestBody UserSearchDto dto) throws IOException {
        if(dto.getPageSize()== 0){
            dto.setPageSize(10);
        }
        if (dto.getMinBehotTime() == null) {
            dto.setMinBehotTime(new Date());
        }
        return articleSearchService.search(dto);
    }
}
