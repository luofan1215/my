package org.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * MongoDB工具类
 * 
 * @author: luofan
 * @Date: 2019/10/29 10:08
 */
@Component
public class MongoUtils {

	@Autowired
	private MongoTemplate mongoTemplate;

	/***
	 * 插入时，若主键已经存在，则抛出异常
	 */
	public <T> void insert(T t) {
		mongoTemplate.insert(t);
	}

	/***
	 * 插入时，若主键已经存在，则更新数据
	 */
	public <T> void save(T t) {
		mongoTemplate.save(t);
	}

	/***
	 * 通过id查询单条数据
	 */
	public <T> T findById(Integer id, Class<T> entityClass) {
		Query query = new Query(Criteria.where("_id").is(id));
		return mongoTemplate.findOne(query, entityClass);
	}

	/**
	 * 根据多字段组合条件查询单条数据
	 */
	public <T> T findOne(Object queryObj, Class<T> entityClass) {
		Query query = getQueryByObject(queryObj);
		return mongoTemplate.findOne(query, entityClass);
	}

	/**
	 * 根据多字段组合条件查询多条数据
	 */
	public <T> List<T> findList(Object queryObj, Class<T> entityClass) {
		Query query = getQueryByObject(queryObj);
		return mongoTemplate.find(query, entityClass);
	}

	/***
	 * 根据多字段组合条件查询多条数据（分页）
	 */
	public <T> List<T> findPage(Object queryObj, Class<T> entityClass, int offset, int limit) {
		Query query = getQueryByObject(queryObj);
		query.skip(offset);
		query.limit(limit);
		return mongoTemplate.find(query, entityClass);
	}

	/***
	 * 统计总数
	 */
	public Long count(Object queryObj, Class<?> entityClass) {
		Query query = getQueryByObject(queryObj);
		return mongoTemplate.count(query, entityClass);
	}

	/**
	 * 根据id删除单条数据
	 */
	public void removeById(Integer id, Class<?> entityClass) {
		Criteria criteria = Criteria.where("_id").is(id);
		Query query = new Query(criteria);
		removeList(query, entityClass);
	}

	/***
	 * 根据对象删除单条数据
	 */
	public int removeOne(Object remObject) {
		return (int) mongoTemplate.remove(remObject).getDeletedCount();
	}

	/***
	 * 根据多字段组合条件删除多条数据
	 */
	public int removeList(Object queryObj, Class<?> entityClass) {
		Query query = getQueryByObject(queryObj);
		return (int) mongoTemplate.remove(query, entityClass).getDeletedCount();
	}

	/**
	 * 修改匹配到的第一条记录
	 */
	public void updateFirst(Object queryObj, Object updateObj, Class<?> entityClass) {
		Query query = getQueryByObject(queryObj);
		Update update = getUpdateByObject(updateObj);
		mongoTemplate.updateFirst(query, update, entityClass);
	}

	/***
	 * 修改匹配到的所有记录
	 */
	public void updateMulti(Object queryObj, Object updateObj, Class<?> entityClass) {
		Query query = getQueryByObject(queryObj);
		Update update = getUpdateByObject(updateObj);
		mongoTemplate.updateMulti(query, update, entityClass);
	}

	/***
	 * 修改匹配到的记录，若不存在该记录则进行添加
	 */
	public void upsert(Object queryObj, Object updateObj, Class<?> entityClass) {
		Query query = getQueryByObject(queryObj);
		Update update = getUpdateByObject(updateObj);
		mongoTemplate.upsert(query, update, entityClass);
	}

	/**
	 * 将查询条件对象转换为query
	 */
	private Query getQueryByObject(Object queryObj) {
		if (queryObj instanceof Query) {
			return (Query) queryObj;
		}
		Query query = new Query();
		Criteria criteria = new Criteria();
		String[] fileds = getFiledNames(queryObj);
		for (int i = 0; i < fileds.length; i++) {
			String filedName = fileds[i];
			Object filedValue = getFieldValue(filedName, queryObj);
			if (filedValue != null) {
				if (BeanUtils.isSimpleValueType(filedValue.getClass())) {
					criteria.and(filedName).is(filedValue);
				} else {
					Map<String, ? extends Object> map = convertObjectToMap(filedValue);
					for (Map.Entry<String, ? extends Object> entry : map.entrySet()) {
						if (!ObjectUtils.isEmpty(entry.getValue())) {
							criteria.and(filedName + "." + entry.getKey()).is(entry.getValue());
						}
					}
				}
			}
		}
		query.addCriteria(criteria);
		return query;
	}

	/**
	 * 将查询条件对象转换为update
	 */
	private Update getUpdateByObject(Object updateObj) {
		if (updateObj instanceof Update) {
			return (Update) updateObj;
		}
		Update update = new Update();
		String[] fileds = getFiledNames(updateObj);
		for (int i = 0; i < fileds.length; i++) {
			String filedName = fileds[i];
			Object filedValue = getFieldValue(filedName, updateObj);
			if (filedValue != null) {
				update.set(filedName, filedValue);
			}
		}
		return update;
	}

	/***
	 * 获取对象属性返回字符串数组
	 */
	private String[] getFiledNames(Object o) {
		Field[] fields = o.getClass().getDeclaredFields();
		String[] fieldNames = new String[fields.length];

		for (int i = 0; i < fields.length; ++i) {
			fieldNames[i] = fields[i].getName();
		}

		return fieldNames;
	}

	/***
	 * 根据属性获取对象属性值
	 */
	private Object getFieldValue(String fieldName, Object o) {
		Object fieldValue;
		try {
			String e = fieldName.substring(0, 1).toUpperCase();
			String getter = "get" + e + fieldName.substring(1);
			Method method = o.getClass().getMethod(getter);
			fieldValue = method.invoke(o);
		} catch (Exception e) {
			fieldValue = null;
		}
		return fieldValue;
	}

	/**
	 * 将对象转为Map
	 */
	private Map<String, ? extends Object> convertObjectToMap(Object filedValue) {
		if (filedValue.getClass().isArray() || filedValue instanceof Collection) {
			return null;
		}
		return doConvertObjectToMap(filedValue);
	}

	@SuppressWarnings("unchecked")
	private Map<String, ? extends Object> doConvertObjectToMap(Object filedValue) {
		if (filedValue instanceof Map) {
			return (Map<String, ? extends Object>) filedValue;
		} else {
			try {
				return org.apache.commons.beanutils.BeanUtils.describe(filedValue);
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	public <T> List<T> aggregate(Aggregation aggregation, String collectionName, Class<T> entityClass){
		AggregationResults<T> results = mongoTemplate.aggregate(aggregation, collectionName, entityClass);
		return results.getMappedResults();
	}
}
