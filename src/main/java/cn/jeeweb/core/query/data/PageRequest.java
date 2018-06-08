package cn.jeeweb.core.query.data;

import java.io.Serializable;

import cn.jeeweb.core.query.data.Sort.Direction;

/**
 * Basic Java Bean implementation of {@code Pageable}.
 * 
 * @author Oliver Gierke
 */
public class PageRequest implements Pageable, Serializable {

	private static final long serialVersionUID = 8280485938848398236L;

	/**
	 * 当前第几页
	 */
	private final int page;
	/**
	 * 总页数
	 */
	private final int size;
	/**
	 * 排序对象
	 */
	private final Sort sort;

	public PageRequest(int page, int size) {
		this(page, size, null);
	}
	public PageRequest(int page, int size, Direction direction, String... properties) {
		this(page, size, new Sort(direction, properties));
	}
	public PageRequest(int page, int size, Sort sort) {
		if (page < 0) {
			throw new IllegalArgumentException("Page index must not be less than zero!");
		}
		if (size < 0) {
			throw new IllegalArgumentException("Page size must not be less than zero!");
		}
		this.page = page;
		this.size = size;
		this.sort = sort;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.domain.Pageable#getPageSize()
	 */
	public int getPageSize() {

		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.domain.Pageable#getPageNumber()
	 */
	public int getPageNumber() {
		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.domain.Pageable#getOffset()
	 */
	public int getOffset() {
		return page * size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.domain.Pageable#getSort()
	 */
	public Sort getSort() {
		return sort;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.domain.Pageable#hasPrevious()
	 */
	public boolean hasPrevious() {
		return page > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.domain.Pageable#next()
	 */
	public Pageable next() {
		return new PageRequest(page + 1, size, sort);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.domain.Pageable#previousOrFirst()
	 */
	public Pageable previousOrFirst() {
		return hasPrevious() ? new PageRequest(page - 1, size, sort) : this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.domain.Pageable#first()
	 */
	public Pageable first() {
		return new PageRequest(0, size, sort);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof PageRequest)) {
			return false;
		}

		PageRequest that = (PageRequest) obj;

		boolean pageEqual = this.page == that.page;
		boolean sizeEqual = this.size == that.size;

		boolean sortEqual = this.sort == null ? that.sort == null : this.sort.equals(that.sort);

		return pageEqual && sizeEqual && sortEqual;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = 17;

		result = 31 * result + page;
		result = 31 * result + size;
		result = 31 * result + (null == sort ? 0 : sort.hashCode());

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Page request [number: %d, size %d, sort: %s]", page, size,
				sort == null ? null : sort.toString());
	}
}
