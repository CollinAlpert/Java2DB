package com.github.collinalpert.java2db.sandbox;

import com.github.collinalpert.java2db.entities.BaseEntity;

public class Order extends BaseEntity {
	String product;
	int amount;

	public Order(String product, int amount) {
		this.product = product;
		this.amount = amount;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
}
