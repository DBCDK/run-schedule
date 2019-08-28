run-schedule
============

A Java library providing a parser and evaluator for unix-like cron 
expressions.

Cron expressions are comprised of 5 required fields separated by white space. 
The fields respectively are described as follows:

| Field Name   | Allowed Values  | Allowed Special Characters |
|--------------|-----------------|----------------------------|
| minute       | 0-59            | , - * /                    |
| hour         | 0-23            | , - * /                    |
| day-of-month | 1-31            | , - * ? /                  |
| month        | 1-12 or JAN-DEC | , - * /                    |
| day-of-week  | 1-7 or SUN-SAT  | , - * ? /                  |

The * character is used to specify all values. For example, * in the hour
field means "every hour".

The ? character is allowed for the day-of-month and day-of-week fields. It
is used to specify 'no specific value'. This is useful when you need to 
specify something in one of the two fields, but not the other.

The - character is used to specify ranges For example 1-5 in the minute
field means "the minutes 1, 2, 3, 4, and 5".

The , character is used to specify additional values. For example 
"MON,WED,FRI" in the day-of-week field means "the days Monday, Wednesday,
and Friday".

The / character is used to specify increments. For example "0/6" in the
hour field means "the hours 0, 6, 12, and 18".

The legal characters and the names of months and days of the week are
not case sensitive.

### usage

Add the dependency to your Maven pom.xml

```xml
<dependency>
  <groupId>dk.dbc</groupId>
  <artifactId>run-schedule</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

In your Java code

```java
import dk.dbc.util.RunSchedule;

final RunSchedule runSchedule = new RunSchedule("45 * * * *");
if (runSchedule.isSatisfiedBy(Instant.now())) {
    // do something if instant satisfies the run schedule
}
if (runSchedule.isSatisfiedBy(Instant.now(), lastRun)) {
    // do something if instant satisfies the run schedule
    // and the lastRun time is not within the last 60 seconds
}
if (runSchedule.isSatisfiedBy(instant) 
    || runSchedule.isOverdue(instant, lastRun)) {
    // do something if instant satisfies the run schedule
    // or is overdue when comparing the given last run time 
    // with the expected last run time according to the schedule
}
```

### development

**Requirements**

To build this project JDK 1.8 or higher and Apache Maven is required.

### License

Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3.
See license text in LICENSE.txt