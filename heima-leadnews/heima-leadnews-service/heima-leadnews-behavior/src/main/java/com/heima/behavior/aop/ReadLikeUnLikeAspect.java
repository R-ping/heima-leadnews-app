package com.heima.behavior.aop;

import com.alibaba.fastjson.JSON;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.UpdateArticleMess;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ReadLikeUnLikeAspect {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Pointcut("@annotation(com.heima.behavior.anno.UserBehavior)")
    public void userBehaviorPointcut() {
    }

    @Around("userBehaviorPointcut()")
    public Object readAndLikeUnLike(ProceedingJoinPoint joinPoint) throws Throwable {

        //获取参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        Object obj = args[0];

        if (obj == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if(obj instanceof LikesBehaviorDto){
            LikesBehaviorDto dto = (LikesBehaviorDto) obj;
            UpdateArticleMess updateArticleMess = new UpdateArticleMess();
            updateArticleMess.setType(UpdateArticleMess.UpdateArticleType.LIKES);
            updateArticleMess.setArticleId(dto.getArticleId());
            if(dto.getOperation()==0){
                updateArticleMess.setAdd(1);
            }else{
                updateArticleMess.setAdd(-1);
            }
            kafkaTemplate.send("hot.article.score.topic", JSON.toJSONString(updateArticleMess));
        } else if (obj instanceof ReadBehaviorDto) {
            ReadBehaviorDto dto = (ReadBehaviorDto) obj;
            UpdateArticleMess updateArticleMess = new UpdateArticleMess();
            updateArticleMess.setType(UpdateArticleMess.UpdateArticleType.VIEWS);
            updateArticleMess.setArticleId(dto.getArticleId());
            updateArticleMess.setAdd(1);
            kafkaTemplate.send("hot.article.score.topic", JSON.toJSONString(updateArticleMess));

        }

        Object result = joinPoint.proceed();
        return result;
    }

}
