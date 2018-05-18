package cn.jeeweb.modules.sys.service.impl;

import org.springframework.stereotype.Service;
import cn.jeeweb.core.common.service.impl.CommonServiceImpl;
import cn.jeeweb.core.query.data.Page;
import cn.jeeweb.core.query.data.PageImpl;
import cn.jeeweb.core.query.data.PageRequest;
import cn.jeeweb.core.query.data.Pageable;
import cn.jeeweb.core.query.wrapper.EntityWrapper;
import cn.jeeweb.core.utils.IpUtils;
import cn.jeeweb.core.utils.ServletUtils;
import cn.jeeweb.core.utils.StringUtils;
import cn.jeeweb.modules.sys.entity.UserOnline;
import cn.jeeweb.modules.sys.mapper.UserOnlineMapper;
import cn.jeeweb.modules.sys.security.shiro.realm.UserRealm.Principal;
import cn.jeeweb.modules.sys.service.IUserOnlineService;
import cn.jeeweb.modules.sys.utils.UserUtils;

import java.util.Date;
import java.util.List;

@Service("userOnlineService")
public class UserOnlineServiceImpl extends CommonServiceImpl<UserOnlineMapper, UserOnline> implements IUserOnlineService {

	/**
	 * 上线，根据当前登陆者信息设置userOnline并插入数据库
	 *
	 * @param userOnline
	 */
	@Override
	public void online(UserOnline userOnline) {
		//设置userOnline的host/username/userid属性
		if (StringUtils.isEmpty(userOnline.getHost())) {
			String hostIp = IpUtils.getIpAddr(ServletUtils.getRequest());
			userOnline.setHost(hostIp);
		}
		//获取当前登陆者对象
		Principal principal = UserUtils.getPrincipal();
		userOnline.setUsername(principal.getUsername());
		userOnline.setUserId(principal.getId());



		//设置好属性以后，更新或者插入到数据库
		UserOnline oldOnline = selectById(userOnline.getId());
		if (oldOnline != null) {
			insertOrUpdate(userOnline);
		} else {
			insert(userOnline);
		}
	}

	/**
	 * 下线，根据id删除useronline
	 *
	 * @param sid
	 */
	@Override
	public void offline(String sid) {
		UserOnline userOnline = selectById(sid);
		if (userOnline != null) {
			deleteById(userOnline.getId());
		}
	}

	/**
	 * 批量下线,根据ids删除从条useronline
	 *
	 * @param needOfflineIdList
	 */
	@Override
	public void batchOffline(List<String> needOfflineIdList) {
		deleteBatchIds(needOfflineIdList);
	}

	/**
	 * 获取无效的UserOnline
	 *
	 * @return
	 */
	@Override
	public Page<UserOnline> findExpiredUserOnlineList(Date expiredDate, int page, int rows) {
		com.baomidou.mybatisplus.plugins.Page<UserOnline> userOnlinePage = new com.baomidou.mybatisplus.plugins.Page<UserOnline>(page, rows);
		EntityWrapper<UserOnline> wrapper = new EntityWrapper<UserOnline>(UserOnline.class);
		//相当于lastAccessTime<'expiredDate'
		wrapper.lt("lastAccessTime", expiredDate);
		wrapper.orderBy("lastAccessTime");

		List<UserOnline> content = baseMapper.selectUserOnlinePage(userOnlinePage, wrapper);
		Integer total = baseMapper.selectCount(wrapper);
		Pageable pageable = new PageRequest(page, rows);
		return new PageImpl<UserOnline>(content, pageable, total);
	}

}
