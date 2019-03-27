
# Java2DB
This little badboy allows you to communicate with your database without writing a single line of SQL in your application.\
While this library may not offer the most fancy and crazy features, that is not what it is intended for. It was created to be lightweight and meant for a quick and simple access to your database. As of now, MySQL is the supported dialect.

Feature requests are always welcome. Just open an issue. Really, don't hesitate. Also, if my documentation is missing something or unclear, please let me know.

If you are completely lost or have no idea how to use this library (or both), feel free to [contact me](mailto:collinalpert@gmail.com). I would be happy to assist you with any problems you might have.

**Please note:** This is a Java 10 library. Make sure you have Java 10 installed when using this library. This library is not suitable for projects requiring complex SQL queries, although it does offer some advanced features.\
It is meant for projects which want to interact with their database in a simple, but easy way, without bloating the source code with SQL queries.

## Introduction
POJOs imitate tables on the database where every field in the POJO is equivalent to a table column. Every class has a corresponding service class, which acts as a data service and interacts with the database. It is possible to define custom methods in the respective service classes to retrieve specific data. The service classes will fill the POJO with values from the database using a default mapper. The mapping functionality is explained [further down](#custom-mapping).\
Enough said. Let's get you set up.

### Setup
Include the Maven artifact:
```xml
<dependency>
    <groupId>com.github.collinalpert</groupId>
    <artifactId>java2db</artifactId>
    <version>4.1</version>
</dependency>
```
Or include the [JAR](https://github.com/CollinAlpert/Java2DB/releases/latest) in your project. 

### Example
Lets say we have a database with two tables that have the following structure:

`gender`\
&nbsp;&nbsp;&nbsp;&nbsp;`id`\
&nbsp;&nbsp;&nbsp;&nbsp;`code`\
&nbsp;&nbsp;&nbsp;&nbsp;`description`\
&nbsp;&nbsp;&nbsp;&nbsp;`isBinary`

`person`\
&nbsp;&nbsp;&nbsp;&nbsp;`id`\
&nbsp;&nbsp;&nbsp;&nbsp;`name`\
&nbsp;&nbsp;&nbsp;&nbsp;`age`\
&nbsp;&nbsp;&nbsp;&nbsp;`genderId` (foreign key to `id` column of `gender`)

Then we would need two POJOs:

```java
@TableName("gender")
public class Gender extends BaseEntity {
	// BaseEntity gives us the id field.
	// We need to add the rest.

	private String code;
	private String description;
	private boolean isBinary;
	
	// Getters and setters...
}
```

```java
@TableName("person")
public class Person extends BaseEntity {
	//BaseEntity gives us the id field.
	// We need to add the rest.
	
	private String name;
	private int age;
	private long genderId;
	
	// When annotating a field with the @ForeignKeyEntity attribute,
	// you are telling Java2DB that this is a POJO that depicts the
	// foreign key relationship. It will be automatically filled with the according value.
	// Adding this sort of field is completely optional.
	@ForeignKeyEntity("genderId")
	private Gender gender;
	
	// Getters and setters...
	// Note that it is not suggested for foreign key POJOs to have setters. 
	// They are effectively useless since they are only retrieved from the database.
}
```

Every POJO *must* extend `BaseEntity` and have an empty or default constructor.\
If you want to have a field in your POJO that should be ignored by Java2DB, you can apply the `Ignore` attribute to the specific field.

Then we can go ahead and create the service classes:

```java
public class GenderService extends BaseService<Gender> {
	
}
```

```java
public class PersonService extends BaseService<Person> {
    public List<Person> getAdults() {
	return getMultiple(p -> p.getAge() >= 18).orderBy(Person::getName).toList();
    }
}
```

Every service *must* extend `BaseService`.

That's it! Now we can get data from the database using the services using simple methods like `getById` and so on.\
As you can see from the example, custom methods can be defined in the respective service using the `getSingle` or `getMultiple` methods provided by the `BaseService`.\

The last thing you need to do is give Java2DB access to your database. Set the static variables `HOST`, `DATABASE`, `USERNAME`, `PASSWORD` and optionally `PORT` of the `DBConnection` class to achieve possibility of connection.

## Features

### CRUD operations 

#### Create
Every service class has support for creating a single as well as multiple entities at once on the database.
Check out the different `create` methods provided by your service class.
To achieve asynchronous behavior, please read the [Asynchronous operations](#asynchronous-operations) section.

#### Read
The `BaseService` provides a `createQuery` method which allows you to manually build a query and then execute it with the `toList`, `toStream` or `toArray` methods. You should only need this approach seldomly.\
Much rather, use the `getSingle` or `getMultiple` methods. `getMultiple` returns a `Query` object with a preconfigured WHERE condition and then allows you to chain some additional query options. As of the current `Query` version, WHERE, LIMIT and ORDER BY are supported. With the ORDER BY functionality, there is also the possibility to coalesce multiple columns when ordering. Effectively, the calls `createQuery().where(predicate)` and `getMultiple(predicate)` are the same. The latter is recommended.\
As previously mentioned, to execute the query and retrieve a result, use the `toList`, `toStream` or `toArray` methods.

#### Update
Every service class has support for updating a single as well as multiple entities at once on the database.
Check out the different `update` methods provided by your service class. 
To achieve asynchronous behavior, please read the [Asynchronous operations](#asynchronous-operations) section.
To reduce overhead, there is also an update which changes the value for a single column. An example would look something like this:
`service.update(entity.getId(), Person::getAge, 25)`. This would change a specific person's age to 25. 

#### Delete
Every service class has support for deleting a single as well as multiple entities at once on the database. 
To achieve asynchronous behavior, please read the [Asynchronous operations](#asynchronous-operations) section.
Check out the different `delete` methods provided by your service class.
To include support for soft-deletion, please read the [Common structures](#common-structures) section.

### LIKE operations
It is also possible to achieve `LIKE` operations using the String `startsWith`, `endsWith` and `contains` methods in a predicate. This, in the context of the `PersonService` from the [example](#example), would look something like this:\
`getMultiple(p -> person.getName().startsWith("A"));`. The generated WHERE clause would be ``where `person`.name LIKE 'A%'``.

### Counting
For counting functionality, the `BaseService` provides a `count` method. You can use it to either count all rows in a table, or to count all rows which match a certain condition. E.g. `personService.count()` would return the total number of people while `personService.count(person -> person.getAge() >= 50)` would return the amount of people that are of age 50 and older in your table.

### Existential conditions
If you would like to check if a certain record exists in a table, you can use the `any` method provided by the `BaseService`.\
Using the above [example](#example), the usages would look something like this: `personService.any(person -> person.getName() == "Steve")`.\
You can also check if a table has at least one row by calling `personService.any()`.

### Duplicate value checking
To check if a column's values are unique in a table, use the `hasDuplicates` method provided by the `BaseService`. It will return `true` if there is at least one duplicate value and false if all the values are unique.

### Asynchronous operations
As of version 4.0 it is possible to execute all of the CRUD operations asynchronously. 
To use the asynchronous methods with your service classes, the individual service class should inherit from the 
`AsyncBaseService`. You wil find all the methods that the `BaseService` has plus every method with an `Async` suffix. 
That is the asynchronous version.\
The asynchronous versions of methods which have a return value, e.g. `create`, `count` or `any`, accept a `Consumer` which defines an action for the value once it is computed asynchronously. 
If you do not wish to use the computed value, e.g. for the `create` method, the `CallbackUtils` class offers an `empty()` method, which returns an empty `Consumer` that just does nothing. 
Use this as an argument in the asynchronous methods, when needed.

### Enums for static values
Lets suppose you are using a "mood" in which you have certain moods (happy, sad, mad, etc.) stored. Now, to describe the mood 
of a person you would obviously reference this table via a foreign key and add the `@ForeignKeyEntity` attribute to a POJO field you have defined for the "mood" table.
For static tables, meaning tables which just contain informational values e.g. statuses which do not change, you can define an enum to keep track of the values.
That way a complete entity is not always needed. Using the above example, it would look something like this:

```java
// An enum representing used in combination with the @ForeignKeyEntity must implement IdentifiableEnum. 
public enum MoodTypes implements IdentifiableEnum {
	
    HAPPY(1), SAD(2), MAD(3);
	
    private final int id;
	
    MoodTypes(int id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return this.id;
    }
}
```

```java
@TableName("person")
public class Person extends BaseEntity {
	
    // All of the above fields.
    
    private int moodId;
    
    // You can still use the foreign key entity if you like, 
    // but it becomes sort of redundant as soon as you use the enum.
    @ForeignKeyEntity("moodId")
    private Mood mood;
    
    // The enum value will be set to the value corresponding with the "moodId".
    @ForeignKeyEntity("moodId")
    private MoodTypes moodType;
}
``` 

### Column name deviations
To be able to target any column naming conventions, it is possible to explicitly tell Java2DB which table column a POJO field targets with the `@ColumnName` attribute. Simply apply the attribute to a field.
Note that when supplying a different name for a foreign key, the `@ForeignKeyEntity` annotation must still point to the name of the actual table's foreign key column. Here's an example in which the gender foreign key is not suffixed with "id" on the database: 
```java
@TableName("person")
public class Person extends BaseEntity {
	
	private String name;
	private int age;
	
	//The "gender" field corresponds to the table column "genderId" on the database.
	@ColumnName("genderId")
	private long gender;
	
	//The foreign key entity still refers to the actual column name.
	@ForeignKeyEntity("genderId")
	private Gender genderObject;
	
	// Getters and setters...
}
```

### Query constraints
Java2DB offers full support for default query constraints.\
This means you can tell Java2DB to execute a certain WHERE condition with *every* query that is executed on a specific table.\
Here's an example: Say every query executed on the `person` table should only return people of age 18 and older and with an id greater than 0 (just because). This can be achieved adding query constraints using the `QueryConstraints` class.\
In our case, this would look something like this:

```java
public class Main {
	public static void main(String[] args){
	    // register the services and all that
	    QueryConstraints.addConstraint(Person.class, person -> person.getAge() >= 18);
	    QueryConstraints.addConstraint(Person.class, person -> person.getId() >= 0);
	    // Now, every DQL query executed to the person table will contain these conditions.
	}
}
```
This is useful for cases where deleted records in a table are marked by an `isDeleted` flag (or something similar) and these records should never return in any query.

Of course, nothing prevents you from concatenating the predicates to something like this: `QueryConstraints.addConstraint(Person.class, person -> person.getAge() >= 18 && person.getId() >= 0);`

### Pagination
In case you are interested in pagination, Java2DB also offers support for that. Since I am assuming you already know what pagination is, when reading this section, I will not explain it.\
To receive a `PaginationResult`, use one of the `createPagination` methods from the `BaseService`. The result will allow you to get a certain page. The database query will only be executed when you request a page, in order to minimize data transfer of data that might not be needed. It would be unnecessary to load all of the pages if only the first one will be viewed by the user.\
You also have option to add caching to the pagination. To do this, simply add a cache expiry duration to the `createPagination` method and you will receive a `CacheablePaginationResult`. When getting pages which you have previously requested, they will be loaded from the cache, which can significantly reduce loading times. This will only happen as long as the expiry duration is not over yet. 
After that, the page will be re-loaded from the database and loaded into the cache. Caching for pages will not work if the pages were fetched asynchronously.\
You also have the option to invalidate/clear the caches and trigger a fresh reload the next time a page is requested.\
In case you want to add an ORDER BY statement to your pagination queries, you can do this on the `PaginationResult`. This will effect the pages in an overlapping manner and not just each page separately. 

### Executing plain SQL
If you still feel the need that you need to perform plain SQL queries, maybe because one of your queries is more complex or because this library is missing a feature (in which case, please let me know), this is still possible.
Using the `DBConnection` class, you can execute SQL queries and also receive a `ResultSet` which you can then work with. It spares you the hassle of manually creating a connection and preparing statements etc. Here's a basic example that executes a DML statement:

```jshelllanguage
try (var connection = new DBConnection()) {
    connection.update(Files.lines(Paths.get("path/to/file.sql")).collect(Collectors.joining()));
}
```

If you are trying to retrieve execute a more complex DQL statement and want a `ResultSet`, you can use the `execute` method from the `DBConnection` class.

### Common structures
Since there are some columns that are very common in database tables, Java2DB ships some base classes you can use in order to tackle some of the redundancy.\
Entities modeling tables which feature a code and a description of some sort could benefit from using the `BaseCodeAndDescriptionEntity` in combination with the `BaseCodeAndDescriptionService`.\
Entities modeling tables which feature support for "soft-deletion" could benefit from using the `BaseDeletableEntity` in combination with the `BaseDeletableService`.\
These two extended options are also available in combination with each other.

### Miscellaneous 
- If you would not like your queries logged in the console, use the `DBConnection.LOG_QUERIES = false;` statement on program start.
- In case Java2DB can't establish a connection with your database, it will throw a `ConnectionFailedException`. You can catch it and perform your own handling.

## Using Inversion of Control (IoC)

### Custom mapping

This is an advanced feature and completely optional.\
You can define your own mappings by implementing the `IMapper` interface and registering your custom mapper with the `IoC.registerMapper` method. When you register a mapper, it will be used when mapping the database result into POJOs.

### Following the singleton pattern for services
This feature is also optional and solely exists to promote good programming practice.\
Only one instance of a data service is really needed in the life cycle of an application. It has no behavior that could change, since it only transfers data, nothing else.\
Because of this, it is possible to register an instance of a service that can be retrieved at a later point in time and as many times as you want. Use the `IoC.registerService` method to do this. Using the above [example](#example), it would look something like this: ``IoC.registerService(Person.class, new PersonService());``. Now that you have registered an instance of a service, you can retrieve it someplace in your application using the `IoC.resolveService` call. This will return the exact instance of the service you registered.

