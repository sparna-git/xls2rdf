package fr.sparna.rdf.xls2rdf.web.form.convert;

//Enumération des clés utilisées pour stocker les données dans le modèle des vues
public enum ConvertFormModelKey {

        FORM_DATA("formData"),
		APPLICATION_DATA("applicationData"),
		SESSION_DATA("sessionData"),
		ERROR("error");

		private final String key;

		public String getKey() {
			return this.key;
		}

		private ConvertFormModelKey(String key) {
			this.key = key;
		}
    
}
