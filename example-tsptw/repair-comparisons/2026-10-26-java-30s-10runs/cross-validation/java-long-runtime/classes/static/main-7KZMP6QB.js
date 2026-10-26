var k$1=Object.create;var g=Object.defineProperty;var l$1=Object.defineProperties;var m$1=Object.getOwnPropertyDescriptor;var n=Object.getOwnPropertyDescriptors;var o=Object.getOwnPropertyNames;var f$1=Object.getOwnPropertySymbols;var p=Object.getPrototypeOf;var h=Object.prototype.hasOwnProperty;var j$1=Object.prototype.propertyIsEnumerable;var i=(a,b,c)=>b in a?g(a,b,{enumerable:!0,configurable:!0,writable:!0,value:c}):a[b]=c;var r=(a,b)=>{for(var c in b||={})h.call(b,c)&&i(a,c,b[c]);if(f$1)for(var c of f$1(b))j$1.call(b,c)&&i(a,c,b[c]);return a};var s=(a,b)=>l$1(a,n(b));var t=(a,b)=>{var c={};for(var d in a)h.call(a,d)&&b.indexOf(d)<0&&(c[d]=a[d]);if(a!=null&&f$1)for(var d of f$1(a))b.indexOf(d)<0&&j$1.call(a,d)&&(c[d]=a[d]);return c};var u$1=(a,b)=>()=>{try{return b||a((b={exports:{}}).exports,b),b.exports}catch(c){throw b=0,c}};var q$1=(a,b,c,d)=>{if(b&&typeof b==`object`||typeof b==`function`)for(let e of o(b))!h.call(a,e)&&e!==c&&g(a,e,{get:()=>b[e],enumerable:!(d=m$1(b,e))||d.enumerable});return a};var v=(a,b,c)=>(c=a!=null?k$1(p(a)):{},q$1(b||!a||!a.__esModule?g(c,`default`,{value:a,enumerable:!0}):c,a));var Bw=u$1((bm,Vw)=>{"use strict";(function(t,n){typeof bm==`object`&&typeof Vw<`u`?n(bm):typeof define==`function`&&define.amd?define([`exports`],n):(t=typeof globalThis<`u`?globalThis:t||self,n(t.StompJs={}))})(bm,(function(t){"use strict";function n(z,x){z.terminate=function(){let M=()=>{};this.onerror=M,this.onmessage=M,this.onopen=M;let O=new Date,$=Math.random().toString().substring(2,8),_e=this.onclose;this.onclose=bt=>{let We=new Date().getTime()-O.getTime();x(`Discarded socket (#${$})  closed after ${We}ms, with code/reason: ${bt.code}/${bt.reason}`)},this.close(),_e?.call(z,{code:4001,reason:`Quick discarding socket (#${$}) without waiting for the shutdown sequence.`,wasClean:!1})}}let e={LF:`
`,NULL:`\0`};class i{get body(){return!this._body&&this.isBinaryBody&&(this._body=new TextDecoder().decode(this._binaryBody)),this._body||``}get binaryBody(){return!this._binaryBody&&!this.isBinaryBody&&(this._binaryBody=new TextEncoder().encode(this._body)),this._binaryBody}constructor(x){let{command:M,headers:O,body:$,binaryBody:_e,escapeHeaderValues:bt,skipContentLengthHeader:We}=x;this.command=M,this.headers=Object.assign({},O||{}),_e?(this._binaryBody=_e,this.isBinaryBody=!0):(this._body=$||``,this.isBinaryBody=!1),this.escapeHeaderValues=bt||!1,this.skipContentLengthHeader=We||!1}static fromRawFrame(x,M){let O={},$=_e=>_e.replace(/^\s+|\s+$/g,``);for(let _e of x.headers.reverse()){_e.indexOf(`:`);let bt=$(_e[0]),We=$(_e[1]);M&&x.command!==`CONNECT`&&x.command!==`CONNECTED`&&(We=i.hdrValueUnEscape(We)),O[bt]=We}return new i({command:x.command,headers:O,binaryBody:x.binaryBody,escapeHeaderValues:M})}toString(){return this.serializeCmdAndHeaders()}serialize(){let x=this.serializeCmdAndHeaders();return this.isBinaryBody?i.toUnit8Array(x,this._binaryBody).buffer:x+this._body+e.NULL}serializeCmdAndHeaders(){let x=[this.command];this.skipContentLengthHeader&&delete this.headers[`content-length`];for(let M of Object.keys(this.headers||{})){let O=this.headers[M];this.escapeHeaderValues&&this.command!==`CONNECT`&&this.command!==`CONNECTED`?x.push(`${M}:${i.hdrValueEscape(`${O}`)}`):x.push(`${M}:${O}`)}return(this.isBinaryBody||!this.isBodyEmpty()&&!this.skipContentLengthHeader)&&x.push(`content-length:${this.bodyLength()}`),x.join(e.LF)+e.LF+e.LF}isBodyEmpty(){return this.bodyLength()===0}bodyLength(){let x=this.binaryBody;return x?x.length:0}static sizeOfUTF8(x){return x?new TextEncoder().encode(x).length:0}static toUnit8Array(x,M){let O=new TextEncoder().encode(x),$=new Uint8Array([0]),_e=new Uint8Array(O.length+M.length+$.length);return _e.set(O),_e.set(M,O.length),_e.set($,O.length+M.length),_e}static marshall(x){return new i(x).serialize()}static hdrValueEscape(x){return x.replace(/\\/g,`\\\\`).replace(/\r/g,`\\r`).replace(/\n/g,`\\n`).replace(/:/g,`\\c`)}static hdrValueUnEscape(x){return x.replace(/\\r/g,`\r`).replace(/\\n/g,`
`).replace(/\\c/g,`:`).replace(/\\\\/g,`\\`)}}let a=0,o=10,r=13,s=58;class c{constructor(x,M){this.onFrame=x,this.onIncomingPing=M,this._encoder=new TextEncoder,this._decoder=new TextDecoder,this._token=[],this._initState()}parseChunk(x,M=!1){let O;if(typeof x==`string`?O=this._encoder.encode(x):O=new Uint8Array(x),M&&O[O.length-1]!==0){let $=new Uint8Array(O.length+1);$.set(O,0),$[O.length]=0,O=$}for(let $=0;$<O.length;$++){let _e=O[$];this._onByte(_e)}}_collectFrame(x){if(x!==a&&x!==r){if(x===o){this.onIncomingPing();return}this._onByte=this._collectCommand,this._reinjectByte(x)}}_collectCommand(x){if(x!==r){if(x===o){this._results.command=this._consumeTokenAsUTF8(),this._onByte=this._collectHeaders;return}this._consumeByte(x)}}_collectHeaders(x){if(x!==r){if(x===o){this._setupCollectBody();return}this._onByte=this._collectHeaderKey,this._reinjectByte(x)}}_reinjectByte(x){this._onByte(x)}_collectHeaderKey(x){if(x===s){this._headerKey=this._consumeTokenAsUTF8(),this._onByte=this._collectHeaderValue;return}this._consumeByte(x)}_collectHeaderValue(x){if(x!==r){if(x===o){this._results.headers.push([this._headerKey,this._consumeTokenAsUTF8()]),this._headerKey=void 0,this._onByte=this._collectHeaders;return}this._consumeByte(x)}}_setupCollectBody(){let x=this._results.headers.filter(M=>M[0]===`content-length`)[0];x?(this._bodyBytesRemaining=parseInt(x[1],10),this._onByte=this._collectBodyFixedSize):this._onByte=this._collectBodyNullTerminated}_collectBodyNullTerminated(x){if(x===a){this._retrievedBody();return}this._consumeByte(x)}_collectBodyFixedSize(x){if(this._bodyBytesRemaining--===0){this._retrievedBody();return}this._consumeByte(x)}_retrievedBody(){this._results.binaryBody=this._consumeTokenAsRaw();try{this.onFrame(this._results)}catch(x){console.log(`Ignoring an exception thrown by a frame handler. Original exception: `,x)}this._initState()}_consumeByte(x){this._token.push(x)}_consumeTokenAsUTF8(){return this._decoder.decode(this._consumeTokenAsRaw())}_consumeTokenAsRaw(){let x=new Uint8Array(this._token);return this._token=[],x}_initState(){this._results={command:void 0,headers:[],binaryBody:void 0},this._token=[],this._headerKey=void 0,this._onByte=this._collectFrame}}t.StompSocketState=void 0,(function(z){z[z.CONNECTING=0]=`CONNECTING`,z[z.OPEN=1]=`OPEN`,z[z.CLOSING=2]=`CLOSING`,z[z.CLOSED=3]=`CLOSED`})(t.StompSocketState||(t.StompSocketState={})),t.ActivationState=void 0,(function(z){z[z.ACTIVE=0]=`ACTIVE`,z[z.DEACTIVATING=1]=`DEACTIVATING`,z[z.INACTIVE=2]=`INACTIVE`})(t.ActivationState||(t.ActivationState={})),t.ReconnectionTimeMode=void 0,(function(z){z[z.LINEAR=0]=`LINEAR`,z[z.EXPONENTIAL=1]=`EXPONENTIAL`})(t.ReconnectionTimeMode||(t.ReconnectionTimeMode={})),t.TickerStrategy=void 0,(function(z){z.Interval=`interval`,z.Worker=`worker`})(t.TickerStrategy||(t.TickerStrategy={}));class h{constructor(x,M=t.TickerStrategy.Interval,O){this._interval=x,this._strategy=M,this._debug=O,this._workerScript=`
    var startTime = Date.now();
    setInterval(function() {
        self.postMessage(Date.now() - startTime);
    }, ${this._interval});
  `}start(x){this.stop(),this.shouldUseWorker()?this.runWorker(x):this.runInterval(x)}stop(){this.disposeWorker(),this.disposeInterval()}shouldUseWorker(){return typeof Worker<`u`&&this._strategy===t.TickerStrategy.Worker}runWorker(x){this._debug(`Using runWorker for outgoing pings`),this._worker||(this._worker=new Worker(URL.createObjectURL(new Blob([this._workerScript],{type:`text/javascript`}))),this._worker.onmessage=M=>x(M.data))}runInterval(x){if(this._debug(`Using runInterval for outgoing pings`),!this._timer){let M=Date.now();this._timer=setInterval(()=>{x(Date.now()-M)},this._interval)}}disposeWorker(){this._worker&&(this._worker.terminate(),delete this._worker,this._debug(`Outgoing ping disposeWorker`))}disposeInterval(){this._timer&&(clearInterval(this._timer),delete this._timer,this._debug(`Outgoing ping disposeInterval`))}}class p{constructor(x){this.versions=x}supportedVersions(){return this.versions.join(`,`)}protocolVersions(){return this.versions.map(x=>`v${x.replace(`.`,``)}.stomp`)}}p.V1_0=`1.0`,p.V1_1=`1.1`,p.V1_2=`1.2`,p.default=new p([p.V1_2,p.V1_1,p.V1_0]);class g{get connectedVersion(){return this._connectedVersion}get connected(){return this._connected}constructor(x,M,O){this._client=x,this._webSocket=M,this._connected=!1,this._serverFrameHandlers={CONNECTED:$=>{this.debug(`connected to server ${$.headers.server}`),this._connected=!0,this._connectedVersion=$.headers.version,this._connectedVersion===p.V1_2&&(this._escapeHeaderValues=!0),this._setupHeartbeat($.headers),this.onConnect($)},MESSAGE:$=>{let _e=$.headers.subscription,bt=this._subscriptions[_e]||this.onUnhandledMessage,We=$,pe=this,Ie=this._connectedVersion===p.V1_2?We.headers.ack:We.headers[`message-id`];We.ack=(F={})=>pe.ack(Ie,_e,F),We.nack=(F={})=>pe.nack(Ie,_e,F),bt(We)},RECEIPT:$=>{let _e=this._receiptWatchers[$.headers[`receipt-id`]];_e?(_e($),delete this._receiptWatchers[$.headers[`receipt-id`]]):this.onUnhandledReceipt($)},ERROR:$=>{this.onStompError($)}},this._counter=0,this._subscriptions={},this._receiptWatchers={},this._partialData=``,this._escapeHeaderValues=!1,this._lastServerActivityTS=Date.now(),this.debug=O.debug,this.stompVersions=O.stompVersions,this.connectHeaders=O.connectHeaders,this.disconnectHeaders=O.disconnectHeaders,this.heartbeatIncoming=O.heartbeatIncoming,this.heartbeatToleranceMultiplier=O.heartbeatGracePeriods,this.heartbeatOutgoing=O.heartbeatOutgoing,this.splitLargeFrames=O.splitLargeFrames,this.maxWebSocketChunkSize=O.maxWebSocketChunkSize,this.forceBinaryWSFrames=O.forceBinaryWSFrames,this.logRawCommunication=O.logRawCommunication,this.appendMissingNULLonIncoming=O.appendMissingNULLonIncoming,this.discardWebsocketOnCommFailure=O.discardWebsocketOnCommFailure,this.onConnect=O.onConnect,this.onDisconnect=O.onDisconnect,this.onStompError=O.onStompError,this.onWebSocketClose=O.onWebSocketClose,this.onWebSocketError=O.onWebSocketError,this.onUnhandledMessage=O.onUnhandledMessage,this.onUnhandledReceipt=O.onUnhandledReceipt,this.onUnhandledFrame=O.onUnhandledFrame,this.onHeartbeatReceived=O.onHeartbeatReceived,this.onHeartbeatLost=O.onHeartbeatLost}start(){let x=new c(O=>{let $=i.fromRawFrame(O,this._escapeHeaderValues);this.logRawCommunication||this.debug(`<<< ${$}`),(this._serverFrameHandlers[$.command]||this.onUnhandledFrame)($)},()=>{this.debug(`<<< PONG`),this.onHeartbeatReceived()});this._webSocket.onmessage=O=>{if(this.debug(`Received data`),this._lastServerActivityTS=Date.now(),this.logRawCommunication){let $=O.data instanceof ArrayBuffer?new TextDecoder().decode(O.data):O.data;this.debug(`<<< ${$}`)}x.parseChunk(O.data,this.appendMissingNULLonIncoming)},this._webSocket.onclose=O=>{this.debug(`Connection closed to ${this._webSocket.url}`),this._cleanUp(),this.onWebSocketClose(O)},this._webSocket.onerror=O=>{this.onWebSocketError(O)};let M=()=>{let O=Object.assign({},this.connectHeaders);this.debug(`Web Socket Opened...`),O[`accept-version`]=this.stompVersions.supportedVersions(),O[`heart-beat`]=[this.heartbeatOutgoing,this.heartbeatIncoming].join(`,`),this._transmit({command:`CONNECT`,headers:O})};this._webSocket.readyState===t.StompSocketState.OPEN?M():this._webSocket.onopen=M}_setupHeartbeat(x){if(x.version!==p.V1_1&&x.version!==p.V1_2||!x[`heart-beat`])return;let[M,O]=x[`heart-beat`].split(`,`).map($=>parseInt($,10));if(this.heartbeatOutgoing!==0&&O!==0){let $=Math.max(this.heartbeatOutgoing,O);this.debug(`send PING every ${$}ms`),this._pinger=new h($,this._client.heartbeatStrategy,this.debug),this._pinger.start(()=>{this._webSocket.readyState===t.StompSocketState.OPEN&&(this._webSocket.send(e.LF),this.debug(`>>> PING`))})}if(this.heartbeatIncoming!==0&&M!==0){let $=Math.max(this.heartbeatIncoming,M);this.debug(`check PONG every ${$}ms`),this._ponger=setInterval(()=>{let _e=Date.now()-this._lastServerActivityTS;_e>$*this.heartbeatToleranceMultiplier&&(this.debug(`did not receive server activity for the last ${_e}ms`),this.onHeartbeatLost(),this._closeOrDiscardWebsocket())},$)}}_closeOrDiscardWebsocket(){this.discardWebsocketOnCommFailure?(this.debug(`Discarding websocket, the underlying socket may linger for a while`),this.discardWebsocket()):(this.debug(`Issuing close on the websocket`),this._closeWebsocket())}forceDisconnect(){this._webSocket&&(this._webSocket.readyState===t.StompSocketState.CONNECTING||this._webSocket.readyState===t.StompSocketState.OPEN)&&this._closeOrDiscardWebsocket()}_closeWebsocket(){this._webSocket.onmessage=()=>{},this._webSocket.close()}discardWebsocket(){typeof this._webSocket.terminate!=`function`&&n(this._webSocket,x=>this.debug(x)),this._webSocket.terminate()}_transmit(x){let{command:M,headers:O,body:$,binaryBody:_e,skipContentLengthHeader:bt}=x,We=new i({command:M,headers:O,body:$,binaryBody:_e,escapeHeaderValues:this._escapeHeaderValues,skipContentLengthHeader:bt}),pe=We.serialize();if(this.logRawCommunication?this.debug(`>>> ${pe}`):this.debug(`>>> ${We}`),this.forceBinaryWSFrames&&typeof pe==`string`&&(pe=new TextEncoder().encode(pe)),typeof pe!=`string`||!this.splitLargeFrames)this._webSocket.send(pe);else{let Ie=pe;for(;Ie.length>0;){let F=Ie.substring(0,this.maxWebSocketChunkSize);Ie=Ie.substring(this.maxWebSocketChunkSize),this._webSocket.send(F),this.debug(`chunk sent = ${F.length}, remaining = ${Ie.length}`)}}}dispose(){if(this.connected)try{let x=Object.assign({},this.disconnectHeaders);x.receipt||(x.receipt=`close-${this._counter++}`),this.watchForReceipt(x.receipt,M=>{this._closeWebsocket(),this._cleanUp(),this.onDisconnect(M)}),this._transmit({command:`DISCONNECT`,headers:x})}catch(x){this.debug(`Ignoring error during disconnect ${x}`)}else(this._webSocket.readyState===t.StompSocketState.CONNECTING||this._webSocket.readyState===t.StompSocketState.OPEN)&&this._closeWebsocket()}_cleanUp(){this._connected=!1,this._pinger&&(this._pinger.stop(),this._pinger=void 0),this._ponger&&(clearInterval(this._ponger),this._ponger=void 0)}publish(x){let{destination:M,headers:O,body:$,binaryBody:_e,skipContentLengthHeader:bt}=x,We=Object.assign({destination:M},O);this._transmit({command:`SEND`,headers:We,body:$,binaryBody:_e,skipContentLengthHeader:bt})}watchForReceipt(x,M){this._receiptWatchers[x]=M}subscribe(x,M,O={}){O=Object.assign({},O),O.id||(O.id=`sub-${this._counter++}`),O.destination=x,this._subscriptions[O.id]=M,this._transmit({command:`SUBSCRIBE`,headers:O});let $=this;return{id:O.id,unsubscribe(_e){return $.unsubscribe(O.id,_e)}}}unsubscribe(x,M={}){M=Object.assign({},M),delete this._subscriptions[x],M.id=x,this._transmit({command:`UNSUBSCRIBE`,headers:M})}begin(x){let M=x||`tx-${this._counter++}`;this._transmit({command:`BEGIN`,headers:{transaction:M}});let O=this;return{id:M,commit(){O.commit(M)},abort(){O.abort(M)}}}commit(x){this._transmit({command:`COMMIT`,headers:{transaction:x}})}abort(x){this._transmit({command:`ABORT`,headers:{transaction:x}})}ack(x,M,O={}){O=Object.assign({},O),this._connectedVersion===p.V1_2?O.id=x:O[`message-id`]=x,O.subscription=M,this._transmit({command:`ACK`,headers:O})}nack(x,M,O={}){return O=Object.assign({},O),this._connectedVersion===p.V1_2?O.id=x:O[`message-id`]=x,O.subscription=M,this._transmit({command:`NACK`,headers:O})}}class v{get webSocket(){return this._stompHandler?._webSocket}get disconnectHeaders(){return this._disconnectHeaders}set disconnectHeaders(x){this._disconnectHeaders=x,this._stompHandler&&(this._stompHandler.disconnectHeaders=this._disconnectHeaders)}get connected(){return!!this._stompHandler&&this._stompHandler.connected}get connectedVersion(){return this._stompHandler?this._stompHandler.connectedVersion:void 0}get active(){return this.state===t.ActivationState.ACTIVE}_changeState(x){this.state=x,this.onChangeState(x)}constructor(x={}){this.stompVersions=p.default,this.connectionTimeout=0,this.reconnectDelay=5e3,this._nextReconnectDelay=0,this.maxReconnectDelay=9e5,this.reconnectTimeMode=t.ReconnectionTimeMode.LINEAR,this.heartbeatIncoming=1e4,this.heartbeatToleranceMultiplier=2,this.heartbeatOutgoing=1e4,this.heartbeatStrategy=t.TickerStrategy.Interval,this.splitLargeFrames=!1,this.maxWebSocketChunkSize=8192,this.forceBinaryWSFrames=!1,this.appendMissingNULLonIncoming=!1,this.discardWebsocketOnCommFailure=!1,this.state=t.ActivationState.INACTIVE;let M=()=>{};this.debug=M,this.beforeConnect=M,this.onConnect=M,this.onDisconnect=M,this.onUnhandledMessage=M,this.onUnhandledReceipt=M,this.onUnhandledFrame=M,this.onHeartbeatReceived=M,this.onHeartbeatLost=M,this.onStompError=M,this.onWebSocketClose=M,this.onWebSocketError=M,this.logRawCommunication=!1,this.onChangeState=M,this.connectHeaders={},this._disconnectHeaders={},this.configure(x)}configure(x){Object.assign(this,x),this.maxReconnectDelay>0&&this.maxReconnectDelay<this.reconnectDelay&&(this.debug(`Warning: maxReconnectDelay (${this.maxReconnectDelay}ms) is less than reconnectDelay (${this.reconnectDelay}ms). Using reconnectDelay as the maxReconnectDelay delay.`),this.maxReconnectDelay=this.reconnectDelay)}activate(){let x=()=>{if(this.active){this.debug(`Already ACTIVE, ignoring request to activate`);return}this._changeState(t.ActivationState.ACTIVE),this._nextReconnectDelay=this.reconnectDelay,this._connect()};this.state===t.ActivationState.DEACTIVATING?(this.debug(`Waiting for deactivation to finish before activating`),this.deactivate().then(()=>{x()})):x()}async _connect(){if(await this.beforeConnect(this),this._stompHandler){this.debug(`There is already a stompHandler, skipping the call to connect`);return}if(!this.active){this.debug(`Client has been marked inactive, will not attempt to connect`);return}this.connectionTimeout>0&&(this._connectionWatcher&&clearTimeout(this._connectionWatcher),this._connectionWatcher=setTimeout(()=>{this.connected||(this.debug(`Connection not established in ${this.connectionTimeout}ms, closing socket`),this.forceDisconnect())},this.connectionTimeout)),this.debug(`Opening Web Socket...`);let x=this._createWebSocket();this._stompHandler=new g(this,x,{debug:this.debug,stompVersions:this.stompVersions,connectHeaders:this.connectHeaders,disconnectHeaders:this._disconnectHeaders,heartbeatIncoming:this.heartbeatIncoming,heartbeatGracePeriods:this.heartbeatToleranceMultiplier,heartbeatOutgoing:this.heartbeatOutgoing,heartbeatStrategy:this.heartbeatStrategy,splitLargeFrames:this.splitLargeFrames,maxWebSocketChunkSize:this.maxWebSocketChunkSize,forceBinaryWSFrames:this.forceBinaryWSFrames,logRawCommunication:this.logRawCommunication,appendMissingNULLonIncoming:this.appendMissingNULLonIncoming,discardWebsocketOnCommFailure:this.discardWebsocketOnCommFailure,onConnect:M=>{if(this._connectionWatcher&&(clearTimeout(this._connectionWatcher),this._connectionWatcher=void 0),this._nextReconnectDelay=this.reconnectDelay,!this.active){this.debug(`STOMP got connected while deactivate was issued, will disconnect now`),this._disposeStompHandler();return}this.onConnect(M)},onDisconnect:M=>{this.onDisconnect(M)},onStompError:M=>{this.onStompError(M)},onWebSocketClose:M=>{this._stompHandler=void 0,this.state===t.ActivationState.DEACTIVATING&&this._changeState(t.ActivationState.INACTIVE),this.onWebSocketClose(M),this.active&&this._schedule_reconnect()},onWebSocketError:M=>{this.onWebSocketError(M)},onUnhandledMessage:M=>{this.onUnhandledMessage(M)},onUnhandledReceipt:M=>{this.onUnhandledReceipt(M)},onUnhandledFrame:M=>{this.onUnhandledFrame(M)},onHeartbeatReceived:()=>{this.onHeartbeatReceived()},onHeartbeatLost:()=>{this.onHeartbeatLost()}}),this._stompHandler.start()}_createWebSocket(){let x;if(this.webSocketFactory)x=this.webSocketFactory();else if(this.brokerURL)x=new WebSocket(this.brokerURL,this.stompVersions.protocolVersions());else throw new Error(`Either brokerURL or webSocketFactory must be provided`);return x.binaryType=`arraybuffer`,x}_schedule_reconnect(){this._nextReconnectDelay>0&&(this.debug(`STOMP: scheduling reconnection in ${this._nextReconnectDelay}ms`),this._reconnector=setTimeout(()=>{this.reconnectTimeMode===t.ReconnectionTimeMode.EXPONENTIAL&&(this._nextReconnectDelay=this._nextReconnectDelay*2,this.maxReconnectDelay!==0&&(this._nextReconnectDelay=Math.min(this._nextReconnectDelay,this.maxReconnectDelay))),this._connect()},this._nextReconnectDelay))}async deactivate(x={}){let M=x.force||!1,O=this.active,$;if(this.state===t.ActivationState.INACTIVE)return this.debug(`Already INACTIVE, nothing more to do`),Promise.resolve();if(this._changeState(t.ActivationState.DEACTIVATING),this._nextReconnectDelay=0,this._reconnector&&(clearTimeout(this._reconnector),this._reconnector=void 0),this._stompHandler&&this.webSocket.readyState!==t.StompSocketState.CLOSED){let _e=this._stompHandler.onWebSocketClose;$=new Promise((bt,We)=>{this._stompHandler.onWebSocketClose=pe=>{_e(pe),bt()}})}else return this._changeState(t.ActivationState.INACTIVE),Promise.resolve();return M?this._stompHandler?.discardWebsocket():O&&this._disposeStompHandler(),$}forceDisconnect(){this._stompHandler&&this._stompHandler.forceDisconnect()}_disposeStompHandler(){this._stompHandler&&this._stompHandler.dispose()}publish(x){this._checkConnection(),this._stompHandler.publish(x)}_checkConnection(){if(!this.connected)throw new TypeError(`There is no underlying STOMP connection`)}watchForReceipt(x,M){this._checkConnection(),this._stompHandler.watchForReceipt(x,M)}subscribe(x,M,O={}){return this._checkConnection(),this._stompHandler.subscribe(x,M,O)}unsubscribe(x,M={}){this._checkConnection(),this._stompHandler.unsubscribe(x,M)}begin(x){return this._checkConnection(),this._stompHandler.begin(x)}commit(x){this._checkConnection(),this._stompHandler.commit(x)}abort(x){this._checkConnection(),this._stompHandler.abort(x)}ack(x,M,O={}){this._checkConnection(),this._stompHandler.ack(x,M,O)}nack(x,M,O={}){this._checkConnection(),this._stompHandler.nack(x,M,O)}}class b{}class y{}class E{constructor(x){this.client=x}get outgoing(){return this.client.heartbeatOutgoing}set outgoing(x){this.client.heartbeatOutgoing=x}get incoming(){return this.client.heartbeatIncoming}set incoming(x){this.client.heartbeatIncoming=x}}class P extends v{constructor(x){super(),this.maxWebSocketFrameSize=16384,this._heartbeatInfo=new E(this),this.reconnect_delay=0,this.webSocketFactory=x,this.debug=(...M)=>{console.log(...M)}}_parseConnect(...x){let M,O,$,_e={};if(x.length<2)throw new Error(`Connect requires at least 2 arguments`);return typeof x[1]==`function`?[_e,O,$,M]=x:x.length===6?[_e.login,_e.passcode,O,$,M,_e.host]=x:[_e.login,_e.passcode,O,$,M]=x,[_e,O,$,M]}connect(...x){let M=this._parseConnect(...x);M[0]&&(this.connectHeaders=M[0]),M[1]&&(this.onConnect=M[1]),M[2]&&(this.onStompError=M[2]),M[3]&&(this.onWebSocketClose=M[3]),super.activate()}disconnect(x,M={}){x&&(this.onDisconnect=x),this.disconnectHeaders=M,super.deactivate()}send(x,M={},O=``){M=Object.assign({},M);let $=M[`content-length`]===!1;$&&delete M[`content-length`],this.publish({destination:x,headers:M,body:O,skipContentLengthHeader:$})}set reconnect_delay(x){this.reconnectDelay=x}get ws(){return this.webSocket}get version(){return this.connectedVersion}get onreceive(){return this.onUnhandledMessage}set onreceive(x){this.onUnhandledMessage=x}get onreceipt(){return this.onUnhandledReceipt}set onreceipt(x){this.onUnhandledReceipt=x}get heartbeat(){return this._heartbeatInfo}set heartbeat(x){this.heartbeatIncoming=x.incoming,this.heartbeatOutgoing=x.outgoing}}class ae{static client(x,M){M??=p.default.protocolVersions();let O=()=>{return new(ae.WebSocketClass||WebSocket)(x,M)};return new P(O)}static over(x){let M;return typeof x==`function`?M=x:(console.warn(`Stomp.over did not receive a factory, auto reconnect will not work. Please see https://stomp-js.github.io/api-docs/latest/classes/Stomp.html#over`),M=()=>x),new P(M)}}ae.WebSocketClass=null,t.Client=v,t.CompatClient=P,t.FrameImpl=i,t.Parser=c,t.Stomp=ae,t.StompConfig=b,t.StompHeaders=y,t.Versions=p}))});var an=null;var Jc=!1;var za=1;var uE=null;var Ct=Symbol(`SIGNAL`);function ge(t){let n=an;return an=t,n}function il(){return an}var Ua={version:0,lastCleanEpoch:0,dirty:!1,producers:void 0,producersTail:void 0,consumers:void 0,consumersTail:void 0,recomputing:!1,consumerAllowSignalWrites:!1,consumerIsAlwaysLive:!1,kind:`unknown`,producerMustRecompute:()=>!1,producerRecomputeValue:()=>{},consumerMarkedDirty:()=>{},consumerOnSignalRead:()=>{}};function Oi(t){if(Jc)throw new Error(``);if(an===null)return;an.consumerOnSignalRead(t);let n=an.producersTail;if(n!==void 0&&n.producer===t)return;let e,i=an.recomputing;if(i&&(e=n!==void 0?n.nextProducer:an.producers,e!==void 0&&e.producer===t)){an.producersTail=e,e.lastReadVersion=t.version,e.knownValidAtEpoch=za;return}let a=t.consumersTail;if(a!==void 0&&a.consumer===an&&(!i||a.knownValidAtEpoch===za))return;let o=Go(an),r={producer:t,consumer:an,nextProducer:e,prevConsumer:void 0,knownValidAtEpoch:za,lastReadVersion:t.version,nextConsumer:void 0};an.producersTail=r,n!==void 0?n.nextProducer=r:an.producers=r,o&&Gb(t,r)}function Hb(){za++}function ps(t){if(!(Go(t)&&!t.dirty)&&!(!t.dirty&&t.lastCleanEpoch===za)){if(!t.producerMustRecompute(t)&&!$o(t)){nl(t);return}t.producerRecomputeValue(t),nl(t)}}function Hm(t){if(t.consumers===void 0)return;let n=Jc;Jc=!0;try{for(let e=t.consumers;e!==void 0;e=e.nextConsumer){let i=e.consumer;i.dirty||mE(i)}}finally{Jc=n}}function Um(){return an?.consumerAllowSignalWrites!==!1}function mE(t){t.dirty=!0,Hm(t),t.consumerMarkedDirty?.(t)}function nl(t){t.dirty=!1,t.lastCleanEpoch=za}function la(t){return t&&Ub(t),ge(t)}function Ub(t){if(t.producersTail?.knownValidAtEpoch===za){let n=t.producers;for(;n!==void 0;)n.knownValidAtEpoch=null,n=n.nextProducer}t.producersTail=void 0,t.recomputing=!0}function $a(t,n){ge(n),t&&$b(t)}function $b(t){t.recomputing=!1;let n=t.producersTail,e=n!==void 0?n.nextProducer:t.producers;if(e!==void 0){if(Go(t))do e=$m(e);while(e!==void 0);n!==void 0?n.nextProducer=void 0:t.producers=void 0}}function $o(t){for(let n=t.producers;n!==void 0;n=n.nextProducer){let e=n.producer,i=n.lastReadVersion;if(i!==e.version||(ps(e),i!==e.version))return!0}return!1}function da(t){if(Go(t)){let n=t.producers;for(;n!==void 0;)n=$m(n)}t.producers=void 0,t.producersTail=void 0,t.consumers=void 0,t.consumersTail=void 0}function Gb(t,n){let e=t.consumersTail,i=Go(t);if(e!==void 0?(n.nextConsumer=e.nextConsumer,e.nextConsumer=n):(n.nextConsumer=void 0,t.consumers=n),n.prevConsumer=e,t.consumersTail=n,!i)for(let a=t.producers;a!==void 0;a=a.nextProducer)Gb(a.producer,a)}function $m(t){let n=t.producer,e=t.nextProducer,i=t.nextConsumer,a=t.prevConsumer;if(t.nextConsumer=void 0,t.prevConsumer=void 0,i!==void 0?i.prevConsumer=a:n.consumersTail=a,a!==void 0)a.nextConsumer=i;else if(n.consumers=i,!Go(n)){let o=n.producers;for(;o!==void 0;)o=$m(o)}return e}function Go(t){return t.consumerIsAlwaysLive||t.consumers!==void 0}function al(t){uE?.(t)}function ol(t,n){return Object.is(t,n)}function hs(t,n){let e=Object.create(pE);e.computation=t,n!==void 0&&(e.equal=n);let i=()=>{if(ps(e),Oi(e),e.value===Ha)throw e.error;return e.value};return i[Ct]=e,al(e),i}var el=Symbol(`UNSET`);var tl=Symbol(`COMPUTING`);var Ha=Symbol(`ERRORED`);var pE=s(r({},Ua),{value:el,dirty:!0,error:null,equal:ol,kind:`computed`,producerMustRecompute(t){return t.value===el||t.value===tl},producerRecomputeValue(t){if(t.value===tl)throw new Error(``);let n=t.value;t.value=tl;let e=la(t),i,a=!1;try{i=t.computation(),ge(null),a=n!==el&&n!==Ha&&i!==Ha&&t.equal(n,i)}catch(o){i=Ha,t.error=o}finally{$a(t,e)}if(a){t.value=n;return}t.value=i,t.version++}});function hE(){throw new Error}var Wb=hE;function qb(t){Wb(t)}function Gm(t){Wb=t}var fE=null;function Wm(t,n){let e=Object.create(Wo);e.value=t,n!==void 0&&(e.equal=n);let i=()=>Yb(e);return i[Ct]=e,al(e),[i,r=>ua(e,r),r=>qm(e,r)]}function Yb(t){return Oi(t),t.value}function ua(t,n){Um()||qb(t),t.equal(t.value,n)||(t.value=n,gE(t))}function qm(t,n){Um()||qb(t),ua(t,n(t.value))}var Wo=s(r({},Ua),{equal:ol,value:void 0,kind:`signal`});function gE(t){t.version++,Hb(),Hm(t),fE?.(t)}var Ym=s(r({},Ua),{consumerIsAlwaysLive:!0,consumerAllowSignalWrites:!0,dirty:!0,kind:`effect`});function Km(t){if(t.dirty=!1,t.version>0&&!$o(t))return;t.version++;let n=la(t);try{t.cleanup(),t.fn()}finally{$a(t,n)}}var Qm;function rl(){return Qm}function pi(t){let n=Qm;return Qm=t,n}var Kb=Symbol(`NotFound`);function qo(t){return t===Kb||t?.name===`ɵNotFound`}function Qb(t){let n=ge(null);try{return t()}finally{ge(n)}}function Me(t){return typeof t==`function`}function Yo(t){let e=t(i=>{Error.call(i),i.stack=new Error().stack});return e.prototype=Object.create(Error.prototype),e.prototype.constructor=e,e}var sl=Yo(t=>function(e){t(this),this.message=e?`${e.length} errors occurred during unsubscription:
${e.map((i,a)=>`${a+1}) ${i.toString()}`).join(`
  `)}`:``,this.name=`UnsubscriptionError`,this.errors=e});function Ga(t,n){if(t){let e=t.indexOf(n);0<=e&&t.splice(e,1)}}var De=class t{constructor(n){this.initialTeardown=n,this.closed=!1,this._parentage=null,this._finalizers=null}unsubscribe(){let n;if(!this.closed){this.closed=!0;let{_parentage:e}=this;if(e)if(this._parentage=null,Array.isArray(e))for(let o of e)o.remove(this);else e.remove(this);let{initialTeardown:i}=this;if(Me(i))try{i()}catch(o){n=o instanceof sl?o.errors:[o]}let{_finalizers:a}=this;if(a){this._finalizers=null;for(let o of a)try{Xb(o)}catch(r){n=n??[],r instanceof sl?n=[...n,...r.errors]:n.push(r)}}if(n)throw new sl(n)}}add(n){var e;if(n&&n!==this)if(this.closed)Xb(n);else{if(n instanceof t){if(n.closed||n._hasParent(this))return;n._addParent(this)}(this._finalizers=(e=this._finalizers)!==null&&e!==void 0?e:[]).push(n)}}_hasParent(n){let{_parentage:e}=this;return e===n||Array.isArray(e)&&e.includes(n)}_addParent(n){let{_parentage:e}=this;this._parentage=Array.isArray(e)?(e.push(n),e):e?[e,n]:n}_removeParent(n){let{_parentage:e}=this;e===n?this._parentage=null:Array.isArray(e)&&Ga(e,n)}remove(n){let{_finalizers:e}=this;e&&Ga(e,n),n instanceof t&&n._removeParent(this)}};De.EMPTY=(()=>{let t=new De;return t.closed=!0,t})();var Xm=De.EMPTY;function cl(t){return t instanceof De||t&&`closed`in t&&Me(t.remove)&&Me(t.add)&&Me(t.unsubscribe)}function Xb(t){Me(t)?t():t.unsubscribe()}var Gn={onUnhandledError:null,onStoppedNotification:null,Promise:void 0,useDeprecatedSynchronousErrorHandling:!1,useDeprecatedNextContext:!1};var Ko={setTimeout(t,n,...e){let{delegate:i}=Ko;return i?.setTimeout?i.setTimeout(t,n,...e):setTimeout(t,n,...e)},clearTimeout(t){let{delegate:n}=Ko;return(n?.clearTimeout||clearTimeout)(t)},delegate:void 0};function ll(t){Ko.setTimeout(()=>{let{onUnhandledError:n}=Gn;if(n)n(t);else throw t})}function fs(){}var Zb=Zm(`C`,void 0,void 0);function Jb(t){return Zm(`E`,void 0,t)}function ev(t){return Zm(`N`,t,void 0)}function Zm(t,n,e){return{kind:t,value:n,error:e}}var Wa=null;function Qo(t){if(Gn.useDeprecatedSynchronousErrorHandling){let n=!Wa;if(n&&(Wa={errorThrown:!1,error:null}),t(),n){let{errorThrown:e,error:i}=Wa;if(Wa=null,e)throw i}}else t()}function tv(t){Gn.useDeprecatedSynchronousErrorHandling&&Wa&&(Wa.errorThrown=!0,Wa.error=t)}var qa=class extends De{constructor(n){super(),this.isStopped=!1,n?(this.destination=n,cl(n)&&n.add(this)):this.destination=_E}static create(n,e,i){return new Wn(n,e,i)}next(n){this.isStopped?ep(ev(n),this):this._next(n)}error(n){this.isStopped?ep(Jb(n),this):(this.isStopped=!0,this._error(n))}complete(){this.isStopped?ep(Zb,this):(this.isStopped=!0,this._complete())}unsubscribe(){this.closed||(this.isStopped=!0,super.unsubscribe(),this.destination=null)}_next(n){this.destination.next(n)}_error(n){try{this.destination.error(n)}finally{this.unsubscribe()}}_complete(){try{this.destination.complete()}finally{this.unsubscribe()}}};var bE=Function.prototype.bind;function Jm(t,n){return bE.call(t,n)}var tp=class{constructor(n){this.partialObserver=n}next(n){let{partialObserver:e}=this;if(e.next)try{e.next(n)}catch(i){dl(i)}}error(n){let{partialObserver:e}=this;if(e.error)try{e.error(n)}catch(i){dl(i)}else dl(n)}complete(){let{partialObserver:n}=this;if(n.complete)try{n.complete()}catch(e){dl(e)}}};var Wn=class extends qa{constructor(n,e,i){super();let a;if(Me(n)||!n)a={next:n??void 0,error:e??void 0,complete:i??void 0};else{let o;this&&Gn.useDeprecatedNextContext?(o=Object.create(n),o.unsubscribe=()=>this.unsubscribe(),a={next:n.next&&Jm(n.next,o),error:n.error&&Jm(n.error,o),complete:n.complete&&Jm(n.complete,o)}):a=n}this.destination=new tp(a)}};function dl(t){Gn.useDeprecatedSynchronousErrorHandling?tv(t):ll(t)}function vE(t){throw t}function ep(t,n){let{onStoppedNotification:e}=Gn;e&&Ko.setTimeout(()=>e(t,n))}var _E={closed:!0,next:fs,error:vE,complete:fs};var Xo=typeof Symbol==`function`&&Symbol.observable||`@@observable`;function qn(t){return t}function nv(t){return t.length===0?qn:t.length===1?t[0]:function(e){return t.reduce((i,a)=>a(i),e)}}var Ce=class t{constructor(n){n&&(this._subscribe=n)}lift(n){let e=new t;return e.source=this,e.operator=n,e}subscribe(n,e,i){let a=xE(n)?n:new Wn(n,e,i);return Qo(()=>{let{operator:o,source:r}=this;a.add(o?o.call(a,r):r?this._subscribe(a):this._trySubscribe(a))}),a}_trySubscribe(n){try{return this._subscribe(n)}catch(e){n.error(e)}}forEach(n,e){return e=iv(e),new e((i,a)=>{let o=new Wn({next:r=>{try{n(r)}catch(s){a(s),o.unsubscribe()}},error:a,complete:i});this.subscribe(o)})}_subscribe(n){var e;return(e=this.source)===null||e===void 0?void 0:e.subscribe(n)}[Xo](){return this}pipe(...n){return nv(n)(this)}toPromise(n){return n=iv(n),new n((e,i)=>{let a;this.subscribe(o=>a=o,o=>i(o),()=>e(a))})}};Ce.create=t=>new Ce(t);function iv(t){var n;return(n=t??Gn.Promise)!==null&&n!==void 0?n:Promise}function yE(t){return t&&Me(t.next)&&Me(t.error)&&Me(t.complete)}function xE(t){return t&&t instanceof qa||yE(t)&&cl(t)}function CE(t){return Me(t?.lift)}function Le(t){return n=>{if(CE(n))return n.lift(function(e){try{return t(e,this)}catch(i){this.error(i)}});throw new TypeError(`Unable to lift unknown Observable type`)}}function je(t,n,e,i,a){return new np(t,n,e,i,a)}var np=class extends qa{constructor(n,e,i,a,o,r){super(n),this.onFinalize=o,this.shouldUnsubscribe=r,this._next=e?function(s){try{e(s)}catch(c){n.error(c)}}:super._next,this._error=a?function(s){try{a(s)}catch(c){n.error(c)}finally{this.unsubscribe()}}:super._error,this._complete=i?function(){try{i()}catch(s){n.error(s)}finally{this.unsubscribe()}}:super._complete}unsubscribe(){var n;if(!this.shouldUnsubscribe||this.shouldUnsubscribe()){let{closed:e}=this;super.unsubscribe(),!e&&((n=this.onFinalize)===null||n===void 0||n.call(this))}}};var av=Yo(t=>function(){t(this),this.name=`ObjectUnsubscribedError`,this.message=`object unsubscribed`});var L=class extends Ce{constructor(){super(),this.closed=!1,this.currentObservers=null,this.observers=[],this.isStopped=!1,this.hasError=!1,this.thrownError=null}lift(n){let e=new ul(this,this);return e.operator=n,e}_throwIfClosed(){if(this.closed)throw new av}next(n){Qo(()=>{if(this._throwIfClosed(),!this.isStopped){this.currentObservers||(this.currentObservers=Array.from(this.observers));for(let e of this.currentObservers)e.next(n)}})}error(n){Qo(()=>{if(this._throwIfClosed(),!this.isStopped){this.hasError=this.isStopped=!0,this.thrownError=n;let{observers:e}=this;for(;e.length;)e.shift().error(n)}})}complete(){Qo(()=>{if(this._throwIfClosed(),!this.isStopped){this.isStopped=!0;let{observers:n}=this;for(;n.length;)n.shift().complete()}})}unsubscribe(){this.isStopped=this.closed=!0,this.observers=this.currentObservers=null}get observed(){var n;return((n=this.observers)===null||n===void 0?void 0:n.length)>0}_trySubscribe(n){return this._throwIfClosed(),super._trySubscribe(n)}_subscribe(n){return this._throwIfClosed(),this._checkFinalizedStatuses(n),this._innerSubscribe(n)}_innerSubscribe(n){let{hasError:e,isStopped:i,observers:a}=this;return e||i?Xm:(this.currentObservers=null,a.push(n),new De(()=>{this.currentObservers=null,Ga(a,n)}))}_checkFinalizedStatuses(n){let{hasError:e,thrownError:i,isStopped:a}=this;e?n.error(i):a&&n.complete()}asObservable(){let n=new Ce;return n.source=this,n}};L.create=(t,n)=>new ul(t,n);var ul=class extends L{constructor(n,e){super(),this.destination=n,this.source=e}next(n){var e,i;(i=(e=this.destination)===null||e===void 0?void 0:e.next)===null||i===void 0||i.call(e,n)}error(n){var e,i;(i=(e=this.destination)===null||e===void 0?void 0:e.error)===null||i===void 0||i.call(e,n)}complete(){var n,e;(e=(n=this.destination)===null||n===void 0?void 0:n.complete)===null||e===void 0||e.call(n)}_subscribe(n){var e,i;return(i=(e=this.source)===null||e===void 0?void 0:e.subscribe(n))!==null&&i!==void 0?i:Xm}};var hi=class extends L{constructor(n){super(),this._value=n}get value(){return this.getValue()}_subscribe(n){let e=super._subscribe(n);return!e.closed&&n.next(this._value),e}getValue(){let{hasError:n,thrownError:e,_value:i}=this;if(n)throw e;return this._throwIfClosed(),i}next(n){super.next(this._value=n)}};var gs={now(){return(gs.delegate||Date).now()},delegate:void 0};var Yn=class extends L{constructor(n=1/0,e=1/0,i=gs){super(),this._bufferSize=n,this._windowTime=e,this._timestampProvider=i,this._buffer=[],this._infiniteTimeWindow=!0,this._infiniteTimeWindow=e===1/0,this._bufferSize=Math.max(1,n),this._windowTime=Math.max(1,e)}next(n){let{isStopped:e,_buffer:i,_infiniteTimeWindow:a,_timestampProvider:o,_windowTime:r}=this;e||(i.push(n),!a&&i.push(o.now()+r)),this._trimBuffer(),super.next(n)}_subscribe(n){this._throwIfClosed(),this._trimBuffer();let e=this._innerSubscribe(n),{_infiniteTimeWindow:i,_buffer:a}=this,o=a.slice();for(let r=0;r<o.length&&!n.closed;r+=i?1:2)n.next(o[r]);return this._checkFinalizedStatuses(n),e}_trimBuffer(){let{_bufferSize:n,_timestampProvider:e,_buffer:i,_infiniteTimeWindow:a}=this,o=(a?1:2)*n;if(n<1/0&&o<i.length&&i.splice(0,i.length-o),!a){let r=e.now(),s=0;for(let c=1;c<i.length&&i[c]<=r;c+=2)s=c;s&&i.splice(0,s+1)}}};var ml=class extends De{constructor(n,e){super()}schedule(n,e=0){return this}};var bs={setInterval(t,n,...e){let{delegate:i}=bs;return i?.setInterval?i.setInterval(t,n,...e):setInterval(t,n,...e)},clearInterval(t){let{delegate:n}=bs;return(n?.clearInterval||clearInterval)(t)},delegate:void 0};var pl=class extends ml{constructor(n,e){super(n,e),this.scheduler=n,this.work=e,this.pending=!1}schedule(n,e=0){var i;if(this.closed)return this;this.state=n;let a=this.id,o=this.scheduler;return a!=null&&(this.id=this.recycleAsyncId(o,a,e)),this.pending=!0,this.delay=e,this.id=(i=this.id)!==null&&i!==void 0?i:this.requestAsyncId(o,this.id,e),this}requestAsyncId(n,e,i=0){return bs.setInterval(n.flush.bind(n,this),i)}recycleAsyncId(n,e,i=0){if(i!=null&&this.delay===i&&this.pending===!1)return e;e!=null&&bs.clearInterval(e)}execute(n,e){if(this.closed)return new Error(`executing a cancelled action`);this.pending=!1;let i=this._execute(n,e);if(i)return i;this.pending===!1&&this.id!=null&&(this.id=this.recycleAsyncId(this.scheduler,this.id,null))}_execute(n,e){let i=!1,a;try{this.work(n)}catch(o){i=!0,a=o||new Error(`Scheduled action threw falsy error`)}if(i)return this.unsubscribe(),a}unsubscribe(){if(!this.closed){let{id:n,scheduler:e}=this,{actions:i}=e;this.work=this.state=this.scheduler=null,this.pending=!1,Ga(i,this),n!=null&&(this.id=this.recycleAsyncId(e,n,null)),this.delay=null,super.unsubscribe()}}};var Zo=class t{constructor(n,e=t.now){this.schedulerActionCtor=n,this.now=e}schedule(n,e=0,i){return new this.schedulerActionCtor(this,n).schedule(i,e)}};Zo.now=gs.now;var hl=class extends Zo{constructor(n,e=Zo.now){super(n,e),this.actions=[],this._active=!1}flush(n){let{actions:e}=this;if(this._active){e.push(n);return}let i;this._active=!0;do if(i=n.execute(n.state,n.delay))break;while(n=e.shift());if(this._active=!1,i){for(;n=e.shift();)n.unsubscribe();throw i}}};var vs=new hl(pl);var ov=vs;var Ri=new Ce(t=>t.complete());function fl(t){return t&&Me(t.schedule)}function ip(t){return t[t.length-1]}function gl(t){return Me(ip(t))?t.pop():void 0}function fi(t){return fl(ip(t))?t.pop():void 0}function rv(t,n){return typeof ip(t)==`number`?t.pop():n}function cv(t,n,e,i){function a(o){return o instanceof e?o:new e(function(r){r(o)})}return new(e||(e=Promise))(function(o,r){function s(p){try{h(i.next(p))}catch(g){r(g)}}function c(p){try{h(i.throw(p))}catch(g){r(g)}}function h(p){p.done?o(p.value):a(p.value).then(s,c)}h((i=i.apply(t,n||[])).next())})}function sv(t){var n=typeof Symbol==`function`&&Symbol.iterator,e=n&&t[n],i=0;if(e)return e.call(t);if(t&&typeof t.length==`number`)return{next:function(){return t&&i>=t.length&&(t=void 0),{value:t&&t[i++],done:!t}}};throw new TypeError(n?`Object is not iterable.`:`Symbol.iterator is not defined.`)}function Ya(t){return this instanceof Ya?(this.v=t,this):new Ya(t)}function lv(t,n,e){if(!Symbol.asyncIterator)throw new TypeError(`Symbol.asyncIterator is not defined.`);var i=e.apply(t,n||[]),a,o=[];return a=Object.create((typeof AsyncIterator==`function`?AsyncIterator:Object).prototype),s(`next`),s(`throw`),s(`return`,r),a[Symbol.asyncIterator]=function(){return this},a;function r(b){return function(y){return Promise.resolve(y).then(b,g)}}function s(b,y){i[b]&&(a[b]=function(E){return new Promise(function(P,ae){o.push([b,E,P,ae])>1||c(b,E)})},y&&(a[b]=y(a[b])))}function c(b,y){try{h(i[b](y))}catch(E){v(o[0][3],E)}}function h(b){b.value instanceof Ya?Promise.resolve(b.value.v).then(p,g):v(o[0][2],b)}function p(b){c(`next`,b)}function g(b){c(`throw`,b)}function v(b,y){b(y),o.shift(),o.length&&c(o[0][0],o[0][1])}}function dv(t){if(!Symbol.asyncIterator)throw new TypeError(`Symbol.asyncIterator is not defined.`);var n=t[Symbol.asyncIterator],e;return n?n.call(t):(t=typeof sv==`function`?sv(t):t[Symbol.iterator](),e={},i(`next`),i(`throw`),i(`return`),e[Symbol.asyncIterator]=function(){return this},e);function i(o){e[o]=t[o]&&function(r){return new Promise(function(s,c){r=t[o](r),a(s,c,r.done,r.value)})}}function a(o,r,s,c){Promise.resolve(c).then(function(h){o({value:h,done:s})},r)}}var bl=(t=>t&&typeof t.length==`number`&&typeof t!=`function`);function vl(t){return Me(t?.then)}function _l(t){return Me(t[Xo])}function yl(t){return Symbol.asyncIterator&&Me(t?.[Symbol.asyncIterator])}function xl(t){return new TypeError(`You provided ${t!==null&&typeof t==`object`?`an invalid object`:`'${t}'`} where a stream was expected. You can provide an Observable, Promise, ReadableStream, Array, AsyncIterable, or Iterable.`)}function SE(){return typeof Symbol!=`function`||!Symbol.iterator?`@@iterator`:Symbol.iterator}var Cl=SE();function Sl(t){return Me(t?.[Cl])}function wl(t){return lv(this,arguments,function*(){let e=t.getReader();try{for(;;){let{value:i,done:a}=yield Ya(e.read());if(a)return yield Ya(void 0);yield yield Ya(i)}}finally{e.releaseLock()}})}function El(t){return Me(t?.getReader)}function ut(t){if(t instanceof Ce)return t;if(t!=null){if(_l(t))return wE(t);if(bl(t))return EE(t);if(vl(t))return DE(t);if(yl(t))return uv(t);if(Sl(t))return IE(t);if(El(t))return kE(t)}throw xl(t)}function wE(t){return new Ce(n=>{let e=t[Xo]();if(Me(e.subscribe))return e.subscribe(n);throw new TypeError(`Provided object does not correctly implement Symbol.observable`)})}function EE(t){return new Ce(n=>{for(let e=0;e<t.length&&!n.closed;e++)n.next(t[e]);n.complete()})}function DE(t){return new Ce(n=>{t.then(e=>{n.closed||(n.next(e),n.complete())},e=>n.error(e)).then(null,ll)})}function IE(t){return new Ce(n=>{for(let e of t)if(n.next(e),n.closed)return;n.complete()})}function uv(t){return new Ce(n=>{ME(t,n).catch(e=>n.error(e))})}function kE(t){return uv(wl(t))}function ME(t,n){var e,i,a,o;return cv(this,void 0,void 0,function*(){try{for(e=dv(t);i=yield e.next(),!i.done;){let r=i.value;if(n.next(r),n.closed)return}}catch(r){a={error:r}}finally{try{i&&!i.done&&(o=e.return)&&(yield o.call(e))}finally{if(a)throw a.error}}n.complete()})}function mn(t,n,e,i=0,a=!1){let o=n.schedule(function(){e(),a?t.add(this.schedule(null,i)):this.unsubscribe()},i);if(t.add(o),!a)return o}function Dl(t,n=0){return Le((e,i)=>{e.subscribe(je(i,a=>mn(i,t,()=>i.next(a),n),()=>mn(i,t,()=>i.complete(),n),a=>mn(i,t,()=>i.error(a),n)))})}function Il(t,n=0){return Le((e,i)=>{i.add(t.schedule(()=>e.subscribe(i),n))})}function mv(t,n){return ut(t).pipe(Il(n),Dl(n))}function pv(t,n){return ut(t).pipe(Il(n),Dl(n))}function hv(t,n){return new Ce(e=>{let i=0;return n.schedule(function(){i===t.length?e.complete():(e.next(t[i++]),e.closed||this.schedule())})})}function fv(t,n){return new Ce(e=>{let i;return mn(e,n,()=>{i=t[Cl](),mn(e,n,()=>{let a,o;try{({value:a,done:o}=i.next())}catch(r){e.error(r);return}o?e.complete():e.next(a)},0,!0)}),()=>Me(i?.return)&&i.return()})}function kl(t,n){if(!t)throw new Error(`Iterable cannot be null`);return new Ce(e=>{mn(e,n,()=>{let i=t[Symbol.asyncIterator]();mn(e,n,()=>{i.next().then(a=>{a.done?e.complete():e.next(a.value)})},0,!0)})})}function gv(t,n){return kl(wl(t),n)}function bv(t,n){if(t!=null){if(_l(t))return mv(t,n);if(bl(t))return hv(t,n);if(vl(t))return pv(t,n);if(yl(t))return kl(t,n);if(Sl(t))return fv(t,n);if(El(t))return gv(t,n)}throw xl(t)}function kn(t,n){return n?bv(t,n):ut(t)}function Mn(...t){return kn(t,fi(t))}var vv=Yo(t=>function(){t(this),this.name=`EmptyError`,this.message=`no elements in sequence`});function St(t,n){let e=typeof n==`object`;return new Promise((i,a)=>{let o=new Wn({next:r=>{i(r),o.unsubscribe()},error:a,complete:()=>{e?i(n.defaultValue):a(new vv)}});t.subscribe(o)})}function _v(t){return t instanceof Date&&!isNaN(t)}function st(t,n){return Le((e,i)=>{let a=0;e.subscribe(je(i,o=>{i.next(t.call(n,o,a++))}))})}var{isArray:TE}=Array;function NE(t,n){return TE(n)?t(...n):t(n)}function Ml(t){return st(n=>NE(t,n))}var{isArray:AE}=Array,{getPrototypeOf:OE,prototype:RE,keys:PE}=Object;function Tl(t){if(t.length===1){let n=t[0];if(AE(n))return{args:n,keys:null};if(FE(n)){let e=PE(n);return{args:e.map(i=>n[i]),keys:e}}}return{args:t,keys:null}}function FE(t){return t&&typeof t==`object`&&OE(t)===RE}function Nl(t,n){return t.reduce((e,i,a)=>(e[i]=n[a],e),{})}function ap(...t){let n=fi(t),e=gl(t),{args:i,keys:a}=Tl(t);if(i.length===0)return kn([],n);let o=new Ce(LE(i,n,a?r=>Nl(a,r):qn));return e?o.pipe(Ml(e)):o}function LE(t,n,e=qn){return i=>{yv(n,()=>{let{length:a}=t,o=new Array(a),r=a,s=a;for(let c=0;c<a;c++)yv(n,()=>{let h=kn(t[c],n),p=!1;h.subscribe(je(i,g=>{o[c]=g,p||(p=!0,s--),s||i.next(e(o.slice()))},()=>{--r||i.complete()}))},i)},i)}}function yv(t,n,e){t?mn(e,t,n):n()}function xv(t,n,e,i,a,o,r,s){let c=[],h=0,p=0,g=!1,v=()=>{g&&!c.length&&!h&&n.complete()},b=E=>h<i?y(E):c.push(E),y=E=>{o&&n.next(E),h++;let P=!1;ut(e(E,p++)).subscribe(je(n,ae=>{a?.(ae),o?b(ae):n.next(ae)},()=>{P=!0},void 0,()=>{if(P)try{for(h--;c.length&&h<i;){let ae=c.shift();r?mn(n,r,()=>y(ae)):y(ae)}v()}catch(ae){n.error(ae)}}))};return t.subscribe(je(n,b,()=>{g=!0,v()})),()=>{s?.()}}function Jo(t,n,e=1/0){return Me(n)?Jo((i,a)=>st((o,r)=>n(i,o,a,r))(ut(t(i,a))),e):(typeof n==`number`&&(e=n),Le((i,a)=>xv(i,a,t,e)))}function Al(t=1/0){return Jo(qn,t)}function Cv(){return Al(1)}function er(...t){return Cv()(kn(t,fi(t)))}function Ka(t){return new Ce(n=>{ut(t()).subscribe(n)})}function op(...t){let n=gl(t),{args:e,keys:i}=Tl(t),a=new Ce(o=>{let{length:r}=e;if(!r){o.complete();return}let s=new Array(r),c=r,h=r;for(let p=0;p<r;p++){let g=!1;ut(e[p]).subscribe(je(o,v=>{g||(g=!0,h--),s[p]=v},()=>c--,void 0,()=>{(!c||!g)&&(h||o.next(i?Nl(i,s):s),o.complete())}))}});return n?a.pipe(Ml(n)):a}function _s(t=0,n,e=ov){let i=-1;return n!=null&&(fl(n)?e=n:i=n),new Ce(a=>{let o=_v(t)?+t-e.now():t;o<0&&(o=0);let r=0;return e.schedule(function(){a.closed||(a.next(r++),0<=i?this.schedule(void 0,i):a.complete())},o)})}function It(...t){let n=fi(t),e=rv(t,1/0),i=t;return i.length?i.length===1?ut(i[0]):Al(e)(kn(i,n)):Ri}function Qe(t,n){return Le((e,i)=>{let a=0;e.subscribe(je(i,o=>t.call(n,o,a++)&&i.next(o)))})}function Sv(t){return Le((n,e)=>{let i=!1,a=null,o=null,r=!1,s=()=>{if(o?.unsubscribe(),o=null,i){i=!1;let h=a;a=null,e.next(h)}r&&e.complete()},c=()=>{o=null,r&&e.complete()};n.subscribe(je(e,h=>{i=!0,a=h,o||ut(t(h)).subscribe(o=je(e,s,c))},()=>{r=!0,(!i||!o||o.closed)&&e.complete()}))})}function Ol(t,n=vs){return Sv(()=>_s(t,n))}function rp(t,n){return Me(n)?Jo(t,n,1):Jo(t,1)}function Pi(t,n=vs){return Le((e,i)=>{let a=null,o=null,r=null,s=()=>{if(a){a.unsubscribe(),a=null;let h=o;o=null,i.next(h)}};function c(){let h=r+t,p=n.now();if(p<h){a=this.schedule(void 0,h-p),i.add(a);return}s()}e.subscribe(je(i,h=>{o=h,r=n.now(),a||(a=n.schedule(c,t),i.add(a))},()=>{s(),i.complete()},void 0,()=>{o=a=null}))})}function vn(t){return t<=0?()=>Ri:Le((n,e)=>{let i=0;n.subscribe(je(e,a=>{++i<=t&&(e.next(a),t<=i&&e.complete())}))})}function Rl(t,n=qn){return t=t??VE,Le((e,i)=>{let a,o=!0;e.subscribe(je(i,r=>{let s=n(r);(o||!t(a,s))&&(o=!1,a=s,i.next(r))}))})}function VE(t,n){return t===n}function sp(t){return Le((n,e)=>{try{n.subscribe(e)}finally{e.add(t)}})}function Pl(){return Le((t,n)=>{let e,i=!1;t.subscribe(je(n,a=>{let o=e;e=a,i&&n.next([o,a]),i=!0}))})}function wv(t={}){let{connector:n=()=>new L,resetOnError:e=!0,resetOnComplete:i=!0,resetOnRefCountZero:a=!0}=t;return o=>{let r,s,c,h=0,p=!1,g=!1,v=()=>{s?.unsubscribe(),s=void 0},b=()=>{v(),r=c=void 0,p=g=!1},y=()=>{let E=r;b(),E?.unsubscribe()};return Le((E,P)=>{h++,!g&&!p&&v();let ae=c=c??n();P.add(()=>{h--,h===0&&!g&&!p&&(s=cp(y,a))}),ae.subscribe(P),!r&&h>0&&(r=new Wn({next:z=>ae.next(z),error:z=>{g=!0,v(),s=cp(b,e,z),ae.error(z)},complete:()=>{p=!0,v(),s=cp(b,i),ae.complete()}}),ut(E).subscribe(r))})(o)}}function cp(t,n,...e){if(n===!0){t();return}if(n===!1)return;let i=new Wn({next:()=>{i.unsubscribe(),t()}});return ut(n(...e)).subscribe(i)}function Fl(t,n,e){let i,a=!1;return t&&typeof t==`object`?{bufferSize:i=1/0,windowTime:n=1/0,refCount:a=!1,scheduler:e}=t:i=t??1/0,wv({connector:()=>new Yn(i,n,e),resetOnError:!0,resetOnComplete:!1,resetOnRefCountZero:a})}function Qa(t){return Qe((n,e)=>t<=e)}function mt(...t){let n=fi(t);return Le((e,i)=>{(n?er(t,e,n):er(t,e)).subscribe(i)})}function _n(t,n){return Le((e,i)=>{let a=null,o=0,r=!1,s=()=>r&&!a&&i.complete();e.subscribe(je(i,c=>{a?.unsubscribe();let h=0,p=o++;ut(t(c,p)).subscribe(a=je(i,g=>i.next(n?n(c,g,p,h++):g),()=>{a=null,s()}))},()=>{r=!0,s()}))})}function Te(t){return Le((n,e)=>{ut(t).subscribe(je(e,()=>e.complete(),fs)),!e.closed&&n.subscribe(e)})}function lp(t,n=!1){return Le((e,i)=>{let a=0;e.subscribe(je(i,o=>{let r=t(o,a++);(r||n)&&i.next(o),!r&&i.complete()}))})}function dp(t,n,e){let i=Me(t)||n||e?{next:t,error:n,complete:e}:t;return i?Le((a,o)=>{var r;(r=i.subscribe)===null||r===void 0||r.call(i);let s=!0;a.subscribe(je(o,c=>{var h;(h=i.next)===null||h===void 0||h.call(i,c),o.next(c)},()=>{var c;s=!1,(c=i.complete)===null||c===void 0||c.call(i),o.complete()},c=>{var h;s=!1,(h=i.error)===null||h===void 0||h.call(i,c),o.error(c)},()=>{var c,h;s&&((c=i.unsubscribe)===null||c===void 0||c.call(i)),(h=i.finalize)===null||h===void 0||h.call(i)}))}):qn}var Ul=`https://angular.dev/best-practices/security#preventing-cross-site-scripting-xss`;var G=class extends Error{code;constructor(n,e){super(Bi(n,e)),this.code=n}};function BE(t){return`NG0${Math.abs(t)}`}function Bi(t,n){return`${BE(t)}${n?`: `+n:``}`}function Ye(t){for(let n in t)if(t[n]===Ye)return n;throw Error(``)}function Mv(t,n){for(let e in n)Object.hasOwn(n,e)&&!Object.hasOwn(t,e)&&(t[e]=n[e])}function Es(t){if(typeof t==`string`)return t;if(Array.isArray(t))return`[${t.map(Es).join(`, `)}]`;if(t==null)return``+t;let n=t.overriddenName||t.name;if(n)return`${n}`;let e=t.toString();if(e==null)return``+e;let i=e.indexOf(`
`);return i>=0?e.slice(0,i):e}function $l(t,n){return t?n?`${t} ${n}`:t:n||``}var jE=Ye({__forward_ref__:Ye});function Ht(t){return t.__forward_ref__=Ht,t}function Ft(t){return wp(t)?t():t}function wp(t){return typeof t==`function`&&Object.hasOwn(t,jE)&&t.__forward_ref__===Ht}function be(t){return{token:t.token,providedIn:t.providedIn||null,factory:t.factory,value:void 0}}function ie(t){return{providers:t.providers||[],imports:t.imports||[]}}function Gl(t){return zE(t,Wl)}function zE(t,n){return Object.hasOwn(t,n)&&t[n]||null}function HE(t){return(t?.[Wl]??null)||null}function mp(t){return t&&Object.hasOwn(t,Vl)?t[Vl]:null}var Wl=Ye({ɵprov:Ye});var Vl=Ye({ɵinj:Ye});var k=class{_desc;ngMetadataName=`InjectionToken`;ɵprov;constructor(n,e){this._desc=n,this.ɵprov=void 0,typeof e==`number`?this.__NG_ELEMENT_ID__=e:e!==void 0&&(this.ɵprov=be({token:this,providedIn:e.providedIn||`root`,factory:e.factory}))}get multi(){return this}toString(){return`InjectionToken ${this._desc}`}};function Ep(t){return t&&!!t.ɵproviders}var Ds=Ye({ɵcmp:Ye});var Is=Ye({ɵdir:Ye});var Dp=Ye({ɵpipe:Ye});var Ip=Ye({ɵmod:Ye});var xs=Ye({ɵfac:Ye});var to=Ye({__NG_ELEMENT_ID__:Ye});var Ev=Ye({__NG_ENV_ID__:Ye});function Tv(t){return ql(t,`@NgModule`),t[Ip]||null}function pa(t){return ql(t,`@Component`),t[Ds]||null}function ks(t){return ql(t,`@Directive`),t[Is]||null}function Nv(t){return ql(t,`@Pipe`),t[Dp]||null}function ql(t,n){if(t==null)throw new G(-919,!1)}function $e(t){return typeof t==`string`?t:t==null?``:String(t)}var Av=Ye({ngErrorCode:Ye});var UE=Ye({ngErrorMessage:Ye});var $E=Ye({ngTokenPath:Ye});function kp(t,n){return Ov(``,-200,n)}function Yl(t,n){throw new G(-201,!1)}function Ov(t,n,e){let i=new G(n,t);return i[Av]=n,i[UE]=t,e&&(i[$E]=e),i}function GE(t){return t[Av]}var pp;function Rv(){return pp}function on(t){let n=pp;return pp=t,n}function Mp(t,n,e){let i=Gl(t);if(i&&i.providedIn==`root`)return i.value===void 0?i.value=i.factory():i.value;if(e&8)return null;if(n!==void 0)return n;Yl(t,``)}var bi=globalThis;var Xa={};var qE=`__NG_DI_FLAG__`;var hp=class{injector;constructor(n){this.injector=n}retrieve(n,e){let i=Za(e)||0;try{return this.injector.get(n,i&8?null:Xa,i)}catch(a){if(qo(a))return a;throw a}}};function YE(t,n=0){let e=rl();if(e===void 0)throw new G(-203,!1);if(e===null)return Mp(t,void 0,n);{let i=KE(n),a=e.retrieve(t,i);if(qo(a)){if(i.optional)return null;throw a}return a}}function Se(t,n=0){return(Rv()||YE)(Ft(t),n)}function f(t,n){return Se(t,Za(n))}function Za(t){return typeof t>`u`||typeof t==`number`?t:0|(t.optional&&8)|(t.host&&1)|(t.self&&2)|(t.skipSelf&&4)}function KE(t){return{optional:!!(t&8),host:!!(t&1),self:!!(t&2),skipSelf:!!(t&4)}}function fp(t){let n=[];for(let e=0;e<t.length;e++){let i=Ft(t[e]);if(Array.isArray(i)){if(i.length===0)throw new G(900,!1);let a,o=0;for(let r=0;r<i.length;r++){let s=i[r],c=QE(s);typeof c==`number`?c===-1?a=s.token:o|=c:a=s}n.push(Se(a,o))}else n.push(Se(i))}return n}function QE(t){return t[qE]}function ma(t,n){return Object.hasOwn(t,xs)?t[xs]:null}function Pv(t,n,e){if(t.length!==n.length)return!1;for(let i=0;i<t.length;i++){let a=t[i],o=n[i];if(e&&(a=e(a),o=e(o)),o!==a)return!1}return!0}function Fv(t){return t.flat(Number.POSITIVE_INFINITY)}function Kl(t,n){t.forEach(e=>Array.isArray(e)?Kl(e,n):n(e))}function Tp(t,n,e){n>=t.length?t.push(e):t.splice(n,0,e)}function Ms(t,n){return n>=t.length-1?t.pop():t.splice(n,1)[0]}function Lv(t,n){let e=[];for(let i=0;i<t;i++)e.push(n);return e}function Vv(t,n,e,i){let a=t.length;if(a==n)t.push(e,i);else if(a===1)t.push(i,t[0]),t[0]=e;else{for(a--,t.push(t[a-1],t[a]);a>n;){let o=a-2;t[a]=t[o],a--}t[n]=e,t[n+1]=i}}function Ql(t,n,e){let i=ir(t,n);return i>=0?t[i|1]=e:(i=~i,Vv(t,i,n,e)),i}function Xl(t,n){let e=ir(t,n);if(e>=0)return t[e|1]}function ir(t,n){return XE(t,n,1)}function XE(t,n,e){let i=0,a=t.length>>e;for(;a!==i;){let o=i+(a-i>>1),r=t[o<<e];if(n===r)return o<<e;r>n?a=o:i=o+1}return~(a<<e)}var ha={};var Kt=[];var no=new k(``);var Zl=new k(``,-1);var Np=new k(``);var nr=class{get(n,e=Xa){if(e===Xa){let a=Ov(``,-201);throw a.name=`ɵNotFound`,a}return e}};function rn(t){return{ɵproviders:t}}function Bv(t){return rn([{provide:no,multi:!0,useValue:t}])}function jv(...t){return{ɵproviders:Ap(!0,t),ɵfromNgModule:!0}}function Ap(t,...n){let e=[],i=new Set,a,o=r=>{e.push(r)};return Kl(n,r=>{let s=r;Bl(s,o,[],i)&&(a||=[],a.push(s))}),a!==void 0&&zv(a,o),e}function zv(t,n){for(let e=0;e<t.length;e++){let{ngModule:i,providers:a}=t[e];Op(a,o=>{n(o,i)})}}function Bl(t,n,e,i){if(t=Ft(t),!t)return!1;let a=null,o=mp(t),r=!o&&pa(t);if(!o&&!r){let c=t.ngModule;if(o=mp(c),o)a=c;else return!1}else{if(r&&!r.standalone)return!1;a=t}let s=i.has(a);if(r){if(s)return!1;if(i.add(a),r.dependencies){let c=typeof r.dependencies==`function`?r.dependencies():r.dependencies;for(let h of c)Bl(h,n,e,i)}}else if(o){if(o.imports!=null&&!s){i.add(a);let h;Kl(o.imports,p=>{Bl(p,n,e,i)&&(h||=[],h.push(p))}),h!==void 0&&zv(h,n)}if(!s){let h=ma(a)||(()=>new a);n({provide:a,useFactory:h,deps:Kt},a),n({provide:Np,useValue:a,multi:!0},a),n({provide:no,useValue:()=>Se(a),multi:!0},a)}let c=o.providers;if(c!=null&&!s){let h=t;Op(c,p=>{n(p,h)})}}else return!1;return a!==t&&t.providers!==void 0}function Op(t,n){for(let e of t)Ep(e)&&(e=e.ɵproviders),Array.isArray(e)?Op(e,n):n(e)}var ZE=Ye({provide:String,useValue:Ye});function Hv(t){return t!==null&&typeof t==`object`&&ZE in t}function JE(t){return!!(t&&t.useExisting)}function eD(t){return!!(t&&t.useFactory)}function Ja(t){return typeof t==`function`}function Uv(t){return!!t.useClass}var Ts=new k(``);var Ll={};var Dv={};var up;function ar(){return up===void 0&&(up=new nr),up}var kt=class{};var eo=class extends kt{parent;source;scopes;records=new Map;_ngOnDestroyHooks=new Set;_onDestroyHooks=[];get destroyed(){return this._destroyed}_destroyed=!1;injectorDefTypes;constructor(n,e,i,a){super(),this.parent=e,this.source=i,this.scopes=a,bp(n,r=>this.processProvider(r)),this.records.set(Zl,tr(void 0,this)),a.has(`environment`)&&this.records.set(kt,tr(void 0,this));let o=this.records.get(Ts);o!=null&&typeof o.value==`string`&&this.scopes.add(o.value),this.injectorDefTypes=new Set(this.get(Np,Kt,{self:!0}))}retrieve(n,e){let i=Za(e)||0;try{return this.get(n,Xa,i)}catch(a){if(qo(a))return a;throw a}}destroy(){ys(this),this._destroyed=!0;let n=ge(null);try{for(let i of this._ngOnDestroyHooks)i.ngOnDestroy();let e=this._onDestroyHooks;this._onDestroyHooks=[];for(let i of e)i()}finally{this.records.clear(),this._ngOnDestroyHooks.clear(),this.injectorDefTypes.clear(),ge(n)}}onDestroy(n){return ys(this),this._onDestroyHooks.push(n),()=>this.removeOnDestroy(n)}runInContext(n){ys(this);let e=pi(this),i=on(void 0);try{return n()}finally{pi(e),on(i)}}get(n,e=Xa,i){if(ys(this),Object.hasOwn(n,Ev))return n[Ev](this);let a=Za(i),r=pi(this),s=on(void 0);try{if(!(a&4)){let h=this.records.get(n);if(h===void 0){let p=oD(n)&&Gl(n);p&&this.injectableDefInScope(p)?h=tr(gp(n),Ll):h=null,this.records.set(n,h)}if(h!=null)return this.hydrate(n,h,a)}let c=a&2?ar():this.parent;return e=a&8&&e===Xa?null:e,c.get(n,e)}catch(c){let h=GE(c);throw h===-200||h===-201?new G(h,null):c}finally{on(s),pi(r)}}resolveInjectorInitializers(){let n=ge(null),e=pi(this),i=on(void 0);try{let o=this.get(no,Kt,{self:!0});for(let r of o)r()}finally{pi(e),on(i),ge(n)}}toString(){return`R3Injector[...]`}processProvider(n){n=Ft(n);let e=Ja(n)?n:Ft(n&&n.provide),i=nD(n);if(!Ja(n)&&n.multi===!0){let a=this.records.get(e);a||(a=tr(void 0,Ll,!0),a.factory=()=>fp(a.multi),this.records.set(e,a)),e=n,a.multi.push(n)}this.records.set(e,i)}hydrate(n,e,i){let a=ge(null);try{if(e.value===Dv)throw kp(``);return e.value===Ll&&(e.value=Dv,e.value=e.factory(void 0,i)),typeof e.value==`object`&&e.value&&aD(e.value)&&this._ngOnDestroyHooks.add(e.value),e.value}finally{ge(a)}}injectableDefInScope(n){if(!n.providedIn)return!1;let e=Ft(n.providedIn);return typeof e==`string`?e===`any`||this.scopes.has(e):this.injectorDefTypes.has(e)}removeOnDestroy(n){let e=this._onDestroyHooks.indexOf(n);e!==-1&&this._onDestroyHooks.splice(e,1)}};function gp(t){let n=Gl(t),e=n!==null?n.factory:ma(t);if(e!==null)return e;if(t instanceof k)throw new G(-204,!1);if(t instanceof Function)return tD(t);throw new G(-204,!1)}function tD(t){if(t.length>0)throw new G(-204,!1);let e=HE(t);return e!==null?()=>e.factory(t):()=>new t}function nD(t){if(Hv(t))return tr(void 0,t.useValue);return tr(Rp(t),Ll)}function Rp(t,n,e){let i;if(Ja(t)){let a=Ft(t);return ma(a)||gp(a)}else if(Hv(t))i=()=>Ft(t.useValue);else if(eD(t))i=()=>t.useFactory(...fp(t.deps||[]));else if(JE(t))i=(a,o)=>Se(Ft(t.useExisting),o!==void 0&&o&8?8:void 0);else{let a=Ft(t&&(t.useClass||t.provide));if(iD(t))i=()=>new a(...fp(t.deps));else return ma(a)||gp(a)}return i}function ys(t){if(t.destroyed)throw new G(-205,!1)}function tr(t,n,e=!1){return{factory:t,value:n,multi:e?[]:void 0}}function iD(t){return!!t.deps}function aD(t){return t!==null&&typeof t==`object`&&typeof t.ngOnDestroy==`function`}function oD(t){return typeof t==`function`||typeof t==`object`&&t.ngMetadataName===`InjectionToken`}function bp(t,n){for(let e of t)Array.isArray(e)?bp(e,n):e&&Ep(e)?bp(e.ɵproviders,n):n(e)}function or(t,n){let e;t instanceof eo?(ys(t),e=t):e=new hp(t);let a=pi(e),o=on(void 0);try{return n()}finally{pi(a),on(o)}}function $v(){return Rv()!==void 0||rl()!=null}var Kn=0;var ve=1;var xe=2;var wt=3;var Nn=4;var Ut=5;var io=6;var rr=7;var _t=8;var ji=9;var Qn=10;var et=11;var sr=12;var Pp=13;var fa=14;var Qt=15;var ga=16;var ao=17;var vi=18;var Ns=19;var Fp=20;var Fi=21;var Jl=22;var Li=23;var yn=24;var oo=25;var Gv=26;var cr=27;var at=28;var Wv=1;var Lp=6;var ro=7;var As=8;var so=9;var yt=10;function zi(t){return Array.isArray(t)&&typeof t[Wv]==`object`}function An(t){return Array.isArray(t)&&t[Wv]===!0}function Vp(t){return(t.flags&4)!==0}function _i(t){return t.componentOffset>-1}function lr(t){return(t.flags&1)===1}function Xn(t){return!!t.template}function dr(t){return(t[xe]&512)!==0}function co(t){return(t[xe]&256)===256}var xn=(function(t){return t[t.NONE=0]=`NONE`,t[t.HTML=1]=`HTML`,t[t.STYLE=2]=`STYLE`,t[t.SCRIPT=3]=`SCRIPT`,t[t.URL=4]=`URL`,t[t.RESOURCE_URL=5]=`RESOURCE_URL`,t[t.ATTRIBUTE_NO_BINDING=6]=`ATTRIBUTE_NO_BINDING`,t})(xn||{});var Bp=`svg`;var qv=`math`;function $t(t){for(;Array.isArray(t);)t=t[Kn];return t}function jp(t,n){return $t(n[t])}function Cn(t,n){return $t(n[t.index])}function ed(t,n){return t.data[n]}function Os(t,n){return t[n]}function Rs(t,n,e,i){e>=t.data.length&&(t.data[e]=null,t.blueprint[e]=null),n[e]=i}function On(t,n){let e=n[t];return zi(e)?e:e[Kn]}function Yv(t){return(t[xe]&4)===4}function td(t){return(t[xe]&128)===128}function Kv(t){return An(t[wt])}function Sn(t,n){return n==null?null:t[n]}function zp(t){t[ao]=0}function Hp(t){t[xe]&1024||(t[xe]|=1024,td(t)&&lo(t))}function Qv(t,n){for(;t>0;)n=n[fa],t--;return n}function Ps(t){return!!(t[xe]&9216||t[yn]?.dirty)}function nd(t){t[Qn].changeDetectionScheduler?.notify(8),t[xe]&64&&(t[xe]|=1024),Ps(t)&&lo(t)}function lo(t){t[Qn].changeDetectionScheduler?.notify(0);let n=Vi(t);for(;n!==null&&!(n[xe]&8192||(n[xe]|=8192,!td(n)));)n=Vi(n)}function id(t,n){if(co(t))throw new G(911,!1);t[Fi]===null&&(t[Fi]=[]),t[Fi].push(n)}function Xv(t,n){if(t[Fi]===null)return;let e=t[Fi].indexOf(n);e!==-1&&t[Fi].splice(e,1)}function Vi(t){let n=t[wt];return An(n)?n[wt]:n}function Up(t){return t[rr]??=[]}function $p(t){return t.cleanup??=[]}function Zv(t,n,e,i){let a=Up(n);a.push(e),t.firstCreatePass&&$p(t).push(i,a.length-1)}var Ee={lFrame:c_(null),bindingsEnabled:!0,skipHydrationRootTNode:null};var vp=!1;function Jv(){return Ee.lFrame.elementDepthCount}function e_(){Ee.lFrame.elementDepthCount++}function Gp(){Ee.lFrame.elementDepthCount--}function ad(){return Ee.bindingsEnabled}function Wp(){return Ee.skipHydrationRootTNode!==null}function qp(t){return Ee.skipHydrationRootTNode===t}function Yp(){Ee.skipHydrationRootTNode=null}function le(){return Ee.lFrame.lView}function Xe(){return Ee.lFrame.tView}function N(t){return Ee.lFrame.contextLView=t,t[_t]}function A(t){return Ee.lFrame.contextLView=null,t}function Mt(){let t=Kp();for(;t!==null&&t.type===64;)t=t.parent;return t}function Kp(){return Ee.lFrame.currentTNode}function t_(){let t=Ee.lFrame,n=t.currentTNode;return t.isParent?n:n.parent}function uo(t,n){let e=Ee.lFrame;e.currentTNode=t,e.isParent=n}function Qp(){return Ee.lFrame.isParent}function Xp(){Ee.lFrame.isParent=!1}function Zp(){return Ee.lFrame.contextLView}function Jp(){return vp}function Cs(t){let n=vp;return vp=t,n}function od(){let t=Ee.lFrame,n=t.bindingRootIndex;return n===-1&&(n=t.bindingRootIndex=t.tView.bindingStartIndex),n}function mo(){return Ee.lFrame.bindingIndex}function n_(t){return Ee.lFrame.bindingIndex=t}function ba(){return Ee.lFrame.bindingIndex++}function Hi(t){let n=Ee.lFrame,e=n.bindingIndex;return n.bindingIndex=n.bindingIndex+t,e}function i_(){return Ee.lFrame.inI18n}function a_(t,n){let e=Ee.lFrame;e.bindingIndex=e.bindingRootIndex=t,rd(n)}function o_(){return Ee.lFrame.currentDirectiveIndex}function rd(t){Ee.lFrame.currentDirectiveIndex=t}function r_(t){let n=Ee.lFrame.currentDirectiveIndex;return n===-1?null:t[n]}function sd(){return Ee.lFrame.currentQueryIndex}function Fs(t){Ee.lFrame.currentQueryIndex=t}function rD(t){let n=t[ve];return n.type===2?n.declTNode:n.type===1?t[Ut]:null}function eh(t,n,e){if(e&4){let a=n,o=t;for(;a=a.parent,a===null&&!(e&1);)if(a=rD(o),a===null||(o=o[fa],a.type&10))break;if(a===null)return!1;n=a,t=o}let i=Ee.lFrame=s_();return i.currentTNode=n,i.lView=t,!0}function cd(t){let n=s_(),e=t[ve];Ee.lFrame=n,n.currentTNode=e.firstChild,n.lView=t,n.tView=e,n.contextLView=t,n.bindingIndex=e.bindingStartIndex,n.inI18n=!1}function s_(){let t=Ee.lFrame,n=t===null?null:t.child;return n===null?c_(t):n}function c_(t){let n={currentTNode:null,isParent:!0,lView:null,tView:null,selectedIndex:-1,contextLView:null,elementDepthCount:0,currentNamespace:null,currentDirectiveIndex:-1,bindingRootIndex:-1,bindingIndex:-1,currentQueryIndex:0,parent:t,child:null,inI18n:!1};return t!==null&&(t.child=n),n}function l_(){let t=Ee.lFrame;return Ee.lFrame=t.parent,t.currentTNode=null,t.lView=null,t}var th=l_;function ld(){let t=l_();t.isParent=!0,t.tView=null,t.selectedIndex=-1,t.contextLView=null,t.elementDepthCount=0,t.currentDirectiveIndex=-1,t.currentNamespace=null,t.bindingRootIndex=-1,t.bindingIndex=-1,t.currentQueryIndex=0}function d_(t){return(Ee.lFrame.contextLView=Qv(t,Ee.lFrame.contextLView))[_t]}function Xt(){return Ee.lFrame.selectedIndex}function va(t){Ee.lFrame.selectedIndex=t}function ur(){let t=Ee.lFrame;return ed(t.tView,t.selectedIndex)}function Gt(){Ee.lFrame.currentNamespace=Bp}function _a(){sD()}function sD(){Ee.lFrame.currentNamespace=null}function nh(){return Ee.lFrame.currentNamespace}var u_=!0;function dd(){return u_}function Ls(t){u_=t}function _p(t,n=null,e=null,i){let a=ih(t,n,e,i);return a.resolveInjectorInitializers(),a}function ih(t,n=null,e=null,i,a=new Set){return new eo([e||Kt,jv(t)],n||ar(),null,a)}var se=class t{static THROW_IF_NOT_FOUND=Xa;static NULL=new nr;static create(n,e){if(Array.isArray(n))return _p({name:``},e,n,``);{let i=n.name??``;return _p({name:i},n.parent,n.providers,i)}}static ɵprov=be({token:t,providedIn:`any`,factory:()=>Se(Zl)});static __NG_ELEMENT_ID__=-1};var de=new k(``);var Ne=class{static __NG_ELEMENT_ID__=cD;static __NG_ENV_ID__=n=>n};var jl=class extends Ne{_lView;constructor(n){super(),this._lView=n}get destroyed(){return co(this._lView)}onDestroy(n){let e=this._lView;return id(e,n),()=>Xv(e,n)}};function cD(){return new jl(le())}var m_=!1;var p_=new k(``);var po=(()=>{class t{taskId=0;pendingTasks=new Set;destroyed=!1;pendingTask=new hi(!1);debugTaskTracker=f(p_,{optional:!0});get hasPendingTasks(){return this.destroyed?!1:this.pendingTask.value}get hasPendingTasksObservable(){return this.destroyed?new Ce(e=>{e.next(!1),e.complete()}):this.pendingTask}add(){!this.hasPendingTasks&&!this.destroyed&&this.pendingTask.next(!0);let e=this.taskId++;return this.pendingTasks.add(e),this.debugTaskTracker?.add(e),e}has(e){return this.pendingTasks.has(e)}remove(e){this.pendingTasks.delete(e),this.debugTaskTracker?.remove(e),this.pendingTasks.size===0&&this.hasPendingTasks&&this.pendingTask.next(!1)}ngOnDestroy(){this.pendingTasks.clear(),this.hasPendingTasks&&this.pendingTask.next(!1),this.destroyed=!0,this.pendingTask.unsubscribe()}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var yp=class extends L{__isAsync;destroyRef=void 0;pendingTasks=void 0;constructor(n=!1){super(),this.__isAsync=n,$v()&&(this.destroyRef=f(Ne,{optional:!0})??void 0,this.pendingTasks=f(po,{optional:!0})??void 0)}emit(n){let e=ge(null);try{super.next(n)}finally{ge(e)}}subscribe(n,e,i){let a=n,o=e||(()=>null),r=i;if(n&&typeof n==`object`){let c=n;a=c.next?.bind(c),o=c.error?.bind(c),r=c.complete?.bind(c)}this.__isAsync&&(o=this.wrapInTimeout(o),a&&(a=this.wrapInTimeout(a)),r&&(r=this.wrapInTimeout(r)));let s=super.subscribe({next:a,error:o,complete:r});return n instanceof De&&n.add(s),s}wrapInTimeout(n){return e=>{let i=this.pendingTasks?.add();setTimeout(()=>{try{n(e)}finally{i!==void 0&&this.pendingTasks?.remove(i)}})}}};var ee=yp;function zl(...t){}function ah(t){let n,e;function i(){t=zl;try{e!==void 0&&typeof cancelAnimationFrame==`function`&&cancelAnimationFrame(e),n!==void 0&&clearTimeout(n)}catch{}}return n=setTimeout(()=>{t(),i()}),typeof requestAnimationFrame==`function`&&(e=requestAnimationFrame(()=>{t(),i()})),()=>i()}function h_(t){return queueMicrotask(()=>t()),()=>{t=zl}}var oh=`isAngularZone`;var Ss=oh+`_ID`;var lD=0;var X=class t{hasPendingMacrotasks=!1;hasPendingMicrotasks=!1;isStable=!0;onUnstable=new ee(!1);onMicrotaskEmpty=new ee(!1);onStable=new ee(!1);onError=new ee(!1);constructor(n){let{enableLongStackTrace:e=!1,shouldCoalesceEventChangeDetection:i=!1,shouldCoalesceRunChangeDetection:a=!1,scheduleInRootZone:o=m_}=n;if(typeof Zone>`u`)throw new G(908,!1);Zone.assertZonePatched();let r=this;r._nesting=0,r._outer=r._inner=Zone.current,Zone.TaskTrackingZoneSpec&&(r._inner=r._inner.fork(new Zone.TaskTrackingZoneSpec)),e&&Zone.longStackTraceZoneSpec&&(r._inner=r._inner.fork(Zone.longStackTraceZoneSpec)),r.shouldCoalesceEventChangeDetection=!a&&i,r.shouldCoalesceRunChangeDetection=a,r.callbackScheduled=!1,r.scheduleInRootZone=o,mD(r)}static isInAngularZone(){return typeof Zone<`u`&&Zone.current.get(oh)===!0}static assertInAngularZone(){if(!t.isInAngularZone())throw new G(909,!1)}static assertNotInAngularZone(){if(t.isInAngularZone())throw new G(909,!1)}run(n,e,i){return this._inner.run(n,e,i)}runTask(n,e,i,a){let o=this._inner,r=o.scheduleEventTask(`NgZoneEvent: `+a,n,dD,zl,zl);try{return o.runTask(r,e,i)}finally{o.cancelTask(r)}}runGuarded(n,e,i){return this._inner.runGuarded(n,e,i)}runOutsideAngular(n){return this._outer.run(n)}};var dD={};function rh(t){if(t._nesting==0&&!t.hasPendingMicrotasks&&!t.isStable)try{t._nesting++,t.onMicrotaskEmpty.emit(null)}finally{if(t._nesting--,!t.hasPendingMicrotasks)try{t.runOutsideAngular(()=>t.onStable.emit(null))}finally{t.isStable=!0}}}function uD(t){if(t.isCheckStableRunning||t.callbackScheduled)return;t.callbackScheduled=!0;function n(){ah(()=>{t.callbackScheduled=!1,xp(t),t.isCheckStableRunning=!0,rh(t),t.isCheckStableRunning=!1})}t.scheduleInRootZone?Zone.root.run(()=>{n()}):t._outer.run(()=>{n()}),xp(t)}function mD(t){let n=()=>{uD(t)},e=lD++;t._inner=t._inner.fork({name:`angular`,properties:{[oh]:!0,[Ss]:e,[Ss+e]:!0},onInvokeTask:(i,a,o,r,s,c)=>{if(pD(c))return i.invokeTask(o,r,s,c);try{return Iv(t),i.invokeTask(o,r,s,c)}finally{(t.shouldCoalesceEventChangeDetection&&r.type===`eventTask`||t.shouldCoalesceRunChangeDetection)&&n(),kv(t)}},onInvoke:(i,a,o,r,s,c,h)=>{try{return Iv(t),i.invoke(o,r,s,c,h)}finally{t.shouldCoalesceRunChangeDetection&&!t.callbackScheduled&&!hD(c)&&n(),kv(t)}},onHasTask:(i,a,o,r)=>{i.hasTask(o,r),a===o&&(r.change==`microTask`?(t._hasPendingMicrotasks=r.microTask,xp(t),rh(t)):r.change==`macroTask`&&(t.hasPendingMacrotasks=r.macroTask))},onHandleError:(i,a,o,r)=>(i.handleError(o,r),t.runOutsideAngular(()=>t.onError.emit(r)),!1)})}function xp(t){t._hasPendingMicrotasks||(t.shouldCoalesceEventChangeDetection||t.shouldCoalesceRunChangeDetection)&&t.callbackScheduled===!0?t.hasPendingMicrotasks=!0:t.hasPendingMicrotasks=!1}function Iv(t){t._nesting++,t.isStable&&(t.isStable=!1,t.onUnstable.emit(null))}function kv(t){t._nesting--,rh(t)}var ws=class{hasPendingMicrotasks=!1;hasPendingMacrotasks=!1;isStable=!0;onUnstable=new ee;onMicrotaskEmpty=new ee;onStable=new ee;onError=new ee;run(n,e,i){return n.apply(e,i)}runGuarded(n,e,i){return n.apply(e,i)}runOutsideAngular(n){return n()}runTask(n,e,i,a){return n.apply(e,i)}};function pD(t){return f_(t,`__ignore_ng_zone__`)}function hD(t){return f_(t,`__scheduler_tick__`)}function f_(t,n){return!Array.isArray(t)||t.length!==1?!1:t[0]?.data?.[n]===!0}var Tn=class{_console=console;handleError(n){this._console.error(`ERROR`,n)}};function g_(t){return b_(t)?t:new Cp(t)}function b_(t){return t instanceof Error||typeof t==`object`&&t!==null&&typeof t.name==`string`&&typeof t.message==`string`}var Cp=class extends Error{constructor(n){super(String(n),{cause:n}),this.name=`ErrorBoundaryWrappedError`}};var Ui=new k(``,{factory:()=>{let t=f(X),n=f(kt),e;return i=>{t.runOutsideAngular(()=>{n.destroyed&&!e?setTimeout(()=>{throw i}):(e??=n.get(Tn),e.handleError(i))})}}});var v_={provide:no,useValue:()=>{f(Tn,{optional:!0})},multi:!0};var fD=new k(``,{factory:()=>{let t=f(de).defaultView;if(!t)return;let n=f(Ui),e=o=>{n(o.reason),o.preventDefault()},i=o=>{o.error?n(o.error):n(new Error(o.message,{cause:o})),o.preventDefault()},a=()=>{t.addEventListener(`unhandledrejection`,e),t.addEventListener(`error`,i)};typeof Zone<`u`?Zone.root.run(a):a(),f(Ne).onDestroy(()=>{t.removeEventListener(`error`,i),t.removeEventListener(`unhandledrejection`,e)})}});function sh(){return rn([Bv(()=>{f(fD)})])}function T(t,n){let[e,i,a]=Wm(t,n?.equal),o=e;o[Ct];return o.set=i,o.update=a,o.asReadonly=ud.bind(o),o}function ud(){let t=this[Ct];if(t.readonlyFn===void 0){let n=()=>this();n[Ct]=t,t.readonlyFn=n}return t.readonlyFn}var $i=new k(``,{factory:()=>gD});var gD=`ng`;var md=new k(``);var Gi=new k(``,{providedIn:`platform`,factory:()=>`unknown`});var Vs=new k(``);var ya=new k(``,{factory:()=>f(de).body?.querySelector(`[ngCspNonce]`)?.getAttribute(`ngCspNonce`)||null});var mr=(()=>{class t{view;node;constructor(e,i){this.view=e,this.node=i}static __NG_ELEMENT_ID__=bD}return t})();function bD(){return new mr(le(),Mt())}var gi=class{};var Bs=new k(``,{factory:()=>!0});var ch=new k(``);var pd=(()=>{class t{static ɵprov=be({token:t,providedIn:`root`,factory:()=>new Sp})}return t})();var Sp=class{dirtyEffectCount=0;queues=new Map;add(n){this.enqueue(n),this.schedule(n)}schedule(n){n.dirty&&this.dirtyEffectCount++}remove(n){let e=n.zone,i=this.queues.get(e);i.has(n)&&(i.delete(n),n.dirty&&this.dirtyEffectCount--)}enqueue(n){let e=n.zone;this.queues.has(e)||this.queues.set(e,new Set);let i=this.queues.get(e);i.has(n)||i.add(n)}flush(){for(;this.dirtyEffectCount>0;){let n=!1;for(let[e,i]of this.queues)e===null?n||=this.flushQueue(i):n||=e.run(()=>this.flushQueue(i));n||(this.dirtyEffectCount=0)}}flushQueue(n){let e=!1;for(let i of n)i.dirty&&(this.dirtyEffectCount--,e=!0,i.run());return e}};var Hl=class{[Ct];constructor(n){this[Ct]=n}destroy(){this[Ct].destroy()}};function Lt(t,n){let e=n?.injector??f(se),i=n?.manualCleanup!==!0?e.get(Ne):null,a,o=e.get(mr,null,{optional:!0}),r=e.get(gi);return o!==null?(a=y_(o.view,r,t),i instanceof jl&&i._lView===o.view&&(i=null)):a=yD(t,e.get(pd),r),a.injector=e,i!==null&&(a.onDestroyFns=[i.onDestroy(()=>a.destroy())]),new Hl(a)}var __=s(r({},Ym),{cleanupFns:void 0,zone:null,onDestroyFns:null,run(){let t=Cs(!1);try{Km(this)}finally{Cs(t)}},cleanup(){if(!this.cleanupFns?.length)return;let t=ge(null);try{for(;this.cleanupFns.length;)this.cleanupFns.pop()()}finally{this.cleanupFns=[],ge(t)}}});var vD=s(r({},__),{consumerMarkedDirty(){this.scheduler.schedule(this),this.notifier.notify(12)},destroy(){if(da(this),this.onDestroyFns!==null)for(let t of this.onDestroyFns)t();this.cleanup(),this.scheduler.remove(this)}});var _D=s(r({},__),{consumerMarkedDirty(){this.view[xe]|=8192,lo(this.view),this.notifier.notify(13)},destroy(){if(da(this),this.onDestroyFns!==null)for(let t of this.onDestroyFns)t();this.cleanup(),this.view[Li]?.delete(this)}});function y_(t,n,e){let i=Object.create(_D);return i.view=t,i.zone=typeof Zone<`u`?Zone.current:null,i.notifier=n,i.userFn=e,i.fn=x_(i,e),t[Li]??=new Set,t[Li].add(i),i.consumerMarkedDirty(i),i}function yD(t,n,e){let i=Object.create(vD);return i.userFn=t,i.fn=x_(i,t),i.scheduler=n,i.notifier=e,i.zone=typeof Zone<`u`?Zone.current:null,i.scheduler.add(i),i.notifier.notify(12),i}function x_(t,n){return()=>{n(e=>(t.cleanupFns??=[]).push(e))}}function sn(t){return typeof t==`function`&&t[Ct]!==void 0}var xa=(()=>{class t{internalPendingTasks=f(po);scheduler=f(gi);errorHandler=f(Ui);add(){let e=this.internalPendingTasks.add();return()=>{this.internalPendingTasks.has(e)&&(this.scheduler.notify(11),this.internalPendingTasks.remove(e))}}run(e){let i=this.add();try{e().catch(this.errorHandler).finally(i)}catch(a){this.errorHandler(a),i()}}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var Sr=Symbol(`InputSignalNode#UNSET`);var af=s(r({},Wo),{transformFn:void 0,applyValueToInputSignal(t,n){ua(t,n)}});function Ks(t){return{toString:t}.toString()}var ze=(function(t){return t[t.TemplateCreateStart=0]=`TemplateCreateStart`,t[t.TemplateCreateEnd=1]=`TemplateCreateEnd`,t[t.TemplateUpdateStart=2]=`TemplateUpdateStart`,t[t.TemplateUpdateEnd=3]=`TemplateUpdateEnd`,t[t.LifecycleHookStart=4]=`LifecycleHookStart`,t[t.LifecycleHookEnd=5]=`LifecycleHookEnd`,t[t.OutputStart=6]=`OutputStart`,t[t.OutputEnd=7]=`OutputEnd`,t[t.BootstrapApplicationStart=8]=`BootstrapApplicationStart`,t[t.BootstrapApplicationEnd=9]=`BootstrapApplicationEnd`,t[t.BootstrapComponentStart=10]=`BootstrapComponentStart`,t[t.BootstrapComponentEnd=11]=`BootstrapComponentEnd`,t[t.ChangeDetectionStart=12]=`ChangeDetectionStart`,t[t.ChangeDetectionEnd=13]=`ChangeDetectionEnd`,t[t.ChangeDetectionSyncStart=14]=`ChangeDetectionSyncStart`,t[t.ChangeDetectionSyncEnd=15]=`ChangeDetectionSyncEnd`,t[t.AfterRenderHooksStart=16]=`AfterRenderHooksStart`,t[t.AfterRenderHooksEnd=17]=`AfterRenderHooksEnd`,t[t.ComponentStart=18]=`ComponentStart`,t[t.ComponentEnd=19]=`ComponentEnd`,t[t.DeferBlockStateStart=20]=`DeferBlockStateStart`,t[t.DeferBlockStateEnd=21]=`DeferBlockStateEnd`,t[t.DynamicComponentStart=22]=`DynamicComponentStart`,t[t.DynamicComponentEnd=23]=`DynamicComponentEnd`,t[t.HostBindingsUpdateStart=24]=`HostBindingsUpdateStart`,t[t.HostBindingsUpdateEnd=25]=`HostBindingsUpdateEnd`,t})(ze||{});var xd=class{previousValue;currentValue;firstChange;constructor(n,e,i){this.previousValue=n,this.currentValue=e,this.firstChange=i}isFirstChange(){return this.firstChange}};function ly(t,n,e,i){n!==null?n.applyValueToInputSignal(n,i):t[e]=i}var dy=null;var vt=(()=>{dy=C_;let t=()=>C_;return t.ngInherit=!0,t})();function ND(){return dy}function C_(t){return t.type.prototype.ngOnChanges&&(t.setInput=OD),AD}function AD(){let t=uy(this),n=t?.current;if(n){let e=t.previous;if(e===ha)t.previous=n;else for(let i in n)e[i]=n[i];t.current=null,this.ngOnChanges(n)}}function OD(t,n,e,i,a){let o=this.declaredInputs[i],r=uy(t)||RD(t,{previous:ha,current:null}),s=r.current||(r.current={}),c=r.previous,h=c[o];s[o]=new xd(h&&h.currentValue,e,c===ha),ly(t,n,a,e)}var yh=`__ngSimpleChanges__`;function uy(t){return Object.hasOwn(t,yh)&&t[yh]||null}function RD(t,n){return t[yh]=n}var S_=[];var Ke=function(t,n=null,e){for(let i=0;i<S_.length;i++){let a=S_[i];a(t,n,e)}};function PD(t,n,e){let{ngOnChanges:i,ngOnInit:a,ngDoCheck:o}=n.type.prototype;if(i){let r=ND()(n);(e.preOrderHooks??=[]).push(t,r),(e.preOrderCheckHooks??=[]).push(t,r)}a&&(e.preOrderHooks??=[]).push(0-t,a),o&&((e.preOrderHooks??=[]).push(t,o),(e.preOrderCheckHooks??=[]).push(t,o))}function my(t,n){for(let e=n.directiveStart,i=n.directiveEnd;e<i;e++){let{ngAfterContentInit:r,ngAfterContentChecked:s,ngAfterViewInit:c,ngAfterViewChecked:h,ngOnDestroy:p}=t.data[e].type.prototype;r&&(t.contentHooks??=[]).push(-e,r),s&&((t.contentHooks??=[]).push(e,s),(t.contentCheckHooks??=[]).push(e,s)),c&&(t.viewHooks??=[]).push(-e,c),h&&((t.viewHooks??=[]).push(e,h),(t.viewCheckHooks??=[]).push(e,h)),p!=null&&(t.destroyHooks??=[]).push(e,p)}}function bd(t,n,e){py(t,n,3,e)}function vd(t,n,e,i){(t[xe]&3)===e&&py(t,n,e,i)}function lh(t,n){let e=t[xe];(e&3)===n&&(e&=16383,e+=1,t[xe]=e)}function py(t,n,e,i){let a=i!==void 0?t[ao]&65535:0,o=i??-1,r=n.length-1,s=0;for(let c=a;c<r;c++)if(typeof n[c+1]==`number`){if(s=n[c],i!=null&&s>=i)break}else n[c]<0&&(t[ao]+=65536),(s<o||o==-1)&&(FD(t,e,n,c),t[ao]=(t[ao]&4294901760)+c+2),c++}function w_(t,n){Ke(ze.LifecycleHookStart,t,n);let e=ge(null);try{n.call(t)}finally{ge(e),Ke(ze.LifecycleHookEnd,t,n)}}function FD(t,n,e,i){let a=e[i]<0,o=e[i+1],s=t[a?-e[i]:e[i]];a?t[xe]>>14<t[ao]>>16&&(t[xe]&3)===n&&(t[xe]+=16384,w_(s,o)):w_(s,o)}var fr=-1;var go=class{factory;name;injectImpl;resolving=!1;canSeeViewProviders;multi;componentProviders;index;providerFactory;constructor(n,e,i,a){this.factory=n,this.name=a,this.canSeeViewProviders=e,this.injectImpl=i}};function LD(t){return(t.flags&8)!==0}function VD(t){return(t.flags&16)!==0}function BD(t,n,e){let i=0;for(;i<e.length;){let a=e[i];if(typeof a==`number`){if(a!==0)break;i++;let o=e[i++],r=e[i++],s=e[i++];t.setAttribute(n,r,s,o)}else{let o=a,r=e[++i];jD(o)?t.setProperty(n,o,r):t.setAttribute(n,o,r),i++}}return i}function hy(t){return t===3||t===4||t===6}function jD(t){return t.charCodeAt(0)===64}function gr(t,n){if(!(n===null||n.length===0))if(t===null||t.length===0)t=n.slice();else{let e=-1;for(let i=0;i<n.length;i++){let a=n[i];typeof a==`number`?e=a:e===0||(e===-1||e===2?E_(t,e,a,null,n[++i]):E_(t,e,a,null,null))}}return t}function E_(t,n,e,i,a){let o=0,r=t.length;if(n===-1)r=-1;else for(;o<t.length;){let s=t[o++];if(typeof s==`number`){if(s===n){r=-1;break}else if(s>n){r=o-1;break}}}for(;o<t.length;){let s=t[o];if(typeof s==`number`)break;if(s===e){a!==null&&(t[o+1]=a);return}o++,a!==null&&o++}r!==-1&&(t.splice(r,0,n),o=r+1),t.splice(o++,0,e),a!==null&&t.splice(o++,0,a)}function fy(t){return t!==fr}function Cd(t){return t&32767}function zD(t){return t>>16}function Sd(t,n){let e=zD(t),i=n;for(;e>0;)i=i[fa],e--;return i}var xh=!0;function wd(t){let n=xh;return xh=t,n}var gy=255;var by=5;var UD=0;var yi={};function $D(t,n,e){let i;typeof e==`string`?i=e.charCodeAt(0)||0:Object.hasOwn(e,to)&&(i=e[to]),i??=e[to]=UD++;let a=i&gy,o=1<<a;n.data[t+(a>>by)]|=o}function Ed(t,n){let e=vy(t,n);if(e!==-1)return e;let i=n[ve];i.firstCreatePass&&(t.injectorIndex=n.length,dh(i.data,t),dh(n,null),dh(i.blueprint,null));let a=of(t,n),o=t.injectorIndex;if(fy(a)){let r=Cd(a),s=Sd(a,n),c=s[ve].data;for(let h=0;h<8;h++)n[o+h]=s[r+h]|c[r+h]}return n[o+8]=a,o}function dh(t,n){t.push(0,0,0,0,0,0,0,0,n)}function vy(t,n){return t.injectorIndex===-1||t.parent&&t.parent.injectorIndex===t.injectorIndex||n[t.injectorIndex+8]===null?-1:t.injectorIndex}function of(t,n){if(t.parent&&t.parent.injectorIndex!==-1)return t.parent.injectorIndex;let e=0,i=null,a=n;for(;a!==null;){if(i=Sy(a),i===null)return fr;if(e++,a=a[fa],i.injectorIndex!==-1)return i.injectorIndex|e<<16}return fr}function Ch(t,n,e){$D(t,n,e)}function GD(t,n){if(n===`class`)return t.classes;if(n===`style`)return t.styles;let e=t.attrs;if(e){let i=e.length,a=0;for(;a<i;){let o=e[a];if(hy(o))break;if(o===0)a=a+2;else if(typeof o==`number`)for(a++;a<i&&typeof e[a]==`string`;)a++;else{if(o===n)return e[a+1];a=a+2}}}return null}function _y(t,n,e){if(e&8||t!==void 0)return t;Yl(n,`NodeInjector`)}function yy(t,n,e,i){if(e&8&&i===void 0&&(i=null),(e&3)===0){let a=t[ji],o=on(void 0);try{return a?a.get(n,i,e&8):Mp(n,i,e&8)}finally{on(o)}}return _y(i,n,e)}function xy(t,n,e,i=0,a){if(t!==null){if(n[xe]&2048&&!(i&2)){let r=KD(t,n,e,i,yi);if(r!==yi)return r}let o=Cy(t,n,e,i,yi);if(o!==yi)return o}return yy(n,e,i,a)}function Cy(t,n,e,i,a){let o=qD(e);if(typeof o==`function`){if(!eh(n,t,i))return i&1?_y(a,e,i):yy(n,e,i,a);try{let r;if(r=o(i),r==null&&!(i&8))Yl(e);else return r}finally{th()}}else if(typeof o==`number`){let r=null,s=vy(t,n),c=fr,h=i&1?n[Qt][Ut]:null;for((s===-1||i&4)&&(c=s===-1?of(t,n):n[s+8],c===fr||!I_(i,!1)?s=-1:(r=n[ve],s=Cd(c),n=Sd(c,n)));s!==-1;){let p=n[ve];if(D_(o,s,p.data)){let g=WD(s,n,e,r,i,h);if(g!==yi)return g}c=n[s+8],c!==fr&&I_(i,n[ve].data[s+8]===h)&&D_(o,s,n)?(r=p,s=Cd(c),n=Sd(c,n)):s=-1}}return a}function WD(t,n,e,i,a,o){let r=n[ve],s=r.data[t+8],p=_d(s,r,e,i==null?_i(s)&&xh:i!=r&&(s.type&3)!==0,a&1&&o===s);return p!==null?Us(n,r,p,s,a):yi}function _d(t,n,e,i,a){let o=t.providerIndexes,r=n.data,s=o&1048575,c=t.directiveStart,h=t.directiveEnd,p=o>>20,g=i?s:s+p,v=a?s+p:h;for(let b=g;b<v;b++){let y=r[b];if(b<c&&e===y||b>=c&&y.type===e)return b}if(a){let b=r[c];if(b&&Xn(b)&&b.type===e)return c}return null}function Us(t,n,e,i,a){let o=t[e],r=n.data;if(o instanceof go){let s=o;if(s.resolving)throw kp(``);let c=wd(s.canSeeViewProviders);s.resolving=!0;r[e].type||r[e];let g=s.injectImpl?on(s.injectImpl):null;eh(t,i,0);try{o=t[e]=s.factory(void 0,a,r,t,i),n.firstCreatePass&&e>=i.directiveStart&&PD(e,r[e],n)}finally{g!==null&&on(g),wd(c),s.resolving=!1,th()}}return o}function qD(t){if(typeof t==`string`)return t.charCodeAt(0)||0;let n=Object.hasOwn(t,to)?t[to]:void 0;return typeof n==`number`?n>=0?n&gy:YD:n}function D_(t,n,e){let i=1<<t;return!!(e[n+(t>>by)]&i)}function I_(t,n){return!(t&2)&&!(t&1&&n)}var Wi=class{_tNode;_lView;constructor(n,e){this._tNode=n,this._lView=e}get(n,e,i){return xy(this._tNode,this._lView,n,Za(i),e)}};function YD(){return new Wi(Mt(),le())}function Nt(t){return Ks(()=>{let n=t.prototype.constructor,e=n[xs]||Sh(n),i=Object.prototype,a=Object.getPrototypeOf(t.prototype).constructor;for(;a&&a!==i;){let o=a[xs]||Sh(a);if(o&&o!==e)return o;a=Object.getPrototypeOf(a)}return o=>new o})}function Sh(t){return wp(t)?()=>{let n=Sh(Ft(t));return n&&n()}:ma(t)}function KD(t,n,e,i,a){let o=t,r=n;for(;o!==null&&r!==null&&r[xe]&2048&&!dr(r);){let s=Cy(o,r,e,i|2,yi);if(s!==yi)return s;i&=-5;let c=o.parent;if(!c){let h=r[Fp];if(h){let p=h.get(e,yi,i);if(p!==yi)return p}c=Sy(r),r=r[fa]}o=c}return a}function Sy(t){let n=t[ve],e=n.type;return e===2?n.declTNode:e===1?t[Ut]:null}function rf(t){return GD(Mt(),t)}function ce(t){return{token:t.token,providedIn:t.autoProvided===!1?null:`root`,factory:t.factory,value:void 0}}function QD(){return wr(Mt(),le())}function wr(t,n){return new W(Cn(t,n))}var W=(()=>{class t{nativeElement;constructor(e){this.nativeElement=e}static __NG_ELEMENT_ID__=QD}return t})();function wy(t){return t instanceof W?t.nativeElement:t}function XD(){return this._results[Symbol.iterator]()}var wn=class{_emitDistinctChangesOnly;dirty=!0;_onDirty=void 0;_results=[];_changesDetected=!1;_changes=void 0;length=0;first=void 0;last=void 0;get changes(){return this._changes??=new L}constructor(n=!1){this._emitDistinctChangesOnly=n}get(n){return this._results[n]}map(n){return this._results.map(n)}filter(n){return this._results.filter(n)}find(n){return this._results.find(n)}reduce(n,e){return this._results.reduce(n,e)}forEach(n){this._results.forEach(n)}some(n){return this._results.some(n)}toArray(){return this._results.slice()}toString(){return this._results.toString()}reset(n,e){this.dirty=!1;let i=Fv(n);(this._changesDetected=!Pv(this._results,i,e))&&(this._results=i,this.length=i.length,this.last=i[this.length-1],this.first=i[0])}notifyOnChanges(){this._changes!==void 0&&(this._changesDetected||!this._emitDistinctChangesOnly)&&this._changes.next(this)}onDirty(n){this._onDirty=n}setDirty(){this.dirty=!0,this._onDirty?.()}destroy(){this._changes!==void 0&&(this._changes.complete(),this._changes.unsubscribe())}[Symbol.iterator]=XD};function Ey(t){return(t.flags&128)===128}var sf=(function(t){return t[t.OnPush=0]=`OnPush`,t[t.Eager=1]=`Eager`,t[t.Default=1]=`Default`,t})(sf||{});var Dy=new Map;var ZD=0;function JD(){return ZD++}function eI(t){Dy.set(t[Ns],t)}function wh(t){Dy.delete(t[Ns])}var k_=`__ngContext__`;function br(t,n){zi(n)?(t[k_]=n[Ns],eI(n)):t[k_]=n}function Iy(t){return My(t[sr])}function ky(t){return My(t[Nn])}function My(t){for(;t!==null&&!An(t);)t=t[Nn];return t}var Eh;function cf(t){Eh=t}function Ty(){if(Eh!==void 0)return Eh;if(typeof document<`u`)return document;throw new G(210,!1)}var Ny=`r`;var Ay=`di`;var Oy=!1;var Ry=new k(``,{factory:()=>Oy});var M_=new WeakMap;function tI(t,n){if(t==null||typeof t!=`object`)return;let e=M_.get(t);e||(e=new WeakSet,M_.set(t,e)),e.add(n)}function Ld(t){return(t.flags&32)===32}var aI=()=>null;function Py(t,n,e=!1){return aI(t,n,e)}function Fy(t,n){let e=t.contentQueries;if(e!==null){let i=ge(null);try{for(let a=0;a<e.length;a+=2){let o=e[a],r=e[a+1];if(r!==-1){let s=t.data[r];Fs(o),s.contentQueries(2,n[r],r)}}}finally{ge(i)}}}function Dh(t,n,e){Fs(0);let i=ge(null);try{n(t,e)}finally{ge(i)}}function lf(t,n,e){if(Vp(n)){let i=ge(null);try{let a=n.directiveStart,o=n.directiveEnd;for(let r=a;r<o;r++){let s=t.data[r];if(s.contentQueries){let c=e[r];s.contentQueries(1,c,r)}}}finally{ge(i)}}}var ei=(function(t){return t[t.Emulated=0]=`Emulated`,t[t.None=2]=`None`,t[t.ShadowDom=3]=`ShadowDom`,t[t.ExperimentalIsolatedShadowDom=4]=`ExperimentalIsolatedShadowDom`,t})(ei||{});var hd;function oI(){if(hd===void 0&&(hd=null,bi.trustedTypes))try{hd=bi.trustedTypes.createPolicy(`angular`,{createHTML:t=>t,createScript:t=>t,createScriptURL:t=>t})}catch{}return hd}function Vd(t){return oI()?.createHTML(t)||t}var qi=class{changingThisBreaksApplicationSecurity;constructor(n){this.changingThisBreaksApplicationSecurity=n}toString(){return`SafeValue must use [property]=binding: ${this.changingThisBreaksApplicationSecurity} (see ${Ul})`}};var Ih=class extends qi{getTypeName(){return`HTML`}};var kh=class extends qi{getTypeName(){return`Style`}};var Mh=class extends qi{getTypeName(){return`Script`}};var Th=class extends qi{getTypeName(){return`URL`}};var Nh=class extends qi{getTypeName(){return`ResourceURL`}};function ti(t){return t instanceof qi?t.changingThisBreaksApplicationSecurity:t}function Sa(t,n){let e=Ly(t);if(e!=null&&e!==n){if(e===`ResourceURL`&&n===`URL`)return!0;throw new Error(`Required a safe ${n}, got a ${e} (see ${Ul})`)}return e===n}function Ly(t){return t instanceof qi&&t.getTypeName()||null}function df(t){return new Ih(t)}function uf(t){return new kh(t)}function mf(t){return new Mh(t)}function pf(t){return new Th(t)}function hf(t){return new Nh(t)}function rI(t){let n=new Oh(t);return sI()?new Ah(n):n}var Ah=class{inertDocumentHelper;constructor(n){this.inertDocumentHelper=n}getInertBodyElement(n){n=`<body><remove></remove>`+n;try{let e=new window.DOMParser().parseFromString(Vd(n),`text/html`).body;return e===null?this.inertDocumentHelper.getInertBodyElement(n):(e.firstChild?.remove(),e)}catch{return null}}};var Oh=class{defaultDoc;inertDocument;constructor(n){this.defaultDoc=n,this.inertDocument=this.defaultDoc.implementation.createHTMLDocument(`sanitization-inert`)}getInertBodyElement(n){let e=this.inertDocument.createElement(`template`);return e.innerHTML=Vd(n),e}};function sI(){try{return!!new window.DOMParser().parseFromString(Vd(``),`text/html`)}catch{return!1}}var cI=/^(?!javascript:)(?:[a-z0-9+.-]+:|[^&:\/?#]*(?:[\/?#]|$))/i;function Qs(t){return t=String(t),t.match(cI)?t:`unsafe:`+t}function Yi(t){let n=Object.create(null);for(let e of t.split(`,`))n[e]=!0;return n}function Xs(...t){let n=Object.create(null);for(let e of t)for(let i in e)Object.hasOwn(e,i)&&(n[i]=!0);return n}var Vy=Yi(`area,br,col,hr,img,wbr`);var By=Yi(`colgroup,dd,dt,li,p,tbody,td,tfoot,th,thead,tr`);var jy=Yi(`rp,rt`);var lI=Xs(jy,By);var T_=Xs(Vy,Xs(By,Yi(`address,article,aside,blockquote,caption,center,del,details,dialog,dir,div,dl,figure,figcaption,footer,h1,h2,h3,h4,h5,h6,header,hgroup,hr,ins,main,map,menu,nav,ol,pre,section,summary,table,ul`)),Xs(jy,Yi(`a,abbr,acronym,audio,b,bdi,bdo,big,br,cite,code,del,dfn,em,font,i,img,ins,kbd,label,map,mark,picture,q,ruby,rp,rt,s,samp,small,source,span,strike,strong,sub,sup,time,track,tt,u,var,video`)),lI);var zy=Yi(`background,cite,href,itemtype,longdesc,poster,src,xlink:href`);var hI=Xs(zy,Yi(`abbr,accesskey,align,alt,autoplay,axis,bgcolor,border,cellpadding,cellspacing,class,clear,color,cols,colspan,compact,controls,coords,datetime,default,dir,download,face,headers,height,hidden,hreflang,hspace,ismap,itemscope,itemprop,kind,label,lang,language,loop,media,muted,nohref,nowrap,open,preload,rel,rev,role,rows,rowspan,rules,scope,scrolling,shape,size,sizes,span,srclang,srcset,start,summary,tabindex,target,title,translate,type,usemap,valign,value,vspace,width`),Yi(`aria-activedescendant,aria-atomic,aria-autocomplete,aria-busy,aria-checked,aria-colcount,aria-colindex,aria-colspan,aria-controls,aria-current,aria-describedby,aria-details,aria-disabled,aria-dropeffect,aria-errormessage,aria-expanded,aria-flowto,aria-grabbed,aria-haspopup,aria-hidden,aria-invalid,aria-keyshortcuts,aria-label,aria-labelledby,aria-level,aria-live,aria-modal,aria-multiline,aria-multiselectable,aria-orientation,aria-owns,aria-placeholder,aria-posinset,aria-pressed,aria-readonly,aria-relevant,aria-required,aria-roledescription,aria-rowcount,aria-rowindex,aria-rowspan,aria-selected,aria-setsize,aria-sort,aria-valuemax,aria-valuemin,aria-valuenow,aria-valuetext`));var fI=Yi(`script,style,template`);var Rh=class{sanitizedSomething=!1;buf=[];sanitizeChildren(n){let e=n.firstChild,i=!0,a=[];for(;e;){if(e.nodeType===Node.ELEMENT_NODE?i=this.startElement(e):e.nodeType===Node.TEXT_NODE?this.chars(e.nodeValue):this.sanitizedSomething=!0,i&&e.firstChild){a.push(e),e=vI(e);continue}for(;e;){e.nodeType===Node.ELEMENT_NODE&&this.endElement(e);let o=bI(e);if(o){e=o;break}e=a.pop()}}return this.buf.join(``)}startElement(n){let e=N_(n).toLowerCase();if(!Object.hasOwn(T_,e))return this.sanitizedSomething=!0,!Object.hasOwn(fI,e);this.buf.push(`<`),this.buf.push(e);let i=n.attributes;for(let a=0;a<i.length;a++){let o=i.item(a),r=o.name,s=r.toLowerCase();if(!Object.hasOwn(hI,s)){this.sanitizedSomething=!0;continue}let c=o.value;zy[s]&&(c=Qs(c)),this.buf.push(` `,r,`="`,A_(c),`"`)}return this.buf.push(`>`),!0}endElement(n){let e=N_(n).toLowerCase();Object.hasOwn(T_,e)&&!Object.hasOwn(Vy,e)&&(this.buf.push(`</`),this.buf.push(e),this.buf.push(`>`))}chars(n){this.buf.push(A_(n))}};function gI(t,n){return(t.compareDocumentPosition(n)&Node.DOCUMENT_POSITION_CONTAINED_BY)!==Node.DOCUMENT_POSITION_CONTAINED_BY}function bI(t){let n=t.nextSibling;if(n&&t!==n.previousSibling)throw Hy(n);return n}function vI(t){let n=t.firstChild;if(n&&gI(t,n))throw Hy(n);return n}function N_(t){let n=t.nodeName;return typeof n==`string`?n:`FORM`}function Hy(t){return new Error(`Failed to sanitize html because the element is clobbered: ${t.outerHTML}`)}var _I=/[\uD800-\uDBFF][\uDC00-\uDFFF]/g;var yI=/([^\#-~ |!])/g;function A_(t){return t.replace(/&/g,`&amp;`).replace(_I,function(n){let e=n.charCodeAt(0),i=n.charCodeAt(1);return`&#`+((e-55296)*1024+(i-56320)+65536)+`;`}).replace(yI,function(n){return`&#`+n.charCodeAt(0)+`;`}).replace(/</g,`&lt;`).replace(/>/g,`&gt;`)}var fd;function ff(t,n){let e=null;try{fd=fd||rI(t);let i=n?String(n):``;e=fd.getInertBodyElement(i);let a=5,o=i;do{if(a===0)throw new Error(`Failed to sanitize html because the input is unstable`);a--,i=o,o=e.innerHTML,e=fd.getInertBodyElement(i)}while(i!==o);return Vd(new Rh().sanitizeChildren(O_(e)||e))}finally{if(e){let i=O_(e)||e;for(;i.firstChild;)i.firstChild.remove()}}}function O_(t){return`content`in t&&xI(t)?t.content:null}function xI(t){return t.nodeType===Node.ELEMENT_NODE&&t.nodeName===`TEMPLATE`}var CI=/^>|^->|<!--|-->|--!>|<!-$/g;var SI=/(<|>)/g;var wI=`​$1​`;function EI(t){return t.replace(CI,n=>n.replace(SI,wI))}function DI(t,n){return t.createText(n)}function II(t,n,e){t.setValue(n,e)}function kI(t,n){return t.createComment(EI(n))}function Uy(t,n,e){return t.createElement(n,e)}function ho(t,n,e,i,a){t.insertBefore(n,e,i,a)}function $y(t,n,e){t.appendChild(n,e)}function R_(t,n,e,i,a){i!==null?ho(t,n,e,i,a):$y(t,n,e)}function Gy(t,n,e,i){t.removeChild(null,n,e,i)}function MI(t,n,e){t.setAttribute(n,`style`,e)}function TI(t,n,e){e===``?t.removeAttribute(n,`class`):t.setAttribute(n,`class`,e)}function Wy(t,n,e){let{mergedAttrs:i,classes:a,styles:o}=e;i!==null&&BD(t,n,i),a!==null&&TI(t,n,a),o!==null&&MI(t,n,o)}function hn(t){let n=NI();return n?n.sanitize(xn.URL,t)||``:Sa(t,`URL`)?ti(t):Qs($e(t))}function NI(){let t=le();return t&&t[Qn].sanitizer}function AI(t){return t instanceof Function?t():t}function OI(t,n,e){let i=t.length;for(;;){let a=t.indexOf(n,e);if(a===-1)return a;if(a===0||t.charCodeAt(a-1)<=32){let o=n.length;if(a+o===i||t.charCodeAt(a+o)<=32)return a}e=a+1}}var qy=`ng-template`;function RI(t,n,e,i){let a=0;if(i){for(;a<n.length&&typeof n[a]==`string`;a+=2)if(n[a]===`class`&&OI(n[a+1].toLowerCase(),e,0)!==-1)return!0}else if(gf(t))return!1;if(a=n.indexOf(1,a),a>-1){let o;for(;++a<n.length&&typeof(o=n[a])==`string`;)if(o.toLowerCase()===e)return!0}return!1}function gf(t){return t.type===4&&t.value!==qy}function PI(t,n,e){return n===(t.type===4&&!e?qy:t.value)}function FI(t,n,e){let i=4,a=t.attrs,o=a!==null?BI(a):0,r=!1;for(let s=0;s<n.length;s++){let c=n[s];if(typeof c==`number`){if(!r&&!Zn(i)&&!Zn(c))return!1;if(r&&Zn(c))continue;r=!1,i=c|i&1;continue}if(!r)if(i&4){if(i=2|i&1,c!==``&&!PI(t,c,e)||c===``&&n.length===1){if(Zn(i))return!1;r=!0}}else if(i&8){if(a===null||!RI(t,a,c,e)){if(Zn(i))return!1;r=!0}}else{let h=n[++s],p=LI(c,a,gf(t),e);if(p===-1){if(Zn(i))return!1;r=!0;continue}if(h!==``){let g;if(p>o?g=``:g=a[p+1].toLowerCase(),i&2&&h!==g){if(Zn(i))return!1;r=!0}}}}return Zn(i)||r}function Zn(t){return(t&1)===0}function LI(t,n,e,i){if(n===null)return-1;let a=0;if(i||!e){let o=!1;for(;a<n.length;){let r=n[a];if(r===t)return a;if(r===3||r===6)o=!0;else if(r===1||r===2){let s=n[++a];for(;typeof s==`string`;)s=n[++a];continue}else{if(r===4)break;if(r===0){a+=4;continue}}a+=o?1:2}return-1}else return jI(n,t)}function Yy(t,n,e=!1){for(let i=0;i<n.length;i++)if(FI(t,n[i],e))return!0;return!1}function VI(t){let n=t.attrs;if(n!=null){let e=n.indexOf(5);if((e&1)===0)return n[e+1]}return null}function BI(t){for(let n=0;n<t.length;n++){let e=t[n];if(hy(e))return n}return t.length}function jI(t,n){let e=t.indexOf(4);if(e>-1)for(e++;e<t.length;){let i=t[e];if(typeof i==`number`)return-1;if(i===n)return e;e++}return-1}function zI(t,n){e:for(let e=0;e<n.length;e++){let i=n[e];if(t.length===i.length){for(let a=0;a<t.length;a++)if(t[a]!==i[a])continue e;return!0}}return!1}function P_(t,n){return t?`:not(`+n.trim()+`)`:n}function HI(t){let n=t[0],e=1,i=2,a=``,o=!1;for(;e<t.length;){let r=t[e];if(typeof r==`string`)if(i&2){let s=t[++e];a+=`[`+r+(s.length>0?`="`+s+`"`:``)+`]`}else i&8?a+=`.`+r:i&4&&(a+=` `+r);else a!==``&&!Zn(r)&&(n+=P_(o,a),a=``),i=r,o=o||!Zn(i);e++}return a!==``&&(n+=P_(o,a)),n}function UI(t){return t.map(HI).join(`,`)}function $I(t){let n=[],e=[],i=1,a=2;for(;i<t.length;){let o=t[i];if(typeof o==`string`)a===2?o!==``&&n.push(o,t[++i]):a===8&&e.push(o);else{if(!Zn(a))break;a=o}i++}return e.length&&n.push(1,...e),n}var pt={};var xi=(function(t){return t[t.Important=1]=`Important`,t[t.DashCase=2]=`DashCase`,t})(xi||{});var GI;function bf(t,n){return GI(t,n)}var vf=new Set;typeof document<`u`&&document?.documentElement?.getAnimations;var Ph=new WeakMap;function Ky(t){return t?t[fa]??t:null}var pr=new WeakSet;function WI(t,n,e){let i=Ph.get(t);if(!i||i.length===0)return;let a=n.parentNode,o=n.previousSibling,r=Ky(e);for(let s=i.length-1;s>=0;s--){let{el:c,declarationView:h}=i[s],p=c.parentNode;c===n?(i.splice(s,1),pr.add(c),c.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}}))):o&&c===o?(i.splice(s,1),c.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}})),c.parentNode?.removeChild(c)):p&&a&&p!==a&&(r===null||h===null||r===h)&&(i.splice(s,1),c.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}})),c.parentNode?.removeChild(c))}}function qI(t,n,e){let i=Ky(e),a=Ph.get(t);a?a.some(o=>o.el===n)||a.push({el:n,declarationView:i}):Ph.set(t,[{el:n,declarationView:i}])}var Bd=(function(t){return t[t.CHANGE_DETECTION=0]=`CHANGE_DETECTION`,t[t.AFTER_NEXT_RENDER=1]=`AFTER_NEXT_RENDER`,t})(Bd||{});var ii=new k(``);var F_=new Set;function Ci(t){F_.has(t)||(F_.add(t),performance?.mark?.(`mark_feature_usage`,{detail:{feature:t}}))}var jd=(()=>{class t{impl=null;execute(){this.impl?.execute()}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var _f=[0,1,2,3];var yf=(()=>{class t{ngZone=f(X);scheduler=f(gi);errorHandler=f(Tn,{optional:!0});sequences=new Set;deferredRegistrations=new Set;executing=!1;constructor(){f(ii,{optional:!0})}execute(){let e=this.sequences.size>0;e&&Ke(ze.AfterRenderHooksStart),this.executing=!0;for(let i of _f)for(let a of this.sequences)if(!(a.erroredOrDestroyed||!a.hooks[i]))try{a.pipelinedValue=this.ngZone.runOutsideAngular(()=>this.maybeTrace(()=>{let o=a.hooks[i];return o(a.pipelinedValue)},a.snapshot))}catch(o){a.erroredOrDestroyed=!0,this.errorHandler?.handleError(o)}this.executing=!1;for(let i of this.sequences)i.afterRun(),i.once&&(this.sequences.delete(i),i.destroy());for(let i of this.deferredRegistrations)this.sequences.add(i);this.deferredRegistrations.size>0&&this.scheduler.notify(7),this.deferredRegistrations.clear(),e&&Ke(ze.AfterRenderHooksEnd)}register(e){let{view:i}=e;i!==void 0?((i[oo]??=[]).push(e),lo(i),i[xe]|=8192):this.executing?this.deferredRegistrations.add(e):this.addSequence(e)}addSequence(e){this.sequences.add(e),this.scheduler.notify(7)}unregister(e){this.executing&&this.sequences.has(e)?(e.erroredOrDestroyed=!0,e.pipelinedValue=void 0,e.once=!0):(this.sequences.delete(e),this.deferredRegistrations.delete(e))}maybeTrace(e,i){return i?i.run(Bd.AFTER_NEXT_RENDER,e):e()}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var $s=class{impl;hooks;view;once;snapshot;erroredOrDestroyed=!1;pipelinedValue=void 0;unregisterOnDestroy;constructor(n,e,i,a,o,r=null){this.impl=n,this.hooks=e,this.view=i,this.once=a,this.snapshot=r,this.unregisterOnDestroy=o?.onDestroy(()=>this.destroy())}afterRun(){this.erroredOrDestroyed=!1,this.pipelinedValue=void 0,this.snapshot?.dispose(),this.snapshot=null}destroy(){this.impl.unregister(this),this.unregisterOnDestroy?.();let n=this.view?.[oo];n&&(this.view[oo]=n.filter(e=>e!==this))}};function At(t,n){let e=n?.injector??f(se);return Ci(`NgAfterNextRender`),KI(t,e,n,!0)}function YI(t){return t instanceof Function?[void 0,void 0,t,void 0]:[t.earlyRead,t.write,t.mixedReadWrite,t.read]}function KI(t,n,e,i){let a=n.get(jd);a.impl??=n.get(yf);let o=n.get(ii,null,{optional:!0}),r=e?.manualCleanup!==!0?n.get(Ne):null,s=n.get(mr,null,{optional:!0}),c=new $s(a.impl,YI(t),s?.view,i,r,o?.snapshot(null));return a.impl.register(c),c}var Fh=null;var Qy=null;function L_(t,n,e,i){Fh?.maybeQueueEnterAnimation(t,n,e,i)}function V_(t,n,e,i){Fh===null?i(!1):Fh.runLeaveAnimationsWithCallback(t,n,e,i)}function QI(t){Qy?.initViewDetachAnimations(t)}function XI(t){Qy?.clearViewDetachAnimations(t)}function hr(t,n,e,i,a,o,r,s){if(a!=null){let c,h=!1;An(a)?c=a:zi(a)&&(h=!0,a=a[Kn]);let p=$t(a);t===0&&i!==null?(L_(s,i,o,e),r==null?$y(n,i,p):ho(n,i,p,r||null,!0)):t===1&&i!==null?(ho(n,i,p,r||null,!0),WI(o,p,s),pr.has(p)||L_(s,i,o,e)):t===2?(s?.[Gv]?.leave?.has(o.index)&&qI(o,p,s),pr.delete(p),V_(s,o,e,g=>{if(pr.has(p)){pr.delete(p);return}Gy(n,p,h,g)})):t===3&&(pr.delete(p),V_(s,o,e,()=>{n.destroyNode(p)})),c!=null&&ck(n,t,e,c,o,i,r)}}function ZI(t,n){Xy(t,n),n[Kn]=null,n[Ut]=null}function JI(t,n,e,i,a,o){i[Kn]=a,i[Ut]=n,Hd(t,i,e,1,a,o)}function Xy(t,n){n[Qn].changeDetectionScheduler?.notify(9),Hd(t,n,n[et],2,null,null)}function ek(t){let n=t[sr];if(!n)return uh(t[ve],t);for(;n;){let e=null;if(zi(n))e=n[sr];else{let i=n[yt];i&&(e=i)}if(!e){for(;n&&!n[Nn]&&n!==t;)zi(n)&&uh(n[ve],n),n=n[wt];n===null&&(n=t),zi(n)&&uh(n[ve],n),e=n&&n[Nn]}n=e}}function xf(t,n){let e=t[so],i=e.indexOf(n);e.splice(i,1)}function zd(t,n){if(co(n))return;let e=n[et];e.destroyNode&&Hd(t,n,e,3,null,null),ek(n)}function uh(t,n){if(co(n))return;let e=ge(null);try{n[xe]&=-129,n[xe]|=256,n[yn]&&da(n[yn]),nk(t,n),tk(t,n),n[ve].type===1&&n[et].destroy();let i=n[ga];if(i!==null&&An(n[wt])){i!==n[wt]&&xf(i,n);let a=n[vi];a!==null&&a.detachView(t)}wh(n)}finally{ge(e)}}function tk(t,n){let e=t.cleanup,i=n[rr];if(e!==null)for(let r=0;r<e.length-1;r+=2)if(typeof e[r]==`string`){let s=e[r+3];s>=0?i[s]():i[-s].unsubscribe(),r+=2}else{let s=i[e[r+1]];e[r].call(s)}i!==null&&(n[rr]=null);let a=n[Fi];if(a!==null){n[Fi]=null;for(let r=0;r<a.length;r++){let s=a[r];s()}}let o=n[Li];if(o!==null){n[Li]=null;for(let r of o)r.destroy()}}function nk(t,n){let e;if(t!=null&&(e=t.destroyHooks)!=null)for(let i=0;i<e.length;i+=2){let a=n[e[i]];if(!(a instanceof go)){let o=e[i+1];if(Array.isArray(o))for(let r=0;r<o.length;r+=2){let s=a[o[r]],c=o[r+1];Ke(ze.LifecycleHookStart,s,c);try{c.call(s)}finally{Ke(ze.LifecycleHookEnd,s,c)}}else{Ke(ze.LifecycleHookStart,a,o);try{o.call(a)}finally{Ke(ze.LifecycleHookEnd,a,o)}}}}}function Zy(t,n,e){if(n===null)throw new G(510,!1);return ik(t,n.parent,e)}function ik(t,n,e){let i=n;for(;i!==null&&i.type&168;)n=i,i=n.parent;if(i===null)return e[Kn];if(_i(i)){let{encapsulation:a}=t.data[i.directiveStart+i.componentOffset];if(a===ei.None||a===ei.Emulated)return null}return Cn(i,e)}function Jy(t,n,e){return ok(t,n,e)}function ak(t,n,e){return t.type&40?Cn(t,e):null}var ok=ak;function Cf(t,n,e,i){let a=Zy(t,i,n),o=n[et],s=Jy(i.parent||n[Ut],i,n);if(a!=null)if(Array.isArray(e))for(let c=0;c<e.length;c++)R_(o,a,e[c],s,!1);else R_(o,a,e,s,!1)}function zs(t,n){if(n!==null){let e=n.type;if(e&3)return Cn(n,t);if(e&4)return Lh(-1,t[n.index]);if(e&8){let i=n.child;if(i!==null)return zs(t,i);{let a=t[n.index];return An(a)?Lh(-1,a):$t(a)}}else{if(e&128)return zs(t,n.next);if(e&32)return bf(n,t)()||$t(t[n.index]);{let i=e0(t,n);if(i!==null){if(Array.isArray(i))return i[0];return zs(Vi(t[Qt]),i)}else return zs(t,n.next)}}}return null}function e0(t,n){if(n!==null){let i=t[Qt][Ut],a=n.projection;return i.projection[a]}return null}function Lh(t,n){let e=yt+t+1;if(e<n.length){let i=n[e],a=i[ve].firstChild;if(a!==null)return zs(i,a)}return n[ro]}function Sf(t,n,e,i,a,o,r){for(;e!=null;){let s=i[ji];if(e.type===128){e=e.next;continue}let c=i[e.index],h=e.type;if(r&&n===0&&(c&&br($t(c),i),e.flags|=2),!Ld(e))if(h&8)Sf(t,n,e.child,i,a,o,!1),hr(n,t,s,a,c,e,o,i);else if(h&32){let p=bf(e,i),g;for(;g=p();)hr(n,t,s,a,g,e,o,i);hr(n,t,s,a,c,e,o,i)}else h&16?t0(t,n,i,e,a,o):hr(n,t,s,a,c,e,o,i);e=r?e.projectionNext:e.next}}function Hd(t,n,e,i,a,o){t.type===3?rk(e,i,n,a,o):Sf(e,i,t.firstChild,n,a,o,!1)}function rk(t,n,e,i,a){let r=e[ve].firstChild,s=r.next,c=$t(e[r.index]),h=$t(e[s.index]),p=s.index+1,g=e[p];if(n===1||n===0)i!==null&&(g&&g.hasChildNodes()?ho(t,i,g,a,!0):(ho(t,i,c,a,!0),ho(t,i,h,a,!0)));else if(n===2){if(g||(g=document.createDocumentFragment(),e[p]=g),c&&c.parentNode===g)return;let v=c;for(;v!==null;){let b=v.nextSibling;if(g.appendChild(v),v===h)break;v=b}}}function sk(t,n,e){let i=n[et];t0(i,0,n,e,Zy(t,e,n),Jy(e.parent||n[Ut],e,n))}function t0(t,n,e,i,a,o){let r=e[Qt],c=r[Ut].projection[i.projection];if(Array.isArray(c))for(let h=0;h<c.length;h++){let p=c[h];hr(n,t,e[ji],a,p,i,o,e)}else{let h=c,p=r[wt];Ey(i)&&(h.flags|=128),Sf(t,n,h,p,a,o,!0)}}function ck(t,n,e,i,a,o,r){let s=i[ro];if(s!==$t(i)&&hr(n,t,e,o,s,a,r),(i[xe]&4)===0)for(let h=yt;h<i.length;h++){let p=i[h];Hd(p[ve],p,t,n,o,s)}}function lk(t,n,e,i,a){if(n)a?t.addClass(e,i):t.removeClass(e,i);else{let o=i.indexOf(`-`)===-1?void 0:xi.DashCase;a==null?t.removeStyle(e,i,o):(typeof a==`string`&&a.endsWith(`!important`)&&(a=a.slice(0,-10),o|=xi.Important),t.setStyle(e,i,a,o))}}function wf(t,n,e,i,a,o,r,s,c,h,p){let g=at+i,v=g+a,b=dk(g,v),y=typeof h==`function`?h():h;return b[ve]={type:t,blueprint:b,template:e,queries:null,viewQuery:s,declTNode:n,data:b.slice().fill(null,g),bindingStartIndex:g,expandoStartIndex:v,hostBindingOpCodes:null,firstCreatePass:!0,firstUpdatePass:!0,staticViewQueries:!1,staticContentQueries:!1,preOrderHooks:null,preOrderCheckHooks:null,contentHooks:null,contentCheckHooks:null,viewHooks:null,viewCheckHooks:null,destroyHooks:null,cleanup:null,contentQueries:null,components:null,directiveRegistry:typeof o==`function`?o():o,pipeRegistry:typeof r==`function`?r():r,firstChild:null,schemas:c,consts:y,incompleteFirstPass:!1,ssrId:p}}function dk(t,n){let e=[];for(let i=0;i<n;i++)e.push(i<t?null:pt);return e}function uk(t){let n=t.tView;return n===null||n.incompleteFirstPass?t.tView=wf(1,null,t.template,t.decls,t.vars,t.directiveDefs,t.pipeDefs,t.viewQuery,t.schemas,t.consts,t.id):n}function Ef(t,n,e,i,a,o,r,s,c,h,p){let g=n.blueprint.slice();return g[Kn]=a,g[xe]=i|1228,(h!==null||t&&t[xe]&2048)&&(g[xe]|=2048),zp(g),g[wt]=g[fa]=t,g[_t]=e,g[Qn]=r||t&&t[Qn],g[et]=s||t&&t[et],g[ji]=c||t&&t[ji]||null,g[Ut]=o,g[Ns]=JD(),g[io]=p,g[Fp]=h,g[Qt]=n.type==2?t[Qt]:g,g}function mk(t,n,e){let i=Cn(n,t),a=uk(e),o=t[Qn].rendererFactory,r=Df(t,Ef(t,a,null,n0(e),i,n,null,o.createRenderer(i,e),null,null,null));return t[n.index]=r}function n0(t){let n=16;return t.signals?n=4096:t.onPush&&(n=64),n}function i0(t,n,e,i){if(e===0)return-1;let a=n.length;for(let o=0;o<e;o++)n.push(i),t.blueprint.push(i),t.data.push(null);return a}function Df(t,n){return t[sr]?t[Pp][Nn]=n:t[sr]=n,t[Pp]=n,n}function m(t=1){a0(Xe(),le(),Xt()+t,!1)}function a0(t,n,e,i){if(!i)if((n[xe]&3)===3){let o=t.preOrderCheckHooks;o!==null&&bd(n,o,e)}else{let o=t.preOrderHooks;o!==null&&vd(n,o,0,e)}va(e)}var Zs=(function(t){return t[t.None=0]=`None`,t[t.SignalBased=1]=`SignalBased`,t[t.HasDecoratorInputTransform=2]=`HasDecoratorInputTransform`,t})(Zs||{});function bo(t,n,e,i){let a=ge(null);try{let[o,r,s]=t.inputs[e],c=null;(r&Zs.SignalBased)!==0&&(c=n[o][Ct]),c!==null&&c.transformFn!==void 0?i=c.transformFn(i):s!==null&&(i=s.call(n,i)),t.setInput!==null?t.setInput(n,c,i,e,o):ly(n,c,o,i)}finally{ge(a)}}function o0(t,n,e,i,a){let o=Xt(),r=i&2;try{va(-1),r&&n.length>at&&a0(t,n,at,!1);Ke(r?ze.TemplateUpdateStart:ze.TemplateCreateStart,a,e),e(i,a)}finally{va(o);Ke(r?ze.TemplateUpdateEnd:ze.TemplateCreateEnd,a,e)}}function Ud(t,n,e){vk(t,n,e),(e.flags&64)===64&&_k(t,n,e)}function Js(t,n,e=Cn){let i=n.localNames;if(i!==null){let a=n.index+1;for(let o=0;o<i.length;o+=2){let r=i[o+1],s=r===-1?e(n,t):t[r];t[a++]=s}}}function pk(t,n,e,i){let o=i.get(Ry,Oy)||e===ei.ShadowDom||e===ei.ExperimentalIsolatedShadowDom;return t.selectRootElement(n,o)}function gk(t){return t===`class`?`className`:t===`for`?`htmlFor`:t===`formaction`?`formAction`:t===`innerHtml`?`innerHTML`:t===`readonly`?`readOnly`:t===`tabindex`?`tabIndex`:t}function bk(t,n,e,i,a,o){let r=n[ve];if($d(t,r,n,e,i)){_i(t)&&s0(n,t.index);return}t.type&3&&(e=gk(e)),r0(t,n,e,i,a,o)}function r0(t,n,e,i,a,o){if(t.type&3){let r=Cn(t,n);i=o!=null?o(i,t.value||``,e):i,a.setProperty(r,e,i)}else t.type&12}function s0(t,n){let e=On(n,t);e[xe]&16||(e[xe]|=64)}function vk(t,n,e){let i=e.directiveStart,a=e.directiveEnd;_i(e)&&mk(n,e,t.data[i+e.componentOffset]),t.firstCreatePass||Ed(e,n);let o=e.initialInputs;for(let r=i;r<a;r++){let s=t.data[r],c=Us(n,t,r,e);if(br(c,n),o!==null&&Ck(n,r-i,c,s,e,o),Xn(s)){let h=On(e.index,n);h[_t]=Us(n,t,r,e)}}}function _k(t,n,e){let i=e.directiveStart,a=e.directiveEnd,o=e.index,r=o_();try{va(o);for(let s=i;s<a;s++){let c=t.data[s],h=n[s];rd(s),(c.hostBindings!==null||c.hostVars!==0||c.hostAttrs!==null)&&yk(c,h)}}finally{va(-1),rd(r)}}function yk(t,n){t.hostBindings!==null&&t.hostBindings(1,n)}function If(t,n){let e=t.directiveRegistry,i=null;if(e)for(let a=0;a<e.length;a++){let o=e[a];Yy(n,o.selectors,!1)&&(i??=[],Xn(o)?i.unshift(o):i.push(o))}return i}function xk(t,n,e,i,a,o){let r=Cn(t,n);c0(n[et],r,o,t.value,e,i,a)}function c0(t,n,e,i,a,o,r){if(o==null)r?.(o,i||``,a),t.removeAttribute(n,a,e);else{let s=r==null?$e(o):r(o,i||``,a);t.setAttribute(n,a,s,e)}}function Ck(t,n,e,i,a,o){let r=o[n];if(r!==null)for(let s=0;s<r.length;s+=2){let c=r[s],h=r[s+1];bo(i,e,c,h)}}function kf(t,n,e,i,a){let o=at+e,r=n[ve],s=a(r,n,t,i,e);n[o]=s,uo(t,!0);let c=t.type===2;return c?(Wy(n[et],s,t),(Jv()===0||lr(t))&&br(s,n),e_()):br(s,n),dd()&&(!c||!Ld(t))&&Cf(r,n,s,t),t}function Mf(t){let n=t;return Qp()?Xp():(n=n.parent,uo(n,!1)),n}function Sk(t,n){let e=t[ji];if(!e)return;let i;try{i=e.get(Ui,null)}catch{i=null}i?.(n)}function $d(t,n,e,i,a){let o=t.inputs?.[i],r=t.hostDirectiveInputs?.[i],s=!1;if(r)for(let c=0;c<r.length;c+=2){let h=r[c],p=r[c+1],g=n.data[h];bo(g,e[h],p,a),s=!0}if(o)for(let c of o){let h=e[c],p=n.data[c];bo(p,h,i,a),s=!0}return s}function wk(t,n,e,i,a,o){let r=null,s=null,c=null,h=!1,p=t.directiveToIndex.get(i.type);if(typeof p==`number`?r=p:[r,s,c]=p,s!==null&&c!==null&&t.hostDirectiveInputs&&Object.hasOwn(t.hostDirectiveInputs,a)){let g=t.hostDirectiveInputs[a];for(let v=0;v<g.length;v+=2){let b=g[v];if(b>=s&&b<=c){let y=n.data[b],E=g[v+1];bo(y,e[b],E,o),h=!0}else if(b>c)break}}return r!==null&&Object.hasOwn(i.inputs,a)&&(bo(i,e[r],a,o),h=!0),h}function Ek(t,n){let e=On(n,t),i=e[ve];Dk(i,e);let a=e[Kn];a!==null&&e[io]===null&&(e[io]=Py(a,e[ji])),Ke(ze.ComponentStart);try{Tf(i,e,e[_t])}finally{Ke(ze.ComponentEnd,e[_t])}}function Dk(t,n){for(let e=n.length;e<t.blueprint.length;e++)n.push(t.blueprint[e])}function Tf(t,n,e){cd(n);try{let i=t.viewQuery;i!==null&&Dh(1,i,e);let a=t.template;a!==null&&o0(t,n,a,1,e),t.firstCreatePass&&(t.firstCreatePass=!1),n[vi]?.finishViewCreation(t),t.staticContentQueries&&Fy(t,n),t.staticViewQueries&&Dh(2,t.viewQuery,e);let o=t.components;o!==null&&Ik(n,o)}catch(i){throw t.firstCreatePass&&(t.incompleteFirstPass=!0,t.firstCreatePass=!1),i}finally{n[xe]&=-5,ld()}}function Ik(t,n){for(let e=0;e<n.length;e++)Ek(t,n[e])}function ec(t,n,e,i){let a=ge(null);try{let o=n.tView,c=Ef(t,o,e,t[xe]&4096?4096:16,null,n,null,null,i?.injector??null,i?.embeddedViewInjector??null,i?.dehydratedView??null);c[ga]=t[n.index];let p=t[vi];return p!==null&&(c[vi]=p.createEmbeddedView(o)),Tf(o,c,e),c}finally{ge(a)}}function vr(t,n){return!n||n.firstChild===null||Ey(t)}function Gs(t,n,e,i,a=!1){if(t.type===3){let o=t.firstChild,r=o.next,s=$t(n[o.index]),c=$t(n[r.index]),h=s;for(;h!==null&&(i.push(h),h!==c);)h=h.nextSibling;return i}for(;e!==null;){if(e.type===128){e=a?e.projectionNext:e.next;continue}let o=n[e.index];if(o!==null)if(An(o)){let s=o[ro];s!==o[Kn]&&i.push($t(o)),o[xe]&4||l0(o,i),i.push(s)}else i.push($t(o));let r=e.type;if(r&8)Gs(t,n,e.child,i);else if(r&32){let s=bf(e,n),c;for(;c=s();)i.push(c)}else if(r&16){let s=e0(n,e);if(Array.isArray(s))i.push(...s);else{let c=Vi(n[Qt]);Gs(c[ve],c,s,i,!0)}}e=a?e.projectionNext:e.next}return i}function l0(t,n){for(let e=yt;e<t.length;e++){let i=t[e],a=i[ve].firstChild;a!==null&&Gs(i[ve],i,a,n)}}function d0(t){if(t[oo]!==null){for(let n of t[oo])n.impl.addSequence(n);t[oo].length=0}}var u0=[];function kk(t){return t[yn]??Mk(t)}function Mk(t){let n=u0.pop()??Object.create(Nk);return n.lView=t,n}function Tk(t){t.lView[yn]!==t&&(t.lView=null,u0.push(t))}var Nk=s(r({},Ua),{consumerIsAlwaysLive:!0,kind:`template`,consumerMarkedDirty:t=>{lo(t.lView)},consumerOnSignalRead(){this.lView[yn]=this}});function Ak(t){let n=t[yn]??Object.create(Ok);return n.lView=t,n}var Ok=s(r({},Ua),{consumerIsAlwaysLive:!0,kind:`template`,consumerMarkedDirty:t=>{let n=Vi(t.lView);for(;n&&!m0(n[ve]);)n=Vi(n);n&&Hp(n)},consumerOnSignalRead(){this.lView[yn]=this}});function m0(t){return t.type!==2}function p0(t){if(t[Li]===null)return;let n=!0;for(;n;){let e=!1;for(let i of t[Li])if(i.dirty&&(e=!0,i.zone===null||Zone.current===i.zone?i.run():i.zone.run(()=>i.run()),t[Li]===null))return;n=e&&!!(t[xe]&8192)}}var Rk=100;function h0(t,n=0){let i=t[Qn].rendererFactory;i.begin?.();try{Pk(t,n)}finally{i.end?.()}}function Pk(t,n){let e=Jp();try{Cs(!0),Vh(t,n);let i=0;for(;Ps(t);){if(i===Rk)throw new G(103,!1);i++,Vh(t,1)}}finally{Cs(e)}}function Fk(t,n,e,i){if(co(n))return;let a=n[xe];cd(n);let s=!0,c=null,h=null;m0(t)?(h=kk(n),c=la(h)):il()===null?(s=!1,h=Ak(n),c=la(h)):n[yn]&&(da(n[yn]),n[yn]=null);try{zp(n),n_(t.bindingStartIndex),e!==null&&o0(t,n,e,2,i);let p=(a&3)===3;if(p){let b=t.preOrderCheckHooks;b!==null&&bd(n,b,null)}else{let b=t.preOrderHooks;b!==null&&vd(n,b,0,null),lh(n,0)}if(Lk(n),p0(n),f0(n,0),t.contentQueries!==null&&Fy(t,n),true)if(p){let b=t.contentCheckHooks;b!==null&&bd(n,b)}else{let b=t.contentHooks;b!==null&&vd(n,b,1),lh(n,1)}Bk(t,n);let g=t.components;g!==null&&b0(n,g,0);let v=t.viewQuery;if(v!==null&&Dh(2,v,i),true)if(p){let b=t.viewCheckHooks;b!==null&&bd(n,b)}else{let b=t.viewHooks;b!==null&&vd(n,b,2),lh(n,2)}if(t.firstUpdatePass===!0&&(t.firstUpdatePass=!1),n[Jl]){for(let b of n[Jl])b();n[Jl]=null}d0(n),n[xe]&=-73}catch(p){let g=!1,v=p,b=n;for(;b!==null;){if(An(b)){b=b[wt];continue}let y=b[cr];if(y)try{let E=n[Qt][_t],ae={declarationInstance:E,declarationType:E?.constructor,caughtBy:y};y(g_(v),ae),g=!0;break}catch(E){v=E}b=b[wt]}if(!g)throw lo(n),v}finally{h!==null&&($a(h,c),s&&Tk(h)),ld()}}function f0(t,n){for(let e=Iy(t);e!==null;e=ky(e))for(let i=yt;i<e.length;i++){let a=e[i];g0(a,n)}}function Lk(t){for(let n=Iy(t);n!==null;n=ky(n)){if(!(n[xe]&2))continue;let e=n[so];for(let i=0;i<e.length;i++){let a=e[i];Hp(a)}}}function Vk(t,n,e){Ke(ze.ComponentStart);let i=On(n,t);try{g0(i,e)}finally{Ke(ze.ComponentEnd,i[_t])}}function g0(t,n){td(t)&&Vh(t,n)}function Vh(t,n){let i=t[ve],a=t[xe],o=t[yn],r=!!(n===0&&a&16);if(r||=!!(a&64&&n===0),r||=!!(a&1024),r||=!!(o?.dirty&&$o(o)),r||=!1,o&&(o.dirty=!1),t[xe]&=-9217,r)Fk(i,t,i.template,t[_t]);else if(a&8192){let s=ge(null);try{p0(t),f0(t,1);let c=i.components;c!==null&&b0(t,c,1),d0(t)}finally{ge(s)}}}function b0(t,n,e){for(let i=0;i<n.length;i++)Vk(t,n[i],e)}function Bk(t,n){let e=t.hostBindingOpCodes;if(e!==null)try{for(let i=0;i<e.length;i++){let a=e[i];if(a<0)va(~a);else{let o=a,r=e[++i],s=e[++i];a_(r,o);let c=n[o];Ke(ze.HostBindingsUpdateStart,c);try{s(2,c)}finally{Ke(ze.HostBindingsUpdateEnd,c)}}}}finally{va(-1)}}function Nf(t,n){let e=Jp()?64:1088;for(t[Qn].changeDetectionScheduler?.notify(n);t;){t[xe]|=e;let i=Vi(t);if(dr(t)&&!i)return t;t=i}return null}function v0(t,n,e,i){return[t,!0,0,n,null,i,null,e,null,null]}function _0(t,n){let e=yt+n;if(e<t.length)return t[e]}function tc(t,n,e,i=!0){let a=n[ve];if(jk(a,n,t,e),i){let r=Lh(e,t),s=n[et],c=s.parentNode(t[ro]);c!==null&&JI(a,t[Ut],s,n,c,r)}let o=n[io];o!==null&&o.firstChild!==null&&(o.firstChild=null)}function y0(t,n){let e=Ws(t,n);return e!==void 0&&zd(e[ve],e),e}function Ws(t,n){if(t.length<=yt)return;let e=yt+n,i=t[e];if(i){let a=i[ga];a!==null&&a!==t&&xf(a,i),n>0&&(t[e-1][Nn]=i[Nn]);let o=Ms(t,yt+n);ZI(i[ve],i);let r=o[vi];r!==null&&r.detachView(o[ve]),i[wt]=null,i[Nn]=null,i[xe]&=-129}return i}function jk(t,n,e,i){let a=yt+i,o=e.length;i>0&&(e[a-1][Nn]=n),i<o-yt?(n[Nn]=e[a],Tp(e,yt+i,n)):(e.push(n),n[Nn]=null),n[wt]=e;let r=n[ga];r!==null&&e!==r&&x0(r,n);let s=n[vi];s!==null&&s.insertView(t),nd(n),n[xe]|=128}function x0(t,n){let e=t[so],i=n[wt];if(zi(i))t[xe]|=2;else{let a=i[wt][Qt];n[Qt]!==a&&(t[xe]|=2)}e===null?t[so]=[n]:e.push(n)}var Ca=class{_lView;_cdRefInjectingView;_appRef=null;_attachedToViewContainer=!1;exhaustive;get rootNodes(){let n=this._lView,e=n[ve];return Gs(e,n,e.firstChild,[])}constructor(n,e){this._lView=n,this._cdRefInjectingView=e}get context(){return this._lView[_t]}set context(n){this._lView[_t]=n}get destroyed(){return co(this._lView)}destroy(){if(this._appRef)this._appRef.detachView(this);else if(this._attachedToViewContainer){let n=this._lView[wt];if(An(n)){let e=n[As],i=e?e.indexOf(this):-1;i>-1&&(Ws(n,i),Ms(e,i))}this._attachedToViewContainer=!1}zd(this._lView[ve],this._lView)}onDestroy(n){id(this._lView,n)}markForCheck(){Nf(this._cdRefInjectingView||this._lView,4)}detach(){this._lView[xe]&=-129}reattach(){nd(this._lView),this._lView[xe]|=128}detectChanges(){this._lView[xe]|=1024,h0(this._lView)}checkNoChanges(){}attachToViewContainerRef(){if(this._appRef)throw new G(902,!1);this._attachedToViewContainer=!0}detachFromAppRef(){this._appRef=null;let n=dr(this._lView),e=this._lView[ga];e!==null&&!n&&xf(e,this._lView),Xy(this._lView[ve],this._lView)}attachToAppRef(n){if(this._attachedToViewContainer)throw new G(902,!1);this._appRef=n;let e=dr(this._lView),i=this._lView[ga];i!==null&&!e&&x0(i,this._lView),nd(this._lView)}};var Vt=(()=>{class t{_declarationLView;_declarationTContainer;elementRef;static __NG_ELEMENT_ID__=zk;constructor(e,i,a){this._declarationLView=e,this._declarationTContainer=i,this.elementRef=a}get ssrId(){return this._declarationTContainer.tView?.ssrId||null}createEmbeddedView(e,i){return this.createEmbeddedViewImpl(e,i)}createEmbeddedViewImpl(e,i,a){return new Ca(ec(this._declarationLView,this._declarationTContainer,e,{embeddedViewInjector:i,dehydratedView:a}))}}return t})();function zk(){return Gd(Mt(),le())}function Gd(t,n){return t.type&4?new Vt(n,t,wr(t,n)):null}function _o(t,n,e,i,a){let o=t.data[n];if(o===null)o=Hk(t,n,e,i,a),i_()&&(o.flags|=32);else if(o.type&64){o.type=e,o.value=i,o.attrs=a;let r=t_();o.injectorIndex=r===null?-1:r.injectorIndex}return uo(o,!0),o}function Hk(t,n,e,i,a){let o=Kp(),r=Qp(),s=r?o:o&&o.parent,c=t.data[n]=$k(t,s,e,n,i,a);return Uk(t,c,o,r),c}function Uk(t,n,e,i){t.firstChild===null&&(t.firstChild=n),e!==null&&(i?e.child==null&&n.parent!==null&&(e.child=n):e.next===null&&(e.next=n,n.prev=e))}function $k(t,n,e,i,a,o){let r=n?n.injectorIndex:-1,s=0;return Wp()&&(s|=128),{type:e,index:i,insertBeforeIndex:null,injectorIndex:r,directiveStart:-1,directiveEnd:-1,directiveStylingLast:-1,componentOffset:-1,controlDirectiveIndex:-1,customControlIndex:-1,propertyBindings:null,flags:s,providerIndexes:0,value:a,namespace:nh(),attrs:o,mergedAttrs:null,localNames:null,initialInputs:null,inputs:null,hostDirectiveInputs:null,outputs:null,hostDirectiveOutputs:null,directiveToIndex:null,tView:null,next:null,prev:null,projectionNext:null,child:null,parent:n,projection:null,styles:null,stylesWithoutHost:null,residualStyles:void 0,classes:null,classesWithoutHost:null,residualClasses:void 0,classBindings:0,styleBindings:0}}function Gk(t){let n=t[Lp]??[],i=t[wt][et],a=[];for(let o of n)o.data[Ay]!==void 0?a.push(o):Wk(o,i);t[Lp]=a}function Wk(t,n){let e=0,i=t.firstChild;if(i){let a=t.data[Ny];for(;e<a;){let o=i.nextSibling;Gy(n,i,!1),i=o,e++}}}var qk=()=>null;var Yk=()=>null;function Dd(t,n){return qk(t,n)}function C0(t,n,e){return Yk(t,n,e)}var S0=class{};var Tt=class{};var Re=class{destroyNode=null;static __NG_ELEMENT_ID__=()=>Kk()};function Kk(){let t=le(),e=On(Mt().index,t);return(zi(e)?e:t)[et]}var w0=(()=>{class t{static ɵprov=be({token:t,providedIn:`root`,factory:()=>null})}return t})();function E0(t){return t.debugInfo?.className||t.type.name||null}var yd={};var Id=class{injector;parentInjector;constructor(n,e){this.injector=n,this.parentInjector=e}get(n,e,i){let a=this.injector.get(n,yd,i);return a!==yd||e===yd?a:this.parentInjector.get(n,e,i)}};function Af(t,n,e){return t[n]=e}function Qk(t,n){return t[n]}function pn(t,n,e){if(e===pt)return!1;let i=t[n];return Object.is(i,e)?!1:(t[n]=e,!0)}function _r(t,n,e,i){let a=pn(t,n,e);return pn(t,n+1,i)||a}function D0(t,n,e,i,a){let o=_r(t,n,e,i);return pn(t,n+2,a)||o}function Wd(t,n,e,i,a,o){let r=_r(t,n,e,i);return _r(t,n+2,a,o)||r}function fo(t,n,e){return function i(a){let o=i.__ngNativeEl__;o!==void 0&&tI(a,o);Nf(_i(t)?On(t.index,n):n,5);let s=n[_t],c=j_(n,s,e,a),h=i.__ngNextListenerFn__;for(;h;)c=j_(n,s,h,a)&&c,h=h.__ngNextListenerFn__;return c}}function j_(t,n,e,i){let a=ge(null);try{return Ke(ze.OutputStart,n,e),e(i)!==!1}catch(o){return Sk(t,o),!1}finally{Ke(ze.OutputEnd,n,e),ge(a)}}function Of(t,n,e,i,a,o,r,s){let c=lr(t),h=!1,p=null;if(!i&&c&&(p=Zk(n,e,o,t.index)),p!==null){let g=p.__ngLastListenerFn__||p;g.__ngNextListenerFn__=r,p.__ngLastListenerFn__=r,h=!0}else{let g=Cn(t,e),v=i?i(g):g;i||(s.__ngNativeEl__=g);let b=a.listen(v,o,s);if(!Xk(o))I0(i?E=>i($t(E[t.index])):t.index,n,e,o,s,b,!1)}return h}function Xk(t){return t.startsWith(`animation`)||t.startsWith(`transition`)}function Zk(t,n,e,i){let a=t.cleanup;if(a!=null)for(let o=0;o<a.length-1;o+=2){let r=a[o];if(r===e&&a[o+1]===i){let s=n[rr],c=a[o+2];return s&&s.length>c?s[c]:null}typeof r==`string`&&(o+=2)}return null}function I0(t,n,e,i,a,o,r){let s=n.firstCreatePass?$p(n):null,c=Up(e),h=c.length;c.push(a,o),s&&s.push(i,t,h,(h+1)*(r?-1:1))}function z_(t,n,e,i,a){let o=null,r=null,s=null,c=!1,h=t.directiveToIndex.get(e.type);if(typeof h==`number`?o=h:[o,r,s]=h,r!==null&&s!==null&&t.hostDirectiveOutputs&&Object.hasOwn(t.hostDirectiveOutputs,i)){let p=t.hostDirectiveOutputs[i];for(let g=0;g<p.length;g+=2){let v=p[g];if(v>=r&&v<=s)c=!0,kd(t,n,v,p[g+1],i,a);else if(v>s)break}}return Object.hasOwn(e.outputs,i)&&(c=!0,kd(t,n,o,i,i,a)),c}function kd(t,n,e,i,a,o){let r=n[e],s=n[ve],g=r[s.data[e].outputs[i]].subscribe(o);I0(t.index,s,n,a,o,g,!0)}function Er(){Jk()}function Jk(){let t=le(),n=Xe(),e=Mt();if(n.firstCreatePass&&tM(n,e),e.controlDirectiveIndex===-1)return;Ci(`NgSignalForms`);let i=t[e.controlDirectiveIndex];n.data[e.controlDirectiveIndex].controlDef.create(i,new Md(t,n,e))}function Dr(){eM()}function eM(){let t=le(),n=Xe(),e=ur();if(e.controlDirectiveIndex===-1)return;let i=n.data[e.controlDirectiveIndex].controlDef,a=t[e.controlDirectiveIndex];i.update(a,new Md(t,n,e))}var Md=class{lView;tView;tNode;hasPassThrough;constructor(n,e,i){this.lView=n,this.tView=e,this.tNode=i,this.hasPassThrough=!!(i.flags&4096)}get customControl(){return this.tNode.customControlIndex!==-1?this.lView[this.tNode.customControlIndex]:void 0}get nativeElement(){return Cn(this.tNode,this.lView)}get descriptor(){return`<${this.tNode.value}>`}listenToCustomControlOutput(n,e){let i=this.tView.data[this.tNode.customControlIndex];z_(this.tNode,this.lView,i,n,fo(this.tNode,this.lView,e))}listenToCustomControlModel(n){let e=this.tNode.flags&1024?`valueChange`:`checkedChange`,i=this.tView.data[this.tNode.customControlIndex];z_(this.tNode,this.lView,i,e,fo(this.tNode,this.lView,n))}listenToDom(n,e){Of(this.tNode,this.tView,this.lView,void 0,this.lView[et],n,e,fo(this.tNode,this.lView,e))}setInputOnDirectives(n,e,i){let a=this.tNode.inputs?.[n],o=this.tNode.hostDirectiveInputs?.[n];if(!a&&!o)return!1;let r=!1;if(a)for(let s of a){if(s===this.tNode.controlDirectiveIndex)continue;let c=this.lView[s],h=this.tView.data[s];(!i||i(U_(c,h,n)))&&(bo(h,c,n,e),r=!0)}if(o)for(let s=0;s<o.length;s+=2){let c=o[s];if(c===this.tNode.controlDirectiveIndex)continue;let h=this.lView[c],p=o[s+1],g=this.tView.data[c];(!i||i(U_(h,g,n)))&&(bo(g,h,p,e),r=!0)}return r}setCustomControlModelInput(n){let e=this.tView.data[this.tNode.customControlIndex],i=this.tNode.flags&1024?`value`:`checked`;wk(this.tNode,this.tView,this.lView,e,i,n)}customControlHasInput(n){if(this.tNode.customControlIndex===-1)return!1;let e=this.tView.data[this.tNode.customControlIndex];return(e.signalFormsInputPresence??=this._buildCustomControlInputCache(e))[n]===!0}_buildCustomControlInputCache(n){let e={};for(let i in n.inputs)e[i]=!0;if(n.hostDirectives!==null){let i=[...n.hostDirectives];for(;i.length>0;){let a=i.shift();if(typeof a!=`function`){for(let r in a.inputs)e[a.inputs[r]]=!0;let o=H_(a.directive);o!==null&&i.push(...o);continue}for(let o of a()){if(typeof o==`function`)continue;if(o.inputs)for(let s=0;s<o.inputs.length;s+=2){let c=o.inputs[s+1]||o.inputs[s];e[c]=!0}let r=H_(o.directive);r!==null&&i.push(...r)}}}return e}};function H_(t){return typeof t==`function`&&`ɵdir`in t?t.ɵdir.hostDirectives??null:null}function U_(t,n,e){if(!n.inputs||!Object.hasOwn(n.inputs,e))return;let[i,a]=n.inputs[e];if((a&Zs.SignalBased)!==0){let r=t[i][Ct];return r.value===Sr?void 0:r.value}return t[i]}function tM(t,n,e){for(let a=n.directiveStart;a<n.directiveEnd;a++)if(t.data[a].controlDef){n.controlDirectiveIndex=a;break}if(n.controlDirectiveIndex===-1)return;let i=t.data[n.controlDirectiveIndex].controlDef;if(i.passThroughInput&&(n.inputs?.[i.passThroughInput]?.length??0)>1){n.flags|=4096;return}nM(t,n)}function nM(t,n){for(let e=n.directiveStart;e<n.directiveEnd;e++){let i=t.data[e];if(!(n.directiveToIndex&&!n.directiveToIndex.has(i.type))){if($_(i,`value`)){n.flags|=1024,n.customControlIndex=e;return}if($_(i,`checked`)){n.flags|=2048,n.customControlIndex=e;return}}}if(n.hostDirectiveInputs!==null&&n.hostDirectiveOutputs!==null&&n.directiveToIndex!==null){let e=(i,a)=>{let o=n.hostDirectiveInputs[i],r=n.hostDirectiveOutputs[i+`Change`];if(!o||!r)return!1;for(let s=0;s<o.length;s+=2){let c=o[s];for(let h=0;h<r.length;h+=2)if(c===r[h])for(let g of n.directiveToIndex.values()){if(!Array.isArray(g))continue;let[v,b,y]=g;if(c>=b&&c<=y)return n.flags|=a,n.customControlIndex=v,!0}}return!1};if(e(`value`,1024)||e(`checked`,2048))return}}function $_(t,n){return iM(t,n)&&aM(t,n+`Change`)}function iM(t,n){return n in t.inputs}function aM(t,n){return n in t.outputs}var Bh=Symbol(`BINDING`);var yo=new k(``);function Td(t,n,e){let i=e?t.styles:null,a=e?t.classes:null,o=0;if(n!==null)for(let r=0;r<n.length;r++){let s=n[r];if(typeof s==`number`)o=s;else if(o==1)a=$l(a,s);else if(o==2){let c=s,h=n[++r];i=$l(i,c+`: `+h+`;`)}}e?t.styles=i:t.stylesWithoutHost=i,e?t.classes=a:t.classesWithoutHost=a}function He(t,n=0){let e=le();if(e===null)return Se(t,n);return xy(Mt(),e,Ft(t),n)}function Rf(){throw new Error(`invalid`)}function k0(t,n,e,i,a){let o=i===null?null:{"":-1},r=a(t,e);if(r!==null){let s=r,c=null,h=null;for(let p of r)if(p.resolveHostDirectives!==null){[s,c,h]=p.resolveHostDirectives(r);break}sM(t,n,e,s,o,c,h)}o!==null&&i!==null&&oM(e,i,o)}function oM(t,n,e){let i=t.localNames=[];for(let a=0;a<n.length;a+=2){let o=e[n[a+1]];if(o==null)throw new G(-301,!1);i.push(n[a],o)}}function rM(t,n,e){n.componentOffset=e,(t.components??=[]).push(n.index)}function sM(t,n,e,i,a,o,r){let s=i.length,c=null;for(let v=0;v<s;v++){let b=i[v];c===null&&Xn(b)&&(c=b,rM(t,e,v)),Ch(Ed(e,n),t,b.type)}pM(e,t.data.length,s),c?.viewProvidersResolver&&c.viewProvidersResolver(c);for(let v=0;v<s;v++){let b=i[v];b.providersResolver&&b.providersResolver(b)}let h=!1,p=!1,g=i0(t,n,s,null);s>0&&(e.directiveToIndex=new Map);for(let v=0;v<s;v++){let b=i[v];if(e.mergedAttrs=gr(e.mergedAttrs,b.hostAttrs),lM(t,e,n,g,b),mM(g,b,a),r!==null&&r.has(b)){let[E,P]=r.get(b);e.directiveToIndex.set(b.type,[g,E+e.directiveStart,P+e.directiveStart])}else(o===null||!o.has(b))&&e.directiveToIndex.set(b.type,g);b.contentQueries!==null&&(e.flags|=4),(b.hostBindings!==null||b.hostAttrs!==null||b.hostVars!==0)&&(e.flags|=64);let y=b.type.prototype;!h&&(y.ngOnChanges||y.ngOnInit||y.ngDoCheck)&&((t.preOrderHooks??=[]).push(e.index),h=!0),!p&&(y.ngOnChanges||y.ngDoCheck)&&((t.preOrderCheckHooks??=[]).push(e.index),p=!0),g++}cM(t,e,o)}function cM(t,n,e){for(let i=n.directiveStart;i<n.directiveEnd;i++){let a=t.data[i];if(e===null||!e.has(a))G_(0,n,a,i),G_(1,n,a,i),q_(n,i,!1);else{let o=e.get(a);W_(0,n,o,i),W_(1,n,o,i),q_(n,i,!0)}}}function G_(t,n,e,i){let a=t===0?e.inputs:e.outputs;for(let o in a)if(Object.hasOwn(a,o)){let r;t===0?r=n.inputs??={}:r=n.outputs??={},r[o]??=[],r[o].push(i),M0(n,o)}}function W_(t,n,e,i){let a=t===0?e.inputs:e.outputs;for(let o in a)if(Object.hasOwn(a,o)){let r=a[o],s;t===0?s=n.hostDirectiveInputs??={}:s=n.hostDirectiveOutputs??={},s[r]??=[],s[r].push(i,o),M0(n,r)}}function M0(t,n){n===`class`?t.flags|=8:n===`style`&&(t.flags|=16)}function q_(t,n,e){let{attrs:i,inputs:a,hostDirectiveInputs:o}=t;if(i===null||!e&&a===null||e&&o===null||gf(t)){t.initialInputs??=[],t.initialInputs.push(null);return}let r=null,s=0;for(;s<i.length;){let c=i[s];if(c===0){s+=4;continue}else if(c===5){s+=2;continue}else if(typeof c==`number`)break;if(!e&&Object.hasOwn(a,c)){let h=a[c];for(let p of h)if(p===n){r??=[],r.push(c,i[s+1]);break}}else if(e&&Object.hasOwn(o,c)){let h=o[c];for(let p=0;p<h.length;p+=2)if(h[p]===n){r??=[],r.push(h[p+1],i[s+1]);break}}s+=2}t.initialInputs??=[],t.initialInputs.push(r)}function lM(t,n,e,i,a){t.data[i]=a;let r=new go(a.factory||(a.factory=ma(a.type,!0)),Xn(a),He,null);t.blueprint[i]=r,e[i]=r,dM(t,n,i,i0(t,e,a.hostVars,pt),a)}function dM(t,n,e,i,a){let o=a.hostBindings;if(o){let r=t.hostBindingOpCodes;r===null&&(r=t.hostBindingOpCodes=[]);let s=~n.index;uM(r)!=s&&r.push(s),r.push(e,i,o)}}function uM(t){let n=t.length;for(;n>0;){let e=t[--n];if(typeof e==`number`&&e<0)return e}return 0}function mM(t,n,e){if(e){if(n.exportAs)for(let i=0;i<n.exportAs.length;i++)e[n.exportAs[i]]=t;Xn(n)&&(e[``]=t)}}function pM(t,n,e){t.flags|=1,t.directiveStart=n,t.directiveEnd=n+e,t.providerIndexes=n}function Pf(t,n,e,i,a,o,r,s){let c=n[ve],h=c.consts,g=_o(c,t,e,i,Sn(h,r));return o&&k0(c,n,g,Sn(h,s),a),g.mergedAttrs=gr(g.mergedAttrs,g.attrs),g.attrs!==null&&Td(g,g.attrs,!1),g.mergedAttrs!==null&&Td(g,g.mergedAttrs,!0),c.queries!==null&&c.queries.elementStart(c,g),g}function Ff(t,n){my(t,n),Vp(n)&&t.queries.elementEnd(n)}function hM(t,n,e,i,a,o){let r=n.consts,c=_o(n,t,e,i,Sn(r,a));if(c.mergedAttrs=gr(c.mergedAttrs,c.attrs),o!=null){let h=Sn(r,o);c.localNames=[];for(let p=0;p<h.length;p+=2)c.localNames.push(h[p],-1)}return c.attrs!==null&&Td(c,c.attrs,!1),c.mergedAttrs!==null&&Td(c,c.mergedAttrs,!0),n.queries!==null&&n.queries.elementStart(n,c),c}var T0=typeof ShadowRoot<`u`;var fM=typeof Document<`u`;function gM(t){return Object.keys(t).map(n=>{let[e,i,a]=t[n],o={propName:e,templateName:n,isSignal:(i&Zs.SignalBased)!==0};return a&&(o.transform=a),o})}function bM(t){return Object.keys(t).map(n=>({propName:t[n],templateName:n}))}function vM(t,n,e){let i=n instanceof kt?n:n?.injector;return i&&t.getStandaloneInjector!==null&&(i=t.getStandaloneInjector(i)||i),i?new Id(e,i):e}function _M(t){let n=t.get(Tt,null);if(n===null)throw new G(407,!1);return{rendererFactory:n,sanitizer:t.get(w0,null),changeDetectionScheduler:t.get(gi,null),ngReflect:!1,tracingService:t.get(ii,null,{optional:!0})}}function yM(t,n,e){let i=N0(t);return Uy(n,i,i===`svg`?Bp:i===`math`?qv:e)}function xM(t){if((t&&`localName`in t&&typeof t.localName==`string`?t.localName:t?.tagName)?.toLowerCase()===`script`)throw new G(905,!1)}function N0(t){return(t.selectors[0][0]||`div`).toLowerCase()}var yr=class{componentDef;ngModule;selector;componentType;ngContentSelectors;isBoundToModule;cachedInputs=null;cachedOutputs=null;get inputs(){return this.cachedInputs??=gM(this.componentDef.inputs),this.cachedInputs}get outputs(){return this.cachedOutputs??=bM(this.componentDef.outputs),this.cachedOutputs}constructor(n,e){this.componentDef=n,this.ngModule=e,this.componentType=n.type,this.selector=UI(n.selectors),this.ngContentSelectors=n.ngContentSelectors??[],this.isBoundToModule=!!e}create(n,e,i,a,o,r,s,c){Ke(ze.DynamicComponentStart);let h=ge(null);try{let p=this.componentDef,g=vM(p,a||this.ngModule,n),v=_M(g),b=v.tracingService;return b&&b.componentCreate?b.componentCreate(E0(p),()=>this.createComponentRef(v,g,e,i,o,r,s,c)):this.createComponentRef(v,g,e,i,o,r,s,c)}finally{ge(h)}}createComponentRef(n,e,i,a,o,r,s,c){let h=this.componentDef,p=CM(a,h,r,o,c),g=n.rendererFactory.createRenderer(null,h),v=a?pk(g,a,h.encapsulation,e):yM(h,g,s??null);xM(v);let b=e.get(yo,null),y=SM(v,()=>e.get(de,null)??Ty());b&&b.addHost(y);let E=r?.some(Y_)||o?.some(z=>typeof z!=`function`&&z.bindings.some(Y_)),P=Ef(null,p,null,512|n0(h),null,null,n,g,e,null,Py(v,e,!0));b&&T0&&y instanceof ShadowRoot&&id(P,()=>{b.removeHost(y)}),P[at]=v,cd(P);let ae=null;try{let z=Pf(at,P,2,`#host`,()=>p.directiveRegistry,!0,0);Wy(g,v,z),br(v,P),Ud(p,P,z),lf(p,z,P),Ff(p,z),i!==void 0&&EM(z,this.ngContentSelectors,i),ae=On(z.index,P),P[_t]=ae[_t],Tf(p,P,null)}catch(z){throw ae!==null&&wh(ae),wh(P),z}finally{Ke(ze.DynamicComponentEnd),ld()}return new Nd(this.componentType,P,!!E)}};function CM(t,n,e,i,a){let o=t?[`ng-version`,`22.2.1`]:$I(n.selectors[0]),r=null,s=null,c=0;if(e)for(let g of e)c+=g[Bh].requiredVars,g.create&&(g.targetIdx=0,(r??=[]).push(g)),g.update&&(g.targetIdx=0,(s??=[]).push(g));if(i)for(let g=0;g<i.length;g++){let v=i[g];if(typeof v!=`function`)for(let b of v.bindings){c+=b[Bh].requiredVars;let y=g+1;b.create&&(b.targetIdx=y,(r??=[]).push(b)),b.update&&(b.targetIdx=y,(s??=[]).push(b))}}let h=[n];if(i)for(let g of i){let b=ks(typeof g==`function`?g:g.type);h.push(b)}return wf(0,null,wM(r,s),1,c,h,null,null,null,[o],null)}function SM(t,n){let e=t.getRootNode?.();return fM&&e instanceof Document?e.head:e&&T0&&e instanceof ShadowRoot?e:n().head}function wM(t,n){return!t&&!n?null:e=>{if(e&1&&t)for(let i of t)i.create();if(e&2&&n)for(let i of n)i.update()}}function Y_(t){let n=t[Bh].kind;return n===`input`||n===`twoWay`}var Nd=class extends S0{_rootLView;_hasInputBindings;instance;hostView;changeDetectorRef;componentType;location;previousInputValues=null;_tNode;constructor(n,e,i){super(),this._rootLView=e,this._hasInputBindings=i,this._tNode=ed(e[ve],at),this.location=wr(this._tNode,e),this.instance=On(this._tNode.index,e)[_t],this.hostView=this.changeDetectorRef=new Ca(e,void 0),this.componentType=n}setInput(n,e){this._hasInputBindings;let i=this._tNode;if(this.previousInputValues??=new Map,this.previousInputValues.has(n)&&Object.is(this.previousInputValues.get(n),e))return;let a=this._rootLView;$d(i,a[ve],a,n,e);this.previousInputValues.set(n,e);Nf(On(i.index,a),1)}get injector(){return new Wi(this._tNode,this._rootLView)}destroy(){this.hostView.destroy()}onDestroy(n){this.hostView.onDestroy(n)}};function EM(t,n,e){let i=t.projection=[];for(let a=0;a<n.length;a++){let o=e[a];i.push(o!=null&&o.length?Array.from(o):null)}}var Bt=(()=>{class t{static __NG_ELEMENT_ID__=DM}return t})();function DM(){return A0(Mt(),le())}var jh=class t extends Bt{_lContainer;_hostTNode;_hostLView;constructor(n,e,i){super(),this._lContainer=n,this._hostTNode=e,this._hostLView=i}get element(){return wr(this._hostTNode,this._hostLView)}get injector(){return new Wi(this._hostTNode,this._hostLView)}get parentInjector(){let n=of(this._hostTNode,this._hostLView);if(fy(n)){let e=Sd(n,this._hostLView),i=Cd(n),a=e[ve].data[i+8];return new Wi(a,e)}else return new Wi(null,this._hostLView)}clear(){for(;this.length>0;)this.remove(this.length-1)}get(n){let e=K_(this._lContainer);return e!==null&&e[n]||null}get length(){return this._lContainer.length-yt}createEmbeddedView(n,e,i){let a,o,r;typeof i==`number`?a=i:i!=null&&(a=i.index,o=i.injector,r=i.onError);let s=Dd(this._lContainer,n.ssrId),c=n.createEmbeddedViewImpl(e||{},o,s);return r&&(c._lView[cr]=r),this.insertImpl(c,a,vr(this._hostTNode,s)),c}createComponent(n,e,i,a,o,r,s){let c,h,p=e||{};c=p.index,i=p.injector,a=p.projectableNodes,o=p.environmentInjector||p.ngModuleRef,r=p.directives,s=p.bindings,h=p.onError;let g=new yr(pa(n)),v=i||this.parentInjector;if(!o&&g.ngModule==null){let z=this.parentInjector.get(kt,null);z&&(o=z)}let b=pa(g.componentType??{}),y=Dd(this._lContainer,b?.id??null),E=y?.firstChild??null,P=g.create(v,a,E,o,r,s,this._getHostElementNamespace());return h&&(P.hostView._lView[cr]=h),this.insertImpl(P.hostView,c,vr(this._hostTNode,y)),P}_getHostElementNamespace(){if(this._hostTNode.type&2){let n=this._hostTNode.parent??this._hostLView[Ut];return n!==null&&n.type&2&&typeof n.value==`string`&&n.value.toLowerCase()===`foreignobject`?null:n?.namespace??null}return this._hostTNode.namespace}insert(n,e){return this.insertImpl(n,e,!0)}insertImpl(n,e,i){let a=n._lView;if(Kv(a)){let s=this.indexOf(n);if(s!==-1)this.detach(s);else{let c=a[wt],h=new t(c,c[Ut],c[wt]);h.detach(h.indexOf(n))}}let o=this._adjustIndex(e),r=this._lContainer;return tc(r,a,o,i),n.attachToViewContainerRef(),Tp(mh(r),o,n),n}move(n,e){return this.insert(n,e)}indexOf(n){let e=K_(this._lContainer);return e!==null?e.indexOf(n):-1}remove(n){let e=this._adjustIndex(n,-1),i=Ws(this._lContainer,e);i&&(Ms(mh(this._lContainer),e),zd(i[ve],i))}detach(n){let e=this._adjustIndex(n,-1),i=Ws(this._lContainer,e);return i&&Ms(mh(this._lContainer),e)!=null?new Ca(i):null}_adjustIndex(n,e=0){return n??this.length+e}};function K_(t){return t[As]}function mh(t){return t[As]||(t[As]=[])}function A0(t,n){let e,i=n[t.index];return An(i)?e=i:(e=v0(i,n,null,t),n[t.index]=e,Df(n,e)),kM(e,n,t,i),new jh(e,t,n)}function IM(t,n){let e=t[et],i=e.createComment(``),a=Cn(n,t);return ho(e,e.parentNode(a),i,e.nextSibling(a),!1),i}var kM=NM;var MM=()=>!1;function TM(t,n,e){return MM(t,n,e)}function NM(t,n,e,i){if(t[ro])return;let a;e.type&8?a=$t(i):a=IM(n,e),t[ro]=a}var zh=class t{queryList;matches=null;constructor(n){this.queryList=n}clone(){return new t(this.queryList)}setDirty(){this.queryList.setDirty()}};var Hh=class t{queries;constructor(n=[]){this.queries=n}createEmbeddedView(n){let e=n.queries;if(e!==null){let i=n.contentQueries!==null?n.contentQueries[0]:e.length,a=[];for(let o=0;o<i;o++){let r=e.getByIndex(o),s=this.queries[r.indexInDeclarationView];a.push(s.clone())}return new t(a)}return null}insertView(n){this.dirtyQueriesWithMatches(n)}detachView(n){this.dirtyQueriesWithMatches(n)}finishViewCreation(n){this.dirtyQueriesWithMatches(n)}dirtyQueriesWithMatches(n){for(let e=0;e<this.queries.length;e++)Vf(n,e).matches!==null&&this.queries[e].setDirty()}};var Ad=class{flags;read;predicate;constructor(n,e,i=null){this.flags=e,this.read=i,typeof n==`string`?this.predicate=FM(n):this.predicate=n}};var Uh=class t{queries;constructor(n=[]){this.queries=n}elementStart(n,e){for(let i=0;i<this.queries.length;i++)this.queries[i].elementStart(n,e)}elementEnd(n){for(let e=0;e<this.queries.length;e++)this.queries[e].elementEnd(n)}embeddedTView(n){let e=null;for(let i=0;i<this.length;i++){let a=e!==null?e.length:0,o=this.getByIndex(i).embeddedTView(n,a);o&&(o.indexInDeclarationView=i,e!==null?e.push(o):e=[o])}return e!==null?new t(e):null}template(n,e){for(let i=0;i<this.queries.length;i++)this.queries[i].template(n,e)}getByIndex(n){return this.queries[n]}get length(){return this.queries.length}track(n){this.queries.push(n)}};var $h=class t{metadata;matches=null;indexInDeclarationView=-1;crossesNgTemplate=!1;_declarationNodeIndex;_appliesToNextNode=!0;constructor(n,e=-1){this.metadata=n,this._declarationNodeIndex=e}elementStart(n,e){this.isApplyingToNode(e)&&this.matchTNode(n,e)}elementEnd(n){this._declarationNodeIndex===n.index&&(this._appliesToNextNode=!1)}template(n,e){this.elementStart(n,e)}embeddedTView(n,e){return this.isApplyingToNode(n)?(this.crossesNgTemplate=!0,this.addMatch(-n.index,e),new t(this.metadata)):null}isApplyingToNode(n){if(this._appliesToNextNode&&(this.metadata.flags&1)!==1){let e=this._declarationNodeIndex,i=n.parent;for(;i!==null&&i.type&8&&i.index!==e;)i=i.parent;return e===(i!==null?i.index:-1)}return this._appliesToNextNode}matchTNode(n,e){let i=this.metadata.predicate;if(Array.isArray(i))for(let a=0;a<i.length;a++){let o=i[a];this.matchTNodeWithReadOption(n,e,AM(e,o)),this.matchTNodeWithReadOption(n,e,_d(e,n,o,!1,!1))}else i===Vt?e.type&4&&this.matchTNodeWithReadOption(n,e,-1):this.matchTNodeWithReadOption(n,e,_d(e,n,i,!1,!1))}matchTNodeWithReadOption(n,e,i){if(i!==null){let a=this.metadata.read;if(a!==null)if(a===W||a===Bt||a===se||a===Vt&&e.type&4)this.addMatch(e.index,-2);else{let o=_d(e,n,a,!1,!1);o!==null&&this.addMatch(e.index,o)}else this.addMatch(e.index,i)}}addMatch(n,e){this.matches===null?this.matches=[n,e]:this.matches.push(n,e)}};function AM(t,n){let e=t.localNames;if(e!==null){for(let i=0;i<e.length;i+=2)if(e[i]===n)return e[i+1]}return null}function OM(t,n){return t.type&11?wr(t,n):t.type&4?Gd(t,n):null}function RM(t,n,e,i){return e===-1?OM(n,t):e===-2?PM(t,n,i):Us(t,t[ve],e,n)}function PM(t,n,e){if(e===W)return wr(n,t);if(e===Vt)return Gd(n,t);if(e===Bt)return A0(n,t);if(e===se)return new Wi(n,t)}function O0(t,n,e,i){let a=n[vi].queries[i];if(a.matches===null){let o=t.data,r=e.matches,s=[];for(let c=0;r!==null&&c<r.length;c+=2){let h=r[c];if(h<0)s.push(null);else{let p=o[h];s.push(RM(n,p,r[c+1],e.metadata.read))}}a.matches=s}return a.matches}function Gh(t,n,e,i){let a=t.queries.getByIndex(e),o=a.matches;if(o!==null){let r=O0(t,n,a,e);for(let s=0;s<o.length;s+=2){let c=o[s];if(c>0)i.push(r[s/2]);else{let h=o[s+1],p=n[-c];for(let g=yt;g<p.length;g++){let v=p[g];v[ga]===v[wt]&&Gh(v[ve],v,h,i)}if(p[so]!==null){let g=p[so];for(let v=0;v<g.length;v++){let b=g[v];Gh(b[ve],b,h,i)}}}}}return i}function Lf(t,n){return t[vi].queries[n].queryList}function R0(t,n,e){let i=new wn((e&4)===4);return Zv(t,n,i,i.destroy),(n[vi]??=new Hh).queries.push(new zh(i))-1}function P0(t,n,e){let i=Xe();return i.firstCreatePass&&(L0(i,new Ad(t,n,e),-1),(n&2)===2&&(i.staticViewQueries=!0)),R0(i,le(),n)}function F0(t,n,e,i){let a=Xe();if(a.firstCreatePass){let o=Mt();L0(a,new Ad(n,e,i),o.index),LM(a,t),(e&2)===2&&(a.staticContentQueries=!0)}return R0(a,le(),e)}function FM(t){return t.split(`,`).map(n=>n.trim())}function L0(t,n,e){t.queries===null&&(t.queries=new Uh),t.queries.track(new $h(n,e))}function LM(t,n){let e=t.contentQueries||(t.contentQueries=[]);n!==(e.length?e[e.length-1]:-1)&&e.push(t.queries.length-1,n)}function Vf(t,n){return t.queries.getByIndex(n)}function V0(t,n){let e=t[ve],i=Vf(e,n);return i.crossesNgTemplate?Gh(e,t,n,[]):O0(e,t,i,n)}function B0(t,n,e){let i,a=hs(()=>{i._dirtyCounter();let o=VM(i,t);if(n&&o===void 0)throw new G(-951,!1);return o});return i=a[Ct],i._dirtyCounter=T(0),i._flatValue=void 0,a}function Bf(t){return B0(!0,!1,t)}function jf(t){return B0(!0,!0,t)}function j0(t,n){let e=t[Ct];e._lView=le(),e._queryIndex=n,e._queryList=Lf(e._lView,n),e._queryList.onDirty(()=>e._dirtyCounter.update(i=>i+1))}function VM(t,n){let e=t._lView,i=t._queryIndex;if(e===void 0||i===void 0||e[xe]&4)return n?void 0:Kt;let a=Lf(e,i),o=V0(e,i);return a.reset(o,wy),n?a.first:a._changesDetected||t._flatValue===void 0?t._flatValue=a.toArray():t._flatValue}function xo(t){return!!t&&typeof t.then==`function`}function zf(t){return!!t&&typeof t.subscribe==`function`}var ni=class{};function Hf(t,n){return new Od(t,n??null,[])}var Od=class extends ni{ngModuleType;_parent;_bootstrapComponents=[];_r3Injector;instance;destroyCbs=[];constructor(n,e,i,a=!0){super(),this.ngModuleType=n,this._parent=e;let o=Tv(n);this._bootstrapComponents=AI(o.bootstrap),this._r3Injector=ih(n,e,[{provide:ni,useValue:this},...i],Es(n),new Set([`environment`])),a&&this.resolveInjectorInitializers()}resolveInjectorInitializers(){this._r3Injector.resolveInjectorInitializers(),this.instance=this._r3Injector.get(this.ngModuleType)}get injector(){return this._r3Injector}destroy(){let n=this._r3Injector;!n.destroyed&&n.destroy(),this.destroyCbs.forEach(e=>e()),this.destroyCbs=null}onDestroy(n){this.destroyCbs.push(n)}};var qs=class extends ni{injector;instance=null;constructor(n){super();let e=new eo([...n.providers,{provide:ni,useValue:this}],n.parent||ar(),n.debugName,new Set([`environment`]));this.injector=e,n.runEnvironmentInitializers&&e.resolveInjectorInitializers()}destroy(){this.injector.destroy()}onDestroy(n){this.injector.onDestroy(n)}};function z0(t,n,e=null){return new qs({providers:t,parent:n,debugName:e,runEnvironmentInitializers:!0}).injector}var BM=(()=>{class t{_injector;cachedInjectors=new Map;constructor(e){this._injector=e}getOrCreateStandaloneInjector(e){if(!e.standalone)return null;if(!this.cachedInjectors.has(e)){let i=Ap(!1,e.type),a=i.length>0?z0([i],this._injector,``):null;this.cachedInjectors.set(e,a)}return this.cachedInjectors.get(e)}ngOnDestroy(){try{for(let e of this.cachedInjectors.values())e!==null&&e.destroy()}finally{this.cachedInjectors.clear()}}static ɵprov=be({token:t,providedIn:`environment`,factory:()=>new t(Se(kt))})}return t})();function B(t){return Ks(()=>{let n=H0(t),e=s(r({},n),{decls:t.decls,vars:t.vars,template:t.template,consts:t.consts||null,ngContentSelectors:t.ngContentSelectors,onPush:t.changeDetection!==sf.Eager,directiveDefs:null,pipeDefs:null,dependencies:n.standalone&&t.dependencies||null,getStandaloneInjector:n.standalone?a=>a.get(BM).getOrCreateStandaloneInjector(e):null,getExternalStyles:null,signals:t.signals??!1,data:t.data||{},encapsulation:t.encapsulation||ei.Emulated,styles:t.styles||Kt,_:null,schemas:t.schemas||null,tView:null,id:``});n.standalone&&Ci(`NgStandalone`),U0(e);let i=t.dependencies;return e.directiveDefs=Q_(i,jM),e.pipeDefs=Q_(i,Nv),e.id=UM(e),e})}function jM(t){return pa(t)||ks(t)}function re(t){return Ks(()=>({type:t.type,bootstrap:t.bootstrap||Kt,declarations:t.declarations||Kt,imports:t.imports||Kt,exports:t.exports||Kt,transitiveCompileScopes:null,schemas:t.schemas||null,id:t.id||null}))}function zM(t,n){if(t==null)return ha;let e={};for(let i in t)if(Object.hasOwn(t,i)){let a=t[i],o,r,s,c;Array.isArray(a)?(s=a[0],o=a[1],r=a[2]??o,c=a[3]||null):(o=a,r=a,s=Zs.None,c=null),e[o]=[i,s,c],n[o]=r}return e}function HM(t){if(t==null)return ha;let n={};for(let e in t)Object.hasOwn(t,e)&&(n[t[e]]=e);return n}function K(t){return Ks(()=>{let n=H0(t);return U0(n),n})}function nc(t){return{type:t.type,name:t.name,factory:null,pure:t.pure!==!1,standalone:t.standalone??!0,onDestroy:t.type.prototype.ngOnDestroy||null}}function H0(t){let n={};return{type:t.type,providersResolver:null,viewProvidersResolver:null,factory:null,hostBindings:t.hostBindings||null,hostVars:t.hostVars||0,hostAttrs:t.hostAttrs||null,contentQueries:t.contentQueries||null,declaredInputs:n,inputConfig:t.inputs||ha,exportAs:t.exportAs||null,standalone:t.standalone??!0,signals:t.signals===!0,selectors:t.selectors||Kt,viewQuery:t.viewQuery||null,features:t.features||null,setInput:null,resolveHostDirectives:null,hostDirectives:null,controlDef:null,signalFormsInputPresence:null,inputs:zM(t.inputs,n),outputs:HM(t.outputs),debugInfo:null}}function U0(t){t.features?.forEach(n=>n(t))}function Q_(t,n){return t?()=>{let e=typeof t==`function`?t():t,i=[];for(let a of e){let o=n(a);o!==null&&i.push(o)}return i}:null}function UM(t){let n=0,e=typeof t.consts==`function`?``:t.consts,i=[t.selectors,t.ngContentSelectors,t.hostVars,t.hostAttrs,e,t.vars,t.decls,t.encapsulation,t.standalone,t.signals,t.exportAs,JSON.stringify(t.inputs),JSON.stringify(t.outputs),Object.getOwnPropertyNames(t.type.prototype),!!t.contentQueries,!!t.viewQuery];for(let o of i.join(`|`))n=Math.imul(31,n)+o.charCodeAt(0)<<0;return n+=2147483648,`c`+n}var $0=new k(``);var Uf=(()=>{class t{resolve;reject;initialized=!1;done=!1;donePromise=new Promise((e,i)=>{this.resolve=e,this.reject=i});appInits=f($0,{optional:!0})??[];injector=f(se);constructor(){}runInitializers(){if(this.initialized)return;let e=[];for(let a of this.appInits){let o=or(this.injector,a);if(xo(o))e.push(o);else if(zf(o)){let r=new Promise((s,c)=>{o.subscribe({complete:s,error:c})});e.push(r)}}let i=()=>{this.done=!0,this.resolve()};Promise.all(e).then(()=>{i()}).catch(a=>{this.reject(a)}),e.length===0&&i(),this.initialized=!0}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();function $f(t){return n=>{n.controlDef={create:(e,i)=>{e?.ɵngControlCreate(i)},update:(e,i)=>{e?.ɵngControlUpdate?.(i)},passThroughInput:t}}}function ic(t){let n=e=>{let i=Array.isArray(t);e.hostDirectives===null?(e.resolveHostDirectives=$M,e.hostDirectives=i?t.map(Wh):[t]):i?e.hostDirectives.unshift(...t.map(Wh)):e.hostDirectives.unshift(t)};return n.ngInherit=!0,n}function $M(t){let n=[],e=!1,i=null,a=null;for(let o=0;o<t.length;o++){let r=t[o];if(r.hostDirectives!==null){let s=n.length;i??=new Map,a??=new Map,G0(r,n,i,t),a.set(r,[s,n.length-1])}o===0&&Xn(r)&&(e=!0,n.push(r))}for(let o=e?1:0;o<t.length;o++)n.push(t[o]);return i!==null&&i.forEach((o,r)=>{GM(r.declaredInputs,o.inputs)}),[n,i,a]}function G0(t,n,e,i){if(t.hostDirectives!==null)for(let a of t.hostDirectives)if(typeof a==`function`){let o=a();for(let r of o)X_(Wh(r),n,e,i)}else X_(a,n,e,i)}function X_(t,n,e,i){let a=ks(t.directive);if(G0(a,n,e,i),e.has(a)){let o=e.get(a);Z_(o,t.inputs,`input`),Z_(o,t.outputs,`output`)}else i.includes(a)||(e.set(a,t),n.push(a))}function Z_(t,n,e){let i=e===`input`?t.inputs:t.outputs;Object.keys(n).forEach(a=>{let o=n[a];(!Object.hasOwn(i,a)||i[a]===o)&&(i[a]=o)})}function Wh(t){return typeof t==`function`?{directive:Ft(t),inputs:{},outputs:{}}:{directive:Ft(t.directive),inputs:J_(t.inputs),outputs:J_(t.outputs)}}function J_(t){let n={};if(t!==void 0&&t.length>0)for(let e=0;e<t.length;e+=2)n[t[e]]=t[e+1];return n}function GM(t,n){for(let e in n)if(Object.hasOwn(n,e)){let i=n[e];t[i]=t[e]}}function WM(t){return Object.getPrototypeOf(t.prototype).constructor}function Ve(t){let n=WM(t.type),e=!0,i=[t];for(;n&&n!==Function.prototype&&n!==Object.prototype;){let a,o=Object.hasOwn(n,Ds)?n[Ds]:void 0,r=Object.hasOwn(n,Is)?n[Is]:void 0;if(Xn(t))a=o??r;else{if(o)throw new G(903,!1);a=r}if(a){if(e){i.push(a);let c=t;c.inputs=ph(t.inputs),c.declaredInputs=ph(t.declaredInputs),c.outputs=ph(t.outputs);let h=a.hostBindings;h&&XM(t,h);let p=a.viewQuery,g=a.contentQueries;if(p&&KM(t,p),g&&QM(t,g),qM(t,a),Mv(t.outputs,a.outputs),Xn(a)&&a.data.animation){let v=t.data;v.animation=(v.animation||[]).concat(a.data.animation)}}let s=a.features;if(s)for(let c=0;c<s.length;c++){let h=s[c];h&&h.ngInherit&&h(t),h===Ve&&(e=!1)}}n=Object.getPrototypeOf(n)}YM(i)}function qM(t,n){for(let e in n.inputs){if(!Object.hasOwn(n.inputs,e)||Object.hasOwn(t.inputs,e))continue;let i=n.inputs[e];i!==void 0&&(t.inputs[e]=i,t.declaredInputs[e]=n.declaredInputs[e])}}function YM(t){let n=0,e=null;for(let i=t.length-1;i>=0;i--){let a=t[i];a.hostVars=n+=a.hostVars,a.hostAttrs=gr(a.hostAttrs,e=gr(e,a.hostAttrs))}}function ph(t){return t===ha?{}:t===Kt?[]:t}function KM(t,n){let e=t.viewQuery;e?t.viewQuery=(i,a)=>{n(i,a),e(i,a)}:t.viewQuery=n}function QM(t,n){let e=t.contentQueries;e?t.contentQueries=(i,a,o)=>{n(i,a,o),e(i,a,o)}:t.contentQueries=n}function XM(t,n){let e=t.hostBindings;e?t.hostBindings=(i,a)=>{n(i,a),e(i,a)}:t.hostBindings=n}function W0(t,n,e,i,a,o,r,s){if(e.firstCreatePass){t.mergedAttrs=gr(t.mergedAttrs,t.attrs);let p=t.tView=wf(2,t,a,o,r,e.directiveRegistry,e.pipeRegistry,null,e.schemas,e.consts,null);e.queries!==null&&(e.queries.template(e,t),p.queries=e.queries.embeddedTView(t))}s&&(t.flags|=s),uo(t,!1);let c=JM(e,n,t,i);dd()&&Cf(e,n,c,t),br(c,n);let h=v0(c,n,c,t);n[i+at]=h,Df(n,h),TM(h,t,n)}function ZM(t,n,e,i,a,o,r,s,c,h,p){let g=e+at,v;return n.firstCreatePass?(v=_o(n,g,4,r||null,s||null),ad()&&k0(n,t,v,Sn(n.consts,h),If),my(n,v)):v=n.data[g],W0(v,t,n,e,i,a,o,c),lr(v)&&Ud(n,t,v),h!=null&&Js(t,v,p),v}function xr(t,n,e,i,a,o,r,s,c,h,p){let g=e+at,v;if(n.firstCreatePass){if(v=_o(n,g,4,r||null,s||null),h!=null){let b=Sn(n.consts,h);v.localNames=[];for(let y=0;y<b.length;y+=2)v.localNames.push(b[y],-1)}}else v=n.data[g];return W0(v,t,n,e,i,a,o,c),h!=null&&Js(t,v,p),v}function ot(t,n,e,i,a,o,r,s){let c=le(),h=Xe();return ZM(c,h,t,n,e,i,a,Sn(h.consts,o),void 0,r,s),ot}function Ir(t,n,e,i,a,o,r,s){let c=le(),h=Xe();return xr(c,h,t,n,e,i,a,Sn(h.consts,o),void 0,r,s),Ir}var JM=e1;function e1(t,n,e,i){return Ls(!0),n[et].createComment(``)}var Gf=new k(``);var Wf=new k(``);function q0(){Gm(t=>{throw new G(-600,``)})}var t1=10;var Rn=(()=>{class t{_runningTick=!1;_destroyed=!1;_destroyListeners=[];_views=[];internalErrorHandler=f(Ui);afterRenderManager=f(jd);zonelessEnabled=f(Bs);rootEffectScheduler=f(pd);dirtyFlags=0;tracingSnapshot=null;allTestViews=new Set;autoDetectTestViews=new Set;includeAllTestViews=!1;afterTick=new L;get allViews(){return[...(this.includeAllTestViews?this.allTestViews:this.autoDetectTestViews).keys(),...this._views]}get destroyed(){return this._destroyed}componentTypes=[];components=[];internalPendingTask=f(po);get isStable(){return this.internalPendingTask.hasPendingTasksObservable.pipe(st(e=>!e))}constructor(){f(ii,{optional:!0})}whenStable(){let e;return new Promise(i=>{e=this.isStable.subscribe({next:a=>{a&&i()}})}).finally(()=>{e.unsubscribe()})}_injector=f(kt);_rendererFactory=null;get injector(){return this._injector}bootstrap(e,i){return this.bootstrapImpl(e,i)}bootstrapImpl(e,i,a=se.NULL){return this._injector.get(X).run(()=>{if(Ke(ze.BootstrapComponentStart),!this._injector.get(Uf).done)throw new G(405,``);let s=pa(e),c=this._injector.get(ni),h=new yr(s,c);this.componentTypes.push(e);let{hostElement:p,directives:g,bindings:v}=n1(i),b=p||h.selector,y=h.create(a,[],b,c.injector,g,v),E=y.location.nativeElement,P=y.injector.get(Gf,null);return P?.registerApplication(E),y.onDestroy(()=>{this.detachView(y.hostView),Hs(this.components,y),P?.unregisterApplication(E)}),this._loadComponent(y),Ke(ze.BootstrapComponentEnd,y),y})}tick(){this.zonelessEnabled||(this.dirtyFlags|=1),this._tick()}_tick(){Ke(ze.ChangeDetectionStart),this.tracingSnapshot!==null?this.tracingSnapshot.run(Bd.CHANGE_DETECTION,this.tickImpl):this.tickImpl()}tickImpl=()=>{if(this._runningTick)throw Ke(ze.ChangeDetectionEnd),new G(101,!1);let e=ge(null);try{this._runningTick=!0,this.synchronize()}finally{this._runningTick=!1,this.tracingSnapshot?.dispose(),this.tracingSnapshot=null,ge(e),this.afterTick.next(),Ke(ze.ChangeDetectionEnd)}};synchronize(){this._rendererFactory===null&&!this._injector.destroyed&&(this._rendererFactory=this._injector.get(Tt,null,{optional:!0}));let e=0;for(;this.dirtyFlags!==0&&e++<t1;){Ke(ze.ChangeDetectionSyncStart);try{this.synchronizeOnce()}finally{Ke(ze.ChangeDetectionSyncEnd)}}}synchronizeOnce(){this.dirtyFlags&16&&(this.dirtyFlags&=-17,this.rootEffectScheduler.flush());let e=!1;if(this.dirtyFlags&7){let i=!!(this.dirtyFlags&1);this.dirtyFlags&=-8,this.dirtyFlags|=8;for(let{_lView:a}of this.allViews){if(!i&&!Ps(a))continue;h0(a,i&&!this.zonelessEnabled?0:1),e=!0}if(this.dirtyFlags&=-5,this.syncDirtyFlagsWithViews(),this.dirtyFlags&23)return}e||(this._rendererFactory?.begin?.(),this._rendererFactory?.end?.()),this.dirtyFlags&8&&(this.dirtyFlags&=-9,this.afterRenderManager.execute()),this.syncDirtyFlagsWithViews()}syncDirtyFlagsWithViews(){if(this.allViews.some(({_lView:e})=>Ps(e))){this.dirtyFlags|=2;return}else this.dirtyFlags&=-8}attachView(e){let i=e;this._views.push(i),i.attachToAppRef(this)}detachView(e){let i=e;Hs(this._views,i),i.detachFromAppRef()}_loadComponent(e){this.attachView(e.hostView);try{this.tick()}catch(a){this.internalErrorHandler(a)}this.components.push(e),this._injector.get(Wf,[]).forEach(a=>a(e))}ngOnDestroy(){if(!this._destroyed)try{this._destroyListeners.forEach(e=>e()),this._views.slice().forEach(e=>e.destroy())}finally{this._destroyed=!0,this._views=[],this._destroyListeners=[]}}onDestroy(e){return this._destroyListeners.push(e),()=>Hs(this._destroyListeners,e)}destroy(){if(this._destroyed)throw new G(406,!1);let e=this._injector;e.destroy&&!e.destroyed&&e.destroy()}get viewCount(){return this._views.length}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();function n1(t){return t===void 0||typeof t==`string`||t instanceof Element?{hostElement:t}:t}function Hs(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}function kr(t,n){let e=le();if(pn(e,ba(),n)){let a=Xe(),o=ur();if($d(o,a,e,t,n))_i(o)&&s0(e,o.index);else{let s=Cn(o,e);c0(e[et],s,null,o.value,t,n,null)}}return kr}function te(t,n,e,i){let a=le();if(pn(a,ba(),n)){Xe();xk(ur(),a,t,n,e,i)}return te}var qh=class{destroy(n){}updateValue(n,e){}swap(n,e){let i=Math.min(n,e),a=Math.max(n,e),o=this.detach(a);if(a-i>1){let r=this.detach(i);this.attach(i,o),this.attach(a,r)}else this.attach(i,o)}move(n,e){this.attach(e,this.detach(n))}};function hh(t,n,e,i,a){return t===e&&Object.is(n,i)?1:Object.is(a(t,n),a(e,i))?-1:0}function i1(t,n,e,i){let a,o,r=0,s=t.length-1;if(Array.isArray(n)){ge(i);let h=n.length-1;for(ge(null);r<=s&&r<=h;){let p=t.at(r),g=n[r],v=hh(r,p,r,g,e);if(v!==0){v<0&&t.updateValue(r,g),r++;continue}let b=t.at(s),y=n[h],E=hh(s,b,h,y,e);if(E!==0){E<0&&t.updateValue(s,y),s--,h--;continue}let P=e(r,p),ae=e(s,b),z=e(r,g);if(Object.is(z,ae)){let x=e(h,y);Object.is(x,P)?(t.swap(r,s),t.updateValue(s,y),h--,s--):t.move(s,r),t.updateValue(r,g),r++;continue}if(a??=new Rd,o??=ty(t,r,s,e),Yh(t,a,r,z))t.updateValue(r,g),r++,s++;else if(o.has(z))a.set(P,t.detach(r)),s--;else{let x=t.create(r,n[r]);t.attach(r,x),r++,s++}}for(;r<=h;)ey(t,a,e,r,n[r]),r++}else if(n!=null){ge(i);let h=n[Symbol.iterator]();ge(null);let p=h.next();for(;!p.done&&r<=s;){let g=t.at(r),v=p.value,b=hh(r,g,r,v,e);if(b!==0)b<0&&t.updateValue(r,v),r++,p=h.next();else{a??=new Rd,o??=ty(t,r,s,e);let y=e(r,v);if(Yh(t,a,r,y))t.updateValue(r,v),r++,s++,p=h.next();else if(!o.has(y))t.attach(r,t.create(r,v)),r++,s++,p=h.next();else{let E=e(r,g);a.set(E,t.detach(r)),s--}}}for(;!p.done;)ey(t,a,e,t.length,p.value),p=h.next()}for(;r<=s;)t.destroy(t.detach(s--));a?.forEach(h=>{t.destroy(h)})}function Yh(t,n,e,i){return n!==void 0&&n.has(i)?(t.attach(e,n.get(i)),n.delete(i),!0):!1}function ey(t,n,e,i,a){if(Yh(t,n,i,e(i,a)))t.updateValue(i,a);else{let o=t.create(i,a);t.attach(i,o)}}function ty(t,n,e,i){let a=new Set;for(let o=n;o<=e;o++)a.add(i(o,t.at(o)));return a}var Rd=class{kvMap=new Map;_vMap=void 0;has(n){return this.kvMap.has(n)}delete(n){if(!this.has(n))return!1;let e=this.kvMap.get(n);return this._vMap!==void 0&&this._vMap.has(e)?(this.kvMap.set(n,this._vMap.get(e)),this._vMap.delete(e)):this.kvMap.delete(n),!0}get(n){return this.kvMap.get(n)}set(n,e){if(this.kvMap.has(n)){let i=this.kvMap.get(n);this._vMap===void 0&&(this._vMap=new Map);let a=this._vMap;for(;a.has(i);)i=a.get(i);a.set(i,e)}else this.kvMap.set(n,e)}forEach(n){for(let[e,i]of this.kvMap)if(n(i,e),this._vMap!==void 0){let a=this._vMap;for(;a.has(i);)i=a.get(i),n(i,e)}}};function D(t,n,e,i,a,o,r,s){Ci(`NgControlFlow`);let c=le(),h=Xe();return xr(c,h,t,n,e,i,a,Sn(h.consts,o),256,r,s),qf}function qf(t,n,e,i,a,o,r,s){return Ci(`NgControlFlow`),a1(t,n,e,i,a,o,r,s),qf}function a1(t,n,e,i,a,o,r,s){let c=le(),h=Xe();xr(c,h,t,n,e,i,a,Sn(h.consts,o),512,r,s)}function I(t,n){Ci(`NgControlFlow`);let e=le(),i=ba(),a=e[i]!==pt?e[i]:-1,o=a!==-1?Pd(e,at+a):void 0,r=0;if(pn(e,i,t)){let s=ge(null);try{if(o!==void 0&&y0(o,r),t!==-1){let c=at+t,h=Pd(e,c),p=Zh(e[ve],c),g=C0(h,p,e);tc(h,ec(e,p,n,{dehydratedView:g}),r,vr(p,g))}}finally{ge(s)}}else if(o!==void 0){let s=_0(o,r);s!==void 0&&(s[_t]=n)}}var Kh=class{lContainer;$implicit;$index;constructor(n,e,i){this.lContainer=n,this.$implicit=e,this.$index=i}get $count(){return this.lContainer.length-yt}};function Co(t){return t}function Pn(t,n){return n}var Qh=class{hasEmptyBlock;trackByFn;liveCollection;constructor(n,e,i){this.hasEmptyBlock=n,this.trackByFn=e,this.liveCollection=i}};function ue(t,n,e,i,a,o,r,s,c,h,p,g,v){Ci(`NgControlFlow`);let b=le(),y=Xe(),E=c!==void 0,P=le(),z=new Qh(E,s?r.bind(P[Qt][_t]):r);P[at+t]=z,xr(b,y,t+1,n,e,i,a,Sn(y.consts,o),256),E&&xr(b,y,t+2,c,h,p,g,Sn(y.consts,v),512)}var Xh=class extends qh{lContainer;hostLView;templateTNode;operationsCounter=void 0;needsIndexUpdate=!1;constructor(n,e,i){super(),this.lContainer=n,this.hostLView=e,this.templateTNode=i}get length(){return this.lContainer.length-yt}at(n){return this.getLView(n)[_t].$implicit}attach(n,e){let i=e[io];this.needsIndexUpdate||=n!==this.length,tc(this.lContainer,e,n,vr(this.templateTNode,i)),o1(this.lContainer,n)}detach(n){return this.needsIndexUpdate||=n!==this.length-1,r1(this.lContainer,n),s1(this.lContainer,n)}create(n,e){let i=Dd(this.lContainer,this.templateTNode.tView.ssrId);return ec(this.hostLView,this.templateTNode,new Kh(this.lContainer,e,n),{dehydratedView:i})}destroy(n){zd(n[ve],n)}updateValue(n,e){this.getLView(n)[_t].$implicit=e}reset(){this.needsIndexUpdate=!1}updateIndexes(){if(this.needsIndexUpdate)for(let n=0;n<this.length;n++)this.getLView(n)[_t].$index=n}getLView(n){return c1(this.lContainer,n)}};function me(t){let n=ge(null),e=Xt();try{let i=le(),a=i[ve],o=i[e],r=e+1,s=Pd(i,r);if(o.liveCollection===void 0)o.liveCollection=new Xh(s,i,Zh(a,r));else o.liveCollection.reset();let c=o.liveCollection;if(i1(c,t,o.trackByFn,n),c.updateIndexes(),o.hasEmptyBlock){let h=ba(),p=c.length===0;if(pn(i,h,p)){let g=e+2,v=Pd(i,g);if(p){let b=Zh(a,g),y=C0(v,b,i);tc(v,ec(i,b,void 0,{dehydratedView:y}),0,vr(b,y))}else a.firstUpdatePass&&Gk(v),y0(v,0)}}}finally{ge(n)}}function Pd(t,n){return t[n]}function o1(t,n){if(t.length<=yt)return;let i=t[yt+n];i&&XI(i)}function r1(t,n){if(t.length<=yt)return;let i=t[yt+n];i&&QI(i)}function s1(t,n){return Ws(t,n)}function c1(t,n){return _0(t,n)}function Zh(t,n){return ed(t,n)}function S(t,n,e){let i=le();if(pn(i,ba(),n)){Xe();bk(ur(),i,t,n,i[et],e)}return S}function Jh(t,n,e,i,a){$d(n,t,e,a?`class`:`style`,i)}function l(t,n,e,i){let a=le(),o=a[ve],r=t+at,s=o.firstCreatePass?Pf(r,a,2,n,If,ad(),e,i):o.data[r];if(_i(s)){let c=a[Qn].tracingService;if(c&&c.componentCreate){let h=o.data[s.directiveStart+s.componentOffset];return c.componentCreate(E0(h),()=>(ny(t,n,a,s,i),l))}}return ny(t,n,a,s,i),l}function ny(t,n,e,i,a){if(kf(i,e,t,n,Y0),lr(i)){let o=e[ve];Ud(o,e,i),lf(o,i,e)}a!=null&&Js(e,i)}function d(){let t=Xe(),e=Mf(Mt());return t.firstCreatePass&&Ff(t,e),qp(e)&&Yp(),Gp(),e.classesWithoutHost!=null&&LD(e)&&Jh(t,e,le(),e.classesWithoutHost,!0),e.stylesWithoutHost!=null&&VD(e)&&Jh(t,e,le(),e.stylesWithoutHost,!1),d}function H(t,n,e,i){return l(t,n,e,i),d(),H}function ct(t,n,e,i){let a=le(),o=a[ve],r=t+at,s=o.firstCreatePass?hM(r,o,2,n,e,i):o.data[r];return kf(s,a,t,n,Y0),i!=null&&Js(a,s),ct}function ht(){return qp(Mf(Mt()))&&Yp(),Gp(),ht}function Wt(t,n,e,i){return ct(t,n,e,i),ht(),Wt}var Y0=(t,n,e,i,a)=>(Ls(!0),Uy(n[et],i,nh()));function Yf(t,n,e){let i=le(),a=i[ve],o=t+at,r=a.firstCreatePass?Pf(o,i,8,`ng-container`,If,ad(),n,e):a.data[o];if(kf(r,i,t,`ng-container`,l1),lr(r)){let s=i[ve];Ud(s,i,r),lf(s,r,i)}return e!=null&&Js(i,r),Yf}function Kf(){let t=Xe(),e=Mf(Mt());return t.firstCreatePass&&Ff(t,e),Kf}function wa(t,n,e){return Yf(t,n,e),Kf(),wa}var l1=(t,n,e,i,a)=>(Ls(!0),kI(n[et],``));function q(){return le()}function qt(t,n,e){let i=le();if(pn(i,ba(),n)){Xe();r0(ur(),i,t,n,i[et],e)}return qt}var js=void 0;function d1(t){let n=Math.floor(Math.abs(t)),e=t.toString().replace(/^[^.]*\.?/,``).length;return n===1&&e===0?1:5}var u1=[`en`,[[`a`,`p`],[`AM`,`PM`]],[[`AM`,`PM`]],[[`S`,`M`,`T`,`W`,`T`,`F`,`S`],[`Sun`,`Mon`,`Tue`,`Wed`,`Thu`,`Fri`,`Sat`],[`Sunday`,`Monday`,`Tuesday`,`Wednesday`,`Thursday`,`Friday`,`Saturday`],[`Su`,`Mo`,`Tu`,`We`,`Th`,`Fr`,`Sa`]],js,[[`J`,`F`,`M`,`A`,`M`,`J`,`J`,`A`,`S`,`O`,`N`,`D`],[`Jan`,`Feb`,`Mar`,`Apr`,`May`,`Jun`,`Jul`,`Aug`,`Sep`,`Oct`,`Nov`,`Dec`],[`January`,`February`,`March`,`April`,`May`,`June`,`July`,`August`,`September`,`October`,`November`,`December`]],js,[[`B`,`A`],[`BC`,`AD`],[`Before Christ`,`Anno Domini`]],0,[6,0],[`M/d/yy`,`MMM d, y`,`MMMM d, y`,`EEEE, MMMM d, y`],[`h:mm a`,`h:mm:ss a`,`h:mm:ss a z`,`h:mm:ss a zzzz`],[`{1}, {0}`,js,js,js],[`.`,`,`,`;`,`%`,`+`,`-`,`E`,`×`,`‰`,`∞`,`NaN`,`:`],[`#,##0.###`,`#,##0%`,`¤#,##0.00`,`#E0`],`USD`,`$`,`US Dollar`,{},`ltr`,d1];var fh=Object.create(null);function fn(t){let n=m1(t),e=iy(n);if(e)return e;let i=n.split(`-`)[0];if(e=iy(i),e)return e;if(i===`en`)return u1;throw new G(701,!1)}function iy(t){if(!(t in fh)){let n=bi.ng&&bi.ng.common&&bi.ng.common.locales&&bi.ng.common.locales[t];return n!==void 0&&(fh[t]=n),n}return fh[t]}var Et={LocaleId:0,DayPeriodsFormat:1,DayPeriodsStandalone:2,DaysFormat:3,DaysStandalone:4,MonthsFormat:5,MonthsStandalone:6,Eras:7,FirstDayOfWeek:8,WeekendRange:9,DateFormat:10,TimeFormat:11,DateTimeFormat:12,NumberSymbols:13,NumberFormats:14,CurrencyCode:15,CurrencySymbol:16,CurrencyName:17,Currencies:18,Directionality:19,PluralCase:20,ExtraData:21};function m1(t){return t.toLowerCase().replace(/_/g,`-`)}var ac=`en-US`;function K0(t){typeof t==`string`&&t.toLowerCase().replace(/_/g,`-`)}function C(t,n,e){let i=le(),a=Xe(),o=Mt();return h1(a,i,i[et],o,t,n,e),C}function Mr(t,n,e){let i=le(),a=Xe(),o=Mt();return(o.type&3||e)&&Of(o,a,i,e,i[et],t,n,fo(o,i,n)),Mr}function h1(t,n,e,i,a,o,r){let s=!0,c=null;if((i.type&3||r)&&(c??=fo(i,n,o),Of(i,t,n,r,e,a,o,c)&&(s=!1)),s){let h=i.outputs?.[a],p=i.hostDirectiveOutputs?.[a];if(p&&p.length)for(let g=0;g<p.length;g+=2){let v=p[g],b=p[g+1];c??=fo(i,n,o),kd(i,n,v,b,a,c)}if(h&&h.length)for(let g of h)c??=fo(i,n,o),kd(i,n,g,a,a,c)}}function _(t=1){return d_(t)}function f1(t,n){let e=null,i=VI(t);for(let a=0;a<n.length;a++){let o=n[a];if(o===`*`){e=a;continue}if(i===null?Yy(t,o,!0):zI(i,o))return a}return e}function Pe(t){let n=le()[Qt][Ut];if(!n.projection){let i=n.projection=Lv(t?t.length:1,null),a=i.slice(),o=n.child;for(;o!==null;){if(o.type!==128){let r=t?f1(o,t):0;r!==null&&(a[r]?a[r].projectionNext=o:i[r]=o,a[r]=o)}o=o.next}}}function he(t,n=0,e,i,a,o){let r=le(),s=Xe(),c=i?t+1:null;c!==null&&xr(r,s,c,i,a,o,null,e);let h=_o(s,at+t,16,null,e||null);h.projection===null&&(h.projection=n),Xp();let g=!r[io]||Wp();r[Qt][Ut].projection[h.projection]===null&&c!==null?g1(r,s,c):g&&!Ld(h)&&sk(s,r,h)}function g1(t,n,e){let i=at+e,a=n.data[i],o=t[i],r=Dd(o,a.tView.ssrId);tc(o,ec(t,a,void 0,{dehydratedView:r}),0,vr(a,r))}function jt(t,n,e,i){return F0(t,n,e,i),jt}function Ze(t,n,e){return P0(t,n,e),Ze}function Z(t){let n=le(),e=Xe(),i=sd();Fs(i+1);let a=Vf(e,i);if(t.dirty&&Yv(n)===((a.metadata.flags&2)===2)){if(a.matches===null)t.reset([]);else{let o=V0(n,i);t.reset(o,wy),t.notifyOnChanges()}return!0}return!1}function J(){return Lf(le(),sd())}function qd(t,n,e,i,a){return j0(n,F0(t,e,i,a)),qd}function Yd(t,n,e,i){return j0(t,P0(n,e,i)),Yd}function Kd(t=1){Fs(sd()+t)}function ft(t){return Os(Zp(),at+t)}function gd(t,n){return t<<17|n<<2}function vo(t){return t>>17&32767}function b1(t){return(t&2)==2}function v1(t,n){return t&131071|n<<17}function ef(t){return t|2}function Cr(t){return(t&131068)>>2}function gh(t,n){return t&-131069|n<<2}function _1(t){return(t&1)===1}function tf(t){return t|1}function y1(t,n,e,i,a,o){let r=o?n.classBindings:n.styleBindings,s=vo(r),c=Cr(r);t[i]=e;let h=!1,p;if(Array.isArray(e)){let g=e;p=g[1],(p===null||ir(g,p)>0)&&(h=!0)}else p=e;if(a)if(c!==0){let v=vo(t[s+1]);t[i+1]=gd(v,s),v!==0&&(t[v+1]=gh(t[v+1],i)),t[s+1]=v1(t[s+1],i)}else t[i+1]=gd(s,0),s!==0&&(t[s+1]=gh(t[s+1],i)),s=i;else t[i+1]=gd(c,0),s===0?s=i:t[c+1]=gh(t[c+1],i),c=i;h&&(t[i+1]=ef(t[i+1])),ay(t,p,i,!0),ay(t,p,i,!1),x1(n,p,t,i,o),r=gd(s,c),o?n.classBindings=r:n.styleBindings=r}function x1(t,n,e,i,a){let o=a?t.residualClasses:t.residualStyles;o!=null&&typeof n==`string`&&ir(o,n)>=0&&(e[i+1]=tf(e[i+1]))}function ay(t,n,e,i){let a=t[e+1],o=n===null,r=i?vo(a):Cr(a),s=!1;for(;r!==0&&(s===!1||o);){let c=t[r],h=t[r+1];C1(c,n)&&(s=!0,t[r+1]=i?tf(h):ef(h)),r=i?vo(h):Cr(h)}s&&(t[e+1]=i?ef(a):tf(a))}function C1(t,n){return t===null||n==null||(Array.isArray(t)?t[1]:t)===n?!0:Array.isArray(t)&&typeof n==`string`?ir(t,n)>=0:!1}var Jn={textEnd:0,key:0,keyEnd:0,value:0,valueEnd:0};function S1(t){return t.substring(Jn.key,Jn.keyEnd)}function w1(t){return E1(t),Q0(t,X0(t,0,Jn.textEnd))}function Q0(t,n){let e=Jn.textEnd;return e===n?-1:(n=Jn.keyEnd=D1(t,Jn.key=n,e),X0(t,n,e))}function E1(t){Jn.key=0,Jn.keyEnd=0,Jn.value=0,Jn.valueEnd=0,Jn.textEnd=t.length}function X0(t,n,e){for(;n<e&&t.charCodeAt(n)<=32;)n++;return n}function D1(t,n,e){for(;n<e&&t.charCodeAt(n)>32;)n++;return n}function gn(t,n,e){return Z0(t,n,e,!1),gn}function Y(t,n){return Z0(t,n,null,!0),Y}function tt(t){k1(R1,I1,t,!0)}function I1(t,n){for(let e=w1(n);e>=0;e=Q0(n,e))Ql(t,S1(n),!0)}function Z0(t,n,e,i){let a=le(),o=Xe(),r=Hi(2);if(o.firstUpdatePass&&ex(o,t,r,i),n!==pt&&pn(a,r,n)){let s=o.data[Xt()];tx(o,s,a,a[et],t,a[r+1]=F1(n,e),i,r)}}function k1(t,n,e,i){let a=Xe(),o=Hi(2);a.firstUpdatePass&&ex(a,null,o,i);let r=le();if(e!==pt&&pn(r,o,e)){let s=a.data[Xt()];if(nx(s,i)&&!J0(a,o)){let c=i?s.classesWithoutHost:s.stylesWithoutHost;c!==null&&(e=$l(c,e||``)),Jh(a,s,r,e,i)}else P1(a,s,r,r[et],r[o+1],r[o+1]=O1(t,n,e),i,o)}}function J0(t,n){return n>=t.expandoStartIndex}function ex(t,n,e,i){let a=t.data;if(a[e+1]===null){let o=a[Xt()],r=J0(t,e);nx(o,i)&&n===null&&!r&&(n=!1),n=M1(a,o,n,i),y1(a,o,n,e,r,i)}}function M1(t,n,e,i){let a=r_(t),o=i?n.residualClasses:n.residualStyles;if(a===null)(i?n.classBindings:n.styleBindings)===0&&(e=bh(null,t,n,e,i),e=Ys(e,n.attrs,i),o=null);else{let r=n.directiveStylingLast;if(r===-1||t[r]!==a)if(e=bh(a,t,n,e,i),o===null){let c=T1(t,n,i);c!==void 0&&Array.isArray(c)&&(c=bh(null,t,n,c[1],i),c=Ys(c,n.attrs,i),N1(t,n,i,c))}else o=A1(t,n,i)}return o!==void 0&&(i?n.residualClasses=o:n.residualStyles=o),e}function T1(t,n,e){let i=e?n.classBindings:n.styleBindings;if(Cr(i)!==0)return t[vo(i)]}function N1(t,n,e,i){let a=e?n.classBindings:n.styleBindings;t[vo(a)]=i}function A1(t,n,e){let i,a=n.directiveEnd;for(let o=1+n.directiveStylingLast;o<a;o++){let r=t[o].hostAttrs;i=Ys(i,r,e)}return Ys(i,n.attrs,e)}function bh(t,n,e,i,a){let o=null,r=e.directiveEnd,s=e.directiveStylingLast;for(s===-1?s=e.directiveStart:s++;s<r&&(o=n[s],i=Ys(i,o.hostAttrs,a),o!==t);)s++;return t!==null&&(e.directiveStylingLast=s),i}function Ys(t,n,e){let i=e?1:2,a=-1;if(n!==null)for(let o=0;o<n.length;o++){let r=n[o];typeof r==`number`?a=r:a===i&&(Array.isArray(t)||(t=t===void 0?[]:[``,t]),Ql(t,r,e?!0:n[++o]))}return t===void 0?null:t}function O1(t,n,e){if(e==null||e===``)return Kt;let i=[],a=ti(e);if(Array.isArray(a))for(let o=0;o<a.length;o++)t(i,a[o],!0);else if(a instanceof Set)for(let o of a)t(i,o,!0);else if(typeof a==`object`)for(let o in a)Object.hasOwn(a,o)&&t(i,o,a[o]);else typeof a==`string`&&n(i,a);return i}function R1(t,n,e){let i=String(n);i!==``&&!i.includes(` `)&&Ql(t,i,e)}function P1(t,n,e,i,a,o,r,s){a===pt&&(a=Kt);let c=0,h=0,p=0<a.length?a[0]:null,g=0<o.length?o[0]:null;for(;p!==null||g!==null;){let v=c<a.length?a[c+1]:void 0,b=h<o.length?o[h+1]:void 0,y=null,E;p===g?(c+=2,h+=2,v!==b&&(y=g,E=b)):g===null||p!==null&&p<g?(c+=2,y=p):(h+=2,y=g,E=b),y!==null&&tx(t,n,e,i,y,E,r,s),p=c<a.length?a[c]:null,g=h<o.length?o[h]:null}}function tx(t,n,e,i,a,o,r,s){if(!(n.type&3))return;let c=t.data,h=c[s+1];if(!Fd(_1(h)?oy(c,n,e,a,Cr(h),r):void 0)){Fd(o)||b1(h)&&(o=oy(c,null,e,a,s,r));lk(i,r,jp(Xt(),e),a,o)}}function oy(t,n,e,i,a,o){let r=n===null,s;for(;a>0;){let c=t[a],h=Array.isArray(c),p=h?c[1]:c,g=p===null,v=e[a+1];v===pt&&(v=g?Kt:void 0);let b=g?Xl(v,i):p===i?v:void 0;if(h&&!Fd(b)&&(b=Xl(c,i)),Fd(b)&&(s=b,r))return s;let y=t[a+1];a=r?vo(y):Cr(y)}if(n!==null){let c=o?n.residualClasses:n.residualStyles;c!=null&&(s=Xl(c,i))}return s}function Fd(t){return t!==void 0}function F1(t,n){return t==null||t===``||(typeof n==`string`?t=ti(t)+n:typeof t==`object`&&(t=Es(ti(t)))),t}function nx(t,n){return(t.flags&(n?8:16))!==0}function u(t,n=``){let e=le(),i=Xe(),a=t+at,o=i.firstCreatePass?_o(i,a,1,n,null):i.data[a],r=L1(i,e,o,n);e[a]=r,dd()&&Cf(i,e,r,o),uo(o,!1)}var L1=(t,n,e,i)=>(Ls(!0),DI(n[et],i));function V1(t,n,e,i=``){return pn(t,ba(),e)?n+$e(e)+i:pt}function B1(t,n,e,i,a,o=``){let s=_r(t,mo(),e,a);return Hi(2),s?n+$e(e)+i+$e(a)+o:pt}function j1(t,n,e,i,a,o,r,s=``){let h=D0(t,mo(),e,a,r);return Hi(3),h?n+$e(e)+i+$e(a)+o+$e(r)+s:pt}function z1(t,n,e,i,a,o,r,s,c,h=``){let g=Wd(t,mo(),e,a,r,c);return Hi(4),g?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(c)+h:pt}function H1(t,n,e,i,a,o,r,s,c,h,p,g=``){let v=mo(),b=Wd(t,v,e,a,r,c);return b=pn(t,v+4,p)||b,Hi(5),b?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(c)+h+$e(p)+g:pt}function U1(t,n,e,i,a,o,r,s,c,h,p,g,v,b=``){let y=mo(),E=Wd(t,y,e,a,r,c);return E=_r(t,y+4,p,v)||E,Hi(6),E?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(c)+h+$e(p)+g+$e(v)+b:pt}function $1(t,n,e,i,a,o,r,s,c,h,p,g,v,b,y,E=``){let P=mo(),ae=Wd(t,P,e,a,r,c);return ae=D0(t,P+4,p,v,y)||ae,Hi(7),ae?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(c)+h+$e(p)+g+$e(v)+b+$e(y)+E:pt}function w(t){return j(``,t),w}function j(t,n,e){let i=le(),a=V1(i,t,n,e);return a!==pt&&So(i,Xt(),a),j}function Ae(t,n,e,i,a){let o=le(),r=B1(o,t,n,e,i,a);return r!==pt&&So(o,Xt(),r),Ae}function Ot(t,n,e,i,a,o,r){let s=le(),c=j1(s,t,n,e,i,a,o,r);return c!==pt&&So(s,Xt(),c),Ot}function Si(t,n,e,i,a,o,r,s,c){let h=le(),p=z1(h,t,n,e,i,a,o,r,s,c);return p!==pt&&So(h,Xt(),p),Si}function oc(t,n,e,i,a,o,r,s,c,h,p){let g=le(),v=H1(g,t,n,e,i,a,o,r,s,c,h,p);return v!==pt&&So(g,Xt(),v),oc}function Qd(t,n,e,i,a,o,r,s,c,h,p,g,v){let b=le(),y=U1(b,t,n,e,i,a,o,r,s,c,h,p,g,v);return y!==pt&&So(b,Xt(),y),Qd}function Xd(t,n,e,i,a,o,r,s,c,h,p,g,v,b,y){let E=le(),P=$1(E,t,n,e,i,a,o,r,s,c,h,p,g,v,b,y);return P!==pt&&So(E,Xt(),P),Xd}function So(t,n,e){let i=jp(n,t);II(t[et],i,e)}var ix={};function Zd(t){Ci(`NgLet`);let n=Xe(),e=le(),i=t+at;return uo(_o(n,i,128,null,null),!1),Rs(n,e,i,ix),Zd}function Qf(t){return Rs(Xe(),le(),Xt(),t),t}function Xf(t){let e=Os(Zp(),at+t);if(e===ix)throw new G(314,!1);return e}function ry(t,n,e){let i=Xe();i.firstCreatePass&&ax(n,i.data,i.blueprint,Xn(t),e)}function ax(t,n,e,i,a){if(t=Ft(t),Array.isArray(t))for(let o=0;o<t.length;o++)ax(t[o],n,e,i,a);else{let o=Xe(),r=le(),s=Mt(),c=Ja(t)?t:Ft(t.provide),h=Rp(t),p=s.providerIndexes&1048575,g=s.directiveStart,v=s.providerIndexes>>20;if(Ja(t)||!t.multi){let b=new go(h,a,He,null),y=_h(c,n,a?p:p+v,g);y===-1?(Ch(Ed(s,r),o,c),vh(o,t,n.length),n.push(c),s.directiveStart++,s.directiveEnd++,a&&(s.providerIndexes+=1048576),e.push(b),r.push(b)):(e[y]=b,r[y]=b)}else{let b=_h(c,n,p+v,g),y=_h(c,n,p,p+v),E=b>=0&&e[b],P=y>=0&&e[y];if(a&&!P||!a&&!E){Ch(Ed(s,r),o,c);let ae=q1(a?W1:G1,e.length,a,i,h,t);!a&&P&&(e[y].providerFactory=ae),vh(o,t,n.length,0),n.push(c),s.directiveStart++,s.directiveEnd++,a&&(s.providerIndexes+=1048576),e.push(ae),r.push(ae)}else{let ae=ox(e[a?y:b],h,!a&&i);vh(o,t,b>-1?b:y,ae)}!a&&i&&P&&e[y].componentProviders++}}}function vh(t,n,e,i){let a=Ja(n),o=Uv(n);if(a||o){let c=(o?Ft(n.useClass):n).prototype.ngOnDestroy;if(c){let h=t.destroyHooks||(t.destroyHooks=[]);if(!a&&n.multi){let p=h.indexOf(e);p===-1?h.push(e,[i,c]):h[p+1].push(i,c)}else h.push(e,c)}}}function ox(t,n,e){return e&&t.componentProviders++,t.multi.push(n)-1}function _h(t,n,e,i){for(let a=e;a<i;a++)if(n[a]===t)return a;return-1}function G1(t,n,e,i,a){return nf(this.multi,[])}function W1(t,n,e,i,a){let o=this.multi,r;if(this.providerFactory){let s=this.providerFactory.componentProviders,c=Us(i,i[ve],this.providerFactory.index,a);r=c.slice(0,s),nf(o,r);for(let h=s;h<c.length;h++)r.push(c[h])}else r=[],nf(o,r);return r}function nf(t,n){for(let e=0;e<t.length;e++){let i=t[e];n.push(i())}return n}function q1(t,n,e,i,a,o){let r=new go(t,e,He,null);return r.multi=[],r.index=n,r.componentProviders=0,ox(r,a,i&&!e),r}function Ge(t,n){return e=>{e.providersResolver=(i,a)=>ry(i,a?a(t):t,!1),n&&(e.viewProvidersResolver=(i,a)=>ry(i,a?a(n):n,!0))}}function rt(t,n){let e=od()+t,i=le();return i[e]===pt?Af(i,e,n()):Qk(i,e)}function rx(t,n){let e=t[n];return e===pt?void 0:e}function Y1(t,n,e,i,a,o){let r=n+e;return pn(t,r,a)?Af(t,r+1,o?i.call(o,a):i(a)):rx(t,r+1)}function K1(t,n,e,i,a,o,r){let s=n+e;return _r(t,s,a,o)?Af(t,s+2,r?i.call(r,a,o):i(a,o)):rx(t,s+2)}function U(t,n){let e=Xe(),i,a=t+at;e.firstCreatePass?(i=Q1(n,e.pipeRegistry),e.data[a]=i,i.onDestroy&&(e.destroyHooks??=[]).push(a,i.onDestroy)):i=e.data[a];let o=i.factory||(i.factory=ma(i.type,!0)),s=on(He);try{let c=wd(!1),h=o();return wd(c),Rs(e,le(),a,h),h}finally{on(s)}}function Q1(t,n){if(n)for(let e=n.length-1;e>=0;e--){let i=n[e];if(t===i.name)return i}}function ye(t,n,e){let i=t+at,a=le(),o=Os(a,i);return sx(a,i)?Y1(a,od(),n,o.transform,e,o):o.transform(e)}function ke(t,n,e,i){let a=t+at,o=le(),r=Os(o,a);return sx(o,a)?K1(o,od(),n,r.transform,e,i,r):r.transform(e,i)}function sx(t,n){return t[ve].data[n].pure}function rc(t,n){return Gd(t,n)}var cx=(()=>{class t{applicationErrorHandler=f(Ui);appRef=f(Rn);taskService=f(po);ngZone=f(X);zonelessEnabled=f(Bs);tracing=f(ii,{optional:!0});zoneIsDefined=typeof Zone<`u`&&!!Zone.root.run;schedulerTickApplyArgs=[{data:{__scheduler_tick__:!0}}];subscriptions=new De;angularZoneId=this.zoneIsDefined?this.ngZone._inner?.get(Ss):null;scheduleInRootZone=!this.zonelessEnabled&&this.zoneIsDefined&&(f(ch,{optional:!0})??!1);cancelScheduledCallback=null;useMicrotaskScheduler=!1;runningTick=!1;pendingRenderTaskId=null;constructor(){this.subscriptions.add(this.appRef.afterTick.subscribe(()=>{let e=this.taskService.add();if(!this.runningTick&&(this.cleanup(),!this.zonelessEnabled||this.appRef.includeAllTestViews)){this.taskService.remove(e);return}this.switchToMicrotaskScheduler(),this.taskService.remove(e)})),this.subscriptions.add(this.ngZone.onUnstable.subscribe(()=>{this.runningTick||this.cleanup()}))}switchToMicrotaskScheduler(){this.ngZone.runOutsideAngular(()=>{let e=this.taskService.add();this.useMicrotaskScheduler=!0,queueMicrotask(()=>{this.useMicrotaskScheduler=!1,this.taskService.remove(e)})})}notify(e){if(!this.zonelessEnabled&&e===5)return;switch(e){case 0:case 2:this.appRef.dirtyFlags|=2;break;case 3:case 4:case 5:case 1:this.appRef.dirtyFlags|=4;break;case 6:this.appRef.dirtyFlags|=2;break;case 12:this.appRef.dirtyFlags|=16;break;case 13:this.appRef.dirtyFlags|=2;break;case 11:break;default:this.appRef.dirtyFlags|=8}if(this.appRef.tracingSnapshot=this.tracing?.snapshot(this.appRef.tracingSnapshot)??null,!this.shouldScheduleTick())return;let i=this.useMicrotaskScheduler?h_:ah;this.pendingRenderTaskId=this.taskService.add(),this.scheduleInRootZone?this.cancelScheduledCallback=Zone.root.run(()=>i(()=>this.tick())):this.cancelScheduledCallback=this.ngZone.runOutsideAngular(()=>i(()=>this.tick()))}shouldScheduleTick(){return!(this.appRef.destroyed||this.pendingRenderTaskId!==null||this.runningTick||this.appRef._runningTick||!this.zonelessEnabled&&this.zoneIsDefined&&Zone.current.get(Ss+this.angularZoneId))}tick(){if(this.runningTick||this.appRef.destroyed)return;if(this.appRef.dirtyFlags===0){this.cleanup();return}!this.zonelessEnabled&&this.appRef.dirtyFlags&7&&(this.appRef.dirtyFlags|=1);let e=this.taskService.add();try{this.ngZone.run(()=>{this.runningTick=!0,this.appRef._tick()},void 0,this.schedulerTickApplyArgs)}catch(i){this.applicationErrorHandler(i)}finally{this.taskService.remove(e),this.cleanup()}}ngOnDestroy(){this.subscriptions.unsubscribe(),this.cleanup()}cleanup(){if(this.runningTick=!1,this.cancelScheduledCallback?.(),this.cancelScheduledCallback=null,this.pendingRenderTaskId!==null){let e=this.pendingRenderTaskId;this.pendingRenderTaskId=null,this.taskService.remove(e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();function Zf(){return Ci(`NgZoneless`),rn([...Jf(),[]])}function Jf(){return[{provide:gi,useExisting:cx},{provide:X,useClass:ws},{provide:Bs,useValue:!0}]}function X1(){return typeof $localize<`u`&&$localize.locale||ac}var Tr=new k(``,{factory:()=>f(Tr,{optional:!0,skipSelf:!0})||X1()});var sc=class{destroyed=!1;listeners=null;errorHandler=f(Tn,{optional:!0});isEmitting=!1;hasNullListeners=!1;destroyRef=f(Ne);constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.listeners=null})}subscribe(n){if(this.destroyed)throw new G(953,!1);return(this.listeners??=[]).push(n),{unsubscribe:()=>{let e=this.listeners?this.listeners.indexOf(n):-1;e>-1&&(this.isEmitting?(this.hasNullListeners=!0,this.listeners[e]=null):this.listeners.splice(e,1))}}}emit(n){if(this.destroyed){console.warn(Bi(953,!1));return}if(this.listeners===null)return;this.isEmitting=!0;let e=ge(null);try{for(let i of this.listeners)try{i!==null&&i(n)}catch(a){this.errorHandler?.handleError(a)}}finally{this.hasNullListeners&&(this.hasNullListeners=!1,this.listeners&&Z1(this.listeners)),ge(e),this.isEmitting=!1}}};function Z1(t){let n=t.length-1;for(;n>-1;)t[n]===null&&t.splice(n,1),n--}function ne(t,n){return hs(t,n?.equal)}function nt(t){return Qb(t)}(class t extends Error{_brand;constructor(n){super(n)}static IDLE=new t(`IDLE`);static LOADING=new t(`LOADING`)});function gx(t,n){let e=Object.create(af);e.value=t,e.transformFn=n?.transform;function i(){if(Oi(e),e.value===Sr)throw new G(-950,null);return e.value}return i[Ct]=e,i}var Ki=class{attributeName;constructor(n){this.attributeName=n}__NG_ELEMENT_ID__=()=>rf(this.attributeName);toString(){return`HostAttributeToken ${this.attributeName}`}};var bx=(()=>{let t=new k(``);return t.__NG_ELEMENT_ID__=n=>{let e=Mt();if(e===null)throw new G(-204,!1);if(e.type&2)return e.value;if(n&8)return null;throw new G(-204,!1)},t})();function eu(t){return new sc}function dx(t,n){return gx(t,n)}function mT(t){return gx(Sr,t)}var zt=(dx.required=mT,dx);function vx(t,n){let e=Object.create(af),i=new sc;e.value=t;function a(){return Oi(e),ux(e.value),e.value}return a[Ct]=e,a.asReadonly=ud.bind(a),a.set=o=>{e.equal(e.value,o)||(ua(e,o),i.emit(o))},a.update=o=>{ux(e.value),a.set(o(e.value))},a.subscribe=i.subscribe.bind(i),a.destroyRef=i.destroyRef,a}function ux(t){if(t===Sr)throw new G(952,!1)}function mx(t,n){return vx(t,n)}function pT(t){return vx(Sr,t)}var _x=(mx.required=pT,mx);function px(t,n){return Bf(n)}function hT(t,n){return jf(n)}var lc=(px.required=hT,px);function hx(t,n){return Bf(n)}function fT(t,n){return jf(n)}var yx=(hx.required=fT,hx);var Fe=(()=>{class t{static __NG_ELEMENT_ID__=bT}return t})();function bT(t){return vT(Mt(),le(),(t&16)===16)}function vT(t,n,e){if(_i(t)&&!e){let i=On(t.index,n);return new Ca(i,i)}else if(t.type&175){let i=n[Qt];return new Ca(i,n)}return null}var tg=new k(``);var _T=new k(``);function cc(t){return!t.moduleRef}function yT(t){let n=cc(t)?t.r3Injector:t.moduleRef.injector,e=n.get(X);return e.run(()=>{cc(t)?t.r3Injector.resolveInjectorInitializers():t.moduleRef.resolveInjectorInitializers();let i=n.get(Ui),a;if(e.runOutsideAngular(()=>{a=e.onError.subscribe({next:i})}),cc(t)){let o=()=>n.destroy(),r=t.platformInjector.get(tg);r.add(o),n.onDestroy(()=>{a.unsubscribe(),r.delete(o)})}else{let o=()=>t.moduleRef.destroy(),r=t.platformInjector.get(tg);r.add(o),t.moduleRef.onDestroy(()=>{Hs(t.allPlatformModules,t.moduleRef),a.unsubscribe(),r.delete(o)})}return CT(i,e,()=>{let o=n.get(po),r=o.add(),s=n.get(Uf);return s.runInitializers(),s.donePromise.then(()=>{if(K0(n.get(Tr,ac)||ac),!n.get(_T,!0))return cc(t)?n.get(Rn):(t.allPlatformModules.push(t.moduleRef),t.moduleRef);if(cc(t)){let p=n.get(Rn);return t.rootComponent!==void 0&&p.bootstrap(t.rootComponent),p}else return xT?.(t.moduleRef,t.allPlatformModules),t.moduleRef}).finally(()=>{o.remove(r)})})})}var xT;function CT(t,n,e){try{let i=e();return xo(i)?i.catch(a=>{throw n.runOutsideAngular(()=>t(a)),a}):i}catch(i){throw n.runOutsideAngular(()=>t(i)),i}}var Jd=null;function ST(t=[],n){return se.create({name:n,providers:[{provide:Ts,useValue:`platform`},{provide:tg,useValue:new Set([()=>Jd=null])},...t]})}function wT(t=[]){if(Jd)return Jd;let n=ST(t);return Jd=n,q0(),ET(n),n}function ET(t){let n=t.get(md,null);or(t,()=>{n?.forEach(e=>e())})}function xx(t){let{rootComponent:n,appProviders:e,platformProviders:i,platformRef:a}=t;Ke(ze.BootstrapApplicationStart);try{let o=a?.injector??wT(i);return yT({r3Injector:new qs({providers:[Jf(),v_,...e||[]],parent:o,debugName:``,runEnvironmentInitializers:!1}).injector,platformInjector:o,rootComponent:n})}catch(o){return Promise.reject(o)}finally{Ke(ze.BootstrapApplicationEnd)}}function V(t){return typeof t==`boolean`?t:t!=null&&t!==`false`}function lt(t,n=NaN){return!isNaN(parseFloat(t))&&!isNaN(Number(t))?Number(t):n}var eg=Symbol(`NOT_SET`);var Cx=new Set;var DT=s(r({},Wo),{kind:`afterRenderEffectPhase`,consumerIsAlwaysLive:!0,consumerAllowSignalWrites:!0,value:eg,cleanup:null,consumerMarkedDirty(){if(this.sequence.impl.executing){if(this.sequence.lastPhase===null||this.sequence.lastPhase<this.phase)return;this.sequence.erroredOrDestroyed=!0}this.sequence.scheduler.notify(7)},phaseFn(t){if(this.sequence.lastPhase=this.phase,!this.dirty)return this.signal;if(this.dirty=!1,this.value!==eg&&!$o(this))return this.signal;try{for(let a of this.cleanup??Cx)a()}finally{this.cleanup?.clear()}let n=[];t!==void 0&&n.push(t),n.push(this.registerCleanupFn);let e=la(this),i;try{i=this.userFn.apply(null,n)}finally{$a(this,e)}return(this.value===eg||!this.equal(this.value,i))&&(this.value=i,this.version++),this.signal}});var ng=class extends $s{scheduler;lastPhase=null;nodes=[void 0,void 0,void 0,void 0];onDestroyFns=null;constructor(n,e,i,a,o,r=null,s){super(n,[void 0,void 0,void 0,void 0],i,!1,o.get(Ne),r),this.scheduler=a;for(let c of _f){let h=e[c];if(h===void 0)continue;let p=Object.create(DT);p.sequence=this,p.phase=c,p.userFn=h,p.dirty=!0,p.signal=()=>(Oi(p),p.value),p.signal[Ct]=p,p.registerCleanupFn=g=>(p.cleanup??=new Set).add(g),this.nodes[c]=p,this.hooks[c]=g=>p.phaseFn(g)}}afterRun(){super.afterRun(),this.lastPhase=null}destroy(){if(this.onDestroyFns!==null)for(let n of this.onDestroyFns)n();super.destroy();for(let n of this.nodes)if(n)try{for(let e of n.cleanup??Cx)e()}finally{da(n)}}};function ig(t,n){let e=n?.injector??f(se),i=e.get(gi),a=e.get(jd),o=e.get(ii,null,{optional:!0});a.impl??=e.get(yf);let r=t;typeof r==`function`&&(r={mixedReadWrite:t});let s=e.get(mr,null,{optional:!0}),c=new ng(a.impl,[r.earlyRead,r.write,r.mixedReadWrite,r.read],s?.view,i,e,o?.snapshot(null),void 0);return a.impl.register(c),c}function tu(t,n){let e=pa(t),i=n.elementInjector||ar(),o=new yr(e).create(i,n.projectableNodes,n.hostElement,n.environmentInjector,n.directives,n.bindings);return n.onError&&(o.hostView._lView[cr]=n.onError),o}var Sx=null;function Fn(){return Sx}function ag(t){Sx??=t}var dc=class{};var Nr=(()=>{class t{historyGo(e){throw new Error(``)}static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:()=>f(wx),providedIn:`platform`})}return t})();var wx=(()=>{class t extends Nr{_location;_history;_doc=f(de);constructor(){super(),this._location=window.location,this._history=window.history}getBaseHrefFromDOM(){return Fn().getBaseHref(this._doc)}onPopState(e){let i=Fn().getGlobalEventTarget(this._doc,`window`);return i.addEventListener(`popstate`,e,!1),()=>i.removeEventListener(`popstate`,e)}onHashChange(e){let i=Fn().getGlobalEventTarget(this._doc,`window`);return i.addEventListener(`hashchange`,e,!1),()=>i.removeEventListener(`hashchange`,e)}get href(){return this._location.href}get protocol(){return this._location.protocol}get hostname(){return this._location.hostname}get port(){return this._location.port}get pathname(){return this._location.pathname}get search(){return this._location.search}get hash(){return this._location.hash}set pathname(e){this._location.pathname=e}pushState(e,i,a){this._history.pushState(e,i,a)}replaceState(e,i,a){this._history.replaceState(e,i,a)}forward(){this._history.forward()}back(){this._history.back()}historyGo(e=0){this._history.go(e)}getState(){return this._history.state}static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:()=>new t,providedIn:`platform`})}return t})();function Ix(t,n){return t?n?t.endsWith(`/`)?n.startsWith(`/`)?t+n.slice(1):t+n:n.startsWith(`/`)?t+n:`${t}/${n}`:t:n}function Ex(t){let n=t.search(/#|\?|$/);return t[n-1]===`/`?t.slice(0,n-1)+t.slice(n):t}function Ea(t){return t&&t[0]!==`?`?`?${t}`:t}var nu=(()=>{class t{historyGo(e){throw new Error(``)}static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:()=>f(kT),providedIn:`root`})}return t})();var IT=new k(``);var kT=(()=>{class t extends nu{_platformLocation;_baseHref;_removeListenerFns=[];constructor(e,i){super(),this._platformLocation=e,this._baseHref=i??this._platformLocation.getBaseHrefFromDOM()??f(de).location?.origin??``}ngOnDestroy(){for(;this._removeListenerFns.length;)this._removeListenerFns.pop()()}onPopState(e){this._removeListenerFns.push(this._platformLocation.onPopState(e),this._platformLocation.onHashChange(e))}getBaseHref(){return this._baseHref}prepareExternalUrl(e){return Ix(this._baseHref,e)}path(e=!1){let i=this._platformLocation.pathname+Ea(this._platformLocation.search),a=this._platformLocation.hash;return a&&e?`${i}${a}`:i}pushState(e,i,a,o){let r=this.prepareExternalUrl(a+Ea(o));this._platformLocation.pushState(e,i,r)}replaceState(e,i,a,o){let r=this.prepareExternalUrl(a+Ea(o));this._platformLocation.replaceState(e,i,r)}forward(){this._platformLocation.forward()}back(){this._platformLocation.back()}getState(){return this._platformLocation.getState()}historyGo(e=0){this._platformLocation.historyGo?.(e)}static ɵfac=function(i){return new(i||t)(Se(Nr),Se(IT,8))};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();var iu=(()=>{class t{_subject=new L;_basePath;_locationStrategy;_urlChangeListeners=[];_urlChangeSubscription=null;constructor(e){this._locationStrategy=e;let i=this._locationStrategy.getBaseHref();this._basePath=NT(Ex(Dx(i))),this._locationStrategy.onPopState(a=>{let o={url:this.path(!0),pop:!0,state:a.state,type:a.type};a.hasUAVisualTransition&&(o.hasUAVisualTransition=!0),this._subject.next(o)})}ngOnDestroy(){this._urlChangeSubscription?.unsubscribe(),this._urlChangeListeners=[]}path(e=!1){return this.normalize(this._locationStrategy.path(e))}getState(){return this._locationStrategy.getState()}isCurrentPathEqualTo(e,i=``){return this.path()==this.normalize(e+Ea(i))}normalize(e){return t.stripTrailingSlash(TT(this._basePath,Dx(e)))}prepareExternalUrl(e){return e&&e[0]!==`/`&&(e=`/`+e),this._locationStrategy.prepareExternalUrl(e)}go(e,i=``,a=null){this._locationStrategy.pushState(a,``,e,i),this._notifyUrlChangeListeners(this.prepareExternalUrl(e+Ea(i)),a)}replaceState(e,i=``,a=null){this._locationStrategy.replaceState(a,``,e,i),this._notifyUrlChangeListeners(this.prepareExternalUrl(e+Ea(i)),a)}forward(){this._locationStrategy.forward()}back(){this._locationStrategy.back()}historyGo(e=0){this._locationStrategy.historyGo?.(e)}onUrlChange(e){return this._urlChangeListeners.push(e),this._urlChangeSubscription??=this.subscribe(i=>{this._notifyUrlChangeListeners(i.url,i.state)}),()=>{let i=this._urlChangeListeners.indexOf(e);this._urlChangeListeners.splice(i,1),this._urlChangeListeners.length===0&&(this._urlChangeSubscription?.unsubscribe(),this._urlChangeSubscription=null)}}_notifyUrlChangeListeners(e=``,i){this._urlChangeListeners.forEach(a=>a(e,i))}subscribe(e,i,a){return this._subject.subscribe({next:e,error:i??void 0,complete:a??void 0})}static normalizeQueryParams=Ea;static joinWithSlash=Ix;static stripTrailingSlash=Ex;static ɵfac=function(i){return new(i||t)(Se(nu))};static ɵprov=be({token:t,factory:()=>MT(),providedIn:`root`})}return t})();function MT(){return new iu(Se(nu))}function TT(t,n){if(!t||!n.startsWith(t))return n;let e=n.substring(t.length);return e===``||[`/`,`;`,`?`,`#`].includes(e[0])?e:n}function Dx(t){return t.replace(/\/index\.html$/,``)}function NT(t){if(new RegExp(`^(https?:)?//`).test(t)){let[,e]=t.split(/\/\/[^\/]+/);return e}return t}var dg=(function(t){return t[t.Decimal=0]=`Decimal`,t[t.Percent=1]=`Percent`,t[t.Currency=2]=`Currency`,t[t.Scientific=3]=`Scientific`,t})(dg||{});var Zt=(function(t){return t[t.Format=0]=`Format`,t[t.Standalone=1]=`Standalone`,t})(Zt||{});var it=(function(t){return t[t.Narrow=0]=`Narrow`,t[t.Abbreviated=1]=`Abbreviated`,t[t.Wide=2]=`Wide`,t[t.Short=3]=`Short`,t})(it||{});var bn=(function(t){return t[t.Short=0]=`Short`,t[t.Medium=1]=`Medium`,t[t.Long=2]=`Long`,t[t.Full=3]=`Full`,t})(bn||{});var cn={Decimal:0,Group:1,List:2,PercentSign:3,PlusSign:4,MinusSign:5,Exponential:6,SuperscriptingExponent:7,PerMille:8,Infinity:9,NaN:10,TimeSeparator:11,CurrencyDecimal:12,CurrencyGroup:13};function Tx(t){return fn(t)[Et.LocaleId]}function Nx(t,n,e){let i=fn(t);return Ln(Ln([i[Et.DayPeriodsFormat],i[Et.DayPeriodsStandalone]],n),e)}function Ax(t,n,e){let i=fn(t);return Ln(Ln([i[Et.DaysFormat],i[Et.DaysStandalone]],n),e)}function Ox(t,n,e){let i=fn(t);return Ln(Ln([i[Et.MonthsFormat],i[Et.MonthsStandalone]],n),e)}function Rx(t,n){let i=fn(t)[Et.Eras];return Ln(i,n)}function uc(t,n){return Ln(fn(t)[Et.DateFormat],n)}function mc(t,n){return Ln(fn(t)[Et.TimeFormat],n)}function pc(t,n){let i=fn(t)[Et.DateTimeFormat];return Ln(i,n)}function wi(t,n){let e=fn(t),i=e[Et.NumberSymbols][n];if(typeof i>`u`){if(n===cn.CurrencyDecimal)return e[Et.NumberSymbols][cn.Decimal];if(n===cn.CurrencyGroup)return e[Et.NumberSymbols][cn.Group]}return i}function Px(t,n){return fn(t)[Et.NumberFormats][n]}function Fx(t){if(!t[Et.ExtraData])throw new G(2303,!1)}function Lx(t){let n=fn(t);return Fx(n),(n[Et.ExtraData][2]||[]).map(i=>typeof i==`string`?og(i):[og(i[0]),og(i[1])])}function Vx(t,n,e){let i=fn(t);Fx(i);return Ln(Ln([i[Et.ExtraData][0],i[Et.ExtraData][1]],n)||[],e)||[]}function Ln(t,n){for(let e=n;e>-1;e--)if(typeof t[e]<`u`)return t[e];throw new G(2304,!1)}function og(t){let[n,e]=t.split(`:`);return{hours:+n,minutes:+e}}var OT=/^(\d{4,})-?(\d\d)-?(\d\d)(?:T(\d\d)(?::?(\d\d)(?::?(\d\d)(?:\.(\d+))?)?)?(Z|([+-])(\d\d):?(\d\d))?)?$/;var au=Object.create(null);var RT=/((?:[^BEGHLMOSWYZabcdhmswyz']+)|(?:'(?:[^']|'')*')|(?:G{1,5}|y{1,4}|Y{1,4}|M{1,5}|L{1,5}|w{1,2}|W{1}|d{1,2}|E{1,6}|c{1,6}|a{1,5}|b{1,5}|B{1,5}|h{1,2}|H{1,2}|m{1,2}|s{1,2}|S{1,3}|z{1,4}|Z{1,5}|O{1,4}))([\s\S]*)/;var PT=256;function Bx(t,n,e,i){let a=GT(t);FT(n),n=Qi(e,n)||n;let r=[],s;for(;n;)if(s=RT.exec(n),s){r=r.concat(s.slice(1));let p=r.pop();if(!p)break;n=p}else{r.push(n);break}let c=a.getTimezoneOffset();i&&(c=zx(i,c),a=$T(a,i));let h=``;return r.forEach(p=>{let g=HT(p);h+=g?g(a,e,c):p===`''`?`'`:p.replace(/(^'|'$)/g,``).replace(/''/g,`'`)}),h}function FT(t){if(t.length>PT)throw new G(2300,!1)}function lu(t,n,e){let i=new Date(0);return i.setFullYear(t,n,e),i.setHours(0,0,0),i}function Qi(t,n){let e=Tx(t);if(au[e]??=Object.create(null),au[e][n])return au[e][n];let i=``;switch(n){case`shortDate`:i=uc(t,bn.Short);break;case`mediumDate`:i=uc(t,bn.Medium);break;case`longDate`:i=uc(t,bn.Long);break;case`fullDate`:i=uc(t,bn.Full);break;case`shortTime`:i=mc(t,bn.Short);break;case`mediumTime`:i=mc(t,bn.Medium);break;case`longTime`:i=mc(t,bn.Long);break;case`fullTime`:i=mc(t,bn.Full);break;case`short`:let a=Qi(t,`shortTime`),o=Qi(t,`shortDate`);i=ou(pc(t,bn.Short),[a,o]);break;case`medium`:let r=Qi(t,`mediumTime`),s=Qi(t,`mediumDate`);i=ou(pc(t,bn.Medium),[r,s]);break;case`long`:let c=Qi(t,`longTime`),h=Qi(t,`longDate`);i=ou(pc(t,bn.Long),[c,h]);break;case`full`:let p=Qi(t,`fullTime`),g=Qi(t,`fullDate`);i=ou(pc(t,bn.Full),[p,g])}return i&&(au[e][n]=i),i}function ou(t,n){return n&&(t=t.replace(/\{([^}]+)}/g,function(e,i){return Object.hasOwn(n,i)?n[i]:e})),t}function oi(t,n,e=`-`,i,a){let o=``;(t<0||a&&t<=0)&&(a?t=-t+1:(t=-t,o=e));let r=String(t);for(;r.length<n;)r=`0`+r;return i&&(r=r.slice(r.length-n)),o+r}function LT(t,n){return oi(t,3).substring(0,n)}function Rt(t,n,e=0,i=!1,a=!1){return function(o,r){let s=VT(t,o);if((e>0||s>-e)&&(s+=e),t===3)s===0&&e===-12&&(s=12);else if(t===6)return LT(s,n);let c=wi(r,cn.MinusSign);return oi(s,n,c,i,a)}}function VT(t,n){switch(t){case 0:return n.getFullYear();case 1:return n.getMonth();case 2:return n.getDate();case 3:return n.getHours();case 4:return n.getMinutes();case 5:return n.getSeconds();case 6:return n.getMilliseconds();case 7:return n.getDay();default:throw new G(2301,!1)}}function dt(t,n,e=Zt.Format,i=!1){return function(a,o){return BT(a,o,t,n,e,i)}}function BT(t,n,e,i,a,o){switch(e){case 2:return Ox(n,a,i)[t.getMonth()];case 1:return Ax(n,a,i)[t.getDay()];case 0:let r=t.getHours(),s=t.getMinutes();if(o){let h=Lx(n),p=Vx(n,a,i),g=h.findIndex(v=>{if(Array.isArray(v)){let[b,y]=v,E=r>=b.hours&&s>=b.minutes,P=r<y.hours||r===y.hours&&s<y.minutes;if(b.hours<y.hours){if(E&&P)return!0}else if(E||P)return!0}else if(v.hours===r&&v.minutes===s)return!0;return!1});if(g!==-1)return p[g]}return Nx(n,a,i)[r<12?0:1];case 3:return Rx(n,i)[t.getFullYear()<=0?0:1];default:throw new G(2302,!1)}}function ru(t){return function(n,e,i){let a=-1*i,o=wi(e,cn.MinusSign),r=a>0?Math.floor(a/60):Math.ceil(a/60);switch(t){case 0:return(a>=0?`+`:``)+oi(r,2,o)+oi(Math.abs(a%60),2,o);case 1:return`GMT`+(a>=0?`+`:``)+oi(r,1,o);case 2:return`GMT`+(a>=0?`+`:``)+oi(r,2,o)+`:`+oi(Math.abs(a%60),2,o);case 3:return i===0?`Z`:(a>=0?`+`:``)+oi(r,2,o)+`:`+oi(Math.abs(a%60),2,o);default:throw new G(2310,!1)}}}var jT=0;var cu=4;function zT(t){let n=lu(t,jT,1).getDay();return lu(t,0,1+(n<=cu?cu:cu+7)-n)}function jx(t){let n=t.getDay(),e=n===0?-3:cu-n;return lu(t.getFullYear(),t.getMonth(),t.getDate()+e)}function rg(t,n=!1){return function(e,i){let a;if(n){let o=new Date(e.getFullYear(),e.getMonth(),1).getDay()-1,r=e.getDate();a=1+Math.floor((r+o)/7)}else{let o=jx(e),r=zT(o.getFullYear()),s=o.getTime()-r.getTime();a=1+Math.round(s/6048e5)}return oi(a,t,wi(i,cn.MinusSign))}}function su(t,n=!1){return function(e,i){return oi(jx(e).getFullYear(),t,wi(i,cn.MinusSign),n)}}var sg=Object.create(null);function HT(t){if(sg[t])return sg[t];let n;switch(t){case`G`:case`GG`:case`GGG`:n=dt(3,it.Abbreviated);break;case`GGGG`:n=dt(3,it.Wide);break;case`GGGGG`:n=dt(3,it.Narrow);break;case`y`:n=Rt(0,1,0,!1,!0);break;case`yy`:n=Rt(0,2,0,!0,!0);break;case`yyy`:n=Rt(0,3,0,!1,!0);break;case`yyyy`:n=Rt(0,4,0,!1,!0);break;case`Y`:n=su(1);break;case`YY`:n=su(2,!0);break;case`YYY`:n=su(3);break;case`YYYY`:n=su(4);break;case`M`:case`L`:n=Rt(1,1,1);break;case`MM`:case`LL`:n=Rt(1,2,1);break;case`MMM`:n=dt(2,it.Abbreviated);break;case`MMMM`:n=dt(2,it.Wide);break;case`MMMMM`:n=dt(2,it.Narrow);break;case`LLL`:n=dt(2,it.Abbreviated,Zt.Standalone);break;case`LLLL`:n=dt(2,it.Wide,Zt.Standalone);break;case`LLLLL`:n=dt(2,it.Narrow,Zt.Standalone);break;case`w`:n=rg(1);break;case`ww`:n=rg(2);break;case`W`:n=rg(1,!0);break;case`d`:n=Rt(2,1);break;case`dd`:n=Rt(2,2);break;case`c`:case`cc`:n=Rt(7,1);break;case`ccc`:n=dt(1,it.Abbreviated,Zt.Standalone);break;case`cccc`:n=dt(1,it.Wide,Zt.Standalone);break;case`ccccc`:n=dt(1,it.Narrow,Zt.Standalone);break;case`cccccc`:n=dt(1,it.Short,Zt.Standalone);break;case`E`:case`EE`:case`EEE`:n=dt(1,it.Abbreviated);break;case`EEEE`:n=dt(1,it.Wide);break;case`EEEEE`:n=dt(1,it.Narrow);break;case`EEEEEE`:n=dt(1,it.Short);break;case`a`:case`aa`:case`aaa`:n=dt(0,it.Abbreviated);break;case`aaaa`:n=dt(0,it.Wide);break;case`aaaaa`:n=dt(0,it.Narrow);break;case`b`:case`bb`:case`bbb`:n=dt(0,it.Abbreviated,Zt.Standalone,!0);break;case`bbbb`:n=dt(0,it.Wide,Zt.Standalone,!0);break;case`bbbbb`:n=dt(0,it.Narrow,Zt.Standalone,!0);break;case`B`:case`BB`:case`BBB`:n=dt(0,it.Abbreviated,Zt.Format,!0);break;case`BBBB`:n=dt(0,it.Wide,Zt.Format,!0);break;case`BBBBB`:n=dt(0,it.Narrow,Zt.Format,!0);break;case`h`:n=Rt(3,1,-12);break;case`hh`:n=Rt(3,2,-12);break;case`H`:n=Rt(3,1);break;case`HH`:n=Rt(3,2);break;case`m`:n=Rt(4,1);break;case`mm`:n=Rt(4,2);break;case`s`:n=Rt(5,1);break;case`ss`:n=Rt(5,2);break;case`S`:n=Rt(6,1);break;case`SS`:n=Rt(6,2);break;case`SSS`:n=Rt(6,3);break;case`Z`:case`ZZ`:case`ZZZ`:n=ru(0);break;case`ZZZZZ`:n=ru(3);break;case`O`:case`OO`:case`OOO`:case`z`:case`zz`:case`zzz`:n=ru(1);break;case`OOOO`:case`ZZZZ`:case`zzzz`:n=ru(2);break;default:return null}return sg[t]=n,n}function zx(t,n){t=t.replace(/:/g,``);let e=Date.parse(`Jan 01, 1970 00:00:00 `+t)/6e4;return isNaN(e)?n:e}function UT(t,n){return t=new Date(t.getTime()),t.setMinutes(t.getMinutes()+n),t}function $T(t,n,e){let a=t.getTimezoneOffset();return UT(t,-1*(zx(n,a)-a))}function GT(t){if(kx(t))return t;if(typeof t==`number`&&!isNaN(t))return new Date(t);if(typeof t==`string`){if(t=t.trim(),/^(\d{4}(-\d{1,2}(-\d{1,2})?)?)$/.test(t)){let[a,o=1,r=1]=t.split(`-`).map(s=>+s);return lu(a,o-1,r)}let e=parseFloat(t);if(!isNaN(t-e))return new Date(e);let i;if(i=t.match(OT))return WT(i)}let n=new Date(t);if(!kx(n))throw new G(2311,!1);return n}function WT(t){let n=new Date(0),e=0,i=0,a=t[8]?n.setUTCFullYear:n.setFullYear,o=t[8]?n.setUTCHours:n.setHours;t[9]&&(e=Number(t[9]+t[10]),i=Number(t[9]+t[11])),a.call(n,Number(t[1]),Number(t[2])-1,Number(t[3]));let r=Number(t[4]||0)-e,s=Number(t[5]||0)-i,c=Number(t[6]||0),h=Math.floor(parseFloat(`0.`+(t[7]||0))*1e3);return o.call(n,r,s,c,h),n}function kx(t){return t instanceof Date&&!isNaN(t.valueOf())}var qT=/^(\d+)?\.((\d+)(-(\d+))?)?$/;var Mx=22;var du=`.`;var hc=`0`;var YT=`;`;var KT=`,`;var cg=`#`;function QT(t,n,e,i,a,o,r=!1){let s=``,c=!1;if(!isFinite(t))s=wi(e,Number.isNaN(t)?cn.NaN:cn.Infinity);else{let h=JT(t);r&&(h=ZT(h));let p=n.minInt,g=n.minFrac,v=n.maxFrac;if(o){let z=o.match(qT);if(z===null)throw new G(2306,!1);let x=z[1],M=z[3],O=z[5];x!=null&&(p=lg(x)),M!=null&&(g=lg(M)),O!=null?v=lg(O):M!=null&&g>v&&(v=g);let $=100;if(p>$||g>$||v>$)throw new G(2306,!1)}eN(h,g,v);let b=h.digits,y=h.integerLen,E=h.exponent,P=[];for(c=b.every(z=>!z);y<p;y++)b.unshift(0);for(;y<0;y++)b.unshift(0);y>0?P=b.splice(y,b.length):(P=b,b=[0]);let ae=[];for(b.length>=n.lgSize&&ae.unshift(b.splice(-n.lgSize,b.length).join(``));b.length>n.gSize;)ae.unshift(b.splice(-n.gSize,b.length).join(``));b.length&&ae.unshift(b.join(``)),s=ae.join(wi(e,i)),P.length&&(s+=wi(e,a)+P.join(``)),E&&(s+=wi(e,cn.Exponential)+`+`+E)}return t<0&&!c?s=n.negPre+s+n.negSuf:s=n.posPre+s+n.posSuf,s}function Hx(t,n,e){return QT(t,XT(Px(n,dg.Decimal),wi(n,cn.MinusSign)),n,cn.Group,cn.Decimal,e)}function XT(t,n=`-`){let e={minInt:1,minFrac:0,maxFrac:0,posPre:``,posSuf:``,negPre:``,negSuf:``,gSize:0,lgSize:0},i=t.split(YT),a=i[0],o=i[1],r=a.indexOf(du)!==-1?a.split(du):[a.substring(0,a.lastIndexOf(hc)+1),a.substring(a.lastIndexOf(hc)+1)],s=r[0],c=r[1]||``;e.posPre=s.substring(0,s.indexOf(cg));for(let p=0;p<c.length;p++){let g=c.charAt(p);g===hc?e.minFrac=e.maxFrac=p+1:g===cg?e.maxFrac=p+1:e.posSuf+=g}let h=s.split(KT);if(e.gSize=h[1]?h[1].length:0,e.lgSize=h[2]||h[1]?(h[2]||h[1]).length:0,o){let p=a.length-e.posPre.length-e.posSuf.length,g=o.indexOf(cg);e.negPre=o.substring(0,g).replace(/'/g,``),e.negSuf=o.slice(g+p).replace(/'/g,``)}else e.negPre=n+e.posPre,e.negSuf=e.posSuf;return e}function ZT(t){if(t.digits[0]===0)return t;let n=t.digits.length-t.integerLen;return t.exponent?t.exponent+=2:(n===0?t.digits.push(0,0):n===1&&t.digits.push(0),t.integerLen+=2),t}function JT(t){let n=Math.abs(t)+``,e=0,i,a,o,r,s;for((a=n.indexOf(du))>-1&&(n=n.replace(du,``)),(o=n.search(/e/i))>0?(a<0&&(a=o),a+=+n.slice(o+1),n=n.substring(0,o)):a<0&&(a=n.length),o=0;n.charAt(o)===hc;o++);if(o===(s=n.length))i=[0],a=1;else{for(s--;n.charAt(s)===hc;)s--;for(a-=o,i=[],r=0;o<=s;o++,r++)i[r]=Number(n.charAt(o))}return a>Mx&&(i=i.splice(0,Mx-1),e=a-1,a=1),{digits:i,exponent:e,integerLen:a}}function eN(t,n,e){if(n>e)throw new G(2307,!1);let i=t.digits,a=i.length-t.integerLen,o=Math.min(Math.max(n,a),e),r=o+t.integerLen,s=i[r];if(r>0){i.splice(Math.max(t.integerLen,r));for(let g=r;g<i.length;g++)i[g]=0}else{a=Math.max(0,a),t.integerLen=1,i.length=Math.max(1,r=o+1),i[0]=0;for(let g=1;g<r;g++)i[g]=0}if(s>=5)if(r-1<0){for(let g=0;g>r;g--)i.unshift(0),t.integerLen++;i.unshift(1),t.integerLen++}else i[r-1]++;for(;a<Math.max(0,o);a++)i.push(0);let c=o!==0,h=n+t.integerLen,p=i.reduceRight(function(g,v,b,y){return v=v+g,y[b]=v<10?v:v-10,c&&(y[b]===0&&b>=h?y.pop():c=!1),v>=10?1:0},0);p&&(i.unshift(p),t.integerLen++)}function lg(t){let n=parseInt(t);if(isNaN(n))throw new G(2305,!1);return n}var ug=(()=>{class t{_viewContainerRef;ngComponentOutlet=null;ngComponentOutletInputs;ngComponentOutletInjector;ngComponentOutletEnvironmentInjector;ngComponentOutletContent;ngComponentOutletNgModule;_componentRef;_moduleRef;_inputsUsed=new Map;get componentInstance(){return this._componentRef?.instance??null}constructor(e){this._viewContainerRef=e}_needToReCreateNgModuleInstance(e){return e.ngComponentOutletNgModule!==void 0}_needToReCreateComponentInstance(e){return e.ngComponentOutlet!==void 0||e.ngComponentOutletContent!==void 0||e.ngComponentOutletInjector!==void 0||e.ngComponentOutletEnvironmentInjector!==void 0||this._needToReCreateNgModuleInstance(e)}ngOnChanges(e){if(this._needToReCreateComponentInstance(e)&&(this._viewContainerRef.clear(),this._inputsUsed.clear(),this._componentRef=void 0,this.ngComponentOutlet)){let i=this.ngComponentOutletInjector||this._viewContainerRef.parentInjector;this._needToReCreateNgModuleInstance(e)&&(this._moduleRef?.destroy(),this.ngComponentOutletNgModule?this._moduleRef=Hf(this.ngComponentOutletNgModule,tN(i)):this._moduleRef=void 0),this._componentRef=this._viewContainerRef.createComponent(this.ngComponentOutlet,{injector:i,ngModuleRef:this._moduleRef,projectableNodes:this.ngComponentOutletContent,environmentInjector:this.ngComponentOutletEnvironmentInjector})}}ngDoCheck(){if(this._componentRef){if(this.ngComponentOutletInputs)for(let e of Object.keys(this.ngComponentOutletInputs))this._inputsUsed.set(e,!0);this._applyInputStateDiff(this._componentRef)}}ngOnDestroy(){this._moduleRef?.destroy()}_applyInputStateDiff(e){for(let[i,a]of this._inputsUsed)a?(e.setInput(i,this.ngComponentOutletInputs[i]),this._inputsUsed.set(i,!1)):(e.setInput(i,void 0),this._inputsUsed.delete(i))}static ɵfac=function(i){return new(i||t)(He(Bt))};static ɵdir=K({type:t,selectors:[[``,`ngComponentOutlet`,``]],inputs:{ngComponentOutlet:`ngComponentOutlet`,ngComponentOutletInputs:`ngComponentOutletInputs`,ngComponentOutletInjector:`ngComponentOutletInjector`,ngComponentOutletEnvironmentInjector:`ngComponentOutletEnvironmentInjector`,ngComponentOutletContent:`ngComponentOutletContent`,ngComponentOutletNgModule:`ngComponentOutletNgModule`},exportAs:[`ngComponentOutlet`],features:[vt]})}return t})();function tN(t){return t.get(ni).injector}var fc=(()=>{class t{_viewContainerRef;_viewRef=null;ngTemplateOutletContext=null;ngTemplateOutlet=null;ngTemplateOutletInjector=null;injector=f(se);constructor(e){this._viewContainerRef=e}ngOnChanges(e){if(this._shouldRecreateView(e)){let i=this._viewContainerRef;if(this._viewRef&&i.remove(i.indexOf(this._viewRef)),!this.ngTemplateOutlet){this._viewRef=null;return}let a=this._createContextForwardProxy();this._viewRef=i.createEmbeddedView(this.ngTemplateOutlet,a,{injector:this._getInjector()})}}_getInjector(){return this.ngTemplateOutletInjector===`outlet`?this.injector:this.ngTemplateOutletInjector??void 0}_shouldRecreateView(e){return!!e.ngTemplateOutlet||!!e.ngTemplateOutletInjector}_createContextForwardProxy(){return new Proxy({},{set:(e,i,a)=>this.ngTemplateOutletContext?Reflect.set(this.ngTemplateOutletContext,i,a):!1,get:(e,i,a)=>{if(this.ngTemplateOutletContext)return Reflect.get(this.ngTemplateOutletContext,i,a)}})}static ɵfac=function(i){return new(i||t)(He(Bt))};static ɵdir=K({type:t,selectors:[[``,`ngTemplateOutlet`,``]],inputs:{ngTemplateOutletContext:`ngTemplateOutletContext`,ngTemplateOutlet:`ngTemplateOutlet`,ngTemplateOutletInjector:`ngTemplateOutletInjector`},features:[vt]})}return t})();function Ux(t,n){return new G(2100,!1)}var nN=`mediumDate`;var $x=new k(``);var Gx=new k(``);var Xi=(()=>{class t{locale;defaultTimezone;defaultOptions;constructor(e,i,a){this.locale=e,this.defaultTimezone=i,this.defaultOptions=a}transform(e,i,a,o){if(e==null||e===``||e!==e)return null;try{let r=i??this.defaultOptions?.dateFormat??nN,s=a??this.defaultOptions?.timezone??this.defaultTimezone??void 0;return Bx(e,r,o||this.locale,s)}catch(r){throw Ux(t,r.message)}}static ɵfac=function(i){return new(i||t)(He(Tr,16),He($x,24),He(Gx,24))};static ɵpipe=nc({name:`date`,type:t,pure:!0})}return t})();var wo=(()=>{class t{transform(e){return JSON.stringify(e,null,2)}static ɵfac=function(i){return new(i||t)};static ɵpipe=nc({name:`json`,type:t,pure:!1})}return t})();var En=(()=>{class t{_locale;constructor(e){this._locale=e}transform(e,i,a){if(!iN(e))return null;a||=this._locale;try{return Hx(aN(e),a,i)}catch(o){throw Ux(t,o.message)}}static ɵfac=function(i){return new(i||t)(He(Tr,16))};static ɵpipe=nc({name:`number`,type:t,pure:!0})}return t})();function iN(t){return!(t==null||t===``||t!==t)}function aN(t){if(typeof t==`string`&&!isNaN(Number(t)-parseFloat(t)))return Number(t);if(typeof t!=`number`)throw new G(2309,!1);return t}function gc(t,n){n=encodeURIComponent(n);for(let e of t.split(`;`)){let i=e.indexOf(`=`),[a,o]=i==-1?[e,``]:[e.slice(0,i),e.slice(i+1)];if(a.trim()!==n)continue;let r=o;try{r=decodeURIComponent(o)}catch{}return r.length>1&&r[0]===`"`&&r[r.length-1]===`"`&&(r=r.slice(1,-1)),r}return null}var mg=`browser`;var cN=`server`;function Wx(t){return t===mg}function qx(t){return t===cN}var bc=class{_doc;constructor(n){this._doc=n}manager};var uu=(()=>{class t extends bc{constructor(e){super(e)}supports(e){return!0}addEventListener(e,i,a,o){return e.addEventListener(i,a,o),()=>this.removeEventListener(e,i,a,o)}removeEventListener(e,i,a,o){return e.removeEventListener(i,a,o)}static ɵfac=function(i){return new(i||t)(Se(de))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var hu=new k(``);var gg=(()=>{class t{_zone;_plugins;_eventNameToPlugin=new Map;constructor(e,i){this._zone=i,e.forEach(r=>{r.manager=this});let a=e.filter(r=>!(r instanceof uu));this._plugins=a.slice().reverse();let o=e.find(r=>r instanceof uu);o&&this._plugins.push(o)}addEventListener(e,i,a,o){return this._findPluginFor(i).addEventListener(e,i,a,o)}getZone(){return this._zone}_findPluginFor(e){let i=this._eventNameToPlugin.get(e);if(i)return i;if(i=this._plugins.find(o=>o.supports(e)),!i)throw new G(-5101,!1);return this._eventNameToPlugin.set(e,i),i}static ɵfac=function(i){return new(i||t)(Se(hu),Se(X))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var pg=`ng-app-id`;function Yx(t){for(let n of t)n.remove()}function Kx(t,n){let e=n.createElement(`style`);return e.textContent=t,e}function lN(t,n,e,i){let a=t.head?.querySelectorAll(`style[${pg}="${n}"],link[${pg}="${n}"]`);if(!a||a.length===0)return!1;for(let o of a)o.removeAttribute(pg),o instanceof HTMLLinkElement?i.set(o.href.slice(o.href.lastIndexOf(`/`)+1),{usage:0,elements:[o]}):o.textContent&&e.set(o.textContent,{usage:0,elements:[o]});return!0}function fg(t,n){let e=n.createElement(`link`);return e.setAttribute(`rel`,`stylesheet`),e.setAttribute(`href`,t),e}var bg=(()=>{class t{doc;appId;nonce;inline=new Map;external=new Map;hosts=new Set;constructor(e,i,a,o={}){this.doc=e,this.appId=i,this.nonce=a,lN(e,i,this.inline,this.external)&&this.hosts.add(e.head)}addStyles(e,i){for(let a of e)this.addUsage(a,this.inline,Kx);i?.forEach(a=>this.addUsage(a,this.external,fg))}removeStyles(e,i){for(let a of e)this.removeUsage(a,this.inline);i?.forEach(a=>this.removeUsage(a,this.external))}addUsage(e,i,a){let o=i.get(e);o?o.usage++:i.set(e,{usage:1,elements:[...this.hosts].map(r=>this.addElement(r,a(e,this.doc)))})}removeUsage(e,i){let a=i.get(e);a&&(a.usage--,a.usage<=0&&(Yx(a.elements),i.delete(e)))}ngOnDestroy(){for(let[,{elements:e}]of[...this.inline,...this.external])Yx(e);this.hosts.clear()}addHost(e){if(!this.hosts.has(e)){this.hosts.add(e);for(let[i,{elements:a}]of this.inline)a.push(this.addElement(e,Kx(i,this.doc)));for(let[i,{elements:a}]of this.external)a.push(this.addElement(e,fg(i,this.doc)))}}removeHost(e){this.hosts.delete(e);for(let i of[...this.inline.values(),...this.external.values()]){let a=[];for(let o of i.elements)o.parentNode===e?o.remove():a.push(o);i.elements=a}}addElement(e,i){return this.nonce&&i.setAttribute(`nonce`,this.nonce),e.appendChild(i)}static ɵfac=function(i){return new(i||t)(Se(de),Se($i),Se(ya,8),Se(Gi))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var hg={svg:`http://www.w3.org/2000/svg`,xhtml:`http://www.w3.org/1999/xhtml`,xlink:`http://www.w3.org/1999/xlink`,xml:`http://www.w3.org/XML/1998/namespace`,xmlns:`http://www.w3.org/2000/xmlns/`,math:`http://www.w3.org/1998/Math/MathML`};var vg=/%COMP%/g;var Xx=`%COMP%`;var dN=`_nghost-${Xx}`;var uN=`_ngcontent-${Xx}`;var mN=!0;var pN=new k(``,{factory:()=>mN});var hN=new k(``);function fN(t){return uN.replace(vg,t)}function gN(t){return dN.replace(vg,t)}function Zx(t,n){return n.map(e=>e.replace(vg,t))}var _g=(()=>{class t{eventManager;sharedStylesHost;appId;removeStylesOnCompDestroy;doc;ngZone;nonce;tracingService;rendererByCompId=new Map;defaultRenderer;cssVarNamespace;constructor(e,i,a,o,r,s,c=null,h=null,p=null){this.eventManager=e,this.sharedStylesHost=i,this.appId=a,this.removeStylesOnCompDestroy=o,this.doc=r,this.ngZone=s,this.nonce=c,this.tracingService=h,this.cssVarNamespace=p??``,this.defaultRenderer=new vc(e,r,s,this.tracingService,this.cssVarNamespace)}createRenderer(e,i){if(!e||!i)return this.defaultRenderer;let a=this.getOrCreateRenderer(e,i);return a instanceof pu?a.applyToHost(e):a instanceof _c&&a.applyStyles(),a}getOrCreateRenderer(e,i){let a=this.rendererByCompId,o=a.get(i.id);if(!o){let r=this.doc,s=this.ngZone,c=this.eventManager,h=this.sharedStylesHost,p=this.removeStylesOnCompDestroy,g=this.tracingService;switch(i.encapsulation){case ei.Emulated:o=new pu(c,h,i,this.appId,p,r,s,g,this.cssVarNamespace);break;case ei.ShadowDom:return new mu(c,e,i,r,s,this.nonce,g,this.cssVarNamespace,h);case ei.ExperimentalIsolatedShadowDom:return new mu(c,e,i,r,s,this.nonce,g,this.cssVarNamespace);default:o=new _c(c,h,i,p,r,s,g,this.cssVarNamespace)}a.set(i.id,o)}return o}ngOnDestroy(){this.rendererByCompId.clear()}componentReplaced(e){this.rendererByCompId.delete(e)}static ɵfac=function(i){return new(i||t)(Se(gg),Se(yo),Se($i),Se(pN),Se(de),Se(X),Se(ya),Se(ii,8),Se(hN,8))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var vc=class{eventManager;doc;ngZone;tracingService;cssVarNamespace;data=Object.create(null);throwOnSyntheticProps=!0;constructor(n,e,i,a,o=``){this.eventManager=n,this.doc=e,this.ngZone=i,this.tracingService=a,this.cssVarNamespace=o}destroy(){}destroyNode=null;createElement(n,e){return e?this.doc.createElementNS(hg[e]||e,n):this.doc.createElement(n)}createComment(n){return this.doc.createComment(n)}createText(n){return this.doc.createTextNode(n)}appendChild(n,e){(Qx(n)?n.content:n).appendChild(e)}insertBefore(n,e,i){if(n){let a=Qx(n)?n.content:n;if(i!=null&&i.parentNode!==a)throw new G(-5106,!1);a.insertBefore(e,i)}}removeChild(n,e){e.remove()}selectRootElement(n,e){let i=typeof n==`string`?this.doc.querySelector(n):n;if(!i)throw new G(-5104,!1);return e||(i.textContent=``),i}parentNode(n){return n.parentNode}nextSibling(n){return n.nextSibling}setAttribute(n,e,i,a){if(a){e=a+`:`+e;let o=hg[a];o?n.setAttributeNS(o,e,i):n.setAttribute(e,i)}else n.setAttribute(e,i)}removeAttribute(n,e,i){if(i){let a=hg[i];a?n.removeAttributeNS(a,e):n.removeAttribute(`${i}:${e}`)}else n.removeAttribute(e)}addClass(n,e){n.classList.add(e)}removeClass(n,e){n.classList.remove(e)}setStyle(n,e,i,a){let o=e.startsWith(`--`);o&&(e=e.replace(`%NS%`,this.cssVarNamespace)),o||a&(xi.DashCase|xi.Important)?n.style.setProperty(e,i,a&xi.Important?`important`:``):n.style[e]=i}removeStyle(n,e,i){let a=e.startsWith(`--`);a&&(e=e.replace(`%NS%`,this.cssVarNamespace)),a||i&xi.DashCase?n.style.removeProperty(e):n.style[e]=``}setProperty(n,e,i){n!=null&&(n[e]=i)}setValue(n,e){n.nodeValue=e}listen(n,e,i,a){if(typeof n==`string`&&(n=Fn().getGlobalEventTarget(this.doc,n),!n))throw new G(-5102,!1);let o=this.decoratePreventDefault(i);return this.tracingService?.wrapEventListener&&(o=this.tracingService.wrapEventListener(n,e,o)),this.eventManager.addEventListener(n,e,o,a)}decoratePreventDefault(n){return e=>{if(e===`__ngUnwrap__`)return n;n(e)===!1&&e.preventDefault()}}};function Qx(t){return t.tagName===`TEMPLATE`&&t.content!==void 0}var mu=class extends vc{hostEl;sharedStylesHost;shadowRoot;constructor(n,e,i,a,o,r,s,c,h){super(n,a,o,s,c),this.hostEl=e,this.sharedStylesHost=h,this.shadowRoot=e.attachShadow({mode:`open`}),this.sharedStylesHost&&this.sharedStylesHost.addHost(this.shadowRoot);let p=i.styles;p=Zx(i.id,p).map(v=>v.replace(/%NS%/g,c));for(let v of p){let b=document.createElement(`style`);r&&b.setAttribute(`nonce`,r),b.textContent=v,this.shadowRoot.appendChild(b)}let g=i.getExternalStyles?.();if(g)for(let v of g){let b=fg(v,a);r&&b.setAttribute(`nonce`,r),this.shadowRoot.appendChild(b)}}nodeOrShadowRoot(n){return n===this.hostEl?this.shadowRoot:n}appendChild(n,e){return super.appendChild(this.nodeOrShadowRoot(n),e)}insertBefore(n,e,i){return super.insertBefore(this.nodeOrShadowRoot(n),e,i)}removeChild(n,e){return super.removeChild(null,e)}parentNode(n){return this.nodeOrShadowRoot(super.parentNode(this.nodeOrShadowRoot(n)))}destroy(){this.sharedStylesHost&&this.sharedStylesHost.removeHost(this.shadowRoot)}};var _c=class extends vc{sharedStylesHost;removeStylesOnCompDestroy;styles;styleUrls;constructor(n,e,i,a,o,r,s,c,h){super(n,o,r,s,c),this.sharedStylesHost=e,this.removeStylesOnCompDestroy=a;let p=i.styles,g=h?Zx(h,p):p;this.styles=g.map(v=>v.replace(/%NS%/g,c)),this.styleUrls=i.getExternalStyles?.(h)}applyStyles(){this.sharedStylesHost.addStyles(this.styles,this.styleUrls)}destroy(){this.removeStylesOnCompDestroy&&vf.size===0&&this.sharedStylesHost.removeStyles(this.styles,this.styleUrls)}};var pu=class extends _c{contentAttr;hostAttr;constructor(n,e,i,a,o,r,s,c,h){let p=a+`-`+i.id;super(n,e,i,o,r,s,c,h,p),this.contentAttr=fN(p),this.hostAttr=gN(p)}applyToHost(n){this.applyStyles(),this.setAttribute(n,this.hostAttr,``)}createElement(n,e){let i=super.createElement(n,e);return super.setAttribute(i,this.contentAttr,``),i}};var fu=class t extends dc{supportsDOMEvents=!0;static makeCurrent(){ag(new t)}onAndCancel(n,e,i,a){return n.addEventListener(e,i,a),()=>{n.removeEventListener(e,i,a)}}dispatchEvent(n,e){n.dispatchEvent(e)}remove(n){n.remove()}createElement(n,e){return e=e||this.getDefaultDocument(),e.createElement(n)}createHtmlDocument(){return document.implementation.createHTMLDocument(`fakeTitle`)}getDefaultDocument(){return document}isElementNode(n){return n.nodeType===Node.ELEMENT_NODE}isShadowRoot(n){return n instanceof DocumentFragment}getGlobalEventTarget(n,e){return e===`window`?window:e===`document`?n:e===`body`?n.body:null}getBaseHref(n){let e=bN();return e==null?null:vN(e)}resetBaseElement(){yc=null}getUserAgent(){return window.navigator.userAgent}getCookie(n){return gc(document.cookie,n)}};var yc=null;function bN(){return yc=yc||document.head.querySelector(`base`),yc?yc.getAttribute(`href`):null}function vN(t){return new URL(t,document.baseURI).pathname}var Jx=[`alt`,`control`,`meta`,`shift`];var _N={"\b":`Backspace`,"	":`Tab`,"":`Delete`,"\x1B":`Escape`,Del:`Delete`,Esc:`Escape`,Left:`ArrowLeft`,Right:`ArrowRight`,Up:`ArrowUp`,Down:`ArrowDown`,Menu:`ContextMenu`,Scroll:`ScrollLock`,Win:`OS`};var yN={alt:t=>t.altKey,control:t=>t.ctrlKey,meta:t=>t.metaKey,shift:t=>t.shiftKey};var eC=(()=>{class t extends bc{constructor(e){super(e)}supports(e){return t.parseEventName(e)!=null}addEventListener(e,i,a,o){let r=t.parseEventName(i),s=t.eventCallback(r.fullKey,a,this.manager.getZone());return this.manager.getZone().runOutsideAngular(()=>Fn().onAndCancel(e,r.domEventName,s,o))}static parseEventName(e){let i=e.toLowerCase().split(`.`),a=i.shift();if(i.length===0||!(a===`keydown`||a===`keyup`))return null;let o=t._normalizeKey(i.pop()),r=``,s=i.indexOf(`code`);if(s>-1&&(i.splice(s,1),r=`code.`),Jx.forEach(h=>{let p=i.indexOf(h);p>-1&&(i.splice(p,1),r+=h+`.`)}),r+=o,i.length!=0||o.length===0)return null;let c={};return c.domEventName=a,c.fullKey=r,c}static matchEventFullKeyCode(e,i){let a=_N[e.key]||e.key,o=``;return i.indexOf(`code.`)>-1&&(a=e.code,o=`code.`),a==null||!a?!1:(a=a.toLowerCase(),a===` `?a=`space`:a===`.`&&(a=`dot`),Jx.forEach(r=>{if(r!==a){let s=yN[r];s(e)&&(o+=r+`.`)}}),o+=a,o===i)}static eventCallback(e,i,a){return o=>{t.matchEventFullKeyCode(o,e)&&a.runGuarded(()=>i(o))}}static _normalizeKey(e){return e===`esc`?`escape`:e}static ɵfac=function(i){return new(i||t)(Se(de))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();async function yg(t,n,e){return xx(r({rootComponent:t},xN(n,e)))}function xN(t,n){return{platformRef:n?.platformRef,appProviders:[...DN,...t?.providers??[]],platformProviders:EN}}function CN(){fu.makeCurrent()}function SN(){return new Tn}function wN(){return cf(document),document}var EN=[{provide:Gi,useValue:mg},{provide:md,useValue:CN,multi:!0},{provide:de,useFactory:wN}];var DN=[{provide:Ts,useValue:`root`},{provide:Tn,useFactory:SN},{provide:hu,useClass:uu,multi:!0},{provide:hu,useClass:eC,multi:!0},_g,{provide:yo,useClass:bg},{provide:bg,useExisting:yo},gg,{provide:Tt,useExisting:_g},[]];var Ji=class t{headers;normalizedNames=new Map;lazyInit;lazyUpdate=null;constructor(n){n?typeof n==`string`?this.lazyInit=()=>{this.headers=new Map,n.split(`
`).forEach(e=>{let i=e.indexOf(`:`);if(i>0){let a=e.slice(0,i),o=e.slice(i+1).trim();this.addHeaderEntry(a,o)}})}:typeof Headers<`u`&&n instanceof Headers?(this.headers=new Map,n.forEach((e,i)=>{this.addHeaderEntry(i,e)})):this.lazyInit=()=>{this.headers=new Map,Object.entries(n).forEach(([e,i])=>{this.setHeaderEntries(e,i)})}:this.headers=new Map}has(n){return this.init(),this.headers.has(n.toLowerCase())}get(n){this.init();let e=this.headers.get(n.toLowerCase());return e&&e.length>0?e[0]:null}keys(){return this.init(),Array.from(this.normalizedNames.values())}getAll(n){return this.init(),this.headers.get(n.toLowerCase())||null}append(n,e){return this.clone({name:n,value:e,op:`a`})}set(n,e){return this.clone({name:n,value:e,op:`s`})}delete(n,e){return this.clone({name:n,value:e,op:`d`})}maybeSetNormalizedName(n,e){this.normalizedNames.has(e)||this.normalizedNames.set(e,n)}init(){this.lazyInit&&(this.lazyInit instanceof t?this.copyFrom(this.lazyInit):this.lazyInit(),this.lazyInit=null,this.lazyUpdate&&(this.lazyUpdate.forEach(n=>this.applyUpdate(n)),this.lazyUpdate=null))}copyFrom(n){n.init();for(let[e,i]of n.headers.entries())this.headers.set(e,i),this.normalizedNames.set(e,n.normalizedNames.get(e))}clone(n){let e=new t;return e.lazyInit=this.lazyInit&&this.lazyInit instanceof t?this.lazyInit:this,e.lazyUpdate=(this.lazyUpdate||[]).concat([n]),e}applyUpdate(n){let e=n.name.toLowerCase();switch(n.op){case`a`:case`s`:let i=n.value;if(typeof i==`string`&&(i=[i]),i.length===0)return;this.maybeSetNormalizedName(n.name,e);let a=n.op===`a`?(this.headers.get(e)||[]).slice():[];a.push(...i),this.headers.set(e,a);break;case`d`:let o=n.value;if(o===void 0)this.headers.delete(e),this.normalizedNames.delete(e);else{let r=Array.isArray(o)?o:[o],s=this.headers.get(e);if(!s)return;s=s.filter(c=>r.indexOf(c)===-1),s.length===0?(this.headers.delete(e),this.normalizedNames.delete(e)):this.headers.set(e,s)}}}addHeaderEntry(n,e){let i=n.toLowerCase();this.maybeSetNormalizedName(n,i),this.headers.has(i)?this.headers.get(i).push(e):this.headers.set(i,[e])}setHeaderEntries(n,e){let i=(Array.isArray(e)?e:[e]).map(o=>o.toString()),a=n.toLowerCase();this.headers.set(a,i),this.maybeSetNormalizedName(n,a)}forEach(n){this.init(),Array.from(this.normalizedNames.keys()).forEach(e=>n(this.normalizedNames.get(e),this.headers.get(e)))}};var bu=class{map=new Map;set(n,e){return this.map.set(n,e),this}get(n){return this.map.has(n)||this.map.set(n,n.defaultValue()),this.map.get(n)}delete(n){return this.map.delete(n),this}has(n){return this.map.has(n)}keys(){return this.map.keys()}};var vu=class{encodeKey(n){return tC(n)}encodeValue(n){return tC(n)}decodeKey(n){return decodeURIComponent(n)}decodeValue(n){return decodeURIComponent(n)}};function IN(t,n){let e=new Map;return t.length>0&&t.replace(/^\?/,``).split(`&`).forEach(a=>{let o=a.indexOf(`=`),[r,s]=o==-1?[n.decodeKey(a),``]:[n.decodeKey(a.slice(0,o)),n.decodeValue(a.slice(o+1))],c=e.get(r)||[];c.push(s),e.set(r,c)}),e}var kN=/%(\d[a-f0-9])/gi;var MN={40:`@`,"3A":`:`,24:`$`,"2C":`,`,"3B":`;`,"3D":`=`,"3F":`?`,"2F":`/`};function tC(t){return encodeURIComponent(t).replace(kN,(n,e)=>MN[e]??n)}function gu(t){return`${t}`}var Zi=class t{map;encoder;updates=null;cloneFrom=null;constructor(n={}){if(this.encoder=n.encoder||new vu,n.fromString){if(n.fromObject)throw new G(2805,!1);this.map=IN(n.fromString,this.encoder)}else n.fromObject?(this.map=new Map,Object.keys(n.fromObject).forEach(e=>{let i=n.fromObject[e],a=Array.isArray(i)?i.map(gu):[gu(i)];this.map.set(e,a)})):this.map=null}has(n){return this.init(),this.map.has(n)}get(n){this.init();let e=this.map.get(n);return e?e[0]:null}getAll(n){return this.init(),this.map.get(n)||null}keys(){return this.init(),Array.from(this.map.keys())}append(n,e){return this.clone({param:n,value:e,op:`a`})}appendAll(n){let e=[];return Object.keys(n).forEach(i=>{let a=n[i];Array.isArray(a)?a.forEach(o=>{e.push({param:i,value:o,op:`a`})}):e.push({param:i,value:a,op:`a`})}),this.clone(e)}set(n,e){return this.clone({param:n,value:e,op:`s`})}delete(n,e){return this.clone({param:n,value:e,op:`d`})}toString(){return this.init(),this.keys().map(n=>{let e=this.encoder.encodeKey(n);return this.map.get(n).map(i=>e+`=`+this.encoder.encodeValue(i)).join(`&`)}).filter(n=>n!==``).join(`&`)}clone(n){let e=new t({encoder:this.encoder});return e.cloneFrom=this.cloneFrom||this,e.updates=(this.updates||[]).concat(n),e}init(){if(this.map===null&&(this.map=new Map),this.cloneFrom!==null){this.cloneFrom.init();for(let[n,e]of this.cloneFrom.map.entries())this.map.set(n,e);this.updates.forEach(n=>{switch(n.op){case`a`:case`s`:let e=n.op===`a`?(this.map.get(n.param)||[]).slice():[];e.push(gu(n.value)),this.map.set(n.param,e);break;case`d`:if(n.value!==void 0){let i=(this.map.get(n.param)||[]).slice(),a=i.indexOf(gu(n.value));a!==-1&&i.splice(a,1),i.length>0?this.map.set(n.param,i):this.map.delete(n.param)}else{this.map.delete(n.param);break}}}),this.cloneFrom=this.updates=null}}};function TN(t){switch(t){case`DELETE`:case`GET`:case`HEAD`:case`OPTIONS`:case`JSONP`:return!1;default:return!0}}function nC(t){return typeof ArrayBuffer<`u`&&t instanceof ArrayBuffer}function iC(t){return typeof Blob<`u`&&t instanceof Blob}function aC(t){return typeof FormData<`u`&&t instanceof FormData}function NN(t){return typeof URLSearchParams<`u`&&t instanceof URLSearchParams}var xg=`Content-Type`;var oC=`Accept`;var cC=`text/plain`;var lC=`application/json`;var AN=`${lC}, ${cC}, */*`;var Ar=class t{url;body=null;headers;context;reportProgress=!1;reportUploadProgress=!1;reportDownloadProgress=!1;withCredentials=!1;credentials;keepalive=!1;cache;priority;mode;redirect;referrer;integrity;referrerPolicy;responseType=`json`;method;params;urlWithParams;transferCache;timeout;constructor(n,e,i,a){this.url=e,this.method=n.toUpperCase();let o;if(TN(this.method)||a?(this.body=i!==void 0?i:null,o=a):o=i,o){if(this.reportProgress=!!o.reportProgress,this.reportUploadProgress=!!o.reportUploadProgress,this.reportDownloadProgress=!!o.reportDownloadProgress,this.withCredentials=!!o.withCredentials,this.keepalive=!!o.keepalive,o.responseType&&(this.responseType=o.responseType),o.headers&&(this.headers=o.headers),o.context&&(this.context=o.context),o.params&&(this.params=o.params),o.priority&&(this.priority=o.priority),o.cache&&(this.cache=o.cache),o.credentials&&(this.credentials=o.credentials),typeof o.timeout==`number`){if(o.timeout<1||!Number.isInteger(o.timeout))throw new G(2822,``);this.timeout=o.timeout}o.mode&&(this.mode=o.mode),o.redirect&&(this.redirect=o.redirect),o.integrity&&(this.integrity=o.integrity),o.referrer!==void 0&&(this.referrer=o.referrer),o.referrerPolicy&&(this.referrerPolicy=o.referrerPolicy),this.transferCache=o.transferCache}if(this.headers??=new Ji,this.context??=new bu,!this.params)this.params=new Zi,this.urlWithParams=e;else{let r=this.params.toString();if(r.length===0)this.urlWithParams=e;else{let s=e,c=``,h=e.indexOf(`#`);h!==-1&&(c=e.substring(h),s=e.substring(0,h));let p=s.indexOf(`?`),g=p===-1?`?`:p<s.length-1?`&`:``;this.urlWithParams=s+g+r+c}}}serializeBody(){return this.body===null?null:typeof this.body==`string`||nC(this.body)||iC(this.body)||aC(this.body)||NN(this.body)?this.body:this.body instanceof Zi?this.body.toString():typeof this.body==`object`||typeof this.body==`boolean`||Array.isArray(this.body)?JSON.stringify(this.body):this.body.toString()}detectContentTypeHeader(){return this.body===null||aC(this.body)?null:iC(this.body)?this.body.type||null:nC(this.body)?null:typeof this.body==`string`?cC:this.body instanceof Zi?`application/x-www-form-urlencoded;charset=UTF-8`:typeof this.body==`object`||typeof this.body==`number`||typeof this.body==`boolean`?lC:null}clone(n={}){let e=n.method||this.method,i=n.url||this.url,a=n.responseType||this.responseType,o=n.keepalive??this.keepalive,r=n.priority||this.priority,s=n.cache||this.cache,c=n.mode||this.mode,h=n.redirect||this.redirect,p=n.credentials||this.credentials,g=n.referrer??this.referrer,v=n.integrity||this.integrity,b=n.referrerPolicy||this.referrerPolicy,y=n.transferCache??this.transferCache,E=n.timeout??this.timeout,P=n.body!==void 0?n.body:this.body,ae=n.withCredentials??this.withCredentials,z=n.reportProgress??this.reportProgress,x=n.reportUploadProgress??this.reportUploadProgress,M=n.reportDownloadProgress??this.reportDownloadProgress,O=n.headers||this.headers,$=n.params||this.params,_e=n.context??this.context;return n.setHeaders!==void 0&&(O=Object.keys(n.setHeaders).reduce((bt,We)=>bt.set(We,n.setHeaders[We]),O)),n.setParams&&($=Object.keys(n.setParams).reduce((bt,We)=>bt.set(We,n.setParams[We]),$)),new t(e,i,P,{params:$,headers:O,context:_e,reportProgress:z,reportUploadProgress:x,reportDownloadProgress:M,responseType:a,withCredentials:ae,transferCache:y,keepalive:o,cache:s,priority:r,timeout:E,mode:c,redirect:h,credentials:p,referrer:g,integrity:v,referrerPolicy:b})}};var Eo=(function(t){return t[t.Sent=0]=`Sent`,t[t.UploadProgress=1]=`UploadProgress`,t[t.ResponseHeader=2]=`ResponseHeader`,t[t.DownloadProgress=3]=`DownloadProgress`,t[t.Response=4]=`Response`,t[t.User=5]=`User`,t})(Eo||{});var Or=class{headers;status;statusText;url;ok;type;redirected;responseType;constructor(n,e=200,i=`OK`){this.headers=n.headers||new Ji,this.status=n.status!==void 0?n.status:e,this.statusText=n.statusText||i,this.url=n.url||null,this.redirected=n.redirected,this.responseType=n.responseType,this.ok=this.status>=200&&this.status<300}};var _u=class t extends Or{constructor(n={}){super(n)}type=Eo.ResponseHeader;clone(n={}){return new t({headers:n.headers||this.headers,status:n.status!==void 0?n.status:this.status,statusText:n.statusText||this.statusText,url:n.url||this.url||void 0})}};var xc=class t extends Or{body;constructor(n={}){super(n),this.body=n.body!==void 0?n.body:null}type=Eo.Response;clone(n={}){return new t({body:n.body!==void 0?n.body:this.body,headers:n.headers||this.headers,status:n.status!==void 0?n.status:this.status,statusText:n.statusText||this.statusText,url:n.url||this.url||void 0,redirected:n.redirected??this.redirected,responseType:n.responseType??this.responseType})}};var Ei=class extends Or{name=`HttpErrorResponse`;message;error;ok=!1;constructor(n){super(n,0,`Unknown Error`),this.status>=200&&this.status<300?this.message=`Http failure during parsing for ${n.url||`(unknown url)`}`:this.message=`Http failure response for ${n.url||`(unknown url)`}: ${n.status} ${n.statusText}`,this.error=n.error||null}};var ON=200;var RN=/^\)\]\}',?\n/;var dC=new k(``,{factory:()=>null});var yu=(()=>{class t{fetchImpl=f(Sg,{optional:!0})?.fetch??((...e)=>globalThis.fetch(...e));ngZone=f(X);destroyRef=f(Ne);maxResponseSize=f(dC);handle(e){return new Ce(i=>{let a=new AbortController,o=!1,r={next:c=>{c.type===Eo.Response&&(o=!0),i.next(c)},error:c=>{o=!0,i.error(c)},complete:()=>{o=!0,i.complete()}};this.doRequest(e,a.signal,r).then(wg,c=>r.error(new Ei({error:c})));let s;return e.timeout&&(s=this.ngZone.runOutsideAngular(()=>setTimeout(()=>{a.signal.aborted||a.abort(new DOMException(`signal timed out`,`TimeoutError`))},e.timeout))),()=>{s!==void 0&&clearTimeout(s),!o&&!a.signal.aborted&&a.abort()}})}async doRequest(e,i,a){let o=this.createRequestInit(e),r$1;try{let P=this.ngZone.runOutsideAngular(()=>this.fetchImpl(e.urlWithParams,r({signal:i},o)));PN(P),a.next({type:Eo.Sent}),r$1=await P}catch(P){a.error(new Ei({error:P,status:P.status??0,statusText:P.statusText,url:e.urlWithParams,headers:P.headers}));return}let s=new Ji(r$1.headers),c=r$1.statusText,h=r$1.url||e.urlWithParams,p=r$1.status,g=null,v=e.reportProgress||e.reportDownloadProgress;if(v&&a.next(new _u({headers:s,status:p,statusText:c,url:h})),r$1.body){let P=r$1.headers.get(xg)??``,ae=r$1.headers.get(`content-length`),z=ae!==null?Number(ae):NaN;this.maxResponseSize!==null&&Number.isFinite(z)&&z>this.maxResponseSize&&(await r$1.body.cancel(),rC(this.maxResponseSize));let x=[],M=r$1.body.getReader(),O=0,$,_e,bt=typeof Zone<`u`&&Zone.current,We=!1;if(await this.ngZone.runOutsideAngular(async()=>{for(;;){if(this.destroyRef.destroyed){await M.cancel(),We=!0;break}let{done:Ie,value:F}=await M.read();if(Ie)break;if(x.push(F),O+=F.length,this.maxResponseSize!==null&&O>this.maxResponseSize&&(await M.cancel(),rC(this.maxResponseSize)),v){_e=e.responseType===`text`?(_e??``)+($??=sC(P)).decode(F,{stream:!0}):void 0;let qe=()=>a.next({type:Eo.DownloadProgress,total:Number.isFinite(z)?z:void 0,loaded:O,partialText:_e});bt?bt.run(qe):qe()}}}),We){a.complete();return}let pe=this.concatChunks(x,O);try{g=this.parseBody(e,pe,P,p)}catch(Ie){a.error(new Ei({error:Ie,headers:new Ji(r$1.headers),status:r$1.status,statusText:r$1.statusText,url:r$1.url||e.urlWithParams}));return}}p===0&&(p=g?ON:0);let b=p>=200&&p<300,y=r$1.redirected,E=r$1.type;b?(a.next(new xc({body:g,headers:s,status:p,statusText:c,url:h,redirected:y,responseType:E})),a.complete()):a.error(new Ei({error:g,headers:s,status:p,statusText:c,url:h,redirected:y,responseType:E}))}parseBody(e,i,a,o){switch(e.responseType){case`json`:let r=new TextDecoder().decode(i).replace(RN,``);if(r===``)return null;try{return JSON.parse(r)}catch(s){if(o<200||o>=300)return r;throw s}case`text`:return sC(a).decode(i);case`blob`:return new Blob([i],{type:a});case`arraybuffer`:return i.buffer}}createRequestInit(e){if(e.reportUploadProgress)throw new G(2824,!1);let i={},a;if(a=e.credentials,e.withCredentials&&(a=`include`),e.headers.forEach((o,r)=>i[o]=r.join(`,`)),e.headers.has(oC)||(i[oC]=AN),!e.headers.has(xg)){let o=e.detectContentTypeHeader();o!==null&&(i[xg]=o)}return{body:e.serializeBody(),method:e.method,headers:i,credentials:a,keepalive:e.keepalive,cache:e.cache,priority:e.priority,mode:e.mode,redirect:e.redirect,referrer:e.referrer,integrity:e.integrity,referrerPolicy:e.referrerPolicy}}concatChunks(e,i){let a=new Uint8Array(i),o=0;for(let r of e)a.set(r,o),o+=r.length;return a}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var Sg=class{};function wg(){}function PN(t){t.then(wg,wg)}function rC(t){throw new G(-2825,!1)}var FN=/charset=\s*["']?([^;"'\s]+)["']?/i;function sC(t){let n=t.match(FN);if(n!==null)try{return new TextDecoder(n[1])}catch{}return new TextDecoder}var LN=new k(``,{factory:()=>!0});var VN=`XSRF-TOKEN`;var BN=new k(``,{factory:()=>VN});var jN=`X-XSRF-TOKEN`;var zN=new k(``,{factory:()=>jN});var HN=(()=>{class t{cookieName=f(BN);doc=f(de);lastCookieString=``;lastToken=null;parseCount=0;getToken(){let e=this.doc.cookie||``;return e!==this.lastCookieString&&(this.parseCount++,this.lastToken=gc(e,this.cookieName),this.lastCookieString=e),this.lastToken}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var uC=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(HN),a},providedIn:`root`})}return t})();function mC(t,n){if(!f(LN)||t.method===`GET`||t.method===`HEAD`)return n(t);try{let a=f(Nr).href,{origin:o}=new URL(a),{origin:r}=new URL(t.url,o);if(o!==r)return n(t)}catch{return n(t)}let e=f(uC).getToken(),i=f(zN);return e!=null&&!t.headers.has(i)&&(t=t.clone({headers:t.headers.set(i,e)})),n(t)}function UN(t,n){return n(t)}function $N(t,n,e){return(i,a)=>or(e,()=>n(i,o=>t(o,a)))}var pC=new k(``,{factory:()=>[mC]});var hC=new k(``);var fC=new k(``,{factory:()=>!0});var Eg=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(yu),a},providedIn:`root`})}return t})();var xu=(()=>{class t{backend;injector;chain=null;pendingTasks=f(xa);contributeToStability=f(fC);constructor(e,i){this.backend=e,this.injector=i}handle(e){if(this.chain===null){let a=this.injector.get(Cu,null,{skipSelf:!0}),o=a!==null&&this.backend===a,r=this.injector.get(hC,[],o?{self:!0}:void 0),s=Array.from(new Set([...this.injector.get(pC),...r]));this.chain=s.reduceRight((c,h)=>$N(c,h,this.injector),UN)}let i=this.chain;if(this.contributeToStability){let a=this.pendingTasks.add();return nt(()=>i(e,o=>this.backend.handle(o))).pipe(sp(a))}else return nt(()=>i(e,a=>this.backend.handle(a)))}static ɵfac=function(i){return new(i||t)(Se(Eg),Se(kt))};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();var Cu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(xu),a},providedIn:`root`})}return t})();function Cg(t,n){return r({body:n},t)}var Di=(()=>{class t{handler;constructor(e){this.handler=e}request(e,i,a={}){let o;if(e instanceof Ar)o=e;else{let c;a.headers instanceof Ji?c=a.headers:c=new Ji(a.headers);let h;a.params&&(a.params instanceof Zi?h=a.params:h=new Zi({fromObject:a.params})),o=new Ar(e,i,a.body!==void 0?a.body:null,{headers:c,context:a.context,params:h,reportProgress:a.reportProgress,reportUploadProgress:a.reportUploadProgress,reportDownloadProgress:a.reportDownloadProgress,responseType:a.responseType||`json`,withCredentials:a.withCredentials,transferCache:a.transferCache,keepalive:a.keepalive,priority:a.priority,cache:a.cache,mode:a.mode,redirect:a.redirect,credentials:a.credentials,referrer:a.referrer,referrerPolicy:a.referrerPolicy,integrity:a.integrity,timeout:a.timeout})}let r=Mn(o).pipe(rp(c=>this.handler.handle(c)));if(e instanceof Ar||a.observe===`events`)return r;let s=r.pipe(Qe(c=>c instanceof xc));switch(a.observe||`body`){case`body`:switch(o.responseType){case`arraybuffer`:return s.pipe(st(c=>{if(c.body!==null&&!(c.body instanceof ArrayBuffer))throw new G(2806,!1);return c.body}));case`blob`:return s.pipe(st(c=>{if(c.body!==null&&!(c.body instanceof Blob))throw new G(2807,!1);return c.body}));case`text`:return s.pipe(st(c=>{if(c.body!==null&&typeof c.body!=`string`)throw new G(2808,!1);return c.body}));default:return s.pipe(st(c=>c.body))}case`response`:return s;default:throw new G(2809,!1)}}delete(e,i={}){return this.request(`DELETE`,e,i)}get(e,i={}){return this.request(`GET`,e,i)}head(e,i={}){return this.request(`HEAD`,e,i)}jsonp(e,i){return this.request(`JSONP`,e,{params:new Zi().append(i,`JSONP_CALLBACK`),observe:`body`,responseType:`json`})}options(e,i={}){return this.request(`OPTIONS`,e,i)}patch(e,i,a={}){return this.request(`PATCH`,e,Cg(a,i))}post(e,i,a={}){return this.request(`POST`,e,Cg(a,i))}put(e,i,a={}){return this.request(`PUT`,e,Cg(a,i))}static ɵfac=function(i){return new(i||t)(Se(Cu))};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();function Dg(...t){let n=[Di,yu,xu,{provide:Cu,useExisting:xu},{provide:Eg,useFactory:()=>f(yu)},{provide:pC,useValue:mC,multi:!0}];for(let e of t)n.push(...e.ɵproviders);return rn(n)}var Ig=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(GN),a},providedIn:`root`})}return t})();var GN=(()=>{class t extends Ig{_doc=f(de);sanitize(e,i){if(i==null)return null;switch(e){case xn.NONE:return i;case xn.HTML:return Sa(i,`HTML`)?ti(i):ff(this._doc,String(i)).toString();case xn.STYLE:return Sa(i,`Style`)?ti(i):i;case xn.SCRIPT:if(Sa(i,`Script`))return ti(i);throw new G(5200,!1);case xn.URL:return Sa(i,`URL`)?ti(i):Qs(String(i));case xn.RESOURCE_URL:if(Sa(i,`ResourceURL`))return ti(i);throw new G(-5201,!1);default:throw new G(5202,!1)}}bypassSecurityTrustHtml(e){return df(e)}bypassSecurityTrustStyle(e){return uf(e)}bypassSecurityTrustScript(e){return mf(e)}bypassSecurityTrustUrl(e){return pf(e)}bypassSecurityTrustResourceUrl(e){return hf(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var gC=new k(`HIGHCHARTS_LOADER`);var bC=new k(`HIGHCHARTS_ROOT_MODULES`);var vC=new k(`HIGHCHARTS_OPTIONS`);var WN=new k(`HIGHCHARTS_CONFIG`);var _C=new k(`HIGHCHARTS_TIMEOUT`);var qN=(()=>{class t{constructor(){this.highcharts=T(null),this.loader=f(gC),this.globalOptions=f(vC,{optional:!0}),this.globalModules=f(bC,{optional:!0}),this.sharedHighchartsPromise=null,this.moduleLoadCache=new WeakMap}async loadModules(e){if(!e)return;let i=this.moduleLoadCache.get(e);if(i)return i;let a=Promise.allSettled(e()).then(o=>{let r=o.filter(s=>s.status===`rejected`);if(r.length){let s=r.map(({reason:c})=>c instanceof Error?c.message:String(c));throw new Error(`Failed to load Highcharts modules: ${s.join(`; `)}`)}});return this.moduleLoadCache.set(e,a),a.catch(()=>{this.moduleLoadCache.get(e)===a&&this.moduleLoadCache.delete(e)}),a}async ensureSharedHighcharts(){if(!this.sharedHighchartsPromise){let e=(async()=>{let i=await this.loader();return await this.loadModules(this.globalModules),this.globalOptions&&i.setOptions(this.globalOptions),i})();this.sharedHighchartsPromise=e,e.catch(()=>{this.sharedHighchartsPromise===e&&(this.sharedHighchartsPromise=null)})}return this.sharedHighchartsPromise}async load(e){let i=await this.ensureSharedHighcharts();return await this.loadModules(e?.modules),this.highcharts.set(i),i}static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}}return t})();var YN=(()=>{class t{delay(e){return new Promise(i=>setTimeout(i,e))}getChartFactory(e,i){if(i===`chart`)return e.chart;let o=e[i];if(!o)throw new Error(`Highcharts constructor "${i}" is not available. Did you load the required module?`);return o}createChart(){Lt(e=>{let i=this.loadedHighcharts(),a=this.constructorType();if(!i||this.isDestroyed)return;let o=s=>{if(!(s.renderer.forExport||this.isDestroyed))return this.chartInstance.emit(s)},r=this.getChartFactory(i,a)(this.el.nativeElement,nt(()=>this.options()),o);this.chart.set(r),e(()=>{this.chart()===r&&this.chart.set(null),r.destroy()})})}keepChartUpToDate(){let e=null;Lt(()=>{let i=this.chart(),a=this.update(),o=this.oneToOne(),r=this.options();if(i){if(i!==e){e=i;return}a&&i.update(r,!0,o)}})}async initializeHighcharts(){await this.pendingTasks.run(async()=>{try{let e=await this.highchartsChartService.load(this.relativeConfig),i=this.relativeConfig?.timeout??this.timeout??0;i>0&&await this.delay(i),this.isDestroyed||this.loadedHighcharts.set(e)}catch(e){console.error(`Highcharts failed to load; chart was not created.`,e)}})}constructor(){this.constructorType=zt(`chart`),this.oneToOne=zt(!1),this.options=zt.required(),this.update=_x(!0),this.chartInstance=eu(),this.destroyRef=f(Ne),this.el=f(W),this.platformId=f(Gi),this.relativeConfig=f(WN,{optional:!0}),this.timeout=f(_C,{optional:!0}),this.highchartsChartService=f(qN),this.pendingTasks=f(xa),this.loadedHighcharts=T(null),this.chart=T(null),this.isDestroyed=!1,!(this.platformId&&qx(this.platformId))&&(this.destroyRef.onDestroy(()=>{this.isDestroyed=!0}),this.createChart(),this.keepChartUpToDate(),this.initializeHighcharts())}static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵdir=K({type:t,selectors:[[``,`highchartsChart`,``]],inputs:{constructorType:[1,`constructorType`],oneToOne:[1,`oneToOne`],options:[1,`options`],update:[1,`update`]},outputs:{update:`updateChange`,chartInstance:`chartInstance`}})}}return t})();var yC=(()=>{class t{static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵcmp=B({type:t,selectors:[[`highcharts-chart`]],features:[ic([{directive:YN,inputs:[`constructorType`,`constructorType`,`oneToOne`,`oneToOne`,`options`,`options`,`update`,`update`],outputs:[`chartInstance`,`chartInstance`,`updateChange`,`updateChange`]}])],decls:0,vars:0,template:function(i,a){},encapsulation:2})}}return t})();var KN=()=>[];var QN=()=>import(`./chunk-CnG5sHWW.js`).then(t=>t.default);function XN(t){return rn([{provide:gC,useValue:t??QN}])}function ZN(t){return rn([{provide:vC,useValue:t}])}function JN(t){return rn([{provide:bC,useValue:t}])}function xC(t={}){let n=[XN(t.instance),JN(t.modules??KN),{provide:_C,useValue:t.timeout}];return t.options&&n.push(ZN(t.options)),rn(n)}var CC={providers:[sh(),Zf(),Dg(),xC({instance:()=>import(`./chunk-CnG5sHWW.js`).then(t=>t.default),modules:()=>[Promise.all([import(`./chunk-BhbWCxAJ.js`).then(()=>import(`./chunk-DKkuCsgA.js`)),import(`./chunk-BMpKz9ze.js`),import(`./chunk-DpBPsi9B.js`),import(`./chunk-CNFUSMNV.js`),import(`./chunk-CeD8FXgU.js`)]).then(()=>import(`./chunk-B-qrK9d1.js`))]})]};function Do(t){return t.buttons===0||t.detail===0}function Io(t){let n=t.touches&&t.touches[0]||t.changedTouches&&t.changedTouches[0];return!!n&&n.identifier===-1&&(n.radiusX==null||n.radiusX===1)&&(n.radiusY==null||n.radiusY===1)}var kg;function SC(){if(kg==null){let t=typeof document<`u`?document.head:null;kg=!!(t&&(t.createShadowRoot||t.attachShadow))}return kg}function Mg(t){if(SC()){let n=t.getRootNode?t.getRootNode():null;if(typeof ShadowRoot<`u`&&ShadowRoot&&n instanceof ShadowRoot)return n}return null}function Cc(){let t=typeof document<`u`&&document?document.activeElement:null;for(;t&&t.shadowRoot;){let n=t.shadowRoot.activeElement;if(n===t)break;t=n}return t}function Yt(t){if(t.composedPath)try{return t.composedPath()[0]}catch{}return t.target}var Tg;try{Tg=typeof Intl<`u`&&Intl.v8BreakIterator}catch{Tg=!1}var Oe=(()=>{class t{_platformId=f(Gi);isBrowser=this._platformId?Wx(this._platformId):typeof document==`object`&&!!document;EDGE=this.isBrowser&&/(edge)/i.test(navigator.userAgent);TRIDENT=this.isBrowser&&/(msie|trident)/i.test(navigator.userAgent);BLINK=this.isBrowser&&!!(window.chrome||Tg)&&typeof CSS<`u`&&!this.EDGE&&!this.TRIDENT;WEBKIT=this.isBrowser&&/AppleWebKit/i.test(navigator.userAgent)&&!this.BLINK&&!this.EDGE&&!this.TRIDENT;IOS=this.isBrowser&&/iPad|iPhone|iPod/.test(navigator.userAgent)&&!(`MSStream`in window);FIREFOX=this.isBrowser&&/(firefox|minefield)/i.test(navigator.userAgent);ANDROID=this.isBrowser&&/android/i.test(navigator.userAgent)&&!this.TRIDENT;SAFARI=this.isBrowser&&/safari/i.test(navigator.userAgent)&&this.WEBKIT;static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var Sc;function wC(){if(Sc==null&&typeof window<`u`)try{window.addEventListener(`test`,null,Object.defineProperty({},"passive",{get:()=>Sc=!0}))}finally{Sc=Sc||!1}return Sc}function Rr(t){return wC()?t:!!t.capture}function ri(t,n=0){return EC(t)?Number(t):arguments.length===2?n:0}function EC(t){return!isNaN(parseFloat(t))&&!isNaN(Number(t))}function ln(t){return t instanceof W?t.nativeElement:t}var DC=new k(`cdk-input-modality-detector-options`);var IC={ignoreKeys:[18,17,224,91,16]};var kC=650;var Ng={passive:!0,capture:!0};var MC=(()=>{class t{_platform=f(Oe);_listenerCleanups;modalityDetected;modalityChanged;get mostRecentModality(){return this._modality.value}_mostRecentTarget=null;_modality=new hi(null);_options;_lastTouchMs=0;_onKeydown=e=>{this._options?.ignoreKeys?.some(i=>i===e.keyCode)||(this._modality.next(`keyboard`),this._mostRecentTarget=Yt(e))};_onMousedown=e=>{Date.now()-this._lastTouchMs<kC||(this._modality.next(Do(e)?`keyboard`:`mouse`),this._mostRecentTarget=Yt(e))};_onTouchstart=e=>{if(Io(e)){this._modality.next(`keyboard`);return}this._lastTouchMs=Date.now(),this._modality.next(`touch`),this._mostRecentTarget=Yt(e)};constructor(){let e=f(X),i=f(de),a=f(DC,{optional:!0});if(this._options=r(r({},IC),a),this.modalityDetected=this._modality.pipe(Qa(1)),this.modalityChanged=this.modalityDetected.pipe(Rl()),this._platform.isBrowser){let o=f(Tt).createRenderer(null,null);this._listenerCleanups=e.runOutsideAngular(()=>[o.listen(i,`keydown`,this._onKeydown,Ng),o.listen(i,`mousedown`,this._onMousedown,Ng),o.listen(i,`touchstart`,this._onTouchstart,Ng)])}}ngOnDestroy(){this._modality.complete(),this._listenerCleanups?.forEach(e=>e())}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var wc=(function(t){return t[t.IMMEDIATE=0]=`IMMEDIATE`,t[t.EVENTUAL=1]=`EVENTUAL`,t})(wc||{});var TC=new k(`cdk-focus-monitor-default-options`);var Su=Rr({passive:!0,capture:!0});var Jt=(()=>{class t{_ngZone=f(X);_platform=f(Oe);_inputModalityDetector=f(MC);_origin=null;_lastFocusOrigin=null;_windowFocused=!1;_windowFocusTimeoutId;_originTimeoutId;_originFromTouchInteraction=!1;_elementInfo=new Map;_monitoredElementCount=0;_rootNodeFocusListenerCount=new Map;_detectionMode;_windowFocusListener=()=>{this._windowFocused=!0,this._windowFocusTimeoutId=setTimeout(()=>this._windowFocused=!1)};_document=f(de);_stopInputModalityDetector=new L;constructor(){let e=f(TC,{optional:!0});this._detectionMode=e?.detectionMode||wc.IMMEDIATE}_rootNodeFocusAndBlurListener=e=>{let i=Yt(e);for(let a=i;a;a=a.parentElement)e.type===`focus`?this._onFocus(e,a):this._onBlur(e,a)};monitor(e,i=!1){let a=ln(e);if(!this._platform.isBrowser||a.nodeType!==1)return Mn();let o=Mg(a)||this._document,r=this._elementInfo.get(a);if(r)return i&&(r.checkChildren=!0),r.subject;let s={checkChildren:i,subject:new L,rootNode:o};return this._elementInfo.set(a,s),this._registerGlobalListeners(s),s.subject}stopMonitoring(e){let i=ln(e),a=this._elementInfo.get(i);a&&(a.subject.complete(),this._setClasses(i),this._elementInfo.delete(i),this._removeGlobalListeners(a))}focusVia(e,i,a){let o=ln(e);o===this._document.activeElement?this._getClosestElementsInfo(o).forEach(([s,c])=>this._originChanged(s,i,c)):(this._setOrigin(i),typeof o.focus==`function`&&o.focus(a))}ngOnDestroy(){this._elementInfo.forEach((e,i)=>this.stopMonitoring(i))}_getWindow(){return this._document.defaultView||window}_getFocusOrigin(e){return this._origin?this._originFromTouchInteraction?this._shouldBeAttributedToTouch(e)?`touch`:`program`:this._origin:this._windowFocused&&this._lastFocusOrigin?this._lastFocusOrigin:e&&this._isLastInteractionFromInputLabel(e)?`mouse`:`program`}_shouldBeAttributedToTouch(e){return this._detectionMode===wc.EVENTUAL||!!e?.contains(this._inputModalityDetector._mostRecentTarget)}_setClasses(e,i){e.classList.toggle(`cdk-focused`,!!i),e.classList.toggle(`cdk-touch-focused`,i===`touch`),e.classList.toggle(`cdk-keyboard-focused`,i===`keyboard`),e.classList.toggle(`cdk-mouse-focused`,i===`mouse`),e.classList.toggle(`cdk-program-focused`,i===`program`)}_setOrigin(e,i=!1){this._ngZone.runOutsideAngular(()=>{if(this._origin=e,this._originFromTouchInteraction=e===`touch`&&i,this._detectionMode===wc.IMMEDIATE){clearTimeout(this._originTimeoutId);let a=this._originFromTouchInteraction?kC:1;this._originTimeoutId=setTimeout(()=>this._origin=null,a)}})}_onFocus(e,i){let a=this._elementInfo.get(i),o=Yt(e);!a||!a.checkChildren&&i!==o||this._originChanged(i,this._getFocusOrigin(o),a)}_onBlur(e,i){let a=this._elementInfo.get(i);!a||a.checkChildren&&e.relatedTarget instanceof Node&&i.contains(e.relatedTarget)||(this._setClasses(i),this._emitOrigin(a,null))}_emitOrigin(e,i){e.subject.observers.length&&this._ngZone.run(()=>e.subject.next(i))}_registerGlobalListeners(e){if(!this._platform.isBrowser)return;let i=e.rootNode,a=this._rootNodeFocusListenerCount.get(i)||0;a||this._ngZone.runOutsideAngular(()=>{i.addEventListener(`focus`,this._rootNodeFocusAndBlurListener,Su),i.addEventListener(`blur`,this._rootNodeFocusAndBlurListener,Su)}),this._rootNodeFocusListenerCount.set(i,a+1),++this._monitoredElementCount===1&&(this._ngZone.runOutsideAngular(()=>{this._getWindow().addEventListener(`focus`,this._windowFocusListener)}),this._inputModalityDetector.modalityDetected.pipe(Te(this._stopInputModalityDetector)).subscribe(o=>{this._setOrigin(o,!0)}))}_removeGlobalListeners(e){let i=e.rootNode;if(this._rootNodeFocusListenerCount.has(i)){let a=this._rootNodeFocusListenerCount.get(i);a>1?this._rootNodeFocusListenerCount.set(i,a-1):(i.removeEventListener(`focus`,this._rootNodeFocusAndBlurListener,Su),i.removeEventListener(`blur`,this._rootNodeFocusAndBlurListener,Su),this._rootNodeFocusListenerCount.delete(i))}--this._monitoredElementCount||(this._getWindow().removeEventListener(`focus`,this._windowFocusListener),this._stopInputModalityDetector.next(),clearTimeout(this._windowFocusTimeoutId),clearTimeout(this._originTimeoutId))}_originChanged(e,i,a){this._setClasses(e,i),this._emitOrigin(a,i),this._lastFocusOrigin=i}_getClosestElementsInfo(e){let i=[];return this._elementInfo.forEach((a,o)=>{(o===e||a.checkChildren&&o.contains(e))&&i.push([o,a])}),i}_isLastInteractionFromInputLabel(e){let{_mostRecentTarget:i,mostRecentModality:a}=this._inputModalityDetector;if(a!==`mouse`||!i||i===e||e.nodeName!==`INPUT`&&e.nodeName!==`TEXTAREA`||e.disabled)return!1;let o=e.labels;if(o){for(let r=0;r<o.length;r++)if(o[r].contains(i))return!0}return!1}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var Ag=(()=>{class t{_elementRef=f(W);_focusMonitor=f(Jt);_monitorSubscription;_focusOrigin=null;cdkFocusChange=new ee;get focusOrigin(){return this._focusOrigin}ngAfterViewInit(){let e=this._elementRef.nativeElement;this._monitorSubscription=this._focusMonitor.monitor(e,e.nodeType===1&&e.hasAttribute(`cdkMonitorSubtreeFocus`)).subscribe(i=>{this._focusOrigin=i,this.cdkFocusChange.emit(i)})}ngOnDestroy(){this._focusMonitor.stopMonitoring(this._elementRef),this._monitorSubscription?.unsubscribe()}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`cdkMonitorElementFocus`,``],[``,`cdkMonitorSubtreeFocus`,``]],outputs:{cdkFocusChange:`cdkFocusChange`},exportAs:[`cdkMonitorFocus`]})}return t})();var wu=new WeakMap;var Je=(()=>{class t{_appRef;_injector=f(se);_environmentInjector=f(kt);load(e){let i=this._appRef=this._appRef||this._injector.get(Rn),a=wu.get(i);a||(a={loaders:new Set,refs:[]},wu.set(i,a),i.onDestroy(()=>{wu.get(i)?.refs.forEach(o=>o.destroy()),wu.delete(i)})),a.loaders.has(e)||(a.loaders.add(e),a.refs.push(tu(e,{environmentInjector:this._environmentInjector})))}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var Ii=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],exportAs:[`cdkVisuallyHidden`],decls:0,vars:0,template:function(i,a){},styles:[`.cdk-visually-hidden {
  border: 0;
  clip: rect(0 0 0 0);
  height: 1px;
  margin: -1px;
  overflow: hidden;
  padding: 0;
  position: absolute;
  width: 1px;
  white-space: nowrap;
  outline: 0;
  -webkit-appearance: none;
  -moz-appearance: none;
  left: 0;
}
[dir=rtl] .cdk-visually-hidden {
  left: auto;
  right: 0;
}
`],encapsulation:2})}return t})();var Eu;function eA(){if(Eu===void 0&&(Eu=null,typeof window<`u`)){let t=window;if(t.trustedTypes!==void 0)try{Eu=t.trustedTypes.createPolicy(`angular#components`,{createHTML:n=>n})}catch(n){console.error(n)}}return Eu}function tA(t){return eA()?.createHTML(t)||t}function NC(t,n,e){t.innerHTML=tA(e.sanitize(xn.HTML,n)||``)}function Pr(t){return Array.isArray(t)?t:[t]}var AC=new Set;var ko;var Fr=(()=>{class t{_platform=f(Oe);_nonce=f(ya,{optional:!0});_matchMedia;constructor(){this._matchMedia=this._platform.isBrowser&&window.matchMedia?window.matchMedia.bind(window):iA}matchMedia(e){return(this._platform.WEBKIT||this._platform.BLINK)&&nA(e,this._nonce),this._matchMedia(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();function nA(t,n){if(!AC.has(t))try{ko||(ko=document.createElement(`style`),n&&ko.setAttribute(`nonce`,n),ko.setAttribute(`type`,`text/css`),document.head.appendChild(ko)),ko.sheet&&(ko.sheet.insertRule(`@media ${t.replace(/[{}]/g,``)} {body{ }}`,0),AC.add(t))}catch(e){console.error(e)}}function iA(t){return{matches:t===`all`||t===``,media:t,addListener:()=>{},removeListener:()=>{}}}var Og=(()=>{class t{_mediaMatcher=f(Fr);_zone=f(X);_queries=new Map;_destroySubject=new L;ngOnDestroy(){this._destroySubject.next(),this._destroySubject.complete()}isMatched(e){return OC(Pr(e)).some(a=>this._registerQuery(a).mql.matches)}observe(e){let o=ap(OC(Pr(e)).map(r=>this._registerQuery(r).observable));return o=er(o.pipe(vn(1)),o.pipe(Qa(1),Pi(0))),o.pipe(st(r=>{let s={matches:!1,breakpoints:{}};return r.forEach(({matches:c,query:h})=>{s.matches=s.matches||c,s.breakpoints[h]=c}),s}))}_registerQuery(e){if(this._queries.has(e))return this._queries.get(e);let i=this._mediaMatcher.matchMedia(e),o={observable:new Ce(r=>{let s=c=>this._zone.run(()=>r.next(c));return i.addListener(s),()=>{i.removeListener(s)}}).pipe(mt(i),st(({matches:r})=>({query:e,matches:r})),Te(this._destroySubject)),mql:i};return this._queries.set(e,o),o}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();function OC(t){return t.map(n=>n.split(`,`)).reduce((n,e)=>n.concat(e)).map(n=>n.trim())}function aA(t){if(t.type===`characterData`&&t.target instanceof Comment)return!0;if(t.type===`childList`){for(let n=0;n<t.addedNodes.length;n++)if(!(t.addedNodes[n]instanceof Comment))return!1;for(let n=0;n<t.removedNodes.length;n++)if(!(t.removedNodes[n]instanceof Comment))return!1;return!0}return!1}var RC=(()=>{class t{create(e){return typeof MutationObserver>`u`?null:new MutationObserver(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var PC=(()=>{class t{_mutationObserverFactory=f(RC);_observedElements=new Map;_ngZone=f(X);ngOnDestroy(){this._observedElements.forEach((e,i)=>this._cleanupObserver(i))}observe(e){let i=ln(e);return new Ce(a=>{let r=this._observeElement(i).pipe(st(s=>s.filter(c=>!aA(c))),Qe(s=>!!s.length)).subscribe(s=>{this._ngZone.run(()=>{a.next(s)})});return()=>{r.unsubscribe(),this._unobserveElement(i)}})}_observeElement(e){return this._ngZone.runOutsideAngular(()=>{if(this._observedElements.has(e))this._observedElements.get(e).count++;else{let i=new L,a=this._mutationObserverFactory.create(o=>i.next(o));a&&a.observe(e,{characterData:!0,childList:!0,subtree:!0}),this._observedElements.set(e,{observer:a,stream:i,count:1})}return this._observedElements.get(e).stream})}_unobserveElement(e){this._observedElements.has(e)&&(this._observedElements.get(e).count--,this._observedElements.get(e).count||this._cleanupObserver(e))}_cleanupObserver(e){if(this._observedElements.has(e)){let{observer:i,stream:a}=this._observedElements.get(e);i&&i.disconnect(),a.complete(),this._observedElements.delete(e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var FC=(()=>{class t{_contentObserver=f(PC);_elementRef=f(W);event=new ee;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._disabled?this._unsubscribe():this._subscribe()}_disabled=!1;get debounce(){return this._debounce}set debounce(e){this._debounce=ri(e),this._subscribe()}_debounce;_currentSubscription=null;ngAfterContentInit(){!this._currentSubscription&&!this.disabled&&this._subscribe()}ngOnDestroy(){this._unsubscribe()}_subscribe(){this._unsubscribe();let e=this._contentObserver.observe(this._elementRef);this._currentSubscription=(this.debounce?e.pipe(Pi(this.debounce)):e).subscribe(this.event)}_unsubscribe(){this._currentSubscription?.unsubscribe()}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`cdkObserveContent`,``]],inputs:{disabled:[2,`cdkObserveContentDisabled`,`disabled`,V],debounce:`debounce`},outputs:{event:`cdkObserveContent`},exportAs:[`cdkObserveContent`]})}return t})();var Du=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({providers:[RC]})}return t})();var Lr=(()=>{class t{_platform=f(Oe);isDisabled(e){return e.hasAttribute(`disabled`)}isVisible(e){return rA(e)&&getComputedStyle(e).visibility===`visible`}isTabbable(e){if(!this._platform.isBrowser)return!1;let i=oA(hA(e));if(i&&(LC(i)===-1||!this.isVisible(i)))return!1;let a=e.nodeName.toLowerCase(),o=LC(e);return e.hasAttribute(`contenteditable`)?o!==-1:a===`iframe`||a===`object`||this._platform.WEBKIT&&this._platform.IOS&&!mA(e)?!1:a===`audio`?e.hasAttribute(`controls`)?o!==-1:!1:a===`video`?o===-1?!1:o!==null?!0:this._platform.FIREFOX||e.hasAttribute(`controls`):e.tabIndex>=0}isFocusable(e,i){return pA(e)&&!this.isDisabled(e)&&(i?.ignoreVisibility||this.isVisible(e))}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();function oA(t){try{return t.frameElement}catch{return null}}function rA(t){return!!(t.offsetWidth||t.offsetHeight||typeof t.getClientRects==`function`&&t.getClientRects().length)}function sA(t){let n=t.nodeName.toLowerCase();return n===`input`||n===`select`||n===`button`||n===`textarea`}function cA(t){return dA(t)&&t.type==`hidden`}function lA(t){return uA(t)&&t.hasAttribute(`href`)}function dA(t){return t.nodeName.toLowerCase()==`input`}function uA(t){return t.nodeName.toLowerCase()==`a`}function jC(t){if(!t.hasAttribute(`tabindex`)||t.tabIndex===void 0)return!1;let n=t.getAttribute(`tabindex`);return!!(n&&!isNaN(parseInt(n,10)))}function LC(t){if(!jC(t))return null;let n=parseInt(t.getAttribute(`tabindex`)||``,10);return isNaN(n)?-1:n}function mA(t){let n=t.nodeName.toLowerCase(),e=n===`input`&&t.type;return e===`text`||e===`password`||n===`select`||n===`textarea`}function pA(t){return cA(t)?!1:sA(t)||lA(t)||t.hasAttribute(`contenteditable`)||jC(t)}function hA(t){return t.ownerDocument&&t.ownerDocument.defaultView||window}var Iu=class{_element;_checker;_ngZone;_document;_injector;_startAnchor=null;_endAnchor=null;_hasAttached=!1;startAnchorListener=()=>{!this.focusLastTabbableElement()&&this._checker.isFocusable(this._element)&&this._element.focus()};endAnchorListener=()=>{!this.focusFirstTabbableElement()&&this._checker.isFocusable(this._element)&&this._element.focus()};get enabled(){return this._enabled}set enabled(n){this._enabled=n,this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_enabled=!0;constructor(n,e,i,a,o=!1,r){this._element=n,this._checker=e,this._ngZone=i,this._document=a,this._injector=r,o||this.attachAnchors()}destroy(){let n=this._startAnchor,e=this._endAnchor;n&&(n.removeEventListener(`focus`,this.startAnchorListener),n.remove()),e&&(e.removeEventListener(`focus`,this.endAnchorListener),e.remove()),this._startAnchor=this._endAnchor=null,this._hasAttached=!1}attachAnchors(){return this._hasAttached?!0:(this._ngZone.runOutsideAngular(()=>{this._startAnchor||(this._startAnchor=this._createAnchor(),this._startAnchor.addEventListener(`focus`,this.startAnchorListener)),this._endAnchor||(this._endAnchor=this._createAnchor(),this._endAnchor.addEventListener(`focus`,this.endAnchorListener))}),this._element.parentNode&&(this._element.parentNode.insertBefore(this._startAnchor,this._element),this._element.parentNode.insertBefore(this._endAnchor,this._element.nextSibling),this._hasAttached=!0),this._hasAttached)}focusInitialElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusInitialElement(n)))})}focusFirstTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusFirstTabbableElement(n)))})}focusLastTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusLastTabbableElement(n)))})}_getRegionBoundary(n){let e=this._element.querySelectorAll(`[cdk-focus-region-${n}], [cdkFocusRegion${n}], [cdk-focus-${n}]`);return n==`start`?e.length?e[0]:this._getFirstTabbableElement(this._element):e.length?e[e.length-1]:this._getLastTabbableElement(this._element)}focusInitialElement(n){let e=this._element.querySelector(`[cdk-focus-initial], [cdkFocusInitial]`);if(e){if(!this._checker.isFocusable(e)){let i=this._getFirstTabbableElement(e);return i?.focus(n),!!i}return e.focus(n),!0}return this.focusFirstTabbableElement(n)}focusFirstTabbableElement(n){let e=this._getRegionBoundary(`start`);return e&&e.focus(n),!!e}focusLastTabbableElement(n){let e=this._getRegionBoundary(`end`);return e&&e.focus(n),!!e}hasAttached(){return this._hasAttached}_getFirstTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let i=0;i<e.length;i++){let a=e[i].nodeType===this._document.ELEMENT_NODE?this._getFirstTabbableElement(e[i]):null;if(a)return a}return null}_getLastTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let i=e.length-1;i>=0;i--){let a=e[i].nodeType===this._document.ELEMENT_NODE?this._getLastTabbableElement(e[i]):null;if(a)return a}return null}_createAnchor(){let n=this._document.createElement(`div`);return this._toggleAnchorTabIndex(this._enabled,n),n.classList.add(`cdk-visually-hidden`),n.classList.add(`cdk-focus-trap-anchor`),n.setAttribute(`aria-hidden`,`true`),n}_toggleAnchorTabIndex(n,e){n?e.setAttribute(`tabindex`,`0`):e.removeAttribute(`tabindex`)}toggleAnchors(n){this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_executeOnStable(n){At(n,{injector:this._injector})}};var Pg=(()=>{class t{_checker=f(Lr);_ngZone=f(X);_document=f(de);_injector=f(se);constructor(){f(Je).load(Ii)}create(e,i=!1){return new Iu(e,this._checker,this._ngZone,this._document,i,this._injector)}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var zC=new k(`liveAnnouncerElement`,{providedIn:`root`,factory:()=>null});var HC=new k(`LIVE_ANNOUNCER_DEFAULT_OPTIONS`);var fA=0;var Fg=(()=>{class t{_ngZone=f(X);_defaultOptions=f(HC,{optional:!0});_liveElement;_document=f(de);_sanitizer=f(Ig);_previousTimeout;_currentPromise;_currentResolve;constructor(){let e=f(zC,{optional:!0});this._liveElement=e||this._createLiveElement()}announce(e,...i){let a=this._defaultOptions,o,r;return i.length===1&&typeof i[0]==`number`?r=i[0]:[o,r]=i,this.clear(),clearTimeout(this._previousTimeout),o||(o=a&&a.politeness?a.politeness:`polite`),r==null&&a&&(r=a.duration),this._liveElement.setAttribute(`aria-live`,o),this._liveElement.id&&this._exposeAnnouncerToModals(this._liveElement.id),this._ngZone.runOutsideAngular(()=>(this._currentPromise||(this._currentPromise=new Promise(s=>this._currentResolve=s)),clearTimeout(this._previousTimeout),this._previousTimeout=setTimeout(()=>{!e||typeof e==`string`?this._liveElement.textContent=e:NC(this._liveElement,e,this._sanitizer),typeof r==`number`&&(this._previousTimeout=setTimeout(()=>this.clear(),r)),this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0},100),this._currentPromise))}clear(){this._liveElement&&(this._liveElement.textContent=``)}ngOnDestroy(){clearTimeout(this._previousTimeout),this._liveElement?.remove(),this._liveElement=null,this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0}_createLiveElement(){let e=`cdk-live-announcer-element`,i=this._document.getElementsByClassName(e),a=this._document.createElement(`div`);for(let o=0;o<i.length;o++)i[o].remove();return a.classList.add(e),a.classList.add(`cdk-visually-hidden`),a.setAttribute(`aria-atomic`,`true`),a.setAttribute(`aria-live`,`polite`),a.id=`cdk-live-announcer-${fA++}`,this._document.body.appendChild(a),a}_exposeAnnouncerToModals(e){let i=this._document.querySelectorAll(`body > .cdk-overlay-container [aria-modal="true"]`);for(let a=0;a<i.length;a++){let o=i[a],r=o.getAttribute(`aria-owns`);r?r.indexOf(e)===-1&&o.setAttribute(`aria-owns`,r+` `+e):o.setAttribute(`aria-owns`,e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var Da=(function(t){return t[t.NONE=0]=`NONE`,t[t.BLACK_ON_WHITE=1]=`BLACK_ON_WHITE`,t[t.WHITE_ON_BLACK=2]=`WHITE_ON_BLACK`,t})(Da||{});var VC=`cdk-high-contrast-black-on-white`;var BC=`cdk-high-contrast-white-on-black`;var Rg=`cdk-high-contrast-active`;var UC=(()=>{class t{_platform=f(Oe);_hasCheckedHighContrastMode=!1;_document=f(de);_breakpointSubscription;constructor(){this._breakpointSubscription=f(Og).observe(`(forced-colors: active)`).subscribe(()=>{this._hasCheckedHighContrastMode&&(this._hasCheckedHighContrastMode=!1,this._applyBodyHighContrastModeCssClasses())})}getHighContrastMode(){if(!this._platform.isBrowser)return Da.NONE;let e=this._document.createElement(`div`);e.style.backgroundColor=`rgb(1,2,3)`,e.style.position=`absolute`,this._document.body.appendChild(e);let i=this._document.defaultView||window,a=i&&i.getComputedStyle?i.getComputedStyle(e):null,o=(a&&a.backgroundColor||``).replace(/ /g,``);switch(e.remove(),o){case`rgb(0,0,0)`:case`rgb(45,50,54)`:case`rgb(32,32,32)`:return Da.WHITE_ON_BLACK;case`rgb(255,255,255)`:case`rgb(255,250,239)`:return Da.BLACK_ON_WHITE}return Da.NONE}ngOnDestroy(){this._breakpointSubscription.unsubscribe()}_applyBodyHighContrastModeCssClasses(){if(!this._hasCheckedHighContrastMode&&this._platform.isBrowser&&this._document.body){let e=this._document.body.classList;e.remove(Rg,VC,BC),this._hasCheckedHighContrastMode=!0;let i=this.getHighContrastMode();i===Da.BLACK_ON_WHITE?e.add(Rg,VC):i===Da.WHITE_ON_BLACK&&e.add(Rg,BC)}}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var Mo=(()=>{class t{constructor(){f(UC)._applyBodyHighContrastModeCssClasses()}static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[Du]})}return t})();var gA=200;var ku=class{_letterKeyStream=new L;_items=[];_selectedItemIndex=-1;_pressedLetters=[];_skipPredicateFn;_selectedItem=new L;selectedItem=this._selectedItem;constructor(n,e){let i=typeof e?.debounceInterval==`number`?e.debounceInterval:gA;e?.skipPredicate&&(this._skipPredicateFn=e.skipPredicate),this.setItems(n),this._setupKeyHandler(i)}destroy(){this._pressedLetters=[],this._letterKeyStream.complete(),this._selectedItem.complete()}setCurrentSelectedItemIndex(n){this._selectedItemIndex=n}setItems(n){this._items=n}handleKey(n){let e=n.keyCode;n.key&&n.key.length===1?this._letterKeyStream.next(n.key.toLocaleUpperCase()):(e>=65&&e<=90||e>=48&&e<=57)&&this._letterKeyStream.next(String.fromCharCode(e))}isTyping(){return this._pressedLetters.length>0}reset(){this._pressedLetters=[]}_setupKeyHandler(n){this._letterKeyStream.pipe(dp(e=>this._pressedLetters.push(e)),Pi(n),Qe(()=>this._pressedLetters.length>0),st(()=>this._pressedLetters.join(``).toLocaleUpperCase())).subscribe(e=>{for(let i=1;i<this._items.length+1;i++){let a=(this._selectedItemIndex+i)%this._items.length,o=this._items[a];if(!this._skipPredicateFn?.(o)&&o.getLabel?.().toLocaleUpperCase().trim().indexOf(e)===0){this._selectedItem.next(o);break}}this._pressedLetters=[]})}};function xt(t,...n){return n.length?n.some(e=>t[e]):t.altKey||t.shiftKey||t.ctrlKey||t.metaKey}var Vr=class{_items;_activeItemIndex=T(-1);_activeItem=T(null);_wrap=!1;_typeaheadSubscription=De.EMPTY;_itemChangesSubscription;_vertical=!0;_horizontal=null;_allowedModifierKeys=[];_homeAndEnd=!1;_pageUpAndDown={enabled:!1,delta:10};_effectRef;_typeahead;_skipPredicateFn=n=>n.disabled;constructor(n,e){this._items=n,n instanceof wn?this._itemChangesSubscription=n.changes.subscribe(i=>this._itemsChanged(i.toArray())):sn(n)&&(this._effectRef=Lt(()=>this._itemsChanged(n()),{injector:e}))}tabOut=new L;change=new L;skipPredicate(n){return this._skipPredicateFn=n,this}withWrap(n=!0){return this._wrap=n,this}withVerticalOrientation(n=!0){return this._vertical=n,this}withHorizontalOrientation(n){return this._horizontal=n,this}withAllowedModifierKeys(n){return this._allowedModifierKeys=n,this}withTypeAhead(n=200){this._typeaheadSubscription.unsubscribe();let e=this._getItemsArray();return this._typeahead=new ku(e,{debounceInterval:typeof n==`number`?n:void 0,skipPredicate:i=>this._skipPredicateFn(i)}),this._typeaheadSubscription=this._typeahead.selectedItem.subscribe(i=>{this.setActiveItem(i)}),this}cancelTypeahead(){return this._typeahead?.reset(),this}withHomeAndEnd(n=!0){return this._homeAndEnd=n,this}withPageUpDown(n=!0,e=10){return this._pageUpAndDown={enabled:n,delta:e},this}setActiveItem(n){let e=this._activeItem();this.updateActiveItem(n),this._activeItem()!==e&&this.change.next(this._activeItemIndex())}onKeydown(n){let e=n.keyCode,a=[`altKey`,`ctrlKey`,`metaKey`,`shiftKey`].every(o=>!n[o]||this._allowedModifierKeys.indexOf(o)>-1);switch(e){case 9:this.tabOut.next();return;case 40:if(this._vertical&&a){this.setNextItemActive();break}else return;case 38:if(this._vertical&&a){this.setPreviousItemActive();break}else return;case 39:if(this._horizontal&&a){this._horizontal===`rtl`?this.setPreviousItemActive():this.setNextItemActive();break}else return;case 37:if(this._horizontal&&a){this._horizontal===`rtl`?this.setNextItemActive():this.setPreviousItemActive();break}else return;case 36:if(this._homeAndEnd&&a){this.setFirstItemActive();break}else return;case 35:if(this._homeAndEnd&&a){this.setLastItemActive();break}else return;case 33:if(this._pageUpAndDown.enabled&&a){let o=this._activeItemIndex()-this._pageUpAndDown.delta;this._setActiveItemByIndex(o>0?o:0,1);break}else return;case 34:if(this._pageUpAndDown.enabled&&a){let o=this._activeItemIndex()+this._pageUpAndDown.delta,r=this._getItemsArray().length;this._setActiveItemByIndex(o<r?o:r-1,-1);break}else return;default:(a||xt(n,`shiftKey`))&&this._typeahead?.handleKey(n);return}this._typeahead?.reset(),n.preventDefault()}get activeItemIndex(){return this._activeItemIndex()}get activeItem(){return this._activeItem()}isTyping(){return!!this._typeahead&&this._typeahead.isTyping()}setFirstItemActive(){this._setActiveItemByIndex(0,1)}setLastItemActive(){this._setActiveItemByIndex(this._getItemsArray().length-1,-1)}setNextItemActive(){this._activeItemIndex()<0?this.setFirstItemActive():this._setActiveItemByDelta(1)}setPreviousItemActive(){this._activeItemIndex()<0&&this._wrap?this.setLastItemActive():this._setActiveItemByDelta(-1)}updateActiveItem(n){let e=this._getItemsArray(),i=typeof n==`number`?n:e.indexOf(n),a=e[i];this._activeItem.set(a??null),this._activeItemIndex.set(i),this._typeahead?.setCurrentSelectedItemIndex(i)}destroy(){this._typeaheadSubscription.unsubscribe(),this._itemChangesSubscription?.unsubscribe(),this._effectRef?.destroy(),this._typeahead?.destroy(),this.tabOut.complete(),this.change.complete()}_setActiveItemByDelta(n){this._wrap?this._setActiveInWrapMode(n):this._setActiveInDefaultMode(n)}_setActiveInWrapMode(n){let e=this._getItemsArray();for(let i=1;i<=e.length;i++){let a=(this._activeItemIndex()+n*i+e.length)%e.length,o=e[a];if(!this._skipPredicateFn(o)){this.setActiveItem(a);return}}}_setActiveInDefaultMode(n){this._setActiveItemByIndex(this._activeItemIndex()+n,n)}_setActiveItemByIndex(n,e){let i=this._getItemsArray();if(i[n]){for(;this._skipPredicateFn(i[n]);)if(n+=e,!i[n])return;this.setActiveItem(n)}}_getItemsArray(){return sn(this._items)?this._items():this._items instanceof wn?this._items.toArray():this._items}_itemsChanged(n){this._typeahead?.setItems(n);let e=this._activeItem();if(e){let i=n.indexOf(e);i>-1&&i!==this._activeItemIndex()&&(this._activeItemIndex.set(i),this._typeahead?.setCurrentSelectedItemIndex(i))}}};var Ec=class extends Vr{setActiveItem(n){this.activeItem&&this.activeItem.setInactiveStyles(),super.setActiveItem(n),this.activeItem&&this.activeItem.setActiveStyles()}};var ea=class extends Vr{_origin=`program`;setFocusOrigin(n){return this._origin=n,this}setActiveItem(n){super.setActiveItem(n),this.activeItem&&this.activeItem.focus(this._origin)}};var GC=new Map;var Be=class t{_appId=f($i);static _infix=`a${Math.floor(Math.random()*1e5).toString()}`;getId(n,e=!1){this._appId!==`ng`&&(n+=this._appId);let i=GC.get(n);return i===void 0?i=0:i++,GC.set(n,i),`${n}${e?t._infix+`-`:``}${i}`}static ɵfac=function(e){return new(e||t)};static ɵprov=ce({token:t,factory:t.ɵfac})};var qC=` `;function bA(t,n,e){let i=Tu(t,n);e=e.trim(),!i.some(a=>a.trim()===e)&&(i.push(e),t.setAttribute(n,i.join(qC)))}function vA(t,n,e){let i=Tu(t,n);e=e.trim();let a=i.filter(o=>o!==e);a.length?t.setAttribute(n,a.join(qC)):t.removeAttribute(n)}function Tu(t,n){return t.getAttribute(n)?.match(/\S+/g)??[]}var YC=`cdk-describedby-message`;var Mu=`cdk-describedby-host`;var Vg=0;var Nu=(()=>{class t{_platform=f(Oe);_document=f(de);_messageRegistry=new Map;_messagesContainer=null;_id=`${Vg++}`;constructor(){f(Je).load(Ii),this._id=f($i)+`-`+Vg++}describe(e,i,a){if(!this._canBeDescribed(e,i))return;let o=Lg(i,a);typeof i!=`string`?(WC(i,this._id),this._messageRegistry.set(o,{messageElement:i,referenceCount:0})):this._messageRegistry.has(o)||this._createMessageElement(i,a),this._isElementDescribedByMessage(e,o)||this._addMessageReference(e,o)}removeDescription(e,i,a){if(!i||!this._isElementNode(e))return;let o=Lg(i,a);if(this._isElementDescribedByMessage(e,o)&&this._removeMessageReference(e,o),typeof i==`string`){let r=this._messageRegistry.get(o);r&&r.referenceCount===0&&this._deleteMessageElement(o)}this._messagesContainer?.childNodes.length===0&&(this._messagesContainer.remove(),this._messagesContainer=null)}ngOnDestroy(){let e=this._document.querySelectorAll(`[${Mu}="${this._id}"]`);for(let i=0;i<e.length;i++)this._removeCdkDescribedByReferenceIds(e[i]),e[i].removeAttribute(Mu);this._messagesContainer?.remove(),this._messagesContainer=null,this._messageRegistry.clear()}_createMessageElement(e,i){let a=this._document.createElement(`div`);WC(a,this._id),a.textContent=e,i&&a.setAttribute(`role`,i),this._createMessagesContainer(),this._messagesContainer.appendChild(a),this._messageRegistry.set(Lg(e,i),{messageElement:a,referenceCount:0})}_deleteMessageElement(e){this._messageRegistry.get(e)?.messageElement?.remove(),this._messageRegistry.delete(e)}_createMessagesContainer(){if(this._messagesContainer)return;let e=`cdk-describedby-message-container`,i=this._document.querySelectorAll(`.${e}[platform="server"]`);for(let o=0;o<i.length;o++)i[o].remove();let a=this._document.createElement(`div`);a.style.visibility=`hidden`,a.classList.add(e),a.classList.add(`cdk-visually-hidden`),this._platform.isBrowser||a.setAttribute(`platform`,`server`),this._document.body.appendChild(a),this._messagesContainer=a}_removeCdkDescribedByReferenceIds(e){let i=Tu(e,`aria-describedby`).filter(a=>a.indexOf(YC)!=0);e.setAttribute(`aria-describedby`,i.join(` `))}_addMessageReference(e,i){let a=this._messageRegistry.get(i);bA(e,`aria-describedby`,a.messageElement.id),e.setAttribute(Mu,this._id),a.referenceCount++}_removeMessageReference(e,i){let a=this._messageRegistry.get(i);a.referenceCount--,vA(e,`aria-describedby`,a.messageElement.id),e.removeAttribute(Mu)}_isElementDescribedByMessage(e,i){let a=Tu(e,`aria-describedby`),o=this._messageRegistry.get(i),r=o&&o.messageElement.id;return!!r&&a.indexOf(r)!=-1}_canBeDescribed(e,i){if(!this._isElementNode(e))return!1;if(i&&typeof i==`object`)return!0;let a=i==null?``:`${i}`.trim(),o=e.getAttribute(`aria-label`);return a?!o||o.trim()!==a:!1}_isElementNode(e){return e.nodeType===this._document.ELEMENT_NODE}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();function Lg(t,n){return typeof t==`string`?`${n||``}/${t}`:t}function WC(t,n){t.id||(t.id=`${YC}-${n}-${Vg++}`)}var si=(function(t){return t[t.NORMAL=0]=`NORMAL`,t[t.NEGATED=1]=`NEGATED`,t[t.INVERTED=2]=`INVERTED`,t})(si||{});var Au;var Ro;function Ou(){if(Ro==null){if(typeof document!=`object`||!document||typeof Element!=`function`||!Element)return Ro=!1,Ro;if(document.documentElement?.style&&`scrollBehavior`in document.documentElement.style)Ro=!0;else{let t=Element.prototype.scrollTo;t?Ro=!/\{\s*\[native code\]\s*\}/.test(t.toString()):Ro=!1}}return Ro}function Br(){if(typeof document!=`object`||!document)return si.NORMAL;if(Au==null){let t=document.createElement(`div`),n=t.style;t.dir=`rtl`,n.width=`1px`,n.overflow=`auto`,n.visibility=`hidden`,n.pointerEvents=`none`,n.position=`absolute`;let e=document.createElement(`div`),i=e.style;i.width=`2px`,i.height=`1px`,t.appendChild(e),document.body.appendChild(t),Au=si.NORMAL,t.scrollLeft===0&&(t.scrollLeft=1,Au=t.scrollLeft===0?si.NEGATED:si.INVERTED),t.remove()}return Au}function Bg(){return typeof __karma__<`u`&&!!__karma__||typeof jasmine<`u`&&!!jasmine||typeof jest<`u`&&!!jest||typeof Mocha<`u`&&!!Mocha}var jr;var KC=[`color`,`button`,`checkbox`,`date`,`datetime-local`,`email`,`file`,`hidden`,`image`,`month`,`number`,`password`,`radio`,`range`,`reset`,`search`,`submit`,`tel`,`text`,`time`,`url`,`week`];function jg(){if(jr)return jr;if(typeof document!=`object`||!document)return jr=new Set(KC),jr;let t=document.createElement(`input`);return jr=new Set(KC.filter(n=>(t.setAttribute(`type`,n),t.type===n))),jr}var _A=new k(`MATERIAL_ANIMATIONS`);var QC=null;function Dc(){return f(_A,{optional:!0})?.animationsDisabled||f(Vs,{optional:!0})===`NoopAnimations`?`di-disabled`:(QC??=f(Fr).matchMedia(`(prefers-reduced-motion)`).matches,QC?`reduced-motion`:`enabled`)}function Ue(){return Dc()!==`enabled`}function Dt(t){return t==null?``:typeof t==`string`?t:`${t}px`}function ta(t){return t!=null&&`${t}`!=`false`}var Vn=(function(t){return t[t.FADING_IN=0]=`FADING_IN`,t[t.VISIBLE=1]=`VISIBLE`,t[t.FADING_OUT=2]=`FADING_OUT`,t[t.HIDDEN=3]=`HIDDEN`,t})(Vn||{});var zg=class{_renderer;element;config;_animationForciblyDisabledThroughCss;state=Vn.HIDDEN;constructor(n,e,i,a=!1){this._renderer=n,this.element=e,this.config=i,this._animationForciblyDisabledThroughCss=a}fadeOut(){this._renderer.fadeOutRipple(this)}};var XC=Rr({passive:!0,capture:!0});var Hg=class{_events=new Map;addHandler(n,e,i,a){let o=this._events.get(e);if(o){let r=o.get(i);r?r.add(a):o.set(i,new Set([a]))}else this._events.set(e,new Map([[i,new Set([a])]])),n.runOutsideAngular(()=>{document.addEventListener(e,this._delegateEventHandler,XC)})}removeHandler(n,e,i){let a=this._events.get(n);if(!a)return;let o=a.get(e);o&&(o.delete(i),o.size===0&&a.delete(e),a.size===0&&(this._events.delete(n),document.removeEventListener(n,this._delegateEventHandler,XC)))}_delegateEventHandler=n=>{let e=Yt(n);e&&this._events.get(n.type)?.forEach((i,a)=>{(a===e||a.contains(e))&&i.forEach(o=>o.handleEvent(n))})}};var Ic={enterDuration:225,exitDuration:150};var yA=800;var ZC=Rr({passive:!0,capture:!0});var JC=[`mousedown`,`touchstart`];var eS=[`mouseup`,`mouseleave`,`touchend`,`touchcancel`];var xA=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],hostAttrs:[`mat-ripple-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`.mat-ripple {
  overflow: hidden;
  position: relative;
}
.mat-ripple:not(:empty) {
  transform: translateZ(0);
}

.mat-ripple.mat-ripple-unbounded {
  overflow: visible;
}

.mat-ripple-element {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  transition: opacity, transform 0ms cubic-bezier(0, 0, 0.2, 1);
  transform: scale3d(0, 0, 0);
  background-color: var(--%NS%mat-ripple-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 10%, transparent));
}
@media (forced-colors: active) {
  .mat-ripple-element {
    display: none;
  }
}
.cdk-drag-preview .mat-ripple-element, .cdk-drag-placeholder .mat-ripple-element {
  display: none;
}
`],encapsulation:2})}return t})();var kc=class t{_target;_ngZone;_platform;_containerElement;_triggerElement=null;_isPointerDown=!1;_activeRipples=new Map;_mostRecentTransientRipple=null;_lastTouchStartEvent;_pointerUpEventsRegistered=!1;_containerRect=null;static _eventManager=new Hg;constructor(n,e,i,a,o){this._target=n,this._ngZone=e,this._platform=a,a.isBrowser&&(this._containerElement=ln(i)),o&&o.get(Je).load(xA)}fadeInRipple(n,e,i={}){let a=this._containerRect=this._containerRect||this._containerElement.getBoundingClientRect(),o=r(r({},Ic),i.animation);i.centered&&(n=a.left+a.width/2,e=a.top+a.height/2);let r$2=i.radius||CA(n,e,a),s=n-a.left,c=e-a.top,h=o.enterDuration,p=document.createElement(`div`);p.classList.add(`mat-ripple-element`),p.style.left=`${s-r$2}px`,p.style.top=`${c-r$2}px`,p.style.height=`${r$2*2}px`,p.style.width=`${r$2*2}px`,i.color!=null&&(p.style.backgroundColor=i.color),p.style.transitionDuration=`${h}ms`,this._containerElement.appendChild(p);let g=window.getComputedStyle(p),v=g.transitionProperty,b=g.transitionDuration,y=v===`none`||b===`0s`||b===`0s, 0s`||a.width===0&&a.height===0,E=new zg(this,p,i,y);p.style.transform=`scale3d(1, 1, 1)`,E.state=Vn.FADING_IN,i.persistent||(this._mostRecentTransientRipple=E);let P=null;return!y&&(h||o.exitDuration)&&this._ngZone.runOutsideAngular(()=>{let ae=()=>{P&&(P.fallbackTimer=null),clearTimeout(x),this._finishRippleTransition(E)},z=()=>this._destroyRipple(E),x=setTimeout(z,h+100);p.addEventListener(`transitionend`,ae),p.addEventListener(`transitioncancel`,z),P={onTransitionEnd:ae,onTransitionCancel:z,fallbackTimer:x}}),this._activeRipples.set(E,P),(y||!h)&&this._finishRippleTransition(E),E}fadeOutRipple(n){if(n.state===Vn.FADING_OUT||n.state===Vn.HIDDEN)return;let e=n.element,i=r(r({},Ic),n.config.animation);e.style.transitionDuration=`${i.exitDuration}ms`,e.style.opacity=`0`,n.state=Vn.FADING_OUT,(n._animationForciblyDisabledThroughCss||!i.exitDuration)&&this._finishRippleTransition(n)}fadeOutAll(){this._getActiveRipples().forEach(n=>n.fadeOut())}fadeOutAllNonPersistent(){this._getActiveRipples().forEach(n=>{n.config.persistent||n.fadeOut()})}setupTriggerEvents(n){let e=ln(n);!this._platform.isBrowser||!e||e===this._triggerElement||(this._removeTriggerEvents(),this._triggerElement=e,JC.forEach(i=>{t._eventManager.addHandler(this._ngZone,i,e,this)}))}handleEvent(n){n.type===`mousedown`?this._onMousedown(n):n.type===`touchstart`?this._onTouchStart(n):this._onPointerUp(),this._pointerUpEventsRegistered||(this._ngZone.runOutsideAngular(()=>{eS.forEach(e=>{this._triggerElement.addEventListener(e,this,ZC)})}),this._pointerUpEventsRegistered=!0)}_finishRippleTransition(n){n.state===Vn.FADING_IN?this._startFadeOutTransition(n):n.state===Vn.FADING_OUT&&this._destroyRipple(n)}_startFadeOutTransition(n){let e=n===this._mostRecentTransientRipple,{persistent:i}=n.config;n.state=Vn.VISIBLE,!i&&(!e||!this._isPointerDown)&&n.fadeOut()}_destroyRipple(n){let e=this._activeRipples.get(n)??null;this._activeRipples.delete(n),this._activeRipples.size||(this._containerRect=null),n===this._mostRecentTransientRipple&&(this._mostRecentTransientRipple=null),n.state=Vn.HIDDEN,e!==null&&(n.element.removeEventListener(`transitionend`,e.onTransitionEnd),n.element.removeEventListener(`transitioncancel`,e.onTransitionCancel),e.fallbackTimer!==null&&clearTimeout(e.fallbackTimer)),n.element.remove()}_onMousedown(n){let e=Do(n),i=this._lastTouchStartEvent&&Date.now()<this._lastTouchStartEvent+yA;!this._target.rippleDisabled&&!e&&!i&&(this._isPointerDown=!0,this.fadeInRipple(n.clientX,n.clientY,this._target.rippleConfig))}_onTouchStart(n){if(!this._target.rippleDisabled&&!Io(n)){this._lastTouchStartEvent=Date.now(),this._isPointerDown=!0;let e=n.changedTouches;if(e)for(let i=0;i<e.length;i++)this.fadeInRipple(e[i].clientX,e[i].clientY,this._target.rippleConfig)}}_onPointerUp(){this._isPointerDown&&(this._isPointerDown=!1,this._getActiveRipples().forEach(n=>{let e=n.state===Vn.VISIBLE||n.config.terminateOnPointerUp&&n.state===Vn.FADING_IN;!n.config.persistent&&e&&n.fadeOut()}))}_getActiveRipples(){return Array.from(this._activeRipples.keys())}_removeTriggerEvents(){let n=this._triggerElement;n&&(JC.forEach(e=>t._eventManager.removeHandler(e,n,this)),this._pointerUpEventsRegistered&&(eS.forEach(e=>n.removeEventListener(e,this,ZC)),this._pointerUpEventsRegistered=!1))}};function CA(t,n,e){let i=Math.max(Math.abs(t-e.left),Math.abs(t-e.right)),a=Math.max(Math.abs(n-e.top),Math.abs(n-e.bottom));return Math.sqrt(i*i+a*a)}var zr=new k(`mat-ripple-global-options`);var ci=(()=>{class t{_elementRef=f(W);_animationsDisabled=Ue();color;unbounded=!1;centered=!1;radius=0;animation;get disabled(){return this._disabled}set disabled(e){e&&this.fadeOutAllNonPersistent(),this._disabled=e,this._setupTriggerEventsIfEnabled()}_disabled=!1;get trigger(){return this._trigger||this._elementRef.nativeElement}set trigger(e){this._trigger=e,this._setupTriggerEventsIfEnabled()}_trigger;_rippleRenderer;_globalOptions;_isInitialized=!1;constructor(){let e=f(X),i=f(Oe),a=f(zr,{optional:!0}),o=f(se);this._globalOptions=a||{},this._rippleRenderer=new kc(this,e,this._elementRef,i,o)}ngOnInit(){this._isInitialized=!0,this._setupTriggerEventsIfEnabled()}ngOnDestroy(){this._rippleRenderer._removeTriggerEvents()}fadeOutAll(){this._rippleRenderer.fadeOutAll()}fadeOutAllNonPersistent(){this._rippleRenderer.fadeOutAllNonPersistent()}get rippleConfig(){return{centered:this.centered,radius:this.radius,color:this.color,animation:r(r(r({},this._globalOptions.animation),this._animationsDisabled?{enterDuration:0,exitDuration:0}:{}),this.animation),terminateOnPointerUp:this._globalOptions.terminateOnPointerUp}}get rippleDisabled(){return this.disabled||!!this._globalOptions.disabled}_setupTriggerEventsIfEnabled(){!this.disabled&&this._isInitialized&&this._rippleRenderer.setupTriggerEvents(this.trigger)}launch(e,i=0,a){return typeof e==`number`?this._rippleRenderer.fadeInRipple(e,i,r(r({},this.rippleConfig),a)):this._rippleRenderer.fadeInRipple(0,0,r(r({},this.rippleConfig),e))}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`mat-ripple`,``],[``,`matRipple`,``]],hostAttrs:[1,`mat-ripple`],hostVars:2,hostBindings:function(i,a){i&2&&Y(`mat-ripple-unbounded`,a.unbounded)},inputs:{color:[0,`matRippleColor`,`color`],unbounded:[0,`matRippleUnbounded`,`unbounded`],centered:[0,`matRippleCentered`,`centered`],radius:[0,`matRippleRadius`,`radius`],animation:[0,`matRippleAnimation`,`animation`],disabled:[0,`matRippleDisabled`,`disabled`],trigger:[0,`matRippleTrigger`,`trigger`]},exportAs:[`matRipple`]})}return t})();var SA={capture:!0};var wA=[`focus`,`mousedown`,`mouseenter`,`touchstart`];var Ug=`mat-ripple-loader-uninitialized`;var $g=`mat-ripple-loader-class-name`;var tS=`mat-ripple-loader-centered`;var Ru=`mat-ripple-loader-disabled`;var Pu=(()=>{class t{_document=f(de);_animationsDisabled=Ue();_globalRippleOptions=f(zr,{optional:!0});_platform=f(Oe);_ngZone=f(X);_injector=f(se);_eventCleanups;_hosts=new Map;constructor(){let e=f(Tt).createRenderer(null,null);this._eventCleanups=this._ngZone.runOutsideAngular(()=>wA.map(i=>e.listen(this._document,i,this._onInteraction,SA)))}ngOnDestroy(){let e=this._hosts.keys();for(let i of e)this.destroyRipple(i);this._eventCleanups.forEach(i=>i())}configureRipple(e,i){e.setAttribute(Ug,this._globalRippleOptions?.namespace??``),(i.className||!e.hasAttribute($g))&&e.setAttribute($g,i.className||``),i.centered&&e.setAttribute(tS,``),i.disabled&&e.setAttribute(Ru,``)}setDisabled(e,i){let a=this._hosts.get(e);a?(a.target.rippleDisabled=i,!i&&!a.hasSetUpEvents&&(a.hasSetUpEvents=!0,a.renderer.setupTriggerEvents(e))):i?e.setAttribute(Ru,``):e.removeAttribute(Ru)}_onInteraction=e=>{let i=Yt(e);if(i instanceof HTMLElement){let a=i.closest(`[${Ug}="${this._globalRippleOptions?.namespace??``}"]`);a&&this._createRipple(a)}};_createRipple(e){if(!this._document||this._hosts.has(e))return;e.querySelector(`.mat-ripple`)?.remove();let i=this._document.createElement(`span`);i.classList.add(`mat-ripple`,e.getAttribute($g)),e.append(i);let a=this._globalRippleOptions,o=this._animationsDisabled?0:a?.animation?.enterDuration??Ic.enterDuration,r=this._animationsDisabled?0:a?.animation?.exitDuration??Ic.exitDuration,s={rippleDisabled:this._animationsDisabled||a?.disabled||e.hasAttribute(Ru),rippleConfig:{centered:e.hasAttribute(tS),terminateOnPointerUp:a?.terminateOnPointerUp,animation:{enterDuration:o,exitDuration:r}}},c=new kc(s,this._ngZone,i,this._platform,this._injector),h=!s.rippleDisabled;h&&c.setupTriggerEvents(e),this._hosts.set(e,{target:s,renderer:c,hasSetUpEvents:h}),e.removeAttribute(Ug)}destroyRipple(e){let i=this._hosts.get(e);i&&(i.renderer._removeTriggerEvents(),this._hosts.delete(e))}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var dn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`structural-styles`]],decls:0,vars:0,template:function(i,a){},styles:[`.mat-focus-indicator {
  position: relative;
}
.mat-focus-indicator::before {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  box-sizing: border-box;
  pointer-events: none;
  display: var(--%NS%mat-focus-indicator-display, none);
  border-width: var(--%NS%mat-focus-indicator-border-width, 3px);
  border-style: var(--%NS%mat-focus-indicator-border-style, solid);
  border-color: var(--%NS%mat-focus-indicator-border-color, transparent);
  border-radius: var(--%NS%mat-focus-indicator-border-radius, 4px);
}
.mat-focus-indicator:focus-visible::before {
  content: "";
}

@media (forced-colors: active) {
  html {
    --%NS%mat-focus-indicator-display: block;
    --%NS%mat-focus-indicator-fallback-border-style: none;
  }
}
`],encapsulation:2})}return t})();var EA=new k(`MAT_BUTTON_CONFIG`);function nS(t){return t==null?void 0:lt(t)}var Gg=(()=>{class t{_elementRef=f(W);_ngZone=f(X);_animationsDisabled=Ue();_config=f(EA,{optional:!0});_focusMonitor=f(Jt);_cleanupClick;_renderer=f(Re);_rippleLoader=f(Pu);_isAnchor;_isFab=!1;color;get disableRipple(){return this._disableRipple}set disableRipple(e){this._disableRipple=e,this._updateRippleDisabled()}_disableRipple=!1;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._updateRippleDisabled()}_disabled=!1;ariaDisabled;disabledInteractive;tabIndex;set _tabindex(e){this.tabIndex=e}showProgress=zt(!1,{transform:V});constructor(){f(Je).load(dn);let e=this._elementRef.nativeElement;this._isAnchor=e.tagName===`A`,this.disabledInteractive=this._config?.disabledInteractive??!1,this.color=this._config?.color??null,this._rippleLoader?.configureRipple(e,{className:`mat-mdc-button-ripple`})}ngAfterViewInit(){this._focusMonitor.monitor(this._elementRef,!0),this._isAnchor&&this._setupAsAnchor()}ngOnDestroy(){this._cleanupClick?.(),this._focusMonitor.stopMonitoring(this._elementRef),this._rippleLoader?.destroyRipple(this._elementRef.nativeElement)}focus(e=`program`,i){e?this._focusMonitor.focusVia(this._elementRef.nativeElement,e,i):this._elementRef.nativeElement.focus(i)}_getAriaDisabled(){return this.ariaDisabled!=null?this.ariaDisabled:this._isAnchor?this.disabled||null:this.disabled&&this.disabledInteractive?!0:null}_getDisabledAttribute(){return this.disabledInteractive||!this.disabled?null:!0}_updateRippleDisabled(){this._rippleLoader?.setDisabled(this._elementRef.nativeElement,this.disableRipple||this.disabled)}_getTabIndex(){return this._isAnchor?this.disabled&&!this.disabledInteractive?-1:this.tabIndex:this.tabIndex}_setupAsAnchor(){this._cleanupClick=this._ngZone.runOutsideAngular(()=>this._renderer.listen(this._elementRef.nativeElement,`click`,e=>{this.disabled&&(e.preventDefault(),e.stopImmediatePropagation())}))}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,hostAttrs:[1,`mat-mdc-button-base`],hostVars:15,hostBindings:function(i,a){i&2&&(te(`disabled`,a._getDisabledAttribute())(`aria-disabled`,a._getAriaDisabled())(`tabindex`,a._getTabIndex()),tt(a.color?`mat-`+a.color:``),Y(`mat-mdc-button-progress-indicator-shown`,a.showProgress())(`mat-mdc-button-disabled`,a.disabled)(`mat-mdc-button-disabled-interactive`,a.disabledInteractive)(`mat-unthemed`,!a.color)(`_mat-animation-noopable`,a._animationsDisabled))},inputs:{color:`color`,disableRipple:[2,`disableRipple`,`disableRipple`,V],disabled:[2,`disabled`,`disabled`,V],ariaDisabled:[2,`aria-disabled`,`ariaDisabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],tabIndex:[2,`tabIndex`,`tabIndex`,nS],_tabindex:[2,`tabindex`,`_tabindex`,nS],showProgress:[1,`showProgress`]}})}return t})();var Wg=(()=>{class t extends Gg{constructor(){super(),this._rippleLoader.configureRipple(this._elementRef.nativeElement,{centered:!0})}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[``,`progressIndicator`,``]]],i=[`*`,`[progressIndicator]`];function a(o,r){o&1&&(ct(0,`div`,1),he(1,1),ht())}return B({type:t,selectors:[[`button`,`mat-icon-button`,``],[`a`,`mat-icon-button`,``],[`button`,`matIconButton`,``],[`a`,`matIconButton`,``]],hostAttrs:[1,`mdc-icon-button`,`mat-mdc-icon-button`],exportAs:[`matButton`,`matAnchor`],features:[Ve],ngContentSelectors:i,decls:5,vars:1,consts:[[1,`mat-mdc-button-persistent-ripple`,`mdc-icon-button__ripple`],[1,`mat-mdc-button-progress-indicator-container`],[1,`mat-focus-indicator`],[1,`mat-mdc-button-touch-target`]],template:function(r,s){r&1&&(Pe(e),Wt(0,`span`,0),he(1),D(2,a,2,0,`div`,1),Wt(3,`span`,2)(4,`span`,3)),r&2&&(m(2),I(s.showProgress()?2:-1))},styles:[`.mat-mdc-icon-button {
  -webkit-user-select: none;
  user-select: none;
  display: inline-block;
  position: relative;
  box-sizing: border-box;
  border: none;
  outline: none;
  background-color: transparent;
  fill: currentColor;
  text-decoration: none;
  cursor: pointer;
  z-index: 0;
  overflow: visible;
  border-radius: var(--%NS%mat-icon-button-container-shape, var(--%NS%mat-sys-corner-full, 50%));
  flex-shrink: 0;
  text-align: center;
  width: var(--%NS%mat-icon-button-state-layer-size, 40px);
  height: var(--%NS%mat-icon-button-state-layer-size, 40px);
  padding: calc(calc(var(--%NS%mat-icon-button-state-layer-size, 40px) - var(--%NS%mat-icon-button-icon-size, 24px)) / 2);
  font-size: var(--%NS%mat-icon-button-icon-size, 24px);
  color: var(--%NS%mat-icon-button-icon-color, var(--%NS%mat-sys-on-surface-variant));
  -webkit-tap-highlight-color: transparent;
}
.mat-mdc-icon-button .mat-mdc-button-ripple,
.mat-mdc-icon-button .mat-mdc-button-persistent-ripple,
.mat-mdc-icon-button .mat-mdc-button-persistent-ripple::before {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
  border-radius: inherit;
}
.mat-mdc-icon-button .mat-mdc-button-ripple {
  overflow: hidden;
}
.mat-mdc-icon-button .mat-mdc-button-persistent-ripple::before {
  content: "";
  opacity: 0;
}
.mat-mdc-icon-button .mdc-button__label,
.mat-mdc-icon-button .mat-icon {
  z-index: 1;
  position: relative;
}
.mat-mdc-icon-button .mat-focus-indicator {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  border-radius: inherit;
}
.mat-mdc-icon-button:focus-visible > .mat-focus-indicator::before {
  content: "";
  border-radius: inherit;
}
.mat-mdc-icon-button .mat-ripple-element {
  background-color: var(--%NS%mat-icon-button-ripple-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface-variant) calc(var(--%NS%mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-icon-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-icon-button-state-layer-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-icon-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-icon-button-disabled-state-layer-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-icon-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-icon-button-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
}
.mat-mdc-icon-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-icon-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-icon-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-icon-button-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity));
}
.mat-mdc-icon-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-icon-button-pressed-state-layer-opacity, var(--%NS%mat-sys-pressed-state-layer-opacity));
}
.mat-mdc-icon-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--%NS%mat-icon-button-touch-target-size, 48px);
  display: var(--%NS%mat-icon-button-touch-target-display, block);
  left: 50%;
  width: var(--%NS%mat-icon-button-touch-target-size, 48px);
  transform: translate(-50%, -50%);
}
.mat-mdc-icon-button._mat-animation-noopable {
  transition: none !important;
  animation: none !important;
}
.mat-mdc-icon-button[disabled], .mat-mdc-icon-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--%NS%mat-icon-button-disabled-icon-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-mdc-icon-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}
.mat-mdc-icon-button img,
.mat-mdc-icon-button svg {
  width: var(--%NS%mat-icon-button-icon-size, 24px);
  height: var(--%NS%mat-icon-button-icon-size, 24px);
  vertical-align: baseline;
}
.mat-mdc-icon-button .mat-mdc-button-progress-indicator-container .mdc-circular-progress__determinate-circle-graphic {
  width: inherit;
  height: inherit;
}
.mat-mdc-icon-button .mat-mdc-button-progress-indicator-container .mdc-circular-progress__indeterminate-circle-graphic {
  height: 100%;
}
.mat-mdc-icon-button .mat-mdc-button-persistent-ripple {
  border-radius: var(--%NS%mat-icon-button-container-shape, var(--%NS%mat-sys-corner-full, 50%));
}
.mat-mdc-icon-button[hidden] {
  display: none;
}
.mat-mdc-icon-button.mat-unthemed:not(.mdc-ripple-upgraded):focus::before, .mat-mdc-icon-button.mat-primary:not(.mdc-ripple-upgraded):focus::before, .mat-mdc-icon-button.mat-accent:not(.mdc-ripple-upgraded):focus::before, .mat-mdc-icon-button.mat-warn:not(.mdc-ripple-upgraded):focus::before {
  background: transparent;
  opacity: 1;
}

.mat-mdc-button-progress-indicator-container {
  position: absolute;
  inset-inline-start: 0;
  inset-block-start: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

.mat-mdc-button-progress-indicator-shown mat-icon {
  visibility: hidden;
}
`,`@media (forced-colors: active) {
  .mat-mdc-button:not(.mdc-button--outlined),
  .mat-mdc-unelevated-button:not(.mdc-button--outlined),
  .mat-mdc-raised-button:not(.mdc-button--outlined),
  .mat-mdc-outlined-button:not(.mdc-button--outlined),
  .mat-mdc-button-base.mat-tonal-button,
  .mat-mdc-icon-button.mat-mdc-icon-button,
  .mat-mdc-outlined-button .mdc-button__ripple {
    outline: solid 1px;
  }
}
`],encapsulation:2})})()}return t})();var DA=new k(`cdk-dir-doc`,{providedIn:`root`,factory:()=>f(de)});var IA=/^(ar|ckb|dv|he|iw|fa|nqo|ps|sd|ug|ur|yi|.*[-_](Adlm|Arab|Hebr|Nkoo|Rohg|Thaa))(?!.*[-_](Latn|Cyrl)($|-|_))($|-|_)/i;function iS(t){let n=t?.toLowerCase()||``;return n===`auto`&&typeof navigator<`u`&&navigator?.language?IA.test(navigator.language)?`rtl`:`ltr`:n===`rtl`?`rtl`:`ltr`}var gt=(()=>{class t{get value(){return this.valueSignal()}valueSignal=T(`ltr`);change=new ee;constructor(){let e=f(DA,{optional:!0});if(e){let i=e.body?e.body.dir:null,a=e.documentElement?e.documentElement.dir:null;this.valueSignal.set(iS(i||a||`ltr`))}}ngOnDestroy(){this.change.complete()}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var we=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({})}return t})();var ki=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[we]})}return t})();var aS=new Map([[`text`,[`mat-mdc-button`]],[`filled`,[`mdc-button--unelevated`,`mat-mdc-unelevated-button`]],[`elevated`,[`mdc-button--raised`,`mat-mdc-raised-button`]],[`outlined`,[`mdc-button--outlined`,`mat-mdc-outlined-button`]],[`tonal`,[`mat-tonal-button`]]]);var en=(()=>{class t extends Gg{get appearance(){return this._appearance}set appearance(e){this.setAppearance(e||this._config?.defaultAppearance||`text`)}_appearance=null;constructor(){super();let e=kA(this._elementRef.nativeElement);e&&this.setAppearance(e)}setAppearance(e){if(e===this._appearance)return;let i=this._elementRef.nativeElement.classList,a=this._appearance?aS.get(this._appearance):null,o=aS.get(e);a&&i.remove(...a),i.add(...o),this._appearance=e}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[``,8,`material-icons`,3,`iconPositionEnd`,``],[`mat-icon`,3,`iconPositionEnd`,``],[``,`matButtonIcon`,``,3,`iconPositionEnd`,``],[``,8,`material-symbols-outlined`,3,`iconPositionEnd`,``],[``,8,`material-symbols-rounded`,3,`iconPositionEnd`,``],[``,8,`material-symbols-sharp`,3,`iconPositionEnd`,``]],`*`,[[``,`iconPositionEnd`,``,8,`material-icons`],[`mat-icon`,`iconPositionEnd`,``],[``,`matButtonIcon`,``,`iconPositionEnd`,``],[``,`iconPositionEnd`,``,8,`material-symbols-outlined`],[``,`iconPositionEnd`,``,8,`material-symbols-rounded`],[``,`iconPositionEnd`,``,8,`material-symbols-sharp`]],[[``,`progressIndicator`,``]]],i=[`.material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd]), .material-symbols-outlined:not([iconPositionEnd]), .material-symbols-rounded:not([iconPositionEnd]), .material-symbols-sharp:not([iconPositionEnd])`,`*`,`.material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd], .material-symbols-outlined[iconPositionEnd], .material-symbols-rounded[iconPositionEnd], .material-symbols-sharp[iconPositionEnd]`,`[progressIndicator]`];function a(o,r){o&1&&(ct(0,`div`,2),he(1,3),ht())}return B({type:t,selectors:[[`button`,`matButton`,``],[`a`,`matButton`,``],[`button`,`mat-button`,``],[`button`,`mat-raised-button`,``],[`button`,`mat-flat-button`,``],[`button`,`mat-stroked-button`,``],[`a`,`mat-button`,``],[`a`,`mat-raised-button`,``],[`a`,`mat-flat-button`,``],[`a`,`mat-stroked-button`,``]],hostAttrs:[1,`mdc-button`],inputs:{appearance:[0,`matButton`,`appearance`]},exportAs:[`matButton`,`matAnchor`],features:[Ve],ngContentSelectors:i,decls:8,vars:5,consts:[[1,`mat-mdc-button-persistent-ripple`],[1,`mdc-button__label`],[1,`mat-mdc-button-progress-indicator-container`],[1,`mat-focus-indicator`],[1,`mat-mdc-button-touch-target`]],template:function(r,s){r&1&&(Pe(e),Wt(0,`span`,0),he(1),ct(2,`span`,1),he(3,1),ht(),he(4,2),D(5,a,2,0,`div`,2),Wt(6,`span`,3)(7,`span`,4)),r&2&&(Y(`mdc-button__ripple`,!s._isFab)(`mdc-fab__ripple`,s._isFab),m(5),I(s.showProgress()?5:-1))},styles:[`.mat-mdc-button-base {
  text-decoration: none;
}
.mat-mdc-button-base .mat-icon {
  min-height: fit-content;
  flex-shrink: 0;
}
@media (hover: none) {
  .mat-mdc-button-base:hover > span.mat-mdc-button-persistent-ripple::before {
    opacity: 0;
  }
}

.mdc-button {
  -webkit-user-select: none;
  user-select: none;
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  min-width: 64px;
  border: none;
  outline: none;
  line-height: inherit;
  -webkit-appearance: none;
  overflow: visible;
  vertical-align: middle;
  background: transparent;
  padding: 0 8px;
}
.mdc-button::-moz-focus-inner {
  padding: 0;
  border: 0;
}
.mdc-button:active {
  outline: none;
}
.mdc-button:hover {
  cursor: pointer;
}
.mdc-button:disabled {
  cursor: default;
  pointer-events: none;
}
.mdc-button[hidden] {
  display: none;
}
.mdc-button .mdc-button__label {
  position: relative;
}

.mat-mdc-button {
  padding: 0 var(--%NS%mat-button-text-horizontal-padding, 12px);
  height: var(--%NS%mat-button-text-container-height, 40px);
  font-family: var(--%NS%mat-button-text-label-text-font, var(--%NS%mat-sys-label-large-font));
  font-size: var(--%NS%mat-button-text-label-text-size, var(--%NS%mat-sys-label-large-size));
  letter-spacing: var(--%NS%mat-button-text-label-text-tracking, var(--%NS%mat-sys-label-large-tracking));
  text-transform: var(--%NS%mat-button-text-label-text-transform);
  font-weight: var(--%NS%mat-button-text-label-text-weight, var(--%NS%mat-sys-label-large-weight));
}
.mat-mdc-button, .mat-mdc-button .mdc-button__ripple {
  border-radius: var(--%NS%mat-button-text-container-shape, var(--%NS%mat-sys-corner-full));
}
.mat-mdc-button:not(:disabled) {
  color: var(--%NS%mat-button-text-label-text-color, var(--%NS%mat-sys-primary));
}
.mat-mdc-button[disabled], .mat-mdc-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--%NS%mat-button-text-disabled-label-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-mdc-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}
.mat-mdc-button:has(.material-icons, .material-symbols-outlined, .material-symbols-rounded,
.material-symbols-sharp, mat-icon, [matButtonIcon]) {
  padding: 0 var(--%NS%mat-button-text-with-icon-horizontal-padding, 16px);
}
.mat-mdc-button > .mat-icon {
  margin-right: var(--%NS%mat-button-text-icon-spacing, 8px);
  margin-left: var(--%NS%mat-button-text-icon-offset, -4px);
}
[dir=rtl] .mat-mdc-button > .mat-icon {
  margin-right: var(--%NS%mat-button-text-icon-offset, -4px);
  margin-left: var(--%NS%mat-button-text-icon-spacing, 8px);
}
.mat-mdc-button .mdc-button__label + .mat-icon {
  margin-right: var(--%NS%mat-button-text-icon-offset, -4px);
  margin-left: var(--%NS%mat-button-text-icon-spacing, 8px);
}
[dir=rtl] .mat-mdc-button .mdc-button__label + .mat-icon {
  margin-right: var(--%NS%mat-button-text-icon-spacing, 8px);
  margin-left: var(--%NS%mat-button-text-icon-offset, -4px);
}
.mat-mdc-button .mat-ripple-element {
  background-color: var(--%NS%mat-button-text-ripple-color, color-mix(in srgb, var(--%NS%mat-sys-primary) calc(var(--%NS%mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-button-text-state-layer-color, var(--%NS%mat-sys-primary));
}
.mat-mdc-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-button-text-disabled-state-layer-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-text-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
}
.mat-mdc-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-text-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity));
}
.mat-mdc-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-text-pressed-state-layer-opacity, var(--%NS%mat-sys-pressed-state-layer-opacity));
}
.mat-mdc-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--%NS%mat-button-text-touch-target-size, 48px);
  display: var(--%NS%mat-button-text-touch-target-display, block);
  left: 0;
  right: 0;
  transform: translateY(-50%);
}

.mat-mdc-unelevated-button {
  transition: box-shadow 280ms cubic-bezier(0.4, 0, 0.2, 1);
  height: var(--%NS%mat-button-filled-container-height, 40px);
  font-family: var(--%NS%mat-button-filled-label-text-font, var(--%NS%mat-sys-label-large-font));
  font-size: var(--%NS%mat-button-filled-label-text-size, var(--%NS%mat-sys-label-large-size));
  letter-spacing: var(--%NS%mat-button-filled-label-text-tracking, var(--%NS%mat-sys-label-large-tracking));
  text-transform: var(--%NS%mat-button-filled-label-text-transform);
  font-weight: var(--%NS%mat-button-filled-label-text-weight, var(--%NS%mat-sys-label-large-weight));
  padding: 0 var(--%NS%mat-button-filled-horizontal-padding, 24px);
}
.mat-mdc-unelevated-button > .mat-icon {
  margin-right: var(--%NS%mat-button-filled-icon-spacing, 8px);
  margin-left: var(--%NS%mat-button-filled-icon-offset, -8px);
}
[dir=rtl] .mat-mdc-unelevated-button > .mat-icon {
  margin-right: var(--%NS%mat-button-filled-icon-offset, -8px);
  margin-left: var(--%NS%mat-button-filled-icon-spacing, 8px);
}
.mat-mdc-unelevated-button .mdc-button__label + .mat-icon {
  margin-right: var(--%NS%mat-button-filled-icon-offset, -8px);
  margin-left: var(--%NS%mat-button-filled-icon-spacing, 8px);
}
[dir=rtl] .mat-mdc-unelevated-button .mdc-button__label + .mat-icon {
  margin-right: var(--%NS%mat-button-filled-icon-spacing, 8px);
  margin-left: var(--%NS%mat-button-filled-icon-offset, -8px);
}
.mat-mdc-unelevated-button .mat-ripple-element {
  background-color: var(--%NS%mat-button-filled-ripple-color, color-mix(in srgb, var(--%NS%mat-sys-on-primary) calc(var(--%NS%mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-unelevated-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-button-filled-state-layer-color, var(--%NS%mat-sys-on-primary));
}
.mat-mdc-unelevated-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-button-filled-disabled-state-layer-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-unelevated-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-filled-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
}
.mat-mdc-unelevated-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-unelevated-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-unelevated-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-filled-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity));
}
.mat-mdc-unelevated-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-filled-pressed-state-layer-opacity, var(--%NS%mat-sys-pressed-state-layer-opacity));
}
.mat-mdc-unelevated-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--%NS%mat-button-filled-touch-target-size, 48px);
  display: var(--%NS%mat-button-filled-touch-target-display, block);
  left: 0;
  right: 0;
  transform: translateY(-50%);
}
.mat-mdc-unelevated-button:not(:disabled) {
  color: var(--%NS%mat-button-filled-label-text-color, var(--%NS%mat-sys-on-primary));
  background-color: var(--%NS%mat-button-filled-container-color, var(--%NS%mat-sys-primary));
}
.mat-mdc-unelevated-button, .mat-mdc-unelevated-button .mdc-button__ripple {
  border-radius: var(--%NS%mat-button-filled-container-shape, var(--%NS%mat-sys-corner-full));
}
.mat-mdc-unelevated-button .mat-mdc-button-progress-indicator-container {
  --%NS%mat-progress-spinner-active-indicator-color: var(--%NS%mat-button-filled-progress-active-indicator-color, var(--%NS%mat-sys-on-primary));
}
.mat-mdc-unelevated-button[disabled], .mat-mdc-unelevated-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--%NS%mat-button-filled-disabled-label-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
  background-color: var(--%NS%mat-button-filled-disabled-container-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 12%, transparent));
}
.mat-mdc-unelevated-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}

.mat-mdc-raised-button {
  transition: box-shadow 280ms cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--%NS%mat-button-protected-container-elevation-shadow, var(--%NS%mat-sys-level1));
  height: var(--%NS%mat-button-protected-container-height, 40px);
  font-family: var(--%NS%mat-button-protected-label-text-font, var(--%NS%mat-sys-label-large-font));
  font-size: var(--%NS%mat-button-protected-label-text-size, var(--%NS%mat-sys-label-large-size));
  letter-spacing: var(--%NS%mat-button-protected-label-text-tracking, var(--%NS%mat-sys-label-large-tracking));
  text-transform: var(--%NS%mat-button-protected-label-text-transform);
  font-weight: var(--%NS%mat-button-protected-label-text-weight, var(--%NS%mat-sys-label-large-weight));
  padding: 0 var(--%NS%mat-button-protected-horizontal-padding, 24px);
}
.mat-mdc-raised-button > .mat-icon {
  margin-right: var(--%NS%mat-button-protected-icon-spacing, 8px);
  margin-left: var(--%NS%mat-button-protected-icon-offset, -8px);
}
[dir=rtl] .mat-mdc-raised-button > .mat-icon {
  margin-right: var(--%NS%mat-button-protected-icon-offset, -8px);
  margin-left: var(--%NS%mat-button-protected-icon-spacing, 8px);
}
.mat-mdc-raised-button .mdc-button__label + .mat-icon {
  margin-right: var(--%NS%mat-button-protected-icon-offset, -8px);
  margin-left: var(--%NS%mat-button-protected-icon-spacing, 8px);
}
[dir=rtl] .mat-mdc-raised-button .mdc-button__label + .mat-icon {
  margin-right: var(--%NS%mat-button-protected-icon-spacing, 8px);
  margin-left: var(--%NS%mat-button-protected-icon-offset, -8px);
}
.mat-mdc-raised-button .mat-ripple-element {
  background-color: var(--%NS%mat-button-protected-ripple-color, color-mix(in srgb, var(--%NS%mat-sys-primary) calc(var(--%NS%mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-raised-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-button-protected-state-layer-color, var(--%NS%mat-sys-primary));
}
.mat-mdc-raised-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-button-protected-disabled-state-layer-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-raised-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-protected-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
}
.mat-mdc-raised-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-raised-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-raised-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-protected-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity));
}
.mat-mdc-raised-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-protected-pressed-state-layer-opacity, var(--%NS%mat-sys-pressed-state-layer-opacity));
}
.mat-mdc-raised-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--%NS%mat-button-protected-touch-target-size, 48px);
  display: var(--%NS%mat-button-protected-touch-target-display, block);
  left: 0;
  right: 0;
  transform: translateY(-50%);
}
.mat-mdc-raised-button:not(:disabled) {
  color: var(--%NS%mat-button-protected-label-text-color, var(--%NS%mat-sys-primary));
  background-color: var(--%NS%mat-button-protected-container-color, var(--%NS%mat-sys-surface));
}
.mat-mdc-raised-button, .mat-mdc-raised-button .mdc-button__ripple {
  border-radius: var(--%NS%mat-button-protected-container-shape, var(--%NS%mat-sys-corner-full));
}
@media (hover: hover) {
  .mat-mdc-raised-button:hover {
    box-shadow: var(--%NS%mat-button-protected-hover-container-elevation-shadow, var(--%NS%mat-sys-level2));
  }
}
.mat-mdc-raised-button:focus {
  box-shadow: var(--%NS%mat-button-protected-focus-container-elevation-shadow, var(--%NS%mat-sys-level1));
}
.mat-mdc-raised-button:active, .mat-mdc-raised-button:focus:active {
  box-shadow: var(--%NS%mat-button-protected-pressed-container-elevation-shadow, var(--%NS%mat-sys-level1));
}
.mat-mdc-raised-button[disabled], .mat-mdc-raised-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--%NS%mat-button-protected-disabled-label-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
  background-color: var(--%NS%mat-button-protected-disabled-container-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 12%, transparent));
}
.mat-mdc-raised-button[disabled].mat-mdc-button-disabled, .mat-mdc-raised-button.mat-mdc-button-disabled.mat-mdc-button-disabled {
  box-shadow: var(--%NS%mat-button-protected-disabled-container-elevation-shadow, var(--%NS%mat-sys-level0));
}
.mat-mdc-raised-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}

.mat-mdc-outlined-button {
  border-style: solid;
  transition: border 280ms cubic-bezier(0.4, 0, 0.2, 1);
  height: var(--%NS%mat-button-outlined-container-height, 40px);
  font-family: var(--%NS%mat-button-outlined-label-text-font, var(--%NS%mat-sys-label-large-font));
  font-size: var(--%NS%mat-button-outlined-label-text-size, var(--%NS%mat-sys-label-large-size));
  letter-spacing: var(--%NS%mat-button-outlined-label-text-tracking, var(--%NS%mat-sys-label-large-tracking));
  text-transform: var(--%NS%mat-button-outlined-label-text-transform);
  font-weight: var(--%NS%mat-button-outlined-label-text-weight, var(--%NS%mat-sys-label-large-weight));
  border-radius: var(--%NS%mat-button-outlined-container-shape, var(--%NS%mat-sys-corner-full));
  border-width: var(--%NS%mat-button-outlined-outline-width, 1px);
  padding: 0 var(--%NS%mat-button-outlined-horizontal-padding, 24px);
}
.mat-mdc-outlined-button > .mat-icon {
  margin-right: var(--%NS%mat-button-outlined-icon-spacing, 8px);
  margin-left: var(--%NS%mat-button-outlined-icon-offset, -8px);
}
[dir=rtl] .mat-mdc-outlined-button > .mat-icon {
  margin-right: var(--%NS%mat-button-outlined-icon-offset, -8px);
  margin-left: var(--%NS%mat-button-outlined-icon-spacing, 8px);
}
.mat-mdc-outlined-button .mdc-button__label + .mat-icon {
  margin-right: var(--%NS%mat-button-outlined-icon-offset, -8px);
  margin-left: var(--%NS%mat-button-outlined-icon-spacing, 8px);
}
[dir=rtl] .mat-mdc-outlined-button .mdc-button__label + .mat-icon {
  margin-right: var(--%NS%mat-button-outlined-icon-spacing, 8px);
  margin-left: var(--%NS%mat-button-outlined-icon-offset, -8px);
}
.mat-mdc-outlined-button .mat-ripple-element {
  background-color: var(--%NS%mat-button-outlined-ripple-color, color-mix(in srgb, var(--%NS%mat-sys-primary) calc(var(--%NS%mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-outlined-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-button-outlined-state-layer-color, var(--%NS%mat-sys-primary));
}
.mat-mdc-outlined-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-button-outlined-disabled-state-layer-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-outlined-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-outlined-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
}
.mat-mdc-outlined-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-outlined-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-outlined-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-outlined-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity));
}
.mat-mdc-outlined-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-outlined-pressed-state-layer-opacity, var(--%NS%mat-sys-pressed-state-layer-opacity));
}
.mat-mdc-outlined-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--%NS%mat-button-outlined-touch-target-size, 48px);
  display: var(--%NS%mat-button-outlined-touch-target-display, block);
  left: 0;
  right: 0;
  transform: translateY(-50%);
}
.mat-mdc-outlined-button:not(:disabled) {
  color: var(--%NS%mat-button-outlined-label-text-color, var(--%NS%mat-sys-primary));
  border-color: var(--%NS%mat-button-outlined-outline-color, var(--%NS%mat-sys-outline));
}
.mat-mdc-outlined-button[disabled], .mat-mdc-outlined-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--%NS%mat-button-outlined-disabled-label-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
  border-color: var(--%NS%mat-button-outlined-disabled-outline-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 12%, transparent));
}
.mat-mdc-outlined-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}

.mat-tonal-button {
  transition: box-shadow 280ms cubic-bezier(0.4, 0, 0.2, 1);
  height: var(--%NS%mat-button-tonal-container-height, 40px);
  font-family: var(--%NS%mat-button-tonal-label-text-font, var(--%NS%mat-sys-label-large-font));
  font-size: var(--%NS%mat-button-tonal-label-text-size, var(--%NS%mat-sys-label-large-size));
  letter-spacing: var(--%NS%mat-button-tonal-label-text-tracking, var(--%NS%mat-sys-label-large-tracking));
  text-transform: var(--%NS%mat-button-tonal-label-text-transform);
  font-weight: var(--%NS%mat-button-tonal-label-text-weight, var(--%NS%mat-sys-label-large-weight));
  padding: 0 var(--%NS%mat-button-tonal-horizontal-padding, 24px);
}
.mat-tonal-button:not(:disabled) {
  color: var(--%NS%mat-button-tonal-label-text-color, var(--%NS%mat-sys-on-secondary-container));
  background-color: var(--%NS%mat-button-tonal-container-color, var(--%NS%mat-sys-secondary-container));
}
.mat-tonal-button, .mat-tonal-button .mdc-button__ripple {
  border-radius: var(--%NS%mat-button-tonal-container-shape, var(--%NS%mat-sys-corner-full));
}
.mat-tonal-button[disabled], .mat-tonal-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--%NS%mat-button-tonal-disabled-label-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
  background-color: var(--%NS%mat-button-tonal-disabled-container-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 12%, transparent));
}
.mat-tonal-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}
.mat-tonal-button > .mat-icon {
  margin-right: var(--%NS%mat-button-tonal-icon-spacing, 8px);
  margin-left: var(--%NS%mat-button-tonal-icon-offset, -8px);
}
[dir=rtl] .mat-tonal-button > .mat-icon {
  margin-right: var(--%NS%mat-button-tonal-icon-offset, -8px);
  margin-left: var(--%NS%mat-button-tonal-icon-spacing, 8px);
}
.mat-tonal-button .mdc-button__label + .mat-icon {
  margin-right: var(--%NS%mat-button-tonal-icon-offset, -8px);
  margin-left: var(--%NS%mat-button-tonal-icon-spacing, 8px);
}
[dir=rtl] .mat-tonal-button .mdc-button__label + .mat-icon {
  margin-right: var(--%NS%mat-button-tonal-icon-spacing, 8px);
  margin-left: var(--%NS%mat-button-tonal-icon-offset, -8px);
}
.mat-tonal-button .mat-ripple-element {
  background-color: var(--%NS%mat-button-tonal-ripple-color, color-mix(in srgb, var(--%NS%mat-sys-on-secondary-container) calc(var(--%NS%mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-tonal-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-button-tonal-state-layer-color, var(--%NS%mat-sys-on-secondary-container));
}
.mat-tonal-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--%NS%mat-button-tonal-disabled-state-layer-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-tonal-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-tonal-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
}
.mat-tonal-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-tonal-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-tonal-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-tonal-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity));
}
.mat-tonal-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--%NS%mat-button-tonal-pressed-state-layer-opacity, var(--%NS%mat-sys-pressed-state-layer-opacity));
}
.mat-tonal-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--%NS%mat-button-tonal-touch-target-size, 48px);
  display: var(--%NS%mat-button-tonal-touch-target-display, block);
  left: 0;
  right: 0;
  transform: translateY(-50%);
}

.mat-mdc-button,
.mat-mdc-unelevated-button,
.mat-mdc-raised-button,
.mat-mdc-outlined-button,
.mat-tonal-button {
  -webkit-tap-highlight-color: transparent;
}
.mat-mdc-button .mat-mdc-button-ripple,
.mat-mdc-button .mat-mdc-button-persistent-ripple,
.mat-mdc-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-unelevated-button .mat-mdc-button-ripple,
.mat-mdc-unelevated-button .mat-mdc-button-persistent-ripple,
.mat-mdc-unelevated-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-raised-button .mat-mdc-button-ripple,
.mat-mdc-raised-button .mat-mdc-button-persistent-ripple,
.mat-mdc-raised-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-outlined-button .mat-mdc-button-ripple,
.mat-mdc-outlined-button .mat-mdc-button-persistent-ripple,
.mat-mdc-outlined-button .mat-mdc-button-persistent-ripple::before,
.mat-tonal-button .mat-mdc-button-ripple,
.mat-tonal-button .mat-mdc-button-persistent-ripple,
.mat-tonal-button .mat-mdc-button-persistent-ripple::before {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
  border-radius: inherit;
}
.mat-mdc-button .mat-mdc-button-ripple,
.mat-mdc-unelevated-button .mat-mdc-button-ripple,
.mat-mdc-raised-button .mat-mdc-button-ripple,
.mat-mdc-outlined-button .mat-mdc-button-ripple,
.mat-tonal-button .mat-mdc-button-ripple {
  overflow: hidden;
}
.mat-mdc-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-unelevated-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-raised-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-outlined-button .mat-mdc-button-persistent-ripple::before,
.mat-tonal-button .mat-mdc-button-persistent-ripple::before {
  content: "";
  opacity: 0;
}
.mat-mdc-button .mdc-button__label,
.mat-mdc-button .mat-icon,
.mat-mdc-unelevated-button .mdc-button__label,
.mat-mdc-unelevated-button .mat-icon,
.mat-mdc-raised-button .mdc-button__label,
.mat-mdc-raised-button .mat-icon,
.mat-mdc-outlined-button .mdc-button__label,
.mat-mdc-outlined-button .mat-icon,
.mat-tonal-button .mdc-button__label,
.mat-tonal-button .mat-icon {
  z-index: 1;
  position: relative;
}
.mat-mdc-button .mat-focus-indicator,
.mat-mdc-unelevated-button .mat-focus-indicator,
.mat-mdc-raised-button .mat-focus-indicator,
.mat-mdc-outlined-button .mat-focus-indicator,
.mat-tonal-button .mat-focus-indicator {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  border-radius: inherit;
}
.mat-mdc-button:focus-visible > .mat-focus-indicator::before,
.mat-mdc-unelevated-button:focus-visible > .mat-focus-indicator::before,
.mat-mdc-raised-button:focus-visible > .mat-focus-indicator::before,
.mat-mdc-outlined-button:focus-visible > .mat-focus-indicator::before,
.mat-tonal-button:focus-visible > .mat-focus-indicator::before {
  content: "";
  border-radius: inherit;
}
.mat-mdc-button._mat-animation-noopable,
.mat-mdc-unelevated-button._mat-animation-noopable,
.mat-mdc-raised-button._mat-animation-noopable,
.mat-mdc-outlined-button._mat-animation-noopable,
.mat-tonal-button._mat-animation-noopable {
  transition: none !important;
  animation: none !important;
}
.mat-mdc-button > .mat-icon,
.mat-mdc-unelevated-button > .mat-icon,
.mat-mdc-raised-button > .mat-icon,
.mat-mdc-outlined-button > .mat-icon,
.mat-tonal-button > .mat-icon {
  display: inline-block;
  position: relative;
  vertical-align: top;
  font-size: 1.125rem;
  height: 1.125rem;
  width: 1.125rem;
}

.mat-mdc-outlined-button .mat-mdc-button-ripple,
.mat-mdc-outlined-button .mdc-button__ripple {
  top: -1px;
  left: -1px;
  bottom: -1px;
  right: -1px;
}

.mat-mdc-unelevated-button .mat-focus-indicator::before,
.mat-tonal-button .mat-focus-indicator::before,
.mat-mdc-raised-button .mat-focus-indicator::before {
  margin: calc(calc(var(--%NS%mat-focus-indicator-border-width, 3px) + 2px) * -1);
}

.mat-mdc-outlined-button .mat-focus-indicator::before {
  margin: calc(calc(var(--%NS%mat-focus-indicator-border-width, 3px) + 3px) * -1);
}

.mat-mdc-button-progress-indicator-container {
  position: absolute;
  inset-inline-start: 0;
  inset-block-start: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

.mat-mdc-button-progress-indicator-shown mat-icon,
.mat-mdc-button-progress-indicator-shown [matButtonIcon],
.mat-mdc-button-progress-indicator-shown .mdc-button__label {
  visibility: hidden;
}
`,`@media (forced-colors: active) {
  .mat-mdc-button:not(.mdc-button--outlined),
  .mat-mdc-unelevated-button:not(.mdc-button--outlined),
  .mat-mdc-raised-button:not(.mdc-button--outlined),
  .mat-mdc-outlined-button:not(.mdc-button--outlined),
  .mat-mdc-button-base.mat-tonal-button,
  .mat-mdc-icon-button.mat-mdc-icon-button,
  .mat-mdc-outlined-button .mdc-button__ripple {
    outline: solid 1px;
  }
}
`],encapsulation:2})})()}return t})();function kA(t){return t.hasAttribute(`mat-raised-button`)?`elevated`:t.hasAttribute(`mat-stroked-button`)?`outlined`:t.hasAttribute(`mat-flat-button`)?`filled`:t.hasAttribute(`mat-button`)?`text`:null}var Pt=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[ki,we]})}return t})();var MA=new k(`MAT_CARD_CONFIG`);var Bn=(()=>{class t{appearance;constructor(){let e=f(MA,{optional:!0});this.appearance=e?.appearance||`raised`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return B({type:t,selectors:[[`mat-card`]],hostAttrs:[1,`mat-mdc-card`,`mdc-card`],hostVars:8,hostBindings:function(a,o){a&2&&Y(`mat-mdc-card-outlined`,o.appearance===`outlined`)(`mdc-card--outlined`,o.appearance===`outlined`)(`mat-mdc-card-filled`,o.appearance===`filled`)(`mdc-card--filled`,o.appearance===`filled`)},inputs:{appearance:`appearance`},exportAs:[`matCard`],ngContentSelectors:[`*`],decls:1,vars:0,template:function(a,o){a&1&&(Pe(),he(0))},styles:[`.mat-mdc-card {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  position: relative;
  border-style: solid;
  border-width: 0;
  background-color: var(--%NS%mat-card-elevated-container-color, var(--%NS%mat-sys-surface-container-low));
  border-color: var(--%NS%mat-card-elevated-container-color, var(--%NS%mat-sys-surface-container-low));
  border-radius: var(--%NS%mat-card-elevated-container-shape, var(--%NS%mat-sys-corner-medium));
  box-shadow: var(--%NS%mat-card-elevated-container-elevation, var(--%NS%mat-sys-level1));
}
.mat-mdc-card::after {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: solid 1px transparent;
  content: "";
  display: block;
  pointer-events: none;
  box-sizing: border-box;
  border-radius: var(--%NS%mat-card-elevated-container-shape, var(--%NS%mat-sys-corner-medium));
}

.mat-mdc-card-outlined {
  background-color: var(--%NS%mat-card-outlined-container-color, var(--%NS%mat-sys-surface));
  border-radius: var(--%NS%mat-card-outlined-container-shape, var(--%NS%mat-sys-corner-medium));
  border-width: var(--%NS%mat-card-outlined-outline-width, 1px);
  border-color: var(--%NS%mat-card-outlined-outline-color, var(--%NS%mat-sys-outline-variant));
  box-shadow: var(--%NS%mat-card-outlined-container-elevation, var(--%NS%mat-sys-level0));
}
.mat-mdc-card-outlined::after {
  border: none;
}

.mat-mdc-card-filled {
  background-color: var(--%NS%mat-card-filled-container-color, var(--%NS%mat-sys-surface-container-highest));
  border-radius: var(--%NS%mat-card-filled-container-shape, var(--%NS%mat-sys-corner-medium));
  box-shadow: var(--%NS%mat-card-filled-container-elevation, var(--%NS%mat-sys-level0));
}

.mdc-card__media {
  position: relative;
  box-sizing: border-box;
  background-repeat: no-repeat;
  background-position: center;
  background-size: cover;
}
.mdc-card__media::before {
  display: block;
  content: "";
}
.mdc-card__media:first-child {
  border-top-left-radius: inherit;
  border-top-right-radius: inherit;
}
.mdc-card__media:last-child {
  border-bottom-left-radius: inherit;
  border-bottom-right-radius: inherit;
}

.mat-mdc-card-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  box-sizing: border-box;
  min-height: 52px;
  padding: 8px;
}

.mat-mdc-card-title {
  font-family: var(--%NS%mat-card-title-text-font, var(--%NS%mat-sys-title-large-font));
  line-height: var(--%NS%mat-card-title-text-line-height, var(--%NS%mat-sys-title-large-line-height));
  font-size: var(--%NS%mat-card-title-text-size, var(--%NS%mat-sys-title-large-size));
  letter-spacing: var(--%NS%mat-card-title-text-tracking, var(--%NS%mat-sys-title-large-tracking));
  font-weight: var(--%NS%mat-card-title-text-weight, var(--%NS%mat-sys-title-large-weight));
}

.mat-mdc-card-subtitle {
  color: var(--%NS%mat-card-subtitle-text-color, var(--%NS%mat-sys-on-surface));
  font-family: var(--%NS%mat-card-subtitle-text-font, var(--%NS%mat-sys-title-medium-font));
  line-height: var(--%NS%mat-card-subtitle-text-line-height, var(--%NS%mat-sys-title-medium-line-height));
  font-size: var(--%NS%mat-card-subtitle-text-size, var(--%NS%mat-sys-title-medium-size));
  letter-spacing: var(--%NS%mat-card-subtitle-text-tracking, var(--%NS%mat-sys-title-medium-tracking));
  font-weight: var(--%NS%mat-card-subtitle-text-weight, var(--%NS%mat-sys-title-medium-weight));
}

.mat-mdc-card-title,
.mat-mdc-card-subtitle {
  display: block;
  margin: 0;
}
.mat-mdc-card-avatar ~ .mat-mdc-card-header-text .mat-mdc-card-title,
.mat-mdc-card-avatar ~ .mat-mdc-card-header-text .mat-mdc-card-subtitle {
  padding: 16px 16px 0;
}

.mat-mdc-card-header {
  display: flex;
  padding: 16px 16px 0;
}

.mat-mdc-card-content {
  display: block;
  padding: 0 16px;
}
.mat-mdc-card-content:first-child {
  padding-top: 16px;
}
.mat-mdc-card-content:last-child {
  padding-bottom: 16px;
}

.mat-mdc-card-title-group {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.mat-mdc-card-avatar {
  height: 40px;
  width: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-bottom: 16px;
  object-fit: cover;
}
.mat-mdc-card-avatar ~ .mat-mdc-card-header-text .mat-mdc-card-subtitle,
.mat-mdc-card-avatar ~ .mat-mdc-card-header-text .mat-mdc-card-title {
  line-height: normal;
}

.mat-mdc-card-sm-image {
  width: 80px;
  height: 80px;
}

.mat-mdc-card-md-image {
  width: 112px;
  height: 112px;
}

.mat-mdc-card-lg-image {
  width: 152px;
  height: 152px;
}

.mat-mdc-card-xl-image {
  width: 240px;
  height: 240px;
}

.mat-mdc-card-subtitle ~ .mat-mdc-card-title,
.mat-mdc-card-title ~ .mat-mdc-card-subtitle,
.mat-mdc-card-header .mat-mdc-card-header-text .mat-mdc-card-title,
.mat-mdc-card-header .mat-mdc-card-header-text .mat-mdc-card-subtitle,
.mat-mdc-card-title-group .mat-mdc-card-title,
.mat-mdc-card-title-group .mat-mdc-card-subtitle {
  padding-top: 0;
}

.mat-mdc-card-content > :last-child:not(.mat-mdc-card-footer) {
  margin-bottom: 0;
}

.mat-mdc-card-actions-align-end {
  justify-content: flex-end;
}
`],encapsulation:2})})()}return t})();var ka=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[`mat-card-title`],[``,`mat-card-title`,``],[``,`matCardTitle`,``]],hostAttrs:[1,`mat-mdc-card-title`]})}return t})();var jn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[`mat-card-content`]],hostAttrs:[1,`mat-mdc-card-content`]})}return t})();var Fu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[`mat-card-subtitle`],[``,`mat-card-subtitle`,``],[``,`matCardSubtitle`,``]],hostAttrs:[1,`mat-mdc-card-subtitle`]})}return t})();var Ma=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[``,`mat-card-avatar`,``],[``,`matCardAvatar`,``]],[[`mat-card-title`],[`mat-card-subtitle`],[``,`mat-card-title`,``],[``,`mat-card-subtitle`,``],[``,`matCardTitle`,``],[``,`matCardSubtitle`,``]],`*`];return B({type:t,selectors:[[`mat-card-header`]],hostAttrs:[1,`mat-mdc-card-header`],ngContentSelectors:[`[mat-card-avatar], [matCardAvatar]`,`mat-card-title, mat-card-subtitle,
      [mat-card-title], [mat-card-subtitle],
      [matCardTitle], [matCardSubtitle]`,`*`],decls:4,vars:0,consts:[[1,`mat-mdc-card-header-text`]],template:function(o,r){o&1&&(Pe(e),he(0),ct(1,`div`,0),he(2,1),ht(),he(3,2))},encapsulation:2})})()}return t})();var zn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[we]})}return t})();var TA=new k(`mat-progress-spinner-default-options`,{providedIn:`root`,factory:()=>({diameter:oS})});var oS=100;var NA=10;var rS=(()=>{class t{_elementRef=f(W);_noopAnimations;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor=`primary`;_determinateCircle;constructor(){let e=f(TA),i=Dc(),a=this._elementRef.nativeElement;this._noopAnimations=i===`di-disabled`&&!!e&&!e._forceAnimations,this.mode=a.nodeName.toLowerCase()===`mat-spinner`?`indeterminate`:`determinate`,!this._noopAnimations&&i===`reduced-motion`&&a.classList.add(`mat-progress-spinner-reduced-motion`),e&&(e.color&&(this.color=this._defaultColor=e.color),e.diameter&&(this.diameter=e.diameter),e.strokeWidth&&(this.strokeWidth=e.strokeWidth))}mode;get value(){return this.mode===`determinate`?this._value:0}set value(e){this._value=Math.max(0,Math.min(100,e||0))}_value=0;get diameter(){return this._diameter}set diameter(e){this._diameter=e||0}_diameter=oS;get strokeWidth(){return this._strokeWidth??this.diameter/10}set strokeWidth(e){this._strokeWidth=e||0}_strokeWidth;_circleRadius(){return(this.diameter-NA)/2}_viewBox(){let e=this._circleRadius()*2+this.strokeWidth;return`0 0 ${e} ${e}`}_strokeCircumference(){return 2*Math.PI*this._circleRadius()}_strokeDashOffset(){return this.mode===`determinate`?this._strokeCircumference()*(100-this._value)/100:null}_circleStrokeWidth(){return this.strokeWidth/this.diameter*100}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`determinateSpinner`];function i(a,o){if(a&1&&(Gt(),l(0,`svg`,11),H(1,`circle`,12),d()),a&2){let r=_();te(`viewBox`,r._viewBox()),m(),gn(`stroke-dasharray`,r._strokeCircumference(),`px`)(`stroke-dashoffset`,r._strokeCircumference()/2,`px`)(`stroke-width`,r._circleStrokeWidth(),`%`),te(`r`,r._circleRadius())}}return B({type:t,selectors:[[`mat-progress-spinner`],[`mat-spinner`]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;Z(s=J())&&(r._determinateCircle=s.first)}},hostAttrs:[`role`,`progressbar`,`tabindex`,`-1`,1,`mat-mdc-progress-spinner`,`mdc-circular-progress`],hostVars:18,hostBindings:function(o,r){o&2&&(te(`aria-valuemin`,0)(`aria-valuemax`,100)(`aria-valuenow`,r.mode===`determinate`?r.value:null)(`mode`,r.mode),tt(`mat-`+r.color),gn(`width`,r.diameter,`px`)(`height`,r.diameter,`px`)(`--%NS%mat-progress-spinner-size`,r.diameter+`px`)(`--%NS%mat-progress-spinner-active-indicator-width`,r.diameter+`px`),Y(`_mat-animation-noopable`,r._noopAnimations)(`mdc-circular-progress--indeterminate`,r.mode===`indeterminate`))},inputs:{color:`color`,mode:`mode`,value:[2,`value`,`value`,lt],diameter:[2,`diameter`,`diameter`,lt],strokeWidth:[2,`strokeWidth`,`strokeWidth`,lt]},exportAs:[`matProgressSpinner`],decls:14,vars:11,consts:[[`circle`,``],[`determinateSpinner`,``],[`aria-hidden`,`true`,1,`mdc-circular-progress__determinate-container`],[`xmlns`,`http://www.w3.org/2000/svg`,`focusable`,`false`,1,`mdc-circular-progress__determinate-circle-graphic`],[`cx`,`50%`,`cy`,`50%`,1,`mdc-circular-progress__determinate-circle`],[`aria-hidden`,`true`,1,`mdc-circular-progress__indeterminate-container`],[1,`mdc-circular-progress__spinner-layer`],[1,`mdc-circular-progress__circle-clipper`,`mdc-circular-progress__circle-left`],[3,`ngTemplateOutlet`],[1,`mdc-circular-progress__gap-patch`],[1,`mdc-circular-progress__circle-clipper`,`mdc-circular-progress__circle-right`],[`xmlns`,`http://www.w3.org/2000/svg`,`focusable`,`false`,1,`mdc-circular-progress__indeterminate-circle-graphic`],[`cx`,`50%`,`cy`,`50%`]],template:function(o,r){if(o&1&&(ot(0,i,2,8,`ng-template`,null,0,rc),l(2,`div`,2,1),Gt(),l(4,`svg`,3),H(5,`circle`,4),d()(),_a(),l(6,`div`,5)(7,`div`,6)(8,`div`,7),wa(9,8),d(),l(10,`div`,9),wa(11,8),d(),l(12,`div`,10),wa(13,8),d()()()),o&2){let s=ft(1);m(4),te(`viewBox`,r._viewBox()),m(),gn(`stroke-dasharray`,r._strokeCircumference(),`px`)(`stroke-dashoffset`,r._strokeDashOffset(),`px`)(`stroke-width`,r._circleStrokeWidth(),`%`),te(`r`,r._circleRadius()),m(4),S(`ngTemplateOutlet`,s),m(2),S(`ngTemplateOutlet`,s),m(2),S(`ngTemplateOutlet`,s)}},dependencies:[fc],styles:[`.mat-mdc-progress-spinner {
  --%NS%mat-progress-spinner-animation-multiplier: 1;
  display: block;
  overflow: hidden;
  line-height: 0;
  position: relative;
  direction: ltr;
  transition: opacity 250ms cubic-bezier(0.4, 0, 0.6, 1);
}
.mat-mdc-progress-spinner circle {
  stroke-width: var(--%NS%mat-progress-spinner-active-indicator-width, 4px);
}
.mat-mdc-progress-spinner._mat-animation-noopable, .mat-mdc-progress-spinner._mat-animation-noopable .mdc-circular-progress__determinate-circle {
  transition: none !important;
}
.mat-mdc-progress-spinner._mat-animation-noopable .mdc-circular-progress__indeterminate-circle-graphic,
.mat-mdc-progress-spinner._mat-animation-noopable .mdc-circular-progress__spinner-layer,
.mat-mdc-progress-spinner._mat-animation-noopable .mdc-circular-progress__indeterminate-container {
  animation: none !important;
}
.mat-mdc-progress-spinner._mat-animation-noopable .mdc-circular-progress__indeterminate-container circle {
  stroke-dasharray: 0 !important;
}
@media (forced-colors: active) {
  .mat-mdc-progress-spinner .mdc-circular-progress__indeterminate-circle-graphic,
  .mat-mdc-progress-spinner .mdc-circular-progress__determinate-circle {
    stroke: currentColor;
    stroke: CanvasText;
  }
}

.mat-progress-spinner-reduced-motion {
  --%NS%mat-progress-spinner-animation-multiplier: 1.25;
}

.mdc-circular-progress__determinate-container,
.mdc-circular-progress__indeterminate-circle-graphic,
.mdc-circular-progress__indeterminate-container,
.mdc-circular-progress__spinner-layer {
  position: absolute;
  width: 100%;
  height: 100%;
}

.mdc-circular-progress__determinate-container {
  transform: rotate(-90deg);
}
.mdc-circular-progress--indeterminate .mdc-circular-progress__determinate-container {
  opacity: 0;
}

.mdc-circular-progress__indeterminate-container {
  font-size: 0;
  letter-spacing: 0;
  white-space: nowrap;
  opacity: 0;
}
.mdc-circular-progress--indeterminate .mdc-circular-progress__indeterminate-container {
  opacity: 1;
  animation: mdc-circular-progress-container-rotate calc(1568.2352941176ms * var(--%NS%mat-progress-spinner-animation-multiplier)) linear infinite;
}

.mdc-circular-progress__determinate-circle-graphic,
.mdc-circular-progress__indeterminate-circle-graphic {
  fill: transparent;
}

.mat-mdc-progress-spinner .mdc-circular-progress__determinate-circle,
.mat-mdc-progress-spinner .mdc-circular-progress__indeterminate-circle-graphic {
  stroke: var(--%NS%mat-progress-spinner-active-indicator-color, var(--%NS%mat-sys-primary));
}
@media (forced-colors: active) {
  .mat-mdc-progress-spinner .mdc-circular-progress__determinate-circle,
  .mat-mdc-progress-spinner .mdc-circular-progress__indeterminate-circle-graphic {
    stroke: CanvasText;
  }
}

.mdc-circular-progress__determinate-circle {
  transition: stroke-dashoffset 500ms cubic-bezier(0, 0, 0.2, 1);
}

.mdc-circular-progress__gap-patch {
  position: absolute;
  top: 0;
  left: 47.5%;
  box-sizing: border-box;
  width: 5%;
  height: 100%;
  overflow: hidden;
}

.mdc-circular-progress__gap-patch .mdc-circular-progress__indeterminate-circle-graphic {
  left: -900%;
  width: 2000%;
  transform: rotate(180deg);
}
.mdc-circular-progress__circle-clipper .mdc-circular-progress__indeterminate-circle-graphic {
  width: 200%;
}
.mdc-circular-progress__circle-right .mdc-circular-progress__indeterminate-circle-graphic {
  left: -100%;
}
.mdc-circular-progress--indeterminate .mdc-circular-progress__circle-left .mdc-circular-progress__indeterminate-circle-graphic {
  animation: mdc-circular-progress-left-spin calc(1333ms * var(--%NS%mat-progress-spinner-animation-multiplier)) cubic-bezier(0.4, 0, 0.2, 1) infinite both;
}
.mdc-circular-progress--indeterminate .mdc-circular-progress__circle-right .mdc-circular-progress__indeterminate-circle-graphic {
  animation: mdc-circular-progress-right-spin calc(1333ms * var(--%NS%mat-progress-spinner-animation-multiplier)) cubic-bezier(0.4, 0, 0.2, 1) infinite both;
}

.mdc-circular-progress__circle-clipper {
  display: inline-flex;
  position: relative;
  width: 50%;
  height: 100%;
  overflow: hidden;
}

.mdc-circular-progress--indeterminate .mdc-circular-progress__spinner-layer {
  animation: mdc-circular-progress-spinner-layer-rotate calc(5332ms * var(--%NS%mat-progress-spinner-animation-multiplier)) cubic-bezier(0.4, 0, 0.2, 1) infinite both;
}

@keyframes mdc-circular-progress-container-rotate {
  to {
    transform: rotate(360deg);
  }
}
@keyframes mdc-circular-progress-spinner-layer-rotate {
  12.5% {
    transform: rotate(135deg);
  }
  25% {
    transform: rotate(270deg);
  }
  37.5% {
    transform: rotate(405deg);
  }
  50% {
    transform: rotate(540deg);
  }
  62.5% {
    transform: rotate(675deg);
  }
  75% {
    transform: rotate(810deg);
  }
  87.5% {
    transform: rotate(945deg);
  }
  100% {
    transform: rotate(1080deg);
  }
}
@keyframes mdc-circular-progress-left-spin {
  from {
    transform: rotate(265deg);
  }
  50% {
    transform: rotate(130deg);
  }
  to {
    transform: rotate(265deg);
  }
}
@keyframes mdc-circular-progress-right-spin {
  from {
    transform: rotate(-265deg);
  }
  50% {
    transform: rotate(-130deg);
  }
  to {
    transform: rotate(-265deg);
  }
}
`],encapsulation:2})})()}return t})();var sS=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[we]})}return t})();var gS=(()=>{class t{_renderer;_elementRef;onChange=e=>{};onTouched=()=>{};constructor(e,i){this._renderer=e,this._elementRef=i}setProperty(e,i){this._renderer.setProperty(this._elementRef.nativeElement,e,i)}registerOnTouched(e){this.onTouched=e}registerOnChange(e){this.onChange=e}setDisabledState(e){this.setProperty(`disabled`,e)}static ɵfac=function(i){return new(i||t)(He(Re),He(W))};static ɵdir=K({type:t})}return t})();var OA=(()=>{class t extends gS{static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=K({type:t,features:[Ve]})}return t})();var Wr=new k(``);var RA={provide:Wr,useExisting:Ht(()=>qu),multi:!0};function PA(){let t=Fn()?Fn().getUserAgent():``;return/android (\d+)/.test(t.toLowerCase())}var FA=new k(``);var qu=(()=>{class t extends gS{_compositionMode;_composing=!1;constructor(e,i,a){super(e,i),this._compositionMode=a,this._compositionMode??=!PA()}writeValue(e){let i=e??``;this.setProperty(`value`,i)}_handleInput(e){(!this._compositionMode||this._compositionMode&&!this._composing)&&this.onChange(e)}_compositionStart(){this._composing=!0}_compositionEnd(e){this._composing=!1,this._compositionMode&&this.onChange(e)}static ɵfac=function(i){return new(i||t)(He(Re),He(W),He(FA,8))};static ɵdir=K({type:t,selectors:[[`input`,`formControlName`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`formControlName`,``,3,`ngNoCva`,``],[`input`,`formControl`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`formControl`,``,3,`ngNoCva`,``],[`input`,`ngModel`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`ngModel`,``,3,`ngNoCva`,``],[``,`ngDefaultControl`,``]],hostBindings:function(i,a){i&1&&C(`input`,function(r){return a._handleInput(r.target.value)})(`blur`,function(){return a.onTouched()})(`compositionstart`,function(){return a._compositionStart()})(`compositionend`,function(r){return a._compositionEnd(r.target.value)})},standalone:!1,features:[Ge([RA]),Ve]})}return t})();function Qg(t){return t==null||Xg(t)===0}function Xg(t){return t==null?null:Array.isArray(t)||typeof t==`string`?t.length:t instanceof Set?t.size:null}var qr=new k(``);var Zg=new k(``);var LA=/^(?=.{1,254}$)(?=.{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;var Po=class{static min(n){return VA(n)}static max(n){return BA(n)}static required(n){return bS(n)}static requiredTrue(n){return jA(n)}static email(n){return zA(n)}static minLength(n){return HA(n)}static maxLength(n){return UA(n)}static pattern(n){return $A(n)}static nullValidator(n){return Vu()}static compose(n){return SS(n)}static composeAsync(n){return wS(n)}};function VA(t){return n=>{if(n.value==null||t==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e<t?{min:{min:t,actual:n.value}}:null}}function BA(t){return n=>{if(n.value==null||t==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e>t?{max:{max:t,actual:n.value}}:null}}function bS(t){return Qg(t.value)?{required:!0}:null}function jA(t){return t.value===!0?null:{required:!0}}function zA(t){return Qg(t.value)||LA.test(t.value)?null:{email:!0}}function HA(t){return n=>{let e=n.value?.length??Xg(n.value);return e===null||e===0?null:e<t?{minlength:{requiredLength:t,actualLength:e}}:null}}function UA(t){return n=>{let e=n.value?.length??Xg(n.value);return e!==null&&e>t?{maxlength:{requiredLength:t,actualLength:e}}:null}}function $A(t){if(!t)return Vu;let n,e;return typeof t==`string`?(e=``,t.charAt(0)!==`^`&&(e+=`^`),e+=t,t.charAt(t.length-1)!==`$`&&(e+=`$`),n=new RegExp(e)):(e=t.toString(),n=t),i=>{if(Qg(i.value))return null;let a=i.value;return n.test(a)?null:{pattern:{requiredPattern:e,actualValue:a}}}}function Vu(t){return null}function vS(t){return t!=null}function _S(t){return xo(t)?kn(t):t}function yS(t){let n={};return t.forEach(e=>{n=e!=null?r(r({},n),e):n}),Object.keys(n).length===0?null:n}function xS(t,n){return n.map(e=>e(t))}function GA(t){return!t.validate}function CS(t){return t.map(n=>GA(n)?n:e=>n.validate(e))}function SS(t){if(!t)return null;let n=t.filter(vS);return n.length==0?null:function(e){return yS(xS(e,n))}}function Jg(t){return t!=null?SS(CS(t)):null}function wS(t){if(!t)return null;let n=t.filter(vS);return n.length==0?null:function(e){return op(xS(e,n).map(_S)).pipe(st(yS))}}function eb(t){return t!=null?wS(CS(t)):null}function cS(t,n){return t===null?[n]:Array.isArray(t)?[...t,n]:[t,n]}function ES(t){return t._rawValidators}function DS(t){return t._rawAsyncValidators}function qg(t){return t?Array.isArray(t)?t:[t]:[]}function Bu(t,n){return Array.isArray(t)?t.includes(n):t===n}function lS(t,n){let e=qg(n);return qg(t).forEach(a=>{Bu(e,a)||e.push(a)}),e}function dS(t,n){return qg(n).filter(e=>!Bu(t,e))}var ju=class{get value(){return this.control?this.control.value:null}get valid(){return this.control?this.control.valid:null}get invalid(){return this.control?this.control.invalid:null}get pending(){return this.control?this.control.pending:null}get disabled(){return this.control?this.control.disabled:null}get enabled(){return this.control?this.control.enabled:null}get errors(){return this.control?this.control.errors:null}get pristine(){return this.control?this.control.pristine:null}get dirty(){return this.control?this.control.dirty:null}get touched(){return this.control?this.control.touched:null}get status(){return this.control?this.control.status:null}get untouched(){return this.control?this.control.untouched:null}get statusChanges(){return this.control?this.control.statusChanges:null}get valueChanges(){return this.control?this.control.valueChanges:null}get path(){return null}_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators=[];_rawAsyncValidators=[];_setValidators(n){this._rawValidators=n||[],this._composedValidatorFn=Jg(this._rawValidators)}_setAsyncValidators(n){this._rawAsyncValidators=n||[],this._composedAsyncValidatorFn=eb(this._rawAsyncValidators)}get validator(){return this._composedValidatorFn||null}get asyncValidator(){return this._composedAsyncValidatorFn||null}_onDestroyCallbacks=[];_registerOnDestroy(n){this._onDestroyCallbacks.push(n)}_invokeOnDestroyCallbacks(){this._onDestroyCallbacks.forEach(n=>n()),this._onDestroyCallbacks=[]}reset(n=void 0){this.control?.reset(n)}hasError(n,e){return this.control?this.control.hasError(n,e):!1}getError(n,e){return this.control?this.control.getError(n,e):null}};var Fo=class extends ju{name;get formDirective(){return null}get path(){return null}};var Mc=`VALID`;var Lu=`INVALID`;var Ur=`PENDING`;var Tc=`DISABLED`;var Ta=class{};var zu=class extends Ta{value;source;constructor(n,e){super(),this.value=n,this.source=e}};var Ac=class extends Ta{pristine;source;constructor(n,e){super(),this.pristine=n,this.source=e}};var Oc=class extends Ta{touched;source;constructor(n,e){super(),this.touched=n,this.source=e}};var $r=class extends Ta{status;source;constructor(n,e){super(),this.status=n,this.source=e}};var Hu=class extends Ta{source;constructor(n){super(),this.source=n}};var Gr=class extends Ta{source;constructor(n){super(),this.source=n}};function IS(t){return(Yu(t)?t.validators:t)||null}function WA(t){return Array.isArray(t)?Jg(t):t||null}function kS(t,n){return(Yu(n)?n.asyncValidators:t)||null}function qA(t){return Array.isArray(t)?eb(t):t||null}function Yu(t){return t!=null&&!Array.isArray(t)&&typeof t==`object`}function YA(t,n,e){let i=t.controls;if(!(n?Object.keys(i):i).length)throw new G(1e3,``);if(!MS(i,e))throw new G(1001,``)}function KA(t,n,e){t._forEachChild((i,a)=>{if(e[a]===void 0)throw new G(-1002,``)})}var Uu=class{_pendingDirty=!1;_hasOwnPendingAsyncValidator=null;_pendingTouched=!1;_onCollectionChange=()=>{};_updateOn;_hasRequired=T(!1);_parent=null;_asyncValidationSubscription;_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators;_rawAsyncValidators;value;constructor(n,e){this._assignValidators(n),this._assignAsyncValidators(e)}get validator(){return this._composedValidatorFn}set validator(n){this._rawValidators=this._composedValidatorFn=n,this._updateHasRequiredValidator()}get asyncValidator(){return this._composedAsyncValidatorFn}set asyncValidator(n){this._rawAsyncValidators=this._composedAsyncValidatorFn=n}get parent(){return this._parent}get status(){return nt(this.statusReactive)}set status(n){nt(()=>this.statusReactive.set(n))}_status=ne(()=>this.statusReactive());statusReactive=T(void 0);get valid(){return this.status===Mc}get invalid(){return this.status===Lu}get pending(){return this.status===Ur}get disabled(){return this.status===Tc}get enabled(){return this.status!==Tc}errors;get pristine(){return nt(this.pristineReactive)}set pristine(n){nt(()=>this.pristineReactive.set(n))}_pristine=ne(()=>this.pristineReactive());pristineReactive=T(!0);get dirty(){return!this.pristine}get touched(){return nt(this.touchedReactive)}set touched(n){nt(()=>this.touchedReactive.set(n))}_touched=ne(()=>this.touchedReactive());touchedReactive=T(!1);get untouched(){return!this.touched}_events=new L;events=this._events.asObservable();valueChanges;statusChanges;get updateOn(){return this._updateOn?this._updateOn:this.parent?this.parent.updateOn:`change`}setValidators(n){this._assignValidators(n)}setAsyncValidators(n){this._assignAsyncValidators(n)}addValidators(n){this.setValidators(lS(n,this._rawValidators))}addAsyncValidators(n){this.setAsyncValidators(lS(n,this._rawAsyncValidators))}removeValidators(n){this.setValidators(dS(n,this._rawValidators))}removeAsyncValidators(n){this.setAsyncValidators(dS(n,this._rawAsyncValidators))}hasValidator(n){return Bu(this._rawValidators,n)}hasAsyncValidator(n){return Bu(this._rawAsyncValidators,n)}clearValidators(){this.validator=null}clearAsyncValidators(){this.asyncValidator=null}markAsTouched(n={}){let e=this.touched===!1;this.touched=!0;let i=n.sourceControl??this;n.onlySelf||this._parent?.markAsTouched(s(r({},n),{sourceControl:i})),e&&n.emitEvent!==!1&&this._events.next(new Oc(!0,i))}markAllAsDirty(n={}){this.markAsDirty({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsDirty(n))}markAllAsTouched(n={}){this.markAsTouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsTouched(n))}markAsUntouched(n={}){let e=this.touched===!0;this.touched=!1,this._pendingTouched=!1;let i=n.sourceControl??this;this._forEachChild(a=>{a.markAsUntouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:i})}),n.onlySelf||this._parent?._updateTouched(n,i),e&&n.emitEvent!==!1&&this._events.next(new Oc(!1,i))}markAsDirty(n={}){let e=this.pristine===!0;this.pristine=!1;let i=n.sourceControl??this;n.onlySelf||this._parent?.markAsDirty(s(r({},n),{sourceControl:i})),e&&n.emitEvent!==!1&&this._events.next(new Ac(!1,i))}markAsPristine(n={}){let e=this.pristine===!1;this.pristine=!0,this._pendingDirty=!1;let i=n.sourceControl??this;this._forEachChild(a=>{a.markAsPristine({onlySelf:!0,emitEvent:n.emitEvent})}),n.onlySelf||this._parent?._updatePristine(n,i),e&&n.emitEvent!==!1&&this._events.next(new Ac(!0,i))}markAsPending(n={}){this.status=Ur;let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new $r(this.status,e)),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.markAsPending(s(r({},n),{sourceControl:e}))}disable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=Tc,this.errors=null,this._forEachChild(a=>{a.disable(s(r({},n),{onlySelf:!0}))}),this._updateValue();let i=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new zu(this.value,i)),this._events.next(new $r(this.status,i)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),this._updateAncestors(s(r({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(a=>a(!0))}enable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=Mc,this._forEachChild(i=>{i.enable(s(r({},n),{onlySelf:!0}))}),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent}),this._updateAncestors(s(r({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(i=>i(!1))}_updateAncestors(n,e){n.onlySelf||(this._parent?.updateValueAndValidity(n),n.skipPristineCheck||this._parent?._updatePristine({},e),this._parent?._updateTouched({},e))}setParent(n){this._parent=n}getRawValue(){return this.value}updateValueAndValidity(n={}){if(this._setInitialStatus(),this._updateValue(),this.enabled){let i=this._cancelExistingSubscription();this.errors=this._runValidator(),this.status=this._calculateStatus(),(this.status===Mc||this.status===Ur)&&this._runAsyncValidator(i,n.emitEvent)}let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new zu(this.value,e)),this._events.next(new $r(this.status,e)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.updateValueAndValidity(s(r({},n),{sourceControl:e}))}_updateTreeValidity(n={emitEvent:!0}){this._forEachChild(e=>e._updateTreeValidity(n)),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent})}_setInitialStatus(){this.status=this._allControlsDisabled()?Tc:Mc}_runValidator(){return this.validator?this.validator(this):null}_runAsyncValidator(n,e){if(this.asyncValidator){this.status=Ur,this._hasOwnPendingAsyncValidator={emitEvent:e!==!1,shouldHaveEmitted:n!==!1};let i=_S(this.asyncValidator(this));this._asyncValidationSubscription=i.subscribe(a=>{this._hasOwnPendingAsyncValidator=null,this.setErrors(a,{emitEvent:e,shouldHaveEmitted:n})})}}_cancelExistingSubscription(){if(this._asyncValidationSubscription){this._asyncValidationSubscription.unsubscribe();let n=(this._hasOwnPendingAsyncValidator?.emitEvent||this._hasOwnPendingAsyncValidator?.shouldHaveEmitted)??!1;return this._hasOwnPendingAsyncValidator=null,n}return!1}setErrors(n,e={}){this.errors=n,this._updateControlsErrors(e.emitEvent!==!1,this,e.shouldHaveEmitted)}get(n){let e=n;return e==null||(Array.isArray(e)||(e=e.split(`.`)),e.length===0)?null:e.reduce((i,a)=>i&&i._find(a),this)}getError(n,e){let i=e?this.get(e):this;return i?.errors?i.errors[n]:null}hasError(n,e){return!!this.getError(n,e)}get root(){let n=this;for(;n._parent;)n=n._parent;return n}_updateControlsErrors(n,e,i){this.status=this._calculateStatus(),n&&this.statusChanges.emit(this.status),(n||i)&&this._events.next(new $r(this.status,e)),this._parent&&this._parent._updateControlsErrors(n,e,i)}_initObservables(){this.valueChanges=new ee,this.statusChanges=new ee}_calculateStatus(){return this._allControlsDisabled()?Tc:this.errors?Lu:this._hasOwnPendingAsyncValidator||this._anyControlsHaveStatus(Ur)?Ur:this._anyControlsHaveStatus(Lu)?Lu:Mc}_anyControlsHaveStatus(n){return this._anyControls(e=>e.status===n)}_anyControlsDirty(){return this._anyControls(n=>n.dirty)}_anyControlsTouched(){return this._anyControls(n=>n.touched)}_updatePristine(n,e){let i=!this._anyControlsDirty(),a=this.pristine!==i;this.pristine=i,n.onlySelf||this._parent?._updatePristine(n,e),a&&this._events.next(new Ac(this.pristine,e))}_updateTouched(n={},e){this.touched=this._anyControlsTouched(),this._events.next(new Oc(this.touched,e)),n.onlySelf||this._parent?._updateTouched(n,e)}_onDisabledChange=[];_registerOnCollectionChange(n){this._onCollectionChange=n}_setUpdateStrategy(n){Yu(n)&&n.updateOn!=null&&(this._updateOn=n.updateOn)}_parentMarkedDirty(n){return!n&&!!this._parent?.dirty&&!this._parent._anyControlsDirty()}_find(n){return null}_assignValidators(n){this._rawValidators=Array.isArray(n)?n.slice():n,this._composedValidatorFn=WA(this._rawValidators),this._updateHasRequiredValidator()}_assignAsyncValidators(n){this._rawAsyncValidators=Array.isArray(n)?n.slice():n,this._composedAsyncValidatorFn=qA(this._rawAsyncValidators)}_updateHasRequiredValidator(){nt(()=>this._hasRequired.set(this.hasValidator(Po.required)))}};function MS(t,n){return Object.hasOwn(t,n)}function QA(t){return t.tagName===`INPUT`||t.tagName===`SELECT`||t.tagName===`TEXTAREA`}function XA(t,n,e,i){switch(e){case`name`:t.setAttribute(n,e,i);break;case`disabled`:case`readonly`:case`required`:i?t.setAttribute(n,e,``):t.removeAttribute(n,e);break;case`max`:case`min`:case`minLength`:case`maxLength`:i!==void 0?t.setAttribute(n,e,i.toString()):t.removeAttribute(n,e)}}var Yg=class{kind;context;control;message;constructor({kind:n,context:e,control:i}){this.kind=n,this.context=e,this.control=i}};var ZA=(()=>{class t{_validator=Vu;_onChange;_enabled;ngOnChanges(e){if(this.inputName in e){let i=this.normalizeInput(e[this.inputName].currentValue);this._enabled=this.enabled(i),this._validator=this._enabled?this.createValidator(i):Vu,this._onChange?.()}}validate(e){return this._validator(e)}registerOnValidatorChange(e){this._onChange=e}enabled(e){return e!=null}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,features:[vt]})}return t})();var JA={provide:qr,useExisting:Ht(()=>TS),multi:!0};var TS=(()=>{class t extends ZA{required;inputName=`required`;normalizeInput=V;createValidator=e=>bS;enabled(e){return e}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=K({type:t,selectors:[[``,`required`,``,`formControlName`,``,3,`type`,`checkbox`],[``,`required`,``,`formControl`,``,3,`type`,`checkbox`],[``,`required`,``,`ngModel`,``,3,`type`,`checkbox`]],hostVars:1,hostBindings:function(i,a){i&2&&te(`required`,a._enabled?``:null)},inputs:{required:`required`},standalone:!1,features:[Ge([JA]),Ve]})}return t})();var eO=new k(``);var Ku=new k(``,{factory:()=>tb});var tb=`always`;function tO(t,n){return[...n.path,t]}function uS(t,n,e=tb){nb(t,n),n.valueAccessor.writeValue(t.value),(t.disabled||e===`always`)&&n.valueAccessor.setDisabledState?.(t.disabled),iO(t,n),oO(t,n),aO(t,n),nO(t,n)}function mS(t,n,e=!0){let i=()=>{};n?.valueAccessor?.registerOnChange(i),n?.valueAccessor?.registerOnTouched(i),Gu(t,n),t&&(n._invokeOnDestroyCallbacks(),t._registerOnCollectionChange(()=>{}))}function $u(t,n){t.forEach(e=>{e.registerOnValidatorChange&&e.registerOnValidatorChange(n)})}function nO(t,n){if(n.valueAccessor.setDisabledState){let e=i=>{n.valueAccessor.setDisabledState(i)};t.registerOnDisabledChange(e),n._registerOnDestroy(()=>{t._unregisterOnDisabledChange(e)})}}function nb(t,n){let e=ES(t);n.validator!==null?t.setValidators(cS(e,n.validator)):typeof e==`function`&&t.setValidators([e]);let i=DS(t);n.asyncValidator!==null?t.setAsyncValidators(cS(i,n.asyncValidator)):typeof i==`function`&&t.setAsyncValidators([i]);let a=()=>t.updateValueAndValidity();$u(n._rawValidators,a),$u(n._rawAsyncValidators,a)}function Gu(t,n){let e=!1;if(t!==null){if(n.validator!==null){let a=ES(t);if(Array.isArray(a)&&a.length>0){let o=a.filter(r=>r!==n.validator);o.length!==a.length&&(e=!0,t.setValidators(o))}}if(n.asyncValidator!==null){let a=DS(t);if(Array.isArray(a)&&a.length>0){let o=a.filter(r=>r!==n.asyncValidator);o.length!==a.length&&(e=!0,t.setAsyncValidators(o))}}}let i=()=>{};return $u(n._rawValidators,i),$u(n._rawAsyncValidators,i),e}function iO(t,n){n.valueAccessor.registerOnChange(e=>{t._pendingValue=e,t._pendingChange=!0,t._pendingDirty=!0,t.updateOn===`change`&&NS(t,n)})}function aO(t,n){n.valueAccessor.registerOnTouched(()=>{t._pendingTouched=!0,t.updateOn===`blur`&&t._pendingChange&&NS(t,n),t.updateOn!==`submit`&&t.markAsTouched()})}function NS(t,n){t._pendingDirty&&t.markAsDirty(),t.setValue(t._pendingValue,{emitModelToViewChange:!1}),n.viewToModelUpdate(t._pendingValue),t._pendingChange=!1}function oO(t,n){let e=(i,a)=>{n.valueAccessor.writeValue(i),a&&n.viewToModelUpdate(i)};t.registerOnChange(e),n._registerOnDestroy(()=>{t._unregisterOnChange(e)})}function AS(t,n){nb(t,n)}function rO(t,n){return Gu(t,n)}function sO(t,n){if(!Object.hasOwn(t,`model`))return!1;let e=t.model;return e.isFirstChange()?!0:!Object.is(n,e.currentValue)}function cO(t){return Object.getPrototypeOf(t.constructor)===OA}function OS(t,n){t._syncPendingControls(),n.forEach(e=>{let i=e.control;i.updateOn===`submit`&&i._pendingChange&&(e.viewToModelUpdate(i._pendingValue),i._pendingChange=!1)})}function lO(t,n){if(!n)return null;let e,i,a;return n.forEach(o=>{o.constructor===qu?e=o:cO(o)?i=o:a=o}),a||i||e||null}function dO(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}var uO={provide:eO,useFactory:()=>{let t=f(Mi,{self:!0});return{setParseErrors:n=>{t.setParseErrorSource(n)},set onReset(n){t.onReset=n}}}};var Mi=class extends ju{_parent=null;name=null;valueAccessor=null;isCustomControlBased=!1;userOnReset;resetSubscription;set onReset(n){this.userOnReset=n,this.resetSubscription?.unsubscribe(),this.resetSubscription=void 0,this.control&&(this.resetSubscription=this.control.events.subscribe(e=>{e instanceof Gr&&this.control&&this.userOnReset?.(this.control.value)}),this.subscription?.add(this.resetSubscription))}isNativeFormElement=!1;rawValueAccessors;_selectedValueAccessor=null;get selectedValueAccessor(){return this._selectedValueAccessor??=lO(this,this.rawValueAccessors)}parseErrorsValidator=null;renderer;injector;requiredValidatorViaDi;subscription;customControlBindings=null;constructor(n,e,i){super(),this.injector=n,this.renderer=e,this.rawValueAccessors=i,this.injector?.get(Ne)?.onDestroy(()=>{this.removeParseErrorsValidator(this.control),this.subscription?.unsubscribe()})}setupCustomControl(){this.subscription?.unsubscribe();let n=this.injector?.get(Fe);if(!this.control||!n)return;let e=n.markForCheck.bind(n);this.subscription=new De,this.subscription.add(this.control.valueChanges.subscribe(e)),this.subscription.add(this.control.statusChanges.subscribe(e)),this.resetSubscription?.unsubscribe(),this.resetSubscription=void 0,this.userOnReset&&(this.resetSubscription=this.control.events.subscribe(i=>{i instanceof Gr&&this.control&&this.userOnReset?.(this.control.value)}),this.subscription.add(this.resetSubscription)),this.parseErrorsValidator&&this.control.addValidators(this.parseErrorsValidator)}ngControlCreate(n){!n.nativeElement.hasAttribute?.(`ngNoCva`)&&(this.rawValueAccessors&&this.rawValueAccessors.length>0||this.valueAccessor!==null)||!n.customControl||(this.isCustomControlBased=!0,n.listenToCustomControlModel(a=>{this.control?.markAsDirty(),this.control?.setValue(a,{emitModelToViewChange:!1}),this.viewToModelUpdate(a)}),n.listenToCustomControlOutput(`touch`,()=>{this.control?.markAsTouched()}),this.customControlBindings={},this.isNativeFormElement=QA(n.nativeElement),this.requiredValidatorViaDi=this._rawValidators.find(a=>a instanceof TS))}ngControlUpdate(n,e){if(!this.isCustomControlBased)return;let i=this.control,a=this.customControlBindings;Object.is(a.value,i.value)||(a.value=i.value,n.setCustomControlModelInput(i.value)),this.bindControlProperty(n,a,`touched`,i.touched),this.bindControlProperty(n,a,`dirty`,i.dirty),this.bindControlProperty(n,a,`valid`,i.valid),this.bindControlProperty(n,a,`invalid`,i.invalid),this.bindControlProperty(n,a,`pending`,i.pending),this.bindControlProperty(n,a,`disabled`,i.disabled),this.shouldBindRequired&&this.bindControlProperty(n,a,`required`,this.isRequired);let o=i.errors;if(a.errors!==o){a.errors=o;let r=this._convertErrors(o);n.setInputOnDirectives(`errors`,r)}}get isRequired(){return(this.requiredValidatorViaDi?._enabled||this.control?._hasRequired())??!1}get shouldBindRequired(){return!0}bindControlProperty(n,e,i,a){if(e[i]===a)return;e[i]=a;let o=n.setInputOnDirectives(i,a);this.isNativeFormElement&&!o&&(i===`disabled`||i===`required`)&&this.renderer&&XA(this.renderer,n.nativeElement,i,a)}_convertErrors(n){if(n===null)return[];let e=this.control;return Object.entries(n).map(([i,a])=>new Yg({context:a,kind:i,control:e}))}setParseErrorSource(n){if(n===void 0)return;let e=null,i=ne(()=>{let a=n();return a.length===0?null:a.reduce((o,r)=>(o[r.kind]=r,o),{})});this.parseErrorsValidator=(()=>e).bind(this),Lt(()=>{e=i(),this.control?.updateValueAndValidity({emitEvent:!1})},{injector:this.injector})}removeParseErrorsValidator(n){this.parseErrorsValidator&&(n?.removeValidators(this.parseErrorsValidator),n?.updateValueAndValidity({emitEvent:!1}))}};var Kg=class{_cd;constructor(n){this._cd=n}get isTouched(){return this._cd?.control?._touched?.(),!!this._cd?.control?.touched}get isUntouched(){return!!this._cd?.control?.untouched}get isPristine(){return this._cd?.control?._pristine?.(),!!this._cd?.control?.pristine}get isDirty(){return!!this._cd?.control?.dirty}get isValid(){return this._cd?.control?._status?.(),!!this._cd?.control?.valid}get isInvalid(){return!!this._cd?.control?.invalid}get isPending(){return!!this._cd?.control?.pending}get isSubmitted(){return this._cd?._submitted?.(),!!this._cd?.submitted}};var RS=(()=>{class t extends Kg{constructor(e){super(e)}static ɵfac=function(i){return new(i||t)(He(Mi,2))};static ɵdir=K({type:t,selectors:[[``,`formControlName`,``],[``,`ngModel`,``],[``,`formControl`,``]],hostVars:14,hostBindings:function(i,a){i&2&&Y(`ng-untouched`,a.isUntouched)(`ng-touched`,a.isTouched)(`ng-pristine`,a.isPristine)(`ng-dirty`,a.isDirty)(`ng-valid`,a.isValid)(`ng-invalid`,a.isInvalid)(`ng-pending`,a.isPending)},standalone:!1,features:[Ve]})}return t})();var Wu=class extends Uu{constructor(n,e,i){super(IS(e),kS(i,e)),this.controls=n,this._initObservables(),this._setUpdateStrategy(e),this._setUpControls(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator})}controls;registerControl(n,e){return this._find(n)||(this.controls[n]=e,e.setParent(this),e._registerOnCollectionChange(this._onCollectionChange),e)}addControl(n,e,i={}){this.registerControl(n,e),this.updateValueAndValidity({emitEvent:i.emitEvent}),this._onCollectionChange()}removeControl(n,e={}){let i=this._find(n);i&&i._registerOnCollectionChange(()=>{}),delete this.controls[n],this.updateValueAndValidity({emitEvent:e.emitEvent}),this._onCollectionChange()}setControl(n,e,i={}){let a=this._find(n);a&&a._registerOnCollectionChange(()=>{}),delete this.controls[n],e&&this.registerControl(n,e),this.updateValueAndValidity({emitEvent:i.emitEvent}),this._onCollectionChange()}contains(n){return this._find(n)?.enabled===!0}setValue(n,e={}){nt(()=>{KA(this,!0,n),Object.keys(n).forEach(i=>{YA(this,!0,i),this.controls[i].setValue(n[i],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e)})}patchValue(n,e={}){n!=null&&(Object.keys(n).forEach(i=>{let a=this._find(i);a&&a.patchValue(n[i],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e))}reset(n={},e={}){this._forEachChild((i,a)=>{i.reset(n?n[a]:null,s(r({},e),{onlySelf:!0}))}),this._updatePristine(e,this),this._updateTouched(e,this),this.updateValueAndValidity(e),e?.emitEvent!==!1&&this._events.next(new Gr(this))}getRawValue(){return this._reduceChildren({},(n,e,i)=>(n[i]=e.getRawValue(),n))}_syncPendingControls(){let n=this._reduceChildren(!1,(e,i)=>i._syncPendingControls()?!0:e);return n&&this.updateValueAndValidity({onlySelf:!0}),n}_forEachChild(n){Object.keys(this.controls).forEach(e=>{let i=this.controls[e];i&&n(i,e)})}_setUpControls(){this._forEachChild(n=>{n.setParent(this),n._registerOnCollectionChange(this._onCollectionChange)})}_updateValue(){this.value=this._reduceValue()}_anyControls(n){for(let[e,i]of Object.entries(this.controls))if(this.contains(e)&&n(i))return!0;return!1}_reduceValue(){return this._reduceChildren({},(e,i,a)=>((i.enabled||this.disabled)&&(e[a]=i.value),e))}_reduceChildren(n,e){let i=n;return this._forEachChild((a,o)=>{i=e(i,a,o)}),i}_allControlsDisabled(){for(let n of Object.keys(this.controls))if(this.controls[n].enabled)return!1;return Object.keys(this.controls).length>0||this.disabled}_find(n){return MS(this.controls,n)?this.controls[n]:null}};var mO={provide:Fo,useExisting:Ht(()=>Rc)};var Nc=Promise.resolve();var Rc=(()=>{class t extends Fo{callSetDisabledState;get submitted(){return nt(this.submittedReactive)}_submitted=ne(()=>this.submittedReactive());submittedReactive=T(!1);_directives=new Set;form;ngSubmit=new ee;options;constructor(e,i,a){super(),this.callSetDisabledState=a,this.form=new Wu({},Jg(e),eb(i))}ngAfterViewInit(){this._setUpdateStrategy()}get formDirective(){return this}get control(){return this.form}get path(){return[]}get controls(){return this.form.controls}addControl(e){Nc.then(()=>{e.control=this._findContainer(e.path).registerControl(e.name,e.control),e._setupWithForm(this.callSetDisabledState),e.control.updateValueAndValidity({emitEvent:!1}),this._directives.add(e)})}getControl(e){return this.form.get(e.path)}removeControl(e){Nc.then(()=>{this._findContainer(e.path)?.removeControl(e.name),this._directives.delete(e)})}addFormGroup(e){Nc.then(()=>{let i=this._findContainer(e.path),a=new Wu({});AS(a,e),i.registerControl(e.name,a),a.updateValueAndValidity({emitEvent:!1})})}removeFormGroup(e){Nc.then(()=>{this._findContainer(e.path)?.removeControl?.(e.name)})}getFormGroup(e){return this.form.get(e.path)}updateModel(e,i){Nc.then(()=>{this.form.get(e.path).setValue(i)})}setValue(e){this.control.setValue(e)}onSubmit(e){return this.submittedReactive.set(!0),OS(this.form,this._directives),this.ngSubmit.emit(e),this.form._events.next(new Hu(this.control)),e?.target?.method===`dialog`}onReset(){this.resetForm()}resetForm(e=void 0){this.form.reset(e),this.submittedReactive.set(!1)}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.form._updateOn=this.options.updateOn)}_findContainer(e){return e.pop(),e.length?this.form.get(e):this.form}static ɵfac=function(i){return new(i||t)(He(qr,10),He(Zg,10),He(Ku,8))};static ɵdir=K({type:t,selectors:[[`form`,3,`ngNoForm`,``,3,`formGroup`,``,3,`formArray`,``],[`ng-form`],[``,`ngForm`,``]],hostBindings:function(i,a){i&1&&C(`submit`,function(r){return a.onSubmit(r)})(`reset`,function(){return a.onReset()})},inputs:{options:[0,`ngFormOptions`,`options`]},outputs:{ngSubmit:`ngSubmit`},exportAs:[`ngForm`],standalone:!1,features:[Ge([mO]),Ve]})}return t})();function pS(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}function hS(t){return typeof t==`object`&&t!==null&&Object.keys(t).length===2&&`value`in t&&`disabled`in t}var PS=class extends Uu{defaultValue=null;_onChange=[];_pendingValue;_pendingChange=!1;constructor(n=null,e,i){super(IS(e),kS(i,e)),this._applyFormState(n),this._setUpdateStrategy(e),this._initObservables(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator}),Yu(e)&&(e.nonNullable||e.initialValueIsDefault)&&(hS(n)?this.defaultValue=n.value:this.defaultValue=n)}setValue(n,e={}){nt(()=>{this.value=this._pendingValue=n,this._onChange.length&&e.emitModelToViewChange!==!1&&this._onChange.forEach(i=>i(this.value,e.emitViewToModelChange!==!1)),this.updateValueAndValidity(e)})}patchValue(n,e={}){this.setValue(n,e)}reset(n=this.defaultValue,e={}){this._applyFormState(n),this.markAsPristine(e),this.markAsUntouched(e),this.setValue(this.value,e),e.overwriteDefaultValue&&(this.defaultValue=this.value),this._pendingChange=!1,e?.emitEvent!==!1&&this._events.next(new Gr(this))}_updateValue(){}_anyControls(n){return!1}_allControlsDisabled(){return this.disabled}registerOnChange(n){this._onChange.push(n)}_unregisterOnChange(n){pS(this._onChange,n)}registerOnDisabledChange(n){this._onDisabledChange.push(n)}_unregisterOnDisabledChange(n){pS(this._onDisabledChange,n)}_forEachChild(n){}_syncPendingControls(){return this.updateOn===`submit`&&(this._pendingDirty&&this.markAsDirty(),this._pendingTouched&&this.markAsTouched(),this._pendingChange)?(this.setValue(this._pendingValue,{onlySelf:!0,emitModelToViewChange:!1}),!0):!1}_applyFormState(n){hS(n)?(this.value=this._pendingValue=n.value,n.disabled?this.disable({onlySelf:!0,emitEvent:!1}):this.enable({onlySelf:!0,emitEvent:!1})):this.value=this._pendingValue=n}};var pO=t=>t instanceof PS;var hO=(()=>{class t extends Fo{callSetDisabledState;get submitted(){return nt(this._submittedReactive)}set submitted(e){this._submittedReactive.set(e)}_submitted=ne(()=>this._submittedReactive());_submittedReactive=T(!1);_oldForm;_onCollectionChange=()=>this._updateDomValue();directives=[];constructor(e,i,a){super(),this.callSetDisabledState=a,this._setValidators(e),this._setAsyncValidators(i)}ngOnChanges(e){this.onChanges(e)}ngOnDestroy(){this.onDestroy()}onChanges(e){this._checkFormPresent(),Object.hasOwn(e,`form`)&&(this._updateValidators(),this._updateDomValue(),this._updateRegistrations(),this._oldForm=this.form)}onDestroy(){this.form&&(Gu(this.form,this),this.form._onCollectionChange===this._onCollectionChange&&this.form._registerOnCollectionChange(()=>{}))}get formDirective(){return this}get path(){return[]}addControl(e){let i=this.form.get(e.path);return e._setupWithForm(i,this.callSetDisabledState),i.updateValueAndValidity({emitEvent:!1}),this.directives.push(e),i}getControl(e){return this.form.get(e.path)}removeControl(e){mS(e.control||null,e,!1),dO(this.directives,e)}addFormGroup(e){this._setUpFormContainer(e)}removeFormGroup(e){this._cleanUpFormContainer(e)}getFormGroup(e){return this.form.get(e.path)}getFormArray(e){return this.form.get(e.path)}addFormArray(e){this._setUpFormContainer(e)}removeFormArray(e){this._cleanUpFormContainer(e)}updateModel(e,i){this.form.get(e.path).setValue(i)}onReset(){this.resetForm()}resetForm(e=void 0,i={}){this.form.reset(e,i),this._submittedReactive.set(!1)}onSubmit(e){return this.submitted=!0,OS(this.form,this.directives),this.ngSubmit.emit(e),this.form._events.next(new Hu(this.control)),e?.target?.method===`dialog`}_updateDomValue(){this.directives.forEach(e=>{let i=e.control,a=this.form.get(e.path);i!==a&&(mS(i||null,e),pO(a)&&e._setupWithForm(a,this.callSetDisabledState))}),this.form._updateTreeValidity({emitEvent:!1})}_setUpFormContainer(e){let i=this.form.get(e.path);AS(i,e),i.updateValueAndValidity({emitEvent:!1})}_cleanUpFormContainer(e){let i=this.form?.get(e.path);i&&rO(i,e)&&i.updateValueAndValidity({emitEvent:!1})}_updateRegistrations(){this.form._registerOnCollectionChange(this._onCollectionChange),this._oldForm?._registerOnCollectionChange(()=>{})}_updateValidators(){nb(this.form,this),this._oldForm&&Gu(this._oldForm,this)}_checkFormPresent(){this.form}static ɵfac=function(i){return new(i||t)(He(qr,10),He(Zg,10),He(Ku,8))};static ɵdir=K({type:t,features:[Ve,vt]})}return t})();var fO={provide:Fo,useExisting:Ht(()=>Pc)};var Pc=(()=>{class t extends hO{form=null;ngSubmit=new ee;get control(){return this.form}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=K({type:t,selectors:[[``,`formGroup`,``]],hostBindings:function(i,a){i&1&&C(`submit`,function(r){return a.onSubmit(r)})(`reset`,function(){return a.onReset()})},inputs:{form:[0,`formGroup`,`form`]},outputs:{ngSubmit:`ngSubmit`},exportAs:[`ngForm`],standalone:!1,features:[Ge([fO]),Ve]})}return t})();var gO={provide:Mi,useExisting:Ht(()=>ib)};var fS=Promise.resolve();var ib=(()=>{class t extends Mi{_changeDetectorRef;callSetDisabledState;control=new PS;static ngAcceptInputType_isDisabled;_registered=!1;_ngModelInjector;viewModel;name=``;isDisabled;model;options;update=new ee;constructor(e,i,a,o,r,s,c,h){super(c,h,o),this._changeDetectorRef=r,this.callSetDisabledState=s,this._parent=e,this._setValidators(i),this._setAsyncValidators(a)}ngOnChanges(e){if(this._registered,this._checkForErrors(),!this._registered||`name`in e){if(this._registered&&(this._checkName(),this.formDirective)){let i=e.name.previousValue;this.formDirective.removeControl({name:i,path:this._getPath(i)})}this._setUpControl()}`isDisabled`in e&&this._updateDisabled(e),sO(e,this.viewModel)&&(this._updateValue(this.model),this.viewModel=this.model)}ngOnDestroy(){this.formDirective?.removeControl(this)}ɵngControlCreate(e){super.ngControlCreate(e)}ɵngControlUpdate(e){super.ngControlUpdate(e,!1)}get shouldBindRequired(){return!1}get path(){return this._getPath(this.name)}get formDirective(){return this._parent?this._parent.formDirective:null}viewToModelUpdate(e){this.viewModel=e,this.update.emit(e)}_setUpControl(){this._setUpdateStrategy(),this._isStandalone()?this._setUpStandalone():this.formDirective.addControl(this),this._registered=!0}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.control._updateOn=this.options.updateOn)}_isStandalone(){return!this._parent||!!(this.options&&this.options.standalone)}_setUpStandalone(){this.isCustomControlBased?this.setupCustomControl():(this.valueAccessor??=this.selectedValueAccessor,uS(this.control,this,this.callSetDisabledState)),this.control.updateValueAndValidity({emitEvent:!1})}_setupWithForm(e){this.isCustomControlBased?this.setupCustomControl():(this.valueAccessor??=this.selectedValueAccessor,uS(this.control,this,e))}_checkForErrors(){this._checkName()}_checkName(){this.options&&this.options.name&&(this.name=this.options.name),!this._isStandalone()&&this.name}_updateValue(e){fS.then(()=>{this.control.setValue(e,{emitViewToModelChange:!1}),this._changeDetectorRef?.markForCheck()})}_updateDisabled(e){let i=e.isDisabled.currentValue,a=i!==0&&V(i);fS.then(()=>{a&&!this.control.disabled?this.control.disable():!a&&this.control.disabled&&this.control.enable(),this._changeDetectorRef?.markForCheck()})}_getPath(e){return this._parent?tO(e,this._parent):[e]}static ɵfac=function(i){return new(i||t)(He(Fo,9),He(qr,10),He(Zg,10),He(Wr,10),He(Fe,8),He(Ku,8),He(se,8),He(Re,8))};static ɵdir=K({type:t,selectors:[[``,`ngModel`,``,3,`formControlName`,``,3,`formControl`,``]],inputs:{name:`name`,isDisabled:[0,`disabled`,`isDisabled`],model:[0,`ngModel`,`model`],options:[0,`ngModelOptions`,`options`]},outputs:{update:`ngModelChange`},exportAs:[`ngModel`],standalone:!1,features:[Ge([gO,uO]),Ve,vt,$f(null)]})}return t})();var bO=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({})}return t})();var FS=(()=>{class t{static withConfig(e){return{ngModule:t,providers:[{provide:Ku,useValue:e.callSetDisabledState??tb}]}}static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[bO]})}return t})();var VS=Symbol(`FIELD_TREE`);function Fc(t){return typeof t==`function`&&t[VS]===!0}var Qu=new k(``);var Yr=(()=>{class t{isErrorState(e,i){return!!(e&&e.invalid&&(e.touched||i&&i.submitted))}isSignalErrorState(e){if(!e)return!1;let i=e().invalid(),a=e().touched();return i&&a}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var Kr=class{_defaultMatcher;_parentFormGroup;_parentForm;_stateChanges;errorState=!1;matcher;ngControl;formField;constructor(n,e,i,a,o){this._defaultMatcher=n,this._parentFormGroup=i,this._parentForm=a,this._stateChanges=o,e?sn(e.field)&&!e.updateValueAndValidity?(this.formField=e,this.ngControl=null):(this.formField=null,this.ngControl=e):this.ngControl=this.formField=null}updateErrorState(){let n=this.errorState,e=this._getCurrentErrorState(this.matcher||this._defaultMatcher);e!==n&&(this.errorState=e,this._stateChanges.next())}_getCurrentErrorState(n){if(this.formField&&n?.isSignalErrorState)return n.isSignalErrorState(this.formField.field())??!1;let e=this._parentFormGroup||this._parentForm,i=this.ngControl?this.ngControl.control:null;return n?.isErrorState(i,e)??!1}};var ab=class{_box;_destroyed=new L;_resizeSubject=new L;_resizeObserver;_elementObservables=new Map;constructor(n){this._box=n,typeof ResizeObserver<`u`&&(this._resizeObserver=new ResizeObserver(e=>this._resizeSubject.next(e)))}observe(n){return this._elementObservables.has(n)||this._elementObservables.set(n,new Ce(e=>{let i=this._resizeSubject.subscribe(e);return this._resizeObserver?.observe(n,{box:this._box}),()=>{this._resizeObserver?.unobserve(n),i.unsubscribe(),this._elementObservables.delete(n)}}).pipe(Qe(e=>e.some(i=>i.target===n)),Fl({bufferSize:1,refCount:!0}),Te(this._destroyed))),this._elementObservables.get(n)}destroy(){this._destroyed.next(),this._destroyed.complete(),this._resizeSubject.complete(),this._elementObservables.clear()}};var Xu=(()=>{class t{_cleanupErrorListener;_observers=new Map;_ngZone=f(X);constructor(){}ngOnDestroy(){for(let[,e]of this._observers)e.destroy();this._observers.clear(),this._cleanupErrorListener?.()}observe(e,i){let a=i?.box||`content-box`;return this._observers.has(a)||this._observers.set(a,new ab(a)),this._observers.get(a).observe(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var na=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[`mat-label`]]})}return t})();var _O=new k(`MatError`);var ob=(()=>{class t{align=`start`;id=f(Be).getId(`mat-mdc-hint-`);static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[`mat-hint`]],hostAttrs:[1,`mat-mdc-form-field-hint`,`mat-mdc-form-field-bottom-align`],hostVars:4,hostBindings:function(i,a){i&2&&(qt(`id`,a.id),te(`align`,null),Y(`mat-mdc-form-field-hint-end`,a.align===`end`))},inputs:{align:`align`,id:`id`}})}return t})();var yO=new k(`MatPrefix`);var xO=new k(`MatSuffix`);var WS=new k(`FloatingLabelParent`);var jS=(()=>{class t{_elementRef=f(W);get floating(){return this._floating}set floating(e){this._floating=e,this.monitorResize&&this._handleResize()}_floating=!1;get monitorResize(){return this._monitorResize}set monitorResize(e){this._monitorResize=e,this._monitorResize?this._subscribeToResize():this._resizeSubscription.unsubscribe()}_monitorResize=!1;_resizeObserver=f(Xu);_ngZone=f(X);_parent=f(WS);_resizeSubscription=new De;ngOnDestroy(){this._resizeSubscription.unsubscribe()}getWidth(){return CO(this._elementRef.nativeElement)}get element(){return this._elementRef.nativeElement}_handleResize(){setTimeout(()=>this._parent._handleLabelResized())}_subscribeToResize(){this._resizeSubscription.unsubscribe(),this._ngZone.runOutsideAngular(()=>{this._resizeSubscription=this._resizeObserver.observe(this._elementRef.nativeElement,{box:`border-box`}).subscribe(()=>this._handleResize())})}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[`label`,`matFormFieldFloatingLabel`,``]],hostAttrs:[1,`mdc-floating-label`,`mat-mdc-floating-label`],hostVars:2,hostBindings:function(i,a){i&2&&Y(`mdc-floating-label--float-above`,a.floating)},inputs:{floating:`floating`,monitorResize:`monitorResize`}})}return t})();function CO(t){let n=t;if(n.offsetParent!==null)return n.scrollWidth;let e=n.cloneNode(!0);e.style.setProperty(`position`,`absolute`),e.style.setProperty(`transform`,`translate(-9999px, -9999px)`),document.documentElement.appendChild(e);let i=e.scrollWidth;return e.remove(),i}var zS=`mdc-line-ripple--active`;var Zu=`mdc-line-ripple--deactivating`;var HS=(()=>{class t{_elementRef=f(W);_cleanupTransitionEnd;constructor(){let e=f(X),i=f(Re);e.runOutsideAngular(()=>{this._cleanupTransitionEnd=i.listen(this._elementRef.nativeElement,`transitionend`,this._handleTransitionEnd)})}activate(){let e=this._elementRef.nativeElement.classList;e.remove(Zu),e.add(zS)}deactivate(){this._elementRef.nativeElement.classList.add(Zu)}_handleTransitionEnd=e=>{let i=this._elementRef.nativeElement.classList,a=i.contains(Zu);e.propertyName===`opacity`&&a&&i.remove(zS,Zu)};ngOnDestroy(){this._cleanupTransitionEnd()}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[`div`,`matFormFieldLineRipple`,``]],hostAttrs:[1,`mdc-line-ripple`]})}return t})();var US=(()=>{class t{_elementRef=f(W);_ngZone=f(X);open=!1;_notch;ngAfterViewInit(){let e=this._elementRef.nativeElement,i=e.querySelector(`.mdc-floating-label`);i?(e.classList.add(`mdc-notched-outline--upgraded`),typeof requestAnimationFrame==`function`&&(i.style.transitionDuration=`0s`,this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>i.style.transitionDuration=``)}))):e.classList.add(`mdc-notched-outline--no-label`)}_setNotchWidth(e){let i=this._notch.nativeElement;!this.open||!e?i.style.width=``:i.style.width=`calc(${e}px * var(--mat-mdc-form-field-floating-label-scale, 0.75) + 9px)`}_setMaxWidth(e){this._notch.nativeElement.style.setProperty(`--mat-form-field-notch-max-width`,`calc(100% - ${e}px)`)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`notch`];return B({type:t,selectors:[[`div`,`matFormFieldNotchedOutline`,``]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;Z(s=J())&&(r._notch=s.first)}},hostAttrs:[1,`mdc-notched-outline`],hostVars:2,hostBindings:function(o,r){o&2&&Y(`mdc-notched-outline--notched`,r.open)},inputs:{open:[0,`matFormFieldNotchedOutlineOpen`,`open`]},ngContentSelectors:[`*`],decls:5,vars:0,consts:[[`notch`,``],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__leading`],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__notch`],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__trailing`]],template:function(o,r){o&1&&(Pe(),Wt(0,`div`,1),ct(1,`div`,2,0),he(3),ht(),Wt(4,`div`,3))},encapsulation:2})})()}return t})();var Lc=(()=>{class t{id;ngField=null;ngControl=null;focused=!1;empty=!1;shouldLabelFloat=!1;required=!1;disabled=!1;errorState=!1;controlType;autofilled;userAriaDescribedBy;disableAutomaticLabeling;describedByIds;stateChanges=null;value;static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t})}return t})();var Vc=new k(`MatFormField`);var SO=new k(`MAT_FORM_FIELD_DEFAULT_OPTIONS`);var $S=`fill`;var wO=`auto`;var GS=`fixed`;var EO=`translateY(-50%)`;var Ti=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Fe);_platform=f(Oe);_idGenerator=f(Be);_ngZone=f(X);_defaults=f(SO,{optional:!0});_currentDirection;_unwrapMaybeSignal(e){return sn(e)?e():e}_textField;_iconPrefixContainer;_textPrefixContainer;_iconSuffixContainer;_textSuffixContainer;_floatingLabel;_notchedOutline;_lineRipple;_iconPrefixContainerSignal=lc(`iconPrefixContainer`);_textPrefixContainerSignal=lc(`textPrefixContainer`);_iconSuffixContainerSignal=lc(`iconSuffixContainer`);_textSuffixContainerSignal=lc(`textSuffixContainer`);_prefixSuffixContainers=ne(()=>[this._iconPrefixContainerSignal(),this._textPrefixContainerSignal(),this._iconSuffixContainerSignal(),this._textSuffixContainerSignal()].map(e=>e?.nativeElement).filter(e=>e!==void 0));_formFieldControl;_prefixChildren;_suffixChildren;_errorChildren;_hintChildren;_labelChild=yx(na);get hideRequiredMarker(){return this._hideRequiredMarker}set hideRequiredMarker(e){this._hideRequiredMarker=ta(e)}_hideRequiredMarker=!1;color=`primary`;get floatLabel(){return this._floatLabel||this._defaults?.floatLabel||wO}set floatLabel(e){e!==this._floatLabel&&(this._floatLabel=e,this._changeDetectorRef.markForCheck())}_floatLabel;get appearance(){return this._appearanceSignal()}set appearance(e){let i=e||this._defaults?.appearance||$S;this._appearanceSignal.set(i)}_appearanceSignal=T($S);get subscriptSizing(){return this._subscriptSizing||this._defaults?.subscriptSizing||GS}set subscriptSizing(e){this._subscriptSizing=e||this._defaults?.subscriptSizing||GS}_subscriptSizing=null;get hintLabel(){return this._hintLabel}set hintLabel(e){this._hintLabel=e,this._processHints()}_hintLabel=``;_hasIconPrefix=!1;_hasTextPrefix=!1;_hasIconSuffix=!1;_hasTextSuffix=!1;_labelId=this._idGenerator.getId(`mat-mdc-form-field-label-`);_hintLabelId=this._idGenerator.getId(`mat-mdc-hint-`);_describedByIds;get _control(){return this._explicitFormFieldControl||this._formFieldControl}set _control(e){this._explicitFormFieldControl=e}_destroyed=new L;_isFocused=null;_explicitFormFieldControl;_previousControl=null;_previousControlValidatorFn=null;_stateChanges;_valueChanges;_describedByChanges;_outlineLabelOffsetResizeObserver=null;_animationsDisabled=Ue();constructor(){let e=this._defaults,i=f(gt);e&&(e.appearance&&(this.appearance=e.appearance),this._hideRequiredMarker=!!e?.hideRequiredMarker,e.color&&(this.color=e.color)),Lt(()=>this._currentDirection=i.valueSignal()),this._syncOutlineLabelOffset()}ngAfterViewInit(){this._updateFocusState(),this._animationsDisabled||this._ngZone.runOutsideAngular(()=>{setTimeout(()=>{this._elementRef.nativeElement.classList.add(`mat-form-field-animations-enabled`)},300)}),this._changeDetectorRef.detectChanges()}ngAfterContentInit(){this._assertFormFieldControl(),this._initializeSubscript(),this._initializePrefixAndSuffix()}ngAfterContentChecked(){this._assertFormFieldControl(),this._control!==this._previousControl&&(this._initializeControl(this._previousControl),this._control.ngControl&&this._control.ngControl.control&&(this._previousControlValidatorFn=Fc(this._control.ngField)?null:this._control.ngControl.control.validator),this._previousControl=this._control,this._changeDetectorRef.markForCheck()),this._control.ngControl&&this._control.ngControl.control&&!Fc(this._control.ngField)&&this._control.ngControl.control.validator!==this._previousControlValidatorFn&&this._changeDetectorRef.markForCheck()}ngOnDestroy(){this._outlineLabelOffsetResizeObserver?.disconnect(),this._stateChanges?.unsubscribe(),this._valueChanges?.unsubscribe(),this._describedByChanges?.unsubscribe(),this._destroyed.next(),this._destroyed.complete()}getLabelId=ne(()=>this._hasFloatingLabel()?this._labelId:null);getConnectedOverlayOrigin(){return this._textField||this._elementRef}_animateAndLockLabel(){this._hasFloatingLabel()&&(this.floatLabel=`always`)}_initializeControl(e){let i=this._control,a=`mat-mdc-form-field-type-`;e&&this._elementRef.nativeElement.classList.remove(a+e.controlType),i.controlType&&this._elementRef.nativeElement.classList.add(a+i.controlType),this._stateChanges?.unsubscribe(),this._stateChanges=i.stateChanges?.subscribe(()=>{this._updateFocusState(),this._changeDetectorRef.markForCheck()}),this._describedByChanges?.unsubscribe(),this._describedByChanges=i.stateChanges?.pipe(mt([void 0,void 0]),st(()=>[this._unwrapMaybeSignal(i.errorState),i.userAriaDescribedBy]),Pl(),Qe(([[o,r],[s,c]])=>o!==s||r!==c)).subscribe(()=>this._syncDescribedByIds()),this._valueChanges?.unsubscribe(),i.ngControl&&i.ngControl.valueChanges&&!Fc(i.ngField)&&(this._valueChanges=i.ngControl.valueChanges.pipe(Te(this._destroyed)).subscribe(()=>this._changeDetectorRef.markForCheck()))}_checkPrefixAndSuffixTypes(){this._hasIconPrefix=!!this._prefixChildren.find(e=>!e._isText),this._hasTextPrefix=!!this._prefixChildren.find(e=>e._isText),this._hasIconSuffix=!!this._suffixChildren.find(e=>!e._isText),this._hasTextSuffix=!!this._suffixChildren.find(e=>e._isText)}_initializePrefixAndSuffix(){this._checkPrefixAndSuffixTypes(),It(this._prefixChildren.changes,this._suffixChildren.changes).subscribe(()=>{this._checkPrefixAndSuffixTypes(),this._changeDetectorRef.markForCheck()})}_initializeSubscript(){this._hintChildren.changes.subscribe(()=>{this._processHints(),this._changeDetectorRef.markForCheck()}),this._errorChildren.changes.subscribe(()=>{this._syncDescribedByIds(),this._changeDetectorRef.markForCheck()}),this._validateHints(),this._syncDescribedByIds()}_assertFormFieldControl(){this._control}_updateFocusState(){let e=this._unwrapMaybeSignal(this._control.focused);e&&!this._isFocused?(this._isFocused=!0,this._lineRipple?.activate()):!e&&(this._isFocused||this._isFocused===null)&&(this._isFocused=!1,this._lineRipple?.deactivate()),this._elementRef.nativeElement.classList.toggle(`mat-focused`,e),this._textField?.nativeElement.classList.toggle(`mdc-text-field--focused`,e)}_syncOutlineLabelOffset(){ig({earlyRead:()=>{if(this._appearanceSignal()!==`outline`)return this._outlineLabelOffsetResizeObserver?.disconnect(),null;if(globalThis.ResizeObserver){this._outlineLabelOffsetResizeObserver||=new globalThis.ResizeObserver(()=>{this._writeOutlinedLabelStyles(this._getOutlinedLabelOffset())});for(let e of this._prefixSuffixContainers())this._outlineLabelOffsetResizeObserver.observe(e,{box:`border-box`})}return this._getOutlinedLabelOffset()},write:e=>this._writeOutlinedLabelStyles(e())})}_shouldAlwaysFloat(){return this.floatLabel===`always`}_hasOutline(){return this.appearance===`outline`}_forceDisplayInfixLabel(){return!this._platform.isBrowser&&this._prefixChildren.length&&!this._shouldLabelFloat()}_hasFloatingLabel=ne(()=>!!this._labelChild());_shouldLabelFloat(){return this._hasFloatingLabel()?this._shouldAlwaysFloat()||this._unwrapMaybeSignal(this._control.shouldLabelFloat):!1}_shouldForward(e){let i=this._control?.ngField||this._control?.ngControl;if(!i)return!1;if(Fc(i)){let a=i();return e===`valid`?a.valid():e===`dirty`?a.dirty():e===`touched`?a.touched():e===`pending`?a.pending():e===`untouched`?!a.touched():e===`pristine`?!a.dirty():e===`invalid`?!a.valid():!1}else{let a=i;return e===`valid`?a.valid:e===`dirty`?a.dirty:e===`touched`?a.touched:e===`pending`?a.pending:e===`untouched`?a.untouched:e===`pristine`?a.pristine:e===`invalid`?a.invalid:!1}}_getSubscriptMessageType(){return this._errorChildren&&this._errorChildren.length>0&&this._unwrapMaybeSignal(this._control.errorState)?`error`:`hint`}_handleLabelResized(){this._refreshOutlineNotchWidth()}_refreshOutlineNotchWidth(){!this._hasOutline()||!this._floatingLabel||!this._shouldLabelFloat()?this._notchedOutline?._setNotchWidth(0):this._notchedOutline?._setNotchWidth(this._floatingLabel.getWidth())}_processHints(){this._validateHints(),this._syncDescribedByIds()}_validateHints(){this._hintChildren}_syncDescribedByIds(){if(this._control){let e=[];if(this._control.userAriaDescribedBy&&typeof this._control.userAriaDescribedBy==`string`&&e.push(...this._control.userAriaDescribedBy.split(` `)),this._getSubscriptMessageType()===`hint`){let o=this._hintChildren?this._hintChildren.find(s=>s.align===`start`):null,r=this._hintChildren?this._hintChildren.find(s=>s.align===`end`):null;o?e.push(o.id):this._hintLabel&&e.push(this._hintLabelId),r&&e.push(r.id)}else this._errorChildren&&e.push(...this._errorChildren.map(o=>o.id));let i=this._control.describedByIds,a;if(i){let o=this._describedByIds||e;a=e.concat(i.filter(r=>r&&!o.includes(r)))}else a=e;this._control.setDescribedByIds(a),this._describedByIds=e}}_getOutlinedLabelOffset(){if(!this._hasOutline()||!this._floatingLabel)return null;if(!this._iconPrefixContainer&&!this._textPrefixContainer)return[``,null];if(!this._isAttachedToDom())return null;let e=this._iconPrefixContainer?.nativeElement,i=this._textPrefixContainer?.nativeElement,a=this._iconSuffixContainer?.nativeElement,o=this._textSuffixContainer?.nativeElement,r=e?.getBoundingClientRect().width??0,s=i?.getBoundingClientRect().width??0,c=a?.getBoundingClientRect().width??0,h=o?.getBoundingClientRect().width??0;return[`var(--mat-mdc-form-field-label-transform, ${EO} translateX(${`calc(${this._currentDirection===`rtl`?`-1`:`1`} * (${`${r+s}px`} + var(--mat-mdc-form-field-label-offset-x, 0px)))`}))`,r+s+c+h]}_writeOutlinedLabelStyles(e){if(e!==null){let[i,a]=e;this._floatingLabel&&(this._floatingLabel.element.style.transform=i),a!==null&&this._notchedOutline?._setMaxWidth(a)}}_isAttachedToDom(){let e=this._elementRef.nativeElement;if(e.getRootNode){let i=e.getRootNode();return i&&i!==e}return document.documentElement.contains(e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`iconPrefixContainer`],i=[`textPrefixContainer`],a=[`iconSuffixContainer`],o=[`textSuffixContainer`],r=[`textField`],s=[`*`,[[`mat-label`]],[[``,`matPrefix`,``],[``,`matIconPrefix`,``]],[[``,`matTextPrefix`,``]],[[``,`matTextSuffix`,``]],[[``,`matSuffix`,``],[``,`matIconSuffix`,``]],[[`mat-error`],[``,`matError`,``]],[[`mat-hint`,3,`align`,`end`]],[[`mat-hint`,`align`,`end`]]],c=[`*`,`mat-label`,`[matPrefix], [matIconPrefix]`,`[matTextPrefix]`,`[matTextSuffix]`,`[matSuffix], [matIconSuffix]`,`mat-error, [matError]`,`mat-hint:not([align='end'])`,`mat-hint[align='end']`];function h(pe,Ie){pe&1&&H(0,`span`,21)}function p(pe,Ie){if(pe&1&&(l(0,`label`,20),he(1,1),D(2,h,1,0,`span`,21),d()),pe&2){let F=_(2);S(`floating`,F._shouldLabelFloat())(`monitorResize`,F._hasOutline())(`id`,F._labelId),te(`for`,F._control.disableAutomaticLabeling?null:F._control.id),m(2),I(!F.hideRequiredMarker&&F._unwrapMaybeSignal(F._control.required)?2:-1)}}function g(pe,Ie){if(pe&1&&D(0,p,3,5,`label`,20),pe&2)I(_()._hasFloatingLabel()?0:-1)}function v(pe,Ie){pe&1&&H(0,`div`,7)}function b(pe,Ie){}function y(pe,Ie){if(pe&1&&ot(0,b,0,0,`ng-template`,13),pe&2){_(2);S(`ngTemplateOutlet`,ft(1))}}function E(pe,Ie){if(pe&1&&(l(0,`div`,9),D(1,y,1,1,null,13),d()),pe&2){let F=_();S(`matFormFieldNotchedOutlineOpen`,F._shouldLabelFloat()),m(),I(F._forceDisplayInfixLabel()?-1:1)}}function P(pe,Ie){pe&1&&(l(0,`div`,10,2),he(2,2),d())}function ae(pe,Ie){pe&1&&(l(0,`div`,11,3),he(2,3),d())}function z(pe,Ie){}function x(pe,Ie){if(pe&1&&ot(0,z,0,0,`ng-template`,13),pe&2){_();S(`ngTemplateOutlet`,ft(1))}}function M(pe,Ie){pe&1&&(l(0,`div`,14,4),he(2,4),d())}function O(pe,Ie){pe&1&&(l(0,`div`,15,5),he(2,5),d())}function $(pe,Ie){pe&1&&H(0,`div`,16)}function _e(pe,Ie){pe&1&&(l(0,`div`,18),he(1,6),d())}function bt(pe,Ie){if(pe&1&&(l(0,`mat-hint`,22),u(1),d()),pe&2){let F=_(2);S(`id`,F._hintLabelId),m(),w(F.hintLabel)}}function We(pe,Ie){if(pe&1&&(l(0,`div`,19),D(1,bt,2,2,`mat-hint`,22),he(2,7),H(3,`div`,23),he(4,8),d()),pe&2){let F=_();m(),I(F.hintLabel?1:-1)}}return B({type:t,selectors:[[`mat-form-field`]],contentQueries:function(Ie,F,qe){if(Ie&1&&(qd(qe,F._labelChild,na,5),jt(qe,Lc,5)(qe,yO,5)(qe,xO,5)(qe,_O,5)(qe,ob,5)),Ie&2){Kd();let nn;Z(nn=J())&&(F._formFieldControl=nn.first),Z(nn=J())&&(F._prefixChildren=nn),Z(nn=J())&&(F._suffixChildren=nn),Z(nn=J())&&(F._errorChildren=nn),Z(nn=J())&&(F._hintChildren=nn)}},viewQuery:function(Ie,F){if(Ie&1&&(Yd(F._iconPrefixContainerSignal,e,5)(F._textPrefixContainerSignal,i,5)(F._iconSuffixContainerSignal,a,5)(F._textSuffixContainerSignal,o,5),Ze(r,5)(e,5)(i,5)(a,5)(o,5)(jS,5)(US,5)(HS,5)),Ie&2){Kd(4);let qe;Z(qe=J())&&(F._textField=qe.first),Z(qe=J())&&(F._iconPrefixContainer=qe.first),Z(qe=J())&&(F._textPrefixContainer=qe.first),Z(qe=J())&&(F._iconSuffixContainer=qe.first),Z(qe=J())&&(F._textSuffixContainer=qe.first),Z(qe=J())&&(F._floatingLabel=qe.first),Z(qe=J())&&(F._notchedOutline=qe.first),Z(qe=J())&&(F._lineRipple=qe.first)}},hostAttrs:[1,`mat-mdc-form-field`],hostVars:38,hostBindings:function(Ie,F){Ie&2&&Y(`mat-mdc-form-field-label-always-float`,F._shouldAlwaysFloat())(`mat-mdc-form-field-has-icon-prefix`,F._hasIconPrefix)(`mat-mdc-form-field-has-icon-suffix`,F._hasIconSuffix)(`mat-form-field-invalid`,F._unwrapMaybeSignal(F._control.errorState))(`mat-form-field-disabled`,F._unwrapMaybeSignal(F._control.disabled))(`mat-form-field-autofilled`,F._unwrapMaybeSignal(F._control.autofilled))(`mat-form-field-appearance-fill`,F.appearance==`fill`)(`mat-form-field-appearance-outline`,F.appearance==`outline`)(`mat-form-field-hide-placeholder`,F._hasFloatingLabel()&&!F._shouldLabelFloat())(`mat-primary`,F.color!==`accent`&&F.color!==`warn`)(`mat-accent`,F.color===`accent`)(`mat-warn`,F.color===`warn`)(`ng-untouched`,F._shouldForward(`untouched`))(`ng-touched`,F._shouldForward(`touched`))(`ng-pristine`,F._shouldForward(`pristine`))(`ng-dirty`,F._shouldForward(`dirty`))(`ng-valid`,F._shouldForward(`valid`))(`ng-invalid`,F._shouldForward(`invalid`))(`ng-pending`,F._shouldForward(`pending`))},inputs:{hideRequiredMarker:`hideRequiredMarker`,color:`color`,floatLabel:`floatLabel`,appearance:`appearance`,subscriptSizing:`subscriptSizing`,hintLabel:`hintLabel`},exportAs:[`matFormField`],features:[Ge([{provide:Vc,useExisting:t},{provide:WS,useExisting:t}])],ngContentSelectors:c,decls:18,vars:21,consts:[[`labelTemplate`,``],[`textField`,``],[`iconPrefixContainer`,``],[`textPrefixContainer`,``],[`textSuffixContainer`,``],[`iconSuffixContainer`,``],[1,`mat-mdc-text-field-wrapper`,`mdc-text-field`,3,`click`],[1,`mat-mdc-form-field-focus-overlay`],[1,`mat-mdc-form-field-flex`],[`matFormFieldNotchedOutline`,``,3,`matFormFieldNotchedOutlineOpen`],[1,`mat-mdc-form-field-icon-prefix`],[1,`mat-mdc-form-field-text-prefix`],[1,`mat-mdc-form-field-infix`],[3,`ngTemplateOutlet`],[1,`mat-mdc-form-field-text-suffix`],[1,`mat-mdc-form-field-icon-suffix`],[`matFormFieldLineRipple`,``],[`aria-atomic`,`true`,`aria-live`,`polite`,1,`mat-mdc-form-field-subscript-wrapper`,`mat-mdc-form-field-bottom-align`],[1,`mat-mdc-form-field-error-wrapper`],[1,`mat-mdc-form-field-hint-wrapper`],[`matFormFieldFloatingLabel`,``,3,`floating`,`monitorResize`,`id`],[`aria-hidden`,`true`,1,`mat-mdc-form-field-required-marker`,`mdc-floating-label--required`],[3,`id`],[1,`mat-mdc-form-field-hint-spacer`]],template:function(Ie,F){if(Ie&1&&(Pe(s),ot(0,g,1,1,`ng-template`,null,0,rc),l(2,`div`,6,1),C(`click`,function(nn){return F._control.onContainerClick(nn)}),D(4,v,1,0,`div`,7),l(5,`div`,8),D(6,E,2,2,`div`,9),D(7,P,3,0,`div`,10),D(8,ae,3,0,`div`,11),l(9,`div`,12),D(10,x,1,1,null,13),he(11),d(),D(12,M,3,0,`div`,14),D(13,O,3,0,`div`,15),d(),D(14,$,1,0,`div`,16),d(),l(15,`div`,17),D(16,_e,2,0,`div`,18)(17,We,5,1,`div`,19),d()),Ie&2){let qe,nn=F._unwrapMaybeSignal(F._control.disabled);m(2),Y(`mdc-text-field--filled`,!F._hasOutline())(`mdc-text-field--outlined`,F._hasOutline())(`mdc-text-field--no-label`,!F._hasFloatingLabel())(`mdc-text-field--disabled`,nn)(`mdc-text-field--invalid`,F._unwrapMaybeSignal(F._control.errorState)),m(2),I(!F._hasOutline()&&!nn?4:-1),m(2),I(F._hasOutline()?6:-1),m(),I(F._hasIconPrefix?7:-1),m(),I(F._hasTextPrefix?8:-1),m(2),I(!F._hasOutline()||F._forceDisplayInfixLabel()?10:-1),m(2),I(F._hasTextSuffix?12:-1),m(),I(F._hasIconSuffix?13:-1),m(),I(F._hasOutline()?-1:14),m(),Y(`mat-mdc-form-field-subscript-dynamic-size`,F.subscriptSizing===`dynamic`);let cE=F._getSubscriptMessageType();m(),I((qe=cE)===`error`?16:qe===`hint`?17:-1)}},dependencies:[jS,US,fc,HS,ob],styles:[`.mdc-text-field {
  display: inline-flex;
  align-items: baseline;
  padding: 0 16px;
  position: relative;
  box-sizing: border-box;
  overflow: hidden;
  will-change: opacity, transform, color;
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}

.mdc-text-field__input {
  width: 100%;
  min-width: 0;
  border: none;
  border-radius: 0;
  background: none;
  padding: 0;
  -moz-appearance: none;
  -webkit-appearance: none;
  height: 28px;
}
.mdc-text-field__input::-webkit-calendar-picker-indicator, .mdc-text-field__input::-webkit-search-cancel-button {
  display: none;
}
.mdc-text-field__input::-ms-clear {
  display: none;
}
.mdc-text-field__input:focus {
  outline: none;
}
.mdc-text-field__input:invalid {
  box-shadow: none;
}
.mdc-text-field__input::placeholder {
  opacity: 0;
}
.mdc-text-field__input::-moz-placeholder {
  opacity: 0;
}
.mdc-text-field__input::-webkit-input-placeholder {
  opacity: 0;
}
.mdc-text-field__input:-ms-input-placeholder {
  opacity: 0;
}
.mdc-text-field--no-label .mdc-text-field__input::placeholder, .mdc-text-field--focused .mdc-text-field__input::placeholder {
  opacity: 1;
}
.mdc-text-field--no-label .mdc-text-field__input::-moz-placeholder, .mdc-text-field--focused .mdc-text-field__input::-moz-placeholder {
  opacity: 1;
}
.mdc-text-field--no-label .mdc-text-field__input::-webkit-input-placeholder, .mdc-text-field--focused .mdc-text-field__input::-webkit-input-placeholder {
  opacity: 1;
}
.mdc-text-field--no-label .mdc-text-field__input:-ms-input-placeholder, .mdc-text-field--focused .mdc-text-field__input:-ms-input-placeholder {
  opacity: 1;
}
.mdc-text-field--%NS%disabled:not(.mdc-text-field--no-label) .mdc-text-field__input.mat-mdc-input-disabled-interactive::placeholder {
  opacity: 0;
}
.mdc-text-field--%NS%disabled:not(.mdc-text-field--no-label) .mdc-text-field__input.mat-mdc-input-disabled-interactive::-moz-placeholder {
  opacity: 0;
}
.mdc-text-field--%NS%disabled:not(.mdc-text-field--no-label) .mdc-text-field__input.mat-mdc-input-disabled-interactive::-webkit-input-placeholder {
  opacity: 0;
}
.mdc-text-field--%NS%disabled:not(.mdc-text-field--no-label) .mdc-text-field__input.mat-mdc-input-disabled-interactive:-ms-input-placeholder {
  opacity: 0;
}
.mdc-text-field--outlined .mdc-text-field__input, .mdc-text-field--filled.mdc-text-field--no-label .mdc-text-field__input {
  height: 100%;
}
.mdc-text-field--outlined .mdc-text-field__input {
  display: flex;
  border: none !important;
  background-color: transparent;
}
.mdc-text-field--disabled .mdc-text-field__input {
  pointer-events: auto;
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled) .mdc-text-field__input {
  color: var(--%NS%mat-form-field-filled-input-text-color, var(--%NS%mat-sys-on-surface));
  caret-color: var(--%NS%mat-form-field-filled-caret-color, var(--%NS%mat-sys-primary));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled) .mdc-text-field__input::placeholder {
  color: var(--%NS%mat-form-field-filled-input-text-placeholder-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled) .mdc-text-field__input::-moz-placeholder {
  color: var(--%NS%mat-form-field-filled-input-text-placeholder-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled) .mdc-text-field__input::-webkit-input-placeholder {
  color: var(--%NS%mat-form-field-filled-input-text-placeholder-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled) .mdc-text-field__input:-ms-input-placeholder {
  color: var(--%NS%mat-form-field-filled-input-text-placeholder-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled) .mdc-text-field__input {
  color: var(--%NS%mat-form-field-outlined-input-text-color, var(--%NS%mat-sys-on-surface));
  caret-color: var(--%NS%mat-form-field-outlined-caret-color, var(--%NS%mat-sys-primary));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled) .mdc-text-field__input::placeholder {
  color: var(--%NS%mat-form-field-outlined-input-text-placeholder-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled) .mdc-text-field__input::-moz-placeholder {
  color: var(--%NS%mat-form-field-outlined-input-text-placeholder-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled) .mdc-text-field__input::-webkit-input-placeholder {
  color: var(--%NS%mat-form-field-outlined-input-text-placeholder-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled) .mdc-text-field__input:-ms-input-placeholder {
  color: var(--%NS%mat-form-field-outlined-input-text-placeholder-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--filled.mdc-text-field--%NS%invalid:not(.mdc-text-field--disabled) .mdc-text-field__input {
  caret-color: var(--%NS%mat-form-field-filled-error-caret-color, var(--%NS%mat-sys-error));
}
.mdc-text-field--outlined.mdc-text-field--%NS%invalid:not(.mdc-text-field--disabled) .mdc-text-field__input {
  caret-color: var(--%NS%mat-form-field-outlined-error-caret-color, var(--%NS%mat-sys-error));
}
.mdc-text-field--filled.mdc-text-field--disabled .mdc-text-field__input {
  color: var(--%NS%mat-form-field-filled-disabled-input-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mdc-text-field--outlined.mdc-text-field--disabled .mdc-text-field__input {
  color: var(--%NS%mat-form-field-outlined-disabled-input-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
@media (forced-colors: active) {
  .mdc-text-field--disabled .mdc-text-field__input {
    background-color: Window;
  }
}

.mdc-text-field--filled {
  height: 56px;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
  border-top-left-radius: var(--%NS%mat-form-field-filled-container-shape, var(--%NS%mat-sys-corner-extra-small));
  border-top-right-radius: var(--%NS%mat-form-field-filled-container-shape, var(--%NS%mat-sys-corner-extra-small));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled) {
  background-color: var(--%NS%mat-form-field-filled-container-color, var(--%NS%mat-sys-surface-variant));
}
.mdc-text-field--filled.mdc-text-field--disabled {
  background-color: var(--%NS%mat-form-field-filled-disabled-container-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 4%, transparent));
}

.mdc-text-field--outlined {
  height: 56px;
  overflow: visible;
  padding-right: max(16px, var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small)));
  padding-left: max(16px, var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small)) + 4px);
}
[dir=rtl] .mdc-text-field--outlined {
  padding-right: max(16px, var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small)) + 4px);
  padding-left: max(16px, var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small)));
}

.mdc-floating-label {
  position: absolute;
  left: 0;
  transform-origin: left top;
  line-height: 1.15rem;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: text;
  overflow: hidden;
  will-change: transform;
}
[dir=rtl] .mdc-floating-label {
  right: 0;
  left: auto;
  transform-origin: right top;
  text-align: right;
}
.mdc-text-field .mdc-floating-label {
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}
.mdc-notched-outline .mdc-floating-label {
  display: inline-block;
  position: relative;
  max-width: 100%;
}
.mdc-text-field--outlined .mdc-floating-label {
  left: 4px;
  right: auto;
}
[dir=rtl] .mdc-text-field--outlined .mdc-floating-label {
  left: auto;
  right: 4px;
}
.mdc-text-field--filled .mdc-floating-label {
  left: 16px;
  right: auto;
}
[dir=rtl] .mdc-text-field--filled .mdc-floating-label {
  left: auto;
  right: 16px;
}
.mdc-text-field--disabled .mdc-floating-label {
  cursor: default;
}
@media (forced-colors: active) {
  .mdc-text-field--disabled .mdc-floating-label {
    z-index: 1;
  }
}
.mdc-text-field--filled.mdc-text-field--no-label .mdc-floating-label {
  display: none;
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled) .mdc-floating-label {
  color: var(--%NS%mat-form-field-filled-label-text-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled).mdc-text-field--focused .mdc-floating-label {
  color: var(--%NS%mat-form-field-filled-focus-label-text-color, var(--%NS%mat-sys-primary));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled):not(.mdc-text-field--focused):hover .mdc-floating-label {
  color: var(--%NS%mat-form-field-filled-hover-label-text-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--filled.mdc-text-field--disabled .mdc-floating-label {
  color: var(--%NS%mat-form-field-filled-disabled-label-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled).mdc-text-field--invalid .mdc-floating-label {
  color: var(--%NS%mat-form-field-filled-error-label-text-color, var(--%NS%mat-sys-error));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled).mdc-text-field--invalid.mdc-text-field--focused .mdc-floating-label {
  color: var(--%NS%mat-form-field-filled-error-focus-label-text-color, var(--%NS%mat-sys-error));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled).mdc-text-field--%NS%invalid:not(.mdc-text-field--disabled):hover .mdc-floating-label {
  color: var(--%NS%mat-form-field-filled-error-hover-label-text-color, var(--%NS%mat-sys-on-error-container));
}
.mdc-text-field--filled .mdc-floating-label {
  font-family: var(--%NS%mat-form-field-filled-label-text-font, var(--%NS%mat-sys-body-large-font));
  font-size: var(--%NS%mat-form-field-filled-label-text-size, var(--%NS%mat-sys-body-large-size));
  font-weight: var(--%NS%mat-form-field-filled-label-text-weight, var(--%NS%mat-sys-body-large-weight));
  letter-spacing: var(--%NS%mat-form-field-filled-label-text-tracking, var(--%NS%mat-sys-body-large-tracking));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled) .mdc-floating-label {
  color: var(--%NS%mat-form-field-outlined-label-text-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled).mdc-text-field--focused .mdc-floating-label {
  color: var(--%NS%mat-form-field-outlined-focus-label-text-color, var(--%NS%mat-sys-primary));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled):not(.mdc-text-field--focused):hover .mdc-floating-label {
  color: var(--%NS%mat-form-field-outlined-hover-label-text-color, var(--%NS%mat-sys-on-surface));
}
.mdc-text-field--outlined.mdc-text-field--disabled .mdc-floating-label {
  color: var(--%NS%mat-form-field-outlined-disabled-label-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled).mdc-text-field--invalid .mdc-floating-label {
  color: var(--%NS%mat-form-field-outlined-error-label-text-color, var(--%NS%mat-sys-error));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled).mdc-text-field--invalid.mdc-text-field--focused .mdc-floating-label {
  color: var(--%NS%mat-form-field-outlined-error-focus-label-text-color, var(--%NS%mat-sys-error));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled).mdc-text-field--%NS%invalid:not(.mdc-text-field--disabled):hover .mdc-floating-label {
  color: var(--%NS%mat-form-field-outlined-error-hover-label-text-color, var(--%NS%mat-sys-on-error-container));
}
.mdc-text-field--outlined .mdc-floating-label {
  font-family: var(--%NS%mat-form-field-outlined-label-text-font, var(--%NS%mat-sys-body-large-font));
  font-size: var(--%NS%mat-form-field-outlined-label-text-size, var(--%NS%mat-sys-body-large-size));
  font-weight: var(--%NS%mat-form-field-outlined-label-text-weight, var(--%NS%mat-sys-body-large-weight));
  letter-spacing: var(--%NS%mat-form-field-outlined-label-text-tracking, var(--%NS%mat-sys-body-large-tracking));
}

.mdc-floating-label--float-above {
  cursor: auto;
  transform: translateY(-106%) scale(0.75);
}
.mdc-text-field--filled .mdc-floating-label--float-above {
  transform: translateY(-106%) scale(0.75);
}
.mdc-text-field--outlined .mdc-floating-label--float-above {
  transform: translateY(-37.25px) scale(1);
  font-size: 0.75rem;
}
.mdc-notched-outline .mdc-floating-label--float-above {
  text-overflow: clip;
}
.mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  max-width: 133.3333333333%;
}
.mdc-text-field--outlined.mdc-notched-outline--upgraded .mdc-floating-label--float-above, .mdc-text-field--outlined .mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  transform: translateY(-34.75px) scale(0.75);
}
.mdc-text-field--outlined.mdc-notched-outline--upgraded .mdc-floating-label--float-above, .mdc-text-field--outlined .mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  font-size: 1rem;
}

.mdc-floating-label--%NS%required:not(.mdc-floating-label--hide-required-marker)::after {
  margin-left: 1px;
  margin-right: 0;
  content: "*";
}
[dir=rtl] .mdc-floating-label--%NS%required:not(.mdc-floating-label--hide-required-marker)::after {
  margin-left: 0;
  margin-right: 1px;
}

.mdc-notched-outline {
  display: flex;
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  height: 100%;
  text-align: left;
  pointer-events: none;
}
[dir=rtl] .mdc-notched-outline {
  text-align: right;
}
.mdc-text-field--outlined .mdc-notched-outline {
  z-index: 1;
}

.mat-mdc-notch-piece {
  box-sizing: border-box;
  height: 100%;
  pointer-events: none;
  border: none;
  border-top: 1px solid;
  border-bottom: 1px solid;
}
.mdc-text-field--focused .mat-mdc-notch-piece {
  border-width: 2px;
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled) .mat-mdc-notch-piece {
  border-color: var(--%NS%mat-form-field-outlined-outline-color, var(--%NS%mat-sys-outline));
  border-width: var(--%NS%mat-form-field-outlined-outline-width, 1px);
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled):not(.mdc-text-field--focused):hover .mat-mdc-notch-piece {
  border-color: var(--%NS%mat-form-field-outlined-hover-outline-color, var(--%NS%mat-sys-on-surface));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled).mdc-text-field--focused .mat-mdc-notch-piece {
  border-color: var(--%NS%mat-form-field-outlined-focus-outline-color, var(--%NS%mat-sys-primary));
}
.mdc-text-field--outlined.mdc-text-field--disabled .mat-mdc-notch-piece {
  border-color: var(--%NS%mat-form-field-outlined-disabled-outline-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 12%, transparent));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled).mdc-text-field--invalid .mat-mdc-notch-piece {
  border-color: var(--%NS%mat-form-field-outlined-error-outline-color, var(--%NS%mat-sys-error));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled).mdc-text-field--%NS%invalid:not(.mdc-text-field--focused):hover .mdc-notched-outline .mat-mdc-notch-piece {
  border-color: var(--%NS%mat-form-field-outlined-error-hover-outline-color, var(--%NS%mat-sys-on-error-container));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled).mdc-text-field--invalid.mdc-text-field--focused .mat-mdc-notch-piece {
  border-color: var(--%NS%mat-form-field-outlined-error-focus-outline-color, var(--%NS%mat-sys-error));
}
.mdc-text-field--%NS%outlined:not(.mdc-text-field--disabled).mdc-text-field--focused .mdc-notched-outline .mat-mdc-notch-piece {
  border-width: var(--%NS%mat-form-field-outlined-focus-outline-width, 2px);
}

.mdc-notched-outline__leading {
  border-left: 1px solid;
  border-right: none;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  border-top-left-radius: var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small));
  border-bottom-left-radius: var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small));
}
.mdc-text-field--outlined .mdc-notched-outline .mdc-notched-outline__leading {
  width: max(12px, var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small)));
}
[dir=rtl] .mdc-notched-outline__leading {
  border-left: none;
  border-right: 1px solid;
  border-bottom-left-radius: 0;
  border-top-left-radius: 0;
  border-top-right-radius: var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small));
  border-bottom-right-radius: var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small));
}

.mdc-notched-outline__trailing {
  flex-grow: 1;
  border-left: none;
  border-right: 1px solid;
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  border-top-right-radius: var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small));
  border-bottom-right-radius: var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small));
}
[dir=rtl] .mdc-notched-outline__trailing {
  border-left: 1px solid;
  border-right: none;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  border-top-left-radius: var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small));
  border-bottom-left-radius: var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small));
}

.mdc-notched-outline__notch {
  flex: 0 0 auto;
  width: auto;
}
.mdc-text-field--outlined .mdc-notched-outline .mdc-notched-outline__notch {
  max-width: min(var(--%NS%mat-form-field-notch-max-width, 100%), calc(100% - max(12px, var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small))) * 2));
}
.mdc-text-field--outlined .mdc-notched-outline--notched .mdc-notched-outline__notch {
  max-width: min(100%, calc(100% - max(12px, var(--%NS%mat-form-field-outlined-container-shape, var(--%NS%mat-sys-corner-extra-small))) * 2));
}
.mdc-text-field--outlined .mdc-notched-outline--notched .mdc-notched-outline__notch {
  padding-top: 1px;
}
.mdc-text-field--focused.mdc-text-field--outlined .mdc-notched-outline--notched .mdc-notched-outline__notch {
  padding-top: 2px;
}
.mdc-notched-outline--notched .mdc-notched-outline__notch {
  padding-left: 0;
  padding-right: 8px;
  border-top: none;
}
[dir=rtl] .mdc-notched-outline--notched .mdc-notched-outline__notch {
  padding-left: 8px;
  padding-right: 0;
}
.mdc-notched-outline--no-label .mdc-notched-outline__notch {
  display: none;
}

.mdc-line-ripple::before, .mdc-line-ripple::after {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  border-bottom-style: solid;
  content: "";
}
.mdc-line-ripple::before {
  z-index: 1;
  border-bottom-width: var(--%NS%mat-form-field-filled-active-indicator-height, 1px);
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled) .mdc-line-ripple::before {
  border-bottom-color: var(--%NS%mat-form-field-filled-active-indicator-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled):not(.mdc-text-field--focused):hover .mdc-line-ripple::before {
  border-bottom-color: var(--%NS%mat-form-field-filled-hover-active-indicator-color, var(--%NS%mat-sys-on-surface));
}
.mdc-text-field--filled.mdc-text-field--disabled .mdc-line-ripple::before {
  border-bottom-color: var(--%NS%mat-form-field-filled-disabled-active-indicator-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled).mdc-text-field--invalid .mdc-line-ripple::before {
  border-bottom-color: var(--%NS%mat-form-field-filled-error-active-indicator-color, var(--%NS%mat-sys-error));
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled).mdc-text-field--%NS%invalid:not(.mdc-text-field--focused):hover .mdc-line-ripple::before {
  border-bottom-color: var(--%NS%mat-form-field-filled-error-hover-active-indicator-color, var(--%NS%mat-sys-on-error-container));
}
.mdc-line-ripple::after {
  transform: scaleX(0);
  opacity: 0;
  z-index: 2;
}
.mdc-text-field--filled .mdc-line-ripple::after {
  border-bottom-width: var(--%NS%mat-form-field-filled-focus-active-indicator-height, 2px);
}
.mdc-text-field--%NS%filled:not(.mdc-text-field--disabled) .mdc-line-ripple::after {
  border-bottom-color: var(--%NS%mat-form-field-filled-focus-active-indicator-color, var(--%NS%mat-sys-primary));
}
.mdc-text-field--filled.mdc-text-field--%NS%invalid:not(.mdc-text-field--disabled) .mdc-line-ripple::after {
  border-bottom-color: var(--%NS%mat-form-field-filled-error-focus-active-indicator-color, var(--%NS%mat-sys-error));
}

.mdc-line-ripple--%NS%active::after {
  transform: scaleX(1);
  opacity: 1;
}

.mdc-line-ripple--%NS%deactivating::after {
  opacity: 0;
}

.mdc-text-field--disabled {
  pointer-events: none;
}

.mat-mdc-form-field-textarea-control {
  vertical-align: middle;
  resize: vertical;
  box-sizing: border-box;
  height: auto;
  margin: 0;
  padding: 0;
  border: none;
  overflow: auto;
}

.mat-mdc-form-field-input-control.mat-mdc-form-field-input-control {
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  font: inherit;
  letter-spacing: inherit;
  text-decoration: inherit;
  text-transform: inherit;
  border: none;
}

.mat-mdc-form-field .mat-mdc-floating-label.mdc-floating-label {
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  line-height: normal;
  pointer-events: all;
  will-change: auto;
}

.mat-mdc-form-field:not(.mat-form-field-disabled) .mat-mdc-floating-label.mdc-floating-label {
  cursor: inherit;
}

.mdc-text-field--%NS%no-label:not(.mdc-text-field--textarea) .mat-mdc-form-field-input-control.mdc-text-field__input,
.mat-mdc-text-field-wrapper .mat-mdc-form-field-input-control {
  height: auto;
}

.mat-mdc-text-field-wrapper .mat-mdc-form-field-input-control.mdc-text-field__input[type=color] {
  height: 23px;
}

.mat-mdc-text-field-wrapper {
  height: auto;
  flex: auto;
  will-change: auto;
}

.mat-mdc-form-field-has-icon-prefix .mat-mdc-text-field-wrapper {
  padding-left: 0;
  --%NS%mat-mdc-form-field-label-offset-x: -16px;
}

.mat-mdc-form-field-has-icon-suffix .mat-mdc-text-field-wrapper {
  padding-right: 0;
}

[dir=rtl] .mat-mdc-text-field-wrapper {
  padding-left: 16px;
  padding-right: 16px;
}
[dir=rtl] .mat-mdc-form-field-has-icon-suffix .mat-mdc-text-field-wrapper {
  padding-left: 0;
}
[dir=rtl] .mat-mdc-form-field-has-icon-prefix .mat-mdc-text-field-wrapper {
  padding-right: 0;
}

.mat-form-field-disabled .mdc-text-field__input::placeholder {
  color: var(--%NS%mat-form-field-disabled-input-text-placeholder-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-form-field-disabled .mdc-text-field__input::-moz-placeholder {
  color: var(--%NS%mat-form-field-disabled-input-text-placeholder-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-form-field-disabled .mdc-text-field__input::-webkit-input-placeholder {
  color: var(--%NS%mat-form-field-disabled-input-text-placeholder-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-form-field-disabled .mdc-text-field__input:-ms-input-placeholder {
  color: var(--%NS%mat-form-field-disabled-input-text-placeholder-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}

.mat-mdc-form-field-label-always-float .mdc-text-field__input::placeholder {
  transition-delay: 40ms;
  transition-duration: 110ms;
  opacity: 1;
}

.mat-mdc-text-field-wrapper .mat-mdc-form-field-infix .mat-mdc-floating-label {
  left: auto;
  right: auto;
}

.mat-mdc-text-field-wrapper.mdc-text-field--outlined .mdc-text-field__input {
  display: inline-block;
}

.mat-mdc-form-field .mat-mdc-text-field-wrapper.mdc-text-field .mdc-notched-outline__notch {
  padding-top: 0;
}

.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field .mdc-notched-outline__notch {
  border-left: 1px solid transparent;
}

[dir=rtl] .mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field .mdc-notched-outline__notch {
  border-left: none;
  border-right: 1px solid transparent;
}

.mat-mdc-form-field-infix {
  min-height: var(--%NS%mat-form-field-container-height, 56px);
  padding-top: var(--%NS%mat-form-field-filled-with-label-container-padding-top, 24px);
  padding-bottom: var(--%NS%mat-form-field-filled-with-label-container-padding-bottom, 8px);
}
.mdc-text-field--outlined .mat-mdc-form-field-infix, .mdc-text-field--no-label .mat-mdc-form-field-infix {
  padding-top: var(--%NS%mat-form-field-container-vertical-padding, 16px);
  padding-bottom: var(--%NS%mat-form-field-container-vertical-padding, 16px);
}

.mat-mdc-text-field-wrapper .mat-mdc-form-field-flex .mat-mdc-floating-label {
  top: calc(var(--%NS%mat-form-field-container-height, 56px) / 2);
}

.mdc-text-field--filled .mat-mdc-floating-label {
  display: var(--%NS%mat-form-field-filled-label-display, block);
}

.mat-mdc-text-field-wrapper.mdc-text-field--outlined .mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  --%NS%mat-mdc-form-field-label-transform: translateY(calc(calc(6.75px + var(--%NS%mat-form-field-container-height, 56px) / 2) * -1))
    scale(var(--%NS%mat-mdc-form-field-floating-label-scale, 0.75));
  transform: var(--%NS%mat-mdc-form-field-label-transform);
}

@keyframes _mat-form-field-subscript-animation {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.mat-mdc-form-field-subscript-wrapper {
  box-sizing: border-box;
  width: 100%;
  position: relative;
}

.mat-mdc-form-field-hint-wrapper,
.mat-mdc-form-field-error-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 0 16px;
  opacity: 1;
  transform: translateY(0);
  animation: _mat-form-field-subscript-animation 0ms cubic-bezier(0.55, 0, 0.55, 0.2);
}

.mat-mdc-form-field-subscript-dynamic-size .mat-mdc-form-field-hint-wrapper,
.mat-mdc-form-field-subscript-dynamic-size .mat-mdc-form-field-error-wrapper {
  position: static;
}

.mat-mdc-form-field-bottom-align::before {
  content: "";
  display: inline-block;
  height: 16px;
}

.mat-mdc-form-field-bottom-align.mat-mdc-form-field-subscript-dynamic-size::before {
  content: unset;
}

.mat-mdc-form-field-hint-end {
  order: 1;
}

.mat-mdc-form-field-hint-wrapper {
  display: flex;
}

.mat-mdc-form-field-hint-spacer {
  flex: 1 0 1em;
}

.mat-mdc-form-field-error {
  display: block;
  color: var(--%NS%mat-form-field-error-text-color, var(--%NS%mat-sys-error));
}

.mat-mdc-form-field-subscript-wrapper,
.mat-mdc-form-field-bottom-align::before {
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  font-family: var(--%NS%mat-form-field-subscript-text-font, var(--%NS%mat-sys-body-small-font));
  line-height: var(--%NS%mat-form-field-subscript-text-line-height, var(--%NS%mat-sys-body-small-line-height));
  font-size: var(--%NS%mat-form-field-subscript-text-size, var(--%NS%mat-sys-body-small-size));
  letter-spacing: var(--%NS%mat-form-field-subscript-text-tracking, var(--%NS%mat-sys-body-small-tracking));
  font-weight: var(--%NS%mat-form-field-subscript-text-weight, var(--%NS%mat-sys-body-small-weight));
}

.mat-mdc-form-field-focus-overlay {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  opacity: 0;
  pointer-events: none;
  background-color: var(--%NS%mat-form-field-state-layer-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-text-field-wrapper:hover .mat-mdc-form-field-focus-overlay {
  opacity: var(--%NS%mat-form-field-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
}
.mat-mdc-form-field.mat-focused .mat-mdc-form-field-focus-overlay {
  opacity: var(--%NS%mat-form-field-focus-state-layer-opacity, 0);
}

select.mat-mdc-form-field-input-control {
  -moz-appearance: none;
  -webkit-appearance: none;
  background-color: transparent;
  display: inline-flex;
  box-sizing: border-box;
}
select.mat-mdc-form-field-input-control:not(:disabled) {
  cursor: pointer;
}
select.mat-mdc-form-field-input-control:not(.mat-mdc-native-select-inline) option {
  color: var(--%NS%mat-form-field-select-option-text-color, var(--%NS%mat-sys-neutral10));
}
select.mat-mdc-form-field-input-control:not(.mat-mdc-native-select-inline) option:disabled {
  color: var(--%NS%mat-form-field-select-disabled-option-text-color, color-mix(in srgb, var(--%NS%mat-sys-neutral10) 38%, transparent));
}

.mat-mdc-form-field-type-mat-native-select .mat-mdc-form-field-infix::after {
  content: "";
  width: 0;
  height: 0;
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 5px solid;
  position: absolute;
  right: 0;
  top: 50%;
  margin-top: -2.5px;
  pointer-events: none;
  color: var(--%NS%mat-form-field-enabled-select-arrow-color, var(--%NS%mat-sys-on-surface-variant));
}
[dir=rtl] .mat-mdc-form-field-type-mat-native-select .mat-mdc-form-field-infix::after {
  right: auto;
  left: 0;
}
.mat-mdc-form-field-type-mat-native-select.mat-focused .mat-mdc-form-field-infix::after {
  color: var(--%NS%mat-form-field-focus-select-arrow-color, var(--%NS%mat-sys-primary));
}
.mat-mdc-form-field-type-mat-native-select.mat-form-field-disabled .mat-mdc-form-field-infix::after {
  color: var(--%NS%mat-form-field-disabled-select-arrow-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-mdc-form-field-type-mat-native-select .mat-mdc-form-field-input-control {
  padding-right: 15px;
}
[dir=rtl] .mat-mdc-form-field-type-mat-native-select .mat-mdc-form-field-input-control {
  padding-right: 0;
  padding-left: 15px;
}

@media (forced-colors: active) {
  .mat-form-field-appearance-fill .mat-mdc-text-field-wrapper {
    outline: solid 1px;
  }
}
@media (forced-colors: active) {
  .mat-form-field-appearance-fill.mat-form-field-disabled .mat-mdc-text-field-wrapper {
    outline-color: GrayText;
  }
}

@media (forced-colors: active) {
  .mat-form-field-appearance-fill.mat-focused .mat-mdc-text-field-wrapper {
    outline: dashed 3px;
  }
}

@media (forced-colors: active) {
  .mat-mdc-form-field.mat-focused .mdc-notched-outline {
    border: dashed 3px;
  }
}

.mat-mdc-form-field-input-control[type=date], .mat-mdc-form-field-input-control[type=datetime], .mat-mdc-form-field-input-control[type=datetime-local], .mat-mdc-form-field-input-control[type=month], .mat-mdc-form-field-input-control[type=week], .mat-mdc-form-field-input-control[type=time] {
  line-height: 1;
}
.mat-mdc-form-field-input-control::-webkit-datetime-edit {
  line-height: 1;
  padding: 0;
  margin-bottom: -2px;
}

.mat-mdc-form-field {
  --%NS%mat-mdc-form-field-floating-label-scale: 0.75;
  display: inline-flex;
  flex-direction: column;
  min-width: 0;
  text-align: left;
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  font-family: var(--%NS%mat-form-field-container-text-font, var(--%NS%mat-sys-body-large-font));
  line-height: var(--%NS%mat-form-field-container-text-line-height, var(--%NS%mat-sys-body-large-line-height));
  font-size: var(--%NS%mat-form-field-container-text-size, var(--%NS%mat-sys-body-large-size));
  letter-spacing: var(--%NS%mat-form-field-container-text-tracking, var(--%NS%mat-sys-body-large-tracking));
  font-weight: var(--%NS%mat-form-field-container-text-weight, var(--%NS%mat-sys-body-large-weight));
}
.mat-mdc-form-field .mdc-text-field--outlined .mdc-floating-label--float-above {
  font-size: calc(var(--%NS%mat-form-field-outlined-label-text-populated-size) * var(--%NS%mat-mdc-form-field-floating-label-scale));
}
.mat-mdc-form-field .mdc-text-field--outlined .mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  font-size: var(--%NS%mat-form-field-outlined-label-text-populated-size);
}
[dir=rtl] .mat-mdc-form-field {
  text-align: right;
}

.mat-mdc-form-field-flex {
  display: inline-flex;
  align-items: baseline;
  box-sizing: border-box;
  width: 100%;
}

.mat-mdc-text-field-wrapper {
  width: 100%;
  z-index: 0;
}

.mat-mdc-form-field-icon-prefix,
.mat-mdc-form-field-icon-suffix {
  align-self: center;
  line-height: 0;
  pointer-events: auto;
  position: relative;
  z-index: 1;
}
.mat-mdc-form-field-icon-prefix > .mat-icon,
.mat-mdc-form-field-icon-suffix > .mat-icon {
  padding: 0 12px;
  box-sizing: content-box;
}

.mat-mdc-form-field-icon-prefix {
  color: var(--%NS%mat-form-field-leading-icon-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-form-field-disabled .mat-mdc-form-field-icon-prefix {
  color: var(--%NS%mat-form-field-disabled-leading-icon-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}

.mat-mdc-form-field-icon-suffix {
  color: var(--%NS%mat-form-field-trailing-icon-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-form-field-disabled .mat-mdc-form-field-icon-suffix {
  color: var(--%NS%mat-form-field-disabled-trailing-icon-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-form-field-invalid .mat-mdc-form-field-icon-suffix {
  color: var(--%NS%mat-form-field-error-trailing-icon-color, var(--%NS%mat-sys-error));
}
.mat-form-field-invalid:not(.mat-focused):not(.mat-form-field-disabled) .mat-mdc-text-field-wrapper:hover .mat-mdc-form-field-icon-suffix {
  color: var(--%NS%mat-form-field-error-hover-trailing-icon-color, var(--%NS%mat-sys-on-error-container));
}
.mat-form-field-invalid.mat-focused .mat-mdc-text-field-wrapper .mat-mdc-form-field-icon-suffix {
  color: var(--%NS%mat-form-field-error-focus-trailing-icon-color, var(--%NS%mat-sys-error));
}

.mat-mdc-form-field-icon-prefix,
[dir=rtl] .mat-mdc-form-field-icon-suffix {
  padding: 0 4px 0 0;
}

.mat-mdc-form-field-icon-suffix,
[dir=rtl] .mat-mdc-form-field-icon-prefix {
  padding: 0 0 0 4px;
}

.mat-mdc-form-field-subscript-wrapper .mat-icon,
.mat-mdc-form-field label .mat-icon {
  width: 1em;
  height: 1em;
  font-size: inherit;
}

.mat-mdc-form-field-infix {
  flex: auto;
  min-width: 0;
  width: 180px;
  position: relative;
  box-sizing: border-box;
}
.mat-mdc-form-field-infix:has(textarea[cols]) {
  width: auto;
}

.mat-mdc-form-field .mdc-notched-outline__notch {
  margin-left: -1px;
  -webkit-clip-path: inset(-9em -999em -9em 1px);
  clip-path: inset(-9em -999em -9em 1px);
}
[dir=rtl] .mat-mdc-form-field .mdc-notched-outline__notch {
  margin-left: 0;
  margin-right: -1px;
  -webkit-clip-path: inset(-9em 1px -9em -999em);
  clip-path: inset(-9em 1px -9em -999em);
}

.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-floating-label {
  transition: transform 150ms cubic-bezier(0.4, 0, 0.2, 1), color 150ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field__input {
  transition: opacity 150ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field__input::placeholder {
  transition: opacity 67ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field__input::-moz-placeholder {
  transition: opacity 67ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field__input::-webkit-input-placeholder {
  transition: opacity 67ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field__input:-ms-input-placeholder {
  transition: opacity 67ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--no-label .mdc-text-field__input::placeholder, .mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--focused .mdc-text-field__input::placeholder {
  transition-delay: 40ms;
  transition-duration: 110ms;
}
.mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--no-label .mdc-text-field__input::-moz-placeholder, .mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--focused .mdc-text-field__input::-moz-placeholder {
  transition-delay: 40ms;
  transition-duration: 110ms;
}
.mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--no-label .mdc-text-field__input::-webkit-input-placeholder, .mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--focused .mdc-text-field__input::-webkit-input-placeholder {
  transition-delay: 40ms;
  transition-duration: 110ms;
}
.mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--no-label .mdc-text-field__input:-ms-input-placeholder, .mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--focused .mdc-text-field__input:-ms-input-placeholder {
  transition-delay: 40ms;
  transition-duration: 110ms;
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field--%NS%filled:not(.mdc-ripple-upgraded):focus .mdc-text-field__ripple::before {
  transition-duration: 75ms;
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-line-ripple::after {
  transition: transform 180ms cubic-bezier(0.4, 0, 0.2, 1), opacity 180ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mat-mdc-form-field-hint-wrapper,
.mat-mdc-form-field.mat-form-field-animations-enabled .mat-mdc-form-field-error-wrapper {
  animation-duration: 300ms;
}

.mdc-notched-outline .mdc-floating-label {
  max-width: calc(100% + 1px);
}

.mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  max-width: calc(133.3333333333% + 1px);
}
`],encapsulation:2})})()}return t})();var DO=new k(`mat-chips-default-options`,{providedIn:`root`,factory:()=>({separatorKeyCodes:[13]})});var qS=new k(`MatChipAvatar`);var YS=new k(`MatChipTrailingIcon`);var KS=new k(`MatChipEdit`);var QS=new k(`MatChipRemove`);var XS=new k(`MatChip`);var ZS=(()=>{class t{_elementRef=f(W);_parentChip=f(XS);_isPrimary=!0;_isLeading=!1;get disabled(){return this._disabled||this._parentChip?.disabled||!1}set disabled(e){this._disabled=e}_disabled=!1;tabIndex=-1;_allowFocusWhenDisabled=!1;_getDisabledAttribute(){return this.disabled&&!this._allowFocusWhenDisabled?``:null}constructor(){f(Je).load(dn),this._elementRef.nativeElement.nodeName===`BUTTON`&&this._elementRef.nativeElement.setAttribute(`type`,`button`)}focus(){this._elementRef.nativeElement.focus()}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`matChipContent`,``]],hostAttrs:[1,`mat-mdc-chip-action`,`mdc-evolution-chip__action`,`mdc-evolution-chip__action--presentational`],hostVars:8,hostBindings:function(i,a){i&2&&(te(`disabled`,a._getDisabledAttribute())(`aria-disabled`,a.disabled),Y(`mdc-evolution-chip__action--primary`,a._isPrimary)(`mdc-evolution-chip__action--secondary`,!a._isPrimary)(`mdc-evolution-chip__action--trailing`,!a._isPrimary&&!a._isLeading))},inputs:{disabled:[2,`disabled`,`disabled`,V],tabIndex:[2,`tabIndex`,`tabIndex`,e=>e==null?-1:lt(e)],_allowFocusWhenDisabled:`_allowFocusWhenDisabled`}})}return t})();var IO=(()=>{class t extends ZS{_getTabindex(){return this.disabled&&!this._allowFocusWhenDisabled?null:this.tabIndex.toString()}_handleClick(e){!this.disabled&&this._isPrimary&&(e.preventDefault(),this._parentChip._handlePrimaryActionInteraction())}_handleKeydown(e){(e.keyCode===13||e.keyCode===32)&&!this.disabled&&this._isPrimary&&!this._parentChip._isEditing&&(e.preventDefault(),this._parentChip._handlePrimaryActionInteraction())}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=K({type:t,selectors:[[``,`matChipAction`,``]],hostVars:3,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._handleClick(r)})(`keydown`,function(r){return a._handleKeydown(r)}),i&2&&(te(`tabindex`,a._getTabindex()),Y(`mdc-evolution-chip__action--presentational`,!1))},features:[Ve]})}return t})();var Bc=(()=>{class t{_changeDetectorRef=f(Fe);_elementRef=f(W);_tagName=f(bx);_ngZone=f(X);_focusMonitor=f(Jt);_globalRippleOptions=f(zr,{optional:!0});_document=f(de);_onFocus=new L;_onBlur=new L;_isBasicChip=!1;role=null;_hasFocusInternal=!1;_pendingFocus=!1;_actionChanges;_animationsDisabled=Ue();_allLeadingIcons;_allTrailingIcons;_allEditIcons;_allRemoveIcons;_hasFocus(){return this._hasFocusInternal}id=f(Be).getId(`mat-mdc-chip-`);ariaLabel=null;ariaDescription=null;_chipListDisabled=!1;_hadFocusOnRemove=!1;_textElement;get value(){return this._value!==void 0?this._value:this._textElement.textContent.trim()}set value(e){this._value=e}_value;color;removable=!0;highlighted=!1;disableRipple=!1;get disabled(){return this._disabled||this._chipListDisabled}set disabled(e){this._disabled=e}_disabled=!1;removed=new ee;destroyed=new ee;basicChipAttrName=`mat-basic-chip`;leadingIcon;editIcon;trailingIcon;removeIcon;primaryAction;_rippleLoader=f(Pu);_injector=f(se);constructor(){let e=f(Je);e.load(dn),e.load(Ii),this._monitorFocus(),this._rippleLoader?.configureRipple(this._elementRef.nativeElement,{className:`mat-mdc-chip-ripple`,disabled:this._isRippleDisabled()})}ngOnInit(){this._isBasicChip=this._elementRef.nativeElement.hasAttribute(this.basicChipAttrName)||this._tagName.toLowerCase()===this.basicChipAttrName}ngAfterViewInit(){this._textElement=this._elementRef.nativeElement.querySelector(`.mat-mdc-chip-action-label`),this._pendingFocus&&(this._pendingFocus=!1,this.focus())}ngAfterContentInit(){this._actionChanges=It(this._allLeadingIcons.changes,this._allTrailingIcons.changes,this._allEditIcons.changes,this._allRemoveIcons.changes).subscribe(()=>this._changeDetectorRef.markForCheck())}ngDoCheck(){this._rippleLoader.setDisabled(this._elementRef.nativeElement,this._isRippleDisabled())}ngOnDestroy(){this.destroyed.emit({chip:this}),this.destroyed.complete(),this._focusMonitor.stopMonitoring(this._elementRef),this._rippleLoader?.destroyRipple(this._elementRef.nativeElement),this._actionChanges?.unsubscribe()}remove(){this.removable&&(this._hadFocusOnRemove=this._hasFocus(),this.removed.emit({chip:this}))}_isRippleDisabled(){return this.disabled||this.disableRipple||this._animationsDisabled||this._isBasicChip||!this._hasInteractiveActions()||!!this._globalRippleOptions?.disabled}_hasTrailingIcon(){return!!(this.trailingIcon||this.removeIcon)}_handleKeydown(e){(e.keyCode===8&&!e.repeat||e.keyCode===46)&&(e.preventDefault(),this.remove())}focus(){this.disabled||(this.primaryAction?this.primaryAction.focus():this._pendingFocus=!0)}_getSourceAction(e){return this._getActions().find(i=>{let a=i._elementRef.nativeElement;return a===e||a.contains(e)})}_getActions(){let e=[];return this.editIcon&&e.push(this.editIcon),this.primaryAction&&e.push(this.primaryAction),this.removeIcon&&e.push(this.removeIcon),e}_handlePrimaryActionInteraction(){}_hasInteractiveActions(){return this._getActions().length>0}_edit(e){}_monitorFocus(){this._focusMonitor.monitor(this._elementRef,!0).subscribe(e=>{let i=e!==null;i!==this._hasFocusInternal&&(this._hasFocusInternal=i,i?this._onFocus.next({chip:this}):(this._changeDetectorRef.markForCheck(),setTimeout(()=>this._ngZone.run(()=>this._onBlur.next({chip:this})))))})}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[`mat-chip-avatar`],[``,`matChipAvatar`,``]],[[`mat-chip-trailing-icon`],[``,`matChipRemove`,``],[``,`matChipTrailingIcon`,``]]],i=[`*`,`mat-chip-avatar, [matChipAvatar]`,`mat-chip-trailing-icon,[matChipRemove],[matChipTrailingIcon]`];function a(r,s){r&1&&(l(0,`span`,3),he(1,1),d())}function o(r,s){r&1&&(l(0,`span`,6),he(1,2),d())}return B({type:t,selectors:[[`mat-basic-chip`],[``,`mat-basic-chip`,``],[`mat-chip`],[``,`mat-chip`,``]],contentQueries:function(s,c,h){if(s&1&&jt(h,qS,5)(h,KS,5)(h,YS,5)(h,QS,5)(h,qS,5)(h,YS,5)(h,KS,5)(h,QS,5),s&2){let p;Z(p=J())&&(c.leadingIcon=p.first),Z(p=J())&&(c.editIcon=p.first),Z(p=J())&&(c.trailingIcon=p.first),Z(p=J())&&(c.removeIcon=p.first),Z(p=J())&&(c._allLeadingIcons=p),Z(p=J())&&(c._allTrailingIcons=p),Z(p=J())&&(c._allEditIcons=p),Z(p=J())&&(c._allRemoveIcons=p)}},viewQuery:function(s,c){if(s&1&&Ze(IO,5),s&2){let h;Z(h=J())&&(c.primaryAction=h.first)}},hostAttrs:[1,`mat-mdc-chip`],hostVars:31,hostBindings:function(s,c){s&1&&C(`keydown`,function(p){return c._handleKeydown(p)}),s&2&&(qt(`id`,c.id),te(`role`,c.role)(`aria-label`,c.ariaLabel),tt(`mat-`+(c.color||`primary`)),Y(`mdc-evolution-chip`,!c._isBasicChip)(`mdc-evolution-chip--disabled`,c.disabled)(`mdc-evolution-chip--with-trailing-action`,c._hasTrailingIcon())(`mdc-evolution-chip--with-primary-graphic`,c.leadingIcon)(`mdc-evolution-chip--with-primary-icon`,c.leadingIcon)(`mdc-evolution-chip--with-avatar`,c.leadingIcon)(`mat-mdc-chip-with-avatar`,c.leadingIcon)(`mat-mdc-chip-highlighted`,c.highlighted)(`mat-mdc-chip-disabled`,c.disabled)(`mat-mdc-basic-chip`,c._isBasicChip)(`mat-mdc-standard-chip`,!c._isBasicChip)(`mat-mdc-chip-with-trailing-icon`,c._hasTrailingIcon())(`_mat-animation-noopable`,c._animationsDisabled))},inputs:{role:`role`,id:`id`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaDescription:[0,`aria-description`,`ariaDescription`],value:`value`,color:`color`,removable:[2,`removable`,`removable`,V],highlighted:[2,`highlighted`,`highlighted`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V],disabled:[2,`disabled`,`disabled`,V]},outputs:{removed:`removed`,destroyed:`destroyed`},exportAs:[`matChip`],features:[Ge([{provide:XS,useExisting:t}])],ngContentSelectors:i,decls:8,vars:2,consts:[[1,`mat-mdc-chip-focus-overlay`],[1,`mdc-evolution-chip__cell`,`mdc-evolution-chip__cell--primary`],[`matChipContent`,``],[1,`mdc-evolution-chip__graphic`,`mat-mdc-chip-graphic`],[1,`mdc-evolution-chip__text-label`,`mat-mdc-chip-action-label`],[1,`mat-mdc-chip-primary-focus-indicator`,`mat-focus-indicator`],[1,`mdc-evolution-chip__cell`,`mdc-evolution-chip__cell--trailing`]],template:function(s,c){s&1&&(Pe(e),H(0,`span`,0),l(1,`span`,1)(2,`span`,2),D(3,a,2,0,`span`,3),l(4,`span`,4),he(5),H(6,`span`,5),d()()(),D(7,o,2,0,`span`,6)),s&2&&(m(3),I(c.leadingIcon?3:-1),m(4),I(c._hasTrailingIcon()?7:-1))},dependencies:[ZS],styles:[`.mdc-evolution-chip,
.mdc-evolution-chip__cell,
.mdc-evolution-chip__action {
  display: inline-flex;
  align-items: center;
}

.mdc-evolution-chip {
  position: relative;
  max-width: 100%;
}

.mdc-evolution-chip__cell,
.mdc-evolution-chip__action {
  height: 100%;
}

.mdc-evolution-chip__cell--primary {
  flex-basis: 100%;
  overflow-x: hidden;
}

.mdc-evolution-chip__cell--trailing {
  flex: 1 0 auto;
}

.mdc-evolution-chip__action {
  align-items: center;
  background: none;
  border: none;
  box-sizing: content-box;
  cursor: pointer;
  display: inline-flex;
  justify-content: center;
  outline: none;
  padding: 0;
  text-decoration: none;
  color: inherit;
}

.mdc-evolution-chip__action--presentational {
  cursor: auto;
}

.mdc-evolution-chip--disabled,
.mdc-evolution-chip__action:disabled {
  pointer-events: none;
}
@media (forced-colors: active) {
  .mdc-evolution-chip--disabled,
  .mdc-evolution-chip__action:disabled {
    forced-color-adjust: none;
  }
}

.mdc-evolution-chip__action--primary {
  font: inherit;
  letter-spacing: inherit;
  white-space: inherit;
  overflow-x: hidden;
}
.mat-mdc-standard-chip .mdc-evolution-chip__action--%NS%primary::before {
  border-width: var(--%NS%mat-chip-outline-width, 1px);
  border-radius: var(--%NS%mat-chip-container-shape-radius, 8px);
  box-sizing: border-box;
  content: "";
  height: 100%;
  left: 0;
  position: absolute;
  pointer-events: none;
  top: 0;
  width: 100%;
  z-index: 1;
  border-style: solid;
}
.mat-mdc-standard-chip .mdc-evolution-chip__action--primary {
  padding-left: 12px;
  padding-right: 12px;
}
.mat-mdc-standard-chip.mdc-evolution-chip--with-primary-graphic .mdc-evolution-chip__action--primary {
  padding-left: 0;
  padding-right: 12px;
}
[dir=rtl] .mat-mdc-standard-chip.mdc-evolution-chip--with-primary-graphic .mdc-evolution-chip__action--primary {
  padding-left: 12px;
  padding-right: 0;
}
.mat-mdc-standard-chip:not(.mdc-evolution-chip--disabled) .mdc-evolution-chip__action--%NS%primary::before {
  border-color: var(--%NS%mat-chip-outline-color, var(--%NS%mat-sys-outline));
}
.mdc-evolution-chip__action--%NS%primary:not(.mdc-evolution-chip__action--presentational):not(.mdc-ripple-upgraded):focus::before {
  border-color: var(--%NS%mat-chip-focus-outline-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-standard-chip.mdc-evolution-chip--disabled .mdc-evolution-chip__action--%NS%primary::before {
  border-color: var(--%NS%mat-chip-disabled-outline-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 12%, transparent));
}
.mat-mdc-standard-chip.mdc-evolution-chip--selected .mdc-evolution-chip__action--%NS%primary::before {
  border-width: var(--%NS%mat-chip-flat-selected-outline-width, 0);
}
.mat-mdc-basic-chip .mdc-evolution-chip__action--primary {
  font: inherit;
}
.mat-mdc-standard-chip.mdc-evolution-chip--with-leading-action .mdc-evolution-chip__action--primary {
  padding-left: 0;
  padding-right: 12px;
}
[dir=rtl] .mat-mdc-standard-chip.mdc-evolution-chip--with-leading-action .mdc-evolution-chip__action--primary {
  padding-left: 12px;
  padding-right: 0;
}
.mat-mdc-standard-chip.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__action--primary {
  padding-left: 12px;
  padding-right: 0;
}
[dir=rtl] .mat-mdc-standard-chip.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__action--primary {
  padding-left: 0;
  padding-right: 12px;
}
.mat-mdc-standard-chip.mdc-evolution-chip--with-leading-action.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__action--primary {
  padding-left: 0;
  padding-right: 0;
}
.mat-mdc-standard-chip.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__action--primary {
  padding-left: 0;
  padding-right: 0;
}
[dir=rtl] .mat-mdc-standard-chip.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__action--primary {
  padding-left: 0;
  padding-right: 0;
}
.mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic .mdc-evolution-chip__action--primary {
  padding-left: 0;
  padding-right: 12px;
}
[dir=rtl] .mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic .mdc-evolution-chip__action--primary {
  padding-left: 12px;
  padding-right: 0;
}
.mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__action--primary {
  padding-left: 0;
  padding-right: 0;
}
[dir=rtl] .mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__action--primary {
  padding-left: 0;
  padding-right: 0;
}

.mdc-evolution-chip__action--secondary {
  position: relative;
  overflow: visible;
}
.mat-mdc-standard-chip:not(.mdc-evolution-chip--disabled) .mdc-evolution-chip__action--secondary {
  color: var(--%NS%mat-chip-with-trailing-icon-trailing-icon-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-standard-chip.mdc-evolution-chip--disabled .mdc-evolution-chip__action--secondary {
  color: var(--%NS%mat-chip-with-trailing-icon-disabled-trailing-icon-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-standard-chip.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__action--secondary, .mat-mdc-standard-chip.mdc-evolution-chip--with-leading-action .mdc-evolution-chip__action--secondary {
  padding-left: 8px;
  padding-right: 8px;
}
.mat-mdc-standard-chip.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__action--secondary, .mat-mdc-standard-chip.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-leading-action .mdc-evolution-chip__action--secondary {
  padding-left: 8px;
  padding-right: 8px;
}
.mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__action--secondary, .mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-leading-action .mdc-evolution-chip__action--secondary {
  padding-left: 8px;
  padding-right: 8px;
}
[dir=rtl] .mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__action--secondary, [dir=rtl] .mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-leading-action .mdc-evolution-chip__action--secondary {
  padding-left: 8px;
  padding-right: 8px;
}

.mdc-evolution-chip__text-label {
  -webkit-user-select: none;
  user-select: none;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
.mat-mdc-standard-chip .mdc-evolution-chip__text-label {
  font-family: var(--%NS%mat-chip-label-text-font, var(--%NS%mat-sys-label-large-font));
  line-height: var(--%NS%mat-chip-label-text-line-height, var(--%NS%mat-sys-label-large-line-height));
  font-size: var(--%NS%mat-chip-label-text-size, var(--%NS%mat-sys-label-large-size));
  font-weight: var(--%NS%mat-chip-label-text-weight, var(--%NS%mat-sys-label-large-weight));
  letter-spacing: var(--%NS%mat-chip-label-text-tracking, var(--%NS%mat-sys-label-large-tracking));
}
.mat-mdc-standard-chip:not(.mdc-evolution-chip--disabled) .mdc-evolution-chip__text-label {
  color: var(--%NS%mat-chip-label-text-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-standard-chip.mdc-evolution-chip--%NS%selected:not(.mdc-evolution-chip--disabled) .mdc-evolution-chip__text-label {
  color: var(--%NS%mat-chip-selected-label-text-color, var(--%NS%mat-sys-on-secondary-container));
}
.mat-mdc-standard-chip.mdc-evolution-chip--disabled .mdc-evolution-chip__text-label, .mat-mdc-standard-chip.mdc-evolution-chip--selected.mdc-evolution-chip--disabled .mdc-evolution-chip__text-label {
  color: var(--%NS%mat-chip-disabled-label-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}

.mdc-evolution-chip__graphic {
  align-items: center;
  display: inline-flex;
  justify-content: center;
  overflow: hidden;
  pointer-events: none;
  position: relative;
  flex: 1 0 auto;
}
.mat-mdc-standard-chip .mdc-evolution-chip__graphic {
  width: var(--%NS%mat-chip-with-avatar-avatar-size, 24px);
  height: var(--%NS%mat-chip-with-avatar-avatar-size, 24px);
  font-size: var(--%NS%mat-chip-with-avatar-avatar-size, 24px);
}
.mdc-evolution-chip--selecting .mdc-evolution-chip__graphic {
  transition: width 150ms 0ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mdc-evolution-chip--%NS%selectable:not(.mdc-evolution-chip--selected):not(.mdc-evolution-chip--with-primary-icon) .mdc-evolution-chip__graphic {
  width: 0;
}
.mat-mdc-standard-chip.mdc-evolution-chip--with-primary-graphic .mdc-evolution-chip__graphic {
  padding-left: 6px;
  padding-right: 6px;
}
.mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic .mdc-evolution-chip__graphic {
  padding-left: 4px;
  padding-right: 8px;
}
[dir=rtl] .mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic .mdc-evolution-chip__graphic {
  padding-left: 8px;
  padding-right: 4px;
}
.mat-mdc-standard-chip.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__graphic {
  padding-left: 6px;
  padding-right: 6px;
}
.mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__graphic {
  padding-left: 4px;
  padding-right: 8px;
}
[dir=rtl] .mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-trailing-action .mdc-evolution-chip__graphic {
  padding-left: 8px;
  padding-right: 4px;
}
.mdc-evolution-chip--with-avatar.mdc-evolution-chip--with-primary-graphic.mdc-evolution-chip--with-leading-action .mdc-evolution-chip__graphic {
  padding-left: 0;
}

.mdc-evolution-chip__checkmark {
  position: absolute;
  opacity: 0;
  top: 50%;
  left: 50%;
  height: 20px;
  width: 20px;
}
.mat-mdc-standard-chip:not(.mdc-evolution-chip--disabled) .mdc-evolution-chip__checkmark {
  color: var(--%NS%mat-chip-with-icon-selected-icon-color, var(--%NS%mat-sys-on-secondary-container));
}
.mat-mdc-standard-chip.mdc-evolution-chip--disabled .mdc-evolution-chip__checkmark {
  color: var(--%NS%mat-chip-with-icon-disabled-icon-color, var(--%NS%mat-sys-on-surface));
}
.mdc-evolution-chip--selecting .mdc-evolution-chip__checkmark {
  transition: transform 150ms 0ms cubic-bezier(0.4, 0, 0.2, 1);
  transform: translate(-75%, -50%);
}
.mdc-evolution-chip--selected .mdc-evolution-chip__checkmark {
  transform: translate(-50%, -50%);
  opacity: 1;
}

.mdc-evolution-chip__checkmark-svg {
  display: block;
}

.mdc-evolution-chip__checkmark-path {
  stroke-width: 2px;
  stroke-dasharray: 29.7833385;
  stroke-dashoffset: 29.7833385;
  stroke: currentColor;
}
.mdc-evolution-chip--selecting .mdc-evolution-chip__checkmark-path {
  transition: stroke-dashoffset 150ms 45ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mdc-evolution-chip--selected .mdc-evolution-chip__checkmark-path {
  stroke-dashoffset: 0;
}
@media (forced-colors: active) {
  .mdc-evolution-chip__checkmark-path {
    stroke: CanvasText !important;
  }
}

.mat-mdc-standard-chip .mdc-evolution-chip__icon--trailing {
  height: 18px;
  width: 18px;
  font-size: 18px;
}
.mdc-evolution-chip--disabled .mdc-evolution-chip__icon--trailing.mat-mdc-chip-remove {
  opacity: calc(var(--%NS%mat-chip-trailing-action-opacity, 1) * var(--%NS%mat-chip-with-trailing-icon-disabled-trailing-icon-opacity, 0.38));
}
.mdc-evolution-chip--disabled .mdc-evolution-chip__icon--trailing.mat-mdc-chip-remove:focus {
  opacity: calc(var(--%NS%mat-chip-trailing-action-focus-opacity, 1) * var(--%NS%mat-chip-with-trailing-icon-disabled-trailing-icon-opacity, 0.38));
}

.mat-mdc-standard-chip {
  border-radius: var(--%NS%mat-chip-container-shape-radius, 8px);
  height: var(--%NS%mat-chip-container-height, 32px);
}
.mat-mdc-standard-chip:not(.mdc-evolution-chip--disabled) {
  background-color: var(--%NS%mat-chip-elevated-container-color, transparent);
}
.mat-mdc-standard-chip.mdc-evolution-chip--disabled {
  background-color: var(--%NS%mat-chip-elevated-disabled-container-color);
}
.mat-mdc-standard-chip.mdc-evolution-chip--%NS%selected:not(.mdc-evolution-chip--disabled) {
  background-color: var(--%NS%mat-chip-elevated-selected-container-color, var(--%NS%mat-sys-secondary-container));
}
.mat-mdc-standard-chip.mdc-evolution-chip--selected.mdc-evolution-chip--disabled {
  background-color: var(--%NS%mat-chip-flat-disabled-selected-container-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 12%, transparent));
}
@media (forced-colors: active) {
  .mat-mdc-standard-chip {
    outline: solid 1px;
  }
}

.mat-mdc-standard-chip .mdc-evolution-chip__icon--primary {
  border-radius: var(--%NS%mat-chip-with-avatar-avatar-shape-radius, 24px);
  width: var(--%NS%mat-chip-with-icon-icon-size, 18px);
  height: var(--%NS%mat-chip-with-icon-icon-size, 18px);
  font-size: var(--%NS%mat-chip-with-icon-icon-size, 18px);
}
.mdc-evolution-chip--selected .mdc-evolution-chip__icon--primary {
  opacity: 0;
}
.mat-mdc-standard-chip:not(.mdc-evolution-chip--disabled) .mdc-evolution-chip__icon--primary {
  color: var(--%NS%mat-chip-with-icon-icon-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-standard-chip.mdc-evolution-chip--disabled .mdc-evolution-chip__icon--primary {
  color: var(--%NS%mat-chip-with-icon-disabled-icon-color, var(--%NS%mat-sys-on-surface));
}

.mat-mdc-chip-highlighted {
  --%NS%mat-chip-with-icon-icon-color: var(--%NS%mat-chip-with-icon-selected-icon-color, var(--%NS%mat-sys-on-secondary-container));
  --%NS%mat-chip-elevated-container-color: var(--%NS%mat-chip-elevated-selected-container-color, var(--%NS%mat-sys-secondary-container));
  --%NS%mat-chip-label-text-color: var(--%NS%mat-chip-selected-label-text-color, var(--%NS%mat-sys-on-secondary-container));
  --%NS%mat-chip-outline-width: var(--%NS%mat-chip-flat-selected-outline-width, 0);
}

.mat-mdc-chip-focus-overlay {
  background: var(--%NS%mat-chip-focus-state-layer-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-chip-selected .mat-mdc-chip-focus-overlay, .mat-mdc-chip-highlighted .mat-mdc-chip-focus-overlay {
  background: var(--%NS%mat-chip-selected-focus-state-layer-color, var(--%NS%mat-sys-on-secondary-container));
}
.mat-mdc-chip:hover .mat-mdc-chip-focus-overlay {
  background: var(--%NS%mat-chip-hover-state-layer-color, var(--%NS%mat-sys-on-surface-variant));
  opacity: var(--%NS%mat-chip-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
}
.mat-mdc-chip-focus-overlay .mat-mdc-chip-selected:hover, .mat-mdc-chip-highlighted:hover .mat-mdc-chip-focus-overlay {
  background: var(--%NS%mat-chip-selected-hover-state-layer-color, var(--%NS%mat-sys-on-secondary-container));
  opacity: var(--%NS%mat-chip-selected-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
}
.mat-mdc-chip.cdk-focused .mat-mdc-chip-focus-overlay {
  background: var(--%NS%mat-chip-focus-state-layer-color, var(--%NS%mat-sys-on-surface-variant));
  opacity: var(--%NS%mat-chip-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity));
}
.mat-mdc-chip-selected.cdk-focused .mat-mdc-chip-focus-overlay, .mat-mdc-chip-highlighted.cdk-focused .mat-mdc-chip-focus-overlay {
  background: var(--%NS%mat-chip-selected-focus-state-layer-color, var(--%NS%mat-sys-on-secondary-container));
  opacity: var(--%NS%mat-chip-selected-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity));
}

.mdc-evolution-chip--%NS%disabled:not(.mdc-evolution-chip--selected) .mat-mdc-chip-avatar {
  opacity: var(--%NS%mat-chip-with-avatar-disabled-avatar-opacity, 0.38);
}

.mdc-evolution-chip--disabled .mdc-evolution-chip__icon--trailing {
  opacity: var(--%NS%mat-chip-with-trailing-icon-disabled-trailing-icon-opacity, 0.38);
}

.mdc-evolution-chip--disabled.mdc-evolution-chip--selected .mdc-evolution-chip__checkmark {
  opacity: var(--%NS%mat-chip-with-icon-disabled-icon-opacity, 0.38);
}

.mat-mdc-standard-chip.mdc-evolution-chip--disabled {
  opacity: var(--%NS%mat-chip-disabled-container-opacity, 1);
}
.mat-mdc-standard-chip.mdc-evolution-chip--selected .mdc-evolution-chip__icon--trailing, .mat-mdc-standard-chip.mat-mdc-chip-highlighted .mdc-evolution-chip__icon--trailing {
  color: var(--%NS%mat-chip-selected-trailing-icon-color, var(--%NS%mat-sys-on-secondary-container));
}
.mat-mdc-standard-chip.mdc-evolution-chip--selected.mdc-evolution-chip--disabled .mdc-evolution-chip__icon--trailing, .mat-mdc-standard-chip.mat-mdc-chip-highlighted.mdc-evolution-chip--disabled .mdc-evolution-chip__icon--trailing {
  color: var(--%NS%mat-chip-selected-disabled-trailing-icon-color, var(--%NS%mat-sys-on-surface));
}

.mat-mdc-chip-edit, .mat-mdc-chip-remove {
  opacity: var(--%NS%mat-chip-trailing-action-opacity, 1);
}
.mat-mdc-chip-edit:focus, .mat-mdc-chip-remove:focus {
  opacity: var(--%NS%mat-chip-trailing-action-focus-opacity, 1);
}
.mat-mdc-chip-edit::after, .mat-mdc-chip-remove::after {
  background-color: var(--%NS%mat-chip-trailing-action-state-layer-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-chip-edit:hover::after, .mat-mdc-chip-remove:hover::after {
  opacity: calc(var(--%NS%mat-chip-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity)) + var(--%NS%mat-chip-trailing-action-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity)));
}
.mat-mdc-chip-edit:focus::after, .mat-mdc-chip-remove:focus::after {
  opacity: calc(var(--%NS%mat-chip-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity)) + var(--%NS%mat-chip-trailing-action-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity)));
}

.mat-mdc-chip-selected .mat-mdc-chip-remove::after,
.mat-mdc-chip-highlighted .mat-mdc-chip-remove::after {
  background-color: var(--%NS%mat-chip-selected-trailing-action-state-layer-color, var(--%NS%mat-sys-on-secondary-container));
}

.mat-mdc-chip.cdk-focused .mat-mdc-chip-edit:focus::after, .mat-mdc-chip.cdk-focused .mat-mdc-chip-remove:focus::after {
  opacity: calc(var(--%NS%mat-chip-selected-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity)) + var(--%NS%mat-chip-trailing-action-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity)));
}
.mat-mdc-chip.cdk-focused .mat-mdc-chip-edit:hover::after, .mat-mdc-chip.cdk-focused .mat-mdc-chip-remove:hover::after {
  opacity: calc(var(--%NS%mat-chip-selected-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity)) + var(--%NS%mat-chip-trailing-action-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity)));
}

.mat-mdc-standard-chip {
  -webkit-tap-highlight-color: transparent;
}
.mat-mdc-standard-chip .mat-mdc-chip-graphic,
.mat-mdc-standard-chip .mat-mdc-chip-trailing-icon {
  box-sizing: content-box;
}
.mat-mdc-standard-chip._mat-animation-noopable,
.mat-mdc-standard-chip._mat-animation-noopable .mdc-evolution-chip__graphic,
.mat-mdc-standard-chip._mat-animation-noopable .mdc-evolution-chip__checkmark,
.mat-mdc-standard-chip._mat-animation-noopable .mdc-evolution-chip__checkmark-path {
  transition-duration: 1ms;
  animation-duration: 1ms;
}

.mat-mdc-chip-focus-overlay {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
  opacity: 0;
  border-radius: inherit;
  transition: opacity 150ms linear;
}
._mat-animation-noopable .mat-mdc-chip-focus-overlay {
  transition: none;
}
.mat-mdc-basic-chip .mat-mdc-chip-focus-overlay {
  display: none;
}

.mat-mdc-chip .mat-ripple.mat-mdc-chip-ripple {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
  border-radius: inherit;
}

.mat-mdc-chip-avatar {
  text-align: center;
  line-height: 1;
  color: var(--%NS%mat-chip-with-icon-icon-color, currentColor);
}

.mat-mdc-chip {
  position: relative;
  z-index: 0;
}

.mat-mdc-chip-action-label {
  text-align: left;
  z-index: 1;
}
[dir=rtl] .mat-mdc-chip-action-label {
  text-align: right;
}
.mat-mdc-chip.mdc-evolution-chip--with-trailing-action .mat-mdc-chip-action-label {
  position: relative;
}
.mat-mdc-chip-action-label .mat-mdc-chip-primary-focus-indicator {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  pointer-events: none;
}
.mat-mdc-chip-action-label .mat-focus-indicator::before {
  margin: calc(calc(var(--%NS%mat-focus-indicator-border-width, 3px) + 2px) * -1);
}

.mat-mdc-chip-edit::before, .mat-mdc-chip-remove::before {
  margin: calc(var(--%NS%mat-focus-indicator-border-width, 3px) * -1);
  left: 8px;
  right: 8px;
}
.mat-mdc-chip-edit::after, .mat-mdc-chip-remove::after {
  content: "";
  display: block;
  opacity: 0;
  position: absolute;
  top: -3px;
  bottom: -3px;
  left: 5px;
  right: 5px;
  border-radius: 50%;
  box-sizing: border-box;
  padding: 12px;
  margin: -12px;
  background-clip: content-box;
}
.mat-mdc-chip-edit .mat-icon, .mat-mdc-chip-remove .mat-icon {
  width: 18px;
  height: 18px;
  font-size: 18px;
  box-sizing: content-box;
}

.mat-chip-edit-input {
  cursor: text;
  display: inline-block;
  color: inherit;
  outline: 0;
}

@media (forced-colors: active) {
  .mat-mdc-chip-selected:not(.mat-mdc-chip-multiple) {
    outline-width: 3px;
  }
}

.mat-mdc-chip-action:focus-visible .mat-focus-indicator::before {
  content: "";
}

.mdc-evolution-chip__icon, .mat-mdc-chip-edit .mat-icon, .mat-mdc-chip-remove .mat-icon {
  min-height: fit-content;
}

img.mdc-evolution-chip__icon {
  min-height: 0;
}
`],encapsulation:2})})()}return t})();var Ju=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Fe);_dir=f(gt,{optional:!0});_lastDestroyedFocusedChipIndex=null;_keyManager;_destroyed=new L;_defaultRole=`presentation`;get chipFocusChanges(){return this._getChipStream(e=>e._onFocus)}get chipDestroyedChanges(){return this._getChipStream(e=>e.destroyed)}get chipRemovedChanges(){return this._getChipStream(e=>e.removed)}get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._syncChipsState()}_disabled=!1;get empty(){return!this._chips||this._chips.length===0}get role(){return this._explicitRole?this._explicitRole:this.empty?null:this._defaultRole}tabIndex=0;set role(e){this._explicitRole=e}_explicitRole=null;get focused(){return this._hasFocusedChip()}_chips;_chipActions=new wn;ngAfterViewInit(){this._setUpFocusManagement(),this._trackChipSetChanges(),this._trackDestroyedFocusedChip()}ngOnDestroy(){this._keyManager?.destroy(),this._chipActions.destroy(),this._destroyed.next(),this._destroyed.complete()}_hasFocusedChip(){return this._chips&&this._chips.some(e=>e._hasFocus())}_syncChipsState(){this._chips?.forEach(e=>{e._chipListDisabled=this._disabled,e._changeDetectorRef.markForCheck()})}focus(){}_handleKeydown(e){this._originatesFromChip(e)&&this._keyManager.onKeydown(e)}_isValidIndex(e){return e>=0&&e<this._chips.length}_allowFocusEscape(){let e=this._elementRef.nativeElement.tabIndex;e!==-1&&(this._elementRef.nativeElement.tabIndex=-1,setTimeout(()=>this._elementRef.nativeElement.tabIndex=e))}_getChipStream(e){return this._chips.changes.pipe(mt(null),_n(()=>It(...this._chips.map(e))))}_originatesFromChip(e){let i=e.target;for(;i&&i!==this._elementRef.nativeElement;){if(i.classList.contains(`mat-mdc-chip`))return!0;i=i.parentElement}return!1}_setUpFocusManagement(){this._chips.changes.pipe(mt(this._chips)).subscribe(e=>{let i=[];e.forEach(a=>a._getActions().forEach(o=>i.push(o))),this._chipActions.reset(i),this._chipActions.notifyOnChanges()}),this._keyManager=new ea(this._chipActions).withVerticalOrientation().withHorizontalOrientation(this._dir?this._dir.value:`ltr`).withHomeAndEnd().skipPredicate(e=>this._skipPredicate(e)),this.chipFocusChanges.pipe(Te(this._destroyed)).subscribe(({chip:e})=>{let i=e._getSourceAction(document.activeElement);i&&this._keyManager.updateActiveItem(i)}),this._dir?.change.pipe(Te(this._destroyed)).subscribe(e=>this._keyManager.withHorizontalOrientation(e))}_skipPredicate(e){return e.disabled}_trackChipSetChanges(){this._chips.changes.pipe(mt(null),Te(this._destroyed)).subscribe(()=>{this.disabled&&Promise.resolve().then(()=>this._syncChipsState()),this._redirectDestroyedChipFocus()})}_trackDestroyedFocusedChip(){this.chipDestroyedChanges.pipe(Te(this._destroyed)).subscribe(e=>{let a=this._chips.toArray().indexOf(e.chip),o=e.chip._hasFocus(),r=e.chip._hadFocusOnRemove&&this._keyManager.activeItem&&e.chip._getActions().includes(this._keyManager.activeItem),s=o||r;this._isValidIndex(a)&&s&&(this._lastDestroyedFocusedChipIndex=a)})}_redirectDestroyedChipFocus(){if(this._lastDestroyedFocusedChipIndex!=null){if(this._chips.length){let e=Math.min(this._lastDestroyedFocusedChipIndex,this._chips.length-1),i=this._chips.toArray()[e];i.disabled?this._chips.length===1?this.focus():this._keyManager.setPreviousItemActive():i.focus()}else this.focus();this._lastDestroyedFocusedChipIndex=null}}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return B({type:t,selectors:[[`mat-chip-set`]],contentQueries:function(a,o,r){if(a&1&&jt(r,Bc,5),a&2){let s;Z(s=J())&&(o._chips=s)}},hostAttrs:[1,`mat-mdc-chip-set`,`mdc-evolution-chip-set`],hostVars:1,hostBindings:function(a,o){a&1&&C(`keydown`,function(s){return o._handleKeydown(s)}),a&2&&te(`role`,o.role)},inputs:{disabled:[2,`disabled`,`disabled`,V],role:`role`,tabIndex:[2,`tabIndex`,`tabIndex`,i=>i==null?0:lt(i)]},ngContentSelectors:[`*`],decls:2,vars:0,consts:[[`role`,`presentation`,1,`mdc-evolution-chip-set__chips`]],template:function(a,o){a&1&&(Pe(),ct(0,`div`,0),he(1),ht())},styles:[`.mat-mdc-chip-set {
  display: flex;
}
.mat-mdc-chip-set:focus {
  outline: none;
}
.mat-mdc-chip-set .mdc-evolution-chip-set__chips {
  min-width: 100%;
  margin-left: -8px;
  margin-right: 0;
}
.mat-mdc-chip-set .mdc-evolution-chip {
  margin: 4px 0 4px 8px;
}
[dir=rtl] .mat-mdc-chip-set .mdc-evolution-chip-set__chips {
  margin-left: 0;
  margin-right: -8px;
}
[dir=rtl] .mat-mdc-chip-set .mdc-evolution-chip {
  margin-left: 0;
  margin-right: 8px;
}

.mdc-evolution-chip-set__chips {
  display: flex;
  flex-flow: wrap;
  min-width: 0;
}

.mat-mdc-chip-set-stacked {
  flex-direction: column;
  align-items: flex-start;
}
.mat-mdc-chip-set-stacked .mat-mdc-chip {
  width: 100%;
}
.mat-mdc-chip-set-stacked .mdc-evolution-chip__graphic {
  flex-grow: 0;
}
.mat-mdc-chip-set-stacked .mdc-evolution-chip__action--primary {
  flex-basis: 100%;
  justify-content: start;
}

input.mat-mdc-chip-input {
  flex: 1 0 150px;
  margin-left: 8px;
}
[dir=rtl] input.mat-mdc-chip-input {
  margin-left: 0;
  margin-right: 8px;
}
.mat-mdc-form-field:not(.mat-form-field-hide-placeholder) input.mat-mdc-chip-input::placeholder {
  opacity: 1;
}
.mat-mdc-form-field:not(.mat-form-field-hide-placeholder) input.mat-mdc-chip-input::-moz-placeholder {
  opacity: 1;
}
.mat-mdc-form-field:not(.mat-form-field-hide-placeholder) input.mat-mdc-chip-input::-webkit-input-placeholder {
  opacity: 1;
}
.mat-mdc-form-field:not(.mat-form-field-hide-placeholder) input.mat-mdc-chip-input:-ms-input-placeholder {
  opacity: 1;
}
.mat-mdc-chip-set + input.mat-mdc-chip-input {
  margin-left: 0;
  margin-right: 0;
}
`],encapsulation:2})})()}return t})();var em=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({providers:[Yr,{provide:DO,useValue:{separatorKeyCodes:[13]}}],imports:[ki,we]})}return t})();var ew=(()=>{class t{labelPosition=`after`;static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return B({type:t,selectors:[[``,`mat-internal-form-field`,``]],hostAttrs:[1,`mdc-form-field`,`mat-internal-form-field`],hostVars:2,hostBindings:function(a,o){a&2&&Y(`mdc-form-field--align-end`,o.labelPosition===`before`)},inputs:{labelPosition:`labelPosition`},ngContentSelectors:[`*`],decls:1,vars:0,template:function(a,o){a&1&&(Pe(),he(0))},styles:[`.mat-internal-form-field {
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  display: inline-flex;
  align-items: center;
  vertical-align: middle;
}
.mat-internal-form-field > label, .mat-internal-form-field > .mat-internal-form-field-label {
  margin-left: 0;
  margin-right: auto;
  padding-left: 4px;
  padding-right: 0;
  order: 0;
}
[dir=rtl] .mat-internal-form-field > label, [dir=rtl] .mat-internal-form-field > .mat-internal-form-field-label {
  margin-left: auto;
  margin-right: 0;
  padding-left: 0;
  padding-right: 4px;
}

.mdc-form-field--align-end > label, .mdc-form-field--align-end > .mat-internal-form-field-label {
  margin-left: auto;
  margin-right: 0;
  padding-left: 0;
  padding-right: 4px;
  order: -1;
}
[dir=rtl] .mdc-form-field--align-end .mdc-form-field--align-end label, [dir=rtl] .mdc-form-field--align-end .mdc-form-field--align-end .mat-internal-form-field-label {
  margin-left: 0;
  margin-right: auto;
  padding-left: 4px;
  padding-right: 0;
}
`],encapsulation:2})})()}return t})();var rb={color:`accent`,clickAction:`check-indeterminate`,disabledInteractive:!1};var kO=new k(`mat-checkbox-default-options`,{providedIn:`root`,factory:()=>rb});var tn=(function(t){return t[t.Init=0]=`Init`,t[t.Checked=1]=`Checked`,t[t.Unchecked=2]=`Unchecked`,t[t.Indeterminate=3]=`Indeterminate`,t})(tn||{});var sb=class{source;checked};var jc=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Fe);_ngZone=f(X);_animationsDisabled=Ue();_options=f(kO,{optional:!0});focus(){this._inputElement.nativeElement.focus()}_createChangeEvent(e){let i=new sb;return i.source=this,i.checked=e,i}_getAnimationTargetElement(){return this._inputElement?.nativeElement}_animationClasses={uncheckedToChecked:`mdc-checkbox--anim-unchecked-checked`,uncheckedToIndeterminate:`mdc-checkbox--anim-unchecked-indeterminate`,checkedToUnchecked:`mdc-checkbox--anim-checked-unchecked`,checkedToIndeterminate:`mdc-checkbox--anim-checked-indeterminate`,indeterminateToChecked:`mdc-checkbox--anim-indeterminate-checked`,indeterminateToUnchecked:`mdc-checkbox--anim-indeterminate-unchecked`};ariaLabel=``;ariaLabelledby=null;ariaDescribedby;ariaExpanded;ariaControls;ariaOwns;_uniqueId;id;get inputId(){return`${this.id||this._uniqueId}-input`}required=!1;labelPosition=`after`;name=null;change=new ee;indeterminateChange=new ee;value;disableRipple=!1;_inputElement;tabIndex;color;disabledInteractive;_onTouched=()=>{};_currentAnimationClass=``;_currentCheckState=tn.Init;_controlValueAccessorChangeFn=()=>{};_validatorChangeFn=()=>{};constructor(){f(Je).load(dn);let e=f(new Ki(`tabindex`),{optional:!0});this._options=this._options||rb,this.color=this._options.color||rb.color,this.tabIndex=e==null?0:parseInt(e)||0,this.id=this._uniqueId=f(Be).getId(`mat-mdc-checkbox-`),this.disabledInteractive=this._options?.disabledInteractive??!1}ngOnChanges(e){e.required&&this._validatorChangeFn()}ngAfterViewInit(){this._syncIndeterminate(this.indeterminate)}get checked(){return this._checked}set checked(e){e!=this.checked&&(this._checked=e,this._changeDetectorRef.markForCheck())}_checked=!1;get disabled(){return this._disabled}set disabled(e){e!==this.disabled&&(this._disabled=e,this._changeDetectorRef.markForCheck())}_disabled=!1;get indeterminate(){return this._indeterminate()}set indeterminate(e){let i=e!=this._indeterminate();this._indeterminate.set(e),i&&(e?this._transitionCheckState(tn.Indeterminate):this._transitionCheckState(this.checked?tn.Checked:tn.Unchecked),this.indeterminateChange.emit(e)),this._syncIndeterminate(e)}_indeterminate=T(!1);_isRippleDisabled(){return this.disableRipple||this.disabled}_onLabelTextChange(){this._changeDetectorRef.detectChanges()}writeValue(e){this.checked=!!e}registerOnChange(e){this._controlValueAccessorChangeFn=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e}validate(e){return this.required&&e.value!==!0?{required:!0}:null}registerOnValidatorChange(e){this._validatorChangeFn=e}_transitionCheckState(e){let i=this._currentCheckState,a=this._getAnimationTargetElement();if(!(i===e||!a)&&(this._currentAnimationClass&&a.classList.remove(this._currentAnimationClass),this._currentAnimationClass=this._getAnimationClassForCheckStateTransition(i,e),this._currentCheckState=e,this._currentAnimationClass.length>0)){a.classList.add(this._currentAnimationClass);let o=this._currentAnimationClass;this._ngZone.runOutsideAngular(()=>{setTimeout(()=>{a.classList.remove(o)},1e3)})}}_emitChangeEvent(){this._controlValueAccessorChangeFn(this.checked),this.change.emit(this._createChangeEvent(this.checked)),this._inputElement&&(this._inputElement.nativeElement.checked=this.checked)}toggle(){this.checked=!this.checked,this._controlValueAccessorChangeFn(this.checked)}_handleInputClick(){let e=this._options?.clickAction;!this.disabled&&e!==`noop`?(this.indeterminate&&e!==`check`&&Promise.resolve().then(()=>{this._indeterminate.set(!1),this.indeterminateChange.emit(!1)}),this._checked=!this._checked,this._transitionCheckState(this._checked?tn.Checked:tn.Unchecked),this._emitChangeEvent()):(this.disabled&&this.disabledInteractive||!this.disabled&&e===`noop`)&&(this._inputElement.nativeElement.checked=this.checked,this._inputElement.nativeElement.indeterminate=this.indeterminate)}_onInteractionEvent(e){e.stopPropagation()}_onBlur(){Promise.resolve().then(()=>{this._onTouched(),this._changeDetectorRef.markForCheck()})}_getAnimationClassForCheckStateTransition(e,i){if(this._animationsDisabled)return``;switch(e){case tn.Init:if(i===tn.Checked)return this._animationClasses.uncheckedToChecked;if(i==tn.Indeterminate)return this._checked?this._animationClasses.checkedToIndeterminate:this._animationClasses.uncheckedToIndeterminate;break;case tn.Unchecked:return i===tn.Checked?this._animationClasses.uncheckedToChecked:this._animationClasses.uncheckedToIndeterminate;case tn.Checked:return i===tn.Unchecked?this._animationClasses.checkedToUnchecked:this._animationClasses.checkedToIndeterminate;case tn.Indeterminate:return i===tn.Checked?this._animationClasses.indeterminateToChecked:this._animationClasses.indeterminateToUnchecked}return``}_syncIndeterminate(e){let i=this._inputElement;i&&(i.nativeElement.indeterminate=e)}_onInputClick(){this._handleInputClick()}_preventBubblingFromLabel(e){e.target&&this._inputElement&&e.target!==this._inputElement.nativeElement&&e.stopPropagation()}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`input`];return B({type:t,selectors:[[`mat-checkbox`]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;Z(s=J())&&(r._inputElement=s.first)}},hostAttrs:[1,`mat-mdc-checkbox`],hostVars:16,hostBindings:function(o,r){o&2&&(qt(`id`,r.id),te(`tabindex`,null)(`aria-label`,null)(`aria-labelledby`,null),tt(r.color?`mat-`+r.color:`mat-accent`),Y(`_mat-animation-noopable`,r._animationsDisabled)(`mdc-checkbox--disabled`,r.disabled)(`mat-mdc-checkbox-disabled`,r.disabled)(`mat-mdc-checkbox-checked`,r.checked)(`mat-mdc-checkbox-disabled-interactive`,r.disabledInteractive))},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],ariaDescribedby:[0,`aria-describedby`,`ariaDescribedby`],ariaExpanded:[2,`aria-expanded`,`ariaExpanded`,V],ariaControls:[0,`aria-controls`,`ariaControls`],ariaOwns:[0,`aria-owns`,`ariaOwns`],id:`id`,required:[2,`required`,`required`,V],labelPosition:`labelPosition`,name:`name`,value:`value`,disableRipple:[2,`disableRipple`,`disableRipple`,V],tabIndex:[2,`tabIndex`,`tabIndex`,a=>a==null?void 0:lt(a)],color:`color`,disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],checked:[2,`checked`,`checked`,V],disabled:[2,`disabled`,`disabled`,V],indeterminate:[2,`indeterminate`,`indeterminate`,V]},outputs:{change:`change`,indeterminateChange:`indeterminateChange`},exportAs:[`matCheckbox`],features:[Ge([{provide:Wr,useExisting:Ht(()=>t),multi:!0},{provide:qr,useExisting:t,multi:!0}]),vt],ngContentSelectors:[`*`],decls:15,vars:23,consts:[[`checkbox`,``],[`input`,``],[`label`,``],[`mat-internal-form-field`,``,3,`click`,`labelPosition`,`for`],[1,`mdc-checkbox`],[`aria-hidden`,`true`,1,`mat-mdc-checkbox-touch-target`],[`type`,`checkbox`,1,`mdc-checkbox__native-control`,3,`blur`,`click`,`change`,`checked`,`indeterminate`,`disabled`,`id`,`required`,`tabIndex`],[`aria-hidden`,`true`,1,`mdc-checkbox__ripple`],[`aria-hidden`,`true`,1,`mdc-checkbox__background`],[`focusable`,`false`,`viewBox`,`0 0 24 24`,1,`mdc-checkbox__checkmark`],[`fill`,`none`,`d`,`M1.73,12.91 8.1,19.28 22.79,4.59`,1,`mdc-checkbox__checkmark-path`],[1,`mdc-checkbox__mixedmark`],[`mat-ripple`,``,`aria-hidden`,`true`,1,`mat-mdc-checkbox-ripple`,`mat-focus-indicator`,3,`matRippleTrigger`,`matRippleDisabled`,`matRippleCentered`],[1,`mat-internal-form-field-label`,`mdc-label`]],template:function(o,r){if(o&1&&(Pe(),l(0,`label`,3),C(`click`,function(c){return r._preventBubblingFromLabel(c)}),l(1,`span`,4,0),H(3,`span`,5),l(4,`input`,6,1),C(`blur`,function(){return r._onBlur()})(`click`,function(){return r._onInputClick()})(`change`,function(c){return r._onInteractionEvent(c)}),d(),H(6,`span`,7),l(7,`span`,8),Gt(),l(8,`svg`,9),H(9,`path`,10),d(),_a(),H(10,`span`,11),d(),H(11,`span`,12),d(),l(12,`span`,13,2),he(14),d()()),o&2){let s=ft(2);S(`labelPosition`,r.labelPosition)(`for`,r.inputId),m(4),Y(`mdc-checkbox--selected`,r.checked),S(`checked`,r.checked)(`indeterminate`,r.indeterminate)(`disabled`,r.disabled&&!r.disabledInteractive)(`id`,r.inputId)(`required`,r.required)(`tabIndex`,r.disabled&&!r.disabledInteractive?-1:r.tabIndex),te(`aria-label`,r.ariaLabel||null)(`aria-labelledby`,r.ariaLabelledby)(`aria-describedby`,r.ariaDescribedby)(`aria-checked`,r.indeterminate?`mixed`:null)(`aria-controls`,r.ariaControls)(`aria-disabled`,r.disabled&&r.disabledInteractive?!0:null)(`aria-expanded`,r.ariaExpanded)(`aria-owns`,r.ariaOwns)(`name`,r.name)(`value`,r.value),m(7),S(`matRippleTrigger`,s)(`matRippleDisabled`,r.disableRipple||r.disabled)(`matRippleCentered`,!0)}},dependencies:[ci,ew],styles:[`.mdc-checkbox {
  display: inline-block;
  position: relative;
  flex: 0 0 18px;
  box-sizing: content-box;
  width: 18px;
  height: 18px;
  line-height: 0;
  white-space: nowrap;
  cursor: pointer;
  vertical-align: bottom;
  padding: calc((var(--%NS%mat-checkbox-state-layer-size, 40px) - 18px) / 2);
  margin: calc((var(--%NS%mat-checkbox-state-layer-size, 40px) - var(--%NS%mat-checkbox-state-layer-size, 40px)) / 2);
}
.mdc-checkbox:hover > .mdc-checkbox__ripple {
  opacity: var(--%NS%mat-checkbox-unselected-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
  background-color: var(--%NS%mat-checkbox-unselected-hover-state-layer-color, var(--%NS%mat-sys-on-surface));
}
.mdc-checkbox:hover > .mat-mdc-checkbox-ripple > .mat-ripple-element {
  background-color: var(--%NS%mat-checkbox-unselected-hover-state-layer-color, var(--%NS%mat-sys-on-surface));
}
.mdc-checkbox .mdc-checkbox__native-control:focus + .mdc-checkbox__ripple {
  opacity: var(--%NS%mat-checkbox-unselected-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity));
  background-color: var(--%NS%mat-checkbox-unselected-focus-state-layer-color, var(--%NS%mat-sys-on-surface));
}
.mdc-checkbox .mdc-checkbox__native-control:focus ~ .mat-mdc-checkbox-ripple .mat-ripple-element {
  background-color: var(--%NS%mat-checkbox-unselected-focus-state-layer-color, var(--%NS%mat-sys-on-surface));
}
.mdc-checkbox:active > .mdc-checkbox__native-control + .mdc-checkbox__ripple {
  opacity: var(--%NS%mat-checkbox-unselected-pressed-state-layer-opacity, var(--%NS%mat-sys-pressed-state-layer-opacity));
  background-color: var(--%NS%mat-checkbox-unselected-pressed-state-layer-color, var(--%NS%mat-sys-primary));
}
.mdc-checkbox:active > .mdc-checkbox__native-control ~ .mat-mdc-checkbox-ripple .mat-ripple-element {
  background-color: var(--%NS%mat-checkbox-unselected-pressed-state-layer-color, var(--%NS%mat-sys-primary));
}
.mdc-checkbox:hover > .mdc-checkbox__native-control:checked + .mdc-checkbox__ripple {
  opacity: var(--%NS%mat-checkbox-selected-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
  background-color: var(--%NS%mat-checkbox-selected-hover-state-layer-color, var(--%NS%mat-sys-primary));
}
.mdc-checkbox:hover > .mdc-checkbox__native-control:checked ~ .mat-mdc-checkbox-ripple .mat-ripple-element {
  background-color: var(--%NS%mat-checkbox-selected-hover-state-layer-color, var(--%NS%mat-sys-primary));
}
.mdc-checkbox .mdc-checkbox__native-control:focus:checked + .mdc-checkbox__ripple {
  opacity: var(--%NS%mat-checkbox-selected-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity));
  background-color: var(--%NS%mat-checkbox-selected-focus-state-layer-color, var(--%NS%mat-sys-primary));
}
.mdc-checkbox .mdc-checkbox__native-control:focus:checked ~ .mat-mdc-checkbox-ripple .mat-ripple-element {
  background-color: var(--%NS%mat-checkbox-selected-focus-state-layer-color, var(--%NS%mat-sys-primary));
}
.mdc-checkbox:active > .mdc-checkbox__native-control:checked + .mdc-checkbox__ripple {
  opacity: var(--%NS%mat-checkbox-selected-pressed-state-layer-opacity, var(--%NS%mat-sys-pressed-state-layer-opacity));
  background-color: var(--%NS%mat-checkbox-selected-pressed-state-layer-color, var(--%NS%mat-sys-on-surface));
}
.mdc-checkbox:active > .mdc-checkbox__native-control:checked ~ .mat-mdc-checkbox-ripple .mat-ripple-element {
  background-color: var(--%NS%mat-checkbox-selected-pressed-state-layer-color, var(--%NS%mat-sys-on-surface));
}
.mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox .mdc-checkbox__native-control ~ .mat-mdc-checkbox-ripple .mat-ripple-element,
.mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox .mdc-checkbox__native-control + .mdc-checkbox__ripple {
  background-color: var(--%NS%mat-checkbox-unselected-hover-state-layer-color, var(--%NS%mat-sys-on-surface));
}
.mdc-checkbox .mdc-checkbox__native-control {
  position: absolute;
  margin: 0;
  padding: 0;
  opacity: 0;
  cursor: inherit;
  z-index: 1;
  width: var(--%NS%mat-checkbox-state-layer-size, 40px);
  height: var(--%NS%mat-checkbox-state-layer-size, 40px);
  top: calc((var(--%NS%mat-checkbox-state-layer-size, 40px) - var(--%NS%mat-checkbox-state-layer-size, 40px)) / 2);
  right: calc((var(--%NS%mat-checkbox-state-layer-size, 40px) - var(--%NS%mat-checkbox-state-layer-size, 40px)) / 2);
  left: calc((var(--%NS%mat-checkbox-state-layer-size, 40px) - var(--%NS%mat-checkbox-state-layer-size, 40px)) / 2);
}

.mdc-checkbox--disabled {
  cursor: default;
  pointer-events: none;
}

.mdc-checkbox__background {
  display: inline-flex;
  position: absolute;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  width: 18px;
  height: 18px;
  border: 2px solid currentColor;
  border-radius: 2px;
  background-color: transparent;
  pointer-events: none;
  will-change: background-color, border-color;
  transition: background-color 90ms cubic-bezier(0.4, 0, 0.6, 1), border-color 90ms cubic-bezier(0.4, 0, 0.6, 1);
  -webkit-print-color-adjust: exact;
  color-adjust: exact;
  border-color: var(--%NS%mat-checkbox-unselected-icon-color, var(--%NS%mat-sys-on-surface-variant));
  top: calc((var(--%NS%mat-checkbox-state-layer-size, 40px) - 18px) / 2);
  left: calc((var(--%NS%mat-checkbox-state-layer-size, 40px) - 18px) / 2);
}

.mdc-checkbox__native-control:enabled:checked ~ .mdc-checkbox__background,
.mdc-checkbox__native-control:enabled:indeterminate ~ .mdc-checkbox__background {
  border-color: var(--%NS%mat-checkbox-selected-icon-color, var(--%NS%mat-sys-primary));
  background-color: var(--%NS%mat-checkbox-selected-icon-color, var(--%NS%mat-sys-primary));
}

.mdc-checkbox--disabled .mdc-checkbox__background {
  border-color: var(--%NS%mat-checkbox-disabled-unselected-icon-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
@media (forced-colors: active) {
  .mdc-checkbox--disabled .mdc-checkbox__background {
    border-color: GrayText;
  }
}

.mdc-checkbox__native-control:disabled:checked ~ .mdc-checkbox__background,
.mdc-checkbox__native-control:disabled:indeterminate ~ .mdc-checkbox__background {
  background-color: var(--%NS%mat-checkbox-disabled-selected-icon-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
  border-color: transparent;
}
@media (forced-colors: active) {
  .mdc-checkbox__native-control:disabled:checked ~ .mdc-checkbox__background,
  .mdc-checkbox__native-control:disabled:indeterminate ~ .mdc-checkbox__background {
    border-color: GrayText;
  }
}

.mdc-checkbox:hover > .mdc-checkbox__native-control:not(:checked) ~ .mdc-checkbox__background,
.mdc-checkbox:hover > .mdc-checkbox__native-control:not(:indeterminate) ~ .mdc-checkbox__background {
  border-color: var(--%NS%mat-checkbox-unselected-hover-icon-color, var(--%NS%mat-sys-on-surface));
  background-color: transparent;
}

.mdc-checkbox:hover > .mdc-checkbox__native-control:checked ~ .mdc-checkbox__background,
.mdc-checkbox:hover > .mdc-checkbox__native-control:indeterminate ~ .mdc-checkbox__background {
  border-color: var(--%NS%mat-checkbox-selected-hover-icon-color, var(--%NS%mat-sys-primary));
  background-color: var(--%NS%mat-checkbox-selected-hover-icon-color, var(--%NS%mat-sys-primary));
}

.mdc-checkbox__native-control:focus:focus:not(:checked) ~ .mdc-checkbox__background,
.mdc-checkbox__native-control:focus:focus:not(:indeterminate) ~ .mdc-checkbox__background {
  border-color: var(--%NS%mat-checkbox-unselected-focus-icon-color, var(--%NS%mat-sys-on-surface));
}

.mdc-checkbox__native-control:focus:focus:checked ~ .mdc-checkbox__background,
.mdc-checkbox__native-control:focus:focus:indeterminate ~ .mdc-checkbox__background {
  border-color: var(--%NS%mat-checkbox-selected-focus-icon-color, var(--%NS%mat-sys-primary));
  background-color: var(--%NS%mat-checkbox-selected-focus-icon-color, var(--%NS%mat-sys-primary));
}

.mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox:hover > .mdc-checkbox__native-control ~ .mdc-checkbox__background,
.mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox .mdc-checkbox__native-control:focus ~ .mdc-checkbox__background,
.mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox__background {
  border-color: var(--%NS%mat-checkbox-disabled-unselected-icon-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
@media (forced-colors: active) {
  .mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox:hover > .mdc-checkbox__native-control ~ .mdc-checkbox__background,
  .mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox .mdc-checkbox__native-control:focus ~ .mdc-checkbox__background,
  .mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox__background {
    border-color: GrayText;
  }
}
.mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox__native-control:checked ~ .mdc-checkbox__background,
.mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox__native-control:indeterminate ~ .mdc-checkbox__background {
  background-color: var(--%NS%mat-checkbox-disabled-selected-icon-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
  border-color: transparent;
}

.mdc-checkbox__checkmark {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  width: 100%;
  opacity: 0;
  transition: opacity 180ms cubic-bezier(0.4, 0, 0.6, 1);
  color: var(--%NS%mat-checkbox-selected-checkmark-color, var(--%NS%mat-sys-on-primary));
}
@media (forced-colors: active) {
  .mdc-checkbox__checkmark {
    color: CanvasText;
  }
}

.mdc-checkbox--disabled .mdc-checkbox__checkmark, .mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox__checkmark {
  color: var(--%NS%mat-checkbox-disabled-selected-checkmark-color, var(--%NS%mat-sys-surface));
}
@media (forced-colors: active) {
  .mdc-checkbox--disabled .mdc-checkbox__checkmark, .mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox__checkmark {
    color: GrayText;
  }
}

.mdc-checkbox__checkmark-path {
  transition: stroke-dashoffset 180ms cubic-bezier(0.4, 0, 0.6, 1);
  stroke: currentColor;
  stroke-width: 3.12px;
  stroke-dashoffset: 29.7833385;
  stroke-dasharray: 29.7833385;
}

.mdc-checkbox__mixedmark {
  width: 100%;
  height: 0;
  transform: scaleX(0) rotate(0deg);
  border-width: 1px;
  border-style: solid;
  opacity: 0;
  transition: opacity 90ms cubic-bezier(0.4, 0, 0.6, 1), transform 90ms cubic-bezier(0.4, 0, 0.6, 1);
  border-color: var(--%NS%mat-checkbox-selected-checkmark-color, var(--%NS%mat-sys-on-primary));
}
@media (forced-colors: active) {
  .mdc-checkbox__mixedmark {
    margin: 0 1px;
  }
}

.mdc-checkbox--disabled .mdc-checkbox__mixedmark, .mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox__mixedmark {
  border-color: var(--%NS%mat-checkbox-disabled-selected-checkmark-color, var(--%NS%mat-sys-surface));
}
@media (forced-colors: active) {
  .mdc-checkbox--disabled .mdc-checkbox__mixedmark, .mdc-checkbox--disabled.mat-mdc-checkbox-disabled-interactive .mdc-checkbox__mixedmark {
    border-color: GrayText;
  }
}

.mdc-checkbox--anim-unchecked-checked .mdc-checkbox__background,
.mdc-checkbox--anim-unchecked-indeterminate .mdc-checkbox__background,
.mdc-checkbox--anim-checked-unchecked .mdc-checkbox__background,
.mdc-checkbox--anim-indeterminate-unchecked .mdc-checkbox__background {
  animation-duration: 180ms;
  animation-timing-function: linear;
}

.mdc-checkbox--anim-unchecked-checked .mdc-checkbox__checkmark-path {
  animation: mdc-checkbox-unchecked-checked-checkmark-path 180ms linear;
  transition: none;
}

.mdc-checkbox--anim-unchecked-indeterminate .mdc-checkbox__mixedmark {
  animation: mdc-checkbox-unchecked-indeterminate-mixedmark 90ms linear;
  transition: none;
}

.mdc-checkbox--anim-checked-unchecked .mdc-checkbox__checkmark-path {
  animation: mdc-checkbox-checked-unchecked-checkmark-path 90ms linear;
  transition: none;
}

.mdc-checkbox--anim-checked-indeterminate .mdc-checkbox__checkmark {
  animation: mdc-checkbox-checked-indeterminate-checkmark 90ms linear;
  transition: none;
}
.mdc-checkbox--anim-checked-indeterminate .mdc-checkbox__mixedmark {
  animation: mdc-checkbox-checked-indeterminate-mixedmark 90ms linear;
  transition: none;
}

.mdc-checkbox--anim-indeterminate-checked .mdc-checkbox__checkmark {
  animation: mdc-checkbox-indeterminate-checked-checkmark 500ms linear;
  transition: none;
}
.mdc-checkbox--anim-indeterminate-checked .mdc-checkbox__mixedmark {
  animation: mdc-checkbox-indeterminate-checked-mixedmark 500ms linear;
  transition: none;
}

.mdc-checkbox--anim-indeterminate-unchecked .mdc-checkbox__mixedmark {
  animation: mdc-checkbox-indeterminate-unchecked-mixedmark 300ms linear;
  transition: none;
}

.mdc-checkbox__native-control:checked ~ .mdc-checkbox__background,
.mdc-checkbox__native-control:indeterminate ~ .mdc-checkbox__background {
  transition: border-color 90ms cubic-bezier(0, 0, 0.2, 1), background-color 90ms cubic-bezier(0, 0, 0.2, 1);
}
.mdc-checkbox__native-control:checked ~ .mdc-checkbox__background > .mdc-checkbox__checkmark > .mdc-checkbox__checkmark-path,
.mdc-checkbox__native-control:indeterminate ~ .mdc-checkbox__background > .mdc-checkbox__checkmark > .mdc-checkbox__checkmark-path {
  stroke-dashoffset: 0;
}

.mdc-checkbox__native-control:checked ~ .mdc-checkbox__background > .mdc-checkbox__checkmark {
  transition: opacity 180ms cubic-bezier(0, 0, 0.2, 1), transform 180ms cubic-bezier(0, 0, 0.2, 1);
  opacity: 1;
}
.mdc-checkbox__native-control:checked ~ .mdc-checkbox__background > .mdc-checkbox__mixedmark {
  transform: scaleX(1) rotate(-45deg);
}

.mdc-checkbox__native-control:indeterminate ~ .mdc-checkbox__background > .mdc-checkbox__checkmark {
  transform: rotate(45deg);
  opacity: 0;
  transition: opacity 90ms cubic-bezier(0.4, 0, 0.6, 1), transform 90ms cubic-bezier(0.4, 0, 0.6, 1);
}
.mdc-checkbox__native-control:indeterminate ~ .mdc-checkbox__background > .mdc-checkbox__mixedmark {
  transform: scaleX(1) rotate(0deg);
  opacity: 1;
}

@keyframes mdc-checkbox-unchecked-checked-checkmark-path {
  0%, 50% {
    stroke-dashoffset: 29.7833385;
  }
  50% {
    animation-timing-function: cubic-bezier(0, 0, 0.2, 1);
  }
  100% {
    stroke-dashoffset: 0;
  }
}
@keyframes mdc-checkbox-unchecked-indeterminate-mixedmark {
  0%, 68.2% {
    transform: scaleX(0);
  }
  68.2% {
    animation-timing-function: cubic-bezier(0, 0, 0, 1);
  }
  100% {
    transform: scaleX(1);
  }
}
@keyframes mdc-checkbox-checked-unchecked-checkmark-path {
  from {
    animation-timing-function: cubic-bezier(0.4, 0, 1, 1);
    opacity: 1;
    stroke-dashoffset: 0;
  }
  to {
    opacity: 0;
    stroke-dashoffset: -29.7833385;
  }
}
@keyframes mdc-checkbox-checked-indeterminate-checkmark {
  from {
    animation-timing-function: cubic-bezier(0, 0, 0.2, 1);
    transform: rotate(0deg);
    opacity: 1;
  }
  to {
    transform: rotate(45deg);
    opacity: 0;
  }
}
@keyframes mdc-checkbox-indeterminate-checked-checkmark {
  from {
    animation-timing-function: cubic-bezier(0.14, 0, 0, 1);
    transform: rotate(45deg);
    opacity: 0;
  }
  to {
    transform: rotate(360deg);
    opacity: 1;
  }
}
@keyframes mdc-checkbox-checked-indeterminate-mixedmark {
  from {
    animation-timing-function: cubic-bezier(0, 0, 0.2, 1);
    transform: rotate(-45deg);
    opacity: 0;
  }
  to {
    transform: rotate(0deg);
    opacity: 1;
  }
}
@keyframes mdc-checkbox-indeterminate-checked-mixedmark {
  from {
    animation-timing-function: cubic-bezier(0.14, 0, 0, 1);
    transform: rotate(0deg);
    opacity: 1;
  }
  to {
    transform: rotate(315deg);
    opacity: 0;
  }
}
@keyframes mdc-checkbox-indeterminate-unchecked-mixedmark {
  0% {
    animation-timing-function: linear;
    transform: scaleX(1);
    opacity: 1;
  }
  32.8%, 100% {
    transform: scaleX(0);
    opacity: 0;
  }
}
.mat-mdc-checkbox {
  display: inline-block;
  position: relative;
  -webkit-tap-highlight-color: transparent;
}
.mat-mdc-checkbox._mat-animation-noopable > .mat-internal-form-field > .mdc-checkbox > .mat-mdc-checkbox-touch-target,
.mat-mdc-checkbox._mat-animation-noopable > .mat-internal-form-field > .mdc-checkbox > .mdc-checkbox__native-control,
.mat-mdc-checkbox._mat-animation-noopable > .mat-internal-form-field > .mdc-checkbox > .mdc-checkbox__ripple,
.mat-mdc-checkbox._mat-animation-noopable > .mat-internal-form-field > .mdc-checkbox > .mat-mdc-checkbox-ripple::before,
.mat-mdc-checkbox._mat-animation-noopable > .mat-internal-form-field > .mdc-checkbox > .mdc-checkbox__background,
.mat-mdc-checkbox._mat-animation-noopable > .mat-internal-form-field > .mdc-checkbox > .mdc-checkbox__background > .mdc-checkbox__checkmark,
.mat-mdc-checkbox._mat-animation-noopable > .mat-internal-form-field > .mdc-checkbox > .mdc-checkbox__background > .mdc-checkbox__checkmark > .mdc-checkbox__checkmark-path,
.mat-mdc-checkbox._mat-animation-noopable > .mat-internal-form-field > .mdc-checkbox > .mdc-checkbox__background > .mdc-checkbox__mixedmark {
  transition: none !important;
  animation: none !important;
}
.mat-mdc-checkbox label {
  cursor: pointer;
}
.mat-mdc-checkbox .mat-internal-form-field {
  color: var(--%NS%mat-checkbox-label-text-color, var(--%NS%mat-sys-on-surface));
  font-family: var(--%NS%mat-checkbox-label-text-font, var(--%NS%mat-sys-body-medium-font));
  line-height: var(--%NS%mat-checkbox-label-text-line-height, var(--%NS%mat-sys-body-medium-line-height));
  font-size: var(--%NS%mat-checkbox-label-text-size, var(--%NS%mat-sys-body-medium-size));
  letter-spacing: var(--%NS%mat-checkbox-label-text-tracking, var(--%NS%mat-sys-body-medium-tracking));
  font-weight: var(--%NS%mat-checkbox-label-text-weight, var(--%NS%mat-sys-body-medium-weight));
}
.mat-mdc-checkbox.mat-mdc-checkbox-disabled.mat-mdc-checkbox-disabled-interactive {
  pointer-events: auto;
}
.mat-mdc-checkbox.mat-mdc-checkbox-disabled.mat-mdc-checkbox-disabled-interactive input {
  cursor: default;
}
.mat-mdc-checkbox.mat-mdc-checkbox-disabled label {
  cursor: default;
}
.mat-mdc-checkbox.mat-mdc-checkbox-disabled .mat-internal-form-field-label {
  color: var(--%NS%mat-checkbox-disabled-label-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
@media (forced-colors: active) {
  .mat-mdc-checkbox.mat-mdc-checkbox-disabled .mat-internal-form-field-label {
    color: GrayText;
  }
}
.mat-mdc-checkbox .mat-internal-form-field-label:empty {
  display: none;
}
.mat-mdc-checkbox .mdc-checkbox__ripple {
  opacity: 0;
}

.mat-mdc-checkbox .mat-mdc-checkbox-ripple,
.mdc-checkbox__ripple {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.mat-mdc-checkbox .mat-mdc-checkbox-ripple:not(:empty),
.mdc-checkbox__ripple:not(:empty) {
  transform: translateZ(0);
}

.mat-mdc-checkbox-ripple .mat-ripple-element {
  opacity: 0.1;
}

.mat-mdc-checkbox-touch-target {
  position: absolute;
  top: 50%;
  left: 50%;
  height: var(--%NS%mat-checkbox-touch-target-size, 48px);
  width: var(--%NS%mat-checkbox-touch-target-size, 48px);
  transform: translate(-50%, -50%);
  display: var(--%NS%mat-checkbox-touch-target-display, block);
}

.mat-mdc-checkbox .mat-mdc-checkbox-ripple::before {
  border-radius: 50%;
}

.mdc-checkbox__native-control:focus-visible ~ .mat-focus-indicator::before {
  content: "";
}
`],encapsulation:2})})()}return t})();var tm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[jc,we]})}return t})();var di=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[Du,Ti,we]})}return t})();var MO=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],hostAttrs:[`cdk-text-field-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`textarea.cdk-textarea-autosize {
  resize: none;
}

textarea.cdk-textarea-autosize-measuring {
  padding: 2px 0 !important;
  box-sizing: content-box !important;
  height: auto !important;
  overflow: hidden !important;
}

textarea.cdk-textarea-autosize-measuring-firefox {
  padding: 2px 0 !important;
  box-sizing: content-box !important;
  height: 0 !important;
}

@keyframes cdk-text-field-autofill-start { /*!*/ }
@keyframes cdk-text-field-autofill-end { /*!*/ }
.cdk-text-field-autofill-monitored:-webkit-autofill {
  animation: cdk-text-field-autofill-start 0s 1ms;
}

.cdk-text-field-autofill-monitored:not(:-webkit-autofill) {
  animation: cdk-text-field-autofill-end 0s 1ms;
}
`],encapsulation:2})}return t})();var TO={passive:!0};var nw=(()=>{class t{_platform=f(Oe);_ngZone=f(X);_renderer=f(Tt).createRenderer(null,null);_styleLoader=f(Je);_monitoredElements=new Map;monitor(e){if(!this._platform.isBrowser)return Ri;this._styleLoader.load(MO);let i=ln(e),a=this._monitoredElements.get(i);if(a)return a.subject;let o=new L,r=`cdk-text-field-autofilled`,s=h=>{h.animationName===`cdk-text-field-autofill-start`&&!i.classList.contains(r)?(i.classList.add(r),this._ngZone.run(()=>o.next({target:h.target,isAutofilled:!0}))):h.animationName===`cdk-text-field-autofill-end`&&i.classList.contains(r)&&(i.classList.remove(r),this._ngZone.run(()=>o.next({target:h.target,isAutofilled:!1})))},c=this._ngZone.runOutsideAngular(()=>(i.classList.add(`cdk-text-field-autofill-monitored`),this._renderer.listen(i,`animationstart`,s,TO)));return this._monitoredElements.set(i,{subject:o,unlisten:c}),o}stopMonitoring(e){let i=ln(e),a=this._monitoredElements.get(i);a&&(a.unlisten(),a.subject.complete(),i.classList.remove(`cdk-text-field-autofill-monitored`),i.classList.remove(`cdk-text-field-autofilled`),this._monitoredElements.delete(i))}ngOnDestroy(){this._monitoredElements.forEach((e,i)=>this.stopMonitoring(i))}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var iw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({})}return t})();var aw=new k(`MAT_INPUT_VALUE_ACCESSOR`);var NO=[`button`,`checkbox`,`file`,`hidden`,`image`,`radio`,`range`,`reset`,`submit`];var AO=new k(`MAT_INPUT_CONFIG`);var Qr=(()=>{class t{_elementRef=f(W);_platform=f(Oe);ngControl=f(Mi,{optional:!0,self:!0});_autofillMonitor=f(nw);_ngZone=f(X);_formField=f(Vc,{optional:!0});_renderer=f(Re);_uid=f(Be).getId(`mat-input-`);_previousNativeValue;_inputValueAccessor;_signalBasedValueAccessor;_previousPlaceholder=null;_errorStateTracker;_config=f(AO,{optional:!0});_cleanupIosKeyup;_cleanupWebkitWheel;_isServer=!1;_isNativeSelect=!1;_isTextarea=!1;_isInFormField=!1;focused=!1;stateChanges=new L;controlType=`mat-input`;autofilled=!1;get disabled(){return this._disabled}set disabled(e){this._disabled=ta(e),this.focused&&(this.focused=!1,this.stateChanges.next())}_disabled=!1;get id(){return this._id}set id(e){this._id=e||this._uid}_id;placeholder;name;get required(){return this._required??this.ngControl?.control?.hasValidator(Po.required)??!1}set required(e){this._required=ta(e)}_required;get type(){return this._type}set type(e){this._type=e||`text`,this._validateType(),!this._isTextarea&&jg().has(this._type)&&(this._elementRef.nativeElement.type=this._type)}_type=`text`;get errorStateMatcher(){return this._errorStateTracker.matcher}set errorStateMatcher(e){this._errorStateTracker.matcher=e}userAriaDescribedBy;get value(){return this._signalBasedValueAccessor?this._signalBasedValueAccessor.value():this._inputValueAccessor.value}set value(e){e!==this.value&&(this._signalBasedValueAccessor?this._signalBasedValueAccessor.value.set(e):this._inputValueAccessor.value=e,this.stateChanges.next())}get readonly(){return this._readonly}set readonly(e){this._readonly=ta(e)}_readonly=!1;disabledInteractive;get errorState(){return this._errorStateTracker.errorState}set errorState(e){this._errorStateTracker.errorState=e}_neverEmptyInputTypes=[`date`,`datetime`,`datetime-local`,`month`,`time`,`week`].filter(e=>jg().has(e));constructor(){let e=f(Rc,{optional:!0}),i=f(Pc,{optional:!0}),a=f(Yr),o=f(aw,{optional:!0,self:!0}),r=f(Qu,{optional:!0,self:!0}),s=this._elementRef.nativeElement,c=s.nodeName.toLowerCase();o?sn(o.value)?this._signalBasedValueAccessor=o:this._inputValueAccessor=o:this._inputValueAccessor=s,this._previousNativeValue=this.value,this.id=this.id,this._platform.IOS&&this._ngZone.runOutsideAngular(()=>{this._cleanupIosKeyup=this._renderer.listen(s,`keyup`,this._iOSKeyupListener)}),this._errorStateTracker=new Kr(a,r||this.ngControl,i,e,this.stateChanges),this._isServer=!this._platform.isBrowser,this._isNativeSelect=c===`select`,this._isTextarea=c===`textarea`,this._isInFormField=!!this._formField,this.disabledInteractive=this._config?.disabledInteractive||!1,this._isNativeSelect&&(this.controlType=s.multiple?`mat-native-select-multiple`:`mat-native-select`),this._signalBasedValueAccessor&&Lt(()=>{this._signalBasedValueAccessor.value(),this.stateChanges.next()})}ngAfterViewInit(){this._platform.isBrowser&&this._autofillMonitor.monitor(this._elementRef.nativeElement).subscribe(e=>{this.autofilled=e.isAutofilled,this.stateChanges.next()})}ngOnChanges(){this.stateChanges.next()}ngOnDestroy(){this.stateChanges.complete(),this._platform.isBrowser&&this._autofillMonitor.stopMonitoring(this._elementRef.nativeElement),this._cleanupIosKeyup?.(),this._cleanupWebkitWheel?.()}ngDoCheck(){this.ngControl&&(this.updateErrorState(),this.ngControl.disabled!==null&&this.ngControl.disabled!==this.disabled&&(this.disabled=this.ngControl.disabled,this.stateChanges.next())),this._dirtyCheckNativeValue(),this._dirtyCheckPlaceholder()}focus(e){this._elementRef.nativeElement.focus(e)}updateErrorState(){this._errorStateTracker.updateErrorState()}_focusChanged(e){if(e!==this.focused){if(!this._isNativeSelect&&e&&this.disabled&&this.disabledInteractive){let i=this._elementRef.nativeElement;i.type===`number`?(i.type=`text`,i.setSelectionRange(0,0),i.type=`number`):i.setSelectionRange(0,0)}this.focused=e,this.stateChanges.next()}}_onInput(){}_dirtyCheckNativeValue(){let e=this._elementRef.nativeElement.value;this._previousNativeValue!==e&&(this._previousNativeValue=e,this.stateChanges.next())}_dirtyCheckPlaceholder(){let e=this._getPlaceholder();if(e!==this._previousPlaceholder){let i=this._elementRef.nativeElement;this._previousPlaceholder=e,e?i.setAttribute(`placeholder`,e):i.removeAttribute(`placeholder`)}}_getPlaceholder(){return this.placeholder||null}_validateType(){NO.indexOf(this._type)}_isNeverEmpty(){return this._neverEmptyInputTypes.indexOf(this._type)>-1}_isBadInput(){let e=this._elementRef.nativeElement.validity;return e&&e.badInput}get empty(){return!this._isNeverEmpty()&&!this._elementRef.nativeElement.value&&!this._isBadInput()&&!this.autofilled}get shouldLabelFloat(){if(this._isNativeSelect){let e=this._elementRef.nativeElement,i=e.options[0];return this.focused||e.multiple||!this.empty||!!(e.selectedIndex>-1&&i&&i.label)}else return this.focused&&!this.disabled||!this.empty}get describedByIds(){return this._elementRef.nativeElement.getAttribute(`aria-describedby`)?.split(` `)||[]}setDescribedByIds(e){let i=this._elementRef.nativeElement;e.length?i.setAttribute(`aria-describedby`,e.join(` `)):i.removeAttribute(`aria-describedby`)}onContainerClick(){this.focused||this.focus()}_isInlineSelect(){let e=this._elementRef.nativeElement;return this._isNativeSelect&&(e.multiple||e.size>1)}_iOSKeyupListener=e=>{let i=e.target;!i.value&&i.selectionStart===0&&i.selectionEnd===0&&(i.setSelectionRange(1,1),i.setSelectionRange(0,0))};_getReadonlyAttribute(){return this._isNativeSelect?null:this.readonly||this.disabled&&this.disabledInteractive?`true`:null}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[`input`,`matInput`,``],[`textarea`,`matInput`,``],[`select`,`matNativeControl`,``],[`input`,`matNativeControl`,``],[`textarea`,`matNativeControl`,``]],hostAttrs:[1,`mat-mdc-input-element`],hostVars:21,hostBindings:function(i,a){i&1&&C(`focus`,function(){return a._focusChanged(!0)})(`blur`,function(){return a._focusChanged(!1)})(`input`,function(){return a._onInput()}),i&2&&(qt(`id`,a.id)(`disabled`,a.disabled&&!a.disabledInteractive)(`required`,a.required),te(`name`,a.name||null)(`readonly`,a._getReadonlyAttribute())(`aria-disabled`,a.disabled&&a.disabledInteractive?`true`:null)(`aria-invalid`,a.empty&&a.required?null:a.errorState)(`aria-required`,a.required)(`id`,a.id),Y(`mat-input-server`,a._isServer)(`mat-mdc-form-field-textarea-control`,a._isInFormField&&a._isTextarea)(`mat-mdc-form-field-input-control`,a._isInFormField)(`mat-mdc-input-disabled-interactive`,a.disabledInteractive)(`mdc-text-field__input`,a._isInFormField)(`mat-mdc-native-select-inline`,a._isInlineSelect()))},inputs:{disabled:`disabled`,id:`id`,placeholder:`placeholder`,name:`name`,required:`required`,type:`type`,errorStateMatcher:`errorStateMatcher`,userAriaDescribedBy:[0,`aria-describedby`,`userAriaDescribedBy`],value:`value`,readonly:`readonly`,disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V]},exportAs:[`matInput`],features:[Ge([{provide:Lc,useExisting:t}]),vt]})}return t})();var Xr=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[di,di,iw,we]})}return t})();var zc=class{_attachedHost=null;attach(n){return this._attachedHost=n,n.attach(this)}detach(){let n=this._attachedHost;n!=null&&(this._attachedHost=null,n.detach())}get isAttached(){return this._attachedHost!=null}setAttachedHost(n){this._attachedHost=n}};var Na=class extends zc{component;viewContainerRef;injector;projectableNodes;bindings;directives;constructor(n,e,i,a,o,r){super(),this.component=n,this.viewContainerRef=e,this.injector=i,this.projectableNodes=a,this.bindings=o||null,this.directives=r||null}};var Hn=class extends zc{templateRef;viewContainerRef;context;injector;constructor(n,e,i,a){super(),this.templateRef=n,this.viewContainerRef=e,this.context=i,this.injector=a}get origin(){return this.templateRef.elementRef}attach(n,e=this.context){return this.context=e,super.attach(n)}detach(){return this.context=void 0,super.detach()}};var db=class extends zc{element;constructor(n){super(),this.element=n instanceof W?n.nativeElement:n}};var Zr=class{_attachedPortal=null;_disposeFn=null;_isDisposed=!1;hasAttached(){return!!this._attachedPortal}attach(n){if(n instanceof Na)return this._attachedPortal=n,this.attachComponentPortal(n);if(n instanceof Hn)return this._attachedPortal=n,this.attachTemplatePortal(n);if(this.attachDomPortal&&n instanceof db)return this._attachedPortal=n,this.attachDomPortal(n)}attachDomPortal=null;detach(){this._attachedPortal&&(this._attachedPortal.setAttachedHost(null),this._attachedPortal=null),this._invokeDisposeFn()}dispose(){this.hasAttached()&&this.detach(),this._invokeDisposeFn(),this._isDisposed=!0}setDisposeFn(n){this._disposeFn=n}_invokeDisposeFn(){this._disposeFn&&(this._disposeFn(),this._disposeFn=null)}};var Hc=class extends Zr{outletElement;_appRef;_defaultInjector;constructor(n,e,i){super(),this.outletElement=n,this._appRef=e,this._defaultInjector=i}attachComponentPortal(n){let e;if(n.viewContainerRef){let i=n.injector||n.viewContainerRef.injector,a=i.get(ni,null,{optional:!0})||void 0;e=n.viewContainerRef.createComponent(n.component,{index:n.viewContainerRef.length,injector:i,ngModuleRef:a,projectableNodes:n.projectableNodes||void 0,bindings:n.bindings||void 0,directives:n.directives||void 0}),this.setDisposeFn(()=>e.destroy())}else{let i=this._appRef,a=n.injector||this._defaultInjector||se.NULL,o=a.get(kt,i.injector);e=tu(n.component,{elementInjector:a,environmentInjector:o,projectableNodes:n.projectableNodes||void 0,bindings:n.bindings||void 0,directives:n.directives||void 0}),i.attachView(e.hostView),this.setDisposeFn(()=>{i.viewCount>0&&i.detachView(e.hostView),e.destroy()})}return this.outletElement.appendChild(this._getComponentRootNode(e)),this._attachedPortal=n,e}attachTemplatePortal(n){let e=n.viewContainerRef,i=e.createEmbeddedView(n.templateRef,n.context,{injector:n.injector});return i.rootNodes.forEach(a=>this.outletElement.appendChild(a)),i.detectChanges(),this.setDisposeFn(()=>{let a=e.indexOf(i);a!==-1&&e.remove(a)}),this._attachedPortal=n,i}attachDomPortal=n=>{let e=n.element;e.parentNode;let i=this.outletElement.ownerDocument.createComment(`dom-portal`);e.parentNode.insertBefore(i,e),this.outletElement.appendChild(e),this._attachedPortal=n,super.setDisposeFn(()=>{i.parentNode&&i.parentNode.replaceChild(e,i)})};dispose(){super.dispose(),this.outletElement.remove()}_getComponentRootNode(n){return n.hostView.rootNodes[0]}};var ow=(()=>{class t extends Hn{constructor(){let e=f(Vt),i=f(Bt);super(e,i)}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`cdkPortal`,``]],exportAs:[`cdkPortal`],features:[Ve]})}return t})();var Aa=(()=>{class t extends Zr{_moduleRef=f(ni,{optional:!0});_document=f(de);_viewContainerRef=f(Bt);_isInitialized=!1;_attachedRef=null;get portal(){return this._attachedPortal}set portal(e){this.hasAttached()&&!e&&!this._isInitialized||(this.hasAttached()&&super.detach(),e&&super.attach(e),this._attachedPortal=e||null)}attached=new ee;get attachedRef(){return this._attachedRef}ngOnInit(){this._isInitialized=!0}ngOnDestroy(){super.dispose(),this._attachedRef=this._attachedPortal=null}attachComponentPortal(e){e.setAttachedHost(this);let i=e.viewContainerRef!=null?e.viewContainerRef:this._viewContainerRef,a=i.createComponent(e.component,{index:i.length,injector:e.injector||i.injector,projectableNodes:e.projectableNodes||void 0,ngModuleRef:this._moduleRef||void 0,bindings:e.bindings||void 0,directives:e.directives||void 0});return i!==this._viewContainerRef&&this._getRootNode().appendChild(a.hostView.rootNodes[0]),super.setDisposeFn(()=>a.destroy()),this._attachedPortal=e,this._attachedRef=a,this.attached.emit(a),a}attachTemplatePortal(e){e.setAttachedHost(this);let i=this._viewContainerRef.createEmbeddedView(e.templateRef,e.context,{injector:e.injector});return super.setDisposeFn(()=>this._viewContainerRef.clear()),this._attachedPortal=e,this._attachedRef=i,this.attached.emit(i),i}attachDomPortal=e=>{let i=e.element;i.parentNode;let a=this._document.createComment(`dom-portal`);e.setAttachedHost(this),i.parentNode.insertBefore(a,i),this._getRootNode().appendChild(i),this._attachedPortal=e,super.setDisposeFn(()=>{a.parentNode&&a.parentNode.replaceChild(i,a)})};_getRootNode(){let e=this._viewContainerRef.element.nativeElement;return e.nodeType===e.ELEMENT_NODE?e:e.parentNode}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=K({type:t,selectors:[[``,`cdkPortalOutlet`,``]],inputs:{portal:[0,`cdkPortalOutlet`,`portal`]},outputs:{attached:`attached`},exportAs:[`cdkPortalOutlet`],features:[Ve]})}return t})();var Lo=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({})}return t})();var OO=20;var Oa=(()=>{class t{_ngZone=f(X);_platform=f(Oe);_renderer=f(Tt).createRenderer(null,null);_cleanupGlobalListener;_scrolled=new L;_scrolledCount=0;scrollContainers=new Map;register(e){this.scrollContainers.has(e)||this.scrollContainers.set(e,e.elementScrolled().subscribe(()=>this._scrolled.next(e)))}deregister(e){let i=this.scrollContainers.get(e);i&&(i.unsubscribe(),this.scrollContainers.delete(e))}scrolled(e=OO){return this._platform.isBrowser?new Ce(i=>{this._cleanupGlobalListener||(this._cleanupGlobalListener=this._ngZone.runOutsideAngular(()=>this._renderer.listen(`document`,`scroll`,()=>this._scrolled.next())));let a=e>0?this._scrolled.pipe(Ol(e)).subscribe(i):this._scrolled.subscribe(i);return this._scrolledCount++,()=>{a.unsubscribe(),this._scrolledCount--,this._scrolledCount||(this._cleanupGlobalListener?.(),this._cleanupGlobalListener=void 0)}}):Mn()}ngOnDestroy(){this._cleanupGlobalListener?.(),this._cleanupGlobalListener=void 0,this.scrollContainers.forEach((e,i)=>this.deregister(i)),this._scrolled.complete()}ancestorScrolled(e,i){let a=this.getAncestorScrollContainers(e);return this.scrolled(i).pipe(Qe(o=>!o||a.indexOf(o)>-1))}getAncestorScrollContainers(e){let i=[];return this.scrollContainers.forEach((a,o)=>{this._targetContainsElement(o,e)&&i.push(o)}),i}_targetContainsElement(e,i){let a=ln(i),o=e.getElementRef().nativeElement;do if(a==o)return!0;while(a=a.parentElement);return!1}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var Uc=(()=>{class t{elementRef=f(W);scrollDispatcher=f(Oa);ngZone=f(X);dir=f(gt,{optional:!0});_scrollElement=this.elementRef.nativeElement;_destroyed=new L;_renderer=f(Re);_cleanupScroll;_elementScrolled=new L;ngOnInit(){this._cleanupScroll=this.ngZone.runOutsideAngular(()=>this._renderer.listen(this._scrollElement,`scroll`,e=>this._elementScrolled.next(e))),this.scrollDispatcher.register(this)}ngOnDestroy(){this._cleanupScroll?.(),this._elementScrolled.complete(),this.scrollDispatcher.deregister(this),this._destroyed.next(),this._destroyed.complete()}elementScrolled(){return this._elementScrolled}getElementRef(){return this.elementRef}scrollTo(e){let i=this.elementRef.nativeElement,a=this.dir&&this.dir.value==`rtl`;e.left??=a?e.end:e.start,e.right??=a?e.start:e.end,e.bottom!=null&&(e.top=i.scrollHeight-i.clientHeight-e.bottom),a&&Br()!=si.NORMAL?(e.left!=null&&(e.right=i.scrollWidth-i.clientWidth-e.left),Br()==si.INVERTED?e.left=e.right:Br()==si.NEGATED&&(e.left=e.right?-e.right:e.right)):e.right!=null&&(e.left=i.scrollWidth-i.clientWidth-e.right),this._applyScrollToOptions(e)}_applyScrollToOptions(e){let i=this.elementRef.nativeElement;Ou()?i.scrollTo(e):(e.top!=null&&(i.scrollTop=e.top),e.left!=null&&(i.scrollLeft=e.left))}measureScrollOffset(e){let i=`left`,a=`right`,o=this.elementRef.nativeElement;if(e==`top`)return o.scrollTop;if(e==`bottom`)return o.scrollHeight-o.clientHeight-o.scrollTop;let r=this.dir&&this.dir.value==`rtl`;return e==`start`?e=r?a:i:e==`end`&&(e=r?i:a),r&&Br()==si.INVERTED?e==i?o.scrollWidth-o.clientWidth-o.scrollLeft:o.scrollLeft:r&&Br()==si.NEGATED?e==i?o.scrollLeft+o.scrollWidth-o.clientWidth:-o.scrollLeft:e==i?o.scrollLeft:o.scrollWidth-o.clientWidth-o.scrollLeft}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`cdk-scrollable`,``],[``,`cdkScrollable`,``]]})}return t})();var RO=20;var ui=(()=>{class t{_platform=f(Oe);_listeners;_viewportSize=null;_change=new L;_document=f(de);constructor(){let e=f(X),i=f(Tt).createRenderer(null,null);e.runOutsideAngular(()=>{if(this._platform.isBrowser){let a=o=>this._change.next(o);this._listeners=[i.listen(`window`,`resize`,a),i.listen(`window`,`orientationchange`,a)]}this.change().subscribe(()=>this._viewportSize=null)})}ngOnDestroy(){this._listeners?.forEach(e=>e()),this._change.complete()}getViewportSize(){this._viewportSize||this._updateViewportSize();let e={width:this._viewportSize.width,height:this._viewportSize.height};return this._platform.isBrowser||(this._viewportSize=null),e}getViewportRect(){let e=this.getViewportScrollPosition(),{width:i,height:a}=this.getViewportSize();return{top:e.top,left:e.left,bottom:e.top+a,right:e.left+i,height:a,width:i}}getViewportScrollPosition(){if(!this._platform.isBrowser)return{top:0,left:0};let e=this._document,i=this._getWindow(),a=e.documentElement,o=a.getBoundingClientRect();return{top:-o.top||e.body?.scrollTop||i.scrollY||a.scrollTop||0,left:-o.left||e.body?.scrollLeft||i.scrollX||a.scrollLeft||0}}change(e=RO){return e>0?this._change.pipe(Ol(e)):this._change}_getWindow(){return this._document.defaultView||window}_updateViewportSize(){let e=this._getWindow();this._viewportSize=this._platform.isBrowser?{width:e.innerWidth,height:e.innerHeight}:{width:0,height:0}}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var ia=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({})}return t})();var ub=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[we,ia,we,ia]})}return t})();var rw=Ou();function ts(t){return new nm(t.get(ui),t.get(de))}var nm=class{_viewportRuler;_previousHTMLStyles={top:``,left:``};_previousScrollPosition;_isEnabled=!1;_document;constructor(n,e){this._viewportRuler=n,this._document=e}attach(){}enable(){if(this._canBeEnabled()){let n=this._document.documentElement;this._previousScrollPosition=this._viewportRuler.getViewportScrollPosition(),this._previousHTMLStyles.left=n.style.left||``,this._previousHTMLStyles.top=n.style.top||``,n.style.left=Dt(-this._previousScrollPosition.left),n.style.top=Dt(-this._previousScrollPosition.top),n.classList.add(`cdk-global-scrollblock`),this._isEnabled=!0}}disable(){if(this._isEnabled){let n=this._document.documentElement,e=this._document.body,i=n.style,a=e.style,o=i.scrollBehavior||``,r=a.scrollBehavior||``;this._isEnabled=!1,i.left=this._previousHTMLStyles.left,i.top=this._previousHTMLStyles.top,n.classList.remove(`cdk-global-scrollblock`),rw&&(i.scrollBehavior=a.scrollBehavior=`auto`),window.scroll(this._previousScrollPosition.left,this._previousScrollPosition.top),rw&&(i.scrollBehavior=o,a.scrollBehavior=r)}}_canBeEnabled(){if(this._document.documentElement.classList.contains(`cdk-global-scrollblock`)||this._isEnabled)return!1;let e=this._document.documentElement,i=this._viewportRuler.getViewportSize();return e.scrollHeight>i.height||e.scrollWidth>i.width}};function pw(t,n){return new im(t.get(Oa),t.get(X),t.get(ui),n)}var im=class{_scrollDispatcher;_ngZone;_viewportRuler;_config;_scrollSubscription=null;_overlayRef;_initialScrollPosition;constructor(n,e,i,a){this._scrollDispatcher=n,this._ngZone=e,this._viewportRuler=i,this._config=a}attach(n){this._overlayRef,this._overlayRef=n}enable(){if(this._scrollSubscription)return;let n=this._scrollDispatcher.scrolled(0).pipe(Qe(e=>!e||!this._overlayRef.overlayElement.contains(e.getElementRef().nativeElement)));this._config&&this._config.threshold&&this._config.threshold>1?(this._initialScrollPosition=this._viewportRuler.getViewportScrollPosition().top,this._scrollSubscription=n.subscribe(()=>{let e=this._viewportRuler.getViewportScrollPosition().top;Math.abs(e-this._initialScrollPosition)>this._config.threshold?this._detach():this._overlayRef.updatePosition()})):this._scrollSubscription=n.subscribe(this._detach)}disable(){this._scrollSubscription&&(this._scrollSubscription.unsubscribe(),this._scrollSubscription=null)}detach(){this.disable(),this._overlayRef=null}_detach=()=>{this.disable(),this._overlayRef.hasAttached()&&this._ngZone.run(()=>this._overlayRef.detach())}};var $c=class{enable(){}disable(){}attach(){}};function pb(t,n){return n.some(e=>{let i=t.bottom<e.top,a=t.top>e.bottom,o=t.right<e.left,r=t.left>e.right;return i||a||o||r})}function sw(t,n){return n.some(e=>{let i=t.top<e.top,a=t.bottom>e.bottom,o=t.left<e.left,r=t.right>e.right;return i||a||o||r})}function ra(t,n){return new am(t.get(Oa),t.get(ui),t.get(X),n)}var am=class{_scrollDispatcher;_viewportRuler;_ngZone;_config;_scrollSubscription=null;_overlayRef;constructor(n,e,i,a){this._scrollDispatcher=n,this._viewportRuler=e,this._ngZone=i,this._config=a}attach(n){this._overlayRef,this._overlayRef=n}enable(){if(!this._scrollSubscription){let n=this._config?this._config.scrollThrottle:0;this._scrollSubscription=this._scrollDispatcher.scrolled(n).subscribe(()=>{if(this._overlayRef.updatePosition(),this._config&&this._config.autoClose){let e=this._overlayRef.overlayElement.getBoundingClientRect(),{width:i,height:a}=this._viewportRuler.getViewportSize();pb(e,[{width:i,height:a,bottom:a,right:i,top:0,left:0}])&&(this.disable(),this._ngZone.run(()=>this._overlayRef.detach()))}})}}disable(){this._scrollSubscription&&(this._scrollSubscription.unsubscribe(),this._scrollSubscription=null)}detach(){this.disable(),this._overlayRef=null}};var hw=(()=>{class t{_injector=f(se);noop=()=>new $c;close=e=>pw(this._injector,e);block=()=>ts(this._injector);reposition=e=>ra(this._injector,e);static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var aa=class{positionStrategy;scrollStrategy=new $c;panelClass=``;hasBackdrop=!1;backdropClass=`cdk-overlay-dark-backdrop`;disableAnimations;width;height;minWidth;minHeight;maxWidth;maxHeight;direction;disposeOnNavigation=!1;usePopover;eventPredicate;constructor(n){if(n){let e=Object.keys(n);for(let i of e)n[i]!==void 0&&(this[i]=n[i])}}};var om=class{connectionPair;scrollableViewProperties;constructor(n,e){this.connectionPair=n,this.scrollableViewProperties=e}};var fw=(()=>{class t{_attachedOverlays=[];_document=f(de);_isAttached=!1;ngOnDestroy(){this.detach()}add(e){this.remove(e),this._attachedOverlays.push(e)}remove(e){let i=this._attachedOverlays.indexOf(e);i>-1&&this._attachedOverlays.splice(i,1),this._attachedOverlays.length===0&&this.detach()}canReceiveEvent(e,i,a){return a.observers.length<1?!1:e.eventPredicate?e.eventPredicate(i):!0}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var gw=(()=>{class t extends fw{_ngZone=f(X);_renderer=f(Tt).createRenderer(null,null);_cleanupKeydown;add(e){super.add(e),this._isAttached||(this._ngZone.runOutsideAngular(()=>{this._cleanupKeydown=this._renderer.listen(`body`,`keydown`,this._keydownListener)}),this._isAttached=!0)}detach(){this._isAttached&&(this._cleanupKeydown?.(),this._isAttached=!1)}_keydownListener=e=>{let i=this._attachedOverlays;for(let a=i.length-1;a>-1;a--){let o=i[a];if(this.canReceiveEvent(o,e,o._keydownEvents)){this._ngZone.run(()=>o._keydownEvents.next(e));break}}};static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var bw=(()=>{class t extends fw{_platform=f(Oe);_ngZone=f(X);_renderer=f(Tt).createRenderer(null,null);_cursorOriginalValue;_cursorStyleIsSet=!1;_pointerDownEventTarget=null;_cleanups;add(e){if(super.add(e),!this._isAttached){let i=this._document.body,a={capture:!0},o=this._renderer;this._cleanups=this._ngZone.runOutsideAngular(()=>[o.listen(i,`pointerdown`,this._pointerDownListener,a),o.listen(i,`click`,this._clickListener,a),o.listen(i,`auxclick`,this._clickListener,a),o.listen(i,`contextmenu`,this._clickListener,a)]),this._platform.IOS&&!this._cursorStyleIsSet&&(this._cursorOriginalValue=i.style.cursor,i.style.cursor=`pointer`,this._cursorStyleIsSet=!0),this._isAttached=!0}}detach(){this._isAttached&&(this._cleanups?.forEach(e=>e()),this._cleanups=void 0,this._platform.IOS&&this._cursorStyleIsSet&&(this._document.body.style.cursor=this._cursorOriginalValue,this._cursorStyleIsSet=!1),this._isAttached=!1)}_pointerDownListener=e=>{this._pointerDownEventTarget=Yt(e)};_clickListener=e=>{let i=Yt(e),a=e.type===`click`&&this._pointerDownEventTarget?this._pointerDownEventTarget:i;this._pointerDownEventTarget=null;let o=this._attachedOverlays.slice();for(let r=o.length-1;r>-1;r--){let s=o[r],c=s._outsidePointerEvents;if(!(!s.hasAttached()||!this.canReceiveEvent(s,e,c))){if(cw(s.overlayElement,i)||cw(s.overlayElement,a))break;this._ngZone?this._ngZone.run(()=>c.next(e)):c.next(e)}}};static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();function cw(t,n){let e=typeof ShadowRoot<`u`&&ShadowRoot,i=n;for(;i;){if(i===t)return!0;i=e&&i instanceof ShadowRoot?i.host:i.parentNode}return!1}var vw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],hostAttrs:[`cdk-overlay-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`.cdk-overlay-container, .cdk-global-overlay-wrapper {
  pointer-events: none;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
}

.cdk-overlay-container {
  position: fixed;
}
@layer cdk-overlay {
  .cdk-overlay-container {
    z-index: 1000;
  }
}
.cdk-overlay-container:empty {
  display: none;
}

.cdk-global-overlay-wrapper {
  display: flex;
  position: absolute;
}
@layer cdk-overlay {
  .cdk-global-overlay-wrapper {
    z-index: 1000;
  }
}

.cdk-overlay-pane {
  position: absolute;
  pointer-events: auto;
  box-sizing: border-box;
  display: flex;
  max-width: 100%;
  max-height: 100%;
}
@layer cdk-overlay {
  .cdk-overlay-pane {
    z-index: 1000;
  }
}

.cdk-overlay-backdrop {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  pointer-events: auto;
  -webkit-tap-highlight-color: transparent;
  opacity: 0;
  touch-action: manipulation;
}
@layer cdk-overlay {
  .cdk-overlay-backdrop {
    z-index: 1000;
    transition: opacity 400ms cubic-bezier(0.25, 0.8, 0.25, 1);
  }
}
@media (prefers-reduced-motion) {
  .cdk-overlay-backdrop {
    transition-duration: 1ms;
  }
}

.cdk-overlay-backdrop-showing {
  opacity: 1;
}
@media (forced-colors: active) {
  .cdk-overlay-backdrop-showing {
    opacity: 0.6;
  }
}

@layer cdk-overlay {
  .cdk-overlay-dark-backdrop {
    background: rgba(0, 0, 0, 0.32);
  }
}

.cdk-overlay-transparent-backdrop {
  transition: visibility 1ms linear, opacity 1ms linear;
  visibility: hidden;
  opacity: 1;
}
.cdk-overlay-transparent-backdrop.cdk-overlay-backdrop-showing, .cdk-high-contrast-active .cdk-overlay-transparent-backdrop {
  opacity: 0;
  visibility: visible;
}

.cdk-overlay-backdrop-noop-animation {
  transition: none;
}

.cdk-overlay-connected-position-bounding-box {
  position: absolute;
  display: flex;
  flex-direction: column;
  min-width: 1px;
  min-height: 1px;
}
@layer cdk-overlay {
  .cdk-overlay-connected-position-bounding-box {
    z-index: 1000;
  }
}

.cdk-global-scrollblock {
  position: fixed;
  width: 100%;
  overflow-y: scroll;
}

.cdk-overlay-popover {
  background: none;
  border: none;
  padding: 0;
  outline: 0;
  overflow: visible;
  position: fixed;
  pointer-events: none;
  white-space: normal;
  color: inherit;
  text-decoration: none;
  width: 100%;
  height: 100%;
  inset: auto;
  top: 0;
  left: 0;
}
.cdk-overlay-popover::backdrop {
  display: none;
}
.cdk-overlay-popover .cdk-overlay-backdrop {
  position: fixed;
  z-index: auto;
}
`],encapsulation:2})}return t})();var cm=(()=>{class t{_platform=f(Oe);_containerElement;_document=f(de);_styleLoader=f(Je);ngOnDestroy(){this._containerElement?.remove()}getContainerElement(){return this._loadStyles(),this._containerElement||this._createContainer(),this._containerElement}_createContainer(){let e=`cdk-overlay-container`;if(this._platform.isBrowser||Bg()){let a=this._document.querySelectorAll(`.${e}[platform="server"], .${e}[platform="test"]`);for(let o=0;o<a.length;o++)a[o].remove()}let i=this._document.createElement(`div`);i.classList.add(e),Bg()?i.setAttribute(`platform`,`test`):this._platform.isBrowser||i.setAttribute(`platform`,`server`),this._document.body.appendChild(i),this._containerElement=i}_loadStyles(){this._styleLoader.load(vw)}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var hb=class{_renderer;_ngZone;element;_cleanupClick;_cleanupTransitionEnd;_fallbackTimeout;constructor(n,e,i,a){this._renderer=e,this._ngZone=i,this.element=n.createElement(`div`),this.element.classList.add(`cdk-overlay-backdrop`),this._cleanupClick=e.listen(this.element,`click`,a)}detach(){this._ngZone.runOutsideAngular(()=>{let n=this.element;clearTimeout(this._fallbackTimeout),this._cleanupTransitionEnd?.(),this._cleanupTransitionEnd=this._renderer.listen(n,`transitionend`,this.dispose),this._fallbackTimeout=setTimeout(this.dispose,500),n.style.pointerEvents=`none`,n.classList.remove(`cdk-overlay-backdrop-showing`)})}dispose=()=>{clearTimeout(this._fallbackTimeout),this._cleanupClick?.(),this._cleanupTransitionEnd?.(),this._cleanupClick=this._cleanupTransitionEnd=this._fallbackTimeout=void 0,this.element.remove()}};function fb(t){return t&&t.nodeType===1}var mb=T([]);var Jr=class{_portalOutlet;_host;_pane;_config;_ngZone;_keyboardDispatcher;_document;_location;_outsideClickDispatcher;_animationsDisabled;_injector;_renderer;_backdropClick=new L;_attachments=new L;_detachments=new L;_positionStrategy;_scrollStrategy;_locationChanges;_backdropRef=null;_detachContentMutationObserver;_detachContentAfterRenderRef;_disposed=!1;_previousHostParent;_keydownEvents=new L;_outsidePointerEvents=new L;_afterNextRenderRef;constructor(n,e,i,a,o,r,s,c,h,p=!1,g,v){this._portalOutlet=n,this._host=e,this._pane=i,this._config=a,this._ngZone=o,this._keyboardDispatcher=r,this._document=s,this._location=c,this._outsideClickDispatcher=h,this._animationsDisabled=p,this._injector=g,this._renderer=v,a.scrollStrategy&&(this._scrollStrategy=a.scrollStrategy,this._scrollStrategy.attach(this)),this._positionStrategy=a.positionStrategy}get overlayElement(){return this._pane}get backdropElement(){return this._backdropRef?.element||null}get hostElement(){return this._host}get eventPredicate(){return this._config?.eventPredicate||null}attach(n){if(this._disposed)return null;this._attachHost();let e=this._portalOutlet.attach(n);if(this._positionStrategy?.attach(this),this._updateStackingOrder(),this._updateElementSize(),this._updateElementDirection(),nt(()=>{mb.update(i=>i.includes(this)?i:[...i,this])}),this._scrollStrategy&&this._scrollStrategy.enable(),this._afterNextRenderRef?.destroy(),this._afterNextRenderRef=At(()=>{this.hasAttached()&&this.updatePosition()},{injector:this._injector}),this._togglePointerEvents(!0),this._config.hasBackdrop&&this._attachBackdrop(),this._config.panelClass&&this._toggleClasses(this._pane,this._config.panelClass,!0),this._attachments.next(),this._completeDetachContent(),this._keyboardDispatcher.add(this),this._config.disposeOnNavigation===!0||this._config.disposeOnNavigation===`pop-state`){let i=this._location.subscribe(()=>this.dispose());this._locationChanges=()=>i.unsubscribe()}else this._config.disposeOnNavigation===`url-change`&&(this._locationChanges=this._location.onUrlChange(()=>this.dispose()));return this._outsideClickDispatcher.add(this),typeof e?.onDestroy==`function`&&e.onDestroy(()=>{this.hasAttached()&&this._ngZone.runOutsideAngular(()=>Promise.resolve().then(()=>this.detach()))}),e}detach(){if(!this.hasAttached())return;this.detachBackdrop(),this._togglePointerEvents(!1),this._positionStrategy&&this._positionStrategy.detach&&this._positionStrategy.detach(),this._scrollStrategy&&this._scrollStrategy.disable();let n=this._portalOutlet.detach();return this._detachments.next(),this._completeDetachContent(),this._keyboardDispatcher.remove(this),this._detachContentWhenEmpty(),this._locationChanges?.(),this._outsideClickDispatcher.remove(this),nt(()=>{mb.update(e=>e.filter(i=>i!==this))}),n}dispose(){if(this._disposed)return;let n=this.hasAttached();this._positionStrategy&&this._positionStrategy.dispose(),this._disposeScrollStrategy(),this._backdropRef?.dispose(),this._locationChanges?.(),this._keyboardDispatcher.remove(this),this._portalOutlet.dispose(),this._attachments.complete(),this._backdropClick.complete(),this._keydownEvents.complete(),this._outsidePointerEvents.complete(),this._outsideClickDispatcher.remove(this),this._host?.remove(),this._afterNextRenderRef?.destroy(),this._previousHostParent=this._pane=this._host=this._backdropRef=null,n&&this._detachments.next(),this._detachments.complete(),this._completeDetachContent(),this._disposed=!0,nt(()=>{mb.update(e=>e.filter(i=>i!==this))})}hasAttached(){return this._portalOutlet.hasAttached()}backdropClick(){return this._backdropClick}attachments(){return this._attachments}detachments(){return this._detachments}keydownEvents(){return this._keydownEvents}outsidePointerEvents(){return this._outsidePointerEvents}getConfig(){return this._config}updatePosition(){this._positionStrategy&&this._positionStrategy.apply()}updatePositionStrategy(n){n!==this._positionStrategy&&(this._positionStrategy&&this._positionStrategy.dispose(),this._positionStrategy=n,this.hasAttached()&&(n.attach(this),this.updatePosition()))}updateSize(n){this._config=r(r({},this._config),n),this._updateElementSize()}setDirection(n){this._config=s(r({},this._config),{direction:n}),this._updateElementDirection()}addPanelClass(n){this._pane&&this._toggleClasses(this._pane,n,!0)}removePanelClass(n){this._pane&&this._toggleClasses(this._pane,n,!1)}getDirection(){let n=this._config.direction;return n?typeof n==`string`?n:n.value:`ltr`}updateScrollStrategy(n){n!==this._scrollStrategy&&(this._disposeScrollStrategy(),this._scrollStrategy=n,this.hasAttached()&&(n.attach(this),n.enable()))}_updateElementDirection(){this._host.setAttribute(`dir`,this.getDirection())}_updateElementSize(){if(!this._pane)return;let n=this._pane.style;n.width=Dt(this._config.width),n.height=Dt(this._config.height),n.minWidth=Dt(this._config.minWidth),n.minHeight=Dt(this._config.minHeight),n.maxWidth=Dt(this._config.maxWidth),n.maxHeight=Dt(this._config.maxHeight)}_togglePointerEvents(n){this._pane.style.pointerEvents=n?``:`none`}_attachHost(){if(!this._host.parentElement){let n=this._config.usePopover?this._positionStrategy?.getPopoverInsertionPoint?.():null;fb(n)?n.after(this._host):n?.type===`parent`?n.element.appendChild(this._host):this._previousHostParent?.appendChild(this._host)}if(this._config.usePopover)try{this._host.showPopover()}catch{}}_attachBackdrop(){let n=`cdk-overlay-backdrop-showing`;this._backdropRef?.dispose(),this._backdropRef=new hb(this._document,this._renderer,this._ngZone,e=>{this._backdropClick.next(e)}),this._animationsDisabled&&this._backdropRef.element.classList.add(`cdk-overlay-backdrop-noop-animation`),this._config.backdropClass&&this._toggleClasses(this._backdropRef.element,this._config.backdropClass,!0),this._config.usePopover?this._host.prepend(this._backdropRef.element):this._host.parentElement.insertBefore(this._backdropRef.element,this._host),!this._animationsDisabled&&typeof requestAnimationFrame<`u`?this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>this._backdropRef?.element.classList.add(n))}):this._backdropRef.element.classList.add(n)}_updateStackingOrder(){!this._config.usePopover&&this._host.nextSibling&&this._host.parentNode.appendChild(this._host)}detachBackdrop(){this._animationsDisabled?(this._backdropRef?.dispose(),this._backdropRef=null):this._backdropRef?.detach()}_toggleClasses(n,e,i){let a=Pr(e||[]).filter(o=>!!o);a.length&&(i?n.classList.add(...a):n.classList.remove(...a))}_detachContentWhenEmpty(){let n=!1;try{this._detachContentAfterRenderRef=At(()=>{n=!0,this._detachContent()},{injector:this._injector})}catch(e){if(n)throw e;this._detachContent()}globalThis.MutationObserver&&this._pane&&(this._detachContentMutationObserver||=new globalThis.MutationObserver(()=>{this._detachContent()}),this._detachContentMutationObserver.observe(this._pane,{childList:!0}))}_detachContent(){(!this._pane||!this._host||this._pane.children.length===0)&&(this._pane&&this._config.panelClass&&this._toggleClasses(this._pane,this._config.panelClass,!1),this._host&&this._host.parentElement&&(this._previousHostParent=this._host.parentElement,this._host.remove()),this._completeDetachContent())}_completeDetachContent(){this._detachContentAfterRenderRef?.destroy(),this._detachContentAfterRenderRef=void 0,this._detachContentMutationObserver?.disconnect()}_disposeScrollStrategy(){let n=this._scrollStrategy;n?.disable(),n?.detach?.()}};var lw=`cdk-overlay-connected-position-bounding-box`;var FO=/([A-Za-z%]+)$/;function Bo(t,n){return new rm(n,t.get(ui),t.get(de),t.get(Oe),t.get(cm))}var rm=class{_viewportRuler;_document;_platform;_overlayContainer;_overlayRef;_isInitialRender=!1;_lastBoundingBoxSize={width:0,height:0};_isPushed=!1;_canPush=!0;_growAfterOpen=!1;_hasFlexibleDimensions=!0;_positionLocked=!1;_originRect;_overlayRect;_viewportRect;_containerRect;_viewportMargin=0;_scrollables=[];_preferredPositions=[];_origin;_pane;_isDisposed=!1;_boundingBox=null;_lastPosition=null;_lastScrollVisibility=null;_positionChanges=new L;_resizeSubscription=De.EMPTY;_offsetX=0;_offsetY=0;_transformOriginSelector;_appliedPanelClasses=[];_previousPushAmount=null;_popoverLocation=`global`;positionChanges=this._positionChanges;get positions(){return this._preferredPositions}constructor(n,e,i,a,o){this._viewportRuler=e,this._document=i,this._platform=a,this._overlayContainer=o,this.setOrigin(n)}attach(n){this._overlayRef&&this._overlayRef,this._validatePositions(),n.hostElement.classList.add(lw),this._overlayRef=n,this._boundingBox=n.hostElement,this._pane=n.overlayElement,this._isDisposed=!1,this._isInitialRender=!0,this._lastPosition=null,this._resizeSubscription.unsubscribe(),this._resizeSubscription=this._viewportRuler.change().subscribe(()=>{this._isInitialRender=!0,this.apply()})}apply(){if(this._isDisposed||!this._platform.isBrowser)return;if(!this._isInitialRender&&this._positionLocked&&this._lastPosition){this.reapplyLastPosition();return}this._clearPanelClasses(),this._resetOverlayElementStyles(),this._resetBoundingBoxStyles(),this._viewportRect=this._getNarrowedViewportRect(),this._originRect=this._getOriginRect(),this._overlayRect=this._pane.getBoundingClientRect(),this._containerRect=this._getContainerRect();let n=this._originRect,e=this._overlayRect,i=this._viewportRect,a=this._containerRect,o=[],r;for(let s of this._preferredPositions){let c=this._getOriginPoint(n,a,s),h=this._getOverlayPoint(c,e,s),p=this._getOverlayFit(h,e,i,s);if(p.isCompletelyWithinViewport){this._isPushed=!1,this._applyPosition(s,c);return}if(this._canFitWithFlexibleDimensions(p,h,i)){o.push({position:s,origin:c,overlayRect:e,boundingBoxRect:this._calculateBoundingBoxRect(c,s)});continue}(!r||r.overlayFit.visibleArea<p.visibleArea)&&(r={overlayFit:p,overlayPoint:h,originPoint:c,position:s,overlayRect:e})}if(o.length){let s=null,c=-1;for(let h of o){let p=h.boundingBoxRect.width*h.boundingBoxRect.height*(h.position.weight||1);p>c&&(c=p,s=h)}this._isPushed=!1,this._applyPosition(s.position,s.origin);return}if(this._canPush){this._isPushed=!0,this._applyPosition(r.position,r.originPoint);return}this._applyPosition(r.position,r.originPoint)}detach(){this._clearPanelClasses(),this._lastPosition=null,this._previousPushAmount=null,this._resizeSubscription.unsubscribe()}dispose(){this._isDisposed||(this._boundingBox&&Vo(this._boundingBox.style,{top:``,left:``,right:``,bottom:``,height:``,width:``,alignItems:``,justifyContent:``}),this._pane&&this._resetOverlayElementStyles(),this._overlayRef&&this._overlayRef.hostElement.classList.remove(lw),this.detach(),this._positionChanges.complete(),this._overlayRef=this._boundingBox=null,this._isDisposed=!0)}reapplyLastPosition(){if(this._isDisposed||!this._platform.isBrowser)return;let n=this._lastPosition;n?(this._originRect=this._getOriginRect(),this._overlayRect=this._pane.getBoundingClientRect(),this._viewportRect=this._getNarrowedViewportRect(),this._containerRect=this._getContainerRect(),this._applyPosition(n,this._getOriginPoint(this._originRect,this._containerRect,n))):this.apply()}withScrollableContainers(n){return this._scrollables=n,this}withPositions(n){return this._preferredPositions=n,n.indexOf(this._lastPosition)===-1&&(this._lastPosition=null),this._validatePositions(),this}withViewportMargin(n){return this._viewportMargin=n,this}withFlexibleDimensions(n=!0){return this._hasFlexibleDimensions=n,this}withGrowAfterOpen(n=!0){return this._growAfterOpen=n,this}withPush(n=!0){return this._canPush=n,this}withLockedPosition(n=!0){return this._positionLocked=n,this}setOrigin(n){return this._origin=n,this}withDefaultOffsetX(n){return this._offsetX=n,this}withDefaultOffsetY(n){return this._offsetY=n,this}withTransformOriginOn(n){return this._transformOriginSelector=n,this}withPopoverLocation(n){return this._popoverLocation=n,this}getPopoverInsertionPoint(){return this._popoverLocation===`global`?null:this._popoverLocation!==`inline`?this._popoverLocation:this._origin instanceof W?this._origin.nativeElement:fb(this._origin)?this._origin:null}_getOriginPoint(n,e,i){let a;if(i.originX==`center`)a=n.left+n.width/2;else{let r=this._isRtl()?n.right:n.left,s=this._isRtl()?n.left:n.right;a=i.originX==`start`?r:s}e.left<0&&(a-=e.left);let o;return i.originY==`center`?o=n.top+n.height/2:o=i.originY==`top`?n.top:n.bottom,e.top<0&&(o-=e.top),{x:a,y:o}}_getOverlayPoint(n,e,i){let a;i.overlayX==`center`?a=-e.width/2:i.overlayX===`start`?a=this._isRtl()?-e.width:0:a=this._isRtl()?0:-e.width;let o;return i.overlayY==`center`?o=-e.height/2:o=i.overlayY==`top`?0:-e.height,{x:n.x+a,y:n.y+o}}_getOverlayFit(n,e,i,a){let o=uw(e),{x:r,y:s}=n,c=this._getOffset(a,`x`),h=this._getOffset(a,`y`);c&&(r+=c),h&&(s+=h);let p=0-r,g=r+o.width-i.width,v=0-s,b=s+o.height-i.height,y=this._subtractOverflows(o.width,p,g),E=this._subtractOverflows(o.height,v,b),P=y*E;return{visibleArea:P,isCompletelyWithinViewport:o.width*o.height===P,fitsInViewportVertically:E===o.height,fitsInViewportHorizontally:y==o.width}}_canFitWithFlexibleDimensions(n,e,i){if(this._hasFlexibleDimensions){let a=i.bottom-e.y,o=i.right-e.x,r=dw(this._overlayRef.getConfig().minHeight),s=dw(this._overlayRef.getConfig().minWidth),c=n.fitsInViewportVertically||r!=null&&r<=a,h=n.fitsInViewportHorizontally||s!=null&&s<=o;return c&&h}return!1}_pushOverlayOnScreen(n,e,i){if(this._previousPushAmount&&this._positionLocked)return{x:n.x+this._previousPushAmount.x,y:n.y+this._previousPushAmount.y};let a=uw(e),o=this._viewportRect,r=Math.max(n.x+a.width-o.width,0),s=Math.max(n.y+a.height-o.height,0),c=Math.max(o.top-i.top-n.y,0),h=Math.max(o.left-i.left-n.x,0),p=0,g=0;return a.width<=o.width?p=h||-r:p=n.x<this._getViewportMarginStart()?o.left-i.left-n.x:0,a.height<=o.height?g=c||-s:g=n.y<this._getViewportMarginTop()?o.top-i.top-n.y:0,this._previousPushAmount={x:p,y:g},{x:n.x+p,y:n.y+g}}_applyPosition(n,e){if(this._setTransformOrigin(n),this._setOverlayElementStyles(e,n),this._setBoundingBoxStyles(e,n),n.panelClass&&this._addPanelClasses(n.panelClass),this._positionChanges.observers.length){let i=this._getScrollVisibility();if(n!==this._lastPosition||!this._lastScrollVisibility||!LO(this._lastScrollVisibility,i)){let a=new om(n,i);this._positionChanges.next(a)}this._lastScrollVisibility=i}this._lastPosition=n,this._isInitialRender=!1}_setTransformOrigin(n){if(!this._transformOriginSelector)return;let e=this._boundingBox.querySelectorAll(this._transformOriginSelector),i,a=n.overlayY;n.overlayX===`center`?i=`center`:this._isRtl()?i=n.overlayX===`start`?`right`:`left`:i=n.overlayX===`start`?`left`:`right`;for(let o=0;o<e.length;o++)e[o].style.transformOrigin=`${i} ${a}`}_calculateBoundingBoxRect(n,e){let i=this._viewportRect,a=this._isRtl(),o,r,s;if(e.overlayY===`top`)r=n.y,o=i.height-r+this._getViewportMarginBottom();else if(e.overlayY===`bottom`)s=i.height-n.y+this._getViewportMarginTop()+this._getViewportMarginBottom(),o=i.height-s+this._getViewportMarginTop();else{let b=Math.min(i.bottom-n.y+i.top,n.y),y=this._lastBoundingBoxSize.height;o=b*2,r=n.y-b,o>y&&!this._isInitialRender&&!this._growAfterOpen&&(r=n.y-y/2)}let c=e.overlayX===`start`&&!a||e.overlayX===`end`&&a,h=e.overlayX===`end`&&!a||e.overlayX===`start`&&a,p,g,v;if(h)v=i.width-n.x+this._getViewportMarginStart()+this._getViewportMarginEnd(),p=n.x-this._getViewportMarginStart();else if(c)g=n.x,p=i.right-n.x-this._getViewportMarginEnd();else{let b=Math.min(i.right-n.x+i.left,n.x),y=this._lastBoundingBoxSize.width;p=b*2,g=n.x-b,p>y&&!this._isInitialRender&&!this._growAfterOpen&&(g=n.x-y/2)}return{top:r,left:g,bottom:s,right:v,width:p,height:o}}_setBoundingBoxStyles(n,e){let i=this._calculateBoundingBoxRect(n,e);!this._isInitialRender&&!this._growAfterOpen&&(i.height=Math.min(i.height,this._lastBoundingBoxSize.height),i.width=Math.min(i.width,this._lastBoundingBoxSize.width));let a={};if(this._hasExactPosition())a.top=a.left=`0`,a.bottom=a.right=`auto`,a.maxHeight=a.maxWidth=``,a.width=a.height=`100%`;else{let o=this._overlayRef.getConfig().maxHeight,r=this._overlayRef.getConfig().maxWidth;a.width=Dt(i.width),a.height=Dt(i.height),a.top=Dt(i.top)||`auto`,a.bottom=Dt(i.bottom)||`auto`,a.left=Dt(i.left)||`auto`,a.right=Dt(i.right)||`auto`,e.overlayX===`center`?a.alignItems=`center`:a.alignItems=e.overlayX===`end`?`flex-end`:`flex-start`,e.overlayY===`center`?a.justifyContent=`center`:a.justifyContent=e.overlayY===`bottom`?`flex-end`:`flex-start`,o&&(a.maxHeight=Dt(o)),r&&(a.maxWidth=Dt(r))}this._lastBoundingBoxSize=i,Vo(this._boundingBox.style,a)}_resetBoundingBoxStyles(){Vo(this._boundingBox.style,{top:`0`,left:`0`,right:`0`,bottom:`0`,height:``,width:``,alignItems:``,justifyContent:``})}_resetOverlayElementStyles(){Vo(this._pane.style,{top:``,left:``,bottom:``,right:``,position:``,transform:``})}_setOverlayElementStyles(n,e){let i={},a=this._hasExactPosition(),o=this._hasFlexibleDimensions,r=this._overlayRef.getConfig();if(a){let p=this._viewportRuler.getViewportScrollPosition();Vo(i,this._getExactOverlayY(e,n,p)),Vo(i,this._getExactOverlayX(e,n,p))}else i.position=`static`;let s=``,c=this._getOffset(e,`x`),h=this._getOffset(e,`y`);c&&(s+=`translateX(${c}px) `),h&&(s+=`translateY(${h}px)`),i.transform=s.trim(),r.maxHeight&&(a?i.maxHeight=Dt(r.maxHeight):o&&(i.maxHeight=``)),r.maxWidth&&(a?i.maxWidth=Dt(r.maxWidth):o&&(i.maxWidth=``)),Vo(this._pane.style,i)}_getExactOverlayY(n,e,i){let a={top:``,bottom:``},o=this._getOverlayPoint(e,this._overlayRect,n);if(this._isPushed&&(o=this._pushOverlayOnScreen(o,this._overlayRect,i)),n.overlayY===`bottom`)a.bottom=`${this._document.documentElement.clientHeight-(o.y+this._overlayRect.height)}px`;else a.top=Dt(o.y);return a}_getExactOverlayX(n,e,i){let a={left:``,right:``},o=this._getOverlayPoint(e,this._overlayRect,n);this._isPushed&&(o=this._pushOverlayOnScreen(o,this._overlayRect,i));let r;if(this._isRtl()?r=n.overlayX===`end`?`left`:`right`:r=n.overlayX===`end`?`right`:`left`,r===`right`)a.right=`${this._document.documentElement.clientWidth-(o.x+this._overlayRect.width)}px`;else a.left=Dt(o.x);return a}_getScrollVisibility(){let n=this._getOriginRect(),e=this._pane.getBoundingClientRect(),i=this._scrollables.map(a=>a.getElementRef().nativeElement.getBoundingClientRect());return{isOriginClipped:sw(n,i),isOriginOutsideView:pb(n,i),isOverlayClipped:sw(e,i),isOverlayOutsideView:pb(e,i)}}_subtractOverflows(n,...e){return e.reduce((i,a)=>i-Math.max(a,0),n)}_getNarrowedViewportRect(){let n=this._document.documentElement.clientWidth,e=this._document.documentElement.clientHeight,i=this._viewportRuler.getViewportScrollPosition();return{top:i.top+this._getViewportMarginTop(),left:i.left+this._getViewportMarginStart(),right:i.left+n-this._getViewportMarginEnd(),bottom:i.top+e-this._getViewportMarginBottom(),width:n-this._getViewportMarginStart()-this._getViewportMarginEnd(),height:e-this._getViewportMarginTop()-this._getViewportMarginBottom()}}_isRtl(){return this._overlayRef.getDirection()===`rtl`}_hasExactPosition(){return!this._hasFlexibleDimensions||this._isPushed}_getOffset(n,e){return e===`x`?n.offsetX==null?this._offsetX:n.offsetX:n.offsetY==null?this._offsetY:n.offsetY}_validatePositions(){}_addPanelClasses(n){this._pane&&Pr(n).forEach(e=>{e!==``&&this._appliedPanelClasses.indexOf(e)===-1&&(this._appliedPanelClasses.push(e),this._pane.classList.add(e))})}_clearPanelClasses(){this._pane&&(this._appliedPanelClasses.forEach(n=>{this._pane.classList.remove(n)}),this._appliedPanelClasses=[])}_getViewportMarginStart(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.start??0}_getViewportMarginEnd(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.end??0}_getViewportMarginTop(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.top??0}_getViewportMarginBottom(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.bottom??0}_getOriginRect(){let n=this._origin;if(n instanceof W)return n.nativeElement.getBoundingClientRect();if(n instanceof Element)return n.getBoundingClientRect();let e=n.width||0,i=n.height||0;return{top:n.y,bottom:n.y+i,left:n.x,right:n.x+e,height:i,width:e}}_getContainerRect(){let n=this._overlayRef.getConfig().usePopover&&this._popoverLocation!==`global`,e=this._overlayContainer.getContainerElement();n&&(e.style.display=`block`);let i=e.getBoundingClientRect();return n&&(e.style.display=``),i}};function Vo(t,n){for(let e in n)Object.hasOwn(n,e)&&(t[e]=n[e]);return t}function dw(t){if(typeof t!=`number`&&t!=null){let[n,e]=t.split(FO);return!e||e===`px`?parseFloat(n):null}return t||null}function uw(t){return{top:Math.floor(t.top),right:Math.floor(t.right),bottom:Math.floor(t.bottom),left:Math.floor(t.left),width:Math.floor(t.width),height:Math.floor(t.height)}}function LO(t,n){return t===n?!0:t.isOriginClipped===n.isOriginClipped&&t.isOriginOutsideView===n.isOriginOutsideView&&t.isOverlayClipped===n.isOverlayClipped&&t.isOverlayOutsideView===n.isOverlayOutsideView}var mw=`cdk-global-overlay-wrapper`;function ns(t){return new sm}var sm=class{_overlayRef;_cssPosition=`static`;_topOffset=``;_bottomOffset=``;_alignItems=``;_xPosition=``;_xOffset=``;_width=``;_height=``;_isDisposed=!1;attach(n){let e=n.getConfig();this._overlayRef=n,this._width&&!e.width&&n.updateSize({width:this._width}),this._height&&!e.height&&n.updateSize({height:this._height}),n.hostElement.classList.add(mw),this._isDisposed=!1}top(n=``){return this._bottomOffset=``,this._topOffset=n,this._alignItems=`flex-start`,this}left(n=``){return this._xOffset=n,this._xPosition=`left`,this}bottom(n=``){return this._topOffset=``,this._bottomOffset=n,this._alignItems=`flex-end`,this}right(n=``){return this._xOffset=n,this._xPosition=`right`,this}start(n=``){return this._xOffset=n,this._xPosition=`start`,this}end(n=``){return this._xOffset=n,this._xPosition=`end`,this}width(n=``){return this._overlayRef?this._overlayRef.updateSize({width:n}):this._width=n,this}height(n=``){return this._overlayRef?this._overlayRef.updateSize({height:n}):this._height=n,this}centerHorizontally(n=``){return this.left(n),this._xPosition=`center`,this}centerVertically(n=``){return this.top(n),this._alignItems=`center`,this}apply(){if(!this._overlayRef||!this._overlayRef.hasAttached())return;let n=this._overlayRef.overlayElement.style,e=this._overlayRef.hostElement.style,{width:a,height:o,maxWidth:r,maxHeight:s}=this._overlayRef.getConfig(),c=(a===`100%`||a===`100vw`)&&(!r||r===`100%`||r===`100vw`),h=(o===`100%`||o===`100vh`)&&(!s||s===`100%`||s===`100vh`),p=this._xPosition,g=this._xOffset,v=this._overlayRef.getConfig().direction===`rtl`,b=``,y=``,E=``;c?E=`flex-start`:p===`center`?(E=`center`,v?y=g:b=g):v?p===`left`||p===`end`?(E=`flex-end`,b=g):(p===`right`||p===`start`)&&(E=`flex-start`,y=g):p===`left`||p===`start`?(E=`flex-start`,b=g):(p===`right`||p===`end`)&&(E=`flex-end`,y=g),n.position=this._cssPosition,n.marginLeft=c?`0`:b,n.marginTop=h?`0`:this._topOffset,n.marginBottom=this._bottomOffset,n.marginRight=c?`0`:y,e.justifyContent=E,e.alignItems=h?`flex-start`:this._alignItems}dispose(){if(this._isDisposed||!this._overlayRef)return;let n=this._overlayRef.overlayElement.style,e=this._overlayRef.hostElement,i=e.style;e.classList.remove(mw),i.justifyContent=i.alignItems=n.marginTop=n.marginBottom=n.marginLeft=n.marginRight=n.position=``,this._overlayRef=null,this._isDisposed=!0}};var _w=(()=>{class t{_injector=f(se);global(){return ns()}flexibleConnectedTo(e){return Bo(this._injector,e)}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var Gc=new k(`OVERLAY_DEFAULT_CONFIG`);function sa(t,n){t.get(Je).load(vw);let e=t.get(cm),i=t.get(de),a=t.get(Be),o=t.get(Rn),r=t.get(gt),s=t.get(Re,null,{optional:!0})||t.get(Tt).createRenderer(null,null),c=new aa(n),h=t.get(Gc,null,{optional:!0})?.usePopover??!0;c.direction=c.direction||r.value,!i.body||!(`showPopover`in i.body)?c.usePopover=!1:c.usePopover=n?.usePopover??h;let p=i.createElement(`div`),g=i.createElement(`div`);p.id=a.getId(`cdk-overlay-`),p.classList.add(`cdk-overlay-pane`),g.appendChild(p),c.usePopover&&(g.setAttribute(`popover`,`manual`),g.classList.add(`cdk-overlay-popover`));let v=c.usePopover?c.positionStrategy?.getPopoverInsertionPoint?.():null;return fb(v)?v.after(g):v?.type===`parent`?v.element.appendChild(g):e.getContainerElement().appendChild(g),new Jr(new Hc(p,o,t),g,p,c,t.get(X),t.get(gw),i,t.get(iu),t.get(bw),n?.disableAnimations??t.get(Vs,null,{optional:!0})===`NoopAnimations`,t.get(kt),s)}var yw=(()=>{class t{scrollStrategies=f(hw);_positionBuilder=f(_w);_injector=f(se);create(e){return sa(this._injector,e)}position(){return this._positionBuilder}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var VO=[{originX:`start`,originY:`bottom`,overlayX:`start`,overlayY:`top`},{originX:`start`,originY:`top`,overlayX:`start`,overlayY:`bottom`},{originX:`end`,originY:`top`,overlayX:`end`,overlayY:`bottom`},{originX:`end`,originY:`bottom`,overlayX:`end`,overlayY:`top`}];var BO=new k(`cdk-connected-overlay-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t)}});var es=(()=>{class t{elementRef=f(W);static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`cdk-overlay-origin`,``],[``,`overlay-origin`,``],[``,`cdkOverlayOrigin`,``]],exportAs:[`cdkOverlayOrigin`]})}return t})();var xw=new k(`cdk-connected-overlay-default-config`);var lm=(()=>{class t{_dir=f(gt,{optional:!0});_injector=f(se);_overlayRef;_templatePortal;_backdropSubscription=De.EMPTY;_attachSubscription=De.EMPTY;_detachSubscription=De.EMPTY;_positionSubscription=De.EMPTY;_offsetX;_offsetY;_position;_scrollStrategyFactory=f(BO);_ngZone=f(X);origin;positions;positionStrategy;get offsetX(){return this._offsetX}set offsetX(e){this._offsetX=e,this._position&&this._updatePositionStrategy(this._position)}get offsetY(){return this._offsetY}set offsetY(e){this._offsetY=e,this._position&&this._updatePositionStrategy(this._position)}width;height;minWidth;minHeight;backdropClass;panelClass;viewportMargin=0;scrollStrategy;open=!1;disableClose=!1;transformOriginSelector;hasBackdrop=!1;lockPosition=!1;flexibleDimensions=!1;growAfterOpen=!1;push=!1;disposeOnNavigation=!1;usePopover;matchWidth=!1;set _config(e){typeof e!=`string`&&this._assignConfig(e)}backdropClick=new ee;positionChange=new ee;attach=new ee;detach=new ee;overlayKeydown=new ee;overlayOutsideClick=new ee;constructor(){let e=f(Vt),i=f(Bt),a=f(xw,{optional:!0}),o=f(Gc,{optional:!0});this.usePopover=o?.usePopover===!1?null:`global`,this._templatePortal=new Hn(e,i),this.scrollStrategy=this._scrollStrategyFactory(),a&&this._assignConfig(a)}get overlayRef(){return this._overlayRef}get dir(){return this._dir?this._dir.value:`ltr`}ngOnDestroy(){this._attachSubscription.unsubscribe(),this._detachSubscription.unsubscribe(),this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this._overlayRef?.dispose()}ngOnChanges(e){this._position&&(this._updatePositionStrategy(this._position),this._overlayRef?.updateSize({width:this._getWidth(),minWidth:this.minWidth,height:this.height,minHeight:this.minHeight}),e.origin&&this.open&&this._position.apply()),e.open&&(this.open?this.attachOverlay():this.detachOverlay())}_createOverlay(){(!this.positions||!this.positions.length)&&(this.positions=VO);let e=this._overlayRef=sa(this._injector,this._buildConfig());this._attachSubscription=e.attachments().subscribe(()=>this.attach.emit()),this._detachSubscription=e.detachments().subscribe(()=>this.detach.emit()),e.keydownEvents().subscribe(i=>{this.overlayKeydown.next(i),i.keyCode===27&&!this.disableClose&&!xt(i)&&(i.preventDefault(),this.detachOverlay())}),this._overlayRef.outsidePointerEvents().subscribe(i=>{let a=this._getOriginElement(),o=Yt(i);(!a||a!==o&&!a.contains(o))&&this.overlayOutsideClick.next(i)})}_buildConfig(){let e=this._position=this.positionStrategy||this._createPositionStrategy(),i=new aa({direction:this._dir||`ltr`,positionStrategy:e,scrollStrategy:this.scrollStrategy,hasBackdrop:this.hasBackdrop,disposeOnNavigation:this.disposeOnNavigation,usePopover:!!this.usePopover});return(this.height||this.height===0)&&(i.height=this.height),(this.minWidth||this.minWidth===0)&&(i.minWidth=this.minWidth),(this.minHeight||this.minHeight===0)&&(i.minHeight=this.minHeight),this.backdropClass&&(i.backdropClass=this.backdropClass),this.panelClass&&(i.panelClass=this.panelClass),i}_updatePositionStrategy(e){let i=this.positions.map(a=>({originX:a.originX,originY:a.originY,overlayX:a.overlayX,overlayY:a.overlayY,offsetX:a.offsetX||this.offsetX,offsetY:a.offsetY||this.offsetY,panelClass:a.panelClass||void 0}));return e.setOrigin(this._getOrigin()).withPositions(i).withFlexibleDimensions(this.flexibleDimensions).withPush(this.push).withGrowAfterOpen(this.growAfterOpen).withViewportMargin(this.viewportMargin).withLockedPosition(this.lockPosition).withTransformOriginOn(this.transformOriginSelector).withPopoverLocation(this.usePopover===null?`global`:this.usePopover)}_createPositionStrategy(){let e=Bo(this._injector,this._getOrigin());return this._updatePositionStrategy(e),e}_getOrigin(){return this.origin instanceof es?this.origin.elementRef:this.origin}_getOriginElement(){return this.origin instanceof es?this.origin.elementRef.nativeElement:this.origin instanceof W?this.origin.nativeElement:typeof Element<`u`&&this.origin instanceof Element?this.origin:null}_getWidth(){return this.width?this.width:this.matchWidth?this._getOriginElement()?.getBoundingClientRect?.().width:void 0}attachOverlay(){this._overlayRef||this._createOverlay();let e=this._overlayRef;e.getConfig().hasBackdrop=this.hasBackdrop,e.updateSize({width:this._getWidth()}),e.hasAttached()||e.attach(this._templatePortal),this.hasBackdrop?this._backdropSubscription=e.backdropClick().subscribe(i=>this.backdropClick.emit(i)):this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this.positionChange.observers.length>0&&(this._positionSubscription=this._position.positionChanges.pipe(lp(()=>this.positionChange.observers.length>0)).subscribe(i=>{this._ngZone.run(()=>this.positionChange.emit(i)),this.positionChange.observers.length===0&&this._positionSubscription.unsubscribe()})),this.open=!0}detachOverlay(){this._overlayRef?.detach(),this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this.open=!1}_assignConfig(e){this.origin=e.origin??this.origin,this.positions=e.positions??this.positions,this.positionStrategy=e.positionStrategy??this.positionStrategy,this.offsetX=e.offsetX??this.offsetX,this.offsetY=e.offsetY??this.offsetY,this.width=e.width??this.width,this.height=e.height??this.height,this.minWidth=e.minWidth??this.minWidth,this.minHeight=e.minHeight??this.minHeight,this.backdropClass=e.backdropClass??this.backdropClass,this.panelClass=e.panelClass??this.panelClass,this.viewportMargin=e.viewportMargin??this.viewportMargin,this.scrollStrategy=e.scrollStrategy??this.scrollStrategy,this.disableClose=e.disableClose??this.disableClose,this.transformOriginSelector=e.transformOriginSelector??this.transformOriginSelector,this.hasBackdrop=e.hasBackdrop??this.hasBackdrop,this.lockPosition=e.lockPosition??this.lockPosition,this.flexibleDimensions=e.flexibleDimensions??this.flexibleDimensions,this.growAfterOpen=e.growAfterOpen??this.growAfterOpen,this.push=e.push??this.push,this.disposeOnNavigation=e.disposeOnNavigation??this.disposeOnNavigation,this.usePopover=e.usePopover??this.usePopover,this.matchWidth=e.matchWidth??this.matchWidth}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`cdk-connected-overlay`,``],[``,`connected-overlay`,``],[``,`cdkConnectedOverlay`,``]],inputs:{origin:[0,`cdkConnectedOverlayOrigin`,`origin`],positions:[0,`cdkConnectedOverlayPositions`,`positions`],positionStrategy:[0,`cdkConnectedOverlayPositionStrategy`,`positionStrategy`],offsetX:[0,`cdkConnectedOverlayOffsetX`,`offsetX`],offsetY:[0,`cdkConnectedOverlayOffsetY`,`offsetY`],width:[0,`cdkConnectedOverlayWidth`,`width`],height:[0,`cdkConnectedOverlayHeight`,`height`],minWidth:[0,`cdkConnectedOverlayMinWidth`,`minWidth`],minHeight:[0,`cdkConnectedOverlayMinHeight`,`minHeight`],backdropClass:[0,`cdkConnectedOverlayBackdropClass`,`backdropClass`],panelClass:[0,`cdkConnectedOverlayPanelClass`,`panelClass`],viewportMargin:[0,`cdkConnectedOverlayViewportMargin`,`viewportMargin`],scrollStrategy:[0,`cdkConnectedOverlayScrollStrategy`,`scrollStrategy`],open:[0,`cdkConnectedOverlayOpen`,`open`],disableClose:[0,`cdkConnectedOverlayDisableClose`,`disableClose`],transformOriginSelector:[0,`cdkConnectedOverlayTransformOriginOn`,`transformOriginSelector`],hasBackdrop:[2,`cdkConnectedOverlayHasBackdrop`,`hasBackdrop`,V],lockPosition:[2,`cdkConnectedOverlayLockPosition`,`lockPosition`,V],flexibleDimensions:[2,`cdkConnectedOverlayFlexibleDimensions`,`flexibleDimensions`,V],growAfterOpen:[2,`cdkConnectedOverlayGrowAfterOpen`,`growAfterOpen`,V],push:[2,`cdkConnectedOverlayPush`,`push`,V],disposeOnNavigation:[2,`cdkConnectedOverlayDisposeOnNavigation`,`disposeOnNavigation`,e=>e===`url-change`||e===`pop-state`?e:V(e)],usePopover:[0,`cdkConnectedOverlayUsePopover`,`usePopover`],matchWidth:[2,`cdkConnectedOverlayMatchWidth`,`matchWidth`,V],_config:[0,`cdkConnectedOverlay`,`_config`]},outputs:{backdropClick:`backdropClick`,positionChange:`positionChange`,attach:`attach`,detach:`detach`,overlayKeydown:`overlayKeydown`,overlayOutsideClick:`overlayOutsideClick`},exportAs:[`cdkConnectedOverlay`],features:[vt]})}return t})();var mi=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({providers:[yw],imports:[we,Lo,ub,ub]})}return t})();var bb=new k(`MAT_MENU_PANEL`);var ca=(()=>{class t{_isAnchor;_elementRef=f(W);_document=f(de);_focusMonitor=f(Jt);_parentMenu=f(bb,{optional:!0});_changeDetectorRef=f(Fe);role=`menuitem`;disabled=!1;disabledInteractive=!1;disableRipple=!1;_hovered=new L;_focused=new L;_highlighted=!1;_triggersSubmenu=!1;constructor(){f(Je).load(dn),this._parentMenu?.addItem?.(this),this._isAnchor=this._elementRef.nativeElement.tagName===`A`}focus(e,i){this._focusMonitor&&e?this._focusMonitor.focusVia(this._getHostElement(),e,i):this._getHostElement().focus(i),this._focused.next(this)}ngAfterViewInit(){this._focusMonitor&&this._focusMonitor.monitor(this._elementRef,!1)}ngOnDestroy(){this._focusMonitor&&this._focusMonitor.stopMonitoring(this._elementRef),this._parentMenu&&this._parentMenu.removeItem&&this._parentMenu.removeItem(this),this._hovered.complete(),this._focused.complete()}_getTabIndex(){return this.disabled&&!this.disabledInteractive?`-1`:`0`}_getAriaDisabled(){return this._isAnchor?this.disabled||null:this.disabled&&this.disabledInteractive?!0:null}_getDisabled(){return this.disabledInteractive||!this.disabled?null:!0}_getHostElement(){return this._elementRef.nativeElement}_checkDisabled(e){this.disabled&&(e.preventDefault(),e.stopPropagation())}_handleMouseEnter(){this._hovered.next(this)}getLabel(){let e=this._elementRef.nativeElement.cloneNode(!0),i=e.querySelectorAll(`mat-icon, .material-icons, .material-symbols-outlined, .material-symbols-rounded, .material-symbols-sharp`);for(let a=0;a<i.length;a++)i[a].remove();return e.textContent?.trim()||``}_setHighlighted(e){this._highlighted=e,this._changeDetectorRef.markForCheck()}_setTriggersSubmenu(e){this._triggersSubmenu=e,this._changeDetectorRef.markForCheck()}_hasFocus(){return this._document&&this._document.activeElement===this._getHostElement()}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[`mat-icon`],[``,`matMenuItemIcon`,``]],`*`],i=[`mat-icon, [matMenuItemIcon]`,`*`];function a(o,r){o&1&&(Gt(),l(0,`svg`,2),H(1,`polygon`,3),d())}return B({type:t,selectors:[[``,`mat-menu-item`,``]],hostAttrs:[1,`mat-mdc-menu-item`,`mat-focus-indicator`],hostVars:12,hostBindings:function(r,s){r&1&&C(`click`,function(h){return s._checkDisabled(h)})(`mouseenter`,function(){return s._handleMouseEnter()}),r&2&&(te(`role`,s.role)(`tabindex`,s._getTabIndex())(`aria-disabled`,s._getAriaDisabled())(`disabled`,s._getDisabled()),Y(`mat-mdc-menu-item-highlighted`,s._highlighted)(`mat-mdc-menu-item-submenu-trigger`,s._triggersSubmenu)(`mat-mdc-menu-item-disabled`,s.disabled)(`mat-mdc-menu-item-disabled-interactive`,s.disabledInteractive))},inputs:{role:`role`,disabled:[2,`disabled`,`disabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V]},exportAs:[`matMenuItem`],ngContentSelectors:i,decls:5,vars:3,consts:[[1,`mat-mdc-menu-item-text`],[`matRipple`,``,1,`mat-mdc-menu-ripple`,3,`matRippleDisabled`,`matRippleTrigger`],[`viewBox`,`0 0 5 10`,`focusable`,`false`,`aria-hidden`,`true`,1,`mat-mdc-menu-submenu-icon`],[`points`,`0,0 5,5 0,10`]],template:function(r,s){r&1&&(Pe(e),he(0),l(1,`span`,0),he(2,1),d(),H(3,`div`,1),D(4,a,2,0,`:svg:svg`,2)),r&2&&(m(3),S(`matRippleDisabled`,s.disableRipple||s.disabled)(`matRippleTrigger`,s._getHostElement()),m(),I(s._triggersSubmenu?4:-1))},dependencies:[ci],encapsulation:2})})()}return t})();var jO=new k(`MatMenuContent`);var zO=new k(`mat-menu-default-options`,{providedIn:`root`,factory:()=>({overlapTrigger:!1,xPosition:`after`,yPosition:`below`,backdropClass:`cdk-overlay-transparent-backdrop`})});var gb=`_mat-menu-enter`;var dm=`_mat-menu-exit`;var Ni=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Fe);_injector=f(se);_keyManager;_xPosition;_yPosition;_firstItemFocusRef;_exitFallbackTimeout;_animationsDisabled=Ue();_allItems;_directDescendantItems=new wn;_classList={};_panelAnimationState=`void`;_animationDone=new L;_isAnimating=T(!1);parentMenu;direction;overlayPanelClass;backdropClass;ariaLabel;ariaLabelledby;ariaDescribedby;get xPosition(){return this._xPosition}set xPosition(e){this._xPosition=e,this.setPositionClasses()}get yPosition(){return this._yPosition}set yPosition(e){this._yPosition=e,this.setPositionClasses()}templateRef;items;lazyContent;overlapTrigger=!1;hasBackdrop;get panelClass(){return this._previousPanelClass}set panelClass(e){let i=this._previousPanelClass,a=r({},this._classList);i&&i.length&&i.split(` `).forEach(o=>{a[o]=!1}),this._previousPanelClass=e,e&&e.length&&(e.split(` `).forEach(o=>{a[o]=!0}),this._elementRef.nativeElement.className=``),this._classList=a}_previousPanelClass=``;get classList(){return this.panelClass}set classList(e){this.panelClass=e}closed=new ee;close=this.closed;panelId=f(Be).getId(`mat-menu-panel-`);constructor(){let e=f(zO);this.overlayPanelClass=e.overlayPanelClass||``,this._xPosition=e.xPosition,this._yPosition=e.yPosition,this.backdropClass=e.backdropClass,this.overlapTrigger=e.overlapTrigger,this.hasBackdrop=e.hasBackdrop}ngOnInit(){this.setPositionClasses()}ngAfterContentInit(){this._updateDirectDescendants(),this._keyManager=new ea(this._directDescendantItems).withWrap().withTypeAhead().withHomeAndEnd().skipPredicate(e=>e.disabled&&!e.disabledInteractive),this._keyManager.tabOut.subscribe(()=>this.closed.emit(`tab`)),this._directDescendantItems.changes.pipe(mt(this._directDescendantItems),_n(e=>It(...e.map(i=>i._focused)))).subscribe(e=>this._keyManager.updateActiveItem(e)),this._directDescendantItems.changes.subscribe(e=>{let i=this._keyManager;if(this._panelAnimationState===`enter`&&i.activeItem?._hasFocus()){let a=e.toArray(),o=Math.max(0,Math.min(a.length-1,i.activeItemIndex||0));a[o]&&!a[o].disabled?i.setActiveItem(o):i.setNextItemActive()}})}ngOnDestroy(){this._keyManager?.destroy(),this._directDescendantItems.destroy(),this.closed.complete(),this._firstItemFocusRef?.destroy(),clearTimeout(this._exitFallbackTimeout)}_hovered(){return this._directDescendantItems.changes.pipe(mt(this._directDescendantItems),_n(i=>It(...i.map(a=>a._hovered))))}addItem(e){}removeItem(e){}_handleKeydown(e){let i=e.keyCode,a=this._keyManager;switch(i){case 27:xt(e)||(e.preventDefault(),this.closed.emit(`keydown`));break;case 37:this.parentMenu&&this.direction===`ltr`&&this.closed.emit(`keydown`);break;case 39:this.parentMenu&&this.direction===`rtl`&&this.closed.emit(`keydown`);break;default:(i===38||i===40)&&a.setFocusOrigin(`keyboard`),a.onKeydown(e);return}}focusFirstItem(e=`program`){this._firstItemFocusRef?.destroy(),this._firstItemFocusRef=At(()=>{let i=this._resolvePanel();if(!i||!i.contains(document.activeElement)){let a=this._keyManager;a.setFocusOrigin(e).setFirstItemActive(),!a.activeItem&&i&&i.focus()}},{injector:this._injector})}resetActiveItem(){this._keyManager.setActiveItem(-1)}setElevation(e){}setPositionClasses(e=this.xPosition,i=this.yPosition){this._classList=s(r({},this._classList),{"mat-menu-before":e===`before`,"mat-menu-after":e===`after`,"mat-menu-above":i===`above`,"mat-menu-below":i===`below`}),this._changeDetectorRef.markForCheck()}_onAnimationDone(e){let i=e===dm;(i||e===gb)&&(i&&(clearTimeout(this._exitFallbackTimeout),this._exitFallbackTimeout=void 0),this._animationDone.next(i?`void`:`enter`),this._isAnimating.set(!1))}_onAnimationStart(e){(e===gb||e===dm)&&this._isAnimating.set(!0)}_setIsOpen(e){if(this._panelAnimationState=e?`enter`:`void`,e){if(this._keyManager.activeItemIndex===0){let i=this._resolvePanel();i&&(i.scrollTop=0)}}else this._animationsDisabled||(this._exitFallbackTimeout=setTimeout(()=>this._onAnimationDone(dm),200));this._animationsDisabled&&setTimeout(()=>{this._onAnimationDone(e?gb:dm)}),this._changeDetectorRef.markForCheck()}_updateDirectDescendants(){this._allItems.changes.pipe(mt(this._allItems)).subscribe(e=>{this._directDescendantItems.reset(e.filter(i=>i._parentMenu===this)),this._directDescendantItems.notifyOnChanges()})}_resolvePanel(){let e=null;return this._directDescendantItems.length&&(e=this._directDescendantItems.first._getHostElement().closest(`[role="menu"]`)),e}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`];function i(a,o){if(a&1){let r=q();ct(0,`div`,0),Mr(`click`,function(){N(r);return A(_().closed.emit(`click`))})(`animationstart`,function(c){N(r);return A(_()._onAnimationStart(c.animationName))})(`animationend`,function(c){N(r);return A(_()._onAnimationDone(c.animationName))})(`animationcancel`,function(c){N(r);return A(_()._onAnimationDone(c.animationName))}),ct(1,`div`,1),he(2),ht()()}if(a&2){let r=_();tt(r._classList),Y(`mat-menu-panel-animations-disabled`,r._animationsDisabled)(`mat-menu-panel-exit-animation`,r._panelAnimationState===`void`)(`mat-menu-panel-animating`,r._isAnimating()),qt(`id`,r.panelId),te(`aria-label`,r.ariaLabel||null)(`aria-labelledby`,r.ariaLabelledby||null)(`aria-describedby`,r.ariaDescribedby||null)}}return B({type:t,selectors:[[`mat-menu`]],contentQueries:function(o,r,s){if(o&1&&jt(s,jO,5)(s,ca,5)(s,ca,4),o&2){let c;Z(c=J())&&(r.lazyContent=c.first),Z(c=J())&&(r._allItems=c),Z(c=J())&&(r.items=c)}},viewQuery:function(o,r){if(o&1&&Ze(Vt,5),o&2){let s;Z(s=J())&&(r.templateRef=s.first)}},hostVars:3,hostBindings:function(o,r){o&2&&te(`aria-label`,null)(`aria-labelledby`,null)(`aria-describedby`,null)},inputs:{backdropClass:`backdropClass`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],ariaDescribedby:[0,`aria-describedby`,`ariaDescribedby`],xPosition:`xPosition`,yPosition:`yPosition`,overlapTrigger:[2,`overlapTrigger`,`overlapTrigger`,V],hasBackdrop:[2,`hasBackdrop`,`hasBackdrop`,a=>a==null?null:V(a)],panelClass:[0,`class`,`panelClass`],classList:`classList`},outputs:{closed:`closed`,close:`close`},exportAs:[`matMenu`],features:[Ge([{provide:bb,useExisting:t}])],ngContentSelectors:e,decls:1,vars:0,consts:[[`tabindex`,`-1`,`role`,`menu`,1,`mat-mdc-menu-panel`,3,`click`,`animationstart`,`animationend`,`animationcancel`,`id`],[1,`mat-mdc-menu-content`]],template:function(o,r){o&1&&(Pe(),Ir(0,i,3,12,`ng-template`))},styles:[`mat-menu {
  display: none;
}

.mat-mdc-menu-content {
  margin: 0;
  padding: 8px 0;
  outline: 0;
}
.mat-mdc-menu-content,
.mat-mdc-menu-content .mat-mdc-menu-item .mat-mdc-menu-item-text {
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  flex: 1;
  white-space: normal;
  font-family: var(--%NS%mat-menu-item-label-text-font, var(--%NS%mat-sys-label-large-font));
  line-height: var(--%NS%mat-menu-item-label-text-line-height, var(--%NS%mat-sys-label-large-line-height));
  font-size: var(--%NS%mat-menu-item-label-text-size, var(--%NS%mat-sys-label-large-size));
  letter-spacing: var(--%NS%mat-menu-item-label-text-tracking, var(--%NS%mat-sys-label-large-tracking));
  font-weight: var(--%NS%mat-menu-item-label-text-weight, var(--%NS%mat-sys-label-large-weight));
}

@keyframes _mat-menu-enter {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
@keyframes _mat-menu-exit {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}
.mat-mdc-menu-panel {
  min-width: 112px;
  max-width: 280px;
  overflow: auto;
  box-sizing: border-box;
  outline: 0;
  animation: _mat-menu-enter 120ms cubic-bezier(0, 0, 0.2, 1);
  border-radius: var(--%NS%mat-menu-container-shape, var(--%NS%mat-sys-corner-extra-small));
  background-color: var(--%NS%mat-menu-container-color, var(--%NS%mat-sys-surface-container));
  box-shadow: var(--%NS%mat-menu-container-elevation-shadow, 0px 3px 1px -2px rgba(0, 0, 0, 0.2), 0px 2px 2px 0px rgba(0, 0, 0, 0.14), 0px 1px 5px 0px rgba(0, 0, 0, 0.12));
  will-change: transform, opacity;
}
.mat-mdc-menu-panel.mat-menu-panel-exit-animation {
  animation: _mat-menu-exit 100ms 25ms linear forwards;
}
.mat-mdc-menu-panel.mat-menu-panel-animations-disabled {
  animation: none;
}
.mat-mdc-menu-panel.mat-menu-panel-animating {
  pointer-events: none;
}
.mat-mdc-menu-panel.mat-menu-panel-animating:has(.mat-mdc-menu-content:empty) {
  display: none;
}
@media (forced-colors: active) {
  .mat-mdc-menu-panel {
    outline: solid 1px;
  }
}
.mat-mdc-menu-panel .mat-divider {
  border-top-color: var(--%NS%mat-menu-divider-color, var(--%NS%mat-sys-surface-variant));
  margin-bottom: var(--%NS%mat-menu-divider-bottom-spacing, 8px);
  margin-top: var(--%NS%mat-menu-divider-top-spacing, 8px);
}

.mat-mdc-menu-item {
  display: flex;
  position: relative;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  padding: 0;
  cursor: pointer;
  width: 100%;
  text-align: left;
  box-sizing: border-box;
  color: inherit;
  font-size: inherit;
  background: none;
  text-decoration: none;
  margin: 0;
  min-height: 48px;
  padding-left: var(--%NS%mat-menu-item-leading-spacing, 12px);
  padding-right: var(--%NS%mat-menu-item-trailing-spacing, 12px);
  -webkit-user-select: none;
  user-select: none;
  cursor: pointer;
  outline: none;
  border: none;
  -webkit-tap-highlight-color: transparent;
}
.mat-mdc-menu-item::-moz-focus-inner {
  border: 0;
}
[dir=rtl] .mat-mdc-menu-item {
  padding-left: var(--%NS%mat-menu-item-trailing-spacing, 12px);
  padding-right: var(--%NS%mat-menu-item-leading-spacing, 12px);
}
.mat-mdc-menu-item:has(.material-icons, .material-symbols-outlined, .material-symbols-rounded, .material-symbols-sharp, mat-icon, [matButtonIcon]) {
  padding-left: var(--%NS%mat-menu-item-with-icon-leading-spacing, 12px);
  padding-right: var(--%NS%mat-menu-item-with-icon-trailing-spacing, 12px);
}
[dir=rtl] .mat-mdc-menu-item:has(.material-icons, .material-symbols-outlined, .material-symbols-rounded, .material-symbols-sharp, mat-icon, [matButtonIcon]) {
  padding-left: var(--%NS%mat-menu-item-with-icon-trailing-spacing, 12px);
  padding-right: var(--%NS%mat-menu-item-with-icon-leading-spacing, 12px);
}
.mat-mdc-menu-item, .mat-mdc-menu-item:visited, .mat-mdc-menu-item:link {
  color: var(--%NS%mat-menu-item-label-text-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-menu-item .mat-icon-no-color,
.mat-mdc-menu-item .mat-mdc-menu-submenu-icon {
  color: var(--%NS%mat-menu-item-icon-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-menu-item[disabled], .mat-mdc-menu-item.mat-mdc-menu-item-disabled {
  cursor: default;
  opacity: 0.38;
}
.mat-mdc-menu-item[disabled]::after, .mat-mdc-menu-item.mat-mdc-menu-item-disabled::after {
  display: block;
  position: absolute;
  content: "";
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
}
.mat-mdc-menu-item:focus {
  outline: 0;
}
.mat-mdc-menu-item .mat-icon {
  flex-shrink: 0;
  margin-right: var(--%NS%mat-menu-item-spacing, 12px);
  height: var(--%NS%mat-menu-item-icon-size, 24px);
  width: var(--%NS%mat-menu-item-icon-size, 24px);
}
[dir=rtl] .mat-mdc-menu-item {
  text-align: right;
}
[dir=rtl] .mat-mdc-menu-item .mat-icon {
  margin-right: 0;
  margin-left: var(--%NS%mat-menu-item-spacing, 12px);
}
.mat-mdc-menu-item:not([disabled]):hover {
  background-color: var(--%NS%mat-menu-item-hover-state-layer-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) calc(var(--%NS%mat-sys-hover-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-menu-item:not([disabled]).cdk-program-focused, .mat-mdc-menu-item:not([disabled]).cdk-keyboard-focused, .mat-mdc-menu-item:not([disabled]).mat-mdc-menu-item-highlighted {
  background-color: var(--%NS%mat-menu-item-focus-state-layer-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) calc(var(--%NS%mat-sys-focus-state-layer-opacity) * 100%), transparent));
}
@media (forced-colors: active) {
  .mat-mdc-menu-item {
    margin-top: 1px;
  }
}

.mat-mdc-menu-submenu-icon {
  width: var(--%NS%mat-menu-item-icon-size, 24px);
  height: 10px;
  fill: currentColor;
  padding-left: var(--%NS%mat-menu-item-spacing, 12px);
}
[dir=rtl] .mat-mdc-menu-submenu-icon {
  padding-right: var(--%NS%mat-menu-item-spacing, 12px);
  padding-left: 0;
}
[dir=rtl] .mat-mdc-menu-submenu-icon polygon {
  transform: scaleX(-1);
  transform-origin: center;
}
@media (forced-colors: active) {
  .mat-mdc-menu-submenu-icon {
    fill: CanvasText;
  }
}

.mat-mdc-menu-item .mat-mdc-menu-ripple {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
}
`],encapsulation:2})})()}return t})();var HO=new k(`mat-menu-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t)}});var is=new WeakMap;var UO=(()=>{class t{_canHaveBackdrop;_element=f(W);_viewContainerRef=f(Bt);_menuItemInstance=f(ca,{optional:!0,self:!0});_dir=f(gt,{optional:!0});_focusMonitor=f(Jt);_ngZone=f(X);_injector=f(se);_scrollStrategy=f(HO);_changeDetectorRef=f(Fe);_animationsDisabled=Ue();_portal;_overlayRef=null;_menuOpen=!1;_closingActionsSubscription=De.EMPTY;_menuCloseSubscription=De.EMPTY;_pendingRemoval;_parentMaterialMenu;_parentInnerPadding;_openedBy=void 0;get _menu(){return this._menuInternal}set _menu(e){e!==this._menuInternal&&(this._menuInternal=e,this._menuCloseSubscription.unsubscribe(),e?(this._parentMaterialMenu,this._menuCloseSubscription=e.close.subscribe(i=>{this._destroyMenu(i),(i===`click`||i===`tab`)&&this._parentMaterialMenu&&this._parentMaterialMenu.closed.emit(i)})):this._destroyMenu(),this._menuItemInstance?._setTriggersSubmenu(this._triggersSubmenu()))}_menuInternal=null;constructor(e){this._canHaveBackdrop=e;let i=f(bb,{optional:!0});this._parentMaterialMenu=i instanceof Ni?i:void 0}ngOnDestroy(){this._menu&&this._ownsMenu(this._menu)&&is.delete(this._menu),this._pendingRemoval?.unsubscribe(),this._menuCloseSubscription.unsubscribe(),this._closingActionsSubscription.unsubscribe(),this._overlayRef&&(this._overlayRef.dispose(),this._overlayRef=null)}get menuOpen(){return this._menuOpen}get dir(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_triggersSubmenu(){return!!(this._menuItemInstance&&this._parentMaterialMenu&&this._menu)}_closeMenu(){this._menu?.close.emit()}_openMenu(e){if(this._triggerIsAriaDisabled())return;let i=this._menu;if(this._menuOpen||!i)return;this._pendingRemoval?.unsubscribe();let a=is.get(i);is.set(i,this),a&&a!==this&&a._closeMenu();let o=this._createOverlay(i),r=o.getConfig(),s=r.positionStrategy;this._setPosition(i,s),this._canHaveBackdrop?r.hasBackdrop=i.hasBackdrop==null?!this._triggersSubmenu():i.hasBackdrop:r.hasBackdrop=i.hasBackdrop??!1,o.hasAttached()||(o.attach(this._getPortal(i)),i.lazyContent?.attach(this.menuData)),this._closingActionsSubscription=this._menuClosingActions().subscribe(()=>this._closeMenu()),i.parentMenu=this._triggersSubmenu()?this._parentMaterialMenu:void 0,i.direction=this.dir,e&&i.focusFirstItem(this._openedBy||`program`),this._setIsMenuOpen(!0),i instanceof Ni&&(i._setIsOpen(!0),i._directDescendantItems.changes.pipe(Te(i.close)).subscribe(()=>{s.withLockedPosition(!1).reapplyLastPosition(),s.withLockedPosition(!0)}))}focus(e,i){this._focusMonitor&&e?this._focusMonitor.focusVia(this._element,e,i):this._element.nativeElement.focus(i)}_destroyMenu(e){let i=this._overlayRef,a=this._menu;!i||!this.menuOpen||(this._closingActionsSubscription.unsubscribe(),this._pendingRemoval?.unsubscribe(),a instanceof Ni&&this._ownsMenu(a)?(this._pendingRemoval=a._animationDone.pipe(vn(1)).subscribe(()=>{i.detach(),is.has(a)||a.lazyContent?.detach()}),a._setIsOpen(!1)):(i.detach(),a?.lazyContent?.detach()),a&&this._ownsMenu(a)&&is.delete(a),this.restoreFocus&&(e===`keydown`||!this._openedBy||!this._triggersSubmenu())&&this.focus(this._openedBy),this._openedBy=void 0,this._setIsMenuOpen(!1))}_setIsMenuOpen(e){e!==this._menuOpen&&(this._menuOpen=e,this._menuOpen?this.menuOpened.emit():this.menuClosed.emit(),this._triggersSubmenu()&&this._menuItemInstance._setHighlighted(e),this._changeDetectorRef.markForCheck())}_createOverlay(e){if(!this._overlayRef){let i=this._getOverlayConfig(e);this._subscribeToPositions(e,i.positionStrategy),this._overlayRef=sa(this._injector,i),this._overlayRef.keydownEvents().subscribe(a=>{this._menu instanceof Ni&&this._menu._handleKeydown(a)})}return this._overlayRef}_getOverlayConfig(e){return new aa({positionStrategy:Bo(this._injector,this._getOverlayOrigin()).withLockedPosition().withGrowAfterOpen().withTransformOriginOn(`.mat-menu-panel, .mat-mdc-menu-panel`),backdropClass:e.backdropClass||`cdk-overlay-transparent-backdrop`,panelClass:e.overlayPanelClass,scrollStrategy:this._scrollStrategy(),direction:this._dir||`ltr`,disableAnimations:this._animationsDisabled})}_subscribeToPositions(e,i){e.setPositionClasses&&i.positionChanges.subscribe(a=>{this._ngZone.run(()=>{let o=a.connectionPair.overlayX===`start`?`after`:`before`,r=a.connectionPair.overlayY===`top`?`below`:`above`;e.setPositionClasses(o,r)})})}_setPosition(e,i){let[a,o]=e.xPosition===`before`?[`end`,`start`]:[`start`,`end`],[r,s]=e.yPosition===`above`?[`bottom`,`top`]:[`top`,`bottom`],[c,h]=[r,s],[p,g]=[a,o],v=0;if(this._triggersSubmenu()){if(g=a=e.xPosition===`before`?`start`:`end`,o=p=a===`end`?`start`:`end`,this._parentMaterialMenu){if(this._parentInnerPadding==null){let b=this._parentMaterialMenu.items.first;this._parentInnerPadding=b?b._getHostElement().offsetTop:0}v=r===`bottom`?this._parentInnerPadding:-this._parentInnerPadding}}else e.overlapTrigger||(c=r===`top`?`bottom`:`top`,h=s===`top`?`bottom`:`top`);i.withPositions([{originX:a,originY:c,overlayX:p,overlayY:r,offsetY:v},{originX:o,originY:c,overlayX:g,overlayY:r,offsetY:v},{originX:a,originY:h,overlayX:p,overlayY:s,offsetY:-v},{originX:o,originY:h,overlayX:g,overlayY:s,offsetY:-v}])}_menuClosingActions(){let e=this._getOutsideClickStream(this._overlayRef),i=this._overlayRef.detachments();return It(e,this._parentMaterialMenu?this._parentMaterialMenu.closed:Mn(),this._parentMaterialMenu?this._parentMaterialMenu._hovered().pipe(Qe(r=>this._menuOpen&&r!==this._menuItemInstance)):Mn(),i)}_getPortal(e){return(!this._portal||this._portal.templateRef!==e.templateRef)&&(this._portal=new Hn(e.templateRef,this._viewContainerRef)),this._portal}_ownsMenu(e){return is.get(e)===this}_triggerIsAriaDisabled(){return V(this._element.nativeElement.getAttribute(`aria-disabled`))}static ɵfac=function(i){Rf()};static ɵdir=K({type:t})}return t})();var as=(()=>{class t extends UO{_cleanupTouchstart;_hoverSubscription=De.EMPTY;get _deprecatedMatMenuTriggerFor(){return this.menu}set _deprecatedMatMenuTriggerFor(e){this.menu=e}get menu(){return this._menu}set menu(e){this._menu=e}menuData;restoreFocus=!0;menuOpened=new ee;onMenuOpen=this.menuOpened;menuClosed=new ee;onMenuClose=this.menuClosed;constructor(){super(!0);let e=f(Re);this._cleanupTouchstart=e.listen(this._element.nativeElement,`touchstart`,i=>{Io(i)||(this._openedBy=`touch`)},{passive:!0})}triggersSubmenu(){return super._triggersSubmenu()}toggleMenu(){return this.menuOpen?this.closeMenu():this.openMenu()}openMenu(){this._openMenu(!0)}closeMenu(){this._closeMenu()}updatePosition(){this._overlayRef?.updatePosition()}ngAfterContentInit(){this._handleHover()}ngOnDestroy(){super.ngOnDestroy(),this._cleanupTouchstart(),this._hoverSubscription.unsubscribe()}_getOverlayOrigin(){return this._element}_getOutsideClickStream(e){return e.backdropClick()}_handleMousedown(e){Do(e)||(this._openedBy=e.button===0?`mouse`:void 0,this.triggersSubmenu()&&e.preventDefault())}_handleKeydown(e){let i=e.keyCode;(i===13||i===32)&&(this._openedBy=`keyboard`),this.triggersSubmenu()&&(i===39&&this.dir===`ltr`||i===37&&this.dir===`rtl`)&&(this._openedBy=`keyboard`,this.openMenu())}_handleClick(e){this.triggersSubmenu()?(e.stopPropagation(),this.openMenu()):this.toggleMenu()}_handleHover(){this.triggersSubmenu()&&this._parentMaterialMenu&&(this._hoverSubscription=this._parentMaterialMenu._hovered().subscribe(e=>{e===this._menuItemInstance&&!e.disabled&&this._parentMaterialMenu?._panelAnimationState!==`void`&&(this._openedBy=`mouse`,this._openMenu(!1))}))}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`mat-menu-trigger-for`,``],[``,`matMenuTriggerFor`,``]],hostAttrs:[1,`mat-mdc-menu-trigger`],hostVars:3,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._handleClick(r)})(`mousedown`,function(r){return a._handleMousedown(r)})(`keydown`,function(r){return a._handleKeydown(r)}),i&2&&te(`aria-haspopup`,a.menu?`menu`:null)(`aria-expanded`,a.menuOpen)(`aria-controls`,a.menuOpen?a.menu?.panelId:null)},inputs:{_deprecatedMatMenuTriggerFor:[0,`mat-menu-trigger-for`,`_deprecatedMatMenuTriggerFor`],menu:[0,`matMenuTriggerFor`,`menu`],menuData:[0,`matMenuTriggerData`,`menuData`],restoreFocus:[0,`matMenuTriggerRestoreFocus`,`restoreFocus`]},outputs:{menuOpened:`menuOpened`,onMenuOpen:`onMenuOpen`,menuClosed:`menuClosed`,onMenuClose:`onMenuClose`},exportAs:[`matMenuTrigger`],features:[Ve]})}return t})();var os=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[ki,mi,we,ia]})}return t})();var jo=class{_multiple;_emitChanges;compareWith;_selection=new Set;_deselectedToEmit=[];_selectedToEmit=[];_selected=null;get selected(){return this._selected||(this._selected=Array.from(this._selection.values())),this._selected}changed=new L;bulk={select:n=>this._select(n),deselect:n=>this._deselect(n),setSelection:n=>this._setSelection(n)};constructor(n=!1,e,i=!0,a){this._multiple=n,this._emitChanges=i,this.compareWith=a,e&&e.length&&(n?e.forEach(o=>this._markSelected(o)):this._markSelected(e[0]),this._selectedToEmit.length=0)}select(...n){return this._select(n)}deselect(...n){return this._deselect(n)}setSelection(...n){return this._setSelection(n)}toggle(n){return this.isSelected(n)?this.deselect(n):this.select(n)}clear(n=!0){this._unmarkAll();let e=this._hasQueuedChanges();return n&&this._emitChangeEvent(),e}isSelected(n){return this._selection.has(this._getConcreteValue(n))}isEmpty(){return this._selection.size===0}hasValue(){return!this.isEmpty()}sort(n){this._multiple&&this.selected&&this._selected.sort(n)}isMultipleSelection(){return this._multiple}_select(n){this._verifyValueAssignment(n),n.forEach(i=>this._markSelected(i));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}_deselect(n){this._verifyValueAssignment(n),n.forEach(i=>this._unmarkSelected(i));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}_setSelection(n){this._verifyValueAssignment(n);let e=this.selected,i=new Set(n.map(o=>this._getConcreteValue(o)));n.forEach(o=>this._markSelected(o)),e.filter(o=>!i.has(this._getConcreteValue(o,i))).forEach(o=>this._unmarkSelected(o));let a=this._hasQueuedChanges();return this._emitChangeEvent(),a}_emitChangeEvent(){this._selected=null,(this._selectedToEmit.length||this._deselectedToEmit.length)&&(this.changed.next({source:this,added:this._selectedToEmit,removed:this._deselectedToEmit}),this._deselectedToEmit=[],this._selectedToEmit=[])}_markSelected(n){n=this._getConcreteValue(n),this.isSelected(n)||(this._multiple||this._unmarkAll(),this.isSelected(n)||this._selection.add(n),this._emitChanges&&this._selectedToEmit.push(n))}_unmarkSelected(n){n=this._getConcreteValue(n),this.isSelected(n)&&(this._selection.delete(n),this._emitChanges&&this._deselectedToEmit.push(n))}_unmarkAll(){this.isEmpty()||this._selection.forEach(n=>this._unmarkSelected(n))}_verifyValueAssignment(n){n.length>1&&this._multiple}_hasQueuedChanges(){return!!(this._deselectedToEmit.length||this._selectedToEmit.length)}_getConcreteValue(n,e){if(this.compareWith){e=e??this._selection;for(let i of e)if(this.compareWith(n,i))return i;return n}else return n}};var um=(()=>{class t{_animationsDisabled=Ue();state=`unchecked`;disabled=!1;appearance=`full`;static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`mat-pseudo-checkbox`]],hostAttrs:[1,`mat-pseudo-checkbox`],hostVars:12,hostBindings:function(i,a){i&2&&Y(`mat-pseudo-checkbox-indeterminate`,a.state===`indeterminate`)(`mat-pseudo-checkbox-checked`,a.state===`checked`)(`mat-pseudo-checkbox-disabled`,a.disabled)(`mat-pseudo-checkbox-minimal`,a.appearance===`minimal`)(`mat-pseudo-checkbox-full`,a.appearance===`full`)(`_mat-animation-noopable`,a._animationsDisabled)},inputs:{state:`state`,disabled:`disabled`,appearance:`appearance`},decls:0,vars:0,template:function(i,a){},styles:[`.mat-pseudo-checkbox {
  border-radius: 2px;
  cursor: pointer;
  display: inline-block;
  vertical-align: middle;
  box-sizing: border-box;
  position: relative;
  flex-shrink: 0;
  transition: border-color 90ms cubic-bezier(0, 0, 0.2, 0.1), background-color 90ms cubic-bezier(0, 0, 0.2, 0.1);
}
.mat-pseudo-checkbox::after {
  position: absolute;
  opacity: 0;
  content: "";
  border-bottom: 2px solid currentColor;
  transition: opacity 90ms cubic-bezier(0, 0, 0.2, 0.1);
}
.mat-pseudo-checkbox._mat-animation-noopable {
  transition: none !important;
  animation: none !important;
}
.mat-pseudo-checkbox._mat-animation-noopable::after {
  transition: none;
}

.mat-pseudo-checkbox-disabled {
  cursor: default;
}

.mat-pseudo-checkbox-indeterminate::after {
  left: 1px;
  opacity: 1;
  border-radius: 2px;
}

.mat-pseudo-checkbox-checked::after {
  left: 1px;
  border-left: 2px solid currentColor;
  transform: rotate(-45deg);
  opacity: 1;
  box-sizing: content-box;
}

.mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-checked::after, .mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-indeterminate::after {
  color: var(--%NS%mat-pseudo-checkbox-minimal-selected-checkmark-color, var(--%NS%mat-sys-primary));
}
.mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-checked.mat-pseudo-checkbox-disabled::after, .mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-indeterminate.mat-pseudo-checkbox-disabled::after {
  color: var(--%NS%mat-pseudo-checkbox-minimal-disabled-selected-checkmark-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}

.mat-pseudo-checkbox-full {
  border-color: var(--%NS%mat-pseudo-checkbox-full-unselected-icon-color, var(--%NS%mat-sys-on-surface-variant));
  border-width: 2px;
  border-style: solid;
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-disabled {
  border-color: var(--%NS%mat-pseudo-checkbox-full-disabled-unselected-icon-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-checked, .mat-pseudo-checkbox-full.mat-pseudo-checkbox-indeterminate {
  background-color: var(--%NS%mat-pseudo-checkbox-full-selected-icon-color, var(--%NS%mat-sys-primary));
  border-color: transparent;
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-checked::after, .mat-pseudo-checkbox-full.mat-pseudo-checkbox-indeterminate::after {
  color: var(--%NS%mat-pseudo-checkbox-full-selected-checkmark-color, var(--%NS%mat-sys-on-primary));
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-checked.mat-pseudo-checkbox-disabled, .mat-pseudo-checkbox-full.mat-pseudo-checkbox-indeterminate.mat-pseudo-checkbox-disabled {
  background-color: var(--%NS%mat-pseudo-checkbox-full-disabled-selected-icon-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-checked.mat-pseudo-checkbox-disabled::after, .mat-pseudo-checkbox-full.mat-pseudo-checkbox-indeterminate.mat-pseudo-checkbox-disabled::after {
  color: var(--%NS%mat-pseudo-checkbox-full-disabled-selected-checkmark-color, var(--%NS%mat-sys-surface));
}

.mat-pseudo-checkbox {
  width: 18px;
  height: 18px;
}

.mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-checked::after {
  width: 14px;
  height: 6px;
  transform-origin: center;
  top: -4.2426406871px;
  left: 0;
  bottom: 0;
  right: 0;
  margin: auto;
}
.mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-indeterminate::after {
  top: 8px;
  width: 16px;
}

.mat-pseudo-checkbox-full.mat-pseudo-checkbox-checked::after {
  width: 10px;
  height: 4px;
  transform-origin: center;
  top: -2.8284271247px;
  left: 0;
  bottom: 0;
  right: 0;
  margin: auto;
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-indeterminate::after {
  top: 6px;
  width: 12px;
}
`],encapsulation:2})}return t})();var yb=new k(`MAT_OPTION_PARENT_COMPONENT`);var xb=new k(`MatOptgroup`);var _b=class{source;isUserInput;constructor(n,e=!1){this.source=n,this.isUserInput=e}};var Un=(()=>{class t{_element=f(W);_changeDetectorRef=f(Fe);_parent=f(yb,{optional:!0});group=f(xb,{optional:!0});_signalDisableRipple=!1;_selected=!1;_active=!1;_mostRecentViewValue=``;get multiple(){return this._parent&&this._parent.multiple}get selected(){return this._selected}value;id=f(Be).getId(`mat-option-`);get disabled(){return this.group&&this.group.disabled||this._disabled()}set disabled(e){this._disabled.set(e)}_disabled=T(!1);get disableRipple(){return this._signalDisableRipple?this._parent.disableRipple():!!this._parent?.disableRipple}get hideSingleSelectionIndicator(){return!!(this._parent&&this._parent.hideSingleSelectionIndicator)}onSelectionChange=new ee;_text;_stateChanges=new L;constructor(){let e=f(Je);e.load(dn),e.load(Ii),this._signalDisableRipple=!!this._parent&&sn(this._parent.disableRipple)}get active(){return this._active}get viewValue(){return(this._text?.nativeElement.textContent||``).trim()}select(e=!0){this._selected||(this._selected=!0,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}deselect(e=!0){this._selected&&(this._selected=!1,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}focus(e,i){let a=this._getHostElement();typeof a.focus==`function`&&a.focus(i)}setActiveStyles(){this._active||(this._active=!0,this._changeDetectorRef.markForCheck())}setInactiveStyles(){this._active&&(this._active=!1,this._changeDetectorRef.markForCheck())}getLabel(){return this.viewValue}_handleKeydown(e){(e.keyCode===13||e.keyCode===32)&&!xt(e)&&(this._selectViaInteraction(),e.preventDefault())}_selectViaInteraction(){this.disabled||(this._selected=this.multiple?!this._selected:!0,this._changeDetectorRef.markForCheck(),this._emitSelectionChangeEvent(!0))}_getTabIndex(){return this.disabled?`-1`:`0`}_getHostElement(){return this._element.nativeElement}ngAfterViewChecked(){if(this._selected){let e=this.viewValue;e!==this._mostRecentViewValue&&(this._mostRecentViewValue&&this._stateChanges.next(),this._mostRecentViewValue=e)}}ngOnDestroy(){this._stateChanges.complete()}_emitSelectionChangeEvent(e=!1){this.onSelectionChange.emit(new _b(this,e))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`text`],i=[[[`mat-icon`]],`*`],a=[`mat-icon`,`*`];function o(c,h){if(c&1&&H(0,`mat-pseudo-checkbox`,1),c&2){let p=_();S(`disabled`,p.disabled)(`state`,p.selected?`checked`:`unchecked`)}}function r(c,h){if(c&1&&H(0,`mat-pseudo-checkbox`,3),c&2)S(`disabled`,_().disabled)}function s(c,h){if(c&1&&(l(0,`span`,4),u(1),d()),c&2){let p=_();m(),j(`(`,p.group.label,`)`)}}return B({type:t,selectors:[[`mat-option`]],viewQuery:function(h,p){if(h&1&&Ze(e,7),h&2){let g;Z(g=J())&&(p._text=g.first)}},hostAttrs:[`role`,`option`,1,`mat-mdc-option`,`mdc-list-item`],hostVars:11,hostBindings:function(h,p){h&1&&C(`click`,function(){return p._selectViaInteraction()})(`keydown`,function(v){return p._handleKeydown(v)}),h&2&&(qt(`id`,p.id),te(`aria-selected`,p.selected)(`aria-disabled`,p.disabled.toString()),Y(`mdc-list-item--selected`,p.selected)(`mat-mdc-option-multiple`,p.multiple)(`mat-mdc-option-active`,p.active)(`mdc-list-item--disabled`,p.disabled))},inputs:{value:`value`,id:`id`,disabled:[2,`disabled`,`disabled`,V]},outputs:{onSelectionChange:`onSelectionChange`},exportAs:[`matOption`],ngContentSelectors:a,decls:8,vars:5,consts:[[`text`,``],[`aria-hidden`,`true`,1,`mat-mdc-option-pseudo-checkbox`,3,`disabled`,`state`],[1,`mdc-list-item__primary-text`],[`state`,`checked`,`aria-hidden`,`true`,`appearance`,`minimal`,1,`mat-mdc-option-pseudo-checkbox`,3,`disabled`],[1,`cdk-visually-hidden`],[`aria-hidden`,`true`,`mat-ripple`,``,1,`mat-mdc-option-ripple`,`mat-focus-indicator`,3,`matRippleTrigger`,`matRippleDisabled`]],template:function(h,p){h&1&&(Pe(i),D(0,o,1,2,`mat-pseudo-checkbox`,1),he(1),l(2,`span`,2,0),he(4,1),d(),D(5,r,1,1,`mat-pseudo-checkbox`,3),D(6,s,2,1,`span`,4),H(7,`div`,5)),h&2&&(I(p.multiple?0:-1),m(5),I(!p.multiple&&p.selected&&!p.hideSingleSelectionIndicator?5:-1),m(),I(p.group&&p.group._inert?6:-1),m(),S(`matRippleTrigger`,p._getHostElement())(`matRippleDisabled`,p.disabled||p.disableRipple))},dependencies:[um,ci],styles:[`.mat-mdc-option {
  -webkit-user-select: none;
  user-select: none;
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  display: flex;
  position: relative;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  min-height: 48px;
  padding: 0 16px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  color: var(--%NS%mat-option-label-text-color, var(--%NS%mat-sys-on-surface));
  font-family: var(--%NS%mat-option-label-text-font, var(--%NS%mat-sys-label-large-font));
  line-height: var(--%NS%mat-option-label-text-line-height, var(--%NS%mat-sys-label-large-line-height));
  font-size: var(--%NS%mat-option-label-text-size, var(--%NS%mat-sys-body-large-size));
  letter-spacing: var(--%NS%mat-option-label-text-tracking, var(--%NS%mat-sys-label-large-tracking));
  font-weight: var(--%NS%mat-option-label-text-weight, var(--%NS%mat-sys-body-large-weight));
}
.mat-mdc-option:hover:not(.mdc-list-item--disabled) {
  background-color: var(--%NS%mat-option-hover-state-layer-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) calc(var(--%NS%mat-sys-hover-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-option:focus.mdc-list-item, .mat-mdc-option.mat-mdc-option-active.mdc-list-item {
  background-color: var(--%NS%mat-option-focus-state-layer-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) calc(var(--%NS%mat-sys-focus-state-layer-opacity) * 100%), transparent));
  outline: 0;
}
.mat-mdc-option.mdc-list-item--%NS%selected:not(.mdc-list-item--disabled):not(.mat-mdc-option-active, .mat-mdc-option-multiple, :focus, :hover) {
  background-color: var(--%NS%mat-option-selected-state-layer-color, var(--%NS%mat-sys-secondary-container));
}
.mat-mdc-option.mdc-list-item--%NS%selected:not(.mdc-list-item--disabled):not(.mat-mdc-option-active, .mat-mdc-option-multiple, :focus, :hover) .mdc-list-item__primary-text {
  color: var(--%NS%mat-option-selected-state-label-text-color, var(--%NS%mat-sys-on-secondary-container));
}
.mat-mdc-option .mat-pseudo-checkbox {
  --%NS%mat-pseudo-checkbox-minimal-selected-checkmark-color: var(--%NS%mat-option-selected-state-label-text-color, var(--%NS%mat-sys-on-secondary-container));
}
.mat-mdc-option.mdc-list-item {
  align-items: center;
  background: transparent;
}
.mat-mdc-option.mdc-list-item--disabled {
  cursor: default;
  pointer-events: none;
}
.mat-mdc-option.mdc-list-item--disabled .mat-mdc-option-pseudo-checkbox, .mat-mdc-option.mdc-list-item--disabled .mdc-list-item__primary-text, .mat-mdc-option.mdc-list-item--disabled > mat-icon {
  opacity: 0.38;
}
.mat-mdc-optgroup .mat-mdc-option:not(.mat-mdc-option-multiple) {
  padding-left: 32px;
}
[dir=rtl] .mat-mdc-optgroup .mat-mdc-option:not(.mat-mdc-option-multiple) {
  padding-left: 16px;
  padding-right: 32px;
}
.mat-mdc-option .mat-icon,
.mat-mdc-option .mat-pseudo-checkbox-full {
  margin-right: 16px;
  flex-shrink: 0;
}
[dir=rtl] .mat-mdc-option .mat-icon,
[dir=rtl] .mat-mdc-option .mat-pseudo-checkbox-full {
  margin-right: 0;
  margin-left: 16px;
}
.mat-mdc-option .mat-pseudo-checkbox-minimal {
  margin-left: 16px;
  flex-shrink: 0;
}
[dir=rtl] .mat-mdc-option .mat-pseudo-checkbox-minimal {
  margin-right: 16px;
  margin-left: 0;
}
.mat-mdc-option .mat-mdc-option-ripple {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
}
.mat-mdc-option .mdc-list-item__primary-text {
  white-space: normal;
  font-size: inherit;
  font-weight: inherit;
  letter-spacing: inherit;
  line-height: inherit;
  font-family: inherit;
  text-decoration: inherit;
  text-transform: inherit;
  margin-right: auto;
}
[dir=rtl] .mat-mdc-option .mdc-list-item__primary-text {
  margin-right: 0;
  margin-left: auto;
}
@media (forced-colors: active) {
  .mat-mdc-option.mdc-list-item--%NS%selected:not(:has(.mat-mdc-option-pseudo-checkbox))::after {
    content: "";
    position: absolute;
    top: 50%;
    right: 16px;
    transform: translateY(-50%);
    width: 10px;
    height: 0;
    border-bottom: solid 10px;
    border-radius: 10px;
  }
  [dir=rtl] .mat-mdc-option.mdc-list-item--%NS%selected:not(:has(.mat-mdc-option-pseudo-checkbox))::after {
    right: auto;
    left: 16px;
  }
}

.mat-mdc-option-multiple {
  --%NS%mat-list-list-item-selected-container-color: var(--%NS%mat-list-list-item-container-color, transparent);
}

.mat-mdc-option-active .mat-focus-indicator::before {
  content: "";
}
`],encapsulation:2})})()}return t})();function Cw(t,n,e){if(e.length){let i=n.toArray(),a=e.toArray(),o=0;for(let r=0;r<t+1;r++)i[r].group&&i[r].group===a[o]&&o++;return o}return 0}function Sw(t,n,e,i){return t<e?t:t+n>e+i?Math.max(0,t-i+n):e}var ww=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[we]})}return t})();var Cb=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[ki,ww,Un,we]})}return t})();var $O=new k(`mat-select-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t)}});var GO=new k(`MAT_SELECT_CONFIG`);var WO=new k(`MatSelectTrigger`);var Sb=class{source;value;constructor(n,e){this.source=n,this.value=e}};var Ra=(()=>{class t{_viewportRuler=f(ui);_changeDetectorRef=f(Fe);_elementRef=f(W);_dir=f(gt,{optional:!0});_idGenerator=f(Be);_renderer=f(Re);_parentFormField=f(Vc,{optional:!0});ngControl=f(Mi,{self:!0,optional:!0});_liveAnnouncer=f(Fg);_defaultOptions=f(GO,{optional:!0});_animationsDisabled=Ue();_popoverLocation;_initialized=new L;_cleanupDetach;options;optionGroups;customTrigger;_positions=[{originX:`start`,originY:`bottom`,overlayX:`start`,overlayY:`top`},{originX:`end`,originY:`bottom`,overlayX:`end`,overlayY:`top`},{originX:`start`,originY:`top`,overlayX:`start`,overlayY:`bottom`,panelClass:`mat-mdc-select-panel-above`},{originX:`end`,originY:`top`,overlayX:`end`,overlayY:`bottom`,panelClass:`mat-mdc-select-panel-above`}];_scrollOptionIntoView(e){let i=this.options.toArray()[e];if(i){let a=this.panel.nativeElement,o=Cw(e,this.options,this.optionGroups),r=i._getHostElement();e===0&&o===1?a.scrollTop=0:a.scrollTop=Sw(r.offsetTop,r.offsetHeight,a.scrollTop,a.offsetHeight)}}_positioningSettled(){this._scrollOptionIntoView(this._keyManager.activeItemIndex||0)}_getChangeEvent(e){return new Sb(this,e)}_scrollStrategyFactory=f($O);_panelOpen=!1;_compareWith=(e,i)=>e===i;_uid=this._idGenerator.getId(`mat-select-`);_triggerAriaLabelledBy=null;_previousControl;_destroy=new L;_errorStateTracker;stateChanges=new L;disableAutomaticLabeling=!0;userAriaDescribedBy;_selectionModel;_keyManager;_preferredOverlayOrigin;_overlayWidth;_onChange=()=>{};_onTouched=()=>{};_valueId=this._idGenerator.getId(`mat-select-value-`);_scrollStrategy;_overlayPanelClass=this._defaultOptions?.overlayPanelClass||``;get focused(){return this._focused||this._panelOpen}_focused=!1;controlType=`mat-select`;trigger;panel;_overlayDir;panelClass;disabled=!1;get disableRipple(){return this._disableRipple()}set disableRipple(e){this._disableRipple.set(e)}_disableRipple=T(!1);tabIndex=0;get hideSingleSelectionIndicator(){return this._hideSingleSelectionIndicator}set hideSingleSelectionIndicator(e){this._hideSingleSelectionIndicator=e,this._syncParentProperties()}_hideSingleSelectionIndicator=this._defaultOptions?.hideSingleSelectionIndicator??!1;get placeholder(){return this._placeholder}set placeholder(e){this._placeholder=e,this.stateChanges.next()}_placeholder;get required(){return this._required??this.ngControl?.control?.hasValidator(Po.required)??!1}set required(e){this._required=e,this.stateChanges.next()}_required;get multiple(){return this._multiple}set multiple(e){this._selectionModel,this._multiple=e}_multiple=!1;disableOptionCentering=this._defaultOptions?.disableOptionCentering??!1;get compareWith(){return this._compareWith}set compareWith(e){this._compareWith=e,this._selectionModel&&this._initializeSelection()}get value(){return this._value}set value(e){this._assignValue(e)&&this._onChange(e)}_value;ariaLabel=``;ariaLabelledby;get errorStateMatcher(){return this._errorStateTracker.matcher}set errorStateMatcher(e){this._errorStateTracker.matcher=e}typeaheadDebounceInterval;sortComparator;get id(){return this._id}set id(e){this._id=e||this._uid,this.stateChanges.next()}_id;get errorState(){return this._errorStateTracker.errorState}set errorState(e){this._errorStateTracker.errorState=e}panelWidth=this._defaultOptions&&typeof this._defaultOptions.panelWidth<`u`?this._defaultOptions.panelWidth:`auto`;canSelectNullableOptions=this._defaultOptions?.canSelectNullableOptions??!1;optionSelectionChanges=Ka(()=>{let e=this.options;return e?e.changes.pipe(mt(e),_n(()=>It(...e.map(i=>i.onSelectionChange)))):this._initialized.pipe(_n(()=>this.optionSelectionChanges))});openedChange=new ee;_openedStream=this.openedChange.pipe(Qe(e=>e),st(()=>{}));_closedStream=this.openedChange.pipe(Qe(e=>!e),st(()=>{}));selectionChange=new ee;valueChange=new ee;constructor(){let e=f(Yr),i=f(Rc,{optional:!0}),a=f(Pc,{optional:!0}),o=f(new Ki(`tabindex`),{optional:!0}),r=f(Gc,{optional:!0}),s=f(Qu,{optional:!0,self:!0});this.ngControl&&(this.ngControl.valueAccessor=this),this._defaultOptions?.typeaheadDebounceInterval!=null&&(this.typeaheadDebounceInterval=this._defaultOptions.typeaheadDebounceInterval),this._errorStateTracker=new Kr(e,s||this.ngControl,a,i,this.stateChanges),this._scrollStrategy=this._scrollStrategyFactory(),this.tabIndex=o==null?0:parseInt(o)||0,this._popoverLocation=r?.usePopover===!1?null:`inline`,this.id=this.id}ngOnInit(){this._selectionModel=new jo(this.multiple),this.stateChanges.next(),this._viewportRuler.change().pipe(Te(this._destroy)).subscribe(()=>{this.panelOpen&&(this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._changeDetectorRef.detectChanges())})}ngAfterContentInit(){this._initialized.next(),this._initialized.complete(),this._initKeyManager(),this._selectionModel.changed.pipe(Te(this._destroy)).subscribe(e=>{e.added.forEach(i=>i.select()),e.removed.forEach(i=>i.deselect())}),this.options.changes.pipe(mt(null),Te(this._destroy)).subscribe(()=>{this._resetOptions(),this._initializeSelection()})}ngDoCheck(){let e=this._getTriggerAriaLabelledby(),i=this.ngControl;if(e!==this._triggerAriaLabelledBy){let a=this._elementRef.nativeElement;this._triggerAriaLabelledBy=e,e?a.setAttribute(`aria-labelledby`,e):a.removeAttribute(`aria-labelledby`)}i&&(this._previousControl!==i.control&&(this._previousControl!==void 0&&i.disabled!==null&&i.disabled!==this.disabled&&(this.disabled=i.disabled),this._previousControl=i.control),this.updateErrorState())}ngOnChanges(e){(e.disabled||e.userAriaDescribedBy)&&this.stateChanges.next(),e.typeaheadDebounceInterval&&this._keyManager&&this._keyManager.withTypeAhead(this.typeaheadDebounceInterval),e.panelClass&&this.panelClass instanceof Set&&(this.panelClass=Array.from(this.panelClass))}ngOnDestroy(){this._cleanupDetach?.(),this._keyManager?.destroy(),this._destroy.next(),this._destroy.complete(),this.stateChanges.complete()}toggle(){this.panelOpen?this.close():this.open()}open(){this._canOpen()&&(this._parentFormField&&(this._preferredOverlayOrigin=this._parentFormField.getConnectedOverlayOrigin()),this._cleanupDetach?.(),this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._panelOpen=!0,this._overlayDir.positionChange.pipe(vn(1)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this._positioningSettled()}),this._overlayDir.attachOverlay(),this._keyManager.withHorizontalOrientation(null),this._highlightCorrectOption(),this._changeDetectorRef.markForCheck(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!0)))}close(){this._panelOpen&&(this._panelOpen=!1,this._exitAndDetach(),this._keyManager.withHorizontalOrientation(this._isRtl()?`rtl`:`ltr`),this._changeDetectorRef.markForCheck(),this._onTouched(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!1)))}_exitAndDetach(){if(this._animationsDisabled||!this.panel){this._detachOverlay();return}this._cleanupDetach?.(),this._cleanupDetach=()=>{i(),clearTimeout(a),this._cleanupDetach=void 0};let e=this.panel.nativeElement,i=this._renderer.listen(e,`animationend`,o=>{o.animationName===`_mat-select-exit`&&(this._cleanupDetach?.(),this._detachOverlay())}),a=setTimeout(()=>{this._cleanupDetach?.(),this._detachOverlay()},200);e.classList.add(`mat-select-panel-exit`)}_detachOverlay(){this._overlayDir.detachOverlay(),this._changeDetectorRef.markForCheck()}writeValue(e){this._assignValue(e)}registerOnChange(e){this._onChange=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e,this._changeDetectorRef.markForCheck(),this.stateChanges.next()}get panelOpen(){return this._panelOpen}get selected(){return this.multiple?this._selectionModel?.selected||[]:this._selectionModel?.selected[0]}get triggerValue(){if(this.empty)return``;if(this._multiple){let e=this._selectionModel.selected.map(i=>i.viewValue);return this._isRtl()&&e.reverse(),e.join(`, `)}return this._selectionModel.selected[0].viewValue}updateErrorState(){this._errorStateTracker.updateErrorState()}_isRtl(){return this._dir?this._dir.value===`rtl`:!1}_handleKeydown(e){this.disabled||(this.panelOpen?this._handleOpenKeydown(e):this._handleClosedKeydown(e))}_handleClosedKeydown(e){let i=e.keyCode,a=i===40||i===38||i===37||i===39,o=i===13||i===32,r=this._keyManager;if(!r.isTyping()&&o&&!xt(e)||(this.multiple||e.altKey)&&a)e.preventDefault(),this.open();else if(!this.multiple){let s=this.selected;r.onKeydown(e);let c=this.selected;c&&s!==c&&this._liveAnnouncer.announce(c.viewValue,1e4)}}_handleOpenKeydown(e){let i=this._keyManager,a=e.keyCode,o=a===40||a===38,r=i.isTyping();if(o&&e.altKey)e.preventDefault(),this.close();else if(!r&&(a===13||a===32)&&i.activeItem&&!xt(e))e.preventDefault(),i.activeItem._selectViaInteraction();else if(!r&&this._multiple&&a===65&&e.ctrlKey){e.preventDefault();let s=this.options.some(c=>!c.disabled&&!c.selected);this.options.forEach(c=>{c.disabled||(s?c.select():c.deselect())})}else{let s=i.activeItemIndex;i.onKeydown(e),this._multiple&&o&&e.shiftKey&&i.activeItem&&i.activeItemIndex!==s&&i.activeItem._selectViaInteraction()}}_handleOverlayKeydown(e){e.keyCode===27&&!xt(e)&&(e.preventDefault(),this.close())}_onFocus(){this.disabled||(this._focused=!0,this.stateChanges.next())}_onBlur(){this._focused=!1,this._keyManager?.cancelTypeahead(),!this.disabled&&!this.panelOpen&&(this._onTouched(),this._changeDetectorRef.markForCheck(),this.stateChanges.next())}get empty(){return!this._selectionModel||this._selectionModel.isEmpty()}_initializeSelection(){Promise.resolve().then(()=>{this.ngControl&&(this._value=this.ngControl.value),this._setSelectionByValue(this._value),this.stateChanges.next()})}_setSelectionByValue(e){if(this.options.forEach(i=>i.setInactiveStyles()),this._selectionModel.clear(),this.multiple&&e)e.forEach(i=>this._selectOptionByValue(i)),this._sortValues();else{let i=this._selectOptionByValue(e);i?this._keyManager.updateActiveItem(i):this.panelOpen||this._keyManager.updateActiveItem(-1)}this._changeDetectorRef.markForCheck()}_selectOptionByValue(e){let i=this.options.find(a=>{if(this._selectionModel.isSelected(a))return!1;try{return(a.value!=null||this.canSelectNullableOptions)&&this._compareWith(a.value,e)}catch{return!1}});return i&&this._selectionModel.select(i),i}_assignValue(e){return e!==this._value||this._multiple&&Array.isArray(e)?(this.options&&this._setSelectionByValue(e),this._value=e,!0):!1}_skipPredicate=e=>this.panelOpen?!1:e.disabled;_getOverlayWidth(e){return this.panelWidth===`auto`?(e instanceof es?e.elementRef:e||this._elementRef).nativeElement.getBoundingClientRect().width:this.panelWidth===null?``:this.panelWidth}_syncParentProperties(){if(this.options)for(let e of this.options)e._changeDetectorRef.markForCheck()}_initKeyManager(){this._keyManager=new Ec(this.options).withTypeAhead(this.typeaheadDebounceInterval).withVerticalOrientation().withHorizontalOrientation(this._isRtl()?`rtl`:`ltr`).withHomeAndEnd().withPageUpDown().withAllowedModifierKeys([`shiftKey`]).skipPredicate(this._skipPredicate),this._keyManager.tabOut.subscribe(()=>{this.panelOpen&&(!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction(),this.focus(),this.close())}),this._keyManager.change.subscribe(()=>{this._panelOpen&&this.panel?this._scrollOptionIntoView(this._keyManager.activeItemIndex||0):!this._panelOpen&&!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction()})}_resetOptions(){let e=It(this.options.changes,this._destroy);this.optionSelectionChanges.pipe(Te(e)).subscribe(i=>{this._onSelect(i.source,i.isUserInput),i.isUserInput&&!this.multiple&&this._panelOpen&&(this.close(),this.focus())}),It(...this.options.map(i=>i._stateChanges)).pipe(Te(e)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this.stateChanges.next()})}_onSelect(e,i){let a=this._selectionModel.isSelected(e);!this.canSelectNullableOptions&&e.value==null&&!this._multiple?(e.deselect(),this._selectionModel.clear(),this.value!=null&&this._propagateChanges(e.value)):(a!==e.selected&&(e.selected?this._selectionModel.select(e):this._selectionModel.deselect(e)),i&&this._keyManager.setActiveItem(e),this.multiple&&(this._sortValues(),i&&this.focus())),a!==this._selectionModel.isSelected(e)&&this._propagateChanges(),this.stateChanges.next()}_sortValues(){if(this.multiple){let e=this.options.toArray();this._selectionModel.sort((i,a)=>this.sortComparator?this.sortComparator(i,a,e):e.indexOf(i)-e.indexOf(a)),this.stateChanges.next()}}_propagateChanges(e){let i;this.multiple?i=this.selected.map(a=>a.value):i=this.selected?this.selected.value:e,this._value=i,this.valueChange.emit(i),this._onChange(i),this.selectionChange.emit(this._getChangeEvent(i)),this._changeDetectorRef.markForCheck()}_highlightCorrectOption(){if(this._keyManager)if(this.empty){let e=-1;for(let i=0;i<this.options.length;i++)if(!this.options.get(i).disabled){e=i;break}this._keyManager.setActiveItem(e)}else this._keyManager.setActiveItem(this._selectionModel.selected[0])}_canOpen(){return!this._panelOpen&&!this.disabled&&this.options?.length>0&&!!this._overlayDir}focus(e){this._elementRef.nativeElement.focus(e)}_getPanelAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||null,i=e?e+` `:``;return this.ariaLabelledby?i+this.ariaLabelledby:e}_getAriaActiveDescendant(){return this.panelOpen&&this._keyManager&&this._keyManager.activeItem?this._keyManager.activeItem.id:null}_getTriggerAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||``;return this.ariaLabelledby&&(e+=` `+this.ariaLabelledby),e||(e=this._valueId),e}get describedByIds(){return this._elementRef.nativeElement.getAttribute(`aria-describedby`)?.split(` `)||[]}setDescribedByIds(e){let i=this._elementRef.nativeElement;e.length?i.setAttribute(`aria-describedby`,e.join(` `)):i.removeAttribute(`aria-describedby`)}onContainerClick(e){let i=Yt(e);i&&(i.tagName===`MAT-OPTION`||i.classList.contains(`cdk-overlay-backdrop`)||i.closest(`.mat-mdc-select-panel`))||(this.focus(),this.open())}get shouldLabelFloat(){return this.panelOpen||!this.empty||this.focused&&!!this.placeholder}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`trigger`],i=[`panel`],a=[[[`mat-select-trigger`]],`*`],o=[`mat-select-trigger`,`*`];function r(g,v){if(g&1&&(l(0,`span`,4),u(1),d()),g&2){let b=_();m(),w(b.placeholder)}}function s(g,v){g&1&&he(0)}function c(g,v){if(g&1&&(l(0,`span`,11),u(1),d()),g&2){let b=_(2);m(),w(b.triggerValue)}}function h(g,v){if(g&1&&(l(0,`span`,5),D(1,s,1,0)(2,c,2,1,`span`,11),d()),g&2){let b=_();m(),I(b.customTrigger?1:2)}}function p(g,v){if(g&1){let b=q();l(0,`div`,12,1),C(`keydown`,function(E){N(b);return A(_()._handleKeydown(E))}),he(2,1),d()}if(g&2){let b=_();tt(b.panelClass),Y(`mat-select-panel-animations-enabled`,!b._animationsDisabled)(`mat-primary`,b._parentFormField?.color===`primary`)(`mat-accent`,b._parentFormField?.color===`accent`)(`mat-warn`,b._parentFormField?.color===`warn`)(`mat-undefined`,!b._parentFormField?.color),te(`id`,b.id+`-panel`)(`aria-multiselectable`,b.multiple)(`aria-label`,b.ariaLabel||null)(`aria-labelledby`,b._getPanelAriaLabelledby())}}return B({type:t,selectors:[[`mat-select`]],contentQueries:function(v,b,y){if(v&1&&jt(y,WO,5)(y,Un,5)(y,xb,5),v&2){let E;Z(E=J())&&(b.customTrigger=E.first),Z(E=J())&&(b.options=E),Z(E=J())&&(b.optionGroups=E)}},viewQuery:function(v,b){if(v&1&&Ze(e,5)(i,5)(lm,5),v&2){let y;Z(y=J())&&(b.trigger=y.first),Z(y=J())&&(b.panel=y.first),Z(y=J())&&(b._overlayDir=y.first)}},hostAttrs:[`role`,`combobox`,`aria-haspopup`,`listbox`,1,`mat-mdc-select`],hostVars:21,hostBindings:function(v,b){v&1&&C(`keydown`,function(E){return b._handleKeydown(E)})(`focus`,function(){return b._onFocus()})(`blur`,function(){return b._onBlur()}),v&2&&(te(`id`,b.id)(`tabindex`,b.disabled?-1:b.tabIndex)(`aria-controls`,b.panelOpen?b.id+`-panel`:null)(`aria-expanded`,b.panelOpen)(`aria-label`,b.ariaLabel||null)(`aria-required`,b.required.toString())(`aria-disabled`,b.disabled.toString())(`aria-invalid`,b.errorState)(`aria-activedescendant`,b._getAriaActiveDescendant()),Y(`mat-mdc-select-disabled`,b.disabled)(`mat-mdc-select-invalid`,b.errorState)(`mat-mdc-select-required`,b.required)(`mat-mdc-select-empty`,b.empty)(`mat-mdc-select-multiple`,b.multiple)(`mat-select-open`,b.panelOpen))},inputs:{userAriaDescribedBy:[0,`aria-describedby`,`userAriaDescribedBy`],panelClass:`panelClass`,disabled:[2,`disabled`,`disabled`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V],tabIndex:[2,`tabIndex`,`tabIndex`,g=>g==null?0:lt(g)],hideSingleSelectionIndicator:[2,`hideSingleSelectionIndicator`,`hideSingleSelectionIndicator`,V],placeholder:`placeholder`,required:[2,`required`,`required`,V],multiple:[2,`multiple`,`multiple`,V],disableOptionCentering:[2,`disableOptionCentering`,`disableOptionCentering`,V],compareWith:`compareWith`,value:`value`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],errorStateMatcher:`errorStateMatcher`,typeaheadDebounceInterval:[2,`typeaheadDebounceInterval`,`typeaheadDebounceInterval`,lt],sortComparator:`sortComparator`,id:`id`,panelWidth:`panelWidth`,canSelectNullableOptions:[2,`canSelectNullableOptions`,`canSelectNullableOptions`,V]},outputs:{openedChange:`openedChange`,_openedStream:`opened`,_closedStream:`closed`,selectionChange:`selectionChange`,valueChange:`valueChange`},exportAs:[`matSelect`],features:[Ge([{provide:Lc,useExisting:t},{provide:yb,useExisting:t}]),vt],ngContentSelectors:o,decls:11,vars:10,consts:[[`fallbackOverlayOrigin`,`cdkOverlayOrigin`,`trigger`,``],[`panel`,``],[`cdk-overlay-origin`,``,1,`mat-mdc-select-trigger`,3,`click`],[1,`mat-mdc-select-value`],[1,`mat-mdc-select-placeholder`,`mat-mdc-select-min-line`],[1,`mat-mdc-select-value-text`],[1,`mat-mdc-select-arrow-wrapper`],[1,`mat-mdc-select-arrow`],[`viewBox`,`0 0 24 24`,`width`,`24px`,`height`,`24px`,`focusable`,`false`,`aria-hidden`,`true`],[`d`,`M7 10l5 5 5-5z`],[`cdk-connected-overlay`,``,`cdkConnectedOverlayHasBackdrop`,``,`cdkConnectedOverlayBackdropClass`,`cdk-overlay-transparent-backdrop`,3,`detach`,`backdropClick`,`overlayKeydown`,`cdkConnectedOverlayDisableClose`,`cdkConnectedOverlayPanelClass`,`cdkConnectedOverlayScrollStrategy`,`cdkConnectedOverlayOrigin`,`cdkConnectedOverlayPositions`,`cdkConnectedOverlayWidth`,`cdkConnectedOverlayFlexibleDimensions`,`cdkConnectedOverlayUsePopover`],[1,`mat-mdc-select-min-line`],[`role`,`listbox`,`tabindex`,`-1`,1,`mat-mdc-select-panel`,`mdc-menu-surface`,`mdc-menu-surface--open`,3,`keydown`]],template:function(v,b){if(v&1&&(Pe(a),l(0,`div`,2,0),C(`click`,function(){return b.open()}),l(3,`div`,3),D(4,r,2,1,`span`,4)(5,h,3,1,`span`,5),d(),l(6,`div`,6)(7,`div`,7),Gt(),l(8,`svg`,8),H(9,`path`,9),d()()()(),ot(10,p,3,16,`ng-template`,10),C(`detach`,function(){return b.close()})(`backdropClick`,function(){return b.close()})(`overlayKeydown`,function(E){return b._handleOverlayKeydown(E)})),v&2){let y=ft(1);m(3),te(`id`,b._valueId),m(),I(b.empty?4:5),m(6),S(`cdkConnectedOverlayDisableClose`,!0)(`cdkConnectedOverlayPanelClass`,b._overlayPanelClass)(`cdkConnectedOverlayScrollStrategy`,b._scrollStrategy)(`cdkConnectedOverlayOrigin`,b._preferredOverlayOrigin||y)(`cdkConnectedOverlayPositions`,b._positions)(`cdkConnectedOverlayWidth`,b._overlayWidth)(`cdkConnectedOverlayFlexibleDimensions`,!0)(`cdkConnectedOverlayUsePopover`,b._popoverLocation)}},dependencies:[es,lm],styles:[`@keyframes _mat-select-enter {
  from {
    opacity: 0;
    transform: scaleY(0.8);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
@keyframes _mat-select-exit {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}
.mat-mdc-select {
  display: inline-block;
  width: 100%;
  outline: none;
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  color: var(--%NS%mat-select-enabled-trigger-text-color, var(--%NS%mat-sys-on-surface));
  font-family: var(--%NS%mat-select-trigger-text-font, var(--%NS%mat-sys-body-large-font));
  line-height: var(--%NS%mat-select-trigger-text-line-height, var(--%NS%mat-sys-body-large-line-height));
  font-size: var(--%NS%mat-select-trigger-text-size, var(--%NS%mat-sys-body-large-size));
  font-weight: var(--%NS%mat-select-trigger-text-weight, var(--%NS%mat-sys-body-large-weight));
  letter-spacing: var(--%NS%mat-select-trigger-text-tracking, var(--%NS%mat-sys-body-large-tracking));
}

div.mat-mdc-select-panel {
  box-shadow: var(--%NS%mat-select-container-elevation-shadow, 0px 3px 1px -2px rgba(0, 0, 0, 0.2), 0px 2px 2px 0px rgba(0, 0, 0, 0.14), 0px 1px 5px 0px rgba(0, 0, 0, 0.12));
}

.mat-mdc-select-disabled {
  color: var(--%NS%mat-select-disabled-trigger-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-mdc-select-disabled .mat-mdc-select-placeholder {
  color: var(--%NS%mat-select-disabled-trigger-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}

.mat-mdc-select-trigger {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  position: relative;
  box-sizing: border-box;
  width: 100%;
}
.mat-mdc-select-disabled .mat-mdc-select-trigger {
  -webkit-user-select: none;
  user-select: none;
  cursor: default;
}

.mat-mdc-select-value {
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mat-mdc-select-value-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mat-mdc-select-arrow-wrapper {
  height: 24px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
}
.mat-form-field-appearance-fill .mdc-text-field--no-label .mat-mdc-select-arrow-wrapper {
  transform: none;
}

.mat-mdc-form-field .mat-mdc-select.mat-mdc-select-invalid .mat-mdc-select-arrow,
.mat-form-field-invalid:not(.mat-form-field-disabled) .mat-mdc-form-field-infix::after {
  color: var(--%NS%mat-select-invalid-arrow-color, var(--%NS%mat-sys-error));
}

.mat-mdc-select-arrow {
  width: 10px;
  height: 5px;
  position: relative;
  color: var(--%NS%mat-select-enabled-arrow-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-form-field.mat-focused .mat-mdc-select-arrow {
  color: var(--%NS%mat-select-focused-arrow-color, var(--%NS%mat-sys-primary));
}
.mat-mdc-form-field .mat-mdc-select.mat-mdc-select-disabled .mat-mdc-select-arrow {
  color: var(--%NS%mat-select-disabled-arrow-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-select-open .mat-mdc-select-arrow {
  transform: rotate(180deg);
}
.mat-form-field-animations-enabled .mat-mdc-select-arrow {
  transition: transform 80ms linear;
}
.mat-mdc-select-arrow svg {
  fill: currentColor;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
@media (forced-colors: active) {
  .mat-mdc-select-arrow svg {
    fill: CanvasText;
  }
  .mat-mdc-select-disabled .mat-mdc-select-arrow svg {
    fill: GrayText;
  }
}

div.mat-mdc-select-panel {
  width: 100%;
  max-height: 275px;
  outline: 0;
  overflow: auto;
  padding: 8px 0;
  box-sizing: border-box;
  transform-origin: top center;
  border-radius: 0 0 4px 4px;
  position: relative;
  background-color: var(--%NS%mat-select-panel-background-color, var(--%NS%mat-sys-surface-container));
}
.mat-mdc-select-panel-above div.mat-mdc-select-panel {
  border-radius: 4px 4px 0 0;
  transform-origin: bottom center;
}
@media (forced-colors: active) {
  div.mat-mdc-select-panel {
    outline: solid 1px;
  }
}

.mat-select-panel-animations-enabled {
  animation: _mat-select-enter 120ms cubic-bezier(0, 0, 0.2, 1) forwards;
}
.mat-select-panel-animations-enabled.mat-select-panel-exit {
  animation: _mat-select-exit 100ms linear forwards;
}

.mat-mdc-select-placeholder {
  transition: color 400ms 133.3333333333ms cubic-bezier(0.25, 0.8, 0.25, 1);
  color: var(--%NS%mat-select-placeholder-text-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-form-field:not(.mat-form-field-animations-enabled) .mat-mdc-select-placeholder, ._mat-animation-noopable .mat-mdc-select-placeholder {
  transition: none;
}
.mat-form-field-hide-placeholder .mat-mdc-select-placeholder {
  color: transparent;
  -webkit-text-fill-color: transparent;
  transition: none;
  display: block;
}

.mat-mdc-form-field-type-mat-select:not(.mat-form-field-disabled) .mat-mdc-text-field-wrapper {
  cursor: pointer;
}
.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-fill .mat-mdc-floating-label {
  max-width: calc(100% - 18px);
}
.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-fill .mdc-floating-label--float-above {
  max-width: calc(100% / 0.75 - 24px);
}
.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-outline .mdc-notched-outline__notch {
  max-width: calc(100% - 60px);
}
.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-outline .mdc-text-field--label-floating .mdc-notched-outline__notch {
  max-width: calc(100% - 24px);
}

.mat-mdc-select-min-line:empty::before {
  content: " ";
  white-space: pre;
  width: 1px;
  display: inline-block;
  visibility: hidden;
}

.mat-form-field-appearance-fill .mat-mdc-select-arrow-wrapper {
  transform: var(--%NS%mat-select-arrow-transform, translateY(-8px));
}
`],encapsulation:2})})()}return t})();var Pa=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[mi,Cb,we,ia,di,Cb]})}return t})();var qO=20;var YO=new k(`mat-tooltip-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t,{scrollThrottle:qO})}});var KO=new k(`mat-tooltip-default-options`,{providedIn:`root`,factory:()=>({showDelay:0,hideDelay:0,touchendHideDelay:1500})});var Ew=`tooltip-panel`;var QO={passive:!0};var XO=8;var ZO=8;var JO=24;var eR=200;var Dw=(()=>{class t{_elementRef=f(W);_ngZone=f(X);_platform=f(Oe);_ariaDescriber=f(Nu);_focusMonitor=f(Jt);_dir=f(gt);_injector=f(se);_viewContainerRef=f(Bt);_mediaMatcher=f(Fr);_document=f(de);_renderer=f(Re);_animationsDisabled=Ue();_defaultOptions=f(KO,{optional:!0});_overlayRef=null;_tooltipInstance=null;_overlayPanelClass;_portal;_position=`below`;_positionAtOrigin=!1;_disabled=!1;_tooltipClass;_viewInitialized=!1;_pointerExitEventsInitialized=!1;_tooltipComponent=tR;_viewportMargin=8;_currentPosition;_cssClassPrefix=`mat-mdc`;_ariaDescriptionPending=!1;_dirSubscribed=!1;get position(){return this._position}set position(e){e!==this._position&&(this._position=e,this._overlayRef&&(this._updatePosition(this._overlayRef),this._tooltipInstance?.show(0),this._overlayRef.updatePosition()))}get positionAtOrigin(){return this._positionAtOrigin}set positionAtOrigin(e){this._positionAtOrigin=ta(e),this._detach(),this._overlayRef=null}get disabled(){return this._disabled}set disabled(e){let i=ta(e);this._disabled!==i&&(this._disabled=i,i?this.hide(0):this._setupPointerEnterEventsIfNeeded(),this._syncAriaDescription(this.message))}get showDelay(){return this._showDelay}set showDelay(e){this._showDelay=ri(e)}_showDelay;get hideDelay(){return this._hideDelay}set hideDelay(e){this._hideDelay=ri(e),this._tooltipInstance&&(this._tooltipInstance._mouseLeaveHideDelay=this._hideDelay)}_hideDelay;touchGestures=`auto`;get message(){return this._message}set message(e){let i=this._message;this._message=e!=null?String(e).trim():``,!this._message&&this._isTooltipVisible()?this.hide(0):(this._setupPointerEnterEventsIfNeeded(),this._updateTooltipMessage()),this._syncAriaDescription(i)}_message=``;get tooltipClass(){return this._tooltipClass}set tooltipClass(e){this._tooltipClass=e,this._tooltipInstance&&this._setTooltipClass(this._tooltipClass)}_eventCleanups=[];_touchstartTimeout=null;_destroyed=new L;_isDestroyed=!1;constructor(){let e=this._defaultOptions;e&&(this._showDelay=e.showDelay,this._hideDelay=e.hideDelay,e.position&&(this.position=e.position),e.positionAtOrigin&&(this.positionAtOrigin=e.positionAtOrigin),e.touchGestures&&(this.touchGestures=e.touchGestures),e.tooltipClass&&(this.tooltipClass=e.tooltipClass)),this._viewportMargin=XO}ngAfterViewInit(){this._viewInitialized=!0,this._setupPointerEnterEventsIfNeeded(),this._focusMonitor.monitor(this._elementRef).pipe(Te(this._destroyed)).subscribe(e=>{e?e===`keyboard`&&this._ngZone.run(()=>this.show()):this._ngZone.run(()=>this.hide(0))})}ngOnDestroy(){let e=this._elementRef.nativeElement;this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this._overlayRef&&(this._overlayRef.dispose(),this._tooltipInstance=null),this._eventCleanups.forEach(i=>i()),this._eventCleanups.length=0,this._destroyed.next(),this._destroyed.complete(),this._isDestroyed=!0,this._ariaDescriber.removeDescription(e,this.message,`tooltip`),this._focusMonitor.stopMonitoring(e)}show(e=this.showDelay,i){if(this.disabled||!this.message||this._isTooltipVisible()){this._tooltipInstance?._cancelPendingAnimations();return}let a=this._createOverlay(i);this._detach(),this._portal=this._portal||new Na(this._tooltipComponent,this._viewContainerRef);let o=this._tooltipInstance=a.attach(this._portal).instance;o._triggerElement=this._elementRef.nativeElement,o._mouseLeaveHideDelay=this._hideDelay,o.afterHidden().pipe(Te(this._destroyed)).subscribe(()=>this._detach()),this._setTooltipClass(this._tooltipClass),this._updateTooltipMessage(),o.show(e)}hide(e=this.hideDelay){let i=this._tooltipInstance;i&&(i.isVisible()?i.hide(e):(i._cancelPendingAnimations(),this._detach()))}toggle(e){this._isTooltipVisible()?this.hide():this.show(void 0,e)}_isTooltipVisible(){return!!this._tooltipInstance&&this._tooltipInstance.isVisible()}_createOverlay(e){if(this._overlayRef){let r=this._overlayRef.getConfig().positionStrategy;if((!this.positionAtOrigin||!e)&&r._origin instanceof W)return this._overlayRef;this._detach()}let i=this._injector.get(Oa).getAncestorScrollContainers(this._elementRef),a=`${this._cssClassPrefix}-${Ew}`,o=Bo(this._injector,this.positionAtOrigin?e||this._elementRef:this._elementRef).withTransformOriginOn(`.${this._cssClassPrefix}-tooltip`).withFlexibleDimensions(!1).withViewportMargin(this._viewportMargin).withScrollableContainers(i).withPopoverLocation(`global`);return o.positionChanges.pipe(Te(this._destroyed)).subscribe(r=>{this._updateCurrentPositionClass(r.connectionPair),this._tooltipInstance&&r.scrollableViewProperties.isOverlayClipped&&this._tooltipInstance.isVisible()&&this._ngZone.run(()=>this.hide(0))}),this._overlayRef=sa(this._injector,{direction:this._dir,positionStrategy:o,panelClass:this._overlayPanelClass?[...this._overlayPanelClass,a]:a,scrollStrategy:this._injector.get(YO)(),disableAnimations:this._animationsDisabled,eventPredicate:this._overlayEventPredicate}),this._updatePosition(this._overlayRef),this._overlayRef.detachments().pipe(Te(this._destroyed)).subscribe(()=>this._detach()),this._overlayRef.outsidePointerEvents().pipe(Te(this._destroyed)).subscribe(()=>this._tooltipInstance?._handleBodyInteraction()),this._overlayRef.keydownEvents().pipe(Te(this._destroyed)).subscribe(r=>{r.preventDefault(),r.stopPropagation(),this._ngZone.run(()=>this.hide(0))}),this._defaultOptions?.disableTooltipInteractivity&&this._overlayRef.addPanelClass(`${this._cssClassPrefix}-tooltip-panel-non-interactive`),this._dirSubscribed||(this._dirSubscribed=!0,this._dir.change.pipe(Te(this._destroyed)).subscribe(()=>{this._overlayRef&&this._updatePosition(this._overlayRef)})),this._overlayRef}_detach(){this._overlayRef&&this._overlayRef.hasAttached()&&this._overlayRef.detach(),this._tooltipInstance=null}_updatePosition(e){let i=e.getConfig().positionStrategy,a=this._getOrigin(),o=this._getOverlayPosition();i.withPositions([this._addOffset(r(r({},a.main),o.main)),this._addOffset(r(r({},a.fallback),o.fallback))])}_addOffset(e){let i=ZO,a=!this._dir||this._dir.value==`ltr`;return e.originY===`top`?e.offsetY=-i:e.originY===`bottom`?e.offsetY=i:e.originX===`start`?e.offsetX=a?-i:i:e.originX===`end`&&(e.offsetX=a?i:-i),e}_getOrigin(){let e=!this._dir||this._dir.value==`ltr`,i=this.position,a;i==`above`||i==`below`?a={originX:`center`,originY:i==`above`?`top`:`bottom`}:i==`before`||i==`left`&&e||i==`right`&&!e?a={originX:`start`,originY:`center`}:(i==`after`||i==`right`&&e||i==`left`&&!e)&&(a={originX:`end`,originY:`center`});let{x:o,y:r}=this._invertPosition(a.originX,a.originY);return{main:a,fallback:{originX:o,originY:r}}}_getOverlayPosition(){let e=!this._dir||this._dir.value==`ltr`,i=this.position,a;i==`above`?a={overlayX:`center`,overlayY:`bottom`}:i==`below`?a={overlayX:`center`,overlayY:`top`}:i==`before`||i==`left`&&e||i==`right`&&!e?a={overlayX:`end`,overlayY:`center`}:(i==`after`||i==`right`&&e||i==`left`&&!e)&&(a={overlayX:`start`,overlayY:`center`});let{x:o,y:r}=this._invertPosition(a.overlayX,a.overlayY);return{main:a,fallback:{overlayX:o,overlayY:r}}}_updateTooltipMessage(){this._tooltipInstance&&(this._tooltipInstance.message=this.message,this._tooltipInstance._markForCheck(),At(()=>{this._tooltipInstance&&this._overlayRef.updatePosition()},{injector:this._injector}))}_setTooltipClass(e){this._tooltipInstance&&(this._tooltipInstance.tooltipClass=e instanceof Set?Array.from(e):e,this._tooltipInstance._markForCheck())}_invertPosition(e,i){return this.position===`above`||this.position===`below`?i===`top`?i=`bottom`:i===`bottom`&&(i=`top`):e===`end`?e=`start`:e===`start`&&(e=`end`),{x:e,y:i}}_updateCurrentPositionClass(e){let{overlayY:i,originX:a,originY:o}=e,r;if(i===`center`?this._dir&&this._dir.value===`rtl`?r=a===`end`?`left`:`right`:r=a===`start`?`left`:`right`:r=i===`bottom`&&o===`top`?`above`:`below`,r!==this._currentPosition){let s=this._overlayRef;if(s){let c=`${this._cssClassPrefix}-${Ew}-`;s.removePanelClass(c+this._currentPosition),s.addPanelClass(c+r)}this._currentPosition=r}}_setupPointerEnterEventsIfNeeded(){this._disabled||!this.message||!this._viewInitialized||this._eventCleanups.length||(this._isTouchPlatform()?this.touchGestures!==`off`&&(this._disableNativeGesturesIfNecessary(),this._addListener(`touchstart`,e=>{let i=e.targetTouches?.[0],a=i?{x:i.clientX,y:i.clientY}:void 0;this._setupPointerExitEventsIfNeeded(),this._touchstartTimeout&&clearTimeout(this._touchstartTimeout);let o=500;this._touchstartTimeout=setTimeout(()=>{this._touchstartTimeout=null,this.show(void 0,a)},this._defaultOptions?.touchLongPressShowDelay??o)})):this._addListener(`mouseenter`,e=>{this._setupPointerExitEventsIfNeeded();let i;e.x!==void 0&&e.y!==void 0&&(i=e),this.show(void 0,i)}))}_setupPointerExitEventsIfNeeded(){if(!this._pointerExitEventsInitialized){if(this._pointerExitEventsInitialized=!0,!this._isTouchPlatform())this._addListener(`mouseleave`,e=>{let i=e.relatedTarget;(!i||!this._overlayRef?.overlayElement.contains(i))&&this.hide()}),this._addListener(`wheel`,e=>{if(this._isTooltipVisible()){let i=this._document.elementFromPoint(e.clientX,e.clientY),a=this._elementRef.nativeElement;i!==a&&!a.contains(i)&&this.hide()}});else if(this.touchGestures!==`off`){this._disableNativeGesturesIfNecessary();let e=()=>{this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this.hide(this._defaultOptions?.touchendHideDelay)};this._addListener(`touchend`,e),this._addListener(`touchcancel`,e)}}}_addListener(e,i){this._eventCleanups.push(this._renderer.listen(this._elementRef.nativeElement,e,i,QO))}_isTouchPlatform(){let e=this._defaultOptions?.detectHoverCapability;return typeof e==`function`?!e():this._platform.IOS||this._platform.ANDROID?!0:this._platform.isBrowser?!!e&&this._mediaMatcher.matchMedia(`(any-hover: none)`).matches:!1}_disableNativeGesturesIfNecessary(){let e=this.touchGestures;if(e!==`off`){let i=this._elementRef.nativeElement,a=i.style;(e===`on`||i.nodeName!==`INPUT`&&i.nodeName!==`TEXTAREA`)&&(a.userSelect=a.msUserSelect=a.webkitUserSelect=a.MozUserSelect=`none`),(e===`on`||!i.draggable)&&(a.webkitUserDrag=`none`),a.touchAction=`none`,a.webkitTapHighlightColor=`transparent`}}_syncAriaDescription(e){this._ariaDescriptionPending||(this._ariaDescriptionPending=!0,this._ariaDescriber.removeDescription(this._elementRef.nativeElement,e,`tooltip`),this._isDestroyed||At({write:()=>{this._ariaDescriptionPending=!1,this.message&&!this.disabled&&this._ariaDescriber.describe(this._elementRef.nativeElement,this.message,`tooltip`)}},{injector:this._injector}))}_overlayEventPredicate=e=>e.type===`keydown`?this._isTooltipVisible()&&e.keyCode===27&&!xt(e):!0;static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`matTooltip`,``]],hostAttrs:[1,`mat-mdc-tooltip-trigger`],hostVars:2,hostBindings:function(i,a){i&2&&Y(`mat-mdc-tooltip-disabled`,a.disabled)},inputs:{position:[0,`matTooltipPosition`,`position`],positionAtOrigin:[0,`matTooltipPositionAtOrigin`,`positionAtOrigin`],disabled:[0,`matTooltipDisabled`,`disabled`],showDelay:[0,`matTooltipShowDelay`,`showDelay`],hideDelay:[0,`matTooltipHideDelay`,`hideDelay`],touchGestures:[0,`matTooltipTouchGestures`,`touchGestures`],message:[0,`matTooltip`,`message`],tooltipClass:[0,`matTooltipClass`,`tooltipClass`]},exportAs:[`matTooltip`]})}return t})();var tR=(()=>{class t{_changeDetectorRef=f(Fe);_elementRef=f(W);_isMultiline=!1;message;tooltipClass;_showTimeoutId;_hideTimeoutId;_triggerElement;_mouseLeaveHideDelay;_animationsDisabled=Ue();_tooltip;_closeOnInteraction=!1;_isVisible=!1;_onHide=new L;_showAnimation=`mat-mdc-tooltip-show`;_hideAnimation=`mat-mdc-tooltip-hide`;show(e){this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=setTimeout(()=>{this._toggleVisibility(!0),this._showTimeoutId=void 0},e)}hide(e){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId=setTimeout(()=>{this._toggleVisibility(!1),this._hideTimeoutId=void 0},e)}afterHidden(){return this._onHide}isVisible(){return this._isVisible}ngOnDestroy(){this._cancelPendingAnimations(),this._onHide.complete(),this._triggerElement=null}_handleBodyInteraction(){this._closeOnInteraction&&this.hide(0)}_markForCheck(){this._changeDetectorRef.markForCheck()}_handleMouseLeave({relatedTarget:e}){(!e||!this._triggerElement.contains(e))&&(this.isVisible()?this.hide(this._mouseLeaveHideDelay):this._finalizeAnimation(!1))}_onShow(){this._isMultiline=this._isTooltipMultiline(),this._markForCheck()}_isTooltipMultiline(){let e=this._elementRef.nativeElement.getBoundingClientRect();return e.height>JO&&e.width>=eR}_handleAnimationEnd({animationName:e}){(e===this._showAnimation||e===this._hideAnimation)&&this._finalizeAnimation(e===this._showAnimation)}_cancelPendingAnimations(){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=this._hideTimeoutId=void 0}_finalizeAnimation(e){e?this._closeOnInteraction=!0:this.isVisible()||this._onHide.next()}_toggleVisibility(e){let i=this._tooltip.nativeElement,a=this._showAnimation,o=this._hideAnimation;if(i.classList.remove(e?o:a),i.classList.add(e?a:o),this._isVisible!==e&&(this._isVisible=e,this._changeDetectorRef.markForCheck()),e&&!this._animationsDisabled&&typeof getComputedStyle==`function`){let r=getComputedStyle(i);(r.getPropertyValue(`animation-duration`)===`0s`||r.getPropertyValue(`animation-name`)===`none`)&&(this._animationsDisabled=!0)}e&&this._onShow(),this._animationsDisabled&&(i.classList.add(`_mat-animation-noopable`),this._finalizeAnimation(e))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`tooltip`];return B({type:t,selectors:[[`mat-tooltip-component`]],viewQuery:function(a,o){if(a&1&&Ze(e,7),a&2){let r;Z(r=J())&&(o._tooltip=r.first)}},hostAttrs:[`aria-hidden`,`true`],hostBindings:function(a,o){a&1&&C(`mouseleave`,function(s){return o._handleMouseLeave(s)})},decls:4,vars:5,consts:[[`tooltip`,``],[1,`mdc-tooltip`,`mat-mdc-tooltip`,3,`animationend`],[1,`mat-mdc-tooltip-surface`,`mdc-tooltip__surface`]],template:function(a,o){a&1&&(ct(0,`div`,1,0),Mr(`animationend`,function(s){return o._handleAnimationEnd(s)}),ct(2,`div`,2),u(3),ht()()),a&2&&(tt(o.tooltipClass),Y(`mdc-tooltip--multiline`,o._isMultiline),m(3),w(o.message))},styles:[`.mat-mdc-tooltip {
  position: relative;
  transform: scale(0);
  display: inline-flex;
}
.mat-mdc-tooltip::before {
  content: "";
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: -1;
  position: absolute;
}
.mat-mdc-tooltip-panel-below .mat-mdc-tooltip::before {
  top: -8px;
}
.mat-mdc-tooltip-panel-above .mat-mdc-tooltip::before {
  bottom: -8px;
}
.mat-mdc-tooltip-panel-right .mat-mdc-tooltip::before {
  left: -8px;
}
.mat-mdc-tooltip-panel-left .mat-mdc-tooltip::before {
  right: -8px;
}
.mat-mdc-tooltip._mat-animation-noopable {
  animation: none;
  transform: scale(1);
}

.mat-mdc-tooltip-surface {
  word-break: normal;
  overflow-wrap: anywhere;
  padding: 4px 8px;
  min-width: 40px;
  max-width: 200px;
  min-height: 24px;
  max-height: 40vh;
  box-sizing: border-box;
  overflow: hidden;
  text-align: center;
  will-change: transform, opacity;
  background-color: var(--%NS%mat-tooltip-container-color, var(--%NS%mat-sys-inverse-surface));
  color: var(--%NS%mat-tooltip-supporting-text-color, var(--%NS%mat-sys-inverse-on-surface));
  border-radius: var(--%NS%mat-tooltip-container-shape, var(--%NS%mat-sys-corner-extra-small));
  font-family: var(--%NS%mat-tooltip-supporting-text-font, var(--%NS%mat-sys-body-small-font));
  font-size: var(--%NS%mat-tooltip-supporting-text-size, var(--%NS%mat-sys-body-small-size));
  font-weight: var(--%NS%mat-tooltip-supporting-text-weight, var(--%NS%mat-sys-body-small-weight));
  line-height: var(--%NS%mat-tooltip-supporting-text-line-height, var(--%NS%mat-sys-body-small-line-height));
  letter-spacing: var(--%NS%mat-tooltip-supporting-text-tracking, var(--%NS%mat-sys-body-small-tracking));
}
.mat-mdc-tooltip-surface::before {
  position: absolute;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  border: 1px solid transparent;
  border-radius: inherit;
  content: "";
  pointer-events: none;
}
.mdc-tooltip--multiline .mat-mdc-tooltip-surface {
  text-align: left;
}
[dir=rtl] .mdc-tooltip--multiline .mat-mdc-tooltip-surface {
  text-align: right;
}

.mat-mdc-tooltip-panel {
  line-height: normal;
}
.mat-mdc-tooltip-panel.mat-mdc-tooltip-panel-non-interactive {
  pointer-events: none;
}

@keyframes mat-mdc-tooltip-show {
  0% {
    opacity: 0;
    transform: scale(0.8);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}
@keyframes mat-mdc-tooltip-hide {
  0% {
    opacity: 1;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(0.8);
  }
}
.mat-mdc-tooltip-show {
  animation: mat-mdc-tooltip-show 150ms cubic-bezier(0, 0, 0.2, 1) forwards;
}

.mat-mdc-tooltip-hide {
  animation: mat-mdc-tooltip-hide 75ms cubic-bezier(0.4, 0, 1, 1) forwards;
}
`],encapsulation:2})})()}return t})();var Iw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[Mo,mi,we,ia]})}return t})();var nR=(()=>{class t{changes=new L;itemsPerPageLabel=`Items per page:`;nextPageLabel=`Next page`;previousPageLabel=`Previous page`;firstPageLabel=`First page`;lastPageLabel=`Last page`;getRangeLabel=(e,i,a)=>{if(a==0||i==0)return`0 of ${a}`;a=Math.max(a,0);let o=e*i,r=o<a?Math.min(o+i,a):o+i;return`${o+1} \u2013 ${r} of ${a}`};static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var iR=50;var aR=new k(`MAT_PAGINATOR_DEFAULT_OPTIONS`);var Wc=(()=>{class t{_intl=f(nR);_changeDetectorRef=f(Fe);_formFieldAppearance;_pageSizeLabelId=f(Be).getId(`mat-paginator-page-size-label-`);_intlChanges;_isInitialized=!1;_initializedStream=new Yn(1);color;get pageIndex(){return this._pageIndex}set pageIndex(e){this._pageIndex=Math.max(e||0,0),this._changeDetectorRef.markForCheck()}_pageIndex=0;get length(){return this._length}set length(e){this._length=e||0,this._changeDetectorRef.markForCheck()}_length=0;get pageSize(){return this._pageSize}set pageSize(e){this._pageSize=Math.max(e||0,0),this._updateDisplayedPageSizeOptions()}_pageSize;get pageSizeOptions(){return this._pageSizeOptions}set pageSizeOptions(e){this._pageSizeOptions=(e||[]).map(i=>lt(i,0)),this._updateDisplayedPageSizeOptions()}_pageSizeOptions=[];hidePageSize=!1;showFirstLastButtons=!1;selectConfig={};disabled=!1;page=new ee;_displayedPageSizeOptions;initialized=this._initializedStream;constructor(){let e=this._intl,i=f(aR,{optional:!0});if(this._intlChanges=e.changes.subscribe(()=>this._changeDetectorRef.markForCheck()),i){let{pageSize:a,pageSizeOptions:o,hidePageSize:r,showFirstLastButtons:s}=i;a!=null&&(this._pageSize=a),o!=null&&(this._pageSizeOptions=o),r!=null&&(this.hidePageSize=r),s!=null&&(this.showFirstLastButtons=s)}this._formFieldAppearance=i?.formFieldAppearance||`outline`}ngOnInit(){this._isInitialized=!0,this._updateDisplayedPageSizeOptions(),this._initializedStream.next()}ngOnDestroy(){this._initializedStream.complete(),this._intlChanges.unsubscribe()}nextPage(){this.hasNextPage()&&this._navigate(this.pageIndex+1)}previousPage(){this.hasPreviousPage()&&this._navigate(this.pageIndex-1)}firstPage(){this.hasPreviousPage()&&this._navigate(0)}lastPage(){this.hasNextPage()&&this._navigate(this.getNumberOfPages()-1)}hasPreviousPage(){return this.pageIndex>=1&&this.pageSize!=0}hasNextPage(){let e=this.getNumberOfPages()-1;return this.pageIndex<e&&this.pageSize!=0}getNumberOfPages(){return this.pageSize?Math.ceil(this.length/this.pageSize):0}_changePageSize(e){let i=this.pageIndex*this.pageSize,a=this.pageIndex;this.pageIndex=Math.floor(i/e)||0,this.pageSize=e,this._emitPageEvent(a)}_nextButtonsDisabled(){return this.disabled||!this.hasNextPage()}_previousButtonsDisabled(){return this.disabled||!this.hasPreviousPage()}_updateDisplayedPageSizeOptions(){this._isInitialized&&(this.pageSize||(this._pageSize=this.pageSizeOptions.length!=0?this.pageSizeOptions[0]:iR),this._displayedPageSizeOptions=this.pageSizeOptions.slice(),this._displayedPageSizeOptions.indexOf(this.pageSize)===-1&&this._displayedPageSizeOptions.push(this.pageSize),this._displayedPageSizeOptions.sort((e,i)=>e-i),this._changeDetectorRef.markForCheck())}_emitPageEvent(e){this.page.emit({previousPageIndex:e,pageIndex:this.pageIndex,pageSize:this.pageSize,length:this.length})}_navigate(e){let i=this.pageIndex;e!==i&&(this.pageIndex=e,this._emitPageEvent(i))}_buttonClicked(e,i){i||this._navigate(e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(c,h){if(c&1&&(l(0,`mat-option`,17),u(1),d()),c&2){let p=h.$implicit;S(`value`,p),m(),j(` `,p,` `)}}function i(c,h){if(c&1){let p=q();l(0,`mat-form-field`,14)(1,`mat-select`,16,0),C(`selectionChange`,function(v){N(p);return A(_(2)._changePageSize(v.value))}),ue(3,e,2,2,`mat-option`,17,Pn),d(),l(5,`div`,18),C(`click`,function(){N(p);return A(ft(2).open())}),d()()}if(c&2){let p=_(2);S(`appearance`,p._formFieldAppearance)(`color`,p.color),m(),S(`value`,p.pageSize)(`disabled`,p.disabled),kr(`aria-labelledby`,p._pageSizeLabelId),S(`panelClass`,p.selectConfig.panelClass||``)(`disableOptionCentering`,p.selectConfig.disableOptionCentering),m(2),me(p._displayedPageSizeOptions)}}function a(c,h){if(c&1&&(l(0,`div`,15),u(1),d()),c&2){let p=_(2);m(),w(p.pageSize)}}function o(c,h){if(c&1&&(l(0,`div`,3)(1,`div`,13),u(2),d(),D(3,i,6,7,`mat-form-field`,14),D(4,a,2,1,`div`,15),d()),c&2){let p=_();m(),te(`id`,p._pageSizeLabelId),m(),j(` `,p._intl.itemsPerPageLabel,` `),m(),I(p._displayedPageSizeOptions.length>1?3:-1),m(),I(p._displayedPageSizeOptions.length<=1?4:-1)}}function r(c,h){if(c&1){let p=q();l(0,`button`,19),C(`click`,function(){N(p);let v=_();return A(v._buttonClicked(0,v._previousButtonsDisabled()))}),Gt(),l(1,`svg`,8),H(2,`path`,20),d()()}if(c&2){let p=_();S(`matTooltip`,p._intl.firstPageLabel)(`matTooltipDisabled`,p._previousButtonsDisabled())(`disabled`,p._previousButtonsDisabled())(`tabindex`,p._previousButtonsDisabled()?-1:null),te(`aria-label`,p._intl.firstPageLabel)}}function s(c,h){if(c&1){let p=q();l(0,`button`,21),C(`click`,function(){N(p);let v=_();return A(v._buttonClicked(v.getNumberOfPages()-1,v._nextButtonsDisabled()))}),Gt(),l(1,`svg`,8),H(2,`path`,22),d()()}if(c&2){let p=_();S(`matTooltip`,p._intl.lastPageLabel)(`matTooltipDisabled`,p._nextButtonsDisabled())(`disabled`,p._nextButtonsDisabled())(`tabindex`,p._nextButtonsDisabled()?-1:null),te(`aria-label`,p._intl.lastPageLabel)}}return B({type:t,selectors:[[`mat-paginator`]],hostAttrs:[`role`,`group`,1,`mat-mdc-paginator`],inputs:{color:`color`,pageIndex:[2,`pageIndex`,`pageIndex`,lt],length:[2,`length`,`length`,lt],pageSize:[2,`pageSize`,`pageSize`,lt],pageSizeOptions:`pageSizeOptions`,hidePageSize:[2,`hidePageSize`,`hidePageSize`,V],showFirstLastButtons:[2,`showFirstLastButtons`,`showFirstLastButtons`,V],selectConfig:`selectConfig`,disabled:[2,`disabled`,`disabled`,V]},outputs:{page:`page`},exportAs:[`matPaginator`],decls:14,vars:14,consts:[[`selectRef`,``],[1,`mat-mdc-paginator-outer-container`],[1,`mat-mdc-paginator-container`],[1,`mat-mdc-paginator-page-size`],[1,`mat-mdc-paginator-range-actions`],[`aria-atomic`,`true`,`aria-live`,`polite`,`role`,`status`,1,`mat-mdc-paginator-range-label`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-first`,3,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-previous`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`viewBox`,`0 0 24 24`,`focusable`,`false`,`aria-hidden`,`true`,1,`mat-mdc-paginator-icon`],[`d`,`M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-next`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-last`,3,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`aria-hidden`,`true`,1,`mat-mdc-paginator-page-size-label`],[1,`mat-mdc-paginator-page-size-select`,3,`appearance`,`color`],[1,`mat-mdc-paginator-page-size-value`],[`hideSingleSelectionIndicator`,``,3,`selectionChange`,`value`,`disabled`,`aria-labelledby`,`panelClass`,`disableOptionCentering`],[3,`value`],[1,`mat-mdc-paginator-touch-target`,3,`click`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-first`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M18.41 16.59L13.82 12l4.59-4.59L17 6l-6 6 6 6zM6 6h2v12H6z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-last`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M5.59 7.41L10.18 12l-4.59 4.59L7 18l6-6-6-6zM16 6h2v12h-2z`]],template:function(h,p){h&1&&(l(0,`div`,1)(1,`div`,2),D(2,o,5,4,`div`,3),l(3,`div`,4)(4,`div`,5),u(5),d(),D(6,r,3,5,`button`,6),l(7,`button`,7),C(`click`,function(){return p._buttonClicked(p.pageIndex-1,p._previousButtonsDisabled())}),Gt(),l(8,`svg`,8),H(9,`path`,9),d()(),_a(),l(10,`button`,10),C(`click`,function(){return p._buttonClicked(p.pageIndex+1,p._nextButtonsDisabled())}),Gt(),l(11,`svg`,8),H(12,`path`,11),d()(),D(13,s,3,5,`button`,12),d()()()),h&2&&(m(2),I(p.hidePageSize?-1:2),m(3),j(` `,p._intl.getRangeLabel(p.pageIndex,p.pageSize,p.length),` `),m(),I(p.showFirstLastButtons?6:-1),m(),S(`matTooltip`,p._intl.previousPageLabel)(`matTooltipDisabled`,p._previousButtonsDisabled())(`disabled`,p._previousButtonsDisabled())(`tabindex`,p._previousButtonsDisabled()?-1:null),te(`aria-label`,p._intl.previousPageLabel),m(3),S(`matTooltip`,p._intl.nextPageLabel)(`matTooltipDisabled`,p._nextButtonsDisabled())(`disabled`,p._nextButtonsDisabled())(`tabindex`,p._nextButtonsDisabled()?-1:null),te(`aria-label`,p._intl.nextPageLabel),m(3),I(p.showFirstLastButtons?13:-1))},dependencies:[Ti,Ra,Un,Wg,Dw],styles:[`.mat-mdc-paginator {
  display: block;
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  color: var(--%NS%mat-paginator-container-text-color, var(--%NS%mat-sys-on-surface));
  background-color: var(--%NS%mat-paginator-container-background-color, var(--%NS%mat-sys-surface));
  font-family: var(--%NS%mat-paginator-container-text-font, var(--%NS%mat-sys-body-small-font));
  line-height: var(--%NS%mat-paginator-container-text-line-height, var(--%NS%mat-sys-body-small-line-height));
  font-size: var(--%NS%mat-paginator-container-text-size, var(--%NS%mat-sys-body-small-size));
  font-weight: var(--%NS%mat-paginator-container-text-weight, var(--%NS%mat-sys-body-small-weight));
  letter-spacing: var(--%NS%mat-paginator-container-text-tracking, var(--%NS%mat-sys-body-small-tracking));
  --%NS%mat-form-field-container-height: var(--%NS%mat-paginator-form-field-container-height, 40px);
  --%NS%mat-form-field-container-vertical-padding: var(--%NS%mat-paginator-form-field-container-vertical-padding, 8px);
}
.mat-mdc-paginator .mat-mdc-select-value {
  font-size: var(--%NS%mat-paginator-select-trigger-text-size, var(--%NS%mat-sys-body-small-size));
}
.mat-mdc-paginator .mat-mdc-form-field-subscript-wrapper {
  display: none;
}
.mat-mdc-paginator .mat-mdc-select {
  line-height: 1.5;
}

.mat-mdc-paginator-outer-container {
  display: flex;
}

.mat-mdc-paginator-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 8px;
  flex-wrap: wrap;
  width: 100%;
  min-height: var(--%NS%mat-paginator-container-size, 56px);
}

.mat-mdc-paginator-page-size {
  display: flex;
  align-items: baseline;
  margin-right: 8px;
}
[dir=rtl] .mat-mdc-paginator-page-size {
  margin-right: 0;
  margin-left: 8px;
}

.mat-mdc-paginator-page-size-label {
  margin: 0 4px;
}

.mat-mdc-paginator-page-size-select {
  margin: 0 4px;
  width: var(--%NS%mat-paginator-page-size-select-width, 84px);
}

.mat-mdc-paginator-range-label {
  margin: 0 32px 0 24px;
}

.mat-mdc-paginator-range-actions {
  display: flex;
  align-items: center;
}

.mat-mdc-paginator-icon {
  display: inline-block;
  width: 28px;
  fill: var(--%NS%mat-paginator-enabled-icon-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-icon-button[aria-disabled] .mat-mdc-paginator-icon {
  fill: var(--%NS%mat-paginator-disabled-icon-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
[dir=rtl] .mat-mdc-paginator-icon {
  transform: rotate(180deg);
}

@media (forced-colors: active) {
  .mat-mdc-icon-button[aria-disabled] .mat-mdc-paginator-icon,
  .mat-mdc-paginator-icon {
    fill: currentColor;
  }
  .mat-mdc-paginator-range-actions .mat-mdc-icon-button {
    outline: solid 1px;
  }
  .mat-mdc-paginator-range-actions .mat-mdc-icon-button[aria-disabled] {
    color: GrayText;
  }
}
.mat-mdc-paginator-touch-target {
  display: var(--%NS%mat-paginator-touch-target-display, block);
  position: absolute;
  top: 50%;
  left: 50%;
  width: var(--%NS%mat-paginator-page-size-select-width, 84px);
  height: var(--%NS%mat-paginator-page-size-select-touch-target-height, 48px);
  background-color: transparent;
  transform: translate(-50%, -50%);
  cursor: pointer;
}
`],encapsulation:2})})()}return t})();var mm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[Pt,Pa,Iw,Wc]})}return t})();var oR=new k(`MAT_PROGRESS_BAR_DEFAULT_OPTIONS`);var rs=(()=>{class t{_elementRef=f(W);_ngZone=f(X);_changeDetectorRef=f(Fe);_renderer=f(Re);_cleanupTransitionEnd;constructor(){let e=Dc(),i=f(oR,{optional:!0});this._isNoopAnimation=e===`di-disabled`,e===`reduced-motion`&&this._elementRef.nativeElement.classList.add(`mat-progress-bar-reduced-motion`),i&&(i.color&&(this.color=this._defaultColor=i.color),this.mode=i.mode||this.mode)}_isNoopAnimation;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor=`primary`;get value(){return this._value}set value(e){this._value=Mw(e||0),this._changeDetectorRef.markForCheck()}_value=0;get bufferValue(){return this._bufferValue||0}set bufferValue(e){this._bufferValue=Mw(e||0),this._changeDetectorRef.markForCheck()}_bufferValue=0;animationEnd=new ee;get mode(){return this._mode}set mode(e){this._mode=e,this._changeDetectorRef.markForCheck()}_mode=`determinate`;ngAfterViewInit(){this._ngZone.runOutsideAngular(()=>{this._cleanupTransitionEnd=this._renderer.listen(this._elementRef.nativeElement,`transitionend`,this._transitionendHandler)})}ngOnDestroy(){this._cleanupTransitionEnd?.()}_getPrimaryBarTransform(){return`scaleX(${this._isIndeterminate()?1:this.value/100})`}_getBufferBarFlexBasis(){return`${this.mode===`buffer`?this.bufferValue:100}%`}_isIndeterminate(){return this.mode===`indeterminate`||this.mode===`query`}_transitionendHandler=e=>{this.animationEnd.observers.length===0||!e.target||!e.target.classList.contains(`mdc-linear-progress__primary-bar`)||(this.mode===`determinate`||this.mode===`buffer`)&&this._ngZone.run(()=>this.animationEnd.next({value:this.value}))};static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(i,a){i&1&&Wt(0,`div`,2)}return B({type:t,selectors:[[`mat-progress-bar`]],hostAttrs:[`role`,`progressbar`,`aria-valuemin`,`0`,`aria-valuemax`,`100`,`tabindex`,`-1`,1,`mat-mdc-progress-bar`,`mdc-linear-progress`],hostVars:10,hostBindings:function(a,o){a&2&&(te(`aria-valuenow`,o._isIndeterminate()?null:o.value)(`mode`,o.mode),tt(`mat-`+o.color),Y(`_mat-animation-noopable`,o._isNoopAnimation)(`mdc-linear-progress--animation-ready`,!o._isNoopAnimation)(`mdc-linear-progress--indeterminate`,o._isIndeterminate()))},inputs:{color:`color`,value:[2,`value`,`value`,lt],bufferValue:[2,`bufferValue`,`bufferValue`,lt],mode:`mode`},outputs:{animationEnd:`animationEnd`},exportAs:[`matProgressBar`],decls:7,vars:5,consts:[[`aria-hidden`,`true`,1,`mdc-linear-progress__buffer`],[1,`mdc-linear-progress__buffer-bar`],[1,`mdc-linear-progress__buffer-dots`],[`aria-hidden`,`true`,1,`mdc-linear-progress__bar`,`mdc-linear-progress__primary-bar`],[1,`mdc-linear-progress__bar-inner`],[`aria-hidden`,`true`,1,`mdc-linear-progress__bar`,`mdc-linear-progress__secondary-bar`]],template:function(a,o){a&1&&(ct(0,`div`,0),Wt(1,`div`,1),D(2,e,1,0,`div`,2),ht(),ct(3,`div`,3),Wt(4,`span`,4),ht(),ct(5,`div`,5),Wt(6,`span`,4),ht()),a&2&&(m(),gn(`flex-basis`,o._getBufferBarFlexBasis()),m(),I(o.mode===`buffer`?2:-1),m(),gn(`transform`,o._getPrimaryBarTransform()))},styles:[`.mat-mdc-progress-bar {
  --%NS%mat-progress-bar-animation-multiplier: 1;
  display: block;
  text-align: start;
}
.mat-mdc-progress-bar[mode=query] {
  transform: scaleX(-1);
}
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__buffer-dots,
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__primary-bar,
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__secondary-bar,
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__bar-inner.mdc-linear-progress__bar-inner {
  animation: none;
}
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__primary-bar,
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__buffer-bar {
  transition: transform 1ms;
}

.mat-progress-bar-reduced-motion {
  --%NS%mat-progress-bar-animation-multiplier: 2;
}

.mdc-linear-progress {
  position: relative;
  width: 100%;
  transform: translateZ(0);
  outline: 1px solid transparent;
  overflow-x: hidden;
  transition: opacity 250ms 0ms cubic-bezier(0.4, 0, 0.6, 1);
  height: max(var(--%NS%mat-progress-bar-track-height, 4px), var(--%NS%mat-progress-bar-active-indicator-height, 4px));
}
@media (forced-colors: active) {
  .mdc-linear-progress {
    outline-color: CanvasText;
  }
}

.mdc-linear-progress__bar {
  position: absolute;
  top: 0;
  bottom: 0;
  margin: auto 0;
  width: 100%;
  animation: none;
  transform-origin: top left;
  transition: transform 250ms 0ms cubic-bezier(0.4, 0, 0.6, 1);
  height: var(--%NS%mat-progress-bar-active-indicator-height, 4px);
}
.mdc-linear-progress--indeterminate .mdc-linear-progress__bar {
  transition: none;
}
[dir=rtl] .mdc-linear-progress__bar {
  right: 0;
  transform-origin: center right;
}

.mdc-linear-progress__bar-inner {
  display: inline-block;
  position: absolute;
  width: 100%;
  animation: none;
  border-top-style: solid;
  border-color: var(--%NS%mat-progress-bar-active-indicator-color, var(--%NS%mat-sys-primary));
  border-top-width: var(--%NS%mat-progress-bar-active-indicator-height, 4px);
}

.mdc-linear-progress__buffer {
  display: flex;
  position: absolute;
  top: 0;
  bottom: 0;
  margin: auto 0;
  width: 100%;
  overflow: hidden;
  height: var(--%NS%mat-progress-bar-track-height, 4px);
  border-radius: var(--%NS%mat-progress-bar-track-shape, var(--%NS%mat-sys-corner-none));
}

.mdc-linear-progress__buffer-dots {
  background-image: radial-gradient(circle, var(--%NS%mat-progress-bar-track-color, var(--%NS%mat-sys-surface-variant)) calc(var(--%NS%mat-progress-bar-track-height, 4px) / 2), transparent 0);
  background-repeat: repeat-x;
  background-size: calc(calc(var(--%NS%mat-progress-bar-track-height, 4px) / 2) * 5);
  background-position: left;
  flex: auto;
  transform: rotate(180deg);
  animation: mdc-linear-progress-buffering calc(250ms * var(--%NS%mat-progress-bar-animation-multiplier)) infinite linear;
}
@media (forced-colors: active) {
  .mdc-linear-progress__buffer-dots {
    background-color: ButtonBorder;
  }
}
[dir=rtl] .mdc-linear-progress__buffer-dots {
  animation: mdc-linear-progress-buffering-reverse calc(250ms * var(--%NS%mat-progress-bar-animation-multiplier)) infinite linear;
  transform: rotate(0);
}

.mdc-linear-progress__buffer-bar {
  flex: 0 1 100%;
  transition: flex-basis 250ms 0ms cubic-bezier(0.4, 0, 0.6, 1);
  background-color: var(--%NS%mat-progress-bar-track-color, var(--%NS%mat-sys-surface-variant));
}

.mdc-linear-progress__primary-bar {
  transform: scaleX(0);
}
.mdc-linear-progress--indeterminate .mdc-linear-progress__primary-bar {
  left: -145.166611%;
}
.mdc-linear-progress--indeterminate.mdc-linear-progress--animation-ready .mdc-linear-progress__primary-bar {
  animation: mdc-linear-progress-primary-indeterminate-translate calc(2s * var(--%NS%mat-progress-bar-animation-multiplier)) infinite linear;
}
.mdc-linear-progress--indeterminate.mdc-linear-progress--animation-ready .mdc-linear-progress__primary-bar > .mdc-linear-progress__bar-inner {
  animation: mdc-linear-progress-primary-indeterminate-scale calc(2s * var(--%NS%mat-progress-bar-animation-multiplier)) infinite linear;
}
[dir=rtl] .mdc-linear-progress.mdc-linear-progress--animation-ready .mdc-linear-progress__primary-bar {
  animation-name: mdc-linear-progress-primary-indeterminate-translate-reverse;
}
[dir=rtl] .mdc-linear-progress.mdc-linear-progress--indeterminate .mdc-linear-progress__primary-bar {
  right: -145.166611%;
  left: auto;
}

.mdc-linear-progress__secondary-bar {
  display: none;
}
.mdc-linear-progress--indeterminate .mdc-linear-progress__secondary-bar {
  left: -54.888891%;
  display: block;
}
.mdc-linear-progress--indeterminate.mdc-linear-progress--animation-ready .mdc-linear-progress__secondary-bar {
  animation: mdc-linear-progress-secondary-indeterminate-translate calc(2s * var(--%NS%mat-progress-bar-animation-multiplier)) infinite linear;
}
.mdc-linear-progress--indeterminate.mdc-linear-progress--animation-ready .mdc-linear-progress__secondary-bar > .mdc-linear-progress__bar-inner {
  animation: mdc-linear-progress-secondary-indeterminate-scale calc(2s * var(--%NS%mat-progress-bar-animation-multiplier)) infinite linear;
}
[dir=rtl] .mdc-linear-progress.mdc-linear-progress--animation-ready .mdc-linear-progress__secondary-bar {
  animation-name: mdc-linear-progress-secondary-indeterminate-translate-reverse;
}
[dir=rtl] .mdc-linear-progress.mdc-linear-progress--indeterminate .mdc-linear-progress__secondary-bar {
  right: -54.888891%;
  left: auto;
}

@keyframes mdc-linear-progress-buffering {
  from {
    transform: rotate(180deg) translateX(calc(var(--%NS%mat-progress-bar-track-height, 4px) * -2.5));
  }
}
@keyframes mdc-linear-progress-primary-indeterminate-translate {
  0% {
    transform: translateX(0);
  }
  20% {
    animation-timing-function: cubic-bezier(0.5, 0, 0.701732, 0.495819);
    transform: translateX(0);
  }
  59.15% {
    animation-timing-function: cubic-bezier(0.302435, 0.381352, 0.55, 0.956352);
    transform: translateX(83.67142%);
  }
  100% {
    transform: translateX(200.611057%);
  }
}
@keyframes mdc-linear-progress-primary-indeterminate-scale {
  0% {
    transform: scaleX(0.08);
  }
  36.65% {
    animation-timing-function: cubic-bezier(0.334731, 0.12482, 0.785844, 1);
    transform: scaleX(0.08);
  }
  69.15% {
    animation-timing-function: cubic-bezier(0.06, 0.11, 0.6, 1);
    transform: scaleX(0.661479);
  }
  100% {
    transform: scaleX(0.08);
  }
}
@keyframes mdc-linear-progress-secondary-indeterminate-translate {
  0% {
    animation-timing-function: cubic-bezier(0.15, 0, 0.515058, 0.409685);
    transform: translateX(0);
  }
  25% {
    animation-timing-function: cubic-bezier(0.31033, 0.284058, 0.8, 0.733712);
    transform: translateX(37.651913%);
  }
  48.35% {
    animation-timing-function: cubic-bezier(0.4, 0.627035, 0.6, 0.902026);
    transform: translateX(84.386165%);
  }
  100% {
    transform: translateX(160.277782%);
  }
}
@keyframes mdc-linear-progress-secondary-indeterminate-scale {
  0% {
    animation-timing-function: cubic-bezier(0.205028, 0.057051, 0.57661, 0.453971);
    transform: scaleX(0.08);
  }
  19.15% {
    animation-timing-function: cubic-bezier(0.152313, 0.196432, 0.648374, 1.004315);
    transform: scaleX(0.457104);
  }
  44.15% {
    animation-timing-function: cubic-bezier(0.257759, -0.003163, 0.211762, 1.38179);
    transform: scaleX(0.72796);
  }
  100% {
    transform: scaleX(0.08);
  }
}
@keyframes mdc-linear-progress-primary-indeterminate-translate-reverse {
  0% {
    transform: translateX(0);
  }
  20% {
    animation-timing-function: cubic-bezier(0.5, 0, 0.701732, 0.495819);
    transform: translateX(0);
  }
  59.15% {
    animation-timing-function: cubic-bezier(0.302435, 0.381352, 0.55, 0.956352);
    transform: translateX(-83.67142%);
  }
  100% {
    transform: translateX(-200.611057%);
  }
}
@keyframes mdc-linear-progress-secondary-indeterminate-translate-reverse {
  0% {
    animation-timing-function: cubic-bezier(0.15, 0, 0.515058, 0.409685);
    transform: translateX(0);
  }
  25% {
    animation-timing-function: cubic-bezier(0.31033, 0.284058, 0.8, 0.733712);
    transform: translateX(-37.651913%);
  }
  48.35% {
    animation-timing-function: cubic-bezier(0.4, 0.627035, 0.6, 0.902026);
    transform: translateX(-84.386165%);
  }
  100% {
    transform: translateX(-160.277782%);
  }
}
@keyframes mdc-linear-progress-buffering-reverse {
  from {
    transform: translateX(-10px);
  }
}
`],encapsulation:2})})()}return t})();function Mw(t,n=0,e=100){return Math.max(n,Math.min(e,t))}var ss=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[we]})}return t})();var rR=new k(`MatTabContent`);var qc=(()=>{class t{template=f(Vt);static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`matTabContent`,``]],features:[Ge([{provide:rR,useExisting:t}])]})}return t})();var sR=new k(`MatTabLabel`);var Ow=new k(`MAT_TAB`);var cR=(()=>{class t extends ow{_closestTab=f(Ow,{optional:!0});static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=K({type:t,selectors:[[``,`mat-tab-label`,``],[``,`matTabLabel`,``]],features:[Ge([{provide:sR,useExisting:t}]),Ve]})}return t})();var Rw=new k(`MAT_TAB_GROUP`);var Yc=(()=>{class t{_viewContainerRef=f(Bt);_closestTabGroup=f(Rw,{optional:!0});disabled=!1;get templateLabel(){return this._templateLabel}set templateLabel(e){this._setTemplateLabelInput(e)}_templateLabel;_explicitContent=void 0;_implicitContent;textLabel=``;ariaLabel;ariaLabelledby;labelClass;bodyClass;id=null;_contentPortal=null;get content(){return this._contentPortal}_stateChanges=new L;position=null;origin=null;isActive=!1;constructor(){f(Je).load(dn)}ngOnChanges(e){(Object.hasOwn(e,`textLabel`)||Object.hasOwn(e,`disabled`))&&this._stateChanges.next()}ngOnDestroy(){this._stateChanges.complete()}ngOnInit(){this._contentPortal=new Hn(this._explicitContent||this._implicitContent,this._viewContainerRef)}_setTemplateLabelInput(e){e&&e._closestTab===this&&(this._templateLabel=e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`];function i(a,o){a&1&&he(0)}return B({type:t,selectors:[[`mat-tab`]],contentQueries:function(o,r,s){if(o&1&&jt(s,cR,5)(s,qc,7,Vt),o&2){let c;Z(c=J())&&(r.templateLabel=c.first),Z(c=J())&&(r._explicitContent=c.first)}},viewQuery:function(o,r){if(o&1&&Ze(Vt,7),o&2){let s;Z(s=J())&&(r._implicitContent=s.first)}},hostAttrs:[`hidden`,``],hostVars:1,hostBindings:function(o,r){o&2&&te(`id`,null)},inputs:{disabled:[2,`disabled`,`disabled`,V],textLabel:[0,`label`,`textLabel`],ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],labelClass:`labelClass`,bodyClass:`bodyClass`,id:`id`},exportAs:[`matTab`],features:[Ge([{provide:Ow,useExisting:t}]),vt],ngContentSelectors:e,decls:1,vars:0,template:function(o,r){o&1&&(Pe(),Ir(0,i,1,0,`ng-template`))},encapsulation:2,changeDetection:1})})()}return t})();var Db=`mdc-tab-indicator--active`;var Tw=`mdc-tab-indicator--no-transition`;var kb=class{_items;_currentItem;constructor(n){this._items=n}hide(){this._items.forEach(n=>n.deactivateInkBar()),this._currentItem=void 0}alignToElement(n){let e=this._items.find(a=>a.elementRef.nativeElement===n),i=this._currentItem;if(e!==i&&(i?.deactivateInkBar(),e)){let a=i?.elementRef.nativeElement.getBoundingClientRect?.();e.activateInkBar(a),this._currentItem=e}}};var lR=(()=>{class t{_elementRef=f(W);_inkBarElement=null;_inkBarContentElement=null;_fitToContent=!1;get fitInkBarToContent(){return this._fitToContent}set fitInkBarToContent(e){this._fitToContent!==e&&(this._fitToContent=e,this._inkBarElement&&this._appendInkBarElement())}activateInkBar(e){let i=this._elementRef.nativeElement;if(!e||!i.getBoundingClientRect||!this._inkBarContentElement){i.classList.add(Db);return}let a=i.getBoundingClientRect(),o=e.width/a.width,r=e.left-a.left;i.classList.add(Tw),this._inkBarContentElement.style.setProperty(`transform`,`translateX(${r}px) scaleX(${o})`),i.getBoundingClientRect(),i.classList.remove(Tw),i.classList.add(Db),this._inkBarContentElement.style.setProperty(`transform`,``)}deactivateInkBar(){this._elementRef.nativeElement.classList.remove(Db)}ngOnInit(){this._createInkBarElement()}ngOnDestroy(){this._inkBarElement?.remove(),this._inkBarElement=this._inkBarContentElement=null}_createInkBarElement(){let e=this._elementRef.nativeElement.ownerDocument||document,i=this._inkBarElement=e.createElement(`span`),a=this._inkBarContentElement=e.createElement(`span`);i.className=`mdc-tab-indicator`,a.className=`mdc-tab-indicator__content mdc-tab-indicator__content--underline`,i.appendChild(this._inkBarContentElement),this._appendInkBarElement()}_appendInkBarElement(){this._inkBarElement;(this._fitToContent?this._elementRef.nativeElement.querySelector(`.mdc-tab__content`):this._elementRef.nativeElement).appendChild(this._inkBarElement)}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,inputs:{fitInkBarToContent:[2,`fitInkBarToContent`,`fitInkBarToContent`,V]}})}return t})();var Pw=(()=>{class t extends lR{elementRef=f(W);disabled=!1;focus(){this.elementRef.nativeElement.focus()}getOffsetLeft(){return this.elementRef.nativeElement.offsetLeft}getOffsetWidth(){return this.elementRef.nativeElement.offsetWidth}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=K({type:t,selectors:[[``,`matTabLabelWrapper`,``]],hostVars:3,hostBindings:function(i,a){i&2&&(te(`aria-disabled`,!!a.disabled),Y(`mat-mdc-tab-disabled`,a.disabled))},inputs:{disabled:[2,`disabled`,`disabled`,V]},features:[Ve]})}return t})();var Nw={passive:!0};var dR=650;var uR=100;function Ib(t){let n=t+``;return/^[0-9]+(?:\.[0-9]+)?$/.test(n)?`${t}ms`:/^[0-9]+(?:\.[0-9]+)?(?:ms|s)$/.test(n)?n:``}var mR=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Fe);_viewportRuler=f(ui);_dir=f(gt,{optional:!0});_ngZone=f(X);_platform=f(Oe);_sharedResizeObserver=f(Xu);_injector=f(se);_renderer=f(Re);_animationsDisabled=Ue();_eventCleanups;_scrollDistance=0;_selectedIndexChanged=!1;_destroyed=new L;_showPaginationControls=!1;_disableScrollAfter=!0;_disableScrollBefore=!0;_tabLabelCount;_scrollDistanceChanged=!1;_keyManager;_currentTextContent;_stopScrolling=new L;disablePagination=!1;get selectedIndex(){return this._selectedIndex}set selectedIndex(e){let i=isNaN(e)?0:e;this._selectedIndex!=i&&(this._selectedIndexChanged=!0,this._selectedIndex=i,this._keyManager&&this._keyManager.updateActiveItem(i))}_selectedIndex=0;selectFocusedIndex=new ee;indexFocused=new ee;constructor(){this._eventCleanups=this._ngZone.runOutsideAngular(()=>[this._renderer.listen(this._elementRef.nativeElement,`mouseleave`,()=>this._stopInterval())])}ngAfterViewInit(){this._eventCleanups.push(this._renderer.listen(this._previousPaginator.nativeElement,`touchstart`,()=>this._handlePaginatorPress(`before`),Nw),this._renderer.listen(this._nextPaginator.nativeElement,`touchstart`,()=>this._handlePaginatorPress(`after`),Nw))}ngAfterContentInit(){let e=this._dir?this._dir.change:Mn(`ltr`),i=this._sharedResizeObserver.observe(this._elementRef.nativeElement).pipe(Pi(32),Te(this._destroyed)),a=this._viewportRuler.change(150).pipe(Te(this._destroyed)),o=()=>{this.updatePagination(),this._alignInkBarToSelectedTab()};this._keyManager=new ea(this._items).withHorizontalOrientation(this._getLayoutDirection()).withHomeAndEnd().withWrap().skipPredicate(()=>!1),this._keyManager.updateActiveItem(Math.max(this._selectedIndex,0)),At(o,{injector:this._injector}),It(e,a,i,this._items.changes,this._itemsResized()).pipe(Te(this._destroyed)).subscribe(()=>{this._ngZone.run(()=>{Promise.resolve().then(()=>{this._scrollDistance=Math.max(0,Math.min(this._getMaxScrollDistance(),this._scrollDistance)),o()})}),this._keyManager?.withHorizontalOrientation(this._getLayoutDirection())}),this._keyManager.change.subscribe(r=>{this.indexFocused.emit(r),this._setTabFocus(r)})}_itemsResized(){return typeof ResizeObserver!=`function`?Ri:this._items.changes.pipe(mt(this._items),_n(e=>new Ce(i=>this._ngZone.runOutsideAngular(()=>{let a=new ResizeObserver(o=>i.next(o));return e.forEach(o=>a.observe(o.elementRef.nativeElement)),()=>{a.disconnect()}}))),Qa(1),Qe(e=>e.some(i=>i.contentRect.width>0&&i.contentRect.height>0)))}ngAfterContentChecked(){this._tabLabelCount!=this._items.length&&(this.updatePagination(),this._tabLabelCount=this._items.length,this._changeDetectorRef.markForCheck()),this._selectedIndexChanged&&(this._scrollToLabel(this._selectedIndex),this._checkScrollingControls(),this._alignInkBarToSelectedTab(),this._selectedIndexChanged=!1,this._changeDetectorRef.markForCheck()),this._scrollDistanceChanged&&(this._updateTabScrollPosition(),this._scrollDistanceChanged=!1,this._changeDetectorRef.markForCheck())}ngOnDestroy(){this._eventCleanups.forEach(e=>e()),this._keyManager?.destroy(),this._destroyed.next(),this._destroyed.complete(),this._stopScrolling.complete()}_handleKeydown(e){if(!xt(e))switch(e.keyCode){case 13:case 32:if(this.focusIndex!==this.selectedIndex){let i=this._items.get(this.focusIndex);i&&!i.disabled&&(this.selectFocusedIndex.emit(this.focusIndex),this._itemSelected(e))}break;default:this._keyManager?.onKeydown(e)}}_onContentChanges(){let e=this._elementRef.nativeElement.textContent;e!==this._currentTextContent&&(this._currentTextContent=e||``,this._ngZone.run(()=>{this.updatePagination(),this._alignInkBarToSelectedTab(),this._changeDetectorRef.markForCheck()}))}updatePagination(){this._checkPaginationEnabled(),this._checkScrollingControls(),this._updateTabScrollPosition()}get focusIndex(){return this._keyManager?this._keyManager.activeItemIndex:0}set focusIndex(e){!this._isValidIndex(e)||this.focusIndex===e||!this._keyManager||this._keyManager.setActiveItem(e)}_isValidIndex(e){return this._items?!!this._items.toArray()[e]:!0}_setTabFocus(e){if(this._showPaginationControls&&this._scrollToLabel(e),this._items&&this._items.length){this._items.toArray()[e].focus();let i=this._tabListContainer.nativeElement;this._getLayoutDirection()==`ltr`?i.scrollLeft=0:i.scrollLeft=i.scrollWidth-i.offsetWidth}}_getLayoutDirection(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_updateTabScrollPosition(){if(this.disablePagination)return;let e=this.scrollDistance,i=this._getLayoutDirection()===`ltr`?-e:e;this._tabList.nativeElement.style.transform=`translateX(${Math.round(i)}px)`,(this._platform.TRIDENT||this._platform.EDGE)&&(this._tabListContainer.nativeElement.scrollLeft=0)}get scrollDistance(){return this._scrollDistance}set scrollDistance(e){this._scrollTo(e)}_scrollHeader(e){let i=this._tabListContainer.nativeElement.offsetWidth,a=(e==`before`?-1:1)*i/3;return this._scrollTo(this._scrollDistance+a)}_handlePaginatorClick(e){this._stopInterval(),this._scrollHeader(e)}_scrollToLabel(e){if(this.disablePagination)return;let i=this._items?this._items.toArray()[e]:null;if(!i)return;let a=this._tabListContainer.nativeElement.offsetWidth,{offsetLeft:o,offsetWidth:r}=i.elementRef.nativeElement,s,c;this._getLayoutDirection()==`ltr`?(s=o,c=s+r):(c=this._tabListInner.nativeElement.offsetWidth-o,s=c-r);let h=this.scrollDistance,p=this.scrollDistance+a;s<h?this.scrollDistance-=h-s:c>p&&(this.scrollDistance+=Math.min(c-p,s-h))}_checkPaginationEnabled(){if(this.disablePagination)this._showPaginationControls=!1;else{let a=this._tabListInner.nativeElement.scrollWidth-this._elementRef.nativeElement.offsetWidth>=5;a||(this.scrollDistance=0),a!==this._showPaginationControls&&(this._showPaginationControls=a,this._changeDetectorRef.markForCheck())}}_checkScrollingControls(){this.disablePagination?this._disableScrollAfter=this._disableScrollBefore=!0:(this._disableScrollBefore=this.scrollDistance==0,this._disableScrollAfter=this.scrollDistance==this._getMaxScrollDistance(),this._changeDetectorRef.markForCheck())}_getMaxScrollDistance(){return this._tabListInner.nativeElement.scrollWidth-this._tabListContainer.nativeElement.offsetWidth||0}_alignInkBarToSelectedTab(){let e=this._items&&this._items.length?this._items.toArray()[this.selectedIndex]:null,i=e?e.elementRef.nativeElement:null;i?this._inkBar.alignToElement(i):this._inkBar.hide()}_stopInterval(){this._stopScrolling.next()}_handlePaginatorPress(e,i){i&&i.button!=null&&i.button!==0||(this._stopInterval(),_s(dR,uR).pipe(Te(It(this._stopScrolling,this._destroyed))).subscribe(()=>{let{maxScrollDistance:a,distance:o}=this._scrollHeader(e);(o===0||o>=a)&&this._stopInterval()}))}_scrollTo(e){if(this.disablePagination)return{maxScrollDistance:0,distance:0};let i=this._getMaxScrollDistance();return this._scrollDistance=Math.max(0,Math.min(i,e)),this._scrollDistanceChanged=!0,this._checkScrollingControls(),{maxScrollDistance:i,distance:this._scrollDistance}}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,inputs:{disablePagination:[2,`disablePagination`,`disablePagination`,V],selectedIndex:[2,`selectedIndex`,`selectedIndex`,lt]},outputs:{selectFocusedIndex:`selectFocusedIndex`,indexFocused:`indexFocused`}})}return t})();var pR=(()=>{class t extends mR{_items;_tabListContainer;_tabList;_tabListInner;_nextPaginator;_previousPaginator;_inkBar;ariaLabel;ariaLabelledby;disableRipple=!1;ngAfterContentInit(){this._inkBar=new kb(this._items),super.ngAfterContentInit()}_itemSelected(e){e.preventDefault()}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵcmp=(function(){let e=[`tabListContainer`],i=[`tabList`],a=[`tabListInner`],o=[`nextPaginator`],r=[`previousPaginator`];return B({type:t,selectors:[[`mat-tab-header`]],contentQueries:function(h,p,g){if(h&1&&jt(g,Pw,4),h&2){let v;Z(v=J())&&(p._items=v)}},viewQuery:function(h,p){if(h&1&&Ze(e,7)(i,7)(a,7)(o,5)(r,5),h&2){let g;Z(g=J())&&(p._tabListContainer=g.first),Z(g=J())&&(p._tabList=g.first),Z(g=J())&&(p._tabListInner=g.first),Z(g=J())&&(p._nextPaginator=g.first),Z(g=J())&&(p._previousPaginator=g.first)}},hostAttrs:[1,`mat-mdc-tab-header`],hostVars:4,hostBindings:function(h,p){h&2&&Y(`mat-mdc-tab-header-pagination-controls-enabled`,p._showPaginationControls)(`mat-mdc-tab-header-rtl`,p._getLayoutDirection()==`rtl`)},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],disableRipple:[2,`disableRipple`,`disableRipple`,V]},features:[Ve],ngContentSelectors:[`*`],decls:13,vars:10,consts:[[`previousPaginator`,``],[`tabListContainer`,``],[`tabList`,``],[`tabListInner`,``],[`nextPaginator`,``],[`mat-ripple`,``,1,`mat-mdc-tab-header-pagination`,`mat-mdc-tab-header-pagination-before`,3,`click`,`mousedown`,`touchend`,`matRippleDisabled`],[1,`mat-mdc-tab-header-pagination-chevron`],[1,`mat-mdc-tab-label-container`,3,`keydown`],[`role`,`tablist`,1,`mat-mdc-tab-list`,3,`cdkObserveContent`],[1,`mat-mdc-tab-labels`],[`mat-ripple`,``,1,`mat-mdc-tab-header-pagination`,`mat-mdc-tab-header-pagination-after`,3,`mousedown`,`click`,`touchend`,`matRippleDisabled`]],template:function(h,p){h&1&&(Pe(),l(0,`div`,5,0),C(`click`,function(){return p._handlePaginatorClick(`before`)})(`mousedown`,function(v){return p._handlePaginatorPress(`before`,v)})(`touchend`,function(){return p._stopInterval()}),H(2,`div`,6),d(),l(3,`div`,7,1),C(`keydown`,function(v){return p._handleKeydown(v)}),l(5,`div`,8,2),C(`cdkObserveContent`,function(){return p._onContentChanges()}),l(7,`div`,9,3),he(9),d()()(),l(10,`div`,10,4),C(`mousedown`,function(v){return p._handlePaginatorPress(`after`,v)})(`click`,function(){return p._handlePaginatorClick(`after`)})(`touchend`,function(){return p._stopInterval()}),H(12,`div`,6),d()),h&2&&(Y(`mat-mdc-tab-header-pagination-disabled`,p._disableScrollBefore),S(`matRippleDisabled`,p._disableScrollBefore||p.disableRipple),m(3),Y(`_mat-animation-noopable`,p._animationsDisabled),m(2),te(`aria-label`,p.ariaLabel||null)(`aria-labelledby`,p.ariaLabelledby||null),m(5),Y(`mat-mdc-tab-header-pagination-disabled`,p._disableScrollAfter),S(`matRippleDisabled`,p._disableScrollAfter||p.disableRipple))},dependencies:[ci,FC],styles:[`.mat-mdc-tab-header {
  display: flex;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
}

.mdc-tab-indicator .mdc-tab-indicator__content {
  transition-duration: var(--%NS%mat-tab-header-animation-duration, 250ms);
}

.mat-mdc-tab-header-pagination {
  -webkit-user-select: none;
  user-select: none;
  position: relative;
  display: none;
  justify-content: center;
  align-items: center;
  min-width: 32px;
  cursor: pointer;
  z-index: 2;
  -webkit-tap-highlight-color: transparent;
  touch-action: none;
  box-sizing: content-box;
  outline: 0;
}
.mat-mdc-tab-header-pagination::-moz-focus-inner {
  border: 0;
}
.mat-mdc-tab-header-pagination .mat-ripple-element {
  opacity: 0.12;
  background-color: var(--%NS%mat-tab-inactive-ripple-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-tab-header-pagination-controls-enabled .mat-mdc-tab-header-pagination {
  display: flex;
}

.mat-mdc-tab-header-pagination-before,
.mat-mdc-tab-header-rtl .mat-mdc-tab-header-pagination-after {
  padding-left: 4px;
}
.mat-mdc-tab-header-pagination-before .mat-mdc-tab-header-pagination-chevron,
.mat-mdc-tab-header-rtl .mat-mdc-tab-header-pagination-after .mat-mdc-tab-header-pagination-chevron {
  transform: rotate(-135deg);
}

.mat-mdc-tab-header-rtl .mat-mdc-tab-header-pagination-before,
.mat-mdc-tab-header-pagination-after {
  padding-right: 4px;
}
.mat-mdc-tab-header-rtl .mat-mdc-tab-header-pagination-before .mat-mdc-tab-header-pagination-chevron,
.mat-mdc-tab-header-pagination-after .mat-mdc-tab-header-pagination-chevron {
  transform: rotate(45deg);
}

.mat-mdc-tab-header-pagination-chevron {
  border-style: solid;
  border-width: 2px 2px 0 0;
  height: 8px;
  width: 8px;
  border-color: var(--%NS%mat-tab-pagination-icon-color, var(--%NS%mat-sys-on-surface));
}

.mat-mdc-tab-header-pagination-disabled {
  box-shadow: none;
  cursor: default;
  pointer-events: none;
}
.mat-mdc-tab-header-pagination-disabled .mat-mdc-tab-header-pagination-chevron {
  opacity: 0.4;
}

.mat-mdc-tab-list {
  flex-grow: 1;
  position: relative;
  transition: transform 500ms cubic-bezier(0.35, 0, 0.25, 1);
}
._mat-animation-noopable .mat-mdc-tab-list {
  transition: none;
}

.mat-mdc-tab-label-container {
  display: flex;
  flex-grow: 1;
  overflow: hidden;
  z-index: 1;
  border-bottom-style: solid;
  border-bottom-width: var(--%NS%mat-tab-divider-height, 1px);
  border-bottom-color: var(--%NS%mat-tab-divider-color, var(--%NS%mat-sys-surface-variant));
}
.mat-mdc-tab-group-inverted-header .mat-mdc-tab-label-container {
  border-bottom: none;
  border-top-style: solid;
  border-top-width: var(--%NS%mat-tab-divider-height, 1px);
  border-top-color: var(--%NS%mat-tab-divider-color, var(--%NS%mat-sys-surface-variant));
}

.mat-mdc-tab-labels {
  display: flex;
  flex: 1 0 auto;
}
[mat-align-tabs=center] > .mat-mdc-tab-header .mat-mdc-tab-labels {
  justify-content: center;
}
[mat-align-tabs=end] > .mat-mdc-tab-header .mat-mdc-tab-labels {
  justify-content: flex-end;
}
.cdk-drop-list .mat-mdc-tab-labels, .mat-mdc-tab-labels.cdk-drop-list {
  min-height: var(--%NS%mat-tab-container-height, 48px);
}

.mat-mdc-tab::before {
  margin: 5px;
}
@media (forced-colors: active) {
  .mat-mdc-tab[aria-disabled=true] {
    color: GrayText;
  }
}
`],encapsulation:2,changeDetection:1})})()}return t})();var hR=new k(`MAT_TABS_CONFIG`);var Aw=(()=>{class t extends Aa{_host=f(Mb);_ngZone=f(X);_centeringSub=De.EMPTY;_leavingSub=De.EMPTY;ngOnInit(){super.ngOnInit(),this._centeringSub=this._host._beforeCentering.pipe(mt(this._host._isCenterPosition())).subscribe(e=>{this._host._content&&e&&!this.hasAttached()&&this._ngZone.run(()=>{Promise.resolve().then(),this.attach(this._host._content)})}),this._leavingSub=this._host._afterLeavingCenter.subscribe(()=>{this._host.preserveContent||this._ngZone.run(()=>this.detach())})}ngOnDestroy(){super.ngOnDestroy(),this._centeringSub.unsubscribe(),this._leavingSub.unsubscribe()}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=K({type:t,selectors:[[``,`matTabBodyHost`,``]],features:[Ve]})}return t})();var Mb=(()=>{class t{_elementRef=f(W);_dir=f(gt,{optional:!0});_ngZone=f(X);_injector=f(se);_renderer=f(Re);_diAnimationsDisabled=Ue();_eventCleanups;_initialized=!1;_fallbackTimer;_positionIndex;_dirChangeSubscription=De.EMPTY;_position;_previousPosition;_onCentering=new ee;_beforeCentering=new ee;_afterLeavingCenter=new ee;_onCentered=new ee(!0);_portalHost;_contentElement;_content;animationDuration=`500ms`;preserveContent=!1;set position(e){this._positionIndex=e,this._computePositionAnimationState()}constructor(){if(this._dir){let e=f(Fe);this._dirChangeSubscription=this._dir.change.subscribe(i=>{this._computePositionAnimationState(i),e.markForCheck()})}}ngOnInit(){this._bindTransitionEvents(),this._position===`center`&&(this._setActiveClass(!0),At(()=>this._onCentering.emit(this._elementRef.nativeElement.clientHeight),{injector:this._injector})),this._initialized=!0}ngOnDestroy(){clearTimeout(this._fallbackTimer),this._eventCleanups?.forEach(e=>e()),this._dirChangeSubscription.unsubscribe()}_bindTransitionEvents(){this._ngZone.runOutsideAngular(()=>{let e=this._elementRef.nativeElement,i=a=>{a.target===this._contentElement?.nativeElement&&(this._elementRef.nativeElement.classList.remove(`mat-tab-body-animating`),a.type===`transitionend`&&this._transitionDone())};this._eventCleanups=[this._renderer.listen(e,`transitionstart`,a=>{a.target===this._contentElement?.nativeElement&&(this._elementRef.nativeElement.classList.add(`mat-tab-body-animating`),this._transitionStarted())}),this._renderer.listen(e,`transitionend`,i),this._renderer.listen(e,`transitioncancel`,i)]})}_transitionStarted(){clearTimeout(this._fallbackTimer);let e=this._position===`center`;this._beforeCentering.emit(e),e&&this._onCentering.emit(this._elementRef.nativeElement.clientHeight)}_transitionDone(){this._position===`center`?this._onCentered.emit():this._previousPosition===`center`&&this._afterLeavingCenter.emit()}_setActiveClass(e){this._elementRef.nativeElement.classList.toggle(`mat-mdc-tab-body-active`,e)}_getLayoutDirection(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_isCenterPosition(){return this._positionIndex===0}_computePositionAnimationState(e=this._getLayoutDirection()){this._previousPosition=this._position,this._positionIndex<0?this._position=e==`ltr`?`left`:`right`:this._positionIndex>0?this._position=e==`ltr`?`right`:`left`:this._position=`center`,this._animationsDisabled()?this._simulateTransitionEvents():this._initialized&&(this._position===`center`||this._previousPosition===`center`)&&(clearTimeout(this._fallbackTimer),this._fallbackTimer=this._ngZone.runOutsideAngular(()=>setTimeout(()=>this._simulateTransitionEvents(),100)))}_simulateTransitionEvents(){this._transitionStarted(),At(()=>this._transitionDone(),{injector:this._injector})}_animationsDisabled(){return this._diAnimationsDisabled||this.animationDuration===`0ms`||this.animationDuration===`0s`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`content`];function i(a,o){}return B({type:t,selectors:[[`mat-tab-body`]],viewQuery:function(o,r){if(o&1&&Ze(Aw,5)(e,5),o&2){let s;Z(s=J())&&(r._portalHost=s.first),Z(s=J())&&(r._contentElement=s.first)}},hostAttrs:[1,`mat-mdc-tab-body`],hostVars:1,hostBindings:function(o,r){o&2&&te(`inert`,r._position===`center`?null:``)},inputs:{_content:[0,`content`,`_content`],animationDuration:`animationDuration`,preserveContent:`preserveContent`,position:`position`},outputs:{_onCentering:`_onCentering`,_beforeCentering:`_beforeCentering`,_onCentered:`_onCentered`},decls:3,vars:6,consts:[[`content`,``],[`cdkScrollable`,``,1,`mat-mdc-tab-body-content`],[`matTabBodyHost`,``]],template:function(o,r){o&1&&(l(0,`div`,1,0),ot(2,i,0,0,`ng-template`,2),d()),o&2&&Y(`mat-tab-body-content-left`,r._position===`left`)(`mat-tab-body-content-right`,r._position===`right`)(`mat-tab-body-content-can-animate`,r._position===`center`||r._previousPosition===`center`)},dependencies:[Aw,Uc],styles:[`.mat-mdc-tab-body {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  display: block;
  overflow: hidden;
  outline: 0;
  flex-basis: 100%;
}
.mat-mdc-tab-body.mat-mdc-tab-body-active {
  position: relative;
  overflow-x: hidden;
  overflow-y: auto;
  z-index: 1;
  flex-grow: 1;
}
.mat-mdc-tab-group.mat-mdc-tab-group-dynamic-height .mat-mdc-tab-body.mat-mdc-tab-body-active {
  overflow-y: hidden;
}

.mat-mdc-tab-body-content {
  height: 100%;
  overflow: auto;
  transform: none;
  visibility: hidden;
}
.mat-tab-body-animating > .mat-mdc-tab-body-content, .mat-mdc-tab-body-active > .mat-mdc-tab-body-content {
  visibility: visible;
}
.mat-tab-body-animating > .mat-mdc-tab-body-content {
  min-height: 1px;
}
.mat-mdc-tab-group-dynamic-height .mat-mdc-tab-body-content {
  overflow: hidden;
}

.mat-tab-body-content-can-animate {
  transition: transform var(--%NS%mat-tab-body-animation-duration) 1ms cubic-bezier(0.35, 0, 0.25, 1);
}
.mat-mdc-tab-body-wrapper._mat-animation-noopable .mat-tab-body-content-can-animate {
  transition: none;
}

.mat-tab-body-content-left {
  transform: translate3d(-100%, 0, 0);
}

.mat-tab-body-content-right {
  transform: translate3d(100%, 0, 0);
}
`],encapsulation:2,changeDetection:1})})()}return t})();var pm=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Fe);_ngZone=f(X);_tabsSubscription=De.EMPTY;_tabLabelSubscription=De.EMPTY;_tabBodySubscription=De.EMPTY;_diAnimationsDisabled=Ue();_bodyAnimationDuration;_headerAnimationDuration;_allTabs;_tabBodies;_tabBodyWrapper;_tabHeader;_tabs=new wn;_indexToSelect=0;_lastFocusedTabIndex=null;_tabBodyWrapperHeight=0;color;get fitInkBarToContent(){return this._fitInkBarToContent}set fitInkBarToContent(e){this._fitInkBarToContent=e,this._changeDetectorRef.markForCheck()}_fitInkBarToContent=!1;stretchTabs=!0;alignTabs=null;dynamicHeight=!1;get selectedIndex(){return this._selectedIndex}set selectedIndex(e){this._indexToSelect=isNaN(e)?null:e}_selectedIndex=null;headerPosition=`above`;get animationDuration(){return this._animationDuration}set animationDuration(e){this._animationDuration=e,e&&typeof e==`object`?(this._bodyAnimationDuration=Ib(e.body),this._headerAnimationDuration=Ib(e.header)):this._headerAnimationDuration=this._bodyAnimationDuration=Ib(e)}_animationDuration;get contentTabIndex(){return this._contentTabIndex}set contentTabIndex(e){this._contentTabIndex=isNaN(e)?null:e}_contentTabIndex=null;disablePagination=!1;disableRipple=!1;preserveContent=!1;get backgroundColor(){return this._backgroundColor}set backgroundColor(e){let i=this._elementRef.nativeElement.classList;i.remove(`mat-tabs-with-background`,`mat-background-${this.backgroundColor}`),e&&i.add(`mat-tabs-with-background`,`mat-background-${e}`),this._backgroundColor=e}_backgroundColor;ariaLabel;ariaLabelledby;selectedIndexChange=new ee;focusChange=new ee;animationDone=new ee;selectedTabChange=new ee(!0);_groupId;_isServer=!f(Oe).isBrowser;constructor(){let e=f(hR,{optional:!0});this._groupId=f(Be).getId(`mat-tab-group-`),this.animationDuration=e&&e.animationDuration?e.animationDuration:`500ms`,this.disablePagination=e&&e.disablePagination!=null?e.disablePagination:!1,this.dynamicHeight=e&&e.dynamicHeight!=null?e.dynamicHeight:!1,e?.contentTabIndex!=null&&(this.contentTabIndex=e.contentTabIndex),this.preserveContent=!!e?.preserveContent,this.fitInkBarToContent=e&&e.fitInkBarToContent!=null?e.fitInkBarToContent:!1,this.stretchTabs=e&&e.stretchTabs!=null?e.stretchTabs:!0,this.alignTabs=e&&e.alignTabs!=null?e.alignTabs:null}ngAfterContentChecked(){let e=this._indexToSelect=this._clampTabIndex(this._indexToSelect);if(this._selectedIndex!=e){let i=this._selectedIndex==null;if(!i){this.selectedTabChange.emit(this._createChangeEvent(e));let a=this._tabBodyWrapper.nativeElement;a.style.minHeight=a.clientHeight+`px`}Promise.resolve().then(()=>{this._tabs.forEach((a,o)=>a.isActive=o===e),i||(this.selectedIndexChange.emit(e),this._tabBodyWrapper.nativeElement.style.minHeight=``)})}this._tabs.forEach((i,a)=>{i.position=a-e,this._selectedIndex!=null&&i.position==0&&!i.origin&&(i.origin=e-this._selectedIndex)}),this._selectedIndex!==e&&(this._selectedIndex=e,this._lastFocusedTabIndex=null,this._changeDetectorRef.markForCheck())}ngAfterContentInit(){this._subscribeToAllTabChanges(),this._subscribeToTabLabels(),this._tabsSubscription=this._tabs.changes.subscribe(()=>{let e=this._clampTabIndex(this._indexToSelect);if(e===this._selectedIndex){let i=this._tabs.toArray(),a;for(let o=0;o<i.length;o++)if(i[o].isActive){this._indexToSelect=this._selectedIndex=o,this._lastFocusedTabIndex=null,a=i[o];break}!a&&i[e]&&Promise.resolve().then(()=>{i[e].isActive=!0,this.selectedTabChange.emit(this._createChangeEvent(e))})}this._changeDetectorRef.markForCheck()})}ngAfterViewInit(){this._tabBodySubscription=this._tabBodies.changes.subscribe(()=>this._bodyCentered(!0))}_subscribeToAllTabChanges(){this._allTabs.changes.pipe(mt(this._allTabs)).subscribe(e=>{this._tabs.reset(e.filter(i=>i._closestTabGroup===this||!i._closestTabGroup)),this._tabs.notifyOnChanges()})}ngOnDestroy(){this._tabs.destroy(),this._tabsSubscription.unsubscribe(),this._tabLabelSubscription.unsubscribe(),this._tabBodySubscription.unsubscribe()}realignInkBar(){this._tabHeader&&this._tabHeader._alignInkBarToSelectedTab()}updatePagination(){this._tabHeader&&this._tabHeader.updatePagination()}focusTab(e){let i=this._tabHeader;i&&(i.focusIndex=e)}_focusChanged(e){this._lastFocusedTabIndex=e,this.focusChange.emit(this._createChangeEvent(e))}_createChangeEvent(e){let i=new Tb;return i.index=e,this._tabs&&this._tabs.length&&(i.tab=this._tabs.toArray()[e]),i}_subscribeToTabLabels(){this._tabLabelSubscription&&this._tabLabelSubscription.unsubscribe(),this._tabLabelSubscription=It(...this._tabs.map(e=>e._stateChanges)).subscribe(()=>this._changeDetectorRef.markForCheck())}_clampTabIndex(e){return Math.min(this._tabs.length-1,Math.max(e||0,0))}_getTabLabelId(e,i){return e.id||`${this._groupId}-label-${i}`}_getTabContentId(e){return`${this._groupId}-content-${e}`}_setTabBodyWrapperHeight(e){if(!this.dynamicHeight||!this._tabBodyWrapperHeight){this._tabBodyWrapperHeight=e;return}let i=this._tabBodyWrapper.nativeElement;i.style.height=this._tabBodyWrapperHeight+`px`,this._tabBodyWrapper.nativeElement.offsetHeight&&(i.style.height=e+`px`)}_removeTabBodyWrapperHeight(){let e=this._tabBodyWrapper.nativeElement;this._tabBodyWrapperHeight=e.clientHeight,e.style.height=``,this._ngZone.run(()=>this.animationDone.emit())}_handleClick(e,i,a){i.focusIndex=a,e.disabled||(this.selectedIndex=a)}_getTabIndex(e){return e===(this._lastFocusedTabIndex??this.selectedIndex)?0:-1}_tabFocusChanged(e,i){e&&e!==`mouse`&&e!==`touch`&&(this._tabHeader.focusIndex=i)}_bodyCentered(e){e&&this._tabBodies?.forEach((i,a)=>i._setActiveClass(a===this._selectedIndex))}_bodyAnimationsDisabled(){return this._diAnimationsDisabled||this._bodyAnimationDuration===`0`||this._bodyAnimationDuration===`0ms`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`tabBodyWrapper`],i=[`tabHeader`],a=[`*`];function o(g,v){}function r(g,v){if(g&1&&ot(0,o,0,0,`ng-template`,12),g&2){let b=_().$implicit;S(`cdkPortalOutlet`,b.templateLabel)}}function s(g,v){if(g&1&&u(0),g&2){let b=_().$implicit;w(b.textLabel)}}function c(g,v){if(g&1){let b=q();l(0,`div`,7,2),C(`click`,function(){let E=N(b),P=E.$implicit,ae=E.$index,z=_(),x=ft(1);return A(z._handleClick(P,x,ae))})(`cdkFocusChange`,function(E){let P=N(b).$index;return A(_()._tabFocusChanged(E,P))}),H(2,`span`,8)(3,`div`,9),l(4,`span`,10)(5,`span`,11),D(6,r,1,1,null,12)(7,s,1,1),d()()()}if(g&2){let b=v.$implicit,y=v.$index,E=ft(1),P=_();tt(b.labelClass),Y(`mdc-tab--active`,P.selectedIndex===y),S(`id`,P._getTabLabelId(b,y))(`disabled`,b.disabled)(`fitInkBarToContent`,P.fitInkBarToContent),te(`tabIndex`,P._getTabIndex(y))(`aria-posinset`,y+1)(`aria-setsize`,P._tabs.length)(`aria-controls`,P._getTabContentId(y))(`aria-selected`,P.selectedIndex===y)(`aria-label`,b.ariaLabel||null)(`aria-labelledby`,!b.ariaLabel&&b.ariaLabelledby?b.ariaLabelledby:null),m(3),S(`matRippleTrigger`,E)(`matRippleDisabled`,b.disabled||P.disableRipple),m(3),I(b.templateLabel?6:7)}}function h(g,v){g&1&&he(0)}function p(g,v){if(g&1){let b=q();l(0,`mat-tab-body`,13),C(`_onCentered`,function(){N(b);return A(_()._removeTabBodyWrapperHeight())})(`_onCentering`,function(E){N(b);return A(_()._setTabBodyWrapperHeight(E))})(`_beforeCentering`,function(E){N(b);return A(_()._bodyCentered(E))}),d()}if(g&2){let b=v.$implicit,y=v.$index,E=_();tt(b.bodyClass),S(`id`,E._getTabContentId(y))(`content`,b.content)(`position`,b.position)(`animationDuration`,E._bodyAnimationDuration)(`preserveContent`,E.preserveContent),te(`tabindex`,E.contentTabIndex!=null&&E.selectedIndex===y?E.contentTabIndex:null)(`aria-labelledby`,E._getTabLabelId(b,y))(`aria-hidden`,E.selectedIndex!==y)}}return B({type:t,selectors:[[`mat-tab-group`]],contentQueries:function(v,b,y){if(v&1&&jt(y,Yc,5),v&2){let E;Z(E=J())&&(b._allTabs=E)}},viewQuery:function(v,b){if(v&1&&Ze(e,5)(i,5)(Mb,5),v&2){let y;Z(y=J())&&(b._tabBodyWrapper=y.first),Z(y=J())&&(b._tabHeader=y.first),Z(y=J())&&(b._tabBodies=y)}},hostAttrs:[1,`mat-mdc-tab-group`],hostVars:13,hostBindings:function(v,b){v&2&&(te(`mat-align-tabs`,b.alignTabs),tt(`mat-`+(b.color||`primary`)),gn(`--%NS%mat-tab-body-animation-duration`,b._bodyAnimationDuration)(`--%NS%mat-tab-header-animation-duration`,b._headerAnimationDuration),Y(`mat-mdc-tab-group-dynamic-height`,b.dynamicHeight)(`mat-mdc-tab-group-inverted-header`,b.headerPosition===`below`)(`mat-mdc-tab-group-stretch-tabs`,b.stretchTabs))},inputs:{color:`color`,fitInkBarToContent:[2,`fitInkBarToContent`,`fitInkBarToContent`,V],stretchTabs:[2,`mat-stretch-tabs`,`stretchTabs`,V],alignTabs:[0,`mat-align-tabs`,`alignTabs`],dynamicHeight:[2,`dynamicHeight`,`dynamicHeight`,V],selectedIndex:[2,`selectedIndex`,`selectedIndex`,lt],headerPosition:`headerPosition`,animationDuration:`animationDuration`,contentTabIndex:[2,`contentTabIndex`,`contentTabIndex`,lt],disablePagination:[2,`disablePagination`,`disablePagination`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V],preserveContent:[2,`preserveContent`,`preserveContent`,V],backgroundColor:`backgroundColor`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`]},outputs:{selectedIndexChange:`selectedIndexChange`,focusChange:`focusChange`,animationDone:`animationDone`,selectedTabChange:`selectedTabChange`},exportAs:[`matTabGroup`],features:[Ge([{provide:Rw,useExisting:t}])],ngContentSelectors:a,decls:9,vars:8,consts:[[`tabHeader`,``],[`tabBodyWrapper`,``],[`tabNode`,``],[3,`indexFocused`,`selectFocusedIndex`,`selectedIndex`,`disableRipple`,`disablePagination`,`aria-label`,`aria-labelledby`],[`role`,`tab`,`matTabLabelWrapper`,``,`cdkMonitorElementFocus`,``,1,`mdc-tab`,`mat-mdc-tab`,`mat-focus-indicator`,3,`id`,`mdc-tab--active`,`class`,`disabled`,`fitInkBarToContent`],[1,`mat-mdc-tab-body-wrapper`],[`role`,`tabpanel`,3,`id`,`class`,`content`,`position`,`animationDuration`,`preserveContent`],[`role`,`tab`,`matTabLabelWrapper`,``,`cdkMonitorElementFocus`,``,1,`mdc-tab`,`mat-mdc-tab`,`mat-focus-indicator`,3,`click`,`cdkFocusChange`,`id`,`disabled`,`fitInkBarToContent`],[1,`mdc-tab__ripple`],[`mat-ripple`,``,1,`mat-mdc-tab-ripple`,3,`matRippleTrigger`,`matRippleDisabled`],[1,`mdc-tab__content`],[1,`mdc-tab__text-label`],[3,`cdkPortalOutlet`],[`role`,`tabpanel`,3,`_onCentered`,`_onCentering`,`_beforeCentering`,`id`,`content`,`position`,`animationDuration`,`preserveContent`]],template:function(v,b){v&1&&(Pe(),l(0,`mat-tab-header`,3,0),C(`indexFocused`,function(E){return b._focusChanged(E)})(`selectFocusedIndex`,function(E){return b.selectedIndex=E}),ue(2,c,8,17,`div`,4,Pn),d(),D(4,h,1,0),l(5,`div`,5,1),ue(7,p,1,10,`mat-tab-body`,6,Pn),d()),v&2&&(S(`selectedIndex`,b.selectedIndex||0)(`disableRipple`,b.disableRipple)(`disablePagination`,b.disablePagination),kr(`aria-label`,b.ariaLabel)(`aria-labelledby`,b.ariaLabelledby),m(2),me(b._tabs),m(2),I(b._isServer?4:-1),m(),Y(`_mat-animation-noopable`,b._bodyAnimationsDisabled()),m(2),me(b._tabs))},dependencies:[pR,Pw,Ag,ci,Aa,Mb],styles:[`.mdc-tab {
  min-width: 90px;
  padding: 0 24px;
  display: flex;
  flex: 1 0 auto;
  justify-content: center;
  box-sizing: border-box;
  border: none;
  outline: none;
  text-align: center;
  white-space: nowrap;
  cursor: pointer;
  z-index: 1;
  touch-action: manipulation;
}

.mdc-tab__content {
  display: flex;
  align-items: center;
  justify-content: center;
  height: inherit;
  pointer-events: none;
}

.mdc-tab__text-label {
  transition: 150ms color linear;
  display: inline-block;
  line-height: 1;
  z-index: 2;
}

.mdc-tab--active .mdc-tab__text-label {
  transition-delay: 100ms;
}

._mat-animation-noopable .mdc-tab__text-label {
  transition: none;
}

.mdc-tab-indicator {
  display: flex;
  position: absolute;
  top: 0;
  left: 0;
  justify-content: center;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.mdc-tab-indicator__content {
  transition: var(--%NS%mat-tab-header-animation-duration, 250ms) transform cubic-bezier(0.4, 0, 0.2, 1);
  transform-origin: left;
  opacity: 0;
}

.mdc-tab-indicator__content--underline {
  align-self: flex-end;
  box-sizing: border-box;
  width: 100%;
  border-top-style: solid;
}

.mdc-tab-indicator--active .mdc-tab-indicator__content {
  opacity: 1;
}

._mat-animation-noopable .mdc-tab-indicator__content, .mdc-tab-indicator--no-transition .mdc-tab-indicator__content {
  transition: none;
}

.mat-mdc-tab-ripple.mat-mdc-tab-ripple {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  pointer-events: none;
}

.mat-mdc-tab {
  -webkit-tap-highlight-color: transparent;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-decoration: none;
  background: none;
  height: var(--%NS%mat-tab-container-height, 48px);
  font-family: var(--%NS%mat-tab-label-text-font, var(--%NS%mat-sys-title-small-font));
  font-size: var(--%NS%mat-tab-label-text-size, var(--%NS%mat-sys-title-small-size));
  letter-spacing: var(--%NS%mat-tab-label-text-tracking, var(--%NS%mat-sys-title-small-tracking));
  line-height: var(--%NS%mat-tab-label-text-line-height, var(--%NS%mat-sys-title-small-line-height));
  font-weight: var(--%NS%mat-tab-label-text-weight, var(--%NS%mat-sys-title-small-weight));
}
.mat-mdc-tab.mdc-tab {
  flex-grow: 0;
}
.mat-mdc-tab .mdc-tab-indicator__content--underline {
  border-color: var(--%NS%mat-tab-active-indicator-color, var(--%NS%mat-sys-primary));
  border-top-width: var(--%NS%mat-tab-active-indicator-height, 2px);
  border-radius: var(--%NS%mat-tab-active-indicator-shape, 0);
}
.mat-mdc-tab:hover .mdc-tab__text-label {
  color: var(--%NS%mat-tab-inactive-hover-label-text-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-tab:focus .mdc-tab__text-label {
  color: var(--%NS%mat-tab-inactive-focus-label-text-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-tab.mdc-tab--active .mdc-tab__text-label {
  color: var(--%NS%mat-tab-active-label-text-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-tab.mdc-tab--active .mdc-tab__ripple::before,
.mat-mdc-tab.mdc-tab--active .mat-ripple-element {
  background-color: var(--%NS%mat-tab-active-ripple-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-tab.mdc-tab--%NS%active:hover .mdc-tab__text-label {
  color: var(--%NS%mat-tab-active-hover-label-text-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-tab.mdc-tab--%NS%active:hover .mdc-tab-indicator__content--underline {
  border-color: var(--%NS%mat-tab-active-hover-indicator-color, var(--%NS%mat-sys-primary));
}
.mat-mdc-tab.mdc-tab--%NS%active:focus .mdc-tab__text-label {
  color: var(--%NS%mat-tab-active-focus-label-text-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-tab.mdc-tab--%NS%active:focus .mdc-tab-indicator__content--underline {
  border-color: var(--%NS%mat-tab-active-focus-indicator-color, var(--%NS%mat-sys-primary));
}
.mat-mdc-tab.mat-mdc-tab-disabled {
  opacity: 0.4;
  pointer-events: none;
}
.mat-mdc-tab.mat-mdc-tab-disabled .mdc-tab__content {
  pointer-events: none;
}
.mat-mdc-tab.mat-mdc-tab-disabled .mdc-tab__ripple::before,
.mat-mdc-tab.mat-mdc-tab-disabled .mat-ripple-element {
  background-color: var(--%NS%mat-tab-disabled-ripple-color, var(--%NS%mat-sys-on-surface-variant));
}
.mat-mdc-tab .mdc-tab__ripple::before {
  content: "";
  display: block;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0;
  pointer-events: none;
  background-color: var(--%NS%mat-tab-inactive-ripple-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-tab .mdc-tab__text-label {
  color: var(--%NS%mat-tab-inactive-label-text-color, var(--%NS%mat-sys-on-surface));
  display: inline-flex;
  align-items: center;
}
.mat-mdc-tab .mdc-tab__content {
  position: relative;
  pointer-events: auto;
}
.mat-mdc-tab:hover .mdc-tab__ripple::before {
  opacity: 0.04;
}
.mat-mdc-tab.cdk-program-focused .mdc-tab__ripple::before, .mat-mdc-tab.cdk-keyboard-focused .mdc-tab__ripple::before {
  opacity: 0.12;
}
.mat-mdc-tab .mat-ripple-element {
  opacity: 0.12;
  background-color: var(--%NS%mat-tab-inactive-ripple-color, var(--%NS%mat-sys-on-surface));
}
.mat-mdc-tab-group.mat-mdc-tab-group-stretch-tabs > .mat-mdc-tab-header .mat-mdc-tab {
  flex-grow: 1;
}

.mat-mdc-tab-group {
  display: flex;
  flex-direction: column;
  max-width: 100%;
}
.mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header, .mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header-pagination {
  background-color: var(--%NS%mat-tab-background-color);
}
.mat-mdc-tab-group.mat-tabs-with-background.mat-primary > .mat-mdc-tab-header .mat-mdc-tab .mdc-tab__text-label {
  color: var(--%NS%mat-tab-foreground-color);
}
.mat-mdc-tab-group.mat-tabs-with-background.mat-primary > .mat-mdc-tab-header .mdc-tab-indicator__content--underline {
  border-color: var(--%NS%mat-tab-foreground-color);
}
.mat-mdc-tab-group.mat-tabs-with-background:not(.mat-primary) > .mat-mdc-tab-header .mat-mdc-tab:not(.mdc-tab--active) .mdc-tab__text-label {
  color: var(--%NS%mat-tab-foreground-color);
}
.mat-mdc-tab-group.mat-tabs-with-background:not(.mat-primary) > .mat-mdc-tab-header .mat-mdc-tab:not(.mdc-tab--active) .mdc-tab-indicator__content--underline {
  border-color: var(--%NS%mat-tab-foreground-color);
}
.mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header .mat-mdc-tab-header-pagination-chevron,
.mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header .mat-focus-indicator::before, .mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header-pagination .mat-mdc-tab-header-pagination-chevron,
.mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header-pagination .mat-focus-indicator::before {
  border-color: var(--%NS%mat-tab-foreground-color);
}
.mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header .mat-ripple-element, .mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header .mdc-tab__ripple::before, .mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header-pagination .mat-ripple-element, .mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header-pagination .mdc-tab__ripple::before {
  background-color: var(--%NS%mat-tab-foreground-color);
}
.mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header .mat-mdc-tab-header-pagination-chevron, .mat-mdc-tab-group.mat-tabs-with-background > .mat-mdc-tab-header-pagination .mat-mdc-tab-header-pagination-chevron {
  color: var(--%NS%mat-tab-foreground-color);
}
.mat-mdc-tab-group.mat-mdc-tab-group-inverted-header {
  flex-direction: column-reverse;
}
.mat-mdc-tab-group.mat-mdc-tab-group-inverted-header .mdc-tab-indicator__content--underline {
  align-self: flex-start;
}

.mat-mdc-tab-body-wrapper {
  position: relative;
  overflow: hidden;
  display: flex;
  transition: height 500ms cubic-bezier(0.35, 0, 0.25, 1);
}
.mat-mdc-tab-body-wrapper._mat-animation-noopable {
  transition: none !important;
  animation: none !important;
}
`],encapsulation:2,changeDetection:1})})()}return t})();var Tb=class{index;tab};var hm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[we]})}return t})();var fR=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[`mat-toolbar-row`]],hostAttrs:[1,`mat-toolbar-row`],exportAs:[`matToolbarRow`]})}return t})();var fm=(()=>{class t{_elementRef=f(W);_platform=f(Oe);_document=f(de);color;_toolbarRows;ngAfterViewInit(){this._platform.isBrowser&&(this._checkToolbarMixedModes(),this._toolbarRows.changes.subscribe(()=>this._checkToolbarMixedModes()))}_checkToolbarMixedModes(){this._toolbarRows.length}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[`mat-toolbar-row`]]];return B({type:t,selectors:[[`mat-toolbar`]],contentQueries:function(o,r,s){if(o&1&&jt(s,fR,5),o&2){let c;Z(c=J())&&(r._toolbarRows=c)}},hostAttrs:[1,`mat-toolbar`],hostVars:6,hostBindings:function(o,r){o&2&&(tt(r.color?`mat-`+r.color:``),Y(`mat-toolbar-multiple-rows`,r._toolbarRows.length>0)(`mat-toolbar-single-row`,r._toolbarRows.length===0))},inputs:{color:`color`},exportAs:[`matToolbar`],ngContentSelectors:[`*`,`mat-toolbar-row`],decls:2,vars:0,template:function(o,r){o&1&&(Pe(e),he(0),he(1,1))},styles:[`.mat-toolbar {
  background: var(--%NS%mat-toolbar-container-background-color, var(--%NS%mat-sys-surface));
  color: var(--%NS%mat-toolbar-container-text-color, var(--%NS%mat-sys-on-surface));
}
.mat-toolbar, .mat-toolbar h1, .mat-toolbar h2, .mat-toolbar h3, .mat-toolbar h4, .mat-toolbar h5, .mat-toolbar h6 {
  font-family: var(--%NS%mat-toolbar-title-text-font, var(--%NS%mat-sys-title-large-font));
  font-size: var(--%NS%mat-toolbar-title-text-size, var(--%NS%mat-sys-title-large-size));
  line-height: var(--%NS%mat-toolbar-title-text-line-height, var(--%NS%mat-sys-title-large-line-height));
  font-weight: var(--%NS%mat-toolbar-title-text-weight, var(--%NS%mat-sys-title-large-weight));
  letter-spacing: var(--%NS%mat-toolbar-title-text-tracking, var(--%NS%mat-sys-title-large-tracking));
  margin: 0;
}
@media (forced-colors: active) {
  .mat-toolbar {
    outline: solid 1px;
  }
}
.mat-toolbar .mat-form-field-underline,
.mat-toolbar .mat-form-field-ripple,
.mat-toolbar .mat-focused .mat-form-field-ripple {
  background-color: currentColor;
}
.mat-toolbar .mat-form-field-label,
.mat-toolbar .mat-focused .mat-form-field-label,
.mat-toolbar .mat-select-value,
.mat-toolbar .mat-select-arrow,
.mat-toolbar .mat-form-field.mat-focused .mat-select-arrow {
  color: inherit;
}
.mat-toolbar .mat-input-element {
  caret-color: currentColor;
}
.mat-toolbar .mat-mdc-button-base.mat-mdc-button-base.mat-unthemed {
  --%NS%mat-button-text-label-text-color: var(--%NS%mat-toolbar-container-text-color, var(--%NS%mat-sys-on-surface));
  --%NS%mat-button-outlined-label-text-color: var(--%NS%mat-toolbar-container-text-color, var(--%NS%mat-sys-on-surface));
}

.mat-toolbar-row, .mat-toolbar-single-row {
  display: flex;
  box-sizing: border-box;
  padding: 0 16px;
  width: 100%;
  flex-direction: row;
  align-items: center;
  white-space: nowrap;
  height: var(--%NS%mat-toolbar-standard-height, 64px);
}
@media (max-width: 599px) {
  .mat-toolbar-row, .mat-toolbar-single-row {
    height: var(--%NS%mat-toolbar-mobile-height, 56px);
  }
}

.mat-toolbar-multiple-rows {
  display: flex;
  box-sizing: border-box;
  flex-direction: column;
  width: 100%;
  min-height: var(--%NS%mat-toolbar-standard-height, 64px);
}
@media (max-width: 599px) {
  .mat-toolbar-multiple-rows {
    min-height: var(--%NS%mat-toolbar-mobile-height, 56px);
  }
}
`],encapsulation:2})})()}return t})();var gm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[we]})}return t})();function un(t){t||(t=f(Ne));let n=new Ce(e=>{if(t.destroyed){e.next();return}return t.onDestroy(e.next.bind(e))});return e=>e.pipe(Te(n))}var jw=v(Bw());var In={apiBaseUrl:``,websocketUrl:`${window.location.protocol===`https:`?`wss:`:`ws:`}//${window.location.host}/websocket`,websocketDebug:!1};var cs=class t{http=f(Di);destroyRef=f(Ne);envelopeSubject=new L;connectedSubject=new L;disconnectedSubject=new L;errorSubject=new L;stateSubject=new hi(`connecting`);pending=new Map;client=null;subscription=null;retryTimer=null;connected=!1;synchronization=null;again=!1;generation=0;delay=500;logId=null;cursor=-1;envelopes$=this.envelopeSubject.asObservable();connected$=this.connectedSubject.asObservable();disconnected$=this.disconnectedSubject.asObservable();errors$=this.errorSubject.asObservable();state$=this.stateSubject.asObservable();get lastEventId(){return this.cursor}get eventLogId(){return this.logId}constructor(){this.destroyRef.onDestroy(()=>{this.stop()})}start(){if(this.client)return;let n=new jw.Client({brokerURL:In.websocketUrl,heartbeatIncoming:4e3,heartbeatOutgoing:4e3,reconnectDelay:1e3,debug:In.websocketDebug?e=>console.debug(`[STOMP] `+e):()=>{}});n.onConnect=()=>{this.subscription?.unsubscribe(),this.subscription=n.subscribe(`/topic/events`,e=>this.receiveMessage(e)),this.connected=!0,this.generation++,this.connectedSubject.next(),this.retryNow()},n.onWebSocketClose=()=>{this.subscription=null,this.connected=!1,this.generation++,this.stateSubject.next(`disconnected`),this.disconnectedSubject.next(),this.clearTimer()},n.onWebSocketError=e=>this.fail(e),n.onStompError=e=>this.fail(e),this.client=n,n.activate()}async stop(){this.connected=!1,this.generation++,this.clearTimer(),this.pending.clear(),this.subscription?.unsubscribe(),this.subscription=null;let n=this.client;this.client=null,n&&await n.deactivate()}retryNow(){this.clearTimer(),this.delay=500,this.requestSync()}getHead(){return St(this.http.get(In.apiBaseUrl+`/lastevent`).pipe(un(this.destroyRef)))}getEvents(n,e,i){return St(this.http.get(In.apiBaseUrl+`/events`,{params:{eventLogId:n,from:e,to:i}}).pipe(un(this.destroyRef)))}receiveMessage(n){try{let e=JSON.parse(n.body);if(this.validate(e),e.eventLogId===this.logId&&e.eventId<=this.cursor)return;this.pending.size>=5e3&&(this.pending.clear(),this.requestSync()),this.pending.set(this.key(e.eventLogId,e.eventId),e),this.stateSubject.value===`live`&&e.eventLogId===this.logId&&this.drain(),(e.eventLogId!==this.logId||this.hasGap())&&this.requestSync()}catch(e){this.fail(e)}}requestSync(){if(!this.connected)return;if(this.synchronization){this.again=!0;return}this.again=!1;let n=this.generation;this.synchronization=this.synchronize(n).catch(e=>{n===this.generation&&this.connected&&this.fail(e)}).finally(()=>{this.synchronization=null,this.again&&this.connected&&this.requestSync()})}async synchronize(n){this.stateSubject.next(`syncing`);let e=await this.getHead();if(this.current(n)){if(typeof e.eventLogId!=`string`||!Number.isInteger(e.lastEventId)||e.lastEventId<-1)throw new Error(`Invalid event head`);if(this.logId!==e.eventLogId){this.logId=e.eventLogId,this.cursor=e.lastEventId;for(let[i,a]of this.pending)(a.eventLogId!==this.logId||a.eventId<=this.cursor)&&this.pending.delete(i)}else{if(e.lastEventId<this.cursor)throw new Error(`Cursor moved backwards without a new log identity`);await this.download(this.cursor+1,e.lastEventId+1,n)}if(this.current(n)){this.drain();for(let i=0;this.hasGap();i++){if(i===10)throw new Error(`Live delivery has not caught up`);let a=this.smallestPending();if(a===null)break;if(await this.download(this.cursor+1,a,n),!this.current(n))return;this.drain()}if(this.cursor<e.lastEventId)throw new Error(`Incomplete event history`);this.current(n)&&(this.delay=500,this.clearTimer(),this.stateSubject.next(`live`))}}}async download(n,e,i){for(let a=n;a<e;){let o=Math.min(e,a+1e3),r=this.logId,s=await this.getEvents(r,a,o);if(!this.current(i))return;if(s.eventLogId!==r||s.from!==a||s.to!==o||s.events.length!==o-a)throw new Error(`Incomplete or obsolete history`);let c=a;for(let h of s.events){if(this.validate(h),h.eventLogId!==r||h.eventId!==c++)throw new Error(`Non-contiguous history`);h.eventId>this.cursor&&this.pending.set(this.key(r,h.eventId),h)}this.drain(),a=o}}drain(){if(this.logId)for(let n=this.pending.get(this.key(this.logId,this.cursor+1));n;n=this.pending.get(this.key(this.logId,this.cursor+1)))this.pending.delete(this.key(this.logId,n.eventId)),this.cursor=n.eventId,this.envelopeSubject.next(n)}smallestPending(){let n=null;for(let e of this.pending.values())e.eventLogId===this.logId&&e.eventId>this.cursor&&(n===null||e.eventId<n)&&(n=e.eventId);return n}hasGap(){let n=this.smallestPending();return n!==null&&n>this.cursor+1}current(n){return this.connected&&n===this.generation}key(n,e){return n+`:`+e}clearTimer(){this.retryTimer&&clearTimeout(this.retryTimer),this.retryTimer=null}fail(n){this.errorSubject.next(n),this.stateSubject.next(`retrying`),this.connected&&!this.retryTimer&&(this.retryTimer=setTimeout(()=>{this.retryTimer=null,this.requestSync()},this.delay),this.delay=Math.min(3e4,this.delay*2))}validate(n){if(!n||typeof n.eventLogId!=`string`||!Number.isInteger(n.eventId)||n.eventId<0||typeof n.type!=`string`||!Number.isFinite(n.timestamp)||typeof n.workerName!=`string`||!n.payload||typeof n.payload!=`object`)throw new Error(`Invalid envelope`);let e=n.payload;if(n.type===`SolutionGeneratedEvent`&&(typeof e.resultId!=`string`||typeof e.success!=`boolean`||typeof e.iteration!=`string`||!e.objectives||typeof e.objectives!=`object`))throw new Error(`Invalid result event`);if(n.type===`ExecutionStartedEvent`&&(!e.objectives||typeof e.objectives!=`object`||!Array.isArray(e.experimentNames)))throw new Error(`Invalid execution event`);if(n.type===`ErrorEvent`&&typeof e.exceptionType!=`string`)throw new Error(`Invalid error event`)}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Fa=class t{http=f(Di);destroyRef=f(Ne);get(n,e={}){return St(this.http.get(`${In.apiBaseUrl}${n}`,{params:e}).pipe(un(this.destroyRef)))}getMode(){return this.get(`/api/monitor/status`)}getStatus(){return this.get(`/api/experiments/status`)}getInstances(n,e){return this.get(`/api/experiments/instances`,r({runId:n},e))}getInstance(n,e,i){return this.get(`/api/experiments/instances/${encodeURIComponent(e)}`,{runId:n,objective:i})}getResults(n,e){let i=s(r({runId:n},e),{order:e.order??`PLANNED`});return e.state||delete i.state,this.get(`/api/experiments/results`,i)}getResult(n,e){return this.get(`/api/experiments/results/${encodeURIComponent(e)}`,{runId:n})}getIssues(n,e,i){return this.get(`/api/experiments/issues`,{runId:n,severity:e,offset:i,limit:25})}getArtifacts(n){return this.get(`/api/experiments/artifacts`,{runId:n})}artifactUrl(n,e){return`${In.apiBaseUrl}/api/experiments/artifacts/${encodeURIComponent(e)}?${new URLSearchParams({runId:n})}`}exportUrl(n,e,i){let a=new URLSearchParams({runId:n,format:i,instanceId:e.instanceId,algorithm:e.algorithm,search:e.search,order:e.order??`PLANNED`});return e.state&&a.set(`state`,e.state),`${In.apiBaseUrl}/api/experiments/results/export?${a}`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var La=class{static buildInstance(n,e,i,a=!0){let o=this.referenceValue(n.started.refValues,e);return{key:n.key,experimentName:n.started.experimentName,instanceName:n.started.instanceName,selectedObjective:e,referenceValue:o,scoreChart:this.buildScoreChart(n,e,o,a),convergenceChart:this.buildConvergenceChart(n,e,i,o,a),bestSolution:this.findBest(n.solutions,e,i)}}static buildScoreChart(n,e,i,a){let o=this.groupSuccessfulPoints(n,e,!1,null);return{options:this.chartOptions(`${n.started.instanceName} scores`,e,i,o,`Raw score for every successful repetition.`,a)}}static buildConvergenceChart(n,e,i,a,o){let r=this.groupSuccessfulPoints(n,e,!0,i);return{options:this.chartOptions(`${n.started.instanceName} best score across repetitions`,e,a,r,`Best score reached by each algorithm over completed repetitions.`,o)}}static groupSuccessfulPoints(n,e,i,a){if(!e)return[];let o=[...n.started.algorithms];for(let s of n.solutions)o.includes(s.event.algorithmName)||o.push(s.event.algorithmName);let r=[];for(let s of o){let c=[],h=null;for(let p of n.solutions){let g=p.event;if(g.algorithmName!==s)continue;let v=g.success?this.objectiveValue(g,e):null;if(v===null){c.push({x:p.ordinal,y:null,custom:{iteration:g.iteration}});continue}i&&(h=h===null||this.isBetter(v,h,a)?v:h),c.push({x:p.ordinal,y:i?h:v,custom:{iteration:g.iteration}})}r.push({type:`line`,name:s,data:c,lineWidth:i?2:1,marker:{enabled:!i},animation:!1,turboThreshold:0})}return r}static referenceValue(n,e){let i=e?n[e]:void 0;return typeof i==`number`&&Number.isFinite(i)?i:null}static scoreAxis(n,e,i){let a=i?e:null,o=[];return a!==null&&o.push({id:`reference`,className:`reference-line`,value:a,color:`#d32f2f`,dashStyle:`Dash`,width:2,zIndex:4,label:{text:`Reference: ${a}`}}),{title:{text:n??`Score`},plotLines:o,softMin:a??void 0,softMax:a??void 0}}static chartOptions(n,e,i,a,o,r){return{accessibility:{enabled:!0,description:o},boost:{enabled:!0,useGPUTranslations:!0,seriesThreshold:25},chart:{animation:!1,height:320,spacing:[12,12,12,12]},credits:{enabled:!1},exporting:{enabled:!0},legend:{enabled:!0},title:{text:n,style:{display:`none`}},subtitle:{text:e?`Objective: ${e}`:`Waiting for objective metadata`},xAxis:{allowDecimals:!1,min:1,title:{text:`Repetition`}},yAxis:this.scoreAxis(e,i,r),tooltip:{headerFormat:``,pointFormat:`<b>{series.name}</b><br/>Repetition: {point.x}<br/>Iteration: {point.custom.iteration}<br/>Value: {point.y}`},plotOptions:{series:{animation:!1,connectNulls:!1}},series:a}}static findBest(n,e,i){if(!e||!i)return null;let a=null,o=null;for(let r of n){if(!r.event.success)continue;let s=this.objectiveValue(r.event,e);s!==null&&(o===null||this.isBetter(s,o,i))&&(o=s,a=r.event)}return a}static objectiveValue(n,e){let i=n.objectives[e];return typeof i==`number`&&Number.isFinite(i)?i:null}static isBetter(n,e,i){return i===`MAXIMIZE`?n>e:n<e}};var ls=class{static instance(n,e,i=!0){return La.buildInstance({key:n.instance.id,started:{experimentName:n.instance.experimentName,instanceName:n.instance.instanceName,algorithms:n.algorithms.map(a=>a.name),repetitions:n.instance.counts.planned,refValues:n.instance.references},solutions:n.results.map(a=>({ordinal:a.ordinal,event:{resultId:a.resultId,success:a.state===`SUCCEEDED`,experimentName:a.experimentName,instanceName:a.instanceName,algorithmName:a.algorithmName,iteration:a.iteration,objectives:a.objectives,score:a.objectives[n.objective]??NaN,executionTime:a.executionTime??-1,timeToBest:a.timeToBest??-1}}))},n.objective,e,i)}static distribution(n,e=!0){return{options:{chart:{type:`boxplot`,height:320,animation:!1},plotOptions:{series:{animation:!1}},title:{text:`Score distribution`},credits:{enabled:!1},exporting:{enabled:!0},accessibility:{enabled:!0},xAxis:{categories:n.algorithms.map(i=>i.name)},yAxis:La.scoreAxis(n.objective,La.referenceValue(n.instance.references,n.objective),e),series:[{type:`boxplot`,name:n.objective,data:n.algorithms.flatMap((i,a)=>{let o=i.objective;return o.best===null||o.worst===null?[]:[{x:a,low:Math.min(o.best,o.worst),q1:o.lowerQuartile,median:o.median,q3:o.upperQuartile,high:Math.max(o.best,o.worst)}]})}]}}}static qualityRuntime(n,e=!0){return{options:{chart:{type:`scatter`,height:320,animation:!1},plotOptions:{series:{animation:!1}},title:{text:`Score versus runtime`},credits:{enabled:!1},exporting:{enabled:!0},accessibility:{enabled:!0},xAxis:{title:{text:`Runtime (ms)`}},yAxis:La.scoreAxis(n.objective,La.referenceValue(n.instance.references,n.objective),e),tooltip:{pointFormat:`Runtime: {point.x} ms<br/>Score: {point.y}<br/>Iteration: {point.custom.iteration}`},series:n.algorithms.map(i=>({type:`scatter`,name:i.name,data:n.results.filter(a=>a.algorithmName===i.name&&a.state===`SUCCEEDED`&&a.executionTime!==null&&Number.isFinite(a.objectives[n.objective])).map(a=>({x:a.executionTime/1e6,y:a.objectives[n.objective],custom:{iteration:a.iteration}}))}))}}}};var Kc=class t{client=f(Fa);events=f(cs);destroyRef=f(Ne);statusState=T(null);status=this.statusState.asReadonly();syncStateState=T(`connecting`);syncState=this.syncStateState.asReadonly();transportStateState=T(`connecting`);transportState=this.transportStateState.asReadonly();latestErrorState=T(null);latestError=this.latestErrorState.asReadonly();transportErrorState=T(null);transportError=this.transportErrorState.asReadonly();lastUpdatedState=T(null);lastUpdated=this.lastUpdatedState.asReadonly();selectedObjectiveState=T(null);selectedObjective=this.selectedObjectiveState.asReadonly();showReferenceState=T(!0);showReference=this.showReferenceState.asReadonly();selectedInstanceIdState=T(null);selectedInstanceId=this.selectedInstanceIdState.asReadonly();instancePageState=T(null);instancePage=this.instancePageState.asReadonly();resultPageState=T(null);resultPage=this.resultPageState.asReadonly();issuePageState=T(null);issuePage=this.issuePageState.asReadonly();artifactsState=T(null);artifacts=this.artifactsState.asReadonly();detailState=T(null);detail=this.detailState.asReadonly();inspectedResultState=T(null);inspectedResult=this.inspectedResultState.asReadonly();resultErrorState=T(null);resultError=this.resultErrorState.asReadonly();tabState=T(0);tab=this.tabState.asReadonly();instanceQueryState=T({experiment:``,search:``,offset:0,limit:25});instanceQuery=this.instanceQueryState.asReadonly();resultQueryState=T({instanceId:``,algorithm:``,state:``,search:``,offset:0,limit:25});resultQuery=this.resultQueryState.asReadonly();issueSeverityState=T(``);issueSeverity=this.issueSeverityState.asReadonly();issueOffsetState=T(0);issueOffset=this.issueOffsetState.asReadonly();seenIssueIdState=T(0);seenIssueId=this.seenIssueIdState.asReadonly();objectives=ne(()=>Object.entries(this.status()?.objectives??{}).map(([n,e])=>({name:n,mode:e})));terminal=ne(()=>[`SUCCEEDED`,`COMPLETED_WITH_ERRORS`,`FAILED`,`CANCELLED`].includes(this.status()?.phase??``));connectionLabel=ne(()=>this.syncState()===`retrying`?`Retrying · data may be stale`:this.syncState()===`connecting`?`Connecting`:this.transportState()===`live`?`Live`:`Live via HTTP`);unseenIssues=ne(()=>Math.max(0,(this.status()?.issueCount??0)-this.seenIssueId()));estimatedRemainingMillis=ne(()=>{let n=this.status();if(!n||n.phase!==`RUNNING`||n.elapsedMillis<1e4)return null;let e=n.counts.succeeded+n.counts.failed;return e<5||n.counts.cancelled>0?null:n.elapsedMillis/e*(n.counts.pending+n.counts.running)});charts=ne(()=>{let n=this.detail(),e=this.selectedObjective(),i=this.selectedObjectiveMode();return n&&e&&i&&n.objective===e?ls.instance(n,i,this.showReference()):null});selectedObjectiveMode=ne(()=>this.status()?.objectives[this.selectedObjective()??``]??null);distribution=ne(()=>this.detail()?ls.distribution(this.detail(),this.showReference()):null);qualityRuntime=ne(()=>this.detail()?ls.qualityRuntime(this.detail(),this.showReference()):null);lastEventId=ne(()=>(this.transportState(),this.lastUpdated(),this.events.lastEventId));progress=ne(()=>{let n=this.status(),e=this.detail(),i=e?.algorithms.find(o=>o.name===this.resultQuery().algorithm),a=i?.counts??e?.instance.counts??n?.counts;return{instances:{completed:n?.completedInstances??0,total:n?.instances??0},algorithms:{completed:e?.instance.completedAlgorithms??0,total:e?.instance.algorithms??0},repetitions:{completed:(a?.succeeded??0)+(a?.failed??0),total:a?.planned??0},repetitionScope:i?`selected algorithm`:e?`selected instance`:`execution`}});cache=new Map;timer=null;polling=!1;pending=!1;started=!1;destroyed=!1;generation=0;delay=500;forceDetail=!0;lastDetailFetch=0;resultRequest=0;constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.generation++,this.resultRequest++,this.clearTimer()})}start(){this.started||(this.started=!0,this.events.state$.pipe(un(this.destroyRef)).subscribe(n=>{this.transportStateState.set(n),n===`live`&&this.transportErrorState.set(null)}),this.events.errors$.pipe(un(this.destroyRef)).subscribe(n=>this.transportErrorState.set(this.message(n))),this.events.connected$.pipe(un(this.destroyRef)).subscribe(()=>{this.generation++,this.forceDetail=!0,this.refreshNow()}),this.events.envelopes$.pipe(un(this.destroyRef)).subscribe(n=>{n.type!==`PingEvent`&&(n.type===`ExecutionEndedEvent`||n.type===`InstanceProcessingEndedEvent`?(this.forceDetail=!0,this.refreshNow()):this.polling||this.schedule(Math.max(100,500-(Date.now()-(this.lastUpdated()?.getTime()??0)))))}),this.events.start(),this.refreshNow())}retryNow(){this.delay=500,this.events.retryNow(),this.refreshNow()}setReferenceVisible(n){this.showReferenceState.set(n)}selectObjective(n){!this.status()?.objectives[n]||this.selectedObjective()===n||(this.selectedObjectiveState.set(n),this.generation++,this.forceDetail=!0,this.detailState.set(null),this.refreshNow())}selectInstance(n){this.selectedInstanceIdState.set(n),this.generation++,this.forceDetail=!0;let e=this.cache.get(n);this.detailState.set(e?.objective===this.selectedObjective()?e:null),this.refreshNow()}selectTab(n){this.tabState.set(n),this.refreshNow()}filterInstances(n,e){this.instanceQueryState.set(s(r({},this.instanceQuery()),{experiment:n,search:e,offset:0})),this.invalidate()}pageInstances(n){this.instanceQueryState.update(e=>s(r({},e),{offset:n})),this.invalidate()}filterResults(n,e,i,a){this.resultQueryState.set(s(r({},this.resultQuery()),{instanceId:n,algorithm:e,state:i,search:a,offset:0})),this.invalidate()}pageResults(n){this.resultQueryState.update(e=>s(r({},e),{offset:n})),this.invalidate()}sortResults(n){this.resultQueryState.update(e=>s(r({},e),{order:n,offset:0})),this.invalidate()}filterIssues(n){this.issueSeverityState.set(n),this.issueOffsetState.set(0),this.invalidate()}pageIssues(n){this.issueOffsetState.set(n),this.invalidate()}markIssuesSeen(){this.seenIssueIdState.set(this.status()?.issueCount??0);try{localStorage.setItem(`mork.normal.issues.v1`,JSON.stringify({runId:this.status()?.runId,id:this.seenIssueId()}))}catch{}}async inspectResult(n){let e=this.status()?.runId;if(!e)return;let i=++this.resultRequest;this.inspectedResultState.set(null),this.resultErrorState.set(null);try{let[a,o]=await Promise.all([this.client.getResult(e,n),this.client.getArtifacts(e)]);!this.destroyed&&i===this.resultRequest&&e===this.status()?.runId&&a.runId===e&&(this.inspectedResultState.set(a),o.runId===e&&this.artifactsState.set(o))}catch(a){!this.destroyed&&i===this.resultRequest&&e===this.status()?.runId&&this.resultErrorState.set(this.message(a))}}closeResult(){this.resultRequest++,this.inspectedResultState.set(null),this.resultErrorState.set(null)}artifactUrl(n){return this.client.artifactUrl(this.status().runId,n)}exportUrl(n){return this.client.exportUrl(this.status().runId,this.resultQuery(),n)}refreshNow(){this.clearTimer(),this.polling?this.pending=!0:this.poll()}invalidate(){this.generation++,this.refreshNow()}async poll(){if(this.destroyed)return;this.polling=!0,this.pending=!1;let n=this.generation,e=2e3;try{let i=await this.client.getStatus();if(!this.current(n))return;let a=this.status();if(i.runId!==a?.runId){this.cache.clear(),this.detailState.set(null),this.inspectedResultState.set(null),this.resultRequest++,this.instancePageState.set(null),this.resultPageState.set(null),this.issuePageState.set(null),this.artifactsState.set(null),this.selectedInstanceIdState.set(null),this.selectedObjectiveState.set(Object.keys(i.objectives)[0]??null),this.instanceQueryState.set({experiment:``,search:``,offset:0,limit:25}),this.resultQueryState.set({instanceId:``,algorithm:``,state:``,search:``,offset:0,limit:25}),this.seenIssueIdState.set(0),this.forceDetail=!0;try{let o=JSON.parse(localStorage.getItem(`mork.normal.issues.v1`)??`null`);o?.runId===i.runId&&this.seenIssueIdState.set(o.id)}catch{}}if(a?.runId===i.runId&&i.revision<a.revision)throw new Error(`Monitoring revision moved backwards`);if(this.statusState.set(i),i.runId){let o=i.runId,r=await this.client.getInstances(o,this.instanceQuery());if(!this.current(n)||r.runId!==o)return;this.instancePageState.set(r),!this.selectedInstanceId()&&r.items.length&&this.selectedInstanceIdState.set(r.items.find(b=>b.counts.running>0)?.id??r.items[0].id);let s=this.selectedInstanceId(),c=this.selectedObjective(),h=s?this.cache.get(s):null,p=r.items.find(b=>b.id===s),g=!h||h.objective!==c||!p||h.revision!==p.revision,v=[`SUCCEEDED`,`COMPLETED_WITH_ERRORS`,`FAILED`,`CANCELLED`].includes(i.phase)&&i.revision!==a?.revision;if(s&&c&&g&&(this.forceDetail||v||Date.now()-this.lastDetailFetch>=2e3)){let b=await this.client.getInstance(o,s,c);if(!this.current(n)||b.runId!==o||b.objective!==this.selectedObjective())return;for(this.cache.delete(s),this.cache.set(s,b);this.cache.size>10;)this.cache.delete(this.cache.keys().next().value);this.detailState.set(b),this.lastDetailFetch=Date.now(),this.forceDetail=!1}else h&&h.objective===c&&this.detailState.set(h);if(this.tab()===1){let b=await this.client.getResults(o,this.resultQuery());if(!this.current(n)||b.runId!==o)return;this.resultPageState.set(b)}if(this.tab()===2||i.issueCount!==a?.issueCount||!this.issuePage()){let b=await this.client.getIssues(o,this.issueSeverity(),this.issueOffset());if(!this.current(n)||b.runId!==o)return;this.issuePageState.set(b)}if(this.tab()===3){let b=await this.client.getArtifacts(o);if(!this.current(n)||b.runId!==o)return;this.artifactsState.set(b)}}this.latestErrorState.set(null),this.lastUpdatedState.set(new Date),this.syncStateState.set(`live`),this.delay=500,e=this.terminal()||i.phase===`IDLE`?1e4:2e3}catch(i){if(!this.current(n))return;this.latestErrorState.set(this.message(i)),this.syncStateState.set(`retrying`),e=this.delay,this.delay=Math.min(3e4,this.delay*2)}finally{this.polling=!1,this.destroyed||this.schedule(this.pending||n!==this.generation?0:e)}}current(n){return!this.destroyed&&n===this.generation}message(n){return n instanceof Error?n.message:String(n)}schedule(n){this.destroyed||(this.clearTimer(),this.timer=setTimeout(()=>{this.poll()},n))}clearTimer(){this.timer&&clearTimeout(this.timer),this.timer=null}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Nb=[{id:`evaluation-health`,label:`Evaluation health`},{id:`evaluation-cost`,label:`Evaluation cost`},{id:`elite-evolution`,label:`Elite rank evolution`},{id:`current-elites`,label:`Current elites`},{id:`run-information`,label:`Run information`}];var Ab=[{id:`normal-comparison`,label:`Algorithm comparison`},{id:`normal-scores`,label:`Scores`},{id:`normal-best`,label:`Best score across repetitions`},{id:`normal-distribution`,label:`Score distribution`},{id:`normal-runtime`,label:`Score versus runtime`}];var vm=`mork.autoconfig.panelPreferences.v1`;var Va=class t{hiddenSignal=T(new Set);collapsedSignal=T(new Set);hidden=this.hiddenSignal.asReadonly();collapsed=this.collapsedSignal.asReadonly();constructor(){this.restore()}isHidden(n){return this.hiddenSignal().has(n)}isCollapsed(n){return this.collapsedSignal().has(n)}show(n){this.hiddenSignal.update(e=>this.without(e,n)),this.persist()}hide(n){this.hiddenSignal.update(e=>this.with(e,n)),this.persist()}toggleCollapsed(n){this.collapsedSignal.update(e=>e.has(n)?this.without(e,n):this.with(e,n)),this.persist()}reset(){this.hiddenSignal.set(new Set),this.collapsedSignal.set(new Set),localStorage.removeItem(vm)}restore(){try{let n=localStorage.getItem(vm);if(!n)return;let e=JSON.parse(n),i=new Set([...Nb,...Ab].map(a=>a.id));this.hiddenSignal.set(new Set((e.hidden??[]).filter(a=>i.has(a)))),this.collapsedSignal.set(new Set((e.collapsed??[]).filter(a=>i.has(a))))}catch{localStorage.removeItem(vm)}}with(n,e){let i=new Set(n);return i.add(e),i}without(n,e){let i=new Set(n);return i.delete(e),i}persist(){let n={hidden:[...this.hiddenSignal()],collapsed:[...this.collapsedSignal()]};localStorage.setItem(vm,JSON.stringify(n))}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Ai=class t{model=zt.required();static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-dashboard-chart`]],inputs:{model:[1,`model`]},decls:1,vars:2,consts:[[1,`chart`,`highcharts-light`,3,`options`,`oneToOne`]],template:function(e,i){e&1&&H(0,`highcharts-chart`,0),e&2&&S(`options`,i.model().options)(`oneToOne`,!0)},dependencies:[yC],styles:[`[_nghost-%COMP%], .chart[_ngcontent-%COMP%]{display:block;width:100%;min-width:0}`]})};var bR=[`*`];function vR(t,n){t&1&&(l(0,`mat-card-content`),he(1),d())}function _R(t,n){if(t&1){let e=q();l(0,`mat-card`,1)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d(),H(4,`span`,2),l(5,`button`,3),C(`click`,function(){N(e);let a=_();return A(a.preferences.toggleCollapsed(a.panelId()))}),u(6),d(),l(7,`button`,4),u(8,` ⋮ `),d(),l(9,`mat-menu`,null,0)(11,`button`,5),C(`click`,function(){N(e);let a=_();return A(a.preferences.toggleCollapsed(a.panelId()))}),u(12),d(),l(13,`button`,5),C(`click`,function(){N(e);let a=_();return A(a.preferences.hide(a.panelId()))}),u(14,` Hide panel `),d()()(),D(15,vR,2,0,`mat-card-content`),d()}if(t&2){let e=ft(10),i=_();m(3),w(i.title()),m(2),te(`aria-label`,i.preferences.isCollapsed(i.panelId())?`Expand `+i.title():`Collapse `+i.title()),m(),j(` `,i.preferences.isCollapsed(i.panelId())?`+`:`−`,` `),m(),S(`matMenuTriggerFor`,e),te(`aria-label`,i.title()+` options`),m(5),j(` `,i.preferences.isCollapsed(i.panelId())?`Expand panel`:`Collapse panel`,` `),m(3),I(i.preferences.isCollapsed(i.panelId())?-1:15)}}var ds=class t{preferences=f(Va);panelId=zt.required();title=zt.required();static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-monitor-panel`]],inputs:{panelId:[1,`panelId`],title:[1,`title`]},ngContentSelectors:bR,decls:1,vars:1,consts:[[`panelMenu`,`matMenu`],[`appearance`,`outlined`],[1,`spacer`],[`mat-button`,``,`type`,`button`,1,`collapse`,3,`click`],[`mat-button`,``,`type`,`button`,1,`menu`,3,`matMenuTriggerFor`],[`mat-menu-item`,``,`type`,`button`,3,`click`]],template:function(e,i){e&1&&(Pe(),D(0,_R,16,7,`mat-card`,1)),e&2&&I(i.preferences.isHidden(i.panelId())?-1:0)},dependencies:[Pt,en,zn,Bn,jn,Ma,ka,os,Ni,ca,as],styles:[`[_nghost-%COMP%]{display:block;min-width:0}mat-card[_ngcontent-%COMP%]{height:100%}mat-card-header[_ngcontent-%COMP%]{min-height:3.5rem;align-items:center;padding-bottom:.5rem}mat-card-title[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-medium);font-weight:650}mat-card-content[_ngcontent-%COMP%]{min-width:0}.spacer[_ngcontent-%COMP%]{flex:1}.collapse[_ngcontent-%COMP%], .menu[_ngcontent-%COMP%]{min-width:2.25rem;padding:0;font-size:1.25rem}`]})};var yR=(t,n)=>n.id;function xR(t,n){if(t&1&&(l(0,`p`,0),u(1),d()),t&2){let e=n.$implicit;m(),Si(` `,e.severity,` · `,e.scope,`: `,e.exceptionType,` — `,e.message,` `)}}function CR(t,n){t&1&&wa(0)}function SR(t,n){if(t&1&&ot(0,CR,1,0,`ng-container`,2),t&2){let e=_();S(`ngComponentOutlet`,n)(`ngComponentOutletInputs`,e.rendererInputs())}}function wR(t,n){if(t&1&&(l(0,`details`,1)(1,`summary`),u(2,`Solution view data`),d(),l(3,`pre`),u(4),U(5,`json`),d()()),t&2){let e=_();m(4),w(ye(5,1,e.detail().rendering.data))}}function ER(t,n){if(t&1&&(l(0,`p`,0),u(1),d()),t&2){let e=_();m(),j(` `,e.detail().rendering.state===`UNSUPPORTED`?`A problem-specific solution view is not provided for this project.`:`This solution view is no longer retained.`,` Metadata remains available; exported solution files are listed below or in Artifacts. `)}}var DR=new k(`Mork solution renderers`,{providedIn:`root`,factory:()=>({})});var _m=class t{detail=zt.required();selectedObjective=zt.required();registered=f(DR);renderer=ne(()=>this.detail().rendering.state===`AVAILABLE`?this.registered[this.detail().rendering.rendererId??``]??null:null);rendererInputs=ne(()=>({result:this.detail().result,selectedObjective:this.selectedObjective(),view:this.detail().rendering.data}));static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-solution-renderer`]],inputs:{detail:[1,`detail`],selectedObjective:[1,`selectedObjective`]},decls:58,vars:27,consts:[[1,`note`],[`open`,``],[4,`ngComponentOutlet`,`ngComponentOutletInputs`]],template:function(e,i){if(e&1&&(l(0,`dl`)(1,`div`)(2,`dt`),u(3,`Instance / experiment`),d(),l(4,`dd`),u(5),d()(),l(6,`div`)(7,`dt`),u(8,`Algorithm`),d(),l(9,`dd`),u(10),d()(),l(11,`div`)(12,`dt`),u(13,`Iteration / seed`),d(),l(14,`dd`),u(15),d()(),l(16,`div`)(17,`dt`),u(18,`Outcome`),d(),l(19,`dd`),u(20),d()(),l(21,`div`)(22,`dt`),u(23),d(),l(24,`dd`),u(25),U(26,`number`),d()(),l(27,`div`)(28,`dt`),u(29,`Runtime`),d(),l(30,`dd`),u(31),U(32,`number`),d()(),l(33,`div`)(34,`dt`),u(35,`Time to best`),d(),l(36,`dd`),u(37),U(38,`number`),d()(),l(39,`div`)(40,`dt`),u(41,`Result ID`),d(),l(42,`dd`),u(43),d()()(),l(44,`details`)(45,`summary`),u(46,`All objective values and custom properties`),d(),l(47,`pre`),u(48),U(49,`json`),d(),l(50,`pre`),u(51),U(52,`json`),d()(),ue(53,xR,2,4,`p`,0,yR),D(55,SR,1,2,`ng-container`)(56,wR,6,3,`details`,1)(57,ER,2,1,`p`,0)),e&2){let a,o=i.detail().result;m(5),Ae(``,o.instanceName,` / `,o.experimentName),m(5),w(o.algorithmName),m(5),Ae(``,o.iteration,` / `,o.seed),m(5),w(o.state),m(3),w(i.selectedObjective()??`Objective`),m(2),j(` `,ke(26,14,o.objectives[i.selectedObjective()??``],`1.0-4`)??`Unavailable`,` `),m(6),j(` `,o.executionTime===null?`Unavailable`:ke(32,17,o.executionTime/1e6,`1.0-2`)+` ms`,` `),m(6),j(` `,o.timeToBest===null?`Unavailable`:ke(38,20,o.timeToBest/1e6,`1.0-2`)+` ms`,` `),m(6),w(o.resultId),m(5),w(ye(49,23,o.objectives)),m(3),w(ye(52,25,o.properties)),m(2),me(i.detail().issues),m(2),I((a=i.renderer())?55:i.detail().rendering.state===`AVAILABLE`?56:57,a)}},dependencies:[ug,wo,En],styles:[`dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.75rem}dl[_ngcontent-%COMP%] > div[_ngcontent-%COMP%]{display:grid;gap:.25rem}dt[_ngcontent-%COMP%], .note[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}pre[_ngcontent-%COMP%]{overflow:auto;max-height:24rem;white-space:pre-wrap}@media(max-width:42rem){dl[_ngcontent-%COMP%]{grid-template-columns:1fr}}`]})};var us=()=>[];var Qc=(t,n)=>n.id;var Ob=(t,n)=>n.name;var IR=(t,n)=>n.resultId;function kR(t,n){if(t&1){let e=q();l(0,`button`,8),C(`click`,function(){let a=N(e).$implicit,o=_();return A(o.preferences.isHidden(a.id)?o.preferences.show(a.id):o.preferences.hide(a.id))}),u(1),d()}if(t&2){let e=n.$implicit,i=_();m(),Ae(` `,i.preferences.isHidden(e.id)?`Show`:`Hide`,` `,e.label,` `)}}function MR(t,n){if(t&1){let e=q();l(0,`mat-card`,9)(1,`mat-card-content`)(2,`strong`),u(3,`Connection problem · showing last known data`),d(),l(4,`p`),u(5),d(),l(6,`button`,34),C(`click`,function(){N(e);return A(_().store.retryNow())}),u(7,`Retry now`),d()()()}t&2&&(m(5),w(n))}function TR(t,n){if(t&1&&(l(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit;S(`value`,e.name),m(),Ae(``,e.name,` · `,e.mode===`MINIMIZE`?`Minimize`:`Maximize`)}}function NR(t,n){t&1&&(u(0),U(1,`date`)),t&2&&j(` Last refreshed `,ke(1,1,n,`mediumTime`),`. `)}function AR(t,n){if(t&1&&(l(0,`p`,24),u(1),d()),t&2){let e=_();m(),j(` Approximate time remaining: `,e.elapsedLabel(n),` · based on observed throughput; different instance sizes can change this estimate. `)}}function OR(t,n){if(t&1){let e=q();l(0,`button`,35),C(`click`,function(){N(e);return A(_().store.selectTab(2))}),u(1),d()}if(t&2){let e=_();m(),j(` `,e.store.unseenIssues(),` unseen issues `)}}function RR(t,n){if(t&1&&(l(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit;S(`value`,e.name),m(),w(e.name)}}function PR(t,n){if(t&1){let e=q();l(0,`tr`)(1,`td`)(2,`button`,35),C(`click`,function(){let a=N(e).$implicit;return A(_(2).store.selectInstance(a.id))}),u(3),d()(),l(4,`td`),u(5),d(),l(6,`td`),u(7),d(),l(8,`td`),u(9),d(),l(10,`td`),u(11),d()()}if(t&2){let e=n.$implicit,i=_(2);Y(`selected`,e.id===i.store.selectedInstanceId()),m(3),j(` `,e.instanceName,` `),m(2),w(e.experimentName),m(2),Ae(` `,e.counts.succeeded+e.counts.failed,` / `,e.counts.planned,` `),m(2),w(e.counts.running),m(2),Ae(` `,e.counts.failed,` failed · `,e.finalized?`finalized`:`processing`,` `)}}function FR(t,n){t&1&&(l(0,`tr`)(1,`td`,46),u(2,`No matching instances yet.`),d()())}function LR(t,n){if(t&1&&(l(0,`p`,24),u(1),d()),t&2){let e=_();m(),Ae(` Charts show `,e.results.length,` sampled points from `,e.resultCount,` repetitions. Statistics are exact; full data is available in Results and exports. `)}}function VR(t,n){if(t&1&&(u(0),U(1,`number`)),t&2){let e=_().$implicit;j(` (`,ke(1,1,e.objective.percentageGap,`1.0-2`),`%) `)}}function BR(t,n){if(t&1){let e=q();l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),d(),l(7,`td`),u(8),U(9,`number`),d(),l(10,`td`),u(11),U(12,`number`),d(),l(13,`td`),u(14),U(15,`number`),U(16,`number`),d(),l(17,`td`),u(18),U(19,`number`),D(20,VR,2,4),d(),l(21,`td`),u(22),U(23,`number`),d(),l(24,`td`),u(25),U(26,`number`),d(),l(27,`td`)(28,`button`,49),C(`click`,function(){let a=N(e).$implicit;return A(_(3).store.inspectResult(a.bestResultId))}),u(29,` Best result `),d()()()}if(t&2){let e=n.$implicit;m(2),w(e.name),m(2),Ae(` `,e.counts.succeeded,` / `,e.counts.succeeded+e.counts.failed,` `),m(2),w(e.objective.count),m(2),w(ke(9,13,e.objective.best,`1.0-4`)??`—`),m(3),w(ke(12,16,e.objective.median,`1.0-4`)??`—`),m(3),Ae(` `,ke(15,19,e.objective.mean,`1.0-4`)??`—`,` ± `,ke(16,22,e.objective.standardDeviation,`1.0-4`)??`—`,` `),m(4),j(` `,ke(19,25,e.objective.absoluteGap,`1.0-4`)??`—`,` `),m(2),I(e.objective.percentageGap!==null?20:-1),m(2),j(``,ke(23,28,e.runtimeMillis.median,`1.0-2`)??`—`,` ms`),m(3),j(``,ke(26,31,e.timeToBestMillis.median,`1.0-2`)??`—`,` ms`),m(3),S(`disabled`,!e.bestResultId)}}function jR(t,n){if(t&1){let e=q();l(0,`mat-checkbox`,56),C(`change`,function(a){N(e);return A(_(4).store.setReferenceVisible(a.checked))}),u(1),d(),l(2,`p`,24),u(3),d()}if(t&2){let e=_(),i=_(),a=_(2);S(`checked`,a.store.showReference()),m(),Ae(` Show reference (`,i.objective,`: `,e.referenceValue,`) `),m(2),j(` The red dashed line marks the reference in all score charts. `,a.store.status()?.objectives?.[i.objective]===`MAXIMIZE`?`Higher`:`Lower`,` scores are better. `)}}function zR(t,n){if(t&1&&(l(0,`p`,24),u(1),d()),t&2){let e=_(2);m(),j(` No reference value is available for `,e.objective,`. `)}}function HR(t,n){if(t&1&&H(0,`app-dashboard-chart`,52),t&2)S(`model`,_().scoreChart)}function UR(t,n){if(t&1&&H(0,`app-dashboard-chart`,52),t&2)S(`model`,_().convergenceChart)}function $R(t,n){t&1&&H(0,`app-dashboard-chart`,52),t&2&&S(`model`,n)}function GR(t,n){t&1&&H(0,`app-dashboard-chart`,52),t&2&&S(`model`,n)}function WR(t,n){if(t&1&&(D(0,jR,4,4)(1,zR,2,1,`p`,24),l(2,`div`,50)(3,`app-monitor-panel`,51),D(4,HR,1,1,`app-dashboard-chart`,52),d(),l(5,`app-monitor-panel`,53),D(6,UR,1,1,`app-dashboard-chart`,52),l(7,`p`,24),u(8,` Independent repetitions, not the optimization trajectory within a single run. Failures are gaps. `),d()(),l(9,`app-monitor-panel`,54),D(10,$R,1,1,`app-dashboard-chart`,52),d(),l(11,`app-monitor-panel`,55),D(12,GR,1,1,`app-dashboard-chart`,52),d()()),t&2){let e,i,a=_(3);I(n.referenceValue!==null?0:1),m(4),I(!a.preferences.isHidden(`normal-scores`)&&!a.preferences.isCollapsed(`normal-scores`)?4:-1),m(2),I(!a.preferences.isHidden(`normal-best`)&&!a.preferences.isCollapsed(`normal-best`)?6:-1),m(4),I((e=!a.preferences.isHidden(`normal-distribution`)&&!a.preferences.isCollapsed(`normal-distribution`)&&a.store.distribution())?10:-1,e),m(2),I((i=!a.preferences.isHidden(`normal-runtime`)&&!a.preferences.isCollapsed(`normal-runtime`)&&a.store.qualityRuntime())?12:-1,i)}}function qR(t,n){if(t&1&&(l(0,`h2`,47),u(1),l(2,`small`),u(3),d()(),D(4,LR,2,2,`p`,24),l(5,`app-monitor-panel`,48)(6,`div`,41)(7,`table`)(8,`caption`,42),u(9),d(),l(10,`thead`)(11,`tr`)(12,`th`),u(13,`Algorithm`),d(),l(14,`th`),u(15,`Successful / attempted`),d(),l(16,`th`),u(17,`Valid scores`),d(),l(18,`th`),u(19,`Best`),d(),l(20,`th`),u(21,`Median`),d(),l(22,`th`),u(23,`Mean ± SD`),d(),l(24,`th`),u(25,`Gap to reference`),d(),l(26,`th`),u(27,`Runtime median`),d(),l(28,`th`),u(29,`Time-to-best median`),d(),l(30,`th`),u(31,`Details`),d()()(),l(32,`tbody`),ue(33,BR,30,34,`tr`,null,Ob),d()()(),l(35,`p`,24),u(36,` Statistics use successful results with finite values. Scores from different instances are not averaged. Negative gaps improve on the reference. `),d()(),D(37,WR,13,5)),t&2){let e,i=n,a=_(2);m(),j(` `,i.instance.instanceName,` `),m(2),w(i.instance.experimentName),m(),I(i.sampled?4:-1),m(5),j(` `,i.objective,` statistics by algorithm `),m(24),me(i.algorithms),m(4),I((e=a.store.charts())?37:-1,e)}}function YR(t,n){t&1&&(l(0,`p`,45),u(1,`Select an instance to inspect its results and charts.`),d())}function KR(t,n){if(t&1){let e=q();l(0,`mat-card`,36)(1,`mat-card-content`)(2,`h2`),u(3,`Instances`),d(),l(4,`div`,37)(5,`mat-form-field`,38)(6,`mat-label`),u(7,`Experiment`),d(),l(8,`mat-select`,15),C(`selectionChange`,function(a){N(e);let o=_();return A(o.store.filterInstances(a.value,o.store.instanceQuery().search))}),l(9,`mat-option`,39),u(10,`All experiments`),d(),ue(11,RR,2,2,`mat-option`,16,Ob),d()(),l(13,`mat-form-field`,38)(14,`mat-label`),u(15,`Search instances`),d(),l(16,`input`,40),C(`input`,function(a){N(e);let o=_();return A(o.store.filterInstances(o.store.instanceQuery().experiment,a.target.value))}),d()()(),l(17,`div`,41)(18,`table`)(19,`caption`,42),u(20,` Instances across the execution `),d(),l(21,`thead`)(22,`tr`)(23,`th`),u(24,`Instance`),d(),l(25,`th`),u(26,`Experiment`),d(),l(27,`th`),u(28,`Completed repetitions`),d(),l(29,`th`),u(30,`Running`),d(),l(31,`th`),u(32,`Outcome`),d()()(),l(33,`tbody`),ue(34,PR,12,9,`tr`,43,Qc,!1,FR,3,0,`tr`),d()()(),l(37,`mat-paginator`,44),C(`page`,function(a){N(e);return A(_().store.pageInstances(a.pageIndex*a.pageSize))}),d()()(),D(38,qR,38,5)(39,YR,2,0,`p`,45)}if(t&2){let e,i=_();m(8),S(`value`,i.store.instanceQuery().experiment),m(3),me(i.store.status()?.experiments??rt(7,us)),m(5),S(`value`,i.store.instanceQuery().search),m(18),me(i.store.instancePage()?.items??rt(8,us)),m(3),S(`length`,i.store.instancePage()?.total??0)(`pageSize`,25)(`pageIndex`,i.store.instanceQuery().offset/25),m(),I((e=i.store.detail())?38:39,e)}}function QR(t,n){if(t&1&&(l(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit,i=_(2);S(`value`,e),m(),w(e?i.phaseLabel(e):`All outcomes`)}}function XR(t,n){if(t&1&&(l(0,`a`,63),u(1,`CSV`),d(),l(2,`a`,63),u(3,`JSON`),d()),t&2){let e=_(2);S(`href`,e.store.exportUrl(`csv`),hn),m(2),S(`href`,e.store.exportUrl(`json`),hn)}}function ZR(t,n){if(t&1){let e=q();l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),d(),l(7,`td`),u(8),d(),l(9,`td`),u(10),d(),l(11,`td`),u(12),U(13,`number`),d(),l(14,`td`),u(15),U(16,`number`),d(),l(17,`td`)(18,`button`,35),C(`click`,function(){let a=N(e).$implicit;return A(_(2).store.inspectResult(a.resultId))}),u(19,` Inspect `),d()()()}if(t&2){let e=n.$implicit,i=_(2);m(2),Ae(``,e.experimentName,` / `,e.instanceName),m(2),w(e.algorithmName),m(2),w(e.iteration),m(2),w(e.seed),m(2),w(i.phaseLabel(e.state)),m(2),j(` `,ke(13,8,e.objectives[i.store.selectedObjective()??``],`1.0-4`)??`—`,` `),m(3),j(` `,e.executionTime===null?`—`:ke(16,11,e.executionTime/1e6,`1.0-2`)+` ms`,` `)}}function JR(t,n){t&1&&(l(0,`tr`)(1,`td`,64),u(2,`No matching repetitions.`),d()())}function eP(t,n){if(t&1){let e=q();l(0,`mat-card`,36)(1,`mat-card-content`)(2,`div`,37)(3,`mat-form-field`,38)(4,`mat-label`),u(5,`Search experiment, instance or algorithm`),d(),l(6,`input`,40),C(`input`,function(a){N(e);let o=_();return A(o.store.filterResults(o.store.resultQuery().instanceId,o.store.resultQuery().algorithm,o.store.resultQuery().state,a.target.value))}),d()(),l(7,`mat-form-field`,38)(8,`mat-label`),u(9,`Outcome`),d(),l(10,`mat-select`,15),C(`selectionChange`,function(a){N(e);let o=_();return A(o.store.filterResults(o.store.resultQuery().instanceId,o.store.resultQuery().algorithm,a.value,o.store.resultQuery().search))}),ue(11,QR,2,2,`mat-option`,16,Pn),d()(),l(13,`mat-form-field`,38)(14,`mat-label`),u(15,`Algorithm name`),d(),l(16,`input`,40),C(`input`,function(a){N(e);let o=_();return A(o.store.filterResults(o.store.resultQuery().instanceId,a.target.value,o.store.resultQuery().state,o.store.resultQuery().search))}),d()(),l(17,`mat-form-field`,38)(18,`mat-label`),u(19,`Sort results`),d(),l(20,`mat-select`,15),C(`selectionChange`,function(a){N(e);return A(_().store.sortResults(a.value))}),l(21,`mat-option`,57),u(22,`Planned order`),d(),l(23,`mat-option`,58),u(24,`Instance name`),d(),l(25,`mat-option`,59),u(26,`Algorithm name`),d(),l(27,`mat-option`,60),u(28,`Outcome`),d(),l(29,`mat-option`,61),u(30,`Runtime · fastest first`),d()()(),l(31,`button`,35),C(`click`,function(){N(e);let a=_();return A(a.store.filterResults(a.store.selectedInstanceId()??``,a.store.resultQuery().algorithm,a.store.resultQuery().state,a.store.resultQuery().search))}),u(32,` Selected instance only `),d(),l(33,`button`,35),C(`click`,function(){N(e);return A(_().store.filterResults(``,``,``,``))}),u(34,` Clear filters `),d(),D(35,XR,4,2),d(),l(36,`div`,41)(37,`table`)(38,`caption`,42),u(39,` Repetition result ledger `),d(),l(40,`thead`)(41,`tr`)(42,`th`),u(43,`Experiment / instance`),d(),l(44,`th`),u(45,`Algorithm`),d(),l(46,`th`),u(47,`Iteration`),d(),l(48,`th`),u(49,`Seed`),d(),l(50,`th`),u(51,`Outcome`),d(),l(52,`th`),u(53),d(),l(54,`th`),u(55,`Runtime`),d(),l(56,`th`),u(57,`Details`),d()()(),l(58,`tbody`),ue(59,ZR,20,14,`tr`,null,IR,!1,JR,3,0,`tr`),d()()(),l(62,`mat-paginator`,62),C(`page`,function(a){N(e);return A(_().store.pageResults(a.pageIndex*a.pageSize))}),d()()()}if(t&2){let e=_();m(6),S(`value`,e.store.resultQuery().search),m(4),S(`value`,e.store.resultQuery().state),m(),me(e.workStates),m(5),S(`value`,e.store.resultQuery().algorithm),m(4),S(`value`,e.store.resultQuery().order??`PLANNED`),m(15),I(e.store.status()?.runId?35:-1),m(18),w(e.store.selectedObjective()??`Objective`),m(6),me(e.store.resultPage()?.items??rt(10,us)),m(3),S(`length`,e.store.resultPage()?.total??0)(`pageSize`,25)(`pageIndex`,e.store.resultQuery().offset/25)}}function tP(t,n){if(t&1&&u(0),t&2){let e=_().$implicit;Ae(` · iteration `,e.iteration,` · seed `,e.seed,` `)}}function nP(t,n){if(t&1){let e=q();l(0,`button`,35),C(`click`,function(){N(e);let a=_().$implicit;return A(_(2).store.inspectResult(a.resultId))}),u(1,` Inspect result `),d()}}function iP(t,n){if(t&1&&(l(0,`mat-card`,68)(1,`mat-card-content`)(2,`strong`),u(3),d(),l(4,`p`),u(5),d(),l(6,`p`),u(7),D(8,tP,1,2),u(9),U(10,`date`),d(),D(11,nP,2,0,`button`,25),d()()),t&2){let e=n.$implicit;m(3),Ot(``,e.severity,` · `,e.scope,` · `,e.exceptionType),m(2),w(e.message??`No details provided`),m(2),Ot(` `,e.experimentName,` / `,e.instanceName,` / `,e.algorithmName,` `),m(),I(e.iteration!==null?8:-1),m(),j(` · `,ke(10,10,e.timestamp,`medium`),` `),m(2),I(e.resultId?11:-1)}}function aP(t,n){t&1&&(l(0,`p`,45),u(1,`No experiment issues have been reported.`),d())}function oP(t,n){if(t&1){let e=q();l(0,`div`,37)(1,`mat-form-field`,38)(2,`mat-label`),u(3,`Severity`),d(),l(4,`mat-select`,15),C(`selectionChange`,function(a){N(e);return A(_().store.filterIssues(a.value))}),l(5,`mat-option`,39),u(6,`All severities`),d(),l(7,`mat-option`,65),u(8,`Fatal`),d(),l(9,`mat-option`,66),u(10,`Error`),d(),l(11,`mat-option`,67),u(12,`Warning`),d()()(),l(13,`button`,35),C(`click`,function(){N(e);return A(_().store.markIssuesSeen())}),u(14,`Mark issues seen`),d()(),ue(15,iP,12,13,`mat-card`,68,Qc,!1,aP,2,0,`p`,45),l(18,`mat-paginator`,69),C(`page`,function(a){N(e);return A(_().store.pageIssues(a.pageIndex*a.pageSize))}),d()}if(t&2){let e=_();m(4),S(`value`,e.store.issueSeverity()),m(11),me(e.store.issuePage()?.items??rt(5,us)),m(3),S(`length`,e.store.issuePage()?.total??0)(`pageSize`,25)(`pageIndex`,e.store.issueOffset()/25)}}function rP(t,n){if(t&1&&(l(0,`a`,63),u(1,`Download results CSV`),d(),l(2,`a`,63),u(3,`Download results JSON`),d()),t&2){let e=_(2);S(`href`,e.store.exportUrl(`csv`),hn),m(2),S(`href`,e.store.exportUrl(`json`),hn)}}function sP(t,n){if(t&1&&(l(0,`tr`)(1,`td`)(2,`a`,70),u(3),d()(),l(4,`td`),u(5),d(),l(6,`td`),u(7),U(8,`number`),d(),l(9,`td`),u(10),U(11,`date`),d()()),t&2){let e=n.$implicit,i=_(2);m(2),S(`href`,i.store.artifactUrl(e.id),hn),m(),w(e.filename),m(2),w(e.experimentName),m(2),w(ye(8,5,e.size)),m(3),w(ke(11,7,e.modifiedAt,`medium`))}}function cP(t,n){t&1&&(l(0,`tr`)(1,`td`,71),u(2,`No exported artifacts are available yet.`),d()())}function lP(t,n){if(t&1&&(l(0,`p`),u(1,` Only successfully exported, registered files are available. Result exports use the current Results filters. `),d(),D(2,rP,4,2),l(3,`div`,41)(4,`table`)(5,`thead`)(6,`tr`)(7,`th`),u(8,`File`),d(),l(9,`th`),u(10,`Experiment`),d(),l(11,`th`),u(12,`Bytes`),d(),l(13,`th`),u(14,`Updated`),d()()(),l(15,`tbody`),ue(16,sP,12,10,`tr`,null,Qc,!1,cP,3,0,`tr`),d()()()),t&2){let e=_();m(2),I(e.store.status()?.runId?2:-1),m(14),me(e.store.artifacts()?.artifacts??rt(2,us))}}function dP(t,n){t&1&&(l(0,`p`),u(1),d()),t&2&&(m(),w(n))}function uP(t,n){if(t&1){let e=q();l(0,`mat-card`,72)(1,`mat-card-content`)(2,`h2`),u(3,`Transport diagnostics`),d(),l(4,`p`),u(5),d(),l(6,`p`),u(7),d(),l(8,`p`),u(9),d(),l(10,`p`),u(11),d(),D(12,dP,2,1,`p`),l(13,`p`),u(14,` Summaries and results come from backend snapshots. Connection health is independent of execution outcome. `),d(),l(15,`button`,35),C(`click`,function(){N(e);return A(_().store.retryNow())}),u(16,`Reconnect / refresh`),d()()()}if(t&2){let e,i=_();m(5),j(`Run: `,i.store.status()?.runId??`No execution`),m(2),j(`Revision: `,i.store.status()?.revision??0),m(2),j(`WebSocket: `,i.store.transportState()),m(2),j(`Last processed notification ID: `,i.store.lastEventId()),m(),I((e=i.store.transportError())?12:-1,e)}}function mP(t,n){if(t&1){let e=q();l(0,`mat-card`,9)(1,`mat-card-content`),u(2),l(3,`button`,35),C(`click`,function(){N(e);return A(_().store.closeResult())}),u(4,`Close`),d()()()}t&2&&(m(2),w(n))}function pP(t,n){if(t&1&&(l(0,`a`,63),u(1),d()),t&2){let e=_().$implicit;S(`href`,_(2).store.artifactUrl(e.id),hn),m(),w(e.filename)}}function hP(t,n){if(t&1&&D(0,pP,2,2,`a`,63),t&2){let e=n.$implicit,i=_();I(e.resultId===i.result.resultId?0:-1)}}function fP(t,n){if(t&1){let e=q();l(0,`mat-card`,33)(1,`mat-card-content`)(2,`div`,73)(3,`h2`),u(4,`Result details`),d(),l(5,`button`,35),C(`click`,function(){N(e);return A(_().store.closeResult())}),u(6,`Close`),d()(),H(7,`app-solution-renderer`,74),ue(8,hP,1,1,null,null,Qc),d()()}if(t&2){let e=_();m(7),S(`detail`,n)(`selectedObjective`,e.store.selectedObjective()),m(),me(e.store.artifacts()?.artifacts??rt(2,us))}}var ym=class t{store=f(Kc);preferences=f(Va);panels=Ab;workStates=[``,`PENDING`,`RUNNING`,`SUCCEEDED`,`FAILED`,`CANCELLED`];ngOnInit(){this.store.start()}progressValue(n,e){return e>0?Math.min(100,n/e*100):0}elapsedLabel(n){let e=Math.floor(n/1e3);return e>=3600?Math.floor(e/3600)+`h `+Math.floor(e%3600/60)+`m`:Math.floor(e/60)+`m `+e%60+`s`}phaseLabel(n){return n.toLowerCase().replaceAll(`_`,` `)}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-standard-dashboard`]],features:[Ge([Kc,cs,Fa])],decls:113,vars:31,consts:[[`panelMenu`,`matMenu`],[1,`topbar`],[`src`,`favicon.svg`,`alt`,`Mork`,1,`logo`],[1,`brand`],[1,`toolbar-spacer`],[`aria-label`,`Connection state`],[`mat-button`,``,`aria-label`,`Choose dashboard panels`,3,`matMenuTriggerFor`],[`mat-menu-item`,``],[`mat-menu-item`,``,3,`click`],[`role`,`alert`,1,`error-card`],[`appearance`,`outlined`,1,`status-card`],[`aria-label`,`Execution summary`,1,`summary`],[1,`eyebrow`],[1,`phase`],[`appearance`,`outline`,`subscriptSizing`,`dynamic`],[3,`selectionChange`,`value`],[3,`value`],[`aria-label`,`Repetition outcomes`,1,`metrics`],[`aria-label`,`Execution progress`,1,`progress-grid`],[1,`progress-item`],[1,`progress-label`],[`mode`,`determinate`,`aria-label`,`Finalized instances in execution`,3,`value`],[`mode`,`determinate`,`aria-label`,`Finalized algorithms in selected instance`,3,`value`],[`mode`,`determinate`,`aria-label`,`Completed repetitions in selection`,3,`value`],[1,`status-footnote`],[`mat-button`,``],[`animationDuration`,`0ms`,3,`selectedIndexChange`,`selectedIndex`],[`label`,`Overview`],[`matTabContent`,``],[`label`,`Results`],[`label`,`Issues`],[`label`,`Artifacts`],[`label`,`Diagnostics`],[`appearance`,`outlined`,`aria-label`,`Result details`,1,`result-detail`],[`mat-flat-button`,``,3,`click`],[`mat-button`,``,3,`click`],[`appearance`,`outlined`,1,`browser`],[1,`filters`],[`appearance`,`outline`],[`value`,``],[`matInput`,``,3,`input`,`value`],[1,`table-scroll`],[1,`sr-only`],[3,`selected`],[`aria-label`,`Instance pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[1,`empty-state`],[`colspan`,`5`],[1,`detail-title`],[`panelId`,`normal-comparison`,`title`,`Algorithm comparison`],[`mat-button`,``,3,`click`,`disabled`],[1,`chart-grid`],[`panelId`,`normal-scores`,`title`,`Scores`],[3,`model`],[`panelId`,`normal-best`,`title`,`Best score across repetitions`],[`panelId`,`normal-distribution`,`title`,`Score distribution`],[`panelId`,`normal-runtime`,`title`,`Score versus runtime`],[1,`reference-toggle`,3,`change`,`checked`],[`value`,`PLANNED`],[`value`,`INSTANCE`],[`value`,`ALGORITHM`],[`value`,`STATE`],[`value`,`RUNTIME`],[`aria-label`,`Result pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[`mat-button`,``,3,`href`],[`colspan`,`8`],[`value`,`FATAL`],[`value`,`ERROR`],[`value`,`WARNING`],[`appearance`,`outlined`,1,`issue`],[`aria-label`,`Issue pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[3,`href`],[`colspan`,`4`],[1,`browser`],[1,`result-heading`],[3,`detail`,`selectedObjective`]],template:function(e,i){if(e&1&&(l(0,`mat-toolbar`,1),H(1,`img`,2),l(2,`span`,3),u(3,`Experiment monitor`),d(),H(4,`span`,4),l(5,`mat-chip-set`,5)(6,`mat-chip`),u(7),d()(),l(8,`button`,6),u(9,` Panels `),d(),l(10,`mat-menu`,null,0),ue(12,kR,2,2,`button`,7,Qc),l(14,`button`,8),C(`click`,function(){return i.preferences.reset()}),u(15,`Reset panels`),d()()(),l(16,`main`),D(17,MR,8,1,`mat-card`,9),l(18,`mat-card`,10)(19,`mat-card-content`)(20,`section`,11)(21,`div`)(22,`span`,12),u(23,`Execution`),d(),l(24,`strong`,13),u(25),d()(),l(26,`div`)(27,`span`,12),u(28,`Experiment`),d(),l(29,`strong`),u(30),d()(),l(31,`div`)(32,`span`,12),u(33,`Elapsed`),d(),l(34,`strong`),u(35),d()(),l(36,`mat-form-field`,14)(37,`mat-label`),u(38,`Objective`),d(),l(39,`mat-select`,15),C(`selectionChange`,function(o){return i.store.selectObjective(o.value)}),ue(40,TR,2,3,`mat-option`,16,Ob),d()()(),l(42,`section`,17)(43,`div`)(44,`span`),u(45,`Planned`),d(),l(46,`strong`),u(47),d()(),l(48,`div`)(49,`span`),u(50,`Pending`),d(),l(51,`strong`),u(52),d()(),l(53,`div`)(54,`span`),u(55,`Running`),d(),l(56,`strong`),u(57),d()(),l(58,`div`)(59,`span`),u(60,`Successful`),d(),l(61,`strong`),u(62),d()(),l(63,`div`)(64,`span`),u(65,`Failed`),d(),l(66,`strong`),u(67),d()(),l(68,`div`)(69,`span`),u(70,`Cancelled`),d(),l(71,`strong`),u(72),d()()(),l(73,`section`,18)(74,`div`,19)(75,`div`,20)(76,`span`),u(77,`Instances · execution`),d(),l(78,`span`),u(79),d()(),H(80,`mat-progress-bar`,21),d(),l(81,`div`,19)(82,`div`,20)(83,`span`),u(84,`Algorithms · selected instance`),d(),l(85,`span`),u(86),d()(),H(87,`mat-progress-bar`,22),d(),l(88,`div`,19)(89,`div`,20)(90,`span`),u(91),d(),l(92,`span`),u(93),d()(),H(94,`mat-progress-bar`,23),d()(),l(95,`p`,24),u(96),D(97,NR,2,4),d(),D(98,AR,2,1,`p`,24),D(99,OR,2,1,`button`,25),d()(),l(100,`mat-tab-group`,26),C(`selectedIndexChange`,function(o){return i.store.selectTab(o)}),l(101,`mat-tab`,27),ot(102,KR,40,9,`ng-template`,28),d(),l(103,`mat-tab`,29),ot(104,eP,63,11,`ng-template`,28),d(),l(105,`mat-tab`,30),ot(106,oP,19,6,`ng-template`,28),d(),l(107,`mat-tab`,31),ot(108,lP,19,3,`ng-template`,28),d(),l(109,`mat-tab`,32),ot(110,uP,17,5,`ng-template`,28),d()(),D(111,mP,5,1,`mat-card`,9),D(112,fP,10,3,`mat-card`,33),d()),e&2){let a,o,r,s,c,h=ft(11);m(7),w(i.store.connectionLabel()),m(),S(`matMenuTriggerFor`,h),m(4),me(i.panels),m(5),I((a=i.store.latestError())?17:-1,a);let p=i.store.status();m(8),w(i.phaseLabel(p?.phase??`IDLE`)),m(5),w(p?.currentExperiment??`Waiting for execution`),m(5),w(i.elapsedLabel(p?.elapsedMillis??0)),m(4),S(`value`,i.store.selectedObjective()),m(),me(i.store.objectives()),m(7),w(p?.counts?.planned??0),m(5),w(p?.counts?.pending??0),m(5),w(p?.counts?.running??0),m(5),w(p?.counts?.succeeded??0),m(5),w(p?.counts?.failed??0),m(5),w(p?.counts?.cancelled??0);let g=i.store.progress();m(7),Ae(``,g.instances.completed,` / `,g.instances.total),m(),S(`value`,i.progressValue(g.instances.completed,g.instances.total)),m(6),Ae(``,g.algorithms.completed,` / `,g.algorithms.total),m(),S(`value`,i.progressValue(g.algorithms.completed,g.algorithms.total)),m(4),j(`Repetitions · `,g.repetitionScope),m(2),Ae(``,g.repetitions.completed,` / `,g.repetitions.total),m(),S(`value`,i.progressValue(g.repetitions.completed,g.repetitions.total)),m(2),Ae(` `,p?.runningInstances??0,` active instances · `,p?.runningAlgorithms??0,` active algorithms. Completed repetitions include failures; cancelled work does not count as completed. `),m(),I((o=i.store.lastUpdated())?97:-1,o),m(),I((r=i.store.estimatedRemainingMillis())?98:-1,r),m(),I(i.store.unseenIssues()>0?99:-1),m(),S(`selectedIndex`,i.store.tab()),m(11),I((s=i.store.resultError())?111:-1,s),m(),I((c=i.store.inspectedResult())?112:-1,c)}},dependencies:[Ai,ds,Pt,en,zn,Bn,jn,em,Bc,Ju,tm,jc,di,Ti,na,Xr,Qr,os,Ni,ca,as,mm,Wc,ss,rs,Pa,Ra,Un,hm,qc,Yc,pm,gm,fm,_m,Xi,En],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh;color:var(--%NS%mat-sys-on-surface);background:var(--%NS%mat-sys-surface-container-lowest)}.topbar[_ngcontent-%COMP%]{position:sticky;top:0;z-index:10;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:var(--%NS%mat-sys-surface);gap:.75rem}.logo[_ngcontent-%COMP%]{width:2.2rem;height:2.2rem}.brand[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-large);font-weight:700}.toolbar-spacer[_ngcontent-%COMP%]{flex:1}main[_ngcontent-%COMP%]{width:min(100% - 2rem,96rem);margin:auto;padding:1.5rem 0 4rem}.status-card[_ngcontent-%COMP%], .browser[_ngcontent-%COMP%], .issue[_ngcontent-%COMP%], .result-detail[_ngcontent-%COMP%]{margin:1rem 0}.summary[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(3,minmax(0,1fr)) minmax(13rem,1fr);gap:1rem;align-items:center}.summary[_ngcontent-%COMP%] > div[_ngcontent-%COMP%], .metrics[_ngcontent-%COMP%] > div[_ngcontent-%COMP%]{display:grid;gap:.3rem}.phase[_ngcontent-%COMP%]{text-transform:capitalize}.eyebrow[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-label-medium);text-transform:uppercase;color:var(--%NS%mat-sys-on-surface-variant);letter-spacing:.05em}.metrics[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(6,1fr);gap:1rem;padding:1.2rem 0}.metrics[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-headline-small)}.progress-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:1.5rem;border-top:1px solid var(--%NS%mat-sys-outline-variant);padding-top:1rem}.progress-item[_ngcontent-%COMP%]{display:grid;gap:.5rem}.progress-label[_ngcontent-%COMP%]{display:flex;justify-content:space-between;gap:.5rem;font:var(--%NS%mat-sys-label-large)}.status-footnote[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.85rem;line-height:1.5}.filters[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem;align-items:center;margin:1rem 0}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{flex:1;min-width:12rem}.table-scroll[_ngcontent-%COMP%]{overflow-x:auto}table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse;font:var(--%NS%mat-sys-body-medium)}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{text-align:left;padding:.65rem .75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);white-space:nowrap}th[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-large)}tr.selected[_ngcontent-%COMP%]{background:var(--%NS%mat-sys-secondary-container)}.chart-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem;margin-top:1rem}.detail-title[_ngcontent-%COMP%]{margin:1.5rem 0 1rem}small[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.85rem}.error-card[_ngcontent-%COMP%]{background:var(--%NS%mat-sys-error-container);color:var(--%NS%mat-sys-on-error-container);margin:1rem 0}.result-heading[_ngcontent-%COMP%]{display:flex;justify-content:space-between;align-items:center}.empty-state[_ngcontent-%COMP%]{min-height:8rem;display:grid;place-items:center;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}.sr-only[_ngcontent-%COMP%]{position:absolute;width:1px;height:1px;padding:0;overflow:hidden;clip:rect(0,0,0,0)}@media(max-width:60rem){.summary[_ngcontent-%COMP%]{grid-template-columns:repeat(2,minmax(0,1fr))}.metrics[_ngcontent-%COMP%]{grid-template-columns:repeat(3,1fr)}.progress-grid[_ngcontent-%COMP%]{grid-template-columns:1fr}}@media(max-width:42rem){main[_ngcontent-%COMP%]{width:calc(100% - 1rem);padding-top:.25rem}.chart-grid[_ngcontent-%COMP%], .summary[_ngcontent-%COMP%]{grid-template-columns:1fr}.brand[_ngcontent-%COMP%]{display:none}.topbar[_ngcontent-%COMP%]{gap:.25rem;padding:0 .5rem}.logo[_ngcontent-%COMP%]{width:1.5rem}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{min-width:100%}}`]})};var gP=new k(`MAT_BADGE_CONFIG`);var zw=`mat-badge-content`;var bP=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],decls:0,vars:0,template:function(i,a){},styles:[`.mat-badge {
  position: relative;
}
.mat-badge.mat-badge {
  overflow: visible;
}

.mat-badge-content {
  position: absolute;
  text-align: center;
  display: inline-block;
  transition: transform 200ms ease-in-out;
  transform: scale(0.6);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  box-sizing: border-box;
  pointer-events: none;
  background-color: var(--%NS%mat-badge-background-color, var(--%NS%mat-sys-error));
  color: var(--%NS%mat-badge-text-color, var(--%NS%mat-sys-on-error));
  font-family: var(--%NS%mat-badge-text-font, var(--%NS%mat-sys-label-small-font));
  font-weight: var(--%NS%mat-badge-text-weight, var(--%NS%mat-sys-label-small-weight));
  border-radius: var(--%NS%mat-badge-container-shape, var(--%NS%mat-sys-corner-full));
}
.mat-badge-above .mat-badge-content {
  bottom: 100%;
}
.mat-badge-below .mat-badge-content {
  top: 100%;
}
.mat-badge-before .mat-badge-content {
  right: 100%;
}
[dir=rtl] .mat-badge-before .mat-badge-content {
  right: auto;
  left: 100%;
}
.mat-badge-after .mat-badge-content {
  left: 100%;
}
[dir=rtl] .mat-badge-after .mat-badge-content {
  left: auto;
  right: 100%;
}
@media (forced-colors: active) {
  .mat-badge-content {
    outline: solid 1px;
    border-radius: 0;
  }
}

.mat-badge-disabled .mat-badge-content {
  background-color: var(--%NS%mat-badge-disabled-state-background-color, color-mix(in srgb, var(--%NS%mat-sys-error) 38%, transparent));
  color: var(--%NS%mat-badge-disabled-state-text-color, var(--%NS%mat-sys-on-error));
}

.mat-badge-hidden .mat-badge-content {
  display: none;
}

.ng-animate-disabled .mat-badge-content,
.mat-badge-content._mat-animation-noopable {
  transition: none;
}

.mat-badge-content.mat-badge-active {
  transform: none;
}

.mat-badge-small .mat-badge-content {
  width: var(--%NS%mat-badge-legacy-small-size-container-size, unset);
  height: var(--%NS%mat-badge-legacy-small-size-container-size, unset);
  min-width: var(--%NS%mat-badge-small-size-container-size, 6px);
  min-height: var(--%NS%mat-badge-small-size-container-size, 6px);
  line-height: var(--%NS%mat-badge-small-size-line-height, 6px);
  padding: var(--%NS%mat-badge-small-size-container-padding, 0);
  font-size: var(--%NS%mat-badge-small-size-text-size, 0);
  margin: var(--%NS%mat-badge-small-size-container-offset, -6px 0);
}
.mat-badge-small.mat-badge-overlap .mat-badge-content {
  margin: var(--%NS%mat-badge-small-size-container-overlap-offset, -6px);
}

.mat-badge-medium .mat-badge-content {
  width: var(--%NS%mat-badge-legacy-container-size, unset);
  height: var(--%NS%mat-badge-legacy-container-size, unset);
  min-width: var(--%NS%mat-badge-container-size, 16px);
  min-height: var(--%NS%mat-badge-container-size, 16px);
  line-height: var(--%NS%mat-badge-line-height, 16px);
  padding: var(--%NS%mat-badge-container-padding, 0 4px);
  font-size: var(--%NS%mat-badge-text-size, var(--%NS%mat-sys-label-small-size));
  margin: var(--%NS%mat-badge-container-offset, -12px 0);
}
.mat-badge-medium.mat-badge-overlap .mat-badge-content {
  margin: var(--%NS%mat-badge-container-overlap-offset, -12px);
}

.mat-badge-large .mat-badge-content {
  width: var(--%NS%mat-badge-legacy-large-size-container-size, unset);
  height: var(--%NS%mat-badge-legacy-large-size-container-size, unset);
  min-width: var(--%NS%mat-badge-large-size-container-size, 16px);
  min-height: var(--%NS%mat-badge-large-size-container-size, 16px);
  line-height: var(--%NS%mat-badge-large-size-line-height, 16px);
  padding: var(--%NS%mat-badge-large-size-container-padding, 0 4px);
  font-size: var(--%NS%mat-badge-large-size-text-size, var(--%NS%mat-sys-label-small-size));
  margin: var(--%NS%mat-badge-large-size-container-offset, -12px 0);
}
.mat-badge-large.mat-badge-overlap .mat-badge-content {
  margin: var(--%NS%mat-badge-large-size-container-overlap-offset, -12px);
}
`],encapsulation:2})}return t})();var Hw=(()=>{class t{_ngZone=f(X);_elementRef=f(W);_ariaDescriber=f(Nu);_renderer=f(Re);_animationsDisabled=Ue();_idGenerator=f(Be);get color(){return this._color}set color(e){this._setColor(e),this._color=e}_color;overlap;disabled=!1;position;get content(){return this._content}set content(e){this._updateRenderedContent(e)}_content;get description(){return this._description}set description(e){this._updateDescription(e)}_description;size;hidden=!1;_badgeElement;_inlineBadgeDescription;_isInitialized=!1;_interactivityChecker=f(Lr);_document=f(de);constructor(){let e=f(gP,{optional:!0}),i=f(Je);i.load(bP),i.load(Ii),this._color=e?.color||`primary`,this.overlap=e?.overlap??!0,this.position=e?.position||`above after`,this.size=e?.size||`medium`}isAbove(){return this.position.indexOf(`below`)===-1}isAfter(){return this.position.indexOf(`before`)===-1}getBadgeElement(){return this._badgeElement}ngOnInit(){this._clearExistingBadges(),this.content&&!this._badgeElement&&(this._badgeElement=this._createBadgeElement(),this._updateRenderedContent(this.content)),this._isInitialized=!0}ngAfterViewInit(){}ngOnDestroy(){this._renderer.destroyNode&&(this._renderer.destroyNode(this._badgeElement),this._inlineBadgeDescription?.remove()),this._ariaDescriber.removeDescription(this._elementRef.nativeElement,this.description)}_isHostInteractive(){return this._interactivityChecker.isFocusable(this._elementRef.nativeElement,{ignoreVisibility:!0})}_createBadgeElement(){let e=this._renderer.createElement(`span`),i=`mat-badge-active`;return e.setAttribute(`id`,this._idGenerator.getId(`mat-badge-content-`)),e.setAttribute(`aria-hidden`,`true`),e.classList.add(zw),this._animationsDisabled&&e.classList.add(`_mat-animation-noopable`),this._elementRef.nativeElement.appendChild(e),typeof requestAnimationFrame==`function`&&!this._animationsDisabled?this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>{e.classList.add(i)})}):e.classList.add(i),e}_updateRenderedContent(e){let i=`${e??``}`.trim();this._isInitialized&&i&&!this._badgeElement&&(this._badgeElement=this._createBadgeElement()),this._badgeElement&&(this._badgeElement.textContent=i),this._content=i}_updateDescription(e){this._ariaDescriber.removeDescription(this._elementRef.nativeElement,this.description),(!e||this._isHostInteractive())&&this._removeInlineDescription(),this._description=e,this._isHostInteractive()?this._ariaDescriber.describe(this._elementRef.nativeElement,e):this._updateInlineDescription()}_updateInlineDescription(){this._inlineBadgeDescription||(this._inlineBadgeDescription=this._document.createElement(`span`),this._inlineBadgeDescription.classList.add(`cdk-visually-hidden`)),this._inlineBadgeDescription.textContent=this.description,this._badgeElement?.appendChild(this._inlineBadgeDescription)}_removeInlineDescription(){this._inlineBadgeDescription?.remove(),this._inlineBadgeDescription=void 0}_setColor(e){let i=this._elementRef.nativeElement.classList;i.remove(`mat-badge-${this._color}`),e&&i.add(`mat-badge-${e}`)}_clearExistingBadges(){let e=this._elementRef.nativeElement.querySelectorAll(`:scope > .${zw}`);for(let i of Array.from(e))i!==this._badgeElement&&i.remove()}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`matBadge`,``]],hostAttrs:[1,`mat-badge`],hostVars:20,hostBindings:function(i,a){i&2&&Y(`mat-badge-overlap`,a.overlap)(`mat-badge-above`,a.isAbove())(`mat-badge-below`,!a.isAbove())(`mat-badge-before`,!a.isAfter())(`mat-badge-after`,a.isAfter())(`mat-badge-small`,a.size===`small`)(`mat-badge-medium`,a.size===`medium`)(`mat-badge-large`,a.size===`large`)(`mat-badge-hidden`,a.hidden||!a.content)(`mat-badge-disabled`,a.disabled)},inputs:{color:[0,`matBadgeColor`,`color`],overlap:[2,`matBadgeOverlap`,`overlap`,V],disabled:[2,`matBadgeDisabled`,`disabled`,V],position:[0,`matBadgePosition`,`position`],content:[0,`matBadge`,`content`],description:[0,`matBadgeDescription`,`description`],size:[0,`matBadgeSize`,`size`],hidden:[2,`matBadgeHidden`,`hidden`,V]}})}return t})();var Uw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[Mo,we]})}return t})();var Ba=class{viewContainerRef;injector;id;role=`dialog`;panelClass=``;hasBackdrop=!0;backdropClass=``;disableClose=!1;closePredicate;width=``;height=``;minWidth;minHeight;maxWidth;maxHeight;positionStrategy;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus=`first-tabbable`;restoreFocus=!0;scrollStrategy;closeOnNavigation=!0;closeOnDestroy=!0;closeOnOverlayDetachments=!0;disableAnimations=!1;providers;container;templateContext;bindings};var Pb=(()=>{class t extends Zr{_elementRef=f(W);_focusTrapFactory=f(Pg);_config;_interactivityChecker=f(Lr);_ngZone=f(X);_focusMonitor=f(Jt);_renderer=f(Re);_changeDetectorRef=f(Fe);_injector=f(se);_platform=f(Oe);_document=f(de);_portalOutlet;_focusTrapped=new L;_focusTrap=null;_elementFocusedBeforeDialogWasOpened=null;_closeInteractionType=null;_ariaLabelledByQueue=[];_isDestroyed=!1;constructor(){super(),this._config=f(Ba,{optional:!0})||new Ba,this._config.ariaLabelledBy&&this._ariaLabelledByQueue.push(this._config.ariaLabelledBy)}_addAriaLabelledBy(e){this._ariaLabelledByQueue.push(e),this._changeDetectorRef.markForCheck()}_removeAriaLabelledBy(e){let i=this._ariaLabelledByQueue.indexOf(e);i>-1&&(this._ariaLabelledByQueue.splice(i,1),this._changeDetectorRef.markForCheck())}_contentAttached(){this._initializeFocusTrap(),this._captureInitialFocus()}_captureInitialFocus(){this._trapFocus()}ngOnDestroy(){this._focusTrapped.complete(),this._isDestroyed=!0,this._restoreFocus()}attachComponentPortal(e){this._portalOutlet.hasAttached();let i=this._portalOutlet.attachComponentPortal(e);return this._contentAttached(),i}attachTemplatePortal(e){this._portalOutlet.hasAttached();let i=this._portalOutlet.attachTemplatePortal(e);return this._contentAttached(),i}attachDomPortal=e=>{this._portalOutlet.hasAttached();let i=this._portalOutlet.attachDomPortal(e);return this._contentAttached(),i};_recaptureFocus(){this._containsFocus()||this._trapFocus()}_forceFocus(e,i){this._interactivityChecker.isFocusable(e)||(e.tabIndex=-1,this._ngZone.runOutsideAngular(()=>{let a=()=>{o(),r(),e.removeAttribute(`tabindex`)},o=this._renderer.listen(e,`blur`,a),r=this._renderer.listen(e,`mousedown`,a)})),e.focus(i)}_focusByCssSelector(e,i){let a=this._elementRef.nativeElement.querySelector(e);a&&this._forceFocus(a,i)}_trapFocus(e){this._isDestroyed||At(()=>{let i=this._elementRef.nativeElement;switch(this._config.autoFocus){case!1:case`dialog`:this._containsFocus()||i.focus(e);break;case!0:case`first-tabbable`:this._focusTrap?.focusInitialElement(e)||this._focusDialogContainer(e);break;case`first-heading`:this._focusByCssSelector(`h1, h2, h3, h4, h5, h6, [role="heading"]`,e);break;default:this._focusByCssSelector(this._config.autoFocus,e)}this._focusTrapped.next()},{injector:this._injector})}_restoreFocus(){let e=this._config.restoreFocus,i=null;if(typeof e==`string`?i=this._document.querySelector(e):typeof e==`boolean`?i=e?this._elementFocusedBeforeDialogWasOpened:null:e&&(i=e),this._config.restoreFocus&&i&&typeof i.focus==`function`){let a=Cc(),o=this._elementRef.nativeElement;(!a||a===this._document.body||a===o||o.contains(a))&&(this._focusMonitor?(this._focusMonitor.focusVia(i,this._closeInteractionType),this._closeInteractionType=null):i.focus())}this._focusTrap&&this._focusTrap.destroy()}_focusDialogContainer(e){this._elementRef.nativeElement.focus?.(e)}_containsFocus(){let e=this._elementRef.nativeElement,i=Cc();return e===i||e.contains(i)}_initializeFocusTrap(){this._platform.isBrowser&&(this._focusTrap=this._focusTrapFactory.create(this._elementRef.nativeElement),this._document&&(this._elementFocusedBeforeDialogWasOpened=Cc()))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(i,a){}return B({type:t,selectors:[[`cdk-dialog-container`]],viewQuery:function(a,o){if(a&1&&Ze(Aa,7),a&2){let r;Z(r=J())&&(o._portalOutlet=r.first)}},hostAttrs:[`tabindex`,`-1`,1,`cdk-dialog-container`],hostVars:6,hostBindings:function(a,o){a&2&&te(`id`,o._config.id||null)(`role`,o._config.role)(`aria-modal`,o._config.ariaModal)(`aria-labelledby`,o._config.ariaLabel?null:o._ariaLabelledByQueue[0])(`aria-label`,o._config.ariaLabel)(`aria-describedby`,o._config.ariaDescribedBy||null)},features:[Ve],decls:1,vars:0,consts:[[`cdkPortalOutlet`,``]],template:function(a,o){a&1&&ot(0,e,0,0,`ng-template`,0)},dependencies:[Aa],styles:[`.cdk-dialog-container {
  display: block;
  width: 100%;
  height: 100%;
  min-height: inherit;
  max-height: inherit;
}
`],encapsulation:2,changeDetection:1})})()}return t})();var zo=class{overlayRef;config;componentInstance=null;componentRef=null;containerInstance;disableClose;closed=new L;backdropClick;keydownEvents;outsidePointerEvents;id;_detachSubscription;constructor(n,e){this.overlayRef=n,this.config=e,this.disableClose=e.disableClose,this.backdropClick=n.backdropClick(),this.keydownEvents=n.keydownEvents(),this.outsidePointerEvents=n.outsidePointerEvents(),this.id=e.id,this.keydownEvents.subscribe(i=>{i.keyCode===27&&!this.disableClose&&!xt(i)&&(i.preventDefault(),this.close(void 0,{focusOrigin:`keyboard`}))}),this.backdropClick.subscribe(()=>{!this.disableClose&&this._canClose()?this.close(void 0,{focusOrigin:`mouse`}):this.containerInstance._recaptureFocus?.()}),this._detachSubscription=n.detachments().subscribe(()=>{e.closeOnOverlayDetachments!==!1&&this.close()})}close(n,e){if(this._canClose(n)){let i=this.closed;this.containerInstance._closeInteractionType=e?.focusOrigin||`program`,this._detachSubscription.unsubscribe(),this.overlayRef.dispose(),i.next(n),i.complete(),this.componentInstance=this.containerInstance=null}}updatePosition(){return this.overlayRef.updatePosition(),this}updateSize(n=``,e=``){return this.overlayRef.updateSize({width:n,height:e}),this}addPanelClass(n){return this.overlayRef.addPanelClass(n),this}removePanelClass(n){return this.overlayRef.removePanelClass(n),this}_canClose(n){let e=this.config;return!!this.containerInstance&&(!e.closePredicate||e.closePredicate(n,e,this.componentInstance))}};var _P=new k(`DialogScrollStrategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ts(t)}});var yP=new k(`DialogData`);var xP=new k(`DefaultDialogConfig`);function CP(t){let n=T(t),e=new ee;return{valueSignal:n,get value(){return n()},change:e,ngOnDestroy(){e.complete()}}}var Fb=(()=>{class t{_injector=f(se);_defaultOptions=f(xP,{optional:!0});_parentDialog=f(t,{optional:!0,skipSelf:!0});_overlayContainer=f(cm);_idGenerator=f(Be);_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new L;_afterOpenedAtThisLevel=new L;_ariaHiddenElements=new Map;_scrollStrategy=f(_P);get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}afterAllClosed=Ka(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(mt(void 0)));open(e,i){let a=this._defaultOptions||new Ba;i=r(r({},a),i),i.id=i.id||this._idGenerator.getId(`cdk-dialog-`),i.id&&this.getDialogById(i.id);let o=this._getOverlayConfig(i),r$3=sa(this._injector,o),s=new zo(r$3,i),c=this._attachContainer(r$3,s,i);if(s.containerInstance=c,!this.openDialogs.length){let h=this._overlayContainer.getContainerElement();c._focusTrapped?c._focusTrapped.pipe(vn(1)).subscribe(()=>{this._hideNonDialogContentFromAssistiveTechnology(h)}):this._hideNonDialogContentFromAssistiveTechnology(h)}return this._attachDialogContent(e,s,c,i),this.openDialogs.push(s),s.closed.subscribe(()=>this._removeOpenDialog(s,!0)),this.afterOpened.next(s),s}closeAll(){Rb(this.openDialogs,e=>e.close())}getDialogById(e){return this.openDialogs.find(i=>i.id===e)}ngOnDestroy(){Rb(this._openDialogsAtThisLevel,e=>{e.config.closeOnDestroy===!1&&this._removeOpenDialog(e,!1)}),Rb(this._openDialogsAtThisLevel,e=>e.close()),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete(),this._openDialogsAtThisLevel=[]}_getOverlayConfig(e){let i=new aa({positionStrategy:e.positionStrategy||ns().centerHorizontally().centerVertically(),scrollStrategy:e.scrollStrategy||this._scrollStrategy(),panelClass:e.panelClass,hasBackdrop:e.hasBackdrop,direction:e.direction,minWidth:e.minWidth,minHeight:e.minHeight,maxWidth:e.maxWidth,maxHeight:e.maxHeight,width:e.width,height:e.height,disposeOnNavigation:e.closeOnNavigation,disableAnimations:e.disableAnimations});return e.backdropClass&&(i.backdropClass=e.backdropClass),i}_attachContainer(e,i,a){let o=a.injector||a.viewContainerRef?.injector,r=[{provide:Ba,useValue:a},{provide:zo,useValue:i},{provide:Jr,useValue:e}],s;a.container?typeof a.container==`function`?s=a.container:(s=a.container.type,r.push(...a.container.providers(a))):s=Pb;let c=new Na(s,a.viewContainerRef,se.create({parent:o||this._injector,providers:r}));return e.attach(c).instance}_attachDialogContent(e,i,a,o){if(e instanceof Vt){let r$4=this._createInjector(o,i,a,void 0),s={$implicit:o.data,dialogRef:i};o.templateContext&&(s=r(r({},s),typeof o.templateContext==`function`?o.templateContext():o.templateContext)),a.attachTemplatePortal(new Hn(e,null,s,r$4))}else{let r=this._createInjector(o,i,a,this._injector),s=a.attachComponentPortal(new Na(e,o.viewContainerRef,r,null,o.bindings));i.componentRef=s,i.componentInstance=s.instance}}_createInjector(e,i,a,o){let r=e.injector||e.viewContainerRef?.injector,s=[{provide:yP,useValue:e.data},{provide:zo,useValue:i}];return e.providers&&(typeof e.providers==`function`?s.push(...e.providers(i,e,a)):s.push(...e.providers)),e.direction&&(!r||!r.get(gt,null,{optional:!0}))&&s.push({provide:gt,useValue:CP(e.direction)}),se.create({parent:r||o,providers:s})}_removeOpenDialog(e,i){let a=this.openDialogs.indexOf(e);a>-1&&(this.openDialogs.splice(a,1),this.openDialogs.length||(this._ariaHiddenElements.forEach((o,r)=>{o?r.setAttribute(`aria-hidden`,o):r.removeAttribute(`aria-hidden`)}),this._ariaHiddenElements.clear(),i&&this._getAfterAllClosed().next()))}_hideNonDialogContentFromAssistiveTechnology(e){if(e.parentElement){let i=e.parentElement.children;for(let a=i.length-1;a>-1;a--){let o=i[a];o!==e&&o.nodeName!==`SCRIPT`&&o.nodeName!==`STYLE`&&!o.hasAttribute(`aria-live`)&&!o.hasAttribute(`popover`)&&(this._ariaHiddenElements.set(o,o.getAttribute(`aria-hidden`)),o.setAttribute(`aria-hidden`,`true`))}}}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();function Rb(t,n){let e=t.length;for(;e--;)n(t[e])}var $w=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({providers:[Fb],imports:[mi,Lo,Mo,Lo]})}return t})();var Cm=class{viewContainerRef;injector;id;role=`dialog`;panelClass=``;hasBackdrop=!0;backdropClass=``;disableClose=!1;closePredicate;width=``;height=``;minWidth;minHeight;maxWidth;maxHeight;position;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus=`first-tabbable`;restoreFocus=!0;delayFocusTrap=!0;scrollStrategy;closeOnNavigation=!0;enterAnimationDuration;exitAnimationDuration;bindings};var Lb=`mdc-dialog--open`;var Gw=`mdc-dialog--opening`;var Ww=`mdc-dialog--closing`;var SP=150;var wP=75;var EP=(()=>{class t extends Pb{_animationStateChanged=new ee;_animationsEnabled=!Ue();_actionSectionCount=0;_hostElement=this._elementRef.nativeElement;_enterAnimationDuration=this._animationsEnabled?Yw(this._config.enterAnimationDuration)??SP:0;_exitAnimationDuration=this._animationsEnabled?Yw(this._config.exitAnimationDuration)??wP:0;_animationTimer=null;_contentAttached(){super._contentAttached(),this._startOpenAnimation()}_startOpenAnimation(){this._animationStateChanged.emit({state:`opening`,totalTime:this._enterAnimationDuration}),this._animationsEnabled?(this._hostElement.style.setProperty(qw,`${this._enterAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add(Gw,Lb)),this._waitForAnimationToComplete(this._enterAnimationDuration,this._finishDialogOpen)):(this._hostElement.classList.add(Lb),Promise.resolve().then(()=>this._finishDialogOpen()))}_startExitAnimation(){this._animationStateChanged.emit({state:`closing`,totalTime:this._exitAnimationDuration}),this._hostElement.classList.remove(Lb),this._animationsEnabled?(this._hostElement.style.setProperty(qw,`${this._exitAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add(Ww)),this._waitForAnimationToComplete(this._exitAnimationDuration,this._finishDialogClose)):Promise.resolve().then(()=>this._finishDialogClose())}_updateActionSectionCount(e){this._actionSectionCount+=e,this._changeDetectorRef.markForCheck()}_finishDialogOpen=()=>{this._clearAnimationClasses(),this._openAnimationDone(this._enterAnimationDuration)};_finishDialogClose=()=>{this._clearAnimationClasses(),this._animationStateChanged.emit({state:`closed`,totalTime:this._exitAnimationDuration})};_clearAnimationClasses(){this._hostElement.classList.remove(Gw,Ww)}_waitForAnimationToComplete(e,i){this._animationTimer!==null&&clearTimeout(this._animationTimer),this._animationTimer=setTimeout(i,e)}_requestAnimationFrame(e){this._ngZone.runOutsideAngular(()=>{typeof requestAnimationFrame==`function`?requestAnimationFrame(e):e()})}_captureInitialFocus(){this._config.delayFocusTrap||this._trapFocus()}_openAnimationDone(e){this._config.delayFocusTrap&&this._trapFocus(),this._animationStateChanged.next({state:`opened`,totalTime:e})}ngOnDestroy(){super.ngOnDestroy(),this._animationTimer!==null&&clearTimeout(this._animationTimer)}attachComponentPortal(e){let i=super.attachComponentPortal(e);return i.location.nativeElement.classList.add(`mat-mdc-dialog-component-host`),i}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵcmp=(function(){function e(i,a){}return B({type:t,selectors:[[`mat-dialog-container`]],hostAttrs:[`tabindex`,`-1`,1,`mat-mdc-dialog-container`,`mdc-dialog`],hostVars:10,hostBindings:function(a,o){a&2&&(qt(`id`,o._config.id),te(`aria-modal`,o._config.ariaModal)(`role`,o._config.role)(`aria-labelledby`,o._config.ariaLabel?null:o._ariaLabelledByQueue[0])(`aria-label`,o._config.ariaLabel)(`aria-describedby`,o._config.ariaDescribedBy||null),Y(`_mat-animation-noopable`,!o._animationsEnabled)(`mat-mdc-dialog-container-with-actions`,o._actionSectionCount>0))},features:[Ve],decls:3,vars:0,consts:[[1,`mat-mdc-dialog-inner-container`,`mdc-dialog__container`],[1,`mat-mdc-dialog-surface`,`mdc-dialog__surface`],[`cdkPortalOutlet`,``]],template:function(a,o){a&1&&(l(0,`div`,0)(1,`div`,1),ot(2,e,0,0,`ng-template`,2),d()())},dependencies:[Aa],styles:[`.mat-mdc-dialog-container {
  width: 100%;
  height: 100%;
  display: block;
  box-sizing: border-box;
  max-height: inherit;
  min-height: inherit;
  min-width: inherit;
  max-width: inherit;
  outline: 0;
}

.cdk-overlay-pane.mat-mdc-dialog-panel {
  max-width: var(--%NS%mat-dialog-container-max-width, 560px);
  min-width: var(--%NS%mat-dialog-container-min-width, 280px);
}
@media (max-width: 599px) {
  .cdk-overlay-pane.mat-mdc-dialog-panel {
    max-width: var(--%NS%mat-dialog-container-small-max-width, calc(100vw - 32px));
  }
}

.mat-mdc-dialog-inner-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-around;
  box-sizing: border-box;
  height: 100%;
  opacity: 0;
  transition: opacity linear var(--%NS%mat-dialog-transition-duration, 0ms);
  max-height: inherit;
  min-height: inherit;
  min-width: inherit;
  max-width: inherit;
}
.mdc-dialog--closing .mat-mdc-dialog-inner-container {
  transition: opacity 75ms linear;
  transform: none;
}
.mdc-dialog--open .mat-mdc-dialog-inner-container {
  opacity: 1;
}
._mat-animation-noopable .mat-mdc-dialog-inner-container {
  transition: none;
}

.mat-mdc-dialog-surface {
  display: flex;
  flex-direction: column;
  flex-grow: 0;
  flex-shrink: 0;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  position: relative;
  overflow-y: auto;
  outline: 0;
  transform: scale(0.8);
  transition: transform var(--%NS%mat-dialog-transition-duration, 0ms) cubic-bezier(0, 0, 0.2, 1);
  max-height: inherit;
  min-height: inherit;
  min-width: inherit;
  max-width: inherit;
  box-shadow: var(--%NS%mat-dialog-container-elevation-shadow, none);
  border-radius: var(--%NS%mat-dialog-container-shape, var(--%NS%mat-sys-corner-extra-large, 4px));
  background-color: var(--%NS%mat-dialog-container-color, var(--%NS%mat-sys-surface, white));
}
[dir=rtl] .mat-mdc-dialog-surface {
  text-align: right;
}
.mdc-dialog--open .mat-mdc-dialog-surface, .mdc-dialog--closing .mat-mdc-dialog-surface {
  transform: none;
}
._mat-animation-noopable .mat-mdc-dialog-surface {
  transition: none;
}
.mat-mdc-dialog-surface::before {
  position: absolute;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  border: 2px solid transparent;
  border-radius: inherit;
  content: "";
  pointer-events: none;
}

.mat-mdc-dialog-title {
  display: block;
  position: relative;
  flex-shrink: 0;
  box-sizing: border-box;
  margin: 0 0 1px;
  padding: var(--%NS%mat-dialog-headline-padding, 6px 24px 13px);
}
.mat-mdc-dialog-title::before {
  display: inline-block;
  width: 0;
  height: 40px;
  content: "";
  vertical-align: 0;
}
[dir=rtl] .mat-mdc-dialog-title {
  text-align: right;
}
.mat-mdc-dialog-container .mat-mdc-dialog-title {
  color: var(--%NS%mat-dialog-subhead-color, var(--%NS%mat-sys-on-surface, rgba(0, 0, 0, 0.87)));
  font-family: var(--%NS%mat-dialog-subhead-font, var(--%NS%mat-sys-headline-small-font, inherit));
  line-height: var(--%NS%mat-dialog-subhead-line-height, var(--%NS%mat-sys-headline-small-line-height, 1.5rem));
  font-size: var(--%NS%mat-dialog-subhead-size, var(--%NS%mat-sys-headline-small-size, 1rem));
  font-weight: var(--%NS%mat-dialog-subhead-weight, var(--%NS%mat-sys-headline-small-weight, 400));
  letter-spacing: var(--%NS%mat-dialog-subhead-tracking, var(--%NS%mat-sys-headline-small-tracking, 0.03125em));
}

.mat-mdc-dialog-content {
  display: block;
  flex-grow: 1;
  box-sizing: border-box;
  margin: 0;
  overflow: auto;
  max-height: 65vh;
}
.mat-mdc-dialog-content > :first-child {
  margin-top: 0;
}
.mat-mdc-dialog-content > :last-child {
  margin-bottom: 0;
}
.mat-mdc-dialog-container .mat-mdc-dialog-content {
  color: var(--%NS%mat-dialog-supporting-text-color, var(--%NS%mat-sys-on-surface-variant, rgba(0, 0, 0, 0.6)));
  font-family: var(--%NS%mat-dialog-supporting-text-font, var(--%NS%mat-sys-body-medium-font, inherit));
  line-height: var(--%NS%mat-dialog-supporting-text-line-height, var(--%NS%mat-sys-body-medium-line-height, 1.5rem));
  font-size: var(--%NS%mat-dialog-supporting-text-size, var(--%NS%mat-sys-body-medium-size, 1rem));
  font-weight: var(--%NS%mat-dialog-supporting-text-weight, var(--%NS%mat-sys-body-medium-weight, 400));
  letter-spacing: var(--%NS%mat-dialog-supporting-text-tracking, var(--%NS%mat-sys-body-medium-tracking, 0.03125em));
}
.mat-mdc-dialog-container .mat-mdc-dialog-content {
  padding: var(--%NS%mat-dialog-content-padding, 20px 24px);
}
.mat-mdc-dialog-container-with-actions .mat-mdc-dialog-content {
  padding: var(--%NS%mat-dialog-with-actions-content-padding, 20px 24px 0);
}
.mat-mdc-dialog-container .mat-mdc-dialog-title + .mat-mdc-dialog-content {
  padding-top: 0;
}

.mat-mdc-dialog-actions {
  display: flex;
  position: relative;
  flex-shrink: 0;
  flex-wrap: wrap;
  align-items: center;
  box-sizing: border-box;
  min-height: 52px;
  margin: 0;
  border-top: 1px solid transparent;
  padding: var(--%NS%mat-dialog-actions-padding, 16px 24px);
  justify-content: var(--%NS%mat-dialog-actions-alignment, flex-end);
}
@media (forced-colors: active) {
  .mat-mdc-dialog-actions {
    border-top-color: CanvasText;
  }
}
.mat-mdc-dialog-actions.mat-mdc-dialog-actions-align-start, .mat-mdc-dialog-actions[align=start] {
  justify-content: start;
}
.mat-mdc-dialog-actions.mat-mdc-dialog-actions-align-center, .mat-mdc-dialog-actions[align=center] {
  justify-content: center;
}
.mat-mdc-dialog-actions.mat-mdc-dialog-actions-align-end, .mat-mdc-dialog-actions[align=end] {
  justify-content: flex-end;
}
.mat-mdc-dialog-actions .mat-button-base + .mat-button-base,
.mat-mdc-dialog-actions .mat-mdc-button-base + .mat-mdc-button-base {
  margin-left: 8px;
}
[dir=rtl] .mat-mdc-dialog-actions .mat-button-base + .mat-button-base,
[dir=rtl] .mat-mdc-dialog-actions .mat-mdc-button-base + .mat-mdc-button-base {
  margin-left: 0;
  margin-right: 8px;
}

.mat-mdc-dialog-component-host {
  display: contents;
}
`],encapsulation:2,changeDetection:1})})()}return t})();var qw=`--mat-dialog-transition-duration`;function Yw(t){return t==null?null:typeof t==`number`?t:t.endsWith(`ms`)?ri(t.substring(0,t.length-2)):t.endsWith(`s`)?ri(t.substring(0,t.length-1))*1e3:t===`0`?0:null}var xm=(function(t){return t[t.OPEN=0]=`OPEN`,t[t.CLOSING=1]=`CLOSING`,t[t.CLOSED=2]=`CLOSED`,t})(xm||{});var Xc=class{_ref;_config;_containerInstance;componentInstance;componentRef=null;disableClose;id;_afterOpened=new Yn(1);_beforeClosed=new Yn(1);_result;_closeFallbackTimeout;_state=xm.OPEN;_closeInteractionType;constructor(n,e,i){this._ref=n,this._config=e,this._containerInstance=i,this.disableClose=e.disableClose,this.id=n.id,n.addPanelClass(`mat-mdc-dialog-panel`),i._animationStateChanged.pipe(Qe(a=>a.state===`opened`),vn(1)).subscribe(()=>{this._afterOpened.next(),this._afterOpened.complete()}),i._animationStateChanged.pipe(Qe(a=>a.state===`closed`),vn(1)).subscribe(()=>{clearTimeout(this._closeFallbackTimeout),this._finishDialogClose()}),n.overlayRef.detachments().subscribe(()=>{this._beforeClosed.next(this._result),this._beforeClosed.complete(),this._finishDialogClose()}),It(this.backdropClick(),this.keydownEvents().pipe(Qe(a=>a.keyCode===27&&!this.disableClose&&!xt(a)))).subscribe(a=>{this.disableClose||(a.preventDefault(),Kw(this,a.type===`keydown`?`keyboard`:`mouse`))})}close(n){let e=this._config.closePredicate;e&&!e(n,this._config,this.componentInstance)||(this._result=n,this._containerInstance._animationStateChanged.pipe(Qe(i=>i.state===`closing`),vn(1)).subscribe(i=>{this._beforeClosed.next(n),this._beforeClosed.complete(),this._ref.overlayRef.detachBackdrop(),this._closeFallbackTimeout=setTimeout(()=>this._finishDialogClose(),i.totalTime+100)}),this._state=xm.CLOSING,this._containerInstance._startExitAnimation())}afterOpened(){return this._afterOpened}afterClosed(){return this._ref.closed}beforeClosed(){return this._beforeClosed}backdropClick(){return this._ref.backdropClick}keydownEvents(){return this._ref.keydownEvents}updatePosition(n){let e=this._ref.config.positionStrategy;return n&&(n.left||n.right)?n.left?e.left(n.left):e.right(n.right):e.centerHorizontally(),n&&(n.top||n.bottom)?n.top?e.top(n.top):e.bottom(n.bottom):e.centerVertically(),this._ref.updatePosition(),this}updateSize(n=``,e=``){return this._ref.updateSize(n,e),this}addPanelClass(n){return this._ref.addPanelClass(n),this}removePanelClass(n){return this._ref.removePanelClass(n),this}getState(){return this._state}_finishDialogClose(){this._state=xm.CLOSED,this._ref.close(this._result,{focusOrigin:this._closeInteractionType}),this.componentInstance=null}};function Kw(t,n,e){return t._closeInteractionType=n,t.close(e)}var Zc=new k(`MatMdcDialogData`);var DP=new k(`mat-mdc-dialog-default-options`);var IP=new k(`mat-mdc-dialog-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ts(t)}});var Ho=(()=>{class t{_defaultOptions=f(DP,{optional:!0});_scrollStrategy=f(IP);_parentDialog=f(t,{optional:!0,skipSelf:!0});_idGenerator=f(Be);_injector=f(se);_dialog=f(Fb);_animationsDisabled=Ue();_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new L;_afterOpenedAtThisLevel=new L;dialogConfigClass=Cm;_dialogRefConstructor;_dialogContainerType;_dialogDataToken;get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}afterAllClosed=Ka(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(mt(void 0)));constructor(){this._dialogRefConstructor=Xc,this._dialogContainerType=EP,this._dialogDataToken=Zc}open(e,i){let a;i=r(r({},this._defaultOptions||new Cm),i),i.id=i.id||this._idGenerator.getId(`mat-mdc-dialog-`),i.scrollStrategy=i.scrollStrategy||this._scrollStrategy();let o=this._dialog.open(e,s(r({},i),{positionStrategy:ns(this._injector).centerHorizontally().centerVertically(),disableClose:!0,closePredicate:void 0,closeOnDestroy:!1,closeOnOverlayDetachments:!1,disableAnimations:this._animationsDisabled||i.enterAnimationDuration?.toLocaleString()===`0`||i.exitAnimationDuration?.toString()===`0`,container:{type:this._dialogContainerType,providers:()=>[{provide:this.dialogConfigClass,useValue:i},{provide:Ba,useValue:i}]},templateContext:()=>({dialogRef:a}),providers:(r,s,c)=>(a=new this._dialogRefConstructor(r,i,c),a.updatePosition(i?.position),[{provide:this._dialogContainerType,useValue:c},{provide:this._dialogDataToken,useValue:s.data},{provide:this._dialogRefConstructor,useValue:a},{provide:zo,useValue:null}])}));return a.componentRef=o.componentRef,a.componentInstance=o.componentInstance,this.openDialogs.push(a),this.afterOpened.next(a),a.afterClosed().subscribe(()=>{let r=this.openDialogs.indexOf(a);r>-1&&(this.openDialogs.splice(r,1),this.openDialogs.length||this._getAfterAllClosed().next())}),a}closeAll(){this._closeDialogs(this.openDialogs)}getDialogById(e){return this.openDialogs.find(i=>i.id===e)}ngOnDestroy(){this._closeDialogs(this._openDialogsAtThisLevel),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete()}_closeDialogs(e){let i=e.length;for(;i--;)e[i].close()}static ɵfac=function(i){return new(i||t)};static ɵprov=ce({token:t,factory:t.ɵfac})}return t})();var Sm=(()=>{class t{dialogRef=f(Xc,{optional:!0});_elementRef=f(W);_dialog=f(Ho);ariaLabel;type=`button`;dialogResult;_matDialogClose;ngOnInit(){this.dialogRef||(this.dialogRef=Xw(this._elementRef,this._dialog.openDialogs))}ngOnChanges(e){let i=e._matDialogClose;i&&(this.dialogResult=i.currentValue)}_onButtonClick(e){this._elementRef.nativeElement.getAttribute(`aria-disabled`)!==`true`&&Kw(this.dialogRef,e.screenX===0&&e.screenY===0?`keyboard`:`mouse`,this.dialogResult)}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`mat-dialog-close`,``],[``,`matDialogClose`,``]],hostVars:2,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._onButtonClick(r)}),i&2&&te(`aria-label`,a.ariaLabel||null)(`type`,a.type)},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],type:`type`,dialogResult:[0,`mat-dialog-close`,`dialogResult`],_matDialogClose:[0,`matDialogClose`,`_matDialogClose`]},exportAs:[`matDialogClose`],features:[vt]})}return t})();var Qw=(()=>{class t{_dialogRef=f(Xc,{optional:!0});_elementRef=f(W);_dialog=f(Ho);ngOnInit(){this._dialogRef||(this._dialogRef=Xw(this._elementRef,this._dialog.openDialogs)),this._dialogRef&&Promise.resolve().then(()=>{this._onAdd()})}ngOnDestroy(){this._dialogRef?._containerInstance&&Promise.resolve().then(()=>{this._onRemove()})}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t})}return t})();var wm=(()=>{class t extends Qw{id=f(Be).getId(`mat-mdc-dialog-title-`);_onAdd(){this._dialogRef._containerInstance?._addAriaLabelledBy?.(this.id)}_onRemove(){this._dialogRef?._containerInstance?._removeAriaLabelledBy?.(this.id)}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=K({type:t,selectors:[[``,`mat-dialog-title`,``],[``,`matDialogTitle`,``]],hostAttrs:[1,`mat-mdc-dialog-title`,`mdc-dialog__title`],hostVars:1,hostBindings:function(i,a){i&2&&qt(`id`,a.id)},inputs:{id:`id`},exportAs:[`matDialogTitle`],features:[Ve]})}return t})();var Em=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[``,`mat-dialog-content`,``],[`mat-dialog-content`],[``,`matDialogContent`,``]],hostAttrs:[1,`mat-mdc-dialog-content`,`mdc-dialog__content`],features:[ic([Uc])]})}return t})();var Dm=(()=>{class t extends Qw{align;_onAdd(){this._dialogRef._containerInstance?._updateActionSectionCount?.(1)}_onRemove(){this._dialogRef._containerInstance?._updateActionSectionCount?.(-1)}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=K({type:t,selectors:[[``,`mat-dialog-actions`,``],[`mat-dialog-actions`],[``,`matDialogActions`,``]],hostAttrs:[1,`mat-mdc-dialog-actions`,`mdc-dialog__actions`],hostVars:6,hostBindings:function(i,a){i&2&&Y(`mat-mdc-dialog-actions-align-start`,a.align===`start`)(`mat-mdc-dialog-actions-align-center`,a.align===`center`)(`mat-mdc-dialog-actions-align-end`,a.align===`end`)},inputs:{align:`align`},features:[Ve]})}return t})();function Xw(t,n){let e=t.nativeElement.parentElement;for(;e&&!e.classList.contains(`mat-mdc-dialog-container`);)e=e.parentElement;return e?n.find(i=>i.id===e.id):null}var Im=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({providers:[Ho],imports:[$w,mi,Lo,we]})}return t})();var km=class t{http=f(Di);getStatus(){return St(this.http.get(this.endpoint(`/status`)))}getComponentUsage(n){return St(this.http.get(this.endpoint(`/components/usage`),{params:{scope:n}}))}getComponentCandidates(n){return St(this.http.get(this.endpoint(`/components/candidates`),{params:r({scope:n.scope,basis:n.basis,offset:n.offset,limit:n.limit},n.selector)}))}getEvaluationChanges(n,e){return St(this.http.get(this.endpoint(`/evaluations/changes`),{params:{after:n,limit:e}}))}getElites(){return St(this.http.get(this.endpoint(`/elites`)))}getEliteHistory(){return St(this.http.get(this.endpoint(`/elites/history`)))}getCandidate(n){return St(this.http.get(this.endpoint(`/candidates/${encodeURIComponent(n)}`)))}getSearchSpace(){return St(this.http.get(this.endpoint(`/search-space`)))}getArtifacts(){return St(this.http.get(this.endpoint(`/artifacts`)))}artifactUrl(n){return this.endpoint(`/artifacts/${encodeURIComponent(n)}`)}endpoint(n){return`${In.apiBaseUrl}/api/autoconfig${n}`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Uo=class{static evaluationCost(n){let e=[],i=[];for(let a of n){if(a.state!==`SUCCEEDED`||typeof a.cost!=`number`||!Number.isFinite(a.cost))continue;let o={x:a.id,y:a.cost,custom:{configurationId:a.configurationId,instanceName:a.instanceName,timeSeconds:a.timeSeconds}};(a.slow?i:e).push(o)}return{options:this.baseOptions(`Evaluation cost`,`Successful IRACE costs. Lower is better.`,{allowDecimals:!1,title:{text:`Evaluation ID`}},{title:{text:`IRACE cost`}},[{type:`scatter`,name:`Succeeded`,data:e,color:`#00aab5`,marker:{radius:2.5},turboThreshold:0},{type:`scatter`,name:`Slow`,data:i,color:`#ed8e00`,marker:{radius:3.5,symbol:`diamond`},turboThreshold:0}],`<b>{series.name}</b><br/>Evaluation: {point.x}<br/>Cost: {point.y}<br/>Configuration: {point.custom.configurationId}<br/>Instance: {point.custom.instanceName}`)}}static evaluationActivity(n){let e=n.filter(s=>s.finishedAt&&s.state!==`RUNNING`).slice().sort((s,c)=>{let h=Date.parse(s.finishedAt)-Date.parse(c.finishedAt);return h===0?s.id-c.id:h}),i={SUCCEEDED:0,REJECTED:0,FAILED:0},a=[],o=[],r=[];for(let s of e){let c=s.state;if(c!==`SUCCEEDED`&&c!==`REJECTED`&&c!==`FAILED`)continue;i[c]++;let h=[Date.parse(s.finishedAt),i[c]];c===`SUCCEEDED`?a.push(h):c===`REJECTED`?o.push(h):r.push(h)}return{options:this.baseOptions(`Evaluation activity`,`Cumulative terminal evaluations over wall-clock time.`,{type:`datetime`,title:{text:`Completion time`}},{allowDecimals:!1,min:0,title:{text:`Evaluations`}},[{type:`line`,name:`Succeeded`,data:a,color:`#079447`,marker:{enabled:!1}},{type:`line`,name:`Rejected`,data:o,color:`#ed8e00`,marker:{enabled:!1}},{type:`line`,name:`Failed`,data:r,color:`#d32f2f`,marker:{enabled:!1}}],`<b>{series.name}</b><br/>{point.x:%Y-%m-%d %H:%M:%S}<br/>Count: {point.y}`)}}static eliteRanks(n){let e=n?.iterations??[],i=[];for(let o of e)for(let r of o.elites)i.includes(r.configurationId)||i.push(r.configurationId);let a=[];for(let o of i){let r=[];for(let s of e){let c=null;for(let h of s.elites)if(h.configurationId===o){c=h.position;break}r.push([s.iteration,c])}a.push({type:`line`,name:o,data:r,connectNulls:!1,marker:{enabled:!0,radius:2.5},step:`left`})}return{options:this.baseOptions(`Elite rank evolution`,`Elite positions at completed IRACE race checkpoints.`,{allowDecimals:!1,title:{text:`Race checkpoint`}},{allowDecimals:!1,min:1,reversed:!0,title:{text:`Elite position`}},a,`<b>{series.name}</b><br/>Checkpoint: {point.x}<br/>Position: {point.y}`)}}static instanceSummaries(n){let e=new Map;for(let i of n){let a=e.get(i.instanceName)??{instanceName:i.instanceName,evaluations:0,bestCost:null,issues:0,slow:0},o=a.bestCost;i.state===`SUCCEEDED`&&typeof i.cost==`number`&&Number.isFinite(i.cost)&&(o===null||i.cost<o)&&(o=i.cost),e.set(i.instanceName,{instanceName:a.instanceName,evaluations:a.evaluations+1,bestCost:o,issues:a.issues+(i.state===`REJECTED`||i.state===`FAILED`?1:0),slow:a.slow+(i.slow?1:0)})}return[...e.values()].sort((i,a)=>a.issues-i.issues||i.instanceName.localeCompare(a.instanceName))}static baseOptions(n,e,i,a,o,r){return{accessibility:{enabled:!0,description:e},boost:{enabled:!0,useGPUTranslations:!0,seriesThreshold:20},chart:{animation:!1,height:330,spacing:[12,12,12,12]},credits:{enabled:!1},exporting:{enabled:!0},legend:{enabled:!0,maxHeight:72},title:{text:n,style:{display:`none`}},xAxis:i,yAxis:a,tooltip:{headerFormat:``,pointFormat:r},plotOptions:{series:{animation:!1,connectNulls:!1}},series:o}}};var kP=500;var MP=2e3;var TP=1e4;var NP=1e4;var Vb=`mork.autoconfig.alertSeen.v1`;var ja=class t{client=f(km);destroyRef=f(Ne);statusSignal=T(null);componentUsageSignal=T(null);componentScopeSignal=T(`ALL`);componentErrorSignal=T(null);syncStateSignal=T(`detecting`);latestErrorSignal=T(null);evaluationsSignal=T([]);elitesSignal=T(null);eliteHistorySignal=T(null);searchSpaceSignal=T(null);artifactsSignal=T(null);alertsSignal=T([]);seenAlertRevisionSignal=T(0);runFailureSeenSignal=T(!1);lastUpdatedSignal=T(null);evaluationMap=new Map;candidateCache=new Map;pollTimer=null;currentRunId=null;evaluationRevision=0;eliteMarker=null;componentMarker=null;componentRequest=0;searchSpaceLoaded=!1;lastArtifactFetch=0;lastArtifactPhase=null;started=!1;destroyed=!1;status=this.statusSignal.asReadonly();componentUsage=this.componentUsageSignal.asReadonly();componentScope=this.componentScopeSignal.asReadonly();componentError=this.componentErrorSignal.asReadonly();syncState=this.syncStateSignal.asReadonly();latestError=this.latestErrorSignal.asReadonly();evaluations=this.evaluationsSignal.asReadonly();elites=this.elitesSignal.asReadonly();eliteHistory=this.eliteHistorySignal.asReadonly();searchSpace=this.searchSpaceSignal.asReadonly();artifacts=this.artifactsSignal.asReadonly();alerts=this.alertsSignal.asReadonly();lastUpdated=this.lastUpdatedSignal.asReadonly();viewMode=ne(()=>{let n=this.statusSignal();return n?n.mode===`DISABLED`?`standard`:n.role===`WORKER`?`worker`:`tuning`:this.syncStateSignal()===`retrying`?`unavailable`:`detecting`});connectionLabel=ne(()=>{switch(this.syncStateSignal()){case`detecting`:return`Detecting run mode`;case`synchronizing`:return`Synchronizing`;case`live`:return`Live`;case`retrying`:return`Reconnecting`}});budgetPercent=ne(()=>{let n=this.statusSignal()?.budget;return!n||n.maximum<=0?0:Math.min(100,n.used/n.maximum*100)});evaluationCostChart=ne(()=>Uo.evaluationCost(this.evaluationsSignal()));evaluationActivityChart=ne(()=>Uo.evaluationActivity(this.evaluationsSignal()));eliteRankChart=ne(()=>Uo.eliteRanks(this.eliteHistorySignal()));instanceSummaries=ne(()=>Uo.instanceSummaries(this.evaluationsSignal()));recentEvaluations=ne(()=>this.evaluationsSignal().slice(-100).reverse());unseenAlertCount=ne(()=>{let n=0;for(let e of this.alertsSignal())e.revision===null?this.runFailureSeenSignal()||n++:e.revision>this.seenAlertRevisionSignal()&&n++;return n});constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.clearPollTimer()})}start(){this.started||(this.started=!0,this.schedulePoll(0))}retryNow(){this.clearPollTimer(),this.schedulePoll(0)}markAlertsSeen(){this.seenAlertRevisionSignal.set(this.evaluationRevision),this.statusSignal()?.failure&&this.runFailureSeenSignal.set(!0),this.persistSeenAlerts()}async loadCandidate(n){let e=this.candidateCache.get(n);if(e)return e;let i=this.currentRunId,a=await this.client.getCandidate(n);if(i!==this.currentRunId)throw new Error(`Candidate belongs to an obsolete autoconfig run`);return(this.statusSignal()?.state===`COMPLETED`||this.statusSignal()?.state===`FAILED`)&&this.candidateCache.set(n,a),a}selectComponentScope(n){if(n===this.componentScopeSignal())return;this.componentScopeSignal.set(n),this.componentUsageSignal.set(null),this.componentMarker=null;let e=this.statusSignal();e&&this.synchronizeComponentUsage(e)}loadComponentCandidates(n){return this.client.getComponentCandidates(n)}refreshComponentUsage(){this.componentMarker=null;let n=this.statusSignal();n&&this.synchronizeComponentUsage(n)}async refreshArtifacts(){let n=this.statusSignal();if(!n?.runId||n.role!==`COORDINATOR`){this.artifactsSignal.set(null);return}try{this.artifactsSignal.set(await this.client.getArtifacts()),this.lastArtifactFetch=Date.now()}catch(e){if(this.isNotFound(e)){this.artifactsSignal.set({runId:n.runId,artifacts:[]}),this.lastArtifactFetch=Date.now();return}throw e}}artifactUrl(n){return this.client.artifactUrl(n)}schedulePoll(n){this.destroyed||(this.pollTimer=setTimeout(()=>{this.poll()},n))}async poll(){this.pollTimer=null,this.syncStateSignal.set(`synchronizing`);try{let n=await this.client.getStatus();this.adoptStatus(n),n.mode!==`DISABLED`&&n.role===`COORDINATOR`&&n.runId&&await this.synchronizeRun(n),this.latestErrorSignal.set(null),this.lastUpdatedSignal.set(new Date),this.syncStateSignal.set(`live`)}catch(n){this.latestErrorSignal.set(this.errorMessage(n)),this.syncStateSignal.set(`retrying`)}finally{let n=this.statusSignal(),e=n?.state===`PREPARING`||n?.state===`RUNNING`;this.schedulePoll(e?MP:TP)}}adoptStatus(n){n.runId!==this.currentRunId?this.resetRun(n.runId):n.latestEvaluationRevision<this.evaluationRevision&&this.resetRun(n.runId),this.statusSignal.set(n),this.publishAlerts()}async synchronizeRun(n){await this.synchronizeEvaluations(n),await this.synchronizeElites(n),await this.synchronizeSearchSpace(n),await this.synchronizeComponentUsage(n),await this.synchronizeArtifacts(n)}async synchronizeEvaluations(n){let e=!1;for(;this.evaluationRevision<n.latestEvaluationRevision;){let i=await this.client.getEvaluationChanges(this.evaluationRevision,kP);if(i.runId!==n.runId)throw new Error(`Evaluation changes belong to a different autoconfig run`);for(let a of i.changes)this.evaluationMap.set(a.evaluation.id,{revision:a.revision,evaluation:a.evaluation}),e=!0;if(i.nextRevision<=this.evaluationRevision)break;this.evaluationRevision=i.nextRevision}if(e){let i=[...this.evaluationMap.values()].sort((a,o)=>a.evaluation.id-o.evaluation.id).map(a=>a.evaluation);this.evaluationsSignal.set(i),this.publishAlerts()}}async synchronizeElites(n){let e=`${n.irace.iteration??``}|${n.irace.updatedAt??``}|${n.irace.finalSnapshot}`;if(e===this.eliteMarker)return;let[i,a]=await Promise.all([this.client.getElites(),this.client.getEliteHistory()]);if(i.runId!==n.runId||a.runId!==n.runId)throw new Error(`Elite snapshots belong to a different autoconfig run`);this.elitesSignal.set(i),this.eliteHistorySignal.set(a),this.eliteMarker=e}async synchronizeSearchSpace(n){if(!(n.mode!==`AUTOCONFIG`||n.generatedParameterCount<=0||this.searchSpaceLoaded))try{this.searchSpaceSignal.set(await this.client.getSearchSpace()),this.searchSpaceLoaded=!0}catch(e){if(!this.isNotFound(e))throw e}}async synchronizeComponentUsage(n){if(n.mode!==`AUTOCONFIG`||n.role!==`COORDINATOR`||!n.runId)return;let e=this.componentScopeSignal(),i=`${n.runId}|${e}|${n.latestEvaluationRevision}|${n.irace.updatedAt??``}|${n.irace.finalSnapshot}`;if(i===this.componentMarker)return;let a=++this.componentRequest;try{let o=await this.client.getComponentUsage(e);if(this.destroyed||a!==this.componentRequest||e!==this.componentScopeSignal())return;if(o.runId!==this.currentRunId||o.scope!==e)throw new Error(`Component usage belongs to a different run or scope`);this.componentUsageSignal.set(o),this.componentMarker=i,this.componentErrorSignal.set(null)}catch(o){a===this.componentRequest&&!this.destroyed&&this.componentErrorSignal.set(this.errorMessage(o))}}async synchronizeArtifacts(n){let e=Date.now();n.phase===this.lastArtifactPhase&&e-this.lastArtifactFetch<NP||(await this.refreshArtifacts(),this.lastArtifactPhase=n.phase)}publishAlerts(){let n=[],e=this.statusSignal();e?.failure&&n.push({key:`run:${e.runId}:failure`,revision:null,severity:`critical`,title:`Tuning run failed`,message:e.failure.message??e.failure.type,timestamp:e.finishedAt,evaluationId:null});for(let i of this.evaluationMap.values()){let a=i.evaluation;if(a.state!==`FAILED`&&a.state!==`REJECTED`&&!a.slow)continue;let o=a.state===`FAILED`,r=a.state===`REJECTED`,s=o?`Evaluation failed`:r?`Evaluation rejected`:`Slow evaluation`,c=a.message??a.reasonCode??a.instanceName;n.push({key:`evaluation:${a.id}`,revision:i.revision,severity:o?`critical`:`warning`,title:s,message:a.slow&&r?`${c} \xB7 also slow`:c,timestamp:a.finishedAt,evaluationId:a.id})}n.sort((i,a)=>i.revision===null?-1:a.revision===null?1:a.revision-i.revision),this.alertsSignal.set(n)}resetRun(n){this.currentRunId=n,this.evaluationRevision=0,this.evaluationMap.clear(),this.candidateCache.clear(),this.componentRequest++,this.componentUsageSignal.set(null),this.componentScopeSignal.set(`ALL`),this.componentErrorSignal.set(null),this.componentMarker=null,this.evaluationsSignal.set([]),this.elitesSignal.set(null),this.eliteHistorySignal.set(null),this.searchSpaceSignal.set(null),this.artifactsSignal.set(null),this.alertsSignal.set([]),this.eliteMarker=null,this.searchSpaceLoaded=!1,this.lastArtifactFetch=0,this.lastArtifactPhase=null,this.restoreSeenAlerts(n)}restoreSeenAlerts(n){if(this.seenAlertRevisionSignal.set(0),this.runFailureSeenSignal.set(!1),!!n)try{let e=localStorage.getItem(Vb);if(!e)return;let i=JSON.parse(e);i.runId===n&&(this.seenAlertRevisionSignal.set(typeof i.revision==`number`?i.revision:0),this.runFailureSeenSignal.set(i.runFailureSeen===!0))}catch{localStorage.removeItem(Vb)}}persistSeenAlerts(){if(!this.currentRunId)return;let n={runId:this.currentRunId,revision:this.seenAlertRevisionSignal(),runFailureSeen:this.runFailureSeenSignal()};localStorage.setItem(Vb,JSON.stringify(n))}clearPollTimer(){this.pollTimer&&(clearTimeout(this.pollTimer),this.pollTimer=null)}isNotFound(n){return n instanceof Ei&&n.status===404}errorMessage(n){return n instanceof Ei?`Backend request failed (${n.status||`network error`})`:n instanceof Error?n.message:String(n)}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var AP=(t,n)=>n[0];function OP(t,n){if(t&1&&(l(0,`dt`),u(1),d(),l(2,`dd`),u(3),d()),t&2){let e=n.$implicit;m(),w(e[0]),m(2),w(e[1])}}function RP(t,n){t&1&&(l(0,`dd`),u(1,`No parameters were reported.`),d())}function PP(t,n){if(t&1&&(l(0,`h3`),u(1,`Decoded algorithm`),d(),l(2,`pre`),u(3),U(4,`json`),d()),t&2){let e=_();m(3),w(ye(4,1,e.algorithm))}}function FP(t,n){if(t&1&&(l(0,`p`,1),u(1),d()),t&2){let e=_();m(),w(e.decodeError)}}function LP(t,n){if(t&1&&(l(0,`section`,4)(1,`span`)(2,`strong`),u(3),d(),u(4,` succeeded`),d(),l(5,`span`)(6,`strong`),u(7),d(),u(8,` running`),d(),l(9,`span`)(10,`strong`),u(11),d(),u(12,` rejected`),d(),l(13,`span`)(14,`strong`),u(15),d(),u(16,` failed`),d(),l(17,`span`)(18,`strong`),u(19),d(),u(20,` slow`),d()(),l(21,`h3`),u(22,`Parameters`),d(),l(23,`dl`),ue(24,OP,4,2,null,null,AP,!1,RP,2,0,`dd`),d(),D(27,PP,5,3),D(28,FP,2,1,`p`,1)),t&2){let e=n,i=_();m(3),w(e.evaluations.succeeded),m(4),w(e.evaluations.running),m(4),w(e.evaluations.rejected),m(4),w(e.evaluations.failed),m(4),w(e.evaluations.slow),m(5),me(i.parameterEntries(e)),m(3),I(e.algorithm?27:-1),m(),I(e.decodeError?28:-1)}}function VP(t,n){if(t&1&&(l(0,`p`,1),u(1),d()),t&2){let e=_();m(),w(e.error())}}function BP(t,n){t&1&&(l(0,`p`),u(1,`Loading configuration…`),d())}var ms=class t{store=f(ja);configurationId=f(Zc);candidate=T(null);error=T(null);ngOnInit(){this.store.loadCandidate(this.configurationId).then(n=>this.candidate.set(n)).catch(n=>this.error.set(n instanceof Error?n.message:String(n)))}parameterEntries(n){return Object.entries(n.parameters)}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-candidate-dialog`]],decls:9,vars:2,consts:[[`mat-dialog-title`,``],[`role`,`alert`,1,`error`],[`align`,`end`],[`mat-button`,``,`mat-dialog-close`,``,`type`,`button`],[`aria-label`,`Candidate evaluation counts`,1,`counts`]],template:function(e,i){if(e&1&&(l(0,`h2`,0),u(1),d(),l(2,`mat-dialog-content`),D(3,LP,29,8)(4,VP,2,1,`p`,1)(5,BP,2,0,`p`),d(),l(6,`mat-dialog-actions`,2)(7,`button`,3),u(8,`Close`),d()()),e&2){let a;m(),j(`Configuration `,i.configurationId),m(2),I((a=i.candidate())?3:i.error()?4:5,a)}},dependencies:[Pt,en,Im,Sm,wm,Dm,Em,wo],styles:[`mat-dialog-content[_ngcontent-%COMP%]{min-width:min(42rem,80vw)}.counts[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.5rem 1.25rem;margin-bottom:1.25rem}dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(10rem,1fr) minmax(8rem,1fr);gap:.35rem 1rem}dt[_ngcontent-%COMP%]{overflow-wrap:anywhere;color:var(--%NS%mat-sys-on-surface-variant)}dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}pre[_ngcontent-%COMP%]{max-height:22rem;overflow:auto;padding:1rem;border-radius:.5rem;background:var(--%NS%mat-sys-surface-container);white-space:pre-wrap}.error[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}`]})};var jP=()=>[25,50,100];var zP=(t,n)=>n.id;function HP(t,n){t&1&&(l(0,`span`,17),u(1,`slow`),d())}function UP(t,n){if(t&1){let e=q();l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`)(4,`button`,14),C(`click`,function(){let a=N(e).$implicit;return A(_().candidateSelected.emit(a.configurationId))}),u(5),d()(),l(6,`td`,15),u(7),d(),l(8,`td`)(9,`span`,16),u(10),d()(),l(11,`td`),u(12),U(13,`number`),d(),l(14,`td`),u(15),U(16,`number`),D(17,HP,2,0,`span`,17),d(),l(18,`td`),u(19),U(20,`date`),d(),l(21,`td`,18),u(22),d()()}if(t&2){let e=n.$implicit,i=_();Y(`issue`,e.state===`FAILED`||e.state===`REJECTED`),m(2),w(e.id),m(3),j(` `,e.configurationId,` `),m(),S(`title`,e.instanceName),m(),w(e.instanceName),m(2),tt(i.stateClass(e.state)),m(),w(e.state),m(2),w(e.cost===null?`—`:ke(13,14,e.cost,`1.0-5`)),m(3),j(` `,e.timeSeconds===null?`—`:ke(16,17,e.timeSeconds,`1.2-2`)+` s`,` `),m(2),I(e.slow?17:-1),m(2),j(` `,e.finishedAt?ke(20,20,e.finishedAt,`HH:mm:ss`):`Running`,` `),m(3),w(e.message??e.reasonCode??`—`)}}function $P(t,n){t&1&&(l(0,`tr`)(1,`td`,19),u(2,`No evaluations match these filters.`),d()())}var Mm=class t{evaluations=zt.required();forceIssues=zt(!1);candidateSelected=eu();search=T(``);state=T(`ALL`);slowOnly=T(!1);issueOnly=T(!1);pageIndex=T(0);pageSize=T(25);sortColumn=T(`id`);sortAscending=T(!1);filtered=ne(()=>{let n=this.search().trim().toLowerCase(),e=this.state(),i=this.slowOnly(),a=this.issueOnly()||this.forceIssues(),o=[];for(let c of this.evaluations())e!==`ALL`&&c.state!==e||i&&!c.slow||a&&c.state!==`FAILED`&&c.state!==`REJECTED`&&!c.slow||n&&!c.configurationId.toLowerCase().includes(n)&&!c.instanceName.toLowerCase().includes(n)&&!String(c.id).includes(n)||o.push(c);let r=this.sortColumn(),s=this.sortAscending()?1:-1;return o.sort((c,h)=>this.compare(c[r],h[r])*s),o});page=ne(()=>{let n=this.pageIndex()*this.pageSize();return this.filtered().slice(n,n+this.pageSize())});updateSearch(n){this.search.set(n),this.pageIndex.set(0)}updateState(n){this.state.set(n),this.pageIndex.set(0)}updateSlowOnly(n){this.slowOnly.set(n),this.pageIndex.set(0)}updateIssueOnly(n){this.issueOnly.set(n),this.pageIndex.set(0)}updatePage(n){this.pageIndex.set(n.pageIndex),this.pageSize.set(n.pageSize)}sortBy(n){this.sortColumn()===n?this.sortAscending.update(e=>!e):(this.sortColumn.set(n),this.sortAscending.set(!0)),this.pageIndex.set(0)}sortIndicator(n){return this.sortColumn()!==n?``:this.sortAscending()?` ↑`:` ↓`}stateClass(n){return`state-${n.toLowerCase()}`}compare(n,e){return n===e?0:n===null?1:e===null?-1:typeof n==`number`&&typeof e==`number`?n-e:String(n).localeCompare(String(e),void 0,{numeric:!0})}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-evaluation-table`]],inputs:{evaluations:[1,`evaluations`],forceIssues:[1,`forceIssues`]},outputs:{candidateSelected:`candidateSelected`},decls:54,vars:17,consts:[[`aria-label`,`Evaluation filters`,1,`filters`],[`appearance`,`outline`,`subscriptSizing`,`dynamic`],[`matInput`,``,`type`,`search`,`placeholder`,`ID, configuration, or instance`,3,`ngModelChange`,`ngModel`],[3,`ngModelChange`,`ngModel`],[`value`,`ALL`],[`value`,`RUNNING`],[`value`,`SUCCEEDED`],[`value`,`REJECTED`],[`value`,`FAILED`],[3,`ngModelChange`,`ngModel`,`disabled`],[1,`table-scroll`],[`type`,`button`,3,`click`],[3,`issue`],[`aria-label`,`Evaluation pages`,3,`page`,`length`,`pageIndex`,`pageSize`,`pageSizeOptions`],[`type`,`button`,1,`candidate-link`,3,`click`],[1,`instance`,3,`title`],[1,`state`],[1,`slow`],[1,`details`],[`colspan`,`8`,1,`empty`]],template:function(e,i){e&1&&(l(0,`section`,0)(1,`mat-form-field`,1)(2,`mat-label`),u(3,`Search`),d(),l(4,`input`,2),Er(),C(`ngModelChange`,function(o){return i.updateSearch(o)}),d()(),l(5,`mat-form-field`,1)(6,`mat-label`),u(7,`State`),d(),l(8,`mat-select`,3),Er(),C(`ngModelChange`,function(o){return i.updateState(o)}),l(9,`mat-option`,4),u(10,`All states`),d(),l(11,`mat-option`,5),u(12,`Running`),d(),l(13,`mat-option`,6),u(14,`Succeeded`),d(),l(15,`mat-option`,7),u(16,`Rejected`),d(),l(17,`mat-option`,8),u(18,`Failed`),d()()(),l(19,`mat-checkbox`,3),Er(),C(`ngModelChange`,function(o){return i.updateSlowOnly(o)}),u(20,` Slow only `),d(),l(21,`mat-checkbox`,9),Er(),C(`ngModelChange`,function(o){return i.updateIssueOnly(o)}),u(22,` Issues only `),d()(),l(23,`div`,10)(24,`table`)(25,`thead`)(26,`tr`)(27,`th`)(28,`button`,11),C(`click`,function(){return i.sortBy(`id`)}),u(29),d()(),l(30,`th`)(31,`button`,11),C(`click`,function(){return i.sortBy(`configurationId`)}),u(32),d()(),l(33,`th`)(34,`button`,11),C(`click`,function(){return i.sortBy(`instanceName`)}),u(35),d()(),l(36,`th`)(37,`button`,11),C(`click`,function(){return i.sortBy(`state`)}),u(38),d()(),l(39,`th`)(40,`button`,11),C(`click`,function(){return i.sortBy(`cost`)}),u(41),d()(),l(42,`th`)(43,`button`,11),C(`click`,function(){return i.sortBy(`timeSeconds`)}),u(44),d()(),l(45,`th`),u(46,`Finished`),d(),l(47,`th`),u(48,`Details`),d()()(),l(49,`tbody`),ue(50,UP,23,23,`tr`,12,zP,!1,$P,3,0,`tr`),d()()(),l(53,`mat-paginator`,13),C(`page`,function(o){return i.updatePage(o)}),d()),e&2&&(m(4),S(`ngModel`,i.search()),Dr(),m(4),S(`ngModel`,i.state()),Dr(),m(11),S(`ngModel`,i.slowOnly()),Dr(),m(2),S(`ngModel`,i.issueOnly()||i.forceIssues())(`disabled`,i.forceIssues()),Dr(),m(8),j(`ID`,i.sortIndicator(`id`)),m(3),j(` Configuration`,i.sortIndicator(`configurationId`),` `),m(3),j(` Instance`,i.sortIndicator(`instanceName`),` `),m(3),j(`State`,i.sortIndicator(`state`)),m(3),j(`Cost`,i.sortIndicator(`cost`)),m(3),j(` Time`,i.sortIndicator(`timeSeconds`),` `),m(6),me(i.page()),m(3),S(`length`,i.filtered().length)(`pageIndex`,i.pageIndex())(`pageSize`,i.pageSize())(`pageSizeOptions`,rt(16,jP)))},dependencies:[FS,qu,RS,ib,Pt,tm,jc,di,Ti,na,Xr,Qr,mm,Wc,Pa,Ra,Un,Xi,En],styles:[`[_nghost-%COMP%]{display:block;min-width:0}.filters[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem 1rem;align-items:center;margin-bottom:1rem}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]:first-child{flex:1 1 18rem}.table-scroll[_ngcontent-%COMP%]{overflow:auto}table[_ngcontent-%COMP%]{width:100%;min-width:70rem;border-collapse:collapse;font:var(--%NS%mat-sys-body-small)}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.65rem .55rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left;vertical-align:middle}th[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-weight:650}th[_ngcontent-%COMP%]   button[_ngcontent-%COMP%], .candidate-link[_ngcontent-%COMP%]{padding:0;border:0;color:inherit;background:transparent;cursor:pointer;font-weight:inherit}.candidate-link[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-primary);text-decoration:underline;text-underline-offset:.15rem}.instance[_ngcontent-%COMP%], .details[_ngcontent-%COMP%]{max-width:18rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.state[_ngcontent-%COMP%]{display:inline-block;padding:.15rem .45rem;border-radius:999px;background:var(--%NS%mat-sys-surface-container-high);font:var(--%NS%mat-sys-label-small)}.state-succeeded[_ngcontent-%COMP%]{color:#08743a;background:#d9f5e4}.state-rejected[_ngcontent-%COMP%], .slow[_ngcontent-%COMP%]{color:#835100;background:#fff0c6}.state-failed[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-error-container);background:var(--%NS%mat-sys-error-container)}.slow[_ngcontent-%COMP%]{margin-left:.25rem;padding:.1rem .3rem;border-radius:.25rem;font:var(--%NS%mat-sys-label-small)}tr.issue[_ngcontent-%COMP%]{background:color-mix(in srgb,var(--%NS%mat-sys-error-container) 22%,transparent)}.empty[_ngcontent-%COMP%]{padding:2.5rem;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}mat-paginator[_ngcontent-%COMP%]{background:transparent}`]})};var Jw=(t,n)=>n.name;function GP(t,n){if(t&1&&(l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),d()()),t&2){let e=n.$implicit,i=_(2);m(2),w(e.name),m(2),w(e.kind),m(2),w(i.domain(e))}}function WP(t,n){if(t&1&&(l(0,`details`)(1,`summary`),u(2),d(),l(3,`table`)(4,`thead`)(5,`tr`)(6,`th`),u(7,`Name`),d(),l(8,`th`),u(9,`Kind`),d(),l(10,`th`),u(11,`Domain`),d()()(),l(12,`tbody`),ue(13,GP,7,3,`tr`,null,Jw),d()()()),t&2){let e=n.$implicit;m(2),Ae(``,e.name,` · `,e.parameters.length,` parameters`),m(11),me(e.parameters)}}var Tm=class t{data=f(Zc);domain(n){return n.choices.length>0?n.choices.join(`, `):n.values.length>0?n.values.join(`, `):n.minimum!==null||n.maximum!==null?`${String(n.minimum)} \u2013 ${String(n.maximum)}`:`Provided by Mork`}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-search-space-dialog`]],decls:30,vars:5,consts:[[`mat-dialog-title`,``],[1,`summary`],[1,`components`],[`align`,`end`],[`mat-button`,``,`mat-dialog-close`,``,`type`,`button`]],template:function(e,i){e&1&&(l(0,`h2`,0),u(1,`Generated search space`),d(),l(2,`mat-dialog-content`)(3,`section`,1)(4,`span`)(5,`strong`),u(6),d(),u(7,` roots`),d(),l(8,`span`)(9,`strong`),u(10),d(),u(11,` components`),d(),l(12,`span`)(13,`strong`),u(14),d(),u(15,` IRACE parameters`),d(),l(16,`span`)(17,`strong`),u(18),d(),u(19,` constraints`),d()(),l(20,`p`)(21,`strong`),u(22,`Root choices:`),d(),u(23),d(),l(24,`section`,2),ue(25,WP,15,2,`details`,null,Jw),d()(),l(27,`mat-dialog-actions`,3)(28,`button`,4),u(29,`Close`),d()()),e&2&&(m(6),w(i.data.summary.rootCount),m(4),w(i.data.summary.componentCount),m(4),w(i.data.summary.generatedIraceParameterCount),m(4),w(i.data.summary.generatedForbiddenConstraintCount),m(5),j(` `,i.data.roots.join(`, `)),m(2),me(i.data.components))},dependencies:[Pt,en,Im,Sm,wm,Dm,Em],styles:[`mat-dialog-content[_ngcontent-%COMP%]{width:min(60rem,84vw)}.summary[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem 2rem;margin-bottom:1rem}.components[_ngcontent-%COMP%]{display:grid;gap:.5rem}details[_ngcontent-%COMP%]{padding:.75rem;border:1px solid var(--%NS%mat-sys-outline-variant);border-radius:.5rem}summary[_ngcontent-%COMP%]{cursor:pointer;font-weight:650}table[_ngcontent-%COMP%]{width:100%;margin-top:.75rem;border-collapse:collapse}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.45rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left;overflow-wrap:anywhere}`]})};var eE=new k(`MAT_BUTTON_TOGGLE_DEFAULT_OPTIONS`,{providedIn:`root`,factory:()=>({hideSingleSelectionIndicator:!1,hideMultipleSelectionIndicator:!1,disabledInteractive:!1})});var tE=new k(`MatButtonToggleGroup`);var qP={provide:Wr,useExisting:Ht(()=>Bb),multi:!0};var Nm=class{source;value;constructor(n,e){this.source=n,this.value=e}};var Bb=(()=>{class t{_changeDetector=f(Fe);_dir=f(gt,{optional:!0});_multiple=!1;_disabled=!1;_disabledInteractive=!1;_selectionModel;_rawValue;_controlValueAccessorChangeFn=()=>{};_onTouched=()=>{};_buttonToggles;appearance;get name(){return this._name}set name(e){this._name=e,this._markButtonsForCheck()}_name=f(Be).getId(`mat-button-toggle-group-`);vertical=!1;get value(){let e=this._selectionModel?this._selectionModel.selected:[];return this.multiple?e.map(i=>i.value):e[0]?e[0].value:void 0}set value(e){this._setSelectionByValue(e),this.valueChange.emit(this.value)}valueChange=new ee;get selected(){let e=this._selectionModel?this._selectionModel.selected:[];return this.multiple?e:e[0]||null}get multiple(){return this._multiple}set multiple(e){this._multiple=e,this._markButtonsForCheck()}get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._markButtonsForCheck()}get disabledInteractive(){return this._disabledInteractive}set disabledInteractive(e){this._disabledInteractive=e,this._markButtonsForCheck()}get dir(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}change=new ee;get hideSingleSelectionIndicator(){return this._hideSingleSelectionIndicator}set hideSingleSelectionIndicator(e){this._hideSingleSelectionIndicator=e,this._markButtonsForCheck()}_hideSingleSelectionIndicator;get hideMultipleSelectionIndicator(){return this._hideMultipleSelectionIndicator}set hideMultipleSelectionIndicator(e){this._hideMultipleSelectionIndicator=e,this._markButtonsForCheck()}_hideMultipleSelectionIndicator;constructor(){let e=f(eE,{optional:!0});this.appearance=e&&e.appearance?e.appearance:`standard`,this._hideSingleSelectionIndicator=e?.hideSingleSelectionIndicator??!1,this._hideMultipleSelectionIndicator=e?.hideMultipleSelectionIndicator??!1}ngOnInit(){this._selectionModel=new jo(this.multiple,void 0,!1)}ngAfterContentInit(){this._selectionModel.select(...this._buttonToggles.filter(e=>e.checked)),this.multiple||this._initializeTabIndex()}writeValue(e){this.value=e,this._changeDetector.markForCheck()}registerOnChange(e){this._controlValueAccessorChangeFn=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e}_keydown(e){if(this.multiple||this.disabled||xt(e))return;let a=e.target.id,o=this._buttonToggles.toArray().findIndex(s=>s.buttonId===a),r=null;switch(e.keyCode){case 32:case 13:r=this._buttonToggles.get(o)||null;break;case 38:r=this._getNextButton(o,-1);break;case 37:r=this._getNextButton(o,this.dir===`ltr`?-1:1);break;case 40:r=this._getNextButton(o,1);break;case 39:r=this._getNextButton(o,this.dir===`ltr`?1:-1);break;default:return}r&&(e.preventDefault(),r._onButtonClick(),r.focus())}_emitChangeEvent(e){let i=new Nm(e,this.value);this._rawValue=i.value,this._controlValueAccessorChangeFn(i.value),this.change.emit(i)}_syncButtonToggle(e,i,a=!1,o=!1){!this.multiple&&this.selected&&!e.checked&&(this.selected.checked=!1),this._selectionModel?i?this._selectionModel.select(e):this._selectionModel.deselect(e):o=!0,o?Promise.resolve().then(()=>this._updateModelValue(e,a)):this._updateModelValue(e,a)}_isSelected(e){return this._selectionModel&&this._selectionModel.isSelected(e)}_isPrechecked(e){return typeof this._rawValue>`u`?!1:this.multiple&&Array.isArray(this._rawValue)?this._rawValue.some(i=>e.value!=null&&i===e.value):e.value===this._rawValue}_initializeTabIndex(){if(this._buttonToggles.forEach(e=>{e.tabIndex=-1}),this.selected)this.selected.tabIndex=0;else for(let e=0;e<this._buttonToggles.length;e++){let i=this._buttonToggles.get(e);if(!i.disabled){i.tabIndex=0;break}}}_getNextButton(e,i){let a=this._buttonToggles;for(let o=1;o<=a.length;o++){let r=(e+i*o+a.length)%a.length,s=a.get(r);if(s&&!s.disabled)return s}return null}_setSelectionByValue(e){if(this._rawValue=e,!this._buttonToggles)return;let i=this._buttonToggles.toArray();if(this.multiple&&e?(this._clearSelection(),e.forEach(a=>this._selectValue(a,i))):(this._clearSelection(),this._selectValue(e,i)),!this.multiple&&i.every(a=>a.tabIndex===-1)){for(let a of i)if(!a.disabled){a.tabIndex=0;break}}}_clearSelection(){this._selectionModel.clear(),this._buttonToggles.forEach(e=>{e.checked=!1,this.multiple||(e.tabIndex=-1)})}_selectValue(e,i){for(let a of i)if(a.value===e){a.checked=!0,this._selectionModel.select(a),this.multiple||(a.tabIndex=0);break}}_updateModelValue(e,i){i&&this._emitChangeEvent(e),this.valueChange.emit(this.value)}_markButtonsForCheck(){this._buttonToggles?.forEach(e=>e._markForCheck())}static ɵfac=function(i){return new(i||t)};static ɵdir=K({type:t,selectors:[[`mat-button-toggle-group`]],contentQueries:function(i,a,o){if(i&1&&jt(o,Am,5),i&2){let r;Z(r=J())&&(a._buttonToggles=r)}},hostAttrs:[1,`mat-button-toggle-group`],hostVars:6,hostBindings:function(i,a){i&1&&C(`keydown`,function(r){return a._keydown(r)}),i&2&&(te(`role`,a.multiple?`group`:`radiogroup`)(`aria-disabled`,a.disabled),Y(`mat-button-toggle-vertical`,a.vertical)(`mat-button-toggle-group-appearance-standard`,a.appearance===`standard`))},inputs:{appearance:`appearance`,name:`name`,vertical:[2,`vertical`,`vertical`,V],value:`value`,multiple:[2,`multiple`,`multiple`,V],disabled:[2,`disabled`,`disabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],hideSingleSelectionIndicator:[2,`hideSingleSelectionIndicator`,`hideSingleSelectionIndicator`,V],hideMultipleSelectionIndicator:[2,`hideMultipleSelectionIndicator`,`hideMultipleSelectionIndicator`,V]},outputs:{valueChange:`valueChange`,change:`change`},exportAs:[`matButtonToggleGroup`],features:[Ge([qP,{provide:tE,useExisting:t}])]})}return t})();var Am=(()=>{class t{_changeDetectorRef=f(Fe);_elementRef=f(W);_focusMonitor=f(Jt);_idGenerator=f(Be);_animationDisabled=Ue();_checked=!1;ariaLabel;ariaLabelledby=null;_buttonElement;buttonToggleGroup;get buttonId(){return`${this.id}-button`}id;name;value;get tabIndex(){return this._tabIndex()}set tabIndex(e){this._tabIndex.set(e)}_tabIndex;disableRipple=!1;get appearance(){return this.buttonToggleGroup?this.buttonToggleGroup.appearance:this._appearance}set appearance(e){this._appearance=e}_appearance;get checked(){return this.buttonToggleGroup?this.buttonToggleGroup._isSelected(this):this._checked}set checked(e){e!==this._checked&&(this._checked=e,this.buttonToggleGroup&&this.buttonToggleGroup._syncButtonToggle(this,this._checked),this._changeDetectorRef.markForCheck())}get disabled(){return this._disabled||this.buttonToggleGroup&&this.buttonToggleGroup.disabled}set disabled(e){this._disabled=e}_disabled=!1;get disabledInteractive(){return this._disabledInteractive||this.buttonToggleGroup!==null&&this.buttonToggleGroup.disabledInteractive}set disabledInteractive(e){this._disabledInteractive=e}_disabledInteractive;change=new ee;constructor(){f(Je).load(dn);let e=f(tE,{optional:!0}),i=f(new Ki(`tabindex`),{optional:!0})||``,a=f(eE,{optional:!0});this._tabIndex=T(parseInt(i)||0),this.buttonToggleGroup=e,this._appearance=a&&a.appearance?a.appearance:`standard`,this._disabledInteractive=a?.disabledInteractive??!1}ngOnInit(){let e=this.buttonToggleGroup;this.id=this.id||this._idGenerator.getId(`mat-button-toggle-`),e&&(e._isPrechecked(this)?this.checked=!0:e._isSelected(this)!==this._checked&&e._syncButtonToggle(this,this._checked))}ngAfterViewInit(){this._animationDisabled||this._elementRef.nativeElement.classList.add(`mat-button-toggle-animations-enabled`),this._focusMonitor.monitor(this._elementRef,!0)}ngOnDestroy(){let e=this.buttonToggleGroup;this._focusMonitor.stopMonitoring(this._elementRef),e&&e._isSelected(this)&&e._syncButtonToggle(this,!1,!1,!0)}focus(e){this._buttonElement.nativeElement.focus(e)}_onButtonClick(){if(this.disabled)return;let e=this.isSingleSelector()?!0:!this._checked;if(e!==this._checked&&(this._checked=e,this.buttonToggleGroup&&(this.buttonToggleGroup._syncButtonToggle(this,this._checked,!0),this.buttonToggleGroup._onTouched())),this.isSingleSelector()){let i=this.buttonToggleGroup._buttonToggles.find(a=>a.tabIndex===0);i&&(i.tabIndex=-1),this.tabIndex=0}this.change.emit(new Nm(this,this.value))}_markForCheck(){this._changeDetectorRef.markForCheck()}_getButtonName(){return this.isSingleSelector()?this.buttonToggleGroup.name:this.name||null}isSingleSelector(){return this.buttonToggleGroup&&!this.buttonToggleGroup.multiple}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`button`],i=[`*`];function a(o,r){if(o&1&&(l(0,`div`,2),H(1,`mat-pseudo-checkbox`,6),d()),o&2){let s=_();m(),S(`disabled`,s.disabled)}}return B({type:t,selectors:[[`mat-button-toggle`]],viewQuery:function(r,s){if(r&1&&Ze(e,5),r&2){let c;Z(c=J())&&(s._buttonElement=c.first)}},hostAttrs:[`role`,`presentation`,1,`mat-button-toggle`],hostVars:14,hostBindings:function(r,s){r&1&&C(`focus`,function(){return s.focus()}),r&2&&(te(`aria-label`,null)(`aria-labelledby`,null)(`id`,s.id)(`name`,null),Y(`mat-button-toggle-standalone`,!s.buttonToggleGroup)(`mat-button-toggle-checked`,s.checked)(`mat-button-toggle-disabled`,s.disabled)(`mat-button-toggle-disabled-interactive`,s.disabledInteractive)(`mat-button-toggle-appearance-standard`,s.appearance===`standard`))},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],id:`id`,name:`name`,value:`value`,tabIndex:`tabIndex`,disableRipple:[2,`disableRipple`,`disableRipple`,V],appearance:`appearance`,checked:[2,`checked`,`checked`,V],disabled:[2,`disabled`,`disabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V]},outputs:{change:`change`},exportAs:[`matButtonToggle`],ngContentSelectors:i,decls:7,vars:13,consts:[[`button`,``],[`type`,`button`,1,`mat-button-toggle-button`,`mat-focus-indicator`,3,`click`,`id`,`disabled`],[1,`mat-button-toggle-checkbox-wrapper`],[1,`mat-button-toggle-label-content`],[1,`mat-button-toggle-focus-overlay`],[`matRipple`,``,1,`mat-button-toggle-ripple`,3,`matRippleTrigger`,`matRippleDisabled`],[`state`,`checked`,`aria-hidden`,`true`,`appearance`,`minimal`,3,`disabled`]],template:function(r,s){if(r&1&&(Pe(),l(0,`button`,1,0),C(`click`,function(){return s._onButtonClick()}),D(2,a,2,1,`div`,2),l(3,`span`,3),he(4),d()(),H(5,`span`,4)(6,`span`,5)),r&2){let c=ft(1);S(`id`,s.buttonId)(`disabled`,s.disabled&&!s.disabledInteractive||null),te(`role`,s.isSingleSelector()?`radio`:`button`)(`tabindex`,s.disabled&&!s.disabledInteractive?-1:s.tabIndex)(`aria-pressed`,s.isSingleSelector()?null:s.checked)(`aria-checked`,s.isSingleSelector()?s.checked:null)(`name`,s._getButtonName())(`aria-label`,s.ariaLabel)(`aria-labelledby`,s.ariaLabelledby)(`aria-disabled`,s.disabled&&s.disabledInteractive?`true`:null),m(2),I(s.buttonToggleGroup&&(!s.buttonToggleGroup.multiple&&!s.buttonToggleGroup.hideSingleSelectionIndicator||s.buttonToggleGroup.multiple&&!s.buttonToggleGroup.hideMultipleSelectionIndicator)?2:-1),m(4),S(`matRippleTrigger`,c)(`matRippleDisabled`,s.disableRipple||s.disabled)}},dependencies:[ci,um],styles:[`.mat-button-toggle-standalone,
.mat-button-toggle-group {
  position: relative;
  display: inline-flex;
  flex-direction: row;
  white-space: nowrap;
  overflow: hidden;
  -webkit-tap-highlight-color: transparent;
  border-radius: var(--%NS%mat-button-toggle-legacy-shape);
  transform: translateZ(0);
}
.mat-button-toggle-standalone:not([class*=mat-elevation-z]),
.mat-button-toggle-group:not([class*=mat-elevation-z]) {
  box-shadow: 0px 3px 1px -2px rgba(0, 0, 0, 0.2), 0px 2px 2px 0px rgba(0, 0, 0, 0.14), 0px 1px 5px 0px rgba(0, 0, 0, 0.12);
}
@media (forced-colors: active) {
  .mat-button-toggle-standalone,
  .mat-button-toggle-group {
    outline: solid 1px;
  }
}

.mat-button-toggle-standalone.mat-button-toggle-appearance-standard,
.mat-button-toggle-group-appearance-standard {
  border-radius: var(--%NS%mat-button-toggle-shape, var(--%NS%mat-sys-corner-extra-large));
  border: solid 1px var(--%NS%mat-button-toggle-divider-color, var(--%NS%mat-sys-outline));
}
.mat-button-toggle-standalone.mat-button-toggle-appearance-standard .mat-pseudo-checkbox,
.mat-button-toggle-group-appearance-standard .mat-pseudo-checkbox {
  --%NS%mat-pseudo-checkbox-minimal-selected-checkmark-color: var(--%NS%mat-button-toggle-selected-state-text-color, var(--%NS%mat-sys-on-secondary-container));
}
.mat-button-toggle-standalone.mat-button-toggle-appearance-standard:not([class*=mat-elevation-z]),
.mat-button-toggle-group-appearance-standard:not([class*=mat-elevation-z]) {
  box-shadow: none;
}
@media (forced-colors: active) {
  .mat-button-toggle-standalone.mat-button-toggle-appearance-standard,
  .mat-button-toggle-group-appearance-standard {
    outline: 0;
  }
}

.mat-button-toggle-vertical {
  flex-direction: column;
}
.mat-button-toggle-vertical .mat-button-toggle-label-content {
  display: block;
}

.mat-button-toggle {
  white-space: nowrap;
  position: relative;
  color: var(--%NS%mat-button-toggle-legacy-text-color);
  font-family: var(--%NS%mat-button-toggle-legacy-label-text-font);
  font-size: var(--%NS%mat-button-toggle-legacy-label-text-size);
  line-height: var(--%NS%mat-button-toggle-legacy-label-text-line-height);
  font-weight: var(--%NS%mat-button-toggle-legacy-label-text-weight);
  letter-spacing: var(--%NS%mat-button-toggle-legacy-label-text-tracking);
  --%NS%mat-pseudo-checkbox-minimal-selected-checkmark-color: var(--%NS%mat-button-toggle-legacy-selected-state-text-color);
}
.mat-button-toggle.cdk-keyboard-focused .mat-button-toggle-focus-overlay {
  opacity: var(--%NS%mat-button-toggle-legacy-focus-state-layer-opacity);
}
.mat-button-toggle .mat-icon svg {
  vertical-align: top;
}

.mat-button-toggle-checkbox-wrapper {
  display: inline-block;
  justify-content: flex-start;
  align-items: center;
  width: 0;
  height: 18px;
  line-height: 18px;
  overflow: hidden;
  box-sizing: border-box;
  position: absolute;
  top: 50%;
  left: 16px;
  transform: translate3d(0, -50%, 0);
}
[dir=rtl] .mat-button-toggle-checkbox-wrapper {
  left: auto;
  right: 16px;
}
.mat-button-toggle-appearance-standard .mat-button-toggle-checkbox-wrapper {
  left: 12px;
}
[dir=rtl] .mat-button-toggle-appearance-standard .mat-button-toggle-checkbox-wrapper {
  left: auto;
  right: 12px;
}
.mat-button-toggle-checked .mat-button-toggle-checkbox-wrapper {
  width: 18px;
}
.mat-button-toggle-animations-enabled .mat-button-toggle-checkbox-wrapper {
  transition: width 150ms 45ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-button-toggle-vertical .mat-button-toggle-checkbox-wrapper {
  transition: none;
}

.mat-button-toggle-checked {
  color: var(--%NS%mat-button-toggle-legacy-selected-state-text-color);
  background-color: var(--%NS%mat-button-toggle-legacy-selected-state-background-color);
}

.mat-button-toggle-disabled {
  pointer-events: none;
  color: var(--%NS%mat-button-toggle-legacy-disabled-state-text-color);
  background-color: var(--%NS%mat-button-toggle-legacy-disabled-state-background-color);
  --%NS%mat-pseudo-checkbox-minimal-disabled-selected-checkmark-color: var(--%NS%mat-button-toggle-legacy-disabled-state-text-color);
}
.mat-button-toggle-disabled.mat-button-toggle-checked {
  background-color: var(--%NS%mat-button-toggle-legacy-disabled-selected-state-background-color);
}

.mat-button-toggle-disabled-interactive {
  pointer-events: auto;
}

.mat-button-toggle-appearance-standard {
  color: var(--%NS%mat-button-toggle-text-color, var(--%NS%mat-sys-on-surface));
  background-color: var(--%NS%mat-button-toggle-background-color, transparent);
  font-family: var(--%NS%mat-button-toggle-label-text-font, var(--%NS%mat-sys-label-large-font));
  font-size: var(--%NS%mat-button-toggle-label-text-size, var(--%NS%mat-sys-label-large-size));
  line-height: var(--%NS%mat-button-toggle-label-text-line-height, var(--%NS%mat-sys-label-large-line-height));
  font-weight: var(--%NS%mat-button-toggle-label-text-weight, var(--%NS%mat-sys-label-large-weight));
  letter-spacing: var(--%NS%mat-button-toggle-label-text-tracking, var(--%NS%mat-sys-label-large-tracking));
}
.mat-button-toggle-group-appearance-standard .mat-button-toggle-appearance-standard + .mat-button-toggle-appearance-standard {
  border-left: solid 1px var(--%NS%mat-button-toggle-divider-color, var(--%NS%mat-sys-outline));
}
[dir=rtl] .mat-button-toggle-group-appearance-standard .mat-button-toggle-appearance-standard + .mat-button-toggle-appearance-standard {
  border-left: none;
  border-right: solid 1px var(--%NS%mat-button-toggle-divider-color, var(--%NS%mat-sys-outline));
}
.mat-button-toggle-group-appearance-standard.mat-button-toggle-vertical .mat-button-toggle-appearance-standard + .mat-button-toggle-appearance-standard {
  border-left: none;
  border-right: none;
  border-top: solid 1px var(--%NS%mat-button-toggle-divider-color, var(--%NS%mat-sys-outline));
}
.mat-button-toggle-appearance-standard.mat-button-toggle-checked {
  color: var(--%NS%mat-button-toggle-selected-state-text-color, var(--%NS%mat-sys-on-secondary-container));
  background-color: var(--%NS%mat-button-toggle-selected-state-background-color, var(--%NS%mat-sys-secondary-container));
}
.mat-button-toggle-appearance-standard.mat-button-toggle-disabled {
  color: var(--%NS%mat-button-toggle-disabled-state-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
  background-color: var(--%NS%mat-button-toggle-disabled-state-background-color, transparent);
}
.mat-button-toggle-appearance-standard.mat-button-toggle-disabled .mat-pseudo-checkbox {
  --%NS%mat-pseudo-checkbox-minimal-disabled-selected-checkmark-color: var(--%NS%mat-button-toggle-disabled-selected-state-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
}
.mat-button-toggle-appearance-standard.mat-button-toggle-disabled.mat-button-toggle-checked {
  color: var(--%NS%mat-button-toggle-disabled-selected-state-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));
  background-color: var(--%NS%mat-button-toggle-disabled-selected-state-background-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 12%, transparent));
}
.mat-button-toggle-appearance-standard .mat-button-toggle-focus-overlay {
  background-color: var(--%NS%mat-button-toggle-state-layer-color, var(--%NS%mat-sys-on-surface));
}
.mat-button-toggle-appearance-standard:hover .mat-button-toggle-focus-overlay {
  opacity: var(--%NS%mat-button-toggle-hover-state-layer-opacity, var(--%NS%mat-sys-hover-state-layer-opacity));
}
.mat-button-toggle-appearance-standard.cdk-keyboard-focused .mat-button-toggle-focus-overlay {
  opacity: var(--%NS%mat-button-toggle-focus-state-layer-opacity, var(--%NS%mat-sys-focus-state-layer-opacity));
}
@media (hover: none) {
  .mat-button-toggle-appearance-standard:hover .mat-button-toggle-focus-overlay {
    display: none;
  }
}

.mat-button-toggle-label-content {
  -webkit-user-select: none;
  user-select: none;
  display: inline-block;
  padding: 0 16px;
  line-height: var(--%NS%mat-button-toggle-legacy-height);
  position: relative;
}
.mat-button-toggle-appearance-standard .mat-button-toggle-label-content {
  padding: 0 12px;
  line-height: var(--%NS%mat-button-toggle-height, 40px);
}

.mat-button-toggle-label-content > * {
  vertical-align: middle;
}

.mat-button-toggle-focus-overlay {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  border-radius: inherit;
  pointer-events: none;
  opacity: 0;
  background-color: var(--%NS%mat-button-toggle-legacy-state-layer-color);
}

@media (forced-colors: active) {
  .mat-button-toggle-checked .mat-button-toggle-focus-overlay {
    border-bottom: solid 500px;
    opacity: 0.5;
    height: 0;
  }
  .mat-button-toggle-checked:hover .mat-button-toggle-focus-overlay {
    opacity: 0.6;
  }
  .mat-button-toggle-checked.mat-button-toggle-appearance-standard .mat-button-toggle-focus-overlay {
    border-bottom: solid 500px;
  }
}
.mat-button-toggle .mat-button-toggle-ripple {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
}

.mat-button-toggle-button {
  border: 0;
  background: none;
  color: inherit;
  padding: 0;
  margin: 0;
  font: inherit;
  outline: none;
  width: 100%;
  cursor: pointer;
}
.mat-button-toggle-animations-enabled .mat-button-toggle-button {
  transition: padding 150ms 45ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-button-toggle-vertical .mat-button-toggle-button {
  transition: none;
}
.mat-button-toggle-disabled .mat-button-toggle-button {
  cursor: default;
}
.mat-button-toggle-button::-moz-focus-inner {
  border: 0;
}
.mat-button-toggle-checked .mat-button-toggle-button:has(.mat-button-toggle-checkbox-wrapper) {
  padding-left: 30px;
}
[dir=rtl] .mat-button-toggle-checked .mat-button-toggle-button:has(.mat-button-toggle-checkbox-wrapper) {
  padding-left: 0;
  padding-right: 30px;
}

.mat-button-toggle-standalone.mat-button-toggle-appearance-standard {
  --%NS%mat-focus-indicator-border-radius: var(--%NS%mat-button-toggle-shape, var(--%NS%mat-sys-corner-extra-large));
}

.mat-button-toggle-group-appearance-standard:not(.mat-button-toggle-vertical) .mat-button-toggle:last-of-type .mat-button-toggle-button::before {
  border-top-right-radius: var(--%NS%mat-button-toggle-shape, var(--%NS%mat-sys-corner-extra-large));
  border-bottom-right-radius: var(--%NS%mat-button-toggle-shape, var(--%NS%mat-sys-corner-extra-large));
}
.mat-button-toggle-group-appearance-standard:not(.mat-button-toggle-vertical) .mat-button-toggle:first-of-type .mat-button-toggle-button::before {
  border-top-left-radius: var(--%NS%mat-button-toggle-shape, var(--%NS%mat-sys-corner-extra-large));
  border-bottom-left-radius: var(--%NS%mat-button-toggle-shape, var(--%NS%mat-sys-corner-extra-large));
}

.mat-button-toggle-group-appearance-standard.mat-button-toggle-vertical .mat-button-toggle:last-of-type .mat-button-toggle-button::before {
  border-bottom-right-radius: var(--%NS%mat-button-toggle-shape, var(--%NS%mat-sys-corner-extra-large));
  border-bottom-left-radius: var(--%NS%mat-button-toggle-shape, var(--%NS%mat-sys-corner-extra-large));
}
.mat-button-toggle-group-appearance-standard.mat-button-toggle-vertical .mat-button-toggle:first-of-type .mat-button-toggle-button::before {
  border-top-right-radius: var(--%NS%mat-button-toggle-shape, var(--%NS%mat-sys-corner-extra-large));
  border-top-left-radius: var(--%NS%mat-button-toggle-shape, var(--%NS%mat-sys-corner-extra-large));
}
`],encapsulation:2})})()}return t})();var nE=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ie({imports:[ki,Am,we]})}return t})();var $n=class{static count(n,e){return e===`CANDIDATE`?n.candidatePlacements:n.evaluationPlacements}static components(n,e,i){let a=i.trim().toLocaleLowerCase();return(n?.components??[]).filter(o=>o.name.toLocaleLowerCase().includes(a)).sort((o,r)=>this.count(r,e)-this.count(o,e)||o.name.localeCompare(r.name))}static relationships(n,e,i,a){return(n?.relationships??[]).filter(o=>i===`incoming`?o.child===e:o.parent===e).sort((o,r)=>this.count(r,a)-this.count(o,a)||o.parent.localeCompare(r.parent)||o.role.localeCompare(r.role)||o.child.localeCompare(r.child))}static treemap(n,e,i){return{options:{chart:{type:`treemap`,height:390,animation:!1,backgroundColor:`transparent`},title:{text:void 0},credits:{enabled:!1},accessibility:{description:`Component placements. Select a tile or a component in the list to inspect its parents, children and configurations.`},tooltip:{pointFormat:`<b>{point.name}</b><br/>Placements: {point.value:,.0f}`},plotOptions:{series:{animation:!1}},series:[{type:`treemap`,name:`Component placements`,layoutAlgorithm:`squarified`,colorByPoint:!0,borderWidth:3,borderColor:`#fff`,colors:[`#2265a7`,`#00897b`,`#407fab`,`#49a69e`,`#697cad`,`#78b8ad`],dataLabels:{enabled:!0,format:`{point.name}<br/>{point.value:,.0f}`,style:{textOutline:`none`,fontWeight:`600`}},point:{events:{click:function(){this.name&&i(this.name)}}},data:n.filter(a=>this.count(a,e)>0).map(a=>({name:a.name,value:this.count(a,e)}))}]}}}static sankey(n,e,i,a,o){let r=[{id:`focus`,name:n??`Component`,color:`#2265a7`}],s=[],c=(p,g)=>{let v=p.filter(y=>this.count(y,a)>0);for(let[y,E]of v.slice(0,10).entries()){let P=`${g}:${y}`;r.push({id:P,name:g===`in`?`${E.parent} \xB7 ${E.role}`:`${E.role} \u2192 ${E.child}`,color:`#00897b`}),s.push({from:g===`in`?P:`focus`,to:g===`in`?`focus`:P,weight:this.count(E,a),custom:{edge:E}})}let b=v.slice(10).reduce((y,E)=>y+this.count(E,a),0);if(b>0){let y=`${g}:other`;r.push({id:y,name:`Other ${g===`in`?`parents`:`children`}`,color:`#aab7c5`}),s.push({from:g===`in`?y:`focus`,to:g===`in`?`focus`:y,weight:b})}};c(e,`in`),c(i,`out`);let h={type:`sankey`,animation:!1,name:`Direct relationships`,nodes:r,data:s,point:{events:{click:function(){let p=this.options.custom?.edge;p&&o(p)}}}};return{options:{chart:{height:Math.max(300,Math.min(560,Math.max(e.length,i.length)*32)),animation:!1,backgroundColor:`transparent`},title:{text:void 0},credits:{enabled:!1},accessibility:{description:`Direct parents and children of ${n??`the selected component`}. The complete role relationships are also listed in the inspector.`},series:[h]}}}static matrix(n,e,i,a){let o=new Set(this.components(n,e,i).map(y=>y.name)),r=i.trim().length>0,s=(n?.relationships??[]).filter(y=>!r||o.has(y.parent)||o.has(y.child)),c=new Set(o);for(let y of s)c.add(y.parent),c.add(y.child);let h=this.components(n,e,``).filter(y=>c.has(y.name)).sort((y,E)=>(r?Number(o.has(E.name))-Number(o.has(y.name)):0)||this.count(E,e)-this.count(y,e)||y.name.localeCompare(E.name)).slice(0,a),p=new Set(h.map(y=>y.name)),g=new Map,v=0;for(let y of s){if(!p.has(y.parent)||!p.has(y.child))continue;let E=JSON.stringify([y.parent,y.role]),P=g.get(E);P||(P={parent:y.parent,role:y.role,count:0,edges:new Map},g.set(E,P)),P.edges.set(y.child,y),P.count+=this.count(y,e),v=Math.max(v,this.count(y,e))}return{columns:h,rows:[...g.values()].sort((y,E)=>E.count-y.count||y.parent.localeCompare(E.parent)||y.role.localeCompare(E.role)).slice(0,a).map(y=>({parent:y.parent,role:y.role,cells:h.map(E=>y.edges.get(E.name)??null)})),maximum:v}}static parentPercent(n,e){let i=e?.components.find(a=>a.name===n.parent);return i?.configurationCount?100*n.configurationCount/i.configurationCount:0}};var KP=()=>[];var QP=(t,n)=>n.configurationId;var iE=(t,n)=>n.name;var aE=(t,n)=>n.parent+`:`+n.role;var XP=(t,n)=>n.role+`:`+n.child;function ZP(t,n){t&1&&u(0,` Count each unique configuration tree once. Repeated components within a tree count as separate placements. `)}function JP(t,n){t&1&&u(0,` Weight each placement by accepted evaluations, including evaluations that later fail or are rejected. `)}function eF(t,n){if(t&1){let e=q();l(0,`p`,13),u(1),l(2,`button`,15),C(`click`,function(){N(e);return A(_().store.refreshComponentUsage())}),u(3,`Retry`),d()()}t&2&&(m(),j(` `,n,` `))}function tF(t,n){if(t&1&&(l(0,`p`,17),u(1),U(2,`number`),d()),t&2){let e=_();m(),j(` `,ye(2,1,e.unavailableConfigurationCount),` configurations have unavailable component trees and are excluded from placement counts. `)}}function nF(t,n){if(t&1&&(l(0,`p`,14),u(1),d()),t&2){let e=_(2);m(),j(` `,e.store.componentScope()===`CURRENT_ELITES`?`Elites appear after the first race checkpoint.`:`Component usage appears when the first decoded candidate is evaluated.`,` `)}}function iF(t,n){t&1&&(l(0,`p`,14),u(1,`No components match this search.`),d())}function aF(t,n){t&1&&(l(0,`p`,14),u(1,` These configurations have no accepted evaluations yet. Select candidate structures to inspect their composition. `),d())}function oF(t,n){if(t&1){let e=q();l(0,`button`,31),C(`click`,function(){let a=N(e).$implicit;return A(_(4).selectComponent(a.name))}),l(1,`span`),u(2),d(),l(3,`strong`),u(4),U(5,`number`),d()()}if(t&2){let e=n.$implicit,i=_(4);Y(`selected`,i.selected()?.name===e.name),te(`aria-pressed`,i.selected()?.name===e.name),m(2),w(e.name),m(2),w(ye(5,5,i.count(e)))}}function rF(t,n){if(t&1&&(l(0,`mat-card-header`)(1,`mat-card-title`),u(2,`Component usage`),d()(),l(3,`mat-card-content`),H(4,`app-dashboard-chart`,28),D(5,aF,2,0,`p`,14),l(6,`div`,29),ue(7,oF,6,7,`button`,30,iE),d()()),t&2){let e=_(2),i=_();m(4),S(`model`,i.treemap()),m(),I(i.basis()===`EVALUATION`&&e.evaluationPlacementCount===0?5:-1),m(2),me(i.components())}}function sF(t,n){if(t&1&&(l(0,`th`,25),u(1),d()),t&2){let e=n.$implicit;m(),w(e.name)}}function cF(t,n){if(t&1){let e=q();l(0,`button`,31),C(`click`,function(){N(e);let a=_().$implicit;return A(_(5).selectRelationship(a))}),u(1),d()}if(t&2){let e=_().$implicit,i=_(5);gn(`background-color`,i.cellColor(e)),te(`aria-label`,i.cellDescription(e)),m(),j(` `,i.cellLabel(e),` `)}}function lF(t,n){t&1&&(l(0,`span`),u(1,`—`),d())}function dF(t,n){if(t&1&&(l(0,`td`),D(1,cF,2,4,`button`,38)(2,lF,2,0,`span`),d()),t&2){let e=n.$implicit;m(),I(e?1:2)}}function uF(t,n){if(t&1&&(l(0,`tr`)(1,`th`,37),u(2),l(3,`small`),u(4),d()(),ue(5,dF,3,1,`td`,null,Co),d()),t&2){let e=n.$implicit;m(2),j(` `,e.parent),m(2),w(e.role),m(),me(e.cells)}}function mF(t,n){if(t&1&&(l(0,`tr`)(1,`td`),u(2,` No direct relationships in this selection. `),d()()),t&2){let e=_(4);m(),te(`colspan`,e.matrix().columns.length+1)}}function pF(t,n){if(t&1){let e=q();l(0,`mat-card-header`)(1,`mat-card-title`),u(2,`Direct component relationships`),d()(),l(3,`mat-card-content`)(4,`div`,32)(5,`mat-form-field`,2)(6,`mat-label`),u(7,`Maximum rows / columns`),d(),l(8,`mat-select`,3),C(`selectionChange`,function(a){N(e);return A(_(3).matrixLimit.set(a.value))}),l(9,`mat-option`,33),u(10,`10`),d(),l(11,`mat-option`,33),u(12,`20`),d(),l(13,`mat-option`,33),u(14,`50`),d()()(),l(15,`mat-button-toggle-group`,34),C(`change`,function(a){N(e);return A(_(3).matrixPercent.set(a.value))}),l(16,`mat-button-toggle`,33),u(17,`Count`),d(),l(18,`mat-button-toggle`,33),u(19,`% of parent configurations`),d()()(),l(20,`p`,12),u(21,` Rows identify the parent and parameter role; columns identify the direct child. The most frequent matching components are shown. `),d(),l(22,`div`,35)(23,`table`,36)(24,`caption`),u(25,` Direct parent-role to child usage `),d(),l(26,`thead`)(27,`tr`)(28,`th`,25),u(29,`Parent · role`),d(),ue(30,sF,2,1,`th`,25,iE),d()(),l(32,`tbody`),ue(33,uF,7,2,`tr`,null,aE,!1,mF,3,1,`tr`),d()()()()}if(t&2){let e=_(3);m(8),S(`value`,e.matrixLimit()),m(),S(`value`,10),m(2),S(`value`,20),m(2),S(`value`,50),m(2),S(`value`,e.matrixPercent()),m(),S(`value`,!1),m(2),S(`value`,!0),m(12),me(e.matrix().columns),m(3),me(e.matrix().rows)}}function hF(t,n){if(t&1){let e=q();l(0,`button`,31),C(`click`,function(){let a=N(e).$implicit;return A(_(4).selectRelationship(a))}),l(1,`span`)(2,`strong`),u(3),d(),l(4,`small`),u(5),d()(),l(6,`span`),u(7),U(8,`number`),l(9,`small`),u(10),U(11,`number`),d()()()}if(t&2){let e=n.$implicit,i=_(4);m(3),w(e.parent),m(2),w(e.role),m(2),w(ye(8,4,i.count(e))),m(3),j(``,ye(11,6,e.configurationCount),` configurations`)}}function fF(t,n){t&1&&(l(0,`p`),u(1,`No direct parents; this component is used only at the root.`),d())}function gF(t,n){if(t&1){let e=q();l(0,`button`,31),C(`click`,function(){let a=N(e).$implicit;return A(_(4).selectRelationship(a))}),l(1,`span`)(2,`strong`),u(3),d(),l(4,`small`),u(5),d()(),l(6,`span`),u(7),U(8,`number`),l(9,`small`),u(10),U(11,`number`),d()()()}if(t&2){let e=n.$implicit,i=_(4);m(3),w(e.child),m(2),w(e.role),m(2),w(ye(8,4,i.count(e))),m(3),j(``,ye(11,6,e.configurationCount),` configurations`)}}function bF(t,n){t&1&&(l(0,`p`),u(1,`No direct child components.`),d())}function vF(t,n){if(t&1&&(l(0,`mat-card`,20)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d(),l(4,`mat-card-subtitle`),u(5,`Component inspector`),d()(),l(6,`mat-card-content`)(7,`dl`)(8,`dt`),u(9,`Candidate placements`),d(),l(10,`dd`),u(11),U(12,`number`),d(),l(13,`dt`),u(14,`Evaluation exposure`),d(),l(15,`dd`),u(16),U(17,`number`),d(),l(18,`dt`),u(19,`Configuration coverage`),d(),l(20,`dd`),u(21),U(22,`number`),U(23,`number`),d(),l(24,`dt`),u(25,`Root placements / exposure`),d(),l(26,`dd`),u(27),U(28,`number`),U(29,`number`),d()(),l(30,`h3`),u(31,`Used by`),d(),l(32,`div`,39),ue(33,hF,12,8,`button`,40,aE,!1,fF,2,0,`p`),d(),l(36,`h3`),u(37,`Contains`),d(),l(38,`div`,39),ue(39,gF,12,8,`button`,40,XP,!1,bF,2,0,`p`),d()()()),t&2){let e=n,i=_(3);m(3),w(e.name),m(8),w(ye(12,9,e.candidatePlacements)),m(5),w(ye(17,11,e.evaluationPlacements)),m(5),Ae(` `,ye(22,13,e.configurationCount),` (`,ke(23,15,i.selectedCoverage(),`1.0-1`),`%) `),m(6),Ae(` `,ye(28,18,e.rootCandidatePlacements),` / `,ye(29,20,e.rootEvaluationPlacements),` `),m(6),me(i.incoming()),m(6),me(i.outgoing())}}function _F(t,n){if(t&1&&(l(0,`mat-card`,21)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d()(),l(4,`mat-card-content`),H(5,`app-dashboard-chart`,28),l(6,`p`,12),u(7,` Direct parents → selected component → direct children. Up to ten relationships are shown on each side; the inspector lists all relationships. `),d()()()),t&2){let e=_(3);m(3),j(`Neighborhood of `,e.selected()?.name),m(2),S(`model`,e.sankey())}}function yF(t,n){if(t&1){let e=q();l(0,`p`),u(1),l(2,`button`,15),C(`click`,function(){N(e);return A(_(3).selectedEdge.set(null))}),u(3),d()()}if(t&2){let e=n,i=_(3);m(),Ot(` `,e.parent,` → `,e.role,` → `,e.child,` `),m(2),j(` Show all using `,i.selected()?.name,` `)}}function xF(t,n){if(t&1&&(l(0,`p`),u(1),d()),t&2){let e=_(3);m(),j(`Configurations containing `,e.selected()?.name)}}function CF(t,n){if(t&1){let e=q();l(0,`p`,22),u(1),l(2,`button`,15),C(`click`,function(){N(e);return A(_(3).retryPage())}),u(3,`Retry`),d()()}t&2&&(m(),j(` `,n,` `))}function SF(t,n){if(t&1){let e=q();l(0,`tr`)(1,`td`)(2,`button`,15),C(`click`,function(){let a=N(e).$implicit;return A(_(3).inspectCandidate(a.configurationId))}),u(3),d()(),l(4,`td`),u(5),U(6,`number`),d(),l(7,`td`),u(8),U(9,`number`),d(),l(10,`td`),u(11),d(),l(12,`td`),u(13),d()()}if(t&2){let e=n.$implicit;m(3),j(` `,e.configurationId,` `),m(2),w(ye(6,8,e.occurrences)),m(3),w(ye(9,10,e.evaluationPlacements)),m(3),Si(` `,e.evaluations.running,` / `,e.evaluations.succeeded,` / `,e.evaluations.rejected,` / `,e.evaluations.failed,` `),m(2),w(e.elitePosition??`—`)}}function wF(t,n){if(t&1&&(l(0,`tr`)(1,`td`,41),u(2),d()()),t&2){let e=_(3);m(2),j(` `,e.loading()?`Loading configurations…`:`No matching configurations.`,` `)}}function EF(t,n){if(t&1){let e=q();l(0,`div`,18)(1,`mat-card`,19),D(2,rF,9,2)(3,pF,36,8),d(),D(4,vF,42,22,`mat-card`,20),D(5,_F,8,2,`mat-card`,21),l(6,`mat-card`,21)(7,`mat-card-header`)(8,`mat-card-title`),u(9,`Matching configurations`),d()(),l(10,`mat-card-content`),D(11,yF,4,4,`p`)(12,xF,2,1,`p`),D(13,CF,4,1,`p`,22),l(14,`div`,23)(15,`table`,24)(16,`thead`)(17,`tr`)(18,`th`,25),u(19,`Configuration`),d(),l(20,`th`,25),u(21,`Tree placements`),d(),l(22,`th`,25),u(23,`Evaluation exposure`),d(),l(24,`th`,25),u(25,`Running / succeeded / rejected / failed`),d(),l(26,`th`,25),u(27,`Elite rank`),d()()(),l(28,`tbody`),ue(29,SF,14,12,`tr`,null,QP,!1,wF,3,1,`tr`),d()()(),l(32,`div`,26)(33,`span`),u(34),U(35,`number`),d(),l(36,`button`,27),C(`click`,function(){N(e);let a=_(2);return A(a.offset.set(a.offset()-25))}),u(37,` Previous `),d(),l(38,`span`),u(39),d(),l(40,`button`,27),C(`click`,function(){N(e);let a=_(2);return A(a.offset.set(a.page()?.nextOffset??0))}),u(41,` Next `),d()()()()()}if(t&2){let e,i,a,o=_(2);m(2),I(o.view()===`usage`?2:3),m(2),I((e=o.selected())?4:-1,e),m(),I(o.view()===`usage`&&(o.incoming().length>0||o.outgoing().length>0)?5:-1),m(6),I((i=o.relationship())?11:12,i),m(2),I((a=o.pageError())?13:-1,a),m(),te(`aria-busy`,o.loading()),m(15),me(o.page()?.candidates??rt(13,KP)),m(5),j(``,ye(35,11,o.page()?.total??0),` matching configurations`),m(2),S(`disabled`,o.offset()===0||o.loading()),m(3),j(`Page `,o.offset()/25+1),m(),S(`disabled`,o.page()?.nextOffset==null||o.loading())}}function DF(t,n){if(t&1&&(l(0,`div`,16)(1,`article`)(2,`span`),u(3,`Decoded configurations`),d(),l(4,`strong`),u(5),U(6,`number`),U(7,`number`),d()(),l(8,`article`)(9,`span`),u(10),d(),l(11,`strong`),u(12),U(13,`number`),d()(),l(14,`article`)(15,`span`),u(16,`Distinct components`),d(),l(17,`strong`),u(18),U(19,`number`),d()(),l(20,`article`)(21,`span`),u(22,`Accepted evaluations`),d(),l(23,`strong`),u(24),U(25,`number`),d()()(),D(26,tF,3,3,`p`,17),D(27,nF,2,1,`p`,14)(28,iF,2,0,`p`,14)(29,EF,42,14,`div`,18)),t&2){let e=n,i=_();m(5),Ae(``,ye(6,8,e.decodedConfigurationCount),` / `,ye(7,10,e.configurationCount)),m(5),w(i.basis()===`CANDIDATE`?`Component placements`:`Evaluation placements`),m(2),w(ye(13,12,i.basis()===`CANDIDATE`?e.candidatePlacementCount:e.evaluationPlacementCount)),m(6),w(ye(19,14,e.components.length)),m(6),w(ye(25,16,e.evaluationCount)),m(2),I(e.unavailableConfigurationCount>0?26:-1),m(),I(e.components.length===0?27:i.components().length===0?28:29)}}function IF(t,n){t&1&&(l(0,`p`,14),u(1,`Loading component usage…`),d())}var Om=class t{store=f(ja);dialog=f(Ho);destroyRef=f(Ne);basis=T(`CANDIDATE`);view=T(`usage`);search=T(``);matrixLimit=T(20);matrixPercent=T(!1);selectedName=T(null);selectedEdge=T(null);offset=T(0);page=T(null);loading=T(!1);pageError=T(null);pageRefresh=T(0);request=0;pageKey=null;destroyed=!1;components=ne(()=>$n.components(this.store.componentUsage(),this.basis(),this.search()));selected=ne(()=>this.components().find(n=>n.name===this.selectedName())??this.components()[0]);relationship=ne(()=>{let n=this.selectedEdge();return this.store.componentUsage()?.relationships.find(e=>n&&e.parent===n.parent&&e.role===n.role&&e.child===n.child)??null});incoming=ne(()=>$n.relationships(this.store.componentUsage(),this.selected()?.name,`incoming`,this.basis()));outgoing=ne(()=>$n.relationships(this.store.componentUsage(),this.selected()?.name,`outgoing`,this.basis()));treemap=ne(()=>$n.treemap(this.components(),this.basis(),n=>this.selectComponent(n)));sankey=ne(()=>$n.sankey(this.selected()?.name,this.incoming(),this.outgoing(),this.basis(),n=>this.selectRelationship(n)));matrix=ne(()=>$n.matrix(this.store.componentUsage(),this.basis(),this.search(),this.matrixLimit()));selector=ne(()=>{let n=this.relationship();if(n)return{parent:n.parent,role:n.role,child:n.child};let e=this.selected()?.name;return e?{component:e}:null});selectedCoverage=ne(()=>{let n=this.store.componentUsage()?.decodedConfigurationCount??0;return n?100*(this.selected()?.configurationCount??0)/n:0});selectionKey=ne(()=>JSON.stringify(this.selector()));constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.request++}),Lt(()=>{this.store.componentScope(),this.basis(),this.selectionKey(),this.store.status()?.runId,nt(()=>this.offset.set(0))}),Lt(()=>{let n=this.store.componentUsage(),e=this.selector(),i=this.store.componentScope(),a=this.basis(),o=this.offset();this.pageRefresh(),nt(()=>{if(!n||!e){this.request++,this.page.set(null),this.loading.set(!1),this.pageError.set(null);return}this.loadPage({scope:i,basis:a,selector:e,offset:o,limit:25})})})}selectComponent(n){this.selectedName.set(n),this.selectedEdge.set(null)}selectRelationship(n){this.selectedName.set(n.child),this.selectedEdge.set(n)}updateSearch(n){this.search.set(n.target.value),this.selectedEdge.set(null)}count(n){return $n.count(n,this.basis())}cellLabel(n){return this.matrixPercent()?`${$n.parentPercent(n,this.store.componentUsage()).toFixed(1)}%`:this.count(n).toLocaleString()}cellColor(n){let e=this.matrixPercent()?$n.parentPercent(n,this.store.componentUsage())/100:this.count(n)/(this.matrix().maximum||1);return`rgba(0, 137, 123, ${.08+Math.max(0,Math.min(1,e))*.65})`}cellDescription(n){return`${n.parent}, ${n.role}, ${n.child}: ${this.cellLabel(n)}`}inspectCandidate(n){this.dialog.open(ms,{data:n,width:`46rem`,maxWidth:`94vw`})}retryPage(){this.pageRefresh.update(n=>n+1)}async loadPage(n){let e=++this.request,i=this.store.componentUsage()?.runId;this.loading.set(!0),this.pageError.set(null);let a=JSON.stringify([i,n]);a!==this.pageKey&&this.page.set(null),this.pageKey=a;try{let o=await this.store.loadComponentCandidates(n);if(this.destroyed||e!==this.request)return;if(o.runId!==i||o.scope!==n.scope)throw new Error(`Configurations belong to a different run or scope`);if(n.offset>0&&o.total<=n.offset){this.offset.set(0);return}this.page.set(o)}catch(o){!this.destroyed&&e===this.request&&this.pageError.set(o instanceof Error?o.message:`Unable to load configurations`)}finally{!this.destroyed&&e===this.request&&this.loading.set(!1)}}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-component-usage-dashboard`]],decls:33,vars:7,consts:[[1,`component-page`],[1,`controls`],[`appearance`,`outline`],[3,`selectionChange`,`value`],[`value`,`ALL`],[`value`,`CURRENT_ELITES`],[`value`,`CANDIDATE`],[`value`,`EVALUATION`],[`matInput`,``,3,`input`,`value`],[`aria-label`,`Component visualization`,3,`change`,`value`],[`value`,`usage`],[`value`,`matrix`],[1,`explanation`],[`role`,`alert`,1,`warning`],[1,`empty`],[`mat-button`,``,3,`click`],[1,`totals`],[1,`warning`],[1,`layout`],[`appearance`,`outlined`,1,`visualization`],[`appearance`,`outlined`,1,`inspector`],[`appearance`,`outlined`,1,`wide`],[`role`,`alert`],[1,`table-scroll`],[1,`candidate-table`],[`scope`,`col`],[1,`pagination`],[`mat-button`,``,3,`click`,`disabled`],[3,`model`],[`aria-label`,`Select a component`,1,`component-list`],[`type`,`button`,3,`selected`],[`type`,`button`,3,`click`],[1,`matrix-controls`],[3,`value`],[`aria-label`,`Matrix values`,3,`change`,`value`],[`tabindex`,`0`,`aria-label`,`Scrollable component relationship matrix`,1,`matrix-scroll`],[1,`matrix`],[`scope`,`row`],[`type`,`button`,3,`background-color`],[1,`relationships`],[`type`,`button`],[`colspan`,`5`]],template:function(e,i){if(e&1&&(l(0,`section`,0)(1,`div`,1)(2,`mat-form-field`,2)(3,`mat-label`),u(4,`Configurations`),d(),l(5,`mat-select`,3),C(`selectionChange`,function(o){return i.store.selectComponentScope(o.value)}),l(6,`mat-option`,4),u(7,`All evaluated candidates`),d(),l(8,`mat-option`,5),u(9,`Current elites`),d()()(),l(10,`mat-form-field`,2)(11,`mat-label`),u(12,`Count by`),d(),l(13,`mat-select`,3),C(`selectionChange`,function(o){return i.basis.set(o.value)}),l(14,`mat-option`,6),u(15,`Candidate structures`),d(),l(16,`mat-option`,7),u(17,`Evaluation exposure`),d()()(),l(18,`mat-form-field`,2)(19,`mat-label`),u(20,`Find component`),d(),l(21,`input`,8),C(`input`,function(o){return i.updateSearch(o)}),d()(),l(22,`mat-button-toggle-group`,9),C(`change`,function(o){return i.view.set(o.value)}),l(23,`mat-button-toggle`,10),u(24,`Usage map`),d(),l(25,`mat-button-toggle`,11),u(26,`Relationship matrix`),d()()(),l(27,`p`,12),D(28,ZP,1,0)(29,JP,1,0),d(),D(30,eF,4,1,`p`,13),D(31,DF,30,18)(32,IF,2,0,`p`,14),d()),e&2){let a,o;m(5),S(`value`,i.store.componentScope()),m(8),S(`value`,i.basis()),m(8),S(`value`,i.search()),m(),S(`value`,i.view()),m(6),I(i.basis()===`CANDIDATE`?28:29),m(2),I((a=i.store.componentError())?30:-1,a),m(),I((o=i.store.componentUsage())?31:i.store.componentError()?-1:32,o)}},dependencies:[Pt,en,nE,Bb,Am,zn,Bn,jn,Ma,Fu,ka,di,Ti,na,Xr,Qr,Pa,Ra,Un,Ai,En],styles:[`[_nghost-%COMP%]{display:block}.component-page[_ngcontent-%COMP%]{padding:1.4rem 0}.controls[_ngcontent-%COMP%], .matrix-controls[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:1rem;align-items:center}.controls[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{flex:1 1 12rem}.explanation[_ngcontent-%COMP%]{color:#526477;line-height:1.5;font-size:.9rem;margin:.5rem 0 1.25rem}.totals[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(4,1fr);gap:1rem;margin-bottom:1.4rem}.totals[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{border:1px solid #d9e3ec;border-radius:.8rem;padding:1rem;background:#fff}.totals[_ngcontent-%COMP%]   span[_ngcontent-%COMP%], .totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{display:block}.totals[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:#526477;font-size:.85rem}.totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{color:#174b80;margin-top:.4rem;font-size:1.5rem}.warning[_ngcontent-%COMP%]{background:#fff5dc;padding:.8rem 1rem;border-radius:.6rem}.empty[_ngcontent-%COMP%]{padding:2rem;text-align:center;color:#526477}.layout[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(0,2fr) minmax(18rem,1fr);gap:1.25rem}.layout[_ngcontent-%COMP%] > *[_ngcontent-%COMP%]{min-width:0}.wide[_ngcontent-%COMP%]{grid-column:1/-1}mat-card-header[_ngcontent-%COMP%]{margin-bottom:1rem}.component-list[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.3rem;max-height:15rem;overflow:auto;margin-top:1rem}.component-list[_ngcontent-%COMP%]   button[_ngcontent-%COMP%], .relationships[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{border:0;background:#f3f7fb;cursor:pointer;border-radius:.4rem;padding:.65rem;display:flex;justify-content:space-between;gap:.5rem;text-align:left;color:#193b59}.component-list[_ngcontent-%COMP%]   button.selected[_ngcontent-%COMP%]{background:#d5e8f7;outline:2px solid #2265a7;outline-offset:-2px}.component-list[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{overflow-wrap:anywhere}.relationships[_ngcontent-%COMP%]{max-height:17rem;overflow:auto;display:grid;gap:.35rem}.relationships[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{display:block;color:#526477;margin-top:.25rem}.relationships[_ngcontent-%COMP%]   button[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{text-align:right}.relationships[_ngcontent-%COMP%]   p[_ngcontent-%COMP%]{color:#526477}dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:1fr auto;gap:.8rem;font-size:.9rem}dt[_ngcontent-%COMP%]{color:#526477}dd[_ngcontent-%COMP%]{margin:0;text-align:right}h3[_ngcontent-%COMP%]{font-size:1rem;margin:1.4rem 0 .7rem}.matrix-scroll[_ngcontent-%COMP%], .table-scroll[_ngcontent-%COMP%]{overflow:auto}table[_ngcontent-%COMP%]{border-collapse:separate;border-spacing:0;font-size:.85rem;width:100%}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.65rem;border-bottom:1px solid #dce5ee;text-align:left}th[_ngcontent-%COMP%]{color:#526477;font-weight:600}.matrix[_ngcontent-%COMP%]   caption[_ngcontent-%COMP%]{text-align:left;padding:.5rem 0;color:#526477}.matrix[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]{min-width:7rem}.matrix[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{display:block;margin-top:.2rem;font-weight:400}.matrix[_ngcontent-%COMP%]   tbody[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]{position:sticky;left:0;background:#fff;z-index:1}.matrix[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]{padding:.2rem;text-align:center}.matrix[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{width:100%;min-width:5rem;min-height:2.8rem;border:0;border-radius:.25rem;cursor:pointer;color:#112e3b}.pagination[_ngcontent-%COMP%]{display:flex;justify-content:flex-end;align-items:center;flex-wrap:wrap;gap:.7rem;margin-top:.8rem}.pagination[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:first-child{margin-right:auto}@media(max-width:1050px){.layout[_ngcontent-%COMP%]{grid-template-columns:1fr}.totals[_ngcontent-%COMP%]{grid-template-columns:repeat(2,1fr)}}@media(max-width:600px){.component-list[_ngcontent-%COMP%]{grid-template-columns:1fr}.controls[_ngcontent-%COMP%]   mat-button-toggle-group[_ngcontent-%COMP%], .matrix-controls[_ngcontent-%COMP%]   mat-button-toggle-group[_ngcontent-%COMP%]{max-width:100%;overflow:auto}.totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font-size:1.2rem}}`]})};var Rm=()=>[];var kF=(t,n)=>n.key;var oE=(t,n)=>n.id;var rE=(t,n)=>n.configurationId;var MF=(t,n)=>n.instanceName;var TF=(t,n)=>n.iteration;function NF(t,n){if(t&1){let e=q();l(0,`button`,19),C(`click`,function(){N(e);return A(_().showIssues())}),l(1,`span`,53)(2,`strong`),u(3),d(),l(4,`small`),u(5),d()()()}if(t&2){let e=n.$implicit;m(2),Y(`critical`,e.severity===`critical`),m(),w(e.title),m(2),w(e.message)}}function AF(t,n){t&1&&(l(0,`span`,10),u(1,`No evaluation alerts`),d())}function OF(t,n){if(t&1){let e=q();l(0,`button`,19),C(`click`,function(){N(e);return A(_().showIssues())}),l(1,`strong`),u(2,`View all issues`),d()()}}function RF(t,n){if(t&1&&(l(0,`section`,11)(1,`div`,54)(2,`span`,55),u(3,`Evaluation budget`),d(),l(4,`strong`),u(5),U(6,`number`),U(7,`number`),d(),l(8,`span`),u(9),U(10,`number`),d()(),H(11,`mat-progress-bar`,56),l(12,`div`,57),H(13,`span`,58),u(14),d()()),t&2){let e=_(),i=Xf(0);m(5),Ae(``,ye(6,7,i.budget.used),` / `,ye(7,9,i.budget.maximum)),m(4),j(``,ye(10,11,i.budget.remaining),` remaining`),m(2),S(`value`,e.store.budgetPercent()),m(2),Y(`complete`,i.state===`COMPLETED`),m(),j(` `,e.phaseLabel(i.phase),` `)}}function PF(t,n){if(t&1){let e=q();l(0,`section`,12)(1,`strong`),u(2,`Monitor connection problem`),d(),l(3,`span`),u(4),d(),l(5,`button`,50),C(`click`,function(){N(e);return A(_().store.retryNow())}),u(6,`Retry now`),d()()}t&2&&(m(4),w(n))}function FF(t,n){if(t&1&&(l(0,`section`,13)(1,`strong`),u(2),d(),l(3,`span`),u(4),d()()),t&2){let e=n;m(2),w(e.type),m(2),w(e.message??`The tuning run stopped unexpectedly.`)}}function LF(t,n){if(t&1){let e=q();l(0,`button`,19),C(`click`,function(){N(e);let a=_().$implicit;return A(_().preferences.show(a.id))}),u(1),d()}if(t&2){let e=_().$implicit;m(),j(` Show `,e.label,` `)}}function VF(t,n){if(t&1&&D(0,LF,2,1,`button`,9),t&2){let e=n.$implicit;I(_().preferences.isHidden(e.id)?0:-1)}}function BF(t,n){if(t&1){let e=q();l(0,`button`,59),C(`click`,function(){let a=N(e).$implicit;return A(_().inspectCandidate(a.configurationId))}),l(1,`span`,60),u(2),d(),l(3,`span`)(4,`strong`),u(5),d(),l(6,`small`),u(7),d()()()}if(t&2){let e=n.$implicit,i=_();m(),tt(i.rankClass(e.position)),m(),w(e.position),m(3),j(`Configuration `,e.configurationId),m(2),w(i.parameterPreview(e))}}function jF(t,n){t&1&&(l(0,`p`,32),u(1,`Elites appear after the first completed race checkpoint.`),d())}function zF(t,n){if(t&1){let e=q();l(0,`button`,61),C(`click`,function(){N(e);return A(_().inspectSearchSpace())}),u(1,` Inspect search space `),d()}}function HF(t,n){if(t&1&&(l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),U(7,`number`),d(),l(8,`td`),u(9),d(),l(10,`td`),u(11),d()()),t&2){let e=n.$implicit;m(2),w(e.instanceName),m(2),w(e.evaluations),m(2),j(` `,e.bestCost===null?`—`:ke(7,5,e.bestCost,`1.0-5`),` `),m(3),w(e.issues),m(2),w(e.slow)}}function UF(t,n){t&1&&(l(0,`tr`)(1,`td`,62),u(2,`No instance activity yet.`),d()())}function $F(t,n){t&1&&u(0,` Final result `)}function GF(t,n){t&1&&u(0,` Latest completed checkpoint `)}function WF(t,n){if(t&1){let e=q();l(0,`button`,59),C(`click`,function(){let a=N(e).$implicit;return A(_().inspectCandidate(a.configurationId))}),l(1,`span`,60),u(2),d(),l(3,`span`)(4,`strong`),u(5),d(),l(6,`small`),u(7),d()()()}if(t&2){let e=n.$implicit,i=_();m(),tt(i.rankClass(e.position)),m(),w(e.position),m(3),j(`Configuration `,e.configurationId),m(2),w(i.parameterPreview(e))}}function qF(t,n){t&1&&(l(0,`p`,32),u(1,`No elite snapshot is available yet.`),d())}function YF(t,n){if(t&1&&(l(0,`article`)(1,`strong`),u(2),d(),l(3,`span`),u(4),U(5,`date`),d(),l(6,`span`),u(7),U(8,`number`),d(),l(9,`span`),u(10),d()()),t&2){let e=n.$implicit;m(2),j(`Checkpoint `,e.iteration),m(2),w(ke(5,4,e.updatedAt,`medium`)),m(3),j(``,ye(8,7,e.progress.experimentsUsed),` evaluations used`),m(3),j(``,e.elites.length,` elites`)}}function KF(t,n){t&1&&(l(0,`p`,32),u(1,`No completed race checkpoints yet.`),d())}function QF(t,n){t&1&&H(0,`app-component-usage-dashboard`)}function XF(t,n){t&1&&(l(0,`mat-tab`,48),ot(1,QF,1,0,`ng-template`,63),d())}function ZF(t,n){if(t&1&&(l(0,`a`,52)(1,`span`)(2,`strong`),u(3),d(),l(4,`small`),u(5),d()(),l(6,`span`),u(7),d(),l(8,`span`),u(9,`Download`),d()()),t&2){let e=n.$implicit,i=_();S(`href`,i.store.artifactUrl(e.id),hn),m(3),w(e.filename),m(2),w(e.mediaType),m(2),w(i.artifactSize(e))}}function JF(t,n){t&1&&(l(0,`p`,32),u(1,` Artifacts appear as IRACE writes logs, plots, workspace data, and final elites. `),d())}var Pm=class t{store=f(ja);preferences=f(Va);panels=Nb;selectedTab=T(0);forceIssues=T(!1);dialog=f(Ho);constructor(){this.store.start()}selectTab(n){this.selectedTab.set(n.index),n.tab.textLabel!==`Evaluations`&&this.forceIssues.set(!1),n.tab.textLabel===`Artifacts`&&this.store.refreshArtifacts()}showIssues(){this.forceIssues.set(!0),this.selectedTab.set(1)}inspectCandidate(n){this.dialog.open(ms,{data:n,width:`46rem`,maxWidth:`94vw`})}inspectSearchSpace(){let n=this.store.searchSpace();n&&this.dialog.open(Tm,{data:n,maxWidth:`96vw`})}rankClass(n){return n<=3?`rank-${n}`:`rank-other`}phaseLabel(n){return n.toLowerCase().split(`_`).map(e=>e.charAt(0).toUpperCase()+e.slice(1)).join(` `)}elapsedLabel(n){if(n===null)return`—`;let e=Math.floor(n/1e3),i=Math.floor(e/3600),a=Math.floor(e%3600/60),o=e%60;return i>0?`${i}h ${a}m`:`${a}m ${o}s`}metricLabel(){let n=this.store.status()?.metric;if(!n)return`Waiting for metric details`;let e=n.kind===`AREA_UNDER_CURVE`?`Area under curve`:`Final objective`,i=n.negated?`maximized, converted to cost`:`minimized`;return`${n.objectiveName} \xB7 ${e} \xB7 ${i}`}parameterPreview(n){let e=Object.entries(n.parameters);return e.length===0?`No parameters`:e.slice(0,3).map(([i,a])=>`${i}=${a}`).join(` · `)}artifactSize(n){return n.size<1024?`${n.size} B`:n.size<1048576?`${(n.size/1024).toFixed(1)} KB`:`${(n.size/1048576).toFixed(1)} MB`}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-tuning-dashboard`]],decls:183,vars:47,consts:[[`alertsMenu`,`matMenu`],[`panelMenu`,`matMenu`],[1,`topbar`],[1,`brand`],[`src`,`favicon.svg`,`alt`,``],[1,`toolbar-spacer`],[`aria-label`,`Synchronization state`],[`mat-button`,``,`type`,`button`,`matBadgeColor`,`warn`,`aria-label`,`Tuning alerts`,1,`alerts-button`,3,`menuOpened`,`matMenuTriggerFor`,`matBadge`,`matBadgeHidden`],[1,`alerts-menu`],[`mat-menu-item`,``,`type`,`button`],[1,`menu-empty`],[`aria-label`,`Evaluation budget`,1,`budget-ribbon`],[`role`,`alert`,1,`request-error`],[`role`,`alert`,1,`run-error`],[`animationDuration`,`150ms`,3,`selectedTabChange`,`selectedIndex`],[`label`,`Overview`],[1,`tab-page`,`overview`],[1,`overview-actions`],[`mat-button`,``,`type`,`button`,3,`matMenuTriggerFor`],[`mat-menu-item`,``,`type`,`button`,3,`click`],[`panelId`,`evaluation-health`,`title`,`Evaluation health`,1,`wide`],[1,`kpi-grid`],[1,`kpi`],[1,`kpi`,`success`],[1,`kpi`,`warning`],[1,`kpi`,`danger`],[`panelId`,`evaluation-cost`,`title`,`Evaluation cost`],[3,`model`],[`panelId`,`elite-evolution`,`title`,`Elite rank evolution`],[`panelId`,`current-elites`,`title`,`Current elites`],[1,`elite-list`],[`type`,`button`,1,`elite-row`],[1,`empty-state`],[`panelId`,`run-information`,`title`,`Run information`],[1,`run-info`],[`mat-stroked-button`,``,`type`,`button`],[`label`,`Evaluations`],[1,`tab-page`],[1,`two-column`],[`appearance`,`outlined`],[1,`summary-scroll`],[1,`summary-table`],[`appearance`,`outlined`,1,`evaluation-card`],[3,`candidateSelected`,`evaluations`,`forceIssues`],[`label`,`Elites`],[1,`two-column`,`elite-layout`],[1,`elite-list`,`detailed`],[1,`history-list`],[`label`,`Components`],[`label`,`Artifacts`],[`mat-button`,``,`type`,`button`,3,`click`],[1,`artifact-list`],[`download`,``,3,`href`],[1,`alert-item`],[1,`budget-copy`],[1,`eyebrow`],[`mode`,`determinate`,3,`value`],[1,`phase`],[1,`phase-dot`],[`type`,`button`,1,`elite-row`,3,`click`],[1,`medal`],[`mat-stroked-button`,``,`type`,`button`,3,`click`],[`colspan`,`5`,1,`empty-state`],[`matTabContent`,``]],template:function(e,i){if(e&1&&(Zd(0),l(1,`mat-toolbar`,2)(2,`span`,3),H(3,`img`,4),u(4,` Mork tuning monitor`),d(),H(5,`span`,5),l(6,`mat-chip-set`,6)(7,`mat-chip`),u(8),d()(),l(9,`button`,7),C(`menuOpened`,function(){return i.store.markAlertsSeen()}),u(10,` Alerts `),d(),l(11,`mat-menu`,8,0),ue(13,NF,6,4,`button`,9,kF,!1,AF,2,0,`span`,10),D(16,OF,3,0,`button`,9),d()(),D(17,RF,15,13,`section`,11),l(18,`main`),D(19,PF,7,1,`section`,12),D(20,FF,5,2,`section`,13),l(21,`mat-tab-group`,14),C(`selectedTabChange`,function(o){return i.selectTab(o)}),l(22,`mat-tab`,15)(23,`section`,16)(24,`div`,17)(25,`span`),u(26,`Live view of the current tuning run`),d(),l(27,`button`,18),u(28,`Panels`),d(),l(29,`mat-menu`,null,1),ue(31,VF,1,1,null,null,oE),l(33,`button`,19),C(`click`,function(){return i.preferences.reset()}),u(34,` Restore all panels `),d()()(),l(35,`app-monitor-panel`,20)(36,`section`,21)(37,`article`,22)(38,`span`),u(39,`Running`),d(),l(40,`strong`),u(41),d()(),l(42,`article`,23)(43,`span`),u(44,`Succeeded`),d(),l(45,`strong`),u(46),d()(),l(47,`article`,24)(48,`span`),u(49,`Rejected`),d(),l(50,`strong`),u(51),d()(),l(52,`article`,25)(53,`span`),u(54,`Failed`),d(),l(55,`strong`),u(56),d()(),l(57,`article`,24)(58,`span`),u(59,`Slow`),d(),l(60,`strong`),u(61),d()()()(),l(62,`app-monitor-panel`,26),H(63,`app-dashboard-chart`,27),d(),l(64,`app-monitor-panel`,28),H(65,`app-dashboard-chart`,27),d(),l(66,`app-monitor-panel`,29)(67,`div`,30),ue(68,BF,8,5,`button`,31,rE,!1,jF,2,0,`p`,32),d()(),l(71,`app-monitor-panel`,33)(72,`dl`,34)(73,`dt`),u(74,`Run ID`),d(),l(75,`dd`),u(76),d(),l(77,`dt`),u(78,`Mode`),d(),l(79,`dd`),u(80),d(),l(81,`dt`),u(82,`Elapsed`),d(),l(83,`dd`),u(84),d(),l(85,`dt`),u(86,`Training instances`),d(),l(87,`dd`),u(88),d(),l(89,`dt`),u(90,`Generated parameters`),d(),l(91,`dd`),u(92),d(),l(93,`dt`),u(94,`Cost metric`),d(),l(95,`dd`),u(96),d(),l(97,`dt`),u(98,`Last update`),d(),l(99,`dd`),u(100),U(101,`date`),d()(),D(102,zF,2,0,`button`,35),d()()(),l(103,`mat-tab`,36)(104,`section`,37)(105,`section`,38)(106,`mat-card`,39)(107,`mat-card-header`)(108,`mat-card-title`),u(109,`Evaluation activity`),d()(),l(110,`mat-card-content`),H(111,`app-dashboard-chart`,27),d()(),l(112,`mat-card`,39)(113,`mat-card-header`)(114,`mat-card-title`),u(115,`Instance summary`),d()(),l(116,`mat-card-content`,40)(117,`table`,41)(118,`thead`)(119,`tr`)(120,`th`),u(121,`Instance`),d(),l(122,`th`),u(123,`Runs`),d(),l(124,`th`),u(125,`Best cost`),d(),l(126,`th`),u(127,`Issues`),d(),l(128,`th`),u(129,`Slow`),d()()(),l(130,`tbody`),ue(131,HF,12,8,`tr`,null,MF,!1,UF,3,0,`tr`),d()()()()(),l(134,`mat-card`,42)(135,`mat-card-header`)(136,`mat-card-title`),u(137,`Evaluation ledger`),d()(),l(138,`mat-card-content`)(139,`app-evaluation-table`,43),C(`candidateSelected`,function(o){return i.inspectCandidate(o)}),d()()()()(),l(140,`mat-tab`,44)(141,`section`,37)(142,`section`,45)(143,`mat-card`,39)(144,`mat-card-header`)(145,`mat-card-title`),u(146,`Current elite set`),d(),l(147,`mat-card-subtitle`),D(148,$F,1,0)(149,GF,1,0),d()(),l(150,`mat-card-content`)(151,`div`,46),ue(152,WF,8,5,`button`,31,rE,!1,qF,2,0,`p`,32),d()()(),l(155,`mat-card`,39)(156,`mat-card-header`)(157,`mat-card-title`),u(158,`Rank history`),d()(),l(159,`mat-card-content`),H(160,`app-dashboard-chart`,27),d()()(),l(161,`mat-card`,39)(162,`mat-card-header`)(163,`mat-card-title`),u(164,`Race checkpoints`),d()(),l(165,`mat-card-content`,47),ue(166,YF,11,9,`article`,null,TF,!1,KF,2,0,`p`,32),d()()()(),D(169,XF,2,0,`mat-tab`,48),l(170,`mat-tab`,49)(171,`section`,37)(172,`mat-card`,39)(173,`mat-card-header`)(174,`mat-card-title`),u(175,`Run artifacts`),d(),H(176,`span`,5),l(177,`button`,50),C(`click`,function(){return i.store.refreshArtifacts()}),u(178,`Refresh`),d()(),l(179,`mat-card-content`,51),ue(180,ZF,10,4,`a`,52,oE,!1,JF,2,0,`p`,32),d()()()()()()),e&2){let a,o,r=ft(12),s=ft(30),c=Qf(i.store.status());m(7),Y(`live`,i.store.syncState()===`live`),m(),w(i.store.connectionLabel()),m(),S(`matMenuTriggerFor`,r)(`matBadge`,i.store.unseenAlertCount())(`matBadgeHidden`,i.store.unseenAlertCount()===0),m(4),me(i.store.alerts().slice(0,10)),m(3),I(i.store.alerts().length>0?16:-1),m(),I(c?17:-1),m(2),I((a=i.store.latestError())?19:-1,a),m(),I((o=c?.failure)?20:-1,o),m(),S(`selectedIndex`,i.selectedTab()),m(6),S(`matMenuTriggerFor`,s),m(4),me(i.panels),m(10),w(c?.evaluations?.running??0),m(5),w(c?.evaluations?.succeeded??0),m(5),w(c?.evaluations?.rejected??0),m(5),w(c?.evaluations?.failed??0),m(5),w(c?.evaluations?.slow??0),m(2),S(`model`,i.store.evaluationCostChart()),m(2),S(`model`,i.store.eliteRankChart()),m(3),me(i.store.elites()?.elites??rt(43,Rm)),m(8),w(c?.runId??`—`),m(4),w(c?.mode??`—`),m(4),w(i.elapsedLabel(c?.elapsedMillis??null)),m(4),w(c?.trainingInstanceCount??`Preparing…`),m(4),w(c?.generatedParameterCount??0),m(4),w(i.metricLabel()),m(4),w(ke(101,40,i.store.lastUpdated(),`mediumTime`)),m(2),I(i.store.searchSpace()?102:-1),m(9),S(`model`,i.store.evaluationActivityChart()),m(20),me(i.store.instanceSummaries()),m(8),S(`evaluations`,i.store.evaluations())(`forceIssues`,i.forceIssues()),m(9),I(i.store.elites()?.finalSnapshot?148:149),m(4),me(i.store.elites()?.elites??rt(44,Rm)),m(8),S(`model`,i.store.eliteRankChart()),m(6),me(i.store.eliteHistory()?.iterations??rt(45,Rm)),m(3),I(c?.mode===`AUTOCONFIG`?169:-1),m(11),me(i.store.artifacts()?.artifacts??rt(46,Rm))}},dependencies:[Ai,Mm,Uw,Hw,Pt,en,zn,Bn,jn,Ma,Fu,ka,em,Bc,Ju,os,Ni,ca,as,ss,rs,hm,qc,Yc,pm,gm,fm,ds,Om,Xi,En],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh;color:var(--%NS%mat-sys-on-surface);background:radial-gradient(circle at 92% -5%,color-mix(in srgb,var(--%NS%mat-sys-tertiary) 14%,transparent),transparent 28rem),var(--%NS%mat-sys-surface-container-lowest)}.topbar[_ngcontent-%COMP%]{position:sticky;top:0;z-index:20;gap:.75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:color-mix(in srgb,var(--%NS%mat-sys-surface) 94%,transparent);-webkit-backdrop-filter:blur(12px);backdrop-filter:blur(12px)}.brand[_ngcontent-%COMP%]{display:flex;gap:.65rem;align-items:center;font:var(--%NS%mat-sys-title-large);font-weight:750;letter-spacing:-.025em}.brand[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:2rem;height:2rem}.toolbar-spacer[_ngcontent-%COMP%]{flex:1}mat-chip.live[_ngcontent-%COMP%]{--%NS%mdc-chip-elevated-container-color: color-mix(in srgb, var(--%NS%mat-sys-tertiary) 20%, transparent)}.alerts-button[_ngcontent-%COMP%]{margin-right:.5rem}.alert-item[_ngcontent-%COMP%]{display:grid;width:min(24rem,72vw);line-height:1.25}.alert-item[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{overflow:hidden;color:var(--%NS%mat-sys-on-surface-variant);text-overflow:ellipsis}.critical[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}.menu-empty[_ngcontent-%COMP%]{display:block;padding:1rem;color:var(--%NS%mat-sys-on-surface-variant)}.budget-ribbon[_ngcontent-%COMP%]{position:sticky;top:4rem;z-index:15;display:grid;grid-template-columns:minmax(15rem,auto) minmax(12rem,1fr) auto;gap:1.5rem;align-items:center;padding:.65rem max(1rem,(100% - 96rem) / 2);border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:color-mix(in srgb,var(--%NS%mat-sys-surface-container-low) 96%,transparent);-webkit-backdrop-filter:blur(12px);backdrop-filter:blur(12px)}.budget-copy[_ngcontent-%COMP%]{display:flex;gap:.55rem;align-items:baseline;white-space:nowrap}.budget-copy[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-body-small)}.eyebrow[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-small);text-transform:uppercase;letter-spacing:.07em}.phase[_ngcontent-%COMP%]{display:flex;gap:.4rem;align-items:center;font:var(--%NS%mat-sys-label-large);white-space:nowrap}.phase-dot[_ngcontent-%COMP%]{width:.6rem;height:.6rem;border-radius:50%;background:var(--%NS%mat-sys-tertiary);box-shadow:0 0 0 .2rem color-mix(in srgb,var(--%NS%mat-sys-tertiary) 18%,transparent)}.phase-dot.complete[_ngcontent-%COMP%]{background:#079447}main[_ngcontent-%COMP%]{width:min(100% - 2rem,96rem);margin:0 auto;padding-bottom:4rem}.request-error[_ngcontent-%COMP%], .run-error[_ngcontent-%COMP%]{display:flex;gap:.75rem;align-items:center;margin:1rem 0 0;padding:.75rem 1rem;border:1px solid var(--%NS%mat-sys-error);border-radius:.75rem;color:var(--%NS%mat-sys-on-error-container);background:var(--%NS%mat-sys-error-container)}.request-error[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{margin-left:auto}.tab-page[_ngcontent-%COMP%]{display:grid;gap:1rem;padding:1.25rem 0}.overview[_ngcontent-%COMP%]{grid-template-columns:repeat(2,minmax(0,1fr))}.wide[_ngcontent-%COMP%], .overview-actions[_ngcontent-%COMP%]{grid-column:1/-1}.overview-actions[_ngcontent-%COMP%]{display:flex;align-items:center;min-height:2rem;color:var(--%NS%mat-sys-on-surface-variant)}.overview-actions[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{margin-left:auto}.kpi-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(5,minmax(7rem,1fr));gap:.75rem}.kpi[_ngcontent-%COMP%]{display:grid;gap:.2rem;padding:.8rem 1rem;border-left:.25rem solid var(--%NS%mat-sys-primary);border-radius:.4rem;background:var(--%NS%mat-sys-surface-container-low)}.kpi[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-medium)}.kpi[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-headline-small)}.kpi.success[_ngcontent-%COMP%]{border-color:#079447}.kpi.warning[_ngcontent-%COMP%]{border-color:#ed8e00}.kpi.danger[_ngcontent-%COMP%]{border-color:var(--%NS%mat-sys-error)}.elite-list[_ngcontent-%COMP%]{display:grid;gap:.5rem}.elite-row[_ngcontent-%COMP%]{display:grid;grid-template-columns:2.3rem minmax(0,1fr);gap:.75rem;align-items:center;width:100%;padding:.65rem;border:1px solid transparent;border-radius:.5rem;color:inherit;background:transparent;text-align:left;cursor:pointer}.elite-row[_ngcontent-%COMP%]:hover{border-color:var(--%NS%mat-sys-outline-variant);background:var(--%NS%mat-sys-surface-container-low)}.elite-row[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{display:grid;min-width:0}.elite-row[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{overflow:hidden;color:var(--%NS%mat-sys-on-surface-variant);text-overflow:ellipsis;white-space:nowrap}.medal[_ngcontent-%COMP%]{display:grid;place-items:center;width:2rem;height:2rem;border-radius:50%;color:#292929;background:#d1d5db;font-weight:750}.rank-1[_ngcontent-%COMP%]{background:#f2c94c}.rank-2[_ngcontent-%COMP%]{background:#c7ccd4}.rank-3[_ngcontent-%COMP%]{background:#cd8b55}.rank-other[_ngcontent-%COMP%]{background:#e5e7eb}.run-info[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(8rem,auto) minmax(0,1fr);gap:.55rem 1rem;margin:0 0 1rem}.run-info[_ngcontent-%COMP%]   dt[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.run-info[_ngcontent-%COMP%]   dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}.two-column[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem}mat-card-header[_ngcontent-%COMP%]{align-items:center;padding-bottom:.75rem}.summary-scroll[_ngcontent-%COMP%]{max-height:21rem;overflow:auto}.summary-table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse}.summary-table[_ngcontent-%COMP%]   th[_ngcontent-%COMP%], .summary-table[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]{padding:.55rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left}.evaluation-card[_ngcontent-%COMP%]   mat-card-content[_ngcontent-%COMP%]{min-width:0}.history-list[_ngcontent-%COMP%]{display:grid;gap:.5rem}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{display:grid;grid-template-columns:1fr repeat(3,auto);gap:1rem;padding:.75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.artifact-list[_ngcontent-%COMP%]{display:grid}.artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(0,1fr) auto auto;gap:1.5rem;align-items:center;padding:1rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);color:var(--%NS%mat-sys-primary);text-decoration:none}.artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:first-child{display:grid}.artifact-list[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.empty-state[_ngcontent-%COMP%]{padding:1.5rem 0;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}@media(max-width:62rem){.overview[_ngcontent-%COMP%], .two-column[_ngcontent-%COMP%]{grid-template-columns:1fr}.kpi-grid[_ngcontent-%COMP%]{grid-template-columns:repeat(3,1fr)}.budget-ribbon[_ngcontent-%COMP%]{grid-template-columns:1fr auto}.budget-ribbon[_ngcontent-%COMP%]   mat-progress-bar[_ngcontent-%COMP%]{grid-column:1/-1;grid-row:2}}@media(max-width:42rem){.topbar[_ngcontent-%COMP%]   mat-chip-set[_ngcontent-%COMP%]{display:none}.brand[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-medium)}.budget-ribbon[_ngcontent-%COMP%]{top:3.5rem;gap:.5rem}.budget-copy[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child, .eyebrow[_ngcontent-%COMP%]{display:none}main[_ngcontent-%COMP%]{width:min(100% - 1rem,96rem)}.kpi-grid[_ngcontent-%COMP%]{grid-template-columns:repeat(2,1fr)}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%], .artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{grid-template-columns:1fr;gap:.2rem}}`]})};var Fm=class t{http=f(Di);destroy=f(Ne);base=`${In.apiBaseUrl}/api/autollm`;get(n,e={}){return St(this.http.get(this.base+n,{params:e}).pipe(un(this.destroy)))}post(n,e={}){return St(this.http.post(this.base+n,e).pipe(un(this.destroy)))}defaults(){return this.get(`/defaults`)}validate(n){return this.post(`/validate`,n)}probeTarget(n){return this.post(`/targets/probe`,n)}list(){return this.get(`/campaigns`)}create(n){return this.post(`/campaigns`,n)}baselineCheck(n){return this.post(`/campaigns/${encodeURIComponent(n)}/baseline-check`)}action(n,e){return this.post(`/campaigns/${encodeURIComponent(n)}/${e}`)}addTarget(n,e){return St(this.http.put(`${this.base}/campaigns/${encodeURIComponent(n)}/targets/${encodeURIComponent(e.id)}`,e).pipe(un(this.destroy)))}targetAction(n,e,i){let a=`/campaigns/${encodeURIComponent(n)}/targets/${encodeURIComponent(e)}`;return i===`remove`?St(this.http.delete(this.base+a).pipe(un(this.destroy))):this.post(`${a}/${i}`)}progress(n,e){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/progress`)}summary(n){return this.get(`/campaigns/${encodeURIComponent(n)}/summary`)}activity(n,e,i,a){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/activity`,{attempt:i,cursor:a})}comparison(n,e){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/comparison`)}artifacts(n){return this.get(`/campaigns/${encodeURIComponent(n)}/artifacts`)}log(n,e,i){return this.get(`/campaigns/${encodeURIComponent(n)}/log`,{path:e,cursor:i,maximumBytes:65536})}artifactUrl(n,e){return`${this.base}/campaigns/${encodeURIComponent(n)}/artifact?${new URLSearchParams({path:e})}`}screenings(){return this.get(`/screenings`)}createScreening(n,e,i){return this.post(`/screenings`,{config:n,inputPath:e,trainingFraction:i,settings:{sotaFile:n.sotaFile,python:n.execution.ssh?.servers.find(a=>a.id===n.execution.ssh?.screeningServerId)?.environment?.MORK_SELECTOR_PYTHON??`python3`}})}screeningAction(n,e,i){return this.post(`/screenings/${encodeURIComponent(n)}/${e}`,{proposalSha256:i})}screeningReview(n){return this.get(`/screenings/${encodeURIComponent(n)}/review`)}screeningArtifactUrl(n,e){return`${this.base}/screenings/${encodeURIComponent(n)}/artifact?${new URLSearchParams({path:e})}`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Lm=class t{client=f(Fm);destroy=f(Ne);config=T(null);campaigns=T([]);active=T(``);selectedId=T(``);selectedTrial=T(1);campaign=ne(()=>this.campaigns().find(n=>n.id===this.selectedId())??null);trial=ne(()=>this.campaign()?.trials.find(n=>n.number===this.selectedTrial())??null);completed=ne(()=>this.campaign()?.trials.filter(n=>[`SUCCEEDED`,`FAILED`].includes(n.status)).length??0);progress=T(null);activeProgress=T({});targets=ne(()=>Object.entries(this.campaign()?.targets??{}).map(([n,e])=>r({id:n},e)));activeTrials=ne(()=>this.campaign()?.trials.filter(n=>![`PENDING`,`SUCCEEDED`,`FAILED`].includes(n.status))??[]);now=T(Date.now());tuning=ne(()=>this.progress()?.workload?.budget?this.progress().workload:null);generation=ne(()=>this.progress()?.generation??null);generationAttempt=T(null);conversationAttempt=ne(()=>{let n=this.trial()?.attempts?.filter(e=>[`GENERATION`,`REPAIR`].includes(e.stage))??[];return n.find(e=>e.number===this.generationAttempt())??n.at(-1)??null});activity=T([]);screenings=T([]);activeScreening=T(``);selectedScreening=T(``);screening=ne(()=>this.screenings().find(n=>n.id===this.selectedScreening())??null);review=T(null);summary=T(null);comparison=T(null);artifacts=T([]);logPath=T(``);logText=T(``);error=T(null);connectionError=T(null);busy=T(!1);targetStatus=T(null);timer=null;started=!1;destroyed=!1;logOffset=0;activityKey=``;activityCursor=0;activityPending=``;operations=0;constructor(){this.destroy.onDestroy(()=>{this.destroyed=!0,this.timer&&clearTimeout(this.timer)})}async start(){if(!this.started){this.started=!0;try{this.config.set(await this.client.defaults())}catch(n){this.connectionError.set(this.message(n))}await this.poll()}}async poll(){if(!this.destroyed)try{this.config()||this.config.set(await this.client.defaults());let[n,e]=await Promise.all([this.client.list(),this.client.screenings()]);if(this.destroyed)return;this.connectionError.set(null),this.campaigns.set(n.campaigns),this.active.set(n.active),this.screenings.set(e.jobs),this.activeScreening.set(e.active),this.selectedScreening()||this.selectedScreening.set(e.active||e.jobs[0]?.id||``),this.selectedId()||this.selectedId.set(n.active||n.campaigns[0]?.id||``),this.now.set(Date.now());let i=this.selectedId(),a=this.activeTrials(),o=await Promise.allSettled(a.map(s=>this.client.progress(i,s.number)));if(this.selectedId()===i&&!this.destroyed){let s={};o.forEach((c,h)=>{let p=a[h].number;s[p]=c.status===`fulfilled`?{progress:c.value,stale:!1}:{progress:this.activeProgress()[p]?.progress,stale:!0}}),this.activeProgress.set(s)}if(this.trial()){let s=this.selectedId(),c=this.selectedTrial(),[h,p]=await Promise.all([this.activeProgress()[c]?.progress??this.client.progress(s,c),this.client.comparison(s,c)]);this.selectedId()===s&&this.selectedTrial()===c&&!this.destroyed&&(this.progress.set(h),this.comparison.set(p))}let r=this.conversationAttempt();if(r){let s=this.selectedId(),c=this.selectedTrial(),h=`${s}/${c}/${r.number}`;this.activityKey!==h&&(this.activityKey=h,this.activityCursor=0,this.activityPending=``,this.activity.set([]));let p=this.activityCursor,g=await this.client.activity(s,c,r.number,p);if(this.activityKey===h&&this.selectedId()===s&&this.selectedTrial()===c&&this.conversationAttempt()?.number===r.number&&!this.destroyed){g.reset&&(this.activity.set([]),this.activityPending=``);let v=(this.activityPending+g.text).split(`
`);this.activityPending=v.pop()??``;let b=[];for(let y of v)if(y.trim())try{b.push(JSON.parse(y))}catch{}this.activity.update(y=>[...y,...b].slice(-200)),this.activityCursor=g.nextCursor}}if(this.logPath()){let s=this.selectedId(),c=this.logPath(),h=this.logOffset,p=await this.client.log(s,c,h);s===this.selectedId()&&c===this.logPath()&&h===this.logOffset&&!this.destroyed&&(this.logOffset=p.nextCursor,this.logText.update(g=>((p.reset?``:g)+p.text).slice(-262144)))}}catch(n){this.destroyed||this.connectionError.set(this.message(n))}finally{this.destroyed||(this.timer=setTimeout(()=>{this.poll()},2e3))}}select(n){this.selectedId.set(n),this.selectedTrial.set(1),this.artifacts.set([]),this.selectLog(``),this.progress.set(null),this.comparison.set(null),this.watchGeneration(null)}selectTrial(n){this.selectedTrial.set(n),this.progress.set(null),this.comparison.set(null),this.selectLog(``),this.watchGeneration(null)}watchGeneration(n){this.generationAttempt.set(n),this.activityKey=``,this.activity.set([])}selectLog(n){this.logPath.set(n),this.logOffset=0,this.logText.set(``)}selectScreening(n){this.selectedScreening.set(n),this.review.set(null)}async createScreening(n,e){let i=this.config();i&&await this.operate(async()=>{let a=await this.client.createScreening(i,n,e);this.screenings.update(o=>[a,...o]),this.selectScreening(a.id),await this.client.screeningAction(a.id,`resume`),this.activeScreening.set(a.id)})}async reviewScreening(){let n=this.selectedScreening();await this.operate(async()=>{let e=await this.client.screeningReview(n);this.selectedScreening()===n&&this.review.set(s(r({},e),{jobId:n}))})}async screeningAction(n){let e=this.selectedScreening(),i=this.review();n===`approve`&&i?.jobId!==e||await this.operate(async()=>{await this.client.screeningAction(e,n,i?.proposalSha256)})}useSelection(){let n=this.screening();n?.status!==`APPROVED`||!n.mirror||this.config.update(e=>e&&s(r({},e),{trainingPath:`${n.mirror}/proposal/train.index`,comparisonPath:`${n.mirror}/proposal/test.index`}))}screeningArtifactUrl(n){return this.client.screeningArtifactUrl(this.selectedScreening(),n)}async refreshArtifacts(){if(!this.selectedId())return;let n=this.selectedId();try{let e=await this.client.artifacts(n);n===this.selectedId()&&!this.destroyed&&this.artifacts.set(e)}catch(e){this.error.set(this.message(e))}}async createAndStart(){let n=this.config();n&&await this.operate(async()=>{let e=await this.client.validate(n);if(!e.valid)throw new Error(e.errors.join(`; `));let i=await this.client.create(n);this.campaigns.update(o=>[i,...o]),this.select(i.id);let a=await this.client.baselineCheck(i.id);if(this.summary.set(await this.client.summary(i.id)),!a.withinLimit)throw new Error(`Baseline emits ${a.parameters} parameters; limit is ${a.maxParameters}. Prune the search space while preserving every seed before starting this draft.`);await this.client.action(i.id,`start`),this.active.set(i.id)})}async probeTarget(){let n=this.config();n&&await this.operate(async()=>{this.targetStatus.set(await this.client.probeTarget(n))})}async action(n){await this.operate(async()=>{await this.client.action(this.selectedId(),n),(n===`start`||n===`resume`)&&this.active.set(this.selectedId())})}async addTarget(n){let e=this.selectedId();await this.operate(async()=>{let i=await this.client.addTarget(e,n);this.campaigns.update(a=>a.map(o=>o.id===e?i:o))})}async targetAction(n,e){let i=this.selectedId();await this.operate(async()=>{let a=await this.client.targetAction(i,n,e);this.campaigns.update(o=>o.map(r=>r.id===i?a:r))})}targetStale(n){return!n||this.now()-Date.parse(n)>3e4}async operate(n){this.operations++,this.busy.set(!0),this.error.set(null);try{await n()}catch(e){this.error.set(this.message(e))}finally{this.operations--,this.busy.set(this.operations>0)}}artifactUrl(n){return this.client.artifactUrl(this.selectedId(),n)}message(n){let e=n;return e.error?.message??e.message??`AutoLLM request failed`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var sE=()=>[];var e2=()=>[`DRAFT`,`INTERRUPTED`,`CANCELLED`,`NEEDS_ATTENTION`];var t2=()=>[`AWAITING_APPROVAL`,`APPROVED`];var n2=()=>[`instances.csv`,`proposed-retained.index`,`proposed-train.index`,`proposed-test.index`,`manifest.json`];var i2=()=>[`INTERRUPTED`,`CANCELLED`,`NEEDS_ATTENTION`];var a2=()=>[`SUCCEEDED`,`COMPLETED_WITH_FAILURES`];var o2=()=>[`BUSY`,`QUALIFYING`];var r2=()=>[`GENERATION`,`REPAIR`];var zb=(t,n)=>n.id;var s2=(t,n)=>n.instance;var jb=(t,n)=>n.number;var c2=(t,n)=>n.path;var l2=(t,n)=>n.threadId;function d2(t,n){if(t&1&&(l(0,`p`,1),u(1),d()),t&2){let e=_();m(),w(e.store.error())}}function u2(t,n){if(t&1&&(l(0,`p`,1),u(1),d()),t&2){let e=_();m(),w(e.store.connectionError())}}function m2(t,n){if(t&1&&(l(0,`option`,4),u(1),d()),t&2){let e=n.$implicit;S(`value`,e.id),m(),w(e.id)}}function p2(t,n){if(t&1){let e=q();l(0,`fieldset`,25)(1,`legend`),u(2),d(),l(3,`label`),u(4,`ID`),l(5,`input`,2),C(`change`,function(a){let o=N(e).$index;return A(_(3).update(`execution.ssh.servers.`+o+`.id`,a))}),d()(),l(6,`label`),u(7,`SSH host or alias`),l(8,`input`,2),C(`change`,function(a){let o=N(e).$index;return A(_(3).update(`execution.ssh.servers.`+o+`.host`,a))}),d()(),l(9,`label`),u(10,`Remote workspace`),l(11,`input`,2),C(`change`,function(a){let o=N(e).$index;return A(_(3).update(`execution.ssh.servers.`+o+`.workspace`,a))}),d()(),l(12,`label`),u(13,`Java executable`),l(14,`input`,2),C(`change`,function(a){let o=N(e).$index;return A(_(3).update(`execution.ssh.servers.`+o+`.javaExecutable`,a))}),d()(),l(15,`label`),u(16,`Workers override (blank inherits)`),l(17,`input`,12),C(`change`,function(a){let o=N(e).$index;return A(_(3).update(`execution.ssh.servers.`+o+`.workers`,a,!0))}),d()(),l(18,`label`),u(19,`Heap override (blank inherits)`),l(20,`input`,2),C(`change`,function(a){let o=N(e).$index;return A(_(3).update(`execution.ssh.servers.`+o+`.heap`,a))}),d()(),l(21,`button`,26),C(`click`,function(){let a=N(e).$index;return A(_(3).removeServer(a))}),u(22,`Remove from configuration`),d()()}if(t&2){let e=n.$implicit,i=n.$index;m(2),j(`Server `,i+1),m(3),S(`value`,e.id),m(3),S(`value`,e.host),m(3),S(`value`,e.workspace),m(3),S(`value`,e.javaExecutable),m(3),S(`value`,e.workers??``),m(3),S(`value`,e.heap??``)}}function h2(t,n){if(t&1){let e=q();l(0,`label`),u(1,`Screening server`),l(2,`select`,2),C(`change`,function(a){N(e);return A(_(2).update(`execution.ssh.screeningServerId`,a))}),ue(3,m2,2,2,`option`,4,Co),d()(),l(5,`label`),u(6,`Local remote-runner JAR`),l(7,`input`,2),C(`change`,function(a){N(e);return A(_(2).update(`execution.ssh.runnerArtifact`,a))}),d()(),ue(8,p2,23,7,`fieldset`,25,Co),l(10,`button`,26),C(`click`,function(){N(e);return A(_(2).addServer())}),u(11,`Add server`),d()}if(t&2){let e=_();m(2),S(`value`,e.execution.ssh?.screeningServerId),m(),me(e.execution.ssh?.servers??rt(2,sE)),m(4),S(`value`,e.execution.ssh?.runnerArtifact??``),m(),me(e.execution.ssh?.servers??rt(3,sE))}}function f2(t,n){if(t&1){let e=q();l(0,`label`),u(1,`Worker count`),l(2,`input`,11),C(`change`,function(a){N(e);return A(_(2).update(`execution.workers`,a,!0))}),d()()}if(t&2){let e=_();m(2),S(`value`,e.execution.workers)}}function g2(t,n){if(t&1&&(l(0,`details`,20)(1,`summary`),u(2,`Resolved budgets and workload estimate`),d(),l(3,`pre`),u(4),U(5,`json`),d()()),t&2){let e=_(2);m(4),w(ye(5,1,e.store.summary()))}}function b2(t,n){if(t&1&&(l(0,`pre`),u(1),U(2,`json`),d()),t&2){let e=_(2);m(),w(ye(2,1,e.store.targetStatus()))}}function v2(t,n){if(t&1){let e=q();l(0,`mat-card`)(1,`mat-card-content`)(2,`details`,5)(3,`summary`),u(4,`Configure a campaign`),d(),l(5,`fieldset`,6)(6,`div`,7)(7,`label`),u(8,`Where should the actual workloads run?`),l(9,`select`,2),C(`change`,function(a){N(e);return A(_().executionTarget(a))}),l(10,`option`,8),u(11,`Select execution location`),d(),l(12,`option`,9),u(13,`Locally on this computer`),d(),l(14,`option`,10),u(15,`Configure SSH servers`),d()()(),D(16,h2,12,4),l(17,`label`),u(18,`Baseline repository`),l(19,`input`,2),C(`change`,function(a){N(e);return A(_().update(`project.repository`,a))}),d()(),l(20,`label`),u(21,`Git ref`),l(22,`input`,2),C(`change`,function(a){N(e);return A(_().update(`project.ref`,a))}),d()(),l(23,`label`),u(24,`Build command (JSON arguments)`),l(25,`input`,2),U(26,`json`),C(`change`,function(a){N(e);return A(_().buildCommand(a))}),d()(),l(27,`label`),u(28,`Built JAR path`),l(29,`input`,2),C(`change`,function(a){N(e);return A(_().update(`project.artifact`,a))}),d()(),l(30,`label`),u(31,`SOTA component JSON`),l(32,`input`,2),C(`change`,function(a){N(e);return A(_().update(`sotaFile`,a))}),d()(),l(33,`label`),u(34,`Additional seeds (optional)`),l(35,`input`,2),C(`change`,function(a){N(e);return A(_().update(`initialConfigurationsFile`,a))}),d()(),l(36,`label`),u(37,`Training instances`),l(38,`input`,2),C(`change`,function(a){N(e);return A(_().update(`trainingPath`,a))}),d()(),l(39,`label`),u(40,`Comparison instances`),l(41,`input`,2),C(`change`,function(a){N(e);return A(_().update(`comparisonPath`,a))}),d()(),l(42,`label`),u(43,`Independent trials`),l(44,`input`,11),C(`change`,function(a){N(e);return A(_().update(`trials`,a,!0))}),d()(),l(45,`label`),u(46,`Campaign seed`),l(47,`input`,12),C(`change`,function(a){N(e);return A(_().update(`seed`,a,!0))}),d()(),l(48,`label`),u(49,`Parameter limit`),l(50,`input`,13),C(`change`,function(a){N(e);return A(_().update(`maxParameters`,a,!0))}),d()(),l(51,`label`),u(52,`Workers`),l(53,`select`,2),C(`change`,function(a){N(e);return A(_().workerMode(a))}),l(54,`option`,14),u(55,`Auto: half the available processors on the execution host`),d(),l(56,`option`,15),u(57,`Set worker count`),d()()(),D(58,f2,3,1,`label`),l(59,`label`),u(60,`Main evaluations`),l(61,`input`,11),C(`change`,function(a){N(e);return A(_().update(`tuning.evaluations`,a,!0))}),d()(),l(62,`label`),u(63,`Comparison repetitions`),l(64,`input`,11),C(`change`,function(a){N(e);return A(_().update(`comparison.repetitions`,a,!0))}),d()(),l(65,`label`),u(66,`Pilot duration (milliseconds)`),l(67,`input`,11),C(`change`,function(a){N(e);return A(_().update(`preparation.pilotDurationMillis`,a,!0))}),d()(),l(68,`label`),u(69,`Repair turns`),l(70,`input`,16),C(`change`,function(a){N(e);return A(_().update(`preparation.maxRepairs`,a,!0))}),d()(),l(71,`label`),u(72,`Codex model (optional)`),l(73,`input`,2),C(`change`,function(a){N(e);return A(_().update(`codex.model`,a))}),d()(),l(74,`label`),u(75,`Concurrent Codex sessions`),l(76,`input`,11),C(`change`,function(a){N(e);return A(_().update(`codex.maxConcurrentSessions`,a,!0))}),d()(),l(77,`label`),u(78,`Reasoning effort (optional)`),l(79,`input`,2),C(`change`,function(a){N(e);return A(_().update(`codex.reasoningEffort`,a))}),d()(),l(80,`label`),u(81,`Custom prompt file (optional)`),l(82,`input`,2),C(`change`,function(a){N(e);return A(_().update(`codex.promptFile`,a))}),d()()(),l(83,`label`),u(84,`Prompt override (optional)`),l(85,`textarea`,17),C(`change`,function(a){N(e);return A(_().update(`codex.prompt`,a))}),d()(),l(86,`details`)(87,`summary`),u(88,`All settings, including time budgets and timeouts`),d(),l(89,`textarea`,18),U(90,`json`),C(`change`,function(a){N(e);return A(_().advanced(a))}),d()()(),l(91,`p`),u(92,`Each trial starts from the same baseline. Both comparison algorithms restart within their time budget. Ablation is disabled.`),d(),l(93,`p`,19),u(94),U(95,`number`),d(),D(96,g2,6,3,`details`,20),l(97,`details`)(98,`summary`),u(99,`Prepare a screened dataset`),d(),l(100,`div`,7)(101,`label`),u(102,`Full original input pool`),l(103,`input`,21),C(`input`,function(a){N(e);return A(_().originalPool.set(a.target.value))}),d()(),l(104,`label`),u(105,`Training fraction`),l(106,`input`,22),C(`input`,function(a){N(e);return A(_().trainingFraction.set(+a.target.value))}),d()()(),l(107,`p`),u(108,`Screening uses one sequential worker. Review the completed proposal before publishing training/testing indexes. Testing includes all retained instances, including training. Approval permits reuse by qualified servers; measurement provenance remains tied to the screening server.`),d(),l(109,`button`,23),C(`click`,function(){N(e);let a=_();return A(a.store.createScreening(a.originalPool(),a.trainingFraction()))}),u(110,`Prepare dataset`),d()(),l(111,`button`,23),C(`click`,function(){N(e);return A(_().store.probeTarget())}),u(112,`Check execution host`),d(),D(113,b2,3,3,`pre`),l(114,`button`,24),C(`click`,function(){N(e);return A(_().store.createAndStart())}),u(115,`Create and start campaign`),d()()()()}if(t&2){let e=n,i=_();m(2),S(`open`,!i.store.campaign()),m(3),S(`disabled`,!!i.store.active()||i.store.busy()),m(4),S(`value`,i.executionChoice()),m(7),I(e.execution.target===`SSH`?16:-1),m(3),S(`value`,e.project.repository),m(3),S(`value`,e.project.ref),m(3),S(`value`,ye(26,41,e.project.buildCommand)),m(4),S(`value`,e.project.artifact),m(3),S(`value`,e.sotaFile??``),m(3),S(`value`,e.initialConfigurationsFile??``),m(3),S(`value`,e.trainingPath??``),m(3),S(`value`,e.comparisonPath??``),m(3),S(`value`,e.trials),m(3),S(`value`,e.seed),m(3),S(`value`,e.maxParameters),m(3),S(`value`,e.execution.workers<1?`AUTO`:`CUSTOM`),m(5),I(e.execution.workers>0?58:-1),m(3),S(`value`,e.tuning.evaluations),m(3),S(`value`,e.comparison.repetitions),m(3),S(`value`,e.preparation.pilotDurationMillis),m(3),S(`value`,e.preparation.maxRepairs),m(3),S(`value`,e.codex.model??``),m(3),S(`value`,e.codex.maxConcurrentSessions),m(3),S(`value`,e.codex.reasoningEffort??``),m(3),S(`value`,e.codex.promptFile??``),m(3),S(`value`,e.codex.prompt??``),m(4),S(`value`,ye(90,43,e)),m(5),Xd(``,e.trials,` trials · `,ye(95,45,e.tuning.evaluations),` tuning evaluations per trial at `,(e.tuning.ignoreInitialMillis+e.tuning.intervalDurationMillis)/1e3,` seconds · `,e.comparison.repetitions,` paired comparison repetitions at `,e.comparison.timeLimitMillis>0?e.comparison.timeLimitMillis/1e3+` seconds`:`application time limits (resolved during baseline check)`,` · `,e.preparation.pilotDurationMillis/6e4,` minute pilot · `,e.preparation.maxRepairs,` component repairs per trial.`),m(2),I(i.store.summary()?96:-1),m(7),S(`value`,i.originalPool()),m(3),S(`value`,i.trainingFraction()),m(3),S(`disabled`,!i.executionChoice()||!i.originalPool()||!!i.store.active()||!!i.store.activeScreening()||i.store.busy()),m(2),S(`disabled`,!i.executionChoice()||!!i.store.active()||i.store.busy()),m(2),I(i.store.targetStatus()?113:-1),m(),S(`disabled`,!i.executionChoice()||!!i.store.active()||!!i.store.activeScreening()||i.store.busy())}}function _2(t,n){if(t&1&&(l(0,`option`,4),u(1),U(2,`date`),d()),t&2){let e=n.$implicit;S(`value`,e.id),m(),Ot(``,ke(2,4,e.createdAt,`short`),` · `,e.status,` · `,e.id)}}function y2(t,n){if(t&1&&H(0,`mat-progress-bar`,30),t&2){let e=_();S(`value`,100*e.initialCompleted/e.initialTotal)}}function x2(t,n){if(t&1&&H(0,`mat-progress-bar`,30),t&2){let e=_();S(`value`,100*e.confirmationsCompleted/e.confirmationsTotal)}}function C2(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`date`),d(),D(3,y2,1,1,`mat-progress-bar`,30),D(4,x2,1,1,`mat-progress-bar`,30)),t&2){let e=n;m(),oc(`Initial measurements: `,e.initialCompleted,`/`,e.initialTotal,` · Confirmations: `,e.confirmationsCompleted,`/`,e.confirmationsTotal,` · Last activity: `,ke(2,7,e.updatedAt,`mediumTime`)),m(2),I(e.phase===`INITIAL`&&e.initialTotal?3:-1),m(),I(e.phase===`CONFIRMATIONS`&&e.confirmationsTotal?4:-1)}}function S2(t,n){if(t&1&&(l(0,`p`,31),u(1),d(),l(2,`p`),u(3),d()),t&2){let e=n;m(),Ae(``,e.category,` · `,e.message),m(2),w(e.recovery)}}function w2(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`date`),d()),t&2){let e=n;m(),Ot(`Remote job `,e.jobId,` · `,e.status,` · Last sync: `,ke(2,3,e.lastSynchronizedAt,`mediumTime`))}}function E2(t,n){if(t&1){let e=q();l(0,`button`,23),C(`click`,function(){N(e);return A(_(3).store.screeningAction(`resume`))}),u(1,`Resume preparation`),d()}if(t&2){let e=_(3);S(`disabled`,!!e.store.active()||!!e.store.activeScreening()||e.store.busy())}}function D2(t,n){if(t&1){let e=q();l(0,`button`,23),C(`click`,function(){N(e);return A(_(3).store.screeningAction(`cancel`))}),u(1,`Cancel preparation`),d()}if(t&2)S(`disabled`,_(3).store.busy())}function I2(t,n){if(t&1){let e=q();l(0,`button`,23),C(`click`,function(){N(e);return A(_(3).store.reviewScreening())}),u(1,`Review proposal`),d()}if(t&2)S(`disabled`,_(3).store.busy())}function k2(t,n){if(t&1){let e=q();l(0,`button`,26),C(`click`,function(){N(e);return A(_(3).store.useSelection())}),u(1,`Use approved selection`),d()}}function M2(t,n){if(t&1&&(l(0,`p`),u(1),d(),D(2,C2,5,10),D(3,S2,4,3),D(4,w2,3,6,`p`),l(5,`div`,27),D(6,E2,2,1,`button`,28),D(7,D2,2,1,`button`,28),D(8,I2,2,1,`button`,28),D(9,k2,2,0,`button`,29),d()),t&2){let e,i,a,o=n,r=_(2);m(),Ae(``,o.status,` · `,o.progress?.phase||o.activity?.phase||`Waiting to start`),m(),I((e=o.progress)?2:-1,e),m(),I((i=o.problem)?3:-1,i),m(),I((a=o.activity?.remote)?4:-1,a),m(2),I(rt(9,e2).includes(o.status)?6:-1),m(),I(r.store.activeScreening()===o.id||o.status===`NEEDS_ATTENTION`?7:-1),m(),I(rt(10,t2).includes(o.status)?8:-1),m(),I(o.status===`APPROVED`?9:-1)}}function T2(t,n){if(t&1&&(l(0,`a`,33),u(1),d()),t&2){let e=n.$implicit,i=_();S(`href`,_(2).store.screeningArtifactUrl(i.artifactPrefix+`/`+e),hn),m(),w(e)}}function N2(t,n){if(t&1&&(l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),d(),l(7,`td`),u(8),d()()),t&2){let e=n.$implicit;m(2),w(e.instance),m(2),w(e.classification),m(2),Ae(``,e.fastHits,`/`,e.confirmations),m(2),w(e.reason)}}function A2(t,n){if(t&1){let e=q();l(0,`button`,24),C(`click`,function(){N(e);return A(_(3).store.screeningAction(`approve`))}),u(1,`Approve this reviewed selection`),d()}if(t&2){let e=_(),i=_(2);S(`disabled`,i.store.busy()||!!i.store.active()||!!i.store.activeScreening()||!!e.proposal.splitFailure)}}function O2(t,n){if(t&1&&(l(0,`pre`,32),u(1),d(),l(2,`p`),u(3),d(),l(4,`div`,27),ue(5,T2,2,2,`a`,33,Pn),d(),l(7,`details`)(8,`summary`),u(9,`All instance decisions`),d(),l(10,`table`)(11,`thead`)(12,`tr`)(13,`th`),u(14,`Instance`),d(),l(15,`th`),u(16,`Decision`),d(),l(17,`th`),u(18,`Fast confirmations`),d(),l(19,`th`),u(20,`Reason`),d()()(),l(21,`tbody`),ue(22,N2,9,5,`tr`,null,s2),d()()(),D(24,A2,2,1,`button`,34)),t&2){let e=n,i=_(2);m(),w(e.report),m(2),j(`Reviewed proposal SHA-256: `,e.proposalSha256),m(2),me(rt(3,n2)),m(17),me(e.proposal.instances),m(2),I(i.store.screening()?.status===`AWAITING_APPROVAL`?24:-1)}}function R2(t,n){if(t&1){let e=q();l(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),u(3,`Dataset preparation`),d(),l(4,`label`),u(5,`Saved screening`),l(6,`select`,2),C(`change`,function(a){N(e);return A(_().store.selectScreening(a.target.value))}),ue(7,_2,3,7,`option`,4,zb),d()(),D(9,M2,10,11),D(10,O2,25,4),d()()}if(t&2){let e,i,a=_();m(6),S(`value`,a.store.selectedScreening()),m(),me(a.store.screenings()),m(2),I((e=a.store.screening())?9:-1,e),m(),I((i=a.store.review())?10:-1,i)}}function P2(t,n){if(t&1&&(l(0,`option`,4),u(1),U(2,`date`),d()),t&2){let e=n.$implicit;S(`value`,e.id),m(),Ot(``,ke(2,4,e.createdAt,`short`),` — `,e.status,` — `,e.id)}}function F2(t,n){if(t&1){let e=q();l(0,`button`,23),C(`click`,function(){N(e);return A(_(2).store.action(`cancel`))}),u(1,`Cancel campaign`),d()}if(t&2)S(`disabled`,_().status===`CANCELLATION_PENDING`)}function L2(t,n){if(t&1){let e=q();l(0,`button`,24),C(`click`,function(){N(e);return A(_(2).store.action(`resume`))}),u(1,`Resume from checkpoints`),d()}if(t&2){let e=_(2);S(`disabled`,e.store.busy()||!!e.store.activeScreening())}}function V2(t,n){if(t&1){let e=q();l(0,`button`,23),C(`click`,function(){N(e);return A(_(2).store.action(`cancel`))}),u(1,`Reconcile and cancel remote work`),d()}if(t&2)S(`disabled`,_(2).store.busy())}function B2(t,n){if(t&1){let e=q();l(0,`button`,41),C(`click`,function(){N(e);return A(_(2).store.action(`start`))}),u(1,`Start saved draft`),d()}}function j2(t,n){if(t&1){let e=q();l(0,`button`,23),C(`click`,function(){N(e);return A(_(2).store.action(`report`))}),u(1,`Regenerate reports`),d()}if(t&2)S(`disabled`,_(2).store.busy())}function z2(t,n){t&1&&(l(0,`strong`,31),u(1,`Status is stale`),d())}function H2(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`date`),D(3,z2,2,0,`strong`,31),d()),t&2){let e=n,i=_().$implicit,a=_(2);m(),Ae(``,e.status,` · Last sync `,ke(2,3,e.lastSynchronizedAt,`mediumTime`),` `),m(2),I(rt(6,o2).includes(i.status)&&a.store.targetStale(e.lastSynchronizedAt)?3:-1)}}function U2(t,n){if(t&1&&(l(0,`p`,31),u(1),d(),l(2,`p`),u(3),d()),t&2){let e=n;m(),Ae(``,e.category,` · `,e.message),m(2),w(e.recovery)}}function $2(t,n){if(t&1){let e=q();l(0,`button`,23),C(`click`,function(){N(e);let a=_().$implicit;return A(_(2).store.targetAction(a.id,`drain`))}),u(1,`Drain`),d(),l(2,`button`,23),C(`click`,function(){N(e);let a=_().$implicit;return A(_(2).store.targetAction(a.id,`resume`))}),u(3,`Resume server`),d(),l(4,`button`,23),C(`click`,function(){N(e);let a=_().$implicit;return A(_(2).store.targetAction(a.id,`remove`))}),u(5,`Remove idle server`),d()}if(t&2){let e=_().$implicit,i=_(),a=_();S(`disabled`,a.store.busy()||e.draining),m(2),S(`disabled`,a.store.busy()||a.store.active()!==i.id||!!e.assignedTrial),m(2),S(`disabled`,a.store.busy()||!!e.assignedTrial||e.status===`QUALIFYING`)}}function G2(t,n){if(t&1&&(l(0,`article`,37)(1,`h3`),u(2),d(),l(3,`p`),u(4),d(),D(5,H2,4,7,`p`),D(6,U2,4,3),D(7,$2,6,3),d()),t&2){let e,i,a=n.$implicit,o=_();m(2),Ae(``,a.id,` · `,a.status),m(2),Ot(``,a.definition.host||`Local`,` · Trial `,a.assignedTrial??`—`,` · Workers `,a.host?.effectiveWorkers??`Pending qualification`),m(),I((e=a.remote)?5:-1,e),m(),I((i=a.problem)?6:-1,i),m(),I(o.config.execution.target===`SSH`&&a.status!==`REMOVED`?7:-1)}}function W2(t,n){if(t&1){let e=q();l(0,`details`)(1,`summary`),u(2,`Add a server to this campaign`),d(),l(3,`p`),u(4,`New servers qualify automatically while the campaign is active. Existing trial assignments stay on their original server.`),d(),l(5,`label`),u(6,`Server definition (JSON)`),l(7,`textarea`,42),C(`input`,function(a){N(e);return A(_(2).newTarget.set(a.target.value))}),d()(),l(8,`button`,23),C(`click`,function(){N(e);return A(_(2).enrollTarget())}),u(9,`Register server`),d()()}if(t&2){let e=_(2);m(7),S(`value`,e.newTarget()),m(),S(`disabled`,e.store.busy())}}function q2(t,n){t&1&&(l(0,`strong`,31),u(1,`Progress unavailable; showing last update`),d())}function Y2(t,n){if(t&1&&u(0),t&2){let e=n;Ot(` · `,e.state,` · `,e.commandsCompleted,`/`,e.commandsStarted,` commands `)}}function K2(t,n){if(t&1){let e=q();l(0,`p`,38),u(1),D(2,q2,2,0,`strong`,31),D(3,Y2,1,3),l(4,`button`,26),C(`click`,function(){let a=N(e).$implicit;return A(_(2).store.selectTrial(a.number))}),u(5,`Inspect`),d()()}if(t&2){let e,i=n.$implicit,a=_(2);m(),Ot(`Trial `,i.number,` · `,i.targetId,` · `,a.store.activeProgress()[i.number]?.progress?.substage||i.status,` `),m(),I(a.store.activeProgress()[i.number]?.stale?2:-1),m(),I((e=a.store.activeProgress()[i.number]?.progress?.generation)?3:-1,e)}}function Q2(t,n){t&1&&u(0,` Known totals may be incomplete. `)}function X2(t,n){if(t&1&&(l(0,`p`,39),u(1),U(2,`number`),U(3,`number`),U(4,`number`),d(),l(5,`p`),u(6),D(7,Q2,1,0),d()),t&2){let e=n;m(),Si(`Codex token usage · `,e.status,` · Input: `,e.tokens.inputTokens===null?`Unknown`:ye(2,9,e.tokens.inputTokens),` · Output: `,e.tokens.outputTokens===null?`Unknown`:ye(3,11,e.tokens.outputTokens),` · Total: `,e.tokens.totalTokens===null?`Unknown`:ye(4,13,e.tokens.totalTokens)),m(5),Si(``,e.completeAttempts,`/`,e.attempts,` attempts have complete usage; `,e.partialAttempts,` partial, `,e.unavailableAttempts,` unavailable. Includes repairs and failed or interrupted attempts. `),m(),I(e.status===`PARTIAL`?7:-1)}}function Z2(t,n){t&1&&(l(0,`p`,39),u(1,`Codex token usage · UNAVAILABLE`),d())}function J2(t,n){if(t&1&&(l(0,`pre`,31),u(1),d()),t&2){let e=_();m(),w(e.failure)}}function eL(t,n){if(t&1&&(l(0,`p`,31),u(1),d(),l(2,`p`),u(3),d()),t&2){let e=n;m(),Ot(``,e.category,` · `,e.code,` · `,e.message),m(2),w(e.recovery)}}function tL(t,n){if(t&1&&(l(0,`details`)(1,`summary`),u(2,`Report export failed`),d(),l(3,`pre`,31),u(4),d()()),t&2){let e=_();m(4),w(e.reportFailure)}}function nL(t,n){if(t&1){let e=q();l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),d(),l(7,`td`),u(8),d(),l(9,`td`),u(10),d(),l(11,`td`)(12,`button`,26),C(`click`,function(){let a=N(e).$implicit;return A(_(2).store.selectTrial(a.number))}),u(13,`Open trial`),d()()()}if(t&2){let e=n.$implicit;m(2),w(e.number),m(2),w(e.targetId||`Unassigned`),m(2),w(e.status),m(2),w(e.tuningSeed),m(2),w(e.repairTurns)}}function iL(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`number`),d()),t&2){let e=n,i=_();m(),Si(``,e.substage||e.status,` · `,ke(2,4,(e.elapsedMillis||0)/1e3,`1.0-0`),` seconds · Infrastructure retries: `,e.infrastructureRetries||0,` · Component repairs: `,i.repairTurns)}}function aL(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`json`),d()),t&2){let e=_();m(),j(`Reported usage: `,ye(2,1,e.usage))}}function oL(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`date`),d(),l(3,`p`),u(4),d(),D(5,aL,3,3,`p`),l(6,`p`),u(7,`These are observed CLI events. Component repair turns are counted separately; model iteration totals are unavailable.`),d()),t&2){let e=n;m(),Ot(`Generation: `,e.state,` · Session: `,e.threadId||`Waiting for session event`,` · Last activity: `,e.lastActivityAt?ke(2,9,e.lastActivityAt,`mediumTime`):`No events received`),m(3),oc(`Turns started: `,e.turns,` · Commands: `,e.commandsCompleted,` completed / `,e.commandsStarted,` started · Edit events: `,e.edits,` · Assistant messages: `,e.messages),m(),I(e.usage?5:-1)}}function rL(t,n){if(t&1&&(l(0,`article`)(1,`small`),u(2),U(3,`date`),d(),l(4,`pre`),u(5),d()()),t&2){let e=n.$implicit;m(2),Ae(``,ke(3,3,e.timestamp,`mediumTime`),` · `,e.type),m(3),w(e.text)}}function sL(t,n){t&1&&(l(0,`p`),u(1,`Waiting for conversation events. Raw stdout and stderr remain available below.`),d())}function cL(t,n){if(t&1){let e=q();l(0,`details`,43)(1,`summary`),u(2),d(),l(3,`div`,45),ue(4,rL,6,6,`article`,null,Co,!1,sL,2,0,`p`),d(),l(7,`button`,26),C(`click`,function(){let a=N(e);return A(_(3).store.selectLog(a.directory+`/stdout.log`))}),u(8,`Raw stdout`),d(),l(9,`button`,26),C(`click`,function(){let a=N(e);return A(_(3).store.selectLog(a.directory+`/stderr.log`))}),u(10,`Raw stderr`),d()()}if(t&2){let e=n,i=_(3);m(2),Ot(`Conversation · `,e.stage,` attempt `,e.number,` · `,e.status),m(2),me(i.store.activity())}}function lL(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`number`),U(3,`number`),d()),t&2){let e=n;m(),Ot(`Trial token usage · `,e.status,` · Input: `,e.tokens.inputTokens===null?`Unknown`:ye(2,3,e.tokens.inputTokens),` · Output: `,e.tokens.outputTokens===null?`Unknown`:ye(3,5,e.tokens.outputTokens))}}function dL(t,n){if(t&1&&(l(0,`p`),u(1),d(),H(2,`mat-progress-bar`,30)),t&2){let e=n;m(),Qd(``,e.phase,` · `,e.budget.used,`/`,e.budget.maximum,` evaluations · `,e.evaluations.succeeded,` succeeded · `,e.evaluations.rejected,` rejected · `,e.evaluations.failed,` failed`),m(),S(`value`,100*e.budget.used/e.budget.maximum)}}function uL(t,n){if(t&1&&(l(0,`pre`,31),u(1),d()),t&2){let e=_();m(),w(e.failure)}}function mL(t,n){if(t&1){let e=q();l(0,`button`,26),C(`click`,function(){N(e);let a=_().$implicit;return A(_(3).store.watchGeneration(a.number))}),u(1,`View conversation`),d()}}function pL(t,n){if(t&1&&(l(0,`a`,33),u(1),d()),t&2){let e=n.$implicit,i=_(2).$implicit;S(`href`,_(3).store.artifactUrl(i.directory+`/`+e.path),hn),m(),j(`Download conversation `,e.threadId)}}function hL(t,n){if(t&1&&(l(0,`span`),u(1),d(),ue(2,pL,2,2,`a`,33,l2)),t&2){let e=n;m(),Ae(`Conversation: `,e.status,` · Usage: `,e.usage.status),m(),me(e.conversations)}}function fL(t,n){if(t&1&&(l(0,`details`)(1,`summary`),u(2,`Failure details`),d(),l(3,`pre`),u(4),d()()),t&2){let e=_().$implicit;m(4),w(e.failure)}}function gL(t,n){if(t&1){let e=q();l(0,`div`,44)(1,`span`),u(2),U(3,`date`),d(),l(4,`button`,26),C(`click`,function(){let a=N(e).$implicit;return A(_(3).store.selectLog(a.directory+([`PREPARATION`].includes(a.stage)?`/build`:``)+`/stdout.log`))}),u(5,`View log`),d(),D(6,mL,2,0,`button`,29),D(7,hL,4,2),D(8,fL,5,1,`details`),d()}if(t&2){let e,i=n.$implicit;m(2),Ot(``,i.stage,` · `,i.status,` · `,ke(3,6,i.startedAt,`short`)),m(4),I(rt(9,r2).includes(i.stage)?6:-1),m(),I((e=i.codexEvidence)?7:-1,e),m(),I(i.failure?8:-1)}}function bL(t,n){if(t&1&&(l(0,`h3`),u(1,`First-ranked proposal: components and parameters`),d(),l(2,`p`),u(3,`Generated algorithm components end in New.`),d(),l(4,`pre`),u(5),U(6,`json`),d()),t&2){let e=_();m(5),w(ye(6,1,e.winner))}}function vL(t,n){if(t&1&&(l(0,`details`,20)(1,`summary`),u(2,`Comparison results`),d(),l(3,`pre`),u(4),U(5,`json`),d()()),t&2){let e=_(3);m(4),w(ye(5,1,e.comparisonSummary()))}}function _L(t,n){if(t&1&&(l(0,`option`,4),u(1),d()),t&2){let e=n.$implicit;S(`value`,e),m(),w(e)}}function yL(t,n){if(t&1&&(l(0,`option`,4),u(1),d()),t&2){let e=n.$implicit;S(`value`,e),m(),w(e)}}function xL(t,n){if(t&1){let e=q();l(0,`div`,7)(1,`label`),u(2,`Convergence instance`),l(3,`select`,2),C(`change`,function(a){N(e);return A(_(3).selectedInstance.set(a.target.value))}),ue(4,_L,2,2,`option`,4,Pn),d()(),l(6,`label`),u(7,`Objective`),l(8,`select`,2),C(`change`,function(a){N(e);return A(_(3).selectedObjective.set(a.target.value))}),ue(9,yL,2,2,`option`,4,Pn),d()()(),H(11,`app-dashboard-chart`,46)}if(t&2){let e=_(3);m(3),S(`value`,e.selectedInstance()||e.instances()[0]),m(),me(e.instances()),m(4),S(`value`,e.selectedObjective()||e.objectives()[0]),m(),me(e.objectives()),m(2),S(`model`,e.curveChart())}}function CL(t,n){if(t&1&&(l(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),u(3),d(),D(4,iL,3,7,`p`),D(5,oL,8,12),D(6,cL,11,4,`details`,43),D(7,lL,4,7,`p`),D(8,dL,3,7),D(9,uL,2,1,`pre`,31),ue(10,gL,9,10,`div`,44,jb),D(12,bL,7,3),D(13,vL,6,3,`details`,20),D(14,xL,12,3),d()()),t&2){let e,i,a,o,r,s=n,c=_(2);m(3),Ae(`Trial `,s.number,` · `,s.status),m(),I((e=c.store.progress())?4:-1,e),m(),I((i=c.store.generation())?5:-1,i),m(),I((a=c.store.conversationAttempt())?6:-1,a),m(),I((o=s.codexUsage)?7:-1,o),m(),I((r=c.store.tuning())?8:-1,r),m(),I(s.failure?9:-1),m(),me(s.attempts),m(2),I(s.winner?12:-1),m(),I(c.store.comparison()?13:-1),m(),I(c.instances().length?14:-1)}}function SL(t,n){if(t&1&&(l(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),u(3),d(),l(4,`pre`,47),u(5),d()()()),t&2){let e=_(2);m(3),w(e.store.logPath()),m(2),w(e.store.logText())}}function wL(t,n){if(t&1){let e=q();l(0,`button`,26),C(`click`,function(){N(e);let a=_().$implicit;return A(_(2).store.selectLog(a.path))}),u(1,`View log`),d()}}function EL(t,n){if(t&1&&(l(0,`div`,40)(1,`a`,33),u(2),d(),l(3,`span`),u(4),d(),D(5,wL,2,0,`button`,29),d()),t&2){let e=n.$implicit,i=_(2);m(),S(`href`,i.store.artifactUrl(e.path),hn),m(),w(e.path),m(2),j(``,e.size,` bytes`),m(),I(e.path.endsWith(`.log`)||e.path.endsWith(`.jsonl`)?5:-1)}}function DL(t,n){if(t&1){let e=q();l(0,`mat-card`)(1,`mat-card-content`)(2,`div`,27)(3,`strong`),u(4),d(),D(5,F2,2,1,`button`,28),D(6,L2,2,1,`button`,34),D(7,V2,2,1,`button`,28),D(8,B2,2,0,`button`,35),D(9,j2,2,1,`button`,28),d(),H(10,`mat-progress-bar`,30),l(11,`h2`),u(12),d(),l(13,`p`),u(14,`Each server runs one trial at a time. SOTA and proposal measurements stay paired on that server; reports are grouped by server.`),d(),l(15,`div`,36),ue(16,G2,8,8,`article`,37,zb),d(),D(18,W2,10,2,`details`),ue(19,K2,6,5,`p`,38,jb),D(21,X2,8,15)(22,Z2,2,0,`p`,39),D(23,J2,2,1,`pre`,31),D(24,eL,4,4),D(25,tL,5,1,`details`),l(26,`table`)(27,`thead`)(28,`tr`)(29,`th`),u(30,`Trial`),d(),l(31,`th`),u(32,`Server`),d(),l(33,`th`),u(34,`Stage / outcome`),d(),l(35,`th`),u(36,`Tuning seed`),d(),l(37,`th`),u(38,`Repairs`),d(),l(39,`th`),u(40,`Inspect`),d()()(),l(41,`tbody`),ue(42,nL,14,5,`tr`,null,jb),d()()()(),D(44,CL,15,11,`mat-card`),D(45,SL,6,2,`mat-card`),l(46,`mat-card`)(47,`mat-card-content`)(48,`div`,27)(49,`h2`),u(50,`Saved data and artifacts`),d(),l(51,`button`,26),C(`click`,function(){N(e);return A(_().store.refreshArtifacts())}),u(52,`Refresh artifacts`),d()(),ue(53,EL,6,4,`div`,40,c2),l(55,`details`)(56,`summary`),u(57,`Frozen campaign settings and versions`),d(),l(58,`pre`),u(59),U(60,`json`),d(),l(61,`pre`),u(62),U(63,`json`),d()()()()}if(t&2){let e,i,a,o=n,r=_();m(4),Ot(``,o.status,` · `,r.store.completed(),`/`,o.trials.length,` trials finished`),m(),I(r.store.active()===o.id?5:-1),m(),I(rt(23,i2).includes(o.status)&&!r.store.active()?6:-1),m(),I(o.status===`NEEDS_ATTENTION`?7:-1),m(),I(o.status===`DRAFT`&&!r.store.active()?8:-1),m(),I(o.status!==`DRAFT`&&!r.store.active()?9:-1),m(),S(`value`,100*r.store.completed()/o.trials.length),m(2),j(`Servers · pool revision `,o.poolRevision),m(4),me(r.store.targets()),m(2),I(o.config.execution.target===`SSH`&&!rt(24,a2).includes(o.status)?18:-1),m(),me(r.store.activeTrials()),m(2),I((e=o.codexUsage)?21:22,e),m(2),I(o.failure?23:-1),m(),I((i=o.problem)?24:-1,i),m(),I(o.reportFailure?25:-1),m(17),me(o.trials),m(2),I((a=r.store.trial())?44:-1,a),m(),I(r.store.logPath()?45:-1),m(8),me(r.store.artifacts()),m(6),w(ye(60,19,o.config)),m(3),w(ye(63,21,o.versions))}}var Vm=class t$1{store=f(Lm);selectedInstance=T(``);selectedObjective=T(``);executionChoice=T(``);newTarget=T(`{"id":"server-2","host":"user@host","workspace":"/home/user/autollm","javaExecutable":"java","environment":{}}`);originalPool=T(``);trainingFraction=T(.15);curves=ne(()=>this.store.comparison()?.curves??[]);instances=ne(()=>[...new Set(this.curves().map(n=>n.instance))]);objectives=ne(()=>[...new Set(this.curves().flatMap(n=>Object.keys(n.curves)))]);comparisonSummary=ne(()=>{let i=this.store.comparison()??{},{curves:n}=i;return t(i,[`curves`])});curveChart=ne(()=>{let n=this.instances().includes(this.selectedInstance())?this.selectedInstance():this.instances()[0],e=this.objectives().includes(this.selectedObjective())?this.selectedObjective():this.objectives()[0];return{options:{chart:{type:`line`,height:360},title:{text:e??`Comparison convergence`},xAxis:{title:{text:`Time (milliseconds)`}},yAxis:{title:{text:`Objective value`}},series:this.curves().filter(i=>i.instance===n).map(i=>({type:`line`,name:`${i.algorithm} \xB7 repetition ${Number(i.iteration)+1}`,data:(i.curves[e]??[]).filter(a=>Number.isFinite(a.value)).map(a=>[a.instant/1e6,a.value])}))}}});constructor(){this.store.start()}executionTarget(n){let e=n.target.value;this.executionChoice.set(e),this.store.config.update(i=>i&&s(r({},i),{execution:s(r({},i.execution),{target:e,ssh:i.execution.ssh??{screeningServerId:`server-1`,runnerArtifact:null,servers:[{id:`server-1`,host:``,workspace:``,javaExecutable:`java`,environment:{}}]}})})),this.store.targetStatus.set(null)}addServer(){this.store.config.update(n=>{if(!n?.execution.ssh)return n;let e=[...n.execution.ssh.servers,{id:`server-${n.execution.ssh.servers.length+1}`,host:``,workspace:``,javaExecutable:`java`,environment:{}}];return s(r({},n),{execution:s(r({},n.execution),{ssh:s(r({},n.execution.ssh),{servers:e})})})})}removeServer(n){this.store.config.update(e=>e?.execution.ssh?s(r({},e),{execution:s(r({},e.execution),{ssh:s(r({},e.execution.ssh),{servers:e.execution.ssh.servers.filter((i,a)=>a!==n)})})}):e)}enrollTarget(){try{let n=JSON.parse(this.newTarget());if(!n?.id||!n.host||!n.workspace)throw new Error(`Provide server id, host, and workspace`);this.store.addTarget(n)}catch(n){this.store.error.set(n.message)}}workerMode(n){let e=n.target.value===`AUTO`;this.store.config.update(i=>i&&s(r({},i),{execution:s(r({},i.execution),{workers:e?-1:1})}))}update(n,e,i=!1){let a=this.store.config();if(!a)return;let o=structuredClone(a),r=o,s=n.split(`.`);for(let h of s.slice(0,-1))(!r[h]||typeof r[h]!=`object`)&&(r[h]={}),r=r[h];let c=e.target.value;r[s[s.length-1]]=i?c?Number(c):null:c||null,this.store.config.set(o)}buildCommand(n){try{let e=JSON.parse(n.target.value);if(!Array.isArray(e)||e.some(i=>typeof i!=`string`))throw new Error(`Build command must be a JSON array of arguments`);this.store.config.update(i=>i&&s(r({},i),{project:s(r({},i.project),{buildCommand:e})}))}catch(e){this.store.error.set(e.message)}}advanced(n){try{let e=this.store.config();if(!e)return;let i=JSON.parse(n.target.value);if(!i||typeof i!=`object`||Array.isArray(i))throw new Error(`Settings must be a JSON object`);for(let o of[`project`,`codex`,`preparation`,`tuning`,`comparison`,`execution`])if(i[o]!=null&&(typeof i[o]!=`object`||Array.isArray(i[o])))throw new Error(`${o} must be a JSON object`);let a=i.execution?.ssh;if(a!=null&&(typeof a!=`object`||Array.isArray(a)))throw new Error(`execution.ssh must be a JSON object`);this.store.config.set(s(r(r({},e),i),{project:r(r({},e.project),i.project),codex:r(r({},e.codex),i.codex),preparation:r(r({},e.preparation),i.preparation),tuning:r(r({},e.tuning),i.tuning),comparison:r(r({},e.comparison),i.comparison),execution:s(r(r({},e.execution),i.execution),{ssh:a===null?null:e.execution.ssh||a?r(r({screeningServerId:``,runnerArtifact:null,servers:[]},e.execution.ssh),a):null})})),i.execution?.target&&this.executionChoice.set(i.execution.target),this.store.error.set(null)}catch(e){this.store.error.set(e.message)}}static ɵfac=function(e){return new(e||t$1)};static ɵcmp=B({type:t$1,selectors:[[`app-autollm-dashboard`]],decls:20,vars:6,consts:[[`src`,`favicon.svg`,`alt`,`Mork`],[`role`,`alert`,1,`error`],[3,`change`,`value`],[`value`,``],[3,`value`],[3,`open`],[3,`disabled`],[1,`fields`],[`value`,``,`disabled`,``],[`value`,`LOCAL`],[`value`,`SSH`],[`type`,`number`,`min`,`1`,3,`change`,`value`],[`type`,`number`,3,`change`,`value`],[`type`,`number`,`min`,`1`,`max`,`299`,3,`change`,`value`],[`value`,`AUTO`],[`value`,`CUSTOM`],[`type`,`number`,`min`,`0`,3,`change`,`value`],[`rows`,`6`,3,`change`,`value`],[`rows`,`16`,3,`change`,`value`],[1,`budget-summary`],[`open`,``],[3,`input`,`value`],[`type`,`number`,`min`,`0.01`,`max`,`0.99`,`step`,`0.01`,3,`input`,`value`],[`mat-button`,``,3,`click`,`disabled`],[`mat-flat-button`,``,3,`click`,`disabled`],[1,`server-definition`],[`mat-button`,``,3,`click`],[1,`actions`],[`mat-button`,``,3,`disabled`],[`mat-button`,``],[`mode`,`determinate`,3,`value`],[1,`error`],[1,`screening-report`],[3,`href`],[`mat-flat-button`,``,3,`disabled`],[`mat-flat-button`,``],[1,`target-grid`],[1,`target-card`],[1,`active-trial`],[1,`codex-usage`],[1,`artifact`],[`mat-flat-button`,``,3,`click`],[`rows`,`5`,3,`input`,`value`],[`open`,``,1,`conversation`],[1,`attempt`],[`aria-live`,`polite`,1,`activity-feed`],[3,`model`],[1,`logs`]],template:function(e,i){if(e&1&&(l(0,`main`)(1,`header`),H(2,`img`,0),l(3,`div`)(4,`h1`),u(5,`AutoLLM campaigns`),d(),l(6,`p`),u(7,`Generate components, tune algorithms, and compare each independent proposal against SOTA.`),d()()(),D(8,d2,2,1,`p`,1),D(9,u2,2,1,`p`,1),D(10,v2,116,47,`mat-card`),D(11,R2,11,3,`mat-card`),l(12,`label`),u(13,`Saved campaign`),l(14,`select`,2),C(`change`,function(o){return i.store.select(o.target.value)}),l(15,`option`,3),u(16,`Select campaign`),d(),ue(17,P2,3,7,`option`,4,zb),d()(),D(19,DL,64,25),d()),e&2){let a,o;m(8),I(i.store.error()?8:-1),m(),I(i.store.connectionError()?9:-1),m(),I((a=i.store.config())?10:-1,a),m(),I(i.store.screenings().length?11:-1),m(3),S(`value`,i.store.selectedId()),m(3),me(i.store.campaigns()),m(2),I((o=i.store.campaign())?19:-1,o)}},dependencies:[Pt,en,zn,Bn,jn,ss,rs,Ai,wo,Xi,En],styles:[`[_nghost-%COMP%]{display:block;background:var(--%NS%mat-sys-surface);color:var(--%NS%mat-sys-on-surface);min-height:100vh}main[_ngcontent-%COMP%]{max-width:88rem;margin:auto;padding:1.5rem;display:grid;gap:1rem}header[_ngcontent-%COMP%], .actions[_ngcontent-%COMP%], .artifact[_ngcontent-%COMP%]{display:flex;align-items:center;flex-wrap:wrap;gap:1rem}header[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:3rem}fieldset[_ngcontent-%COMP%]{border:0;padding:1rem 0}.fields[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(auto-fit,minmax(16rem,1fr));gap:1rem}label[_ngcontent-%COMP%]{display:flex;flex-direction:column;gap:.4rem;font-weight:500;margin-bottom:.6rem}input[_ngcontent-%COMP%], textarea[_ngcontent-%COMP%], select[_ngcontent-%COMP%]{padding:.6rem;background:var(--%NS%mat-sys-surface-container);color:inherit;border:1px solid var(--%NS%mat-sys-outline);border-radius:.4rem;font:inherit}textarea[_ngcontent-%COMP%]{width:100%;box-sizing:border-box}summary[_ngcontent-%COMP%]{cursor:pointer;padding:.5rem 0}table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse;margin:1rem 0}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{text-align:left;padding:.6rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}pre[_ngcontent-%COMP%]{white-space:pre-wrap;overflow-wrap:anywhere;overflow:auto;max-height:25rem;padding:.75rem;background:var(--%NS%mat-sys-surface-container)}.logs[_ngcontent-%COMP%]{white-space:pre}.error[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}.attempt[_ngcontent-%COMP%]{padding:.5rem 0}.artifact[_ngcontent-%COMP%]{padding:.25rem 0}.artifact[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{overflow-wrap:anywhere}.activity-feed[_ngcontent-%COMP%]{max-height:32rem;overflow:auto;padding:.75rem;background:var(--%NS%mat-sys-surface-container-low)}.activity-feed[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{margin-bottom:1rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}.activity-feed[_ngcontent-%COMP%]   pre[_ngcontent-%COMP%], .screening-report[_ngcontent-%COMP%]{white-space:pre-wrap;overflow-wrap:anywhere}.budget-summary[_ngcontent-%COMP%]{line-height:1.6}.target-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:1rem;margin-block:1rem}.target-card[_ngcontent-%COMP%], .server-definition[_ngcontent-%COMP%]{border:1px solid rgba(119,119,119,.3333333333);padding:1rem;border-radius:.5rem}.active-trial[_ngcontent-%COMP%]{border-bottom:1px solid rgba(119,119,119,.3333333333);padding-block:.5rem}`]})};var Bm=class t{client=f(Fa);destroyRef=f(Ne);viewMode=T(`detecting`);latestError=T(null);timer=null;started=!1;destroyed=!1;polling=!1;retry=!1;delay=500;constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.timer&&clearTimeout(this.timer)})}start(){this.started||(this.started=!0,this.poll())}retryNow(){this.timer&&clearTimeout(this.timer),this.polling?this.retry=!0:this.poll()}async poll(){if(this.destroyed)return;this.polling=!0;let n=1e4;try{let e=await this.client.getMode();if(this.destroyed)return;this.viewMode.set(e.mode.toLowerCase()),this.latestError.set(null),this.delay=500,e.mode===`DETECTING`&&(n=500)}catch(e){if(this.destroyed)return;this.latestError.set(e instanceof Error?e.message:`Could not contact the backend`),this.viewMode()===`detecting`&&this.viewMode.set(`unavailable`),n=this.delay,this.delay=Math.min(3e4,this.delay*2)}finally{this.polling=!1,this.destroyed||(this.timer=setTimeout(()=>{this.poll()},this.retry?0:n),this.retry=!1)}}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};function IL(t,n){t&1&&H(0,`app-autollm-dashboard`)}function kL(t,n){t&1&&H(0,`app-standard-dashboard`)}function ML(t,n){t&1&&H(0,`app-tuning-dashboard`)}function TL(t,n){t&1&&(l(0,`main`,0),H(1,`img`,2),l(2,`mat-card`,3)(3,`mat-card-header`)(4,`mat-card-title`),u(5,`Autoconfig worker`),d()(),l(6,`mat-card-content`)(7,`p`),u(8,`This process executes evaluations for the coordinator.`),d(),l(9,`p`),u(10,`The monitoring glass panel is available on the coordinator process.`),d()()()())}function NL(t,n){t&1&&(l(0,`main`,0),H(1,`img`,2),l(2,`mat-card`)(3,`mat-card-content`)(4,`h1`),u(5,`Instance utility`),d(),l(6,`p`),u(7,`This process is analysing or selecting instances. It is not running a measured experiment.`),d()()()())}function AL(t,n){if(t&1){let e=q();l(0,`main`,0),H(1,`img`,2),l(2,`mat-card`,3)(3,`mat-card-header`)(4,`mat-card-title`),u(5,`Waiting for the Mork backend`),d()(),l(6,`mat-card-content`)(7,`p`),u(8),d(),l(9,`button`,4),C(`click`,function(){N(e);return A(_().store.retryNow())}),u(10,`Retry now`),d()()()()}if(t&2){let e=_();m(8),w(e.store.latestError()??`The run mode could not be detected.`)}}function OL(t,n){t&1&&(l(0,`main`,1),H(1,`img`,2)(2,`mat-spinner`,5),l(3,`p`),u(4,`Detecting experiment mode…`),d()())}yg(class t{store=f(Bm);ngOnInit(){this.store.start()}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-root`]],decls:7,vars:1,consts:[[1,`mode-state`],[`aria-label`,`Detecting experiment mode`,1,`mode-state`,`detecting`],[`src`,`favicon.svg`,`alt`,`Mork`],[`appearance`,`outlined`],[`mat-flat-button`,``,`type`,`button`,3,`click`],[`diameter`,`36`]],template:function(e,i){if(e&1&&D(0,IL,1,0,`app-autollm-dashboard`)(1,kL,1,0,`app-standard-dashboard`)(2,ML,1,0,`app-tuning-dashboard`)(3,TL,11,0,`main`,0)(4,NL,8,0,`main`,0)(5,AL,11,1,`main`,0)(6,OL,5,0,`main`,1),e&2){let a;I((a=i.store.viewMode())===`autollm`?0:a===`standard`?1:a===`tuning`?2:a===`worker`?3:a===`utility`?4:a===`unavailable`?5:6)}},dependencies:[Pt,en,zn,Bn,jn,Ma,ka,sS,rS,ym,Pm,Vm],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh}.mode-state[_ngcontent-%COMP%]{display:grid;place-content:center;min-height:100vh;padding:1rem;color:var(--%NS%mat-sys-on-surface);background:radial-gradient(circle at 50% 20%,color-mix(in srgb,var(--%NS%mat-sys-tertiary) 16%,transparent),transparent 25rem),var(--%NS%mat-sys-surface-container-lowest)}.mode-state[_ngcontent-%COMP%] > img[_ngcontent-%COMP%]{width:5rem;height:5rem;margin:0 auto 1.25rem}.mode-state[_ngcontent-%COMP%]   mat-card[_ngcontent-%COMP%]{width:min(32rem,100vw - 2rem)}.mode-state.detecting[_ngcontent-%COMP%]{justify-items:center;color:var(--%NS%mat-sys-on-surface-variant)}.mode-state.detecting[_ngcontent-%COMP%] > img[_ngcontent-%COMP%]{margin-bottom:1.5rem}`]})},CC).catch(t=>console.error(t));export{s as n,r as t};