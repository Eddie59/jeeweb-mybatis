package cn.jeeweb.core.query.data;

import java.util.Iterator;
import java.util.List;

public interface Page<T> extends Iterable<T> {

	/**
	 * @return 当前第几页
	 */
	int getNumber();

	/**
	 * @return 每页数据量
	 */
	int getSize();

	/**
	 * @return 总页数
	 */
	int getTotalPages();

	/**
	 * @return 当前页的数据序号
	 */
	int getNumberOfElements();

	/**
	 * @return 总数据数
	 */
	long getTotalElements();

	/**
	 * @return 是否有上一页
	 */
	boolean hasPreviousPage();

	/**
	 * @return 是否是第一页
	 */
	boolean isFirstPage();

	/**
	 * @return 是否有下一页
	 */
	boolean hasNextPage();

	/**
	 * @return 是否是最后一页
	 */
	boolean isLastPage();

	/**
	 * @return 下一页
	 */
	Pageable nextPageable();

	/**
	 * @return 上一页
	 */
	Pageable previousPageable();

	@Override
	Iterator<T> iterator();


	/**
	 *
	 * @return 当前页的数据集合
	 */
	List<T> getContent();

	/**
	 *
	 * @return 是否有数据
	 */
	boolean hasContent();

	/**
	 *
	 * @return 当前页的排序对象
	 */
	Sort getSort();
}
