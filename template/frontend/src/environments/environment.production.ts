const websocketProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';

export const environment = {
  apiBaseUrl: '',
  websocketUrl: `${websocketProtocol}//${window.location.host}/websocket`,
  websocketDebug: false,
} as const;
