import {Injectable, OnInit} from '@angular/core';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root'
})
export class EventService implements OnInit {

  /**
   * Max number of charts rendered. Oldest charts get removed when limit is reached.
   */
  static readonly max_charts = 10;

  /**
   * Redraw cooldown in milliseconds.
   * Example: If 100 changes are done to a chart in a second,
   * and cooldown is 500, charts are only redrawn twice instead of 100 times.
   */
  static readonly redraw_cooldown = 2000;

  /**
   * Event batch download size
   */
  static readonly event_batch_size = 1000;

  /**
   * Notify to every subscriber for the corresponding EventType
   */
  subscriptions = new Map<EventType, EventHandler[]>();

  /**
   * STOMP client
   */
  stompClient: Stomp.Client;

  constructor() {}

  public subscribe(type: EventType, handler:EventHandler) {
    if(!this.subscriptions.has(type)){
      this.subscriptions.set(type, []);
    }
    // TODO: Why is ignore necessary? from previous if it cannot be undefined
    // @ts-ignore
    this.subscriptions.get(type).push(handler);
  }

  ngOnInit() {
    this.connectAndSubscribe();
  }

  connectAndSubscribe(callback) {
    const brokerURL = "ws://" + window.location.host + "/websocket";
    this.stompClient = new Stomp.Client({brokerURL: brokerURL});
    this.stompClient.reconnectDelay = 1000;
    this.stompClient.onConnect = function () {
      // Subscribe to live event feed from Mork
      const subscription = stompClient.subscribe('/topic/events', function (event) {
        const payload = JSON.parse(event.body);
        callback(payload);
      });
      $('#running-status').text('WAITING');
      console.log("STOMP connected. Waiting for the latest event to synchronize state...");
      $.getJSON( "/lastevent", function(event) {
        if (!event_queue) {
          event_queue = [];
          console.log("Recieved first event with id: " + event.eventId);
          downloadOldEventData(0, event.eventId + 1); // [0, eventId]
        } else {
          console.log("ERROR: Event queue already created, impossible?")
        }
      });
    }

    stompClient.onStompError = function (frame) {
      // Will be invoked in case of error encountered at Broker
      // Bad login/passcode typically will cause an error
      // Complaint brokers will set `message` header with a brief message. Body may contain details.
      // Compliant brokers will terminate the connection after any error
      console.log('Broker reported error: ' + frame.headers['message']);
      console.log('Additional details: ' + frame.body);
    };

    stompClient.activate();
  }

}

