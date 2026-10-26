var k$1=Object.create;var g=Object.defineProperty;var l$1=Object.defineProperties;var m=Object.getOwnPropertyDescriptor;var n=Object.getOwnPropertyDescriptors;var o=Object.getOwnPropertyNames;var f$1=Object.getOwnPropertySymbols;var p=Object.getPrototypeOf;var h$1=Object.prototype.hasOwnProperty;var j=Object.prototype.propertyIsEnumerable;var i=(a,b,c)=>b in a?g(a,b,{enumerable:!0,configurable:!0,writable:!0,value:c}):a[b]=c;var r=(a,b)=>{for(var c in b||={})h$1.call(b,c)&&i(a,c,b[c]);if(f$1)for(var c of f$1(b))j.call(b,c)&&i(a,c,b[c]);return a};var s=(a,b)=>l$1(a,n(b));var t=(a,b)=>{var c={};for(var d in a)h$1.call(a,d)&&b.indexOf(d)<0&&(c[d]=a[d]);if(a!=null&&f$1)for(var d of f$1(a))b.indexOf(d)<0&&j.call(a,d)&&(c[d]=a[d]);return c};var u$1=(a,b)=>()=>{try{return b||a((b={exports:{}}).exports,b),b.exports}catch(c){throw b=0,c}};var q$1=(a,b,c,d)=>{if(b&&typeof b==`object`||typeof b==`function`)for(let e of o(b))!h$1.call(a,e)&&e!==c&&g(a,e,{get:()=>b[e],enumerable:!(d=m(b,e))||d.enumerable});return a};var v=(a,b,c)=>(c=a!=null?k$1(p(a)):{},q$1(b||!a||!a.__esModule?g(c,`default`,{value:a,enumerable:!0}):c,a));var Lw=u$1((bm,Fw)=>{"use strict";(function(t,n){typeof bm==`object`&&typeof Fw<`u`?n(bm):typeof define==`function`&&define.amd?define([`exports`],n):(t=typeof globalThis<`u`?globalThis:t||self,n(t.StompJs={}))})(bm,(function(t){"use strict";function n(j,x){j.terminate=function(){let M=()=>{};this.onerror=M,this.onmessage=M,this.onopen=M;let T=new Date,$=Math.random().toString().substring(2,8),ye=this.onclose;this.onclose=gt=>{let We=new Date().getTime()-T.getTime();x(`Discarded socket (#${$})  closed after ${We}ms, with code/reason: ${gt.code}/${gt.reason}`)},this.close(),ye?.call(j,{code:4001,reason:`Quick discarding socket (#${$}) without waiting for the shutdown sequence.`,wasClean:!1})}}let e={LF:`
`,NULL:`\0`};class i{get body(){return!this._body&&this.isBinaryBody&&(this._body=new TextDecoder().decode(this._binaryBody)),this._body||``}get binaryBody(){return!this._binaryBody&&!this.isBinaryBody&&(this._binaryBody=new TextEncoder().encode(this._body)),this._binaryBody}constructor(x){let{command:M,headers:T,body:$,binaryBody:ye,escapeHeaderValues:gt,skipContentLengthHeader:We}=x;this.command=M,this.headers=Object.assign({},T||{}),ye?(this._binaryBody=ye,this.isBinaryBody=!0):(this._body=$||``,this.isBinaryBody=!1),this.escapeHeaderValues=gt||!1,this.skipContentLengthHeader=We||!1}static fromRawFrame(x,M){let T={},$=ye=>ye.replace(/^\s+|\s+$/g,``);for(let ye of x.headers.reverse()){ye.indexOf(`:`);let gt=$(ye[0]),We=$(ye[1]);M&&x.command!==`CONNECT`&&x.command!==`CONNECTED`&&(We=i.hdrValueUnEscape(We)),T[gt]=We}return new i({command:x.command,headers:T,binaryBody:x.binaryBody,escapeHeaderValues:M})}toString(){return this.serializeCmdAndHeaders()}serialize(){let x=this.serializeCmdAndHeaders();return this.isBinaryBody?i.toUnit8Array(x,this._binaryBody).buffer:x+this._body+e.NULL}serializeCmdAndHeaders(){let x=[this.command];this.skipContentLengthHeader&&delete this.headers[`content-length`];for(let M of Object.keys(this.headers||{})){let T=this.headers[M];this.escapeHeaderValues&&this.command!==`CONNECT`&&this.command!==`CONNECTED`?x.push(`${M}:${i.hdrValueEscape(`${T}`)}`):x.push(`${M}:${T}`)}return(this.isBinaryBody||!this.isBodyEmpty()&&!this.skipContentLengthHeader)&&x.push(`content-length:${this.bodyLength()}`),x.join(e.LF)+e.LF+e.LF}isBodyEmpty(){return this.bodyLength()===0}bodyLength(){let x=this.binaryBody;return x?x.length:0}static sizeOfUTF8(x){return x?new TextEncoder().encode(x).length:0}static toUnit8Array(x,M){let T=new TextEncoder().encode(x),$=new Uint8Array([0]),ye=new Uint8Array(T.length+M.length+$.length);return ye.set(T),ye.set(M,T.length),ye.set($,T.length+M.length),ye}static marshall(x){return new i(x).serialize()}static hdrValueEscape(x){return x.replace(/\\/g,`\\\\`).replace(/\r/g,`\\r`).replace(/\n/g,`\\n`).replace(/:/g,`\\c`)}static hdrValueUnEscape(x){return x.replace(/\\r/g,`\r`).replace(/\\n/g,`
`).replace(/\\c/g,`:`).replace(/\\\\/g,`\\`)}}let a=0,o=10,r=13,s=58;class c{constructor(x,M){this.onFrame=x,this.onIncomingPing=M,this._encoder=new TextEncoder,this._decoder=new TextDecoder,this._token=[],this._initState()}parseChunk(x,M=!1){let T;if(typeof x==`string`?T=this._encoder.encode(x):T=new Uint8Array(x),M&&T[T.length-1]!==0){let $=new Uint8Array(T.length+1);$.set(T,0),$[T.length]=0,T=$}for(let $=0;$<T.length;$++){let ye=T[$];this._onByte(ye)}}_collectFrame(x){if(x!==a&&x!==r){if(x===o){this.onIncomingPing();return}this._onByte=this._collectCommand,this._reinjectByte(x)}}_collectCommand(x){if(x!==r){if(x===o){this._results.command=this._consumeTokenAsUTF8(),this._onByte=this._collectHeaders;return}this._consumeByte(x)}}_collectHeaders(x){if(x!==r){if(x===o){this._setupCollectBody();return}this._onByte=this._collectHeaderKey,this._reinjectByte(x)}}_reinjectByte(x){this._onByte(x)}_collectHeaderKey(x){if(x===s){this._headerKey=this._consumeTokenAsUTF8(),this._onByte=this._collectHeaderValue;return}this._consumeByte(x)}_collectHeaderValue(x){if(x!==r){if(x===o){this._results.headers.push([this._headerKey,this._consumeTokenAsUTF8()]),this._headerKey=void 0,this._onByte=this._collectHeaders;return}this._consumeByte(x)}}_setupCollectBody(){let x=this._results.headers.filter(M=>M[0]===`content-length`)[0];x?(this._bodyBytesRemaining=parseInt(x[1],10),this._onByte=this._collectBodyFixedSize):this._onByte=this._collectBodyNullTerminated}_collectBodyNullTerminated(x){if(x===a){this._retrievedBody();return}this._consumeByte(x)}_collectBodyFixedSize(x){if(this._bodyBytesRemaining--===0){this._retrievedBody();return}this._consumeByte(x)}_retrievedBody(){this._results.binaryBody=this._consumeTokenAsRaw();try{this.onFrame(this._results)}catch(x){console.log(`Ignoring an exception thrown by a frame handler. Original exception: `,x)}this._initState()}_consumeByte(x){this._token.push(x)}_consumeTokenAsUTF8(){return this._decoder.decode(this._consumeTokenAsRaw())}_consumeTokenAsRaw(){let x=new Uint8Array(this._token);return this._token=[],x}_initState(){this._results={command:void 0,headers:[],binaryBody:void 0},this._token=[],this._headerKey=void 0,this._onByte=this._collectFrame}}t.StompSocketState=void 0,(function(j){j[j.CONNECTING=0]=`CONNECTING`,j[j.OPEN=1]=`OPEN`,j[j.CLOSING=2]=`CLOSING`,j[j.CLOSED=3]=`CLOSED`})(t.StompSocketState||(t.StompSocketState={})),t.ActivationState=void 0,(function(j){j[j.ACTIVE=0]=`ACTIVE`,j[j.DEACTIVATING=1]=`DEACTIVATING`,j[j.INACTIVE=2]=`INACTIVE`})(t.ActivationState||(t.ActivationState={})),t.ReconnectionTimeMode=void 0,(function(j){j[j.LINEAR=0]=`LINEAR`,j[j.EXPONENTIAL=1]=`EXPONENTIAL`})(t.ReconnectionTimeMode||(t.ReconnectionTimeMode={})),t.TickerStrategy=void 0,(function(j){j.Interval=`interval`,j.Worker=`worker`})(t.TickerStrategy||(t.TickerStrategy={}));class p{constructor(x,M=t.TickerStrategy.Interval,T){this._interval=x,this._strategy=M,this._debug=T,this._workerScript=`
    var startTime = Date.now();
    setInterval(function() {
        self.postMessage(Date.now() - startTime);
    }, ${this._interval});
  `}start(x){this.stop(),this.shouldUseWorker()?this.runWorker(x):this.runInterval(x)}stop(){this.disposeWorker(),this.disposeInterval()}shouldUseWorker(){return typeof Worker<`u`&&this._strategy===t.TickerStrategy.Worker}runWorker(x){this._debug(`Using runWorker for outgoing pings`),this._worker||(this._worker=new Worker(URL.createObjectURL(new Blob([this._workerScript],{type:`text/javascript`}))),this._worker.onmessage=M=>x(M.data))}runInterval(x){if(this._debug(`Using runInterval for outgoing pings`),!this._timer){let M=Date.now();this._timer=setInterval(()=>{x(Date.now()-M)},this._interval)}}disposeWorker(){this._worker&&(this._worker.terminate(),delete this._worker,this._debug(`Outgoing ping disposeWorker`))}disposeInterval(){this._timer&&(clearInterval(this._timer),delete this._timer,this._debug(`Outgoing ping disposeInterval`))}}class m{constructor(x){this.versions=x}supportedVersions(){return this.versions.join(`,`)}protocolVersions(){return this.versions.map(x=>`v${x.replace(`.`,``)}.stomp`)}}m.V1_0=`1.0`,m.V1_1=`1.1`,m.V1_2=`1.2`,m.default=new m([m.V1_2,m.V1_1,m.V1_0]);class g{get connectedVersion(){return this._connectedVersion}get connected(){return this._connected}constructor(x,M,T){this._client=x,this._webSocket=M,this._connected=!1,this._serverFrameHandlers={CONNECTED:$=>{this.debug(`connected to server ${$.headers.server}`),this._connected=!0,this._connectedVersion=$.headers.version,this._connectedVersion===m.V1_2&&(this._escapeHeaderValues=!0),this._setupHeartbeat($.headers),this.onConnect($)},MESSAGE:$=>{let ye=$.headers.subscription,gt=this._subscriptions[ye]||this.onUnhandledMessage,We=$,ue=this,ke=this._connectedVersion===m.V1_2?We.headers.ack:We.headers[`message-id`];We.ack=(F={})=>ue.ack(ke,ye,F),We.nack=(F={})=>ue.nack(ke,ye,F),gt(We)},RECEIPT:$=>{let ye=this._receiptWatchers[$.headers[`receipt-id`]];ye?(ye($),delete this._receiptWatchers[$.headers[`receipt-id`]]):this.onUnhandledReceipt($)},ERROR:$=>{this.onStompError($)}},this._counter=0,this._subscriptions={},this._receiptWatchers={},this._partialData=``,this._escapeHeaderValues=!1,this._lastServerActivityTS=Date.now(),this.debug=T.debug,this.stompVersions=T.stompVersions,this.connectHeaders=T.connectHeaders,this.disconnectHeaders=T.disconnectHeaders,this.heartbeatIncoming=T.heartbeatIncoming,this.heartbeatToleranceMultiplier=T.heartbeatGracePeriods,this.heartbeatOutgoing=T.heartbeatOutgoing,this.splitLargeFrames=T.splitLargeFrames,this.maxWebSocketChunkSize=T.maxWebSocketChunkSize,this.forceBinaryWSFrames=T.forceBinaryWSFrames,this.logRawCommunication=T.logRawCommunication,this.appendMissingNULLonIncoming=T.appendMissingNULLonIncoming,this.discardWebsocketOnCommFailure=T.discardWebsocketOnCommFailure,this.onConnect=T.onConnect,this.onDisconnect=T.onDisconnect,this.onStompError=T.onStompError,this.onWebSocketClose=T.onWebSocketClose,this.onWebSocketError=T.onWebSocketError,this.onUnhandledMessage=T.onUnhandledMessage,this.onUnhandledReceipt=T.onUnhandledReceipt,this.onUnhandledFrame=T.onUnhandledFrame,this.onHeartbeatReceived=T.onHeartbeatReceived,this.onHeartbeatLost=T.onHeartbeatLost}start(){let x=new c(T=>{let $=i.fromRawFrame(T,this._escapeHeaderValues);this.logRawCommunication||this.debug(`<<< ${$}`),(this._serverFrameHandlers[$.command]||this.onUnhandledFrame)($)},()=>{this.debug(`<<< PONG`),this.onHeartbeatReceived()});this._webSocket.onmessage=T=>{if(this.debug(`Received data`),this._lastServerActivityTS=Date.now(),this.logRawCommunication){let $=T.data instanceof ArrayBuffer?new TextDecoder().decode(T.data):T.data;this.debug(`<<< ${$}`)}x.parseChunk(T.data,this.appendMissingNULLonIncoming)},this._webSocket.onclose=T=>{this.debug(`Connection closed to ${this._webSocket.url}`),this._cleanUp(),this.onWebSocketClose(T)},this._webSocket.onerror=T=>{this.onWebSocketError(T)};let M=()=>{let T=Object.assign({},this.connectHeaders);this.debug(`Web Socket Opened...`),T[`accept-version`]=this.stompVersions.supportedVersions(),T[`heart-beat`]=[this.heartbeatOutgoing,this.heartbeatIncoming].join(`,`),this._transmit({command:`CONNECT`,headers:T})};this._webSocket.readyState===t.StompSocketState.OPEN?M():this._webSocket.onopen=M}_setupHeartbeat(x){if(x.version!==m.V1_1&&x.version!==m.V1_2||!x[`heart-beat`])return;let[M,T]=x[`heart-beat`].split(`,`).map($=>parseInt($,10));if(this.heartbeatOutgoing!==0&&T!==0){let $=Math.max(this.heartbeatOutgoing,T);this.debug(`send PING every ${$}ms`),this._pinger=new p($,this._client.heartbeatStrategy,this.debug),this._pinger.start(()=>{this._webSocket.readyState===t.StompSocketState.OPEN&&(this._webSocket.send(e.LF),this.debug(`>>> PING`))})}if(this.heartbeatIncoming!==0&&M!==0){let $=Math.max(this.heartbeatIncoming,M);this.debug(`check PONG every ${$}ms`),this._ponger=setInterval(()=>{let ye=Date.now()-this._lastServerActivityTS;ye>$*this.heartbeatToleranceMultiplier&&(this.debug(`did not receive server activity for the last ${ye}ms`),this.onHeartbeatLost(),this._closeOrDiscardWebsocket())},$)}}_closeOrDiscardWebsocket(){this.discardWebsocketOnCommFailure?(this.debug(`Discarding websocket, the underlying socket may linger for a while`),this.discardWebsocket()):(this.debug(`Issuing close on the websocket`),this._closeWebsocket())}forceDisconnect(){this._webSocket&&(this._webSocket.readyState===t.StompSocketState.CONNECTING||this._webSocket.readyState===t.StompSocketState.OPEN)&&this._closeOrDiscardWebsocket()}_closeWebsocket(){this._webSocket.onmessage=()=>{},this._webSocket.close()}discardWebsocket(){typeof this._webSocket.terminate!=`function`&&n(this._webSocket,x=>this.debug(x)),this._webSocket.terminate()}_transmit(x){let{command:M,headers:T,body:$,binaryBody:ye,skipContentLengthHeader:gt}=x,We=new i({command:M,headers:T,body:$,binaryBody:ye,escapeHeaderValues:this._escapeHeaderValues,skipContentLengthHeader:gt}),ue=We.serialize();if(this.logRawCommunication?this.debug(`>>> ${ue}`):this.debug(`>>> ${We}`),this.forceBinaryWSFrames&&typeof ue==`string`&&(ue=new TextEncoder().encode(ue)),typeof ue!=`string`||!this.splitLargeFrames)this._webSocket.send(ue);else{let ke=ue;for(;ke.length>0;){let F=ke.substring(0,this.maxWebSocketChunkSize);ke=ke.substring(this.maxWebSocketChunkSize),this._webSocket.send(F),this.debug(`chunk sent = ${F.length}, remaining = ${ke.length}`)}}}dispose(){if(this.connected)try{let x=Object.assign({},this.disconnectHeaders);x.receipt||(x.receipt=`close-${this._counter++}`),this.watchForReceipt(x.receipt,M=>{this._closeWebsocket(),this._cleanUp(),this.onDisconnect(M)}),this._transmit({command:`DISCONNECT`,headers:x})}catch(x){this.debug(`Ignoring error during disconnect ${x}`)}else(this._webSocket.readyState===t.StompSocketState.CONNECTING||this._webSocket.readyState===t.StompSocketState.OPEN)&&this._closeWebsocket()}_cleanUp(){this._connected=!1,this._pinger&&(this._pinger.stop(),this._pinger=void 0),this._ponger&&(clearInterval(this._ponger),this._ponger=void 0)}publish(x){let{destination:M,headers:T,body:$,binaryBody:ye,skipContentLengthHeader:gt}=x,We=Object.assign({destination:M},T);this._transmit({command:`SEND`,headers:We,body:$,binaryBody:ye,skipContentLengthHeader:gt})}watchForReceipt(x,M){this._receiptWatchers[x]=M}subscribe(x,M,T={}){T=Object.assign({},T),T.id||(T.id=`sub-${this._counter++}`),T.destination=x,this._subscriptions[T.id]=M,this._transmit({command:`SUBSCRIBE`,headers:T});let $=this;return{id:T.id,unsubscribe(ye){return $.unsubscribe(T.id,ye)}}}unsubscribe(x,M={}){M=Object.assign({},M),delete this._subscriptions[x],M.id=x,this._transmit({command:`UNSUBSCRIBE`,headers:M})}begin(x){let M=x||`tx-${this._counter++}`;this._transmit({command:`BEGIN`,headers:{transaction:M}});let T=this;return{id:M,commit(){T.commit(M)},abort(){T.abort(M)}}}commit(x){this._transmit({command:`COMMIT`,headers:{transaction:x}})}abort(x){this._transmit({command:`ABORT`,headers:{transaction:x}})}ack(x,M,T={}){T=Object.assign({},T),this._connectedVersion===m.V1_2?T.id=x:T[`message-id`]=x,T.subscription=M,this._transmit({command:`ACK`,headers:T})}nack(x,M,T={}){return T=Object.assign({},T),this._connectedVersion===m.V1_2?T.id=x:T[`message-id`]=x,T.subscription=M,this._transmit({command:`NACK`,headers:T})}}class v{get webSocket(){return this._stompHandler?._webSocket}get disconnectHeaders(){return this._disconnectHeaders}set disconnectHeaders(x){this._disconnectHeaders=x,this._stompHandler&&(this._stompHandler.disconnectHeaders=this._disconnectHeaders)}get connected(){return!!this._stompHandler&&this._stompHandler.connected}get connectedVersion(){return this._stompHandler?this._stompHandler.connectedVersion:void 0}get active(){return this.state===t.ActivationState.ACTIVE}_changeState(x){this.state=x,this.onChangeState(x)}constructor(x={}){this.stompVersions=m.default,this.connectionTimeout=0,this.reconnectDelay=5e3,this._nextReconnectDelay=0,this.maxReconnectDelay=9e5,this.reconnectTimeMode=t.ReconnectionTimeMode.LINEAR,this.heartbeatIncoming=1e4,this.heartbeatToleranceMultiplier=2,this.heartbeatOutgoing=1e4,this.heartbeatStrategy=t.TickerStrategy.Interval,this.splitLargeFrames=!1,this.maxWebSocketChunkSize=8192,this.forceBinaryWSFrames=!1,this.appendMissingNULLonIncoming=!1,this.discardWebsocketOnCommFailure=!1,this.state=t.ActivationState.INACTIVE;let M=()=>{};this.debug=M,this.beforeConnect=M,this.onConnect=M,this.onDisconnect=M,this.onUnhandledMessage=M,this.onUnhandledReceipt=M,this.onUnhandledFrame=M,this.onHeartbeatReceived=M,this.onHeartbeatLost=M,this.onStompError=M,this.onWebSocketClose=M,this.onWebSocketError=M,this.logRawCommunication=!1,this.onChangeState=M,this.connectHeaders={},this._disconnectHeaders={},this.configure(x)}configure(x){Object.assign(this,x),this.maxReconnectDelay>0&&this.maxReconnectDelay<this.reconnectDelay&&(this.debug(`Warning: maxReconnectDelay (${this.maxReconnectDelay}ms) is less than reconnectDelay (${this.reconnectDelay}ms). Using reconnectDelay as the maxReconnectDelay delay.`),this.maxReconnectDelay=this.reconnectDelay)}activate(){let x=()=>{if(this.active){this.debug(`Already ACTIVE, ignoring request to activate`);return}this._changeState(t.ActivationState.ACTIVE),this._nextReconnectDelay=this.reconnectDelay,this._connect()};this.state===t.ActivationState.DEACTIVATING?(this.debug(`Waiting for deactivation to finish before activating`),this.deactivate().then(()=>{x()})):x()}async _connect(){if(await this.beforeConnect(this),this._stompHandler){this.debug(`There is already a stompHandler, skipping the call to connect`);return}if(!this.active){this.debug(`Client has been marked inactive, will not attempt to connect`);return}this.connectionTimeout>0&&(this._connectionWatcher&&clearTimeout(this._connectionWatcher),this._connectionWatcher=setTimeout(()=>{this.connected||(this.debug(`Connection not established in ${this.connectionTimeout}ms, closing socket`),this.forceDisconnect())},this.connectionTimeout)),this.debug(`Opening Web Socket...`);let x=this._createWebSocket();this._stompHandler=new g(this,x,{debug:this.debug,stompVersions:this.stompVersions,connectHeaders:this.connectHeaders,disconnectHeaders:this._disconnectHeaders,heartbeatIncoming:this.heartbeatIncoming,heartbeatGracePeriods:this.heartbeatToleranceMultiplier,heartbeatOutgoing:this.heartbeatOutgoing,heartbeatStrategy:this.heartbeatStrategy,splitLargeFrames:this.splitLargeFrames,maxWebSocketChunkSize:this.maxWebSocketChunkSize,forceBinaryWSFrames:this.forceBinaryWSFrames,logRawCommunication:this.logRawCommunication,appendMissingNULLonIncoming:this.appendMissingNULLonIncoming,discardWebsocketOnCommFailure:this.discardWebsocketOnCommFailure,onConnect:M=>{if(this._connectionWatcher&&(clearTimeout(this._connectionWatcher),this._connectionWatcher=void 0),this._nextReconnectDelay=this.reconnectDelay,!this.active){this.debug(`STOMP got connected while deactivate was issued, will disconnect now`),this._disposeStompHandler();return}this.onConnect(M)},onDisconnect:M=>{this.onDisconnect(M)},onStompError:M=>{this.onStompError(M)},onWebSocketClose:M=>{this._stompHandler=void 0,this.state===t.ActivationState.DEACTIVATING&&this._changeState(t.ActivationState.INACTIVE),this.onWebSocketClose(M),this.active&&this._schedule_reconnect()},onWebSocketError:M=>{this.onWebSocketError(M)},onUnhandledMessage:M=>{this.onUnhandledMessage(M)},onUnhandledReceipt:M=>{this.onUnhandledReceipt(M)},onUnhandledFrame:M=>{this.onUnhandledFrame(M)},onHeartbeatReceived:()=>{this.onHeartbeatReceived()},onHeartbeatLost:()=>{this.onHeartbeatLost()}}),this._stompHandler.start()}_createWebSocket(){let x;if(this.webSocketFactory)x=this.webSocketFactory();else if(this.brokerURL)x=new WebSocket(this.brokerURL,this.stompVersions.protocolVersions());else throw new Error(`Either brokerURL or webSocketFactory must be provided`);return x.binaryType=`arraybuffer`,x}_schedule_reconnect(){this._nextReconnectDelay>0&&(this.debug(`STOMP: scheduling reconnection in ${this._nextReconnectDelay}ms`),this._reconnector=setTimeout(()=>{this.reconnectTimeMode===t.ReconnectionTimeMode.EXPONENTIAL&&(this._nextReconnectDelay=this._nextReconnectDelay*2,this.maxReconnectDelay!==0&&(this._nextReconnectDelay=Math.min(this._nextReconnectDelay,this.maxReconnectDelay))),this._connect()},this._nextReconnectDelay))}async deactivate(x={}){let M=x.force||!1,T=this.active,$;if(this.state===t.ActivationState.INACTIVE)return this.debug(`Already INACTIVE, nothing more to do`),Promise.resolve();if(this._changeState(t.ActivationState.DEACTIVATING),this._nextReconnectDelay=0,this._reconnector&&(clearTimeout(this._reconnector),this._reconnector=void 0),this._stompHandler&&this.webSocket.readyState!==t.StompSocketState.CLOSED){let ye=this._stompHandler.onWebSocketClose;$=new Promise((gt,We)=>{this._stompHandler.onWebSocketClose=ue=>{ye(ue),gt()}})}else return this._changeState(t.ActivationState.INACTIVE),Promise.resolve();return M?this._stompHandler?.discardWebsocket():T&&this._disposeStompHandler(),$}forceDisconnect(){this._stompHandler&&this._stompHandler.forceDisconnect()}_disposeStompHandler(){this._stompHandler&&this._stompHandler.dispose()}publish(x){this._checkConnection(),this._stompHandler.publish(x)}_checkConnection(){if(!this.connected)throw new TypeError(`There is no underlying STOMP connection`)}watchForReceipt(x,M){this._checkConnection(),this._stompHandler.watchForReceipt(x,M)}subscribe(x,M,T={}){return this._checkConnection(),this._stompHandler.subscribe(x,M,T)}unsubscribe(x,M={}){this._checkConnection(),this._stompHandler.unsubscribe(x,M)}begin(x){return this._checkConnection(),this._stompHandler.begin(x)}commit(x){this._checkConnection(),this._stompHandler.commit(x)}abort(x){this._checkConnection(),this._stompHandler.abort(x)}ack(x,M,T={}){this._checkConnection(),this._stompHandler.ack(x,M,T)}nack(x,M,T={}){this._checkConnection(),this._stompHandler.nack(x,M,T)}}class b{}class _{}class E{constructor(x){this.client=x}get outgoing(){return this.client.heartbeatOutgoing}set outgoing(x){this.client.heartbeatOutgoing=x}get incoming(){return this.client.heartbeatIncoming}set incoming(x){this.client.heartbeatIncoming=x}}class P extends v{constructor(x){super(),this.maxWebSocketFrameSize=16384,this._heartbeatInfo=new E(this),this.reconnect_delay=0,this.webSocketFactory=x,this.debug=(...M)=>{console.log(...M)}}_parseConnect(...x){let M,T,$,ye={};if(x.length<2)throw new Error(`Connect requires at least 2 arguments`);return typeof x[1]==`function`?[ye,T,$,M]=x:x.length===6?[ye.login,ye.passcode,T,$,M,ye.host]=x:[ye.login,ye.passcode,T,$,M]=x,[ye,T,$,M]}connect(...x){let M=this._parseConnect(...x);M[0]&&(this.connectHeaders=M[0]),M[1]&&(this.onConnect=M[1]),M[2]&&(this.onStompError=M[2]),M[3]&&(this.onWebSocketClose=M[3]),super.activate()}disconnect(x,M={}){x&&(this.onDisconnect=x),this.disconnectHeaders=M,super.deactivate()}send(x,M={},T=``){M=Object.assign({},M);let $=M[`content-length`]===!1;$&&delete M[`content-length`],this.publish({destination:x,headers:M,body:T,skipContentLengthHeader:$})}set reconnect_delay(x){this.reconnectDelay=x}get ws(){return this.webSocket}get version(){return this.connectedVersion}get onreceive(){return this.onUnhandledMessage}set onreceive(x){this.onUnhandledMessage=x}get onreceipt(){return this.onUnhandledReceipt}set onreceipt(x){this.onUnhandledReceipt=x}get heartbeat(){return this._heartbeatInfo}set heartbeat(x){this.heartbeatIncoming=x.incoming,this.heartbeatOutgoing=x.outgoing}}class ae{static client(x,M){M??=m.default.protocolVersions();let T=()=>{return new(ae.WebSocketClass||WebSocket)(x,M)};return new P(T)}static over(x){let M;return typeof x==`function`?M=x:(console.warn(`Stomp.over did not receive a factory, auto reconnect will not work. Please see https://stomp-js.github.io/api-docs/latest/classes/Stomp.html#over`),M=()=>x),new P(M)}}ae.WebSocketClass=null,t.Client=v,t.CompatClient=P,t.FrameImpl=i,t.Parser=c,t.Stomp=ae,t.StompConfig=b,t.StompHeaders=_,t.Versions=m}))});var an=null;var Jc=!1;var za=1;var dE=null;var Ct=Symbol(`SIGNAL`);function he(t){let n=an;return an=t,n}function il(){return an}var Ua={version:0,lastCleanEpoch:0,dirty:!1,producers:void 0,producersTail:void 0,consumers:void 0,consumersTail:void 0,recomputing:!1,consumerAllowSignalWrites:!1,consumerIsAlwaysLive:!1,kind:`unknown`,producerMustRecompute:()=>!1,producerRecomputeValue:()=>{},consumerMarkedDirty:()=>{},consumerOnSignalRead:()=>{}};function Oi(t){if(Jc)throw new Error(``);if(an===null)return;an.consumerOnSignalRead(t);let n=an.producersTail;if(n!==void 0&&n.producer===t)return;let e,i=an.recomputing;if(i&&(e=n!==void 0?n.nextProducer:an.producers,e!==void 0&&e.producer===t)){an.producersTail=e,e.lastReadVersion=t.version,e.knownValidAtEpoch=za;return}let a=t.consumersTail;if(a!==void 0&&a.consumer===an&&(!i||a.knownValidAtEpoch===za))return;let o=$o(an),r={producer:t,consumer:an,nextProducer:e,prevConsumer:void 0,knownValidAtEpoch:za,lastReadVersion:t.version,nextConsumer:void 0};an.producersTail=r,n!==void 0?n.nextProducer=r:an.producers=r,o&&Ub(t,r)}function jb(){za++}function ms(t){if(!($o(t)&&!t.dirty)&&!(!t.dirty&&t.lastCleanEpoch===za)){if(!t.producerMustRecompute(t)&&!Uo(t)){nl(t);return}t.producerRecomputeValue(t),nl(t)}}function Hm(t){if(t.consumers===void 0)return;let n=Jc;Jc=!0;try{for(let e=t.consumers;e!==void 0;e=e.nextConsumer){let i=e.consumer;i.dirty||uE(i)}}finally{Jc=n}}function Um(){return an?.consumerAllowSignalWrites!==!1}function uE(t){t.dirty=!0,Hm(t),t.consumerMarkedDirty?.(t)}function nl(t){t.dirty=!1,t.lastCleanEpoch=za}function la(t){return t&&zb(t),he(t)}function zb(t){if(t.producersTail?.knownValidAtEpoch===za){let n=t.producers;for(;n!==void 0;)n.knownValidAtEpoch=null,n=n.nextProducer}t.producersTail=void 0,t.recomputing=!0}function $a(t,n){he(n),t&&Hb(t)}function Hb(t){t.recomputing=!1;let n=t.producersTail,e=n!==void 0?n.nextProducer:t.producers;if(e!==void 0){if($o(t))do e=$m(e);while(e!==void 0);n!==void 0?n.nextProducer=void 0:t.producers=void 0}}function Uo(t){for(let n=t.producers;n!==void 0;n=n.nextProducer){let e=n.producer,i=n.lastReadVersion;if(i!==e.version||(ms(e),i!==e.version))return!0}return!1}function da(t){if($o(t)){let n=t.producers;for(;n!==void 0;)n=$m(n)}t.producers=void 0,t.producersTail=void 0,t.consumers=void 0,t.consumersTail=void 0}function Ub(t,n){let e=t.consumersTail,i=$o(t);if(e!==void 0?(n.nextConsumer=e.nextConsumer,e.nextConsumer=n):(n.nextConsumer=void 0,t.consumers=n),n.prevConsumer=e,t.consumersTail=n,!i)for(let a=t.producers;a!==void 0;a=a.nextProducer)Ub(a.producer,a)}function $m(t){let n=t.producer,e=t.nextProducer,i=t.nextConsumer,a=t.prevConsumer;if(t.nextConsumer=void 0,t.prevConsumer=void 0,i!==void 0?i.prevConsumer=a:n.consumersTail=a,a!==void 0)a.nextConsumer=i;else if(n.consumers=i,!$o(n)){let o=n.producers;for(;o!==void 0;)o=$m(o)}return e}function $o(t){return t.consumerIsAlwaysLive||t.consumers!==void 0}function al(t){dE?.(t)}function ol(t,n){return Object.is(t,n)}function ps(t,n){let e=Object.create(mE);e.computation=t,n!==void 0&&(e.equal=n);let i=()=>{if(ms(e),Oi(e),e.value===Ha)throw e.error;return e.value};return i[Ct]=e,al(e),i}var el=Symbol(`UNSET`);var tl=Symbol(`COMPUTING`);var Ha=Symbol(`ERRORED`);var mE=s(r({},Ua),{value:el,dirty:!0,error:null,equal:ol,kind:`computed`,producerMustRecompute(t){return t.value===el||t.value===tl},producerRecomputeValue(t){if(t.value===tl)throw new Error(``);let n=t.value;t.value=tl;let e=la(t),i,a=!1;try{i=t.computation(),he(null),a=n!==el&&n!==Ha&&i!==Ha&&t.equal(n,i)}catch(o){i=Ha,t.error=o}finally{$a(t,e)}if(a){t.value=n;return}t.value=i,t.version++}});function pE(){throw new Error}var $b=pE;function Gb(t){$b(t)}function Gm(t){$b=t}var hE=null;function Wm(t,n){let e=Object.create(Go);e.value=t,n!==void 0&&(e.equal=n);let i=()=>Wb(e);return i[Ct]=e,al(e),[i,r=>ua(e,r),r=>qm(e,r)]}function Wb(t){return Oi(t),t.value}function ua(t,n){Um()||Gb(t),t.equal(t.value,n)||(t.value=n,fE(t))}function qm(t,n){Um()||Gb(t),ua(t,n(t.value))}var Go=s(r({},Ua),{equal:ol,value:void 0,kind:`signal`});function fE(t){t.version++,jb(),Hm(t),hE?.(t)}var Ym=s(r({},Ua),{consumerIsAlwaysLive:!0,consumerAllowSignalWrites:!0,dirty:!0,kind:`effect`});function Km(t){if(t.dirty=!1,t.version>0&&!Uo(t))return;t.version++;let n=la(t);try{t.cleanup(),t.fn()}finally{$a(t,n)}}var Qm;function rl(){return Qm}function hi(t){let n=Qm;return Qm=t,n}var qb=Symbol(`NotFound`);function Wo(t){return t===qb||t?.name===`ɵNotFound`}function Yb(t){let n=he(null);try{return t()}finally{he(n)}}function Me(t){return typeof t==`function`}function qo(t){let e=t(i=>{Error.call(i),i.stack=new Error().stack});return e.prototype=Object.create(Error.prototype),e.prototype.constructor=e,e}var sl=qo(t=>function(e){t(this),this.message=e?`${e.length} errors occurred during unsubscription:
${e.map((i,a)=>`${a+1}) ${i.toString()}`).join(`
  `)}`:``,this.name=`UnsubscriptionError`,this.errors=e});function Ga(t,n){if(t){let e=t.indexOf(n);0<=e&&t.splice(e,1)}}var De=class t{constructor(n){this.initialTeardown=n,this.closed=!1,this._parentage=null,this._finalizers=null}unsubscribe(){let n;if(!this.closed){this.closed=!0;let{_parentage:e}=this;if(e)if(this._parentage=null,Array.isArray(e))for(let o of e)o.remove(this);else e.remove(this);let{initialTeardown:i}=this;if(Me(i))try{i()}catch(o){n=o instanceof sl?o.errors:[o]}let{_finalizers:a}=this;if(a){this._finalizers=null;for(let o of a)try{Kb(o)}catch(r){n=n??[],r instanceof sl?n=[...n,...r.errors]:n.push(r)}}if(n)throw new sl(n)}}add(n){var e;if(n&&n!==this)if(this.closed)Kb(n);else{if(n instanceof t){if(n.closed||n._hasParent(this))return;n._addParent(this)}(this._finalizers=(e=this._finalizers)!==null&&e!==void 0?e:[]).push(n)}}_hasParent(n){let{_parentage:e}=this;return e===n||Array.isArray(e)&&e.includes(n)}_addParent(n){let{_parentage:e}=this;this._parentage=Array.isArray(e)?(e.push(n),e):e?[e,n]:n}_removeParent(n){let{_parentage:e}=this;e===n?this._parentage=null:Array.isArray(e)&&Ga(e,n)}remove(n){let{_finalizers:e}=this;e&&Ga(e,n),n instanceof t&&n._removeParent(this)}};De.EMPTY=(()=>{let t=new De;return t.closed=!0,t})();var Xm=De.EMPTY;function cl(t){return t instanceof De||t&&`closed`in t&&Me(t.remove)&&Me(t.add)&&Me(t.unsubscribe)}function Kb(t){Me(t)?t():t.unsubscribe()}var Gn={onUnhandledError:null,onStoppedNotification:null,Promise:void 0,useDeprecatedSynchronousErrorHandling:!1,useDeprecatedNextContext:!1};var Yo={setTimeout(t,n,...e){let{delegate:i}=Yo;return i?.setTimeout?i.setTimeout(t,n,...e):setTimeout(t,n,...e)},clearTimeout(t){let{delegate:n}=Yo;return(n?.clearTimeout||clearTimeout)(t)},delegate:void 0};function ll(t){Yo.setTimeout(()=>{let{onUnhandledError:n}=Gn;if(n)n(t);else throw t})}function hs(){}var Qb=Zm(`C`,void 0,void 0);function Xb(t){return Zm(`E`,void 0,t)}function Zb(t){return Zm(`N`,t,void 0)}function Zm(t,n,e){return{kind:t,value:n,error:e}}var Wa=null;function Ko(t){if(Gn.useDeprecatedSynchronousErrorHandling){let n=!Wa;if(n&&(Wa={errorThrown:!1,error:null}),t(),n){let{errorThrown:e,error:i}=Wa;if(Wa=null,e)throw i}}else t()}function Jb(t){Gn.useDeprecatedSynchronousErrorHandling&&Wa&&(Wa.errorThrown=!0,Wa.error=t)}var qa=class extends De{constructor(n){super(),this.isStopped=!1,n?(this.destination=n,cl(n)&&n.add(this)):this.destination=vE}static create(n,e,i){return new Wn(n,e,i)}next(n){this.isStopped?ep(Zb(n),this):this._next(n)}error(n){this.isStopped?ep(Xb(n),this):(this.isStopped=!0,this._error(n))}complete(){this.isStopped?ep(Qb,this):(this.isStopped=!0,this._complete())}unsubscribe(){this.closed||(this.isStopped=!0,super.unsubscribe(),this.destination=null)}_next(n){this.destination.next(n)}_error(n){try{this.destination.error(n)}finally{this.unsubscribe()}}_complete(){try{this.destination.complete()}finally{this.unsubscribe()}}};var gE=Function.prototype.bind;function Jm(t,n){return gE.call(t,n)}var tp=class{constructor(n){this.partialObserver=n}next(n){let{partialObserver:e}=this;if(e.next)try{e.next(n)}catch(i){dl(i)}}error(n){let{partialObserver:e}=this;if(e.error)try{e.error(n)}catch(i){dl(i)}else dl(n)}complete(){let{partialObserver:n}=this;if(n.complete)try{n.complete()}catch(e){dl(e)}}};var Wn=class extends qa{constructor(n,e,i){super();let a;if(Me(n)||!n)a={next:n??void 0,error:e??void 0,complete:i??void 0};else{let o;this&&Gn.useDeprecatedNextContext?(o=Object.create(n),o.unsubscribe=()=>this.unsubscribe(),a={next:n.next&&Jm(n.next,o),error:n.error&&Jm(n.error,o),complete:n.complete&&Jm(n.complete,o)}):a=n}this.destination=new tp(a)}};function dl(t){Gn.useDeprecatedSynchronousErrorHandling?Jb(t):ll(t)}function bE(t){throw t}function ep(t,n){let{onStoppedNotification:e}=Gn;e&&Yo.setTimeout(()=>e(t,n))}var vE={closed:!0,next:hs,error:bE,complete:hs};var Qo=typeof Symbol==`function`&&Symbol.observable||`@@observable`;function qn(t){return t}function ev(t){return t.length===0?qn:t.length===1?t[0]:function(e){return t.reduce((i,a)=>a(i),e)}}var Ce=class t{constructor(n){n&&(this._subscribe=n)}lift(n){let e=new t;return e.source=this,e.operator=n,e}subscribe(n,e,i){let a=_E(n)?n:new Wn(n,e,i);return Ko(()=>{let{operator:o,source:r}=this;a.add(o?o.call(a,r):r?this._subscribe(a):this._trySubscribe(a))}),a}_trySubscribe(n){try{return this._subscribe(n)}catch(e){n.error(e)}}forEach(n,e){return e=tv(e),new e((i,a)=>{let o=new Wn({next:r=>{try{n(r)}catch(s){a(s),o.unsubscribe()}},error:a,complete:i});this.subscribe(o)})}_subscribe(n){var e;return(e=this.source)===null||e===void 0?void 0:e.subscribe(n)}[Qo](){return this}pipe(...n){return ev(n)(this)}toPromise(n){return n=tv(n),new n((e,i)=>{let a;this.subscribe(o=>a=o,o=>i(o),()=>e(a))})}};Ce.create=t=>new Ce(t);function tv(t){var n;return(n=t??Gn.Promise)!==null&&n!==void 0?n:Promise}function yE(t){return t&&Me(t.next)&&Me(t.error)&&Me(t.complete)}function _E(t){return t&&t instanceof qa||yE(t)&&cl(t)}function xE(t){return Me(t?.lift)}function Le(t){return n=>{if(xE(n))return n.lift(function(e){try{return t(e,this)}catch(i){this.error(i)}});throw new TypeError(`Unable to lift unknown Observable type`)}}function je(t,n,e,i,a){return new np(t,n,e,i,a)}var np=class extends qa{constructor(n,e,i,a,o,r){super(n),this.onFinalize=o,this.shouldUnsubscribe=r,this._next=e?function(s){try{e(s)}catch(c){n.error(c)}}:super._next,this._error=a?function(s){try{a(s)}catch(c){n.error(c)}finally{this.unsubscribe()}}:super._error,this._complete=i?function(){try{i()}catch(s){n.error(s)}finally{this.unsubscribe()}}:super._complete}unsubscribe(){var n;if(!this.shouldUnsubscribe||this.shouldUnsubscribe()){let{closed:e}=this;super.unsubscribe(),!e&&((n=this.onFinalize)===null||n===void 0||n.call(this))}}};var nv=qo(t=>function(){t(this),this.name=`ObjectUnsubscribedError`,this.message=`object unsubscribed`});var L=class extends Ce{constructor(){super(),this.closed=!1,this.currentObservers=null,this.observers=[],this.isStopped=!1,this.hasError=!1,this.thrownError=null}lift(n){let e=new ul(this,this);return e.operator=n,e}_throwIfClosed(){if(this.closed)throw new nv}next(n){Ko(()=>{if(this._throwIfClosed(),!this.isStopped){this.currentObservers||(this.currentObservers=Array.from(this.observers));for(let e of this.currentObservers)e.next(n)}})}error(n){Ko(()=>{if(this._throwIfClosed(),!this.isStopped){this.hasError=this.isStopped=!0,this.thrownError=n;let{observers:e}=this;for(;e.length;)e.shift().error(n)}})}complete(){Ko(()=>{if(this._throwIfClosed(),!this.isStopped){this.isStopped=!0;let{observers:n}=this;for(;n.length;)n.shift().complete()}})}unsubscribe(){this.isStopped=this.closed=!0,this.observers=this.currentObservers=null}get observed(){var n;return((n=this.observers)===null||n===void 0?void 0:n.length)>0}_trySubscribe(n){return this._throwIfClosed(),super._trySubscribe(n)}_subscribe(n){return this._throwIfClosed(),this._checkFinalizedStatuses(n),this._innerSubscribe(n)}_innerSubscribe(n){let{hasError:e,isStopped:i,observers:a}=this;return e||i?Xm:(this.currentObservers=null,a.push(n),new De(()=>{this.currentObservers=null,Ga(a,n)}))}_checkFinalizedStatuses(n){let{hasError:e,thrownError:i,isStopped:a}=this;e?n.error(i):a&&n.complete()}asObservable(){let n=new Ce;return n.source=this,n}};L.create=(t,n)=>new ul(t,n);var ul=class extends L{constructor(n,e){super(),this.destination=n,this.source=e}next(n){var e,i;(i=(e=this.destination)===null||e===void 0?void 0:e.next)===null||i===void 0||i.call(e,n)}error(n){var e,i;(i=(e=this.destination)===null||e===void 0?void 0:e.error)===null||i===void 0||i.call(e,n)}complete(){var n,e;(e=(n=this.destination)===null||n===void 0?void 0:n.complete)===null||e===void 0||e.call(n)}_subscribe(n){var e,i;return(i=(e=this.source)===null||e===void 0?void 0:e.subscribe(n))!==null&&i!==void 0?i:Xm}};var fi=class extends L{constructor(n){super(),this._value=n}get value(){return this.getValue()}_subscribe(n){let e=super._subscribe(n);return!e.closed&&n.next(this._value),e}getValue(){let{hasError:n,thrownError:e,_value:i}=this;if(n)throw e;return this._throwIfClosed(),i}next(n){super.next(this._value=n)}};var fs={now(){return(fs.delegate||Date).now()},delegate:void 0};var Yn=class extends L{constructor(n=1/0,e=1/0,i=fs){super(),this._bufferSize=n,this._windowTime=e,this._timestampProvider=i,this._buffer=[],this._infiniteTimeWindow=!0,this._infiniteTimeWindow=e===1/0,this._bufferSize=Math.max(1,n),this._windowTime=Math.max(1,e)}next(n){let{isStopped:e,_buffer:i,_infiniteTimeWindow:a,_timestampProvider:o,_windowTime:r}=this;e||(i.push(n),!a&&i.push(o.now()+r)),this._trimBuffer(),super.next(n)}_subscribe(n){this._throwIfClosed(),this._trimBuffer();let e=this._innerSubscribe(n),{_infiniteTimeWindow:i,_buffer:a}=this,o=a.slice();for(let r=0;r<o.length&&!n.closed;r+=i?1:2)n.next(o[r]);return this._checkFinalizedStatuses(n),e}_trimBuffer(){let{_bufferSize:n,_timestampProvider:e,_buffer:i,_infiniteTimeWindow:a}=this,o=(a?1:2)*n;if(n<1/0&&o<i.length&&i.splice(0,i.length-o),!a){let r=e.now(),s=0;for(let c=1;c<i.length&&i[c]<=r;c+=2)s=c;s&&i.splice(0,s+1)}}};var ml=class extends De{constructor(n,e){super()}schedule(n,e=0){return this}};var gs={setInterval(t,n,...e){let{delegate:i}=gs;return i?.setInterval?i.setInterval(t,n,...e):setInterval(t,n,...e)},clearInterval(t){let{delegate:n}=gs;return(n?.clearInterval||clearInterval)(t)},delegate:void 0};var pl=class extends ml{constructor(n,e){super(n,e),this.scheduler=n,this.work=e,this.pending=!1}schedule(n,e=0){var i;if(this.closed)return this;this.state=n;let a=this.id,o=this.scheduler;return a!=null&&(this.id=this.recycleAsyncId(o,a,e)),this.pending=!0,this.delay=e,this.id=(i=this.id)!==null&&i!==void 0?i:this.requestAsyncId(o,this.id,e),this}requestAsyncId(n,e,i=0){return gs.setInterval(n.flush.bind(n,this),i)}recycleAsyncId(n,e,i=0){if(i!=null&&this.delay===i&&this.pending===!1)return e;e!=null&&gs.clearInterval(e)}execute(n,e){if(this.closed)return new Error(`executing a cancelled action`);this.pending=!1;let i=this._execute(n,e);if(i)return i;this.pending===!1&&this.id!=null&&(this.id=this.recycleAsyncId(this.scheduler,this.id,null))}_execute(n,e){let i=!1,a;try{this.work(n)}catch(o){i=!0,a=o||new Error(`Scheduled action threw falsy error`)}if(i)return this.unsubscribe(),a}unsubscribe(){if(!this.closed){let{id:n,scheduler:e}=this,{actions:i}=e;this.work=this.state=this.scheduler=null,this.pending=!1,Ga(i,this),n!=null&&(this.id=this.recycleAsyncId(e,n,null)),this.delay=null,super.unsubscribe()}}};var Xo=class t{constructor(n,e=t.now){this.schedulerActionCtor=n,this.now=e}schedule(n,e=0,i){return new this.schedulerActionCtor(this,n).schedule(i,e)}};Xo.now=fs.now;var hl=class extends Xo{constructor(n,e=Xo.now){super(n,e),this.actions=[],this._active=!1}flush(n){let{actions:e}=this;if(this._active){e.push(n);return}let i;this._active=!0;do if(i=n.execute(n.state,n.delay))break;while(n=e.shift());if(this._active=!1,i){for(;n=e.shift();)n.unsubscribe();throw i}}};var bs=new hl(pl);var iv=bs;var Ri=new Ce(t=>t.complete());function fl(t){return t&&Me(t.schedule)}function ip(t){return t[t.length-1]}function gl(t){return Me(ip(t))?t.pop():void 0}function gi(t){return fl(ip(t))?t.pop():void 0}function av(t,n){return typeof ip(t)==`number`?t.pop():n}function rv(t,n,e,i){function a(o){return o instanceof e?o:new e(function(r){r(o)})}return new(e||(e=Promise))(function(o,r){function s(m){try{p(i.next(m))}catch(g){r(g)}}function c(m){try{p(i.throw(m))}catch(g){r(g)}}function p(m){m.done?o(m.value):a(m.value).then(s,c)}p((i=i.apply(t,n||[])).next())})}function ov(t){var n=typeof Symbol==`function`&&Symbol.iterator,e=n&&t[n],i=0;if(e)return e.call(t);if(t&&typeof t.length==`number`)return{next:function(){return t&&i>=t.length&&(t=void 0),{value:t&&t[i++],done:!t}}};throw new TypeError(n?`Object is not iterable.`:`Symbol.iterator is not defined.`)}function Ya(t){return this instanceof Ya?(this.v=t,this):new Ya(t)}function sv(t,n,e){if(!Symbol.asyncIterator)throw new TypeError(`Symbol.asyncIterator is not defined.`);var i=e.apply(t,n||[]),a,o=[];return a=Object.create((typeof AsyncIterator==`function`?AsyncIterator:Object).prototype),s(`next`),s(`throw`),s(`return`,r),a[Symbol.asyncIterator]=function(){return this},a;function r(b){return function(_){return Promise.resolve(_).then(b,g)}}function s(b,_){i[b]&&(a[b]=function(E){return new Promise(function(P,ae){o.push([b,E,P,ae])>1||c(b,E)})},_&&(a[b]=_(a[b])))}function c(b,_){try{p(i[b](_))}catch(E){v(o[0][3],E)}}function p(b){b.value instanceof Ya?Promise.resolve(b.value.v).then(m,g):v(o[0][2],b)}function m(b){c(`next`,b)}function g(b){c(`throw`,b)}function v(b,_){b(_),o.shift(),o.length&&c(o[0][0],o[0][1])}}function cv(t){if(!Symbol.asyncIterator)throw new TypeError(`Symbol.asyncIterator is not defined.`);var n=t[Symbol.asyncIterator],e;return n?n.call(t):(t=typeof ov==`function`?ov(t):t[Symbol.iterator](),e={},i(`next`),i(`throw`),i(`return`),e[Symbol.asyncIterator]=function(){return this},e);function i(o){e[o]=t[o]&&function(r){return new Promise(function(s,c){r=t[o](r),a(s,c,r.done,r.value)})}}function a(o,r,s,c){Promise.resolve(c).then(function(p){o({value:p,done:s})},r)}}var bl=(t=>t&&typeof t.length==`number`&&typeof t!=`function`);function vl(t){return Me(t?.then)}function yl(t){return Me(t[Qo])}function _l(t){return Symbol.asyncIterator&&Me(t?.[Symbol.asyncIterator])}function xl(t){return new TypeError(`You provided ${t!==null&&typeof t==`object`?`an invalid object`:`'${t}'`} where a stream was expected. You can provide an Observable, Promise, ReadableStream, Array, AsyncIterable, or Iterable.`)}function CE(){return typeof Symbol!=`function`||!Symbol.iterator?`@@iterator`:Symbol.iterator}var Cl=CE();function Sl(t){return Me(t?.[Cl])}function wl(t){return sv(this,arguments,function*(){let e=t.getReader();try{for(;;){let{value:i,done:a}=yield Ya(e.read());if(a)return yield Ya(void 0);yield yield Ya(i)}}finally{e.releaseLock()}})}function El(t){return Me(t?.getReader)}function dt(t){if(t instanceof Ce)return t;if(t!=null){if(yl(t))return SE(t);if(bl(t))return wE(t);if(vl(t))return EE(t);if(_l(t))return lv(t);if(Sl(t))return DE(t);if(El(t))return kE(t)}throw xl(t)}function SE(t){return new Ce(n=>{let e=t[Qo]();if(Me(e.subscribe))return e.subscribe(n);throw new TypeError(`Provided object does not correctly implement Symbol.observable`)})}function wE(t){return new Ce(n=>{for(let e=0;e<t.length&&!n.closed;e++)n.next(t[e]);n.complete()})}function EE(t){return new Ce(n=>{t.then(e=>{n.closed||(n.next(e),n.complete())},e=>n.error(e)).then(null,ll)})}function DE(t){return new Ce(n=>{for(let e of t)if(n.next(e),n.closed)return;n.complete()})}function lv(t){return new Ce(n=>{IE(t,n).catch(e=>n.error(e))})}function kE(t){return lv(wl(t))}function IE(t,n){var e,i,a,o;return rv(this,void 0,void 0,function*(){try{for(e=cv(t);i=yield e.next(),!i.done;){let r=i.value;if(n.next(r),n.closed)return}}catch(r){a={error:r}}finally{try{i&&!i.done&&(o=e.return)&&(yield o.call(e))}finally{if(a)throw a.error}}n.complete()})}function un(t,n,e,i=0,a=!1){let o=n.schedule(function(){e(),a?t.add(this.schedule(null,i)):this.unsubscribe()},i);if(t.add(o),!a)return o}function Dl(t,n=0){return Le((e,i)=>{e.subscribe(je(i,a=>un(i,t,()=>i.next(a),n),()=>un(i,t,()=>i.complete(),n),a=>un(i,t,()=>i.error(a),n)))})}function kl(t,n=0){return Le((e,i)=>{i.add(t.schedule(()=>e.subscribe(i),n))})}function dv(t,n){return dt(t).pipe(kl(n),Dl(n))}function uv(t,n){return dt(t).pipe(kl(n),Dl(n))}function mv(t,n){return new Ce(e=>{let i=0;return n.schedule(function(){i===t.length?e.complete():(e.next(t[i++]),e.closed||this.schedule())})})}function pv(t,n){return new Ce(e=>{let i;return un(e,n,()=>{i=t[Cl](),un(e,n,()=>{let a,o;try{({value:a,done:o}=i.next())}catch(r){e.error(r);return}o?e.complete():e.next(a)},0,!0)}),()=>Me(i?.return)&&i.return()})}function Il(t,n){if(!t)throw new Error(`Iterable cannot be null`);return new Ce(e=>{un(e,n,()=>{let i=t[Symbol.asyncIterator]();un(e,n,()=>{i.next().then(a=>{a.done?e.complete():e.next(a.value)})},0,!0)})})}function hv(t,n){return Il(wl(t),n)}function fv(t,n){if(t!=null){if(yl(t))return dv(t,n);if(bl(t))return mv(t,n);if(vl(t))return uv(t,n);if(_l(t))return Il(t,n);if(Sl(t))return pv(t,n);if(El(t))return hv(t,n)}throw xl(t)}function In(t,n){return n?fv(t,n):dt(t)}function Mn(...t){return In(t,gi(t))}var gv=qo(t=>function(){t(this),this.name=`EmptyError`,this.message=`no elements in sequence`});function Rt(t,n){let e=typeof n==`object`;return new Promise((i,a)=>{let o=new Wn({next:r=>{i(r),o.unsubscribe()},error:a,complete:()=>{e?i(n.defaultValue):a(new gv)}});t.subscribe(o)})}function bv(t){return t instanceof Date&&!isNaN(t)}function rt(t,n){return Le((e,i)=>{let a=0;e.subscribe(je(i,o=>{i.next(t.call(n,o,a++))}))})}var{isArray:ME}=Array;function NE(t,n){return ME(n)?t(...n):t(n)}function Ml(t){return rt(n=>NE(t,n))}var{isArray:TE}=Array,{getPrototypeOf:AE,prototype:OE,keys:RE}=Object;function Nl(t){if(t.length===1){let n=t[0];if(TE(n))return{args:n,keys:null};if(PE(n)){let e=RE(n);return{args:e.map(i=>n[i]),keys:e}}}return{args:t,keys:null}}function PE(t){return t&&typeof t==`object`&&AE(t)===OE}function Tl(t,n){return t.reduce((e,i,a)=>(e[i]=n[a],e),{})}function ap(...t){let n=gi(t),e=gl(t),{args:i,keys:a}=Nl(t);if(i.length===0)return In([],n);let o=new Ce(FE(i,n,a?r=>Tl(a,r):qn));return e?o.pipe(Ml(e)):o}function FE(t,n,e=qn){return i=>{vv(n,()=>{let{length:a}=t,o=new Array(a),r=a,s=a;for(let c=0;c<a;c++)vv(n,()=>{let p=In(t[c],n),m=!1;p.subscribe(je(i,g=>{o[c]=g,m||(m=!0,s--),s||i.next(e(o.slice()))},()=>{--r||i.complete()}))},i)},i)}}function vv(t,n,e){t?un(e,t,n):n()}function yv(t,n,e,i,a,o,r,s){let c=[],p=0,m=0,g=!1,v=()=>{g&&!c.length&&!p&&n.complete()},b=E=>p<i?_(E):c.push(E),_=E=>{o&&n.next(E),p++;let P=!1;dt(e(E,m++)).subscribe(je(n,ae=>{a?.(ae),o?b(ae):n.next(ae)},()=>{P=!0},void 0,()=>{if(P)try{for(p--;c.length&&p<i;){let ae=c.shift();r?un(n,r,()=>_(ae)):_(ae)}v()}catch(ae){n.error(ae)}}))};return t.subscribe(je(n,b,()=>{g=!0,v()})),()=>{s?.()}}function Zo(t,n,e=1/0){return Me(n)?Zo((i,a)=>rt((o,r)=>n(i,o,a,r))(dt(t(i,a))),e):(typeof n==`number`&&(e=n),Le((i,a)=>yv(i,a,t,e)))}function Al(t=1/0){return Zo(qn,t)}function _v(){return Al(1)}function Jo(...t){return _v()(In(t,gi(t)))}function Ka(t){return new Ce(n=>{dt(t()).subscribe(n)})}function op(...t){let n=gl(t),{args:e,keys:i}=Nl(t),a=new Ce(o=>{let{length:r}=e;if(!r){o.complete();return}let s=new Array(r),c=r,p=r;for(let m=0;m<r;m++){let g=!1;dt(e[m]).subscribe(je(o,v=>{g||(g=!0,p--),s[m]=v},()=>c--,void 0,()=>{(!c||!g)&&(p||o.next(i?Tl(i,s):s),o.complete())}))}});return n?a.pipe(Ml(n)):a}function vs(t=0,n,e=iv){let i=-1;return n!=null&&(fl(n)?e=n:i=n),new Ce(a=>{let o=bv(t)?+t-e.now():t;o<0&&(o=0);let r=0;return e.schedule(function(){a.closed||(a.next(r++),0<=i?this.schedule(void 0,i):a.complete())},o)})}function Dt(...t){let n=gi(t),e=av(t,1/0),i=t;return i.length?i.length===1?dt(i[0]):Al(e)(In(i,n)):Ri}function Qe(t,n){return Le((e,i)=>{let a=0;e.subscribe(je(i,o=>t.call(n,o,a++)&&i.next(o)))})}function xv(t){return Le((n,e)=>{let i=!1,a=null,o=null,r=!1,s=()=>{if(o?.unsubscribe(),o=null,i){i=!1;let p=a;a=null,e.next(p)}r&&e.complete()},c=()=>{o=null,r&&e.complete()};n.subscribe(je(e,p=>{i=!0,a=p,o||dt(t(p)).subscribe(o=je(e,s,c))},()=>{r=!0,(!i||!o||o.closed)&&e.complete()}))})}function Ol(t,n=bs){return xv(()=>vs(t,n))}function rp(t,n){return Me(n)?Zo(t,n,1):Zo(t,1)}function Pi(t,n=bs){return Le((e,i)=>{let a=null,o=null,r=null,s=()=>{if(a){a.unsubscribe(),a=null;let p=o;o=null,i.next(p)}};function c(){let p=r+t,m=n.now();if(m<p){a=this.schedule(void 0,p-m),i.add(a);return}s()}e.subscribe(je(i,p=>{o=p,r=n.now(),a||(a=n.schedule(c,t),i.add(a))},()=>{s(),i.complete()},void 0,()=>{o=a=null}))})}function bn(t){return t<=0?()=>Ri:Le((n,e)=>{let i=0;n.subscribe(je(e,a=>{++i<=t&&(e.next(a),t<=i&&e.complete())}))})}function Rl(t,n=qn){return t=t??LE,Le((e,i)=>{let a,o=!0;e.subscribe(je(i,r=>{let s=n(r);(o||!t(a,s))&&(o=!1,a=s,i.next(r))}))})}function LE(t,n){return t===n}function sp(t){return Le((n,e)=>{try{n.subscribe(e)}finally{e.add(t)}})}function Pl(){return Le((t,n)=>{let e,i=!1;t.subscribe(je(n,a=>{let o=e;e=a,i&&n.next([o,a]),i=!0}))})}function Cv(t={}){let{connector:n=()=>new L,resetOnError:e=!0,resetOnComplete:i=!0,resetOnRefCountZero:a=!0}=t;return o=>{let r,s,c,p=0,m=!1,g=!1,v=()=>{s?.unsubscribe(),s=void 0},b=()=>{v(),r=c=void 0,m=g=!1},_=()=>{let E=r;b(),E?.unsubscribe()};return Le((E,P)=>{p++,!g&&!m&&v();let ae=c=c??n();P.add(()=>{p--,p===0&&!g&&!m&&(s=cp(_,a))}),ae.subscribe(P),!r&&p>0&&(r=new Wn({next:j=>ae.next(j),error:j=>{g=!0,v(),s=cp(b,e,j),ae.error(j)},complete:()=>{m=!0,v(),s=cp(b,i),ae.complete()}}),dt(E).subscribe(r))})(o)}}function cp(t,n,...e){if(n===!0){t();return}if(n===!1)return;let i=new Wn({next:()=>{i.unsubscribe(),t()}});return dt(n(...e)).subscribe(i)}function Fl(t,n,e){let i,a=!1;return t&&typeof t==`object`?{bufferSize:i=1/0,windowTime:n=1/0,refCount:a=!1,scheduler:e}=t:i=t??1/0,Cv({connector:()=>new Yn(i,n,e),resetOnError:!0,resetOnComplete:!1,resetOnRefCountZero:a})}function Qa(t){return Qe((n,e)=>t<=e)}function ut(...t){let n=gi(t);return Le((e,i)=>{(n?Jo(t,e,n):Jo(t,e)).subscribe(i)})}function vn(t,n){return Le((e,i)=>{let a=null,o=0,r=!1,s=()=>r&&!a&&i.complete();e.subscribe(je(i,c=>{a?.unsubscribe();let p=0,m=o++;dt(t(c,m)).subscribe(a=je(i,g=>i.next(n?n(c,g,m,p++):g),()=>{a=null,s()}))},()=>{r=!0,s()}))})}function Ne(t){return Le((n,e)=>{dt(t).subscribe(je(e,()=>e.complete(),hs)),!e.closed&&n.subscribe(e)})}function lp(t,n=!1){return Le((e,i)=>{let a=0;e.subscribe(je(i,o=>{let r=t(o,a++);(r||n)&&i.next(o),!r&&i.complete()}))})}function dp(t,n,e){let i=Me(t)||n||e?{next:t,error:n,complete:e}:t;return i?Le((a,o)=>{var r;(r=i.subscribe)===null||r===void 0||r.call(i);let s=!0;a.subscribe(je(o,c=>{var p;(p=i.next)===null||p===void 0||p.call(i,c),o.next(c)},()=>{var c;s=!1,(c=i.complete)===null||c===void 0||c.call(i),o.complete()},c=>{var p;s=!1,(p=i.error)===null||p===void 0||p.call(i,c),o.error(c)},()=>{var c,p;s&&((c=i.unsubscribe)===null||c===void 0||c.call(i)),(p=i.finalize)===null||p===void 0||p.call(i)}))}):qn}var Ul=`https://angular.dev/best-practices/security#preventing-cross-site-scripting-xss`;var G=class extends Error{code;constructor(n,e){super(Bi(n,e)),this.code=n}};function VE(t){return`NG0${Math.abs(t)}`}function Bi(t,n){return`${VE(t)}${n?`: `+n:``}`}function Ye(t){for(let n in t)if(t[n]===Ye)return n;throw Error(``)}function kv(t,n){for(let e in n)Object.hasOwn(n,e)&&!Object.hasOwn(t,e)&&(t[e]=n[e])}function ws(t){if(typeof t==`string`)return t;if(Array.isArray(t))return`[${t.map(ws).join(`, `)}]`;if(t==null)return``+t;let n=t.overriddenName||t.name;if(n)return`${n}`;let e=t.toString();if(e==null)return``+e;let i=e.indexOf(`
`);return i>=0?e.slice(0,i):e}function $l(t,n){return t?n?`${t} ${n}`:t:n||``}var BE=Ye({__forward_ref__:Ye});function Ht(t){return t.__forward_ref__=Ht,t}function Pt(t){return wp(t)?t():t}function wp(t){return typeof t==`function`&&Object.hasOwn(t,BE)&&t.__forward_ref__===Ht}function fe(t){return{token:t.token,providedIn:t.providedIn||null,factory:t.factory,value:void 0}}function ie(t){return{providers:t.providers||[],imports:t.imports||[]}}function Gl(t){return jE(t,Wl)}function jE(t,n){return Object.hasOwn(t,n)&&t[n]||null}function zE(t){return(t?.[Wl]??null)||null}function mp(t){return t&&Object.hasOwn(t,Vl)?t[Vl]:null}var Wl=Ye({ɵprov:Ye});var Vl=Ye({ɵinj:Ye});var I=class{_desc;ngMetadataName=`InjectionToken`;ɵprov;constructor(n,e){this._desc=n,this.ɵprov=void 0,typeof e==`number`?this.__NG_ELEMENT_ID__=e:e!==void 0&&(this.ɵprov=fe({token:this,providedIn:e.providedIn||`root`,factory:e.factory}))}get multi(){return this}toString(){return`InjectionToken ${this._desc}`}};function Ep(t){return t&&!!t.ɵproviders}var Es=Ye({ɵcmp:Ye});var Ds=Ye({ɵdir:Ye});var Dp=Ye({ɵpipe:Ye});var kp=Ye({ɵmod:Ye});var _s=Ye({ɵfac:Ye});var to=Ye({__NG_ELEMENT_ID__:Ye});var Sv=Ye({__NG_ENV_ID__:Ye});function Iv(t){return ql(t,`@NgModule`),t[kp]||null}function pa(t){return ql(t,`@Component`),t[Es]||null}function ks(t){return ql(t,`@Directive`),t[Ds]||null}function Mv(t){return ql(t,`@Pipe`),t[Dp]||null}function ql(t,n){if(t==null)throw new G(-919,!1)}function $e(t){return typeof t==`string`?t:t==null?``:String(t)}var Nv=Ye({ngErrorCode:Ye});var HE=Ye({ngErrorMessage:Ye});var UE=Ye({ngTokenPath:Ye});function Ip(t,n){return Tv(``,-200,n)}function Yl(t,n){throw new G(-201,!1)}function Tv(t,n,e){let i=new G(n,t);return i[Nv]=n,i[HE]=t,e&&(i[UE]=e),i}function $E(t){return t[Nv]}var pp;function Av(){return pp}function on(t){let n=pp;return pp=t,n}function Mp(t,n,e){let i=Gl(t);if(i&&i.providedIn==`root`)return i.value===void 0?i.value=i.factory():i.value;if(e&8)return null;if(n!==void 0)return n;Yl(t,``)}var vi=globalThis;var Xa={};var WE=`__NG_DI_FLAG__`;var hp=class{injector;constructor(n){this.injector=n}retrieve(n,e){let i=Za(e)||0;try{return this.injector.get(n,i&8?null:Xa,i)}catch(a){if(Wo(a))return a;throw a}}};function qE(t,n=0){let e=rl();if(e===void 0)throw new G(-203,!1);if(e===null)return Mp(t,void 0,n);{let i=YE(n),a=e.retrieve(t,i);if(Wo(a)){if(i.optional)return null;throw a}return a}}function Se(t,n=0){return(Av()||qE)(Pt(t),n)}function f(t,n){return Se(t,Za(n))}function Za(t){return typeof t>`u`||typeof t==`number`?t:0|(t.optional&&8)|(t.host&&1)|(t.self&&2)|(t.skipSelf&&4)}function YE(t){return{optional:!!(t&8),host:!!(t&1),self:!!(t&2),skipSelf:!!(t&4)}}function fp(t){let n=[];for(let e=0;e<t.length;e++){let i=Pt(t[e]);if(Array.isArray(i)){if(i.length===0)throw new G(900,!1);let a,o=0;for(let r=0;r<i.length;r++){let s=i[r],c=KE(s);typeof c==`number`?c===-1?a=s.token:o|=c:a=s}n.push(Se(a,o))}else n.push(Se(i))}return n}function KE(t){return t[WE]}function ma(t,n){return Object.hasOwn(t,_s)?t[_s]:null}function Ov(t,n,e){if(t.length!==n.length)return!1;for(let i=0;i<t.length;i++){let a=t[i],o=n[i];if(e&&(a=e(a),o=e(o)),o!==a)return!1}return!0}function Rv(t){return t.flat(Number.POSITIVE_INFINITY)}function Kl(t,n){t.forEach(e=>Array.isArray(e)?Kl(e,n):n(e))}function Np(t,n,e){n>=t.length?t.push(e):t.splice(n,0,e)}function Is(t,n){return n>=t.length-1?t.pop():t.splice(n,1)[0]}function Pv(t,n){let e=[];for(let i=0;i<t;i++)e.push(n);return e}function Fv(t,n,e,i){let a=t.length;if(a==n)t.push(e,i);else if(a===1)t.push(i,t[0]),t[0]=e;else{for(a--,t.push(t[a-1],t[a]);a>n;){let o=a-2;t[a]=t[o],a--}t[n]=e,t[n+1]=i}}function Ql(t,n,e){let i=nr(t,n);return i>=0?t[i|1]=e:(i=~i,Fv(t,i,n,e)),i}function Xl(t,n){let e=nr(t,n);if(e>=0)return t[e|1]}function nr(t,n){return QE(t,n,1)}function QE(t,n,e){let i=0,a=t.length>>e;for(;a!==i;){let o=i+(a-i>>1),r=t[o<<e];if(n===r)return o<<e;r>n?a=o:i=o+1}return~(a<<e)}var ha={};var Kt=[];var no=new I(``);var Zl=new I(``,-1);var Tp=new I(``);var tr=class{get(n,e=Xa){if(e===Xa){let a=Tv(``,-201);throw a.name=`ɵNotFound`,a}return e}};function rn(t){return{ɵproviders:t}}function Lv(t){return rn([{provide:no,multi:!0,useValue:t}])}function Vv(...t){return{ɵproviders:Ap(!0,t),ɵfromNgModule:!0}}function Ap(t,...n){let e=[],i=new Set,a,o=r=>{e.push(r)};return Kl(n,r=>{let s=r;Bl(s,o,[],i)&&(a||=[],a.push(s))}),a!==void 0&&Bv(a,o),e}function Bv(t,n){for(let e=0;e<t.length;e++){let{ngModule:i,providers:a}=t[e];Op(a,o=>{n(o,i)})}}function Bl(t,n,e,i){if(t=Pt(t),!t)return!1;let a=null,o=mp(t),r=!o&&pa(t);if(!o&&!r){let c=t.ngModule;if(o=mp(c),o)a=c;else return!1}else{if(r&&!r.standalone)return!1;a=t}let s=i.has(a);if(r){if(s)return!1;if(i.add(a),r.dependencies){let c=typeof r.dependencies==`function`?r.dependencies():r.dependencies;for(let p of c)Bl(p,n,e,i)}}else if(o){if(o.imports!=null&&!s){i.add(a);let p;Kl(o.imports,m=>{Bl(m,n,e,i)&&(p||=[],p.push(m))}),p!==void 0&&Bv(p,n)}if(!s){let p=ma(a)||(()=>new a);n({provide:a,useFactory:p,deps:Kt},a),n({provide:Tp,useValue:a,multi:!0},a),n({provide:no,useValue:()=>Se(a),multi:!0},a)}let c=o.providers;if(c!=null&&!s){let p=t;Op(c,m=>{n(m,p)})}}else return!1;return a!==t&&t.providers!==void 0}function Op(t,n){for(let e of t)Ep(e)&&(e=e.ɵproviders),Array.isArray(e)?Op(e,n):n(e)}var XE=Ye({provide:String,useValue:Ye});function jv(t){return t!==null&&typeof t==`object`&&XE in t}function ZE(t){return!!(t&&t.useExisting)}function JE(t){return!!(t&&t.useFactory)}function Ja(t){return typeof t==`function`}function zv(t){return!!t.useClass}var Ms=new I(``);var Ll={};var wv={};var up;function ir(){return up===void 0&&(up=new tr),up}var kt=class{};var eo=class extends kt{parent;source;scopes;records=new Map;_ngOnDestroyHooks=new Set;_onDestroyHooks=[];get destroyed(){return this._destroyed}_destroyed=!1;injectorDefTypes;constructor(n,e,i,a){super(),this.parent=e,this.source=i,this.scopes=a,bp(n,r=>this.processProvider(r)),this.records.set(Zl,er(void 0,this)),a.has(`environment`)&&this.records.set(kt,er(void 0,this));let o=this.records.get(Ms);o!=null&&typeof o.value==`string`&&this.scopes.add(o.value),this.injectorDefTypes=new Set(this.get(Tp,Kt,{self:!0}))}retrieve(n,e){let i=Za(e)||0;try{return this.get(n,Xa,i)}catch(a){if(Wo(a))return a;throw a}}destroy(){ys(this),this._destroyed=!0;let n=he(null);try{for(let i of this._ngOnDestroyHooks)i.ngOnDestroy();let e=this._onDestroyHooks;this._onDestroyHooks=[];for(let i of e)i()}finally{this.records.clear(),this._ngOnDestroyHooks.clear(),this.injectorDefTypes.clear(),he(n)}}onDestroy(n){return ys(this),this._onDestroyHooks.push(n),()=>this.removeOnDestroy(n)}runInContext(n){ys(this);let e=hi(this),i=on(void 0);try{return n()}finally{hi(e),on(i)}}get(n,e=Xa,i){if(ys(this),Object.hasOwn(n,Sv))return n[Sv](this);let a=Za(i),r=hi(this),s=on(void 0);try{if(!(a&4)){let p=this.records.get(n);if(p===void 0){let m=aD(n)&&Gl(n);m&&this.injectableDefInScope(m)?p=er(gp(n),Ll):p=null,this.records.set(n,p)}if(p!=null)return this.hydrate(n,p,a)}let c=a&2?ir():this.parent;return e=a&8&&e===Xa?null:e,c.get(n,e)}catch(c){let p=$E(c);throw p===-200||p===-201?new G(p,null):c}finally{on(s),hi(r)}}resolveInjectorInitializers(){let n=he(null),e=hi(this),i=on(void 0);try{let o=this.get(no,Kt,{self:!0});for(let r of o)r()}finally{hi(e),on(i),he(n)}}toString(){return`R3Injector[...]`}processProvider(n){n=Pt(n);let e=Ja(n)?n:Pt(n&&n.provide),i=tD(n);if(!Ja(n)&&n.multi===!0){let a=this.records.get(e);a||(a=er(void 0,Ll,!0),a.factory=()=>fp(a.multi),this.records.set(e,a)),e=n,a.multi.push(n)}this.records.set(e,i)}hydrate(n,e,i){let a=he(null);try{if(e.value===wv)throw Ip(``);return e.value===Ll&&(e.value=wv,e.value=e.factory(void 0,i)),typeof e.value==`object`&&e.value&&iD(e.value)&&this._ngOnDestroyHooks.add(e.value),e.value}finally{he(a)}}injectableDefInScope(n){if(!n.providedIn)return!1;let e=Pt(n.providedIn);return typeof e==`string`?e===`any`||this.scopes.has(e):this.injectorDefTypes.has(e)}removeOnDestroy(n){let e=this._onDestroyHooks.indexOf(n);e!==-1&&this._onDestroyHooks.splice(e,1)}};function gp(t){let n=Gl(t),e=n!==null?n.factory:ma(t);if(e!==null)return e;if(t instanceof I)throw new G(-204,!1);if(t instanceof Function)return eD(t);throw new G(-204,!1)}function eD(t){if(t.length>0)throw new G(-204,!1);let e=zE(t);return e!==null?()=>e.factory(t):()=>new t}function tD(t){if(jv(t))return er(void 0,t.useValue);return er(Rp(t),Ll)}function Rp(t,n,e){let i;if(Ja(t)){let a=Pt(t);return ma(a)||gp(a)}else if(jv(t))i=()=>Pt(t.useValue);else if(JE(t))i=()=>t.useFactory(...fp(t.deps||[]));else if(ZE(t))i=(a,o)=>Se(Pt(t.useExisting),o!==void 0&&o&8?8:void 0);else{let a=Pt(t&&(t.useClass||t.provide));if(nD(t))i=()=>new a(...fp(t.deps));else return ma(a)||gp(a)}return i}function ys(t){if(t.destroyed)throw new G(-205,!1)}function er(t,n,e=!1){return{factory:t,value:n,multi:e?[]:void 0}}function nD(t){return!!t.deps}function iD(t){return t!==null&&typeof t==`object`&&typeof t.ngOnDestroy==`function`}function aD(t){return typeof t==`function`||typeof t==`object`&&t.ngMetadataName===`InjectionToken`}function bp(t,n){for(let e of t)Array.isArray(e)?bp(e,n):e&&Ep(e)?bp(e.ɵproviders,n):n(e)}function ar(t,n){let e;t instanceof eo?(ys(t),e=t):e=new hp(t);let a=hi(e),o=on(void 0);try{return n()}finally{hi(a),on(o)}}function Hv(){return Av()!==void 0||rl()!=null}var Kn=0;var ge=1;var xe=2;var St=3;var Tn=4;var Ut=5;var io=6;var or=7;var yt=8;var ji=9;var Qn=10;var et=11;var rr=12;var Pp=13;var fa=14;var Qt=15;var ga=16;var ao=17;var yi=18;var Ns=19;var Fp=20;var Fi=21;var Jl=22;var Li=23;var yn=24;var oo=25;var Uv=26;var sr=27;var at=28;var $v=1;var Lp=6;var ro=7;var Ts=8;var so=9;var _t=10;function zi(t){return Array.isArray(t)&&typeof t[$v]==`object`}function An(t){return Array.isArray(t)&&t[$v]===!0}function Vp(t){return(t.flags&4)!==0}function _i(t){return t.componentOffset>-1}function cr(t){return(t.flags&1)===1}function Xn(t){return!!t.template}function lr(t){return(t[xe]&512)!==0}function co(t){return(t[xe]&256)===256}var _n=(function(t){return t[t.NONE=0]=`NONE`,t[t.HTML=1]=`HTML`,t[t.STYLE=2]=`STYLE`,t[t.SCRIPT=3]=`SCRIPT`,t[t.URL=4]=`URL`,t[t.RESOURCE_URL=5]=`RESOURCE_URL`,t[t.ATTRIBUTE_NO_BINDING=6]=`ATTRIBUTE_NO_BINDING`,t})(_n||{});var Bp=`svg`;var Gv=`math`;function $t(t){for(;Array.isArray(t);)t=t[Kn];return t}function jp(t,n){return $t(n[t])}function xn(t,n){return $t(n[t.index])}function ed(t,n){return t.data[n]}function As(t,n){return t[n]}function Os(t,n,e,i){e>=t.data.length&&(t.data[e]=null,t.blueprint[e]=null),n[e]=i}function On(t,n){let e=n[t];return zi(e)?e:e[Kn]}function Wv(t){return(t[xe]&4)===4}function td(t){return(t[xe]&128)===128}function qv(t){return An(t[St])}function Cn(t,n){return n==null?null:t[n]}function zp(t){t[ao]=0}function Hp(t){t[xe]&1024||(t[xe]|=1024,td(t)&&lo(t))}function Yv(t,n){for(;t>0;)n=n[fa],t--;return n}function Rs(t){return!!(t[xe]&9216||t[yn]?.dirty)}function nd(t){t[Qn].changeDetectionScheduler?.notify(8),t[xe]&64&&(t[xe]|=1024),Rs(t)&&lo(t)}function lo(t){t[Qn].changeDetectionScheduler?.notify(0);let n=Vi(t);for(;n!==null&&!(n[xe]&8192||(n[xe]|=8192,!td(n)));)n=Vi(n)}function id(t,n){if(co(t))throw new G(911,!1);t[Fi]===null&&(t[Fi]=[]),t[Fi].push(n)}function Kv(t,n){if(t[Fi]===null)return;let e=t[Fi].indexOf(n);e!==-1&&t[Fi].splice(e,1)}function Vi(t){let n=t[St];return An(n)?n[St]:n}function Up(t){return t[or]??=[]}function $p(t){return t.cleanup??=[]}function Qv(t,n,e,i){let a=Up(n);a.push(e),t.firstCreatePass&&$p(t).push(i,a.length-1)}var Ee={lFrame:ry(null),bindingsEnabled:!0,skipHydrationRootTNode:null};var vp=!1;function Xv(){return Ee.lFrame.elementDepthCount}function Zv(){Ee.lFrame.elementDepthCount++}function Gp(){Ee.lFrame.elementDepthCount--}function ad(){return Ee.bindingsEnabled}function Wp(){return Ee.skipHydrationRootTNode!==null}function qp(t){return Ee.skipHydrationRootTNode===t}function Yp(){Ee.skipHydrationRootTNode=null}function le(){return Ee.lFrame.lView}function Xe(){return Ee.lFrame.tView}function A(t){return Ee.lFrame.contextLView=t,t[yt]}function O(t){return Ee.lFrame.contextLView=null,t}function It(){let t=Kp();for(;t!==null&&t.type===64;)t=t.parent;return t}function Kp(){return Ee.lFrame.currentTNode}function Jv(){let t=Ee.lFrame,n=t.currentTNode;return t.isParent?n:n.parent}function uo(t,n){let e=Ee.lFrame;e.currentTNode=t,e.isParent=n}function Qp(){return Ee.lFrame.isParent}function Xp(){Ee.lFrame.isParent=!1}function Zp(){return Ee.lFrame.contextLView}function Jp(){return vp}function xs(t){let n=vp;return vp=t,n}function od(){let t=Ee.lFrame,n=t.bindingRootIndex;return n===-1&&(n=t.bindingRootIndex=t.tView.bindingStartIndex),n}function mo(){return Ee.lFrame.bindingIndex}function ey(t){return Ee.lFrame.bindingIndex=t}function ba(){return Ee.lFrame.bindingIndex++}function Hi(t){let n=Ee.lFrame,e=n.bindingIndex;return n.bindingIndex=n.bindingIndex+t,e}function ty(){return Ee.lFrame.inI18n}function ny(t,n){let e=Ee.lFrame;e.bindingIndex=e.bindingRootIndex=t,rd(n)}function iy(){return Ee.lFrame.currentDirectiveIndex}function rd(t){Ee.lFrame.currentDirectiveIndex=t}function ay(t){let n=Ee.lFrame.currentDirectiveIndex;return n===-1?null:t[n]}function sd(){return Ee.lFrame.currentQueryIndex}function Ps(t){Ee.lFrame.currentQueryIndex=t}function oD(t){let n=t[ge];return n.type===2?n.declTNode:n.type===1?t[Ut]:null}function eh(t,n,e){if(e&4){let a=n,o=t;for(;a=a.parent,a===null&&!(e&1);)if(a=oD(o),a===null||(o=o[fa],a.type&10))break;if(a===null)return!1;n=a,t=o}let i=Ee.lFrame=oy();return i.currentTNode=n,i.lView=t,!0}function cd(t){let n=oy(),e=t[ge];Ee.lFrame=n,n.currentTNode=e.firstChild,n.lView=t,n.tView=e,n.contextLView=t,n.bindingIndex=e.bindingStartIndex,n.inI18n=!1}function oy(){let t=Ee.lFrame,n=t===null?null:t.child;return n===null?ry(t):n}function ry(t){let n={currentTNode:null,isParent:!0,lView:null,tView:null,selectedIndex:-1,contextLView:null,elementDepthCount:0,currentNamespace:null,currentDirectiveIndex:-1,bindingRootIndex:-1,bindingIndex:-1,currentQueryIndex:0,parent:t,child:null,inI18n:!1};return t!==null&&(t.child=n),n}function sy(){let t=Ee.lFrame;return Ee.lFrame=t.parent,t.currentTNode=null,t.lView=null,t}var th=sy;function ld(){let t=sy();t.isParent=!0,t.tView=null,t.selectedIndex=-1,t.contextLView=null,t.elementDepthCount=0,t.currentDirectiveIndex=-1,t.currentNamespace=null,t.bindingRootIndex=-1,t.bindingIndex=-1,t.currentQueryIndex=0}function cy(t){return(Ee.lFrame.contextLView=Yv(t,Ee.lFrame.contextLView))[yt]}function Xt(){return Ee.lFrame.selectedIndex}function va(t){Ee.lFrame.selectedIndex=t}function dr(){let t=Ee.lFrame;return ed(t.tView,t.selectedIndex)}function Gt(){Ee.lFrame.currentNamespace=Bp}function ya(){rD()}function rD(){Ee.lFrame.currentNamespace=null}function nh(){return Ee.lFrame.currentNamespace}var ly=!0;function dd(){return ly}function Fs(t){ly=t}function yp(t,n=null,e=null,i){let a=ih(t,n,e,i);return a.resolveInjectorInitializers(),a}function ih(t,n=null,e=null,i,a=new Set){return new eo([e||Kt,Vv(t)],n||ir(),null,a)}var re=class t{static THROW_IF_NOT_FOUND=Xa;static NULL=new tr;static create(n,e){if(Array.isArray(n))return yp({name:``},e,n,``);{let i=n.name??``;return yp({name:i},n.parent,n.providers,i)}}static ɵprov=fe({token:t,providedIn:`any`,factory:()=>Se(Zl)});static __NG_ELEMENT_ID__=-1};var de=new I(``);var Te=class{static __NG_ELEMENT_ID__=sD;static __NG_ENV_ID__=n=>n};var jl=class extends Te{_lView;constructor(n){super(),this._lView=n}get destroyed(){return co(this._lView)}onDestroy(n){let e=this._lView;return id(e,n),()=>Kv(e,n)}};function sD(){return new jl(le())}var dy=!1;var uy=new I(``);var po=(()=>{class t{taskId=0;pendingTasks=new Set;destroyed=!1;pendingTask=new fi(!1);debugTaskTracker=f(uy,{optional:!0});get hasPendingTasks(){return this.destroyed?!1:this.pendingTask.value}get hasPendingTasksObservable(){return this.destroyed?new Ce(e=>{e.next(!1),e.complete()}):this.pendingTask}add(){!this.hasPendingTasks&&!this.destroyed&&this.pendingTask.next(!0);let e=this.taskId++;return this.pendingTasks.add(e),this.debugTaskTracker?.add(e),e}has(e){return this.pendingTasks.has(e)}remove(e){this.pendingTasks.delete(e),this.debugTaskTracker?.remove(e),this.pendingTasks.size===0&&this.hasPendingTasks&&this.pendingTask.next(!1)}ngOnDestroy(){this.pendingTasks.clear(),this.hasPendingTasks&&this.pendingTask.next(!1),this.destroyed=!0,this.pendingTask.unsubscribe()}static ɵprov=fe({token:t,providedIn:`root`,factory:()=>new t})}return t})();var _p=class extends L{__isAsync;destroyRef=void 0;pendingTasks=void 0;constructor(n=!1){super(),this.__isAsync=n,Hv()&&(this.destroyRef=f(Te,{optional:!0})??void 0,this.pendingTasks=f(po,{optional:!0})??void 0)}emit(n){let e=he(null);try{super.next(n)}finally{he(e)}}subscribe(n,e,i){let a=n,o=e||(()=>null),r=i;if(n&&typeof n==`object`){let c=n;a=c.next?.bind(c),o=c.error?.bind(c),r=c.complete?.bind(c)}this.__isAsync&&(o=this.wrapInTimeout(o),a&&(a=this.wrapInTimeout(a)),r&&(r=this.wrapInTimeout(r)));let s=super.subscribe({next:a,error:o,complete:r});return n instanceof De&&n.add(s),s}wrapInTimeout(n){return e=>{let i=this.pendingTasks?.add();setTimeout(()=>{try{n(e)}finally{i!==void 0&&this.pendingTasks?.remove(i)}})}}};var ee=_p;function zl(...t){}function ah(t){let n,e;function i(){t=zl;try{e!==void 0&&typeof cancelAnimationFrame==`function`&&cancelAnimationFrame(e),n!==void 0&&clearTimeout(n)}catch{}}return n=setTimeout(()=>{t(),i()}),typeof requestAnimationFrame==`function`&&(e=requestAnimationFrame(()=>{t(),i()})),()=>i()}function my(t){return queueMicrotask(()=>t()),()=>{t=zl}}var oh=`isAngularZone`;var Cs=oh+`_ID`;var cD=0;var Q=class t{hasPendingMacrotasks=!1;hasPendingMicrotasks=!1;isStable=!0;onUnstable=new ee(!1);onMicrotaskEmpty=new ee(!1);onStable=new ee(!1);onError=new ee(!1);constructor(n){let{enableLongStackTrace:e=!1,shouldCoalesceEventChangeDetection:i=!1,shouldCoalesceRunChangeDetection:a=!1,scheduleInRootZone:o=dy}=n;if(typeof Zone>`u`)throw new G(908,!1);Zone.assertZonePatched();let r=this;r._nesting=0,r._outer=r._inner=Zone.current,Zone.TaskTrackingZoneSpec&&(r._inner=r._inner.fork(new Zone.TaskTrackingZoneSpec)),e&&Zone.longStackTraceZoneSpec&&(r._inner=r._inner.fork(Zone.longStackTraceZoneSpec)),r.shouldCoalesceEventChangeDetection=!a&&i,r.shouldCoalesceRunChangeDetection=a,r.callbackScheduled=!1,r.scheduleInRootZone=o,uD(r)}static isInAngularZone(){return typeof Zone<`u`&&Zone.current.get(oh)===!0}static assertInAngularZone(){if(!t.isInAngularZone())throw new G(909,!1)}static assertNotInAngularZone(){if(t.isInAngularZone())throw new G(909,!1)}run(n,e,i){return this._inner.run(n,e,i)}runTask(n,e,i,a){let o=this._inner,r=o.scheduleEventTask(`NgZoneEvent: `+a,n,lD,zl,zl);try{return o.runTask(r,e,i)}finally{o.cancelTask(r)}}runGuarded(n,e,i){return this._inner.runGuarded(n,e,i)}runOutsideAngular(n){return this._outer.run(n)}};var lD={};function rh(t){if(t._nesting==0&&!t.hasPendingMicrotasks&&!t.isStable)try{t._nesting++,t.onMicrotaskEmpty.emit(null)}finally{if(t._nesting--,!t.hasPendingMicrotasks)try{t.runOutsideAngular(()=>t.onStable.emit(null))}finally{t.isStable=!0}}}function dD(t){if(t.isCheckStableRunning||t.callbackScheduled)return;t.callbackScheduled=!0;function n(){ah(()=>{t.callbackScheduled=!1,xp(t),t.isCheckStableRunning=!0,rh(t),t.isCheckStableRunning=!1})}t.scheduleInRootZone?Zone.root.run(()=>{n()}):t._outer.run(()=>{n()}),xp(t)}function uD(t){let n=()=>{dD(t)},e=cD++;t._inner=t._inner.fork({name:`angular`,properties:{[oh]:!0,[Cs]:e,[Cs+e]:!0},onInvokeTask:(i,a,o,r,s,c)=>{if(mD(c))return i.invokeTask(o,r,s,c);try{return Ev(t),i.invokeTask(o,r,s,c)}finally{(t.shouldCoalesceEventChangeDetection&&r.type===`eventTask`||t.shouldCoalesceRunChangeDetection)&&n(),Dv(t)}},onInvoke:(i,a,o,r,s,c,p)=>{try{return Ev(t),i.invoke(o,r,s,c,p)}finally{t.shouldCoalesceRunChangeDetection&&!t.callbackScheduled&&!pD(c)&&n(),Dv(t)}},onHasTask:(i,a,o,r)=>{i.hasTask(o,r),a===o&&(r.change==`microTask`?(t._hasPendingMicrotasks=r.microTask,xp(t),rh(t)):r.change==`macroTask`&&(t.hasPendingMacrotasks=r.macroTask))},onHandleError:(i,a,o,r)=>(i.handleError(o,r),t.runOutsideAngular(()=>t.onError.emit(r)),!1)})}function xp(t){t._hasPendingMicrotasks||(t.shouldCoalesceEventChangeDetection||t.shouldCoalesceRunChangeDetection)&&t.callbackScheduled===!0?t.hasPendingMicrotasks=!0:t.hasPendingMicrotasks=!1}function Ev(t){t._nesting++,t.isStable&&(t.isStable=!1,t.onUnstable.emit(null))}function Dv(t){t._nesting--,rh(t)}var Ss=class{hasPendingMicrotasks=!1;hasPendingMacrotasks=!1;isStable=!0;onUnstable=new ee;onMicrotaskEmpty=new ee;onStable=new ee;onError=new ee;run(n,e,i){return n.apply(e,i)}runGuarded(n,e,i){return n.apply(e,i)}runOutsideAngular(n){return n()}runTask(n,e,i,a){return n.apply(e,i)}};function mD(t){return py(t,`__ignore_ng_zone__`)}function pD(t){return py(t,`__scheduler_tick__`)}function py(t,n){return!Array.isArray(t)||t.length!==1?!1:t[0]?.data?.[n]===!0}var Nn=class{_console=console;handleError(n){this._console.error(`ERROR`,n)}};function hy(t){return fy(t)?t:new Cp(t)}function fy(t){return t instanceof Error||typeof t==`object`&&t!==null&&typeof t.name==`string`&&typeof t.message==`string`}var Cp=class extends Error{constructor(n){super(String(n),{cause:n}),this.name=`ErrorBoundaryWrappedError`}};var Ui=new I(``,{factory:()=>{let t=f(Q),n=f(kt),e;return i=>{t.runOutsideAngular(()=>{n.destroyed&&!e?setTimeout(()=>{throw i}):(e??=n.get(Nn),e.handleError(i))})}}});var gy={provide:no,useValue:()=>{f(Nn,{optional:!0})},multi:!0};var hD=new I(``,{factory:()=>{let t=f(de).defaultView;if(!t)return;let n=f(Ui),e=o=>{n(o.reason),o.preventDefault()},i=o=>{o.error?n(o.error):n(new Error(o.message,{cause:o})),o.preventDefault()},a=()=>{t.addEventListener(`unhandledrejection`,e),t.addEventListener(`error`,i)};typeof Zone<`u`?Zone.root.run(a):a(),f(Te).onDestroy(()=>{t.removeEventListener(`error`,i),t.removeEventListener(`unhandledrejection`,e)})}});function sh(){return rn([Lv(()=>{f(hD)})])}function N(t,n){let[e,i,a]=Wm(t,n?.equal),o=e;o[Ct];return o.set=i,o.update=a,o.asReadonly=ud.bind(o),o}function ud(){let t=this[Ct];if(t.readonlyFn===void 0){let n=()=>this();n[Ct]=t,t.readonlyFn=n}return t.readonlyFn}var $i=new I(``,{factory:()=>fD});var fD=`ng`;var md=new I(``);var Gi=new I(``,{providedIn:`platform`,factory:()=>`unknown`});var Ls=new I(``);var _a=new I(``,{factory:()=>f(de).body?.querySelector(`[ngCspNonce]`)?.getAttribute(`ngCspNonce`)||null});var ur=(()=>{class t{view;node;constructor(e,i){this.view=e,this.node=i}static __NG_ELEMENT_ID__=gD}return t})();function gD(){return new ur(le(),It())}var bi=class{};var Vs=new I(``,{factory:()=>!0});var ch=new I(``);var pd=(()=>{class t{static ɵprov=fe({token:t,providedIn:`root`,factory:()=>new Sp})}return t})();var Sp=class{dirtyEffectCount=0;queues=new Map;add(n){this.enqueue(n),this.schedule(n)}schedule(n){n.dirty&&this.dirtyEffectCount++}remove(n){let e=n.zone,i=this.queues.get(e);i.has(n)&&(i.delete(n),n.dirty&&this.dirtyEffectCount--)}enqueue(n){let e=n.zone;this.queues.has(e)||this.queues.set(e,new Set);let i=this.queues.get(e);i.has(n)||i.add(n)}flush(){for(;this.dirtyEffectCount>0;){let n=!1;for(let[e,i]of this.queues)e===null?n||=this.flushQueue(i):n||=e.run(()=>this.flushQueue(i));n||(this.dirtyEffectCount=0)}}flushQueue(n){let e=!1;for(let i of n)i.dirty&&(this.dirtyEffectCount--,e=!0,i.run());return e}};var Hl=class{[Ct];constructor(n){this[Ct]=n}destroy(){this[Ct].destroy()}};function Ft(t,n){let e=n?.injector??f(re),i=n?.manualCleanup!==!0?e.get(Te):null,a,o=e.get(ur,null,{optional:!0}),r=e.get(bi);return o!==null?(a=vy(o.view,r,t),i instanceof jl&&i._lView===o.view&&(i=null)):a=yD(t,e.get(pd),r),a.injector=e,i!==null&&(a.onDestroyFns=[i.onDestroy(()=>a.destroy())]),new Hl(a)}var by=s(r({},Ym),{cleanupFns:void 0,zone:null,onDestroyFns:null,run(){let t=xs(!1);try{Km(this)}finally{xs(t)}},cleanup(){if(!this.cleanupFns?.length)return;let t=he(null);try{for(;this.cleanupFns.length;)this.cleanupFns.pop()()}finally{this.cleanupFns=[],he(t)}}});var bD=s(r({},by),{consumerMarkedDirty(){this.scheduler.schedule(this),this.notifier.notify(12)},destroy(){if(da(this),this.onDestroyFns!==null)for(let t of this.onDestroyFns)t();this.cleanup(),this.scheduler.remove(this)}});var vD=s(r({},by),{consumerMarkedDirty(){this.view[xe]|=8192,lo(this.view),this.notifier.notify(13)},destroy(){if(da(this),this.onDestroyFns!==null)for(let t of this.onDestroyFns)t();this.cleanup(),this.view[Li]?.delete(this)}});function vy(t,n,e){let i=Object.create(vD);return i.view=t,i.zone=typeof Zone<`u`?Zone.current:null,i.notifier=n,i.userFn=e,i.fn=yy(i,e),t[Li]??=new Set,t[Li].add(i),i.consumerMarkedDirty(i),i}function yD(t,n,e){let i=Object.create(bD);return i.userFn=t,i.fn=yy(i,t),i.scheduler=n,i.notifier=e,i.zone=typeof Zone<`u`?Zone.current:null,i.scheduler.add(i),i.notifier.notify(12),i}function yy(t,n){return()=>{n(e=>(t.cleanupFns??=[]).push(e))}}function sn(t){return typeof t==`function`&&t[Ct]!==void 0}var xa=(()=>{class t{internalPendingTasks=f(po);scheduler=f(bi);errorHandler=f(Ui);add(){let e=this.internalPendingTasks.add();return()=>{this.internalPendingTasks.has(e)&&(this.scheduler.notify(11),this.internalPendingTasks.remove(e))}}run(e){let i=this.add();try{e().catch(this.errorHandler).finally(i)}catch(a){this.errorHandler(a),i()}}static ɵprov=fe({token:t,providedIn:`root`,factory:()=>new t})}return t})();var Cr=Symbol(`InputSignalNode#UNSET`);var af=s(r({},Go),{transformFn:void 0,applyValueToInputSignal(t,n){ua(t,n)}});function Ys(t){return{toString:t}.toString()}var ze=(function(t){return t[t.TemplateCreateStart=0]=`TemplateCreateStart`,t[t.TemplateCreateEnd=1]=`TemplateCreateEnd`,t[t.TemplateUpdateStart=2]=`TemplateUpdateStart`,t[t.TemplateUpdateEnd=3]=`TemplateUpdateEnd`,t[t.LifecycleHookStart=4]=`LifecycleHookStart`,t[t.LifecycleHookEnd=5]=`LifecycleHookEnd`,t[t.OutputStart=6]=`OutputStart`,t[t.OutputEnd=7]=`OutputEnd`,t[t.BootstrapApplicationStart=8]=`BootstrapApplicationStart`,t[t.BootstrapApplicationEnd=9]=`BootstrapApplicationEnd`,t[t.BootstrapComponentStart=10]=`BootstrapComponentStart`,t[t.BootstrapComponentEnd=11]=`BootstrapComponentEnd`,t[t.ChangeDetectionStart=12]=`ChangeDetectionStart`,t[t.ChangeDetectionEnd=13]=`ChangeDetectionEnd`,t[t.ChangeDetectionSyncStart=14]=`ChangeDetectionSyncStart`,t[t.ChangeDetectionSyncEnd=15]=`ChangeDetectionSyncEnd`,t[t.AfterRenderHooksStart=16]=`AfterRenderHooksStart`,t[t.AfterRenderHooksEnd=17]=`AfterRenderHooksEnd`,t[t.ComponentStart=18]=`ComponentStart`,t[t.ComponentEnd=19]=`ComponentEnd`,t[t.DeferBlockStateStart=20]=`DeferBlockStateStart`,t[t.DeferBlockStateEnd=21]=`DeferBlockStateEnd`,t[t.DynamicComponentStart=22]=`DynamicComponentStart`,t[t.DynamicComponentEnd=23]=`DynamicComponentEnd`,t[t.HostBindingsUpdateStart=24]=`HostBindingsUpdateStart`,t[t.HostBindingsUpdateEnd=25]=`HostBindingsUpdateEnd`,t})(ze||{});var xd=class{previousValue;currentValue;firstChange;constructor(n,e,i){this.previousValue=n,this.currentValue=e,this.firstChange=i}isFirstChange(){return this.firstChange}};function s_(t,n,e,i){n!==null?n.applyValueToInputSignal(n,i):t[e]=i}var c_=null;var bt=(()=>{c_=_y;let t=()=>_y;return t.ngInherit=!0,t})();function ND(){return c_}function _y(t){return t.type.prototype.ngOnChanges&&(t.setInput=AD),TD}function TD(){let t=l_(this),n=t?.current;if(n){let e=t.previous;if(e===ha)t.previous=n;else for(let i in n)e[i]=n[i];t.current=null,this.ngOnChanges(n)}}function AD(t,n,e,i,a){let o=this.declaredInputs[i],r=l_(t)||OD(t,{previous:ha,current:null}),s=r.current||(r.current={}),c=r.previous,p=c[o];s[o]=new xd(p&&p.currentValue,e,c===ha),s_(t,n,a,e)}var _h=`__ngSimpleChanges__`;function l_(t){return Object.hasOwn(t,_h)&&t[_h]||null}function OD(t,n){return t[_h]=n}var xy=[];var Ke=function(t,n=null,e){for(let i=0;i<xy.length;i++){let a=xy[i];a(t,n,e)}};function RD(t,n,e){let{ngOnChanges:i,ngOnInit:a,ngDoCheck:o}=n.type.prototype;if(i){let r=ND()(n);(e.preOrderHooks??=[]).push(t,r),(e.preOrderCheckHooks??=[]).push(t,r)}a&&(e.preOrderHooks??=[]).push(0-t,a),o&&((e.preOrderHooks??=[]).push(t,o),(e.preOrderCheckHooks??=[]).push(t,o))}function d_(t,n){for(let e=n.directiveStart,i=n.directiveEnd;e<i;e++){let{ngAfterContentInit:r,ngAfterContentChecked:s,ngAfterViewInit:c,ngAfterViewChecked:p,ngOnDestroy:m}=t.data[e].type.prototype;r&&(t.contentHooks??=[]).push(-e,r),s&&((t.contentHooks??=[]).push(e,s),(t.contentCheckHooks??=[]).push(e,s)),c&&(t.viewHooks??=[]).push(-e,c),p&&((t.viewHooks??=[]).push(e,p),(t.viewCheckHooks??=[]).push(e,p)),m!=null&&(t.destroyHooks??=[]).push(e,m)}}function bd(t,n,e){u_(t,n,3,e)}function vd(t,n,e,i){(t[xe]&3)===e&&u_(t,n,e,i)}function lh(t,n){let e=t[xe];(e&3)===n&&(e&=16383,e+=1,t[xe]=e)}function u_(t,n,e,i){let a=i!==void 0?t[ao]&65535:0,o=i??-1,r=n.length-1,s=0;for(let c=a;c<r;c++)if(typeof n[c+1]==`number`){if(s=n[c],i!=null&&s>=i)break}else n[c]<0&&(t[ao]+=65536),(s<o||o==-1)&&(PD(t,e,n,c),t[ao]=(t[ao]&4294901760)+c+2),c++}function Cy(t,n){Ke(ze.LifecycleHookStart,t,n);let e=he(null);try{n.call(t)}finally{he(e),Ke(ze.LifecycleHookEnd,t,n)}}function PD(t,n,e,i){let a=e[i]<0,o=e[i+1],s=t[a?-e[i]:e[i]];a?t[xe]>>14<t[ao]>>16&&(t[xe]&3)===n&&(t[xe]+=16384,Cy(s,o)):Cy(s,o)}var hr=-1;var go=class{factory;name;injectImpl;resolving=!1;canSeeViewProviders;multi;componentProviders;index;providerFactory;constructor(n,e,i,a){this.factory=n,this.name=a,this.canSeeViewProviders=e,this.injectImpl=i}};function FD(t){return(t.flags&8)!==0}function LD(t){return(t.flags&16)!==0}function VD(t,n,e){let i=0;for(;i<e.length;){let a=e[i];if(typeof a==`number`){if(a!==0)break;i++;let o=e[i++],r=e[i++],s=e[i++];t.setAttribute(n,r,s,o)}else{let o=a,r=e[++i];BD(o)?t.setProperty(n,o,r):t.setAttribute(n,o,r),i++}}return i}function m_(t){return t===3||t===4||t===6}function BD(t){return t.charCodeAt(0)===64}function fr(t,n){if(!(n===null||n.length===0))if(t===null||t.length===0)t=n.slice();else{let e=-1;for(let i=0;i<n.length;i++){let a=n[i];typeof a==`number`?e=a:e===0||(e===-1||e===2?Sy(t,e,a,null,n[++i]):Sy(t,e,a,null,null))}}return t}function Sy(t,n,e,i,a){let o=0,r=t.length;if(n===-1)r=-1;else for(;o<t.length;){let s=t[o++];if(typeof s==`number`){if(s===n){r=-1;break}else if(s>n){r=o-1;break}}}for(;o<t.length;){let s=t[o];if(typeof s==`number`)break;if(s===e){a!==null&&(t[o+1]=a);return}o++,a!==null&&o++}r!==-1&&(t.splice(r,0,n),o=r+1),t.splice(o++,0,e),a!==null&&t.splice(o++,0,a)}function p_(t){return t!==hr}function Cd(t){return t&32767}function jD(t){return t>>16}function Sd(t,n){let e=jD(t),i=n;for(;e>0;)i=i[fa],e--;return i}var xh=!0;function wd(t){let n=xh;return xh=t,n}var h_=255;var f_=5;var HD=0;var xi={};function UD(t,n,e){let i;typeof e==`string`?i=e.charCodeAt(0)||0:Object.hasOwn(e,to)&&(i=e[to]),i??=e[to]=HD++;let a=i&h_,o=1<<a;n.data[t+(a>>f_)]|=o}function Ed(t,n){let e=g_(t,n);if(e!==-1)return e;let i=n[ge];i.firstCreatePass&&(t.injectorIndex=n.length,dh(i.data,t),dh(n,null),dh(i.blueprint,null));let a=of(t,n),o=t.injectorIndex;if(p_(a)){let r=Cd(a),s=Sd(a,n),c=s[ge].data;for(let p=0;p<8;p++)n[o+p]=s[r+p]|c[r+p]}return n[o+8]=a,o}function dh(t,n){t.push(0,0,0,0,0,0,0,0,n)}function g_(t,n){return t.injectorIndex===-1||t.parent&&t.parent.injectorIndex===t.injectorIndex||n[t.injectorIndex+8]===null?-1:t.injectorIndex}function of(t,n){if(t.parent&&t.parent.injectorIndex!==-1)return t.parent.injectorIndex;let e=0,i=null,a=n;for(;a!==null;){if(i=x_(a),i===null)return hr;if(e++,a=a[fa],i.injectorIndex!==-1)return i.injectorIndex|e<<16}return hr}function Ch(t,n,e){UD(t,n,e)}function $D(t,n){if(n===`class`)return t.classes;if(n===`style`)return t.styles;let e=t.attrs;if(e){let i=e.length,a=0;for(;a<i;){let o=e[a];if(m_(o))break;if(o===0)a=a+2;else if(typeof o==`number`)for(a++;a<i&&typeof e[a]==`string`;)a++;else{if(o===n)return e[a+1];a=a+2}}}return null}function b_(t,n,e){if(e&8||t!==void 0)return t;Yl(n,`NodeInjector`)}function v_(t,n,e,i){if(e&8&&i===void 0&&(i=null),(e&3)===0){let a=t[ji],o=on(void 0);try{return a?a.get(n,i,e&8):Mp(n,i,e&8)}finally{on(o)}}return b_(i,n,e)}function y_(t,n,e,i=0,a){if(t!==null){if(n[xe]&2048&&!(i&2)){let r=YD(t,n,e,i,xi);if(r!==xi)return r}let o=__(t,n,e,i,xi);if(o!==xi)return o}return v_(n,e,i,a)}function __(t,n,e,i,a){let o=WD(e);if(typeof o==`function`){if(!eh(n,t,i))return i&1?b_(a,e,i):v_(n,e,i,a);try{let r;if(r=o(i),r==null&&!(i&8))Yl(e);else return r}finally{th()}}else if(typeof o==`number`){let r=null,s=g_(t,n),c=hr,p=i&1?n[Qt][Ut]:null;for((s===-1||i&4)&&(c=s===-1?of(t,n):n[s+8],c===hr||!Ey(i,!1)?s=-1:(r=n[ge],s=Cd(c),n=Sd(c,n)));s!==-1;){let m=n[ge];if(wy(o,s,m.data)){let g=GD(s,n,e,r,i,p);if(g!==xi)return g}c=n[s+8],c!==hr&&Ey(i,n[ge].data[s+8]===p)&&wy(o,s,n)?(r=m,s=Cd(c),n=Sd(c,n)):s=-1}}return a}function GD(t,n,e,i,a,o){let r=n[ge],s=r.data[t+8],m=yd(s,r,e,i==null?_i(s)&&xh:i!=r&&(s.type&3)!==0,a&1&&o===s);return m!==null?Hs(n,r,m,s,a):xi}function yd(t,n,e,i,a){let o=t.providerIndexes,r=n.data,s=o&1048575,c=t.directiveStart,p=t.directiveEnd,m=o>>20,g=i?s:s+m,v=a?s+m:p;for(let b=g;b<v;b++){let _=r[b];if(b<c&&e===_||b>=c&&_.type===e)return b}if(a){let b=r[c];if(b&&Xn(b)&&b.type===e)return c}return null}function Hs(t,n,e,i,a){let o=t[e],r=n.data;if(o instanceof go){let s=o;if(s.resolving)throw Ip(``);let c=wd(s.canSeeViewProviders);s.resolving=!0;r[e].type||r[e];let g=s.injectImpl?on(s.injectImpl):null;eh(t,i,0);try{o=t[e]=s.factory(void 0,a,r,t,i),n.firstCreatePass&&e>=i.directiveStart&&RD(e,r[e],n)}finally{g!==null&&on(g),wd(c),s.resolving=!1,th()}}return o}function WD(t){if(typeof t==`string`)return t.charCodeAt(0)||0;let n=Object.hasOwn(t,to)?t[to]:void 0;return typeof n==`number`?n>=0?n&h_:qD:n}function wy(t,n,e){let i=1<<t;return!!(e[n+(t>>f_)]&i)}function Ey(t,n){return!(t&2)&&!(t&1&&n)}var Wi=class{_tNode;_lView;constructor(n,e){this._tNode=n,this._lView=e}get(n,e,i){return y_(this._tNode,this._lView,n,Za(i),e)}};function qD(){return new Wi(It(),le())}function Nt(t){return Ys(()=>{let n=t.prototype.constructor,e=n[_s]||Sh(n),i=Object.prototype,a=Object.getPrototypeOf(t.prototype).constructor;for(;a&&a!==i;){let o=a[_s]||Sh(a);if(o&&o!==e)return o;a=Object.getPrototypeOf(a)}return o=>new o})}function Sh(t){return wp(t)?()=>{let n=Sh(Pt(t));return n&&n()}:ma(t)}function YD(t,n,e,i,a){let o=t,r=n;for(;o!==null&&r!==null&&r[xe]&2048&&!lr(r);){let s=__(o,r,e,i|2,xi);if(s!==xi)return s;i&=-5;let c=o.parent;if(!c){let p=r[Fp];if(p){let m=p.get(e,xi,i);if(m!==xi)return m}c=x_(r),r=r[fa]}o=c}return a}function x_(t){let n=t[ge],e=n.type;return e===2?n.declTNode:e===1?t[Ut]:null}function rf(t){return $D(It(),t)}function se(t){return{token:t.token,providedIn:t.autoProvided===!1?null:`root`,factory:t.factory,value:void 0}}function KD(){return Sr(It(),le())}function Sr(t,n){return new W(xn(t,n))}var W=(()=>{class t{nativeElement;constructor(e){this.nativeElement=e}static __NG_ELEMENT_ID__=KD}return t})();function C_(t){return t instanceof W?t.nativeElement:t}function QD(){return this._results[Symbol.iterator]()}var Sn=class{_emitDistinctChangesOnly;dirty=!0;_onDirty=void 0;_results=[];_changesDetected=!1;_changes=void 0;length=0;first=void 0;last=void 0;get changes(){return this._changes??=new L}constructor(n=!1){this._emitDistinctChangesOnly=n}get(n){return this._results[n]}map(n){return this._results.map(n)}filter(n){return this._results.filter(n)}find(n){return this._results.find(n)}reduce(n,e){return this._results.reduce(n,e)}forEach(n){this._results.forEach(n)}some(n){return this._results.some(n)}toArray(){return this._results.slice()}toString(){return this._results.toString()}reset(n,e){this.dirty=!1;let i=Rv(n);(this._changesDetected=!Ov(this._results,i,e))&&(this._results=i,this.length=i.length,this.last=i[this.length-1],this.first=i[0])}notifyOnChanges(){this._changes!==void 0&&(this._changesDetected||!this._emitDistinctChangesOnly)&&this._changes.next(this)}onDirty(n){this._onDirty=n}setDirty(){this.dirty=!0,this._onDirty?.()}destroy(){this._changes!==void 0&&(this._changes.complete(),this._changes.unsubscribe())}[Symbol.iterator]=QD};function S_(t){return(t.flags&128)===128}var sf=(function(t){return t[t.OnPush=0]=`OnPush`,t[t.Eager=1]=`Eager`,t[t.Default=1]=`Default`,t})(sf||{});var w_=new Map;var XD=0;function ZD(){return XD++}function JD(t){w_.set(t[Ns],t)}function wh(t){w_.delete(t[Ns])}var Dy=`__ngContext__`;function gr(t,n){zi(n)?(t[Dy]=n[Ns],JD(n)):t[Dy]=n}function E_(t){return k_(t[rr])}function D_(t){return k_(t[Tn])}function k_(t){for(;t!==null&&!An(t);)t=t[Tn];return t}var Eh;function cf(t){Eh=t}function I_(){if(Eh!==void 0)return Eh;if(typeof document<`u`)return document;throw new G(210,!1)}var M_=`r`;var N_=`di`;var T_=!1;var A_=new I(``,{factory:()=>T_});var ky=new WeakMap;function ek(t,n){if(t==null||typeof t!=`object`)return;let e=ky.get(t);e||(e=new WeakSet,ky.set(t,e)),e.add(n)}function Ld(t){return(t.flags&32)===32}var ik=()=>null;function O_(t,n,e=!1){return ik(t,n,e)}function R_(t,n){let e=t.contentQueries;if(e!==null){let i=he(null);try{for(let a=0;a<e.length;a+=2){let o=e[a],r=e[a+1];if(r!==-1){let s=t.data[r];Ps(o),s.contentQueries(2,n[r],r)}}}finally{he(i)}}}function Dh(t,n,e){Ps(0);let i=he(null);try{n(t,e)}finally{he(i)}}function lf(t,n,e){if(Vp(n)){let i=he(null);try{let a=n.directiveStart,o=n.directiveEnd;for(let r=a;r<o;r++){let s=t.data[r];if(s.contentQueries){let c=e[r];s.contentQueries(1,c,r)}}}finally{he(i)}}}var ei=(function(t){return t[t.Emulated=0]=`Emulated`,t[t.None=2]=`None`,t[t.ShadowDom=3]=`ShadowDom`,t[t.ExperimentalIsolatedShadowDom=4]=`ExperimentalIsolatedShadowDom`,t})(ei||{});var hd;function ak(){if(hd===void 0&&(hd=null,vi.trustedTypes))try{hd=vi.trustedTypes.createPolicy(`angular`,{createHTML:t=>t,createScript:t=>t,createScriptURL:t=>t})}catch{}return hd}function Vd(t){return ak()?.createHTML(t)||t}var qi=class{changingThisBreaksApplicationSecurity;constructor(n){this.changingThisBreaksApplicationSecurity=n}toString(){return`SafeValue must use [property]=binding: ${this.changingThisBreaksApplicationSecurity} (see ${Ul})`}};var kh=class extends qi{getTypeName(){return`HTML`}};var Ih=class extends qi{getTypeName(){return`Style`}};var Mh=class extends qi{getTypeName(){return`Script`}};var Nh=class extends qi{getTypeName(){return`URL`}};var Th=class extends qi{getTypeName(){return`ResourceURL`}};function ti(t){return t instanceof qi?t.changingThisBreaksApplicationSecurity:t}function Sa(t,n){let e=P_(t);if(e!=null&&e!==n){if(e===`ResourceURL`&&n===`URL`)return!0;throw new Error(`Required a safe ${n}, got a ${e} (see ${Ul})`)}return e===n}function P_(t){return t instanceof qi&&t.getTypeName()||null}function df(t){return new kh(t)}function uf(t){return new Ih(t)}function mf(t){return new Mh(t)}function pf(t){return new Nh(t)}function hf(t){return new Th(t)}function ok(t){let n=new Oh(t);return rk()?new Ah(n):n}var Ah=class{inertDocumentHelper;constructor(n){this.inertDocumentHelper=n}getInertBodyElement(n){n=`<body><remove></remove>`+n;try{let e=new window.DOMParser().parseFromString(Vd(n),`text/html`).body;return e===null?this.inertDocumentHelper.getInertBodyElement(n):(e.firstChild?.remove(),e)}catch{return null}}};var Oh=class{defaultDoc;inertDocument;constructor(n){this.defaultDoc=n,this.inertDocument=this.defaultDoc.implementation.createHTMLDocument(`sanitization-inert`)}getInertBodyElement(n){let e=this.inertDocument.createElement(`template`);return e.innerHTML=Vd(n),e}};function rk(){try{return!!new window.DOMParser().parseFromString(Vd(``),`text/html`)}catch{return!1}}var sk=/^(?!javascript:)(?:[a-z0-9+.-]+:|[^&:\/?#]*(?:[\/?#]|$))/i;function Ks(t){return t=String(t),t.match(sk)?t:`unsafe:`+t}function Yi(t){let n=Object.create(null);for(let e of t.split(`,`))n[e]=!0;return n}function Qs(...t){let n=Object.create(null);for(let e of t)for(let i in e)Object.hasOwn(e,i)&&(n[i]=!0);return n}var F_=Yi(`area,br,col,hr,img,wbr`);var L_=Yi(`colgroup,dd,dt,li,p,tbody,td,tfoot,th,thead,tr`);var V_=Yi(`rp,rt`);var ck=Qs(V_,L_);var Iy=Qs(F_,Qs(L_,Yi(`address,article,aside,blockquote,caption,center,del,details,dialog,dir,div,dl,figure,figcaption,footer,h1,h2,h3,h4,h5,h6,header,hgroup,hr,ins,main,map,menu,nav,ol,pre,section,summary,table,ul`)),Qs(V_,Yi(`a,abbr,acronym,audio,b,bdi,bdo,big,br,cite,code,del,dfn,em,font,i,img,ins,kbd,label,map,mark,picture,q,ruby,rp,rt,s,samp,small,source,span,strike,strong,sub,sup,time,track,tt,u,var,video`)),ck);var B_=Yi(`background,cite,href,itemtype,longdesc,poster,src,xlink:href`);var pk=Qs(B_,Yi(`abbr,accesskey,align,alt,autoplay,axis,bgcolor,border,cellpadding,cellspacing,class,clear,color,cols,colspan,compact,controls,coords,datetime,default,dir,download,face,headers,height,hidden,hreflang,hspace,ismap,itemscope,itemprop,kind,label,lang,language,loop,media,muted,nohref,nowrap,open,preload,rel,rev,role,rows,rowspan,rules,scope,scrolling,shape,size,sizes,span,srclang,srcset,start,summary,tabindex,target,title,translate,type,usemap,valign,value,vspace,width`),Yi(`aria-activedescendant,aria-atomic,aria-autocomplete,aria-busy,aria-checked,aria-colcount,aria-colindex,aria-colspan,aria-controls,aria-current,aria-describedby,aria-details,aria-disabled,aria-dropeffect,aria-errormessage,aria-expanded,aria-flowto,aria-grabbed,aria-haspopup,aria-hidden,aria-invalid,aria-keyshortcuts,aria-label,aria-labelledby,aria-level,aria-live,aria-modal,aria-multiline,aria-multiselectable,aria-orientation,aria-owns,aria-placeholder,aria-posinset,aria-pressed,aria-readonly,aria-relevant,aria-required,aria-roledescription,aria-rowcount,aria-rowindex,aria-rowspan,aria-selected,aria-setsize,aria-sort,aria-valuemax,aria-valuemin,aria-valuenow,aria-valuetext`));var hk=Yi(`script,style,template`);var Rh=class{sanitizedSomething=!1;buf=[];sanitizeChildren(n){let e=n.firstChild,i=!0,a=[];for(;e;){if(e.nodeType===Node.ELEMENT_NODE?i=this.startElement(e):e.nodeType===Node.TEXT_NODE?this.chars(e.nodeValue):this.sanitizedSomething=!0,i&&e.firstChild){a.push(e),e=bk(e);continue}for(;e;){e.nodeType===Node.ELEMENT_NODE&&this.endElement(e);let o=gk(e);if(o){e=o;break}e=a.pop()}}return this.buf.join(``)}startElement(n){let e=My(n).toLowerCase();if(!Object.hasOwn(Iy,e))return this.sanitizedSomething=!0,!Object.hasOwn(hk,e);this.buf.push(`<`),this.buf.push(e);let i=n.attributes;for(let a=0;a<i.length;a++){let o=i.item(a),r=o.name,s=r.toLowerCase();if(!Object.hasOwn(pk,s)){this.sanitizedSomething=!0;continue}let c=o.value;B_[s]&&(c=Ks(c)),this.buf.push(` `,r,`="`,Ny(c),`"`)}return this.buf.push(`>`),!0}endElement(n){let e=My(n).toLowerCase();Object.hasOwn(Iy,e)&&!Object.hasOwn(F_,e)&&(this.buf.push(`</`),this.buf.push(e),this.buf.push(`>`))}chars(n){this.buf.push(Ny(n))}};function fk(t,n){return(t.compareDocumentPosition(n)&Node.DOCUMENT_POSITION_CONTAINED_BY)!==Node.DOCUMENT_POSITION_CONTAINED_BY}function gk(t){let n=t.nextSibling;if(n&&t!==n.previousSibling)throw j_(n);return n}function bk(t){let n=t.firstChild;if(n&&fk(t,n))throw j_(n);return n}function My(t){let n=t.nodeName;return typeof n==`string`?n:`FORM`}function j_(t){return new Error(`Failed to sanitize html because the element is clobbered: ${t.outerHTML}`)}var vk=/[\uD800-\uDBFF][\uDC00-\uDFFF]/g;var yk=/([^\#-~ |!])/g;function Ny(t){return t.replace(/&/g,`&amp;`).replace(vk,function(n){let e=n.charCodeAt(0),i=n.charCodeAt(1);return`&#`+((e-55296)*1024+(i-56320)+65536)+`;`}).replace(yk,function(n){return`&#`+n.charCodeAt(0)+`;`}).replace(/</g,`&lt;`).replace(/>/g,`&gt;`)}var fd;function ff(t,n){let e=null;try{fd=fd||ok(t);let i=n?String(n):``;e=fd.getInertBodyElement(i);let a=5,o=i;do{if(a===0)throw new Error(`Failed to sanitize html because the input is unstable`);a--,i=o,o=e.innerHTML,e=fd.getInertBodyElement(i)}while(i!==o);return Vd(new Rh().sanitizeChildren(Ty(e)||e))}finally{if(e){let i=Ty(e)||e;for(;i.firstChild;)i.firstChild.remove()}}}function Ty(t){return`content`in t&&_k(t)?t.content:null}function _k(t){return t.nodeType===Node.ELEMENT_NODE&&t.nodeName===`TEMPLATE`}var xk=/^>|^->|<!--|-->|--!>|<!-$/g;var Ck=/(<|>)/g;var Sk=`​$1​`;function wk(t){return t.replace(xk,n=>n.replace(Ck,Sk))}function Ek(t,n){return t.createText(n)}function Dk(t,n,e){t.setValue(n,e)}function kk(t,n){return t.createComment(wk(n))}function z_(t,n,e){return t.createElement(n,e)}function ho(t,n,e,i,a){t.insertBefore(n,e,i,a)}function H_(t,n,e){t.appendChild(n,e)}function Ay(t,n,e,i,a){i!==null?ho(t,n,e,i,a):H_(t,n,e)}function U_(t,n,e,i){t.removeChild(null,n,e,i)}function Ik(t,n,e){t.setAttribute(n,`style`,e)}function Mk(t,n,e){e===``?t.removeAttribute(n,`class`):t.setAttribute(n,`class`,e)}function $_(t,n,e){let{mergedAttrs:i,classes:a,styles:o}=e;i!==null&&VD(t,n,i),a!==null&&Mk(t,n,a),o!==null&&Ik(t,n,o)}function pn(t){let n=Nk();return n?n.sanitize(_n.URL,t)||``:Sa(t,`URL`)?ti(t):Ks($e(t))}function Nk(){let t=le();return t&&t[Qn].sanitizer}function Tk(t){return t instanceof Function?t():t}function Ak(t,n,e){let i=t.length;for(;;){let a=t.indexOf(n,e);if(a===-1)return a;if(a===0||t.charCodeAt(a-1)<=32){let o=n.length;if(a+o===i||t.charCodeAt(a+o)<=32)return a}e=a+1}}var G_=`ng-template`;function Ok(t,n,e,i){let a=0;if(i){for(;a<n.length&&typeof n[a]==`string`;a+=2)if(n[a]===`class`&&Ak(n[a+1].toLowerCase(),e,0)!==-1)return!0}else if(gf(t))return!1;if(a=n.indexOf(1,a),a>-1){let o;for(;++a<n.length&&typeof(o=n[a])==`string`;)if(o.toLowerCase()===e)return!0}return!1}function gf(t){return t.type===4&&t.value!==G_}function Rk(t,n,e){return n===(t.type===4&&!e?G_:t.value)}function Pk(t,n,e){let i=4,a=t.attrs,o=a!==null?Vk(a):0,r=!1;for(let s=0;s<n.length;s++){let c=n[s];if(typeof c==`number`){if(!r&&!Zn(i)&&!Zn(c))return!1;if(r&&Zn(c))continue;r=!1,i=c|i&1;continue}if(!r)if(i&4){if(i=2|i&1,c!==``&&!Rk(t,c,e)||c===``&&n.length===1){if(Zn(i))return!1;r=!0}}else if(i&8){if(a===null||!Ok(t,a,c,e)){if(Zn(i))return!1;r=!0}}else{let p=n[++s],m=Fk(c,a,gf(t),e);if(m===-1){if(Zn(i))return!1;r=!0;continue}if(p!==``){let g;if(m>o?g=``:g=a[m+1].toLowerCase(),i&2&&p!==g){if(Zn(i))return!1;r=!0}}}}return Zn(i)||r}function Zn(t){return(t&1)===0}function Fk(t,n,e,i){if(n===null)return-1;let a=0;if(i||!e){let o=!1;for(;a<n.length;){let r=n[a];if(r===t)return a;if(r===3||r===6)o=!0;else if(r===1||r===2){let s=n[++a];for(;typeof s==`string`;)s=n[++a];continue}else{if(r===4)break;if(r===0){a+=4;continue}}a+=o?1:2}return-1}else return Bk(n,t)}function W_(t,n,e=!1){for(let i=0;i<n.length;i++)if(Pk(t,n[i],e))return!0;return!1}function Lk(t){let n=t.attrs;if(n!=null){let e=n.indexOf(5);if((e&1)===0)return n[e+1]}return null}function Vk(t){for(let n=0;n<t.length;n++){let e=t[n];if(m_(e))return n}return t.length}function Bk(t,n){let e=t.indexOf(4);if(e>-1)for(e++;e<t.length;){let i=t[e];if(typeof i==`number`)return-1;if(i===n)return e;e++}return-1}function jk(t,n){e:for(let e=0;e<n.length;e++){let i=n[e];if(t.length===i.length){for(let a=0;a<t.length;a++)if(t[a]!==i[a])continue e;return!0}}return!1}function Oy(t,n){return t?`:not(`+n.trim()+`)`:n}function zk(t){let n=t[0],e=1,i=2,a=``,o=!1;for(;e<t.length;){let r=t[e];if(typeof r==`string`)if(i&2){let s=t[++e];a+=`[`+r+(s.length>0?`="`+s+`"`:``)+`]`}else i&8?a+=`.`+r:i&4&&(a+=` `+r);else a!==``&&!Zn(r)&&(n+=Oy(o,a),a=``),i=r,o=o||!Zn(i);e++}return a!==``&&(n+=Oy(o,a)),n}function Hk(t){return t.map(zk).join(`,`)}function Uk(t){let n=[],e=[],i=1,a=2;for(;i<t.length;){let o=t[i];if(typeof o==`string`)a===2?o!==``&&n.push(o,t[++i]):a===8&&e.push(o);else{if(!Zn(a))break;a=o}i++}return e.length&&n.push(1,...e),n}var mt={};var Ci=(function(t){return t[t.Important=1]=`Important`,t[t.DashCase=2]=`DashCase`,t})(Ci||{});var $k;function bf(t,n){return $k(t,n)}var vf=new Set;typeof document<`u`&&document?.documentElement?.getAnimations;var Ph=new WeakMap;function q_(t){return t?t[fa]??t:null}var mr=new WeakSet;function Gk(t,n,e){let i=Ph.get(t);if(!i||i.length===0)return;let a=n.parentNode,o=n.previousSibling,r=q_(e);for(let s=i.length-1;s>=0;s--){let{el:c,declarationView:p}=i[s],m=c.parentNode;c===n?(i.splice(s,1),mr.add(c),c.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}}))):o&&c===o?(i.splice(s,1),c.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}})),c.parentNode?.removeChild(c)):m&&a&&m!==a&&(r===null||p===null||r===p)&&(i.splice(s,1),c.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}})),c.parentNode?.removeChild(c))}}function Wk(t,n,e){let i=q_(e),a=Ph.get(t);a?a.some(o=>o.el===n)||a.push({el:n,declarationView:i}):Ph.set(t,[{el:n,declarationView:i}])}var Bd=(function(t){return t[t.CHANGE_DETECTION=0]=`CHANGE_DETECTION`,t[t.AFTER_NEXT_RENDER=1]=`AFTER_NEXT_RENDER`,t})(Bd||{});var ii=new I(``);var Ry=new Set;function Si(t){Ry.has(t)||(Ry.add(t),performance?.mark?.(`mark_feature_usage`,{detail:{feature:t}}))}var jd=(()=>{class t{impl=null;execute(){this.impl?.execute()}static ɵprov=fe({token:t,providedIn:`root`,factory:()=>new t})}return t})();var yf=[0,1,2,3];var _f=(()=>{class t{ngZone=f(Q);scheduler=f(bi);errorHandler=f(Nn,{optional:!0});sequences=new Set;deferredRegistrations=new Set;executing=!1;constructor(){f(ii,{optional:!0})}execute(){let e=this.sequences.size>0;e&&Ke(ze.AfterRenderHooksStart),this.executing=!0;for(let i of yf)for(let a of this.sequences)if(!(a.erroredOrDestroyed||!a.hooks[i]))try{a.pipelinedValue=this.ngZone.runOutsideAngular(()=>this.maybeTrace(()=>{let o=a.hooks[i];return o(a.pipelinedValue)},a.snapshot))}catch(o){a.erroredOrDestroyed=!0,this.errorHandler?.handleError(o)}this.executing=!1;for(let i of this.sequences)i.afterRun(),i.once&&(this.sequences.delete(i),i.destroy());for(let i of this.deferredRegistrations)this.sequences.add(i);this.deferredRegistrations.size>0&&this.scheduler.notify(7),this.deferredRegistrations.clear(),e&&Ke(ze.AfterRenderHooksEnd)}register(e){let{view:i}=e;i!==void 0?((i[oo]??=[]).push(e),lo(i),i[xe]|=8192):this.executing?this.deferredRegistrations.add(e):this.addSequence(e)}addSequence(e){this.sequences.add(e),this.scheduler.notify(7)}unregister(e){this.executing&&this.sequences.has(e)?(e.erroredOrDestroyed=!0,e.pipelinedValue=void 0,e.once=!0):(this.sequences.delete(e),this.deferredRegistrations.delete(e))}maybeTrace(e,i){return i?i.run(Bd.AFTER_NEXT_RENDER,e):e()}static ɵprov=fe({token:t,providedIn:`root`,factory:()=>new t})}return t})();var Us=class{impl;hooks;view;once;snapshot;erroredOrDestroyed=!1;pipelinedValue=void 0;unregisterOnDestroy;constructor(n,e,i,a,o,r=null){this.impl=n,this.hooks=e,this.view=i,this.once=a,this.snapshot=r,this.unregisterOnDestroy=o?.onDestroy(()=>this.destroy())}afterRun(){this.erroredOrDestroyed=!1,this.pipelinedValue=void 0,this.snapshot?.dispose(),this.snapshot=null}destroy(){this.impl.unregister(this),this.unregisterOnDestroy?.();let n=this.view?.[oo];n&&(this.view[oo]=n.filter(e=>e!==this))}};function Tt(t,n){let e=n?.injector??f(re);return Si(`NgAfterNextRender`),Yk(t,e,n,!0)}function qk(t){return t instanceof Function?[void 0,void 0,t,void 0]:[t.earlyRead,t.write,t.mixedReadWrite,t.read]}function Yk(t,n,e,i){let a=n.get(jd);a.impl??=n.get(_f);let o=n.get(ii,null,{optional:!0}),r=e?.manualCleanup!==!0?n.get(Te):null,s=n.get(ur,null,{optional:!0}),c=new Us(a.impl,qk(t),s?.view,i,r,o?.snapshot(null));return a.impl.register(c),c}var Fh=null;var Y_=null;function Py(t,n,e,i){Fh?.maybeQueueEnterAnimation(t,n,e,i)}function Fy(t,n,e,i){Fh===null?i(!1):Fh.runLeaveAnimationsWithCallback(t,n,e,i)}function Kk(t){Y_?.initViewDetachAnimations(t)}function Qk(t){Y_?.clearViewDetachAnimations(t)}function pr(t,n,e,i,a,o,r,s){if(a!=null){let c,p=!1;An(a)?c=a:zi(a)&&(p=!0,a=a[Kn]);let m=$t(a);t===0&&i!==null?(Py(s,i,o,e),r==null?H_(n,i,m):ho(n,i,m,r||null,!0)):t===1&&i!==null?(ho(n,i,m,r||null,!0),Gk(o,m,s),mr.has(m)||Py(s,i,o,e)):t===2?(s?.[Uv]?.leave?.has(o.index)&&Wk(o,m,s),mr.delete(m),Fy(s,o,e,g=>{if(mr.has(m)){mr.delete(m);return}U_(n,m,p,g)})):t===3&&(mr.delete(m),Fy(s,o,e,()=>{n.destroyNode(m)})),c!=null&&sI(n,t,e,c,o,i,r)}}function Xk(t,n){K_(t,n),n[Kn]=null,n[Ut]=null}function Zk(t,n,e,i,a,o){i[Kn]=a,i[Ut]=n,Hd(t,i,e,1,a,o)}function K_(t,n){n[Qn].changeDetectionScheduler?.notify(9),Hd(t,n,n[et],2,null,null)}function Jk(t){let n=t[rr];if(!n)return uh(t[ge],t);for(;n;){let e=null;if(zi(n))e=n[rr];else{let i=n[_t];i&&(e=i)}if(!e){for(;n&&!n[Tn]&&n!==t;)zi(n)&&uh(n[ge],n),n=n[St];n===null&&(n=t),zi(n)&&uh(n[ge],n),e=n&&n[Tn]}n=e}}function xf(t,n){let e=t[so],i=e.indexOf(n);e.splice(i,1)}function zd(t,n){if(co(n))return;let e=n[et];e.destroyNode&&Hd(t,n,e,3,null,null),Jk(n)}function uh(t,n){if(co(n))return;let e=he(null);try{n[xe]&=-129,n[xe]|=256,n[yn]&&da(n[yn]),tI(t,n),eI(t,n),n[ge].type===1&&n[et].destroy();let i=n[ga];if(i!==null&&An(n[St])){i!==n[St]&&xf(i,n);let a=n[yi];a!==null&&a.detachView(t)}wh(n)}finally{he(e)}}function eI(t,n){let e=t.cleanup,i=n[or];if(e!==null)for(let r=0;r<e.length-1;r+=2)if(typeof e[r]==`string`){let s=e[r+3];s>=0?i[s]():i[-s].unsubscribe(),r+=2}else{let s=i[e[r+1]];e[r].call(s)}i!==null&&(n[or]=null);let a=n[Fi];if(a!==null){n[Fi]=null;for(let r=0;r<a.length;r++){let s=a[r];s()}}let o=n[Li];if(o!==null){n[Li]=null;for(let r of o)r.destroy()}}function tI(t,n){let e;if(t!=null&&(e=t.destroyHooks)!=null)for(let i=0;i<e.length;i+=2){let a=n[e[i]];if(!(a instanceof go)){let o=e[i+1];if(Array.isArray(o))for(let r=0;r<o.length;r+=2){let s=a[o[r]],c=o[r+1];Ke(ze.LifecycleHookStart,s,c);try{c.call(s)}finally{Ke(ze.LifecycleHookEnd,s,c)}}else{Ke(ze.LifecycleHookStart,a,o);try{o.call(a)}finally{Ke(ze.LifecycleHookEnd,a,o)}}}}}function Q_(t,n,e){if(n===null)throw new G(510,!1);return nI(t,n.parent,e)}function nI(t,n,e){let i=n;for(;i!==null&&i.type&168;)n=i,i=n.parent;if(i===null)return e[Kn];if(_i(i)){let{encapsulation:a}=t.data[i.directiveStart+i.componentOffset];if(a===ei.None||a===ei.Emulated)return null}return xn(i,e)}function X_(t,n,e){return aI(t,n,e)}function iI(t,n,e){return t.type&40?xn(t,e):null}var aI=iI;function Cf(t,n,e,i){let a=Q_(t,i,n),o=n[et],s=X_(i.parent||n[Ut],i,n);if(a!=null)if(Array.isArray(e))for(let c=0;c<e.length;c++)Ay(o,a,e[c],s,!1);else Ay(o,a,e,s,!1)}function js(t,n){if(n!==null){let e=n.type;if(e&3)return xn(n,t);if(e&4)return Lh(-1,t[n.index]);if(e&8){let i=n.child;if(i!==null)return js(t,i);{let a=t[n.index];return An(a)?Lh(-1,a):$t(a)}}else{if(e&128)return js(t,n.next);if(e&32)return bf(n,t)()||$t(t[n.index]);{let i=Z_(t,n);if(i!==null){if(Array.isArray(i))return i[0];return js(Vi(t[Qt]),i)}else return js(t,n.next)}}}return null}function Z_(t,n){if(n!==null){let i=t[Qt][Ut],a=n.projection;return i.projection[a]}return null}function Lh(t,n){let e=_t+t+1;if(e<n.length){let i=n[e],a=i[ge].firstChild;if(a!==null)return js(i,a)}return n[ro]}function Sf(t,n,e,i,a,o,r){for(;e!=null;){let s=i[ji];if(e.type===128){e=e.next;continue}let c=i[e.index],p=e.type;if(r&&n===0&&(c&&gr($t(c),i),e.flags|=2),!Ld(e))if(p&8)Sf(t,n,e.child,i,a,o,!1),pr(n,t,s,a,c,e,o,i);else if(p&32){let m=bf(e,i),g;for(;g=m();)pr(n,t,s,a,g,e,o,i);pr(n,t,s,a,c,e,o,i)}else p&16?J_(t,n,i,e,a,o):pr(n,t,s,a,c,e,o,i);e=r?e.projectionNext:e.next}}function Hd(t,n,e,i,a,o){t.type===3?oI(e,i,n,a,o):Sf(e,i,t.firstChild,n,a,o,!1)}function oI(t,n,e,i,a){let r=e[ge].firstChild,s=r.next,c=$t(e[r.index]),p=$t(e[s.index]),m=s.index+1,g=e[m];if(n===1||n===0)i!==null&&(g&&g.hasChildNodes()?ho(t,i,g,a,!0):(ho(t,i,c,a,!0),ho(t,i,p,a,!0)));else if(n===2){if(g||(g=document.createDocumentFragment(),e[m]=g),c&&c.parentNode===g)return;let v=c;for(;v!==null;){let b=v.nextSibling;if(g.appendChild(v),v===p)break;v=b}}}function rI(t,n,e){let i=n[et];J_(i,0,n,e,Q_(t,e,n),X_(e.parent||n[Ut],e,n))}function J_(t,n,e,i,a,o){let r=e[Qt],c=r[Ut].projection[i.projection];if(Array.isArray(c))for(let p=0;p<c.length;p++){let m=c[p];pr(n,t,e[ji],a,m,i,o,e)}else{let p=c,m=r[St];S_(i)&&(p.flags|=128),Sf(t,n,p,m,a,o,!0)}}function sI(t,n,e,i,a,o,r){let s=i[ro];if(s!==$t(i)&&pr(n,t,e,o,s,a,r),(i[xe]&4)===0)for(let p=_t;p<i.length;p++){let m=i[p];Hd(m[ge],m,t,n,o,s)}}function cI(t,n,e,i,a){if(n)a?t.addClass(e,i):t.removeClass(e,i);else{let o=i.indexOf(`-`)===-1?void 0:Ci.DashCase;a==null?t.removeStyle(e,i,o):(typeof a==`string`&&a.endsWith(`!important`)&&(a=a.slice(0,-10),o|=Ci.Important),t.setStyle(e,i,a,o))}}function wf(t,n,e,i,a,o,r,s,c,p,m){let g=at+i,v=g+a,b=lI(g,v),_=typeof p==`function`?p():p;return b[ge]={type:t,blueprint:b,template:e,queries:null,viewQuery:s,declTNode:n,data:b.slice().fill(null,g),bindingStartIndex:g,expandoStartIndex:v,hostBindingOpCodes:null,firstCreatePass:!0,firstUpdatePass:!0,staticViewQueries:!1,staticContentQueries:!1,preOrderHooks:null,preOrderCheckHooks:null,contentHooks:null,contentCheckHooks:null,viewHooks:null,viewCheckHooks:null,destroyHooks:null,cleanup:null,contentQueries:null,components:null,directiveRegistry:typeof o==`function`?o():o,pipeRegistry:typeof r==`function`?r():r,firstChild:null,schemas:c,consts:_,incompleteFirstPass:!1,ssrId:m}}function lI(t,n){let e=[];for(let i=0;i<n;i++)e.push(i<t?null:mt);return e}function dI(t){let n=t.tView;return n===null||n.incompleteFirstPass?t.tView=wf(1,null,t.template,t.decls,t.vars,t.directiveDefs,t.pipeDefs,t.viewQuery,t.schemas,t.consts,t.id):n}function Ef(t,n,e,i,a,o,r,s,c,p,m){let g=n.blueprint.slice();return g[Kn]=a,g[xe]=i|1228,(p!==null||t&&t[xe]&2048)&&(g[xe]|=2048),zp(g),g[St]=g[fa]=t,g[yt]=e,g[Qn]=r||t&&t[Qn],g[et]=s||t&&t[et],g[ji]=c||t&&t[ji]||null,g[Ut]=o,g[Ns]=ZD(),g[io]=m,g[Fp]=p,g[Qt]=n.type==2?t[Qt]:g,g}function uI(t,n,e){let i=xn(n,t),a=dI(e),o=t[Qn].rendererFactory,r=Df(t,Ef(t,a,null,e0(e),i,n,null,o.createRenderer(i,e),null,null,null));return t[n.index]=r}function e0(t){let n=16;return t.signals?n=4096:t.onPush&&(n=64),n}function t0(t,n,e,i){if(e===0)return-1;let a=n.length;for(let o=0;o<e;o++)n.push(i),t.blueprint.push(i),t.data.push(null);return a}function Df(t,n){return t[rr]?t[Pp][Tn]=n:t[rr]=n,t[Pp]=n,n}function h(t=1){n0(Xe(),le(),Xt()+t,!1)}function n0(t,n,e,i){if(!i)if((n[xe]&3)===3){let o=t.preOrderCheckHooks;o!==null&&bd(n,o,e)}else{let o=t.preOrderHooks;o!==null&&vd(n,o,0,e)}va(e)}var Xs=(function(t){return t[t.None=0]=`None`,t[t.SignalBased=1]=`SignalBased`,t[t.HasDecoratorInputTransform=2]=`HasDecoratorInputTransform`,t})(Xs||{});function bo(t,n,e,i){let a=he(null);try{let[o,r,s]=t.inputs[e],c=null;(r&Xs.SignalBased)!==0&&(c=n[o][Ct]),c!==null&&c.transformFn!==void 0?i=c.transformFn(i):s!==null&&(i=s.call(n,i)),t.setInput!==null?t.setInput(n,c,i,e,o):s_(n,c,o,i)}finally{he(a)}}function i0(t,n,e,i,a){let o=Xt(),r=i&2;try{va(-1),r&&n.length>at&&n0(t,n,at,!1);Ke(r?ze.TemplateUpdateStart:ze.TemplateCreateStart,a,e),e(i,a)}finally{va(o);Ke(r?ze.TemplateUpdateEnd:ze.TemplateCreateEnd,a,e)}}function Ud(t,n,e){bI(t,n,e),(e.flags&64)===64&&vI(t,n,e)}function Zs(t,n,e=xn){let i=n.localNames;if(i!==null){let a=n.index+1;for(let o=0;o<i.length;o+=2){let r=i[o+1],s=r===-1?e(n,t):t[r];t[a++]=s}}}function mI(t,n,e,i){let o=i.get(A_,T_)||e===ei.ShadowDom||e===ei.ExperimentalIsolatedShadowDom;return t.selectRootElement(n,o)}function fI(t){return t===`class`?`className`:t===`for`?`htmlFor`:t===`formaction`?`formAction`:t===`innerHtml`?`innerHTML`:t===`readonly`?`readOnly`:t===`tabindex`?`tabIndex`:t}function gI(t,n,e,i,a,o){let r=n[ge];if($d(t,r,n,e,i)){_i(t)&&o0(n,t.index);return}t.type&3&&(e=fI(e)),a0(t,n,e,i,a,o)}function a0(t,n,e,i,a,o){if(t.type&3){let r=xn(t,n);i=o!=null?o(i,t.value||``,e):i,a.setProperty(r,e,i)}else t.type&12}function o0(t,n){let e=On(n,t);e[xe]&16||(e[xe]|=64)}function bI(t,n,e){let i=e.directiveStart,a=e.directiveEnd;_i(e)&&uI(n,e,t.data[i+e.componentOffset]),t.firstCreatePass||Ed(e,n);let o=e.initialInputs;for(let r=i;r<a;r++){let s=t.data[r],c=Hs(n,t,r,e);if(gr(c,n),o!==null&&xI(n,r-i,c,s,e,o),Xn(s)){let p=On(e.index,n);p[yt]=Hs(n,t,r,e)}}}function vI(t,n,e){let i=e.directiveStart,a=e.directiveEnd,o=e.index,r=iy();try{va(o);for(let s=i;s<a;s++){let c=t.data[s],p=n[s];rd(s),(c.hostBindings!==null||c.hostVars!==0||c.hostAttrs!==null)&&yI(c,p)}}finally{va(-1),rd(r)}}function yI(t,n){t.hostBindings!==null&&t.hostBindings(1,n)}function kf(t,n){let e=t.directiveRegistry,i=null;if(e)for(let a=0;a<e.length;a++){let o=e[a];W_(n,o.selectors,!1)&&(i??=[],Xn(o)?i.unshift(o):i.push(o))}return i}function _I(t,n,e,i,a,o){let r=xn(t,n);r0(n[et],r,o,t.value,e,i,a)}function r0(t,n,e,i,a,o,r){if(o==null)r?.(o,i||``,a),t.removeAttribute(n,a,e);else{let s=r==null?$e(o):r(o,i||``,a);t.setAttribute(n,a,s,e)}}function xI(t,n,e,i,a,o){let r=o[n];if(r!==null)for(let s=0;s<r.length;s+=2){let c=r[s],p=r[s+1];bo(i,e,c,p)}}function If(t,n,e,i,a){let o=at+e,r=n[ge],s=a(r,n,t,i,e);n[o]=s,uo(t,!0);let c=t.type===2;return c?($_(n[et],s,t),(Xv()===0||cr(t))&&gr(s,n),Zv()):gr(s,n),dd()&&(!c||!Ld(t))&&Cf(r,n,s,t),t}function Mf(t){let n=t;return Qp()?Xp():(n=n.parent,uo(n,!1)),n}function CI(t,n){let e=t[ji];if(!e)return;let i;try{i=e.get(Ui,null)}catch{i=null}i?.(n)}function $d(t,n,e,i,a){let o=t.inputs?.[i],r=t.hostDirectiveInputs?.[i],s=!1;if(r)for(let c=0;c<r.length;c+=2){let p=r[c],m=r[c+1],g=n.data[p];bo(g,e[p],m,a),s=!0}if(o)for(let c of o){let p=e[c],m=n.data[c];bo(m,p,i,a),s=!0}return s}function SI(t,n,e,i,a,o){let r=null,s=null,c=null,p=!1,m=t.directiveToIndex.get(i.type);if(typeof m==`number`?r=m:[r,s,c]=m,s!==null&&c!==null&&t.hostDirectiveInputs&&Object.hasOwn(t.hostDirectiveInputs,a)){let g=t.hostDirectiveInputs[a];for(let v=0;v<g.length;v+=2){let b=g[v];if(b>=s&&b<=c){let _=n.data[b],E=g[v+1];bo(_,e[b],E,o),p=!0}else if(b>c)break}}return r!==null&&Object.hasOwn(i.inputs,a)&&(bo(i,e[r],a,o),p=!0),p}function wI(t,n){let e=On(n,t),i=e[ge];EI(i,e);let a=e[Kn];a!==null&&e[io]===null&&(e[io]=O_(a,e[ji])),Ke(ze.ComponentStart);try{Nf(i,e,e[yt])}finally{Ke(ze.ComponentEnd,e[yt])}}function EI(t,n){for(let e=n.length;e<t.blueprint.length;e++)n.push(t.blueprint[e])}function Nf(t,n,e){cd(n);try{let i=t.viewQuery;i!==null&&Dh(1,i,e);let a=t.template;a!==null&&i0(t,n,a,1,e),t.firstCreatePass&&(t.firstCreatePass=!1),n[yi]?.finishViewCreation(t),t.staticContentQueries&&R_(t,n),t.staticViewQueries&&Dh(2,t.viewQuery,e);let o=t.components;o!==null&&DI(n,o)}catch(i){throw t.firstCreatePass&&(t.incompleteFirstPass=!0,t.firstCreatePass=!1),i}finally{n[xe]&=-5,ld()}}function DI(t,n){for(let e=0;e<n.length;e++)wI(t,n[e])}function Js(t,n,e,i){let a=he(null);try{let o=n.tView,c=Ef(t,o,e,t[xe]&4096?4096:16,null,n,null,null,i?.injector??null,i?.embeddedViewInjector??null,i?.dehydratedView??null);c[ga]=t[n.index];let m=t[yi];return m!==null&&(c[yi]=m.createEmbeddedView(o)),Nf(o,c,e),c}finally{he(a)}}function br(t,n){return!n||n.firstChild===null||S_(t)}function $s(t,n,e,i,a=!1){if(t.type===3){let o=t.firstChild,r=o.next,s=$t(n[o.index]),c=$t(n[r.index]),p=s;for(;p!==null&&(i.push(p),p!==c);)p=p.nextSibling;return i}for(;e!==null;){if(e.type===128){e=a?e.projectionNext:e.next;continue}let o=n[e.index];if(o!==null)if(An(o)){let s=o[ro];s!==o[Kn]&&i.push($t(o)),o[xe]&4||s0(o,i),i.push(s)}else i.push($t(o));let r=e.type;if(r&8)$s(t,n,e.child,i);else if(r&32){let s=bf(e,n),c;for(;c=s();)i.push(c)}else if(r&16){let s=Z_(n,e);if(Array.isArray(s))i.push(...s);else{let c=Vi(n[Qt]);$s(c[ge],c,s,i,!0)}}e=a?e.projectionNext:e.next}return i}function s0(t,n){for(let e=_t;e<t.length;e++){let i=t[e],a=i[ge].firstChild;a!==null&&$s(i[ge],i,a,n)}}function c0(t){if(t[oo]!==null){for(let n of t[oo])n.impl.addSequence(n);t[oo].length=0}}var l0=[];function kI(t){return t[yn]??II(t)}function II(t){let n=l0.pop()??Object.create(NI);return n.lView=t,n}function MI(t){t.lView[yn]!==t&&(t.lView=null,l0.push(t))}var NI=s(r({},Ua),{consumerIsAlwaysLive:!0,kind:`template`,consumerMarkedDirty:t=>{lo(t.lView)},consumerOnSignalRead(){this.lView[yn]=this}});function TI(t){let n=t[yn]??Object.create(AI);return n.lView=t,n}var AI=s(r({},Ua),{consumerIsAlwaysLive:!0,kind:`template`,consumerMarkedDirty:t=>{let n=Vi(t.lView);for(;n&&!d0(n[ge]);)n=Vi(n);n&&Hp(n)},consumerOnSignalRead(){this.lView[yn]=this}});function d0(t){return t.type!==2}function u0(t){if(t[Li]===null)return;let n=!0;for(;n;){let e=!1;for(let i of t[Li])if(i.dirty&&(e=!0,i.zone===null||Zone.current===i.zone?i.run():i.zone.run(()=>i.run()),t[Li]===null))return;n=e&&!!(t[xe]&8192)}}var OI=100;function m0(t,n=0){let i=t[Qn].rendererFactory;i.begin?.();try{RI(t,n)}finally{i.end?.()}}function RI(t,n){let e=Jp();try{xs(!0),Vh(t,n);let i=0;for(;Rs(t);){if(i===OI)throw new G(103,!1);i++,Vh(t,1)}}finally{xs(e)}}function PI(t,n,e,i){if(co(n))return;let a=n[xe];cd(n);let s=!0,c=null,p=null;d0(t)?(p=kI(n),c=la(p)):il()===null?(s=!1,p=TI(n),c=la(p)):n[yn]&&(da(n[yn]),n[yn]=null);try{zp(n),ey(t.bindingStartIndex),e!==null&&i0(t,n,e,2,i);let m=(a&3)===3;if(m){let b=t.preOrderCheckHooks;b!==null&&bd(n,b,null)}else{let b=t.preOrderHooks;b!==null&&vd(n,b,0,null),lh(n,0)}if(FI(n),u0(n),p0(n,0),t.contentQueries!==null&&R_(t,n),true)if(m){let b=t.contentCheckHooks;b!==null&&bd(n,b)}else{let b=t.contentHooks;b!==null&&vd(n,b,1),lh(n,1)}VI(t,n);let g=t.components;g!==null&&f0(n,g,0);let v=t.viewQuery;if(v!==null&&Dh(2,v,i),true)if(m){let b=t.viewCheckHooks;b!==null&&bd(n,b)}else{let b=t.viewHooks;b!==null&&vd(n,b,2),lh(n,2)}if(t.firstUpdatePass===!0&&(t.firstUpdatePass=!1),n[Jl]){for(let b of n[Jl])b();n[Jl]=null}c0(n),n[xe]&=-73}catch(m){let g=!1,v=m,b=n;for(;b!==null;){if(An(b)){b=b[St];continue}let _=b[sr];if(_)try{let E=n[Qt][yt],ae={declarationInstance:E,declarationType:E?.constructor,caughtBy:_};_(hy(v),ae),g=!0;break}catch(E){v=E}b=b[St]}if(!g)throw lo(n),v}finally{p!==null&&($a(p,c),s&&MI(p)),ld()}}function p0(t,n){for(let e=E_(t);e!==null;e=D_(e))for(let i=_t;i<e.length;i++){let a=e[i];h0(a,n)}}function FI(t){for(let n=E_(t);n!==null;n=D_(n)){if(!(n[xe]&2))continue;let e=n[so];for(let i=0;i<e.length;i++){let a=e[i];Hp(a)}}}function LI(t,n,e){Ke(ze.ComponentStart);let i=On(n,t);try{h0(i,e)}finally{Ke(ze.ComponentEnd,i[yt])}}function h0(t,n){td(t)&&Vh(t,n)}function Vh(t,n){let i=t[ge],a=t[xe],o=t[yn],r=!!(n===0&&a&16);if(r||=!!(a&64&&n===0),r||=!!(a&1024),r||=!!(o?.dirty&&Uo(o)),r||=!1,o&&(o.dirty=!1),t[xe]&=-9217,r)PI(i,t,i.template,t[yt]);else if(a&8192){let s=he(null);try{u0(t),p0(t,1);let c=i.components;c!==null&&f0(t,c,1),c0(t)}finally{he(s)}}}function f0(t,n,e){for(let i=0;i<n.length;i++)LI(t,n[i],e)}function VI(t,n){let e=t.hostBindingOpCodes;if(e!==null)try{for(let i=0;i<e.length;i++){let a=e[i];if(a<0)va(~a);else{let o=a,r=e[++i],s=e[++i];ny(r,o);let c=n[o];Ke(ze.HostBindingsUpdateStart,c);try{s(2,c)}finally{Ke(ze.HostBindingsUpdateEnd,c)}}}}finally{va(-1)}}function Tf(t,n){let e=Jp()?64:1088;for(t[Qn].changeDetectionScheduler?.notify(n);t;){t[xe]|=e;let i=Vi(t);if(lr(t)&&!i)return t;t=i}return null}function g0(t,n,e,i){return[t,!0,0,n,null,i,null,e,null,null]}function b0(t,n){let e=_t+n;if(e<t.length)return t[e]}function ec(t,n,e,i=!0){let a=n[ge];if(BI(a,n,t,e),i){let r=Lh(e,t),s=n[et],c=s.parentNode(t[ro]);c!==null&&Zk(a,t[Ut],s,n,c,r)}let o=n[io];o!==null&&o.firstChild!==null&&(o.firstChild=null)}function v0(t,n){let e=Gs(t,n);return e!==void 0&&zd(e[ge],e),e}function Gs(t,n){if(t.length<=_t)return;let e=_t+n,i=t[e];if(i){let a=i[ga];a!==null&&a!==t&&xf(a,i),n>0&&(t[e-1][Tn]=i[Tn]);let o=Is(t,_t+n);Xk(i[ge],i);let r=o[yi];r!==null&&r.detachView(o[ge]),i[St]=null,i[Tn]=null,i[xe]&=-129}return i}function BI(t,n,e,i){let a=_t+i,o=e.length;i>0&&(e[a-1][Tn]=n),i<o-_t?(n[Tn]=e[a],Np(e,_t+i,n)):(e.push(n),n[Tn]=null),n[St]=e;let r=n[ga];r!==null&&e!==r&&y0(r,n);let s=n[yi];s!==null&&s.insertView(t),nd(n),n[xe]|=128}function y0(t,n){let e=t[so],i=n[St];if(zi(i))t[xe]|=2;else{let a=i[St][Qt];n[Qt]!==a&&(t[xe]|=2)}e===null?t[so]=[n]:e.push(n)}var Ca=class{_lView;_cdRefInjectingView;_appRef=null;_attachedToViewContainer=!1;exhaustive;get rootNodes(){let n=this._lView,e=n[ge];return $s(e,n,e.firstChild,[])}constructor(n,e){this._lView=n,this._cdRefInjectingView=e}get context(){return this._lView[yt]}set context(n){this._lView[yt]=n}get destroyed(){return co(this._lView)}destroy(){if(this._appRef)this._appRef.detachView(this);else if(this._attachedToViewContainer){let n=this._lView[St];if(An(n)){let e=n[Ts],i=e?e.indexOf(this):-1;i>-1&&(Gs(n,i),Is(e,i))}this._attachedToViewContainer=!1}zd(this._lView[ge],this._lView)}onDestroy(n){id(this._lView,n)}markForCheck(){Tf(this._cdRefInjectingView||this._lView,4)}detach(){this._lView[xe]&=-129}reattach(){nd(this._lView),this._lView[xe]|=128}detectChanges(){this._lView[xe]|=1024,m0(this._lView)}checkNoChanges(){}attachToViewContainerRef(){if(this._appRef)throw new G(902,!1);this._attachedToViewContainer=!0}detachFromAppRef(){this._appRef=null;let n=lr(this._lView),e=this._lView[ga];e!==null&&!n&&xf(e,this._lView),K_(this._lView[ge],this._lView)}attachToAppRef(n){if(this._attachedToViewContainer)throw new G(902,!1);this._appRef=n;let e=lr(this._lView),i=this._lView[ga];i!==null&&!e&&y0(i,this._lView),nd(this._lView)}};var Lt=(()=>{class t{_declarationLView;_declarationTContainer;elementRef;static __NG_ELEMENT_ID__=jI;constructor(e,i,a){this._declarationLView=e,this._declarationTContainer=i,this.elementRef=a}get ssrId(){return this._declarationTContainer.tView?.ssrId||null}createEmbeddedView(e,i){return this.createEmbeddedViewImpl(e,i)}createEmbeddedViewImpl(e,i,a){return new Ca(Js(this._declarationLView,this._declarationTContainer,e,{embeddedViewInjector:i,dehydratedView:a}))}}return t})();function jI(){return Gd(It(),le())}function Gd(t,n){return t.type&4?new Lt(n,t,Sr(t,n)):null}function yo(t,n,e,i,a){let o=t.data[n];if(o===null)o=zI(t,n,e,i,a),ty()&&(o.flags|=32);else if(o.type&64){o.type=e,o.value=i,o.attrs=a;let r=Jv();o.injectorIndex=r===null?-1:r.injectorIndex}return uo(o,!0),o}function zI(t,n,e,i,a){let o=Kp(),r=Qp(),s=r?o:o&&o.parent,c=t.data[n]=UI(t,s,e,n,i,a);return HI(t,c,o,r),c}function HI(t,n,e,i){t.firstChild===null&&(t.firstChild=n),e!==null&&(i?e.child==null&&n.parent!==null&&(e.child=n):e.next===null&&(e.next=n,n.prev=e))}function UI(t,n,e,i,a,o){let r=n?n.injectorIndex:-1,s=0;return Wp()&&(s|=128),{type:e,index:i,insertBeforeIndex:null,injectorIndex:r,directiveStart:-1,directiveEnd:-1,directiveStylingLast:-1,componentOffset:-1,controlDirectiveIndex:-1,customControlIndex:-1,propertyBindings:null,flags:s,providerIndexes:0,value:a,namespace:nh(),attrs:o,mergedAttrs:null,localNames:null,initialInputs:null,inputs:null,hostDirectiveInputs:null,outputs:null,hostDirectiveOutputs:null,directiveToIndex:null,tView:null,next:null,prev:null,projectionNext:null,child:null,parent:n,projection:null,styles:null,stylesWithoutHost:null,residualStyles:void 0,classes:null,classesWithoutHost:null,residualClasses:void 0,classBindings:0,styleBindings:0}}function $I(t){let n=t[Lp]??[],i=t[St][et],a=[];for(let o of n)o.data[N_]!==void 0?a.push(o):GI(o,i);t[Lp]=a}function GI(t,n){let e=0,i=t.firstChild;if(i){let a=t.data[M_];for(;e<a;){let o=i.nextSibling;U_(n,i,!1),i=o,e++}}}var WI=()=>null;var qI=()=>null;function Dd(t,n){return WI(t,n)}function _0(t,n,e){return qI(t,n,e)}var x0=class{};var Mt=class{};var Oe=class{destroyNode=null;static __NG_ELEMENT_ID__=()=>YI()};function YI(){let t=le(),e=On(It().index,t);return(zi(e)?e:t)[et]}var C0=(()=>{class t{static ɵprov=fe({token:t,providedIn:`root`,factory:()=>null})}return t})();function S0(t){return t.debugInfo?.className||t.type.name||null}var _d={};var kd=class{injector;parentInjector;constructor(n,e){this.injector=n,this.parentInjector=e}get(n,e,i){let a=this.injector.get(n,_d,i);return a!==_d||e===_d?a:this.parentInjector.get(n,e,i)}};function Af(t,n,e){return t[n]=e}function KI(t,n){return t[n]}function mn(t,n,e){if(e===mt)return!1;let i=t[n];return Object.is(i,e)?!1:(t[n]=e,!0)}function vr(t,n,e,i){let a=mn(t,n,e);return mn(t,n+1,i)||a}function w0(t,n,e,i,a){let o=vr(t,n,e,i);return mn(t,n+2,a)||o}function Wd(t,n,e,i,a,o){let r=vr(t,n,e,i);return vr(t,n+2,a,o)||r}function fo(t,n,e){return function i(a){let o=i.__ngNativeEl__;o!==void 0&&ek(a,o);Tf(_i(t)?On(t.index,n):n,5);let s=n[yt],c=Vy(n,s,e,a),p=i.__ngNextListenerFn__;for(;p;)c=Vy(n,s,p,a)&&c,p=p.__ngNextListenerFn__;return c}}function Vy(t,n,e,i){let a=he(null);try{return Ke(ze.OutputStart,n,e),e(i)!==!1}catch(o){return CI(t,o),!1}finally{Ke(ze.OutputEnd,n,e),he(a)}}function Of(t,n,e,i,a,o,r,s){let c=cr(t),p=!1,m=null;if(!i&&c&&(m=XI(n,e,o,t.index)),m!==null){let g=m.__ngLastListenerFn__||m;g.__ngNextListenerFn__=r,m.__ngLastListenerFn__=r,p=!0}else{let g=xn(t,e),v=i?i(g):g;i||(s.__ngNativeEl__=g);let b=a.listen(v,o,s);if(!QI(o))E0(i?E=>i($t(E[t.index])):t.index,n,e,o,s,b,!1)}return p}function QI(t){return t.startsWith(`animation`)||t.startsWith(`transition`)}function XI(t,n,e,i){let a=t.cleanup;if(a!=null)for(let o=0;o<a.length-1;o+=2){let r=a[o];if(r===e&&a[o+1]===i){let s=n[or],c=a[o+2];return s&&s.length>c?s[c]:null}typeof r==`string`&&(o+=2)}return null}function E0(t,n,e,i,a,o,r){let s=n.firstCreatePass?$p(n):null,c=Up(e),p=c.length;c.push(a,o),s&&s.push(i,t,p,(p+1)*(r?-1:1))}function By(t,n,e,i,a){let o=null,r=null,s=null,c=!1,p=t.directiveToIndex.get(e.type);if(typeof p==`number`?o=p:[o,r,s]=p,r!==null&&s!==null&&t.hostDirectiveOutputs&&Object.hasOwn(t.hostDirectiveOutputs,i)){let m=t.hostDirectiveOutputs[i];for(let g=0;g<m.length;g+=2){let v=m[g];if(v>=r&&v<=s)c=!0,Id(t,n,v,m[g+1],i,a);else if(v>s)break}}return Object.hasOwn(e.outputs,i)&&(c=!0,Id(t,n,o,i,i,a)),c}function Id(t,n,e,i,a,o){let r=n[e],s=n[ge],g=r[s.data[e].outputs[i]].subscribe(o);E0(t.index,s,n,a,o,g,!0)}function wr(){ZI()}function ZI(){let t=le(),n=Xe(),e=It();if(n.firstCreatePass&&eM(n,e),e.controlDirectiveIndex===-1)return;Si(`NgSignalForms`);let i=t[e.controlDirectiveIndex];n.data[e.controlDirectiveIndex].controlDef.create(i,new Md(t,n,e))}function Er(){JI()}function JI(){let t=le(),n=Xe(),e=dr();if(e.controlDirectiveIndex===-1)return;let i=n.data[e.controlDirectiveIndex].controlDef,a=t[e.controlDirectiveIndex];i.update(a,new Md(t,n,e))}var Md=class{lView;tView;tNode;hasPassThrough;constructor(n,e,i){this.lView=n,this.tView=e,this.tNode=i,this.hasPassThrough=!!(i.flags&4096)}get customControl(){return this.tNode.customControlIndex!==-1?this.lView[this.tNode.customControlIndex]:void 0}get nativeElement(){return xn(this.tNode,this.lView)}get descriptor(){return`<${this.tNode.value}>`}listenToCustomControlOutput(n,e){let i=this.tView.data[this.tNode.customControlIndex];By(this.tNode,this.lView,i,n,fo(this.tNode,this.lView,e))}listenToCustomControlModel(n){let e=this.tNode.flags&1024?`valueChange`:`checkedChange`,i=this.tView.data[this.tNode.customControlIndex];By(this.tNode,this.lView,i,e,fo(this.tNode,this.lView,n))}listenToDom(n,e){Of(this.tNode,this.tView,this.lView,void 0,this.lView[et],n,e,fo(this.tNode,this.lView,e))}setInputOnDirectives(n,e,i){let a=this.tNode.inputs?.[n],o=this.tNode.hostDirectiveInputs?.[n];if(!a&&!o)return!1;let r=!1;if(a)for(let s of a){if(s===this.tNode.controlDirectiveIndex)continue;let c=this.lView[s],p=this.tView.data[s];(!i||i(zy(c,p,n)))&&(bo(p,c,n,e),r=!0)}if(o)for(let s=0;s<o.length;s+=2){let c=o[s];if(c===this.tNode.controlDirectiveIndex)continue;let p=this.lView[c],m=o[s+1],g=this.tView.data[c];(!i||i(zy(p,g,n)))&&(bo(g,p,m,e),r=!0)}return r}setCustomControlModelInput(n){let e=this.tView.data[this.tNode.customControlIndex],i=this.tNode.flags&1024?`value`:`checked`;SI(this.tNode,this.tView,this.lView,e,i,n)}customControlHasInput(n){if(this.tNode.customControlIndex===-1)return!1;let e=this.tView.data[this.tNode.customControlIndex];return(e.signalFormsInputPresence??=this._buildCustomControlInputCache(e))[n]===!0}_buildCustomControlInputCache(n){let e={};for(let i in n.inputs)e[i]=!0;if(n.hostDirectives!==null){let i=[...n.hostDirectives];for(;i.length>0;){let a=i.shift();if(typeof a!=`function`){for(let r in a.inputs)e[a.inputs[r]]=!0;let o=jy(a.directive);o!==null&&i.push(...o);continue}for(let o of a()){if(typeof o==`function`)continue;if(o.inputs)for(let s=0;s<o.inputs.length;s+=2){let c=o.inputs[s+1]||o.inputs[s];e[c]=!0}let r=jy(o.directive);r!==null&&i.push(...r)}}}return e}};function jy(t){return typeof t==`function`&&`ɵdir`in t?t.ɵdir.hostDirectives??null:null}function zy(t,n,e){if(!n.inputs||!Object.hasOwn(n.inputs,e))return;let[i,a]=n.inputs[e];if((a&Xs.SignalBased)!==0){let r=t[i][Ct];return r.value===Cr?void 0:r.value}return t[i]}function eM(t,n,e){for(let a=n.directiveStart;a<n.directiveEnd;a++)if(t.data[a].controlDef){n.controlDirectiveIndex=a;break}if(n.controlDirectiveIndex===-1)return;let i=t.data[n.controlDirectiveIndex].controlDef;if(i.passThroughInput&&(n.inputs?.[i.passThroughInput]?.length??0)>1){n.flags|=4096;return}tM(t,n)}function tM(t,n){for(let e=n.directiveStart;e<n.directiveEnd;e++){let i=t.data[e];if(!(n.directiveToIndex&&!n.directiveToIndex.has(i.type))){if(Hy(i,`value`)){n.flags|=1024,n.customControlIndex=e;return}if(Hy(i,`checked`)){n.flags|=2048,n.customControlIndex=e;return}}}if(n.hostDirectiveInputs!==null&&n.hostDirectiveOutputs!==null&&n.directiveToIndex!==null){let e=(i,a)=>{let o=n.hostDirectiveInputs[i],r=n.hostDirectiveOutputs[i+`Change`];if(!o||!r)return!1;for(let s=0;s<o.length;s+=2){let c=o[s];for(let p=0;p<r.length;p+=2)if(c===r[p])for(let g of n.directiveToIndex.values()){if(!Array.isArray(g))continue;let[v,b,_]=g;if(c>=b&&c<=_)return n.flags|=a,n.customControlIndex=v,!0}}return!1};if(e(`value`,1024)||e(`checked`,2048))return}}function Hy(t,n){return nM(t,n)&&iM(t,n+`Change`)}function nM(t,n){return n in t.inputs}function iM(t,n){return n in t.outputs}var Bh=Symbol(`BINDING`);var _o=new I(``);function Nd(t,n,e){let i=e?t.styles:null,a=e?t.classes:null,o=0;if(n!==null)for(let r=0;r<n.length;r++){let s=n[r];if(typeof s==`number`)o=s;else if(o==1)a=$l(a,s);else if(o==2){let c=s,p=n[++r];i=$l(i,c+`: `+p+`;`)}}e?t.styles=i:t.stylesWithoutHost=i,e?t.classes=a:t.classesWithoutHost=a}function He(t,n=0){let e=le();if(e===null)return Se(t,n);return y_(It(),e,Pt(t),n)}function Rf(){throw new Error(`invalid`)}function D0(t,n,e,i,a){let o=i===null?null:{"":-1},r=a(t,e);if(r!==null){let s=r,c=null,p=null;for(let m of r)if(m.resolveHostDirectives!==null){[s,c,p]=m.resolveHostDirectives(r);break}rM(t,n,e,s,o,c,p)}o!==null&&i!==null&&aM(e,i,o)}function aM(t,n,e){let i=t.localNames=[];for(let a=0;a<n.length;a+=2){let o=e[n[a+1]];if(o==null)throw new G(-301,!1);i.push(n[a],o)}}function oM(t,n,e){n.componentOffset=e,(t.components??=[]).push(n.index)}function rM(t,n,e,i,a,o,r){let s=i.length,c=null;for(let v=0;v<s;v++){let b=i[v];c===null&&Xn(b)&&(c=b,oM(t,e,v)),Ch(Ed(e,n),t,b.type)}mM(e,t.data.length,s),c?.viewProvidersResolver&&c.viewProvidersResolver(c);for(let v=0;v<s;v++){let b=i[v];b.providersResolver&&b.providersResolver(b)}let p=!1,m=!1,g=t0(t,n,s,null);s>0&&(e.directiveToIndex=new Map);for(let v=0;v<s;v++){let b=i[v];if(e.mergedAttrs=fr(e.mergedAttrs,b.hostAttrs),cM(t,e,n,g,b),uM(g,b,a),r!==null&&r.has(b)){let[E,P]=r.get(b);e.directiveToIndex.set(b.type,[g,E+e.directiveStart,P+e.directiveStart])}else(o===null||!o.has(b))&&e.directiveToIndex.set(b.type,g);b.contentQueries!==null&&(e.flags|=4),(b.hostBindings!==null||b.hostAttrs!==null||b.hostVars!==0)&&(e.flags|=64);let _=b.type.prototype;!p&&(_.ngOnChanges||_.ngOnInit||_.ngDoCheck)&&((t.preOrderHooks??=[]).push(e.index),p=!0),!m&&(_.ngOnChanges||_.ngDoCheck)&&((t.preOrderCheckHooks??=[]).push(e.index),m=!0),g++}sM(t,e,o)}function sM(t,n,e){for(let i=n.directiveStart;i<n.directiveEnd;i++){let a=t.data[i];if(e===null||!e.has(a))Uy(0,n,a,i),Uy(1,n,a,i),Gy(n,i,!1);else{let o=e.get(a);$y(0,n,o,i),$y(1,n,o,i),Gy(n,i,!0)}}}function Uy(t,n,e,i){let a=t===0?e.inputs:e.outputs;for(let o in a)if(Object.hasOwn(a,o)){let r;t===0?r=n.inputs??={}:r=n.outputs??={},r[o]??=[],r[o].push(i),k0(n,o)}}function $y(t,n,e,i){let a=t===0?e.inputs:e.outputs;for(let o in a)if(Object.hasOwn(a,o)){let r=a[o],s;t===0?s=n.hostDirectiveInputs??={}:s=n.hostDirectiveOutputs??={},s[r]??=[],s[r].push(i,o),k0(n,r)}}function k0(t,n){n===`class`?t.flags|=8:n===`style`&&(t.flags|=16)}function Gy(t,n,e){let{attrs:i,inputs:a,hostDirectiveInputs:o}=t;if(i===null||!e&&a===null||e&&o===null||gf(t)){t.initialInputs??=[],t.initialInputs.push(null);return}let r=null,s=0;for(;s<i.length;){let c=i[s];if(c===0){s+=4;continue}else if(c===5){s+=2;continue}else if(typeof c==`number`)break;if(!e&&Object.hasOwn(a,c)){let p=a[c];for(let m of p)if(m===n){r??=[],r.push(c,i[s+1]);break}}else if(e&&Object.hasOwn(o,c)){let p=o[c];for(let m=0;m<p.length;m+=2)if(p[m]===n){r??=[],r.push(p[m+1],i[s+1]);break}}s+=2}t.initialInputs??=[],t.initialInputs.push(r)}function cM(t,n,e,i,a){t.data[i]=a;let r=new go(a.factory||(a.factory=ma(a.type,!0)),Xn(a),He,null);t.blueprint[i]=r,e[i]=r,lM(t,n,i,t0(t,e,a.hostVars,mt),a)}function lM(t,n,e,i,a){let o=a.hostBindings;if(o){let r=t.hostBindingOpCodes;r===null&&(r=t.hostBindingOpCodes=[]);let s=~n.index;dM(r)!=s&&r.push(s),r.push(e,i,o)}}function dM(t){let n=t.length;for(;n>0;){let e=t[--n];if(typeof e==`number`&&e<0)return e}return 0}function uM(t,n,e){if(e){if(n.exportAs)for(let i=0;i<n.exportAs.length;i++)e[n.exportAs[i]]=t;Xn(n)&&(e[``]=t)}}function mM(t,n,e){t.flags|=1,t.directiveStart=n,t.directiveEnd=n+e,t.providerIndexes=n}function Pf(t,n,e,i,a,o,r,s){let c=n[ge],p=c.consts,g=yo(c,t,e,i,Cn(p,r));return o&&D0(c,n,g,Cn(p,s),a),g.mergedAttrs=fr(g.mergedAttrs,g.attrs),g.attrs!==null&&Nd(g,g.attrs,!1),g.mergedAttrs!==null&&Nd(g,g.mergedAttrs,!0),c.queries!==null&&c.queries.elementStart(c,g),g}function Ff(t,n){d_(t,n),Vp(n)&&t.queries.elementEnd(n)}function pM(t,n,e,i,a,o){let r=n.consts,c=yo(n,t,e,i,Cn(r,a));if(c.mergedAttrs=fr(c.mergedAttrs,c.attrs),o!=null){let p=Cn(r,o);c.localNames=[];for(let m=0;m<p.length;m+=2)c.localNames.push(p[m],-1)}return c.attrs!==null&&Nd(c,c.attrs,!1),c.mergedAttrs!==null&&Nd(c,c.mergedAttrs,!0),n.queries!==null&&n.queries.elementStart(n,c),c}var I0=typeof ShadowRoot<`u`;var hM=typeof Document<`u`;function fM(t){return Object.keys(t).map(n=>{let[e,i,a]=t[n],o={propName:e,templateName:n,isSignal:(i&Xs.SignalBased)!==0};return a&&(o.transform=a),o})}function gM(t){return Object.keys(t).map(n=>({propName:t[n],templateName:n}))}function bM(t,n,e){let i=n instanceof kt?n:n?.injector;return i&&t.getStandaloneInjector!==null&&(i=t.getStandaloneInjector(i)||i),i?new kd(e,i):e}function vM(t){let n=t.get(Mt,null);if(n===null)throw new G(407,!1);return{rendererFactory:n,sanitizer:t.get(C0,null),changeDetectionScheduler:t.get(bi,null),ngReflect:!1,tracingService:t.get(ii,null,{optional:!0})}}function yM(t,n,e){let i=M0(t);return z_(n,i,i===`svg`?Bp:i===`math`?Gv:e)}function _M(t){if((t&&`localName`in t&&typeof t.localName==`string`?t.localName:t?.tagName)?.toLowerCase()===`script`)throw new G(905,!1)}function M0(t){return(t.selectors[0][0]||`div`).toLowerCase()}var yr=class{componentDef;ngModule;selector;componentType;ngContentSelectors;isBoundToModule;cachedInputs=null;cachedOutputs=null;get inputs(){return this.cachedInputs??=fM(this.componentDef.inputs),this.cachedInputs}get outputs(){return this.cachedOutputs??=gM(this.componentDef.outputs),this.cachedOutputs}constructor(n,e){this.componentDef=n,this.ngModule=e,this.componentType=n.type,this.selector=Hk(n.selectors),this.ngContentSelectors=n.ngContentSelectors??[],this.isBoundToModule=!!e}create(n,e,i,a,o,r,s,c){Ke(ze.DynamicComponentStart);let p=he(null);try{let m=this.componentDef,g=bM(m,a||this.ngModule,n),v=vM(g),b=v.tracingService;return b&&b.componentCreate?b.componentCreate(S0(m),()=>this.createComponentRef(v,g,e,i,o,r,s,c)):this.createComponentRef(v,g,e,i,o,r,s,c)}finally{he(p)}}createComponentRef(n,e,i,a,o,r,s,c){let p=this.componentDef,m=xM(a,p,r,o,c),g=n.rendererFactory.createRenderer(null,p),v=a?mI(g,a,p.encapsulation,e):yM(p,g,s??null);_M(v);let b=e.get(_o,null),_=CM(v,()=>e.get(de,null)??I_());b&&b.addHost(_);let E=r?.some(Wy)||o?.some(j=>typeof j!=`function`&&j.bindings.some(Wy)),P=Ef(null,m,null,512|e0(p),null,null,n,g,e,null,O_(v,e,!0));b&&I0&&_ instanceof ShadowRoot&&id(P,()=>{b.removeHost(_)}),P[at]=v,cd(P);let ae=null;try{let j=Pf(at,P,2,`#host`,()=>m.directiveRegistry,!0,0);$_(g,v,j),gr(v,P),Ud(m,P,j),lf(m,j,P),Ff(m,j),i!==void 0&&wM(j,this.ngContentSelectors,i),ae=On(j.index,P),P[yt]=ae[yt],Nf(m,P,null)}catch(j){throw ae!==null&&wh(ae),wh(P),j}finally{Ke(ze.DynamicComponentEnd),ld()}return new Td(this.componentType,P,!!E)}};function xM(t,n,e,i,a){let o=t?[`ng-version`,`22.2.1`]:Uk(n.selectors[0]),r=null,s=null,c=0;if(e)for(let g of e)c+=g[Bh].requiredVars,g.create&&(g.targetIdx=0,(r??=[]).push(g)),g.update&&(g.targetIdx=0,(s??=[]).push(g));if(i)for(let g=0;g<i.length;g++){let v=i[g];if(typeof v!=`function`)for(let b of v.bindings){c+=b[Bh].requiredVars;let _=g+1;b.create&&(b.targetIdx=_,(r??=[]).push(b)),b.update&&(b.targetIdx=_,(s??=[]).push(b))}}let p=[n];if(i)for(let g of i){let b=ks(typeof g==`function`?g:g.type);p.push(b)}return wf(0,null,SM(r,s),1,c,p,null,null,null,[o],null)}function CM(t,n){let e=t.getRootNode?.();return hM&&e instanceof Document?e.head:e&&I0&&e instanceof ShadowRoot?e:n().head}function SM(t,n){return!t&&!n?null:e=>{if(e&1&&t)for(let i of t)i.create();if(e&2&&n)for(let i of n)i.update()}}function Wy(t){let n=t[Bh].kind;return n===`input`||n===`twoWay`}var Td=class extends x0{_rootLView;_hasInputBindings;instance;hostView;changeDetectorRef;componentType;location;previousInputValues=null;_tNode;constructor(n,e,i){super(),this._rootLView=e,this._hasInputBindings=i,this._tNode=ed(e[ge],at),this.location=Sr(this._tNode,e),this.instance=On(this._tNode.index,e)[yt],this.hostView=this.changeDetectorRef=new Ca(e,void 0),this.componentType=n}setInput(n,e){this._hasInputBindings;let i=this._tNode;if(this.previousInputValues??=new Map,this.previousInputValues.has(n)&&Object.is(this.previousInputValues.get(n),e))return;let a=this._rootLView;$d(i,a[ge],a,n,e);this.previousInputValues.set(n,e);Tf(On(i.index,a),1)}get injector(){return new Wi(this._tNode,this._rootLView)}destroy(){this.hostView.destroy()}onDestroy(n){this.hostView.onDestroy(n)}};function wM(t,n,e){let i=t.projection=[];for(let a=0;a<n.length;a++){let o=e[a];i.push(o!=null&&o.length?Array.from(o):null)}}var Vt=(()=>{class t{static __NG_ELEMENT_ID__=EM}return t})();function EM(){return N0(It(),le())}var jh=class t extends Vt{_lContainer;_hostTNode;_hostLView;constructor(n,e,i){super(),this._lContainer=n,this._hostTNode=e,this._hostLView=i}get element(){return Sr(this._hostTNode,this._hostLView)}get injector(){return new Wi(this._hostTNode,this._hostLView)}get parentInjector(){let n=of(this._hostTNode,this._hostLView);if(p_(n)){let e=Sd(n,this._hostLView),i=Cd(n),a=e[ge].data[i+8];return new Wi(a,e)}else return new Wi(null,this._hostLView)}clear(){for(;this.length>0;)this.remove(this.length-1)}get(n){let e=qy(this._lContainer);return e!==null&&e[n]||null}get length(){return this._lContainer.length-_t}createEmbeddedView(n,e,i){let a,o,r;typeof i==`number`?a=i:i!=null&&(a=i.index,o=i.injector,r=i.onError);let s=Dd(this._lContainer,n.ssrId),c=n.createEmbeddedViewImpl(e||{},o,s);return r&&(c._lView[sr]=r),this.insertImpl(c,a,br(this._hostTNode,s)),c}createComponent(n,e,i,a,o,r,s){let c,p,m=e||{};c=m.index,i=m.injector,a=m.projectableNodes,o=m.environmentInjector||m.ngModuleRef,r=m.directives,s=m.bindings,p=m.onError;let g=new yr(pa(n)),v=i||this.parentInjector;if(!o&&g.ngModule==null){let j=this.parentInjector.get(kt,null);j&&(o=j)}let b=pa(g.componentType??{}),_=Dd(this._lContainer,b?.id??null),E=_?.firstChild??null,P=g.create(v,a,E,o,r,s,this._getHostElementNamespace());return p&&(P.hostView._lView[sr]=p),this.insertImpl(P.hostView,c,br(this._hostTNode,_)),P}_getHostElementNamespace(){if(this._hostTNode.type&2){let n=this._hostTNode.parent??this._hostLView[Ut];return n!==null&&n.type&2&&typeof n.value==`string`&&n.value.toLowerCase()===`foreignobject`?null:n?.namespace??null}return this._hostTNode.namespace}insert(n,e){return this.insertImpl(n,e,!0)}insertImpl(n,e,i){let a=n._lView;if(qv(a)){let s=this.indexOf(n);if(s!==-1)this.detach(s);else{let c=a[St],p=new t(c,c[Ut],c[St]);p.detach(p.indexOf(n))}}let o=this._adjustIndex(e),r=this._lContainer;return ec(r,a,o,i),n.attachToViewContainerRef(),Np(mh(r),o,n),n}move(n,e){return this.insert(n,e)}indexOf(n){let e=qy(this._lContainer);return e!==null?e.indexOf(n):-1}remove(n){let e=this._adjustIndex(n,-1),i=Gs(this._lContainer,e);i&&(Is(mh(this._lContainer),e),zd(i[ge],i))}detach(n){let e=this._adjustIndex(n,-1),i=Gs(this._lContainer,e);return i&&Is(mh(this._lContainer),e)!=null?new Ca(i):null}_adjustIndex(n,e=0){return n??this.length+e}};function qy(t){return t[Ts]}function mh(t){return t[Ts]||(t[Ts]=[])}function N0(t,n){let e,i=n[t.index];return An(i)?e=i:(e=g0(i,n,null,t),n[t.index]=e,Df(n,e)),kM(e,n,t,i),new jh(e,t,n)}function DM(t,n){let e=t[et],i=e.createComment(``),a=xn(n,t);return ho(e,e.parentNode(a),i,e.nextSibling(a),!1),i}var kM=NM;var IM=()=>!1;function MM(t,n,e){return IM(t,n,e)}function NM(t,n,e,i){if(t[ro])return;let a;e.type&8?a=$t(i):a=DM(n,e),t[ro]=a}var zh=class t{queryList;matches=null;constructor(n){this.queryList=n}clone(){return new t(this.queryList)}setDirty(){this.queryList.setDirty()}};var Hh=class t{queries;constructor(n=[]){this.queries=n}createEmbeddedView(n){let e=n.queries;if(e!==null){let i=n.contentQueries!==null?n.contentQueries[0]:e.length,a=[];for(let o=0;o<i;o++){let r=e.getByIndex(o),s=this.queries[r.indexInDeclarationView];a.push(s.clone())}return new t(a)}return null}insertView(n){this.dirtyQueriesWithMatches(n)}detachView(n){this.dirtyQueriesWithMatches(n)}finishViewCreation(n){this.dirtyQueriesWithMatches(n)}dirtyQueriesWithMatches(n){for(let e=0;e<this.queries.length;e++)Vf(n,e).matches!==null&&this.queries[e].setDirty()}};var Ad=class{flags;read;predicate;constructor(n,e,i=null){this.flags=e,this.read=i,typeof n==`string`?this.predicate=PM(n):this.predicate=n}};var Uh=class t{queries;constructor(n=[]){this.queries=n}elementStart(n,e){for(let i=0;i<this.queries.length;i++)this.queries[i].elementStart(n,e)}elementEnd(n){for(let e=0;e<this.queries.length;e++)this.queries[e].elementEnd(n)}embeddedTView(n){let e=null;for(let i=0;i<this.length;i++){let a=e!==null?e.length:0,o=this.getByIndex(i).embeddedTView(n,a);o&&(o.indexInDeclarationView=i,e!==null?e.push(o):e=[o])}return e!==null?new t(e):null}template(n,e){for(let i=0;i<this.queries.length;i++)this.queries[i].template(n,e)}getByIndex(n){return this.queries[n]}get length(){return this.queries.length}track(n){this.queries.push(n)}};var $h=class t{metadata;matches=null;indexInDeclarationView=-1;crossesNgTemplate=!1;_declarationNodeIndex;_appliesToNextNode=!0;constructor(n,e=-1){this.metadata=n,this._declarationNodeIndex=e}elementStart(n,e){this.isApplyingToNode(e)&&this.matchTNode(n,e)}elementEnd(n){this._declarationNodeIndex===n.index&&(this._appliesToNextNode=!1)}template(n,e){this.elementStart(n,e)}embeddedTView(n,e){return this.isApplyingToNode(n)?(this.crossesNgTemplate=!0,this.addMatch(-n.index,e),new t(this.metadata)):null}isApplyingToNode(n){if(this._appliesToNextNode&&(this.metadata.flags&1)!==1){let e=this._declarationNodeIndex,i=n.parent;for(;i!==null&&i.type&8&&i.index!==e;)i=i.parent;return e===(i!==null?i.index:-1)}return this._appliesToNextNode}matchTNode(n,e){let i=this.metadata.predicate;if(Array.isArray(i))for(let a=0;a<i.length;a++){let o=i[a];this.matchTNodeWithReadOption(n,e,TM(e,o)),this.matchTNodeWithReadOption(n,e,yd(e,n,o,!1,!1))}else i===Lt?e.type&4&&this.matchTNodeWithReadOption(n,e,-1):this.matchTNodeWithReadOption(n,e,yd(e,n,i,!1,!1))}matchTNodeWithReadOption(n,e,i){if(i!==null){let a=this.metadata.read;if(a!==null)if(a===W||a===Vt||a===re||a===Lt&&e.type&4)this.addMatch(e.index,-2);else{let o=yd(e,n,a,!1,!1);o!==null&&this.addMatch(e.index,o)}else this.addMatch(e.index,i)}}addMatch(n,e){this.matches===null?this.matches=[n,e]:this.matches.push(n,e)}};function TM(t,n){let e=t.localNames;if(e!==null){for(let i=0;i<e.length;i+=2)if(e[i]===n)return e[i+1]}return null}function AM(t,n){return t.type&11?Sr(t,n):t.type&4?Gd(t,n):null}function OM(t,n,e,i){return e===-1?AM(n,t):e===-2?RM(t,n,i):Hs(t,t[ge],e,n)}function RM(t,n,e){if(e===W)return Sr(n,t);if(e===Lt)return Gd(n,t);if(e===Vt)return N0(n,t);if(e===re)return new Wi(n,t)}function T0(t,n,e,i){let a=n[yi].queries[i];if(a.matches===null){let o=t.data,r=e.matches,s=[];for(let c=0;r!==null&&c<r.length;c+=2){let p=r[c];if(p<0)s.push(null);else{let m=o[p];s.push(OM(n,m,r[c+1],e.metadata.read))}}a.matches=s}return a.matches}function Gh(t,n,e,i){let a=t.queries.getByIndex(e),o=a.matches;if(o!==null){let r=T0(t,n,a,e);for(let s=0;s<o.length;s+=2){let c=o[s];if(c>0)i.push(r[s/2]);else{let p=o[s+1],m=n[-c];for(let g=_t;g<m.length;g++){let v=m[g];v[ga]===v[St]&&Gh(v[ge],v,p,i)}if(m[so]!==null){let g=m[so];for(let v=0;v<g.length;v++){let b=g[v];Gh(b[ge],b,p,i)}}}}}return i}function Lf(t,n){return t[yi].queries[n].queryList}function A0(t,n,e){let i=new Sn((e&4)===4);return Qv(t,n,i,i.destroy),(n[yi]??=new Hh).queries.push(new zh(i))-1}function O0(t,n,e){let i=Xe();return i.firstCreatePass&&(P0(i,new Ad(t,n,e),-1),(n&2)===2&&(i.staticViewQueries=!0)),A0(i,le(),n)}function R0(t,n,e,i){let a=Xe();if(a.firstCreatePass){let o=It();P0(a,new Ad(n,e,i),o.index),FM(a,t),(e&2)===2&&(a.staticContentQueries=!0)}return A0(a,le(),e)}function PM(t){return t.split(`,`).map(n=>n.trim())}function P0(t,n,e){t.queries===null&&(t.queries=new Uh),t.queries.track(new $h(n,e))}function FM(t,n){let e=t.contentQueries||(t.contentQueries=[]);n!==(e.length?e[e.length-1]:-1)&&e.push(t.queries.length-1,n)}function Vf(t,n){return t.queries.getByIndex(n)}function F0(t,n){let e=t[ge],i=Vf(e,n);return i.crossesNgTemplate?Gh(e,t,n,[]):T0(e,t,i,n)}function L0(t,n,e){let i,a=ps(()=>{i._dirtyCounter();let o=LM(i,t);if(n&&o===void 0)throw new G(-951,!1);return o});return i=a[Ct],i._dirtyCounter=N(0),i._flatValue=void 0,a}function Bf(t){return L0(!0,!1,t)}function jf(t){return L0(!0,!0,t)}function V0(t,n){let e=t[Ct];e._lView=le(),e._queryIndex=n,e._queryList=Lf(e._lView,n),e._queryList.onDirty(()=>e._dirtyCounter.update(i=>i+1))}function LM(t,n){let e=t._lView,i=t._queryIndex;if(e===void 0||i===void 0||e[xe]&4)return n?void 0:Kt;let a=Lf(e,i),o=F0(e,i);return a.reset(o,C_),n?a.first:a._changesDetected||t._flatValue===void 0?t._flatValue=a.toArray():t._flatValue}function xo(t){return!!t&&typeof t.then==`function`}function zf(t){return!!t&&typeof t.subscribe==`function`}var ni=class{};function Hf(t,n){return new Od(t,n??null,[])}var Od=class extends ni{ngModuleType;_parent;_bootstrapComponents=[];_r3Injector;instance;destroyCbs=[];constructor(n,e,i,a=!0){super(),this.ngModuleType=n,this._parent=e;let o=Iv(n);this._bootstrapComponents=Tk(o.bootstrap),this._r3Injector=ih(n,e,[{provide:ni,useValue:this},...i],ws(n),new Set([`environment`])),a&&this.resolveInjectorInitializers()}resolveInjectorInitializers(){this._r3Injector.resolveInjectorInitializers(),this.instance=this._r3Injector.get(this.ngModuleType)}get injector(){return this._r3Injector}destroy(){let n=this._r3Injector;!n.destroyed&&n.destroy(),this.destroyCbs.forEach(e=>e()),this.destroyCbs=null}onDestroy(n){this.destroyCbs.push(n)}};var Ws=class extends ni{injector;instance=null;constructor(n){super();let e=new eo([...n.providers,{provide:ni,useValue:this}],n.parent||ir(),n.debugName,new Set([`environment`]));this.injector=e,n.runEnvironmentInitializers&&e.resolveInjectorInitializers()}destroy(){this.injector.destroy()}onDestroy(n){this.injector.onDestroy(n)}};function B0(t,n,e=null){return new Ws({providers:t,parent:n,debugName:e,runEnvironmentInitializers:!0}).injector}var VM=(()=>{class t{_injector;cachedInjectors=new Map;constructor(e){this._injector=e}getOrCreateStandaloneInjector(e){if(!e.standalone)return null;if(!this.cachedInjectors.has(e)){let i=Ap(!1,e.type),a=i.length>0?B0([i],this._injector,``):null;this.cachedInjectors.set(e,a)}return this.cachedInjectors.get(e)}ngOnDestroy(){try{for(let e of this.cachedInjectors.values())e!==null&&e.destroy()}finally{this.cachedInjectors.clear()}}static ɵprov=fe({token:t,providedIn:`environment`,factory:()=>new t(Se(kt))})}return t})();function B(t){return Ys(()=>{let n=j0(t),e=s(r({},n),{decls:t.decls,vars:t.vars,template:t.template,consts:t.consts||null,ngContentSelectors:t.ngContentSelectors,onPush:t.changeDetection!==sf.Eager,directiveDefs:null,pipeDefs:null,dependencies:n.standalone&&t.dependencies||null,getStandaloneInjector:n.standalone?a=>a.get(VM).getOrCreateStandaloneInjector(e):null,getExternalStyles:null,signals:t.signals??!1,data:t.data||{},encapsulation:t.encapsulation||ei.Emulated,styles:t.styles||Kt,_:null,schemas:t.schemas||null,tView:null,id:``});n.standalone&&Si(`NgStandalone`),z0(e);let i=t.dependencies;return e.directiveDefs=Yy(i,BM),e.pipeDefs=Yy(i,Mv),e.id=HM(e),e})}function BM(t){return pa(t)||ks(t)}function oe(t){return Ys(()=>({type:t.type,bootstrap:t.bootstrap||Kt,declarations:t.declarations||Kt,imports:t.imports||Kt,exports:t.exports||Kt,transitiveCompileScopes:null,schemas:t.schemas||null,id:t.id||null}))}function jM(t,n){if(t==null)return ha;let e={};for(let i in t)if(Object.hasOwn(t,i)){let a=t[i],o,r,s,c;Array.isArray(a)?(s=a[0],o=a[1],r=a[2]??o,c=a[3]||null):(o=a,r=a,s=Xs.None,c=null),e[o]=[i,s,c],n[o]=r}return e}function zM(t){if(t==null)return ha;let n={};for(let e in t)Object.hasOwn(t,e)&&(n[t[e]]=e);return n}function Y(t){return Ys(()=>{let n=j0(t);return z0(n),n})}function tc(t){return{type:t.type,name:t.name,factory:null,pure:t.pure!==!1,standalone:t.standalone??!0,onDestroy:t.type.prototype.ngOnDestroy||null}}function j0(t){let n={};return{type:t.type,providersResolver:null,viewProvidersResolver:null,factory:null,hostBindings:t.hostBindings||null,hostVars:t.hostVars||0,hostAttrs:t.hostAttrs||null,contentQueries:t.contentQueries||null,declaredInputs:n,inputConfig:t.inputs||ha,exportAs:t.exportAs||null,standalone:t.standalone??!0,signals:t.signals===!0,selectors:t.selectors||Kt,viewQuery:t.viewQuery||null,features:t.features||null,setInput:null,resolveHostDirectives:null,hostDirectives:null,controlDef:null,signalFormsInputPresence:null,inputs:jM(t.inputs,n),outputs:zM(t.outputs),debugInfo:null}}function z0(t){t.features?.forEach(n=>n(t))}function Yy(t,n){return t?()=>{let e=typeof t==`function`?t():t,i=[];for(let a of e){let o=n(a);o!==null&&i.push(o)}return i}:null}function HM(t){let n=0,e=typeof t.consts==`function`?``:t.consts,i=[t.selectors,t.ngContentSelectors,t.hostVars,t.hostAttrs,e,t.vars,t.decls,t.encapsulation,t.standalone,t.signals,t.exportAs,JSON.stringify(t.inputs),JSON.stringify(t.outputs),Object.getOwnPropertyNames(t.type.prototype),!!t.contentQueries,!!t.viewQuery];for(let o of i.join(`|`))n=Math.imul(31,n)+o.charCodeAt(0)<<0;return n+=2147483648,`c`+n}var H0=new I(``);var Uf=(()=>{class t{resolve;reject;initialized=!1;done=!1;donePromise=new Promise((e,i)=>{this.resolve=e,this.reject=i});appInits=f(H0,{optional:!0})??[];injector=f(re);constructor(){}runInitializers(){if(this.initialized)return;let e=[];for(let a of this.appInits){let o=ar(this.injector,a);if(xo(o))e.push(o);else if(zf(o)){let r=new Promise((s,c)=>{o.subscribe({complete:s,error:c})});e.push(r)}}let i=()=>{this.done=!0,this.resolve()};Promise.all(e).then(()=>{i()}).catch(a=>{this.reject(a)}),e.length===0&&i(),this.initialized=!0}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function $f(t){return n=>{n.controlDef={create:(e,i)=>{e?.ɵngControlCreate(i)},update:(e,i)=>{e?.ɵngControlUpdate?.(i)},passThroughInput:t}}}function nc(t){let n=e=>{let i=Array.isArray(t);e.hostDirectives===null?(e.resolveHostDirectives=UM,e.hostDirectives=i?t.map(Wh):[t]):i?e.hostDirectives.unshift(...t.map(Wh)):e.hostDirectives.unshift(t)};return n.ngInherit=!0,n}function UM(t){let n=[],e=!1,i=null,a=null;for(let o=0;o<t.length;o++){let r=t[o];if(r.hostDirectives!==null){let s=n.length;i??=new Map,a??=new Map,U0(r,n,i,t),a.set(r,[s,n.length-1])}o===0&&Xn(r)&&(e=!0,n.push(r))}for(let o=e?1:0;o<t.length;o++)n.push(t[o]);return i!==null&&i.forEach((o,r)=>{$M(r.declaredInputs,o.inputs)}),[n,i,a]}function U0(t,n,e,i){if(t.hostDirectives!==null)for(let a of t.hostDirectives)if(typeof a==`function`){let o=a();for(let r of o)Ky(Wh(r),n,e,i)}else Ky(a,n,e,i)}function Ky(t,n,e,i){let a=ks(t.directive);if(U0(a,n,e,i),e.has(a)){let o=e.get(a);Qy(o,t.inputs,`input`),Qy(o,t.outputs,`output`)}else i.includes(a)||(e.set(a,t),n.push(a))}function Qy(t,n,e){let i=e===`input`?t.inputs:t.outputs;Object.keys(n).forEach(a=>{let o=n[a];(!Object.hasOwn(i,a)||i[a]===o)&&(i[a]=o)})}function Wh(t){return typeof t==`function`?{directive:Pt(t),inputs:{},outputs:{}}:{directive:Pt(t.directive),inputs:Xy(t.inputs),outputs:Xy(t.outputs)}}function Xy(t){let n={};if(t!==void 0&&t.length>0)for(let e=0;e<t.length;e+=2)n[t[e]]=t[e+1];return n}function $M(t,n){for(let e in n)if(Object.hasOwn(n,e)){let i=n[e];t[i]=t[e]}}function GM(t){return Object.getPrototypeOf(t.prototype).constructor}function Ve(t){let n=GM(t.type),e=!0,i=[t];for(;n&&n!==Function.prototype&&n!==Object.prototype;){let a,o=Object.hasOwn(n,Es)?n[Es]:void 0,r=Object.hasOwn(n,Ds)?n[Ds]:void 0;if(Xn(t))a=o??r;else{if(o)throw new G(903,!1);a=r}if(a){if(e){i.push(a);let c=t;c.inputs=ph(t.inputs),c.declaredInputs=ph(t.declaredInputs),c.outputs=ph(t.outputs);let p=a.hostBindings;p&&QM(t,p);let m=a.viewQuery,g=a.contentQueries;if(m&&YM(t,m),g&&KM(t,g),WM(t,a),kv(t.outputs,a.outputs),Xn(a)&&a.data.animation){let v=t.data;v.animation=(v.animation||[]).concat(a.data.animation)}}let s=a.features;if(s)for(let c=0;c<s.length;c++){let p=s[c];p&&p.ngInherit&&p(t),p===Ve&&(e=!1)}}n=Object.getPrototypeOf(n)}qM(i)}function WM(t,n){for(let e in n.inputs){if(!Object.hasOwn(n.inputs,e)||Object.hasOwn(t.inputs,e))continue;let i=n.inputs[e];i!==void 0&&(t.inputs[e]=i,t.declaredInputs[e]=n.declaredInputs[e])}}function qM(t){let n=0,e=null;for(let i=t.length-1;i>=0;i--){let a=t[i];a.hostVars=n+=a.hostVars,a.hostAttrs=fr(a.hostAttrs,e=fr(e,a.hostAttrs))}}function ph(t){return t===ha?{}:t===Kt?[]:t}function YM(t,n){let e=t.viewQuery;e?t.viewQuery=(i,a)=>{n(i,a),e(i,a)}:t.viewQuery=n}function KM(t,n){let e=t.contentQueries;e?t.contentQueries=(i,a,o)=>{n(i,a,o),e(i,a,o)}:t.contentQueries=n}function QM(t,n){let e=t.hostBindings;e?t.hostBindings=(i,a)=>{n(i,a),e(i,a)}:t.hostBindings=n}function $0(t,n,e,i,a,o,r,s){if(e.firstCreatePass){t.mergedAttrs=fr(t.mergedAttrs,t.attrs);let m=t.tView=wf(2,t,a,o,r,e.directiveRegistry,e.pipeRegistry,null,e.schemas,e.consts,null);e.queries!==null&&(e.queries.template(e,t),m.queries=e.queries.embeddedTView(t))}s&&(t.flags|=s),uo(t,!1);let c=ZM(e,n,t,i);dd()&&Cf(e,n,c,t),gr(c,n);let p=g0(c,n,c,t);n[i+at]=p,Df(n,p),MM(p,t,n)}function XM(t,n,e,i,a,o,r,s,c,p,m){let g=e+at,v;return n.firstCreatePass?(v=yo(n,g,4,r||null,s||null),ad()&&D0(n,t,v,Cn(n.consts,p),kf),d_(n,v)):v=n.data[g],$0(v,t,n,e,i,a,o,c),cr(v)&&Ud(n,t,v),p!=null&&Zs(t,v,m),v}function _r(t,n,e,i,a,o,r,s,c,p,m){let g=e+at,v;if(n.firstCreatePass){if(v=yo(n,g,4,r||null,s||null),p!=null){let b=Cn(n.consts,p);v.localNames=[];for(let _=0;_<b.length;_+=2)v.localNames.push(b[_],-1)}}else v=n.data[g];return $0(v,t,n,e,i,a,o,c),p!=null&&Zs(t,v,m),v}function ot(t,n,e,i,a,o,r,s){let c=le(),p=Xe();return XM(c,p,t,n,e,i,a,Cn(p.consts,o),void 0,r,s),ot}function Dr(t,n,e,i,a,o,r,s){let c=le(),p=Xe();return _r(c,p,t,n,e,i,a,Cn(p.consts,o),void 0,r,s),Dr}var ZM=JM;function JM(t,n,e,i){return Fs(!0),n[et].createComment(``)}var Gf=new I(``);var Wf=new I(``);function G0(){Gm(t=>{throw new G(-600,``)})}var e1=10;var Rn=(()=>{class t{_runningTick=!1;_destroyed=!1;_destroyListeners=[];_views=[];internalErrorHandler=f(Ui);afterRenderManager=f(jd);zonelessEnabled=f(Vs);rootEffectScheduler=f(pd);dirtyFlags=0;tracingSnapshot=null;allTestViews=new Set;autoDetectTestViews=new Set;includeAllTestViews=!1;afterTick=new L;get allViews(){return[...(this.includeAllTestViews?this.allTestViews:this.autoDetectTestViews).keys(),...this._views]}get destroyed(){return this._destroyed}componentTypes=[];components=[];internalPendingTask=f(po);get isStable(){return this.internalPendingTask.hasPendingTasksObservable.pipe(rt(e=>!e))}constructor(){f(ii,{optional:!0})}whenStable(){let e;return new Promise(i=>{e=this.isStable.subscribe({next:a=>{a&&i()}})}).finally(()=>{e.unsubscribe()})}_injector=f(kt);_rendererFactory=null;get injector(){return this._injector}bootstrap(e,i){return this.bootstrapImpl(e,i)}bootstrapImpl(e,i,a=re.NULL){return this._injector.get(Q).run(()=>{if(Ke(ze.BootstrapComponentStart),!this._injector.get(Uf).done)throw new G(405,``);let s=pa(e),c=this._injector.get(ni),p=new yr(s,c);this.componentTypes.push(e);let{hostElement:m,directives:g,bindings:v}=t1(i),b=m||p.selector,_=p.create(a,[],b,c.injector,g,v),E=_.location.nativeElement,P=_.injector.get(Gf,null);return P?.registerApplication(E),_.onDestroy(()=>{this.detachView(_.hostView),zs(this.components,_),P?.unregisterApplication(E)}),this._loadComponent(_),Ke(ze.BootstrapComponentEnd,_),_})}tick(){this.zonelessEnabled||(this.dirtyFlags|=1),this._tick()}_tick(){Ke(ze.ChangeDetectionStart),this.tracingSnapshot!==null?this.tracingSnapshot.run(Bd.CHANGE_DETECTION,this.tickImpl):this.tickImpl()}tickImpl=()=>{if(this._runningTick)throw Ke(ze.ChangeDetectionEnd),new G(101,!1);let e=he(null);try{this._runningTick=!0,this.synchronize()}finally{this._runningTick=!1,this.tracingSnapshot?.dispose(),this.tracingSnapshot=null,he(e),this.afterTick.next(),Ke(ze.ChangeDetectionEnd)}};synchronize(){this._rendererFactory===null&&!this._injector.destroyed&&(this._rendererFactory=this._injector.get(Mt,null,{optional:!0}));let e=0;for(;this.dirtyFlags!==0&&e++<e1;){Ke(ze.ChangeDetectionSyncStart);try{this.synchronizeOnce()}finally{Ke(ze.ChangeDetectionSyncEnd)}}}synchronizeOnce(){this.dirtyFlags&16&&(this.dirtyFlags&=-17,this.rootEffectScheduler.flush());let e=!1;if(this.dirtyFlags&7){let i=!!(this.dirtyFlags&1);this.dirtyFlags&=-8,this.dirtyFlags|=8;for(let{_lView:a}of this.allViews){if(!i&&!Rs(a))continue;m0(a,i&&!this.zonelessEnabled?0:1),e=!0}if(this.dirtyFlags&=-5,this.syncDirtyFlagsWithViews(),this.dirtyFlags&23)return}e||(this._rendererFactory?.begin?.(),this._rendererFactory?.end?.()),this.dirtyFlags&8&&(this.dirtyFlags&=-9,this.afterRenderManager.execute()),this.syncDirtyFlagsWithViews()}syncDirtyFlagsWithViews(){if(this.allViews.some(({_lView:e})=>Rs(e))){this.dirtyFlags|=2;return}else this.dirtyFlags&=-8}attachView(e){let i=e;this._views.push(i),i.attachToAppRef(this)}detachView(e){let i=e;zs(this._views,i),i.detachFromAppRef()}_loadComponent(e){this.attachView(e.hostView);try{this.tick()}catch(a){this.internalErrorHandler(a)}this.components.push(e),this._injector.get(Wf,[]).forEach(a=>a(e))}ngOnDestroy(){if(!this._destroyed)try{this._destroyListeners.forEach(e=>e()),this._views.slice().forEach(e=>e.destroy())}finally{this._destroyed=!0,this._views=[],this._destroyListeners=[]}}onDestroy(e){return this._destroyListeners.push(e),()=>zs(this._destroyListeners,e)}destroy(){if(this._destroyed)throw new G(406,!1);let e=this._injector;e.destroy&&!e.destroyed&&e.destroy()}get viewCount(){return this._views.length}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function t1(t){return t===void 0||typeof t==`string`||t instanceof Element?{hostElement:t}:t}function zs(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}function kr(t,n){let e=le();if(mn(e,ba(),n)){let a=Xe(),o=dr();if($d(o,a,e,t,n))_i(o)&&o0(e,o.index);else{let s=xn(o,e);r0(e[et],s,null,o.value,t,n,null)}}return kr}function te(t,n,e,i){let a=le();if(mn(a,ba(),n)){Xe();_I(dr(),a,t,n,e,i)}return te}var qh=class{destroy(n){}updateValue(n,e){}swap(n,e){let i=Math.min(n,e),a=Math.max(n,e),o=this.detach(a);if(a-i>1){let r=this.detach(i);this.attach(i,o),this.attach(a,r)}else this.attach(i,o)}move(n,e){this.attach(e,this.detach(n))}};function hh(t,n,e,i,a){return t===e&&Object.is(n,i)?1:Object.is(a(t,n),a(e,i))?-1:0}function n1(t,n,e,i){let a,o,r=0,s=t.length-1;if(Array.isArray(n)){he(i);let p=n.length-1;for(he(null);r<=s&&r<=p;){let m=t.at(r),g=n[r],v=hh(r,m,r,g,e);if(v!==0){v<0&&t.updateValue(r,g),r++;continue}let b=t.at(s),_=n[p],E=hh(s,b,p,_,e);if(E!==0){E<0&&t.updateValue(s,_),s--,p--;continue}let P=e(r,m),ae=e(s,b),j=e(r,g);if(Object.is(j,ae)){let x=e(p,_);Object.is(x,P)?(t.swap(r,s),t.updateValue(s,_),p--,s--):t.move(s,r),t.updateValue(r,g),r++;continue}if(a??=new Rd,o??=Jy(t,r,s,e),Yh(t,a,r,j))t.updateValue(r,g),r++,s++;else if(o.has(j))a.set(P,t.detach(r)),s--;else{let x=t.create(r,n[r]);t.attach(r,x),r++,s++}}for(;r<=p;)Zy(t,a,e,r,n[r]),r++}else if(n!=null){he(i);let p=n[Symbol.iterator]();he(null);let m=p.next();for(;!m.done&&r<=s;){let g=t.at(r),v=m.value,b=hh(r,g,r,v,e);if(b!==0)b<0&&t.updateValue(r,v),r++,m=p.next();else{a??=new Rd,o??=Jy(t,r,s,e);let _=e(r,v);if(Yh(t,a,r,_))t.updateValue(r,v),r++,s++,m=p.next();else if(!o.has(_))t.attach(r,t.create(r,v)),r++,s++,m=p.next();else{let E=e(r,g);a.set(E,t.detach(r)),s--}}}for(;!m.done;)Zy(t,a,e,t.length,m.value),m=p.next()}for(;r<=s;)t.destroy(t.detach(s--));a?.forEach(p=>{t.destroy(p)})}function Yh(t,n,e,i){return n!==void 0&&n.has(i)?(t.attach(e,n.get(i)),n.delete(i),!0):!1}function Zy(t,n,e,i,a){if(Yh(t,n,i,e(i,a)))t.updateValue(i,a);else{let o=t.create(i,a);t.attach(i,o)}}function Jy(t,n,e,i){let a=new Set;for(let o=n;o<=e;o++)a.add(i(o,t.at(o)));return a}var Rd=class{kvMap=new Map;_vMap=void 0;has(n){return this.kvMap.has(n)}delete(n){if(!this.has(n))return!1;let e=this.kvMap.get(n);return this._vMap!==void 0&&this._vMap.has(e)?(this.kvMap.set(n,this._vMap.get(e)),this._vMap.delete(e)):this.kvMap.delete(n),!0}get(n){return this.kvMap.get(n)}set(n,e){if(this.kvMap.has(n)){let i=this.kvMap.get(n);this._vMap===void 0&&(this._vMap=new Map);let a=this._vMap;for(;a.has(i);)i=a.get(i);a.set(i,e)}else this.kvMap.set(n,e)}forEach(n){for(let[e,i]of this.kvMap)if(n(i,e),this._vMap!==void 0){let a=this._vMap;for(;a.has(i);)i=a.get(i),n(i,e)}}};function D(t,n,e,i,a,o,r,s){Si(`NgControlFlow`);let c=le(),p=Xe();return _r(c,p,t,n,e,i,a,Cn(p.consts,o),256,r,s),qf}function qf(t,n,e,i,a,o,r,s){return Si(`NgControlFlow`),i1(t,n,e,i,a,o,r,s),qf}function i1(t,n,e,i,a,o,r,s){let c=le(),p=Xe();_r(c,p,t,n,e,i,a,Cn(p.consts,o),512,r,s)}function k(t,n){Si(`NgControlFlow`);let e=le(),i=ba(),a=e[i]!==mt?e[i]:-1,o=a!==-1?Pd(e,at+a):void 0,r=0;if(mn(e,i,t)){let s=he(null);try{if(o!==void 0&&v0(o,r),t!==-1){let c=at+t,p=Pd(e,c),m=Zh(e[ge],c),g=_0(p,m,e);ec(p,Js(e,m,n,{dehydratedView:g}),r,br(m,g))}}finally{he(s)}}else if(o!==void 0){let s=b0(o,r);s!==void 0&&(s[yt]=n)}}var Kh=class{lContainer;$implicit;$index;constructor(n,e,i){this.lContainer=n,this.$implicit=e,this.$index=i}get $count(){return this.lContainer.length-_t}};function ic(t){return t}function Pn(t,n){return n}var Qh=class{hasEmptyBlock;trackByFn;liveCollection;constructor(n,e,i){this.hasEmptyBlock=n,this.trackByFn=e,this.liveCollection=i}};function be(t,n,e,i,a,o,r,s,c,p,m,g,v){Si(`NgControlFlow`);let b=le(),_=Xe(),E=c!==void 0,P=le(),j=new Qh(E,s?r.bind(P[Qt][yt]):r);P[at+t]=j,_r(b,_,t+1,n,e,i,a,Cn(_.consts,o),256),E&&_r(b,_,t+2,c,p,m,g,Cn(_.consts,v),512)}var Xh=class extends qh{lContainer;hostLView;templateTNode;operationsCounter=void 0;needsIndexUpdate=!1;constructor(n,e,i){super(),this.lContainer=n,this.hostLView=e,this.templateTNode=i}get length(){return this.lContainer.length-_t}at(n){return this.getLView(n)[yt].$implicit}attach(n,e){let i=e[io];this.needsIndexUpdate||=n!==this.length,ec(this.lContainer,e,n,br(this.templateTNode,i)),a1(this.lContainer,n)}detach(n){return this.needsIndexUpdate||=n!==this.length-1,o1(this.lContainer,n),r1(this.lContainer,n)}create(n,e){let i=Dd(this.lContainer,this.templateTNode.tView.ssrId);return Js(this.hostLView,this.templateTNode,new Kh(this.lContainer,e,n),{dehydratedView:i})}destroy(n){zd(n[ge],n)}updateValue(n,e){this.getLView(n)[yt].$implicit=e}reset(){this.needsIndexUpdate=!1}updateIndexes(){if(this.needsIndexUpdate)for(let n=0;n<this.length;n++)this.getLView(n)[yt].$index=n}getLView(n){return s1(this.lContainer,n)}};function ve(t){let n=he(null),e=Xt();try{let i=le(),a=i[ge],o=i[e],r=e+1,s=Pd(i,r);if(o.liveCollection===void 0)o.liveCollection=new Xh(s,i,Zh(a,r));else o.liveCollection.reset();let c=o.liveCollection;if(n1(c,t,o.trackByFn,n),c.updateIndexes(),o.hasEmptyBlock){let p=ba(),m=c.length===0;if(mn(i,p,m)){let g=e+2,v=Pd(i,g);if(m){let b=Zh(a,g),_=_0(v,b,i);ec(v,Js(i,b,void 0,{dehydratedView:_}),0,br(b,_))}else a.firstUpdatePass&&$I(v),v0(v,0)}}}finally{he(n)}}function Pd(t,n){return t[n]}function a1(t,n){if(t.length<=_t)return;let i=t[_t+n];i&&Qk(i)}function o1(t,n){if(t.length<=_t)return;let i=t[_t+n];i&&Kk(i)}function r1(t,n){return Gs(t,n)}function s1(t,n){return b0(t,n)}function Zh(t,n){return ed(t,n)}function S(t,n,e){let i=le();if(mn(i,ba(),n)){Xe();gI(dr(),i,t,n,i[et],e)}return S}function Jh(t,n,e,i,a){$d(n,t,e,a?`class`:`style`,i)}function l(t,n,e,i){let a=le(),o=a[ge],r=t+at,s=o.firstCreatePass?Pf(r,a,2,n,kf,ad(),e,i):o.data[r];if(_i(s)){let c=a[Qn].tracingService;if(c&&c.componentCreate){let p=o.data[s.directiveStart+s.componentOffset];return c.componentCreate(S0(p),()=>(e_(t,n,a,s,i),l))}}return e_(t,n,a,s,i),l}function e_(t,n,e,i,a){if(If(i,e,t,n,W0),cr(i)){let o=e[ge];Ud(o,e,i),lf(o,i,e)}a!=null&&Zs(e,i)}function d(){let t=Xe(),e=Mf(It());return t.firstCreatePass&&Ff(t,e),qp(e)&&Yp(),Gp(),e.classesWithoutHost!=null&&FD(e)&&Jh(t,e,le(),e.classesWithoutHost,!0),e.stylesWithoutHost!=null&&LD(e)&&Jh(t,e,le(),e.stylesWithoutHost,!1),d}function z(t,n,e,i){return l(t,n,e,i),d(),z}function st(t,n,e,i){let a=le(),o=a[ge],r=t+at,s=o.firstCreatePass?pM(r,o,2,n,e,i):o.data[r];return If(s,a,t,n,W0),i!=null&&Zs(a,s),st}function pt(){return qp(Mf(It()))&&Yp(),Gp(),pt}function Wt(t,n,e,i){return st(t,n,e,i),pt(),Wt}var W0=(t,n,e,i,a)=>(Fs(!0),z_(n[et],i,nh()));function Yf(t,n,e){let i=le(),a=i[ge],o=t+at,r=a.firstCreatePass?Pf(o,i,8,`ng-container`,kf,ad(),n,e):a.data[o];if(If(r,i,t,`ng-container`,c1),cr(r)){let s=i[ge];Ud(s,i,r),lf(s,r,i)}return e!=null&&Zs(i,r),Yf}function Kf(){let t=Xe(),e=Mf(It());return t.firstCreatePass&&Ff(t,e),Kf}function wa(t,n,e){return Yf(t,n,e),Kf(),wa}var c1=(t,n,e,i,a)=>(Fs(!0),kk(n[et],``));function X(){return le()}function qt(t,n,e){let i=le();if(mn(i,ba(),n)){Xe();a0(dr(),i,t,n,i[et],e)}return qt}var Bs=void 0;function l1(t){let n=Math.floor(Math.abs(t)),e=t.toString().replace(/^[^.]*\.?/,``).length;return n===1&&e===0?1:5}var d1=[`en`,[[`a`,`p`],[`AM`,`PM`]],[[`AM`,`PM`]],[[`S`,`M`,`T`,`W`,`T`,`F`,`S`],[`Sun`,`Mon`,`Tue`,`Wed`,`Thu`,`Fri`,`Sat`],[`Sunday`,`Monday`,`Tuesday`,`Wednesday`,`Thursday`,`Friday`,`Saturday`],[`Su`,`Mo`,`Tu`,`We`,`Th`,`Fr`,`Sa`]],Bs,[[`J`,`F`,`M`,`A`,`M`,`J`,`J`,`A`,`S`,`O`,`N`,`D`],[`Jan`,`Feb`,`Mar`,`Apr`,`May`,`Jun`,`Jul`,`Aug`,`Sep`,`Oct`,`Nov`,`Dec`],[`January`,`February`,`March`,`April`,`May`,`June`,`July`,`August`,`September`,`October`,`November`,`December`]],Bs,[[`B`,`A`],[`BC`,`AD`],[`Before Christ`,`Anno Domini`]],0,[6,0],[`M/d/yy`,`MMM d, y`,`MMMM d, y`,`EEEE, MMMM d, y`],[`h:mm a`,`h:mm:ss a`,`h:mm:ss a z`,`h:mm:ss a zzzz`],[`{1}, {0}`,Bs,Bs,Bs],[`.`,`,`,`;`,`%`,`+`,`-`,`E`,`×`,`‰`,`∞`,`NaN`,`:`],[`#,##0.###`,`#,##0%`,`¤#,##0.00`,`#E0`],`USD`,`$`,`US Dollar`,{},`ltr`,l1];var fh=Object.create(null);function hn(t){let n=u1(t),e=t_(n);if(e)return e;let i=n.split(`-`)[0];if(e=t_(i),e)return e;if(i===`en`)return d1;throw new G(701,!1)}function t_(t){if(!(t in fh)){let n=vi.ng&&vi.ng.common&&vi.ng.common.locales&&vi.ng.common.locales[t];return n!==void 0&&(fh[t]=n),n}return fh[t]}var wt={LocaleId:0,DayPeriodsFormat:1,DayPeriodsStandalone:2,DaysFormat:3,DaysStandalone:4,MonthsFormat:5,MonthsStandalone:6,Eras:7,FirstDayOfWeek:8,WeekendRange:9,DateFormat:10,TimeFormat:11,DateTimeFormat:12,NumberSymbols:13,NumberFormats:14,CurrencyCode:15,CurrencySymbol:16,CurrencyName:17,Currencies:18,Directionality:19,PluralCase:20,ExtraData:21};function u1(t){return t.toLowerCase().replace(/_/g,`-`)}var ac=`en-US`;function q0(t){typeof t==`string`&&t.toLowerCase().replace(/_/g,`-`)}function C(t,n,e){let i=le(),a=Xe(),o=It();return p1(a,i,i[et],o,t,n,e),C}function Ir(t,n,e){let i=le(),a=Xe(),o=It();return(o.type&3||e)&&Of(o,a,i,e,i[et],t,n,fo(o,i,n)),Ir}function p1(t,n,e,i,a,o,r){let s=!0,c=null;if((i.type&3||r)&&(c??=fo(i,n,o),Of(i,t,n,r,e,a,o,c)&&(s=!1)),s){let p=i.outputs?.[a],m=i.hostDirectiveOutputs?.[a];if(m&&m.length)for(let g=0;g<m.length;g+=2){let v=m[g],b=m[g+1];c??=fo(i,n,o),Id(i,n,v,b,a,c)}if(p&&p.length)for(let g of p)c??=fo(i,n,o),Id(i,n,g,a,a,c)}}function y(t=1){return cy(t)}function h1(t,n){let e=null,i=Lk(t);for(let a=0;a<n.length;a++){let o=n[a];if(o===`*`){e=a;continue}if(i===null?W_(t,o,!0):jk(i,o))return a}return e}function Re(t){let n=le()[Qt][Ut];if(!n.projection){let i=n.projection=Pv(t?t.length:1,null),a=i.slice(),o=n.child;for(;o!==null;){if(o.type!==128){let r=t?h1(o,t):0;r!==null&&(a[r]?a[r].projectionNext=o:i[r]=o,a[r]=o)}o=o.next}}}function me(t,n=0,e,i,a,o){let r=le(),s=Xe(),c=i?t+1:null;c!==null&&_r(r,s,c,i,a,o,null,e);let p=yo(s,at+t,16,null,e||null);p.projection===null&&(p.projection=n),Xp();let g=!r[io]||Wp();r[Qt][Ut].projection[p.projection]===null&&c!==null?f1(r,s,c):g&&!Ld(p)&&rI(s,r,p)}function f1(t,n,e){let i=at+e,a=n.data[i],o=t[i],r=Dd(o,a.tView.ssrId);ec(o,Js(t,a,void 0,{dehydratedView:r}),0,br(a,r))}function Bt(t,n,e,i){return R0(t,n,e,i),Bt}function Ze(t,n,e){return O0(t,n,e),Ze}function Z(t){let n=le(),e=Xe(),i=sd();Ps(i+1);let a=Vf(e,i);if(t.dirty&&Wv(n)===((a.metadata.flags&2)===2)){if(a.matches===null)t.reset([]);else{let o=F0(n,i);t.reset(o,C_),t.notifyOnChanges()}return!0}return!1}function J(){return Lf(le(),sd())}function qd(t,n,e,i,a){return V0(n,R0(t,e,i,a)),qd}function Yd(t,n,e,i){return V0(t,O0(n,e,i)),Yd}function Kd(t=1){Ps(sd()+t)}function ht(t){return As(Zp(),at+t)}function gd(t,n){return t<<17|n<<2}function vo(t){return t>>17&32767}function g1(t){return(t&2)==2}function b1(t,n){return t&131071|n<<17}function ef(t){return t|2}function xr(t){return(t&131068)>>2}function gh(t,n){return t&-131069|n<<2}function v1(t){return(t&1)===1}function tf(t){return t|1}function y1(t,n,e,i,a,o){let r=o?n.classBindings:n.styleBindings,s=vo(r),c=xr(r);t[i]=e;let p=!1,m;if(Array.isArray(e)){let g=e;m=g[1],(m===null||nr(g,m)>0)&&(p=!0)}else m=e;if(a)if(c!==0){let v=vo(t[s+1]);t[i+1]=gd(v,s),v!==0&&(t[v+1]=gh(t[v+1],i)),t[s+1]=b1(t[s+1],i)}else t[i+1]=gd(s,0),s!==0&&(t[s+1]=gh(t[s+1],i)),s=i;else t[i+1]=gd(c,0),s===0?s=i:t[c+1]=gh(t[c+1],i),c=i;p&&(t[i+1]=ef(t[i+1])),n_(t,m,i,!0),n_(t,m,i,!1),_1(n,m,t,i,o),r=gd(s,c),o?n.classBindings=r:n.styleBindings=r}function _1(t,n,e,i,a){let o=a?t.residualClasses:t.residualStyles;o!=null&&typeof n==`string`&&nr(o,n)>=0&&(e[i+1]=tf(e[i+1]))}function n_(t,n,e,i){let a=t[e+1],o=n===null,r=i?vo(a):xr(a),s=!1;for(;r!==0&&(s===!1||o);){let c=t[r],p=t[r+1];x1(c,n)&&(s=!0,t[r+1]=i?tf(p):ef(p)),r=i?vo(p):xr(p)}s&&(t[e+1]=i?ef(a):tf(a))}function x1(t,n){return t===null||n==null||(Array.isArray(t)?t[1]:t)===n?!0:Array.isArray(t)&&typeof n==`string`?nr(t,n)>=0:!1}var Jn={textEnd:0,key:0,keyEnd:0,value:0,valueEnd:0};function C1(t){return t.substring(Jn.key,Jn.keyEnd)}function S1(t){return w1(t),Y0(t,K0(t,0,Jn.textEnd))}function Y0(t,n){let e=Jn.textEnd;return e===n?-1:(n=Jn.keyEnd=E1(t,Jn.key=n,e),K0(t,n,e))}function w1(t){Jn.key=0,Jn.keyEnd=0,Jn.value=0,Jn.valueEnd=0,Jn.textEnd=t.length}function K0(t,n,e){for(;n<e&&t.charCodeAt(n)<=32;)n++;return n}function E1(t,n,e){for(;n<e&&t.charCodeAt(n)>32;)n++;return n}function fn(t,n,e){return Q0(t,n,e,!1),fn}function q(t,n){return Q0(t,n,null,!0),q}function tt(t){k1(O1,D1,t,!0)}function D1(t,n){for(let e=S1(n);e>=0;e=Y0(n,e))Ql(t,C1(n),!0)}function Q0(t,n,e,i){let a=le(),o=Xe(),r=Hi(2);if(o.firstUpdatePass&&Z0(o,t,r,i),n!==mt&&mn(a,r,n)){let s=o.data[Xt()];J0(o,s,a,a[et],t,a[r+1]=P1(n,e),i,r)}}function k1(t,n,e,i){let a=Xe(),o=Hi(2);a.firstUpdatePass&&Z0(a,null,o,i);let r=le();if(e!==mt&&mn(r,o,e)){let s=a.data[Xt()];if(ex(s,i)&&!X0(a,o)){let c=i?s.classesWithoutHost:s.stylesWithoutHost;c!==null&&(e=$l(c,e||``)),Jh(a,s,r,e,i)}else R1(a,s,r,r[et],r[o+1],r[o+1]=A1(t,n,e),i,o)}}function X0(t,n){return n>=t.expandoStartIndex}function Z0(t,n,e,i){let a=t.data;if(a[e+1]===null){let o=a[Xt()],r=X0(t,e);ex(o,i)&&n===null&&!r&&(n=!1),n=I1(a,o,n,i),y1(a,o,n,e,r,i)}}function I1(t,n,e,i){let a=ay(t),o=i?n.residualClasses:n.residualStyles;if(a===null)(i?n.classBindings:n.styleBindings)===0&&(e=bh(null,t,n,e,i),e=qs(e,n.attrs,i),o=null);else{let r=n.directiveStylingLast;if(r===-1||t[r]!==a)if(e=bh(a,t,n,e,i),o===null){let c=M1(t,n,i);c!==void 0&&Array.isArray(c)&&(c=bh(null,t,n,c[1],i),c=qs(c,n.attrs,i),N1(t,n,i,c))}else o=T1(t,n,i)}return o!==void 0&&(i?n.residualClasses=o:n.residualStyles=o),e}function M1(t,n,e){let i=e?n.classBindings:n.styleBindings;if(xr(i)!==0)return t[vo(i)]}function N1(t,n,e,i){let a=e?n.classBindings:n.styleBindings;t[vo(a)]=i}function T1(t,n,e){let i,a=n.directiveEnd;for(let o=1+n.directiveStylingLast;o<a;o++){let r=t[o].hostAttrs;i=qs(i,r,e)}return qs(i,n.attrs,e)}function bh(t,n,e,i,a){let o=null,r=e.directiveEnd,s=e.directiveStylingLast;for(s===-1?s=e.directiveStart:s++;s<r&&(o=n[s],i=qs(i,o.hostAttrs,a),o!==t);)s++;return t!==null&&(e.directiveStylingLast=s),i}function qs(t,n,e){let i=e?1:2,a=-1;if(n!==null)for(let o=0;o<n.length;o++){let r=n[o];typeof r==`number`?a=r:a===i&&(Array.isArray(t)||(t=t===void 0?[]:[``,t]),Ql(t,r,e?!0:n[++o]))}return t===void 0?null:t}function A1(t,n,e){if(e==null||e===``)return Kt;let i=[],a=ti(e);if(Array.isArray(a))for(let o=0;o<a.length;o++)t(i,a[o],!0);else if(a instanceof Set)for(let o of a)t(i,o,!0);else if(typeof a==`object`)for(let o in a)Object.hasOwn(a,o)&&t(i,o,a[o]);else typeof a==`string`&&n(i,a);return i}function O1(t,n,e){let i=String(n);i!==``&&!i.includes(` `)&&Ql(t,i,e)}function R1(t,n,e,i,a,o,r,s){a===mt&&(a=Kt);let c=0,p=0,m=0<a.length?a[0]:null,g=0<o.length?o[0]:null;for(;m!==null||g!==null;){let v=c<a.length?a[c+1]:void 0,b=p<o.length?o[p+1]:void 0,_=null,E;m===g?(c+=2,p+=2,v!==b&&(_=g,E=b)):g===null||m!==null&&m<g?(c+=2,_=m):(p+=2,_=g,E=b),_!==null&&J0(t,n,e,i,_,E,r,s),m=c<a.length?a[c]:null,g=p<o.length?o[p]:null}}function J0(t,n,e,i,a,o,r,s){if(!(n.type&3))return;let c=t.data,p=c[s+1];if(!Fd(v1(p)?i_(c,n,e,a,xr(p),r):void 0)){Fd(o)||g1(p)&&(o=i_(c,null,e,a,s,r));cI(i,r,jp(Xt(),e),a,o)}}function i_(t,n,e,i,a,o){let r=n===null,s;for(;a>0;){let c=t[a],p=Array.isArray(c),m=p?c[1]:c,g=m===null,v=e[a+1];v===mt&&(v=g?Kt:void 0);let b=g?Xl(v,i):m===i?v:void 0;if(p&&!Fd(b)&&(b=Xl(c,i)),Fd(b)&&(s=b,r))return s;let _=t[a+1];a=r?vo(_):xr(_)}if(n!==null){let c=o?n.residualClasses:n.residualStyles;c!=null&&(s=Xl(c,i))}return s}function Fd(t){return t!==void 0}function P1(t,n){return t==null||t===``||(typeof n==`string`?t=ti(t)+n:typeof t==`object`&&(t=ws(ti(t)))),t}function ex(t,n){return(t.flags&(n?8:16))!==0}function u(t,n=``){let e=le(),i=Xe(),a=t+at,o=i.firstCreatePass?yo(i,a,1,n,null):i.data[a],r=F1(i,e,o,n);e[a]=r,dd()&&Cf(i,e,r,o),uo(o,!1)}var F1=(t,n,e,i)=>(Fs(!0),Ek(n[et],i));function L1(t,n,e,i=``){return mn(t,ba(),e)?n+$e(e)+i:mt}function V1(t,n,e,i,a,o=``){let s=vr(t,mo(),e,a);return Hi(2),s?n+$e(e)+i+$e(a)+o:mt}function B1(t,n,e,i,a,o,r,s=``){let p=w0(t,mo(),e,a,r);return Hi(3),p?n+$e(e)+i+$e(a)+o+$e(r)+s:mt}function j1(t,n,e,i,a,o,r,s,c,p=``){let g=Wd(t,mo(),e,a,r,c);return Hi(4),g?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(c)+p:mt}function z1(t,n,e,i,a,o,r,s,c,p,m,g=``){let v=mo(),b=Wd(t,v,e,a,r,c);return b=mn(t,v+4,m)||b,Hi(5),b?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(c)+p+$e(m)+g:mt}function H1(t,n,e,i,a,o,r,s,c,p,m,g,v,b=``){let _=mo(),E=Wd(t,_,e,a,r,c);return E=vr(t,_+4,m,v)||E,Hi(6),E?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(c)+p+$e(m)+g+$e(v)+b:mt}function U1(t,n,e,i,a,o,r,s,c,p,m,g,v,b,_,E=``){let P=mo(),ae=Wd(t,P,e,a,r,c);return ae=w0(t,P+4,m,v,_)||ae,Hi(7),ae?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(c)+p+$e(m)+g+$e(v)+b+$e(_)+E:mt}function w(t){return H(``,t),w}function H(t,n,e){let i=le(),a=L1(i,t,n,e);return a!==mt&&Co(i,Xt(),a),H}function Fe(t,n,e,i,a){let o=le(),r=V1(o,t,n,e,i,a);return r!==mt&&Co(o,Xt(),r),Fe}function jt(t,n,e,i,a,o,r){let s=le(),c=B1(s,t,n,e,i,a,o,r);return c!==mt&&Co(s,Xt(),c),jt}function ai(t,n,e,i,a,o,r,s,c){let p=le(),m=j1(p,t,n,e,i,a,o,r,s,c);return m!==mt&&Co(p,Xt(),m),ai}function oc(t,n,e,i,a,o,r,s,c,p,m){let g=le(),v=z1(g,t,n,e,i,a,o,r,s,c,p,m);return v!==mt&&Co(g,Xt(),v),oc}function Qd(t,n,e,i,a,o,r,s,c,p,m,g,v){let b=le(),_=H1(b,t,n,e,i,a,o,r,s,c,p,m,g,v);return _!==mt&&Co(b,Xt(),_),Qd}function Xd(t,n,e,i,a,o,r,s,c,p,m,g,v,b,_){let E=le(),P=U1(E,t,n,e,i,a,o,r,s,c,p,m,g,v,b,_);return P!==mt&&Co(E,Xt(),P),Xd}function Co(t,n,e){let i=jp(n,t);Dk(t[et],i,e)}var tx={};function Zd(t){Si(`NgLet`);let n=Xe(),e=le(),i=t+at;return uo(yo(n,i,128,null,null),!1),Os(n,e,i,tx),Zd}function Qf(t){return Os(Xe(),le(),Xt(),t),t}function Xf(t){let e=As(Zp(),at+t);if(e===tx)throw new G(314,!1);return e}function a_(t,n,e){let i=Xe();i.firstCreatePass&&nx(n,i.data,i.blueprint,Xn(t),e)}function nx(t,n,e,i,a){if(t=Pt(t),Array.isArray(t))for(let o=0;o<t.length;o++)nx(t[o],n,e,i,a);else{let o=Xe(),r=le(),s=It(),c=Ja(t)?t:Pt(t.provide),p=Rp(t),m=s.providerIndexes&1048575,g=s.directiveStart,v=s.providerIndexes>>20;if(Ja(t)||!t.multi){let b=new go(p,a,He,null),_=yh(c,n,a?m:m+v,g);_===-1?(Ch(Ed(s,r),o,c),vh(o,t,n.length),n.push(c),s.directiveStart++,s.directiveEnd++,a&&(s.providerIndexes+=1048576),e.push(b),r.push(b)):(e[_]=b,r[_]=b)}else{let b=yh(c,n,m+v,g),_=yh(c,n,m,m+v),E=b>=0&&e[b],P=_>=0&&e[_];if(a&&!P||!a&&!E){Ch(Ed(s,r),o,c);let ae=W1(a?G1:$1,e.length,a,i,p,t);!a&&P&&(e[_].providerFactory=ae),vh(o,t,n.length,0),n.push(c),s.directiveStart++,s.directiveEnd++,a&&(s.providerIndexes+=1048576),e.push(ae),r.push(ae)}else{let ae=ix(e[a?_:b],p,!a&&i);vh(o,t,b>-1?b:_,ae)}!a&&i&&P&&e[_].componentProviders++}}}function vh(t,n,e,i){let a=Ja(n),o=zv(n);if(a||o){let c=(o?Pt(n.useClass):n).prototype.ngOnDestroy;if(c){let p=t.destroyHooks||(t.destroyHooks=[]);if(!a&&n.multi){let m=p.indexOf(e);m===-1?p.push(e,[i,c]):p[m+1].push(i,c)}else p.push(e,c)}}}function ix(t,n,e){return e&&t.componentProviders++,t.multi.push(n)-1}function yh(t,n,e,i){for(let a=e;a<i;a++)if(n[a]===t)return a;return-1}function $1(t,n,e,i,a){return nf(this.multi,[])}function G1(t,n,e,i,a){let o=this.multi,r;if(this.providerFactory){let s=this.providerFactory.componentProviders,c=Hs(i,i[ge],this.providerFactory.index,a);r=c.slice(0,s),nf(o,r);for(let p=s;p<c.length;p++)r.push(c[p])}else r=[],nf(o,r);return r}function nf(t,n){for(let e=0;e<t.length;e++){let i=t[e];n.push(i())}return n}function W1(t,n,e,i,a,o){let r=new go(t,e,He,null);return r.multi=[],r.index=n,r.componentProviders=0,ix(r,a,i&&!e),r}function Ge(t,n){return e=>{e.providersResolver=(i,a)=>a_(i,a?a(t):t,!1),n&&(e.viewProvidersResolver=(i,a)=>a_(i,a?a(n):n,!0))}}function vt(t,n){let e=od()+t,i=le();return i[e]===mt?Af(i,e,n()):KI(i,e)}function ax(t,n){let e=t[n];return e===mt?void 0:e}function q1(t,n,e,i,a,o){let r=n+e;return mn(t,r,a)?Af(t,r+1,o?i.call(o,a):i(a)):ax(t,r+1)}function Y1(t,n,e,i,a,o,r){let s=n+e;return vr(t,s,a,o)?Af(t,s+2,r?i.call(r,a,o):i(a,o)):ax(t,s+2)}function U(t,n){let e=Xe(),i,a=t+at;e.firstCreatePass?(i=K1(n,e.pipeRegistry),e.data[a]=i,i.onDestroy&&(e.destroyHooks??=[]).push(a,i.onDestroy)):i=e.data[a];let o=i.factory||(i.factory=ma(i.type,!0)),s=on(He);try{let c=wd(!1),p=o();return wd(c),Os(e,le(),a,p),p}finally{on(s)}}function K1(t,n){if(n)for(let e=n.length-1;e>=0;e--){let i=n[e];if(t===i.name)return i}}function _e(t,n,e){let i=t+at,a=le(),o=As(a,i);return ox(a,i)?q1(a,od(),n,o.transform,e,o):o.transform(e)}function Ie(t,n,e,i){let a=t+at,o=le(),r=As(o,a);return ox(o,a)?Y1(o,od(),n,r.transform,e,i,r):r.transform(e,i)}function ox(t,n){return t[ge].data[n].pure}function rc(t,n){return Gd(t,n)}var rx=(()=>{class t{applicationErrorHandler=f(Ui);appRef=f(Rn);taskService=f(po);ngZone=f(Q);zonelessEnabled=f(Vs);tracing=f(ii,{optional:!0});zoneIsDefined=typeof Zone<`u`&&!!Zone.root.run;schedulerTickApplyArgs=[{data:{__scheduler_tick__:!0}}];subscriptions=new De;angularZoneId=this.zoneIsDefined?this.ngZone._inner?.get(Cs):null;scheduleInRootZone=!this.zonelessEnabled&&this.zoneIsDefined&&(f(ch,{optional:!0})??!1);cancelScheduledCallback=null;useMicrotaskScheduler=!1;runningTick=!1;pendingRenderTaskId=null;constructor(){this.subscriptions.add(this.appRef.afterTick.subscribe(()=>{let e=this.taskService.add();if(!this.runningTick&&(this.cleanup(),!this.zonelessEnabled||this.appRef.includeAllTestViews)){this.taskService.remove(e);return}this.switchToMicrotaskScheduler(),this.taskService.remove(e)})),this.subscriptions.add(this.ngZone.onUnstable.subscribe(()=>{this.runningTick||this.cleanup()}))}switchToMicrotaskScheduler(){this.ngZone.runOutsideAngular(()=>{let e=this.taskService.add();this.useMicrotaskScheduler=!0,queueMicrotask(()=>{this.useMicrotaskScheduler=!1,this.taskService.remove(e)})})}notify(e){if(!this.zonelessEnabled&&e===5)return;switch(e){case 0:case 2:this.appRef.dirtyFlags|=2;break;case 3:case 4:case 5:case 1:this.appRef.dirtyFlags|=4;break;case 6:this.appRef.dirtyFlags|=2;break;case 12:this.appRef.dirtyFlags|=16;break;case 13:this.appRef.dirtyFlags|=2;break;case 11:break;default:this.appRef.dirtyFlags|=8}if(this.appRef.tracingSnapshot=this.tracing?.snapshot(this.appRef.tracingSnapshot)??null,!this.shouldScheduleTick())return;let i=this.useMicrotaskScheduler?my:ah;this.pendingRenderTaskId=this.taskService.add(),this.scheduleInRootZone?this.cancelScheduledCallback=Zone.root.run(()=>i(()=>this.tick())):this.cancelScheduledCallback=this.ngZone.runOutsideAngular(()=>i(()=>this.tick()))}shouldScheduleTick(){return!(this.appRef.destroyed||this.pendingRenderTaskId!==null||this.runningTick||this.appRef._runningTick||!this.zonelessEnabled&&this.zoneIsDefined&&Zone.current.get(Cs+this.angularZoneId))}tick(){if(this.runningTick||this.appRef.destroyed)return;if(this.appRef.dirtyFlags===0){this.cleanup();return}!this.zonelessEnabled&&this.appRef.dirtyFlags&7&&(this.appRef.dirtyFlags|=1);let e=this.taskService.add();try{this.ngZone.run(()=>{this.runningTick=!0,this.appRef._tick()},void 0,this.schedulerTickApplyArgs)}catch(i){this.applicationErrorHandler(i)}finally{this.taskService.remove(e),this.cleanup()}}ngOnDestroy(){this.subscriptions.unsubscribe(),this.cleanup()}cleanup(){if(this.runningTick=!1,this.cancelScheduledCallback?.(),this.cancelScheduledCallback=null,this.pendingRenderTaskId!==null){let e=this.pendingRenderTaskId;this.pendingRenderTaskId=null,this.taskService.remove(e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function Zf(){return Si(`NgZoneless`),rn([...Jf(),[]])}function Jf(){return[{provide:bi,useExisting:rx},{provide:Q,useClass:Ss},{provide:Vs,useValue:!0}]}function Q1(){return typeof $localize<`u`&&$localize.locale||ac}var Mr=new I(``,{factory:()=>f(Mr,{optional:!0,skipSelf:!0})||Q1()});var sc=class{destroyed=!1;listeners=null;errorHandler=f(Nn,{optional:!0});isEmitting=!1;hasNullListeners=!1;destroyRef=f(Te);constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.listeners=null})}subscribe(n){if(this.destroyed)throw new G(953,!1);return(this.listeners??=[]).push(n),{unsubscribe:()=>{let e=this.listeners?this.listeners.indexOf(n):-1;e>-1&&(this.isEmitting?(this.hasNullListeners=!0,this.listeners[e]=null):this.listeners.splice(e,1))}}}emit(n){if(this.destroyed){console.warn(Bi(953,!1));return}if(this.listeners===null)return;this.isEmitting=!0;let e=he(null);try{for(let i of this.listeners)try{i!==null&&i(n)}catch(a){this.errorHandler?.handleError(a)}}finally{this.hasNullListeners&&(this.hasNullListeners=!1,this.listeners&&X1(this.listeners)),he(e),this.isEmitting=!1}}};function X1(t){let n=t.length-1;for(;n>-1;)t[n]===null&&t.splice(n,1),n--}function ne(t,n){return ps(t,n?.equal)}function nt(t){return Yb(t)}(class t extends Error{_brand;constructor(n){super(n)}static IDLE=new t(`IDLE`);static LOADING=new t(`LOADING`)});function hx(t,n){let e=Object.create(af);e.value=t,e.transformFn=n?.transform;function i(){if(Oi(e),e.value===Cr)throw new G(-950,null);return e.value}return i[Ct]=e,i}var Ki=class{attributeName;constructor(n){this.attributeName=n}__NG_ELEMENT_ID__=()=>rf(this.attributeName);toString(){return`HostAttributeToken ${this.attributeName}`}};var fx=(()=>{let t=new I(``);return t.__NG_ELEMENT_ID__=n=>{let e=It();if(e===null)throw new G(-204,!1);if(e.type&2)return e.value;if(n&8)return null;throw new G(-204,!1)},t})();function eu(t){return new sc}function cx(t,n){return hx(t,n)}function uN(t){return hx(Cr,t)}var zt=(cx.required=uN,cx);function gx(t,n){let e=Object.create(af),i=new sc;e.value=t;function a(){return Oi(e),lx(e.value),e.value}return a[Ct]=e,a.asReadonly=ud.bind(a),a.set=o=>{e.equal(e.value,o)||(ua(e,o),i.emit(o))},a.update=o=>{lx(e.value),a.set(o(e.value))},a.subscribe=i.subscribe.bind(i),a.destroyRef=i.destroyRef,a}function lx(t){if(t===Cr)throw new G(952,!1)}function dx(t,n){return gx(t,n)}function mN(t){return gx(Cr,t)}var bx=(dx.required=mN,dx);function ux(t,n){return Bf(n)}function pN(t,n){return jf(n)}var lc=(ux.required=pN,ux);function mx(t,n){return Bf(n)}function hN(t,n){return jf(n)}var vx=(mx.required=hN,mx);var Pe=(()=>{class t{static __NG_ELEMENT_ID__=gN}return t})();function gN(t){return bN(It(),le(),(t&16)===16)}function bN(t,n,e){if(_i(t)&&!e){let i=On(t.index,n);return new Ca(i,i)}else if(t.type&175){let i=n[Qt];return new Ca(i,n)}return null}var tg=new I(``);var vN=new I(``);function cc(t){return!t.moduleRef}function yN(t){let n=cc(t)?t.r3Injector:t.moduleRef.injector,e=n.get(Q);return e.run(()=>{cc(t)?t.r3Injector.resolveInjectorInitializers():t.moduleRef.resolveInjectorInitializers();let i=n.get(Ui),a;if(e.runOutsideAngular(()=>{a=e.onError.subscribe({next:i})}),cc(t)){let o=()=>n.destroy(),r=t.platformInjector.get(tg);r.add(o),n.onDestroy(()=>{a.unsubscribe(),r.delete(o)})}else{let o=()=>t.moduleRef.destroy(),r=t.platformInjector.get(tg);r.add(o),t.moduleRef.onDestroy(()=>{zs(t.allPlatformModules,t.moduleRef),a.unsubscribe(),r.delete(o)})}return xN(i,e,()=>{let o=n.get(po),r=o.add(),s=n.get(Uf);return s.runInitializers(),s.donePromise.then(()=>{if(q0(n.get(Mr,ac)||ac),!n.get(vN,!0))return cc(t)?n.get(Rn):(t.allPlatformModules.push(t.moduleRef),t.moduleRef);if(cc(t)){let m=n.get(Rn);return t.rootComponent!==void 0&&m.bootstrap(t.rootComponent),m}else return _N?.(t.moduleRef,t.allPlatformModules),t.moduleRef}).finally(()=>{o.remove(r)})})})}var _N;function xN(t,n,e){try{let i=e();return xo(i)?i.catch(a=>{throw n.runOutsideAngular(()=>t(a)),a}):i}catch(i){throw n.runOutsideAngular(()=>t(i)),i}}var Jd=null;function CN(t=[],n){return re.create({name:n,providers:[{provide:Ms,useValue:`platform`},{provide:tg,useValue:new Set([()=>Jd=null])},...t]})}function SN(t=[]){if(Jd)return Jd;let n=CN(t);return Jd=n,G0(),wN(n),n}function wN(t){let n=t.get(md,null);ar(t,()=>{n?.forEach(e=>e())})}function yx(t){let{rootComponent:n,appProviders:e,platformProviders:i,platformRef:a}=t;Ke(ze.BootstrapApplicationStart);try{let o=a?.injector??SN(i);return yN({r3Injector:new Ws({providers:[Jf(),gy,...e||[]],parent:o,debugName:``,runEnvironmentInitializers:!1}).injector,platformInjector:o,rootComponent:n})}catch(o){return Promise.reject(o)}finally{Ke(ze.BootstrapApplicationEnd)}}function V(t){return typeof t==`boolean`?t:t!=null&&t!==`false`}function ct(t,n=NaN){return!isNaN(parseFloat(t))&&!isNaN(Number(t))?Number(t):n}var eg=Symbol(`NOT_SET`);var _x=new Set;var EN=s(r({},Go),{kind:`afterRenderEffectPhase`,consumerIsAlwaysLive:!0,consumerAllowSignalWrites:!0,value:eg,cleanup:null,consumerMarkedDirty(){if(this.sequence.impl.executing){if(this.sequence.lastPhase===null||this.sequence.lastPhase<this.phase)return;this.sequence.erroredOrDestroyed=!0}this.sequence.scheduler.notify(7)},phaseFn(t){if(this.sequence.lastPhase=this.phase,!this.dirty)return this.signal;if(this.dirty=!1,this.value!==eg&&!Uo(this))return this.signal;try{for(let a of this.cleanup??_x)a()}finally{this.cleanup?.clear()}let n=[];t!==void 0&&n.push(t),n.push(this.registerCleanupFn);let e=la(this),i;try{i=this.userFn.apply(null,n)}finally{$a(this,e)}return(this.value===eg||!this.equal(this.value,i))&&(this.value=i,this.version++),this.signal}});var ng=class extends Us{scheduler;lastPhase=null;nodes=[void 0,void 0,void 0,void 0];onDestroyFns=null;constructor(n,e,i,a,o,r=null,s){super(n,[void 0,void 0,void 0,void 0],i,!1,o.get(Te),r),this.scheduler=a;for(let c of yf){let p=e[c];if(p===void 0)continue;let m=Object.create(EN);m.sequence=this,m.phase=c,m.userFn=p,m.dirty=!0,m.signal=()=>(Oi(m),m.value),m.signal[Ct]=m,m.registerCleanupFn=g=>(m.cleanup??=new Set).add(g),this.nodes[c]=m,this.hooks[c]=g=>m.phaseFn(g)}}afterRun(){super.afterRun(),this.lastPhase=null}destroy(){if(this.onDestroyFns!==null)for(let n of this.onDestroyFns)n();super.destroy();for(let n of this.nodes)if(n)try{for(let e of n.cleanup??_x)e()}finally{da(n)}}};function ig(t,n){let e=n?.injector??f(re),i=e.get(bi),a=e.get(jd),o=e.get(ii,null,{optional:!0});a.impl??=e.get(_f);let r=t;typeof r==`function`&&(r={mixedReadWrite:t});let s=e.get(ur,null,{optional:!0}),c=new ng(a.impl,[r.earlyRead,r.write,r.mixedReadWrite,r.read],s?.view,i,e,o?.snapshot(null),void 0);return a.impl.register(c),c}function tu(t,n){let e=pa(t),i=n.elementInjector||ir(),o=new yr(e).create(i,n.projectableNodes,n.hostElement,n.environmentInjector,n.directives,n.bindings);return n.onError&&(o.hostView._lView[sr]=n.onError),o}var xx=null;function Fn(){return xx}function ag(t){xx??=t}var dc=class{};var Nr=(()=>{class t{historyGo(e){throw new Error(``)}static ɵfac=function(i){return new(i||t)};static ɵprov=fe({token:t,factory:()=>f(Cx),providedIn:`platform`})}return t})();var Cx=(()=>{class t extends Nr{_location;_history;_doc=f(de);constructor(){super(),this._location=window.location,this._history=window.history}getBaseHrefFromDOM(){return Fn().getBaseHref(this._doc)}onPopState(e){let i=Fn().getGlobalEventTarget(this._doc,`window`);return i.addEventListener(`popstate`,e,!1),()=>i.removeEventListener(`popstate`,e)}onHashChange(e){let i=Fn().getGlobalEventTarget(this._doc,`window`);return i.addEventListener(`hashchange`,e,!1),()=>i.removeEventListener(`hashchange`,e)}get href(){return this._location.href}get protocol(){return this._location.protocol}get hostname(){return this._location.hostname}get port(){return this._location.port}get pathname(){return this._location.pathname}get search(){return this._location.search}get hash(){return this._location.hash}set pathname(e){this._location.pathname=e}pushState(e,i,a){this._history.pushState(e,i,a)}replaceState(e,i,a){this._history.replaceState(e,i,a)}forward(){this._history.forward()}back(){this._history.back()}historyGo(e=0){this._history.go(e)}getState(){return this._history.state}static ɵfac=function(i){return new(i||t)};static ɵprov=fe({token:t,factory:()=>new t,providedIn:`platform`})}return t})();function Ex(t,n){return t?n?t.endsWith(`/`)?n.startsWith(`/`)?t+n.slice(1):t+n:n.startsWith(`/`)?t+n:`${t}/${n}`:t:n}function Sx(t){let n=t.search(/#|\?|$/);return t[n-1]===`/`?t.slice(0,n-1)+t.slice(n):t}function Ea(t){return t&&t[0]!==`?`?`?${t}`:t}var nu=(()=>{class t{historyGo(e){throw new Error(``)}static ɵfac=function(i){return new(i||t)};static ɵprov=fe({token:t,factory:()=>f(kN),providedIn:`root`})}return t})();var DN=new I(``);var kN=(()=>{class t extends nu{_platformLocation;_baseHref;_removeListenerFns=[];constructor(e,i){super(),this._platformLocation=e,this._baseHref=i??this._platformLocation.getBaseHrefFromDOM()??f(de).location?.origin??``}ngOnDestroy(){for(;this._removeListenerFns.length;)this._removeListenerFns.pop()()}onPopState(e){this._removeListenerFns.push(this._platformLocation.onPopState(e),this._platformLocation.onHashChange(e))}getBaseHref(){return this._baseHref}prepareExternalUrl(e){return Ex(this._baseHref,e)}path(e=!1){let i=this._platformLocation.pathname+Ea(this._platformLocation.search),a=this._platformLocation.hash;return a&&e?`${i}${a}`:i}pushState(e,i,a,o){let r=this.prepareExternalUrl(a+Ea(o));this._platformLocation.pushState(e,i,r)}replaceState(e,i,a,o){let r=this.prepareExternalUrl(a+Ea(o));this._platformLocation.replaceState(e,i,r)}forward(){this._platformLocation.forward()}back(){this._platformLocation.back()}getState(){return this._platformLocation.getState()}historyGo(e=0){this._platformLocation.historyGo?.(e)}static ɵfac=function(i){return new(i||t)(Se(Nr),Se(DN,8))};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();var iu=(()=>{class t{_subject=new L;_basePath;_locationStrategy;_urlChangeListeners=[];_urlChangeSubscription=null;constructor(e){this._locationStrategy=e;let i=this._locationStrategy.getBaseHref();this._basePath=NN(Sx(wx(i))),this._locationStrategy.onPopState(a=>{let o={url:this.path(!0),pop:!0,state:a.state,type:a.type};a.hasUAVisualTransition&&(o.hasUAVisualTransition=!0),this._subject.next(o)})}ngOnDestroy(){this._urlChangeSubscription?.unsubscribe(),this._urlChangeListeners=[]}path(e=!1){return this.normalize(this._locationStrategy.path(e))}getState(){return this._locationStrategy.getState()}isCurrentPathEqualTo(e,i=``){return this.path()==this.normalize(e+Ea(i))}normalize(e){return t.stripTrailingSlash(MN(this._basePath,wx(e)))}prepareExternalUrl(e){return e&&e[0]!==`/`&&(e=`/`+e),this._locationStrategy.prepareExternalUrl(e)}go(e,i=``,a=null){this._locationStrategy.pushState(a,``,e,i),this._notifyUrlChangeListeners(this.prepareExternalUrl(e+Ea(i)),a)}replaceState(e,i=``,a=null){this._locationStrategy.replaceState(a,``,e,i),this._notifyUrlChangeListeners(this.prepareExternalUrl(e+Ea(i)),a)}forward(){this._locationStrategy.forward()}back(){this._locationStrategy.back()}historyGo(e=0){this._locationStrategy.historyGo?.(e)}onUrlChange(e){return this._urlChangeListeners.push(e),this._urlChangeSubscription??=this.subscribe(i=>{this._notifyUrlChangeListeners(i.url,i.state)}),()=>{let i=this._urlChangeListeners.indexOf(e);this._urlChangeListeners.splice(i,1),this._urlChangeListeners.length===0&&(this._urlChangeSubscription?.unsubscribe(),this._urlChangeSubscription=null)}}_notifyUrlChangeListeners(e=``,i){this._urlChangeListeners.forEach(a=>a(e,i))}subscribe(e,i,a){return this._subject.subscribe({next:e,error:i??void 0,complete:a??void 0})}static normalizeQueryParams=Ea;static joinWithSlash=Ex;static stripTrailingSlash=Sx;static ɵfac=function(i){return new(i||t)(Se(nu))};static ɵprov=fe({token:t,factory:()=>IN(),providedIn:`root`})}return t})();function IN(){return new iu(Se(nu))}function MN(t,n){if(!t||!n.startsWith(t))return n;let e=n.substring(t.length);return e===``||[`/`,`;`,`?`,`#`].includes(e[0])?e:n}function wx(t){return t.replace(/\/index\.html$/,``)}function NN(t){if(new RegExp(`^(https?:)?//`).test(t)){let[,e]=t.split(/\/\/[^\/]+/);return e}return t}var dg=(function(t){return t[t.Decimal=0]=`Decimal`,t[t.Percent=1]=`Percent`,t[t.Currency=2]=`Currency`,t[t.Scientific=3]=`Scientific`,t})(dg||{});var Zt=(function(t){return t[t.Format=0]=`Format`,t[t.Standalone=1]=`Standalone`,t})(Zt||{});var it=(function(t){return t[t.Narrow=0]=`Narrow`,t[t.Abbreviated=1]=`Abbreviated`,t[t.Wide=2]=`Wide`,t[t.Short=3]=`Short`,t})(it||{});var gn=(function(t){return t[t.Short=0]=`Short`,t[t.Medium=1]=`Medium`,t[t.Long=2]=`Long`,t[t.Full=3]=`Full`,t})(gn||{});var cn={Decimal:0,Group:1,List:2,PercentSign:3,PlusSign:4,MinusSign:5,Exponential:6,SuperscriptingExponent:7,PerMille:8,Infinity:9,NaN:10,TimeSeparator:11,CurrencyDecimal:12,CurrencyGroup:13};function Ix(t){return hn(t)[wt.LocaleId]}function Mx(t,n,e){let i=hn(t);return Ln(Ln([i[wt.DayPeriodsFormat],i[wt.DayPeriodsStandalone]],n),e)}function Nx(t,n,e){let i=hn(t);return Ln(Ln([i[wt.DaysFormat],i[wt.DaysStandalone]],n),e)}function Tx(t,n,e){let i=hn(t);return Ln(Ln([i[wt.MonthsFormat],i[wt.MonthsStandalone]],n),e)}function Ax(t,n){let i=hn(t)[wt.Eras];return Ln(i,n)}function uc(t,n){return Ln(hn(t)[wt.DateFormat],n)}function mc(t,n){return Ln(hn(t)[wt.TimeFormat],n)}function pc(t,n){let i=hn(t)[wt.DateTimeFormat];return Ln(i,n)}function wi(t,n){let e=hn(t),i=e[wt.NumberSymbols][n];if(typeof i>`u`){if(n===cn.CurrencyDecimal)return e[wt.NumberSymbols][cn.Decimal];if(n===cn.CurrencyGroup)return e[wt.NumberSymbols][cn.Group]}return i}function Ox(t,n){return hn(t)[wt.NumberFormats][n]}function Rx(t){if(!t[wt.ExtraData])throw new G(2303,!1)}function Px(t){let n=hn(t);return Rx(n),(n[wt.ExtraData][2]||[]).map(i=>typeof i==`string`?og(i):[og(i[0]),og(i[1])])}function Fx(t,n,e){let i=hn(t);Rx(i);return Ln(Ln([i[wt.ExtraData][0],i[wt.ExtraData][1]],n)||[],e)||[]}function Ln(t,n){for(let e=n;e>-1;e--)if(typeof t[e]<`u`)return t[e];throw new G(2304,!1)}function og(t){let[n,e]=t.split(`:`);return{hours:+n,minutes:+e}}var AN=/^(\d{4,})-?(\d\d)-?(\d\d)(?:T(\d\d)(?::?(\d\d)(?::?(\d\d)(?:\.(\d+))?)?)?(Z|([+-])(\d\d):?(\d\d))?)?$/;var au=Object.create(null);var ON=/((?:[^BEGHLMOSWYZabcdhmswyz']+)|(?:'(?:[^']|'')*')|(?:G{1,5}|y{1,4}|Y{1,4}|M{1,5}|L{1,5}|w{1,2}|W{1}|d{1,2}|E{1,6}|c{1,6}|a{1,5}|b{1,5}|B{1,5}|h{1,2}|H{1,2}|m{1,2}|s{1,2}|S{1,3}|z{1,4}|Z{1,5}|O{1,4}))([\s\S]*)/;var RN=256;function Lx(t,n,e,i){let a=$N(t);PN(n),n=Qi(e,n)||n;let r=[],s;for(;n;)if(s=ON.exec(n),s){r=r.concat(s.slice(1));let m=r.pop();if(!m)break;n=m}else{r.push(n);break}let c=a.getTimezoneOffset();i&&(c=Bx(i,c),a=UN(a,i));let p=``;return r.forEach(m=>{let g=zN(m);p+=g?g(a,e,c):m===`''`?`'`:m.replace(/(^'|'$)/g,``).replace(/''/g,`'`)}),p}function PN(t){if(t.length>RN)throw new G(2300,!1)}function lu(t,n,e){let i=new Date(0);return i.setFullYear(t,n,e),i.setHours(0,0,0),i}function Qi(t,n){let e=Ix(t);if(au[e]??=Object.create(null),au[e][n])return au[e][n];let i=``;switch(n){case`shortDate`:i=uc(t,gn.Short);break;case`mediumDate`:i=uc(t,gn.Medium);break;case`longDate`:i=uc(t,gn.Long);break;case`fullDate`:i=uc(t,gn.Full);break;case`shortTime`:i=mc(t,gn.Short);break;case`mediumTime`:i=mc(t,gn.Medium);break;case`longTime`:i=mc(t,gn.Long);break;case`fullTime`:i=mc(t,gn.Full);break;case`short`:let a=Qi(t,`shortTime`),o=Qi(t,`shortDate`);i=ou(pc(t,gn.Short),[a,o]);break;case`medium`:let r=Qi(t,`mediumTime`),s=Qi(t,`mediumDate`);i=ou(pc(t,gn.Medium),[r,s]);break;case`long`:let c=Qi(t,`longTime`),p=Qi(t,`longDate`);i=ou(pc(t,gn.Long),[c,p]);break;case`full`:let m=Qi(t,`fullTime`),g=Qi(t,`fullDate`);i=ou(pc(t,gn.Full),[m,g])}return i&&(au[e][n]=i),i}function ou(t,n){return n&&(t=t.replace(/\{([^}]+)}/g,function(e,i){return Object.hasOwn(n,i)?n[i]:e})),t}function ri(t,n,e=`-`,i,a){let o=``;(t<0||a&&t<=0)&&(a?t=-t+1:(t=-t,o=e));let r=String(t);for(;r.length<n;)r=`0`+r;return i&&(r=r.slice(r.length-n)),o+r}function FN(t,n){return ri(t,3).substring(0,n)}function At(t,n,e=0,i=!1,a=!1){return function(o,r){let s=LN(t,o);if((e>0||s>-e)&&(s+=e),t===3)s===0&&e===-12&&(s=12);else if(t===6)return FN(s,n);let c=wi(r,cn.MinusSign);return ri(s,n,c,i,a)}}function LN(t,n){switch(t){case 0:return n.getFullYear();case 1:return n.getMonth();case 2:return n.getDate();case 3:return n.getHours();case 4:return n.getMinutes();case 5:return n.getSeconds();case 6:return n.getMilliseconds();case 7:return n.getDay();default:throw new G(2301,!1)}}function lt(t,n,e=Zt.Format,i=!1){return function(a,o){return VN(a,o,t,n,e,i)}}function VN(t,n,e,i,a,o){switch(e){case 2:return Tx(n,a,i)[t.getMonth()];case 1:return Nx(n,a,i)[t.getDay()];case 0:let r=t.getHours(),s=t.getMinutes();if(o){let p=Px(n),m=Fx(n,a,i),g=p.findIndex(v=>{if(Array.isArray(v)){let[b,_]=v,E=r>=b.hours&&s>=b.minutes,P=r<_.hours||r===_.hours&&s<_.minutes;if(b.hours<_.hours){if(E&&P)return!0}else if(E||P)return!0}else if(v.hours===r&&v.minutes===s)return!0;return!1});if(g!==-1)return m[g]}return Mx(n,a,i)[r<12?0:1];case 3:return Ax(n,i)[t.getFullYear()<=0?0:1];default:throw new G(2302,!1)}}function ru(t){return function(n,e,i){let a=-1*i,o=wi(e,cn.MinusSign),r=a>0?Math.floor(a/60):Math.ceil(a/60);switch(t){case 0:return(a>=0?`+`:``)+ri(r,2,o)+ri(Math.abs(a%60),2,o);case 1:return`GMT`+(a>=0?`+`:``)+ri(r,1,o);case 2:return`GMT`+(a>=0?`+`:``)+ri(r,2,o)+`:`+ri(Math.abs(a%60),2,o);case 3:return i===0?`Z`:(a>=0?`+`:``)+ri(r,2,o)+`:`+ri(Math.abs(a%60),2,o);default:throw new G(2310,!1)}}}var BN=0;var cu=4;function jN(t){let n=lu(t,BN,1).getDay();return lu(t,0,1+(n<=cu?cu:cu+7)-n)}function Vx(t){let n=t.getDay(),e=n===0?-3:cu-n;return lu(t.getFullYear(),t.getMonth(),t.getDate()+e)}function rg(t,n=!1){return function(e,i){let a;if(n){let o=new Date(e.getFullYear(),e.getMonth(),1).getDay()-1,r=e.getDate();a=1+Math.floor((r+o)/7)}else{let o=Vx(e),r=jN(o.getFullYear()),s=o.getTime()-r.getTime();a=1+Math.round(s/6048e5)}return ri(a,t,wi(i,cn.MinusSign))}}function su(t,n=!1){return function(e,i){return ri(Vx(e).getFullYear(),t,wi(i,cn.MinusSign),n)}}var sg=Object.create(null);function zN(t){if(sg[t])return sg[t];let n;switch(t){case`G`:case`GG`:case`GGG`:n=lt(3,it.Abbreviated);break;case`GGGG`:n=lt(3,it.Wide);break;case`GGGGG`:n=lt(3,it.Narrow);break;case`y`:n=At(0,1,0,!1,!0);break;case`yy`:n=At(0,2,0,!0,!0);break;case`yyy`:n=At(0,3,0,!1,!0);break;case`yyyy`:n=At(0,4,0,!1,!0);break;case`Y`:n=su(1);break;case`YY`:n=su(2,!0);break;case`YYY`:n=su(3);break;case`YYYY`:n=su(4);break;case`M`:case`L`:n=At(1,1,1);break;case`MM`:case`LL`:n=At(1,2,1);break;case`MMM`:n=lt(2,it.Abbreviated);break;case`MMMM`:n=lt(2,it.Wide);break;case`MMMMM`:n=lt(2,it.Narrow);break;case`LLL`:n=lt(2,it.Abbreviated,Zt.Standalone);break;case`LLLL`:n=lt(2,it.Wide,Zt.Standalone);break;case`LLLLL`:n=lt(2,it.Narrow,Zt.Standalone);break;case`w`:n=rg(1);break;case`ww`:n=rg(2);break;case`W`:n=rg(1,!0);break;case`d`:n=At(2,1);break;case`dd`:n=At(2,2);break;case`c`:case`cc`:n=At(7,1);break;case`ccc`:n=lt(1,it.Abbreviated,Zt.Standalone);break;case`cccc`:n=lt(1,it.Wide,Zt.Standalone);break;case`ccccc`:n=lt(1,it.Narrow,Zt.Standalone);break;case`cccccc`:n=lt(1,it.Short,Zt.Standalone);break;case`E`:case`EE`:case`EEE`:n=lt(1,it.Abbreviated);break;case`EEEE`:n=lt(1,it.Wide);break;case`EEEEE`:n=lt(1,it.Narrow);break;case`EEEEEE`:n=lt(1,it.Short);break;case`a`:case`aa`:case`aaa`:n=lt(0,it.Abbreviated);break;case`aaaa`:n=lt(0,it.Wide);break;case`aaaaa`:n=lt(0,it.Narrow);break;case`b`:case`bb`:case`bbb`:n=lt(0,it.Abbreviated,Zt.Standalone,!0);break;case`bbbb`:n=lt(0,it.Wide,Zt.Standalone,!0);break;case`bbbbb`:n=lt(0,it.Narrow,Zt.Standalone,!0);break;case`B`:case`BB`:case`BBB`:n=lt(0,it.Abbreviated,Zt.Format,!0);break;case`BBBB`:n=lt(0,it.Wide,Zt.Format,!0);break;case`BBBBB`:n=lt(0,it.Narrow,Zt.Format,!0);break;case`h`:n=At(3,1,-12);break;case`hh`:n=At(3,2,-12);break;case`H`:n=At(3,1);break;case`HH`:n=At(3,2);break;case`m`:n=At(4,1);break;case`mm`:n=At(4,2);break;case`s`:n=At(5,1);break;case`ss`:n=At(5,2);break;case`S`:n=At(6,1);break;case`SS`:n=At(6,2);break;case`SSS`:n=At(6,3);break;case`Z`:case`ZZ`:case`ZZZ`:n=ru(0);break;case`ZZZZZ`:n=ru(3);break;case`O`:case`OO`:case`OOO`:case`z`:case`zz`:case`zzz`:n=ru(1);break;case`OOOO`:case`ZZZZ`:case`zzzz`:n=ru(2);break;default:return null}return sg[t]=n,n}function Bx(t,n){t=t.replace(/:/g,``);let e=Date.parse(`Jan 01, 1970 00:00:00 `+t)/6e4;return isNaN(e)?n:e}function HN(t,n){return t=new Date(t.getTime()),t.setMinutes(t.getMinutes()+n),t}function UN(t,n,e){let a=t.getTimezoneOffset();return HN(t,-1*(Bx(n,a)-a))}function $N(t){if(Dx(t))return t;if(typeof t==`number`&&!isNaN(t))return new Date(t);if(typeof t==`string`){if(t=t.trim(),/^(\d{4}(-\d{1,2}(-\d{1,2})?)?)$/.test(t)){let[a,o=1,r=1]=t.split(`-`).map(s=>+s);return lu(a,o-1,r)}let e=parseFloat(t);if(!isNaN(t-e))return new Date(e);let i;if(i=t.match(AN))return GN(i)}let n=new Date(t);if(!Dx(n))throw new G(2311,!1);return n}function GN(t){let n=new Date(0),e=0,i=0,a=t[8]?n.setUTCFullYear:n.setFullYear,o=t[8]?n.setUTCHours:n.setHours;t[9]&&(e=Number(t[9]+t[10]),i=Number(t[9]+t[11])),a.call(n,Number(t[1]),Number(t[2])-1,Number(t[3]));let r=Number(t[4]||0)-e,s=Number(t[5]||0)-i,c=Number(t[6]||0),p=Math.floor(parseFloat(`0.`+(t[7]||0))*1e3);return o.call(n,r,s,c,p),n}function Dx(t){return t instanceof Date&&!isNaN(t.valueOf())}var WN=/^(\d+)?\.((\d+)(-(\d+))?)?$/;var kx=22;var du=`.`;var hc=`0`;var qN=`;`;var YN=`,`;var cg=`#`;function KN(t,n,e,i,a,o,r=!1){let s=``,c=!1;if(!isFinite(t))s=wi(e,Number.isNaN(t)?cn.NaN:cn.Infinity);else{let p=ZN(t);r&&(p=XN(p));let m=n.minInt,g=n.minFrac,v=n.maxFrac;if(o){let j=o.match(WN);if(j===null)throw new G(2306,!1);let x=j[1],M=j[3],T=j[5];x!=null&&(m=lg(x)),M!=null&&(g=lg(M)),T!=null?v=lg(T):M!=null&&g>v&&(v=g);let $=100;if(m>$||g>$||v>$)throw new G(2306,!1)}JN(p,g,v);let b=p.digits,_=p.integerLen,E=p.exponent,P=[];for(c=b.every(j=>!j);_<m;_++)b.unshift(0);for(;_<0;_++)b.unshift(0);_>0?P=b.splice(_,b.length):(P=b,b=[0]);let ae=[];for(b.length>=n.lgSize&&ae.unshift(b.splice(-n.lgSize,b.length).join(``));b.length>n.gSize;)ae.unshift(b.splice(-n.gSize,b.length).join(``));b.length&&ae.unshift(b.join(``)),s=ae.join(wi(e,i)),P.length&&(s+=wi(e,a)+P.join(``)),E&&(s+=wi(e,cn.Exponential)+`+`+E)}return t<0&&!c?s=n.negPre+s+n.negSuf:s=n.posPre+s+n.posSuf,s}function jx(t,n,e){return KN(t,QN(Ox(n,dg.Decimal),wi(n,cn.MinusSign)),n,cn.Group,cn.Decimal,e)}function QN(t,n=`-`){let e={minInt:1,minFrac:0,maxFrac:0,posPre:``,posSuf:``,negPre:``,negSuf:``,gSize:0,lgSize:0},i=t.split(qN),a=i[0],o=i[1],r=a.indexOf(du)!==-1?a.split(du):[a.substring(0,a.lastIndexOf(hc)+1),a.substring(a.lastIndexOf(hc)+1)],s=r[0],c=r[1]||``;e.posPre=s.substring(0,s.indexOf(cg));for(let m=0;m<c.length;m++){let g=c.charAt(m);g===hc?e.minFrac=e.maxFrac=m+1:g===cg?e.maxFrac=m+1:e.posSuf+=g}let p=s.split(YN);if(e.gSize=p[1]?p[1].length:0,e.lgSize=p[2]||p[1]?(p[2]||p[1]).length:0,o){let m=a.length-e.posPre.length-e.posSuf.length,g=o.indexOf(cg);e.negPre=o.substring(0,g).replace(/'/g,``),e.negSuf=o.slice(g+m).replace(/'/g,``)}else e.negPre=n+e.posPre,e.negSuf=e.posSuf;return e}function XN(t){if(t.digits[0]===0)return t;let n=t.digits.length-t.integerLen;return t.exponent?t.exponent+=2:(n===0?t.digits.push(0,0):n===1&&t.digits.push(0),t.integerLen+=2),t}function ZN(t){let n=Math.abs(t)+``,e=0,i,a,o,r,s;for((a=n.indexOf(du))>-1&&(n=n.replace(du,``)),(o=n.search(/e/i))>0?(a<0&&(a=o),a+=+n.slice(o+1),n=n.substring(0,o)):a<0&&(a=n.length),o=0;n.charAt(o)===hc;o++);if(o===(s=n.length))i=[0],a=1;else{for(s--;n.charAt(s)===hc;)s--;for(a-=o,i=[],r=0;o<=s;o++,r++)i[r]=Number(n.charAt(o))}return a>kx&&(i=i.splice(0,kx-1),e=a-1,a=1),{digits:i,exponent:e,integerLen:a}}function JN(t,n,e){if(n>e)throw new G(2307,!1);let i=t.digits,a=i.length-t.integerLen,o=Math.min(Math.max(n,a),e),r=o+t.integerLen,s=i[r];if(r>0){i.splice(Math.max(t.integerLen,r));for(let g=r;g<i.length;g++)i[g]=0}else{a=Math.max(0,a),t.integerLen=1,i.length=Math.max(1,r=o+1),i[0]=0;for(let g=1;g<r;g++)i[g]=0}if(s>=5)if(r-1<0){for(let g=0;g>r;g--)i.unshift(0),t.integerLen++;i.unshift(1),t.integerLen++}else i[r-1]++;for(;a<Math.max(0,o);a++)i.push(0);let c=o!==0,p=n+t.integerLen,m=i.reduceRight(function(g,v,b,_){return v=v+g,_[b]=v<10?v:v-10,c&&(_[b]===0&&b>=p?_.pop():c=!1),v>=10?1:0},0);m&&(i.unshift(m),t.integerLen++)}function lg(t){let n=parseInt(t);if(isNaN(n))throw new G(2305,!1);return n}var ug=(()=>{class t{_viewContainerRef;ngComponentOutlet=null;ngComponentOutletInputs;ngComponentOutletInjector;ngComponentOutletEnvironmentInjector;ngComponentOutletContent;ngComponentOutletNgModule;_componentRef;_moduleRef;_inputsUsed=new Map;get componentInstance(){return this._componentRef?.instance??null}constructor(e){this._viewContainerRef=e}_needToReCreateNgModuleInstance(e){return e.ngComponentOutletNgModule!==void 0}_needToReCreateComponentInstance(e){return e.ngComponentOutlet!==void 0||e.ngComponentOutletContent!==void 0||e.ngComponentOutletInjector!==void 0||e.ngComponentOutletEnvironmentInjector!==void 0||this._needToReCreateNgModuleInstance(e)}ngOnChanges(e){if(this._needToReCreateComponentInstance(e)&&(this._viewContainerRef.clear(),this._inputsUsed.clear(),this._componentRef=void 0,this.ngComponentOutlet)){let i=this.ngComponentOutletInjector||this._viewContainerRef.parentInjector;this._needToReCreateNgModuleInstance(e)&&(this._moduleRef?.destroy(),this.ngComponentOutletNgModule?this._moduleRef=Hf(this.ngComponentOutletNgModule,eT(i)):this._moduleRef=void 0),this._componentRef=this._viewContainerRef.createComponent(this.ngComponentOutlet,{injector:i,ngModuleRef:this._moduleRef,projectableNodes:this.ngComponentOutletContent,environmentInjector:this.ngComponentOutletEnvironmentInjector})}}ngDoCheck(){if(this._componentRef){if(this.ngComponentOutletInputs)for(let e of Object.keys(this.ngComponentOutletInputs))this._inputsUsed.set(e,!0);this._applyInputStateDiff(this._componentRef)}}ngOnDestroy(){this._moduleRef?.destroy()}_applyInputStateDiff(e){for(let[i,a]of this._inputsUsed)a?(e.setInput(i,this.ngComponentOutletInputs[i]),this._inputsUsed.set(i,!1)):(e.setInput(i,void 0),this._inputsUsed.delete(i))}static ɵfac=function(i){return new(i||t)(He(Vt))};static ɵdir=Y({type:t,selectors:[[``,`ngComponentOutlet`,``]],inputs:{ngComponentOutlet:`ngComponentOutlet`,ngComponentOutletInputs:`ngComponentOutletInputs`,ngComponentOutletInjector:`ngComponentOutletInjector`,ngComponentOutletEnvironmentInjector:`ngComponentOutletEnvironmentInjector`,ngComponentOutletContent:`ngComponentOutletContent`,ngComponentOutletNgModule:`ngComponentOutletNgModule`},exportAs:[`ngComponentOutlet`],features:[bt]})}return t})();function eT(t){return t.get(ni).injector}var fc=(()=>{class t{_viewContainerRef;_viewRef=null;ngTemplateOutletContext=null;ngTemplateOutlet=null;ngTemplateOutletInjector=null;injector=f(re);constructor(e){this._viewContainerRef=e}ngOnChanges(e){if(this._shouldRecreateView(e)){let i=this._viewContainerRef;if(this._viewRef&&i.remove(i.indexOf(this._viewRef)),!this.ngTemplateOutlet){this._viewRef=null;return}let a=this._createContextForwardProxy();this._viewRef=i.createEmbeddedView(this.ngTemplateOutlet,a,{injector:this._getInjector()})}}_getInjector(){return this.ngTemplateOutletInjector===`outlet`?this.injector:this.ngTemplateOutletInjector??void 0}_shouldRecreateView(e){return!!e.ngTemplateOutlet||!!e.ngTemplateOutletInjector}_createContextForwardProxy(){return new Proxy({},{set:(e,i,a)=>this.ngTemplateOutletContext?Reflect.set(this.ngTemplateOutletContext,i,a):!1,get:(e,i,a)=>{if(this.ngTemplateOutletContext)return Reflect.get(this.ngTemplateOutletContext,i,a)}})}static ɵfac=function(i){return new(i||t)(He(Vt))};static ɵdir=Y({type:t,selectors:[[``,`ngTemplateOutlet`,``]],inputs:{ngTemplateOutletContext:`ngTemplateOutletContext`,ngTemplateOutlet:`ngTemplateOutlet`,ngTemplateOutletInjector:`ngTemplateOutletInjector`},features:[bt]})}return t})();function zx(t,n){return new G(2100,!1)}var tT=`mediumDate`;var Hx=new I(``);var Ux=new I(``);var Xi=(()=>{class t{locale;defaultTimezone;defaultOptions;constructor(e,i,a){this.locale=e,this.defaultTimezone=i,this.defaultOptions=a}transform(e,i,a,o){if(e==null||e===``||e!==e)return null;try{let r=i??this.defaultOptions?.dateFormat??tT,s=a??this.defaultOptions?.timezone??this.defaultTimezone??void 0;return Lx(e,r,o||this.locale,s)}catch(r){throw zx(t,r.message)}}static ɵfac=function(i){return new(i||t)(He(Mr,16),He(Hx,24),He(Ux,24))};static ɵpipe=tc({name:`date`,type:t,pure:!0})}return t})();var So=(()=>{class t{transform(e){return JSON.stringify(e,null,2)}static ɵfac=function(i){return new(i||t)};static ɵpipe=tc({name:`json`,type:t,pure:!1})}return t})();var wn=(()=>{class t{_locale;constructor(e){this._locale=e}transform(e,i,a){if(!nT(e))return null;a||=this._locale;try{return jx(iT(e),a,i)}catch(o){throw zx(t,o.message)}}static ɵfac=function(i){return new(i||t)(He(Mr,16))};static ɵpipe=tc({name:`number`,type:t,pure:!0})}return t})();function nT(t){return!(t==null||t===``||t!==t)}function iT(t){if(typeof t==`string`&&!isNaN(Number(t)-parseFloat(t)))return Number(t);if(typeof t!=`number`)throw new G(2309,!1);return t}function gc(t,n){n=encodeURIComponent(n);for(let e of t.split(`;`)){let i=e.indexOf(`=`),[a,o]=i==-1?[e,``]:[e.slice(0,i),e.slice(i+1)];if(a.trim()!==n)continue;let r=o;try{r=decodeURIComponent(o)}catch{}return r.length>1&&r[0]===`"`&&r[r.length-1]===`"`&&(r=r.slice(1,-1)),r}return null}var mg=`browser`;var sT=`server`;function $x(t){return t===mg}function Gx(t){return t===sT}var bc=class{_doc;constructor(n){this._doc=n}manager};var uu=(()=>{class t extends bc{constructor(e){super(e)}supports(e){return!0}addEventListener(e,i,a,o){return e.addEventListener(i,a,o),()=>this.removeEventListener(e,i,a,o)}removeEventListener(e,i,a,o){return e.removeEventListener(i,a,o)}static ɵfac=function(i){return new(i||t)(Se(de))};static ɵprov=fe({token:t,factory:t.ɵfac})}return t})();var hu=new I(``);var gg=(()=>{class t{_zone;_plugins;_eventNameToPlugin=new Map;constructor(e,i){this._zone=i,e.forEach(r=>{r.manager=this});let a=e.filter(r=>!(r instanceof uu));this._plugins=a.slice().reverse();let o=e.find(r=>r instanceof uu);o&&this._plugins.push(o)}addEventListener(e,i,a,o){return this._findPluginFor(i).addEventListener(e,i,a,o)}getZone(){return this._zone}_findPluginFor(e){let i=this._eventNameToPlugin.get(e);if(i)return i;if(i=this._plugins.find(o=>o.supports(e)),!i)throw new G(-5101,!1);return this._eventNameToPlugin.set(e,i),i}static ɵfac=function(i){return new(i||t)(Se(hu),Se(Q))};static ɵprov=fe({token:t,factory:t.ɵfac})}return t})();var pg=`ng-app-id`;function Wx(t){for(let n of t)n.remove()}function qx(t,n){let e=n.createElement(`style`);return e.textContent=t,e}function cT(t,n,e,i){let a=t.head?.querySelectorAll(`style[${pg}="${n}"],link[${pg}="${n}"]`);if(!a||a.length===0)return!1;for(let o of a)o.removeAttribute(pg),o instanceof HTMLLinkElement?i.set(o.href.slice(o.href.lastIndexOf(`/`)+1),{usage:0,elements:[o]}):o.textContent&&e.set(o.textContent,{usage:0,elements:[o]});return!0}function fg(t,n){let e=n.createElement(`link`);return e.setAttribute(`rel`,`stylesheet`),e.setAttribute(`href`,t),e}var bg=(()=>{class t{doc;appId;nonce;inline=new Map;external=new Map;hosts=new Set;constructor(e,i,a,o={}){this.doc=e,this.appId=i,this.nonce=a,cT(e,i,this.inline,this.external)&&this.hosts.add(e.head)}addStyles(e,i){for(let a of e)this.addUsage(a,this.inline,qx);i?.forEach(a=>this.addUsage(a,this.external,fg))}removeStyles(e,i){for(let a of e)this.removeUsage(a,this.inline);i?.forEach(a=>this.removeUsage(a,this.external))}addUsage(e,i,a){let o=i.get(e);o?o.usage++:i.set(e,{usage:1,elements:[...this.hosts].map(r=>this.addElement(r,a(e,this.doc)))})}removeUsage(e,i){let a=i.get(e);a&&(a.usage--,a.usage<=0&&(Wx(a.elements),i.delete(e)))}ngOnDestroy(){for(let[,{elements:e}]of[...this.inline,...this.external])Wx(e);this.hosts.clear()}addHost(e){if(!this.hosts.has(e)){this.hosts.add(e);for(let[i,{elements:a}]of this.inline)a.push(this.addElement(e,qx(i,this.doc)));for(let[i,{elements:a}]of this.external)a.push(this.addElement(e,fg(i,this.doc)))}}removeHost(e){this.hosts.delete(e);for(let i of[...this.inline.values(),...this.external.values()]){let a=[];for(let o of i.elements)o.parentNode===e?o.remove():a.push(o);i.elements=a}}addElement(e,i){return this.nonce&&i.setAttribute(`nonce`,this.nonce),e.appendChild(i)}static ɵfac=function(i){return new(i||t)(Se(de),Se($i),Se(_a,8),Se(Gi))};static ɵprov=fe({token:t,factory:t.ɵfac})}return t})();var hg={svg:`http://www.w3.org/2000/svg`,xhtml:`http://www.w3.org/1999/xhtml`,xlink:`http://www.w3.org/1999/xlink`,xml:`http://www.w3.org/XML/1998/namespace`,xmlns:`http://www.w3.org/2000/xmlns/`,math:`http://www.w3.org/1998/Math/MathML`};var vg=/%COMP%/g;var Kx=`%COMP%`;var lT=`_nghost-${Kx}`;var dT=`_ngcontent-${Kx}`;var uT=!0;var mT=new I(``,{factory:()=>uT});var pT=new I(``);function hT(t){return dT.replace(vg,t)}function fT(t){return lT.replace(vg,t)}function Qx(t,n){return n.map(e=>e.replace(vg,t))}var yg=(()=>{class t{eventManager;sharedStylesHost;appId;removeStylesOnCompDestroy;doc;ngZone;nonce;tracingService;rendererByCompId=new Map;defaultRenderer;cssVarNamespace;constructor(e,i,a,o,r,s,c=null,p=null,m=null){this.eventManager=e,this.sharedStylesHost=i,this.appId=a,this.removeStylesOnCompDestroy=o,this.doc=r,this.ngZone=s,this.nonce=c,this.tracingService=p,this.cssVarNamespace=m??``,this.defaultRenderer=new vc(e,r,s,this.tracingService,this.cssVarNamespace)}createRenderer(e,i){if(!e||!i)return this.defaultRenderer;let a=this.getOrCreateRenderer(e,i);return a instanceof pu?a.applyToHost(e):a instanceof yc&&a.applyStyles(),a}getOrCreateRenderer(e,i){let a=this.rendererByCompId,o=a.get(i.id);if(!o){let r=this.doc,s=this.ngZone,c=this.eventManager,p=this.sharedStylesHost,m=this.removeStylesOnCompDestroy,g=this.tracingService;switch(i.encapsulation){case ei.Emulated:o=new pu(c,p,i,this.appId,m,r,s,g,this.cssVarNamespace);break;case ei.ShadowDom:return new mu(c,e,i,r,s,this.nonce,g,this.cssVarNamespace,p);case ei.ExperimentalIsolatedShadowDom:return new mu(c,e,i,r,s,this.nonce,g,this.cssVarNamespace);default:o=new yc(c,p,i,m,r,s,g,this.cssVarNamespace)}a.set(i.id,o)}return o}ngOnDestroy(){this.rendererByCompId.clear()}componentReplaced(e){this.rendererByCompId.delete(e)}static ɵfac=function(i){return new(i||t)(Se(gg),Se(_o),Se($i),Se(mT),Se(de),Se(Q),Se(_a),Se(ii,8),Se(pT,8))};static ɵprov=fe({token:t,factory:t.ɵfac})}return t})();var vc=class{eventManager;doc;ngZone;tracingService;cssVarNamespace;data=Object.create(null);throwOnSyntheticProps=!0;constructor(n,e,i,a,o=``){this.eventManager=n,this.doc=e,this.ngZone=i,this.tracingService=a,this.cssVarNamespace=o}destroy(){}destroyNode=null;createElement(n,e){return e?this.doc.createElementNS(hg[e]||e,n):this.doc.createElement(n)}createComment(n){return this.doc.createComment(n)}createText(n){return this.doc.createTextNode(n)}appendChild(n,e){(Yx(n)?n.content:n).appendChild(e)}insertBefore(n,e,i){if(n){let a=Yx(n)?n.content:n;if(i!=null&&i.parentNode!==a)throw new G(-5106,!1);a.insertBefore(e,i)}}removeChild(n,e){e.remove()}selectRootElement(n,e){let i=typeof n==`string`?this.doc.querySelector(n):n;if(!i)throw new G(-5104,!1);return e||(i.textContent=``),i}parentNode(n){return n.parentNode}nextSibling(n){return n.nextSibling}setAttribute(n,e,i,a){if(a){e=a+`:`+e;let o=hg[a];o?n.setAttributeNS(o,e,i):n.setAttribute(e,i)}else n.setAttribute(e,i)}removeAttribute(n,e,i){if(i){let a=hg[i];a?n.removeAttributeNS(a,e):n.removeAttribute(`${i}:${e}`)}else n.removeAttribute(e)}addClass(n,e){n.classList.add(e)}removeClass(n,e){n.classList.remove(e)}setStyle(n,e,i,a){let o=e.startsWith(`--`);o&&(e=e.replace(`%NS%`,this.cssVarNamespace)),o||a&(Ci.DashCase|Ci.Important)?n.style.setProperty(e,i,a&Ci.Important?`important`:``):n.style[e]=i}removeStyle(n,e,i){let a=e.startsWith(`--`);a&&(e=e.replace(`%NS%`,this.cssVarNamespace)),a||i&Ci.DashCase?n.style.removeProperty(e):n.style[e]=``}setProperty(n,e,i){n!=null&&(n[e]=i)}setValue(n,e){n.nodeValue=e}listen(n,e,i,a){if(typeof n==`string`&&(n=Fn().getGlobalEventTarget(this.doc,n),!n))throw new G(-5102,!1);let o=this.decoratePreventDefault(i);return this.tracingService?.wrapEventListener&&(o=this.tracingService.wrapEventListener(n,e,o)),this.eventManager.addEventListener(n,e,o,a)}decoratePreventDefault(n){return e=>{if(e===`__ngUnwrap__`)return n;n(e)===!1&&e.preventDefault()}}};function Yx(t){return t.tagName===`TEMPLATE`&&t.content!==void 0}var mu=class extends vc{hostEl;sharedStylesHost;shadowRoot;constructor(n,e,i,a,o,r,s,c,p){super(n,a,o,s,c),this.hostEl=e,this.sharedStylesHost=p,this.shadowRoot=e.attachShadow({mode:`open`}),this.sharedStylesHost&&this.sharedStylesHost.addHost(this.shadowRoot);let m=i.styles;m=Qx(i.id,m).map(v=>v.replace(/%NS%/g,c));for(let v of m){let b=document.createElement(`style`);r&&b.setAttribute(`nonce`,r),b.textContent=v,this.shadowRoot.appendChild(b)}let g=i.getExternalStyles?.();if(g)for(let v of g){let b=fg(v,a);r&&b.setAttribute(`nonce`,r),this.shadowRoot.appendChild(b)}}nodeOrShadowRoot(n){return n===this.hostEl?this.shadowRoot:n}appendChild(n,e){return super.appendChild(this.nodeOrShadowRoot(n),e)}insertBefore(n,e,i){return super.insertBefore(this.nodeOrShadowRoot(n),e,i)}removeChild(n,e){return super.removeChild(null,e)}parentNode(n){return this.nodeOrShadowRoot(super.parentNode(this.nodeOrShadowRoot(n)))}destroy(){this.sharedStylesHost&&this.sharedStylesHost.removeHost(this.shadowRoot)}};var yc=class extends vc{sharedStylesHost;removeStylesOnCompDestroy;styles;styleUrls;constructor(n,e,i,a,o,r,s,c,p){super(n,o,r,s,c),this.sharedStylesHost=e,this.removeStylesOnCompDestroy=a;let m=i.styles,g=p?Qx(p,m):m;this.styles=g.map(v=>v.replace(/%NS%/g,c)),this.styleUrls=i.getExternalStyles?.(p)}applyStyles(){this.sharedStylesHost.addStyles(this.styles,this.styleUrls)}destroy(){this.removeStylesOnCompDestroy&&vf.size===0&&this.sharedStylesHost.removeStyles(this.styles,this.styleUrls)}};var pu=class extends yc{contentAttr;hostAttr;constructor(n,e,i,a,o,r,s,c,p){let m=a+`-`+i.id;super(n,e,i,o,r,s,c,p,m),this.contentAttr=hT(m),this.hostAttr=fT(m)}applyToHost(n){this.applyStyles(),this.setAttribute(n,this.hostAttr,``)}createElement(n,e){let i=super.createElement(n,e);return super.setAttribute(i,this.contentAttr,``),i}};var fu=class t extends dc{supportsDOMEvents=!0;static makeCurrent(){ag(new t)}onAndCancel(n,e,i,a){return n.addEventListener(e,i,a),()=>{n.removeEventListener(e,i,a)}}dispatchEvent(n,e){n.dispatchEvent(e)}remove(n){n.remove()}createElement(n,e){return e=e||this.getDefaultDocument(),e.createElement(n)}createHtmlDocument(){return document.implementation.createHTMLDocument(`fakeTitle`)}getDefaultDocument(){return document}isElementNode(n){return n.nodeType===Node.ELEMENT_NODE}isShadowRoot(n){return n instanceof DocumentFragment}getGlobalEventTarget(n,e){return e===`window`?window:e===`document`?n:e===`body`?n.body:null}getBaseHref(n){let e=gT();return e==null?null:bT(e)}resetBaseElement(){_c=null}getUserAgent(){return window.navigator.userAgent}getCookie(n){return gc(document.cookie,n)}};var _c=null;function gT(){return _c=_c||document.head.querySelector(`base`),_c?_c.getAttribute(`href`):null}function bT(t){return new URL(t,document.baseURI).pathname}var Xx=[`alt`,`control`,`meta`,`shift`];var vT={"\b":`Backspace`,"	":`Tab`,"":`Delete`,"\x1B":`Escape`,Del:`Delete`,Esc:`Escape`,Left:`ArrowLeft`,Right:`ArrowRight`,Up:`ArrowUp`,Down:`ArrowDown`,Menu:`ContextMenu`,Scroll:`ScrollLock`,Win:`OS`};var yT={alt:t=>t.altKey,control:t=>t.ctrlKey,meta:t=>t.metaKey,shift:t=>t.shiftKey};var Zx=(()=>{class t extends bc{constructor(e){super(e)}supports(e){return t.parseEventName(e)!=null}addEventListener(e,i,a,o){let r=t.parseEventName(i),s=t.eventCallback(r.fullKey,a,this.manager.getZone());return this.manager.getZone().runOutsideAngular(()=>Fn().onAndCancel(e,r.domEventName,s,o))}static parseEventName(e){let i=e.toLowerCase().split(`.`),a=i.shift();if(i.length===0||!(a===`keydown`||a===`keyup`))return null;let o=t._normalizeKey(i.pop()),r=``,s=i.indexOf(`code`);if(s>-1&&(i.splice(s,1),r=`code.`),Xx.forEach(p=>{let m=i.indexOf(p);m>-1&&(i.splice(m,1),r+=p+`.`)}),r+=o,i.length!=0||o.length===0)return null;let c={};return c.domEventName=a,c.fullKey=r,c}static matchEventFullKeyCode(e,i){let a=vT[e.key]||e.key,o=``;return i.indexOf(`code.`)>-1&&(a=e.code,o=`code.`),a==null||!a?!1:(a=a.toLowerCase(),a===` `?a=`space`:a===`.`&&(a=`dot`),Xx.forEach(r=>{if(r!==a){let s=yT[r];s(e)&&(o+=r+`.`)}}),o+=a,o===i)}static eventCallback(e,i,a){return o=>{t.matchEventFullKeyCode(o,e)&&a.runGuarded(()=>i(o))}}static _normalizeKey(e){return e===`esc`?`escape`:e}static ɵfac=function(i){return new(i||t)(Se(de))};static ɵprov=fe({token:t,factory:t.ɵfac})}return t})();async function _g(t,n,e){return yx(r({rootComponent:t},_T(n,e)))}function _T(t,n){return{platformRef:n?.platformRef,appProviders:[...ET,...t?.providers??[]],platformProviders:wT}}function xT(){fu.makeCurrent()}function CT(){return new Nn}function ST(){return cf(document),document}var wT=[{provide:Gi,useValue:mg},{provide:md,useValue:xT,multi:!0},{provide:de,useFactory:ST}];var ET=[{provide:Ms,useValue:`root`},{provide:Nn,useFactory:CT},{provide:hu,useClass:uu,multi:!0},{provide:hu,useClass:Zx,multi:!0},yg,{provide:_o,useClass:bg},{provide:bg,useExisting:_o},gg,{provide:Mt,useExisting:yg},[]];var Ji=class t{headers;normalizedNames=new Map;lazyInit;lazyUpdate=null;constructor(n){n?typeof n==`string`?this.lazyInit=()=>{this.headers=new Map,n.split(`
`).forEach(e=>{let i=e.indexOf(`:`);if(i>0){let a=e.slice(0,i),o=e.slice(i+1).trim();this.addHeaderEntry(a,o)}})}:typeof Headers<`u`&&n instanceof Headers?(this.headers=new Map,n.forEach((e,i)=>{this.addHeaderEntry(i,e)})):this.lazyInit=()=>{this.headers=new Map,Object.entries(n).forEach(([e,i])=>{this.setHeaderEntries(e,i)})}:this.headers=new Map}has(n){return this.init(),this.headers.has(n.toLowerCase())}get(n){this.init();let e=this.headers.get(n.toLowerCase());return e&&e.length>0?e[0]:null}keys(){return this.init(),Array.from(this.normalizedNames.values())}getAll(n){return this.init(),this.headers.get(n.toLowerCase())||null}append(n,e){return this.clone({name:n,value:e,op:`a`})}set(n,e){return this.clone({name:n,value:e,op:`s`})}delete(n,e){return this.clone({name:n,value:e,op:`d`})}maybeSetNormalizedName(n,e){this.normalizedNames.has(e)||this.normalizedNames.set(e,n)}init(){this.lazyInit&&(this.lazyInit instanceof t?this.copyFrom(this.lazyInit):this.lazyInit(),this.lazyInit=null,this.lazyUpdate&&(this.lazyUpdate.forEach(n=>this.applyUpdate(n)),this.lazyUpdate=null))}copyFrom(n){n.init();for(let[e,i]of n.headers.entries())this.headers.set(e,i),this.normalizedNames.set(e,n.normalizedNames.get(e))}clone(n){let e=new t;return e.lazyInit=this.lazyInit&&this.lazyInit instanceof t?this.lazyInit:this,e.lazyUpdate=(this.lazyUpdate||[]).concat([n]),e}applyUpdate(n){let e=n.name.toLowerCase();switch(n.op){case`a`:case`s`:let i=n.value;if(typeof i==`string`&&(i=[i]),i.length===0)return;this.maybeSetNormalizedName(n.name,e);let a=n.op===`a`?(this.headers.get(e)||[]).slice():[];a.push(...i),this.headers.set(e,a);break;case`d`:let o=n.value;if(o===void 0)this.headers.delete(e),this.normalizedNames.delete(e);else{let r=Array.isArray(o)?o:[o],s=this.headers.get(e);if(!s)return;s=s.filter(c=>r.indexOf(c)===-1),s.length===0?(this.headers.delete(e),this.normalizedNames.delete(e)):this.headers.set(e,s)}}}addHeaderEntry(n,e){let i=n.toLowerCase();this.maybeSetNormalizedName(n,i),this.headers.has(i)?this.headers.get(i).push(e):this.headers.set(i,[e])}setHeaderEntries(n,e){let i=(Array.isArray(e)?e:[e]).map(o=>o.toString()),a=n.toLowerCase();this.headers.set(a,i),this.maybeSetNormalizedName(n,a)}forEach(n){this.init(),Array.from(this.normalizedNames.keys()).forEach(e=>n(this.normalizedNames.get(e),this.headers.get(e)))}};var bu=class{map=new Map;set(n,e){return this.map.set(n,e),this}get(n){return this.map.has(n)||this.map.set(n,n.defaultValue()),this.map.get(n)}delete(n){return this.map.delete(n),this}has(n){return this.map.has(n)}keys(){return this.map.keys()}};var vu=class{encodeKey(n){return Jx(n)}encodeValue(n){return Jx(n)}decodeKey(n){return decodeURIComponent(n)}decodeValue(n){return decodeURIComponent(n)}};function DT(t,n){let e=new Map;return t.length>0&&t.replace(/^\?/,``).split(`&`).forEach(a=>{let o=a.indexOf(`=`),[r,s]=o==-1?[n.decodeKey(a),``]:[n.decodeKey(a.slice(0,o)),n.decodeValue(a.slice(o+1))],c=e.get(r)||[];c.push(s),e.set(r,c)}),e}var kT=/%(\d[a-f0-9])/gi;var IT={40:`@`,"3A":`:`,24:`$`,"2C":`,`,"3B":`;`,"3D":`=`,"3F":`?`,"2F":`/`};function Jx(t){return encodeURIComponent(t).replace(kT,(n,e)=>IT[e]??n)}function gu(t){return`${t}`}var Zi=class t{map;encoder;updates=null;cloneFrom=null;constructor(n={}){if(this.encoder=n.encoder||new vu,n.fromString){if(n.fromObject)throw new G(2805,!1);this.map=DT(n.fromString,this.encoder)}else n.fromObject?(this.map=new Map,Object.keys(n.fromObject).forEach(e=>{let i=n.fromObject[e],a=Array.isArray(i)?i.map(gu):[gu(i)];this.map.set(e,a)})):this.map=null}has(n){return this.init(),this.map.has(n)}get(n){this.init();let e=this.map.get(n);return e?e[0]:null}getAll(n){return this.init(),this.map.get(n)||null}keys(){return this.init(),Array.from(this.map.keys())}append(n,e){return this.clone({param:n,value:e,op:`a`})}appendAll(n){let e=[];return Object.keys(n).forEach(i=>{let a=n[i];Array.isArray(a)?a.forEach(o=>{e.push({param:i,value:o,op:`a`})}):e.push({param:i,value:a,op:`a`})}),this.clone(e)}set(n,e){return this.clone({param:n,value:e,op:`s`})}delete(n,e){return this.clone({param:n,value:e,op:`d`})}toString(){return this.init(),this.keys().map(n=>{let e=this.encoder.encodeKey(n);return this.map.get(n).map(i=>e+`=`+this.encoder.encodeValue(i)).join(`&`)}).filter(n=>n!==``).join(`&`)}clone(n){let e=new t({encoder:this.encoder});return e.cloneFrom=this.cloneFrom||this,e.updates=(this.updates||[]).concat(n),e}init(){if(this.map===null&&(this.map=new Map),this.cloneFrom!==null){this.cloneFrom.init();for(let[n,e]of this.cloneFrom.map.entries())this.map.set(n,e);this.updates.forEach(n=>{switch(n.op){case`a`:case`s`:let e=n.op===`a`?(this.map.get(n.param)||[]).slice():[];e.push(gu(n.value)),this.map.set(n.param,e);break;case`d`:if(n.value!==void 0){let i=(this.map.get(n.param)||[]).slice(),a=i.indexOf(gu(n.value));a!==-1&&i.splice(a,1),i.length>0?this.map.set(n.param,i):this.map.delete(n.param)}else{this.map.delete(n.param);break}}}),this.cloneFrom=this.updates=null}}};function MT(t){switch(t){case`DELETE`:case`GET`:case`HEAD`:case`OPTIONS`:case`JSONP`:return!1;default:return!0}}function eC(t){return typeof ArrayBuffer<`u`&&t instanceof ArrayBuffer}function tC(t){return typeof Blob<`u`&&t instanceof Blob}function nC(t){return typeof FormData<`u`&&t instanceof FormData}function NT(t){return typeof URLSearchParams<`u`&&t instanceof URLSearchParams}var xg=`Content-Type`;var iC=`Accept`;var rC=`text/plain`;var sC=`application/json`;var TT=`${sC}, ${rC}, */*`;var Tr=class t{url;body=null;headers;context;reportProgress=!1;reportUploadProgress=!1;reportDownloadProgress=!1;withCredentials=!1;credentials;keepalive=!1;cache;priority;mode;redirect;referrer;integrity;referrerPolicy;responseType=`json`;method;params;urlWithParams;transferCache;timeout;constructor(n,e,i,a){this.url=e,this.method=n.toUpperCase();let o;if(MT(this.method)||a?(this.body=i!==void 0?i:null,o=a):o=i,o){if(this.reportProgress=!!o.reportProgress,this.reportUploadProgress=!!o.reportUploadProgress,this.reportDownloadProgress=!!o.reportDownloadProgress,this.withCredentials=!!o.withCredentials,this.keepalive=!!o.keepalive,o.responseType&&(this.responseType=o.responseType),o.headers&&(this.headers=o.headers),o.context&&(this.context=o.context),o.params&&(this.params=o.params),o.priority&&(this.priority=o.priority),o.cache&&(this.cache=o.cache),o.credentials&&(this.credentials=o.credentials),typeof o.timeout==`number`){if(o.timeout<1||!Number.isInteger(o.timeout))throw new G(2822,``);this.timeout=o.timeout}o.mode&&(this.mode=o.mode),o.redirect&&(this.redirect=o.redirect),o.integrity&&(this.integrity=o.integrity),o.referrer!==void 0&&(this.referrer=o.referrer),o.referrerPolicy&&(this.referrerPolicy=o.referrerPolicy),this.transferCache=o.transferCache}if(this.headers??=new Ji,this.context??=new bu,!this.params)this.params=new Zi,this.urlWithParams=e;else{let r=this.params.toString();if(r.length===0)this.urlWithParams=e;else{let s=e,c=``,p=e.indexOf(`#`);p!==-1&&(c=e.substring(p),s=e.substring(0,p));let m=s.indexOf(`?`),g=m===-1?`?`:m<s.length-1?`&`:``;this.urlWithParams=s+g+r+c}}}serializeBody(){return this.body===null?null:typeof this.body==`string`||eC(this.body)||tC(this.body)||nC(this.body)||NT(this.body)?this.body:this.body instanceof Zi?this.body.toString():typeof this.body==`object`||typeof this.body==`boolean`||Array.isArray(this.body)?JSON.stringify(this.body):this.body.toString()}detectContentTypeHeader(){return this.body===null||nC(this.body)?null:tC(this.body)?this.body.type||null:eC(this.body)?null:typeof this.body==`string`?rC:this.body instanceof Zi?`application/x-www-form-urlencoded;charset=UTF-8`:typeof this.body==`object`||typeof this.body==`number`||typeof this.body==`boolean`?sC:null}clone(n={}){let e=n.method||this.method,i=n.url||this.url,a=n.responseType||this.responseType,o=n.keepalive??this.keepalive,r=n.priority||this.priority,s=n.cache||this.cache,c=n.mode||this.mode,p=n.redirect||this.redirect,m=n.credentials||this.credentials,g=n.referrer??this.referrer,v=n.integrity||this.integrity,b=n.referrerPolicy||this.referrerPolicy,_=n.transferCache??this.transferCache,E=n.timeout??this.timeout,P=n.body!==void 0?n.body:this.body,ae=n.withCredentials??this.withCredentials,j=n.reportProgress??this.reportProgress,x=n.reportUploadProgress??this.reportUploadProgress,M=n.reportDownloadProgress??this.reportDownloadProgress,T=n.headers||this.headers,$=n.params||this.params,ye=n.context??this.context;return n.setHeaders!==void 0&&(T=Object.keys(n.setHeaders).reduce((gt,We)=>gt.set(We,n.setHeaders[We]),T)),n.setParams&&($=Object.keys(n.setParams).reduce((gt,We)=>gt.set(We,n.setParams[We]),$)),new t(e,i,P,{params:$,headers:T,context:ye,reportProgress:j,reportUploadProgress:x,reportDownloadProgress:M,responseType:a,withCredentials:ae,transferCache:_,keepalive:o,cache:s,priority:r,timeout:E,mode:c,redirect:p,credentials:m,referrer:g,integrity:v,referrerPolicy:b})}};var wo=(function(t){return t[t.Sent=0]=`Sent`,t[t.UploadProgress=1]=`UploadProgress`,t[t.ResponseHeader=2]=`ResponseHeader`,t[t.DownloadProgress=3]=`DownloadProgress`,t[t.Response=4]=`Response`,t[t.User=5]=`User`,t})(wo||{});var Ar=class{headers;status;statusText;url;ok;type;redirected;responseType;constructor(n,e=200,i=`OK`){this.headers=n.headers||new Ji,this.status=n.status!==void 0?n.status:e,this.statusText=n.statusText||i,this.url=n.url||null,this.redirected=n.redirected,this.responseType=n.responseType,this.ok=this.status>=200&&this.status<300}};var yu=class t extends Ar{constructor(n={}){super(n)}type=wo.ResponseHeader;clone(n={}){return new t({headers:n.headers||this.headers,status:n.status!==void 0?n.status:this.status,statusText:n.statusText||this.statusText,url:n.url||this.url||void 0})}};var xc=class t extends Ar{body;constructor(n={}){super(n),this.body=n.body!==void 0?n.body:null}type=wo.Response;clone(n={}){return new t({body:n.body!==void 0?n.body:this.body,headers:n.headers||this.headers,status:n.status!==void 0?n.status:this.status,statusText:n.statusText||this.statusText,url:n.url||this.url||void 0,redirected:n.redirected??this.redirected,responseType:n.responseType??this.responseType})}};var Ei=class extends Ar{name=`HttpErrorResponse`;message;error;ok=!1;constructor(n){super(n,0,`Unknown Error`),this.status>=200&&this.status<300?this.message=`Http failure during parsing for ${n.url||`(unknown url)`}`:this.message=`Http failure response for ${n.url||`(unknown url)`}: ${n.status} ${n.statusText}`,this.error=n.error||null}};var AT=200;var OT=/^\)\]\}',?\n/;var cC=new I(``,{factory:()=>null});var _u=(()=>{class t{fetchImpl=f(Sg,{optional:!0})?.fetch??((...e)=>globalThis.fetch(...e));ngZone=f(Q);destroyRef=f(Te);maxResponseSize=f(cC);handle(e){return new Ce(i=>{let a=new AbortController,o=!1,r={next:c=>{c.type===wo.Response&&(o=!0),i.next(c)},error:c=>{o=!0,i.error(c)},complete:()=>{o=!0,i.complete()}};this.doRequest(e,a.signal,r).then(wg,c=>r.error(new Ei({error:c})));let s;return e.timeout&&(s=this.ngZone.runOutsideAngular(()=>setTimeout(()=>{a.signal.aborted||a.abort(new DOMException(`signal timed out`,`TimeoutError`))},e.timeout))),()=>{s!==void 0&&clearTimeout(s),!o&&!a.signal.aborted&&a.abort()}})}async doRequest(e,i,a){let o=this.createRequestInit(e),r$1;try{let P=this.ngZone.runOutsideAngular(()=>this.fetchImpl(e.urlWithParams,r({signal:i},o)));RT(P),a.next({type:wo.Sent}),r$1=await P}catch(P){a.error(new Ei({error:P,status:P.status??0,statusText:P.statusText,url:e.urlWithParams,headers:P.headers}));return}let s=new Ji(r$1.headers),c=r$1.statusText,p=r$1.url||e.urlWithParams,m=r$1.status,g=null,v=e.reportProgress||e.reportDownloadProgress;if(v&&a.next(new yu({headers:s,status:m,statusText:c,url:p})),r$1.body){let P=r$1.headers.get(xg)??``,ae=r$1.headers.get(`content-length`),j=ae!==null?Number(ae):NaN;this.maxResponseSize!==null&&Number.isFinite(j)&&j>this.maxResponseSize&&(await r$1.body.cancel(),aC(this.maxResponseSize));let x=[],M=r$1.body.getReader(),T=0,$,ye,gt=typeof Zone<`u`&&Zone.current,We=!1;if(await this.ngZone.runOutsideAngular(async()=>{for(;;){if(this.destroyRef.destroyed){await M.cancel(),We=!0;break}let{done:ke,value:F}=await M.read();if(ke)break;if(x.push(F),T+=F.length,this.maxResponseSize!==null&&T>this.maxResponseSize&&(await M.cancel(),aC(this.maxResponseSize)),v){ye=e.responseType===`text`?(ye??``)+($??=oC(P)).decode(F,{stream:!0}):void 0;let qe=()=>a.next({type:wo.DownloadProgress,total:Number.isFinite(j)?j:void 0,loaded:T,partialText:ye});gt?gt.run(qe):qe()}}}),We){a.complete();return}let ue=this.concatChunks(x,T);try{g=this.parseBody(e,ue,P,m)}catch(ke){a.error(new Ei({error:ke,headers:new Ji(r$1.headers),status:r$1.status,statusText:r$1.statusText,url:r$1.url||e.urlWithParams}));return}}m===0&&(m=g?AT:0);let b=m>=200&&m<300,_=r$1.redirected,E=r$1.type;b?(a.next(new xc({body:g,headers:s,status:m,statusText:c,url:p,redirected:_,responseType:E})),a.complete()):a.error(new Ei({error:g,headers:s,status:m,statusText:c,url:p,redirected:_,responseType:E}))}parseBody(e,i,a,o){switch(e.responseType){case`json`:let r=new TextDecoder().decode(i).replace(OT,``);if(r===``)return null;try{return JSON.parse(r)}catch(s){if(o<200||o>=300)return r;throw s}case`text`:return oC(a).decode(i);case`blob`:return new Blob([i],{type:a});case`arraybuffer`:return i.buffer}}createRequestInit(e){if(e.reportUploadProgress)throw new G(2824,!1);let i={},a;if(a=e.credentials,e.withCredentials&&(a=`include`),e.headers.forEach((o,r)=>i[o]=r.join(`,`)),e.headers.has(iC)||(i[iC]=TT),!e.headers.has(xg)){let o=e.detectContentTypeHeader();o!==null&&(i[xg]=o)}return{body:e.serializeBody(),method:e.method,headers:i,credentials:a,keepalive:e.keepalive,cache:e.cache,priority:e.priority,mode:e.mode,redirect:e.redirect,referrer:e.referrer,integrity:e.integrity,referrerPolicy:e.referrerPolicy}}concatChunks(e,i){let a=new Uint8Array(i),o=0;for(let r of e)a.set(r,o),o+=r.length;return a}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Sg=class{};function wg(){}function RT(t){t.then(wg,wg)}function aC(t){throw new G(-2825,!1)}var PT=/charset=\s*["']?([^;"'\s]+)["']?/i;function oC(t){let n=t.match(PT);if(n!==null)try{return new TextDecoder(n[1])}catch{}return new TextDecoder}var FT=new I(``,{factory:()=>!0});var LT=`XSRF-TOKEN`;var VT=new I(``,{factory:()=>LT});var BT=`X-XSRF-TOKEN`;var jT=new I(``,{factory:()=>BT});var zT=(()=>{class t{cookieName=f(VT);doc=f(de);lastCookieString=``;lastToken=null;parseCount=0;getToken(){let e=this.doc.cookie||``;return e!==this.lastCookieString&&(this.parseCount++,this.lastToken=gc(e,this.cookieName),this.lastCookieString=e),this.lastToken}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var lC=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=fe({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(zT),a},providedIn:`root`})}return t})();function dC(t,n){if(!f(FT)||t.method===`GET`||t.method===`HEAD`)return n(t);try{let a=f(Nr).href,{origin:o}=new URL(a),{origin:r}=new URL(t.url,o);if(o!==r)return n(t)}catch{return n(t)}let e=f(lC).getToken(),i=f(jT);return e!=null&&!t.headers.has(i)&&(t=t.clone({headers:t.headers.set(i,e)})),n(t)}function HT(t,n){return n(t)}function UT(t,n,e){return(i,a)=>ar(e,()=>n(i,o=>t(o,a)))}var uC=new I(``,{factory:()=>[dC]});var mC=new I(``);var pC=new I(``,{factory:()=>!0});var Eg=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=fe({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(_u),a},providedIn:`root`})}return t})();var xu=(()=>{class t{backend;injector;chain=null;pendingTasks=f(xa);contributeToStability=f(pC);constructor(e,i){this.backend=e,this.injector=i}handle(e){if(this.chain===null){let a=this.injector.get(Cu,null,{skipSelf:!0}),o=a!==null&&this.backend===a,r=this.injector.get(mC,[],o?{self:!0}:void 0),s=Array.from(new Set([...this.injector.get(uC),...r]));this.chain=s.reduceRight((c,p)=>UT(c,p,this.injector),HT)}let i=this.chain;if(this.contributeToStability){let a=this.pendingTasks.add();return nt(()=>i(e,o=>this.backend.handle(o))).pipe(sp(a))}else return nt(()=>i(e,a=>this.backend.handle(a)))}static ɵfac=function(i){return new(i||t)(Se(Eg),Se(kt))};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();var Cu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=fe({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(xu),a},providedIn:`root`})}return t})();function Cg(t,n){return r({body:n},t)}var Di=(()=>{class t{handler;constructor(e){this.handler=e}request(e,i,a={}){let o;if(e instanceof Tr)o=e;else{let c;a.headers instanceof Ji?c=a.headers:c=new Ji(a.headers);let p;a.params&&(a.params instanceof Zi?p=a.params:p=new Zi({fromObject:a.params})),o=new Tr(e,i,a.body!==void 0?a.body:null,{headers:c,context:a.context,params:p,reportProgress:a.reportProgress,reportUploadProgress:a.reportUploadProgress,reportDownloadProgress:a.reportDownloadProgress,responseType:a.responseType||`json`,withCredentials:a.withCredentials,transferCache:a.transferCache,keepalive:a.keepalive,priority:a.priority,cache:a.cache,mode:a.mode,redirect:a.redirect,credentials:a.credentials,referrer:a.referrer,referrerPolicy:a.referrerPolicy,integrity:a.integrity,timeout:a.timeout})}let r=Mn(o).pipe(rp(c=>this.handler.handle(c)));if(e instanceof Tr||a.observe===`events`)return r;let s=r.pipe(Qe(c=>c instanceof xc));switch(a.observe||`body`){case`body`:switch(o.responseType){case`arraybuffer`:return s.pipe(rt(c=>{if(c.body!==null&&!(c.body instanceof ArrayBuffer))throw new G(2806,!1);return c.body}));case`blob`:return s.pipe(rt(c=>{if(c.body!==null&&!(c.body instanceof Blob))throw new G(2807,!1);return c.body}));case`text`:return s.pipe(rt(c=>{if(c.body!==null&&typeof c.body!=`string`)throw new G(2808,!1);return c.body}));default:return s.pipe(rt(c=>c.body))}case`response`:return s;default:throw new G(2809,!1)}}delete(e,i={}){return this.request(`DELETE`,e,i)}get(e,i={}){return this.request(`GET`,e,i)}head(e,i={}){return this.request(`HEAD`,e,i)}jsonp(e,i){return this.request(`JSONP`,e,{params:new Zi().append(i,`JSONP_CALLBACK`),observe:`body`,responseType:`json`})}options(e,i={}){return this.request(`OPTIONS`,e,i)}patch(e,i,a={}){return this.request(`PATCH`,e,Cg(a,i))}post(e,i,a={}){return this.request(`POST`,e,Cg(a,i))}put(e,i,a={}){return this.request(`PUT`,e,Cg(a,i))}static ɵfac=function(i){return new(i||t)(Se(Cu))};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();function Dg(...t){let n=[Di,_u,xu,{provide:Cu,useExisting:xu},{provide:Eg,useFactory:()=>f(_u)},{provide:uC,useValue:dC,multi:!0}];for(let e of t)n.push(...e.ɵproviders);return rn(n)}var kg=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=fe({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se($T),a},providedIn:`root`})}return t})();var $T=(()=>{class t extends kg{_doc=f(de);sanitize(e,i){if(i==null)return null;switch(e){case _n.NONE:return i;case _n.HTML:return Sa(i,`HTML`)?ti(i):ff(this._doc,String(i)).toString();case _n.STYLE:return Sa(i,`Style`)?ti(i):i;case _n.SCRIPT:if(Sa(i,`Script`))return ti(i);throw new G(5200,!1);case _n.URL:return Sa(i,`URL`)?ti(i):Ks(String(i));case _n.RESOURCE_URL:if(Sa(i,`ResourceURL`))return ti(i);throw new G(-5201,!1);default:throw new G(5202,!1)}}bypassSecurityTrustHtml(e){return df(e)}bypassSecurityTrustStyle(e){return uf(e)}bypassSecurityTrustScript(e){return mf(e)}bypassSecurityTrustUrl(e){return pf(e)}bypassSecurityTrustResourceUrl(e){return hf(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var hC=new I(`HIGHCHARTS_LOADER`);var fC=new I(`HIGHCHARTS_ROOT_MODULES`);var gC=new I(`HIGHCHARTS_OPTIONS`);var GT=new I(`HIGHCHARTS_CONFIG`);var bC=new I(`HIGHCHARTS_TIMEOUT`);var WT=(()=>{class t{constructor(){this.highcharts=N(null),this.loader=f(hC),this.globalOptions=f(gC,{optional:!0}),this.globalModules=f(fC,{optional:!0}),this.sharedHighchartsPromise=null,this.moduleLoadCache=new WeakMap}async loadModules(e){if(!e)return;let i=this.moduleLoadCache.get(e);if(i)return i;let a=Promise.allSettled(e()).then(o=>{let r=o.filter(s=>s.status===`rejected`);if(r.length){let s=r.map(({reason:c})=>c instanceof Error?c.message:String(c));throw new Error(`Failed to load Highcharts modules: ${s.join(`; `)}`)}});return this.moduleLoadCache.set(e,a),a.catch(()=>{this.moduleLoadCache.get(e)===a&&this.moduleLoadCache.delete(e)}),a}async ensureSharedHighcharts(){if(!this.sharedHighchartsPromise){let e=(async()=>{let i=await this.loader();return await this.loadModules(this.globalModules),this.globalOptions&&i.setOptions(this.globalOptions),i})();this.sharedHighchartsPromise=e,e.catch(()=>{this.sharedHighchartsPromise===e&&(this.sharedHighchartsPromise=null)})}return this.sharedHighchartsPromise}async load(e){let i=await this.ensureSharedHighcharts();return await this.loadModules(e?.modules),this.highcharts.set(i),i}static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})}}return t})();var qT=(()=>{class t{delay(e){return new Promise(i=>setTimeout(i,e))}getChartFactory(e,i){if(i===`chart`)return e.chart;let o=e[i];if(!o)throw new Error(`Highcharts constructor "${i}" is not available. Did you load the required module?`);return o}createChart(){Ft(e=>{let i=this.loadedHighcharts(),a=this.constructorType();if(!i||this.isDestroyed)return;let o=s=>{if(!(s.renderer.forExport||this.isDestroyed))return this.chartInstance.emit(s)},r=this.getChartFactory(i,a)(this.el.nativeElement,nt(()=>this.options()),o);this.chart.set(r),e(()=>{this.chart()===r&&this.chart.set(null),r.destroy()})})}keepChartUpToDate(){let e=null;Ft(()=>{let i=this.chart(),a=this.update(),o=this.oneToOne(),r=this.options();if(i){if(i!==e){e=i;return}a&&i.update(r,!0,o)}})}async initializeHighcharts(){await this.pendingTasks.run(async()=>{try{let e=await this.highchartsChartService.load(this.relativeConfig),i=this.relativeConfig?.timeout??this.timeout??0;i>0&&await this.delay(i),this.isDestroyed||this.loadedHighcharts.set(e)}catch(e){console.error(`Highcharts failed to load; chart was not created.`,e)}})}constructor(){this.constructorType=zt(`chart`),this.oneToOne=zt(!1),this.options=zt.required(),this.update=bx(!0),this.chartInstance=eu(),this.destroyRef=f(Te),this.el=f(W),this.platformId=f(Gi),this.relativeConfig=f(GT,{optional:!0}),this.timeout=f(bC,{optional:!0}),this.highchartsChartService=f(WT),this.pendingTasks=f(xa),this.loadedHighcharts=N(null),this.chart=N(null),this.isDestroyed=!1,!(this.platformId&&Gx(this.platformId))&&(this.destroyRef.onDestroy(()=>{this.isDestroyed=!0}),this.createChart(),this.keepChartUpToDate(),this.initializeHighcharts())}static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵdir=Y({type:t,selectors:[[``,`highchartsChart`,``]],inputs:{constructorType:[1,`constructorType`],oneToOne:[1,`oneToOne`],options:[1,`options`],update:[1,`update`]},outputs:{update:`updateChange`,chartInstance:`chartInstance`}})}}return t})();var vC=(()=>{class t{static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵcmp=B({type:t,selectors:[[`highcharts-chart`]],features:[nc([{directive:qT,inputs:[`constructorType`,`constructorType`,`oneToOne`,`oneToOne`,`options`,`options`,`update`,`update`],outputs:[`chartInstance`,`chartInstance`,`updateChange`,`updateChange`]}])],decls:0,vars:0,template:function(i,a){},encapsulation:2})}}return t})();var YT=()=>[];var KT=()=>import(`./chunk-DsRReT52.js`).then(t=>t.default);function QT(t){return rn([{provide:hC,useValue:t??KT}])}function XT(t){return rn([{provide:gC,useValue:t}])}function ZT(t){return rn([{provide:fC,useValue:t}])}function yC(t={}){let n=[QT(t.instance),ZT(t.modules??YT),{provide:bC,useValue:t.timeout}];return t.options&&n.push(XT(t.options)),rn(n)}var _C={providers:[sh(),Zf(),Dg(),yC({instance:()=>import(`./chunk-DsRReT52.js`).then(t=>t.default),modules:()=>[Promise.all([import(`./chunk-D_HyaF3j.js`).then(()=>import(`./chunk-8NrhPs3X.js`)),import(`./chunk-DVg4tciB.js`),import(`./chunk-hCCa7UBb.js`),import(`./chunk-DfdYbIWf.js`),import(`./chunk-CekEWd3a.js`)]).then(()=>import(`./chunk-HHM3Czrn.js`))]})]};function Eo(t){return t.buttons===0||t.detail===0}function Do(t){let n=t.touches&&t.touches[0]||t.changedTouches&&t.changedTouches[0];return!!n&&n.identifier===-1&&(n.radiusX==null||n.radiusX===1)&&(n.radiusY==null||n.radiusY===1)}var Ig;function xC(){if(Ig==null){let t=typeof document<`u`?document.head:null;Ig=!!(t&&(t.createShadowRoot||t.attachShadow))}return Ig}function Mg(t){if(xC()){let n=t.getRootNode?t.getRootNode():null;if(typeof ShadowRoot<`u`&&ShadowRoot&&n instanceof ShadowRoot)return n}return null}function Cc(){let t=typeof document<`u`&&document?document.activeElement:null;for(;t&&t.shadowRoot;){let n=t.shadowRoot.activeElement;if(n===t)break;t=n}return t}function Yt(t){if(t.composedPath)try{return t.composedPath()[0]}catch{}return t.target}var Ng;try{Ng=typeof Intl<`u`&&Intl.v8BreakIterator}catch{Ng=!1}var Ae=(()=>{class t{_platformId=f(Gi);isBrowser=this._platformId?$x(this._platformId):typeof document==`object`&&!!document;EDGE=this.isBrowser&&/(edge)/i.test(navigator.userAgent);TRIDENT=this.isBrowser&&/(msie|trident)/i.test(navigator.userAgent);BLINK=this.isBrowser&&!!(window.chrome||Ng)&&typeof CSS<`u`&&!this.EDGE&&!this.TRIDENT;WEBKIT=this.isBrowser&&/AppleWebKit/i.test(navigator.userAgent)&&!this.BLINK&&!this.EDGE&&!this.TRIDENT;IOS=this.isBrowser&&/iPad|iPhone|iPod/.test(navigator.userAgent)&&!(`MSStream`in window);FIREFOX=this.isBrowser&&/(firefox|minefield)/i.test(navigator.userAgent);ANDROID=this.isBrowser&&/android/i.test(navigator.userAgent)&&!this.TRIDENT;SAFARI=this.isBrowser&&/safari/i.test(navigator.userAgent)&&this.WEBKIT;static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Sc;function CC(){if(Sc==null&&typeof window<`u`)try{window.addEventListener(`test`,null,Object.defineProperty({},"passive",{get:()=>Sc=!0}))}finally{Sc=Sc||!1}return Sc}function Or(t){return CC()?t:!!t.capture}function si(t,n=0){return SC(t)?Number(t):arguments.length===2?n:0}function SC(t){return!isNaN(parseFloat(t))&&!isNaN(Number(t))}function ln(t){return t instanceof W?t.nativeElement:t}var wC=new I(`cdk-input-modality-detector-options`);var EC={ignoreKeys:[18,17,224,91,16]};var DC=650;var Tg={passive:!0,capture:!0};var kC=(()=>{class t{_platform=f(Ae);_listenerCleanups;modalityDetected;modalityChanged;get mostRecentModality(){return this._modality.value}_mostRecentTarget=null;_modality=new fi(null);_options;_lastTouchMs=0;_onKeydown=e=>{this._options?.ignoreKeys?.some(i=>i===e.keyCode)||(this._modality.next(`keyboard`),this._mostRecentTarget=Yt(e))};_onMousedown=e=>{Date.now()-this._lastTouchMs<DC||(this._modality.next(Eo(e)?`keyboard`:`mouse`),this._mostRecentTarget=Yt(e))};_onTouchstart=e=>{if(Do(e)){this._modality.next(`keyboard`);return}this._lastTouchMs=Date.now(),this._modality.next(`touch`),this._mostRecentTarget=Yt(e)};constructor(){let e=f(Q),i=f(de),a=f(wC,{optional:!0});if(this._options=r(r({},EC),a),this.modalityDetected=this._modality.pipe(Qa(1)),this.modalityChanged=this.modalityDetected.pipe(Rl()),this._platform.isBrowser){let o=f(Mt).createRenderer(null,null);this._listenerCleanups=e.runOutsideAngular(()=>[o.listen(i,`keydown`,this._onKeydown,Tg),o.listen(i,`mousedown`,this._onMousedown,Tg),o.listen(i,`touchstart`,this._onTouchstart,Tg)])}}ngOnDestroy(){this._modality.complete(),this._listenerCleanups?.forEach(e=>e())}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var wc=(function(t){return t[t.IMMEDIATE=0]=`IMMEDIATE`,t[t.EVENTUAL=1]=`EVENTUAL`,t})(wc||{});var IC=new I(`cdk-focus-monitor-default-options`);var Su=Or({passive:!0,capture:!0});var Jt=(()=>{class t{_ngZone=f(Q);_platform=f(Ae);_inputModalityDetector=f(kC);_origin=null;_lastFocusOrigin=null;_windowFocused=!1;_windowFocusTimeoutId;_originTimeoutId;_originFromTouchInteraction=!1;_elementInfo=new Map;_monitoredElementCount=0;_rootNodeFocusListenerCount=new Map;_detectionMode;_windowFocusListener=()=>{this._windowFocused=!0,this._windowFocusTimeoutId=setTimeout(()=>this._windowFocused=!1)};_document=f(de);_stopInputModalityDetector=new L;constructor(){let e=f(IC,{optional:!0});this._detectionMode=e?.detectionMode||wc.IMMEDIATE}_rootNodeFocusAndBlurListener=e=>{let i=Yt(e);for(let a=i;a;a=a.parentElement)e.type===`focus`?this._onFocus(e,a):this._onBlur(e,a)};monitor(e,i=!1){let a=ln(e);if(!this._platform.isBrowser||a.nodeType!==1)return Mn();let o=Mg(a)||this._document,r=this._elementInfo.get(a);if(r)return i&&(r.checkChildren=!0),r.subject;let s={checkChildren:i,subject:new L,rootNode:o};return this._elementInfo.set(a,s),this._registerGlobalListeners(s),s.subject}stopMonitoring(e){let i=ln(e),a=this._elementInfo.get(i);a&&(a.subject.complete(),this._setClasses(i),this._elementInfo.delete(i),this._removeGlobalListeners(a))}focusVia(e,i,a){let o=ln(e);o===this._document.activeElement?this._getClosestElementsInfo(o).forEach(([s,c])=>this._originChanged(s,i,c)):(this._setOrigin(i),typeof o.focus==`function`&&o.focus(a))}ngOnDestroy(){this._elementInfo.forEach((e,i)=>this.stopMonitoring(i))}_getWindow(){return this._document.defaultView||window}_getFocusOrigin(e){return this._origin?this._originFromTouchInteraction?this._shouldBeAttributedToTouch(e)?`touch`:`program`:this._origin:this._windowFocused&&this._lastFocusOrigin?this._lastFocusOrigin:e&&this._isLastInteractionFromInputLabel(e)?`mouse`:`program`}_shouldBeAttributedToTouch(e){return this._detectionMode===wc.EVENTUAL||!!e?.contains(this._inputModalityDetector._mostRecentTarget)}_setClasses(e,i){e.classList.toggle(`cdk-focused`,!!i),e.classList.toggle(`cdk-touch-focused`,i===`touch`),e.classList.toggle(`cdk-keyboard-focused`,i===`keyboard`),e.classList.toggle(`cdk-mouse-focused`,i===`mouse`),e.classList.toggle(`cdk-program-focused`,i===`program`)}_setOrigin(e,i=!1){this._ngZone.runOutsideAngular(()=>{if(this._origin=e,this._originFromTouchInteraction=e===`touch`&&i,this._detectionMode===wc.IMMEDIATE){clearTimeout(this._originTimeoutId);let a=this._originFromTouchInteraction?DC:1;this._originTimeoutId=setTimeout(()=>this._origin=null,a)}})}_onFocus(e,i){let a=this._elementInfo.get(i),o=Yt(e);!a||!a.checkChildren&&i!==o||this._originChanged(i,this._getFocusOrigin(o),a)}_onBlur(e,i){let a=this._elementInfo.get(i);!a||a.checkChildren&&e.relatedTarget instanceof Node&&i.contains(e.relatedTarget)||(this._setClasses(i),this._emitOrigin(a,null))}_emitOrigin(e,i){e.subject.observers.length&&this._ngZone.run(()=>e.subject.next(i))}_registerGlobalListeners(e){if(!this._platform.isBrowser)return;let i=e.rootNode,a=this._rootNodeFocusListenerCount.get(i)||0;a||this._ngZone.runOutsideAngular(()=>{i.addEventListener(`focus`,this._rootNodeFocusAndBlurListener,Su),i.addEventListener(`blur`,this._rootNodeFocusAndBlurListener,Su)}),this._rootNodeFocusListenerCount.set(i,a+1),++this._monitoredElementCount===1&&(this._ngZone.runOutsideAngular(()=>{this._getWindow().addEventListener(`focus`,this._windowFocusListener)}),this._inputModalityDetector.modalityDetected.pipe(Ne(this._stopInputModalityDetector)).subscribe(o=>{this._setOrigin(o,!0)}))}_removeGlobalListeners(e){let i=e.rootNode;if(this._rootNodeFocusListenerCount.has(i)){let a=this._rootNodeFocusListenerCount.get(i);a>1?this._rootNodeFocusListenerCount.set(i,a-1):(i.removeEventListener(`focus`,this._rootNodeFocusAndBlurListener,Su),i.removeEventListener(`blur`,this._rootNodeFocusAndBlurListener,Su),this._rootNodeFocusListenerCount.delete(i))}--this._monitoredElementCount||(this._getWindow().removeEventListener(`focus`,this._windowFocusListener),this._stopInputModalityDetector.next(),clearTimeout(this._windowFocusTimeoutId),clearTimeout(this._originTimeoutId))}_originChanged(e,i,a){this._setClasses(e,i),this._emitOrigin(a,i),this._lastFocusOrigin=i}_getClosestElementsInfo(e){let i=[];return this._elementInfo.forEach((a,o)=>{(o===e||a.checkChildren&&o.contains(e))&&i.push([o,a])}),i}_isLastInteractionFromInputLabel(e){let{_mostRecentTarget:i,mostRecentModality:a}=this._inputModalityDetector;if(a!==`mouse`||!i||i===e||e.nodeName!==`INPUT`&&e.nodeName!==`TEXTAREA`||e.disabled)return!1;let o=e.labels;if(o){for(let r=0;r<o.length;r++)if(o[r].contains(i))return!0}return!1}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Ag=(()=>{class t{_elementRef=f(W);_focusMonitor=f(Jt);_monitorSubscription;_focusOrigin=null;cdkFocusChange=new ee;get focusOrigin(){return this._focusOrigin}ngAfterViewInit(){let e=this._elementRef.nativeElement;this._monitorSubscription=this._focusMonitor.monitor(e,e.nodeType===1&&e.hasAttribute(`cdkMonitorSubtreeFocus`)).subscribe(i=>{this._focusOrigin=i,this.cdkFocusChange.emit(i)})}ngOnDestroy(){this._focusMonitor.stopMonitoring(this._elementRef),this._monitorSubscription?.unsubscribe()}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`cdkMonitorElementFocus`,``],[``,`cdkMonitorSubtreeFocus`,``]],outputs:{cdkFocusChange:`cdkFocusChange`},exportAs:[`cdkMonitorFocus`]})}return t})();var wu=new WeakMap;var Je=(()=>{class t{_appRef;_injector=f(re);_environmentInjector=f(kt);load(e){let i=this._appRef=this._appRef||this._injector.get(Rn),a=wu.get(i);a||(a={loaders:new Set,refs:[]},wu.set(i,a),i.onDestroy(()=>{wu.get(i)?.refs.forEach(o=>o.destroy()),wu.delete(i)})),a.loaders.has(e)||(a.loaders.add(e),a.refs.push(tu(e,{environmentInjector:this._environmentInjector})))}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var ki=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],exportAs:[`cdkVisuallyHidden`],decls:0,vars:0,template:function(i,a){},styles:[`.cdk-visually-hidden {
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
`],encapsulation:2})}return t})();var Eu;function JT(){if(Eu===void 0&&(Eu=null,typeof window<`u`)){let t=window;if(t.trustedTypes!==void 0)try{Eu=t.trustedTypes.createPolicy(`angular#components`,{createHTML:n=>n})}catch(n){console.error(n)}}return Eu}function eA(t){return JT()?.createHTML(t)||t}function MC(t,n,e){t.innerHTML=eA(e.sanitize(_n.HTML,n)||``)}function Rr(t){return Array.isArray(t)?t:[t]}var NC=new Set;var ko;var Pr=(()=>{class t{_platform=f(Ae);_nonce=f(_a,{optional:!0});_matchMedia;constructor(){this._matchMedia=this._platform.isBrowser&&window.matchMedia?window.matchMedia.bind(window):nA}matchMedia(e){return(this._platform.WEBKIT||this._platform.BLINK)&&tA(e,this._nonce),this._matchMedia(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function tA(t,n){if(!NC.has(t))try{ko||(ko=document.createElement(`style`),n&&ko.setAttribute(`nonce`,n),ko.setAttribute(`type`,`text/css`),document.head.appendChild(ko)),ko.sheet&&(ko.sheet.insertRule(`@media ${t.replace(/[{}]/g,``)} {body{ }}`,0),NC.add(t))}catch(e){console.error(e)}}function nA(t){return{matches:t===`all`||t===``,media:t,addListener:()=>{},removeListener:()=>{}}}var Og=(()=>{class t{_mediaMatcher=f(Pr);_zone=f(Q);_queries=new Map;_destroySubject=new L;ngOnDestroy(){this._destroySubject.next(),this._destroySubject.complete()}isMatched(e){return TC(Rr(e)).some(a=>this._registerQuery(a).mql.matches)}observe(e){let o=ap(TC(Rr(e)).map(r=>this._registerQuery(r).observable));return o=Jo(o.pipe(bn(1)),o.pipe(Qa(1),Pi(0))),o.pipe(rt(r=>{let s={matches:!1,breakpoints:{}};return r.forEach(({matches:c,query:p})=>{s.matches=s.matches||c,s.breakpoints[p]=c}),s}))}_registerQuery(e){if(this._queries.has(e))return this._queries.get(e);let i=this._mediaMatcher.matchMedia(e),o={observable:new Ce(r=>{let s=c=>this._zone.run(()=>r.next(c));return i.addListener(s),()=>{i.removeListener(s)}}).pipe(ut(i),rt(({matches:r})=>({query:e,matches:r})),Ne(this._destroySubject)),mql:i};return this._queries.set(e,o),o}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function TC(t){return t.map(n=>n.split(`,`)).reduce((n,e)=>n.concat(e)).map(n=>n.trim())}function iA(t){if(t.type===`characterData`&&t.target instanceof Comment)return!0;if(t.type===`childList`){for(let n=0;n<t.addedNodes.length;n++)if(!(t.addedNodes[n]instanceof Comment))return!1;for(let n=0;n<t.removedNodes.length;n++)if(!(t.removedNodes[n]instanceof Comment))return!1;return!0}return!1}var AC=(()=>{class t{create(e){return typeof MutationObserver>`u`?null:new MutationObserver(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var OC=(()=>{class t{_mutationObserverFactory=f(AC);_observedElements=new Map;_ngZone=f(Q);ngOnDestroy(){this._observedElements.forEach((e,i)=>this._cleanupObserver(i))}observe(e){let i=ln(e);return new Ce(a=>{let r=this._observeElement(i).pipe(rt(s=>s.filter(c=>!iA(c))),Qe(s=>!!s.length)).subscribe(s=>{this._ngZone.run(()=>{a.next(s)})});return()=>{r.unsubscribe(),this._unobserveElement(i)}})}_observeElement(e){return this._ngZone.runOutsideAngular(()=>{if(this._observedElements.has(e))this._observedElements.get(e).count++;else{let i=new L,a=this._mutationObserverFactory.create(o=>i.next(o));a&&a.observe(e,{characterData:!0,childList:!0,subtree:!0}),this._observedElements.set(e,{observer:a,stream:i,count:1})}return this._observedElements.get(e).stream})}_unobserveElement(e){this._observedElements.has(e)&&(this._observedElements.get(e).count--,this._observedElements.get(e).count||this._cleanupObserver(e))}_cleanupObserver(e){if(this._observedElements.has(e)){let{observer:i,stream:a}=this._observedElements.get(e);i&&i.disconnect(),a.complete(),this._observedElements.delete(e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var RC=(()=>{class t{_contentObserver=f(OC);_elementRef=f(W);event=new ee;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._disabled?this._unsubscribe():this._subscribe()}_disabled=!1;get debounce(){return this._debounce}set debounce(e){this._debounce=si(e),this._subscribe()}_debounce;_currentSubscription=null;ngAfterContentInit(){!this._currentSubscription&&!this.disabled&&this._subscribe()}ngOnDestroy(){this._unsubscribe()}_subscribe(){this._unsubscribe();let e=this._contentObserver.observe(this._elementRef);this._currentSubscription=(this.debounce?e.pipe(Pi(this.debounce)):e).subscribe(this.event)}_unsubscribe(){this._currentSubscription?.unsubscribe()}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`cdkObserveContent`,``]],inputs:{disabled:[2,`cdkObserveContentDisabled`,`disabled`,V],debounce:`debounce`},outputs:{event:`cdkObserveContent`},exportAs:[`cdkObserveContent`]})}return t})();var Du=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({providers:[AC]})}return t})();var Fr=(()=>{class t{_platform=f(Ae);isDisabled(e){return e.hasAttribute(`disabled`)}isVisible(e){return oA(e)&&getComputedStyle(e).visibility===`visible`}isTabbable(e){if(!this._platform.isBrowser)return!1;let i=aA(pA(e));if(i&&(PC(i)===-1||!this.isVisible(i)))return!1;let a=e.nodeName.toLowerCase(),o=PC(e);return e.hasAttribute(`contenteditable`)?o!==-1:a===`iframe`||a===`object`||this._platform.WEBKIT&&this._platform.IOS&&!uA(e)?!1:a===`audio`?e.hasAttribute(`controls`)?o!==-1:!1:a===`video`?o===-1?!1:o!==null?!0:this._platform.FIREFOX||e.hasAttribute(`controls`):e.tabIndex>=0}isFocusable(e,i){return mA(e)&&!this.isDisabled(e)&&(i?.ignoreVisibility||this.isVisible(e))}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function aA(t){try{return t.frameElement}catch{return null}}function oA(t){return!!(t.offsetWidth||t.offsetHeight||typeof t.getClientRects==`function`&&t.getClientRects().length)}function rA(t){let n=t.nodeName.toLowerCase();return n===`input`||n===`select`||n===`button`||n===`textarea`}function sA(t){return lA(t)&&t.type==`hidden`}function cA(t){return dA(t)&&t.hasAttribute(`href`)}function lA(t){return t.nodeName.toLowerCase()==`input`}function dA(t){return t.nodeName.toLowerCase()==`a`}function VC(t){if(!t.hasAttribute(`tabindex`)||t.tabIndex===void 0)return!1;let n=t.getAttribute(`tabindex`);return!!(n&&!isNaN(parseInt(n,10)))}function PC(t){if(!VC(t))return null;let n=parseInt(t.getAttribute(`tabindex`)||``,10);return isNaN(n)?-1:n}function uA(t){let n=t.nodeName.toLowerCase(),e=n===`input`&&t.type;return e===`text`||e===`password`||n===`select`||n===`textarea`}function mA(t){return sA(t)?!1:rA(t)||cA(t)||t.hasAttribute(`contenteditable`)||VC(t)}function pA(t){return t.ownerDocument&&t.ownerDocument.defaultView||window}var ku=class{_element;_checker;_ngZone;_document;_injector;_startAnchor=null;_endAnchor=null;_hasAttached=!1;startAnchorListener=()=>{!this.focusLastTabbableElement()&&this._checker.isFocusable(this._element)&&this._element.focus()};endAnchorListener=()=>{!this.focusFirstTabbableElement()&&this._checker.isFocusable(this._element)&&this._element.focus()};get enabled(){return this._enabled}set enabled(n){this._enabled=n,this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_enabled=!0;constructor(n,e,i,a,o=!1,r){this._element=n,this._checker=e,this._ngZone=i,this._document=a,this._injector=r,o||this.attachAnchors()}destroy(){let n=this._startAnchor,e=this._endAnchor;n&&(n.removeEventListener(`focus`,this.startAnchorListener),n.remove()),e&&(e.removeEventListener(`focus`,this.endAnchorListener),e.remove()),this._startAnchor=this._endAnchor=null,this._hasAttached=!1}attachAnchors(){return this._hasAttached?!0:(this._ngZone.runOutsideAngular(()=>{this._startAnchor||(this._startAnchor=this._createAnchor(),this._startAnchor.addEventListener(`focus`,this.startAnchorListener)),this._endAnchor||(this._endAnchor=this._createAnchor(),this._endAnchor.addEventListener(`focus`,this.endAnchorListener))}),this._element.parentNode&&(this._element.parentNode.insertBefore(this._startAnchor,this._element),this._element.parentNode.insertBefore(this._endAnchor,this._element.nextSibling),this._hasAttached=!0),this._hasAttached)}focusInitialElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusInitialElement(n)))})}focusFirstTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusFirstTabbableElement(n)))})}focusLastTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusLastTabbableElement(n)))})}_getRegionBoundary(n){let e=this._element.querySelectorAll(`[cdk-focus-region-${n}], [cdkFocusRegion${n}], [cdk-focus-${n}]`);return n==`start`?e.length?e[0]:this._getFirstTabbableElement(this._element):e.length?e[e.length-1]:this._getLastTabbableElement(this._element)}focusInitialElement(n){let e=this._element.querySelector(`[cdk-focus-initial], [cdkFocusInitial]`);if(e){if(!this._checker.isFocusable(e)){let i=this._getFirstTabbableElement(e);return i?.focus(n),!!i}return e.focus(n),!0}return this.focusFirstTabbableElement(n)}focusFirstTabbableElement(n){let e=this._getRegionBoundary(`start`);return e&&e.focus(n),!!e}focusLastTabbableElement(n){let e=this._getRegionBoundary(`end`);return e&&e.focus(n),!!e}hasAttached(){return this._hasAttached}_getFirstTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let i=0;i<e.length;i++){let a=e[i].nodeType===this._document.ELEMENT_NODE?this._getFirstTabbableElement(e[i]):null;if(a)return a}return null}_getLastTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let i=e.length-1;i>=0;i--){let a=e[i].nodeType===this._document.ELEMENT_NODE?this._getLastTabbableElement(e[i]):null;if(a)return a}return null}_createAnchor(){let n=this._document.createElement(`div`);return this._toggleAnchorTabIndex(this._enabled,n),n.classList.add(`cdk-visually-hidden`),n.classList.add(`cdk-focus-trap-anchor`),n.setAttribute(`aria-hidden`,`true`),n}_toggleAnchorTabIndex(n,e){n?e.setAttribute(`tabindex`,`0`):e.removeAttribute(`tabindex`)}toggleAnchors(n){this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_executeOnStable(n){Tt(n,{injector:this._injector})}};var Pg=(()=>{class t{_checker=f(Fr);_ngZone=f(Q);_document=f(de);_injector=f(re);constructor(){f(Je).load(ki)}create(e,i=!1){return new ku(e,this._checker,this._ngZone,this._document,i,this._injector)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var BC=new I(`liveAnnouncerElement`,{providedIn:`root`,factory:()=>null});var jC=new I(`LIVE_ANNOUNCER_DEFAULT_OPTIONS`);var hA=0;var Fg=(()=>{class t{_ngZone=f(Q);_defaultOptions=f(jC,{optional:!0});_liveElement;_document=f(de);_sanitizer=f(kg);_previousTimeout;_currentPromise;_currentResolve;constructor(){let e=f(BC,{optional:!0});this._liveElement=e||this._createLiveElement()}announce(e,...i){let a=this._defaultOptions,o,r;return i.length===1&&typeof i[0]==`number`?r=i[0]:[o,r]=i,this.clear(),clearTimeout(this._previousTimeout),o||(o=a&&a.politeness?a.politeness:`polite`),r==null&&a&&(r=a.duration),this._liveElement.setAttribute(`aria-live`,o),this._liveElement.id&&this._exposeAnnouncerToModals(this._liveElement.id),this._ngZone.runOutsideAngular(()=>(this._currentPromise||(this._currentPromise=new Promise(s=>this._currentResolve=s)),clearTimeout(this._previousTimeout),this._previousTimeout=setTimeout(()=>{!e||typeof e==`string`?this._liveElement.textContent=e:MC(this._liveElement,e,this._sanitizer),typeof r==`number`&&(this._previousTimeout=setTimeout(()=>this.clear(),r)),this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0},100),this._currentPromise))}clear(){this._liveElement&&(this._liveElement.textContent=``)}ngOnDestroy(){clearTimeout(this._previousTimeout),this._liveElement?.remove(),this._liveElement=null,this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0}_createLiveElement(){let e=`cdk-live-announcer-element`,i=this._document.getElementsByClassName(e),a=this._document.createElement(`div`);for(let o=0;o<i.length;o++)i[o].remove();return a.classList.add(e),a.classList.add(`cdk-visually-hidden`),a.setAttribute(`aria-atomic`,`true`),a.setAttribute(`aria-live`,`polite`),a.id=`cdk-live-announcer-${hA++}`,this._document.body.appendChild(a),a}_exposeAnnouncerToModals(e){let i=this._document.querySelectorAll(`body > .cdk-overlay-container [aria-modal="true"]`);for(let a=0;a<i.length;a++){let o=i[a],r=o.getAttribute(`aria-owns`);r?r.indexOf(e)===-1&&o.setAttribute(`aria-owns`,r+` `+e):o.setAttribute(`aria-owns`,e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Da=(function(t){return t[t.NONE=0]=`NONE`,t[t.BLACK_ON_WHITE=1]=`BLACK_ON_WHITE`,t[t.WHITE_ON_BLACK=2]=`WHITE_ON_BLACK`,t})(Da||{});var FC=`cdk-high-contrast-black-on-white`;var LC=`cdk-high-contrast-white-on-black`;var Rg=`cdk-high-contrast-active`;var zC=(()=>{class t{_platform=f(Ae);_hasCheckedHighContrastMode=!1;_document=f(de);_breakpointSubscription;constructor(){this._breakpointSubscription=f(Og).observe(`(forced-colors: active)`).subscribe(()=>{this._hasCheckedHighContrastMode&&(this._hasCheckedHighContrastMode=!1,this._applyBodyHighContrastModeCssClasses())})}getHighContrastMode(){if(!this._platform.isBrowser)return Da.NONE;let e=this._document.createElement(`div`);e.style.backgroundColor=`rgb(1,2,3)`,e.style.position=`absolute`,this._document.body.appendChild(e);let i=this._document.defaultView||window,a=i&&i.getComputedStyle?i.getComputedStyle(e):null,o=(a&&a.backgroundColor||``).replace(/ /g,``);switch(e.remove(),o){case`rgb(0,0,0)`:case`rgb(45,50,54)`:case`rgb(32,32,32)`:return Da.WHITE_ON_BLACK;case`rgb(255,255,255)`:case`rgb(255,250,239)`:return Da.BLACK_ON_WHITE}return Da.NONE}ngOnDestroy(){this._breakpointSubscription.unsubscribe()}_applyBodyHighContrastModeCssClasses(){if(!this._hasCheckedHighContrastMode&&this._platform.isBrowser&&this._document.body){let e=this._document.body.classList;e.remove(Rg,FC,LC),this._hasCheckedHighContrastMode=!0;let i=this.getHighContrastMode();i===Da.BLACK_ON_WHITE?e.add(Rg,FC):i===Da.WHITE_ON_BLACK&&e.add(Rg,LC)}}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Io=(()=>{class t{constructor(){f(zC)._applyBodyHighContrastModeCssClasses()}static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[Du]})}return t})();var fA=200;var Iu=class{_letterKeyStream=new L;_items=[];_selectedItemIndex=-1;_pressedLetters=[];_skipPredicateFn;_selectedItem=new L;selectedItem=this._selectedItem;constructor(n,e){let i=typeof e?.debounceInterval==`number`?e.debounceInterval:fA;e?.skipPredicate&&(this._skipPredicateFn=e.skipPredicate),this.setItems(n),this._setupKeyHandler(i)}destroy(){this._pressedLetters=[],this._letterKeyStream.complete(),this._selectedItem.complete()}setCurrentSelectedItemIndex(n){this._selectedItemIndex=n}setItems(n){this._items=n}handleKey(n){let e=n.keyCode;n.key&&n.key.length===1?this._letterKeyStream.next(n.key.toLocaleUpperCase()):(e>=65&&e<=90||e>=48&&e<=57)&&this._letterKeyStream.next(String.fromCharCode(e))}isTyping(){return this._pressedLetters.length>0}reset(){this._pressedLetters=[]}_setupKeyHandler(n){this._letterKeyStream.pipe(dp(e=>this._pressedLetters.push(e)),Pi(n),Qe(()=>this._pressedLetters.length>0),rt(()=>this._pressedLetters.join(``).toLocaleUpperCase())).subscribe(e=>{for(let i=1;i<this._items.length+1;i++){let a=(this._selectedItemIndex+i)%this._items.length,o=this._items[a];if(!this._skipPredicateFn?.(o)&&o.getLabel?.().toLocaleUpperCase().trim().indexOf(e)===0){this._selectedItem.next(o);break}}this._pressedLetters=[]})}};function xt(t,...n){return n.length?n.some(e=>t[e]):t.altKey||t.shiftKey||t.ctrlKey||t.metaKey}var Lr=class{_items;_activeItemIndex=N(-1);_activeItem=N(null);_wrap=!1;_typeaheadSubscription=De.EMPTY;_itemChangesSubscription;_vertical=!0;_horizontal=null;_allowedModifierKeys=[];_homeAndEnd=!1;_pageUpAndDown={enabled:!1,delta:10};_effectRef;_typeahead;_skipPredicateFn=n=>n.disabled;constructor(n,e){this._items=n,n instanceof Sn?this._itemChangesSubscription=n.changes.subscribe(i=>this._itemsChanged(i.toArray())):sn(n)&&(this._effectRef=Ft(()=>this._itemsChanged(n()),{injector:e}))}tabOut=new L;change=new L;skipPredicate(n){return this._skipPredicateFn=n,this}withWrap(n=!0){return this._wrap=n,this}withVerticalOrientation(n=!0){return this._vertical=n,this}withHorizontalOrientation(n){return this._horizontal=n,this}withAllowedModifierKeys(n){return this._allowedModifierKeys=n,this}withTypeAhead(n=200){this._typeaheadSubscription.unsubscribe();let e=this._getItemsArray();return this._typeahead=new Iu(e,{debounceInterval:typeof n==`number`?n:void 0,skipPredicate:i=>this._skipPredicateFn(i)}),this._typeaheadSubscription=this._typeahead.selectedItem.subscribe(i=>{this.setActiveItem(i)}),this}cancelTypeahead(){return this._typeahead?.reset(),this}withHomeAndEnd(n=!0){return this._homeAndEnd=n,this}withPageUpDown(n=!0,e=10){return this._pageUpAndDown={enabled:n,delta:e},this}setActiveItem(n){let e=this._activeItem();this.updateActiveItem(n),this._activeItem()!==e&&this.change.next(this._activeItemIndex())}onKeydown(n){let e=n.keyCode,a=[`altKey`,`ctrlKey`,`metaKey`,`shiftKey`].every(o=>!n[o]||this._allowedModifierKeys.indexOf(o)>-1);switch(e){case 9:this.tabOut.next();return;case 40:if(this._vertical&&a){this.setNextItemActive();break}else return;case 38:if(this._vertical&&a){this.setPreviousItemActive();break}else return;case 39:if(this._horizontal&&a){this._horizontal===`rtl`?this.setPreviousItemActive():this.setNextItemActive();break}else return;case 37:if(this._horizontal&&a){this._horizontal===`rtl`?this.setNextItemActive():this.setPreviousItemActive();break}else return;case 36:if(this._homeAndEnd&&a){this.setFirstItemActive();break}else return;case 35:if(this._homeAndEnd&&a){this.setLastItemActive();break}else return;case 33:if(this._pageUpAndDown.enabled&&a){let o=this._activeItemIndex()-this._pageUpAndDown.delta;this._setActiveItemByIndex(o>0?o:0,1);break}else return;case 34:if(this._pageUpAndDown.enabled&&a){let o=this._activeItemIndex()+this._pageUpAndDown.delta,r=this._getItemsArray().length;this._setActiveItemByIndex(o<r?o:r-1,-1);break}else return;default:(a||xt(n,`shiftKey`))&&this._typeahead?.handleKey(n);return}this._typeahead?.reset(),n.preventDefault()}get activeItemIndex(){return this._activeItemIndex()}get activeItem(){return this._activeItem()}isTyping(){return!!this._typeahead&&this._typeahead.isTyping()}setFirstItemActive(){this._setActiveItemByIndex(0,1)}setLastItemActive(){this._setActiveItemByIndex(this._getItemsArray().length-1,-1)}setNextItemActive(){this._activeItemIndex()<0?this.setFirstItemActive():this._setActiveItemByDelta(1)}setPreviousItemActive(){this._activeItemIndex()<0&&this._wrap?this.setLastItemActive():this._setActiveItemByDelta(-1)}updateActiveItem(n){let e=this._getItemsArray(),i=typeof n==`number`?n:e.indexOf(n),a=e[i];this._activeItem.set(a??null),this._activeItemIndex.set(i),this._typeahead?.setCurrentSelectedItemIndex(i)}destroy(){this._typeaheadSubscription.unsubscribe(),this._itemChangesSubscription?.unsubscribe(),this._effectRef?.destroy(),this._typeahead?.destroy(),this.tabOut.complete(),this.change.complete()}_setActiveItemByDelta(n){this._wrap?this._setActiveInWrapMode(n):this._setActiveInDefaultMode(n)}_setActiveInWrapMode(n){let e=this._getItemsArray();for(let i=1;i<=e.length;i++){let a=(this._activeItemIndex()+n*i+e.length)%e.length,o=e[a];if(!this._skipPredicateFn(o)){this.setActiveItem(a);return}}}_setActiveInDefaultMode(n){this._setActiveItemByIndex(this._activeItemIndex()+n,n)}_setActiveItemByIndex(n,e){let i=this._getItemsArray();if(i[n]){for(;this._skipPredicateFn(i[n]);)if(n+=e,!i[n])return;this.setActiveItem(n)}}_getItemsArray(){return sn(this._items)?this._items():this._items instanceof Sn?this._items.toArray():this._items}_itemsChanged(n){this._typeahead?.setItems(n);let e=this._activeItem();if(e){let i=n.indexOf(e);i>-1&&i!==this._activeItemIndex()&&(this._activeItemIndex.set(i),this._typeahead?.setCurrentSelectedItemIndex(i))}}};var Ec=class extends Lr{setActiveItem(n){this.activeItem&&this.activeItem.setInactiveStyles(),super.setActiveItem(n),this.activeItem&&this.activeItem.setActiveStyles()}};var ea=class extends Lr{_origin=`program`;setFocusOrigin(n){return this._origin=n,this}setActiveItem(n){super.setActiveItem(n),this.activeItem&&this.activeItem.focus(this._origin)}};var UC=new Map;var Be=class t{_appId=f($i);static _infix=`a${Math.floor(Math.random()*1e5).toString()}`;getId(n,e=!1){this._appId!==`ng`&&(n+=this._appId);let i=UC.get(n);return i===void 0?i=0:i++,UC.set(n,i),`${n}${e?t._infix+`-`:``}${i}`}static ɵfac=function(e){return new(e||t)};static ɵprov=se({token:t,factory:t.ɵfac})};var GC=` `;function gA(t,n,e){let i=Nu(t,n);e=e.trim(),!i.some(a=>a.trim()===e)&&(i.push(e),t.setAttribute(n,i.join(GC)))}function bA(t,n,e){let i=Nu(t,n);e=e.trim();let a=i.filter(o=>o!==e);a.length?t.setAttribute(n,a.join(GC)):t.removeAttribute(n)}function Nu(t,n){return t.getAttribute(n)?.match(/\S+/g)??[]}var WC=`cdk-describedby-message`;var Mu=`cdk-describedby-host`;var Vg=0;var Tu=(()=>{class t{_platform=f(Ae);_document=f(de);_messageRegistry=new Map;_messagesContainer=null;_id=`${Vg++}`;constructor(){f(Je).load(ki),this._id=f($i)+`-`+Vg++}describe(e,i,a){if(!this._canBeDescribed(e,i))return;let o=Lg(i,a);typeof i!=`string`?($C(i,this._id),this._messageRegistry.set(o,{messageElement:i,referenceCount:0})):this._messageRegistry.has(o)||this._createMessageElement(i,a),this._isElementDescribedByMessage(e,o)||this._addMessageReference(e,o)}removeDescription(e,i,a){if(!i||!this._isElementNode(e))return;let o=Lg(i,a);if(this._isElementDescribedByMessage(e,o)&&this._removeMessageReference(e,o),typeof i==`string`){let r=this._messageRegistry.get(o);r&&r.referenceCount===0&&this._deleteMessageElement(o)}this._messagesContainer?.childNodes.length===0&&(this._messagesContainer.remove(),this._messagesContainer=null)}ngOnDestroy(){let e=this._document.querySelectorAll(`[${Mu}="${this._id}"]`);for(let i=0;i<e.length;i++)this._removeCdkDescribedByReferenceIds(e[i]),e[i].removeAttribute(Mu);this._messagesContainer?.remove(),this._messagesContainer=null,this._messageRegistry.clear()}_createMessageElement(e,i){let a=this._document.createElement(`div`);$C(a,this._id),a.textContent=e,i&&a.setAttribute(`role`,i),this._createMessagesContainer(),this._messagesContainer.appendChild(a),this._messageRegistry.set(Lg(e,i),{messageElement:a,referenceCount:0})}_deleteMessageElement(e){this._messageRegistry.get(e)?.messageElement?.remove(),this._messageRegistry.delete(e)}_createMessagesContainer(){if(this._messagesContainer)return;let e=`cdk-describedby-message-container`,i=this._document.querySelectorAll(`.${e}[platform="server"]`);for(let o=0;o<i.length;o++)i[o].remove();let a=this._document.createElement(`div`);a.style.visibility=`hidden`,a.classList.add(e),a.classList.add(`cdk-visually-hidden`),this._platform.isBrowser||a.setAttribute(`platform`,`server`),this._document.body.appendChild(a),this._messagesContainer=a}_removeCdkDescribedByReferenceIds(e){let i=Nu(e,`aria-describedby`).filter(a=>a.indexOf(WC)!=0);e.setAttribute(`aria-describedby`,i.join(` `))}_addMessageReference(e,i){let a=this._messageRegistry.get(i);gA(e,`aria-describedby`,a.messageElement.id),e.setAttribute(Mu,this._id),a.referenceCount++}_removeMessageReference(e,i){let a=this._messageRegistry.get(i);a.referenceCount--,bA(e,`aria-describedby`,a.messageElement.id),e.removeAttribute(Mu)}_isElementDescribedByMessage(e,i){let a=Nu(e,`aria-describedby`),o=this._messageRegistry.get(i),r=o&&o.messageElement.id;return!!r&&a.indexOf(r)!=-1}_canBeDescribed(e,i){if(!this._isElementNode(e))return!1;if(i&&typeof i==`object`)return!0;let a=i==null?``:`${i}`.trim(),o=e.getAttribute(`aria-label`);return a?!o||o.trim()!==a:!1}_isElementNode(e){return e.nodeType===this._document.ELEMENT_NODE}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function Lg(t,n){return typeof t==`string`?`${n||``}/${t}`:t}function $C(t,n){t.id||(t.id=`${WC}-${n}-${Vg++}`)}var ci=(function(t){return t[t.NORMAL=0]=`NORMAL`,t[t.NEGATED=1]=`NEGATED`,t[t.INVERTED=2]=`INVERTED`,t})(ci||{});var Au;var Oo;function Ou(){if(Oo==null){if(typeof document!=`object`||!document||typeof Element!=`function`||!Element)return Oo=!1,Oo;if(document.documentElement?.style&&`scrollBehavior`in document.documentElement.style)Oo=!0;else{let t=Element.prototype.scrollTo;t?Oo=!/\{\s*\[native code\]\s*\}/.test(t.toString()):Oo=!1}}return Oo}function Vr(){if(typeof document!=`object`||!document)return ci.NORMAL;if(Au==null){let t=document.createElement(`div`),n=t.style;t.dir=`rtl`,n.width=`1px`,n.overflow=`auto`,n.visibility=`hidden`,n.pointerEvents=`none`,n.position=`absolute`;let e=document.createElement(`div`),i=e.style;i.width=`2px`,i.height=`1px`,t.appendChild(e),document.body.appendChild(t),Au=ci.NORMAL,t.scrollLeft===0&&(t.scrollLeft=1,Au=t.scrollLeft===0?ci.NEGATED:ci.INVERTED),t.remove()}return Au}function Bg(){return typeof __karma__<`u`&&!!__karma__||typeof jasmine<`u`&&!!jasmine||typeof jest<`u`&&!!jest||typeof Mocha<`u`&&!!Mocha}var Br;var qC=[`color`,`button`,`checkbox`,`date`,`datetime-local`,`email`,`file`,`hidden`,`image`,`month`,`number`,`password`,`radio`,`range`,`reset`,`search`,`submit`,`tel`,`text`,`time`,`url`,`week`];function jg(){if(Br)return Br;if(typeof document!=`object`||!document)return Br=new Set(qC),Br;let t=document.createElement(`input`);return Br=new Set(qC.filter(n=>(t.setAttribute(`type`,n),t.type===n))),Br}var vA=new I(`MATERIAL_ANIMATIONS`);var YC=null;function Dc(){return f(vA,{optional:!0})?.animationsDisabled||f(Ls,{optional:!0})===`NoopAnimations`?`di-disabled`:(YC??=f(Pr).matchMedia(`(prefers-reduced-motion)`).matches,YC?`reduced-motion`:`enabled`)}function Ue(){return Dc()!==`enabled`}function Et(t){return t==null?``:typeof t==`string`?t:`${t}px`}function ta(t){return t!=null&&`${t}`!=`false`}var Vn=(function(t){return t[t.FADING_IN=0]=`FADING_IN`,t[t.VISIBLE=1]=`VISIBLE`,t[t.FADING_OUT=2]=`FADING_OUT`,t[t.HIDDEN=3]=`HIDDEN`,t})(Vn||{});var zg=class{_renderer;element;config;_animationForciblyDisabledThroughCss;state=Vn.HIDDEN;constructor(n,e,i,a=!1){this._renderer=n,this.element=e,this.config=i,this._animationForciblyDisabledThroughCss=a}fadeOut(){this._renderer.fadeOutRipple(this)}};var KC=Or({passive:!0,capture:!0});var Hg=class{_events=new Map;addHandler(n,e,i,a){let o=this._events.get(e);if(o){let r=o.get(i);r?r.add(a):o.set(i,new Set([a]))}else this._events.set(e,new Map([[i,new Set([a])]])),n.runOutsideAngular(()=>{document.addEventListener(e,this._delegateEventHandler,KC)})}removeHandler(n,e,i){let a=this._events.get(n);if(!a)return;let o=a.get(e);o&&(o.delete(i),o.size===0&&a.delete(e),a.size===0&&(this._events.delete(n),document.removeEventListener(n,this._delegateEventHandler,KC)))}_delegateEventHandler=n=>{let e=Yt(n);e&&this._events.get(n.type)?.forEach((i,a)=>{(a===e||a.contains(e))&&i.forEach(o=>o.handleEvent(n))})}};var kc={enterDuration:225,exitDuration:150};var yA=800;var QC=Or({passive:!0,capture:!0});var XC=[`mousedown`,`touchstart`];var ZC=[`mouseup`,`mouseleave`,`touchend`,`touchcancel`];var _A=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],hostAttrs:[`mat-ripple-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`.mat-ripple {
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
`],encapsulation:2})}return t})();var Ic=class t{_target;_ngZone;_platform;_containerElement;_triggerElement=null;_isPointerDown=!1;_activeRipples=new Map;_mostRecentTransientRipple=null;_lastTouchStartEvent;_pointerUpEventsRegistered=!1;_containerRect=null;static _eventManager=new Hg;constructor(n,e,i,a,o){this._target=n,this._ngZone=e,this._platform=a,a.isBrowser&&(this._containerElement=ln(i)),o&&o.get(Je).load(_A)}fadeInRipple(n,e,i={}){let a=this._containerRect=this._containerRect||this._containerElement.getBoundingClientRect(),o=r(r({},kc),i.animation);i.centered&&(n=a.left+a.width/2,e=a.top+a.height/2);let r$2=i.radius||xA(n,e,a),s=n-a.left,c=e-a.top,p=o.enterDuration,m=document.createElement(`div`);m.classList.add(`mat-ripple-element`),m.style.left=`${s-r$2}px`,m.style.top=`${c-r$2}px`,m.style.height=`${r$2*2}px`,m.style.width=`${r$2*2}px`,i.color!=null&&(m.style.backgroundColor=i.color),m.style.transitionDuration=`${p}ms`,this._containerElement.appendChild(m);let g=window.getComputedStyle(m),v=g.transitionProperty,b=g.transitionDuration,_=v===`none`||b===`0s`||b===`0s, 0s`||a.width===0&&a.height===0,E=new zg(this,m,i,_);m.style.transform=`scale3d(1, 1, 1)`,E.state=Vn.FADING_IN,i.persistent||(this._mostRecentTransientRipple=E);let P=null;return!_&&(p||o.exitDuration)&&this._ngZone.runOutsideAngular(()=>{let ae=()=>{P&&(P.fallbackTimer=null),clearTimeout(x),this._finishRippleTransition(E)},j=()=>this._destroyRipple(E),x=setTimeout(j,p+100);m.addEventListener(`transitionend`,ae),m.addEventListener(`transitioncancel`,j),P={onTransitionEnd:ae,onTransitionCancel:j,fallbackTimer:x}}),this._activeRipples.set(E,P),(_||!p)&&this._finishRippleTransition(E),E}fadeOutRipple(n){if(n.state===Vn.FADING_OUT||n.state===Vn.HIDDEN)return;let e=n.element,i=r(r({},kc),n.config.animation);e.style.transitionDuration=`${i.exitDuration}ms`,e.style.opacity=`0`,n.state=Vn.FADING_OUT,(n._animationForciblyDisabledThroughCss||!i.exitDuration)&&this._finishRippleTransition(n)}fadeOutAll(){this._getActiveRipples().forEach(n=>n.fadeOut())}fadeOutAllNonPersistent(){this._getActiveRipples().forEach(n=>{n.config.persistent||n.fadeOut()})}setupTriggerEvents(n){let e=ln(n);!this._platform.isBrowser||!e||e===this._triggerElement||(this._removeTriggerEvents(),this._triggerElement=e,XC.forEach(i=>{t._eventManager.addHandler(this._ngZone,i,e,this)}))}handleEvent(n){n.type===`mousedown`?this._onMousedown(n):n.type===`touchstart`?this._onTouchStart(n):this._onPointerUp(),this._pointerUpEventsRegistered||(this._ngZone.runOutsideAngular(()=>{ZC.forEach(e=>{this._triggerElement.addEventListener(e,this,QC)})}),this._pointerUpEventsRegistered=!0)}_finishRippleTransition(n){n.state===Vn.FADING_IN?this._startFadeOutTransition(n):n.state===Vn.FADING_OUT&&this._destroyRipple(n)}_startFadeOutTransition(n){let e=n===this._mostRecentTransientRipple,{persistent:i}=n.config;n.state=Vn.VISIBLE,!i&&(!e||!this._isPointerDown)&&n.fadeOut()}_destroyRipple(n){let e=this._activeRipples.get(n)??null;this._activeRipples.delete(n),this._activeRipples.size||(this._containerRect=null),n===this._mostRecentTransientRipple&&(this._mostRecentTransientRipple=null),n.state=Vn.HIDDEN,e!==null&&(n.element.removeEventListener(`transitionend`,e.onTransitionEnd),n.element.removeEventListener(`transitioncancel`,e.onTransitionCancel),e.fallbackTimer!==null&&clearTimeout(e.fallbackTimer)),n.element.remove()}_onMousedown(n){let e=Eo(n),i=this._lastTouchStartEvent&&Date.now()<this._lastTouchStartEvent+yA;!this._target.rippleDisabled&&!e&&!i&&(this._isPointerDown=!0,this.fadeInRipple(n.clientX,n.clientY,this._target.rippleConfig))}_onTouchStart(n){if(!this._target.rippleDisabled&&!Do(n)){this._lastTouchStartEvent=Date.now(),this._isPointerDown=!0;let e=n.changedTouches;if(e)for(let i=0;i<e.length;i++)this.fadeInRipple(e[i].clientX,e[i].clientY,this._target.rippleConfig)}}_onPointerUp(){this._isPointerDown&&(this._isPointerDown=!1,this._getActiveRipples().forEach(n=>{let e=n.state===Vn.VISIBLE||n.config.terminateOnPointerUp&&n.state===Vn.FADING_IN;!n.config.persistent&&e&&n.fadeOut()}))}_getActiveRipples(){return Array.from(this._activeRipples.keys())}_removeTriggerEvents(){let n=this._triggerElement;n&&(XC.forEach(e=>t._eventManager.removeHandler(e,n,this)),this._pointerUpEventsRegistered&&(ZC.forEach(e=>n.removeEventListener(e,this,QC)),this._pointerUpEventsRegistered=!1))}};function xA(t,n,e){let i=Math.max(Math.abs(t-e.left),Math.abs(t-e.right)),a=Math.max(Math.abs(n-e.top),Math.abs(n-e.bottom));return Math.sqrt(i*i+a*a)}var jr=new I(`mat-ripple-global-options`);var li=(()=>{class t{_elementRef=f(W);_animationsDisabled=Ue();color;unbounded=!1;centered=!1;radius=0;animation;get disabled(){return this._disabled}set disabled(e){e&&this.fadeOutAllNonPersistent(),this._disabled=e,this._setupTriggerEventsIfEnabled()}_disabled=!1;get trigger(){return this._trigger||this._elementRef.nativeElement}set trigger(e){this._trigger=e,this._setupTriggerEventsIfEnabled()}_trigger;_rippleRenderer;_globalOptions;_isInitialized=!1;constructor(){let e=f(Q),i=f(Ae),a=f(jr,{optional:!0}),o=f(re);this._globalOptions=a||{},this._rippleRenderer=new Ic(this,e,this._elementRef,i,o)}ngOnInit(){this._isInitialized=!0,this._setupTriggerEventsIfEnabled()}ngOnDestroy(){this._rippleRenderer._removeTriggerEvents()}fadeOutAll(){this._rippleRenderer.fadeOutAll()}fadeOutAllNonPersistent(){this._rippleRenderer.fadeOutAllNonPersistent()}get rippleConfig(){return{centered:this.centered,radius:this.radius,color:this.color,animation:r(r(r({},this._globalOptions.animation),this._animationsDisabled?{enterDuration:0,exitDuration:0}:{}),this.animation),terminateOnPointerUp:this._globalOptions.terminateOnPointerUp}}get rippleDisabled(){return this.disabled||!!this._globalOptions.disabled}_setupTriggerEventsIfEnabled(){!this.disabled&&this._isInitialized&&this._rippleRenderer.setupTriggerEvents(this.trigger)}launch(e,i=0,a){return typeof e==`number`?this._rippleRenderer.fadeInRipple(e,i,r(r({},this.rippleConfig),a)):this._rippleRenderer.fadeInRipple(0,0,r(r({},this.rippleConfig),e))}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`mat-ripple`,``],[``,`matRipple`,``]],hostAttrs:[1,`mat-ripple`],hostVars:2,hostBindings:function(i,a){i&2&&q(`mat-ripple-unbounded`,a.unbounded)},inputs:{color:[0,`matRippleColor`,`color`],unbounded:[0,`matRippleUnbounded`,`unbounded`],centered:[0,`matRippleCentered`,`centered`],radius:[0,`matRippleRadius`,`radius`],animation:[0,`matRippleAnimation`,`animation`],disabled:[0,`matRippleDisabled`,`disabled`],trigger:[0,`matRippleTrigger`,`trigger`]},exportAs:[`matRipple`]})}return t})();var CA={capture:!0};var SA=[`focus`,`mousedown`,`mouseenter`,`touchstart`];var Ug=`mat-ripple-loader-uninitialized`;var $g=`mat-ripple-loader-class-name`;var JC=`mat-ripple-loader-centered`;var Ru=`mat-ripple-loader-disabled`;var Pu=(()=>{class t{_document=f(de);_animationsDisabled=Ue();_globalRippleOptions=f(jr,{optional:!0});_platform=f(Ae);_ngZone=f(Q);_injector=f(re);_eventCleanups;_hosts=new Map;constructor(){let e=f(Mt).createRenderer(null,null);this._eventCleanups=this._ngZone.runOutsideAngular(()=>SA.map(i=>e.listen(this._document,i,this._onInteraction,CA)))}ngOnDestroy(){let e=this._hosts.keys();for(let i of e)this.destroyRipple(i);this._eventCleanups.forEach(i=>i())}configureRipple(e,i){e.setAttribute(Ug,this._globalRippleOptions?.namespace??``),(i.className||!e.hasAttribute($g))&&e.setAttribute($g,i.className||``),i.centered&&e.setAttribute(JC,``),i.disabled&&e.setAttribute(Ru,``)}setDisabled(e,i){let a=this._hosts.get(e);a?(a.target.rippleDisabled=i,!i&&!a.hasSetUpEvents&&(a.hasSetUpEvents=!0,a.renderer.setupTriggerEvents(e))):i?e.setAttribute(Ru,``):e.removeAttribute(Ru)}_onInteraction=e=>{let i=Yt(e);if(i instanceof HTMLElement){let a=i.closest(`[${Ug}="${this._globalRippleOptions?.namespace??``}"]`);a&&this._createRipple(a)}};_createRipple(e){if(!this._document||this._hosts.has(e))return;e.querySelector(`.mat-ripple`)?.remove();let i=this._document.createElement(`span`);i.classList.add(`mat-ripple`,e.getAttribute($g)),e.append(i);let a=this._globalRippleOptions,o=this._animationsDisabled?0:a?.animation?.enterDuration??kc.enterDuration,r=this._animationsDisabled?0:a?.animation?.exitDuration??kc.exitDuration,s={rippleDisabled:this._animationsDisabled||a?.disabled||e.hasAttribute(Ru),rippleConfig:{centered:e.hasAttribute(JC),terminateOnPointerUp:a?.terminateOnPointerUp,animation:{enterDuration:o,exitDuration:r}}},c=new Ic(s,this._ngZone,i,this._platform,this._injector),p=!s.rippleDisabled;p&&c.setupTriggerEvents(e),this._hosts.set(e,{target:s,renderer:c,hasSetUpEvents:p}),e.removeAttribute(Ug)}destroyRipple(e){let i=this._hosts.get(e);i&&(i.renderer._removeTriggerEvents(),this._hosts.delete(e))}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var dn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`structural-styles`]],decls:0,vars:0,template:function(i,a){},styles:[`.mat-focus-indicator {
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
`],encapsulation:2})}return t})();var wA=new I(`MAT_BUTTON_CONFIG`);function eS(t){return t==null?void 0:ct(t)}var Gg=(()=>{class t{_elementRef=f(W);_ngZone=f(Q);_animationsDisabled=Ue();_config=f(wA,{optional:!0});_focusMonitor=f(Jt);_cleanupClick;_renderer=f(Oe);_rippleLoader=f(Pu);_isAnchor;_isFab=!1;color;get disableRipple(){return this._disableRipple}set disableRipple(e){this._disableRipple=e,this._updateRippleDisabled()}_disableRipple=!1;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._updateRippleDisabled()}_disabled=!1;ariaDisabled;disabledInteractive;tabIndex;set _tabindex(e){this.tabIndex=e}showProgress=zt(!1,{transform:V});constructor(){f(Je).load(dn);let e=this._elementRef.nativeElement;this._isAnchor=e.tagName===`A`,this.disabledInteractive=this._config?.disabledInteractive??!1,this.color=this._config?.color??null,this._rippleLoader?.configureRipple(e,{className:`mat-mdc-button-ripple`})}ngAfterViewInit(){this._focusMonitor.monitor(this._elementRef,!0),this._isAnchor&&this._setupAsAnchor()}ngOnDestroy(){this._cleanupClick?.(),this._focusMonitor.stopMonitoring(this._elementRef),this._rippleLoader?.destroyRipple(this._elementRef.nativeElement)}focus(e=`program`,i){e?this._focusMonitor.focusVia(this._elementRef.nativeElement,e,i):this._elementRef.nativeElement.focus(i)}_getAriaDisabled(){return this.ariaDisabled!=null?this.ariaDisabled:this._isAnchor?this.disabled||null:this.disabled&&this.disabledInteractive?!0:null}_getDisabledAttribute(){return this.disabledInteractive||!this.disabled?null:!0}_updateRippleDisabled(){this._rippleLoader?.setDisabled(this._elementRef.nativeElement,this.disableRipple||this.disabled)}_getTabIndex(){return this._isAnchor?this.disabled&&!this.disabledInteractive?-1:this.tabIndex:this.tabIndex}_setupAsAnchor(){this._cleanupClick=this._ngZone.runOutsideAngular(()=>this._renderer.listen(this._elementRef.nativeElement,`click`,e=>{this.disabled&&(e.preventDefault(),e.stopImmediatePropagation())}))}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,hostAttrs:[1,`mat-mdc-button-base`],hostVars:15,hostBindings:function(i,a){i&2&&(te(`disabled`,a._getDisabledAttribute())(`aria-disabled`,a._getAriaDisabled())(`tabindex`,a._getTabIndex()),tt(a.color?`mat-`+a.color:``),q(`mat-mdc-button-progress-indicator-shown`,a.showProgress())(`mat-mdc-button-disabled`,a.disabled)(`mat-mdc-button-disabled-interactive`,a.disabledInteractive)(`mat-unthemed`,!a.color)(`_mat-animation-noopable`,a._animationsDisabled))},inputs:{color:`color`,disableRipple:[2,`disableRipple`,`disableRipple`,V],disabled:[2,`disabled`,`disabled`,V],ariaDisabled:[2,`aria-disabled`,`ariaDisabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],tabIndex:[2,`tabIndex`,`tabIndex`,eS],_tabindex:[2,`tabindex`,`_tabindex`,eS],showProgress:[1,`showProgress`]}})}return t})();var Wg=(()=>{class t extends Gg{constructor(){super(),this._rippleLoader.configureRipple(this._elementRef.nativeElement,{centered:!0})}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[``,`progressIndicator`,``]]],i=[`*`,`[progressIndicator]`];function a(o,r){o&1&&(st(0,`div`,1),me(1,1),pt())}return B({type:t,selectors:[[`button`,`mat-icon-button`,``],[`a`,`mat-icon-button`,``],[`button`,`matIconButton`,``],[`a`,`matIconButton`,``]],hostAttrs:[1,`mdc-icon-button`,`mat-mdc-icon-button`],exportAs:[`matButton`,`matAnchor`],features:[Ve],ngContentSelectors:i,decls:5,vars:1,consts:[[1,`mat-mdc-button-persistent-ripple`,`mdc-icon-button__ripple`],[1,`mat-mdc-button-progress-indicator-container`],[1,`mat-focus-indicator`],[1,`mat-mdc-button-touch-target`]],template:function(r,s){r&1&&(Re(e),Wt(0,`span`,0),me(1),D(2,a,2,0,`div`,1),Wt(3,`span`,2)(4,`span`,3)),r&2&&(h(2),k(s.showProgress()?2:-1))},styles:[`.mat-mdc-icon-button {
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
`],encapsulation:2})})()}return t})();var EA=new I(`cdk-dir-doc`,{providedIn:`root`,factory:()=>f(de)});var DA=/^(ar|ckb|dv|he|iw|fa|nqo|ps|sd|ug|ur|yi|.*[-_](Adlm|Arab|Hebr|Nkoo|Rohg|Thaa))(?!.*[-_](Latn|Cyrl)($|-|_))($|-|_)/i;function tS(t){let n=t?.toLowerCase()||``;return n===`auto`&&typeof navigator<`u`&&navigator?.language?DA.test(navigator.language)?`rtl`:`ltr`:n===`rtl`?`rtl`:`ltr`}var ft=(()=>{class t{get value(){return this.valueSignal()}valueSignal=N(`ltr`);change=new ee;constructor(){let e=f(EA,{optional:!0});if(e){let i=e.body?e.body.dir:null,a=e.documentElement?e.documentElement.dir:null;this.valueSignal.set(tS(i||a||`ltr`))}}ngOnDestroy(){this.change.complete()}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var we=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({})}return t})();var Ii=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[we]})}return t})();var nS=new Map([[`text`,[`mat-mdc-button`]],[`filled`,[`mdc-button--unelevated`,`mat-mdc-unelevated-button`]],[`elevated`,[`mdc-button--raised`,`mat-mdc-raised-button`]],[`outlined`,[`mdc-button--outlined`,`mat-mdc-outlined-button`]],[`tonal`,[`mat-tonal-button`]]]);var en=(()=>{class t extends Gg{get appearance(){return this._appearance}set appearance(e){this.setAppearance(e||this._config?.defaultAppearance||`text`)}_appearance=null;constructor(){super();let e=kA(this._elementRef.nativeElement);e&&this.setAppearance(e)}setAppearance(e){if(e===this._appearance)return;let i=this._elementRef.nativeElement.classList,a=this._appearance?nS.get(this._appearance):null,o=nS.get(e);a&&i.remove(...a),i.add(...o),this._appearance=e}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[``,8,`material-icons`,3,`iconPositionEnd`,``],[`mat-icon`,3,`iconPositionEnd`,``],[``,`matButtonIcon`,``,3,`iconPositionEnd`,``],[``,8,`material-symbols-outlined`,3,`iconPositionEnd`,``],[``,8,`material-symbols-rounded`,3,`iconPositionEnd`,``],[``,8,`material-symbols-sharp`,3,`iconPositionEnd`,``]],`*`,[[``,`iconPositionEnd`,``,8,`material-icons`],[`mat-icon`,`iconPositionEnd`,``],[``,`matButtonIcon`,``,`iconPositionEnd`,``],[``,`iconPositionEnd`,``,8,`material-symbols-outlined`],[``,`iconPositionEnd`,``,8,`material-symbols-rounded`],[``,`iconPositionEnd`,``,8,`material-symbols-sharp`]],[[``,`progressIndicator`,``]]],i=[`.material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd]), .material-symbols-outlined:not([iconPositionEnd]), .material-symbols-rounded:not([iconPositionEnd]), .material-symbols-sharp:not([iconPositionEnd])`,`*`,`.material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd], .material-symbols-outlined[iconPositionEnd], .material-symbols-rounded[iconPositionEnd], .material-symbols-sharp[iconPositionEnd]`,`[progressIndicator]`];function a(o,r){o&1&&(st(0,`div`,2),me(1,3),pt())}return B({type:t,selectors:[[`button`,`matButton`,``],[`a`,`matButton`,``],[`button`,`mat-button`,``],[`button`,`mat-raised-button`,``],[`button`,`mat-flat-button`,``],[`button`,`mat-stroked-button`,``],[`a`,`mat-button`,``],[`a`,`mat-raised-button`,``],[`a`,`mat-flat-button`,``],[`a`,`mat-stroked-button`,``]],hostAttrs:[1,`mdc-button`],inputs:{appearance:[0,`matButton`,`appearance`]},exportAs:[`matButton`,`matAnchor`],features:[Ve],ngContentSelectors:i,decls:8,vars:5,consts:[[1,`mat-mdc-button-persistent-ripple`],[1,`mdc-button__label`],[1,`mat-mdc-button-progress-indicator-container`],[1,`mat-focus-indicator`],[1,`mat-mdc-button-touch-target`]],template:function(r,s){r&1&&(Re(e),Wt(0,`span`,0),me(1),st(2,`span`,1),me(3,1),pt(),me(4,2),D(5,a,2,0,`div`,2),Wt(6,`span`,3)(7,`span`,4)),r&2&&(q(`mdc-button__ripple`,!s._isFab)(`mdc-fab__ripple`,s._isFab),h(5),k(s.showProgress()?5:-1))},styles:[`.mat-mdc-button-base {
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
`],encapsulation:2})})()}return t})();function kA(t){return t.hasAttribute(`mat-raised-button`)?`elevated`:t.hasAttribute(`mat-stroked-button`)?`outlined`:t.hasAttribute(`mat-flat-button`)?`filled`:t.hasAttribute(`mat-button`)?`text`:null}var Ot=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[Ii,we]})}return t})();var IA=new I(`MAT_CARD_CONFIG`);var Bn=(()=>{class t{appearance;constructor(){let e=f(IA,{optional:!0});this.appearance=e?.appearance||`raised`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return B({type:t,selectors:[[`mat-card`]],hostAttrs:[1,`mat-mdc-card`,`mdc-card`],hostVars:8,hostBindings:function(a,o){a&2&&q(`mat-mdc-card-outlined`,o.appearance===`outlined`)(`mdc-card--outlined`,o.appearance===`outlined`)(`mat-mdc-card-filled`,o.appearance===`filled`)(`mdc-card--filled`,o.appearance===`filled`)},inputs:{appearance:`appearance`},exportAs:[`matCard`],ngContentSelectors:[`*`],decls:1,vars:0,template:function(a,o){a&1&&(Re(),me(0))},styles:[`.mat-mdc-card {
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
`],encapsulation:2})})()}return t})();var Ia=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[`mat-card-title`],[``,`mat-card-title`,``],[``,`matCardTitle`,``]],hostAttrs:[1,`mat-mdc-card-title`]})}return t})();var jn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[`mat-card-content`]],hostAttrs:[1,`mat-mdc-card-content`]})}return t})();var Fu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[`mat-card-subtitle`],[``,`mat-card-subtitle`,``],[``,`matCardSubtitle`,``]],hostAttrs:[1,`mat-mdc-card-subtitle`]})}return t})();var Ma=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[``,`mat-card-avatar`,``],[``,`matCardAvatar`,``]],[[`mat-card-title`],[`mat-card-subtitle`],[``,`mat-card-title`,``],[``,`mat-card-subtitle`,``],[``,`matCardTitle`,``],[``,`matCardSubtitle`,``]],`*`];return B({type:t,selectors:[[`mat-card-header`]],hostAttrs:[1,`mat-mdc-card-header`],ngContentSelectors:[`[mat-card-avatar], [matCardAvatar]`,`mat-card-title, mat-card-subtitle,
      [mat-card-title], [mat-card-subtitle],
      [matCardTitle], [matCardSubtitle]`,`*`],decls:4,vars:0,consts:[[1,`mat-mdc-card-header-text`]],template:function(o,r){o&1&&(Re(e),me(0),st(1,`div`,0),me(2,1),pt(),me(3,2))},encapsulation:2})})()}return t})();var zn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[we]})}return t})();var MA=new I(`mat-progress-spinner-default-options`,{providedIn:`root`,factory:()=>({diameter:iS})});var iS=100;var NA=10;var aS=(()=>{class t{_elementRef=f(W);_noopAnimations;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor=`primary`;_determinateCircle;constructor(){let e=f(MA),i=Dc(),a=this._elementRef.nativeElement;this._noopAnimations=i===`di-disabled`&&!!e&&!e._forceAnimations,this.mode=a.nodeName.toLowerCase()===`mat-spinner`?`indeterminate`:`determinate`,!this._noopAnimations&&i===`reduced-motion`&&a.classList.add(`mat-progress-spinner-reduced-motion`),e&&(e.color&&(this.color=this._defaultColor=e.color),e.diameter&&(this.diameter=e.diameter),e.strokeWidth&&(this.strokeWidth=e.strokeWidth))}mode;get value(){return this.mode===`determinate`?this._value:0}set value(e){this._value=Math.max(0,Math.min(100,e||0))}_value=0;get diameter(){return this._diameter}set diameter(e){this._diameter=e||0}_diameter=iS;get strokeWidth(){return this._strokeWidth??this.diameter/10}set strokeWidth(e){this._strokeWidth=e||0}_strokeWidth;_circleRadius(){return(this.diameter-NA)/2}_viewBox(){let e=this._circleRadius()*2+this.strokeWidth;return`0 0 ${e} ${e}`}_strokeCircumference(){return 2*Math.PI*this._circleRadius()}_strokeDashOffset(){return this.mode===`determinate`?this._strokeCircumference()*(100-this._value)/100:null}_circleStrokeWidth(){return this.strokeWidth/this.diameter*100}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`determinateSpinner`];function i(a,o){if(a&1&&(Gt(),l(0,`svg`,11),z(1,`circle`,12),d()),a&2){let r=y();te(`viewBox`,r._viewBox()),h(),fn(`stroke-dasharray`,r._strokeCircumference(),`px`)(`stroke-dashoffset`,r._strokeCircumference()/2,`px`)(`stroke-width`,r._circleStrokeWidth(),`%`),te(`r`,r._circleRadius())}}return B({type:t,selectors:[[`mat-progress-spinner`],[`mat-spinner`]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;Z(s=J())&&(r._determinateCircle=s.first)}},hostAttrs:[`role`,`progressbar`,`tabindex`,`-1`,1,`mat-mdc-progress-spinner`,`mdc-circular-progress`],hostVars:18,hostBindings:function(o,r){o&2&&(te(`aria-valuemin`,0)(`aria-valuemax`,100)(`aria-valuenow`,r.mode===`determinate`?r.value:null)(`mode`,r.mode),tt(`mat-`+r.color),fn(`width`,r.diameter,`px`)(`height`,r.diameter,`px`)(`--%NS%mat-progress-spinner-size`,r.diameter+`px`)(`--%NS%mat-progress-spinner-active-indicator-width`,r.diameter+`px`),q(`_mat-animation-noopable`,r._noopAnimations)(`mdc-circular-progress--indeterminate`,r.mode===`indeterminate`))},inputs:{color:`color`,mode:`mode`,value:[2,`value`,`value`,ct],diameter:[2,`diameter`,`diameter`,ct],strokeWidth:[2,`strokeWidth`,`strokeWidth`,ct]},exportAs:[`matProgressSpinner`],decls:14,vars:11,consts:[[`circle`,``],[`determinateSpinner`,``],[`aria-hidden`,`true`,1,`mdc-circular-progress__determinate-container`],[`xmlns`,`http://www.w3.org/2000/svg`,`focusable`,`false`,1,`mdc-circular-progress__determinate-circle-graphic`],[`cx`,`50%`,`cy`,`50%`,1,`mdc-circular-progress__determinate-circle`],[`aria-hidden`,`true`,1,`mdc-circular-progress__indeterminate-container`],[1,`mdc-circular-progress__spinner-layer`],[1,`mdc-circular-progress__circle-clipper`,`mdc-circular-progress__circle-left`],[3,`ngTemplateOutlet`],[1,`mdc-circular-progress__gap-patch`],[1,`mdc-circular-progress__circle-clipper`,`mdc-circular-progress__circle-right`],[`xmlns`,`http://www.w3.org/2000/svg`,`focusable`,`false`,1,`mdc-circular-progress__indeterminate-circle-graphic`],[`cx`,`50%`,`cy`,`50%`]],template:function(o,r){if(o&1&&(ot(0,i,2,8,`ng-template`,null,0,rc),l(2,`div`,2,1),Gt(),l(4,`svg`,3),z(5,`circle`,4),d()(),ya(),l(6,`div`,5)(7,`div`,6)(8,`div`,7),wa(9,8),d(),l(10,`div`,9),wa(11,8),d(),l(12,`div`,10),wa(13,8),d()()()),o&2){let s=ht(1);h(4),te(`viewBox`,r._viewBox()),h(),fn(`stroke-dasharray`,r._strokeCircumference(),`px`)(`stroke-dashoffset`,r._strokeDashOffset(),`px`)(`stroke-width`,r._circleStrokeWidth(),`%`),te(`r`,r._circleRadius()),h(4),S(`ngTemplateOutlet`,s),h(2),S(`ngTemplateOutlet`,s),h(2),S(`ngTemplateOutlet`,s)}},dependencies:[fc],styles:[`.mat-mdc-progress-spinner {
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
`],encapsulation:2})})()}return t})();var oS=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[we]})}return t})();var hS=(()=>{class t{_renderer;_elementRef;onChange=e=>{};onTouched=()=>{};constructor(e,i){this._renderer=e,this._elementRef=i}setProperty(e,i){this._renderer.setProperty(this._elementRef.nativeElement,e,i)}registerOnTouched(e){this.onTouched=e}registerOnChange(e){this.onChange=e}setDisabledState(e){this.setProperty(`disabled`,e)}static ɵfac=function(i){return new(i||t)(He(Oe),He(W))};static ɵdir=Y({type:t})}return t})();var AA=(()=>{class t extends hS{static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=Y({type:t,features:[Ve]})}return t})();var Gr=new I(``);var OA={provide:Gr,useExisting:Ht(()=>qu),multi:!0};function RA(){let t=Fn()?Fn().getUserAgent():``;return/android (\d+)/.test(t.toLowerCase())}var PA=new I(``);var qu=(()=>{class t extends hS{_compositionMode;_composing=!1;constructor(e,i,a){super(e,i),this._compositionMode=a,this._compositionMode??=!RA()}writeValue(e){let i=e??``;this.setProperty(`value`,i)}_handleInput(e){(!this._compositionMode||this._compositionMode&&!this._composing)&&this.onChange(e)}_compositionStart(){this._composing=!0}_compositionEnd(e){this._composing=!1,this._compositionMode&&this.onChange(e)}static ɵfac=function(i){return new(i||t)(He(Oe),He(W),He(PA,8))};static ɵdir=Y({type:t,selectors:[[`input`,`formControlName`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`formControlName`,``,3,`ngNoCva`,``],[`input`,`formControl`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`formControl`,``,3,`ngNoCva`,``],[`input`,`ngModel`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`ngModel`,``,3,`ngNoCva`,``],[``,`ngDefaultControl`,``]],hostBindings:function(i,a){i&1&&C(`input`,function(r){return a._handleInput(r.target.value)})(`blur`,function(){return a.onTouched()})(`compositionstart`,function(){return a._compositionStart()})(`compositionend`,function(r){return a._compositionEnd(r.target.value)})},standalone:!1,features:[Ge([OA]),Ve]})}return t})();function Qg(t){return t==null||Xg(t)===0}function Xg(t){return t==null?null:Array.isArray(t)||typeof t==`string`?t.length:t instanceof Set?t.size:null}var Wr=new I(``);var Zg=new I(``);var FA=/^(?=.{1,254}$)(?=.{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;var Ro=class{static min(n){return LA(n)}static max(n){return VA(n)}static required(n){return fS(n)}static requiredTrue(n){return BA(n)}static email(n){return jA(n)}static minLength(n){return zA(n)}static maxLength(n){return HA(n)}static pattern(n){return UA(n)}static nullValidator(n){return Vu()}static compose(n){return xS(n)}static composeAsync(n){return CS(n)}};function LA(t){return n=>{if(n.value==null||t==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e<t?{min:{min:t,actual:n.value}}:null}}function VA(t){return n=>{if(n.value==null||t==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e>t?{max:{max:t,actual:n.value}}:null}}function fS(t){return Qg(t.value)?{required:!0}:null}function BA(t){return t.value===!0?null:{required:!0}}function jA(t){return Qg(t.value)||FA.test(t.value)?null:{email:!0}}function zA(t){return n=>{let e=n.value?.length??Xg(n.value);return e===null||e===0?null:e<t?{minlength:{requiredLength:t,actualLength:e}}:null}}function HA(t){return n=>{let e=n.value?.length??Xg(n.value);return e!==null&&e>t?{maxlength:{requiredLength:t,actualLength:e}}:null}}function UA(t){if(!t)return Vu;let n,e;return typeof t==`string`?(e=``,t.charAt(0)!==`^`&&(e+=`^`),e+=t,t.charAt(t.length-1)!==`$`&&(e+=`$`),n=new RegExp(e)):(e=t.toString(),n=t),i=>{if(Qg(i.value))return null;let a=i.value;return n.test(a)?null:{pattern:{requiredPattern:e,actualValue:a}}}}function Vu(t){return null}function gS(t){return t!=null}function bS(t){return xo(t)?In(t):t}function vS(t){let n={};return t.forEach(e=>{n=e!=null?r(r({},n),e):n}),Object.keys(n).length===0?null:n}function yS(t,n){return n.map(e=>e(t))}function $A(t){return!t.validate}function _S(t){return t.map(n=>$A(n)?n:e=>n.validate(e))}function xS(t){if(!t)return null;let n=t.filter(gS);return n.length==0?null:function(e){return vS(yS(e,n))}}function Jg(t){return t!=null?xS(_S(t)):null}function CS(t){if(!t)return null;let n=t.filter(gS);return n.length==0?null:function(e){return op(yS(e,n).map(bS)).pipe(rt(vS))}}function eb(t){return t!=null?CS(_S(t)):null}function rS(t,n){return t===null?[n]:Array.isArray(t)?[...t,n]:[t,n]}function SS(t){return t._rawValidators}function wS(t){return t._rawAsyncValidators}function qg(t){return t?Array.isArray(t)?t:[t]:[]}function Bu(t,n){return Array.isArray(t)?t.includes(n):t===n}function sS(t,n){let e=qg(n);return qg(t).forEach(a=>{Bu(e,a)||e.push(a)}),e}function cS(t,n){return qg(n).filter(e=>!Bu(t,e))}var ju=class{get value(){return this.control?this.control.value:null}get valid(){return this.control?this.control.valid:null}get invalid(){return this.control?this.control.invalid:null}get pending(){return this.control?this.control.pending:null}get disabled(){return this.control?this.control.disabled:null}get enabled(){return this.control?this.control.enabled:null}get errors(){return this.control?this.control.errors:null}get pristine(){return this.control?this.control.pristine:null}get dirty(){return this.control?this.control.dirty:null}get touched(){return this.control?this.control.touched:null}get status(){return this.control?this.control.status:null}get untouched(){return this.control?this.control.untouched:null}get statusChanges(){return this.control?this.control.statusChanges:null}get valueChanges(){return this.control?this.control.valueChanges:null}get path(){return null}_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators=[];_rawAsyncValidators=[];_setValidators(n){this._rawValidators=n||[],this._composedValidatorFn=Jg(this._rawValidators)}_setAsyncValidators(n){this._rawAsyncValidators=n||[],this._composedAsyncValidatorFn=eb(this._rawAsyncValidators)}get validator(){return this._composedValidatorFn||null}get asyncValidator(){return this._composedAsyncValidatorFn||null}_onDestroyCallbacks=[];_registerOnDestroy(n){this._onDestroyCallbacks.push(n)}_invokeOnDestroyCallbacks(){this._onDestroyCallbacks.forEach(n=>n()),this._onDestroyCallbacks=[]}reset(n=void 0){this.control?.reset(n)}hasError(n,e){return this.control?this.control.hasError(n,e):!1}getError(n,e){return this.control?this.control.getError(n,e):null}};var Po=class extends ju{name;get formDirective(){return null}get path(){return null}};var Mc=`VALID`;var Lu=`INVALID`;var Hr=`PENDING`;var Nc=`DISABLED`;var Na=class{};var zu=class extends Na{value;source;constructor(n,e){super(),this.value=n,this.source=e}};var Ac=class extends Na{pristine;source;constructor(n,e){super(),this.pristine=n,this.source=e}};var Oc=class extends Na{touched;source;constructor(n,e){super(),this.touched=n,this.source=e}};var Ur=class extends Na{status;source;constructor(n,e){super(),this.status=n,this.source=e}};var Hu=class extends Na{source;constructor(n){super(),this.source=n}};var $r=class extends Na{source;constructor(n){super(),this.source=n}};function ES(t){return(Yu(t)?t.validators:t)||null}function GA(t){return Array.isArray(t)?Jg(t):t||null}function DS(t,n){return(Yu(n)?n.asyncValidators:t)||null}function WA(t){return Array.isArray(t)?eb(t):t||null}function Yu(t){return t!=null&&!Array.isArray(t)&&typeof t==`object`}function qA(t,n,e){let i=t.controls;if(!(n?Object.keys(i):i).length)throw new G(1e3,``);if(!kS(i,e))throw new G(1001,``)}function YA(t,n,e){t._forEachChild((i,a)=>{if(e[a]===void 0)throw new G(-1002,``)})}var Uu=class{_pendingDirty=!1;_hasOwnPendingAsyncValidator=null;_pendingTouched=!1;_onCollectionChange=()=>{};_updateOn;_hasRequired=N(!1);_parent=null;_asyncValidationSubscription;_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators;_rawAsyncValidators;value;constructor(n,e){this._assignValidators(n),this._assignAsyncValidators(e)}get validator(){return this._composedValidatorFn}set validator(n){this._rawValidators=this._composedValidatorFn=n,this._updateHasRequiredValidator()}get asyncValidator(){return this._composedAsyncValidatorFn}set asyncValidator(n){this._rawAsyncValidators=this._composedAsyncValidatorFn=n}get parent(){return this._parent}get status(){return nt(this.statusReactive)}set status(n){nt(()=>this.statusReactive.set(n))}_status=ne(()=>this.statusReactive());statusReactive=N(void 0);get valid(){return this.status===Mc}get invalid(){return this.status===Lu}get pending(){return this.status===Hr}get disabled(){return this.status===Nc}get enabled(){return this.status!==Nc}errors;get pristine(){return nt(this.pristineReactive)}set pristine(n){nt(()=>this.pristineReactive.set(n))}_pristine=ne(()=>this.pristineReactive());pristineReactive=N(!0);get dirty(){return!this.pristine}get touched(){return nt(this.touchedReactive)}set touched(n){nt(()=>this.touchedReactive.set(n))}_touched=ne(()=>this.touchedReactive());touchedReactive=N(!1);get untouched(){return!this.touched}_events=new L;events=this._events.asObservable();valueChanges;statusChanges;get updateOn(){return this._updateOn?this._updateOn:this.parent?this.parent.updateOn:`change`}setValidators(n){this._assignValidators(n)}setAsyncValidators(n){this._assignAsyncValidators(n)}addValidators(n){this.setValidators(sS(n,this._rawValidators))}addAsyncValidators(n){this.setAsyncValidators(sS(n,this._rawAsyncValidators))}removeValidators(n){this.setValidators(cS(n,this._rawValidators))}removeAsyncValidators(n){this.setAsyncValidators(cS(n,this._rawAsyncValidators))}hasValidator(n){return Bu(this._rawValidators,n)}hasAsyncValidator(n){return Bu(this._rawAsyncValidators,n)}clearValidators(){this.validator=null}clearAsyncValidators(){this.asyncValidator=null}markAsTouched(n={}){let e=this.touched===!1;this.touched=!0;let i=n.sourceControl??this;n.onlySelf||this._parent?.markAsTouched(s(r({},n),{sourceControl:i})),e&&n.emitEvent!==!1&&this._events.next(new Oc(!0,i))}markAllAsDirty(n={}){this.markAsDirty({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsDirty(n))}markAllAsTouched(n={}){this.markAsTouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsTouched(n))}markAsUntouched(n={}){let e=this.touched===!0;this.touched=!1,this._pendingTouched=!1;let i=n.sourceControl??this;this._forEachChild(a=>{a.markAsUntouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:i})}),n.onlySelf||this._parent?._updateTouched(n,i),e&&n.emitEvent!==!1&&this._events.next(new Oc(!1,i))}markAsDirty(n={}){let e=this.pristine===!0;this.pristine=!1;let i=n.sourceControl??this;n.onlySelf||this._parent?.markAsDirty(s(r({},n),{sourceControl:i})),e&&n.emitEvent!==!1&&this._events.next(new Ac(!1,i))}markAsPristine(n={}){let e=this.pristine===!1;this.pristine=!0,this._pendingDirty=!1;let i=n.sourceControl??this;this._forEachChild(a=>{a.markAsPristine({onlySelf:!0,emitEvent:n.emitEvent})}),n.onlySelf||this._parent?._updatePristine(n,i),e&&n.emitEvent!==!1&&this._events.next(new Ac(!0,i))}markAsPending(n={}){this.status=Hr;let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new Ur(this.status,e)),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.markAsPending(s(r({},n),{sourceControl:e}))}disable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=Nc,this.errors=null,this._forEachChild(a=>{a.disable(s(r({},n),{onlySelf:!0}))}),this._updateValue();let i=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new zu(this.value,i)),this._events.next(new Ur(this.status,i)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),this._updateAncestors(s(r({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(a=>a(!0))}enable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=Mc,this._forEachChild(i=>{i.enable(s(r({},n),{onlySelf:!0}))}),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent}),this._updateAncestors(s(r({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(i=>i(!1))}_updateAncestors(n,e){n.onlySelf||(this._parent?.updateValueAndValidity(n),n.skipPristineCheck||this._parent?._updatePristine({},e),this._parent?._updateTouched({},e))}setParent(n){this._parent=n}getRawValue(){return this.value}updateValueAndValidity(n={}){if(this._setInitialStatus(),this._updateValue(),this.enabled){let i=this._cancelExistingSubscription();this.errors=this._runValidator(),this.status=this._calculateStatus(),(this.status===Mc||this.status===Hr)&&this._runAsyncValidator(i,n.emitEvent)}let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new zu(this.value,e)),this._events.next(new Ur(this.status,e)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.updateValueAndValidity(s(r({},n),{sourceControl:e}))}_updateTreeValidity(n={emitEvent:!0}){this._forEachChild(e=>e._updateTreeValidity(n)),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent})}_setInitialStatus(){this.status=this._allControlsDisabled()?Nc:Mc}_runValidator(){return this.validator?this.validator(this):null}_runAsyncValidator(n,e){if(this.asyncValidator){this.status=Hr,this._hasOwnPendingAsyncValidator={emitEvent:e!==!1,shouldHaveEmitted:n!==!1};let i=bS(this.asyncValidator(this));this._asyncValidationSubscription=i.subscribe(a=>{this._hasOwnPendingAsyncValidator=null,this.setErrors(a,{emitEvent:e,shouldHaveEmitted:n})})}}_cancelExistingSubscription(){if(this._asyncValidationSubscription){this._asyncValidationSubscription.unsubscribe();let n=(this._hasOwnPendingAsyncValidator?.emitEvent||this._hasOwnPendingAsyncValidator?.shouldHaveEmitted)??!1;return this._hasOwnPendingAsyncValidator=null,n}return!1}setErrors(n,e={}){this.errors=n,this._updateControlsErrors(e.emitEvent!==!1,this,e.shouldHaveEmitted)}get(n){let e=n;return e==null||(Array.isArray(e)||(e=e.split(`.`)),e.length===0)?null:e.reduce((i,a)=>i&&i._find(a),this)}getError(n,e){let i=e?this.get(e):this;return i?.errors?i.errors[n]:null}hasError(n,e){return!!this.getError(n,e)}get root(){let n=this;for(;n._parent;)n=n._parent;return n}_updateControlsErrors(n,e,i){this.status=this._calculateStatus(),n&&this.statusChanges.emit(this.status),(n||i)&&this._events.next(new Ur(this.status,e)),this._parent&&this._parent._updateControlsErrors(n,e,i)}_initObservables(){this.valueChanges=new ee,this.statusChanges=new ee}_calculateStatus(){return this._allControlsDisabled()?Nc:this.errors?Lu:this._hasOwnPendingAsyncValidator||this._anyControlsHaveStatus(Hr)?Hr:this._anyControlsHaveStatus(Lu)?Lu:Mc}_anyControlsHaveStatus(n){return this._anyControls(e=>e.status===n)}_anyControlsDirty(){return this._anyControls(n=>n.dirty)}_anyControlsTouched(){return this._anyControls(n=>n.touched)}_updatePristine(n,e){let i=!this._anyControlsDirty(),a=this.pristine!==i;this.pristine=i,n.onlySelf||this._parent?._updatePristine(n,e),a&&this._events.next(new Ac(this.pristine,e))}_updateTouched(n={},e){this.touched=this._anyControlsTouched(),this._events.next(new Oc(this.touched,e)),n.onlySelf||this._parent?._updateTouched(n,e)}_onDisabledChange=[];_registerOnCollectionChange(n){this._onCollectionChange=n}_setUpdateStrategy(n){Yu(n)&&n.updateOn!=null&&(this._updateOn=n.updateOn)}_parentMarkedDirty(n){return!n&&!!this._parent?.dirty&&!this._parent._anyControlsDirty()}_find(n){return null}_assignValidators(n){this._rawValidators=Array.isArray(n)?n.slice():n,this._composedValidatorFn=GA(this._rawValidators),this._updateHasRequiredValidator()}_assignAsyncValidators(n){this._rawAsyncValidators=Array.isArray(n)?n.slice():n,this._composedAsyncValidatorFn=WA(this._rawAsyncValidators)}_updateHasRequiredValidator(){nt(()=>this._hasRequired.set(this.hasValidator(Ro.required)))}};function kS(t,n){return Object.hasOwn(t,n)}function KA(t){return t.tagName===`INPUT`||t.tagName===`SELECT`||t.tagName===`TEXTAREA`}function QA(t,n,e,i){switch(e){case`name`:t.setAttribute(n,e,i);break;case`disabled`:case`readonly`:case`required`:i?t.setAttribute(n,e,``):t.removeAttribute(n,e);break;case`max`:case`min`:case`minLength`:case`maxLength`:i!==void 0?t.setAttribute(n,e,i.toString()):t.removeAttribute(n,e)}}var Yg=class{kind;context;control;message;constructor({kind:n,context:e,control:i}){this.kind=n,this.context=e,this.control=i}};var XA=(()=>{class t{_validator=Vu;_onChange;_enabled;ngOnChanges(e){if(this.inputName in e){let i=this.normalizeInput(e[this.inputName].currentValue);this._enabled=this.enabled(i),this._validator=this._enabled?this.createValidator(i):Vu,this._onChange?.()}}validate(e){return this._validator(e)}registerOnValidatorChange(e){this._onChange=e}enabled(e){return e!=null}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,features:[bt]})}return t})();var ZA={provide:Wr,useExisting:Ht(()=>IS),multi:!0};var IS=(()=>{class t extends XA{required;inputName=`required`;normalizeInput=V;createValidator=e=>fS;enabled(e){return e}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=Y({type:t,selectors:[[``,`required`,``,`formControlName`,``,3,`type`,`checkbox`],[``,`required`,``,`formControl`,``,3,`type`,`checkbox`],[``,`required`,``,`ngModel`,``,3,`type`,`checkbox`]],hostVars:1,hostBindings:function(i,a){i&2&&te(`required`,a._enabled?``:null)},inputs:{required:`required`},standalone:!1,features:[Ge([ZA]),Ve]})}return t})();var JA=new I(``);var Ku=new I(``,{factory:()=>tb});var tb=`always`;function eO(t,n){return[...n.path,t]}function lS(t,n,e=tb){nb(t,n),n.valueAccessor.writeValue(t.value),(t.disabled||e===`always`)&&n.valueAccessor.setDisabledState?.(t.disabled),nO(t,n),aO(t,n),iO(t,n),tO(t,n)}function dS(t,n,e=!0){let i=()=>{};n?.valueAccessor?.registerOnChange(i),n?.valueAccessor?.registerOnTouched(i),Gu(t,n),t&&(n._invokeOnDestroyCallbacks(),t._registerOnCollectionChange(()=>{}))}function $u(t,n){t.forEach(e=>{e.registerOnValidatorChange&&e.registerOnValidatorChange(n)})}function tO(t,n){if(n.valueAccessor.setDisabledState){let e=i=>{n.valueAccessor.setDisabledState(i)};t.registerOnDisabledChange(e),n._registerOnDestroy(()=>{t._unregisterOnDisabledChange(e)})}}function nb(t,n){let e=SS(t);n.validator!==null?t.setValidators(rS(e,n.validator)):typeof e==`function`&&t.setValidators([e]);let i=wS(t);n.asyncValidator!==null?t.setAsyncValidators(rS(i,n.asyncValidator)):typeof i==`function`&&t.setAsyncValidators([i]);let a=()=>t.updateValueAndValidity();$u(n._rawValidators,a),$u(n._rawAsyncValidators,a)}function Gu(t,n){let e=!1;if(t!==null){if(n.validator!==null){let a=SS(t);if(Array.isArray(a)&&a.length>0){let o=a.filter(r=>r!==n.validator);o.length!==a.length&&(e=!0,t.setValidators(o))}}if(n.asyncValidator!==null){let a=wS(t);if(Array.isArray(a)&&a.length>0){let o=a.filter(r=>r!==n.asyncValidator);o.length!==a.length&&(e=!0,t.setAsyncValidators(o))}}}let i=()=>{};return $u(n._rawValidators,i),$u(n._rawAsyncValidators,i),e}function nO(t,n){n.valueAccessor.registerOnChange(e=>{t._pendingValue=e,t._pendingChange=!0,t._pendingDirty=!0,t.updateOn===`change`&&MS(t,n)})}function iO(t,n){n.valueAccessor.registerOnTouched(()=>{t._pendingTouched=!0,t.updateOn===`blur`&&t._pendingChange&&MS(t,n),t.updateOn!==`submit`&&t.markAsTouched()})}function MS(t,n){t._pendingDirty&&t.markAsDirty(),t.setValue(t._pendingValue,{emitModelToViewChange:!1}),n.viewToModelUpdate(t._pendingValue),t._pendingChange=!1}function aO(t,n){let e=(i,a)=>{n.valueAccessor.writeValue(i),a&&n.viewToModelUpdate(i)};t.registerOnChange(e),n._registerOnDestroy(()=>{t._unregisterOnChange(e)})}function NS(t,n){nb(t,n)}function oO(t,n){return Gu(t,n)}function rO(t,n){if(!Object.hasOwn(t,`model`))return!1;let e=t.model;return e.isFirstChange()?!0:!Object.is(n,e.currentValue)}function sO(t){return Object.getPrototypeOf(t.constructor)===AA}function TS(t,n){t._syncPendingControls(),n.forEach(e=>{let i=e.control;i.updateOn===`submit`&&i._pendingChange&&(e.viewToModelUpdate(i._pendingValue),i._pendingChange=!1)})}function cO(t,n){if(!n)return null;let e,i,a;return n.forEach(o=>{o.constructor===qu?e=o:sO(o)?i=o:a=o}),a||i||e||null}function lO(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}var dO={provide:JA,useFactory:()=>{let t=f(Mi,{self:!0});return{setParseErrors:n=>{t.setParseErrorSource(n)},set onReset(n){t.onReset=n}}}};var Mi=class extends ju{_parent=null;name=null;valueAccessor=null;isCustomControlBased=!1;userOnReset;resetSubscription;set onReset(n){this.userOnReset=n,this.resetSubscription?.unsubscribe(),this.resetSubscription=void 0,this.control&&(this.resetSubscription=this.control.events.subscribe(e=>{e instanceof $r&&this.control&&this.userOnReset?.(this.control.value)}),this.subscription?.add(this.resetSubscription))}isNativeFormElement=!1;rawValueAccessors;_selectedValueAccessor=null;get selectedValueAccessor(){return this._selectedValueAccessor??=cO(this,this.rawValueAccessors)}parseErrorsValidator=null;renderer;injector;requiredValidatorViaDi;subscription;customControlBindings=null;constructor(n,e,i){super(),this.injector=n,this.renderer=e,this.rawValueAccessors=i,this.injector?.get(Te)?.onDestroy(()=>{this.removeParseErrorsValidator(this.control),this.subscription?.unsubscribe()})}setupCustomControl(){this.subscription?.unsubscribe();let n=this.injector?.get(Pe);if(!this.control||!n)return;let e=n.markForCheck.bind(n);this.subscription=new De,this.subscription.add(this.control.valueChanges.subscribe(e)),this.subscription.add(this.control.statusChanges.subscribe(e)),this.resetSubscription?.unsubscribe(),this.resetSubscription=void 0,this.userOnReset&&(this.resetSubscription=this.control.events.subscribe(i=>{i instanceof $r&&this.control&&this.userOnReset?.(this.control.value)}),this.subscription.add(this.resetSubscription)),this.parseErrorsValidator&&this.control.addValidators(this.parseErrorsValidator)}ngControlCreate(n){!n.nativeElement.hasAttribute?.(`ngNoCva`)&&(this.rawValueAccessors&&this.rawValueAccessors.length>0||this.valueAccessor!==null)||!n.customControl||(this.isCustomControlBased=!0,n.listenToCustomControlModel(a=>{this.control?.markAsDirty(),this.control?.setValue(a,{emitModelToViewChange:!1}),this.viewToModelUpdate(a)}),n.listenToCustomControlOutput(`touch`,()=>{this.control?.markAsTouched()}),this.customControlBindings={},this.isNativeFormElement=KA(n.nativeElement),this.requiredValidatorViaDi=this._rawValidators.find(a=>a instanceof IS))}ngControlUpdate(n,e){if(!this.isCustomControlBased)return;let i=this.control,a=this.customControlBindings;Object.is(a.value,i.value)||(a.value=i.value,n.setCustomControlModelInput(i.value)),this.bindControlProperty(n,a,`touched`,i.touched),this.bindControlProperty(n,a,`dirty`,i.dirty),this.bindControlProperty(n,a,`valid`,i.valid),this.bindControlProperty(n,a,`invalid`,i.invalid),this.bindControlProperty(n,a,`pending`,i.pending),this.bindControlProperty(n,a,`disabled`,i.disabled),this.shouldBindRequired&&this.bindControlProperty(n,a,`required`,this.isRequired);let o=i.errors;if(a.errors!==o){a.errors=o;let r=this._convertErrors(o);n.setInputOnDirectives(`errors`,r)}}get isRequired(){return(this.requiredValidatorViaDi?._enabled||this.control?._hasRequired())??!1}get shouldBindRequired(){return!0}bindControlProperty(n,e,i,a){if(e[i]===a)return;e[i]=a;let o=n.setInputOnDirectives(i,a);this.isNativeFormElement&&!o&&(i===`disabled`||i===`required`)&&this.renderer&&QA(this.renderer,n.nativeElement,i,a)}_convertErrors(n){if(n===null)return[];let e=this.control;return Object.entries(n).map(([i,a])=>new Yg({context:a,kind:i,control:e}))}setParseErrorSource(n){if(n===void 0)return;let e=null,i=ne(()=>{let a=n();return a.length===0?null:a.reduce((o,r)=>(o[r.kind]=r,o),{})});this.parseErrorsValidator=(()=>e).bind(this),Ft(()=>{e=i(),this.control?.updateValueAndValidity({emitEvent:!1})},{injector:this.injector})}removeParseErrorsValidator(n){this.parseErrorsValidator&&(n?.removeValidators(this.parseErrorsValidator),n?.updateValueAndValidity({emitEvent:!1}))}};var Kg=class{_cd;constructor(n){this._cd=n}get isTouched(){return this._cd?.control?._touched?.(),!!this._cd?.control?.touched}get isUntouched(){return!!this._cd?.control?.untouched}get isPristine(){return this._cd?.control?._pristine?.(),!!this._cd?.control?.pristine}get isDirty(){return!!this._cd?.control?.dirty}get isValid(){return this._cd?.control?._status?.(),!!this._cd?.control?.valid}get isInvalid(){return!!this._cd?.control?.invalid}get isPending(){return!!this._cd?.control?.pending}get isSubmitted(){return this._cd?._submitted?.(),!!this._cd?.submitted}};var AS=(()=>{class t extends Kg{constructor(e){super(e)}static ɵfac=function(i){return new(i||t)(He(Mi,2))};static ɵdir=Y({type:t,selectors:[[``,`formControlName`,``],[``,`ngModel`,``],[``,`formControl`,``]],hostVars:14,hostBindings:function(i,a){i&2&&q(`ng-untouched`,a.isUntouched)(`ng-touched`,a.isTouched)(`ng-pristine`,a.isPristine)(`ng-dirty`,a.isDirty)(`ng-valid`,a.isValid)(`ng-invalid`,a.isInvalid)(`ng-pending`,a.isPending)},standalone:!1,features:[Ve]})}return t})();var Wu=class extends Uu{constructor(n,e,i){super(ES(e),DS(i,e)),this.controls=n,this._initObservables(),this._setUpdateStrategy(e),this._setUpControls(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator})}controls;registerControl(n,e){return this._find(n)||(this.controls[n]=e,e.setParent(this),e._registerOnCollectionChange(this._onCollectionChange),e)}addControl(n,e,i={}){this.registerControl(n,e),this.updateValueAndValidity({emitEvent:i.emitEvent}),this._onCollectionChange()}removeControl(n,e={}){let i=this._find(n);i&&i._registerOnCollectionChange(()=>{}),delete this.controls[n],this.updateValueAndValidity({emitEvent:e.emitEvent}),this._onCollectionChange()}setControl(n,e,i={}){let a=this._find(n);a&&a._registerOnCollectionChange(()=>{}),delete this.controls[n],e&&this.registerControl(n,e),this.updateValueAndValidity({emitEvent:i.emitEvent}),this._onCollectionChange()}contains(n){return this._find(n)?.enabled===!0}setValue(n,e={}){nt(()=>{YA(this,!0,n),Object.keys(n).forEach(i=>{qA(this,!0,i),this.controls[i].setValue(n[i],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e)})}patchValue(n,e={}){n!=null&&(Object.keys(n).forEach(i=>{let a=this._find(i);a&&a.patchValue(n[i],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e))}reset(n={},e={}){this._forEachChild((i,a)=>{i.reset(n?n[a]:null,s(r({},e),{onlySelf:!0}))}),this._updatePristine(e,this),this._updateTouched(e,this),this.updateValueAndValidity(e),e?.emitEvent!==!1&&this._events.next(new $r(this))}getRawValue(){return this._reduceChildren({},(n,e,i)=>(n[i]=e.getRawValue(),n))}_syncPendingControls(){let n=this._reduceChildren(!1,(e,i)=>i._syncPendingControls()?!0:e);return n&&this.updateValueAndValidity({onlySelf:!0}),n}_forEachChild(n){Object.keys(this.controls).forEach(e=>{let i=this.controls[e];i&&n(i,e)})}_setUpControls(){this._forEachChild(n=>{n.setParent(this),n._registerOnCollectionChange(this._onCollectionChange)})}_updateValue(){this.value=this._reduceValue()}_anyControls(n){for(let[e,i]of Object.entries(this.controls))if(this.contains(e)&&n(i))return!0;return!1}_reduceValue(){return this._reduceChildren({},(e,i,a)=>((i.enabled||this.disabled)&&(e[a]=i.value),e))}_reduceChildren(n,e){let i=n;return this._forEachChild((a,o)=>{i=e(i,a,o)}),i}_allControlsDisabled(){for(let n of Object.keys(this.controls))if(this.controls[n].enabled)return!1;return Object.keys(this.controls).length>0||this.disabled}_find(n){return kS(this.controls,n)?this.controls[n]:null}};var uO={provide:Po,useExisting:Ht(()=>Rc)};var Tc=Promise.resolve();var Rc=(()=>{class t extends Po{callSetDisabledState;get submitted(){return nt(this.submittedReactive)}_submitted=ne(()=>this.submittedReactive());submittedReactive=N(!1);_directives=new Set;form;ngSubmit=new ee;options;constructor(e,i,a){super(),this.callSetDisabledState=a,this.form=new Wu({},Jg(e),eb(i))}ngAfterViewInit(){this._setUpdateStrategy()}get formDirective(){return this}get control(){return this.form}get path(){return[]}get controls(){return this.form.controls}addControl(e){Tc.then(()=>{e.control=this._findContainer(e.path).registerControl(e.name,e.control),e._setupWithForm(this.callSetDisabledState),e.control.updateValueAndValidity({emitEvent:!1}),this._directives.add(e)})}getControl(e){return this.form.get(e.path)}removeControl(e){Tc.then(()=>{this._findContainer(e.path)?.removeControl(e.name),this._directives.delete(e)})}addFormGroup(e){Tc.then(()=>{let i=this._findContainer(e.path),a=new Wu({});NS(a,e),i.registerControl(e.name,a),a.updateValueAndValidity({emitEvent:!1})})}removeFormGroup(e){Tc.then(()=>{this._findContainer(e.path)?.removeControl?.(e.name)})}getFormGroup(e){return this.form.get(e.path)}updateModel(e,i){Tc.then(()=>{this.form.get(e.path).setValue(i)})}setValue(e){this.control.setValue(e)}onSubmit(e){return this.submittedReactive.set(!0),TS(this.form,this._directives),this.ngSubmit.emit(e),this.form._events.next(new Hu(this.control)),e?.target?.method===`dialog`}onReset(){this.resetForm()}resetForm(e=void 0){this.form.reset(e),this.submittedReactive.set(!1)}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.form._updateOn=this.options.updateOn)}_findContainer(e){return e.pop(),e.length?this.form.get(e):this.form}static ɵfac=function(i){return new(i||t)(He(Wr,10),He(Zg,10),He(Ku,8))};static ɵdir=Y({type:t,selectors:[[`form`,3,`ngNoForm`,``,3,`formGroup`,``,3,`formArray`,``],[`ng-form`],[``,`ngForm`,``]],hostBindings:function(i,a){i&1&&C(`submit`,function(r){return a.onSubmit(r)})(`reset`,function(){return a.onReset()})},inputs:{options:[0,`ngFormOptions`,`options`]},outputs:{ngSubmit:`ngSubmit`},exportAs:[`ngForm`],standalone:!1,features:[Ge([uO]),Ve]})}return t})();function uS(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}function mS(t){return typeof t==`object`&&t!==null&&Object.keys(t).length===2&&`value`in t&&`disabled`in t}var OS=class extends Uu{defaultValue=null;_onChange=[];_pendingValue;_pendingChange=!1;constructor(n=null,e,i){super(ES(e),DS(i,e)),this._applyFormState(n),this._setUpdateStrategy(e),this._initObservables(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator}),Yu(e)&&(e.nonNullable||e.initialValueIsDefault)&&(mS(n)?this.defaultValue=n.value:this.defaultValue=n)}setValue(n,e={}){nt(()=>{this.value=this._pendingValue=n,this._onChange.length&&e.emitModelToViewChange!==!1&&this._onChange.forEach(i=>i(this.value,e.emitViewToModelChange!==!1)),this.updateValueAndValidity(e)})}patchValue(n,e={}){this.setValue(n,e)}reset(n=this.defaultValue,e={}){this._applyFormState(n),this.markAsPristine(e),this.markAsUntouched(e),this.setValue(this.value,e),e.overwriteDefaultValue&&(this.defaultValue=this.value),this._pendingChange=!1,e?.emitEvent!==!1&&this._events.next(new $r(this))}_updateValue(){}_anyControls(n){return!1}_allControlsDisabled(){return this.disabled}registerOnChange(n){this._onChange.push(n)}_unregisterOnChange(n){uS(this._onChange,n)}registerOnDisabledChange(n){this._onDisabledChange.push(n)}_unregisterOnDisabledChange(n){uS(this._onDisabledChange,n)}_forEachChild(n){}_syncPendingControls(){return this.updateOn===`submit`&&(this._pendingDirty&&this.markAsDirty(),this._pendingTouched&&this.markAsTouched(),this._pendingChange)?(this.setValue(this._pendingValue,{onlySelf:!0,emitModelToViewChange:!1}),!0):!1}_applyFormState(n){mS(n)?(this.value=this._pendingValue=n.value,n.disabled?this.disable({onlySelf:!0,emitEvent:!1}):this.enable({onlySelf:!0,emitEvent:!1})):this.value=this._pendingValue=n}};var mO=t=>t instanceof OS;var pO=(()=>{class t extends Po{callSetDisabledState;get submitted(){return nt(this._submittedReactive)}set submitted(e){this._submittedReactive.set(e)}_submitted=ne(()=>this._submittedReactive());_submittedReactive=N(!1);_oldForm;_onCollectionChange=()=>this._updateDomValue();directives=[];constructor(e,i,a){super(),this.callSetDisabledState=a,this._setValidators(e),this._setAsyncValidators(i)}ngOnChanges(e){this.onChanges(e)}ngOnDestroy(){this.onDestroy()}onChanges(e){this._checkFormPresent(),Object.hasOwn(e,`form`)&&(this._updateValidators(),this._updateDomValue(),this._updateRegistrations(),this._oldForm=this.form)}onDestroy(){this.form&&(Gu(this.form,this),this.form._onCollectionChange===this._onCollectionChange&&this.form._registerOnCollectionChange(()=>{}))}get formDirective(){return this}get path(){return[]}addControl(e){let i=this.form.get(e.path);return e._setupWithForm(i,this.callSetDisabledState),i.updateValueAndValidity({emitEvent:!1}),this.directives.push(e),i}getControl(e){return this.form.get(e.path)}removeControl(e){dS(e.control||null,e,!1),lO(this.directives,e)}addFormGroup(e){this._setUpFormContainer(e)}removeFormGroup(e){this._cleanUpFormContainer(e)}getFormGroup(e){return this.form.get(e.path)}getFormArray(e){return this.form.get(e.path)}addFormArray(e){this._setUpFormContainer(e)}removeFormArray(e){this._cleanUpFormContainer(e)}updateModel(e,i){this.form.get(e.path).setValue(i)}onReset(){this.resetForm()}resetForm(e=void 0,i={}){this.form.reset(e,i),this._submittedReactive.set(!1)}onSubmit(e){return this.submitted=!0,TS(this.form,this.directives),this.ngSubmit.emit(e),this.form._events.next(new Hu(this.control)),e?.target?.method===`dialog`}_updateDomValue(){this.directives.forEach(e=>{let i=e.control,a=this.form.get(e.path);i!==a&&(dS(i||null,e),mO(a)&&e._setupWithForm(a,this.callSetDisabledState))}),this.form._updateTreeValidity({emitEvent:!1})}_setUpFormContainer(e){let i=this.form.get(e.path);NS(i,e),i.updateValueAndValidity({emitEvent:!1})}_cleanUpFormContainer(e){let i=this.form?.get(e.path);i&&oO(i,e)&&i.updateValueAndValidity({emitEvent:!1})}_updateRegistrations(){this.form._registerOnCollectionChange(this._onCollectionChange),this._oldForm?._registerOnCollectionChange(()=>{})}_updateValidators(){nb(this.form,this),this._oldForm&&Gu(this._oldForm,this)}_checkFormPresent(){this.form}static ɵfac=function(i){return new(i||t)(He(Wr,10),He(Zg,10),He(Ku,8))};static ɵdir=Y({type:t,features:[Ve,bt]})}return t})();var hO={provide:Po,useExisting:Ht(()=>Pc)};var Pc=(()=>{class t extends pO{form=null;ngSubmit=new ee;get control(){return this.form}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=Y({type:t,selectors:[[``,`formGroup`,``]],hostBindings:function(i,a){i&1&&C(`submit`,function(r){return a.onSubmit(r)})(`reset`,function(){return a.onReset()})},inputs:{form:[0,`formGroup`,`form`]},outputs:{ngSubmit:`ngSubmit`},exportAs:[`ngForm`],standalone:!1,features:[Ge([hO]),Ve]})}return t})();var fO={provide:Mi,useExisting:Ht(()=>ib)};var pS=Promise.resolve();var ib=(()=>{class t extends Mi{_changeDetectorRef;callSetDisabledState;control=new OS;static ngAcceptInputType_isDisabled;_registered=!1;_ngModelInjector;viewModel;name=``;isDisabled;model;options;update=new ee;constructor(e,i,a,o,r,s,c,p){super(c,p,o),this._changeDetectorRef=r,this.callSetDisabledState=s,this._parent=e,this._setValidators(i),this._setAsyncValidators(a)}ngOnChanges(e){if(this._registered,this._checkForErrors(),!this._registered||`name`in e){if(this._registered&&(this._checkName(),this.formDirective)){let i=e.name.previousValue;this.formDirective.removeControl({name:i,path:this._getPath(i)})}this._setUpControl()}`isDisabled`in e&&this._updateDisabled(e),rO(e,this.viewModel)&&(this._updateValue(this.model),this.viewModel=this.model)}ngOnDestroy(){this.formDirective?.removeControl(this)}ɵngControlCreate(e){super.ngControlCreate(e)}ɵngControlUpdate(e){super.ngControlUpdate(e,!1)}get shouldBindRequired(){return!1}get path(){return this._getPath(this.name)}get formDirective(){return this._parent?this._parent.formDirective:null}viewToModelUpdate(e){this.viewModel=e,this.update.emit(e)}_setUpControl(){this._setUpdateStrategy(),this._isStandalone()?this._setUpStandalone():this.formDirective.addControl(this),this._registered=!0}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.control._updateOn=this.options.updateOn)}_isStandalone(){return!this._parent||!!(this.options&&this.options.standalone)}_setUpStandalone(){this.isCustomControlBased?this.setupCustomControl():(this.valueAccessor??=this.selectedValueAccessor,lS(this.control,this,this.callSetDisabledState)),this.control.updateValueAndValidity({emitEvent:!1})}_setupWithForm(e){this.isCustomControlBased?this.setupCustomControl():(this.valueAccessor??=this.selectedValueAccessor,lS(this.control,this,e))}_checkForErrors(){this._checkName()}_checkName(){this.options&&this.options.name&&(this.name=this.options.name),!this._isStandalone()&&this.name}_updateValue(e){pS.then(()=>{this.control.setValue(e,{emitViewToModelChange:!1}),this._changeDetectorRef?.markForCheck()})}_updateDisabled(e){let i=e.isDisabled.currentValue,a=i!==0&&V(i);pS.then(()=>{a&&!this.control.disabled?this.control.disable():!a&&this.control.disabled&&this.control.enable(),this._changeDetectorRef?.markForCheck()})}_getPath(e){return this._parent?eO(e,this._parent):[e]}static ɵfac=function(i){return new(i||t)(He(Po,9),He(Wr,10),He(Zg,10),He(Gr,10),He(Pe,8),He(Ku,8),He(re,8),He(Oe,8))};static ɵdir=Y({type:t,selectors:[[``,`ngModel`,``,3,`formControlName`,``,3,`formControl`,``]],inputs:{name:`name`,isDisabled:[0,`disabled`,`isDisabled`],model:[0,`ngModel`,`model`],options:[0,`ngModelOptions`,`options`]},outputs:{update:`ngModelChange`},exportAs:[`ngModel`],standalone:!1,features:[Ge([fO,dO]),Ve,bt,$f(null)]})}return t})();var gO=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({})}return t})();var RS=(()=>{class t{static withConfig(e){return{ngModule:t,providers:[{provide:Ku,useValue:e.callSetDisabledState??tb}]}}static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[gO]})}return t})();var FS=Symbol(`FIELD_TREE`);function Fc(t){return typeof t==`function`&&t[FS]===!0}var Qu=new I(``);var qr=(()=>{class t{isErrorState(e,i){return!!(e&&e.invalid&&(e.touched||i&&i.submitted))}isSignalErrorState(e){if(!e)return!1;let i=e().invalid(),a=e().touched();return i&&a}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Yr=class{_defaultMatcher;_parentFormGroup;_parentForm;_stateChanges;errorState=!1;matcher;ngControl;formField;constructor(n,e,i,a,o){this._defaultMatcher=n,this._parentFormGroup=i,this._parentForm=a,this._stateChanges=o,e?sn(e.field)&&!e.updateValueAndValidity?(this.formField=e,this.ngControl=null):(this.formField=null,this.ngControl=e):this.ngControl=this.formField=null}updateErrorState(){let n=this.errorState,e=this._getCurrentErrorState(this.matcher||this._defaultMatcher);e!==n&&(this.errorState=e,this._stateChanges.next())}_getCurrentErrorState(n){if(this.formField&&n?.isSignalErrorState)return n.isSignalErrorState(this.formField.field())??!1;let e=this._parentFormGroup||this._parentForm,i=this.ngControl?this.ngControl.control:null;return n?.isErrorState(i,e)??!1}};var ab=class{_box;_destroyed=new L;_resizeSubject=new L;_resizeObserver;_elementObservables=new Map;constructor(n){this._box=n,typeof ResizeObserver<`u`&&(this._resizeObserver=new ResizeObserver(e=>this._resizeSubject.next(e)))}observe(n){return this._elementObservables.has(n)||this._elementObservables.set(n,new Ce(e=>{let i=this._resizeSubject.subscribe(e);return this._resizeObserver?.observe(n,{box:this._box}),()=>{this._resizeObserver?.unobserve(n),i.unsubscribe(),this._elementObservables.delete(n)}}).pipe(Qe(e=>e.some(i=>i.target===n)),Fl({bufferSize:1,refCount:!0}),Ne(this._destroyed))),this._elementObservables.get(n)}destroy(){this._destroyed.next(),this._destroyed.complete(),this._resizeSubject.complete(),this._elementObservables.clear()}};var Xu=(()=>{class t{_cleanupErrorListener;_observers=new Map;_ngZone=f(Q);constructor(){}ngOnDestroy(){for(let[,e]of this._observers)e.destroy();this._observers.clear(),this._cleanupErrorListener?.()}observe(e,i){let a=i?.box||`content-box`;return this._observers.has(a)||this._observers.set(a,new ab(a)),this._observers.get(a).observe(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var na=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[`mat-label`]]})}return t})();var vO=new I(`MatError`);var ob=(()=>{class t{align=`start`;id=f(Be).getId(`mat-mdc-hint-`);static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[`mat-hint`]],hostAttrs:[1,`mat-mdc-form-field-hint`,`mat-mdc-form-field-bottom-align`],hostVars:4,hostBindings:function(i,a){i&2&&(qt(`id`,a.id),te(`align`,null),q(`mat-mdc-form-field-hint-end`,a.align===`end`))},inputs:{align:`align`,id:`id`}})}return t})();var yO=new I(`MatPrefix`);var _O=new I(`MatSuffix`);var $S=new I(`FloatingLabelParent`);var VS=(()=>{class t{_elementRef=f(W);get floating(){return this._floating}set floating(e){this._floating=e,this.monitorResize&&this._handleResize()}_floating=!1;get monitorResize(){return this._monitorResize}set monitorResize(e){this._monitorResize=e,this._monitorResize?this._subscribeToResize():this._resizeSubscription.unsubscribe()}_monitorResize=!1;_resizeObserver=f(Xu);_ngZone=f(Q);_parent=f($S);_resizeSubscription=new De;ngOnDestroy(){this._resizeSubscription.unsubscribe()}getWidth(){return xO(this._elementRef.nativeElement)}get element(){return this._elementRef.nativeElement}_handleResize(){setTimeout(()=>this._parent._handleLabelResized())}_subscribeToResize(){this._resizeSubscription.unsubscribe(),this._ngZone.runOutsideAngular(()=>{this._resizeSubscription=this._resizeObserver.observe(this._elementRef.nativeElement,{box:`border-box`}).subscribe(()=>this._handleResize())})}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[`label`,`matFormFieldFloatingLabel`,``]],hostAttrs:[1,`mdc-floating-label`,`mat-mdc-floating-label`],hostVars:2,hostBindings:function(i,a){i&2&&q(`mdc-floating-label--float-above`,a.floating)},inputs:{floating:`floating`,monitorResize:`monitorResize`}})}return t})();function xO(t){let n=t;if(n.offsetParent!==null)return n.scrollWidth;let e=n.cloneNode(!0);e.style.setProperty(`position`,`absolute`),e.style.setProperty(`transform`,`translate(-9999px, -9999px)`),document.documentElement.appendChild(e);let i=e.scrollWidth;return e.remove(),i}var BS=`mdc-line-ripple--active`;var Zu=`mdc-line-ripple--deactivating`;var jS=(()=>{class t{_elementRef=f(W);_cleanupTransitionEnd;constructor(){let e=f(Q),i=f(Oe);e.runOutsideAngular(()=>{this._cleanupTransitionEnd=i.listen(this._elementRef.nativeElement,`transitionend`,this._handleTransitionEnd)})}activate(){let e=this._elementRef.nativeElement.classList;e.remove(Zu),e.add(BS)}deactivate(){this._elementRef.nativeElement.classList.add(Zu)}_handleTransitionEnd=e=>{let i=this._elementRef.nativeElement.classList,a=i.contains(Zu);e.propertyName===`opacity`&&a&&i.remove(BS,Zu)};ngOnDestroy(){this._cleanupTransitionEnd()}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[`div`,`matFormFieldLineRipple`,``]],hostAttrs:[1,`mdc-line-ripple`]})}return t})();var zS=(()=>{class t{_elementRef=f(W);_ngZone=f(Q);open=!1;_notch;ngAfterViewInit(){let e=this._elementRef.nativeElement,i=e.querySelector(`.mdc-floating-label`);i?(e.classList.add(`mdc-notched-outline--upgraded`),typeof requestAnimationFrame==`function`&&(i.style.transitionDuration=`0s`,this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>i.style.transitionDuration=``)}))):e.classList.add(`mdc-notched-outline--no-label`)}_setNotchWidth(e){let i=this._notch.nativeElement;!this.open||!e?i.style.width=``:i.style.width=`calc(${e}px * var(--mat-mdc-form-field-floating-label-scale, 0.75) + 9px)`}_setMaxWidth(e){this._notch.nativeElement.style.setProperty(`--mat-form-field-notch-max-width`,`calc(100% - ${e}px)`)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`notch`];return B({type:t,selectors:[[`div`,`matFormFieldNotchedOutline`,``]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;Z(s=J())&&(r._notch=s.first)}},hostAttrs:[1,`mdc-notched-outline`],hostVars:2,hostBindings:function(o,r){o&2&&q(`mdc-notched-outline--notched`,r.open)},inputs:{open:[0,`matFormFieldNotchedOutlineOpen`,`open`]},ngContentSelectors:[`*`],decls:5,vars:0,consts:[[`notch`,``],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__leading`],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__notch`],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__trailing`]],template:function(o,r){o&1&&(Re(),Wt(0,`div`,1),st(1,`div`,2,0),me(3),pt(),Wt(4,`div`,3))},encapsulation:2})})()}return t})();var Lc=(()=>{class t{id;ngField=null;ngControl=null;focused=!1;empty=!1;shouldLabelFloat=!1;required=!1;disabled=!1;errorState=!1;controlType;autofilled;userAriaDescribedBy;disableAutomaticLabeling;describedByIds;stateChanges=null;value;static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t})}return t})();var Vc=new I(`MatFormField`);var CO=new I(`MAT_FORM_FIELD_DEFAULT_OPTIONS`);var HS=`fill`;var SO=`auto`;var US=`fixed`;var wO=`translateY(-50%)`;var Ni=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Pe);_platform=f(Ae);_idGenerator=f(Be);_ngZone=f(Q);_defaults=f(CO,{optional:!0});_currentDirection;_unwrapMaybeSignal(e){return sn(e)?e():e}_textField;_iconPrefixContainer;_textPrefixContainer;_iconSuffixContainer;_textSuffixContainer;_floatingLabel;_notchedOutline;_lineRipple;_iconPrefixContainerSignal=lc(`iconPrefixContainer`);_textPrefixContainerSignal=lc(`textPrefixContainer`);_iconSuffixContainerSignal=lc(`iconSuffixContainer`);_textSuffixContainerSignal=lc(`textSuffixContainer`);_prefixSuffixContainers=ne(()=>[this._iconPrefixContainerSignal(),this._textPrefixContainerSignal(),this._iconSuffixContainerSignal(),this._textSuffixContainerSignal()].map(e=>e?.nativeElement).filter(e=>e!==void 0));_formFieldControl;_prefixChildren;_suffixChildren;_errorChildren;_hintChildren;_labelChild=vx(na);get hideRequiredMarker(){return this._hideRequiredMarker}set hideRequiredMarker(e){this._hideRequiredMarker=ta(e)}_hideRequiredMarker=!1;color=`primary`;get floatLabel(){return this._floatLabel||this._defaults?.floatLabel||SO}set floatLabel(e){e!==this._floatLabel&&(this._floatLabel=e,this._changeDetectorRef.markForCheck())}_floatLabel;get appearance(){return this._appearanceSignal()}set appearance(e){let i=e||this._defaults?.appearance||HS;this._appearanceSignal.set(i)}_appearanceSignal=N(HS);get subscriptSizing(){return this._subscriptSizing||this._defaults?.subscriptSizing||US}set subscriptSizing(e){this._subscriptSizing=e||this._defaults?.subscriptSizing||US}_subscriptSizing=null;get hintLabel(){return this._hintLabel}set hintLabel(e){this._hintLabel=e,this._processHints()}_hintLabel=``;_hasIconPrefix=!1;_hasTextPrefix=!1;_hasIconSuffix=!1;_hasTextSuffix=!1;_labelId=this._idGenerator.getId(`mat-mdc-form-field-label-`);_hintLabelId=this._idGenerator.getId(`mat-mdc-hint-`);_describedByIds;get _control(){return this._explicitFormFieldControl||this._formFieldControl}set _control(e){this._explicitFormFieldControl=e}_destroyed=new L;_isFocused=null;_explicitFormFieldControl;_previousControl=null;_previousControlValidatorFn=null;_stateChanges;_valueChanges;_describedByChanges;_outlineLabelOffsetResizeObserver=null;_animationsDisabled=Ue();constructor(){let e=this._defaults,i=f(ft);e&&(e.appearance&&(this.appearance=e.appearance),this._hideRequiredMarker=!!e?.hideRequiredMarker,e.color&&(this.color=e.color)),Ft(()=>this._currentDirection=i.valueSignal()),this._syncOutlineLabelOffset()}ngAfterViewInit(){this._updateFocusState(),this._animationsDisabled||this._ngZone.runOutsideAngular(()=>{setTimeout(()=>{this._elementRef.nativeElement.classList.add(`mat-form-field-animations-enabled`)},300)}),this._changeDetectorRef.detectChanges()}ngAfterContentInit(){this._assertFormFieldControl(),this._initializeSubscript(),this._initializePrefixAndSuffix()}ngAfterContentChecked(){this._assertFormFieldControl(),this._control!==this._previousControl&&(this._initializeControl(this._previousControl),this._control.ngControl&&this._control.ngControl.control&&(this._previousControlValidatorFn=Fc(this._control.ngField)?null:this._control.ngControl.control.validator),this._previousControl=this._control,this._changeDetectorRef.markForCheck()),this._control.ngControl&&this._control.ngControl.control&&!Fc(this._control.ngField)&&this._control.ngControl.control.validator!==this._previousControlValidatorFn&&this._changeDetectorRef.markForCheck()}ngOnDestroy(){this._outlineLabelOffsetResizeObserver?.disconnect(),this._stateChanges?.unsubscribe(),this._valueChanges?.unsubscribe(),this._describedByChanges?.unsubscribe(),this._destroyed.next(),this._destroyed.complete()}getLabelId=ne(()=>this._hasFloatingLabel()?this._labelId:null);getConnectedOverlayOrigin(){return this._textField||this._elementRef}_animateAndLockLabel(){this._hasFloatingLabel()&&(this.floatLabel=`always`)}_initializeControl(e){let i=this._control,a=`mat-mdc-form-field-type-`;e&&this._elementRef.nativeElement.classList.remove(a+e.controlType),i.controlType&&this._elementRef.nativeElement.classList.add(a+i.controlType),this._stateChanges?.unsubscribe(),this._stateChanges=i.stateChanges?.subscribe(()=>{this._updateFocusState(),this._changeDetectorRef.markForCheck()}),this._describedByChanges?.unsubscribe(),this._describedByChanges=i.stateChanges?.pipe(ut([void 0,void 0]),rt(()=>[this._unwrapMaybeSignal(i.errorState),i.userAriaDescribedBy]),Pl(),Qe(([[o,r],[s,c]])=>o!==s||r!==c)).subscribe(()=>this._syncDescribedByIds()),this._valueChanges?.unsubscribe(),i.ngControl&&i.ngControl.valueChanges&&!Fc(i.ngField)&&(this._valueChanges=i.ngControl.valueChanges.pipe(Ne(this._destroyed)).subscribe(()=>this._changeDetectorRef.markForCheck()))}_checkPrefixAndSuffixTypes(){this._hasIconPrefix=!!this._prefixChildren.find(e=>!e._isText),this._hasTextPrefix=!!this._prefixChildren.find(e=>e._isText),this._hasIconSuffix=!!this._suffixChildren.find(e=>!e._isText),this._hasTextSuffix=!!this._suffixChildren.find(e=>e._isText)}_initializePrefixAndSuffix(){this._checkPrefixAndSuffixTypes(),Dt(this._prefixChildren.changes,this._suffixChildren.changes).subscribe(()=>{this._checkPrefixAndSuffixTypes(),this._changeDetectorRef.markForCheck()})}_initializeSubscript(){this._hintChildren.changes.subscribe(()=>{this._processHints(),this._changeDetectorRef.markForCheck()}),this._errorChildren.changes.subscribe(()=>{this._syncDescribedByIds(),this._changeDetectorRef.markForCheck()}),this._validateHints(),this._syncDescribedByIds()}_assertFormFieldControl(){this._control}_updateFocusState(){let e=this._unwrapMaybeSignal(this._control.focused);e&&!this._isFocused?(this._isFocused=!0,this._lineRipple?.activate()):!e&&(this._isFocused||this._isFocused===null)&&(this._isFocused=!1,this._lineRipple?.deactivate()),this._elementRef.nativeElement.classList.toggle(`mat-focused`,e),this._textField?.nativeElement.classList.toggle(`mdc-text-field--focused`,e)}_syncOutlineLabelOffset(){ig({earlyRead:()=>{if(this._appearanceSignal()!==`outline`)return this._outlineLabelOffsetResizeObserver?.disconnect(),null;if(globalThis.ResizeObserver){this._outlineLabelOffsetResizeObserver||=new globalThis.ResizeObserver(()=>{this._writeOutlinedLabelStyles(this._getOutlinedLabelOffset())});for(let e of this._prefixSuffixContainers())this._outlineLabelOffsetResizeObserver.observe(e,{box:`border-box`})}return this._getOutlinedLabelOffset()},write:e=>this._writeOutlinedLabelStyles(e())})}_shouldAlwaysFloat(){return this.floatLabel===`always`}_hasOutline(){return this.appearance===`outline`}_forceDisplayInfixLabel(){return!this._platform.isBrowser&&this._prefixChildren.length&&!this._shouldLabelFloat()}_hasFloatingLabel=ne(()=>!!this._labelChild());_shouldLabelFloat(){return this._hasFloatingLabel()?this._shouldAlwaysFloat()||this._unwrapMaybeSignal(this._control.shouldLabelFloat):!1}_shouldForward(e){let i=this._control?.ngField||this._control?.ngControl;if(!i)return!1;if(Fc(i)){let a=i();return e===`valid`?a.valid():e===`dirty`?a.dirty():e===`touched`?a.touched():e===`pending`?a.pending():e===`untouched`?!a.touched():e===`pristine`?!a.dirty():e===`invalid`?!a.valid():!1}else{let a=i;return e===`valid`?a.valid:e===`dirty`?a.dirty:e===`touched`?a.touched:e===`pending`?a.pending:e===`untouched`?a.untouched:e===`pristine`?a.pristine:e===`invalid`?a.invalid:!1}}_getSubscriptMessageType(){return this._errorChildren&&this._errorChildren.length>0&&this._unwrapMaybeSignal(this._control.errorState)?`error`:`hint`}_handleLabelResized(){this._refreshOutlineNotchWidth()}_refreshOutlineNotchWidth(){!this._hasOutline()||!this._floatingLabel||!this._shouldLabelFloat()?this._notchedOutline?._setNotchWidth(0):this._notchedOutline?._setNotchWidth(this._floatingLabel.getWidth())}_processHints(){this._validateHints(),this._syncDescribedByIds()}_validateHints(){this._hintChildren}_syncDescribedByIds(){if(this._control){let e=[];if(this._control.userAriaDescribedBy&&typeof this._control.userAriaDescribedBy==`string`&&e.push(...this._control.userAriaDescribedBy.split(` `)),this._getSubscriptMessageType()===`hint`){let o=this._hintChildren?this._hintChildren.find(s=>s.align===`start`):null,r=this._hintChildren?this._hintChildren.find(s=>s.align===`end`):null;o?e.push(o.id):this._hintLabel&&e.push(this._hintLabelId),r&&e.push(r.id)}else this._errorChildren&&e.push(...this._errorChildren.map(o=>o.id));let i=this._control.describedByIds,a;if(i){let o=this._describedByIds||e;a=e.concat(i.filter(r=>r&&!o.includes(r)))}else a=e;this._control.setDescribedByIds(a),this._describedByIds=e}}_getOutlinedLabelOffset(){if(!this._hasOutline()||!this._floatingLabel)return null;if(!this._iconPrefixContainer&&!this._textPrefixContainer)return[``,null];if(!this._isAttachedToDom())return null;let e=this._iconPrefixContainer?.nativeElement,i=this._textPrefixContainer?.nativeElement,a=this._iconSuffixContainer?.nativeElement,o=this._textSuffixContainer?.nativeElement,r=e?.getBoundingClientRect().width??0,s=i?.getBoundingClientRect().width??0,c=a?.getBoundingClientRect().width??0,p=o?.getBoundingClientRect().width??0;return[`var(--mat-mdc-form-field-label-transform, ${wO} translateX(${`calc(${this._currentDirection===`rtl`?`-1`:`1`} * (${`${r+s}px`} + var(--mat-mdc-form-field-label-offset-x, 0px)))`}))`,r+s+c+p]}_writeOutlinedLabelStyles(e){if(e!==null){let[i,a]=e;this._floatingLabel&&(this._floatingLabel.element.style.transform=i),a!==null&&this._notchedOutline?._setMaxWidth(a)}}_isAttachedToDom(){let e=this._elementRef.nativeElement;if(e.getRootNode){let i=e.getRootNode();return i&&i!==e}return document.documentElement.contains(e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`iconPrefixContainer`],i=[`textPrefixContainer`],a=[`iconSuffixContainer`],o=[`textSuffixContainer`],r=[`textField`],s=[`*`,[[`mat-label`]],[[``,`matPrefix`,``],[``,`matIconPrefix`,``]],[[``,`matTextPrefix`,``]],[[``,`matTextSuffix`,``]],[[``,`matSuffix`,``],[``,`matIconSuffix`,``]],[[`mat-error`],[``,`matError`,``]],[[`mat-hint`,3,`align`,`end`]],[[`mat-hint`,`align`,`end`]]],c=[`*`,`mat-label`,`[matPrefix], [matIconPrefix]`,`[matTextPrefix]`,`[matTextSuffix]`,`[matSuffix], [matIconSuffix]`,`mat-error, [matError]`,`mat-hint:not([align='end'])`,`mat-hint[align='end']`];function p(ue,ke){ue&1&&z(0,`span`,21)}function m(ue,ke){if(ue&1&&(l(0,`label`,20),me(1,1),D(2,p,1,0,`span`,21),d()),ue&2){let F=y(2);S(`floating`,F._shouldLabelFloat())(`monitorResize`,F._hasOutline())(`id`,F._labelId),te(`for`,F._control.disableAutomaticLabeling?null:F._control.id),h(2),k(!F.hideRequiredMarker&&F._unwrapMaybeSignal(F._control.required)?2:-1)}}function g(ue,ke){if(ue&1&&D(0,m,3,5,`label`,20),ue&2)k(y()._hasFloatingLabel()?0:-1)}function v(ue,ke){ue&1&&z(0,`div`,7)}function b(ue,ke){}function _(ue,ke){if(ue&1&&ot(0,b,0,0,`ng-template`,13),ue&2){y(2);S(`ngTemplateOutlet`,ht(1))}}function E(ue,ke){if(ue&1&&(l(0,`div`,9),D(1,_,1,1,null,13),d()),ue&2){let F=y();S(`matFormFieldNotchedOutlineOpen`,F._shouldLabelFloat()),h(),k(F._forceDisplayInfixLabel()?-1:1)}}function P(ue,ke){ue&1&&(l(0,`div`,10,2),me(2,2),d())}function ae(ue,ke){ue&1&&(l(0,`div`,11,3),me(2,3),d())}function j(ue,ke){}function x(ue,ke){if(ue&1&&ot(0,j,0,0,`ng-template`,13),ue&2){y();S(`ngTemplateOutlet`,ht(1))}}function M(ue,ke){ue&1&&(l(0,`div`,14,4),me(2,4),d())}function T(ue,ke){ue&1&&(l(0,`div`,15,5),me(2,5),d())}function $(ue,ke){ue&1&&z(0,`div`,16)}function ye(ue,ke){ue&1&&(l(0,`div`,18),me(1,6),d())}function gt(ue,ke){if(ue&1&&(l(0,`mat-hint`,22),u(1),d()),ue&2){let F=y(2);S(`id`,F._hintLabelId),h(),w(F.hintLabel)}}function We(ue,ke){if(ue&1&&(l(0,`div`,19),D(1,gt,2,2,`mat-hint`,22),me(2,7),z(3,`div`,23),me(4,8),d()),ue&2){let F=y();h(),k(F.hintLabel?1:-1)}}return B({type:t,selectors:[[`mat-form-field`]],contentQueries:function(ke,F,qe){if(ke&1&&(qd(qe,F._labelChild,na,5),Bt(qe,Lc,5)(qe,yO,5)(qe,_O,5)(qe,vO,5)(qe,ob,5)),ke&2){Kd();let nn;Z(nn=J())&&(F._formFieldControl=nn.first),Z(nn=J())&&(F._prefixChildren=nn),Z(nn=J())&&(F._suffixChildren=nn),Z(nn=J())&&(F._errorChildren=nn),Z(nn=J())&&(F._hintChildren=nn)}},viewQuery:function(ke,F){if(ke&1&&(Yd(F._iconPrefixContainerSignal,e,5)(F._textPrefixContainerSignal,i,5)(F._iconSuffixContainerSignal,a,5)(F._textSuffixContainerSignal,o,5),Ze(r,5)(e,5)(i,5)(a,5)(o,5)(VS,5)(zS,5)(jS,5)),ke&2){Kd(4);let qe;Z(qe=J())&&(F._textField=qe.first),Z(qe=J())&&(F._iconPrefixContainer=qe.first),Z(qe=J())&&(F._textPrefixContainer=qe.first),Z(qe=J())&&(F._iconSuffixContainer=qe.first),Z(qe=J())&&(F._textSuffixContainer=qe.first),Z(qe=J())&&(F._floatingLabel=qe.first),Z(qe=J())&&(F._notchedOutline=qe.first),Z(qe=J())&&(F._lineRipple=qe.first)}},hostAttrs:[1,`mat-mdc-form-field`],hostVars:38,hostBindings:function(ke,F){ke&2&&q(`mat-mdc-form-field-label-always-float`,F._shouldAlwaysFloat())(`mat-mdc-form-field-has-icon-prefix`,F._hasIconPrefix)(`mat-mdc-form-field-has-icon-suffix`,F._hasIconSuffix)(`mat-form-field-invalid`,F._unwrapMaybeSignal(F._control.errorState))(`mat-form-field-disabled`,F._unwrapMaybeSignal(F._control.disabled))(`mat-form-field-autofilled`,F._unwrapMaybeSignal(F._control.autofilled))(`mat-form-field-appearance-fill`,F.appearance==`fill`)(`mat-form-field-appearance-outline`,F.appearance==`outline`)(`mat-form-field-hide-placeholder`,F._hasFloatingLabel()&&!F._shouldLabelFloat())(`mat-primary`,F.color!==`accent`&&F.color!==`warn`)(`mat-accent`,F.color===`accent`)(`mat-warn`,F.color===`warn`)(`ng-untouched`,F._shouldForward(`untouched`))(`ng-touched`,F._shouldForward(`touched`))(`ng-pristine`,F._shouldForward(`pristine`))(`ng-dirty`,F._shouldForward(`dirty`))(`ng-valid`,F._shouldForward(`valid`))(`ng-invalid`,F._shouldForward(`invalid`))(`ng-pending`,F._shouldForward(`pending`))},inputs:{hideRequiredMarker:`hideRequiredMarker`,color:`color`,floatLabel:`floatLabel`,appearance:`appearance`,subscriptSizing:`subscriptSizing`,hintLabel:`hintLabel`},exportAs:[`matFormField`],features:[Ge([{provide:Vc,useExisting:t},{provide:$S,useExisting:t}])],ngContentSelectors:c,decls:18,vars:21,consts:[[`labelTemplate`,``],[`textField`,``],[`iconPrefixContainer`,``],[`textPrefixContainer`,``],[`textSuffixContainer`,``],[`iconSuffixContainer`,``],[1,`mat-mdc-text-field-wrapper`,`mdc-text-field`,3,`click`],[1,`mat-mdc-form-field-focus-overlay`],[1,`mat-mdc-form-field-flex`],[`matFormFieldNotchedOutline`,``,3,`matFormFieldNotchedOutlineOpen`],[1,`mat-mdc-form-field-icon-prefix`],[1,`mat-mdc-form-field-text-prefix`],[1,`mat-mdc-form-field-infix`],[3,`ngTemplateOutlet`],[1,`mat-mdc-form-field-text-suffix`],[1,`mat-mdc-form-field-icon-suffix`],[`matFormFieldLineRipple`,``],[`aria-atomic`,`true`,`aria-live`,`polite`,1,`mat-mdc-form-field-subscript-wrapper`,`mat-mdc-form-field-bottom-align`],[1,`mat-mdc-form-field-error-wrapper`],[1,`mat-mdc-form-field-hint-wrapper`],[`matFormFieldFloatingLabel`,``,3,`floating`,`monitorResize`,`id`],[`aria-hidden`,`true`,1,`mat-mdc-form-field-required-marker`,`mdc-floating-label--required`],[3,`id`],[1,`mat-mdc-form-field-hint-spacer`]],template:function(ke,F){if(ke&1&&(Re(s),ot(0,g,1,1,`ng-template`,null,0,rc),l(2,`div`,6,1),C(`click`,function(nn){return F._control.onContainerClick(nn)}),D(4,v,1,0,`div`,7),l(5,`div`,8),D(6,E,2,2,`div`,9),D(7,P,3,0,`div`,10),D(8,ae,3,0,`div`,11),l(9,`div`,12),D(10,x,1,1,null,13),me(11),d(),D(12,M,3,0,`div`,14),D(13,T,3,0,`div`,15),d(),D(14,$,1,0,`div`,16),d(),l(15,`div`,17),D(16,ye,2,0,`div`,18)(17,We,5,1,`div`,19),d()),ke&2){let qe,nn=F._unwrapMaybeSignal(F._control.disabled);h(2),q(`mdc-text-field--filled`,!F._hasOutline())(`mdc-text-field--outlined`,F._hasOutline())(`mdc-text-field--no-label`,!F._hasFloatingLabel())(`mdc-text-field--disabled`,nn)(`mdc-text-field--invalid`,F._unwrapMaybeSignal(F._control.errorState)),h(2),k(!F._hasOutline()&&!nn?4:-1),h(2),k(F._hasOutline()?6:-1),h(),k(F._hasIconPrefix?7:-1),h(),k(F._hasTextPrefix?8:-1),h(2),k(!F._hasOutline()||F._forceDisplayInfixLabel()?10:-1),h(2),k(F._hasTextSuffix?12:-1),h(),k(F._hasIconSuffix?13:-1),h(),k(F._hasOutline()?-1:14),h(),q(`mat-mdc-form-field-subscript-dynamic-size`,F.subscriptSizing===`dynamic`);let sE=F._getSubscriptMessageType();h(),k((qe=sE)===`error`?16:qe===`hint`?17:-1)}},dependencies:[VS,zS,fc,jS,ob],styles:[`.mdc-text-field {
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
`],encapsulation:2})})()}return t})();var EO=new I(`mat-chips-default-options`,{providedIn:`root`,factory:()=>({separatorKeyCodes:[13]})});var GS=new I(`MatChipAvatar`);var WS=new I(`MatChipTrailingIcon`);var qS=new I(`MatChipEdit`);var YS=new I(`MatChipRemove`);var KS=new I(`MatChip`);var QS=(()=>{class t{_elementRef=f(W);_parentChip=f(KS);_isPrimary=!0;_isLeading=!1;get disabled(){return this._disabled||this._parentChip?.disabled||!1}set disabled(e){this._disabled=e}_disabled=!1;tabIndex=-1;_allowFocusWhenDisabled=!1;_getDisabledAttribute(){return this.disabled&&!this._allowFocusWhenDisabled?``:null}constructor(){f(Je).load(dn),this._elementRef.nativeElement.nodeName===`BUTTON`&&this._elementRef.nativeElement.setAttribute(`type`,`button`)}focus(){this._elementRef.nativeElement.focus()}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`matChipContent`,``]],hostAttrs:[1,`mat-mdc-chip-action`,`mdc-evolution-chip__action`,`mdc-evolution-chip__action--presentational`],hostVars:8,hostBindings:function(i,a){i&2&&(te(`disabled`,a._getDisabledAttribute())(`aria-disabled`,a.disabled),q(`mdc-evolution-chip__action--primary`,a._isPrimary)(`mdc-evolution-chip__action--secondary`,!a._isPrimary)(`mdc-evolution-chip__action--trailing`,!a._isPrimary&&!a._isLeading))},inputs:{disabled:[2,`disabled`,`disabled`,V],tabIndex:[2,`tabIndex`,`tabIndex`,e=>e==null?-1:ct(e)],_allowFocusWhenDisabled:`_allowFocusWhenDisabled`}})}return t})();var DO=(()=>{class t extends QS{_getTabindex(){return this.disabled&&!this._allowFocusWhenDisabled?null:this.tabIndex.toString()}_handleClick(e){!this.disabled&&this._isPrimary&&(e.preventDefault(),this._parentChip._handlePrimaryActionInteraction())}_handleKeydown(e){(e.keyCode===13||e.keyCode===32)&&!this.disabled&&this._isPrimary&&!this._parentChip._isEditing&&(e.preventDefault(),this._parentChip._handlePrimaryActionInteraction())}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=Y({type:t,selectors:[[``,`matChipAction`,``]],hostVars:3,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._handleClick(r)})(`keydown`,function(r){return a._handleKeydown(r)}),i&2&&(te(`tabindex`,a._getTabindex()),q(`mdc-evolution-chip__action--presentational`,!1))},features:[Ve]})}return t})();var Bc=(()=>{class t{_changeDetectorRef=f(Pe);_elementRef=f(W);_tagName=f(fx);_ngZone=f(Q);_focusMonitor=f(Jt);_globalRippleOptions=f(jr,{optional:!0});_document=f(de);_onFocus=new L;_onBlur=new L;_isBasicChip=!1;role=null;_hasFocusInternal=!1;_pendingFocus=!1;_actionChanges;_animationsDisabled=Ue();_allLeadingIcons;_allTrailingIcons;_allEditIcons;_allRemoveIcons;_hasFocus(){return this._hasFocusInternal}id=f(Be).getId(`mat-mdc-chip-`);ariaLabel=null;ariaDescription=null;_chipListDisabled=!1;_hadFocusOnRemove=!1;_textElement;get value(){return this._value!==void 0?this._value:this._textElement.textContent.trim()}set value(e){this._value=e}_value;color;removable=!0;highlighted=!1;disableRipple=!1;get disabled(){return this._disabled||this._chipListDisabled}set disabled(e){this._disabled=e}_disabled=!1;removed=new ee;destroyed=new ee;basicChipAttrName=`mat-basic-chip`;leadingIcon;editIcon;trailingIcon;removeIcon;primaryAction;_rippleLoader=f(Pu);_injector=f(re);constructor(){let e=f(Je);e.load(dn),e.load(ki),this._monitorFocus(),this._rippleLoader?.configureRipple(this._elementRef.nativeElement,{className:`mat-mdc-chip-ripple`,disabled:this._isRippleDisabled()})}ngOnInit(){this._isBasicChip=this._elementRef.nativeElement.hasAttribute(this.basicChipAttrName)||this._tagName.toLowerCase()===this.basicChipAttrName}ngAfterViewInit(){this._textElement=this._elementRef.nativeElement.querySelector(`.mat-mdc-chip-action-label`),this._pendingFocus&&(this._pendingFocus=!1,this.focus())}ngAfterContentInit(){this._actionChanges=Dt(this._allLeadingIcons.changes,this._allTrailingIcons.changes,this._allEditIcons.changes,this._allRemoveIcons.changes).subscribe(()=>this._changeDetectorRef.markForCheck())}ngDoCheck(){this._rippleLoader.setDisabled(this._elementRef.nativeElement,this._isRippleDisabled())}ngOnDestroy(){this.destroyed.emit({chip:this}),this.destroyed.complete(),this._focusMonitor.stopMonitoring(this._elementRef),this._rippleLoader?.destroyRipple(this._elementRef.nativeElement),this._actionChanges?.unsubscribe()}remove(){this.removable&&(this._hadFocusOnRemove=this._hasFocus(),this.removed.emit({chip:this}))}_isRippleDisabled(){return this.disabled||this.disableRipple||this._animationsDisabled||this._isBasicChip||!this._hasInteractiveActions()||!!this._globalRippleOptions?.disabled}_hasTrailingIcon(){return!!(this.trailingIcon||this.removeIcon)}_handleKeydown(e){(e.keyCode===8&&!e.repeat||e.keyCode===46)&&(e.preventDefault(),this.remove())}focus(){this.disabled||(this.primaryAction?this.primaryAction.focus():this._pendingFocus=!0)}_getSourceAction(e){return this._getActions().find(i=>{let a=i._elementRef.nativeElement;return a===e||a.contains(e)})}_getActions(){let e=[];return this.editIcon&&e.push(this.editIcon),this.primaryAction&&e.push(this.primaryAction),this.removeIcon&&e.push(this.removeIcon),e}_handlePrimaryActionInteraction(){}_hasInteractiveActions(){return this._getActions().length>0}_edit(e){}_monitorFocus(){this._focusMonitor.monitor(this._elementRef,!0).subscribe(e=>{let i=e!==null;i!==this._hasFocusInternal&&(this._hasFocusInternal=i,i?this._onFocus.next({chip:this}):(this._changeDetectorRef.markForCheck(),setTimeout(()=>this._ngZone.run(()=>this._onBlur.next({chip:this})))))})}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[`mat-chip-avatar`],[``,`matChipAvatar`,``]],[[`mat-chip-trailing-icon`],[``,`matChipRemove`,``],[``,`matChipTrailingIcon`,``]]],i=[`*`,`mat-chip-avatar, [matChipAvatar]`,`mat-chip-trailing-icon,[matChipRemove],[matChipTrailingIcon]`];function a(r,s){r&1&&(l(0,`span`,3),me(1,1),d())}function o(r,s){r&1&&(l(0,`span`,6),me(1,2),d())}return B({type:t,selectors:[[`mat-basic-chip`],[``,`mat-basic-chip`,``],[`mat-chip`],[``,`mat-chip`,``]],contentQueries:function(s,c,p){if(s&1&&Bt(p,GS,5)(p,qS,5)(p,WS,5)(p,YS,5)(p,GS,5)(p,WS,5)(p,qS,5)(p,YS,5),s&2){let m;Z(m=J())&&(c.leadingIcon=m.first),Z(m=J())&&(c.editIcon=m.first),Z(m=J())&&(c.trailingIcon=m.first),Z(m=J())&&(c.removeIcon=m.first),Z(m=J())&&(c._allLeadingIcons=m),Z(m=J())&&(c._allTrailingIcons=m),Z(m=J())&&(c._allEditIcons=m),Z(m=J())&&(c._allRemoveIcons=m)}},viewQuery:function(s,c){if(s&1&&Ze(DO,5),s&2){let p;Z(p=J())&&(c.primaryAction=p.first)}},hostAttrs:[1,`mat-mdc-chip`],hostVars:31,hostBindings:function(s,c){s&1&&C(`keydown`,function(m){return c._handleKeydown(m)}),s&2&&(qt(`id`,c.id),te(`role`,c.role)(`aria-label`,c.ariaLabel),tt(`mat-`+(c.color||`primary`)),q(`mdc-evolution-chip`,!c._isBasicChip)(`mdc-evolution-chip--disabled`,c.disabled)(`mdc-evolution-chip--with-trailing-action`,c._hasTrailingIcon())(`mdc-evolution-chip--with-primary-graphic`,c.leadingIcon)(`mdc-evolution-chip--with-primary-icon`,c.leadingIcon)(`mdc-evolution-chip--with-avatar`,c.leadingIcon)(`mat-mdc-chip-with-avatar`,c.leadingIcon)(`mat-mdc-chip-highlighted`,c.highlighted)(`mat-mdc-chip-disabled`,c.disabled)(`mat-mdc-basic-chip`,c._isBasicChip)(`mat-mdc-standard-chip`,!c._isBasicChip)(`mat-mdc-chip-with-trailing-icon`,c._hasTrailingIcon())(`_mat-animation-noopable`,c._animationsDisabled))},inputs:{role:`role`,id:`id`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaDescription:[0,`aria-description`,`ariaDescription`],value:`value`,color:`color`,removable:[2,`removable`,`removable`,V],highlighted:[2,`highlighted`,`highlighted`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V],disabled:[2,`disabled`,`disabled`,V]},outputs:{removed:`removed`,destroyed:`destroyed`},exportAs:[`matChip`],features:[Ge([{provide:KS,useExisting:t}])],ngContentSelectors:i,decls:8,vars:2,consts:[[1,`mat-mdc-chip-focus-overlay`],[1,`mdc-evolution-chip__cell`,`mdc-evolution-chip__cell--primary`],[`matChipContent`,``],[1,`mdc-evolution-chip__graphic`,`mat-mdc-chip-graphic`],[1,`mdc-evolution-chip__text-label`,`mat-mdc-chip-action-label`],[1,`mat-mdc-chip-primary-focus-indicator`,`mat-focus-indicator`],[1,`mdc-evolution-chip__cell`,`mdc-evolution-chip__cell--trailing`]],template:function(s,c){s&1&&(Re(e),z(0,`span`,0),l(1,`span`,1)(2,`span`,2),D(3,a,2,0,`span`,3),l(4,`span`,4),me(5),z(6,`span`,5),d()()(),D(7,o,2,0,`span`,6)),s&2&&(h(3),k(c.leadingIcon?3:-1),h(4),k(c._hasTrailingIcon()?7:-1))},dependencies:[QS],styles:[`.mdc-evolution-chip,
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
`],encapsulation:2})})()}return t})();var Ju=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Pe);_dir=f(ft,{optional:!0});_lastDestroyedFocusedChipIndex=null;_keyManager;_destroyed=new L;_defaultRole=`presentation`;get chipFocusChanges(){return this._getChipStream(e=>e._onFocus)}get chipDestroyedChanges(){return this._getChipStream(e=>e.destroyed)}get chipRemovedChanges(){return this._getChipStream(e=>e.removed)}get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._syncChipsState()}_disabled=!1;get empty(){return!this._chips||this._chips.length===0}get role(){return this._explicitRole?this._explicitRole:this.empty?null:this._defaultRole}tabIndex=0;set role(e){this._explicitRole=e}_explicitRole=null;get focused(){return this._hasFocusedChip()}_chips;_chipActions=new Sn;ngAfterViewInit(){this._setUpFocusManagement(),this._trackChipSetChanges(),this._trackDestroyedFocusedChip()}ngOnDestroy(){this._keyManager?.destroy(),this._chipActions.destroy(),this._destroyed.next(),this._destroyed.complete()}_hasFocusedChip(){return this._chips&&this._chips.some(e=>e._hasFocus())}_syncChipsState(){this._chips?.forEach(e=>{e._chipListDisabled=this._disabled,e._changeDetectorRef.markForCheck()})}focus(){}_handleKeydown(e){this._originatesFromChip(e)&&this._keyManager.onKeydown(e)}_isValidIndex(e){return e>=0&&e<this._chips.length}_allowFocusEscape(){let e=this._elementRef.nativeElement.tabIndex;e!==-1&&(this._elementRef.nativeElement.tabIndex=-1,setTimeout(()=>this._elementRef.nativeElement.tabIndex=e))}_getChipStream(e){return this._chips.changes.pipe(ut(null),vn(()=>Dt(...this._chips.map(e))))}_originatesFromChip(e){let i=e.target;for(;i&&i!==this._elementRef.nativeElement;){if(i.classList.contains(`mat-mdc-chip`))return!0;i=i.parentElement}return!1}_setUpFocusManagement(){this._chips.changes.pipe(ut(this._chips)).subscribe(e=>{let i=[];e.forEach(a=>a._getActions().forEach(o=>i.push(o))),this._chipActions.reset(i),this._chipActions.notifyOnChanges()}),this._keyManager=new ea(this._chipActions).withVerticalOrientation().withHorizontalOrientation(this._dir?this._dir.value:`ltr`).withHomeAndEnd().skipPredicate(e=>this._skipPredicate(e)),this.chipFocusChanges.pipe(Ne(this._destroyed)).subscribe(({chip:e})=>{let i=e._getSourceAction(document.activeElement);i&&this._keyManager.updateActiveItem(i)}),this._dir?.change.pipe(Ne(this._destroyed)).subscribe(e=>this._keyManager.withHorizontalOrientation(e))}_skipPredicate(e){return e.disabled}_trackChipSetChanges(){this._chips.changes.pipe(ut(null),Ne(this._destroyed)).subscribe(()=>{this.disabled&&Promise.resolve().then(()=>this._syncChipsState()),this._redirectDestroyedChipFocus()})}_trackDestroyedFocusedChip(){this.chipDestroyedChanges.pipe(Ne(this._destroyed)).subscribe(e=>{let a=this._chips.toArray().indexOf(e.chip),o=e.chip._hasFocus(),r=e.chip._hadFocusOnRemove&&this._keyManager.activeItem&&e.chip._getActions().includes(this._keyManager.activeItem),s=o||r;this._isValidIndex(a)&&s&&(this._lastDestroyedFocusedChipIndex=a)})}_redirectDestroyedChipFocus(){if(this._lastDestroyedFocusedChipIndex!=null){if(this._chips.length){let e=Math.min(this._lastDestroyedFocusedChipIndex,this._chips.length-1),i=this._chips.toArray()[e];i.disabled?this._chips.length===1?this.focus():this._keyManager.setPreviousItemActive():i.focus()}else this.focus();this._lastDestroyedFocusedChipIndex=null}}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return B({type:t,selectors:[[`mat-chip-set`]],contentQueries:function(a,o,r){if(a&1&&Bt(r,Bc,5),a&2){let s;Z(s=J())&&(o._chips=s)}},hostAttrs:[1,`mat-mdc-chip-set`,`mdc-evolution-chip-set`],hostVars:1,hostBindings:function(a,o){a&1&&C(`keydown`,function(s){return o._handleKeydown(s)}),a&2&&te(`role`,o.role)},inputs:{disabled:[2,`disabled`,`disabled`,V],role:`role`,tabIndex:[2,`tabIndex`,`tabIndex`,i=>i==null?0:ct(i)]},ngContentSelectors:[`*`],decls:2,vars:0,consts:[[`role`,`presentation`,1,`mdc-evolution-chip-set__chips`]],template:function(a,o){a&1&&(Re(),st(0,`div`,0),me(1),pt())},styles:[`.mat-mdc-chip-set {
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
`],encapsulation:2})})()}return t})();var em=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({providers:[qr,{provide:EO,useValue:{separatorKeyCodes:[13]}}],imports:[Ii,we]})}return t})();var ZS=(()=>{class t{labelPosition=`after`;static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return B({type:t,selectors:[[``,`mat-internal-form-field`,``]],hostAttrs:[1,`mdc-form-field`,`mat-internal-form-field`],hostVars:2,hostBindings:function(a,o){a&2&&q(`mdc-form-field--align-end`,o.labelPosition===`before`)},inputs:{labelPosition:`labelPosition`},ngContentSelectors:[`*`],decls:1,vars:0,template:function(a,o){a&1&&(Re(),me(0))},styles:[`.mat-internal-form-field {
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
`],encapsulation:2})})()}return t})();var rb={color:`accent`,clickAction:`check-indeterminate`,disabledInteractive:!1};var kO=new I(`mat-checkbox-default-options`,{providedIn:`root`,factory:()=>rb});var tn=(function(t){return t[t.Init=0]=`Init`,t[t.Checked=1]=`Checked`,t[t.Unchecked=2]=`Unchecked`,t[t.Indeterminate=3]=`Indeterminate`,t})(tn||{});var sb=class{source;checked};var jc=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Pe);_ngZone=f(Q);_animationsDisabled=Ue();_options=f(kO,{optional:!0});focus(){this._inputElement.nativeElement.focus()}_createChangeEvent(e){let i=new sb;return i.source=this,i.checked=e,i}_getAnimationTargetElement(){return this._inputElement?.nativeElement}_animationClasses={uncheckedToChecked:`mdc-checkbox--anim-unchecked-checked`,uncheckedToIndeterminate:`mdc-checkbox--anim-unchecked-indeterminate`,checkedToUnchecked:`mdc-checkbox--anim-checked-unchecked`,checkedToIndeterminate:`mdc-checkbox--anim-checked-indeterminate`,indeterminateToChecked:`mdc-checkbox--anim-indeterminate-checked`,indeterminateToUnchecked:`mdc-checkbox--anim-indeterminate-unchecked`};ariaLabel=``;ariaLabelledby=null;ariaDescribedby;ariaExpanded;ariaControls;ariaOwns;_uniqueId;id;get inputId(){return`${this.id||this._uniqueId}-input`}required=!1;labelPosition=`after`;name=null;change=new ee;indeterminateChange=new ee;value;disableRipple=!1;_inputElement;tabIndex;color;disabledInteractive;_onTouched=()=>{};_currentAnimationClass=``;_currentCheckState=tn.Init;_controlValueAccessorChangeFn=()=>{};_validatorChangeFn=()=>{};constructor(){f(Je).load(dn);let e=f(new Ki(`tabindex`),{optional:!0});this._options=this._options||rb,this.color=this._options.color||rb.color,this.tabIndex=e==null?0:parseInt(e)||0,this.id=this._uniqueId=f(Be).getId(`mat-mdc-checkbox-`),this.disabledInteractive=this._options?.disabledInteractive??!1}ngOnChanges(e){e.required&&this._validatorChangeFn()}ngAfterViewInit(){this._syncIndeterminate(this.indeterminate)}get checked(){return this._checked}set checked(e){e!=this.checked&&(this._checked=e,this._changeDetectorRef.markForCheck())}_checked=!1;get disabled(){return this._disabled}set disabled(e){e!==this.disabled&&(this._disabled=e,this._changeDetectorRef.markForCheck())}_disabled=!1;get indeterminate(){return this._indeterminate()}set indeterminate(e){let i=e!=this._indeterminate();this._indeterminate.set(e),i&&(e?this._transitionCheckState(tn.Indeterminate):this._transitionCheckState(this.checked?tn.Checked:tn.Unchecked),this.indeterminateChange.emit(e)),this._syncIndeterminate(e)}_indeterminate=N(!1);_isRippleDisabled(){return this.disableRipple||this.disabled}_onLabelTextChange(){this._changeDetectorRef.detectChanges()}writeValue(e){this.checked=!!e}registerOnChange(e){this._controlValueAccessorChangeFn=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e}validate(e){return this.required&&e.value!==!0?{required:!0}:null}registerOnValidatorChange(e){this._validatorChangeFn=e}_transitionCheckState(e){let i=this._currentCheckState,a=this._getAnimationTargetElement();if(!(i===e||!a)&&(this._currentAnimationClass&&a.classList.remove(this._currentAnimationClass),this._currentAnimationClass=this._getAnimationClassForCheckStateTransition(i,e),this._currentCheckState=e,this._currentAnimationClass.length>0)){a.classList.add(this._currentAnimationClass);let o=this._currentAnimationClass;this._ngZone.runOutsideAngular(()=>{setTimeout(()=>{a.classList.remove(o)},1e3)})}}_emitChangeEvent(){this._controlValueAccessorChangeFn(this.checked),this.change.emit(this._createChangeEvent(this.checked)),this._inputElement&&(this._inputElement.nativeElement.checked=this.checked)}toggle(){this.checked=!this.checked,this._controlValueAccessorChangeFn(this.checked)}_handleInputClick(){let e=this._options?.clickAction;!this.disabled&&e!==`noop`?(this.indeterminate&&e!==`check`&&Promise.resolve().then(()=>{this._indeterminate.set(!1),this.indeterminateChange.emit(!1)}),this._checked=!this._checked,this._transitionCheckState(this._checked?tn.Checked:tn.Unchecked),this._emitChangeEvent()):(this.disabled&&this.disabledInteractive||!this.disabled&&e===`noop`)&&(this._inputElement.nativeElement.checked=this.checked,this._inputElement.nativeElement.indeterminate=this.indeterminate)}_onInteractionEvent(e){e.stopPropagation()}_onBlur(){Promise.resolve().then(()=>{this._onTouched(),this._changeDetectorRef.markForCheck()})}_getAnimationClassForCheckStateTransition(e,i){if(this._animationsDisabled)return``;switch(e){case tn.Init:if(i===tn.Checked)return this._animationClasses.uncheckedToChecked;if(i==tn.Indeterminate)return this._checked?this._animationClasses.checkedToIndeterminate:this._animationClasses.uncheckedToIndeterminate;break;case tn.Unchecked:return i===tn.Checked?this._animationClasses.uncheckedToChecked:this._animationClasses.uncheckedToIndeterminate;case tn.Checked:return i===tn.Unchecked?this._animationClasses.checkedToUnchecked:this._animationClasses.checkedToIndeterminate;case tn.Indeterminate:return i===tn.Checked?this._animationClasses.indeterminateToChecked:this._animationClasses.indeterminateToUnchecked}return``}_syncIndeterminate(e){let i=this._inputElement;i&&(i.nativeElement.indeterminate=e)}_onInputClick(){this._handleInputClick()}_preventBubblingFromLabel(e){e.target&&this._inputElement&&e.target!==this._inputElement.nativeElement&&e.stopPropagation()}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`input`];return B({type:t,selectors:[[`mat-checkbox`]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;Z(s=J())&&(r._inputElement=s.first)}},hostAttrs:[1,`mat-mdc-checkbox`],hostVars:16,hostBindings:function(o,r){o&2&&(qt(`id`,r.id),te(`tabindex`,null)(`aria-label`,null)(`aria-labelledby`,null),tt(r.color?`mat-`+r.color:`mat-accent`),q(`_mat-animation-noopable`,r._animationsDisabled)(`mdc-checkbox--disabled`,r.disabled)(`mat-mdc-checkbox-disabled`,r.disabled)(`mat-mdc-checkbox-checked`,r.checked)(`mat-mdc-checkbox-disabled-interactive`,r.disabledInteractive))},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],ariaDescribedby:[0,`aria-describedby`,`ariaDescribedby`],ariaExpanded:[2,`aria-expanded`,`ariaExpanded`,V],ariaControls:[0,`aria-controls`,`ariaControls`],ariaOwns:[0,`aria-owns`,`ariaOwns`],id:`id`,required:[2,`required`,`required`,V],labelPosition:`labelPosition`,name:`name`,value:`value`,disableRipple:[2,`disableRipple`,`disableRipple`,V],tabIndex:[2,`tabIndex`,`tabIndex`,a=>a==null?void 0:ct(a)],color:`color`,disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],checked:[2,`checked`,`checked`,V],disabled:[2,`disabled`,`disabled`,V],indeterminate:[2,`indeterminate`,`indeterminate`,V]},outputs:{change:`change`,indeterminateChange:`indeterminateChange`},exportAs:[`matCheckbox`],features:[Ge([{provide:Gr,useExisting:Ht(()=>t),multi:!0},{provide:Wr,useExisting:t,multi:!0}]),bt],ngContentSelectors:[`*`],decls:15,vars:23,consts:[[`checkbox`,``],[`input`,``],[`label`,``],[`mat-internal-form-field`,``,3,`click`,`labelPosition`,`for`],[1,`mdc-checkbox`],[`aria-hidden`,`true`,1,`mat-mdc-checkbox-touch-target`],[`type`,`checkbox`,1,`mdc-checkbox__native-control`,3,`blur`,`click`,`change`,`checked`,`indeterminate`,`disabled`,`id`,`required`,`tabIndex`],[`aria-hidden`,`true`,1,`mdc-checkbox__ripple`],[`aria-hidden`,`true`,1,`mdc-checkbox__background`],[`focusable`,`false`,`viewBox`,`0 0 24 24`,1,`mdc-checkbox__checkmark`],[`fill`,`none`,`d`,`M1.73,12.91 8.1,19.28 22.79,4.59`,1,`mdc-checkbox__checkmark-path`],[1,`mdc-checkbox__mixedmark`],[`mat-ripple`,``,`aria-hidden`,`true`,1,`mat-mdc-checkbox-ripple`,`mat-focus-indicator`,3,`matRippleTrigger`,`matRippleDisabled`,`matRippleCentered`],[1,`mat-internal-form-field-label`,`mdc-label`]],template:function(o,r){if(o&1&&(Re(),l(0,`label`,3),C(`click`,function(c){return r._preventBubblingFromLabel(c)}),l(1,`span`,4,0),z(3,`span`,5),l(4,`input`,6,1),C(`blur`,function(){return r._onBlur()})(`click`,function(){return r._onInputClick()})(`change`,function(c){return r._onInteractionEvent(c)}),d(),z(6,`span`,7),l(7,`span`,8),Gt(),l(8,`svg`,9),z(9,`path`,10),d(),ya(),z(10,`span`,11),d(),z(11,`span`,12),d(),l(12,`span`,13,2),me(14),d()()),o&2){let s=ht(2);S(`labelPosition`,r.labelPosition)(`for`,r.inputId),h(4),q(`mdc-checkbox--selected`,r.checked),S(`checked`,r.checked)(`indeterminate`,r.indeterminate)(`disabled`,r.disabled&&!r.disabledInteractive)(`id`,r.inputId)(`required`,r.required)(`tabIndex`,r.disabled&&!r.disabledInteractive?-1:r.tabIndex),te(`aria-label`,r.ariaLabel||null)(`aria-labelledby`,r.ariaLabelledby)(`aria-describedby`,r.ariaDescribedby)(`aria-checked`,r.indeterminate?`mixed`:null)(`aria-controls`,r.ariaControls)(`aria-disabled`,r.disabled&&r.disabledInteractive?!0:null)(`aria-expanded`,r.ariaExpanded)(`aria-owns`,r.ariaOwns)(`name`,r.name)(`value`,r.value),h(7),S(`matRippleTrigger`,s)(`matRippleDisabled`,r.disableRipple||r.disabled)(`matRippleCentered`,!0)}},dependencies:[li,ZS],styles:[`.mdc-checkbox {
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
`],encapsulation:2})})()}return t})();var tm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[jc,we]})}return t})();var ui=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[Du,Ni,we]})}return t})();var IO=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],hostAttrs:[`cdk-text-field-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`textarea.cdk-textarea-autosize {
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
`],encapsulation:2})}return t})();var MO={passive:!0};var ew=(()=>{class t{_platform=f(Ae);_ngZone=f(Q);_renderer=f(Mt).createRenderer(null,null);_styleLoader=f(Je);_monitoredElements=new Map;monitor(e){if(!this._platform.isBrowser)return Ri;this._styleLoader.load(IO);let i=ln(e),a=this._monitoredElements.get(i);if(a)return a.subject;let o=new L,r=`cdk-text-field-autofilled`,s=p=>{p.animationName===`cdk-text-field-autofill-start`&&!i.classList.contains(r)?(i.classList.add(r),this._ngZone.run(()=>o.next({target:p.target,isAutofilled:!0}))):p.animationName===`cdk-text-field-autofill-end`&&i.classList.contains(r)&&(i.classList.remove(r),this._ngZone.run(()=>o.next({target:p.target,isAutofilled:!1})))},c=this._ngZone.runOutsideAngular(()=>(i.classList.add(`cdk-text-field-autofill-monitored`),this._renderer.listen(i,`animationstart`,s,MO)));return this._monitoredElements.set(i,{subject:o,unlisten:c}),o}stopMonitoring(e){let i=ln(e),a=this._monitoredElements.get(i);a&&(a.unlisten(),a.subject.complete(),i.classList.remove(`cdk-text-field-autofill-monitored`),i.classList.remove(`cdk-text-field-autofilled`),this._monitoredElements.delete(i))}ngOnDestroy(){this._monitoredElements.forEach((e,i)=>this.stopMonitoring(i))}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var tw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({})}return t})();var nw=new I(`MAT_INPUT_VALUE_ACCESSOR`);var NO=[`button`,`checkbox`,`file`,`hidden`,`image`,`radio`,`range`,`reset`,`submit`];var TO=new I(`MAT_INPUT_CONFIG`);var Kr=(()=>{class t{_elementRef=f(W);_platform=f(Ae);ngControl=f(Mi,{optional:!0,self:!0});_autofillMonitor=f(ew);_ngZone=f(Q);_formField=f(Vc,{optional:!0});_renderer=f(Oe);_uid=f(Be).getId(`mat-input-`);_previousNativeValue;_inputValueAccessor;_signalBasedValueAccessor;_previousPlaceholder=null;_errorStateTracker;_config=f(TO,{optional:!0});_cleanupIosKeyup;_cleanupWebkitWheel;_isServer=!1;_isNativeSelect=!1;_isTextarea=!1;_isInFormField=!1;focused=!1;stateChanges=new L;controlType=`mat-input`;autofilled=!1;get disabled(){return this._disabled}set disabled(e){this._disabled=ta(e),this.focused&&(this.focused=!1,this.stateChanges.next())}_disabled=!1;get id(){return this._id}set id(e){this._id=e||this._uid}_id;placeholder;name;get required(){return this._required??this.ngControl?.control?.hasValidator(Ro.required)??!1}set required(e){this._required=ta(e)}_required;get type(){return this._type}set type(e){this._type=e||`text`,this._validateType(),!this._isTextarea&&jg().has(this._type)&&(this._elementRef.nativeElement.type=this._type)}_type=`text`;get errorStateMatcher(){return this._errorStateTracker.matcher}set errorStateMatcher(e){this._errorStateTracker.matcher=e}userAriaDescribedBy;get value(){return this._signalBasedValueAccessor?this._signalBasedValueAccessor.value():this._inputValueAccessor.value}set value(e){e!==this.value&&(this._signalBasedValueAccessor?this._signalBasedValueAccessor.value.set(e):this._inputValueAccessor.value=e,this.stateChanges.next())}get readonly(){return this._readonly}set readonly(e){this._readonly=ta(e)}_readonly=!1;disabledInteractive;get errorState(){return this._errorStateTracker.errorState}set errorState(e){this._errorStateTracker.errorState=e}_neverEmptyInputTypes=[`date`,`datetime`,`datetime-local`,`month`,`time`,`week`].filter(e=>jg().has(e));constructor(){let e=f(Rc,{optional:!0}),i=f(Pc,{optional:!0}),a=f(qr),o=f(nw,{optional:!0,self:!0}),r=f(Qu,{optional:!0,self:!0}),s=this._elementRef.nativeElement,c=s.nodeName.toLowerCase();o?sn(o.value)?this._signalBasedValueAccessor=o:this._inputValueAccessor=o:this._inputValueAccessor=s,this._previousNativeValue=this.value,this.id=this.id,this._platform.IOS&&this._ngZone.runOutsideAngular(()=>{this._cleanupIosKeyup=this._renderer.listen(s,`keyup`,this._iOSKeyupListener)}),this._errorStateTracker=new Yr(a,r||this.ngControl,i,e,this.stateChanges),this._isServer=!this._platform.isBrowser,this._isNativeSelect=c===`select`,this._isTextarea=c===`textarea`,this._isInFormField=!!this._formField,this.disabledInteractive=this._config?.disabledInteractive||!1,this._isNativeSelect&&(this.controlType=s.multiple?`mat-native-select-multiple`:`mat-native-select`),this._signalBasedValueAccessor&&Ft(()=>{this._signalBasedValueAccessor.value(),this.stateChanges.next()})}ngAfterViewInit(){this._platform.isBrowser&&this._autofillMonitor.monitor(this._elementRef.nativeElement).subscribe(e=>{this.autofilled=e.isAutofilled,this.stateChanges.next()})}ngOnChanges(){this.stateChanges.next()}ngOnDestroy(){this.stateChanges.complete(),this._platform.isBrowser&&this._autofillMonitor.stopMonitoring(this._elementRef.nativeElement),this._cleanupIosKeyup?.(),this._cleanupWebkitWheel?.()}ngDoCheck(){this.ngControl&&(this.updateErrorState(),this.ngControl.disabled!==null&&this.ngControl.disabled!==this.disabled&&(this.disabled=this.ngControl.disabled,this.stateChanges.next())),this._dirtyCheckNativeValue(),this._dirtyCheckPlaceholder()}focus(e){this._elementRef.nativeElement.focus(e)}updateErrorState(){this._errorStateTracker.updateErrorState()}_focusChanged(e){if(e!==this.focused){if(!this._isNativeSelect&&e&&this.disabled&&this.disabledInteractive){let i=this._elementRef.nativeElement;i.type===`number`?(i.type=`text`,i.setSelectionRange(0,0),i.type=`number`):i.setSelectionRange(0,0)}this.focused=e,this.stateChanges.next()}}_onInput(){}_dirtyCheckNativeValue(){let e=this._elementRef.nativeElement.value;this._previousNativeValue!==e&&(this._previousNativeValue=e,this.stateChanges.next())}_dirtyCheckPlaceholder(){let e=this._getPlaceholder();if(e!==this._previousPlaceholder){let i=this._elementRef.nativeElement;this._previousPlaceholder=e,e?i.setAttribute(`placeholder`,e):i.removeAttribute(`placeholder`)}}_getPlaceholder(){return this.placeholder||null}_validateType(){NO.indexOf(this._type)}_isNeverEmpty(){return this._neverEmptyInputTypes.indexOf(this._type)>-1}_isBadInput(){let e=this._elementRef.nativeElement.validity;return e&&e.badInput}get empty(){return!this._isNeverEmpty()&&!this._elementRef.nativeElement.value&&!this._isBadInput()&&!this.autofilled}get shouldLabelFloat(){if(this._isNativeSelect){let e=this._elementRef.nativeElement,i=e.options[0];return this.focused||e.multiple||!this.empty||!!(e.selectedIndex>-1&&i&&i.label)}else return this.focused&&!this.disabled||!this.empty}get describedByIds(){return this._elementRef.nativeElement.getAttribute(`aria-describedby`)?.split(` `)||[]}setDescribedByIds(e){let i=this._elementRef.nativeElement;e.length?i.setAttribute(`aria-describedby`,e.join(` `)):i.removeAttribute(`aria-describedby`)}onContainerClick(){this.focused||this.focus()}_isInlineSelect(){let e=this._elementRef.nativeElement;return this._isNativeSelect&&(e.multiple||e.size>1)}_iOSKeyupListener=e=>{let i=e.target;!i.value&&i.selectionStart===0&&i.selectionEnd===0&&(i.setSelectionRange(1,1),i.setSelectionRange(0,0))};_getReadonlyAttribute(){return this._isNativeSelect?null:this.readonly||this.disabled&&this.disabledInteractive?`true`:null}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[`input`,`matInput`,``],[`textarea`,`matInput`,``],[`select`,`matNativeControl`,``],[`input`,`matNativeControl`,``],[`textarea`,`matNativeControl`,``]],hostAttrs:[1,`mat-mdc-input-element`],hostVars:21,hostBindings:function(i,a){i&1&&C(`focus`,function(){return a._focusChanged(!0)})(`blur`,function(){return a._focusChanged(!1)})(`input`,function(){return a._onInput()}),i&2&&(qt(`id`,a.id)(`disabled`,a.disabled&&!a.disabledInteractive)(`required`,a.required),te(`name`,a.name||null)(`readonly`,a._getReadonlyAttribute())(`aria-disabled`,a.disabled&&a.disabledInteractive?`true`:null)(`aria-invalid`,a.empty&&a.required?null:a.errorState)(`aria-required`,a.required)(`id`,a.id),q(`mat-input-server`,a._isServer)(`mat-mdc-form-field-textarea-control`,a._isInFormField&&a._isTextarea)(`mat-mdc-form-field-input-control`,a._isInFormField)(`mat-mdc-input-disabled-interactive`,a.disabledInteractive)(`mdc-text-field__input`,a._isInFormField)(`mat-mdc-native-select-inline`,a._isInlineSelect()))},inputs:{disabled:`disabled`,id:`id`,placeholder:`placeholder`,name:`name`,required:`required`,type:`type`,errorStateMatcher:`errorStateMatcher`,userAriaDescribedBy:[0,`aria-describedby`,`userAriaDescribedBy`],value:`value`,readonly:`readonly`,disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V]},exportAs:[`matInput`],features:[Ge([{provide:Lc,useExisting:t}]),bt]})}return t})();var Qr=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[ui,ui,tw,we]})}return t})();var zc=class{_attachedHost=null;attach(n){return this._attachedHost=n,n.attach(this)}detach(){let n=this._attachedHost;n!=null&&(this._attachedHost=null,n.detach())}get isAttached(){return this._attachedHost!=null}setAttachedHost(n){this._attachedHost=n}};var Ta=class extends zc{component;viewContainerRef;injector;projectableNodes;bindings;directives;constructor(n,e,i,a,o,r){super(),this.component=n,this.viewContainerRef=e,this.injector=i,this.projectableNodes=a,this.bindings=o||null,this.directives=r||null}};var Hn=class extends zc{templateRef;viewContainerRef;context;injector;constructor(n,e,i,a){super(),this.templateRef=n,this.viewContainerRef=e,this.context=i,this.injector=a}get origin(){return this.templateRef.elementRef}attach(n,e=this.context){return this.context=e,super.attach(n)}detach(){return this.context=void 0,super.detach()}};var db=class extends zc{element;constructor(n){super(),this.element=n instanceof W?n.nativeElement:n}};var Xr=class{_attachedPortal=null;_disposeFn=null;_isDisposed=!1;hasAttached(){return!!this._attachedPortal}attach(n){if(n instanceof Ta)return this._attachedPortal=n,this.attachComponentPortal(n);if(n instanceof Hn)return this._attachedPortal=n,this.attachTemplatePortal(n);if(this.attachDomPortal&&n instanceof db)return this._attachedPortal=n,this.attachDomPortal(n)}attachDomPortal=null;detach(){this._attachedPortal&&(this._attachedPortal.setAttachedHost(null),this._attachedPortal=null),this._invokeDisposeFn()}dispose(){this.hasAttached()&&this.detach(),this._invokeDisposeFn(),this._isDisposed=!0}setDisposeFn(n){this._disposeFn=n}_invokeDisposeFn(){this._disposeFn&&(this._disposeFn(),this._disposeFn=null)}};var Hc=class extends Xr{outletElement;_appRef;_defaultInjector;constructor(n,e,i){super(),this.outletElement=n,this._appRef=e,this._defaultInjector=i}attachComponentPortal(n){let e;if(n.viewContainerRef){let i=n.injector||n.viewContainerRef.injector,a=i.get(ni,null,{optional:!0})||void 0;e=n.viewContainerRef.createComponent(n.component,{index:n.viewContainerRef.length,injector:i,ngModuleRef:a,projectableNodes:n.projectableNodes||void 0,bindings:n.bindings||void 0,directives:n.directives||void 0}),this.setDisposeFn(()=>e.destroy())}else{let i=this._appRef,a=n.injector||this._defaultInjector||re.NULL,o=a.get(kt,i.injector);e=tu(n.component,{elementInjector:a,environmentInjector:o,projectableNodes:n.projectableNodes||void 0,bindings:n.bindings||void 0,directives:n.directives||void 0}),i.attachView(e.hostView),this.setDisposeFn(()=>{i.viewCount>0&&i.detachView(e.hostView),e.destroy()})}return this.outletElement.appendChild(this._getComponentRootNode(e)),this._attachedPortal=n,e}attachTemplatePortal(n){let e=n.viewContainerRef,i=e.createEmbeddedView(n.templateRef,n.context,{injector:n.injector});return i.rootNodes.forEach(a=>this.outletElement.appendChild(a)),i.detectChanges(),this.setDisposeFn(()=>{let a=e.indexOf(i);a!==-1&&e.remove(a)}),this._attachedPortal=n,i}attachDomPortal=n=>{let e=n.element;e.parentNode;let i=this.outletElement.ownerDocument.createComment(`dom-portal`);e.parentNode.insertBefore(i,e),this.outletElement.appendChild(e),this._attachedPortal=n,super.setDisposeFn(()=>{i.parentNode&&i.parentNode.replaceChild(e,i)})};dispose(){super.dispose(),this.outletElement.remove()}_getComponentRootNode(n){return n.hostView.rootNodes[0]}};var iw=(()=>{class t extends Hn{constructor(){let e=f(Lt),i=f(Vt);super(e,i)}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`cdkPortal`,``]],exportAs:[`cdkPortal`],features:[Ve]})}return t})();var Aa=(()=>{class t extends Xr{_moduleRef=f(ni,{optional:!0});_document=f(de);_viewContainerRef=f(Vt);_isInitialized=!1;_attachedRef=null;get portal(){return this._attachedPortal}set portal(e){this.hasAttached()&&!e&&!this._isInitialized||(this.hasAttached()&&super.detach(),e&&super.attach(e),this._attachedPortal=e||null)}attached=new ee;get attachedRef(){return this._attachedRef}ngOnInit(){this._isInitialized=!0}ngOnDestroy(){super.dispose(),this._attachedRef=this._attachedPortal=null}attachComponentPortal(e){e.setAttachedHost(this);let i=e.viewContainerRef!=null?e.viewContainerRef:this._viewContainerRef,a=i.createComponent(e.component,{index:i.length,injector:e.injector||i.injector,projectableNodes:e.projectableNodes||void 0,ngModuleRef:this._moduleRef||void 0,bindings:e.bindings||void 0,directives:e.directives||void 0});return i!==this._viewContainerRef&&this._getRootNode().appendChild(a.hostView.rootNodes[0]),super.setDisposeFn(()=>a.destroy()),this._attachedPortal=e,this._attachedRef=a,this.attached.emit(a),a}attachTemplatePortal(e){e.setAttachedHost(this);let i=this._viewContainerRef.createEmbeddedView(e.templateRef,e.context,{injector:e.injector});return super.setDisposeFn(()=>this._viewContainerRef.clear()),this._attachedPortal=e,this._attachedRef=i,this.attached.emit(i),i}attachDomPortal=e=>{let i=e.element;i.parentNode;let a=this._document.createComment(`dom-portal`);e.setAttachedHost(this),i.parentNode.insertBefore(a,i),this._getRootNode().appendChild(i),this._attachedPortal=e,super.setDisposeFn(()=>{a.parentNode&&a.parentNode.replaceChild(i,a)})};_getRootNode(){let e=this._viewContainerRef.element.nativeElement;return e.nodeType===e.ELEMENT_NODE?e:e.parentNode}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=Y({type:t,selectors:[[``,`cdkPortalOutlet`,``]],inputs:{portal:[0,`cdkPortalOutlet`,`portal`]},outputs:{attached:`attached`},exportAs:[`cdkPortalOutlet`],features:[Ve]})}return t})();var Fo=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({})}return t})();var AO=20;var Oa=(()=>{class t{_ngZone=f(Q);_platform=f(Ae);_renderer=f(Mt).createRenderer(null,null);_cleanupGlobalListener;_scrolled=new L;_scrolledCount=0;scrollContainers=new Map;register(e){this.scrollContainers.has(e)||this.scrollContainers.set(e,e.elementScrolled().subscribe(()=>this._scrolled.next(e)))}deregister(e){let i=this.scrollContainers.get(e);i&&(i.unsubscribe(),this.scrollContainers.delete(e))}scrolled(e=AO){return this._platform.isBrowser?new Ce(i=>{this._cleanupGlobalListener||(this._cleanupGlobalListener=this._ngZone.runOutsideAngular(()=>this._renderer.listen(`document`,`scroll`,()=>this._scrolled.next())));let a=e>0?this._scrolled.pipe(Ol(e)).subscribe(i):this._scrolled.subscribe(i);return this._scrolledCount++,()=>{a.unsubscribe(),this._scrolledCount--,this._scrolledCount||(this._cleanupGlobalListener?.(),this._cleanupGlobalListener=void 0)}}):Mn()}ngOnDestroy(){this._cleanupGlobalListener?.(),this._cleanupGlobalListener=void 0,this.scrollContainers.forEach((e,i)=>this.deregister(i)),this._scrolled.complete()}ancestorScrolled(e,i){let a=this.getAncestorScrollContainers(e);return this.scrolled(i).pipe(Qe(o=>!o||a.indexOf(o)>-1))}getAncestorScrollContainers(e){let i=[];return this.scrollContainers.forEach((a,o)=>{this._targetContainsElement(o,e)&&i.push(o)}),i}_targetContainsElement(e,i){let a=ln(i),o=e.getElementRef().nativeElement;do if(a==o)return!0;while(a=a.parentElement);return!1}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Uc=(()=>{class t{elementRef=f(W);scrollDispatcher=f(Oa);ngZone=f(Q);dir=f(ft,{optional:!0});_scrollElement=this.elementRef.nativeElement;_destroyed=new L;_renderer=f(Oe);_cleanupScroll;_elementScrolled=new L;ngOnInit(){this._cleanupScroll=this.ngZone.runOutsideAngular(()=>this._renderer.listen(this._scrollElement,`scroll`,e=>this._elementScrolled.next(e))),this.scrollDispatcher.register(this)}ngOnDestroy(){this._cleanupScroll?.(),this._elementScrolled.complete(),this.scrollDispatcher.deregister(this),this._destroyed.next(),this._destroyed.complete()}elementScrolled(){return this._elementScrolled}getElementRef(){return this.elementRef}scrollTo(e){let i=this.elementRef.nativeElement,a=this.dir&&this.dir.value==`rtl`;e.left??=a?e.end:e.start,e.right??=a?e.start:e.end,e.bottom!=null&&(e.top=i.scrollHeight-i.clientHeight-e.bottom),a&&Vr()!=ci.NORMAL?(e.left!=null&&(e.right=i.scrollWidth-i.clientWidth-e.left),Vr()==ci.INVERTED?e.left=e.right:Vr()==ci.NEGATED&&(e.left=e.right?-e.right:e.right)):e.right!=null&&(e.left=i.scrollWidth-i.clientWidth-e.right),this._applyScrollToOptions(e)}_applyScrollToOptions(e){let i=this.elementRef.nativeElement;Ou()?i.scrollTo(e):(e.top!=null&&(i.scrollTop=e.top),e.left!=null&&(i.scrollLeft=e.left))}measureScrollOffset(e){let i=`left`,a=`right`,o=this.elementRef.nativeElement;if(e==`top`)return o.scrollTop;if(e==`bottom`)return o.scrollHeight-o.clientHeight-o.scrollTop;let r=this.dir&&this.dir.value==`rtl`;return e==`start`?e=r?a:i:e==`end`&&(e=r?i:a),r&&Vr()==ci.INVERTED?e==i?o.scrollWidth-o.clientWidth-o.scrollLeft:o.scrollLeft:r&&Vr()==ci.NEGATED?e==i?o.scrollLeft+o.scrollWidth-o.clientWidth:-o.scrollLeft:e==i?o.scrollLeft:o.scrollWidth-o.clientWidth-o.scrollLeft}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`cdk-scrollable`,``],[``,`cdkScrollable`,``]]})}return t})();var OO=20;var mi=(()=>{class t{_platform=f(Ae);_listeners;_viewportSize=null;_change=new L;_document=f(de);constructor(){let e=f(Q),i=f(Mt).createRenderer(null,null);e.runOutsideAngular(()=>{if(this._platform.isBrowser){let a=o=>this._change.next(o);this._listeners=[i.listen(`window`,`resize`,a),i.listen(`window`,`orientationchange`,a)]}this.change().subscribe(()=>this._viewportSize=null)})}ngOnDestroy(){this._listeners?.forEach(e=>e()),this._change.complete()}getViewportSize(){this._viewportSize||this._updateViewportSize();let e={width:this._viewportSize.width,height:this._viewportSize.height};return this._platform.isBrowser||(this._viewportSize=null),e}getViewportRect(){let e=this.getViewportScrollPosition(),{width:i,height:a}=this.getViewportSize();return{top:e.top,left:e.left,bottom:e.top+a,right:e.left+i,height:a,width:i}}getViewportScrollPosition(){if(!this._platform.isBrowser)return{top:0,left:0};let e=this._document,i=this._getWindow(),a=e.documentElement,o=a.getBoundingClientRect();return{top:-o.top||e.body?.scrollTop||i.scrollY||a.scrollTop||0,left:-o.left||e.body?.scrollLeft||i.scrollX||a.scrollLeft||0}}change(e=OO){return e>0?this._change.pipe(Ol(e)):this._change}_getWindow(){return this._document.defaultView||window}_updateViewportSize(){let e=this._getWindow();this._viewportSize=this._platform.isBrowser?{width:e.innerWidth,height:e.innerHeight}:{width:0,height:0}}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var ia=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({})}return t})();var ub=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[we,ia,we,ia]})}return t})();var aw=Ou();function es(t){return new nm(t.get(mi),t.get(de))}var nm=class{_viewportRuler;_previousHTMLStyles={top:``,left:``};_previousScrollPosition;_isEnabled=!1;_document;constructor(n,e){this._viewportRuler=n,this._document=e}attach(){}enable(){if(this._canBeEnabled()){let n=this._document.documentElement;this._previousScrollPosition=this._viewportRuler.getViewportScrollPosition(),this._previousHTMLStyles.left=n.style.left||``,this._previousHTMLStyles.top=n.style.top||``,n.style.left=Et(-this._previousScrollPosition.left),n.style.top=Et(-this._previousScrollPosition.top),n.classList.add(`cdk-global-scrollblock`),this._isEnabled=!0}}disable(){if(this._isEnabled){let n=this._document.documentElement,e=this._document.body,i=n.style,a=e.style,o=i.scrollBehavior||``,r=a.scrollBehavior||``;this._isEnabled=!1,i.left=this._previousHTMLStyles.left,i.top=this._previousHTMLStyles.top,n.classList.remove(`cdk-global-scrollblock`),aw&&(i.scrollBehavior=a.scrollBehavior=`auto`),window.scroll(this._previousScrollPosition.left,this._previousScrollPosition.top),aw&&(i.scrollBehavior=o,a.scrollBehavior=r)}}_canBeEnabled(){if(this._document.documentElement.classList.contains(`cdk-global-scrollblock`)||this._isEnabled)return!1;let e=this._document.documentElement,i=this._viewportRuler.getViewportSize();return e.scrollHeight>i.height||e.scrollWidth>i.width}};function uw(t,n){return new im(t.get(Oa),t.get(Q),t.get(mi),n)}var im=class{_scrollDispatcher;_ngZone;_viewportRuler;_config;_scrollSubscription=null;_overlayRef;_initialScrollPosition;constructor(n,e,i,a){this._scrollDispatcher=n,this._ngZone=e,this._viewportRuler=i,this._config=a}attach(n){this._overlayRef,this._overlayRef=n}enable(){if(this._scrollSubscription)return;let n=this._scrollDispatcher.scrolled(0).pipe(Qe(e=>!e||!this._overlayRef.overlayElement.contains(e.getElementRef().nativeElement)));this._config&&this._config.threshold&&this._config.threshold>1?(this._initialScrollPosition=this._viewportRuler.getViewportScrollPosition().top,this._scrollSubscription=n.subscribe(()=>{let e=this._viewportRuler.getViewportScrollPosition().top;Math.abs(e-this._initialScrollPosition)>this._config.threshold?this._detach():this._overlayRef.updatePosition()})):this._scrollSubscription=n.subscribe(this._detach)}disable(){this._scrollSubscription&&(this._scrollSubscription.unsubscribe(),this._scrollSubscription=null)}detach(){this.disable(),this._overlayRef=null}_detach=()=>{this.disable(),this._overlayRef.hasAttached()&&this._ngZone.run(()=>this._overlayRef.detach())}};var $c=class{enable(){}disable(){}attach(){}};function pb(t,n){return n.some(e=>{let i=t.bottom<e.top,a=t.top>e.bottom,o=t.right<e.left,r=t.left>e.right;return i||a||o||r})}function ow(t,n){return n.some(e=>{let i=t.top<e.top,a=t.bottom>e.bottom,o=t.left<e.left,r=t.right>e.right;return i||a||o||r})}function ra(t,n){return new am(t.get(Oa),t.get(mi),t.get(Q),n)}var am=class{_scrollDispatcher;_viewportRuler;_ngZone;_config;_scrollSubscription=null;_overlayRef;constructor(n,e,i,a){this._scrollDispatcher=n,this._viewportRuler=e,this._ngZone=i,this._config=a}attach(n){this._overlayRef,this._overlayRef=n}enable(){if(!this._scrollSubscription){let n=this._config?this._config.scrollThrottle:0;this._scrollSubscription=this._scrollDispatcher.scrolled(n).subscribe(()=>{if(this._overlayRef.updatePosition(),this._config&&this._config.autoClose){let e=this._overlayRef.overlayElement.getBoundingClientRect(),{width:i,height:a}=this._viewportRuler.getViewportSize();pb(e,[{width:i,height:a,bottom:a,right:i,top:0,left:0}])&&(this.disable(),this._ngZone.run(()=>this._overlayRef.detach()))}})}}disable(){this._scrollSubscription&&(this._scrollSubscription.unsubscribe(),this._scrollSubscription=null)}detach(){this.disable(),this._overlayRef=null}};var mw=(()=>{class t{_injector=f(re);noop=()=>new $c;close=e=>uw(this._injector,e);block=()=>es(this._injector);reposition=e=>ra(this._injector,e);static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var aa=class{positionStrategy;scrollStrategy=new $c;panelClass=``;hasBackdrop=!1;backdropClass=`cdk-overlay-dark-backdrop`;disableAnimations;width;height;minWidth;minHeight;maxWidth;maxHeight;direction;disposeOnNavigation=!1;usePopover;eventPredicate;constructor(n){if(n){let e=Object.keys(n);for(let i of e)n[i]!==void 0&&(this[i]=n[i])}}};var om=class{connectionPair;scrollableViewProperties;constructor(n,e){this.connectionPair=n,this.scrollableViewProperties=e}};var pw=(()=>{class t{_attachedOverlays=[];_document=f(de);_isAttached=!1;ngOnDestroy(){this.detach()}add(e){this.remove(e),this._attachedOverlays.push(e)}remove(e){let i=this._attachedOverlays.indexOf(e);i>-1&&this._attachedOverlays.splice(i,1),this._attachedOverlays.length===0&&this.detach()}canReceiveEvent(e,i,a){return a.observers.length<1?!1:e.eventPredicate?e.eventPredicate(i):!0}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var hw=(()=>{class t extends pw{_ngZone=f(Q);_renderer=f(Mt).createRenderer(null,null);_cleanupKeydown;add(e){super.add(e),this._isAttached||(this._ngZone.runOutsideAngular(()=>{this._cleanupKeydown=this._renderer.listen(`body`,`keydown`,this._keydownListener)}),this._isAttached=!0)}detach(){this._isAttached&&(this._cleanupKeydown?.(),this._isAttached=!1)}_keydownListener=e=>{let i=this._attachedOverlays;for(let a=i.length-1;a>-1;a--){let o=i[a];if(this.canReceiveEvent(o,e,o._keydownEvents)){this._ngZone.run(()=>o._keydownEvents.next(e));break}}};static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var fw=(()=>{class t extends pw{_platform=f(Ae);_ngZone=f(Q);_renderer=f(Mt).createRenderer(null,null);_cursorOriginalValue;_cursorStyleIsSet=!1;_pointerDownEventTarget=null;_cleanups;add(e){if(super.add(e),!this._isAttached){let i=this._document.body,a={capture:!0},o=this._renderer;this._cleanups=this._ngZone.runOutsideAngular(()=>[o.listen(i,`pointerdown`,this._pointerDownListener,a),o.listen(i,`click`,this._clickListener,a),o.listen(i,`auxclick`,this._clickListener,a),o.listen(i,`contextmenu`,this._clickListener,a)]),this._platform.IOS&&!this._cursorStyleIsSet&&(this._cursorOriginalValue=i.style.cursor,i.style.cursor=`pointer`,this._cursorStyleIsSet=!0),this._isAttached=!0}}detach(){this._isAttached&&(this._cleanups?.forEach(e=>e()),this._cleanups=void 0,this._platform.IOS&&this._cursorStyleIsSet&&(this._document.body.style.cursor=this._cursorOriginalValue,this._cursorStyleIsSet=!1),this._isAttached=!1)}_pointerDownListener=e=>{this._pointerDownEventTarget=Yt(e)};_clickListener=e=>{let i=Yt(e),a=e.type===`click`&&this._pointerDownEventTarget?this._pointerDownEventTarget:i;this._pointerDownEventTarget=null;let o=this._attachedOverlays.slice();for(let r=o.length-1;r>-1;r--){let s=o[r],c=s._outsidePointerEvents;if(!(!s.hasAttached()||!this.canReceiveEvent(s,e,c))){if(rw(s.overlayElement,i)||rw(s.overlayElement,a))break;this._ngZone?this._ngZone.run(()=>c.next(e)):c.next(e)}}};static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function rw(t,n){let e=typeof ShadowRoot<`u`&&ShadowRoot,i=n;for(;i;){if(i===t)return!0;i=e&&i instanceof ShadowRoot?i.host:i.parentNode}return!1}var gw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],hostAttrs:[`cdk-overlay-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`.cdk-overlay-container, .cdk-global-overlay-wrapper {
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
`],encapsulation:2})}return t})();var cm=(()=>{class t{_platform=f(Ae);_containerElement;_document=f(de);_styleLoader=f(Je);ngOnDestroy(){this._containerElement?.remove()}getContainerElement(){return this._loadStyles(),this._containerElement||this._createContainer(),this._containerElement}_createContainer(){let e=`cdk-overlay-container`;if(this._platform.isBrowser||Bg()){let a=this._document.querySelectorAll(`.${e}[platform="server"], .${e}[platform="test"]`);for(let o=0;o<a.length;o++)a[o].remove()}let i=this._document.createElement(`div`);i.classList.add(e),Bg()?i.setAttribute(`platform`,`test`):this._platform.isBrowser||i.setAttribute(`platform`,`server`),this._document.body.appendChild(i),this._containerElement=i}_loadStyles(){this._styleLoader.load(gw)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var hb=class{_renderer;_ngZone;element;_cleanupClick;_cleanupTransitionEnd;_fallbackTimeout;constructor(n,e,i,a){this._renderer=e,this._ngZone=i,this.element=n.createElement(`div`),this.element.classList.add(`cdk-overlay-backdrop`),this._cleanupClick=e.listen(this.element,`click`,a)}detach(){this._ngZone.runOutsideAngular(()=>{let n=this.element;clearTimeout(this._fallbackTimeout),this._cleanupTransitionEnd?.(),this._cleanupTransitionEnd=this._renderer.listen(n,`transitionend`,this.dispose),this._fallbackTimeout=setTimeout(this.dispose,500),n.style.pointerEvents=`none`,n.classList.remove(`cdk-overlay-backdrop-showing`)})}dispose=()=>{clearTimeout(this._fallbackTimeout),this._cleanupClick?.(),this._cleanupTransitionEnd?.(),this._cleanupClick=this._cleanupTransitionEnd=this._fallbackTimeout=void 0,this.element.remove()}};function fb(t){return t&&t.nodeType===1}var mb=N([]);var Zr=class{_portalOutlet;_host;_pane;_config;_ngZone;_keyboardDispatcher;_document;_location;_outsideClickDispatcher;_animationsDisabled;_injector;_renderer;_backdropClick=new L;_attachments=new L;_detachments=new L;_positionStrategy;_scrollStrategy;_locationChanges;_backdropRef=null;_detachContentMutationObserver;_detachContentAfterRenderRef;_disposed=!1;_previousHostParent;_keydownEvents=new L;_outsidePointerEvents=new L;_afterNextRenderRef;constructor(n,e,i,a,o,r,s,c,p,m=!1,g,v){this._portalOutlet=n,this._host=e,this._pane=i,this._config=a,this._ngZone=o,this._keyboardDispatcher=r,this._document=s,this._location=c,this._outsideClickDispatcher=p,this._animationsDisabled=m,this._injector=g,this._renderer=v,a.scrollStrategy&&(this._scrollStrategy=a.scrollStrategy,this._scrollStrategy.attach(this)),this._positionStrategy=a.positionStrategy}get overlayElement(){return this._pane}get backdropElement(){return this._backdropRef?.element||null}get hostElement(){return this._host}get eventPredicate(){return this._config?.eventPredicate||null}attach(n){if(this._disposed)return null;this._attachHost();let e=this._portalOutlet.attach(n);if(this._positionStrategy?.attach(this),this._updateStackingOrder(),this._updateElementSize(),this._updateElementDirection(),nt(()=>{mb.update(i=>i.includes(this)?i:[...i,this])}),this._scrollStrategy&&this._scrollStrategy.enable(),this._afterNextRenderRef?.destroy(),this._afterNextRenderRef=Tt(()=>{this.hasAttached()&&this.updatePosition()},{injector:this._injector}),this._togglePointerEvents(!0),this._config.hasBackdrop&&this._attachBackdrop(),this._config.panelClass&&this._toggleClasses(this._pane,this._config.panelClass,!0),this._attachments.next(),this._completeDetachContent(),this._keyboardDispatcher.add(this),this._config.disposeOnNavigation===!0||this._config.disposeOnNavigation===`pop-state`){let i=this._location.subscribe(()=>this.dispose());this._locationChanges=()=>i.unsubscribe()}else this._config.disposeOnNavigation===`url-change`&&(this._locationChanges=this._location.onUrlChange(()=>this.dispose()));return this._outsideClickDispatcher.add(this),typeof e?.onDestroy==`function`&&e.onDestroy(()=>{this.hasAttached()&&this._ngZone.runOutsideAngular(()=>Promise.resolve().then(()=>this.detach()))}),e}detach(){if(!this.hasAttached())return;this.detachBackdrop(),this._togglePointerEvents(!1),this._positionStrategy&&this._positionStrategy.detach&&this._positionStrategy.detach(),this._scrollStrategy&&this._scrollStrategy.disable();let n=this._portalOutlet.detach();return this._detachments.next(),this._completeDetachContent(),this._keyboardDispatcher.remove(this),this._detachContentWhenEmpty(),this._locationChanges?.(),this._outsideClickDispatcher.remove(this),nt(()=>{mb.update(e=>e.filter(i=>i!==this))}),n}dispose(){if(this._disposed)return;let n=this.hasAttached();this._positionStrategy&&this._positionStrategy.dispose(),this._disposeScrollStrategy(),this._backdropRef?.dispose(),this._locationChanges?.(),this._keyboardDispatcher.remove(this),this._portalOutlet.dispose(),this._attachments.complete(),this._backdropClick.complete(),this._keydownEvents.complete(),this._outsidePointerEvents.complete(),this._outsideClickDispatcher.remove(this),this._host?.remove(),this._afterNextRenderRef?.destroy(),this._previousHostParent=this._pane=this._host=this._backdropRef=null,n&&this._detachments.next(),this._detachments.complete(),this._completeDetachContent(),this._disposed=!0,nt(()=>{mb.update(e=>e.filter(i=>i!==this))})}hasAttached(){return this._portalOutlet.hasAttached()}backdropClick(){return this._backdropClick}attachments(){return this._attachments}detachments(){return this._detachments}keydownEvents(){return this._keydownEvents}outsidePointerEvents(){return this._outsidePointerEvents}getConfig(){return this._config}updatePosition(){this._positionStrategy&&this._positionStrategy.apply()}updatePositionStrategy(n){n!==this._positionStrategy&&(this._positionStrategy&&this._positionStrategy.dispose(),this._positionStrategy=n,this.hasAttached()&&(n.attach(this),this.updatePosition()))}updateSize(n){this._config=r(r({},this._config),n),this._updateElementSize()}setDirection(n){this._config=s(r({},this._config),{direction:n}),this._updateElementDirection()}addPanelClass(n){this._pane&&this._toggleClasses(this._pane,n,!0)}removePanelClass(n){this._pane&&this._toggleClasses(this._pane,n,!1)}getDirection(){let n=this._config.direction;return n?typeof n==`string`?n:n.value:`ltr`}updateScrollStrategy(n){n!==this._scrollStrategy&&(this._disposeScrollStrategy(),this._scrollStrategy=n,this.hasAttached()&&(n.attach(this),n.enable()))}_updateElementDirection(){this._host.setAttribute(`dir`,this.getDirection())}_updateElementSize(){if(!this._pane)return;let n=this._pane.style;n.width=Et(this._config.width),n.height=Et(this._config.height),n.minWidth=Et(this._config.minWidth),n.minHeight=Et(this._config.minHeight),n.maxWidth=Et(this._config.maxWidth),n.maxHeight=Et(this._config.maxHeight)}_togglePointerEvents(n){this._pane.style.pointerEvents=n?``:`none`}_attachHost(){if(!this._host.parentElement){let n=this._config.usePopover?this._positionStrategy?.getPopoverInsertionPoint?.():null;fb(n)?n.after(this._host):n?.type===`parent`?n.element.appendChild(this._host):this._previousHostParent?.appendChild(this._host)}if(this._config.usePopover)try{this._host.showPopover()}catch{}}_attachBackdrop(){let n=`cdk-overlay-backdrop-showing`;this._backdropRef?.dispose(),this._backdropRef=new hb(this._document,this._renderer,this._ngZone,e=>{this._backdropClick.next(e)}),this._animationsDisabled&&this._backdropRef.element.classList.add(`cdk-overlay-backdrop-noop-animation`),this._config.backdropClass&&this._toggleClasses(this._backdropRef.element,this._config.backdropClass,!0),this._config.usePopover?this._host.prepend(this._backdropRef.element):this._host.parentElement.insertBefore(this._backdropRef.element,this._host),!this._animationsDisabled&&typeof requestAnimationFrame<`u`?this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>this._backdropRef?.element.classList.add(n))}):this._backdropRef.element.classList.add(n)}_updateStackingOrder(){!this._config.usePopover&&this._host.nextSibling&&this._host.parentNode.appendChild(this._host)}detachBackdrop(){this._animationsDisabled?(this._backdropRef?.dispose(),this._backdropRef=null):this._backdropRef?.detach()}_toggleClasses(n,e,i){let a=Rr(e||[]).filter(o=>!!o);a.length&&(i?n.classList.add(...a):n.classList.remove(...a))}_detachContentWhenEmpty(){let n=!1;try{this._detachContentAfterRenderRef=Tt(()=>{n=!0,this._detachContent()},{injector:this._injector})}catch(e){if(n)throw e;this._detachContent()}globalThis.MutationObserver&&this._pane&&(this._detachContentMutationObserver||=new globalThis.MutationObserver(()=>{this._detachContent()}),this._detachContentMutationObserver.observe(this._pane,{childList:!0}))}_detachContent(){(!this._pane||!this._host||this._pane.children.length===0)&&(this._pane&&this._config.panelClass&&this._toggleClasses(this._pane,this._config.panelClass,!1),this._host&&this._host.parentElement&&(this._previousHostParent=this._host.parentElement,this._host.remove()),this._completeDetachContent())}_completeDetachContent(){this._detachContentAfterRenderRef?.destroy(),this._detachContentAfterRenderRef=void 0,this._detachContentMutationObserver?.disconnect()}_disposeScrollStrategy(){let n=this._scrollStrategy;n?.disable(),n?.detach?.()}};var sw=`cdk-overlay-connected-position-bounding-box`;var PO=/([A-Za-z%]+)$/;function Vo(t,n){return new rm(n,t.get(mi),t.get(de),t.get(Ae),t.get(cm))}var rm=class{_viewportRuler;_document;_platform;_overlayContainer;_overlayRef;_isInitialRender=!1;_lastBoundingBoxSize={width:0,height:0};_isPushed=!1;_canPush=!0;_growAfterOpen=!1;_hasFlexibleDimensions=!0;_positionLocked=!1;_originRect;_overlayRect;_viewportRect;_containerRect;_viewportMargin=0;_scrollables=[];_preferredPositions=[];_origin;_pane;_isDisposed=!1;_boundingBox=null;_lastPosition=null;_lastScrollVisibility=null;_positionChanges=new L;_resizeSubscription=De.EMPTY;_offsetX=0;_offsetY=0;_transformOriginSelector;_appliedPanelClasses=[];_previousPushAmount=null;_popoverLocation=`global`;positionChanges=this._positionChanges;get positions(){return this._preferredPositions}constructor(n,e,i,a,o){this._viewportRuler=e,this._document=i,this._platform=a,this._overlayContainer=o,this.setOrigin(n)}attach(n){this._overlayRef&&this._overlayRef,this._validatePositions(),n.hostElement.classList.add(sw),this._overlayRef=n,this._boundingBox=n.hostElement,this._pane=n.overlayElement,this._isDisposed=!1,this._isInitialRender=!0,this._lastPosition=null,this._resizeSubscription.unsubscribe(),this._resizeSubscription=this._viewportRuler.change().subscribe(()=>{this._isInitialRender=!0,this.apply()})}apply(){if(this._isDisposed||!this._platform.isBrowser)return;if(!this._isInitialRender&&this._positionLocked&&this._lastPosition){this.reapplyLastPosition();return}this._clearPanelClasses(),this._resetOverlayElementStyles(),this._resetBoundingBoxStyles(),this._viewportRect=this._getNarrowedViewportRect(),this._originRect=this._getOriginRect(),this._overlayRect=this._pane.getBoundingClientRect(),this._containerRect=this._getContainerRect();let n=this._originRect,e=this._overlayRect,i=this._viewportRect,a=this._containerRect,o=[],r;for(let s of this._preferredPositions){let c=this._getOriginPoint(n,a,s),p=this._getOverlayPoint(c,e,s),m=this._getOverlayFit(p,e,i,s);if(m.isCompletelyWithinViewport){this._isPushed=!1,this._applyPosition(s,c);return}if(this._canFitWithFlexibleDimensions(m,p,i)){o.push({position:s,origin:c,overlayRect:e,boundingBoxRect:this._calculateBoundingBoxRect(c,s)});continue}(!r||r.overlayFit.visibleArea<m.visibleArea)&&(r={overlayFit:m,overlayPoint:p,originPoint:c,position:s,overlayRect:e})}if(o.length){let s=null,c=-1;for(let p of o){let m=p.boundingBoxRect.width*p.boundingBoxRect.height*(p.position.weight||1);m>c&&(c=m,s=p)}this._isPushed=!1,this._applyPosition(s.position,s.origin);return}if(this._canPush){this._isPushed=!0,this._applyPosition(r.position,r.originPoint);return}this._applyPosition(r.position,r.originPoint)}detach(){this._clearPanelClasses(),this._lastPosition=null,this._previousPushAmount=null,this._resizeSubscription.unsubscribe()}dispose(){this._isDisposed||(this._boundingBox&&Lo(this._boundingBox.style,{top:``,left:``,right:``,bottom:``,height:``,width:``,alignItems:``,justifyContent:``}),this._pane&&this._resetOverlayElementStyles(),this._overlayRef&&this._overlayRef.hostElement.classList.remove(sw),this.detach(),this._positionChanges.complete(),this._overlayRef=this._boundingBox=null,this._isDisposed=!0)}reapplyLastPosition(){if(this._isDisposed||!this._platform.isBrowser)return;let n=this._lastPosition;n?(this._originRect=this._getOriginRect(),this._overlayRect=this._pane.getBoundingClientRect(),this._viewportRect=this._getNarrowedViewportRect(),this._containerRect=this._getContainerRect(),this._applyPosition(n,this._getOriginPoint(this._originRect,this._containerRect,n))):this.apply()}withScrollableContainers(n){return this._scrollables=n,this}withPositions(n){return this._preferredPositions=n,n.indexOf(this._lastPosition)===-1&&(this._lastPosition=null),this._validatePositions(),this}withViewportMargin(n){return this._viewportMargin=n,this}withFlexibleDimensions(n=!0){return this._hasFlexibleDimensions=n,this}withGrowAfterOpen(n=!0){return this._growAfterOpen=n,this}withPush(n=!0){return this._canPush=n,this}withLockedPosition(n=!0){return this._positionLocked=n,this}setOrigin(n){return this._origin=n,this}withDefaultOffsetX(n){return this._offsetX=n,this}withDefaultOffsetY(n){return this._offsetY=n,this}withTransformOriginOn(n){return this._transformOriginSelector=n,this}withPopoverLocation(n){return this._popoverLocation=n,this}getPopoverInsertionPoint(){return this._popoverLocation===`global`?null:this._popoverLocation!==`inline`?this._popoverLocation:this._origin instanceof W?this._origin.nativeElement:fb(this._origin)?this._origin:null}_getOriginPoint(n,e,i){let a;if(i.originX==`center`)a=n.left+n.width/2;else{let r=this._isRtl()?n.right:n.left,s=this._isRtl()?n.left:n.right;a=i.originX==`start`?r:s}e.left<0&&(a-=e.left);let o;return i.originY==`center`?o=n.top+n.height/2:o=i.originY==`top`?n.top:n.bottom,e.top<0&&(o-=e.top),{x:a,y:o}}_getOverlayPoint(n,e,i){let a;i.overlayX==`center`?a=-e.width/2:i.overlayX===`start`?a=this._isRtl()?-e.width:0:a=this._isRtl()?0:-e.width;let o;return i.overlayY==`center`?o=-e.height/2:o=i.overlayY==`top`?0:-e.height,{x:n.x+a,y:n.y+o}}_getOverlayFit(n,e,i,a){let o=lw(e),{x:r,y:s}=n,c=this._getOffset(a,`x`),p=this._getOffset(a,`y`);c&&(r+=c),p&&(s+=p);let m=0-r,g=r+o.width-i.width,v=0-s,b=s+o.height-i.height,_=this._subtractOverflows(o.width,m,g),E=this._subtractOverflows(o.height,v,b),P=_*E;return{visibleArea:P,isCompletelyWithinViewport:o.width*o.height===P,fitsInViewportVertically:E===o.height,fitsInViewportHorizontally:_==o.width}}_canFitWithFlexibleDimensions(n,e,i){if(this._hasFlexibleDimensions){let a=i.bottom-e.y,o=i.right-e.x,r=cw(this._overlayRef.getConfig().minHeight),s=cw(this._overlayRef.getConfig().minWidth),c=n.fitsInViewportVertically||r!=null&&r<=a,p=n.fitsInViewportHorizontally||s!=null&&s<=o;return c&&p}return!1}_pushOverlayOnScreen(n,e,i){if(this._previousPushAmount&&this._positionLocked)return{x:n.x+this._previousPushAmount.x,y:n.y+this._previousPushAmount.y};let a=lw(e),o=this._viewportRect,r=Math.max(n.x+a.width-o.width,0),s=Math.max(n.y+a.height-o.height,0),c=Math.max(o.top-i.top-n.y,0),p=Math.max(o.left-i.left-n.x,0),m=0,g=0;return a.width<=o.width?m=p||-r:m=n.x<this._getViewportMarginStart()?o.left-i.left-n.x:0,a.height<=o.height?g=c||-s:g=n.y<this._getViewportMarginTop()?o.top-i.top-n.y:0,this._previousPushAmount={x:m,y:g},{x:n.x+m,y:n.y+g}}_applyPosition(n,e){if(this._setTransformOrigin(n),this._setOverlayElementStyles(e,n),this._setBoundingBoxStyles(e,n),n.panelClass&&this._addPanelClasses(n.panelClass),this._positionChanges.observers.length){let i=this._getScrollVisibility();if(n!==this._lastPosition||!this._lastScrollVisibility||!FO(this._lastScrollVisibility,i)){let a=new om(n,i);this._positionChanges.next(a)}this._lastScrollVisibility=i}this._lastPosition=n,this._isInitialRender=!1}_setTransformOrigin(n){if(!this._transformOriginSelector)return;let e=this._boundingBox.querySelectorAll(this._transformOriginSelector),i,a=n.overlayY;n.overlayX===`center`?i=`center`:this._isRtl()?i=n.overlayX===`start`?`right`:`left`:i=n.overlayX===`start`?`left`:`right`;for(let o=0;o<e.length;o++)e[o].style.transformOrigin=`${i} ${a}`}_calculateBoundingBoxRect(n,e){let i=this._viewportRect,a=this._isRtl(),o,r,s;if(e.overlayY===`top`)r=n.y,o=i.height-r+this._getViewportMarginBottom();else if(e.overlayY===`bottom`)s=i.height-n.y+this._getViewportMarginTop()+this._getViewportMarginBottom(),o=i.height-s+this._getViewportMarginTop();else{let b=Math.min(i.bottom-n.y+i.top,n.y),_=this._lastBoundingBoxSize.height;o=b*2,r=n.y-b,o>_&&!this._isInitialRender&&!this._growAfterOpen&&(r=n.y-_/2)}let c=e.overlayX===`start`&&!a||e.overlayX===`end`&&a,p=e.overlayX===`end`&&!a||e.overlayX===`start`&&a,m,g,v;if(p)v=i.width-n.x+this._getViewportMarginStart()+this._getViewportMarginEnd(),m=n.x-this._getViewportMarginStart();else if(c)g=n.x,m=i.right-n.x-this._getViewportMarginEnd();else{let b=Math.min(i.right-n.x+i.left,n.x),_=this._lastBoundingBoxSize.width;m=b*2,g=n.x-b,m>_&&!this._isInitialRender&&!this._growAfterOpen&&(g=n.x-_/2)}return{top:r,left:g,bottom:s,right:v,width:m,height:o}}_setBoundingBoxStyles(n,e){let i=this._calculateBoundingBoxRect(n,e);!this._isInitialRender&&!this._growAfterOpen&&(i.height=Math.min(i.height,this._lastBoundingBoxSize.height),i.width=Math.min(i.width,this._lastBoundingBoxSize.width));let a={};if(this._hasExactPosition())a.top=a.left=`0`,a.bottom=a.right=`auto`,a.maxHeight=a.maxWidth=``,a.width=a.height=`100%`;else{let o=this._overlayRef.getConfig().maxHeight,r=this._overlayRef.getConfig().maxWidth;a.width=Et(i.width),a.height=Et(i.height),a.top=Et(i.top)||`auto`,a.bottom=Et(i.bottom)||`auto`,a.left=Et(i.left)||`auto`,a.right=Et(i.right)||`auto`,e.overlayX===`center`?a.alignItems=`center`:a.alignItems=e.overlayX===`end`?`flex-end`:`flex-start`,e.overlayY===`center`?a.justifyContent=`center`:a.justifyContent=e.overlayY===`bottom`?`flex-end`:`flex-start`,o&&(a.maxHeight=Et(o)),r&&(a.maxWidth=Et(r))}this._lastBoundingBoxSize=i,Lo(this._boundingBox.style,a)}_resetBoundingBoxStyles(){Lo(this._boundingBox.style,{top:`0`,left:`0`,right:`0`,bottom:`0`,height:``,width:``,alignItems:``,justifyContent:``})}_resetOverlayElementStyles(){Lo(this._pane.style,{top:``,left:``,bottom:``,right:``,position:``,transform:``})}_setOverlayElementStyles(n,e){let i={},a=this._hasExactPosition(),o=this._hasFlexibleDimensions,r=this._overlayRef.getConfig();if(a){let m=this._viewportRuler.getViewportScrollPosition();Lo(i,this._getExactOverlayY(e,n,m)),Lo(i,this._getExactOverlayX(e,n,m))}else i.position=`static`;let s=``,c=this._getOffset(e,`x`),p=this._getOffset(e,`y`);c&&(s+=`translateX(${c}px) `),p&&(s+=`translateY(${p}px)`),i.transform=s.trim(),r.maxHeight&&(a?i.maxHeight=Et(r.maxHeight):o&&(i.maxHeight=``)),r.maxWidth&&(a?i.maxWidth=Et(r.maxWidth):o&&(i.maxWidth=``)),Lo(this._pane.style,i)}_getExactOverlayY(n,e,i){let a={top:``,bottom:``},o=this._getOverlayPoint(e,this._overlayRect,n);if(this._isPushed&&(o=this._pushOverlayOnScreen(o,this._overlayRect,i)),n.overlayY===`bottom`)a.bottom=`${this._document.documentElement.clientHeight-(o.y+this._overlayRect.height)}px`;else a.top=Et(o.y);return a}_getExactOverlayX(n,e,i){let a={left:``,right:``},o=this._getOverlayPoint(e,this._overlayRect,n);this._isPushed&&(o=this._pushOverlayOnScreen(o,this._overlayRect,i));let r;if(this._isRtl()?r=n.overlayX===`end`?`left`:`right`:r=n.overlayX===`end`?`right`:`left`,r===`right`)a.right=`${this._document.documentElement.clientWidth-(o.x+this._overlayRect.width)}px`;else a.left=Et(o.x);return a}_getScrollVisibility(){let n=this._getOriginRect(),e=this._pane.getBoundingClientRect(),i=this._scrollables.map(a=>a.getElementRef().nativeElement.getBoundingClientRect());return{isOriginClipped:ow(n,i),isOriginOutsideView:pb(n,i),isOverlayClipped:ow(e,i),isOverlayOutsideView:pb(e,i)}}_subtractOverflows(n,...e){return e.reduce((i,a)=>i-Math.max(a,0),n)}_getNarrowedViewportRect(){let n=this._document.documentElement.clientWidth,e=this._document.documentElement.clientHeight,i=this._viewportRuler.getViewportScrollPosition();return{top:i.top+this._getViewportMarginTop(),left:i.left+this._getViewportMarginStart(),right:i.left+n-this._getViewportMarginEnd(),bottom:i.top+e-this._getViewportMarginBottom(),width:n-this._getViewportMarginStart()-this._getViewportMarginEnd(),height:e-this._getViewportMarginTop()-this._getViewportMarginBottom()}}_isRtl(){return this._overlayRef.getDirection()===`rtl`}_hasExactPosition(){return!this._hasFlexibleDimensions||this._isPushed}_getOffset(n,e){return e===`x`?n.offsetX==null?this._offsetX:n.offsetX:n.offsetY==null?this._offsetY:n.offsetY}_validatePositions(){}_addPanelClasses(n){this._pane&&Rr(n).forEach(e=>{e!==``&&this._appliedPanelClasses.indexOf(e)===-1&&(this._appliedPanelClasses.push(e),this._pane.classList.add(e))})}_clearPanelClasses(){this._pane&&(this._appliedPanelClasses.forEach(n=>{this._pane.classList.remove(n)}),this._appliedPanelClasses=[])}_getViewportMarginStart(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.start??0}_getViewportMarginEnd(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.end??0}_getViewportMarginTop(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.top??0}_getViewportMarginBottom(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.bottom??0}_getOriginRect(){let n=this._origin;if(n instanceof W)return n.nativeElement.getBoundingClientRect();if(n instanceof Element)return n.getBoundingClientRect();let e=n.width||0,i=n.height||0;return{top:n.y,bottom:n.y+i,left:n.x,right:n.x+e,height:i,width:e}}_getContainerRect(){let n=this._overlayRef.getConfig().usePopover&&this._popoverLocation!==`global`,e=this._overlayContainer.getContainerElement();n&&(e.style.display=`block`);let i=e.getBoundingClientRect();return n&&(e.style.display=``),i}};function Lo(t,n){for(let e in n)Object.hasOwn(n,e)&&(t[e]=n[e]);return t}function cw(t){if(typeof t!=`number`&&t!=null){let[n,e]=t.split(PO);return!e||e===`px`?parseFloat(n):null}return t||null}function lw(t){return{top:Math.floor(t.top),right:Math.floor(t.right),bottom:Math.floor(t.bottom),left:Math.floor(t.left),width:Math.floor(t.width),height:Math.floor(t.height)}}function FO(t,n){return t===n?!0:t.isOriginClipped===n.isOriginClipped&&t.isOriginOutsideView===n.isOriginOutsideView&&t.isOverlayClipped===n.isOverlayClipped&&t.isOverlayOutsideView===n.isOverlayOutsideView}var dw=`cdk-global-overlay-wrapper`;function ts(t){return new sm}var sm=class{_overlayRef;_cssPosition=`static`;_topOffset=``;_bottomOffset=``;_alignItems=``;_xPosition=``;_xOffset=``;_width=``;_height=``;_isDisposed=!1;attach(n){let e=n.getConfig();this._overlayRef=n,this._width&&!e.width&&n.updateSize({width:this._width}),this._height&&!e.height&&n.updateSize({height:this._height}),n.hostElement.classList.add(dw),this._isDisposed=!1}top(n=``){return this._bottomOffset=``,this._topOffset=n,this._alignItems=`flex-start`,this}left(n=``){return this._xOffset=n,this._xPosition=`left`,this}bottom(n=``){return this._topOffset=``,this._bottomOffset=n,this._alignItems=`flex-end`,this}right(n=``){return this._xOffset=n,this._xPosition=`right`,this}start(n=``){return this._xOffset=n,this._xPosition=`start`,this}end(n=``){return this._xOffset=n,this._xPosition=`end`,this}width(n=``){return this._overlayRef?this._overlayRef.updateSize({width:n}):this._width=n,this}height(n=``){return this._overlayRef?this._overlayRef.updateSize({height:n}):this._height=n,this}centerHorizontally(n=``){return this.left(n),this._xPosition=`center`,this}centerVertically(n=``){return this.top(n),this._alignItems=`center`,this}apply(){if(!this._overlayRef||!this._overlayRef.hasAttached())return;let n=this._overlayRef.overlayElement.style,e=this._overlayRef.hostElement.style,{width:a,height:o,maxWidth:r,maxHeight:s}=this._overlayRef.getConfig(),c=(a===`100%`||a===`100vw`)&&(!r||r===`100%`||r===`100vw`),p=(o===`100%`||o===`100vh`)&&(!s||s===`100%`||s===`100vh`),m=this._xPosition,g=this._xOffset,v=this._overlayRef.getConfig().direction===`rtl`,b=``,_=``,E=``;c?E=`flex-start`:m===`center`?(E=`center`,v?_=g:b=g):v?m===`left`||m===`end`?(E=`flex-end`,b=g):(m===`right`||m===`start`)&&(E=`flex-start`,_=g):m===`left`||m===`start`?(E=`flex-start`,b=g):(m===`right`||m===`end`)&&(E=`flex-end`,_=g),n.position=this._cssPosition,n.marginLeft=c?`0`:b,n.marginTop=p?`0`:this._topOffset,n.marginBottom=this._bottomOffset,n.marginRight=c?`0`:_,e.justifyContent=E,e.alignItems=p?`flex-start`:this._alignItems}dispose(){if(this._isDisposed||!this._overlayRef)return;let n=this._overlayRef.overlayElement.style,e=this._overlayRef.hostElement,i=e.style;e.classList.remove(dw),i.justifyContent=i.alignItems=n.marginTop=n.marginBottom=n.marginLeft=n.marginRight=n.position=``,this._overlayRef=null,this._isDisposed=!0}};var bw=(()=>{class t{_injector=f(re);global(){return ts()}flexibleConnectedTo(e){return Vo(this._injector,e)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Gc=new I(`OVERLAY_DEFAULT_CONFIG`);function sa(t,n){t.get(Je).load(gw);let e=t.get(cm),i=t.get(de),a=t.get(Be),o=t.get(Rn),r=t.get(ft),s=t.get(Oe,null,{optional:!0})||t.get(Mt).createRenderer(null,null),c=new aa(n),p=t.get(Gc,null,{optional:!0})?.usePopover??!0;c.direction=c.direction||r.value,!i.body||!(`showPopover`in i.body)?c.usePopover=!1:c.usePopover=n?.usePopover??p;let m=i.createElement(`div`),g=i.createElement(`div`);m.id=a.getId(`cdk-overlay-`),m.classList.add(`cdk-overlay-pane`),g.appendChild(m),c.usePopover&&(g.setAttribute(`popover`,`manual`),g.classList.add(`cdk-overlay-popover`));let v=c.usePopover?c.positionStrategy?.getPopoverInsertionPoint?.():null;return fb(v)?v.after(g):v?.type===`parent`?v.element.appendChild(g):e.getContainerElement().appendChild(g),new Zr(new Hc(m,o,t),g,m,c,t.get(Q),t.get(hw),i,t.get(iu),t.get(fw),n?.disableAnimations??t.get(Ls,null,{optional:!0})===`NoopAnimations`,t.get(kt),s)}var vw=(()=>{class t{scrollStrategies=f(mw);_positionBuilder=f(bw);_injector=f(re);create(e){return sa(this._injector,e)}position(){return this._positionBuilder}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var LO=[{originX:`start`,originY:`bottom`,overlayX:`start`,overlayY:`top`},{originX:`start`,originY:`top`,overlayX:`start`,overlayY:`bottom`},{originX:`end`,originY:`top`,overlayX:`end`,overlayY:`bottom`},{originX:`end`,originY:`bottom`,overlayX:`end`,overlayY:`top`}];var VO=new I(`cdk-connected-overlay-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>ra(t)}});var Jr=(()=>{class t{elementRef=f(W);static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`cdk-overlay-origin`,``],[``,`overlay-origin`,``],[``,`cdkOverlayOrigin`,``]],exportAs:[`cdkOverlayOrigin`]})}return t})();var yw=new I(`cdk-connected-overlay-default-config`);var lm=(()=>{class t{_dir=f(ft,{optional:!0});_injector=f(re);_overlayRef;_templatePortal;_backdropSubscription=De.EMPTY;_attachSubscription=De.EMPTY;_detachSubscription=De.EMPTY;_positionSubscription=De.EMPTY;_offsetX;_offsetY;_position;_scrollStrategyFactory=f(VO);_ngZone=f(Q);origin;positions;positionStrategy;get offsetX(){return this._offsetX}set offsetX(e){this._offsetX=e,this._position&&this._updatePositionStrategy(this._position)}get offsetY(){return this._offsetY}set offsetY(e){this._offsetY=e,this._position&&this._updatePositionStrategy(this._position)}width;height;minWidth;minHeight;backdropClass;panelClass;viewportMargin=0;scrollStrategy;open=!1;disableClose=!1;transformOriginSelector;hasBackdrop=!1;lockPosition=!1;flexibleDimensions=!1;growAfterOpen=!1;push=!1;disposeOnNavigation=!1;usePopover;matchWidth=!1;set _config(e){typeof e!=`string`&&this._assignConfig(e)}backdropClick=new ee;positionChange=new ee;attach=new ee;detach=new ee;overlayKeydown=new ee;overlayOutsideClick=new ee;constructor(){let e=f(Lt),i=f(Vt),a=f(yw,{optional:!0}),o=f(Gc,{optional:!0});this.usePopover=o?.usePopover===!1?null:`global`,this._templatePortal=new Hn(e,i),this.scrollStrategy=this._scrollStrategyFactory(),a&&this._assignConfig(a)}get overlayRef(){return this._overlayRef}get dir(){return this._dir?this._dir.value:`ltr`}ngOnDestroy(){this._attachSubscription.unsubscribe(),this._detachSubscription.unsubscribe(),this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this._overlayRef?.dispose()}ngOnChanges(e){this._position&&(this._updatePositionStrategy(this._position),this._overlayRef?.updateSize({width:this._getWidth(),minWidth:this.minWidth,height:this.height,minHeight:this.minHeight}),e.origin&&this.open&&this._position.apply()),e.open&&(this.open?this.attachOverlay():this.detachOverlay())}_createOverlay(){(!this.positions||!this.positions.length)&&(this.positions=LO);let e=this._overlayRef=sa(this._injector,this._buildConfig());this._attachSubscription=e.attachments().subscribe(()=>this.attach.emit()),this._detachSubscription=e.detachments().subscribe(()=>this.detach.emit()),e.keydownEvents().subscribe(i=>{this.overlayKeydown.next(i),i.keyCode===27&&!this.disableClose&&!xt(i)&&(i.preventDefault(),this.detachOverlay())}),this._overlayRef.outsidePointerEvents().subscribe(i=>{let a=this._getOriginElement(),o=Yt(i);(!a||a!==o&&!a.contains(o))&&this.overlayOutsideClick.next(i)})}_buildConfig(){let e=this._position=this.positionStrategy||this._createPositionStrategy(),i=new aa({direction:this._dir||`ltr`,positionStrategy:e,scrollStrategy:this.scrollStrategy,hasBackdrop:this.hasBackdrop,disposeOnNavigation:this.disposeOnNavigation,usePopover:!!this.usePopover});return(this.height||this.height===0)&&(i.height=this.height),(this.minWidth||this.minWidth===0)&&(i.minWidth=this.minWidth),(this.minHeight||this.minHeight===0)&&(i.minHeight=this.minHeight),this.backdropClass&&(i.backdropClass=this.backdropClass),this.panelClass&&(i.panelClass=this.panelClass),i}_updatePositionStrategy(e){let i=this.positions.map(a=>({originX:a.originX,originY:a.originY,overlayX:a.overlayX,overlayY:a.overlayY,offsetX:a.offsetX||this.offsetX,offsetY:a.offsetY||this.offsetY,panelClass:a.panelClass||void 0}));return e.setOrigin(this._getOrigin()).withPositions(i).withFlexibleDimensions(this.flexibleDimensions).withPush(this.push).withGrowAfterOpen(this.growAfterOpen).withViewportMargin(this.viewportMargin).withLockedPosition(this.lockPosition).withTransformOriginOn(this.transformOriginSelector).withPopoverLocation(this.usePopover===null?`global`:this.usePopover)}_createPositionStrategy(){let e=Vo(this._injector,this._getOrigin());return this._updatePositionStrategy(e),e}_getOrigin(){return this.origin instanceof Jr?this.origin.elementRef:this.origin}_getOriginElement(){return this.origin instanceof Jr?this.origin.elementRef.nativeElement:this.origin instanceof W?this.origin.nativeElement:typeof Element<`u`&&this.origin instanceof Element?this.origin:null}_getWidth(){return this.width?this.width:this.matchWidth?this._getOriginElement()?.getBoundingClientRect?.().width:void 0}attachOverlay(){this._overlayRef||this._createOverlay();let e=this._overlayRef;e.getConfig().hasBackdrop=this.hasBackdrop,e.updateSize({width:this._getWidth()}),e.hasAttached()||e.attach(this._templatePortal),this.hasBackdrop?this._backdropSubscription=e.backdropClick().subscribe(i=>this.backdropClick.emit(i)):this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this.positionChange.observers.length>0&&(this._positionSubscription=this._position.positionChanges.pipe(lp(()=>this.positionChange.observers.length>0)).subscribe(i=>{this._ngZone.run(()=>this.positionChange.emit(i)),this.positionChange.observers.length===0&&this._positionSubscription.unsubscribe()})),this.open=!0}detachOverlay(){this._overlayRef?.detach(),this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this.open=!1}_assignConfig(e){this.origin=e.origin??this.origin,this.positions=e.positions??this.positions,this.positionStrategy=e.positionStrategy??this.positionStrategy,this.offsetX=e.offsetX??this.offsetX,this.offsetY=e.offsetY??this.offsetY,this.width=e.width??this.width,this.height=e.height??this.height,this.minWidth=e.minWidth??this.minWidth,this.minHeight=e.minHeight??this.minHeight,this.backdropClass=e.backdropClass??this.backdropClass,this.panelClass=e.panelClass??this.panelClass,this.viewportMargin=e.viewportMargin??this.viewportMargin,this.scrollStrategy=e.scrollStrategy??this.scrollStrategy,this.disableClose=e.disableClose??this.disableClose,this.transformOriginSelector=e.transformOriginSelector??this.transformOriginSelector,this.hasBackdrop=e.hasBackdrop??this.hasBackdrop,this.lockPosition=e.lockPosition??this.lockPosition,this.flexibleDimensions=e.flexibleDimensions??this.flexibleDimensions,this.growAfterOpen=e.growAfterOpen??this.growAfterOpen,this.push=e.push??this.push,this.disposeOnNavigation=e.disposeOnNavigation??this.disposeOnNavigation,this.usePopover=e.usePopover??this.usePopover,this.matchWidth=e.matchWidth??this.matchWidth}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`cdk-connected-overlay`,``],[``,`connected-overlay`,``],[``,`cdkConnectedOverlay`,``]],inputs:{origin:[0,`cdkConnectedOverlayOrigin`,`origin`],positions:[0,`cdkConnectedOverlayPositions`,`positions`],positionStrategy:[0,`cdkConnectedOverlayPositionStrategy`,`positionStrategy`],offsetX:[0,`cdkConnectedOverlayOffsetX`,`offsetX`],offsetY:[0,`cdkConnectedOverlayOffsetY`,`offsetY`],width:[0,`cdkConnectedOverlayWidth`,`width`],height:[0,`cdkConnectedOverlayHeight`,`height`],minWidth:[0,`cdkConnectedOverlayMinWidth`,`minWidth`],minHeight:[0,`cdkConnectedOverlayMinHeight`,`minHeight`],backdropClass:[0,`cdkConnectedOverlayBackdropClass`,`backdropClass`],panelClass:[0,`cdkConnectedOverlayPanelClass`,`panelClass`],viewportMargin:[0,`cdkConnectedOverlayViewportMargin`,`viewportMargin`],scrollStrategy:[0,`cdkConnectedOverlayScrollStrategy`,`scrollStrategy`],open:[0,`cdkConnectedOverlayOpen`,`open`],disableClose:[0,`cdkConnectedOverlayDisableClose`,`disableClose`],transformOriginSelector:[0,`cdkConnectedOverlayTransformOriginOn`,`transformOriginSelector`],hasBackdrop:[2,`cdkConnectedOverlayHasBackdrop`,`hasBackdrop`,V],lockPosition:[2,`cdkConnectedOverlayLockPosition`,`lockPosition`,V],flexibleDimensions:[2,`cdkConnectedOverlayFlexibleDimensions`,`flexibleDimensions`,V],growAfterOpen:[2,`cdkConnectedOverlayGrowAfterOpen`,`growAfterOpen`,V],push:[2,`cdkConnectedOverlayPush`,`push`,V],disposeOnNavigation:[2,`cdkConnectedOverlayDisposeOnNavigation`,`disposeOnNavigation`,e=>e===`url-change`||e===`pop-state`?e:V(e)],usePopover:[0,`cdkConnectedOverlayUsePopover`,`usePopover`],matchWidth:[2,`cdkConnectedOverlayMatchWidth`,`matchWidth`,V],_config:[0,`cdkConnectedOverlay`,`_config`]},outputs:{backdropClick:`backdropClick`,positionChange:`positionChange`,attach:`attach`,detach:`detach`,overlayKeydown:`overlayKeydown`,overlayOutsideClick:`overlayOutsideClick`},exportAs:[`cdkConnectedOverlay`],features:[bt]})}return t})();var pi=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({providers:[vw],imports:[we,Fo,ub,ub]})}return t})();var bb=new I(`MAT_MENU_PANEL`);var ca=(()=>{class t{_isAnchor;_elementRef=f(W);_document=f(de);_focusMonitor=f(Jt);_parentMenu=f(bb,{optional:!0});_changeDetectorRef=f(Pe);role=`menuitem`;disabled=!1;disabledInteractive=!1;disableRipple=!1;_hovered=new L;_focused=new L;_highlighted=!1;_triggersSubmenu=!1;constructor(){f(Je).load(dn),this._parentMenu?.addItem?.(this),this._isAnchor=this._elementRef.nativeElement.tagName===`A`}focus(e,i){this._focusMonitor&&e?this._focusMonitor.focusVia(this._getHostElement(),e,i):this._getHostElement().focus(i),this._focused.next(this)}ngAfterViewInit(){this._focusMonitor&&this._focusMonitor.monitor(this._elementRef,!1)}ngOnDestroy(){this._focusMonitor&&this._focusMonitor.stopMonitoring(this._elementRef),this._parentMenu&&this._parentMenu.removeItem&&this._parentMenu.removeItem(this),this._hovered.complete(),this._focused.complete()}_getTabIndex(){return this.disabled&&!this.disabledInteractive?`-1`:`0`}_getAriaDisabled(){return this._isAnchor?this.disabled||null:this.disabled&&this.disabledInteractive?!0:null}_getDisabled(){return this.disabledInteractive||!this.disabled?null:!0}_getHostElement(){return this._elementRef.nativeElement}_checkDisabled(e){this.disabled&&(e.preventDefault(),e.stopPropagation())}_handleMouseEnter(){this._hovered.next(this)}getLabel(){let e=this._elementRef.nativeElement.cloneNode(!0),i=e.querySelectorAll(`mat-icon, .material-icons, .material-symbols-outlined, .material-symbols-rounded, .material-symbols-sharp`);for(let a=0;a<i.length;a++)i[a].remove();return e.textContent?.trim()||``}_setHighlighted(e){this._highlighted=e,this._changeDetectorRef.markForCheck()}_setTriggersSubmenu(e){this._triggersSubmenu=e,this._changeDetectorRef.markForCheck()}_hasFocus(){return this._document&&this._document.activeElement===this._getHostElement()}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[`mat-icon`],[``,`matMenuItemIcon`,``]],`*`],i=[`mat-icon, [matMenuItemIcon]`,`*`];function a(o,r){o&1&&(Gt(),l(0,`svg`,2),z(1,`polygon`,3),d())}return B({type:t,selectors:[[``,`mat-menu-item`,``]],hostAttrs:[1,`mat-mdc-menu-item`,`mat-focus-indicator`],hostVars:12,hostBindings:function(r,s){r&1&&C(`click`,function(p){return s._checkDisabled(p)})(`mouseenter`,function(){return s._handleMouseEnter()}),r&2&&(te(`role`,s.role)(`tabindex`,s._getTabIndex())(`aria-disabled`,s._getAriaDisabled())(`disabled`,s._getDisabled()),q(`mat-mdc-menu-item-highlighted`,s._highlighted)(`mat-mdc-menu-item-submenu-trigger`,s._triggersSubmenu)(`mat-mdc-menu-item-disabled`,s.disabled)(`mat-mdc-menu-item-disabled-interactive`,s.disabledInteractive))},inputs:{role:`role`,disabled:[2,`disabled`,`disabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V]},exportAs:[`matMenuItem`],ngContentSelectors:i,decls:5,vars:3,consts:[[1,`mat-mdc-menu-item-text`],[`matRipple`,``,1,`mat-mdc-menu-ripple`,3,`matRippleDisabled`,`matRippleTrigger`],[`viewBox`,`0 0 5 10`,`focusable`,`false`,`aria-hidden`,`true`,1,`mat-mdc-menu-submenu-icon`],[`points`,`0,0 5,5 0,10`]],template:function(r,s){r&1&&(Re(e),me(0),l(1,`span`,0),me(2,1),d(),z(3,`div`,1),D(4,a,2,0,`:svg:svg`,2)),r&2&&(h(3),S(`matRippleDisabled`,s.disableRipple||s.disabled)(`matRippleTrigger`,s._getHostElement()),h(),k(s._triggersSubmenu?4:-1))},dependencies:[li],encapsulation:2})})()}return t})();var BO=new I(`MatMenuContent`);var jO=new I(`mat-menu-default-options`,{providedIn:`root`,factory:()=>({overlapTrigger:!1,xPosition:`after`,yPosition:`below`,backdropClass:`cdk-overlay-transparent-backdrop`})});var gb=`_mat-menu-enter`;var dm=`_mat-menu-exit`;var Ti=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Pe);_injector=f(re);_keyManager;_xPosition;_yPosition;_firstItemFocusRef;_exitFallbackTimeout;_animationsDisabled=Ue();_allItems;_directDescendantItems=new Sn;_classList={};_panelAnimationState=`void`;_animationDone=new L;_isAnimating=N(!1);parentMenu;direction;overlayPanelClass;backdropClass;ariaLabel;ariaLabelledby;ariaDescribedby;get xPosition(){return this._xPosition}set xPosition(e){this._xPosition=e,this.setPositionClasses()}get yPosition(){return this._yPosition}set yPosition(e){this._yPosition=e,this.setPositionClasses()}templateRef;items;lazyContent;overlapTrigger=!1;hasBackdrop;get panelClass(){return this._previousPanelClass}set panelClass(e){let i=this._previousPanelClass,a=r({},this._classList);i&&i.length&&i.split(` `).forEach(o=>{a[o]=!1}),this._previousPanelClass=e,e&&e.length&&(e.split(` `).forEach(o=>{a[o]=!0}),this._elementRef.nativeElement.className=``),this._classList=a}_previousPanelClass=``;get classList(){return this.panelClass}set classList(e){this.panelClass=e}closed=new ee;close=this.closed;panelId=f(Be).getId(`mat-menu-panel-`);constructor(){let e=f(jO);this.overlayPanelClass=e.overlayPanelClass||``,this._xPosition=e.xPosition,this._yPosition=e.yPosition,this.backdropClass=e.backdropClass,this.overlapTrigger=e.overlapTrigger,this.hasBackdrop=e.hasBackdrop}ngOnInit(){this.setPositionClasses()}ngAfterContentInit(){this._updateDirectDescendants(),this._keyManager=new ea(this._directDescendantItems).withWrap().withTypeAhead().withHomeAndEnd().skipPredicate(e=>e.disabled&&!e.disabledInteractive),this._keyManager.tabOut.subscribe(()=>this.closed.emit(`tab`)),this._directDescendantItems.changes.pipe(ut(this._directDescendantItems),vn(e=>Dt(...e.map(i=>i._focused)))).subscribe(e=>this._keyManager.updateActiveItem(e)),this._directDescendantItems.changes.subscribe(e=>{let i=this._keyManager;if(this._panelAnimationState===`enter`&&i.activeItem?._hasFocus()){let a=e.toArray(),o=Math.max(0,Math.min(a.length-1,i.activeItemIndex||0));a[o]&&!a[o].disabled?i.setActiveItem(o):i.setNextItemActive()}})}ngOnDestroy(){this._keyManager?.destroy(),this._directDescendantItems.destroy(),this.closed.complete(),this._firstItemFocusRef?.destroy(),clearTimeout(this._exitFallbackTimeout)}_hovered(){return this._directDescendantItems.changes.pipe(ut(this._directDescendantItems),vn(i=>Dt(...i.map(a=>a._hovered))))}addItem(e){}removeItem(e){}_handleKeydown(e){let i=e.keyCode,a=this._keyManager;switch(i){case 27:xt(e)||(e.preventDefault(),this.closed.emit(`keydown`));break;case 37:this.parentMenu&&this.direction===`ltr`&&this.closed.emit(`keydown`);break;case 39:this.parentMenu&&this.direction===`rtl`&&this.closed.emit(`keydown`);break;default:(i===38||i===40)&&a.setFocusOrigin(`keyboard`),a.onKeydown(e);return}}focusFirstItem(e=`program`){this._firstItemFocusRef?.destroy(),this._firstItemFocusRef=Tt(()=>{let i=this._resolvePanel();if(!i||!i.contains(document.activeElement)){let a=this._keyManager;a.setFocusOrigin(e).setFirstItemActive(),!a.activeItem&&i&&i.focus()}},{injector:this._injector})}resetActiveItem(){this._keyManager.setActiveItem(-1)}setElevation(e){}setPositionClasses(e=this.xPosition,i=this.yPosition){this._classList=s(r({},this._classList),{"mat-menu-before":e===`before`,"mat-menu-after":e===`after`,"mat-menu-above":i===`above`,"mat-menu-below":i===`below`}),this._changeDetectorRef.markForCheck()}_onAnimationDone(e){let i=e===dm;(i||e===gb)&&(i&&(clearTimeout(this._exitFallbackTimeout),this._exitFallbackTimeout=void 0),this._animationDone.next(i?`void`:`enter`),this._isAnimating.set(!1))}_onAnimationStart(e){(e===gb||e===dm)&&this._isAnimating.set(!0)}_setIsOpen(e){if(this._panelAnimationState=e?`enter`:`void`,e){if(this._keyManager.activeItemIndex===0){let i=this._resolvePanel();i&&(i.scrollTop=0)}}else this._animationsDisabled||(this._exitFallbackTimeout=setTimeout(()=>this._onAnimationDone(dm),200));this._animationsDisabled&&setTimeout(()=>{this._onAnimationDone(e?gb:dm)}),this._changeDetectorRef.markForCheck()}_updateDirectDescendants(){this._allItems.changes.pipe(ut(this._allItems)).subscribe(e=>{this._directDescendantItems.reset(e.filter(i=>i._parentMenu===this)),this._directDescendantItems.notifyOnChanges()})}_resolvePanel(){let e=null;return this._directDescendantItems.length&&(e=this._directDescendantItems.first._getHostElement().closest(`[role="menu"]`)),e}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`];function i(a,o){if(a&1){let r=X();st(0,`div`,0),Ir(`click`,function(){A(r);return O(y().closed.emit(`click`))})(`animationstart`,function(c){A(r);return O(y()._onAnimationStart(c.animationName))})(`animationend`,function(c){A(r);return O(y()._onAnimationDone(c.animationName))})(`animationcancel`,function(c){A(r);return O(y()._onAnimationDone(c.animationName))}),st(1,`div`,1),me(2),pt()()}if(a&2){let r=y();tt(r._classList),q(`mat-menu-panel-animations-disabled`,r._animationsDisabled)(`mat-menu-panel-exit-animation`,r._panelAnimationState===`void`)(`mat-menu-panel-animating`,r._isAnimating()),qt(`id`,r.panelId),te(`aria-label`,r.ariaLabel||null)(`aria-labelledby`,r.ariaLabelledby||null)(`aria-describedby`,r.ariaDescribedby||null)}}return B({type:t,selectors:[[`mat-menu`]],contentQueries:function(o,r,s){if(o&1&&Bt(s,BO,5)(s,ca,5)(s,ca,4),o&2){let c;Z(c=J())&&(r.lazyContent=c.first),Z(c=J())&&(r._allItems=c),Z(c=J())&&(r.items=c)}},viewQuery:function(o,r){if(o&1&&Ze(Lt,5),o&2){let s;Z(s=J())&&(r.templateRef=s.first)}},hostVars:3,hostBindings:function(o,r){o&2&&te(`aria-label`,null)(`aria-labelledby`,null)(`aria-describedby`,null)},inputs:{backdropClass:`backdropClass`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],ariaDescribedby:[0,`aria-describedby`,`ariaDescribedby`],xPosition:`xPosition`,yPosition:`yPosition`,overlapTrigger:[2,`overlapTrigger`,`overlapTrigger`,V],hasBackdrop:[2,`hasBackdrop`,`hasBackdrop`,a=>a==null?null:V(a)],panelClass:[0,`class`,`panelClass`],classList:`classList`},outputs:{closed:`closed`,close:`close`},exportAs:[`matMenu`],features:[Ge([{provide:bb,useExisting:t}])],ngContentSelectors:e,decls:1,vars:0,consts:[[`tabindex`,`-1`,`role`,`menu`,1,`mat-mdc-menu-panel`,3,`click`,`animationstart`,`animationend`,`animationcancel`,`id`],[1,`mat-mdc-menu-content`]],template:function(o,r){o&1&&(Re(),Dr(0,i,3,12,`ng-template`))},styles:[`mat-menu {
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
`],encapsulation:2})})()}return t})();var zO=new I(`mat-menu-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>ra(t)}});var ns=new WeakMap;var HO=(()=>{class t{_canHaveBackdrop;_element=f(W);_viewContainerRef=f(Vt);_menuItemInstance=f(ca,{optional:!0,self:!0});_dir=f(ft,{optional:!0});_focusMonitor=f(Jt);_ngZone=f(Q);_injector=f(re);_scrollStrategy=f(zO);_changeDetectorRef=f(Pe);_animationsDisabled=Ue();_portal;_overlayRef=null;_menuOpen=!1;_closingActionsSubscription=De.EMPTY;_menuCloseSubscription=De.EMPTY;_pendingRemoval;_parentMaterialMenu;_parentInnerPadding;_openedBy=void 0;get _menu(){return this._menuInternal}set _menu(e){e!==this._menuInternal&&(this._menuInternal=e,this._menuCloseSubscription.unsubscribe(),e?(this._parentMaterialMenu,this._menuCloseSubscription=e.close.subscribe(i=>{this._destroyMenu(i),(i===`click`||i===`tab`)&&this._parentMaterialMenu&&this._parentMaterialMenu.closed.emit(i)})):this._destroyMenu(),this._menuItemInstance?._setTriggersSubmenu(this._triggersSubmenu()))}_menuInternal=null;constructor(e){this._canHaveBackdrop=e;let i=f(bb,{optional:!0});this._parentMaterialMenu=i instanceof Ti?i:void 0}ngOnDestroy(){this._menu&&this._ownsMenu(this._menu)&&ns.delete(this._menu),this._pendingRemoval?.unsubscribe(),this._menuCloseSubscription.unsubscribe(),this._closingActionsSubscription.unsubscribe(),this._overlayRef&&(this._overlayRef.dispose(),this._overlayRef=null)}get menuOpen(){return this._menuOpen}get dir(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_triggersSubmenu(){return!!(this._menuItemInstance&&this._parentMaterialMenu&&this._menu)}_closeMenu(){this._menu?.close.emit()}_openMenu(e){if(this._triggerIsAriaDisabled())return;let i=this._menu;if(this._menuOpen||!i)return;this._pendingRemoval?.unsubscribe();let a=ns.get(i);ns.set(i,this),a&&a!==this&&a._closeMenu();let o=this._createOverlay(i),r=o.getConfig(),s=r.positionStrategy;this._setPosition(i,s),this._canHaveBackdrop?r.hasBackdrop=i.hasBackdrop==null?!this._triggersSubmenu():i.hasBackdrop:r.hasBackdrop=i.hasBackdrop??!1,o.hasAttached()||(o.attach(this._getPortal(i)),i.lazyContent?.attach(this.menuData)),this._closingActionsSubscription=this._menuClosingActions().subscribe(()=>this._closeMenu()),i.parentMenu=this._triggersSubmenu()?this._parentMaterialMenu:void 0,i.direction=this.dir,e&&i.focusFirstItem(this._openedBy||`program`),this._setIsMenuOpen(!0),i instanceof Ti&&(i._setIsOpen(!0),i._directDescendantItems.changes.pipe(Ne(i.close)).subscribe(()=>{s.withLockedPosition(!1).reapplyLastPosition(),s.withLockedPosition(!0)}))}focus(e,i){this._focusMonitor&&e?this._focusMonitor.focusVia(this._element,e,i):this._element.nativeElement.focus(i)}_destroyMenu(e){let i=this._overlayRef,a=this._menu;!i||!this.menuOpen||(this._closingActionsSubscription.unsubscribe(),this._pendingRemoval?.unsubscribe(),a instanceof Ti&&this._ownsMenu(a)?(this._pendingRemoval=a._animationDone.pipe(bn(1)).subscribe(()=>{i.detach(),ns.has(a)||a.lazyContent?.detach()}),a._setIsOpen(!1)):(i.detach(),a?.lazyContent?.detach()),a&&this._ownsMenu(a)&&ns.delete(a),this.restoreFocus&&(e===`keydown`||!this._openedBy||!this._triggersSubmenu())&&this.focus(this._openedBy),this._openedBy=void 0,this._setIsMenuOpen(!1))}_setIsMenuOpen(e){e!==this._menuOpen&&(this._menuOpen=e,this._menuOpen?this.menuOpened.emit():this.menuClosed.emit(),this._triggersSubmenu()&&this._menuItemInstance._setHighlighted(e),this._changeDetectorRef.markForCheck())}_createOverlay(e){if(!this._overlayRef){let i=this._getOverlayConfig(e);this._subscribeToPositions(e,i.positionStrategy),this._overlayRef=sa(this._injector,i),this._overlayRef.keydownEvents().subscribe(a=>{this._menu instanceof Ti&&this._menu._handleKeydown(a)})}return this._overlayRef}_getOverlayConfig(e){return new aa({positionStrategy:Vo(this._injector,this._getOverlayOrigin()).withLockedPosition().withGrowAfterOpen().withTransformOriginOn(`.mat-menu-panel, .mat-mdc-menu-panel`),backdropClass:e.backdropClass||`cdk-overlay-transparent-backdrop`,panelClass:e.overlayPanelClass,scrollStrategy:this._scrollStrategy(),direction:this._dir||`ltr`,disableAnimations:this._animationsDisabled})}_subscribeToPositions(e,i){e.setPositionClasses&&i.positionChanges.subscribe(a=>{this._ngZone.run(()=>{let o=a.connectionPair.overlayX===`start`?`after`:`before`,r=a.connectionPair.overlayY===`top`?`below`:`above`;e.setPositionClasses(o,r)})})}_setPosition(e,i){let[a,o]=e.xPosition===`before`?[`end`,`start`]:[`start`,`end`],[r,s]=e.yPosition===`above`?[`bottom`,`top`]:[`top`,`bottom`],[c,p]=[r,s],[m,g]=[a,o],v=0;if(this._triggersSubmenu()){if(g=a=e.xPosition===`before`?`start`:`end`,o=m=a===`end`?`start`:`end`,this._parentMaterialMenu){if(this._parentInnerPadding==null){let b=this._parentMaterialMenu.items.first;this._parentInnerPadding=b?b._getHostElement().offsetTop:0}v=r===`bottom`?this._parentInnerPadding:-this._parentInnerPadding}}else e.overlapTrigger||(c=r===`top`?`bottom`:`top`,p=s===`top`?`bottom`:`top`);i.withPositions([{originX:a,originY:c,overlayX:m,overlayY:r,offsetY:v},{originX:o,originY:c,overlayX:g,overlayY:r,offsetY:v},{originX:a,originY:p,overlayX:m,overlayY:s,offsetY:-v},{originX:o,originY:p,overlayX:g,overlayY:s,offsetY:-v}])}_menuClosingActions(){let e=this._getOutsideClickStream(this._overlayRef),i=this._overlayRef.detachments();return Dt(e,this._parentMaterialMenu?this._parentMaterialMenu.closed:Mn(),this._parentMaterialMenu?this._parentMaterialMenu._hovered().pipe(Qe(r=>this._menuOpen&&r!==this._menuItemInstance)):Mn(),i)}_getPortal(e){return(!this._portal||this._portal.templateRef!==e.templateRef)&&(this._portal=new Hn(e.templateRef,this._viewContainerRef)),this._portal}_ownsMenu(e){return ns.get(e)===this}_triggerIsAriaDisabled(){return V(this._element.nativeElement.getAttribute(`aria-disabled`))}static ɵfac=function(i){Rf()};static ɵdir=Y({type:t})}return t})();var is=(()=>{class t extends HO{_cleanupTouchstart;_hoverSubscription=De.EMPTY;get _deprecatedMatMenuTriggerFor(){return this.menu}set _deprecatedMatMenuTriggerFor(e){this.menu=e}get menu(){return this._menu}set menu(e){this._menu=e}menuData;restoreFocus=!0;menuOpened=new ee;onMenuOpen=this.menuOpened;menuClosed=new ee;onMenuClose=this.menuClosed;constructor(){super(!0);let e=f(Oe);this._cleanupTouchstart=e.listen(this._element.nativeElement,`touchstart`,i=>{Do(i)||(this._openedBy=`touch`)},{passive:!0})}triggersSubmenu(){return super._triggersSubmenu()}toggleMenu(){return this.menuOpen?this.closeMenu():this.openMenu()}openMenu(){this._openMenu(!0)}closeMenu(){this._closeMenu()}updatePosition(){this._overlayRef?.updatePosition()}ngAfterContentInit(){this._handleHover()}ngOnDestroy(){super.ngOnDestroy(),this._cleanupTouchstart(),this._hoverSubscription.unsubscribe()}_getOverlayOrigin(){return this._element}_getOutsideClickStream(e){return e.backdropClick()}_handleMousedown(e){Eo(e)||(this._openedBy=e.button===0?`mouse`:void 0,this.triggersSubmenu()&&e.preventDefault())}_handleKeydown(e){let i=e.keyCode;(i===13||i===32)&&(this._openedBy=`keyboard`),this.triggersSubmenu()&&(i===39&&this.dir===`ltr`||i===37&&this.dir===`rtl`)&&(this._openedBy=`keyboard`,this.openMenu())}_handleClick(e){this.triggersSubmenu()?(e.stopPropagation(),this.openMenu()):this.toggleMenu()}_handleHover(){this.triggersSubmenu()&&this._parentMaterialMenu&&(this._hoverSubscription=this._parentMaterialMenu._hovered().subscribe(e=>{e===this._menuItemInstance&&!e.disabled&&this._parentMaterialMenu?._panelAnimationState!==`void`&&(this._openedBy=`mouse`,this._openMenu(!1))}))}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`mat-menu-trigger-for`,``],[``,`matMenuTriggerFor`,``]],hostAttrs:[1,`mat-mdc-menu-trigger`],hostVars:3,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._handleClick(r)})(`mousedown`,function(r){return a._handleMousedown(r)})(`keydown`,function(r){return a._handleKeydown(r)}),i&2&&te(`aria-haspopup`,a.menu?`menu`:null)(`aria-expanded`,a.menuOpen)(`aria-controls`,a.menuOpen?a.menu?.panelId:null)},inputs:{_deprecatedMatMenuTriggerFor:[0,`mat-menu-trigger-for`,`_deprecatedMatMenuTriggerFor`],menu:[0,`matMenuTriggerFor`,`menu`],menuData:[0,`matMenuTriggerData`,`menuData`],restoreFocus:[0,`matMenuTriggerRestoreFocus`,`restoreFocus`]},outputs:{menuOpened:`menuOpened`,onMenuOpen:`onMenuOpen`,menuClosed:`menuClosed`,onMenuClose:`onMenuClose`},exportAs:[`matMenuTrigger`],features:[Ve]})}return t})();var as=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[Ii,pi,we,ia]})}return t})();var Bo=class{_multiple;_emitChanges;compareWith;_selection=new Set;_deselectedToEmit=[];_selectedToEmit=[];_selected=null;get selected(){return this._selected||(this._selected=Array.from(this._selection.values())),this._selected}changed=new L;bulk={select:n=>this._select(n),deselect:n=>this._deselect(n),setSelection:n=>this._setSelection(n)};constructor(n=!1,e,i=!0,a){this._multiple=n,this._emitChanges=i,this.compareWith=a,e&&e.length&&(n?e.forEach(o=>this._markSelected(o)):this._markSelected(e[0]),this._selectedToEmit.length=0)}select(...n){return this._select(n)}deselect(...n){return this._deselect(n)}setSelection(...n){return this._setSelection(n)}toggle(n){return this.isSelected(n)?this.deselect(n):this.select(n)}clear(n=!0){this._unmarkAll();let e=this._hasQueuedChanges();return n&&this._emitChangeEvent(),e}isSelected(n){return this._selection.has(this._getConcreteValue(n))}isEmpty(){return this._selection.size===0}hasValue(){return!this.isEmpty()}sort(n){this._multiple&&this.selected&&this._selected.sort(n)}isMultipleSelection(){return this._multiple}_select(n){this._verifyValueAssignment(n),n.forEach(i=>this._markSelected(i));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}_deselect(n){this._verifyValueAssignment(n),n.forEach(i=>this._unmarkSelected(i));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}_setSelection(n){this._verifyValueAssignment(n);let e=this.selected,i=new Set(n.map(o=>this._getConcreteValue(o)));n.forEach(o=>this._markSelected(o)),e.filter(o=>!i.has(this._getConcreteValue(o,i))).forEach(o=>this._unmarkSelected(o));let a=this._hasQueuedChanges();return this._emitChangeEvent(),a}_emitChangeEvent(){this._selected=null,(this._selectedToEmit.length||this._deselectedToEmit.length)&&(this.changed.next({source:this,added:this._selectedToEmit,removed:this._deselectedToEmit}),this._deselectedToEmit=[],this._selectedToEmit=[])}_markSelected(n){n=this._getConcreteValue(n),this.isSelected(n)||(this._multiple||this._unmarkAll(),this.isSelected(n)||this._selection.add(n),this._emitChanges&&this._selectedToEmit.push(n))}_unmarkSelected(n){n=this._getConcreteValue(n),this.isSelected(n)&&(this._selection.delete(n),this._emitChanges&&this._deselectedToEmit.push(n))}_unmarkAll(){this.isEmpty()||this._selection.forEach(n=>this._unmarkSelected(n))}_verifyValueAssignment(n){n.length>1&&this._multiple}_hasQueuedChanges(){return!!(this._deselectedToEmit.length||this._selectedToEmit.length)}_getConcreteValue(n,e){if(this.compareWith){e=e??this._selection;for(let i of e)if(this.compareWith(n,i))return i;return n}else return n}};var um=(()=>{class t{_animationsDisabled=Ue();state=`unchecked`;disabled=!1;appearance=`full`;static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`mat-pseudo-checkbox`]],hostAttrs:[1,`mat-pseudo-checkbox`],hostVars:12,hostBindings:function(i,a){i&2&&q(`mat-pseudo-checkbox-indeterminate`,a.state===`indeterminate`)(`mat-pseudo-checkbox-checked`,a.state===`checked`)(`mat-pseudo-checkbox-disabled`,a.disabled)(`mat-pseudo-checkbox-minimal`,a.appearance===`minimal`)(`mat-pseudo-checkbox-full`,a.appearance===`full`)(`_mat-animation-noopable`,a._animationsDisabled)},inputs:{state:`state`,disabled:`disabled`,appearance:`appearance`},decls:0,vars:0,template:function(i,a){},styles:[`.mat-pseudo-checkbox {
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
`],encapsulation:2})}return t})();var _b=new I(`MAT_OPTION_PARENT_COMPONENT`);var xb=new I(`MatOptgroup`);var yb=class{source;isUserInput;constructor(n,e=!1){this.source=n,this.isUserInput=e}};var Un=(()=>{class t{_element=f(W);_changeDetectorRef=f(Pe);_parent=f(_b,{optional:!0});group=f(xb,{optional:!0});_signalDisableRipple=!1;_selected=!1;_active=!1;_mostRecentViewValue=``;get multiple(){return this._parent&&this._parent.multiple}get selected(){return this._selected}value;id=f(Be).getId(`mat-option-`);get disabled(){return this.group&&this.group.disabled||this._disabled()}set disabled(e){this._disabled.set(e)}_disabled=N(!1);get disableRipple(){return this._signalDisableRipple?this._parent.disableRipple():!!this._parent?.disableRipple}get hideSingleSelectionIndicator(){return!!(this._parent&&this._parent.hideSingleSelectionIndicator)}onSelectionChange=new ee;_text;_stateChanges=new L;constructor(){let e=f(Je);e.load(dn),e.load(ki),this._signalDisableRipple=!!this._parent&&sn(this._parent.disableRipple)}get active(){return this._active}get viewValue(){return(this._text?.nativeElement.textContent||``).trim()}select(e=!0){this._selected||(this._selected=!0,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}deselect(e=!0){this._selected&&(this._selected=!1,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}focus(e,i){let a=this._getHostElement();typeof a.focus==`function`&&a.focus(i)}setActiveStyles(){this._active||(this._active=!0,this._changeDetectorRef.markForCheck())}setInactiveStyles(){this._active&&(this._active=!1,this._changeDetectorRef.markForCheck())}getLabel(){return this.viewValue}_handleKeydown(e){(e.keyCode===13||e.keyCode===32)&&!xt(e)&&(this._selectViaInteraction(),e.preventDefault())}_selectViaInteraction(){this.disabled||(this._selected=this.multiple?!this._selected:!0,this._changeDetectorRef.markForCheck(),this._emitSelectionChangeEvent(!0))}_getTabIndex(){return this.disabled?`-1`:`0`}_getHostElement(){return this._element.nativeElement}ngAfterViewChecked(){if(this._selected){let e=this.viewValue;e!==this._mostRecentViewValue&&(this._mostRecentViewValue&&this._stateChanges.next(),this._mostRecentViewValue=e)}}ngOnDestroy(){this._stateChanges.complete()}_emitSelectionChangeEvent(e=!1){this.onSelectionChange.emit(new yb(this,e))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`text`],i=[[[`mat-icon`]],`*`],a=[`mat-icon`,`*`];function o(c,p){if(c&1&&z(0,`mat-pseudo-checkbox`,1),c&2){let m=y();S(`disabled`,m.disabled)(`state`,m.selected?`checked`:`unchecked`)}}function r(c,p){if(c&1&&z(0,`mat-pseudo-checkbox`,3),c&2)S(`disabled`,y().disabled)}function s(c,p){if(c&1&&(l(0,`span`,4),u(1),d()),c&2){let m=y();h(),H(`(`,m.group.label,`)`)}}return B({type:t,selectors:[[`mat-option`]],viewQuery:function(p,m){if(p&1&&Ze(e,7),p&2){let g;Z(g=J())&&(m._text=g.first)}},hostAttrs:[`role`,`option`,1,`mat-mdc-option`,`mdc-list-item`],hostVars:11,hostBindings:function(p,m){p&1&&C(`click`,function(){return m._selectViaInteraction()})(`keydown`,function(v){return m._handleKeydown(v)}),p&2&&(qt(`id`,m.id),te(`aria-selected`,m.selected)(`aria-disabled`,m.disabled.toString()),q(`mdc-list-item--selected`,m.selected)(`mat-mdc-option-multiple`,m.multiple)(`mat-mdc-option-active`,m.active)(`mdc-list-item--disabled`,m.disabled))},inputs:{value:`value`,id:`id`,disabled:[2,`disabled`,`disabled`,V]},outputs:{onSelectionChange:`onSelectionChange`},exportAs:[`matOption`],ngContentSelectors:a,decls:8,vars:5,consts:[[`text`,``],[`aria-hidden`,`true`,1,`mat-mdc-option-pseudo-checkbox`,3,`disabled`,`state`],[1,`mdc-list-item__primary-text`],[`state`,`checked`,`aria-hidden`,`true`,`appearance`,`minimal`,1,`mat-mdc-option-pseudo-checkbox`,3,`disabled`],[1,`cdk-visually-hidden`],[`aria-hidden`,`true`,`mat-ripple`,``,1,`mat-mdc-option-ripple`,`mat-focus-indicator`,3,`matRippleTrigger`,`matRippleDisabled`]],template:function(p,m){p&1&&(Re(i),D(0,o,1,2,`mat-pseudo-checkbox`,1),me(1),l(2,`span`,2,0),me(4,1),d(),D(5,r,1,1,`mat-pseudo-checkbox`,3),D(6,s,2,1,`span`,4),z(7,`div`,5)),p&2&&(k(m.multiple?0:-1),h(5),k(!m.multiple&&m.selected&&!m.hideSingleSelectionIndicator?5:-1),h(),k(m.group&&m.group._inert?6:-1),h(),S(`matRippleTrigger`,m._getHostElement())(`matRippleDisabled`,m.disabled||m.disableRipple))},dependencies:[um,li],styles:[`.mat-mdc-option {
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
`],encapsulation:2})})()}return t})();function _w(t,n,e){if(e.length){let i=n.toArray(),a=e.toArray(),o=0;for(let r=0;r<t+1;r++)i[r].group&&i[r].group===a[o]&&o++;return o}return 0}function xw(t,n,e,i){return t<e?t:t+n>e+i?Math.max(0,t-i+n):e}var Cw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[we]})}return t})();var Cb=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[Ii,Cw,Un,we]})}return t})();var UO=new I(`mat-select-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>ra(t)}});var $O=new I(`MAT_SELECT_CONFIG`);var GO=new I(`MatSelectTrigger`);var Sb=class{source;value;constructor(n,e){this.source=n,this.value=e}};var Ra=(()=>{class t{_viewportRuler=f(mi);_changeDetectorRef=f(Pe);_elementRef=f(W);_dir=f(ft,{optional:!0});_idGenerator=f(Be);_renderer=f(Oe);_parentFormField=f(Vc,{optional:!0});ngControl=f(Mi,{self:!0,optional:!0});_liveAnnouncer=f(Fg);_defaultOptions=f($O,{optional:!0});_animationsDisabled=Ue();_popoverLocation;_initialized=new L;_cleanupDetach;options;optionGroups;customTrigger;_positions=[{originX:`start`,originY:`bottom`,overlayX:`start`,overlayY:`top`},{originX:`end`,originY:`bottom`,overlayX:`end`,overlayY:`top`},{originX:`start`,originY:`top`,overlayX:`start`,overlayY:`bottom`,panelClass:`mat-mdc-select-panel-above`},{originX:`end`,originY:`top`,overlayX:`end`,overlayY:`bottom`,panelClass:`mat-mdc-select-panel-above`}];_scrollOptionIntoView(e){let i=this.options.toArray()[e];if(i){let a=this.panel.nativeElement,o=_w(e,this.options,this.optionGroups),r=i._getHostElement();e===0&&o===1?a.scrollTop=0:a.scrollTop=xw(r.offsetTop,r.offsetHeight,a.scrollTop,a.offsetHeight)}}_positioningSettled(){this._scrollOptionIntoView(this._keyManager.activeItemIndex||0)}_getChangeEvent(e){return new Sb(this,e)}_scrollStrategyFactory=f(UO);_panelOpen=!1;_compareWith=(e,i)=>e===i;_uid=this._idGenerator.getId(`mat-select-`);_triggerAriaLabelledBy=null;_previousControl;_destroy=new L;_errorStateTracker;stateChanges=new L;disableAutomaticLabeling=!0;userAriaDescribedBy;_selectionModel;_keyManager;_preferredOverlayOrigin;_overlayWidth;_onChange=()=>{};_onTouched=()=>{};_valueId=this._idGenerator.getId(`mat-select-value-`);_scrollStrategy;_overlayPanelClass=this._defaultOptions?.overlayPanelClass||``;get focused(){return this._focused||this._panelOpen}_focused=!1;controlType=`mat-select`;trigger;panel;_overlayDir;panelClass;disabled=!1;get disableRipple(){return this._disableRipple()}set disableRipple(e){this._disableRipple.set(e)}_disableRipple=N(!1);tabIndex=0;get hideSingleSelectionIndicator(){return this._hideSingleSelectionIndicator}set hideSingleSelectionIndicator(e){this._hideSingleSelectionIndicator=e,this._syncParentProperties()}_hideSingleSelectionIndicator=this._defaultOptions?.hideSingleSelectionIndicator??!1;get placeholder(){return this._placeholder}set placeholder(e){this._placeholder=e,this.stateChanges.next()}_placeholder;get required(){return this._required??this.ngControl?.control?.hasValidator(Ro.required)??!1}set required(e){this._required=e,this.stateChanges.next()}_required;get multiple(){return this._multiple}set multiple(e){this._selectionModel,this._multiple=e}_multiple=!1;disableOptionCentering=this._defaultOptions?.disableOptionCentering??!1;get compareWith(){return this._compareWith}set compareWith(e){this._compareWith=e,this._selectionModel&&this._initializeSelection()}get value(){return this._value}set value(e){this._assignValue(e)&&this._onChange(e)}_value;ariaLabel=``;ariaLabelledby;get errorStateMatcher(){return this._errorStateTracker.matcher}set errorStateMatcher(e){this._errorStateTracker.matcher=e}typeaheadDebounceInterval;sortComparator;get id(){return this._id}set id(e){this._id=e||this._uid,this.stateChanges.next()}_id;get errorState(){return this._errorStateTracker.errorState}set errorState(e){this._errorStateTracker.errorState=e}panelWidth=this._defaultOptions&&typeof this._defaultOptions.panelWidth<`u`?this._defaultOptions.panelWidth:`auto`;canSelectNullableOptions=this._defaultOptions?.canSelectNullableOptions??!1;optionSelectionChanges=Ka(()=>{let e=this.options;return e?e.changes.pipe(ut(e),vn(()=>Dt(...e.map(i=>i.onSelectionChange)))):this._initialized.pipe(vn(()=>this.optionSelectionChanges))});openedChange=new ee;_openedStream=this.openedChange.pipe(Qe(e=>e),rt(()=>{}));_closedStream=this.openedChange.pipe(Qe(e=>!e),rt(()=>{}));selectionChange=new ee;valueChange=new ee;constructor(){let e=f(qr),i=f(Rc,{optional:!0}),a=f(Pc,{optional:!0}),o=f(new Ki(`tabindex`),{optional:!0}),r=f(Gc,{optional:!0}),s=f(Qu,{optional:!0,self:!0});this.ngControl&&(this.ngControl.valueAccessor=this),this._defaultOptions?.typeaheadDebounceInterval!=null&&(this.typeaheadDebounceInterval=this._defaultOptions.typeaheadDebounceInterval),this._errorStateTracker=new Yr(e,s||this.ngControl,a,i,this.stateChanges),this._scrollStrategy=this._scrollStrategyFactory(),this.tabIndex=o==null?0:parseInt(o)||0,this._popoverLocation=r?.usePopover===!1?null:`inline`,this.id=this.id}ngOnInit(){this._selectionModel=new Bo(this.multiple),this.stateChanges.next(),this._viewportRuler.change().pipe(Ne(this._destroy)).subscribe(()=>{this.panelOpen&&(this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._changeDetectorRef.detectChanges())})}ngAfterContentInit(){this._initialized.next(),this._initialized.complete(),this._initKeyManager(),this._selectionModel.changed.pipe(Ne(this._destroy)).subscribe(e=>{e.added.forEach(i=>i.select()),e.removed.forEach(i=>i.deselect())}),this.options.changes.pipe(ut(null),Ne(this._destroy)).subscribe(()=>{this._resetOptions(),this._initializeSelection()})}ngDoCheck(){let e=this._getTriggerAriaLabelledby(),i=this.ngControl;if(e!==this._triggerAriaLabelledBy){let a=this._elementRef.nativeElement;this._triggerAriaLabelledBy=e,e?a.setAttribute(`aria-labelledby`,e):a.removeAttribute(`aria-labelledby`)}i&&(this._previousControl!==i.control&&(this._previousControl!==void 0&&i.disabled!==null&&i.disabled!==this.disabled&&(this.disabled=i.disabled),this._previousControl=i.control),this.updateErrorState())}ngOnChanges(e){(e.disabled||e.userAriaDescribedBy)&&this.stateChanges.next(),e.typeaheadDebounceInterval&&this._keyManager&&this._keyManager.withTypeAhead(this.typeaheadDebounceInterval),e.panelClass&&this.panelClass instanceof Set&&(this.panelClass=Array.from(this.panelClass))}ngOnDestroy(){this._cleanupDetach?.(),this._keyManager?.destroy(),this._destroy.next(),this._destroy.complete(),this.stateChanges.complete()}toggle(){this.panelOpen?this.close():this.open()}open(){this._canOpen()&&(this._parentFormField&&(this._preferredOverlayOrigin=this._parentFormField.getConnectedOverlayOrigin()),this._cleanupDetach?.(),this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._panelOpen=!0,this._overlayDir.positionChange.pipe(bn(1)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this._positioningSettled()}),this._overlayDir.attachOverlay(),this._keyManager.withHorizontalOrientation(null),this._highlightCorrectOption(),this._changeDetectorRef.markForCheck(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!0)))}close(){this._panelOpen&&(this._panelOpen=!1,this._exitAndDetach(),this._keyManager.withHorizontalOrientation(this._isRtl()?`rtl`:`ltr`),this._changeDetectorRef.markForCheck(),this._onTouched(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!1)))}_exitAndDetach(){if(this._animationsDisabled||!this.panel){this._detachOverlay();return}this._cleanupDetach?.(),this._cleanupDetach=()=>{i(),clearTimeout(a),this._cleanupDetach=void 0};let e=this.panel.nativeElement,i=this._renderer.listen(e,`animationend`,o=>{o.animationName===`_mat-select-exit`&&(this._cleanupDetach?.(),this._detachOverlay())}),a=setTimeout(()=>{this._cleanupDetach?.(),this._detachOverlay()},200);e.classList.add(`mat-select-panel-exit`)}_detachOverlay(){this._overlayDir.detachOverlay(),this._changeDetectorRef.markForCheck()}writeValue(e){this._assignValue(e)}registerOnChange(e){this._onChange=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e,this._changeDetectorRef.markForCheck(),this.stateChanges.next()}get panelOpen(){return this._panelOpen}get selected(){return this.multiple?this._selectionModel?.selected||[]:this._selectionModel?.selected[0]}get triggerValue(){if(this.empty)return``;if(this._multiple){let e=this._selectionModel.selected.map(i=>i.viewValue);return this._isRtl()&&e.reverse(),e.join(`, `)}return this._selectionModel.selected[0].viewValue}updateErrorState(){this._errorStateTracker.updateErrorState()}_isRtl(){return this._dir?this._dir.value===`rtl`:!1}_handleKeydown(e){this.disabled||(this.panelOpen?this._handleOpenKeydown(e):this._handleClosedKeydown(e))}_handleClosedKeydown(e){let i=e.keyCode,a=i===40||i===38||i===37||i===39,o=i===13||i===32,r=this._keyManager;if(!r.isTyping()&&o&&!xt(e)||(this.multiple||e.altKey)&&a)e.preventDefault(),this.open();else if(!this.multiple){let s=this.selected;r.onKeydown(e);let c=this.selected;c&&s!==c&&this._liveAnnouncer.announce(c.viewValue,1e4)}}_handleOpenKeydown(e){let i=this._keyManager,a=e.keyCode,o=a===40||a===38,r=i.isTyping();if(o&&e.altKey)e.preventDefault(),this.close();else if(!r&&(a===13||a===32)&&i.activeItem&&!xt(e))e.preventDefault(),i.activeItem._selectViaInteraction();else if(!r&&this._multiple&&a===65&&e.ctrlKey){e.preventDefault();let s=this.options.some(c=>!c.disabled&&!c.selected);this.options.forEach(c=>{c.disabled||(s?c.select():c.deselect())})}else{let s=i.activeItemIndex;i.onKeydown(e),this._multiple&&o&&e.shiftKey&&i.activeItem&&i.activeItemIndex!==s&&i.activeItem._selectViaInteraction()}}_handleOverlayKeydown(e){e.keyCode===27&&!xt(e)&&(e.preventDefault(),this.close())}_onFocus(){this.disabled||(this._focused=!0,this.stateChanges.next())}_onBlur(){this._focused=!1,this._keyManager?.cancelTypeahead(),!this.disabled&&!this.panelOpen&&(this._onTouched(),this._changeDetectorRef.markForCheck(),this.stateChanges.next())}get empty(){return!this._selectionModel||this._selectionModel.isEmpty()}_initializeSelection(){Promise.resolve().then(()=>{this.ngControl&&(this._value=this.ngControl.value),this._setSelectionByValue(this._value),this.stateChanges.next()})}_setSelectionByValue(e){if(this.options.forEach(i=>i.setInactiveStyles()),this._selectionModel.clear(),this.multiple&&e)e.forEach(i=>this._selectOptionByValue(i)),this._sortValues();else{let i=this._selectOptionByValue(e);i?this._keyManager.updateActiveItem(i):this.panelOpen||this._keyManager.updateActiveItem(-1)}this._changeDetectorRef.markForCheck()}_selectOptionByValue(e){let i=this.options.find(a=>{if(this._selectionModel.isSelected(a))return!1;try{return(a.value!=null||this.canSelectNullableOptions)&&this._compareWith(a.value,e)}catch{return!1}});return i&&this._selectionModel.select(i),i}_assignValue(e){return e!==this._value||this._multiple&&Array.isArray(e)?(this.options&&this._setSelectionByValue(e),this._value=e,!0):!1}_skipPredicate=e=>this.panelOpen?!1:e.disabled;_getOverlayWidth(e){return this.panelWidth===`auto`?(e instanceof Jr?e.elementRef:e||this._elementRef).nativeElement.getBoundingClientRect().width:this.panelWidth===null?``:this.panelWidth}_syncParentProperties(){if(this.options)for(let e of this.options)e._changeDetectorRef.markForCheck()}_initKeyManager(){this._keyManager=new Ec(this.options).withTypeAhead(this.typeaheadDebounceInterval).withVerticalOrientation().withHorizontalOrientation(this._isRtl()?`rtl`:`ltr`).withHomeAndEnd().withPageUpDown().withAllowedModifierKeys([`shiftKey`]).skipPredicate(this._skipPredicate),this._keyManager.tabOut.subscribe(()=>{this.panelOpen&&(!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction(),this.focus(),this.close())}),this._keyManager.change.subscribe(()=>{this._panelOpen&&this.panel?this._scrollOptionIntoView(this._keyManager.activeItemIndex||0):!this._panelOpen&&!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction()})}_resetOptions(){let e=Dt(this.options.changes,this._destroy);this.optionSelectionChanges.pipe(Ne(e)).subscribe(i=>{this._onSelect(i.source,i.isUserInput),i.isUserInput&&!this.multiple&&this._panelOpen&&(this.close(),this.focus())}),Dt(...this.options.map(i=>i._stateChanges)).pipe(Ne(e)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this.stateChanges.next()})}_onSelect(e,i){let a=this._selectionModel.isSelected(e);!this.canSelectNullableOptions&&e.value==null&&!this._multiple?(e.deselect(),this._selectionModel.clear(),this.value!=null&&this._propagateChanges(e.value)):(a!==e.selected&&(e.selected?this._selectionModel.select(e):this._selectionModel.deselect(e)),i&&this._keyManager.setActiveItem(e),this.multiple&&(this._sortValues(),i&&this.focus())),a!==this._selectionModel.isSelected(e)&&this._propagateChanges(),this.stateChanges.next()}_sortValues(){if(this.multiple){let e=this.options.toArray();this._selectionModel.sort((i,a)=>this.sortComparator?this.sortComparator(i,a,e):e.indexOf(i)-e.indexOf(a)),this.stateChanges.next()}}_propagateChanges(e){let i;this.multiple?i=this.selected.map(a=>a.value):i=this.selected?this.selected.value:e,this._value=i,this.valueChange.emit(i),this._onChange(i),this.selectionChange.emit(this._getChangeEvent(i)),this._changeDetectorRef.markForCheck()}_highlightCorrectOption(){if(this._keyManager)if(this.empty){let e=-1;for(let i=0;i<this.options.length;i++)if(!this.options.get(i).disabled){e=i;break}this._keyManager.setActiveItem(e)}else this._keyManager.setActiveItem(this._selectionModel.selected[0])}_canOpen(){return!this._panelOpen&&!this.disabled&&this.options?.length>0&&!!this._overlayDir}focus(e){this._elementRef.nativeElement.focus(e)}_getPanelAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||null,i=e?e+` `:``;return this.ariaLabelledby?i+this.ariaLabelledby:e}_getAriaActiveDescendant(){return this.panelOpen&&this._keyManager&&this._keyManager.activeItem?this._keyManager.activeItem.id:null}_getTriggerAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||``;return this.ariaLabelledby&&(e+=` `+this.ariaLabelledby),e||(e=this._valueId),e}get describedByIds(){return this._elementRef.nativeElement.getAttribute(`aria-describedby`)?.split(` `)||[]}setDescribedByIds(e){let i=this._elementRef.nativeElement;e.length?i.setAttribute(`aria-describedby`,e.join(` `)):i.removeAttribute(`aria-describedby`)}onContainerClick(e){let i=Yt(e);i&&(i.tagName===`MAT-OPTION`||i.classList.contains(`cdk-overlay-backdrop`)||i.closest(`.mat-mdc-select-panel`))||(this.focus(),this.open())}get shouldLabelFloat(){return this.panelOpen||!this.empty||this.focused&&!!this.placeholder}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`trigger`],i=[`panel`],a=[[[`mat-select-trigger`]],`*`],o=[`mat-select-trigger`,`*`];function r(g,v){if(g&1&&(l(0,`span`,4),u(1),d()),g&2){let b=y();h(),w(b.placeholder)}}function s(g,v){g&1&&me(0)}function c(g,v){if(g&1&&(l(0,`span`,11),u(1),d()),g&2){let b=y(2);h(),w(b.triggerValue)}}function p(g,v){if(g&1&&(l(0,`span`,5),D(1,s,1,0)(2,c,2,1,`span`,11),d()),g&2){let b=y();h(),k(b.customTrigger?1:2)}}function m(g,v){if(g&1){let b=X();l(0,`div`,12,1),C(`keydown`,function(E){A(b);return O(y()._handleKeydown(E))}),me(2,1),d()}if(g&2){let b=y();tt(b.panelClass),q(`mat-select-panel-animations-enabled`,!b._animationsDisabled)(`mat-primary`,b._parentFormField?.color===`primary`)(`mat-accent`,b._parentFormField?.color===`accent`)(`mat-warn`,b._parentFormField?.color===`warn`)(`mat-undefined`,!b._parentFormField?.color),te(`id`,b.id+`-panel`)(`aria-multiselectable`,b.multiple)(`aria-label`,b.ariaLabel||null)(`aria-labelledby`,b._getPanelAriaLabelledby())}}return B({type:t,selectors:[[`mat-select`]],contentQueries:function(v,b,_){if(v&1&&Bt(_,GO,5)(_,Un,5)(_,xb,5),v&2){let E;Z(E=J())&&(b.customTrigger=E.first),Z(E=J())&&(b.options=E),Z(E=J())&&(b.optionGroups=E)}},viewQuery:function(v,b){if(v&1&&Ze(e,5)(i,5)(lm,5),v&2){let _;Z(_=J())&&(b.trigger=_.first),Z(_=J())&&(b.panel=_.first),Z(_=J())&&(b._overlayDir=_.first)}},hostAttrs:[`role`,`combobox`,`aria-haspopup`,`listbox`,1,`mat-mdc-select`],hostVars:21,hostBindings:function(v,b){v&1&&C(`keydown`,function(E){return b._handleKeydown(E)})(`focus`,function(){return b._onFocus()})(`blur`,function(){return b._onBlur()}),v&2&&(te(`id`,b.id)(`tabindex`,b.disabled?-1:b.tabIndex)(`aria-controls`,b.panelOpen?b.id+`-panel`:null)(`aria-expanded`,b.panelOpen)(`aria-label`,b.ariaLabel||null)(`aria-required`,b.required.toString())(`aria-disabled`,b.disabled.toString())(`aria-invalid`,b.errorState)(`aria-activedescendant`,b._getAriaActiveDescendant()),q(`mat-mdc-select-disabled`,b.disabled)(`mat-mdc-select-invalid`,b.errorState)(`mat-mdc-select-required`,b.required)(`mat-mdc-select-empty`,b.empty)(`mat-mdc-select-multiple`,b.multiple)(`mat-select-open`,b.panelOpen))},inputs:{userAriaDescribedBy:[0,`aria-describedby`,`userAriaDescribedBy`],panelClass:`panelClass`,disabled:[2,`disabled`,`disabled`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V],tabIndex:[2,`tabIndex`,`tabIndex`,g=>g==null?0:ct(g)],hideSingleSelectionIndicator:[2,`hideSingleSelectionIndicator`,`hideSingleSelectionIndicator`,V],placeholder:`placeholder`,required:[2,`required`,`required`,V],multiple:[2,`multiple`,`multiple`,V],disableOptionCentering:[2,`disableOptionCentering`,`disableOptionCentering`,V],compareWith:`compareWith`,value:`value`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],errorStateMatcher:`errorStateMatcher`,typeaheadDebounceInterval:[2,`typeaheadDebounceInterval`,`typeaheadDebounceInterval`,ct],sortComparator:`sortComparator`,id:`id`,panelWidth:`panelWidth`,canSelectNullableOptions:[2,`canSelectNullableOptions`,`canSelectNullableOptions`,V]},outputs:{openedChange:`openedChange`,_openedStream:`opened`,_closedStream:`closed`,selectionChange:`selectionChange`,valueChange:`valueChange`},exportAs:[`matSelect`],features:[Ge([{provide:Lc,useExisting:t},{provide:_b,useExisting:t}]),bt],ngContentSelectors:o,decls:11,vars:10,consts:[[`fallbackOverlayOrigin`,`cdkOverlayOrigin`,`trigger`,``],[`panel`,``],[`cdk-overlay-origin`,``,1,`mat-mdc-select-trigger`,3,`click`],[1,`mat-mdc-select-value`],[1,`mat-mdc-select-placeholder`,`mat-mdc-select-min-line`],[1,`mat-mdc-select-value-text`],[1,`mat-mdc-select-arrow-wrapper`],[1,`mat-mdc-select-arrow`],[`viewBox`,`0 0 24 24`,`width`,`24px`,`height`,`24px`,`focusable`,`false`,`aria-hidden`,`true`],[`d`,`M7 10l5 5 5-5z`],[`cdk-connected-overlay`,``,`cdkConnectedOverlayHasBackdrop`,``,`cdkConnectedOverlayBackdropClass`,`cdk-overlay-transparent-backdrop`,3,`detach`,`backdropClick`,`overlayKeydown`,`cdkConnectedOverlayDisableClose`,`cdkConnectedOverlayPanelClass`,`cdkConnectedOverlayScrollStrategy`,`cdkConnectedOverlayOrigin`,`cdkConnectedOverlayPositions`,`cdkConnectedOverlayWidth`,`cdkConnectedOverlayFlexibleDimensions`,`cdkConnectedOverlayUsePopover`],[1,`mat-mdc-select-min-line`],[`role`,`listbox`,`tabindex`,`-1`,1,`mat-mdc-select-panel`,`mdc-menu-surface`,`mdc-menu-surface--open`,3,`keydown`]],template:function(v,b){if(v&1&&(Re(a),l(0,`div`,2,0),C(`click`,function(){return b.open()}),l(3,`div`,3),D(4,r,2,1,`span`,4)(5,p,3,1,`span`,5),d(),l(6,`div`,6)(7,`div`,7),Gt(),l(8,`svg`,8),z(9,`path`,9),d()()()(),ot(10,m,3,16,`ng-template`,10),C(`detach`,function(){return b.close()})(`backdropClick`,function(){return b.close()})(`overlayKeydown`,function(E){return b._handleOverlayKeydown(E)})),v&2){let _=ht(1);h(3),te(`id`,b._valueId),h(),k(b.empty?4:5),h(6),S(`cdkConnectedOverlayDisableClose`,!0)(`cdkConnectedOverlayPanelClass`,b._overlayPanelClass)(`cdkConnectedOverlayScrollStrategy`,b._scrollStrategy)(`cdkConnectedOverlayOrigin`,b._preferredOverlayOrigin||_)(`cdkConnectedOverlayPositions`,b._positions)(`cdkConnectedOverlayWidth`,b._overlayWidth)(`cdkConnectedOverlayFlexibleDimensions`,!0)(`cdkConnectedOverlayUsePopover`,b._popoverLocation)}},dependencies:[Jr,lm],styles:[`@keyframes _mat-select-enter {
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
`],encapsulation:2})})()}return t})();var Pa=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[pi,Cb,we,ia,ui,Cb]})}return t})();var WO=20;var qO=new I(`mat-tooltip-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>ra(t,{scrollThrottle:WO})}});var YO=new I(`mat-tooltip-default-options`,{providedIn:`root`,factory:()=>({showDelay:0,hideDelay:0,touchendHideDelay:1500})});var Sw=`tooltip-panel`;var KO={passive:!0};var QO=8;var XO=8;var ZO=24;var JO=200;var ww=(()=>{class t{_elementRef=f(W);_ngZone=f(Q);_platform=f(Ae);_ariaDescriber=f(Tu);_focusMonitor=f(Jt);_dir=f(ft);_injector=f(re);_viewContainerRef=f(Vt);_mediaMatcher=f(Pr);_document=f(de);_renderer=f(Oe);_animationsDisabled=Ue();_defaultOptions=f(YO,{optional:!0});_overlayRef=null;_tooltipInstance=null;_overlayPanelClass;_portal;_position=`below`;_positionAtOrigin=!1;_disabled=!1;_tooltipClass;_viewInitialized=!1;_pointerExitEventsInitialized=!1;_tooltipComponent=eR;_viewportMargin=8;_currentPosition;_cssClassPrefix=`mat-mdc`;_ariaDescriptionPending=!1;_dirSubscribed=!1;get position(){return this._position}set position(e){e!==this._position&&(this._position=e,this._overlayRef&&(this._updatePosition(this._overlayRef),this._tooltipInstance?.show(0),this._overlayRef.updatePosition()))}get positionAtOrigin(){return this._positionAtOrigin}set positionAtOrigin(e){this._positionAtOrigin=ta(e),this._detach(),this._overlayRef=null}get disabled(){return this._disabled}set disabled(e){let i=ta(e);this._disabled!==i&&(this._disabled=i,i?this.hide(0):this._setupPointerEnterEventsIfNeeded(),this._syncAriaDescription(this.message))}get showDelay(){return this._showDelay}set showDelay(e){this._showDelay=si(e)}_showDelay;get hideDelay(){return this._hideDelay}set hideDelay(e){this._hideDelay=si(e),this._tooltipInstance&&(this._tooltipInstance._mouseLeaveHideDelay=this._hideDelay)}_hideDelay;touchGestures=`auto`;get message(){return this._message}set message(e){let i=this._message;this._message=e!=null?String(e).trim():``,!this._message&&this._isTooltipVisible()?this.hide(0):(this._setupPointerEnterEventsIfNeeded(),this._updateTooltipMessage()),this._syncAriaDescription(i)}_message=``;get tooltipClass(){return this._tooltipClass}set tooltipClass(e){this._tooltipClass=e,this._tooltipInstance&&this._setTooltipClass(this._tooltipClass)}_eventCleanups=[];_touchstartTimeout=null;_destroyed=new L;_isDestroyed=!1;constructor(){let e=this._defaultOptions;e&&(this._showDelay=e.showDelay,this._hideDelay=e.hideDelay,e.position&&(this.position=e.position),e.positionAtOrigin&&(this.positionAtOrigin=e.positionAtOrigin),e.touchGestures&&(this.touchGestures=e.touchGestures),e.tooltipClass&&(this.tooltipClass=e.tooltipClass)),this._viewportMargin=QO}ngAfterViewInit(){this._viewInitialized=!0,this._setupPointerEnterEventsIfNeeded(),this._focusMonitor.monitor(this._elementRef).pipe(Ne(this._destroyed)).subscribe(e=>{e?e===`keyboard`&&this._ngZone.run(()=>this.show()):this._ngZone.run(()=>this.hide(0))})}ngOnDestroy(){let e=this._elementRef.nativeElement;this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this._overlayRef&&(this._overlayRef.dispose(),this._tooltipInstance=null),this._eventCleanups.forEach(i=>i()),this._eventCleanups.length=0,this._destroyed.next(),this._destroyed.complete(),this._isDestroyed=!0,this._ariaDescriber.removeDescription(e,this.message,`tooltip`),this._focusMonitor.stopMonitoring(e)}show(e=this.showDelay,i){if(this.disabled||!this.message||this._isTooltipVisible()){this._tooltipInstance?._cancelPendingAnimations();return}let a=this._createOverlay(i);this._detach(),this._portal=this._portal||new Ta(this._tooltipComponent,this._viewContainerRef);let o=this._tooltipInstance=a.attach(this._portal).instance;o._triggerElement=this._elementRef.nativeElement,o._mouseLeaveHideDelay=this._hideDelay,o.afterHidden().pipe(Ne(this._destroyed)).subscribe(()=>this._detach()),this._setTooltipClass(this._tooltipClass),this._updateTooltipMessage(),o.show(e)}hide(e=this.hideDelay){let i=this._tooltipInstance;i&&(i.isVisible()?i.hide(e):(i._cancelPendingAnimations(),this._detach()))}toggle(e){this._isTooltipVisible()?this.hide():this.show(void 0,e)}_isTooltipVisible(){return!!this._tooltipInstance&&this._tooltipInstance.isVisible()}_createOverlay(e){if(this._overlayRef){let r=this._overlayRef.getConfig().positionStrategy;if((!this.positionAtOrigin||!e)&&r._origin instanceof W)return this._overlayRef;this._detach()}let i=this._injector.get(Oa).getAncestorScrollContainers(this._elementRef),a=`${this._cssClassPrefix}-${Sw}`,o=Vo(this._injector,this.positionAtOrigin?e||this._elementRef:this._elementRef).withTransformOriginOn(`.${this._cssClassPrefix}-tooltip`).withFlexibleDimensions(!1).withViewportMargin(this._viewportMargin).withScrollableContainers(i).withPopoverLocation(`global`);return o.positionChanges.pipe(Ne(this._destroyed)).subscribe(r=>{this._updateCurrentPositionClass(r.connectionPair),this._tooltipInstance&&r.scrollableViewProperties.isOverlayClipped&&this._tooltipInstance.isVisible()&&this._ngZone.run(()=>this.hide(0))}),this._overlayRef=sa(this._injector,{direction:this._dir,positionStrategy:o,panelClass:this._overlayPanelClass?[...this._overlayPanelClass,a]:a,scrollStrategy:this._injector.get(qO)(),disableAnimations:this._animationsDisabled,eventPredicate:this._overlayEventPredicate}),this._updatePosition(this._overlayRef),this._overlayRef.detachments().pipe(Ne(this._destroyed)).subscribe(()=>this._detach()),this._overlayRef.outsidePointerEvents().pipe(Ne(this._destroyed)).subscribe(()=>this._tooltipInstance?._handleBodyInteraction()),this._overlayRef.keydownEvents().pipe(Ne(this._destroyed)).subscribe(r=>{r.preventDefault(),r.stopPropagation(),this._ngZone.run(()=>this.hide(0))}),this._defaultOptions?.disableTooltipInteractivity&&this._overlayRef.addPanelClass(`${this._cssClassPrefix}-tooltip-panel-non-interactive`),this._dirSubscribed||(this._dirSubscribed=!0,this._dir.change.pipe(Ne(this._destroyed)).subscribe(()=>{this._overlayRef&&this._updatePosition(this._overlayRef)})),this._overlayRef}_detach(){this._overlayRef&&this._overlayRef.hasAttached()&&this._overlayRef.detach(),this._tooltipInstance=null}_updatePosition(e){let i=e.getConfig().positionStrategy,a=this._getOrigin(),o=this._getOverlayPosition();i.withPositions([this._addOffset(r(r({},a.main),o.main)),this._addOffset(r(r({},a.fallback),o.fallback))])}_addOffset(e){let i=XO,a=!this._dir||this._dir.value==`ltr`;return e.originY===`top`?e.offsetY=-i:e.originY===`bottom`?e.offsetY=i:e.originX===`start`?e.offsetX=a?-i:i:e.originX===`end`&&(e.offsetX=a?i:-i),e}_getOrigin(){let e=!this._dir||this._dir.value==`ltr`,i=this.position,a;i==`above`||i==`below`?a={originX:`center`,originY:i==`above`?`top`:`bottom`}:i==`before`||i==`left`&&e||i==`right`&&!e?a={originX:`start`,originY:`center`}:(i==`after`||i==`right`&&e||i==`left`&&!e)&&(a={originX:`end`,originY:`center`});let{x:o,y:r}=this._invertPosition(a.originX,a.originY);return{main:a,fallback:{originX:o,originY:r}}}_getOverlayPosition(){let e=!this._dir||this._dir.value==`ltr`,i=this.position,a;i==`above`?a={overlayX:`center`,overlayY:`bottom`}:i==`below`?a={overlayX:`center`,overlayY:`top`}:i==`before`||i==`left`&&e||i==`right`&&!e?a={overlayX:`end`,overlayY:`center`}:(i==`after`||i==`right`&&e||i==`left`&&!e)&&(a={overlayX:`start`,overlayY:`center`});let{x:o,y:r}=this._invertPosition(a.overlayX,a.overlayY);return{main:a,fallback:{overlayX:o,overlayY:r}}}_updateTooltipMessage(){this._tooltipInstance&&(this._tooltipInstance.message=this.message,this._tooltipInstance._markForCheck(),Tt(()=>{this._tooltipInstance&&this._overlayRef.updatePosition()},{injector:this._injector}))}_setTooltipClass(e){this._tooltipInstance&&(this._tooltipInstance.tooltipClass=e instanceof Set?Array.from(e):e,this._tooltipInstance._markForCheck())}_invertPosition(e,i){return this.position===`above`||this.position===`below`?i===`top`?i=`bottom`:i===`bottom`&&(i=`top`):e===`end`?e=`start`:e===`start`&&(e=`end`),{x:e,y:i}}_updateCurrentPositionClass(e){let{overlayY:i,originX:a,originY:o}=e,r;if(i===`center`?this._dir&&this._dir.value===`rtl`?r=a===`end`?`left`:`right`:r=a===`start`?`left`:`right`:r=i===`bottom`&&o===`top`?`above`:`below`,r!==this._currentPosition){let s=this._overlayRef;if(s){let c=`${this._cssClassPrefix}-${Sw}-`;s.removePanelClass(c+this._currentPosition),s.addPanelClass(c+r)}this._currentPosition=r}}_setupPointerEnterEventsIfNeeded(){this._disabled||!this.message||!this._viewInitialized||this._eventCleanups.length||(this._isTouchPlatform()?this.touchGestures!==`off`&&(this._disableNativeGesturesIfNecessary(),this._addListener(`touchstart`,e=>{let i=e.targetTouches?.[0],a=i?{x:i.clientX,y:i.clientY}:void 0;this._setupPointerExitEventsIfNeeded(),this._touchstartTimeout&&clearTimeout(this._touchstartTimeout);let o=500;this._touchstartTimeout=setTimeout(()=>{this._touchstartTimeout=null,this.show(void 0,a)},this._defaultOptions?.touchLongPressShowDelay??o)})):this._addListener(`mouseenter`,e=>{this._setupPointerExitEventsIfNeeded();let i;e.x!==void 0&&e.y!==void 0&&(i=e),this.show(void 0,i)}))}_setupPointerExitEventsIfNeeded(){if(!this._pointerExitEventsInitialized){if(this._pointerExitEventsInitialized=!0,!this._isTouchPlatform())this._addListener(`mouseleave`,e=>{let i=e.relatedTarget;(!i||!this._overlayRef?.overlayElement.contains(i))&&this.hide()}),this._addListener(`wheel`,e=>{if(this._isTooltipVisible()){let i=this._document.elementFromPoint(e.clientX,e.clientY),a=this._elementRef.nativeElement;i!==a&&!a.contains(i)&&this.hide()}});else if(this.touchGestures!==`off`){this._disableNativeGesturesIfNecessary();let e=()=>{this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this.hide(this._defaultOptions?.touchendHideDelay)};this._addListener(`touchend`,e),this._addListener(`touchcancel`,e)}}}_addListener(e,i){this._eventCleanups.push(this._renderer.listen(this._elementRef.nativeElement,e,i,KO))}_isTouchPlatform(){let e=this._defaultOptions?.detectHoverCapability;return typeof e==`function`?!e():this._platform.IOS||this._platform.ANDROID?!0:this._platform.isBrowser?!!e&&this._mediaMatcher.matchMedia(`(any-hover: none)`).matches:!1}_disableNativeGesturesIfNecessary(){let e=this.touchGestures;if(e!==`off`){let i=this._elementRef.nativeElement,a=i.style;(e===`on`||i.nodeName!==`INPUT`&&i.nodeName!==`TEXTAREA`)&&(a.userSelect=a.msUserSelect=a.webkitUserSelect=a.MozUserSelect=`none`),(e===`on`||!i.draggable)&&(a.webkitUserDrag=`none`),a.touchAction=`none`,a.webkitTapHighlightColor=`transparent`}}_syncAriaDescription(e){this._ariaDescriptionPending||(this._ariaDescriptionPending=!0,this._ariaDescriber.removeDescription(this._elementRef.nativeElement,e,`tooltip`),this._isDestroyed||Tt({write:()=>{this._ariaDescriptionPending=!1,this.message&&!this.disabled&&this._ariaDescriber.describe(this._elementRef.nativeElement,this.message,`tooltip`)}},{injector:this._injector}))}_overlayEventPredicate=e=>e.type===`keydown`?this._isTooltipVisible()&&e.keyCode===27&&!xt(e):!0;static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`matTooltip`,``]],hostAttrs:[1,`mat-mdc-tooltip-trigger`],hostVars:2,hostBindings:function(i,a){i&2&&q(`mat-mdc-tooltip-disabled`,a.disabled)},inputs:{position:[0,`matTooltipPosition`,`position`],positionAtOrigin:[0,`matTooltipPositionAtOrigin`,`positionAtOrigin`],disabled:[0,`matTooltipDisabled`,`disabled`],showDelay:[0,`matTooltipShowDelay`,`showDelay`],hideDelay:[0,`matTooltipHideDelay`,`hideDelay`],touchGestures:[0,`matTooltipTouchGestures`,`touchGestures`],message:[0,`matTooltip`,`message`],tooltipClass:[0,`matTooltipClass`,`tooltipClass`]},exportAs:[`matTooltip`]})}return t})();var eR=(()=>{class t{_changeDetectorRef=f(Pe);_elementRef=f(W);_isMultiline=!1;message;tooltipClass;_showTimeoutId;_hideTimeoutId;_triggerElement;_mouseLeaveHideDelay;_animationsDisabled=Ue();_tooltip;_closeOnInteraction=!1;_isVisible=!1;_onHide=new L;_showAnimation=`mat-mdc-tooltip-show`;_hideAnimation=`mat-mdc-tooltip-hide`;show(e){this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=setTimeout(()=>{this._toggleVisibility(!0),this._showTimeoutId=void 0},e)}hide(e){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId=setTimeout(()=>{this._toggleVisibility(!1),this._hideTimeoutId=void 0},e)}afterHidden(){return this._onHide}isVisible(){return this._isVisible}ngOnDestroy(){this._cancelPendingAnimations(),this._onHide.complete(),this._triggerElement=null}_handleBodyInteraction(){this._closeOnInteraction&&this.hide(0)}_markForCheck(){this._changeDetectorRef.markForCheck()}_handleMouseLeave({relatedTarget:e}){(!e||!this._triggerElement.contains(e))&&(this.isVisible()?this.hide(this._mouseLeaveHideDelay):this._finalizeAnimation(!1))}_onShow(){this._isMultiline=this._isTooltipMultiline(),this._markForCheck()}_isTooltipMultiline(){let e=this._elementRef.nativeElement.getBoundingClientRect();return e.height>ZO&&e.width>=JO}_handleAnimationEnd({animationName:e}){(e===this._showAnimation||e===this._hideAnimation)&&this._finalizeAnimation(e===this._showAnimation)}_cancelPendingAnimations(){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=this._hideTimeoutId=void 0}_finalizeAnimation(e){e?this._closeOnInteraction=!0:this.isVisible()||this._onHide.next()}_toggleVisibility(e){let i=this._tooltip.nativeElement,a=this._showAnimation,o=this._hideAnimation;if(i.classList.remove(e?o:a),i.classList.add(e?a:o),this._isVisible!==e&&(this._isVisible=e,this._changeDetectorRef.markForCheck()),e&&!this._animationsDisabled&&typeof getComputedStyle==`function`){let r=getComputedStyle(i);(r.getPropertyValue(`animation-duration`)===`0s`||r.getPropertyValue(`animation-name`)===`none`)&&(this._animationsDisabled=!0)}e&&this._onShow(),this._animationsDisabled&&(i.classList.add(`_mat-animation-noopable`),this._finalizeAnimation(e))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`tooltip`];return B({type:t,selectors:[[`mat-tooltip-component`]],viewQuery:function(a,o){if(a&1&&Ze(e,7),a&2){let r;Z(r=J())&&(o._tooltip=r.first)}},hostAttrs:[`aria-hidden`,`true`],hostBindings:function(a,o){a&1&&C(`mouseleave`,function(s){return o._handleMouseLeave(s)})},decls:4,vars:5,consts:[[`tooltip`,``],[1,`mdc-tooltip`,`mat-mdc-tooltip`,3,`animationend`],[1,`mat-mdc-tooltip-surface`,`mdc-tooltip__surface`]],template:function(a,o){a&1&&(st(0,`div`,1,0),Ir(`animationend`,function(s){return o._handleAnimationEnd(s)}),st(2,`div`,2),u(3),pt()()),a&2&&(tt(o.tooltipClass),q(`mdc-tooltip--multiline`,o._isMultiline),h(3),w(o.message))},styles:[`.mat-mdc-tooltip {
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
`],encapsulation:2})})()}return t})();var Ew=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[Io,pi,we,ia]})}return t})();var tR=(()=>{class t{changes=new L;itemsPerPageLabel=`Items per page:`;nextPageLabel=`Next page`;previousPageLabel=`Previous page`;firstPageLabel=`First page`;lastPageLabel=`Last page`;getRangeLabel=(e,i,a)=>{if(a==0||i==0)return`0 of ${a}`;a=Math.max(a,0);let o=e*i,r=o<a?Math.min(o+i,a):o+i;return`${o+1} \u2013 ${r} of ${a}`};static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var nR=50;var iR=new I(`MAT_PAGINATOR_DEFAULT_OPTIONS`);var Wc=(()=>{class t{_intl=f(tR);_changeDetectorRef=f(Pe);_formFieldAppearance;_pageSizeLabelId=f(Be).getId(`mat-paginator-page-size-label-`);_intlChanges;_isInitialized=!1;_initializedStream=new Yn(1);color;get pageIndex(){return this._pageIndex}set pageIndex(e){this._pageIndex=Math.max(e||0,0),this._changeDetectorRef.markForCheck()}_pageIndex=0;get length(){return this._length}set length(e){this._length=e||0,this._changeDetectorRef.markForCheck()}_length=0;get pageSize(){return this._pageSize}set pageSize(e){this._pageSize=Math.max(e||0,0),this._updateDisplayedPageSizeOptions()}_pageSize;get pageSizeOptions(){return this._pageSizeOptions}set pageSizeOptions(e){this._pageSizeOptions=(e||[]).map(i=>ct(i,0)),this._updateDisplayedPageSizeOptions()}_pageSizeOptions=[];hidePageSize=!1;showFirstLastButtons=!1;selectConfig={};disabled=!1;page=new ee;_displayedPageSizeOptions;initialized=this._initializedStream;constructor(){let e=this._intl,i=f(iR,{optional:!0});if(this._intlChanges=e.changes.subscribe(()=>this._changeDetectorRef.markForCheck()),i){let{pageSize:a,pageSizeOptions:o,hidePageSize:r,showFirstLastButtons:s}=i;a!=null&&(this._pageSize=a),o!=null&&(this._pageSizeOptions=o),r!=null&&(this.hidePageSize=r),s!=null&&(this.showFirstLastButtons=s)}this._formFieldAppearance=i?.formFieldAppearance||`outline`}ngOnInit(){this._isInitialized=!0,this._updateDisplayedPageSizeOptions(),this._initializedStream.next()}ngOnDestroy(){this._initializedStream.complete(),this._intlChanges.unsubscribe()}nextPage(){this.hasNextPage()&&this._navigate(this.pageIndex+1)}previousPage(){this.hasPreviousPage()&&this._navigate(this.pageIndex-1)}firstPage(){this.hasPreviousPage()&&this._navigate(0)}lastPage(){this.hasNextPage()&&this._navigate(this.getNumberOfPages()-1)}hasPreviousPage(){return this.pageIndex>=1&&this.pageSize!=0}hasNextPage(){let e=this.getNumberOfPages()-1;return this.pageIndex<e&&this.pageSize!=0}getNumberOfPages(){return this.pageSize?Math.ceil(this.length/this.pageSize):0}_changePageSize(e){let i=this.pageIndex*this.pageSize,a=this.pageIndex;this.pageIndex=Math.floor(i/e)||0,this.pageSize=e,this._emitPageEvent(a)}_nextButtonsDisabled(){return this.disabled||!this.hasNextPage()}_previousButtonsDisabled(){return this.disabled||!this.hasPreviousPage()}_updateDisplayedPageSizeOptions(){this._isInitialized&&(this.pageSize||(this._pageSize=this.pageSizeOptions.length!=0?this.pageSizeOptions[0]:nR),this._displayedPageSizeOptions=this.pageSizeOptions.slice(),this._displayedPageSizeOptions.indexOf(this.pageSize)===-1&&this._displayedPageSizeOptions.push(this.pageSize),this._displayedPageSizeOptions.sort((e,i)=>e-i),this._changeDetectorRef.markForCheck())}_emitPageEvent(e){this.page.emit({previousPageIndex:e,pageIndex:this.pageIndex,pageSize:this.pageSize,length:this.length})}_navigate(e){let i=this.pageIndex;e!==i&&(this.pageIndex=e,this._emitPageEvent(i))}_buttonClicked(e,i){i||this._navigate(e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(c,p){if(c&1&&(l(0,`mat-option`,17),u(1),d()),c&2){let m=p.$implicit;S(`value`,m),h(),H(` `,m,` `)}}function i(c,p){if(c&1){let m=X();l(0,`mat-form-field`,14)(1,`mat-select`,16,0),C(`selectionChange`,function(v){A(m);return O(y(2)._changePageSize(v.value))}),be(3,e,2,2,`mat-option`,17,Pn),d(),l(5,`div`,18),C(`click`,function(){A(m);return O(ht(2).open())}),d()()}if(c&2){let m=y(2);S(`appearance`,m._formFieldAppearance)(`color`,m.color),h(),S(`value`,m.pageSize)(`disabled`,m.disabled),kr(`aria-labelledby`,m._pageSizeLabelId),S(`panelClass`,m.selectConfig.panelClass||``)(`disableOptionCentering`,m.selectConfig.disableOptionCentering),h(2),ve(m._displayedPageSizeOptions)}}function a(c,p){if(c&1&&(l(0,`div`,15),u(1),d()),c&2){let m=y(2);h(),w(m.pageSize)}}function o(c,p){if(c&1&&(l(0,`div`,3)(1,`div`,13),u(2),d(),D(3,i,6,7,`mat-form-field`,14),D(4,a,2,1,`div`,15),d()),c&2){let m=y();h(),te(`id`,m._pageSizeLabelId),h(),H(` `,m._intl.itemsPerPageLabel,` `),h(),k(m._displayedPageSizeOptions.length>1?3:-1),h(),k(m._displayedPageSizeOptions.length<=1?4:-1)}}function r(c,p){if(c&1){let m=X();l(0,`button`,19),C(`click`,function(){A(m);let v=y();return O(v._buttonClicked(0,v._previousButtonsDisabled()))}),Gt(),l(1,`svg`,8),z(2,`path`,20),d()()}if(c&2){let m=y();S(`matTooltip`,m._intl.firstPageLabel)(`matTooltipDisabled`,m._previousButtonsDisabled())(`disabled`,m._previousButtonsDisabled())(`tabindex`,m._previousButtonsDisabled()?-1:null),te(`aria-label`,m._intl.firstPageLabel)}}function s(c,p){if(c&1){let m=X();l(0,`button`,21),C(`click`,function(){A(m);let v=y();return O(v._buttonClicked(v.getNumberOfPages()-1,v._nextButtonsDisabled()))}),Gt(),l(1,`svg`,8),z(2,`path`,22),d()()}if(c&2){let m=y();S(`matTooltip`,m._intl.lastPageLabel)(`matTooltipDisabled`,m._nextButtonsDisabled())(`disabled`,m._nextButtonsDisabled())(`tabindex`,m._nextButtonsDisabled()?-1:null),te(`aria-label`,m._intl.lastPageLabel)}}return B({type:t,selectors:[[`mat-paginator`]],hostAttrs:[`role`,`group`,1,`mat-mdc-paginator`],inputs:{color:`color`,pageIndex:[2,`pageIndex`,`pageIndex`,ct],length:[2,`length`,`length`,ct],pageSize:[2,`pageSize`,`pageSize`,ct],pageSizeOptions:`pageSizeOptions`,hidePageSize:[2,`hidePageSize`,`hidePageSize`,V],showFirstLastButtons:[2,`showFirstLastButtons`,`showFirstLastButtons`,V],selectConfig:`selectConfig`,disabled:[2,`disabled`,`disabled`,V]},outputs:{page:`page`},exportAs:[`matPaginator`],decls:14,vars:14,consts:[[`selectRef`,``],[1,`mat-mdc-paginator-outer-container`],[1,`mat-mdc-paginator-container`],[1,`mat-mdc-paginator-page-size`],[1,`mat-mdc-paginator-range-actions`],[`aria-atomic`,`true`,`aria-live`,`polite`,`role`,`status`,1,`mat-mdc-paginator-range-label`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-first`,3,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-previous`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`viewBox`,`0 0 24 24`,`focusable`,`false`,`aria-hidden`,`true`,1,`mat-mdc-paginator-icon`],[`d`,`M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-next`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-last`,3,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`aria-hidden`,`true`,1,`mat-mdc-paginator-page-size-label`],[1,`mat-mdc-paginator-page-size-select`,3,`appearance`,`color`],[1,`mat-mdc-paginator-page-size-value`],[`hideSingleSelectionIndicator`,``,3,`selectionChange`,`value`,`disabled`,`aria-labelledby`,`panelClass`,`disableOptionCentering`],[3,`value`],[1,`mat-mdc-paginator-touch-target`,3,`click`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-first`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M18.41 16.59L13.82 12l4.59-4.59L17 6l-6 6 6 6zM6 6h2v12H6z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-last`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M5.59 7.41L10.18 12l-4.59 4.59L7 18l6-6-6-6zM16 6h2v12h-2z`]],template:function(p,m){p&1&&(l(0,`div`,1)(1,`div`,2),D(2,o,5,4,`div`,3),l(3,`div`,4)(4,`div`,5),u(5),d(),D(6,r,3,5,`button`,6),l(7,`button`,7),C(`click`,function(){return m._buttonClicked(m.pageIndex-1,m._previousButtonsDisabled())}),Gt(),l(8,`svg`,8),z(9,`path`,9),d()(),ya(),l(10,`button`,10),C(`click`,function(){return m._buttonClicked(m.pageIndex+1,m._nextButtonsDisabled())}),Gt(),l(11,`svg`,8),z(12,`path`,11),d()(),D(13,s,3,5,`button`,12),d()()()),p&2&&(h(2),k(m.hidePageSize?-1:2),h(3),H(` `,m._intl.getRangeLabel(m.pageIndex,m.pageSize,m.length),` `),h(),k(m.showFirstLastButtons?6:-1),h(),S(`matTooltip`,m._intl.previousPageLabel)(`matTooltipDisabled`,m._previousButtonsDisabled())(`disabled`,m._previousButtonsDisabled())(`tabindex`,m._previousButtonsDisabled()?-1:null),te(`aria-label`,m._intl.previousPageLabel),h(3),S(`matTooltip`,m._intl.nextPageLabel)(`matTooltipDisabled`,m._nextButtonsDisabled())(`disabled`,m._nextButtonsDisabled())(`tabindex`,m._nextButtonsDisabled()?-1:null),te(`aria-label`,m._intl.nextPageLabel),h(3),k(m.showFirstLastButtons?13:-1))},dependencies:[Ni,Ra,Un,Wg,ww],styles:[`.mat-mdc-paginator {
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
`],encapsulation:2})})()}return t})();var mm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[Ot,Pa,Ew,Wc]})}return t})();var aR=new I(`MAT_PROGRESS_BAR_DEFAULT_OPTIONS`);var os=(()=>{class t{_elementRef=f(W);_ngZone=f(Q);_changeDetectorRef=f(Pe);_renderer=f(Oe);_cleanupTransitionEnd;constructor(){let e=Dc(),i=f(aR,{optional:!0});this._isNoopAnimation=e===`di-disabled`,e===`reduced-motion`&&this._elementRef.nativeElement.classList.add(`mat-progress-bar-reduced-motion`),i&&(i.color&&(this.color=this._defaultColor=i.color),this.mode=i.mode||this.mode)}_isNoopAnimation;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor=`primary`;get value(){return this._value}set value(e){this._value=kw(e||0),this._changeDetectorRef.markForCheck()}_value=0;get bufferValue(){return this._bufferValue||0}set bufferValue(e){this._bufferValue=kw(e||0),this._changeDetectorRef.markForCheck()}_bufferValue=0;animationEnd=new ee;get mode(){return this._mode}set mode(e){this._mode=e,this._changeDetectorRef.markForCheck()}_mode=`determinate`;ngAfterViewInit(){this._ngZone.runOutsideAngular(()=>{this._cleanupTransitionEnd=this._renderer.listen(this._elementRef.nativeElement,`transitionend`,this._transitionendHandler)})}ngOnDestroy(){this._cleanupTransitionEnd?.()}_getPrimaryBarTransform(){return`scaleX(${this._isIndeterminate()?1:this.value/100})`}_getBufferBarFlexBasis(){return`${this.mode===`buffer`?this.bufferValue:100}%`}_isIndeterminate(){return this.mode===`indeterminate`||this.mode===`query`}_transitionendHandler=e=>{this.animationEnd.observers.length===0||!e.target||!e.target.classList.contains(`mdc-linear-progress__primary-bar`)||(this.mode===`determinate`||this.mode===`buffer`)&&this._ngZone.run(()=>this.animationEnd.next({value:this.value}))};static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(i,a){i&1&&Wt(0,`div`,2)}return B({type:t,selectors:[[`mat-progress-bar`]],hostAttrs:[`role`,`progressbar`,`aria-valuemin`,`0`,`aria-valuemax`,`100`,`tabindex`,`-1`,1,`mat-mdc-progress-bar`,`mdc-linear-progress`],hostVars:10,hostBindings:function(a,o){a&2&&(te(`aria-valuenow`,o._isIndeterminate()?null:o.value)(`mode`,o.mode),tt(`mat-`+o.color),q(`_mat-animation-noopable`,o._isNoopAnimation)(`mdc-linear-progress--animation-ready`,!o._isNoopAnimation)(`mdc-linear-progress--indeterminate`,o._isIndeterminate()))},inputs:{color:`color`,value:[2,`value`,`value`,ct],bufferValue:[2,`bufferValue`,`bufferValue`,ct],mode:`mode`},outputs:{animationEnd:`animationEnd`},exportAs:[`matProgressBar`],decls:7,vars:5,consts:[[`aria-hidden`,`true`,1,`mdc-linear-progress__buffer`],[1,`mdc-linear-progress__buffer-bar`],[1,`mdc-linear-progress__buffer-dots`],[`aria-hidden`,`true`,1,`mdc-linear-progress__bar`,`mdc-linear-progress__primary-bar`],[1,`mdc-linear-progress__bar-inner`],[`aria-hidden`,`true`,1,`mdc-linear-progress__bar`,`mdc-linear-progress__secondary-bar`]],template:function(a,o){a&1&&(st(0,`div`,0),Wt(1,`div`,1),D(2,e,1,0,`div`,2),pt(),st(3,`div`,3),Wt(4,`span`,4),pt(),st(5,`div`,5),Wt(6,`span`,4),pt()),a&2&&(h(),fn(`flex-basis`,o._getBufferBarFlexBasis()),h(),k(o.mode===`buffer`?2:-1),h(),fn(`transform`,o._getPrimaryBarTransform()))},styles:[`.mat-mdc-progress-bar {
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
`],encapsulation:2})})()}return t})();function kw(t,n=0,e=100){return Math.max(n,Math.min(e,t))}var rs=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[we]})}return t})();var oR=new I(`MatTabContent`);var qc=(()=>{class t{template=f(Lt);static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`matTabContent`,``]],features:[Ge([{provide:oR,useExisting:t}])]})}return t})();var rR=new I(`MatTabLabel`);var Tw=new I(`MAT_TAB`);var sR=(()=>{class t extends iw{_closestTab=f(Tw,{optional:!0});static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=Y({type:t,selectors:[[``,`mat-tab-label`,``],[``,`matTabLabel`,``]],features:[Ge([{provide:rR,useExisting:t}]),Ve]})}return t})();var Aw=new I(`MAT_TAB_GROUP`);var Yc=(()=>{class t{_viewContainerRef=f(Vt);_closestTabGroup=f(Aw,{optional:!0});disabled=!1;get templateLabel(){return this._templateLabel}set templateLabel(e){this._setTemplateLabelInput(e)}_templateLabel;_explicitContent=void 0;_implicitContent;textLabel=``;ariaLabel;ariaLabelledby;labelClass;bodyClass;id=null;_contentPortal=null;get content(){return this._contentPortal}_stateChanges=new L;position=null;origin=null;isActive=!1;constructor(){f(Je).load(dn)}ngOnChanges(e){(Object.hasOwn(e,`textLabel`)||Object.hasOwn(e,`disabled`))&&this._stateChanges.next()}ngOnDestroy(){this._stateChanges.complete()}ngOnInit(){this._contentPortal=new Hn(this._explicitContent||this._implicitContent,this._viewContainerRef)}_setTemplateLabelInput(e){e&&e._closestTab===this&&(this._templateLabel=e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`];function i(a,o){a&1&&me(0)}return B({type:t,selectors:[[`mat-tab`]],contentQueries:function(o,r,s){if(o&1&&Bt(s,sR,5)(s,qc,7,Lt),o&2){let c;Z(c=J())&&(r.templateLabel=c.first),Z(c=J())&&(r._explicitContent=c.first)}},viewQuery:function(o,r){if(o&1&&Ze(Lt,7),o&2){let s;Z(s=J())&&(r._implicitContent=s.first)}},hostAttrs:[`hidden`,``],hostVars:1,hostBindings:function(o,r){o&2&&te(`id`,null)},inputs:{disabled:[2,`disabled`,`disabled`,V],textLabel:[0,`label`,`textLabel`],ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],labelClass:`labelClass`,bodyClass:`bodyClass`,id:`id`},exportAs:[`matTab`],features:[Ge([{provide:Tw,useExisting:t}]),bt],ngContentSelectors:e,decls:1,vars:0,template:function(o,r){o&1&&(Re(),Dr(0,i,1,0,`ng-template`))},encapsulation:2,changeDetection:1})})()}return t})();var Db=`mdc-tab-indicator--active`;var Iw=`mdc-tab-indicator--no-transition`;var Ib=class{_items;_currentItem;constructor(n){this._items=n}hide(){this._items.forEach(n=>n.deactivateInkBar()),this._currentItem=void 0}alignToElement(n){let e=this._items.find(a=>a.elementRef.nativeElement===n),i=this._currentItem;if(e!==i&&(i?.deactivateInkBar(),e)){let a=i?.elementRef.nativeElement.getBoundingClientRect?.();e.activateInkBar(a),this._currentItem=e}}};var cR=(()=>{class t{_elementRef=f(W);_inkBarElement=null;_inkBarContentElement=null;_fitToContent=!1;get fitInkBarToContent(){return this._fitToContent}set fitInkBarToContent(e){this._fitToContent!==e&&(this._fitToContent=e,this._inkBarElement&&this._appendInkBarElement())}activateInkBar(e){let i=this._elementRef.nativeElement;if(!e||!i.getBoundingClientRect||!this._inkBarContentElement){i.classList.add(Db);return}let a=i.getBoundingClientRect(),o=e.width/a.width,r=e.left-a.left;i.classList.add(Iw),this._inkBarContentElement.style.setProperty(`transform`,`translateX(${r}px) scaleX(${o})`),i.getBoundingClientRect(),i.classList.remove(Iw),i.classList.add(Db),this._inkBarContentElement.style.setProperty(`transform`,``)}deactivateInkBar(){this._elementRef.nativeElement.classList.remove(Db)}ngOnInit(){this._createInkBarElement()}ngOnDestroy(){this._inkBarElement?.remove(),this._inkBarElement=this._inkBarContentElement=null}_createInkBarElement(){let e=this._elementRef.nativeElement.ownerDocument||document,i=this._inkBarElement=e.createElement(`span`),a=this._inkBarContentElement=e.createElement(`span`);i.className=`mdc-tab-indicator`,a.className=`mdc-tab-indicator__content mdc-tab-indicator__content--underline`,i.appendChild(this._inkBarContentElement),this._appendInkBarElement()}_appendInkBarElement(){this._inkBarElement;(this._fitToContent?this._elementRef.nativeElement.querySelector(`.mdc-tab__content`):this._elementRef.nativeElement).appendChild(this._inkBarElement)}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,inputs:{fitInkBarToContent:[2,`fitInkBarToContent`,`fitInkBarToContent`,V]}})}return t})();var Ow=(()=>{class t extends cR{elementRef=f(W);disabled=!1;focus(){this.elementRef.nativeElement.focus()}getOffsetLeft(){return this.elementRef.nativeElement.offsetLeft}getOffsetWidth(){return this.elementRef.nativeElement.offsetWidth}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=Y({type:t,selectors:[[``,`matTabLabelWrapper`,``]],hostVars:3,hostBindings:function(i,a){i&2&&(te(`aria-disabled`,!!a.disabled),q(`mat-mdc-tab-disabled`,a.disabled))},inputs:{disabled:[2,`disabled`,`disabled`,V]},features:[Ve]})}return t})();var Mw={passive:!0};var lR=650;var dR=100;function kb(t){let n=t+``;return/^[0-9]+(?:\.[0-9]+)?$/.test(n)?`${t}ms`:/^[0-9]+(?:\.[0-9]+)?(?:ms|s)$/.test(n)?n:``}var uR=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Pe);_viewportRuler=f(mi);_dir=f(ft,{optional:!0});_ngZone=f(Q);_platform=f(Ae);_sharedResizeObserver=f(Xu);_injector=f(re);_renderer=f(Oe);_animationsDisabled=Ue();_eventCleanups;_scrollDistance=0;_selectedIndexChanged=!1;_destroyed=new L;_showPaginationControls=!1;_disableScrollAfter=!0;_disableScrollBefore=!0;_tabLabelCount;_scrollDistanceChanged=!1;_keyManager;_currentTextContent;_stopScrolling=new L;disablePagination=!1;get selectedIndex(){return this._selectedIndex}set selectedIndex(e){let i=isNaN(e)?0:e;this._selectedIndex!=i&&(this._selectedIndexChanged=!0,this._selectedIndex=i,this._keyManager&&this._keyManager.updateActiveItem(i))}_selectedIndex=0;selectFocusedIndex=new ee;indexFocused=new ee;constructor(){this._eventCleanups=this._ngZone.runOutsideAngular(()=>[this._renderer.listen(this._elementRef.nativeElement,`mouseleave`,()=>this._stopInterval())])}ngAfterViewInit(){this._eventCleanups.push(this._renderer.listen(this._previousPaginator.nativeElement,`touchstart`,()=>this._handlePaginatorPress(`before`),Mw),this._renderer.listen(this._nextPaginator.nativeElement,`touchstart`,()=>this._handlePaginatorPress(`after`),Mw))}ngAfterContentInit(){let e=this._dir?this._dir.change:Mn(`ltr`),i=this._sharedResizeObserver.observe(this._elementRef.nativeElement).pipe(Pi(32),Ne(this._destroyed)),a=this._viewportRuler.change(150).pipe(Ne(this._destroyed)),o=()=>{this.updatePagination(),this._alignInkBarToSelectedTab()};this._keyManager=new ea(this._items).withHorizontalOrientation(this._getLayoutDirection()).withHomeAndEnd().withWrap().skipPredicate(()=>!1),this._keyManager.updateActiveItem(Math.max(this._selectedIndex,0)),Tt(o,{injector:this._injector}),Dt(e,a,i,this._items.changes,this._itemsResized()).pipe(Ne(this._destroyed)).subscribe(()=>{this._ngZone.run(()=>{Promise.resolve().then(()=>{this._scrollDistance=Math.max(0,Math.min(this._getMaxScrollDistance(),this._scrollDistance)),o()})}),this._keyManager?.withHorizontalOrientation(this._getLayoutDirection())}),this._keyManager.change.subscribe(r=>{this.indexFocused.emit(r),this._setTabFocus(r)})}_itemsResized(){return typeof ResizeObserver!=`function`?Ri:this._items.changes.pipe(ut(this._items),vn(e=>new Ce(i=>this._ngZone.runOutsideAngular(()=>{let a=new ResizeObserver(o=>i.next(o));return e.forEach(o=>a.observe(o.elementRef.nativeElement)),()=>{a.disconnect()}}))),Qa(1),Qe(e=>e.some(i=>i.contentRect.width>0&&i.contentRect.height>0)))}ngAfterContentChecked(){this._tabLabelCount!=this._items.length&&(this.updatePagination(),this._tabLabelCount=this._items.length,this._changeDetectorRef.markForCheck()),this._selectedIndexChanged&&(this._scrollToLabel(this._selectedIndex),this._checkScrollingControls(),this._alignInkBarToSelectedTab(),this._selectedIndexChanged=!1,this._changeDetectorRef.markForCheck()),this._scrollDistanceChanged&&(this._updateTabScrollPosition(),this._scrollDistanceChanged=!1,this._changeDetectorRef.markForCheck())}ngOnDestroy(){this._eventCleanups.forEach(e=>e()),this._keyManager?.destroy(),this._destroyed.next(),this._destroyed.complete(),this._stopScrolling.complete()}_handleKeydown(e){if(!xt(e))switch(e.keyCode){case 13:case 32:if(this.focusIndex!==this.selectedIndex){let i=this._items.get(this.focusIndex);i&&!i.disabled&&(this.selectFocusedIndex.emit(this.focusIndex),this._itemSelected(e))}break;default:this._keyManager?.onKeydown(e)}}_onContentChanges(){let e=this._elementRef.nativeElement.textContent;e!==this._currentTextContent&&(this._currentTextContent=e||``,this._ngZone.run(()=>{this.updatePagination(),this._alignInkBarToSelectedTab(),this._changeDetectorRef.markForCheck()}))}updatePagination(){this._checkPaginationEnabled(),this._checkScrollingControls(),this._updateTabScrollPosition()}get focusIndex(){return this._keyManager?this._keyManager.activeItemIndex:0}set focusIndex(e){!this._isValidIndex(e)||this.focusIndex===e||!this._keyManager||this._keyManager.setActiveItem(e)}_isValidIndex(e){return this._items?!!this._items.toArray()[e]:!0}_setTabFocus(e){if(this._showPaginationControls&&this._scrollToLabel(e),this._items&&this._items.length){this._items.toArray()[e].focus();let i=this._tabListContainer.nativeElement;this._getLayoutDirection()==`ltr`?i.scrollLeft=0:i.scrollLeft=i.scrollWidth-i.offsetWidth}}_getLayoutDirection(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_updateTabScrollPosition(){if(this.disablePagination)return;let e=this.scrollDistance,i=this._getLayoutDirection()===`ltr`?-e:e;this._tabList.nativeElement.style.transform=`translateX(${Math.round(i)}px)`,(this._platform.TRIDENT||this._platform.EDGE)&&(this._tabListContainer.nativeElement.scrollLeft=0)}get scrollDistance(){return this._scrollDistance}set scrollDistance(e){this._scrollTo(e)}_scrollHeader(e){let i=this._tabListContainer.nativeElement.offsetWidth,a=(e==`before`?-1:1)*i/3;return this._scrollTo(this._scrollDistance+a)}_handlePaginatorClick(e){this._stopInterval(),this._scrollHeader(e)}_scrollToLabel(e){if(this.disablePagination)return;let i=this._items?this._items.toArray()[e]:null;if(!i)return;let a=this._tabListContainer.nativeElement.offsetWidth,{offsetLeft:o,offsetWidth:r}=i.elementRef.nativeElement,s,c;this._getLayoutDirection()==`ltr`?(s=o,c=s+r):(c=this._tabListInner.nativeElement.offsetWidth-o,s=c-r);let p=this.scrollDistance,m=this.scrollDistance+a;s<p?this.scrollDistance-=p-s:c>m&&(this.scrollDistance+=Math.min(c-m,s-p))}_checkPaginationEnabled(){if(this.disablePagination)this._showPaginationControls=!1;else{let a=this._tabListInner.nativeElement.scrollWidth-this._elementRef.nativeElement.offsetWidth>=5;a||(this.scrollDistance=0),a!==this._showPaginationControls&&(this._showPaginationControls=a,this._changeDetectorRef.markForCheck())}}_checkScrollingControls(){this.disablePagination?this._disableScrollAfter=this._disableScrollBefore=!0:(this._disableScrollBefore=this.scrollDistance==0,this._disableScrollAfter=this.scrollDistance==this._getMaxScrollDistance(),this._changeDetectorRef.markForCheck())}_getMaxScrollDistance(){return this._tabListInner.nativeElement.scrollWidth-this._tabListContainer.nativeElement.offsetWidth||0}_alignInkBarToSelectedTab(){let e=this._items&&this._items.length?this._items.toArray()[this.selectedIndex]:null,i=e?e.elementRef.nativeElement:null;i?this._inkBar.alignToElement(i):this._inkBar.hide()}_stopInterval(){this._stopScrolling.next()}_handlePaginatorPress(e,i){i&&i.button!=null&&i.button!==0||(this._stopInterval(),vs(lR,dR).pipe(Ne(Dt(this._stopScrolling,this._destroyed))).subscribe(()=>{let{maxScrollDistance:a,distance:o}=this._scrollHeader(e);(o===0||o>=a)&&this._stopInterval()}))}_scrollTo(e){if(this.disablePagination)return{maxScrollDistance:0,distance:0};let i=this._getMaxScrollDistance();return this._scrollDistance=Math.max(0,Math.min(i,e)),this._scrollDistanceChanged=!0,this._checkScrollingControls(),{maxScrollDistance:i,distance:this._scrollDistance}}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,inputs:{disablePagination:[2,`disablePagination`,`disablePagination`,V],selectedIndex:[2,`selectedIndex`,`selectedIndex`,ct]},outputs:{selectFocusedIndex:`selectFocusedIndex`,indexFocused:`indexFocused`}})}return t})();var mR=(()=>{class t extends uR{_items;_tabListContainer;_tabList;_tabListInner;_nextPaginator;_previousPaginator;_inkBar;ariaLabel;ariaLabelledby;disableRipple=!1;ngAfterContentInit(){this._inkBar=new Ib(this._items),super.ngAfterContentInit()}_itemSelected(e){e.preventDefault()}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵcmp=(function(){let e=[`tabListContainer`],i=[`tabList`],a=[`tabListInner`],o=[`nextPaginator`],r=[`previousPaginator`];return B({type:t,selectors:[[`mat-tab-header`]],contentQueries:function(p,m,g){if(p&1&&Bt(g,Ow,4),p&2){let v;Z(v=J())&&(m._items=v)}},viewQuery:function(p,m){if(p&1&&Ze(e,7)(i,7)(a,7)(o,5)(r,5),p&2){let g;Z(g=J())&&(m._tabListContainer=g.first),Z(g=J())&&(m._tabList=g.first),Z(g=J())&&(m._tabListInner=g.first),Z(g=J())&&(m._nextPaginator=g.first),Z(g=J())&&(m._previousPaginator=g.first)}},hostAttrs:[1,`mat-mdc-tab-header`],hostVars:4,hostBindings:function(p,m){p&2&&q(`mat-mdc-tab-header-pagination-controls-enabled`,m._showPaginationControls)(`mat-mdc-tab-header-rtl`,m._getLayoutDirection()==`rtl`)},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],disableRipple:[2,`disableRipple`,`disableRipple`,V]},features:[Ve],ngContentSelectors:[`*`],decls:13,vars:10,consts:[[`previousPaginator`,``],[`tabListContainer`,``],[`tabList`,``],[`tabListInner`,``],[`nextPaginator`,``],[`mat-ripple`,``,1,`mat-mdc-tab-header-pagination`,`mat-mdc-tab-header-pagination-before`,3,`click`,`mousedown`,`touchend`,`matRippleDisabled`],[1,`mat-mdc-tab-header-pagination-chevron`],[1,`mat-mdc-tab-label-container`,3,`keydown`],[`role`,`tablist`,1,`mat-mdc-tab-list`,3,`cdkObserveContent`],[1,`mat-mdc-tab-labels`],[`mat-ripple`,``,1,`mat-mdc-tab-header-pagination`,`mat-mdc-tab-header-pagination-after`,3,`mousedown`,`click`,`touchend`,`matRippleDisabled`]],template:function(p,m){p&1&&(Re(),l(0,`div`,5,0),C(`click`,function(){return m._handlePaginatorClick(`before`)})(`mousedown`,function(v){return m._handlePaginatorPress(`before`,v)})(`touchend`,function(){return m._stopInterval()}),z(2,`div`,6),d(),l(3,`div`,7,1),C(`keydown`,function(v){return m._handleKeydown(v)}),l(5,`div`,8,2),C(`cdkObserveContent`,function(){return m._onContentChanges()}),l(7,`div`,9,3),me(9),d()()(),l(10,`div`,10,4),C(`mousedown`,function(v){return m._handlePaginatorPress(`after`,v)})(`click`,function(){return m._handlePaginatorClick(`after`)})(`touchend`,function(){return m._stopInterval()}),z(12,`div`,6),d()),p&2&&(q(`mat-mdc-tab-header-pagination-disabled`,m._disableScrollBefore),S(`matRippleDisabled`,m._disableScrollBefore||m.disableRipple),h(3),q(`_mat-animation-noopable`,m._animationsDisabled),h(2),te(`aria-label`,m.ariaLabel||null)(`aria-labelledby`,m.ariaLabelledby||null),h(5),q(`mat-mdc-tab-header-pagination-disabled`,m._disableScrollAfter),S(`matRippleDisabled`,m._disableScrollAfter||m.disableRipple))},dependencies:[li,RC],styles:[`.mat-mdc-tab-header {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var pR=new I(`MAT_TABS_CONFIG`);var Nw=(()=>{class t extends Aa{_host=f(Mb);_ngZone=f(Q);_centeringSub=De.EMPTY;_leavingSub=De.EMPTY;ngOnInit(){super.ngOnInit(),this._centeringSub=this._host._beforeCentering.pipe(ut(this._host._isCenterPosition())).subscribe(e=>{this._host._content&&e&&!this.hasAttached()&&this._ngZone.run(()=>{Promise.resolve().then(),this.attach(this._host._content)})}),this._leavingSub=this._host._afterLeavingCenter.subscribe(()=>{this._host.preserveContent||this._ngZone.run(()=>this.detach())})}ngOnDestroy(){super.ngOnDestroy(),this._centeringSub.unsubscribe(),this._leavingSub.unsubscribe()}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=Y({type:t,selectors:[[``,`matTabBodyHost`,``]],features:[Ve]})}return t})();var Mb=(()=>{class t{_elementRef=f(W);_dir=f(ft,{optional:!0});_ngZone=f(Q);_injector=f(re);_renderer=f(Oe);_diAnimationsDisabled=Ue();_eventCleanups;_initialized=!1;_fallbackTimer;_positionIndex;_dirChangeSubscription=De.EMPTY;_position;_previousPosition;_onCentering=new ee;_beforeCentering=new ee;_afterLeavingCenter=new ee;_onCentered=new ee(!0);_portalHost;_contentElement;_content;animationDuration=`500ms`;preserveContent=!1;set position(e){this._positionIndex=e,this._computePositionAnimationState()}constructor(){if(this._dir){let e=f(Pe);this._dirChangeSubscription=this._dir.change.subscribe(i=>{this._computePositionAnimationState(i),e.markForCheck()})}}ngOnInit(){this._bindTransitionEvents(),this._position===`center`&&(this._setActiveClass(!0),Tt(()=>this._onCentering.emit(this._elementRef.nativeElement.clientHeight),{injector:this._injector})),this._initialized=!0}ngOnDestroy(){clearTimeout(this._fallbackTimer),this._eventCleanups?.forEach(e=>e()),this._dirChangeSubscription.unsubscribe()}_bindTransitionEvents(){this._ngZone.runOutsideAngular(()=>{let e=this._elementRef.nativeElement,i=a=>{a.target===this._contentElement?.nativeElement&&(this._elementRef.nativeElement.classList.remove(`mat-tab-body-animating`),a.type===`transitionend`&&this._transitionDone())};this._eventCleanups=[this._renderer.listen(e,`transitionstart`,a=>{a.target===this._contentElement?.nativeElement&&(this._elementRef.nativeElement.classList.add(`mat-tab-body-animating`),this._transitionStarted())}),this._renderer.listen(e,`transitionend`,i),this._renderer.listen(e,`transitioncancel`,i)]})}_transitionStarted(){clearTimeout(this._fallbackTimer);let e=this._position===`center`;this._beforeCentering.emit(e),e&&this._onCentering.emit(this._elementRef.nativeElement.clientHeight)}_transitionDone(){this._position===`center`?this._onCentered.emit():this._previousPosition===`center`&&this._afterLeavingCenter.emit()}_setActiveClass(e){this._elementRef.nativeElement.classList.toggle(`mat-mdc-tab-body-active`,e)}_getLayoutDirection(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_isCenterPosition(){return this._positionIndex===0}_computePositionAnimationState(e=this._getLayoutDirection()){this._previousPosition=this._position,this._positionIndex<0?this._position=e==`ltr`?`left`:`right`:this._positionIndex>0?this._position=e==`ltr`?`right`:`left`:this._position=`center`,this._animationsDisabled()?this._simulateTransitionEvents():this._initialized&&(this._position===`center`||this._previousPosition===`center`)&&(clearTimeout(this._fallbackTimer),this._fallbackTimer=this._ngZone.runOutsideAngular(()=>setTimeout(()=>this._simulateTransitionEvents(),100)))}_simulateTransitionEvents(){this._transitionStarted(),Tt(()=>this._transitionDone(),{injector:this._injector})}_animationsDisabled(){return this._diAnimationsDisabled||this.animationDuration===`0ms`||this.animationDuration===`0s`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`content`];function i(a,o){}return B({type:t,selectors:[[`mat-tab-body`]],viewQuery:function(o,r){if(o&1&&Ze(Nw,5)(e,5),o&2){let s;Z(s=J())&&(r._portalHost=s.first),Z(s=J())&&(r._contentElement=s.first)}},hostAttrs:[1,`mat-mdc-tab-body`],hostVars:1,hostBindings:function(o,r){o&2&&te(`inert`,r._position===`center`?null:``)},inputs:{_content:[0,`content`,`_content`],animationDuration:`animationDuration`,preserveContent:`preserveContent`,position:`position`},outputs:{_onCentering:`_onCentering`,_beforeCentering:`_beforeCentering`,_onCentered:`_onCentered`},decls:3,vars:6,consts:[[`content`,``],[`cdkScrollable`,``,1,`mat-mdc-tab-body-content`],[`matTabBodyHost`,``]],template:function(o,r){o&1&&(l(0,`div`,1,0),ot(2,i,0,0,`ng-template`,2),d()),o&2&&q(`mat-tab-body-content-left`,r._position===`left`)(`mat-tab-body-content-right`,r._position===`right`)(`mat-tab-body-content-can-animate`,r._position===`center`||r._previousPosition===`center`)},dependencies:[Nw,Uc],styles:[`.mat-mdc-tab-body {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var pm=(()=>{class t{_elementRef=f(W);_changeDetectorRef=f(Pe);_ngZone=f(Q);_tabsSubscription=De.EMPTY;_tabLabelSubscription=De.EMPTY;_tabBodySubscription=De.EMPTY;_diAnimationsDisabled=Ue();_bodyAnimationDuration;_headerAnimationDuration;_allTabs;_tabBodies;_tabBodyWrapper;_tabHeader;_tabs=new Sn;_indexToSelect=0;_lastFocusedTabIndex=null;_tabBodyWrapperHeight=0;color;get fitInkBarToContent(){return this._fitInkBarToContent}set fitInkBarToContent(e){this._fitInkBarToContent=e,this._changeDetectorRef.markForCheck()}_fitInkBarToContent=!1;stretchTabs=!0;alignTabs=null;dynamicHeight=!1;get selectedIndex(){return this._selectedIndex}set selectedIndex(e){this._indexToSelect=isNaN(e)?null:e}_selectedIndex=null;headerPosition=`above`;get animationDuration(){return this._animationDuration}set animationDuration(e){this._animationDuration=e,e&&typeof e==`object`?(this._bodyAnimationDuration=kb(e.body),this._headerAnimationDuration=kb(e.header)):this._headerAnimationDuration=this._bodyAnimationDuration=kb(e)}_animationDuration;get contentTabIndex(){return this._contentTabIndex}set contentTabIndex(e){this._contentTabIndex=isNaN(e)?null:e}_contentTabIndex=null;disablePagination=!1;disableRipple=!1;preserveContent=!1;get backgroundColor(){return this._backgroundColor}set backgroundColor(e){let i=this._elementRef.nativeElement.classList;i.remove(`mat-tabs-with-background`,`mat-background-${this.backgroundColor}`),e&&i.add(`mat-tabs-with-background`,`mat-background-${e}`),this._backgroundColor=e}_backgroundColor;ariaLabel;ariaLabelledby;selectedIndexChange=new ee;focusChange=new ee;animationDone=new ee;selectedTabChange=new ee(!0);_groupId;_isServer=!f(Ae).isBrowser;constructor(){let e=f(pR,{optional:!0});this._groupId=f(Be).getId(`mat-tab-group-`),this.animationDuration=e&&e.animationDuration?e.animationDuration:`500ms`,this.disablePagination=e&&e.disablePagination!=null?e.disablePagination:!1,this.dynamicHeight=e&&e.dynamicHeight!=null?e.dynamicHeight:!1,e?.contentTabIndex!=null&&(this.contentTabIndex=e.contentTabIndex),this.preserveContent=!!e?.preserveContent,this.fitInkBarToContent=e&&e.fitInkBarToContent!=null?e.fitInkBarToContent:!1,this.stretchTabs=e&&e.stretchTabs!=null?e.stretchTabs:!0,this.alignTabs=e&&e.alignTabs!=null?e.alignTabs:null}ngAfterContentChecked(){let e=this._indexToSelect=this._clampTabIndex(this._indexToSelect);if(this._selectedIndex!=e){let i=this._selectedIndex==null;if(!i){this.selectedTabChange.emit(this._createChangeEvent(e));let a=this._tabBodyWrapper.nativeElement;a.style.minHeight=a.clientHeight+`px`}Promise.resolve().then(()=>{this._tabs.forEach((a,o)=>a.isActive=o===e),i||(this.selectedIndexChange.emit(e),this._tabBodyWrapper.nativeElement.style.minHeight=``)})}this._tabs.forEach((i,a)=>{i.position=a-e,this._selectedIndex!=null&&i.position==0&&!i.origin&&(i.origin=e-this._selectedIndex)}),this._selectedIndex!==e&&(this._selectedIndex=e,this._lastFocusedTabIndex=null,this._changeDetectorRef.markForCheck())}ngAfterContentInit(){this._subscribeToAllTabChanges(),this._subscribeToTabLabels(),this._tabsSubscription=this._tabs.changes.subscribe(()=>{let e=this._clampTabIndex(this._indexToSelect);if(e===this._selectedIndex){let i=this._tabs.toArray(),a;for(let o=0;o<i.length;o++)if(i[o].isActive){this._indexToSelect=this._selectedIndex=o,this._lastFocusedTabIndex=null,a=i[o];break}!a&&i[e]&&Promise.resolve().then(()=>{i[e].isActive=!0,this.selectedTabChange.emit(this._createChangeEvent(e))})}this._changeDetectorRef.markForCheck()})}ngAfterViewInit(){this._tabBodySubscription=this._tabBodies.changes.subscribe(()=>this._bodyCentered(!0))}_subscribeToAllTabChanges(){this._allTabs.changes.pipe(ut(this._allTabs)).subscribe(e=>{this._tabs.reset(e.filter(i=>i._closestTabGroup===this||!i._closestTabGroup)),this._tabs.notifyOnChanges()})}ngOnDestroy(){this._tabs.destroy(),this._tabsSubscription.unsubscribe(),this._tabLabelSubscription.unsubscribe(),this._tabBodySubscription.unsubscribe()}realignInkBar(){this._tabHeader&&this._tabHeader._alignInkBarToSelectedTab()}updatePagination(){this._tabHeader&&this._tabHeader.updatePagination()}focusTab(e){let i=this._tabHeader;i&&(i.focusIndex=e)}_focusChanged(e){this._lastFocusedTabIndex=e,this.focusChange.emit(this._createChangeEvent(e))}_createChangeEvent(e){let i=new Nb;return i.index=e,this._tabs&&this._tabs.length&&(i.tab=this._tabs.toArray()[e]),i}_subscribeToTabLabels(){this._tabLabelSubscription&&this._tabLabelSubscription.unsubscribe(),this._tabLabelSubscription=Dt(...this._tabs.map(e=>e._stateChanges)).subscribe(()=>this._changeDetectorRef.markForCheck())}_clampTabIndex(e){return Math.min(this._tabs.length-1,Math.max(e||0,0))}_getTabLabelId(e,i){return e.id||`${this._groupId}-label-${i}`}_getTabContentId(e){return`${this._groupId}-content-${e}`}_setTabBodyWrapperHeight(e){if(!this.dynamicHeight||!this._tabBodyWrapperHeight){this._tabBodyWrapperHeight=e;return}let i=this._tabBodyWrapper.nativeElement;i.style.height=this._tabBodyWrapperHeight+`px`,this._tabBodyWrapper.nativeElement.offsetHeight&&(i.style.height=e+`px`)}_removeTabBodyWrapperHeight(){let e=this._tabBodyWrapper.nativeElement;this._tabBodyWrapperHeight=e.clientHeight,e.style.height=``,this._ngZone.run(()=>this.animationDone.emit())}_handleClick(e,i,a){i.focusIndex=a,e.disabled||(this.selectedIndex=a)}_getTabIndex(e){return e===(this._lastFocusedTabIndex??this.selectedIndex)?0:-1}_tabFocusChanged(e,i){e&&e!==`mouse`&&e!==`touch`&&(this._tabHeader.focusIndex=i)}_bodyCentered(e){e&&this._tabBodies?.forEach((i,a)=>i._setActiveClass(a===this._selectedIndex))}_bodyAnimationsDisabled(){return this._diAnimationsDisabled||this._bodyAnimationDuration===`0`||this._bodyAnimationDuration===`0ms`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`tabBodyWrapper`],i=[`tabHeader`],a=[`*`];function o(g,v){}function r(g,v){if(g&1&&ot(0,o,0,0,`ng-template`,12),g&2){let b=y().$implicit;S(`cdkPortalOutlet`,b.templateLabel)}}function s(g,v){if(g&1&&u(0),g&2){let b=y().$implicit;w(b.textLabel)}}function c(g,v){if(g&1){let b=X();l(0,`div`,7,2),C(`click`,function(){let E=A(b),P=E.$implicit,ae=E.$index,j=y(),x=ht(1);return O(j._handleClick(P,x,ae))})(`cdkFocusChange`,function(E){let P=A(b).$index;return O(y()._tabFocusChanged(E,P))}),z(2,`span`,8)(3,`div`,9),l(4,`span`,10)(5,`span`,11),D(6,r,1,1,null,12)(7,s,1,1),d()()()}if(g&2){let b=v.$implicit,_=v.$index,E=ht(1),P=y();tt(b.labelClass),q(`mdc-tab--active`,P.selectedIndex===_),S(`id`,P._getTabLabelId(b,_))(`disabled`,b.disabled)(`fitInkBarToContent`,P.fitInkBarToContent),te(`tabIndex`,P._getTabIndex(_))(`aria-posinset`,_+1)(`aria-setsize`,P._tabs.length)(`aria-controls`,P._getTabContentId(_))(`aria-selected`,P.selectedIndex===_)(`aria-label`,b.ariaLabel||null)(`aria-labelledby`,!b.ariaLabel&&b.ariaLabelledby?b.ariaLabelledby:null),h(3),S(`matRippleTrigger`,E)(`matRippleDisabled`,b.disabled||P.disableRipple),h(3),k(b.templateLabel?6:7)}}function p(g,v){g&1&&me(0)}function m(g,v){if(g&1){let b=X();l(0,`mat-tab-body`,13),C(`_onCentered`,function(){A(b);return O(y()._removeTabBodyWrapperHeight())})(`_onCentering`,function(E){A(b);return O(y()._setTabBodyWrapperHeight(E))})(`_beforeCentering`,function(E){A(b);return O(y()._bodyCentered(E))}),d()}if(g&2){let b=v.$implicit,_=v.$index,E=y();tt(b.bodyClass),S(`id`,E._getTabContentId(_))(`content`,b.content)(`position`,b.position)(`animationDuration`,E._bodyAnimationDuration)(`preserveContent`,E.preserveContent),te(`tabindex`,E.contentTabIndex!=null&&E.selectedIndex===_?E.contentTabIndex:null)(`aria-labelledby`,E._getTabLabelId(b,_))(`aria-hidden`,E.selectedIndex!==_)}}return B({type:t,selectors:[[`mat-tab-group`]],contentQueries:function(v,b,_){if(v&1&&Bt(_,Yc,5),v&2){let E;Z(E=J())&&(b._allTabs=E)}},viewQuery:function(v,b){if(v&1&&Ze(e,5)(i,5)(Mb,5),v&2){let _;Z(_=J())&&(b._tabBodyWrapper=_.first),Z(_=J())&&(b._tabHeader=_.first),Z(_=J())&&(b._tabBodies=_)}},hostAttrs:[1,`mat-mdc-tab-group`],hostVars:13,hostBindings:function(v,b){v&2&&(te(`mat-align-tabs`,b.alignTabs),tt(`mat-`+(b.color||`primary`)),fn(`--%NS%mat-tab-body-animation-duration`,b._bodyAnimationDuration)(`--%NS%mat-tab-header-animation-duration`,b._headerAnimationDuration),q(`mat-mdc-tab-group-dynamic-height`,b.dynamicHeight)(`mat-mdc-tab-group-inverted-header`,b.headerPosition===`below`)(`mat-mdc-tab-group-stretch-tabs`,b.stretchTabs))},inputs:{color:`color`,fitInkBarToContent:[2,`fitInkBarToContent`,`fitInkBarToContent`,V],stretchTabs:[2,`mat-stretch-tabs`,`stretchTabs`,V],alignTabs:[0,`mat-align-tabs`,`alignTabs`],dynamicHeight:[2,`dynamicHeight`,`dynamicHeight`,V],selectedIndex:[2,`selectedIndex`,`selectedIndex`,ct],headerPosition:`headerPosition`,animationDuration:`animationDuration`,contentTabIndex:[2,`contentTabIndex`,`contentTabIndex`,ct],disablePagination:[2,`disablePagination`,`disablePagination`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V],preserveContent:[2,`preserveContent`,`preserveContent`,V],backgroundColor:`backgroundColor`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`]},outputs:{selectedIndexChange:`selectedIndexChange`,focusChange:`focusChange`,animationDone:`animationDone`,selectedTabChange:`selectedTabChange`},exportAs:[`matTabGroup`],features:[Ge([{provide:Aw,useExisting:t}])],ngContentSelectors:a,decls:9,vars:8,consts:[[`tabHeader`,``],[`tabBodyWrapper`,``],[`tabNode`,``],[3,`indexFocused`,`selectFocusedIndex`,`selectedIndex`,`disableRipple`,`disablePagination`,`aria-label`,`aria-labelledby`],[`role`,`tab`,`matTabLabelWrapper`,``,`cdkMonitorElementFocus`,``,1,`mdc-tab`,`mat-mdc-tab`,`mat-focus-indicator`,3,`id`,`mdc-tab--active`,`class`,`disabled`,`fitInkBarToContent`],[1,`mat-mdc-tab-body-wrapper`],[`role`,`tabpanel`,3,`id`,`class`,`content`,`position`,`animationDuration`,`preserveContent`],[`role`,`tab`,`matTabLabelWrapper`,``,`cdkMonitorElementFocus`,``,1,`mdc-tab`,`mat-mdc-tab`,`mat-focus-indicator`,3,`click`,`cdkFocusChange`,`id`,`disabled`,`fitInkBarToContent`],[1,`mdc-tab__ripple`],[`mat-ripple`,``,1,`mat-mdc-tab-ripple`,3,`matRippleTrigger`,`matRippleDisabled`],[1,`mdc-tab__content`],[1,`mdc-tab__text-label`],[3,`cdkPortalOutlet`],[`role`,`tabpanel`,3,`_onCentered`,`_onCentering`,`_beforeCentering`,`id`,`content`,`position`,`animationDuration`,`preserveContent`]],template:function(v,b){v&1&&(Re(),l(0,`mat-tab-header`,3,0),C(`indexFocused`,function(E){return b._focusChanged(E)})(`selectFocusedIndex`,function(E){return b.selectedIndex=E}),be(2,c,8,17,`div`,4,Pn),d(),D(4,p,1,0),l(5,`div`,5,1),be(7,m,1,10,`mat-tab-body`,6,Pn),d()),v&2&&(S(`selectedIndex`,b.selectedIndex||0)(`disableRipple`,b.disableRipple)(`disablePagination`,b.disablePagination),kr(`aria-label`,b.ariaLabel)(`aria-labelledby`,b.ariaLabelledby),h(2),ve(b._tabs),h(2),k(b._isServer?4:-1),h(),q(`_mat-animation-noopable`,b._bodyAnimationsDisabled()),h(2),ve(b._tabs))},dependencies:[mR,Ow,Ag,li,Aa,Mb],styles:[`.mdc-tab {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var Nb=class{index;tab};var hm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[we]})}return t})();var hR=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[`mat-toolbar-row`]],hostAttrs:[1,`mat-toolbar-row`],exportAs:[`matToolbarRow`]})}return t})();var fm=(()=>{class t{_elementRef=f(W);_platform=f(Ae);_document=f(de);color;_toolbarRows;ngAfterViewInit(){this._platform.isBrowser&&(this._checkToolbarMixedModes(),this._toolbarRows.changes.subscribe(()=>this._checkToolbarMixedModes()))}_checkToolbarMixedModes(){this._toolbarRows.length}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[`mat-toolbar-row`]]];return B({type:t,selectors:[[`mat-toolbar`]],contentQueries:function(o,r,s){if(o&1&&Bt(s,hR,5),o&2){let c;Z(c=J())&&(r._toolbarRows=c)}},hostAttrs:[1,`mat-toolbar`],hostVars:6,hostBindings:function(o,r){o&2&&(tt(r.color?`mat-`+r.color:``),q(`mat-toolbar-multiple-rows`,r._toolbarRows.length>0)(`mat-toolbar-single-row`,r._toolbarRows.length===0))},inputs:{color:`color`},exportAs:[`matToolbar`],ngContentSelectors:[`*`,`mat-toolbar-row`],decls:2,vars:0,template:function(o,r){o&1&&(Re(e),me(0),me(1,1))},styles:[`.mat-toolbar {
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
`],encapsulation:2})})()}return t})();var gm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[we]})}return t})();function Dn(t){t||(t=f(Te));let n=new Ce(e=>{if(t.destroyed){e.next();return}return t.onDestroy(e.next.bind(e))});return e=>e.pipe(Ne(n))}var Vw=v(Lw());var kn={apiBaseUrl:``,websocketUrl:`${window.location.protocol===`https:`?`wss:`:`ws:`}//${window.location.host}/websocket`,websocketDebug:!1};var ss=class t{http=f(Di);destroyRef=f(Te);envelopeSubject=new L;connectedSubject=new L;disconnectedSubject=new L;errorSubject=new L;stateSubject=new fi(`connecting`);pending=new Map;client=null;subscription=null;retryTimer=null;connected=!1;synchronization=null;again=!1;generation=0;delay=500;logId=null;cursor=-1;envelopes$=this.envelopeSubject.asObservable();connected$=this.connectedSubject.asObservable();disconnected$=this.disconnectedSubject.asObservable();errors$=this.errorSubject.asObservable();state$=this.stateSubject.asObservable();get lastEventId(){return this.cursor}get eventLogId(){return this.logId}constructor(){this.destroyRef.onDestroy(()=>{this.stop()})}start(){if(this.client)return;let n=new Vw.Client({brokerURL:kn.websocketUrl,heartbeatIncoming:4e3,heartbeatOutgoing:4e3,reconnectDelay:1e3,debug:kn.websocketDebug?e=>console.debug(`[STOMP] `+e):()=>{}});n.onConnect=()=>{this.subscription?.unsubscribe(),this.subscription=n.subscribe(`/topic/events`,e=>this.receiveMessage(e)),this.connected=!0,this.generation++,this.connectedSubject.next(),this.retryNow()},n.onWebSocketClose=()=>{this.subscription=null,this.connected=!1,this.generation++,this.stateSubject.next(`disconnected`),this.disconnectedSubject.next(),this.clearTimer()},n.onWebSocketError=e=>this.fail(e),n.onStompError=e=>this.fail(e),this.client=n,n.activate()}async stop(){this.connected=!1,this.generation++,this.clearTimer(),this.pending.clear(),this.subscription?.unsubscribe(),this.subscription=null;let n=this.client;this.client=null,n&&await n.deactivate()}retryNow(){this.clearTimer(),this.delay=500,this.requestSync()}getHead(){return Rt(this.http.get(kn.apiBaseUrl+`/lastevent`).pipe(Dn(this.destroyRef)))}getEvents(n,e,i){return Rt(this.http.get(kn.apiBaseUrl+`/events`,{params:{eventLogId:n,from:e,to:i}}).pipe(Dn(this.destroyRef)))}receiveMessage(n){try{let e=JSON.parse(n.body);if(this.validate(e),e.eventLogId===this.logId&&e.eventId<=this.cursor)return;this.pending.size>=5e3&&(this.pending.clear(),this.requestSync()),this.pending.set(this.key(e.eventLogId,e.eventId),e),this.stateSubject.value===`live`&&e.eventLogId===this.logId&&this.drain(),(e.eventLogId!==this.logId||this.hasGap())&&this.requestSync()}catch(e){this.fail(e)}}requestSync(){if(!this.connected)return;if(this.synchronization){this.again=!0;return}this.again=!1;let n=this.generation;this.synchronization=this.synchronize(n).catch(e=>{n===this.generation&&this.connected&&this.fail(e)}).finally(()=>{this.synchronization=null,this.again&&this.connected&&this.requestSync()})}async synchronize(n){this.stateSubject.next(`syncing`);let e=await this.getHead();if(this.current(n)){if(typeof e.eventLogId!=`string`||!Number.isInteger(e.lastEventId)||e.lastEventId<-1)throw new Error(`Invalid event head`);if(this.logId!==e.eventLogId){this.logId=e.eventLogId,this.cursor=e.lastEventId;for(let[i,a]of this.pending)(a.eventLogId!==this.logId||a.eventId<=this.cursor)&&this.pending.delete(i)}else{if(e.lastEventId<this.cursor)throw new Error(`Cursor moved backwards without a new log identity`);await this.download(this.cursor+1,e.lastEventId+1,n)}if(this.current(n)){this.drain();for(let i=0;this.hasGap();i++){if(i===10)throw new Error(`Live delivery has not caught up`);let a=this.smallestPending();if(a===null)break;if(await this.download(this.cursor+1,a,n),!this.current(n))return;this.drain()}if(this.cursor<e.lastEventId)throw new Error(`Incomplete event history`);this.current(n)&&(this.delay=500,this.clearTimer(),this.stateSubject.next(`live`))}}}async download(n,e,i){for(let a=n;a<e;){let o=Math.min(e,a+1e3),r=this.logId,s=await this.getEvents(r,a,o);if(!this.current(i))return;if(s.eventLogId!==r||s.from!==a||s.to!==o||s.events.length!==o-a)throw new Error(`Incomplete or obsolete history`);let c=a;for(let p of s.events){if(this.validate(p),p.eventLogId!==r||p.eventId!==c++)throw new Error(`Non-contiguous history`);p.eventId>this.cursor&&this.pending.set(this.key(r,p.eventId),p)}this.drain(),a=o}}drain(){if(this.logId)for(let n=this.pending.get(this.key(this.logId,this.cursor+1));n;n=this.pending.get(this.key(this.logId,this.cursor+1)))this.pending.delete(this.key(this.logId,n.eventId)),this.cursor=n.eventId,this.envelopeSubject.next(n)}smallestPending(){let n=null;for(let e of this.pending.values())e.eventLogId===this.logId&&e.eventId>this.cursor&&(n===null||e.eventId<n)&&(n=e.eventId);return n}hasGap(){let n=this.smallestPending();return n!==null&&n>this.cursor+1}current(n){return this.connected&&n===this.generation}key(n,e){return n+`:`+e}clearTimer(){this.retryTimer&&clearTimeout(this.retryTimer),this.retryTimer=null}fail(n){this.errorSubject.next(n),this.stateSubject.next(`retrying`),this.connected&&!this.retryTimer&&(this.retryTimer=setTimeout(()=>{this.retryTimer=null,this.requestSync()},this.delay),this.delay=Math.min(3e4,this.delay*2))}validate(n){if(!n||typeof n.eventLogId!=`string`||!Number.isInteger(n.eventId)||n.eventId<0||typeof n.type!=`string`||!Number.isFinite(n.timestamp)||typeof n.workerName!=`string`||!n.payload||typeof n.payload!=`object`)throw new Error(`Invalid envelope`);let e=n.payload;if(n.type===`SolutionGeneratedEvent`&&(typeof e.resultId!=`string`||typeof e.success!=`boolean`||typeof e.iteration!=`string`||!e.objectives||typeof e.objectives!=`object`))throw new Error(`Invalid result event`);if(n.type===`ExecutionStartedEvent`&&(!e.objectives||typeof e.objectives!=`object`||!Array.isArray(e.experimentNames)))throw new Error(`Invalid execution event`);if(n.type===`ErrorEvent`&&typeof e.exceptionType!=`string`)throw new Error(`Invalid error event`)}static ɵfac=function(e){return new(e||t)};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})};var Fa=class t{http=f(Di);destroyRef=f(Te);get(n,e={}){return Rt(this.http.get(`${kn.apiBaseUrl}${n}`,{params:e}).pipe(Dn(this.destroyRef)))}getMode(){return this.get(`/api/monitor/status`)}getStatus(){return this.get(`/api/experiments/status`)}getInstances(n,e){return this.get(`/api/experiments/instances`,r({runId:n},e))}getInstance(n,e,i){return this.get(`/api/experiments/instances/${encodeURIComponent(e)}`,{runId:n,objective:i})}getResults(n,e){let i=s(r({runId:n},e),{order:e.order??`PLANNED`});return e.state||delete i.state,this.get(`/api/experiments/results`,i)}getResult(n,e){return this.get(`/api/experiments/results/${encodeURIComponent(e)}`,{runId:n})}getIssues(n,e,i){return this.get(`/api/experiments/issues`,{runId:n,severity:e,offset:i,limit:25})}getArtifacts(n){return this.get(`/api/experiments/artifacts`,{runId:n})}artifactUrl(n,e){return`${kn.apiBaseUrl}/api/experiments/artifacts/${encodeURIComponent(e)}?${new URLSearchParams({runId:n})}`}exportUrl(n,e,i){let a=new URLSearchParams({runId:n,format:i,instanceId:e.instanceId,algorithm:e.algorithm,search:e.search,order:e.order??`PLANNED`});return e.state&&a.set(`state`,e.state),`${kn.apiBaseUrl}/api/experiments/results/export?${a}`}static ɵfac=function(e){return new(e||t)};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})};var La=class{static buildInstance(n,e,i,a=!0){let o=this.referenceValue(n.started.refValues,e);return{key:n.key,experimentName:n.started.experimentName,instanceName:n.started.instanceName,selectedObjective:e,referenceValue:o,scoreChart:this.buildScoreChart(n,e,o,a),convergenceChart:this.buildConvergenceChart(n,e,i,o,a),bestSolution:this.findBest(n.solutions,e,i)}}static buildScoreChart(n,e,i,a){let o=this.groupSuccessfulPoints(n,e,!1,null);return{options:this.chartOptions(`${n.started.instanceName} scores`,e,i,o,`Raw score for every successful repetition.`,a)}}static buildConvergenceChart(n,e,i,a,o){let r=this.groupSuccessfulPoints(n,e,!0,i);return{options:this.chartOptions(`${n.started.instanceName} best score across repetitions`,e,a,r,`Best score reached by each algorithm over completed repetitions.`,o)}}static groupSuccessfulPoints(n,e,i,a){if(!e)return[];let o=[...n.started.algorithms];for(let s of n.solutions)o.includes(s.event.algorithmName)||o.push(s.event.algorithmName);let r=[];for(let s of o){let c=[],p=null;for(let m of n.solutions){let g=m.event;if(g.algorithmName!==s)continue;let v=g.success?this.objectiveValue(g,e):null;if(v===null){c.push({x:m.ordinal,y:null,custom:{iteration:g.iteration}});continue}i&&(p=p===null||this.isBetter(v,p,a)?v:p),c.push({x:m.ordinal,y:i?p:v,custom:{iteration:g.iteration}})}r.push({type:`line`,name:s,data:c,lineWidth:i?2:1,marker:{enabled:!i},animation:!1,turboThreshold:0})}return r}static referenceValue(n,e){let i=e?n[e]:void 0;return typeof i==`number`&&Number.isFinite(i)?i:null}static scoreAxis(n,e,i){let a=i?e:null,o=[];return a!==null&&o.push({id:`reference`,className:`reference-line`,value:a,color:`#d32f2f`,dashStyle:`Dash`,width:2,zIndex:4,label:{text:`Reference: ${a}`}}),{title:{text:n??`Score`},plotLines:o,softMin:a??void 0,softMax:a??void 0}}static chartOptions(n,e,i,a,o,r){return{accessibility:{enabled:!0,description:o},boost:{enabled:!0,useGPUTranslations:!0,seriesThreshold:25},chart:{animation:!1,height:320,spacing:[12,12,12,12]},credits:{enabled:!1},exporting:{enabled:!0},legend:{enabled:!0},title:{text:n,style:{display:`none`}},subtitle:{text:e?`Objective: ${e}`:`Waiting for objective metadata`},xAxis:{allowDecimals:!1,min:1,title:{text:`Repetition`}},yAxis:this.scoreAxis(e,i,r),tooltip:{headerFormat:``,pointFormat:`<b>{series.name}</b><br/>Repetition: {point.x}<br/>Iteration: {point.custom.iteration}<br/>Value: {point.y}`},plotOptions:{series:{animation:!1,connectNulls:!1}},series:a}}static findBest(n,e,i){if(!e||!i)return null;let a=null,o=null;for(let r of n){if(!r.event.success)continue;let s=this.objectiveValue(r.event,e);s!==null&&(o===null||this.isBetter(s,o,i))&&(o=s,a=r.event)}return a}static objectiveValue(n,e){let i=n.objectives[e];return typeof i==`number`&&Number.isFinite(i)?i:null}static isBetter(n,e,i){return i===`MAXIMIZE`?n>e:n<e}};var cs=class{static instance(n,e,i=!0){return La.buildInstance({key:n.instance.id,started:{experimentName:n.instance.experimentName,instanceName:n.instance.instanceName,algorithms:n.algorithms.map(a=>a.name),repetitions:n.instance.counts.planned,refValues:n.instance.references},solutions:n.results.map(a=>({ordinal:a.ordinal,event:{resultId:a.resultId,success:a.state===`SUCCEEDED`,experimentName:a.experimentName,instanceName:a.instanceName,algorithmName:a.algorithmName,iteration:a.iteration,objectives:a.objectives,score:a.objectives[n.objective]??NaN,executionTime:a.executionTime??-1,timeToBest:a.timeToBest??-1}}))},n.objective,e,i)}static distribution(n,e=!0){return{options:{chart:{type:`boxplot`,height:320,animation:!1},plotOptions:{series:{animation:!1}},title:{text:`Score distribution`},credits:{enabled:!1},exporting:{enabled:!0},accessibility:{enabled:!0},xAxis:{categories:n.algorithms.map(i=>i.name)},yAxis:La.scoreAxis(n.objective,La.referenceValue(n.instance.references,n.objective),e),series:[{type:`boxplot`,name:n.objective,data:n.algorithms.flatMap((i,a)=>{let o=i.objective;return o.best===null||o.worst===null?[]:[{x:a,low:Math.min(o.best,o.worst),q1:o.lowerQuartile,median:o.median,q3:o.upperQuartile,high:Math.max(o.best,o.worst)}]})}]}}}static qualityRuntime(n,e=!0){return{options:{chart:{type:`scatter`,height:320,animation:!1},plotOptions:{series:{animation:!1}},title:{text:`Score versus runtime`},credits:{enabled:!1},exporting:{enabled:!0},accessibility:{enabled:!0},xAxis:{title:{text:`Runtime (ms)`}},yAxis:La.scoreAxis(n.objective,La.referenceValue(n.instance.references,n.objective),e),tooltip:{pointFormat:`Runtime: {point.x} ms<br/>Score: {point.y}<br/>Iteration: {point.custom.iteration}`},series:n.algorithms.map(i=>({type:`scatter`,name:i.name,data:n.results.filter(a=>a.algorithmName===i.name&&a.state===`SUCCEEDED`&&a.executionTime!==null&&Number.isFinite(a.objectives[n.objective])).map(a=>({x:a.executionTime/1e6,y:a.objectives[n.objective],custom:{iteration:a.iteration}}))}))}}}};var Kc=class t{client=f(Fa);events=f(ss);destroyRef=f(Te);statusState=N(null);status=this.statusState.asReadonly();syncStateState=N(`connecting`);syncState=this.syncStateState.asReadonly();transportStateState=N(`connecting`);transportState=this.transportStateState.asReadonly();latestErrorState=N(null);latestError=this.latestErrorState.asReadonly();transportErrorState=N(null);transportError=this.transportErrorState.asReadonly();lastUpdatedState=N(null);lastUpdated=this.lastUpdatedState.asReadonly();selectedObjectiveState=N(null);selectedObjective=this.selectedObjectiveState.asReadonly();showReferenceState=N(!0);showReference=this.showReferenceState.asReadonly();selectedInstanceIdState=N(null);selectedInstanceId=this.selectedInstanceIdState.asReadonly();instancePageState=N(null);instancePage=this.instancePageState.asReadonly();resultPageState=N(null);resultPage=this.resultPageState.asReadonly();issuePageState=N(null);issuePage=this.issuePageState.asReadonly();artifactsState=N(null);artifacts=this.artifactsState.asReadonly();detailState=N(null);detail=this.detailState.asReadonly();inspectedResultState=N(null);inspectedResult=this.inspectedResultState.asReadonly();resultErrorState=N(null);resultError=this.resultErrorState.asReadonly();tabState=N(0);tab=this.tabState.asReadonly();instanceQueryState=N({experiment:``,search:``,offset:0,limit:25});instanceQuery=this.instanceQueryState.asReadonly();resultQueryState=N({instanceId:``,algorithm:``,state:``,search:``,offset:0,limit:25});resultQuery=this.resultQueryState.asReadonly();issueSeverityState=N(``);issueSeverity=this.issueSeverityState.asReadonly();issueOffsetState=N(0);issueOffset=this.issueOffsetState.asReadonly();seenIssueIdState=N(0);seenIssueId=this.seenIssueIdState.asReadonly();objectives=ne(()=>Object.entries(this.status()?.objectives??{}).map(([n,e])=>({name:n,mode:e})));terminal=ne(()=>[`SUCCEEDED`,`COMPLETED_WITH_ERRORS`,`FAILED`,`CANCELLED`].includes(this.status()?.phase??``));connectionLabel=ne(()=>this.syncState()===`retrying`?`Retrying · data may be stale`:this.syncState()===`connecting`?`Connecting`:this.transportState()===`live`?`Live`:`Live via HTTP`);unseenIssues=ne(()=>Math.max(0,(this.status()?.issueCount??0)-this.seenIssueId()));estimatedRemainingMillis=ne(()=>{let n=this.status();if(!n||n.phase!==`RUNNING`||n.elapsedMillis<1e4)return null;let e=n.counts.succeeded+n.counts.failed;return e<5||n.counts.cancelled>0?null:n.elapsedMillis/e*(n.counts.pending+n.counts.running)});charts=ne(()=>{let n=this.detail(),e=this.selectedObjective(),i=this.selectedObjectiveMode();return n&&e&&i&&n.objective===e?cs.instance(n,i,this.showReference()):null});selectedObjectiveMode=ne(()=>this.status()?.objectives[this.selectedObjective()??``]??null);distribution=ne(()=>this.detail()?cs.distribution(this.detail(),this.showReference()):null);qualityRuntime=ne(()=>this.detail()?cs.qualityRuntime(this.detail(),this.showReference()):null);lastEventId=ne(()=>(this.transportState(),this.lastUpdated(),this.events.lastEventId));progress=ne(()=>{let n=this.status(),e=this.detail(),i=e?.algorithms.find(o=>o.name===this.resultQuery().algorithm),a=i?.counts??e?.instance.counts??n?.counts;return{instances:{completed:n?.completedInstances??0,total:n?.instances??0},algorithms:{completed:e?.instance.completedAlgorithms??0,total:e?.instance.algorithms??0},repetitions:{completed:(a?.succeeded??0)+(a?.failed??0),total:a?.planned??0},repetitionScope:i?`selected algorithm`:e?`selected instance`:`execution`}});cache=new Map;timer=null;polling=!1;pending=!1;started=!1;destroyed=!1;generation=0;delay=500;forceDetail=!0;lastDetailFetch=0;resultRequest=0;constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.generation++,this.resultRequest++,this.clearTimer()})}start(){this.started||(this.started=!0,this.events.state$.pipe(Dn(this.destroyRef)).subscribe(n=>{this.transportStateState.set(n),n===`live`&&this.transportErrorState.set(null)}),this.events.errors$.pipe(Dn(this.destroyRef)).subscribe(n=>this.transportErrorState.set(this.message(n))),this.events.connected$.pipe(Dn(this.destroyRef)).subscribe(()=>{this.generation++,this.forceDetail=!0,this.refreshNow()}),this.events.envelopes$.pipe(Dn(this.destroyRef)).subscribe(n=>{n.type!==`PingEvent`&&(n.type===`ExecutionEndedEvent`||n.type===`InstanceProcessingEndedEvent`?(this.forceDetail=!0,this.refreshNow()):this.polling||this.schedule(Math.max(100,500-(Date.now()-(this.lastUpdated()?.getTime()??0)))))}),this.events.start(),this.refreshNow())}retryNow(){this.delay=500,this.events.retryNow(),this.refreshNow()}setReferenceVisible(n){this.showReferenceState.set(n)}selectObjective(n){!this.status()?.objectives[n]||this.selectedObjective()===n||(this.selectedObjectiveState.set(n),this.generation++,this.forceDetail=!0,this.detailState.set(null),this.refreshNow())}selectInstance(n){this.selectedInstanceIdState.set(n),this.generation++,this.forceDetail=!0;let e=this.cache.get(n);this.detailState.set(e?.objective===this.selectedObjective()?e:null),this.refreshNow()}selectTab(n){this.tabState.set(n),this.refreshNow()}filterInstances(n,e){this.instanceQueryState.set(s(r({},this.instanceQuery()),{experiment:n,search:e,offset:0})),this.invalidate()}pageInstances(n){this.instanceQueryState.update(e=>s(r({},e),{offset:n})),this.invalidate()}filterResults(n,e,i,a){this.resultQueryState.set(s(r({},this.resultQuery()),{instanceId:n,algorithm:e,state:i,search:a,offset:0})),this.invalidate()}pageResults(n){this.resultQueryState.update(e=>s(r({},e),{offset:n})),this.invalidate()}sortResults(n){this.resultQueryState.update(e=>s(r({},e),{order:n,offset:0})),this.invalidate()}filterIssues(n){this.issueSeverityState.set(n),this.issueOffsetState.set(0),this.invalidate()}pageIssues(n){this.issueOffsetState.set(n),this.invalidate()}markIssuesSeen(){this.seenIssueIdState.set(this.status()?.issueCount??0);try{localStorage.setItem(`mork.normal.issues.v1`,JSON.stringify({runId:this.status()?.runId,id:this.seenIssueId()}))}catch{}}async inspectResult(n){let e=this.status()?.runId;if(!e)return;let i=++this.resultRequest;this.inspectedResultState.set(null),this.resultErrorState.set(null);try{let[a,o]=await Promise.all([this.client.getResult(e,n),this.client.getArtifacts(e)]);!this.destroyed&&i===this.resultRequest&&e===this.status()?.runId&&a.runId===e&&(this.inspectedResultState.set(a),o.runId===e&&this.artifactsState.set(o))}catch(a){!this.destroyed&&i===this.resultRequest&&e===this.status()?.runId&&this.resultErrorState.set(this.message(a))}}closeResult(){this.resultRequest++,this.inspectedResultState.set(null),this.resultErrorState.set(null)}artifactUrl(n){return this.client.artifactUrl(this.status().runId,n)}exportUrl(n){return this.client.exportUrl(this.status().runId,this.resultQuery(),n)}refreshNow(){this.clearTimer(),this.polling?this.pending=!0:this.poll()}invalidate(){this.generation++,this.refreshNow()}async poll(){if(this.destroyed)return;this.polling=!0,this.pending=!1;let n=this.generation,e=2e3;try{let i=await this.client.getStatus();if(!this.current(n))return;let a=this.status();if(i.runId!==a?.runId){this.cache.clear(),this.detailState.set(null),this.inspectedResultState.set(null),this.resultRequest++,this.instancePageState.set(null),this.resultPageState.set(null),this.issuePageState.set(null),this.artifactsState.set(null),this.selectedInstanceIdState.set(null),this.selectedObjectiveState.set(Object.keys(i.objectives)[0]??null),this.instanceQueryState.set({experiment:``,search:``,offset:0,limit:25}),this.resultQueryState.set({instanceId:``,algorithm:``,state:``,search:``,offset:0,limit:25}),this.seenIssueIdState.set(0),this.forceDetail=!0;try{let o=JSON.parse(localStorage.getItem(`mork.normal.issues.v1`)??`null`);o?.runId===i.runId&&this.seenIssueIdState.set(o.id)}catch{}}if(a?.runId===i.runId&&i.revision<a.revision)throw new Error(`Monitoring revision moved backwards`);if(this.statusState.set(i),i.runId){let o=i.runId,r=await this.client.getInstances(o,this.instanceQuery());if(!this.current(n)||r.runId!==o)return;this.instancePageState.set(r),!this.selectedInstanceId()&&r.items.length&&this.selectedInstanceIdState.set(r.items.find(b=>b.counts.running>0)?.id??r.items[0].id);let s=this.selectedInstanceId(),c=this.selectedObjective(),p=s?this.cache.get(s):null,m=r.items.find(b=>b.id===s),g=!p||p.objective!==c||!m||p.revision!==m.revision,v=[`SUCCEEDED`,`COMPLETED_WITH_ERRORS`,`FAILED`,`CANCELLED`].includes(i.phase)&&i.revision!==a?.revision;if(s&&c&&g&&(this.forceDetail||v||Date.now()-this.lastDetailFetch>=2e3)){let b=await this.client.getInstance(o,s,c);if(!this.current(n)||b.runId!==o||b.objective!==this.selectedObjective())return;for(this.cache.delete(s),this.cache.set(s,b);this.cache.size>10;)this.cache.delete(this.cache.keys().next().value);this.detailState.set(b),this.lastDetailFetch=Date.now(),this.forceDetail=!1}else p&&p.objective===c&&this.detailState.set(p);if(this.tab()===1){let b=await this.client.getResults(o,this.resultQuery());if(!this.current(n)||b.runId!==o)return;this.resultPageState.set(b)}if(this.tab()===2||i.issueCount!==a?.issueCount||!this.issuePage()){let b=await this.client.getIssues(o,this.issueSeverity(),this.issueOffset());if(!this.current(n)||b.runId!==o)return;this.issuePageState.set(b)}if(this.tab()===3){let b=await this.client.getArtifacts(o);if(!this.current(n)||b.runId!==o)return;this.artifactsState.set(b)}}this.latestErrorState.set(null),this.lastUpdatedState.set(new Date),this.syncStateState.set(`live`),this.delay=500,e=this.terminal()||i.phase===`IDLE`?1e4:2e3}catch(i){if(!this.current(n))return;this.latestErrorState.set(this.message(i)),this.syncStateState.set(`retrying`),e=this.delay,this.delay=Math.min(3e4,this.delay*2)}finally{this.polling=!1,this.destroyed||this.schedule(this.pending||n!==this.generation?0:e)}}current(n){return!this.destroyed&&n===this.generation}message(n){return n instanceof Error?n.message:String(n)}schedule(n){this.destroyed||(this.clearTimer(),this.timer=setTimeout(()=>{this.poll()},n))}clearTimer(){this.timer&&clearTimeout(this.timer),this.timer=null}static ɵfac=function(e){return new(e||t)};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})};var Tb=[{id:`evaluation-health`,label:`Evaluation health`},{id:`evaluation-cost`,label:`Evaluation cost`},{id:`elite-evolution`,label:`Elite rank evolution`},{id:`current-elites`,label:`Current elites`},{id:`run-information`,label:`Run information`}];var Ab=[{id:`normal-comparison`,label:`Algorithm comparison`},{id:`normal-scores`,label:`Scores`},{id:`normal-best`,label:`Best score across repetitions`},{id:`normal-distribution`,label:`Score distribution`},{id:`normal-runtime`,label:`Score versus runtime`}];var vm=`mork.autoconfig.panelPreferences.v1`;var Va=class t{hiddenSignal=N(new Set);collapsedSignal=N(new Set);hidden=this.hiddenSignal.asReadonly();collapsed=this.collapsedSignal.asReadonly();constructor(){this.restore()}isHidden(n){return this.hiddenSignal().has(n)}isCollapsed(n){return this.collapsedSignal().has(n)}show(n){this.hiddenSignal.update(e=>this.without(e,n)),this.persist()}hide(n){this.hiddenSignal.update(e=>this.with(e,n)),this.persist()}toggleCollapsed(n){this.collapsedSignal.update(e=>e.has(n)?this.without(e,n):this.with(e,n)),this.persist()}reset(){this.hiddenSignal.set(new Set),this.collapsedSignal.set(new Set),localStorage.removeItem(vm)}restore(){try{let n=localStorage.getItem(vm);if(!n)return;let e=JSON.parse(n),i=new Set([...Tb,...Ab].map(a=>a.id));this.hiddenSignal.set(new Set((e.hidden??[]).filter(a=>i.has(a)))),this.collapsedSignal.set(new Set((e.collapsed??[]).filter(a=>i.has(a))))}catch{localStorage.removeItem(vm)}}with(n,e){let i=new Set(n);return i.add(e),i}without(n,e){let i=new Set(n);return i.delete(e),i}persist(){let n={hidden:[...this.hiddenSignal()],collapsed:[...this.collapsedSignal()]};localStorage.setItem(vm,JSON.stringify(n))}static ɵfac=function(e){return new(e||t)};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})};var Ai=class t{model=zt.required();static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-dashboard-chart`]],inputs:{model:[1,`model`]},decls:1,vars:2,consts:[[1,`chart`,`highcharts-light`,3,`options`,`oneToOne`]],template:function(e,i){e&1&&z(0,`highcharts-chart`,0),e&2&&S(`options`,i.model().options)(`oneToOne`,!0)},dependencies:[vC],styles:[`[_nghost-%COMP%], .chart[_ngcontent-%COMP%]{display:block;width:100%;min-width:0}`]})};var gR=[`*`];function bR(t,n){t&1&&(l(0,`mat-card-content`),me(1),d())}function vR(t,n){if(t&1){let e=X();l(0,`mat-card`,1)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d(),z(4,`span`,2),l(5,`button`,3),C(`click`,function(){A(e);let a=y();return O(a.preferences.toggleCollapsed(a.panelId()))}),u(6),d(),l(7,`button`,4),u(8,` ⋮ `),d(),l(9,`mat-menu`,null,0)(11,`button`,5),C(`click`,function(){A(e);let a=y();return O(a.preferences.toggleCollapsed(a.panelId()))}),u(12),d(),l(13,`button`,5),C(`click`,function(){A(e);let a=y();return O(a.preferences.hide(a.panelId()))}),u(14,` Hide panel `),d()()(),D(15,bR,2,0,`mat-card-content`),d()}if(t&2){let e=ht(10),i=y();h(3),w(i.title()),h(2),te(`aria-label`,i.preferences.isCollapsed(i.panelId())?`Expand `+i.title():`Collapse `+i.title()),h(),H(` `,i.preferences.isCollapsed(i.panelId())?`+`:`−`,` `),h(),S(`matMenuTriggerFor`,e),te(`aria-label`,i.title()+` options`),h(5),H(` `,i.preferences.isCollapsed(i.panelId())?`Expand panel`:`Collapse panel`,` `),h(3),k(i.preferences.isCollapsed(i.panelId())?-1:15)}}var ls=class t{preferences=f(Va);panelId=zt.required();title=zt.required();static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-monitor-panel`]],inputs:{panelId:[1,`panelId`],title:[1,`title`]},ngContentSelectors:gR,decls:1,vars:1,consts:[[`panelMenu`,`matMenu`],[`appearance`,`outlined`],[1,`spacer`],[`mat-button`,``,`type`,`button`,1,`collapse`,3,`click`],[`mat-button`,``,`type`,`button`,1,`menu`,3,`matMenuTriggerFor`],[`mat-menu-item`,``,`type`,`button`,3,`click`]],template:function(e,i){e&1&&(Re(),D(0,vR,16,7,`mat-card`,1)),e&2&&k(i.preferences.isHidden(i.panelId())?-1:0)},dependencies:[Ot,en,zn,Bn,jn,Ma,Ia,as,Ti,ca,is],styles:[`[_nghost-%COMP%]{display:block;min-width:0}mat-card[_ngcontent-%COMP%]{height:100%}mat-card-header[_ngcontent-%COMP%]{min-height:3.5rem;align-items:center;padding-bottom:.5rem}mat-card-title[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-medium);font-weight:650}mat-card-content[_ngcontent-%COMP%]{min-width:0}.spacer[_ngcontent-%COMP%]{flex:1}.collapse[_ngcontent-%COMP%], .menu[_ngcontent-%COMP%]{min-width:2.25rem;padding:0;font-size:1.25rem}`]})};var yR=(t,n)=>n.id;function _R(t,n){if(t&1&&(l(0,`p`,0),u(1),d()),t&2){let e=n.$implicit;h(),ai(` `,e.severity,` · `,e.scope,`: `,e.exceptionType,` — `,e.message,` `)}}function xR(t,n){t&1&&wa(0)}function CR(t,n){if(t&1&&ot(0,xR,1,0,`ng-container`,2),t&2){let e=y();S(`ngComponentOutlet`,n)(`ngComponentOutletInputs`,e.rendererInputs())}}function SR(t,n){if(t&1&&(l(0,`details`,1)(1,`summary`),u(2,`Solution view data`),d(),l(3,`pre`),u(4),U(5,`json`),d()()),t&2){let e=y();h(4),w(_e(5,1,e.detail().rendering.data))}}function wR(t,n){if(t&1&&(l(0,`p`,0),u(1),d()),t&2){let e=y();h(),H(` `,e.detail().rendering.state===`UNSUPPORTED`?`A problem-specific solution view is not provided for this project.`:`This solution view is no longer retained.`,` Metadata remains available; exported solution files are listed below or in Artifacts. `)}}var ER=new I(`Mork solution renderers`,{providedIn:`root`,factory:()=>({})});var ym=class t{detail=zt.required();selectedObjective=zt.required();registered=f(ER);renderer=ne(()=>this.detail().rendering.state===`AVAILABLE`?this.registered[this.detail().rendering.rendererId??``]??null:null);rendererInputs=ne(()=>({result:this.detail().result,selectedObjective:this.selectedObjective(),view:this.detail().rendering.data}));static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-solution-renderer`]],inputs:{detail:[1,`detail`],selectedObjective:[1,`selectedObjective`]},decls:58,vars:27,consts:[[1,`note`],[`open`,``],[4,`ngComponentOutlet`,`ngComponentOutletInputs`]],template:function(e,i){if(e&1&&(l(0,`dl`)(1,`div`)(2,`dt`),u(3,`Instance / experiment`),d(),l(4,`dd`),u(5),d()(),l(6,`div`)(7,`dt`),u(8,`Algorithm`),d(),l(9,`dd`),u(10),d()(),l(11,`div`)(12,`dt`),u(13,`Iteration / seed`),d(),l(14,`dd`),u(15),d()(),l(16,`div`)(17,`dt`),u(18,`Outcome`),d(),l(19,`dd`),u(20),d()(),l(21,`div`)(22,`dt`),u(23),d(),l(24,`dd`),u(25),U(26,`number`),d()(),l(27,`div`)(28,`dt`),u(29,`Runtime`),d(),l(30,`dd`),u(31),U(32,`number`),d()(),l(33,`div`)(34,`dt`),u(35,`Time to best`),d(),l(36,`dd`),u(37),U(38,`number`),d()(),l(39,`div`)(40,`dt`),u(41,`Result ID`),d(),l(42,`dd`),u(43),d()()(),l(44,`details`)(45,`summary`),u(46,`All objective values and custom properties`),d(),l(47,`pre`),u(48),U(49,`json`),d(),l(50,`pre`),u(51),U(52,`json`),d()(),be(53,_R,2,4,`p`,0,yR),D(55,CR,1,2,`ng-container`)(56,SR,6,3,`details`,1)(57,wR,2,1,`p`,0)),e&2){let a,o=i.detail().result;h(5),Fe(``,o.instanceName,` / `,o.experimentName),h(5),w(o.algorithmName),h(5),Fe(``,o.iteration,` / `,o.seed),h(5),w(o.state),h(3),w(i.selectedObjective()??`Objective`),h(2),H(` `,Ie(26,14,o.objectives[i.selectedObjective()??``],`1.0-4`)??`Unavailable`,` `),h(6),H(` `,o.executionTime===null?`Unavailable`:Ie(32,17,o.executionTime/1e6,`1.0-2`)+` ms`,` `),h(6),H(` `,o.timeToBest===null?`Unavailable`:Ie(38,20,o.timeToBest/1e6,`1.0-2`)+` ms`,` `),h(6),w(o.resultId),h(5),w(_e(49,23,o.objectives)),h(3),w(_e(52,25,o.properties)),h(2),ve(i.detail().issues),h(2),k((a=i.renderer())?55:i.detail().rendering.state===`AVAILABLE`?56:57,a)}},dependencies:[ug,So,wn],styles:[`dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.75rem}dl[_ngcontent-%COMP%] > div[_ngcontent-%COMP%]{display:grid;gap:.25rem}dt[_ngcontent-%COMP%], .note[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}pre[_ngcontent-%COMP%]{overflow:auto;max-height:24rem;white-space:pre-wrap}@media(max-width:42rem){dl[_ngcontent-%COMP%]{grid-template-columns:1fr}}`]})};var ds=()=>[];var Qc=(t,n)=>n.id;var Ob=(t,n)=>n.name;var DR=(t,n)=>n.resultId;function kR(t,n){if(t&1){let e=X();l(0,`button`,8),C(`click`,function(){let a=A(e).$implicit,o=y();return O(o.preferences.isHidden(a.id)?o.preferences.show(a.id):o.preferences.hide(a.id))}),u(1),d()}if(t&2){let e=n.$implicit,i=y();h(),Fe(` `,i.preferences.isHidden(e.id)?`Show`:`Hide`,` `,e.label,` `)}}function IR(t,n){if(t&1){let e=X();l(0,`mat-card`,9)(1,`mat-card-content`)(2,`strong`),u(3,`Connection problem · showing last known data`),d(),l(4,`p`),u(5),d(),l(6,`button`,34),C(`click`,function(){A(e);return O(y().store.retryNow())}),u(7,`Retry now`),d()()()}t&2&&(h(5),w(n))}function MR(t,n){if(t&1&&(l(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit;S(`value`,e.name),h(),Fe(``,e.name,` · `,e.mode===`MINIMIZE`?`Minimize`:`Maximize`)}}function NR(t,n){t&1&&(u(0),U(1,`date`)),t&2&&H(` Last refreshed `,Ie(1,1,n,`mediumTime`),`. `)}function TR(t,n){if(t&1&&(l(0,`p`,24),u(1),d()),t&2){let e=y();h(),H(` Approximate time remaining: `,e.elapsedLabel(n),` · based on observed throughput; different instance sizes can change this estimate. `)}}function AR(t,n){if(t&1){let e=X();l(0,`button`,35),C(`click`,function(){A(e);return O(y().store.selectTab(2))}),u(1),d()}if(t&2){let e=y();h(),H(` `,e.store.unseenIssues(),` unseen issues `)}}function OR(t,n){if(t&1&&(l(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit;S(`value`,e.name),h(),w(e.name)}}function RR(t,n){if(t&1){let e=X();l(0,`tr`)(1,`td`)(2,`button`,35),C(`click`,function(){let a=A(e).$implicit;return O(y(2).store.selectInstance(a.id))}),u(3),d()(),l(4,`td`),u(5),d(),l(6,`td`),u(7),d(),l(8,`td`),u(9),d(),l(10,`td`),u(11),d()()}if(t&2){let e=n.$implicit,i=y(2);q(`selected`,e.id===i.store.selectedInstanceId()),h(3),H(` `,e.instanceName,` `),h(2),w(e.experimentName),h(2),Fe(` `,e.counts.succeeded+e.counts.failed,` / `,e.counts.planned,` `),h(2),w(e.counts.running),h(2),Fe(` `,e.counts.failed,` failed · `,e.finalized?`finalized`:`processing`,` `)}}function PR(t,n){t&1&&(l(0,`tr`)(1,`td`,46),u(2,`No matching instances yet.`),d()())}function FR(t,n){if(t&1&&(l(0,`p`,24),u(1),d()),t&2){let e=y();h(),Fe(` Charts show `,e.results.length,` sampled points from `,e.resultCount,` repetitions. Statistics are exact; full data is available in Results and exports. `)}}function LR(t,n){if(t&1&&(u(0),U(1,`number`)),t&2){let e=y().$implicit;H(` (`,Ie(1,1,e.objective.percentageGap,`1.0-2`),`%) `)}}function VR(t,n){if(t&1){let e=X();l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),d(),l(7,`td`),u(8),U(9,`number`),d(),l(10,`td`),u(11),U(12,`number`),d(),l(13,`td`),u(14),U(15,`number`),U(16,`number`),d(),l(17,`td`),u(18),U(19,`number`),D(20,LR,2,4),d(),l(21,`td`),u(22),U(23,`number`),d(),l(24,`td`),u(25),U(26,`number`),d(),l(27,`td`)(28,`button`,49),C(`click`,function(){let a=A(e).$implicit;return O(y(3).store.inspectResult(a.bestResultId))}),u(29,` Best result `),d()()()}if(t&2){let e=n.$implicit;h(2),w(e.name),h(2),Fe(` `,e.counts.succeeded,` / `,e.counts.succeeded+e.counts.failed,` `),h(2),w(e.objective.count),h(2),w(Ie(9,13,e.objective.best,`1.0-4`)??`—`),h(3),w(Ie(12,16,e.objective.median,`1.0-4`)??`—`),h(3),Fe(` `,Ie(15,19,e.objective.mean,`1.0-4`)??`—`,` ± `,Ie(16,22,e.objective.standardDeviation,`1.0-4`)??`—`,` `),h(4),H(` `,Ie(19,25,e.objective.absoluteGap,`1.0-4`)??`—`,` `),h(2),k(e.objective.percentageGap!==null?20:-1),h(2),H(``,Ie(23,28,e.runtimeMillis.median,`1.0-2`)??`—`,` ms`),h(3),H(``,Ie(26,31,e.timeToBestMillis.median,`1.0-2`)??`—`,` ms`),h(3),S(`disabled`,!e.bestResultId)}}function BR(t,n){if(t&1){let e=X();l(0,`mat-checkbox`,56),C(`change`,function(a){A(e);return O(y(4).store.setReferenceVisible(a.checked))}),u(1),d(),l(2,`p`,24),u(3),d()}if(t&2){let e=y(),i=y(),a=y(2);S(`checked`,a.store.showReference()),h(),Fe(` Show reference (`,i.objective,`: `,e.referenceValue,`) `),h(2),H(` The red dashed line marks the reference in all score charts. `,a.store.status()?.objectives?.[i.objective]===`MAXIMIZE`?`Higher`:`Lower`,` scores are better. `)}}function jR(t,n){if(t&1&&(l(0,`p`,24),u(1),d()),t&2){let e=y(2);h(),H(` No reference value is available for `,e.objective,`. `)}}function zR(t,n){if(t&1&&z(0,`app-dashboard-chart`,52),t&2)S(`model`,y().scoreChart)}function HR(t,n){if(t&1&&z(0,`app-dashboard-chart`,52),t&2)S(`model`,y().convergenceChart)}function UR(t,n){t&1&&z(0,`app-dashboard-chart`,52),t&2&&S(`model`,n)}function $R(t,n){t&1&&z(0,`app-dashboard-chart`,52),t&2&&S(`model`,n)}function GR(t,n){if(t&1&&(D(0,BR,4,4)(1,jR,2,1,`p`,24),l(2,`div`,50)(3,`app-monitor-panel`,51),D(4,zR,1,1,`app-dashboard-chart`,52),d(),l(5,`app-monitor-panel`,53),D(6,HR,1,1,`app-dashboard-chart`,52),l(7,`p`,24),u(8,` Independent repetitions, not the optimization trajectory within a single run. Failures are gaps. `),d()(),l(9,`app-monitor-panel`,54),D(10,UR,1,1,`app-dashboard-chart`,52),d(),l(11,`app-monitor-panel`,55),D(12,$R,1,1,`app-dashboard-chart`,52),d()()),t&2){let e,i,a=y(3);k(n.referenceValue!==null?0:1),h(4),k(!a.preferences.isHidden(`normal-scores`)&&!a.preferences.isCollapsed(`normal-scores`)?4:-1),h(2),k(!a.preferences.isHidden(`normal-best`)&&!a.preferences.isCollapsed(`normal-best`)?6:-1),h(4),k((e=!a.preferences.isHidden(`normal-distribution`)&&!a.preferences.isCollapsed(`normal-distribution`)&&a.store.distribution())?10:-1,e),h(2),k((i=!a.preferences.isHidden(`normal-runtime`)&&!a.preferences.isCollapsed(`normal-runtime`)&&a.store.qualityRuntime())?12:-1,i)}}function WR(t,n){if(t&1&&(l(0,`h2`,47),u(1),l(2,`small`),u(3),d()(),D(4,FR,2,2,`p`,24),l(5,`app-monitor-panel`,48)(6,`div`,41)(7,`table`)(8,`caption`,42),u(9),d(),l(10,`thead`)(11,`tr`)(12,`th`),u(13,`Algorithm`),d(),l(14,`th`),u(15,`Successful / attempted`),d(),l(16,`th`),u(17,`Valid scores`),d(),l(18,`th`),u(19,`Best`),d(),l(20,`th`),u(21,`Median`),d(),l(22,`th`),u(23,`Mean ± SD`),d(),l(24,`th`),u(25,`Gap to reference`),d(),l(26,`th`),u(27,`Runtime median`),d(),l(28,`th`),u(29,`Time-to-best median`),d(),l(30,`th`),u(31,`Details`),d()()(),l(32,`tbody`),be(33,VR,30,34,`tr`,null,Ob),d()()(),l(35,`p`,24),u(36,` Statistics use successful results with finite values. Scores from different instances are not averaged. Negative gaps improve on the reference. `),d()(),D(37,GR,13,5)),t&2){let e,i=n,a=y(2);h(),H(` `,i.instance.instanceName,` `),h(2),w(i.instance.experimentName),h(),k(i.sampled?4:-1),h(5),H(` `,i.objective,` statistics by algorithm `),h(24),ve(i.algorithms),h(4),k((e=a.store.charts())?37:-1,e)}}function qR(t,n){t&1&&(l(0,`p`,45),u(1,`Select an instance to inspect its results and charts.`),d())}function YR(t,n){if(t&1){let e=X();l(0,`mat-card`,36)(1,`mat-card-content`)(2,`h2`),u(3,`Instances`),d(),l(4,`div`,37)(5,`mat-form-field`,38)(6,`mat-label`),u(7,`Experiment`),d(),l(8,`mat-select`,15),C(`selectionChange`,function(a){A(e);let o=y();return O(o.store.filterInstances(a.value,o.store.instanceQuery().search))}),l(9,`mat-option`,39),u(10,`All experiments`),d(),be(11,OR,2,2,`mat-option`,16,Ob),d()(),l(13,`mat-form-field`,38)(14,`mat-label`),u(15,`Search instances`),d(),l(16,`input`,40),C(`input`,function(a){A(e);let o=y();return O(o.store.filterInstances(o.store.instanceQuery().experiment,a.target.value))}),d()()(),l(17,`div`,41)(18,`table`)(19,`caption`,42),u(20,` Instances across the execution `),d(),l(21,`thead`)(22,`tr`)(23,`th`),u(24,`Instance`),d(),l(25,`th`),u(26,`Experiment`),d(),l(27,`th`),u(28,`Completed repetitions`),d(),l(29,`th`),u(30,`Running`),d(),l(31,`th`),u(32,`Outcome`),d()()(),l(33,`tbody`),be(34,RR,12,9,`tr`,43,Qc,!1,PR,3,0,`tr`),d()()(),l(37,`mat-paginator`,44),C(`page`,function(a){A(e);return O(y().store.pageInstances(a.pageIndex*a.pageSize))}),d()()(),D(38,WR,38,5)(39,qR,2,0,`p`,45)}if(t&2){let e,i=y();h(8),S(`value`,i.store.instanceQuery().experiment),h(3),ve(i.store.status()?.experiments??vt(7,ds)),h(5),S(`value`,i.store.instanceQuery().search),h(18),ve(i.store.instancePage()?.items??vt(8,ds)),h(3),S(`length`,i.store.instancePage()?.total??0)(`pageSize`,25)(`pageIndex`,i.store.instanceQuery().offset/25),h(),k((e=i.store.detail())?38:39,e)}}function KR(t,n){if(t&1&&(l(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit,i=y(2);S(`value`,e),h(),w(e?i.phaseLabel(e):`All outcomes`)}}function QR(t,n){if(t&1&&(l(0,`a`,63),u(1,`CSV`),d(),l(2,`a`,63),u(3,`JSON`),d()),t&2){let e=y(2);S(`href`,e.store.exportUrl(`csv`),pn),h(2),S(`href`,e.store.exportUrl(`json`),pn)}}function XR(t,n){if(t&1){let e=X();l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),d(),l(7,`td`),u(8),d(),l(9,`td`),u(10),d(),l(11,`td`),u(12),U(13,`number`),d(),l(14,`td`),u(15),U(16,`number`),d(),l(17,`td`)(18,`button`,35),C(`click`,function(){let a=A(e).$implicit;return O(y(2).store.inspectResult(a.resultId))}),u(19,` Inspect `),d()()()}if(t&2){let e=n.$implicit,i=y(2);h(2),Fe(``,e.experimentName,` / `,e.instanceName),h(2),w(e.algorithmName),h(2),w(e.iteration),h(2),w(e.seed),h(2),w(i.phaseLabel(e.state)),h(2),H(` `,Ie(13,8,e.objectives[i.store.selectedObjective()??``],`1.0-4`)??`—`,` `),h(3),H(` `,e.executionTime===null?`—`:Ie(16,11,e.executionTime/1e6,`1.0-2`)+` ms`,` `)}}function ZR(t,n){t&1&&(l(0,`tr`)(1,`td`,64),u(2,`No matching repetitions.`),d()())}function JR(t,n){if(t&1){let e=X();l(0,`mat-card`,36)(1,`mat-card-content`)(2,`div`,37)(3,`mat-form-field`,38)(4,`mat-label`),u(5,`Search experiment, instance or algorithm`),d(),l(6,`input`,40),C(`input`,function(a){A(e);let o=y();return O(o.store.filterResults(o.store.resultQuery().instanceId,o.store.resultQuery().algorithm,o.store.resultQuery().state,a.target.value))}),d()(),l(7,`mat-form-field`,38)(8,`mat-label`),u(9,`Outcome`),d(),l(10,`mat-select`,15),C(`selectionChange`,function(a){A(e);let o=y();return O(o.store.filterResults(o.store.resultQuery().instanceId,o.store.resultQuery().algorithm,a.value,o.store.resultQuery().search))}),be(11,KR,2,2,`mat-option`,16,Pn),d()(),l(13,`mat-form-field`,38)(14,`mat-label`),u(15,`Algorithm name`),d(),l(16,`input`,40),C(`input`,function(a){A(e);let o=y();return O(o.store.filterResults(o.store.resultQuery().instanceId,a.target.value,o.store.resultQuery().state,o.store.resultQuery().search))}),d()(),l(17,`mat-form-field`,38)(18,`mat-label`),u(19,`Sort results`),d(),l(20,`mat-select`,15),C(`selectionChange`,function(a){A(e);return O(y().store.sortResults(a.value))}),l(21,`mat-option`,57),u(22,`Planned order`),d(),l(23,`mat-option`,58),u(24,`Instance name`),d(),l(25,`mat-option`,59),u(26,`Algorithm name`),d(),l(27,`mat-option`,60),u(28,`Outcome`),d(),l(29,`mat-option`,61),u(30,`Runtime · fastest first`),d()()(),l(31,`button`,35),C(`click`,function(){A(e);let a=y();return O(a.store.filterResults(a.store.selectedInstanceId()??``,a.store.resultQuery().algorithm,a.store.resultQuery().state,a.store.resultQuery().search))}),u(32,` Selected instance only `),d(),l(33,`button`,35),C(`click`,function(){A(e);return O(y().store.filterResults(``,``,``,``))}),u(34,` Clear filters `),d(),D(35,QR,4,2),d(),l(36,`div`,41)(37,`table`)(38,`caption`,42),u(39,` Repetition result ledger `),d(),l(40,`thead`)(41,`tr`)(42,`th`),u(43,`Experiment / instance`),d(),l(44,`th`),u(45,`Algorithm`),d(),l(46,`th`),u(47,`Iteration`),d(),l(48,`th`),u(49,`Seed`),d(),l(50,`th`),u(51,`Outcome`),d(),l(52,`th`),u(53),d(),l(54,`th`),u(55,`Runtime`),d(),l(56,`th`),u(57,`Details`),d()()(),l(58,`tbody`),be(59,XR,20,14,`tr`,null,DR,!1,ZR,3,0,`tr`),d()()(),l(62,`mat-paginator`,62),C(`page`,function(a){A(e);return O(y().store.pageResults(a.pageIndex*a.pageSize))}),d()()()}if(t&2){let e=y();h(6),S(`value`,e.store.resultQuery().search),h(4),S(`value`,e.store.resultQuery().state),h(),ve(e.workStates),h(5),S(`value`,e.store.resultQuery().algorithm),h(4),S(`value`,e.store.resultQuery().order??`PLANNED`),h(15),k(e.store.status()?.runId?35:-1),h(18),w(e.store.selectedObjective()??`Objective`),h(6),ve(e.store.resultPage()?.items??vt(10,ds)),h(3),S(`length`,e.store.resultPage()?.total??0)(`pageSize`,25)(`pageIndex`,e.store.resultQuery().offset/25)}}function eP(t,n){if(t&1&&u(0),t&2){let e=y().$implicit;Fe(` · iteration `,e.iteration,` · seed `,e.seed,` `)}}function tP(t,n){if(t&1){let e=X();l(0,`button`,35),C(`click`,function(){A(e);let a=y().$implicit;return O(y(2).store.inspectResult(a.resultId))}),u(1,` Inspect result `),d()}}function nP(t,n){if(t&1&&(l(0,`mat-card`,68)(1,`mat-card-content`)(2,`strong`),u(3),d(),l(4,`p`),u(5),d(),l(6,`p`),u(7),D(8,eP,1,2),u(9),U(10,`date`),d(),D(11,tP,2,0,`button`,25),d()()),t&2){let e=n.$implicit;h(3),jt(``,e.severity,` · `,e.scope,` · `,e.exceptionType),h(2),w(e.message??`No details provided`),h(2),jt(` `,e.experimentName,` / `,e.instanceName,` / `,e.algorithmName,` `),h(),k(e.iteration!==null?8:-1),h(),H(` · `,Ie(10,10,e.timestamp,`medium`),` `),h(2),k(e.resultId?11:-1)}}function iP(t,n){t&1&&(l(0,`p`,45),u(1,`No experiment issues have been reported.`),d())}function aP(t,n){if(t&1){let e=X();l(0,`div`,37)(1,`mat-form-field`,38)(2,`mat-label`),u(3,`Severity`),d(),l(4,`mat-select`,15),C(`selectionChange`,function(a){A(e);return O(y().store.filterIssues(a.value))}),l(5,`mat-option`,39),u(6,`All severities`),d(),l(7,`mat-option`,65),u(8,`Fatal`),d(),l(9,`mat-option`,66),u(10,`Error`),d(),l(11,`mat-option`,67),u(12,`Warning`),d()()(),l(13,`button`,35),C(`click`,function(){A(e);return O(y().store.markIssuesSeen())}),u(14,`Mark issues seen`),d()(),be(15,nP,12,13,`mat-card`,68,Qc,!1,iP,2,0,`p`,45),l(18,`mat-paginator`,69),C(`page`,function(a){A(e);return O(y().store.pageIssues(a.pageIndex*a.pageSize))}),d()}if(t&2){let e=y();h(4),S(`value`,e.store.issueSeverity()),h(11),ve(e.store.issuePage()?.items??vt(5,ds)),h(3),S(`length`,e.store.issuePage()?.total??0)(`pageSize`,25)(`pageIndex`,e.store.issueOffset()/25)}}function oP(t,n){if(t&1&&(l(0,`a`,63),u(1,`Download results CSV`),d(),l(2,`a`,63),u(3,`Download results JSON`),d()),t&2){let e=y(2);S(`href`,e.store.exportUrl(`csv`),pn),h(2),S(`href`,e.store.exportUrl(`json`),pn)}}function rP(t,n){if(t&1&&(l(0,`tr`)(1,`td`)(2,`a`,70),u(3),d()(),l(4,`td`),u(5),d(),l(6,`td`),u(7),U(8,`number`),d(),l(9,`td`),u(10),U(11,`date`),d()()),t&2){let e=n.$implicit,i=y(2);h(2),S(`href`,i.store.artifactUrl(e.id),pn),h(),w(e.filename),h(2),w(e.experimentName),h(2),w(_e(8,5,e.size)),h(3),w(Ie(11,7,e.modifiedAt,`medium`))}}function sP(t,n){t&1&&(l(0,`tr`)(1,`td`,71),u(2,`No exported artifacts are available yet.`),d()())}function cP(t,n){if(t&1&&(l(0,`p`),u(1,` Only successfully exported, registered files are available. Result exports use the current Results filters. `),d(),D(2,oP,4,2),l(3,`div`,41)(4,`table`)(5,`thead`)(6,`tr`)(7,`th`),u(8,`File`),d(),l(9,`th`),u(10,`Experiment`),d(),l(11,`th`),u(12,`Bytes`),d(),l(13,`th`),u(14,`Updated`),d()()(),l(15,`tbody`),be(16,rP,12,10,`tr`,null,Qc,!1,sP,3,0,`tr`),d()()()),t&2){let e=y();h(2),k(e.store.status()?.runId?2:-1),h(14),ve(e.store.artifacts()?.artifacts??vt(2,ds))}}function lP(t,n){t&1&&(l(0,`p`),u(1),d()),t&2&&(h(),w(n))}function dP(t,n){if(t&1){let e=X();l(0,`mat-card`,72)(1,`mat-card-content`)(2,`h2`),u(3,`Transport diagnostics`),d(),l(4,`p`),u(5),d(),l(6,`p`),u(7),d(),l(8,`p`),u(9),d(),l(10,`p`),u(11),d(),D(12,lP,2,1,`p`),l(13,`p`),u(14,` Summaries and results come from backend snapshots. Connection health is independent of execution outcome. `),d(),l(15,`button`,35),C(`click`,function(){A(e);return O(y().store.retryNow())}),u(16,`Reconnect / refresh`),d()()()}if(t&2){let e,i=y();h(5),H(`Run: `,i.store.status()?.runId??`No execution`),h(2),H(`Revision: `,i.store.status()?.revision??0),h(2),H(`WebSocket: `,i.store.transportState()),h(2),H(`Last processed notification ID: `,i.store.lastEventId()),h(),k((e=i.store.transportError())?12:-1,e)}}function uP(t,n){if(t&1){let e=X();l(0,`mat-card`,9)(1,`mat-card-content`),u(2),l(3,`button`,35),C(`click`,function(){A(e);return O(y().store.closeResult())}),u(4,`Close`),d()()()}t&2&&(h(2),w(n))}function mP(t,n){if(t&1&&(l(0,`a`,63),u(1),d()),t&2){let e=y().$implicit;S(`href`,y(2).store.artifactUrl(e.id),pn),h(),w(e.filename)}}function pP(t,n){if(t&1&&D(0,mP,2,2,`a`,63),t&2){let e=n.$implicit,i=y();k(e.resultId===i.result.resultId?0:-1)}}function hP(t,n){if(t&1){let e=X();l(0,`mat-card`,33)(1,`mat-card-content`)(2,`div`,73)(3,`h2`),u(4,`Result details`),d(),l(5,`button`,35),C(`click`,function(){A(e);return O(y().store.closeResult())}),u(6,`Close`),d()(),z(7,`app-solution-renderer`,74),be(8,pP,1,1,null,null,Qc),d()()}if(t&2){let e=y();h(7),S(`detail`,n)(`selectedObjective`,e.store.selectedObjective()),h(),ve(e.store.artifacts()?.artifacts??vt(2,ds))}}var _m=class t{store=f(Kc);preferences=f(Va);panels=Ab;workStates=[``,`PENDING`,`RUNNING`,`SUCCEEDED`,`FAILED`,`CANCELLED`];ngOnInit(){this.store.start()}progressValue(n,e){return e>0?Math.min(100,n/e*100):0}elapsedLabel(n){let e=Math.floor(n/1e3);return e>=3600?Math.floor(e/3600)+`h `+Math.floor(e%3600/60)+`m`:Math.floor(e/60)+`m `+e%60+`s`}phaseLabel(n){return n.toLowerCase().replaceAll(`_`,` `)}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-standard-dashboard`]],features:[Ge([Kc,ss,Fa])],decls:113,vars:31,consts:[[`panelMenu`,`matMenu`],[1,`topbar`],[`src`,`favicon.svg`,`alt`,`Mork`,1,`logo`],[1,`brand`],[1,`toolbar-spacer`],[`aria-label`,`Connection state`],[`mat-button`,``,`aria-label`,`Choose dashboard panels`,3,`matMenuTriggerFor`],[`mat-menu-item`,``],[`mat-menu-item`,``,3,`click`],[`role`,`alert`,1,`error-card`],[`appearance`,`outlined`,1,`status-card`],[`aria-label`,`Execution summary`,1,`summary`],[1,`eyebrow`],[1,`phase`],[`appearance`,`outline`,`subscriptSizing`,`dynamic`],[3,`selectionChange`,`value`],[3,`value`],[`aria-label`,`Repetition outcomes`,1,`metrics`],[`aria-label`,`Execution progress`,1,`progress-grid`],[1,`progress-item`],[1,`progress-label`],[`mode`,`determinate`,`aria-label`,`Finalized instances in execution`,3,`value`],[`mode`,`determinate`,`aria-label`,`Finalized algorithms in selected instance`,3,`value`],[`mode`,`determinate`,`aria-label`,`Completed repetitions in selection`,3,`value`],[1,`status-footnote`],[`mat-button`,``],[`animationDuration`,`0ms`,3,`selectedIndexChange`,`selectedIndex`],[`label`,`Overview`],[`matTabContent`,``],[`label`,`Results`],[`label`,`Issues`],[`label`,`Artifacts`],[`label`,`Diagnostics`],[`appearance`,`outlined`,`aria-label`,`Result details`,1,`result-detail`],[`mat-flat-button`,``,3,`click`],[`mat-button`,``,3,`click`],[`appearance`,`outlined`,1,`browser`],[1,`filters`],[`appearance`,`outline`],[`value`,``],[`matInput`,``,3,`input`,`value`],[1,`table-scroll`],[1,`sr-only`],[3,`selected`],[`aria-label`,`Instance pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[1,`empty-state`],[`colspan`,`5`],[1,`detail-title`],[`panelId`,`normal-comparison`,`title`,`Algorithm comparison`],[`mat-button`,``,3,`click`,`disabled`],[1,`chart-grid`],[`panelId`,`normal-scores`,`title`,`Scores`],[3,`model`],[`panelId`,`normal-best`,`title`,`Best score across repetitions`],[`panelId`,`normal-distribution`,`title`,`Score distribution`],[`panelId`,`normal-runtime`,`title`,`Score versus runtime`],[1,`reference-toggle`,3,`change`,`checked`],[`value`,`PLANNED`],[`value`,`INSTANCE`],[`value`,`ALGORITHM`],[`value`,`STATE`],[`value`,`RUNTIME`],[`aria-label`,`Result pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[`mat-button`,``,3,`href`],[`colspan`,`8`],[`value`,`FATAL`],[`value`,`ERROR`],[`value`,`WARNING`],[`appearance`,`outlined`,1,`issue`],[`aria-label`,`Issue pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[3,`href`],[`colspan`,`4`],[1,`browser`],[1,`result-heading`],[3,`detail`,`selectedObjective`]],template:function(e,i){if(e&1&&(l(0,`mat-toolbar`,1),z(1,`img`,2),l(2,`span`,3),u(3,`Experiment monitor`),d(),z(4,`span`,4),l(5,`mat-chip-set`,5)(6,`mat-chip`),u(7),d()(),l(8,`button`,6),u(9,` Panels `),d(),l(10,`mat-menu`,null,0),be(12,kR,2,2,`button`,7,Qc),l(14,`button`,8),C(`click`,function(){return i.preferences.reset()}),u(15,`Reset panels`),d()()(),l(16,`main`),D(17,IR,8,1,`mat-card`,9),l(18,`mat-card`,10)(19,`mat-card-content`)(20,`section`,11)(21,`div`)(22,`span`,12),u(23,`Execution`),d(),l(24,`strong`,13),u(25),d()(),l(26,`div`)(27,`span`,12),u(28,`Experiment`),d(),l(29,`strong`),u(30),d()(),l(31,`div`)(32,`span`,12),u(33,`Elapsed`),d(),l(34,`strong`),u(35),d()(),l(36,`mat-form-field`,14)(37,`mat-label`),u(38,`Objective`),d(),l(39,`mat-select`,15),C(`selectionChange`,function(o){return i.store.selectObjective(o.value)}),be(40,MR,2,3,`mat-option`,16,Ob),d()()(),l(42,`section`,17)(43,`div`)(44,`span`),u(45,`Planned`),d(),l(46,`strong`),u(47),d()(),l(48,`div`)(49,`span`),u(50,`Pending`),d(),l(51,`strong`),u(52),d()(),l(53,`div`)(54,`span`),u(55,`Running`),d(),l(56,`strong`),u(57),d()(),l(58,`div`)(59,`span`),u(60,`Successful`),d(),l(61,`strong`),u(62),d()(),l(63,`div`)(64,`span`),u(65,`Failed`),d(),l(66,`strong`),u(67),d()(),l(68,`div`)(69,`span`),u(70,`Cancelled`),d(),l(71,`strong`),u(72),d()()(),l(73,`section`,18)(74,`div`,19)(75,`div`,20)(76,`span`),u(77,`Instances · execution`),d(),l(78,`span`),u(79),d()(),z(80,`mat-progress-bar`,21),d(),l(81,`div`,19)(82,`div`,20)(83,`span`),u(84,`Algorithms · selected instance`),d(),l(85,`span`),u(86),d()(),z(87,`mat-progress-bar`,22),d(),l(88,`div`,19)(89,`div`,20)(90,`span`),u(91),d(),l(92,`span`),u(93),d()(),z(94,`mat-progress-bar`,23),d()(),l(95,`p`,24),u(96),D(97,NR,2,4),d(),D(98,TR,2,1,`p`,24),D(99,AR,2,1,`button`,25),d()(),l(100,`mat-tab-group`,26),C(`selectedIndexChange`,function(o){return i.store.selectTab(o)}),l(101,`mat-tab`,27),ot(102,YR,40,9,`ng-template`,28),d(),l(103,`mat-tab`,29),ot(104,JR,63,11,`ng-template`,28),d(),l(105,`mat-tab`,30),ot(106,aP,19,6,`ng-template`,28),d(),l(107,`mat-tab`,31),ot(108,cP,19,3,`ng-template`,28),d(),l(109,`mat-tab`,32),ot(110,dP,17,5,`ng-template`,28),d()(),D(111,uP,5,1,`mat-card`,9),D(112,hP,10,3,`mat-card`,33),d()),e&2){let a,o,r,s,c,p=ht(11);h(7),w(i.store.connectionLabel()),h(),S(`matMenuTriggerFor`,p),h(4),ve(i.panels),h(5),k((a=i.store.latestError())?17:-1,a);let m=i.store.status();h(8),w(i.phaseLabel(m?.phase??`IDLE`)),h(5),w(m?.currentExperiment??`Waiting for execution`),h(5),w(i.elapsedLabel(m?.elapsedMillis??0)),h(4),S(`value`,i.store.selectedObjective()),h(),ve(i.store.objectives()),h(7),w(m?.counts?.planned??0),h(5),w(m?.counts?.pending??0),h(5),w(m?.counts?.running??0),h(5),w(m?.counts?.succeeded??0),h(5),w(m?.counts?.failed??0),h(5),w(m?.counts?.cancelled??0);let g=i.store.progress();h(7),Fe(``,g.instances.completed,` / `,g.instances.total),h(),S(`value`,i.progressValue(g.instances.completed,g.instances.total)),h(6),Fe(``,g.algorithms.completed,` / `,g.algorithms.total),h(),S(`value`,i.progressValue(g.algorithms.completed,g.algorithms.total)),h(4),H(`Repetitions · `,g.repetitionScope),h(2),Fe(``,g.repetitions.completed,` / `,g.repetitions.total),h(),S(`value`,i.progressValue(g.repetitions.completed,g.repetitions.total)),h(2),Fe(` `,m?.runningInstances??0,` active instances · `,m?.runningAlgorithms??0,` active algorithms. Completed repetitions include failures; cancelled work does not count as completed. `),h(),k((o=i.store.lastUpdated())?97:-1,o),h(),k((r=i.store.estimatedRemainingMillis())?98:-1,r),h(),k(i.store.unseenIssues()>0?99:-1),h(),S(`selectedIndex`,i.store.tab()),h(11),k((s=i.store.resultError())?111:-1,s),h(),k((c=i.store.inspectedResult())?112:-1,c)}},dependencies:[Ai,ls,Ot,en,zn,Bn,jn,em,Bc,Ju,tm,jc,ui,Ni,na,Qr,Kr,as,Ti,ca,is,mm,Wc,rs,os,Pa,Ra,Un,hm,qc,Yc,pm,gm,fm,ym,Xi,wn],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh;color:var(--%NS%mat-sys-on-surface);background:var(--%NS%mat-sys-surface-container-lowest)}.topbar[_ngcontent-%COMP%]{position:sticky;top:0;z-index:10;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:var(--%NS%mat-sys-surface);gap:.75rem}.logo[_ngcontent-%COMP%]{width:2.2rem;height:2.2rem}.brand[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-large);font-weight:700}.toolbar-spacer[_ngcontent-%COMP%]{flex:1}main[_ngcontent-%COMP%]{width:min(100% - 2rem,96rem);margin:auto;padding:1.5rem 0 4rem}.status-card[_ngcontent-%COMP%], .browser[_ngcontent-%COMP%], .issue[_ngcontent-%COMP%], .result-detail[_ngcontent-%COMP%]{margin:1rem 0}.summary[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(3,minmax(0,1fr)) minmax(13rem,1fr);gap:1rem;align-items:center}.summary[_ngcontent-%COMP%] > div[_ngcontent-%COMP%], .metrics[_ngcontent-%COMP%] > div[_ngcontent-%COMP%]{display:grid;gap:.3rem}.phase[_ngcontent-%COMP%]{text-transform:capitalize}.eyebrow[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-label-medium);text-transform:uppercase;color:var(--%NS%mat-sys-on-surface-variant);letter-spacing:.05em}.metrics[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(6,1fr);gap:1rem;padding:1.2rem 0}.metrics[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-headline-small)}.progress-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:1.5rem;border-top:1px solid var(--%NS%mat-sys-outline-variant);padding-top:1rem}.progress-item[_ngcontent-%COMP%]{display:grid;gap:.5rem}.progress-label[_ngcontent-%COMP%]{display:flex;justify-content:space-between;gap:.5rem;font:var(--%NS%mat-sys-label-large)}.status-footnote[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.85rem;line-height:1.5}.filters[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem;align-items:center;margin:1rem 0}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{flex:1;min-width:12rem}.table-scroll[_ngcontent-%COMP%]{overflow-x:auto}table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse;font:var(--%NS%mat-sys-body-medium)}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{text-align:left;padding:.65rem .75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);white-space:nowrap}th[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-large)}tr.selected[_ngcontent-%COMP%]{background:var(--%NS%mat-sys-secondary-container)}.chart-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem;margin-top:1rem}.detail-title[_ngcontent-%COMP%]{margin:1.5rem 0 1rem}small[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.85rem}.error-card[_ngcontent-%COMP%]{background:var(--%NS%mat-sys-error-container);color:var(--%NS%mat-sys-on-error-container);margin:1rem 0}.result-heading[_ngcontent-%COMP%]{display:flex;justify-content:space-between;align-items:center}.empty-state[_ngcontent-%COMP%]{min-height:8rem;display:grid;place-items:center;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}.sr-only[_ngcontent-%COMP%]{position:absolute;width:1px;height:1px;padding:0;overflow:hidden;clip:rect(0,0,0,0)}@media(max-width:60rem){.summary[_ngcontent-%COMP%]{grid-template-columns:repeat(2,minmax(0,1fr))}.metrics[_ngcontent-%COMP%]{grid-template-columns:repeat(3,1fr)}.progress-grid[_ngcontent-%COMP%]{grid-template-columns:1fr}}@media(max-width:42rem){main[_ngcontent-%COMP%]{width:calc(100% - 1rem);padding-top:.25rem}.chart-grid[_ngcontent-%COMP%], .summary[_ngcontent-%COMP%]{grid-template-columns:1fr}.brand[_ngcontent-%COMP%]{display:none}.topbar[_ngcontent-%COMP%]{gap:.25rem;padding:0 .5rem}.logo[_ngcontent-%COMP%]{width:1.5rem}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{min-width:100%}}`]})};var fP=new I(`MAT_BADGE_CONFIG`);var Bw=`mat-badge-content`;var gP=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],decls:0,vars:0,template:function(i,a){},styles:[`.mat-badge {
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
`],encapsulation:2})}return t})();var jw=(()=>{class t{_ngZone=f(Q);_elementRef=f(W);_ariaDescriber=f(Tu);_renderer=f(Oe);_animationsDisabled=Ue();_idGenerator=f(Be);get color(){return this._color}set color(e){this._setColor(e),this._color=e}_color;overlap;disabled=!1;position;get content(){return this._content}set content(e){this._updateRenderedContent(e)}_content;get description(){return this._description}set description(e){this._updateDescription(e)}_description;size;hidden=!1;_badgeElement;_inlineBadgeDescription;_isInitialized=!1;_interactivityChecker=f(Fr);_document=f(de);constructor(){let e=f(fP,{optional:!0}),i=f(Je);i.load(gP),i.load(ki),this._color=e?.color||`primary`,this.overlap=e?.overlap??!0,this.position=e?.position||`above after`,this.size=e?.size||`medium`}isAbove(){return this.position.indexOf(`below`)===-1}isAfter(){return this.position.indexOf(`before`)===-1}getBadgeElement(){return this._badgeElement}ngOnInit(){this._clearExistingBadges(),this.content&&!this._badgeElement&&(this._badgeElement=this._createBadgeElement(),this._updateRenderedContent(this.content)),this._isInitialized=!0}ngAfterViewInit(){}ngOnDestroy(){this._renderer.destroyNode&&(this._renderer.destroyNode(this._badgeElement),this._inlineBadgeDescription?.remove()),this._ariaDescriber.removeDescription(this._elementRef.nativeElement,this.description)}_isHostInteractive(){return this._interactivityChecker.isFocusable(this._elementRef.nativeElement,{ignoreVisibility:!0})}_createBadgeElement(){let e=this._renderer.createElement(`span`),i=`mat-badge-active`;return e.setAttribute(`id`,this._idGenerator.getId(`mat-badge-content-`)),e.setAttribute(`aria-hidden`,`true`),e.classList.add(Bw),this._animationsDisabled&&e.classList.add(`_mat-animation-noopable`),this._elementRef.nativeElement.appendChild(e),typeof requestAnimationFrame==`function`&&!this._animationsDisabled?this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>{e.classList.add(i)})}):e.classList.add(i),e}_updateRenderedContent(e){let i=`${e??``}`.trim();this._isInitialized&&i&&!this._badgeElement&&(this._badgeElement=this._createBadgeElement()),this._badgeElement&&(this._badgeElement.textContent=i),this._content=i}_updateDescription(e){this._ariaDescriber.removeDescription(this._elementRef.nativeElement,this.description),(!e||this._isHostInteractive())&&this._removeInlineDescription(),this._description=e,this._isHostInteractive()?this._ariaDescriber.describe(this._elementRef.nativeElement,e):this._updateInlineDescription()}_updateInlineDescription(){this._inlineBadgeDescription||(this._inlineBadgeDescription=this._document.createElement(`span`),this._inlineBadgeDescription.classList.add(`cdk-visually-hidden`)),this._inlineBadgeDescription.textContent=this.description,this._badgeElement?.appendChild(this._inlineBadgeDescription)}_removeInlineDescription(){this._inlineBadgeDescription?.remove(),this._inlineBadgeDescription=void 0}_setColor(e){let i=this._elementRef.nativeElement.classList;i.remove(`mat-badge-${this._color}`),e&&i.add(`mat-badge-${e}`)}_clearExistingBadges(){let e=this._elementRef.nativeElement.querySelectorAll(`:scope > .${Bw}`);for(let i of Array.from(e))i!==this._badgeElement&&i.remove()}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`matBadge`,``]],hostAttrs:[1,`mat-badge`],hostVars:20,hostBindings:function(i,a){i&2&&q(`mat-badge-overlap`,a.overlap)(`mat-badge-above`,a.isAbove())(`mat-badge-below`,!a.isAbove())(`mat-badge-before`,!a.isAfter())(`mat-badge-after`,a.isAfter())(`mat-badge-small`,a.size===`small`)(`mat-badge-medium`,a.size===`medium`)(`mat-badge-large`,a.size===`large`)(`mat-badge-hidden`,a.hidden||!a.content)(`mat-badge-disabled`,a.disabled)},inputs:{color:[0,`matBadgeColor`,`color`],overlap:[2,`matBadgeOverlap`,`overlap`,V],disabled:[2,`matBadgeDisabled`,`disabled`,V],position:[0,`matBadgePosition`,`position`],content:[0,`matBadge`,`content`],description:[0,`matBadgeDescription`,`description`],size:[0,`matBadgeSize`,`size`],hidden:[2,`matBadgeHidden`,`hidden`,V]}})}return t})();var zw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[Io,we]})}return t})();var Ba=class{viewContainerRef;injector;id;role=`dialog`;panelClass=``;hasBackdrop=!0;backdropClass=``;disableClose=!1;closePredicate;width=``;height=``;minWidth;minHeight;maxWidth;maxHeight;positionStrategy;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus=`first-tabbable`;restoreFocus=!0;scrollStrategy;closeOnNavigation=!0;closeOnDestroy=!0;closeOnOverlayDetachments=!0;disableAnimations=!1;providers;container;templateContext;bindings};var Pb=(()=>{class t extends Xr{_elementRef=f(W);_focusTrapFactory=f(Pg);_config;_interactivityChecker=f(Fr);_ngZone=f(Q);_focusMonitor=f(Jt);_renderer=f(Oe);_changeDetectorRef=f(Pe);_injector=f(re);_platform=f(Ae);_document=f(de);_portalOutlet;_focusTrapped=new L;_focusTrap=null;_elementFocusedBeforeDialogWasOpened=null;_closeInteractionType=null;_ariaLabelledByQueue=[];_isDestroyed=!1;constructor(){super(),this._config=f(Ba,{optional:!0})||new Ba,this._config.ariaLabelledBy&&this._ariaLabelledByQueue.push(this._config.ariaLabelledBy)}_addAriaLabelledBy(e){this._ariaLabelledByQueue.push(e),this._changeDetectorRef.markForCheck()}_removeAriaLabelledBy(e){let i=this._ariaLabelledByQueue.indexOf(e);i>-1&&(this._ariaLabelledByQueue.splice(i,1),this._changeDetectorRef.markForCheck())}_contentAttached(){this._initializeFocusTrap(),this._captureInitialFocus()}_captureInitialFocus(){this._trapFocus()}ngOnDestroy(){this._focusTrapped.complete(),this._isDestroyed=!0,this._restoreFocus()}attachComponentPortal(e){this._portalOutlet.hasAttached();let i=this._portalOutlet.attachComponentPortal(e);return this._contentAttached(),i}attachTemplatePortal(e){this._portalOutlet.hasAttached();let i=this._portalOutlet.attachTemplatePortal(e);return this._contentAttached(),i}attachDomPortal=e=>{this._portalOutlet.hasAttached();let i=this._portalOutlet.attachDomPortal(e);return this._contentAttached(),i};_recaptureFocus(){this._containsFocus()||this._trapFocus()}_forceFocus(e,i){this._interactivityChecker.isFocusable(e)||(e.tabIndex=-1,this._ngZone.runOutsideAngular(()=>{let a=()=>{o(),r(),e.removeAttribute(`tabindex`)},o=this._renderer.listen(e,`blur`,a),r=this._renderer.listen(e,`mousedown`,a)})),e.focus(i)}_focusByCssSelector(e,i){let a=this._elementRef.nativeElement.querySelector(e);a&&this._forceFocus(a,i)}_trapFocus(e){this._isDestroyed||Tt(()=>{let i=this._elementRef.nativeElement;switch(this._config.autoFocus){case!1:case`dialog`:this._containsFocus()||i.focus(e);break;case!0:case`first-tabbable`:this._focusTrap?.focusInitialElement(e)||this._focusDialogContainer(e);break;case`first-heading`:this._focusByCssSelector(`h1, h2, h3, h4, h5, h6, [role="heading"]`,e);break;default:this._focusByCssSelector(this._config.autoFocus,e)}this._focusTrapped.next()},{injector:this._injector})}_restoreFocus(){let e=this._config.restoreFocus,i=null;if(typeof e==`string`?i=this._document.querySelector(e):typeof e==`boolean`?i=e?this._elementFocusedBeforeDialogWasOpened:null:e&&(i=e),this._config.restoreFocus&&i&&typeof i.focus==`function`){let a=Cc(),o=this._elementRef.nativeElement;(!a||a===this._document.body||a===o||o.contains(a))&&(this._focusMonitor?(this._focusMonitor.focusVia(i,this._closeInteractionType),this._closeInteractionType=null):i.focus())}this._focusTrap&&this._focusTrap.destroy()}_focusDialogContainer(e){this._elementRef.nativeElement.focus?.(e)}_containsFocus(){let e=this._elementRef.nativeElement,i=Cc();return e===i||e.contains(i)}_initializeFocusTrap(){this._platform.isBrowser&&(this._focusTrap=this._focusTrapFactory.create(this._elementRef.nativeElement),this._document&&(this._elementFocusedBeforeDialogWasOpened=Cc()))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(i,a){}return B({type:t,selectors:[[`cdk-dialog-container`]],viewQuery:function(a,o){if(a&1&&Ze(Aa,7),a&2){let r;Z(r=J())&&(o._portalOutlet=r.first)}},hostAttrs:[`tabindex`,`-1`,1,`cdk-dialog-container`],hostVars:6,hostBindings:function(a,o){a&2&&te(`id`,o._config.id||null)(`role`,o._config.role)(`aria-modal`,o._config.ariaModal)(`aria-labelledby`,o._config.ariaLabel?null:o._ariaLabelledByQueue[0])(`aria-label`,o._config.ariaLabel)(`aria-describedby`,o._config.ariaDescribedBy||null)},features:[Ve],decls:1,vars:0,consts:[[`cdkPortalOutlet`,``]],template:function(a,o){a&1&&ot(0,e,0,0,`ng-template`,0)},dependencies:[Aa],styles:[`.cdk-dialog-container {
  display: block;
  width: 100%;
  height: 100%;
  min-height: inherit;
  max-height: inherit;
}
`],encapsulation:2,changeDetection:1})})()}return t})();var jo=class{overlayRef;config;componentInstance=null;componentRef=null;containerInstance;disableClose;closed=new L;backdropClick;keydownEvents;outsidePointerEvents;id;_detachSubscription;constructor(n,e){this.overlayRef=n,this.config=e,this.disableClose=e.disableClose,this.backdropClick=n.backdropClick(),this.keydownEvents=n.keydownEvents(),this.outsidePointerEvents=n.outsidePointerEvents(),this.id=e.id,this.keydownEvents.subscribe(i=>{i.keyCode===27&&!this.disableClose&&!xt(i)&&(i.preventDefault(),this.close(void 0,{focusOrigin:`keyboard`}))}),this.backdropClick.subscribe(()=>{!this.disableClose&&this._canClose()?this.close(void 0,{focusOrigin:`mouse`}):this.containerInstance._recaptureFocus?.()}),this._detachSubscription=n.detachments().subscribe(()=>{e.closeOnOverlayDetachments!==!1&&this.close()})}close(n,e){if(this._canClose(n)){let i=this.closed;this.containerInstance._closeInteractionType=e?.focusOrigin||`program`,this._detachSubscription.unsubscribe(),this.overlayRef.dispose(),i.next(n),i.complete(),this.componentInstance=this.containerInstance=null}}updatePosition(){return this.overlayRef.updatePosition(),this}updateSize(n=``,e=``){return this.overlayRef.updateSize({width:n,height:e}),this}addPanelClass(n){return this.overlayRef.addPanelClass(n),this}removePanelClass(n){return this.overlayRef.removePanelClass(n),this}_canClose(n){let e=this.config;return!!this.containerInstance&&(!e.closePredicate||e.closePredicate(n,e,this.componentInstance))}};var vP=new I(`DialogScrollStrategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>es(t)}});var yP=new I(`DialogData`);var _P=new I(`DefaultDialogConfig`);function xP(t){let n=N(t),e=new ee;return{valueSignal:n,get value(){return n()},change:e,ngOnDestroy(){e.complete()}}}var Fb=(()=>{class t{_injector=f(re);_defaultOptions=f(_P,{optional:!0});_parentDialog=f(t,{optional:!0,skipSelf:!0});_overlayContainer=f(cm);_idGenerator=f(Be);_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new L;_afterOpenedAtThisLevel=new L;_ariaHiddenElements=new Map;_scrollStrategy=f(vP);get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}afterAllClosed=Ka(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(ut(void 0)));open(e,i){let a=this._defaultOptions||new Ba;i=r(r({},a),i),i.id=i.id||this._idGenerator.getId(`cdk-dialog-`),i.id&&this.getDialogById(i.id);let o=this._getOverlayConfig(i),r$3=sa(this._injector,o),s=new jo(r$3,i),c=this._attachContainer(r$3,s,i);if(s.containerInstance=c,!this.openDialogs.length){let p=this._overlayContainer.getContainerElement();c._focusTrapped?c._focusTrapped.pipe(bn(1)).subscribe(()=>{this._hideNonDialogContentFromAssistiveTechnology(p)}):this._hideNonDialogContentFromAssistiveTechnology(p)}return this._attachDialogContent(e,s,c,i),this.openDialogs.push(s),s.closed.subscribe(()=>this._removeOpenDialog(s,!0)),this.afterOpened.next(s),s}closeAll(){Rb(this.openDialogs,e=>e.close())}getDialogById(e){return this.openDialogs.find(i=>i.id===e)}ngOnDestroy(){Rb(this._openDialogsAtThisLevel,e=>{e.config.closeOnDestroy===!1&&this._removeOpenDialog(e,!1)}),Rb(this._openDialogsAtThisLevel,e=>e.close()),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete(),this._openDialogsAtThisLevel=[]}_getOverlayConfig(e){let i=new aa({positionStrategy:e.positionStrategy||ts().centerHorizontally().centerVertically(),scrollStrategy:e.scrollStrategy||this._scrollStrategy(),panelClass:e.panelClass,hasBackdrop:e.hasBackdrop,direction:e.direction,minWidth:e.minWidth,minHeight:e.minHeight,maxWidth:e.maxWidth,maxHeight:e.maxHeight,width:e.width,height:e.height,disposeOnNavigation:e.closeOnNavigation,disableAnimations:e.disableAnimations});return e.backdropClass&&(i.backdropClass=e.backdropClass),i}_attachContainer(e,i,a){let o=a.injector||a.viewContainerRef?.injector,r=[{provide:Ba,useValue:a},{provide:jo,useValue:i},{provide:Zr,useValue:e}],s;a.container?typeof a.container==`function`?s=a.container:(s=a.container.type,r.push(...a.container.providers(a))):s=Pb;let c=new Ta(s,a.viewContainerRef,re.create({parent:o||this._injector,providers:r}));return e.attach(c).instance}_attachDialogContent(e,i,a,o){if(e instanceof Lt){let r$4=this._createInjector(o,i,a,void 0),s={$implicit:o.data,dialogRef:i};o.templateContext&&(s=r(r({},s),typeof o.templateContext==`function`?o.templateContext():o.templateContext)),a.attachTemplatePortal(new Hn(e,null,s,r$4))}else{let r=this._createInjector(o,i,a,this._injector),s=a.attachComponentPortal(new Ta(e,o.viewContainerRef,r,null,o.bindings));i.componentRef=s,i.componentInstance=s.instance}}_createInjector(e,i,a,o){let r=e.injector||e.viewContainerRef?.injector,s=[{provide:yP,useValue:e.data},{provide:jo,useValue:i}];return e.providers&&(typeof e.providers==`function`?s.push(...e.providers(i,e,a)):s.push(...e.providers)),e.direction&&(!r||!r.get(ft,null,{optional:!0}))&&s.push({provide:ft,useValue:xP(e.direction)}),re.create({parent:r||o,providers:s})}_removeOpenDialog(e,i){let a=this.openDialogs.indexOf(e);a>-1&&(this.openDialogs.splice(a,1),this.openDialogs.length||(this._ariaHiddenElements.forEach((o,r)=>{o?r.setAttribute(`aria-hidden`,o):r.removeAttribute(`aria-hidden`)}),this._ariaHiddenElements.clear(),i&&this._getAfterAllClosed().next()))}_hideNonDialogContentFromAssistiveTechnology(e){if(e.parentElement){let i=e.parentElement.children;for(let a=i.length-1;a>-1;a--){let o=i[a];o!==e&&o.nodeName!==`SCRIPT`&&o.nodeName!==`STYLE`&&!o.hasAttribute(`aria-live`)&&!o.hasAttribute(`popover`)&&(this._ariaHiddenElements.set(o,o.getAttribute(`aria-hidden`)),o.setAttribute(`aria-hidden`,`true`))}}}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function Rb(t,n){let e=t.length;for(;e--;)n(t[e])}var Hw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({providers:[Fb],imports:[pi,Fo,Io,Fo]})}return t})();var Cm=class{viewContainerRef;injector;id;role=`dialog`;panelClass=``;hasBackdrop=!0;backdropClass=``;disableClose=!1;closePredicate;width=``;height=``;minWidth;minHeight;maxWidth;maxHeight;position;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus=`first-tabbable`;restoreFocus=!0;delayFocusTrap=!0;scrollStrategy;closeOnNavigation=!0;enterAnimationDuration;exitAnimationDuration;bindings};var Lb=`mdc-dialog--open`;var Uw=`mdc-dialog--opening`;var $w=`mdc-dialog--closing`;var CP=150;var SP=75;var wP=(()=>{class t extends Pb{_animationStateChanged=new ee;_animationsEnabled=!Ue();_actionSectionCount=0;_hostElement=this._elementRef.nativeElement;_enterAnimationDuration=this._animationsEnabled?Ww(this._config.enterAnimationDuration)??CP:0;_exitAnimationDuration=this._animationsEnabled?Ww(this._config.exitAnimationDuration)??SP:0;_animationTimer=null;_contentAttached(){super._contentAttached(),this._startOpenAnimation()}_startOpenAnimation(){this._animationStateChanged.emit({state:`opening`,totalTime:this._enterAnimationDuration}),this._animationsEnabled?(this._hostElement.style.setProperty(Gw,`${this._enterAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add(Uw,Lb)),this._waitForAnimationToComplete(this._enterAnimationDuration,this._finishDialogOpen)):(this._hostElement.classList.add(Lb),Promise.resolve().then(()=>this._finishDialogOpen()))}_startExitAnimation(){this._animationStateChanged.emit({state:`closing`,totalTime:this._exitAnimationDuration}),this._hostElement.classList.remove(Lb),this._animationsEnabled?(this._hostElement.style.setProperty(Gw,`${this._exitAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add($w)),this._waitForAnimationToComplete(this._exitAnimationDuration,this._finishDialogClose)):Promise.resolve().then(()=>this._finishDialogClose())}_updateActionSectionCount(e){this._actionSectionCount+=e,this._changeDetectorRef.markForCheck()}_finishDialogOpen=()=>{this._clearAnimationClasses(),this._openAnimationDone(this._enterAnimationDuration)};_finishDialogClose=()=>{this._clearAnimationClasses(),this._animationStateChanged.emit({state:`closed`,totalTime:this._exitAnimationDuration})};_clearAnimationClasses(){this._hostElement.classList.remove(Uw,$w)}_waitForAnimationToComplete(e,i){this._animationTimer!==null&&clearTimeout(this._animationTimer),this._animationTimer=setTimeout(i,e)}_requestAnimationFrame(e){this._ngZone.runOutsideAngular(()=>{typeof requestAnimationFrame==`function`?requestAnimationFrame(e):e()})}_captureInitialFocus(){this._config.delayFocusTrap||this._trapFocus()}_openAnimationDone(e){this._config.delayFocusTrap&&this._trapFocus(),this._animationStateChanged.next({state:`opened`,totalTime:e})}ngOnDestroy(){super.ngOnDestroy(),this._animationTimer!==null&&clearTimeout(this._animationTimer)}attachComponentPortal(e){let i=super.attachComponentPortal(e);return i.location.nativeElement.classList.add(`mat-mdc-dialog-component-host`),i}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵcmp=(function(){function e(i,a){}return B({type:t,selectors:[[`mat-dialog-container`]],hostAttrs:[`tabindex`,`-1`,1,`mat-mdc-dialog-container`,`mdc-dialog`],hostVars:10,hostBindings:function(a,o){a&2&&(qt(`id`,o._config.id),te(`aria-modal`,o._config.ariaModal)(`role`,o._config.role)(`aria-labelledby`,o._config.ariaLabel?null:o._ariaLabelledByQueue[0])(`aria-label`,o._config.ariaLabel)(`aria-describedby`,o._config.ariaDescribedBy||null),q(`_mat-animation-noopable`,!o._animationsEnabled)(`mat-mdc-dialog-container-with-actions`,o._actionSectionCount>0))},features:[Ve],decls:3,vars:0,consts:[[1,`mat-mdc-dialog-inner-container`,`mdc-dialog__container`],[1,`mat-mdc-dialog-surface`,`mdc-dialog__surface`],[`cdkPortalOutlet`,``]],template:function(a,o){a&1&&(l(0,`div`,0)(1,`div`,1),ot(2,e,0,0,`ng-template`,2),d()())},dependencies:[Aa],styles:[`.mat-mdc-dialog-container {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var Gw=`--mat-dialog-transition-duration`;function Ww(t){return t==null?null:typeof t==`number`?t:t.endsWith(`ms`)?si(t.substring(0,t.length-2)):t.endsWith(`s`)?si(t.substring(0,t.length-1))*1e3:t===`0`?0:null}var xm=(function(t){return t[t.OPEN=0]=`OPEN`,t[t.CLOSING=1]=`CLOSING`,t[t.CLOSED=2]=`CLOSED`,t})(xm||{});var Xc=class{_ref;_config;_containerInstance;componentInstance;componentRef=null;disableClose;id;_afterOpened=new Yn(1);_beforeClosed=new Yn(1);_result;_closeFallbackTimeout;_state=xm.OPEN;_closeInteractionType;constructor(n,e,i){this._ref=n,this._config=e,this._containerInstance=i,this.disableClose=e.disableClose,this.id=n.id,n.addPanelClass(`mat-mdc-dialog-panel`),i._animationStateChanged.pipe(Qe(a=>a.state===`opened`),bn(1)).subscribe(()=>{this._afterOpened.next(),this._afterOpened.complete()}),i._animationStateChanged.pipe(Qe(a=>a.state===`closed`),bn(1)).subscribe(()=>{clearTimeout(this._closeFallbackTimeout),this._finishDialogClose()}),n.overlayRef.detachments().subscribe(()=>{this._beforeClosed.next(this._result),this._beforeClosed.complete(),this._finishDialogClose()}),Dt(this.backdropClick(),this.keydownEvents().pipe(Qe(a=>a.keyCode===27&&!this.disableClose&&!xt(a)))).subscribe(a=>{this.disableClose||(a.preventDefault(),qw(this,a.type===`keydown`?`keyboard`:`mouse`))})}close(n){let e=this._config.closePredicate;e&&!e(n,this._config,this.componentInstance)||(this._result=n,this._containerInstance._animationStateChanged.pipe(Qe(i=>i.state===`closing`),bn(1)).subscribe(i=>{this._beforeClosed.next(n),this._beforeClosed.complete(),this._ref.overlayRef.detachBackdrop(),this._closeFallbackTimeout=setTimeout(()=>this._finishDialogClose(),i.totalTime+100)}),this._state=xm.CLOSING,this._containerInstance._startExitAnimation())}afterOpened(){return this._afterOpened}afterClosed(){return this._ref.closed}beforeClosed(){return this._beforeClosed}backdropClick(){return this._ref.backdropClick}keydownEvents(){return this._ref.keydownEvents}updatePosition(n){let e=this._ref.config.positionStrategy;return n&&(n.left||n.right)?n.left?e.left(n.left):e.right(n.right):e.centerHorizontally(),n&&(n.top||n.bottom)?n.top?e.top(n.top):e.bottom(n.bottom):e.centerVertically(),this._ref.updatePosition(),this}updateSize(n=``,e=``){return this._ref.updateSize(n,e),this}addPanelClass(n){return this._ref.addPanelClass(n),this}removePanelClass(n){return this._ref.removePanelClass(n),this}getState(){return this._state}_finishDialogClose(){this._state=xm.CLOSED,this._ref.close(this._result,{focusOrigin:this._closeInteractionType}),this.componentInstance=null}};function qw(t,n,e){return t._closeInteractionType=n,t.close(e)}var Zc=new I(`MatMdcDialogData`);var EP=new I(`mat-mdc-dialog-default-options`);var DP=new I(`mat-mdc-dialog-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>es(t)}});var zo=(()=>{class t{_defaultOptions=f(EP,{optional:!0});_scrollStrategy=f(DP);_parentDialog=f(t,{optional:!0,skipSelf:!0});_idGenerator=f(Be);_injector=f(re);_dialog=f(Fb);_animationsDisabled=Ue();_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new L;_afterOpenedAtThisLevel=new L;dialogConfigClass=Cm;_dialogRefConstructor;_dialogContainerType;_dialogDataToken;get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}afterAllClosed=Ka(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(ut(void 0)));constructor(){this._dialogRefConstructor=Xc,this._dialogContainerType=wP,this._dialogDataToken=Zc}open(e,i){let a;i=r(r({},this._defaultOptions||new Cm),i),i.id=i.id||this._idGenerator.getId(`mat-mdc-dialog-`),i.scrollStrategy=i.scrollStrategy||this._scrollStrategy();let o=this._dialog.open(e,s(r({},i),{positionStrategy:ts(this._injector).centerHorizontally().centerVertically(),disableClose:!0,closePredicate:void 0,closeOnDestroy:!1,closeOnOverlayDetachments:!1,disableAnimations:this._animationsDisabled||i.enterAnimationDuration?.toLocaleString()===`0`||i.exitAnimationDuration?.toString()===`0`,container:{type:this._dialogContainerType,providers:()=>[{provide:this.dialogConfigClass,useValue:i},{provide:Ba,useValue:i}]},templateContext:()=>({dialogRef:a}),providers:(r,s,c)=>(a=new this._dialogRefConstructor(r,i,c),a.updatePosition(i?.position),[{provide:this._dialogContainerType,useValue:c},{provide:this._dialogDataToken,useValue:s.data},{provide:this._dialogRefConstructor,useValue:a},{provide:jo,useValue:null}])}));return a.componentRef=o.componentRef,a.componentInstance=o.componentInstance,this.openDialogs.push(a),this.afterOpened.next(a),a.afterClosed().subscribe(()=>{let r=this.openDialogs.indexOf(a);r>-1&&(this.openDialogs.splice(r,1),this.openDialogs.length||this._getAfterAllClosed().next())}),a}closeAll(){this._closeDialogs(this.openDialogs)}getDialogById(e){return this.openDialogs.find(i=>i.id===e)}ngOnDestroy(){this._closeDialogs(this._openDialogsAtThisLevel),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete()}_closeDialogs(e){let i=e.length;for(;i--;)e[i].close()}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Sm=(()=>{class t{dialogRef=f(Xc,{optional:!0});_elementRef=f(W);_dialog=f(zo);ariaLabel;type=`button`;dialogResult;_matDialogClose;ngOnInit(){this.dialogRef||(this.dialogRef=Kw(this._elementRef,this._dialog.openDialogs))}ngOnChanges(e){let i=e._matDialogClose;i&&(this.dialogResult=i.currentValue)}_onButtonClick(e){this._elementRef.nativeElement.getAttribute(`aria-disabled`)!==`true`&&qw(this.dialogRef,e.screenX===0&&e.screenY===0?`keyboard`:`mouse`,this.dialogResult)}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`mat-dialog-close`,``],[``,`matDialogClose`,``]],hostVars:2,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._onButtonClick(r)}),i&2&&te(`aria-label`,a.ariaLabel||null)(`type`,a.type)},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],type:`type`,dialogResult:[0,`mat-dialog-close`,`dialogResult`],_matDialogClose:[0,`matDialogClose`,`_matDialogClose`]},exportAs:[`matDialogClose`],features:[bt]})}return t})();var Yw=(()=>{class t{_dialogRef=f(Xc,{optional:!0});_elementRef=f(W);_dialog=f(zo);ngOnInit(){this._dialogRef||(this._dialogRef=Kw(this._elementRef,this._dialog.openDialogs)),this._dialogRef&&Promise.resolve().then(()=>{this._onAdd()})}ngOnDestroy(){this._dialogRef?._containerInstance&&Promise.resolve().then(()=>{this._onRemove()})}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t})}return t})();var wm=(()=>{class t extends Yw{id=f(Be).getId(`mat-mdc-dialog-title-`);_onAdd(){this._dialogRef._containerInstance?._addAriaLabelledBy?.(this.id)}_onRemove(){this._dialogRef?._containerInstance?._removeAriaLabelledBy?.(this.id)}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=Y({type:t,selectors:[[``,`mat-dialog-title`,``],[``,`matDialogTitle`,``]],hostAttrs:[1,`mat-mdc-dialog-title`,`mdc-dialog__title`],hostVars:1,hostBindings:function(i,a){i&2&&qt(`id`,a.id)},inputs:{id:`id`},exportAs:[`matDialogTitle`],features:[Ve]})}return t})();var Em=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[``,`mat-dialog-content`,``],[`mat-dialog-content`],[``,`matDialogContent`,``]],hostAttrs:[1,`mat-mdc-dialog-content`,`mdc-dialog__content`],features:[nc([Uc])]})}return t})();var Dm=(()=>{class t extends Yw{align;_onAdd(){this._dialogRef._containerInstance?._updateActionSectionCount?.(1)}_onRemove(){this._dialogRef._containerInstance?._updateActionSectionCount?.(-1)}static ɵfac=(()=>{let e;return function(a){return(e||(e=Nt(t)))(a||t)}})();static ɵdir=Y({type:t,selectors:[[``,`mat-dialog-actions`,``],[`mat-dialog-actions`],[``,`matDialogActions`,``]],hostAttrs:[1,`mat-mdc-dialog-actions`,`mdc-dialog__actions`],hostVars:6,hostBindings:function(i,a){i&2&&q(`mat-mdc-dialog-actions-align-start`,a.align===`start`)(`mat-mdc-dialog-actions-align-center`,a.align===`center`)(`mat-mdc-dialog-actions-align-end`,a.align===`end`)},inputs:{align:`align`},features:[Ve]})}return t})();function Kw(t,n){let e=t.nativeElement.parentElement;for(;e&&!e.classList.contains(`mat-mdc-dialog-container`);)e=e.parentElement;return e?n.find(i=>i.id===e.id):null}var km=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({providers:[zo],imports:[Hw,pi,Fo,we]})}return t})();var Im=class t{http=f(Di);getStatus(){return Rt(this.http.get(this.endpoint(`/status`)))}getComponentUsage(n){return Rt(this.http.get(this.endpoint(`/components/usage`),{params:{scope:n}}))}getComponentCandidates(n){return Rt(this.http.get(this.endpoint(`/components/candidates`),{params:r({scope:n.scope,basis:n.basis,offset:n.offset,limit:n.limit},n.selector)}))}getEvaluationChanges(n,e){return Rt(this.http.get(this.endpoint(`/evaluations/changes`),{params:{after:n,limit:e}}))}getElites(){return Rt(this.http.get(this.endpoint(`/elites`)))}getEliteHistory(){return Rt(this.http.get(this.endpoint(`/elites/history`)))}getCandidate(n){return Rt(this.http.get(this.endpoint(`/candidates/${encodeURIComponent(n)}`)))}getSearchSpace(){return Rt(this.http.get(this.endpoint(`/search-space`)))}getArtifacts(){return Rt(this.http.get(this.endpoint(`/artifacts`)))}artifactUrl(n){return this.endpoint(`/artifacts/${encodeURIComponent(n)}`)}endpoint(n){return`${kn.apiBaseUrl}/api/autoconfig${n}`}static ɵfac=function(e){return new(e||t)};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})};var Ho=class{static evaluationCost(n){let e=[],i=[];for(let a of n){if(a.state!==`SUCCEEDED`||typeof a.cost!=`number`||!Number.isFinite(a.cost))continue;let o={x:a.id,y:a.cost,custom:{configurationId:a.configurationId,instanceName:a.instanceName,timeSeconds:a.timeSeconds}};(a.slow?i:e).push(o)}return{options:this.baseOptions(`Evaluation cost`,`Successful IRACE costs. Lower is better.`,{allowDecimals:!1,title:{text:`Evaluation ID`}},{title:{text:`IRACE cost`}},[{type:`scatter`,name:`Succeeded`,data:e,color:`#00aab5`,marker:{radius:2.5},turboThreshold:0},{type:`scatter`,name:`Slow`,data:i,color:`#ed8e00`,marker:{radius:3.5,symbol:`diamond`},turboThreshold:0}],`<b>{series.name}</b><br/>Evaluation: {point.x}<br/>Cost: {point.y}<br/>Configuration: {point.custom.configurationId}<br/>Instance: {point.custom.instanceName}`)}}static evaluationActivity(n){let e=n.filter(s=>s.finishedAt&&s.state!==`RUNNING`).slice().sort((s,c)=>{let p=Date.parse(s.finishedAt)-Date.parse(c.finishedAt);return p===0?s.id-c.id:p}),i={SUCCEEDED:0,REJECTED:0,FAILED:0},a=[],o=[],r=[];for(let s of e){let c=s.state;if(c!==`SUCCEEDED`&&c!==`REJECTED`&&c!==`FAILED`)continue;i[c]++;let p=[Date.parse(s.finishedAt),i[c]];c===`SUCCEEDED`?a.push(p):c===`REJECTED`?o.push(p):r.push(p)}return{options:this.baseOptions(`Evaluation activity`,`Cumulative terminal evaluations over wall-clock time.`,{type:`datetime`,title:{text:`Completion time`}},{allowDecimals:!1,min:0,title:{text:`Evaluations`}},[{type:`line`,name:`Succeeded`,data:a,color:`#079447`,marker:{enabled:!1}},{type:`line`,name:`Rejected`,data:o,color:`#ed8e00`,marker:{enabled:!1}},{type:`line`,name:`Failed`,data:r,color:`#d32f2f`,marker:{enabled:!1}}],`<b>{series.name}</b><br/>{point.x:%Y-%m-%d %H:%M:%S}<br/>Count: {point.y}`)}}static eliteRanks(n){let e=n?.iterations??[],i=[];for(let o of e)for(let r of o.elites)i.includes(r.configurationId)||i.push(r.configurationId);let a=[];for(let o of i){let r=[];for(let s of e){let c=null;for(let p of s.elites)if(p.configurationId===o){c=p.position;break}r.push([s.iteration,c])}a.push({type:`line`,name:o,data:r,connectNulls:!1,marker:{enabled:!0,radius:2.5},step:`left`})}return{options:this.baseOptions(`Elite rank evolution`,`Elite positions at completed IRACE race checkpoints.`,{allowDecimals:!1,title:{text:`Race checkpoint`}},{allowDecimals:!1,min:1,reversed:!0,title:{text:`Elite position`}},a,`<b>{series.name}</b><br/>Checkpoint: {point.x}<br/>Position: {point.y}`)}}static instanceSummaries(n){let e=new Map;for(let i of n){let a=e.get(i.instanceName)??{instanceName:i.instanceName,evaluations:0,bestCost:null,issues:0,slow:0},o=a.bestCost;i.state===`SUCCEEDED`&&typeof i.cost==`number`&&Number.isFinite(i.cost)&&(o===null||i.cost<o)&&(o=i.cost),e.set(i.instanceName,{instanceName:a.instanceName,evaluations:a.evaluations+1,bestCost:o,issues:a.issues+(i.state===`REJECTED`||i.state===`FAILED`?1:0),slow:a.slow+(i.slow?1:0)})}return[...e.values()].sort((i,a)=>a.issues-i.issues||i.instanceName.localeCompare(a.instanceName))}static baseOptions(n,e,i,a,o,r){return{accessibility:{enabled:!0,description:e},boost:{enabled:!0,useGPUTranslations:!0,seriesThreshold:20},chart:{animation:!1,height:330,spacing:[12,12,12,12]},credits:{enabled:!1},exporting:{enabled:!0},legend:{enabled:!0,maxHeight:72},title:{text:n,style:{display:`none`}},xAxis:i,yAxis:a,tooltip:{headerFormat:``,pointFormat:r},plotOptions:{series:{animation:!1,connectNulls:!1}},series:o}}};var kP=500;var IP=2e3;var MP=1e4;var NP=1e4;var Vb=`mork.autoconfig.alertSeen.v1`;var ja=class t{client=f(Im);destroyRef=f(Te);statusSignal=N(null);componentUsageSignal=N(null);componentScopeSignal=N(`ALL`);componentErrorSignal=N(null);syncStateSignal=N(`detecting`);latestErrorSignal=N(null);evaluationsSignal=N([]);elitesSignal=N(null);eliteHistorySignal=N(null);searchSpaceSignal=N(null);artifactsSignal=N(null);alertsSignal=N([]);seenAlertRevisionSignal=N(0);runFailureSeenSignal=N(!1);lastUpdatedSignal=N(null);evaluationMap=new Map;candidateCache=new Map;pollTimer=null;currentRunId=null;evaluationRevision=0;eliteMarker=null;componentMarker=null;componentRequest=0;searchSpaceLoaded=!1;lastArtifactFetch=0;lastArtifactPhase=null;started=!1;destroyed=!1;status=this.statusSignal.asReadonly();componentUsage=this.componentUsageSignal.asReadonly();componentScope=this.componentScopeSignal.asReadonly();componentError=this.componentErrorSignal.asReadonly();syncState=this.syncStateSignal.asReadonly();latestError=this.latestErrorSignal.asReadonly();evaluations=this.evaluationsSignal.asReadonly();elites=this.elitesSignal.asReadonly();eliteHistory=this.eliteHistorySignal.asReadonly();searchSpace=this.searchSpaceSignal.asReadonly();artifacts=this.artifactsSignal.asReadonly();alerts=this.alertsSignal.asReadonly();lastUpdated=this.lastUpdatedSignal.asReadonly();viewMode=ne(()=>{let n=this.statusSignal();return n?n.mode===`DISABLED`?`standard`:n.role===`WORKER`?`worker`:`tuning`:this.syncStateSignal()===`retrying`?`unavailable`:`detecting`});connectionLabel=ne(()=>{switch(this.syncStateSignal()){case`detecting`:return`Detecting run mode`;case`synchronizing`:return`Synchronizing`;case`live`:return`Live`;case`retrying`:return`Reconnecting`}});budgetPercent=ne(()=>{let n=this.statusSignal()?.budget;return!n||n.maximum<=0?0:Math.min(100,n.used/n.maximum*100)});evaluationCostChart=ne(()=>Ho.evaluationCost(this.evaluationsSignal()));evaluationActivityChart=ne(()=>Ho.evaluationActivity(this.evaluationsSignal()));eliteRankChart=ne(()=>Ho.eliteRanks(this.eliteHistorySignal()));instanceSummaries=ne(()=>Ho.instanceSummaries(this.evaluationsSignal()));recentEvaluations=ne(()=>this.evaluationsSignal().slice(-100).reverse());unseenAlertCount=ne(()=>{let n=0;for(let e of this.alertsSignal())e.revision===null?this.runFailureSeenSignal()||n++:e.revision>this.seenAlertRevisionSignal()&&n++;return n});constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.clearPollTimer()})}start(){this.started||(this.started=!0,this.schedulePoll(0))}retryNow(){this.clearPollTimer(),this.schedulePoll(0)}markAlertsSeen(){this.seenAlertRevisionSignal.set(this.evaluationRevision),this.statusSignal()?.failure&&this.runFailureSeenSignal.set(!0),this.persistSeenAlerts()}async loadCandidate(n){let e=this.candidateCache.get(n);if(e)return e;let i=this.currentRunId,a=await this.client.getCandidate(n);if(i!==this.currentRunId)throw new Error(`Candidate belongs to an obsolete autoconfig run`);return(this.statusSignal()?.state===`COMPLETED`||this.statusSignal()?.state===`FAILED`)&&this.candidateCache.set(n,a),a}selectComponentScope(n){if(n===this.componentScopeSignal())return;this.componentScopeSignal.set(n),this.componentUsageSignal.set(null),this.componentMarker=null;let e=this.statusSignal();e&&this.synchronizeComponentUsage(e)}loadComponentCandidates(n){return this.client.getComponentCandidates(n)}refreshComponentUsage(){this.componentMarker=null;let n=this.statusSignal();n&&this.synchronizeComponentUsage(n)}async refreshArtifacts(){let n=this.statusSignal();if(!n?.runId||n.role!==`COORDINATOR`){this.artifactsSignal.set(null);return}try{this.artifactsSignal.set(await this.client.getArtifacts()),this.lastArtifactFetch=Date.now()}catch(e){if(this.isNotFound(e)){this.artifactsSignal.set({runId:n.runId,artifacts:[]}),this.lastArtifactFetch=Date.now();return}throw e}}artifactUrl(n){return this.client.artifactUrl(n)}schedulePoll(n){this.destroyed||(this.pollTimer=setTimeout(()=>{this.poll()},n))}async poll(){this.pollTimer=null,this.syncStateSignal.set(`synchronizing`);try{let n=await this.client.getStatus();this.adoptStatus(n),n.mode!==`DISABLED`&&n.role===`COORDINATOR`&&n.runId&&await this.synchronizeRun(n),this.latestErrorSignal.set(null),this.lastUpdatedSignal.set(new Date),this.syncStateSignal.set(`live`)}catch(n){this.latestErrorSignal.set(this.errorMessage(n)),this.syncStateSignal.set(`retrying`)}finally{let n=this.statusSignal(),e=n?.state===`PREPARING`||n?.state===`RUNNING`;this.schedulePoll(e?IP:MP)}}adoptStatus(n){n.runId!==this.currentRunId?this.resetRun(n.runId):n.latestEvaluationRevision<this.evaluationRevision&&this.resetRun(n.runId),this.statusSignal.set(n),this.publishAlerts()}async synchronizeRun(n){await this.synchronizeEvaluations(n),await this.synchronizeElites(n),await this.synchronizeSearchSpace(n),await this.synchronizeComponentUsage(n),await this.synchronizeArtifacts(n)}async synchronizeEvaluations(n){let e=!1;for(;this.evaluationRevision<n.latestEvaluationRevision;){let i=await this.client.getEvaluationChanges(this.evaluationRevision,kP);if(i.runId!==n.runId)throw new Error(`Evaluation changes belong to a different autoconfig run`);for(let a of i.changes)this.evaluationMap.set(a.evaluation.id,{revision:a.revision,evaluation:a.evaluation}),e=!0;if(i.nextRevision<=this.evaluationRevision)break;this.evaluationRevision=i.nextRevision}if(e){let i=[...this.evaluationMap.values()].sort((a,o)=>a.evaluation.id-o.evaluation.id).map(a=>a.evaluation);this.evaluationsSignal.set(i),this.publishAlerts()}}async synchronizeElites(n){let e=`${n.irace.iteration??``}|${n.irace.updatedAt??``}|${n.irace.finalSnapshot}`;if(e===this.eliteMarker)return;let[i,a]=await Promise.all([this.client.getElites(),this.client.getEliteHistory()]);if(i.runId!==n.runId||a.runId!==n.runId)throw new Error(`Elite snapshots belong to a different autoconfig run`);this.elitesSignal.set(i),this.eliteHistorySignal.set(a),this.eliteMarker=e}async synchronizeSearchSpace(n){if(!(n.mode!==`AUTOCONFIG`||n.generatedParameterCount<=0||this.searchSpaceLoaded))try{this.searchSpaceSignal.set(await this.client.getSearchSpace()),this.searchSpaceLoaded=!0}catch(e){if(!this.isNotFound(e))throw e}}async synchronizeComponentUsage(n){if(n.mode!==`AUTOCONFIG`||n.role!==`COORDINATOR`||!n.runId)return;let e=this.componentScopeSignal(),i=`${n.runId}|${e}|${n.latestEvaluationRevision}|${n.irace.updatedAt??``}|${n.irace.finalSnapshot}`;if(i===this.componentMarker)return;let a=++this.componentRequest;try{let o=await this.client.getComponentUsage(e);if(this.destroyed||a!==this.componentRequest||e!==this.componentScopeSignal())return;if(o.runId!==this.currentRunId||o.scope!==e)throw new Error(`Component usage belongs to a different run or scope`);this.componentUsageSignal.set(o),this.componentMarker=i,this.componentErrorSignal.set(null)}catch(o){a===this.componentRequest&&!this.destroyed&&this.componentErrorSignal.set(this.errorMessage(o))}}async synchronizeArtifacts(n){let e=Date.now();n.phase===this.lastArtifactPhase&&e-this.lastArtifactFetch<NP||(await this.refreshArtifacts(),this.lastArtifactPhase=n.phase)}publishAlerts(){let n=[],e=this.statusSignal();e?.failure&&n.push({key:`run:${e.runId}:failure`,revision:null,severity:`critical`,title:`Tuning run failed`,message:e.failure.message??e.failure.type,timestamp:e.finishedAt,evaluationId:null});for(let i of this.evaluationMap.values()){let a=i.evaluation;if(a.state!==`FAILED`&&a.state!==`REJECTED`&&!a.slow)continue;let o=a.state===`FAILED`,r=a.state===`REJECTED`,s=o?`Evaluation failed`:r?`Evaluation rejected`:`Slow evaluation`,c=a.message??a.reasonCode??a.instanceName;n.push({key:`evaluation:${a.id}`,revision:i.revision,severity:o?`critical`:`warning`,title:s,message:a.slow&&r?`${c} \xB7 also slow`:c,timestamp:a.finishedAt,evaluationId:a.id})}n.sort((i,a)=>i.revision===null?-1:a.revision===null?1:a.revision-i.revision),this.alertsSignal.set(n)}resetRun(n){this.currentRunId=n,this.evaluationRevision=0,this.evaluationMap.clear(),this.candidateCache.clear(),this.componentRequest++,this.componentUsageSignal.set(null),this.componentScopeSignal.set(`ALL`),this.componentErrorSignal.set(null),this.componentMarker=null,this.evaluationsSignal.set([]),this.elitesSignal.set(null),this.eliteHistorySignal.set(null),this.searchSpaceSignal.set(null),this.artifactsSignal.set(null),this.alertsSignal.set([]),this.eliteMarker=null,this.searchSpaceLoaded=!1,this.lastArtifactFetch=0,this.lastArtifactPhase=null,this.restoreSeenAlerts(n)}restoreSeenAlerts(n){if(this.seenAlertRevisionSignal.set(0),this.runFailureSeenSignal.set(!1),!!n)try{let e=localStorage.getItem(Vb);if(!e)return;let i=JSON.parse(e);i.runId===n&&(this.seenAlertRevisionSignal.set(typeof i.revision==`number`?i.revision:0),this.runFailureSeenSignal.set(i.runFailureSeen===!0))}catch{localStorage.removeItem(Vb)}}persistSeenAlerts(){if(!this.currentRunId)return;let n={runId:this.currentRunId,revision:this.seenAlertRevisionSignal(),runFailureSeen:this.runFailureSeenSignal()};localStorage.setItem(Vb,JSON.stringify(n))}clearPollTimer(){this.pollTimer&&(clearTimeout(this.pollTimer),this.pollTimer=null)}isNotFound(n){return n instanceof Ei&&n.status===404}errorMessage(n){return n instanceof Ei?`Backend request failed (${n.status||`network error`})`:n instanceof Error?n.message:String(n)}static ɵfac=function(e){return new(e||t)};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})};var TP=(t,n)=>n[0];function AP(t,n){if(t&1&&(l(0,`dt`),u(1),d(),l(2,`dd`),u(3),d()),t&2){let e=n.$implicit;h(),w(e[0]),h(2),w(e[1])}}function OP(t,n){t&1&&(l(0,`dd`),u(1,`No parameters were reported.`),d())}function RP(t,n){if(t&1&&(l(0,`h3`),u(1,`Decoded algorithm`),d(),l(2,`pre`),u(3),U(4,`json`),d()),t&2){let e=y();h(3),w(_e(4,1,e.algorithm))}}function PP(t,n){if(t&1&&(l(0,`p`,1),u(1),d()),t&2){let e=y();h(),w(e.decodeError)}}function FP(t,n){if(t&1&&(l(0,`section`,4)(1,`span`)(2,`strong`),u(3),d(),u(4,` succeeded`),d(),l(5,`span`)(6,`strong`),u(7),d(),u(8,` running`),d(),l(9,`span`)(10,`strong`),u(11),d(),u(12,` rejected`),d(),l(13,`span`)(14,`strong`),u(15),d(),u(16,` failed`),d(),l(17,`span`)(18,`strong`),u(19),d(),u(20,` slow`),d()(),l(21,`h3`),u(22,`Parameters`),d(),l(23,`dl`),be(24,AP,4,2,null,null,TP,!1,OP,2,0,`dd`),d(),D(27,RP,5,3),D(28,PP,2,1,`p`,1)),t&2){let e=n,i=y();h(3),w(e.evaluations.succeeded),h(4),w(e.evaluations.running),h(4),w(e.evaluations.rejected),h(4),w(e.evaluations.failed),h(4),w(e.evaluations.slow),h(5),ve(i.parameterEntries(e)),h(3),k(e.algorithm?27:-1),h(),k(e.decodeError?28:-1)}}function LP(t,n){if(t&1&&(l(0,`p`,1),u(1),d()),t&2){let e=y();h(),w(e.error())}}function VP(t,n){t&1&&(l(0,`p`),u(1,`Loading configuration…`),d())}var us=class t{store=f(ja);configurationId=f(Zc);candidate=N(null);error=N(null);ngOnInit(){this.store.loadCandidate(this.configurationId).then(n=>this.candidate.set(n)).catch(n=>this.error.set(n instanceof Error?n.message:String(n)))}parameterEntries(n){return Object.entries(n.parameters)}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-candidate-dialog`]],decls:9,vars:2,consts:[[`mat-dialog-title`,``],[`role`,`alert`,1,`error`],[`align`,`end`],[`mat-button`,``,`mat-dialog-close`,``,`type`,`button`],[`aria-label`,`Candidate evaluation counts`,1,`counts`]],template:function(e,i){if(e&1&&(l(0,`h2`,0),u(1),d(),l(2,`mat-dialog-content`),D(3,FP,29,8)(4,LP,2,1,`p`,1)(5,VP,2,0,`p`),d(),l(6,`mat-dialog-actions`,2)(7,`button`,3),u(8,`Close`),d()()),e&2){let a;h(),H(`Configuration `,i.configurationId),h(2),k((a=i.candidate())?3:i.error()?4:5,a)}},dependencies:[Ot,en,km,Sm,wm,Dm,Em,So],styles:[`mat-dialog-content[_ngcontent-%COMP%]{min-width:min(42rem,80vw)}.counts[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.5rem 1.25rem;margin-bottom:1.25rem}dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(10rem,1fr) minmax(8rem,1fr);gap:.35rem 1rem}dt[_ngcontent-%COMP%]{overflow-wrap:anywhere;color:var(--%NS%mat-sys-on-surface-variant)}dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}pre[_ngcontent-%COMP%]{max-height:22rem;overflow:auto;padding:1rem;border-radius:.5rem;background:var(--%NS%mat-sys-surface-container);white-space:pre-wrap}.error[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}`]})};var BP=()=>[25,50,100];var jP=(t,n)=>n.id;function zP(t,n){t&1&&(l(0,`span`,17),u(1,`slow`),d())}function HP(t,n){if(t&1){let e=X();l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`)(4,`button`,14),C(`click`,function(){let a=A(e).$implicit;return O(y().candidateSelected.emit(a.configurationId))}),u(5),d()(),l(6,`td`,15),u(7),d(),l(8,`td`)(9,`span`,16),u(10),d()(),l(11,`td`),u(12),U(13,`number`),d(),l(14,`td`),u(15),U(16,`number`),D(17,zP,2,0,`span`,17),d(),l(18,`td`),u(19),U(20,`date`),d(),l(21,`td`,18),u(22),d()()}if(t&2){let e=n.$implicit,i=y();q(`issue`,e.state===`FAILED`||e.state===`REJECTED`),h(2),w(e.id),h(3),H(` `,e.configurationId,` `),h(),S(`title`,e.instanceName),h(),w(e.instanceName),h(2),tt(i.stateClass(e.state)),h(),w(e.state),h(2),w(e.cost===null?`—`:Ie(13,14,e.cost,`1.0-5`)),h(3),H(` `,e.timeSeconds===null?`—`:Ie(16,17,e.timeSeconds,`1.2-2`)+` s`,` `),h(2),k(e.slow?17:-1),h(2),H(` `,e.finishedAt?Ie(20,20,e.finishedAt,`HH:mm:ss`):`Running`,` `),h(3),w(e.message??e.reasonCode??`—`)}}function UP(t,n){t&1&&(l(0,`tr`)(1,`td`,19),u(2,`No evaluations match these filters.`),d()())}var Mm=class t{evaluations=zt.required();forceIssues=zt(!1);candidateSelected=eu();search=N(``);state=N(`ALL`);slowOnly=N(!1);issueOnly=N(!1);pageIndex=N(0);pageSize=N(25);sortColumn=N(`id`);sortAscending=N(!1);filtered=ne(()=>{let n=this.search().trim().toLowerCase(),e=this.state(),i=this.slowOnly(),a=this.issueOnly()||this.forceIssues(),o=[];for(let c of this.evaluations())e!==`ALL`&&c.state!==e||i&&!c.slow||a&&c.state!==`FAILED`&&c.state!==`REJECTED`&&!c.slow||n&&!c.configurationId.toLowerCase().includes(n)&&!c.instanceName.toLowerCase().includes(n)&&!String(c.id).includes(n)||o.push(c);let r=this.sortColumn(),s=this.sortAscending()?1:-1;return o.sort((c,p)=>this.compare(c[r],p[r])*s),o});page=ne(()=>{let n=this.pageIndex()*this.pageSize();return this.filtered().slice(n,n+this.pageSize())});updateSearch(n){this.search.set(n),this.pageIndex.set(0)}updateState(n){this.state.set(n),this.pageIndex.set(0)}updateSlowOnly(n){this.slowOnly.set(n),this.pageIndex.set(0)}updateIssueOnly(n){this.issueOnly.set(n),this.pageIndex.set(0)}updatePage(n){this.pageIndex.set(n.pageIndex),this.pageSize.set(n.pageSize)}sortBy(n){this.sortColumn()===n?this.sortAscending.update(e=>!e):(this.sortColumn.set(n),this.sortAscending.set(!0)),this.pageIndex.set(0)}sortIndicator(n){return this.sortColumn()!==n?``:this.sortAscending()?` ↑`:` ↓`}stateClass(n){return`state-${n.toLowerCase()}`}compare(n,e){return n===e?0:n===null?1:e===null?-1:typeof n==`number`&&typeof e==`number`?n-e:String(n).localeCompare(String(e),void 0,{numeric:!0})}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-evaluation-table`]],inputs:{evaluations:[1,`evaluations`],forceIssues:[1,`forceIssues`]},outputs:{candidateSelected:`candidateSelected`},decls:54,vars:17,consts:[[`aria-label`,`Evaluation filters`,1,`filters`],[`appearance`,`outline`,`subscriptSizing`,`dynamic`],[`matInput`,``,`type`,`search`,`placeholder`,`ID, configuration, or instance`,3,`ngModelChange`,`ngModel`],[3,`ngModelChange`,`ngModel`],[`value`,`ALL`],[`value`,`RUNNING`],[`value`,`SUCCEEDED`],[`value`,`REJECTED`],[`value`,`FAILED`],[3,`ngModelChange`,`ngModel`,`disabled`],[1,`table-scroll`],[`type`,`button`,3,`click`],[3,`issue`],[`aria-label`,`Evaluation pages`,3,`page`,`length`,`pageIndex`,`pageSize`,`pageSizeOptions`],[`type`,`button`,1,`candidate-link`,3,`click`],[1,`instance`,3,`title`],[1,`state`],[1,`slow`],[1,`details`],[`colspan`,`8`,1,`empty`]],template:function(e,i){e&1&&(l(0,`section`,0)(1,`mat-form-field`,1)(2,`mat-label`),u(3,`Search`),d(),l(4,`input`,2),wr(),C(`ngModelChange`,function(o){return i.updateSearch(o)}),d()(),l(5,`mat-form-field`,1)(6,`mat-label`),u(7,`State`),d(),l(8,`mat-select`,3),wr(),C(`ngModelChange`,function(o){return i.updateState(o)}),l(9,`mat-option`,4),u(10,`All states`),d(),l(11,`mat-option`,5),u(12,`Running`),d(),l(13,`mat-option`,6),u(14,`Succeeded`),d(),l(15,`mat-option`,7),u(16,`Rejected`),d(),l(17,`mat-option`,8),u(18,`Failed`),d()()(),l(19,`mat-checkbox`,3),wr(),C(`ngModelChange`,function(o){return i.updateSlowOnly(o)}),u(20,` Slow only `),d(),l(21,`mat-checkbox`,9),wr(),C(`ngModelChange`,function(o){return i.updateIssueOnly(o)}),u(22,` Issues only `),d()(),l(23,`div`,10)(24,`table`)(25,`thead`)(26,`tr`)(27,`th`)(28,`button`,11),C(`click`,function(){return i.sortBy(`id`)}),u(29),d()(),l(30,`th`)(31,`button`,11),C(`click`,function(){return i.sortBy(`configurationId`)}),u(32),d()(),l(33,`th`)(34,`button`,11),C(`click`,function(){return i.sortBy(`instanceName`)}),u(35),d()(),l(36,`th`)(37,`button`,11),C(`click`,function(){return i.sortBy(`state`)}),u(38),d()(),l(39,`th`)(40,`button`,11),C(`click`,function(){return i.sortBy(`cost`)}),u(41),d()(),l(42,`th`)(43,`button`,11),C(`click`,function(){return i.sortBy(`timeSeconds`)}),u(44),d()(),l(45,`th`),u(46,`Finished`),d(),l(47,`th`),u(48,`Details`),d()()(),l(49,`tbody`),be(50,HP,23,23,`tr`,12,jP,!1,UP,3,0,`tr`),d()()(),l(53,`mat-paginator`,13),C(`page`,function(o){return i.updatePage(o)}),d()),e&2&&(h(4),S(`ngModel`,i.search()),Er(),h(4),S(`ngModel`,i.state()),Er(),h(11),S(`ngModel`,i.slowOnly()),Er(),h(2),S(`ngModel`,i.issueOnly()||i.forceIssues())(`disabled`,i.forceIssues()),Er(),h(8),H(`ID`,i.sortIndicator(`id`)),h(3),H(` Configuration`,i.sortIndicator(`configurationId`),` `),h(3),H(` Instance`,i.sortIndicator(`instanceName`),` `),h(3),H(`State`,i.sortIndicator(`state`)),h(3),H(`Cost`,i.sortIndicator(`cost`)),h(3),H(` Time`,i.sortIndicator(`timeSeconds`),` `),h(6),ve(i.page()),h(3),S(`length`,i.filtered().length)(`pageIndex`,i.pageIndex())(`pageSize`,i.pageSize())(`pageSizeOptions`,vt(16,BP)))},dependencies:[RS,qu,AS,ib,Ot,tm,jc,ui,Ni,na,Qr,Kr,mm,Wc,Pa,Ra,Un,Xi,wn],styles:[`[_nghost-%COMP%]{display:block;min-width:0}.filters[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem 1rem;align-items:center;margin-bottom:1rem}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]:first-child{flex:1 1 18rem}.table-scroll[_ngcontent-%COMP%]{overflow:auto}table[_ngcontent-%COMP%]{width:100%;min-width:70rem;border-collapse:collapse;font:var(--%NS%mat-sys-body-small)}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.65rem .55rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left;vertical-align:middle}th[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-weight:650}th[_ngcontent-%COMP%]   button[_ngcontent-%COMP%], .candidate-link[_ngcontent-%COMP%]{padding:0;border:0;color:inherit;background:transparent;cursor:pointer;font-weight:inherit}.candidate-link[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-primary);text-decoration:underline;text-underline-offset:.15rem}.instance[_ngcontent-%COMP%], .details[_ngcontent-%COMP%]{max-width:18rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.state[_ngcontent-%COMP%]{display:inline-block;padding:.15rem .45rem;border-radius:999px;background:var(--%NS%mat-sys-surface-container-high);font:var(--%NS%mat-sys-label-small)}.state-succeeded[_ngcontent-%COMP%]{color:#08743a;background:#d9f5e4}.state-rejected[_ngcontent-%COMP%], .slow[_ngcontent-%COMP%]{color:#835100;background:#fff0c6}.state-failed[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-error-container);background:var(--%NS%mat-sys-error-container)}.slow[_ngcontent-%COMP%]{margin-left:.25rem;padding:.1rem .3rem;border-radius:.25rem;font:var(--%NS%mat-sys-label-small)}tr.issue[_ngcontent-%COMP%]{background:color-mix(in srgb,var(--%NS%mat-sys-error-container) 22%,transparent)}.empty[_ngcontent-%COMP%]{padding:2.5rem;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}mat-paginator[_ngcontent-%COMP%]{background:transparent}`]})};var Xw=(t,n)=>n.name;function $P(t,n){if(t&1&&(l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),d()()),t&2){let e=n.$implicit,i=y(2);h(2),w(e.name),h(2),w(e.kind),h(2),w(i.domain(e))}}function GP(t,n){if(t&1&&(l(0,`details`)(1,`summary`),u(2),d(),l(3,`table`)(4,`thead`)(5,`tr`)(6,`th`),u(7,`Name`),d(),l(8,`th`),u(9,`Kind`),d(),l(10,`th`),u(11,`Domain`),d()()(),l(12,`tbody`),be(13,$P,7,3,`tr`,null,Xw),d()()()),t&2){let e=n.$implicit;h(2),Fe(``,e.name,` · `,e.parameters.length,` parameters`),h(11),ve(e.parameters)}}var Nm=class t{data=f(Zc);domain(n){return n.choices.length>0?n.choices.join(`, `):n.values.length>0?n.values.join(`, `):n.minimum!==null||n.maximum!==null?`${String(n.minimum)} \u2013 ${String(n.maximum)}`:`Provided by Mork`}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-search-space-dialog`]],decls:30,vars:5,consts:[[`mat-dialog-title`,``],[1,`summary`],[1,`components`],[`align`,`end`],[`mat-button`,``,`mat-dialog-close`,``,`type`,`button`]],template:function(e,i){e&1&&(l(0,`h2`,0),u(1,`Generated search space`),d(),l(2,`mat-dialog-content`)(3,`section`,1)(4,`span`)(5,`strong`),u(6),d(),u(7,` roots`),d(),l(8,`span`)(9,`strong`),u(10),d(),u(11,` components`),d(),l(12,`span`)(13,`strong`),u(14),d(),u(15,` IRACE parameters`),d(),l(16,`span`)(17,`strong`),u(18),d(),u(19,` constraints`),d()(),l(20,`p`)(21,`strong`),u(22,`Root choices:`),d(),u(23),d(),l(24,`section`,2),be(25,GP,15,2,`details`,null,Xw),d()(),l(27,`mat-dialog-actions`,3)(28,`button`,4),u(29,`Close`),d()()),e&2&&(h(6),w(i.data.summary.rootCount),h(4),w(i.data.summary.componentCount),h(4),w(i.data.summary.generatedIraceParameterCount),h(4),w(i.data.summary.generatedForbiddenConstraintCount),h(5),H(` `,i.data.roots.join(`, `)),h(2),ve(i.data.components))},dependencies:[Ot,en,km,Sm,wm,Dm,Em],styles:[`mat-dialog-content[_ngcontent-%COMP%]{width:min(60rem,84vw)}.summary[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem 2rem;margin-bottom:1rem}.components[_ngcontent-%COMP%]{display:grid;gap:.5rem}details[_ngcontent-%COMP%]{padding:.75rem;border:1px solid var(--%NS%mat-sys-outline-variant);border-radius:.5rem}summary[_ngcontent-%COMP%]{cursor:pointer;font-weight:650}table[_ngcontent-%COMP%]{width:100%;margin-top:.75rem;border-collapse:collapse}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.45rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left;overflow-wrap:anywhere}`]})};var Zw=new I(`MAT_BUTTON_TOGGLE_DEFAULT_OPTIONS`,{providedIn:`root`,factory:()=>({hideSingleSelectionIndicator:!1,hideMultipleSelectionIndicator:!1,disabledInteractive:!1})});var Jw=new I(`MatButtonToggleGroup`);var WP={provide:Gr,useExisting:Ht(()=>Bb),multi:!0};var Tm=class{source;value;constructor(n,e){this.source=n,this.value=e}};var Bb=(()=>{class t{_changeDetector=f(Pe);_dir=f(ft,{optional:!0});_multiple=!1;_disabled=!1;_disabledInteractive=!1;_selectionModel;_rawValue;_controlValueAccessorChangeFn=()=>{};_onTouched=()=>{};_buttonToggles;appearance;get name(){return this._name}set name(e){this._name=e,this._markButtonsForCheck()}_name=f(Be).getId(`mat-button-toggle-group-`);vertical=!1;get value(){let e=this._selectionModel?this._selectionModel.selected:[];return this.multiple?e.map(i=>i.value):e[0]?e[0].value:void 0}set value(e){this._setSelectionByValue(e),this.valueChange.emit(this.value)}valueChange=new ee;get selected(){let e=this._selectionModel?this._selectionModel.selected:[];return this.multiple?e:e[0]||null}get multiple(){return this._multiple}set multiple(e){this._multiple=e,this._markButtonsForCheck()}get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._markButtonsForCheck()}get disabledInteractive(){return this._disabledInteractive}set disabledInteractive(e){this._disabledInteractive=e,this._markButtonsForCheck()}get dir(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}change=new ee;get hideSingleSelectionIndicator(){return this._hideSingleSelectionIndicator}set hideSingleSelectionIndicator(e){this._hideSingleSelectionIndicator=e,this._markButtonsForCheck()}_hideSingleSelectionIndicator;get hideMultipleSelectionIndicator(){return this._hideMultipleSelectionIndicator}set hideMultipleSelectionIndicator(e){this._hideMultipleSelectionIndicator=e,this._markButtonsForCheck()}_hideMultipleSelectionIndicator;constructor(){let e=f(Zw,{optional:!0});this.appearance=e&&e.appearance?e.appearance:`standard`,this._hideSingleSelectionIndicator=e?.hideSingleSelectionIndicator??!1,this._hideMultipleSelectionIndicator=e?.hideMultipleSelectionIndicator??!1}ngOnInit(){this._selectionModel=new Bo(this.multiple,void 0,!1)}ngAfterContentInit(){this._selectionModel.select(...this._buttonToggles.filter(e=>e.checked)),this.multiple||this._initializeTabIndex()}writeValue(e){this.value=e,this._changeDetector.markForCheck()}registerOnChange(e){this._controlValueAccessorChangeFn=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e}_keydown(e){if(this.multiple||this.disabled||xt(e))return;let a=e.target.id,o=this._buttonToggles.toArray().findIndex(s=>s.buttonId===a),r=null;switch(e.keyCode){case 32:case 13:r=this._buttonToggles.get(o)||null;break;case 38:r=this._getNextButton(o,-1);break;case 37:r=this._getNextButton(o,this.dir===`ltr`?-1:1);break;case 40:r=this._getNextButton(o,1);break;case 39:r=this._getNextButton(o,this.dir===`ltr`?1:-1);break;default:return}r&&(e.preventDefault(),r._onButtonClick(),r.focus())}_emitChangeEvent(e){let i=new Tm(e,this.value);this._rawValue=i.value,this._controlValueAccessorChangeFn(i.value),this.change.emit(i)}_syncButtonToggle(e,i,a=!1,o=!1){!this.multiple&&this.selected&&!e.checked&&(this.selected.checked=!1),this._selectionModel?i?this._selectionModel.select(e):this._selectionModel.deselect(e):o=!0,o?Promise.resolve().then(()=>this._updateModelValue(e,a)):this._updateModelValue(e,a)}_isSelected(e){return this._selectionModel&&this._selectionModel.isSelected(e)}_isPrechecked(e){return typeof this._rawValue>`u`?!1:this.multiple&&Array.isArray(this._rawValue)?this._rawValue.some(i=>e.value!=null&&i===e.value):e.value===this._rawValue}_initializeTabIndex(){if(this._buttonToggles.forEach(e=>{e.tabIndex=-1}),this.selected)this.selected.tabIndex=0;else for(let e=0;e<this._buttonToggles.length;e++){let i=this._buttonToggles.get(e);if(!i.disabled){i.tabIndex=0;break}}}_getNextButton(e,i){let a=this._buttonToggles;for(let o=1;o<=a.length;o++){let r=(e+i*o+a.length)%a.length,s=a.get(r);if(s&&!s.disabled)return s}return null}_setSelectionByValue(e){if(this._rawValue=e,!this._buttonToggles)return;let i=this._buttonToggles.toArray();if(this.multiple&&e?(this._clearSelection(),e.forEach(a=>this._selectValue(a,i))):(this._clearSelection(),this._selectValue(e,i)),!this.multiple&&i.every(a=>a.tabIndex===-1)){for(let a of i)if(!a.disabled){a.tabIndex=0;break}}}_clearSelection(){this._selectionModel.clear(),this._buttonToggles.forEach(e=>{e.checked=!1,this.multiple||(e.tabIndex=-1)})}_selectValue(e,i){for(let a of i)if(a.value===e){a.checked=!0,this._selectionModel.select(a),this.multiple||(a.tabIndex=0);break}}_updateModelValue(e,i){i&&this._emitChangeEvent(e),this.valueChange.emit(this.value)}_markButtonsForCheck(){this._buttonToggles?.forEach(e=>e._markForCheck())}static ɵfac=function(i){return new(i||t)};static ɵdir=Y({type:t,selectors:[[`mat-button-toggle-group`]],contentQueries:function(i,a,o){if(i&1&&Bt(o,Am,5),i&2){let r;Z(r=J())&&(a._buttonToggles=r)}},hostAttrs:[1,`mat-button-toggle-group`],hostVars:6,hostBindings:function(i,a){i&1&&C(`keydown`,function(r){return a._keydown(r)}),i&2&&(te(`role`,a.multiple?`group`:`radiogroup`)(`aria-disabled`,a.disabled),q(`mat-button-toggle-vertical`,a.vertical)(`mat-button-toggle-group-appearance-standard`,a.appearance===`standard`))},inputs:{appearance:`appearance`,name:`name`,vertical:[2,`vertical`,`vertical`,V],value:`value`,multiple:[2,`multiple`,`multiple`,V],disabled:[2,`disabled`,`disabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],hideSingleSelectionIndicator:[2,`hideSingleSelectionIndicator`,`hideSingleSelectionIndicator`,V],hideMultipleSelectionIndicator:[2,`hideMultipleSelectionIndicator`,`hideMultipleSelectionIndicator`,V]},outputs:{valueChange:`valueChange`,change:`change`},exportAs:[`matButtonToggleGroup`],features:[Ge([WP,{provide:Jw,useExisting:t}])]})}return t})();var Am=(()=>{class t{_changeDetectorRef=f(Pe);_elementRef=f(W);_focusMonitor=f(Jt);_idGenerator=f(Be);_animationDisabled=Ue();_checked=!1;ariaLabel;ariaLabelledby=null;_buttonElement;buttonToggleGroup;get buttonId(){return`${this.id}-button`}id;name;value;get tabIndex(){return this._tabIndex()}set tabIndex(e){this._tabIndex.set(e)}_tabIndex;disableRipple=!1;get appearance(){return this.buttonToggleGroup?this.buttonToggleGroup.appearance:this._appearance}set appearance(e){this._appearance=e}_appearance;get checked(){return this.buttonToggleGroup?this.buttonToggleGroup._isSelected(this):this._checked}set checked(e){e!==this._checked&&(this._checked=e,this.buttonToggleGroup&&this.buttonToggleGroup._syncButtonToggle(this,this._checked),this._changeDetectorRef.markForCheck())}get disabled(){return this._disabled||this.buttonToggleGroup&&this.buttonToggleGroup.disabled}set disabled(e){this._disabled=e}_disabled=!1;get disabledInteractive(){return this._disabledInteractive||this.buttonToggleGroup!==null&&this.buttonToggleGroup.disabledInteractive}set disabledInteractive(e){this._disabledInteractive=e}_disabledInteractive;change=new ee;constructor(){f(Je).load(dn);let e=f(Jw,{optional:!0}),i=f(new Ki(`tabindex`),{optional:!0})||``,a=f(Zw,{optional:!0});this._tabIndex=N(parseInt(i)||0),this.buttonToggleGroup=e,this._appearance=a&&a.appearance?a.appearance:`standard`,this._disabledInteractive=a?.disabledInteractive??!1}ngOnInit(){let e=this.buttonToggleGroup;this.id=this.id||this._idGenerator.getId(`mat-button-toggle-`),e&&(e._isPrechecked(this)?this.checked=!0:e._isSelected(this)!==this._checked&&e._syncButtonToggle(this,this._checked))}ngAfterViewInit(){this._animationDisabled||this._elementRef.nativeElement.classList.add(`mat-button-toggle-animations-enabled`),this._focusMonitor.monitor(this._elementRef,!0)}ngOnDestroy(){let e=this.buttonToggleGroup;this._focusMonitor.stopMonitoring(this._elementRef),e&&e._isSelected(this)&&e._syncButtonToggle(this,!1,!1,!0)}focus(e){this._buttonElement.nativeElement.focus(e)}_onButtonClick(){if(this.disabled)return;let e=this.isSingleSelector()?!0:!this._checked;if(e!==this._checked&&(this._checked=e,this.buttonToggleGroup&&(this.buttonToggleGroup._syncButtonToggle(this,this._checked,!0),this.buttonToggleGroup._onTouched())),this.isSingleSelector()){let i=this.buttonToggleGroup._buttonToggles.find(a=>a.tabIndex===0);i&&(i.tabIndex=-1),this.tabIndex=0}this.change.emit(new Tm(this,this.value))}_markForCheck(){this._changeDetectorRef.markForCheck()}_getButtonName(){return this.isSingleSelector()?this.buttonToggleGroup.name:this.name||null}isSingleSelector(){return this.buttonToggleGroup&&!this.buttonToggleGroup.multiple}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`button`],i=[`*`];function a(o,r){if(o&1&&(l(0,`div`,2),z(1,`mat-pseudo-checkbox`,6),d()),o&2){let s=y();h(),S(`disabled`,s.disabled)}}return B({type:t,selectors:[[`mat-button-toggle`]],viewQuery:function(r,s){if(r&1&&Ze(e,5),r&2){let c;Z(c=J())&&(s._buttonElement=c.first)}},hostAttrs:[`role`,`presentation`,1,`mat-button-toggle`],hostVars:14,hostBindings:function(r,s){r&1&&C(`focus`,function(){return s.focus()}),r&2&&(te(`aria-label`,null)(`aria-labelledby`,null)(`id`,s.id)(`name`,null),q(`mat-button-toggle-standalone`,!s.buttonToggleGroup)(`mat-button-toggle-checked`,s.checked)(`mat-button-toggle-disabled`,s.disabled)(`mat-button-toggle-disabled-interactive`,s.disabledInteractive)(`mat-button-toggle-appearance-standard`,s.appearance===`standard`))},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],id:`id`,name:`name`,value:`value`,tabIndex:`tabIndex`,disableRipple:[2,`disableRipple`,`disableRipple`,V],appearance:`appearance`,checked:[2,`checked`,`checked`,V],disabled:[2,`disabled`,`disabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V]},outputs:{change:`change`},exportAs:[`matButtonToggle`],ngContentSelectors:i,decls:7,vars:13,consts:[[`button`,``],[`type`,`button`,1,`mat-button-toggle-button`,`mat-focus-indicator`,3,`click`,`id`,`disabled`],[1,`mat-button-toggle-checkbox-wrapper`],[1,`mat-button-toggle-label-content`],[1,`mat-button-toggle-focus-overlay`],[`matRipple`,``,1,`mat-button-toggle-ripple`,3,`matRippleTrigger`,`matRippleDisabled`],[`state`,`checked`,`aria-hidden`,`true`,`appearance`,`minimal`,3,`disabled`]],template:function(r,s){if(r&1&&(Re(),l(0,`button`,1,0),C(`click`,function(){return s._onButtonClick()}),D(2,a,2,1,`div`,2),l(3,`span`,3),me(4),d()(),z(5,`span`,4)(6,`span`,5)),r&2){let c=ht(1);S(`id`,s.buttonId)(`disabled`,s.disabled&&!s.disabledInteractive||null),te(`role`,s.isSingleSelector()?`radio`:`button`)(`tabindex`,s.disabled&&!s.disabledInteractive?-1:s.tabIndex)(`aria-pressed`,s.isSingleSelector()?null:s.checked)(`aria-checked`,s.isSingleSelector()?s.checked:null)(`name`,s._getButtonName())(`aria-label`,s.ariaLabel)(`aria-labelledby`,s.ariaLabelledby)(`aria-disabled`,s.disabled&&s.disabledInteractive?`true`:null),h(2),k(s.buttonToggleGroup&&(!s.buttonToggleGroup.multiple&&!s.buttonToggleGroup.hideSingleSelectionIndicator||s.buttonToggleGroup.multiple&&!s.buttonToggleGroup.hideMultipleSelectionIndicator)?2:-1),h(4),S(`matRippleTrigger`,c)(`matRippleDisabled`,s.disableRipple||s.disabled)}},dependencies:[li,um],styles:[`.mat-button-toggle-standalone,
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
`],encapsulation:2})})()}return t})();var eE=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=oe({type:t});static ɵinj=ie({imports:[Ii,Am,we]})}return t})();var $n=class{static count(n,e){return e===`CANDIDATE`?n.candidatePlacements:n.evaluationPlacements}static components(n,e,i){let a=i.trim().toLocaleLowerCase();return(n?.components??[]).filter(o=>o.name.toLocaleLowerCase().includes(a)).sort((o,r)=>this.count(r,e)-this.count(o,e)||o.name.localeCompare(r.name))}static relationships(n,e,i,a){return(n?.relationships??[]).filter(o=>i===`incoming`?o.child===e:o.parent===e).sort((o,r)=>this.count(r,a)-this.count(o,a)||o.parent.localeCompare(r.parent)||o.role.localeCompare(r.role)||o.child.localeCompare(r.child))}static treemap(n,e,i){return{options:{chart:{type:`treemap`,height:390,animation:!1,backgroundColor:`transparent`},title:{text:void 0},credits:{enabled:!1},accessibility:{description:`Component placements. Select a tile or a component in the list to inspect its parents, children and configurations.`},tooltip:{pointFormat:`<b>{point.name}</b><br/>Placements: {point.value:,.0f}`},plotOptions:{series:{animation:!1}},series:[{type:`treemap`,name:`Component placements`,layoutAlgorithm:`squarified`,colorByPoint:!0,borderWidth:3,borderColor:`#fff`,colors:[`#2265a7`,`#00897b`,`#407fab`,`#49a69e`,`#697cad`,`#78b8ad`],dataLabels:{enabled:!0,format:`{point.name}<br/>{point.value:,.0f}`,style:{textOutline:`none`,fontWeight:`600`}},point:{events:{click:function(){this.name&&i(this.name)}}},data:n.filter(a=>this.count(a,e)>0).map(a=>({name:a.name,value:this.count(a,e)}))}]}}}static sankey(n,e,i,a,o){let r=[{id:`focus`,name:n??`Component`,color:`#2265a7`}],s=[],c=(m,g)=>{let v=m.filter(_=>this.count(_,a)>0);for(let[_,E]of v.slice(0,10).entries()){let P=`${g}:${_}`;r.push({id:P,name:g===`in`?`${E.parent} \xB7 ${E.role}`:`${E.role} \u2192 ${E.child}`,color:`#00897b`}),s.push({from:g===`in`?P:`focus`,to:g===`in`?`focus`:P,weight:this.count(E,a),custom:{edge:E}})}let b=v.slice(10).reduce((_,E)=>_+this.count(E,a),0);if(b>0){let _=`${g}:other`;r.push({id:_,name:`Other ${g===`in`?`parents`:`children`}`,color:`#aab7c5`}),s.push({from:g===`in`?_:`focus`,to:g===`in`?`focus`:_,weight:b})}};c(e,`in`),c(i,`out`);let p={type:`sankey`,animation:!1,name:`Direct relationships`,nodes:r,data:s,point:{events:{click:function(){let m=this.options.custom?.edge;m&&o(m)}}}};return{options:{chart:{height:Math.max(300,Math.min(560,Math.max(e.length,i.length)*32)),animation:!1,backgroundColor:`transparent`},title:{text:void 0},credits:{enabled:!1},accessibility:{description:`Direct parents and children of ${n??`the selected component`}. The complete role relationships are also listed in the inspector.`},series:[p]}}}static matrix(n,e,i,a){let o=new Set(this.components(n,e,i).map(_=>_.name)),r=i.trim().length>0,s=(n?.relationships??[]).filter(_=>!r||o.has(_.parent)||o.has(_.child)),c=new Set(o);for(let _ of s)c.add(_.parent),c.add(_.child);let p=this.components(n,e,``).filter(_=>c.has(_.name)).sort((_,E)=>(r?Number(o.has(E.name))-Number(o.has(_.name)):0)||this.count(E,e)-this.count(_,e)||_.name.localeCompare(E.name)).slice(0,a),m=new Set(p.map(_=>_.name)),g=new Map,v=0;for(let _ of s){if(!m.has(_.parent)||!m.has(_.child))continue;let E=JSON.stringify([_.parent,_.role]),P=g.get(E);P||(P={parent:_.parent,role:_.role,count:0,edges:new Map},g.set(E,P)),P.edges.set(_.child,_),P.count+=this.count(_,e),v=Math.max(v,this.count(_,e))}return{columns:p,rows:[...g.values()].sort((_,E)=>E.count-_.count||_.parent.localeCompare(E.parent)||_.role.localeCompare(E.role)).slice(0,a).map(_=>({parent:_.parent,role:_.role,cells:p.map(E=>_.edges.get(E.name)??null)})),maximum:v}}static parentPercent(n,e){let i=e?.components.find(a=>a.name===n.parent);return i?.configurationCount?100*n.configurationCount/i.configurationCount:0}};var YP=()=>[];var KP=(t,n)=>n.configurationId;var tE=(t,n)=>n.name;var nE=(t,n)=>n.parent+`:`+n.role;var QP=(t,n)=>n.role+`:`+n.child;function XP(t,n){t&1&&u(0,` Count each unique configuration tree once. Repeated components within a tree count as separate placements. `)}function ZP(t,n){t&1&&u(0,` Weight each placement by accepted evaluations, including evaluations that later fail or are rejected. `)}function JP(t,n){if(t&1){let e=X();l(0,`p`,13),u(1),l(2,`button`,15),C(`click`,function(){A(e);return O(y().store.refreshComponentUsage())}),u(3,`Retry`),d()()}t&2&&(h(),H(` `,n,` `))}function eF(t,n){if(t&1&&(l(0,`p`,17),u(1),U(2,`number`),d()),t&2){let e=y();h(),H(` `,_e(2,1,e.unavailableConfigurationCount),` configurations have unavailable component trees and are excluded from placement counts. `)}}function tF(t,n){if(t&1&&(l(0,`p`,14),u(1),d()),t&2){let e=y(2);h(),H(` `,e.store.componentScope()===`CURRENT_ELITES`?`Elites appear after the first race checkpoint.`:`Component usage appears when the first decoded candidate is evaluated.`,` `)}}function nF(t,n){t&1&&(l(0,`p`,14),u(1,`No components match this search.`),d())}function iF(t,n){t&1&&(l(0,`p`,14),u(1,` These configurations have no accepted evaluations yet. Select candidate structures to inspect their composition. `),d())}function aF(t,n){if(t&1){let e=X();l(0,`button`,31),C(`click`,function(){let a=A(e).$implicit;return O(y(4).selectComponent(a.name))}),l(1,`span`),u(2),d(),l(3,`strong`),u(4),U(5,`number`),d()()}if(t&2){let e=n.$implicit,i=y(4);q(`selected`,i.selected()?.name===e.name),te(`aria-pressed`,i.selected()?.name===e.name),h(2),w(e.name),h(2),w(_e(5,5,i.count(e)))}}function oF(t,n){if(t&1&&(l(0,`mat-card-header`)(1,`mat-card-title`),u(2,`Component usage`),d()(),l(3,`mat-card-content`),z(4,`app-dashboard-chart`,28),D(5,iF,2,0,`p`,14),l(6,`div`,29),be(7,aF,6,7,`button`,30,tE),d()()),t&2){let e=y(2),i=y();h(4),S(`model`,i.treemap()),h(),k(i.basis()===`EVALUATION`&&e.evaluationPlacementCount===0?5:-1),h(2),ve(i.components())}}function rF(t,n){if(t&1&&(l(0,`th`,25),u(1),d()),t&2){let e=n.$implicit;h(),w(e.name)}}function sF(t,n){if(t&1){let e=X();l(0,`button`,31),C(`click`,function(){A(e);let a=y().$implicit;return O(y(5).selectRelationship(a))}),u(1),d()}if(t&2){let e=y().$implicit,i=y(5);fn(`background-color`,i.cellColor(e)),te(`aria-label`,i.cellDescription(e)),h(),H(` `,i.cellLabel(e),` `)}}function cF(t,n){t&1&&(l(0,`span`),u(1,`—`),d())}function lF(t,n){if(t&1&&(l(0,`td`),D(1,sF,2,4,`button`,38)(2,cF,2,0,`span`),d()),t&2){let e=n.$implicit;h(),k(e?1:2)}}function dF(t,n){if(t&1&&(l(0,`tr`)(1,`th`,37),u(2),l(3,`small`),u(4),d()(),be(5,lF,3,1,`td`,null,ic),d()),t&2){let e=n.$implicit;h(2),H(` `,e.parent),h(2),w(e.role),h(),ve(e.cells)}}function uF(t,n){if(t&1&&(l(0,`tr`)(1,`td`),u(2,` No direct relationships in this selection. `),d()()),t&2){let e=y(4);h(),te(`colspan`,e.matrix().columns.length+1)}}function mF(t,n){if(t&1){let e=X();l(0,`mat-card-header`)(1,`mat-card-title`),u(2,`Direct component relationships`),d()(),l(3,`mat-card-content`)(4,`div`,32)(5,`mat-form-field`,2)(6,`mat-label`),u(7,`Maximum rows / columns`),d(),l(8,`mat-select`,3),C(`selectionChange`,function(a){A(e);return O(y(3).matrixLimit.set(a.value))}),l(9,`mat-option`,33),u(10,`10`),d(),l(11,`mat-option`,33),u(12,`20`),d(),l(13,`mat-option`,33),u(14,`50`),d()()(),l(15,`mat-button-toggle-group`,34),C(`change`,function(a){A(e);return O(y(3).matrixPercent.set(a.value))}),l(16,`mat-button-toggle`,33),u(17,`Count`),d(),l(18,`mat-button-toggle`,33),u(19,`% of parent configurations`),d()()(),l(20,`p`,12),u(21,` Rows identify the parent and parameter role; columns identify the direct child. The most frequent matching components are shown. `),d(),l(22,`div`,35)(23,`table`,36)(24,`caption`),u(25,` Direct parent-role to child usage `),d(),l(26,`thead`)(27,`tr`)(28,`th`,25),u(29,`Parent · role`),d(),be(30,rF,2,1,`th`,25,tE),d()(),l(32,`tbody`),be(33,dF,7,2,`tr`,null,nE,!1,uF,3,1,`tr`),d()()()()}if(t&2){let e=y(3);h(8),S(`value`,e.matrixLimit()),h(),S(`value`,10),h(2),S(`value`,20),h(2),S(`value`,50),h(2),S(`value`,e.matrixPercent()),h(),S(`value`,!1),h(2),S(`value`,!0),h(12),ve(e.matrix().columns),h(3),ve(e.matrix().rows)}}function pF(t,n){if(t&1){let e=X();l(0,`button`,31),C(`click`,function(){let a=A(e).$implicit;return O(y(4).selectRelationship(a))}),l(1,`span`)(2,`strong`),u(3),d(),l(4,`small`),u(5),d()(),l(6,`span`),u(7),U(8,`number`),l(9,`small`),u(10),U(11,`number`),d()()()}if(t&2){let e=n.$implicit,i=y(4);h(3),w(e.parent),h(2),w(e.role),h(2),w(_e(8,4,i.count(e))),h(3),H(``,_e(11,6,e.configurationCount),` configurations`)}}function hF(t,n){t&1&&(l(0,`p`),u(1,`No direct parents; this component is used only at the root.`),d())}function fF(t,n){if(t&1){let e=X();l(0,`button`,31),C(`click`,function(){let a=A(e).$implicit;return O(y(4).selectRelationship(a))}),l(1,`span`)(2,`strong`),u(3),d(),l(4,`small`),u(5),d()(),l(6,`span`),u(7),U(8,`number`),l(9,`small`),u(10),U(11,`number`),d()()()}if(t&2){let e=n.$implicit,i=y(4);h(3),w(e.child),h(2),w(e.role),h(2),w(_e(8,4,i.count(e))),h(3),H(``,_e(11,6,e.configurationCount),` configurations`)}}function gF(t,n){t&1&&(l(0,`p`),u(1,`No direct child components.`),d())}function bF(t,n){if(t&1&&(l(0,`mat-card`,20)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d(),l(4,`mat-card-subtitle`),u(5,`Component inspector`),d()(),l(6,`mat-card-content`)(7,`dl`)(8,`dt`),u(9,`Candidate placements`),d(),l(10,`dd`),u(11),U(12,`number`),d(),l(13,`dt`),u(14,`Evaluation exposure`),d(),l(15,`dd`),u(16),U(17,`number`),d(),l(18,`dt`),u(19,`Configuration coverage`),d(),l(20,`dd`),u(21),U(22,`number`),U(23,`number`),d(),l(24,`dt`),u(25,`Root placements / exposure`),d(),l(26,`dd`),u(27),U(28,`number`),U(29,`number`),d()(),l(30,`h3`),u(31,`Used by`),d(),l(32,`div`,39),be(33,pF,12,8,`button`,40,nE,!1,hF,2,0,`p`),d(),l(36,`h3`),u(37,`Contains`),d(),l(38,`div`,39),be(39,fF,12,8,`button`,40,QP,!1,gF,2,0,`p`),d()()()),t&2){let e=n,i=y(3);h(3),w(e.name),h(8),w(_e(12,9,e.candidatePlacements)),h(5),w(_e(17,11,e.evaluationPlacements)),h(5),Fe(` `,_e(22,13,e.configurationCount),` (`,Ie(23,15,i.selectedCoverage(),`1.0-1`),`%) `),h(6),Fe(` `,_e(28,18,e.rootCandidatePlacements),` / `,_e(29,20,e.rootEvaluationPlacements),` `),h(6),ve(i.incoming()),h(6),ve(i.outgoing())}}function vF(t,n){if(t&1&&(l(0,`mat-card`,21)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d()(),l(4,`mat-card-content`),z(5,`app-dashboard-chart`,28),l(6,`p`,12),u(7,` Direct parents → selected component → direct children. Up to ten relationships are shown on each side; the inspector lists all relationships. `),d()()()),t&2){let e=y(3);h(3),H(`Neighborhood of `,e.selected()?.name),h(2),S(`model`,e.sankey())}}function yF(t,n){if(t&1){let e=X();l(0,`p`),u(1),l(2,`button`,15),C(`click`,function(){A(e);return O(y(3).selectedEdge.set(null))}),u(3),d()()}if(t&2){let e=n,i=y(3);h(),jt(` `,e.parent,` → `,e.role,` → `,e.child,` `),h(2),H(` Show all using `,i.selected()?.name,` `)}}function _F(t,n){if(t&1&&(l(0,`p`),u(1),d()),t&2){let e=y(3);h(),H(`Configurations containing `,e.selected()?.name)}}function xF(t,n){if(t&1){let e=X();l(0,`p`,22),u(1),l(2,`button`,15),C(`click`,function(){A(e);return O(y(3).retryPage())}),u(3,`Retry`),d()()}t&2&&(h(),H(` `,n,` `))}function CF(t,n){if(t&1){let e=X();l(0,`tr`)(1,`td`)(2,`button`,15),C(`click`,function(){let a=A(e).$implicit;return O(y(3).inspectCandidate(a.configurationId))}),u(3),d()(),l(4,`td`),u(5),U(6,`number`),d(),l(7,`td`),u(8),U(9,`number`),d(),l(10,`td`),u(11),d(),l(12,`td`),u(13),d()()}if(t&2){let e=n.$implicit;h(3),H(` `,e.configurationId,` `),h(2),w(_e(6,8,e.occurrences)),h(3),w(_e(9,10,e.evaluationPlacements)),h(3),ai(` `,e.evaluations.running,` / `,e.evaluations.succeeded,` / `,e.evaluations.rejected,` / `,e.evaluations.failed,` `),h(2),w(e.elitePosition??`—`)}}function SF(t,n){if(t&1&&(l(0,`tr`)(1,`td`,41),u(2),d()()),t&2){let e=y(3);h(2),H(` `,e.loading()?`Loading configurations…`:`No matching configurations.`,` `)}}function wF(t,n){if(t&1){let e=X();l(0,`div`,18)(1,`mat-card`,19),D(2,oF,9,2)(3,mF,36,8),d(),D(4,bF,42,22,`mat-card`,20),D(5,vF,8,2,`mat-card`,21),l(6,`mat-card`,21)(7,`mat-card-header`)(8,`mat-card-title`),u(9,`Matching configurations`),d()(),l(10,`mat-card-content`),D(11,yF,4,4,`p`)(12,_F,2,1,`p`),D(13,xF,4,1,`p`,22),l(14,`div`,23)(15,`table`,24)(16,`thead`)(17,`tr`)(18,`th`,25),u(19,`Configuration`),d(),l(20,`th`,25),u(21,`Tree placements`),d(),l(22,`th`,25),u(23,`Evaluation exposure`),d(),l(24,`th`,25),u(25,`Running / succeeded / rejected / failed`),d(),l(26,`th`,25),u(27,`Elite rank`),d()()(),l(28,`tbody`),be(29,CF,14,12,`tr`,null,KP,!1,SF,3,1,`tr`),d()()(),l(32,`div`,26)(33,`span`),u(34),U(35,`number`),d(),l(36,`button`,27),C(`click`,function(){A(e);let a=y(2);return O(a.offset.set(a.offset()-25))}),u(37,` Previous `),d(),l(38,`span`),u(39),d(),l(40,`button`,27),C(`click`,function(){A(e);let a=y(2);return O(a.offset.set(a.page()?.nextOffset??0))}),u(41,` Next `),d()()()()()}if(t&2){let e,i,a,o=y(2);h(2),k(o.view()===`usage`?2:3),h(2),k((e=o.selected())?4:-1,e),h(),k(o.view()===`usage`&&(o.incoming().length>0||o.outgoing().length>0)?5:-1),h(6),k((i=o.relationship())?11:12,i),h(2),k((a=o.pageError())?13:-1,a),h(),te(`aria-busy`,o.loading()),h(15),ve(o.page()?.candidates??vt(13,YP)),h(5),H(``,_e(35,11,o.page()?.total??0),` matching configurations`),h(2),S(`disabled`,o.offset()===0||o.loading()),h(3),H(`Page `,o.offset()/25+1),h(),S(`disabled`,o.page()?.nextOffset==null||o.loading())}}function EF(t,n){if(t&1&&(l(0,`div`,16)(1,`article`)(2,`span`),u(3,`Decoded configurations`),d(),l(4,`strong`),u(5),U(6,`number`),U(7,`number`),d()(),l(8,`article`)(9,`span`),u(10),d(),l(11,`strong`),u(12),U(13,`number`),d()(),l(14,`article`)(15,`span`),u(16,`Distinct components`),d(),l(17,`strong`),u(18),U(19,`number`),d()(),l(20,`article`)(21,`span`),u(22,`Accepted evaluations`),d(),l(23,`strong`),u(24),U(25,`number`),d()()(),D(26,eF,3,3,`p`,17),D(27,tF,2,1,`p`,14)(28,nF,2,0,`p`,14)(29,wF,42,14,`div`,18)),t&2){let e=n,i=y();h(5),Fe(``,_e(6,8,e.decodedConfigurationCount),` / `,_e(7,10,e.configurationCount)),h(5),w(i.basis()===`CANDIDATE`?`Component placements`:`Evaluation placements`),h(2),w(_e(13,12,i.basis()===`CANDIDATE`?e.candidatePlacementCount:e.evaluationPlacementCount)),h(6),w(_e(19,14,e.components.length)),h(6),w(_e(25,16,e.evaluationCount)),h(2),k(e.unavailableConfigurationCount>0?26:-1),h(),k(e.components.length===0?27:i.components().length===0?28:29)}}function DF(t,n){t&1&&(l(0,`p`,14),u(1,`Loading component usage…`),d())}var Om=class t{store=f(ja);dialog=f(zo);destroyRef=f(Te);basis=N(`CANDIDATE`);view=N(`usage`);search=N(``);matrixLimit=N(20);matrixPercent=N(!1);selectedName=N(null);selectedEdge=N(null);offset=N(0);page=N(null);loading=N(!1);pageError=N(null);pageRefresh=N(0);request=0;pageKey=null;destroyed=!1;components=ne(()=>$n.components(this.store.componentUsage(),this.basis(),this.search()));selected=ne(()=>this.components().find(n=>n.name===this.selectedName())??this.components()[0]);relationship=ne(()=>{let n=this.selectedEdge();return this.store.componentUsage()?.relationships.find(e=>n&&e.parent===n.parent&&e.role===n.role&&e.child===n.child)??null});incoming=ne(()=>$n.relationships(this.store.componentUsage(),this.selected()?.name,`incoming`,this.basis()));outgoing=ne(()=>$n.relationships(this.store.componentUsage(),this.selected()?.name,`outgoing`,this.basis()));treemap=ne(()=>$n.treemap(this.components(),this.basis(),n=>this.selectComponent(n)));sankey=ne(()=>$n.sankey(this.selected()?.name,this.incoming(),this.outgoing(),this.basis(),n=>this.selectRelationship(n)));matrix=ne(()=>$n.matrix(this.store.componentUsage(),this.basis(),this.search(),this.matrixLimit()));selector=ne(()=>{let n=this.relationship();if(n)return{parent:n.parent,role:n.role,child:n.child};let e=this.selected()?.name;return e?{component:e}:null});selectedCoverage=ne(()=>{let n=this.store.componentUsage()?.decodedConfigurationCount??0;return n?100*(this.selected()?.configurationCount??0)/n:0});selectionKey=ne(()=>JSON.stringify(this.selector()));constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.request++}),Ft(()=>{this.store.componentScope(),this.basis(),this.selectionKey(),this.store.status()?.runId,nt(()=>this.offset.set(0))}),Ft(()=>{let n=this.store.componentUsage(),e=this.selector(),i=this.store.componentScope(),a=this.basis(),o=this.offset();this.pageRefresh(),nt(()=>{if(!n||!e){this.request++,this.page.set(null),this.loading.set(!1),this.pageError.set(null);return}this.loadPage({scope:i,basis:a,selector:e,offset:o,limit:25})})})}selectComponent(n){this.selectedName.set(n),this.selectedEdge.set(null)}selectRelationship(n){this.selectedName.set(n.child),this.selectedEdge.set(n)}updateSearch(n){this.search.set(n.target.value),this.selectedEdge.set(null)}count(n){return $n.count(n,this.basis())}cellLabel(n){return this.matrixPercent()?`${$n.parentPercent(n,this.store.componentUsage()).toFixed(1)}%`:this.count(n).toLocaleString()}cellColor(n){let e=this.matrixPercent()?$n.parentPercent(n,this.store.componentUsage())/100:this.count(n)/(this.matrix().maximum||1);return`rgba(0, 137, 123, ${.08+Math.max(0,Math.min(1,e))*.65})`}cellDescription(n){return`${n.parent}, ${n.role}, ${n.child}: ${this.cellLabel(n)}`}inspectCandidate(n){this.dialog.open(us,{data:n,width:`46rem`,maxWidth:`94vw`})}retryPage(){this.pageRefresh.update(n=>n+1)}async loadPage(n){let e=++this.request,i=this.store.componentUsage()?.runId;this.loading.set(!0),this.pageError.set(null);let a=JSON.stringify([i,n]);a!==this.pageKey&&this.page.set(null),this.pageKey=a;try{let o=await this.store.loadComponentCandidates(n);if(this.destroyed||e!==this.request)return;if(o.runId!==i||o.scope!==n.scope)throw new Error(`Configurations belong to a different run or scope`);if(n.offset>0&&o.total<=n.offset){this.offset.set(0);return}this.page.set(o)}catch(o){!this.destroyed&&e===this.request&&this.pageError.set(o instanceof Error?o.message:`Unable to load configurations`)}finally{!this.destroyed&&e===this.request&&this.loading.set(!1)}}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-component-usage-dashboard`]],decls:33,vars:7,consts:[[1,`component-page`],[1,`controls`],[`appearance`,`outline`],[3,`selectionChange`,`value`],[`value`,`ALL`],[`value`,`CURRENT_ELITES`],[`value`,`CANDIDATE`],[`value`,`EVALUATION`],[`matInput`,``,3,`input`,`value`],[`aria-label`,`Component visualization`,3,`change`,`value`],[`value`,`usage`],[`value`,`matrix`],[1,`explanation`],[`role`,`alert`,1,`warning`],[1,`empty`],[`mat-button`,``,3,`click`],[1,`totals`],[1,`warning`],[1,`layout`],[`appearance`,`outlined`,1,`visualization`],[`appearance`,`outlined`,1,`inspector`],[`appearance`,`outlined`,1,`wide`],[`role`,`alert`],[1,`table-scroll`],[1,`candidate-table`],[`scope`,`col`],[1,`pagination`],[`mat-button`,``,3,`click`,`disabled`],[3,`model`],[`aria-label`,`Select a component`,1,`component-list`],[`type`,`button`,3,`selected`],[`type`,`button`,3,`click`],[1,`matrix-controls`],[3,`value`],[`aria-label`,`Matrix values`,3,`change`,`value`],[`tabindex`,`0`,`aria-label`,`Scrollable component relationship matrix`,1,`matrix-scroll`],[1,`matrix`],[`scope`,`row`],[`type`,`button`,3,`background-color`],[1,`relationships`],[`type`,`button`],[`colspan`,`5`]],template:function(e,i){if(e&1&&(l(0,`section`,0)(1,`div`,1)(2,`mat-form-field`,2)(3,`mat-label`),u(4,`Configurations`),d(),l(5,`mat-select`,3),C(`selectionChange`,function(o){return i.store.selectComponentScope(o.value)}),l(6,`mat-option`,4),u(7,`All evaluated candidates`),d(),l(8,`mat-option`,5),u(9,`Current elites`),d()()(),l(10,`mat-form-field`,2)(11,`mat-label`),u(12,`Count by`),d(),l(13,`mat-select`,3),C(`selectionChange`,function(o){return i.basis.set(o.value)}),l(14,`mat-option`,6),u(15,`Candidate structures`),d(),l(16,`mat-option`,7),u(17,`Evaluation exposure`),d()()(),l(18,`mat-form-field`,2)(19,`mat-label`),u(20,`Find component`),d(),l(21,`input`,8),C(`input`,function(o){return i.updateSearch(o)}),d()(),l(22,`mat-button-toggle-group`,9),C(`change`,function(o){return i.view.set(o.value)}),l(23,`mat-button-toggle`,10),u(24,`Usage map`),d(),l(25,`mat-button-toggle`,11),u(26,`Relationship matrix`),d()()(),l(27,`p`,12),D(28,XP,1,0)(29,ZP,1,0),d(),D(30,JP,4,1,`p`,13),D(31,EF,30,18)(32,DF,2,0,`p`,14),d()),e&2){let a,o;h(5),S(`value`,i.store.componentScope()),h(8),S(`value`,i.basis()),h(8),S(`value`,i.search()),h(),S(`value`,i.view()),h(6),k(i.basis()===`CANDIDATE`?28:29),h(2),k((a=i.store.componentError())?30:-1,a),h(),k((o=i.store.componentUsage())?31:i.store.componentError()?-1:32,o)}},dependencies:[Ot,en,eE,Bb,Am,zn,Bn,jn,Ma,Fu,Ia,ui,Ni,na,Qr,Kr,Pa,Ra,Un,Ai,wn],styles:[`[_nghost-%COMP%]{display:block}.component-page[_ngcontent-%COMP%]{padding:1.4rem 0}.controls[_ngcontent-%COMP%], .matrix-controls[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:1rem;align-items:center}.controls[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{flex:1 1 12rem}.explanation[_ngcontent-%COMP%]{color:#526477;line-height:1.5;font-size:.9rem;margin:.5rem 0 1.25rem}.totals[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(4,1fr);gap:1rem;margin-bottom:1.4rem}.totals[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{border:1px solid #d9e3ec;border-radius:.8rem;padding:1rem;background:#fff}.totals[_ngcontent-%COMP%]   span[_ngcontent-%COMP%], .totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{display:block}.totals[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:#526477;font-size:.85rem}.totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{color:#174b80;margin-top:.4rem;font-size:1.5rem}.warning[_ngcontent-%COMP%]{background:#fff5dc;padding:.8rem 1rem;border-radius:.6rem}.empty[_ngcontent-%COMP%]{padding:2rem;text-align:center;color:#526477}.layout[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(0,2fr) minmax(18rem,1fr);gap:1.25rem}.layout[_ngcontent-%COMP%] > *[_ngcontent-%COMP%]{min-width:0}.wide[_ngcontent-%COMP%]{grid-column:1/-1}mat-card-header[_ngcontent-%COMP%]{margin-bottom:1rem}.component-list[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.3rem;max-height:15rem;overflow:auto;margin-top:1rem}.component-list[_ngcontent-%COMP%]   button[_ngcontent-%COMP%], .relationships[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{border:0;background:#f3f7fb;cursor:pointer;border-radius:.4rem;padding:.65rem;display:flex;justify-content:space-between;gap:.5rem;text-align:left;color:#193b59}.component-list[_ngcontent-%COMP%]   button.selected[_ngcontent-%COMP%]{background:#d5e8f7;outline:2px solid #2265a7;outline-offset:-2px}.component-list[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{overflow-wrap:anywhere}.relationships[_ngcontent-%COMP%]{max-height:17rem;overflow:auto;display:grid;gap:.35rem}.relationships[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{display:block;color:#526477;margin-top:.25rem}.relationships[_ngcontent-%COMP%]   button[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{text-align:right}.relationships[_ngcontent-%COMP%]   p[_ngcontent-%COMP%]{color:#526477}dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:1fr auto;gap:.8rem;font-size:.9rem}dt[_ngcontent-%COMP%]{color:#526477}dd[_ngcontent-%COMP%]{margin:0;text-align:right}h3[_ngcontent-%COMP%]{font-size:1rem;margin:1.4rem 0 .7rem}.matrix-scroll[_ngcontent-%COMP%], .table-scroll[_ngcontent-%COMP%]{overflow:auto}table[_ngcontent-%COMP%]{border-collapse:separate;border-spacing:0;font-size:.85rem;width:100%}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.65rem;border-bottom:1px solid #dce5ee;text-align:left}th[_ngcontent-%COMP%]{color:#526477;font-weight:600}.matrix[_ngcontent-%COMP%]   caption[_ngcontent-%COMP%]{text-align:left;padding:.5rem 0;color:#526477}.matrix[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]{min-width:7rem}.matrix[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{display:block;margin-top:.2rem;font-weight:400}.matrix[_ngcontent-%COMP%]   tbody[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]{position:sticky;left:0;background:#fff;z-index:1}.matrix[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]{padding:.2rem;text-align:center}.matrix[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{width:100%;min-width:5rem;min-height:2.8rem;border:0;border-radius:.25rem;cursor:pointer;color:#112e3b}.pagination[_ngcontent-%COMP%]{display:flex;justify-content:flex-end;align-items:center;flex-wrap:wrap;gap:.7rem;margin-top:.8rem}.pagination[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:first-child{margin-right:auto}@media(max-width:1050px){.layout[_ngcontent-%COMP%]{grid-template-columns:1fr}.totals[_ngcontent-%COMP%]{grid-template-columns:repeat(2,1fr)}}@media(max-width:600px){.component-list[_ngcontent-%COMP%]{grid-template-columns:1fr}.controls[_ngcontent-%COMP%]   mat-button-toggle-group[_ngcontent-%COMP%], .matrix-controls[_ngcontent-%COMP%]   mat-button-toggle-group[_ngcontent-%COMP%]{max-width:100%;overflow:auto}.totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font-size:1.2rem}}`]})};var Rm=()=>[];var kF=(t,n)=>n.key;var iE=(t,n)=>n.id;var aE=(t,n)=>n.configurationId;var IF=(t,n)=>n.instanceName;var MF=(t,n)=>n.iteration;function NF(t,n){if(t&1){let e=X();l(0,`button`,19),C(`click`,function(){A(e);return O(y().showIssues())}),l(1,`span`,53)(2,`strong`),u(3),d(),l(4,`small`),u(5),d()()()}if(t&2){let e=n.$implicit;h(2),q(`critical`,e.severity===`critical`),h(),w(e.title),h(2),w(e.message)}}function TF(t,n){t&1&&(l(0,`span`,10),u(1,`No evaluation alerts`),d())}function AF(t,n){if(t&1){let e=X();l(0,`button`,19),C(`click`,function(){A(e);return O(y().showIssues())}),l(1,`strong`),u(2,`View all issues`),d()()}}function OF(t,n){if(t&1&&(l(0,`section`,11)(1,`div`,54)(2,`span`,55),u(3,`Evaluation budget`),d(),l(4,`strong`),u(5),U(6,`number`),U(7,`number`),d(),l(8,`span`),u(9),U(10,`number`),d()(),z(11,`mat-progress-bar`,56),l(12,`div`,57),z(13,`span`,58),u(14),d()()),t&2){let e=y(),i=Xf(0);h(5),Fe(``,_e(6,7,i.budget.used),` / `,_e(7,9,i.budget.maximum)),h(4),H(``,_e(10,11,i.budget.remaining),` remaining`),h(2),S(`value`,e.store.budgetPercent()),h(2),q(`complete`,i.state===`COMPLETED`),h(),H(` `,e.phaseLabel(i.phase),` `)}}function RF(t,n){if(t&1){let e=X();l(0,`section`,12)(1,`strong`),u(2,`Monitor connection problem`),d(),l(3,`span`),u(4),d(),l(5,`button`,50),C(`click`,function(){A(e);return O(y().store.retryNow())}),u(6,`Retry now`),d()()}t&2&&(h(4),w(n))}function PF(t,n){if(t&1&&(l(0,`section`,13)(1,`strong`),u(2),d(),l(3,`span`),u(4),d()()),t&2){let e=n;h(2),w(e.type),h(2),w(e.message??`The tuning run stopped unexpectedly.`)}}function FF(t,n){if(t&1){let e=X();l(0,`button`,19),C(`click`,function(){A(e);let a=y().$implicit;return O(y().preferences.show(a.id))}),u(1),d()}if(t&2){let e=y().$implicit;h(),H(` Show `,e.label,` `)}}function LF(t,n){if(t&1&&D(0,FF,2,1,`button`,9),t&2){let e=n.$implicit;k(y().preferences.isHidden(e.id)?0:-1)}}function VF(t,n){if(t&1){let e=X();l(0,`button`,59),C(`click`,function(){let a=A(e).$implicit;return O(y().inspectCandidate(a.configurationId))}),l(1,`span`,60),u(2),d(),l(3,`span`)(4,`strong`),u(5),d(),l(6,`small`),u(7),d()()()}if(t&2){let e=n.$implicit,i=y();h(),tt(i.rankClass(e.position)),h(),w(e.position),h(3),H(`Configuration `,e.configurationId),h(2),w(i.parameterPreview(e))}}function BF(t,n){t&1&&(l(0,`p`,32),u(1,`Elites appear after the first completed race checkpoint.`),d())}function jF(t,n){if(t&1){let e=X();l(0,`button`,61),C(`click`,function(){A(e);return O(y().inspectSearchSpace())}),u(1,` Inspect search space `),d()}}function zF(t,n){if(t&1&&(l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),U(7,`number`),d(),l(8,`td`),u(9),d(),l(10,`td`),u(11),d()()),t&2){let e=n.$implicit;h(2),w(e.instanceName),h(2),w(e.evaluations),h(2),H(` `,e.bestCost===null?`—`:Ie(7,5,e.bestCost,`1.0-5`),` `),h(3),w(e.issues),h(2),w(e.slow)}}function HF(t,n){t&1&&(l(0,`tr`)(1,`td`,62),u(2,`No instance activity yet.`),d()())}function UF(t,n){t&1&&u(0,` Final result `)}function $F(t,n){t&1&&u(0,` Latest completed checkpoint `)}function GF(t,n){if(t&1){let e=X();l(0,`button`,59),C(`click`,function(){let a=A(e).$implicit;return O(y().inspectCandidate(a.configurationId))}),l(1,`span`,60),u(2),d(),l(3,`span`)(4,`strong`),u(5),d(),l(6,`small`),u(7),d()()()}if(t&2){let e=n.$implicit,i=y();h(),tt(i.rankClass(e.position)),h(),w(e.position),h(3),H(`Configuration `,e.configurationId),h(2),w(i.parameterPreview(e))}}function WF(t,n){t&1&&(l(0,`p`,32),u(1,`No elite snapshot is available yet.`),d())}function qF(t,n){if(t&1&&(l(0,`article`)(1,`strong`),u(2),d(),l(3,`span`),u(4),U(5,`date`),d(),l(6,`span`),u(7),U(8,`number`),d(),l(9,`span`),u(10),d()()),t&2){let e=n.$implicit;h(2),H(`Checkpoint `,e.iteration),h(2),w(Ie(5,4,e.updatedAt,`medium`)),h(3),H(``,_e(8,7,e.progress.experimentsUsed),` evaluations used`),h(3),H(``,e.elites.length,` elites`)}}function YF(t,n){t&1&&(l(0,`p`,32),u(1,`No completed race checkpoints yet.`),d())}function KF(t,n){t&1&&z(0,`app-component-usage-dashboard`)}function QF(t,n){t&1&&(l(0,`mat-tab`,48),ot(1,KF,1,0,`ng-template`,63),d())}function XF(t,n){if(t&1&&(l(0,`a`,52)(1,`span`)(2,`strong`),u(3),d(),l(4,`small`),u(5),d()(),l(6,`span`),u(7),d(),l(8,`span`),u(9,`Download`),d()()),t&2){let e=n.$implicit,i=y();S(`href`,i.store.artifactUrl(e.id),pn),h(3),w(e.filename),h(2),w(e.mediaType),h(2),w(i.artifactSize(e))}}function ZF(t,n){t&1&&(l(0,`p`,32),u(1,` Artifacts appear as IRACE writes logs, plots, workspace data, and final elites. `),d())}var Pm=class t{store=f(ja);preferences=f(Va);panels=Tb;selectedTab=N(0);forceIssues=N(!1);dialog=f(zo);constructor(){this.store.start()}selectTab(n){this.selectedTab.set(n.index),n.tab.textLabel!==`Evaluations`&&this.forceIssues.set(!1),n.tab.textLabel===`Artifacts`&&this.store.refreshArtifacts()}showIssues(){this.forceIssues.set(!0),this.selectedTab.set(1)}inspectCandidate(n){this.dialog.open(us,{data:n,width:`46rem`,maxWidth:`94vw`})}inspectSearchSpace(){let n=this.store.searchSpace();n&&this.dialog.open(Nm,{data:n,maxWidth:`96vw`})}rankClass(n){return n<=3?`rank-${n}`:`rank-other`}phaseLabel(n){return n.toLowerCase().split(`_`).map(e=>e.charAt(0).toUpperCase()+e.slice(1)).join(` `)}elapsedLabel(n){if(n===null)return`—`;let e=Math.floor(n/1e3),i=Math.floor(e/3600),a=Math.floor(e%3600/60),o=e%60;return i>0?`${i}h ${a}m`:`${a}m ${o}s`}metricLabel(){let n=this.store.status()?.metric;if(!n)return`Waiting for metric details`;let e=n.kind===`AREA_UNDER_CURVE`?`Area under curve`:`Final objective`,i=n.negated?`maximized, converted to cost`:`minimized`;return`${n.objectiveName} \xB7 ${e} \xB7 ${i}`}parameterPreview(n){let e=Object.entries(n.parameters);return e.length===0?`No parameters`:e.slice(0,3).map(([i,a])=>`${i}=${a}`).join(` · `)}artifactSize(n){return n.size<1024?`${n.size} B`:n.size<1048576?`${(n.size/1024).toFixed(1)} KB`:`${(n.size/1048576).toFixed(1)} MB`}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-tuning-dashboard`]],decls:183,vars:47,consts:[[`alertsMenu`,`matMenu`],[`panelMenu`,`matMenu`],[1,`topbar`],[1,`brand`],[`src`,`favicon.svg`,`alt`,``],[1,`toolbar-spacer`],[`aria-label`,`Synchronization state`],[`mat-button`,``,`type`,`button`,`matBadgeColor`,`warn`,`aria-label`,`Tuning alerts`,1,`alerts-button`,3,`menuOpened`,`matMenuTriggerFor`,`matBadge`,`matBadgeHidden`],[1,`alerts-menu`],[`mat-menu-item`,``,`type`,`button`],[1,`menu-empty`],[`aria-label`,`Evaluation budget`,1,`budget-ribbon`],[`role`,`alert`,1,`request-error`],[`role`,`alert`,1,`run-error`],[`animationDuration`,`150ms`,3,`selectedTabChange`,`selectedIndex`],[`label`,`Overview`],[1,`tab-page`,`overview`],[1,`overview-actions`],[`mat-button`,``,`type`,`button`,3,`matMenuTriggerFor`],[`mat-menu-item`,``,`type`,`button`,3,`click`],[`panelId`,`evaluation-health`,`title`,`Evaluation health`,1,`wide`],[1,`kpi-grid`],[1,`kpi`],[1,`kpi`,`success`],[1,`kpi`,`warning`],[1,`kpi`,`danger`],[`panelId`,`evaluation-cost`,`title`,`Evaluation cost`],[3,`model`],[`panelId`,`elite-evolution`,`title`,`Elite rank evolution`],[`panelId`,`current-elites`,`title`,`Current elites`],[1,`elite-list`],[`type`,`button`,1,`elite-row`],[1,`empty-state`],[`panelId`,`run-information`,`title`,`Run information`],[1,`run-info`],[`mat-stroked-button`,``,`type`,`button`],[`label`,`Evaluations`],[1,`tab-page`],[1,`two-column`],[`appearance`,`outlined`],[1,`summary-scroll`],[1,`summary-table`],[`appearance`,`outlined`,1,`evaluation-card`],[3,`candidateSelected`,`evaluations`,`forceIssues`],[`label`,`Elites`],[1,`two-column`,`elite-layout`],[1,`elite-list`,`detailed`],[1,`history-list`],[`label`,`Components`],[`label`,`Artifacts`],[`mat-button`,``,`type`,`button`,3,`click`],[1,`artifact-list`],[`download`,``,3,`href`],[1,`alert-item`],[1,`budget-copy`],[1,`eyebrow`],[`mode`,`determinate`,3,`value`],[1,`phase`],[1,`phase-dot`],[`type`,`button`,1,`elite-row`,3,`click`],[1,`medal`],[`mat-stroked-button`,``,`type`,`button`,3,`click`],[`colspan`,`5`,1,`empty-state`],[`matTabContent`,``]],template:function(e,i){if(e&1&&(Zd(0),l(1,`mat-toolbar`,2)(2,`span`,3),z(3,`img`,4),u(4,` Mork tuning monitor`),d(),z(5,`span`,5),l(6,`mat-chip-set`,6)(7,`mat-chip`),u(8),d()(),l(9,`button`,7),C(`menuOpened`,function(){return i.store.markAlertsSeen()}),u(10,` Alerts `),d(),l(11,`mat-menu`,8,0),be(13,NF,6,4,`button`,9,kF,!1,TF,2,0,`span`,10),D(16,AF,3,0,`button`,9),d()(),D(17,OF,15,13,`section`,11),l(18,`main`),D(19,RF,7,1,`section`,12),D(20,PF,5,2,`section`,13),l(21,`mat-tab-group`,14),C(`selectedTabChange`,function(o){return i.selectTab(o)}),l(22,`mat-tab`,15)(23,`section`,16)(24,`div`,17)(25,`span`),u(26,`Live view of the current tuning run`),d(),l(27,`button`,18),u(28,`Panels`),d(),l(29,`mat-menu`,null,1),be(31,LF,1,1,null,null,iE),l(33,`button`,19),C(`click`,function(){return i.preferences.reset()}),u(34,` Restore all panels `),d()()(),l(35,`app-monitor-panel`,20)(36,`section`,21)(37,`article`,22)(38,`span`),u(39,`Running`),d(),l(40,`strong`),u(41),d()(),l(42,`article`,23)(43,`span`),u(44,`Succeeded`),d(),l(45,`strong`),u(46),d()(),l(47,`article`,24)(48,`span`),u(49,`Rejected`),d(),l(50,`strong`),u(51),d()(),l(52,`article`,25)(53,`span`),u(54,`Failed`),d(),l(55,`strong`),u(56),d()(),l(57,`article`,24)(58,`span`),u(59,`Slow`),d(),l(60,`strong`),u(61),d()()()(),l(62,`app-monitor-panel`,26),z(63,`app-dashboard-chart`,27),d(),l(64,`app-monitor-panel`,28),z(65,`app-dashboard-chart`,27),d(),l(66,`app-monitor-panel`,29)(67,`div`,30),be(68,VF,8,5,`button`,31,aE,!1,BF,2,0,`p`,32),d()(),l(71,`app-monitor-panel`,33)(72,`dl`,34)(73,`dt`),u(74,`Run ID`),d(),l(75,`dd`),u(76),d(),l(77,`dt`),u(78,`Mode`),d(),l(79,`dd`),u(80),d(),l(81,`dt`),u(82,`Elapsed`),d(),l(83,`dd`),u(84),d(),l(85,`dt`),u(86,`Training instances`),d(),l(87,`dd`),u(88),d(),l(89,`dt`),u(90,`Generated parameters`),d(),l(91,`dd`),u(92),d(),l(93,`dt`),u(94,`Cost metric`),d(),l(95,`dd`),u(96),d(),l(97,`dt`),u(98,`Last update`),d(),l(99,`dd`),u(100),U(101,`date`),d()(),D(102,jF,2,0,`button`,35),d()()(),l(103,`mat-tab`,36)(104,`section`,37)(105,`section`,38)(106,`mat-card`,39)(107,`mat-card-header`)(108,`mat-card-title`),u(109,`Evaluation activity`),d()(),l(110,`mat-card-content`),z(111,`app-dashboard-chart`,27),d()(),l(112,`mat-card`,39)(113,`mat-card-header`)(114,`mat-card-title`),u(115,`Instance summary`),d()(),l(116,`mat-card-content`,40)(117,`table`,41)(118,`thead`)(119,`tr`)(120,`th`),u(121,`Instance`),d(),l(122,`th`),u(123,`Runs`),d(),l(124,`th`),u(125,`Best cost`),d(),l(126,`th`),u(127,`Issues`),d(),l(128,`th`),u(129,`Slow`),d()()(),l(130,`tbody`),be(131,zF,12,8,`tr`,null,IF,!1,HF,3,0,`tr`),d()()()()(),l(134,`mat-card`,42)(135,`mat-card-header`)(136,`mat-card-title`),u(137,`Evaluation ledger`),d()(),l(138,`mat-card-content`)(139,`app-evaluation-table`,43),C(`candidateSelected`,function(o){return i.inspectCandidate(o)}),d()()()()(),l(140,`mat-tab`,44)(141,`section`,37)(142,`section`,45)(143,`mat-card`,39)(144,`mat-card-header`)(145,`mat-card-title`),u(146,`Current elite set`),d(),l(147,`mat-card-subtitle`),D(148,UF,1,0)(149,$F,1,0),d()(),l(150,`mat-card-content`)(151,`div`,46),be(152,GF,8,5,`button`,31,aE,!1,WF,2,0,`p`,32),d()()(),l(155,`mat-card`,39)(156,`mat-card-header`)(157,`mat-card-title`),u(158,`Rank history`),d()(),l(159,`mat-card-content`),z(160,`app-dashboard-chart`,27),d()()(),l(161,`mat-card`,39)(162,`mat-card-header`)(163,`mat-card-title`),u(164,`Race checkpoints`),d()(),l(165,`mat-card-content`,47),be(166,qF,11,9,`article`,null,MF,!1,YF,2,0,`p`,32),d()()()(),D(169,QF,2,0,`mat-tab`,48),l(170,`mat-tab`,49)(171,`section`,37)(172,`mat-card`,39)(173,`mat-card-header`)(174,`mat-card-title`),u(175,`Run artifacts`),d(),z(176,`span`,5),l(177,`button`,50),C(`click`,function(){return i.store.refreshArtifacts()}),u(178,`Refresh`),d()(),l(179,`mat-card-content`,51),be(180,XF,10,4,`a`,52,iE,!1,ZF,2,0,`p`,32),d()()()()()()),e&2){let a,o,r=ht(12),s=ht(30),c=Qf(i.store.status());h(7),q(`live`,i.store.syncState()===`live`),h(),w(i.store.connectionLabel()),h(),S(`matMenuTriggerFor`,r)(`matBadge`,i.store.unseenAlertCount())(`matBadgeHidden`,i.store.unseenAlertCount()===0),h(4),ve(i.store.alerts().slice(0,10)),h(3),k(i.store.alerts().length>0?16:-1),h(),k(c?17:-1),h(2),k((a=i.store.latestError())?19:-1,a),h(),k((o=c?.failure)?20:-1,o),h(),S(`selectedIndex`,i.selectedTab()),h(6),S(`matMenuTriggerFor`,s),h(4),ve(i.panels),h(10),w(c?.evaluations?.running??0),h(5),w(c?.evaluations?.succeeded??0),h(5),w(c?.evaluations?.rejected??0),h(5),w(c?.evaluations?.failed??0),h(5),w(c?.evaluations?.slow??0),h(2),S(`model`,i.store.evaluationCostChart()),h(2),S(`model`,i.store.eliteRankChart()),h(3),ve(i.store.elites()?.elites??vt(43,Rm)),h(8),w(c?.runId??`—`),h(4),w(c?.mode??`—`),h(4),w(i.elapsedLabel(c?.elapsedMillis??null)),h(4),w(c?.trainingInstanceCount??`Preparing…`),h(4),w(c?.generatedParameterCount??0),h(4),w(i.metricLabel()),h(4),w(Ie(101,40,i.store.lastUpdated(),`mediumTime`)),h(2),k(i.store.searchSpace()?102:-1),h(9),S(`model`,i.store.evaluationActivityChart()),h(20),ve(i.store.instanceSummaries()),h(8),S(`evaluations`,i.store.evaluations())(`forceIssues`,i.forceIssues()),h(9),k(i.store.elites()?.finalSnapshot?148:149),h(4),ve(i.store.elites()?.elites??vt(44,Rm)),h(8),S(`model`,i.store.eliteRankChart()),h(6),ve(i.store.eliteHistory()?.iterations??vt(45,Rm)),h(3),k(c?.mode===`AUTOCONFIG`?169:-1),h(11),ve(i.store.artifacts()?.artifacts??vt(46,Rm))}},dependencies:[Ai,Mm,zw,jw,Ot,en,zn,Bn,jn,Ma,Fu,Ia,em,Bc,Ju,as,Ti,ca,is,rs,os,hm,qc,Yc,pm,gm,fm,ls,Om,Xi,wn],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh;color:var(--%NS%mat-sys-on-surface);background:radial-gradient(circle at 92% -5%,color-mix(in srgb,var(--%NS%mat-sys-tertiary) 14%,transparent),transparent 28rem),var(--%NS%mat-sys-surface-container-lowest)}.topbar[_ngcontent-%COMP%]{position:sticky;top:0;z-index:20;gap:.75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:color-mix(in srgb,var(--%NS%mat-sys-surface) 94%,transparent);-webkit-backdrop-filter:blur(12px);backdrop-filter:blur(12px)}.brand[_ngcontent-%COMP%]{display:flex;gap:.65rem;align-items:center;font:var(--%NS%mat-sys-title-large);font-weight:750;letter-spacing:-.025em}.brand[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:2rem;height:2rem}.toolbar-spacer[_ngcontent-%COMP%]{flex:1}mat-chip.live[_ngcontent-%COMP%]{--%NS%mdc-chip-elevated-container-color: color-mix(in srgb, var(--%NS%mat-sys-tertiary) 20%, transparent)}.alerts-button[_ngcontent-%COMP%]{margin-right:.5rem}.alert-item[_ngcontent-%COMP%]{display:grid;width:min(24rem,72vw);line-height:1.25}.alert-item[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{overflow:hidden;color:var(--%NS%mat-sys-on-surface-variant);text-overflow:ellipsis}.critical[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}.menu-empty[_ngcontent-%COMP%]{display:block;padding:1rem;color:var(--%NS%mat-sys-on-surface-variant)}.budget-ribbon[_ngcontent-%COMP%]{position:sticky;top:4rem;z-index:15;display:grid;grid-template-columns:minmax(15rem,auto) minmax(12rem,1fr) auto;gap:1.5rem;align-items:center;padding:.65rem max(1rem,(100% - 96rem) / 2);border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:color-mix(in srgb,var(--%NS%mat-sys-surface-container-low) 96%,transparent);-webkit-backdrop-filter:blur(12px);backdrop-filter:blur(12px)}.budget-copy[_ngcontent-%COMP%]{display:flex;gap:.55rem;align-items:baseline;white-space:nowrap}.budget-copy[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-body-small)}.eyebrow[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-small);text-transform:uppercase;letter-spacing:.07em}.phase[_ngcontent-%COMP%]{display:flex;gap:.4rem;align-items:center;font:var(--%NS%mat-sys-label-large);white-space:nowrap}.phase-dot[_ngcontent-%COMP%]{width:.6rem;height:.6rem;border-radius:50%;background:var(--%NS%mat-sys-tertiary);box-shadow:0 0 0 .2rem color-mix(in srgb,var(--%NS%mat-sys-tertiary) 18%,transparent)}.phase-dot.complete[_ngcontent-%COMP%]{background:#079447}main[_ngcontent-%COMP%]{width:min(100% - 2rem,96rem);margin:0 auto;padding-bottom:4rem}.request-error[_ngcontent-%COMP%], .run-error[_ngcontent-%COMP%]{display:flex;gap:.75rem;align-items:center;margin:1rem 0 0;padding:.75rem 1rem;border:1px solid var(--%NS%mat-sys-error);border-radius:.75rem;color:var(--%NS%mat-sys-on-error-container);background:var(--%NS%mat-sys-error-container)}.request-error[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{margin-left:auto}.tab-page[_ngcontent-%COMP%]{display:grid;gap:1rem;padding:1.25rem 0}.overview[_ngcontent-%COMP%]{grid-template-columns:repeat(2,minmax(0,1fr))}.wide[_ngcontent-%COMP%], .overview-actions[_ngcontent-%COMP%]{grid-column:1/-1}.overview-actions[_ngcontent-%COMP%]{display:flex;align-items:center;min-height:2rem;color:var(--%NS%mat-sys-on-surface-variant)}.overview-actions[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{margin-left:auto}.kpi-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(5,minmax(7rem,1fr));gap:.75rem}.kpi[_ngcontent-%COMP%]{display:grid;gap:.2rem;padding:.8rem 1rem;border-left:.25rem solid var(--%NS%mat-sys-primary);border-radius:.4rem;background:var(--%NS%mat-sys-surface-container-low)}.kpi[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-medium)}.kpi[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-headline-small)}.kpi.success[_ngcontent-%COMP%]{border-color:#079447}.kpi.warning[_ngcontent-%COMP%]{border-color:#ed8e00}.kpi.danger[_ngcontent-%COMP%]{border-color:var(--%NS%mat-sys-error)}.elite-list[_ngcontent-%COMP%]{display:grid;gap:.5rem}.elite-row[_ngcontent-%COMP%]{display:grid;grid-template-columns:2.3rem minmax(0,1fr);gap:.75rem;align-items:center;width:100%;padding:.65rem;border:1px solid transparent;border-radius:.5rem;color:inherit;background:transparent;text-align:left;cursor:pointer}.elite-row[_ngcontent-%COMP%]:hover{border-color:var(--%NS%mat-sys-outline-variant);background:var(--%NS%mat-sys-surface-container-low)}.elite-row[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{display:grid;min-width:0}.elite-row[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{overflow:hidden;color:var(--%NS%mat-sys-on-surface-variant);text-overflow:ellipsis;white-space:nowrap}.medal[_ngcontent-%COMP%]{display:grid;place-items:center;width:2rem;height:2rem;border-radius:50%;color:#292929;background:#d1d5db;font-weight:750}.rank-1[_ngcontent-%COMP%]{background:#f2c94c}.rank-2[_ngcontent-%COMP%]{background:#c7ccd4}.rank-3[_ngcontent-%COMP%]{background:#cd8b55}.rank-other[_ngcontent-%COMP%]{background:#e5e7eb}.run-info[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(8rem,auto) minmax(0,1fr);gap:.55rem 1rem;margin:0 0 1rem}.run-info[_ngcontent-%COMP%]   dt[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.run-info[_ngcontent-%COMP%]   dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}.two-column[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem}mat-card-header[_ngcontent-%COMP%]{align-items:center;padding-bottom:.75rem}.summary-scroll[_ngcontent-%COMP%]{max-height:21rem;overflow:auto}.summary-table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse}.summary-table[_ngcontent-%COMP%]   th[_ngcontent-%COMP%], .summary-table[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]{padding:.55rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left}.evaluation-card[_ngcontent-%COMP%]   mat-card-content[_ngcontent-%COMP%]{min-width:0}.history-list[_ngcontent-%COMP%]{display:grid;gap:.5rem}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{display:grid;grid-template-columns:1fr repeat(3,auto);gap:1rem;padding:.75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.artifact-list[_ngcontent-%COMP%]{display:grid}.artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(0,1fr) auto auto;gap:1.5rem;align-items:center;padding:1rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);color:var(--%NS%mat-sys-primary);text-decoration:none}.artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:first-child{display:grid}.artifact-list[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.empty-state[_ngcontent-%COMP%]{padding:1.5rem 0;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}@media(max-width:62rem){.overview[_ngcontent-%COMP%], .two-column[_ngcontent-%COMP%]{grid-template-columns:1fr}.kpi-grid[_ngcontent-%COMP%]{grid-template-columns:repeat(3,1fr)}.budget-ribbon[_ngcontent-%COMP%]{grid-template-columns:1fr auto}.budget-ribbon[_ngcontent-%COMP%]   mat-progress-bar[_ngcontent-%COMP%]{grid-column:1/-1;grid-row:2}}@media(max-width:42rem){.topbar[_ngcontent-%COMP%]   mat-chip-set[_ngcontent-%COMP%]{display:none}.brand[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-medium)}.budget-ribbon[_ngcontent-%COMP%]{top:3.5rem;gap:.5rem}.budget-copy[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child, .eyebrow[_ngcontent-%COMP%]{display:none}main[_ngcontent-%COMP%]{width:min(100% - 1rem,96rem)}.kpi-grid[_ngcontent-%COMP%]{grid-template-columns:repeat(2,1fr)}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%], .artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{grid-template-columns:1fr;gap:.2rem}}`]})};var Fm=class t{http=f(Di);destroy=f(Te);base=`${kn.apiBaseUrl}/api/autollm`;get(n,e={}){return Rt(this.http.get(this.base+n,{params:e}).pipe(Dn(this.destroy)))}post(n,e={}){return Rt(this.http.post(this.base+n,e).pipe(Dn(this.destroy)))}defaults(){return this.get(`/defaults`)}validate(n){return this.post(`/validate`,n)}probeTarget(n){return this.post(`/targets/probe`,n)}list(){return this.get(`/campaigns`)}create(n){return this.post(`/campaigns`,n)}baselineCheck(n){return this.post(`/campaigns/${encodeURIComponent(n)}/baseline-check`)}action(n,e){return this.post(`/campaigns/${encodeURIComponent(n)}/${e}`)}progress(n,e){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/progress`)}summary(n){return this.get(`/campaigns/${encodeURIComponent(n)}/summary`)}activity(n,e,i,a){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/activity`,{attempt:i,cursor:a})}comparison(n,e){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/comparison`)}artifacts(n){return this.get(`/campaigns/${encodeURIComponent(n)}/artifacts`)}log(n,e,i){return this.get(`/campaigns/${encodeURIComponent(n)}/log`,{path:e,cursor:i,maximumBytes:65536})}artifactUrl(n,e){return`${this.base}/campaigns/${encodeURIComponent(n)}/artifact?${new URLSearchParams({path:e})}`}screenings(){return this.get(`/screenings`)}createScreening(n,e,i){return this.post(`/screenings`,{config:n,inputPath:e,trainingFraction:i,settings:{sotaFile:n.sotaFile,python:n.execution.ssh?.environment?.MORK_SELECTOR_PYTHON??`python3`}})}screeningAction(n,e,i){return this.post(`/screenings/${encodeURIComponent(n)}/${e}`,{proposalSha256:i})}screeningReview(n){return this.get(`/screenings/${encodeURIComponent(n)}/review`)}screeningArtifactUrl(n,e){return`${this.base}/screenings/${encodeURIComponent(n)}/artifact?${new URLSearchParams({path:e})}`}static ɵfac=function(e){return new(e||t)};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})};var Lm=class t{client=f(Fm);destroy=f(Te);config=N(null);campaigns=N([]);active=N(``);selectedId=N(``);selectedTrial=N(1);campaign=ne(()=>this.campaigns().find(n=>n.id===this.selectedId())??null);trial=ne(()=>this.campaign()?.trials.find(n=>n.number===this.selectedTrial())??null);completed=ne(()=>this.campaign()?.trials.filter(n=>[`SUCCEEDED`,`FAILED`].includes(n.status)).length??0);progress=N(null);tuning=ne(()=>this.progress()?.workload?.budget?this.progress().workload:null);generation=ne(()=>this.progress()?.generation??null);generationAttempt=N(null);conversationAttempt=ne(()=>{let n=this.trial()?.attempts?.filter(e=>[`GENERATION`,`REPAIR`].includes(e.stage))??[];return n.find(e=>e.number===this.generationAttempt())??n.at(-1)??null});activity=N([]);screenings=N([]);activeScreening=N(``);selectedScreening=N(``);screening=ne(()=>this.screenings().find(n=>n.id===this.selectedScreening())??null);review=N(null);summary=N(null);comparison=N(null);artifacts=N([]);logPath=N(``);logText=N(``);error=N(null);connectionError=N(null);busy=N(!1);targetStatus=N(null);timer=null;started=!1;destroyed=!1;logOffset=0;activityKey=``;activityCursor=0;activityPending=``;operations=0;constructor(){this.destroy.onDestroy(()=>{this.destroyed=!0,this.timer&&clearTimeout(this.timer)})}async start(){if(!this.started){this.started=!0;try{this.config.set(await this.client.defaults())}catch(n){this.connectionError.set(this.message(n))}await this.poll()}}async poll(){if(!this.destroyed)try{this.config()||this.config.set(await this.client.defaults());let[n,e]=await Promise.all([this.client.list(),this.client.screenings()]);if(this.destroyed)return;if(this.connectionError.set(null),this.campaigns.set(n.campaigns),this.active.set(n.active),this.screenings.set(e.jobs),this.activeScreening.set(e.active),this.selectedScreening()||this.selectedScreening.set(e.active||e.jobs[0]?.id||``),this.selectedId()||this.selectedId.set(n.active||n.campaigns[0]?.id||``),this.trial()){let a=this.selectedId(),o=this.selectedTrial(),[r,s]=await Promise.all([this.client.progress(a,o),this.client.comparison(a,o)]);this.selectedId()===a&&this.selectedTrial()===o&&!this.destroyed&&(this.progress.set(r),this.comparison.set(s))}let i=this.conversationAttempt();if(i){let a=this.selectedId(),o=this.selectedTrial(),r=`${a}/${o}/${i.number}`;this.activityKey!==r&&(this.activityKey=r,this.activityCursor=0,this.activityPending=``,this.activity.set([]));let s=this.activityCursor,c=await this.client.activity(a,o,i.number,s);if(this.activityKey===r&&this.selectedId()===a&&this.selectedTrial()===o&&this.conversationAttempt()?.number===i.number&&!this.destroyed){c.reset&&(this.activity.set([]),this.activityPending=``);let p=(this.activityPending+c.text).split(`
`);this.activityPending=p.pop()??``;let m=[];for(let g of p)if(g.trim())try{m.push(JSON.parse(g))}catch{}this.activity.update(g=>[...g,...m].slice(-200)),this.activityCursor=c.nextCursor}}if(this.logPath()){let a=this.selectedId(),o=this.logPath(),r=this.logOffset,s=await this.client.log(a,o,r);a===this.selectedId()&&o===this.logPath()&&r===this.logOffset&&!this.destroyed&&(this.logOffset=s.nextCursor,this.logText.update(c=>((s.reset?``:c)+s.text).slice(-262144)))}}catch(n){this.destroyed||this.connectionError.set(this.message(n))}finally{this.destroyed||(this.timer=setTimeout(()=>{this.poll()},2e3))}}select(n){this.selectedId.set(n),this.selectedTrial.set(1),this.artifacts.set([]),this.selectLog(``),this.progress.set(null),this.comparison.set(null),this.watchGeneration(null)}selectTrial(n){this.selectedTrial.set(n),this.progress.set(null),this.comparison.set(null),this.selectLog(``),this.watchGeneration(null)}watchGeneration(n){this.generationAttempt.set(n),this.activityKey=``,this.activity.set([])}selectLog(n){this.logPath.set(n),this.logOffset=0,this.logText.set(``)}selectScreening(n){this.selectedScreening.set(n),this.review.set(null)}async createScreening(n,e){let i=this.config();i&&await this.operate(async()=>{let a=await this.client.createScreening(i,n,e);this.screenings.update(o=>[a,...o]),this.selectScreening(a.id),await this.client.screeningAction(a.id,`resume`),this.activeScreening.set(a.id)})}async reviewScreening(){let n=this.selectedScreening();await this.operate(async()=>{let e=await this.client.screeningReview(n);this.selectedScreening()===n&&this.review.set(s(r({},e),{jobId:n}))})}async screeningAction(n){let e=this.selectedScreening(),i=this.review();n===`approve`&&i?.jobId!==e||await this.operate(async()=>{await this.client.screeningAction(e,n,i?.proposalSha256)})}useSelection(){let n=this.screening();n?.status!==`APPROVED`||!n.mirror||this.config.update(e=>e&&s(r({},e),{trainingPath:`${n.mirror}/proposal/train.index`,comparisonPath:`${n.mirror}/proposal/test.index`}))}screeningArtifactUrl(n){return this.client.screeningArtifactUrl(this.selectedScreening(),n)}async refreshArtifacts(){if(!this.selectedId())return;let n=this.selectedId();try{let e=await this.client.artifacts(n);n===this.selectedId()&&!this.destroyed&&this.artifacts.set(e)}catch(e){this.error.set(this.message(e))}}async createAndStart(){let n=this.config();n&&await this.operate(async()=>{let e=await this.client.validate(n);if(!e.valid)throw new Error(e.errors.join(`; `));let i=await this.client.create(n);this.campaigns.update(o=>[i,...o]),this.select(i.id);let a=await this.client.baselineCheck(i.id);if(this.summary.set(await this.client.summary(i.id)),!a.withinLimit)throw new Error(`Baseline emits ${a.parameters} parameters; limit is ${a.maxParameters}. Prune the search space while preserving every seed before starting this draft.`);await this.client.action(i.id,`start`),this.active.set(i.id)})}async probeTarget(){let n=this.config();n&&await this.operate(async()=>{this.targetStatus.set(await this.client.probeTarget(n))})}async action(n){await this.operate(async()=>{await this.client.action(this.selectedId(),n),(n===`start`||n===`resume`)&&this.active.set(this.selectedId())})}async operate(n){this.operations++,this.busy.set(!0),this.error.set(null);try{await n()}catch(e){this.error.set(this.message(e))}finally{this.operations--,this.busy.set(this.operations>0)}}artifactUrl(n){return this.client.artifactUrl(this.selectedId(),n)}message(n){let e=n;return e.error?.message??e.message??`AutoLLM request failed`}static ɵfac=function(e){return new(e||t)};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})};var JF=()=>[`DRAFT`,`INTERRUPTED`,`CANCELLED`,`NEEDS_ATTENTION`];var e2=()=>[`AWAITING_APPROVAL`,`APPROVED`];var t2=()=>[`instances.csv`,`proposed-retained.index`,`proposed-train.index`,`proposed-test.index`,`manifest.json`];var n2=()=>[`INTERRUPTED`,`CANCELLED`,`NEEDS_ATTENTION`];var i2=()=>[`GENERATION`,`REPAIR`];var oE=(t,n)=>n.id;var a2=(t,n)=>n.instance;var rE=(t,n)=>n.number;var o2=(t,n)=>n.path;var r2=(t,n)=>n.threadId;function s2(t,n){if(t&1&&(l(0,`p`,1),u(1),d()),t&2){let e=y();h(),w(e.store.error())}}function c2(t,n){if(t&1&&(l(0,`p`,1),u(1),d()),t&2){let e=y();h(),w(e.store.connectionError())}}function l2(t,n){if(t&1){let e=X();l(0,`label`),u(1,`SSH host or alias`),l(2,`input`,2),C(`change`,function(a){A(e);return O(y(2).update(`execution.ssh.host`,a))}),d()(),l(3,`label`),u(4,`Remote workspace`),l(5,`input`,2),C(`change`,function(a){A(e);return O(y(2).update(`execution.ssh.workspace`,a))}),d()(),l(6,`label`),u(7,`Remote Java executable`),l(8,`input`,2),C(`change`,function(a){A(e);return O(y(2).update(`execution.ssh.javaExecutable`,a))}),d()(),l(9,`label`),u(10,`Local remote-runner JAR`),l(11,`input`,2),C(`change`,function(a){A(e);return O(y(2).update(`execution.ssh.runnerArtifact`,a))}),d()()}if(t&2){let e=y();h(2),S(`value`,e.execution.ssh?.host??``),h(3),S(`value`,e.execution.ssh?.workspace??``),h(3),S(`value`,e.execution.ssh?.javaExecutable??`java`),h(3),S(`value`,e.execution.ssh?.runnerArtifact??``)}}function d2(t,n){if(t&1){let e=X();l(0,`label`),u(1,`Worker count`),l(2,`input`,11),C(`change`,function(a){A(e);return O(y(2).update(`execution.workers`,a,!0))}),d()()}if(t&2){let e=y();h(2),S(`value`,e.execution.workers)}}function u2(t,n){if(t&1&&(l(0,`details`,20)(1,`summary`),u(2,`Resolved budgets and workload estimate`),d(),l(3,`pre`),u(4),U(5,`json`),d()()),t&2){let e=y(2);h(4),w(_e(5,1,e.store.summary()))}}function m2(t,n){if(t&1&&(l(0,`pre`),u(1),U(2,`json`),d()),t&2){let e=y(2);h(),w(_e(2,1,e.store.targetStatus()))}}function p2(t,n){if(t&1){let e=X();l(0,`mat-card`)(1,`mat-card-content`)(2,`details`,5)(3,`summary`),u(4,`Configure a campaign`),d(),l(5,`fieldset`,6)(6,`div`,7)(7,`label`),u(8,`Where should the actual workloads run?`),l(9,`select`,2),C(`change`,function(a){A(e);return O(y().executionTarget(a))}),l(10,`option`,8),u(11,`Select execution location`),d(),l(12,`option`,9),u(13,`Locally on this computer`),d(),l(14,`option`,10),u(15,`Configure a remote server over SSH`),d()()(),D(16,l2,12,4),l(17,`label`),u(18,`Baseline repository`),l(19,`input`,2),C(`change`,function(a){A(e);return O(y().update(`project.repository`,a))}),d()(),l(20,`label`),u(21,`Git ref`),l(22,`input`,2),C(`change`,function(a){A(e);return O(y().update(`project.ref`,a))}),d()(),l(23,`label`),u(24,`Build command (JSON arguments)`),l(25,`input`,2),U(26,`json`),C(`change`,function(a){A(e);return O(y().buildCommand(a))}),d()(),l(27,`label`),u(28,`Built JAR path`),l(29,`input`,2),C(`change`,function(a){A(e);return O(y().update(`project.artifact`,a))}),d()(),l(30,`label`),u(31,`SOTA component JSON`),l(32,`input`,2),C(`change`,function(a){A(e);return O(y().update(`sotaFile`,a))}),d()(),l(33,`label`),u(34,`Additional seeds (optional)`),l(35,`input`,2),C(`change`,function(a){A(e);return O(y().update(`initialConfigurationsFile`,a))}),d()(),l(36,`label`),u(37,`Training instances`),l(38,`input`,2),C(`change`,function(a){A(e);return O(y().update(`trainingPath`,a))}),d()(),l(39,`label`),u(40,`Comparison instances`),l(41,`input`,2),C(`change`,function(a){A(e);return O(y().update(`comparisonPath`,a))}),d()(),l(42,`label`),u(43,`Independent trials`),l(44,`input`,11),C(`change`,function(a){A(e);return O(y().update(`trials`,a,!0))}),d()(),l(45,`label`),u(46,`Campaign seed`),l(47,`input`,12),C(`change`,function(a){A(e);return O(y().update(`seed`,a,!0))}),d()(),l(48,`label`),u(49,`Parameter limit`),l(50,`input`,13),C(`change`,function(a){A(e);return O(y().update(`maxParameters`,a,!0))}),d()(),l(51,`label`),u(52,`Workers`),l(53,`select`,2),C(`change`,function(a){A(e);return O(y().workerMode(a))}),l(54,`option`,14),u(55,`Auto: half the available processors on the execution host`),d(),l(56,`option`,15),u(57,`Set worker count`),d()()(),D(58,d2,3,1,`label`),l(59,`label`),u(60,`Main evaluations`),l(61,`input`,11),C(`change`,function(a){A(e);return O(y().update(`tuning.evaluations`,a,!0))}),d()(),l(62,`label`),u(63,`Comparison repetitions`),l(64,`input`,11),C(`change`,function(a){A(e);return O(y().update(`comparison.repetitions`,a,!0))}),d()(),l(65,`label`),u(66,`Pilot duration (milliseconds)`),l(67,`input`,11),C(`change`,function(a){A(e);return O(y().update(`preparation.pilotDurationMillis`,a,!0))}),d()(),l(68,`label`),u(69,`Repair turns`),l(70,`input`,16),C(`change`,function(a){A(e);return O(y().update(`preparation.maxRepairs`,a,!0))}),d()(),l(71,`label`),u(72,`Codex model (optional)`),l(73,`input`,2),C(`change`,function(a){A(e);return O(y().update(`codex.model`,a))}),d()(),l(74,`label`),u(75,`Reasoning effort (optional)`),l(76,`input`,2),C(`change`,function(a){A(e);return O(y().update(`codex.reasoningEffort`,a))}),d()(),l(77,`label`),u(78,`Custom prompt file (optional)`),l(79,`input`,2),C(`change`,function(a){A(e);return O(y().update(`codex.promptFile`,a))}),d()()(),l(80,`label`),u(81,`Prompt override (optional)`),l(82,`textarea`,17),C(`change`,function(a){A(e);return O(y().update(`codex.prompt`,a))}),d()(),l(83,`details`)(84,`summary`),u(85,`All settings, including time budgets and timeouts`),d(),l(86,`textarea`,18),U(87,`json`),C(`change`,function(a){A(e);return O(y().advanced(a))}),d()()(),l(88,`p`),u(89,`Each trial starts from the same baseline. Both comparison algorithms restart within their time budget. Ablation is disabled.`),d(),l(90,`p`,19),u(91),U(92,`number`),d(),D(93,u2,6,3,`details`,20),l(94,`details`)(95,`summary`),u(96,`Prepare a screened dataset`),d(),l(97,`div`,7)(98,`label`),u(99,`Full original input pool`),l(100,`input`,21),C(`input`,function(a){A(e);return O(y().originalPool.set(a.target.value))}),d()(),l(101,`label`),u(102,`Training fraction`),l(103,`input`,22),C(`input`,function(a){A(e);return O(y().trainingFraction.set(+a.target.value))}),d()()(),l(104,`p`),u(105,`Screening uses one sequential worker. Review the completed proposal before publishing training/testing indexes. Testing includes all retained instances, including training.`),d(),l(106,`button`,23),C(`click`,function(){A(e);let a=y();return O(a.store.createScreening(a.originalPool(),a.trainingFraction()))}),u(107,`Prepare dataset`),d()(),l(108,`button`,23),C(`click`,function(){A(e);return O(y().store.probeTarget())}),u(109,`Check execution host`),d(),D(110,m2,3,3,`pre`),l(111,`button`,24),C(`click`,function(){A(e);return O(y().store.createAndStart())}),u(112,`Create and start campaign`),d()()()()}if(t&2){let e=n,i=y();h(2),S(`open`,!i.store.campaign()),h(3),S(`disabled`,!!i.store.active()||i.store.busy()),h(4),S(`value`,i.executionChoice()),h(7),k(e.execution.target===`SSH`?16:-1),h(3),S(`value`,e.project.repository),h(3),S(`value`,e.project.ref),h(3),S(`value`,_e(26,40,e.project.buildCommand)),h(4),S(`value`,e.project.artifact),h(3),S(`value`,e.sotaFile??``),h(3),S(`value`,e.initialConfigurationsFile??``),h(3),S(`value`,e.trainingPath??``),h(3),S(`value`,e.comparisonPath??``),h(3),S(`value`,e.trials),h(3),S(`value`,e.seed),h(3),S(`value`,e.maxParameters),h(3),S(`value`,e.execution.workers<1?`AUTO`:`CUSTOM`),h(5),k(e.execution.workers>0?58:-1),h(3),S(`value`,e.tuning.evaluations),h(3),S(`value`,e.comparison.repetitions),h(3),S(`value`,e.preparation.pilotDurationMillis),h(3),S(`value`,e.preparation.maxRepairs),h(3),S(`value`,e.codex.model??``),h(3),S(`value`,e.codex.reasoningEffort??``),h(3),S(`value`,e.codex.promptFile??``),h(3),S(`value`,e.codex.prompt??``),h(4),S(`value`,_e(87,42,e)),h(5),Xd(``,e.trials,` trials · `,_e(92,44,e.tuning.evaluations),` tuning evaluations per trial at `,(e.tuning.ignoreInitialMillis+e.tuning.intervalDurationMillis)/1e3,` seconds · `,e.comparison.repetitions,` paired comparison repetitions at `,e.comparison.timeLimitMillis>0?e.comparison.timeLimitMillis/1e3+` seconds`:`application time limits (resolved during baseline check)`,` · `,e.preparation.pilotDurationMillis/6e4,` minute pilot · `,e.preparation.maxRepairs,` component repairs per trial.`),h(2),k(i.store.summary()?93:-1),h(7),S(`value`,i.originalPool()),h(3),S(`value`,i.trainingFraction()),h(3),S(`disabled`,!i.executionChoice()||!i.originalPool()||!!i.store.active()||!!i.store.activeScreening()||i.store.busy()),h(2),S(`disabled`,!i.executionChoice()||!!i.store.active()||i.store.busy()),h(2),k(i.store.targetStatus()?110:-1),h(),S(`disabled`,!i.executionChoice()||!!i.store.active()||!!i.store.activeScreening()||i.store.busy())}}function h2(t,n){if(t&1&&(l(0,`option`,4),u(1),U(2,`date`),d()),t&2){let e=n.$implicit;S(`value`,e.id),h(),jt(``,Ie(2,4,e.createdAt,`short`),` · `,e.status,` · `,e.id)}}function f2(t,n){if(t&1&&z(0,`mat-progress-bar`,28),t&2){let e=y();S(`value`,100*e.initialCompleted/e.initialTotal)}}function g2(t,n){if(t&1&&z(0,`mat-progress-bar`,28),t&2){let e=y();S(`value`,100*e.confirmationsCompleted/e.confirmationsTotal)}}function b2(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`date`),d(),D(3,f2,1,1,`mat-progress-bar`,28),D(4,g2,1,1,`mat-progress-bar`,28)),t&2){let e=n;h(),oc(`Initial measurements: `,e.initialCompleted,`/`,e.initialTotal,` · Confirmations: `,e.confirmationsCompleted,`/`,e.confirmationsTotal,` · Last activity: `,Ie(2,7,e.updatedAt,`mediumTime`)),h(2),k(e.phase===`INITIAL`&&e.initialTotal?3:-1),h(),k(e.phase===`CONFIRMATIONS`&&e.confirmationsTotal?4:-1)}}function v2(t,n){if(t&1&&(l(0,`p`,29),u(1),d(),l(2,`p`),u(3),d()),t&2){let e=n;h(),Fe(``,e.category,` · `,e.message),h(2),w(e.recovery)}}function y2(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`date`),d()),t&2){let e=n;h(),jt(`Remote job `,e.jobId,` · `,e.status,` · Last sync: `,Ie(2,3,e.lastSynchronizedAt,`mediumTime`))}}function _2(t,n){if(t&1){let e=X();l(0,`button`,23),C(`click`,function(){A(e);return O(y(3).store.screeningAction(`resume`))}),u(1,`Resume preparation`),d()}if(t&2){let e=y(3);S(`disabled`,!!e.store.active()||!!e.store.activeScreening()||e.store.busy())}}function x2(t,n){if(t&1){let e=X();l(0,`button`,23),C(`click`,function(){A(e);return O(y(3).store.screeningAction(`cancel`))}),u(1,`Cancel preparation`),d()}if(t&2)S(`disabled`,y(3).store.busy())}function C2(t,n){if(t&1){let e=X();l(0,`button`,23),C(`click`,function(){A(e);return O(y(3).store.reviewScreening())}),u(1,`Review proposal`),d()}if(t&2)S(`disabled`,y(3).store.busy())}function S2(t,n){if(t&1){let e=X();l(0,`button`,30),C(`click`,function(){A(e);return O(y(3).store.useSelection())}),u(1,`Use approved selection`),d()}}function w2(t,n){if(t&1&&(l(0,`p`),u(1),d(),D(2,b2,5,10),D(3,v2,4,3),D(4,y2,3,6,`p`),l(5,`div`,25),D(6,_2,2,1,`button`,26),D(7,x2,2,1,`button`,26),D(8,C2,2,1,`button`,26),D(9,S2,2,0,`button`,27),d()),t&2){let e,i,a,o=n,r=y(2);h(),Fe(``,o.status,` · `,o.progress?.phase||o.activity?.phase||`Waiting to start`),h(),k((e=o.progress)?2:-1,e),h(),k((i=o.problem)?3:-1,i),h(),k((a=o.activity?.remote)?4:-1,a),h(2),k(vt(9,JF).includes(o.status)?6:-1),h(),k(r.store.activeScreening()===o.id||o.status===`NEEDS_ATTENTION`?7:-1),h(),k(vt(10,e2).includes(o.status)?8:-1),h(),k(o.status===`APPROVED`?9:-1)}}function E2(t,n){if(t&1&&(l(0,`a`,32),u(1),d()),t&2){let e=n.$implicit,i=y();S(`href`,y(2).store.screeningArtifactUrl(i.artifactPrefix+`/`+e),pn),h(),w(e)}}function D2(t,n){if(t&1&&(l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),d(),l(7,`td`),u(8),d()()),t&2){let e=n.$implicit;h(2),w(e.instance),h(2),w(e.classification),h(2),Fe(``,e.fastHits,`/`,e.confirmations),h(2),w(e.reason)}}function k2(t,n){if(t&1){let e=X();l(0,`button`,24),C(`click`,function(){A(e);return O(y(3).store.screeningAction(`approve`))}),u(1,`Approve this reviewed selection`),d()}if(t&2){let e=y(),i=y(2);S(`disabled`,i.store.busy()||!!i.store.active()||!!i.store.activeScreening()||!!e.proposal.splitFailure)}}function I2(t,n){if(t&1&&(l(0,`pre`,31),u(1),d(),l(2,`p`),u(3),d(),l(4,`div`,25),be(5,E2,2,2,`a`,32,Pn),d(),l(7,`details`)(8,`summary`),u(9,`All instance decisions`),d(),l(10,`table`)(11,`thead`)(12,`tr`)(13,`th`),u(14,`Instance`),d(),l(15,`th`),u(16,`Decision`),d(),l(17,`th`),u(18,`Fast confirmations`),d(),l(19,`th`),u(20,`Reason`),d()()(),l(21,`tbody`),be(22,D2,9,5,`tr`,null,a2),d()()(),D(24,k2,2,1,`button`,33)),t&2){let e=n,i=y(2);h(),w(e.report),h(2),H(`Reviewed proposal SHA-256: `,e.proposalSha256),h(2),ve(vt(3,t2)),h(17),ve(e.proposal.instances),h(2),k(i.store.screening()?.status===`AWAITING_APPROVAL`?24:-1)}}function M2(t,n){if(t&1){let e=X();l(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),u(3,`Dataset preparation`),d(),l(4,`label`),u(5,`Saved screening`),l(6,`select`,2),C(`change`,function(a){A(e);return O(y().store.selectScreening(a.target.value))}),be(7,h2,3,7,`option`,4,oE),d()(),D(9,w2,10,11),D(10,I2,25,4),d()()}if(t&2){let e,i,a=y();h(6),S(`value`,a.store.selectedScreening()),h(),ve(a.store.screenings()),h(2),k((e=a.store.screening())?9:-1,e),h(),k((i=a.store.review())?10:-1,i)}}function N2(t,n){if(t&1&&(l(0,`option`,4),u(1),U(2,`date`),d()),t&2){let e=n.$implicit;S(`value`,e.id),h(),jt(``,Ie(2,4,e.createdAt,`short`),` — `,e.status,` — `,e.id)}}function T2(t,n){if(t&1){let e=X();l(0,`button`,23),C(`click`,function(){A(e);return O(y(2).store.action(`cancel`))}),u(1,`Cancel campaign`),d()}if(t&2)S(`disabled`,y().remote?.status===`CANCELLATION_PENDING`)}function A2(t,n){if(t&1){let e=X();l(0,`button`,24),C(`click`,function(){A(e);return O(y(2).store.action(`resume`))}),u(1,`Resume from checkpoints`),d()}if(t&2){let e=y(2);S(`disabled`,e.store.busy()||!!e.store.activeScreening())}}function O2(t,n){if(t&1){let e=X();l(0,`button`,23),C(`click`,function(){A(e);return O(y(2).store.action(`cancel`))}),u(1,`Reconcile and cancel remote work`),d()}if(t&2)S(`disabled`,y(2).store.busy())}function R2(t,n){if(t&1){let e=X();l(0,`button`,37),C(`click`,function(){A(e);return O(y(2).store.action(`start`))}),u(1,`Start saved draft`),d()}}function P2(t,n){if(t&1){let e=X();l(0,`button`,23),C(`click`,function(){A(e);return O(y(2).store.action(`report`))}),u(1,`Regenerate reports`),d()}if(t&2)S(`disabled`,y(2).store.busy())}function F2(t,n){if(t&1&&(l(0,`p`),u(1),d()),t&2){let e=n;h(),jt(`Execution workers: `,e.effectiveWorkers,` · Requested: `,e.requestedWorkers===-1?`Auto`:e.requestedWorkers,` · Host: `,e.identity.hostname)}}function L2(t,n){if(t&1&&(l(0,`p`,29),u(1),d()),t&2){let e=y();h(),w(e.failure)}}function V2(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`date`),d(),D(3,L2,2,1,`p`,29)),t&2){let e=n;h(),ai(`Latest remote workload: `,e.host,` · `,e.stageDirectory,` · `,e.status,` · Last sync: `,e.lastSynchronizedAt?Ie(2,5,e.lastSynchronizedAt,`short`):`Not synchronized`),h(2),k(e.failure?3:-1)}}function B2(t,n){t&1&&u(0,` Known totals may be incomplete. `)}function j2(t,n){if(t&1&&(l(0,`p`,35),u(1),U(2,`number`),U(3,`number`),U(4,`number`),d(),l(5,`p`),u(6),D(7,B2,1,0),d()),t&2){let e=n;h(),ai(`Codex token usage · `,e.status,` · Input: `,e.tokens.inputTokens===null?`Unknown`:_e(2,9,e.tokens.inputTokens),` · Output: `,e.tokens.outputTokens===null?`Unknown`:_e(3,11,e.tokens.outputTokens),` · Total: `,e.tokens.totalTokens===null?`Unknown`:_e(4,13,e.tokens.totalTokens)),h(5),ai(``,e.completeAttempts,`/`,e.attempts,` attempts have complete usage; `,e.partialAttempts,` partial, `,e.unavailableAttempts,` unavailable. Includes repairs and failed or interrupted attempts. `),h(),k(e.status===`PARTIAL`?7:-1)}}function z2(t,n){t&1&&(l(0,`p`,35),u(1,`Codex token usage · UNAVAILABLE`),d())}function H2(t,n){if(t&1&&(l(0,`pre`,29),u(1),d()),t&2){let e=y();h(),w(e.failure)}}function U2(t,n){if(t&1&&(l(0,`p`,29),u(1),d(),l(2,`p`),u(3),d()),t&2){let e=n;h(),jt(``,e.category,` · `,e.code,` · `,e.message),h(2),w(e.recovery)}}function $2(t,n){if(t&1&&(l(0,`details`)(1,`summary`),u(2,`Report export failed`),d(),l(3,`pre`,29),u(4),d()()),t&2){let e=y();h(4),w(e.reportFailure)}}function G2(t,n){if(t&1){let e=X();l(0,`tr`)(1,`td`),u(2),d(),l(3,`td`),u(4),d(),l(5,`td`),u(6),d(),l(7,`td`),u(8),d(),l(9,`td`)(10,`button`,30),C(`click`,function(){let a=A(e).$implicit;return O(y(2).store.selectTrial(a.number))}),u(11,`Open trial`),d()()()}if(t&2){let e=n.$implicit;h(2),w(e.number),h(2),w(e.status),h(2),w(e.tuningSeed),h(2),w(e.repairTurns)}}function W2(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`number`),d()),t&2){let e=n,i=y();h(),ai(``,e.substage||e.status,` · `,Ie(2,4,(e.elapsedMillis||0)/1e3,`1.0-0`),` seconds · Infrastructure retries: `,e.infrastructureRetries||0,` · Component repairs: `,i.repairTurns)}}function q2(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`json`),d()),t&2){let e=y();h(),H(`Reported usage: `,_e(2,1,e.usage))}}function Y2(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`date`),d(),l(3,`p`),u(4),d(),D(5,q2,3,3,`p`),l(6,`p`),u(7,`These are observed CLI events. Component repair turns are counted separately; model iteration totals are unavailable.`),d()),t&2){let e=n;h(),jt(`Generation: `,e.state,` · Session: `,e.threadId||`Waiting for session event`,` · Last activity: `,e.lastActivityAt?Ie(2,9,e.lastActivityAt,`mediumTime`):`No events received`),h(3),oc(`Turns started: `,e.turns,` · Commands: `,e.commandsCompleted,` completed / `,e.commandsStarted,` started · Edit events: `,e.edits,` · Assistant messages: `,e.messages),h(),k(e.usage?5:-1)}}function K2(t,n){if(t&1&&(l(0,`article`)(1,`small`),u(2),U(3,`date`),d(),l(4,`pre`),u(5),d()()),t&2){let e=n.$implicit;h(2),Fe(``,Ie(3,3,e.timestamp,`mediumTime`),` · `,e.type),h(3),w(e.text)}}function Q2(t,n){t&1&&(l(0,`p`),u(1,`Waiting for conversation events. Raw stdout and stderr remain available below.`),d())}function X2(t,n){if(t&1){let e=X();l(0,`details`,38)(1,`summary`),u(2),d(),l(3,`div`,40),be(4,K2,6,6,`article`,null,ic,!1,Q2,2,0,`p`),d(),l(7,`button`,30),C(`click`,function(){let a=A(e);return O(y(3).store.selectLog(a.directory+`/stdout.log`))}),u(8,`Raw stdout`),d(),l(9,`button`,30),C(`click`,function(){let a=A(e);return O(y(3).store.selectLog(a.directory+`/stderr.log`))}),u(10,`Raw stderr`),d()()}if(t&2){let e=n,i=y(3);h(2),jt(`Conversation · `,e.stage,` attempt `,e.number,` · `,e.status),h(2),ve(i.store.activity())}}function Z2(t,n){if(t&1&&(l(0,`p`),u(1),U(2,`number`),U(3,`number`),d()),t&2){let e=n;h(),jt(`Trial token usage · `,e.status,` · Input: `,e.tokens.inputTokens===null?`Unknown`:_e(2,3,e.tokens.inputTokens),` · Output: `,e.tokens.outputTokens===null?`Unknown`:_e(3,5,e.tokens.outputTokens))}}function J2(t,n){if(t&1&&(l(0,`p`),u(1),d(),z(2,`mat-progress-bar`,28)),t&2){let e=n;h(),Qd(``,e.phase,` · `,e.budget.used,`/`,e.budget.maximum,` evaluations · `,e.evaluations.succeeded,` succeeded · `,e.evaluations.rejected,` rejected · `,e.evaluations.failed,` failed`),h(),S(`value`,100*e.budget.used/e.budget.maximum)}}function eL(t,n){if(t&1&&(l(0,`pre`,29),u(1),d()),t&2){let e=y();h(),w(e.failure)}}function tL(t,n){if(t&1){let e=X();l(0,`button`,30),C(`click`,function(){A(e);let a=y().$implicit;return O(y(3).store.watchGeneration(a.number))}),u(1,`View conversation`),d()}}function nL(t,n){if(t&1&&(l(0,`a`,32),u(1),d()),t&2){let e=n.$implicit,i=y(2).$implicit;S(`href`,y(3).store.artifactUrl(i.directory+`/`+e.path),pn),h(),H(`Download conversation `,e.threadId)}}function iL(t,n){if(t&1&&(l(0,`span`),u(1),d(),be(2,nL,2,2,`a`,32,r2)),t&2){let e=n;h(),Fe(`Conversation: `,e.status,` · Usage: `,e.usage.status),h(),ve(e.conversations)}}function aL(t,n){if(t&1&&(l(0,`details`)(1,`summary`),u(2,`Failure details`),d(),l(3,`pre`),u(4),d()()),t&2){let e=y().$implicit;h(4),w(e.failure)}}function oL(t,n){if(t&1){let e=X();l(0,`div`,39)(1,`span`),u(2),U(3,`date`),d(),l(4,`button`,30),C(`click`,function(){let a=A(e).$implicit;return O(y(3).store.selectLog(a.directory+([`PREPARATION`].includes(a.stage)?`/build`:``)+`/stdout.log`))}),u(5,`View log`),d(),D(6,tL,2,0,`button`,27),D(7,iL,4,2),D(8,aL,5,1,`details`),d()}if(t&2){let e,i=n.$implicit;h(2),jt(``,i.stage,` · `,i.status,` · `,Ie(3,6,i.startedAt,`short`)),h(4),k(vt(9,i2).includes(i.stage)?6:-1),h(),k((e=i.codexEvidence)?7:-1,e),h(),k(i.failure?8:-1)}}function rL(t,n){if(t&1&&(l(0,`h3`),u(1,`First-ranked proposal: components and parameters`),d(),l(2,`p`),u(3,`Generated algorithm components end in New.`),d(),l(4,`pre`),u(5),U(6,`json`),d()),t&2){let e=y();h(5),w(_e(6,1,e.winner))}}function sL(t,n){if(t&1&&(l(0,`details`,20)(1,`summary`),u(2,`Comparison results`),d(),l(3,`pre`),u(4),U(5,`json`),d()()),t&2){let e=y(3);h(4),w(_e(5,1,e.comparisonSummary()))}}function cL(t,n){if(t&1&&(l(0,`option`,4),u(1),d()),t&2){let e=n.$implicit;S(`value`,e),h(),w(e)}}function lL(t,n){if(t&1&&(l(0,`option`,4),u(1),d()),t&2){let e=n.$implicit;S(`value`,e),h(),w(e)}}function dL(t,n){if(t&1){let e=X();l(0,`div`,7)(1,`label`),u(2,`Convergence instance`),l(3,`select`,2),C(`change`,function(a){A(e);return O(y(3).selectedInstance.set(a.target.value))}),be(4,cL,2,2,`option`,4,Pn),d()(),l(6,`label`),u(7,`Objective`),l(8,`select`,2),C(`change`,function(a){A(e);return O(y(3).selectedObjective.set(a.target.value))}),be(9,lL,2,2,`option`,4,Pn),d()()(),z(11,`app-dashboard-chart`,41)}if(t&2){let e=y(3);h(3),S(`value`,e.selectedInstance()||e.instances()[0]),h(),ve(e.instances()),h(4),S(`value`,e.selectedObjective()||e.objectives()[0]),h(),ve(e.objectives()),h(2),S(`model`,e.curveChart())}}function uL(t,n){if(t&1&&(l(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),u(3),d(),D(4,W2,3,7,`p`),D(5,Y2,8,12),D(6,X2,11,4,`details`,38),D(7,Z2,4,7,`p`),D(8,J2,3,7),D(9,eL,2,1,`pre`,29),be(10,oL,9,10,`div`,39,rE),D(12,rL,7,3),D(13,sL,6,3,`details`,20),D(14,dL,12,3),d()()),t&2){let e,i,a,o,r,s=n,c=y(2);h(3),Fe(`Trial `,s.number,` · `,s.status),h(),k((e=c.store.progress())?4:-1,e),h(),k((i=c.store.generation())?5:-1,i),h(),k((a=c.store.conversationAttempt())?6:-1,a),h(),k((o=s.codexUsage)?7:-1,o),h(),k((r=c.store.tuning())?8:-1,r),h(),k(s.failure?9:-1),h(),ve(s.attempts),h(2),k(s.winner?12:-1),h(),k(c.store.comparison()?13:-1),h(),k(c.instances().length?14:-1)}}function mL(t,n){if(t&1&&(l(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),u(3),d(),l(4,`pre`,42),u(5),d()()()),t&2){let e=y(2);h(3),w(e.store.logPath()),h(2),w(e.store.logText())}}function pL(t,n){if(t&1){let e=X();l(0,`button`,30),C(`click`,function(){A(e);let a=y().$implicit;return O(y(2).store.selectLog(a.path))}),u(1,`View log`),d()}}function hL(t,n){if(t&1&&(l(0,`div`,36)(1,`a`,32),u(2),d(),l(3,`span`),u(4),d(),D(5,pL,2,0,`button`,27),d()),t&2){let e=n.$implicit,i=y(2);h(),S(`href`,i.store.artifactUrl(e.path),pn),h(),w(e.path),h(2),H(``,e.size,` bytes`),h(),k(e.path.endsWith(`.log`)||e.path.endsWith(`.jsonl`)?5:-1)}}function fL(t,n){if(t&1){let e=X();l(0,`mat-card`)(1,`mat-card-content`)(2,`div`,25)(3,`strong`),u(4),d(),D(5,T2,2,1,`button`,26),D(6,A2,2,1,`button`,33),D(7,O2,2,1,`button`,26),D(8,R2,2,0,`button`,34),D(9,P2,2,1,`button`,26),d(),z(10,`mat-progress-bar`,28),D(11,F2,2,3,`p`),D(12,V2,4,8),D(13,j2,8,15)(14,z2,2,0,`p`,35),D(15,H2,2,1,`pre`,29),D(16,U2,4,4),D(17,$2,5,1,`details`),l(18,`table`)(19,`thead`)(20,`tr`)(21,`th`),u(22,`Trial`),d(),l(23,`th`),u(24,`Stage / outcome`),d(),l(25,`th`),u(26,`Tuning seed`),d(),l(27,`th`),u(28,`Repairs`),d(),l(29,`th`),u(30,`Inspect`),d()()(),l(31,`tbody`),be(32,G2,12,4,`tr`,null,rE),d()()()(),D(34,uL,15,11,`mat-card`),D(35,mL,6,2,`mat-card`),l(36,`mat-card`)(37,`mat-card-content`)(38,`div`,25)(39,`h2`),u(40,`Saved data and artifacts`),d(),l(41,`button`,30),C(`click`,function(){A(e);return O(y().store.refreshArtifacts())}),u(42,`Refresh artifacts`),d()(),be(43,hL,6,4,`div`,36,o2),l(45,`details`)(46,`summary`),u(47,`Frozen campaign settings and versions`),d(),l(48,`pre`),u(49),U(50,`json`),d(),l(51,`pre`),u(52),U(53,`json`),d()()()()}if(t&2){let e,i,a,o,r,s=n,c=y();h(4),jt(``,s.status,` · `,c.store.completed(),`/`,s.trials.length,` trials finished`),h(),k(c.store.active()===s.id?5:-1),h(),k(vt(23,n2).includes(s.status)&&!c.store.active()?6:-1),h(),k(s.status===`NEEDS_ATTENTION`?7:-1),h(),k(s.status===`DRAFT`&&!c.store.active()?8:-1),h(),k(s.status!==`DRAFT`&&!c.store.active()?9:-1),h(),S(`value`,100*c.store.completed()/s.trials.length),h(),k((e=s.executionHost)?11:-1,e),h(),k((i=s.remote)?12:-1,i),h(),k((a=s.codexUsage)?13:14,a),h(2),k(s.failure?15:-1),h(),k((o=s.problem)?16:-1,o),h(),k(s.reportFailure?17:-1),h(15),ve(s.trials),h(2),k((r=c.store.trial())?34:-1,r),h(),k(c.store.logPath()?35:-1),h(8),ve(c.store.artifacts()),h(6),w(_e(50,19,s.config)),h(3),w(_e(53,21,s.versions))}}var Vm=class t$1{store=f(Lm);selectedInstance=N(``);selectedObjective=N(``);executionChoice=N(``);originalPool=N(``);trainingFraction=N(.15);curves=ne(()=>this.store.comparison()?.curves??[]);instances=ne(()=>[...new Set(this.curves().map(n=>n.instance))]);objectives=ne(()=>[...new Set(this.curves().flatMap(n=>Object.keys(n.curves)))]);comparisonSummary=ne(()=>{let i=this.store.comparison()??{},{curves:n}=i;return t(i,[`curves`])});curveChart=ne(()=>{let n=this.instances().includes(this.selectedInstance())?this.selectedInstance():this.instances()[0],e=this.objectives().includes(this.selectedObjective())?this.selectedObjective():this.objectives()[0];return{options:{chart:{type:`line`,height:360},title:{text:e??`Comparison convergence`},xAxis:{title:{text:`Time (milliseconds)`}},yAxis:{title:{text:`Objective value`}},series:this.curves().filter(i=>i.instance===n).map(i=>({type:`line`,name:`${i.algorithm} \xB7 repetition ${Number(i.iteration)+1}`,data:(i.curves[e]??[]).filter(a=>Number.isFinite(a.value)).map(a=>[a.instant/1e6,a.value])}))}}});constructor(){this.store.start()}executionTarget(n){let e=n.target.value;this.executionChoice.set(e),this.store.config.update(i=>i&&s(r({},i),{execution:s(r({},i.execution),{target:e,ssh:i.execution.ssh??{host:``,workspace:``,javaExecutable:`java`,runnerArtifact:null,environment:{}}})})),this.store.targetStatus.set(null)}workerMode(n){let e=n.target.value===`AUTO`;this.store.config.update(i=>i&&s(r({},i),{execution:s(r({},i.execution),{workers:e?-1:1})}))}update(n,e,i=!1){let a=this.store.config();if(!a)return;let o=structuredClone(a),r=o,s=n.split(`.`);for(let p of s.slice(0,-1))(!r[p]||typeof r[p]!=`object`)&&(r[p]={}),r=r[p];let c=e.target.value;r[s[s.length-1]]=i?Number(c):c||null,this.store.config.set(o)}buildCommand(n){try{let e=JSON.parse(n.target.value);if(!Array.isArray(e)||e.some(i=>typeof i!=`string`))throw new Error(`Build command must be a JSON array of arguments`);this.store.config.update(i=>i&&s(r({},i),{project:s(r({},i.project),{buildCommand:e})}))}catch(e){this.store.error.set(e.message)}}advanced(n){try{let e=this.store.config();if(!e)return;let i=JSON.parse(n.target.value);if(!i||typeof i!=`object`||Array.isArray(i))throw new Error(`Settings must be a JSON object`);for(let o of[`project`,`codex`,`preparation`,`tuning`,`comparison`,`execution`])if(i[o]!=null&&(typeof i[o]!=`object`||Array.isArray(i[o])))throw new Error(`${o} must be a JSON object`);let a=i.execution?.ssh;if(a!=null&&(typeof a!=`object`||Array.isArray(a)))throw new Error(`execution.ssh must be a JSON object`);this.store.config.set(s(r(r({},e),i),{project:r(r({},e.project),i.project),codex:r(r({},e.codex),i.codex),preparation:r(r({},e.preparation),i.preparation),tuning:r(r({},e.tuning),i.tuning),comparison:r(r({},e.comparison),i.comparison),execution:s(r(r({},e.execution),i.execution),{ssh:a===null?null:e.execution.ssh||a?s(r(r({host:``,workspace:``,javaExecutable:`java`,runnerArtifact:null},e.execution.ssh),a),{environment:r(r({},e.execution.ssh?.environment),a?.environment)}):null})})),i.execution?.target&&this.executionChoice.set(i.execution.target),this.store.error.set(null)}catch(e){this.store.error.set(e.message)}}static ɵfac=function(e){return new(e||t$1)};static ɵcmp=B({type:t$1,selectors:[[`app-autollm-dashboard`]],decls:20,vars:6,consts:[[`src`,`favicon.svg`,`alt`,`Mork`],[`role`,`alert`,1,`error`],[3,`change`,`value`],[`value`,``],[3,`value`],[3,`open`],[3,`disabled`],[1,`fields`],[`value`,``,`disabled`,``],[`value`,`LOCAL`],[`value`,`SSH`],[`type`,`number`,`min`,`1`,3,`change`,`value`],[`type`,`number`,3,`change`,`value`],[`type`,`number`,`min`,`1`,`max`,`299`,3,`change`,`value`],[`value`,`AUTO`],[`value`,`CUSTOM`],[`type`,`number`,`min`,`0`,3,`change`,`value`],[`rows`,`6`,3,`change`,`value`],[`rows`,`16`,3,`change`,`value`],[1,`budget-summary`],[`open`,``],[3,`input`,`value`],[`type`,`number`,`min`,`0.01`,`max`,`0.99`,`step`,`0.01`,3,`input`,`value`],[`mat-button`,``,3,`click`,`disabled`],[`mat-flat-button`,``,3,`click`,`disabled`],[1,`actions`],[`mat-button`,``,3,`disabled`],[`mat-button`,``],[`mode`,`determinate`,3,`value`],[1,`error`],[`mat-button`,``,3,`click`],[1,`screening-report`],[3,`href`],[`mat-flat-button`,``,3,`disabled`],[`mat-flat-button`,``],[1,`codex-usage`],[1,`artifact`],[`mat-flat-button`,``,3,`click`],[`open`,``,1,`conversation`],[1,`attempt`],[`aria-live`,`polite`,1,`activity-feed`],[3,`model`],[1,`logs`]],template:function(e,i){if(e&1&&(l(0,`main`)(1,`header`),z(2,`img`,0),l(3,`div`)(4,`h1`),u(5,`AutoLLM campaigns`),d(),l(6,`p`),u(7,`Generate components, tune algorithms, and compare each independent proposal against SOTA.`),d()()(),D(8,s2,2,1,`p`,1),D(9,c2,2,1,`p`,1),D(10,p2,113,46,`mat-card`),D(11,M2,11,3,`mat-card`),l(12,`label`),u(13,`Saved campaign`),l(14,`select`,2),C(`change`,function(o){return i.store.select(o.target.value)}),l(15,`option`,3),u(16,`Select campaign`),d(),be(17,N2,3,7,`option`,4,oE),d()(),D(19,fL,54,24),d()),e&2){let a,o;h(8),k(i.store.error()?8:-1),h(),k(i.store.connectionError()?9:-1),h(),k((a=i.store.config())?10:-1,a),h(),k(i.store.screenings().length?11:-1),h(3),S(`value`,i.store.selectedId()),h(3),ve(i.store.campaigns()),h(2),k((o=i.store.campaign())?19:-1,o)}},dependencies:[Ot,en,zn,Bn,jn,rs,os,Ai,So,Xi,wn],styles:[`[_nghost-%COMP%]{display:block;background:var(--%NS%mat-sys-surface);color:var(--%NS%mat-sys-on-surface);min-height:100vh}main[_ngcontent-%COMP%]{max-width:88rem;margin:auto;padding:1.5rem;display:grid;gap:1rem}header[_ngcontent-%COMP%], .actions[_ngcontent-%COMP%], .artifact[_ngcontent-%COMP%]{display:flex;align-items:center;flex-wrap:wrap;gap:1rem}header[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:3rem}fieldset[_ngcontent-%COMP%]{border:0;padding:1rem 0}.fields[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(auto-fit,minmax(16rem,1fr));gap:1rem}label[_ngcontent-%COMP%]{display:flex;flex-direction:column;gap:.4rem;font-weight:500;margin-bottom:.6rem}input[_ngcontent-%COMP%], textarea[_ngcontent-%COMP%], select[_ngcontent-%COMP%]{padding:.6rem;background:var(--%NS%mat-sys-surface-container);color:inherit;border:1px solid var(--%NS%mat-sys-outline);border-radius:.4rem;font:inherit}textarea[_ngcontent-%COMP%]{width:100%;box-sizing:border-box}summary[_ngcontent-%COMP%]{cursor:pointer;padding:.5rem 0}table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse;margin:1rem 0}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{text-align:left;padding:.6rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}pre[_ngcontent-%COMP%]{white-space:pre-wrap;overflow-wrap:anywhere;overflow:auto;max-height:25rem;padding:.75rem;background:var(--%NS%mat-sys-surface-container)}.logs[_ngcontent-%COMP%]{white-space:pre}.error[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}.attempt[_ngcontent-%COMP%]{padding:.5rem 0}.artifact[_ngcontent-%COMP%]{padding:.25rem 0}.artifact[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{overflow-wrap:anywhere}.activity-feed[_ngcontent-%COMP%]{max-height:32rem;overflow:auto;padding:.75rem;background:var(--%NS%mat-sys-surface-container-low)}.activity-feed[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{margin-bottom:1rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}.activity-feed[_ngcontent-%COMP%]   pre[_ngcontent-%COMP%], .screening-report[_ngcontent-%COMP%]{white-space:pre-wrap;overflow-wrap:anywhere}.budget-summary[_ngcontent-%COMP%]{line-height:1.6}`]})};var Bm=class t{client=f(Fa);destroyRef=f(Te);viewMode=N(`detecting`);latestError=N(null);timer=null;started=!1;destroyed=!1;polling=!1;retry=!1;delay=500;constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.timer&&clearTimeout(this.timer)})}start(){this.started||(this.started=!0,this.poll())}retryNow(){this.timer&&clearTimeout(this.timer),this.polling?this.retry=!0:this.poll()}async poll(){if(this.destroyed)return;this.polling=!0;let n=1e4;try{let e=await this.client.getMode();if(this.destroyed)return;this.viewMode.set(e.mode.toLowerCase()),this.latestError.set(null),this.delay=500,e.mode===`DETECTING`&&(n=500)}catch(e){if(this.destroyed)return;this.latestError.set(e instanceof Error?e.message:`Could not contact the backend`),this.viewMode()===`detecting`&&this.viewMode.set(`unavailable`),n=this.delay,this.delay=Math.min(3e4,this.delay*2)}finally{this.polling=!1,this.destroyed||(this.timer=setTimeout(()=>{this.poll()},this.retry?0:n),this.retry=!1)}}static ɵfac=function(e){return new(e||t)};static ɵprov=fe({token:t,factory:t.ɵfac,providedIn:`root`})};function gL(t,n){t&1&&z(0,`app-autollm-dashboard`)}function bL(t,n){t&1&&z(0,`app-standard-dashboard`)}function vL(t,n){t&1&&z(0,`app-tuning-dashboard`)}function yL(t,n){t&1&&(l(0,`main`,0),z(1,`img`,2),l(2,`mat-card`,3)(3,`mat-card-header`)(4,`mat-card-title`),u(5,`Autoconfig worker`),d()(),l(6,`mat-card-content`)(7,`p`),u(8,`This process executes evaluations for the coordinator.`),d(),l(9,`p`),u(10,`The monitoring glass panel is available on the coordinator process.`),d()()()())}function _L(t,n){t&1&&(l(0,`main`,0),z(1,`img`,2),l(2,`mat-card`)(3,`mat-card-content`)(4,`h1`),u(5,`Instance utility`),d(),l(6,`p`),u(7,`This process is analysing or selecting instances. It is not running a measured experiment.`),d()()()())}function xL(t,n){if(t&1){let e=X();l(0,`main`,0),z(1,`img`,2),l(2,`mat-card`,3)(3,`mat-card-header`)(4,`mat-card-title`),u(5,`Waiting for the Mork backend`),d()(),l(6,`mat-card-content`)(7,`p`),u(8),d(),l(9,`button`,4),C(`click`,function(){A(e);return O(y().store.retryNow())}),u(10,`Retry now`),d()()()()}if(t&2){let e=y();h(8),w(e.store.latestError()??`The run mode could not be detected.`)}}function CL(t,n){t&1&&(l(0,`main`,1),z(1,`img`,2)(2,`mat-spinner`,5),l(3,`p`),u(4,`Detecting experiment mode…`),d()())}_g(class t{store=f(Bm);ngOnInit(){this.store.start()}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-root`]],decls:7,vars:1,consts:[[1,`mode-state`],[`aria-label`,`Detecting experiment mode`,1,`mode-state`,`detecting`],[`src`,`favicon.svg`,`alt`,`Mork`],[`appearance`,`outlined`],[`mat-flat-button`,``,`type`,`button`,3,`click`],[`diameter`,`36`]],template:function(e,i){if(e&1&&D(0,gL,1,0,`app-autollm-dashboard`)(1,bL,1,0,`app-standard-dashboard`)(2,vL,1,0,`app-tuning-dashboard`)(3,yL,11,0,`main`,0)(4,_L,8,0,`main`,0)(5,xL,11,1,`main`,0)(6,CL,5,0,`main`,1),e&2){let a;k((a=i.store.viewMode())===`autollm`?0:a===`standard`?1:a===`tuning`?2:a===`worker`?3:a===`utility`?4:a===`unavailable`?5:6)}},dependencies:[Ot,en,zn,Bn,jn,Ma,Ia,oS,aS,_m,Pm,Vm],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh}.mode-state[_ngcontent-%COMP%]{display:grid;place-content:center;min-height:100vh;padding:1rem;color:var(--%NS%mat-sys-on-surface);background:radial-gradient(circle at 50% 20%,color-mix(in srgb,var(--%NS%mat-sys-tertiary) 16%,transparent),transparent 25rem),var(--%NS%mat-sys-surface-container-lowest)}.mode-state[_ngcontent-%COMP%] > img[_ngcontent-%COMP%]{width:5rem;height:5rem;margin:0 auto 1.25rem}.mode-state[_ngcontent-%COMP%]   mat-card[_ngcontent-%COMP%]{width:min(32rem,100vw - 2rem)}.mode-state.detecting[_ngcontent-%COMP%]{justify-items:center;color:var(--%NS%mat-sys-on-surface-variant)}.mode-state.detecting[_ngcontent-%COMP%] > img[_ngcontent-%COMP%]{margin-bottom:1.5rem}`]})},_C).catch(t=>console.error(t));export{s as n,r as t};