package es.urjc.etsii.grafo.autoconfig;

import org.antlr.v4.runtime.*;

public class BailErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        throw new AlgorithmParsingException("Error while parsing algorithm string" , e);
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        throw new AlgorithmParsingException("Error while parsing algorithm string" , new InputMismatchException(recognizer));
    }

    @Override
    public void sync(Parser recognizer) throws RecognitionException {
        // Do nothing
    }
}
