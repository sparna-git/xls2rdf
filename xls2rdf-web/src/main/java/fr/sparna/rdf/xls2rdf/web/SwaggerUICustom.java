package fr.sparna.rdf.xls2rdf.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SwaggerUICustom extends SwaggerIndexPageTransformer {

    public SwaggerUICustom(SwaggerUiConfigProperties swaggerUiConfig, SwaggerUiOAuthProperties swaggerUiOAuthProperties, SwaggerWelcomeCommon swaggerWelcomeCommon, ObjectMapperProvider objectMapperProvider) {
        super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerWelcomeCommon, objectMapperProvider);
    }

    @Override
    public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformerChain) throws IOException {

        if (resource.toString().contains("swagger-ui.css")) {
            final InputStream is = resource.getInputStream();
            String css = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            css += """
/* =========================================================
   CUSTOM SWAGGER UI THEME OVERRIDES
   ========================================================= */

/* Masquage de la top bar et du sélecteur de serveur */
.swagger-ui .topbar,
.swagger-ui .scheme-container {
    display: none;
}

/* global */
.swagger-ui,
.swagger-ui .opblock .opblock-summary-path-description-wrapper,
.swagger-ui .opblock-tag.no-desc span,
.swagger-ui table thead tr td,
.swagger-ui table thead tr th,
.swagger-ui .parameter__name,
.swagger-ui .parameter__in,
.swagger-ui .parameter__type,
.swagger-ui a.nostyle,
.swagger-ui a.nostyle:visited,
.swagger-ui .parameters-col_description .renderedMarkdown p,
.swagger-ui .info li,
.swagger-ui .info p,
.swagger-ui .info table,
.swagger-ui .opblock-tag,
.swagger-ui .opblock .opblock-summary-description,
.swagger-ui .opblock .opblock-section-header h4,
.swagger-ui .response-col_status,
.swagger-ui .tab li button.tablinks,
.swagger-ui .models-control > span,
.swagger-ui .json-schema-2020-12-property .json-schema-2020-12__title,
.swagger-ui .json-schema-2020-12 *,
.swagger-ui .arrow {
    color: white;
}

.swagger-ui p {
    font-size: medium;
}

.swagger-ui .json-schema-2020-12-accordion *,
.swagger-ui .json-schema-2020-12-expand-deep-button,
.swagger-ui .json-schema-2020-12,
.swagger-ui .json-schema-2020-12-accordion {
    background-color: rgba(32, 55, 76, 1);
}

.swagger-ui .json-schema-2020-12-keyword__value--const {
    border: 1px dashed #DF6919;
}

.swagger-ui .json-schema-2020-12__attribute {
    color: #DF6919;
}

.swagger-ui .json-schema-2020-12__constraint--string {
    background-color: #DF6919;
}

.swagger-ui .json-schema-2020-12-expand-deep-button {
    border-bottom: 1px solid white;
    padding: 0;
}

/* GET */
.swagger-ui .opblock.opblock-get .opblock-summary {
    background-color: #61AFFE;
    color: white;
}

/* POST */
.swagger-ui .opblock.opblock-post .opblock-summary {
    background-color: rgba(73, 204, 144);
    color: white;
}

/* title */
.swagger-ui .info .title {
    color: #DF6919;
}

/* version api */
.swagger-ui .info .title small {
    background-color: rgba(32, 55, 76, 1);
    color: white;
}

/* version oas */
.swagger-ui .info .title small.version-stamp {
    background-color: #DF6919;
    color: white;
}
                    """;
            return new TransformedResource(resource, css.getBytes(StandardCharsets.UTF_8));
        }
        return super.transform(request, resource, transformerChain);
    }
}
