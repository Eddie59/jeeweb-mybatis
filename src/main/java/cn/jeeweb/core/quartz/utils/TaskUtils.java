package cn.jeeweb.core.quartz.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.jeeweb.core.quartz.data.ScheduleJob;
import cn.jeeweb.core.utils.SpringContextHolder;

public class TaskUtils {
    public final static Logger log = Logger.getLogger(TaskUtils.class);

    /**
     * 通过反射调用scheduleJob中定义的方法，调用具体的方法，是在QuartzJobFactory、QuartzJobFactoryDisallowConcurrentExecution中通过反射调用
     *
     * @param scheduleJob
     */
    public static void invokMethod(ScheduleJob scheduleJob) {
        Object object = null;
        Class<?> clazz = null;

        if (StringUtils.isNotBlank(scheduleJob.getSpringBean())) {
            //根据配置的Spring Bean的名字
            object = SpringContextHolder.getBean(scheduleJob.getSpringBean());
        } else if (StringUtils.isNotBlank(scheduleJob.getBeanClass())) {
            try {
                //根据配置的类的名字
                clazz = Class.forName(scheduleJob.getBeanClass());
                object = clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (object == null) {
            log.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------未启动成功，请检查是否配置正确！！！");
            return;
        }

        //无论配置的是Bean Name还是Class Name，都获取Class类对象
        clazz = object.getClass();

        Method method = null;
        try {
            //根据配置的方法的名称
            method = clazz.getDeclaredMethod(scheduleJob.getMethodName());
        } catch (NoSuchMethodException e) {
            log.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------未启动成功，方法名设置错误！！！");
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        //调用方法
        if (method != null) {
            try {
                method.invoke(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
