package com.ftc.ftcli.service;

import org.springframework.stereotype.Service;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 10:31:11
 * @describe
 */
@Service
public class GreetService {
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
