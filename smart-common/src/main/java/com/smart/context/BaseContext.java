package com.smart.context;

public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static ThreadLocal<String> threadLocalUserType = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

    /** 设置当前用户类型：admin（管理员） / employee（员工） */
    public static void setCurrentUserType(String userType) {
        threadLocalUserType.set(userType);
    }

    /** 获取当前用户类型 */
    public static String getCurrentUserType() {
        return threadLocalUserType.get();
    }

    /** 清除用户类型 */
    public static void removeCurrentUserType() {
        threadLocalUserType.remove();
    }

}
