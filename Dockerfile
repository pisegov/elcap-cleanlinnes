FROM amazoncorretto:17-alpine-jdk

# Create an application directory
RUN mkdir -p /app
RUN mkdir -p /app/database

# The /app directory should act as the main application directory
WORKDIR /app

# Copy or project directory (locally) in the current directory of our docker image (/app)
COPY build/libs/*.jar ./app.jar

# Start the app
CMD [ "java", "-jar", "./app.jar" ]
