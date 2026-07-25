package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dto.PasswordUpdateDTO;
import com.heima.model.user.dto.PrivacyMessageDTO;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.UserOauth;
import com.heima.model.user.pojos.UserProfile;
import com.heima.model.user.vo.BindingsVO;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.UserOauthMapper;
import com.heima.user.mapper.UserProfileMapper;
import com.heima.user.service.AccountService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private ApUserMapper apUserMapper;

    @Autowired
    private UserOauthMapper userOauthMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public ResponseResult getBindings() {
        ApUser currentUser = AppThreadLocalUtil.getUser();
        if (currentUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Long userId = currentUser.getId().longValue();

        BindingsVO vo = new BindingsVO();

        // 手机号脱敏
        String phone = currentUser.getPhone();
        if (phone != null && phone.length() == 11) {
            vo.setPhone(phone.substring(0, 3) + "****" + phone.substring(7));
        } else if (phone != null) {
            vo.setPhone(phone);
        }

        // 查询 OAuth 绑定
        LambdaQueryWrapper<UserOauth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserOauth::getUserId, userId);
        List<UserOauth> oauthList = userOauthMapper.selectList(wrapper);

        BindingsVO.OAuthBinding wechat = new BindingsVO.OAuthBinding();
        BindingsVO.OAuthBinding weibo = new BindingsVO.OAuthBinding();
        BindingsVO.OAuthBinding github = new BindingsVO.OAuthBinding();

        for (UserOauth oauth : oauthList) {
            BindingsVO.OAuthBinding binding = new BindingsVO.OAuthBinding();
            binding.setBound(true);
            binding.setNickname(oauth.getNickname());
            binding.setAvatar(oauth.getAvatar());
            if (oauth.getProvider() == 1) {
                wechat = binding;
            } else if (oauth.getProvider() == 2) {
                weibo = binding;
            } else if (oauth.getProvider() == 3) {
                github = binding;
            }
        }

        vo.setWechat(wechat);
        vo.setWeibo(weibo);
        vo.setGithub(github);

        return ResponseResult.okResult(vo);
    }

    @Override
    @Transactional
    public ResponseResult updatePassword(PasswordUpdateDTO dto) {
        ApUser currentUser = AppThreadLocalUtil.getUser();
        if (currentUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        if (dto.getOldPassword() == null || dto.getOldPassword().isEmpty()) {
            return ResponseResult.errorResult(503, "旧密码不能为空");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            return ResponseResult.errorResult(503, "新密码至少6位");
        }

        // 校验旧密码
        ApUser user = apUserMapper.selectById(currentUser.getId());
        if (user.getPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return ResponseResult.errorResult(503, "旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        apUserMapper.updateById(user);

        return ResponseResult.okResult();
    }

    @Override
    @Transactional
    public ResponseResult deleteAccount() {
        ApUser currentUser = AppThreadLocalUtil.getUser();
        if (currentUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 软删除：标记 status=0，清除手机号和邮箱
        apUserMapper.update(null,
            new LambdaUpdateWrapper<ApUser>()
                .eq(ApUser::getId, currentUser.getId())
                .set(ApUser::getStatus, false)
                .set(ApUser::getPhone, null)
                .set(ApUser::getEmail, null)
        );

        return ResponseResult.okResult();
    }

    @Override
    @Transactional
    public ResponseResult updatePrivacyMessage(PrivacyMessageDTO dto) {
        ApUser currentUser = AppThreadLocalUtil.getUser();
        if (currentUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Long userId = currentUser.getId().longValue();

        if (dto.getScope() == null || dto.getScope() < 0 || dto.getScope() > 3) {
            return ResponseResult.errorResult(503, "无效的私信权限设置");
        }

        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }
        profile.setPrivacyMessage(dto.getScope());
        profile.setUpdateTime(new Date());

        if (userProfileMapper.selectById(userId) != null) {
            userProfileMapper.updateById(profile);
        } else {
            userProfileMapper.insert(profile);
        }

        return ResponseResult.okResult();
    }
}