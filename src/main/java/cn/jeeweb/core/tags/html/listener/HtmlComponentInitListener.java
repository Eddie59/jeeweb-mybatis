package cn.jeeweb.core.tags.html.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import cn.jeeweb.core.tags.html.manager.HtmlComponentManager;
import cn.jeeweb.core.utils.SpringContextHolder;

/**
 * ApplicationListener容器加载完成之后，执行其onApplicationEvent方法，常用来做一些数据初始化工作
 *
 */
public class HtmlComponentInitListener implements ApplicationListener<ContextRefreshedEvent> {

	protected HtmlComponentManager htmlComponentManager = SpringContextHolder.getApplicationContext().getBean(HtmlComponentManager.class);

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			//这里就是初始化HTML组件的入口 ，在容器加载完成后执行
			htmlComponentManager.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}