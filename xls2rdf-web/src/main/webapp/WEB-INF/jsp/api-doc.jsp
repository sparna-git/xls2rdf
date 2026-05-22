<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<fmt:setLocale value="${sessionData.userLocale.language}"/>
<fmt:setBundle basename="fr.sparna.rdf.xls2rdf.i18n.Bundle"/>

<!DOCTYPE html>
<html>
<head>
    <title><fmt:message key="page.api-doc"/></title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/7.0.1/css/all.min.css" integrity="sha512-2SwdPD6INVrV/lHTZbO2nodKhrnDdJK9/kg2XD1r9uGqPo1cUbujc+IYdlYdEErWNu69gVcYgdxlmVmzTWnetw==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <!--Add Boostrap min.css and Boostrap bundle.min.js // version > 5.3.8-->
    <link href="bootstrap/bootstrap.min.css" rel="stylesheet"/>
    <script src="bootstrap/bootstrap.bundle.min.js"></script>
    <!-- Swagger CSS / SCRIPTS -->
    <script src="${sessionData.baseUrl}/swagger-ui/swagger-ui-bundle.js" charset="UTF-8"> </script>
    <script src="${sessionData.baseUrl}/swagger-ui/swagger-ui-standalone-preset.js" charset="UTF-8"> </script>
    <script src="${sessionData.baseUrl}/swagger-ui//swagger-initializer.js" charset="UTF-8"> </script>
    <link rel="stylesheet" type="text/css" href="${sessionData.baseUrl}/swagger-ui/swagger-ui.css" />
    <!--Custom CSS-->
    <link href="css/custom.css" rel="stylesheet"/>
</head>

<body>

    <jsp:include page="includeTag/header.jsp"/>

    <div class="content">
        <div id="swagger-ui" class="container-fluid"></div>
    </div>


    <jsp:include page="includeTag/footer.jsp"/>
</body>

</html>

