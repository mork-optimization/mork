package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.util.IOUtil;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@InheritedComponent
public abstract class ResultsSerializer {
    protected final Logger log;
    protected final boolean enabled;
    protected final String folder;
    protected final String format;

    public ResultsSerializer(boolean enabled, String folder, String format) {
        this.enabled = enabled;
        this.folder = folder;
        this.format = format;
        log = Logger.getLogger(this.getClass().getName());
    }

    public void serializeResults(String experimentName, List<? extends SolutionGeneratedEvent<?,?>> results){
        if(!enabled){
            return;
        }

        if(results.isEmpty()){
            throw new IllegalArgumentException("Cannot save empty list of results");
        }

        IOUtil.createFolder(folder);
        Path p = Path.of(folder, getFilename(experimentName));
        _serializeResults(results, p);
    }

    protected abstract void _serializeResults(List<? extends SolutionGeneratedEvent<?,?>> results, Path p);

    private String getFilename(String experimentName){
        return experimentName + new SimpleDateFormat(format).format(new Date());
    }
}
