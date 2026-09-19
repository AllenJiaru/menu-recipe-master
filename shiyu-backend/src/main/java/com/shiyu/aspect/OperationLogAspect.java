package com.shiyu.aspect;

import com.shiyu.annotation.OperationLog;
import com.shiyu.entity.User;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class OperationLogAspect {

    private static final Map<String, String> ACTION_MAP = new HashMap<>();

    static {
        ACTION_MAP.put("CREATE", "创建");
        ACTION_MAP.put("UPDATE", "更新");
        ACTION_MAP.put("DELETE", "删除");
        ACTION_MAP.put("QUERY", "查询");
        ACTION_MAP.put("LOGIN", "登录");
        ACTION_MAP.put("LOGOUT", "登出");
        ACTION_MAP.put("EXPORT", "导出");
        ACTION_MAP.put("SYNC", "同步");
        ACTION_MAP.put("ACCEPT", "接受");
        ACTION_MAP.put("REJECT", "拒绝");
        ACTION_MAP.put("COMPLETE", "完成");
        ACTION_MAP.put("CANCEL", "取消");
        ACTION_MAP.put("FAVORITE", "收藏");
        ACTION_MAP.put("UPLOAD", "上传");
        ACTION_MAP.put("RESTORE", "恢复");
        ACTION_MAP.put("RESET", "重置");
        ACTION_MAP.put("ASSIGN", "分配");
    }

    @Autowired
    private OperationLogService operationLogService;

    @Around("@annotation(com.shiyu.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        String errorMsg = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            errorMsg = e.getMessage();
            throw e;
        } finally {
            try {
                saveLog(joinPoint, startTime, result, errorMsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long startTime, Object result, String errorMsg) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);

        String action = annotation.action();
        String target = annotation.target();
        String detail = annotation.detail();

        if (action.isEmpty()) {
            String methodName = method.getName();
            if (methodName.startsWith("create") || methodName.startsWith("add") || methodName.startsWith("save")) {
                action = "创建";
            } else if (methodName.startsWith("update") || methodName.startsWith("edit")) {
                action = "更新";
            } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
                action = "删除";
            } else if (methodName.startsWith("get") || methodName.startsWith("list") || methodName.startsWith("find")) {
                action = "查询";
            } else if (methodName.startsWith("batch")) {
                action = "批量操作";
            } else {
                action = "操作";
            }
        } else {
            String cn = ACTION_MAP.get(action);
            if (cn != null) action = cn;
        }

        StringBuilder detailBuilder = new StringBuilder();
        if (detail.isEmpty()) {
            detailBuilder.append(action).append(target);
        } else {
            detailBuilder.append(detail);
        }
        detailBuilder.append(" [").append(method.getName()).append("]");

        if (errorMsg != null) {
            detailBuilder.append(" 失败: ").append(errorMsg.length() > 200 ? errorMsg.substring(0, 200) : errorMsg);
        } else {
            detailBuilder.append(" 成功");
        }

        long duration = System.currentTimeMillis() - startTime;
        detailBuilder.append(" (").append(duration).append("ms)");

        String username = "anonymous";
        Long userId = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
                username = userDetails.getUsername();
                userId = userDetails.getUserId();
            }
        } catch (Exception ignored) {}

        String ip = getClientIp();

        operationLogService.log(username, action, target, userId, detailBuilder.toString(), ip);
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception ignored) {}
        return "unknown";
    }
}
