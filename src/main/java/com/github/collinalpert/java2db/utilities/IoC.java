package com.github.collinalpert.java2db.utilities;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.services.BaseService;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Collin Alpert
 * <p>
 * A <code>Inversion of Control</code> class.
 * It is responsible for registering services, resolving services and administrating default query constraints.
 * </p>
 */
public class IoC {
	private static Map<Class<? extends BaseEntity>, BaseService<? extends BaseEntity>> services;
	private static Map<Class<? extends BaseEntity>, SqlPredicate<? extends BaseEntity>> selectConstraints;

	static {
		services = new HashMap<>();
		selectConstraints = new HashMap<>();
	}

	/**
	 * Resolves any class.
	 *
	 * @param clazz The class to be constructed.
	 * @param <T>   The type of the class.
	 * @return An instance of this class.
	 * @throws IllegalArgumentException if the class cannot be constructed for any reason.
	 *                                  This can occur if there is no public parameterless constructor available.
	 */
	public static <T> T resolve(Class<T> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(String.format("Class %s could not be instantiated.", clazz.getSimpleName()));
		}
	}

	/**
	 * Resolves a service class. This is to prevent multiple instances of a service. Only one is needed in the lifecycle of an application.
	 *
	 * @param clazz The service class to retrieve.
	 * @param <T>   The type of the service class.
	 * @return The previously registered instance of a service class.
	 */
	public static <T extends BaseService> T resolveService(Class<T> clazz) {
		var service = services.values().stream().filter(x -> x.getClass().equals(clazz)).findFirst();
		if (!service.isPresent()) {
			throw new IllegalArgumentException(String.format("An instance of the service %s has not been registered yet. Please use the \"registerService\" method.", clazz.getSimpleName()));
		}
		return clazz.cast(service.get());
	}

	/**
	 * Resolves a service class by the entity it was registered with.
	 *
	 * @param clazz The entity class corresponding to a service class.
	 * @return An instance of a previously registered service class.
	 */
	public static BaseService<?> resolveServiceByEntity(Class<? extends BaseEntity> clazz) {
		if (!services.containsKey(clazz)) {
			throw new IllegalArgumentException(String.format("An instance of a service for the entity %s has not been registered yet. Please use the \"registerService\" method.", clazz.getSimpleName()));
		}
		return services.get(clazz);
	}

	/**
	 * Registers an instance of a service class.
	 *
	 * @param clazz   An entity class.
	 * @param service The service class this entity corresponds to.
	 * @param <T>     The type of the entity.
	 * @param <K>     The type of the service class.
	 */
	public static <T extends BaseEntity, K extends BaseService<T>> void registerService(Class<T> clazz, K service) {
		services.put(clazz, service);
	}

	// This section does not work yet due to a bug in the JaQue library. It will be available soon.

	public static <T extends BaseEntity> void addDefaultConstraint(Class<T> clazz, SqlPredicate<T> predicate) {
		selectConstraints.put(clazz, predicate);
	}

	public static SqlPredicate<? extends BaseEntity> getConstraints(Class<? extends BaseEntity> tClass) {
		if (tClass == BaseEntity.class) {
			return selectConstraints.getOrDefault(BaseEntity.class, x -> true);
		}
		Class<? extends BaseEntity> superClass = (Class<? extends BaseEntity>) tClass.getSuperclass();
		return selectConstraints.getOrDefault(tClass, x -> true).and(((SqlPredicate<? super BaseEntity>) getConstraints(superClass)));
	}
}
