package cn.jeeweb.core.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class ComplexPropertyPreFilter implements PropertyPreFilter {

	//Map<Class<?>, Set<String>> includes   Class<?>作为key，Set<String>作为value，
	//记录的是每一个模块，比如DictClass，和它对应的，列名，比如code name
	//什么时候用呢，序列化json的时候会用到
	//includes表示需要序列化的集合，excludes表示不需要序列化的集合
	private Map<Class<?>, Set<String>> includes = new HashMap<Class<?>, Set<String>>();
	private Map<Class<?>, Set<String>> excludes = new HashMap<Class<?>, Set<String>>();

	static {
		JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
	}

	public ComplexPropertyPreFilter() {

	}

	public ComplexPropertyPreFilter(Map<Class<?>, Set<String>> includes) {
		super();
		this.includes = includes;
	}

	/**
	 *
	 * @param clazz
	 * @param properties
	 * 以Class<?> clazz作为key，String... properties作为value集合，把列集合保存到includes中
	 */
	public void addIncludeFilter(Class<?> clazz, String... properties) {
		Set<String> includeSet = null;
		if (!includes.containsKey(clazz)) {
			includeSet = new HashSet<String>();
			includes.put(clazz, includeSet);
		} else {
			includeSet = includes.get(clazz);
		}
		for (String item : properties) {
			if (item != null) {
				includeSet.add(item);
			}
		}
	}

	public void addExcludeFilter(Class<?> clazz, String... properties) {
		Set<String> excludeSet = null;
		if (!excludes.containsKey(clazz)) {
			excludeSet = new HashSet<String>();
			excludes.put(clazz, excludeSet);
		} else {
			excludeSet = excludes.get(clazz);
		}
		for (String item : properties) {
			if (item != null) {
				excludeSet.add(item);
			}
		}
	}

	@Override
	public boolean apply(JSONSerializer serializer, Object source, String name) {
	 	if (source == null) {
			return true;
		}
		Class<?> clazz = source.getClass();
		Set<String> excludeSet = getValue(this.excludes, clazz);
		if (excludeSet != null) {
		 	if (excludeSet.contains(name)) {
				return false;
			}
		}

	 	if (this.includes.isEmpty()) {
			return true;
		}

		Set<String> includeSet = getValue(this.includes, clazz);
		if (includeSet != null) {
			if (includeSet.contains(name)) {
				return true;
			}
		} else {
			return true;
		}
		return false;
	}

	public Set<String> getValue(Map<Class<?>, Set<String>> clazzMaps, Class<?> clazz) {
		Set<String> value = null;
		for (Map.Entry<Class<?>, Set<String>> item : clazzMaps.entrySet()) {
			// isAssignableFrom()，用来判断类型间是否有继承关系
			if (item.getKey().isAssignableFrom(clazz)) {
				value = item.getValue();
			}
		}
		return value;
	}

	public Map<Class<?>, Set<String>> getIncludes() {
		return includes;
	}

	public void setIncludes(Map<Class<?>, Set<String>> includes) {
		this.includes = includes;
	}

	public Map<Class<?>, Set<String>> getExcludes() {
		return excludes;
	}

	public void setExcludes(Map<Class<?>, Set<String>> excludes) {
		this.excludes = excludes;
	}

}