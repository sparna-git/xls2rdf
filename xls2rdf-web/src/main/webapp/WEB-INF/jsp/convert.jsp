<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" 	prefix="fmt" 	%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" 	prefix="c" 		%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<!-- setup the locale for the messages based on the language in the session -->
<fmt:setLocale value="${sessionData.userLocale.language}"/>
<fmt:setBundle basename="fr.sparna.rdf.xls2rdf.i18n.Bundle"/>

<html>
  <head>

    <title>TITRE A METTRE</title>
    <link rel="canonical" href="https://xls2rdf.sparna.fr/web/" />
		<meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <!--FONTAWSEOME CDN-->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/7.0.1/css/all.min.css" integrity="sha512-2SwdPD6INVrV/lHTZbO2nodKhrnDdJK9/kg2XD1r9uGqPo1cUbujc+IYdlYdEErWNu69gVcYgdxlmVmzTWnetw==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <!--Add Boostrap min.css and Boostrap bundle.min.js // version > 5.3.8-->
		<link href="bootstrap/bootstrap.min.css" rel="stylesheet">
    <script src="bootstrap/bootstrap.bundle.min.js"></script>
    <!--Custom CSS-->
    <link href="css/custom.css" rel="stylesheet">
    <!--event Listener-->
   
		<script>
		      function enabledInput(selected) {
		      				document.getElementById('source-' + selected).checked = true;
		      				document.getElementById('url').disabled = selected != 'url';
		      				document.getElementById('example').disabled = selected != 'example';
		      				document.getElementById('file').disabled = selected != 'file';		
		      			}	

					function handleDownloadExample(select, downloadLink) {
               const selectedOption = select.options[select.selectedIndex];

               const valueHref  = selectedOption.value;
               const optionText = selectedOption.text;

               downloadLink.textContent = optionText;
               downloadLink.href = valueHref;
          }

          document.addEventListener("DOMContentLoaded", () => {
            const downloadLink = document.getElementById("lien");
                
            document.addEventListener("change", (event) => {
              const target = event.target;
              if (target.id === "example") {
                handleDownloadExample(target, downloadLink);
              }
            });
          });

		</script>
  
  </head>
    <body>

    <jsp:include page="includeTag/header.jsp"/>


    
		<!-- Error block -->
			<div class="container mt-4">
			  <div class="messages">
			    <c:if test="${formData.errorMessage != null || error != null}">
			      <div class="fw-bold alert alert-danger alert-dismissible fade show" role="alert" style="background-color: #df6919; border-radius: 5px;">
			        <h5 class="alert-heading">
			          <fmt:message key="error" />
			        </h5>
              <!--Si l'exception Xls2RdfException est levée l'attribut ${error} s'affiche sinon ${formData.errorMessage} pour Xls2RdfConverSION-->
			        ${formData.errorMessage}${error}
			        <button type="button"
			                class="btn-close"
			                data-bs-dismiss="alert"
			                aria-label="Close">
			        </button>
			      </div>
			    </c:if>	
			  </div>
			</div>
      
  
    <div class="container mt-5">
      <form action="convert" method="post" enctype="multipart/form-data">
        <!--START FIELSET = SOURCE-->
        <fieldset id="fieldset-source" class="row">
          <h2 class="fs-5">
            <i class="fa-solid fa-upload" style="color: #df6919; font-size: 25px"></i> &nbsp;<fmt:message key="convert.form.legend" />
          </h2>
          <!--START PARTIES RADIO BUTTON + LABEL + INPUT-->
          <div class="row gy-3">
            <!--START SOURCE RADIO/INPUT = EXAMPLE-->
              <div class="col-4 form-check">
                <input
                class="form-check-input"
                type="radio"
                id="source-example"
                name="source"
                checked="checked"
                onchange="enabledInput('example')"
                value="example">
                <label class="form-label fw-bold"for="example">
                  <fmt:message key="convert.form.providedExample" />
                </label>
              </div>
              <div class="col-6">
                <select class="form-select" name="example" id="example" onchange="enabledInput('example')">
                    <option value="${sessionData.baseUrl}/excel_test/excel2skos-exemple-1.xlsx" selected>Example 1 (simple exemple, in english)</option>
							      <option value="${sessionData.baseUrl}/excel_test/excel2skos-exemple-2.xlsx">Example 2 (prefixes)</option>
							      <option value="${sessionData.baseUrl}/excel_test/excel2skos-exemple-3.xlsx">Example 3 (multilingual columns and deprecation)</option>
							      <option value="${sessionData.baseUrl}/excel_test/excel2skos-exemple-4.xlsx">Example 4 (schema.org, datatypes, multiple sheets)</option>
							      <option value="${sessionData.baseUrl}/excel_test/excel2skos-exemple-5.xlsx">Example 5 (skos:Collection, inverse columns)</option>
							      <option value="${sessionData.baseUrl}/excel_test/excel2skos-exemple-6.xlsx">Example 6 (skos:OrderedCollection, dealing with rdf:Lists)</option>
							      <option value="${sessionData.baseUrl}/excel_test/excel2skos-exemple-7.xlsx">Example 7 (different subjects with subjectColumn parameter)</option>
							      <option value="${sessionData.baseUrl}/excel_test/excel2skos-exemple-8.xlsx">Example 8 (ease references with lookupColumn parameter)</option>
							      <option value="${sessionData.baseUrl}/excel_test/excel2skos-use-case-1.xlsx">Use-case 1 (real-world thesaurus : maintaining concept hierarchy in Excel)</option>
							      <option value="${sessionData.baseUrl}/excel_test/excel2skos-use-case-2.xlsx">Use-case 2 (real-world person authority file)</option>  
							      <option value="${sessionData.baseUrl}/excel_test/excel2skos-use-case-3.xlsx">Use-case 3 (real-world SHACL constraints)</option>
							      <option value="${sessionData.baseUrl}/excel_test/excel2skos-use-case-4.xlsx">Use-case 4 (Metadata template from fairdatacollective.com)</option>
                </select>
                  <div class="form-text fst-italic"><fmt:message key="convert.form.Example.download"/>&nbsp;
                      <a id="lien" href="${sessionData.baseUrl}/excel_test/excel2skos-exemple-1.xlsx">Example 1 (simple exemple, in english)</a>
                  </div>
              </div>
            <!--END SOURCE RADIO/INPUT = EXAMPLE-->
            <!--START SOURCE RADIO/INPUT = LOCALFILE-->
              <div class="col-4 form-check">
                <input
                class="form-check-input"
                type="radio"
                id="source-file"
                name="source"
                value="file"
                onchange="enabledInput('file')">
                <label class="form-label fw-bold"for="file">
                  <fmt:message key="convert.form.localFile"/>
                </label>
              </div>
              <div class="col-6">
                

                
                 <input
                  class="form-control"
                  type="file"
                  name="file" 
                  id="file"
                  onchange="enabledInput('file')"/>
                  <div class="
                  form-text 
                  fst-italic">
                  <fmt:message key="convert.form.localFile.help"/>
                  </div>
              </div>
            <!--END SOURCE RADIO/INPUT = LOCALFILE--> 
            <!--START SOURCE RADIO/INPUT = WEB-->
              <div class="col-4 form-check">
                <input
                class="form-check-input"
                type="radio"
                id="source-url"
                name="source"
                value="url"
                onchange="enabledInput('url')"/>
                <label class="form-label fw-bold" for="url">
                  <fmt:message key="convert.form.remoteUrl"/>
                </label>
              </div>
              <div class="col-6">
                  <input
                  class="form-control"
                  type="url"
                  name="url" 
                  id="url" 
                  onchange="enabledInput('url')"
                  placeholder="http://..."/>
                      <div class="text-wrap text-break form-text fst-italic">
                        <fmt:message key="convert.form.remoteUrl.help"/>
                      </div>
              </div>
            <!--END SOURCE RADIO/INPUT = WEB-->
          </div>
          <!--END PARTIES RADIO BUTTON + LABEL + INPUT-->
        
        </fieldset>
        <!--END FIELSET = SOURCE-->
        <!--FIELDSET=LANGUAGE-->
        <fieldset id="fieldset-language" class="row mt-5 align-items-baseline justify-content-around">
          <h2 class="col-12 fs-5 mb-5">
            <i class="fa-solid fa-language" style="color: #df6919; font-size: 25px"></i> &nbsp;<fmt:message key="convert.form.legend.language"/>
          </h2>
          <label class="col-4 fw-bold text-center text-sm-start" for="choice_Language">
            <fmt:message key="convert.form.language.legend"/>
          </label>
            <div class="col-4">
              <select class="col-3 form-select" id="choice_Language" name="language">
                <option value="" selected></option>	
							  <option value="de">de</option>
							  <option value="en">en</option>
							  <option value="es">es</option>	
							  <option value="fr">fr</option>
							  <option value="it">it</option>
							  <option value="ru">ru</option>
              </select>
            </div>
          
        </fieldset>
        <!--FIELDSET=OPTIONS-->
        <fieldset id="fieldset-option" class="row mt-5">
          <div class="accordion" id="accordionOptions">
            <div class="accordion-item" style="border-radius: 5px;">
              <h2 class="accordion-header" id="headingOne">
                
                <button class="accordion-button fw-bold bg-dark" type="button"
                        data-bs-toggle="collapse"
                        data-bs-target="#collapseOne"
                        aria-expanded="true"
                        aria-controls="collapseOne" style="border-radius: 4px;">
                <i class="fa-solid fa-screwdriver-wrench" style="color: #df6919; font-size: 25px"></i> &nbsp;<fmt:message key="convert.form.advanced.legend"/>
                </button>
              </h2>
                <div id="collapseOne"
                          class="accordion-collapse collapse show"
                          aria-labelledby="headingOne"
                          data-bs-parent="#accordionOptions">
                  <div class="
                  row 
                  accordion-body">
                  <!--START FORMAT OUTPUT-->
                  <div class="row mb-4">
                    <label class="col-6 form-label fw-bold" for="output">
                      <fmt:message key="convert.form.outputFormat.legend"/>
                    </label>
                    <div class="col-4">
                      <select class="form-select" required id="output" name="output" >						
									      <option value="text/turtle" selected>Turtle</option>
									      <option value="application/rdf+xml">RDF/XML</option>	 
									      <option value="text/plain">N-Triples</option>
									      <option value="text/x-nquads">N-Quads</option>
									      <option value="text/n3">N3</option>
									      <option value="application/x-trig">TriG</option>							 
								      </select>
                    </div>
                  </div>
                  <!--END FORMAT OUTPUT--> 
                  <!--START LABELS OUTPUT-->
                  <div class="row mb-4 fw-bold">
                    <label class="col-6 form-label" for="useskosxl">
                      <fmt:message key="convert.form.useskosxl"/>
                    </label>
                    <div class="col-4 form-check ms-3">
                        <input
                          class="form-check-input"
                          type="checkbox"
                          id="useskosxl"
                          name="useskosxl"/>
                    </div>
                  </div>
                  <!--END LABELS OUTPUT--> 
                  <!--START TRANSITIVE OUTPUT-->
                  <div class="row mb-4">
                    <label class="col-6 form-label fw-bold" for="broaderTransitive">
                      <fmt:message key="convert.form.broaderTransitive"/>
                    </label>
                    <div class="col-4 form-check ms-3">
                        <input
                          class="form-check-input"
                          type="checkbox"
                          id="broaderTransitive"
                          name="broaderTransitive"/>
                    </div>
                  </div>
                  <!--END TRANSITIVE OUTPUT--> 
                  <!--START ZIP OUTPUT-->
                  <div class="row mb-4">
                    <label class="col-6 form-label fw-bold" for="usezip">
                      <fmt:message key="convert.form.usezip"/>
                    </label>
                    <div class="col-4 form-check ms-3">
                        <input
                          class="form-check-input"
                          type="checkbox"
                          id="usezip"
                          name="usezip"/>
                    </div>
                  </div>
                  <!--END ZIP OUTPUT-->
                  <!--START ZIP OUTPUT-->
                  <div class="row">
                    <label class="col-6 form-label fw-bold" for="ignorePostProc">
                      <fmt:message key="convert.form.ignorepostproc"/>
                    </label>
                    <div class="col-4 form-check ms-3">
                        <input
                          class="form-check-input"
                          type="checkbox"
                          id="ignorePostProc"
                          name="useignorePostProczip"/>
                    </div>
                  </div>
                  <!--END ZIP OUTPUT--> 
                  </div>
                </div>
                <!--END FORMAT OUTPUT-->
                </div>
            </div>

        </fieldset>

        <div class="row justify-content-center mt-5">
            <div class="col-auto mb-5">
              <button class="
              btn
              btn-lg
              btn-info" 
              type="submit" id="submit">
              <fmt:message key="convert"/>
            </button>
            </div>
        </div>
      </form>
    </div>

    <jsp:include page="includeTag/footer.jsp"/>


  </body>
</html>
