package fr.sparna.rdf.xls2rdf.postprocess;


import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

public class ModelPostProcessingOutput {

    public Model toAdd;
    public Model toRemove;

    public ModelPostProcessingOutput() {
        this.toAdd = new LinkedHashModel();
        this.toRemove = new LinkedHashModel();
    }

    public ModelPostProcessingOutput(Model toAdd, Model toRemove) {
        this.toAdd = toAdd;
        this.toRemove = toRemove;
    }
}
