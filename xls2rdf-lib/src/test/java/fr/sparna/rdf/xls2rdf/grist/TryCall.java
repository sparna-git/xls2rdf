package fr.sparna.rdf.xls2rdf.grist;

import fr.sparna.rdf.xls2rdf.WorkbookMapping;
import fr.sparna.rdf.xls2rdf.Xls2RdfConverterBuilder;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbookFactory;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class TryCall {



    @Test
    public void test() throws IOException {

        Workbook workbook = GristWorkbookFactory.open("pchyrkhBY6DvUt6vW9s1xt", "c8e10c28c0e5f99b0ae57e44ef9d69e19e3a9ed5", true);
        Properties p = new Properties();
        p.load(this.getClass().getResourceAsStream("/grist_properties.txt"));

        Xls2RdfConverterBuilder builder = Xls2RdfConverterBuilder.getInstance()
                .withSkipHidden(false)
                .withFailOnReconcile(false)
                .withGenerateXlDefinitions(false)
                .withGenerateBroaderTransitive(false)
                .withGenerateXl(false)
                .withSkipHidden(true)
                .withApplyPostProcessing(false)
                .withFormat((String)null)
                .withModelWriterFactory(false, false, true)
                .withOutputStream(new FileOutputStream("C://Users//ArthurLeroux//Documents//GristOutput.txt"))
                .withWorkbookMapping(new WorkbookMapping(p));

                builder.buildConverter().processWorkbook(workbook);




    }

}
