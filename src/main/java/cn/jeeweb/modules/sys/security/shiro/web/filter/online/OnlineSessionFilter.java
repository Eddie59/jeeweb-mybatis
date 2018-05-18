
package cn.jeeweb.modules.sys.security.shiro.web.filter.online;

import cn.jeeweb.core.security.shiro.session.SessionDAO;
import cn.jeeweb.core.utils.StringUtils;
import cn.jeeweb.modules.sys.Constants;
import cn.jeeweb.modules.sys.entity.User;
import cn.jeeweb.modules.sys.security.shiro.ShiroConstants;
import cn.jeeweb.modules.sys.security.shiro.session.mgt.OnlineSession;
import cn.jeeweb.modules.sys.security.shiro.session.mgt.eis.OnlineSessionDAO;

import cn.jeeweb.modules.sys.utils.UserUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.filter.authc.UserFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * 
 * All rights Reserved, Designed By www.jeeweb.cn
 * @title:  OnlineSessionFilter.java   
 * @package cn.jeeweb.modules.sys.security.shiro.web.filter.online   
 * @description:   在线
 * @author: 王存见   
 * @date:   2017年6月26日 下午5:55:19   
 * @version V1.0 
 * @copyright: 2017 www.jeeweb.cn Inc. All rights reserved. 
 *
 */
public class OnlineSessionFilter extends AccessControlFilter {

	/**
	 * 强制退出后重定向的地址
	 */
	private String forceLogoutUrl;

	private SessionDAO sessionDAO;

	public String getForceLogoutUrl() {
		return forceLogoutUrl;
	}

	public void setForceLogoutUrl(String forceLogoutUrl) {
		this.forceLogoutUrl = forceLogoutUrl;
	}

	public void setSessionDAO(SessionDAO sessionDAO) {
		this.sessionDAO = sessionDAO;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws Exception {
		Subject subject = getSubject(request, response);
		//subject.getSession()获取Session,如果没有创建则调用sessionmanager的doCreateSession方法创建一个（这时id为null）（用到了xml配置的sessionManager中的<property name="sessionFactory" ref="onlineSessionFactory"/>）
		//然后利用配置的id生成器生成session id并赋值给创建的session（用到了xml配置的sessionManager中的<property name="sessionDAO" ref="sessionDAO"/>，sessionDAO又使用了sessionIdGenerator）
		//然后把session以 key:session id value:session对象 保存到 配置的ehcache（用到了xml配置的sessionManager中的<property name="cacheManager" ref="shiroCacheManager"/>）
		if (subject == null || subject.getSession() == null) {
			return true;
		}

		//根据session id 先从request中获取session，如果为空从ehcache中获取session对象
		Session session = sessionDAO.readSession(subject.getSession().getId());
		if (session != null && session instanceof OnlineSession) {
			OnlineSession onlineSession = (OnlineSession) session;
			//把onlineSession对象存放到request中
			request.setAttribute(ShiroConstants.ONLINE_SESSION, onlineSession);
			if (StringUtils.isEmpty(onlineSession.getUserId())) {
				User user = UserUtils.getUser();
				if (user != null) {
					onlineSession.setUserId(user.getId());
					onlineSession.setUsername(user.getUsername());
					//标记，缓存的Attribute改变了，SyncOnlineSessionFilter在同步Session到DB时会读这个字段
					onlineSession.markAttributeChanged();
				}
			}
			//如果session是退出状态 返回false 执行下面的onAccessDenied，返回login页面，强制退出用户的实现
			if (onlineSession.getStatus() == OnlineSession.OnlineStatus.force_logout) {
				return false;
			}
		}
		return true;
	}

	/**
	 *  如果当前Session状态为force_logout，调用subject.logout()退出，重定向到login页
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		Subject subject = getSubject(request, response);
		if (subject != null) {
			//退出
			subject.logout();
		}
		//跳转到login页
		saveRequestAndRedirectToLogin(request, response);
		return true;
	}

	@Override
	protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
		WebUtils.issueRedirect(request, response, getForceLogoutUrl());
	}

}
