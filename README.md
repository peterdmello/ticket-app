# Description
Coding exercise application

#Building
## Pre-requisites
gradle - You must have gradle installed with JDK 8

`gradle build`

This will compile the source code into the `build` directory (also runs tests which may take some time).

# Running the code
The only way to run the code is via tests. If interested, a simple solution is to create a test and perform a bunch of operations. Since an ExecutorService is used, it runs in daemon mode and hence you should be able to write test scenarios and verify results. Most of the business logic is in `TicketServiceImpl.java` and `Event.java`.

# Running Tests
There are both unit tests and integration tests. The integration tests may take a little long. Use `gradle -x test build` to skip all tests in case they fail or seem to take too long.

`gradle test`

# Implementation Notes
Most of the documentation is present in the source code describing what was done and why. Here are some general things about the code:
* Most of the classes have been created to be immutable (except the service)
* Ideally, the service should be stateless, but, we've stored the `holdsCollection`, `reservations` and `seatLevels` in it just for this exercise.
* It should be relatively easy to add Spring dependency injection using constructor autowiring in `TicketServieImpl`
* The unit and integration tests only check business logic and not performance or scalability

# Development
To import the project into Eclipse, run

`gradle eclipse`

# TODO
* REST API - I'll probably create a branch and make a REST api after submission, just for fun and testing since this was a interesting exercise :)
* Code coverage could have been improved with more mocking.
* Separate integration tests from unit tests using new source package and gradle sourceSets
* JaCoCo Code coverage on project
* Load test using JMeter - write custom dataset provider which will maintain its own state and run it against the REST API endpoints
