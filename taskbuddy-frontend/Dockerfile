# Stage 1: Build - Compilăm proiectul TypeScript/React
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Run - Servim fișierele cu Nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# Configurația Nginx folosește mecanismul oficial de templates:
# variabila ${BACKEND_URL} e înlocuită la pornire (image-ul oficial nginx
# rulează envsubst pe /etc/nginx/templates/*.template).
# Asta ne permite să folosim aceeași imagine pe AWS, GCP și Azure,
# dând doar BACKEND_URL diferit ca variabilă de mediu.
# Default-ul păstrează comportamentul vechi pentru docker-compose local.
ENV BACKEND_URL=http://backend:8080
# Cloud Run seteaza PORT=8080; default-ul pastreaza portul 80 pt docker-compose.
# NGINX_ENVSUBST_FILTER lasa envsubst sa inlocuiasca doar BACKEND_URL si PORT
# (altfel ar inlocui si $host/$remote_addr, dar nu si ${VAR} fara default).
ENV PORT=80
# Host-ul trimis backend-ului prin proxy (Cloud Run routeaza dupa Host).
# In docker-compose e alias-ul de retea 'backend'; in Cloud Run e domeniul
# backend-ului, dat ca variabila BACKEND_HOST in pipeline.
ENV BACKEND_HOST=backend
ENV NGINX_ENVSUBST_FILTER="^(BACKEND_URL|PORT|BACKEND_HOST)$"
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]