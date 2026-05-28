package fr.sparna.rdf.xls2rdf.app;

import fr.sparna.rdf.xls2rdf.Xls2RdfConverterBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class DirectoryWatcher {

    private final Logger log = LoggerFactory.getLogger(DirectoryWatcher.class.getName());

    private WatchService watchService;
    private Xls2RdfConverterBuilder builder;
    private Map<WatchKey, Path> watchKeyToPath;
    private File fileIn;
    private File fileOut;

    public DirectoryWatcher(File fileIn, File fileOut, Xls2RdfConverterBuilder builder) throws IOException {
        this.fileIn = fileIn;
        this.fileOut = fileOut;
        this.builder = builder;
        this.watchService = FileSystems.getDefault().newWatchService();
        this.watchKeyToPath = new HashMap<>();
        if(!this.registerWatchKey(this.fileIn.getParentFile())) throw new IllegalArgumentException("The file does not contain a valid directory to watch.");
    }


    private boolean registerWatchKey(File dir) throws IOException {
        if(!dir.isDirectory()) return false;
            Path filePath = dir.toPath();
            WatchKey key = filePath.register(this.watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            this.watchKeyToPath.put(key, filePath);
            log.info("{} has been registered with watch service.", dir);
        return true;
    }

    private boolean isCorrectFile(Path modifiedFile){
        return modifiedFile.toString().contains(this.fileIn.getName());
    }

    //Voir https://docs.oracle.com/javase/tutorial/essential/io/notification.html#name pour WatchService fonctionnement
    public void runWatchService(){

                WatchKey key;

                try {
                    System.out.println("Watch service is looking for changes within " + this.fileIn.getName() + ".");

                    while(true){

                        key = this.watchService.take();

                        for(WatchEvent<?> event: key.pollEvents()){

                            WatchEvent.Kind<?> kind = event.kind();

                            Path keyPath = this.watchKeyToPath.get(key);

                            if (kind == OVERFLOW) continue;

                            WatchEvent<Path> currentEvent = (WatchEvent<Path>) event;
                            //On récupère le fichier qui a levé l'event
                            Path eventContext = currentEvent.context();
                            //On reconstruit le chemin fichier qui a levé l'event avec le chemin du watchkey enregistré
                            Path modifiedFile = keyPath.resolve(eventContext);

                            if(!this.isCorrectFile(modifiedFile)) continue;

                            if(kind == StandardWatchEventKinds.ENTRY_MODIFY && event.count() == 1){
                                try(FileInputStream  in  = new FileInputStream (this.fileIn);
                                    FileOutputStream out = new FileOutputStream(this.fileOut)){
                                    this.builder.withOutputStream(out);
                                    this.builder.buildConverter().processInputStream(in);
                                    out.flush();
                                    System.out.println("Conversion made for " + this.fileIn + ".");
                                }catch (IOException ex){
                                    ex.printStackTrace();
                                }
                            }
                        }

                        boolean isReset = key.reset();
                        if(!isReset) break;
                    }
                }catch (InterruptedException e) {
                    try {
                        this.watchService.close();
                    } catch (IOException ex) {
                        Thread.currentThread().interrupt();
                        System.exit(-1);
                    }
                    Thread.currentThread().interrupt();
                    System.exit(-1);
                }
    }

    public static String createFileName(String eventContext, RDFFormat format){
        if(eventContext.contains(".")){
            return eventContext
                    .substring(0, eventContext.lastIndexOf(".")) + "." + format.getDefaultFileExtension();
        }
        else return eventContext + "." + format.getDefaultFileExtension();
    }

}
