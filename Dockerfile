FROM maven:3.10.0-amazoncorretto-27-alpine AS builder

WORKDIR /build

COPY ./ ./

RUN mvn package

FROM tomcat:jre17-temurin-resolute

RUN apt update -y

RUN rm -rf /usr/local/tomwer/webapps/*

COPY --from=builder /build/xls2rdf-web/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]