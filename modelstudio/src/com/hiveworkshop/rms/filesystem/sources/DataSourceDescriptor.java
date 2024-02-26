package com.hiveworkshop.rms.filesystem.sources;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

public interface DataSourceDescriptor extends Serializable {
	DataSource createDataSource();

	String getDisplayName();

	DataSourceDescriptor duplicate();

	String getPath();

	static String toSaveString(DataSourceDescriptor descriptor) {
		StringBuilder sb = new StringBuilder();
		Field[] declaredFields = descriptor.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			if(!Modifier.isStatic(field.getModifiers())){
				try {
					String name = field.getName();
					field.setAccessible(true);
					Object o = field.get(descriptor);

					if (o instanceof Collection<?> collection) {
						sb.append(name).append(" = ").append("[");
						for (Object e : collection) {
							if (e instanceof String) {
								sb.append("\"").append(e).append("\", ");
							} else {
								sb.append(e).append(", ");
							}
						}
						sb.append("]; ");
					} else {
						if (o instanceof String) {
							sb.append(name).append(" = \"").append(o).append("\"; ");
						} else {
							sb.append(name).append(" = ").append(o).append("; ");
						}
					}
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return sb.toString();
	}
}
