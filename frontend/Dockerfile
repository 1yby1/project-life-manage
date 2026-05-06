# ============================================================================
# 前端镜像 — Multi-stage build
#   1. builder: Node 20 + npm + vite build → dist/
#   2. runtime: nginx:alpine serve dist + reverse-proxy /api/* & /uploads/* → backend
# ============================================================================

# --- Stage 1: build ---
FROM node:20-alpine AS builder
WORKDIR /build

# package.json 单独拷以利用层缓存
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

# 拷源码并构建
COPY . .
RUN npm run build

# --- Stage 2: runtime ---
FROM nginx:alpine

# 自定义 SPA + reverse-proxy 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 静态资源
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
