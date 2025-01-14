FROM maven:3-eclipse-temurin-21 AS build
RUN mkdir /build
WORKDIR /build
COPY ./pom.xml pom.xml

# fetch all dependencies
RUN mvn dependency:go-offline -B

# copy sources and compile
COPY ./src src
RUN mvn clean package -B -Dmaven.test.skip=true

FROM eclipse-temurin:21-alpine AS run

# Remember to add extra files if needed by your project
COPY instances /instances
COPY *.csv *.tsv /
COPY --from=build /root/.m2 /root/.m2
COPY --from=build /build/target/*.jar /app.jar

CMD [\
"java",\
"--enable-preview",\
# TODO Set memory usage, default to 4 GB
"-Xmx4G", "-Xms4G",\
"-jar", "/app.jar"\
]
