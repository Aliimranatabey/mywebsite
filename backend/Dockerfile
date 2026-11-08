# Build Stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Uygulama root olarak çalışmasın; bir açık bulunsa bile container'da yetkisi sınırlı kalır.
# uploads klasörü burada oluşturulur ki yeni volume bu sahiplikle başlasın.
RUN useradd --system --uid 10001 --no-create-home app \
    && mkdir -p /app/uploads && chown app /app/uploads
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
