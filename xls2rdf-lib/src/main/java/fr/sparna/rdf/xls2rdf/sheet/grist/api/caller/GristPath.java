package fr.sparna.rdf.xls2rdf.sheet.grist.api.caller;

public enum GristPath {

    GET_TABLES ("docs/"
            .concat(GristPlaceholder.DOC_ID.getPlaceholder())
            .concat("/tables"), HttpMethod.GET),
    GET_RECORDS("docs/"
            .concat(GristPlaceholder.DOC_ID.getPlaceholder())
            .concat("/tables/"
                    .concat(GristPlaceholder.TABLE_ID.getPlaceholder())
                    .concat("/records")), HttpMethod.GET),
    GET_COLUMNS("docs/"
            .concat(GristPlaceholder.DOC_ID.getPlaceholder())
            .concat("/tables/"
                    .concat(GristPlaceholder.TABLE_ID.getPlaceholder())
                    .concat("/columns")), HttpMethod.GET),
    GET_WORKSPACE("workspaces/"
            .concat(GristPlaceholder.WORKSPACE_ID.getPlaceholder()), HttpMethod.GET),
    GET_DOCUMENT("docs/"
            .concat(GristPlaceholder.DOC_ID.getPlaceholder()), HttpMethod.GET);

    GristPath(String path, HttpMethod method){
        this.path = path;
        this.method = method;
    }

    private final String path;
    private final HttpMethod method;

    public String getPath(){
        return this.path;
    }

    public HttpMethod getMethod(){
        return this.method;
    }

    public enum GristPlaceholder{

        DOC_ID("{docId}"),
        TABLE_ID("{tableId}"),
        WORKSPACE_ID("{workspaceId}");

        private  final String placeholder;

        GristPlaceholder(String placeholder){
            this.placeholder = placeholder;
        }

        public String getPlaceholder(){
            return this.placeholder;
        }
    }

}
