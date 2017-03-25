![alt text](https://travis-ci.org/vkorobkov/jfixtures.svg?branch=master "Build status")
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.rsql/rsql-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.vkorobkov/jfixtures)

# JFixtures

## Preface
Nowadays almost every project has acceptance/integration/other tests which use a dedicated test database with some test 
data inside. JFixtures is a small java lib that helps to define the test data in a human-readable YAML format and to translate 
the data into a plain SQL. At some degree it is a port of 
[Ruby On Rails fixtures](http://api.rubyonrails.org/v3.2/classes/ActiveRecord/Fixtures.html) but for java world
(or any other JVM language such as groovy, scala, e.t.c.)

Disclaimer: only generation of PG SQL(and compatible) is supported at the moment.

## That's wrong with SQL ?

Of course, all the test data could be defined as a SQL script(or scripts), so why JFixtures?

Because describing test data in SQL constructions is inconvenient:
 
* It it hard to match values to columns:
```sql
INSERT INTO users(id, first_name, last_name, middle_name age, sex, is_admin, is_guest) VALUES (5, 'Vladimir', 'Korobkov',
'Vadimovich', 'm', 29, true, false); 
```
Imagine, when your're typing and your cursor is somewhere in the middle of `VALUES (...)` part of the statement, it is
hard enough to understand which value belongs to which column.
The more insert statements/more columns you have, the harder understanding and maintainability of such code.

* Hard management of references:
```sql
INSERT INTO comment(id, ticket_id, user_id, text) VALUES (1, 4, 8, 'Hello, world');
```
Table `comment` has two foreign keys: ticket_id and user_id. You need to match foreign keys of the `comment` table to 
primary keys of the referred tables manually which is not intuitive and looks really unreadable - you need to lookup
to other place of the script for get the values of ticket_id/user_id.

* Tables order matters and you need to take it into account. The _referred_ tables go first, the _referring_ tables go 
last. That means poor developer has to remember the whole tables hierarchy.

* It is verbose - for each row you need to duplicate all this ceremony: `INSERT INTO <table> (...) VALUES(...)`

## JFixtures way

* Human readable test data description with a set of yaml files
* SQL scrip as result
* Human readable, defined by user, string keys for each row instead of numeric IDs
* Numeric PK's are auto generated, however, user can specify them manually
* Foreign key values get calculated automatically(see example below)
* Table references are defined explicitly in a special `.conf.yml` file.
* Tables in output SQL script appear in the right order(according to references between the tavles)
* Early errors detection: fixture processing fails on sytax errors, circular references, incorrect foreign key value,
e.t.c.
* A small java library with only dependency(org.yaml:snakeyaml)

JFixtures offers you to populate a number of readable YAML files with test data and than it converts these files into a 
SQL script. Let's define the following tables: user and ticket. Each user could be a reporter and an assignee of any
ticket.

Let's create an empty folder and add 2 new YML files - one file per table: user.yml and ticket.yml. 

user.yml:
```yaml
vlad:
    first_name: Vladimir
    second_name: Korobkov
    age: 29
    role: admin
alex:
    first_name: Alex
    second_name: Krasnov
    age: 20
    role: developer
    
```

ticket.yml:
```yaml
skeleton:
    reporter: vlad
    title: Project skeleton
    text: To create a project skeleton and push into github
    assignee: vlad
tests:
    reporter: vlad
    title: Include a test framework
    text: To include spock framework into pom.xml and to create a dummy unit test
    assignee: alex
```

Now we need to describe the relations between the tables in a special file:

.conf.yml:
```yaml
refs:
    ticket:
        reporter: user # ticket.reporter refers to user.id
        assignee: user # ticket.assignee refers to user.id
```

That's is really all - now we can generate a valid SQL file with all the test data. We just need a few lines of
java code(later I am planning to create also an executable JAR file and a maven plugin for doing that):
```java
import com.github.vkorobkov.jfixtures.JFixtures;

JFixtures.postgres("/path/to/fixtures/folder").toFile("output.sql");
```

That's all! Output SQL file will contain all the required INSERT instructions or correct order, with correct 
primary/foreign keys and with `DELETE FROM <table>` instruction for cleaning up every table before inserting a new
test data.````

No hard magic here - yml file names get converted as they are(but without .yml extension) into table names.
Each row has a human readable key like `vlad` and `alex` for `user` table and like `skeleton` and `tests`
for `ticket` table. These keys get converted into a numeric PK columns named `id` for each table. Foreign key values 
get resolved using row keys and table relation definitions from `.conf.yml:`. Tables are getting sorted accordingly: 
the _referred_ tables go first, the _referring_ tables go last. Circular dependencies get detected and an exception
will be thrown.


## Maven / Requirements / Dependencies
JFixtures is available on maven central: 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.rsql/rsql-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.vkorobkov/jfixtures)

**JFixtures requires Java 8** or higher and it is written in Java 8.

JFixtures itself has only one "compile" time dependency - it is `org.yaml:snakeyaml:jar:1.17:compile`. 
All other dependencies in the tree are for tests only:
```
com.github.vkorobkov:jfixtures:jar:1.0.2
+- org.yaml:snakeyaml:jar:1.17:compile
+- org.projectlombok:lombok:jar:1.16.14:provided
+- org.spockframework:spock-core:jar:1.1-groovy-2.4-rc-3:test
|  +- org.codehaus.groovy:groovy-all:jar:2.4.6:test
|  \- junit:junit:jar:4.12:test
|     \- org.hamcrest:hamcrest-core:jar:1.3:test
+- nl.jqno.equalsverifier:equalsverifier:jar:2.2.1:test
\- cglib:cglib-nodep:jar:3.2.5:test
```

## Usage
Once you included a dependency into your project, you're ready to use JFixtures.
Generally speaking, JFixtures is just a text process - it expect to receive a folder with fixtures as an input and it 
writes a SQL file as the output.

See how it works:
```java
import com.github.vkorobkov.jfixtures.JFixtures;

JFixtures.postgres("/path/to/fixtures").toFile("test-data.sql");
```

So this code will scan for YML fixtures in `/path/to/fixtures` folder and will write the output into `test-data.sql`
file. If output file had presented before you launched the processing, it will be _recreated_.

It is also possible to get SQL instructions as a string rather than as a file, for example, if you want to execute the 
sql against already opened SQL connection in your custom code:
```java
import com.github.vkorobkov.jfixtures.JFixtures;

String sqlInstructions = JFixtures.postgres("/path/to/fixtures").asString();
```
