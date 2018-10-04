package com.github.collinalpert.java2db.utilities;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.services.BaseService;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Collin Alpert
 * An <pre>Inversion of Control</pre> container.
 * It is responsible for registering and resolving services.
 */
public class IoC {

	private static Map<Class<? extends BaseEntity>, BaseService<? extends BaseEntity>> services;


	static {
		services = new HashMap<>();
	}

	/**
	 * Creates an instance of any class with an empty constructor.
	 *
	 * @param clazz The class to be constructed.
	 * @param <E>   The type of the entity.
	 * @return An instance of this class.
	 * @throws IllegalArgumentException if the class cannot be constructed for any reason.
	 *                                  This can occur if there is no public parameterless constructor available.
	 */
	public static <E> E resolve(Class<E> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(String.format("Class %s could not be instantiated.", clazz.getSimpleName()));
		}
	}

	/**
	 * Resolves a service class. This is to preserve the singleton pattern.
	 * Only one instance of a service is needed in the lifecycle of an application.
	 *
	 * @param clazz The service class to retrieve.
	 * @param <S>   The type of the service class.
	 * @return The previously registered instance of a service class.
	 */
	public static <S extends BaseService> S resolveService(Class<S> clazz) {
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
	 * @param <E>   The type of the entity.
	 * @param <S>   The type of the service.
	 * @return An instance of a previously registered service class.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends BaseEntity, S extends BaseService<E>> S resolveServiceByEntity(Class<E> clazz) {
		if (!services.containsKey(clazz)) {
			throw new IllegalArgumentException(String.format("An instance of a service for the entity %s has not been registered yet. Please use the \"registerService\" method.", clazz.getSimpleName()));
		}
		return (S) services.get(clazz);
	}

	/**
	 * Registers an instance of a service class.
	 *
	 * @param clazz   An entity class.
	 * @param service The service class this entity corresponds to.
	 * @param <E>     The type of the entity.
	 * @param <S>     The type of the service class.
	 */
	public static <E extends BaseEntity, S extends BaseService<E>> void registerService(Class<E> clazz, S service) {
		services.put(clazz, service);
	}

	/**
	 * Checks if a service has already been registered.
	 *
	 * @param clazz The class os the service.
	 * @param <S>   The type of the service.
	 * @return {@code True} if an instance of the service is registered, {@code false} if not.
	 */
	public <S extends BaseService> boolean isRegistered(Class<S> clazz) {
		return services.values().stream().anyMatch(x -> x.getClass() == clazz);
	}
}
