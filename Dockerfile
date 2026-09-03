FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY RandiAlkalmazas.java .
RUN javac RandiAlkalmazas.java

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/RandiAlkalmazas*.class .
COPY index.html .
COPY style.css .

EXPOSE 8090

CMD ["java", "RandiAlkalmazas"]
