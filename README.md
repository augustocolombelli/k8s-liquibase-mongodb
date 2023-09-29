# k8s-liquibase-mongodb
This project is an example using Liquibase, MongoDB and Kubernates. The purpose is to test the integration of this tools.

## Pushing an image of product service
The first step, is necessary to push an image of product to docker hub (or other repository). There is a file name Makefile in `Makefile`. This file have the command necessary to push de image.

## Starting the services
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
![image](https://github.com/augustocolombelli/k8s-liquibase-mongodb/assets/20463205/8f2c4a37-ad0d-41cb-82eb-1811f5bce92f)

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
