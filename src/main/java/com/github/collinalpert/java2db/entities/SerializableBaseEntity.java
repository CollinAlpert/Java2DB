package com.github.collinalpert.java2db.entities;

import java.io.Serializable;

/**
 * Use this class when you need your objects to be automatically serializable.
 *
 * @author Collin Alpert
 */
public class SerializableBaseEntity extends BaseEntity implements Serializable {

	private int id;

	public int getId() {
		return this.id;
	}

	/**
	 * This setter only exists for frameworks like Spring, where a form needs to set this id.
	 * It is <b>greatly</b> discouraged from using this setter directly and it's effects will not be considered with any of the CRUD operations.
	 *
	 * @param id The id of the entity.
	 */
	@Deprecated
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Id: " + this.id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof SerializableBaseEntity)) {
			return false;
		}

		SerializableBaseEntity that = (SerializableBaseEntity) o;

		return this.id == that.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}
}
