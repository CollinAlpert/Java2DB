# Java2DB
This little badboy allows you to access your 
database without writing a single line of SQL in your application.\
While this library may not offer the most fancy features or allow 
you to fire huge queries to your database, that is not what it is intended for. 
It is lightweight and meant for a quick and simple access to your database.

**Please note:** This is a Java 11 library. Make sure you have Java 11 installed when using this library. 
This library is not suitable for projects requiring complex SQL queries, although it does offer some advanced features.
It is meant for smaller projects which want to interact with their database in a simple, 
but easy way without bloating the source code with SQL queries.

### How it works
Java classes (entities) model tables on the database and their fields. 
Every class has a corresponding service class, which acts as a data 
service and interacts with the database. The service classes will fill an entity with values from the database. 
Which values should be fetched, can be defined inside the services. 

### Usage/Example
Lets say we have a database with two tables with the following structure:

``gender``\
&nbsp;&nbsp;&nbsp;&nbsp;``id``\
&nbsp;&nbsp;&nbsp;&nbsp;``code``\
&nbsp;&nbsp;&nbsp;&nbsp;``description``\
&nbsp;&nbsp;&nbsp;&nbsp;``isBinary``

``person``\
&nbsp;&nbsp;&nbsp;&nbsp;``id``\
&nbsp;&nbsp;&nbsp;&nbsp;``name``\
&nbsp;&nbsp;&nbsp;&nbsp;``age``\
&nbsp;&nbsp;&nbsp;&nbsp;``genderId``(foreign key to `id` column of `gender`)

Then we would need two entities:

```java
@TableName("gender")
public class Gender extends BaseCodeAndDescriptionEntity {
	// In this case, BaseCodeAndDescriptionEntity gives us the id, the code and the description.
	// We need to add the rest.
	
	private boolean isBinary;
	
	// Getters and setters...
}
```

```java
@TableName("person")
public class Person extends BaseEntity {
	// In this case, BaseEntity only gives us an id field. 
	// We need to add the rest.
	
	private String name;
	
	private int age;
	
	private int genderId;
	
	// That way Java2DB will know that this field does not exist on the database.
	// It will be filled accordingly.
	@ForeignKeyObject("genderId")
	private Gender gender;
	
	// Getters and setters...
	// Note that it is not suggested for foreign key entities to have setters. 
	// They are effectively useless.
}
```

Every entity *must* extend ``BaseEntity`` and have an empty constructor.

Then we can go ahead and create the (for now) empty service classes:

```java
public class GenderService extends BaseService<Gender> {
	
}
```

```java
public class PersonService extends BaseService<Person> {
	
}
```

Every service *must* extend ``BaseService``.

That's it! Now we can access the database using the services with simple predefined methods like ``getById`` and so on. 
Custom methods can be defined in the respective service using the 
``getSingle`` or ``getMultiple`` methods provided by the ``BaseService`` class. 
When using ``getMultiple`` method, you can use some more query options, like ``where``, ``orderBy`` and ``limit`` 
in the returned ``Query`` object.

All these methods can only be used by methods in the respective service classes.
This is because every service should have descriptive methods for any data they get.

The current version also offers full support for default query constraints. 
This means you can tell Java2DB to execute a certain WHERE condition on *every* query that is executed on a specific table.
Here's an example: Say every query executed on the ``person`` table should only return people of age
18 and older and with an id greater than 0 (just because). This can be achieved adding query constraints using the ``QueryConstraints`` class.
In our case, this would look something like this:

```java
public class Main {
	public static void main(String[] args){
	    // register the services and all that
	    QueryConstraints.addConstraint(Person.class, person -> person.getAge() >= 18);
	    QueryConstraints.addConstraint(Person.class, person -> person.getId() >= 0);
	}
}
```

### Getting started

First, include the Maven artifact:
```xml
<dependency>
    <groupId>com.github.collinalpert</groupId>
    <artifactId>java2db</artifactId>
    <version>2.2</version>
</dependency>
```
Or include the [JAR](https://github.com/CollinAlpert/Java2DB/releases/latest) in your project. To begin using this library, you need to do two things on program start:
1. Connect to the database. Set the static variables ``HOST``, ``DATABASE``, ``USERNAME``, ``PASSWORD`` and optionally ``DATABASE_TYPE`` and ``PORT`` of the `DBConnection` class to achieve possibility of connection.
2. Register an instance of all of your services. Use the ``IoC.registerService`` method to do this. Using the above example, it would look something like this: ``IoC.registerService(Person.class, new PersonService());``.

As the services should follow the singleton pattern, you should retrieve an instance of a service using the ``IoC.resolveService`` method.\
If you would not like your queries logged in the console, use the ``DBConnection.LOG_QUERIES = false;`` statement.  