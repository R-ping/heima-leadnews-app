package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.model.user.pojos.ApUserSocial;
import com.heima.user.config.OAuthProperties;
import com.heima.user.mapper.ApUserSocialMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialAuthServiceImpl 单元测试")
class SocialAuthServiceImplTest {

    @Mock
    private OAuthProperties oAuthProperties;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ApUserSocialMapper apUserSocialMapper;

    @InjectMocks
    private SocialAuthServiceImpl socialAuthService;

    // ==================== getAccessToken2Github ====================

    @Nested
    @DisplayName("getAccessToken2Github 方法测试")
    class GetAccessToken2GithubTests {

        @Test
        @DisplayName("成功获取GitHub access_token")
        void shouldGetAccessTokenSuccessfully() {
            // Arrange
            OAuthProperties.Github github = new OAuthProperties.Github();
            github.setClientId("client-id");
            github.setRedirectUri("http://localhost/callback");
            when(oAuthProperties.getGithub()).thenReturn(github);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("access_token", "ghu_abcdef123456");
            ResponseEntity<Map> responseEntity = ResponseEntity.ok(responseBody);
            when(restTemplate.postForEntity(
                    eq("https://github.com/login/oauth/access_token"),
                    any(HttpEntity.class),
                    eq(Map.class)
            )).thenReturn(responseEntity);

            // Act
            String accessToken = socialAuthService.getAccessToken2Github("code-value");

            // Assert
            assertNotNull(accessToken);
            assertEquals("ghu_abcdef123456", accessToken);
        }

        @Test
        @DisplayName("GitHub返回错误时返回null")
        void shouldReturnNullWhenGithubReturnsError() {
            OAuthProperties.Github github = new OAuthProperties.Github();
            github.setClientId("client-id");
            github.setRedirectUri("http://localhost/callback");
            when(oAuthProperties.getGithub()).thenReturn(github);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("error", "bad_verification_code");
            responseBody.put("error_description", "The code passed is incorrect or expired");
            ResponseEntity<Map> responseEntity = ResponseEntity.ok(responseBody);
            when(restTemplate.postForEntity(
                    eq("https://github.com/login/oauth/access_token"),
                    any(HttpEntity.class),
                    eq(Map.class)
            )).thenReturn(responseEntity);

            String accessToken = socialAuthService.getAccessToken2Github("wrong-code");

            assertNull(accessToken);
        }

        @Test
        @DisplayName("GitHub返回空body时返回null")
        void shouldReturnNullWhenResponseBodyIsNull() {
            OAuthProperties.Github github = new OAuthProperties.Github();
            github.setClientId("client-id");
            github.setRedirectUri("http://localhost/callback");
            when(oAuthProperties.getGithub()).thenReturn(github);

            ResponseEntity<Map> responseEntity = ResponseEntity.ok(null);
            when(restTemplate.postForEntity(
                    eq("https://github.com/login/oauth/access_token"),
                    any(HttpEntity.class),
                    eq(Map.class)
            )).thenReturn(responseEntity);

            String accessToken = socialAuthService.getAccessToken2Github("code");

            assertNull(accessToken);
        }
    }

    // ==================== getStraightUid2Weibo ====================

    @Nested
    @DisplayName("getStraightUid2Weibo 方法测试")
    class GetStraightUid2WeiboTests {

        @Test
        @DisplayName("成功获取微博uid")
        void shouldGetWeiboUidSuccessfully() {
            OAuthProperties.Weibo weibo = new OAuthProperties.Weibo();
            weibo.setClientId("weibo-client-id");
            weibo.setRedirectUri("http://localhost/callback");
            when(oAuthProperties.getWeibo()).thenReturn(weibo);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("access_token", "abc123");
            responseBody.put("uid", "123456789");
            ResponseEntity<Map> responseEntity = ResponseEntity.ok(responseBody);
            when(restTemplate.postForEntity(
                    eq("https://api.weibo.com/oauth2/access_token"),
                    any(HttpEntity.class),
                    eq(Map.class)
            )).thenReturn(responseEntity);

            String uid = socialAuthService.getStraightUid2Weibo("code");

            assertNotNull(uid);
            assertEquals("123456789", uid);
        }

        @Test
        @DisplayName("微博返回错误时返回null")
        void shouldReturnNullWhenWeiboReturnsError() {
            OAuthProperties.Weibo weibo = new OAuthProperties.Weibo();
            weibo.setClientId("weibo-client-id");
            weibo.setRedirectUri("http://localhost/callback");
            when(oAuthProperties.getWeibo()).thenReturn(weibo);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("error_code", 21314);
            responseBody.put("error_description", "invalid code");
            ResponseEntity<Map> responseEntity = ResponseEntity.ok(responseBody);
            when(restTemplate.postForEntity(
                    eq("https://api.weibo.com/oauth2/access_token"),
                    any(HttpEntity.class),
                    eq(Map.class)
            )).thenReturn(responseEntity);

            String uid = socialAuthService.getStraightUid2Weibo("wrong-code");

            assertNull(uid);
        }
    }

    // ==================== getUserInfo ====================

    @Nested
    @DisplayName("getUserInfo 方法测试")
    class GetUserInfoTests {

        @Test
        @DisplayName("成功获取GitHub用户信息")
        void shouldGetUserInfoSuccessfully() {
            Map<String, Object> userInfoMap = new HashMap<>();
            userInfoMap.put("id", 12345);
            userInfoMap.put("login", "octocat");
            userInfoMap.put("name", "The Octocat");
            userInfoMap.put("avatar_url", "https://avatars.githubusercontent.com/u/583231?v=4");

            ResponseEntity<Map> responseEntity = ResponseEntity.ok(userInfoMap);
            when(restTemplate.exchange(
                    eq("https://api.github.com/user"),
                    any(),
                    any(HttpEntity.class),
                    eq(Map.class)
            )).thenReturn(responseEntity);

            Map<String, Object> result = socialAuthService.getUserInfo("access-token");

            assertNotNull(result);
            assertEquals(12345, result.get("id"));
            assertEquals("octocat", result.get("login"));
        }
    }

    // ==================== checkUidBound ====================

    @Nested
    @DisplayName("checkUidBound 方法测试")
    class CheckUidBoundTests {

        @Test
        @DisplayName("uid已绑定返回true")
        void shouldReturnTrueWhenUidIsBound() {
            String uid = "12345";
            String platform = "github";

            ApUserSocial social = new ApUserSocial();
            when(apUserSocialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(social);

            boolean result = socialAuthService.checkUidBound(uid, platform);

            assertTrue(result);
        }

        @Test
        @DisplayName("uid未绑定返回false")
        void shouldReturnFalseWhenUidIsNotBound() {
            String uid = "12345";
            String platform = "github";

            when(apUserSocialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(null);

            boolean result = socialAuthService.checkUidBound(uid, platform);

            assertFalse(result);
        }
    }
}