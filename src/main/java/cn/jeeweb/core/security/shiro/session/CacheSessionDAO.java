package cn.jeeweb.core.security.shiro.session;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Sets;

import cn.jeeweb.core.utils.DateUtils;
import cn.jeeweb.core.utils.JeewebPropertiesUtil;
import cn.jeeweb.core.utils.ServletUtils;
import cn.jeeweb.core.utils.StringUtils;

/**
 * 自定义WEB会话管理类
 * 
 * @author 王存见
 * @version 2017-02-23
 */
public class CacheSessionDAO extends EnterpriseCacheSessionDAO implements SessionDAO {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public CacheSessionDAO() {
		super();
	}

	/**
	 * 每次请求shiro都会更新最后访问时间（LastAccessTime字段），导致调用此方法，由于使用了redis缓存，
	 * 那么如果页面的静态资源很多会导致短时间内大量更新redis缓存
	 * 但是这是不需要的，只有在访问controller请求时才需要更新session
	 */
	@Override
	protected void doUpdate(Session session) {
		if (session == null || session.getId() == null) {
			return;
		}

		HttpServletRequest request = ServletUtils.getRequest();
		if (request != null) {
			String uri = request.getServletPath();
			// 如果是静态文件，则不更新SESSION
			if (ServletUtils.isStaticFile(uri)) {
				return;
			}
			// 如果是视图文件，则不更新SESSION
			if (StringUtils.startsWith(uri, JeewebPropertiesUtil.getConfig("web.view.prefix"))
					&& StringUtils.endsWith(uri, JeewebPropertiesUtil.getConfig("web.view.suffix"))) {
				return;
			}
			//页面中updateSession参数为false时不更新SESSION
			String updateSession = request.getParameter("updateSession");
			if (Boolean.FALSE.equals(Boolean.parseBoolean(updateSession))) {
				return;
			}
		}
		//每次请求，调用CachingSessionDao类去更新ehcache中的session
		super.doUpdate(session);
		logger.debug("update {} {}", session.getId(), request != null ? request.getRequestURI() : "");
	}

	@Override
	protected void doDelete(Session session) {
		if (session == null || session.getId() == null) {
			return;
		}

		super.doDelete(session);
		logger.debug("delete {} ", session.getId());
	}

	/**
	 * 	如DefaultSessionManager在创建完session后会调用该方法；
	 * 	这方法的功能是保存到ehcache/文件系统/NoSQL数据库，即可以实现会话的持久化，返回会话ID； 
	 * 	这里调用super.doCreate(Session),生成个session id，返回session id，给session对象设置sessionid
	 *
	 * @param session
	 * @return
	 */
	@Override
	protected Serializable doCreate(Session session) {
		HttpServletRequest request = ServletUtils.getRequest();
		if (request != null) {
			String uri = request.getServletPath();
			// 如果是静态文件，则不创建SESSION
			if (ServletUtils.isStaticFile(uri)) {
				return null;
			}
		}
		//重点，生成id，把session对象保存到ehcache
		super.doCreate(session);
		logger.debug("doCreate {} {}", session, request != null ? request.getRequestURI() : "");
		return session.getId();
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		return super.doReadSession(sessionId);
	}

	@Override
	public Session readSession(Serializable sessionId) throws UnknownSessionException {
		try {
			Session s = null;
			HttpServletRequest request = ServletUtils.getRequest();
			if (request != null) {
				String uri = request.getServletPath();
				// 如果是静态文件，则不获取SESSION
				if (ServletUtils.isStaticFile(uri)) {
					return null;
				}
				//先从request对象中取session
				s = (Session) request.getAttribute("session_" + sessionId);
			}

//			如果request对象中有相应的session，直接使用，如果没有，从ehcache或者redis中取
			if (s != null) {
				return s;
			}

//			如果request中没有对应的session，从ehcache中取出，并保存到request中，这样避免每次访问ehcache(如果是Redis就会多次访问Redis)
			Session session = super.readSession(sessionId);
			logger.debug("readSession {} {}", sessionId, request != null ? request.getRequestURI() : "");
			if (request != null && session != null) {
				//把Session保存到request的Attribute中
				request.setAttribute("session_" + sessionId, session);
			}

			return session;
		} catch (UnknownSessionException e) {
			return null;
		}
	}

	/**
	 * 获取活动会话
	 * 
	 * @param includeLeave
	 *            是否包括离线（最后访问时间大于3分钟为离线会话）
	 * @return
	 */
	@Override
	public Collection<Session> getActiveSessions(boolean includeLeave) {
		return getActiveSessions(includeLeave, null, null);
	}

	/**
	 * 获取活动会话
	 * 
	 * @param includeLeave
	 *            是否包括离线（最后访问时间大于3分钟为离线会话）
	 * @param principal
	 *            根据登录者对象获取活动会话
	 * @param filterSession
	 *            不为空，则过滤掉（不包含）这个会话。
	 * @return
	 */
	@Override
	public Collection<Session> getActiveSessions(boolean includeLeave, Object principal, Session filterSession) {
		// 如果包括离线，并无登录者条件。
		if (includeLeave && principal == null) {
			return getActiveSessions();
		}
		Set<Session> sessions = Sets.newHashSet();
		for (Session session : getActiveSessions()) {
			boolean isActiveSession = false;
			// 不包括离线并符合最后访问时间小于等于3分钟条件。
			if (includeLeave || DateUtils.pastMinutes(session.getLastAccessTime()) <= 3) {
				isActiveSession = true;
			}
			// 符合登陆者条件。
			if (principal != null) {
				PrincipalCollection pc = (PrincipalCollection) session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
				if (principal.toString().equals(pc != null ? pc.getPrimaryPrincipal().toString() : StringUtils.EMPTY)) {
					isActiveSession = true;
				}
			}
			// 过滤掉的SESSION
			if (filterSession != null && filterSession.getId().equals(session.getId())) {
				isActiveSession = false;
			}
			if (isActiveSession) {
				sessions.add(session);
			}
		}
		return sessions;
	}

}
