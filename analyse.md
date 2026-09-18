```json
"maColonne" : {
	"@key": "xxxxx",
	"@id": "skos:prefLabel" / "@reverse" : "dcterms:related",
	"@language": "fr",
	"@type" : "xsd:date",

	// pre-process
	"@separator": ";",
	// post-process
	"@list": "true",

	// lookup and reconcile work the same way of done in a post process
	"@lookup": "skos:prefLabel", / "@lookup": "N" , / "@lookup": "a key"
	"@reconcile": "local", "@reconcileOn": "type or scheme",

	"@manchester": "true",
	"@wrapper": "sh:or / sh:and / sh:xor",

	// could be made a regex to deal with parenthesis
	"@ignore": "n", "@ignore": "\(.*\)",

	// preprocess, forcément
	"@normalize": "false",
	// subject column : works with ID or predicate
	"@about": "skos:prefLabel" / "@about" : "a key", / "@about": "N"

	// ++
	"@graph": "..."
},
```

ou un tableau de mappings:

```json
"maColonne": [
	{

	},
	{

	}
]
```



```json

"@about": {
	"@id": "SubjectColumn",
	"@context": {
		"@vocab": "http://..."
	},
	"@type": "skos:Concept",
	"predicate": {
		"skos:inScheme": "http://foo"
	}
}


"maColonne" : {
	"@key": "xxxxx",

	"@id": "skos:prefLabel" / "@reverse" : "dcterms:related",
	"@language": "fr",
	"@type" : "xsd:date",

	// pre-processes :
	// - normalize false
	// - lookup
	"@context": {
		"@vocab": "http://...",
		"@base": "http://...",
		"lookup": "skos:prefLabel", "lookup": "a key",
		"normalize": "false"

		// éventuellement mapping clé-valeur donné inline
	}

	// post-processes :
	// - split
	// - ignore
	// - manchester
	// - list
	// - wrapper
	// - subject
	// - reconcile

	"@processes": [
		{
			"@type" : "split",
			"separator": ";"
		},
		{
			"@type": "delete",
			"regex" : "\(.*\)"
		},
		{
			"@type": "manchester"
		},
		{
			"@type": "list"
		},
		{
			"@type": "shacl",
			"predicate": "sh:or"
		},
		{
			"@type": "subject"
			"key": "X", "predicate": "dcterms:creator"
		},
		{
			"@type": "reconcile"
			"on" : "http://...."
		},
		{
			"@type": "graph"
			"value" : "http://...."
		},
		{
			"@type": "update",
			"sparql": "INSERT { ... } DELETE { ... } WHERE { ... }"
		}
	]
},
```