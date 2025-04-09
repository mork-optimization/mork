package es.urjc.etsii.grafo.util;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PythonUtil {
    private static final Logger log = LoggerFactory.getLogger(PythonUtil.class);

    public static final String VENV_PATH = "venv";
    public static final String PYTHON_PATH_VENV = VENV_PATH + "/bin/python3";
    public static final String PYTHON_PATH_WINDOWS_VENV = VENV_PATH + "/Scripts/python.exe";
    public static final String PYTHON_PATH = "python3";
    public static final String PYTHON_PATH_WINDOW = "python.exe";

    public static Process run(boolean venv, String... args) {
        try {
            var pythonPath = getPythonPath(venv);
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(ArrayUtils.addFirst(args, pythonPath));
            var process = processBuilder.inheritIO().start();
            process.waitFor();
            return process;
        } catch (IOException e){
            throw new RuntimeException(e);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static String getPythonPath(boolean venv){
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if(venv){
            if(!Files.exists(Path.of(VENV_PATH))){
                log.debug("Creating Python venv at " + VENV_PATH);
                run(false, "-m", "venv", VENV_PATH);
            } else {
                log.debug("Using existing Python venv at " + VENV_PATH);
            }
            if(isWindows){
                return PYTHON_PATH_WINDOWS_VENV;
            } else {
                return PYTHON_PATH_VENV;
            }
        } else {
            if(isWindows){
                return PYTHON_PATH_WINDOW;
            } else {
                return PYTHON_PATH;
            }
        }
    }
}
