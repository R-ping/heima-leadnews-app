package com.heima.app.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AppJwtUtil {

    // TOKEN的有效期一天（S）
    private static final int TOKEN_TIME_OUT = 3_600;
    //    private static final int TOKEN_TIME_OUT = 120;
    // 加密KEY
    private static final String TOKEN_ENCRY_KEY = "MDk4ZjZiY2Q0NjIxZDM3M2NMDk4ZjZiY2Q0NjIxZDM3M2NhZGU0ZTgzMjYyN2I0ZjYhZGU0ZTgzMjYyN2I0ZjY";
    // 最小刷新间隔(S)
    private static final int REFRESH_TIME = 600;
    // 生产ID
    public static String getToken(Long id) {
        Map<String, Object> claimMaps = new HashMap<>();
        claimMaps.put("id", id);
        long currentTime = System.currentTimeMillis();
        return getObjJwt(currentTime, claimMaps);
    }

    private static String getObjJwt(long currentTime, Map<String, Object> claimMaps) {
        return Jwts.builder()
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(new Date(currentTime))  //签发时间
            .setSubject("system")  //说明
            .setIssuer("heima") //签发者信息
            .setAudience("app")  //接收用户
            .compressWith(CompressionCodecs.GZIP)  //数据压缩方式
            .signWith(SignatureAlgorithm.HS512, generalKey()) //加密方式
            .setExpiration(new Date(currentTime + TOKEN_TIME_OUT * 1000))  //过期时间戳
            .addClaims(claimMaps) //cla信息
            .compact();
    }

    /**
     * 生成JWT access token（带更多claims，用于双token体系）
     * @param id       用户ID
     * @param extraClaims 额外claims（如 name, phone 等）
     * @return JWT token
     */
    public static String getToken(Long id, Map<String, Object> extraClaims) {
        Map<String, Object> claimMaps = new HashMap<>();
        claimMaps.put("id", id);
        if (extraClaims != null) {
            claimMaps.putAll(extraClaims);
        }
        long currentTime = System.currentTimeMillis();
        return getObjJwt(currentTime, claimMaps);
    }
    public static String getToken(Map<String, String> extraClaims) {
        Map<String, Object> claimMaps = new HashMap<>();
        if (extraClaims != null) {
            claimMaps.putAll(extraClaims);
        }
        long currentTime = System.currentTimeMillis();
        return getObjJwt(currentTime, claimMaps);
    }
    public static boolean verifyToken(Claims claims) {
        if (claims == null) {
            return false;
        }
        // 校验过期
        return claims.getExpiration().after(new Date());
    }
    /**
     * 获取token中的claims信息
     */
    private static Jws<Claims> getJws(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(generalKey())
            .build()
            .parseClaimsJws(token);
    }

    /**
     * 获取payload body信息
     */
    public static Claims getClaimsBody(String token) throws ExpiredJwtException {
        return getJws(token).getBody();
    }

    /**
     * 获取hearder body信息
     */
    public static JwsHeader getHeaderBody(String token) {
        return getJws(token).getHeader();
    }

    /**
     * 是否过期
     *
     * @return -1：有效，0：有效，1：过期，2：过期
     */
    // 弃用，没必要用时间窗口形式做提前告警
    public static int verifyToken2(Claims claims) {
        if (claims == null) {
            return 1;
        }
        boolean before = claims.getExpiration().before(new Date());
        if (!before) {
            long interval = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (interval < REFRESH_TIME * 1000) {
                log.info("还有{}ms时间过期", interval);
                return -1;
            } else {
                return 0;
            }
        }
        return 1;
    }


    /**
     * 由字符串生成加密key
     */
    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.getEncoder().encode(TOKEN_ENCRY_KEY.getBytes());
        return new SecretKeySpec(encodedKey, SignatureAlgorithm.HS512.getJcaName()); // HmacSHA512
    }

    public static void main(String[] args) {
       /* Map map = new HashMap();
        map.put("id","11");*/
//        System.out.println(AppJwtUtil.getToken(1102L));
//        Jws<Claims> jws = AppJwtUtil.getJws(
//            "eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAADWLQQqEMAwA_5KzhURNt_qb1KZYQSi0wi6Lf9942NsMw3zh6AVW2DYmDGl2WabkZgreCaM6VXzhFBfJMcMARTqsxIG9Z888QLui3e3Tup5Pb81013KKmVzJTGo11nf9n8v4nMUaEY73DzTabjmDAAAA.4SuqQ42IGqCgBai6qd4RaVpVxTlZIWC826QA9kLvt9d-yVUw82gU47HDaSfOzgAcloZedYNNpUcd18Ne8vvjQA");
//        Claims claims = jws.getBody();
//        System.out.println(claims.get("id"));

    }

}
