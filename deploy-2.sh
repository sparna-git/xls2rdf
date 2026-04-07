# Transfer
scp -i /home/thomas/.ssh/sparna-keypair-francfort.pem xls2rdf-web/target/xls2rdf-web-4.0.0.war ubuntu@92.243.27.145:xls2rdf-web.war

# Redeploy
ssh -i /home/thomas/.ssh/sparna-keypair-francfort.pem ubuntu@92.243.27.145 'sudo su -c "\
service tomcat10 stop
rm -rf /var/lib/tomcat10/webapps/web.war
rm -rf /var/lib/tomcat10/webapps/web
mv /home/ubuntu/xls2rdf-web.war /var/lib/tomcat10/webapps/web.war
service tomcat10 start"'