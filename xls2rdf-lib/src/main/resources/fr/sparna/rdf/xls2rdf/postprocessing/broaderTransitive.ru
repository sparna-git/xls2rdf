PREFIX skos:<http://www.w3.org/2004/02/skos/core#>

insert {
	?child skos:broaderTransitive ?ancestor .
} WHERE {
  ?child (skos:broader|^skos:narrower)+ ?ancestor .
}