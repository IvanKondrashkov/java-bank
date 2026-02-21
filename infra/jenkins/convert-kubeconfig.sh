#!/bin/sh
# Скрипт для конвертации jenkins_kubeconfig.yaml в формат с встроенными base64 сертификатами
# Запуск: ./convert-kubeconfig.sh
# Только Linux

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MINIKUBE_PATH="${HOME}/.minikube"
CLIENT_CRT="${MINIKUBE_PATH}/profiles/minikube/client.crt"
CLIENT_KEY="${MINIKUBE_PATH}/profiles/minikube/client.key"
OUT_FILE="${SCRIPT_DIR}/jenkins_kubeconfig.yaml"

if [ ! -f "$CLIENT_CRT" ]; then
  echo "Ошибка: не найден $CLIENT_CRT"
  exit 1
fi
if [ ! -f "$CLIENT_KEY" ]; then
  echo "Ошибка: не найден $CLIENT_KEY"
  exit 1
fi

# Примечание: certificate-authority-data не используем — нельзя сочетать с insecure-skip-tls-verify
CLIENT_CRT_DATA="$(base64 -w 0 "$CLIENT_CRT")"
CLIENT_KEY_DATA="$(base64 -w 0 "$CLIENT_KEY")"

# Порт Minikube API — берём из текущего kubeconfig (напр. https://127.0.0.1:64848)
MINIKUBE_PORT=58558
if command -v kubectl >/dev/null 2>&1; then
  SERVER=$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}' 2>/dev/null)
  if [ -n "$SERVER" ]; then
    PORT=$(echo "$SERVER" | sed -n 's/.*:\([0-9]*\)$/\1/p')
    [ -n "$PORT" ] && MINIKUBE_PORT="$PORT"
  fi
fi

cat > "$OUT_FILE" << EOF
apiVersion: v1
clusters:
- cluster:
    server: https://host.docker.internal:${MINIKUBE_PORT}
    insecure-skip-tls-verify: true
  name: minikube
contexts:
- context:
    cluster: minikube
    namespace: default
    user: minikube
  name: minikube
current-context: minikube
kind: Config
preferences: {}
users:
- name: minikube
  user:
    client-certificate-data: ${CLIENT_CRT_DATA}
    client-key-data: ${CLIENT_KEY_DATA}
EOF

echo "Готово: $OUT_FILE"