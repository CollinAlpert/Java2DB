module com.github.collinalpert.java2db {
	requires java.sql;
	requires mysql.connector.java;
	requires com.github.collinalpert.lambda2sql;

	exports com.github.collinalpert.java2db.annotations;
	exports com.github.collinalpert.java2db.database;
	exports com.github.collinalpert.java2db.entities;
	exports com.github.collinalpert.java2db.mappers;
	exports com.github.collinalpert.java2db.queries;
	exports com.github.collinalpert.java2db.services;
	exports com.github.collinalpert.java2db.utilities;
}