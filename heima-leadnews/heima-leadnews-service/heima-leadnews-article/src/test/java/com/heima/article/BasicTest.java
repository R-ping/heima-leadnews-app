package com.heima.article;

import cn.hutool.core.util.StrUtil;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.model.article.pojos.ApArticle;
import java.util.Arrays;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires running services")
@SpringBootTest
public class BasicTest {
    @Autowired
    private ApArticleMapper apArticleMapper;
    public String string="[1, 2, 3]";
    @Test
    public void test1() {
        String[] str = {"1", "2", "3"};
        String s = StrUtil.join(",", (Object) str);
        System.out.println(s);
        System.out.println(Arrays.toString(str));
        String[] strings = StrUtil.splitToArray(string, ',');
        System.out.println(Arrays.toString(strings));
        ApArticle apArticle = apArticleMapper.selectById(2079607701202997249L);
        System.out.println(apArticle);

    }

}
