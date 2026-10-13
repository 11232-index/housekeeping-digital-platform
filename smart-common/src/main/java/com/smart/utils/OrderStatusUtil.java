package com.smart.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单状态提示工具
 * <p>
 * 后端的状态校验失败（CAS 影响 0 行）时，原来抛的是
 * 「该订单当前不可取消（仅待接单、已接单、服务中可取消）」这类带括号的说明，
 * 对使用者来说太像技术报错。这里统一换成「这单已经取消了，请勿重复操作」的口吻。
 * <p>
 * 每个状态配一句完整的话而不是拼接模板 —— 直接拼会出现「这单已经已取消了」这种叠字。
 */
public class OrderStatusUtil {

    /** 状态码 → 提示语（按状态逐条写，保证每句都读得通） */
    private static final Map<Integer, String> ALREADY_MESSAGE = new HashMap<>();

    static {
        ALREADY_MESSAGE.put(1, "这单还在待付款，请勿重复操作");
        ALREADY_MESSAGE.put(2, "这单还在待接单，请勿重复操作");
        ALREADY_MESSAGE.put(3, "这单已经接单了，请勿重复操作");
        ALREADY_MESSAGE.put(4, "这单已经服务中了，请勿重复操作");
        ALREADY_MESSAGE.put(5, "这单已经完成了，请勿重复操作");
        ALREADY_MESSAGE.put(6, "这单已经转入待退款了，请勿重复操作");
        ALREADY_MESSAGE.put(7, "这单已经取消了，请勿重复操作");
    }

    private OrderStatusUtil() {
    }

    /**
     * 根据订单当前状态生成统一提示。
     *
     * @param status 订单当前状态码（为 null 或未知时退化成通用提示）
     * @return 给用户看的提示语
     */
    public static String alreadyMessage(Integer status) {
        if (status == null) {
            return "订单状态已变化，请刷新后重试";
        }
        String message = ALREADY_MESSAGE.get(status);
        return message != null ? message : "订单状态已变化，请刷新后重试";
    }
}
