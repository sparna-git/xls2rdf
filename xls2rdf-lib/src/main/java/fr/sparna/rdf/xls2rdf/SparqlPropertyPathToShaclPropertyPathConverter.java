package fr.sparna.rdf.xls2rdf;

import org.eclipse.rdf4j.query.parser.sparql.ast.ASTBasicGraphPattern;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTGraphPatternGroup;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTIRI;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTPathAlternative;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTPathElt;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTPathMod;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTPathSequence;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTPropertyListPath;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQName;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQueryContainer;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTSelectQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTWhereClause;
import org.eclipse.rdf4j.query.parser.sparql.ast.Node;
import org.eclipse.rdf4j.query.parser.sparql.ast.ParseException;
import org.eclipse.rdf4j.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.eclipse.rdf4j.query.parser.sparql.ast.TokenMgrError;

/**
 * Parses a property path in SPARQL and returns its SHACL representation written in Turtle
 */
public class SparqlPropertyPathToShaclPropertyPathConverter {

    /**
     * The different path modifies and their corresponding lower and upper bounds
     */
    enum PATH_MOD {
        STAR("*", "sh:zeroOrMorePath", 1, Long.MAX_VALUE),
        PLUS("+", "sh:oneOrMorePath", 0, Long.MAX_VALUE),
        QUESTION_MARK("?", "sh:zeroOrOne", 0, 1);

        private String sparqlRepresentation;
        private String shaclPath;
        private long lowerBound;
        private long upperBound;
        
        private PATH_MOD(String sparqlRepresentation, String shaclPath, long lowerBound, long upperBound) {
            this.sparqlRepresentation = sparqlRepresentation;
            this.shaclPath = shaclPath;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public String getShaclPath() {
            return shaclPath;
        }

        public String getSparqlRepresentation() {
            return sparqlRepresentation;
        }

        public long getLowerBound() {
            return lowerBound;
        }

        public long getUpperBound() {
            return upperBound;
        } 
        
        public static PATH_MOD parseASTPathMod(ASTPathMod pathMod) {
            for(PATH_MOD pm : PATH_MOD.values()) {
                if(
                    (pathMod.getLowerBound() == pm.getLowerBound())
                    &&
                    (pathMod.getUpperBound() == pm.getUpperBound())
                ) {
                    return pm;
                }
            }
            return null;
        }
        
    }

    private PrefixManager prefixManager;
    
    public SparqlPropertyPathToShaclPropertyPathConverter(PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
    }

    /**
     * @param sparqlPropertyPath a property path in SPARQL, with prefixes e.g. "^skos:broader/skos:narrower"
     * @return The SHACL representation of the path in Turtle syntax, e.g ([sh:inversePath skos:broader] skos:narrower)
     * @throws TokenMgrError
     * @throws ParseException
     */
    public String convertToShaclPropertyPath(String sparqlPropertyPath) throws TokenMgrError, ParseException {
        String sparqlQuery = this.buildSparqlQuery(sparqlPropertyPath);
        ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(sparqlQuery);
        Node astPropertyPath = extractASTPropertyPath(qc);
        String shaclPropertyPath = this.toShaclTurtlePath(astPropertyPath);

        return shaclPropertyPath;
    }

    /**
     * Build a complete SPARQL query with the pat inside, in order to parse it
     * @param sparqlPropertyPath
     * @return
     */
    private String buildSparqlQuery(String sparqlPropertyPath) {
        StringBuffer result = new StringBuffer();
        result.append(this.prefixManager.getPrefixesSparqlHeader());
        result.append("SELECT ?x WHERE {"+"\n");
        result.append("  ?x "+sparqlPropertyPath+" ?y ."+"\n");
        result.append("}");

        return result.toString();
    }

    /**
     * From the full query object, extracts the property path AST node
     * @param query
     * @return
     */
    private Node extractASTPropertyPath(ASTQueryContainer query) {
        ASTSelectQuery select = (ASTSelectQuery)query.jjtGetChild(ASTSelectQuery.class);
        ASTWhereClause whereClause = select.jjtGetChildren(ASTWhereClause.class).get(0);
        ASTGraphPatternGroup graphPatternGroup = whereClause.jjtGetChildren(ASTGraphPatternGroup.class).get(0);
        ASTBasicGraphPattern bgp = graphPatternGroup.jjtGetChildren(ASTBasicGraphPattern.class).get(0);
        ASTTriplesSameSubjectPath triplesSameSubjectPath = bgp.jjtGetChildren(ASTTriplesSameSubjectPath.class).get(0);
        // first is var, second is the property path with object
        Node propertyPath = triplesSameSubjectPath.jjtGetChildren()[1];

        return propertyPath;
    }


    private void printPropertyPath(Node propertyPath, String indent) {
        System.out.print(indent);
        if(propertyPath instanceof ASTPropertyListPath) {
            System.out.println("ASTPropertyListPath");
        } else if (propertyPath instanceof ASTPathAlternative) {
            System.out.println("ASTPathAlternative");
        } else if (propertyPath instanceof ASTPathSequence) {
            System.out.println("ASTPathSequence");
        } else if(propertyPath instanceof ASTPathElt) {
            System.out.println("ASTPathElt"+(((ASTPathElt)propertyPath).isInverse()?" inverse":""));
        } else if(propertyPath instanceof ASTPathMod) {
            System.out.println("ASTPathMod : "+PATH_MOD.parseASTPathMod((ASTPathMod)propertyPath).getSparqlRepresentation());
        } else if(propertyPath instanceof ASTQName) {
            System.out.println("ASTQName : "+((ASTQName)propertyPath).getValue());
        } else {
            System.out.println("UNKNOWN ! "+propertyPath.getClass().getName());
        }

        String newIndent = indent+"-";
        for(int i=0;i<propertyPath.jjtGetNumChildren();i++) {
            printPropertyPath(propertyPath.jjtGetChild(i), newIndent);
        }

    }

    /**
     * Turns as AST property path into its corresponding SHACL representation written in Turtle
     * @param propertyPath the property path in the abstract syntax tree
     * @return
     */
    public String toShaclTurtlePath(Node propertyPath) {
        StringBuffer b = new StringBuffer();
        if(propertyPath instanceof ASTPropertyListPath) {
            for(int i=0;i<propertyPath.jjtGetNumChildren();i++) {
                b.append(toShaclTurtlePath(propertyPath.jjtGetChild(i)));
            }
        } else if (propertyPath instanceof ASTPathAlternative) {
            if(((ASTPathAlternative)propertyPath).jjtGetNumChildren() ==1) {
                b.append(toShaclTurtlePath(propertyPath.jjtGetChild(0)));
            } else {
                b.append("[sh:alternativePath (");
                for(int i=0;i<propertyPath.jjtGetNumChildren();i++) {
                    b.append(toShaclTurtlePath(propertyPath.jjtGetChild(i)));
                    b.append(" ");
                }
                b.deleteCharAt(b.length()-1);
                b.append(")]");
            }
        } else if (propertyPath instanceof ASTPathSequence) {
            if(((ASTPathSequence)propertyPath).jjtGetNumChildren() == 1) {
                b.append(toShaclTurtlePath(propertyPath.jjtGetChild(0)));
            } else {
                b.append("(");
                for(int i=0;i<propertyPath.jjtGetNumChildren();i++) {
                    b.append(toShaclTurtlePath(propertyPath.jjtGetChild(i)));
                    b.append(" ");
                }
                b.deleteCharAt(b.length()-1);
                b.append(")");
            }
        } else if(propertyPath instanceof ASTPathElt) {
            if(((ASTPathElt)propertyPath).isInverse()) {
                b.append("[sh:inversePath "+toShaclTurtlePath(propertyPath.jjtGetChild(0))+"]");
            } else if(((ASTPathElt)propertyPath).isNegatedPropertySet()) {
                b.append("[sh:negativePath "+toShaclTurtlePath(propertyPath.jjtGetChild(0))+"]");
            } else if(((ASTPathElt)propertyPath).getPathMod() != null) {
                PATH_MOD pm = PATH_MOD.parseASTPathMod(((ASTPathElt)propertyPath).getPathMod());
                switch (pm) {
                    case PLUS:
                    b.append("[sh:oneOrMorePath "+toShaclTurtlePath(propertyPath.jjtGetChild(0))+"]");
                        break;                
                    case QUESTION_MARK:
                        b.append("[sh:zeroOrOnePath "+toShaclTurtlePath(propertyPath.jjtGetChild(0))+"]");
                        break;
                    case STAR:
                        b.append("[sh:zeroOrMorePath "+toShaclTurtlePath(propertyPath.jjtGetChild(0))+"]");
                        break;
                    default:
                        System.out.println("Unknown path modifier "+pm);                    
                        break;
                }
            } else {
                return toShaclTurtlePath(propertyPath.jjtGetChild(0));
            }            
        } else if(propertyPath instanceof ASTPathMod) {
            // nothing, already processsed above
        } else if(propertyPath instanceof ASTQName) {
            return ((ASTQName)propertyPath).getValue();
        } else if(propertyPath instanceof ASTIRI) {
            return "<"+((ASTIRI)propertyPath).getValue()+">";
        }

        return b.toString();
    }


    public static void main(String... args) throws Exception {
        PrefixManager pm = new PrefixManager();
        SparqlPropertyPathToShaclPropertyPathConverter me = new SparqlPropertyPathToShaclPropertyPathConverter(pm);
        System.out.println(me.convertToShaclPropertyPath("(^(<http://www.w3.org/2000/01/rdf-schema#subClassOf>+))/rdfs:label"));
    }
}
