package cn.jeeweb.modules.common.interceptor;

import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import cn.jeeweb.core.utils.DateUtils;
import cn.jeeweb.modules.sys.utils.LogUtils;

/**
 * 
 * All rights Reserved, Designed By www.jeeweb.cn
 * 
 * @title: LogInterceptor.java
 * @package cn.jeeweb.modules.common.interceptor
 * @description: 访问日志拦截器
 * @author: 王存见
 * @date: 2017年7月11日 下午12:17:54
 * @version V1.0
 * @copyright: 2017 www.jeeweb.cn Inc. All rights reserved.
 *
 */
public class LogInterceptor implements HandlerInterceptor {

	private Boolean openAccessLog = Boolean.FALSE;

	 

	public void setOpenAccessLog(Boolean openAccessLog) {
		this.openAccessLog = openAccessLog;
	}

	private static final ThreadLocal<Long> startTimeThreadLocal = new NamedThreadLocal<Long>("ThreadLocal StartTime");
	/**
	 * 日志对象
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 请求处理开始前执行此方法
	 * @param request
	 * @param response
	 * @param handler
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (logger.isDebugEnabled()) {
			long beginTime = System.currentTimeMillis();
			// 线程绑定变量（该数据只有当前请求的线程可见）
			startTimeThreadLocal.set(beginTime);
			logger.debug("开始计时: {}  URI: {}", new SimpleDateFormat("hh:mm:ss.SSS").format(beginTime), request.getRequestURI());
		}
		return true;
	}

	/**
	 * 请求处理之后，视图渲染之前执行，可以修改modelandview
	 * @param request
	 * @param response
	 * @param handler
	 * @param modelAndView
	 * @throws Exception
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (modelAndView != null) {
			//logger.info("ViewName: " + modelAndView.getViewName());
		}
	}

	/**
	 * 视图渲染之后执行，类似于try-catch-finally中的finally，但仅调用处理器执行链中preHandle返回true的拦截器的afterCompletion
	 * @param request
	 * @param response
	 * @param handler
	 * @param ex
	 * @throws Exception
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		if (openAccessLog) {
			// 保存日志
			LogUtils.saveLog(request, handler, ex, null);
			// 打印JVM信息。
			if (logger.isDebugEnabled()) {
				// 得到线程绑定的局部变量（开始时间）
				long beginTime = startTimeThreadLocal.get();
				// 2、结束时间
				long endTime = System.currentTimeMillis();
				logger.debug("计时结束：{}  耗时：{}  URI: {}  最大内存: {}m  已分配内存: {}m  已分配内存中的剩余空间: {}m  最大可用内存: {}m",
						new SimpleDateFormat("hh:mm:ss.SSS").format(endTime),
						DateUtils.formatDateTime(endTime - beginTime), request.getRequestURI(),
						Runtime.getRuntime().maxMemory() / 1024 / 1024,
						Runtime.getRuntime().totalMemory() / 1024 / 1024,
						Runtime.getRuntime().freeMemory() / 1024 / 1024,
						(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()
								+ Runtime.getRuntime().freeMemory()) / 1024 / 1024);
			}
		}

	}

}
