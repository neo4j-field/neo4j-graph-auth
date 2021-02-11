# a graph-based authorization plugin

Are you tired of a *flat*, *boring*, role-based access control?

Do you dream of *dynamic*, *inheritable* roles and permissions?

Do you question why the most advanced graph database on the planet uses a
simplistic User->Role based approach, akin to POSIX users and groups?

Do you have any idea what I just said? (No? oh,...sorry)

**Introducing:** native security graphs for securing your user graphs!

![security-graph](./security-graph.png?raw=true)

## Building
You need either the local Neo4j EE jars in your local maven repo or need to set
up access to the hosted private maven repo. This mostly just means you need to
create a `${HOME}/.gradle/gradle.properties` file containing:

```properties
neo4j_enterprise_source_user=neo4j-enterprise-sources
neo4j_enterprise_source_password=<SUPER_SECRET_PASSWORD>
```

> Talk to Dave V. or someone else in the field team to get the password ;-)

Then run:

```
$ ./gradlew jar
```

## Using
Right now this is simple...things are hardcoded...but I did provide a
[Dockerfile](./Dockerfile) you can use to fire up a local instance.

```
$ docker build -t neo4j-graph-auth:latest . 
```

> Note: if you use Intellij, I also included a pre-configured configuration
> for running both the gradle task and Docker in the `.docker-run` directory.

Then start your engines:

```
$  docker run -p 7474:7474 -p 7687:7687 \
  --name neo4j-graph-auth-test neo4j-graph-auth-test:latest  
```

> Note: most of the docker env vars for Neo4j were baked into the Dockerfile
> for ease of testing.

## Testing
You should end up with a database that has a `neo4j` user with password set to
`password`. Log in via Browser or Cypher-Shell.

As the `neo4j` users, Create two new users: `dave` and `sam`

```cypher
CREATE USER dave SET PASSWORD "password" CHANGE NOT REQUIRED;
CREATE USER sam SET PASSWORD "password" CHANGE NOT REQUIRED;
```

Both users already have group memberships in the graph, so do a 
`:server disconnect` and then log in as one or the other.

You should see you get roles granted based on the `security` graph!
