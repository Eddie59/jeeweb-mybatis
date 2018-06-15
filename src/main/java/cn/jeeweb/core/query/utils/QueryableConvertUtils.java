
package cn.jeeweb.core.query.utils;

import com.google.common.collect.Lists;

import cn.jeeweb.core.query.data.Condition;
import cn.jeeweb.core.query.data.Condition.Filter;
import cn.jeeweb.core.query.data.Condition.Operator;
import cn.jeeweb.core.query.data.Queryable;
import cn.jeeweb.core.query.exception.QueryException;
import cn.jeeweb.core.utils.SpringContextHolder;
import cn.jeeweb.core.utils.convert.DateConvertEditor;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.CollectionUtils;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@SuppressWarnings({ "unchecked", "rawtypes" })
public final class QueryableConvertUtils {

	//ConversionService是作用就是，里面可以注册多个Converter用来类型转换
	//精准的类型转换器，可以把任意类型转化为任意类型，和ProperyEditor区别，ProperyEditor只能由String到其它类型
	private static volatile ConversionService conversionService;

	//spring-mvc.xml文件里配置org.springframework.beans.factory.config.MethodInvokingFactoryBean类
	//调用此方法，参数为org.springframework.format.support.FormattingConversionServiceFactoryBean
	//所以，这里的conversionService就是FormattingConversionServiceFactoryBean
	public static void setConversionService(ConversionService conversionService) {
		QueryableConvertUtils.conversionService = conversionService;
	}

	public static ConversionService getConversionService() {
		if (conversionService == null) {
			synchronized (QueryableConvertUtils.class) {
				if (conversionService == null) {
					try {
						conversionService = SpringContextHolder.getBean(ConversionService.class);
					} catch (Exception e) {
						throw new QueryException(
								"conversionService is null, " + "query param convert must use conversionService. "
										+ "please see [com.sishuok.es.common.entity.query.utils."
										+ "QueryableConvertUtils#setConversionService]");
					}
				}
			}
		}
		return conversionService;
	}

	/**
	 * @param query
	 *            查询条件
	 * @param entityClass
	 *            实体类型
	 * @param <T>
	 */
	public static <T> void convertQueryValueToEntityValue(final Queryable query, final Class<T> entityClass) {

		if (query.isConverted()) {
			return;
		}

		//Condition implements Iterable<cn.jeeweb.core.query.data.Condition.Filter>
		Condition condition = query.getCondition();
		//BeanWrapperImpl是进行bean属性的操作
		BeanWrapperImpl beanWrapper = new BeanWrapperImpl(entityClass);
		//对于null值，是否自动创建新对象
		beanWrapper.setAutoGrowNestedPaths(true);
		//类型转换器，xml文件注入来的org.springframework.format.support.FormattingConversionServiceFactoryBean
		beanWrapper.setConversionService(getConversionService());
		//向beanWrapper中注册自定义的属性编辑器，如果属性里有Date类型的属性，会按自己的方法对日期进行格式化
		beanWrapper.registerCustomEditor(Date.class, new DateConvertEditor());
		if (condition != null) {
			//遍历每个Filter，使用每个filter对entityClass的属性设置值，然后把新值（成功时为原来的值，不成功为默认的值）设置到filter对象
			for (Filter filter : condition) {
				convert(beanWrapper, filter);
			}
		}

	}

	/**
	 *
	 * @param beanWrapper
	 * @param filter
	 * 使用BeanWrapperImpl给entityClass的Bean的属性设值
	 */
	private static void convert(BeanWrapperImpl beanWrapper, Filter filter) {
		String property = filter.getProperty();

		// 自定义的也不转换
		if (filter.getOperator() == Operator.custom) {
			return;
		}

		// 一元运算符不需要计算
		if (filter.isUnaryFilter()) {
			return;
		}

		Object value = filter.getValue();

		Object newValue = null;
		boolean isCollection = value instanceof Collection;
		boolean isArray = value != null && value.getClass().isArray();
		if (isCollection || isArray) {
			List<Object> list = Lists.newArrayList();
			if (isCollection) {
				list.addAll((Collection) value);
			} else {
				list = Lists.newArrayList(CollectionUtils.arrayToList(value));
			}
			int length = list.size();
			//对于list中的每一个值，都先赋值给property属性，如果不成功，值设置为null
			for (int i = 0; i < length; i++) {
				list.set(i, getConvertedValue(beanWrapper, property, list.get(i)));
			}
			newValue = list;
		} else {
			//如果不是集合，设置原来的值或null
			newValue = getConvertedValue(beanWrapper, property, value);
		}

		//设置Condition里面的每一个List<Filter>，供以后的多条件查询需要
		filter.setValue(newValue);
	}

	/**
	 * 使用beanWrapper对entityClass属性设置值
	 * 如果设置成功，返回的值跟原来的一样，如果设置不成功，返回null
	 * @param beanWrapper
	 * @param property
	 * @param value
	 * @return
	 */
	private static Object getConvertedValue(final BeanWrapperImpl beanWrapper, final String property, final Object value) {

		Object newValue;
		try {
			//beanWrapper，在设置entityClass的Bean的属性时，当取到entityClass的属性是默认的类型，比如number,string等，会检索“默认的属性编辑器表”，
			//找到entityClass属性的类型对应的编辑器，把value字符串的值，转化为entityClass属性的类型的值，然后把转化后的值赋值给属性
			//如果是自定义类型，会检索"自定义属性编辑器表"，找到entityClass对应的编辑器
			//这里这样做的原因是，value值是用户输入的值，跟entityClass的Bean属性的类型可能不匹配；
			beanWrapper.setPropertyValue(property, value);
			//返回set后的新值，如果设置成功，返回的值跟原来的一样，如果设置不成功，返回null
			newValue = beanWrapper.getPropertyValue(property);
		} catch (InvalidPropertyException e) {
			newValue = null;
		} catch (Exception e) {
			newValue = null;
		}

		return newValue;
	}

}
