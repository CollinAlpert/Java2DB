package de.collin.utilities;

import de.collin.entities.BaseEntity;
import de.collin.services.BaseService;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Collin Alpert
 */
public class IoC {

	private static Map<Class<? extends BaseEntity>, BaseService<?>> services;
	private static Map<Class<? extends BaseEntity>, Predicate<? extends BaseEntity>> defaultConstraints;

	static {
		services = new HashMap<>();
		defaultConstraints = new HashMap<>();
	}

	public static <T> T resolve(Class<T> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(String.format("Class %s could not be instantiated.", clazz.getSimpleName()));
		}
	}

	public static <T extends BaseService> T resolveService(Class<T> clazz) {
		var service = services.values().stream().filter(x -> x.getClass().equals(clazz)).findFirst();
		if (!service.isPresent()) {
			throw new IllegalArgumentException(String.format("An instance of the service %s has not been registered yet. Please use the \"registerService\" method.", clazz.getSimpleName()));
		}
		return clazz.cast(service.get());
	}

	public static BaseService<?> resolveServiceByEntity(Class<? extends BaseEntity> clazz) {
		if (!services.containsKey(clazz)) {
			throw new IllegalArgumentException(String.format("An instance of a service for the entity %s has not been registered yet. Please use the \"registerService\" method.", clazz.getSimpleName()));
		}
		return services.get(clazz);
	}

	public static <T extends BaseEntity, K extends BaseService<T>> void registerService(Class<T> clazz, K service) {
		services.put(clazz, service);
	}
}
