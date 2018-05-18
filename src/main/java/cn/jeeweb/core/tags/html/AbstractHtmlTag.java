package cn.jeeweb.core.tags.html;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import cn.jeeweb.core.tags.html.manager.HtmlComponentManager;
import cn.jeeweb.core.utils.SpringContextHolder;
import cn.jeeweb.core.utils.StringUtils;
import cn.jeeweb.modules.sys.tags.SysFunctions;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 
 * All rights Reserved, Designed By www.jeeweb.cn
 * 
 * @title: ComponentTag.java
 * @package cn.jeeweb.core.tags.html
 * @description: 组建获取标签
 * @author: 王存见
 * @date: 2017年5月13日 上午9:05:06
 * @version V1.0
 * @copyright: 2017 www.jeeweb.cn Inc. All rights reserved.
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractHtmlTag extends BodyTagSupport {

	protected HtmlComponentManager htmlComponentManager = SpringContextHolder.getApplicationContext().getBean(HtmlComponentManager.class);

	private static final String[] SUPPORT_TYPES = { "CSS", "JS" };
	// 组件名称
	private String name = "";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getHtmlComponents() {
		return null;
	}

	/**
	 * 
	 * @title: getSupportTypes
	 * @description: 获取支持的类型
	 * @return
	 * @return: String[]
	 */
	public abstract String[] getSupportTypes();

	@Override
	public int doStartTag() throws JspException {
		return EVAL_PAGE;
	}

	/**
	 *
	 * @return 根据在页面设置的Name和控件支持的类型，获取缓存中的html,
	 * 然后调用parseContent方法，对缓存中的html进行解析替换占位符
	 *
	 */
	@Override
	public int doEndTag() throws JspTagException {
		try {

			//获取pageContext的JspWriter，用来在页面上输出
			JspWriter out = this.pageContext.getOut();
			String content = "";
			String[] components = name.split(",");
			for (String component : components) {
				if (isComponent(component)) {
					//获取控件支持的类型
					String[] types = getSupportTypes();
					if (types == null) {
						types = SUPPORT_TYPES;
					}
					for (String type : types) {
						//使用freemarker解析模板
						String componentContent = getComponentHtml(component.toLowerCase(), type);
						if (!StringUtils.isEmpty(componentContent)) {
							content += componentContent + "\r\n";
						}
					}
				}
			}
			//解析html，替换html中的点位符
			content = parseContent(content);
			//把解析好的html，输出到页面
			out.print(content);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 * @param component 组件名
	 * @param type 类型，如css js fragement等
	 * @return 从缓存中获取html
	 */
	public String getComponentHtml(String component, String type) {
		try {
			String content = "";
			if (type.equals("CSS")) {
				content = htmlComponentManager.getCssComponent(component);
			} else if (type.equals("JS")) {
				content = htmlComponentManager.getJsComponent(component);
			} else if (type.equals("FRAGMENT")) {
				content = htmlComponentManager.getFragmentComponent(component);
			}
			return content;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 *
	 * @param content 待替换的hmtl
	 * @return 返回替换后的html
	 * @throws TemplateException
	 * @throws IOException
	 * 使用Freemarker模板引擎，把存储在缓存里面的html当成Freeemarker的模板，使用properties的数据当成Freeemarker的数据
	 * 把模板替换为Html
	 */
	private String parseContent(String content) throws TemplateException, IOException {
		Map<String, Object> rootMap = new HashMap<String, Object>();

		String ctx = pageContext.getServletContext().getContextPath() + SysFunctions.getAdminUrlPrefix();
		String adminPath = pageContext.getServletContext().getContextPath() + SysFunctions.getAdminUrlPrefix();
		String staticPath = pageContext.getServletContext().getContextPath() + "/static";
		rootMap.put("ctx", ctx);
		rootMap.put("adminPath", adminPath);
		rootMap.put("staticPath", staticPath);

		String tempname = StringUtils.hashKeyForDisk(content);

		//实例化Freemarker的配置类
		Configuration configuration = new Configuration();
		//设定freemarker对数值的格式化
		configuration.setNumberFormat("#");
		//实例化模板
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		stringLoader.putTemplate(tempname, content);

		//使用rootMap作为模板的数据，tempname作为模板名content作为模板，生成html
		@SuppressWarnings("deprecation")
		Template template = new Template(tempname, new StringReader(content));
		StringWriter stringWriter = new StringWriter();
		template.process(rootMap, stringWriter);

		//???
		configuration.setTemplateLoader(stringLoader);

		content = stringWriter.toString();
		return content;
	}

	/**
	 * 
	 * @title: isComponent
	 * @description:是否为组件
	 * @param name
	 * @return
	 * @return: boolean
	 */
	private boolean isComponent(String name) {
		/*
		 * if (StringUtils.isEmpty(name)) { return false; } for (String
		 * component : HTML_COMPONENTS) { if (component.equals(name.trim())) {
		 * return true; } } // 扩展中的 String[] htmlComponents =
		 * getHtmlComponents(); if (htmlComponents != null) { for (String
		 * component : HTML_COMPONENTS) { if (component.equals(name.trim())) {
		 * return true; } } }
		 */

		return true;
	}

}
