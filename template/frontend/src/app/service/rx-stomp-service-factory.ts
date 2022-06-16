import { RxStompService } from './rx-stomp.service';
import { MorkRxStompConfig } from './rx-stomp.config';

export function rxStompServiceFactory() {
  const rxStomp = new RxStompService();
  rxStomp.configure(MorkRxStompConfig);
  rxStomp.activate();
  return rxStomp;
}
