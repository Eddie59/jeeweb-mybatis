package cn.jeeweb.core.common.service.impl;

import cn.jeeweb.core.common.data.DuplicateValid;
import cn.jeeweb.core.common.service.ICommonService;
import cn.jeeweb.core.query.data.Page;
import cn.jeeweb.core.query.data.PageImpl;
import cn.jeeweb.core.query.data.Pageable;
import cn.jeeweb.core.query.data.Queryable;
import cn.jeeweb.core.query.parse.QueryToWrapper;
import cn.jeeweb.core.query.wrapper.EntityWrapper;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

/**
 *
 * @param <M> M extends BaseMapper<T> M为继承BaseMapper<T>的Mapper类，比如UserMapper
 * @param <T> T表示entity类，比如User Dict等
 *          这里的@Override方法，都是实现ICommonService<T>接口的方法
 *           继承extends ServiceImpl<M, T>的原因，为了使用“默认的17个方法的默认实现”
 *           所以说，CommonServiceImpl方法，不仅有17个方法的默认的实现，也有自己扩展的ICommonService<T>接口中的5个方法
 *           跟业务有关的service方法，写在了DictServiceImpl、UserServiceImpl等类中，这些类继承了CommonServiceImpl方法，功能强大，一边继承
 *           通用方法（17个mybatis-plus封装的方法，5个自己封装的方法），一边自己实现跟业务有关的方法
 */
@Transactional
public class CommonServiceImpl<M extends BaseMapper<T>, T>
		extends ServiceImpl<M, T>
		implements ICommonService<T>
{

	@Override
	public Page<T> list(Queryable queryable) {
		EntityWrapper<T> entityWrapper = new EntityWrapper<>();
		return list(queryable, entityWrapper);
	}

	@Override
	public Page<T> list(Queryable queryable, Wrapper<T> wrapper) {

		QueryToWrapper<T> queryToWrapper = new QueryToWrapper<T>();
		//多条件查询
		queryToWrapper.parseCondition(wrapper, queryable);
		//排序
		queryToWrapper.parseSort(wrapper, queryable);
		//分页
		Pageable pageable = queryable.getPageable();
		com.baomidou.mybatisplus.plugins.Page<T> page = new com.baomidou.mybatisplus.plugins.Page<T>(pageable.getPageNumber(), pageable.getPageSize());
		com.baomidou.mybatisplus.plugins.Page<T> content = selectPage(page, wrapper);
		//返回一PageImpl对象
		return new PageImpl<T>(content.getRecords(), queryable.getPageable(), content.getTotal());
	}

	@Override
	public com.baomidou.mybatisplus.plugins.Page<T> myList(Queryable queryable, Wrapper<T> wrapper)
	{
		QueryToWrapper<T> queryToWrapper = new QueryToWrapper<T>();
		//多条件查询
		queryToWrapper.parseCondition(wrapper, queryable);
		//排序
		queryToWrapper.parseSort(wrapper, queryable);
		//分页
		Pageable pageable = queryable.getPageable();
		com.baomidou.mybatisplus.plugins.Page<T> page = new com.baomidou.mybatisplus.plugins.Page<T>(pageable.getPageNumber(), pageable.getPageSize());
		com.baomidou.mybatisplus.plugins.Page<T> content = selectPage(page, wrapper);
		//返回一PageImpl对象
		return content;
	}

	@Override
	public List<T> listWithNoPage(Queryable queryable) {
		EntityWrapper<T> entityWrapper = new EntityWrapper<T>();
		return listWithNoPage(queryable, entityWrapper);
	}

	@Override
	public List<T> listWithNoPage(Queryable queryable, Wrapper<T> wrapper) {
		QueryToWrapper<T> queryToWrapper = new QueryToWrapper<T>();

		queryToWrapper.parseCondition(wrapper, queryable);
		// 排序问题
		queryToWrapper.parseSort(wrapper, queryable);
		List<T> content = selectList(wrapper);
		return content;
	}

	@Override
	public Boolean doValid(DuplicateValid duplicateValid, Wrapper<T> wrapper) {
		Boolean valid = Boolean.FALSE;
		String queryType = duplicateValid.getQueryType();
		if (StringUtils.isEmpty(queryType)) {
			queryType = "table";
		}
		if (queryType.equals("table")) {
			valid = validTable(duplicateValid, wrapper);
		}
		return valid;
	}

	private Boolean validTable(DuplicateValid duplicateValid, Wrapper<T> wrapper) {
		Integer num = null;
		String extendName = duplicateValid.getExtendName();
		String extendParam = duplicateValid.getExtendParam();
		if (!StringUtils.isEmpty(extendParam)) {
			// [2].编辑页面校验
			wrapper.eq(duplicateValid.getName(), duplicateValid.getParam()).ne(extendName, extendParam);
			num = baseMapper.selectCount(wrapper);
		} else {
			// [1].添加页面校验
			wrapper.eq(duplicateValid.getName(), duplicateValid.getParam());
			num = baseMapper.selectCount(wrapper);
		}

		if (num == null || num == 0) {
			// 该值可用
			return true;
		} else {
			// 该值不可用
			return false;
		}
	}

}