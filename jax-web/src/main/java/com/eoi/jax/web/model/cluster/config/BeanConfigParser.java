/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eoi.jax.web.model.cluster.config;

import com.eoi.jax.web.common.ResponseCode;
import com.eoi.jax.web.common.exception.BizException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class BeanConfigParser {

    public Map<String, BeanConfigsDescription> getBeanDescriptions(Class... classes) {
        Map<String, BeanConfigsDescription> beanConfigDefs = new LinkedHashMap<>();
        for (Class clazz: classes) {
            BeanConfigsDescription beanDesc = parseConfigBeanByClass(clazz);
            if (null != beanDesc) {
                beanConfigDefs.put(beanDesc.getName(),beanDesc);
            }
        }
        return beanConfigDefs;
    }

    public static Map<String, List<ConfigDescription>> parseConfigDescriptions(Class... classes) {
        Map<String, List<ConfigDescription>> beanConfigDefs = new LinkedHashMap<>();
        for (Class clazz: classes) {
            BeanConfigsDescription beanDesc = parseConfigBeanByClass(clazz);
            if (null != beanDesc) {
                beanConfigDefs.put(beanDesc.getName(), new ArrayList<>(beanDesc.getConfigs().values()));
            }
        }
        return beanConfigDefs;
    }

    public static BeanConfigsDescription parseConfigBeanByClass(Class clazz) {
        List<Field> allFields = new LinkedList<>();
        Class tmpClazz = clazz;
        while (null != tmpClazz) {
            Field[] fields = tmpClazz.getDeclaredFields();
            if (null != fields && fields.length > 0) {
                for (Field field: fields) {
                    allFields.add(field);
                }
            }
            tmpClazz = tmpClazz.getSuperclass();
        }

        if (allFields.isEmpty()) {
            return null;
        }

        Map<String, ConfigDescription> foundConfigDefs = new LinkedHashMap<>();
        for (Field field: allFields) {
            try {
                field.setAccessible(true);
                ConfigDef annotation = field.getAnnotation(ConfigDef.class);
                if (annotation == null) {
                    continue;
                }
                String fieldName = field.getName();
                ConfigDescription confDesc = ConfigDescription.fromDef(fieldName,annotation);
                foundConfigDefs.put(fieldName,confDesc);
            } catch (RuntimeException e) {
                throw new BizException(ResponseCode.INVALID_PARAM);
            }
        }

        if (foundConfigDefs.isEmpty()) {
            return null;
        }

        String name = clazz.getSimpleName();
        if (clazz.isAnnotationPresent(ConfigBean.class)) {
            ConfigBean annotation = (ConfigBean) clazz.getAnnotation(ConfigBean.class);
            String value = annotation.value();
            if (! value.isEmpty()) {
                name = value;
            }
        }
        return new BeanConfigsDescription(name,clazz.getCanonicalName(),foundConfigDefs);
    }


}
