removeImages:
	docker rmi augustocolombelli/product-service:v1

startServices:
	kubectl apply -f ./k8s/

deleteServices:
	kubectl delete -f ./k8s/

getAll:
	kubectl get all

testGetProducts:
	curl --location --request GET 'http://localhost:30080/products'
