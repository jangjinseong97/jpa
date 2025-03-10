package com.green.greengram.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CookieUtils {

    //Req header 에서 내가 원하는 쿠키를 찾는 메소드
    public Cookie getCookie(HttpServletRequest req, String name) {
        // HttpServletRequest 에서 외부의 Q-S 또는 json 등등 형태로 오는 데이터를 받아서 분류해서
        // controller 로 주는 것
        Cookie[] cookies = req.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    //Res header 에서 내가 원하는 쿠키를 담는 메소드
    public void setCookie(HttpServletResponse res, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/api/user/access-token");
        //이 요청으로 들어올 때만 쿠키 값이 넘어올 수 있도록
        cookie.setHttpOnly(true); // 보안 쿠키 설정, 프론트에서 JS로 쿠키값을 얻을수 없어짐
        cookie.setMaxAge(maxAge);
        res.addCookie(cookie);
    }
}
