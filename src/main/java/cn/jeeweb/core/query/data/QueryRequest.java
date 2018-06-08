package cn.jeeweb.core.query.data;

import java.util.List;
import org.springframework.util.Assert;
import cn.jeeweb.core.query.data.Condition.Filter;
import cn.jeeweb.core.query.data.Condition.Operator;
import cn.jeeweb.core.query.exception.InvlidOperatorException;
import cn.jeeweb.core.query.exception.QueryException;
import cn.jeeweb.core.utils.StringUtils;

public class QueryRequest implements Queryable {
	private Pageable pageable; // 分页
	private Sort sort;// 排序
	private Condition condition;// 条件
	// 查询参数分隔符
	public static final String separator = "||";

	private boolean converted;

	public QueryRequest() {

	}

	public static Queryable newQueryable() {
		return new QueryRequest();
	}

	public Pageable getPageable() {
		return pageable;
	}

	public void setPageable(Pageable pageable) {
		this.pageable = pageable;
	}

	public Sort getSort() {
		return sort;
	}

	/**
	 * 添加排序
	 */
	public void addSort(Sort sort) {

	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	/**
	 *
	 * @param property 列名
	 * @return 过滤值，就是用户输入的搜索条件
	 */
	@Override
	public Object getValue(String property) {
		if (this.getCondition() != null && this.getCondition().getFilterFor(property) != null) {
			//this.getCondition()当前Queryrequest对象的Condition对象（一个Condition对象包含多个Filter对象）
			//.getFilterFor(property)从众多Filter对象中，根据property列名获取一个Filter对象
			//.getValue()获取这个Filter对象的value，其实就是过滤值，就是用户输入的搜索条件
			return this.getCondition().getFilterFor(property).getValue();
		}
		return null;
	}

	@Override
	public Queryable addCondition(String property, Object value) {
		Assert.notNull(property, "Condition key must not null");

		String[] searchs = StringUtils.split(property, "||");
		if (searchs.length == 0) {
			throw new QueryException("Condition key format must be : property or property_op");
		}
		//列名
		property = searchs[0];
		//操作（枚举）
		Operator operator = null;

		if (searchs.length == 1) {
			operator = Operator.eq;
		}
		else
		{
			//返回指定名称的操作枚举
			operator = Operator.fromString(searchs[1]);
		}

		boolean isValueBlank = (value == null);
		isValueBlank = isValueBlank || (value instanceof String && StringUtils.isBlank((String) value));
		isValueBlank = isValueBlank || (value instanceof List && ((List<?>) value).size() == 0);
		//is null  is not null
		boolean allowBlankValue = Operator.isAllowBlankValue(operator);
		// 过滤掉空格、空值，即不参与查询
		if (!allowBlankValue && isValueBlank) {
			return null;
		}


		if (condition == null) {
			Condition.Filter filter = new Condition.Filter(operator, property, value);
			condition = new Condition(filter);
		} else {
			//添加搜索条件
			condition.and(operator, property, value);
		}
		return this;
	}

	@Override
	public boolean isConverted() {
		return converted;
	}

	@Override
	public void removeCondition(String property) {
		this.getCondition().remove(property);
	}

}
