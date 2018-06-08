package cn.jeeweb.core.common.service;

import java.util.List;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;

import cn.jeeweb.core.common.data.DuplicateValid;
import cn.jeeweb.core.query.data.Page;
import cn.jeeweb.core.query.data.Queryable;

/**
 *
 * @param <T>
 *     IService<T>有mybatis-plus封装的17个方法
 *     自己再封装5个方法
 */
public interface ICommonService<T> extends IService<T> {

	 Page<T> list(Queryable queryable);
	
	 Page<T> list(Queryable queryable, Wrapper<T> wrapper);

	 List<T> listWithNoPage(Queryable queryable);

	 List<T> listWithNoPage(Queryable queryable, Wrapper<T> wrapper);

	 Boolean doValid(DuplicateValid duplicateValid, Wrapper<T> wrapper);

	com.baomidou.mybatisplus.plugins.Page myList(Queryable queryable, Wrapper<T> wrapper);
}