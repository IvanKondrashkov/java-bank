#!/bin/bash
set -e

if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

if [ -z "$DOCKER_REGISTRY" ]; then
  echo "DOCKER_REGISTRY не задан в .env"
  exit 1
fi

echo "Using DOCKER_REGISTRY: $DOCKER_REGISTRY"

echo "Uninstalling Helm releases (bank = umbrella with postgresql, keycloak, rabbitmq)..."
for ns in dev test prod; do
  helm uninstall bank -n "$ns" --wait --timeout 120s 2>/dev/null || true
done

echo "Deleting namespaces (удаляет все ресурсы, включая orphaned)..."
for ns in dev test prod; do
  kubectl delete ns "$ns" --ignore-not-found --wait --timeout=120s 2>/dev/null || true
done

echo "Shutting down Jenkins..."
docker compose down -v || true
docker stop jenkins && docker rm jenkins || true
docker volume rm jenkins_home || true

echo "Removing images..."
docker image rm ${DOCKER_REGISTRY}/account-service:1 || true
docker image rm ${DOCKER_REGISTRY}/cash-service:1 || true
docker image rm ${DOCKER_REGISTRY}/transfer-service:1 || true
docker image rm ${DOCKER_REGISTRY}/notifications-service:1 || true
docker image rm ${DOCKER_REGISTRY}/gateway-service:1 || true
docker image rm ${DOCKER_REGISTRY}/front-service:1 || true
docker image rm jenkins/jenkins:lts-jdk21 || true

#echo "Pruning system..."
minikube ssh -- docker image prune -a

echo "Done! All clean."