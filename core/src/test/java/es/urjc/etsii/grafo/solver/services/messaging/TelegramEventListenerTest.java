package es.urjc.etsii.grafo.solver.services.messaging;

import es.urjc.etsii.grafo.solver.services.events.types.ErrorEvent;
import es.urjc.etsii.grafo.solver.services.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramEventListenerTest {

    @Mock
    TelegramService telegramService;

    @InjectMocks
    TelegramEventListener telegramEventListener;

    Throwable fakeException = new RuntimeException("test exception");


    @Test
    public void readyCheckOnError(){
        given(telegramService.ready()).willReturn(false);
        telegramEventListener.onError(new ErrorEvent(fakeException));

        verify(telegramService).ready();
        verifyNoMoreInteractions(telegramService);
    }

    @Test
    public void onError(){
        given(telegramService.ready()).willReturn(true);
        telegramEventListener.onError(new ErrorEvent(fakeException));
        verify(telegramService).sendMessage(anyString());
    }

    @Test
    public void readyCheckOnExperimentEnd(){
        given(telegramService.ready()).willReturn(false);
        telegramEventListener.onExperimentEnd(new ExperimentEndedEvent("name", 1000, System.currentTimeMillis()));

        verify(telegramService).ready();
        verifyNoMoreInteractions(telegramService);
    }

    @Test
    public void onExperimentEnd(){
        given(telegramService.ready()).willReturn(true);
        telegramEventListener.onExperimentEnd(new ExperimentEndedEvent("name", 1000, System.currentTimeMillis()));
        verify(telegramService).sendMessage(anyString());
    }

    @Test
    public void onExecutionEnd(){
        telegramEventListener.onExecutionEnd(new ExecutionEndedEvent(1000));
        verify(telegramService).stop();
    }

}
