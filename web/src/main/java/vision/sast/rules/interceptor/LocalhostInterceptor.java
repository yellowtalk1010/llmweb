package vision.sast.rules.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import vision.sast.rules.DatabaseIssue;

/***
 * 拦截器
 */
@Slf4j
public class LocalhostInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        log.info("aaaa");

        DatabaseIssue.checkLicense(); //许可是否过期

        String ip = request.getRemoteAddr();
        if (!"127.0.0.1".equals(ip) && !"0:0:0:0:0:0:0:1".equals(ip)) {
            // 限制只能localhost、127.0.0.1访问
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("403 - Only localhost allowed");
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //controller 执行完成了
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //获取一个返回结果，请求结束了
    }

}
