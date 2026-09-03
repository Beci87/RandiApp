# 1. Lépés: Olyan környezetet kérünk, amiben van Java fordító (JDK)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Másoljuk a fájlokat és lefordítjuk a Java kódot
COPY RandiAlkalmazas.java .
RUN javac RandiAlkalmazas.java

# 2. Lépés: Egy tiszta, kicsi futtató környezetet készítünk
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Átmásoljuk a lefordított .class fájlt és a webes fájlokat
COPY --from=build /app/RandiAlkalmazas*.class .
COPY index.html .
COPY style.css .

# Jelezzük a portot (a Render automatikusan kezeli)
EXPOSE 8090

# Elindítjuk a programot
CMD ["java", "RandiAlkalmazas"]
