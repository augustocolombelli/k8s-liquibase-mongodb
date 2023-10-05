# k8s-liquibase-mongodb
To have more control and version management over changes made to the database structure, a database schema change management tool is one approach that can help by introducing in-application code versioning for database changes.

For example, considering a scenario where the application has some indexes or schema validators. Adding these changes to the database manually is not a good approach, for example, in a scenario where with more than one environment it is necessary to replicate the changes manually to each of them. To solve this problem, there are some tools that can help manage scripts using code, an example is [Liquibase](https://www.liquibase.org/).

This project is an example using Liquibase, MongoDB and Kubernates. The purpose is to test the integration of this tools.

## Getting started
On this project, a Java service named product-service will be used. It's a simple service that connects to MongoDB and runs some scripts with the purpose to create some collections, schema validators and indexes. It's used k8s to start more than one pod for same database.

The scripts used to run are on the product-service/src/main/resources/db/changelog directory.

### Pushing an image of product service
The first step, is necessary to push an image of product to docker hub (or other repository). There is a file name Makefile in `Makefile`. This file have the command necessary to push de image.

### Starting the services
After the service pushed, the next step is necessary to start the application with k8s. For this, it's necessary to have the k8s ready in our machine, Rancher can be a option for this.

There is a file name Makefile with all commands necessary to run the services.

Remove the image of product service:
> make removeImages

Start the services
> make startServices

Delete services
> make deleteServices

Get information about services
> make getAll

Test if product service is running
> make testGetProducts

# Adding Liquibase in a Legacy System
Reference: https://www.liquibase.com/blog/adding-liquibase-on-an-existing-project

- Download the Liquibase (only files): https://www.liquibase.org/DOWNLOAD
- Add the libs in the lib folder. (See the libs attached on this repo)
![image](https://github.com/augustocolombelli/k8s-liquibase-mongodb/assets/20463205/7f9847c7-8c3a-4f62-aa5f-a33b0152cbf9)


Add in the file "liquibase.properties" the configurations, as below:
```
changelog-file=liquibase-changelog.xml
url=mongodb://localhost:27017/database_name_test?ssl=false&tlsAllowInvalidHostnames=true&serverSelectionTimeoutMS=2000
log-level=DEBUG
driver=liquibase.ext.mongodb.database.MongoClientDriver
```
Run the command below:
> ./liquibase changelog-sync

If everything is OK, the tables responsible to mange the database script version will be created with the files that is in in the liquibase-changelog.xml

The defaul package that liquibase is installed on mac is `/usr/local/opt/liquibase`

## Roles to run collMod command
When we try to run que command changelog-sync or try to start the application, we received and error because the user do not have the privilege to run collMod command. This happens, because liquibase needs to add some validators to the collections DATABASECHANGELOG and DATABASECHANGELOGLOCK.

To solve this, it's necessary to add this privileges to the user used to changelog-sync and the user used to connect to the database in the service. It's possible to create a specific role just for these two collections and avoid to add more roles that is necessary. For this, first it's necessary to create the roles as demonstraded below:

```
use service-db-name
db.createRole(
   {
     role: "collmod-databasechangelog", 
     privileges: [
       {
         actions: [ "collMod" ],
         resource: { db: "service-db-name", collection: "DATABASECHANGELOG" }
       }
     ],
     roles: []
   }
)
```
```
db.createRole(
   {
     role: "collmod-databasechangeloglock", 
     privileges: [
       {
         actions: [ "collMod" ],
         resource: { db: "service-db-name", collection: "DATABASECHANGELOGLOCK" }
       }
     ],
     roles: []
   }
)
```
After this, it's necessary to add this role for the user with the command below:
```
use service-db-name
db.grantRolesToUser(
   "liquibase-collmod-user",
   [ { role: "collmod-databasechangelog", db: "service-db-name" } ],
   { w: "majority" , wtimeout: 4000 }
)
```
```
db.grantRolesToUser(
   "liquibase-collmod-user",
   [ { role: "collmod-databasechangeloglock", db: "service-db-name" } ],
   { w: "majority" , wtimeout: 4000 }
)
```
If the local container is not validating the user roles in the tests, it's necessary to start the container with --auth as below. Using this approach, it's possible to simulate eventual issues considering the user privileges.
```
  mongodb:
    command: [--auth]
    restart: unless-stopped
    build:
      context: localstack
      dockerfile: Dockerfile.mongodb
    environment:
      key: "value"
    ports:
      - "27017:27017"
```


