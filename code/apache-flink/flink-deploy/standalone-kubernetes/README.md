# Commands

kubectl get pods -n inventpoc-ns

kubectl create -f flink-configuration-configmap.yaml -n inventpoc-ns
kubectl create -f jobmanager-service.yaml -n inventpoc-ns
kubectl create -f jobmanager-session-deployment-non-ha.yaml -n inventpoc-ns
kubectl create -f taskmanager-session-deployment.yaml -n inventpoc-ns

kubectl apply -f flink-configuration-configmap.yaml -n inventpoc-ns
kubectl apply -f jobmanager-service.yaml -n inventpoc-ns
kubectl apply -f jobmanager-session-deployment-non-ha.yaml -n inventpoc-ns
kubectl apply -f taskmanager-session-deployment.yaml -n inventpoc-ns


kubectl delete -f jobmanager-service.yaml -n inventpoc-ns
kubectl delete -f flink-configuration-configmap.yaml -n inventpoc-ns
kubectl delete -f taskmanager-session-deployment.yaml -n inventpoc-ns
kubectl delete -f jobmanager-session-deployment-non-ha.yaml -n inventpoc-ns