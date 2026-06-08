package fr.sparna.rdf.xls2rdf.web.utils;

//Enumération des clés utilisées pour stocker les modèles dans les vues
public enum ConvertFormModelKey {

        FORM_DATA("formData"),
		APPLICATION_DATA("applicationData"),
		SESSION_DATA("sessionData"),
		ERROR("error"),
		VIEW("view"),
		API("api"),
		DOC("doc"),
		API_DOC("api-doc"),
		CONVERT("convert");
		
		private final String key;

		public String getKey() {
			return this.key;
		}

		ConvertFormModelKey(String key) {
			this.key = key;
		}
    
}
