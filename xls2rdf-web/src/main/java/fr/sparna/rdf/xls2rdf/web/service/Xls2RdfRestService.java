package fr.sparna.rdf.xls2rdf.web.service;

import fr.sparna.rdf.xls2rdf.web.exception.ExceptionManager;
import fr.sparna.rdf.xls2rdf.web.exception.Xls2RdfRestControllerException;
import fr.sparna.rdf.xls2rdf.web.utils.StringUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

@Service
public class Xls2RdfRestService {

    final static Logger log = LoggerFactory.getLogger(Xls2RdfRestService.class);

    private final Xls2RdfConverterService converter;

    @Autowired
    public Xls2RdfRestService(Xls2RdfConverterService converter){
        this.converter = converter;
    }

    public ResponseEntity<ByteArrayResource> runRestConversion(
            String language,
            Object clientObj,
            String format,
            boolean useSkosXl,
            boolean skipHidden,
            boolean broaderTransitive,
            boolean ignorePostProc
    ){
        RDFFormat theFormat = RDFWriterRegistry.getInstance().getFileFormatForMIMEType(format).orElse(RDFFormat.TURTLE);

        String fileName = StringUtils.formatFileName(clientObj, theFormat.getDefaultFileExtension());

        InputStream in = null;

        if(clientObj instanceof URL clientURL){
            try {
                in = clientURL.openStream();
            } catch (IOException ex) {
                ExceptionManager.throwException(Xls2RdfRestControllerException.class, ex.getMessage());
            }
        }
        if(clientObj instanceof MultipartFile clientFile){
            try {
                in = clientFile.getInputStream();
            } catch (IOException ex) {
                ExceptionManager.throwException(Xls2RdfRestControllerException.class, ex.getMessage());
            }
        }

        try{
            ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
            List<String> cvIds = this.converter.convert(
                    in,
                    responseOutputStream,
                    language,
                    theFormat,
                    useSkosXl,
                    broaderTransitive,
                    ignorePostProc,
                    //fail if no reconcile
                    false,
                    // skip hidden rows and columns
                    skipHidden,
                    false
            );

            cvIds.stream().map(cv -> "Converted Graph: " + cv).forEach(log::info);

            return this.converter.transformConversionToResponseEntity(fileName, theFormat.getDefaultMIMEType(), responseOutputStream.toByteArray(), ContentDisposition.attachment());

        }catch (Exception ex) {
            ExceptionManager.throwException(Xls2RdfRestControllerException.class, ex.getMessage());
        }finally{
            try {
                in.close();
            } catch (IOException ex) {
                ExceptionManager.throwException(Xls2RdfRestControllerException.class, ex.getMessage());
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
