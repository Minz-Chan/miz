package com.miz.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.*;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class JsonUtil {

    private static Logger log = LoggerFactory.getLogger(JsonUtil.class);

    private static SerializeConfig mapping = new SerializeConfig();

    private static Gson LOWER_CASE_WITH_UNDERSCORES_GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd").setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private static Gson COMMON_GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    static {
        mapping.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        mapping.put(java.sql.Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        mapping.put(java.sql.Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        mapping.put(java.sql.Time.class, new SimpleDateFormatSerializer("HH:mm:ss"));
    }

    public static SerializeConfig put(Class<?> clazz, SerializeFilter filter) {
        mapping.addFilter(clazz, filter);
        return mapping;
    }

    public static SerializeConfig put(Class<?> clazz, ObjectSerializer serializer) {
        mapping.put(clazz, serializer);
        return mapping;
    }

    public static <T> T toBean(String jsonString, Class<T> tt) {
        try {
            if (StringUtils.isBlank(jsonString)) {
                return null;
            }
            T t = JSON.parseObject(jsonString, tt);
            return t;
        } catch (Exception e) {
            log.error(jsonString, e);
            return null;
        }
    }

    public static <T> List<T> toList(String jsonString, Class<T> clazz) {
        try {
            if (StringUtils.isBlank(jsonString)) {
                return null;
            }
            List<T> list = JSON.parseArray(jsonString, clazz);
            return list;
        } catch (Exception e) {
            log.error(jsonString, e);
            return null;
        }
    }

    /**
     *
     * @param bean
     * @return
     * @author xiaobo
     */
    public static String toFormatedJson(Object bean) {
        return JSON.toJSONString(bean, mapping, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat);
    }

    /**
     *
     * @param bean
     * @return
     * @author xiaobo
     */
    public static String toJson(Object bean) {
        return JSON.toJSONString(bean, mapping, SerializerFeature.DisableCircularReferenceDetect);
    }

    /**
     * 可以返回null的key值
     * @param bean
     * @return
     * @author xiaobo
     */
    public static String toJsonAboutNull(Object bean) {
        return JSON.toJSONString(bean, mapping, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteNullStringAsEmpty);
    }

    /**
     *
     * @param bean
     * @param serializeFilter
     * @return
     * @author xiaobo
     */
    public static String toJson(Object bean, SerializeFilter serializeFilter) {
        if (serializeFilter != null) {
            return JSON.toJSONString(bean, mapping, serializeFilter, SerializerFeature.DisableCircularReferenceDetect);
        } else {
            return JSON.toJSONString(bean, mapping, SerializerFeature.DisableCircularReferenceDetect);
        }
    }

    public static Object fromJsonObjectToBean(JSONObject json, Class pojo) throws Exception {
        // 首先得到pojo所定义的字段
        Field[] fields = pojo.getDeclaredFields();
        // 根据传入的Class动态生成pojo对象
        Object obj = pojo.newInstance();
        for (Field field : fields) {
            // 设置字段可访问（必须，否则报错）
            field.setAccessible(true);
            // 得到字段的属性名
            String name = field.getName();
            // 这一段的作用是如果字段在JSONObject中不存在会抛出异常，如果出异常，则跳过。
            try {
                json.get(name);
            } catch (Exception ex) {
                continue;
            }
            if (json.get(name) != null && !"".equals(json.getString(name))) {
                // 根据字段的类型将值转化为相应的类型，并设置到生成的对象中。
                if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
                    field.set(obj, Long.parseLong(json.getString(name)));
                } else if (field.getType().equals(String.class)) {
                    field.set(obj, json.getString(name));
                } else if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
                    field.set(obj, Double.parseDouble(json.getString(name)));
                } else if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
                    field.set(obj, Integer.parseInt(json.getString(name)));
                } else if (field.getType().equals(Date.class)) {
                    field.set(obj, Date.parse(json.getString(name)));
                } else if (field.getType().equals(BigDecimal.class)) {
                    field.set(obj, BigDecimal.valueOf(Double.parseDouble(json.getString(name))));
                } else if (field.getType().equals(Set.class)) {
                    field.set(obj, JSON.parseObject(json.getString(name), Set.class));
                } else {
                    continue;
                }
            }
        }
        return obj;
    }


    public static Gson getLowerCaseWithUnderscoresGson() {
        return LOWER_CASE_WITH_UNDERSCORES_GSON;
    }

    public static Gson getCommonGson() {
        return COMMON_GSON;
    }

    public static <T>T  fromLowerCaseWithUnderscoresJson(String json, Class<T> tClass) {
        return LOWER_CASE_WITH_UNDERSCORES_GSON.fromJson(json, tClass);
    }

    public static <T>T fromCommonJson(String json, Class<T> clazz) {
        return COMMON_GSON.fromJson(json, clazz);
    }


    public static String toCommonJson(Object obj) {
        return COMMON_GSON.toJson(obj);
    }

    public static String toLowerCaseWithUnderscoresJson(Object obj) {
        return LOWER_CASE_WITH_UNDERSCORES_GSON.toJson(obj);
    }

    public static boolean isGoodJson(String json) {
        if (StringUtils.isBlank(json)) {
            return false;
        }
        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonParseException e) {
            return false;
        }
    }
}
