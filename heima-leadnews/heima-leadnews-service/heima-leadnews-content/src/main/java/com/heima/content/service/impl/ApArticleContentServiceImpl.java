package com.heima.content.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.content.mapper.ApArticleContentMapper;
import com.heima.content.service.ApArticleContentService;
import com.heima.model.article.pojos.ApArticleContent;
import org.springframework.stereotype.Service;

@Service
public class ApArticleContentServiceImpl extends ServiceImpl<ApArticleContentMapper, ApArticleContent> implements
    ApArticleContentService {

}
