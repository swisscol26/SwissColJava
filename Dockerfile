# Render construye esta imagen en sus servidores; no requiere Docker local.
FROM node:22-bookworm-slim AS panel
WORKDIR /panel
COPY frontend-productos/package*.json ./
RUN npm ci
COPY frontend-productos/ ./
RUN npm run build

FROM alpine/git:latest AS tienda
WORKDIR /tienda
# Version fija del repositorio publico: actualizar al publicar cambios de la tienda.
RUN git init && git remote add origin https://github.com/swisscol26/Swisscol-2026.git \
    && git fetch --depth 1 origin 161bb4c659425bfa4aa7c8fb960dc187ea7a54c5 \
    && git checkout --detach FETCH_HEAD

FROM eclipse-temurin:21-jdk-jammy AS java
WORKDIR /build
COPY lib/ lib/
COPY src/ src/
RUN mkdir classes && find src -name '*.java' > sources.txt \
    && javac -encoding UTF-8 -cp 'lib/*' -d classes @sources.txt

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=java /build/classes/ classes/
COPY --from=java /build/lib/ lib/
COPY --from=tienda /tienda/index.html web/index.html
COPY --from=tienda /tienda/css/ web/css/
COPY --from=tienda /tienda/js/ web/js/
COPY --from=tienda /tienda/img/ web/img/
COPY --from=panel /panel/dist/index.html web/admin/index.html
COPY --from=panel /panel/dist/assets/ web/assets/
ENV SWISSCOL_WEB_ROOT=/app/web SWISSCOL_BIND_ADDRESS=0.0.0.0
USER 10001:10001
CMD ["java", "-XX:MaxRAMPercentage=60", "-cp", "classes:lib/*", "api.ServidorApi"]
