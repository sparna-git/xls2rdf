<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<fmt:setLocale value="${sessionData.userLocale.language}"/>
<fmt:setBundle basename="fr.sparna.rdf.xls2rdf.i18n.Bundle"/>

<!DOCTYPE html>
<html>

<head>
	<title><fmt:message key="page.api"/></title>

	<!--FONTAWSEOME CDN-->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/7.0.1/css/all.min.css" integrity="sha512-2SwdPD6INVrV/lHTZbO2nodKhrnDdJK9/kg2XD1r9uGqPo1cUbujc+IYdlYdEErWNu69gVcYgdxlmVmzTWnetw==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <!--Add Boostrap min.css and Boostrap bundle.min.js // version > 5.3.8-->
	<link href="bootstrap/bootstrap.min.css" rel="stylesheet">
    <script src="bootstrap/bootstrap.bundle.min.js"></script>
	<!--Custom CSS-->
    <link href="css/custom.css" rel="stylesheet">

</head>

<body>

	<jsp:include page="includeTag/header.jsp"/>

	<div class="content">

		<div class="container" style="margin-top:30px;">
			<div class="row">
				<div class="col-lg-12">
					<h1>Excel 2 RDF</h1>
					<p>
						This is an open-source application that converts Excel or ODS files to RDF, following the template rules described in <a href="${sessionData.baseUrl}/doc">the documentation</a>.
						It comes in 4 variants : 
					</p>
					<ul>
						<li>An <a href="${sessionData.baseUrl}/convert">input form</a></li>
						<li>An <a href="${sessionData.baseUrl}/api-doc">API</a> (GET / POST)</li>
						<li>A <a href="https://github.com/sparna-git/xls2rdf/wiki/Command-line-Excel-to-RDF-conversion">command-line tool</a></li>
						<li>A <a href="https://github.com/sparna-git/xls2rdf/releases">Java library that you can integrate in your application</a></li>
					</ul>
					<p>Check-out the <a href="https://github.com/sparna-git/xls2rdf/raw/refs/heads/master/documentation/xls2rdf-presentation.pptx"><b>introduction slides</b></a> for more information.</p>

					<h2>Human input form</h2>
					<p>
						The RDF converter is integrated in <a href="${sessionData.baseUrl}/convert">this input form</a> where you will find a form to upload an Excel / ODS file, enter a URL
						or test one of the provided example files. It contains a few options such as getting the result in ZIP, etc.
						<br />
						By default the converter will apply SKOS post-processings, check the corresponding checkbox to disable this.
					</p>
					
					<h2>Working with google spreadsheets</h2>
					<p></p>To convert a Google spreadsheet, you can :</p>
					<ol>
						<li>Share the Sheet with everyone ("Everyone with the link" in Sharing options.) It is not possible to convert private Google Sheets.</li>
						<li>Build the Excel download URL of the sheet, which is <code>https://docs.google.com/spreadsheets/d/{ID of your spreadhseet}/export?format=xlsx</code></li>
						<li>Use this as the URL value.</li>
					</ol>
					<p>For example:</p>
					<ol>
						<li>Google spreadsheet at <a href="https://docs.google.com/spreadsheets/d/1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg">https://docs.google.com/spreadsheets/d/1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg</a></li>
						<li>Excel export of the spreadsheet at <a href="https://docs.google.com/spreadsheets/d/1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg/export?format=xlsx">https://docs.google.com/spreadsheets/d/1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg/export?format=xlsx</a></li>
						<li>Result of RDF conversion at <a href="https://xls2rdf.sparna.fr/web/api/convert?url=https%3A%2F%2Fdocs.google.com%2Fspreadsheets%2Fd%2F1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg%2Fexport%3Fformat%3Dxlsx&noPostProcessings=true"><code>https://xls2rdf.sparna.fr/rest/convert?url=https%3A%2F%2Fdocs.google.com%2Fspreadsheets%2Fd%2F1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg%2Fexport%3Fformat%3Dxlsx&noPostProcessings=true</code></a></li>
					</ol>
					
					<h2>Can I use the API in production ?</h2>
					<p>
						Of course not, you fool ! Please refer to <a href="https://github.com/sparna-git/xls2rdf">the Github repository</a> where you will find the open-source code of the converter
						as well as a command-line version.
					</p>

				</div>
			</div>
		</div>

	</div>

	<script src="https://cdn.jsdelivr.net/npm/anchor-js/anchor.min.js"></script>
        
    <script>
      anchors.options = {
		placement: 'left',
		visible: 'hover'
	  }
      anchors.add("h2,h3,h4");
    </script>

	<jsp:include page="includeTag/footer.jsp"/>

</body>

</html>