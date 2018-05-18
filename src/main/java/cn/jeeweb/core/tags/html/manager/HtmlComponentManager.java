package cn.jeeweb.core.tags.html.manager;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import cn.jeeweb.core.tags.html.builder.HtmlComponentBuilder;
import cn.jeeweb.core.tags.html.builder.NoneHtmlComponentBuilder;
import cn.jeeweb.core.utils.EhcacheUtil;
import cn.jeeweb.core.utils.StringUtils;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * HTML组建管理器
 * 
 * @author 王存见
 * @version 2017-02-09
 */
public class HtmlComponentManager {
	protected HtmlComponentBuilder dynamicStatementBuilder = null;
	protected final static String HTML_COMPONENT_CACHE_NAME = "htmlComponentCache";
	protected final static String HTML_COMPONENT_PRE_NAME_JS = "js_";
	protected final static String HTML_COMPONENT_PRE_NAME_CSS = "css_";
	protected final static String HTML_COMPONENT_PRE_NAME_FRAGMENT = "fragment_";
	protected final static EhcacheUtil ehcacheUtil = new EhcacheUtil(HTML_COMPONENT_CACHE_NAME);
	public final static String COMPONENT_TYPE_JS = "js";
	public final static String COMPONENT_TYPE_CSS = "css";
	public final static String COMPONENT_TYPE_FRAGMENT = "fragment";

	public void setDynamicStatementBuilder(HtmlComponentBuilder dynamicStatementBuilder) {
		this.dynamicStatementBuilder = dynamicStatementBuilder;
	}

	public void init() throws IOException {
		if (dynamicStatementBuilder == null) {
			dynamicStatementBuilder = new NoneHtmlComponentBuilder();
		}
		//把css js 代码片段加载到HtmlComponentManager的变量里
		dynamicStatementBuilder.init();
		//从HtmlComponentManager变量里加载数据
		Map<String, String> cssComponents = dynamicStatementBuilder.getCssComponents();
		Map<String, String> jsComponents = dynamicStatementBuilder.getJsComponents();
		Map<String, String> fragmentComponents = dynamicStatementBuilder.getFragmentComponents();

		//保存css到ehcache
		setCache(cssComponents, HTML_COMPONENT_PRE_NAME_CSS);
		//保存js到ehcache
		setCache(jsComponents, HTML_COMPONENT_PRE_NAME_JS);
		//保存代码片段到ehcache
		setCache(fragmentComponents, HTML_COMPONENT_PRE_NAME_FRAGMENT);
	}

	private void setCache(Map<String, String> cssComponents, String htmlComponentPreName) {
		for (Entry<String, String> entry : cssComponents.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			ehcacheUtil.set(htmlComponentPreName + key, value);
		}
	}

	/**
	 * 
	 * @title: getComponent
	 * @description: 获得组件的html
	 * @param componentType
	 * @param name
	 * @return
	 * @return: String
	 */
	public String getComponent(String componentType, String name) {
		if (componentType.equals(COMPONENT_TYPE_JS)) {
			return ehcacheUtil.getString(HTML_COMPONENT_PRE_NAME_JS + name);
		} else if (componentType.equals(COMPONENT_TYPE_CSS)) {
			return ehcacheUtil.getString(HTML_COMPONENT_PRE_NAME_CSS + name);
		} else if (componentType.equals(COMPONENT_TYPE_FRAGMENT)) {
			return ehcacheUtil.getString(HTML_COMPONENT_PRE_NAME_FRAGMENT + name);
		}
		return "";
	}

	/**
	 *
	 * @param componentType 组件的类型（用来获取缓存中的数据）
	 * @param name 组件的名称（用来获取缓存中的数据）
	 * @param dataMap freemarker的数据
	 * @return 返回freemarker生成的html
	 */
	public String getComponent(String componentType, String name, Map<String, Object> dataMap) {
		try {
			//获取缓存中的html
			String content = getComponent(componentType, name);
			//生成一个临时的名字
			String tempname = StringUtils.hashKeyForDisk(content);
			//初始化freemarker
			Configuration configuration = new Configuration();
			configuration.setNumberFormat("#");
			StringTemplateLoader stringLoader = new StringTemplateLoader();
			stringLoader.putTemplate(tempname, content);

			@SuppressWarnings("deprecation")
			Template template = new Template(tempname, new StringReader(content));
			StringWriter stringWriter = new StringWriter();
			template.process(dataMap, stringWriter);

			configuration.setTemplateLoader(stringLoader);
			content = stringWriter.toString();

			return content;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getJsComponent(String name) {
		return getComponent(COMPONENT_TYPE_JS, name);
	}

	public String getJsComponent(String name, Map<String, Object> dataMap) {
		return getComponent(COMPONENT_TYPE_JS, name, dataMap);
	}

	public String getCssComponent(String name) {
		return getComponent(COMPONENT_TYPE_CSS, name);
	}

	public String getCssComponent(String name, Map<String, Object> dataMap) {
		return getComponent(COMPONENT_TYPE_CSS, name, dataMap);
	}

	public String getFragmentComponent(String name) {
		return getComponent(COMPONENT_TYPE_FRAGMENT, name);
	}

	public String getFragmentComponent(String name, Map<String, Object> dataMap) {
		////不明白为什么还要init一次，项目启动时已经Init了，先注释掉
//		try {
//			init();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return getComponent(COMPONENT_TYPE_FRAGMENT, name, dataMap);
	}

	/*
	 * 清除换成
	 */
	public static void clear() {
		ehcacheUtil.remove(HTML_COMPONENT_CACHE_NAME);
	}

}
