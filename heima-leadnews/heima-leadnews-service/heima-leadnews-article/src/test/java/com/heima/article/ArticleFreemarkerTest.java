package com.heima.article;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.utils.MarkdownUtils;
import com.heima.file.utils.MinioUtil;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@Disabled("Requires running services")
@SpringBootTest
public class ArticleFreemarkerTest {

    @Autowired
    private Configuration configuration;
    //
//    @Autowired
//    private FileStorageService fileStorageService;
    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Test
    public void createStaticUrlTest() throws Exception {
        //1.获取文章内容
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(
            Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, 1936621867502784514L));
        if (apArticleContent != null && StringUtils.isNotBlank(apArticleContent.getContent())) {
            //2.文章内容通过freemarker生成html文件
            StringWriter out = new StringWriter();
            Template template = configuration.getTemplate("article.ftl");

            String markdown = apArticleContent.getContent();
            String rawHtml = MarkdownUtils.toHtml(markdown);
            Map<String, Object> params = new HashMap<>();
            params.put("title", "测试文章");
            params.put("authorName", "测试作者");
            params.put("htmlContent", MarkdownUtils.injectHeadingAnchors(rawHtml));
            params.put("tocList", MarkdownUtils.extractToc(rawHtml));
            params.put("articleId", apArticleContent.getArticleId());

            template.process(params, out);
            InputStream is = new ByteArrayInputStream(out.toString().getBytes());

            //3.把html文件上传到minio中
            String path = minioUtil.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", is);

            //4.修改ap_article表，保存static_url字段
            ApArticle article = new ApArticle();
            article.setId(apArticleContent.getArticleId());
            article.setStaticUrl(path);
            apArticleMapper.updateById(article);

        }
    }

    @Test
    public void renderMarkdownTemplateTest() throws Exception {
        String markdown = "# 一级标题\n\n正文段落。\n\n## 二级标题\n\n- 列表项 1\n- 列表项 2\n\n### 三级标题\n\n```java\nSystem.out.println(\"hello\");\n```\n";
        String rawHtml = MarkdownUtils.toHtml(markdown);

        StringWriter out = new StringWriter();
        Template template = configuration.getTemplate("article.ftl");
        Map<String, Object> params = new HashMap<>();
        params.put("title", "Markdown 测试文章");
        params.put("authorName", "测试作者");
        params.put("publishTime", new java.util.Date());
        params.put("htmlContent", MarkdownUtils.injectHeadingAnchors(rawHtml));
        params.put("tocList", MarkdownUtils.extractToc(rawHtml));
        params.put("articleId", 1L);

        template.process(params, out);
        String html = out.toString();
        System.out.println("生成 HTML 长度：" + html.length());
        org.junit.jupiter.api.Assertions.assertTrue(html.contains("一级标题"));
        org.junit.jupiter.api.Assertions.assertTrue(html.contains("二级标题"));
        org.junit.jupiter.api.Assertions.assertTrue(html.contains("System.out.println"));
    }

    @Test
    public void readContent() {

        long start = System.currentTimeMillis();
        ArrayList<ApArticleContent> list = new ArrayList<>();
        //1.获取文章内容

        ApArticle apArticle = apArticleMapper.selectById(1936339548132884482L);

        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - start));

    }

    @Test
    public void readContentFromRemoteMysql() {

        long start = System.currentTimeMillis();
        ArrayList<ApArticleContent> list = new ArrayList<>();
        //1.获取文章内容
        for (int i = 0; i < 1000; i++) {
            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(
                Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, 1936621867502784514L));
            list.add(apArticleContent);
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - start));

    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void readContentFromRemoteRedis() {
        long start = System.currentTimeMillis();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            String content = redisTemplate.opsForValue().get("content:1936621867502784514");
            list.add(content);
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - start));

    }

}
