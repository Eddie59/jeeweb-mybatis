package cn.jeeweb.core.common.form;

import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class BaseMethodArgumentResolver implements HandlerMethodArgumentResolver {

	/**
	 *
	 * @param namePrefix 请求参数的前缀
	 * @param request 当前请求对象
	 * @param subPrefix 是否有分割符
	 * @return 过滤请求的所有参数，如果参数有前缀（查询参数），把参数前缀去掉；所有参数存放到Map中返回
	 */
	protected Map<String, String[]> getPrefixParameterMap(String namePrefix, NativeWebRequest request,
			boolean subPrefix) {
		Map<String, String[]> result = new HashMap<String, String[]>();

		Map<String, String> variables = getUriTemplateVariables(request);

		int namePrefixLength = namePrefix.length();
		for (String name : variables.keySet()) {
			// && !namePrefix.endsWith(name)
			if (name.startsWith(namePrefix)) {
				// page.pn 则截取 pn
				if (subPrefix) {
					if (name.length() <= namePrefix.length()) {
						continue;
					}
					char ch = name.charAt(namePrefix.length());
					// 如果下一个字符不是 数字 . _ 则不可能是查询 只是前缀类似
					if (illegalChar(ch)) {
						continue;
					}
					result.put(name.substring(namePrefixLength + 1), new String[] { variables.get(name) });
				} else {
					result.put(name, new String[] { variables.get(name) });
				}
			}
		}

		Iterator<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasNext()) {
			String name = parameterNames.next();
			// && !namePrefix.endsWith(name)
			if (name.startsWith(namePrefix)) {
				// page.pn 则截取 pn
				if (subPrefix) {
					// 这个写入之后，导致无法分页
					if (name.length() <= namePrefix.length()) {
						continue;
					}
					char ch = name.charAt(namePrefix.length());
					// 如果下一个字符不是 数字 . _ 则不可能是查询 只是前缀类似
					if (illegalChar(ch)) {
						continue;
					}
					result.put(name.substring(namePrefixLength + 1), request.getParameterValues(name));
				} else {
					result.put(name, request.getParameterValues(name));
				}
			}
		}

		return result;
	}

	private boolean illegalChar(char ch) {
		return ch != '.' && ch != '_' && !(ch >= '0' && ch <= '9');
	}


/**
 *
 * @param request 请求对象
 * @return 如果请求中有路径参数，获取路径参数并返回
 */
@SuppressWarnings("unchecked")
	public final Map<String, String> getUriTemplateVariables(NativeWebRequest request) {
		Map<String, String> variables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
		if (variables == null) {
			return Collections.<String, String>emptyMap();
		}
		return variables;
	}

}
