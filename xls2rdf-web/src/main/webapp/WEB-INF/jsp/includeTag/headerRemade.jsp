<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top">
    <div class="container">
      <a class="navbar-brand" href="${sessionData.baseUrl}/"><i class="fa-regular  fa-house" style="color: #df6919; font-size: 25px"></i> &nbsp;Excel 2 RDF</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
         <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">
            <li class="nav-item">
              <a class="nav-link active" href="${sessionData.baseUrl}/">API</a>
            </li>
            <li class="nav-item">
              <a class="nav-link active" href="${sessionData.baseUrl}/doc">Excel structure documentation</a>
            </li>
            <li class="nav-item">
              <a class="nav-link active" href="${sessionData.baseUrl}/convert">Convert</a>
            </li>
          </ul>
        </div>
    </div>
</nav>