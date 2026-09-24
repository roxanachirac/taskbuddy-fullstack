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
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]