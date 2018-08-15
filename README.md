# Java2DB
This little badboy allows you to access your 
database without writing a single line of SQL in your application.

**Please note:** This is a Java 10 library. Make sure you have Java 10 installed when using this library. 
This library is not suitable for projects requiring complex SQL queries. 
It is meant for smaller projects which want to interact with their database in a simple, 
but easy way without bloating the source code with SQL queries.

### How it works
 Java classes (entities) model tables on the database and their fields. 
 Every class has a corresponding service class, which acts as a data 
 service and interacts with the database. The service classes will fill an entity with values from the database. 
 Which values should be fetched, can be defined inside the services. 

### Example
Lets say we have a database with two tables with the following structure:

``gender``\
&nbsp;&nbsp;&nbsp;&nbsp;``id``\
&nbsp;&nbsp;&nbsp;&nbsp;``code``\
&nbsp;&nbsp;&nbsp;&nbsp;``description``  

``person``\
&nbsp;&nbsp;&nbsp;&nbsp;``id``\
&nbsp;&nbsp;&nbsp;&nbsp;``name``\
&nbsp;&nbsp;&nbsp;&nbsp;``genderId``(foreign key to `id` column of `gender`)

Then we would need two entities:

```java
@TableName("gender")
public class Gender extends BaseCodeAndDescriptionEntity {
	// In this case, BaseCodeAndDescriptionEntity 
	// gives us all the columns we need. 
	// If the table grows, we can always add more fields. 
}

@TableName("person")
public class Person extends BaseEntity {
	// In this case, BaseEntity only gives us an id field. 
	// We need to add the rest.
	
	private String name;
	
	// The number only needs to correspond with the number of the foreign key object. 
	@ForeignKey(1)   
	private int genderId;
	
	// That way Java2DB will know from which foreign key to fill this Gender entity from.
	@ForeignKeyObject(1)
	private Gender gender;
	
	//Getters and setters...
}
```

Every entity *must* extend ``BaseEntity``.

Then we can go ahead and create the service classes:

```java
public class GenderService extends BaseService<Gender> {
	public GenderService(){
	    super(Gender.class);	
	}
}

public class PersonService extends BaseService<Person> {
	public PersonService() {
		super(Person.class);
	}
}
```

Every service *must* extend ``BaseService``.

That's it! Now we can access the database using the services with simple predefined methods like ``getById`` and so on. 
Custom methods can be defined in the respective service. 

### Getting started

First, include the Maven artifact. To begin using this library, you need to do two things on program start (or whenever you feel like it, it just may not work then):
1. Connect to the database. Set the static variables ``HOST``, ``DATABASE``, ``USERNAME`` and ``PASSWORD`` of the `DBConnection` class so achieve possibility of connection.
2. Register an instance of all of your services. Use the ``IoC.registerService`` method to do this. Using the above example, it would look something like this: ``IoC.registerService(Person.class, new PersonService());``.

To retrieve an instance of a service, use the ``IoC.resolveService`` method.
  
