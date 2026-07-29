package com.heima.app.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AppJwtUtil {


    // 加密KEY
    private static final String TOKEN_ENCRY_KEY = "MDk4ZjZiY2Q0NjIxZDM3M2NMDk4ZjZiY2Q0NjIxZDM3M2NhZGU0ZTgzMjYyN2I0ZjYhZGU0ZTgzMjYyN2I0ZjY";

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
     * 由字符串生成加密key
     */
    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.getDecoder().decode(TOKEN_ENCRY_KEY);
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
