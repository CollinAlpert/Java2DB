package com.github.collinalpert.java2db.utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory class for generating unique identifiers.
 * They are used for generating unique aliases.
 *
 * @author Collin Alpert
 */
public class UniqueIdentifier {

	private static int id;
	private static Map<String, String> identifiers;

	static {
		id = 0;
		identifiers = new HashMap<>();
	}

	/**
	 * Generates a unique alias from a base.
	 *
	 * @param base The base to generate from.
	 * @param name The name of the nested property for which this alias is used.
	 *             It is needed so the {@code BaseMapper} can retrieve it later.
	 * @return A unique alias.
	 */
	public static String generate(String base, String name) {
		var identifier = base + ++id;
		identifiers.put(name, identifier);
		return identifier;
	}

	/**
	 * Gets the alias for a certain nested property.
	 *
	 * @param key The name of the nested property.
	 * @return The alias.
	 */
	public static String getIdentifier(String key) {
		return identifiers.getOrDefault(key, "");
	}

	/**
	 * Resets the values in this class.
	 */
	public static void unset() {
		id = 0;
		identifiers.clear();
	}
}
