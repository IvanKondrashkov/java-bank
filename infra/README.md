# kubectl — шпаргалка по диагностике
## Обзор ресурсов в namespace
```bash
# Всё: pods, services, deployments, statefulsets, jobs + secrets, pvc, networkpolicies, pdb, sa
kubectl get all,secret,pvc,networkpolicy,pdb,sa -n test -o wide

# Кратко (только pods)
kubectl get pods -n test -o wide

# С событиями
kubectl get events -n test --sort-by='.lastTimestamp'
```

## Логи
```bash
# Логи пода
kubectl logs <pod-name> -n test

# Логи предыдущего (упавшего) контейнера
kubectl logs <pod-name> -n test --previous

# Логи с follow
kubectl logs -f <pod-name> -n test

# Логи всех контейнеров в поде
kubectl logs <pod-name> -n test --all-containers=true

# Логи Job (контейнер по умолчанию)
kubectl logs job/<job-name> -n test
```

## Описание ресурсов (describe)
```bash
kubectl describe pod <pod-name> -n test
kubectl describe node <node-name>
kubectl describe svc <service-name> -n test
kubectl describe deployment <deployment-name> -n test
kubectl describe pvc <pvc-name> -n test
```

## Проверка состояния приложения
```bash
# Readiness / Liveness
kubectl get pods -n test -o custom-columns='NAME:.metadata.name,READY:.status.containerStatuses[*].ready,RESTARTS:.status.containerStatuses[*].restartCount'

# Endpoints (подключённые к Service поды)
kubectl get endpoints -n test

# События в namespace
kubectl get events -n test -w
```

## Helm
```bash
helm list -n test
helm status bank -n test
helm history bank -n test
helm get values bank -n test
```

## Exec в под
```bash
kubectl exec -it <pod-name> -n test -- /bin/sh
kubectl exec <pod-name> -n test -- env
```

## Отладка сети / DNS
```bash
# Тест DNS
kubectl run -it --rm debug --image=busybox --restart=Never -n test -- nslookup bank-postgresql

# Тест доступа к сервису
kubectl run -it --rm debug --image=curlimages/curl --restart=Never -n test -- curl -v http://bank-keycloak:80
```

## Память и ресурсы нод
```bash
kubectl top nodes
kubectl top pods -n test
```

## Удаление / очистка
```bash
# Удалить failed/completed pods
kubectl delete pods -n test --field-selector=status.phase=Failed

# Удалить completed Jobs
kubectl delete jobs -n test --field-selector=status.successful=1
```