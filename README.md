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
