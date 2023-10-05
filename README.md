# Spring Boot, Liquibase, MongoDB and k8s
To have more control and version management over changes made to the database structure, a database schema change management tool is one approach that can help by introducing in-application code versioning for database changes.

For example, considering a scenario where the application has some indexes or schema validators. Adding these changes to the database manually is not a good approach, for example, in a scenario where with more than one environment it is necessary to replicate the changes manually to each of them. To solve this problem, there are some tools that can help manage scripts using code, an example is [Liquibase](https://www.liquibase.org/).

This project is an example using Liquibase, MongoDB and Kubernates. The purpose is to test the integration of this tools.

## Table of contents

* [Technologies](#technologies)
* [Getting started](#getting-started)
* [Adding Liquibase in a Legacy System](#adding-liquibase-in-a-legacy-system)
* [Troubleshooting](#troubleshooting)

## Technologies
* Java
* Spring Boot
* k8s
* MongoDB

## Getting started
On this project, a Java service named product-service will be used. It's a simple service that connects to MongoDB and runs some scripts with the purpose to create some collections, schema validators and indexes. It's used k8s to start more than one pod for same database.

The scripts used to run are on the `product-service/src/main/resources/db/changelog directory`.

### Pushing an image of product service
The first step, is necessary to push an image of product to docker hub (or other repository). There is a file name Makefile in `Makefile`. This file have the command necessary to push de image.

### Starting the services
After the service pushed, the next step is necessary to start the application with k8s. For this, it's necessary to have the k8s ready in our machine, [Rancher](https://rancherdesktop.io/) can be a option for this.

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

## Adding Liquibase in a Legacy System
Considering that some services already have a database running, there are some cases where it is necessary to synchronize the Liquibase scripts with the current status of the database. The first step to achieve this is to map the current status of the database. Map the collections, indexes, schema validators, etc.

There are more than one approaches that can be used to sync the Liquibase with the current status of the database. This article explain more about each approach [Adding Liquibase to an Existing Project | Start Using Liquibase](https://www.liquibase.com/blog/adding-liquibase-on-an-existing-project). Below are more details using the command sync approach.


### Download and install the Liquibase
The first step is to download and install the Liquibase. The Liquibase can be downloaded in the url [Download Liquibase | Liquibase.com](https://www.liquibase.org/DOWNLOAD). After installed, check if Liquibase is available with command `liquibase -version` .

### Add libs to Liquibase
The next step is to add the libs used by Liquibase to synchronize with Mongo DB. This libs are available in the zip file on this project and needs to be in the lib folder `/usr/local/opt/liquibase/lib`. With this step, Liquibase is ready to run the `liquibase changelog-sync`. 
![image](https://github.com/augustocolombelli/k8s-liquibase-mongodb/assets/20463205/da8c03b3-d05f-4635-89bf-eb09c91ea54a)

### Change-sets using the current database status
The next step is to prepare the scripts, using the current status of the database mapped. Considering the current status of the service, it was created three new files in the product-service/src/main/resources/db/changelog:

### Running the command to sync
It’s necessary to create a temporary file named `liquibase.properties` in the `product-service/src/main/resources directory`. This file is used just in the first time, to synchronize the Liquibase, it’s not necessary to push it to the repository. Below are more details about the file.

```
changelog-file=liquibase-changelog.xml
url=mongodb://<USERT>:<PASSWORD>@localhost:27017/database_name_test?ssl=false&tlsAllowInvalidHostnames=true&serverSelectionTimeoutMS=2000
log-level=DEBUG
driver=liquibase.ext.mongodb.database.MongoClientDriver
```

With this file created, the last step is to run the command `liquibase changelog-sync` in the same directory. Running the command, it will be created the two new collections DATABASECHANGELOG and DATABASECHANGELOGLOCK. In the DATABASECHANGELOG will be added the documents considering the scripts. The DATABASECHANGELOGLOCK is used to manage the Liquibase when the service uses more than one pod, in this case, when a pod is running the Liquibase scripts, the others pods will wait to start the service.

## Troubleshooting
### Roles to run collMod command
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


