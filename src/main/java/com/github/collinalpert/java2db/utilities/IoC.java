package com.github.collinalpert.java2db.utilities;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.mappers.IMapper;
import com.github.collinalpert.java2db.services.BaseService;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * An <pre>Inversion of Control</pre> container.
 * It is responsible for registering and resolving services and custom mappers.
 *
 * @author Collin Alpert
 */
public final class IoC {

	private static Map<Class<? extends BaseEntity>, BaseService<? extends BaseEntity>> services;
	private static Map<Class<? extends BaseEntity>, IMapper<? extends BaseEntity>> mappers;


	static {
		services = new HashMap<>();
		mappers = new HashMap<>();
	}

	/**
	 * Creates an instance of any class with an empty constructor.
	 * It is used to create a new instance of the entity classes, so they can be filled with values.
	 * This is why every entity class needs an empty constructor.
	 *
	 * @param clazz The class to be constructed.
	 * @param <E>   The type of the entity.
	 * @return An instance of this class.
	 * @throws IllegalArgumentException if the class cannot be constructed for any reason.
	 *                                  This can occur if there is no public parameterless constructor available.
	 */
	public static <E> E createInstance(Class<E> clazz) {
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
	 * Resolves a mapper class. This is to preserve the singleton pattern.
	 * Only one instance of a mapper is needed in the lifecycle of an application.
	 *
	 * @param clazz The entity that the mapper was registered for.
	 * @param <E>   The type of the entity.
	 * @return The previously registered instance of a service class.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends BaseEntity> IMapper<E> resolveMapper(Class<E> clazz) {
		if (!mappers.containsKey(clazz)) {
			throw new IllegalArgumentException(String.format("An instance of a mapper for the entity %s has not been registered yet. Please use the \"registerMapper\" method.", clazz.getSimpleName()));
		}

		return (IMapper<E>) mappers.get(clazz);
	}

	/**
	 * Resolves a mapper class. If an instance of this mapper has not been registered yet,
	 * a backup mapper is used and also registered.
	 *
	 * @param clazz         The type of the corresponding mapper.
	 * @param defaultMapper The default mapper, in case a custom mapper is not registered for this type.
	 * @param <E>           The type of the mapper.
	 * @return The mapper for the entity. If is does not exists, the default mapper is returned.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends BaseEntity> IMapper<E> resolveMapper(Class<E> clazz, IMapper<E> defaultMapper) {
		if (mappers.containsKey(clazz)) {
			return (IMapper<E>) mappers.get(clazz);
		}

		registerMapper(clazz, defaultMapper);
		return defaultMapper;
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
	 * Resolves a service class by the entity it was registered with. If an instance of this service has not been registered yet,
	 * a backup service is used and also registered.
	 *
	 * @param clazz          The entity class corresponding to a service class.
	 * @param defaultService The backup to use in case a service for the supplied class has not been registered yet.
	 * @param <E>            The type of the entity.
	 * @param <S>            The type of the service.
	 * @return An instance of a previously registered service class.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends BaseEntity, S extends BaseService<E>> S resolveServiceByEntity(Class<E> clazz, S defaultService) {
		if (services.containsKey(clazz)) {
			return (S) services.get(clazz);
		}

		registerService(clazz, defaultService);
		return defaultService;
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
	 * Registers an instance of a mapper class.
	 *
	 * @param clazz  An entity class.
	 * @param mapper The mapper class for the entity.
	 * @param <E>    The type of the entity.
	 * @param <M>    The type of the mapper class.
	 */
	public static <E extends BaseEntity, M extends IMapper<E>> void registerMapper(Class<E> clazz, M mapper) {
		mappers.put(clazz, mapper);
	}

	/**
	 * Checks if a service has already been registered.
	 *
	 * @param clazz The class of the entity a service was registered for.
	 * @param <E>   The type of the service.
	 * @return {@code True} if an instance of the service is registered, {@code false} if not.
	 */
	public static <E extends BaseEntity> boolean isServiceRegistered(Class<E> clazz) {
		return services.containsKey(clazz);
	}

	/**
	 * Checks if a mapper has already been registered.
	 *
	 * @param clazz The class of the entity a mapper was registered for.
	 * @param <E>   The type of the entity.
	 * @return {@code True} if an instance of the mapper is registered, {@code false} if not.
	 */
	public static <E extends BaseEntity> boolean isMapperRegistered(Class<E> clazz) {
		return mappers.containsKey(clazz);
	}
}
