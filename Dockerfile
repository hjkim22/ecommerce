# Build stage
FROM amazoncorretto:17-alpine AS build

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean build --no-daemon --refresh-dependencies -x test

# Run stage
FROM amazoncorretto:17-alpine

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]