FROM azul/zulu-openjdk-alpine:17-jre-headless

# Remember to add extra files if needed by your project
COPY instances /instances
COPY target/__RNAME__*.jar /app.jar

CMD [\
"/usr/bin/java",\
"--add-opens", "java.base/java.util=ALL-UNNAMED",\
"--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",\
"--add-opens", "java.base/java.lang=ALL-UNNAMED",\
# Set memory usage
"-Xmx1G", "-Xms1G",\
"-jar", "/app.jar"\
]
