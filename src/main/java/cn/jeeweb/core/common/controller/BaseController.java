package cn.jeeweb.core.common.controller;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alibaba.fastjson.JSON;
import cn.jeeweb.core.utils.StringUtils;
import cn.jeeweb.core.utils.convert.DateConvertEditor;
import cn.jeeweb.core.utils.convert.StringConvertEditor;

/**
 * 基础控制器 http://blog.csdn.net/catoop/article/details/51278675 写得不错的表单验证
 * 
 * @author 王存见
 * @date 2016-12-21
 * @version V 1.0
 *
 * spring容器在初始化Controller时，设置每个Controller的视图前缀
 *
 */
public class BaseController {
	/**
	 * 日志对象
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private String viewPrefix;

	/**
	 * spring初始化Controller的bean时，默认使用构造函数，会调用父类的构造函数，
	 * 所以在这里，设置每个Controller对应的视图的前缀
	 */
	protected BaseController() {
		setViewPrefix(defaultViewPrefix());
	}

	/**
	 * 返回JSON字符串
	 * 
	 * @param response
	 * @param object
	 * @return
	 * @return
	 */
	protected void printString(HttpServletResponse response, Object object) {
		printString(response, JSON.toJSONString(object));
	}

	/**
	 * 打印字符串到页面
	 * 
	 * @param response
	 * @param string
	 * @return
	 */
	protected void printString(HttpServletResponse response, String string) {
		try {
			response.reset();
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			response.getWriter().print(string);
		} catch (IOException e) {

		}
	}

	/**
	 * 初始化数据绑定
	 * 
	 * @param binder
	 */
	@InitBinder
	void initBinder(ServletRequestDataBinder binder) {
		// 将所有传递进来的String进行HTML编码，防止XSS攻击
		binder.registerCustomEditor(String.class, new StringConvertEditor());
		// 日期格式
		binder.registerCustomEditor(Date.class, new DateConvertEditor());
	}

	/**
	 * 当前模块 视图的前缀 默认
	 * 1、获取当前类头上的@RequestMapping中的value作为前缀
	 * 2、如果没有就使用当前模型小写的简单类名
	 */
	public void setViewPrefix(String viewPrefix) {
		if (viewPrefix.startsWith("/")) {
			viewPrefix = viewPrefix.substring(1);
		}
		this.viewPrefix = viewPrefix;
	}

	public String getViewPrefix() {
		return viewPrefix;
	}

	/**
	 * 获取视图名称：即prefixViewName + "/" + suffixName
	 *
	 * @return
	 */
	public String display(String suffixName) {
		if (!suffixName.startsWith("/")) {
			suffixName = "/" + suffixName;
		}
		return getViewPrefix().toLowerCase() + suffixName;
	}


	protected String defaultViewPrefix() {
		String currentViewPrefix = "";
		//获取当前Controller上的RequestMapping注解，把视图前缀设置为requestMapping的值
		RequestMapping requestMapping = AnnotationUtils.findAnnotation(getClass(), RequestMapping.class);
		if (requestMapping != null && requestMapping.value().length > 0) {
			currentViewPrefix = requestMapping.value()[0];
			currentViewPrefix = currentViewPrefix.replace("${admin.url.prefix}", "modules");
		}
		//如果当前Controller没有RequestMapping注解，把视图前缀设置为Controller的小写名
		if (StringUtils.isEmpty(currentViewPrefix)) {
			currentViewPrefix = this.getClass().getSimpleName().replace("Controller", "").toLowerCase();
		}
		return currentViewPrefix;
	}

}
