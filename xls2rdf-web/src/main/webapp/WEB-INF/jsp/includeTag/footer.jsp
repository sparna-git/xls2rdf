<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:setLocale value="${sessionData.userLocale.language}"/>
<fmt:setBundle basename="fr.sparna.rdf.xls2rdf.i18n.Bundle"/>

<div id="footer" class="container-fluid bg-dark">
    <div class="row justify-content-center align-items-baseline text-center p-2">
      <div class="col-auto text-secondary text-center">
              <span>Xls2Rdf by</span> 
                    <a href="https://sparna.fr" target="_blank"><img id="footer-logo" src="images/sparna.png" /></a>
              &nbsp;|&nbsp;
              <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.fr">
                <img alt="Licence Creative Commons" style="border-width:0" src="http://i.creativecommons.org/l/by-sa/3.0/88x31.png"/>
              </a>
              &nbsp;|&nbsp;
              <span class="fst-italic">version : ${applicationData.buildVersion} (${applicationData.buildTimestamp})</span> 
              <a href="https://github.com/sparna-git/xls2rdf">
                <i class="fa-brands fa-github fa-rotate-by" style="color: #df6919; font-size: 25px"></i>
              </a>
        </div>
     </div>
</div>

