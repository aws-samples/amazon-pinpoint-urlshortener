FROM openjdk:11
ENV region ap-south-1
COPY /target/urlshortener.jar /usr/src/app/urlshortener.jar
EXPOSE 80
ENTRYPOINT ["java","-jar","/usr/src/app/urlshortener.jar","${region}"]