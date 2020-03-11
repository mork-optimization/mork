package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AlgorithmsManager {

    private static final Logger log = Logger.getLogger(Orquestrator.class.toString());

    private final List<Algorithm<?, ?>> allAlgorithms;
    private final List<Algorithm<?, ?>> filteredAlgorithms = new ArrayList<>();

    @Value("${solver.algorithms}")
    private String algFilter;

    public AlgorithmsManager(List<Algorithm<?, ?>> allAlgorithms) {
        this.allAlgorithms = allAlgorithms;
    }

    @PostConstruct
    private void initialize(){
        log.info("Algorithm filter is: *");
        if(allAlgorithms.isEmpty()){
            log.warning("Could not detect any algorithm, wtf?");
        }
        if(algFilter.equals("*")){
            log.info("Loading all algorithms: "+ allAlgorithms);
            this.filteredAlgorithms.addAll(allAlgorithms);
        } else {
            var validNames = Arrays.stream(algFilter.split(",")).map(String::trim).collect(Collectors.toSet());
            for(var algorithm: allAlgorithms){
                String algname = algorithm.getClass().getSimpleName();
                if (validNames.contains(algname)) {
                    this.filteredAlgorithms.add(algorithm);
                    log.info("Adding algorithm to list: " + algname);
                } else {
                    log.info("Ignoring algorithm: " + algname);
                }
            }
        }

        if(this.filteredAlgorithms.isEmpty()){
            log.warning("Algorithm list after filtering is empty :(");
        }
    }

    public List<Algorithm<?,?>> getAlgorithms(){
        return Collections.unmodifiableList(this.filteredAlgorithms);
    }
}
