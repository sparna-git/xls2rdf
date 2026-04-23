<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:setLocale value="${sessionData.userLocale.language}"/>
<fmt:setBundle basename="fr.sparna.rdf.xls2rdf.i18n.Bundle"/>

<div id="footer" class="container-fluid bg-dark">
    <div class="row justify-content-center align-items-baseline text-center p-2">
      <div class="col-auto text-secondary text-center">
              <span>Xls2Rdf by</span> 
                    <a class="text-decoration-none" href="http://blog.sparna.fr" target="_blank"><span style="color: #4285f4;">Thomas Francart</span></a>, 
                    <a href="http://sparna.fr" target="_blank"><img id="footer-logo" src="images/sparna.png" /></a>
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

	<script>
		const h1 = document.querySelectorAll("h1");
		const h2 = document.querySelectorAll("h2");
		const h3 = document.querySelectorAll("h3");
		const h4 = document.querySelectorAll("h4");

		Array.from(h1).forEach(h => {h.classList.add("display-3")});
		Array.from(h2).forEach(h => {h.classList.add("display-3")});
		Array.from(h3).forEach(h => {h.classList.add("display-6")});
		Array.from(h4).forEach(h => {h.classList.add("display-4")});
	</script>
