# Transfer
scp -i /home/thomas/.ssh/sparna-keypair-francfort.pem xls2rdf-rest/target/xls2rdf-rest-4.0.0.war ubuntu@92.243.27.145:xls2rdf-rest.war

# Redeploy
ssh -i /home/thomas/.ssh/sparna-keypair-francfort.pem ubuntu@92.243.27.145 'sudo su -c "\
service tomcat10 stop
rm -rf /var/lib/tomcat10/webapps/rest.war
rm -rf /var/lib/tomcat10/webapps/rest
mv /home/ubuntu/xls2rdf-rest.war /var/lib/tomcat10/webapps/rest.war
service tomcat10 start"'