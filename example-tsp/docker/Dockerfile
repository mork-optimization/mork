FROM azul/zulu-openjdk-alpine:17-jre-headless

# Remember to add extra files if needed by your project
COPY instances /instances
COPY target/TSP*.jar /app.jar

CMD [\
"/usr/bin/java",\
"--enable-preview",\
# Set memory usage
"-Xmx1G", "-Xms1G",\
"-jar", "/app.jar"\
]
