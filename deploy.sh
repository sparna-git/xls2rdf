
scp xls2rdf-rest/target/xls2rdf-rest-3.1.0.war admin@calliope.sparna.fr:xls2rdf-rest.war

ssh -t admin@calliope.sparna.fr 'su -c "\
rm -rf /var/lib/tomcat8/tomcat8-instance0/webapps/rest.war;\
rm -rf /var/lib/tomcat8/tomcat8-instance0/webapps/rest;\
service tomcat8-instance0 stop;\
mv /home/admin/xls2rdf-rest.war /var/lib/tomcat8/tomcat8-instance0/webapps/rest.war;\
service tomcat8-instance0 start;\
"'

