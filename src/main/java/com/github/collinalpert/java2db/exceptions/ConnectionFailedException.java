package com.github.collinalpert.java2db.exceptions;

/**
 * @author Collin Alpert
 */
public class ConnectionFailedException extends RuntimeException {

	public ConnectionFailedException() {
		super("The connection to the database failed. Please check your host/ip address, if the MySQL server is reachable and if you have an internet connection.");
	}
}
