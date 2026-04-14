package com.tangtang.order.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 *
 * @author tangtang
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    // 模拟数据
    private static final List<Map<String, Object>> ORDER_LIST = new ArrayList<>();

    static {
        Map<String, Object> order1 = new HashMap<>();
        order1.put("id", 1L);
        order1.put("productName", "iPhone 15");
        order1.put("price", 7999.0);
        ORDER_LIST.add(order1);

        Map<String, Object> order2 = new HashMap<>();
        order2.put("id", 2L);
        order2.put("productName", "MacBook Pro");
        order2.put("price", 15999.0);
        ORDER_LIST.add(order2);
    }

    @SaCheckLogin
    @GetMapping("/my")
    public SaResult my() {
        Object userId = StpUtil.getLoginId();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("role", userId.equals(10001L) ? "admin" : "user");
        data.put("message", "分布式 Session 验证成功！");
        return SaResult.ok().setData(data);
    }

    @SaCheckLogin
    @SaCheckPermission("order:list")
    @GetMapping("/list")
    public SaResult list() {
        return SaResult.ok().setData(ORDER_LIST);
    }

    @SaCheckLogin
    @SaCheckPermission("order:create")
    @PostMapping("/create")
    public SaResult create(@RequestParam String product, @RequestParam Double price) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", ORDER_LIST.size() + 1L);
        order.put("productName", product);
        order.put("price", price);
        ORDER_LIST.add(order);
        return SaResult.ok("创建成功").setData(order);
    }
}
