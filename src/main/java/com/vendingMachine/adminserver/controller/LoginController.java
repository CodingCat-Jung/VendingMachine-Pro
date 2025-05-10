package com.vendingMachine.adminserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // 로그인 페이지 매핑
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // → templates/login.html 렌더링
    }
}
