<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:setLocale value="${sessionData.userLocale.language}"/>
<fmt:setBundle basename="fr.sparna.rdf.xls2rdf.i18n.Bundle"/>

<nav id="menu" class="navbar navbar-expand-lg bg-dark sticky-top">
    <div class="container">
      <a class="navbar-brand fw-normal" href="${sessionData.baseUrl}/"><i class="fa-regular fa-house" style="color: #df6919; font-size: 25px"></i> &nbsp;Excel 2 RDF</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
         <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">
            <li class="nav-item">
              <a id="api" class="nav-link active ${view eq 'api' ? 'highlight' : ''}" href="${sessionData.baseUrl}/">Home</a>
            </li>
            <li class="nav-item">
              <a id="doc" class="nav-link active ${view eq 'doc' ? 'highlight' : ''}" href="${sessionData.baseUrl}/doc">Excel structure documentation</a>
            </li>
            <li class="nav-item">
              <a id="convert" class="nav-link active ${view eq 'convert' ? 'highlight' : ''}" href="${sessionData.baseUrl}/convert">Convert</a>
            </li>
          </ul>
          <ul class="navbar-nav d-flex justify-content-end align-items-baseline">
            <li class="nav-item dropdown">
            <a id="glob_img" class="nav-link dropdown-toggle" data-bs-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false">
            <i class="fa-solid fa-earth-americas" style="color: #df6919; font-size: 25px;"></i>
             <fmt:message key="lang"/></a>
          <div class="dropdown-menu">
            <a class="dropdown-item" href="?lang=fr">fr</a>
            <a class="dropdown-item" href="?lang=en">en</a>
            <a class="dropdown-item" href="?lang=de">de</a>
          </div>
        </li>
        <li class="nav-item">
          <a class="nav-link active" href="https://github.com/sparna-git/xls2rdf">
                <i class="fa-brands fa-github fa-rotate-by" style="color: #df6919; font-size: 25px"></i>
          </a>
        </li>
          </ul>
        </div>
    </div>
</nav>

