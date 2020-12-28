package com.github.collinalpert.java2db.sandbox;

import com.github.collinalpert.java2db.entities.BaseEntity;

public class User extends BaseEntity {
	private String name;

	public User(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
