<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<fmt:setLocale value="${sessionData.userLocale.language}"/>
<fmt:setBundle basename="fr.sparna.rdf.xls2rdf.i18n.Bundle"/>

<!DOCTYPE html>

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

	<div class="container" style="margin-top:30px;">
		<div class="row">
			<div class="col-lg-12">
				<h1>Excel 2 RDF</h1>
				<p>
					This is an application that converts Excel files to RDF, following the template rules described <a href=""https://skos-play.sparna.fr/play/convert">here</a>.
					It comes in 2 variants : an <a href="${sessionData.baseUrl}/convert">input form</a>, and an API.
				</p>
				<h2>Human input form</h2>
				<p>
					The RDF converter is integrated in <a href="${sessionData.baseUrl}/convert">this input form</a> where you will find a form to upload an Excel file, enter a URL
					or test one of the provided example files. It contains a few options such as getting the result in ZIP, etc.
					<br />
					By default the converter will apply SKOS post-processings, check the corresponding checkbox to disable this.
				</p>
				<h2>API endpoint</h2>
				<p>The API is available at <a href="https://xls2rdf.sparna.fr/web/api/convert"><code>https://xls2rdf.sparna.fr/web/api/convert</code></a></p>
				<h2>API parameters reference</h2>
				The synopsis for calling the API is <code>https://xls2rdf.sparna.fr/web/api/convert?url=http://encoded.url.of.excel.file&noPostprocessings=true</code>. Full list of parameters is given below :
				<table class="table">
					<thead>
						<tr>
							<th>Parameter name</th>
							<th>Required ?</th>
							<th>Description</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><code>url</code></td>
							<td>Required</td>
							<td>URL of the Excel file to convert. 
								<br/>To convert a Google spreadsheet, you can :
								<ol>
									<li>Share the Sheet with everyone ("Everyone with the link" in Sharing options.) It is not possible to convert private Google Sheets.</li>
									<li>Build the Excel download URL of the sheet, which is <code>https://docs.google.com/spreadsheets/d/{ID of your spreadhseet}/export?format=xlsx</code></li>
									<li>Use this as the URL value.</li>
								</ol>
							</td>
						</tr>
						<tr>
							<td><code>noPostProcessings</code></td>
							<td>Optional (recommended)</td>
							<td>Don't apply SKOS post-processings after conversion. Set this to true only if you are explicitely generating SKOS taxonomies.</td>
						</tr>
						<tr>
							<td><code>format</code></td>
							<td>Optional</td>
							<td>Mime type of the RDF output format (e.g. "application/rdf+xml"). Turtle is returned by default</td>
						</tr>
						<tr>
							<td><code>lang</code></td>
							<td>Optional</td>
							<td>Default language to apply to literal columns.</td>
						</tr>
						<tr>
							<td><code>skosxl</code></td>
							<td>Optional</td>
							<td>Apply SKOS-XL post-processings to reify labels.</td>
						</tr>
						<tr>
							<td><code>broaderTransitive</code></td>
							<td>Optional</td>
							<td>Adds skos:broaderTransitive explicit links.</td>
						</tr>
					</tbody>
				</table>
				
				<h2>API example</h2>
				<ol>
					<li>Google spreadsheet at <a href="https://docs.google.com/spreadsheets/d/1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg">https://docs.google.com/spreadsheets/d/1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg</a></li>
					<li>Excel export of the spreadsheet at <a href="https://docs.google.com/spreadsheets/d/1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg/export?format=xlsx">https://docs.google.com/spreadsheets/d/1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg/export?format=xlsx</a></li>
					<li>Result of RDF conversion at <a href="https://xls2rdf.sparna.fr/web/api/convert?url=https%3A%2F%2Fdocs.google.com%2Fspreadsheets%2Fd%2F1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg%2Fexport%3Fformat%3Dxlsx&noPostProcessings=true"><code>https://xls2rdf.sparna.fr/rest/convert?url=https%3A%2F%2Fdocs.google.com%2Fspreadsheets%2Fd%2F1S5K7cl-8-JFyqnCZP2pVhI3-E4IK0XgCPElkr657jJg%2Fexport%3Fformat%3Dxlsx&noPostProcessings=true</code></a></li>
				</ol>
				
				<h2>Can I use this API in production ?</h2>
				<p>
					Of course not, you fool ! Please refer to <a href="https://github.com/sparna-git/xls2rdf">the Github repository</a> where you will find the open-source code of the converter
					as well as a command-line version.
				</p>

			</div>
		</div>
	</div>

	<script src="https://cdn.jsdelivr.net/npm/anchor-js/anchor.min.js"></script>
        
    <script>
      anchors.options.placement = 'left';
      anchors.add("h2,h3,h4");
    </script>

	<jsp:include page="includeTag/footer.jsp"/>

</body>

</html>