var k$1=Object.create;var g=Object.defineProperty;var l=Object.defineProperties;var m$1=Object.getOwnPropertyDescriptor;var n=Object.getOwnPropertyDescriptors;var o=Object.getOwnPropertyNames;var f$1=Object.getOwnPropertySymbols;var p=Object.getPrototypeOf;var h=Object.prototype.hasOwnProperty;var j$1=Object.prototype.propertyIsEnumerable;var i=(a,b,c)=>b in a?g(a,b,{enumerable:!0,configurable:!0,writable:!0,value:c}):a[b]=c;var r=(a,b)=>{for(var c in b||={})h.call(b,c)&&i(a,c,b[c]);if(f$1)for(var c of f$1(b))j$1.call(b,c)&&i(a,c,b[c]);return a};var s=(a,b)=>l(a,n(b));var t=(a,b)=>{var c={};for(var d in a)h.call(a,d)&&b.indexOf(d)<0&&(c[d]=a[d]);if(a!=null&&f$1)for(var d of f$1(a))b.indexOf(d)<0&&j$1.call(a,d)&&(c[d]=a[d]);return c};var u$1=(a,b)=>()=>{try{return b||a((b={exports:{}}).exports,b),b.exports}catch(c){throw b=0,c}};var q$1=(a,b,c,d)=>{if(b&&typeof b==`object`||typeof b==`function`)for(let e of o(b))!h.call(a,e)&&e!==c&&g(a,e,{get:()=>b[e],enumerable:!(d=m$1(b,e))||d.enumerable});return a};var v$1=(a,b,c)=>(c=a!=null?k$1(p(a)):{},q$1(b||!a||!a.__esModule?g(c,`default`,{value:a,enumerable:!0}):c,a));var Hw=u$1((xm,zw)=>{"use strict";(function(t,n){typeof xm==`object`&&typeof zw<`u`?n(xm):typeof define==`function`&&define.amd?define([`exports`],n):(t=typeof globalThis<`u`?globalThis:t||self,n(t.StompJs={}))})(xm,(function(t){"use strict";function n(L,x){L.terminate=function(){let A=()=>{};this.onerror=A,this.onmessage=A,this.onopen=A;let R=new Date,W=Math.random().toString().substring(2,8),ye=this.onclose;this.onclose=bt=>{let We=new Date().getTime()-R.getTime();x(`Discarded socket (#${W})  closed after ${We}ms, with code/reason: ${bt.code}/${bt.reason}`)},this.close(),ye?.call(L,{code:4001,reason:`Quick discarding socket (#${W}) without waiting for the shutdown sequence.`,wasClean:!1})}}let e={LF:`
`,NULL:`\0`};class i{get body(){return!this._body&&this.isBinaryBody&&(this._body=new TextDecoder().decode(this._binaryBody)),this._body||``}get binaryBody(){return!this._binaryBody&&!this.isBinaryBody&&(this._binaryBody=new TextEncoder().encode(this._body)),this._binaryBody}constructor(x){let{command:A,headers:R,body:W,binaryBody:ye,escapeHeaderValues:bt,skipContentLengthHeader:We}=x;this.command=A,this.headers=Object.assign({},R||{}),ye?(this._binaryBody=ye,this.isBinaryBody=!0):(this._body=W||``,this.isBinaryBody=!1),this.escapeHeaderValues=bt||!1,this.skipContentLengthHeader=We||!1}static fromRawFrame(x,A){let R={},W=ye=>ye.replace(/^\s+|\s+$/g,``);for(let ye of x.headers.reverse()){ye.indexOf(`:`);let bt=W(ye[0]),We=W(ye[1]);A&&x.command!==`CONNECT`&&x.command!==`CONNECTED`&&(We=i.hdrValueUnEscape(We)),R[bt]=We}return new i({command:x.command,headers:R,binaryBody:x.binaryBody,escapeHeaderValues:A})}toString(){return this.serializeCmdAndHeaders()}serialize(){let x=this.serializeCmdAndHeaders();return this.isBinaryBody?i.toUnit8Array(x,this._binaryBody).buffer:x+this._body+e.NULL}serializeCmdAndHeaders(){let x=[this.command];this.skipContentLengthHeader&&delete this.headers[`content-length`];for(let A of Object.keys(this.headers||{})){let R=this.headers[A];this.escapeHeaderValues&&this.command!==`CONNECT`&&this.command!==`CONNECTED`?x.push(`${A}:${i.hdrValueEscape(`${R}`)}`):x.push(`${A}:${R}`)}return(this.isBinaryBody||!this.isBodyEmpty()&&!this.skipContentLengthHeader)&&x.push(`content-length:${this.bodyLength()}`),x.join(e.LF)+e.LF+e.LF}isBodyEmpty(){return this.bodyLength()===0}bodyLength(){let x=this.binaryBody;return x?x.length:0}static sizeOfUTF8(x){return x?new TextEncoder().encode(x).length:0}static toUnit8Array(x,A){let R=new TextEncoder().encode(x),W=new Uint8Array([0]),ye=new Uint8Array(R.length+A.length+W.length);return ye.set(R),ye.set(A,R.length),ye.set(W,R.length+A.length),ye}static marshall(x){return new i(x).serialize()}static hdrValueEscape(x){return x.replace(/\\/g,`\\\\`).replace(/\r/g,`\\r`).replace(/\n/g,`\\n`).replace(/:/g,`\\c`)}static hdrValueUnEscape(x){return x.replace(/\\r/g,`\r`).replace(/\\n/g,`
`).replace(/\\c/g,`:`).replace(/\\\\/g,`\\`)}}let a=0,o=10,r=13,s=58;class l{constructor(x,A){this.onFrame=x,this.onIncomingPing=A,this._encoder=new TextEncoder,this._decoder=new TextDecoder,this._token=[],this._initState()}parseChunk(x,A=!1){let R;if(typeof x==`string`?R=this._encoder.encode(x):R=new Uint8Array(x),A&&R[R.length-1]!==0){let W=new Uint8Array(R.length+1);W.set(R,0),W[R.length]=0,R=W}for(let W=0;W<R.length;W++){let ye=R[W];this._onByte(ye)}}_collectFrame(x){if(x!==a&&x!==r){if(x===o){this.onIncomingPing();return}this._onByte=this._collectCommand,this._reinjectByte(x)}}_collectCommand(x){if(x!==r){if(x===o){this._results.command=this._consumeTokenAsUTF8(),this._onByte=this._collectHeaders;return}this._consumeByte(x)}}_collectHeaders(x){if(x!==r){if(x===o){this._setupCollectBody();return}this._onByte=this._collectHeaderKey,this._reinjectByte(x)}}_reinjectByte(x){this._onByte(x)}_collectHeaderKey(x){if(x===s){this._headerKey=this._consumeTokenAsUTF8(),this._onByte=this._collectHeaderValue;return}this._consumeByte(x)}_collectHeaderValue(x){if(x!==r){if(x===o){this._results.headers.push([this._headerKey,this._consumeTokenAsUTF8()]),this._headerKey=void 0,this._onByte=this._collectHeaders;return}this._consumeByte(x)}}_setupCollectBody(){let x=this._results.headers.filter(A=>A[0]===`content-length`)[0];x?(this._bodyBytesRemaining=parseInt(x[1],10),this._onByte=this._collectBodyFixedSize):this._onByte=this._collectBodyNullTerminated}_collectBodyNullTerminated(x){if(x===a){this._retrievedBody();return}this._consumeByte(x)}_collectBodyFixedSize(x){if(this._bodyBytesRemaining--===0){this._retrievedBody();return}this._consumeByte(x)}_retrievedBody(){this._results.binaryBody=this._consumeTokenAsRaw();try{this.onFrame(this._results)}catch(x){console.log(`Ignoring an exception thrown by a frame handler. Original exception: `,x)}this._initState()}_consumeByte(x){this._token.push(x)}_consumeTokenAsUTF8(){return this._decoder.decode(this._consumeTokenAsRaw())}_consumeTokenAsRaw(){let x=new Uint8Array(this._token);return this._token=[],x}_initState(){this._results={command:void 0,headers:[],binaryBody:void 0},this._token=[],this._headerKey=void 0,this._onByte=this._collectFrame}}t.StompSocketState=void 0,(function(L){L[L.CONNECTING=0]=`CONNECTING`,L[L.OPEN=1]=`OPEN`,L[L.CLOSING=2]=`CLOSING`,L[L.CLOSED=3]=`CLOSED`})(t.StompSocketState||(t.StompSocketState={})),t.ActivationState=void 0,(function(L){L[L.ACTIVE=0]=`ACTIVE`,L[L.DEACTIVATING=1]=`DEACTIVATING`,L[L.INACTIVE=2]=`INACTIVE`})(t.ActivationState||(t.ActivationState={})),t.ReconnectionTimeMode=void 0,(function(L){L[L.LINEAR=0]=`LINEAR`,L[L.EXPONENTIAL=1]=`EXPONENTIAL`})(t.ReconnectionTimeMode||(t.ReconnectionTimeMode={})),t.TickerStrategy=void 0,(function(L){L.Interval=`interval`,L.Worker=`worker`})(t.TickerStrategy||(t.TickerStrategy={}));class h{constructor(x,A=t.TickerStrategy.Interval,R){this._interval=x,this._strategy=A,this._debug=R,this._workerScript=`
    var startTime = Date.now();
    setInterval(function() {
        self.postMessage(Date.now() - startTime);
    }, ${this._interval});
  `}start(x){this.stop(),this.shouldUseWorker()?this.runWorker(x):this.runInterval(x)}stop(){this.disposeWorker(),this.disposeInterval()}shouldUseWorker(){return typeof Worker<`u`&&this._strategy===t.TickerStrategy.Worker}runWorker(x){this._debug(`Using runWorker for outgoing pings`),this._worker||(this._worker=new Worker(URL.createObjectURL(new Blob([this._workerScript],{type:`text/javascript`}))),this._worker.onmessage=A=>x(A.data))}runInterval(x){if(this._debug(`Using runInterval for outgoing pings`),!this._timer){let A=Date.now();this._timer=setInterval(()=>{x(Date.now()-A)},this._interval)}}disposeWorker(){this._worker&&(this._worker.terminate(),delete this._worker,this._debug(`Outgoing ping disposeWorker`))}disposeInterval(){this._timer&&(clearInterval(this._timer),delete this._timer,this._debug(`Outgoing ping disposeInterval`))}}class p{constructor(x){this.versions=x}supportedVersions(){return this.versions.join(`,`)}protocolVersions(){return this.versions.map(x=>`v${x.replace(`.`,``)}.stomp`)}}p.V1_0=`1.0`,p.V1_1=`1.1`,p.V1_2=`1.2`,p.default=new p([p.V1_2,p.V1_1,p.V1_0]);class g{get connectedVersion(){return this._connectedVersion}get connected(){return this._connected}constructor(x,A,R){this._client=x,this._webSocket=A,this._connected=!1,this._serverFrameHandlers={CONNECTED:W=>{this.debug(`connected to server ${W.headers.server}`),this._connected=!0,this._connectedVersion=W.headers.version,this._connectedVersion===p.V1_2&&(this._escapeHeaderValues=!0),this._setupHeartbeat(W.headers),this.onConnect(W)},MESSAGE:W=>{let ye=W.headers.subscription,bt=this._subscriptions[ye]||this.onUnhandledMessage,We=W,pe=this,Ie=this._connectedVersion===p.V1_2?We.headers.ack:We.headers[`message-id`];We.ack=(F={})=>pe.ack(Ie,ye,F),We.nack=(F={})=>pe.nack(Ie,ye,F),bt(We)},RECEIPT:W=>{let ye=this._receiptWatchers[W.headers[`receipt-id`]];ye?(ye(W),delete this._receiptWatchers[W.headers[`receipt-id`]]):this.onUnhandledReceipt(W)},ERROR:W=>{this.onStompError(W)}},this._counter=0,this._subscriptions={},this._receiptWatchers={},this._partialData=``,this._escapeHeaderValues=!1,this._lastServerActivityTS=Date.now(),this.debug=R.debug,this.stompVersions=R.stompVersions,this.connectHeaders=R.connectHeaders,this.disconnectHeaders=R.disconnectHeaders,this.heartbeatIncoming=R.heartbeatIncoming,this.heartbeatToleranceMultiplier=R.heartbeatGracePeriods,this.heartbeatOutgoing=R.heartbeatOutgoing,this.splitLargeFrames=R.splitLargeFrames,this.maxWebSocketChunkSize=R.maxWebSocketChunkSize,this.forceBinaryWSFrames=R.forceBinaryWSFrames,this.logRawCommunication=R.logRawCommunication,this.appendMissingNULLonIncoming=R.appendMissingNULLonIncoming,this.discardWebsocketOnCommFailure=R.discardWebsocketOnCommFailure,this.onConnect=R.onConnect,this.onDisconnect=R.onDisconnect,this.onStompError=R.onStompError,this.onWebSocketClose=R.onWebSocketClose,this.onWebSocketError=R.onWebSocketError,this.onUnhandledMessage=R.onUnhandledMessage,this.onUnhandledReceipt=R.onUnhandledReceipt,this.onUnhandledFrame=R.onUnhandledFrame,this.onHeartbeatReceived=R.onHeartbeatReceived,this.onHeartbeatLost=R.onHeartbeatLost}start(){let x=new l(R=>{let W=i.fromRawFrame(R,this._escapeHeaderValues);this.logRawCommunication||this.debug(`<<< ${W}`),(this._serverFrameHandlers[W.command]||this.onUnhandledFrame)(W)},()=>{this.debug(`<<< PONG`),this.onHeartbeatReceived()});this._webSocket.onmessage=R=>{if(this.debug(`Received data`),this._lastServerActivityTS=Date.now(),this.logRawCommunication){let W=R.data instanceof ArrayBuffer?new TextDecoder().decode(R.data):R.data;this.debug(`<<< ${W}`)}x.parseChunk(R.data,this.appendMissingNULLonIncoming)},this._webSocket.onclose=R=>{this.debug(`Connection closed to ${this._webSocket.url}`),this._cleanUp(),this.onWebSocketClose(R)},this._webSocket.onerror=R=>{this.onWebSocketError(R)};let A=()=>{let R=Object.assign({},this.connectHeaders);this.debug(`Web Socket Opened...`),R[`accept-version`]=this.stompVersions.supportedVersions(),R[`heart-beat`]=[this.heartbeatOutgoing,this.heartbeatIncoming].join(`,`),this._transmit({command:`CONNECT`,headers:R})};this._webSocket.readyState===t.StompSocketState.OPEN?A():this._webSocket.onopen=A}_setupHeartbeat(x){if(x.version!==p.V1_1&&x.version!==p.V1_2||!x[`heart-beat`])return;let[A,R]=x[`heart-beat`].split(`,`).map(W=>parseInt(W,10));if(this.heartbeatOutgoing!==0&&R!==0){let W=Math.max(this.heartbeatOutgoing,R);this.debug(`send PING every ${W}ms`),this._pinger=new h(W,this._client.heartbeatStrategy,this.debug),this._pinger.start(()=>{this._webSocket.readyState===t.StompSocketState.OPEN&&(this._webSocket.send(e.LF),this.debug(`>>> PING`))})}if(this.heartbeatIncoming!==0&&A!==0){let W=Math.max(this.heartbeatIncoming,A);this.debug(`check PONG every ${W}ms`),this._ponger=setInterval(()=>{let ye=Date.now()-this._lastServerActivityTS;ye>W*this.heartbeatToleranceMultiplier&&(this.debug(`did not receive server activity for the last ${ye}ms`),this.onHeartbeatLost(),this._closeOrDiscardWebsocket())},W)}}_closeOrDiscardWebsocket(){this.discardWebsocketOnCommFailure?(this.debug(`Discarding websocket, the underlying socket may linger for a while`),this.discardWebsocket()):(this.debug(`Issuing close on the websocket`),this._closeWebsocket())}forceDisconnect(){this._webSocket&&(this._webSocket.readyState===t.StompSocketState.CONNECTING||this._webSocket.readyState===t.StompSocketState.OPEN)&&this._closeOrDiscardWebsocket()}_closeWebsocket(){this._webSocket.onmessage=()=>{},this._webSocket.close()}discardWebsocket(){typeof this._webSocket.terminate!=`function`&&n(this._webSocket,x=>this.debug(x)),this._webSocket.terminate()}_transmit(x){let{command:A,headers:R,body:W,binaryBody:ye,skipContentLengthHeader:bt}=x,We=new i({command:A,headers:R,body:W,binaryBody:ye,escapeHeaderValues:this._escapeHeaderValues,skipContentLengthHeader:bt}),pe=We.serialize();if(this.logRawCommunication?this.debug(`>>> ${pe}`):this.debug(`>>> ${We}`),this.forceBinaryWSFrames&&typeof pe==`string`&&(pe=new TextEncoder().encode(pe)),typeof pe!=`string`||!this.splitLargeFrames)this._webSocket.send(pe);else{let Ie=pe;for(;Ie.length>0;){let F=Ie.substring(0,this.maxWebSocketChunkSize);Ie=Ie.substring(this.maxWebSocketChunkSize),this._webSocket.send(F),this.debug(`chunk sent = ${F.length}, remaining = ${Ie.length}`)}}}dispose(){if(this.connected)try{let x=Object.assign({},this.disconnectHeaders);x.receipt||(x.receipt=`close-${this._counter++}`),this.watchForReceipt(x.receipt,A=>{this._closeWebsocket(),this._cleanUp(),this.onDisconnect(A)}),this._transmit({command:`DISCONNECT`,headers:x})}catch(x){this.debug(`Ignoring error during disconnect ${x}`)}else(this._webSocket.readyState===t.StompSocketState.CONNECTING||this._webSocket.readyState===t.StompSocketState.OPEN)&&this._closeWebsocket()}_cleanUp(){this._connected=!1,this._pinger&&(this._pinger.stop(),this._pinger=void 0),this._ponger&&(clearInterval(this._ponger),this._ponger=void 0)}publish(x){let{destination:A,headers:R,body:W,binaryBody:ye,skipContentLengthHeader:bt}=x,We=Object.assign({destination:A},R);this._transmit({command:`SEND`,headers:We,body:W,binaryBody:ye,skipContentLengthHeader:bt})}watchForReceipt(x,A){this._receiptWatchers[x]=A}subscribe(x,A,R={}){R=Object.assign({},R),R.id||(R.id=`sub-${this._counter++}`),R.destination=x,this._subscriptions[R.id]=A,this._transmit({command:`SUBSCRIBE`,headers:R});let W=this;return{id:R.id,unsubscribe(ye){return W.unsubscribe(R.id,ye)}}}unsubscribe(x,A={}){A=Object.assign({},A),delete this._subscriptions[x],A.id=x,this._transmit({command:`UNSUBSCRIBE`,headers:A})}begin(x){let A=x||`tx-${this._counter++}`;this._transmit({command:`BEGIN`,headers:{transaction:A}});let R=this;return{id:A,commit(){R.commit(A)},abort(){R.abort(A)}}}commit(x){this._transmit({command:`COMMIT`,headers:{transaction:x}})}abort(x){this._transmit({command:`ABORT`,headers:{transaction:x}})}ack(x,A,R={}){R=Object.assign({},R),this._connectedVersion===p.V1_2?R.id=x:R[`message-id`]=x,R.subscription=A,this._transmit({command:`ACK`,headers:R})}nack(x,A,R={}){return R=Object.assign({},R),this._connectedVersion===p.V1_2?R.id=x:R[`message-id`]=x,R.subscription=A,this._transmit({command:`NACK`,headers:R})}}class _{get webSocket(){return this._stompHandler?._webSocket}get disconnectHeaders(){return this._disconnectHeaders}set disconnectHeaders(x){this._disconnectHeaders=x,this._stompHandler&&(this._stompHandler.disconnectHeaders=this._disconnectHeaders)}get connected(){return!!this._stompHandler&&this._stompHandler.connected}get connectedVersion(){return this._stompHandler?this._stompHandler.connectedVersion:void 0}get active(){return this.state===t.ActivationState.ACTIVE}_changeState(x){this.state=x,this.onChangeState(x)}constructor(x={}){this.stompVersions=p.default,this.connectionTimeout=0,this.reconnectDelay=5e3,this._nextReconnectDelay=0,this.maxReconnectDelay=9e5,this.reconnectTimeMode=t.ReconnectionTimeMode.LINEAR,this.heartbeatIncoming=1e4,this.heartbeatToleranceMultiplier=2,this.heartbeatOutgoing=1e4,this.heartbeatStrategy=t.TickerStrategy.Interval,this.splitLargeFrames=!1,this.maxWebSocketChunkSize=8192,this.forceBinaryWSFrames=!1,this.appendMissingNULLonIncoming=!1,this.discardWebsocketOnCommFailure=!1,this.state=t.ActivationState.INACTIVE;let A=()=>{};this.debug=A,this.beforeConnect=A,this.onConnect=A,this.onDisconnect=A,this.onUnhandledMessage=A,this.onUnhandledReceipt=A,this.onUnhandledFrame=A,this.onHeartbeatReceived=A,this.onHeartbeatLost=A,this.onStompError=A,this.onWebSocketClose=A,this.onWebSocketError=A,this.logRawCommunication=!1,this.onChangeState=A,this.connectHeaders={},this._disconnectHeaders={},this.configure(x)}configure(x){Object.assign(this,x),this.maxReconnectDelay>0&&this.maxReconnectDelay<this.reconnectDelay&&(this.debug(`Warning: maxReconnectDelay (${this.maxReconnectDelay}ms) is less than reconnectDelay (${this.reconnectDelay}ms). Using reconnectDelay as the maxReconnectDelay delay.`),this.maxReconnectDelay=this.reconnectDelay)}activate(){let x=()=>{if(this.active){this.debug(`Already ACTIVE, ignoring request to activate`);return}this._changeState(t.ActivationState.ACTIVE),this._nextReconnectDelay=this.reconnectDelay,this._connect()};this.state===t.ActivationState.DEACTIVATING?(this.debug(`Waiting for deactivation to finish before activating`),this.deactivate().then(()=>{x()})):x()}async _connect(){if(await this.beforeConnect(this),this._stompHandler){this.debug(`There is already a stompHandler, skipping the call to connect`);return}if(!this.active){this.debug(`Client has been marked inactive, will not attempt to connect`);return}this.connectionTimeout>0&&(this._connectionWatcher&&clearTimeout(this._connectionWatcher),this._connectionWatcher=setTimeout(()=>{this.connected||(this.debug(`Connection not established in ${this.connectionTimeout}ms, closing socket`),this.forceDisconnect())},this.connectionTimeout)),this.debug(`Opening Web Socket...`);let x=this._createWebSocket();this._stompHandler=new g(this,x,{debug:this.debug,stompVersions:this.stompVersions,connectHeaders:this.connectHeaders,disconnectHeaders:this._disconnectHeaders,heartbeatIncoming:this.heartbeatIncoming,heartbeatGracePeriods:this.heartbeatToleranceMultiplier,heartbeatOutgoing:this.heartbeatOutgoing,heartbeatStrategy:this.heartbeatStrategy,splitLargeFrames:this.splitLargeFrames,maxWebSocketChunkSize:this.maxWebSocketChunkSize,forceBinaryWSFrames:this.forceBinaryWSFrames,logRawCommunication:this.logRawCommunication,appendMissingNULLonIncoming:this.appendMissingNULLonIncoming,discardWebsocketOnCommFailure:this.discardWebsocketOnCommFailure,onConnect:A=>{if(this._connectionWatcher&&(clearTimeout(this._connectionWatcher),this._connectionWatcher=void 0),this._nextReconnectDelay=this.reconnectDelay,!this.active){this.debug(`STOMP got connected while deactivate was issued, will disconnect now`),this._disposeStompHandler();return}this.onConnect(A)},onDisconnect:A=>{this.onDisconnect(A)},onStompError:A=>{this.onStompError(A)},onWebSocketClose:A=>{this._stompHandler=void 0,this.state===t.ActivationState.DEACTIVATING&&this._changeState(t.ActivationState.INACTIVE),this.onWebSocketClose(A),this.active&&this._schedule_reconnect()},onWebSocketError:A=>{this.onWebSocketError(A)},onUnhandledMessage:A=>{this.onUnhandledMessage(A)},onUnhandledReceipt:A=>{this.onUnhandledReceipt(A)},onUnhandledFrame:A=>{this.onUnhandledFrame(A)},onHeartbeatReceived:()=>{this.onHeartbeatReceived()},onHeartbeatLost:()=>{this.onHeartbeatLost()}}),this._stompHandler.start()}_createWebSocket(){let x;if(this.webSocketFactory)x=this.webSocketFactory();else if(this.brokerURL)x=new WebSocket(this.brokerURL,this.stompVersions.protocolVersions());else throw new Error(`Either brokerURL or webSocketFactory must be provided`);return x.binaryType=`arraybuffer`,x}_schedule_reconnect(){this._nextReconnectDelay>0&&(this.debug(`STOMP: scheduling reconnection in ${this._nextReconnectDelay}ms`),this._reconnector=setTimeout(()=>{this.reconnectTimeMode===t.ReconnectionTimeMode.EXPONENTIAL&&(this._nextReconnectDelay=this._nextReconnectDelay*2,this.maxReconnectDelay!==0&&(this._nextReconnectDelay=Math.min(this._nextReconnectDelay,this.maxReconnectDelay))),this._connect()},this._nextReconnectDelay))}async deactivate(x={}){let A=x.force||!1,R=this.active,W;if(this.state===t.ActivationState.INACTIVE)return this.debug(`Already INACTIVE, nothing more to do`),Promise.resolve();if(this._changeState(t.ActivationState.DEACTIVATING),this._nextReconnectDelay=0,this._reconnector&&(clearTimeout(this._reconnector),this._reconnector=void 0),this._stompHandler&&this.webSocket.readyState!==t.StompSocketState.CLOSED){let ye=this._stompHandler.onWebSocketClose;W=new Promise((bt,We)=>{this._stompHandler.onWebSocketClose=pe=>{ye(pe),bt()}})}else return this._changeState(t.ActivationState.INACTIVE),Promise.resolve();return A?this._stompHandler?.discardWebsocket():R&&this._disposeStompHandler(),W}forceDisconnect(){this._stompHandler&&this._stompHandler.forceDisconnect()}_disposeStompHandler(){this._stompHandler&&this._stompHandler.dispose()}publish(x){this._checkConnection(),this._stompHandler.publish(x)}_checkConnection(){if(!this.connected)throw new TypeError(`There is no underlying STOMP connection`)}watchForReceipt(x,A){this._checkConnection(),this._stompHandler.watchForReceipt(x,A)}subscribe(x,A,R={}){return this._checkConnection(),this._stompHandler.subscribe(x,A,R)}unsubscribe(x,A={}){this._checkConnection(),this._stompHandler.unsubscribe(x,A)}begin(x){return this._checkConnection(),this._stompHandler.begin(x)}commit(x){this._checkConnection(),this._stompHandler.commit(x)}abort(x){this._checkConnection(),this._stompHandler.abort(x)}ack(x,A,R={}){this._checkConnection(),this._stompHandler.ack(x,A,R)}nack(x,A,R={}){this._checkConnection(),this._stompHandler.nack(x,A,R)}}class b{}class y{}class I{constructor(x){this.client=x}get outgoing(){return this.client.heartbeatOutgoing}set outgoing(x){this.client.heartbeatOutgoing=x}get incoming(){return this.client.heartbeatIncoming}set incoming(x){this.client.heartbeatIncoming=x}}class P extends _{constructor(x){super(),this.maxWebSocketFrameSize=16384,this._heartbeatInfo=new I(this),this.reconnect_delay=0,this.webSocketFactory=x,this.debug=(...A)=>{console.log(...A)}}_parseConnect(...x){let A,R,W,ye={};if(x.length<2)throw new Error(`Connect requires at least 2 arguments`);return typeof x[1]==`function`?[ye,R,W,A]=x:x.length===6?[ye.login,ye.passcode,R,W,A,ye.host]=x:[ye.login,ye.passcode,R,W,A]=x,[ye,R,W,A]}connect(...x){let A=this._parseConnect(...x);A[0]&&(this.connectHeaders=A[0]),A[1]&&(this.onConnect=A[1]),A[2]&&(this.onStompError=A[2]),A[3]&&(this.onWebSocketClose=A[3]),super.activate()}disconnect(x,A={}){x&&(this.onDisconnect=x),this.disconnectHeaders=A,super.deactivate()}send(x,A={},R=``){A=Object.assign({},A);let W=A[`content-length`]===!1;W&&delete A[`content-length`],this.publish({destination:x,headers:A,body:R,skipContentLengthHeader:W})}set reconnect_delay(x){this.reconnectDelay=x}get ws(){return this.webSocket}get version(){return this.connectedVersion}get onreceive(){return this.onUnhandledMessage}set onreceive(x){this.onUnhandledMessage=x}get onreceipt(){return this.onUnhandledReceipt}set onreceipt(x){this.onUnhandledReceipt=x}get heartbeat(){return this._heartbeatInfo}set heartbeat(x){this.heartbeatIncoming=x.incoming,this.heartbeatOutgoing=x.outgoing}}class ie{static client(x,A){A??=p.default.protocolVersions();let R=()=>{return new(ie.WebSocketClass||WebSocket)(x,A)};return new P(R)}static over(x){let A;return typeof x==`function`?A=x:(console.warn(`Stomp.over did not receive a factory, auto reconnect will not work. Please see https://stomp-js.github.io/api-docs/latest/classes/Stomp.html#over`),A=()=>x),new P(A)}}ie.WebSocketClass=null,t.Client=_,t.CompatClient=P,t.FrameImpl=i,t.Parser=l,t.Stomp=ie,t.StompConfig=b,t.StompHeaders=y,t.Versions=p}))});var rn=null;var ac=!1;var Ha=1;var hE=null;var St=Symbol(`SIGNAL`);function ge(t){let n=rn;return rn=t,n}function lc(){return rn}var $a={version:0,lastCleanEpoch:0,dirty:!1,producers:void 0,producersTail:void 0,consumers:void 0,consumersTail:void 0,recomputing:!1,consumerAllowSignalWrites:!1,consumerIsAlwaysLive:!1,kind:`unknown`,producerMustRecompute:()=>!1,producerRecomputeValue:()=>{},consumerMarkedDirty:()=>{},consumerOnSignalRead:()=>{}};function Ri(t){if(ac)throw new Error(``);if(rn===null)return;rn.consumerOnSignalRead(t);let n=rn.producersTail;if(n!==void 0&&n.producer===t)return;let e,i=rn.recomputing;if(i&&(e=n!==void 0?n.nextProducer:rn.producers,e!==void 0&&e.producer===t)){rn.producersTail=e,e.lastReadVersion=t.version,e.knownValidAtEpoch=Ha;return}let a=t.consumersTail;if(a!==void 0&&a.consumer===rn&&(!i||a.knownValidAtEpoch===Ha))return;let o=qo(rn),r={producer:t,consumer:rn,nextProducer:e,prevConsumer:void 0,knownValidAtEpoch:Ha,lastReadVersion:t.version,nextConsumer:void 0};rn.producersTail=r,n!==void 0?n.nextProducer=r:rn.producers=r,o&&Qb(t,r)}function qb(){Ha++}function gs(t){if(!(qo(t)&&!t.dirty)&&!(!t.dirty&&t.lastCleanEpoch===Ha)){if(!t.producerMustRecompute(t)&&!Wo(t)){sc(t);return}t.producerRecomputeValue(t),sc(t)}}function Km(t){if(t.consumers===void 0)return;let n=ac;ac=!0;try{for(let e=t.consumers;e!==void 0;e=e.nextConsumer){let i=e.consumer;i.dirty||fE(i)}}finally{ac=n}}function Qm(){return rn?.consumerAllowSignalWrites!==!1}function fE(t){t.dirty=!0,Km(t),t.consumerMarkedDirty?.(t)}function sc(t){t.dirty=!1,t.lastCleanEpoch=Ha}function ca(t){return t&&Yb(t),ge(t)}function Yb(t){if(t.producersTail?.knownValidAtEpoch===Ha){let n=t.producers;for(;n!==void 0;)n.knownValidAtEpoch=null,n=n.nextProducer}t.producersTail=void 0,t.recomputing=!0}function Ga(t,n){ge(n),t&&Kb(t)}function Kb(t){t.recomputing=!1;let n=t.producersTail,e=n!==void 0?n.nextProducer:t.producers;if(e!==void 0){if(qo(t))do e=Xm(e);while(e!==void 0);n!==void 0?n.nextProducer=void 0:t.producers=void 0}}function Wo(t){for(let n=t.producers;n!==void 0;n=n.nextProducer){let e=n.producer,i=n.lastReadVersion;if(i!==e.version||(gs(e),i!==e.version))return!0}return!1}function da(t){if(qo(t)){let n=t.producers;for(;n!==void 0;)n=Xm(n)}t.producers=void 0,t.producersTail=void 0,t.consumers=void 0,t.consumersTail=void 0}function Qb(t,n){let e=t.consumersTail,i=qo(t);if(e!==void 0?(n.nextConsumer=e.nextConsumer,e.nextConsumer=n):(n.nextConsumer=void 0,t.consumers=n),n.prevConsumer=e,t.consumersTail=n,!i)for(let a=t.producers;a!==void 0;a=a.nextProducer)Qb(a.producer,a)}function Xm(t){let n=t.producer,e=t.nextProducer,i=t.nextConsumer,a=t.prevConsumer;if(t.nextConsumer=void 0,t.prevConsumer=void 0,i!==void 0?i.prevConsumer=a:n.consumersTail=a,a!==void 0)a.nextConsumer=i;else if(n.consumers=i,!qo(n)){let o=n.producers;for(;o!==void 0;)o=Xm(o)}return e}function qo(t){return t.consumerIsAlwaysLive||t.consumers!==void 0}function cc(t){hE?.(t)}function dc(t,n){return Object.is(t,n)}function bs(t,n){let e=Object.create(gE);e.computation=t,n!==void 0&&(e.equal=n);let i=()=>{if(gs(e),Ri(e),e.value===Ua)throw e.error;return e.value};return i[St]=e,cc(e),i}var oc=Symbol(`UNSET`);var rc=Symbol(`COMPUTING`);var Ua=Symbol(`ERRORED`);var gE=s(r({},$a),{value:oc,dirty:!0,error:null,equal:dc,kind:`computed`,producerMustRecompute(t){return t.value===oc||t.value===rc},producerRecomputeValue(t){if(t.value===rc)throw new Error(``);let n=t.value;t.value=rc;let e=ca(t),i,a=!1;try{i=t.computation(),ge(null),a=n!==oc&&n!==Ua&&i!==Ua&&t.equal(n,i)}catch(o){i=Ua,t.error=o}finally{Ga(t,e)}if(a){t.value=n;return}t.value=i,t.version++}});function bE(){throw new Error}var Xb=bE;function Zb(t){Xb(t)}function Zm(t){Xb=t}var vE=null;function Jm(t,n){let e=Object.create(Yo);e.value=t,n!==void 0&&(e.equal=n);let i=()=>Jb(e);return i[St]=e,cc(e),[i,r=>ua(e,r),r=>ep(e,r)]}function Jb(t){return Ri(t),t.value}function ua(t,n){Qm()||Zb(t),t.equal(t.value,n)||(t.value=n,_E(t))}function ep(t,n){Qm()||Zb(t),ua(t,n(t.value))}var Yo=s(r({},$a),{equal:dc,value:void 0,kind:`signal`});function _E(t){t.version++,qb(),Km(t),vE?.(t)}var tp=s(r({},$a),{consumerIsAlwaysLive:!0,consumerAllowSignalWrites:!0,dirty:!0,kind:`effect`});function np(t){if(t.dirty=!1,t.version>0&&!Wo(t))return;t.version++;let n=ca(t);try{t.cleanup(),t.fn()}finally{Ga(t,n)}}var ip;function uc(){return ip}function pi(t){let n=ip;return ip=t,n}var ev=Symbol(`NotFound`);function Ko(t){return t===ev||t?.name===`ɵNotFound`}function tv(t){let n=ge(null);try{return t()}finally{ge(n)}}function Te(t){return typeof t==`function`}function ma(t){let e=t(i=>{Error.call(i),i.stack=new Error().stack});return e.prototype=Object.create(Error.prototype),e.prototype.constructor=e,e}var mc=ma(t=>function(e){t(this),this.message=e?`${e.length} errors occurred during unsubscription:
${e.map((i,a)=>`${a+1}) ${i.toString()}`).join(`
  `)}`:``,this.name=`UnsubscriptionError`,this.errors=e});function Wa(t,n){if(t){let e=t.indexOf(n);0<=e&&t.splice(e,1)}}var De=class t{constructor(n){this.initialTeardown=n,this.closed=!1,this._parentage=null,this._finalizers=null}unsubscribe(){let n;if(!this.closed){this.closed=!0;let{_parentage:e}=this;if(e)if(this._parentage=null,Array.isArray(e))for(let o of e)o.remove(this);else e.remove(this);let{initialTeardown:i}=this;if(Te(i))try{i()}catch(o){n=o instanceof mc?o.errors:[o]}let{_finalizers:a}=this;if(a){this._finalizers=null;for(let o of a)try{nv(o)}catch(r){n=n??[],r instanceof mc?n=[...n,...r.errors]:n.push(r)}}if(n)throw new mc(n)}}add(n){var e;if(n&&n!==this)if(this.closed)nv(n);else{if(n instanceof t){if(n.closed||n._hasParent(this))return;n._addParent(this)}(this._finalizers=(e=this._finalizers)!==null&&e!==void 0?e:[]).push(n)}}_hasParent(n){let{_parentage:e}=this;return e===n||Array.isArray(e)&&e.includes(n)}_addParent(n){let{_parentage:e}=this;this._parentage=Array.isArray(e)?(e.push(n),e):e?[e,n]:n}_removeParent(n){let{_parentage:e}=this;e===n?this._parentage=null:Array.isArray(e)&&Wa(e,n)}remove(n){let{_finalizers:e}=this;e&&Wa(e,n),n instanceof t&&n._removeParent(this)}};De.EMPTY=(()=>{let t=new De;return t.closed=!0,t})();var ap=De.EMPTY;function pc(t){return t instanceof De||t&&`closed`in t&&Te(t.remove)&&Te(t.add)&&Te(t.unsubscribe)}function nv(t){Te(t)?t():t.unsubscribe()}var Gn={onUnhandledError:null,onStoppedNotification:null,Promise:void 0,useDeprecatedSynchronousErrorHandling:!1,useDeprecatedNextContext:!1};var Qo={setTimeout(t,n,...e){let{delegate:i}=Qo;return i?.setTimeout?i.setTimeout(t,n,...e):setTimeout(t,n,...e)},clearTimeout(t){let{delegate:n}=Qo;return(n?.clearTimeout||clearTimeout)(t)},delegate:void 0};function hc(t){Qo.setTimeout(()=>{let{onUnhandledError:n}=Gn;if(n)n(t);else throw t})}function vs(){}var iv=op(`C`,void 0,void 0);function av(t){return op(`E`,void 0,t)}function ov(t){return op(`N`,t,void 0)}function op(t,n,e){return{kind:t,value:n,error:e}}var qa=null;function Xo(t){if(Gn.useDeprecatedSynchronousErrorHandling){let n=!qa;if(n&&(qa={errorThrown:!1,error:null}),t(),n){let{errorThrown:e,error:i}=qa;if(qa=null,e)throw i}}else t()}function rv(t){Gn.useDeprecatedSynchronousErrorHandling&&qa&&(qa.errorThrown=!0,qa.error=t)}var Ya=class extends De{constructor(n){super(),this.isStopped=!1,n?(this.destination=n,pc(n)&&n.add(this)):this.destination=CE}static create(n,e,i){return new Wn(n,e,i)}next(n){this.isStopped?sp(ov(n),this):this._next(n)}error(n){this.isStopped?sp(av(n),this):(this.isStopped=!0,this._error(n))}complete(){this.isStopped?sp(iv,this):(this.isStopped=!0,this._complete())}unsubscribe(){this.closed||(this.isStopped=!0,super.unsubscribe(),this.destination=null)}_next(n){this.destination.next(n)}_error(n){try{this.destination.error(n)}finally{this.unsubscribe()}}_complete(){try{this.destination.complete()}finally{this.unsubscribe()}}};var yE=Function.prototype.bind;function rp(t,n){return yE.call(t,n)}var lp=class{constructor(n){this.partialObserver=n}next(n){let{partialObserver:e}=this;if(e.next)try{e.next(n)}catch(i){fc(i)}}error(n){let{partialObserver:e}=this;if(e.error)try{e.error(n)}catch(i){fc(i)}else fc(n)}complete(){let{partialObserver:n}=this;if(n.complete)try{n.complete()}catch(e){fc(e)}}};var Wn=class extends Ya{constructor(n,e,i){super();let a;if(Te(n)||!n)a={next:n??void 0,error:e??void 0,complete:i??void 0};else{let o;this&&Gn.useDeprecatedNextContext?(o=Object.create(n),o.unsubscribe=()=>this.unsubscribe(),a={next:n.next&&rp(n.next,o),error:n.error&&rp(n.error,o),complete:n.complete&&rp(n.complete,o)}):a=n}this.destination=new lp(a)}};function fc(t){Gn.useDeprecatedSynchronousErrorHandling?rv(t):hc(t)}function xE(t){throw t}function sp(t,n){let{onStoppedNotification:e}=Gn;e&&Qo.setTimeout(()=>e(t,n))}var CE={closed:!0,next:vs,error:xE,complete:vs};var Zo=typeof Symbol==`function`&&Symbol.observable||`@@observable`;function qn(t){return t}function sv(t){return t.length===0?qn:t.length===1?t[0]:function(e){return t.reduce((i,a)=>a(i),e)}}var Ce=class t{constructor(n){n&&(this._subscribe=n)}lift(n){let e=new t;return e.source=this,e.operator=n,e}subscribe(n,e,i){let a=wE(n)?n:new Wn(n,e,i);return Xo(()=>{let{operator:o,source:r}=this;a.add(o?o.call(a,r):r?this._subscribe(a):this._trySubscribe(a))}),a}_trySubscribe(n){try{return this._subscribe(n)}catch(e){n.error(e)}}forEach(n,e){return e=lv(e),new e((i,a)=>{let o=new Wn({next:r=>{try{n(r)}catch(s){a(s),o.unsubscribe()}},error:a,complete:i});this.subscribe(o)})}_subscribe(n){var e;return(e=this.source)===null||e===void 0?void 0:e.subscribe(n)}[Zo](){return this}pipe(...n){return sv(n)(this)}toPromise(n){return n=lv(n),new n((e,i)=>{let a;this.subscribe(o=>a=o,o=>i(o),()=>e(a))})}};Ce.create=t=>new Ce(t);function lv(t){var n;return(n=t??Gn.Promise)!==null&&n!==void 0?n:Promise}function SE(t){return t&&Te(t.next)&&Te(t.error)&&Te(t.complete)}function wE(t){return t&&t instanceof Ya||SE(t)&&pc(t)}function EE(t){return Te(t?.lift)}function Oe(t){return n=>{if(EE(n))return n.lift(function(e){try{return t(e,this)}catch(i){this.error(i)}});throw new TypeError(`Unable to lift unknown Observable type`)}}function Ve(t,n,e,i,a){return new cp(t,n,e,i,a)}var cp=class extends Ya{constructor(n,e,i,a,o,r){super(n),this.onFinalize=o,this.shouldUnsubscribe=r,this._next=e?function(s){try{e(s)}catch(l){n.error(l)}}:super._next,this._error=a?function(s){try{a(s)}catch(l){n.error(l)}finally{this.unsubscribe()}}:super._error,this._complete=i?function(){try{i()}catch(s){n.error(s)}finally{this.unsubscribe()}}:super._complete}unsubscribe(){var n;if(!this.shouldUnsubscribe||this.shouldUnsubscribe()){let{closed:e}=this;super.unsubscribe(),!e&&((n=this.onFinalize)===null||n===void 0||n.call(this))}}};var cv=ma(t=>function(){t(this),this.name=`ObjectUnsubscribedError`,this.message=`object unsubscribed`});var V=class extends Ce{constructor(){super(),this.closed=!1,this.currentObservers=null,this.observers=[],this.isStopped=!1,this.hasError=!1,this.thrownError=null}lift(n){let e=new gc(this,this);return e.operator=n,e}_throwIfClosed(){if(this.closed)throw new cv}next(n){Xo(()=>{if(this._throwIfClosed(),!this.isStopped){this.currentObservers||(this.currentObservers=Array.from(this.observers));for(let e of this.currentObservers)e.next(n)}})}error(n){Xo(()=>{if(this._throwIfClosed(),!this.isStopped){this.hasError=this.isStopped=!0,this.thrownError=n;let{observers:e}=this;for(;e.length;)e.shift().error(n)}})}complete(){Xo(()=>{if(this._throwIfClosed(),!this.isStopped){this.isStopped=!0;let{observers:n}=this;for(;n.length;)n.shift().complete()}})}unsubscribe(){this.isStopped=this.closed=!0,this.observers=this.currentObservers=null}get observed(){var n;return((n=this.observers)===null||n===void 0?void 0:n.length)>0}_trySubscribe(n){return this._throwIfClosed(),super._trySubscribe(n)}_subscribe(n){return this._throwIfClosed(),this._checkFinalizedStatuses(n),this._innerSubscribe(n)}_innerSubscribe(n){let{hasError:e,isStopped:i,observers:a}=this;return e||i?ap:(this.currentObservers=null,a.push(n),new De(()=>{this.currentObservers=null,Wa(a,n)}))}_checkFinalizedStatuses(n){let{hasError:e,thrownError:i,isStopped:a}=this;e?n.error(i):a&&n.complete()}asObservable(){let n=new Ce;return n.source=this,n}};V.create=(t,n)=>new gc(t,n);var gc=class extends V{constructor(n,e){super(),this.destination=n,this.source=e}next(n){var e,i;(i=(e=this.destination)===null||e===void 0?void 0:e.next)===null||i===void 0||i.call(e,n)}error(n){var e,i;(i=(e=this.destination)===null||e===void 0?void 0:e.error)===null||i===void 0||i.call(e,n)}complete(){var n,e;(e=(n=this.destination)===null||n===void 0?void 0:n.complete)===null||e===void 0||e.call(n)}_subscribe(n){var e,i;return(i=(e=this.source)===null||e===void 0?void 0:e.subscribe(n))!==null&&i!==void 0?i:ap}};var hi=class extends V{constructor(n){super(),this._value=n}get value(){return this.getValue()}_subscribe(n){let e=super._subscribe(n);return!e.closed&&n.next(this._value),e}getValue(){let{hasError:n,thrownError:e,_value:i}=this;if(n)throw e;return this._throwIfClosed(),i}next(n){super.next(this._value=n)}};var _s={now(){return(_s.delegate||Date).now()},delegate:void 0};var Yn=class extends V{constructor(n=1/0,e=1/0,i=_s){super(),this._bufferSize=n,this._windowTime=e,this._timestampProvider=i,this._buffer=[],this._infiniteTimeWindow=!0,this._infiniteTimeWindow=e===1/0,this._bufferSize=Math.max(1,n),this._windowTime=Math.max(1,e)}next(n){let{isStopped:e,_buffer:i,_infiniteTimeWindow:a,_timestampProvider:o,_windowTime:r}=this;e||(i.push(n),!a&&i.push(o.now()+r)),this._trimBuffer(),super.next(n)}_subscribe(n){this._throwIfClosed(),this._trimBuffer();let e=this._innerSubscribe(n),{_infiniteTimeWindow:i,_buffer:a}=this,o=a.slice();for(let r=0;r<o.length&&!n.closed;r+=i?1:2)n.next(o[r]);return this._checkFinalizedStatuses(n),e}_trimBuffer(){let{_bufferSize:n,_timestampProvider:e,_buffer:i,_infiniteTimeWindow:a}=this,o=(a?1:2)*n;if(n<1/0&&o<i.length&&i.splice(0,i.length-o),!a){let r=e.now(),s=0;for(let l=1;l<i.length&&i[l]<=r;l+=2)s=l;s&&i.splice(0,s+1)}}};var bc=class extends De{constructor(n,e){super()}schedule(n,e=0){return this}};var ys={setInterval(t,n,...e){let{delegate:i}=ys;return i?.setInterval?i.setInterval(t,n,...e):setInterval(t,n,...e)},clearInterval(t){let{delegate:n}=ys;return(n?.clearInterval||clearInterval)(t)},delegate:void 0};var vc=class extends bc{constructor(n,e){super(n,e),this.scheduler=n,this.work=e,this.pending=!1}schedule(n,e=0){var i;if(this.closed)return this;this.state=n;let a=this.id,o=this.scheduler;return a!=null&&(this.id=this.recycleAsyncId(o,a,e)),this.pending=!0,this.delay=e,this.id=(i=this.id)!==null&&i!==void 0?i:this.requestAsyncId(o,this.id,e),this}requestAsyncId(n,e,i=0){return ys.setInterval(n.flush.bind(n,this),i)}recycleAsyncId(n,e,i=0){if(i!=null&&this.delay===i&&this.pending===!1)return e;e!=null&&ys.clearInterval(e)}execute(n,e){if(this.closed)return new Error(`executing a cancelled action`);this.pending=!1;let i=this._execute(n,e);if(i)return i;this.pending===!1&&this.id!=null&&(this.id=this.recycleAsyncId(this.scheduler,this.id,null))}_execute(n,e){let i=!1,a;try{this.work(n)}catch(o){i=!0,a=o||new Error(`Scheduled action threw falsy error`)}if(i)return this.unsubscribe(),a}unsubscribe(){if(!this.closed){let{id:n,scheduler:e}=this,{actions:i}=e;this.work=this.state=this.scheduler=null,this.pending=!1,Wa(i,this),n!=null&&(this.id=this.recycleAsyncId(e,n,null)),this.delay=null,super.unsubscribe()}}};var Jo=class t{constructor(n,e=t.now){this.schedulerActionCtor=n,this.now=e}schedule(n,e=0,i){return new this.schedulerActionCtor(this,n).schedule(i,e)}};Jo.now=_s.now;var _c=class extends Jo{constructor(n,e=Jo.now){super(n,e),this.actions=[],this._active=!1}flush(n){let{actions:e}=this;if(this._active){e.push(n);return}let i;this._active=!0;do if(i=n.execute(n.state,n.delay))break;while(n=e.shift());if(this._active=!1,i){for(;n=e.shift();)n.unsubscribe();throw i}}};var Ka=new _c(vc);var dv=Ka;var Oi=new Ce(t=>t.complete());function yc(t){return t&&Te(t.schedule)}function dp(t){return t[t.length-1]}function xc(t){return Te(dp(t))?t.pop():void 0}function fi(t){return yc(dp(t))?t.pop():void 0}function uv(t,n){return typeof dp(t)==`number`?t.pop():n}function pv(t,n,e,i){function a(o){return o instanceof e?o:new e(function(r){r(o)})}return new(e||(e=Promise))(function(o,r){function s(p){try{h(i.next(p))}catch(g){r(g)}}function l(p){try{h(i.throw(p))}catch(g){r(g)}}function h(p){p.done?o(p.value):a(p.value).then(s,l)}h((i=i.apply(t,n||[])).next())})}function mv(t){var n=typeof Symbol==`function`&&Symbol.iterator,e=n&&t[n],i=0;if(e)return e.call(t);if(t&&typeof t.length==`number`)return{next:function(){return t&&i>=t.length&&(t=void 0),{value:t&&t[i++],done:!t}}};throw new TypeError(n?`Object is not iterable.`:`Symbol.iterator is not defined.`)}function Qa(t){return this instanceof Qa?(this.v=t,this):new Qa(t)}function hv(t,n,e){if(!Symbol.asyncIterator)throw new TypeError(`Symbol.asyncIterator is not defined.`);var i=e.apply(t,n||[]),a,o=[];return a=Object.create((typeof AsyncIterator==`function`?AsyncIterator:Object).prototype),s(`next`),s(`throw`),s(`return`,r),a[Symbol.asyncIterator]=function(){return this},a;function r(b){return function(y){return Promise.resolve(y).then(b,g)}}function s(b,y){i[b]&&(a[b]=function(I){return new Promise(function(P,ie){o.push([b,I,P,ie])>1||l(b,I)})},y&&(a[b]=y(a[b])))}function l(b,y){try{h(i[b](y))}catch(I){_(o[0][3],I)}}function h(b){b.value instanceof Qa?Promise.resolve(b.value.v).then(p,g):_(o[0][2],b)}function p(b){l(`next`,b)}function g(b){l(`throw`,b)}function _(b,y){b(y),o.shift(),o.length&&l(o[0][0],o[0][1])}}function fv(t){if(!Symbol.asyncIterator)throw new TypeError(`Symbol.asyncIterator is not defined.`);var n=t[Symbol.asyncIterator],e;return n?n.call(t):(t=typeof mv==`function`?mv(t):t[Symbol.iterator](),e={},i(`next`),i(`throw`),i(`return`),e[Symbol.asyncIterator]=function(){return this},e);function i(o){e[o]=t[o]&&function(r){return new Promise(function(s,l){r=t[o](r),a(s,l,r.done,r.value)})}}function a(o,r,s,l){Promise.resolve(l).then(function(h){o({value:h,done:s})},r)}}var Cc=(t=>t&&typeof t.length==`number`&&typeof t!=`function`);function Sc(t){return Te(t?.then)}function wc(t){return Te(t[Zo])}function Ec(t){return Symbol.asyncIterator&&Te(t?.[Symbol.asyncIterator])}function Dc(t){return new TypeError(`You provided ${t!==null&&typeof t==`object`?`an invalid object`:`'${t}'`} where a stream was expected. You can provide an Observable, Promise, ReadableStream, Array, AsyncIterable, or Iterable.`)}function DE(){return typeof Symbol!=`function`||!Symbol.iterator?`@@iterator`:Symbol.iterator}var Ic=DE();function kc(t){return Te(t?.[Ic])}function Mc(t){return hv(this,arguments,function*(){let e=t.getReader();try{for(;;){let{value:i,done:a}=yield Qa(e.read());if(a)return yield Qa(void 0);yield yield Qa(i)}}finally{e.releaseLock()}})}function Tc(t){return Te(t?.getReader)}function ot(t){if(t instanceof Ce)return t;if(t!=null){if(wc(t))return IE(t);if(Cc(t))return kE(t);if(Sc(t))return ME(t);if(Ec(t))return gv(t);if(kc(t))return TE(t);if(Tc(t))return NE(t)}throw Dc(t)}function IE(t){return new Ce(n=>{let e=t[Zo]();if(Te(e.subscribe))return e.subscribe(n);throw new TypeError(`Provided object does not correctly implement Symbol.observable`)})}function kE(t){return new Ce(n=>{for(let e=0;e<t.length&&!n.closed;e++)n.next(t[e]);n.complete()})}function ME(t){return new Ce(n=>{t.then(e=>{n.closed||(n.next(e),n.complete())},e=>n.error(e)).then(null,hc)})}function TE(t){return new Ce(n=>{for(let e of t)if(n.next(e),n.closed)return;n.complete()})}function gv(t){return new Ce(n=>{AE(t,n).catch(e=>n.error(e))})}function NE(t){return gv(Mc(t))}function AE(t,n){var e,i,a,o;return pv(this,void 0,void 0,function*(){try{for(e=fv(t);i=yield e.next(),!i.done;){let r=i.value;if(n.next(r),n.closed)return}}catch(r){a={error:r}}finally{try{i&&!i.done&&(o=e.return)&&(yield o.call(e))}finally{if(a)throw a.error}}n.complete()})}function Kt(t,n,e,i=0,a=!1){let o=n.schedule(function(){e(),a?t.add(this.schedule(null,i)):this.unsubscribe()},i);if(t.add(o),!a)return o}function Nc(t,n=0){return Oe((e,i)=>{e.subscribe(Ve(i,a=>Kt(i,t,()=>i.next(a),n),()=>Kt(i,t,()=>i.complete(),n),a=>Kt(i,t,()=>i.error(a),n)))})}function Ac(t,n=0){return Oe((e,i)=>{i.add(t.schedule(()=>e.subscribe(i),n))})}function bv(t,n){return ot(t).pipe(Ac(n),Nc(n))}function vv(t,n){return ot(t).pipe(Ac(n),Nc(n))}function _v(t,n){return new Ce(e=>{let i=0;return n.schedule(function(){i===t.length?e.complete():(e.next(t[i++]),e.closed||this.schedule())})})}function yv(t,n){return new Ce(e=>{let i;return Kt(e,n,()=>{i=t[Ic](),Kt(e,n,()=>{let a,o;try{({value:a,done:o}=i.next())}catch(r){e.error(r);return}o?e.complete():e.next(a)},0,!0)}),()=>Te(i?.return)&&i.return()})}function Rc(t,n){if(!t)throw new Error(`Iterable cannot be null`);return new Ce(e=>{Kt(e,n,()=>{let i=t[Symbol.asyncIterator]();Kt(e,n,()=>{i.next().then(a=>{a.done?e.complete():e.next(a.value)})},0,!0)})})}function xv(t,n){return Rc(Mc(t),n)}function Cv(t,n){if(t!=null){if(wc(t))return bv(t,n);if(Cc(t))return _v(t,n);if(Sc(t))return vv(t,n);if(Ec(t))return Rc(t,n);if(kc(t))return yv(t,n);if(Tc(t))return xv(t,n)}throw Dc(t)}function kn(t,n){return n?Cv(t,n):ot(t)}function Mn(...t){return kn(t,fi(t))}var Sv=ma(t=>function(){t(this),this.name=`EmptyError`,this.message=`no elements in sequence`});function wt(t,n){let e=typeof n==`object`;return new Promise((i,a)=>{let o=new Wn({next:r=>{i(r),o.unsubscribe()},error:a,complete:()=>{e?i(n.defaultValue):a(new Sv)}});t.subscribe(o)})}function Oc(t){return t instanceof Date&&!isNaN(t)}var RE=ma(t=>function(e=null){t(this),this.message=`Timeout has occurred`,this.name=`TimeoutError`,this.info=e});function up(t,n){let{first:e,each:i,with:a=OE,scheduler:o=n??Ka,meta:r=null}=Oc(t)?{first:t}:typeof t==`number`?{each:t}:t;if(e==null&&i==null)throw new TypeError(`No timeout provided.`);return Oe((s,l)=>{let h,p,g=null,_=0,b=y=>{p=Kt(l,o,()=>{try{h.unsubscribe(),ot(a({meta:r,lastValue:g,seen:_})).subscribe(l)}catch(I){l.error(I)}},y)};h=s.subscribe(Ve(l,y=>{p?.unsubscribe(),_++,l.next(g=y),i>0&&b(i)},void 0,void 0,()=>{p?.closed||p?.unsubscribe(),g=null})),!_&&b(e!=null?typeof e==`number`?e:+e-o.now():i)})}function OE(t){throw new RE(t)}function lt(t,n){return Oe((e,i)=>{let a=0;e.subscribe(Ve(i,o=>{i.next(t.call(n,o,a++))}))})}var{isArray:PE}=Array;function FE(t,n){return PE(n)?t(...n):t(n)}function Pc(t){return lt(n=>FE(t,n))}var{isArray:LE}=Array,{getPrototypeOf:VE,prototype:BE,keys:jE}=Object;function Fc(t){if(t.length===1){let n=t[0];if(LE(n))return{args:n,keys:null};if(zE(n)){let e=jE(n);return{args:e.map(i=>n[i]),keys:e}}}return{args:t,keys:null}}function zE(t){return t&&typeof t==`object`&&VE(t)===BE}function Lc(t,n){return t.reduce((e,i,a)=>(e[i]=n[a],e),{})}function mp(...t){let n=fi(t),e=xc(t),{args:i,keys:a}=Fc(t);if(i.length===0)return kn([],n);let o=new Ce(HE(i,n,a?r=>Lc(a,r):qn));return e?o.pipe(Pc(e)):o}function HE(t,n,e=qn){return i=>{wv(n,()=>{let{length:a}=t,o=new Array(a),r=a,s=a;for(let l=0;l<a;l++)wv(n,()=>{let h=kn(t[l],n),p=!1;h.subscribe(Ve(i,g=>{o[l]=g,p||(p=!0,s--),s||i.next(e(o.slice()))},()=>{--r||i.complete()}))},i)},i)}}function wv(t,n,e){t?Kt(e,t,n):n()}function Ev(t,n,e,i,a,o,r,s){let l=[],h=0,p=0,g=!1,_=()=>{g&&!l.length&&!h&&n.complete()},b=I=>h<i?y(I):l.push(I),y=I=>{o&&n.next(I),h++;let P=!1;ot(e(I,p++)).subscribe(Ve(n,ie=>{a?.(ie),o?b(ie):n.next(ie)},()=>{P=!0},void 0,()=>{if(P)try{for(h--;l.length&&h<i;){let ie=l.shift();r?Kt(n,r,()=>y(ie)):y(ie)}_()}catch(ie){n.error(ie)}}))};return t.subscribe(Ve(n,b,()=>{g=!0,_()})),()=>{s?.()}}function er(t,n,e=1/0){return Te(n)?er((i,a)=>lt((o,r)=>n(i,o,a,r))(ot(t(i,a))),e):(typeof n==`number`&&(e=n),Oe((i,a)=>Ev(i,a,t,e)))}function Vc(t=1/0){return er(qn,t)}function Dv(){return Vc(1)}function tr(...t){return Dv()(kn(t,fi(t)))}function Xa(t){return new Ce(n=>{ot(t()).subscribe(n)})}function pp(...t){let n=xc(t),{args:e,keys:i}=Fc(t),a=new Ce(o=>{let{length:r}=e;if(!r){o.complete();return}let s=new Array(r),l=r,h=r;for(let p=0;p<r;p++){let g=!1;ot(e[p]).subscribe(Ve(o,_=>{g||(g=!0,h--),s[p]=_},()=>l--,void 0,()=>{(!l||!g)&&(h||o.next(i?Lc(i,s):s),o.complete())}))}});return n?a.pipe(Pc(n)):a}function xs(t=0,n,e=dv){let i=-1;return n!=null&&(yc(n)?e=n:i=n),new Ce(a=>{let o=Oc(t)?+t-e.now():t;o<0&&(o=0);let r=0;return e.schedule(function(){a.closed||(a.next(r++),0<=i?this.schedule(void 0,i):a.complete())},o)})}function kt(...t){let n=fi(t),e=uv(t,1/0),i=t;return i.length?i.length===1?ot(i[0]):Vc(e)(kn(i,n)):Oi}function Qe(t,n){return Oe((e,i)=>{let a=0;e.subscribe(Ve(i,o=>t.call(n,o,a++)&&i.next(o)))})}function Iv(t){return Oe((n,e)=>{let i=!1,a=null,o=null,r=!1,s=()=>{if(o?.unsubscribe(),o=null,i){i=!1;let h=a;a=null,e.next(h)}r&&e.complete()},l=()=>{o=null,r&&e.complete()};n.subscribe(Ve(e,h=>{i=!0,a=h,o||ot(t(h)).subscribe(o=Ve(e,s,l))},()=>{r=!0,(!i||!o||o.closed)&&e.complete()}))})}function Bc(t,n=Ka){return Iv(()=>xs(t,n))}function hp(t,n){return Te(n)?er(t,n,1):er(t,1)}function Pi(t,n=Ka){return Oe((e,i)=>{let a=null,o=null,r=null,s=()=>{if(a){a.unsubscribe(),a=null;let h=o;o=null,i.next(h)}};function l(){let h=r+t,p=n.now();if(p<h){a=this.schedule(void 0,h-p),i.add(a);return}s()}e.subscribe(Ve(i,h=>{o=h,r=n.now(),a||(a=n.schedule(l,t),i.add(a))},()=>{s(),i.complete()},void 0,()=>{o=a=null}))})}function vn(t){return t<=0?()=>Oi:Oe((n,e)=>{let i=0;n.subscribe(Ve(e,a=>{++i<=t&&(e.next(a),t<=i&&e.complete())}))})}function jc(t,n=qn){return t=t??UE,Oe((e,i)=>{let a,o=!0;e.subscribe(Ve(i,r=>{let s=n(r);(o||!t(a,s))&&(o=!1,a=s,i.next(r))}))})}function UE(t,n){return t===n}function fp(t){return Oe((n,e)=>{try{n.subscribe(e)}finally{e.add(t)}})}function zc(){return Oe((t,n)=>{let e,i=!1;t.subscribe(Ve(n,a=>{let o=e;e=a,i&&n.next([o,a]),i=!0}))})}function kv(t={}){let{connector:n=()=>new V,resetOnError:e=!0,resetOnComplete:i=!0,resetOnRefCountZero:a=!0}=t;return o=>{let r,s,l,h=0,p=!1,g=!1,_=()=>{s?.unsubscribe(),s=void 0},b=()=>{_(),r=l=void 0,p=g=!1},y=()=>{let I=r;b(),I?.unsubscribe()};return Oe((I,P)=>{h++,!g&&!p&&_();let ie=l=l??n();P.add(()=>{h--,h===0&&!g&&!p&&(s=gp(y,a))}),ie.subscribe(P),!r&&h>0&&(r=new Wn({next:L=>ie.next(L),error:L=>{g=!0,_(),s=gp(b,e,L),ie.error(L)},complete:()=>{p=!0,_(),s=gp(b,i),ie.complete()}}),ot(I).subscribe(r))})(o)}}function gp(t,n,...e){if(n===!0){t();return}if(n===!1)return;let i=new Wn({next:()=>{i.unsubscribe(),t()}});return ot(n(...e)).subscribe(i)}function Hc(t,n,e){let i,a=!1;return t&&typeof t==`object`?{bufferSize:i=1/0,windowTime:n=1/0,refCount:a=!1,scheduler:e}=t:i=t??1/0,kv({connector:()=>new Yn(i,n,e),resetOnError:!0,resetOnComplete:!1,resetOnRefCountZero:a})}function Za(t){return Qe((n,e)=>t<=e)}function mt(...t){let n=fi(t);return Oe((e,i)=>{(n?tr(t,e,n):tr(t,e)).subscribe(i)})}function _n(t,n){return Oe((e,i)=>{let a=null,o=0,r=!1,s=()=>r&&!a&&i.complete();e.subscribe(Ve(i,l=>{a?.unsubscribe();let h=0,p=o++;ot(t(l,p)).subscribe(a=Ve(i,g=>i.next(n?n(l,g,p,h++):g),()=>{a=null,s()}))},()=>{r=!0,s()}))})}function Ne(t){return Oe((n,e)=>{ot(t).subscribe(Ve(e,()=>e.complete(),vs)),!e.closed&&n.subscribe(e)})}function bp(t,n=!1){return Oe((e,i)=>{let a=0;e.subscribe(Ve(i,o=>{let r=t(o,a++);(r||n)&&i.next(o),!r&&i.complete()}))})}function vp(t,n,e){let i=Te(t)||n||e?{next:t,error:n,complete:e}:t;return i?Oe((a,o)=>{var r;(r=i.subscribe)===null||r===void 0||r.call(i);let s=!0;a.subscribe(Ve(o,l=>{var h;(h=i.next)===null||h===void 0||h.call(i,l),o.next(l)},()=>{var l;s=!1,(l=i.complete)===null||l===void 0||l.call(i),o.complete()},l=>{var h;s=!1,(h=i.error)===null||h===void 0||h.call(i,l),o.error(l)},()=>{var l,h;s&&((l=i.unsubscribe)===null||l===void 0||l.call(i)),(h=i.finalize)===null||h===void 0||h.call(i)}))}):qn}var Kc=`https://angular.dev/best-practices/security#preventing-cross-site-scripting-xss`;var q=class extends Error{code;constructor(n,e){super(Bi(n,e)),this.code=n}};function $E(t){return`NG0${Math.abs(t)}`}function Bi(t,n){return`${$E(t)}${n?`: `+n:``}`}function Ye(t){for(let n in t)if(t[n]===Ye)return n;throw Error(``)}function Rv(t,n){for(let e in n)Object.hasOwn(n,e)&&!Object.hasOwn(t,e)&&(t[e]=n[e])}function Is(t){if(typeof t==`string`)return t;if(Array.isArray(t))return`[${t.map(Is).join(`, `)}]`;if(t==null)return``+t;let n=t.overriddenName||t.name;if(n)return`${n}`;let e=t.toString();if(e==null)return``+e;let i=e.indexOf(`
`);return i>=0?e.slice(0,i):e}function Qc(t,n){return t?n?`${t} ${n}`:t:n||``}var GE=Ye({__forward_ref__:Ye});function Ht(t){return t.__forward_ref__=Ht,t}function Lt(t){return Ap(t)?t():t}function Ap(t){return typeof t==`function`&&Object.hasOwn(t,GE)&&t.__forward_ref__===Ht}function be(t){return{token:t.token,providedIn:t.providedIn||null,factory:t.factory,value:void 0}}function oe(t){return{providers:t.providers||[],imports:t.imports||[]}}function Xc(t){return WE(t,Zc)}function WE(t,n){return Object.hasOwn(t,n)&&t[n]||null}function qE(t){return(t?.[Zc]??null)||null}function yp(t){return t&&Object.hasOwn(t,$c)?t[$c]:null}var Zc=Ye({ɵprov:Ye});var $c=Ye({ɵinj:Ye});var N=class{_desc;ngMetadataName=`InjectionToken`;ɵprov;constructor(n,e){this._desc=n,this.ɵprov=void 0,typeof e==`number`?this.__NG_ELEMENT_ID__=e:e!==void 0&&(this.ɵprov=be({token:this,providedIn:e.providedIn||`root`,factory:e.factory}))}get multi(){return this}toString(){return`InjectionToken ${this._desc}`}};function Rp(t){return t&&!!t.ɵproviders}var ks=Ye({ɵcmp:Ye});var Ms=Ye({ɵdir:Ye});var Op=Ye({ɵpipe:Ye});var Pp=Ye({ɵmod:Ye});var Ss=Ye({ɵfac:Ye});var io=Ye({__NG_ELEMENT_ID__:Ye});var Mv=Ye({__NG_ENV_ID__:Ye});function Ov(t){return Jc(t,`@NgModule`),t[Pp]||null}function ha(t){return Jc(t,`@Component`),t[ks]||null}function Ts(t){return Jc(t,`@Directive`),t[Ms]||null}function Pv(t){return Jc(t,`@Pipe`),t[Op]||null}function Jc(t,n){if(t==null)throw new q(-919,!1)}function $e(t){return typeof t==`string`?t:t==null?``:String(t)}var Fv=Ye({ngErrorCode:Ye});var YE=Ye({ngErrorMessage:Ye});var KE=Ye({ngTokenPath:Ye});function Fp(t,n){return Lv(``,-200,n)}function ed(t,n){throw new q(-201,!1)}function Lv(t,n,e){let i=new q(n,t);return i[Fv]=n,i[YE]=t,e&&(i[KE]=e),i}function QE(t){return t[Fv]}var xp;function Vv(){return xp}function sn(t){let n=xp;return xp=t,n}function Lp(t,n,e){let i=Xc(t);if(i&&i.providedIn==`root`)return i.value===void 0?i.value=i.factory():i.value;if(e&8)return null;if(n!==void 0)return n;ed(t,``)}var bi=globalThis;var Ja={};var ZE=`__NG_DI_FLAG__`;var Cp=class{injector;constructor(n){this.injector=n}retrieve(n,e){let i=eo(e)||0;try{return this.injector.get(n,i&8?null:Ja,i)}catch(a){if(Ko(a))return a;throw a}}};function JE(t,n=0){let e=uc();if(e===void 0)throw new q(-203,!1);if(e===null)return Lp(t,void 0,n);{let i=eD(n),a=e.retrieve(t,i);if(Ko(a)){if(i.optional)return null;throw a}return a}}function Se(t,n=0){return(Vv()||JE)(Lt(t),n)}function f(t,n){return Se(t,eo(n))}function eo(t){return typeof t>`u`||typeof t==`number`?t:0|(t.optional&&8)|(t.host&&1)|(t.self&&2)|(t.skipSelf&&4)}function eD(t){return{optional:!!(t&8),host:!!(t&1),self:!!(t&2),skipSelf:!!(t&4)}}function Sp(t){let n=[];for(let e=0;e<t.length;e++){let i=Lt(t[e]);if(Array.isArray(i)){if(i.length===0)throw new q(900,!1);let a,o=0;for(let r=0;r<i.length;r++){let s=i[r],l=tD(s);typeof l==`number`?l===-1?a=s.token:o|=l:a=s}n.push(Se(a,o))}else n.push(Se(i))}return n}function tD(t){return t[ZE]}function pa(t,n){return Object.hasOwn(t,Ss)?t[Ss]:null}function Bv(t,n,e){if(t.length!==n.length)return!1;for(let i=0;i<t.length;i++){let a=t[i],o=n[i];if(e&&(a=e(a),o=e(o)),o!==a)return!1}return!0}function jv(t){return t.flat(Number.POSITIVE_INFINITY)}function td(t,n){t.forEach(e=>Array.isArray(e)?td(e,n):n(e))}function Vp(t,n,e){n>=t.length?t.push(e):t.splice(n,0,e)}function Ns(t,n){return n>=t.length-1?t.pop():t.splice(n,1)[0]}function zv(t,n){let e=[];for(let i=0;i<t;i++)e.push(n);return e}function Hv(t,n,e,i){let a=t.length;if(a==n)t.push(e,i);else if(a===1)t.push(i,t[0]),t[0]=e;else{for(a--,t.push(t[a-1],t[a]);a>n;){let o=a-2;t[a]=t[o],a--}t[n]=e,t[n+1]=i}}function nd(t,n,e){let i=ar(t,n);return i>=0?t[i|1]=e:(i=~i,Hv(t,i,n,e)),i}function id(t,n){let e=ar(t,n);if(e>=0)return t[e|1]}function ar(t,n){return nD(t,n,1)}function nD(t,n,e){let i=0,a=t.length>>e;for(;a!==i;){let o=i+(a-i>>1),r=t[o<<e];if(n===r)return o<<e;r>n?a=o:i=o+1}return~(a<<e)}var fa={};var Qt=[];var ao=new N(``);var ad=new N(``,-1);var Bp=new N(``);var ir=class{get(n,e=Ja){if(e===Ja){let a=Lv(``,-201);throw a.name=`ɵNotFound`,a}return e}};function ln(t){return{ɵproviders:t}}function Uv(t){return ln([{provide:ao,multi:!0,useValue:t}])}function $v(...t){return{ɵproviders:jp(!0,t),ɵfromNgModule:!0}}function jp(t,...n){let e=[],i=new Set,a,o=r=>{e.push(r)};return td(n,r=>{let s=r;Gc(s,o,[],i)&&(a||=[],a.push(s))}),a!==void 0&&Gv(a,o),e}function Gv(t,n){for(let e=0;e<t.length;e++){let{ngModule:i,providers:a}=t[e];zp(a,o=>{n(o,i)})}}function Gc(t,n,e,i){if(t=Lt(t),!t)return!1;let a=null,o=yp(t),r=!o&&ha(t);if(!o&&!r){let l=t.ngModule;if(o=yp(l),o)a=l;else return!1}else{if(r&&!r.standalone)return!1;a=t}let s=i.has(a);if(r){if(s)return!1;if(i.add(a),r.dependencies){let l=typeof r.dependencies==`function`?r.dependencies():r.dependencies;for(let h of l)Gc(h,n,e,i)}}else if(o){if(o.imports!=null&&!s){i.add(a);let h;td(o.imports,p=>{Gc(p,n,e,i)&&(h||=[],h.push(p))}),h!==void 0&&Gv(h,n)}if(!s){let h=pa(a)||(()=>new a);n({provide:a,useFactory:h,deps:Qt},a),n({provide:Bp,useValue:a,multi:!0},a),n({provide:ao,useValue:()=>Se(a),multi:!0},a)}let l=o.providers;if(l!=null&&!s){let h=t;zp(l,p=>{n(p,h)})}}else return!1;return a!==t&&t.providers!==void 0}function zp(t,n){for(let e of t)Rp(e)&&(e=e.ɵproviders),Array.isArray(e)?zp(e,n):n(e)}var iD=Ye({provide:String,useValue:Ye});function Wv(t){return t!==null&&typeof t==`object`&&iD in t}function aD(t){return!!(t&&t.useExisting)}function oD(t){return!!(t&&t.useFactory)}function to(t){return typeof t==`function`}function qv(t){return!!t.useClass}var As=new N(``);var Uc={};var Tv={};var _p;function or(){return _p===void 0&&(_p=new ir),_p}var Mt=class{};var no=class extends Mt{parent;source;scopes;records=new Map;_ngOnDestroyHooks=new Set;_onDestroyHooks=[];get destroyed(){return this._destroyed}_destroyed=!1;injectorDefTypes;constructor(n,e,i,a){super(),this.parent=e,this.source=i,this.scopes=a,Ep(n,r=>this.processProvider(r)),this.records.set(ad,nr(void 0,this)),a.has(`environment`)&&this.records.set(Mt,nr(void 0,this));let o=this.records.get(As);o!=null&&typeof o.value==`string`&&this.scopes.add(o.value),this.injectorDefTypes=new Set(this.get(Bp,Qt,{self:!0}))}retrieve(n,e){let i=eo(e)||0;try{return this.get(n,Ja,i)}catch(a){if(Ko(a))return a;throw a}}destroy(){Cs(this),this._destroyed=!0;let n=ge(null);try{for(let i of this._ngOnDestroyHooks)i.ngOnDestroy();let e=this._onDestroyHooks;this._onDestroyHooks=[];for(let i of e)i()}finally{this.records.clear(),this._ngOnDestroyHooks.clear(),this.injectorDefTypes.clear(),ge(n)}}onDestroy(n){return Cs(this),this._onDestroyHooks.push(n),()=>this.removeOnDestroy(n)}runInContext(n){Cs(this);let e=pi(this),i=sn(void 0);try{return n()}finally{pi(e),sn(i)}}get(n,e=Ja,i){if(Cs(this),Object.hasOwn(n,Mv))return n[Mv](this);let a=eo(i),r=pi(this),s=sn(void 0);try{if(!(a&4)){let h=this.records.get(n);if(h===void 0){let p=dD(n)&&Xc(n);p&&this.injectableDefInScope(p)?h=nr(wp(n),Uc):h=null,this.records.set(n,h)}if(h!=null)return this.hydrate(n,h,a)}let l=a&2?or():this.parent;return e=a&8&&e===Ja?null:e,l.get(n,e)}catch(l){let h=QE(l);throw h===-200||h===-201?new q(h,null):l}finally{sn(s),pi(r)}}resolveInjectorInitializers(){let n=ge(null),e=pi(this),i=sn(void 0);try{let o=this.get(ao,Qt,{self:!0});for(let r of o)r()}finally{pi(e),sn(i),ge(n)}}toString(){return`R3Injector[...]`}processProvider(n){n=Lt(n);let e=to(n)?n:Lt(n&&n.provide),i=sD(n);if(!to(n)&&n.multi===!0){let a=this.records.get(e);a||(a=nr(void 0,Uc,!0),a.factory=()=>Sp(a.multi),this.records.set(e,a)),e=n,a.multi.push(n)}this.records.set(e,i)}hydrate(n,e,i){let a=ge(null);try{if(e.value===Tv)throw Fp(``);return e.value===Uc&&(e.value=Tv,e.value=e.factory(void 0,i)),typeof e.value==`object`&&e.value&&cD(e.value)&&this._ngOnDestroyHooks.add(e.value),e.value}finally{ge(a)}}injectableDefInScope(n){if(!n.providedIn)return!1;let e=Lt(n.providedIn);return typeof e==`string`?e===`any`||this.scopes.has(e):this.injectorDefTypes.has(e)}removeOnDestroy(n){let e=this._onDestroyHooks.indexOf(n);e!==-1&&this._onDestroyHooks.splice(e,1)}};function wp(t){let n=Xc(t),e=n!==null?n.factory:pa(t);if(e!==null)return e;if(t instanceof N)throw new q(-204,!1);if(t instanceof Function)return rD(t);throw new q(-204,!1)}function rD(t){if(t.length>0)throw new q(-204,!1);let e=qE(t);return e!==null?()=>e.factory(t):()=>new t}function sD(t){if(Wv(t))return nr(void 0,t.useValue);return nr(Hp(t),Uc)}function Hp(t,n,e){let i;if(to(t)){let a=Lt(t);return pa(a)||wp(a)}else if(Wv(t))i=()=>Lt(t.useValue);else if(oD(t))i=()=>t.useFactory(...Sp(t.deps||[]));else if(aD(t))i=(a,o)=>Se(Lt(t.useExisting),o!==void 0&&o&8?8:void 0);else{let a=Lt(t&&(t.useClass||t.provide));if(lD(t))i=()=>new a(...Sp(t.deps));else return pa(a)||wp(a)}return i}function Cs(t){if(t.destroyed)throw new q(-205,!1)}function nr(t,n,e=!1){return{factory:t,value:n,multi:e?[]:void 0}}function lD(t){return!!t.deps}function cD(t){return t!==null&&typeof t==`object`&&typeof t.ngOnDestroy==`function`}function dD(t){return typeof t==`function`||typeof t==`object`&&t.ngMetadataName===`InjectionToken`}function Ep(t,n){for(let e of t)Array.isArray(e)?Ep(e,n):e&&Rp(e)?Ep(e.ɵproviders,n):n(e)}function rr(t,n){let e;t instanceof no?(Cs(t),e=t):e=new Cp(t);let a=pi(e),o=sn(void 0);try{return n()}finally{pi(a),sn(o)}}function Yv(){return Vv()!==void 0||uc()!=null}var Kn=0;var ve=1;var xe=2;var Et=3;var Nn=4;var Ut=5;var oo=6;var sr=7;var yt=8;var ji=9;var Qn=10;var et=11;var lr=12;var Up=13;var ga=14;var Xt=15;var ba=16;var ro=17;var vi=18;var Rs=19;var $p=20;var Fi=21;var od=22;var Li=23;var yn=24;var so=25;var Kv=26;var cr=27;var rt=28;var Qv=1;var Gp=6;var lo=7;var Os=8;var co=9;var xt=10;function zi(t){return Array.isArray(t)&&typeof t[Qv]==`object`}function An(t){return Array.isArray(t)&&t[Qv]===!0}function Wp(t){return(t.flags&4)!==0}function _i(t){return t.componentOffset>-1}function dr(t){return(t.flags&1)===1}function Xn(t){return!!t.template}function ur(t){return(t[xe]&512)!==0}function uo(t){return(t[xe]&256)===256}var xn=(function(t){return t[t.NONE=0]=`NONE`,t[t.HTML=1]=`HTML`,t[t.STYLE=2]=`STYLE`,t[t.SCRIPT=3]=`SCRIPT`,t[t.URL=4]=`URL`,t[t.RESOURCE_URL=5]=`RESOURCE_URL`,t[t.ATTRIBUTE_NO_BINDING=6]=`ATTRIBUTE_NO_BINDING`,t})(xn||{});var qp=`svg`;var Xv=`math`;function $t(t){for(;Array.isArray(t);)t=t[Kn];return t}function Yp(t,n){return $t(n[t])}function Cn(t,n){return $t(n[t.index])}function rd(t,n){return t.data[n]}function Ps(t,n){return t[n]}function Fs(t,n,e,i){e>=t.data.length&&(t.data[e]=null,t.blueprint[e]=null),n[e]=i}function Rn(t,n){let e=n[t];return zi(e)?e:e[Kn]}function Zv(t){return(t[xe]&4)===4}function sd(t){return(t[xe]&128)===128}function Jv(t){return An(t[Et])}function Sn(t,n){return n==null?null:t[n]}function Kp(t){t[ro]=0}function Qp(t){t[xe]&1024||(t[xe]|=1024,sd(t)&&mo(t))}function e_(t,n){for(;t>0;)n=n[ga],t--;return n}function Ls(t){return!!(t[xe]&9216||t[yn]?.dirty)}function ld(t){t[Qn].changeDetectionScheduler?.notify(8),t[xe]&64&&(t[xe]|=1024),Ls(t)&&mo(t)}function mo(t){t[Qn].changeDetectionScheduler?.notify(0);let n=Vi(t);for(;n!==null&&!(n[xe]&8192||(n[xe]|=8192,!sd(n)));)n=Vi(n)}function cd(t,n){if(uo(t))throw new q(911,!1);t[Fi]===null&&(t[Fi]=[]),t[Fi].push(n)}function t_(t,n){if(t[Fi]===null)return;let e=t[Fi].indexOf(n);e!==-1&&t[Fi].splice(e,1)}function Vi(t){let n=t[Et];return An(n)?n[Et]:n}function Xp(t){return t[sr]??=[]}function Zp(t){return t.cleanup??=[]}function n_(t,n,e,i){let a=Xp(n);a.push(e),t.firstCreatePass&&Zp(t).push(i,a.length-1)}var Ee={lFrame:m_(null),bindingsEnabled:!0,skipHydrationRootTNode:null};var Dp=!1;function i_(){return Ee.lFrame.elementDepthCount}function a_(){Ee.lFrame.elementDepthCount++}function Jp(){Ee.lFrame.elementDepthCount--}function dd(){return Ee.bindingsEnabled}function eh(){return Ee.skipHydrationRootTNode!==null}function th(t){return Ee.skipHydrationRootTNode===t}function nh(){Ee.skipHydrationRootTNode=null}function ce(){return Ee.lFrame.lView}function Xe(){return Ee.lFrame.tView}function k(t){return Ee.lFrame.contextLView=t,t[yt]}function M(t){return Ee.lFrame.contextLView=null,t}function Tt(){let t=ih();for(;t!==null&&t.type===64;)t=t.parent;return t}function ih(){return Ee.lFrame.currentTNode}function o_(){let t=Ee.lFrame,n=t.currentTNode;return t.isParent?n:n.parent}function po(t,n){let e=Ee.lFrame;e.currentTNode=t,e.isParent=n}function ah(){return Ee.lFrame.isParent}function oh(){Ee.lFrame.isParent=!1}function rh(){return Ee.lFrame.contextLView}function sh(){return Dp}function ws(t){let n=Dp;return Dp=t,n}function ud(){let t=Ee.lFrame,n=t.bindingRootIndex;return n===-1&&(n=t.bindingRootIndex=t.tView.bindingStartIndex),n}function ho(){return Ee.lFrame.bindingIndex}function r_(t){return Ee.lFrame.bindingIndex=t}function va(){return Ee.lFrame.bindingIndex++}function Hi(t){let n=Ee.lFrame,e=n.bindingIndex;return n.bindingIndex=n.bindingIndex+t,e}function s_(){return Ee.lFrame.inI18n}function l_(t,n){let e=Ee.lFrame;e.bindingIndex=e.bindingRootIndex=t,md(n)}function c_(){return Ee.lFrame.currentDirectiveIndex}function md(t){Ee.lFrame.currentDirectiveIndex=t}function d_(t){let n=Ee.lFrame.currentDirectiveIndex;return n===-1?null:t[n]}function pd(){return Ee.lFrame.currentQueryIndex}function Vs(t){Ee.lFrame.currentQueryIndex=t}function uD(t){let n=t[ve];return n.type===2?n.declTNode:n.type===1?t[Ut]:null}function lh(t,n,e){if(e&4){let a=n,o=t;for(;a=a.parent,a===null&&!(e&1);)if(a=uD(o),a===null||(o=o[ga],a.type&10))break;if(a===null)return!1;n=a,t=o}let i=Ee.lFrame=u_();return i.currentTNode=n,i.lView=t,!0}function hd(t){let n=u_(),e=t[ve];Ee.lFrame=n,n.currentTNode=e.firstChild,n.lView=t,n.tView=e,n.contextLView=t,n.bindingIndex=e.bindingStartIndex,n.inI18n=!1}function u_(){let t=Ee.lFrame,n=t===null?null:t.child;return n===null?m_(t):n}function m_(t){let n={currentTNode:null,isParent:!0,lView:null,tView:null,selectedIndex:-1,contextLView:null,elementDepthCount:0,currentNamespace:null,currentDirectiveIndex:-1,bindingRootIndex:-1,bindingIndex:-1,currentQueryIndex:0,parent:t,child:null,inI18n:!1};return t!==null&&(t.child=n),n}function p_(){let t=Ee.lFrame;return Ee.lFrame=t.parent,t.currentTNode=null,t.lView=null,t}var ch=p_;function fd(){let t=p_();t.isParent=!0,t.tView=null,t.selectedIndex=-1,t.contextLView=null,t.elementDepthCount=0,t.currentDirectiveIndex=-1,t.currentNamespace=null,t.bindingRootIndex=-1,t.bindingIndex=-1,t.currentQueryIndex=0}function h_(t){return(Ee.lFrame.contextLView=e_(t,Ee.lFrame.contextLView))[yt]}function Zt(){return Ee.lFrame.selectedIndex}function _a(t){Ee.lFrame.selectedIndex=t}function mr(){let t=Ee.lFrame;return rd(t.tView,t.selectedIndex)}function Gt(){Ee.lFrame.currentNamespace=qp}function ya(){mD()}function mD(){Ee.lFrame.currentNamespace=null}function dh(){return Ee.lFrame.currentNamespace}var f_=!0;function gd(){return f_}function Bs(t){f_=t}function Ip(t,n=null,e=null,i){let a=uh(t,n,e,i);return a.resolveInjectorInitializers(),a}function uh(t,n=null,e=null,i,a=new Set){return new no([e||Qt,$v(t)],n||or(),null,a)}var se=class t{static THROW_IF_NOT_FOUND=Ja;static NULL=new ir;static create(n,e){if(Array.isArray(n))return Ip({name:``},e,n,``);{let i=n.name??``;return Ip({name:i},n.parent,n.providers,i)}}static ɵprov=be({token:t,providedIn:`any`,factory:()=>Se(ad)});static __NG_ELEMENT_ID__=-1};var me=new N(``);var Ae=class{static __NG_ELEMENT_ID__=pD;static __NG_ENV_ID__=n=>n};var Wc=class extends Ae{_lView;constructor(n){super(),this._lView=n}get destroyed(){return uo(this._lView)}onDestroy(n){let e=this._lView;return cd(e,n),()=>t_(e,n)}};function pD(){return new Wc(ce())}var g_=!1;var b_=new N(``);var fo=(()=>{class t{taskId=0;pendingTasks=new Set;destroyed=!1;pendingTask=new hi(!1);debugTaskTracker=f(b_,{optional:!0});get hasPendingTasks(){return this.destroyed?!1:this.pendingTask.value}get hasPendingTasksObservable(){return this.destroyed?new Ce(e=>{e.next(!1),e.complete()}):this.pendingTask}add(){!this.hasPendingTasks&&!this.destroyed&&this.pendingTask.next(!0);let e=this.taskId++;return this.pendingTasks.add(e),this.debugTaskTracker?.add(e),e}has(e){return this.pendingTasks.has(e)}remove(e){this.pendingTasks.delete(e),this.debugTaskTracker?.remove(e),this.pendingTasks.size===0&&this.hasPendingTasks&&this.pendingTask.next(!1)}ngOnDestroy(){this.pendingTasks.clear(),this.hasPendingTasks&&this.pendingTask.next(!1),this.destroyed=!0,this.pendingTask.unsubscribe()}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var kp=class extends V{__isAsync;destroyRef=void 0;pendingTasks=void 0;constructor(n=!1){super(),this.__isAsync=n,Yv()&&(this.destroyRef=f(Ae,{optional:!0})??void 0,this.pendingTasks=f(fo,{optional:!0})??void 0)}emit(n){let e=ge(null);try{super.next(n)}finally{ge(e)}}subscribe(n,e,i){let a=n,o=e||(()=>null),r=i;if(n&&typeof n==`object`){let l=n;a=l.next?.bind(l),o=l.error?.bind(l),r=l.complete?.bind(l)}this.__isAsync&&(o=this.wrapInTimeout(o),a&&(a=this.wrapInTimeout(a)),r&&(r=this.wrapInTimeout(r)));let s=super.subscribe({next:a,error:o,complete:r});return n instanceof De&&n.add(s),s}wrapInTimeout(n){return e=>{let i=this.pendingTasks?.add();setTimeout(()=>{try{n(e)}finally{i!==void 0&&this.pendingTasks?.remove(i)}})}}};var te=kp;function qc(...t){}function mh(t){let n,e;function i(){t=qc;try{e!==void 0&&typeof cancelAnimationFrame==`function`&&cancelAnimationFrame(e),n!==void 0&&clearTimeout(n)}catch{}}return n=setTimeout(()=>{t(),i()}),typeof requestAnimationFrame==`function`&&(e=requestAnimationFrame(()=>{t(),i()})),()=>i()}function v_(t){return queueMicrotask(()=>t()),()=>{t=qc}}var ph=`isAngularZone`;var Es=ph+`_ID`;var hD=0;var Z=class t{hasPendingMacrotasks=!1;hasPendingMicrotasks=!1;isStable=!0;onUnstable=new te(!1);onMicrotaskEmpty=new te(!1);onStable=new te(!1);onError=new te(!1);constructor(n){let{enableLongStackTrace:e=!1,shouldCoalesceEventChangeDetection:i=!1,shouldCoalesceRunChangeDetection:a=!1,scheduleInRootZone:o=g_}=n;if(typeof Zone>`u`)throw new q(908,!1);Zone.assertZonePatched();let r=this;r._nesting=0,r._outer=r._inner=Zone.current,Zone.TaskTrackingZoneSpec&&(r._inner=r._inner.fork(new Zone.TaskTrackingZoneSpec)),e&&Zone.longStackTraceZoneSpec&&(r._inner=r._inner.fork(Zone.longStackTraceZoneSpec)),r.shouldCoalesceEventChangeDetection=!a&&i,r.shouldCoalesceRunChangeDetection=a,r.callbackScheduled=!1,r.scheduleInRootZone=o,bD(r)}static isInAngularZone(){return typeof Zone<`u`&&Zone.current.get(ph)===!0}static assertInAngularZone(){if(!t.isInAngularZone())throw new q(909,!1)}static assertNotInAngularZone(){if(t.isInAngularZone())throw new q(909,!1)}run(n,e,i){return this._inner.run(n,e,i)}runTask(n,e,i,a){let o=this._inner,r=o.scheduleEventTask(`NgZoneEvent: `+a,n,fD,qc,qc);try{return o.runTask(r,e,i)}finally{o.cancelTask(r)}}runGuarded(n,e,i){return this._inner.runGuarded(n,e,i)}runOutsideAngular(n){return this._outer.run(n)}};var fD={};function hh(t){if(t._nesting==0&&!t.hasPendingMicrotasks&&!t.isStable)try{t._nesting++,t.onMicrotaskEmpty.emit(null)}finally{if(t._nesting--,!t.hasPendingMicrotasks)try{t.runOutsideAngular(()=>t.onStable.emit(null))}finally{t.isStable=!0}}}function gD(t){if(t.isCheckStableRunning||t.callbackScheduled)return;t.callbackScheduled=!0;function n(){mh(()=>{t.callbackScheduled=!1,Mp(t),t.isCheckStableRunning=!0,hh(t),t.isCheckStableRunning=!1})}t.scheduleInRootZone?Zone.root.run(()=>{n()}):t._outer.run(()=>{n()}),Mp(t)}function bD(t){let n=()=>{gD(t)},e=hD++;t._inner=t._inner.fork({name:`angular`,properties:{[ph]:!0,[Es]:e,[Es+e]:!0},onInvokeTask:(i,a,o,r,s,l)=>{if(vD(l))return i.invokeTask(o,r,s,l);try{return Nv(t),i.invokeTask(o,r,s,l)}finally{(t.shouldCoalesceEventChangeDetection&&r.type===`eventTask`||t.shouldCoalesceRunChangeDetection)&&n(),Av(t)}},onInvoke:(i,a,o,r,s,l,h)=>{try{return Nv(t),i.invoke(o,r,s,l,h)}finally{t.shouldCoalesceRunChangeDetection&&!t.callbackScheduled&&!_D(l)&&n(),Av(t)}},onHasTask:(i,a,o,r)=>{i.hasTask(o,r),a===o&&(r.change==`microTask`?(t._hasPendingMicrotasks=r.microTask,Mp(t),hh(t)):r.change==`macroTask`&&(t.hasPendingMacrotasks=r.macroTask))},onHandleError:(i,a,o,r)=>(i.handleError(o,r),t.runOutsideAngular(()=>t.onError.emit(r)),!1)})}function Mp(t){t._hasPendingMicrotasks||(t.shouldCoalesceEventChangeDetection||t.shouldCoalesceRunChangeDetection)&&t.callbackScheduled===!0?t.hasPendingMicrotasks=!0:t.hasPendingMicrotasks=!1}function Nv(t){t._nesting++,t.isStable&&(t.isStable=!1,t.onUnstable.emit(null))}function Av(t){t._nesting--,hh(t)}var Ds=class{hasPendingMicrotasks=!1;hasPendingMacrotasks=!1;isStable=!0;onUnstable=new te;onMicrotaskEmpty=new te;onStable=new te;onError=new te;run(n,e,i){return n.apply(e,i)}runGuarded(n,e,i){return n.apply(e,i)}runOutsideAngular(n){return n()}runTask(n,e,i,a){return n.apply(e,i)}};function vD(t){return __(t,`__ignore_ng_zone__`)}function _D(t){return __(t,`__scheduler_tick__`)}function __(t,n){return!Array.isArray(t)||t.length!==1?!1:t[0]?.data?.[n]===!0}var Tn=class{_console=console;handleError(n){this._console.error(`ERROR`,n)}};function y_(t){return x_(t)?t:new Tp(t)}function x_(t){return t instanceof Error||typeof t==`object`&&t!==null&&typeof t.name==`string`&&typeof t.message==`string`}var Tp=class extends Error{constructor(n){super(String(n),{cause:n}),this.name=`ErrorBoundaryWrappedError`}};var Ui=new N(``,{factory:()=>{let t=f(Z),n=f(Mt),e;return i=>{t.runOutsideAngular(()=>{n.destroyed&&!e?setTimeout(()=>{throw i}):(e??=n.get(Tn),e.handleError(i))})}}});var C_={provide:ao,useValue:()=>{f(Tn,{optional:!0})},multi:!0};var yD=new N(``,{factory:()=>{let t=f(me).defaultView;if(!t)return;let n=f(Ui),e=o=>{n(o.reason),o.preventDefault()},i=o=>{o.error?n(o.error):n(new Error(o.message,{cause:o})),o.preventDefault()},a=()=>{t.addEventListener(`unhandledrejection`,e),t.addEventListener(`error`,i)};typeof Zone<`u`?Zone.root.run(a):a(),f(Ae).onDestroy(()=>{t.removeEventListener(`error`,i),t.removeEventListener(`unhandledrejection`,e)})}});function fh(){return ln([Uv(()=>{f(yD)})])}function T(t,n){let[e,i,a]=Jm(t,n?.equal),o=e;o[St];return o.set=i,o.update=a,o.asReadonly=bd.bind(o),o}function bd(){let t=this[St];if(t.readonlyFn===void 0){let n=()=>this();n[St]=t,t.readonlyFn=n}return t.readonlyFn}var $i=new N(``,{factory:()=>xD});var xD=`ng`;var vd=new N(``);var Gi=new N(``,{providedIn:`platform`,factory:()=>`unknown`});var js=new N(``);var xa=new N(``,{factory:()=>f(me).body?.querySelector(`[ngCspNonce]`)?.getAttribute(`ngCspNonce`)||null});var pr=(()=>{class t{view;node;constructor(e,i){this.view=e,this.node=i}static __NG_ELEMENT_ID__=CD}return t})();function CD(){return new pr(ce(),Tt())}var gi=class{};var zs=new N(``,{factory:()=>!0});var gh=new N(``);var _d=(()=>{class t{static ɵprov=be({token:t,providedIn:`root`,factory:()=>new Np})}return t})();var Np=class{dirtyEffectCount=0;queues=new Map;add(n){this.enqueue(n),this.schedule(n)}schedule(n){n.dirty&&this.dirtyEffectCount++}remove(n){let e=n.zone,i=this.queues.get(e);i.has(n)&&(i.delete(n),n.dirty&&this.dirtyEffectCount--)}enqueue(n){let e=n.zone;this.queues.has(e)||this.queues.set(e,new Set);let i=this.queues.get(e);i.has(n)||i.add(n)}flush(){for(;this.dirtyEffectCount>0;){let n=!1;for(let[e,i]of this.queues)e===null?n||=this.flushQueue(i):n||=e.run(()=>this.flushQueue(i));n||(this.dirtyEffectCount=0)}}flushQueue(n){let e=!1;for(let i of n)i.dirty&&(this.dirtyEffectCount--,e=!0,i.run());return e}};var Yc=class{[St];constructor(n){this[St]=n}destroy(){this[St].destroy()}};function vt(t,n){let e=n?.injector??f(se),i=n?.manualCleanup!==!0?e.get(Ae):null,a,o=e.get(pr,null,{optional:!0}),r=e.get(gi);return o!==null?(a=w_(o.view,r,t),i instanceof Wc&&i._lView===o.view&&(i=null)):a=ED(t,e.get(_d),r),a.injector=e,i!==null&&(a.onDestroyFns=[i.onDestroy(()=>a.destroy())]),new Yc(a)}var S_=s(r({},tp),{cleanupFns:void 0,zone:null,onDestroyFns:null,run(){let t=ws(!1);try{np(this)}finally{ws(t)}},cleanup(){if(!this.cleanupFns?.length)return;let t=ge(null);try{for(;this.cleanupFns.length;)this.cleanupFns.pop()()}finally{this.cleanupFns=[],ge(t)}}});var SD=s(r({},S_),{consumerMarkedDirty(){this.scheduler.schedule(this),this.notifier.notify(12)},destroy(){if(da(this),this.onDestroyFns!==null)for(let t of this.onDestroyFns)t();this.cleanup(),this.scheduler.remove(this)}});var wD=s(r({},S_),{consumerMarkedDirty(){this.view[xe]|=8192,mo(this.view),this.notifier.notify(13)},destroy(){if(da(this),this.onDestroyFns!==null)for(let t of this.onDestroyFns)t();this.cleanup(),this.view[Li]?.delete(this)}});function w_(t,n,e){let i=Object.create(wD);return i.view=t,i.zone=typeof Zone<`u`?Zone.current:null,i.notifier=n,i.userFn=e,i.fn=E_(i,e),t[Li]??=new Set,t[Li].add(i),i.consumerMarkedDirty(i),i}function ED(t,n,e){let i=Object.create(SD);return i.userFn=t,i.fn=E_(i,t),i.scheduler=n,i.notifier=e,i.zone=typeof Zone<`u`?Zone.current:null,i.scheduler.add(i),i.notifier.notify(12),i}function E_(t,n){return()=>{n(e=>(t.cleanupFns??=[]).push(e))}}function cn(t){return typeof t==`function`&&t[St]!==void 0}var Ca=(()=>{class t{internalPendingTasks=f(fo);scheduler=f(gi);errorHandler=f(Ui);add(){let e=this.internalPendingTasks.add();return()=>{this.internalPendingTasks.has(e)&&(this.scheduler.notify(11),this.internalPendingTasks.remove(e))}}run(e){let i=this.add();try{e().catch(this.errorHandler).finally(i)}catch(a){this.errorHandler(a),i()}}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var wr=Symbol(`InputSignalNode#UNSET`);var mf=s(r({},Yo),{transformFn:void 0,applyValueToInputSignal(t,n){ua(t,n)}});function Zs(t){return{toString:t}.toString()}var ze=(function(t){return t[t.TemplateCreateStart=0]=`TemplateCreateStart`,t[t.TemplateCreateEnd=1]=`TemplateCreateEnd`,t[t.TemplateUpdateStart=2]=`TemplateUpdateStart`,t[t.TemplateUpdateEnd=3]=`TemplateUpdateEnd`,t[t.LifecycleHookStart=4]=`LifecycleHookStart`,t[t.LifecycleHookEnd=5]=`LifecycleHookEnd`,t[t.OutputStart=6]=`OutputStart`,t[t.OutputEnd=7]=`OutputEnd`,t[t.BootstrapApplicationStart=8]=`BootstrapApplicationStart`,t[t.BootstrapApplicationEnd=9]=`BootstrapApplicationEnd`,t[t.BootstrapComponentStart=10]=`BootstrapComponentStart`,t[t.BootstrapComponentEnd=11]=`BootstrapComponentEnd`,t[t.ChangeDetectionStart=12]=`ChangeDetectionStart`,t[t.ChangeDetectionEnd=13]=`ChangeDetectionEnd`,t[t.ChangeDetectionSyncStart=14]=`ChangeDetectionSyncStart`,t[t.ChangeDetectionSyncEnd=15]=`ChangeDetectionSyncEnd`,t[t.AfterRenderHooksStart=16]=`AfterRenderHooksStart`,t[t.AfterRenderHooksEnd=17]=`AfterRenderHooksEnd`,t[t.ComponentStart=18]=`ComponentStart`,t[t.ComponentEnd=19]=`ComponentEnd`,t[t.DeferBlockStateStart=20]=`DeferBlockStateStart`,t[t.DeferBlockStateEnd=21]=`DeferBlockStateEnd`,t[t.DynamicComponentStart=22]=`DynamicComponentStart`,t[t.DynamicComponentEnd=23]=`DynamicComponentEnd`,t[t.HostBindingsUpdateStart=24]=`HostBindingsUpdateStart`,t[t.HostBindingsUpdateEnd=25]=`HostBindingsUpdateEnd`,t})(ze||{});var Id=class{previousValue;currentValue;firstChange;constructor(n,e,i){this.previousValue=n,this.currentValue=e,this.firstChange=i}isFirstChange(){return this.firstChange}};function py(t,n,e,i){n!==null?n.applyValueToInputSignal(n,i):t[e]=i}var hy=null;var _t=(()=>{hy=D_;let t=()=>D_;return t.ngInherit=!0,t})();function FD(){return hy}function D_(t){return t.type.prototype.ngOnChanges&&(t.setInput=VD),LD}function LD(){let t=fy(this),n=t?.current;if(n){let e=t.previous;if(e===fa)t.previous=n;else for(let i in n)e[i]=n[i];t.current=null,this.ngOnChanges(n)}}function VD(t,n,e,i,a){let o=this.declaredInputs[i],r=fy(t)||BD(t,{previous:fa,current:null}),s=r.current||(r.current={}),l=r.previous,h=l[o];s[o]=new Id(h&&h.currentValue,e,l===fa),py(t,n,a,e)}var kh=`__ngSimpleChanges__`;function fy(t){return Object.hasOwn(t,kh)&&t[kh]||null}function BD(t,n){return t[kh]=n}var I_=[];var Ke=function(t,n=null,e){for(let i=0;i<I_.length;i++){let a=I_[i];a(t,n,e)}};function jD(t,n,e){let{ngOnChanges:i,ngOnInit:a,ngDoCheck:o}=n.type.prototype;if(i){let r=FD()(n);(e.preOrderHooks??=[]).push(t,r),(e.preOrderCheckHooks??=[]).push(t,r)}a&&(e.preOrderHooks??=[]).push(0-t,a),o&&((e.preOrderHooks??=[]).push(t,o),(e.preOrderCheckHooks??=[]).push(t,o))}function gy(t,n){for(let e=n.directiveStart,i=n.directiveEnd;e<i;e++){let{ngAfterContentInit:r,ngAfterContentChecked:s,ngAfterViewInit:l,ngAfterViewChecked:h,ngOnDestroy:p}=t.data[e].type.prototype;r&&(t.contentHooks??=[]).push(-e,r),s&&((t.contentHooks??=[]).push(e,s),(t.contentCheckHooks??=[]).push(e,s)),l&&(t.viewHooks??=[]).push(-e,l),h&&((t.viewHooks??=[]).push(e,h),(t.viewCheckHooks??=[]).push(e,h)),p!=null&&(t.destroyHooks??=[]).push(e,p)}}function Sd(t,n,e){by(t,n,3,e)}function wd(t,n,e,i){(t[xe]&3)===e&&by(t,n,e,i)}function bh(t,n){let e=t[xe];(e&3)===n&&(e&=16383,e+=1,t[xe]=e)}function by(t,n,e,i){let a=i!==void 0?t[ro]&65535:0,o=i??-1,r=n.length-1,s=0;for(let l=a;l<r;l++)if(typeof n[l+1]==`number`){if(s=n[l],i!=null&&s>=i)break}else n[l]<0&&(t[ro]+=65536),(s<o||o==-1)&&(zD(t,e,n,l),t[ro]=(t[ro]&4294901760)+l+2),l++}function k_(t,n){Ke(ze.LifecycleHookStart,t,n);let e=ge(null);try{n.call(t)}finally{ge(e),Ke(ze.LifecycleHookEnd,t,n)}}function zD(t,n,e,i){let a=e[i]<0,o=e[i+1],s=t[a?-e[i]:e[i]];a?t[xe]>>14<t[ro]>>16&&(t[xe]&3)===n&&(t[xe]+=16384,k_(s,o)):k_(s,o)}var gr=-1;var vo=class{factory;name;injectImpl;resolving=!1;canSeeViewProviders;multi;componentProviders;index;providerFactory;constructor(n,e,i,a){this.factory=n,this.name=a,this.canSeeViewProviders=e,this.injectImpl=i}};function HD(t){return(t.flags&8)!==0}function UD(t){return(t.flags&16)!==0}function $D(t,n,e){let i=0;for(;i<e.length;){let a=e[i];if(typeof a==`number`){if(a!==0)break;i++;let o=e[i++],r=e[i++],s=e[i++];t.setAttribute(n,r,s,o)}else{let o=a,r=e[++i];GD(o)?t.setProperty(n,o,r):t.setAttribute(n,o,r),i++}}return i}function vy(t){return t===3||t===4||t===6}function GD(t){return t.charCodeAt(0)===64}function br(t,n){if(!(n===null||n.length===0))if(t===null||t.length===0)t=n.slice();else{let e=-1;for(let i=0;i<n.length;i++){let a=n[i];typeof a==`number`?e=a:e===0||(e===-1||e===2?M_(t,e,a,null,n[++i]):M_(t,e,a,null,null))}}return t}function M_(t,n,e,i,a){let o=0,r=t.length;if(n===-1)r=-1;else for(;o<t.length;){let s=t[o++];if(typeof s==`number`){if(s===n){r=-1;break}else if(s>n){r=o-1;break}}}for(;o<t.length;){let s=t[o];if(typeof s==`number`)break;if(s===e){a!==null&&(t[o+1]=a);return}o++,a!==null&&o++}r!==-1&&(t.splice(r,0,n),o=r+1),t.splice(o++,0,e),a!==null&&t.splice(o++,0,a)}function _y(t){return t!==gr}function kd(t){return t&32767}function WD(t){return t>>16}function Md(t,n){let e=WD(t),i=n;for(;e>0;)i=i[ga],e--;return i}var Mh=!0;function Td(t){let n=Mh;return Mh=t,n}var yy=255;var xy=5;var YD=0;var yi={};function KD(t,n,e){let i;typeof e==`string`?i=e.charCodeAt(0)||0:Object.hasOwn(e,io)&&(i=e[io]),i??=e[io]=YD++;let a=i&yy,o=1<<a;n.data[t+(a>>xy)]|=o}function Nd(t,n){let e=Cy(t,n);if(e!==-1)return e;let i=n[ve];i.firstCreatePass&&(t.injectorIndex=n.length,vh(i.data,t),vh(n,null),vh(i.blueprint,null));let a=pf(t,n),o=t.injectorIndex;if(_y(a)){let r=kd(a),s=Md(a,n),l=s[ve].data;for(let h=0;h<8;h++)n[o+h]=s[r+h]|l[r+h]}return n[o+8]=a,o}function vh(t,n){t.push(0,0,0,0,0,0,0,0,n)}function Cy(t,n){return t.injectorIndex===-1||t.parent&&t.parent.injectorIndex===t.injectorIndex||n[t.injectorIndex+8]===null?-1:t.injectorIndex}function pf(t,n){if(t.parent&&t.parent.injectorIndex!==-1)return t.parent.injectorIndex;let e=0,i=null,a=n;for(;a!==null;){if(i=Iy(a),i===null)return gr;if(e++,a=a[ga],i.injectorIndex!==-1)return i.injectorIndex|e<<16}return gr}function Th(t,n,e){KD(t,n,e)}function QD(t,n){if(n===`class`)return t.classes;if(n===`style`)return t.styles;let e=t.attrs;if(e){let i=e.length,a=0;for(;a<i;){let o=e[a];if(vy(o))break;if(o===0)a=a+2;else if(typeof o==`number`)for(a++;a<i&&typeof e[a]==`string`;)a++;else{if(o===n)return e[a+1];a=a+2}}}return null}function Sy(t,n,e){if(e&8||t!==void 0)return t;ed(n,`NodeInjector`)}function wy(t,n,e,i){if(e&8&&i===void 0&&(i=null),(e&3)===0){let a=t[ji],o=sn(void 0);try{return a?a.get(n,i,e&8):Lp(n,i,e&8)}finally{sn(o)}}return Sy(i,n,e)}function Ey(t,n,e,i=0,a){if(t!==null){if(n[xe]&2048&&!(i&2)){let r=eI(t,n,e,i,yi);if(r!==yi)return r}let o=Dy(t,n,e,i,yi);if(o!==yi)return o}return wy(n,e,i,a)}function Dy(t,n,e,i,a){let o=ZD(e);if(typeof o==`function`){if(!lh(n,t,i))return i&1?Sy(a,e,i):wy(n,e,i,a);try{let r;if(r=o(i),r==null&&!(i&8))ed(e);else return r}finally{ch()}}else if(typeof o==`number`){let r=null,s=Cy(t,n),l=gr,h=i&1?n[Xt][Ut]:null;for((s===-1||i&4)&&(l=s===-1?pf(t,n):n[s+8],l===gr||!N_(i,!1)?s=-1:(r=n[ve],s=kd(l),n=Md(l,n)));s!==-1;){let p=n[ve];if(T_(o,s,p.data)){let g=XD(s,n,e,r,i,h);if(g!==yi)return g}l=n[s+8],l!==gr&&N_(i,n[ve].data[s+8]===h)&&T_(o,s,n)?(r=p,s=kd(l),n=Md(l,n)):s=-1}}return a}function XD(t,n,e,i,a,o){let r=n[ve],s=r.data[t+8],p=Ed(s,r,e,i==null?_i(s)&&Mh:i!=r&&(s.type&3)!==0,a&1&&o===s);return p!==null?Gs(n,r,p,s,a):yi}function Ed(t,n,e,i,a){let o=t.providerIndexes,r=n.data,s=o&1048575,l=t.directiveStart,h=t.directiveEnd,p=o>>20,g=i?s:s+p,_=a?s+p:h;for(let b=g;b<_;b++){let y=r[b];if(b<l&&e===y||b>=l&&y.type===e)return b}if(a){let b=r[l];if(b&&Xn(b)&&b.type===e)return l}return null}function Gs(t,n,e,i,a){let o=t[e],r=n.data;if(o instanceof vo){let s=o;if(s.resolving)throw Fp(``);let l=Td(s.canSeeViewProviders);s.resolving=!0;r[e].type||r[e];let g=s.injectImpl?sn(s.injectImpl):null;lh(t,i,0);try{o=t[e]=s.factory(void 0,a,r,t,i),n.firstCreatePass&&e>=i.directiveStart&&jD(e,r[e],n)}finally{g!==null&&sn(g),Td(l),s.resolving=!1,ch()}}return o}function ZD(t){if(typeof t==`string`)return t.charCodeAt(0)||0;let n=Object.hasOwn(t,io)?t[io]:void 0;return typeof n==`number`?n>=0?n&yy:JD:n}function T_(t,n,e){let i=1<<t;return!!(e[n+(t>>xy)]&i)}function N_(t,n){return!(t&2)&&!(t&1&&n)}var Wi=class{_tNode;_lView;constructor(n,e){this._tNode=n,this._lView=e}get(n,e,i){return Ey(this._tNode,this._lView,n,eo(i),e)}};function JD(){return new Wi(Tt(),ce())}function At(t){return Zs(()=>{let n=t.prototype.constructor,e=n[Ss]||Nh(n),i=Object.prototype,a=Object.getPrototypeOf(t.prototype).constructor;for(;a&&a!==i;){let o=a[Ss]||Nh(a);if(o&&o!==e)return o;a=Object.getPrototypeOf(a)}return o=>new o})}function Nh(t){return Ap(t)?()=>{let n=Nh(Lt(t));return n&&n()}:pa(t)}function eI(t,n,e,i,a){let o=t,r=n;for(;o!==null&&r!==null&&r[xe]&2048&&!ur(r);){let s=Dy(o,r,e,i|2,yi);if(s!==yi)return s;i&=-5;let l=o.parent;if(!l){let h=r[$p];if(h){let p=h.get(e,yi,i);if(p!==yi)return p}l=Iy(r),r=r[ga]}o=l}return a}function Iy(t){let n=t[ve],e=n.type;return e===2?n.declTNode:e===1?t[Ut]:null}function hf(t){return QD(Tt(),t)}function le(t){return{token:t.token,providedIn:t.autoProvided===!1?null:`root`,factory:t.factory,value:void 0}}function tI(){return Er(Tt(),ce())}function Er(t,n){return new Y(Cn(t,n))}var Y=(()=>{class t{nativeElement;constructor(e){this.nativeElement=e}static __NG_ELEMENT_ID__=tI}return t})();function ky(t){return t instanceof Y?t.nativeElement:t}function nI(){return this._results[Symbol.iterator]()}var wn=class{_emitDistinctChangesOnly;dirty=!0;_onDirty=void 0;_results=[];_changesDetected=!1;_changes=void 0;length=0;first=void 0;last=void 0;get changes(){return this._changes??=new V}constructor(n=!1){this._emitDistinctChangesOnly=n}get(n){return this._results[n]}map(n){return this._results.map(n)}filter(n){return this._results.filter(n)}find(n){return this._results.find(n)}reduce(n,e){return this._results.reduce(n,e)}forEach(n){this._results.forEach(n)}some(n){return this._results.some(n)}toArray(){return this._results.slice()}toString(){return this._results.toString()}reset(n,e){this.dirty=!1;let i=jv(n);(this._changesDetected=!Bv(this._results,i,e))&&(this._results=i,this.length=i.length,this.last=i[this.length-1],this.first=i[0])}notifyOnChanges(){this._changes!==void 0&&(this._changesDetected||!this._emitDistinctChangesOnly)&&this._changes.next(this)}onDirty(n){this._onDirty=n}setDirty(){this.dirty=!0,this._onDirty?.()}destroy(){this._changes!==void 0&&(this._changes.complete(),this._changes.unsubscribe())}[Symbol.iterator]=nI};function My(t){return(t.flags&128)===128}var ff=(function(t){return t[t.OnPush=0]=`OnPush`,t[t.Eager=1]=`Eager`,t[t.Default=1]=`Default`,t})(ff||{});var Ty=new Map;var iI=0;function aI(){return iI++}function oI(t){Ty.set(t[Rs],t)}function Ah(t){Ty.delete(t[Rs])}var A_=`__ngContext__`;function vr(t,n){zi(n)?(t[A_]=n[Rs],oI(n)):t[A_]=n}function Ny(t){return Ry(t[lr])}function Ay(t){return Ry(t[Nn])}function Ry(t){for(;t!==null&&!An(t);)t=t[Nn];return t}var Rh;function gf(t){Rh=t}function Oy(){if(Rh!==void 0)return Rh;if(typeof document<`u`)return document;throw new q(210,!1)}var Py=`r`;var Fy=`di`;var Ly=!1;var Vy=new N(``,{factory:()=>Ly});var R_=new WeakMap;function rI(t,n){if(t==null||typeof t!=`object`)return;let e=R_.get(t);e||(e=new WeakSet,R_.set(t,e)),e.add(n)}function Ud(t){return(t.flags&32)===32}var cI=()=>null;function By(t,n,e=!1){return cI(t,n,e)}function jy(t,n){let e=t.contentQueries;if(e!==null){let i=ge(null);try{for(let a=0;a<e.length;a+=2){let o=e[a],r=e[a+1];if(r!==-1){let s=t.data[r];Vs(o),s.contentQueries(2,n[r],r)}}}finally{ge(i)}}}function Oh(t,n,e){Vs(0);let i=ge(null);try{n(t,e)}finally{ge(i)}}function bf(t,n,e){if(Wp(n)){let i=ge(null);try{let a=n.directiveStart,o=n.directiveEnd;for(let r=a;r<o;r++){let s=t.data[r];if(s.contentQueries){let l=e[r];s.contentQueries(1,l,r)}}}finally{ge(i)}}}var ei=(function(t){return t[t.Emulated=0]=`Emulated`,t[t.None=2]=`None`,t[t.ShadowDom=3]=`ShadowDom`,t[t.ExperimentalIsolatedShadowDom=4]=`ExperimentalIsolatedShadowDom`,t})(ei||{});var yd;function dI(){if(yd===void 0&&(yd=null,bi.trustedTypes))try{yd=bi.trustedTypes.createPolicy(`angular`,{createHTML:t=>t,createScript:t=>t,createScriptURL:t=>t})}catch{}return yd}function $d(t){return dI()?.createHTML(t)||t}var qi=class{changingThisBreaksApplicationSecurity;constructor(n){this.changingThisBreaksApplicationSecurity=n}toString(){return`SafeValue must use [property]=binding: ${this.changingThisBreaksApplicationSecurity} (see ${Kc})`}};var Ph=class extends qi{getTypeName(){return`HTML`}};var Fh=class extends qi{getTypeName(){return`Style`}};var Lh=class extends qi{getTypeName(){return`Script`}};var Vh=class extends qi{getTypeName(){return`URL`}};var Bh=class extends qi{getTypeName(){return`ResourceURL`}};function ti(t){return t instanceof qi?t.changingThisBreaksApplicationSecurity:t}function wa(t,n){let e=zy(t);if(e!=null&&e!==n){if(e===`ResourceURL`&&n===`URL`)return!0;throw new Error(`Required a safe ${n}, got a ${e} (see ${Kc})`)}return e===n}function zy(t){return t instanceof qi&&t.getTypeName()||null}function vf(t){return new Ph(t)}function _f(t){return new Fh(t)}function yf(t){return new Lh(t)}function xf(t){return new Vh(t)}function Cf(t){return new Bh(t)}function uI(t){let n=new zh(t);return mI()?new jh(n):n}var jh=class{inertDocumentHelper;constructor(n){this.inertDocumentHelper=n}getInertBodyElement(n){n=`<body><remove></remove>`+n;try{let e=new window.DOMParser().parseFromString($d(n),`text/html`).body;return e===null?this.inertDocumentHelper.getInertBodyElement(n):(e.firstChild?.remove(),e)}catch{return null}}};var zh=class{defaultDoc;inertDocument;constructor(n){this.defaultDoc=n,this.inertDocument=this.defaultDoc.implementation.createHTMLDocument(`sanitization-inert`)}getInertBodyElement(n){let e=this.inertDocument.createElement(`template`);return e.innerHTML=$d(n),e}};function mI(){try{return!!new window.DOMParser().parseFromString($d(``),`text/html`)}catch{return!1}}var pI=/^(?!javascript:)(?:[a-z0-9+.-]+:|[^&:\/?#]*(?:[\/?#]|$))/i;function Js(t){return t=String(t),t.match(pI)?t:`unsafe:`+t}function Yi(t){let n=Object.create(null);for(let e of t.split(`,`))n[e]=!0;return n}function el(...t){let n=Object.create(null);for(let e of t)for(let i in e)Object.hasOwn(e,i)&&(n[i]=!0);return n}var Hy=Yi(`area,br,col,hr,img,wbr`);var Uy=Yi(`colgroup,dd,dt,li,p,tbody,td,tfoot,th,thead,tr`);var $y=Yi(`rp,rt`);var hI=el($y,Uy);var O_=el(Hy,el(Uy,Yi(`address,article,aside,blockquote,caption,center,del,details,dialog,dir,div,dl,figure,figcaption,footer,h1,h2,h3,h4,h5,h6,header,hgroup,hr,ins,main,map,menu,nav,ol,pre,section,summary,table,ul`)),el($y,Yi(`a,abbr,acronym,audio,b,bdi,bdo,big,br,cite,code,del,dfn,em,font,i,img,ins,kbd,label,map,mark,picture,q,ruby,rp,rt,s,samp,small,source,span,strike,strong,sub,sup,time,track,tt,u,var,video`)),hI);var Gy=Yi(`background,cite,href,itemtype,longdesc,poster,src,xlink:href`);var _I=el(Gy,Yi(`abbr,accesskey,align,alt,autoplay,axis,bgcolor,border,cellpadding,cellspacing,class,clear,color,cols,colspan,compact,controls,coords,datetime,default,dir,download,face,headers,height,hidden,hreflang,hspace,ismap,itemscope,itemprop,kind,label,lang,language,loop,media,muted,nohref,nowrap,open,preload,rel,rev,role,rows,rowspan,rules,scope,scrolling,shape,size,sizes,span,srclang,srcset,start,summary,tabindex,target,title,translate,type,usemap,valign,value,vspace,width`),Yi(`aria-activedescendant,aria-atomic,aria-autocomplete,aria-busy,aria-checked,aria-colcount,aria-colindex,aria-colspan,aria-controls,aria-current,aria-describedby,aria-details,aria-disabled,aria-dropeffect,aria-errormessage,aria-expanded,aria-flowto,aria-grabbed,aria-haspopup,aria-hidden,aria-invalid,aria-keyshortcuts,aria-label,aria-labelledby,aria-level,aria-live,aria-modal,aria-multiline,aria-multiselectable,aria-orientation,aria-owns,aria-placeholder,aria-posinset,aria-pressed,aria-readonly,aria-relevant,aria-required,aria-roledescription,aria-rowcount,aria-rowindex,aria-rowspan,aria-selected,aria-setsize,aria-sort,aria-valuemax,aria-valuemin,aria-valuenow,aria-valuetext`));var yI=Yi(`script,style,template`);var Hh=class{sanitizedSomething=!1;buf=[];sanitizeChildren(n){let e=n.firstChild,i=!0,a=[];for(;e;){if(e.nodeType===Node.ELEMENT_NODE?i=this.startElement(e):e.nodeType===Node.TEXT_NODE?this.chars(e.nodeValue):this.sanitizedSomething=!0,i&&e.firstChild){a.push(e),e=SI(e);continue}for(;e;){e.nodeType===Node.ELEMENT_NODE&&this.endElement(e);let o=CI(e);if(o){e=o;break}e=a.pop()}}return this.buf.join(``)}startElement(n){let e=P_(n).toLowerCase();if(!Object.hasOwn(O_,e))return this.sanitizedSomething=!0,!Object.hasOwn(yI,e);this.buf.push(`<`),this.buf.push(e);let i=n.attributes;for(let a=0;a<i.length;a++){let o=i.item(a),r=o.name,s=r.toLowerCase();if(!Object.hasOwn(_I,s)){this.sanitizedSomething=!0;continue}let l=o.value;Gy[s]&&(l=Js(l)),this.buf.push(` `,r,`="`,F_(l),`"`)}return this.buf.push(`>`),!0}endElement(n){let e=P_(n).toLowerCase();Object.hasOwn(O_,e)&&!Object.hasOwn(Hy,e)&&(this.buf.push(`</`),this.buf.push(e),this.buf.push(`>`))}chars(n){this.buf.push(F_(n))}};function xI(t,n){return(t.compareDocumentPosition(n)&Node.DOCUMENT_POSITION_CONTAINED_BY)!==Node.DOCUMENT_POSITION_CONTAINED_BY}function CI(t){let n=t.nextSibling;if(n&&t!==n.previousSibling)throw Wy(n);return n}function SI(t){let n=t.firstChild;if(n&&xI(t,n))throw Wy(n);return n}function P_(t){let n=t.nodeName;return typeof n==`string`?n:`FORM`}function Wy(t){return new Error(`Failed to sanitize html because the element is clobbered: ${t.outerHTML}`)}var wI=/[\uD800-\uDBFF][\uDC00-\uDFFF]/g;var EI=/([^\#-~ |!])/g;function F_(t){return t.replace(/&/g,`&amp;`).replace(wI,function(n){let e=n.charCodeAt(0),i=n.charCodeAt(1);return`&#`+((e-55296)*1024+(i-56320)+65536)+`;`}).replace(EI,function(n){return`&#`+n.charCodeAt(0)+`;`}).replace(/</g,`&lt;`).replace(/>/g,`&gt;`)}var xd;function Sf(t,n){let e=null;try{xd=xd||uI(t);let i=n?String(n):``;e=xd.getInertBodyElement(i);let a=5,o=i;do{if(a===0)throw new Error(`Failed to sanitize html because the input is unstable`);a--,i=o,o=e.innerHTML,e=xd.getInertBodyElement(i)}while(i!==o);return $d(new Hh().sanitizeChildren(L_(e)||e))}finally{if(e){let i=L_(e)||e;for(;i.firstChild;)i.firstChild.remove()}}}function L_(t){return`content`in t&&DI(t)?t.content:null}function DI(t){return t.nodeType===Node.ELEMENT_NODE&&t.nodeName===`TEMPLATE`}var II=/^>|^->|<!--|-->|--!>|<!-$/g;var kI=/(<|>)/g;var MI=`​$1​`;function TI(t){return t.replace(II,n=>n.replace(kI,MI))}function NI(t,n){return t.createText(n)}function AI(t,n,e){t.setValue(n,e)}function RI(t,n){return t.createComment(TI(n))}function qy(t,n,e){return t.createElement(n,e)}function go(t,n,e,i,a){t.insertBefore(n,e,i,a)}function Yy(t,n,e){t.appendChild(n,e)}function V_(t,n,e,i,a){i!==null?go(t,n,e,i,a):Yy(t,n,e)}function Ky(t,n,e,i){t.removeChild(null,n,e,i)}function OI(t,n,e){t.setAttribute(n,`style`,e)}function PI(t,n,e){e===``?t.removeAttribute(n,`class`):t.setAttribute(n,`class`,e)}function Qy(t,n,e){let{mergedAttrs:i,classes:a,styles:o}=e;i!==null&&$D(t,n,i),a!==null&&PI(t,n,a),o!==null&&OI(t,n,o)}function dn(t){let n=FI();return n?n.sanitize(xn.URL,t)||``:wa(t,`URL`)?ti(t):Js($e(t))}function FI(){let t=ce();return t&&t[Qn].sanitizer}function LI(t){return t instanceof Function?t():t}function VI(t,n,e){let i=t.length;for(;;){let a=t.indexOf(n,e);if(a===-1)return a;if(a===0||t.charCodeAt(a-1)<=32){let o=n.length;if(a+o===i||t.charCodeAt(a+o)<=32)return a}e=a+1}}var Xy=`ng-template`;function BI(t,n,e,i){let a=0;if(i){for(;a<n.length&&typeof n[a]==`string`;a+=2)if(n[a]===`class`&&VI(n[a+1].toLowerCase(),e,0)!==-1)return!0}else if(wf(t))return!1;if(a=n.indexOf(1,a),a>-1){let o;for(;++a<n.length&&typeof(o=n[a])==`string`;)if(o.toLowerCase()===e)return!0}return!1}function wf(t){return t.type===4&&t.value!==Xy}function jI(t,n,e){return n===(t.type===4&&!e?Xy:t.value)}function zI(t,n,e){let i=4,a=t.attrs,o=a!==null?$I(a):0,r=!1;for(let s=0;s<n.length;s++){let l=n[s];if(typeof l==`number`){if(!r&&!Zn(i)&&!Zn(l))return!1;if(r&&Zn(l))continue;r=!1,i=l|i&1;continue}if(!r)if(i&4){if(i=2|i&1,l!==``&&!jI(t,l,e)||l===``&&n.length===1){if(Zn(i))return!1;r=!0}}else if(i&8){if(a===null||!BI(t,a,l,e)){if(Zn(i))return!1;r=!0}}else{let h=n[++s],p=HI(l,a,wf(t),e);if(p===-1){if(Zn(i))return!1;r=!0;continue}if(h!==``){let g;if(p>o?g=``:g=a[p+1].toLowerCase(),i&2&&h!==g){if(Zn(i))return!1;r=!0}}}}return Zn(i)||r}function Zn(t){return(t&1)===0}function HI(t,n,e,i){if(n===null)return-1;let a=0;if(i||!e){let o=!1;for(;a<n.length;){let r=n[a];if(r===t)return a;if(r===3||r===6)o=!0;else if(r===1||r===2){let s=n[++a];for(;typeof s==`string`;)s=n[++a];continue}else{if(r===4)break;if(r===0){a+=4;continue}}a+=o?1:2}return-1}else return GI(n,t)}function Zy(t,n,e=!1){for(let i=0;i<n.length;i++)if(zI(t,n[i],e))return!0;return!1}function UI(t){let n=t.attrs;if(n!=null){let e=n.indexOf(5);if((e&1)===0)return n[e+1]}return null}function $I(t){for(let n=0;n<t.length;n++){let e=t[n];if(vy(e))return n}return t.length}function GI(t,n){let e=t.indexOf(4);if(e>-1)for(e++;e<t.length;){let i=t[e];if(typeof i==`number`)return-1;if(i===n)return e;e++}return-1}function WI(t,n){e:for(let e=0;e<n.length;e++){let i=n[e];if(t.length===i.length){for(let a=0;a<t.length;a++)if(t[a]!==i[a])continue e;return!0}}return!1}function B_(t,n){return t?`:not(`+n.trim()+`)`:n}function qI(t){let n=t[0],e=1,i=2,a=``,o=!1;for(;e<t.length;){let r=t[e];if(typeof r==`string`)if(i&2){let s=t[++e];a+=`[`+r+(s.length>0?`="`+s+`"`:``)+`]`}else i&8?a+=`.`+r:i&4&&(a+=` `+r);else a!==``&&!Zn(r)&&(n+=B_(o,a),a=``),i=r,o=o||!Zn(i);e++}return a!==``&&(n+=B_(o,a)),n}function YI(t){return t.map(qI).join(`,`)}function KI(t){let n=[],e=[],i=1,a=2;for(;i<t.length;){let o=t[i];if(typeof o==`string`)a===2?o!==``&&n.push(o,t[++i]):a===8&&e.push(o);else{if(!Zn(a))break;a=o}i++}return e.length&&n.push(1,...e),n}var pt={};var xi=(function(t){return t[t.Important=1]=`Important`,t[t.DashCase=2]=`DashCase`,t})(xi||{});var QI;function Ef(t,n){return QI(t,n)}var Df=new Set;typeof document<`u`&&document?.documentElement?.getAnimations;var Uh=new WeakMap;function Jy(t){return t?t[ga]??t:null}var hr=new WeakSet;function XI(t,n,e){let i=Uh.get(t);if(!i||i.length===0)return;let a=n.parentNode,o=n.previousSibling,r=Jy(e);for(let s=i.length-1;s>=0;s--){let{el:l,declarationView:h}=i[s],p=l.parentNode;l===n?(i.splice(s,1),hr.add(l),l.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}}))):o&&l===o?(i.splice(s,1),l.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}})),l.parentNode?.removeChild(l)):p&&a&&p!==a&&(r===null||h===null||r===h)&&(i.splice(s,1),l.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}})),l.parentNode?.removeChild(l))}}function ZI(t,n,e){let i=Jy(e),a=Uh.get(t);a?a.some(o=>o.el===n)||a.push({el:n,declarationView:i}):Uh.set(t,[{el:n,declarationView:i}])}var Gd=(function(t){return t[t.CHANGE_DETECTION=0]=`CHANGE_DETECTION`,t[t.AFTER_NEXT_RENDER=1]=`AFTER_NEXT_RENDER`,t})(Gd||{});var ii=new N(``);var j_=new Set;function Ci(t){j_.has(t)||(j_.add(t),performance?.mark?.(`mark_feature_usage`,{detail:{feature:t}}))}var Wd=(()=>{class t{impl=null;execute(){this.impl?.execute()}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var If=[0,1,2,3];var kf=(()=>{class t{ngZone=f(Z);scheduler=f(gi);errorHandler=f(Tn,{optional:!0});sequences=new Set;deferredRegistrations=new Set;executing=!1;constructor(){f(ii,{optional:!0})}execute(){let e=this.sequences.size>0;e&&Ke(ze.AfterRenderHooksStart),this.executing=!0;for(let i of If)for(let a of this.sequences)if(!(a.erroredOrDestroyed||!a.hooks[i]))try{a.pipelinedValue=this.ngZone.runOutsideAngular(()=>this.maybeTrace(()=>{let o=a.hooks[i];return o(a.pipelinedValue)},a.snapshot))}catch(o){a.erroredOrDestroyed=!0,this.errorHandler?.handleError(o)}this.executing=!1;for(let i of this.sequences)i.afterRun(),i.once&&(this.sequences.delete(i),i.destroy());for(let i of this.deferredRegistrations)this.sequences.add(i);this.deferredRegistrations.size>0&&this.scheduler.notify(7),this.deferredRegistrations.clear(),e&&Ke(ze.AfterRenderHooksEnd)}register(e){let{view:i}=e;i!==void 0?((i[so]??=[]).push(e),mo(i),i[xe]|=8192):this.executing?this.deferredRegistrations.add(e):this.addSequence(e)}addSequence(e){this.sequences.add(e),this.scheduler.notify(7)}unregister(e){this.executing&&this.sequences.has(e)?(e.erroredOrDestroyed=!0,e.pipelinedValue=void 0,e.once=!0):(this.sequences.delete(e),this.deferredRegistrations.delete(e))}maybeTrace(e,i){return i?i.run(Gd.AFTER_NEXT_RENDER,e):e()}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var Ws=class{impl;hooks;view;once;snapshot;erroredOrDestroyed=!1;pipelinedValue=void 0;unregisterOnDestroy;constructor(n,e,i,a,o,r=null){this.impl=n,this.hooks=e,this.view=i,this.once=a,this.snapshot=r,this.unregisterOnDestroy=o?.onDestroy(()=>this.destroy())}afterRun(){this.erroredOrDestroyed=!1,this.pipelinedValue=void 0,this.snapshot?.dispose(),this.snapshot=null}destroy(){this.impl.unregister(this),this.unregisterOnDestroy?.();let n=this.view?.[so];n&&(this.view[so]=n.filter(e=>e!==this))}};function Rt(t,n){let e=n?.injector??f(se);return Ci(`NgAfterNextRender`),ek(t,e,n,!0)}function JI(t){return t instanceof Function?[void 0,void 0,t,void 0]:[t.earlyRead,t.write,t.mixedReadWrite,t.read]}function ek(t,n,e,i){let a=n.get(Wd);a.impl??=n.get(kf);let o=n.get(ii,null,{optional:!0}),r=e?.manualCleanup!==!0?n.get(Ae):null,s=n.get(pr,null,{optional:!0}),l=new Ws(a.impl,JI(t),s?.view,i,r,o?.snapshot(null));return a.impl.register(l),l}var $h=null;var e0=null;function z_(t,n,e,i){$h?.maybeQueueEnterAnimation(t,n,e,i)}function H_(t,n,e,i){$h===null?i(!1):$h.runLeaveAnimationsWithCallback(t,n,e,i)}function tk(t){e0?.initViewDetachAnimations(t)}function nk(t){e0?.clearViewDetachAnimations(t)}function fr(t,n,e,i,a,o,r,s){if(a!=null){let l,h=!1;An(a)?l=a:zi(a)&&(h=!0,a=a[Kn]);let p=$t(a);t===0&&i!==null?(z_(s,i,o,e),r==null?Yy(n,i,p):go(n,i,p,r||null,!0)):t===1&&i!==null?(go(n,i,p,r||null,!0),XI(o,p,s),hr.has(p)||z_(s,i,o,e)):t===2?(s?.[Kv]?.leave?.has(o.index)&&ZI(o,p,s),hr.delete(p),H_(s,o,e,g=>{if(hr.has(p)){hr.delete(p);return}Ky(n,p,h,g)})):t===3&&(hr.delete(p),H_(s,o,e,()=>{n.destroyNode(p)})),l!=null&&pk(n,t,e,l,o,i,r)}}function ik(t,n){t0(t,n),n[Kn]=null,n[Ut]=null}function ak(t,n,e,i,a,o){i[Kn]=a,i[Ut]=n,Yd(t,i,e,1,a,o)}function t0(t,n){n[Qn].changeDetectionScheduler?.notify(9),Yd(t,n,n[et],2,null,null)}function ok(t){let n=t[lr];if(!n)return _h(t[ve],t);for(;n;){let e=null;if(zi(n))e=n[lr];else{let i=n[xt];i&&(e=i)}if(!e){for(;n&&!n[Nn]&&n!==t;)zi(n)&&_h(n[ve],n),n=n[Et];n===null&&(n=t),zi(n)&&_h(n[ve],n),e=n&&n[Nn]}n=e}}function Mf(t,n){let e=t[co],i=e.indexOf(n);e.splice(i,1)}function qd(t,n){if(uo(n))return;let e=n[et];e.destroyNode&&Yd(t,n,e,3,null,null),ok(n)}function _h(t,n){if(uo(n))return;let e=ge(null);try{n[xe]&=-129,n[xe]|=256,n[yn]&&da(n[yn]),sk(t,n),rk(t,n),n[ve].type===1&&n[et].destroy();let i=n[ba];if(i!==null&&An(n[Et])){i!==n[Et]&&Mf(i,n);let a=n[vi];a!==null&&a.detachView(t)}Ah(n)}finally{ge(e)}}function rk(t,n){let e=t.cleanup,i=n[sr];if(e!==null)for(let r=0;r<e.length-1;r+=2)if(typeof e[r]==`string`){let s=e[r+3];s>=0?i[s]():i[-s].unsubscribe(),r+=2}else{let s=i[e[r+1]];e[r].call(s)}i!==null&&(n[sr]=null);let a=n[Fi];if(a!==null){n[Fi]=null;for(let r=0;r<a.length;r++){let s=a[r];s()}}let o=n[Li];if(o!==null){n[Li]=null;for(let r of o)r.destroy()}}function sk(t,n){let e;if(t!=null&&(e=t.destroyHooks)!=null)for(let i=0;i<e.length;i+=2){let a=n[e[i]];if(!(a instanceof vo)){let o=e[i+1];if(Array.isArray(o))for(let r=0;r<o.length;r+=2){let s=a[o[r]],l=o[r+1];Ke(ze.LifecycleHookStart,s,l);try{l.call(s)}finally{Ke(ze.LifecycleHookEnd,s,l)}}else{Ke(ze.LifecycleHookStart,a,o);try{o.call(a)}finally{Ke(ze.LifecycleHookEnd,a,o)}}}}}function n0(t,n,e){if(n===null)throw new q(510,!1);return lk(t,n.parent,e)}function lk(t,n,e){let i=n;for(;i!==null&&i.type&168;)n=i,i=n.parent;if(i===null)return e[Kn];if(_i(i)){let{encapsulation:a}=t.data[i.directiveStart+i.componentOffset];if(a===ei.None||a===ei.Emulated)return null}return Cn(i,e)}function i0(t,n,e){return dk(t,n,e)}function ck(t,n,e){return t.type&40?Cn(t,e):null}var dk=ck;function Tf(t,n,e,i){let a=n0(t,i,n),o=n[et],s=i0(i.parent||n[Ut],i,n);if(a!=null)if(Array.isArray(e))for(let l=0;l<e.length;l++)V_(o,a,e[l],s,!1);else V_(o,a,e,s,!1)}function Us(t,n){if(n!==null){let e=n.type;if(e&3)return Cn(n,t);if(e&4)return Gh(-1,t[n.index]);if(e&8){let i=n.child;if(i!==null)return Us(t,i);{let a=t[n.index];return An(a)?Gh(-1,a):$t(a)}}else{if(e&128)return Us(t,n.next);if(e&32)return Ef(n,t)()||$t(t[n.index]);{let i=a0(t,n);if(i!==null){if(Array.isArray(i))return i[0];return Us(Vi(t[Xt]),i)}else return Us(t,n.next)}}}return null}function a0(t,n){if(n!==null){let i=t[Xt][Ut],a=n.projection;return i.projection[a]}return null}function Gh(t,n){let e=xt+t+1;if(e<n.length){let i=n[e],a=i[ve].firstChild;if(a!==null)return Us(i,a)}return n[lo]}function Nf(t,n,e,i,a,o,r){for(;e!=null;){let s=i[ji];if(e.type===128){e=e.next;continue}let l=i[e.index],h=e.type;if(r&&n===0&&(l&&vr($t(l),i),e.flags|=2),!Ud(e))if(h&8)Nf(t,n,e.child,i,a,o,!1),fr(n,t,s,a,l,e,o,i);else if(h&32){let p=Ef(e,i),g;for(;g=p();)fr(n,t,s,a,g,e,o,i);fr(n,t,s,a,l,e,o,i)}else h&16?o0(t,n,i,e,a,o):fr(n,t,s,a,l,e,o,i);e=r?e.projectionNext:e.next}}function Yd(t,n,e,i,a,o){t.type===3?uk(e,i,n,a,o):Nf(e,i,t.firstChild,n,a,o,!1)}function uk(t,n,e,i,a){let r=e[ve].firstChild,s=r.next,l=$t(e[r.index]),h=$t(e[s.index]),p=s.index+1,g=e[p];if(n===1||n===0)i!==null&&(g&&g.hasChildNodes()?go(t,i,g,a,!0):(go(t,i,l,a,!0),go(t,i,h,a,!0)));else if(n===2){if(g||(g=document.createDocumentFragment(),e[p]=g),l&&l.parentNode===g)return;let _=l;for(;_!==null;){let b=_.nextSibling;if(g.appendChild(_),_===h)break;_=b}}}function mk(t,n,e){let i=n[et];o0(i,0,n,e,n0(t,e,n),i0(e.parent||n[Ut],e,n))}function o0(t,n,e,i,a,o){let r=e[Xt],l=r[Ut].projection[i.projection];if(Array.isArray(l))for(let h=0;h<l.length;h++){let p=l[h];fr(n,t,e[ji],a,p,i,o,e)}else{let h=l,p=r[Et];My(i)&&(h.flags|=128),Nf(t,n,h,p,a,o,!0)}}function pk(t,n,e,i,a,o,r){let s=i[lo];if(s!==$t(i)&&fr(n,t,e,o,s,a,r),(i[xe]&4)===0)for(let h=xt;h<i.length;h++){let p=i[h];Yd(p[ve],p,t,n,o,s)}}function hk(t,n,e,i,a){if(n)a?t.addClass(e,i):t.removeClass(e,i);else{let o=i.indexOf(`-`)===-1?void 0:xi.DashCase;a==null?t.removeStyle(e,i,o):(typeof a==`string`&&a.endsWith(`!important`)&&(a=a.slice(0,-10),o|=xi.Important),t.setStyle(e,i,a,o))}}function Af(t,n,e,i,a,o,r,s,l,h,p){let g=rt+i,_=g+a,b=fk(g,_),y=typeof h==`function`?h():h;return b[ve]={type:t,blueprint:b,template:e,queries:null,viewQuery:s,declTNode:n,data:b.slice().fill(null,g),bindingStartIndex:g,expandoStartIndex:_,hostBindingOpCodes:null,firstCreatePass:!0,firstUpdatePass:!0,staticViewQueries:!1,staticContentQueries:!1,preOrderHooks:null,preOrderCheckHooks:null,contentHooks:null,contentCheckHooks:null,viewHooks:null,viewCheckHooks:null,destroyHooks:null,cleanup:null,contentQueries:null,components:null,directiveRegistry:typeof o==`function`?o():o,pipeRegistry:typeof r==`function`?r():r,firstChild:null,schemas:l,consts:y,incompleteFirstPass:!1,ssrId:p}}function fk(t,n){let e=[];for(let i=0;i<n;i++)e.push(i<t?null:pt);return e}function gk(t){let n=t.tView;return n===null||n.incompleteFirstPass?t.tView=Af(1,null,t.template,t.decls,t.vars,t.directiveDefs,t.pipeDefs,t.viewQuery,t.schemas,t.consts,t.id):n}function Rf(t,n,e,i,a,o,r,s,l,h,p){let g=n.blueprint.slice();return g[Kn]=a,g[xe]=i|1228,(h!==null||t&&t[xe]&2048)&&(g[xe]|=2048),Kp(g),g[Et]=g[ga]=t,g[yt]=e,g[Qn]=r||t&&t[Qn],g[et]=s||t&&t[et],g[ji]=l||t&&t[ji]||null,g[Ut]=o,g[Rs]=aI(),g[oo]=p,g[$p]=h,g[Xt]=n.type==2?t[Xt]:g,g}function bk(t,n,e){let i=Cn(n,t),a=gk(e),o=t[Qn].rendererFactory,r=Of(t,Rf(t,a,null,r0(e),i,n,null,o.createRenderer(i,e),null,null,null));return t[n.index]=r}function r0(t){let n=16;return t.signals?n=4096:t.onPush&&(n=64),n}function s0(t,n,e,i){if(e===0)return-1;let a=n.length;for(let o=0;o<e;o++)n.push(i),t.blueprint.push(i),t.data.push(null);return a}function Of(t,n){return t[lr]?t[Up][Nn]=n:t[lr]=n,t[Up]=n,n}function m(t=1){l0(Xe(),ce(),Zt()+t,!1)}function l0(t,n,e,i){if(!i)if((n[xe]&3)===3){let o=t.preOrderCheckHooks;o!==null&&Sd(n,o,e)}else{let o=t.preOrderHooks;o!==null&&wd(n,o,0,e)}_a(e)}var tl=(function(t){return t[t.None=0]=`None`,t[t.SignalBased=1]=`SignalBased`,t[t.HasDecoratorInputTransform=2]=`HasDecoratorInputTransform`,t})(tl||{});function _o(t,n,e,i){let a=ge(null);try{let[o,r,s]=t.inputs[e],l=null;(r&tl.SignalBased)!==0&&(l=n[o][St]),l!==null&&l.transformFn!==void 0?i=l.transformFn(i):s!==null&&(i=s.call(n,i)),t.setInput!==null?t.setInput(n,l,i,e,o):py(n,l,o,i)}finally{ge(a)}}function c0(t,n,e,i,a){let o=Zt(),r=i&2;try{_a(-1),r&&n.length>rt&&l0(t,n,rt,!1);Ke(r?ze.TemplateUpdateStart:ze.TemplateCreateStart,a,e),e(i,a)}finally{_a(o);Ke(r?ze.TemplateUpdateEnd:ze.TemplateCreateEnd,a,e)}}function Kd(t,n,e){Sk(t,n,e),(e.flags&64)===64&&wk(t,n,e)}function nl(t,n,e=Cn){let i=n.localNames;if(i!==null){let a=n.index+1;for(let o=0;o<i.length;o+=2){let r=i[o+1],s=r===-1?e(n,t):t[r];t[a++]=s}}}function vk(t,n,e,i){let o=i.get(Vy,Ly)||e===ei.ShadowDom||e===ei.ExperimentalIsolatedShadowDom;return t.selectRootElement(n,o)}function xk(t){return t===`class`?`className`:t===`for`?`htmlFor`:t===`formaction`?`formAction`:t===`innerHtml`?`innerHTML`:t===`readonly`?`readOnly`:t===`tabindex`?`tabIndex`:t}function Ck(t,n,e,i,a,o){let r=n[ve];if(Qd(t,r,n,e,i)){_i(t)&&u0(n,t.index);return}t.type&3&&(e=xk(e)),d0(t,n,e,i,a,o)}function d0(t,n,e,i,a,o){if(t.type&3){let r=Cn(t,n);i=o!=null?o(i,t.value||``,e):i,a.setProperty(r,e,i)}else t.type&12}function u0(t,n){let e=Rn(n,t);e[xe]&16||(e[xe]|=64)}function Sk(t,n,e){let i=e.directiveStart,a=e.directiveEnd;_i(e)&&bk(n,e,t.data[i+e.componentOffset]),t.firstCreatePass||Nd(e,n);let o=e.initialInputs;for(let r=i;r<a;r++){let s=t.data[r],l=Gs(n,t,r,e);if(vr(l,n),o!==null&&Ik(n,r-i,l,s,e,o),Xn(s)){let h=Rn(e.index,n);h[yt]=Gs(n,t,r,e)}}}function wk(t,n,e){let i=e.directiveStart,a=e.directiveEnd,o=e.index,r=c_();try{_a(o);for(let s=i;s<a;s++){let l=t.data[s],h=n[s];md(s),(l.hostBindings!==null||l.hostVars!==0||l.hostAttrs!==null)&&Ek(l,h)}}finally{_a(-1),md(r)}}function Ek(t,n){t.hostBindings!==null&&t.hostBindings(1,n)}function Pf(t,n){let e=t.directiveRegistry,i=null;if(e)for(let a=0;a<e.length;a++){let o=e[a];Zy(n,o.selectors,!1)&&(i??=[],Xn(o)?i.unshift(o):i.push(o))}return i}function Dk(t,n,e,i,a,o){let r=Cn(t,n);m0(n[et],r,o,t.value,e,i,a)}function m0(t,n,e,i,a,o,r){if(o==null)r?.(o,i||``,a),t.removeAttribute(n,a,e);else{let s=r==null?$e(o):r(o,i||``,a);t.setAttribute(n,a,s,e)}}function Ik(t,n,e,i,a,o){let r=o[n];if(r!==null)for(let s=0;s<r.length;s+=2){let l=r[s],h=r[s+1];_o(i,e,l,h)}}function Ff(t,n,e,i,a){let o=rt+e,r=n[ve],s=a(r,n,t,i,e);n[o]=s,po(t,!0);let l=t.type===2;return l?(Qy(n[et],s,t),(i_()===0||dr(t))&&vr(s,n),a_()):vr(s,n),gd()&&(!l||!Ud(t))&&Tf(r,n,s,t),t}function Lf(t){let n=t;return ah()?oh():(n=n.parent,po(n,!1)),n}function kk(t,n){let e=t[ji];if(!e)return;let i;try{i=e.get(Ui,null)}catch{i=null}i?.(n)}function Qd(t,n,e,i,a){let o=t.inputs?.[i],r=t.hostDirectiveInputs?.[i],s=!1;if(r)for(let l=0;l<r.length;l+=2){let h=r[l],p=r[l+1],g=n.data[h];_o(g,e[h],p,a),s=!0}if(o)for(let l of o){let h=e[l],p=n.data[l];_o(p,h,i,a),s=!0}return s}function Mk(t,n,e,i,a,o){let r=null,s=null,l=null,h=!1,p=t.directiveToIndex.get(i.type);if(typeof p==`number`?r=p:[r,s,l]=p,s!==null&&l!==null&&t.hostDirectiveInputs&&Object.hasOwn(t.hostDirectiveInputs,a)){let g=t.hostDirectiveInputs[a];for(let _=0;_<g.length;_+=2){let b=g[_];if(b>=s&&b<=l){let y=n.data[b],I=g[_+1];_o(y,e[b],I,o),h=!0}else if(b>l)break}}return r!==null&&Object.hasOwn(i.inputs,a)&&(_o(i,e[r],a,o),h=!0),h}function Tk(t,n){let e=Rn(n,t),i=e[ve];Nk(i,e);let a=e[Kn];a!==null&&e[oo]===null&&(e[oo]=By(a,e[ji])),Ke(ze.ComponentStart);try{Vf(i,e,e[yt])}finally{Ke(ze.ComponentEnd,e[yt])}}function Nk(t,n){for(let e=n.length;e<t.blueprint.length;e++)n.push(t.blueprint[e])}function Vf(t,n,e){hd(n);try{let i=t.viewQuery;i!==null&&Oh(1,i,e);let a=t.template;a!==null&&c0(t,n,a,1,e),t.firstCreatePass&&(t.firstCreatePass=!1),n[vi]?.finishViewCreation(t),t.staticContentQueries&&jy(t,n),t.staticViewQueries&&Oh(2,t.viewQuery,e);let o=t.components;o!==null&&Ak(n,o)}catch(i){throw t.firstCreatePass&&(t.incompleteFirstPass=!0,t.firstCreatePass=!1),i}finally{n[xe]&=-5,fd()}}function Ak(t,n){for(let e=0;e<n.length;e++)Tk(t,n[e])}function il(t,n,e,i){let a=ge(null);try{let o=n.tView,l=Rf(t,o,e,t[xe]&4096?4096:16,null,n,null,null,i?.injector??null,i?.embeddedViewInjector??null,i?.dehydratedView??null);l[ba]=t[n.index];let p=t[vi];return p!==null&&(l[vi]=p.createEmbeddedView(o)),Vf(o,l,e),l}finally{ge(a)}}function _r(t,n){return!n||n.firstChild===null||My(t)}function qs(t,n,e,i,a=!1){if(t.type===3){let o=t.firstChild,r=o.next,s=$t(n[o.index]),l=$t(n[r.index]),h=s;for(;h!==null&&(i.push(h),h!==l);)h=h.nextSibling;return i}for(;e!==null;){if(e.type===128){e=a?e.projectionNext:e.next;continue}let o=n[e.index];if(o!==null)if(An(o)){let s=o[lo];s!==o[Kn]&&i.push($t(o)),o[xe]&4||p0(o,i),i.push(s)}else i.push($t(o));let r=e.type;if(r&8)qs(t,n,e.child,i);else if(r&32){let s=Ef(e,n),l;for(;l=s();)i.push(l)}else if(r&16){let s=a0(n,e);if(Array.isArray(s))i.push(...s);else{let l=Vi(n[Xt]);qs(l[ve],l,s,i,!0)}}e=a?e.projectionNext:e.next}return i}function p0(t,n){for(let e=xt;e<t.length;e++){let i=t[e],a=i[ve].firstChild;a!==null&&qs(i[ve],i,a,n)}}function h0(t){if(t[so]!==null){for(let n of t[so])n.impl.addSequence(n);t[so].length=0}}var f0=[];function Rk(t){return t[yn]??Ok(t)}function Ok(t){let n=f0.pop()??Object.create(Fk);return n.lView=t,n}function Pk(t){t.lView[yn]!==t&&(t.lView=null,f0.push(t))}var Fk=s(r({},$a),{consumerIsAlwaysLive:!0,kind:`template`,consumerMarkedDirty:t=>{mo(t.lView)},consumerOnSignalRead(){this.lView[yn]=this}});function Lk(t){let n=t[yn]??Object.create(Vk);return n.lView=t,n}var Vk=s(r({},$a),{consumerIsAlwaysLive:!0,kind:`template`,consumerMarkedDirty:t=>{let n=Vi(t.lView);for(;n&&!g0(n[ve]);)n=Vi(n);n&&Qp(n)},consumerOnSignalRead(){this.lView[yn]=this}});function g0(t){return t.type!==2}function b0(t){if(t[Li]===null)return;let n=!0;for(;n;){let e=!1;for(let i of t[Li])if(i.dirty&&(e=!0,i.zone===null||Zone.current===i.zone?i.run():i.zone.run(()=>i.run()),t[Li]===null))return;n=e&&!!(t[xe]&8192)}}var Bk=100;function v0(t,n=0){let i=t[Qn].rendererFactory;i.begin?.();try{jk(t,n)}finally{i.end?.()}}function jk(t,n){let e=sh();try{ws(!0),Wh(t,n);let i=0;for(;Ls(t);){if(i===Bk)throw new q(103,!1);i++,Wh(t,1)}}finally{ws(e)}}function zk(t,n,e,i){if(uo(n))return;let a=n[xe];hd(n);let s=!0,l=null,h=null;g0(t)?(h=Rk(n),l=ca(h)):lc()===null?(s=!1,h=Lk(n),l=ca(h)):n[yn]&&(da(n[yn]),n[yn]=null);try{Kp(n),r_(t.bindingStartIndex),e!==null&&c0(t,n,e,2,i);let p=(a&3)===3;if(p){let b=t.preOrderCheckHooks;b!==null&&Sd(n,b,null)}else{let b=t.preOrderHooks;b!==null&&wd(n,b,0,null),bh(n,0)}if(Hk(n),b0(n),_0(n,0),t.contentQueries!==null&&jy(t,n),true)if(p){let b=t.contentCheckHooks;b!==null&&Sd(n,b)}else{let b=t.contentHooks;b!==null&&wd(n,b,1),bh(n,1)}$k(t,n);let g=t.components;g!==null&&x0(n,g,0);let _=t.viewQuery;if(_!==null&&Oh(2,_,i),true)if(p){let b=t.viewCheckHooks;b!==null&&Sd(n,b)}else{let b=t.viewHooks;b!==null&&wd(n,b,2),bh(n,2)}if(t.firstUpdatePass===!0&&(t.firstUpdatePass=!1),n[od]){for(let b of n[od])b();n[od]=null}h0(n),n[xe]&=-73}catch(p){let g=!1,_=p,b=n;for(;b!==null;){if(An(b)){b=b[Et];continue}let y=b[cr];if(y)try{let I=n[Xt][yt],ie={declarationInstance:I,declarationType:I?.constructor,caughtBy:y};y(y_(_),ie),g=!0;break}catch(I){_=I}b=b[Et]}if(!g)throw mo(n),_}finally{h!==null&&(Ga(h,l),s&&Pk(h)),fd()}}function _0(t,n){for(let e=Ny(t);e!==null;e=Ay(e))for(let i=xt;i<e.length;i++){let a=e[i];y0(a,n)}}function Hk(t){for(let n=Ny(t);n!==null;n=Ay(n)){if(!(n[xe]&2))continue;let e=n[co];for(let i=0;i<e.length;i++){let a=e[i];Qp(a)}}}function Uk(t,n,e){Ke(ze.ComponentStart);let i=Rn(n,t);try{y0(i,e)}finally{Ke(ze.ComponentEnd,i[yt])}}function y0(t,n){sd(t)&&Wh(t,n)}function Wh(t,n){let i=t[ve],a=t[xe],o=t[yn],r=!!(n===0&&a&16);if(r||=!!(a&64&&n===0),r||=!!(a&1024),r||=!!(o?.dirty&&Wo(o)),r||=!1,o&&(o.dirty=!1),t[xe]&=-9217,r)zk(i,t,i.template,t[yt]);else if(a&8192){let s=ge(null);try{b0(t),_0(t,1);let l=i.components;l!==null&&x0(t,l,1),h0(t)}finally{ge(s)}}}function x0(t,n,e){for(let i=0;i<n.length;i++)Uk(t,n[i],e)}function $k(t,n){let e=t.hostBindingOpCodes;if(e!==null)try{for(let i=0;i<e.length;i++){let a=e[i];if(a<0)_a(~a);else{let o=a,r=e[++i],s=e[++i];l_(r,o);let l=n[o];Ke(ze.HostBindingsUpdateStart,l);try{s(2,l)}finally{Ke(ze.HostBindingsUpdateEnd,l)}}}}finally{_a(-1)}}function Bf(t,n){let e=sh()?64:1088;for(t[Qn].changeDetectionScheduler?.notify(n);t;){t[xe]|=e;let i=Vi(t);if(ur(t)&&!i)return t;t=i}return null}function C0(t,n,e,i){return[t,!0,0,n,null,i,null,e,null,null]}function S0(t,n){let e=xt+n;if(e<t.length)return t[e]}function al(t,n,e,i=!0){let a=n[ve];if(Gk(a,n,t,e),i){let r=Gh(e,t),s=n[et],l=s.parentNode(t[lo]);l!==null&&ak(a,t[Ut],s,n,l,r)}let o=n[oo];o!==null&&o.firstChild!==null&&(o.firstChild=null)}function w0(t,n){let e=Ys(t,n);return e!==void 0&&qd(e[ve],e),e}function Ys(t,n){if(t.length<=xt)return;let e=xt+n,i=t[e];if(i){let a=i[ba];a!==null&&a!==t&&Mf(a,i),n>0&&(t[e-1][Nn]=i[Nn]);let o=Ns(t,xt+n);ik(i[ve],i);let r=o[vi];r!==null&&r.detachView(o[ve]),i[Et]=null,i[Nn]=null,i[xe]&=-129}return i}function Gk(t,n,e,i){let a=xt+i,o=e.length;i>0&&(e[a-1][Nn]=n),i<o-xt?(n[Nn]=e[a],Vp(e,xt+i,n)):(e.push(n),n[Nn]=null),n[Et]=e;let r=n[ba];r!==null&&e!==r&&E0(r,n);let s=n[vi];s!==null&&s.insertView(t),ld(n),n[xe]|=128}function E0(t,n){let e=t[co],i=n[Et];if(zi(i))t[xe]|=2;else{let a=i[Et][Xt];n[Xt]!==a&&(t[xe]|=2)}e===null?t[co]=[n]:e.push(n)}var Sa=class{_lView;_cdRefInjectingView;_appRef=null;_attachedToViewContainer=!1;exhaustive;get rootNodes(){let n=this._lView,e=n[ve];return qs(e,n,e.firstChild,[])}constructor(n,e){this._lView=n,this._cdRefInjectingView=e}get context(){return this._lView[yt]}set context(n){this._lView[yt]=n}get destroyed(){return uo(this._lView)}destroy(){if(this._appRef)this._appRef.detachView(this);else if(this._attachedToViewContainer){let n=this._lView[Et];if(An(n)){let e=n[Os],i=e?e.indexOf(this):-1;i>-1&&(Ys(n,i),Ns(e,i))}this._attachedToViewContainer=!1}qd(this._lView[ve],this._lView)}onDestroy(n){cd(this._lView,n)}markForCheck(){Bf(this._cdRefInjectingView||this._lView,4)}detach(){this._lView[xe]&=-129}reattach(){ld(this._lView),this._lView[xe]|=128}detectChanges(){this._lView[xe]|=1024,v0(this._lView)}checkNoChanges(){}attachToViewContainerRef(){if(this._appRef)throw new q(902,!1);this._attachedToViewContainer=!0}detachFromAppRef(){this._appRef=null;let n=ur(this._lView),e=this._lView[ba];e!==null&&!n&&Mf(e,this._lView),t0(this._lView[ve],this._lView)}attachToAppRef(n){if(this._attachedToViewContainer)throw new q(902,!1);this._appRef=n;let e=ur(this._lView),i=this._lView[ba];i!==null&&!e&&E0(i,this._lView),ld(this._lView)}};var Vt=(()=>{class t{_declarationLView;_declarationTContainer;elementRef;static __NG_ELEMENT_ID__=Wk;constructor(e,i,a){this._declarationLView=e,this._declarationTContainer=i,this.elementRef=a}get ssrId(){return this._declarationTContainer.tView?.ssrId||null}createEmbeddedView(e,i){return this.createEmbeddedViewImpl(e,i)}createEmbeddedViewImpl(e,i,a){return new Sa(il(this._declarationLView,this._declarationTContainer,e,{embeddedViewInjector:i,dehydratedView:a}))}}return t})();function Wk(){return Xd(Tt(),ce())}function Xd(t,n){return t.type&4?new Vt(n,t,Er(t,n)):null}function xo(t,n,e,i,a){let o=t.data[n];if(o===null)o=qk(t,n,e,i,a),s_()&&(o.flags|=32);else if(o.type&64){o.type=e,o.value=i,o.attrs=a;let r=o_();o.injectorIndex=r===null?-1:r.injectorIndex}return po(o,!0),o}function qk(t,n,e,i,a){let o=ih(),r=ah(),s=r?o:o&&o.parent,l=t.data[n]=Kk(t,s,e,n,i,a);return Yk(t,l,o,r),l}function Yk(t,n,e,i){t.firstChild===null&&(t.firstChild=n),e!==null&&(i?e.child==null&&n.parent!==null&&(e.child=n):e.next===null&&(e.next=n,n.prev=e))}function Kk(t,n,e,i,a,o){let r=n?n.injectorIndex:-1,s=0;return eh()&&(s|=128),{type:e,index:i,insertBeforeIndex:null,injectorIndex:r,directiveStart:-1,directiveEnd:-1,directiveStylingLast:-1,componentOffset:-1,controlDirectiveIndex:-1,customControlIndex:-1,propertyBindings:null,flags:s,providerIndexes:0,value:a,namespace:dh(),attrs:o,mergedAttrs:null,localNames:null,initialInputs:null,inputs:null,hostDirectiveInputs:null,outputs:null,hostDirectiveOutputs:null,directiveToIndex:null,tView:null,next:null,prev:null,projectionNext:null,child:null,parent:n,projection:null,styles:null,stylesWithoutHost:null,residualStyles:void 0,classes:null,classesWithoutHost:null,residualClasses:void 0,classBindings:0,styleBindings:0}}function Qk(t){let n=t[Gp]??[],i=t[Et][et],a=[];for(let o of n)o.data[Fy]!==void 0?a.push(o):Xk(o,i);t[Gp]=a}function Xk(t,n){let e=0,i=t.firstChild;if(i){let a=t.data[Py];for(;e<a;){let o=i.nextSibling;Ky(n,i,!1),i=o,e++}}}var Zk=()=>null;var Jk=()=>null;function Ad(t,n){return Zk(t,n)}function D0(t,n,e){return Jk(t,n,e)}var I0=class{};var Nt=class{};var Pe=class{destroyNode=null;static __NG_ELEMENT_ID__=()=>eM()};function eM(){let t=ce(),e=Rn(Tt().index,t);return(zi(e)?e:t)[et]}var k0=(()=>{class t{static ɵprov=be({token:t,providedIn:`root`,factory:()=>null})}return t})();function M0(t){return t.debugInfo?.className||t.type.name||null}var Dd={};var Rd=class{injector;parentInjector;constructor(n,e){this.injector=n,this.parentInjector=e}get(n,e,i){let a=this.injector.get(n,Dd,i);return a!==Dd||e===Dd?a:this.parentInjector.get(n,e,i)}};function jf(t,n,e){return t[n]=e}function tM(t,n){return t[n]}function fn(t,n,e){if(e===pt)return!1;let i=t[n];return Object.is(i,e)?!1:(t[n]=e,!0)}function yr(t,n,e,i){let a=fn(t,n,e);return fn(t,n+1,i)||a}function nM(t,n,e,i,a){let o=yr(t,n,e,i);return fn(t,n+2,a)||o}function Ks(t,n,e,i,a,o){let r=yr(t,n,e,i);return yr(t,n+2,a,o)||r}function bo(t,n,e){return function i(a){let o=i.__ngNativeEl__;o!==void 0&&rI(a,o);Bf(_i(t)?Rn(t.index,n):n,5);let s=n[yt],l=$_(n,s,e,a),h=i.__ngNextListenerFn__;for(;h;)l=$_(n,s,h,a)&&l,h=h.__ngNextListenerFn__;return l}}function $_(t,n,e,i){let a=ge(null);try{return Ke(ze.OutputStart,n,e),e(i)!==!1}catch(o){return kk(t,o),!1}finally{Ke(ze.OutputEnd,n,e),ge(a)}}function zf(t,n,e,i,a,o,r,s){let l=dr(t),h=!1,p=null;if(!i&&l&&(p=aM(n,e,o,t.index)),p!==null){let g=p.__ngLastListenerFn__||p;g.__ngNextListenerFn__=r,p.__ngLastListenerFn__=r,h=!0}else{let g=Cn(t,e),_=i?i(g):g;i||(s.__ngNativeEl__=g);let b=a.listen(_,o,s);if(!iM(o))T0(i?I=>i($t(I[t.index])):t.index,n,e,o,s,b,!1)}return h}function iM(t){return t.startsWith(`animation`)||t.startsWith(`transition`)}function aM(t,n,e,i){let a=t.cleanup;if(a!=null)for(let o=0;o<a.length-1;o+=2){let r=a[o];if(r===e&&a[o+1]===i){let s=n[sr],l=a[o+2];return s&&s.length>l?s[l]:null}typeof r==`string`&&(o+=2)}return null}function T0(t,n,e,i,a,o,r){let s=n.firstCreatePass?Zp(n):null,l=Xp(e),h=l.length;l.push(a,o),s&&s.push(i,t,h,(h+1)*(r?-1:1))}function G_(t,n,e,i,a){let o=null,r=null,s=null,l=!1,h=t.directiveToIndex.get(e.type);if(typeof h==`number`?o=h:[o,r,s]=h,r!==null&&s!==null&&t.hostDirectiveOutputs&&Object.hasOwn(t.hostDirectiveOutputs,i)){let p=t.hostDirectiveOutputs[i];for(let g=0;g<p.length;g+=2){let _=p[g];if(_>=r&&_<=s)l=!0,Od(t,n,_,p[g+1],i,a);else if(_>s)break}}return Object.hasOwn(e.outputs,i)&&(l=!0,Od(t,n,o,i,i,a)),l}function Od(t,n,e,i,a,o){let r=n[e],s=n[ve],g=r[s.data[e].outputs[i]].subscribe(o);T0(t.index,s,n,a,o,g,!0)}function Dr(){oM()}function oM(){let t=ce(),n=Xe(),e=Tt();if(n.firstCreatePass&&sM(n,e),e.controlDirectiveIndex===-1)return;Ci(`NgSignalForms`);let i=t[e.controlDirectiveIndex];n.data[e.controlDirectiveIndex].controlDef.create(i,new Pd(t,n,e))}function Ir(){rM()}function rM(){let t=ce(),n=Xe(),e=mr();if(e.controlDirectiveIndex===-1)return;let i=n.data[e.controlDirectiveIndex].controlDef,a=t[e.controlDirectiveIndex];i.update(a,new Pd(t,n,e))}var Pd=class{lView;tView;tNode;hasPassThrough;constructor(n,e,i){this.lView=n,this.tView=e,this.tNode=i,this.hasPassThrough=!!(i.flags&4096)}get customControl(){return this.tNode.customControlIndex!==-1?this.lView[this.tNode.customControlIndex]:void 0}get nativeElement(){return Cn(this.tNode,this.lView)}get descriptor(){return`<${this.tNode.value}>`}listenToCustomControlOutput(n,e){let i=this.tView.data[this.tNode.customControlIndex];G_(this.tNode,this.lView,i,n,bo(this.tNode,this.lView,e))}listenToCustomControlModel(n){let e=this.tNode.flags&1024?`valueChange`:`checkedChange`,i=this.tView.data[this.tNode.customControlIndex];G_(this.tNode,this.lView,i,e,bo(this.tNode,this.lView,n))}listenToDom(n,e){zf(this.tNode,this.tView,this.lView,void 0,this.lView[et],n,e,bo(this.tNode,this.lView,e))}setInputOnDirectives(n,e,i){let a=this.tNode.inputs?.[n],o=this.tNode.hostDirectiveInputs?.[n];if(!a&&!o)return!1;let r=!1;if(a)for(let s of a){if(s===this.tNode.controlDirectiveIndex)continue;let l=this.lView[s],h=this.tView.data[s];(!i||i(q_(l,h,n)))&&(_o(h,l,n,e),r=!0)}if(o)for(let s=0;s<o.length;s+=2){let l=o[s];if(l===this.tNode.controlDirectiveIndex)continue;let h=this.lView[l],p=o[s+1],g=this.tView.data[l];(!i||i(q_(h,g,n)))&&(_o(g,h,p,e),r=!0)}return r}setCustomControlModelInput(n){let e=this.tView.data[this.tNode.customControlIndex],i=this.tNode.flags&1024?`value`:`checked`;Mk(this.tNode,this.tView,this.lView,e,i,n)}customControlHasInput(n){if(this.tNode.customControlIndex===-1)return!1;let e=this.tView.data[this.tNode.customControlIndex];return(e.signalFormsInputPresence??=this._buildCustomControlInputCache(e))[n]===!0}_buildCustomControlInputCache(n){let e={};for(let i in n.inputs)e[i]=!0;if(n.hostDirectives!==null){let i=[...n.hostDirectives];for(;i.length>0;){let a=i.shift();if(typeof a!=`function`){for(let r in a.inputs)e[a.inputs[r]]=!0;let o=W_(a.directive);o!==null&&i.push(...o);continue}for(let o of a()){if(typeof o==`function`)continue;if(o.inputs)for(let s=0;s<o.inputs.length;s+=2){let l=o.inputs[s+1]||o.inputs[s];e[l]=!0}let r=W_(o.directive);r!==null&&i.push(...r)}}}return e}};function W_(t){return typeof t==`function`&&`ɵdir`in t?t.ɵdir.hostDirectives??null:null}function q_(t,n,e){if(!n.inputs||!Object.hasOwn(n.inputs,e))return;let[i,a]=n.inputs[e];if((a&tl.SignalBased)!==0){let r=t[i][St];return r.value===wr?void 0:r.value}return t[i]}function sM(t,n,e){for(let a=n.directiveStart;a<n.directiveEnd;a++)if(t.data[a].controlDef){n.controlDirectiveIndex=a;break}if(n.controlDirectiveIndex===-1)return;let i=t.data[n.controlDirectiveIndex].controlDef;if(i.passThroughInput&&(n.inputs?.[i.passThroughInput]?.length??0)>1){n.flags|=4096;return}lM(t,n)}function lM(t,n){for(let e=n.directiveStart;e<n.directiveEnd;e++){let i=t.data[e];if(!(n.directiveToIndex&&!n.directiveToIndex.has(i.type))){if(Y_(i,`value`)){n.flags|=1024,n.customControlIndex=e;return}if(Y_(i,`checked`)){n.flags|=2048,n.customControlIndex=e;return}}}if(n.hostDirectiveInputs!==null&&n.hostDirectiveOutputs!==null&&n.directiveToIndex!==null){let e=(i,a)=>{let o=n.hostDirectiveInputs[i],r=n.hostDirectiveOutputs[i+`Change`];if(!o||!r)return!1;for(let s=0;s<o.length;s+=2){let l=o[s];for(let h=0;h<r.length;h+=2)if(l===r[h])for(let g of n.directiveToIndex.values()){if(!Array.isArray(g))continue;let[_,b,y]=g;if(l>=b&&l<=y)return n.flags|=a,n.customControlIndex=_,!0}}return!1};if(e(`value`,1024)||e(`checked`,2048))return}}function Y_(t,n){return cM(t,n)&&dM(t,n+`Change`)}function cM(t,n){return n in t.inputs}function dM(t,n){return n in t.outputs}var qh=Symbol(`BINDING`);var Co=new N(``);function Fd(t,n,e){let i=e?t.styles:null,a=e?t.classes:null,o=0;if(n!==null)for(let r=0;r<n.length;r++){let s=n[r];if(typeof s==`number`)o=s;else if(o==1)a=Qc(a,s);else if(o==2){let l=s,h=n[++r];i=Qc(i,l+`: `+h+`;`)}}e?t.styles=i:t.stylesWithoutHost=i,e?t.classes=a:t.classesWithoutHost=a}function He(t,n=0){let e=ce();if(e===null)return Se(t,n);return Ey(Tt(),e,Lt(t),n)}function Hf(){throw new Error(`invalid`)}function N0(t,n,e,i,a){let o=i===null?null:{"":-1},r=a(t,e);if(r!==null){let s=r,l=null,h=null;for(let p of r)if(p.resolveHostDirectives!==null){[s,l,h]=p.resolveHostDirectives(r);break}pM(t,n,e,s,o,l,h)}o!==null&&i!==null&&uM(e,i,o)}function uM(t,n,e){let i=t.localNames=[];for(let a=0;a<n.length;a+=2){let o=e[n[a+1]];if(o==null)throw new q(-301,!1);i.push(n[a],o)}}function mM(t,n,e){n.componentOffset=e,(t.components??=[]).push(n.index)}function pM(t,n,e,i,a,o,r){let s=i.length,l=null;for(let _=0;_<s;_++){let b=i[_];l===null&&Xn(b)&&(l=b,mM(t,e,_)),Th(Nd(e,n),t,b.type)}_M(e,t.data.length,s),l?.viewProvidersResolver&&l.viewProvidersResolver(l);for(let _=0;_<s;_++){let b=i[_];b.providersResolver&&b.providersResolver(b)}let h=!1,p=!1,g=s0(t,n,s,null);s>0&&(e.directiveToIndex=new Map);for(let _=0;_<s;_++){let b=i[_];if(e.mergedAttrs=br(e.mergedAttrs,b.hostAttrs),fM(t,e,n,g,b),vM(g,b,a),r!==null&&r.has(b)){let[I,P]=r.get(b);e.directiveToIndex.set(b.type,[g,I+e.directiveStart,P+e.directiveStart])}else(o===null||!o.has(b))&&e.directiveToIndex.set(b.type,g);b.contentQueries!==null&&(e.flags|=4),(b.hostBindings!==null||b.hostAttrs!==null||b.hostVars!==0)&&(e.flags|=64);let y=b.type.prototype;!h&&(y.ngOnChanges||y.ngOnInit||y.ngDoCheck)&&((t.preOrderHooks??=[]).push(e.index),h=!0),!p&&(y.ngOnChanges||y.ngDoCheck)&&((t.preOrderCheckHooks??=[]).push(e.index),p=!0),g++}hM(t,e,o)}function hM(t,n,e){for(let i=n.directiveStart;i<n.directiveEnd;i++){let a=t.data[i];if(e===null||!e.has(a))K_(0,n,a,i),K_(1,n,a,i),X_(n,i,!1);else{let o=e.get(a);Q_(0,n,o,i),Q_(1,n,o,i),X_(n,i,!0)}}}function K_(t,n,e,i){let a=t===0?e.inputs:e.outputs;for(let o in a)if(Object.hasOwn(a,o)){let r;t===0?r=n.inputs??={}:r=n.outputs??={},r[o]??=[],r[o].push(i),A0(n,o)}}function Q_(t,n,e,i){let a=t===0?e.inputs:e.outputs;for(let o in a)if(Object.hasOwn(a,o)){let r=a[o],s;t===0?s=n.hostDirectiveInputs??={}:s=n.hostDirectiveOutputs??={},s[r]??=[],s[r].push(i,o),A0(n,r)}}function A0(t,n){n===`class`?t.flags|=8:n===`style`&&(t.flags|=16)}function X_(t,n,e){let{attrs:i,inputs:a,hostDirectiveInputs:o}=t;if(i===null||!e&&a===null||e&&o===null||wf(t)){t.initialInputs??=[],t.initialInputs.push(null);return}let r=null,s=0;for(;s<i.length;){let l=i[s];if(l===0){s+=4;continue}else if(l===5){s+=2;continue}else if(typeof l==`number`)break;if(!e&&Object.hasOwn(a,l)){let h=a[l];for(let p of h)if(p===n){r??=[],r.push(l,i[s+1]);break}}else if(e&&Object.hasOwn(o,l)){let h=o[l];for(let p=0;p<h.length;p+=2)if(h[p]===n){r??=[],r.push(h[p+1],i[s+1]);break}}s+=2}t.initialInputs??=[],t.initialInputs.push(r)}function fM(t,n,e,i,a){t.data[i]=a;let r=new vo(a.factory||(a.factory=pa(a.type,!0)),Xn(a),He,null);t.blueprint[i]=r,e[i]=r,gM(t,n,i,s0(t,e,a.hostVars,pt),a)}function gM(t,n,e,i,a){let o=a.hostBindings;if(o){let r=t.hostBindingOpCodes;r===null&&(r=t.hostBindingOpCodes=[]);let s=~n.index;bM(r)!=s&&r.push(s),r.push(e,i,o)}}function bM(t){let n=t.length;for(;n>0;){let e=t[--n];if(typeof e==`number`&&e<0)return e}return 0}function vM(t,n,e){if(e){if(n.exportAs)for(let i=0;i<n.exportAs.length;i++)e[n.exportAs[i]]=t;Xn(n)&&(e[``]=t)}}function _M(t,n,e){t.flags|=1,t.directiveStart=n,t.directiveEnd=n+e,t.providerIndexes=n}function Uf(t,n,e,i,a,o,r,s){let l=n[ve],h=l.consts,g=xo(l,t,e,i,Sn(h,r));return o&&N0(l,n,g,Sn(h,s),a),g.mergedAttrs=br(g.mergedAttrs,g.attrs),g.attrs!==null&&Fd(g,g.attrs,!1),g.mergedAttrs!==null&&Fd(g,g.mergedAttrs,!0),l.queries!==null&&l.queries.elementStart(l,g),g}function $f(t,n){gy(t,n),Wp(n)&&t.queries.elementEnd(n)}function yM(t,n,e,i,a,o){let r=n.consts,l=xo(n,t,e,i,Sn(r,a));if(l.mergedAttrs=br(l.mergedAttrs,l.attrs),o!=null){let h=Sn(r,o);l.localNames=[];for(let p=0;p<h.length;p+=2)l.localNames.push(h[p],-1)}return l.attrs!==null&&Fd(l,l.attrs,!1),l.mergedAttrs!==null&&Fd(l,l.mergedAttrs,!0),n.queries!==null&&n.queries.elementStart(n,l),l}var R0=typeof ShadowRoot<`u`;var xM=typeof Document<`u`;function CM(t){return Object.keys(t).map(n=>{let[e,i,a]=t[n],o={propName:e,templateName:n,isSignal:(i&tl.SignalBased)!==0};return a&&(o.transform=a),o})}function SM(t){return Object.keys(t).map(n=>({propName:t[n],templateName:n}))}function wM(t,n,e){let i=n instanceof Mt?n:n?.injector;return i&&t.getStandaloneInjector!==null&&(i=t.getStandaloneInjector(i)||i),i?new Rd(e,i):e}function EM(t){let n=t.get(Nt,null);if(n===null)throw new q(407,!1);return{rendererFactory:n,sanitizer:t.get(k0,null),changeDetectionScheduler:t.get(gi,null),ngReflect:!1,tracingService:t.get(ii,null,{optional:!0})}}function DM(t,n,e){let i=O0(t);return qy(n,i,i===`svg`?qp:i===`math`?Xv:e)}function IM(t){if((t&&`localName`in t&&typeof t.localName==`string`?t.localName:t?.tagName)?.toLowerCase()===`script`)throw new q(905,!1)}function O0(t){return(t.selectors[0][0]||`div`).toLowerCase()}var xr=class{componentDef;ngModule;selector;componentType;ngContentSelectors;isBoundToModule;cachedInputs=null;cachedOutputs=null;get inputs(){return this.cachedInputs??=CM(this.componentDef.inputs),this.cachedInputs}get outputs(){return this.cachedOutputs??=SM(this.componentDef.outputs),this.cachedOutputs}constructor(n,e){this.componentDef=n,this.ngModule=e,this.componentType=n.type,this.selector=YI(n.selectors),this.ngContentSelectors=n.ngContentSelectors??[],this.isBoundToModule=!!e}create(n,e,i,a,o,r,s,l){Ke(ze.DynamicComponentStart);let h=ge(null);try{let p=this.componentDef,g=wM(p,a||this.ngModule,n),_=EM(g),b=_.tracingService;return b&&b.componentCreate?b.componentCreate(M0(p),()=>this.createComponentRef(_,g,e,i,o,r,s,l)):this.createComponentRef(_,g,e,i,o,r,s,l)}finally{ge(h)}}createComponentRef(n,e,i,a,o,r,s,l){let h=this.componentDef,p=kM(a,h,r,o,l),g=n.rendererFactory.createRenderer(null,h),_=a?vk(g,a,h.encapsulation,e):DM(h,g,s??null);IM(_);let b=e.get(Co,null),y=MM(_,()=>e.get(me,null)??Oy());b&&b.addHost(y);let I=r?.some(Z_)||o?.some(L=>typeof L!=`function`&&L.bindings.some(Z_)),P=Rf(null,p,null,512|r0(h),null,null,n,g,e,null,By(_,e,!0));b&&R0&&y instanceof ShadowRoot&&cd(P,()=>{b.removeHost(y)}),P[rt]=_,hd(P);let ie=null;try{let L=Uf(rt,P,2,`#host`,()=>p.directiveRegistry,!0,0);Qy(g,_,L),vr(_,P),Kd(p,P,L),bf(p,L,P),$f(p,L),i!==void 0&&NM(L,this.ngContentSelectors,i),ie=Rn(L.index,P),P[yt]=ie[yt],Vf(p,P,null)}catch(L){throw ie!==null&&Ah(ie),Ah(P),L}finally{Ke(ze.DynamicComponentEnd),fd()}return new Ld(this.componentType,P,!!I)}};function kM(t,n,e,i,a){let o=t?[`ng-version`,`22.2.1`]:KI(n.selectors[0]),r=null,s=null,l=0;if(e)for(let g of e)l+=g[qh].requiredVars,g.create&&(g.targetIdx=0,(r??=[]).push(g)),g.update&&(g.targetIdx=0,(s??=[]).push(g));if(i)for(let g=0;g<i.length;g++){let _=i[g];if(typeof _!=`function`)for(let b of _.bindings){l+=b[qh].requiredVars;let y=g+1;b.create&&(b.targetIdx=y,(r??=[]).push(b)),b.update&&(b.targetIdx=y,(s??=[]).push(b))}}let h=[n];if(i)for(let g of i){let b=Ts(typeof g==`function`?g:g.type);h.push(b)}return Af(0,null,TM(r,s),1,l,h,null,null,null,[o],null)}function MM(t,n){let e=t.getRootNode?.();return xM&&e instanceof Document?e.head:e&&R0&&e instanceof ShadowRoot?e:n().head}function TM(t,n){return!t&&!n?null:e=>{if(e&1&&t)for(let i of t)i.create();if(e&2&&n)for(let i of n)i.update()}}function Z_(t){let n=t[qh].kind;return n===`input`||n===`twoWay`}var Ld=class extends I0{_rootLView;_hasInputBindings;instance;hostView;changeDetectorRef;componentType;location;previousInputValues=null;_tNode;constructor(n,e,i){super(),this._rootLView=e,this._hasInputBindings=i,this._tNode=rd(e[ve],rt),this.location=Er(this._tNode,e),this.instance=Rn(this._tNode.index,e)[yt],this.hostView=this.changeDetectorRef=new Sa(e,void 0),this.componentType=n}setInput(n,e){this._hasInputBindings;let i=this._tNode;if(this.previousInputValues??=new Map,this.previousInputValues.has(n)&&Object.is(this.previousInputValues.get(n),e))return;let a=this._rootLView;Qd(i,a[ve],a,n,e);this.previousInputValues.set(n,e);Bf(Rn(i.index,a),1)}get injector(){return new Wi(this._tNode,this._rootLView)}destroy(){this.hostView.destroy()}onDestroy(n){this.hostView.onDestroy(n)}};function NM(t,n,e){let i=t.projection=[];for(let a=0;a<n.length;a++){let o=e[a];i.push(o!=null&&o.length?Array.from(o):null)}}var Bt=(()=>{class t{static __NG_ELEMENT_ID__=AM}return t})();function AM(){return P0(Tt(),ce())}var Yh=class t extends Bt{_lContainer;_hostTNode;_hostLView;constructor(n,e,i){super(),this._lContainer=n,this._hostTNode=e,this._hostLView=i}get element(){return Er(this._hostTNode,this._hostLView)}get injector(){return new Wi(this._hostTNode,this._hostLView)}get parentInjector(){let n=pf(this._hostTNode,this._hostLView);if(_y(n)){let e=Md(n,this._hostLView),i=kd(n),a=e[ve].data[i+8];return new Wi(a,e)}else return new Wi(null,this._hostLView)}clear(){for(;this.length>0;)this.remove(this.length-1)}get(n){let e=J_(this._lContainer);return e!==null&&e[n]||null}get length(){return this._lContainer.length-xt}createEmbeddedView(n,e,i){let a,o,r;typeof i==`number`?a=i:i!=null&&(a=i.index,o=i.injector,r=i.onError);let s=Ad(this._lContainer,n.ssrId),l=n.createEmbeddedViewImpl(e||{},o,s);return r&&(l._lView[cr]=r),this.insertImpl(l,a,_r(this._hostTNode,s)),l}createComponent(n,e,i,a,o,r,s){let l,h,p=e||{};l=p.index,i=p.injector,a=p.projectableNodes,o=p.environmentInjector||p.ngModuleRef,r=p.directives,s=p.bindings,h=p.onError;let g=new xr(ha(n)),_=i||this.parentInjector;if(!o&&g.ngModule==null){let L=this.parentInjector.get(Mt,null);L&&(o=L)}let b=ha(g.componentType??{}),y=Ad(this._lContainer,b?.id??null),I=y?.firstChild??null,P=g.create(_,a,I,o,r,s,this._getHostElementNamespace());return h&&(P.hostView._lView[cr]=h),this.insertImpl(P.hostView,l,_r(this._hostTNode,y)),P}_getHostElementNamespace(){if(this._hostTNode.type&2){let n=this._hostTNode.parent??this._hostLView[Ut];return n!==null&&n.type&2&&typeof n.value==`string`&&n.value.toLowerCase()===`foreignobject`?null:n?.namespace??null}return this._hostTNode.namespace}insert(n,e){return this.insertImpl(n,e,!0)}insertImpl(n,e,i){let a=n._lView;if(Jv(a)){let s=this.indexOf(n);if(s!==-1)this.detach(s);else{let l=a[Et],h=new t(l,l[Ut],l[Et]);h.detach(h.indexOf(n))}}let o=this._adjustIndex(e),r=this._lContainer;return al(r,a,o,i),n.attachToViewContainerRef(),Vp(yh(r),o,n),n}move(n,e){return this.insert(n,e)}indexOf(n){let e=J_(this._lContainer);return e!==null?e.indexOf(n):-1}remove(n){let e=this._adjustIndex(n,-1),i=Ys(this._lContainer,e);i&&(Ns(yh(this._lContainer),e),qd(i[ve],i))}detach(n){let e=this._adjustIndex(n,-1),i=Ys(this._lContainer,e);return i&&Ns(yh(this._lContainer),e)!=null?new Sa(i):null}_adjustIndex(n,e=0){return n??this.length+e}};function J_(t){return t[Os]}function yh(t){return t[Os]||(t[Os]=[])}function P0(t,n){let e,i=n[t.index];return An(i)?e=i:(e=C0(i,n,null,t),n[t.index]=e,Of(n,e)),OM(e,n,t,i),new Yh(e,t,n)}function RM(t,n){let e=t[et],i=e.createComment(``),a=Cn(n,t);return go(e,e.parentNode(a),i,e.nextSibling(a),!1),i}var OM=LM;var PM=()=>!1;function FM(t,n,e){return PM(t,n,e)}function LM(t,n,e,i){if(t[lo])return;let a;e.type&8?a=$t(i):a=RM(n,e),t[lo]=a}var Kh=class t{queryList;matches=null;constructor(n){this.queryList=n}clone(){return new t(this.queryList)}setDirty(){this.queryList.setDirty()}};var Qh=class t{queries;constructor(n=[]){this.queries=n}createEmbeddedView(n){let e=n.queries;if(e!==null){let i=n.contentQueries!==null?n.contentQueries[0]:e.length,a=[];for(let o=0;o<i;o++){let r=e.getByIndex(o),s=this.queries[r.indexInDeclarationView];a.push(s.clone())}return new t(a)}return null}insertView(n){this.dirtyQueriesWithMatches(n)}detachView(n){this.dirtyQueriesWithMatches(n)}finishViewCreation(n){this.dirtyQueriesWithMatches(n)}dirtyQueriesWithMatches(n){for(let e=0;e<this.queries.length;e++)Wf(n,e).matches!==null&&this.queries[e].setDirty()}};var Vd=class{flags;read;predicate;constructor(n,e,i=null){this.flags=e,this.read=i,typeof n==`string`?this.predicate=HM(n):this.predicate=n}};var Xh=class t{queries;constructor(n=[]){this.queries=n}elementStart(n,e){for(let i=0;i<this.queries.length;i++)this.queries[i].elementStart(n,e)}elementEnd(n){for(let e=0;e<this.queries.length;e++)this.queries[e].elementEnd(n)}embeddedTView(n){let e=null;for(let i=0;i<this.length;i++){let a=e!==null?e.length:0,o=this.getByIndex(i).embeddedTView(n,a);o&&(o.indexInDeclarationView=i,e!==null?e.push(o):e=[o])}return e!==null?new t(e):null}template(n,e){for(let i=0;i<this.queries.length;i++)this.queries[i].template(n,e)}getByIndex(n){return this.queries[n]}get length(){return this.queries.length}track(n){this.queries.push(n)}};var Zh=class t{metadata;matches=null;indexInDeclarationView=-1;crossesNgTemplate=!1;_declarationNodeIndex;_appliesToNextNode=!0;constructor(n,e=-1){this.metadata=n,this._declarationNodeIndex=e}elementStart(n,e){this.isApplyingToNode(e)&&this.matchTNode(n,e)}elementEnd(n){this._declarationNodeIndex===n.index&&(this._appliesToNextNode=!1)}template(n,e){this.elementStart(n,e)}embeddedTView(n,e){return this.isApplyingToNode(n)?(this.crossesNgTemplate=!0,this.addMatch(-n.index,e),new t(this.metadata)):null}isApplyingToNode(n){if(this._appliesToNextNode&&(this.metadata.flags&1)!==1){let e=this._declarationNodeIndex,i=n.parent;for(;i!==null&&i.type&8&&i.index!==e;)i=i.parent;return e===(i!==null?i.index:-1)}return this._appliesToNextNode}matchTNode(n,e){let i=this.metadata.predicate;if(Array.isArray(i))for(let a=0;a<i.length;a++){let o=i[a];this.matchTNodeWithReadOption(n,e,VM(e,o)),this.matchTNodeWithReadOption(n,e,Ed(e,n,o,!1,!1))}else i===Vt?e.type&4&&this.matchTNodeWithReadOption(n,e,-1):this.matchTNodeWithReadOption(n,e,Ed(e,n,i,!1,!1))}matchTNodeWithReadOption(n,e,i){if(i!==null){let a=this.metadata.read;if(a!==null)if(a===Y||a===Bt||a===se||a===Vt&&e.type&4)this.addMatch(e.index,-2);else{let o=Ed(e,n,a,!1,!1);o!==null&&this.addMatch(e.index,o)}else this.addMatch(e.index,i)}}addMatch(n,e){this.matches===null?this.matches=[n,e]:this.matches.push(n,e)}};function VM(t,n){let e=t.localNames;if(e!==null){for(let i=0;i<e.length;i+=2)if(e[i]===n)return e[i+1]}return null}function BM(t,n){return t.type&11?Er(t,n):t.type&4?Xd(t,n):null}function jM(t,n,e,i){return e===-1?BM(n,t):e===-2?zM(t,n,i):Gs(t,t[ve],e,n)}function zM(t,n,e){if(e===Y)return Er(n,t);if(e===Vt)return Xd(n,t);if(e===Bt)return P0(n,t);if(e===se)return new Wi(n,t)}function F0(t,n,e,i){let a=n[vi].queries[i];if(a.matches===null){let o=t.data,r=e.matches,s=[];for(let l=0;r!==null&&l<r.length;l+=2){let h=r[l];if(h<0)s.push(null);else{let p=o[h];s.push(jM(n,p,r[l+1],e.metadata.read))}}a.matches=s}return a.matches}function Jh(t,n,e,i){let a=t.queries.getByIndex(e),o=a.matches;if(o!==null){let r=F0(t,n,a,e);for(let s=0;s<o.length;s+=2){let l=o[s];if(l>0)i.push(r[s/2]);else{let h=o[s+1],p=n[-l];for(let g=xt;g<p.length;g++){let _=p[g];_[ba]===_[Et]&&Jh(_[ve],_,h,i)}if(p[co]!==null){let g=p[co];for(let _=0;_<g.length;_++){let b=g[_];Jh(b[ve],b,h,i)}}}}}return i}function Gf(t,n){return t[vi].queries[n].queryList}function L0(t,n,e){let i=new wn((e&4)===4);return n_(t,n,i,i.destroy),(n[vi]??=new Qh).queries.push(new Kh(i))-1}function V0(t,n,e){let i=Xe();return i.firstCreatePass&&(j0(i,new Vd(t,n,e),-1),(n&2)===2&&(i.staticViewQueries=!0)),L0(i,ce(),n)}function B0(t,n,e,i){let a=Xe();if(a.firstCreatePass){let o=Tt();j0(a,new Vd(n,e,i),o.index),UM(a,t),(e&2)===2&&(a.staticContentQueries=!0)}return L0(a,ce(),e)}function HM(t){return t.split(`,`).map(n=>n.trim())}function j0(t,n,e){t.queries===null&&(t.queries=new Xh),t.queries.track(new Zh(n,e))}function UM(t,n){let e=t.contentQueries||(t.contentQueries=[]);n!==(e.length?e[e.length-1]:-1)&&e.push(t.queries.length-1,n)}function Wf(t,n){return t.queries.getByIndex(n)}function z0(t,n){let e=t[ve],i=Wf(e,n);return i.crossesNgTemplate?Jh(e,t,n,[]):F0(e,t,i,n)}function H0(t,n,e){let i,a=bs(()=>{i._dirtyCounter();let o=$M(i,t);if(n&&o===void 0)throw new q(-951,!1);return o});return i=a[St],i._dirtyCounter=T(0),i._flatValue=void 0,a}function qf(t){return H0(!0,!1,t)}function Yf(t){return H0(!0,!0,t)}function U0(t,n){let e=t[St];e._lView=ce(),e._queryIndex=n,e._queryList=Gf(e._lView,n),e._queryList.onDirty(()=>e._dirtyCounter.update(i=>i+1))}function $M(t,n){let e=t._lView,i=t._queryIndex;if(e===void 0||i===void 0||e[xe]&4)return n?void 0:Qt;let a=Gf(e,i),o=z0(e,i);return a.reset(o,ky),n?a.first:a._changesDetected||t._flatValue===void 0?t._flatValue=a.toArray():t._flatValue}function So(t){return!!t&&typeof t.then==`function`}function Kf(t){return!!t&&typeof t.subscribe==`function`}var ni=class{};function Qf(t,n){return new Bd(t,n??null,[])}var Bd=class extends ni{ngModuleType;_parent;_bootstrapComponents=[];_r3Injector;instance;destroyCbs=[];constructor(n,e,i,a=!0){super(),this.ngModuleType=n,this._parent=e;let o=Ov(n);this._bootstrapComponents=LI(o.bootstrap),this._r3Injector=uh(n,e,[{provide:ni,useValue:this},...i],Is(n),new Set([`environment`])),a&&this.resolveInjectorInitializers()}resolveInjectorInitializers(){this._r3Injector.resolveInjectorInitializers(),this.instance=this._r3Injector.get(this.ngModuleType)}get injector(){return this._r3Injector}destroy(){let n=this._r3Injector;!n.destroyed&&n.destroy(),this.destroyCbs.forEach(e=>e()),this.destroyCbs=null}onDestroy(n){this.destroyCbs.push(n)}};var Qs=class extends ni{injector;instance=null;constructor(n){super();let e=new no([...n.providers,{provide:ni,useValue:this}],n.parent||or(),n.debugName,new Set([`environment`]));this.injector=e,n.runEnvironmentInitializers&&e.resolveInjectorInitializers()}destroy(){this.injector.destroy()}onDestroy(n){this.injector.onDestroy(n)}};function $0(t,n,e=null){return new Qs({providers:t,parent:n,debugName:e,runEnvironmentInitializers:!0}).injector}var GM=(()=>{class t{_injector;cachedInjectors=new Map;constructor(e){this._injector=e}getOrCreateStandaloneInjector(e){if(!e.standalone)return null;if(!this.cachedInjectors.has(e)){let i=jp(!1,e.type),a=i.length>0?$0([i],this._injector,``):null;this.cachedInjectors.set(e,a)}return this.cachedInjectors.get(e)}ngOnDestroy(){try{for(let e of this.cachedInjectors.values())e!==null&&e.destroy()}finally{this.cachedInjectors.clear()}}static ɵprov=be({token:t,providedIn:`environment`,factory:()=>new t(Se(Mt))})}return t})();function H(t){return Zs(()=>{let n=G0(t),e=s(r({},n),{decls:t.decls,vars:t.vars,template:t.template,consts:t.consts||null,ngContentSelectors:t.ngContentSelectors,onPush:t.changeDetection!==ff.Eager,directiveDefs:null,pipeDefs:null,dependencies:n.standalone&&t.dependencies||null,getStandaloneInjector:n.standalone?a=>a.get(GM).getOrCreateStandaloneInjector(e):null,getExternalStyles:null,signals:t.signals??!1,data:t.data||{},encapsulation:t.encapsulation||ei.Emulated,styles:t.styles||Qt,_:null,schemas:t.schemas||null,tView:null,id:``});n.standalone&&Ci(`NgStandalone`),W0(e);let i=t.dependencies;return e.directiveDefs=ey(i,WM),e.pipeDefs=ey(i,Pv),e.id=KM(e),e})}function WM(t){return ha(t)||Ts(t)}function re(t){return Zs(()=>({type:t.type,bootstrap:t.bootstrap||Qt,declarations:t.declarations||Qt,imports:t.imports||Qt,exports:t.exports||Qt,transitiveCompileScopes:null,schemas:t.schemas||null,id:t.id||null}))}function qM(t,n){if(t==null)return fa;let e={};for(let i in t)if(Object.hasOwn(t,i)){let a=t[i],o,r,s,l;Array.isArray(a)?(s=a[0],o=a[1],r=a[2]??o,l=a[3]||null):(o=a,r=a,s=tl.None,l=null),e[o]=[i,s,l],n[o]=r}return e}function YM(t){if(t==null)return fa;let n={};for(let e in t)Object.hasOwn(t,e)&&(n[t[e]]=e);return n}function Q(t){return Zs(()=>{let n=G0(t);return W0(n),n})}function ol(t){return{type:t.type,name:t.name,factory:null,pure:t.pure!==!1,standalone:t.standalone??!0,onDestroy:t.type.prototype.ngOnDestroy||null}}function G0(t){let n={};return{type:t.type,providersResolver:null,viewProvidersResolver:null,factory:null,hostBindings:t.hostBindings||null,hostVars:t.hostVars||0,hostAttrs:t.hostAttrs||null,contentQueries:t.contentQueries||null,declaredInputs:n,inputConfig:t.inputs||fa,exportAs:t.exportAs||null,standalone:t.standalone??!0,signals:t.signals===!0,selectors:t.selectors||Qt,viewQuery:t.viewQuery||null,features:t.features||null,setInput:null,resolveHostDirectives:null,hostDirectives:null,controlDef:null,signalFormsInputPresence:null,inputs:qM(t.inputs,n),outputs:YM(t.outputs),debugInfo:null}}function W0(t){t.features?.forEach(n=>n(t))}function ey(t,n){return t?()=>{let e=typeof t==`function`?t():t,i=[];for(let a of e){let o=n(a);o!==null&&i.push(o)}return i}:null}function KM(t){let n=0,e=typeof t.consts==`function`?``:t.consts,i=[t.selectors,t.ngContentSelectors,t.hostVars,t.hostAttrs,e,t.vars,t.decls,t.encapsulation,t.standalone,t.signals,t.exportAs,JSON.stringify(t.inputs),JSON.stringify(t.outputs),Object.getOwnPropertyNames(t.type.prototype),!!t.contentQueries,!!t.viewQuery];for(let o of i.join(`|`))n=Math.imul(31,n)+o.charCodeAt(0)<<0;return n+=2147483648,`c`+n}var q0=new N(``);var Xf=(()=>{class t{resolve;reject;initialized=!1;done=!1;donePromise=new Promise((e,i)=>{this.resolve=e,this.reject=i});appInits=f(q0,{optional:!0})??[];injector=f(se);constructor(){}runInitializers(){if(this.initialized)return;let e=[];for(let a of this.appInits){let o=rr(this.injector,a);if(So(o))e.push(o);else if(Kf(o)){let r=new Promise((s,l)=>{o.subscribe({complete:s,error:l})});e.push(r)}}let i=()=>{this.done=!0,this.resolve()};Promise.all(e).then(()=>{i()}).catch(a=>{this.reject(a)}),e.length===0&&i(),this.initialized=!0}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function Zf(t){return n=>{n.controlDef={create:(e,i)=>{e?.ɵngControlCreate(i)},update:(e,i)=>{e?.ɵngControlUpdate?.(i)},passThroughInput:t}}}function rl(t){let n=e=>{let i=Array.isArray(t);e.hostDirectives===null?(e.resolveHostDirectives=QM,e.hostDirectives=i?t.map(ef):[t]):i?e.hostDirectives.unshift(...t.map(ef)):e.hostDirectives.unshift(t)};return n.ngInherit=!0,n}function QM(t){let n=[],e=!1,i=null,a=null;for(let o=0;o<t.length;o++){let r=t[o];if(r.hostDirectives!==null){let s=n.length;i??=new Map,a??=new Map,Y0(r,n,i,t),a.set(r,[s,n.length-1])}o===0&&Xn(r)&&(e=!0,n.push(r))}for(let o=e?1:0;o<t.length;o++)n.push(t[o]);return i!==null&&i.forEach((o,r)=>{XM(r.declaredInputs,o.inputs)}),[n,i,a]}function Y0(t,n,e,i){if(t.hostDirectives!==null)for(let a of t.hostDirectives)if(typeof a==`function`){let o=a();for(let r of o)ty(ef(r),n,e,i)}else ty(a,n,e,i)}function ty(t,n,e,i){let a=Ts(t.directive);if(Y0(a,n,e,i),e.has(a)){let o=e.get(a);ny(o,t.inputs,`input`),ny(o,t.outputs,`output`)}else i.includes(a)||(e.set(a,t),n.push(a))}function ny(t,n,e){let i=e===`input`?t.inputs:t.outputs;Object.keys(n).forEach(a=>{let o=n[a];(!Object.hasOwn(i,a)||i[a]===o)&&(i[a]=o)})}function ef(t){return typeof t==`function`?{directive:Lt(t),inputs:{},outputs:{}}:{directive:Lt(t.directive),inputs:iy(t.inputs),outputs:iy(t.outputs)}}function iy(t){let n={};if(t!==void 0&&t.length>0)for(let e=0;e<t.length;e+=2)n[t[e]]=t[e+1];return n}function XM(t,n){for(let e in n)if(Object.hasOwn(n,e)){let i=n[e];t[i]=t[e]}}function ZM(t){return Object.getPrototypeOf(t.prototype).constructor}function Be(t){let n=ZM(t.type),e=!0,i=[t];for(;n&&n!==Function.prototype&&n!==Object.prototype;){let a,o=Object.hasOwn(n,ks)?n[ks]:void 0,r=Object.hasOwn(n,Ms)?n[Ms]:void 0;if(Xn(t))a=o??r;else{if(o)throw new q(903,!1);a=r}if(a){if(e){i.push(a);let l=t;l.inputs=xh(t.inputs),l.declaredInputs=xh(t.declaredInputs),l.outputs=xh(t.outputs);let h=a.hostBindings;h&&i1(t,h);let p=a.viewQuery,g=a.contentQueries;if(p&&t1(t,p),g&&n1(t,g),JM(t,a),Rv(t.outputs,a.outputs),Xn(a)&&a.data.animation){let _=t.data;_.animation=(_.animation||[]).concat(a.data.animation)}}let s=a.features;if(s)for(let l=0;l<s.length;l++){let h=s[l];h&&h.ngInherit&&h(t),h===Be&&(e=!1)}}n=Object.getPrototypeOf(n)}e1(i)}function JM(t,n){for(let e in n.inputs){if(!Object.hasOwn(n.inputs,e)||Object.hasOwn(t.inputs,e))continue;let i=n.inputs[e];i!==void 0&&(t.inputs[e]=i,t.declaredInputs[e]=n.declaredInputs[e])}}function e1(t){let n=0,e=null;for(let i=t.length-1;i>=0;i--){let a=t[i];a.hostVars=n+=a.hostVars,a.hostAttrs=br(a.hostAttrs,e=br(e,a.hostAttrs))}}function xh(t){return t===fa?{}:t===Qt?[]:t}function t1(t,n){let e=t.viewQuery;e?t.viewQuery=(i,a)=>{n(i,a),e(i,a)}:t.viewQuery=n}function n1(t,n){let e=t.contentQueries;e?t.contentQueries=(i,a,o)=>{n(i,a,o),e(i,a,o)}:t.contentQueries=n}function i1(t,n){let e=t.hostBindings;e?t.hostBindings=(i,a)=>{n(i,a),e(i,a)}:t.hostBindings=n}function K0(t,n,e,i,a,o,r,s){if(e.firstCreatePass){t.mergedAttrs=br(t.mergedAttrs,t.attrs);let p=t.tView=Af(2,t,a,o,r,e.directiveRegistry,e.pipeRegistry,null,e.schemas,e.consts,null);e.queries!==null&&(e.queries.template(e,t),p.queries=e.queries.embeddedTView(t))}s&&(t.flags|=s),po(t,!1);let l=o1(e,n,t,i);gd()&&Tf(e,n,l,t),vr(l,n);let h=C0(l,n,l,t);n[i+rt]=h,Of(n,h),FM(h,t,n)}function a1(t,n,e,i,a,o,r,s,l,h,p){let g=e+rt,_;return n.firstCreatePass?(_=xo(n,g,4,r||null,s||null),dd()&&N0(n,t,_,Sn(n.consts,h),Pf),gy(n,_)):_=n.data[g],K0(_,t,n,e,i,a,o,l),dr(_)&&Kd(n,t,_),h!=null&&nl(t,_,p),_}function Cr(t,n,e,i,a,o,r,s,l,h,p){let g=e+rt,_;if(n.firstCreatePass){if(_=xo(n,g,4,r||null,s||null),h!=null){let b=Sn(n.consts,h);_.localNames=[];for(let y=0;y<b.length;y+=2)_.localNames.push(b[y],-1)}}else _=n.data[g];return K0(_,t,n,e,i,a,o,l),h!=null&&nl(t,_,p),_}function st(t,n,e,i,a,o,r,s){let l=ce(),h=Xe();return a1(l,h,t,n,e,i,a,Sn(h.consts,o),void 0,r,s),st}function kr(t,n,e,i,a,o,r,s){let l=ce(),h=Xe();return Cr(l,h,t,n,e,i,a,Sn(h.consts,o),void 0,r,s),kr}var o1=r1;function r1(t,n,e,i){return Bs(!0),n[et].createComment(``)}var Jf=new N(``);var eg=new N(``);function Q0(){Zm(t=>{throw new q(-600,``)})}var s1=10;var On=(()=>{class t{_runningTick=!1;_destroyed=!1;_destroyListeners=[];_views=[];internalErrorHandler=f(Ui);afterRenderManager=f(Wd);zonelessEnabled=f(zs);rootEffectScheduler=f(_d);dirtyFlags=0;tracingSnapshot=null;allTestViews=new Set;autoDetectTestViews=new Set;includeAllTestViews=!1;afterTick=new V;get allViews(){return[...(this.includeAllTestViews?this.allTestViews:this.autoDetectTestViews).keys(),...this._views]}get destroyed(){return this._destroyed}componentTypes=[];components=[];internalPendingTask=f(fo);get isStable(){return this.internalPendingTask.hasPendingTasksObservable.pipe(lt(e=>!e))}constructor(){f(ii,{optional:!0})}whenStable(){let e;return new Promise(i=>{e=this.isStable.subscribe({next:a=>{a&&i()}})}).finally(()=>{e.unsubscribe()})}_injector=f(Mt);_rendererFactory=null;get injector(){return this._injector}bootstrap(e,i){return this.bootstrapImpl(e,i)}bootstrapImpl(e,i,a=se.NULL){return this._injector.get(Z).run(()=>{if(Ke(ze.BootstrapComponentStart),!this._injector.get(Xf).done)throw new q(405,``);let s=ha(e),l=this._injector.get(ni),h=new xr(s,l);this.componentTypes.push(e);let{hostElement:p,directives:g,bindings:_}=l1(i),b=p||h.selector,y=h.create(a,[],b,l.injector,g,_),I=y.location.nativeElement,P=y.injector.get(Jf,null);return P?.registerApplication(I),y.onDestroy(()=>{this.detachView(y.hostView),$s(this.components,y),P?.unregisterApplication(I)}),this._loadComponent(y),Ke(ze.BootstrapComponentEnd,y),y})}tick(){this.zonelessEnabled||(this.dirtyFlags|=1),this._tick()}_tick(){Ke(ze.ChangeDetectionStart),this.tracingSnapshot!==null?this.tracingSnapshot.run(Gd.CHANGE_DETECTION,this.tickImpl):this.tickImpl()}tickImpl=()=>{if(this._runningTick)throw Ke(ze.ChangeDetectionEnd),new q(101,!1);let e=ge(null);try{this._runningTick=!0,this.synchronize()}finally{this._runningTick=!1,this.tracingSnapshot?.dispose(),this.tracingSnapshot=null,ge(e),this.afterTick.next(),Ke(ze.ChangeDetectionEnd)}};synchronize(){this._rendererFactory===null&&!this._injector.destroyed&&(this._rendererFactory=this._injector.get(Nt,null,{optional:!0}));let e=0;for(;this.dirtyFlags!==0&&e++<s1;){Ke(ze.ChangeDetectionSyncStart);try{this.synchronizeOnce()}finally{Ke(ze.ChangeDetectionSyncEnd)}}}synchronizeOnce(){this.dirtyFlags&16&&(this.dirtyFlags&=-17,this.rootEffectScheduler.flush());let e=!1;if(this.dirtyFlags&7){let i=!!(this.dirtyFlags&1);this.dirtyFlags&=-8,this.dirtyFlags|=8;for(let{_lView:a}of this.allViews){if(!i&&!Ls(a))continue;v0(a,i&&!this.zonelessEnabled?0:1),e=!0}if(this.dirtyFlags&=-5,this.syncDirtyFlagsWithViews(),this.dirtyFlags&23)return}e||(this._rendererFactory?.begin?.(),this._rendererFactory?.end?.()),this.dirtyFlags&8&&(this.dirtyFlags&=-9,this.afterRenderManager.execute()),this.syncDirtyFlagsWithViews()}syncDirtyFlagsWithViews(){if(this.allViews.some(({_lView:e})=>Ls(e))){this.dirtyFlags|=2;return}else this.dirtyFlags&=-8}attachView(e){let i=e;this._views.push(i),i.attachToAppRef(this)}detachView(e){let i=e;$s(this._views,i),i.detachFromAppRef()}_loadComponent(e){this.attachView(e.hostView);try{this.tick()}catch(a){this.internalErrorHandler(a)}this.components.push(e),this._injector.get(eg,[]).forEach(a=>a(e))}ngOnDestroy(){if(!this._destroyed)try{this._destroyListeners.forEach(e=>e()),this._views.slice().forEach(e=>e.destroy())}finally{this._destroyed=!0,this._views=[],this._destroyListeners=[]}}onDestroy(e){return this._destroyListeners.push(e),()=>$s(this._destroyListeners,e)}destroy(){if(this._destroyed)throw new q(406,!1);let e=this._injector;e.destroy&&!e.destroyed&&e.destroy()}get viewCount(){return this._views.length}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function l1(t){return t===void 0||typeof t==`string`||t instanceof Element?{hostElement:t}:t}function $s(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}function Mr(t,n){let e=ce();if(fn(e,va(),n)){let a=Xe(),o=mr();if(Qd(o,a,e,t,n))_i(o)&&u0(e,o.index);else{let s=Cn(o,e);m0(e[et],s,null,o.value,t,n,null)}}return Mr}function ne(t,n,e,i){let a=ce();if(fn(a,va(),n)){Xe();Dk(mr(),a,t,n,e,i)}return ne}var tf=class{destroy(n){}updateValue(n,e){}swap(n,e){let i=Math.min(n,e),a=Math.max(n,e),o=this.detach(a);if(a-i>1){let r=this.detach(i);this.attach(i,o),this.attach(a,r)}else this.attach(i,o)}move(n,e){this.attach(e,this.detach(n))}};function Ch(t,n,e,i,a){return t===e&&Object.is(n,i)?1:Object.is(a(t,n),a(e,i))?-1:0}function c1(t,n,e,i){let a,o,r=0,s=t.length-1;if(Array.isArray(n)){ge(i);let h=n.length-1;for(ge(null);r<=s&&r<=h;){let p=t.at(r),g=n[r],_=Ch(r,p,r,g,e);if(_!==0){_<0&&t.updateValue(r,g),r++;continue}let b=t.at(s),y=n[h],I=Ch(s,b,h,y,e);if(I!==0){I<0&&t.updateValue(s,y),s--,h--;continue}let P=e(r,p),ie=e(s,b),L=e(r,g);if(Object.is(L,ie)){let x=e(h,y);Object.is(x,P)?(t.swap(r,s),t.updateValue(s,y),h--,s--):t.move(s,r),t.updateValue(r,g),r++;continue}if(a??=new jd,o??=oy(t,r,s,e),nf(t,a,r,L))t.updateValue(r,g),r++,s++;else if(o.has(L))a.set(P,t.detach(r)),s--;else{let x=t.create(r,n[r]);t.attach(r,x),r++,s++}}for(;r<=h;)ay(t,a,e,r,n[r]),r++}else if(n!=null){ge(i);let h=n[Symbol.iterator]();ge(null);let p=h.next();for(;!p.done&&r<=s;){let g=t.at(r),_=p.value,b=Ch(r,g,r,_,e);if(b!==0)b<0&&t.updateValue(r,_),r++,p=h.next();else{a??=new jd,o??=oy(t,r,s,e);let y=e(r,_);if(nf(t,a,r,y))t.updateValue(r,_),r++,s++,p=h.next();else if(!o.has(y))t.attach(r,t.create(r,_)),r++,s++,p=h.next();else{let I=e(r,g);a.set(I,t.detach(r)),s--}}}for(;!p.done;)ay(t,a,e,t.length,p.value),p=h.next()}for(;r<=s;)t.destroy(t.detach(s--));a?.forEach(h=>{t.destroy(h)})}function nf(t,n,e,i){return n!==void 0&&n.has(i)?(t.attach(e,n.get(i)),n.delete(i),!0):!1}function ay(t,n,e,i,a){if(nf(t,n,i,e(i,a)))t.updateValue(i,a);else{let o=t.create(i,a);t.attach(i,o)}}function oy(t,n,e,i){let a=new Set;for(let o=n;o<=e;o++)a.add(i(o,t.at(o)));return a}var jd=class{kvMap=new Map;_vMap=void 0;has(n){return this.kvMap.has(n)}delete(n){if(!this.has(n))return!1;let e=this.kvMap.get(n);return this._vMap!==void 0&&this._vMap.has(e)?(this.kvMap.set(n,this._vMap.get(e)),this._vMap.delete(e)):this.kvMap.delete(n),!0}get(n){return this.kvMap.get(n)}set(n,e){if(this.kvMap.has(n)){let i=this.kvMap.get(n);this._vMap===void 0&&(this._vMap=new Map);let a=this._vMap;for(;a.has(i);)i=a.get(i);a.set(i,e)}else this.kvMap.set(n,e)}forEach(n){for(let[e,i]of this.kvMap)if(n(i,e),this._vMap!==void 0){let a=this._vMap;for(;a.has(i);)i=a.get(i),n(i,e)}}};function E(t,n,e,i,a,o,r,s){Ci(`NgControlFlow`);let l=ce(),h=Xe();return Cr(l,h,t,n,e,i,a,Sn(h.consts,o),256,r,s),tg}function tg(t,n,e,i,a,o,r,s){return Ci(`NgControlFlow`),d1(t,n,e,i,a,o,r,s),tg}function d1(t,n,e,i,a,o,r,s){let l=ce(),h=Xe();Cr(l,h,t,n,e,i,a,Sn(h.consts,o),512,r,s)}function D(t,n){Ci(`NgControlFlow`);let e=ce(),i=va(),a=e[i]!==pt?e[i]:-1,o=a!==-1?zd(e,rt+a):void 0,r=0;if(fn(e,i,t)){let s=ge(null);try{if(o!==void 0&&w0(o,r),t!==-1){let l=rt+t,h=zd(e,l),p=sf(e[ve],l),g=D0(h,p,e);al(h,il(e,p,n,{dehydratedView:g}),r,_r(p,g))}}finally{ge(s)}}else if(o!==void 0){let s=S0(o,r);s!==void 0&&(s[yt]=n)}}var af=class{lContainer;$implicit;$index;constructor(n,e,i){this.lContainer=n,this.$implicit=e,this.$index=i}get $count(){return this.lContainer.length-xt}};function wo(t){return t}function Pn(t,n){return n}var of=class{hasEmptyBlock;trackByFn;liveCollection;constructor(n,e,i){this.hasEmptyBlock=n,this.trackByFn=e,this.liveCollection=i}};function de(t,n,e,i,a,o,r,s,l,h,p,g,_){Ci(`NgControlFlow`);let b=ce(),y=Xe(),I=l!==void 0,P=ce(),L=new of(I,s?r.bind(P[Xt][yt]):r);P[rt+t]=L,Cr(b,y,t+1,n,e,i,a,Sn(y.consts,o),256),I&&Cr(b,y,t+2,l,h,p,g,Sn(y.consts,_),512)}var rf=class extends tf{lContainer;hostLView;templateTNode;operationsCounter=void 0;needsIndexUpdate=!1;constructor(n,e,i){super(),this.lContainer=n,this.hostLView=e,this.templateTNode=i}get length(){return this.lContainer.length-xt}at(n){return this.getLView(n)[yt].$implicit}attach(n,e){let i=e[oo];this.needsIndexUpdate||=n!==this.length,al(this.lContainer,e,n,_r(this.templateTNode,i)),u1(this.lContainer,n)}detach(n){return this.needsIndexUpdate||=n!==this.length-1,m1(this.lContainer,n),p1(this.lContainer,n)}create(n,e){let i=Ad(this.lContainer,this.templateTNode.tView.ssrId);return il(this.hostLView,this.templateTNode,new af(this.lContainer,e,n),{dehydratedView:i})}destroy(n){qd(n[ve],n)}updateValue(n,e){this.getLView(n)[yt].$implicit=e}reset(){this.needsIndexUpdate=!1}updateIndexes(){if(this.needsIndexUpdate)for(let n=0;n<this.length;n++)this.getLView(n)[yt].$index=n}getLView(n){return h1(this.lContainer,n)}};function ue(t){let n=ge(null),e=Zt();try{let i=ce(),a=i[ve],o=i[e],r=e+1,s=zd(i,r);if(o.liveCollection===void 0)o.liveCollection=new rf(s,i,sf(a,r));else o.liveCollection.reset();let l=o.liveCollection;if(c1(l,t,o.trackByFn,n),l.updateIndexes(),o.hasEmptyBlock){let h=va(),p=l.length===0;if(fn(i,h,p)){let g=e+2,_=zd(i,g);if(p){let b=sf(a,g),y=D0(_,b,i);al(_,il(i,b,void 0,{dehydratedView:y}),0,_r(b,y))}else a.firstUpdatePass&&Qk(_),w0(_,0)}}}finally{ge(n)}}function zd(t,n){return t[n]}function u1(t,n){if(t.length<=xt)return;let i=t[xt+n];i&&nk(i)}function m1(t,n){if(t.length<=xt)return;let i=t[xt+n];i&&tk(i)}function p1(t,n){return Ys(t,n)}function h1(t,n){return S0(t,n)}function sf(t,n){return rd(t,n)}function S(t,n,e){let i=ce();if(fn(i,va(),n)){Xe();Ck(mr(),i,t,n,i[et],e)}return S}function lf(t,n,e,i,a){Qd(n,t,e,a?`class`:`style`,i)}function c(t,n,e,i){let a=ce(),o=a[ve],r=t+rt,s=o.firstCreatePass?Uf(r,a,2,n,Pf,dd(),e,i):o.data[r];if(_i(s)){let l=a[Qn].tracingService;if(l&&l.componentCreate){let h=o.data[s.directiveStart+s.componentOffset];return l.componentCreate(M0(h),()=>(ry(t,n,a,s,i),c))}}return ry(t,n,a,s,i),c}function ry(t,n,e,i,a){if(Ff(i,e,t,n,X0),dr(i)){let o=e[ve];Kd(o,e,i),bf(o,i,e)}a!=null&&nl(e,i)}function d(){let t=Xe(),e=Lf(Tt());return t.firstCreatePass&&$f(t,e),th(e)&&nh(),Jp(),e.classesWithoutHost!=null&&HD(e)&&lf(t,e,ce(),e.classesWithoutHost,!0),e.stylesWithoutHost!=null&&UD(e)&&lf(t,e,ce(),e.stylesWithoutHost,!1),d}function U(t,n,e,i){return c(t,n,e,i),d(),U}function ct(t,n,e,i){let a=ce(),o=a[ve],r=t+rt,s=o.firstCreatePass?yM(r,o,2,n,e,i):o.data[r];return Ff(s,a,t,n,X0),i!=null&&nl(a,s),ct}function ht(){return th(Lf(Tt()))&&nh(),Jp(),ht}function Wt(t,n,e,i){return ct(t,n,e,i),ht(),Wt}var X0=(t,n,e,i,a)=>(Bs(!0),qy(n[et],i,dh()));function ng(t,n,e){let i=ce(),a=i[ve],o=t+rt,r=a.firstCreatePass?Uf(o,i,8,`ng-container`,Pf,dd(),n,e):a.data[o];if(Ff(r,i,t,`ng-container`,f1),dr(r)){let s=i[ve];Kd(s,i,r),bf(s,r,i)}return e!=null&&nl(i,r),ng}function ig(){let t=Xe(),e=Lf(Tt());return t.firstCreatePass&&$f(t,e),ig}function Ea(t,n,e){return ng(t,n,e),ig(),Ea}var f1=(t,n,e,i,a)=>(Bs(!0),RI(n[et],``));function $(){return ce()}function qt(t,n,e){let i=ce();if(fn(i,va(),n)){Xe();d0(mr(),i,t,n,i[et],e)}return qt}var Hs=void 0;function g1(t){let n=Math.floor(Math.abs(t)),e=t.toString().replace(/^[^.]*\.?/,``).length;return n===1&&e===0?1:5}var b1=[`en`,[[`a`,`p`],[`AM`,`PM`]],[[`AM`,`PM`]],[[`S`,`M`,`T`,`W`,`T`,`F`,`S`],[`Sun`,`Mon`,`Tue`,`Wed`,`Thu`,`Fri`,`Sat`],[`Sunday`,`Monday`,`Tuesday`,`Wednesday`,`Thursday`,`Friday`,`Saturday`],[`Su`,`Mo`,`Tu`,`We`,`Th`,`Fr`,`Sa`]],Hs,[[`J`,`F`,`M`,`A`,`M`,`J`,`J`,`A`,`S`,`O`,`N`,`D`],[`Jan`,`Feb`,`Mar`,`Apr`,`May`,`Jun`,`Jul`,`Aug`,`Sep`,`Oct`,`Nov`,`Dec`],[`January`,`February`,`March`,`April`,`May`,`June`,`July`,`August`,`September`,`October`,`November`,`December`]],Hs,[[`B`,`A`],[`BC`,`AD`],[`Before Christ`,`Anno Domini`]],0,[6,0],[`M/d/yy`,`MMM d, y`,`MMMM d, y`,`EEEE, MMMM d, y`],[`h:mm a`,`h:mm:ss a`,`h:mm:ss a z`,`h:mm:ss a zzzz`],[`{1}, {0}`,Hs,Hs,Hs],[`.`,`,`,`;`,`%`,`+`,`-`,`E`,`×`,`‰`,`∞`,`NaN`,`:`],[`#,##0.###`,`#,##0%`,`¤#,##0.00`,`#E0`],`USD`,`$`,`US Dollar`,{},`ltr`,g1];var Sh=Object.create(null);function gn(t){let n=v1(t),e=sy(n);if(e)return e;let i=n.split(`-`)[0];if(e=sy(i),e)return e;if(i===`en`)return b1;throw new q(701,!1)}function sy(t){if(!(t in Sh)){let n=bi.ng&&bi.ng.common&&bi.ng.common.locales&&bi.ng.common.locales[t];return n!==void 0&&(Sh[t]=n),n}return Sh[t]}var Dt={LocaleId:0,DayPeriodsFormat:1,DayPeriodsStandalone:2,DaysFormat:3,DaysStandalone:4,MonthsFormat:5,MonthsStandalone:6,Eras:7,FirstDayOfWeek:8,WeekendRange:9,DateFormat:10,TimeFormat:11,DateTimeFormat:12,NumberSymbols:13,NumberFormats:14,CurrencyCode:15,CurrencySymbol:16,CurrencyName:17,Currencies:18,Directionality:19,PluralCase:20,ExtraData:21};function v1(t){return t.toLowerCase().replace(/_/g,`-`)}var sl=`en-US`;function Z0(t){typeof t==`string`&&t.toLowerCase().replace(/_/g,`-`)}function C(t,n,e){let i=ce(),a=Xe(),o=Tt();return y1(a,i,i[et],o,t,n,e),C}function Tr(t,n,e){let i=ce(),a=Xe(),o=Tt();return(o.type&3||e)&&zf(o,a,i,e,i[et],t,n,bo(o,i,n)),Tr}function y1(t,n,e,i,a,o,r){let s=!0,l=null;if((i.type&3||r)&&(l??=bo(i,n,o),zf(i,t,n,r,e,a,o,l)&&(s=!1)),s){let h=i.outputs?.[a],p=i.hostDirectiveOutputs?.[a];if(p&&p.length)for(let g=0;g<p.length;g+=2){let _=p[g],b=p[g+1];l??=bo(i,n,o),Od(i,n,_,b,a,l)}if(h&&h.length)for(let g of h)l??=bo(i,n,o),Od(i,n,g,a,a,l)}}function v(t=1){return h_(t)}function x1(t,n){let e=null,i=UI(t);for(let a=0;a<n.length;a++){let o=n[a];if(o===`*`){e=a;continue}if(i===null?Zy(t,o,!0):WI(i,o))return a}return e}function Fe(t){let n=ce()[Xt][Ut];if(!n.projection){let i=n.projection=zv(t?t.length:1,null),a=i.slice(),o=n.child;for(;o!==null;){if(o.type!==128){let r=t?x1(o,t):0;r!==null&&(a[r]?a[r].projectionNext=o:i[r]=o,a[r]=o)}o=o.next}}}function he(t,n=0,e,i,a,o){let r=ce(),s=Xe(),l=i?t+1:null;l!==null&&Cr(r,s,l,i,a,o,null,e);let h=xo(s,rt+t,16,null,e||null);h.projection===null&&(h.projection=n),oh();let g=!r[oo]||eh();r[Xt][Ut].projection[h.projection]===null&&l!==null?C1(r,s,l):g&&!Ud(h)&&mk(s,r,h)}function C1(t,n,e){let i=rt+e,a=n.data[i],o=t[i],r=Ad(o,a.tView.ssrId);al(o,il(t,a,void 0,{dehydratedView:r}),0,_r(a,r))}function jt(t,n,e,i){return B0(t,n,e,i),jt}function Ze(t,n,e){return V0(t,n,e),Ze}function J(t){let n=ce(),e=Xe(),i=pd();Vs(i+1);let a=Wf(e,i);if(t.dirty&&Zv(n)===((a.metadata.flags&2)===2)){if(a.matches===null)t.reset([]);else{let o=z0(n,i);t.reset(o,ky),t.notifyOnChanges()}return!0}return!1}function ee(){return Gf(ce(),pd())}function Zd(t,n,e,i,a){return U0(n,B0(t,e,i,a)),Zd}function Jd(t,n,e,i){return U0(t,V0(n,e,i)),Jd}function eu(t=1){Vs(pd()+t)}function ft(t){return Ps(rh(),rt+t)}function Cd(t,n){return t<<17|n<<2}function yo(t){return t>>17&32767}function S1(t){return(t&2)==2}function w1(t,n){return t&131071|n<<17}function cf(t){return t|2}function Sr(t){return(t&131068)>>2}function wh(t,n){return t&-131069|n<<2}function E1(t){return(t&1)===1}function df(t){return t|1}function D1(t,n,e,i,a,o){let r=o?n.classBindings:n.styleBindings,s=yo(r),l=Sr(r);t[i]=e;let h=!1,p;if(Array.isArray(e)){let g=e;p=g[1],(p===null||ar(g,p)>0)&&(h=!0)}else p=e;if(a)if(l!==0){let _=yo(t[s+1]);t[i+1]=Cd(_,s),_!==0&&(t[_+1]=wh(t[_+1],i)),t[s+1]=w1(t[s+1],i)}else t[i+1]=Cd(s,0),s!==0&&(t[s+1]=wh(t[s+1],i)),s=i;else t[i+1]=Cd(l,0),s===0?s=i:t[l+1]=wh(t[l+1],i),l=i;h&&(t[i+1]=cf(t[i+1])),ly(t,p,i,!0),ly(t,p,i,!1),I1(n,p,t,i,o),r=Cd(s,l),o?n.classBindings=r:n.styleBindings=r}function I1(t,n,e,i,a){let o=a?t.residualClasses:t.residualStyles;o!=null&&typeof n==`string`&&ar(o,n)>=0&&(e[i+1]=df(e[i+1]))}function ly(t,n,e,i){let a=t[e+1],o=n===null,r=i?yo(a):Sr(a),s=!1;for(;r!==0&&(s===!1||o);){let l=t[r],h=t[r+1];k1(l,n)&&(s=!0,t[r+1]=i?df(h):cf(h)),r=i?yo(h):Sr(h)}s&&(t[e+1]=i?cf(a):df(a))}function k1(t,n){return t===null||n==null||(Array.isArray(t)?t[1]:t)===n?!0:Array.isArray(t)&&typeof n==`string`?ar(t,n)>=0:!1}var Jn={textEnd:0,key:0,keyEnd:0,value:0,valueEnd:0};function M1(t){return t.substring(Jn.key,Jn.keyEnd)}function T1(t){return N1(t),J0(t,ex(t,0,Jn.textEnd))}function J0(t,n){let e=Jn.textEnd;return e===n?-1:(n=Jn.keyEnd=A1(t,Jn.key=n,e),ex(t,n,e))}function N1(t){Jn.key=0,Jn.keyEnd=0,Jn.value=0,Jn.valueEnd=0,Jn.textEnd=t.length}function ex(t,n,e){for(;n<e&&t.charCodeAt(n)<=32;)n++;return n}function A1(t,n,e){for(;n<e&&t.charCodeAt(n)>32;)n++;return n}function Jt(t,n,e){return tx(t,n,e,!1),Jt}function G(t,n){return tx(t,n,null,!0),G}function tt(t){O1(j1,R1,t,!0)}function R1(t,n){for(let e=T1(n);e>=0;e=J0(n,e))nd(t,M1(n),!0)}function tx(t,n,e,i){let a=ce(),o=Xe(),r=Hi(2);if(o.firstUpdatePass&&ix(o,t,r,i),n!==pt&&fn(a,r,n)){let s=o.data[Zt()];ax(o,s,a,a[et],t,a[r+1]=H1(n,e),i,r)}}function O1(t,n,e,i){let a=Xe(),o=Hi(2);a.firstUpdatePass&&ix(a,null,o,i);let r=ce();if(e!==pt&&fn(r,o,e)){let s=a.data[Zt()];if(ox(s,i)&&!nx(a,o)){let l=i?s.classesWithoutHost:s.stylesWithoutHost;l!==null&&(e=Qc(l,e||``)),lf(a,s,r,e,i)}else z1(a,s,r,r[et],r[o+1],r[o+1]=B1(t,n,e),i,o)}}function nx(t,n){return n>=t.expandoStartIndex}function ix(t,n,e,i){let a=t.data;if(a[e+1]===null){let o=a[Zt()],r=nx(t,e);ox(o,i)&&n===null&&!r&&(n=!1),n=P1(a,o,n,i),D1(a,o,n,e,r,i)}}function P1(t,n,e,i){let a=d_(t),o=i?n.residualClasses:n.residualStyles;if(a===null)(i?n.classBindings:n.styleBindings)===0&&(e=Eh(null,t,n,e,i),e=Xs(e,n.attrs,i),o=null);else{let r=n.directiveStylingLast;if(r===-1||t[r]!==a)if(e=Eh(a,t,n,e,i),o===null){let l=F1(t,n,i);l!==void 0&&Array.isArray(l)&&(l=Eh(null,t,n,l[1],i),l=Xs(l,n.attrs,i),L1(t,n,i,l))}else o=V1(t,n,i)}return o!==void 0&&(i?n.residualClasses=o:n.residualStyles=o),e}function F1(t,n,e){let i=e?n.classBindings:n.styleBindings;if(Sr(i)!==0)return t[yo(i)]}function L1(t,n,e,i){let a=e?n.classBindings:n.styleBindings;t[yo(a)]=i}function V1(t,n,e){let i,a=n.directiveEnd;for(let o=1+n.directiveStylingLast;o<a;o++){let r=t[o].hostAttrs;i=Xs(i,r,e)}return Xs(i,n.attrs,e)}function Eh(t,n,e,i,a){let o=null,r=e.directiveEnd,s=e.directiveStylingLast;for(s===-1?s=e.directiveStart:s++;s<r&&(o=n[s],i=Xs(i,o.hostAttrs,a),o!==t);)s++;return t!==null&&(e.directiveStylingLast=s),i}function Xs(t,n,e){let i=e?1:2,a=-1;if(n!==null)for(let o=0;o<n.length;o++){let r=n[o];typeof r==`number`?a=r:a===i&&(Array.isArray(t)||(t=t===void 0?[]:[``,t]),nd(t,r,e?!0:n[++o]))}return t===void 0?null:t}function B1(t,n,e){if(e==null||e===``)return Qt;let i=[],a=ti(e);if(Array.isArray(a))for(let o=0;o<a.length;o++)t(i,a[o],!0);else if(a instanceof Set)for(let o of a)t(i,o,!0);else if(typeof a==`object`)for(let o in a)Object.hasOwn(a,o)&&t(i,o,a[o]);else typeof a==`string`&&n(i,a);return i}function j1(t,n,e){let i=String(n);i!==``&&!i.includes(` `)&&nd(t,i,e)}function z1(t,n,e,i,a,o,r,s){a===pt&&(a=Qt);let l=0,h=0,p=0<a.length?a[0]:null,g=0<o.length?o[0]:null;for(;p!==null||g!==null;){let _=l<a.length?a[l+1]:void 0,b=h<o.length?o[h+1]:void 0,y=null,I;p===g?(l+=2,h+=2,_!==b&&(y=g,I=b)):g===null||p!==null&&p<g?(l+=2,y=p):(h+=2,y=g,I=b),y!==null&&ax(t,n,e,i,y,I,r,s),p=l<a.length?a[l]:null,g=h<o.length?o[h]:null}}function ax(t,n,e,i,a,o,r,s){if(!(n.type&3))return;let l=t.data,h=l[s+1];if(!Hd(E1(h)?cy(l,n,e,a,Sr(h),r):void 0)){Hd(o)||S1(h)&&(o=cy(l,null,e,a,s,r));hk(i,r,Yp(Zt(),e),a,o)}}function cy(t,n,e,i,a,o){let r=n===null,s;for(;a>0;){let l=t[a],h=Array.isArray(l),p=h?l[1]:l,g=p===null,_=e[a+1];_===pt&&(_=g?Qt:void 0);let b=g?id(_,i):p===i?_:void 0;if(h&&!Hd(b)&&(b=id(l,i)),Hd(b)&&(s=b,r))return s;let y=t[a+1];a=r?yo(y):Sr(y)}if(n!==null){let l=o?n.residualClasses:n.residualStyles;l!=null&&(s=id(l,i))}return s}function Hd(t){return t!==void 0}function H1(t,n){return t==null||t===``||(typeof n==`string`?t=ti(t)+n:typeof t==`object`&&(t=Is(ti(t)))),t}function ox(t,n){return(t.flags&(n?8:16))!==0}function u(t,n=``){let e=ce(),i=Xe(),a=t+rt,o=i.firstCreatePass?xo(i,a,1,n,null):i.data[a],r=U1(i,e,o,n);e[a]=r,gd()&&Tf(i,e,r,o),po(o,!1)}var U1=(t,n,e,i)=>(Bs(!0),NI(n[et],i));function $1(t,n,e,i=``){return fn(t,va(),e)?n+$e(e)+i:pt}function G1(t,n,e,i,a,o=``){let s=yr(t,ho(),e,a);return Hi(2),s?n+$e(e)+i+$e(a)+o:pt}function W1(t,n,e,i,a,o,r,s=``){let h=nM(t,ho(),e,a,r);return Hi(3),h?n+$e(e)+i+$e(a)+o+$e(r)+s:pt}function q1(t,n,e,i,a,o,r,s,l,h=``){let g=Ks(t,ho(),e,a,r,l);return Hi(4),g?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(l)+h:pt}function Y1(t,n,e,i,a,o,r,s,l,h,p,g=``){let _=ho(),b=Ks(t,_,e,a,r,l);return b=fn(t,_+4,p)||b,Hi(5),b?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(l)+h+$e(p)+g:pt}function K1(t,n,e,i,a,o,r,s,l,h,p,g,_,b=``){let y=ho(),I=Ks(t,y,e,a,r,l);return I=yr(t,y+4,p,_)||I,Hi(6),I?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(l)+h+$e(p)+g+$e(_)+b:pt}function Q1(t,n,e,i,a,o,r,s,l,h,p,g,_,b,y,I,P,ie=``){let L=ho(),x=Ks(t,L,e,a,r,l);return x=Ks(t,L+4,p,_,y,P)||x,Hi(8),x?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(l)+h+$e(p)+g+$e(_)+b+$e(y)+I+$e(P)+ie:pt}function w(t){return B(``,t),w}function B(t,n,e){let i=ce(),a=$1(i,t,n,e);return a!==pt&&Eo(i,Zt(),a),B}function Me(t,n,e,i,a){let o=ce(),r=G1(o,t,n,e,i,a);return r!==pt&&Eo(o,Zt(),r),Me}function Ot(t,n,e,i,a,o,r){let s=ce(),l=W1(s,t,n,e,i,a,o,r);return l!==pt&&Eo(s,Zt(),l),Ot}function Si(t,n,e,i,a,o,r,s,l){let h=ce(),p=q1(h,t,n,e,i,a,o,r,s,l);return p!==pt&&Eo(h,Zt(),p),Si}function ll(t,n,e,i,a,o,r,s,l,h,p){let g=ce(),_=Y1(g,t,n,e,i,a,o,r,s,l,h,p);return _!==pt&&Eo(g,Zt(),_),ll}function tu(t,n,e,i,a,o,r,s,l,h,p,g,_){let b=ce(),y=K1(b,t,n,e,i,a,o,r,s,l,h,p,g,_);return y!==pt&&Eo(b,Zt(),y),tu}function nu(t,n,e,i,a,o,r,s,l,h,p,g,_,b,y,I,P){let ie=ce(),L=Q1(ie,t,n,e,i,a,o,r,s,l,h,p,g,_,b,y,I,P);return L!==pt&&Eo(ie,Zt(),L),nu}function Eo(t,n,e){let i=Yp(n,t);AI(t[et],i,e)}var rx={};function Nr(t){Ci(`NgLet`);let n=Xe(),e=ce(),i=t+rt;return po(xo(n,i,128,null,null),!1),Fs(n,e,i,rx),Nr}function cl(t){return Fs(Xe(),ce(),Zt(),t),t}function Ar(t){let e=Ps(rh(),rt+t);if(e===rx)throw new q(314,!1);return e}function dy(t,n,e){let i=Xe();i.firstCreatePass&&sx(n,i.data,i.blueprint,Xn(t),e)}function sx(t,n,e,i,a){if(t=Lt(t),Array.isArray(t))for(let o=0;o<t.length;o++)sx(t[o],n,e,i,a);else{let o=Xe(),r=ce(),s=Tt(),l=to(t)?t:Lt(t.provide),h=Hp(t),p=s.providerIndexes&1048575,g=s.directiveStart,_=s.providerIndexes>>20;if(to(t)||!t.multi){let b=new vo(h,a,He,null),y=Ih(l,n,a?p:p+_,g);y===-1?(Th(Nd(s,r),o,l),Dh(o,t,n.length),n.push(l),s.directiveStart++,s.directiveEnd++,a&&(s.providerIndexes+=1048576),e.push(b),r.push(b)):(e[y]=b,r[y]=b)}else{let b=Ih(l,n,p+_,g),y=Ih(l,n,p,p+_),I=b>=0&&e[b],P=y>=0&&e[y];if(a&&!P||!a&&!I){Th(Nd(s,r),o,l);let ie=J1(a?Z1:X1,e.length,a,i,h,t);!a&&P&&(e[y].providerFactory=ie),Dh(o,t,n.length,0),n.push(l),s.directiveStart++,s.directiveEnd++,a&&(s.providerIndexes+=1048576),e.push(ie),r.push(ie)}else{let ie=lx(e[a?y:b],h,!a&&i);Dh(o,t,b>-1?b:y,ie)}!a&&i&&P&&e[y].componentProviders++}}}function Dh(t,n,e,i){let a=to(n),o=qv(n);if(a||o){let l=(o?Lt(n.useClass):n).prototype.ngOnDestroy;if(l){let h=t.destroyHooks||(t.destroyHooks=[]);if(!a&&n.multi){let p=h.indexOf(e);p===-1?h.push(e,[i,l]):h[p+1].push(i,l)}else h.push(e,l)}}}function lx(t,n,e){return e&&t.componentProviders++,t.multi.push(n)-1}function Ih(t,n,e,i){for(let a=e;a<i;a++)if(n[a]===t)return a;return-1}function X1(t,n,e,i,a){return uf(this.multi,[])}function Z1(t,n,e,i,a){let o=this.multi,r;if(this.providerFactory){let s=this.providerFactory.componentProviders,l=Gs(i,i[ve],this.providerFactory.index,a);r=l.slice(0,s),uf(o,r);for(let h=s;h<l.length;h++)r.push(l[h])}else r=[],uf(o,r);return r}function uf(t,n){for(let e=0;e<t.length;e++){let i=t[e];n.push(i())}return n}function J1(t,n,e,i,a,o){let r=new vo(t,e,He,null);return r.multi=[],r.index=n,r.componentProviders=0,lx(r,a,i&&!e),r}function Ge(t,n){return e=>{e.providersResolver=(i,a)=>dy(i,a?a(t):t,!1),n&&(e.viewProvidersResolver=(i,a)=>dy(i,a?a(n):n,!0))}}function nt(t,n){let e=ud()+t,i=ce();return i[e]===pt?jf(i,e,n()):tM(i,e)}function cx(t,n){let e=t[n];return e===pt?void 0:e}function eT(t,n,e,i,a,o){let r=n+e;return fn(t,r,a)?jf(t,r+1,o?i.call(o,a):i(a)):cx(t,r+1)}function tT(t,n,e,i,a,o,r){let s=n+e;return yr(t,s,a,o)?jf(t,s+2,r?i.call(r,a,o):i(a,o)):cx(t,s+2)}function j(t,n){let e=Xe(),i,a=t+rt;e.firstCreatePass?(i=nT(n,e.pipeRegistry),e.data[a]=i,i.onDestroy&&(e.destroyHooks??=[]).push(a,i.onDestroy)):i=e.data[a];let o=i.factory||(i.factory=pa(i.type,!0)),s=sn(He);try{let l=Td(!1),h=o();return Td(l),Fs(e,ce(),a,h),h}finally{sn(s)}}function nT(t,n){if(n)for(let e=n.length-1;e>=0;e--){let i=n[e];if(t===i.name)return i}}function _e(t,n,e){let i=t+rt,a=ce(),o=Ps(a,i);return dx(a,i)?eT(a,ud(),n,o.transform,e,o):o.transform(e)}function ke(t,n,e,i){let a=t+rt,o=ce(),r=Ps(o,a);return dx(o,a)?tT(o,ud(),n,r.transform,e,i,r):r.transform(e,i)}function dx(t,n){return t[ve].data[n].pure}function dl(t,n){return Xd(t,n)}var ux=(()=>{class t{applicationErrorHandler=f(Ui);appRef=f(On);taskService=f(fo);ngZone=f(Z);zonelessEnabled=f(zs);tracing=f(ii,{optional:!0});zoneIsDefined=typeof Zone<`u`&&!!Zone.root.run;schedulerTickApplyArgs=[{data:{__scheduler_tick__:!0}}];subscriptions=new De;angularZoneId=this.zoneIsDefined?this.ngZone._inner?.get(Es):null;scheduleInRootZone=!this.zonelessEnabled&&this.zoneIsDefined&&(f(gh,{optional:!0})??!1);cancelScheduledCallback=null;useMicrotaskScheduler=!1;runningTick=!1;pendingRenderTaskId=null;constructor(){this.subscriptions.add(this.appRef.afterTick.subscribe(()=>{let e=this.taskService.add();if(!this.runningTick&&(this.cleanup(),!this.zonelessEnabled||this.appRef.includeAllTestViews)){this.taskService.remove(e);return}this.switchToMicrotaskScheduler(),this.taskService.remove(e)})),this.subscriptions.add(this.ngZone.onUnstable.subscribe(()=>{this.runningTick||this.cleanup()}))}switchToMicrotaskScheduler(){this.ngZone.runOutsideAngular(()=>{let e=this.taskService.add();this.useMicrotaskScheduler=!0,queueMicrotask(()=>{this.useMicrotaskScheduler=!1,this.taskService.remove(e)})})}notify(e){if(!this.zonelessEnabled&&e===5)return;switch(e){case 0:case 2:this.appRef.dirtyFlags|=2;break;case 3:case 4:case 5:case 1:this.appRef.dirtyFlags|=4;break;case 6:this.appRef.dirtyFlags|=2;break;case 12:this.appRef.dirtyFlags|=16;break;case 13:this.appRef.dirtyFlags|=2;break;case 11:break;default:this.appRef.dirtyFlags|=8}if(this.appRef.tracingSnapshot=this.tracing?.snapshot(this.appRef.tracingSnapshot)??null,!this.shouldScheduleTick())return;let i=this.useMicrotaskScheduler?v_:mh;this.pendingRenderTaskId=this.taskService.add(),this.scheduleInRootZone?this.cancelScheduledCallback=Zone.root.run(()=>i(()=>this.tick())):this.cancelScheduledCallback=this.ngZone.runOutsideAngular(()=>i(()=>this.tick()))}shouldScheduleTick(){return!(this.appRef.destroyed||this.pendingRenderTaskId!==null||this.runningTick||this.appRef._runningTick||!this.zonelessEnabled&&this.zoneIsDefined&&Zone.current.get(Es+this.angularZoneId))}tick(){if(this.runningTick||this.appRef.destroyed)return;if(this.appRef.dirtyFlags===0){this.cleanup();return}!this.zonelessEnabled&&this.appRef.dirtyFlags&7&&(this.appRef.dirtyFlags|=1);let e=this.taskService.add();try{this.ngZone.run(()=>{this.runningTick=!0,this.appRef._tick()},void 0,this.schedulerTickApplyArgs)}catch(i){this.applicationErrorHandler(i)}finally{this.taskService.remove(e),this.cleanup()}}ngOnDestroy(){this.subscriptions.unsubscribe(),this.cleanup()}cleanup(){if(this.runningTick=!1,this.cancelScheduledCallback?.(),this.cancelScheduledCallback=null,this.pendingRenderTaskId!==null){let e=this.pendingRenderTaskId;this.pendingRenderTaskId=null,this.taskService.remove(e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function ag(){return Ci(`NgZoneless`),ln([...og(),[]])}function og(){return[{provide:gi,useExisting:ux},{provide:Z,useClass:Ds},{provide:zs,useValue:!0}]}function iT(){return typeof $localize<`u`&&$localize.locale||sl}var Rr=new N(``,{factory:()=>f(Rr,{optional:!0,skipSelf:!0})||iT()});var ul=class{destroyed=!1;listeners=null;errorHandler=f(Tn,{optional:!0});isEmitting=!1;hasNullListeners=!1;destroyRef=f(Ae);constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.listeners=null})}subscribe(n){if(this.destroyed)throw new q(953,!1);return(this.listeners??=[]).push(n),{unsubscribe:()=>{let e=this.listeners?this.listeners.indexOf(n):-1;e>-1&&(this.isEmitting?(this.hasNullListeners=!0,this.listeners[e]=null):this.listeners.splice(e,1))}}}emit(n){if(this.destroyed){console.warn(Bi(953,!1));return}if(this.listeners===null)return;this.isEmitting=!0;let e=ge(null);try{for(let i of this.listeners)try{i!==null&&i(n)}catch(a){this.errorHandler?.handleError(a)}}finally{this.hasNullListeners&&(this.hasNullListeners=!1,this.listeners&&aT(this.listeners)),ge(e),this.isEmitting=!1}}};function aT(t){let n=t.length-1;for(;n>-1;)t[n]===null&&t.splice(n,1),n--}function K(t,n){return bs(t,n?.equal)}function it(t){return tv(t)}(class t extends Error{_brand;constructor(n){super(n)}static IDLE=new t(`IDLE`);static LOADING=new t(`LOADING`)});function _x(t,n){let e=Object.create(mf);e.value=t,e.transformFn=n?.transform;function i(){if(Ri(e),e.value===wr)throw new q(-950,null);return e.value}return i[St]=e,i}var Ki=class{attributeName;constructor(n){this.attributeName=n}__NG_ELEMENT_ID__=()=>hf(this.attributeName);toString(){return`HostAttributeToken ${this.attributeName}`}};var yx=(()=>{let t=new N(``);return t.__NG_ELEMENT_ID__=n=>{let e=Tt();if(e===null)throw new q(-204,!1);if(e.type&2)return e.value;if(n&8)return null;throw new q(-204,!1)},t})();function au(t){return new ul}function px(t,n){return _x(t,n)}function vT(t){return _x(wr,t)}var zt=(px.required=vT,px);function xx(t,n){let e=Object.create(mf),i=new ul;e.value=t;function a(){return Ri(e),hx(e.value),e.value}return a[St]=e,a.asReadonly=bd.bind(a),a.set=o=>{e.equal(e.value,o)||(ua(e,o),i.emit(o))},a.update=o=>{hx(e.value),a.set(o(e.value))},a.subscribe=i.subscribe.bind(i),a.destroyRef=i.destroyRef,a}function hx(t){if(t===wr)throw new q(952,!1)}function fx(t,n){return xx(t,n)}function _T(t){return xx(wr,t)}var Cx=(fx.required=_T,fx);function gx(t,n){return qf(n)}function yT(t,n){return Yf(n)}var pl=(gx.required=yT,gx);function bx(t,n){return qf(n)}function xT(t,n){return Yf(n)}var Sx=(bx.required=xT,bx);var Le=(()=>{class t{static __NG_ELEMENT_ID__=ST}return t})();function ST(t){return wT(Tt(),ce(),(t&16)===16)}function wT(t,n,e){if(_i(t)&&!e){let i=Rn(t.index,n);return new Sa(i,i)}else if(t.type&175){let i=n[Xt];return new Sa(i,n)}return null}var sg=new N(``);var ET=new N(``);function ml(t){return!t.moduleRef}function DT(t){let n=ml(t)?t.r3Injector:t.moduleRef.injector,e=n.get(Z);return e.run(()=>{ml(t)?t.r3Injector.resolveInjectorInitializers():t.moduleRef.resolveInjectorInitializers();let i=n.get(Ui),a;if(e.runOutsideAngular(()=>{a=e.onError.subscribe({next:i})}),ml(t)){let o=()=>n.destroy(),r=t.platformInjector.get(sg);r.add(o),n.onDestroy(()=>{a.unsubscribe(),r.delete(o)})}else{let o=()=>t.moduleRef.destroy(),r=t.platformInjector.get(sg);r.add(o),t.moduleRef.onDestroy(()=>{$s(t.allPlatformModules,t.moduleRef),a.unsubscribe(),r.delete(o)})}return kT(i,e,()=>{let o=n.get(fo),r=o.add(),s=n.get(Xf);return s.runInitializers(),s.donePromise.then(()=>{if(Z0(n.get(Rr,sl)||sl),!n.get(ET,!0))return ml(t)?n.get(On):(t.allPlatformModules.push(t.moduleRef),t.moduleRef);if(ml(t)){let p=n.get(On);return t.rootComponent!==void 0&&p.bootstrap(t.rootComponent),p}else return IT?.(t.moduleRef,t.allPlatformModules),t.moduleRef}).finally(()=>{o.remove(r)})})})}var IT;function kT(t,n,e){try{let i=e();return So(i)?i.catch(a=>{throw n.runOutsideAngular(()=>t(a)),a}):i}catch(i){throw n.runOutsideAngular(()=>t(i)),i}}var iu=null;function MT(t=[],n){return se.create({name:n,providers:[{provide:As,useValue:`platform`},{provide:sg,useValue:new Set([()=>iu=null])},...t]})}function TT(t=[]){if(iu)return iu;let n=MT(t);return iu=n,Q0(),NT(n),n}function NT(t){let n=t.get(vd,null);rr(t,()=>{n?.forEach(e=>e())})}function wx(t){let{rootComponent:n,appProviders:e,platformProviders:i,platformRef:a}=t;Ke(ze.BootstrapApplicationStart);try{let o=a?.injector??TT(i);return DT({r3Injector:new Qs({providers:[og(),C_,...e||[]],parent:o,debugName:``,runEnvironmentInitializers:!1}).injector,platformInjector:o,rootComponent:n})}catch(o){return Promise.reject(o)}finally{Ke(ze.BootstrapApplicationEnd)}}function z(t){return typeof t==`boolean`?t:t!=null&&t!==`false`}function dt(t,n=NaN){return!isNaN(parseFloat(t))&&!isNaN(Number(t))?Number(t):n}var rg=Symbol(`NOT_SET`);var Ex=new Set;var AT=s(r({},Yo),{kind:`afterRenderEffectPhase`,consumerIsAlwaysLive:!0,consumerAllowSignalWrites:!0,value:rg,cleanup:null,consumerMarkedDirty(){if(this.sequence.impl.executing){if(this.sequence.lastPhase===null||this.sequence.lastPhase<this.phase)return;this.sequence.erroredOrDestroyed=!0}this.sequence.scheduler.notify(7)},phaseFn(t){if(this.sequence.lastPhase=this.phase,!this.dirty)return this.signal;if(this.dirty=!1,this.value!==rg&&!Wo(this))return this.signal;try{for(let a of this.cleanup??Ex)a()}finally{this.cleanup?.clear()}let n=[];t!==void 0&&n.push(t),n.push(this.registerCleanupFn);let e=ca(this),i;try{i=this.userFn.apply(null,n)}finally{Ga(this,e)}return(this.value===rg||!this.equal(this.value,i))&&(this.value=i,this.version++),this.signal}});var lg=class extends Ws{scheduler;lastPhase=null;nodes=[void 0,void 0,void 0,void 0];onDestroyFns=null;constructor(n,e,i,a,o,r=null,s){super(n,[void 0,void 0,void 0,void 0],i,!1,o.get(Ae),r),this.scheduler=a;for(let l of If){let h=e[l];if(h===void 0)continue;let p=Object.create(AT);p.sequence=this,p.phase=l,p.userFn=h,p.dirty=!0,p.signal=()=>(Ri(p),p.value),p.signal[St]=p,p.registerCleanupFn=g=>(p.cleanup??=new Set).add(g),this.nodes[l]=p,this.hooks[l]=g=>p.phaseFn(g)}}afterRun(){super.afterRun(),this.lastPhase=null}destroy(){if(this.onDestroyFns!==null)for(let n of this.onDestroyFns)n();super.destroy();for(let n of this.nodes)if(n)try{for(let e of n.cleanup??Ex)e()}finally{da(n)}}};function cg(t,n){let e=n?.injector??f(se),i=e.get(gi),a=e.get(Wd),o=e.get(ii,null,{optional:!0});a.impl??=e.get(kf);let r=t;typeof r==`function`&&(r={mixedReadWrite:t});let s=e.get(pr,null,{optional:!0}),l=new lg(a.impl,[r.earlyRead,r.write,r.mixedReadWrite,r.read],s?.view,i,e,o?.snapshot(null),void 0);return a.impl.register(l),l}function ou(t,n){let e=ha(t),i=n.elementInjector||or(),o=new xr(e).create(i,n.projectableNodes,n.hostElement,n.environmentInjector,n.directives,n.bindings);return n.onError&&(o.hostView._lView[cr]=n.onError),o}var Dx=null;function Fn(){return Dx}function dg(t){Dx??=t}var hl=class{};var Or=(()=>{class t{historyGo(e){throw new Error(``)}static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:()=>f(Ix),providedIn:`platform`})}return t})();var Ix=(()=>{class t extends Or{_location;_history;_doc=f(me);constructor(){super(),this._location=window.location,this._history=window.history}getBaseHrefFromDOM(){return Fn().getBaseHref(this._doc)}onPopState(e){let i=Fn().getGlobalEventTarget(this._doc,`window`);return i.addEventListener(`popstate`,e,!1),()=>i.removeEventListener(`popstate`,e)}onHashChange(e){let i=Fn().getGlobalEventTarget(this._doc,`window`);return i.addEventListener(`hashchange`,e,!1),()=>i.removeEventListener(`hashchange`,e)}get href(){return this._location.href}get protocol(){return this._location.protocol}get hostname(){return this._location.hostname}get port(){return this._location.port}get pathname(){return this._location.pathname}get search(){return this._location.search}get hash(){return this._location.hash}set pathname(e){this._location.pathname=e}pushState(e,i,a){this._history.pushState(e,i,a)}replaceState(e,i,a){this._history.replaceState(e,i,a)}forward(){this._history.forward()}back(){this._history.back()}historyGo(e=0){this._history.go(e)}getState(){return this._history.state}static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:()=>new t,providedIn:`platform`})}return t})();function Tx(t,n){return t?n?t.endsWith(`/`)?n.startsWith(`/`)?t+n.slice(1):t+n:n.startsWith(`/`)?t+n:`${t}/${n}`:t:n}function kx(t){let n=t.search(/#|\?|$/);return t[n-1]===`/`?t.slice(0,n-1)+t.slice(n):t}function Da(t){return t&&t[0]!==`?`?`?${t}`:t}var ru=(()=>{class t{historyGo(e){throw new Error(``)}static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:()=>f(OT),providedIn:`root`})}return t})();var RT=new N(``);var OT=(()=>{class t extends ru{_platformLocation;_baseHref;_removeListenerFns=[];constructor(e,i){super(),this._platformLocation=e,this._baseHref=i??this._platformLocation.getBaseHrefFromDOM()??f(me).location?.origin??``}ngOnDestroy(){for(;this._removeListenerFns.length;)this._removeListenerFns.pop()()}onPopState(e){this._removeListenerFns.push(this._platformLocation.onPopState(e),this._platformLocation.onHashChange(e))}getBaseHref(){return this._baseHref}prepareExternalUrl(e){return Tx(this._baseHref,e)}path(e=!1){let i=this._platformLocation.pathname+Da(this._platformLocation.search),a=this._platformLocation.hash;return a&&e?`${i}${a}`:i}pushState(e,i,a,o){let r=this.prepareExternalUrl(a+Da(o));this._platformLocation.pushState(e,i,r)}replaceState(e,i,a,o){let r=this.prepareExternalUrl(a+Da(o));this._platformLocation.replaceState(e,i,r)}forward(){this._platformLocation.forward()}back(){this._platformLocation.back()}getState(){return this._platformLocation.getState()}historyGo(e=0){this._platformLocation.historyGo?.(e)}static ɵfac=function(i){return new(i||t)(Se(Or),Se(RT,8))};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();var su=(()=>{class t{_subject=new V;_basePath;_locationStrategy;_urlChangeListeners=[];_urlChangeSubscription=null;constructor(e){this._locationStrategy=e;let i=this._locationStrategy.getBaseHref();this._basePath=LT(kx(Mx(i))),this._locationStrategy.onPopState(a=>{let o={url:this.path(!0),pop:!0,state:a.state,type:a.type};a.hasUAVisualTransition&&(o.hasUAVisualTransition=!0),this._subject.next(o)})}ngOnDestroy(){this._urlChangeSubscription?.unsubscribe(),this._urlChangeListeners=[]}path(e=!1){return this.normalize(this._locationStrategy.path(e))}getState(){return this._locationStrategy.getState()}isCurrentPathEqualTo(e,i=``){return this.path()==this.normalize(e+Da(i))}normalize(e){return t.stripTrailingSlash(FT(this._basePath,Mx(e)))}prepareExternalUrl(e){return e&&e[0]!==`/`&&(e=`/`+e),this._locationStrategy.prepareExternalUrl(e)}go(e,i=``,a=null){this._locationStrategy.pushState(a,``,e,i),this._notifyUrlChangeListeners(this.prepareExternalUrl(e+Da(i)),a)}replaceState(e,i=``,a=null){this._locationStrategy.replaceState(a,``,e,i),this._notifyUrlChangeListeners(this.prepareExternalUrl(e+Da(i)),a)}forward(){this._locationStrategy.forward()}back(){this._locationStrategy.back()}historyGo(e=0){this._locationStrategy.historyGo?.(e)}onUrlChange(e){return this._urlChangeListeners.push(e),this._urlChangeSubscription??=this.subscribe(i=>{this._notifyUrlChangeListeners(i.url,i.state)}),()=>{let i=this._urlChangeListeners.indexOf(e);this._urlChangeListeners.splice(i,1),this._urlChangeListeners.length===0&&(this._urlChangeSubscription?.unsubscribe(),this._urlChangeSubscription=null)}}_notifyUrlChangeListeners(e=``,i){this._urlChangeListeners.forEach(a=>a(e,i))}subscribe(e,i,a){return this._subject.subscribe({next:e,error:i??void 0,complete:a??void 0})}static normalizeQueryParams=Da;static joinWithSlash=Tx;static stripTrailingSlash=kx;static ɵfac=function(i){return new(i||t)(Se(ru))};static ɵprov=be({token:t,factory:()=>PT(),providedIn:`root`})}return t})();function PT(){return new su(Se(ru))}function FT(t,n){if(!t||!n.startsWith(t))return n;let e=n.substring(t.length);return e===``||[`/`,`;`,`?`,`#`].includes(e[0])?e:n}function Mx(t){return t.replace(/\/index\.html$/,``)}function LT(t){if(new RegExp(`^(https?:)?//`).test(t)){let[,e]=t.split(/\/\/[^\/]+/);return e}return t}var gg=(function(t){return t[t.Decimal=0]=`Decimal`,t[t.Percent=1]=`Percent`,t[t.Currency=2]=`Currency`,t[t.Scientific=3]=`Scientific`,t})(gg||{});var en=(function(t){return t[t.Format=0]=`Format`,t[t.Standalone=1]=`Standalone`,t})(en||{});var at=(function(t){return t[t.Narrow=0]=`Narrow`,t[t.Abbreviated=1]=`Abbreviated`,t[t.Wide=2]=`Wide`,t[t.Short=3]=`Short`,t})(at||{});var bn=(function(t){return t[t.Short=0]=`Short`,t[t.Medium=1]=`Medium`,t[t.Long=2]=`Long`,t[t.Full=3]=`Full`,t})(bn||{});var un={Decimal:0,Group:1,List:2,PercentSign:3,PlusSign:4,MinusSign:5,Exponential:6,SuperscriptingExponent:7,PerMille:8,Infinity:9,NaN:10,TimeSeparator:11,CurrencyDecimal:12,CurrencyGroup:13};function Rx(t){return gn(t)[Dt.LocaleId]}function Ox(t,n,e){let i=gn(t);return Ln(Ln([i[Dt.DayPeriodsFormat],i[Dt.DayPeriodsStandalone]],n),e)}function Px(t,n,e){let i=gn(t);return Ln(Ln([i[Dt.DaysFormat],i[Dt.DaysStandalone]],n),e)}function Fx(t,n,e){let i=gn(t);return Ln(Ln([i[Dt.MonthsFormat],i[Dt.MonthsStandalone]],n),e)}function Lx(t,n){let i=gn(t)[Dt.Eras];return Ln(i,n)}function fl(t,n){return Ln(gn(t)[Dt.DateFormat],n)}function gl(t,n){return Ln(gn(t)[Dt.TimeFormat],n)}function bl(t,n){let i=gn(t)[Dt.DateTimeFormat];return Ln(i,n)}function wi(t,n){let e=gn(t),i=e[Dt.NumberSymbols][n];if(typeof i>`u`){if(n===un.CurrencyDecimal)return e[Dt.NumberSymbols][un.Decimal];if(n===un.CurrencyGroup)return e[Dt.NumberSymbols][un.Group]}return i}function Vx(t,n){return gn(t)[Dt.NumberFormats][n]}function Bx(t){if(!t[Dt.ExtraData])throw new q(2303,!1)}function jx(t){let n=gn(t);return Bx(n),(n[Dt.ExtraData][2]||[]).map(i=>typeof i==`string`?ug(i):[ug(i[0]),ug(i[1])])}function zx(t,n,e){let i=gn(t);Bx(i);return Ln(Ln([i[Dt.ExtraData][0],i[Dt.ExtraData][1]],n)||[],e)||[]}function Ln(t,n){for(let e=n;e>-1;e--)if(typeof t[e]<`u`)return t[e];throw new q(2304,!1)}function ug(t){let[n,e]=t.split(`:`);return{hours:+n,minutes:+e}}var BT=/^(\d{4,})-?(\d\d)-?(\d\d)(?:T(\d\d)(?::?(\d\d)(?::?(\d\d)(?:\.(\d+))?)?)?(Z|([+-])(\d\d):?(\d\d))?)?$/;var lu=Object.create(null);var jT=/((?:[^BEGHLMOSWYZabcdhmswyz']+)|(?:'(?:[^']|'')*')|(?:G{1,5}|y{1,4}|Y{1,4}|M{1,5}|L{1,5}|w{1,2}|W{1}|d{1,2}|E{1,6}|c{1,6}|a{1,5}|b{1,5}|B{1,5}|h{1,2}|H{1,2}|m{1,2}|s{1,2}|S{1,3}|z{1,4}|Z{1,5}|O{1,4}))([\s\S]*)/;var zT=256;function Hx(t,n,e,i){let a=XT(t);HT(n),n=Qi(e,n)||n;let r=[],s;for(;n;)if(s=jT.exec(n),s){r=r.concat(s.slice(1));let p=r.pop();if(!p)break;n=p}else{r.push(n);break}let l=a.getTimezoneOffset();i&&(l=$x(i,l),a=QT(a,i));let h=``;return r.forEach(p=>{let g=YT(p);h+=g?g(a,e,l):p===`''`?`'`:p.replace(/(^'|'$)/g,``).replace(/''/g,`'`)}),h}function HT(t){if(t.length>zT)throw new q(2300,!1)}function pu(t,n,e){let i=new Date(0);return i.setFullYear(t,n,e),i.setHours(0,0,0),i}function Qi(t,n){let e=Rx(t);if(lu[e]??=Object.create(null),lu[e][n])return lu[e][n];let i=``;switch(n){case`shortDate`:i=fl(t,bn.Short);break;case`mediumDate`:i=fl(t,bn.Medium);break;case`longDate`:i=fl(t,bn.Long);break;case`fullDate`:i=fl(t,bn.Full);break;case`shortTime`:i=gl(t,bn.Short);break;case`mediumTime`:i=gl(t,bn.Medium);break;case`longTime`:i=gl(t,bn.Long);break;case`fullTime`:i=gl(t,bn.Full);break;case`short`:let a=Qi(t,`shortTime`),o=Qi(t,`shortDate`);i=cu(bl(t,bn.Short),[a,o]);break;case`medium`:let r=Qi(t,`mediumTime`),s=Qi(t,`mediumDate`);i=cu(bl(t,bn.Medium),[r,s]);break;case`long`:let l=Qi(t,`longTime`),h=Qi(t,`longDate`);i=cu(bl(t,bn.Long),[l,h]);break;case`full`:let p=Qi(t,`fullTime`),g=Qi(t,`fullDate`);i=cu(bl(t,bn.Full),[p,g])}return i&&(lu[e][n]=i),i}function cu(t,n){return n&&(t=t.replace(/\{([^}]+)}/g,function(e,i){return Object.hasOwn(n,i)?n[i]:e})),t}function oi(t,n,e=`-`,i,a){let o=``;(t<0||a&&t<=0)&&(a?t=-t+1:(t=-t,o=e));let r=String(t);for(;r.length<n;)r=`0`+r;return i&&(r=r.slice(r.length-n)),o+r}function UT(t,n){return oi(t,3).substring(0,n)}function Pt(t,n,e=0,i=!1,a=!1){return function(o,r){let s=$T(t,o);if((e>0||s>-e)&&(s+=e),t===3)s===0&&e===-12&&(s=12);else if(t===6)return UT(s,n);let l=wi(r,un.MinusSign);return oi(s,n,l,i,a)}}function $T(t,n){switch(t){case 0:return n.getFullYear();case 1:return n.getMonth();case 2:return n.getDate();case 3:return n.getHours();case 4:return n.getMinutes();case 5:return n.getSeconds();case 6:return n.getMilliseconds();case 7:return n.getDay();default:throw new q(2301,!1)}}function ut(t,n,e=en.Format,i=!1){return function(a,o){return GT(a,o,t,n,e,i)}}function GT(t,n,e,i,a,o){switch(e){case 2:return Fx(n,a,i)[t.getMonth()];case 1:return Px(n,a,i)[t.getDay()];case 0:let r=t.getHours(),s=t.getMinutes();if(o){let h=jx(n),p=zx(n,a,i),g=h.findIndex(_=>{if(Array.isArray(_)){let[b,y]=_,I=r>=b.hours&&s>=b.minutes,P=r<y.hours||r===y.hours&&s<y.minutes;if(b.hours<y.hours){if(I&&P)return!0}else if(I||P)return!0}else if(_.hours===r&&_.minutes===s)return!0;return!1});if(g!==-1)return p[g]}return Ox(n,a,i)[r<12?0:1];case 3:return Lx(n,i)[t.getFullYear()<=0?0:1];default:throw new q(2302,!1)}}function du(t){return function(n,e,i){let a=-1*i,o=wi(e,un.MinusSign),r=a>0?Math.floor(a/60):Math.ceil(a/60);switch(t){case 0:return(a>=0?`+`:``)+oi(r,2,o)+oi(Math.abs(a%60),2,o);case 1:return`GMT`+(a>=0?`+`:``)+oi(r,1,o);case 2:return`GMT`+(a>=0?`+`:``)+oi(r,2,o)+`:`+oi(Math.abs(a%60),2,o);case 3:return i===0?`Z`:(a>=0?`+`:``)+oi(r,2,o)+`:`+oi(Math.abs(a%60),2,o);default:throw new q(2310,!1)}}}var WT=0;var mu=4;function qT(t){let n=pu(t,WT,1).getDay();return pu(t,0,1+(n<=mu?mu:mu+7)-n)}function Ux(t){let n=t.getDay(),e=n===0?-3:mu-n;return pu(t.getFullYear(),t.getMonth(),t.getDate()+e)}function mg(t,n=!1){return function(e,i){let a;if(n){let o=new Date(e.getFullYear(),e.getMonth(),1).getDay()-1,r=e.getDate();a=1+Math.floor((r+o)/7)}else{let o=Ux(e),r=qT(o.getFullYear()),s=o.getTime()-r.getTime();a=1+Math.round(s/6048e5)}return oi(a,t,wi(i,un.MinusSign))}}function uu(t,n=!1){return function(e,i){return oi(Ux(e).getFullYear(),t,wi(i,un.MinusSign),n)}}var pg=Object.create(null);function YT(t){if(pg[t])return pg[t];let n;switch(t){case`G`:case`GG`:case`GGG`:n=ut(3,at.Abbreviated);break;case`GGGG`:n=ut(3,at.Wide);break;case`GGGGG`:n=ut(3,at.Narrow);break;case`y`:n=Pt(0,1,0,!1,!0);break;case`yy`:n=Pt(0,2,0,!0,!0);break;case`yyy`:n=Pt(0,3,0,!1,!0);break;case`yyyy`:n=Pt(0,4,0,!1,!0);break;case`Y`:n=uu(1);break;case`YY`:n=uu(2,!0);break;case`YYY`:n=uu(3);break;case`YYYY`:n=uu(4);break;case`M`:case`L`:n=Pt(1,1,1);break;case`MM`:case`LL`:n=Pt(1,2,1);break;case`MMM`:n=ut(2,at.Abbreviated);break;case`MMMM`:n=ut(2,at.Wide);break;case`MMMMM`:n=ut(2,at.Narrow);break;case`LLL`:n=ut(2,at.Abbreviated,en.Standalone);break;case`LLLL`:n=ut(2,at.Wide,en.Standalone);break;case`LLLLL`:n=ut(2,at.Narrow,en.Standalone);break;case`w`:n=mg(1);break;case`ww`:n=mg(2);break;case`W`:n=mg(1,!0);break;case`d`:n=Pt(2,1);break;case`dd`:n=Pt(2,2);break;case`c`:case`cc`:n=Pt(7,1);break;case`ccc`:n=ut(1,at.Abbreviated,en.Standalone);break;case`cccc`:n=ut(1,at.Wide,en.Standalone);break;case`ccccc`:n=ut(1,at.Narrow,en.Standalone);break;case`cccccc`:n=ut(1,at.Short,en.Standalone);break;case`E`:case`EE`:case`EEE`:n=ut(1,at.Abbreviated);break;case`EEEE`:n=ut(1,at.Wide);break;case`EEEEE`:n=ut(1,at.Narrow);break;case`EEEEEE`:n=ut(1,at.Short);break;case`a`:case`aa`:case`aaa`:n=ut(0,at.Abbreviated);break;case`aaaa`:n=ut(0,at.Wide);break;case`aaaaa`:n=ut(0,at.Narrow);break;case`b`:case`bb`:case`bbb`:n=ut(0,at.Abbreviated,en.Standalone,!0);break;case`bbbb`:n=ut(0,at.Wide,en.Standalone,!0);break;case`bbbbb`:n=ut(0,at.Narrow,en.Standalone,!0);break;case`B`:case`BB`:case`BBB`:n=ut(0,at.Abbreviated,en.Format,!0);break;case`BBBB`:n=ut(0,at.Wide,en.Format,!0);break;case`BBBBB`:n=ut(0,at.Narrow,en.Format,!0);break;case`h`:n=Pt(3,1,-12);break;case`hh`:n=Pt(3,2,-12);break;case`H`:n=Pt(3,1);break;case`HH`:n=Pt(3,2);break;case`m`:n=Pt(4,1);break;case`mm`:n=Pt(4,2);break;case`s`:n=Pt(5,1);break;case`ss`:n=Pt(5,2);break;case`S`:n=Pt(6,1);break;case`SS`:n=Pt(6,2);break;case`SSS`:n=Pt(6,3);break;case`Z`:case`ZZ`:case`ZZZ`:n=du(0);break;case`ZZZZZ`:n=du(3);break;case`O`:case`OO`:case`OOO`:case`z`:case`zz`:case`zzz`:n=du(1);break;case`OOOO`:case`ZZZZ`:case`zzzz`:n=du(2);break;default:return null}return pg[t]=n,n}function $x(t,n){t=t.replace(/:/g,``);let e=Date.parse(`Jan 01, 1970 00:00:00 `+t)/6e4;return isNaN(e)?n:e}function KT(t,n){return t=new Date(t.getTime()),t.setMinutes(t.getMinutes()+n),t}function QT(t,n,e){let a=t.getTimezoneOffset();return KT(t,-1*($x(n,a)-a))}function XT(t){if(Nx(t))return t;if(typeof t==`number`&&!isNaN(t))return new Date(t);if(typeof t==`string`){if(t=t.trim(),/^(\d{4}(-\d{1,2}(-\d{1,2})?)?)$/.test(t)){let[a,o=1,r=1]=t.split(`-`).map(s=>+s);return pu(a,o-1,r)}let e=parseFloat(t);if(!isNaN(t-e))return new Date(e);let i;if(i=t.match(BT))return ZT(i)}let n=new Date(t);if(!Nx(n))throw new q(2311,!1);return n}function ZT(t){let n=new Date(0),e=0,i=0,a=t[8]?n.setUTCFullYear:n.setFullYear,o=t[8]?n.setUTCHours:n.setHours;t[9]&&(e=Number(t[9]+t[10]),i=Number(t[9]+t[11])),a.call(n,Number(t[1]),Number(t[2])-1,Number(t[3]));let r=Number(t[4]||0)-e,s=Number(t[5]||0)-i,l=Number(t[6]||0),h=Math.floor(parseFloat(`0.`+(t[7]||0))*1e3);return o.call(n,r,s,l,h),n}function Nx(t){return t instanceof Date&&!isNaN(t.valueOf())}var JT=/^(\d+)?\.((\d+)(-(\d+))?)?$/;var Ax=22;var hu=`.`;var vl=`0`;var eN=`;`;var tN=`,`;var hg=`#`;function nN(t,n,e,i,a,o,r=!1){let s=``,l=!1;if(!isFinite(t))s=wi(e,Number.isNaN(t)?un.NaN:un.Infinity);else{let h=oN(t);r&&(h=aN(h));let p=n.minInt,g=n.minFrac,_=n.maxFrac;if(o){let L=o.match(JT);if(L===null)throw new q(2306,!1);let x=L[1],A=L[3],R=L[5];x!=null&&(p=fg(x)),A!=null&&(g=fg(A)),R!=null?_=fg(R):A!=null&&g>_&&(_=g);let W=100;if(p>W||g>W||_>W)throw new q(2306,!1)}rN(h,g,_);let b=h.digits,y=h.integerLen,I=h.exponent,P=[];for(l=b.every(L=>!L);y<p;y++)b.unshift(0);for(;y<0;y++)b.unshift(0);y>0?P=b.splice(y,b.length):(P=b,b=[0]);let ie=[];for(b.length>=n.lgSize&&ie.unshift(b.splice(-n.lgSize,b.length).join(``));b.length>n.gSize;)ie.unshift(b.splice(-n.gSize,b.length).join(``));b.length&&ie.unshift(b.join(``)),s=ie.join(wi(e,i)),P.length&&(s+=wi(e,a)+P.join(``)),I&&(s+=wi(e,un.Exponential)+`+`+I)}return t<0&&!l?s=n.negPre+s+n.negSuf:s=n.posPre+s+n.posSuf,s}function Gx(t,n,e){return nN(t,iN(Vx(n,gg.Decimal),wi(n,un.MinusSign)),n,un.Group,un.Decimal,e)}function iN(t,n=`-`){let e={minInt:1,minFrac:0,maxFrac:0,posPre:``,posSuf:``,negPre:``,negSuf:``,gSize:0,lgSize:0},i=t.split(eN),a=i[0],o=i[1],r=a.indexOf(hu)!==-1?a.split(hu):[a.substring(0,a.lastIndexOf(vl)+1),a.substring(a.lastIndexOf(vl)+1)],s=r[0],l=r[1]||``;e.posPre=s.substring(0,s.indexOf(hg));for(let p=0;p<l.length;p++){let g=l.charAt(p);g===vl?e.minFrac=e.maxFrac=p+1:g===hg?e.maxFrac=p+1:e.posSuf+=g}let h=s.split(tN);if(e.gSize=h[1]?h[1].length:0,e.lgSize=h[2]||h[1]?(h[2]||h[1]).length:0,o){let p=a.length-e.posPre.length-e.posSuf.length,g=o.indexOf(hg);e.negPre=o.substring(0,g).replace(/'/g,``),e.negSuf=o.slice(g+p).replace(/'/g,``)}else e.negPre=n+e.posPre,e.negSuf=e.posSuf;return e}function aN(t){if(t.digits[0]===0)return t;let n=t.digits.length-t.integerLen;return t.exponent?t.exponent+=2:(n===0?t.digits.push(0,0):n===1&&t.digits.push(0),t.integerLen+=2),t}function oN(t){let n=Math.abs(t)+``,e=0,i,a,o,r,s;for((a=n.indexOf(hu))>-1&&(n=n.replace(hu,``)),(o=n.search(/e/i))>0?(a<0&&(a=o),a+=+n.slice(o+1),n=n.substring(0,o)):a<0&&(a=n.length),o=0;n.charAt(o)===vl;o++);if(o===(s=n.length))i=[0],a=1;else{for(s--;n.charAt(s)===vl;)s--;for(a-=o,i=[],r=0;o<=s;o++,r++)i[r]=Number(n.charAt(o))}return a>Ax&&(i=i.splice(0,Ax-1),e=a-1,a=1),{digits:i,exponent:e,integerLen:a}}function rN(t,n,e){if(n>e)throw new q(2307,!1);let i=t.digits,a=i.length-t.integerLen,o=Math.min(Math.max(n,a),e),r=o+t.integerLen,s=i[r];if(r>0){i.splice(Math.max(t.integerLen,r));for(let g=r;g<i.length;g++)i[g]=0}else{a=Math.max(0,a),t.integerLen=1,i.length=Math.max(1,r=o+1),i[0]=0;for(let g=1;g<r;g++)i[g]=0}if(s>=5)if(r-1<0){for(let g=0;g>r;g--)i.unshift(0),t.integerLen++;i.unshift(1),t.integerLen++}else i[r-1]++;for(;a<Math.max(0,o);a++)i.push(0);let l=o!==0,h=n+t.integerLen,p=i.reduceRight(function(g,_,b,y){return _=_+g,y[b]=_<10?_:_-10,l&&(y[b]===0&&b>=h?y.pop():l=!1),_>=10?1:0},0);p&&(i.unshift(p),t.integerLen++)}function fg(t){let n=parseInt(t);if(isNaN(n))throw new q(2305,!1);return n}var bg=(()=>{class t{_viewContainerRef;ngComponentOutlet=null;ngComponentOutletInputs;ngComponentOutletInjector;ngComponentOutletEnvironmentInjector;ngComponentOutletContent;ngComponentOutletNgModule;_componentRef;_moduleRef;_inputsUsed=new Map;get componentInstance(){return this._componentRef?.instance??null}constructor(e){this._viewContainerRef=e}_needToReCreateNgModuleInstance(e){return e.ngComponentOutletNgModule!==void 0}_needToReCreateComponentInstance(e){return e.ngComponentOutlet!==void 0||e.ngComponentOutletContent!==void 0||e.ngComponentOutletInjector!==void 0||e.ngComponentOutletEnvironmentInjector!==void 0||this._needToReCreateNgModuleInstance(e)}ngOnChanges(e){if(this._needToReCreateComponentInstance(e)&&(this._viewContainerRef.clear(),this._inputsUsed.clear(),this._componentRef=void 0,this.ngComponentOutlet)){let i=this.ngComponentOutletInjector||this._viewContainerRef.parentInjector;this._needToReCreateNgModuleInstance(e)&&(this._moduleRef?.destroy(),this.ngComponentOutletNgModule?this._moduleRef=Qf(this.ngComponentOutletNgModule,sN(i)):this._moduleRef=void 0),this._componentRef=this._viewContainerRef.createComponent(this.ngComponentOutlet,{injector:i,ngModuleRef:this._moduleRef,projectableNodes:this.ngComponentOutletContent,environmentInjector:this.ngComponentOutletEnvironmentInjector})}}ngDoCheck(){if(this._componentRef){if(this.ngComponentOutletInputs)for(let e of Object.keys(this.ngComponentOutletInputs))this._inputsUsed.set(e,!0);this._applyInputStateDiff(this._componentRef)}}ngOnDestroy(){this._moduleRef?.destroy()}_applyInputStateDiff(e){for(let[i,a]of this._inputsUsed)a?(e.setInput(i,this.ngComponentOutletInputs[i]),this._inputsUsed.set(i,!1)):(e.setInput(i,void 0),this._inputsUsed.delete(i))}static ɵfac=function(i){return new(i||t)(He(Bt))};static ɵdir=Q({type:t,selectors:[[``,`ngComponentOutlet`,``]],inputs:{ngComponentOutlet:`ngComponentOutlet`,ngComponentOutletInputs:`ngComponentOutletInputs`,ngComponentOutletInjector:`ngComponentOutletInjector`,ngComponentOutletEnvironmentInjector:`ngComponentOutletEnvironmentInjector`,ngComponentOutletContent:`ngComponentOutletContent`,ngComponentOutletNgModule:`ngComponentOutletNgModule`},exportAs:[`ngComponentOutlet`],features:[_t]})}return t})();function sN(t){return t.get(ni).injector}var _l=(()=>{class t{_viewContainerRef;_viewRef=null;ngTemplateOutletContext=null;ngTemplateOutlet=null;ngTemplateOutletInjector=null;injector=f(se);constructor(e){this._viewContainerRef=e}ngOnChanges(e){if(this._shouldRecreateView(e)){let i=this._viewContainerRef;if(this._viewRef&&i.remove(i.indexOf(this._viewRef)),!this.ngTemplateOutlet){this._viewRef=null;return}let a=this._createContextForwardProxy();this._viewRef=i.createEmbeddedView(this.ngTemplateOutlet,a,{injector:this._getInjector()})}}_getInjector(){return this.ngTemplateOutletInjector===`outlet`?this.injector:this.ngTemplateOutletInjector??void 0}_shouldRecreateView(e){return!!e.ngTemplateOutlet||!!e.ngTemplateOutletInjector}_createContextForwardProxy(){return new Proxy({},{set:(e,i,a)=>this.ngTemplateOutletContext?Reflect.set(this.ngTemplateOutletContext,i,a):!1,get:(e,i,a)=>{if(this.ngTemplateOutletContext)return Reflect.get(this.ngTemplateOutletContext,i,a)}})}static ɵfac=function(i){return new(i||t)(He(Bt))};static ɵdir=Q({type:t,selectors:[[``,`ngTemplateOutlet`,``]],inputs:{ngTemplateOutletContext:`ngTemplateOutletContext`,ngTemplateOutlet:`ngTemplateOutlet`,ngTemplateOutletInjector:`ngTemplateOutletInjector`},features:[_t]})}return t})();function Wx(t,n){return new q(2100,!1)}var lN=`mediumDate`;var qx=new N(``);var Yx=new N(``);var Xi=(()=>{class t{locale;defaultTimezone;defaultOptions;constructor(e,i,a){this.locale=e,this.defaultTimezone=i,this.defaultOptions=a}transform(e,i,a,o){if(e==null||e===``||e!==e)return null;try{let r=i??this.defaultOptions?.dateFormat??lN,s=a??this.defaultOptions?.timezone??this.defaultTimezone??void 0;return Hx(e,r,o||this.locale,s)}catch(r){throw Wx(t,r.message)}}static ɵfac=function(i){return new(i||t)(He(Rr,16),He(qx,24),He(Yx,24))};static ɵpipe=ol({name:`date`,type:t,pure:!0})}return t})();var Do=(()=>{class t{transform(e){return JSON.stringify(e,null,2)}static ɵfac=function(i){return new(i||t)};static ɵpipe=ol({name:`json`,type:t,pure:!1})}return t})();var En=(()=>{class t{_locale;constructor(e){this._locale=e}transform(e,i,a){if(!cN(e))return null;a||=this._locale;try{return Gx(dN(e),a,i)}catch(o){throw Wx(t,o.message)}}static ɵfac=function(i){return new(i||t)(He(Rr,16))};static ɵpipe=ol({name:`number`,type:t,pure:!0})}return t})();function cN(t){return!(t==null||t===``||t!==t)}function dN(t){if(typeof t==`string`&&!isNaN(Number(t)-parseFloat(t)))return Number(t);if(typeof t!=`number`)throw new q(2309,!1);return t}function yl(t,n){n=encodeURIComponent(n);for(let e of t.split(`;`)){let i=e.indexOf(`=`),[a,o]=i==-1?[e,``]:[e.slice(0,i),e.slice(i+1)];if(a.trim()!==n)continue;let r=o;try{r=decodeURIComponent(o)}catch{}return r.length>1&&r[0]===`"`&&r[r.length-1]===`"`&&(r=r.slice(1,-1)),r}return null}var vg=`browser`;var hN=`server`;function Kx(t){return t===vg}function Qx(t){return t===hN}var xl=class{_doc;constructor(n){this._doc=n}manager};var fu=(()=>{class t extends xl{constructor(e){super(e)}supports(e){return!0}addEventListener(e,i,a,o){return e.addEventListener(i,a,o),()=>this.removeEventListener(e,i,a,o)}removeEventListener(e,i,a,o){return e.removeEventListener(i,a,o)}static ɵfac=function(i){return new(i||t)(Se(me))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var vu=new N(``);var Cg=(()=>{class t{_zone;_plugins;_eventNameToPlugin=new Map;constructor(e,i){this._zone=i,e.forEach(r=>{r.manager=this});let a=e.filter(r=>!(r instanceof fu));this._plugins=a.slice().reverse();let o=e.find(r=>r instanceof fu);o&&this._plugins.push(o)}addEventListener(e,i,a,o){return this._findPluginFor(i).addEventListener(e,i,a,o)}getZone(){return this._zone}_findPluginFor(e){let i=this._eventNameToPlugin.get(e);if(i)return i;if(i=this._plugins.find(o=>o.supports(e)),!i)throw new q(-5101,!1);return this._eventNameToPlugin.set(e,i),i}static ɵfac=function(i){return new(i||t)(Se(vu),Se(Z))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var _g=`ng-app-id`;function Xx(t){for(let n of t)n.remove()}function Zx(t,n){let e=n.createElement(`style`);return e.textContent=t,e}function fN(t,n,e,i){let a=t.head?.querySelectorAll(`style[${_g}="${n}"],link[${_g}="${n}"]`);if(!a||a.length===0)return!1;for(let o of a)o.removeAttribute(_g),o instanceof HTMLLinkElement?i.set(o.href.slice(o.href.lastIndexOf(`/`)+1),{usage:0,elements:[o]}):o.textContent&&e.set(o.textContent,{usage:0,elements:[o]});return!0}function xg(t,n){let e=n.createElement(`link`);return e.setAttribute(`rel`,`stylesheet`),e.setAttribute(`href`,t),e}var Sg=(()=>{class t{doc;appId;nonce;inline=new Map;external=new Map;hosts=new Set;constructor(e,i,a,o={}){this.doc=e,this.appId=i,this.nonce=a,fN(e,i,this.inline,this.external)&&this.hosts.add(e.head)}addStyles(e,i){for(let a of e)this.addUsage(a,this.inline,Zx);i?.forEach(a=>this.addUsage(a,this.external,xg))}removeStyles(e,i){for(let a of e)this.removeUsage(a,this.inline);i?.forEach(a=>this.removeUsage(a,this.external))}addUsage(e,i,a){let o=i.get(e);o?o.usage++:i.set(e,{usage:1,elements:[...this.hosts].map(r=>this.addElement(r,a(e,this.doc)))})}removeUsage(e,i){let a=i.get(e);a&&(a.usage--,a.usage<=0&&(Xx(a.elements),i.delete(e)))}ngOnDestroy(){for(let[,{elements:e}]of[...this.inline,...this.external])Xx(e);this.hosts.clear()}addHost(e){if(!this.hosts.has(e)){this.hosts.add(e);for(let[i,{elements:a}]of this.inline)a.push(this.addElement(e,Zx(i,this.doc)));for(let[i,{elements:a}]of this.external)a.push(this.addElement(e,xg(i,this.doc)))}}removeHost(e){this.hosts.delete(e);for(let i of[...this.inline.values(),...this.external.values()]){let a=[];for(let o of i.elements)o.parentNode===e?o.remove():a.push(o);i.elements=a}}addElement(e,i){return this.nonce&&i.setAttribute(`nonce`,this.nonce),e.appendChild(i)}static ɵfac=function(i){return new(i||t)(Se(me),Se($i),Se(xa,8),Se(Gi))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var yg={svg:`http://www.w3.org/2000/svg`,xhtml:`http://www.w3.org/1999/xhtml`,xlink:`http://www.w3.org/1999/xlink`,xml:`http://www.w3.org/XML/1998/namespace`,xmlns:`http://www.w3.org/2000/xmlns/`,math:`http://www.w3.org/1998/Math/MathML`};var wg=/%COMP%/g;var eC=`%COMP%`;var gN=`_nghost-${eC}`;var bN=`_ngcontent-${eC}`;var vN=!0;var _N=new N(``,{factory:()=>vN});var yN=new N(``);function xN(t){return bN.replace(wg,t)}function CN(t){return gN.replace(wg,t)}function tC(t,n){return n.map(e=>e.replace(wg,t))}var Eg=(()=>{class t{eventManager;sharedStylesHost;appId;removeStylesOnCompDestroy;doc;ngZone;nonce;tracingService;rendererByCompId=new Map;defaultRenderer;cssVarNamespace;constructor(e,i,a,o,r,s,l=null,h=null,p=null){this.eventManager=e,this.sharedStylesHost=i,this.appId=a,this.removeStylesOnCompDestroy=o,this.doc=r,this.ngZone=s,this.nonce=l,this.tracingService=h,this.cssVarNamespace=p??``,this.defaultRenderer=new Cl(e,r,s,this.tracingService,this.cssVarNamespace)}createRenderer(e,i){if(!e||!i)return this.defaultRenderer;let a=this.getOrCreateRenderer(e,i);return a instanceof bu?a.applyToHost(e):a instanceof Sl&&a.applyStyles(),a}getOrCreateRenderer(e,i){let a=this.rendererByCompId,o=a.get(i.id);if(!o){let r=this.doc,s=this.ngZone,l=this.eventManager,h=this.sharedStylesHost,p=this.removeStylesOnCompDestroy,g=this.tracingService;switch(i.encapsulation){case ei.Emulated:o=new bu(l,h,i,this.appId,p,r,s,g,this.cssVarNamespace);break;case ei.ShadowDom:return new gu(l,e,i,r,s,this.nonce,g,this.cssVarNamespace,h);case ei.ExperimentalIsolatedShadowDom:return new gu(l,e,i,r,s,this.nonce,g,this.cssVarNamespace);default:o=new Sl(l,h,i,p,r,s,g,this.cssVarNamespace)}a.set(i.id,o)}return o}ngOnDestroy(){this.rendererByCompId.clear()}componentReplaced(e){this.rendererByCompId.delete(e)}static ɵfac=function(i){return new(i||t)(Se(Cg),Se(Co),Se($i),Se(_N),Se(me),Se(Z),Se(xa),Se(ii,8),Se(yN,8))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var Cl=class{eventManager;doc;ngZone;tracingService;cssVarNamespace;data=Object.create(null);throwOnSyntheticProps=!0;constructor(n,e,i,a,o=``){this.eventManager=n,this.doc=e,this.ngZone=i,this.tracingService=a,this.cssVarNamespace=o}destroy(){}destroyNode=null;createElement(n,e){return e?this.doc.createElementNS(yg[e]||e,n):this.doc.createElement(n)}createComment(n){return this.doc.createComment(n)}createText(n){return this.doc.createTextNode(n)}appendChild(n,e){(Jx(n)?n.content:n).appendChild(e)}insertBefore(n,e,i){if(n){let a=Jx(n)?n.content:n;if(i!=null&&i.parentNode!==a)throw new q(-5106,!1);a.insertBefore(e,i)}}removeChild(n,e){e.remove()}selectRootElement(n,e){let i=typeof n==`string`?this.doc.querySelector(n):n;if(!i)throw new q(-5104,!1);return e||(i.textContent=``),i}parentNode(n){return n.parentNode}nextSibling(n){return n.nextSibling}setAttribute(n,e,i,a){if(a){e=a+`:`+e;let o=yg[a];o?n.setAttributeNS(o,e,i):n.setAttribute(e,i)}else n.setAttribute(e,i)}removeAttribute(n,e,i){if(i){let a=yg[i];a?n.removeAttributeNS(a,e):n.removeAttribute(`${i}:${e}`)}else n.removeAttribute(e)}addClass(n,e){n.classList.add(e)}removeClass(n,e){n.classList.remove(e)}setStyle(n,e,i,a){let o=e.startsWith(`--`);o&&(e=e.replace(`%NS%`,this.cssVarNamespace)),o||a&(xi.DashCase|xi.Important)?n.style.setProperty(e,i,a&xi.Important?`important`:``):n.style[e]=i}removeStyle(n,e,i){let a=e.startsWith(`--`);a&&(e=e.replace(`%NS%`,this.cssVarNamespace)),a||i&xi.DashCase?n.style.removeProperty(e):n.style[e]=``}setProperty(n,e,i){n!=null&&(n[e]=i)}setValue(n,e){n.nodeValue=e}listen(n,e,i,a){if(typeof n==`string`&&(n=Fn().getGlobalEventTarget(this.doc,n),!n))throw new q(-5102,!1);let o=this.decoratePreventDefault(i);return this.tracingService?.wrapEventListener&&(o=this.tracingService.wrapEventListener(n,e,o)),this.eventManager.addEventListener(n,e,o,a)}decoratePreventDefault(n){return e=>{if(e===`__ngUnwrap__`)return n;n(e)===!1&&e.preventDefault()}}};function Jx(t){return t.tagName===`TEMPLATE`&&t.content!==void 0}var gu=class extends Cl{hostEl;sharedStylesHost;shadowRoot;constructor(n,e,i,a,o,r,s,l,h){super(n,a,o,s,l),this.hostEl=e,this.sharedStylesHost=h,this.shadowRoot=e.attachShadow({mode:`open`}),this.sharedStylesHost&&this.sharedStylesHost.addHost(this.shadowRoot);let p=i.styles;p=tC(i.id,p).map(_=>_.replace(/%NS%/g,l));for(let _ of p){let b=document.createElement(`style`);r&&b.setAttribute(`nonce`,r),b.textContent=_,this.shadowRoot.appendChild(b)}let g=i.getExternalStyles?.();if(g)for(let _ of g){let b=xg(_,a);r&&b.setAttribute(`nonce`,r),this.shadowRoot.appendChild(b)}}nodeOrShadowRoot(n){return n===this.hostEl?this.shadowRoot:n}appendChild(n,e){return super.appendChild(this.nodeOrShadowRoot(n),e)}insertBefore(n,e,i){return super.insertBefore(this.nodeOrShadowRoot(n),e,i)}removeChild(n,e){return super.removeChild(null,e)}parentNode(n){return this.nodeOrShadowRoot(super.parentNode(this.nodeOrShadowRoot(n)))}destroy(){this.sharedStylesHost&&this.sharedStylesHost.removeHost(this.shadowRoot)}};var Sl=class extends Cl{sharedStylesHost;removeStylesOnCompDestroy;styles;styleUrls;constructor(n,e,i,a,o,r,s,l,h){super(n,o,r,s,l),this.sharedStylesHost=e,this.removeStylesOnCompDestroy=a;let p=i.styles,g=h?tC(h,p):p;this.styles=g.map(_=>_.replace(/%NS%/g,l)),this.styleUrls=i.getExternalStyles?.(h)}applyStyles(){this.sharedStylesHost.addStyles(this.styles,this.styleUrls)}destroy(){this.removeStylesOnCompDestroy&&Df.size===0&&this.sharedStylesHost.removeStyles(this.styles,this.styleUrls)}};var bu=class extends Sl{contentAttr;hostAttr;constructor(n,e,i,a,o,r,s,l,h){let p=a+`-`+i.id;super(n,e,i,o,r,s,l,h,p),this.contentAttr=xN(p),this.hostAttr=CN(p)}applyToHost(n){this.applyStyles(),this.setAttribute(n,this.hostAttr,``)}createElement(n,e){let i=super.createElement(n,e);return super.setAttribute(i,this.contentAttr,``),i}};var _u=class t extends hl{supportsDOMEvents=!0;static makeCurrent(){dg(new t)}onAndCancel(n,e,i,a){return n.addEventListener(e,i,a),()=>{n.removeEventListener(e,i,a)}}dispatchEvent(n,e){n.dispatchEvent(e)}remove(n){n.remove()}createElement(n,e){return e=e||this.getDefaultDocument(),e.createElement(n)}createHtmlDocument(){return document.implementation.createHTMLDocument(`fakeTitle`)}getDefaultDocument(){return document}isElementNode(n){return n.nodeType===Node.ELEMENT_NODE}isShadowRoot(n){return n instanceof DocumentFragment}getGlobalEventTarget(n,e){return e===`window`?window:e===`document`?n:e===`body`?n.body:null}getBaseHref(n){let e=SN();return e==null?null:wN(e)}resetBaseElement(){wl=null}getUserAgent(){return window.navigator.userAgent}getCookie(n){return yl(document.cookie,n)}};var wl=null;function SN(){return wl=wl||document.head.querySelector(`base`),wl?wl.getAttribute(`href`):null}function wN(t){return new URL(t,document.baseURI).pathname}var nC=[`alt`,`control`,`meta`,`shift`];var EN={"\b":`Backspace`,"	":`Tab`,"":`Delete`,"\x1B":`Escape`,Del:`Delete`,Esc:`Escape`,Left:`ArrowLeft`,Right:`ArrowRight`,Up:`ArrowUp`,Down:`ArrowDown`,Menu:`ContextMenu`,Scroll:`ScrollLock`,Win:`OS`};var DN={alt:t=>t.altKey,control:t=>t.ctrlKey,meta:t=>t.metaKey,shift:t=>t.shiftKey};var iC=(()=>{class t extends xl{constructor(e){super(e)}supports(e){return t.parseEventName(e)!=null}addEventListener(e,i,a,o){let r=t.parseEventName(i),s=t.eventCallback(r.fullKey,a,this.manager.getZone());return this.manager.getZone().runOutsideAngular(()=>Fn().onAndCancel(e,r.domEventName,s,o))}static parseEventName(e){let i=e.toLowerCase().split(`.`),a=i.shift();if(i.length===0||!(a===`keydown`||a===`keyup`))return null;let o=t._normalizeKey(i.pop()),r=``,s=i.indexOf(`code`);if(s>-1&&(i.splice(s,1),r=`code.`),nC.forEach(h=>{let p=i.indexOf(h);p>-1&&(i.splice(p,1),r+=h+`.`)}),r+=o,i.length!=0||o.length===0)return null;let l={};return l.domEventName=a,l.fullKey=r,l}static matchEventFullKeyCode(e,i){let a=EN[e.key]||e.key,o=``;return i.indexOf(`code.`)>-1&&(a=e.code,o=`code.`),a==null||!a?!1:(a=a.toLowerCase(),a===` `?a=`space`:a===`.`&&(a=`dot`),nC.forEach(r=>{if(r!==a){let s=DN[r];s(e)&&(o+=r+`.`)}}),o+=a,o===i)}static eventCallback(e,i,a){return o=>{t.matchEventFullKeyCode(o,e)&&a.runGuarded(()=>i(o))}}static _normalizeKey(e){return e===`esc`?`escape`:e}static ɵfac=function(i){return new(i||t)(Se(me))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();async function Dg(t,n,e){return wx(r({rootComponent:t},IN(n,e)))}function IN(t,n){return{platformRef:n?.platformRef,appProviders:[...AN,...t?.providers??[]],platformProviders:NN}}function kN(){_u.makeCurrent()}function MN(){return new Tn}function TN(){return gf(document),document}var NN=[{provide:Gi,useValue:vg},{provide:vd,useValue:kN,multi:!0},{provide:me,useFactory:TN}];var AN=[{provide:As,useValue:`root`},{provide:Tn,useFactory:MN},{provide:vu,useClass:fu,multi:!0},{provide:vu,useClass:iC,multi:!0},Eg,{provide:Co,useClass:Sg},{provide:Sg,useExisting:Co},Cg,{provide:Nt,useExisting:Eg},[]];var Ji=class t{headers;normalizedNames=new Map;lazyInit;lazyUpdate=null;constructor(n){n?typeof n==`string`?this.lazyInit=()=>{this.headers=new Map,n.split(`
`).forEach(e=>{let i=e.indexOf(`:`);if(i>0){let a=e.slice(0,i),o=e.slice(i+1).trim();this.addHeaderEntry(a,o)}})}:typeof Headers<`u`&&n instanceof Headers?(this.headers=new Map,n.forEach((e,i)=>{this.addHeaderEntry(i,e)})):this.lazyInit=()=>{this.headers=new Map,Object.entries(n).forEach(([e,i])=>{this.setHeaderEntries(e,i)})}:this.headers=new Map}has(n){return this.init(),this.headers.has(n.toLowerCase())}get(n){this.init();let e=this.headers.get(n.toLowerCase());return e&&e.length>0?e[0]:null}keys(){return this.init(),Array.from(this.normalizedNames.values())}getAll(n){return this.init(),this.headers.get(n.toLowerCase())||null}append(n,e){return this.clone({name:n,value:e,op:`a`})}set(n,e){return this.clone({name:n,value:e,op:`s`})}delete(n,e){return this.clone({name:n,value:e,op:`d`})}maybeSetNormalizedName(n,e){this.normalizedNames.has(e)||this.normalizedNames.set(e,n)}init(){this.lazyInit&&(this.lazyInit instanceof t?this.copyFrom(this.lazyInit):this.lazyInit(),this.lazyInit=null,this.lazyUpdate&&(this.lazyUpdate.forEach(n=>this.applyUpdate(n)),this.lazyUpdate=null))}copyFrom(n){n.init();for(let[e,i]of n.headers.entries())this.headers.set(e,i),this.normalizedNames.set(e,n.normalizedNames.get(e))}clone(n){let e=new t;return e.lazyInit=this.lazyInit&&this.lazyInit instanceof t?this.lazyInit:this,e.lazyUpdate=(this.lazyUpdate||[]).concat([n]),e}applyUpdate(n){let e=n.name.toLowerCase();switch(n.op){case`a`:case`s`:let i=n.value;if(typeof i==`string`&&(i=[i]),i.length===0)return;this.maybeSetNormalizedName(n.name,e);let a=n.op===`a`?(this.headers.get(e)||[]).slice():[];a.push(...i),this.headers.set(e,a);break;case`d`:let o=n.value;if(o===void 0)this.headers.delete(e),this.normalizedNames.delete(e);else{let r=Array.isArray(o)?o:[o],s=this.headers.get(e);if(!s)return;s=s.filter(l=>r.indexOf(l)===-1),s.length===0?(this.headers.delete(e),this.normalizedNames.delete(e)):this.headers.set(e,s)}}}addHeaderEntry(n,e){let i=n.toLowerCase();this.maybeSetNormalizedName(n,i),this.headers.has(i)?this.headers.get(i).push(e):this.headers.set(i,[e])}setHeaderEntries(n,e){let i=(Array.isArray(e)?e:[e]).map(o=>o.toString()),a=n.toLowerCase();this.headers.set(a,i),this.maybeSetNormalizedName(n,a)}forEach(n){this.init(),Array.from(this.normalizedNames.keys()).forEach(e=>n(this.normalizedNames.get(e),this.headers.get(e)))}};var xu=class{map=new Map;set(n,e){return this.map.set(n,e),this}get(n){return this.map.has(n)||this.map.set(n,n.defaultValue()),this.map.get(n)}delete(n){return this.map.delete(n),this}has(n){return this.map.has(n)}keys(){return this.map.keys()}};var Cu=class{encodeKey(n){return aC(n)}encodeValue(n){return aC(n)}decodeKey(n){return decodeURIComponent(n)}decodeValue(n){return decodeURIComponent(n)}};function RN(t,n){let e=new Map;return t.length>0&&t.replace(/^\?/,``).split(`&`).forEach(a=>{let o=a.indexOf(`=`),[r,s]=o==-1?[n.decodeKey(a),``]:[n.decodeKey(a.slice(0,o)),n.decodeValue(a.slice(o+1))],l=e.get(r)||[];l.push(s),e.set(r,l)}),e}var ON=/%(\d[a-f0-9])/gi;var PN={40:`@`,"3A":`:`,24:`$`,"2C":`,`,"3B":`;`,"3D":`=`,"3F":`?`,"2F":`/`};function aC(t){return encodeURIComponent(t).replace(ON,(n,e)=>PN[e]??n)}function yu(t){return`${t}`}var Zi=class t{map;encoder;updates=null;cloneFrom=null;constructor(n={}){if(this.encoder=n.encoder||new Cu,n.fromString){if(n.fromObject)throw new q(2805,!1);this.map=RN(n.fromString,this.encoder)}else n.fromObject?(this.map=new Map,Object.keys(n.fromObject).forEach(e=>{let i=n.fromObject[e],a=Array.isArray(i)?i.map(yu):[yu(i)];this.map.set(e,a)})):this.map=null}has(n){return this.init(),this.map.has(n)}get(n){this.init();let e=this.map.get(n);return e?e[0]:null}getAll(n){return this.init(),this.map.get(n)||null}keys(){return this.init(),Array.from(this.map.keys())}append(n,e){return this.clone({param:n,value:e,op:`a`})}appendAll(n){let e=[];return Object.keys(n).forEach(i=>{let a=n[i];Array.isArray(a)?a.forEach(o=>{e.push({param:i,value:o,op:`a`})}):e.push({param:i,value:a,op:`a`})}),this.clone(e)}set(n,e){return this.clone({param:n,value:e,op:`s`})}delete(n,e){return this.clone({param:n,value:e,op:`d`})}toString(){return this.init(),this.keys().map(n=>{let e=this.encoder.encodeKey(n);return this.map.get(n).map(i=>e+`=`+this.encoder.encodeValue(i)).join(`&`)}).filter(n=>n!==``).join(`&`)}clone(n){let e=new t({encoder:this.encoder});return e.cloneFrom=this.cloneFrom||this,e.updates=(this.updates||[]).concat(n),e}init(){if(this.map===null&&(this.map=new Map),this.cloneFrom!==null){this.cloneFrom.init();for(let[n,e]of this.cloneFrom.map.entries())this.map.set(n,e);this.updates.forEach(n=>{switch(n.op){case`a`:case`s`:let e=n.op===`a`?(this.map.get(n.param)||[]).slice():[];e.push(yu(n.value)),this.map.set(n.param,e);break;case`d`:if(n.value!==void 0){let i=(this.map.get(n.param)||[]).slice(),a=i.indexOf(yu(n.value));a!==-1&&i.splice(a,1),i.length>0?this.map.set(n.param,i):this.map.delete(n.param)}else{this.map.delete(n.param);break}}}),this.cloneFrom=this.updates=null}}};function FN(t){switch(t){case`DELETE`:case`GET`:case`HEAD`:case`OPTIONS`:case`JSONP`:return!1;default:return!0}}function oC(t){return typeof ArrayBuffer<`u`&&t instanceof ArrayBuffer}function rC(t){return typeof Blob<`u`&&t instanceof Blob}function sC(t){return typeof FormData<`u`&&t instanceof FormData}function LN(t){return typeof URLSearchParams<`u`&&t instanceof URLSearchParams}var Ig=`Content-Type`;var lC=`Accept`;var uC=`text/plain`;var mC=`application/json`;var VN=`${mC}, ${uC}, */*`;var Pr=class t{url;body=null;headers;context;reportProgress=!1;reportUploadProgress=!1;reportDownloadProgress=!1;withCredentials=!1;credentials;keepalive=!1;cache;priority;mode;redirect;referrer;integrity;referrerPolicy;responseType=`json`;method;params;urlWithParams;transferCache;timeout;constructor(n,e,i,a){this.url=e,this.method=n.toUpperCase();let o;if(FN(this.method)||a?(this.body=i!==void 0?i:null,o=a):o=i,o){if(this.reportProgress=!!o.reportProgress,this.reportUploadProgress=!!o.reportUploadProgress,this.reportDownloadProgress=!!o.reportDownloadProgress,this.withCredentials=!!o.withCredentials,this.keepalive=!!o.keepalive,o.responseType&&(this.responseType=o.responseType),o.headers&&(this.headers=o.headers),o.context&&(this.context=o.context),o.params&&(this.params=o.params),o.priority&&(this.priority=o.priority),o.cache&&(this.cache=o.cache),o.credentials&&(this.credentials=o.credentials),typeof o.timeout==`number`){if(o.timeout<1||!Number.isInteger(o.timeout))throw new q(2822,``);this.timeout=o.timeout}o.mode&&(this.mode=o.mode),o.redirect&&(this.redirect=o.redirect),o.integrity&&(this.integrity=o.integrity),o.referrer!==void 0&&(this.referrer=o.referrer),o.referrerPolicy&&(this.referrerPolicy=o.referrerPolicy),this.transferCache=o.transferCache}if(this.headers??=new Ji,this.context??=new xu,!this.params)this.params=new Zi,this.urlWithParams=e;else{let r=this.params.toString();if(r.length===0)this.urlWithParams=e;else{let s=e,l=``,h=e.indexOf(`#`);h!==-1&&(l=e.substring(h),s=e.substring(0,h));let p=s.indexOf(`?`),g=p===-1?`?`:p<s.length-1?`&`:``;this.urlWithParams=s+g+r+l}}}serializeBody(){return this.body===null?null:typeof this.body==`string`||oC(this.body)||rC(this.body)||sC(this.body)||LN(this.body)?this.body:this.body instanceof Zi?this.body.toString():typeof this.body==`object`||typeof this.body==`boolean`||Array.isArray(this.body)?JSON.stringify(this.body):this.body.toString()}detectContentTypeHeader(){return this.body===null||sC(this.body)?null:rC(this.body)?this.body.type||null:oC(this.body)?null:typeof this.body==`string`?uC:this.body instanceof Zi?`application/x-www-form-urlencoded;charset=UTF-8`:typeof this.body==`object`||typeof this.body==`number`||typeof this.body==`boolean`?mC:null}clone(n={}){let e=n.method||this.method,i=n.url||this.url,a=n.responseType||this.responseType,o=n.keepalive??this.keepalive,r=n.priority||this.priority,s=n.cache||this.cache,l=n.mode||this.mode,h=n.redirect||this.redirect,p=n.credentials||this.credentials,g=n.referrer??this.referrer,_=n.integrity||this.integrity,b=n.referrerPolicy||this.referrerPolicy,y=n.transferCache??this.transferCache,I=n.timeout??this.timeout,P=n.body!==void 0?n.body:this.body,ie=n.withCredentials??this.withCredentials,L=n.reportProgress??this.reportProgress,x=n.reportUploadProgress??this.reportUploadProgress,A=n.reportDownloadProgress??this.reportDownloadProgress,R=n.headers||this.headers,W=n.params||this.params,ye=n.context??this.context;return n.setHeaders!==void 0&&(R=Object.keys(n.setHeaders).reduce((bt,We)=>bt.set(We,n.setHeaders[We]),R)),n.setParams&&(W=Object.keys(n.setParams).reduce((bt,We)=>bt.set(We,n.setParams[We]),W)),new t(e,i,P,{params:W,headers:R,context:ye,reportProgress:L,reportUploadProgress:x,reportDownloadProgress:A,responseType:a,withCredentials:ie,transferCache:y,keepalive:o,cache:s,priority:r,timeout:I,mode:l,redirect:h,credentials:p,referrer:g,integrity:_,referrerPolicy:b})}};var Io=(function(t){return t[t.Sent=0]=`Sent`,t[t.UploadProgress=1]=`UploadProgress`,t[t.ResponseHeader=2]=`ResponseHeader`,t[t.DownloadProgress=3]=`DownloadProgress`,t[t.Response=4]=`Response`,t[t.User=5]=`User`,t})(Io||{});var Fr=class{headers;status;statusText;url;ok;type;redirected;responseType;constructor(n,e=200,i=`OK`){this.headers=n.headers||new Ji,this.status=n.status!==void 0?n.status:e,this.statusText=n.statusText||i,this.url=n.url||null,this.redirected=n.redirected,this.responseType=n.responseType,this.ok=this.status>=200&&this.status<300}};var Su=class t extends Fr{constructor(n={}){super(n)}type=Io.ResponseHeader;clone(n={}){return new t({headers:n.headers||this.headers,status:n.status!==void 0?n.status:this.status,statusText:n.statusText||this.statusText,url:n.url||this.url||void 0})}};var El=class t extends Fr{body;constructor(n={}){super(n),this.body=n.body!==void 0?n.body:null}type=Io.Response;clone(n={}){return new t({body:n.body!==void 0?n.body:this.body,headers:n.headers||this.headers,status:n.status!==void 0?n.status:this.status,statusText:n.statusText||this.statusText,url:n.url||this.url||void 0,redirected:n.redirected??this.redirected,responseType:n.responseType??this.responseType})}};var Ei=class extends Fr{name=`HttpErrorResponse`;message;error;ok=!1;constructor(n){super(n,0,`Unknown Error`),this.status>=200&&this.status<300?this.message=`Http failure during parsing for ${n.url||`(unknown url)`}`:this.message=`Http failure response for ${n.url||`(unknown url)`}: ${n.status} ${n.statusText}`,this.error=n.error||null}};var BN=200;var jN=/^\)\]\}',?\n/;var pC=new N(``,{factory:()=>null});var wu=(()=>{class t{fetchImpl=f(Mg,{optional:!0})?.fetch??((...e)=>globalThis.fetch(...e));ngZone=f(Z);destroyRef=f(Ae);maxResponseSize=f(pC);handle(e){return new Ce(i=>{let a=new AbortController,o=!1,r={next:l=>{l.type===Io.Response&&(o=!0),i.next(l)},error:l=>{o=!0,i.error(l)},complete:()=>{o=!0,i.complete()}};this.doRequest(e,a.signal,r).then(Tg,l=>r.error(new Ei({error:l})));let s;return e.timeout&&(s=this.ngZone.runOutsideAngular(()=>setTimeout(()=>{a.signal.aborted||a.abort(new DOMException(`signal timed out`,`TimeoutError`))},e.timeout))),()=>{s!==void 0&&clearTimeout(s),!o&&!a.signal.aborted&&a.abort()}})}async doRequest(e,i,a){let o=this.createRequestInit(e),r$1;try{let P=this.ngZone.runOutsideAngular(()=>this.fetchImpl(e.urlWithParams,r({signal:i},o)));zN(P),a.next({type:Io.Sent}),r$1=await P}catch(P){a.error(new Ei({error:P,status:P.status??0,statusText:P.statusText,url:e.urlWithParams,headers:P.headers}));return}let s=new Ji(r$1.headers),l=r$1.statusText,h=r$1.url||e.urlWithParams,p=r$1.status,g=null,_=e.reportProgress||e.reportDownloadProgress;if(_&&a.next(new Su({headers:s,status:p,statusText:l,url:h})),r$1.body){let P=r$1.headers.get(Ig)??``,ie=r$1.headers.get(`content-length`),L=ie!==null?Number(ie):NaN;this.maxResponseSize!==null&&Number.isFinite(L)&&L>this.maxResponseSize&&(await r$1.body.cancel(),cC(this.maxResponseSize));let x=[],A=r$1.body.getReader(),R=0,W,ye,bt=typeof Zone<`u`&&Zone.current,We=!1;if(await this.ngZone.runOutsideAngular(async()=>{for(;;){if(this.destroyRef.destroyed){await A.cancel(),We=!0;break}let{done:Ie,value:F}=await A.read();if(Ie)break;if(x.push(F),R+=F.length,this.maxResponseSize!==null&&R>this.maxResponseSize&&(await A.cancel(),cC(this.maxResponseSize)),_){ye=e.responseType===`text`?(ye??``)+(W??=dC(P)).decode(F,{stream:!0}):void 0;let qe=()=>a.next({type:Io.DownloadProgress,total:Number.isFinite(L)?L:void 0,loaded:R,partialText:ye});bt?bt.run(qe):qe()}}}),We){a.complete();return}let pe=this.concatChunks(x,R);try{g=this.parseBody(e,pe,P,p)}catch(Ie){a.error(new Ei({error:Ie,headers:new Ji(r$1.headers),status:r$1.status,statusText:r$1.statusText,url:r$1.url||e.urlWithParams}));return}}p===0&&(p=g?BN:0);let b=p>=200&&p<300,y=r$1.redirected,I=r$1.type;b?(a.next(new El({body:g,headers:s,status:p,statusText:l,url:h,redirected:y,responseType:I})),a.complete()):a.error(new Ei({error:g,headers:s,status:p,statusText:l,url:h,redirected:y,responseType:I}))}parseBody(e,i,a,o){switch(e.responseType){case`json`:let r=new TextDecoder().decode(i).replace(jN,``);if(r===``)return null;try{return JSON.parse(r)}catch(s){if(o<200||o>=300)return r;throw s}case`text`:return dC(a).decode(i);case`blob`:return new Blob([i],{type:a});case`arraybuffer`:return i.buffer}}createRequestInit(e){if(e.reportUploadProgress)throw new q(2824,!1);let i={},a;if(a=e.credentials,e.withCredentials&&(a=`include`),e.headers.forEach((o,r)=>i[o]=r.join(`,`)),e.headers.has(lC)||(i[lC]=VN),!e.headers.has(Ig)){let o=e.detectContentTypeHeader();o!==null&&(i[Ig]=o)}return{body:e.serializeBody(),method:e.method,headers:i,credentials:a,keepalive:e.keepalive,cache:e.cache,priority:e.priority,mode:e.mode,redirect:e.redirect,referrer:e.referrer,integrity:e.integrity,referrerPolicy:e.referrerPolicy}}concatChunks(e,i){let a=new Uint8Array(i),o=0;for(let r of e)a.set(r,o),o+=r.length;return a}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Mg=class{};function Tg(){}function zN(t){t.then(Tg,Tg)}function cC(t){throw new q(-2825,!1)}var HN=/charset=\s*["']?([^;"'\s]+)["']?/i;function dC(t){let n=t.match(HN);if(n!==null)try{return new TextDecoder(n[1])}catch{}return new TextDecoder}var UN=new N(``,{factory:()=>!0});var $N=`XSRF-TOKEN`;var GN=new N(``,{factory:()=>$N});var WN=`X-XSRF-TOKEN`;var qN=new N(``,{factory:()=>WN});var YN=(()=>{class t{cookieName=f(GN);doc=f(me);lastCookieString=``;lastToken=null;parseCount=0;getToken(){let e=this.doc.cookie||``;return e!==this.lastCookieString&&(this.parseCount++,this.lastToken=yl(e,this.cookieName),this.lastCookieString=e),this.lastToken}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var hC=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(YN),a},providedIn:`root`})}return t})();function fC(t,n){if(!f(UN)||t.method===`GET`||t.method===`HEAD`)return n(t);try{let a=f(Or).href,{origin:o}=new URL(a),{origin:r}=new URL(t.url,o);if(o!==r)return n(t)}catch{return n(t)}let e=f(hC).getToken(),i=f(qN);return e!=null&&!t.headers.has(i)&&(t=t.clone({headers:t.headers.set(i,e)})),n(t)}function KN(t,n){return n(t)}function QN(t,n,e){return(i,a)=>rr(e,()=>n(i,o=>t(o,a)))}var gC=new N(``,{factory:()=>[fC]});var bC=new N(``);var vC=new N(``,{factory:()=>!0});var Ng=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(wu),a},providedIn:`root`})}return t})();var Eu=(()=>{class t{backend;injector;chain=null;pendingTasks=f(Ca);contributeToStability=f(vC);constructor(e,i){this.backend=e,this.injector=i}handle(e){if(this.chain===null){let a=this.injector.get(Du,null,{skipSelf:!0}),o=a!==null&&this.backend===a,r=this.injector.get(bC,[],o?{self:!0}:void 0),s=Array.from(new Set([...this.injector.get(gC),...r]));this.chain=s.reduceRight((l,h)=>QN(l,h,this.injector),KN)}let i=this.chain;if(this.contributeToStability){let a=this.pendingTasks.add();return it(()=>i(e,o=>this.backend.handle(o))).pipe(fp(a))}else return it(()=>i(e,a=>this.backend.handle(a)))}static ɵfac=function(i){return new(i||t)(Se(Ng),Se(Mt))};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();var Du=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(Eu),a},providedIn:`root`})}return t})();function kg(t,n){return r({body:n},t)}var Di=(()=>{class t{handler;constructor(e){this.handler=e}request(e,i,a={}){let o;if(e instanceof Pr)o=e;else{let l;a.headers instanceof Ji?l=a.headers:l=new Ji(a.headers);let h;a.params&&(a.params instanceof Zi?h=a.params:h=new Zi({fromObject:a.params})),o=new Pr(e,i,a.body!==void 0?a.body:null,{headers:l,context:a.context,params:h,reportProgress:a.reportProgress,reportUploadProgress:a.reportUploadProgress,reportDownloadProgress:a.reportDownloadProgress,responseType:a.responseType||`json`,withCredentials:a.withCredentials,transferCache:a.transferCache,keepalive:a.keepalive,priority:a.priority,cache:a.cache,mode:a.mode,redirect:a.redirect,credentials:a.credentials,referrer:a.referrer,referrerPolicy:a.referrerPolicy,integrity:a.integrity,timeout:a.timeout})}let r=Mn(o).pipe(hp(l=>this.handler.handle(l)));if(e instanceof Pr||a.observe===`events`)return r;let s=r.pipe(Qe(l=>l instanceof El));switch(a.observe||`body`){case`body`:switch(o.responseType){case`arraybuffer`:return s.pipe(lt(l=>{if(l.body!==null&&!(l.body instanceof ArrayBuffer))throw new q(2806,!1);return l.body}));case`blob`:return s.pipe(lt(l=>{if(l.body!==null&&!(l.body instanceof Blob))throw new q(2807,!1);return l.body}));case`text`:return s.pipe(lt(l=>{if(l.body!==null&&typeof l.body!=`string`)throw new q(2808,!1);return l.body}));default:return s.pipe(lt(l=>l.body))}case`response`:return s;default:throw new q(2809,!1)}}delete(e,i={}){return this.request(`DELETE`,e,i)}get(e,i={}){return this.request(`GET`,e,i)}head(e,i={}){return this.request(`HEAD`,e,i)}jsonp(e,i){return this.request(`JSONP`,e,{params:new Zi().append(i,`JSONP_CALLBACK`),observe:`body`,responseType:`json`})}options(e,i={}){return this.request(`OPTIONS`,e,i)}patch(e,i,a={}){return this.request(`PATCH`,e,kg(a,i))}post(e,i,a={}){return this.request(`POST`,e,kg(a,i))}put(e,i,a={}){return this.request(`PUT`,e,kg(a,i))}static ɵfac=function(i){return new(i||t)(Se(Du))};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();function Ag(...t){let n=[Di,wu,Eu,{provide:Du,useExisting:Eu},{provide:Ng,useFactory:()=>f(wu)},{provide:gC,useValue:fC,multi:!0}];for(let e of t)n.push(...e.ɵproviders);return ln(n)}var Rg=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(XN),a},providedIn:`root`})}return t})();var XN=(()=>{class t extends Rg{_doc=f(me);sanitize(e,i){if(i==null)return null;switch(e){case xn.NONE:return i;case xn.HTML:return wa(i,`HTML`)?ti(i):Sf(this._doc,String(i)).toString();case xn.STYLE:return wa(i,`Style`)?ti(i):i;case xn.SCRIPT:if(wa(i,`Script`))return ti(i);throw new q(5200,!1);case xn.URL:return wa(i,`URL`)?ti(i):Js(String(i));case xn.RESOURCE_URL:if(wa(i,`ResourceURL`))return ti(i);throw new q(-5201,!1);default:throw new q(5202,!1)}}bypassSecurityTrustHtml(e){return vf(e)}bypassSecurityTrustStyle(e){return _f(e)}bypassSecurityTrustScript(e){return yf(e)}bypassSecurityTrustUrl(e){return xf(e)}bypassSecurityTrustResourceUrl(e){return Cf(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var _C=new N(`HIGHCHARTS_LOADER`);var yC=new N(`HIGHCHARTS_ROOT_MODULES`);var xC=new N(`HIGHCHARTS_OPTIONS`);var ZN=new N(`HIGHCHARTS_CONFIG`);var CC=new N(`HIGHCHARTS_TIMEOUT`);var JN=(()=>{class t{constructor(){this.highcharts=T(null),this.loader=f(_C),this.globalOptions=f(xC,{optional:!0}),this.globalModules=f(yC,{optional:!0}),this.sharedHighchartsPromise=null,this.moduleLoadCache=new WeakMap}async loadModules(e){if(!e)return;let i=this.moduleLoadCache.get(e);if(i)return i;let a=Promise.allSettled(e()).then(o=>{let r=o.filter(s=>s.status===`rejected`);if(r.length){let s=r.map(({reason:l})=>l instanceof Error?l.message:String(l));throw new Error(`Failed to load Highcharts modules: ${s.join(`; `)}`)}});return this.moduleLoadCache.set(e,a),a.catch(()=>{this.moduleLoadCache.get(e)===a&&this.moduleLoadCache.delete(e)}),a}async ensureSharedHighcharts(){if(!this.sharedHighchartsPromise){let e=(async()=>{let i=await this.loader();return await this.loadModules(this.globalModules),this.globalOptions&&i.setOptions(this.globalOptions),i})();this.sharedHighchartsPromise=e,e.catch(()=>{this.sharedHighchartsPromise===e&&(this.sharedHighchartsPromise=null)})}return this.sharedHighchartsPromise}async load(e){let i=await this.ensureSharedHighcharts();return await this.loadModules(e?.modules),this.highcharts.set(i),i}static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}}return t})();var eA=(()=>{class t{delay(e){return new Promise(i=>setTimeout(i,e))}getChartFactory(e,i){if(i===`chart`)return e.chart;let o=e[i];if(!o)throw new Error(`Highcharts constructor "${i}" is not available. Did you load the required module?`);return o}createChart(){vt(e=>{let i=this.loadedHighcharts(),a=this.constructorType();if(!i||this.isDestroyed)return;let o=s=>{if(!(s.renderer.forExport||this.isDestroyed))return this.chartInstance.emit(s)},r=this.getChartFactory(i,a)(this.el.nativeElement,it(()=>this.options()),o);this.chart.set(r),e(()=>{this.chart()===r&&this.chart.set(null),r.destroy()})})}keepChartUpToDate(){let e=null;vt(()=>{let i=this.chart(),a=this.update(),o=this.oneToOne(),r=this.options();if(i){if(i!==e){e=i;return}a&&i.update(r,!0,o)}})}async initializeHighcharts(){await this.pendingTasks.run(async()=>{try{let e=await this.highchartsChartService.load(this.relativeConfig),i=this.relativeConfig?.timeout??this.timeout??0;i>0&&await this.delay(i),this.isDestroyed||this.loadedHighcharts.set(e)}catch(e){console.error(`Highcharts failed to load; chart was not created.`,e)}})}constructor(){this.constructorType=zt(`chart`),this.oneToOne=zt(!1),this.options=zt.required(),this.update=Cx(!0),this.chartInstance=au(),this.destroyRef=f(Ae),this.el=f(Y),this.platformId=f(Gi),this.relativeConfig=f(ZN,{optional:!0}),this.timeout=f(CC,{optional:!0}),this.highchartsChartService=f(JN),this.pendingTasks=f(Ca),this.loadedHighcharts=T(null),this.chart=T(null),this.isDestroyed=!1,!(this.platformId&&Qx(this.platformId))&&(this.destroyRef.onDestroy(()=>{this.isDestroyed=!0}),this.createChart(),this.keepChartUpToDate(),this.initializeHighcharts())}static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵdir=Q({type:t,selectors:[[``,`highchartsChart`,``]],inputs:{constructorType:[1,`constructorType`],oneToOne:[1,`oneToOne`],options:[1,`options`],update:[1,`update`]},outputs:{update:`updateChange`,chartInstance:`chartInstance`}})}}return t})();var SC=(()=>{class t{static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵcmp=H({type:t,selectors:[[`highcharts-chart`]],features:[rl([{directive:eA,inputs:[`constructorType`,`constructorType`,`oneToOne`,`oneToOne`,`options`,`options`,`update`,`update`],outputs:[`chartInstance`,`chartInstance`,`updateChange`,`updateChange`]}])],decls:0,vars:0,template:function(i,a){},encapsulation:2})}}return t})();var tA=()=>[];var nA=()=>import(`./chunk-ClkgY8l1.js`).then(t=>t.default);function iA(t){return ln([{provide:_C,useValue:t??nA}])}function aA(t){return ln([{provide:xC,useValue:t}])}function oA(t){return ln([{provide:yC,useValue:t}])}function wC(t={}){let n=[iA(t.instance),oA(t.modules??tA),{provide:CC,useValue:t.timeout}];return t.options&&n.push(aA(t.options)),ln(n)}var EC={providers:[fh(),ag(),Ag(),wC({instance:()=>import(`./chunk-ClkgY8l1.js`).then(t=>t.default),modules:()=>[Promise.all([import(`./chunk-CyquI88w.js`).then(()=>import(`./chunk-CPXHXDJg.js`)),import(`./chunk-DlS8TT_6.js`),import(`./chunk-DAHFo9wB.js`),import(`./chunk-DDxsqTBe.js`),import(`./chunk-B4SUuPnf.js`)]).then(()=>import(`./chunk-DcGq9my2.js`))]})]};function ko(t){return t.buttons===0||t.detail===0}function Mo(t){let n=t.touches&&t.touches[0]||t.changedTouches&&t.changedTouches[0];return!!n&&n.identifier===-1&&(n.radiusX==null||n.radiusX===1)&&(n.radiusY==null||n.radiusY===1)}var Og;function DC(){if(Og==null){let t=typeof document<`u`?document.head:null;Og=!!(t&&(t.createShadowRoot||t.attachShadow))}return Og}function Pg(t){if(DC()){let n=t.getRootNode?t.getRootNode():null;if(typeof ShadowRoot<`u`&&ShadowRoot&&n instanceof ShadowRoot)return n}return null}function Dl(){let t=typeof document<`u`&&document?document.activeElement:null;for(;t&&t.shadowRoot;){let n=t.shadowRoot.activeElement;if(n===t)break;t=n}return t}function Yt(t){if(t.composedPath)try{return t.composedPath()[0]}catch{}return t.target}var Fg;try{Fg=typeof Intl<`u`&&Intl.v8BreakIterator}catch{Fg=!1}var Re=(()=>{class t{_platformId=f(Gi);isBrowser=this._platformId?Kx(this._platformId):typeof document==`object`&&!!document;EDGE=this.isBrowser&&/(edge)/i.test(navigator.userAgent);TRIDENT=this.isBrowser&&/(msie|trident)/i.test(navigator.userAgent);BLINK=this.isBrowser&&!!(window.chrome||Fg)&&typeof CSS<`u`&&!this.EDGE&&!this.TRIDENT;WEBKIT=this.isBrowser&&/AppleWebKit/i.test(navigator.userAgent)&&!this.BLINK&&!this.EDGE&&!this.TRIDENT;IOS=this.isBrowser&&/iPad|iPhone|iPod/.test(navigator.userAgent)&&!(`MSStream`in window);FIREFOX=this.isBrowser&&/(firefox|minefield)/i.test(navigator.userAgent);ANDROID=this.isBrowser&&/android/i.test(navigator.userAgent)&&!this.TRIDENT;SAFARI=this.isBrowser&&/safari/i.test(navigator.userAgent)&&this.WEBKIT;static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Il;function IC(){if(Il==null&&typeof window<`u`)try{window.addEventListener(`test`,null,Object.defineProperty({},"passive",{get:()=>Il=!0}))}finally{Il=Il||!1}return Il}function Lr(t){return IC()?t:!!t.capture}function ri(t,n=0){return kC(t)?Number(t):arguments.length===2?n:0}function kC(t){return!isNaN(parseFloat(t))&&!isNaN(Number(t))}function mn(t){return t instanceof Y?t.nativeElement:t}var MC=new N(`cdk-input-modality-detector-options`);var TC={ignoreKeys:[18,17,224,91,16]};var NC=650;var Lg={passive:!0,capture:!0};var AC=(()=>{class t{_platform=f(Re);_listenerCleanups;modalityDetected;modalityChanged;get mostRecentModality(){return this._modality.value}_mostRecentTarget=null;_modality=new hi(null);_options;_lastTouchMs=0;_onKeydown=e=>{this._options?.ignoreKeys?.some(i=>i===e.keyCode)||(this._modality.next(`keyboard`),this._mostRecentTarget=Yt(e))};_onMousedown=e=>{Date.now()-this._lastTouchMs<NC||(this._modality.next(ko(e)?`keyboard`:`mouse`),this._mostRecentTarget=Yt(e))};_onTouchstart=e=>{if(Mo(e)){this._modality.next(`keyboard`);return}this._lastTouchMs=Date.now(),this._modality.next(`touch`),this._mostRecentTarget=Yt(e)};constructor(){let e=f(Z),i=f(me),a=f(MC,{optional:!0});if(this._options=r(r({},TC),a),this.modalityDetected=this._modality.pipe(Za(1)),this.modalityChanged=this.modalityDetected.pipe(jc()),this._platform.isBrowser){let o=f(Nt).createRenderer(null,null);this._listenerCleanups=e.runOutsideAngular(()=>[o.listen(i,`keydown`,this._onKeydown,Lg),o.listen(i,`mousedown`,this._onMousedown,Lg),o.listen(i,`touchstart`,this._onTouchstart,Lg)])}}ngOnDestroy(){this._modality.complete(),this._listenerCleanups?.forEach(e=>e())}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var kl=(function(t){return t[t.IMMEDIATE=0]=`IMMEDIATE`,t[t.EVENTUAL=1]=`EVENTUAL`,t})(kl||{});var RC=new N(`cdk-focus-monitor-default-options`);var Iu=Lr({passive:!0,capture:!0});var tn=(()=>{class t{_ngZone=f(Z);_platform=f(Re);_inputModalityDetector=f(AC);_origin=null;_lastFocusOrigin=null;_windowFocused=!1;_windowFocusTimeoutId;_originTimeoutId;_originFromTouchInteraction=!1;_elementInfo=new Map;_monitoredElementCount=0;_rootNodeFocusListenerCount=new Map;_detectionMode;_windowFocusListener=()=>{this._windowFocused=!0,this._windowFocusTimeoutId=setTimeout(()=>this._windowFocused=!1)};_document=f(me);_stopInputModalityDetector=new V;constructor(){let e=f(RC,{optional:!0});this._detectionMode=e?.detectionMode||kl.IMMEDIATE}_rootNodeFocusAndBlurListener=e=>{let i=Yt(e);for(let a=i;a;a=a.parentElement)e.type===`focus`?this._onFocus(e,a):this._onBlur(e,a)};monitor(e,i=!1){let a=mn(e);if(!this._platform.isBrowser||a.nodeType!==1)return Mn();let o=Pg(a)||this._document,r=this._elementInfo.get(a);if(r)return i&&(r.checkChildren=!0),r.subject;let s={checkChildren:i,subject:new V,rootNode:o};return this._elementInfo.set(a,s),this._registerGlobalListeners(s),s.subject}stopMonitoring(e){let i=mn(e),a=this._elementInfo.get(i);a&&(a.subject.complete(),this._setClasses(i),this._elementInfo.delete(i),this._removeGlobalListeners(a))}focusVia(e,i,a){let o=mn(e);o===this._document.activeElement?this._getClosestElementsInfo(o).forEach(([s,l])=>this._originChanged(s,i,l)):(this._setOrigin(i),typeof o.focus==`function`&&o.focus(a))}ngOnDestroy(){this._elementInfo.forEach((e,i)=>this.stopMonitoring(i))}_getWindow(){return this._document.defaultView||window}_getFocusOrigin(e){return this._origin?this._originFromTouchInteraction?this._shouldBeAttributedToTouch(e)?`touch`:`program`:this._origin:this._windowFocused&&this._lastFocusOrigin?this._lastFocusOrigin:e&&this._isLastInteractionFromInputLabel(e)?`mouse`:`program`}_shouldBeAttributedToTouch(e){return this._detectionMode===kl.EVENTUAL||!!e?.contains(this._inputModalityDetector._mostRecentTarget)}_setClasses(e,i){e.classList.toggle(`cdk-focused`,!!i),e.classList.toggle(`cdk-touch-focused`,i===`touch`),e.classList.toggle(`cdk-keyboard-focused`,i===`keyboard`),e.classList.toggle(`cdk-mouse-focused`,i===`mouse`),e.classList.toggle(`cdk-program-focused`,i===`program`)}_setOrigin(e,i=!1){this._ngZone.runOutsideAngular(()=>{if(this._origin=e,this._originFromTouchInteraction=e===`touch`&&i,this._detectionMode===kl.IMMEDIATE){clearTimeout(this._originTimeoutId);let a=this._originFromTouchInteraction?NC:1;this._originTimeoutId=setTimeout(()=>this._origin=null,a)}})}_onFocus(e,i){let a=this._elementInfo.get(i),o=Yt(e);!a||!a.checkChildren&&i!==o||this._originChanged(i,this._getFocusOrigin(o),a)}_onBlur(e,i){let a=this._elementInfo.get(i);!a||a.checkChildren&&e.relatedTarget instanceof Node&&i.contains(e.relatedTarget)||(this._setClasses(i),this._emitOrigin(a,null))}_emitOrigin(e,i){e.subject.observers.length&&this._ngZone.run(()=>e.subject.next(i))}_registerGlobalListeners(e){if(!this._platform.isBrowser)return;let i=e.rootNode,a=this._rootNodeFocusListenerCount.get(i)||0;a||this._ngZone.runOutsideAngular(()=>{i.addEventListener(`focus`,this._rootNodeFocusAndBlurListener,Iu),i.addEventListener(`blur`,this._rootNodeFocusAndBlurListener,Iu)}),this._rootNodeFocusListenerCount.set(i,a+1),++this._monitoredElementCount===1&&(this._ngZone.runOutsideAngular(()=>{this._getWindow().addEventListener(`focus`,this._windowFocusListener)}),this._inputModalityDetector.modalityDetected.pipe(Ne(this._stopInputModalityDetector)).subscribe(o=>{this._setOrigin(o,!0)}))}_removeGlobalListeners(e){let i=e.rootNode;if(this._rootNodeFocusListenerCount.has(i)){let a=this._rootNodeFocusListenerCount.get(i);a>1?this._rootNodeFocusListenerCount.set(i,a-1):(i.removeEventListener(`focus`,this._rootNodeFocusAndBlurListener,Iu),i.removeEventListener(`blur`,this._rootNodeFocusAndBlurListener,Iu),this._rootNodeFocusListenerCount.delete(i))}--this._monitoredElementCount||(this._getWindow().removeEventListener(`focus`,this._windowFocusListener),this._stopInputModalityDetector.next(),clearTimeout(this._windowFocusTimeoutId),clearTimeout(this._originTimeoutId))}_originChanged(e,i,a){this._setClasses(e,i),this._emitOrigin(a,i),this._lastFocusOrigin=i}_getClosestElementsInfo(e){let i=[];return this._elementInfo.forEach((a,o)=>{(o===e||a.checkChildren&&o.contains(e))&&i.push([o,a])}),i}_isLastInteractionFromInputLabel(e){let{_mostRecentTarget:i,mostRecentModality:a}=this._inputModalityDetector;if(a!==`mouse`||!i||i===e||e.nodeName!==`INPUT`&&e.nodeName!==`TEXTAREA`||e.disabled)return!1;let o=e.labels;if(o){for(let r=0;r<o.length;r++)if(o[r].contains(i))return!0}return!1}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Vg=(()=>{class t{_elementRef=f(Y);_focusMonitor=f(tn);_monitorSubscription;_focusOrigin=null;cdkFocusChange=new te;get focusOrigin(){return this._focusOrigin}ngAfterViewInit(){let e=this._elementRef.nativeElement;this._monitorSubscription=this._focusMonitor.monitor(e,e.nodeType===1&&e.hasAttribute(`cdkMonitorSubtreeFocus`)).subscribe(i=>{this._focusOrigin=i,this.cdkFocusChange.emit(i)})}ngOnDestroy(){this._focusMonitor.stopMonitoring(this._elementRef),this._monitorSubscription?.unsubscribe()}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdkMonitorElementFocus`,``],[``,`cdkMonitorSubtreeFocus`,``]],outputs:{cdkFocusChange:`cdkFocusChange`},exportAs:[`cdkMonitorFocus`]})}return t})();var ku=new WeakMap;var Je=(()=>{class t{_appRef;_injector=f(se);_environmentInjector=f(Mt);load(e){let i=this._appRef=this._appRef||this._injector.get(On),a=ku.get(i);a||(a={loaders:new Set,refs:[]},ku.set(i,a),i.onDestroy(()=>{ku.get(i)?.refs.forEach(o=>o.destroy()),ku.delete(i)})),a.loaders.has(e)||(a.loaders.add(e),a.refs.push(ou(e,{environmentInjector:this._environmentInjector})))}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Ii=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`ng-component`]],exportAs:[`cdkVisuallyHidden`],decls:0,vars:0,template:function(i,a){},styles:[`.cdk-visually-hidden {
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
`],encapsulation:2})}return t})();var Mu;function rA(){if(Mu===void 0&&(Mu=null,typeof window<`u`)){let t=window;if(t.trustedTypes!==void 0)try{Mu=t.trustedTypes.createPolicy(`angular#components`,{createHTML:n=>n})}catch(n){console.error(n)}}return Mu}function sA(t){return rA()?.createHTML(t)||t}function OC(t,n,e){t.innerHTML=sA(e.sanitize(xn.HTML,n)||``)}function Vr(t){return Array.isArray(t)?t:[t]}var PC=new Set;var To;var Br=(()=>{class t{_platform=f(Re);_nonce=f(xa,{optional:!0});_matchMedia;constructor(){this._matchMedia=this._platform.isBrowser&&window.matchMedia?window.matchMedia.bind(window):cA}matchMedia(e){return(this._platform.WEBKIT||this._platform.BLINK)&&lA(e,this._nonce),this._matchMedia(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function lA(t,n){if(!PC.has(t))try{To||(To=document.createElement(`style`),n&&To.setAttribute(`nonce`,n),To.setAttribute(`type`,`text/css`),document.head.appendChild(To)),To.sheet&&(To.sheet.insertRule(`@media ${t.replace(/[{}]/g,``)} {body{ }}`,0),PC.add(t))}catch(e){console.error(e)}}function cA(t){return{matches:t===`all`||t===``,media:t,addListener:()=>{},removeListener:()=>{}}}var Bg=(()=>{class t{_mediaMatcher=f(Br);_zone=f(Z);_queries=new Map;_destroySubject=new V;ngOnDestroy(){this._destroySubject.next(),this._destroySubject.complete()}isMatched(e){return FC(Vr(e)).some(a=>this._registerQuery(a).mql.matches)}observe(e){let o=mp(FC(Vr(e)).map(r=>this._registerQuery(r).observable));return o=tr(o.pipe(vn(1)),o.pipe(Za(1),Pi(0))),o.pipe(lt(r=>{let s={matches:!1,breakpoints:{}};return r.forEach(({matches:l,query:h})=>{s.matches=s.matches||l,s.breakpoints[h]=l}),s}))}_registerQuery(e){if(this._queries.has(e))return this._queries.get(e);let i=this._mediaMatcher.matchMedia(e),o={observable:new Ce(r=>{let s=l=>this._zone.run(()=>r.next(l));return i.addListener(s),()=>{i.removeListener(s)}}).pipe(mt(i),lt(({matches:r})=>({query:e,matches:r})),Ne(this._destroySubject)),mql:i};return this._queries.set(e,o),o}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function FC(t){return t.map(n=>n.split(`,`)).reduce((n,e)=>n.concat(e)).map(n=>n.trim())}function dA(t){if(t.type===`characterData`&&t.target instanceof Comment)return!0;if(t.type===`childList`){for(let n=0;n<t.addedNodes.length;n++)if(!(t.addedNodes[n]instanceof Comment))return!1;for(let n=0;n<t.removedNodes.length;n++)if(!(t.removedNodes[n]instanceof Comment))return!1;return!0}return!1}var LC=(()=>{class t{create(e){return typeof MutationObserver>`u`?null:new MutationObserver(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var VC=(()=>{class t{_mutationObserverFactory=f(LC);_observedElements=new Map;_ngZone=f(Z);ngOnDestroy(){this._observedElements.forEach((e,i)=>this._cleanupObserver(i))}observe(e){let i=mn(e);return new Ce(a=>{let r=this._observeElement(i).pipe(lt(s=>s.filter(l=>!dA(l))),Qe(s=>!!s.length)).subscribe(s=>{this._ngZone.run(()=>{a.next(s)})});return()=>{r.unsubscribe(),this._unobserveElement(i)}})}_observeElement(e){return this._ngZone.runOutsideAngular(()=>{if(this._observedElements.has(e))this._observedElements.get(e).count++;else{let i=new V,a=this._mutationObserverFactory.create(o=>i.next(o));a&&a.observe(e,{characterData:!0,childList:!0,subtree:!0}),this._observedElements.set(e,{observer:a,stream:i,count:1})}return this._observedElements.get(e).stream})}_unobserveElement(e){this._observedElements.has(e)&&(this._observedElements.get(e).count--,this._observedElements.get(e).count||this._cleanupObserver(e))}_cleanupObserver(e){if(this._observedElements.has(e)){let{observer:i,stream:a}=this._observedElements.get(e);i&&i.disconnect(),a.complete(),this._observedElements.delete(e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var BC=(()=>{class t{_contentObserver=f(VC);_elementRef=f(Y);event=new te;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._disabled?this._unsubscribe():this._subscribe()}_disabled=!1;get debounce(){return this._debounce}set debounce(e){this._debounce=ri(e),this._subscribe()}_debounce;_currentSubscription=null;ngAfterContentInit(){!this._currentSubscription&&!this.disabled&&this._subscribe()}ngOnDestroy(){this._unsubscribe()}_subscribe(){this._unsubscribe();let e=this._contentObserver.observe(this._elementRef);this._currentSubscription=(this.debounce?e.pipe(Pi(this.debounce)):e).subscribe(this.event)}_unsubscribe(){this._currentSubscription?.unsubscribe()}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdkObserveContent`,``]],inputs:{disabled:[2,`cdkObserveContentDisabled`,`disabled`,z],debounce:`debounce`},outputs:{event:`cdkObserveContent`},exportAs:[`cdkObserveContent`]})}return t})();var Tu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({providers:[LC]})}return t})();var jr=(()=>{class t{_platform=f(Re);isDisabled(e){return e.hasAttribute(`disabled`)}isVisible(e){return mA(e)&&getComputedStyle(e).visibility===`visible`}isTabbable(e){if(!this._platform.isBrowser)return!1;let i=uA(yA(e));if(i&&(jC(i)===-1||!this.isVisible(i)))return!1;let a=e.nodeName.toLowerCase(),o=jC(e);return e.hasAttribute(`contenteditable`)?o!==-1:a===`iframe`||a===`object`||this._platform.WEBKIT&&this._platform.IOS&&!vA(e)?!1:a===`audio`?e.hasAttribute(`controls`)?o!==-1:!1:a===`video`?o===-1?!1:o!==null?!0:this._platform.FIREFOX||e.hasAttribute(`controls`):e.tabIndex>=0}isFocusable(e,i){return _A(e)&&!this.isDisabled(e)&&(i?.ignoreVisibility||this.isVisible(e))}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function uA(t){try{return t.frameElement}catch{return null}}function mA(t){return!!(t.offsetWidth||t.offsetHeight||typeof t.getClientRects==`function`&&t.getClientRects().length)}function pA(t){let n=t.nodeName.toLowerCase();return n===`input`||n===`select`||n===`button`||n===`textarea`}function hA(t){return gA(t)&&t.type==`hidden`}function fA(t){return bA(t)&&t.hasAttribute(`href`)}function gA(t){return t.nodeName.toLowerCase()==`input`}function bA(t){return t.nodeName.toLowerCase()==`a`}function UC(t){if(!t.hasAttribute(`tabindex`)||t.tabIndex===void 0)return!1;let n=t.getAttribute(`tabindex`);return!!(n&&!isNaN(parseInt(n,10)))}function jC(t){if(!UC(t))return null;let n=parseInt(t.getAttribute(`tabindex`)||``,10);return isNaN(n)?-1:n}function vA(t){let n=t.nodeName.toLowerCase(),e=n===`input`&&t.type;return e===`text`||e===`password`||n===`select`||n===`textarea`}function _A(t){return hA(t)?!1:pA(t)||fA(t)||t.hasAttribute(`contenteditable`)||UC(t)}function yA(t){return t.ownerDocument&&t.ownerDocument.defaultView||window}var Nu=class{_element;_checker;_ngZone;_document;_injector;_startAnchor=null;_endAnchor=null;_hasAttached=!1;startAnchorListener=()=>{!this.focusLastTabbableElement()&&this._checker.isFocusable(this._element)&&this._element.focus()};endAnchorListener=()=>{!this.focusFirstTabbableElement()&&this._checker.isFocusable(this._element)&&this._element.focus()};get enabled(){return this._enabled}set enabled(n){this._enabled=n,this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_enabled=!0;constructor(n,e,i,a,o=!1,r){this._element=n,this._checker=e,this._ngZone=i,this._document=a,this._injector=r,o||this.attachAnchors()}destroy(){let n=this._startAnchor,e=this._endAnchor;n&&(n.removeEventListener(`focus`,this.startAnchorListener),n.remove()),e&&(e.removeEventListener(`focus`,this.endAnchorListener),e.remove()),this._startAnchor=this._endAnchor=null,this._hasAttached=!1}attachAnchors(){return this._hasAttached?!0:(this._ngZone.runOutsideAngular(()=>{this._startAnchor||(this._startAnchor=this._createAnchor(),this._startAnchor.addEventListener(`focus`,this.startAnchorListener)),this._endAnchor||(this._endAnchor=this._createAnchor(),this._endAnchor.addEventListener(`focus`,this.endAnchorListener))}),this._element.parentNode&&(this._element.parentNode.insertBefore(this._startAnchor,this._element),this._element.parentNode.insertBefore(this._endAnchor,this._element.nextSibling),this._hasAttached=!0),this._hasAttached)}focusInitialElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusInitialElement(n)))})}focusFirstTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusFirstTabbableElement(n)))})}focusLastTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusLastTabbableElement(n)))})}_getRegionBoundary(n){let e=this._element.querySelectorAll(`[cdk-focus-region-${n}], [cdkFocusRegion${n}], [cdk-focus-${n}]`);return n==`start`?e.length?e[0]:this._getFirstTabbableElement(this._element):e.length?e[e.length-1]:this._getLastTabbableElement(this._element)}focusInitialElement(n){let e=this._element.querySelector(`[cdk-focus-initial], [cdkFocusInitial]`);if(e){if(!this._checker.isFocusable(e)){let i=this._getFirstTabbableElement(e);return i?.focus(n),!!i}return e.focus(n),!0}return this.focusFirstTabbableElement(n)}focusFirstTabbableElement(n){let e=this._getRegionBoundary(`start`);return e&&e.focus(n),!!e}focusLastTabbableElement(n){let e=this._getRegionBoundary(`end`);return e&&e.focus(n),!!e}hasAttached(){return this._hasAttached}_getFirstTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let i=0;i<e.length;i++){let a=e[i].nodeType===this._document.ELEMENT_NODE?this._getFirstTabbableElement(e[i]):null;if(a)return a}return null}_getLastTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let i=e.length-1;i>=0;i--){let a=e[i].nodeType===this._document.ELEMENT_NODE?this._getLastTabbableElement(e[i]):null;if(a)return a}return null}_createAnchor(){let n=this._document.createElement(`div`);return this._toggleAnchorTabIndex(this._enabled,n),n.classList.add(`cdk-visually-hidden`),n.classList.add(`cdk-focus-trap-anchor`),n.setAttribute(`aria-hidden`,`true`),n}_toggleAnchorTabIndex(n,e){n?e.setAttribute(`tabindex`,`0`):e.removeAttribute(`tabindex`)}toggleAnchors(n){this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_executeOnStable(n){Rt(n,{injector:this._injector})}};var zg=(()=>{class t{_checker=f(jr);_ngZone=f(Z);_document=f(me);_injector=f(se);constructor(){f(Je).load(Ii)}create(e,i=!1){return new Nu(e,this._checker,this._ngZone,this._document,i,this._injector)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var $C=new N(`liveAnnouncerElement`,{providedIn:`root`,factory:()=>null});var GC=new N(`LIVE_ANNOUNCER_DEFAULT_OPTIONS`);var xA=0;var Hg=(()=>{class t{_ngZone=f(Z);_defaultOptions=f(GC,{optional:!0});_liveElement;_document=f(me);_sanitizer=f(Rg);_previousTimeout;_currentPromise;_currentResolve;constructor(){let e=f($C,{optional:!0});this._liveElement=e||this._createLiveElement()}announce(e,...i){let a=this._defaultOptions,o,r;return i.length===1&&typeof i[0]==`number`?r=i[0]:[o,r]=i,this.clear(),clearTimeout(this._previousTimeout),o||(o=a&&a.politeness?a.politeness:`polite`),r==null&&a&&(r=a.duration),this._liveElement.setAttribute(`aria-live`,o),this._liveElement.id&&this._exposeAnnouncerToModals(this._liveElement.id),this._ngZone.runOutsideAngular(()=>(this._currentPromise||(this._currentPromise=new Promise(s=>this._currentResolve=s)),clearTimeout(this._previousTimeout),this._previousTimeout=setTimeout(()=>{!e||typeof e==`string`?this._liveElement.textContent=e:OC(this._liveElement,e,this._sanitizer),typeof r==`number`&&(this._previousTimeout=setTimeout(()=>this.clear(),r)),this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0},100),this._currentPromise))}clear(){this._liveElement&&(this._liveElement.textContent=``)}ngOnDestroy(){clearTimeout(this._previousTimeout),this._liveElement?.remove(),this._liveElement=null,this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0}_createLiveElement(){let e=`cdk-live-announcer-element`,i=this._document.getElementsByClassName(e),a=this._document.createElement(`div`);for(let o=0;o<i.length;o++)i[o].remove();return a.classList.add(e),a.classList.add(`cdk-visually-hidden`),a.setAttribute(`aria-atomic`,`true`),a.setAttribute(`aria-live`,`polite`),a.id=`cdk-live-announcer-${xA++}`,this._document.body.appendChild(a),a}_exposeAnnouncerToModals(e){let i=this._document.querySelectorAll(`body > .cdk-overlay-container [aria-modal="true"]`);for(let a=0;a<i.length;a++){let o=i[a],r=o.getAttribute(`aria-owns`);r?r.indexOf(e)===-1&&o.setAttribute(`aria-owns`,r+` `+e):o.setAttribute(`aria-owns`,e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Ia=(function(t){return t[t.NONE=0]=`NONE`,t[t.BLACK_ON_WHITE=1]=`BLACK_ON_WHITE`,t[t.WHITE_ON_BLACK=2]=`WHITE_ON_BLACK`,t})(Ia||{});var zC=`cdk-high-contrast-black-on-white`;var HC=`cdk-high-contrast-white-on-black`;var jg=`cdk-high-contrast-active`;var WC=(()=>{class t{_platform=f(Re);_hasCheckedHighContrastMode=!1;_document=f(me);_breakpointSubscription;constructor(){this._breakpointSubscription=f(Bg).observe(`(forced-colors: active)`).subscribe(()=>{this._hasCheckedHighContrastMode&&(this._hasCheckedHighContrastMode=!1,this._applyBodyHighContrastModeCssClasses())})}getHighContrastMode(){if(!this._platform.isBrowser)return Ia.NONE;let e=this._document.createElement(`div`);e.style.backgroundColor=`rgb(1,2,3)`,e.style.position=`absolute`,this._document.body.appendChild(e);let i=this._document.defaultView||window,a=i&&i.getComputedStyle?i.getComputedStyle(e):null,o=(a&&a.backgroundColor||``).replace(/ /g,``);switch(e.remove(),o){case`rgb(0,0,0)`:case`rgb(45,50,54)`:case`rgb(32,32,32)`:return Ia.WHITE_ON_BLACK;case`rgb(255,255,255)`:case`rgb(255,250,239)`:return Ia.BLACK_ON_WHITE}return Ia.NONE}ngOnDestroy(){this._breakpointSubscription.unsubscribe()}_applyBodyHighContrastModeCssClasses(){if(!this._hasCheckedHighContrastMode&&this._platform.isBrowser&&this._document.body){let e=this._document.body.classList;e.remove(jg,zC,HC),this._hasCheckedHighContrastMode=!0;let i=this.getHighContrastMode();i===Ia.BLACK_ON_WHITE?e.add(jg,zC):i===Ia.WHITE_ON_BLACK&&e.add(jg,HC)}}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var No=(()=>{class t{constructor(){f(WC)._applyBodyHighContrastModeCssClasses()}static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[Tu]})}return t})();var CA=200;var Au=class{_letterKeyStream=new V;_items=[];_selectedItemIndex=-1;_pressedLetters=[];_skipPredicateFn;_selectedItem=new V;selectedItem=this._selectedItem;constructor(n,e){let i=typeof e?.debounceInterval==`number`?e.debounceInterval:CA;e?.skipPredicate&&(this._skipPredicateFn=e.skipPredicate),this.setItems(n),this._setupKeyHandler(i)}destroy(){this._pressedLetters=[],this._letterKeyStream.complete(),this._selectedItem.complete()}setCurrentSelectedItemIndex(n){this._selectedItemIndex=n}setItems(n){this._items=n}handleKey(n){let e=n.keyCode;n.key&&n.key.length===1?this._letterKeyStream.next(n.key.toLocaleUpperCase()):(e>=65&&e<=90||e>=48&&e<=57)&&this._letterKeyStream.next(String.fromCharCode(e))}isTyping(){return this._pressedLetters.length>0}reset(){this._pressedLetters=[]}_setupKeyHandler(n){this._letterKeyStream.pipe(vp(e=>this._pressedLetters.push(e)),Pi(n),Qe(()=>this._pressedLetters.length>0),lt(()=>this._pressedLetters.join(``).toLocaleUpperCase())).subscribe(e=>{for(let i=1;i<this._items.length+1;i++){let a=(this._selectedItemIndex+i)%this._items.length,o=this._items[a];if(!this._skipPredicateFn?.(o)&&o.getLabel?.().toLocaleUpperCase().trim().indexOf(e)===0){this._selectedItem.next(o);break}}this._pressedLetters=[]})}};function Ct(t,...n){return n.length?n.some(e=>t[e]):t.altKey||t.shiftKey||t.ctrlKey||t.metaKey}var zr=class{_items;_activeItemIndex=T(-1);_activeItem=T(null);_wrap=!1;_typeaheadSubscription=De.EMPTY;_itemChangesSubscription;_vertical=!0;_horizontal=null;_allowedModifierKeys=[];_homeAndEnd=!1;_pageUpAndDown={enabled:!1,delta:10};_effectRef;_typeahead;_skipPredicateFn=n=>n.disabled;constructor(n,e){this._items=n,n instanceof wn?this._itemChangesSubscription=n.changes.subscribe(i=>this._itemsChanged(i.toArray())):cn(n)&&(this._effectRef=vt(()=>this._itemsChanged(n()),{injector:e}))}tabOut=new V;change=new V;skipPredicate(n){return this._skipPredicateFn=n,this}withWrap(n=!0){return this._wrap=n,this}withVerticalOrientation(n=!0){return this._vertical=n,this}withHorizontalOrientation(n){return this._horizontal=n,this}withAllowedModifierKeys(n){return this._allowedModifierKeys=n,this}withTypeAhead(n=200){this._typeaheadSubscription.unsubscribe();let e=this._getItemsArray();return this._typeahead=new Au(e,{debounceInterval:typeof n==`number`?n:void 0,skipPredicate:i=>this._skipPredicateFn(i)}),this._typeaheadSubscription=this._typeahead.selectedItem.subscribe(i=>{this.setActiveItem(i)}),this}cancelTypeahead(){return this._typeahead?.reset(),this}withHomeAndEnd(n=!0){return this._homeAndEnd=n,this}withPageUpDown(n=!0,e=10){return this._pageUpAndDown={enabled:n,delta:e},this}setActiveItem(n){let e=this._activeItem();this.updateActiveItem(n),this._activeItem()!==e&&this.change.next(this._activeItemIndex())}onKeydown(n){let e=n.keyCode,a=[`altKey`,`ctrlKey`,`metaKey`,`shiftKey`].every(o=>!n[o]||this._allowedModifierKeys.indexOf(o)>-1);switch(e){case 9:this.tabOut.next();return;case 40:if(this._vertical&&a){this.setNextItemActive();break}else return;case 38:if(this._vertical&&a){this.setPreviousItemActive();break}else return;case 39:if(this._horizontal&&a){this._horizontal===`rtl`?this.setPreviousItemActive():this.setNextItemActive();break}else return;case 37:if(this._horizontal&&a){this._horizontal===`rtl`?this.setNextItemActive():this.setPreviousItemActive();break}else return;case 36:if(this._homeAndEnd&&a){this.setFirstItemActive();break}else return;case 35:if(this._homeAndEnd&&a){this.setLastItemActive();break}else return;case 33:if(this._pageUpAndDown.enabled&&a){let o=this._activeItemIndex()-this._pageUpAndDown.delta;this._setActiveItemByIndex(o>0?o:0,1);break}else return;case 34:if(this._pageUpAndDown.enabled&&a){let o=this._activeItemIndex()+this._pageUpAndDown.delta,r=this._getItemsArray().length;this._setActiveItemByIndex(o<r?o:r-1,-1);break}else return;default:(a||Ct(n,`shiftKey`))&&this._typeahead?.handleKey(n);return}this._typeahead?.reset(),n.preventDefault()}get activeItemIndex(){return this._activeItemIndex()}get activeItem(){return this._activeItem()}isTyping(){return!!this._typeahead&&this._typeahead.isTyping()}setFirstItemActive(){this._setActiveItemByIndex(0,1)}setLastItemActive(){this._setActiveItemByIndex(this._getItemsArray().length-1,-1)}setNextItemActive(){this._activeItemIndex()<0?this.setFirstItemActive():this._setActiveItemByDelta(1)}setPreviousItemActive(){this._activeItemIndex()<0&&this._wrap?this.setLastItemActive():this._setActiveItemByDelta(-1)}updateActiveItem(n){let e=this._getItemsArray(),i=typeof n==`number`?n:e.indexOf(n),a=e[i];this._activeItem.set(a??null),this._activeItemIndex.set(i),this._typeahead?.setCurrentSelectedItemIndex(i)}destroy(){this._typeaheadSubscription.unsubscribe(),this._itemChangesSubscription?.unsubscribe(),this._effectRef?.destroy(),this._typeahead?.destroy(),this.tabOut.complete(),this.change.complete()}_setActiveItemByDelta(n){this._wrap?this._setActiveInWrapMode(n):this._setActiveInDefaultMode(n)}_setActiveInWrapMode(n){let e=this._getItemsArray();for(let i=1;i<=e.length;i++){let a=(this._activeItemIndex()+n*i+e.length)%e.length,o=e[a];if(!this._skipPredicateFn(o)){this.setActiveItem(a);return}}}_setActiveInDefaultMode(n){this._setActiveItemByIndex(this._activeItemIndex()+n,n)}_setActiveItemByIndex(n,e){let i=this._getItemsArray();if(i[n]){for(;this._skipPredicateFn(i[n]);)if(n+=e,!i[n])return;this.setActiveItem(n)}}_getItemsArray(){return cn(this._items)?this._items():this._items instanceof wn?this._items.toArray():this._items}_itemsChanged(n){this._typeahead?.setItems(n);let e=this._activeItem();if(e){let i=n.indexOf(e);i>-1&&i!==this._activeItemIndex()&&(this._activeItemIndex.set(i),this._typeahead?.setCurrentSelectedItemIndex(i))}}};var Ml=class extends zr{setActiveItem(n){this.activeItem&&this.activeItem.setInactiveStyles(),super.setActiveItem(n),this.activeItem&&this.activeItem.setActiveStyles()}};var ea=class extends zr{_origin=`program`;setFocusOrigin(n){return this._origin=n,this}setActiveItem(n){super.setActiveItem(n),this.activeItem&&this.activeItem.focus(this._origin)}};var YC=new Map;var je=class t{_appId=f($i);static _infix=`a${Math.floor(Math.random()*1e5).toString()}`;getId(n,e=!1){this._appId!==`ng`&&(n+=this._appId);let i=YC.get(n);return i===void 0?i=0:i++,YC.set(n,i),`${n}${e?t._infix+`-`:``}${i}`}static ɵfac=function(e){return new(e||t)};static ɵprov=le({token:t,factory:t.ɵfac})};var QC=` `;function SA(t,n,e){let i=Ou(t,n);e=e.trim(),!i.some(a=>a.trim()===e)&&(i.push(e),t.setAttribute(n,i.join(QC)))}function wA(t,n,e){let i=Ou(t,n);e=e.trim();let a=i.filter(o=>o!==e);a.length?t.setAttribute(n,a.join(QC)):t.removeAttribute(n)}function Ou(t,n){return t.getAttribute(n)?.match(/\S+/g)??[]}var XC=`cdk-describedby-message`;var Ru=`cdk-describedby-host`;var $g=0;var Pu=(()=>{class t{_platform=f(Re);_document=f(me);_messageRegistry=new Map;_messagesContainer=null;_id=`${$g++}`;constructor(){f(Je).load(Ii),this._id=f($i)+`-`+$g++}describe(e,i,a){if(!this._canBeDescribed(e,i))return;let o=Ug(i,a);typeof i!=`string`?(KC(i,this._id),this._messageRegistry.set(o,{messageElement:i,referenceCount:0})):this._messageRegistry.has(o)||this._createMessageElement(i,a),this._isElementDescribedByMessage(e,o)||this._addMessageReference(e,o)}removeDescription(e,i,a){if(!i||!this._isElementNode(e))return;let o=Ug(i,a);if(this._isElementDescribedByMessage(e,o)&&this._removeMessageReference(e,o),typeof i==`string`){let r=this._messageRegistry.get(o);r&&r.referenceCount===0&&this._deleteMessageElement(o)}this._messagesContainer?.childNodes.length===0&&(this._messagesContainer.remove(),this._messagesContainer=null)}ngOnDestroy(){let e=this._document.querySelectorAll(`[${Ru}="${this._id}"]`);for(let i=0;i<e.length;i++)this._removeCdkDescribedByReferenceIds(e[i]),e[i].removeAttribute(Ru);this._messagesContainer?.remove(),this._messagesContainer=null,this._messageRegistry.clear()}_createMessageElement(e,i){let a=this._document.createElement(`div`);KC(a,this._id),a.textContent=e,i&&a.setAttribute(`role`,i),this._createMessagesContainer(),this._messagesContainer.appendChild(a),this._messageRegistry.set(Ug(e,i),{messageElement:a,referenceCount:0})}_deleteMessageElement(e){this._messageRegistry.get(e)?.messageElement?.remove(),this._messageRegistry.delete(e)}_createMessagesContainer(){if(this._messagesContainer)return;let e=`cdk-describedby-message-container`,i=this._document.querySelectorAll(`.${e}[platform="server"]`);for(let o=0;o<i.length;o++)i[o].remove();let a=this._document.createElement(`div`);a.style.visibility=`hidden`,a.classList.add(e),a.classList.add(`cdk-visually-hidden`),this._platform.isBrowser||a.setAttribute(`platform`,`server`),this._document.body.appendChild(a),this._messagesContainer=a}_removeCdkDescribedByReferenceIds(e){let i=Ou(e,`aria-describedby`).filter(a=>a.indexOf(XC)!=0);e.setAttribute(`aria-describedby`,i.join(` `))}_addMessageReference(e,i){let a=this._messageRegistry.get(i);SA(e,`aria-describedby`,a.messageElement.id),e.setAttribute(Ru,this._id),a.referenceCount++}_removeMessageReference(e,i){let a=this._messageRegistry.get(i);a.referenceCount--,wA(e,`aria-describedby`,a.messageElement.id),e.removeAttribute(Ru)}_isElementDescribedByMessage(e,i){let a=Ou(e,`aria-describedby`),o=this._messageRegistry.get(i),r=o&&o.messageElement.id;return!!r&&a.indexOf(r)!=-1}_canBeDescribed(e,i){if(!this._isElementNode(e))return!1;if(i&&typeof i==`object`)return!0;let a=i==null?``:`${i}`.trim(),o=e.getAttribute(`aria-label`);return a?!o||o.trim()!==a:!1}_isElementNode(e){return e.nodeType===this._document.ELEMENT_NODE}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function Ug(t,n){return typeof t==`string`?`${n||``}/${t}`:t}function KC(t,n){t.id||(t.id=`${XC}-${n}-${$g++}`)}var si=(function(t){return t[t.NORMAL=0]=`NORMAL`,t[t.NEGATED=1]=`NEGATED`,t[t.INVERTED=2]=`INVERTED`,t})(si||{});var Fu;var Fo;function Lu(){if(Fo==null){if(typeof document!=`object`||!document||typeof Element!=`function`||!Element)return Fo=!1,Fo;if(document.documentElement?.style&&`scrollBehavior`in document.documentElement.style)Fo=!0;else{let t=Element.prototype.scrollTo;t?Fo=!/\{\s*\[native code\]\s*\}/.test(t.toString()):Fo=!1}}return Fo}function Hr(){if(typeof document!=`object`||!document)return si.NORMAL;if(Fu==null){let t=document.createElement(`div`),n=t.style;t.dir=`rtl`,n.width=`1px`,n.overflow=`auto`,n.visibility=`hidden`,n.pointerEvents=`none`,n.position=`absolute`;let e=document.createElement(`div`),i=e.style;i.width=`2px`,i.height=`1px`,t.appendChild(e),document.body.appendChild(t),Fu=si.NORMAL,t.scrollLeft===0&&(t.scrollLeft=1,Fu=t.scrollLeft===0?si.NEGATED:si.INVERTED),t.remove()}return Fu}function Gg(){return typeof __karma__<`u`&&!!__karma__||typeof jasmine<`u`&&!!jasmine||typeof jest<`u`&&!!jest||typeof Mocha<`u`&&!!Mocha}var Ur;var ZC=[`color`,`button`,`checkbox`,`date`,`datetime-local`,`email`,`file`,`hidden`,`image`,`month`,`number`,`password`,`radio`,`range`,`reset`,`search`,`submit`,`tel`,`text`,`time`,`url`,`week`];function Wg(){if(Ur)return Ur;if(typeof document!=`object`||!document)return Ur=new Set(ZC),Ur;let t=document.createElement(`input`);return Ur=new Set(ZC.filter(n=>(t.setAttribute(`type`,n),t.type===n))),Ur}var EA=new N(`MATERIAL_ANIMATIONS`);var JC=null;function Tl(){return f(EA,{optional:!0})?.animationsDisabled||f(js,{optional:!0})===`NoopAnimations`?`di-disabled`:(JC??=f(Br).matchMedia(`(prefers-reduced-motion)`).matches,JC?`reduced-motion`:`enabled`)}function Ue(){return Tl()!==`enabled`}function It(t){return t==null?``:typeof t==`string`?t:`${t}px`}function ta(t){return t!=null&&`${t}`!=`false`}var Vn=(function(t){return t[t.FADING_IN=0]=`FADING_IN`,t[t.VISIBLE=1]=`VISIBLE`,t[t.FADING_OUT=2]=`FADING_OUT`,t[t.HIDDEN=3]=`HIDDEN`,t})(Vn||{});var qg=class{_renderer;element;config;_animationForciblyDisabledThroughCss;state=Vn.HIDDEN;constructor(n,e,i,a=!1){this._renderer=n,this.element=e,this.config=i,this._animationForciblyDisabledThroughCss=a}fadeOut(){this._renderer.fadeOutRipple(this)}};var eS=Lr({passive:!0,capture:!0});var Yg=class{_events=new Map;addHandler(n,e,i,a){let o=this._events.get(e);if(o){let r=o.get(i);r?r.add(a):o.set(i,new Set([a]))}else this._events.set(e,new Map([[i,new Set([a])]])),n.runOutsideAngular(()=>{document.addEventListener(e,this._delegateEventHandler,eS)})}removeHandler(n,e,i){let a=this._events.get(n);if(!a)return;let o=a.get(e);o&&(o.delete(i),o.size===0&&a.delete(e),a.size===0&&(this._events.delete(n),document.removeEventListener(n,this._delegateEventHandler,eS)))}_delegateEventHandler=n=>{let e=Yt(n);e&&this._events.get(n.type)?.forEach((i,a)=>{(a===e||a.contains(e))&&i.forEach(o=>o.handleEvent(n))})}};var Nl={enterDuration:225,exitDuration:150};var DA=800;var tS=Lr({passive:!0,capture:!0});var nS=[`mousedown`,`touchstart`];var iS=[`mouseup`,`mouseleave`,`touchend`,`touchcancel`];var IA=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`ng-component`]],hostAttrs:[`mat-ripple-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`.mat-ripple {
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
`],encapsulation:2})}return t})();var Al=class t{_target;_ngZone;_platform;_containerElement;_triggerElement=null;_isPointerDown=!1;_activeRipples=new Map;_mostRecentTransientRipple=null;_lastTouchStartEvent;_pointerUpEventsRegistered=!1;_containerRect=null;static _eventManager=new Yg;constructor(n,e,i,a,o){this._target=n,this._ngZone=e,this._platform=a,a.isBrowser&&(this._containerElement=mn(i)),o&&o.get(Je).load(IA)}fadeInRipple(n,e,i={}){let a=this._containerRect=this._containerRect||this._containerElement.getBoundingClientRect(),o=r(r({},Nl),i.animation);i.centered&&(n=a.left+a.width/2,e=a.top+a.height/2);let r$2=i.radius||kA(n,e,a),s=n-a.left,l=e-a.top,h=o.enterDuration,p=document.createElement(`div`);p.classList.add(`mat-ripple-element`),p.style.left=`${s-r$2}px`,p.style.top=`${l-r$2}px`,p.style.height=`${r$2*2}px`,p.style.width=`${r$2*2}px`,i.color!=null&&(p.style.backgroundColor=i.color),p.style.transitionDuration=`${h}ms`,this._containerElement.appendChild(p);let g=window.getComputedStyle(p),_=g.transitionProperty,b=g.transitionDuration,y=_===`none`||b===`0s`||b===`0s, 0s`||a.width===0&&a.height===0,I=new qg(this,p,i,y);p.style.transform=`scale3d(1, 1, 1)`,I.state=Vn.FADING_IN,i.persistent||(this._mostRecentTransientRipple=I);let P=null;return!y&&(h||o.exitDuration)&&this._ngZone.runOutsideAngular(()=>{let ie=()=>{P&&(P.fallbackTimer=null),clearTimeout(x),this._finishRippleTransition(I)},L=()=>this._destroyRipple(I),x=setTimeout(L,h+100);p.addEventListener(`transitionend`,ie),p.addEventListener(`transitioncancel`,L),P={onTransitionEnd:ie,onTransitionCancel:L,fallbackTimer:x}}),this._activeRipples.set(I,P),(y||!h)&&this._finishRippleTransition(I),I}fadeOutRipple(n){if(n.state===Vn.FADING_OUT||n.state===Vn.HIDDEN)return;let e=n.element,i=r(r({},Nl),n.config.animation);e.style.transitionDuration=`${i.exitDuration}ms`,e.style.opacity=`0`,n.state=Vn.FADING_OUT,(n._animationForciblyDisabledThroughCss||!i.exitDuration)&&this._finishRippleTransition(n)}fadeOutAll(){this._getActiveRipples().forEach(n=>n.fadeOut())}fadeOutAllNonPersistent(){this._getActiveRipples().forEach(n=>{n.config.persistent||n.fadeOut()})}setupTriggerEvents(n){let e=mn(n);!this._platform.isBrowser||!e||e===this._triggerElement||(this._removeTriggerEvents(),this._triggerElement=e,nS.forEach(i=>{t._eventManager.addHandler(this._ngZone,i,e,this)}))}handleEvent(n){n.type===`mousedown`?this._onMousedown(n):n.type===`touchstart`?this._onTouchStart(n):this._onPointerUp(),this._pointerUpEventsRegistered||(this._ngZone.runOutsideAngular(()=>{iS.forEach(e=>{this._triggerElement.addEventListener(e,this,tS)})}),this._pointerUpEventsRegistered=!0)}_finishRippleTransition(n){n.state===Vn.FADING_IN?this._startFadeOutTransition(n):n.state===Vn.FADING_OUT&&this._destroyRipple(n)}_startFadeOutTransition(n){let e=n===this._mostRecentTransientRipple,{persistent:i}=n.config;n.state=Vn.VISIBLE,!i&&(!e||!this._isPointerDown)&&n.fadeOut()}_destroyRipple(n){let e=this._activeRipples.get(n)??null;this._activeRipples.delete(n),this._activeRipples.size||(this._containerRect=null),n===this._mostRecentTransientRipple&&(this._mostRecentTransientRipple=null),n.state=Vn.HIDDEN,e!==null&&(n.element.removeEventListener(`transitionend`,e.onTransitionEnd),n.element.removeEventListener(`transitioncancel`,e.onTransitionCancel),e.fallbackTimer!==null&&clearTimeout(e.fallbackTimer)),n.element.remove()}_onMousedown(n){let e=ko(n),i=this._lastTouchStartEvent&&Date.now()<this._lastTouchStartEvent+DA;!this._target.rippleDisabled&&!e&&!i&&(this._isPointerDown=!0,this.fadeInRipple(n.clientX,n.clientY,this._target.rippleConfig))}_onTouchStart(n){if(!this._target.rippleDisabled&&!Mo(n)){this._lastTouchStartEvent=Date.now(),this._isPointerDown=!0;let e=n.changedTouches;if(e)for(let i=0;i<e.length;i++)this.fadeInRipple(e[i].clientX,e[i].clientY,this._target.rippleConfig)}}_onPointerUp(){this._isPointerDown&&(this._isPointerDown=!1,this._getActiveRipples().forEach(n=>{let e=n.state===Vn.VISIBLE||n.config.terminateOnPointerUp&&n.state===Vn.FADING_IN;!n.config.persistent&&e&&n.fadeOut()}))}_getActiveRipples(){return Array.from(this._activeRipples.keys())}_removeTriggerEvents(){let n=this._triggerElement;n&&(nS.forEach(e=>t._eventManager.removeHandler(e,n,this)),this._pointerUpEventsRegistered&&(iS.forEach(e=>n.removeEventListener(e,this,tS)),this._pointerUpEventsRegistered=!1))}};function kA(t,n,e){let i=Math.max(Math.abs(t-e.left),Math.abs(t-e.right)),a=Math.max(Math.abs(n-e.top),Math.abs(n-e.bottom));return Math.sqrt(i*i+a*a)}var $r=new N(`mat-ripple-global-options`);var li=(()=>{class t{_elementRef=f(Y);_animationsDisabled=Ue();color;unbounded=!1;centered=!1;radius=0;animation;get disabled(){return this._disabled}set disabled(e){e&&this.fadeOutAllNonPersistent(),this._disabled=e,this._setupTriggerEventsIfEnabled()}_disabled=!1;get trigger(){return this._trigger||this._elementRef.nativeElement}set trigger(e){this._trigger=e,this._setupTriggerEventsIfEnabled()}_trigger;_rippleRenderer;_globalOptions;_isInitialized=!1;constructor(){let e=f(Z),i=f(Re),a=f($r,{optional:!0}),o=f(se);this._globalOptions=a||{},this._rippleRenderer=new Al(this,e,this._elementRef,i,o)}ngOnInit(){this._isInitialized=!0,this._setupTriggerEventsIfEnabled()}ngOnDestroy(){this._rippleRenderer._removeTriggerEvents()}fadeOutAll(){this._rippleRenderer.fadeOutAll()}fadeOutAllNonPersistent(){this._rippleRenderer.fadeOutAllNonPersistent()}get rippleConfig(){return{centered:this.centered,radius:this.radius,color:this.color,animation:r(r(r({},this._globalOptions.animation),this._animationsDisabled?{enterDuration:0,exitDuration:0}:{}),this.animation),terminateOnPointerUp:this._globalOptions.terminateOnPointerUp}}get rippleDisabled(){return this.disabled||!!this._globalOptions.disabled}_setupTriggerEventsIfEnabled(){!this.disabled&&this._isInitialized&&this._rippleRenderer.setupTriggerEvents(this.trigger)}launch(e,i=0,a){return typeof e==`number`?this._rippleRenderer.fadeInRipple(e,i,r(r({},this.rippleConfig),a)):this._rippleRenderer.fadeInRipple(0,0,r(r({},this.rippleConfig),e))}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`mat-ripple`,``],[``,`matRipple`,``]],hostAttrs:[1,`mat-ripple`],hostVars:2,hostBindings:function(i,a){i&2&&G(`mat-ripple-unbounded`,a.unbounded)},inputs:{color:[0,`matRippleColor`,`color`],unbounded:[0,`matRippleUnbounded`,`unbounded`],centered:[0,`matRippleCentered`,`centered`],radius:[0,`matRippleRadius`,`radius`],animation:[0,`matRippleAnimation`,`animation`],disabled:[0,`matRippleDisabled`,`disabled`],trigger:[0,`matRippleTrigger`,`trigger`]},exportAs:[`matRipple`]})}return t})();var MA={capture:!0};var TA=[`focus`,`mousedown`,`mouseenter`,`touchstart`];var Kg=`mat-ripple-loader-uninitialized`;var Qg=`mat-ripple-loader-class-name`;var aS=`mat-ripple-loader-centered`;var Vu=`mat-ripple-loader-disabled`;var Bu=(()=>{class t{_document=f(me);_animationsDisabled=Ue();_globalRippleOptions=f($r,{optional:!0});_platform=f(Re);_ngZone=f(Z);_injector=f(se);_eventCleanups;_hosts=new Map;constructor(){let e=f(Nt).createRenderer(null,null);this._eventCleanups=this._ngZone.runOutsideAngular(()=>TA.map(i=>e.listen(this._document,i,this._onInteraction,MA)))}ngOnDestroy(){let e=this._hosts.keys();for(let i of e)this.destroyRipple(i);this._eventCleanups.forEach(i=>i())}configureRipple(e,i){e.setAttribute(Kg,this._globalRippleOptions?.namespace??``),(i.className||!e.hasAttribute(Qg))&&e.setAttribute(Qg,i.className||``),i.centered&&e.setAttribute(aS,``),i.disabled&&e.setAttribute(Vu,``)}setDisabled(e,i){let a=this._hosts.get(e);a?(a.target.rippleDisabled=i,!i&&!a.hasSetUpEvents&&(a.hasSetUpEvents=!0,a.renderer.setupTriggerEvents(e))):i?e.setAttribute(Vu,``):e.removeAttribute(Vu)}_onInteraction=e=>{let i=Yt(e);if(i instanceof HTMLElement){let a=i.closest(`[${Kg}="${this._globalRippleOptions?.namespace??``}"]`);a&&this._createRipple(a)}};_createRipple(e){if(!this._document||this._hosts.has(e))return;e.querySelector(`.mat-ripple`)?.remove();let i=this._document.createElement(`span`);i.classList.add(`mat-ripple`,e.getAttribute(Qg)),e.append(i);let a=this._globalRippleOptions,o=this._animationsDisabled?0:a?.animation?.enterDuration??Nl.enterDuration,r=this._animationsDisabled?0:a?.animation?.exitDuration??Nl.exitDuration,s={rippleDisabled:this._animationsDisabled||a?.disabled||e.hasAttribute(Vu),rippleConfig:{centered:e.hasAttribute(aS),terminateOnPointerUp:a?.terminateOnPointerUp,animation:{enterDuration:o,exitDuration:r}}},l=new Al(s,this._ngZone,i,this._platform,this._injector),h=!s.rippleDisabled;h&&l.setupTriggerEvents(e),this._hosts.set(e,{target:s,renderer:l,hasSetUpEvents:h}),e.removeAttribute(Kg)}destroyRipple(e){let i=this._hosts.get(e);i&&(i.renderer._removeTriggerEvents(),this._hosts.delete(e))}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var pn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`structural-styles`]],decls:0,vars:0,template:function(i,a){},styles:[`.mat-focus-indicator {
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
`],encapsulation:2})}return t})();var NA=new N(`MAT_BUTTON_CONFIG`);function oS(t){return t==null?void 0:dt(t)}var Xg=(()=>{class t{_elementRef=f(Y);_ngZone=f(Z);_animationsDisabled=Ue();_config=f(NA,{optional:!0});_focusMonitor=f(tn);_cleanupClick;_renderer=f(Pe);_rippleLoader=f(Bu);_isAnchor;_isFab=!1;color;get disableRipple(){return this._disableRipple}set disableRipple(e){this._disableRipple=e,this._updateRippleDisabled()}_disableRipple=!1;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._updateRippleDisabled()}_disabled=!1;ariaDisabled;disabledInteractive;tabIndex;set _tabindex(e){this.tabIndex=e}showProgress=zt(!1,{transform:z});constructor(){f(Je).load(pn);let e=this._elementRef.nativeElement;this._isAnchor=e.tagName===`A`,this.disabledInteractive=this._config?.disabledInteractive??!1,this.color=this._config?.color??null,this._rippleLoader?.configureRipple(e,{className:`mat-mdc-button-ripple`})}ngAfterViewInit(){this._focusMonitor.monitor(this._elementRef,!0),this._isAnchor&&this._setupAsAnchor()}ngOnDestroy(){this._cleanupClick?.(),this._focusMonitor.stopMonitoring(this._elementRef),this._rippleLoader?.destroyRipple(this._elementRef.nativeElement)}focus(e=`program`,i){e?this._focusMonitor.focusVia(this._elementRef.nativeElement,e,i):this._elementRef.nativeElement.focus(i)}_getAriaDisabled(){return this.ariaDisabled!=null?this.ariaDisabled:this._isAnchor?this.disabled||null:this.disabled&&this.disabledInteractive?!0:null}_getDisabledAttribute(){return this.disabledInteractive||!this.disabled?null:!0}_updateRippleDisabled(){this._rippleLoader?.setDisabled(this._elementRef.nativeElement,this.disableRipple||this.disabled)}_getTabIndex(){return this._isAnchor?this.disabled&&!this.disabledInteractive?-1:this.tabIndex:this.tabIndex}_setupAsAnchor(){this._cleanupClick=this._ngZone.runOutsideAngular(()=>this._renderer.listen(this._elementRef.nativeElement,`click`,e=>{this.disabled&&(e.preventDefault(),e.stopImmediatePropagation())}))}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,hostAttrs:[1,`mat-mdc-button-base`],hostVars:15,hostBindings:function(i,a){i&2&&(ne(`disabled`,a._getDisabledAttribute())(`aria-disabled`,a._getAriaDisabled())(`tabindex`,a._getTabIndex()),tt(a.color?`mat-`+a.color:``),G(`mat-mdc-button-progress-indicator-shown`,a.showProgress())(`mat-mdc-button-disabled`,a.disabled)(`mat-mdc-button-disabled-interactive`,a.disabledInteractive)(`mat-unthemed`,!a.color)(`_mat-animation-noopable`,a._animationsDisabled))},inputs:{color:`color`,disableRipple:[2,`disableRipple`,`disableRipple`,z],disabled:[2,`disabled`,`disabled`,z],ariaDisabled:[2,`aria-disabled`,`ariaDisabled`,z],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z],tabIndex:[2,`tabIndex`,`tabIndex`,oS],_tabindex:[2,`tabindex`,`_tabindex`,oS],showProgress:[1,`showProgress`]}})}return t})();var Zg=(()=>{class t extends Xg{constructor(){super(),this._rippleLoader.configureRipple(this._elementRef.nativeElement,{centered:!0})}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[``,`progressIndicator`,``]]],i=[`*`,`[progressIndicator]`];function a(o,r){o&1&&(ct(0,`div`,1),he(1,1),ht())}return H({type:t,selectors:[[`button`,`mat-icon-button`,``],[`a`,`mat-icon-button`,``],[`button`,`matIconButton`,``],[`a`,`matIconButton`,``]],hostAttrs:[1,`mdc-icon-button`,`mat-mdc-icon-button`],exportAs:[`matButton`,`matAnchor`],features:[Be],ngContentSelectors:i,decls:5,vars:1,consts:[[1,`mat-mdc-button-persistent-ripple`,`mdc-icon-button__ripple`],[1,`mat-mdc-button-progress-indicator-container`],[1,`mat-focus-indicator`],[1,`mat-mdc-button-touch-target`]],template:function(r,s){r&1&&(Fe(e),Wt(0,`span`,0),he(1),E(2,a,2,0,`div`,1),Wt(3,`span`,2)(4,`span`,3)),r&2&&(m(2),D(s.showProgress()?2:-1))},styles:[`.mat-mdc-icon-button {
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
`],encapsulation:2})})()}return t})();var AA=new N(`cdk-dir-doc`,{providedIn:`root`,factory:()=>f(me)});var RA=/^(ar|ckb|dv|he|iw|fa|nqo|ps|sd|ug|ur|yi|.*[-_](Adlm|Arab|Hebr|Nkoo|Rohg|Thaa))(?!.*[-_](Latn|Cyrl)($|-|_))($|-|_)/i;function rS(t){let n=t?.toLowerCase()||``;return n===`auto`&&typeof navigator<`u`&&navigator?.language?RA.test(navigator.language)?`rtl`:`ltr`:n===`rtl`?`rtl`:`ltr`}var gt=(()=>{class t{get value(){return this.valueSignal()}valueSignal=T(`ltr`);change=new te;constructor(){let e=f(AA,{optional:!0});if(e){let i=e.body?e.body.dir:null,a=e.documentElement?e.documentElement.dir:null;this.valueSignal.set(rS(i||a||`ltr`))}}ngOnDestroy(){this.change.complete()}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var we=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({})}return t})();var ki=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[we]})}return t})();var sS=new Map([[`text`,[`mat-mdc-button`]],[`filled`,[`mdc-button--unelevated`,`mat-mdc-unelevated-button`]],[`elevated`,[`mdc-button--raised`,`mat-mdc-raised-button`]],[`outlined`,[`mdc-button--outlined`,`mat-mdc-outlined-button`]],[`tonal`,[`mat-tonal-button`]]]);var nn=(()=>{class t extends Xg{get appearance(){return this._appearance}set appearance(e){this.setAppearance(e||this._config?.defaultAppearance||`text`)}_appearance=null;constructor(){super();let e=OA(this._elementRef.nativeElement);e&&this.setAppearance(e)}setAppearance(e){if(e===this._appearance)return;let i=this._elementRef.nativeElement.classList,a=this._appearance?sS.get(this._appearance):null,o=sS.get(e);a&&i.remove(...a),i.add(...o),this._appearance=e}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[``,8,`material-icons`,3,`iconPositionEnd`,``],[`mat-icon`,3,`iconPositionEnd`,``],[``,`matButtonIcon`,``,3,`iconPositionEnd`,``],[``,8,`material-symbols-outlined`,3,`iconPositionEnd`,``],[``,8,`material-symbols-rounded`,3,`iconPositionEnd`,``],[``,8,`material-symbols-sharp`,3,`iconPositionEnd`,``]],`*`,[[``,`iconPositionEnd`,``,8,`material-icons`],[`mat-icon`,`iconPositionEnd`,``],[``,`matButtonIcon`,``,`iconPositionEnd`,``],[``,`iconPositionEnd`,``,8,`material-symbols-outlined`],[``,`iconPositionEnd`,``,8,`material-symbols-rounded`],[``,`iconPositionEnd`,``,8,`material-symbols-sharp`]],[[``,`progressIndicator`,``]]],i=[`.material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd]), .material-symbols-outlined:not([iconPositionEnd]), .material-symbols-rounded:not([iconPositionEnd]), .material-symbols-sharp:not([iconPositionEnd])`,`*`,`.material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd], .material-symbols-outlined[iconPositionEnd], .material-symbols-rounded[iconPositionEnd], .material-symbols-sharp[iconPositionEnd]`,`[progressIndicator]`];function a(o,r){o&1&&(ct(0,`div`,2),he(1,3),ht())}return H({type:t,selectors:[[`button`,`matButton`,``],[`a`,`matButton`,``],[`button`,`mat-button`,``],[`button`,`mat-raised-button`,``],[`button`,`mat-flat-button`,``],[`button`,`mat-stroked-button`,``],[`a`,`mat-button`,``],[`a`,`mat-raised-button`,``],[`a`,`mat-flat-button`,``],[`a`,`mat-stroked-button`,``]],hostAttrs:[1,`mdc-button`],inputs:{appearance:[0,`matButton`,`appearance`]},exportAs:[`matButton`,`matAnchor`],features:[Be],ngContentSelectors:i,decls:8,vars:5,consts:[[1,`mat-mdc-button-persistent-ripple`],[1,`mdc-button__label`],[1,`mat-mdc-button-progress-indicator-container`],[1,`mat-focus-indicator`],[1,`mat-mdc-button-touch-target`]],template:function(r,s){r&1&&(Fe(e),Wt(0,`span`,0),he(1),ct(2,`span`,1),he(3,1),ht(),he(4,2),E(5,a,2,0,`div`,2),Wt(6,`span`,3)(7,`span`,4)),r&2&&(G(`mdc-button__ripple`,!s._isFab)(`mdc-fab__ripple`,s._isFab),m(5),D(s.showProgress()?5:-1))},styles:[`.mat-mdc-button-base {
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
`],encapsulation:2})})()}return t})();function OA(t){return t.hasAttribute(`mat-raised-button`)?`elevated`:t.hasAttribute(`mat-stroked-button`)?`outlined`:t.hasAttribute(`mat-flat-button`)?`filled`:t.hasAttribute(`mat-button`)?`text`:null}var Ft=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[ki,we]})}return t})();var PA=new N(`MAT_CARD_CONFIG`);var Bn=(()=>{class t{appearance;constructor(){let e=f(PA,{optional:!0});this.appearance=e?.appearance||`raised`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return H({type:t,selectors:[[`mat-card`]],hostAttrs:[1,`mat-mdc-card`,`mdc-card`],hostVars:8,hostBindings:function(a,o){a&2&&G(`mat-mdc-card-outlined`,o.appearance===`outlined`)(`mdc-card--outlined`,o.appearance===`outlined`)(`mat-mdc-card-filled`,o.appearance===`filled`)(`mdc-card--filled`,o.appearance===`filled`)},inputs:{appearance:`appearance`},exportAs:[`matCard`],ngContentSelectors:[`*`],decls:1,vars:0,template:function(a,o){a&1&&(Fe(),he(0))},styles:[`.mat-mdc-card {
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
`],encapsulation:2})})()}return t})();var Ma=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-card-title`],[``,`mat-card-title`,``],[``,`matCardTitle`,``]],hostAttrs:[1,`mat-mdc-card-title`]})}return t})();var jn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-card-content`]],hostAttrs:[1,`mat-mdc-card-content`]})}return t})();var ju=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-card-subtitle`],[``,`mat-card-subtitle`,``],[``,`matCardSubtitle`,``]],hostAttrs:[1,`mat-mdc-card-subtitle`]})}return t})();var Ta=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[``,`mat-card-avatar`,``],[``,`matCardAvatar`,``]],[[`mat-card-title`],[`mat-card-subtitle`],[``,`mat-card-title`,``],[``,`mat-card-subtitle`,``],[``,`matCardTitle`,``],[``,`matCardSubtitle`,``]],`*`];return H({type:t,selectors:[[`mat-card-header`]],hostAttrs:[1,`mat-mdc-card-header`],ngContentSelectors:[`[mat-card-avatar], [matCardAvatar]`,`mat-card-title, mat-card-subtitle,
      [mat-card-title], [mat-card-subtitle],
      [matCardTitle], [matCardSubtitle]`,`*`],decls:4,vars:0,consts:[[1,`mat-mdc-card-header-text`]],template:function(o,r){o&1&&(Fe(e),he(0),ct(1,`div`,0),he(2,1),ht(),he(3,2))},encapsulation:2})})()}return t})();var zn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[we]})}return t})();var FA=new N(`mat-progress-spinner-default-options`,{providedIn:`root`,factory:()=>({diameter:lS})});var lS=100;var LA=10;var cS=(()=>{class t{_elementRef=f(Y);_noopAnimations;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor=`primary`;_determinateCircle;constructor(){let e=f(FA),i=Tl(),a=this._elementRef.nativeElement;this._noopAnimations=i===`di-disabled`&&!!e&&!e._forceAnimations,this.mode=a.nodeName.toLowerCase()===`mat-spinner`?`indeterminate`:`determinate`,!this._noopAnimations&&i===`reduced-motion`&&a.classList.add(`mat-progress-spinner-reduced-motion`),e&&(e.color&&(this.color=this._defaultColor=e.color),e.diameter&&(this.diameter=e.diameter),e.strokeWidth&&(this.strokeWidth=e.strokeWidth))}mode;get value(){return this.mode===`determinate`?this._value:0}set value(e){this._value=Math.max(0,Math.min(100,e||0))}_value=0;get diameter(){return this._diameter}set diameter(e){this._diameter=e||0}_diameter=lS;get strokeWidth(){return this._strokeWidth??this.diameter/10}set strokeWidth(e){this._strokeWidth=e||0}_strokeWidth;_circleRadius(){return(this.diameter-LA)/2}_viewBox(){let e=this._circleRadius()*2+this.strokeWidth;return`0 0 ${e} ${e}`}_strokeCircumference(){return 2*Math.PI*this._circleRadius()}_strokeDashOffset(){return this.mode===`determinate`?this._strokeCircumference()*(100-this._value)/100:null}_circleStrokeWidth(){return this.strokeWidth/this.diameter*100}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`determinateSpinner`];function i(a,o){if(a&1&&(Gt(),c(0,`svg`,11),U(1,`circle`,12),d()),a&2){let r=v();ne(`viewBox`,r._viewBox()),m(),Jt(`stroke-dasharray`,r._strokeCircumference(),`px`)(`stroke-dashoffset`,r._strokeCircumference()/2,`px`)(`stroke-width`,r._circleStrokeWidth(),`%`),ne(`r`,r._circleRadius())}}return H({type:t,selectors:[[`mat-progress-spinner`],[`mat-spinner`]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;J(s=ee())&&(r._determinateCircle=s.first)}},hostAttrs:[`role`,`progressbar`,`tabindex`,`-1`,1,`mat-mdc-progress-spinner`,`mdc-circular-progress`],hostVars:18,hostBindings:function(o,r){o&2&&(ne(`aria-valuemin`,0)(`aria-valuemax`,100)(`aria-valuenow`,r.mode===`determinate`?r.value:null)(`mode`,r.mode),tt(`mat-`+r.color),Jt(`width`,r.diameter,`px`)(`height`,r.diameter,`px`)(`--%NS%mat-progress-spinner-size`,r.diameter+`px`)(`--%NS%mat-progress-spinner-active-indicator-width`,r.diameter+`px`),G(`_mat-animation-noopable`,r._noopAnimations)(`mdc-circular-progress--indeterminate`,r.mode===`indeterminate`))},inputs:{color:`color`,mode:`mode`,value:[2,`value`,`value`,dt],diameter:[2,`diameter`,`diameter`,dt],strokeWidth:[2,`strokeWidth`,`strokeWidth`,dt]},exportAs:[`matProgressSpinner`],decls:14,vars:11,consts:[[`circle`,``],[`determinateSpinner`,``],[`aria-hidden`,`true`,1,`mdc-circular-progress__determinate-container`],[`xmlns`,`http://www.w3.org/2000/svg`,`focusable`,`false`,1,`mdc-circular-progress__determinate-circle-graphic`],[`cx`,`50%`,`cy`,`50%`,1,`mdc-circular-progress__determinate-circle`],[`aria-hidden`,`true`,1,`mdc-circular-progress__indeterminate-container`],[1,`mdc-circular-progress__spinner-layer`],[1,`mdc-circular-progress__circle-clipper`,`mdc-circular-progress__circle-left`],[3,`ngTemplateOutlet`],[1,`mdc-circular-progress__gap-patch`],[1,`mdc-circular-progress__circle-clipper`,`mdc-circular-progress__circle-right`],[`xmlns`,`http://www.w3.org/2000/svg`,`focusable`,`false`,1,`mdc-circular-progress__indeterminate-circle-graphic`],[`cx`,`50%`,`cy`,`50%`]],template:function(o,r){if(o&1&&(st(0,i,2,8,`ng-template`,null,0,dl),c(2,`div`,2,1),Gt(),c(4,`svg`,3),U(5,`circle`,4),d()(),ya(),c(6,`div`,5)(7,`div`,6)(8,`div`,7),Ea(9,8),d(),c(10,`div`,9),Ea(11,8),d(),c(12,`div`,10),Ea(13,8),d()()()),o&2){let s=ft(1);m(4),ne(`viewBox`,r._viewBox()),m(),Jt(`stroke-dasharray`,r._strokeCircumference(),`px`)(`stroke-dashoffset`,r._strokeDashOffset(),`px`)(`stroke-width`,r._circleStrokeWidth(),`%`),ne(`r`,r._circleRadius()),m(4),S(`ngTemplateOutlet`,s),m(2),S(`ngTemplateOutlet`,s),m(2),S(`ngTemplateOutlet`,s)}},dependencies:[_l],styles:[`.mat-mdc-progress-spinner {
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
`],encapsulation:2})})()}return t})();var dS=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[we]})}return t})();var _S=(()=>{class t{_renderer;_elementRef;onChange=e=>{};onTouched=()=>{};constructor(e,i){this._renderer=e,this._elementRef=i}setProperty(e,i){this._renderer.setProperty(this._elementRef.nativeElement,e,i)}registerOnTouched(e){this.onTouched=e}registerOnChange(e){this.onChange=e}setDisabledState(e){this.setProperty(`disabled`,e)}static ɵfac=function(i){return new(i||t)(He(Pe),He(Y))};static ɵdir=Q({type:t})}return t})();var BA=(()=>{class t extends _S{static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,features:[Be]})}return t})();var Kr=new N(``);var jA={provide:Kr,useExisting:Ht(()=>Xu),multi:!0};function zA(){let t=Fn()?Fn().getUserAgent():``;return/android (\d+)/.test(t.toLowerCase())}var HA=new N(``);var Xu=(()=>{class t extends _S{_compositionMode;_composing=!1;constructor(e,i,a){super(e,i),this._compositionMode=a,this._compositionMode??=!zA()}writeValue(e){let i=e??``;this.setProperty(`value`,i)}_handleInput(e){(!this._compositionMode||this._compositionMode&&!this._composing)&&this.onChange(e)}_compositionStart(){this._composing=!0}_compositionEnd(e){this._composing=!1,this._compositionMode&&this.onChange(e)}static ɵfac=function(i){return new(i||t)(He(Pe),He(Y),He(HA,8))};static ɵdir=Q({type:t,selectors:[[`input`,`formControlName`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`formControlName`,``,3,`ngNoCva`,``],[`input`,`formControl`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`formControl`,``,3,`ngNoCva`,``],[`input`,`ngModel`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`ngModel`,``,3,`ngNoCva`,``],[``,`ngDefaultControl`,``]],hostBindings:function(i,a){i&1&&C(`input`,function(r){return a._handleInput(r.target.value)})(`blur`,function(){return a.onTouched()})(`compositionstart`,function(){return a._compositionStart()})(`compositionend`,function(r){return a._compositionEnd(r.target.value)})},standalone:!1,features:[Ge([jA]),Be]})}return t})();function nb(t){return t==null||ib(t)===0}function ib(t){return t==null?null:Array.isArray(t)||typeof t==`string`?t.length:t instanceof Set?t.size:null}var Qr=new N(``);var ab=new N(``);var UA=/^(?=.{1,254}$)(?=.{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;var Lo=class{static min(n){return $A(n)}static max(n){return GA(n)}static required(n){return yS(n)}static requiredTrue(n){return WA(n)}static email(n){return qA(n)}static minLength(n){return YA(n)}static maxLength(n){return KA(n)}static pattern(n){return QA(n)}static nullValidator(n){return Hu()}static compose(n){return DS(n)}static composeAsync(n){return IS(n)}};function $A(t){return n=>{if(n.value==null||t==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e<t?{min:{min:t,actual:n.value}}:null}}function GA(t){return n=>{if(n.value==null||t==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e>t?{max:{max:t,actual:n.value}}:null}}function yS(t){return nb(t.value)?{required:!0}:null}function WA(t){return t.value===!0?null:{required:!0}}function qA(t){return nb(t.value)||UA.test(t.value)?null:{email:!0}}function YA(t){return n=>{let e=n.value?.length??ib(n.value);return e===null||e===0?null:e<t?{minlength:{requiredLength:t,actualLength:e}}:null}}function KA(t){return n=>{let e=n.value?.length??ib(n.value);return e!==null&&e>t?{maxlength:{requiredLength:t,actualLength:e}}:null}}function QA(t){if(!t)return Hu;let n,e;return typeof t==`string`?(e=``,t.charAt(0)!==`^`&&(e+=`^`),e+=t,t.charAt(t.length-1)!==`$`&&(e+=`$`),n=new RegExp(e)):(e=t.toString(),n=t),i=>{if(nb(i.value))return null;let a=i.value;return n.test(a)?null:{pattern:{requiredPattern:e,actualValue:a}}}}function Hu(t){return null}function xS(t){return t!=null}function CS(t){return So(t)?kn(t):t}function SS(t){let n={};return t.forEach(e=>{n=e!=null?r(r({},n),e):n}),Object.keys(n).length===0?null:n}function wS(t,n){return n.map(e=>e(t))}function XA(t){return!t.validate}function ES(t){return t.map(n=>XA(n)?n:e=>n.validate(e))}function DS(t){if(!t)return null;let n=t.filter(xS);return n.length==0?null:function(e){return SS(wS(e,n))}}function ob(t){return t!=null?DS(ES(t)):null}function IS(t){if(!t)return null;let n=t.filter(xS);return n.length==0?null:function(e){return pp(wS(e,n).map(CS)).pipe(lt(SS))}}function rb(t){return t!=null?IS(ES(t)):null}function uS(t,n){return t===null?[n]:Array.isArray(t)?[...t,n]:[t,n]}function kS(t){return t._rawValidators}function MS(t){return t._rawAsyncValidators}function Jg(t){return t?Array.isArray(t)?t:[t]:[]}function Uu(t,n){return Array.isArray(t)?t.includes(n):t===n}function mS(t,n){let e=Jg(n);return Jg(t).forEach(a=>{Uu(e,a)||e.push(a)}),e}function pS(t,n){return Jg(n).filter(e=>!Uu(t,e))}var $u=class{get value(){return this.control?this.control.value:null}get valid(){return this.control?this.control.valid:null}get invalid(){return this.control?this.control.invalid:null}get pending(){return this.control?this.control.pending:null}get disabled(){return this.control?this.control.disabled:null}get enabled(){return this.control?this.control.enabled:null}get errors(){return this.control?this.control.errors:null}get pristine(){return this.control?this.control.pristine:null}get dirty(){return this.control?this.control.dirty:null}get touched(){return this.control?this.control.touched:null}get status(){return this.control?this.control.status:null}get untouched(){return this.control?this.control.untouched:null}get statusChanges(){return this.control?this.control.statusChanges:null}get valueChanges(){return this.control?this.control.valueChanges:null}get path(){return null}_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators=[];_rawAsyncValidators=[];_setValidators(n){this._rawValidators=n||[],this._composedValidatorFn=ob(this._rawValidators)}_setAsyncValidators(n){this._rawAsyncValidators=n||[],this._composedAsyncValidatorFn=rb(this._rawAsyncValidators)}get validator(){return this._composedValidatorFn||null}get asyncValidator(){return this._composedAsyncValidatorFn||null}_onDestroyCallbacks=[];_registerOnDestroy(n){this._onDestroyCallbacks.push(n)}_invokeOnDestroyCallbacks(){this._onDestroyCallbacks.forEach(n=>n()),this._onDestroyCallbacks=[]}reset(n=void 0){this.control?.reset(n)}hasError(n,e){return this.control?this.control.hasError(n,e):!1}getError(n,e){return this.control?this.control.getError(n,e):null}};var Vo=class extends $u{name;get formDirective(){return null}get path(){return null}};var Rl=`VALID`;var zu=`INVALID`;var Wr=`PENDING`;var Ol=`DISABLED`;var Na=class{};var Gu=class extends Na{value;source;constructor(n,e){super(),this.value=n,this.source=e}};var Fl=class extends Na{pristine;source;constructor(n,e){super(),this.pristine=n,this.source=e}};var Ll=class extends Na{touched;source;constructor(n,e){super(),this.touched=n,this.source=e}};var qr=class extends Na{status;source;constructor(n,e){super(),this.status=n,this.source=e}};var Wu=class extends Na{source;constructor(n){super(),this.source=n}};var Yr=class extends Na{source;constructor(n){super(),this.source=n}};function TS(t){return(Zu(t)?t.validators:t)||null}function ZA(t){return Array.isArray(t)?ob(t):t||null}function NS(t,n){return(Zu(n)?n.asyncValidators:t)||null}function JA(t){return Array.isArray(t)?rb(t):t||null}function Zu(t){return t!=null&&!Array.isArray(t)&&typeof t==`object`}function eR(t,n,e){let i=t.controls;if(!(n?Object.keys(i):i).length)throw new q(1e3,``);if(!AS(i,e))throw new q(1001,``)}function tR(t,n,e){t._forEachChild((i,a)=>{if(e[a]===void 0)throw new q(-1002,``)})}var qu=class{_pendingDirty=!1;_hasOwnPendingAsyncValidator=null;_pendingTouched=!1;_onCollectionChange=()=>{};_updateOn;_hasRequired=T(!1);_parent=null;_asyncValidationSubscription;_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators;_rawAsyncValidators;value;constructor(n,e){this._assignValidators(n),this._assignAsyncValidators(e)}get validator(){return this._composedValidatorFn}set validator(n){this._rawValidators=this._composedValidatorFn=n,this._updateHasRequiredValidator()}get asyncValidator(){return this._composedAsyncValidatorFn}set asyncValidator(n){this._rawAsyncValidators=this._composedAsyncValidatorFn=n}get parent(){return this._parent}get status(){return it(this.statusReactive)}set status(n){it(()=>this.statusReactive.set(n))}_status=K(()=>this.statusReactive());statusReactive=T(void 0);get valid(){return this.status===Rl}get invalid(){return this.status===zu}get pending(){return this.status===Wr}get disabled(){return this.status===Ol}get enabled(){return this.status!==Ol}errors;get pristine(){return it(this.pristineReactive)}set pristine(n){it(()=>this.pristineReactive.set(n))}_pristine=K(()=>this.pristineReactive());pristineReactive=T(!0);get dirty(){return!this.pristine}get touched(){return it(this.touchedReactive)}set touched(n){it(()=>this.touchedReactive.set(n))}_touched=K(()=>this.touchedReactive());touchedReactive=T(!1);get untouched(){return!this.touched}_events=new V;events=this._events.asObservable();valueChanges;statusChanges;get updateOn(){return this._updateOn?this._updateOn:this.parent?this.parent.updateOn:`change`}setValidators(n){this._assignValidators(n)}setAsyncValidators(n){this._assignAsyncValidators(n)}addValidators(n){this.setValidators(mS(n,this._rawValidators))}addAsyncValidators(n){this.setAsyncValidators(mS(n,this._rawAsyncValidators))}removeValidators(n){this.setValidators(pS(n,this._rawValidators))}removeAsyncValidators(n){this.setAsyncValidators(pS(n,this._rawAsyncValidators))}hasValidator(n){return Uu(this._rawValidators,n)}hasAsyncValidator(n){return Uu(this._rawAsyncValidators,n)}clearValidators(){this.validator=null}clearAsyncValidators(){this.asyncValidator=null}markAsTouched(n={}){let e=this.touched===!1;this.touched=!0;let i=n.sourceControl??this;n.onlySelf||this._parent?.markAsTouched(s(r({},n),{sourceControl:i})),e&&n.emitEvent!==!1&&this._events.next(new Ll(!0,i))}markAllAsDirty(n={}){this.markAsDirty({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsDirty(n))}markAllAsTouched(n={}){this.markAsTouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsTouched(n))}markAsUntouched(n={}){let e=this.touched===!0;this.touched=!1,this._pendingTouched=!1;let i=n.sourceControl??this;this._forEachChild(a=>{a.markAsUntouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:i})}),n.onlySelf||this._parent?._updateTouched(n,i),e&&n.emitEvent!==!1&&this._events.next(new Ll(!1,i))}markAsDirty(n={}){let e=this.pristine===!0;this.pristine=!1;let i=n.sourceControl??this;n.onlySelf||this._parent?.markAsDirty(s(r({},n),{sourceControl:i})),e&&n.emitEvent!==!1&&this._events.next(new Fl(!1,i))}markAsPristine(n={}){let e=this.pristine===!1;this.pristine=!0,this._pendingDirty=!1;let i=n.sourceControl??this;this._forEachChild(a=>{a.markAsPristine({onlySelf:!0,emitEvent:n.emitEvent})}),n.onlySelf||this._parent?._updatePristine(n,i),e&&n.emitEvent!==!1&&this._events.next(new Fl(!0,i))}markAsPending(n={}){this.status=Wr;let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new qr(this.status,e)),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.markAsPending(s(r({},n),{sourceControl:e}))}disable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=Ol,this.errors=null,this._forEachChild(a=>{a.disable(s(r({},n),{onlySelf:!0}))}),this._updateValue();let i=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new Gu(this.value,i)),this._events.next(new qr(this.status,i)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),this._updateAncestors(s(r({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(a=>a(!0))}enable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=Rl,this._forEachChild(i=>{i.enable(s(r({},n),{onlySelf:!0}))}),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent}),this._updateAncestors(s(r({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(i=>i(!1))}_updateAncestors(n,e){n.onlySelf||(this._parent?.updateValueAndValidity(n),n.skipPristineCheck||this._parent?._updatePristine({},e),this._parent?._updateTouched({},e))}setParent(n){this._parent=n}getRawValue(){return this.value}updateValueAndValidity(n={}){if(this._setInitialStatus(),this._updateValue(),this.enabled){let i=this._cancelExistingSubscription();this.errors=this._runValidator(),this.status=this._calculateStatus(),(this.status===Rl||this.status===Wr)&&this._runAsyncValidator(i,n.emitEvent)}let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new Gu(this.value,e)),this._events.next(new qr(this.status,e)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.updateValueAndValidity(s(r({},n),{sourceControl:e}))}_updateTreeValidity(n={emitEvent:!0}){this._forEachChild(e=>e._updateTreeValidity(n)),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent})}_setInitialStatus(){this.status=this._allControlsDisabled()?Ol:Rl}_runValidator(){return this.validator?this.validator(this):null}_runAsyncValidator(n,e){if(this.asyncValidator){this.status=Wr,this._hasOwnPendingAsyncValidator={emitEvent:e!==!1,shouldHaveEmitted:n!==!1};let i=CS(this.asyncValidator(this));this._asyncValidationSubscription=i.subscribe(a=>{this._hasOwnPendingAsyncValidator=null,this.setErrors(a,{emitEvent:e,shouldHaveEmitted:n})})}}_cancelExistingSubscription(){if(this._asyncValidationSubscription){this._asyncValidationSubscription.unsubscribe();let n=(this._hasOwnPendingAsyncValidator?.emitEvent||this._hasOwnPendingAsyncValidator?.shouldHaveEmitted)??!1;return this._hasOwnPendingAsyncValidator=null,n}return!1}setErrors(n,e={}){this.errors=n,this._updateControlsErrors(e.emitEvent!==!1,this,e.shouldHaveEmitted)}get(n){let e=n;return e==null||(Array.isArray(e)||(e=e.split(`.`)),e.length===0)?null:e.reduce((i,a)=>i&&i._find(a),this)}getError(n,e){let i=e?this.get(e):this;return i?.errors?i.errors[n]:null}hasError(n,e){return!!this.getError(n,e)}get root(){let n=this;for(;n._parent;)n=n._parent;return n}_updateControlsErrors(n,e,i){this.status=this._calculateStatus(),n&&this.statusChanges.emit(this.status),(n||i)&&this._events.next(new qr(this.status,e)),this._parent&&this._parent._updateControlsErrors(n,e,i)}_initObservables(){this.valueChanges=new te,this.statusChanges=new te}_calculateStatus(){return this._allControlsDisabled()?Ol:this.errors?zu:this._hasOwnPendingAsyncValidator||this._anyControlsHaveStatus(Wr)?Wr:this._anyControlsHaveStatus(zu)?zu:Rl}_anyControlsHaveStatus(n){return this._anyControls(e=>e.status===n)}_anyControlsDirty(){return this._anyControls(n=>n.dirty)}_anyControlsTouched(){return this._anyControls(n=>n.touched)}_updatePristine(n,e){let i=!this._anyControlsDirty(),a=this.pristine!==i;this.pristine=i,n.onlySelf||this._parent?._updatePristine(n,e),a&&this._events.next(new Fl(this.pristine,e))}_updateTouched(n={},e){this.touched=this._anyControlsTouched(),this._events.next(new Ll(this.touched,e)),n.onlySelf||this._parent?._updateTouched(n,e)}_onDisabledChange=[];_registerOnCollectionChange(n){this._onCollectionChange=n}_setUpdateStrategy(n){Zu(n)&&n.updateOn!=null&&(this._updateOn=n.updateOn)}_parentMarkedDirty(n){return!n&&!!this._parent?.dirty&&!this._parent._anyControlsDirty()}_find(n){return null}_assignValidators(n){this._rawValidators=Array.isArray(n)?n.slice():n,this._composedValidatorFn=ZA(this._rawValidators),this._updateHasRequiredValidator()}_assignAsyncValidators(n){this._rawAsyncValidators=Array.isArray(n)?n.slice():n,this._composedAsyncValidatorFn=JA(this._rawAsyncValidators)}_updateHasRequiredValidator(){it(()=>this._hasRequired.set(this.hasValidator(Lo.required)))}};function AS(t,n){return Object.hasOwn(t,n)}function nR(t){return t.tagName===`INPUT`||t.tagName===`SELECT`||t.tagName===`TEXTAREA`}function iR(t,n,e,i){switch(e){case`name`:t.setAttribute(n,e,i);break;case`disabled`:case`readonly`:case`required`:i?t.setAttribute(n,e,``):t.removeAttribute(n,e);break;case`max`:case`min`:case`minLength`:case`maxLength`:i!==void 0?t.setAttribute(n,e,i.toString()):t.removeAttribute(n,e)}}var eb=class{kind;context;control;message;constructor({kind:n,context:e,control:i}){this.kind=n,this.context=e,this.control=i}};var aR=(()=>{class t{_validator=Hu;_onChange;_enabled;ngOnChanges(e){if(this.inputName in e){let i=this.normalizeInput(e[this.inputName].currentValue);this._enabled=this.enabled(i),this._validator=this._enabled?this.createValidator(i):Hu,this._onChange?.()}}validate(e){return this._validator(e)}registerOnValidatorChange(e){this._onChange=e}enabled(e){return e!=null}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,features:[_t]})}return t})();var oR={provide:Qr,useExisting:Ht(()=>RS),multi:!0};var RS=(()=>{class t extends aR{required;inputName=`required`;normalizeInput=z;createValidator=e=>yS;enabled(e){return e}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`required`,``,`formControlName`,``,3,`type`,`checkbox`],[``,`required`,``,`formControl`,``,3,`type`,`checkbox`],[``,`required`,``,`ngModel`,``,3,`type`,`checkbox`]],hostVars:1,hostBindings:function(i,a){i&2&&ne(`required`,a._enabled?``:null)},inputs:{required:`required`},standalone:!1,features:[Ge([oR]),Be]})}return t})();var rR=new N(``);var Ju=new N(``,{factory:()=>sb});var sb=`always`;function sR(t,n){return[...n.path,t]}function hS(t,n,e=sb){lb(t,n),n.valueAccessor.writeValue(t.value),(t.disabled||e===`always`)&&n.valueAccessor.setDisabledState?.(t.disabled),cR(t,n),uR(t,n),dR(t,n),lR(t,n)}function fS(t,n,e=!0){let i=()=>{};n?.valueAccessor?.registerOnChange(i),n?.valueAccessor?.registerOnTouched(i),Ku(t,n),t&&(n._invokeOnDestroyCallbacks(),t._registerOnCollectionChange(()=>{}))}function Yu(t,n){t.forEach(e=>{e.registerOnValidatorChange&&e.registerOnValidatorChange(n)})}function lR(t,n){if(n.valueAccessor.setDisabledState){let e=i=>{n.valueAccessor.setDisabledState(i)};t.registerOnDisabledChange(e),n._registerOnDestroy(()=>{t._unregisterOnDisabledChange(e)})}}function lb(t,n){let e=kS(t);n.validator!==null?t.setValidators(uS(e,n.validator)):typeof e==`function`&&t.setValidators([e]);let i=MS(t);n.asyncValidator!==null?t.setAsyncValidators(uS(i,n.asyncValidator)):typeof i==`function`&&t.setAsyncValidators([i]);let a=()=>t.updateValueAndValidity();Yu(n._rawValidators,a),Yu(n._rawAsyncValidators,a)}function Ku(t,n){let e=!1;if(t!==null){if(n.validator!==null){let a=kS(t);if(Array.isArray(a)&&a.length>0){let o=a.filter(r=>r!==n.validator);o.length!==a.length&&(e=!0,t.setValidators(o))}}if(n.asyncValidator!==null){let a=MS(t);if(Array.isArray(a)&&a.length>0){let o=a.filter(r=>r!==n.asyncValidator);o.length!==a.length&&(e=!0,t.setAsyncValidators(o))}}}let i=()=>{};return Yu(n._rawValidators,i),Yu(n._rawAsyncValidators,i),e}function cR(t,n){n.valueAccessor.registerOnChange(e=>{t._pendingValue=e,t._pendingChange=!0,t._pendingDirty=!0,t.updateOn===`change`&&OS(t,n)})}function dR(t,n){n.valueAccessor.registerOnTouched(()=>{t._pendingTouched=!0,t.updateOn===`blur`&&t._pendingChange&&OS(t,n),t.updateOn!==`submit`&&t.markAsTouched()})}function OS(t,n){t._pendingDirty&&t.markAsDirty(),t.setValue(t._pendingValue,{emitModelToViewChange:!1}),n.viewToModelUpdate(t._pendingValue),t._pendingChange=!1}function uR(t,n){let e=(i,a)=>{n.valueAccessor.writeValue(i),a&&n.viewToModelUpdate(i)};t.registerOnChange(e),n._registerOnDestroy(()=>{t._unregisterOnChange(e)})}function PS(t,n){lb(t,n)}function mR(t,n){return Ku(t,n)}function pR(t,n){if(!Object.hasOwn(t,`model`))return!1;let e=t.model;return e.isFirstChange()?!0:!Object.is(n,e.currentValue)}function hR(t){return Object.getPrototypeOf(t.constructor)===BA}function FS(t,n){t._syncPendingControls(),n.forEach(e=>{let i=e.control;i.updateOn===`submit`&&i._pendingChange&&(e.viewToModelUpdate(i._pendingValue),i._pendingChange=!1)})}function fR(t,n){if(!n)return null;let e,i,a;return n.forEach(o=>{o.constructor===Xu?e=o:hR(o)?i=o:a=o}),a||i||e||null}function gR(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}var bR={provide:rR,useFactory:()=>{let t=f(Mi,{self:!0});return{setParseErrors:n=>{t.setParseErrorSource(n)},set onReset(n){t.onReset=n}}}};var Mi=class extends $u{_parent=null;name=null;valueAccessor=null;isCustomControlBased=!1;userOnReset;resetSubscription;set onReset(n){this.userOnReset=n,this.resetSubscription?.unsubscribe(),this.resetSubscription=void 0,this.control&&(this.resetSubscription=this.control.events.subscribe(e=>{e instanceof Yr&&this.control&&this.userOnReset?.(this.control.value)}),this.subscription?.add(this.resetSubscription))}isNativeFormElement=!1;rawValueAccessors;_selectedValueAccessor=null;get selectedValueAccessor(){return this._selectedValueAccessor??=fR(this,this.rawValueAccessors)}parseErrorsValidator=null;renderer;injector;requiredValidatorViaDi;subscription;customControlBindings=null;constructor(n,e,i){super(),this.injector=n,this.renderer=e,this.rawValueAccessors=i,this.injector?.get(Ae)?.onDestroy(()=>{this.removeParseErrorsValidator(this.control),this.subscription?.unsubscribe()})}setupCustomControl(){this.subscription?.unsubscribe();let n=this.injector?.get(Le);if(!this.control||!n)return;let e=n.markForCheck.bind(n);this.subscription=new De,this.subscription.add(this.control.valueChanges.subscribe(e)),this.subscription.add(this.control.statusChanges.subscribe(e)),this.resetSubscription?.unsubscribe(),this.resetSubscription=void 0,this.userOnReset&&(this.resetSubscription=this.control.events.subscribe(i=>{i instanceof Yr&&this.control&&this.userOnReset?.(this.control.value)}),this.subscription.add(this.resetSubscription)),this.parseErrorsValidator&&this.control.addValidators(this.parseErrorsValidator)}ngControlCreate(n){!n.nativeElement.hasAttribute?.(`ngNoCva`)&&(this.rawValueAccessors&&this.rawValueAccessors.length>0||this.valueAccessor!==null)||!n.customControl||(this.isCustomControlBased=!0,n.listenToCustomControlModel(a=>{this.control?.markAsDirty(),this.control?.setValue(a,{emitModelToViewChange:!1}),this.viewToModelUpdate(a)}),n.listenToCustomControlOutput(`touch`,()=>{this.control?.markAsTouched()}),this.customControlBindings={},this.isNativeFormElement=nR(n.nativeElement),this.requiredValidatorViaDi=this._rawValidators.find(a=>a instanceof RS))}ngControlUpdate(n,e){if(!this.isCustomControlBased)return;let i=this.control,a=this.customControlBindings;Object.is(a.value,i.value)||(a.value=i.value,n.setCustomControlModelInput(i.value)),this.bindControlProperty(n,a,`touched`,i.touched),this.bindControlProperty(n,a,`dirty`,i.dirty),this.bindControlProperty(n,a,`valid`,i.valid),this.bindControlProperty(n,a,`invalid`,i.invalid),this.bindControlProperty(n,a,`pending`,i.pending),this.bindControlProperty(n,a,`disabled`,i.disabled),this.shouldBindRequired&&this.bindControlProperty(n,a,`required`,this.isRequired);let o=i.errors;if(a.errors!==o){a.errors=o;let r=this._convertErrors(o);n.setInputOnDirectives(`errors`,r)}}get isRequired(){return(this.requiredValidatorViaDi?._enabled||this.control?._hasRequired())??!1}get shouldBindRequired(){return!0}bindControlProperty(n,e,i,a){if(e[i]===a)return;e[i]=a;let o=n.setInputOnDirectives(i,a);this.isNativeFormElement&&!o&&(i===`disabled`||i===`required`)&&this.renderer&&iR(this.renderer,n.nativeElement,i,a)}_convertErrors(n){if(n===null)return[];let e=this.control;return Object.entries(n).map(([i,a])=>new eb({context:a,kind:i,control:e}))}setParseErrorSource(n){if(n===void 0)return;let e=null,i=K(()=>{let a=n();return a.length===0?null:a.reduce((o,r)=>(o[r.kind]=r,o),{})});this.parseErrorsValidator=(()=>e).bind(this),vt(()=>{e=i(),this.control?.updateValueAndValidity({emitEvent:!1})},{injector:this.injector})}removeParseErrorsValidator(n){this.parseErrorsValidator&&(n?.removeValidators(this.parseErrorsValidator),n?.updateValueAndValidity({emitEvent:!1}))}};var tb=class{_cd;constructor(n){this._cd=n}get isTouched(){return this._cd?.control?._touched?.(),!!this._cd?.control?.touched}get isUntouched(){return!!this._cd?.control?.untouched}get isPristine(){return this._cd?.control?._pristine?.(),!!this._cd?.control?.pristine}get isDirty(){return!!this._cd?.control?.dirty}get isValid(){return this._cd?.control?._status?.(),!!this._cd?.control?.valid}get isInvalid(){return!!this._cd?.control?.invalid}get isPending(){return!!this._cd?.control?.pending}get isSubmitted(){return this._cd?._submitted?.(),!!this._cd?.submitted}};var LS=(()=>{class t extends tb{constructor(e){super(e)}static ɵfac=function(i){return new(i||t)(He(Mi,2))};static ɵdir=Q({type:t,selectors:[[``,`formControlName`,``],[``,`ngModel`,``],[``,`formControl`,``]],hostVars:14,hostBindings:function(i,a){i&2&&G(`ng-untouched`,a.isUntouched)(`ng-touched`,a.isTouched)(`ng-pristine`,a.isPristine)(`ng-dirty`,a.isDirty)(`ng-valid`,a.isValid)(`ng-invalid`,a.isInvalid)(`ng-pending`,a.isPending)},standalone:!1,features:[Be]})}return t})();var Qu=class extends qu{constructor(n,e,i){super(TS(e),NS(i,e)),this.controls=n,this._initObservables(),this._setUpdateStrategy(e),this._setUpControls(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator})}controls;registerControl(n,e){return this._find(n)||(this.controls[n]=e,e.setParent(this),e._registerOnCollectionChange(this._onCollectionChange),e)}addControl(n,e,i={}){this.registerControl(n,e),this.updateValueAndValidity({emitEvent:i.emitEvent}),this._onCollectionChange()}removeControl(n,e={}){let i=this._find(n);i&&i._registerOnCollectionChange(()=>{}),delete this.controls[n],this.updateValueAndValidity({emitEvent:e.emitEvent}),this._onCollectionChange()}setControl(n,e,i={}){let a=this._find(n);a&&a._registerOnCollectionChange(()=>{}),delete this.controls[n],e&&this.registerControl(n,e),this.updateValueAndValidity({emitEvent:i.emitEvent}),this._onCollectionChange()}contains(n){return this._find(n)?.enabled===!0}setValue(n,e={}){it(()=>{tR(this,!0,n),Object.keys(n).forEach(i=>{eR(this,!0,i),this.controls[i].setValue(n[i],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e)})}patchValue(n,e={}){n!=null&&(Object.keys(n).forEach(i=>{let a=this._find(i);a&&a.patchValue(n[i],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e))}reset(n={},e={}){this._forEachChild((i,a)=>{i.reset(n?n[a]:null,s(r({},e),{onlySelf:!0}))}),this._updatePristine(e,this),this._updateTouched(e,this),this.updateValueAndValidity(e),e?.emitEvent!==!1&&this._events.next(new Yr(this))}getRawValue(){return this._reduceChildren({},(n,e,i)=>(n[i]=e.getRawValue(),n))}_syncPendingControls(){let n=this._reduceChildren(!1,(e,i)=>i._syncPendingControls()?!0:e);return n&&this.updateValueAndValidity({onlySelf:!0}),n}_forEachChild(n){Object.keys(this.controls).forEach(e=>{let i=this.controls[e];i&&n(i,e)})}_setUpControls(){this._forEachChild(n=>{n.setParent(this),n._registerOnCollectionChange(this._onCollectionChange)})}_updateValue(){this.value=this._reduceValue()}_anyControls(n){for(let[e,i]of Object.entries(this.controls))if(this.contains(e)&&n(i))return!0;return!1}_reduceValue(){return this._reduceChildren({},(e,i,a)=>((i.enabled||this.disabled)&&(e[a]=i.value),e))}_reduceChildren(n,e){let i=n;return this._forEachChild((a,o)=>{i=e(i,a,o)}),i}_allControlsDisabled(){for(let n of Object.keys(this.controls))if(this.controls[n].enabled)return!1;return Object.keys(this.controls).length>0||this.disabled}_find(n){return AS(this.controls,n)?this.controls[n]:null}};var vR={provide:Vo,useExisting:Ht(()=>Vl)};var Pl=Promise.resolve();var Vl=(()=>{class t extends Vo{callSetDisabledState;get submitted(){return it(this.submittedReactive)}_submitted=K(()=>this.submittedReactive());submittedReactive=T(!1);_directives=new Set;form;ngSubmit=new te;options;constructor(e,i,a){super(),this.callSetDisabledState=a,this.form=new Qu({},ob(e),rb(i))}ngAfterViewInit(){this._setUpdateStrategy()}get formDirective(){return this}get control(){return this.form}get path(){return[]}get controls(){return this.form.controls}addControl(e){Pl.then(()=>{e.control=this._findContainer(e.path).registerControl(e.name,e.control),e._setupWithForm(this.callSetDisabledState),e.control.updateValueAndValidity({emitEvent:!1}),this._directives.add(e)})}getControl(e){return this.form.get(e.path)}removeControl(e){Pl.then(()=>{this._findContainer(e.path)?.removeControl(e.name),this._directives.delete(e)})}addFormGroup(e){Pl.then(()=>{let i=this._findContainer(e.path),a=new Qu({});PS(a,e),i.registerControl(e.name,a),a.updateValueAndValidity({emitEvent:!1})})}removeFormGroup(e){Pl.then(()=>{this._findContainer(e.path)?.removeControl?.(e.name)})}getFormGroup(e){return this.form.get(e.path)}updateModel(e,i){Pl.then(()=>{this.form.get(e.path).setValue(i)})}setValue(e){this.control.setValue(e)}onSubmit(e){return this.submittedReactive.set(!0),FS(this.form,this._directives),this.ngSubmit.emit(e),this.form._events.next(new Wu(this.control)),e?.target?.method===`dialog`}onReset(){this.resetForm()}resetForm(e=void 0){this.form.reset(e),this.submittedReactive.set(!1)}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.form._updateOn=this.options.updateOn)}_findContainer(e){return e.pop(),e.length?this.form.get(e):this.form}static ɵfac=function(i){return new(i||t)(He(Qr,10),He(ab,10),He(Ju,8))};static ɵdir=Q({type:t,selectors:[[`form`,3,`ngNoForm`,``,3,`formGroup`,``,3,`formArray`,``],[`ng-form`],[``,`ngForm`,``]],hostBindings:function(i,a){i&1&&C(`submit`,function(r){return a.onSubmit(r)})(`reset`,function(){return a.onReset()})},inputs:{options:[0,`ngFormOptions`,`options`]},outputs:{ngSubmit:`ngSubmit`},exportAs:[`ngForm`],standalone:!1,features:[Ge([vR]),Be]})}return t})();function gS(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}function bS(t){return typeof t==`object`&&t!==null&&Object.keys(t).length===2&&`value`in t&&`disabled`in t}var VS=class extends qu{defaultValue=null;_onChange=[];_pendingValue;_pendingChange=!1;constructor(n=null,e,i){super(TS(e),NS(i,e)),this._applyFormState(n),this._setUpdateStrategy(e),this._initObservables(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator}),Zu(e)&&(e.nonNullable||e.initialValueIsDefault)&&(bS(n)?this.defaultValue=n.value:this.defaultValue=n)}setValue(n,e={}){it(()=>{this.value=this._pendingValue=n,this._onChange.length&&e.emitModelToViewChange!==!1&&this._onChange.forEach(i=>i(this.value,e.emitViewToModelChange!==!1)),this.updateValueAndValidity(e)})}patchValue(n,e={}){this.setValue(n,e)}reset(n=this.defaultValue,e={}){this._applyFormState(n),this.markAsPristine(e),this.markAsUntouched(e),this.setValue(this.value,e),e.overwriteDefaultValue&&(this.defaultValue=this.value),this._pendingChange=!1,e?.emitEvent!==!1&&this._events.next(new Yr(this))}_updateValue(){}_anyControls(n){return!1}_allControlsDisabled(){return this.disabled}registerOnChange(n){this._onChange.push(n)}_unregisterOnChange(n){gS(this._onChange,n)}registerOnDisabledChange(n){this._onDisabledChange.push(n)}_unregisterOnDisabledChange(n){gS(this._onDisabledChange,n)}_forEachChild(n){}_syncPendingControls(){return this.updateOn===`submit`&&(this._pendingDirty&&this.markAsDirty(),this._pendingTouched&&this.markAsTouched(),this._pendingChange)?(this.setValue(this._pendingValue,{onlySelf:!0,emitModelToViewChange:!1}),!0):!1}_applyFormState(n){bS(n)?(this.value=this._pendingValue=n.value,n.disabled?this.disable({onlySelf:!0,emitEvent:!1}):this.enable({onlySelf:!0,emitEvent:!1})):this.value=this._pendingValue=n}};var _R=t=>t instanceof VS;var yR=(()=>{class t extends Vo{callSetDisabledState;get submitted(){return it(this._submittedReactive)}set submitted(e){this._submittedReactive.set(e)}_submitted=K(()=>this._submittedReactive());_submittedReactive=T(!1);_oldForm;_onCollectionChange=()=>this._updateDomValue();directives=[];constructor(e,i,a){super(),this.callSetDisabledState=a,this._setValidators(e),this._setAsyncValidators(i)}ngOnChanges(e){this.onChanges(e)}ngOnDestroy(){this.onDestroy()}onChanges(e){this._checkFormPresent(),Object.hasOwn(e,`form`)&&(this._updateValidators(),this._updateDomValue(),this._updateRegistrations(),this._oldForm=this.form)}onDestroy(){this.form&&(Ku(this.form,this),this.form._onCollectionChange===this._onCollectionChange&&this.form._registerOnCollectionChange(()=>{}))}get formDirective(){return this}get path(){return[]}addControl(e){let i=this.form.get(e.path);return e._setupWithForm(i,this.callSetDisabledState),i.updateValueAndValidity({emitEvent:!1}),this.directives.push(e),i}getControl(e){return this.form.get(e.path)}removeControl(e){fS(e.control||null,e,!1),gR(this.directives,e)}addFormGroup(e){this._setUpFormContainer(e)}removeFormGroup(e){this._cleanUpFormContainer(e)}getFormGroup(e){return this.form.get(e.path)}getFormArray(e){return this.form.get(e.path)}addFormArray(e){this._setUpFormContainer(e)}removeFormArray(e){this._cleanUpFormContainer(e)}updateModel(e,i){this.form.get(e.path).setValue(i)}onReset(){this.resetForm()}resetForm(e=void 0,i={}){this.form.reset(e,i),this._submittedReactive.set(!1)}onSubmit(e){return this.submitted=!0,FS(this.form,this.directives),this.ngSubmit.emit(e),this.form._events.next(new Wu(this.control)),e?.target?.method===`dialog`}_updateDomValue(){this.directives.forEach(e=>{let i=e.control,a=this.form.get(e.path);i!==a&&(fS(i||null,e),_R(a)&&e._setupWithForm(a,this.callSetDisabledState))}),this.form._updateTreeValidity({emitEvent:!1})}_setUpFormContainer(e){let i=this.form.get(e.path);PS(i,e),i.updateValueAndValidity({emitEvent:!1})}_cleanUpFormContainer(e){let i=this.form?.get(e.path);i&&mR(i,e)&&i.updateValueAndValidity({emitEvent:!1})}_updateRegistrations(){this.form._registerOnCollectionChange(this._onCollectionChange),this._oldForm?._registerOnCollectionChange(()=>{})}_updateValidators(){lb(this.form,this),this._oldForm&&Ku(this._oldForm,this)}_checkFormPresent(){this.form}static ɵfac=function(i){return new(i||t)(He(Qr,10),He(ab,10),He(Ju,8))};static ɵdir=Q({type:t,features:[Be,_t]})}return t})();var xR={provide:Vo,useExisting:Ht(()=>Bl)};var Bl=(()=>{class t extends yR{form=null;ngSubmit=new te;get control(){return this.form}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`formGroup`,``]],hostBindings:function(i,a){i&1&&C(`submit`,function(r){return a.onSubmit(r)})(`reset`,function(){return a.onReset()})},inputs:{form:[0,`formGroup`,`form`]},outputs:{ngSubmit:`ngSubmit`},exportAs:[`ngForm`],standalone:!1,features:[Ge([xR]),Be]})}return t})();var CR={provide:Mi,useExisting:Ht(()=>cb)};var vS=Promise.resolve();var cb=(()=>{class t extends Mi{_changeDetectorRef;callSetDisabledState;control=new VS;static ngAcceptInputType_isDisabled;_registered=!1;_ngModelInjector;viewModel;name=``;isDisabled;model;options;update=new te;constructor(e,i,a,o,r,s,l,h){super(l,h,o),this._changeDetectorRef=r,this.callSetDisabledState=s,this._parent=e,this._setValidators(i),this._setAsyncValidators(a)}ngOnChanges(e){if(this._registered,this._checkForErrors(),!this._registered||`name`in e){if(this._registered&&(this._checkName(),this.formDirective)){let i=e.name.previousValue;this.formDirective.removeControl({name:i,path:this._getPath(i)})}this._setUpControl()}`isDisabled`in e&&this._updateDisabled(e),pR(e,this.viewModel)&&(this._updateValue(this.model),this.viewModel=this.model)}ngOnDestroy(){this.formDirective?.removeControl(this)}ɵngControlCreate(e){super.ngControlCreate(e)}ɵngControlUpdate(e){super.ngControlUpdate(e,!1)}get shouldBindRequired(){return!1}get path(){return this._getPath(this.name)}get formDirective(){return this._parent?this._parent.formDirective:null}viewToModelUpdate(e){this.viewModel=e,this.update.emit(e)}_setUpControl(){this._setUpdateStrategy(),this._isStandalone()?this._setUpStandalone():this.formDirective.addControl(this),this._registered=!0}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.control._updateOn=this.options.updateOn)}_isStandalone(){return!this._parent||!!(this.options&&this.options.standalone)}_setUpStandalone(){this.isCustomControlBased?this.setupCustomControl():(this.valueAccessor??=this.selectedValueAccessor,hS(this.control,this,this.callSetDisabledState)),this.control.updateValueAndValidity({emitEvent:!1})}_setupWithForm(e){this.isCustomControlBased?this.setupCustomControl():(this.valueAccessor??=this.selectedValueAccessor,hS(this.control,this,e))}_checkForErrors(){this._checkName()}_checkName(){this.options&&this.options.name&&(this.name=this.options.name),!this._isStandalone()&&this.name}_updateValue(e){vS.then(()=>{this.control.setValue(e,{emitViewToModelChange:!1}),this._changeDetectorRef?.markForCheck()})}_updateDisabled(e){let i=e.isDisabled.currentValue,a=i!==0&&z(i);vS.then(()=>{a&&!this.control.disabled?this.control.disable():!a&&this.control.disabled&&this.control.enable(),this._changeDetectorRef?.markForCheck()})}_getPath(e){return this._parent?sR(e,this._parent):[e]}static ɵfac=function(i){return new(i||t)(He(Vo,9),He(Qr,10),He(ab,10),He(Kr,10),He(Le,8),He(Ju,8),He(se,8),He(Pe,8))};static ɵdir=Q({type:t,selectors:[[``,`ngModel`,``,3,`formControlName`,``,3,`formControl`,``]],inputs:{name:`name`,isDisabled:[0,`disabled`,`isDisabled`],model:[0,`ngModel`,`model`],options:[0,`ngModelOptions`,`options`]},outputs:{update:`ngModelChange`},exportAs:[`ngModel`],standalone:!1,features:[Ge([CR,bR]),Be,_t,Zf(null)]})}return t})();var SR=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({})}return t})();var BS=(()=>{class t{static withConfig(e){return{ngModule:t,providers:[{provide:Ju,useValue:e.callSetDisabledState??sb}]}}static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[SR]})}return t})();var zS=Symbol(`FIELD_TREE`);function jl(t){return typeof t==`function`&&t[zS]===!0}var em=new N(``);var Xr=(()=>{class t{isErrorState(e,i){return!!(e&&e.invalid&&(e.touched||i&&i.submitted))}isSignalErrorState(e){if(!e)return!1;let i=e().invalid(),a=e().touched();return i&&a}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Zr=class{_defaultMatcher;_parentFormGroup;_parentForm;_stateChanges;errorState=!1;matcher;ngControl;formField;constructor(n,e,i,a,o){this._defaultMatcher=n,this._parentFormGroup=i,this._parentForm=a,this._stateChanges=o,e?cn(e.field)&&!e.updateValueAndValidity?(this.formField=e,this.ngControl=null):(this.formField=null,this.ngControl=e):this.ngControl=this.formField=null}updateErrorState(){let n=this.errorState,e=this._getCurrentErrorState(this.matcher||this._defaultMatcher);e!==n&&(this.errorState=e,this._stateChanges.next())}_getCurrentErrorState(n){if(this.formField&&n?.isSignalErrorState)return n.isSignalErrorState(this.formField.field())??!1;let e=this._parentFormGroup||this._parentForm,i=this.ngControl?this.ngControl.control:null;return n?.isErrorState(i,e)??!1}};var db=class{_box;_destroyed=new V;_resizeSubject=new V;_resizeObserver;_elementObservables=new Map;constructor(n){this._box=n,typeof ResizeObserver<`u`&&(this._resizeObserver=new ResizeObserver(e=>this._resizeSubject.next(e)))}observe(n){return this._elementObservables.has(n)||this._elementObservables.set(n,new Ce(e=>{let i=this._resizeSubject.subscribe(e);return this._resizeObserver?.observe(n,{box:this._box}),()=>{this._resizeObserver?.unobserve(n),i.unsubscribe(),this._elementObservables.delete(n)}}).pipe(Qe(e=>e.some(i=>i.target===n)),Hc({bufferSize:1,refCount:!0}),Ne(this._destroyed))),this._elementObservables.get(n)}destroy(){this._destroyed.next(),this._destroyed.complete(),this._resizeSubject.complete(),this._elementObservables.clear()}};var tm=(()=>{class t{_cleanupErrorListener;_observers=new Map;_ngZone=f(Z);constructor(){}ngOnDestroy(){for(let[,e]of this._observers)e.destroy();this._observers.clear(),this._cleanupErrorListener?.()}observe(e,i){let a=i?.box||`content-box`;return this._observers.has(a)||this._observers.set(a,new db(a)),this._observers.get(a).observe(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var na=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-label`]]})}return t})();var ER=new N(`MatError`);var ub=(()=>{class t{align=`start`;id=f(je).getId(`mat-mdc-hint-`);static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-hint`]],hostAttrs:[1,`mat-mdc-form-field-hint`,`mat-mdc-form-field-bottom-align`],hostVars:4,hostBindings:function(i,a){i&2&&(qt(`id`,a.id),ne(`align`,null),G(`mat-mdc-form-field-hint-end`,a.align===`end`))},inputs:{align:`align`,id:`id`}})}return t})();var DR=new N(`MatPrefix`);var IR=new N(`MatSuffix`);var KS=new N(`FloatingLabelParent`);var US=(()=>{class t{_elementRef=f(Y);get floating(){return this._floating}set floating(e){this._floating=e,this.monitorResize&&this._handleResize()}_floating=!1;get monitorResize(){return this._monitorResize}set monitorResize(e){this._monitorResize=e,this._monitorResize?this._subscribeToResize():this._resizeSubscription.unsubscribe()}_monitorResize=!1;_resizeObserver=f(tm);_ngZone=f(Z);_parent=f(KS);_resizeSubscription=new De;ngOnDestroy(){this._resizeSubscription.unsubscribe()}getWidth(){return kR(this._elementRef.nativeElement)}get element(){return this._elementRef.nativeElement}_handleResize(){setTimeout(()=>this._parent._handleLabelResized())}_subscribeToResize(){this._resizeSubscription.unsubscribe(),this._ngZone.runOutsideAngular(()=>{this._resizeSubscription=this._resizeObserver.observe(this._elementRef.nativeElement,{box:`border-box`}).subscribe(()=>this._handleResize())})}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`label`,`matFormFieldFloatingLabel`,``]],hostAttrs:[1,`mdc-floating-label`,`mat-mdc-floating-label`],hostVars:2,hostBindings:function(i,a){i&2&&G(`mdc-floating-label--float-above`,a.floating)},inputs:{floating:`floating`,monitorResize:`monitorResize`}})}return t})();function kR(t){let n=t;if(n.offsetParent!==null)return n.scrollWidth;let e=n.cloneNode(!0);e.style.setProperty(`position`,`absolute`),e.style.setProperty(`transform`,`translate(-9999px, -9999px)`),document.documentElement.appendChild(e);let i=e.scrollWidth;return e.remove(),i}var $S=`mdc-line-ripple--active`;var nm=`mdc-line-ripple--deactivating`;var GS=(()=>{class t{_elementRef=f(Y);_cleanupTransitionEnd;constructor(){let e=f(Z),i=f(Pe);e.runOutsideAngular(()=>{this._cleanupTransitionEnd=i.listen(this._elementRef.nativeElement,`transitionend`,this._handleTransitionEnd)})}activate(){let e=this._elementRef.nativeElement.classList;e.remove(nm),e.add($S)}deactivate(){this._elementRef.nativeElement.classList.add(nm)}_handleTransitionEnd=e=>{let i=this._elementRef.nativeElement.classList,a=i.contains(nm);e.propertyName===`opacity`&&a&&i.remove($S,nm)};ngOnDestroy(){this._cleanupTransitionEnd()}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`div`,`matFormFieldLineRipple`,``]],hostAttrs:[1,`mdc-line-ripple`]})}return t})();var WS=(()=>{class t{_elementRef=f(Y);_ngZone=f(Z);open=!1;_notch;ngAfterViewInit(){let e=this._elementRef.nativeElement,i=e.querySelector(`.mdc-floating-label`);i?(e.classList.add(`mdc-notched-outline--upgraded`),typeof requestAnimationFrame==`function`&&(i.style.transitionDuration=`0s`,this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>i.style.transitionDuration=``)}))):e.classList.add(`mdc-notched-outline--no-label`)}_setNotchWidth(e){let i=this._notch.nativeElement;!this.open||!e?i.style.width=``:i.style.width=`calc(${e}px * var(--mat-mdc-form-field-floating-label-scale, 0.75) + 9px)`}_setMaxWidth(e){this._notch.nativeElement.style.setProperty(`--mat-form-field-notch-max-width`,`calc(100% - ${e}px)`)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`notch`];return H({type:t,selectors:[[`div`,`matFormFieldNotchedOutline`,``]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;J(s=ee())&&(r._notch=s.first)}},hostAttrs:[1,`mdc-notched-outline`],hostVars:2,hostBindings:function(o,r){o&2&&G(`mdc-notched-outline--notched`,r.open)},inputs:{open:[0,`matFormFieldNotchedOutlineOpen`,`open`]},ngContentSelectors:[`*`],decls:5,vars:0,consts:[[`notch`,``],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__leading`],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__notch`],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__trailing`]],template:function(o,r){o&1&&(Fe(),Wt(0,`div`,1),ct(1,`div`,2,0),he(3),ht(),Wt(4,`div`,3))},encapsulation:2})})()}return t})();var zl=(()=>{class t{id;ngField=null;ngControl=null;focused=!1;empty=!1;shouldLabelFloat=!1;required=!1;disabled=!1;errorState=!1;controlType;autofilled;userAriaDescribedBy;disableAutomaticLabeling;describedByIds;stateChanges=null;value;static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t})}return t})();var Hl=new N(`MatFormField`);var MR=new N(`MAT_FORM_FIELD_DEFAULT_OPTIONS`);var qS=`fill`;var TR=`auto`;var YS=`fixed`;var NR=`translateY(-50%)`;var Ti=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Le);_platform=f(Re);_idGenerator=f(je);_ngZone=f(Z);_defaults=f(MR,{optional:!0});_currentDirection;_unwrapMaybeSignal(e){return cn(e)?e():e}_textField;_iconPrefixContainer;_textPrefixContainer;_iconSuffixContainer;_textSuffixContainer;_floatingLabel;_notchedOutline;_lineRipple;_iconPrefixContainerSignal=pl(`iconPrefixContainer`);_textPrefixContainerSignal=pl(`textPrefixContainer`);_iconSuffixContainerSignal=pl(`iconSuffixContainer`);_textSuffixContainerSignal=pl(`textSuffixContainer`);_prefixSuffixContainers=K(()=>[this._iconPrefixContainerSignal(),this._textPrefixContainerSignal(),this._iconSuffixContainerSignal(),this._textSuffixContainerSignal()].map(e=>e?.nativeElement).filter(e=>e!==void 0));_formFieldControl;_prefixChildren;_suffixChildren;_errorChildren;_hintChildren;_labelChild=Sx(na);get hideRequiredMarker(){return this._hideRequiredMarker}set hideRequiredMarker(e){this._hideRequiredMarker=ta(e)}_hideRequiredMarker=!1;color=`primary`;get floatLabel(){return this._floatLabel||this._defaults?.floatLabel||TR}set floatLabel(e){e!==this._floatLabel&&(this._floatLabel=e,this._changeDetectorRef.markForCheck())}_floatLabel;get appearance(){return this._appearanceSignal()}set appearance(e){let i=e||this._defaults?.appearance||qS;this._appearanceSignal.set(i)}_appearanceSignal=T(qS);get subscriptSizing(){return this._subscriptSizing||this._defaults?.subscriptSizing||YS}set subscriptSizing(e){this._subscriptSizing=e||this._defaults?.subscriptSizing||YS}_subscriptSizing=null;get hintLabel(){return this._hintLabel}set hintLabel(e){this._hintLabel=e,this._processHints()}_hintLabel=``;_hasIconPrefix=!1;_hasTextPrefix=!1;_hasIconSuffix=!1;_hasTextSuffix=!1;_labelId=this._idGenerator.getId(`mat-mdc-form-field-label-`);_hintLabelId=this._idGenerator.getId(`mat-mdc-hint-`);_describedByIds;get _control(){return this._explicitFormFieldControl||this._formFieldControl}set _control(e){this._explicitFormFieldControl=e}_destroyed=new V;_isFocused=null;_explicitFormFieldControl;_previousControl=null;_previousControlValidatorFn=null;_stateChanges;_valueChanges;_describedByChanges;_outlineLabelOffsetResizeObserver=null;_animationsDisabled=Ue();constructor(){let e=this._defaults,i=f(gt);e&&(e.appearance&&(this.appearance=e.appearance),this._hideRequiredMarker=!!e?.hideRequiredMarker,e.color&&(this.color=e.color)),vt(()=>this._currentDirection=i.valueSignal()),this._syncOutlineLabelOffset()}ngAfterViewInit(){this._updateFocusState(),this._animationsDisabled||this._ngZone.runOutsideAngular(()=>{setTimeout(()=>{this._elementRef.nativeElement.classList.add(`mat-form-field-animations-enabled`)},300)}),this._changeDetectorRef.detectChanges()}ngAfterContentInit(){this._assertFormFieldControl(),this._initializeSubscript(),this._initializePrefixAndSuffix()}ngAfterContentChecked(){this._assertFormFieldControl(),this._control!==this._previousControl&&(this._initializeControl(this._previousControl),this._control.ngControl&&this._control.ngControl.control&&(this._previousControlValidatorFn=jl(this._control.ngField)?null:this._control.ngControl.control.validator),this._previousControl=this._control,this._changeDetectorRef.markForCheck()),this._control.ngControl&&this._control.ngControl.control&&!jl(this._control.ngField)&&this._control.ngControl.control.validator!==this._previousControlValidatorFn&&this._changeDetectorRef.markForCheck()}ngOnDestroy(){this._outlineLabelOffsetResizeObserver?.disconnect(),this._stateChanges?.unsubscribe(),this._valueChanges?.unsubscribe(),this._describedByChanges?.unsubscribe(),this._destroyed.next(),this._destroyed.complete()}getLabelId=K(()=>this._hasFloatingLabel()?this._labelId:null);getConnectedOverlayOrigin(){return this._textField||this._elementRef}_animateAndLockLabel(){this._hasFloatingLabel()&&(this.floatLabel=`always`)}_initializeControl(e){let i=this._control,a=`mat-mdc-form-field-type-`;e&&this._elementRef.nativeElement.classList.remove(a+e.controlType),i.controlType&&this._elementRef.nativeElement.classList.add(a+i.controlType),this._stateChanges?.unsubscribe(),this._stateChanges=i.stateChanges?.subscribe(()=>{this._updateFocusState(),this._changeDetectorRef.markForCheck()}),this._describedByChanges?.unsubscribe(),this._describedByChanges=i.stateChanges?.pipe(mt([void 0,void 0]),lt(()=>[this._unwrapMaybeSignal(i.errorState),i.userAriaDescribedBy]),zc(),Qe(([[o,r],[s,l]])=>o!==s||r!==l)).subscribe(()=>this._syncDescribedByIds()),this._valueChanges?.unsubscribe(),i.ngControl&&i.ngControl.valueChanges&&!jl(i.ngField)&&(this._valueChanges=i.ngControl.valueChanges.pipe(Ne(this._destroyed)).subscribe(()=>this._changeDetectorRef.markForCheck()))}_checkPrefixAndSuffixTypes(){this._hasIconPrefix=!!this._prefixChildren.find(e=>!e._isText),this._hasTextPrefix=!!this._prefixChildren.find(e=>e._isText),this._hasIconSuffix=!!this._suffixChildren.find(e=>!e._isText),this._hasTextSuffix=!!this._suffixChildren.find(e=>e._isText)}_initializePrefixAndSuffix(){this._checkPrefixAndSuffixTypes(),kt(this._prefixChildren.changes,this._suffixChildren.changes).subscribe(()=>{this._checkPrefixAndSuffixTypes(),this._changeDetectorRef.markForCheck()})}_initializeSubscript(){this._hintChildren.changes.subscribe(()=>{this._processHints(),this._changeDetectorRef.markForCheck()}),this._errorChildren.changes.subscribe(()=>{this._syncDescribedByIds(),this._changeDetectorRef.markForCheck()}),this._validateHints(),this._syncDescribedByIds()}_assertFormFieldControl(){this._control}_updateFocusState(){let e=this._unwrapMaybeSignal(this._control.focused);e&&!this._isFocused?(this._isFocused=!0,this._lineRipple?.activate()):!e&&(this._isFocused||this._isFocused===null)&&(this._isFocused=!1,this._lineRipple?.deactivate()),this._elementRef.nativeElement.classList.toggle(`mat-focused`,e),this._textField?.nativeElement.classList.toggle(`mdc-text-field--focused`,e)}_syncOutlineLabelOffset(){cg({earlyRead:()=>{if(this._appearanceSignal()!==`outline`)return this._outlineLabelOffsetResizeObserver?.disconnect(),null;if(globalThis.ResizeObserver){this._outlineLabelOffsetResizeObserver||=new globalThis.ResizeObserver(()=>{this._writeOutlinedLabelStyles(this._getOutlinedLabelOffset())});for(let e of this._prefixSuffixContainers())this._outlineLabelOffsetResizeObserver.observe(e,{box:`border-box`})}return this._getOutlinedLabelOffset()},write:e=>this._writeOutlinedLabelStyles(e())})}_shouldAlwaysFloat(){return this.floatLabel===`always`}_hasOutline(){return this.appearance===`outline`}_forceDisplayInfixLabel(){return!this._platform.isBrowser&&this._prefixChildren.length&&!this._shouldLabelFloat()}_hasFloatingLabel=K(()=>!!this._labelChild());_shouldLabelFloat(){return this._hasFloatingLabel()?this._shouldAlwaysFloat()||this._unwrapMaybeSignal(this._control.shouldLabelFloat):!1}_shouldForward(e){let i=this._control?.ngField||this._control?.ngControl;if(!i)return!1;if(jl(i)){let a=i();return e===`valid`?a.valid():e===`dirty`?a.dirty():e===`touched`?a.touched():e===`pending`?a.pending():e===`untouched`?!a.touched():e===`pristine`?!a.dirty():e===`invalid`?!a.valid():!1}else{let a=i;return e===`valid`?a.valid:e===`dirty`?a.dirty:e===`touched`?a.touched:e===`pending`?a.pending:e===`untouched`?a.untouched:e===`pristine`?a.pristine:e===`invalid`?a.invalid:!1}}_getSubscriptMessageType(){return this._errorChildren&&this._errorChildren.length>0&&this._unwrapMaybeSignal(this._control.errorState)?`error`:`hint`}_handleLabelResized(){this._refreshOutlineNotchWidth()}_refreshOutlineNotchWidth(){!this._hasOutline()||!this._floatingLabel||!this._shouldLabelFloat()?this._notchedOutline?._setNotchWidth(0):this._notchedOutline?._setNotchWidth(this._floatingLabel.getWidth())}_processHints(){this._validateHints(),this._syncDescribedByIds()}_validateHints(){this._hintChildren}_syncDescribedByIds(){if(this._control){let e=[];if(this._control.userAriaDescribedBy&&typeof this._control.userAriaDescribedBy==`string`&&e.push(...this._control.userAriaDescribedBy.split(` `)),this._getSubscriptMessageType()===`hint`){let o=this._hintChildren?this._hintChildren.find(s=>s.align===`start`):null,r=this._hintChildren?this._hintChildren.find(s=>s.align===`end`):null;o?e.push(o.id):this._hintLabel&&e.push(this._hintLabelId),r&&e.push(r.id)}else this._errorChildren&&e.push(...this._errorChildren.map(o=>o.id));let i=this._control.describedByIds,a;if(i){let o=this._describedByIds||e;a=e.concat(i.filter(r=>r&&!o.includes(r)))}else a=e;this._control.setDescribedByIds(a),this._describedByIds=e}}_getOutlinedLabelOffset(){if(!this._hasOutline()||!this._floatingLabel)return null;if(!this._iconPrefixContainer&&!this._textPrefixContainer)return[``,null];if(!this._isAttachedToDom())return null;let e=this._iconPrefixContainer?.nativeElement,i=this._textPrefixContainer?.nativeElement,a=this._iconSuffixContainer?.nativeElement,o=this._textSuffixContainer?.nativeElement,r=e?.getBoundingClientRect().width??0,s=i?.getBoundingClientRect().width??0,l=a?.getBoundingClientRect().width??0,h=o?.getBoundingClientRect().width??0;return[`var(--mat-mdc-form-field-label-transform, ${NR} translateX(${`calc(${this._currentDirection===`rtl`?`-1`:`1`} * (${`${r+s}px`} + var(--mat-mdc-form-field-label-offset-x, 0px)))`}))`,r+s+l+h]}_writeOutlinedLabelStyles(e){if(e!==null){let[i,a]=e;this._floatingLabel&&(this._floatingLabel.element.style.transform=i),a!==null&&this._notchedOutline?._setMaxWidth(a)}}_isAttachedToDom(){let e=this._elementRef.nativeElement;if(e.getRootNode){let i=e.getRootNode();return i&&i!==e}return document.documentElement.contains(e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`iconPrefixContainer`],i=[`textPrefixContainer`],a=[`iconSuffixContainer`],o=[`textSuffixContainer`],r=[`textField`],s=[`*`,[[`mat-label`]],[[``,`matPrefix`,``],[``,`matIconPrefix`,``]],[[``,`matTextPrefix`,``]],[[``,`matTextSuffix`,``]],[[``,`matSuffix`,``],[``,`matIconSuffix`,``]],[[`mat-error`],[``,`matError`,``]],[[`mat-hint`,3,`align`,`end`]],[[`mat-hint`,`align`,`end`]]],l=[`*`,`mat-label`,`[matPrefix], [matIconPrefix]`,`[matTextPrefix]`,`[matTextSuffix]`,`[matSuffix], [matIconSuffix]`,`mat-error, [matError]`,`mat-hint:not([align='end'])`,`mat-hint[align='end']`];function h(pe,Ie){pe&1&&U(0,`span`,21)}function p(pe,Ie){if(pe&1&&(c(0,`label`,20),he(1,1),E(2,h,1,0,`span`,21),d()),pe&2){let F=v(2);S(`floating`,F._shouldLabelFloat())(`monitorResize`,F._hasOutline())(`id`,F._labelId),ne(`for`,F._control.disableAutomaticLabeling?null:F._control.id),m(2),D(!F.hideRequiredMarker&&F._unwrapMaybeSignal(F._control.required)?2:-1)}}function g(pe,Ie){if(pe&1&&E(0,p,3,5,`label`,20),pe&2)D(v()._hasFloatingLabel()?0:-1)}function _(pe,Ie){pe&1&&U(0,`div`,7)}function b(pe,Ie){}function y(pe,Ie){if(pe&1&&st(0,b,0,0,`ng-template`,13),pe&2){v(2);S(`ngTemplateOutlet`,ft(1))}}function I(pe,Ie){if(pe&1&&(c(0,`div`,9),E(1,y,1,1,null,13),d()),pe&2){let F=v();S(`matFormFieldNotchedOutlineOpen`,F._shouldLabelFloat()),m(),D(F._forceDisplayInfixLabel()?-1:1)}}function P(pe,Ie){pe&1&&(c(0,`div`,10,2),he(2,2),d())}function ie(pe,Ie){pe&1&&(c(0,`div`,11,3),he(2,3),d())}function L(pe,Ie){}function x(pe,Ie){if(pe&1&&st(0,L,0,0,`ng-template`,13),pe&2){v();S(`ngTemplateOutlet`,ft(1))}}function A(pe,Ie){pe&1&&(c(0,`div`,14,4),he(2,4),d())}function R(pe,Ie){pe&1&&(c(0,`div`,15,5),he(2,5),d())}function W(pe,Ie){pe&1&&U(0,`div`,16)}function ye(pe,Ie){pe&1&&(c(0,`div`,18),he(1,6),d())}function bt(pe,Ie){if(pe&1&&(c(0,`mat-hint`,22),u(1),d()),pe&2){let F=v(2);S(`id`,F._hintLabelId),m(),w(F.hintLabel)}}function We(pe,Ie){if(pe&1&&(c(0,`div`,19),E(1,bt,2,2,`mat-hint`,22),he(2,7),U(3,`div`,23),he(4,8),d()),pe&2){let F=v();m(),D(F.hintLabel?1:-1)}}return H({type:t,selectors:[[`mat-form-field`]],contentQueries:function(Ie,F,qe){if(Ie&1&&(Zd(qe,F._labelChild,na,5),jt(qe,zl,5)(qe,DR,5)(qe,IR,5)(qe,ER,5)(qe,ub,5)),Ie&2){eu();let on;J(on=ee())&&(F._formFieldControl=on.first),J(on=ee())&&(F._prefixChildren=on),J(on=ee())&&(F._suffixChildren=on),J(on=ee())&&(F._errorChildren=on),J(on=ee())&&(F._hintChildren=on)}},viewQuery:function(Ie,F){if(Ie&1&&(Jd(F._iconPrefixContainerSignal,e,5)(F._textPrefixContainerSignal,i,5)(F._iconSuffixContainerSignal,a,5)(F._textSuffixContainerSignal,o,5),Ze(r,5)(e,5)(i,5)(a,5)(o,5)(US,5)(WS,5)(GS,5)),Ie&2){eu(4);let qe;J(qe=ee())&&(F._textField=qe.first),J(qe=ee())&&(F._iconPrefixContainer=qe.first),J(qe=ee())&&(F._textPrefixContainer=qe.first),J(qe=ee())&&(F._iconSuffixContainer=qe.first),J(qe=ee())&&(F._textSuffixContainer=qe.first),J(qe=ee())&&(F._floatingLabel=qe.first),J(qe=ee())&&(F._notchedOutline=qe.first),J(qe=ee())&&(F._lineRipple=qe.first)}},hostAttrs:[1,`mat-mdc-form-field`],hostVars:38,hostBindings:function(Ie,F){Ie&2&&G(`mat-mdc-form-field-label-always-float`,F._shouldAlwaysFloat())(`mat-mdc-form-field-has-icon-prefix`,F._hasIconPrefix)(`mat-mdc-form-field-has-icon-suffix`,F._hasIconSuffix)(`mat-form-field-invalid`,F._unwrapMaybeSignal(F._control.errorState))(`mat-form-field-disabled`,F._unwrapMaybeSignal(F._control.disabled))(`mat-form-field-autofilled`,F._unwrapMaybeSignal(F._control.autofilled))(`mat-form-field-appearance-fill`,F.appearance==`fill`)(`mat-form-field-appearance-outline`,F.appearance==`outline`)(`mat-form-field-hide-placeholder`,F._hasFloatingLabel()&&!F._shouldLabelFloat())(`mat-primary`,F.color!==`accent`&&F.color!==`warn`)(`mat-accent`,F.color===`accent`)(`mat-warn`,F.color===`warn`)(`ng-untouched`,F._shouldForward(`untouched`))(`ng-touched`,F._shouldForward(`touched`))(`ng-pristine`,F._shouldForward(`pristine`))(`ng-dirty`,F._shouldForward(`dirty`))(`ng-valid`,F._shouldForward(`valid`))(`ng-invalid`,F._shouldForward(`invalid`))(`ng-pending`,F._shouldForward(`pending`))},inputs:{hideRequiredMarker:`hideRequiredMarker`,color:`color`,floatLabel:`floatLabel`,appearance:`appearance`,subscriptSizing:`subscriptSizing`,hintLabel:`hintLabel`},exportAs:[`matFormField`],features:[Ge([{provide:Hl,useExisting:t},{provide:KS,useExisting:t}])],ngContentSelectors:l,decls:18,vars:21,consts:[[`labelTemplate`,``],[`textField`,``],[`iconPrefixContainer`,``],[`textPrefixContainer`,``],[`textSuffixContainer`,``],[`iconSuffixContainer`,``],[1,`mat-mdc-text-field-wrapper`,`mdc-text-field`,3,`click`],[1,`mat-mdc-form-field-focus-overlay`],[1,`mat-mdc-form-field-flex`],[`matFormFieldNotchedOutline`,``,3,`matFormFieldNotchedOutlineOpen`],[1,`mat-mdc-form-field-icon-prefix`],[1,`mat-mdc-form-field-text-prefix`],[1,`mat-mdc-form-field-infix`],[3,`ngTemplateOutlet`],[1,`mat-mdc-form-field-text-suffix`],[1,`mat-mdc-form-field-icon-suffix`],[`matFormFieldLineRipple`,``],[`aria-atomic`,`true`,`aria-live`,`polite`,1,`mat-mdc-form-field-subscript-wrapper`,`mat-mdc-form-field-bottom-align`],[1,`mat-mdc-form-field-error-wrapper`],[1,`mat-mdc-form-field-hint-wrapper`],[`matFormFieldFloatingLabel`,``,3,`floating`,`monitorResize`,`id`],[`aria-hidden`,`true`,1,`mat-mdc-form-field-required-marker`,`mdc-floating-label--required`],[3,`id`],[1,`mat-mdc-form-field-hint-spacer`]],template:function(Ie,F){if(Ie&1&&(Fe(s),st(0,g,1,1,`ng-template`,null,0,dl),c(2,`div`,6,1),C(`click`,function(on){return F._control.onContainerClick(on)}),E(4,_,1,0,`div`,7),c(5,`div`,8),E(6,I,2,2,`div`,9),E(7,P,3,0,`div`,10),E(8,ie,3,0,`div`,11),c(9,`div`,12),E(10,x,1,1,null,13),he(11),d(),E(12,A,3,0,`div`,14),E(13,R,3,0,`div`,15),d(),E(14,W,1,0,`div`,16),d(),c(15,`div`,17),E(16,ye,2,0,`div`,18)(17,We,5,1,`div`,19),d()),Ie&2){let qe,on=F._unwrapMaybeSignal(F._control.disabled);m(2),G(`mdc-text-field--filled`,!F._hasOutline())(`mdc-text-field--outlined`,F._hasOutline())(`mdc-text-field--no-label`,!F._hasFloatingLabel())(`mdc-text-field--disabled`,on)(`mdc-text-field--invalid`,F._unwrapMaybeSignal(F._control.errorState)),m(2),D(!F._hasOutline()&&!on?4:-1),m(2),D(F._hasOutline()?6:-1),m(),D(F._hasIconPrefix?7:-1),m(),D(F._hasTextPrefix?8:-1),m(2),D(!F._hasOutline()||F._forceDisplayInfixLabel()?10:-1),m(2),D(F._hasTextSuffix?12:-1),m(),D(F._hasIconSuffix?13:-1),m(),D(F._hasOutline()?-1:14),m(),G(`mat-mdc-form-field-subscript-dynamic-size`,F.subscriptSizing===`dynamic`);let uE=F._getSubscriptMessageType();m(),D((qe=uE)===`error`?16:qe===`hint`?17:-1)}},dependencies:[US,WS,_l,GS,ub],styles:[`.mdc-text-field {
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
`],encapsulation:2})})()}return t})();var AR=new N(`mat-chips-default-options`,{providedIn:`root`,factory:()=>({separatorKeyCodes:[13]})});var QS=new N(`MatChipAvatar`);var XS=new N(`MatChipTrailingIcon`);var ZS=new N(`MatChipEdit`);var JS=new N(`MatChipRemove`);var ew=new N(`MatChip`);var tw=(()=>{class t{_elementRef=f(Y);_parentChip=f(ew);_isPrimary=!0;_isLeading=!1;get disabled(){return this._disabled||this._parentChip?.disabled||!1}set disabled(e){this._disabled=e}_disabled=!1;tabIndex=-1;_allowFocusWhenDisabled=!1;_getDisabledAttribute(){return this.disabled&&!this._allowFocusWhenDisabled?``:null}constructor(){f(Je).load(pn),this._elementRef.nativeElement.nodeName===`BUTTON`&&this._elementRef.nativeElement.setAttribute(`type`,`button`)}focus(){this._elementRef.nativeElement.focus()}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`matChipContent`,``]],hostAttrs:[1,`mat-mdc-chip-action`,`mdc-evolution-chip__action`,`mdc-evolution-chip__action--presentational`],hostVars:8,hostBindings:function(i,a){i&2&&(ne(`disabled`,a._getDisabledAttribute())(`aria-disabled`,a.disabled),G(`mdc-evolution-chip__action--primary`,a._isPrimary)(`mdc-evolution-chip__action--secondary`,!a._isPrimary)(`mdc-evolution-chip__action--trailing`,!a._isPrimary&&!a._isLeading))},inputs:{disabled:[2,`disabled`,`disabled`,z],tabIndex:[2,`tabIndex`,`tabIndex`,e=>e==null?-1:dt(e)],_allowFocusWhenDisabled:`_allowFocusWhenDisabled`}})}return t})();var RR=(()=>{class t extends tw{_getTabindex(){return this.disabled&&!this._allowFocusWhenDisabled?null:this.tabIndex.toString()}_handleClick(e){!this.disabled&&this._isPrimary&&(e.preventDefault(),this._parentChip._handlePrimaryActionInteraction())}_handleKeydown(e){(e.keyCode===13||e.keyCode===32)&&!this.disabled&&this._isPrimary&&!this._parentChip._isEditing&&(e.preventDefault(),this._parentChip._handlePrimaryActionInteraction())}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`matChipAction`,``]],hostVars:3,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._handleClick(r)})(`keydown`,function(r){return a._handleKeydown(r)}),i&2&&(ne(`tabindex`,a._getTabindex()),G(`mdc-evolution-chip__action--presentational`,!1))},features:[Be]})}return t})();var Ul=(()=>{class t{_changeDetectorRef=f(Le);_elementRef=f(Y);_tagName=f(yx);_ngZone=f(Z);_focusMonitor=f(tn);_globalRippleOptions=f($r,{optional:!0});_document=f(me);_onFocus=new V;_onBlur=new V;_isBasicChip=!1;role=null;_hasFocusInternal=!1;_pendingFocus=!1;_actionChanges;_animationsDisabled=Ue();_allLeadingIcons;_allTrailingIcons;_allEditIcons;_allRemoveIcons;_hasFocus(){return this._hasFocusInternal}id=f(je).getId(`mat-mdc-chip-`);ariaLabel=null;ariaDescription=null;_chipListDisabled=!1;_hadFocusOnRemove=!1;_textElement;get value(){return this._value!==void 0?this._value:this._textElement.textContent.trim()}set value(e){this._value=e}_value;color;removable=!0;highlighted=!1;disableRipple=!1;get disabled(){return this._disabled||this._chipListDisabled}set disabled(e){this._disabled=e}_disabled=!1;removed=new te;destroyed=new te;basicChipAttrName=`mat-basic-chip`;leadingIcon;editIcon;trailingIcon;removeIcon;primaryAction;_rippleLoader=f(Bu);_injector=f(se);constructor(){let e=f(Je);e.load(pn),e.load(Ii),this._monitorFocus(),this._rippleLoader?.configureRipple(this._elementRef.nativeElement,{className:`mat-mdc-chip-ripple`,disabled:this._isRippleDisabled()})}ngOnInit(){this._isBasicChip=this._elementRef.nativeElement.hasAttribute(this.basicChipAttrName)||this._tagName.toLowerCase()===this.basicChipAttrName}ngAfterViewInit(){this._textElement=this._elementRef.nativeElement.querySelector(`.mat-mdc-chip-action-label`),this._pendingFocus&&(this._pendingFocus=!1,this.focus())}ngAfterContentInit(){this._actionChanges=kt(this._allLeadingIcons.changes,this._allTrailingIcons.changes,this._allEditIcons.changes,this._allRemoveIcons.changes).subscribe(()=>this._changeDetectorRef.markForCheck())}ngDoCheck(){this._rippleLoader.setDisabled(this._elementRef.nativeElement,this._isRippleDisabled())}ngOnDestroy(){this.destroyed.emit({chip:this}),this.destroyed.complete(),this._focusMonitor.stopMonitoring(this._elementRef),this._rippleLoader?.destroyRipple(this._elementRef.nativeElement),this._actionChanges?.unsubscribe()}remove(){this.removable&&(this._hadFocusOnRemove=this._hasFocus(),this.removed.emit({chip:this}))}_isRippleDisabled(){return this.disabled||this.disableRipple||this._animationsDisabled||this._isBasicChip||!this._hasInteractiveActions()||!!this._globalRippleOptions?.disabled}_hasTrailingIcon(){return!!(this.trailingIcon||this.removeIcon)}_handleKeydown(e){(e.keyCode===8&&!e.repeat||e.keyCode===46)&&(e.preventDefault(),this.remove())}focus(){this.disabled||(this.primaryAction?this.primaryAction.focus():this._pendingFocus=!0)}_getSourceAction(e){return this._getActions().find(i=>{let a=i._elementRef.nativeElement;return a===e||a.contains(e)})}_getActions(){let e=[];return this.editIcon&&e.push(this.editIcon),this.primaryAction&&e.push(this.primaryAction),this.removeIcon&&e.push(this.removeIcon),e}_handlePrimaryActionInteraction(){}_hasInteractiveActions(){return this._getActions().length>0}_edit(e){}_monitorFocus(){this._focusMonitor.monitor(this._elementRef,!0).subscribe(e=>{let i=e!==null;i!==this._hasFocusInternal&&(this._hasFocusInternal=i,i?this._onFocus.next({chip:this}):(this._changeDetectorRef.markForCheck(),setTimeout(()=>this._ngZone.run(()=>this._onBlur.next({chip:this})))))})}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[`mat-chip-avatar`],[``,`matChipAvatar`,``]],[[`mat-chip-trailing-icon`],[``,`matChipRemove`,``],[``,`matChipTrailingIcon`,``]]],i=[`*`,`mat-chip-avatar, [matChipAvatar]`,`mat-chip-trailing-icon,[matChipRemove],[matChipTrailingIcon]`];function a(r,s){r&1&&(c(0,`span`,3),he(1,1),d())}function o(r,s){r&1&&(c(0,`span`,6),he(1,2),d())}return H({type:t,selectors:[[`mat-basic-chip`],[``,`mat-basic-chip`,``],[`mat-chip`],[``,`mat-chip`,``]],contentQueries:function(s,l,h){if(s&1&&jt(h,QS,5)(h,ZS,5)(h,XS,5)(h,JS,5)(h,QS,5)(h,XS,5)(h,ZS,5)(h,JS,5),s&2){let p;J(p=ee())&&(l.leadingIcon=p.first),J(p=ee())&&(l.editIcon=p.first),J(p=ee())&&(l.trailingIcon=p.first),J(p=ee())&&(l.removeIcon=p.first),J(p=ee())&&(l._allLeadingIcons=p),J(p=ee())&&(l._allTrailingIcons=p),J(p=ee())&&(l._allEditIcons=p),J(p=ee())&&(l._allRemoveIcons=p)}},viewQuery:function(s,l){if(s&1&&Ze(RR,5),s&2){let h;J(h=ee())&&(l.primaryAction=h.first)}},hostAttrs:[1,`mat-mdc-chip`],hostVars:31,hostBindings:function(s,l){s&1&&C(`keydown`,function(p){return l._handleKeydown(p)}),s&2&&(qt(`id`,l.id),ne(`role`,l.role)(`aria-label`,l.ariaLabel),tt(`mat-`+(l.color||`primary`)),G(`mdc-evolution-chip`,!l._isBasicChip)(`mdc-evolution-chip--disabled`,l.disabled)(`mdc-evolution-chip--with-trailing-action`,l._hasTrailingIcon())(`mdc-evolution-chip--with-primary-graphic`,l.leadingIcon)(`mdc-evolution-chip--with-primary-icon`,l.leadingIcon)(`mdc-evolution-chip--with-avatar`,l.leadingIcon)(`mat-mdc-chip-with-avatar`,l.leadingIcon)(`mat-mdc-chip-highlighted`,l.highlighted)(`mat-mdc-chip-disabled`,l.disabled)(`mat-mdc-basic-chip`,l._isBasicChip)(`mat-mdc-standard-chip`,!l._isBasicChip)(`mat-mdc-chip-with-trailing-icon`,l._hasTrailingIcon())(`_mat-animation-noopable`,l._animationsDisabled))},inputs:{role:`role`,id:`id`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaDescription:[0,`aria-description`,`ariaDescription`],value:`value`,color:`color`,removable:[2,`removable`,`removable`,z],highlighted:[2,`highlighted`,`highlighted`,z],disableRipple:[2,`disableRipple`,`disableRipple`,z],disabled:[2,`disabled`,`disabled`,z]},outputs:{removed:`removed`,destroyed:`destroyed`},exportAs:[`matChip`],features:[Ge([{provide:ew,useExisting:t}])],ngContentSelectors:i,decls:8,vars:2,consts:[[1,`mat-mdc-chip-focus-overlay`],[1,`mdc-evolution-chip__cell`,`mdc-evolution-chip__cell--primary`],[`matChipContent`,``],[1,`mdc-evolution-chip__graphic`,`mat-mdc-chip-graphic`],[1,`mdc-evolution-chip__text-label`,`mat-mdc-chip-action-label`],[1,`mat-mdc-chip-primary-focus-indicator`,`mat-focus-indicator`],[1,`mdc-evolution-chip__cell`,`mdc-evolution-chip__cell--trailing`]],template:function(s,l){s&1&&(Fe(e),U(0,`span`,0),c(1,`span`,1)(2,`span`,2),E(3,a,2,0,`span`,3),c(4,`span`,4),he(5),U(6,`span`,5),d()()(),E(7,o,2,0,`span`,6)),s&2&&(m(3),D(l.leadingIcon?3:-1),m(4),D(l._hasTrailingIcon()?7:-1))},dependencies:[tw],styles:[`.mdc-evolution-chip,
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
`],encapsulation:2})})()}return t})();var im=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Le);_dir=f(gt,{optional:!0});_lastDestroyedFocusedChipIndex=null;_keyManager;_destroyed=new V;_defaultRole=`presentation`;get chipFocusChanges(){return this._getChipStream(e=>e._onFocus)}get chipDestroyedChanges(){return this._getChipStream(e=>e.destroyed)}get chipRemovedChanges(){return this._getChipStream(e=>e.removed)}get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._syncChipsState()}_disabled=!1;get empty(){return!this._chips||this._chips.length===0}get role(){return this._explicitRole?this._explicitRole:this.empty?null:this._defaultRole}tabIndex=0;set role(e){this._explicitRole=e}_explicitRole=null;get focused(){return this._hasFocusedChip()}_chips;_chipActions=new wn;ngAfterViewInit(){this._setUpFocusManagement(),this._trackChipSetChanges(),this._trackDestroyedFocusedChip()}ngOnDestroy(){this._keyManager?.destroy(),this._chipActions.destroy(),this._destroyed.next(),this._destroyed.complete()}_hasFocusedChip(){return this._chips&&this._chips.some(e=>e._hasFocus())}_syncChipsState(){this._chips?.forEach(e=>{e._chipListDisabled=this._disabled,e._changeDetectorRef.markForCheck()})}focus(){}_handleKeydown(e){this._originatesFromChip(e)&&this._keyManager.onKeydown(e)}_isValidIndex(e){return e>=0&&e<this._chips.length}_allowFocusEscape(){let e=this._elementRef.nativeElement.tabIndex;e!==-1&&(this._elementRef.nativeElement.tabIndex=-1,setTimeout(()=>this._elementRef.nativeElement.tabIndex=e))}_getChipStream(e){return this._chips.changes.pipe(mt(null),_n(()=>kt(...this._chips.map(e))))}_originatesFromChip(e){let i=e.target;for(;i&&i!==this._elementRef.nativeElement;){if(i.classList.contains(`mat-mdc-chip`))return!0;i=i.parentElement}return!1}_setUpFocusManagement(){this._chips.changes.pipe(mt(this._chips)).subscribe(e=>{let i=[];e.forEach(a=>a._getActions().forEach(o=>i.push(o))),this._chipActions.reset(i),this._chipActions.notifyOnChanges()}),this._keyManager=new ea(this._chipActions).withVerticalOrientation().withHorizontalOrientation(this._dir?this._dir.value:`ltr`).withHomeAndEnd().skipPredicate(e=>this._skipPredicate(e)),this.chipFocusChanges.pipe(Ne(this._destroyed)).subscribe(({chip:e})=>{let i=e._getSourceAction(document.activeElement);i&&this._keyManager.updateActiveItem(i)}),this._dir?.change.pipe(Ne(this._destroyed)).subscribe(e=>this._keyManager.withHorizontalOrientation(e))}_skipPredicate(e){return e.disabled}_trackChipSetChanges(){this._chips.changes.pipe(mt(null),Ne(this._destroyed)).subscribe(()=>{this.disabled&&Promise.resolve().then(()=>this._syncChipsState()),this._redirectDestroyedChipFocus()})}_trackDestroyedFocusedChip(){this.chipDestroyedChanges.pipe(Ne(this._destroyed)).subscribe(e=>{let a=this._chips.toArray().indexOf(e.chip),o=e.chip._hasFocus(),r=e.chip._hadFocusOnRemove&&this._keyManager.activeItem&&e.chip._getActions().includes(this._keyManager.activeItem),s=o||r;this._isValidIndex(a)&&s&&(this._lastDestroyedFocusedChipIndex=a)})}_redirectDestroyedChipFocus(){if(this._lastDestroyedFocusedChipIndex!=null){if(this._chips.length){let e=Math.min(this._lastDestroyedFocusedChipIndex,this._chips.length-1),i=this._chips.toArray()[e];i.disabled?this._chips.length===1?this.focus():this._keyManager.setPreviousItemActive():i.focus()}else this.focus();this._lastDestroyedFocusedChipIndex=null}}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return H({type:t,selectors:[[`mat-chip-set`]],contentQueries:function(a,o,r){if(a&1&&jt(r,Ul,5),a&2){let s;J(s=ee())&&(o._chips=s)}},hostAttrs:[1,`mat-mdc-chip-set`,`mdc-evolution-chip-set`],hostVars:1,hostBindings:function(a,o){a&1&&C(`keydown`,function(s){return o._handleKeydown(s)}),a&2&&ne(`role`,o.role)},inputs:{disabled:[2,`disabled`,`disabled`,z],role:`role`,tabIndex:[2,`tabIndex`,`tabIndex`,i=>i==null?0:dt(i)]},ngContentSelectors:[`*`],decls:2,vars:0,consts:[[`role`,`presentation`,1,`mdc-evolution-chip-set__chips`]],template:function(a,o){a&1&&(Fe(),ct(0,`div`,0),he(1),ht())},styles:[`.mat-mdc-chip-set {
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
`],encapsulation:2})})()}return t})();var am=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({providers:[Xr,{provide:AR,useValue:{separatorKeyCodes:[13]}}],imports:[ki,we]})}return t})();var iw=(()=>{class t{labelPosition=`after`;static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return H({type:t,selectors:[[``,`mat-internal-form-field`,``]],hostAttrs:[1,`mdc-form-field`,`mat-internal-form-field`],hostVars:2,hostBindings:function(a,o){a&2&&G(`mdc-form-field--align-end`,o.labelPosition===`before`)},inputs:{labelPosition:`labelPosition`},ngContentSelectors:[`*`],decls:1,vars:0,template:function(a,o){a&1&&(Fe(),he(0))},styles:[`.mat-internal-form-field {
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
`],encapsulation:2})})()}return t})();var mb={color:`accent`,clickAction:`check-indeterminate`,disabledInteractive:!1};var OR=new N(`mat-checkbox-default-options`,{providedIn:`root`,factory:()=>mb});var an=(function(t){return t[t.Init=0]=`Init`,t[t.Checked=1]=`Checked`,t[t.Unchecked=2]=`Unchecked`,t[t.Indeterminate=3]=`Indeterminate`,t})(an||{});var pb=class{source;checked};var $l=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Le);_ngZone=f(Z);_animationsDisabled=Ue();_options=f(OR,{optional:!0});focus(){this._inputElement.nativeElement.focus()}_createChangeEvent(e){let i=new pb;return i.source=this,i.checked=e,i}_getAnimationTargetElement(){return this._inputElement?.nativeElement}_animationClasses={uncheckedToChecked:`mdc-checkbox--anim-unchecked-checked`,uncheckedToIndeterminate:`mdc-checkbox--anim-unchecked-indeterminate`,checkedToUnchecked:`mdc-checkbox--anim-checked-unchecked`,checkedToIndeterminate:`mdc-checkbox--anim-checked-indeterminate`,indeterminateToChecked:`mdc-checkbox--anim-indeterminate-checked`,indeterminateToUnchecked:`mdc-checkbox--anim-indeterminate-unchecked`};ariaLabel=``;ariaLabelledby=null;ariaDescribedby;ariaExpanded;ariaControls;ariaOwns;_uniqueId;id;get inputId(){return`${this.id||this._uniqueId}-input`}required=!1;labelPosition=`after`;name=null;change=new te;indeterminateChange=new te;value;disableRipple=!1;_inputElement;tabIndex;color;disabledInteractive;_onTouched=()=>{};_currentAnimationClass=``;_currentCheckState=an.Init;_controlValueAccessorChangeFn=()=>{};_validatorChangeFn=()=>{};constructor(){f(Je).load(pn);let e=f(new Ki(`tabindex`),{optional:!0});this._options=this._options||mb,this.color=this._options.color||mb.color,this.tabIndex=e==null?0:parseInt(e)||0,this.id=this._uniqueId=f(je).getId(`mat-mdc-checkbox-`),this.disabledInteractive=this._options?.disabledInteractive??!1}ngOnChanges(e){e.required&&this._validatorChangeFn()}ngAfterViewInit(){this._syncIndeterminate(this.indeterminate)}get checked(){return this._checked}set checked(e){e!=this.checked&&(this._checked=e,this._changeDetectorRef.markForCheck())}_checked=!1;get disabled(){return this._disabled}set disabled(e){e!==this.disabled&&(this._disabled=e,this._changeDetectorRef.markForCheck())}_disabled=!1;get indeterminate(){return this._indeterminate()}set indeterminate(e){let i=e!=this._indeterminate();this._indeterminate.set(e),i&&(e?this._transitionCheckState(an.Indeterminate):this._transitionCheckState(this.checked?an.Checked:an.Unchecked),this.indeterminateChange.emit(e)),this._syncIndeterminate(e)}_indeterminate=T(!1);_isRippleDisabled(){return this.disableRipple||this.disabled}_onLabelTextChange(){this._changeDetectorRef.detectChanges()}writeValue(e){this.checked=!!e}registerOnChange(e){this._controlValueAccessorChangeFn=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e}validate(e){return this.required&&e.value!==!0?{required:!0}:null}registerOnValidatorChange(e){this._validatorChangeFn=e}_transitionCheckState(e){let i=this._currentCheckState,a=this._getAnimationTargetElement();if(!(i===e||!a)&&(this._currentAnimationClass&&a.classList.remove(this._currentAnimationClass),this._currentAnimationClass=this._getAnimationClassForCheckStateTransition(i,e),this._currentCheckState=e,this._currentAnimationClass.length>0)){a.classList.add(this._currentAnimationClass);let o=this._currentAnimationClass;this._ngZone.runOutsideAngular(()=>{setTimeout(()=>{a.classList.remove(o)},1e3)})}}_emitChangeEvent(){this._controlValueAccessorChangeFn(this.checked),this.change.emit(this._createChangeEvent(this.checked)),this._inputElement&&(this._inputElement.nativeElement.checked=this.checked)}toggle(){this.checked=!this.checked,this._controlValueAccessorChangeFn(this.checked)}_handleInputClick(){let e=this._options?.clickAction;!this.disabled&&e!==`noop`?(this.indeterminate&&e!==`check`&&Promise.resolve().then(()=>{this._indeterminate.set(!1),this.indeterminateChange.emit(!1)}),this._checked=!this._checked,this._transitionCheckState(this._checked?an.Checked:an.Unchecked),this._emitChangeEvent()):(this.disabled&&this.disabledInteractive||!this.disabled&&e===`noop`)&&(this._inputElement.nativeElement.checked=this.checked,this._inputElement.nativeElement.indeterminate=this.indeterminate)}_onInteractionEvent(e){e.stopPropagation()}_onBlur(){Promise.resolve().then(()=>{this._onTouched(),this._changeDetectorRef.markForCheck()})}_getAnimationClassForCheckStateTransition(e,i){if(this._animationsDisabled)return``;switch(e){case an.Init:if(i===an.Checked)return this._animationClasses.uncheckedToChecked;if(i==an.Indeterminate)return this._checked?this._animationClasses.checkedToIndeterminate:this._animationClasses.uncheckedToIndeterminate;break;case an.Unchecked:return i===an.Checked?this._animationClasses.uncheckedToChecked:this._animationClasses.uncheckedToIndeterminate;case an.Checked:return i===an.Unchecked?this._animationClasses.checkedToUnchecked:this._animationClasses.checkedToIndeterminate;case an.Indeterminate:return i===an.Checked?this._animationClasses.indeterminateToChecked:this._animationClasses.indeterminateToUnchecked}return``}_syncIndeterminate(e){let i=this._inputElement;i&&(i.nativeElement.indeterminate=e)}_onInputClick(){this._handleInputClick()}_preventBubblingFromLabel(e){e.target&&this._inputElement&&e.target!==this._inputElement.nativeElement&&e.stopPropagation()}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`input`];return H({type:t,selectors:[[`mat-checkbox`]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;J(s=ee())&&(r._inputElement=s.first)}},hostAttrs:[1,`mat-mdc-checkbox`],hostVars:16,hostBindings:function(o,r){o&2&&(qt(`id`,r.id),ne(`tabindex`,null)(`aria-label`,null)(`aria-labelledby`,null),tt(r.color?`mat-`+r.color:`mat-accent`),G(`_mat-animation-noopable`,r._animationsDisabled)(`mdc-checkbox--disabled`,r.disabled)(`mat-mdc-checkbox-disabled`,r.disabled)(`mat-mdc-checkbox-checked`,r.checked)(`mat-mdc-checkbox-disabled-interactive`,r.disabledInteractive))},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],ariaDescribedby:[0,`aria-describedby`,`ariaDescribedby`],ariaExpanded:[2,`aria-expanded`,`ariaExpanded`,z],ariaControls:[0,`aria-controls`,`ariaControls`],ariaOwns:[0,`aria-owns`,`ariaOwns`],id:`id`,required:[2,`required`,`required`,z],labelPosition:`labelPosition`,name:`name`,value:`value`,disableRipple:[2,`disableRipple`,`disableRipple`,z],tabIndex:[2,`tabIndex`,`tabIndex`,a=>a==null?void 0:dt(a)],color:`color`,disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z],checked:[2,`checked`,`checked`,z],disabled:[2,`disabled`,`disabled`,z],indeterminate:[2,`indeterminate`,`indeterminate`,z]},outputs:{change:`change`,indeterminateChange:`indeterminateChange`},exportAs:[`matCheckbox`],features:[Ge([{provide:Kr,useExisting:Ht(()=>t),multi:!0},{provide:Qr,useExisting:t,multi:!0}]),_t],ngContentSelectors:[`*`],decls:15,vars:23,consts:[[`checkbox`,``],[`input`,``],[`label`,``],[`mat-internal-form-field`,``,3,`click`,`labelPosition`,`for`],[1,`mdc-checkbox`],[`aria-hidden`,`true`,1,`mat-mdc-checkbox-touch-target`],[`type`,`checkbox`,1,`mdc-checkbox__native-control`,3,`blur`,`click`,`change`,`checked`,`indeterminate`,`disabled`,`id`,`required`,`tabIndex`],[`aria-hidden`,`true`,1,`mdc-checkbox__ripple`],[`aria-hidden`,`true`,1,`mdc-checkbox__background`],[`focusable`,`false`,`viewBox`,`0 0 24 24`,1,`mdc-checkbox__checkmark`],[`fill`,`none`,`d`,`M1.73,12.91 8.1,19.28 22.79,4.59`,1,`mdc-checkbox__checkmark-path`],[1,`mdc-checkbox__mixedmark`],[`mat-ripple`,``,`aria-hidden`,`true`,1,`mat-mdc-checkbox-ripple`,`mat-focus-indicator`,3,`matRippleTrigger`,`matRippleDisabled`,`matRippleCentered`],[1,`mat-internal-form-field-label`,`mdc-label`]],template:function(o,r){if(o&1&&(Fe(),c(0,`label`,3),C(`click`,function(l){return r._preventBubblingFromLabel(l)}),c(1,`span`,4,0),U(3,`span`,5),c(4,`input`,6,1),C(`blur`,function(){return r._onBlur()})(`click`,function(){return r._onInputClick()})(`change`,function(l){return r._onInteractionEvent(l)}),d(),U(6,`span`,7),c(7,`span`,8),Gt(),c(8,`svg`,9),U(9,`path`,10),d(),ya(),U(10,`span`,11),d(),U(11,`span`,12),d(),c(12,`span`,13,2),he(14),d()()),o&2){let s=ft(2);S(`labelPosition`,r.labelPosition)(`for`,r.inputId),m(4),G(`mdc-checkbox--selected`,r.checked),S(`checked`,r.checked)(`indeterminate`,r.indeterminate)(`disabled`,r.disabled&&!r.disabledInteractive)(`id`,r.inputId)(`required`,r.required)(`tabIndex`,r.disabled&&!r.disabledInteractive?-1:r.tabIndex),ne(`aria-label`,r.ariaLabel||null)(`aria-labelledby`,r.ariaLabelledby)(`aria-describedby`,r.ariaDescribedby)(`aria-checked`,r.indeterminate?`mixed`:null)(`aria-controls`,r.ariaControls)(`aria-disabled`,r.disabled&&r.disabledInteractive?!0:null)(`aria-expanded`,r.ariaExpanded)(`aria-owns`,r.ariaOwns)(`name`,r.name)(`value`,r.value),m(7),S(`matRippleTrigger`,s)(`matRippleDisabled`,r.disableRipple||r.disabled)(`matRippleCentered`,!0)}},dependencies:[li,iw],styles:[`.mdc-checkbox {
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
`],encapsulation:2})})()}return t})();var om=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[$l,we]})}return t})();var di=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[Tu,Ti,we]})}return t})();var PR=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`ng-component`]],hostAttrs:[`cdk-text-field-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`textarea.cdk-textarea-autosize {
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
`],encapsulation:2})}return t})();var FR={passive:!0};var ow=(()=>{class t{_platform=f(Re);_ngZone=f(Z);_renderer=f(Nt).createRenderer(null,null);_styleLoader=f(Je);_monitoredElements=new Map;monitor(e){if(!this._platform.isBrowser)return Oi;this._styleLoader.load(PR);let i=mn(e),a=this._monitoredElements.get(i);if(a)return a.subject;let o=new V,r=`cdk-text-field-autofilled`,s=h=>{h.animationName===`cdk-text-field-autofill-start`&&!i.classList.contains(r)?(i.classList.add(r),this._ngZone.run(()=>o.next({target:h.target,isAutofilled:!0}))):h.animationName===`cdk-text-field-autofill-end`&&i.classList.contains(r)&&(i.classList.remove(r),this._ngZone.run(()=>o.next({target:h.target,isAutofilled:!1})))},l=this._ngZone.runOutsideAngular(()=>(i.classList.add(`cdk-text-field-autofill-monitored`),this._renderer.listen(i,`animationstart`,s,FR)));return this._monitoredElements.set(i,{subject:o,unlisten:l}),o}stopMonitoring(e){let i=mn(e),a=this._monitoredElements.get(i);a&&(a.unlisten(),a.subject.complete(),i.classList.remove(`cdk-text-field-autofill-monitored`),i.classList.remove(`cdk-text-field-autofilled`),this._monitoredElements.delete(i))}ngOnDestroy(){this._monitoredElements.forEach((e,i)=>this.stopMonitoring(i))}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var rw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({})}return t})();var sw=new N(`MAT_INPUT_VALUE_ACCESSOR`);var LR=[`button`,`checkbox`,`file`,`hidden`,`image`,`radio`,`range`,`reset`,`submit`];var VR=new N(`MAT_INPUT_CONFIG`);var Jr=(()=>{class t{_elementRef=f(Y);_platform=f(Re);ngControl=f(Mi,{optional:!0,self:!0});_autofillMonitor=f(ow);_ngZone=f(Z);_formField=f(Hl,{optional:!0});_renderer=f(Pe);_uid=f(je).getId(`mat-input-`);_previousNativeValue;_inputValueAccessor;_signalBasedValueAccessor;_previousPlaceholder=null;_errorStateTracker;_config=f(VR,{optional:!0});_cleanupIosKeyup;_cleanupWebkitWheel;_isServer=!1;_isNativeSelect=!1;_isTextarea=!1;_isInFormField=!1;focused=!1;stateChanges=new V;controlType=`mat-input`;autofilled=!1;get disabled(){return this._disabled}set disabled(e){this._disabled=ta(e),this.focused&&(this.focused=!1,this.stateChanges.next())}_disabled=!1;get id(){return this._id}set id(e){this._id=e||this._uid}_id;placeholder;name;get required(){return this._required??this.ngControl?.control?.hasValidator(Lo.required)??!1}set required(e){this._required=ta(e)}_required;get type(){return this._type}set type(e){this._type=e||`text`,this._validateType(),!this._isTextarea&&Wg().has(this._type)&&(this._elementRef.nativeElement.type=this._type)}_type=`text`;get errorStateMatcher(){return this._errorStateTracker.matcher}set errorStateMatcher(e){this._errorStateTracker.matcher=e}userAriaDescribedBy;get value(){return this._signalBasedValueAccessor?this._signalBasedValueAccessor.value():this._inputValueAccessor.value}set value(e){e!==this.value&&(this._signalBasedValueAccessor?this._signalBasedValueAccessor.value.set(e):this._inputValueAccessor.value=e,this.stateChanges.next())}get readonly(){return this._readonly}set readonly(e){this._readonly=ta(e)}_readonly=!1;disabledInteractive;get errorState(){return this._errorStateTracker.errorState}set errorState(e){this._errorStateTracker.errorState=e}_neverEmptyInputTypes=[`date`,`datetime`,`datetime-local`,`month`,`time`,`week`].filter(e=>Wg().has(e));constructor(){let e=f(Vl,{optional:!0}),i=f(Bl,{optional:!0}),a=f(Xr),o=f(sw,{optional:!0,self:!0}),r=f(em,{optional:!0,self:!0}),s=this._elementRef.nativeElement,l=s.nodeName.toLowerCase();o?cn(o.value)?this._signalBasedValueAccessor=o:this._inputValueAccessor=o:this._inputValueAccessor=s,this._previousNativeValue=this.value,this.id=this.id,this._platform.IOS&&this._ngZone.runOutsideAngular(()=>{this._cleanupIosKeyup=this._renderer.listen(s,`keyup`,this._iOSKeyupListener)}),this._errorStateTracker=new Zr(a,r||this.ngControl,i,e,this.stateChanges),this._isServer=!this._platform.isBrowser,this._isNativeSelect=l===`select`,this._isTextarea=l===`textarea`,this._isInFormField=!!this._formField,this.disabledInteractive=this._config?.disabledInteractive||!1,this._isNativeSelect&&(this.controlType=s.multiple?`mat-native-select-multiple`:`mat-native-select`),this._signalBasedValueAccessor&&vt(()=>{this._signalBasedValueAccessor.value(),this.stateChanges.next()})}ngAfterViewInit(){this._platform.isBrowser&&this._autofillMonitor.monitor(this._elementRef.nativeElement).subscribe(e=>{this.autofilled=e.isAutofilled,this.stateChanges.next()})}ngOnChanges(){this.stateChanges.next()}ngOnDestroy(){this.stateChanges.complete(),this._platform.isBrowser&&this._autofillMonitor.stopMonitoring(this._elementRef.nativeElement),this._cleanupIosKeyup?.(),this._cleanupWebkitWheel?.()}ngDoCheck(){this.ngControl&&(this.updateErrorState(),this.ngControl.disabled!==null&&this.ngControl.disabled!==this.disabled&&(this.disabled=this.ngControl.disabled,this.stateChanges.next())),this._dirtyCheckNativeValue(),this._dirtyCheckPlaceholder()}focus(e){this._elementRef.nativeElement.focus(e)}updateErrorState(){this._errorStateTracker.updateErrorState()}_focusChanged(e){if(e!==this.focused){if(!this._isNativeSelect&&e&&this.disabled&&this.disabledInteractive){let i=this._elementRef.nativeElement;i.type===`number`?(i.type=`text`,i.setSelectionRange(0,0),i.type=`number`):i.setSelectionRange(0,0)}this.focused=e,this.stateChanges.next()}}_onInput(){}_dirtyCheckNativeValue(){let e=this._elementRef.nativeElement.value;this._previousNativeValue!==e&&(this._previousNativeValue=e,this.stateChanges.next())}_dirtyCheckPlaceholder(){let e=this._getPlaceholder();if(e!==this._previousPlaceholder){let i=this._elementRef.nativeElement;this._previousPlaceholder=e,e?i.setAttribute(`placeholder`,e):i.removeAttribute(`placeholder`)}}_getPlaceholder(){return this.placeholder||null}_validateType(){LR.indexOf(this._type)}_isNeverEmpty(){return this._neverEmptyInputTypes.indexOf(this._type)>-1}_isBadInput(){let e=this._elementRef.nativeElement.validity;return e&&e.badInput}get empty(){return!this._isNeverEmpty()&&!this._elementRef.nativeElement.value&&!this._isBadInput()&&!this.autofilled}get shouldLabelFloat(){if(this._isNativeSelect){let e=this._elementRef.nativeElement,i=e.options[0];return this.focused||e.multiple||!this.empty||!!(e.selectedIndex>-1&&i&&i.label)}else return this.focused&&!this.disabled||!this.empty}get describedByIds(){return this._elementRef.nativeElement.getAttribute(`aria-describedby`)?.split(` `)||[]}setDescribedByIds(e){let i=this._elementRef.nativeElement;e.length?i.setAttribute(`aria-describedby`,e.join(` `)):i.removeAttribute(`aria-describedby`)}onContainerClick(){this.focused||this.focus()}_isInlineSelect(){let e=this._elementRef.nativeElement;return this._isNativeSelect&&(e.multiple||e.size>1)}_iOSKeyupListener=e=>{let i=e.target;!i.value&&i.selectionStart===0&&i.selectionEnd===0&&(i.setSelectionRange(1,1),i.setSelectionRange(0,0))};_getReadonlyAttribute(){return this._isNativeSelect?null:this.readonly||this.disabled&&this.disabledInteractive?`true`:null}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`input`,`matInput`,``],[`textarea`,`matInput`,``],[`select`,`matNativeControl`,``],[`input`,`matNativeControl`,``],[`textarea`,`matNativeControl`,``]],hostAttrs:[1,`mat-mdc-input-element`],hostVars:21,hostBindings:function(i,a){i&1&&C(`focus`,function(){return a._focusChanged(!0)})(`blur`,function(){return a._focusChanged(!1)})(`input`,function(){return a._onInput()}),i&2&&(qt(`id`,a.id)(`disabled`,a.disabled&&!a.disabledInteractive)(`required`,a.required),ne(`name`,a.name||null)(`readonly`,a._getReadonlyAttribute())(`aria-disabled`,a.disabled&&a.disabledInteractive?`true`:null)(`aria-invalid`,a.empty&&a.required?null:a.errorState)(`aria-required`,a.required)(`id`,a.id),G(`mat-input-server`,a._isServer)(`mat-mdc-form-field-textarea-control`,a._isInFormField&&a._isTextarea)(`mat-mdc-form-field-input-control`,a._isInFormField)(`mat-mdc-input-disabled-interactive`,a.disabledInteractive)(`mdc-text-field__input`,a._isInFormField)(`mat-mdc-native-select-inline`,a._isInlineSelect()))},inputs:{disabled:`disabled`,id:`id`,placeholder:`placeholder`,name:`name`,required:`required`,type:`type`,errorStateMatcher:`errorStateMatcher`,userAriaDescribedBy:[0,`aria-describedby`,`userAriaDescribedBy`],value:`value`,readonly:`readonly`,disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z]},exportAs:[`matInput`],features:[Ge([{provide:zl,useExisting:t}]),_t]})}return t})();var es=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[di,di,rw,we]})}return t})();var Gl=class{_attachedHost=null;attach(n){return this._attachedHost=n,n.attach(this)}detach(){let n=this._attachedHost;n!=null&&(this._attachedHost=null,n.detach())}get isAttached(){return this._attachedHost!=null}setAttachedHost(n){this._attachedHost=n}};var Aa=class extends Gl{component;viewContainerRef;injector;projectableNodes;bindings;directives;constructor(n,e,i,a,o,r){super(),this.component=n,this.viewContainerRef=e,this.injector=i,this.projectableNodes=a,this.bindings=o||null,this.directives=r||null}};var Hn=class extends Gl{templateRef;viewContainerRef;context;injector;constructor(n,e,i,a){super(),this.templateRef=n,this.viewContainerRef=e,this.context=i,this.injector=a}get origin(){return this.templateRef.elementRef}attach(n,e=this.context){return this.context=e,super.attach(n)}detach(){return this.context=void 0,super.detach()}};var gb=class extends Gl{element;constructor(n){super(),this.element=n instanceof Y?n.nativeElement:n}};var ts=class{_attachedPortal=null;_disposeFn=null;_isDisposed=!1;hasAttached(){return!!this._attachedPortal}attach(n){if(n instanceof Aa)return this._attachedPortal=n,this.attachComponentPortal(n);if(n instanceof Hn)return this._attachedPortal=n,this.attachTemplatePortal(n);if(this.attachDomPortal&&n instanceof gb)return this._attachedPortal=n,this.attachDomPortal(n)}attachDomPortal=null;detach(){this._attachedPortal&&(this._attachedPortal.setAttachedHost(null),this._attachedPortal=null),this._invokeDisposeFn()}dispose(){this.hasAttached()&&this.detach(),this._invokeDisposeFn(),this._isDisposed=!0}setDisposeFn(n){this._disposeFn=n}_invokeDisposeFn(){this._disposeFn&&(this._disposeFn(),this._disposeFn=null)}};var Wl=class extends ts{outletElement;_appRef;_defaultInjector;constructor(n,e,i){super(),this.outletElement=n,this._appRef=e,this._defaultInjector=i}attachComponentPortal(n){let e;if(n.viewContainerRef){let i=n.injector||n.viewContainerRef.injector,a=i.get(ni,null,{optional:!0})||void 0;e=n.viewContainerRef.createComponent(n.component,{index:n.viewContainerRef.length,injector:i,ngModuleRef:a,projectableNodes:n.projectableNodes||void 0,bindings:n.bindings||void 0,directives:n.directives||void 0}),this.setDisposeFn(()=>e.destroy())}else{let i=this._appRef,a=n.injector||this._defaultInjector||se.NULL,o=a.get(Mt,i.injector);e=ou(n.component,{elementInjector:a,environmentInjector:o,projectableNodes:n.projectableNodes||void 0,bindings:n.bindings||void 0,directives:n.directives||void 0}),i.attachView(e.hostView),this.setDisposeFn(()=>{i.viewCount>0&&i.detachView(e.hostView),e.destroy()})}return this.outletElement.appendChild(this._getComponentRootNode(e)),this._attachedPortal=n,e}attachTemplatePortal(n){let e=n.viewContainerRef,i=e.createEmbeddedView(n.templateRef,n.context,{injector:n.injector});return i.rootNodes.forEach(a=>this.outletElement.appendChild(a)),i.detectChanges(),this.setDisposeFn(()=>{let a=e.indexOf(i);a!==-1&&e.remove(a)}),this._attachedPortal=n,i}attachDomPortal=n=>{let e=n.element;e.parentNode;let i=this.outletElement.ownerDocument.createComment(`dom-portal`);e.parentNode.insertBefore(i,e),this.outletElement.appendChild(e),this._attachedPortal=n,super.setDisposeFn(()=>{i.parentNode&&i.parentNode.replaceChild(e,i)})};dispose(){super.dispose(),this.outletElement.remove()}_getComponentRootNode(n){return n.hostView.rootNodes[0]}};var lw=(()=>{class t extends Hn{constructor(){let e=f(Vt),i=f(Bt);super(e,i)}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdkPortal`,``]],exportAs:[`cdkPortal`],features:[Be]})}return t})();var Ra=(()=>{class t extends ts{_moduleRef=f(ni,{optional:!0});_document=f(me);_viewContainerRef=f(Bt);_isInitialized=!1;_attachedRef=null;get portal(){return this._attachedPortal}set portal(e){this.hasAttached()&&!e&&!this._isInitialized||(this.hasAttached()&&super.detach(),e&&super.attach(e),this._attachedPortal=e||null)}attached=new te;get attachedRef(){return this._attachedRef}ngOnInit(){this._isInitialized=!0}ngOnDestroy(){super.dispose(),this._attachedRef=this._attachedPortal=null}attachComponentPortal(e){e.setAttachedHost(this);let i=e.viewContainerRef!=null?e.viewContainerRef:this._viewContainerRef,a=i.createComponent(e.component,{index:i.length,injector:e.injector||i.injector,projectableNodes:e.projectableNodes||void 0,ngModuleRef:this._moduleRef||void 0,bindings:e.bindings||void 0,directives:e.directives||void 0});return i!==this._viewContainerRef&&this._getRootNode().appendChild(a.hostView.rootNodes[0]),super.setDisposeFn(()=>a.destroy()),this._attachedPortal=e,this._attachedRef=a,this.attached.emit(a),a}attachTemplatePortal(e){e.setAttachedHost(this);let i=this._viewContainerRef.createEmbeddedView(e.templateRef,e.context,{injector:e.injector});return super.setDisposeFn(()=>this._viewContainerRef.clear()),this._attachedPortal=e,this._attachedRef=i,this.attached.emit(i),i}attachDomPortal=e=>{let i=e.element;i.parentNode;let a=this._document.createComment(`dom-portal`);e.setAttachedHost(this),i.parentNode.insertBefore(a,i),this._getRootNode().appendChild(i),this._attachedPortal=e,super.setDisposeFn(()=>{a.parentNode&&a.parentNode.replaceChild(i,a)})};_getRootNode(){let e=this._viewContainerRef.element.nativeElement;return e.nodeType===e.ELEMENT_NODE?e:e.parentNode}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`cdkPortalOutlet`,``]],inputs:{portal:[0,`cdkPortalOutlet`,`portal`]},outputs:{attached:`attached`},exportAs:[`cdkPortalOutlet`],features:[Be]})}return t})();var Bo=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({})}return t})();var BR=20;var Oa=(()=>{class t{_ngZone=f(Z);_platform=f(Re);_renderer=f(Nt).createRenderer(null,null);_cleanupGlobalListener;_scrolled=new V;_scrolledCount=0;scrollContainers=new Map;register(e){this.scrollContainers.has(e)||this.scrollContainers.set(e,e.elementScrolled().subscribe(()=>this._scrolled.next(e)))}deregister(e){let i=this.scrollContainers.get(e);i&&(i.unsubscribe(),this.scrollContainers.delete(e))}scrolled(e=BR){return this._platform.isBrowser?new Ce(i=>{this._cleanupGlobalListener||(this._cleanupGlobalListener=this._ngZone.runOutsideAngular(()=>this._renderer.listen(`document`,`scroll`,()=>this._scrolled.next())));let a=e>0?this._scrolled.pipe(Bc(e)).subscribe(i):this._scrolled.subscribe(i);return this._scrolledCount++,()=>{a.unsubscribe(),this._scrolledCount--,this._scrolledCount||(this._cleanupGlobalListener?.(),this._cleanupGlobalListener=void 0)}}):Mn()}ngOnDestroy(){this._cleanupGlobalListener?.(),this._cleanupGlobalListener=void 0,this.scrollContainers.forEach((e,i)=>this.deregister(i)),this._scrolled.complete()}ancestorScrolled(e,i){let a=this.getAncestorScrollContainers(e);return this.scrolled(i).pipe(Qe(o=>!o||a.indexOf(o)>-1))}getAncestorScrollContainers(e){let i=[];return this.scrollContainers.forEach((a,o)=>{this._targetContainsElement(o,e)&&i.push(o)}),i}_targetContainsElement(e,i){let a=mn(i),o=e.getElementRef().nativeElement;do if(a==o)return!0;while(a=a.parentElement);return!1}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var ql=(()=>{class t{elementRef=f(Y);scrollDispatcher=f(Oa);ngZone=f(Z);dir=f(gt,{optional:!0});_scrollElement=this.elementRef.nativeElement;_destroyed=new V;_renderer=f(Pe);_cleanupScroll;_elementScrolled=new V;ngOnInit(){this._cleanupScroll=this.ngZone.runOutsideAngular(()=>this._renderer.listen(this._scrollElement,`scroll`,e=>this._elementScrolled.next(e))),this.scrollDispatcher.register(this)}ngOnDestroy(){this._cleanupScroll?.(),this._elementScrolled.complete(),this.scrollDispatcher.deregister(this),this._destroyed.next(),this._destroyed.complete()}elementScrolled(){return this._elementScrolled}getElementRef(){return this.elementRef}scrollTo(e){let i=this.elementRef.nativeElement,a=this.dir&&this.dir.value==`rtl`;e.left??=a?e.end:e.start,e.right??=a?e.start:e.end,e.bottom!=null&&(e.top=i.scrollHeight-i.clientHeight-e.bottom),a&&Hr()!=si.NORMAL?(e.left!=null&&(e.right=i.scrollWidth-i.clientWidth-e.left),Hr()==si.INVERTED?e.left=e.right:Hr()==si.NEGATED&&(e.left=e.right?-e.right:e.right)):e.right!=null&&(e.left=i.scrollWidth-i.clientWidth-e.right),this._applyScrollToOptions(e)}_applyScrollToOptions(e){let i=this.elementRef.nativeElement;Lu()?i.scrollTo(e):(e.top!=null&&(i.scrollTop=e.top),e.left!=null&&(i.scrollLeft=e.left))}measureScrollOffset(e){let i=`left`,a=`right`,o=this.elementRef.nativeElement;if(e==`top`)return o.scrollTop;if(e==`bottom`)return o.scrollHeight-o.clientHeight-o.scrollTop;let r=this.dir&&this.dir.value==`rtl`;return e==`start`?e=r?a:i:e==`end`&&(e=r?i:a),r&&Hr()==si.INVERTED?e==i?o.scrollWidth-o.clientWidth-o.scrollLeft:o.scrollLeft:r&&Hr()==si.NEGATED?e==i?o.scrollLeft+o.scrollWidth-o.clientWidth:-o.scrollLeft:e==i?o.scrollLeft:o.scrollWidth-o.clientWidth-o.scrollLeft}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdk-scrollable`,``],[``,`cdkScrollable`,``]]})}return t})();var jR=20;var ui=(()=>{class t{_platform=f(Re);_listeners;_viewportSize=null;_change=new V;_document=f(me);constructor(){let e=f(Z),i=f(Nt).createRenderer(null,null);e.runOutsideAngular(()=>{if(this._platform.isBrowser){let a=o=>this._change.next(o);this._listeners=[i.listen(`window`,`resize`,a),i.listen(`window`,`orientationchange`,a)]}this.change().subscribe(()=>this._viewportSize=null)})}ngOnDestroy(){this._listeners?.forEach(e=>e()),this._change.complete()}getViewportSize(){this._viewportSize||this._updateViewportSize();let e={width:this._viewportSize.width,height:this._viewportSize.height};return this._platform.isBrowser||(this._viewportSize=null),e}getViewportRect(){let e=this.getViewportScrollPosition(),{width:i,height:a}=this.getViewportSize();return{top:e.top,left:e.left,bottom:e.top+a,right:e.left+i,height:a,width:i}}getViewportScrollPosition(){if(!this._platform.isBrowser)return{top:0,left:0};let e=this._document,i=this._getWindow(),a=e.documentElement,o=a.getBoundingClientRect();return{top:-o.top||e.body?.scrollTop||i.scrollY||a.scrollTop||0,left:-o.left||e.body?.scrollLeft||i.scrollX||a.scrollLeft||0}}change(e=jR){return e>0?this._change.pipe(Bc(e)):this._change}_getWindow(){return this._document.defaultView||window}_updateViewportSize(){let e=this._getWindow();this._viewportSize=this._platform.isBrowser?{width:e.innerWidth,height:e.innerHeight}:{width:0,height:0}}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var ia=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({})}return t})();var bb=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[we,ia,we,ia]})}return t})();var cw=Lu();function as(t){return new rm(t.get(ui),t.get(me))}var rm=class{_viewportRuler;_previousHTMLStyles={top:``,left:``};_previousScrollPosition;_isEnabled=!1;_document;constructor(n,e){this._viewportRuler=n,this._document=e}attach(){}enable(){if(this._canBeEnabled()){let n=this._document.documentElement;this._previousScrollPosition=this._viewportRuler.getViewportScrollPosition(),this._previousHTMLStyles.left=n.style.left||``,this._previousHTMLStyles.top=n.style.top||``,n.style.left=It(-this._previousScrollPosition.left),n.style.top=It(-this._previousScrollPosition.top),n.classList.add(`cdk-global-scrollblock`),this._isEnabled=!0}}disable(){if(this._isEnabled){let n=this._document.documentElement,e=this._document.body,i=n.style,a=e.style,o=i.scrollBehavior||``,r=a.scrollBehavior||``;this._isEnabled=!1,i.left=this._previousHTMLStyles.left,i.top=this._previousHTMLStyles.top,n.classList.remove(`cdk-global-scrollblock`),cw&&(i.scrollBehavior=a.scrollBehavior=`auto`),window.scroll(this._previousScrollPosition.left,this._previousScrollPosition.top),cw&&(i.scrollBehavior=o,a.scrollBehavior=r)}}_canBeEnabled(){if(this._document.documentElement.classList.contains(`cdk-global-scrollblock`)||this._isEnabled)return!1;let e=this._document.documentElement,i=this._viewportRuler.getViewportSize();return e.scrollHeight>i.height||e.scrollWidth>i.width}};function gw(t,n){return new sm(t.get(Oa),t.get(Z),t.get(ui),n)}var sm=class{_scrollDispatcher;_ngZone;_viewportRuler;_config;_scrollSubscription=null;_overlayRef;_initialScrollPosition;constructor(n,e,i,a){this._scrollDispatcher=n,this._ngZone=e,this._viewportRuler=i,this._config=a}attach(n){this._overlayRef,this._overlayRef=n}enable(){if(this._scrollSubscription)return;let n=this._scrollDispatcher.scrolled(0).pipe(Qe(e=>!e||!this._overlayRef.overlayElement.contains(e.getElementRef().nativeElement)));this._config&&this._config.threshold&&this._config.threshold>1?(this._initialScrollPosition=this._viewportRuler.getViewportScrollPosition().top,this._scrollSubscription=n.subscribe(()=>{let e=this._viewportRuler.getViewportScrollPosition().top;Math.abs(e-this._initialScrollPosition)>this._config.threshold?this._detach():this._overlayRef.updatePosition()})):this._scrollSubscription=n.subscribe(this._detach)}disable(){this._scrollSubscription&&(this._scrollSubscription.unsubscribe(),this._scrollSubscription=null)}detach(){this.disable(),this._overlayRef=null}_detach=()=>{this.disable(),this._overlayRef.hasAttached()&&this._ngZone.run(()=>this._overlayRef.detach())}};var Yl=class{enable(){}disable(){}attach(){}};function _b(t,n){return n.some(e=>{let i=t.bottom<e.top,a=t.top>e.bottom,o=t.right<e.left,r=t.left>e.right;return i||a||o||r})}function dw(t,n){return n.some(e=>{let i=t.top<e.top,a=t.bottom>e.bottom,o=t.left<e.left,r=t.right>e.right;return i||a||o||r})}function ra(t,n){return new lm(t.get(Oa),t.get(ui),t.get(Z),n)}var lm=class{_scrollDispatcher;_viewportRuler;_ngZone;_config;_scrollSubscription=null;_overlayRef;constructor(n,e,i,a){this._scrollDispatcher=n,this._viewportRuler=e,this._ngZone=i,this._config=a}attach(n){this._overlayRef,this._overlayRef=n}enable(){if(!this._scrollSubscription){let n=this._config?this._config.scrollThrottle:0;this._scrollSubscription=this._scrollDispatcher.scrolled(n).subscribe(()=>{if(this._overlayRef.updatePosition(),this._config&&this._config.autoClose){let e=this._overlayRef.overlayElement.getBoundingClientRect(),{width:i,height:a}=this._viewportRuler.getViewportSize();_b(e,[{width:i,height:a,bottom:a,right:i,top:0,left:0}])&&(this.disable(),this._ngZone.run(()=>this._overlayRef.detach()))}})}}disable(){this._scrollSubscription&&(this._scrollSubscription.unsubscribe(),this._scrollSubscription=null)}detach(){this.disable(),this._overlayRef=null}};var bw=(()=>{class t{_injector=f(se);noop=()=>new Yl;close=e=>gw(this._injector,e);block=()=>as(this._injector);reposition=e=>ra(this._injector,e);static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var aa=class{positionStrategy;scrollStrategy=new Yl;panelClass=``;hasBackdrop=!1;backdropClass=`cdk-overlay-dark-backdrop`;disableAnimations;width;height;minWidth;minHeight;maxWidth;maxHeight;direction;disposeOnNavigation=!1;usePopover;eventPredicate;constructor(n){if(n){let e=Object.keys(n);for(let i of e)n[i]!==void 0&&(this[i]=n[i])}}};var cm=class{connectionPair;scrollableViewProperties;constructor(n,e){this.connectionPair=n,this.scrollableViewProperties=e}};var vw=(()=>{class t{_attachedOverlays=[];_document=f(me);_isAttached=!1;ngOnDestroy(){this.detach()}add(e){this.remove(e),this._attachedOverlays.push(e)}remove(e){let i=this._attachedOverlays.indexOf(e);i>-1&&this._attachedOverlays.splice(i,1),this._attachedOverlays.length===0&&this.detach()}canReceiveEvent(e,i,a){return a.observers.length<1?!1:e.eventPredicate?e.eventPredicate(i):!0}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var _w=(()=>{class t extends vw{_ngZone=f(Z);_renderer=f(Nt).createRenderer(null,null);_cleanupKeydown;add(e){super.add(e),this._isAttached||(this._ngZone.runOutsideAngular(()=>{this._cleanupKeydown=this._renderer.listen(`body`,`keydown`,this._keydownListener)}),this._isAttached=!0)}detach(){this._isAttached&&(this._cleanupKeydown?.(),this._isAttached=!1)}_keydownListener=e=>{let i=this._attachedOverlays;for(let a=i.length-1;a>-1;a--){let o=i[a];if(this.canReceiveEvent(o,e,o._keydownEvents)){this._ngZone.run(()=>o._keydownEvents.next(e));break}}};static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var yw=(()=>{class t extends vw{_platform=f(Re);_ngZone=f(Z);_renderer=f(Nt).createRenderer(null,null);_cursorOriginalValue;_cursorStyleIsSet=!1;_pointerDownEventTarget=null;_cleanups;add(e){if(super.add(e),!this._isAttached){let i=this._document.body,a={capture:!0},o=this._renderer;this._cleanups=this._ngZone.runOutsideAngular(()=>[o.listen(i,`pointerdown`,this._pointerDownListener,a),o.listen(i,`click`,this._clickListener,a),o.listen(i,`auxclick`,this._clickListener,a),o.listen(i,`contextmenu`,this._clickListener,a)]),this._platform.IOS&&!this._cursorStyleIsSet&&(this._cursorOriginalValue=i.style.cursor,i.style.cursor=`pointer`,this._cursorStyleIsSet=!0),this._isAttached=!0}}detach(){this._isAttached&&(this._cleanups?.forEach(e=>e()),this._cleanups=void 0,this._platform.IOS&&this._cursorStyleIsSet&&(this._document.body.style.cursor=this._cursorOriginalValue,this._cursorStyleIsSet=!1),this._isAttached=!1)}_pointerDownListener=e=>{this._pointerDownEventTarget=Yt(e)};_clickListener=e=>{let i=Yt(e),a=e.type===`click`&&this._pointerDownEventTarget?this._pointerDownEventTarget:i;this._pointerDownEventTarget=null;let o=this._attachedOverlays.slice();for(let r=o.length-1;r>-1;r--){let s=o[r],l=s._outsidePointerEvents;if(!(!s.hasAttached()||!this.canReceiveEvent(s,e,l))){if(uw(s.overlayElement,i)||uw(s.overlayElement,a))break;this._ngZone?this._ngZone.run(()=>l.next(e)):l.next(e)}}};static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function uw(t,n){let e=typeof ShadowRoot<`u`&&ShadowRoot,i=n;for(;i;){if(i===t)return!0;i=e&&i instanceof ShadowRoot?i.host:i.parentNode}return!1}var xw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`ng-component`]],hostAttrs:[`cdk-overlay-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`.cdk-overlay-container, .cdk-global-overlay-wrapper {
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
`],encapsulation:2})}return t})();var mm=(()=>{class t{_platform=f(Re);_containerElement;_document=f(me);_styleLoader=f(Je);ngOnDestroy(){this._containerElement?.remove()}getContainerElement(){return this._loadStyles(),this._containerElement||this._createContainer(),this._containerElement}_createContainer(){let e=`cdk-overlay-container`;if(this._platform.isBrowser||Gg()){let a=this._document.querySelectorAll(`.${e}[platform="server"], .${e}[platform="test"]`);for(let o=0;o<a.length;o++)a[o].remove()}let i=this._document.createElement(`div`);i.classList.add(e),Gg()?i.setAttribute(`platform`,`test`):this._platform.isBrowser||i.setAttribute(`platform`,`server`),this._document.body.appendChild(i),this._containerElement=i}_loadStyles(){this._styleLoader.load(xw)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var yb=class{_renderer;_ngZone;element;_cleanupClick;_cleanupTransitionEnd;_fallbackTimeout;constructor(n,e,i,a){this._renderer=e,this._ngZone=i,this.element=n.createElement(`div`),this.element.classList.add(`cdk-overlay-backdrop`),this._cleanupClick=e.listen(this.element,`click`,a)}detach(){this._ngZone.runOutsideAngular(()=>{let n=this.element;clearTimeout(this._fallbackTimeout),this._cleanupTransitionEnd?.(),this._cleanupTransitionEnd=this._renderer.listen(n,`transitionend`,this.dispose),this._fallbackTimeout=setTimeout(this.dispose,500),n.style.pointerEvents=`none`,n.classList.remove(`cdk-overlay-backdrop-showing`)})}dispose=()=>{clearTimeout(this._fallbackTimeout),this._cleanupClick?.(),this._cleanupTransitionEnd?.(),this._cleanupClick=this._cleanupTransitionEnd=this._fallbackTimeout=void 0,this.element.remove()}};function xb(t){return t&&t.nodeType===1}var vb=T([]);var ns=class{_portalOutlet;_host;_pane;_config;_ngZone;_keyboardDispatcher;_document;_location;_outsideClickDispatcher;_animationsDisabled;_injector;_renderer;_backdropClick=new V;_attachments=new V;_detachments=new V;_positionStrategy;_scrollStrategy;_locationChanges;_backdropRef=null;_detachContentMutationObserver;_detachContentAfterRenderRef;_disposed=!1;_previousHostParent;_keydownEvents=new V;_outsidePointerEvents=new V;_afterNextRenderRef;constructor(n,e,i,a,o,r,s,l,h,p=!1,g,_){this._portalOutlet=n,this._host=e,this._pane=i,this._config=a,this._ngZone=o,this._keyboardDispatcher=r,this._document=s,this._location=l,this._outsideClickDispatcher=h,this._animationsDisabled=p,this._injector=g,this._renderer=_,a.scrollStrategy&&(this._scrollStrategy=a.scrollStrategy,this._scrollStrategy.attach(this)),this._positionStrategy=a.positionStrategy}get overlayElement(){return this._pane}get backdropElement(){return this._backdropRef?.element||null}get hostElement(){return this._host}get eventPredicate(){return this._config?.eventPredicate||null}attach(n){if(this._disposed)return null;this._attachHost();let e=this._portalOutlet.attach(n);if(this._positionStrategy?.attach(this),this._updateStackingOrder(),this._updateElementSize(),this._updateElementDirection(),it(()=>{vb.update(i=>i.includes(this)?i:[...i,this])}),this._scrollStrategy&&this._scrollStrategy.enable(),this._afterNextRenderRef?.destroy(),this._afterNextRenderRef=Rt(()=>{this.hasAttached()&&this.updatePosition()},{injector:this._injector}),this._togglePointerEvents(!0),this._config.hasBackdrop&&this._attachBackdrop(),this._config.panelClass&&this._toggleClasses(this._pane,this._config.panelClass,!0),this._attachments.next(),this._completeDetachContent(),this._keyboardDispatcher.add(this),this._config.disposeOnNavigation===!0||this._config.disposeOnNavigation===`pop-state`){let i=this._location.subscribe(()=>this.dispose());this._locationChanges=()=>i.unsubscribe()}else this._config.disposeOnNavigation===`url-change`&&(this._locationChanges=this._location.onUrlChange(()=>this.dispose()));return this._outsideClickDispatcher.add(this),typeof e?.onDestroy==`function`&&e.onDestroy(()=>{this.hasAttached()&&this._ngZone.runOutsideAngular(()=>Promise.resolve().then(()=>this.detach()))}),e}detach(){if(!this.hasAttached())return;this.detachBackdrop(),this._togglePointerEvents(!1),this._positionStrategy&&this._positionStrategy.detach&&this._positionStrategy.detach(),this._scrollStrategy&&this._scrollStrategy.disable();let n=this._portalOutlet.detach();return this._detachments.next(),this._completeDetachContent(),this._keyboardDispatcher.remove(this),this._detachContentWhenEmpty(),this._locationChanges?.(),this._outsideClickDispatcher.remove(this),it(()=>{vb.update(e=>e.filter(i=>i!==this))}),n}dispose(){if(this._disposed)return;let n=this.hasAttached();this._positionStrategy&&this._positionStrategy.dispose(),this._disposeScrollStrategy(),this._backdropRef?.dispose(),this._locationChanges?.(),this._keyboardDispatcher.remove(this),this._portalOutlet.dispose(),this._attachments.complete(),this._backdropClick.complete(),this._keydownEvents.complete(),this._outsidePointerEvents.complete(),this._outsideClickDispatcher.remove(this),this._host?.remove(),this._afterNextRenderRef?.destroy(),this._previousHostParent=this._pane=this._host=this._backdropRef=null,n&&this._detachments.next(),this._detachments.complete(),this._completeDetachContent(),this._disposed=!0,it(()=>{vb.update(e=>e.filter(i=>i!==this))})}hasAttached(){return this._portalOutlet.hasAttached()}backdropClick(){return this._backdropClick}attachments(){return this._attachments}detachments(){return this._detachments}keydownEvents(){return this._keydownEvents}outsidePointerEvents(){return this._outsidePointerEvents}getConfig(){return this._config}updatePosition(){this._positionStrategy&&this._positionStrategy.apply()}updatePositionStrategy(n){n!==this._positionStrategy&&(this._positionStrategy&&this._positionStrategy.dispose(),this._positionStrategy=n,this.hasAttached()&&(n.attach(this),this.updatePosition()))}updateSize(n){this._config=r(r({},this._config),n),this._updateElementSize()}setDirection(n){this._config=s(r({},this._config),{direction:n}),this._updateElementDirection()}addPanelClass(n){this._pane&&this._toggleClasses(this._pane,n,!0)}removePanelClass(n){this._pane&&this._toggleClasses(this._pane,n,!1)}getDirection(){let n=this._config.direction;return n?typeof n==`string`?n:n.value:`ltr`}updateScrollStrategy(n){n!==this._scrollStrategy&&(this._disposeScrollStrategy(),this._scrollStrategy=n,this.hasAttached()&&(n.attach(this),n.enable()))}_updateElementDirection(){this._host.setAttribute(`dir`,this.getDirection())}_updateElementSize(){if(!this._pane)return;let n=this._pane.style;n.width=It(this._config.width),n.height=It(this._config.height),n.minWidth=It(this._config.minWidth),n.minHeight=It(this._config.minHeight),n.maxWidth=It(this._config.maxWidth),n.maxHeight=It(this._config.maxHeight)}_togglePointerEvents(n){this._pane.style.pointerEvents=n?``:`none`}_attachHost(){if(!this._host.parentElement){let n=this._config.usePopover?this._positionStrategy?.getPopoverInsertionPoint?.():null;xb(n)?n.after(this._host):n?.type===`parent`?n.element.appendChild(this._host):this._previousHostParent?.appendChild(this._host)}if(this._config.usePopover)try{this._host.showPopover()}catch{}}_attachBackdrop(){let n=`cdk-overlay-backdrop-showing`;this._backdropRef?.dispose(),this._backdropRef=new yb(this._document,this._renderer,this._ngZone,e=>{this._backdropClick.next(e)}),this._animationsDisabled&&this._backdropRef.element.classList.add(`cdk-overlay-backdrop-noop-animation`),this._config.backdropClass&&this._toggleClasses(this._backdropRef.element,this._config.backdropClass,!0),this._config.usePopover?this._host.prepend(this._backdropRef.element):this._host.parentElement.insertBefore(this._backdropRef.element,this._host),!this._animationsDisabled&&typeof requestAnimationFrame<`u`?this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>this._backdropRef?.element.classList.add(n))}):this._backdropRef.element.classList.add(n)}_updateStackingOrder(){!this._config.usePopover&&this._host.nextSibling&&this._host.parentNode.appendChild(this._host)}detachBackdrop(){this._animationsDisabled?(this._backdropRef?.dispose(),this._backdropRef=null):this._backdropRef?.detach()}_toggleClasses(n,e,i){let a=Vr(e||[]).filter(o=>!!o);a.length&&(i?n.classList.add(...a):n.classList.remove(...a))}_detachContentWhenEmpty(){let n=!1;try{this._detachContentAfterRenderRef=Rt(()=>{n=!0,this._detachContent()},{injector:this._injector})}catch(e){if(n)throw e;this._detachContent()}globalThis.MutationObserver&&this._pane&&(this._detachContentMutationObserver||=new globalThis.MutationObserver(()=>{this._detachContent()}),this._detachContentMutationObserver.observe(this._pane,{childList:!0}))}_detachContent(){(!this._pane||!this._host||this._pane.children.length===0)&&(this._pane&&this._config.panelClass&&this._toggleClasses(this._pane,this._config.panelClass,!1),this._host&&this._host.parentElement&&(this._previousHostParent=this._host.parentElement,this._host.remove()),this._completeDetachContent())}_completeDetachContent(){this._detachContentAfterRenderRef?.destroy(),this._detachContentAfterRenderRef=void 0,this._detachContentMutationObserver?.disconnect()}_disposeScrollStrategy(){let n=this._scrollStrategy;n?.disable(),n?.detach?.()}};var mw=`cdk-overlay-connected-position-bounding-box`;var HR=/([A-Za-z%]+)$/;function zo(t,n){return new dm(n,t.get(ui),t.get(me),t.get(Re),t.get(mm))}var dm=class{_viewportRuler;_document;_platform;_overlayContainer;_overlayRef;_isInitialRender=!1;_lastBoundingBoxSize={width:0,height:0};_isPushed=!1;_canPush=!0;_growAfterOpen=!1;_hasFlexibleDimensions=!0;_positionLocked=!1;_originRect;_overlayRect;_viewportRect;_containerRect;_viewportMargin=0;_scrollables=[];_preferredPositions=[];_origin;_pane;_isDisposed=!1;_boundingBox=null;_lastPosition=null;_lastScrollVisibility=null;_positionChanges=new V;_resizeSubscription=De.EMPTY;_offsetX=0;_offsetY=0;_transformOriginSelector;_appliedPanelClasses=[];_previousPushAmount=null;_popoverLocation=`global`;positionChanges=this._positionChanges;get positions(){return this._preferredPositions}constructor(n,e,i,a,o){this._viewportRuler=e,this._document=i,this._platform=a,this._overlayContainer=o,this.setOrigin(n)}attach(n){this._overlayRef&&this._overlayRef,this._validatePositions(),n.hostElement.classList.add(mw),this._overlayRef=n,this._boundingBox=n.hostElement,this._pane=n.overlayElement,this._isDisposed=!1,this._isInitialRender=!0,this._lastPosition=null,this._resizeSubscription.unsubscribe(),this._resizeSubscription=this._viewportRuler.change().subscribe(()=>{this._isInitialRender=!0,this.apply()})}apply(){if(this._isDisposed||!this._platform.isBrowser)return;if(!this._isInitialRender&&this._positionLocked&&this._lastPosition){this.reapplyLastPosition();return}this._clearPanelClasses(),this._resetOverlayElementStyles(),this._resetBoundingBoxStyles(),this._viewportRect=this._getNarrowedViewportRect(),this._originRect=this._getOriginRect(),this._overlayRect=this._pane.getBoundingClientRect(),this._containerRect=this._getContainerRect();let n=this._originRect,e=this._overlayRect,i=this._viewportRect,a=this._containerRect,o=[],r;for(let s of this._preferredPositions){let l=this._getOriginPoint(n,a,s),h=this._getOverlayPoint(l,e,s),p=this._getOverlayFit(h,e,i,s);if(p.isCompletelyWithinViewport){this._isPushed=!1,this._applyPosition(s,l);return}if(this._canFitWithFlexibleDimensions(p,h,i)){o.push({position:s,origin:l,overlayRect:e,boundingBoxRect:this._calculateBoundingBoxRect(l,s)});continue}(!r||r.overlayFit.visibleArea<p.visibleArea)&&(r={overlayFit:p,overlayPoint:h,originPoint:l,position:s,overlayRect:e})}if(o.length){let s=null,l=-1;for(let h of o){let p=h.boundingBoxRect.width*h.boundingBoxRect.height*(h.position.weight||1);p>l&&(l=p,s=h)}this._isPushed=!1,this._applyPosition(s.position,s.origin);return}if(this._canPush){this._isPushed=!0,this._applyPosition(r.position,r.originPoint);return}this._applyPosition(r.position,r.originPoint)}detach(){this._clearPanelClasses(),this._lastPosition=null,this._previousPushAmount=null,this._resizeSubscription.unsubscribe()}dispose(){this._isDisposed||(this._boundingBox&&jo(this._boundingBox.style,{top:``,left:``,right:``,bottom:``,height:``,width:``,alignItems:``,justifyContent:``}),this._pane&&this._resetOverlayElementStyles(),this._overlayRef&&this._overlayRef.hostElement.classList.remove(mw),this.detach(),this._positionChanges.complete(),this._overlayRef=this._boundingBox=null,this._isDisposed=!0)}reapplyLastPosition(){if(this._isDisposed||!this._platform.isBrowser)return;let n=this._lastPosition;n?(this._originRect=this._getOriginRect(),this._overlayRect=this._pane.getBoundingClientRect(),this._viewportRect=this._getNarrowedViewportRect(),this._containerRect=this._getContainerRect(),this._applyPosition(n,this._getOriginPoint(this._originRect,this._containerRect,n))):this.apply()}withScrollableContainers(n){return this._scrollables=n,this}withPositions(n){return this._preferredPositions=n,n.indexOf(this._lastPosition)===-1&&(this._lastPosition=null),this._validatePositions(),this}withViewportMargin(n){return this._viewportMargin=n,this}withFlexibleDimensions(n=!0){return this._hasFlexibleDimensions=n,this}withGrowAfterOpen(n=!0){return this._growAfterOpen=n,this}withPush(n=!0){return this._canPush=n,this}withLockedPosition(n=!0){return this._positionLocked=n,this}setOrigin(n){return this._origin=n,this}withDefaultOffsetX(n){return this._offsetX=n,this}withDefaultOffsetY(n){return this._offsetY=n,this}withTransformOriginOn(n){return this._transformOriginSelector=n,this}withPopoverLocation(n){return this._popoverLocation=n,this}getPopoverInsertionPoint(){return this._popoverLocation===`global`?null:this._popoverLocation!==`inline`?this._popoverLocation:this._origin instanceof Y?this._origin.nativeElement:xb(this._origin)?this._origin:null}_getOriginPoint(n,e,i){let a;if(i.originX==`center`)a=n.left+n.width/2;else{let r=this._isRtl()?n.right:n.left,s=this._isRtl()?n.left:n.right;a=i.originX==`start`?r:s}e.left<0&&(a-=e.left);let o;return i.originY==`center`?o=n.top+n.height/2:o=i.originY==`top`?n.top:n.bottom,e.top<0&&(o-=e.top),{x:a,y:o}}_getOverlayPoint(n,e,i){let a;i.overlayX==`center`?a=-e.width/2:i.overlayX===`start`?a=this._isRtl()?-e.width:0:a=this._isRtl()?0:-e.width;let o;return i.overlayY==`center`?o=-e.height/2:o=i.overlayY==`top`?0:-e.height,{x:n.x+a,y:n.y+o}}_getOverlayFit(n,e,i,a){let o=hw(e),{x:r,y:s}=n,l=this._getOffset(a,`x`),h=this._getOffset(a,`y`);l&&(r+=l),h&&(s+=h);let p=0-r,g=r+o.width-i.width,_=0-s,b=s+o.height-i.height,y=this._subtractOverflows(o.width,p,g),I=this._subtractOverflows(o.height,_,b),P=y*I;return{visibleArea:P,isCompletelyWithinViewport:o.width*o.height===P,fitsInViewportVertically:I===o.height,fitsInViewportHorizontally:y==o.width}}_canFitWithFlexibleDimensions(n,e,i){if(this._hasFlexibleDimensions){let a=i.bottom-e.y,o=i.right-e.x,r=pw(this._overlayRef.getConfig().minHeight),s=pw(this._overlayRef.getConfig().minWidth),l=n.fitsInViewportVertically||r!=null&&r<=a,h=n.fitsInViewportHorizontally||s!=null&&s<=o;return l&&h}return!1}_pushOverlayOnScreen(n,e,i){if(this._previousPushAmount&&this._positionLocked)return{x:n.x+this._previousPushAmount.x,y:n.y+this._previousPushAmount.y};let a=hw(e),o=this._viewportRect,r=Math.max(n.x+a.width-o.width,0),s=Math.max(n.y+a.height-o.height,0),l=Math.max(o.top-i.top-n.y,0),h=Math.max(o.left-i.left-n.x,0),p=0,g=0;return a.width<=o.width?p=h||-r:p=n.x<this._getViewportMarginStart()?o.left-i.left-n.x:0,a.height<=o.height?g=l||-s:g=n.y<this._getViewportMarginTop()?o.top-i.top-n.y:0,this._previousPushAmount={x:p,y:g},{x:n.x+p,y:n.y+g}}_applyPosition(n,e){if(this._setTransformOrigin(n),this._setOverlayElementStyles(e,n),this._setBoundingBoxStyles(e,n),n.panelClass&&this._addPanelClasses(n.panelClass),this._positionChanges.observers.length){let i=this._getScrollVisibility();if(n!==this._lastPosition||!this._lastScrollVisibility||!UR(this._lastScrollVisibility,i)){let a=new cm(n,i);this._positionChanges.next(a)}this._lastScrollVisibility=i}this._lastPosition=n,this._isInitialRender=!1}_setTransformOrigin(n){if(!this._transformOriginSelector)return;let e=this._boundingBox.querySelectorAll(this._transformOriginSelector),i,a=n.overlayY;n.overlayX===`center`?i=`center`:this._isRtl()?i=n.overlayX===`start`?`right`:`left`:i=n.overlayX===`start`?`left`:`right`;for(let o=0;o<e.length;o++)e[o].style.transformOrigin=`${i} ${a}`}_calculateBoundingBoxRect(n,e){let i=this._viewportRect,a=this._isRtl(),o,r,s;if(e.overlayY===`top`)r=n.y,o=i.height-r+this._getViewportMarginBottom();else if(e.overlayY===`bottom`)s=i.height-n.y+this._getViewportMarginTop()+this._getViewportMarginBottom(),o=i.height-s+this._getViewportMarginTop();else{let b=Math.min(i.bottom-n.y+i.top,n.y),y=this._lastBoundingBoxSize.height;o=b*2,r=n.y-b,o>y&&!this._isInitialRender&&!this._growAfterOpen&&(r=n.y-y/2)}let l=e.overlayX===`start`&&!a||e.overlayX===`end`&&a,h=e.overlayX===`end`&&!a||e.overlayX===`start`&&a,p,g,_;if(h)_=i.width-n.x+this._getViewportMarginStart()+this._getViewportMarginEnd(),p=n.x-this._getViewportMarginStart();else if(l)g=n.x,p=i.right-n.x-this._getViewportMarginEnd();else{let b=Math.min(i.right-n.x+i.left,n.x),y=this._lastBoundingBoxSize.width;p=b*2,g=n.x-b,p>y&&!this._isInitialRender&&!this._growAfterOpen&&(g=n.x-y/2)}return{top:r,left:g,bottom:s,right:_,width:p,height:o}}_setBoundingBoxStyles(n,e){let i=this._calculateBoundingBoxRect(n,e);!this._isInitialRender&&!this._growAfterOpen&&(i.height=Math.min(i.height,this._lastBoundingBoxSize.height),i.width=Math.min(i.width,this._lastBoundingBoxSize.width));let a={};if(this._hasExactPosition())a.top=a.left=`0`,a.bottom=a.right=`auto`,a.maxHeight=a.maxWidth=``,a.width=a.height=`100%`;else{let o=this._overlayRef.getConfig().maxHeight,r=this._overlayRef.getConfig().maxWidth;a.width=It(i.width),a.height=It(i.height),a.top=It(i.top)||`auto`,a.bottom=It(i.bottom)||`auto`,a.left=It(i.left)||`auto`,a.right=It(i.right)||`auto`,e.overlayX===`center`?a.alignItems=`center`:a.alignItems=e.overlayX===`end`?`flex-end`:`flex-start`,e.overlayY===`center`?a.justifyContent=`center`:a.justifyContent=e.overlayY===`bottom`?`flex-end`:`flex-start`,o&&(a.maxHeight=It(o)),r&&(a.maxWidth=It(r))}this._lastBoundingBoxSize=i,jo(this._boundingBox.style,a)}_resetBoundingBoxStyles(){jo(this._boundingBox.style,{top:`0`,left:`0`,right:`0`,bottom:`0`,height:``,width:``,alignItems:``,justifyContent:``})}_resetOverlayElementStyles(){jo(this._pane.style,{top:``,left:``,bottom:``,right:``,position:``,transform:``})}_setOverlayElementStyles(n,e){let i={},a=this._hasExactPosition(),o=this._hasFlexibleDimensions,r=this._overlayRef.getConfig();if(a){let p=this._viewportRuler.getViewportScrollPosition();jo(i,this._getExactOverlayY(e,n,p)),jo(i,this._getExactOverlayX(e,n,p))}else i.position=`static`;let s=``,l=this._getOffset(e,`x`),h=this._getOffset(e,`y`);l&&(s+=`translateX(${l}px) `),h&&(s+=`translateY(${h}px)`),i.transform=s.trim(),r.maxHeight&&(a?i.maxHeight=It(r.maxHeight):o&&(i.maxHeight=``)),r.maxWidth&&(a?i.maxWidth=It(r.maxWidth):o&&(i.maxWidth=``)),jo(this._pane.style,i)}_getExactOverlayY(n,e,i){let a={top:``,bottom:``},o=this._getOverlayPoint(e,this._overlayRect,n);if(this._isPushed&&(o=this._pushOverlayOnScreen(o,this._overlayRect,i)),n.overlayY===`bottom`)a.bottom=`${this._document.documentElement.clientHeight-(o.y+this._overlayRect.height)}px`;else a.top=It(o.y);return a}_getExactOverlayX(n,e,i){let a={left:``,right:``},o=this._getOverlayPoint(e,this._overlayRect,n);this._isPushed&&(o=this._pushOverlayOnScreen(o,this._overlayRect,i));let r;if(this._isRtl()?r=n.overlayX===`end`?`left`:`right`:r=n.overlayX===`end`?`right`:`left`,r===`right`)a.right=`${this._document.documentElement.clientWidth-(o.x+this._overlayRect.width)}px`;else a.left=It(o.x);return a}_getScrollVisibility(){let n=this._getOriginRect(),e=this._pane.getBoundingClientRect(),i=this._scrollables.map(a=>a.getElementRef().nativeElement.getBoundingClientRect());return{isOriginClipped:dw(n,i),isOriginOutsideView:_b(n,i),isOverlayClipped:dw(e,i),isOverlayOutsideView:_b(e,i)}}_subtractOverflows(n,...e){return e.reduce((i,a)=>i-Math.max(a,0),n)}_getNarrowedViewportRect(){let n=this._document.documentElement.clientWidth,e=this._document.documentElement.clientHeight,i=this._viewportRuler.getViewportScrollPosition();return{top:i.top+this._getViewportMarginTop(),left:i.left+this._getViewportMarginStart(),right:i.left+n-this._getViewportMarginEnd(),bottom:i.top+e-this._getViewportMarginBottom(),width:n-this._getViewportMarginStart()-this._getViewportMarginEnd(),height:e-this._getViewportMarginTop()-this._getViewportMarginBottom()}}_isRtl(){return this._overlayRef.getDirection()===`rtl`}_hasExactPosition(){return!this._hasFlexibleDimensions||this._isPushed}_getOffset(n,e){return e===`x`?n.offsetX==null?this._offsetX:n.offsetX:n.offsetY==null?this._offsetY:n.offsetY}_validatePositions(){}_addPanelClasses(n){this._pane&&Vr(n).forEach(e=>{e!==``&&this._appliedPanelClasses.indexOf(e)===-1&&(this._appliedPanelClasses.push(e),this._pane.classList.add(e))})}_clearPanelClasses(){this._pane&&(this._appliedPanelClasses.forEach(n=>{this._pane.classList.remove(n)}),this._appliedPanelClasses=[])}_getViewportMarginStart(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.start??0}_getViewportMarginEnd(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.end??0}_getViewportMarginTop(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.top??0}_getViewportMarginBottom(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.bottom??0}_getOriginRect(){let n=this._origin;if(n instanceof Y)return n.nativeElement.getBoundingClientRect();if(n instanceof Element)return n.getBoundingClientRect();let e=n.width||0,i=n.height||0;return{top:n.y,bottom:n.y+i,left:n.x,right:n.x+e,height:i,width:e}}_getContainerRect(){let n=this._overlayRef.getConfig().usePopover&&this._popoverLocation!==`global`,e=this._overlayContainer.getContainerElement();n&&(e.style.display=`block`);let i=e.getBoundingClientRect();return n&&(e.style.display=``),i}};function jo(t,n){for(let e in n)Object.hasOwn(n,e)&&(t[e]=n[e]);return t}function pw(t){if(typeof t!=`number`&&t!=null){let[n,e]=t.split(HR);return!e||e===`px`?parseFloat(n):null}return t||null}function hw(t){return{top:Math.floor(t.top),right:Math.floor(t.right),bottom:Math.floor(t.bottom),left:Math.floor(t.left),width:Math.floor(t.width),height:Math.floor(t.height)}}function UR(t,n){return t===n?!0:t.isOriginClipped===n.isOriginClipped&&t.isOriginOutsideView===n.isOriginOutsideView&&t.isOverlayClipped===n.isOverlayClipped&&t.isOverlayOutsideView===n.isOverlayOutsideView}var fw=`cdk-global-overlay-wrapper`;function os(t){return new um}var um=class{_overlayRef;_cssPosition=`static`;_topOffset=``;_bottomOffset=``;_alignItems=``;_xPosition=``;_xOffset=``;_width=``;_height=``;_isDisposed=!1;attach(n){let e=n.getConfig();this._overlayRef=n,this._width&&!e.width&&n.updateSize({width:this._width}),this._height&&!e.height&&n.updateSize({height:this._height}),n.hostElement.classList.add(fw),this._isDisposed=!1}top(n=``){return this._bottomOffset=``,this._topOffset=n,this._alignItems=`flex-start`,this}left(n=``){return this._xOffset=n,this._xPosition=`left`,this}bottom(n=``){return this._topOffset=``,this._bottomOffset=n,this._alignItems=`flex-end`,this}right(n=``){return this._xOffset=n,this._xPosition=`right`,this}start(n=``){return this._xOffset=n,this._xPosition=`start`,this}end(n=``){return this._xOffset=n,this._xPosition=`end`,this}width(n=``){return this._overlayRef?this._overlayRef.updateSize({width:n}):this._width=n,this}height(n=``){return this._overlayRef?this._overlayRef.updateSize({height:n}):this._height=n,this}centerHorizontally(n=``){return this.left(n),this._xPosition=`center`,this}centerVertically(n=``){return this.top(n),this._alignItems=`center`,this}apply(){if(!this._overlayRef||!this._overlayRef.hasAttached())return;let n=this._overlayRef.overlayElement.style,e=this._overlayRef.hostElement.style,{width:a,height:o,maxWidth:r,maxHeight:s}=this._overlayRef.getConfig(),l=(a===`100%`||a===`100vw`)&&(!r||r===`100%`||r===`100vw`),h=(o===`100%`||o===`100vh`)&&(!s||s===`100%`||s===`100vh`),p=this._xPosition,g=this._xOffset,_=this._overlayRef.getConfig().direction===`rtl`,b=``,y=``,I=``;l?I=`flex-start`:p===`center`?(I=`center`,_?y=g:b=g):_?p===`left`||p===`end`?(I=`flex-end`,b=g):(p===`right`||p===`start`)&&(I=`flex-start`,y=g):p===`left`||p===`start`?(I=`flex-start`,b=g):(p===`right`||p===`end`)&&(I=`flex-end`,y=g),n.position=this._cssPosition,n.marginLeft=l?`0`:b,n.marginTop=h?`0`:this._topOffset,n.marginBottom=this._bottomOffset,n.marginRight=l?`0`:y,e.justifyContent=I,e.alignItems=h?`flex-start`:this._alignItems}dispose(){if(this._isDisposed||!this._overlayRef)return;let n=this._overlayRef.overlayElement.style,e=this._overlayRef.hostElement,i=e.style;e.classList.remove(fw),i.justifyContent=i.alignItems=n.marginTop=n.marginBottom=n.marginLeft=n.marginRight=n.position=``,this._overlayRef=null,this._isDisposed=!0}};var Cw=(()=>{class t{_injector=f(se);global(){return os()}flexibleConnectedTo(e){return zo(this._injector,e)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Kl=new N(`OVERLAY_DEFAULT_CONFIG`);function sa(t,n){t.get(Je).load(xw);let e=t.get(mm),i=t.get(me),a=t.get(je),o=t.get(On),r=t.get(gt),s=t.get(Pe,null,{optional:!0})||t.get(Nt).createRenderer(null,null),l=new aa(n),h=t.get(Kl,null,{optional:!0})?.usePopover??!0;l.direction=l.direction||r.value,!i.body||!(`showPopover`in i.body)?l.usePopover=!1:l.usePopover=n?.usePopover??h;let p=i.createElement(`div`),g=i.createElement(`div`);p.id=a.getId(`cdk-overlay-`),p.classList.add(`cdk-overlay-pane`),g.appendChild(p),l.usePopover&&(g.setAttribute(`popover`,`manual`),g.classList.add(`cdk-overlay-popover`));let _=l.usePopover?l.positionStrategy?.getPopoverInsertionPoint?.():null;return xb(_)?_.after(g):_?.type===`parent`?_.element.appendChild(g):e.getContainerElement().appendChild(g),new ns(new Wl(p,o,t),g,p,l,t.get(Z),t.get(_w),i,t.get(su),t.get(yw),n?.disableAnimations??t.get(js,null,{optional:!0})===`NoopAnimations`,t.get(Mt),s)}var Sw=(()=>{class t{scrollStrategies=f(bw);_positionBuilder=f(Cw);_injector=f(se);create(e){return sa(this._injector,e)}position(){return this._positionBuilder}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var $R=[{originX:`start`,originY:`bottom`,overlayX:`start`,overlayY:`top`},{originX:`start`,originY:`top`,overlayX:`start`,overlayY:`bottom`},{originX:`end`,originY:`top`,overlayX:`end`,overlayY:`bottom`},{originX:`end`,originY:`bottom`,overlayX:`end`,overlayY:`top`}];var GR=new N(`cdk-connected-overlay-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t)}});var is=(()=>{class t{elementRef=f(Y);static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdk-overlay-origin`,``],[``,`overlay-origin`,``],[``,`cdkOverlayOrigin`,``]],exportAs:[`cdkOverlayOrigin`]})}return t})();var ww=new N(`cdk-connected-overlay-default-config`);var pm=(()=>{class t{_dir=f(gt,{optional:!0});_injector=f(se);_overlayRef;_templatePortal;_backdropSubscription=De.EMPTY;_attachSubscription=De.EMPTY;_detachSubscription=De.EMPTY;_positionSubscription=De.EMPTY;_offsetX;_offsetY;_position;_scrollStrategyFactory=f(GR);_ngZone=f(Z);origin;positions;positionStrategy;get offsetX(){return this._offsetX}set offsetX(e){this._offsetX=e,this._position&&this._updatePositionStrategy(this._position)}get offsetY(){return this._offsetY}set offsetY(e){this._offsetY=e,this._position&&this._updatePositionStrategy(this._position)}width;height;minWidth;minHeight;backdropClass;panelClass;viewportMargin=0;scrollStrategy;open=!1;disableClose=!1;transformOriginSelector;hasBackdrop=!1;lockPosition=!1;flexibleDimensions=!1;growAfterOpen=!1;push=!1;disposeOnNavigation=!1;usePopover;matchWidth=!1;set _config(e){typeof e!=`string`&&this._assignConfig(e)}backdropClick=new te;positionChange=new te;attach=new te;detach=new te;overlayKeydown=new te;overlayOutsideClick=new te;constructor(){let e=f(Vt),i=f(Bt),a=f(ww,{optional:!0}),o=f(Kl,{optional:!0});this.usePopover=o?.usePopover===!1?null:`global`,this._templatePortal=new Hn(e,i),this.scrollStrategy=this._scrollStrategyFactory(),a&&this._assignConfig(a)}get overlayRef(){return this._overlayRef}get dir(){return this._dir?this._dir.value:`ltr`}ngOnDestroy(){this._attachSubscription.unsubscribe(),this._detachSubscription.unsubscribe(),this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this._overlayRef?.dispose()}ngOnChanges(e){this._position&&(this._updatePositionStrategy(this._position),this._overlayRef?.updateSize({width:this._getWidth(),minWidth:this.minWidth,height:this.height,minHeight:this.minHeight}),e.origin&&this.open&&this._position.apply()),e.open&&(this.open?this.attachOverlay():this.detachOverlay())}_createOverlay(){(!this.positions||!this.positions.length)&&(this.positions=$R);let e=this._overlayRef=sa(this._injector,this._buildConfig());this._attachSubscription=e.attachments().subscribe(()=>this.attach.emit()),this._detachSubscription=e.detachments().subscribe(()=>this.detach.emit()),e.keydownEvents().subscribe(i=>{this.overlayKeydown.next(i),i.keyCode===27&&!this.disableClose&&!Ct(i)&&(i.preventDefault(),this.detachOverlay())}),this._overlayRef.outsidePointerEvents().subscribe(i=>{let a=this._getOriginElement(),o=Yt(i);(!a||a!==o&&!a.contains(o))&&this.overlayOutsideClick.next(i)})}_buildConfig(){let e=this._position=this.positionStrategy||this._createPositionStrategy(),i=new aa({direction:this._dir||`ltr`,positionStrategy:e,scrollStrategy:this.scrollStrategy,hasBackdrop:this.hasBackdrop,disposeOnNavigation:this.disposeOnNavigation,usePopover:!!this.usePopover});return(this.height||this.height===0)&&(i.height=this.height),(this.minWidth||this.minWidth===0)&&(i.minWidth=this.minWidth),(this.minHeight||this.minHeight===0)&&(i.minHeight=this.minHeight),this.backdropClass&&(i.backdropClass=this.backdropClass),this.panelClass&&(i.panelClass=this.panelClass),i}_updatePositionStrategy(e){let i=this.positions.map(a=>({originX:a.originX,originY:a.originY,overlayX:a.overlayX,overlayY:a.overlayY,offsetX:a.offsetX||this.offsetX,offsetY:a.offsetY||this.offsetY,panelClass:a.panelClass||void 0}));return e.setOrigin(this._getOrigin()).withPositions(i).withFlexibleDimensions(this.flexibleDimensions).withPush(this.push).withGrowAfterOpen(this.growAfterOpen).withViewportMargin(this.viewportMargin).withLockedPosition(this.lockPosition).withTransformOriginOn(this.transformOriginSelector).withPopoverLocation(this.usePopover===null?`global`:this.usePopover)}_createPositionStrategy(){let e=zo(this._injector,this._getOrigin());return this._updatePositionStrategy(e),e}_getOrigin(){return this.origin instanceof is?this.origin.elementRef:this.origin}_getOriginElement(){return this.origin instanceof is?this.origin.elementRef.nativeElement:this.origin instanceof Y?this.origin.nativeElement:typeof Element<`u`&&this.origin instanceof Element?this.origin:null}_getWidth(){return this.width?this.width:this.matchWidth?this._getOriginElement()?.getBoundingClientRect?.().width:void 0}attachOverlay(){this._overlayRef||this._createOverlay();let e=this._overlayRef;e.getConfig().hasBackdrop=this.hasBackdrop,e.updateSize({width:this._getWidth()}),e.hasAttached()||e.attach(this._templatePortal),this.hasBackdrop?this._backdropSubscription=e.backdropClick().subscribe(i=>this.backdropClick.emit(i)):this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this.positionChange.observers.length>0&&(this._positionSubscription=this._position.positionChanges.pipe(bp(()=>this.positionChange.observers.length>0)).subscribe(i=>{this._ngZone.run(()=>this.positionChange.emit(i)),this.positionChange.observers.length===0&&this._positionSubscription.unsubscribe()})),this.open=!0}detachOverlay(){this._overlayRef?.detach(),this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this.open=!1}_assignConfig(e){this.origin=e.origin??this.origin,this.positions=e.positions??this.positions,this.positionStrategy=e.positionStrategy??this.positionStrategy,this.offsetX=e.offsetX??this.offsetX,this.offsetY=e.offsetY??this.offsetY,this.width=e.width??this.width,this.height=e.height??this.height,this.minWidth=e.minWidth??this.minWidth,this.minHeight=e.minHeight??this.minHeight,this.backdropClass=e.backdropClass??this.backdropClass,this.panelClass=e.panelClass??this.panelClass,this.viewportMargin=e.viewportMargin??this.viewportMargin,this.scrollStrategy=e.scrollStrategy??this.scrollStrategy,this.disableClose=e.disableClose??this.disableClose,this.transformOriginSelector=e.transformOriginSelector??this.transformOriginSelector,this.hasBackdrop=e.hasBackdrop??this.hasBackdrop,this.lockPosition=e.lockPosition??this.lockPosition,this.flexibleDimensions=e.flexibleDimensions??this.flexibleDimensions,this.growAfterOpen=e.growAfterOpen??this.growAfterOpen,this.push=e.push??this.push,this.disposeOnNavigation=e.disposeOnNavigation??this.disposeOnNavigation,this.usePopover=e.usePopover??this.usePopover,this.matchWidth=e.matchWidth??this.matchWidth}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdk-connected-overlay`,``],[``,`connected-overlay`,``],[``,`cdkConnectedOverlay`,``]],inputs:{origin:[0,`cdkConnectedOverlayOrigin`,`origin`],positions:[0,`cdkConnectedOverlayPositions`,`positions`],positionStrategy:[0,`cdkConnectedOverlayPositionStrategy`,`positionStrategy`],offsetX:[0,`cdkConnectedOverlayOffsetX`,`offsetX`],offsetY:[0,`cdkConnectedOverlayOffsetY`,`offsetY`],width:[0,`cdkConnectedOverlayWidth`,`width`],height:[0,`cdkConnectedOverlayHeight`,`height`],minWidth:[0,`cdkConnectedOverlayMinWidth`,`minWidth`],minHeight:[0,`cdkConnectedOverlayMinHeight`,`minHeight`],backdropClass:[0,`cdkConnectedOverlayBackdropClass`,`backdropClass`],panelClass:[0,`cdkConnectedOverlayPanelClass`,`panelClass`],viewportMargin:[0,`cdkConnectedOverlayViewportMargin`,`viewportMargin`],scrollStrategy:[0,`cdkConnectedOverlayScrollStrategy`,`scrollStrategy`],open:[0,`cdkConnectedOverlayOpen`,`open`],disableClose:[0,`cdkConnectedOverlayDisableClose`,`disableClose`],transformOriginSelector:[0,`cdkConnectedOverlayTransformOriginOn`,`transformOriginSelector`],hasBackdrop:[2,`cdkConnectedOverlayHasBackdrop`,`hasBackdrop`,z],lockPosition:[2,`cdkConnectedOverlayLockPosition`,`lockPosition`,z],flexibleDimensions:[2,`cdkConnectedOverlayFlexibleDimensions`,`flexibleDimensions`,z],growAfterOpen:[2,`cdkConnectedOverlayGrowAfterOpen`,`growAfterOpen`,z],push:[2,`cdkConnectedOverlayPush`,`push`,z],disposeOnNavigation:[2,`cdkConnectedOverlayDisposeOnNavigation`,`disposeOnNavigation`,e=>e===`url-change`||e===`pop-state`?e:z(e)],usePopover:[0,`cdkConnectedOverlayUsePopover`,`usePopover`],matchWidth:[2,`cdkConnectedOverlayMatchWidth`,`matchWidth`,z],_config:[0,`cdkConnectedOverlay`,`_config`]},outputs:{backdropClick:`backdropClick`,positionChange:`positionChange`,attach:`attach`,detach:`detach`,overlayKeydown:`overlayKeydown`,overlayOutsideClick:`overlayOutsideClick`},exportAs:[`cdkConnectedOverlay`],features:[_t]})}return t})();var mi=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({providers:[Sw],imports:[we,Bo,bb,bb]})}return t})();var Sb=new N(`MAT_MENU_PANEL`);var la=(()=>{class t{_isAnchor;_elementRef=f(Y);_document=f(me);_focusMonitor=f(tn);_parentMenu=f(Sb,{optional:!0});_changeDetectorRef=f(Le);role=`menuitem`;disabled=!1;disabledInteractive=!1;disableRipple=!1;_hovered=new V;_focused=new V;_highlighted=!1;_triggersSubmenu=!1;constructor(){f(Je).load(pn),this._parentMenu?.addItem?.(this),this._isAnchor=this._elementRef.nativeElement.tagName===`A`}focus(e,i){this._focusMonitor&&e?this._focusMonitor.focusVia(this._getHostElement(),e,i):this._getHostElement().focus(i),this._focused.next(this)}ngAfterViewInit(){this._focusMonitor&&this._focusMonitor.monitor(this._elementRef,!1)}ngOnDestroy(){this._focusMonitor&&this._focusMonitor.stopMonitoring(this._elementRef),this._parentMenu&&this._parentMenu.removeItem&&this._parentMenu.removeItem(this),this._hovered.complete(),this._focused.complete()}_getTabIndex(){return this.disabled&&!this.disabledInteractive?`-1`:`0`}_getAriaDisabled(){return this._isAnchor?this.disabled||null:this.disabled&&this.disabledInteractive?!0:null}_getDisabled(){return this.disabledInteractive||!this.disabled?null:!0}_getHostElement(){return this._elementRef.nativeElement}_checkDisabled(e){this.disabled&&(e.preventDefault(),e.stopPropagation())}_handleMouseEnter(){this._hovered.next(this)}getLabel(){let e=this._elementRef.nativeElement.cloneNode(!0),i=e.querySelectorAll(`mat-icon, .material-icons, .material-symbols-outlined, .material-symbols-rounded, .material-symbols-sharp`);for(let a=0;a<i.length;a++)i[a].remove();return e.textContent?.trim()||``}_setHighlighted(e){this._highlighted=e,this._changeDetectorRef.markForCheck()}_setTriggersSubmenu(e){this._triggersSubmenu=e,this._changeDetectorRef.markForCheck()}_hasFocus(){return this._document&&this._document.activeElement===this._getHostElement()}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[`mat-icon`],[``,`matMenuItemIcon`,``]],`*`],i=[`mat-icon, [matMenuItemIcon]`,`*`];function a(o,r){o&1&&(Gt(),c(0,`svg`,2),U(1,`polygon`,3),d())}return H({type:t,selectors:[[``,`mat-menu-item`,``]],hostAttrs:[1,`mat-mdc-menu-item`,`mat-focus-indicator`],hostVars:12,hostBindings:function(r,s){r&1&&C(`click`,function(h){return s._checkDisabled(h)})(`mouseenter`,function(){return s._handleMouseEnter()}),r&2&&(ne(`role`,s.role)(`tabindex`,s._getTabIndex())(`aria-disabled`,s._getAriaDisabled())(`disabled`,s._getDisabled()),G(`mat-mdc-menu-item-highlighted`,s._highlighted)(`mat-mdc-menu-item-submenu-trigger`,s._triggersSubmenu)(`mat-mdc-menu-item-disabled`,s.disabled)(`mat-mdc-menu-item-disabled-interactive`,s.disabledInteractive))},inputs:{role:`role`,disabled:[2,`disabled`,`disabled`,z],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z],disableRipple:[2,`disableRipple`,`disableRipple`,z]},exportAs:[`matMenuItem`],ngContentSelectors:i,decls:5,vars:3,consts:[[1,`mat-mdc-menu-item-text`],[`matRipple`,``,1,`mat-mdc-menu-ripple`,3,`matRippleDisabled`,`matRippleTrigger`],[`viewBox`,`0 0 5 10`,`focusable`,`false`,`aria-hidden`,`true`,1,`mat-mdc-menu-submenu-icon`],[`points`,`0,0 5,5 0,10`]],template:function(r,s){r&1&&(Fe(e),he(0),c(1,`span`,0),he(2,1),d(),U(3,`div`,1),E(4,a,2,0,`:svg:svg`,2)),r&2&&(m(3),S(`matRippleDisabled`,s.disableRipple||s.disabled)(`matRippleTrigger`,s._getHostElement()),m(),D(s._triggersSubmenu?4:-1))},dependencies:[li],encapsulation:2})})()}return t})();var WR=new N(`MatMenuContent`);var qR=new N(`mat-menu-default-options`,{providedIn:`root`,factory:()=>({overlapTrigger:!1,xPosition:`after`,yPosition:`below`,backdropClass:`cdk-overlay-transparent-backdrop`})});var Cb=`_mat-menu-enter`;var hm=`_mat-menu-exit`;var Ni=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Le);_injector=f(se);_keyManager;_xPosition;_yPosition;_firstItemFocusRef;_exitFallbackTimeout;_animationsDisabled=Ue();_allItems;_directDescendantItems=new wn;_classList={};_panelAnimationState=`void`;_animationDone=new V;_isAnimating=T(!1);parentMenu;direction;overlayPanelClass;backdropClass;ariaLabel;ariaLabelledby;ariaDescribedby;get xPosition(){return this._xPosition}set xPosition(e){this._xPosition=e,this.setPositionClasses()}get yPosition(){return this._yPosition}set yPosition(e){this._yPosition=e,this.setPositionClasses()}templateRef;items;lazyContent;overlapTrigger=!1;hasBackdrop;get panelClass(){return this._previousPanelClass}set panelClass(e){let i=this._previousPanelClass,a=r({},this._classList);i&&i.length&&i.split(` `).forEach(o=>{a[o]=!1}),this._previousPanelClass=e,e&&e.length&&(e.split(` `).forEach(o=>{a[o]=!0}),this._elementRef.nativeElement.className=``),this._classList=a}_previousPanelClass=``;get classList(){return this.panelClass}set classList(e){this.panelClass=e}closed=new te;close=this.closed;panelId=f(je).getId(`mat-menu-panel-`);constructor(){let e=f(qR);this.overlayPanelClass=e.overlayPanelClass||``,this._xPosition=e.xPosition,this._yPosition=e.yPosition,this.backdropClass=e.backdropClass,this.overlapTrigger=e.overlapTrigger,this.hasBackdrop=e.hasBackdrop}ngOnInit(){this.setPositionClasses()}ngAfterContentInit(){this._updateDirectDescendants(),this._keyManager=new ea(this._directDescendantItems).withWrap().withTypeAhead().withHomeAndEnd().skipPredicate(e=>e.disabled&&!e.disabledInteractive),this._keyManager.tabOut.subscribe(()=>this.closed.emit(`tab`)),this._directDescendantItems.changes.pipe(mt(this._directDescendantItems),_n(e=>kt(...e.map(i=>i._focused)))).subscribe(e=>this._keyManager.updateActiveItem(e)),this._directDescendantItems.changes.subscribe(e=>{let i=this._keyManager;if(this._panelAnimationState===`enter`&&i.activeItem?._hasFocus()){let a=e.toArray(),o=Math.max(0,Math.min(a.length-1,i.activeItemIndex||0));a[o]&&!a[o].disabled?i.setActiveItem(o):i.setNextItemActive()}})}ngOnDestroy(){this._keyManager?.destroy(),this._directDescendantItems.destroy(),this.closed.complete(),this._firstItemFocusRef?.destroy(),clearTimeout(this._exitFallbackTimeout)}_hovered(){return this._directDescendantItems.changes.pipe(mt(this._directDescendantItems),_n(i=>kt(...i.map(a=>a._hovered))))}addItem(e){}removeItem(e){}_handleKeydown(e){let i=e.keyCode,a=this._keyManager;switch(i){case 27:Ct(e)||(e.preventDefault(),this.closed.emit(`keydown`));break;case 37:this.parentMenu&&this.direction===`ltr`&&this.closed.emit(`keydown`);break;case 39:this.parentMenu&&this.direction===`rtl`&&this.closed.emit(`keydown`);break;default:(i===38||i===40)&&a.setFocusOrigin(`keyboard`),a.onKeydown(e);return}}focusFirstItem(e=`program`){this._firstItemFocusRef?.destroy(),this._firstItemFocusRef=Rt(()=>{let i=this._resolvePanel();if(!i||!i.contains(document.activeElement)){let a=this._keyManager;a.setFocusOrigin(e).setFirstItemActive(),!a.activeItem&&i&&i.focus()}},{injector:this._injector})}resetActiveItem(){this._keyManager.setActiveItem(-1)}setElevation(e){}setPositionClasses(e=this.xPosition,i=this.yPosition){this._classList=s(r({},this._classList),{"mat-menu-before":e===`before`,"mat-menu-after":e===`after`,"mat-menu-above":i===`above`,"mat-menu-below":i===`below`}),this._changeDetectorRef.markForCheck()}_onAnimationDone(e){let i=e===hm;(i||e===Cb)&&(i&&(clearTimeout(this._exitFallbackTimeout),this._exitFallbackTimeout=void 0),this._animationDone.next(i?`void`:`enter`),this._isAnimating.set(!1))}_onAnimationStart(e){(e===Cb||e===hm)&&this._isAnimating.set(!0)}_setIsOpen(e){if(this._panelAnimationState=e?`enter`:`void`,e){if(this._keyManager.activeItemIndex===0){let i=this._resolvePanel();i&&(i.scrollTop=0)}}else this._animationsDisabled||(this._exitFallbackTimeout=setTimeout(()=>this._onAnimationDone(hm),200));this._animationsDisabled&&setTimeout(()=>{this._onAnimationDone(e?Cb:hm)}),this._changeDetectorRef.markForCheck()}_updateDirectDescendants(){this._allItems.changes.pipe(mt(this._allItems)).subscribe(e=>{this._directDescendantItems.reset(e.filter(i=>i._parentMenu===this)),this._directDescendantItems.notifyOnChanges()})}_resolvePanel(){let e=null;return this._directDescendantItems.length&&(e=this._directDescendantItems.first._getHostElement().closest(`[role="menu"]`)),e}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`];function i(a,o){if(a&1){let r=$();ct(0,`div`,0),Tr(`click`,function(){k(r);return M(v().closed.emit(`click`))})(`animationstart`,function(l){k(r);return M(v()._onAnimationStart(l.animationName))})(`animationend`,function(l){k(r);return M(v()._onAnimationDone(l.animationName))})(`animationcancel`,function(l){k(r);return M(v()._onAnimationDone(l.animationName))}),ct(1,`div`,1),he(2),ht()()}if(a&2){let r=v();tt(r._classList),G(`mat-menu-panel-animations-disabled`,r._animationsDisabled)(`mat-menu-panel-exit-animation`,r._panelAnimationState===`void`)(`mat-menu-panel-animating`,r._isAnimating()),qt(`id`,r.panelId),ne(`aria-label`,r.ariaLabel||null)(`aria-labelledby`,r.ariaLabelledby||null)(`aria-describedby`,r.ariaDescribedby||null)}}return H({type:t,selectors:[[`mat-menu`]],contentQueries:function(o,r,s){if(o&1&&jt(s,WR,5)(s,la,5)(s,la,4),o&2){let l;J(l=ee())&&(r.lazyContent=l.first),J(l=ee())&&(r._allItems=l),J(l=ee())&&(r.items=l)}},viewQuery:function(o,r){if(o&1&&Ze(Vt,5),o&2){let s;J(s=ee())&&(r.templateRef=s.first)}},hostVars:3,hostBindings:function(o,r){o&2&&ne(`aria-label`,null)(`aria-labelledby`,null)(`aria-describedby`,null)},inputs:{backdropClass:`backdropClass`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],ariaDescribedby:[0,`aria-describedby`,`ariaDescribedby`],xPosition:`xPosition`,yPosition:`yPosition`,overlapTrigger:[2,`overlapTrigger`,`overlapTrigger`,z],hasBackdrop:[2,`hasBackdrop`,`hasBackdrop`,a=>a==null?null:z(a)],panelClass:[0,`class`,`panelClass`],classList:`classList`},outputs:{closed:`closed`,close:`close`},exportAs:[`matMenu`],features:[Ge([{provide:Sb,useExisting:t}])],ngContentSelectors:e,decls:1,vars:0,consts:[[`tabindex`,`-1`,`role`,`menu`,1,`mat-mdc-menu-panel`,3,`click`,`animationstart`,`animationend`,`animationcancel`,`id`],[1,`mat-mdc-menu-content`]],template:function(o,r){o&1&&(Fe(),kr(0,i,3,12,`ng-template`))},styles:[`mat-menu {
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
`],encapsulation:2})})()}return t})();var YR=new N(`mat-menu-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t)}});var rs=new WeakMap;var KR=(()=>{class t{_canHaveBackdrop;_element=f(Y);_viewContainerRef=f(Bt);_menuItemInstance=f(la,{optional:!0,self:!0});_dir=f(gt,{optional:!0});_focusMonitor=f(tn);_ngZone=f(Z);_injector=f(se);_scrollStrategy=f(YR);_changeDetectorRef=f(Le);_animationsDisabled=Ue();_portal;_overlayRef=null;_menuOpen=!1;_closingActionsSubscription=De.EMPTY;_menuCloseSubscription=De.EMPTY;_pendingRemoval;_parentMaterialMenu;_parentInnerPadding;_openedBy=void 0;get _menu(){return this._menuInternal}set _menu(e){e!==this._menuInternal&&(this._menuInternal=e,this._menuCloseSubscription.unsubscribe(),e?(this._parentMaterialMenu,this._menuCloseSubscription=e.close.subscribe(i=>{this._destroyMenu(i),(i===`click`||i===`tab`)&&this._parentMaterialMenu&&this._parentMaterialMenu.closed.emit(i)})):this._destroyMenu(),this._menuItemInstance?._setTriggersSubmenu(this._triggersSubmenu()))}_menuInternal=null;constructor(e){this._canHaveBackdrop=e;let i=f(Sb,{optional:!0});this._parentMaterialMenu=i instanceof Ni?i:void 0}ngOnDestroy(){this._menu&&this._ownsMenu(this._menu)&&rs.delete(this._menu),this._pendingRemoval?.unsubscribe(),this._menuCloseSubscription.unsubscribe(),this._closingActionsSubscription.unsubscribe(),this._overlayRef&&(this._overlayRef.dispose(),this._overlayRef=null)}get menuOpen(){return this._menuOpen}get dir(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_triggersSubmenu(){return!!(this._menuItemInstance&&this._parentMaterialMenu&&this._menu)}_closeMenu(){this._menu?.close.emit()}_openMenu(e){if(this._triggerIsAriaDisabled())return;let i=this._menu;if(this._menuOpen||!i)return;this._pendingRemoval?.unsubscribe();let a=rs.get(i);rs.set(i,this),a&&a!==this&&a._closeMenu();let o=this._createOverlay(i),r=o.getConfig(),s=r.positionStrategy;this._setPosition(i,s),this._canHaveBackdrop?r.hasBackdrop=i.hasBackdrop==null?!this._triggersSubmenu():i.hasBackdrop:r.hasBackdrop=i.hasBackdrop??!1,o.hasAttached()||(o.attach(this._getPortal(i)),i.lazyContent?.attach(this.menuData)),this._closingActionsSubscription=this._menuClosingActions().subscribe(()=>this._closeMenu()),i.parentMenu=this._triggersSubmenu()?this._parentMaterialMenu:void 0,i.direction=this.dir,e&&i.focusFirstItem(this._openedBy||`program`),this._setIsMenuOpen(!0),i instanceof Ni&&(i._setIsOpen(!0),i._directDescendantItems.changes.pipe(Ne(i.close)).subscribe(()=>{s.withLockedPosition(!1).reapplyLastPosition(),s.withLockedPosition(!0)}))}focus(e,i){this._focusMonitor&&e?this._focusMonitor.focusVia(this._element,e,i):this._element.nativeElement.focus(i)}_destroyMenu(e){let i=this._overlayRef,a=this._menu;!i||!this.menuOpen||(this._closingActionsSubscription.unsubscribe(),this._pendingRemoval?.unsubscribe(),a instanceof Ni&&this._ownsMenu(a)?(this._pendingRemoval=a._animationDone.pipe(vn(1)).subscribe(()=>{i.detach(),rs.has(a)||a.lazyContent?.detach()}),a._setIsOpen(!1)):(i.detach(),a?.lazyContent?.detach()),a&&this._ownsMenu(a)&&rs.delete(a),this.restoreFocus&&(e===`keydown`||!this._openedBy||!this._triggersSubmenu())&&this.focus(this._openedBy),this._openedBy=void 0,this._setIsMenuOpen(!1))}_setIsMenuOpen(e){e!==this._menuOpen&&(this._menuOpen=e,this._menuOpen?this.menuOpened.emit():this.menuClosed.emit(),this._triggersSubmenu()&&this._menuItemInstance._setHighlighted(e),this._changeDetectorRef.markForCheck())}_createOverlay(e){if(!this._overlayRef){let i=this._getOverlayConfig(e);this._subscribeToPositions(e,i.positionStrategy),this._overlayRef=sa(this._injector,i),this._overlayRef.keydownEvents().subscribe(a=>{this._menu instanceof Ni&&this._menu._handleKeydown(a)})}return this._overlayRef}_getOverlayConfig(e){return new aa({positionStrategy:zo(this._injector,this._getOverlayOrigin()).withLockedPosition().withGrowAfterOpen().withTransformOriginOn(`.mat-menu-panel, .mat-mdc-menu-panel`),backdropClass:e.backdropClass||`cdk-overlay-transparent-backdrop`,panelClass:e.overlayPanelClass,scrollStrategy:this._scrollStrategy(),direction:this._dir||`ltr`,disableAnimations:this._animationsDisabled})}_subscribeToPositions(e,i){e.setPositionClasses&&i.positionChanges.subscribe(a=>{this._ngZone.run(()=>{let o=a.connectionPair.overlayX===`start`?`after`:`before`,r=a.connectionPair.overlayY===`top`?`below`:`above`;e.setPositionClasses(o,r)})})}_setPosition(e,i){let[a,o]=e.xPosition===`before`?[`end`,`start`]:[`start`,`end`],[r,s]=e.yPosition===`above`?[`bottom`,`top`]:[`top`,`bottom`],[l,h]=[r,s],[p,g]=[a,o],_=0;if(this._triggersSubmenu()){if(g=a=e.xPosition===`before`?`start`:`end`,o=p=a===`end`?`start`:`end`,this._parentMaterialMenu){if(this._parentInnerPadding==null){let b=this._parentMaterialMenu.items.first;this._parentInnerPadding=b?b._getHostElement().offsetTop:0}_=r===`bottom`?this._parentInnerPadding:-this._parentInnerPadding}}else e.overlapTrigger||(l=r===`top`?`bottom`:`top`,h=s===`top`?`bottom`:`top`);i.withPositions([{originX:a,originY:l,overlayX:p,overlayY:r,offsetY:_},{originX:o,originY:l,overlayX:g,overlayY:r,offsetY:_},{originX:a,originY:h,overlayX:p,overlayY:s,offsetY:-_},{originX:o,originY:h,overlayX:g,overlayY:s,offsetY:-_}])}_menuClosingActions(){let e=this._getOutsideClickStream(this._overlayRef),i=this._overlayRef.detachments();return kt(e,this._parentMaterialMenu?this._parentMaterialMenu.closed:Mn(),this._parentMaterialMenu?this._parentMaterialMenu._hovered().pipe(Qe(r=>this._menuOpen&&r!==this._menuItemInstance)):Mn(),i)}_getPortal(e){return(!this._portal||this._portal.templateRef!==e.templateRef)&&(this._portal=new Hn(e.templateRef,this._viewContainerRef)),this._portal}_ownsMenu(e){return rs.get(e)===this}_triggerIsAriaDisabled(){return z(this._element.nativeElement.getAttribute(`aria-disabled`))}static ɵfac=function(i){Hf()};static ɵdir=Q({type:t})}return t})();var ss=(()=>{class t extends KR{_cleanupTouchstart;_hoverSubscription=De.EMPTY;get _deprecatedMatMenuTriggerFor(){return this.menu}set _deprecatedMatMenuTriggerFor(e){this.menu=e}get menu(){return this._menu}set menu(e){this._menu=e}menuData;restoreFocus=!0;menuOpened=new te;onMenuOpen=this.menuOpened;menuClosed=new te;onMenuClose=this.menuClosed;constructor(){super(!0);let e=f(Pe);this._cleanupTouchstart=e.listen(this._element.nativeElement,`touchstart`,i=>{Mo(i)||(this._openedBy=`touch`)},{passive:!0})}triggersSubmenu(){return super._triggersSubmenu()}toggleMenu(){return this.menuOpen?this.closeMenu():this.openMenu()}openMenu(){this._openMenu(!0)}closeMenu(){this._closeMenu()}updatePosition(){this._overlayRef?.updatePosition()}ngAfterContentInit(){this._handleHover()}ngOnDestroy(){super.ngOnDestroy(),this._cleanupTouchstart(),this._hoverSubscription.unsubscribe()}_getOverlayOrigin(){return this._element}_getOutsideClickStream(e){return e.backdropClick()}_handleMousedown(e){ko(e)||(this._openedBy=e.button===0?`mouse`:void 0,this.triggersSubmenu()&&e.preventDefault())}_handleKeydown(e){let i=e.keyCode;(i===13||i===32)&&(this._openedBy=`keyboard`),this.triggersSubmenu()&&(i===39&&this.dir===`ltr`||i===37&&this.dir===`rtl`)&&(this._openedBy=`keyboard`,this.openMenu())}_handleClick(e){this.triggersSubmenu()?(e.stopPropagation(),this.openMenu()):this.toggleMenu()}_handleHover(){this.triggersSubmenu()&&this._parentMaterialMenu&&(this._hoverSubscription=this._parentMaterialMenu._hovered().subscribe(e=>{e===this._menuItemInstance&&!e.disabled&&this._parentMaterialMenu?._panelAnimationState!==`void`&&(this._openedBy=`mouse`,this._openMenu(!1))}))}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`mat-menu-trigger-for`,``],[``,`matMenuTriggerFor`,``]],hostAttrs:[1,`mat-mdc-menu-trigger`],hostVars:3,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._handleClick(r)})(`mousedown`,function(r){return a._handleMousedown(r)})(`keydown`,function(r){return a._handleKeydown(r)}),i&2&&ne(`aria-haspopup`,a.menu?`menu`:null)(`aria-expanded`,a.menuOpen)(`aria-controls`,a.menuOpen?a.menu?.panelId:null)},inputs:{_deprecatedMatMenuTriggerFor:[0,`mat-menu-trigger-for`,`_deprecatedMatMenuTriggerFor`],menu:[0,`matMenuTriggerFor`,`menu`],menuData:[0,`matMenuTriggerData`,`menuData`],restoreFocus:[0,`matMenuTriggerRestoreFocus`,`restoreFocus`]},outputs:{menuOpened:`menuOpened`,onMenuOpen:`onMenuOpen`,menuClosed:`menuClosed`,onMenuClose:`onMenuClose`},exportAs:[`matMenuTrigger`],features:[Be]})}return t})();var ls=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[ki,mi,we,ia]})}return t})();var Ho=class{_multiple;_emitChanges;compareWith;_selection=new Set;_deselectedToEmit=[];_selectedToEmit=[];_selected=null;get selected(){return this._selected||(this._selected=Array.from(this._selection.values())),this._selected}changed=new V;bulk={select:n=>this._select(n),deselect:n=>this._deselect(n),setSelection:n=>this._setSelection(n)};constructor(n=!1,e,i=!0,a){this._multiple=n,this._emitChanges=i,this.compareWith=a,e&&e.length&&(n?e.forEach(o=>this._markSelected(o)):this._markSelected(e[0]),this._selectedToEmit.length=0)}select(...n){return this._select(n)}deselect(...n){return this._deselect(n)}setSelection(...n){return this._setSelection(n)}toggle(n){return this.isSelected(n)?this.deselect(n):this.select(n)}clear(n=!0){this._unmarkAll();let e=this._hasQueuedChanges();return n&&this._emitChangeEvent(),e}isSelected(n){return this._selection.has(this._getConcreteValue(n))}isEmpty(){return this._selection.size===0}hasValue(){return!this.isEmpty()}sort(n){this._multiple&&this.selected&&this._selected.sort(n)}isMultipleSelection(){return this._multiple}_select(n){this._verifyValueAssignment(n),n.forEach(i=>this._markSelected(i));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}_deselect(n){this._verifyValueAssignment(n),n.forEach(i=>this._unmarkSelected(i));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}_setSelection(n){this._verifyValueAssignment(n);let e=this.selected,i=new Set(n.map(o=>this._getConcreteValue(o)));n.forEach(o=>this._markSelected(o)),e.filter(o=>!i.has(this._getConcreteValue(o,i))).forEach(o=>this._unmarkSelected(o));let a=this._hasQueuedChanges();return this._emitChangeEvent(),a}_emitChangeEvent(){this._selected=null,(this._selectedToEmit.length||this._deselectedToEmit.length)&&(this.changed.next({source:this,added:this._selectedToEmit,removed:this._deselectedToEmit}),this._deselectedToEmit=[],this._selectedToEmit=[])}_markSelected(n){n=this._getConcreteValue(n),this.isSelected(n)||(this._multiple||this._unmarkAll(),this.isSelected(n)||this._selection.add(n),this._emitChanges&&this._selectedToEmit.push(n))}_unmarkSelected(n){n=this._getConcreteValue(n),this.isSelected(n)&&(this._selection.delete(n),this._emitChanges&&this._deselectedToEmit.push(n))}_unmarkAll(){this.isEmpty()||this._selection.forEach(n=>this._unmarkSelected(n))}_verifyValueAssignment(n){n.length>1&&this._multiple}_hasQueuedChanges(){return!!(this._deselectedToEmit.length||this._selectedToEmit.length)}_getConcreteValue(n,e){if(this.compareWith){e=e??this._selection;for(let i of e)if(this.compareWith(n,i))return i;return n}else return n}};var fm=(()=>{class t{_animationsDisabled=Ue();state=`unchecked`;disabled=!1;appearance=`full`;static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`mat-pseudo-checkbox`]],hostAttrs:[1,`mat-pseudo-checkbox`],hostVars:12,hostBindings:function(i,a){i&2&&G(`mat-pseudo-checkbox-indeterminate`,a.state===`indeterminate`)(`mat-pseudo-checkbox-checked`,a.state===`checked`)(`mat-pseudo-checkbox-disabled`,a.disabled)(`mat-pseudo-checkbox-minimal`,a.appearance===`minimal`)(`mat-pseudo-checkbox-full`,a.appearance===`full`)(`_mat-animation-noopable`,a._animationsDisabled)},inputs:{state:`state`,disabled:`disabled`,appearance:`appearance`},decls:0,vars:0,template:function(i,a){},styles:[`.mat-pseudo-checkbox {
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
`],encapsulation:2})}return t})();var Db=new N(`MAT_OPTION_PARENT_COMPONENT`);var Ib=new N(`MatOptgroup`);var Eb=class{source;isUserInput;constructor(n,e=!1){this.source=n,this.isUserInput=e}};var Un=(()=>{class t{_element=f(Y);_changeDetectorRef=f(Le);_parent=f(Db,{optional:!0});group=f(Ib,{optional:!0});_signalDisableRipple=!1;_selected=!1;_active=!1;_mostRecentViewValue=``;get multiple(){return this._parent&&this._parent.multiple}get selected(){return this._selected}value;id=f(je).getId(`mat-option-`);get disabled(){return this.group&&this.group.disabled||this._disabled()}set disabled(e){this._disabled.set(e)}_disabled=T(!1);get disableRipple(){return this._signalDisableRipple?this._parent.disableRipple():!!this._parent?.disableRipple}get hideSingleSelectionIndicator(){return!!(this._parent&&this._parent.hideSingleSelectionIndicator)}onSelectionChange=new te;_text;_stateChanges=new V;constructor(){let e=f(Je);e.load(pn),e.load(Ii),this._signalDisableRipple=!!this._parent&&cn(this._parent.disableRipple)}get active(){return this._active}get viewValue(){return(this._text?.nativeElement.textContent||``).trim()}select(e=!0){this._selected||(this._selected=!0,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}deselect(e=!0){this._selected&&(this._selected=!1,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}focus(e,i){let a=this._getHostElement();typeof a.focus==`function`&&a.focus(i)}setActiveStyles(){this._active||(this._active=!0,this._changeDetectorRef.markForCheck())}setInactiveStyles(){this._active&&(this._active=!1,this._changeDetectorRef.markForCheck())}getLabel(){return this.viewValue}_handleKeydown(e){(e.keyCode===13||e.keyCode===32)&&!Ct(e)&&(this._selectViaInteraction(),e.preventDefault())}_selectViaInteraction(){this.disabled||(this._selected=this.multiple?!this._selected:!0,this._changeDetectorRef.markForCheck(),this._emitSelectionChangeEvent(!0))}_getTabIndex(){return this.disabled?`-1`:`0`}_getHostElement(){return this._element.nativeElement}ngAfterViewChecked(){if(this._selected){let e=this.viewValue;e!==this._mostRecentViewValue&&(this._mostRecentViewValue&&this._stateChanges.next(),this._mostRecentViewValue=e)}}ngOnDestroy(){this._stateChanges.complete()}_emitSelectionChangeEvent(e=!1){this.onSelectionChange.emit(new Eb(this,e))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`text`],i=[[[`mat-icon`]],`*`],a=[`mat-icon`,`*`];function o(l,h){if(l&1&&U(0,`mat-pseudo-checkbox`,1),l&2){let p=v();S(`disabled`,p.disabled)(`state`,p.selected?`checked`:`unchecked`)}}function r(l,h){if(l&1&&U(0,`mat-pseudo-checkbox`,3),l&2)S(`disabled`,v().disabled)}function s(l,h){if(l&1&&(c(0,`span`,4),u(1),d()),l&2){let p=v();m(),B(`(`,p.group.label,`)`)}}return H({type:t,selectors:[[`mat-option`]],viewQuery:function(h,p){if(h&1&&Ze(e,7),h&2){let g;J(g=ee())&&(p._text=g.first)}},hostAttrs:[`role`,`option`,1,`mat-mdc-option`,`mdc-list-item`],hostVars:11,hostBindings:function(h,p){h&1&&C(`click`,function(){return p._selectViaInteraction()})(`keydown`,function(_){return p._handleKeydown(_)}),h&2&&(qt(`id`,p.id),ne(`aria-selected`,p.selected)(`aria-disabled`,p.disabled.toString()),G(`mdc-list-item--selected`,p.selected)(`mat-mdc-option-multiple`,p.multiple)(`mat-mdc-option-active`,p.active)(`mdc-list-item--disabled`,p.disabled))},inputs:{value:`value`,id:`id`,disabled:[2,`disabled`,`disabled`,z]},outputs:{onSelectionChange:`onSelectionChange`},exportAs:[`matOption`],ngContentSelectors:a,decls:8,vars:5,consts:[[`text`,``],[`aria-hidden`,`true`,1,`mat-mdc-option-pseudo-checkbox`,3,`disabled`,`state`],[1,`mdc-list-item__primary-text`],[`state`,`checked`,`aria-hidden`,`true`,`appearance`,`minimal`,1,`mat-mdc-option-pseudo-checkbox`,3,`disabled`],[1,`cdk-visually-hidden`],[`aria-hidden`,`true`,`mat-ripple`,``,1,`mat-mdc-option-ripple`,`mat-focus-indicator`,3,`matRippleTrigger`,`matRippleDisabled`]],template:function(h,p){h&1&&(Fe(i),E(0,o,1,2,`mat-pseudo-checkbox`,1),he(1),c(2,`span`,2,0),he(4,1),d(),E(5,r,1,1,`mat-pseudo-checkbox`,3),E(6,s,2,1,`span`,4),U(7,`div`,5)),h&2&&(D(p.multiple?0:-1),m(5),D(!p.multiple&&p.selected&&!p.hideSingleSelectionIndicator?5:-1),m(),D(p.group&&p.group._inert?6:-1),m(),S(`matRippleTrigger`,p._getHostElement())(`matRippleDisabled`,p.disabled||p.disableRipple))},dependencies:[fm,li],styles:[`.mat-mdc-option {
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
`],encapsulation:2})})()}return t})();function Ew(t,n,e){if(e.length){let i=n.toArray(),a=e.toArray(),o=0;for(let r=0;r<t+1;r++)i[r].group&&i[r].group===a[o]&&o++;return o}return 0}function Dw(t,n,e,i){return t<e?t:t+n>e+i?Math.max(0,t-i+n):e}var Iw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[we]})}return t})();var kb=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[ki,Iw,Un,we]})}return t})();var QR=new N(`mat-select-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t)}});var XR=new N(`MAT_SELECT_CONFIG`);var ZR=new N(`MatSelectTrigger`);var Mb=class{source;value;constructor(n,e){this.source=n,this.value=e}};var Pa=(()=>{class t{_viewportRuler=f(ui);_changeDetectorRef=f(Le);_elementRef=f(Y);_dir=f(gt,{optional:!0});_idGenerator=f(je);_renderer=f(Pe);_parentFormField=f(Hl,{optional:!0});ngControl=f(Mi,{self:!0,optional:!0});_liveAnnouncer=f(Hg);_defaultOptions=f(XR,{optional:!0});_animationsDisabled=Ue();_popoverLocation;_initialized=new V;_cleanupDetach;options;optionGroups;customTrigger;_positions=[{originX:`start`,originY:`bottom`,overlayX:`start`,overlayY:`top`},{originX:`end`,originY:`bottom`,overlayX:`end`,overlayY:`top`},{originX:`start`,originY:`top`,overlayX:`start`,overlayY:`bottom`,panelClass:`mat-mdc-select-panel-above`},{originX:`end`,originY:`top`,overlayX:`end`,overlayY:`bottom`,panelClass:`mat-mdc-select-panel-above`}];_scrollOptionIntoView(e){let i=this.options.toArray()[e];if(i){let a=this.panel.nativeElement,o=Ew(e,this.options,this.optionGroups),r=i._getHostElement();e===0&&o===1?a.scrollTop=0:a.scrollTop=Dw(r.offsetTop,r.offsetHeight,a.scrollTop,a.offsetHeight)}}_positioningSettled(){this._scrollOptionIntoView(this._keyManager.activeItemIndex||0)}_getChangeEvent(e){return new Mb(this,e)}_scrollStrategyFactory=f(QR);_panelOpen=!1;_compareWith=(e,i)=>e===i;_uid=this._idGenerator.getId(`mat-select-`);_triggerAriaLabelledBy=null;_previousControl;_destroy=new V;_errorStateTracker;stateChanges=new V;disableAutomaticLabeling=!0;userAriaDescribedBy;_selectionModel;_keyManager;_preferredOverlayOrigin;_overlayWidth;_onChange=()=>{};_onTouched=()=>{};_valueId=this._idGenerator.getId(`mat-select-value-`);_scrollStrategy;_overlayPanelClass=this._defaultOptions?.overlayPanelClass||``;get focused(){return this._focused||this._panelOpen}_focused=!1;controlType=`mat-select`;trigger;panel;_overlayDir;panelClass;disabled=!1;get disableRipple(){return this._disableRipple()}set disableRipple(e){this._disableRipple.set(e)}_disableRipple=T(!1);tabIndex=0;get hideSingleSelectionIndicator(){return this._hideSingleSelectionIndicator}set hideSingleSelectionIndicator(e){this._hideSingleSelectionIndicator=e,this._syncParentProperties()}_hideSingleSelectionIndicator=this._defaultOptions?.hideSingleSelectionIndicator??!1;get placeholder(){return this._placeholder}set placeholder(e){this._placeholder=e,this.stateChanges.next()}_placeholder;get required(){return this._required??this.ngControl?.control?.hasValidator(Lo.required)??!1}set required(e){this._required=e,this.stateChanges.next()}_required;get multiple(){return this._multiple}set multiple(e){this._selectionModel,this._multiple=e}_multiple=!1;disableOptionCentering=this._defaultOptions?.disableOptionCentering??!1;get compareWith(){return this._compareWith}set compareWith(e){this._compareWith=e,this._selectionModel&&this._initializeSelection()}get value(){return this._value}set value(e){this._assignValue(e)&&this._onChange(e)}_value;ariaLabel=``;ariaLabelledby;get errorStateMatcher(){return this._errorStateTracker.matcher}set errorStateMatcher(e){this._errorStateTracker.matcher=e}typeaheadDebounceInterval;sortComparator;get id(){return this._id}set id(e){this._id=e||this._uid,this.stateChanges.next()}_id;get errorState(){return this._errorStateTracker.errorState}set errorState(e){this._errorStateTracker.errorState=e}panelWidth=this._defaultOptions&&typeof this._defaultOptions.panelWidth<`u`?this._defaultOptions.panelWidth:`auto`;canSelectNullableOptions=this._defaultOptions?.canSelectNullableOptions??!1;optionSelectionChanges=Xa(()=>{let e=this.options;return e?e.changes.pipe(mt(e),_n(()=>kt(...e.map(i=>i.onSelectionChange)))):this._initialized.pipe(_n(()=>this.optionSelectionChanges))});openedChange=new te;_openedStream=this.openedChange.pipe(Qe(e=>e),lt(()=>{}));_closedStream=this.openedChange.pipe(Qe(e=>!e),lt(()=>{}));selectionChange=new te;valueChange=new te;constructor(){let e=f(Xr),i=f(Vl,{optional:!0}),a=f(Bl,{optional:!0}),o=f(new Ki(`tabindex`),{optional:!0}),r=f(Kl,{optional:!0}),s=f(em,{optional:!0,self:!0});this.ngControl&&(this.ngControl.valueAccessor=this),this._defaultOptions?.typeaheadDebounceInterval!=null&&(this.typeaheadDebounceInterval=this._defaultOptions.typeaheadDebounceInterval),this._errorStateTracker=new Zr(e,s||this.ngControl,a,i,this.stateChanges),this._scrollStrategy=this._scrollStrategyFactory(),this.tabIndex=o==null?0:parseInt(o)||0,this._popoverLocation=r?.usePopover===!1?null:`inline`,this.id=this.id}ngOnInit(){this._selectionModel=new Ho(this.multiple),this.stateChanges.next(),this._viewportRuler.change().pipe(Ne(this._destroy)).subscribe(()=>{this.panelOpen&&(this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._changeDetectorRef.detectChanges())})}ngAfterContentInit(){this._initialized.next(),this._initialized.complete(),this._initKeyManager(),this._selectionModel.changed.pipe(Ne(this._destroy)).subscribe(e=>{e.added.forEach(i=>i.select()),e.removed.forEach(i=>i.deselect())}),this.options.changes.pipe(mt(null),Ne(this._destroy)).subscribe(()=>{this._resetOptions(),this._initializeSelection()})}ngDoCheck(){let e=this._getTriggerAriaLabelledby(),i=this.ngControl;if(e!==this._triggerAriaLabelledBy){let a=this._elementRef.nativeElement;this._triggerAriaLabelledBy=e,e?a.setAttribute(`aria-labelledby`,e):a.removeAttribute(`aria-labelledby`)}i&&(this._previousControl!==i.control&&(this._previousControl!==void 0&&i.disabled!==null&&i.disabled!==this.disabled&&(this.disabled=i.disabled),this._previousControl=i.control),this.updateErrorState())}ngOnChanges(e){(e.disabled||e.userAriaDescribedBy)&&this.stateChanges.next(),e.typeaheadDebounceInterval&&this._keyManager&&this._keyManager.withTypeAhead(this.typeaheadDebounceInterval),e.panelClass&&this.panelClass instanceof Set&&(this.panelClass=Array.from(this.panelClass))}ngOnDestroy(){this._cleanupDetach?.(),this._keyManager?.destroy(),this._destroy.next(),this._destroy.complete(),this.stateChanges.complete()}toggle(){this.panelOpen?this.close():this.open()}open(){this._canOpen()&&(this._parentFormField&&(this._preferredOverlayOrigin=this._parentFormField.getConnectedOverlayOrigin()),this._cleanupDetach?.(),this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._panelOpen=!0,this._overlayDir.positionChange.pipe(vn(1)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this._positioningSettled()}),this._overlayDir.attachOverlay(),this._keyManager.withHorizontalOrientation(null),this._highlightCorrectOption(),this._changeDetectorRef.markForCheck(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!0)))}close(){this._panelOpen&&(this._panelOpen=!1,this._exitAndDetach(),this._keyManager.withHorizontalOrientation(this._isRtl()?`rtl`:`ltr`),this._changeDetectorRef.markForCheck(),this._onTouched(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!1)))}_exitAndDetach(){if(this._animationsDisabled||!this.panel){this._detachOverlay();return}this._cleanupDetach?.(),this._cleanupDetach=()=>{i(),clearTimeout(a),this._cleanupDetach=void 0};let e=this.panel.nativeElement,i=this._renderer.listen(e,`animationend`,o=>{o.animationName===`_mat-select-exit`&&(this._cleanupDetach?.(),this._detachOverlay())}),a=setTimeout(()=>{this._cleanupDetach?.(),this._detachOverlay()},200);e.classList.add(`mat-select-panel-exit`)}_detachOverlay(){this._overlayDir.detachOverlay(),this._changeDetectorRef.markForCheck()}writeValue(e){this._assignValue(e)}registerOnChange(e){this._onChange=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e,this._changeDetectorRef.markForCheck(),this.stateChanges.next()}get panelOpen(){return this._panelOpen}get selected(){return this.multiple?this._selectionModel?.selected||[]:this._selectionModel?.selected[0]}get triggerValue(){if(this.empty)return``;if(this._multiple){let e=this._selectionModel.selected.map(i=>i.viewValue);return this._isRtl()&&e.reverse(),e.join(`, `)}return this._selectionModel.selected[0].viewValue}updateErrorState(){this._errorStateTracker.updateErrorState()}_isRtl(){return this._dir?this._dir.value===`rtl`:!1}_handleKeydown(e){this.disabled||(this.panelOpen?this._handleOpenKeydown(e):this._handleClosedKeydown(e))}_handleClosedKeydown(e){let i=e.keyCode,a=i===40||i===38||i===37||i===39,o=i===13||i===32,r=this._keyManager;if(!r.isTyping()&&o&&!Ct(e)||(this.multiple||e.altKey)&&a)e.preventDefault(),this.open();else if(!this.multiple){let s=this.selected;r.onKeydown(e);let l=this.selected;l&&s!==l&&this._liveAnnouncer.announce(l.viewValue,1e4)}}_handleOpenKeydown(e){let i=this._keyManager,a=e.keyCode,o=a===40||a===38,r=i.isTyping();if(o&&e.altKey)e.preventDefault(),this.close();else if(!r&&(a===13||a===32)&&i.activeItem&&!Ct(e))e.preventDefault(),i.activeItem._selectViaInteraction();else if(!r&&this._multiple&&a===65&&e.ctrlKey){e.preventDefault();let s=this.options.some(l=>!l.disabled&&!l.selected);this.options.forEach(l=>{l.disabled||(s?l.select():l.deselect())})}else{let s=i.activeItemIndex;i.onKeydown(e),this._multiple&&o&&e.shiftKey&&i.activeItem&&i.activeItemIndex!==s&&i.activeItem._selectViaInteraction()}}_handleOverlayKeydown(e){e.keyCode===27&&!Ct(e)&&(e.preventDefault(),this.close())}_onFocus(){this.disabled||(this._focused=!0,this.stateChanges.next())}_onBlur(){this._focused=!1,this._keyManager?.cancelTypeahead(),!this.disabled&&!this.panelOpen&&(this._onTouched(),this._changeDetectorRef.markForCheck(),this.stateChanges.next())}get empty(){return!this._selectionModel||this._selectionModel.isEmpty()}_initializeSelection(){Promise.resolve().then(()=>{this.ngControl&&(this._value=this.ngControl.value),this._setSelectionByValue(this._value),this.stateChanges.next()})}_setSelectionByValue(e){if(this.options.forEach(i=>i.setInactiveStyles()),this._selectionModel.clear(),this.multiple&&e)e.forEach(i=>this._selectOptionByValue(i)),this._sortValues();else{let i=this._selectOptionByValue(e);i?this._keyManager.updateActiveItem(i):this.panelOpen||this._keyManager.updateActiveItem(-1)}this._changeDetectorRef.markForCheck()}_selectOptionByValue(e){let i=this.options.find(a=>{if(this._selectionModel.isSelected(a))return!1;try{return(a.value!=null||this.canSelectNullableOptions)&&this._compareWith(a.value,e)}catch{return!1}});return i&&this._selectionModel.select(i),i}_assignValue(e){return e!==this._value||this._multiple&&Array.isArray(e)?(this.options&&this._setSelectionByValue(e),this._value=e,!0):!1}_skipPredicate=e=>this.panelOpen?!1:e.disabled;_getOverlayWidth(e){return this.panelWidth===`auto`?(e instanceof is?e.elementRef:e||this._elementRef).nativeElement.getBoundingClientRect().width:this.panelWidth===null?``:this.panelWidth}_syncParentProperties(){if(this.options)for(let e of this.options)e._changeDetectorRef.markForCheck()}_initKeyManager(){this._keyManager=new Ml(this.options).withTypeAhead(this.typeaheadDebounceInterval).withVerticalOrientation().withHorizontalOrientation(this._isRtl()?`rtl`:`ltr`).withHomeAndEnd().withPageUpDown().withAllowedModifierKeys([`shiftKey`]).skipPredicate(this._skipPredicate),this._keyManager.tabOut.subscribe(()=>{this.panelOpen&&(!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction(),this.focus(),this.close())}),this._keyManager.change.subscribe(()=>{this._panelOpen&&this.panel?this._scrollOptionIntoView(this._keyManager.activeItemIndex||0):!this._panelOpen&&!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction()})}_resetOptions(){let e=kt(this.options.changes,this._destroy);this.optionSelectionChanges.pipe(Ne(e)).subscribe(i=>{this._onSelect(i.source,i.isUserInput),i.isUserInput&&!this.multiple&&this._panelOpen&&(this.close(),this.focus())}),kt(...this.options.map(i=>i._stateChanges)).pipe(Ne(e)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this.stateChanges.next()})}_onSelect(e,i){let a=this._selectionModel.isSelected(e);!this.canSelectNullableOptions&&e.value==null&&!this._multiple?(e.deselect(),this._selectionModel.clear(),this.value!=null&&this._propagateChanges(e.value)):(a!==e.selected&&(e.selected?this._selectionModel.select(e):this._selectionModel.deselect(e)),i&&this._keyManager.setActiveItem(e),this.multiple&&(this._sortValues(),i&&this.focus())),a!==this._selectionModel.isSelected(e)&&this._propagateChanges(),this.stateChanges.next()}_sortValues(){if(this.multiple){let e=this.options.toArray();this._selectionModel.sort((i,a)=>this.sortComparator?this.sortComparator(i,a,e):e.indexOf(i)-e.indexOf(a)),this.stateChanges.next()}}_propagateChanges(e){let i;this.multiple?i=this.selected.map(a=>a.value):i=this.selected?this.selected.value:e,this._value=i,this.valueChange.emit(i),this._onChange(i),this.selectionChange.emit(this._getChangeEvent(i)),this._changeDetectorRef.markForCheck()}_highlightCorrectOption(){if(this._keyManager)if(this.empty){let e=-1;for(let i=0;i<this.options.length;i++)if(!this.options.get(i).disabled){e=i;break}this._keyManager.setActiveItem(e)}else this._keyManager.setActiveItem(this._selectionModel.selected[0])}_canOpen(){return!this._panelOpen&&!this.disabled&&this.options?.length>0&&!!this._overlayDir}focus(e){this._elementRef.nativeElement.focus(e)}_getPanelAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||null,i=e?e+` `:``;return this.ariaLabelledby?i+this.ariaLabelledby:e}_getAriaActiveDescendant(){return this.panelOpen&&this._keyManager&&this._keyManager.activeItem?this._keyManager.activeItem.id:null}_getTriggerAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||``;return this.ariaLabelledby&&(e+=` `+this.ariaLabelledby),e||(e=this._valueId),e}get describedByIds(){return this._elementRef.nativeElement.getAttribute(`aria-describedby`)?.split(` `)||[]}setDescribedByIds(e){let i=this._elementRef.nativeElement;e.length?i.setAttribute(`aria-describedby`,e.join(` `)):i.removeAttribute(`aria-describedby`)}onContainerClick(e){let i=Yt(e);i&&(i.tagName===`MAT-OPTION`||i.classList.contains(`cdk-overlay-backdrop`)||i.closest(`.mat-mdc-select-panel`))||(this.focus(),this.open())}get shouldLabelFloat(){return this.panelOpen||!this.empty||this.focused&&!!this.placeholder}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`trigger`],i=[`panel`],a=[[[`mat-select-trigger`]],`*`],o=[`mat-select-trigger`,`*`];function r(g,_){if(g&1&&(c(0,`span`,4),u(1),d()),g&2){let b=v();m(),w(b.placeholder)}}function s(g,_){g&1&&he(0)}function l(g,_){if(g&1&&(c(0,`span`,11),u(1),d()),g&2){let b=v(2);m(),w(b.triggerValue)}}function h(g,_){if(g&1&&(c(0,`span`,5),E(1,s,1,0)(2,l,2,1,`span`,11),d()),g&2){let b=v();m(),D(b.customTrigger?1:2)}}function p(g,_){if(g&1){let b=$();c(0,`div`,12,1),C(`keydown`,function(I){k(b);return M(v()._handleKeydown(I))}),he(2,1),d()}if(g&2){let b=v();tt(b.panelClass),G(`mat-select-panel-animations-enabled`,!b._animationsDisabled)(`mat-primary`,b._parentFormField?.color===`primary`)(`mat-accent`,b._parentFormField?.color===`accent`)(`mat-warn`,b._parentFormField?.color===`warn`)(`mat-undefined`,!b._parentFormField?.color),ne(`id`,b.id+`-panel`)(`aria-multiselectable`,b.multiple)(`aria-label`,b.ariaLabel||null)(`aria-labelledby`,b._getPanelAriaLabelledby())}}return H({type:t,selectors:[[`mat-select`]],contentQueries:function(_,b,y){if(_&1&&jt(y,ZR,5)(y,Un,5)(y,Ib,5),_&2){let I;J(I=ee())&&(b.customTrigger=I.first),J(I=ee())&&(b.options=I),J(I=ee())&&(b.optionGroups=I)}},viewQuery:function(_,b){if(_&1&&Ze(e,5)(i,5)(pm,5),_&2){let y;J(y=ee())&&(b.trigger=y.first),J(y=ee())&&(b.panel=y.first),J(y=ee())&&(b._overlayDir=y.first)}},hostAttrs:[`role`,`combobox`,`aria-haspopup`,`listbox`,1,`mat-mdc-select`],hostVars:21,hostBindings:function(_,b){_&1&&C(`keydown`,function(I){return b._handleKeydown(I)})(`focus`,function(){return b._onFocus()})(`blur`,function(){return b._onBlur()}),_&2&&(ne(`id`,b.id)(`tabindex`,b.disabled?-1:b.tabIndex)(`aria-controls`,b.panelOpen?b.id+`-panel`:null)(`aria-expanded`,b.panelOpen)(`aria-label`,b.ariaLabel||null)(`aria-required`,b.required.toString())(`aria-disabled`,b.disabled.toString())(`aria-invalid`,b.errorState)(`aria-activedescendant`,b._getAriaActiveDescendant()),G(`mat-mdc-select-disabled`,b.disabled)(`mat-mdc-select-invalid`,b.errorState)(`mat-mdc-select-required`,b.required)(`mat-mdc-select-empty`,b.empty)(`mat-mdc-select-multiple`,b.multiple)(`mat-select-open`,b.panelOpen))},inputs:{userAriaDescribedBy:[0,`aria-describedby`,`userAriaDescribedBy`],panelClass:`panelClass`,disabled:[2,`disabled`,`disabled`,z],disableRipple:[2,`disableRipple`,`disableRipple`,z],tabIndex:[2,`tabIndex`,`tabIndex`,g=>g==null?0:dt(g)],hideSingleSelectionIndicator:[2,`hideSingleSelectionIndicator`,`hideSingleSelectionIndicator`,z],placeholder:`placeholder`,required:[2,`required`,`required`,z],multiple:[2,`multiple`,`multiple`,z],disableOptionCentering:[2,`disableOptionCentering`,`disableOptionCentering`,z],compareWith:`compareWith`,value:`value`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],errorStateMatcher:`errorStateMatcher`,typeaheadDebounceInterval:[2,`typeaheadDebounceInterval`,`typeaheadDebounceInterval`,dt],sortComparator:`sortComparator`,id:`id`,panelWidth:`panelWidth`,canSelectNullableOptions:[2,`canSelectNullableOptions`,`canSelectNullableOptions`,z]},outputs:{openedChange:`openedChange`,_openedStream:`opened`,_closedStream:`closed`,selectionChange:`selectionChange`,valueChange:`valueChange`},exportAs:[`matSelect`],features:[Ge([{provide:zl,useExisting:t},{provide:Db,useExisting:t}]),_t],ngContentSelectors:o,decls:11,vars:10,consts:[[`fallbackOverlayOrigin`,`cdkOverlayOrigin`,`trigger`,``],[`panel`,``],[`cdk-overlay-origin`,``,1,`mat-mdc-select-trigger`,3,`click`],[1,`mat-mdc-select-value`],[1,`mat-mdc-select-placeholder`,`mat-mdc-select-min-line`],[1,`mat-mdc-select-value-text`],[1,`mat-mdc-select-arrow-wrapper`],[1,`mat-mdc-select-arrow`],[`viewBox`,`0 0 24 24`,`width`,`24px`,`height`,`24px`,`focusable`,`false`,`aria-hidden`,`true`],[`d`,`M7 10l5 5 5-5z`],[`cdk-connected-overlay`,``,`cdkConnectedOverlayHasBackdrop`,``,`cdkConnectedOverlayBackdropClass`,`cdk-overlay-transparent-backdrop`,3,`detach`,`backdropClick`,`overlayKeydown`,`cdkConnectedOverlayDisableClose`,`cdkConnectedOverlayPanelClass`,`cdkConnectedOverlayScrollStrategy`,`cdkConnectedOverlayOrigin`,`cdkConnectedOverlayPositions`,`cdkConnectedOverlayWidth`,`cdkConnectedOverlayFlexibleDimensions`,`cdkConnectedOverlayUsePopover`],[1,`mat-mdc-select-min-line`],[`role`,`listbox`,`tabindex`,`-1`,1,`mat-mdc-select-panel`,`mdc-menu-surface`,`mdc-menu-surface--open`,3,`keydown`]],template:function(_,b){if(_&1&&(Fe(a),c(0,`div`,2,0),C(`click`,function(){return b.open()}),c(3,`div`,3),E(4,r,2,1,`span`,4)(5,h,3,1,`span`,5),d(),c(6,`div`,6)(7,`div`,7),Gt(),c(8,`svg`,8),U(9,`path`,9),d()()()(),st(10,p,3,16,`ng-template`,10),C(`detach`,function(){return b.close()})(`backdropClick`,function(){return b.close()})(`overlayKeydown`,function(I){return b._handleOverlayKeydown(I)})),_&2){let y=ft(1);m(3),ne(`id`,b._valueId),m(),D(b.empty?4:5),m(6),S(`cdkConnectedOverlayDisableClose`,!0)(`cdkConnectedOverlayPanelClass`,b._overlayPanelClass)(`cdkConnectedOverlayScrollStrategy`,b._scrollStrategy)(`cdkConnectedOverlayOrigin`,b._preferredOverlayOrigin||y)(`cdkConnectedOverlayPositions`,b._positions)(`cdkConnectedOverlayWidth`,b._overlayWidth)(`cdkConnectedOverlayFlexibleDimensions`,!0)(`cdkConnectedOverlayUsePopover`,b._popoverLocation)}},dependencies:[is,pm],styles:[`@keyframes _mat-select-enter {
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
`],encapsulation:2})})()}return t})();var Fa=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[mi,kb,we,ia,di,kb]})}return t})();var JR=20;var eO=new N(`mat-tooltip-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t,{scrollThrottle:JR})}});var tO=new N(`mat-tooltip-default-options`,{providedIn:`root`,factory:()=>({showDelay:0,hideDelay:0,touchendHideDelay:1500})});var kw=`tooltip-panel`;var nO={passive:!0};var iO=8;var aO=8;var oO=24;var rO=200;var Mw=(()=>{class t{_elementRef=f(Y);_ngZone=f(Z);_platform=f(Re);_ariaDescriber=f(Pu);_focusMonitor=f(tn);_dir=f(gt);_injector=f(se);_viewContainerRef=f(Bt);_mediaMatcher=f(Br);_document=f(me);_renderer=f(Pe);_animationsDisabled=Ue();_defaultOptions=f(tO,{optional:!0});_overlayRef=null;_tooltipInstance=null;_overlayPanelClass;_portal;_position=`below`;_positionAtOrigin=!1;_disabled=!1;_tooltipClass;_viewInitialized=!1;_pointerExitEventsInitialized=!1;_tooltipComponent=sO;_viewportMargin=8;_currentPosition;_cssClassPrefix=`mat-mdc`;_ariaDescriptionPending=!1;_dirSubscribed=!1;get position(){return this._position}set position(e){e!==this._position&&(this._position=e,this._overlayRef&&(this._updatePosition(this._overlayRef),this._tooltipInstance?.show(0),this._overlayRef.updatePosition()))}get positionAtOrigin(){return this._positionAtOrigin}set positionAtOrigin(e){this._positionAtOrigin=ta(e),this._detach(),this._overlayRef=null}get disabled(){return this._disabled}set disabled(e){let i=ta(e);this._disabled!==i&&(this._disabled=i,i?this.hide(0):this._setupPointerEnterEventsIfNeeded(),this._syncAriaDescription(this.message))}get showDelay(){return this._showDelay}set showDelay(e){this._showDelay=ri(e)}_showDelay;get hideDelay(){return this._hideDelay}set hideDelay(e){this._hideDelay=ri(e),this._tooltipInstance&&(this._tooltipInstance._mouseLeaveHideDelay=this._hideDelay)}_hideDelay;touchGestures=`auto`;get message(){return this._message}set message(e){let i=this._message;this._message=e!=null?String(e).trim():``,!this._message&&this._isTooltipVisible()?this.hide(0):(this._setupPointerEnterEventsIfNeeded(),this._updateTooltipMessage()),this._syncAriaDescription(i)}_message=``;get tooltipClass(){return this._tooltipClass}set tooltipClass(e){this._tooltipClass=e,this._tooltipInstance&&this._setTooltipClass(this._tooltipClass)}_eventCleanups=[];_touchstartTimeout=null;_destroyed=new V;_isDestroyed=!1;constructor(){let e=this._defaultOptions;e&&(this._showDelay=e.showDelay,this._hideDelay=e.hideDelay,e.position&&(this.position=e.position),e.positionAtOrigin&&(this.positionAtOrigin=e.positionAtOrigin),e.touchGestures&&(this.touchGestures=e.touchGestures),e.tooltipClass&&(this.tooltipClass=e.tooltipClass)),this._viewportMargin=iO}ngAfterViewInit(){this._viewInitialized=!0,this._setupPointerEnterEventsIfNeeded(),this._focusMonitor.monitor(this._elementRef).pipe(Ne(this._destroyed)).subscribe(e=>{e?e===`keyboard`&&this._ngZone.run(()=>this.show()):this._ngZone.run(()=>this.hide(0))})}ngOnDestroy(){let e=this._elementRef.nativeElement;this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this._overlayRef&&(this._overlayRef.dispose(),this._tooltipInstance=null),this._eventCleanups.forEach(i=>i()),this._eventCleanups.length=0,this._destroyed.next(),this._destroyed.complete(),this._isDestroyed=!0,this._ariaDescriber.removeDescription(e,this.message,`tooltip`),this._focusMonitor.stopMonitoring(e)}show(e=this.showDelay,i){if(this.disabled||!this.message||this._isTooltipVisible()){this._tooltipInstance?._cancelPendingAnimations();return}let a=this._createOverlay(i);this._detach(),this._portal=this._portal||new Aa(this._tooltipComponent,this._viewContainerRef);let o=this._tooltipInstance=a.attach(this._portal).instance;o._triggerElement=this._elementRef.nativeElement,o._mouseLeaveHideDelay=this._hideDelay,o.afterHidden().pipe(Ne(this._destroyed)).subscribe(()=>this._detach()),this._setTooltipClass(this._tooltipClass),this._updateTooltipMessage(),o.show(e)}hide(e=this.hideDelay){let i=this._tooltipInstance;i&&(i.isVisible()?i.hide(e):(i._cancelPendingAnimations(),this._detach()))}toggle(e){this._isTooltipVisible()?this.hide():this.show(void 0,e)}_isTooltipVisible(){return!!this._tooltipInstance&&this._tooltipInstance.isVisible()}_createOverlay(e){if(this._overlayRef){let r=this._overlayRef.getConfig().positionStrategy;if((!this.positionAtOrigin||!e)&&r._origin instanceof Y)return this._overlayRef;this._detach()}let i=this._injector.get(Oa).getAncestorScrollContainers(this._elementRef),a=`${this._cssClassPrefix}-${kw}`,o=zo(this._injector,this.positionAtOrigin?e||this._elementRef:this._elementRef).withTransformOriginOn(`.${this._cssClassPrefix}-tooltip`).withFlexibleDimensions(!1).withViewportMargin(this._viewportMargin).withScrollableContainers(i).withPopoverLocation(`global`);return o.positionChanges.pipe(Ne(this._destroyed)).subscribe(r=>{this._updateCurrentPositionClass(r.connectionPair),this._tooltipInstance&&r.scrollableViewProperties.isOverlayClipped&&this._tooltipInstance.isVisible()&&this._ngZone.run(()=>this.hide(0))}),this._overlayRef=sa(this._injector,{direction:this._dir,positionStrategy:o,panelClass:this._overlayPanelClass?[...this._overlayPanelClass,a]:a,scrollStrategy:this._injector.get(eO)(),disableAnimations:this._animationsDisabled,eventPredicate:this._overlayEventPredicate}),this._updatePosition(this._overlayRef),this._overlayRef.detachments().pipe(Ne(this._destroyed)).subscribe(()=>this._detach()),this._overlayRef.outsidePointerEvents().pipe(Ne(this._destroyed)).subscribe(()=>this._tooltipInstance?._handleBodyInteraction()),this._overlayRef.keydownEvents().pipe(Ne(this._destroyed)).subscribe(r=>{r.preventDefault(),r.stopPropagation(),this._ngZone.run(()=>this.hide(0))}),this._defaultOptions?.disableTooltipInteractivity&&this._overlayRef.addPanelClass(`${this._cssClassPrefix}-tooltip-panel-non-interactive`),this._dirSubscribed||(this._dirSubscribed=!0,this._dir.change.pipe(Ne(this._destroyed)).subscribe(()=>{this._overlayRef&&this._updatePosition(this._overlayRef)})),this._overlayRef}_detach(){this._overlayRef&&this._overlayRef.hasAttached()&&this._overlayRef.detach(),this._tooltipInstance=null}_updatePosition(e){let i=e.getConfig().positionStrategy,a=this._getOrigin(),o=this._getOverlayPosition();i.withPositions([this._addOffset(r(r({},a.main),o.main)),this._addOffset(r(r({},a.fallback),o.fallback))])}_addOffset(e){let i=aO,a=!this._dir||this._dir.value==`ltr`;return e.originY===`top`?e.offsetY=-i:e.originY===`bottom`?e.offsetY=i:e.originX===`start`?e.offsetX=a?-i:i:e.originX===`end`&&(e.offsetX=a?i:-i),e}_getOrigin(){let e=!this._dir||this._dir.value==`ltr`,i=this.position,a;i==`above`||i==`below`?a={originX:`center`,originY:i==`above`?`top`:`bottom`}:i==`before`||i==`left`&&e||i==`right`&&!e?a={originX:`start`,originY:`center`}:(i==`after`||i==`right`&&e||i==`left`&&!e)&&(a={originX:`end`,originY:`center`});let{x:o,y:r}=this._invertPosition(a.originX,a.originY);return{main:a,fallback:{originX:o,originY:r}}}_getOverlayPosition(){let e=!this._dir||this._dir.value==`ltr`,i=this.position,a;i==`above`?a={overlayX:`center`,overlayY:`bottom`}:i==`below`?a={overlayX:`center`,overlayY:`top`}:i==`before`||i==`left`&&e||i==`right`&&!e?a={overlayX:`end`,overlayY:`center`}:(i==`after`||i==`right`&&e||i==`left`&&!e)&&(a={overlayX:`start`,overlayY:`center`});let{x:o,y:r}=this._invertPosition(a.overlayX,a.overlayY);return{main:a,fallback:{overlayX:o,overlayY:r}}}_updateTooltipMessage(){this._tooltipInstance&&(this._tooltipInstance.message=this.message,this._tooltipInstance._markForCheck(),Rt(()=>{this._tooltipInstance&&this._overlayRef.updatePosition()},{injector:this._injector}))}_setTooltipClass(e){this._tooltipInstance&&(this._tooltipInstance.tooltipClass=e instanceof Set?Array.from(e):e,this._tooltipInstance._markForCheck())}_invertPosition(e,i){return this.position===`above`||this.position===`below`?i===`top`?i=`bottom`:i===`bottom`&&(i=`top`):e===`end`?e=`start`:e===`start`&&(e=`end`),{x:e,y:i}}_updateCurrentPositionClass(e){let{overlayY:i,originX:a,originY:o}=e,r;if(i===`center`?this._dir&&this._dir.value===`rtl`?r=a===`end`?`left`:`right`:r=a===`start`?`left`:`right`:r=i===`bottom`&&o===`top`?`above`:`below`,r!==this._currentPosition){let s=this._overlayRef;if(s){let l=`${this._cssClassPrefix}-${kw}-`;s.removePanelClass(l+this._currentPosition),s.addPanelClass(l+r)}this._currentPosition=r}}_setupPointerEnterEventsIfNeeded(){this._disabled||!this.message||!this._viewInitialized||this._eventCleanups.length||(this._isTouchPlatform()?this.touchGestures!==`off`&&(this._disableNativeGesturesIfNecessary(),this._addListener(`touchstart`,e=>{let i=e.targetTouches?.[0],a=i?{x:i.clientX,y:i.clientY}:void 0;this._setupPointerExitEventsIfNeeded(),this._touchstartTimeout&&clearTimeout(this._touchstartTimeout);let o=500;this._touchstartTimeout=setTimeout(()=>{this._touchstartTimeout=null,this.show(void 0,a)},this._defaultOptions?.touchLongPressShowDelay??o)})):this._addListener(`mouseenter`,e=>{this._setupPointerExitEventsIfNeeded();let i;e.x!==void 0&&e.y!==void 0&&(i=e),this.show(void 0,i)}))}_setupPointerExitEventsIfNeeded(){if(!this._pointerExitEventsInitialized){if(this._pointerExitEventsInitialized=!0,!this._isTouchPlatform())this._addListener(`mouseleave`,e=>{let i=e.relatedTarget;(!i||!this._overlayRef?.overlayElement.contains(i))&&this.hide()}),this._addListener(`wheel`,e=>{if(this._isTooltipVisible()){let i=this._document.elementFromPoint(e.clientX,e.clientY),a=this._elementRef.nativeElement;i!==a&&!a.contains(i)&&this.hide()}});else if(this.touchGestures!==`off`){this._disableNativeGesturesIfNecessary();let e=()=>{this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this.hide(this._defaultOptions?.touchendHideDelay)};this._addListener(`touchend`,e),this._addListener(`touchcancel`,e)}}}_addListener(e,i){this._eventCleanups.push(this._renderer.listen(this._elementRef.nativeElement,e,i,nO))}_isTouchPlatform(){let e=this._defaultOptions?.detectHoverCapability;return typeof e==`function`?!e():this._platform.IOS||this._platform.ANDROID?!0:this._platform.isBrowser?!!e&&this._mediaMatcher.matchMedia(`(any-hover: none)`).matches:!1}_disableNativeGesturesIfNecessary(){let e=this.touchGestures;if(e!==`off`){let i=this._elementRef.nativeElement,a=i.style;(e===`on`||i.nodeName!==`INPUT`&&i.nodeName!==`TEXTAREA`)&&(a.userSelect=a.msUserSelect=a.webkitUserSelect=a.MozUserSelect=`none`),(e===`on`||!i.draggable)&&(a.webkitUserDrag=`none`),a.touchAction=`none`,a.webkitTapHighlightColor=`transparent`}}_syncAriaDescription(e){this._ariaDescriptionPending||(this._ariaDescriptionPending=!0,this._ariaDescriber.removeDescription(this._elementRef.nativeElement,e,`tooltip`),this._isDestroyed||Rt({write:()=>{this._ariaDescriptionPending=!1,this.message&&!this.disabled&&this._ariaDescriber.describe(this._elementRef.nativeElement,this.message,`tooltip`)}},{injector:this._injector}))}_overlayEventPredicate=e=>e.type===`keydown`?this._isTooltipVisible()&&e.keyCode===27&&!Ct(e):!0;static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`matTooltip`,``]],hostAttrs:[1,`mat-mdc-tooltip-trigger`],hostVars:2,hostBindings:function(i,a){i&2&&G(`mat-mdc-tooltip-disabled`,a.disabled)},inputs:{position:[0,`matTooltipPosition`,`position`],positionAtOrigin:[0,`matTooltipPositionAtOrigin`,`positionAtOrigin`],disabled:[0,`matTooltipDisabled`,`disabled`],showDelay:[0,`matTooltipShowDelay`,`showDelay`],hideDelay:[0,`matTooltipHideDelay`,`hideDelay`],touchGestures:[0,`matTooltipTouchGestures`,`touchGestures`],message:[0,`matTooltip`,`message`],tooltipClass:[0,`matTooltipClass`,`tooltipClass`]},exportAs:[`matTooltip`]})}return t})();var sO=(()=>{class t{_changeDetectorRef=f(Le);_elementRef=f(Y);_isMultiline=!1;message;tooltipClass;_showTimeoutId;_hideTimeoutId;_triggerElement;_mouseLeaveHideDelay;_animationsDisabled=Ue();_tooltip;_closeOnInteraction=!1;_isVisible=!1;_onHide=new V;_showAnimation=`mat-mdc-tooltip-show`;_hideAnimation=`mat-mdc-tooltip-hide`;show(e){this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=setTimeout(()=>{this._toggleVisibility(!0),this._showTimeoutId=void 0},e)}hide(e){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId=setTimeout(()=>{this._toggleVisibility(!1),this._hideTimeoutId=void 0},e)}afterHidden(){return this._onHide}isVisible(){return this._isVisible}ngOnDestroy(){this._cancelPendingAnimations(),this._onHide.complete(),this._triggerElement=null}_handleBodyInteraction(){this._closeOnInteraction&&this.hide(0)}_markForCheck(){this._changeDetectorRef.markForCheck()}_handleMouseLeave({relatedTarget:e}){(!e||!this._triggerElement.contains(e))&&(this.isVisible()?this.hide(this._mouseLeaveHideDelay):this._finalizeAnimation(!1))}_onShow(){this._isMultiline=this._isTooltipMultiline(),this._markForCheck()}_isTooltipMultiline(){let e=this._elementRef.nativeElement.getBoundingClientRect();return e.height>oO&&e.width>=rO}_handleAnimationEnd({animationName:e}){(e===this._showAnimation||e===this._hideAnimation)&&this._finalizeAnimation(e===this._showAnimation)}_cancelPendingAnimations(){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=this._hideTimeoutId=void 0}_finalizeAnimation(e){e?this._closeOnInteraction=!0:this.isVisible()||this._onHide.next()}_toggleVisibility(e){let i=this._tooltip.nativeElement,a=this._showAnimation,o=this._hideAnimation;if(i.classList.remove(e?o:a),i.classList.add(e?a:o),this._isVisible!==e&&(this._isVisible=e,this._changeDetectorRef.markForCheck()),e&&!this._animationsDisabled&&typeof getComputedStyle==`function`){let r=getComputedStyle(i);(r.getPropertyValue(`animation-duration`)===`0s`||r.getPropertyValue(`animation-name`)===`none`)&&(this._animationsDisabled=!0)}e&&this._onShow(),this._animationsDisabled&&(i.classList.add(`_mat-animation-noopable`),this._finalizeAnimation(e))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`tooltip`];return H({type:t,selectors:[[`mat-tooltip-component`]],viewQuery:function(a,o){if(a&1&&Ze(e,7),a&2){let r;J(r=ee())&&(o._tooltip=r.first)}},hostAttrs:[`aria-hidden`,`true`],hostBindings:function(a,o){a&1&&C(`mouseleave`,function(s){return o._handleMouseLeave(s)})},decls:4,vars:5,consts:[[`tooltip`,``],[1,`mdc-tooltip`,`mat-mdc-tooltip`,3,`animationend`],[1,`mat-mdc-tooltip-surface`,`mdc-tooltip__surface`]],template:function(a,o){a&1&&(ct(0,`div`,1,0),Tr(`animationend`,function(s){return o._handleAnimationEnd(s)}),ct(2,`div`,2),u(3),ht()()),a&2&&(tt(o.tooltipClass),G(`mdc-tooltip--multiline`,o._isMultiline),m(3),w(o.message))},styles:[`.mat-mdc-tooltip {
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
`],encapsulation:2})})()}return t})();var Tw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[No,mi,we,ia]})}return t})();var lO=(()=>{class t{changes=new V;itemsPerPageLabel=`Items per page:`;nextPageLabel=`Next page`;previousPageLabel=`Previous page`;firstPageLabel=`First page`;lastPageLabel=`Last page`;getRangeLabel=(e,i,a)=>{if(a==0||i==0)return`0 of ${a}`;a=Math.max(a,0);let o=e*i,r=o<a?Math.min(o+i,a):o+i;return`${o+1} \u2013 ${r} of ${a}`};static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var cO=50;var dO=new N(`MAT_PAGINATOR_DEFAULT_OPTIONS`);var Ql=(()=>{class t{_intl=f(lO);_changeDetectorRef=f(Le);_formFieldAppearance;_pageSizeLabelId=f(je).getId(`mat-paginator-page-size-label-`);_intlChanges;_isInitialized=!1;_initializedStream=new Yn(1);color;get pageIndex(){return this._pageIndex}set pageIndex(e){this._pageIndex=Math.max(e||0,0),this._changeDetectorRef.markForCheck()}_pageIndex=0;get length(){return this._length}set length(e){this._length=e||0,this._changeDetectorRef.markForCheck()}_length=0;get pageSize(){return this._pageSize}set pageSize(e){this._pageSize=Math.max(e||0,0),this._updateDisplayedPageSizeOptions()}_pageSize;get pageSizeOptions(){return this._pageSizeOptions}set pageSizeOptions(e){this._pageSizeOptions=(e||[]).map(i=>dt(i,0)),this._updateDisplayedPageSizeOptions()}_pageSizeOptions=[];hidePageSize=!1;showFirstLastButtons=!1;selectConfig={};disabled=!1;page=new te;_displayedPageSizeOptions;initialized=this._initializedStream;constructor(){let e=this._intl,i=f(dO,{optional:!0});if(this._intlChanges=e.changes.subscribe(()=>this._changeDetectorRef.markForCheck()),i){let{pageSize:a,pageSizeOptions:o,hidePageSize:r,showFirstLastButtons:s}=i;a!=null&&(this._pageSize=a),o!=null&&(this._pageSizeOptions=o),r!=null&&(this.hidePageSize=r),s!=null&&(this.showFirstLastButtons=s)}this._formFieldAppearance=i?.formFieldAppearance||`outline`}ngOnInit(){this._isInitialized=!0,this._updateDisplayedPageSizeOptions(),this._initializedStream.next()}ngOnDestroy(){this._initializedStream.complete(),this._intlChanges.unsubscribe()}nextPage(){this.hasNextPage()&&this._navigate(this.pageIndex+1)}previousPage(){this.hasPreviousPage()&&this._navigate(this.pageIndex-1)}firstPage(){this.hasPreviousPage()&&this._navigate(0)}lastPage(){this.hasNextPage()&&this._navigate(this.getNumberOfPages()-1)}hasPreviousPage(){return this.pageIndex>=1&&this.pageSize!=0}hasNextPage(){let e=this.getNumberOfPages()-1;return this.pageIndex<e&&this.pageSize!=0}getNumberOfPages(){return this.pageSize?Math.ceil(this.length/this.pageSize):0}_changePageSize(e){let i=this.pageIndex*this.pageSize,a=this.pageIndex;this.pageIndex=Math.floor(i/e)||0,this.pageSize=e,this._emitPageEvent(a)}_nextButtonsDisabled(){return this.disabled||!this.hasNextPage()}_previousButtonsDisabled(){return this.disabled||!this.hasPreviousPage()}_updateDisplayedPageSizeOptions(){this._isInitialized&&(this.pageSize||(this._pageSize=this.pageSizeOptions.length!=0?this.pageSizeOptions[0]:cO),this._displayedPageSizeOptions=this.pageSizeOptions.slice(),this._displayedPageSizeOptions.indexOf(this.pageSize)===-1&&this._displayedPageSizeOptions.push(this.pageSize),this._displayedPageSizeOptions.sort((e,i)=>e-i),this._changeDetectorRef.markForCheck())}_emitPageEvent(e){this.page.emit({previousPageIndex:e,pageIndex:this.pageIndex,pageSize:this.pageSize,length:this.length})}_navigate(e){let i=this.pageIndex;e!==i&&(this.pageIndex=e,this._emitPageEvent(i))}_buttonClicked(e,i){i||this._navigate(e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(l,h){if(l&1&&(c(0,`mat-option`,17),u(1),d()),l&2){let p=h.$implicit;S(`value`,p),m(),B(` `,p,` `)}}function i(l,h){if(l&1){let p=$();c(0,`mat-form-field`,14)(1,`mat-select`,16,0),C(`selectionChange`,function(_){k(p);return M(v(2)._changePageSize(_.value))}),de(3,e,2,2,`mat-option`,17,Pn),d(),c(5,`div`,18),C(`click`,function(){k(p);return M(ft(2).open())}),d()()}if(l&2){let p=v(2);S(`appearance`,p._formFieldAppearance)(`color`,p.color),m(),S(`value`,p.pageSize)(`disabled`,p.disabled),Mr(`aria-labelledby`,p._pageSizeLabelId),S(`panelClass`,p.selectConfig.panelClass||``)(`disableOptionCentering`,p.selectConfig.disableOptionCentering),m(2),ue(p._displayedPageSizeOptions)}}function a(l,h){if(l&1&&(c(0,`div`,15),u(1),d()),l&2){let p=v(2);m(),w(p.pageSize)}}function o(l,h){if(l&1&&(c(0,`div`,3)(1,`div`,13),u(2),d(),E(3,i,6,7,`mat-form-field`,14),E(4,a,2,1,`div`,15),d()),l&2){let p=v();m(),ne(`id`,p._pageSizeLabelId),m(),B(` `,p._intl.itemsPerPageLabel,` `),m(),D(p._displayedPageSizeOptions.length>1?3:-1),m(),D(p._displayedPageSizeOptions.length<=1?4:-1)}}function r(l,h){if(l&1){let p=$();c(0,`button`,19),C(`click`,function(){k(p);let _=v();return M(_._buttonClicked(0,_._previousButtonsDisabled()))}),Gt(),c(1,`svg`,8),U(2,`path`,20),d()()}if(l&2){let p=v();S(`matTooltip`,p._intl.firstPageLabel)(`matTooltipDisabled`,p._previousButtonsDisabled())(`disabled`,p._previousButtonsDisabled())(`tabindex`,p._previousButtonsDisabled()?-1:null),ne(`aria-label`,p._intl.firstPageLabel)}}function s(l,h){if(l&1){let p=$();c(0,`button`,21),C(`click`,function(){k(p);let _=v();return M(_._buttonClicked(_.getNumberOfPages()-1,_._nextButtonsDisabled()))}),Gt(),c(1,`svg`,8),U(2,`path`,22),d()()}if(l&2){let p=v();S(`matTooltip`,p._intl.lastPageLabel)(`matTooltipDisabled`,p._nextButtonsDisabled())(`disabled`,p._nextButtonsDisabled())(`tabindex`,p._nextButtonsDisabled()?-1:null),ne(`aria-label`,p._intl.lastPageLabel)}}return H({type:t,selectors:[[`mat-paginator`]],hostAttrs:[`role`,`group`,1,`mat-mdc-paginator`],inputs:{color:`color`,pageIndex:[2,`pageIndex`,`pageIndex`,dt],length:[2,`length`,`length`,dt],pageSize:[2,`pageSize`,`pageSize`,dt],pageSizeOptions:`pageSizeOptions`,hidePageSize:[2,`hidePageSize`,`hidePageSize`,z],showFirstLastButtons:[2,`showFirstLastButtons`,`showFirstLastButtons`,z],selectConfig:`selectConfig`,disabled:[2,`disabled`,`disabled`,z]},outputs:{page:`page`},exportAs:[`matPaginator`],decls:14,vars:14,consts:[[`selectRef`,``],[1,`mat-mdc-paginator-outer-container`],[1,`mat-mdc-paginator-container`],[1,`mat-mdc-paginator-page-size`],[1,`mat-mdc-paginator-range-actions`],[`aria-atomic`,`true`,`aria-live`,`polite`,`role`,`status`,1,`mat-mdc-paginator-range-label`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-first`,3,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-previous`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`viewBox`,`0 0 24 24`,`focusable`,`false`,`aria-hidden`,`true`,1,`mat-mdc-paginator-icon`],[`d`,`M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-next`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-last`,3,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`aria-hidden`,`true`,1,`mat-mdc-paginator-page-size-label`],[1,`mat-mdc-paginator-page-size-select`,3,`appearance`,`color`],[1,`mat-mdc-paginator-page-size-value`],[`hideSingleSelectionIndicator`,``,3,`selectionChange`,`value`,`disabled`,`aria-labelledby`,`panelClass`,`disableOptionCentering`],[3,`value`],[1,`mat-mdc-paginator-touch-target`,3,`click`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-first`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M18.41 16.59L13.82 12l4.59-4.59L17 6l-6 6 6 6zM6 6h2v12H6z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-last`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M5.59 7.41L10.18 12l-4.59 4.59L7 18l6-6-6-6zM16 6h2v12h-2z`]],template:function(h,p){h&1&&(c(0,`div`,1)(1,`div`,2),E(2,o,5,4,`div`,3),c(3,`div`,4)(4,`div`,5),u(5),d(),E(6,r,3,5,`button`,6),c(7,`button`,7),C(`click`,function(){return p._buttonClicked(p.pageIndex-1,p._previousButtonsDisabled())}),Gt(),c(8,`svg`,8),U(9,`path`,9),d()(),ya(),c(10,`button`,10),C(`click`,function(){return p._buttonClicked(p.pageIndex+1,p._nextButtonsDisabled())}),Gt(),c(11,`svg`,8),U(12,`path`,11),d()(),E(13,s,3,5,`button`,12),d()()()),h&2&&(m(2),D(p.hidePageSize?-1:2),m(3),B(` `,p._intl.getRangeLabel(p.pageIndex,p.pageSize,p.length),` `),m(),D(p.showFirstLastButtons?6:-1),m(),S(`matTooltip`,p._intl.previousPageLabel)(`matTooltipDisabled`,p._previousButtonsDisabled())(`disabled`,p._previousButtonsDisabled())(`tabindex`,p._previousButtonsDisabled()?-1:null),ne(`aria-label`,p._intl.previousPageLabel),m(3),S(`matTooltip`,p._intl.nextPageLabel)(`matTooltipDisabled`,p._nextButtonsDisabled())(`disabled`,p._nextButtonsDisabled())(`tabindex`,p._nextButtonsDisabled()?-1:null),ne(`aria-label`,p._intl.nextPageLabel),m(3),D(p.showFirstLastButtons?13:-1))},dependencies:[Ti,Pa,Un,Zg,Mw],styles:[`.mat-mdc-paginator {
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
`],encapsulation:2})})()}return t})();var gm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[Ft,Fa,Tw,Ql]})}return t})();var uO=new N(`MAT_PROGRESS_BAR_DEFAULT_OPTIONS`);var cs=(()=>{class t{_elementRef=f(Y);_ngZone=f(Z);_changeDetectorRef=f(Le);_renderer=f(Pe);_cleanupTransitionEnd;constructor(){let e=Tl(),i=f(uO,{optional:!0});this._isNoopAnimation=e===`di-disabled`,e===`reduced-motion`&&this._elementRef.nativeElement.classList.add(`mat-progress-bar-reduced-motion`),i&&(i.color&&(this.color=this._defaultColor=i.color),this.mode=i.mode||this.mode)}_isNoopAnimation;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor=`primary`;get value(){return this._value}set value(e){this._value=Aw(e||0),this._changeDetectorRef.markForCheck()}_value=0;get bufferValue(){return this._bufferValue||0}set bufferValue(e){this._bufferValue=Aw(e||0),this._changeDetectorRef.markForCheck()}_bufferValue=0;animationEnd=new te;get mode(){return this._mode}set mode(e){this._mode=e,this._changeDetectorRef.markForCheck()}_mode=`determinate`;ngAfterViewInit(){this._ngZone.runOutsideAngular(()=>{this._cleanupTransitionEnd=this._renderer.listen(this._elementRef.nativeElement,`transitionend`,this._transitionendHandler)})}ngOnDestroy(){this._cleanupTransitionEnd?.()}_getPrimaryBarTransform(){return`scaleX(${this._isIndeterminate()?1:this.value/100})`}_getBufferBarFlexBasis(){return`${this.mode===`buffer`?this.bufferValue:100}%`}_isIndeterminate(){return this.mode===`indeterminate`||this.mode===`query`}_transitionendHandler=e=>{this.animationEnd.observers.length===0||!e.target||!e.target.classList.contains(`mdc-linear-progress__primary-bar`)||(this.mode===`determinate`||this.mode===`buffer`)&&this._ngZone.run(()=>this.animationEnd.next({value:this.value}))};static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(i,a){i&1&&Wt(0,`div`,2)}return H({type:t,selectors:[[`mat-progress-bar`]],hostAttrs:[`role`,`progressbar`,`aria-valuemin`,`0`,`aria-valuemax`,`100`,`tabindex`,`-1`,1,`mat-mdc-progress-bar`,`mdc-linear-progress`],hostVars:10,hostBindings:function(a,o){a&2&&(ne(`aria-valuenow`,o._isIndeterminate()?null:o.value)(`mode`,o.mode),tt(`mat-`+o.color),G(`_mat-animation-noopable`,o._isNoopAnimation)(`mdc-linear-progress--animation-ready`,!o._isNoopAnimation)(`mdc-linear-progress--indeterminate`,o._isIndeterminate()))},inputs:{color:`color`,value:[2,`value`,`value`,dt],bufferValue:[2,`bufferValue`,`bufferValue`,dt],mode:`mode`},outputs:{animationEnd:`animationEnd`},exportAs:[`matProgressBar`],decls:7,vars:5,consts:[[`aria-hidden`,`true`,1,`mdc-linear-progress__buffer`],[1,`mdc-linear-progress__buffer-bar`],[1,`mdc-linear-progress__buffer-dots`],[`aria-hidden`,`true`,1,`mdc-linear-progress__bar`,`mdc-linear-progress__primary-bar`],[1,`mdc-linear-progress__bar-inner`],[`aria-hidden`,`true`,1,`mdc-linear-progress__bar`,`mdc-linear-progress__secondary-bar`]],template:function(a,o){a&1&&(ct(0,`div`,0),Wt(1,`div`,1),E(2,e,1,0,`div`,2),ht(),ct(3,`div`,3),Wt(4,`span`,4),ht(),ct(5,`div`,5),Wt(6,`span`,4),ht()),a&2&&(m(),Jt(`flex-basis`,o._getBufferBarFlexBasis()),m(),D(o.mode===`buffer`?2:-1),m(),Jt(`transform`,o._getPrimaryBarTransform()))},styles:[`.mat-mdc-progress-bar {
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
`],encapsulation:2})})()}return t})();function Aw(t,n=0,e=100){return Math.max(n,Math.min(e,t))}var ds=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[we]})}return t})();var mO=new N(`MatTabContent`);var Xl=(()=>{class t{template=f(Vt);static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`matTabContent`,``]],features:[Ge([{provide:mO,useExisting:t}])]})}return t})();var pO=new N(`MatTabLabel`);var Fw=new N(`MAT_TAB`);var hO=(()=>{class t extends lw{_closestTab=f(Fw,{optional:!0});static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`mat-tab-label`,``],[``,`matTabLabel`,``]],features:[Ge([{provide:pO,useExisting:t}]),Be]})}return t})();var Lw=new N(`MAT_TAB_GROUP`);var Zl=(()=>{class t{_viewContainerRef=f(Bt);_closestTabGroup=f(Lw,{optional:!0});disabled=!1;get templateLabel(){return this._templateLabel}set templateLabel(e){this._setTemplateLabelInput(e)}_templateLabel;_explicitContent=void 0;_implicitContent;textLabel=``;ariaLabel;ariaLabelledby;labelClass;bodyClass;id=null;_contentPortal=null;get content(){return this._contentPortal}_stateChanges=new V;position=null;origin=null;isActive=!1;constructor(){f(Je).load(pn)}ngOnChanges(e){(Object.hasOwn(e,`textLabel`)||Object.hasOwn(e,`disabled`))&&this._stateChanges.next()}ngOnDestroy(){this._stateChanges.complete()}ngOnInit(){this._contentPortal=new Hn(this._explicitContent||this._implicitContent,this._viewContainerRef)}_setTemplateLabelInput(e){e&&e._closestTab===this&&(this._templateLabel=e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`];function i(a,o){a&1&&he(0)}return H({type:t,selectors:[[`mat-tab`]],contentQueries:function(o,r,s){if(o&1&&jt(s,hO,5)(s,Xl,7,Vt),o&2){let l;J(l=ee())&&(r.templateLabel=l.first),J(l=ee())&&(r._explicitContent=l.first)}},viewQuery:function(o,r){if(o&1&&Ze(Vt,7),o&2){let s;J(s=ee())&&(r._implicitContent=s.first)}},hostAttrs:[`hidden`,``],hostVars:1,hostBindings:function(o,r){o&2&&ne(`id`,null)},inputs:{disabled:[2,`disabled`,`disabled`,z],textLabel:[0,`label`,`textLabel`],ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],labelClass:`labelClass`,bodyClass:`bodyClass`,id:`id`},exportAs:[`matTab`],features:[Ge([{provide:Fw,useExisting:t}]),_t],ngContentSelectors:e,decls:1,vars:0,template:function(o,r){o&1&&(Fe(),kr(0,i,1,0,`ng-template`))},encapsulation:2,changeDetection:1})})()}return t})();var Ab=`mdc-tab-indicator--active`;var Rw=`mdc-tab-indicator--no-transition`;var Ob=class{_items;_currentItem;constructor(n){this._items=n}hide(){this._items.forEach(n=>n.deactivateInkBar()),this._currentItem=void 0}alignToElement(n){let e=this._items.find(a=>a.elementRef.nativeElement===n),i=this._currentItem;if(e!==i&&(i?.deactivateInkBar(),e)){let a=i?.elementRef.nativeElement.getBoundingClientRect?.();e.activateInkBar(a),this._currentItem=e}}};var fO=(()=>{class t{_elementRef=f(Y);_inkBarElement=null;_inkBarContentElement=null;_fitToContent=!1;get fitInkBarToContent(){return this._fitToContent}set fitInkBarToContent(e){this._fitToContent!==e&&(this._fitToContent=e,this._inkBarElement&&this._appendInkBarElement())}activateInkBar(e){let i=this._elementRef.nativeElement;if(!e||!i.getBoundingClientRect||!this._inkBarContentElement){i.classList.add(Ab);return}let a=i.getBoundingClientRect(),o=e.width/a.width,r=e.left-a.left;i.classList.add(Rw),this._inkBarContentElement.style.setProperty(`transform`,`translateX(${r}px) scaleX(${o})`),i.getBoundingClientRect(),i.classList.remove(Rw),i.classList.add(Ab),this._inkBarContentElement.style.setProperty(`transform`,``)}deactivateInkBar(){this._elementRef.nativeElement.classList.remove(Ab)}ngOnInit(){this._createInkBarElement()}ngOnDestroy(){this._inkBarElement?.remove(),this._inkBarElement=this._inkBarContentElement=null}_createInkBarElement(){let e=this._elementRef.nativeElement.ownerDocument||document,i=this._inkBarElement=e.createElement(`span`),a=this._inkBarContentElement=e.createElement(`span`);i.className=`mdc-tab-indicator`,a.className=`mdc-tab-indicator__content mdc-tab-indicator__content--underline`,i.appendChild(this._inkBarContentElement),this._appendInkBarElement()}_appendInkBarElement(){this._inkBarElement;(this._fitToContent?this._elementRef.nativeElement.querySelector(`.mdc-tab__content`):this._elementRef.nativeElement).appendChild(this._inkBarElement)}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,inputs:{fitInkBarToContent:[2,`fitInkBarToContent`,`fitInkBarToContent`,z]}})}return t})();var Vw=(()=>{class t extends fO{elementRef=f(Y);disabled=!1;focus(){this.elementRef.nativeElement.focus()}getOffsetLeft(){return this.elementRef.nativeElement.offsetLeft}getOffsetWidth(){return this.elementRef.nativeElement.offsetWidth}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`matTabLabelWrapper`,``]],hostVars:3,hostBindings:function(i,a){i&2&&(ne(`aria-disabled`,!!a.disabled),G(`mat-mdc-tab-disabled`,a.disabled))},inputs:{disabled:[2,`disabled`,`disabled`,z]},features:[Be]})}return t})();var Ow={passive:!0};var gO=650;var bO=100;function Rb(t){let n=t+``;return/^[0-9]+(?:\.[0-9]+)?$/.test(n)?`${t}ms`:/^[0-9]+(?:\.[0-9]+)?(?:ms|s)$/.test(n)?n:``}var vO=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Le);_viewportRuler=f(ui);_dir=f(gt,{optional:!0});_ngZone=f(Z);_platform=f(Re);_sharedResizeObserver=f(tm);_injector=f(se);_renderer=f(Pe);_animationsDisabled=Ue();_eventCleanups;_scrollDistance=0;_selectedIndexChanged=!1;_destroyed=new V;_showPaginationControls=!1;_disableScrollAfter=!0;_disableScrollBefore=!0;_tabLabelCount;_scrollDistanceChanged=!1;_keyManager;_currentTextContent;_stopScrolling=new V;disablePagination=!1;get selectedIndex(){return this._selectedIndex}set selectedIndex(e){let i=isNaN(e)?0:e;this._selectedIndex!=i&&(this._selectedIndexChanged=!0,this._selectedIndex=i,this._keyManager&&this._keyManager.updateActiveItem(i))}_selectedIndex=0;selectFocusedIndex=new te;indexFocused=new te;constructor(){this._eventCleanups=this._ngZone.runOutsideAngular(()=>[this._renderer.listen(this._elementRef.nativeElement,`mouseleave`,()=>this._stopInterval())])}ngAfterViewInit(){this._eventCleanups.push(this._renderer.listen(this._previousPaginator.nativeElement,`touchstart`,()=>this._handlePaginatorPress(`before`),Ow),this._renderer.listen(this._nextPaginator.nativeElement,`touchstart`,()=>this._handlePaginatorPress(`after`),Ow))}ngAfterContentInit(){let e=this._dir?this._dir.change:Mn(`ltr`),i=this._sharedResizeObserver.observe(this._elementRef.nativeElement).pipe(Pi(32),Ne(this._destroyed)),a=this._viewportRuler.change(150).pipe(Ne(this._destroyed)),o=()=>{this.updatePagination(),this._alignInkBarToSelectedTab()};this._keyManager=new ea(this._items).withHorizontalOrientation(this._getLayoutDirection()).withHomeAndEnd().withWrap().skipPredicate(()=>!1),this._keyManager.updateActiveItem(Math.max(this._selectedIndex,0)),Rt(o,{injector:this._injector}),kt(e,a,i,this._items.changes,this._itemsResized()).pipe(Ne(this._destroyed)).subscribe(()=>{this._ngZone.run(()=>{Promise.resolve().then(()=>{this._scrollDistance=Math.max(0,Math.min(this._getMaxScrollDistance(),this._scrollDistance)),o()})}),this._keyManager?.withHorizontalOrientation(this._getLayoutDirection())}),this._keyManager.change.subscribe(r=>{this.indexFocused.emit(r),this._setTabFocus(r)})}_itemsResized(){return typeof ResizeObserver!=`function`?Oi:this._items.changes.pipe(mt(this._items),_n(e=>new Ce(i=>this._ngZone.runOutsideAngular(()=>{let a=new ResizeObserver(o=>i.next(o));return e.forEach(o=>a.observe(o.elementRef.nativeElement)),()=>{a.disconnect()}}))),Za(1),Qe(e=>e.some(i=>i.contentRect.width>0&&i.contentRect.height>0)))}ngAfterContentChecked(){this._tabLabelCount!=this._items.length&&(this.updatePagination(),this._tabLabelCount=this._items.length,this._changeDetectorRef.markForCheck()),this._selectedIndexChanged&&(this._scrollToLabel(this._selectedIndex),this._checkScrollingControls(),this._alignInkBarToSelectedTab(),this._selectedIndexChanged=!1,this._changeDetectorRef.markForCheck()),this._scrollDistanceChanged&&(this._updateTabScrollPosition(),this._scrollDistanceChanged=!1,this._changeDetectorRef.markForCheck())}ngOnDestroy(){this._eventCleanups.forEach(e=>e()),this._keyManager?.destroy(),this._destroyed.next(),this._destroyed.complete(),this._stopScrolling.complete()}_handleKeydown(e){if(!Ct(e))switch(e.keyCode){case 13:case 32:if(this.focusIndex!==this.selectedIndex){let i=this._items.get(this.focusIndex);i&&!i.disabled&&(this.selectFocusedIndex.emit(this.focusIndex),this._itemSelected(e))}break;default:this._keyManager?.onKeydown(e)}}_onContentChanges(){let e=this._elementRef.nativeElement.textContent;e!==this._currentTextContent&&(this._currentTextContent=e||``,this._ngZone.run(()=>{this.updatePagination(),this._alignInkBarToSelectedTab(),this._changeDetectorRef.markForCheck()}))}updatePagination(){this._checkPaginationEnabled(),this._checkScrollingControls(),this._updateTabScrollPosition()}get focusIndex(){return this._keyManager?this._keyManager.activeItemIndex:0}set focusIndex(e){!this._isValidIndex(e)||this.focusIndex===e||!this._keyManager||this._keyManager.setActiveItem(e)}_isValidIndex(e){return this._items?!!this._items.toArray()[e]:!0}_setTabFocus(e){if(this._showPaginationControls&&this._scrollToLabel(e),this._items&&this._items.length){this._items.toArray()[e].focus();let i=this._tabListContainer.nativeElement;this._getLayoutDirection()==`ltr`?i.scrollLeft=0:i.scrollLeft=i.scrollWidth-i.offsetWidth}}_getLayoutDirection(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_updateTabScrollPosition(){if(this.disablePagination)return;let e=this.scrollDistance,i=this._getLayoutDirection()===`ltr`?-e:e;this._tabList.nativeElement.style.transform=`translateX(${Math.round(i)}px)`,(this._platform.TRIDENT||this._platform.EDGE)&&(this._tabListContainer.nativeElement.scrollLeft=0)}get scrollDistance(){return this._scrollDistance}set scrollDistance(e){this._scrollTo(e)}_scrollHeader(e){let i=this._tabListContainer.nativeElement.offsetWidth,a=(e==`before`?-1:1)*i/3;return this._scrollTo(this._scrollDistance+a)}_handlePaginatorClick(e){this._stopInterval(),this._scrollHeader(e)}_scrollToLabel(e){if(this.disablePagination)return;let i=this._items?this._items.toArray()[e]:null;if(!i)return;let a=this._tabListContainer.nativeElement.offsetWidth,{offsetLeft:o,offsetWidth:r}=i.elementRef.nativeElement,s,l;this._getLayoutDirection()==`ltr`?(s=o,l=s+r):(l=this._tabListInner.nativeElement.offsetWidth-o,s=l-r);let h=this.scrollDistance,p=this.scrollDistance+a;s<h?this.scrollDistance-=h-s:l>p&&(this.scrollDistance+=Math.min(l-p,s-h))}_checkPaginationEnabled(){if(this.disablePagination)this._showPaginationControls=!1;else{let a=this._tabListInner.nativeElement.scrollWidth-this._elementRef.nativeElement.offsetWidth>=5;a||(this.scrollDistance=0),a!==this._showPaginationControls&&(this._showPaginationControls=a,this._changeDetectorRef.markForCheck())}}_checkScrollingControls(){this.disablePagination?this._disableScrollAfter=this._disableScrollBefore=!0:(this._disableScrollBefore=this.scrollDistance==0,this._disableScrollAfter=this.scrollDistance==this._getMaxScrollDistance(),this._changeDetectorRef.markForCheck())}_getMaxScrollDistance(){return this._tabListInner.nativeElement.scrollWidth-this._tabListContainer.nativeElement.offsetWidth||0}_alignInkBarToSelectedTab(){let e=this._items&&this._items.length?this._items.toArray()[this.selectedIndex]:null,i=e?e.elementRef.nativeElement:null;i?this._inkBar.alignToElement(i):this._inkBar.hide()}_stopInterval(){this._stopScrolling.next()}_handlePaginatorPress(e,i){i&&i.button!=null&&i.button!==0||(this._stopInterval(),xs(gO,bO).pipe(Ne(kt(this._stopScrolling,this._destroyed))).subscribe(()=>{let{maxScrollDistance:a,distance:o}=this._scrollHeader(e);(o===0||o>=a)&&this._stopInterval()}))}_scrollTo(e){if(this.disablePagination)return{maxScrollDistance:0,distance:0};let i=this._getMaxScrollDistance();return this._scrollDistance=Math.max(0,Math.min(i,e)),this._scrollDistanceChanged=!0,this._checkScrollingControls(),{maxScrollDistance:i,distance:this._scrollDistance}}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,inputs:{disablePagination:[2,`disablePagination`,`disablePagination`,z],selectedIndex:[2,`selectedIndex`,`selectedIndex`,dt]},outputs:{selectFocusedIndex:`selectFocusedIndex`,indexFocused:`indexFocused`}})}return t})();var _O=(()=>{class t extends vO{_items;_tabListContainer;_tabList;_tabListInner;_nextPaginator;_previousPaginator;_inkBar;ariaLabel;ariaLabelledby;disableRipple=!1;ngAfterContentInit(){this._inkBar=new Ob(this._items),super.ngAfterContentInit()}_itemSelected(e){e.preventDefault()}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵcmp=(function(){let e=[`tabListContainer`],i=[`tabList`],a=[`tabListInner`],o=[`nextPaginator`],r=[`previousPaginator`];return H({type:t,selectors:[[`mat-tab-header`]],contentQueries:function(h,p,g){if(h&1&&jt(g,Vw,4),h&2){let _;J(_=ee())&&(p._items=_)}},viewQuery:function(h,p){if(h&1&&Ze(e,7)(i,7)(a,7)(o,5)(r,5),h&2){let g;J(g=ee())&&(p._tabListContainer=g.first),J(g=ee())&&(p._tabList=g.first),J(g=ee())&&(p._tabListInner=g.first),J(g=ee())&&(p._nextPaginator=g.first),J(g=ee())&&(p._previousPaginator=g.first)}},hostAttrs:[1,`mat-mdc-tab-header`],hostVars:4,hostBindings:function(h,p){h&2&&G(`mat-mdc-tab-header-pagination-controls-enabled`,p._showPaginationControls)(`mat-mdc-tab-header-rtl`,p._getLayoutDirection()==`rtl`)},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],disableRipple:[2,`disableRipple`,`disableRipple`,z]},features:[Be],ngContentSelectors:[`*`],decls:13,vars:10,consts:[[`previousPaginator`,``],[`tabListContainer`,``],[`tabList`,``],[`tabListInner`,``],[`nextPaginator`,``],[`mat-ripple`,``,1,`mat-mdc-tab-header-pagination`,`mat-mdc-tab-header-pagination-before`,3,`click`,`mousedown`,`touchend`,`matRippleDisabled`],[1,`mat-mdc-tab-header-pagination-chevron`],[1,`mat-mdc-tab-label-container`,3,`keydown`],[`role`,`tablist`,1,`mat-mdc-tab-list`,3,`cdkObserveContent`],[1,`mat-mdc-tab-labels`],[`mat-ripple`,``,1,`mat-mdc-tab-header-pagination`,`mat-mdc-tab-header-pagination-after`,3,`mousedown`,`click`,`touchend`,`matRippleDisabled`]],template:function(h,p){h&1&&(Fe(),c(0,`div`,5,0),C(`click`,function(){return p._handlePaginatorClick(`before`)})(`mousedown`,function(_){return p._handlePaginatorPress(`before`,_)})(`touchend`,function(){return p._stopInterval()}),U(2,`div`,6),d(),c(3,`div`,7,1),C(`keydown`,function(_){return p._handleKeydown(_)}),c(5,`div`,8,2),C(`cdkObserveContent`,function(){return p._onContentChanges()}),c(7,`div`,9,3),he(9),d()()(),c(10,`div`,10,4),C(`mousedown`,function(_){return p._handlePaginatorPress(`after`,_)})(`click`,function(){return p._handlePaginatorClick(`after`)})(`touchend`,function(){return p._stopInterval()}),U(12,`div`,6),d()),h&2&&(G(`mat-mdc-tab-header-pagination-disabled`,p._disableScrollBefore),S(`matRippleDisabled`,p._disableScrollBefore||p.disableRipple),m(3),G(`_mat-animation-noopable`,p._animationsDisabled),m(2),ne(`aria-label`,p.ariaLabel||null)(`aria-labelledby`,p.ariaLabelledby||null),m(5),G(`mat-mdc-tab-header-pagination-disabled`,p._disableScrollAfter),S(`matRippleDisabled`,p._disableScrollAfter||p.disableRipple))},dependencies:[li,BC],styles:[`.mat-mdc-tab-header {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var yO=new N(`MAT_TABS_CONFIG`);var Pw=(()=>{class t extends Ra{_host=f(Pb);_ngZone=f(Z);_centeringSub=De.EMPTY;_leavingSub=De.EMPTY;ngOnInit(){super.ngOnInit(),this._centeringSub=this._host._beforeCentering.pipe(mt(this._host._isCenterPosition())).subscribe(e=>{this._host._content&&e&&!this.hasAttached()&&this._ngZone.run(()=>{Promise.resolve().then(),this.attach(this._host._content)})}),this._leavingSub=this._host._afterLeavingCenter.subscribe(()=>{this._host.preserveContent||this._ngZone.run(()=>this.detach())})}ngOnDestroy(){super.ngOnDestroy(),this._centeringSub.unsubscribe(),this._leavingSub.unsubscribe()}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`matTabBodyHost`,``]],features:[Be]})}return t})();var Pb=(()=>{class t{_elementRef=f(Y);_dir=f(gt,{optional:!0});_ngZone=f(Z);_injector=f(se);_renderer=f(Pe);_diAnimationsDisabled=Ue();_eventCleanups;_initialized=!1;_fallbackTimer;_positionIndex;_dirChangeSubscription=De.EMPTY;_position;_previousPosition;_onCentering=new te;_beforeCentering=new te;_afterLeavingCenter=new te;_onCentered=new te(!0);_portalHost;_contentElement;_content;animationDuration=`500ms`;preserveContent=!1;set position(e){this._positionIndex=e,this._computePositionAnimationState()}constructor(){if(this._dir){let e=f(Le);this._dirChangeSubscription=this._dir.change.subscribe(i=>{this._computePositionAnimationState(i),e.markForCheck()})}}ngOnInit(){this._bindTransitionEvents(),this._position===`center`&&(this._setActiveClass(!0),Rt(()=>this._onCentering.emit(this._elementRef.nativeElement.clientHeight),{injector:this._injector})),this._initialized=!0}ngOnDestroy(){clearTimeout(this._fallbackTimer),this._eventCleanups?.forEach(e=>e()),this._dirChangeSubscription.unsubscribe()}_bindTransitionEvents(){this._ngZone.runOutsideAngular(()=>{let e=this._elementRef.nativeElement,i=a=>{a.target===this._contentElement?.nativeElement&&(this._elementRef.nativeElement.classList.remove(`mat-tab-body-animating`),a.type===`transitionend`&&this._transitionDone())};this._eventCleanups=[this._renderer.listen(e,`transitionstart`,a=>{a.target===this._contentElement?.nativeElement&&(this._elementRef.nativeElement.classList.add(`mat-tab-body-animating`),this._transitionStarted())}),this._renderer.listen(e,`transitionend`,i),this._renderer.listen(e,`transitioncancel`,i)]})}_transitionStarted(){clearTimeout(this._fallbackTimer);let e=this._position===`center`;this._beforeCentering.emit(e),e&&this._onCentering.emit(this._elementRef.nativeElement.clientHeight)}_transitionDone(){this._position===`center`?this._onCentered.emit():this._previousPosition===`center`&&this._afterLeavingCenter.emit()}_setActiveClass(e){this._elementRef.nativeElement.classList.toggle(`mat-mdc-tab-body-active`,e)}_getLayoutDirection(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_isCenterPosition(){return this._positionIndex===0}_computePositionAnimationState(e=this._getLayoutDirection()){this._previousPosition=this._position,this._positionIndex<0?this._position=e==`ltr`?`left`:`right`:this._positionIndex>0?this._position=e==`ltr`?`right`:`left`:this._position=`center`,this._animationsDisabled()?this._simulateTransitionEvents():this._initialized&&(this._position===`center`||this._previousPosition===`center`)&&(clearTimeout(this._fallbackTimer),this._fallbackTimer=this._ngZone.runOutsideAngular(()=>setTimeout(()=>this._simulateTransitionEvents(),100)))}_simulateTransitionEvents(){this._transitionStarted(),Rt(()=>this._transitionDone(),{injector:this._injector})}_animationsDisabled(){return this._diAnimationsDisabled||this.animationDuration===`0ms`||this.animationDuration===`0s`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`content`];function i(a,o){}return H({type:t,selectors:[[`mat-tab-body`]],viewQuery:function(o,r){if(o&1&&Ze(Pw,5)(e,5),o&2){let s;J(s=ee())&&(r._portalHost=s.first),J(s=ee())&&(r._contentElement=s.first)}},hostAttrs:[1,`mat-mdc-tab-body`],hostVars:1,hostBindings:function(o,r){o&2&&ne(`inert`,r._position===`center`?null:``)},inputs:{_content:[0,`content`,`_content`],animationDuration:`animationDuration`,preserveContent:`preserveContent`,position:`position`},outputs:{_onCentering:`_onCentering`,_beforeCentering:`_beforeCentering`,_onCentered:`_onCentered`},decls:3,vars:6,consts:[[`content`,``],[`cdkScrollable`,``,1,`mat-mdc-tab-body-content`],[`matTabBodyHost`,``]],template:function(o,r){o&1&&(c(0,`div`,1,0),st(2,i,0,0,`ng-template`,2),d()),o&2&&G(`mat-tab-body-content-left`,r._position===`left`)(`mat-tab-body-content-right`,r._position===`right`)(`mat-tab-body-content-can-animate`,r._position===`center`||r._previousPosition===`center`)},dependencies:[Pw,ql],styles:[`.mat-mdc-tab-body {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var bm=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Le);_ngZone=f(Z);_tabsSubscription=De.EMPTY;_tabLabelSubscription=De.EMPTY;_tabBodySubscription=De.EMPTY;_diAnimationsDisabled=Ue();_bodyAnimationDuration;_headerAnimationDuration;_allTabs;_tabBodies;_tabBodyWrapper;_tabHeader;_tabs=new wn;_indexToSelect=0;_lastFocusedTabIndex=null;_tabBodyWrapperHeight=0;color;get fitInkBarToContent(){return this._fitInkBarToContent}set fitInkBarToContent(e){this._fitInkBarToContent=e,this._changeDetectorRef.markForCheck()}_fitInkBarToContent=!1;stretchTabs=!0;alignTabs=null;dynamicHeight=!1;get selectedIndex(){return this._selectedIndex}set selectedIndex(e){this._indexToSelect=isNaN(e)?null:e}_selectedIndex=null;headerPosition=`above`;get animationDuration(){return this._animationDuration}set animationDuration(e){this._animationDuration=e,e&&typeof e==`object`?(this._bodyAnimationDuration=Rb(e.body),this._headerAnimationDuration=Rb(e.header)):this._headerAnimationDuration=this._bodyAnimationDuration=Rb(e)}_animationDuration;get contentTabIndex(){return this._contentTabIndex}set contentTabIndex(e){this._contentTabIndex=isNaN(e)?null:e}_contentTabIndex=null;disablePagination=!1;disableRipple=!1;preserveContent=!1;get backgroundColor(){return this._backgroundColor}set backgroundColor(e){let i=this._elementRef.nativeElement.classList;i.remove(`mat-tabs-with-background`,`mat-background-${this.backgroundColor}`),e&&i.add(`mat-tabs-with-background`,`mat-background-${e}`),this._backgroundColor=e}_backgroundColor;ariaLabel;ariaLabelledby;selectedIndexChange=new te;focusChange=new te;animationDone=new te;selectedTabChange=new te(!0);_groupId;_isServer=!f(Re).isBrowser;constructor(){let e=f(yO,{optional:!0});this._groupId=f(je).getId(`mat-tab-group-`),this.animationDuration=e&&e.animationDuration?e.animationDuration:`500ms`,this.disablePagination=e&&e.disablePagination!=null?e.disablePagination:!1,this.dynamicHeight=e&&e.dynamicHeight!=null?e.dynamicHeight:!1,e?.contentTabIndex!=null&&(this.contentTabIndex=e.contentTabIndex),this.preserveContent=!!e?.preserveContent,this.fitInkBarToContent=e&&e.fitInkBarToContent!=null?e.fitInkBarToContent:!1,this.stretchTabs=e&&e.stretchTabs!=null?e.stretchTabs:!0,this.alignTabs=e&&e.alignTabs!=null?e.alignTabs:null}ngAfterContentChecked(){let e=this._indexToSelect=this._clampTabIndex(this._indexToSelect);if(this._selectedIndex!=e){let i=this._selectedIndex==null;if(!i){this.selectedTabChange.emit(this._createChangeEvent(e));let a=this._tabBodyWrapper.nativeElement;a.style.minHeight=a.clientHeight+`px`}Promise.resolve().then(()=>{this._tabs.forEach((a,o)=>a.isActive=o===e),i||(this.selectedIndexChange.emit(e),this._tabBodyWrapper.nativeElement.style.minHeight=``)})}this._tabs.forEach((i,a)=>{i.position=a-e,this._selectedIndex!=null&&i.position==0&&!i.origin&&(i.origin=e-this._selectedIndex)}),this._selectedIndex!==e&&(this._selectedIndex=e,this._lastFocusedTabIndex=null,this._changeDetectorRef.markForCheck())}ngAfterContentInit(){this._subscribeToAllTabChanges(),this._subscribeToTabLabels(),this._tabsSubscription=this._tabs.changes.subscribe(()=>{let e=this._clampTabIndex(this._indexToSelect);if(e===this._selectedIndex){let i=this._tabs.toArray(),a;for(let o=0;o<i.length;o++)if(i[o].isActive){this._indexToSelect=this._selectedIndex=o,this._lastFocusedTabIndex=null,a=i[o];break}!a&&i[e]&&Promise.resolve().then(()=>{i[e].isActive=!0,this.selectedTabChange.emit(this._createChangeEvent(e))})}this._changeDetectorRef.markForCheck()})}ngAfterViewInit(){this._tabBodySubscription=this._tabBodies.changes.subscribe(()=>this._bodyCentered(!0))}_subscribeToAllTabChanges(){this._allTabs.changes.pipe(mt(this._allTabs)).subscribe(e=>{this._tabs.reset(e.filter(i=>i._closestTabGroup===this||!i._closestTabGroup)),this._tabs.notifyOnChanges()})}ngOnDestroy(){this._tabs.destroy(),this._tabsSubscription.unsubscribe(),this._tabLabelSubscription.unsubscribe(),this._tabBodySubscription.unsubscribe()}realignInkBar(){this._tabHeader&&this._tabHeader._alignInkBarToSelectedTab()}updatePagination(){this._tabHeader&&this._tabHeader.updatePagination()}focusTab(e){let i=this._tabHeader;i&&(i.focusIndex=e)}_focusChanged(e){this._lastFocusedTabIndex=e,this.focusChange.emit(this._createChangeEvent(e))}_createChangeEvent(e){let i=new Fb;return i.index=e,this._tabs&&this._tabs.length&&(i.tab=this._tabs.toArray()[e]),i}_subscribeToTabLabels(){this._tabLabelSubscription&&this._tabLabelSubscription.unsubscribe(),this._tabLabelSubscription=kt(...this._tabs.map(e=>e._stateChanges)).subscribe(()=>this._changeDetectorRef.markForCheck())}_clampTabIndex(e){return Math.min(this._tabs.length-1,Math.max(e||0,0))}_getTabLabelId(e,i){return e.id||`${this._groupId}-label-${i}`}_getTabContentId(e){return`${this._groupId}-content-${e}`}_setTabBodyWrapperHeight(e){if(!this.dynamicHeight||!this._tabBodyWrapperHeight){this._tabBodyWrapperHeight=e;return}let i=this._tabBodyWrapper.nativeElement;i.style.height=this._tabBodyWrapperHeight+`px`,this._tabBodyWrapper.nativeElement.offsetHeight&&(i.style.height=e+`px`)}_removeTabBodyWrapperHeight(){let e=this._tabBodyWrapper.nativeElement;this._tabBodyWrapperHeight=e.clientHeight,e.style.height=``,this._ngZone.run(()=>this.animationDone.emit())}_handleClick(e,i,a){i.focusIndex=a,e.disabled||(this.selectedIndex=a)}_getTabIndex(e){return e===(this._lastFocusedTabIndex??this.selectedIndex)?0:-1}_tabFocusChanged(e,i){e&&e!==`mouse`&&e!==`touch`&&(this._tabHeader.focusIndex=i)}_bodyCentered(e){e&&this._tabBodies?.forEach((i,a)=>i._setActiveClass(a===this._selectedIndex))}_bodyAnimationsDisabled(){return this._diAnimationsDisabled||this._bodyAnimationDuration===`0`||this._bodyAnimationDuration===`0ms`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`tabBodyWrapper`],i=[`tabHeader`],a=[`*`];function o(g,_){}function r(g,_){if(g&1&&st(0,o,0,0,`ng-template`,12),g&2){let b=v().$implicit;S(`cdkPortalOutlet`,b.templateLabel)}}function s(g,_){if(g&1&&u(0),g&2){let b=v().$implicit;w(b.textLabel)}}function l(g,_){if(g&1){let b=$();c(0,`div`,7,2),C(`click`,function(){let I=k(b),P=I.$implicit,ie=I.$index,L=v(),x=ft(1);return M(L._handleClick(P,x,ie))})(`cdkFocusChange`,function(I){let P=k(b).$index;return M(v()._tabFocusChanged(I,P))}),U(2,`span`,8)(3,`div`,9),c(4,`span`,10)(5,`span`,11),E(6,r,1,1,null,12)(7,s,1,1),d()()()}if(g&2){let b=_.$implicit,y=_.$index,I=ft(1),P=v();tt(b.labelClass),G(`mdc-tab--active`,P.selectedIndex===y),S(`id`,P._getTabLabelId(b,y))(`disabled`,b.disabled)(`fitInkBarToContent`,P.fitInkBarToContent),ne(`tabIndex`,P._getTabIndex(y))(`aria-posinset`,y+1)(`aria-setsize`,P._tabs.length)(`aria-controls`,P._getTabContentId(y))(`aria-selected`,P.selectedIndex===y)(`aria-label`,b.ariaLabel||null)(`aria-labelledby`,!b.ariaLabel&&b.ariaLabelledby?b.ariaLabelledby:null),m(3),S(`matRippleTrigger`,I)(`matRippleDisabled`,b.disabled||P.disableRipple),m(3),D(b.templateLabel?6:7)}}function h(g,_){g&1&&he(0)}function p(g,_){if(g&1){let b=$();c(0,`mat-tab-body`,13),C(`_onCentered`,function(){k(b);return M(v()._removeTabBodyWrapperHeight())})(`_onCentering`,function(I){k(b);return M(v()._setTabBodyWrapperHeight(I))})(`_beforeCentering`,function(I){k(b);return M(v()._bodyCentered(I))}),d()}if(g&2){let b=_.$implicit,y=_.$index,I=v();tt(b.bodyClass),S(`id`,I._getTabContentId(y))(`content`,b.content)(`position`,b.position)(`animationDuration`,I._bodyAnimationDuration)(`preserveContent`,I.preserveContent),ne(`tabindex`,I.contentTabIndex!=null&&I.selectedIndex===y?I.contentTabIndex:null)(`aria-labelledby`,I._getTabLabelId(b,y))(`aria-hidden`,I.selectedIndex!==y)}}return H({type:t,selectors:[[`mat-tab-group`]],contentQueries:function(_,b,y){if(_&1&&jt(y,Zl,5),_&2){let I;J(I=ee())&&(b._allTabs=I)}},viewQuery:function(_,b){if(_&1&&Ze(e,5)(i,5)(Pb,5),_&2){let y;J(y=ee())&&(b._tabBodyWrapper=y.first),J(y=ee())&&(b._tabHeader=y.first),J(y=ee())&&(b._tabBodies=y)}},hostAttrs:[1,`mat-mdc-tab-group`],hostVars:13,hostBindings:function(_,b){_&2&&(ne(`mat-align-tabs`,b.alignTabs),tt(`mat-`+(b.color||`primary`)),Jt(`--%NS%mat-tab-body-animation-duration`,b._bodyAnimationDuration)(`--%NS%mat-tab-header-animation-duration`,b._headerAnimationDuration),G(`mat-mdc-tab-group-dynamic-height`,b.dynamicHeight)(`mat-mdc-tab-group-inverted-header`,b.headerPosition===`below`)(`mat-mdc-tab-group-stretch-tabs`,b.stretchTabs))},inputs:{color:`color`,fitInkBarToContent:[2,`fitInkBarToContent`,`fitInkBarToContent`,z],stretchTabs:[2,`mat-stretch-tabs`,`stretchTabs`,z],alignTabs:[0,`mat-align-tabs`,`alignTabs`],dynamicHeight:[2,`dynamicHeight`,`dynamicHeight`,z],selectedIndex:[2,`selectedIndex`,`selectedIndex`,dt],headerPosition:`headerPosition`,animationDuration:`animationDuration`,contentTabIndex:[2,`contentTabIndex`,`contentTabIndex`,dt],disablePagination:[2,`disablePagination`,`disablePagination`,z],disableRipple:[2,`disableRipple`,`disableRipple`,z],preserveContent:[2,`preserveContent`,`preserveContent`,z],backgroundColor:`backgroundColor`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`]},outputs:{selectedIndexChange:`selectedIndexChange`,focusChange:`focusChange`,animationDone:`animationDone`,selectedTabChange:`selectedTabChange`},exportAs:[`matTabGroup`],features:[Ge([{provide:Lw,useExisting:t}])],ngContentSelectors:a,decls:9,vars:8,consts:[[`tabHeader`,``],[`tabBodyWrapper`,``],[`tabNode`,``],[3,`indexFocused`,`selectFocusedIndex`,`selectedIndex`,`disableRipple`,`disablePagination`,`aria-label`,`aria-labelledby`],[`role`,`tab`,`matTabLabelWrapper`,``,`cdkMonitorElementFocus`,``,1,`mdc-tab`,`mat-mdc-tab`,`mat-focus-indicator`,3,`id`,`mdc-tab--active`,`class`,`disabled`,`fitInkBarToContent`],[1,`mat-mdc-tab-body-wrapper`],[`role`,`tabpanel`,3,`id`,`class`,`content`,`position`,`animationDuration`,`preserveContent`],[`role`,`tab`,`matTabLabelWrapper`,``,`cdkMonitorElementFocus`,``,1,`mdc-tab`,`mat-mdc-tab`,`mat-focus-indicator`,3,`click`,`cdkFocusChange`,`id`,`disabled`,`fitInkBarToContent`],[1,`mdc-tab__ripple`],[`mat-ripple`,``,1,`mat-mdc-tab-ripple`,3,`matRippleTrigger`,`matRippleDisabled`],[1,`mdc-tab__content`],[1,`mdc-tab__text-label`],[3,`cdkPortalOutlet`],[`role`,`tabpanel`,3,`_onCentered`,`_onCentering`,`_beforeCentering`,`id`,`content`,`position`,`animationDuration`,`preserveContent`]],template:function(_,b){_&1&&(Fe(),c(0,`mat-tab-header`,3,0),C(`indexFocused`,function(I){return b._focusChanged(I)})(`selectFocusedIndex`,function(I){return b.selectedIndex=I}),de(2,l,8,17,`div`,4,Pn),d(),E(4,h,1,0),c(5,`div`,5,1),de(7,p,1,10,`mat-tab-body`,6,Pn),d()),_&2&&(S(`selectedIndex`,b.selectedIndex||0)(`disableRipple`,b.disableRipple)(`disablePagination`,b.disablePagination),Mr(`aria-label`,b.ariaLabel)(`aria-labelledby`,b.ariaLabelledby),m(2),ue(b._tabs),m(2),D(b._isServer?4:-1),m(),G(`_mat-animation-noopable`,b._bodyAnimationsDisabled()),m(2),ue(b._tabs))},dependencies:[_O,Vw,Vg,li,Ra,Pb],styles:[`.mdc-tab {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var Fb=class{index;tab};var vm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[we]})}return t})();var xO=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-toolbar-row`]],hostAttrs:[1,`mat-toolbar-row`],exportAs:[`matToolbarRow`]})}return t})();var _m=(()=>{class t{_elementRef=f(Y);_platform=f(Re);_document=f(me);color;_toolbarRows;ngAfterViewInit(){this._platform.isBrowser&&(this._checkToolbarMixedModes(),this._toolbarRows.changes.subscribe(()=>this._checkToolbarMixedModes()))}_checkToolbarMixedModes(){this._toolbarRows.length}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[`mat-toolbar-row`]]];return H({type:t,selectors:[[`mat-toolbar`]],contentQueries:function(o,r,s){if(o&1&&jt(s,xO,5),o&2){let l;J(l=ee())&&(r._toolbarRows=l)}},hostAttrs:[1,`mat-toolbar`],hostVars:6,hostBindings:function(o,r){o&2&&(tt(r.color?`mat-`+r.color:``),G(`mat-toolbar-multiple-rows`,r._toolbarRows.length>0)(`mat-toolbar-single-row`,r._toolbarRows.length===0))},inputs:{color:`color`},exportAs:[`matToolbar`],ngContentSelectors:[`*`,`mat-toolbar-row`],decls:2,vars:0,template:function(o,r){o&1&&(Fe(e),he(0),he(1,1))},styles:[`.mat-toolbar {
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
`],encapsulation:2})})()}return t})();var ym=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[we]})}return t})();function hn(t){t||(t=f(Ae));let n=new Ce(e=>{if(t.destroyed){e.next();return}return t.onDestroy(e.next.bind(e))});return e=>e.pipe(Ne(n))}var Uw=v$1(Hw());var In={apiBaseUrl:``,websocketUrl:`${window.location.protocol===`https:`?`wss:`:`ws:`}//${window.location.host}/websocket`,websocketDebug:!1};var us=class t{http=f(Di);destroyRef=f(Ae);envelopeSubject=new V;connectedSubject=new V;disconnectedSubject=new V;errorSubject=new V;stateSubject=new hi(`connecting`);pending=new Map;client=null;subscription=null;retryTimer=null;connected=!1;synchronization=null;again=!1;generation=0;delay=500;logId=null;cursor=-1;envelopes$=this.envelopeSubject.asObservable();connected$=this.connectedSubject.asObservable();disconnected$=this.disconnectedSubject.asObservable();errors$=this.errorSubject.asObservable();state$=this.stateSubject.asObservable();get lastEventId(){return this.cursor}get eventLogId(){return this.logId}constructor(){this.destroyRef.onDestroy(()=>{this.stop()})}start(){if(this.client)return;let n=new Uw.Client({brokerURL:In.websocketUrl,heartbeatIncoming:4e3,heartbeatOutgoing:4e3,reconnectDelay:1e3,debug:In.websocketDebug?e=>console.debug(`[STOMP] `+e):()=>{}});n.onConnect=()=>{this.subscription?.unsubscribe(),this.subscription=n.subscribe(`/topic/events`,e=>this.receiveMessage(e)),this.connected=!0,this.generation++,this.connectedSubject.next(),this.retryNow()},n.onWebSocketClose=()=>{this.subscription=null,this.connected=!1,this.generation++,this.stateSubject.next(`disconnected`),this.disconnectedSubject.next(),this.clearTimer()},n.onWebSocketError=e=>this.fail(e),n.onStompError=e=>this.fail(e),this.client=n,n.activate()}async stop(){this.connected=!1,this.generation++,this.clearTimer(),this.pending.clear(),this.subscription?.unsubscribe(),this.subscription=null;let n=this.client;this.client=null,n&&await n.deactivate()}retryNow(){this.clearTimer(),this.delay=500,this.requestSync()}getHead(){return wt(this.http.get(In.apiBaseUrl+`/lastevent`).pipe(hn(this.destroyRef)))}getEvents(n,e,i){return wt(this.http.get(In.apiBaseUrl+`/events`,{params:{eventLogId:n,from:e,to:i}}).pipe(hn(this.destroyRef)))}receiveMessage(n){try{let e=JSON.parse(n.body);if(this.validate(e),e.eventLogId===this.logId&&e.eventId<=this.cursor)return;this.pending.size>=5e3&&(this.pending.clear(),this.requestSync()),this.pending.set(this.key(e.eventLogId,e.eventId),e),this.stateSubject.value===`live`&&e.eventLogId===this.logId&&this.drain(),(e.eventLogId!==this.logId||this.hasGap())&&this.requestSync()}catch(e){this.fail(e)}}requestSync(){if(!this.connected)return;if(this.synchronization){this.again=!0;return}this.again=!1;let n=this.generation;this.synchronization=this.synchronize(n).catch(e=>{n===this.generation&&this.connected&&this.fail(e)}).finally(()=>{this.synchronization=null,this.again&&this.connected&&this.requestSync()})}async synchronize(n){this.stateSubject.next(`syncing`);let e=await this.getHead();if(this.current(n)){if(typeof e.eventLogId!=`string`||!Number.isInteger(e.lastEventId)||e.lastEventId<-1)throw new Error(`Invalid event head`);if(this.logId!==e.eventLogId){this.logId=e.eventLogId,this.cursor=e.lastEventId;for(let[i,a]of this.pending)(a.eventLogId!==this.logId||a.eventId<=this.cursor)&&this.pending.delete(i)}else{if(e.lastEventId<this.cursor)throw new Error(`Cursor moved backwards without a new log identity`);await this.download(this.cursor+1,e.lastEventId+1,n)}if(this.current(n)){this.drain();for(let i=0;this.hasGap();i++){if(i===10)throw new Error(`Live delivery has not caught up`);let a=this.smallestPending();if(a===null)break;if(await this.download(this.cursor+1,a,n),!this.current(n))return;this.drain()}if(this.cursor<e.lastEventId)throw new Error(`Incomplete event history`);this.current(n)&&(this.delay=500,this.clearTimer(),this.stateSubject.next(`live`))}}}async download(n,e,i){for(let a=n;a<e;){let o=Math.min(e,a+1e3),r=this.logId,s=await this.getEvents(r,a,o);if(!this.current(i))return;if(s.eventLogId!==r||s.from!==a||s.to!==o||s.events.length!==o-a)throw new Error(`Incomplete or obsolete history`);let l=a;for(let h of s.events){if(this.validate(h),h.eventLogId!==r||h.eventId!==l++)throw new Error(`Non-contiguous history`);h.eventId>this.cursor&&this.pending.set(this.key(r,h.eventId),h)}this.drain(),a=o}}drain(){if(this.logId)for(let n=this.pending.get(this.key(this.logId,this.cursor+1));n;n=this.pending.get(this.key(this.logId,this.cursor+1)))this.pending.delete(this.key(this.logId,n.eventId)),this.cursor=n.eventId,this.envelopeSubject.next(n)}smallestPending(){let n=null;for(let e of this.pending.values())e.eventLogId===this.logId&&e.eventId>this.cursor&&(n===null||e.eventId<n)&&(n=e.eventId);return n}hasGap(){let n=this.smallestPending();return n!==null&&n>this.cursor+1}current(n){return this.connected&&n===this.generation}key(n,e){return n+`:`+e}clearTimer(){this.retryTimer&&clearTimeout(this.retryTimer),this.retryTimer=null}fail(n){this.errorSubject.next(n),this.stateSubject.next(`retrying`),this.connected&&!this.retryTimer&&(this.retryTimer=setTimeout(()=>{this.retryTimer=null,this.requestSync()},this.delay),this.delay=Math.min(3e4,this.delay*2))}validate(n){if(!n||typeof n.eventLogId!=`string`||!Number.isInteger(n.eventId)||n.eventId<0||typeof n.type!=`string`||!Number.isFinite(n.timestamp)||typeof n.workerName!=`string`||!n.payload||typeof n.payload!=`object`)throw new Error(`Invalid envelope`);let e=n.payload;if(n.type===`SolutionGeneratedEvent`&&(typeof e.resultId!=`string`||typeof e.success!=`boolean`||typeof e.iteration!=`string`||!e.objectives||typeof e.objectives!=`object`))throw new Error(`Invalid result event`);if(n.type===`ExecutionStartedEvent`&&(!e.objectives||typeof e.objectives!=`object`||!Array.isArray(e.experimentNames)))throw new Error(`Invalid execution event`);if(n.type===`ErrorEvent`&&typeof e.exceptionType!=`string`)throw new Error(`Invalid error event`)}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var La=class t{http=f(Di);destroyRef=f(Ae);get(n,e={}){return wt(this.http.get(`${In.apiBaseUrl}${n}`,{params:e}).pipe(hn(this.destroyRef)))}getMode(){return this.get(`/api/monitor/status`)}getStatus(){return this.get(`/api/experiments/status`)}getInstances(n,e){return this.get(`/api/experiments/instances`,r({runId:n},e))}getInstance(n,e,i){return this.get(`/api/experiments/instances/${encodeURIComponent(e)}`,{runId:n,objective:i})}getResults(n,e){let i=s(r({runId:n},e),{order:e.order??`PLANNED`});return e.state||delete i.state,this.get(`/api/experiments/results`,i)}getResult(n,e){return this.get(`/api/experiments/results/${encodeURIComponent(e)}`,{runId:n})}getIssues(n,e,i){return this.get(`/api/experiments/issues`,{runId:n,severity:e,offset:i,limit:25})}getArtifacts(n){return this.get(`/api/experiments/artifacts`,{runId:n})}artifactUrl(n,e){return`${In.apiBaseUrl}/api/experiments/artifacts/${encodeURIComponent(e)}?${new URLSearchParams({runId:n})}`}exportUrl(n,e,i){let a=new URLSearchParams({runId:n,format:i,instanceId:e.instanceId,algorithm:e.algorithm,search:e.search,order:e.order??`PLANNED`});return e.state&&a.set(`state`,e.state),`${In.apiBaseUrl}/api/experiments/results/export?${a}`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Va=class{static buildInstance(n,e,i,a=!0){let o=this.referenceValue(n.started.refValues,e);return{key:n.key,experimentName:n.started.experimentName,instanceName:n.started.instanceName,selectedObjective:e,referenceValue:o,scoreChart:this.buildScoreChart(n,e,o,a),convergenceChart:this.buildConvergenceChart(n,e,i,o,a),bestSolution:this.findBest(n.solutions,e,i)}}static buildScoreChart(n,e,i,a){let o=this.groupSuccessfulPoints(n,e,!1,null);return{options:this.chartOptions(`${n.started.instanceName} scores`,e,i,o,`Raw score for every successful repetition.`,a)}}static buildConvergenceChart(n,e,i,a,o){let r=this.groupSuccessfulPoints(n,e,!0,i);return{options:this.chartOptions(`${n.started.instanceName} best score across repetitions`,e,a,r,`Best score reached by each algorithm over completed repetitions.`,o)}}static groupSuccessfulPoints(n,e,i,a){if(!e)return[];let o=[...n.started.algorithms];for(let s of n.solutions)o.includes(s.event.algorithmName)||o.push(s.event.algorithmName);let r=[];for(let s of o){let l=[],h=null;for(let p of n.solutions){let g=p.event;if(g.algorithmName!==s)continue;let _=g.success?this.objectiveValue(g,e):null;if(_===null){l.push({x:p.ordinal,y:null,custom:{iteration:g.iteration}});continue}i&&(h=h===null||this.isBetter(_,h,a)?_:h),l.push({x:p.ordinal,y:i?h:_,custom:{iteration:g.iteration}})}r.push({type:`line`,name:s,data:l,lineWidth:i?2:1,marker:{enabled:!i},animation:!1,turboThreshold:0})}return r}static referenceValue(n,e){let i=e?n[e]:void 0;return typeof i==`number`&&Number.isFinite(i)?i:null}static scoreAxis(n,e,i){let a=i?e:null,o=[];return a!==null&&o.push({id:`reference`,className:`reference-line`,value:a,color:`#d32f2f`,dashStyle:`Dash`,width:2,zIndex:4,label:{text:`Reference: ${a}`}}),{title:{text:n??`Score`},plotLines:o,softMin:a??void 0,softMax:a??void 0}}static chartOptions(n,e,i,a,o,r){return{accessibility:{enabled:!0,description:o},boost:{enabled:!0,useGPUTranslations:!0,seriesThreshold:25},chart:{animation:!1,height:320,spacing:[12,12,12,12]},credits:{enabled:!1},exporting:{enabled:!0},legend:{enabled:!0},title:{text:n,style:{display:`none`}},subtitle:{text:e?`Objective: ${e}`:`Waiting for objective metadata`},xAxis:{allowDecimals:!1,min:1,title:{text:`Repetition`}},yAxis:this.scoreAxis(e,i,r),tooltip:{headerFormat:``,pointFormat:`<b>{series.name}</b><br/>Repetition: {point.x}<br/>Iteration: {point.custom.iteration}<br/>Value: {point.y}`},plotOptions:{series:{animation:!1,connectNulls:!1}},series:a}}static findBest(n,e,i){if(!e||!i)return null;let a=null,o=null;for(let r of n){if(!r.event.success)continue;let s=this.objectiveValue(r.event,e);s!==null&&(o===null||this.isBetter(s,o,i))&&(o=s,a=r.event)}return a}static objectiveValue(n,e){let i=n.objectives[e];return typeof i==`number`&&Number.isFinite(i)?i:null}static isBetter(n,e,i){return i===`MAXIMIZE`?n>e:n<e}};var ms=class{static instance(n,e,i=!0){return Va.buildInstance({key:n.instance.id,started:{experimentName:n.instance.experimentName,instanceName:n.instance.instanceName,algorithms:n.algorithms.map(a=>a.name),repetitions:n.instance.counts.planned,refValues:n.instance.references},solutions:n.results.map(a=>({ordinal:a.ordinal,event:{resultId:a.resultId,success:a.state===`SUCCEEDED`,experimentName:a.experimentName,instanceName:a.instanceName,algorithmName:a.algorithmName,iteration:a.iteration,objectives:a.objectives,score:a.objectives[n.objective]??NaN,executionTime:a.executionTime??-1,timeToBest:a.timeToBest??-1}}))},n.objective,e,i)}static distribution(n,e=!0){return{options:{chart:{type:`boxplot`,height:320,animation:!1},plotOptions:{series:{animation:!1}},title:{text:`Score distribution`},credits:{enabled:!1},exporting:{enabled:!0},accessibility:{enabled:!0},xAxis:{categories:n.algorithms.map(i=>i.name)},yAxis:Va.scoreAxis(n.objective,Va.referenceValue(n.instance.references,n.objective),e),series:[{type:`boxplot`,name:n.objective,data:n.algorithms.flatMap((i,a)=>{let o=i.objective;return o.best===null||o.worst===null?[]:[{x:a,low:Math.min(o.best,o.worst),q1:o.lowerQuartile,median:o.median,q3:o.upperQuartile,high:Math.max(o.best,o.worst)}]})}]}}}static qualityRuntime(n,e=!0){return{options:{chart:{type:`scatter`,height:320,animation:!1},plotOptions:{series:{animation:!1}},title:{text:`Score versus runtime`},credits:{enabled:!1},exporting:{enabled:!0},accessibility:{enabled:!0},xAxis:{title:{text:`Runtime (ms)`}},yAxis:Va.scoreAxis(n.objective,Va.referenceValue(n.instance.references,n.objective),e),tooltip:{pointFormat:`Runtime: {point.x} ms<br/>Score: {point.y}<br/>Iteration: {point.custom.iteration}`},series:n.algorithms.map(i=>({type:`scatter`,name:i.name,data:n.results.filter(a=>a.algorithmName===i.name&&a.state===`SUCCEEDED`&&a.executionTime!==null&&Number.isFinite(a.objectives[n.objective])).map(a=>({x:a.executionTime/1e6,y:a.objectives[n.objective],custom:{iteration:a.iteration}}))}))}}}};var Jl=class t{client=f(La);events=f(us);destroyRef=f(Ae);statusState=T(null);status=this.statusState.asReadonly();syncStateState=T(`connecting`);syncState=this.syncStateState.asReadonly();transportStateState=T(`connecting`);transportState=this.transportStateState.asReadonly();latestErrorState=T(null);latestError=this.latestErrorState.asReadonly();transportErrorState=T(null);transportError=this.transportErrorState.asReadonly();lastUpdatedState=T(null);lastUpdated=this.lastUpdatedState.asReadonly();selectedObjectiveState=T(null);selectedObjective=this.selectedObjectiveState.asReadonly();showReferenceState=T(!0);showReference=this.showReferenceState.asReadonly();selectedInstanceIdState=T(null);selectedInstanceId=this.selectedInstanceIdState.asReadonly();instancePageState=T(null);instancePage=this.instancePageState.asReadonly();resultPageState=T(null);resultPage=this.resultPageState.asReadonly();issuePageState=T(null);issuePage=this.issuePageState.asReadonly();artifactsState=T(null);artifacts=this.artifactsState.asReadonly();detailState=T(null);detail=this.detailState.asReadonly();inspectedResultState=T(null);inspectedResult=this.inspectedResultState.asReadonly();resultErrorState=T(null);resultError=this.resultErrorState.asReadonly();tabState=T(0);tab=this.tabState.asReadonly();instanceQueryState=T({experiment:``,search:``,offset:0,limit:25});instanceQuery=this.instanceQueryState.asReadonly();resultQueryState=T({instanceId:``,algorithm:``,state:``,search:``,offset:0,limit:25});resultQuery=this.resultQueryState.asReadonly();issueSeverityState=T(``);issueSeverity=this.issueSeverityState.asReadonly();issueOffsetState=T(0);issueOffset=this.issueOffsetState.asReadonly();seenIssueIdState=T(0);seenIssueId=this.seenIssueIdState.asReadonly();objectives=K(()=>Object.entries(this.status()?.objectives??{}).map(([n,e])=>({name:n,mode:e})));terminal=K(()=>[`SUCCEEDED`,`COMPLETED_WITH_ERRORS`,`FAILED`,`CANCELLED`].includes(this.status()?.phase??``));connectionLabel=K(()=>this.syncState()===`retrying`?`Retrying · data may be stale`:this.syncState()===`connecting`?`Connecting`:this.transportState()===`live`?`Live`:`Live via HTTP`);unseenIssues=K(()=>Math.max(0,(this.status()?.issueCount??0)-this.seenIssueId()));estimatedRemainingMillis=K(()=>{let n=this.status();if(!n||n.phase!==`RUNNING`||n.elapsedMillis<1e4)return null;let e=n.counts.succeeded+n.counts.failed;return e<5||n.counts.cancelled>0?null:n.elapsedMillis/e*(n.counts.pending+n.counts.running)});charts=K(()=>{let n=this.detail(),e=this.selectedObjective(),i=this.selectedObjectiveMode();return n&&e&&i&&n.objective===e?ms.instance(n,i,this.showReference()):null});selectedObjectiveMode=K(()=>this.status()?.objectives[this.selectedObjective()??``]??null);distribution=K(()=>this.detail()?ms.distribution(this.detail(),this.showReference()):null);qualityRuntime=K(()=>this.detail()?ms.qualityRuntime(this.detail(),this.showReference()):null);lastEventId=K(()=>(this.transportState(),this.lastUpdated(),this.events.lastEventId));progress=K(()=>{let n=this.status(),e=this.detail(),i=e?.algorithms.find(o=>o.name===this.resultQuery().algorithm),a=i?.counts??e?.instance.counts??n?.counts;return{instances:{completed:n?.completedInstances??0,total:n?.instances??0},algorithms:{completed:e?.instance.completedAlgorithms??0,total:e?.instance.algorithms??0},repetitions:{completed:(a?.succeeded??0)+(a?.failed??0),total:a?.planned??0},repetitionScope:i?`selected algorithm`:e?`selected instance`:`execution`}});cache=new Map;timer=null;polling=!1;pending=!1;started=!1;destroyed=!1;generation=0;delay=500;forceDetail=!0;lastDetailFetch=0;resultRequest=0;constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.generation++,this.resultRequest++,this.clearTimer()})}start(){this.started||(this.started=!0,this.events.state$.pipe(hn(this.destroyRef)).subscribe(n=>{this.transportStateState.set(n),n===`live`&&this.transportErrorState.set(null)}),this.events.errors$.pipe(hn(this.destroyRef)).subscribe(n=>this.transportErrorState.set(this.message(n))),this.events.connected$.pipe(hn(this.destroyRef)).subscribe(()=>{this.generation++,this.forceDetail=!0,this.refreshNow()}),this.events.envelopes$.pipe(hn(this.destroyRef)).subscribe(n=>{n.type!==`PingEvent`&&(n.type===`ExecutionEndedEvent`||n.type===`InstanceProcessingEndedEvent`?(this.forceDetail=!0,this.refreshNow()):this.polling||this.schedule(Math.max(100,500-(Date.now()-(this.lastUpdated()?.getTime()??0)))))}),this.events.start(),this.refreshNow())}retryNow(){this.delay=500,this.events.retryNow(),this.refreshNow()}setReferenceVisible(n){this.showReferenceState.set(n)}selectObjective(n){!this.status()?.objectives[n]||this.selectedObjective()===n||(this.selectedObjectiveState.set(n),this.generation++,this.forceDetail=!0,this.detailState.set(null),this.refreshNow())}selectInstance(n){this.selectedInstanceIdState.set(n),this.generation++,this.forceDetail=!0;let e=this.cache.get(n);this.detailState.set(e?.objective===this.selectedObjective()?e:null),this.refreshNow()}selectTab(n){this.tabState.set(n),this.refreshNow()}filterInstances(n,e){this.instanceQueryState.set(s(r({},this.instanceQuery()),{experiment:n,search:e,offset:0})),this.invalidate()}pageInstances(n){this.instanceQueryState.update(e=>s(r({},e),{offset:n})),this.invalidate()}filterResults(n,e,i,a){this.resultQueryState.set(s(r({},this.resultQuery()),{instanceId:n,algorithm:e,state:i,search:a,offset:0})),this.invalidate()}pageResults(n){this.resultQueryState.update(e=>s(r({},e),{offset:n})),this.invalidate()}sortResults(n){this.resultQueryState.update(e=>s(r({},e),{order:n,offset:0})),this.invalidate()}filterIssues(n){this.issueSeverityState.set(n),this.issueOffsetState.set(0),this.invalidate()}pageIssues(n){this.issueOffsetState.set(n),this.invalidate()}markIssuesSeen(){this.seenIssueIdState.set(this.status()?.issueCount??0);try{localStorage.setItem(`mork.normal.issues.v1`,JSON.stringify({runId:this.status()?.runId,id:this.seenIssueId()}))}catch{}}async inspectResult(n){let e=this.status()?.runId;if(!e)return;let i=++this.resultRequest;this.inspectedResultState.set(null),this.resultErrorState.set(null);try{let[a,o]=await Promise.all([this.client.getResult(e,n),this.client.getArtifacts(e)]);!this.destroyed&&i===this.resultRequest&&e===this.status()?.runId&&a.runId===e&&(this.inspectedResultState.set(a),o.runId===e&&this.artifactsState.set(o))}catch(a){!this.destroyed&&i===this.resultRequest&&e===this.status()?.runId&&this.resultErrorState.set(this.message(a))}}closeResult(){this.resultRequest++,this.inspectedResultState.set(null),this.resultErrorState.set(null)}artifactUrl(n){return this.client.artifactUrl(this.status().runId,n)}exportUrl(n){return this.client.exportUrl(this.status().runId,this.resultQuery(),n)}refreshNow(){this.clearTimer(),this.polling?this.pending=!0:this.poll()}invalidate(){this.generation++,this.refreshNow()}async poll(){if(this.destroyed)return;this.polling=!0,this.pending=!1;let n=this.generation,e=2e3;try{let i=await this.client.getStatus();if(!this.current(n))return;let a=this.status();if(i.runId!==a?.runId){this.cache.clear(),this.detailState.set(null),this.inspectedResultState.set(null),this.resultRequest++,this.instancePageState.set(null),this.resultPageState.set(null),this.issuePageState.set(null),this.artifactsState.set(null),this.selectedInstanceIdState.set(null),this.selectedObjectiveState.set(Object.keys(i.objectives)[0]??null),this.instanceQueryState.set({experiment:``,search:``,offset:0,limit:25}),this.resultQueryState.set({instanceId:``,algorithm:``,state:``,search:``,offset:0,limit:25}),this.seenIssueIdState.set(0),this.forceDetail=!0;try{let o=JSON.parse(localStorage.getItem(`mork.normal.issues.v1`)??`null`);o?.runId===i.runId&&this.seenIssueIdState.set(o.id)}catch{}}if(a?.runId===i.runId&&i.revision<a.revision)throw new Error(`Monitoring revision moved backwards`);if(this.statusState.set(i),i.runId){let o=i.runId,r=await this.client.getInstances(o,this.instanceQuery());if(!this.current(n)||r.runId!==o)return;this.instancePageState.set(r),!this.selectedInstanceId()&&r.items.length&&this.selectedInstanceIdState.set(r.items.find(b=>b.counts.running>0)?.id??r.items[0].id);let s=this.selectedInstanceId(),l=this.selectedObjective(),h=s?this.cache.get(s):null,p=r.items.find(b=>b.id===s),g=!h||h.objective!==l||!p||h.revision!==p.revision,_=[`SUCCEEDED`,`COMPLETED_WITH_ERRORS`,`FAILED`,`CANCELLED`].includes(i.phase)&&i.revision!==a?.revision;if(s&&l&&g&&(this.forceDetail||_||Date.now()-this.lastDetailFetch>=2e3)){let b=await this.client.getInstance(o,s,l);if(!this.current(n)||b.runId!==o||b.objective!==this.selectedObjective())return;for(this.cache.delete(s),this.cache.set(s,b);this.cache.size>10;)this.cache.delete(this.cache.keys().next().value);this.detailState.set(b),this.lastDetailFetch=Date.now(),this.forceDetail=!1}else h&&h.objective===l&&this.detailState.set(h);if(this.tab()===1){let b=await this.client.getResults(o,this.resultQuery());if(!this.current(n)||b.runId!==o)return;this.resultPageState.set(b)}if(this.tab()===2||i.issueCount!==a?.issueCount||!this.issuePage()){let b=await this.client.getIssues(o,this.issueSeverity(),this.issueOffset());if(!this.current(n)||b.runId!==o)return;this.issuePageState.set(b)}if(this.tab()===3){let b=await this.client.getArtifacts(o);if(!this.current(n)||b.runId!==o)return;this.artifactsState.set(b)}}this.latestErrorState.set(null),this.lastUpdatedState.set(new Date),this.syncStateState.set(`live`),this.delay=500,e=this.terminal()||i.phase===`IDLE`?1e4:2e3}catch(i){if(!this.current(n))return;this.latestErrorState.set(this.message(i)),this.syncStateState.set(`retrying`),e=this.delay,this.delay=Math.min(3e4,this.delay*2)}finally{this.polling=!1,this.destroyed||this.schedule(this.pending||n!==this.generation?0:e)}}current(n){return!this.destroyed&&n===this.generation}message(n){return n instanceof Error?n.message:String(n)}schedule(n){this.destroyed||(this.clearTimer(),this.timer=setTimeout(()=>{this.poll()},n))}clearTimer(){this.timer&&clearTimeout(this.timer),this.timer=null}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Lb=[{id:`evaluation-health`,label:`Evaluation health`},{id:`evaluation-cost`,label:`Evaluation cost`},{id:`elite-evolution`,label:`Elite rank evolution`},{id:`current-elites`,label:`Current elites`},{id:`run-information`,label:`Run information`}];var Vb=[{id:`normal-comparison`,label:`Algorithm comparison`},{id:`normal-scores`,label:`Scores`},{id:`normal-best`,label:`Best score across repetitions`},{id:`normal-distribution`,label:`Score distribution`},{id:`normal-runtime`,label:`Score versus runtime`}];var Cm=`mork.autoconfig.panelPreferences.v1`;var Ba=class t{hiddenSignal=T(new Set);collapsedSignal=T(new Set);hidden=this.hiddenSignal.asReadonly();collapsed=this.collapsedSignal.asReadonly();constructor(){this.restore()}isHidden(n){return this.hiddenSignal().has(n)}isCollapsed(n){return this.collapsedSignal().has(n)}show(n){this.hiddenSignal.update(e=>this.without(e,n)),this.persist()}hide(n){this.hiddenSignal.update(e=>this.with(e,n)),this.persist()}toggleCollapsed(n){this.collapsedSignal.update(e=>e.has(n)?this.without(e,n):this.with(e,n)),this.persist()}reset(){this.hiddenSignal.set(new Set),this.collapsedSignal.set(new Set),localStorage.removeItem(Cm)}restore(){try{let n=localStorage.getItem(Cm);if(!n)return;let e=JSON.parse(n),i=new Set([...Lb,...Vb].map(a=>a.id));this.hiddenSignal.set(new Set((e.hidden??[]).filter(a=>i.has(a)))),this.collapsedSignal.set(new Set((e.collapsed??[]).filter(a=>i.has(a))))}catch{localStorage.removeItem(Cm)}}with(n,e){let i=new Set(n);return i.add(e),i}without(n,e){let i=new Set(n);return i.delete(e),i}persist(){let n={hidden:[...this.hiddenSignal()],collapsed:[...this.collapsedSignal()]};localStorage.setItem(Cm,JSON.stringify(n))}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Ai=class t{model=zt.required();resizable=zt(!1);chart;viewKey;constructor(){vt(()=>{let n=this.model().viewKey;this.chart&&n!==this.viewKey&&(this.viewKey=n,this.chart.zoomOut())})}chartCreated(n){this.chart=n,this.viewKey=this.model().viewKey}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-dashboard-chart`]],hostVars:2,hostBindings:function(e,i){e&2&&G(`resizable`,i.resizable())},inputs:{model:[1,`model`],resizable:[1,`resizable`]},decls:1,vars:2,consts:[[1,`chart`,`highcharts-light`,3,`chartInstance`,`options`,`oneToOne`]],template:function(e,i){e&1&&(c(0,`highcharts-chart`,0),C(`chartInstance`,function(o){return i.chartCreated(o)}),d()),e&2&&S(`options`,i.model().options)(`oneToOne`,!0)},dependencies:[SC],styles:[`[_nghost-%COMP%], .chart[_ngcontent-%COMP%]{display:block;width:100%;min-width:0}.resizable[_nghost-%COMP%]{height:32.5rem;min-height:24rem;max-height:75rem;resize:vertical;overflow:hidden;padding-bottom:.5rem;box-sizing:border-box}.resizable[_nghost-%COMP%]   .chart[_ngcontent-%COMP%]{height:100%}`]})};var SO=[`*`];function wO(t,n){t&1&&(c(0,`mat-card-content`),he(1),d())}function EO(t,n){if(t&1){let e=$();c(0,`mat-card`,1)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d(),U(4,`span`,2),c(5,`button`,3),C(`click`,function(){k(e);let a=v();return M(a.preferences.toggleCollapsed(a.panelId()))}),u(6),d(),c(7,`button`,4),u(8,` ⋮ `),d(),c(9,`mat-menu`,null,0)(11,`button`,5),C(`click`,function(){k(e);let a=v();return M(a.preferences.toggleCollapsed(a.panelId()))}),u(12),d(),c(13,`button`,5),C(`click`,function(){k(e);let a=v();return M(a.preferences.hide(a.panelId()))}),u(14,` Hide panel `),d()()(),E(15,wO,2,0,`mat-card-content`),d()}if(t&2){let e=ft(10),i=v();m(3),w(i.title()),m(2),ne(`aria-label`,i.preferences.isCollapsed(i.panelId())?`Expand `+i.title():`Collapse `+i.title()),m(),B(` `,i.preferences.isCollapsed(i.panelId())?`+`:`−`,` `),m(),S(`matMenuTriggerFor`,e),ne(`aria-label`,i.title()+` options`),m(5),B(` `,i.preferences.isCollapsed(i.panelId())?`Expand panel`:`Collapse panel`,` `),m(3),D(i.preferences.isCollapsed(i.panelId())?-1:15)}}var ps=class t{preferences=f(Ba);panelId=zt.required();title=zt.required();static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-monitor-panel`]],inputs:{panelId:[1,`panelId`],title:[1,`title`]},ngContentSelectors:SO,decls:1,vars:1,consts:[[`panelMenu`,`matMenu`],[`appearance`,`outlined`],[1,`spacer`],[`mat-button`,``,`type`,`button`,1,`collapse`,3,`click`],[`mat-button`,``,`type`,`button`,1,`menu`,3,`matMenuTriggerFor`],[`mat-menu-item`,``,`type`,`button`,3,`click`]],template:function(e,i){e&1&&(Fe(),E(0,EO,16,7,`mat-card`,1)),e&2&&D(i.preferences.isHidden(i.panelId())?-1:0)},dependencies:[Ft,nn,zn,Bn,jn,Ta,Ma,ls,Ni,la,ss],styles:[`[_nghost-%COMP%]{display:block;min-width:0}mat-card[_ngcontent-%COMP%]{height:100%}mat-card-header[_ngcontent-%COMP%]{min-height:3.5rem;align-items:center;padding-bottom:.5rem}mat-card-title[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-medium);font-weight:650}mat-card-content[_ngcontent-%COMP%]{min-width:0}.spacer[_ngcontent-%COMP%]{flex:1}.collapse[_ngcontent-%COMP%], .menu[_ngcontent-%COMP%]{min-width:2.25rem;padding:0;font-size:1.25rem}`]})};var DO=(t,n)=>n.id;function IO(t,n){if(t&1&&(c(0,`p`,0),u(1),d()),t&2){let e=n.$implicit;m(),Si(` `,e.severity,` · `,e.scope,`: `,e.exceptionType,` — `,e.message,` `)}}function kO(t,n){t&1&&Ea(0)}function MO(t,n){if(t&1&&st(0,kO,1,0,`ng-container`,2),t&2){let e=v();S(`ngComponentOutlet`,n)(`ngComponentOutletInputs`,e.rendererInputs())}}function TO(t,n){if(t&1&&(c(0,`details`,1)(1,`summary`),u(2,`Solution view data`),d(),c(3,`pre`),u(4),j(5,`json`),d()()),t&2){let e=v();m(4),w(_e(5,1,e.detail().rendering.data))}}function NO(t,n){if(t&1&&(c(0,`p`,0),u(1),d()),t&2){let e=v();m(),B(` `,e.detail().rendering.state===`UNSUPPORTED`?`A problem-specific solution view is not provided for this project.`:`This solution view is no longer retained.`,` Metadata remains available; exported solution files are listed below or in Artifacts. `)}}var AO=new N(`Mork solution renderers`,{providedIn:`root`,factory:()=>({})});var Sm=class t{detail=zt.required();selectedObjective=zt.required();registered=f(AO);renderer=K(()=>this.detail().rendering.state===`AVAILABLE`?this.registered[this.detail().rendering.rendererId??``]??null:null);rendererInputs=K(()=>({result:this.detail().result,selectedObjective:this.selectedObjective(),view:this.detail().rendering.data}));static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-solution-renderer`]],inputs:{detail:[1,`detail`],selectedObjective:[1,`selectedObjective`]},decls:58,vars:27,consts:[[1,`note`],[`open`,``],[4,`ngComponentOutlet`,`ngComponentOutletInputs`]],template:function(e,i){if(e&1&&(c(0,`dl`)(1,`div`)(2,`dt`),u(3,`Instance / experiment`),d(),c(4,`dd`),u(5),d()(),c(6,`div`)(7,`dt`),u(8,`Algorithm`),d(),c(9,`dd`),u(10),d()(),c(11,`div`)(12,`dt`),u(13,`Iteration / seed`),d(),c(14,`dd`),u(15),d()(),c(16,`div`)(17,`dt`),u(18,`Outcome`),d(),c(19,`dd`),u(20),d()(),c(21,`div`)(22,`dt`),u(23),d(),c(24,`dd`),u(25),j(26,`number`),d()(),c(27,`div`)(28,`dt`),u(29,`Runtime`),d(),c(30,`dd`),u(31),j(32,`number`),d()(),c(33,`div`)(34,`dt`),u(35,`Time to best`),d(),c(36,`dd`),u(37),j(38,`number`),d()(),c(39,`div`)(40,`dt`),u(41,`Result ID`),d(),c(42,`dd`),u(43),d()()(),c(44,`details`)(45,`summary`),u(46,`All objective values and custom properties`),d(),c(47,`pre`),u(48),j(49,`json`),d(),c(50,`pre`),u(51),j(52,`json`),d()(),de(53,IO,2,4,`p`,0,DO),E(55,MO,1,2,`ng-container`)(56,TO,6,3,`details`,1)(57,NO,2,1,`p`,0)),e&2){let a,o=i.detail().result;m(5),Me(``,o.instanceName,` / `,o.experimentName),m(5),w(o.algorithmName),m(5),Me(``,o.iteration,` / `,o.seed),m(5),w(o.state),m(3),w(i.selectedObjective()??`Objective`),m(2),B(` `,ke(26,14,o.objectives[i.selectedObjective()??``],`1.0-4`)??`Unavailable`,` `),m(6),B(` `,o.executionTime===null?`Unavailable`:ke(32,17,o.executionTime/1e6,`1.0-2`)+` ms`,` `),m(6),B(` `,o.timeToBest===null?`Unavailable`:ke(38,20,o.timeToBest/1e6,`1.0-2`)+` ms`,` `),m(6),w(o.resultId),m(5),w(_e(49,23,o.objectives)),m(3),w(_e(52,25,o.properties)),m(2),ue(i.detail().issues),m(2),D((a=i.renderer())?55:i.detail().rendering.state===`AVAILABLE`?56:57,a)}},dependencies:[bg,Do,En],styles:[`dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.75rem}dl[_ngcontent-%COMP%] > div[_ngcontent-%COMP%]{display:grid;gap:.25rem}dt[_ngcontent-%COMP%], .note[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}pre[_ngcontent-%COMP%]{overflow:auto;max-height:24rem;white-space:pre-wrap}@media(max-width:42rem){dl[_ngcontent-%COMP%]{grid-template-columns:1fr}}`]})};var hs=()=>[];var ec=(t,n)=>n.id;var Bb=(t,n)=>n.name;var RO=(t,n)=>n.resultId;function OO(t,n){if(t&1){let e=$();c(0,`button`,8),C(`click`,function(){let a=k(e).$implicit,o=v();return M(o.preferences.isHidden(a.id)?o.preferences.show(a.id):o.preferences.hide(a.id))}),u(1),d()}if(t&2){let e=n.$implicit,i=v();m(),Me(` `,i.preferences.isHidden(e.id)?`Show`:`Hide`,` `,e.label,` `)}}function PO(t,n){if(t&1){let e=$();c(0,`mat-card`,9)(1,`mat-card-content`)(2,`strong`),u(3,`Connection problem · showing last known data`),d(),c(4,`p`),u(5),d(),c(6,`button`,34),C(`click`,function(){k(e);return M(v().store.retryNow())}),u(7,`Retry now`),d()()()}t&2&&(m(5),w(n))}function FO(t,n){if(t&1&&(c(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit;S(`value`,e.name),m(),Me(``,e.name,` · `,e.mode===`MINIMIZE`?`Minimize`:`Maximize`)}}function LO(t,n){t&1&&(u(0),j(1,`date`)),t&2&&B(` Last refreshed `,ke(1,1,n,`mediumTime`),`. `)}function VO(t,n){if(t&1&&(c(0,`p`,24),u(1),d()),t&2){let e=v();m(),B(` Approximate time remaining: `,e.elapsedLabel(n),` · based on observed throughput; different instance sizes can change this estimate. `)}}function BO(t,n){if(t&1){let e=$();c(0,`button`,35),C(`click`,function(){k(e);return M(v().store.selectTab(2))}),u(1),d()}if(t&2){let e=v();m(),B(` `,e.store.unseenIssues(),` unseen issues `)}}function jO(t,n){if(t&1&&(c(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit;S(`value`,e.name),m(),w(e.name)}}function zO(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`)(2,`button`,35),C(`click`,function(){let a=k(e).$implicit;return M(v(2).store.selectInstance(a.id))}),u(3),d()(),c(4,`td`),u(5),d(),c(6,`td`),u(7),d(),c(8,`td`),u(9),d(),c(10,`td`),u(11),d()()}if(t&2){let e=n.$implicit,i=v(2);G(`selected`,e.id===i.store.selectedInstanceId()),m(3),B(` `,e.instanceName,` `),m(2),w(e.experimentName),m(2),Me(` `,e.counts.succeeded+e.counts.failed,` / `,e.counts.planned,` `),m(2),w(e.counts.running),m(2),Me(` `,e.counts.failed,` failed · `,e.finalized?`finalized`:`processing`,` `)}}function HO(t,n){t&1&&(c(0,`tr`)(1,`td`,46),u(2,`No matching instances yet.`),d()())}function UO(t,n){if(t&1&&(c(0,`p`,24),u(1),d()),t&2){let e=v();m(),Me(` Charts show `,e.results.length,` sampled points from `,e.resultCount,` repetitions. Statistics are exact; full data is available in Results and exports. `)}}function $O(t,n){if(t&1&&(u(0),j(1,`number`)),t&2){let e=v().$implicit;B(` (`,ke(1,1,e.objective.percentageGap,`1.0-2`),`%) `)}}function GO(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),d(),c(7,`td`),u(8),j(9,`number`),d(),c(10,`td`),u(11),j(12,`number`),d(),c(13,`td`),u(14),j(15,`number`),j(16,`number`),d(),c(17,`td`),u(18),j(19,`number`),E(20,$O,2,4),d(),c(21,`td`),u(22),j(23,`number`),d(),c(24,`td`),u(25),j(26,`number`),d(),c(27,`td`)(28,`button`,49),C(`click`,function(){let a=k(e).$implicit;return M(v(3).store.inspectResult(a.bestResultId))}),u(29,` Best result `),d()()()}if(t&2){let e=n.$implicit;m(2),w(e.name),m(2),Me(` `,e.counts.succeeded,` / `,e.counts.succeeded+e.counts.failed,` `),m(2),w(e.objective.count),m(2),w(ke(9,13,e.objective.best,`1.0-4`)??`—`),m(3),w(ke(12,16,e.objective.median,`1.0-4`)??`—`),m(3),Me(` `,ke(15,19,e.objective.mean,`1.0-4`)??`—`,` ± `,ke(16,22,e.objective.standardDeviation,`1.0-4`)??`—`,` `),m(4),B(` `,ke(19,25,e.objective.absoluteGap,`1.0-4`)??`—`,` `),m(2),D(e.objective.percentageGap!==null?20:-1),m(2),B(``,ke(23,28,e.runtimeMillis.median,`1.0-2`)??`—`,` ms`),m(3),B(``,ke(26,31,e.timeToBestMillis.median,`1.0-2`)??`—`,` ms`),m(3),S(`disabled`,!e.bestResultId)}}function WO(t,n){if(t&1){let e=$();c(0,`mat-checkbox`,56),C(`change`,function(a){k(e);return M(v(4).store.setReferenceVisible(a.checked))}),u(1),d(),c(2,`p`,24),u(3),d()}if(t&2){let e=v(),i=v(),a=v(2);S(`checked`,a.store.showReference()),m(),Me(` Show reference (`,i.objective,`: `,e.referenceValue,`) `),m(2),B(` The red dashed line marks the reference in all score charts. `,a.store.status()?.objectives?.[i.objective]===`MAXIMIZE`?`Higher`:`Lower`,` scores are better. `)}}function qO(t,n){if(t&1&&(c(0,`p`,24),u(1),d()),t&2){let e=v(2);m(),B(` No reference value is available for `,e.objective,`. `)}}function YO(t,n){if(t&1&&U(0,`app-dashboard-chart`,52),t&2)S(`model`,v().scoreChart)}function KO(t,n){if(t&1&&U(0,`app-dashboard-chart`,52),t&2)S(`model`,v().convergenceChart)}function QO(t,n){t&1&&U(0,`app-dashboard-chart`,52),t&2&&S(`model`,n)}function XO(t,n){t&1&&U(0,`app-dashboard-chart`,52),t&2&&S(`model`,n)}function ZO(t,n){if(t&1&&(E(0,WO,4,4)(1,qO,2,1,`p`,24),c(2,`div`,50)(3,`app-monitor-panel`,51),E(4,YO,1,1,`app-dashboard-chart`,52),d(),c(5,`app-monitor-panel`,53),E(6,KO,1,1,`app-dashboard-chart`,52),c(7,`p`,24),u(8,` Independent repetitions, not the optimization trajectory within a single run. Failures are gaps. `),d()(),c(9,`app-monitor-panel`,54),E(10,QO,1,1,`app-dashboard-chart`,52),d(),c(11,`app-monitor-panel`,55),E(12,XO,1,1,`app-dashboard-chart`,52),d()()),t&2){let e,i,a=v(3);D(n.referenceValue!==null?0:1),m(4),D(!a.preferences.isHidden(`normal-scores`)&&!a.preferences.isCollapsed(`normal-scores`)?4:-1),m(2),D(!a.preferences.isHidden(`normal-best`)&&!a.preferences.isCollapsed(`normal-best`)?6:-1),m(4),D((e=!a.preferences.isHidden(`normal-distribution`)&&!a.preferences.isCollapsed(`normal-distribution`)&&a.store.distribution())?10:-1,e),m(2),D((i=!a.preferences.isHidden(`normal-runtime`)&&!a.preferences.isCollapsed(`normal-runtime`)&&a.store.qualityRuntime())?12:-1,i)}}function JO(t,n){if(t&1&&(c(0,`h2`,47),u(1),c(2,`small`),u(3),d()(),E(4,UO,2,2,`p`,24),c(5,`app-monitor-panel`,48)(6,`div`,41)(7,`table`)(8,`caption`,42),u(9),d(),c(10,`thead`)(11,`tr`)(12,`th`),u(13,`Algorithm`),d(),c(14,`th`),u(15,`Successful / attempted`),d(),c(16,`th`),u(17,`Valid scores`),d(),c(18,`th`),u(19,`Best`),d(),c(20,`th`),u(21,`Median`),d(),c(22,`th`),u(23,`Mean ± SD`),d(),c(24,`th`),u(25,`Gap to reference`),d(),c(26,`th`),u(27,`Runtime median`),d(),c(28,`th`),u(29,`Time-to-best median`),d(),c(30,`th`),u(31,`Details`),d()()(),c(32,`tbody`),de(33,GO,30,34,`tr`,null,Bb),d()()(),c(35,`p`,24),u(36,` Statistics use successful results with finite values. Scores from different instances are not averaged. Negative gaps improve on the reference. `),d()(),E(37,ZO,13,5)),t&2){let e,i=n,a=v(2);m(),B(` `,i.instance.instanceName,` `),m(2),w(i.instance.experimentName),m(),D(i.sampled?4:-1),m(5),B(` `,i.objective,` statistics by algorithm `),m(24),ue(i.algorithms),m(4),D((e=a.store.charts())?37:-1,e)}}function eP(t,n){t&1&&(c(0,`p`,45),u(1,`Select an instance to inspect its results and charts.`),d())}function tP(t,n){if(t&1){let e=$();c(0,`mat-card`,36)(1,`mat-card-content`)(2,`h2`),u(3,`Instances`),d(),c(4,`div`,37)(5,`mat-form-field`,38)(6,`mat-label`),u(7,`Experiment`),d(),c(8,`mat-select`,15),C(`selectionChange`,function(a){k(e);let o=v();return M(o.store.filterInstances(a.value,o.store.instanceQuery().search))}),c(9,`mat-option`,39),u(10,`All experiments`),d(),de(11,jO,2,2,`mat-option`,16,Bb),d()(),c(13,`mat-form-field`,38)(14,`mat-label`),u(15,`Search instances`),d(),c(16,`input`,40),C(`input`,function(a){k(e);let o=v();return M(o.store.filterInstances(o.store.instanceQuery().experiment,a.target.value))}),d()()(),c(17,`div`,41)(18,`table`)(19,`caption`,42),u(20,` Instances across the execution `),d(),c(21,`thead`)(22,`tr`)(23,`th`),u(24,`Instance`),d(),c(25,`th`),u(26,`Experiment`),d(),c(27,`th`),u(28,`Completed repetitions`),d(),c(29,`th`),u(30,`Running`),d(),c(31,`th`),u(32,`Outcome`),d()()(),c(33,`tbody`),de(34,zO,12,9,`tr`,43,ec,!1,HO,3,0,`tr`),d()()(),c(37,`mat-paginator`,44),C(`page`,function(a){k(e);return M(v().store.pageInstances(a.pageIndex*a.pageSize))}),d()()(),E(38,JO,38,5)(39,eP,2,0,`p`,45)}if(t&2){let e,i=v();m(8),S(`value`,i.store.instanceQuery().experiment),m(3),ue(i.store.status()?.experiments??nt(7,hs)),m(5),S(`value`,i.store.instanceQuery().search),m(18),ue(i.store.instancePage()?.items??nt(8,hs)),m(3),S(`length`,i.store.instancePage()?.total??0)(`pageSize`,25)(`pageIndex`,i.store.instanceQuery().offset/25),m(),D((e=i.store.detail())?38:39,e)}}function nP(t,n){if(t&1&&(c(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit,i=v(2);S(`value`,e),m(),w(e?i.phaseLabel(e):`All outcomes`)}}function iP(t,n){if(t&1&&(c(0,`a`,63),u(1,`CSV`),d(),c(2,`a`,63),u(3,`JSON`),d()),t&2){let e=v(2);S(`href`,e.store.exportUrl(`csv`),dn),m(2),S(`href`,e.store.exportUrl(`json`),dn)}}function aP(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),d(),c(7,`td`),u(8),d(),c(9,`td`),u(10),d(),c(11,`td`),u(12),j(13,`number`),d(),c(14,`td`),u(15),j(16,`number`),d(),c(17,`td`)(18,`button`,35),C(`click`,function(){let a=k(e).$implicit;return M(v(2).store.inspectResult(a.resultId))}),u(19,` Inspect `),d()()()}if(t&2){let e=n.$implicit,i=v(2);m(2),Me(``,e.experimentName,` / `,e.instanceName),m(2),w(e.algorithmName),m(2),w(e.iteration),m(2),w(e.seed),m(2),w(i.phaseLabel(e.state)),m(2),B(` `,ke(13,8,e.objectives[i.store.selectedObjective()??``],`1.0-4`)??`—`,` `),m(3),B(` `,e.executionTime===null?`—`:ke(16,11,e.executionTime/1e6,`1.0-2`)+` ms`,` `)}}function oP(t,n){t&1&&(c(0,`tr`)(1,`td`,64),u(2,`No matching repetitions.`),d()())}function rP(t,n){if(t&1){let e=$();c(0,`mat-card`,36)(1,`mat-card-content`)(2,`div`,37)(3,`mat-form-field`,38)(4,`mat-label`),u(5,`Search experiment, instance or algorithm`),d(),c(6,`input`,40),C(`input`,function(a){k(e);let o=v();return M(o.store.filterResults(o.store.resultQuery().instanceId,o.store.resultQuery().algorithm,o.store.resultQuery().state,a.target.value))}),d()(),c(7,`mat-form-field`,38)(8,`mat-label`),u(9,`Outcome`),d(),c(10,`mat-select`,15),C(`selectionChange`,function(a){k(e);let o=v();return M(o.store.filterResults(o.store.resultQuery().instanceId,o.store.resultQuery().algorithm,a.value,o.store.resultQuery().search))}),de(11,nP,2,2,`mat-option`,16,Pn),d()(),c(13,`mat-form-field`,38)(14,`mat-label`),u(15,`Algorithm name`),d(),c(16,`input`,40),C(`input`,function(a){k(e);let o=v();return M(o.store.filterResults(o.store.resultQuery().instanceId,a.target.value,o.store.resultQuery().state,o.store.resultQuery().search))}),d()(),c(17,`mat-form-field`,38)(18,`mat-label`),u(19,`Sort results`),d(),c(20,`mat-select`,15),C(`selectionChange`,function(a){k(e);return M(v().store.sortResults(a.value))}),c(21,`mat-option`,57),u(22,`Planned order`),d(),c(23,`mat-option`,58),u(24,`Instance name`),d(),c(25,`mat-option`,59),u(26,`Algorithm name`),d(),c(27,`mat-option`,60),u(28,`Outcome`),d(),c(29,`mat-option`,61),u(30,`Runtime · fastest first`),d()()(),c(31,`button`,35),C(`click`,function(){k(e);let a=v();return M(a.store.filterResults(a.store.selectedInstanceId()??``,a.store.resultQuery().algorithm,a.store.resultQuery().state,a.store.resultQuery().search))}),u(32,` Selected instance only `),d(),c(33,`button`,35),C(`click`,function(){k(e);return M(v().store.filterResults(``,``,``,``))}),u(34,` Clear filters `),d(),E(35,iP,4,2),d(),c(36,`div`,41)(37,`table`)(38,`caption`,42),u(39,` Repetition result ledger `),d(),c(40,`thead`)(41,`tr`)(42,`th`),u(43,`Experiment / instance`),d(),c(44,`th`),u(45,`Algorithm`),d(),c(46,`th`),u(47,`Iteration`),d(),c(48,`th`),u(49,`Seed`),d(),c(50,`th`),u(51,`Outcome`),d(),c(52,`th`),u(53),d(),c(54,`th`),u(55,`Runtime`),d(),c(56,`th`),u(57,`Details`),d()()(),c(58,`tbody`),de(59,aP,20,14,`tr`,null,RO,!1,oP,3,0,`tr`),d()()(),c(62,`mat-paginator`,62),C(`page`,function(a){k(e);return M(v().store.pageResults(a.pageIndex*a.pageSize))}),d()()()}if(t&2){let e=v();m(6),S(`value`,e.store.resultQuery().search),m(4),S(`value`,e.store.resultQuery().state),m(),ue(e.workStates),m(5),S(`value`,e.store.resultQuery().algorithm),m(4),S(`value`,e.store.resultQuery().order??`PLANNED`),m(15),D(e.store.status()?.runId?35:-1),m(18),w(e.store.selectedObjective()??`Objective`),m(6),ue(e.store.resultPage()?.items??nt(10,hs)),m(3),S(`length`,e.store.resultPage()?.total??0)(`pageSize`,25)(`pageIndex`,e.store.resultQuery().offset/25)}}function sP(t,n){if(t&1&&u(0),t&2){let e=v().$implicit;Me(` · iteration `,e.iteration,` · seed `,e.seed,` `)}}function lP(t,n){if(t&1){let e=$();c(0,`button`,35),C(`click`,function(){k(e);let a=v().$implicit;return M(v(2).store.inspectResult(a.resultId))}),u(1,` Inspect result `),d()}}function cP(t,n){if(t&1&&(c(0,`mat-card`,68)(1,`mat-card-content`)(2,`strong`),u(3),d(),c(4,`p`),u(5),d(),c(6,`p`),u(7),E(8,sP,1,2),u(9),j(10,`date`),d(),E(11,lP,2,0,`button`,25),d()()),t&2){let e=n.$implicit;m(3),Ot(``,e.severity,` · `,e.scope,` · `,e.exceptionType),m(2),w(e.message??`No details provided`),m(2),Ot(` `,e.experimentName,` / `,e.instanceName,` / `,e.algorithmName,` `),m(),D(e.iteration!==null?8:-1),m(),B(` · `,ke(10,10,e.timestamp,`medium`),` `),m(2),D(e.resultId?11:-1)}}function dP(t,n){t&1&&(c(0,`p`,45),u(1,`No experiment issues have been reported.`),d())}function uP(t,n){if(t&1){let e=$();c(0,`div`,37)(1,`mat-form-field`,38)(2,`mat-label`),u(3,`Severity`),d(),c(4,`mat-select`,15),C(`selectionChange`,function(a){k(e);return M(v().store.filterIssues(a.value))}),c(5,`mat-option`,39),u(6,`All severities`),d(),c(7,`mat-option`,65),u(8,`Fatal`),d(),c(9,`mat-option`,66),u(10,`Error`),d(),c(11,`mat-option`,67),u(12,`Warning`),d()()(),c(13,`button`,35),C(`click`,function(){k(e);return M(v().store.markIssuesSeen())}),u(14,`Mark issues seen`),d()(),de(15,cP,12,13,`mat-card`,68,ec,!1,dP,2,0,`p`,45),c(18,`mat-paginator`,69),C(`page`,function(a){k(e);return M(v().store.pageIssues(a.pageIndex*a.pageSize))}),d()}if(t&2){let e=v();m(4),S(`value`,e.store.issueSeverity()),m(11),ue(e.store.issuePage()?.items??nt(5,hs)),m(3),S(`length`,e.store.issuePage()?.total??0)(`pageSize`,25)(`pageIndex`,e.store.issueOffset()/25)}}function mP(t,n){if(t&1&&(c(0,`a`,63),u(1,`Download results CSV`),d(),c(2,`a`,63),u(3,`Download results JSON`),d()),t&2){let e=v(2);S(`href`,e.store.exportUrl(`csv`),dn),m(2),S(`href`,e.store.exportUrl(`json`),dn)}}function pP(t,n){if(t&1&&(c(0,`tr`)(1,`td`)(2,`a`,70),u(3),d()(),c(4,`td`),u(5),d(),c(6,`td`),u(7),j(8,`number`),d(),c(9,`td`),u(10),j(11,`date`),d()()),t&2){let e=n.$implicit,i=v(2);m(2),S(`href`,i.store.artifactUrl(e.id),dn),m(),w(e.filename),m(2),w(e.experimentName),m(2),w(_e(8,5,e.size)),m(3),w(ke(11,7,e.modifiedAt,`medium`))}}function hP(t,n){t&1&&(c(0,`tr`)(1,`td`,71),u(2,`No exported artifacts are available yet.`),d()())}function fP(t,n){if(t&1&&(c(0,`p`),u(1,` Only successfully exported, registered files are available. Result exports use the current Results filters. `),d(),E(2,mP,4,2),c(3,`div`,41)(4,`table`)(5,`thead`)(6,`tr`)(7,`th`),u(8,`File`),d(),c(9,`th`),u(10,`Experiment`),d(),c(11,`th`),u(12,`Bytes`),d(),c(13,`th`),u(14,`Updated`),d()()(),c(15,`tbody`),de(16,pP,12,10,`tr`,null,ec,!1,hP,3,0,`tr`),d()()()),t&2){let e=v();m(2),D(e.store.status()?.runId?2:-1),m(14),ue(e.store.artifacts()?.artifacts??nt(2,hs))}}function gP(t,n){t&1&&(c(0,`p`),u(1),d()),t&2&&(m(),w(n))}function bP(t,n){if(t&1){let e=$();c(0,`mat-card`,72)(1,`mat-card-content`)(2,`h2`),u(3,`Transport diagnostics`),d(),c(4,`p`),u(5),d(),c(6,`p`),u(7),d(),c(8,`p`),u(9),d(),c(10,`p`),u(11),d(),E(12,gP,2,1,`p`),c(13,`p`),u(14,` Summaries and results come from backend snapshots. Connection health is independent of execution outcome. `),d(),c(15,`button`,35),C(`click`,function(){k(e);return M(v().store.retryNow())}),u(16,`Reconnect / refresh`),d()()()}if(t&2){let e,i=v();m(5),B(`Run: `,i.store.status()?.runId??`No execution`),m(2),B(`Revision: `,i.store.status()?.revision??0),m(2),B(`WebSocket: `,i.store.transportState()),m(2),B(`Last processed notification ID: `,i.store.lastEventId()),m(),D((e=i.store.transportError())?12:-1,e)}}function vP(t,n){if(t&1){let e=$();c(0,`mat-card`,9)(1,`mat-card-content`),u(2),c(3,`button`,35),C(`click`,function(){k(e);return M(v().store.closeResult())}),u(4,`Close`),d()()()}t&2&&(m(2),w(n))}function _P(t,n){if(t&1&&(c(0,`a`,63),u(1),d()),t&2){let e=v().$implicit;S(`href`,v(2).store.artifactUrl(e.id),dn),m(),w(e.filename)}}function yP(t,n){if(t&1&&E(0,_P,2,2,`a`,63),t&2){let e=n.$implicit,i=v();D(e.resultId===i.result.resultId?0:-1)}}function xP(t,n){if(t&1){let e=$();c(0,`mat-card`,33)(1,`mat-card-content`)(2,`div`,73)(3,`h2`),u(4,`Result details`),d(),c(5,`button`,35),C(`click`,function(){k(e);return M(v().store.closeResult())}),u(6,`Close`),d()(),U(7,`app-solution-renderer`,74),de(8,yP,1,1,null,null,ec),d()()}if(t&2){let e=v();m(7),S(`detail`,n)(`selectedObjective`,e.store.selectedObjective()),m(),ue(e.store.artifacts()?.artifacts??nt(2,hs))}}var wm=class t{store=f(Jl);preferences=f(Ba);panels=Vb;workStates=[``,`PENDING`,`RUNNING`,`SUCCEEDED`,`FAILED`,`CANCELLED`];ngOnInit(){this.store.start()}progressValue(n,e){return e>0?Math.min(100,n/e*100):0}elapsedLabel(n){let e=Math.floor(n/1e3);return e>=3600?Math.floor(e/3600)+`h `+Math.floor(e%3600/60)+`m`:Math.floor(e/60)+`m `+e%60+`s`}phaseLabel(n){return n.toLowerCase().replaceAll(`_`,` `)}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-standard-dashboard`]],features:[Ge([Jl,us,La])],decls:113,vars:31,consts:[[`panelMenu`,`matMenu`],[1,`topbar`],[`src`,`favicon.svg`,`alt`,`Mork`,1,`logo`],[1,`brand`],[1,`toolbar-spacer`],[`aria-label`,`Connection state`],[`mat-button`,``,`aria-label`,`Choose dashboard panels`,3,`matMenuTriggerFor`],[`mat-menu-item`,``],[`mat-menu-item`,``,3,`click`],[`role`,`alert`,1,`error-card`],[`appearance`,`outlined`,1,`status-card`],[`aria-label`,`Execution summary`,1,`summary`],[1,`eyebrow`],[1,`phase`],[`appearance`,`outline`,`subscriptSizing`,`dynamic`],[3,`selectionChange`,`value`],[3,`value`],[`aria-label`,`Repetition outcomes`,1,`metrics`],[`aria-label`,`Execution progress`,1,`progress-grid`],[1,`progress-item`],[1,`progress-label`],[`mode`,`determinate`,`aria-label`,`Finalized instances in execution`,3,`value`],[`mode`,`determinate`,`aria-label`,`Finalized algorithms in selected instance`,3,`value`],[`mode`,`determinate`,`aria-label`,`Completed repetitions in selection`,3,`value`],[1,`status-footnote`],[`mat-button`,``],[`animationDuration`,`0ms`,3,`selectedIndexChange`,`selectedIndex`],[`label`,`Overview`],[`matTabContent`,``],[`label`,`Results`],[`label`,`Issues`],[`label`,`Artifacts`],[`label`,`Diagnostics`],[`appearance`,`outlined`,`aria-label`,`Result details`,1,`result-detail`],[`mat-flat-button`,``,3,`click`],[`mat-button`,``,3,`click`],[`appearance`,`outlined`,1,`browser`],[1,`filters`],[`appearance`,`outline`],[`value`,``],[`matInput`,``,3,`input`,`value`],[1,`table-scroll`],[1,`sr-only`],[3,`selected`],[`aria-label`,`Instance pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[1,`empty-state`],[`colspan`,`5`],[1,`detail-title`],[`panelId`,`normal-comparison`,`title`,`Algorithm comparison`],[`mat-button`,``,3,`click`,`disabled`],[1,`chart-grid`],[`panelId`,`normal-scores`,`title`,`Scores`],[3,`model`],[`panelId`,`normal-best`,`title`,`Best score across repetitions`],[`panelId`,`normal-distribution`,`title`,`Score distribution`],[`panelId`,`normal-runtime`,`title`,`Score versus runtime`],[1,`reference-toggle`,3,`change`,`checked`],[`value`,`PLANNED`],[`value`,`INSTANCE`],[`value`,`ALGORITHM`],[`value`,`STATE`],[`value`,`RUNTIME`],[`aria-label`,`Result pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[`mat-button`,``,3,`href`],[`colspan`,`8`],[`value`,`FATAL`],[`value`,`ERROR`],[`value`,`WARNING`],[`appearance`,`outlined`,1,`issue`],[`aria-label`,`Issue pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[3,`href`],[`colspan`,`4`],[1,`browser`],[1,`result-heading`],[3,`detail`,`selectedObjective`]],template:function(e,i){if(e&1&&(c(0,`mat-toolbar`,1),U(1,`img`,2),c(2,`span`,3),u(3,`Experiment monitor`),d(),U(4,`span`,4),c(5,`mat-chip-set`,5)(6,`mat-chip`),u(7),d()(),c(8,`button`,6),u(9,` Panels `),d(),c(10,`mat-menu`,null,0),de(12,OO,2,2,`button`,7,ec),c(14,`button`,8),C(`click`,function(){return i.preferences.reset()}),u(15,`Reset panels`),d()()(),c(16,`main`),E(17,PO,8,1,`mat-card`,9),c(18,`mat-card`,10)(19,`mat-card-content`)(20,`section`,11)(21,`div`)(22,`span`,12),u(23,`Execution`),d(),c(24,`strong`,13),u(25),d()(),c(26,`div`)(27,`span`,12),u(28,`Experiment`),d(),c(29,`strong`),u(30),d()(),c(31,`div`)(32,`span`,12),u(33,`Elapsed`),d(),c(34,`strong`),u(35),d()(),c(36,`mat-form-field`,14)(37,`mat-label`),u(38,`Objective`),d(),c(39,`mat-select`,15),C(`selectionChange`,function(o){return i.store.selectObjective(o.value)}),de(40,FO,2,3,`mat-option`,16,Bb),d()()(),c(42,`section`,17)(43,`div`)(44,`span`),u(45,`Planned`),d(),c(46,`strong`),u(47),d()(),c(48,`div`)(49,`span`),u(50,`Pending`),d(),c(51,`strong`),u(52),d()(),c(53,`div`)(54,`span`),u(55,`Running`),d(),c(56,`strong`),u(57),d()(),c(58,`div`)(59,`span`),u(60,`Successful`),d(),c(61,`strong`),u(62),d()(),c(63,`div`)(64,`span`),u(65,`Failed`),d(),c(66,`strong`),u(67),d()(),c(68,`div`)(69,`span`),u(70,`Cancelled`),d(),c(71,`strong`),u(72),d()()(),c(73,`section`,18)(74,`div`,19)(75,`div`,20)(76,`span`),u(77,`Instances · execution`),d(),c(78,`span`),u(79),d()(),U(80,`mat-progress-bar`,21),d(),c(81,`div`,19)(82,`div`,20)(83,`span`),u(84,`Algorithms · selected instance`),d(),c(85,`span`),u(86),d()(),U(87,`mat-progress-bar`,22),d(),c(88,`div`,19)(89,`div`,20)(90,`span`),u(91),d(),c(92,`span`),u(93),d()(),U(94,`mat-progress-bar`,23),d()(),c(95,`p`,24),u(96),E(97,LO,2,4),d(),E(98,VO,2,1,`p`,24),E(99,BO,2,1,`button`,25),d()(),c(100,`mat-tab-group`,26),C(`selectedIndexChange`,function(o){return i.store.selectTab(o)}),c(101,`mat-tab`,27),st(102,tP,40,9,`ng-template`,28),d(),c(103,`mat-tab`,29),st(104,rP,63,11,`ng-template`,28),d(),c(105,`mat-tab`,30),st(106,uP,19,6,`ng-template`,28),d(),c(107,`mat-tab`,31),st(108,fP,19,3,`ng-template`,28),d(),c(109,`mat-tab`,32),st(110,bP,17,5,`ng-template`,28),d()(),E(111,vP,5,1,`mat-card`,9),E(112,xP,10,3,`mat-card`,33),d()),e&2){let a,o,r,s,l,h=ft(11);m(7),w(i.store.connectionLabel()),m(),S(`matMenuTriggerFor`,h),m(4),ue(i.panels),m(5),D((a=i.store.latestError())?17:-1,a);let p=i.store.status();m(8),w(i.phaseLabel(p?.phase??`IDLE`)),m(5),w(p?.currentExperiment??`Waiting for execution`),m(5),w(i.elapsedLabel(p?.elapsedMillis??0)),m(4),S(`value`,i.store.selectedObjective()),m(),ue(i.store.objectives()),m(7),w(p?.counts?.planned??0),m(5),w(p?.counts?.pending??0),m(5),w(p?.counts?.running??0),m(5),w(p?.counts?.succeeded??0),m(5),w(p?.counts?.failed??0),m(5),w(p?.counts?.cancelled??0);let g=i.store.progress();m(7),Me(``,g.instances.completed,` / `,g.instances.total),m(),S(`value`,i.progressValue(g.instances.completed,g.instances.total)),m(6),Me(``,g.algorithms.completed,` / `,g.algorithms.total),m(),S(`value`,i.progressValue(g.algorithms.completed,g.algorithms.total)),m(4),B(`Repetitions · `,g.repetitionScope),m(2),Me(``,g.repetitions.completed,` / `,g.repetitions.total),m(),S(`value`,i.progressValue(g.repetitions.completed,g.repetitions.total)),m(2),Me(` `,p?.runningInstances??0,` active instances · `,p?.runningAlgorithms??0,` active algorithms. Completed repetitions include failures; cancelled work does not count as completed. `),m(),D((o=i.store.lastUpdated())?97:-1,o),m(),D((r=i.store.estimatedRemainingMillis())?98:-1,r),m(),D(i.store.unseenIssues()>0?99:-1),m(),S(`selectedIndex`,i.store.tab()),m(11),D((s=i.store.resultError())?111:-1,s),m(),D((l=i.store.inspectedResult())?112:-1,l)}},dependencies:[Ai,ps,Ft,nn,zn,Bn,jn,am,Ul,im,om,$l,di,Ti,na,es,Jr,ls,Ni,la,ss,gm,Ql,ds,cs,Fa,Pa,Un,vm,Xl,Zl,bm,ym,_m,Sm,Xi,En],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh;color:var(--%NS%mat-sys-on-surface);background:var(--%NS%mat-sys-surface-container-lowest)}.topbar[_ngcontent-%COMP%]{position:sticky;top:0;z-index:10;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:var(--%NS%mat-sys-surface);gap:.75rem}.logo[_ngcontent-%COMP%]{width:2.2rem;height:2.2rem}.brand[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-large);font-weight:700}.toolbar-spacer[_ngcontent-%COMP%]{flex:1}main[_ngcontent-%COMP%]{width:min(100% - 2rem,96rem);margin:auto;padding:1.5rem 0 4rem}.status-card[_ngcontent-%COMP%], .browser[_ngcontent-%COMP%], .issue[_ngcontent-%COMP%], .result-detail[_ngcontent-%COMP%]{margin:1rem 0}.summary[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(3,minmax(0,1fr)) minmax(13rem,1fr);gap:1rem;align-items:center}.summary[_ngcontent-%COMP%] > div[_ngcontent-%COMP%], .metrics[_ngcontent-%COMP%] > div[_ngcontent-%COMP%]{display:grid;gap:.3rem}.phase[_ngcontent-%COMP%]{text-transform:capitalize}.eyebrow[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-label-medium);text-transform:uppercase;color:var(--%NS%mat-sys-on-surface-variant);letter-spacing:.05em}.metrics[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(6,1fr);gap:1rem;padding:1.2rem 0}.metrics[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-headline-small)}.progress-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:1.5rem;border-top:1px solid var(--%NS%mat-sys-outline-variant);padding-top:1rem}.progress-item[_ngcontent-%COMP%]{display:grid;gap:.5rem}.progress-label[_ngcontent-%COMP%]{display:flex;justify-content:space-between;gap:.5rem;font:var(--%NS%mat-sys-label-large)}.status-footnote[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.85rem;line-height:1.5}.filters[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem;align-items:center;margin:1rem 0}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{flex:1;min-width:12rem}.table-scroll[_ngcontent-%COMP%]{overflow-x:auto}table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse;font:var(--%NS%mat-sys-body-medium)}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{text-align:left;padding:.65rem .75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);white-space:nowrap}th[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-large)}tr.selected[_ngcontent-%COMP%]{background:var(--%NS%mat-sys-secondary-container)}.chart-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem;margin-top:1rem}.detail-title[_ngcontent-%COMP%]{margin:1.5rem 0 1rem}small[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.85rem}.error-card[_ngcontent-%COMP%]{background:var(--%NS%mat-sys-error-container);color:var(--%NS%mat-sys-on-error-container);margin:1rem 0}.result-heading[_ngcontent-%COMP%]{display:flex;justify-content:space-between;align-items:center}.empty-state[_ngcontent-%COMP%]{min-height:8rem;display:grid;place-items:center;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}.sr-only[_ngcontent-%COMP%]{position:absolute;width:1px;height:1px;padding:0;overflow:hidden;clip:rect(0,0,0,0)}@media(max-width:60rem){.summary[_ngcontent-%COMP%]{grid-template-columns:repeat(2,minmax(0,1fr))}.metrics[_ngcontent-%COMP%]{grid-template-columns:repeat(3,1fr)}.progress-grid[_ngcontent-%COMP%]{grid-template-columns:1fr}}@media(max-width:42rem){main[_ngcontent-%COMP%]{width:calc(100% - 1rem);padding-top:.25rem}.chart-grid[_ngcontent-%COMP%], .summary[_ngcontent-%COMP%]{grid-template-columns:1fr}.brand[_ngcontent-%COMP%]{display:none}.topbar[_ngcontent-%COMP%]{gap:.25rem;padding:0 .5rem}.logo[_ngcontent-%COMP%]{width:1.5rem}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{min-width:100%}}`]})};var CP=new N(`MAT_BADGE_CONFIG`);var $w=`mat-badge-content`;var SP=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`ng-component`]],decls:0,vars:0,template:function(i,a){},styles:[`.mat-badge {
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
`],encapsulation:2})}return t})();var Gw=(()=>{class t{_ngZone=f(Z);_elementRef=f(Y);_ariaDescriber=f(Pu);_renderer=f(Pe);_animationsDisabled=Ue();_idGenerator=f(je);get color(){return this._color}set color(e){this._setColor(e),this._color=e}_color;overlap;disabled=!1;position;get content(){return this._content}set content(e){this._updateRenderedContent(e)}_content;get description(){return this._description}set description(e){this._updateDescription(e)}_description;size;hidden=!1;_badgeElement;_inlineBadgeDescription;_isInitialized=!1;_interactivityChecker=f(jr);_document=f(me);constructor(){let e=f(CP,{optional:!0}),i=f(Je);i.load(SP),i.load(Ii),this._color=e?.color||`primary`,this.overlap=e?.overlap??!0,this.position=e?.position||`above after`,this.size=e?.size||`medium`}isAbove(){return this.position.indexOf(`below`)===-1}isAfter(){return this.position.indexOf(`before`)===-1}getBadgeElement(){return this._badgeElement}ngOnInit(){this._clearExistingBadges(),this.content&&!this._badgeElement&&(this._badgeElement=this._createBadgeElement(),this._updateRenderedContent(this.content)),this._isInitialized=!0}ngAfterViewInit(){}ngOnDestroy(){this._renderer.destroyNode&&(this._renderer.destroyNode(this._badgeElement),this._inlineBadgeDescription?.remove()),this._ariaDescriber.removeDescription(this._elementRef.nativeElement,this.description)}_isHostInteractive(){return this._interactivityChecker.isFocusable(this._elementRef.nativeElement,{ignoreVisibility:!0})}_createBadgeElement(){let e=this._renderer.createElement(`span`),i=`mat-badge-active`;return e.setAttribute(`id`,this._idGenerator.getId(`mat-badge-content-`)),e.setAttribute(`aria-hidden`,`true`),e.classList.add($w),this._animationsDisabled&&e.classList.add(`_mat-animation-noopable`),this._elementRef.nativeElement.appendChild(e),typeof requestAnimationFrame==`function`&&!this._animationsDisabled?this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>{e.classList.add(i)})}):e.classList.add(i),e}_updateRenderedContent(e){let i=`${e??``}`.trim();this._isInitialized&&i&&!this._badgeElement&&(this._badgeElement=this._createBadgeElement()),this._badgeElement&&(this._badgeElement.textContent=i),this._content=i}_updateDescription(e){this._ariaDescriber.removeDescription(this._elementRef.nativeElement,this.description),(!e||this._isHostInteractive())&&this._removeInlineDescription(),this._description=e,this._isHostInteractive()?this._ariaDescriber.describe(this._elementRef.nativeElement,e):this._updateInlineDescription()}_updateInlineDescription(){this._inlineBadgeDescription||(this._inlineBadgeDescription=this._document.createElement(`span`),this._inlineBadgeDescription.classList.add(`cdk-visually-hidden`)),this._inlineBadgeDescription.textContent=this.description,this._badgeElement?.appendChild(this._inlineBadgeDescription)}_removeInlineDescription(){this._inlineBadgeDescription?.remove(),this._inlineBadgeDescription=void 0}_setColor(e){let i=this._elementRef.nativeElement.classList;i.remove(`mat-badge-${this._color}`),e&&i.add(`mat-badge-${e}`)}_clearExistingBadges(){let e=this._elementRef.nativeElement.querySelectorAll(`:scope > .${$w}`);for(let i of Array.from(e))i!==this._badgeElement&&i.remove()}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`matBadge`,``]],hostAttrs:[1,`mat-badge`],hostVars:20,hostBindings:function(i,a){i&2&&G(`mat-badge-overlap`,a.overlap)(`mat-badge-above`,a.isAbove())(`mat-badge-below`,!a.isAbove())(`mat-badge-before`,!a.isAfter())(`mat-badge-after`,a.isAfter())(`mat-badge-small`,a.size===`small`)(`mat-badge-medium`,a.size===`medium`)(`mat-badge-large`,a.size===`large`)(`mat-badge-hidden`,a.hidden||!a.content)(`mat-badge-disabled`,a.disabled)},inputs:{color:[0,`matBadgeColor`,`color`],overlap:[2,`matBadgeOverlap`,`overlap`,z],disabled:[2,`matBadgeDisabled`,`disabled`,z],position:[0,`matBadgePosition`,`position`],content:[0,`matBadge`,`content`],description:[0,`matBadgeDescription`,`description`],size:[0,`matBadgeSize`,`size`],hidden:[2,`matBadgeHidden`,`hidden`,z]}})}return t})();var Ww=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[No,we]})}return t})();var ja=class{viewContainerRef;injector;id;role=`dialog`;panelClass=``;hasBackdrop=!0;backdropClass=``;disableClose=!1;closePredicate;width=``;height=``;minWidth;minHeight;maxWidth;maxHeight;positionStrategy;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus=`first-tabbable`;restoreFocus=!0;scrollStrategy;closeOnNavigation=!0;closeOnDestroy=!0;closeOnOverlayDetachments=!0;disableAnimations=!1;providers;container;templateContext;bindings};var zb=(()=>{class t extends ts{_elementRef=f(Y);_focusTrapFactory=f(zg);_config;_interactivityChecker=f(jr);_ngZone=f(Z);_focusMonitor=f(tn);_renderer=f(Pe);_changeDetectorRef=f(Le);_injector=f(se);_platform=f(Re);_document=f(me);_portalOutlet;_focusTrapped=new V;_focusTrap=null;_elementFocusedBeforeDialogWasOpened=null;_closeInteractionType=null;_ariaLabelledByQueue=[];_isDestroyed=!1;constructor(){super(),this._config=f(ja,{optional:!0})||new ja,this._config.ariaLabelledBy&&this._ariaLabelledByQueue.push(this._config.ariaLabelledBy)}_addAriaLabelledBy(e){this._ariaLabelledByQueue.push(e),this._changeDetectorRef.markForCheck()}_removeAriaLabelledBy(e){let i=this._ariaLabelledByQueue.indexOf(e);i>-1&&(this._ariaLabelledByQueue.splice(i,1),this._changeDetectorRef.markForCheck())}_contentAttached(){this._initializeFocusTrap(),this._captureInitialFocus()}_captureInitialFocus(){this._trapFocus()}ngOnDestroy(){this._focusTrapped.complete(),this._isDestroyed=!0,this._restoreFocus()}attachComponentPortal(e){this._portalOutlet.hasAttached();let i=this._portalOutlet.attachComponentPortal(e);return this._contentAttached(),i}attachTemplatePortal(e){this._portalOutlet.hasAttached();let i=this._portalOutlet.attachTemplatePortal(e);return this._contentAttached(),i}attachDomPortal=e=>{this._portalOutlet.hasAttached();let i=this._portalOutlet.attachDomPortal(e);return this._contentAttached(),i};_recaptureFocus(){this._containsFocus()||this._trapFocus()}_forceFocus(e,i){this._interactivityChecker.isFocusable(e)||(e.tabIndex=-1,this._ngZone.runOutsideAngular(()=>{let a=()=>{o(),r(),e.removeAttribute(`tabindex`)},o=this._renderer.listen(e,`blur`,a),r=this._renderer.listen(e,`mousedown`,a)})),e.focus(i)}_focusByCssSelector(e,i){let a=this._elementRef.nativeElement.querySelector(e);a&&this._forceFocus(a,i)}_trapFocus(e){this._isDestroyed||Rt(()=>{let i=this._elementRef.nativeElement;switch(this._config.autoFocus){case!1:case`dialog`:this._containsFocus()||i.focus(e);break;case!0:case`first-tabbable`:this._focusTrap?.focusInitialElement(e)||this._focusDialogContainer(e);break;case`first-heading`:this._focusByCssSelector(`h1, h2, h3, h4, h5, h6, [role="heading"]`,e);break;default:this._focusByCssSelector(this._config.autoFocus,e)}this._focusTrapped.next()},{injector:this._injector})}_restoreFocus(){let e=this._config.restoreFocus,i=null;if(typeof e==`string`?i=this._document.querySelector(e):typeof e==`boolean`?i=e?this._elementFocusedBeforeDialogWasOpened:null:e&&(i=e),this._config.restoreFocus&&i&&typeof i.focus==`function`){let a=Dl(),o=this._elementRef.nativeElement;(!a||a===this._document.body||a===o||o.contains(a))&&(this._focusMonitor?(this._focusMonitor.focusVia(i,this._closeInteractionType),this._closeInteractionType=null):i.focus())}this._focusTrap&&this._focusTrap.destroy()}_focusDialogContainer(e){this._elementRef.nativeElement.focus?.(e)}_containsFocus(){let e=this._elementRef.nativeElement,i=Dl();return e===i||e.contains(i)}_initializeFocusTrap(){this._platform.isBrowser&&(this._focusTrap=this._focusTrapFactory.create(this._elementRef.nativeElement),this._document&&(this._elementFocusedBeforeDialogWasOpened=Dl()))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(i,a){}return H({type:t,selectors:[[`cdk-dialog-container`]],viewQuery:function(a,o){if(a&1&&Ze(Ra,7),a&2){let r;J(r=ee())&&(o._portalOutlet=r.first)}},hostAttrs:[`tabindex`,`-1`,1,`cdk-dialog-container`],hostVars:6,hostBindings:function(a,o){a&2&&ne(`id`,o._config.id||null)(`role`,o._config.role)(`aria-modal`,o._config.ariaModal)(`aria-labelledby`,o._config.ariaLabel?null:o._ariaLabelledByQueue[0])(`aria-label`,o._config.ariaLabel)(`aria-describedby`,o._config.ariaDescribedBy||null)},features:[Be],decls:1,vars:0,consts:[[`cdkPortalOutlet`,``]],template:function(a,o){a&1&&st(0,e,0,0,`ng-template`,0)},dependencies:[Ra],styles:[`.cdk-dialog-container {
  display: block;
  width: 100%;
  height: 100%;
  min-height: inherit;
  max-height: inherit;
}
`],encapsulation:2,changeDetection:1})})()}return t})();var Uo=class{overlayRef;config;componentInstance=null;componentRef=null;containerInstance;disableClose;closed=new V;backdropClick;keydownEvents;outsidePointerEvents;id;_detachSubscription;constructor(n,e){this.overlayRef=n,this.config=e,this.disableClose=e.disableClose,this.backdropClick=n.backdropClick(),this.keydownEvents=n.keydownEvents(),this.outsidePointerEvents=n.outsidePointerEvents(),this.id=e.id,this.keydownEvents.subscribe(i=>{i.keyCode===27&&!this.disableClose&&!Ct(i)&&(i.preventDefault(),this.close(void 0,{focusOrigin:`keyboard`}))}),this.backdropClick.subscribe(()=>{!this.disableClose&&this._canClose()?this.close(void 0,{focusOrigin:`mouse`}):this.containerInstance._recaptureFocus?.()}),this._detachSubscription=n.detachments().subscribe(()=>{e.closeOnOverlayDetachments!==!1&&this.close()})}close(n,e){if(this._canClose(n)){let i=this.closed;this.containerInstance._closeInteractionType=e?.focusOrigin||`program`,this._detachSubscription.unsubscribe(),this.overlayRef.dispose(),i.next(n),i.complete(),this.componentInstance=this.containerInstance=null}}updatePosition(){return this.overlayRef.updatePosition(),this}updateSize(n=``,e=``){return this.overlayRef.updateSize({width:n,height:e}),this}addPanelClass(n){return this.overlayRef.addPanelClass(n),this}removePanelClass(n){return this.overlayRef.removePanelClass(n),this}_canClose(n){let e=this.config;return!!this.containerInstance&&(!e.closePredicate||e.closePredicate(n,e,this.componentInstance))}};var EP=new N(`DialogScrollStrategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>as(t)}});var DP=new N(`DialogData`);var IP=new N(`DefaultDialogConfig`);function kP(t){let n=T(t),e=new te;return{valueSignal:n,get value(){return n()},change:e,ngOnDestroy(){e.complete()}}}var Hb=(()=>{class t{_injector=f(se);_defaultOptions=f(IP,{optional:!0});_parentDialog=f(t,{optional:!0,skipSelf:!0});_overlayContainer=f(mm);_idGenerator=f(je);_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new V;_afterOpenedAtThisLevel=new V;_ariaHiddenElements=new Map;_scrollStrategy=f(EP);get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}afterAllClosed=Xa(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(mt(void 0)));open(e,i){let a=this._defaultOptions||new ja;i=r(r({},a),i),i.id=i.id||this._idGenerator.getId(`cdk-dialog-`),i.id&&this.getDialogById(i.id);let o=this._getOverlayConfig(i),r$3=sa(this._injector,o),s=new Uo(r$3,i),l=this._attachContainer(r$3,s,i);if(s.containerInstance=l,!this.openDialogs.length){let h=this._overlayContainer.getContainerElement();l._focusTrapped?l._focusTrapped.pipe(vn(1)).subscribe(()=>{this._hideNonDialogContentFromAssistiveTechnology(h)}):this._hideNonDialogContentFromAssistiveTechnology(h)}return this._attachDialogContent(e,s,l,i),this.openDialogs.push(s),s.closed.subscribe(()=>this._removeOpenDialog(s,!0)),this.afterOpened.next(s),s}closeAll(){jb(this.openDialogs,e=>e.close())}getDialogById(e){return this.openDialogs.find(i=>i.id===e)}ngOnDestroy(){jb(this._openDialogsAtThisLevel,e=>{e.config.closeOnDestroy===!1&&this._removeOpenDialog(e,!1)}),jb(this._openDialogsAtThisLevel,e=>e.close()),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete(),this._openDialogsAtThisLevel=[]}_getOverlayConfig(e){let i=new aa({positionStrategy:e.positionStrategy||os().centerHorizontally().centerVertically(),scrollStrategy:e.scrollStrategy||this._scrollStrategy(),panelClass:e.panelClass,hasBackdrop:e.hasBackdrop,direction:e.direction,minWidth:e.minWidth,minHeight:e.minHeight,maxWidth:e.maxWidth,maxHeight:e.maxHeight,width:e.width,height:e.height,disposeOnNavigation:e.closeOnNavigation,disableAnimations:e.disableAnimations});return e.backdropClass&&(i.backdropClass=e.backdropClass),i}_attachContainer(e,i,a){let o=a.injector||a.viewContainerRef?.injector,r=[{provide:ja,useValue:a},{provide:Uo,useValue:i},{provide:ns,useValue:e}],s;a.container?typeof a.container==`function`?s=a.container:(s=a.container.type,r.push(...a.container.providers(a))):s=zb;let l=new Aa(s,a.viewContainerRef,se.create({parent:o||this._injector,providers:r}));return e.attach(l).instance}_attachDialogContent(e,i,a,o){if(e instanceof Vt){let r$4=this._createInjector(o,i,a,void 0),s={$implicit:o.data,dialogRef:i};o.templateContext&&(s=r(r({},s),typeof o.templateContext==`function`?o.templateContext():o.templateContext)),a.attachTemplatePortal(new Hn(e,null,s,r$4))}else{let r=this._createInjector(o,i,a,this._injector),s=a.attachComponentPortal(new Aa(e,o.viewContainerRef,r,null,o.bindings));i.componentRef=s,i.componentInstance=s.instance}}_createInjector(e,i,a,o){let r=e.injector||e.viewContainerRef?.injector,s=[{provide:DP,useValue:e.data},{provide:Uo,useValue:i}];return e.providers&&(typeof e.providers==`function`?s.push(...e.providers(i,e,a)):s.push(...e.providers)),e.direction&&(!r||!r.get(gt,null,{optional:!0}))&&s.push({provide:gt,useValue:kP(e.direction)}),se.create({parent:r||o,providers:s})}_removeOpenDialog(e,i){let a=this.openDialogs.indexOf(e);a>-1&&(this.openDialogs.splice(a,1),this.openDialogs.length||(this._ariaHiddenElements.forEach((o,r)=>{o?r.setAttribute(`aria-hidden`,o):r.removeAttribute(`aria-hidden`)}),this._ariaHiddenElements.clear(),i&&this._getAfterAllClosed().next()))}_hideNonDialogContentFromAssistiveTechnology(e){if(e.parentElement){let i=e.parentElement.children;for(let a=i.length-1;a>-1;a--){let o=i[a];o!==e&&o.nodeName!==`SCRIPT`&&o.nodeName!==`STYLE`&&!o.hasAttribute(`aria-live`)&&!o.hasAttribute(`popover`)&&(this._ariaHiddenElements.set(o,o.getAttribute(`aria-hidden`)),o.setAttribute(`aria-hidden`,`true`))}}}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function jb(t,n){let e=t.length;for(;e--;)n(t[e])}var qw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({providers:[Hb],imports:[mi,Bo,No,Bo]})}return t})();var Dm=class{viewContainerRef;injector;id;role=`dialog`;panelClass=``;hasBackdrop=!0;backdropClass=``;disableClose=!1;closePredicate;width=``;height=``;minWidth;minHeight;maxWidth;maxHeight;position;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus=`first-tabbable`;restoreFocus=!0;delayFocusTrap=!0;scrollStrategy;closeOnNavigation=!0;enterAnimationDuration;exitAnimationDuration;bindings};var Ub=`mdc-dialog--open`;var Yw=`mdc-dialog--opening`;var Kw=`mdc-dialog--closing`;var MP=150;var TP=75;var NP=(()=>{class t extends zb{_animationStateChanged=new te;_animationsEnabled=!Ue();_actionSectionCount=0;_hostElement=this._elementRef.nativeElement;_enterAnimationDuration=this._animationsEnabled?Xw(this._config.enterAnimationDuration)??MP:0;_exitAnimationDuration=this._animationsEnabled?Xw(this._config.exitAnimationDuration)??TP:0;_animationTimer=null;_contentAttached(){super._contentAttached(),this._startOpenAnimation()}_startOpenAnimation(){this._animationStateChanged.emit({state:`opening`,totalTime:this._enterAnimationDuration}),this._animationsEnabled?(this._hostElement.style.setProperty(Qw,`${this._enterAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add(Yw,Ub)),this._waitForAnimationToComplete(this._enterAnimationDuration,this._finishDialogOpen)):(this._hostElement.classList.add(Ub),Promise.resolve().then(()=>this._finishDialogOpen()))}_startExitAnimation(){this._animationStateChanged.emit({state:`closing`,totalTime:this._exitAnimationDuration}),this._hostElement.classList.remove(Ub),this._animationsEnabled?(this._hostElement.style.setProperty(Qw,`${this._exitAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add(Kw)),this._waitForAnimationToComplete(this._exitAnimationDuration,this._finishDialogClose)):Promise.resolve().then(()=>this._finishDialogClose())}_updateActionSectionCount(e){this._actionSectionCount+=e,this._changeDetectorRef.markForCheck()}_finishDialogOpen=()=>{this._clearAnimationClasses(),this._openAnimationDone(this._enterAnimationDuration)};_finishDialogClose=()=>{this._clearAnimationClasses(),this._animationStateChanged.emit({state:`closed`,totalTime:this._exitAnimationDuration})};_clearAnimationClasses(){this._hostElement.classList.remove(Yw,Kw)}_waitForAnimationToComplete(e,i){this._animationTimer!==null&&clearTimeout(this._animationTimer),this._animationTimer=setTimeout(i,e)}_requestAnimationFrame(e){this._ngZone.runOutsideAngular(()=>{typeof requestAnimationFrame==`function`?requestAnimationFrame(e):e()})}_captureInitialFocus(){this._config.delayFocusTrap||this._trapFocus()}_openAnimationDone(e){this._config.delayFocusTrap&&this._trapFocus(),this._animationStateChanged.next({state:`opened`,totalTime:e})}ngOnDestroy(){super.ngOnDestroy(),this._animationTimer!==null&&clearTimeout(this._animationTimer)}attachComponentPortal(e){let i=super.attachComponentPortal(e);return i.location.nativeElement.classList.add(`mat-mdc-dialog-component-host`),i}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵcmp=(function(){function e(i,a){}return H({type:t,selectors:[[`mat-dialog-container`]],hostAttrs:[`tabindex`,`-1`,1,`mat-mdc-dialog-container`,`mdc-dialog`],hostVars:10,hostBindings:function(a,o){a&2&&(qt(`id`,o._config.id),ne(`aria-modal`,o._config.ariaModal)(`role`,o._config.role)(`aria-labelledby`,o._config.ariaLabel?null:o._ariaLabelledByQueue[0])(`aria-label`,o._config.ariaLabel)(`aria-describedby`,o._config.ariaDescribedBy||null),G(`_mat-animation-noopable`,!o._animationsEnabled)(`mat-mdc-dialog-container-with-actions`,o._actionSectionCount>0))},features:[Be],decls:3,vars:0,consts:[[1,`mat-mdc-dialog-inner-container`,`mdc-dialog__container`],[1,`mat-mdc-dialog-surface`,`mdc-dialog__surface`],[`cdkPortalOutlet`,``]],template:function(a,o){a&1&&(c(0,`div`,0)(1,`div`,1),st(2,e,0,0,`ng-template`,2),d()())},dependencies:[Ra],styles:[`.mat-mdc-dialog-container {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var Qw=`--mat-dialog-transition-duration`;function Xw(t){return t==null?null:typeof t==`number`?t:t.endsWith(`ms`)?ri(t.substring(0,t.length-2)):t.endsWith(`s`)?ri(t.substring(0,t.length-1))*1e3:t===`0`?0:null}var Em=(function(t){return t[t.OPEN=0]=`OPEN`,t[t.CLOSING=1]=`CLOSING`,t[t.CLOSED=2]=`CLOSED`,t})(Em||{});var tc=class{_ref;_config;_containerInstance;componentInstance;componentRef=null;disableClose;id;_afterOpened=new Yn(1);_beforeClosed=new Yn(1);_result;_closeFallbackTimeout;_state=Em.OPEN;_closeInteractionType;constructor(n,e,i){this._ref=n,this._config=e,this._containerInstance=i,this.disableClose=e.disableClose,this.id=n.id,n.addPanelClass(`mat-mdc-dialog-panel`),i._animationStateChanged.pipe(Qe(a=>a.state===`opened`),vn(1)).subscribe(()=>{this._afterOpened.next(),this._afterOpened.complete()}),i._animationStateChanged.pipe(Qe(a=>a.state===`closed`),vn(1)).subscribe(()=>{clearTimeout(this._closeFallbackTimeout),this._finishDialogClose()}),n.overlayRef.detachments().subscribe(()=>{this._beforeClosed.next(this._result),this._beforeClosed.complete(),this._finishDialogClose()}),kt(this.backdropClick(),this.keydownEvents().pipe(Qe(a=>a.keyCode===27&&!this.disableClose&&!Ct(a)))).subscribe(a=>{this.disableClose||(a.preventDefault(),Zw(this,a.type===`keydown`?`keyboard`:`mouse`))})}close(n){let e=this._config.closePredicate;e&&!e(n,this._config,this.componentInstance)||(this._result=n,this._containerInstance._animationStateChanged.pipe(Qe(i=>i.state===`closing`),vn(1)).subscribe(i=>{this._beforeClosed.next(n),this._beforeClosed.complete(),this._ref.overlayRef.detachBackdrop(),this._closeFallbackTimeout=setTimeout(()=>this._finishDialogClose(),i.totalTime+100)}),this._state=Em.CLOSING,this._containerInstance._startExitAnimation())}afterOpened(){return this._afterOpened}afterClosed(){return this._ref.closed}beforeClosed(){return this._beforeClosed}backdropClick(){return this._ref.backdropClick}keydownEvents(){return this._ref.keydownEvents}updatePosition(n){let e=this._ref.config.positionStrategy;return n&&(n.left||n.right)?n.left?e.left(n.left):e.right(n.right):e.centerHorizontally(),n&&(n.top||n.bottom)?n.top?e.top(n.top):e.bottom(n.bottom):e.centerVertically(),this._ref.updatePosition(),this}updateSize(n=``,e=``){return this._ref.updateSize(n,e),this}addPanelClass(n){return this._ref.addPanelClass(n),this}removePanelClass(n){return this._ref.removePanelClass(n),this}getState(){return this._state}_finishDialogClose(){this._state=Em.CLOSED,this._ref.close(this._result,{focusOrigin:this._closeInteractionType}),this.componentInstance=null}};function Zw(t,n,e){return t._closeInteractionType=n,t.close(e)}var nc=new N(`MatMdcDialogData`);var AP=new N(`mat-mdc-dialog-default-options`);var RP=new N(`mat-mdc-dialog-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>as(t)}});var $o=(()=>{class t{_defaultOptions=f(AP,{optional:!0});_scrollStrategy=f(RP);_parentDialog=f(t,{optional:!0,skipSelf:!0});_idGenerator=f(je);_injector=f(se);_dialog=f(Hb);_animationsDisabled=Ue();_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new V;_afterOpenedAtThisLevel=new V;dialogConfigClass=Dm;_dialogRefConstructor;_dialogContainerType;_dialogDataToken;get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}afterAllClosed=Xa(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(mt(void 0)));constructor(){this._dialogRefConstructor=tc,this._dialogContainerType=NP,this._dialogDataToken=nc}open(e,i){let a;i=r(r({},this._defaultOptions||new Dm),i),i.id=i.id||this._idGenerator.getId(`mat-mdc-dialog-`),i.scrollStrategy=i.scrollStrategy||this._scrollStrategy();let o=this._dialog.open(e,s(r({},i),{positionStrategy:os(this._injector).centerHorizontally().centerVertically(),disableClose:!0,closePredicate:void 0,closeOnDestroy:!1,closeOnOverlayDetachments:!1,disableAnimations:this._animationsDisabled||i.enterAnimationDuration?.toLocaleString()===`0`||i.exitAnimationDuration?.toString()===`0`,container:{type:this._dialogContainerType,providers:()=>[{provide:this.dialogConfigClass,useValue:i},{provide:ja,useValue:i}]},templateContext:()=>({dialogRef:a}),providers:(r,s,l)=>(a=new this._dialogRefConstructor(r,i,l),a.updatePosition(i?.position),[{provide:this._dialogContainerType,useValue:l},{provide:this._dialogDataToken,useValue:s.data},{provide:this._dialogRefConstructor,useValue:a},{provide:Uo,useValue:null}])}));return a.componentRef=o.componentRef,a.componentInstance=o.componentInstance,this.openDialogs.push(a),this.afterOpened.next(a),a.afterClosed().subscribe(()=>{let r=this.openDialogs.indexOf(a);r>-1&&(this.openDialogs.splice(r,1),this.openDialogs.length||this._getAfterAllClosed().next())}),a}closeAll(){this._closeDialogs(this.openDialogs)}getDialogById(e){return this.openDialogs.find(i=>i.id===e)}ngOnDestroy(){this._closeDialogs(this._openDialogsAtThisLevel),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete()}_closeDialogs(e){let i=e.length;for(;i--;)e[i].close()}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Im=(()=>{class t{dialogRef=f(tc,{optional:!0});_elementRef=f(Y);_dialog=f($o);ariaLabel;type=`button`;dialogResult;_matDialogClose;ngOnInit(){this.dialogRef||(this.dialogRef=eE(this._elementRef,this._dialog.openDialogs))}ngOnChanges(e){let i=e._matDialogClose;i&&(this.dialogResult=i.currentValue)}_onButtonClick(e){this._elementRef.nativeElement.getAttribute(`aria-disabled`)!==`true`&&Zw(this.dialogRef,e.screenX===0&&e.screenY===0?`keyboard`:`mouse`,this.dialogResult)}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`mat-dialog-close`,``],[``,`matDialogClose`,``]],hostVars:2,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._onButtonClick(r)}),i&2&&ne(`aria-label`,a.ariaLabel||null)(`type`,a.type)},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],type:`type`,dialogResult:[0,`mat-dialog-close`,`dialogResult`],_matDialogClose:[0,`matDialogClose`,`_matDialogClose`]},exportAs:[`matDialogClose`],features:[_t]})}return t})();var Jw=(()=>{class t{_dialogRef=f(tc,{optional:!0});_elementRef=f(Y);_dialog=f($o);ngOnInit(){this._dialogRef||(this._dialogRef=eE(this._elementRef,this._dialog.openDialogs)),this._dialogRef&&Promise.resolve().then(()=>{this._onAdd()})}ngOnDestroy(){this._dialogRef?._containerInstance&&Promise.resolve().then(()=>{this._onRemove()})}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t})}return t})();var km=(()=>{class t extends Jw{id=f(je).getId(`mat-mdc-dialog-title-`);_onAdd(){this._dialogRef._containerInstance?._addAriaLabelledBy?.(this.id)}_onRemove(){this._dialogRef?._containerInstance?._removeAriaLabelledBy?.(this.id)}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`mat-dialog-title`,``],[``,`matDialogTitle`,``]],hostAttrs:[1,`mat-mdc-dialog-title`,`mdc-dialog__title`],hostVars:1,hostBindings:function(i,a){i&2&&qt(`id`,a.id)},inputs:{id:`id`},exportAs:[`matDialogTitle`],features:[Be]})}return t})();var Mm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`mat-dialog-content`,``],[`mat-dialog-content`],[``,`matDialogContent`,``]],hostAttrs:[1,`mat-mdc-dialog-content`,`mdc-dialog__content`],features:[rl([ql])]})}return t})();var Tm=(()=>{class t extends Jw{align;_onAdd(){this._dialogRef._containerInstance?._updateActionSectionCount?.(1)}_onRemove(){this._dialogRef._containerInstance?._updateActionSectionCount?.(-1)}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`mat-dialog-actions`,``],[`mat-dialog-actions`],[``,`matDialogActions`,``]],hostAttrs:[1,`mat-mdc-dialog-actions`,`mdc-dialog__actions`],hostVars:6,hostBindings:function(i,a){i&2&&G(`mat-mdc-dialog-actions-align-start`,a.align===`start`)(`mat-mdc-dialog-actions-align-center`,a.align===`center`)(`mat-mdc-dialog-actions-align-end`,a.align===`end`)},inputs:{align:`align`},features:[Be]})}return t})();function eE(t,n){let e=t.nativeElement.parentElement;for(;e&&!e.classList.contains(`mat-mdc-dialog-container`);)e=e.parentElement;return e?n.find(i=>i.id===e.id):null}var Nm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({providers:[$o],imports:[qw,mi,Bo,we]})}return t})();var Am=class t{http=f(Di);getStatus(){return wt(this.http.get(this.endpoint(`/status`)))}getComponentUsage(n){return wt(this.http.get(this.endpoint(`/components/usage`),{params:{scope:n}}))}getComponentCandidates(n){return wt(this.http.get(this.endpoint(`/components/candidates`),{params:r({scope:n.scope,basis:n.basis,offset:n.offset,limit:n.limit},n.selector)}))}getEvaluationChanges(n,e){return wt(this.http.get(this.endpoint(`/evaluations/changes`),{params:{after:n,limit:e}}))}getElites(){return wt(this.http.get(this.endpoint(`/elites`)))}getEliteHistory(){return wt(this.http.get(this.endpoint(`/elites/history`)))}getCandidate(n){return wt(this.http.get(this.endpoint(`/candidates/${encodeURIComponent(n)}`)))}getSearchSpace(){return wt(this.http.get(this.endpoint(`/search-space`)))}getArtifacts(){return wt(this.http.get(this.endpoint(`/artifacts`)))}artifactUrl(n){return this.endpoint(`/artifacts/${encodeURIComponent(n)}`)}endpoint(n){return`${In.apiBaseUrl}/api/autoconfig${n}`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Go=class{static evaluationCost(n){let e=[],i=[];for(let a of n){if(a.state!==`SUCCEEDED`||typeof a.cost!=`number`||!Number.isFinite(a.cost))continue;let o={x:a.id,y:a.cost,custom:{configurationId:a.configurationId,instanceName:a.instanceName,timeSeconds:a.timeSeconds}};(a.slow?i:e).push(o)}return{options:this.baseOptions(`Evaluation cost`,`Successful IRACE costs. Lower is better.`,{allowDecimals:!1,title:{text:`Evaluation ID`}},{title:{text:`IRACE cost`}},[{type:`scatter`,name:`Succeeded`,data:e,color:`#00aab5`,marker:{radius:2.5},turboThreshold:0},{type:`scatter`,name:`Slow`,data:i,color:`#ed8e00`,marker:{radius:3.5,symbol:`diamond`},turboThreshold:0}],`<b>{series.name}</b><br/>Evaluation: {point.x}<br/>Cost: {point.y}<br/>Configuration: {point.custom.configurationId}<br/>Instance: {point.custom.instanceName}`)}}static evaluationActivity(n){let e=n.filter(s=>s.finishedAt&&s.state!==`RUNNING`).slice().sort((s,l)=>{let h=Date.parse(s.finishedAt)-Date.parse(l.finishedAt);return h===0?s.id-l.id:h}),i={SUCCEEDED:0,REJECTED:0,FAILED:0},a=[],o=[],r=[];for(let s of e){let l=s.state;if(l!==`SUCCEEDED`&&l!==`REJECTED`&&l!==`FAILED`)continue;i[l]++;let h=[Date.parse(s.finishedAt),i[l]];l===`SUCCEEDED`?a.push(h):l===`REJECTED`?o.push(h):r.push(h)}return{options:this.baseOptions(`Evaluation activity`,`Cumulative terminal evaluations over wall-clock time.`,{type:`datetime`,title:{text:`Completion time`}},{allowDecimals:!1,min:0,title:{text:`Evaluations`}},[{type:`line`,name:`Succeeded`,data:a,color:`#079447`,marker:{enabled:!1}},{type:`line`,name:`Rejected`,data:o,color:`#ed8e00`,marker:{enabled:!1}},{type:`line`,name:`Failed`,data:r,color:`#d32f2f`,marker:{enabled:!1}}],`<b>{series.name}</b><br/>{point.x:%Y-%m-%d %H:%M:%S}<br/>Count: {point.y}`)}}static eliteRanks(n){let e=n?.iterations??[],i=[];for(let o of e)for(let r of o.elites)i.includes(r.configurationId)||i.push(r.configurationId);let a=[];for(let o of i){let r=[];for(let s of e){let l=null;for(let h of s.elites)if(h.configurationId===o){l=h.position;break}r.push([s.iteration,l])}a.push({type:`line`,name:o,data:r,connectNulls:!1,marker:{enabled:!0,radius:2.5},step:`left`})}return{options:this.baseOptions(`Elite rank evolution`,`Elite positions at completed IRACE race checkpoints.`,{allowDecimals:!1,title:{text:`Race checkpoint`}},{allowDecimals:!1,min:1,reversed:!0,title:{text:`Elite position`}},a,`<b>{series.name}</b><br/>Checkpoint: {point.x}<br/>Position: {point.y}`)}}static instanceSummaries(n){let e=new Map;for(let i of n){let a=e.get(i.instanceName)??{instanceName:i.instanceName,evaluations:0,bestCost:null,issues:0,slow:0},o=a.bestCost;i.state===`SUCCEEDED`&&typeof i.cost==`number`&&Number.isFinite(i.cost)&&(o===null||i.cost<o)&&(o=i.cost),e.set(i.instanceName,{instanceName:a.instanceName,evaluations:a.evaluations+1,bestCost:o,issues:a.issues+(i.state===`REJECTED`||i.state===`FAILED`?1:0),slow:a.slow+(i.slow?1:0)})}return[...e.values()].sort((i,a)=>a.issues-i.issues||i.instanceName.localeCompare(a.instanceName))}static baseOptions(n,e,i,a,o,r){return{accessibility:{enabled:!0,description:e},boost:{enabled:!0,useGPUTranslations:!0,seriesThreshold:20},chart:{animation:!1,height:330,spacing:[12,12,12,12]},credits:{enabled:!1},exporting:{enabled:!0},legend:{enabled:!0,maxHeight:72},title:{text:n,style:{display:`none`}},xAxis:i,yAxis:a,tooltip:{headerFormat:``,pointFormat:r},plotOptions:{series:{animation:!1,connectNulls:!1}},series:o}}};var OP=500;var PP=2e3;var FP=1e4;var LP=1e4;var $b=`mork.autoconfig.alertSeen.v1`;var za=class t{client=f(Am);destroyRef=f(Ae);statusSignal=T(null);componentUsageSignal=T(null);componentScopeSignal=T(`ALL`);componentErrorSignal=T(null);syncStateSignal=T(`detecting`);latestErrorSignal=T(null);evaluationsSignal=T([]);elitesSignal=T(null);eliteHistorySignal=T(null);searchSpaceSignal=T(null);artifactsSignal=T(null);alertsSignal=T([]);seenAlertRevisionSignal=T(0);runFailureSeenSignal=T(!1);lastUpdatedSignal=T(null);evaluationMap=new Map;candidateCache=new Map;pollTimer=null;currentRunId=null;evaluationRevision=0;eliteMarker=null;componentMarker=null;componentRequest=0;searchSpaceLoaded=!1;lastArtifactFetch=0;lastArtifactPhase=null;started=!1;destroyed=!1;status=this.statusSignal.asReadonly();componentUsage=this.componentUsageSignal.asReadonly();componentScope=this.componentScopeSignal.asReadonly();componentError=this.componentErrorSignal.asReadonly();syncState=this.syncStateSignal.asReadonly();latestError=this.latestErrorSignal.asReadonly();evaluations=this.evaluationsSignal.asReadonly();elites=this.elitesSignal.asReadonly();eliteHistory=this.eliteHistorySignal.asReadonly();searchSpace=this.searchSpaceSignal.asReadonly();artifacts=this.artifactsSignal.asReadonly();alerts=this.alertsSignal.asReadonly();lastUpdated=this.lastUpdatedSignal.asReadonly();viewMode=K(()=>{let n=this.statusSignal();return n?n.mode===`DISABLED`?`standard`:n.role===`WORKER`?`worker`:`tuning`:this.syncStateSignal()===`retrying`?`unavailable`:`detecting`});connectionLabel=K(()=>{switch(this.syncStateSignal()){case`detecting`:return`Detecting run mode`;case`synchronizing`:return`Synchronizing`;case`live`:return`Live`;case`retrying`:return`Reconnecting`}});budgetPercent=K(()=>{let n=this.statusSignal()?.budget;return!n||n.maximum<=0?0:Math.min(100,n.used/n.maximum*100)});evaluationCostChart=K(()=>Go.evaluationCost(this.evaluationsSignal()));evaluationActivityChart=K(()=>Go.evaluationActivity(this.evaluationsSignal()));eliteRankChart=K(()=>Go.eliteRanks(this.eliteHistorySignal()));instanceSummaries=K(()=>Go.instanceSummaries(this.evaluationsSignal()));recentEvaluations=K(()=>this.evaluationsSignal().slice(-100).reverse());unseenAlertCount=K(()=>{let n=0;for(let e of this.alertsSignal())e.revision===null?this.runFailureSeenSignal()||n++:e.revision>this.seenAlertRevisionSignal()&&n++;return n});constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.clearPollTimer()})}start(){this.started||(this.started=!0,this.schedulePoll(0))}retryNow(){this.clearPollTimer(),this.schedulePoll(0)}markAlertsSeen(){this.seenAlertRevisionSignal.set(this.evaluationRevision),this.statusSignal()?.failure&&this.runFailureSeenSignal.set(!0),this.persistSeenAlerts()}async loadCandidate(n){let e=this.candidateCache.get(n);if(e)return e;let i=this.currentRunId,a=await this.client.getCandidate(n);if(i!==this.currentRunId)throw new Error(`Candidate belongs to an obsolete autoconfig run`);return(this.statusSignal()?.state===`COMPLETED`||this.statusSignal()?.state===`FAILED`)&&this.candidateCache.set(n,a),a}selectComponentScope(n){if(n===this.componentScopeSignal())return;this.componentScopeSignal.set(n),this.componentUsageSignal.set(null),this.componentMarker=null;let e=this.statusSignal();e&&this.synchronizeComponentUsage(e)}loadComponentCandidates(n){return this.client.getComponentCandidates(n)}refreshComponentUsage(){this.componentMarker=null;let n=this.statusSignal();n&&this.synchronizeComponentUsage(n)}async refreshArtifacts(){let n=this.statusSignal();if(!n?.runId||n.role!==`COORDINATOR`){this.artifactsSignal.set(null);return}try{this.artifactsSignal.set(await this.client.getArtifacts()),this.lastArtifactFetch=Date.now()}catch(e){if(this.isNotFound(e)){this.artifactsSignal.set({runId:n.runId,artifacts:[]}),this.lastArtifactFetch=Date.now();return}throw e}}artifactUrl(n){return this.client.artifactUrl(n)}schedulePoll(n){this.destroyed||(this.pollTimer=setTimeout(()=>{this.poll()},n))}async poll(){this.pollTimer=null,this.syncStateSignal.set(`synchronizing`);try{let n=await this.client.getStatus();this.adoptStatus(n),n.mode!==`DISABLED`&&n.role===`COORDINATOR`&&n.runId&&await this.synchronizeRun(n),this.latestErrorSignal.set(null),this.lastUpdatedSignal.set(new Date),this.syncStateSignal.set(`live`)}catch(n){this.latestErrorSignal.set(this.errorMessage(n)),this.syncStateSignal.set(`retrying`)}finally{let n=this.statusSignal(),e=n?.state===`PREPARING`||n?.state===`RUNNING`;this.schedulePoll(e?PP:FP)}}adoptStatus(n){n.runId!==this.currentRunId?this.resetRun(n.runId):n.latestEvaluationRevision<this.evaluationRevision&&this.resetRun(n.runId),this.statusSignal.set(n),this.publishAlerts()}async synchronizeRun(n){await this.synchronizeEvaluations(n),await this.synchronizeElites(n),await this.synchronizeSearchSpace(n),await this.synchronizeComponentUsage(n),await this.synchronizeArtifacts(n)}async synchronizeEvaluations(n){let e=!1;for(;this.evaluationRevision<n.latestEvaluationRevision;){let i=await this.client.getEvaluationChanges(this.evaluationRevision,OP);if(i.runId!==n.runId)throw new Error(`Evaluation changes belong to a different autoconfig run`);for(let a of i.changes)this.evaluationMap.set(a.evaluation.id,{revision:a.revision,evaluation:a.evaluation}),e=!0;if(i.nextRevision<=this.evaluationRevision)break;this.evaluationRevision=i.nextRevision}if(e){let i=[...this.evaluationMap.values()].sort((a,o)=>a.evaluation.id-o.evaluation.id).map(a=>a.evaluation);this.evaluationsSignal.set(i),this.publishAlerts()}}async synchronizeElites(n){let e=`${n.irace.iteration??``}|${n.irace.updatedAt??``}|${n.irace.finalSnapshot}`;if(e===this.eliteMarker)return;let[i,a]=await Promise.all([this.client.getElites(),this.client.getEliteHistory()]);if(i.runId!==n.runId||a.runId!==n.runId)throw new Error(`Elite snapshots belong to a different autoconfig run`);this.elitesSignal.set(i),this.eliteHistorySignal.set(a),this.eliteMarker=e}async synchronizeSearchSpace(n){if(!(n.mode!==`AUTOCONFIG`||n.generatedParameterCount<=0||this.searchSpaceLoaded))try{this.searchSpaceSignal.set(await this.client.getSearchSpace()),this.searchSpaceLoaded=!0}catch(e){if(!this.isNotFound(e))throw e}}async synchronizeComponentUsage(n){if(n.mode!==`AUTOCONFIG`||n.role!==`COORDINATOR`||!n.runId)return;let e=this.componentScopeSignal(),i=`${n.runId}|${e}|${n.latestEvaluationRevision}|${n.irace.updatedAt??``}|${n.irace.finalSnapshot}`;if(i===this.componentMarker)return;let a=++this.componentRequest;try{let o=await this.client.getComponentUsage(e);if(this.destroyed||a!==this.componentRequest||e!==this.componentScopeSignal())return;if(o.runId!==this.currentRunId||o.scope!==e)throw new Error(`Component usage belongs to a different run or scope`);this.componentUsageSignal.set(o),this.componentMarker=i,this.componentErrorSignal.set(null)}catch(o){a===this.componentRequest&&!this.destroyed&&this.componentErrorSignal.set(this.errorMessage(o))}}async synchronizeArtifacts(n){let e=Date.now();n.phase===this.lastArtifactPhase&&e-this.lastArtifactFetch<LP||(await this.refreshArtifacts(),this.lastArtifactPhase=n.phase)}publishAlerts(){let n=[],e=this.statusSignal();e?.failure&&n.push({key:`run:${e.runId}:failure`,revision:null,severity:`critical`,title:`Tuning run failed`,message:e.failure.message??e.failure.type,timestamp:e.finishedAt,evaluationId:null});for(let i of this.evaluationMap.values()){let a=i.evaluation;if(a.state!==`FAILED`&&a.state!==`REJECTED`&&!a.slow)continue;let o=a.state===`FAILED`,r=a.state===`REJECTED`,s=o?`Evaluation failed`:r?`Evaluation rejected`:`Slow evaluation`,l=a.message??a.reasonCode??a.instanceName;n.push({key:`evaluation:${a.id}`,revision:i.revision,severity:o?`critical`:`warning`,title:s,message:a.slow&&r?`${l} \xB7 also slow`:l,timestamp:a.finishedAt,evaluationId:a.id})}n.sort((i,a)=>i.revision===null?-1:a.revision===null?1:a.revision-i.revision),this.alertsSignal.set(n)}resetRun(n){this.currentRunId=n,this.evaluationRevision=0,this.evaluationMap.clear(),this.candidateCache.clear(),this.componentRequest++,this.componentUsageSignal.set(null),this.componentScopeSignal.set(`ALL`),this.componentErrorSignal.set(null),this.componentMarker=null,this.evaluationsSignal.set([]),this.elitesSignal.set(null),this.eliteHistorySignal.set(null),this.searchSpaceSignal.set(null),this.artifactsSignal.set(null),this.alertsSignal.set([]),this.eliteMarker=null,this.searchSpaceLoaded=!1,this.lastArtifactFetch=0,this.lastArtifactPhase=null,this.restoreSeenAlerts(n)}restoreSeenAlerts(n){if(this.seenAlertRevisionSignal.set(0),this.runFailureSeenSignal.set(!1),!!n)try{let e=localStorage.getItem($b);if(!e)return;let i=JSON.parse(e);i.runId===n&&(this.seenAlertRevisionSignal.set(typeof i.revision==`number`?i.revision:0),this.runFailureSeenSignal.set(i.runFailureSeen===!0))}catch{localStorage.removeItem($b)}}persistSeenAlerts(){if(!this.currentRunId)return;let n={runId:this.currentRunId,revision:this.seenAlertRevisionSignal(),runFailureSeen:this.runFailureSeenSignal()};localStorage.setItem($b,JSON.stringify(n))}clearPollTimer(){this.pollTimer&&(clearTimeout(this.pollTimer),this.pollTimer=null)}isNotFound(n){return n instanceof Ei&&n.status===404}errorMessage(n){return n instanceof Ei?`Backend request failed (${n.status||`network error`})`:n instanceof Error?n.message:String(n)}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var VP=(t,n)=>n[0];function BP(t,n){if(t&1&&(c(0,`dt`),u(1),d(),c(2,`dd`),u(3),d()),t&2){let e=n.$implicit;m(),w(e[0]),m(2),w(e[1])}}function jP(t,n){t&1&&(c(0,`dd`),u(1,`No parameters were reported.`),d())}function zP(t,n){if(t&1&&(c(0,`h3`),u(1,`Decoded algorithm`),d(),c(2,`pre`),u(3),j(4,`json`),d()),t&2){let e=v();m(3),w(_e(4,1,e.algorithm))}}function HP(t,n){if(t&1&&(c(0,`p`,1),u(1),d()),t&2){let e=v();m(),w(e.decodeError)}}function UP(t,n){if(t&1&&(c(0,`section`,4)(1,`span`)(2,`strong`),u(3),d(),u(4,` succeeded`),d(),c(5,`span`)(6,`strong`),u(7),d(),u(8,` running`),d(),c(9,`span`)(10,`strong`),u(11),d(),u(12,` rejected`),d(),c(13,`span`)(14,`strong`),u(15),d(),u(16,` failed`),d(),c(17,`span`)(18,`strong`),u(19),d(),u(20,` slow`),d()(),c(21,`h3`),u(22,`Parameters`),d(),c(23,`dl`),de(24,BP,4,2,null,null,VP,!1,jP,2,0,`dd`),d(),E(27,zP,5,3),E(28,HP,2,1,`p`,1)),t&2){let e=n,i=v();m(3),w(e.evaluations.succeeded),m(4),w(e.evaluations.running),m(4),w(e.evaluations.rejected),m(4),w(e.evaluations.failed),m(4),w(e.evaluations.slow),m(5),ue(i.parameterEntries(e)),m(3),D(e.algorithm?27:-1),m(),D(e.decodeError?28:-1)}}function $P(t,n){if(t&1&&(c(0,`p`,1),u(1),d()),t&2){let e=v();m(),w(e.error())}}function GP(t,n){t&1&&(c(0,`p`),u(1,`Loading configuration…`),d())}var fs=class t{store=f(za);configurationId=f(nc);candidate=T(null);error=T(null);ngOnInit(){this.store.loadCandidate(this.configurationId).then(n=>this.candidate.set(n)).catch(n=>this.error.set(n instanceof Error?n.message:String(n)))}parameterEntries(n){return Object.entries(n.parameters)}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-candidate-dialog`]],decls:9,vars:2,consts:[[`mat-dialog-title`,``],[`role`,`alert`,1,`error`],[`align`,`end`],[`mat-button`,``,`mat-dialog-close`,``,`type`,`button`],[`aria-label`,`Candidate evaluation counts`,1,`counts`]],template:function(e,i){if(e&1&&(c(0,`h2`,0),u(1),d(),c(2,`mat-dialog-content`),E(3,UP,29,8)(4,$P,2,1,`p`,1)(5,GP,2,0,`p`),d(),c(6,`mat-dialog-actions`,2)(7,`button`,3),u(8,`Close`),d()()),e&2){let a;m(),B(`Configuration `,i.configurationId),m(2),D((a=i.candidate())?3:i.error()?4:5,a)}},dependencies:[Ft,nn,Nm,Im,km,Tm,Mm,Do],styles:[`mat-dialog-content[_ngcontent-%COMP%]{min-width:min(42rem,80vw)}.counts[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.5rem 1.25rem;margin-bottom:1.25rem}dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(10rem,1fr) minmax(8rem,1fr);gap:.35rem 1rem}dt[_ngcontent-%COMP%]{overflow-wrap:anywhere;color:var(--%NS%mat-sys-on-surface-variant)}dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}pre[_ngcontent-%COMP%]{max-height:22rem;overflow:auto;padding:1rem;border-radius:.5rem;background:var(--%NS%mat-sys-surface-container);white-space:pre-wrap}.error[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}`]})};var WP=()=>[25,50,100];var qP=(t,n)=>n.id;function YP(t,n){t&1&&(c(0,`span`,17),u(1,`slow`),d())}function KP(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`)(4,`button`,14),C(`click`,function(){let a=k(e).$implicit;return M(v().candidateSelected.emit(a.configurationId))}),u(5),d()(),c(6,`td`,15),u(7),d(),c(8,`td`)(9,`span`,16),u(10),d()(),c(11,`td`),u(12),j(13,`number`),d(),c(14,`td`),u(15),j(16,`number`),E(17,YP,2,0,`span`,17),d(),c(18,`td`),u(19),j(20,`date`),d(),c(21,`td`,18),u(22),d()()}if(t&2){let e=n.$implicit,i=v();G(`issue`,e.state===`FAILED`||e.state===`REJECTED`),m(2),w(e.id),m(3),B(` `,e.configurationId,` `),m(),S(`title`,e.instanceName),m(),w(e.instanceName),m(2),tt(i.stateClass(e.state)),m(),w(e.state),m(2),w(e.cost===null?`—`:ke(13,14,e.cost,`1.0-5`)),m(3),B(` `,e.timeSeconds===null?`—`:ke(16,17,e.timeSeconds,`1.2-2`)+` s`,` `),m(2),D(e.slow?17:-1),m(2),B(` `,e.finishedAt?ke(20,20,e.finishedAt,`HH:mm:ss`):`Running`,` `),m(3),w(e.message??e.reasonCode??`—`)}}function QP(t,n){t&1&&(c(0,`tr`)(1,`td`,19),u(2,`No evaluations match these filters.`),d()())}var Rm=class t{evaluations=zt.required();forceIssues=zt(!1);candidateSelected=au();search=T(``);state=T(`ALL`);slowOnly=T(!1);issueOnly=T(!1);pageIndex=T(0);pageSize=T(25);sortColumn=T(`id`);sortAscending=T(!1);filtered=K(()=>{let n=this.search().trim().toLowerCase(),e=this.state(),i=this.slowOnly(),a=this.issueOnly()||this.forceIssues(),o=[];for(let l of this.evaluations())e!==`ALL`&&l.state!==e||i&&!l.slow||a&&l.state!==`FAILED`&&l.state!==`REJECTED`&&!l.slow||n&&!l.configurationId.toLowerCase().includes(n)&&!l.instanceName.toLowerCase().includes(n)&&!String(l.id).includes(n)||o.push(l);let r=this.sortColumn(),s=this.sortAscending()?1:-1;return o.sort((l,h)=>this.compare(l[r],h[r])*s),o});page=K(()=>{let n=this.pageIndex()*this.pageSize();return this.filtered().slice(n,n+this.pageSize())});updateSearch(n){this.search.set(n),this.pageIndex.set(0)}updateState(n){this.state.set(n),this.pageIndex.set(0)}updateSlowOnly(n){this.slowOnly.set(n),this.pageIndex.set(0)}updateIssueOnly(n){this.issueOnly.set(n),this.pageIndex.set(0)}updatePage(n){this.pageIndex.set(n.pageIndex),this.pageSize.set(n.pageSize)}sortBy(n){this.sortColumn()===n?this.sortAscending.update(e=>!e):(this.sortColumn.set(n),this.sortAscending.set(!0)),this.pageIndex.set(0)}sortIndicator(n){return this.sortColumn()!==n?``:this.sortAscending()?` ↑`:` ↓`}stateClass(n){return`state-${n.toLowerCase()}`}compare(n,e){return n===e?0:n===null?1:e===null?-1:typeof n==`number`&&typeof e==`number`?n-e:String(n).localeCompare(String(e),void 0,{numeric:!0})}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-evaluation-table`]],inputs:{evaluations:[1,`evaluations`],forceIssues:[1,`forceIssues`]},outputs:{candidateSelected:`candidateSelected`},decls:54,vars:17,consts:[[`aria-label`,`Evaluation filters`,1,`filters`],[`appearance`,`outline`,`subscriptSizing`,`dynamic`],[`matInput`,``,`type`,`search`,`placeholder`,`ID, configuration, or instance`,3,`ngModelChange`,`ngModel`],[3,`ngModelChange`,`ngModel`],[`value`,`ALL`],[`value`,`RUNNING`],[`value`,`SUCCEEDED`],[`value`,`REJECTED`],[`value`,`FAILED`],[3,`ngModelChange`,`ngModel`,`disabled`],[1,`table-scroll`],[`type`,`button`,3,`click`],[3,`issue`],[`aria-label`,`Evaluation pages`,3,`page`,`length`,`pageIndex`,`pageSize`,`pageSizeOptions`],[`type`,`button`,1,`candidate-link`,3,`click`],[1,`instance`,3,`title`],[1,`state`],[1,`slow`],[1,`details`],[`colspan`,`8`,1,`empty`]],template:function(e,i){e&1&&(c(0,`section`,0)(1,`mat-form-field`,1)(2,`mat-label`),u(3,`Search`),d(),c(4,`input`,2),Dr(),C(`ngModelChange`,function(o){return i.updateSearch(o)}),d()(),c(5,`mat-form-field`,1)(6,`mat-label`),u(7,`State`),d(),c(8,`mat-select`,3),Dr(),C(`ngModelChange`,function(o){return i.updateState(o)}),c(9,`mat-option`,4),u(10,`All states`),d(),c(11,`mat-option`,5),u(12,`Running`),d(),c(13,`mat-option`,6),u(14,`Succeeded`),d(),c(15,`mat-option`,7),u(16,`Rejected`),d(),c(17,`mat-option`,8),u(18,`Failed`),d()()(),c(19,`mat-checkbox`,3),Dr(),C(`ngModelChange`,function(o){return i.updateSlowOnly(o)}),u(20,` Slow only `),d(),c(21,`mat-checkbox`,9),Dr(),C(`ngModelChange`,function(o){return i.updateIssueOnly(o)}),u(22,` Issues only `),d()(),c(23,`div`,10)(24,`table`)(25,`thead`)(26,`tr`)(27,`th`)(28,`button`,11),C(`click`,function(){return i.sortBy(`id`)}),u(29),d()(),c(30,`th`)(31,`button`,11),C(`click`,function(){return i.sortBy(`configurationId`)}),u(32),d()(),c(33,`th`)(34,`button`,11),C(`click`,function(){return i.sortBy(`instanceName`)}),u(35),d()(),c(36,`th`)(37,`button`,11),C(`click`,function(){return i.sortBy(`state`)}),u(38),d()(),c(39,`th`)(40,`button`,11),C(`click`,function(){return i.sortBy(`cost`)}),u(41),d()(),c(42,`th`)(43,`button`,11),C(`click`,function(){return i.sortBy(`timeSeconds`)}),u(44),d()(),c(45,`th`),u(46,`Finished`),d(),c(47,`th`),u(48,`Details`),d()()(),c(49,`tbody`),de(50,KP,23,23,`tr`,12,qP,!1,QP,3,0,`tr`),d()()(),c(53,`mat-paginator`,13),C(`page`,function(o){return i.updatePage(o)}),d()),e&2&&(m(4),S(`ngModel`,i.search()),Ir(),m(4),S(`ngModel`,i.state()),Ir(),m(11),S(`ngModel`,i.slowOnly()),Ir(),m(2),S(`ngModel`,i.issueOnly()||i.forceIssues())(`disabled`,i.forceIssues()),Ir(),m(8),B(`ID`,i.sortIndicator(`id`)),m(3),B(` Configuration`,i.sortIndicator(`configurationId`),` `),m(3),B(` Instance`,i.sortIndicator(`instanceName`),` `),m(3),B(`State`,i.sortIndicator(`state`)),m(3),B(`Cost`,i.sortIndicator(`cost`)),m(3),B(` Time`,i.sortIndicator(`timeSeconds`),` `),m(6),ue(i.page()),m(3),S(`length`,i.filtered().length)(`pageIndex`,i.pageIndex())(`pageSize`,i.pageSize())(`pageSizeOptions`,nt(16,WP)))},dependencies:[BS,Xu,LS,cb,Ft,om,$l,di,Ti,na,es,Jr,gm,Ql,Fa,Pa,Un,Xi,En],styles:[`[_nghost-%COMP%]{display:block;min-width:0}.filters[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem 1rem;align-items:center;margin-bottom:1rem}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]:first-child{flex:1 1 18rem}.table-scroll[_ngcontent-%COMP%]{overflow:auto}table[_ngcontent-%COMP%]{width:100%;min-width:70rem;border-collapse:collapse;font:var(--%NS%mat-sys-body-small)}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.65rem .55rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left;vertical-align:middle}th[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-weight:650}th[_ngcontent-%COMP%]   button[_ngcontent-%COMP%], .candidate-link[_ngcontent-%COMP%]{padding:0;border:0;color:inherit;background:transparent;cursor:pointer;font-weight:inherit}.candidate-link[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-primary);text-decoration:underline;text-underline-offset:.15rem}.instance[_ngcontent-%COMP%], .details[_ngcontent-%COMP%]{max-width:18rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.state[_ngcontent-%COMP%]{display:inline-block;padding:.15rem .45rem;border-radius:999px;background:var(--%NS%mat-sys-surface-container-high);font:var(--%NS%mat-sys-label-small)}.state-succeeded[_ngcontent-%COMP%]{color:#08743a;background:#d9f5e4}.state-rejected[_ngcontent-%COMP%], .slow[_ngcontent-%COMP%]{color:#835100;background:#fff0c6}.state-failed[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-error-container);background:var(--%NS%mat-sys-error-container)}.slow[_ngcontent-%COMP%]{margin-left:.25rem;padding:.1rem .3rem;border-radius:.25rem;font:var(--%NS%mat-sys-label-small)}tr.issue[_ngcontent-%COMP%]{background:color-mix(in srgb,var(--%NS%mat-sys-error-container) 22%,transparent)}.empty[_ngcontent-%COMP%]{padding:2.5rem;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}mat-paginator[_ngcontent-%COMP%]{background:transparent}`]})};var nE=(t,n)=>n.name;function XP(t,n){if(t&1&&(c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),d()()),t&2){let e=n.$implicit,i=v(2);m(2),w(e.name),m(2),w(e.kind),m(2),w(i.domain(e))}}function ZP(t,n){if(t&1&&(c(0,`details`)(1,`summary`),u(2),d(),c(3,`table`)(4,`thead`)(5,`tr`)(6,`th`),u(7,`Name`),d(),c(8,`th`),u(9,`Kind`),d(),c(10,`th`),u(11,`Domain`),d()()(),c(12,`tbody`),de(13,XP,7,3,`tr`,null,nE),d()()()),t&2){let e=n.$implicit;m(2),Me(``,e.name,` · `,e.parameters.length,` parameters`),m(11),ue(e.parameters)}}var Om=class t{data=f(nc);domain(n){return n.choices.length>0?n.choices.join(`, `):n.values.length>0?n.values.join(`, `):n.minimum!==null||n.maximum!==null?`${String(n.minimum)} \u2013 ${String(n.maximum)}`:`Provided by Mork`}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-search-space-dialog`]],decls:30,vars:5,consts:[[`mat-dialog-title`,``],[1,`summary`],[1,`components`],[`align`,`end`],[`mat-button`,``,`mat-dialog-close`,``,`type`,`button`]],template:function(e,i){e&1&&(c(0,`h2`,0),u(1,`Generated search space`),d(),c(2,`mat-dialog-content`)(3,`section`,1)(4,`span`)(5,`strong`),u(6),d(),u(7,` roots`),d(),c(8,`span`)(9,`strong`),u(10),d(),u(11,` components`),d(),c(12,`span`)(13,`strong`),u(14),d(),u(15,` IRACE parameters`),d(),c(16,`span`)(17,`strong`),u(18),d(),u(19,` constraints`),d()(),c(20,`p`)(21,`strong`),u(22,`Root choices:`),d(),u(23),d(),c(24,`section`,2),de(25,ZP,15,2,`details`,null,nE),d()(),c(27,`mat-dialog-actions`,3)(28,`button`,4),u(29,`Close`),d()()),e&2&&(m(6),w(i.data.summary.rootCount),m(4),w(i.data.summary.componentCount),m(4),w(i.data.summary.generatedIraceParameterCount),m(4),w(i.data.summary.generatedForbiddenConstraintCount),m(5),B(` `,i.data.roots.join(`, `)),m(2),ue(i.data.components))},dependencies:[Ft,nn,Nm,Im,km,Tm,Mm],styles:[`mat-dialog-content[_ngcontent-%COMP%]{width:min(60rem,84vw)}.summary[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem 2rem;margin-bottom:1rem}.components[_ngcontent-%COMP%]{display:grid;gap:.5rem}details[_ngcontent-%COMP%]{padding:.75rem;border:1px solid var(--%NS%mat-sys-outline-variant);border-radius:.5rem}summary[_ngcontent-%COMP%]{cursor:pointer;font-weight:650}table[_ngcontent-%COMP%]{width:100%;margin-top:.75rem;border-collapse:collapse}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.45rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left;overflow-wrap:anywhere}`]})};var iE=new N(`MAT_BUTTON_TOGGLE_DEFAULT_OPTIONS`,{providedIn:`root`,factory:()=>({hideSingleSelectionIndicator:!1,hideMultipleSelectionIndicator:!1,disabledInteractive:!1})});var aE=new N(`MatButtonToggleGroup`);var JP={provide:Kr,useExisting:Ht(()=>Gb),multi:!0};var Pm=class{source;value;constructor(n,e){this.source=n,this.value=e}};var Gb=(()=>{class t{_changeDetector=f(Le);_dir=f(gt,{optional:!0});_multiple=!1;_disabled=!1;_disabledInteractive=!1;_selectionModel;_rawValue;_controlValueAccessorChangeFn=()=>{};_onTouched=()=>{};_buttonToggles;appearance;get name(){return this._name}set name(e){this._name=e,this._markButtonsForCheck()}_name=f(je).getId(`mat-button-toggle-group-`);vertical=!1;get value(){let e=this._selectionModel?this._selectionModel.selected:[];return this.multiple?e.map(i=>i.value):e[0]?e[0].value:void 0}set value(e){this._setSelectionByValue(e),this.valueChange.emit(this.value)}valueChange=new te;get selected(){let e=this._selectionModel?this._selectionModel.selected:[];return this.multiple?e:e[0]||null}get multiple(){return this._multiple}set multiple(e){this._multiple=e,this._markButtonsForCheck()}get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._markButtonsForCheck()}get disabledInteractive(){return this._disabledInteractive}set disabledInteractive(e){this._disabledInteractive=e,this._markButtonsForCheck()}get dir(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}change=new te;get hideSingleSelectionIndicator(){return this._hideSingleSelectionIndicator}set hideSingleSelectionIndicator(e){this._hideSingleSelectionIndicator=e,this._markButtonsForCheck()}_hideSingleSelectionIndicator;get hideMultipleSelectionIndicator(){return this._hideMultipleSelectionIndicator}set hideMultipleSelectionIndicator(e){this._hideMultipleSelectionIndicator=e,this._markButtonsForCheck()}_hideMultipleSelectionIndicator;constructor(){let e=f(iE,{optional:!0});this.appearance=e&&e.appearance?e.appearance:`standard`,this._hideSingleSelectionIndicator=e?.hideSingleSelectionIndicator??!1,this._hideMultipleSelectionIndicator=e?.hideMultipleSelectionIndicator??!1}ngOnInit(){this._selectionModel=new Ho(this.multiple,void 0,!1)}ngAfterContentInit(){this._selectionModel.select(...this._buttonToggles.filter(e=>e.checked)),this.multiple||this._initializeTabIndex()}writeValue(e){this.value=e,this._changeDetector.markForCheck()}registerOnChange(e){this._controlValueAccessorChangeFn=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e}_keydown(e){if(this.multiple||this.disabled||Ct(e))return;let a=e.target.id,o=this._buttonToggles.toArray().findIndex(s=>s.buttonId===a),r=null;switch(e.keyCode){case 32:case 13:r=this._buttonToggles.get(o)||null;break;case 38:r=this._getNextButton(o,-1);break;case 37:r=this._getNextButton(o,this.dir===`ltr`?-1:1);break;case 40:r=this._getNextButton(o,1);break;case 39:r=this._getNextButton(o,this.dir===`ltr`?1:-1);break;default:return}r&&(e.preventDefault(),r._onButtonClick(),r.focus())}_emitChangeEvent(e){let i=new Pm(e,this.value);this._rawValue=i.value,this._controlValueAccessorChangeFn(i.value),this.change.emit(i)}_syncButtonToggle(e,i,a=!1,o=!1){!this.multiple&&this.selected&&!e.checked&&(this.selected.checked=!1),this._selectionModel?i?this._selectionModel.select(e):this._selectionModel.deselect(e):o=!0,o?Promise.resolve().then(()=>this._updateModelValue(e,a)):this._updateModelValue(e,a)}_isSelected(e){return this._selectionModel&&this._selectionModel.isSelected(e)}_isPrechecked(e){return typeof this._rawValue>`u`?!1:this.multiple&&Array.isArray(this._rawValue)?this._rawValue.some(i=>e.value!=null&&i===e.value):e.value===this._rawValue}_initializeTabIndex(){if(this._buttonToggles.forEach(e=>{e.tabIndex=-1}),this.selected)this.selected.tabIndex=0;else for(let e=0;e<this._buttonToggles.length;e++){let i=this._buttonToggles.get(e);if(!i.disabled){i.tabIndex=0;break}}}_getNextButton(e,i){let a=this._buttonToggles;for(let o=1;o<=a.length;o++){let r=(e+i*o+a.length)%a.length,s=a.get(r);if(s&&!s.disabled)return s}return null}_setSelectionByValue(e){if(this._rawValue=e,!this._buttonToggles)return;let i=this._buttonToggles.toArray();if(this.multiple&&e?(this._clearSelection(),e.forEach(a=>this._selectValue(a,i))):(this._clearSelection(),this._selectValue(e,i)),!this.multiple&&i.every(a=>a.tabIndex===-1)){for(let a of i)if(!a.disabled){a.tabIndex=0;break}}}_clearSelection(){this._selectionModel.clear(),this._buttonToggles.forEach(e=>{e.checked=!1,this.multiple||(e.tabIndex=-1)})}_selectValue(e,i){for(let a of i)if(a.value===e){a.checked=!0,this._selectionModel.select(a),this.multiple||(a.tabIndex=0);break}}_updateModelValue(e,i){i&&this._emitChangeEvent(e),this.valueChange.emit(this.value)}_markButtonsForCheck(){this._buttonToggles?.forEach(e=>e._markForCheck())}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-button-toggle-group`]],contentQueries:function(i,a,o){if(i&1&&jt(o,Fm,5),i&2){let r;J(r=ee())&&(a._buttonToggles=r)}},hostAttrs:[1,`mat-button-toggle-group`],hostVars:6,hostBindings:function(i,a){i&1&&C(`keydown`,function(r){return a._keydown(r)}),i&2&&(ne(`role`,a.multiple?`group`:`radiogroup`)(`aria-disabled`,a.disabled),G(`mat-button-toggle-vertical`,a.vertical)(`mat-button-toggle-group-appearance-standard`,a.appearance===`standard`))},inputs:{appearance:`appearance`,name:`name`,vertical:[2,`vertical`,`vertical`,z],value:`value`,multiple:[2,`multiple`,`multiple`,z],disabled:[2,`disabled`,`disabled`,z],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z],hideSingleSelectionIndicator:[2,`hideSingleSelectionIndicator`,`hideSingleSelectionIndicator`,z],hideMultipleSelectionIndicator:[2,`hideMultipleSelectionIndicator`,`hideMultipleSelectionIndicator`,z]},outputs:{valueChange:`valueChange`,change:`change`},exportAs:[`matButtonToggleGroup`],features:[Ge([JP,{provide:aE,useExisting:t}])]})}return t})();var Fm=(()=>{class t{_changeDetectorRef=f(Le);_elementRef=f(Y);_focusMonitor=f(tn);_idGenerator=f(je);_animationDisabled=Ue();_checked=!1;ariaLabel;ariaLabelledby=null;_buttonElement;buttonToggleGroup;get buttonId(){return`${this.id}-button`}id;name;value;get tabIndex(){return this._tabIndex()}set tabIndex(e){this._tabIndex.set(e)}_tabIndex;disableRipple=!1;get appearance(){return this.buttonToggleGroup?this.buttonToggleGroup.appearance:this._appearance}set appearance(e){this._appearance=e}_appearance;get checked(){return this.buttonToggleGroup?this.buttonToggleGroup._isSelected(this):this._checked}set checked(e){e!==this._checked&&(this._checked=e,this.buttonToggleGroup&&this.buttonToggleGroup._syncButtonToggle(this,this._checked),this._changeDetectorRef.markForCheck())}get disabled(){return this._disabled||this.buttonToggleGroup&&this.buttonToggleGroup.disabled}set disabled(e){this._disabled=e}_disabled=!1;get disabledInteractive(){return this._disabledInteractive||this.buttonToggleGroup!==null&&this.buttonToggleGroup.disabledInteractive}set disabledInteractive(e){this._disabledInteractive=e}_disabledInteractive;change=new te;constructor(){f(Je).load(pn);let e=f(aE,{optional:!0}),i=f(new Ki(`tabindex`),{optional:!0})||``,a=f(iE,{optional:!0});this._tabIndex=T(parseInt(i)||0),this.buttonToggleGroup=e,this._appearance=a&&a.appearance?a.appearance:`standard`,this._disabledInteractive=a?.disabledInteractive??!1}ngOnInit(){let e=this.buttonToggleGroup;this.id=this.id||this._idGenerator.getId(`mat-button-toggle-`),e&&(e._isPrechecked(this)?this.checked=!0:e._isSelected(this)!==this._checked&&e._syncButtonToggle(this,this._checked))}ngAfterViewInit(){this._animationDisabled||this._elementRef.nativeElement.classList.add(`mat-button-toggle-animations-enabled`),this._focusMonitor.monitor(this._elementRef,!0)}ngOnDestroy(){let e=this.buttonToggleGroup;this._focusMonitor.stopMonitoring(this._elementRef),e&&e._isSelected(this)&&e._syncButtonToggle(this,!1,!1,!0)}focus(e){this._buttonElement.nativeElement.focus(e)}_onButtonClick(){if(this.disabled)return;let e=this.isSingleSelector()?!0:!this._checked;if(e!==this._checked&&(this._checked=e,this.buttonToggleGroup&&(this.buttonToggleGroup._syncButtonToggle(this,this._checked,!0),this.buttonToggleGroup._onTouched())),this.isSingleSelector()){let i=this.buttonToggleGroup._buttonToggles.find(a=>a.tabIndex===0);i&&(i.tabIndex=-1),this.tabIndex=0}this.change.emit(new Pm(this,this.value))}_markForCheck(){this._changeDetectorRef.markForCheck()}_getButtonName(){return this.isSingleSelector()?this.buttonToggleGroup.name:this.name||null}isSingleSelector(){return this.buttonToggleGroup&&!this.buttonToggleGroup.multiple}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`button`],i=[`*`];function a(o,r){if(o&1&&(c(0,`div`,2),U(1,`mat-pseudo-checkbox`,6),d()),o&2){let s=v();m(),S(`disabled`,s.disabled)}}return H({type:t,selectors:[[`mat-button-toggle`]],viewQuery:function(r,s){if(r&1&&Ze(e,5),r&2){let l;J(l=ee())&&(s._buttonElement=l.first)}},hostAttrs:[`role`,`presentation`,1,`mat-button-toggle`],hostVars:14,hostBindings:function(r,s){r&1&&C(`focus`,function(){return s.focus()}),r&2&&(ne(`aria-label`,null)(`aria-labelledby`,null)(`id`,s.id)(`name`,null),G(`mat-button-toggle-standalone`,!s.buttonToggleGroup)(`mat-button-toggle-checked`,s.checked)(`mat-button-toggle-disabled`,s.disabled)(`mat-button-toggle-disabled-interactive`,s.disabledInteractive)(`mat-button-toggle-appearance-standard`,s.appearance===`standard`))},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],id:`id`,name:`name`,value:`value`,tabIndex:`tabIndex`,disableRipple:[2,`disableRipple`,`disableRipple`,z],appearance:`appearance`,checked:[2,`checked`,`checked`,z],disabled:[2,`disabled`,`disabled`,z],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z]},outputs:{change:`change`},exportAs:[`matButtonToggle`],ngContentSelectors:i,decls:7,vars:13,consts:[[`button`,``],[`type`,`button`,1,`mat-button-toggle-button`,`mat-focus-indicator`,3,`click`,`id`,`disabled`],[1,`mat-button-toggle-checkbox-wrapper`],[1,`mat-button-toggle-label-content`],[1,`mat-button-toggle-focus-overlay`],[`matRipple`,``,1,`mat-button-toggle-ripple`,3,`matRippleTrigger`,`matRippleDisabled`],[`state`,`checked`,`aria-hidden`,`true`,`appearance`,`minimal`,3,`disabled`]],template:function(r,s){if(r&1&&(Fe(),c(0,`button`,1,0),C(`click`,function(){return s._onButtonClick()}),E(2,a,2,1,`div`,2),c(3,`span`,3),he(4),d()(),U(5,`span`,4)(6,`span`,5)),r&2){let l=ft(1);S(`id`,s.buttonId)(`disabled`,s.disabled&&!s.disabledInteractive||null),ne(`role`,s.isSingleSelector()?`radio`:`button`)(`tabindex`,s.disabled&&!s.disabledInteractive?-1:s.tabIndex)(`aria-pressed`,s.isSingleSelector()?null:s.checked)(`aria-checked`,s.isSingleSelector()?s.checked:null)(`name`,s._getButtonName())(`aria-label`,s.ariaLabel)(`aria-labelledby`,s.ariaLabelledby)(`aria-disabled`,s.disabled&&s.disabledInteractive?`true`:null),m(2),D(s.buttonToggleGroup&&(!s.buttonToggleGroup.multiple&&!s.buttonToggleGroup.hideSingleSelectionIndicator||s.buttonToggleGroup.multiple&&!s.buttonToggleGroup.hideMultipleSelectionIndicator)?2:-1),m(4),S(`matRippleTrigger`,l)(`matRippleDisabled`,s.disableRipple||s.disabled)}},dependencies:[li,fm],styles:[`.mat-button-toggle-standalone,
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
`],encapsulation:2})})()}return t})();var oE=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=oe({imports:[ki,Fm,we]})}return t})();var $n=class{static count(n,e){return e===`CANDIDATE`?n.candidatePlacements:n.evaluationPlacements}static components(n,e,i){let a=i.trim().toLocaleLowerCase();return(n?.components??[]).filter(o=>o.name.toLocaleLowerCase().includes(a)).sort((o,r)=>this.count(r,e)-this.count(o,e)||o.name.localeCompare(r.name))}static relationships(n,e,i,a){return(n?.relationships??[]).filter(o=>i===`incoming`?o.child===e:o.parent===e).sort((o,r)=>this.count(r,a)-this.count(o,a)||o.parent.localeCompare(r.parent)||o.role.localeCompare(r.role)||o.child.localeCompare(r.child))}static treemap(n,e,i){return{options:{chart:{type:`treemap`,height:390,animation:!1,backgroundColor:`transparent`},title:{text:void 0},credits:{enabled:!1},accessibility:{description:`Component placements. Select a tile or a component in the list to inspect its parents, children and configurations.`},tooltip:{pointFormat:`<b>{point.name}</b><br/>Placements: {point.value:,.0f}`},plotOptions:{series:{animation:!1}},series:[{type:`treemap`,name:`Component placements`,layoutAlgorithm:`squarified`,colorByPoint:!0,borderWidth:3,borderColor:`#fff`,colors:[`#2265a7`,`#00897b`,`#407fab`,`#49a69e`,`#697cad`,`#78b8ad`],dataLabels:{enabled:!0,format:`{point.name}<br/>{point.value:,.0f}`,style:{textOutline:`none`,fontWeight:`600`}},point:{events:{click:function(){this.name&&i(this.name)}}},data:n.filter(a=>this.count(a,e)>0).map(a=>({name:a.name,value:this.count(a,e)}))}]}}}static sankey(n,e,i,a,o){let r=[{id:`focus`,name:n??`Component`,color:`#2265a7`}],s=[],l=(p,g)=>{let _=p.filter(y=>this.count(y,a)>0);for(let[y,I]of _.slice(0,10).entries()){let P=`${g}:${y}`;r.push({id:P,name:g===`in`?`${I.parent} \xB7 ${I.role}`:`${I.role} \u2192 ${I.child}`,color:`#00897b`}),s.push({from:g===`in`?P:`focus`,to:g===`in`?`focus`:P,weight:this.count(I,a),custom:{edge:I}})}let b=_.slice(10).reduce((y,I)=>y+this.count(I,a),0);if(b>0){let y=`${g}:other`;r.push({id:y,name:`Other ${g===`in`?`parents`:`children`}`,color:`#aab7c5`}),s.push({from:g===`in`?y:`focus`,to:g===`in`?`focus`:y,weight:b})}};l(e,`in`),l(i,`out`);let h={type:`sankey`,animation:!1,name:`Direct relationships`,nodes:r,data:s,point:{events:{click:function(){let p=this.options.custom?.edge;p&&o(p)}}}};return{options:{chart:{height:Math.max(300,Math.min(560,Math.max(e.length,i.length)*32)),animation:!1,backgroundColor:`transparent`},title:{text:void 0},credits:{enabled:!1},accessibility:{description:`Direct parents and children of ${n??`the selected component`}. The complete role relationships are also listed in the inspector.`},series:[h]}}}static matrix(n,e,i,a){let o=new Set(this.components(n,e,i).map(y=>y.name)),r=i.trim().length>0,s=(n?.relationships??[]).filter(y=>!r||o.has(y.parent)||o.has(y.child)),l=new Set(o);for(let y of s)l.add(y.parent),l.add(y.child);let h=this.components(n,e,``).filter(y=>l.has(y.name)).sort((y,I)=>(r?Number(o.has(I.name))-Number(o.has(y.name)):0)||this.count(I,e)-this.count(y,e)||y.name.localeCompare(I.name)).slice(0,a),p=new Set(h.map(y=>y.name)),g=new Map,_=0;for(let y of s){if(!p.has(y.parent)||!p.has(y.child))continue;let I=JSON.stringify([y.parent,y.role]),P=g.get(I);P||(P={parent:y.parent,role:y.role,count:0,edges:new Map},g.set(I,P)),P.edges.set(y.child,y),P.count+=this.count(y,e),_=Math.max(_,this.count(y,e))}return{columns:h,rows:[...g.values()].sort((y,I)=>I.count-y.count||y.parent.localeCompare(I.parent)||y.role.localeCompare(I.role)).slice(0,a).map(y=>({parent:y.parent,role:y.role,cells:h.map(I=>y.edges.get(I.name)??null)})),maximum:_}}static parentPercent(n,e){let i=e?.components.find(a=>a.name===n.parent);return i?.configurationCount?100*n.configurationCount/i.configurationCount:0}};var tF=()=>[];var nF=(t,n)=>n.configurationId;var rE=(t,n)=>n.name;var sE=(t,n)=>n.parent+`:`+n.role;var iF=(t,n)=>n.role+`:`+n.child;function aF(t,n){t&1&&u(0,` Count each unique configuration tree once. Repeated components within a tree count as separate placements. `)}function oF(t,n){t&1&&u(0,` Weight each placement by accepted evaluations, including evaluations that later fail or are rejected. `)}function rF(t,n){if(t&1){let e=$();c(0,`p`,13),u(1),c(2,`button`,15),C(`click`,function(){k(e);return M(v().store.refreshComponentUsage())}),u(3,`Retry`),d()()}t&2&&(m(),B(` `,n,` `))}function sF(t,n){if(t&1&&(c(0,`p`,17),u(1),j(2,`number`),d()),t&2){let e=v();m(),B(` `,_e(2,1,e.unavailableConfigurationCount),` configurations have unavailable component trees and are excluded from placement counts. `)}}function lF(t,n){if(t&1&&(c(0,`p`,14),u(1),d()),t&2){let e=v(2);m(),B(` `,e.store.componentScope()===`CURRENT_ELITES`?`Elites appear after the first race checkpoint.`:`Component usage appears when the first decoded candidate is evaluated.`,` `)}}function cF(t,n){t&1&&(c(0,`p`,14),u(1,`No components match this search.`),d())}function dF(t,n){t&1&&(c(0,`p`,14),u(1,` These configurations have no accepted evaluations yet. Select candidate structures to inspect their composition. `),d())}function uF(t,n){if(t&1){let e=$();c(0,`button`,31),C(`click`,function(){let a=k(e).$implicit;return M(v(4).selectComponent(a.name))}),c(1,`span`),u(2),d(),c(3,`strong`),u(4),j(5,`number`),d()()}if(t&2){let e=n.$implicit,i=v(4);G(`selected`,i.selected()?.name===e.name),ne(`aria-pressed`,i.selected()?.name===e.name),m(2),w(e.name),m(2),w(_e(5,5,i.count(e)))}}function mF(t,n){if(t&1&&(c(0,`mat-card-header`)(1,`mat-card-title`),u(2,`Component usage`),d()(),c(3,`mat-card-content`),U(4,`app-dashboard-chart`,28),E(5,dF,2,0,`p`,14),c(6,`div`,29),de(7,uF,6,7,`button`,30,rE),d()()),t&2){let e=v(2),i=v();m(4),S(`model`,i.treemap()),m(),D(i.basis()===`EVALUATION`&&e.evaluationPlacementCount===0?5:-1),m(2),ue(i.components())}}function pF(t,n){if(t&1&&(c(0,`th`,25),u(1),d()),t&2){let e=n.$implicit;m(),w(e.name)}}function hF(t,n){if(t&1){let e=$();c(0,`button`,31),C(`click`,function(){k(e);let a=v().$implicit;return M(v(5).selectRelationship(a))}),u(1),d()}if(t&2){let e=v().$implicit,i=v(5);Jt(`background-color`,i.cellColor(e)),ne(`aria-label`,i.cellDescription(e)),m(),B(` `,i.cellLabel(e),` `)}}function fF(t,n){t&1&&(c(0,`span`),u(1,`—`),d())}function gF(t,n){if(t&1&&(c(0,`td`),E(1,hF,2,4,`button`,38)(2,fF,2,0,`span`),d()),t&2){let e=n.$implicit;m(),D(e?1:2)}}function bF(t,n){if(t&1&&(c(0,`tr`)(1,`th`,37),u(2),c(3,`small`),u(4),d()(),de(5,gF,3,1,`td`,null,wo),d()),t&2){let e=n.$implicit;m(2),B(` `,e.parent),m(2),w(e.role),m(),ue(e.cells)}}function vF(t,n){if(t&1&&(c(0,`tr`)(1,`td`),u(2,` No direct relationships in this selection. `),d()()),t&2){let e=v(4);m(),ne(`colspan`,e.matrix().columns.length+1)}}function _F(t,n){if(t&1){let e=$();c(0,`mat-card-header`)(1,`mat-card-title`),u(2,`Direct component relationships`),d()(),c(3,`mat-card-content`)(4,`div`,32)(5,`mat-form-field`,2)(6,`mat-label`),u(7,`Maximum rows / columns`),d(),c(8,`mat-select`,3),C(`selectionChange`,function(a){k(e);return M(v(3).matrixLimit.set(a.value))}),c(9,`mat-option`,33),u(10,`10`),d(),c(11,`mat-option`,33),u(12,`20`),d(),c(13,`mat-option`,33),u(14,`50`),d()()(),c(15,`mat-button-toggle-group`,34),C(`change`,function(a){k(e);return M(v(3).matrixPercent.set(a.value))}),c(16,`mat-button-toggle`,33),u(17,`Count`),d(),c(18,`mat-button-toggle`,33),u(19,`% of parent configurations`),d()()(),c(20,`p`,12),u(21,` Rows identify the parent and parameter role; columns identify the direct child. The most frequent matching components are shown. `),d(),c(22,`div`,35)(23,`table`,36)(24,`caption`),u(25,` Direct parent-role to child usage `),d(),c(26,`thead`)(27,`tr`)(28,`th`,25),u(29,`Parent · role`),d(),de(30,pF,2,1,`th`,25,rE),d()(),c(32,`tbody`),de(33,bF,7,2,`tr`,null,sE,!1,vF,3,1,`tr`),d()()()()}if(t&2){let e=v(3);m(8),S(`value`,e.matrixLimit()),m(),S(`value`,10),m(2),S(`value`,20),m(2),S(`value`,50),m(2),S(`value`,e.matrixPercent()),m(),S(`value`,!1),m(2),S(`value`,!0),m(12),ue(e.matrix().columns),m(3),ue(e.matrix().rows)}}function yF(t,n){if(t&1){let e=$();c(0,`button`,31),C(`click`,function(){let a=k(e).$implicit;return M(v(4).selectRelationship(a))}),c(1,`span`)(2,`strong`),u(3),d(),c(4,`small`),u(5),d()(),c(6,`span`),u(7),j(8,`number`),c(9,`small`),u(10),j(11,`number`),d()()()}if(t&2){let e=n.$implicit,i=v(4);m(3),w(e.parent),m(2),w(e.role),m(2),w(_e(8,4,i.count(e))),m(3),B(``,_e(11,6,e.configurationCount),` configurations`)}}function xF(t,n){t&1&&(c(0,`p`),u(1,`No direct parents; this component is used only at the root.`),d())}function CF(t,n){if(t&1){let e=$();c(0,`button`,31),C(`click`,function(){let a=k(e).$implicit;return M(v(4).selectRelationship(a))}),c(1,`span`)(2,`strong`),u(3),d(),c(4,`small`),u(5),d()(),c(6,`span`),u(7),j(8,`number`),c(9,`small`),u(10),j(11,`number`),d()()()}if(t&2){let e=n.$implicit,i=v(4);m(3),w(e.child),m(2),w(e.role),m(2),w(_e(8,4,i.count(e))),m(3),B(``,_e(11,6,e.configurationCount),` configurations`)}}function SF(t,n){t&1&&(c(0,`p`),u(1,`No direct child components.`),d())}function wF(t,n){if(t&1&&(c(0,`mat-card`,20)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d(),c(4,`mat-card-subtitle`),u(5,`Component inspector`),d()(),c(6,`mat-card-content`)(7,`dl`)(8,`dt`),u(9,`Candidate placements`),d(),c(10,`dd`),u(11),j(12,`number`),d(),c(13,`dt`),u(14,`Evaluation exposure`),d(),c(15,`dd`),u(16),j(17,`number`),d(),c(18,`dt`),u(19,`Configuration coverage`),d(),c(20,`dd`),u(21),j(22,`number`),j(23,`number`),d(),c(24,`dt`),u(25,`Root placements / exposure`),d(),c(26,`dd`),u(27),j(28,`number`),j(29,`number`),d()(),c(30,`h3`),u(31,`Used by`),d(),c(32,`div`,39),de(33,yF,12,8,`button`,40,sE,!1,xF,2,0,`p`),d(),c(36,`h3`),u(37,`Contains`),d(),c(38,`div`,39),de(39,CF,12,8,`button`,40,iF,!1,SF,2,0,`p`),d()()()),t&2){let e=n,i=v(3);m(3),w(e.name),m(8),w(_e(12,9,e.candidatePlacements)),m(5),w(_e(17,11,e.evaluationPlacements)),m(5),Me(` `,_e(22,13,e.configurationCount),` (`,ke(23,15,i.selectedCoverage(),`1.0-1`),`%) `),m(6),Me(` `,_e(28,18,e.rootCandidatePlacements),` / `,_e(29,20,e.rootEvaluationPlacements),` `),m(6),ue(i.incoming()),m(6),ue(i.outgoing())}}function EF(t,n){if(t&1&&(c(0,`mat-card`,21)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d()(),c(4,`mat-card-content`),U(5,`app-dashboard-chart`,28),c(6,`p`,12),u(7,` Direct parents → selected component → direct children. Up to ten relationships are shown on each side; the inspector lists all relationships. `),d()()()),t&2){let e=v(3);m(3),B(`Neighborhood of `,e.selected()?.name),m(2),S(`model`,e.sankey())}}function DF(t,n){if(t&1){let e=$();c(0,`p`),u(1),c(2,`button`,15),C(`click`,function(){k(e);return M(v(3).selectedEdge.set(null))}),u(3),d()()}if(t&2){let e=n,i=v(3);m(),Ot(` `,e.parent,` → `,e.role,` → `,e.child,` `),m(2),B(` Show all using `,i.selected()?.name,` `)}}function IF(t,n){if(t&1&&(c(0,`p`),u(1),d()),t&2){let e=v(3);m(),B(`Configurations containing `,e.selected()?.name)}}function kF(t,n){if(t&1){let e=$();c(0,`p`,22),u(1),c(2,`button`,15),C(`click`,function(){k(e);return M(v(3).retryPage())}),u(3,`Retry`),d()()}t&2&&(m(),B(` `,n,` `))}function MF(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`)(2,`button`,15),C(`click`,function(){let a=k(e).$implicit;return M(v(3).inspectCandidate(a.configurationId))}),u(3),d()(),c(4,`td`),u(5),j(6,`number`),d(),c(7,`td`),u(8),j(9,`number`),d(),c(10,`td`),u(11),d(),c(12,`td`),u(13),d()()}if(t&2){let e=n.$implicit;m(3),B(` `,e.configurationId,` `),m(2),w(_e(6,8,e.occurrences)),m(3),w(_e(9,10,e.evaluationPlacements)),m(3),Si(` `,e.evaluations.running,` / `,e.evaluations.succeeded,` / `,e.evaluations.rejected,` / `,e.evaluations.failed,` `),m(2),w(e.elitePosition??`—`)}}function TF(t,n){if(t&1&&(c(0,`tr`)(1,`td`,41),u(2),d()()),t&2){let e=v(3);m(2),B(` `,e.loading()?`Loading configurations…`:`No matching configurations.`,` `)}}function NF(t,n){if(t&1){let e=$();c(0,`div`,18)(1,`mat-card`,19),E(2,mF,9,2)(3,_F,36,8),d(),E(4,wF,42,22,`mat-card`,20),E(5,EF,8,2,`mat-card`,21),c(6,`mat-card`,21)(7,`mat-card-header`)(8,`mat-card-title`),u(9,`Matching configurations`),d()(),c(10,`mat-card-content`),E(11,DF,4,4,`p`)(12,IF,2,1,`p`),E(13,kF,4,1,`p`,22),c(14,`div`,23)(15,`table`,24)(16,`thead`)(17,`tr`)(18,`th`,25),u(19,`Configuration`),d(),c(20,`th`,25),u(21,`Tree placements`),d(),c(22,`th`,25),u(23,`Evaluation exposure`),d(),c(24,`th`,25),u(25,`Running / succeeded / rejected / failed`),d(),c(26,`th`,25),u(27,`Elite rank`),d()()(),c(28,`tbody`),de(29,MF,14,12,`tr`,null,nF,!1,TF,3,1,`tr`),d()()(),c(32,`div`,26)(33,`span`),u(34),j(35,`number`),d(),c(36,`button`,27),C(`click`,function(){k(e);let a=v(2);return M(a.offset.set(a.offset()-25))}),u(37,` Previous `),d(),c(38,`span`),u(39),d(),c(40,`button`,27),C(`click`,function(){k(e);let a=v(2);return M(a.offset.set(a.page()?.nextOffset??0))}),u(41,` Next `),d()()()()()}if(t&2){let e,i,a,o=v(2);m(2),D(o.view()===`usage`?2:3),m(2),D((e=o.selected())?4:-1,e),m(),D(o.view()===`usage`&&(o.incoming().length>0||o.outgoing().length>0)?5:-1),m(6),D((i=o.relationship())?11:12,i),m(2),D((a=o.pageError())?13:-1,a),m(),ne(`aria-busy`,o.loading()),m(15),ue(o.page()?.candidates??nt(13,tF)),m(5),B(``,_e(35,11,o.page()?.total??0),` matching configurations`),m(2),S(`disabled`,o.offset()===0||o.loading()),m(3),B(`Page `,o.offset()/25+1),m(),S(`disabled`,o.page()?.nextOffset==null||o.loading())}}function AF(t,n){if(t&1&&(c(0,`div`,16)(1,`article`)(2,`span`),u(3,`Decoded configurations`),d(),c(4,`strong`),u(5),j(6,`number`),j(7,`number`),d()(),c(8,`article`)(9,`span`),u(10),d(),c(11,`strong`),u(12),j(13,`number`),d()(),c(14,`article`)(15,`span`),u(16,`Distinct components`),d(),c(17,`strong`),u(18),j(19,`number`),d()(),c(20,`article`)(21,`span`),u(22,`Accepted evaluations`),d(),c(23,`strong`),u(24),j(25,`number`),d()()(),E(26,sF,3,3,`p`,17),E(27,lF,2,1,`p`,14)(28,cF,2,0,`p`,14)(29,NF,42,14,`div`,18)),t&2){let e=n,i=v();m(5),Me(``,_e(6,8,e.decodedConfigurationCount),` / `,_e(7,10,e.configurationCount)),m(5),w(i.basis()===`CANDIDATE`?`Component placements`:`Evaluation placements`),m(2),w(_e(13,12,i.basis()===`CANDIDATE`?e.candidatePlacementCount:e.evaluationPlacementCount)),m(6),w(_e(19,14,e.components.length)),m(6),w(_e(25,16,e.evaluationCount)),m(2),D(e.unavailableConfigurationCount>0?26:-1),m(),D(e.components.length===0?27:i.components().length===0?28:29)}}function RF(t,n){t&1&&(c(0,`p`,14),u(1,`Loading component usage…`),d())}var Lm=class t{store=f(za);dialog=f($o);destroyRef=f(Ae);basis=T(`CANDIDATE`);view=T(`usage`);search=T(``);matrixLimit=T(20);matrixPercent=T(!1);selectedName=T(null);selectedEdge=T(null);offset=T(0);page=T(null);loading=T(!1);pageError=T(null);pageRefresh=T(0);request=0;pageKey=null;destroyed=!1;components=K(()=>$n.components(this.store.componentUsage(),this.basis(),this.search()));selected=K(()=>this.components().find(n=>n.name===this.selectedName())??this.components()[0]);relationship=K(()=>{let n=this.selectedEdge();return this.store.componentUsage()?.relationships.find(e=>n&&e.parent===n.parent&&e.role===n.role&&e.child===n.child)??null});incoming=K(()=>$n.relationships(this.store.componentUsage(),this.selected()?.name,`incoming`,this.basis()));outgoing=K(()=>$n.relationships(this.store.componentUsage(),this.selected()?.name,`outgoing`,this.basis()));treemap=K(()=>$n.treemap(this.components(),this.basis(),n=>this.selectComponent(n)));sankey=K(()=>$n.sankey(this.selected()?.name,this.incoming(),this.outgoing(),this.basis(),n=>this.selectRelationship(n)));matrix=K(()=>$n.matrix(this.store.componentUsage(),this.basis(),this.search(),this.matrixLimit()));selector=K(()=>{let n=this.relationship();if(n)return{parent:n.parent,role:n.role,child:n.child};let e=this.selected()?.name;return e?{component:e}:null});selectedCoverage=K(()=>{let n=this.store.componentUsage()?.decodedConfigurationCount??0;return n?100*(this.selected()?.configurationCount??0)/n:0});selectionKey=K(()=>JSON.stringify(this.selector()));constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.request++}),vt(()=>{this.store.componentScope(),this.basis(),this.selectionKey(),this.store.status()?.runId,it(()=>this.offset.set(0))}),vt(()=>{let n=this.store.componentUsage(),e=this.selector(),i=this.store.componentScope(),a=this.basis(),o=this.offset();this.pageRefresh(),it(()=>{if(!n||!e){this.request++,this.page.set(null),this.loading.set(!1),this.pageError.set(null);return}this.loadPage({scope:i,basis:a,selector:e,offset:o,limit:25})})})}selectComponent(n){this.selectedName.set(n),this.selectedEdge.set(null)}selectRelationship(n){this.selectedName.set(n.child),this.selectedEdge.set(n)}updateSearch(n){this.search.set(n.target.value),this.selectedEdge.set(null)}count(n){return $n.count(n,this.basis())}cellLabel(n){return this.matrixPercent()?`${$n.parentPercent(n,this.store.componentUsage()).toFixed(1)}%`:this.count(n).toLocaleString()}cellColor(n){let e=this.matrixPercent()?$n.parentPercent(n,this.store.componentUsage())/100:this.count(n)/(this.matrix().maximum||1);return`rgba(0, 137, 123, ${.08+Math.max(0,Math.min(1,e))*.65})`}cellDescription(n){return`${n.parent}, ${n.role}, ${n.child}: ${this.cellLabel(n)}`}inspectCandidate(n){this.dialog.open(fs,{data:n,width:`46rem`,maxWidth:`94vw`})}retryPage(){this.pageRefresh.update(n=>n+1)}async loadPage(n){let e=++this.request,i=this.store.componentUsage()?.runId;this.loading.set(!0),this.pageError.set(null);let a=JSON.stringify([i,n]);a!==this.pageKey&&this.page.set(null),this.pageKey=a;try{let o=await this.store.loadComponentCandidates(n);if(this.destroyed||e!==this.request)return;if(o.runId!==i||o.scope!==n.scope)throw new Error(`Configurations belong to a different run or scope`);if(n.offset>0&&o.total<=n.offset){this.offset.set(0);return}this.page.set(o)}catch(o){!this.destroyed&&e===this.request&&this.pageError.set(o instanceof Error?o.message:`Unable to load configurations`)}finally{!this.destroyed&&e===this.request&&this.loading.set(!1)}}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-component-usage-dashboard`]],decls:33,vars:7,consts:[[1,`component-page`],[1,`controls`],[`appearance`,`outline`],[3,`selectionChange`,`value`],[`value`,`ALL`],[`value`,`CURRENT_ELITES`],[`value`,`CANDIDATE`],[`value`,`EVALUATION`],[`matInput`,``,3,`input`,`value`],[`aria-label`,`Component visualization`,3,`change`,`value`],[`value`,`usage`],[`value`,`matrix`],[1,`explanation`],[`role`,`alert`,1,`warning`],[1,`empty`],[`mat-button`,``,3,`click`],[1,`totals`],[1,`warning`],[1,`layout`],[`appearance`,`outlined`,1,`visualization`],[`appearance`,`outlined`,1,`inspector`],[`appearance`,`outlined`,1,`wide`],[`role`,`alert`],[1,`table-scroll`],[1,`candidate-table`],[`scope`,`col`],[1,`pagination`],[`mat-button`,``,3,`click`,`disabled`],[3,`model`],[`aria-label`,`Select a component`,1,`component-list`],[`type`,`button`,3,`selected`],[`type`,`button`,3,`click`],[1,`matrix-controls`],[3,`value`],[`aria-label`,`Matrix values`,3,`change`,`value`],[`tabindex`,`0`,`aria-label`,`Scrollable component relationship matrix`,1,`matrix-scroll`],[1,`matrix`],[`scope`,`row`],[`type`,`button`,3,`background-color`],[1,`relationships`],[`type`,`button`],[`colspan`,`5`]],template:function(e,i){if(e&1&&(c(0,`section`,0)(1,`div`,1)(2,`mat-form-field`,2)(3,`mat-label`),u(4,`Configurations`),d(),c(5,`mat-select`,3),C(`selectionChange`,function(o){return i.store.selectComponentScope(o.value)}),c(6,`mat-option`,4),u(7,`All evaluated candidates`),d(),c(8,`mat-option`,5),u(9,`Current elites`),d()()(),c(10,`mat-form-field`,2)(11,`mat-label`),u(12,`Count by`),d(),c(13,`mat-select`,3),C(`selectionChange`,function(o){return i.basis.set(o.value)}),c(14,`mat-option`,6),u(15,`Candidate structures`),d(),c(16,`mat-option`,7),u(17,`Evaluation exposure`),d()()(),c(18,`mat-form-field`,2)(19,`mat-label`),u(20,`Find component`),d(),c(21,`input`,8),C(`input`,function(o){return i.updateSearch(o)}),d()(),c(22,`mat-button-toggle-group`,9),C(`change`,function(o){return i.view.set(o.value)}),c(23,`mat-button-toggle`,10),u(24,`Usage map`),d(),c(25,`mat-button-toggle`,11),u(26,`Relationship matrix`),d()()(),c(27,`p`,12),E(28,aF,1,0)(29,oF,1,0),d(),E(30,rF,4,1,`p`,13),E(31,AF,30,18)(32,RF,2,0,`p`,14),d()),e&2){let a,o;m(5),S(`value`,i.store.componentScope()),m(8),S(`value`,i.basis()),m(8),S(`value`,i.search()),m(),S(`value`,i.view()),m(6),D(i.basis()===`CANDIDATE`?28:29),m(2),D((a=i.store.componentError())?30:-1,a),m(),D((o=i.store.componentUsage())?31:i.store.componentError()?-1:32,o)}},dependencies:[Ft,nn,oE,Gb,Fm,zn,Bn,jn,Ta,ju,Ma,di,Ti,na,es,Jr,Fa,Pa,Un,Ai,En],styles:[`[_nghost-%COMP%]{display:block}.component-page[_ngcontent-%COMP%]{padding:1.4rem 0}.controls[_ngcontent-%COMP%], .matrix-controls[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:1rem;align-items:center}.controls[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{flex:1 1 12rem}.explanation[_ngcontent-%COMP%]{color:#526477;line-height:1.5;font-size:.9rem;margin:.5rem 0 1.25rem}.totals[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(4,1fr);gap:1rem;margin-bottom:1.4rem}.totals[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{border:1px solid #d9e3ec;border-radius:.8rem;padding:1rem;background:#fff}.totals[_ngcontent-%COMP%]   span[_ngcontent-%COMP%], .totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{display:block}.totals[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:#526477;font-size:.85rem}.totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{color:#174b80;margin-top:.4rem;font-size:1.5rem}.warning[_ngcontent-%COMP%]{background:#fff5dc;padding:.8rem 1rem;border-radius:.6rem}.empty[_ngcontent-%COMP%]{padding:2rem;text-align:center;color:#526477}.layout[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(0,2fr) minmax(18rem,1fr);gap:1.25rem}.layout[_ngcontent-%COMP%] > *[_ngcontent-%COMP%]{min-width:0}.wide[_ngcontent-%COMP%]{grid-column:1/-1}mat-card-header[_ngcontent-%COMP%]{margin-bottom:1rem}.component-list[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.3rem;max-height:15rem;overflow:auto;margin-top:1rem}.component-list[_ngcontent-%COMP%]   button[_ngcontent-%COMP%], .relationships[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{border:0;background:#f3f7fb;cursor:pointer;border-radius:.4rem;padding:.65rem;display:flex;justify-content:space-between;gap:.5rem;text-align:left;color:#193b59}.component-list[_ngcontent-%COMP%]   button.selected[_ngcontent-%COMP%]{background:#d5e8f7;outline:2px solid #2265a7;outline-offset:-2px}.component-list[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{overflow-wrap:anywhere}.relationships[_ngcontent-%COMP%]{max-height:17rem;overflow:auto;display:grid;gap:.35rem}.relationships[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{display:block;color:#526477;margin-top:.25rem}.relationships[_ngcontent-%COMP%]   button[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{text-align:right}.relationships[_ngcontent-%COMP%]   p[_ngcontent-%COMP%]{color:#526477}dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:1fr auto;gap:.8rem;font-size:.9rem}dt[_ngcontent-%COMP%]{color:#526477}dd[_ngcontent-%COMP%]{margin:0;text-align:right}h3[_ngcontent-%COMP%]{font-size:1rem;margin:1.4rem 0 .7rem}.matrix-scroll[_ngcontent-%COMP%], .table-scroll[_ngcontent-%COMP%]{overflow:auto}table[_ngcontent-%COMP%]{border-collapse:separate;border-spacing:0;font-size:.85rem;width:100%}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.65rem;border-bottom:1px solid #dce5ee;text-align:left}th[_ngcontent-%COMP%]{color:#526477;font-weight:600}.matrix[_ngcontent-%COMP%]   caption[_ngcontent-%COMP%]{text-align:left;padding:.5rem 0;color:#526477}.matrix[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]{min-width:7rem}.matrix[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{display:block;margin-top:.2rem;font-weight:400}.matrix[_ngcontent-%COMP%]   tbody[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]{position:sticky;left:0;background:#fff;z-index:1}.matrix[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]{padding:.2rem;text-align:center}.matrix[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{width:100%;min-width:5rem;min-height:2.8rem;border:0;border-radius:.25rem;cursor:pointer;color:#112e3b}.pagination[_ngcontent-%COMP%]{display:flex;justify-content:flex-end;align-items:center;flex-wrap:wrap;gap:.7rem;margin-top:.8rem}.pagination[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:first-child{margin-right:auto}@media(max-width:1050px){.layout[_ngcontent-%COMP%]{grid-template-columns:1fr}.totals[_ngcontent-%COMP%]{grid-template-columns:repeat(2,1fr)}}@media(max-width:600px){.component-list[_ngcontent-%COMP%]{grid-template-columns:1fr}.controls[_ngcontent-%COMP%]   mat-button-toggle-group[_ngcontent-%COMP%], .matrix-controls[_ngcontent-%COMP%]   mat-button-toggle-group[_ngcontent-%COMP%]{max-width:100%;overflow:auto}.totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font-size:1.2rem}}`]})};var Vm=()=>[];var OF=(t,n)=>n.key;var lE=(t,n)=>n.id;var cE=(t,n)=>n.configurationId;var PF=(t,n)=>n.instanceName;var FF=(t,n)=>n.iteration;function LF(t,n){if(t&1){let e=$();c(0,`button`,19),C(`click`,function(){k(e);return M(v().showIssues())}),c(1,`span`,53)(2,`strong`),u(3),d(),c(4,`small`),u(5),d()()()}if(t&2){let e=n.$implicit;m(2),G(`critical`,e.severity===`critical`),m(),w(e.title),m(2),w(e.message)}}function VF(t,n){t&1&&(c(0,`span`,10),u(1,`No evaluation alerts`),d())}function BF(t,n){if(t&1){let e=$();c(0,`button`,19),C(`click`,function(){k(e);return M(v().showIssues())}),c(1,`strong`),u(2,`View all issues`),d()()}}function jF(t,n){if(t&1&&(c(0,`section`,11)(1,`div`,54)(2,`span`,55),u(3,`Evaluation budget`),d(),c(4,`strong`),u(5),j(6,`number`),j(7,`number`),d(),c(8,`span`),u(9),j(10,`number`),d()(),U(11,`mat-progress-bar`,56),c(12,`div`,57),U(13,`span`,58),u(14),d()()),t&2){let e=v(),i=Ar(0);m(5),Me(``,_e(6,7,i.budget.used),` / `,_e(7,9,i.budget.maximum)),m(4),B(``,_e(10,11,i.budget.remaining),` remaining`),m(2),S(`value`,e.store.budgetPercent()),m(2),G(`complete`,i.state===`COMPLETED`),m(),B(` `,e.phaseLabel(i.phase),` `)}}function zF(t,n){if(t&1){let e=$();c(0,`section`,12)(1,`strong`),u(2,`Monitor connection problem`),d(),c(3,`span`),u(4),d(),c(5,`button`,50),C(`click`,function(){k(e);return M(v().store.retryNow())}),u(6,`Retry now`),d()()}t&2&&(m(4),w(n))}function HF(t,n){if(t&1&&(c(0,`section`,13)(1,`strong`),u(2),d(),c(3,`span`),u(4),d()()),t&2){let e=n;m(2),w(e.type),m(2),w(e.message??`The tuning run stopped unexpectedly.`)}}function UF(t,n){if(t&1){let e=$();c(0,`button`,19),C(`click`,function(){k(e);let a=v().$implicit;return M(v().preferences.show(a.id))}),u(1),d()}if(t&2){let e=v().$implicit;m(),B(` Show `,e.label,` `)}}function $F(t,n){if(t&1&&E(0,UF,2,1,`button`,9),t&2){let e=n.$implicit;D(v().preferences.isHidden(e.id)?0:-1)}}function GF(t,n){if(t&1){let e=$();c(0,`button`,59),C(`click`,function(){let a=k(e).$implicit;return M(v().inspectCandidate(a.configurationId))}),c(1,`span`,60),u(2),d(),c(3,`span`)(4,`strong`),u(5),d(),c(6,`small`),u(7),d()()()}if(t&2){let e=n.$implicit,i=v();m(),tt(i.rankClass(e.position)),m(),w(e.position),m(3),B(`Configuration `,e.configurationId),m(2),w(i.parameterPreview(e))}}function WF(t,n){t&1&&(c(0,`p`,32),u(1,`Elites appear after the first completed race checkpoint.`),d())}function qF(t,n){if(t&1){let e=$();c(0,`button`,61),C(`click`,function(){k(e);return M(v().inspectSearchSpace())}),u(1,` Inspect search space `),d()}}function YF(t,n){if(t&1&&(c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),j(7,`number`),d(),c(8,`td`),u(9),d(),c(10,`td`),u(11),d()()),t&2){let e=n.$implicit;m(2),w(e.instanceName),m(2),w(e.evaluations),m(2),B(` `,e.bestCost===null?`—`:ke(7,5,e.bestCost,`1.0-5`),` `),m(3),w(e.issues),m(2),w(e.slow)}}function KF(t,n){t&1&&(c(0,`tr`)(1,`td`,62),u(2,`No instance activity yet.`),d()())}function QF(t,n){t&1&&u(0,` Final result `)}function XF(t,n){t&1&&u(0,` Latest completed checkpoint `)}function ZF(t,n){if(t&1){let e=$();c(0,`button`,59),C(`click`,function(){let a=k(e).$implicit;return M(v().inspectCandidate(a.configurationId))}),c(1,`span`,60),u(2),d(),c(3,`span`)(4,`strong`),u(5),d(),c(6,`small`),u(7),d()()()}if(t&2){let e=n.$implicit,i=v();m(),tt(i.rankClass(e.position)),m(),w(e.position),m(3),B(`Configuration `,e.configurationId),m(2),w(i.parameterPreview(e))}}function JF(t,n){t&1&&(c(0,`p`,32),u(1,`No elite snapshot is available yet.`),d())}function e2(t,n){if(t&1&&(c(0,`article`)(1,`strong`),u(2),d(),c(3,`span`),u(4),j(5,`date`),d(),c(6,`span`),u(7),j(8,`number`),d(),c(9,`span`),u(10),d()()),t&2){let e=n.$implicit;m(2),B(`Checkpoint `,e.iteration),m(2),w(ke(5,4,e.updatedAt,`medium`)),m(3),B(``,_e(8,7,e.progress.experimentsUsed),` evaluations used`),m(3),B(``,e.elites.length,` elites`)}}function t2(t,n){t&1&&(c(0,`p`,32),u(1,`No completed race checkpoints yet.`),d())}function n2(t,n){t&1&&U(0,`app-component-usage-dashboard`)}function i2(t,n){t&1&&(c(0,`mat-tab`,48),st(1,n2,1,0,`ng-template`,63),d())}function a2(t,n){if(t&1&&(c(0,`a`,52)(1,`span`)(2,`strong`),u(3),d(),c(4,`small`),u(5),d()(),c(6,`span`),u(7),d(),c(8,`span`),u(9,`Download`),d()()),t&2){let e=n.$implicit,i=v();S(`href`,i.store.artifactUrl(e.id),dn),m(3),w(e.filename),m(2),w(e.mediaType),m(2),w(i.artifactSize(e))}}function o2(t,n){t&1&&(c(0,`p`,32),u(1,` Artifacts appear as IRACE writes logs, plots, workspace data, and final elites. `),d())}var Bm=class t{store=f(za);preferences=f(Ba);panels=Lb;selectedTab=T(0);forceIssues=T(!1);dialog=f($o);constructor(){this.store.start()}selectTab(n){this.selectedTab.set(n.index),n.tab.textLabel!==`Evaluations`&&this.forceIssues.set(!1),n.tab.textLabel===`Artifacts`&&this.store.refreshArtifacts()}showIssues(){this.forceIssues.set(!0),this.selectedTab.set(1)}inspectCandidate(n){this.dialog.open(fs,{data:n,width:`46rem`,maxWidth:`94vw`})}inspectSearchSpace(){let n=this.store.searchSpace();n&&this.dialog.open(Om,{data:n,maxWidth:`96vw`})}rankClass(n){return n<=3?`rank-${n}`:`rank-other`}phaseLabel(n){return n.toLowerCase().split(`_`).map(e=>e.charAt(0).toUpperCase()+e.slice(1)).join(` `)}elapsedLabel(n){if(n===null)return`—`;let e=Math.floor(n/1e3),i=Math.floor(e/3600),a=Math.floor(e%3600/60),o=e%60;return i>0?`${i}h ${a}m`:`${a}m ${o}s`}metricLabel(){let n=this.store.status()?.metric;if(!n)return`Waiting for metric details`;let e=n.kind===`AREA_UNDER_CURVE`?`Area under curve`:`Final objective`,i=n.negated?`maximized, converted to cost`:`minimized`;return`${n.objectiveName} \xB7 ${e} \xB7 ${i}`}parameterPreview(n){let e=Object.entries(n.parameters);return e.length===0?`No parameters`:e.slice(0,3).map(([i,a])=>`${i}=${a}`).join(` · `)}artifactSize(n){return n.size<1024?`${n.size} B`:n.size<1048576?`${(n.size/1024).toFixed(1)} KB`:`${(n.size/1048576).toFixed(1)} MB`}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-tuning-dashboard`]],decls:183,vars:47,consts:[[`alertsMenu`,`matMenu`],[`panelMenu`,`matMenu`],[1,`topbar`],[1,`brand`],[`src`,`favicon.svg`,`alt`,``],[1,`toolbar-spacer`],[`aria-label`,`Synchronization state`],[`mat-button`,``,`type`,`button`,`matBadgeColor`,`warn`,`aria-label`,`Tuning alerts`,1,`alerts-button`,3,`menuOpened`,`matMenuTriggerFor`,`matBadge`,`matBadgeHidden`],[1,`alerts-menu`],[`mat-menu-item`,``,`type`,`button`],[1,`menu-empty`],[`aria-label`,`Evaluation budget`,1,`budget-ribbon`],[`role`,`alert`,1,`request-error`],[`role`,`alert`,1,`run-error`],[`animationDuration`,`150ms`,3,`selectedTabChange`,`selectedIndex`],[`label`,`Overview`],[1,`tab-page`,`overview`],[1,`overview-actions`],[`mat-button`,``,`type`,`button`,3,`matMenuTriggerFor`],[`mat-menu-item`,``,`type`,`button`,3,`click`],[`panelId`,`evaluation-health`,`title`,`Evaluation health`,1,`wide`],[1,`kpi-grid`],[1,`kpi`],[1,`kpi`,`success`],[1,`kpi`,`warning`],[1,`kpi`,`danger`],[`panelId`,`evaluation-cost`,`title`,`Evaluation cost`],[3,`model`],[`panelId`,`elite-evolution`,`title`,`Elite rank evolution`],[`panelId`,`current-elites`,`title`,`Current elites`],[1,`elite-list`],[`type`,`button`,1,`elite-row`],[1,`empty-state`],[`panelId`,`run-information`,`title`,`Run information`],[1,`run-info`],[`mat-stroked-button`,``,`type`,`button`],[`label`,`Evaluations`],[1,`tab-page`],[1,`two-column`],[`appearance`,`outlined`],[1,`summary-scroll`],[1,`summary-table`],[`appearance`,`outlined`,1,`evaluation-card`],[3,`candidateSelected`,`evaluations`,`forceIssues`],[`label`,`Elites`],[1,`two-column`,`elite-layout`],[1,`elite-list`,`detailed`],[1,`history-list`],[`label`,`Components`],[`label`,`Artifacts`],[`mat-button`,``,`type`,`button`,3,`click`],[1,`artifact-list`],[`download`,``,3,`href`],[1,`alert-item`],[1,`budget-copy`],[1,`eyebrow`],[`mode`,`determinate`,3,`value`],[1,`phase`],[1,`phase-dot`],[`type`,`button`,1,`elite-row`,3,`click`],[1,`medal`],[`mat-stroked-button`,``,`type`,`button`,3,`click`],[`colspan`,`5`,1,`empty-state`],[`matTabContent`,``]],template:function(e,i){if(e&1&&(Nr(0),c(1,`mat-toolbar`,2)(2,`span`,3),U(3,`img`,4),u(4,` Mork tuning monitor`),d(),U(5,`span`,5),c(6,`mat-chip-set`,6)(7,`mat-chip`),u(8),d()(),c(9,`button`,7),C(`menuOpened`,function(){return i.store.markAlertsSeen()}),u(10,` Alerts `),d(),c(11,`mat-menu`,8,0),de(13,LF,6,4,`button`,9,OF,!1,VF,2,0,`span`,10),E(16,BF,3,0,`button`,9),d()(),E(17,jF,15,13,`section`,11),c(18,`main`),E(19,zF,7,1,`section`,12),E(20,HF,5,2,`section`,13),c(21,`mat-tab-group`,14),C(`selectedTabChange`,function(o){return i.selectTab(o)}),c(22,`mat-tab`,15)(23,`section`,16)(24,`div`,17)(25,`span`),u(26,`Live view of the current tuning run`),d(),c(27,`button`,18),u(28,`Panels`),d(),c(29,`mat-menu`,null,1),de(31,$F,1,1,null,null,lE),c(33,`button`,19),C(`click`,function(){return i.preferences.reset()}),u(34,` Restore all panels `),d()()(),c(35,`app-monitor-panel`,20)(36,`section`,21)(37,`article`,22)(38,`span`),u(39,`Running`),d(),c(40,`strong`),u(41),d()(),c(42,`article`,23)(43,`span`),u(44,`Succeeded`),d(),c(45,`strong`),u(46),d()(),c(47,`article`,24)(48,`span`),u(49,`Rejected`),d(),c(50,`strong`),u(51),d()(),c(52,`article`,25)(53,`span`),u(54,`Failed`),d(),c(55,`strong`),u(56),d()(),c(57,`article`,24)(58,`span`),u(59,`Slow`),d(),c(60,`strong`),u(61),d()()()(),c(62,`app-monitor-panel`,26),U(63,`app-dashboard-chart`,27),d(),c(64,`app-monitor-panel`,28),U(65,`app-dashboard-chart`,27),d(),c(66,`app-monitor-panel`,29)(67,`div`,30),de(68,GF,8,5,`button`,31,cE,!1,WF,2,0,`p`,32),d()(),c(71,`app-monitor-panel`,33)(72,`dl`,34)(73,`dt`),u(74,`Run ID`),d(),c(75,`dd`),u(76),d(),c(77,`dt`),u(78,`Mode`),d(),c(79,`dd`),u(80),d(),c(81,`dt`),u(82,`Elapsed`),d(),c(83,`dd`),u(84),d(),c(85,`dt`),u(86,`Training instances`),d(),c(87,`dd`),u(88),d(),c(89,`dt`),u(90,`Generated parameters`),d(),c(91,`dd`),u(92),d(),c(93,`dt`),u(94,`Cost metric`),d(),c(95,`dd`),u(96),d(),c(97,`dt`),u(98,`Last update`),d(),c(99,`dd`),u(100),j(101,`date`),d()(),E(102,qF,2,0,`button`,35),d()()(),c(103,`mat-tab`,36)(104,`section`,37)(105,`section`,38)(106,`mat-card`,39)(107,`mat-card-header`)(108,`mat-card-title`),u(109,`Evaluation activity`),d()(),c(110,`mat-card-content`),U(111,`app-dashboard-chart`,27),d()(),c(112,`mat-card`,39)(113,`mat-card-header`)(114,`mat-card-title`),u(115,`Instance summary`),d()(),c(116,`mat-card-content`,40)(117,`table`,41)(118,`thead`)(119,`tr`)(120,`th`),u(121,`Instance`),d(),c(122,`th`),u(123,`Runs`),d(),c(124,`th`),u(125,`Best cost`),d(),c(126,`th`),u(127,`Issues`),d(),c(128,`th`),u(129,`Slow`),d()()(),c(130,`tbody`),de(131,YF,12,8,`tr`,null,PF,!1,KF,3,0,`tr`),d()()()()(),c(134,`mat-card`,42)(135,`mat-card-header`)(136,`mat-card-title`),u(137,`Evaluation ledger`),d()(),c(138,`mat-card-content`)(139,`app-evaluation-table`,43),C(`candidateSelected`,function(o){return i.inspectCandidate(o)}),d()()()()(),c(140,`mat-tab`,44)(141,`section`,37)(142,`section`,45)(143,`mat-card`,39)(144,`mat-card-header`)(145,`mat-card-title`),u(146,`Current elite set`),d(),c(147,`mat-card-subtitle`),E(148,QF,1,0)(149,XF,1,0),d()(),c(150,`mat-card-content`)(151,`div`,46),de(152,ZF,8,5,`button`,31,cE,!1,JF,2,0,`p`,32),d()()(),c(155,`mat-card`,39)(156,`mat-card-header`)(157,`mat-card-title`),u(158,`Rank history`),d()(),c(159,`mat-card-content`),U(160,`app-dashboard-chart`,27),d()()(),c(161,`mat-card`,39)(162,`mat-card-header`)(163,`mat-card-title`),u(164,`Race checkpoints`),d()(),c(165,`mat-card-content`,47),de(166,e2,11,9,`article`,null,FF,!1,t2,2,0,`p`,32),d()()()(),E(169,i2,2,0,`mat-tab`,48),c(170,`mat-tab`,49)(171,`section`,37)(172,`mat-card`,39)(173,`mat-card-header`)(174,`mat-card-title`),u(175,`Run artifacts`),d(),U(176,`span`,5),c(177,`button`,50),C(`click`,function(){return i.store.refreshArtifacts()}),u(178,`Refresh`),d()(),c(179,`mat-card-content`,51),de(180,a2,10,4,`a`,52,lE,!1,o2,2,0,`p`,32),d()()()()()()),e&2){let a,o,r=ft(12),s=ft(30),l=cl(i.store.status());m(7),G(`live`,i.store.syncState()===`live`),m(),w(i.store.connectionLabel()),m(),S(`matMenuTriggerFor`,r)(`matBadge`,i.store.unseenAlertCount())(`matBadgeHidden`,i.store.unseenAlertCount()===0),m(4),ue(i.store.alerts().slice(0,10)),m(3),D(i.store.alerts().length>0?16:-1),m(),D(l?17:-1),m(2),D((a=i.store.latestError())?19:-1,a),m(),D((o=l?.failure)?20:-1,o),m(),S(`selectedIndex`,i.selectedTab()),m(6),S(`matMenuTriggerFor`,s),m(4),ue(i.panels),m(10),w(l?.evaluations?.running??0),m(5),w(l?.evaluations?.succeeded??0),m(5),w(l?.evaluations?.rejected??0),m(5),w(l?.evaluations?.failed??0),m(5),w(l?.evaluations?.slow??0),m(2),S(`model`,i.store.evaluationCostChart()),m(2),S(`model`,i.store.eliteRankChart()),m(3),ue(i.store.elites()?.elites??nt(43,Vm)),m(8),w(l?.runId??`—`),m(4),w(l?.mode??`—`),m(4),w(i.elapsedLabel(l?.elapsedMillis??null)),m(4),w(l?.trainingInstanceCount??`Preparing…`),m(4),w(l?.generatedParameterCount??0),m(4),w(i.metricLabel()),m(4),w(ke(101,40,i.store.lastUpdated(),`mediumTime`)),m(2),D(i.store.searchSpace()?102:-1),m(9),S(`model`,i.store.evaluationActivityChart()),m(20),ue(i.store.instanceSummaries()),m(8),S(`evaluations`,i.store.evaluations())(`forceIssues`,i.forceIssues()),m(9),D(i.store.elites()?.finalSnapshot?148:149),m(4),ue(i.store.elites()?.elites??nt(44,Vm)),m(8),S(`model`,i.store.eliteRankChart()),m(6),ue(i.store.eliteHistory()?.iterations??nt(45,Vm)),m(3),D(l?.mode===`AUTOCONFIG`?169:-1),m(11),ue(i.store.artifacts()?.artifacts??nt(46,Vm))}},dependencies:[Ai,Rm,Ww,Gw,Ft,nn,zn,Bn,jn,Ta,ju,Ma,am,Ul,im,ls,Ni,la,ss,ds,cs,vm,Xl,Zl,bm,ym,_m,ps,Lm,Xi,En],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh;color:var(--%NS%mat-sys-on-surface);background:radial-gradient(circle at 92% -5%,color-mix(in srgb,var(--%NS%mat-sys-tertiary) 14%,transparent),transparent 28rem),var(--%NS%mat-sys-surface-container-lowest)}.topbar[_ngcontent-%COMP%]{position:sticky;top:0;z-index:20;gap:.75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:color-mix(in srgb,var(--%NS%mat-sys-surface) 94%,transparent);-webkit-backdrop-filter:blur(12px);backdrop-filter:blur(12px)}.brand[_ngcontent-%COMP%]{display:flex;gap:.65rem;align-items:center;font:var(--%NS%mat-sys-title-large);font-weight:750;letter-spacing:-.025em}.brand[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:2rem;height:2rem}.toolbar-spacer[_ngcontent-%COMP%]{flex:1}mat-chip.live[_ngcontent-%COMP%]{--%NS%mdc-chip-elevated-container-color: color-mix(in srgb, var(--%NS%mat-sys-tertiary) 20%, transparent)}.alerts-button[_ngcontent-%COMP%]{margin-right:.5rem}.alert-item[_ngcontent-%COMP%]{display:grid;width:min(24rem,72vw);line-height:1.25}.alert-item[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{overflow:hidden;color:var(--%NS%mat-sys-on-surface-variant);text-overflow:ellipsis}.critical[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}.menu-empty[_ngcontent-%COMP%]{display:block;padding:1rem;color:var(--%NS%mat-sys-on-surface-variant)}.budget-ribbon[_ngcontent-%COMP%]{position:sticky;top:4rem;z-index:15;display:grid;grid-template-columns:minmax(15rem,auto) minmax(12rem,1fr) auto;gap:1.5rem;align-items:center;padding:.65rem max(1rem,(100% - 96rem) / 2);border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:color-mix(in srgb,var(--%NS%mat-sys-surface-container-low) 96%,transparent);-webkit-backdrop-filter:blur(12px);backdrop-filter:blur(12px)}.budget-copy[_ngcontent-%COMP%]{display:flex;gap:.55rem;align-items:baseline;white-space:nowrap}.budget-copy[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-body-small)}.eyebrow[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-small);text-transform:uppercase;letter-spacing:.07em}.phase[_ngcontent-%COMP%]{display:flex;gap:.4rem;align-items:center;font:var(--%NS%mat-sys-label-large);white-space:nowrap}.phase-dot[_ngcontent-%COMP%]{width:.6rem;height:.6rem;border-radius:50%;background:var(--%NS%mat-sys-tertiary);box-shadow:0 0 0 .2rem color-mix(in srgb,var(--%NS%mat-sys-tertiary) 18%,transparent)}.phase-dot.complete[_ngcontent-%COMP%]{background:#079447}main[_ngcontent-%COMP%]{width:min(100% - 2rem,96rem);margin:0 auto;padding-bottom:4rem}.request-error[_ngcontent-%COMP%], .run-error[_ngcontent-%COMP%]{display:flex;gap:.75rem;align-items:center;margin:1rem 0 0;padding:.75rem 1rem;border:1px solid var(--%NS%mat-sys-error);border-radius:.75rem;color:var(--%NS%mat-sys-on-error-container);background:var(--%NS%mat-sys-error-container)}.request-error[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{margin-left:auto}.tab-page[_ngcontent-%COMP%]{display:grid;gap:1rem;padding:1.25rem 0}.overview[_ngcontent-%COMP%]{grid-template-columns:repeat(2,minmax(0,1fr))}.wide[_ngcontent-%COMP%], .overview-actions[_ngcontent-%COMP%]{grid-column:1/-1}.overview-actions[_ngcontent-%COMP%]{display:flex;align-items:center;min-height:2rem;color:var(--%NS%mat-sys-on-surface-variant)}.overview-actions[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{margin-left:auto}.kpi-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(5,minmax(7rem,1fr));gap:.75rem}.kpi[_ngcontent-%COMP%]{display:grid;gap:.2rem;padding:.8rem 1rem;border-left:.25rem solid var(--%NS%mat-sys-primary);border-radius:.4rem;background:var(--%NS%mat-sys-surface-container-low)}.kpi[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-medium)}.kpi[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-headline-small)}.kpi.success[_ngcontent-%COMP%]{border-color:#079447}.kpi.warning[_ngcontent-%COMP%]{border-color:#ed8e00}.kpi.danger[_ngcontent-%COMP%]{border-color:var(--%NS%mat-sys-error)}.elite-list[_ngcontent-%COMP%]{display:grid;gap:.5rem}.elite-row[_ngcontent-%COMP%]{display:grid;grid-template-columns:2.3rem minmax(0,1fr);gap:.75rem;align-items:center;width:100%;padding:.65rem;border:1px solid transparent;border-radius:.5rem;color:inherit;background:transparent;text-align:left;cursor:pointer}.elite-row[_ngcontent-%COMP%]:hover{border-color:var(--%NS%mat-sys-outline-variant);background:var(--%NS%mat-sys-surface-container-low)}.elite-row[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{display:grid;min-width:0}.elite-row[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{overflow:hidden;color:var(--%NS%mat-sys-on-surface-variant);text-overflow:ellipsis;white-space:nowrap}.medal[_ngcontent-%COMP%]{display:grid;place-items:center;width:2rem;height:2rem;border-radius:50%;color:#292929;background:#d1d5db;font-weight:750}.rank-1[_ngcontent-%COMP%]{background:#f2c94c}.rank-2[_ngcontent-%COMP%]{background:#c7ccd4}.rank-3[_ngcontent-%COMP%]{background:#cd8b55}.rank-other[_ngcontent-%COMP%]{background:#e5e7eb}.run-info[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(8rem,auto) minmax(0,1fr);gap:.55rem 1rem;margin:0 0 1rem}.run-info[_ngcontent-%COMP%]   dt[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.run-info[_ngcontent-%COMP%]   dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}.two-column[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem}mat-card-header[_ngcontent-%COMP%]{align-items:center;padding-bottom:.75rem}.summary-scroll[_ngcontent-%COMP%]{max-height:21rem;overflow:auto}.summary-table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse}.summary-table[_ngcontent-%COMP%]   th[_ngcontent-%COMP%], .summary-table[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]{padding:.55rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left}.evaluation-card[_ngcontent-%COMP%]   mat-card-content[_ngcontent-%COMP%]{min-width:0}.history-list[_ngcontent-%COMP%]{display:grid;gap:.5rem}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{display:grid;grid-template-columns:1fr repeat(3,auto);gap:1rem;padding:.75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.artifact-list[_ngcontent-%COMP%]{display:grid}.artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(0,1fr) auto auto;gap:1.5rem;align-items:center;padding:1rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);color:var(--%NS%mat-sys-primary);text-decoration:none}.artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:first-child{display:grid}.artifact-list[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.empty-state[_ngcontent-%COMP%]{padding:1.5rem 0;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}@media(max-width:62rem){.overview[_ngcontent-%COMP%], .two-column[_ngcontent-%COMP%]{grid-template-columns:1fr}.kpi-grid[_ngcontent-%COMP%]{grid-template-columns:repeat(3,1fr)}.budget-ribbon[_ngcontent-%COMP%]{grid-template-columns:1fr auto}.budget-ribbon[_ngcontent-%COMP%]   mat-progress-bar[_ngcontent-%COMP%]{grid-column:1/-1;grid-row:2}}@media(max-width:42rem){.topbar[_ngcontent-%COMP%]   mat-chip-set[_ngcontent-%COMP%]{display:none}.brand[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-medium)}.budget-ribbon[_ngcontent-%COMP%]{top:3.5rem;gap:.5rem}.budget-copy[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child, .eyebrow[_ngcontent-%COMP%]{display:none}main[_ngcontent-%COMP%]{width:min(100% - 1rem,96rem)}.kpi-grid[_ngcontent-%COMP%]{grid-template-columns:repeat(2,1fr)}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%], .artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{grid-template-columns:1fr;gap:.2rem}}`]})};var jm=class t{http=f(Di);destroy=f(Ae);base=`${In.apiBaseUrl}/api/autollm`;get(n,e={}){return wt(this.http.get(this.base+n,{params:e}).pipe(up(1e4),hn(this.destroy)))}post(n,e={}){return wt(this.http.post(this.base+n,e).pipe(hn(this.destroy)))}defaults(){return this.get(`/defaults`)}validate(n){return this.post(`/validate`,n)}probeTarget(n){return this.post(`/targets/probe`,n)}list(){return this.get(`/campaigns`)}create(n){return this.post(`/campaigns`,n)}baselineCheck(n){return this.post(`/campaigns/${encodeURIComponent(n)}/baseline-check`)}action(n,e,i=[]){return this.post(`/campaigns/${encodeURIComponent(n)}/${e}`,e===`start`?{targets:i}:{})}addTarget(n,e){return wt(this.http.put(`${this.base}/campaigns/${encodeURIComponent(n)}/targets/${encodeURIComponent(e.id)}`,e).pipe(hn(this.destroy)))}targetAction(n,e,i){let a=`/campaigns/${encodeURIComponent(n)}/targets/${encodeURIComponent(e)}`;return i===`remove`?wt(this.http.delete(this.base+a).pipe(hn(this.destroy))):this.post(`${a}/${i}`)}progress(n,e){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/progress`)}recovery(n){return this.get(`/campaigns/${encodeURIComponent(n)}/recovery`)}resumeTrial(n,e){return this.post(`/campaigns/${encodeURIComponent(n)}/trials/${e}/resume`)}failTrial(n,e,i){return this.post(`/campaigns/${encodeURIComponent(n)}/trials/${e}/fail`,{reason:i})}summary(n){return this.get(`/campaigns/${encodeURIComponent(n)}/summary`)}activity(n,e,i,a){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/activity`,{attempt:i,cursor:a})}comparison(n,e){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/comparison`)}curves(n,e,i){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/curves`,{cursor:i})}artifacts(n){return this.get(`/campaigns/${encodeURIComponent(n)}/artifacts`)}preview(n,e,i){return this.get(`/campaigns/${encodeURIComponent(n)}/preview`,{path:e,lines:i})}artifactUrl(n,e){return`${this.base}/campaigns/${encodeURIComponent(n)}/artifact?${new URLSearchParams({path:e})}`}screenings(){return this.get(`/screenings`)}createScreening(n,e,i){return this.post(`/screenings`,{config:n,inputPath:e,trainingFraction:i,settings:{sotaFile:n.sotaFile,python:n.execution.ssh?.servers.find(a=>a.id===n.execution.ssh?.screeningServerId)?.environment?.MORK_SELECTOR_PYTHON??`python3`}})}screeningAction(n,e,i){return this.post(`/screenings/${encodeURIComponent(n)}/${e}`,{proposalSha256:i})}screeningReview(n){return this.get(`/screenings/${encodeURIComponent(n)}/review`)}screeningArtifactUrl(n,e){return`${this.base}/screenings/${encodeURIComponent(n)}/artifact?${new URLSearchParams({path:e})}`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var zm=class t{client=f(jm);destroy=f(Ae);config=T(null);campaigns=T([]);active=T(``);selectedId=T(``);selectedTrial=T(1);startTargets=T([]);curveCursor=0;campaign=K(()=>this.campaigns().find(n=>n.id===this.selectedId())??null);trial=K(()=>this.campaign()?.trials.find(n=>n.number===this.selectedTrial())??null);completed=K(()=>this.campaign()?.trials.filter(n=>[`SUCCEEDED`,`FAILED`].includes(n.status)).length??0);progress=T(null);activeProgress=T({});targets=K(()=>Object.entries(this.campaign()?.targets??{}).map(([n,e])=>r({id:n},e)));activeTrials=K(()=>this.campaign()?.trials.filter(n=>![`PENDING`,`SUCCEEDED`,`FAILED`].includes(n.status))??[]);now=T(Date.now());tuning=K(()=>this.progress()?.workload?.budget?this.progress().workload:null);generation=K(()=>this.progress()?.generation??null);generationAttempt=T(null);conversationAttempt=K(()=>{let n=this.trial()?.attempts?.filter(e=>[`GENERATION`,`REPAIR`].includes(e.stage))??[];return n.find(e=>e.number===this.generationAttempt())??n.at(-1)??null});activity=T([]);screenings=T([]);activeScreening=T(``);selectedScreening=T(``);screening=K(()=>this.screenings().find(n=>n.id===this.selectedScreening())??null);review=T(null);summary=T(null);comparison=T(null);artifacts=T([]);logPath=T(``);logLines=T(500);logFollow=T(!0);logPreview=T(null);logLoading=T(!1);logError=T(null);error=T(null);connectionError=T(null);busy=T(!1);targetStatus=T(null);recovery=T(null);recoveryError=T(null);trialRecovery=K(()=>this.recovery()?.trials[this.selectedTrial()]);timer=null;started=!1;destroyed=!1;logRequest=0;activityKey=``;activityCursor=0;activityPending=``;operations=0;constructor(){this.destroy.onDestroy(()=>{this.destroyed=!0,this.timer&&clearTimeout(this.timer)})}async start(){if(!this.started){this.started=!0;try{this.config.set(await this.client.defaults())}catch(n){this.connectionError.set(this.message(n))}await this.poll()}}async poll(){if(!this.destroyed)try{this.config()||this.config.set(await this.client.defaults());let[n,e]=await Promise.allSettled([this.client.list(),this.client.screenings()]);if(this.destroyed)return;if(this.connectionError.set(null),n.status===`fulfilled`){let s=n.value;this.campaigns.set(s.campaigns),this.active.set(s.active),this.selectedId()||this.selectedId.set(s.active||s.campaigns[0]?.id||``)}else this.connectionError.set(this.message(n.reason));if(e.status===`fulfilled`){let s=e.value;this.screenings.set(s.jobs),this.activeScreening.set(s.active),this.selectedScreening()||this.selectedScreening.set(s.active||s.jobs[0]?.id||``)}else this.connectionError.set(this.message(e.reason));this.now.set(Date.now());let i=this.selectedId(),a=this.activeTrials(),[o]=await Promise.all([Promise.allSettled(a.map(s=>this.client.progress(i,s.number))),this.refreshRecovery()]);if(this.selectedId()===i&&!this.destroyed){let s={};o.forEach((l,h)=>{let p=a[h].number;s[p]=l.status===`fulfilled`?{progress:l.value,stale:!1}:{progress:this.activeProgress()[p]?.progress,stale:!0}}),this.activeProgress.set(s)}if(this.trial()){let s$1=this.selectedId(),l=this.selectedTrial(),[h,p]=await Promise.allSettled([this.activeProgress()[l]?.progress??this.client.progress(s$1,l),this.client.comparison(s$1,l)]);this.selectedId()===s$1&&this.selectedTrial()===l&&!this.destroyed&&(h.status===`fulfilled`&&this.progress.set(h.value),p.status===`fulfilled`&&this.comparison.update(g=>s(r({},p.value),{curves:g?.curves??[]})))}let r$5=this.conversationAttempt();if(r$5){let s=this.selectedId(),l=this.selectedTrial(),h=`${s}/${l}/${r$5.number}`;this.activityKey!==h&&(this.activityKey=h,this.activityCursor=0,this.activityPending=``,this.activity.set([]));let p=this.activityCursor,g=await this.client.activity(s,l,r$5.number,p);if(this.activityKey===h&&this.selectedId()===s&&this.selectedTrial()===l&&this.conversationAttempt()?.number===r$5.number&&!this.destroyed){g.reset&&(this.activity.set([]),this.activityPending=``);let _=(this.activityPending+g.text).split(`
`);this.activityPending=_.pop()??``;let b=[];for(let y of _)if(y.trim())try{b.push(JSON.parse(y))}catch{}this.activity.update(y=>[...y,...b].slice(-200)),this.activityCursor=g.nextCursor}}this.logFollow()&&await this.refreshLog()}catch(n){this.destroyed||this.connectionError.set(this.message(n))}finally{this.destroyed||(this.timer=setTimeout(()=>{this.poll()},2e3))}}select(n){this.selectedId.set(n),this.startTargets.set([]),this.curveCursor=0,this.selectedTrial.set(1),this.artifacts.set([]),this.recovery.set(null),this.recoveryError.set(null),this.selectLog(``),this.progress.set(null),this.comparison.set(null),this.watchGeneration(null)}selectTrial(n){this.selectedTrial.set(n),this.curveCursor=0,this.progress.set(null),this.comparison.set(null),this.selectLog(``),this.watchGeneration(null)}watchGeneration(n){this.generationAttempt.set(n),this.activityKey=``,this.activity.set([])}selectLog(n){this.logRequest++,this.logPath.set(n),this.logPreview.set(null),this.logError.set(null),this.logLoading.set(!1),n&&this.refreshLog()}setLogLines(n){!Number.isInteger(n)||n<1||n>5e3||(this.logLines.set(n),this.selectLog(this.logPath()))}async refreshLog(){let n=this.selectedId(),e=this.logPath(),i=this.logLines();if(!n||!e||this.logLoading()||this.destroyed)return;let a=++this.logRequest;this.logLoading.set(!0),this.logError.set(null);try{let o=await this.client.preview(n,e,i);if(a===this.logRequest&&n===this.selectedId()&&!this.destroyed){let r=this.logPreview();(r?.text!==o.text||r?.sizeBytes!==o.sizeBytes)&&this.logPreview.set(o)}}catch(o){a===this.logRequest&&!this.destroyed&&this.logError.set(this.message(o))}finally{a===this.logRequest&&!this.destroyed&&this.logLoading.set(!1)}}selectScreening(n){this.selectedScreening.set(n),this.review.set(null)}async createScreening(n,e){let i=this.config();i&&await this.operate(async()=>{let a=await this.client.createScreening(i,n,e);this.screenings.update(o=>[a,...o]),this.selectScreening(a.id),await this.client.screeningAction(a.id,`resume`),this.activeScreening.set(a.id)})}async reviewScreening(){let n=this.selectedScreening();await this.operate(async()=>{let e=await this.client.screeningReview(n);this.selectedScreening()===n&&this.review.set(s(r({},e),{jobId:n}))})}async screeningAction(n){let e=this.selectedScreening(),i=this.review();n===`approve`&&i?.jobId!==e||await this.operate(async()=>{await this.client.screeningAction(e,n,i?.proposalSha256)})}useSelection(){let n=this.screening();n?.status!==`APPROVED`||!n.mirror||this.config.update(e=>e&&s(r({},e),{trainingPath:`${n.mirror}/proposal/train.index`,comparisonPath:`${n.mirror}/proposal/test.index`}))}screeningArtifactUrl(n){return this.client.screeningArtifactUrl(this.selectedScreening(),n)}async refreshArtifacts(){if(!this.selectedId())return;let n=this.selectedId();try{let e=await this.client.artifacts(n);n===this.selectedId()&&!this.destroyed&&this.artifacts.set(e)}catch(e){this.error.set(this.message(e))}}async createAndStart(){let n=this.config();n&&await this.operate(async()=>{let e=await this.client.validate(n);if(!e.valid)throw new Error(e.errors.join(`; `));let i=await this.client.create(n);this.campaigns.update(o=>[i,...o]),this.select(i.id);let a=await this.client.baselineCheck(i.id);if(this.summary.set(await this.client.summary(i.id)),!a.withinLimit)throw new Error(`Baseline emits ${a.parameters} parameters; limit is ${a.maxParameters}. Prune the search space while preserving every seed before starting this draft.`);if(!a.ready)throw new Error(`Baseline is not ready. Failed targets: ${a.failedTargets.join(`, `)}. Inspect qualification results before selecting servers to start.`);await this.client.action(i.id,`start`),this.active.set(i.id)})}async probeTarget(){let n=this.config();n&&await this.operate(async()=>{this.targetStatus.set(await this.client.probeTarget(n))})}async action(n,e=[]){await this.operate(async()=>{let i=this.selectedId(),a=await this.client.action(i,n,e);this.updateCampaign(a),(n===`start`||n===`resume`)&&this.active.set(i),await this.refreshRecovery()})}toggleStartTarget(n,e){this.startTargets.update(i=>e?[...i,n]:i.filter(a=>a!==n))}async loadCurves(){let n=this.selectedId(),e=this.selectedTrial();await this.operate(async()=>{let i=await this.client.curves(n,e,this.curveCursor);n===this.selectedId()&&e===this.selectedTrial()&&(this.comparison.update(a=>s(r({},a),{curves:i.curves})),this.curveCursor=i.nextCursor)})}async addTarget(n){let e=this.selectedId();await this.operate(async()=>{let i=await this.client.addTarget(e,n);this.campaigns.update(a=>a.map(o=>o.id===e?i:o))})}async targetAction(n,e){let i=this.selectedId();await this.operate(async()=>{this.updateCampaign(await this.client.targetAction(i,n,e)),await this.refreshRecovery()})}async refreshRecovery(){let n=this.selectedId();if(n)try{let e=await this.client.recovery(n);n===this.selectedId()&&!this.destroyed&&(this.recovery.set(e),this.recoveryError.set(null))}catch(e){n===this.selectedId()&&!this.destroyed&&(this.recovery.set(null),this.recoveryError.set(this.message(e)))}}async resumeTrial(){let n=this.selectedId(),e=this.selectedTrial();await this.operate(async()=>{this.updateCampaign(await this.client.resumeTrial(n,e)),await this.refreshRecovery()})}async failTrial(n){if(!n.trim())return;let e=this.selectedId(),i=this.selectedTrial();await this.operate(async()=>{this.updateCampaign(await this.client.failTrial(e,i,n.trim())),await this.refreshRecovery()})}updateCampaign(n){this.campaigns.update(e=>e.map(i=>i.id===n.id?n:i))}targetStale(n){return!n||this.now()-Date.parse(n)>3e4}async operate(n){this.operations++,this.busy.set(!0),this.error.set(null);try{await n()}catch(e){this.error.set(this.message(e))}finally{this.operations--,this.busy.set(this.operations>0)}}artifactUrl(n){return this.client.artifactUrl(this.selectedId(),n)}message(n){let e=n;return e.error?.message??e.message??`AutoLLM request failed`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var ic=class{static palettes=[[`#1565c0`,`#1976d2`,`#1e88e5`,`#0277bd`,`#039be5`,`#0288d1`],[`#ef6c00`,`#f4511e`,`#e64a19`,`#e53935`,`#d84315`,`#c62828`],[`#2e7d32`,`#388e3c`,`#00897b`,`#00796b`],[`#7b1fa2`,`#8e24aa`,`#6a1b9a`,`#5e35b1`]];static canUseLogarithmicScale(n,e){if(!e)return!1;let i=!1;for(let a of n)for(let o of a.curves[e]??[])if(!(!Number.isFinite(o.value)||!Number.isFinite(o.instant))){if(o.value<=0)return!1;i=!0}return i}static build(n,e,i){let a=[...new Set([`SOTA`,`IraceProposal`,...n.map(s=>s.algorithm)])],o=[];for(let s=0;s<a.length;s++){let l=a[s],h=this.palettes[s%this.palettes.length],p=`comparison-${encodeURIComponent(l)}`,g=n.filter(b=>b.algorithm===l).sort((b,y)=>b.iteration.localeCompare(y.iteration,void 0,{numeric:!0})),_=!1;for(let b of g){let y=(b.curves[e??``]??[]).filter(L=>Number.isFinite(L.value)&&Number.isFinite(L.instant)).map(L=>[L.instant/1e6,L.value]);if(!y.length)continue;let I=Number(b.iteration),P=Number.isFinite(I)?I+1:b.iteration,ie=Number.isInteger(I)&&I>=0?I:0;o.push({type:`line`,id:_?`${p}-repetition-${b.iteration}`:p,linkedTo:_?p:void 0,showInLegend:!_,name:`${l} \xB7 repetition ${P}`,custom:{algorithm:l},color:h[ie%h.length],dashStyle:`Solid`,opacity:.45,lineWidth:1.5,marker:{enabled:!1},states:{hover:{lineWidthPlus:1.5,opacity:.85},inactive:{opacity:.15}},data:y}),_=!0}}let r=i===`logarithmic`&&this.canUseLogarithmicScale(n,e)?`logarithmic`:`linear`;return{viewKey:JSON.stringify([n[0]?.instance,e,r]),options:{boost:{enabled:!1},chart:{type:`line`,animation:!1,reflow:!0,zooming:{type:`xy`,pinchType:`xy`},panning:{enabled:!0,type:`xy`},panKey:`shift`},title:{text:e??`Comparison convergence`},xAxis:{title:{text:`Time (milliseconds)`},minRange:0},yAxis:{title:{text:`Objective value`},type:r,minRange:0,startOnTick:!1,endOnTick:!1},legend:{maxHeight:64,symbolWidth:32,labelFormatter(){let s=this.options.custom?.algorithm;return typeof s==`string`?s:this.name??``}},plotOptions:{series:{animation:!1}},series:o}}}};var Hm=class{static relativeLabels(n){if(!n.length)return new Map;let e=n[0].replaceAll(`\\`,`/`);for(let o of n.slice(1)){let r=o.replaceAll(`\\`,`/`),s=0;for(;s<e.length&&e[s]===r[s];)s++;e=e.slice(0,s)}let i=e.lastIndexOf(`/`)+1,a=new Map;for(let o of n)a.set(o,o.replaceAll(`\\`,`/`).slice(i));return a}};var Um=class{static canPreview(n){return/\.(log|jsonl|txt|json|csv|tsv|md|xml|ya?ml|properties|index|r|rout|out|err)$/i.test(n)}};var dE=()=>[];var r2=()=>[`DRAFT`,`INTERRUPTED`,`CANCELLED`,`NEEDS_ATTENTION`];var s2=()=>[`AWAITING_APPROVAL`,`APPROVED`];var l2=()=>[`instances.csv`,`proposed-retained.index`,`proposed-train.index`,`proposed-test.index`,`manifest.json`];var c2=()=>[`INTERRUPTED`,`CANCELLED`,`NEEDS_ATTENTION`,`WAITING_FOR_TARGETS`];var d2=()=>[`SUCCEEDED`,`COMPLETED_WITH_FAILURES`];var u2=()=>[`BUSY`,`QUALIFYING`];var m2=()=>[`BLOCKED`,`INTERRUPTED`];var p2=()=>[`GENERATION`,`REPAIR`];var Gm=(t,n)=>n.id;var h2=(t,n)=>n.instance;var Wb=(t,n)=>n.number;var f2=(t,n)=>n.path;var g2=(t,n)=>n.threadId;function b2(t,n){if(t&1&&(c(0,`p`,1),u(1),d()),t&2){let e=v();m(),w(e.store.error())}}function v2(t,n){if(t&1&&(c(0,`p`,1),u(1),d()),t&2){let e=v();m(),w(e.store.connectionError())}}function _2(t,n){if(t&1&&(c(0,`option`,4),u(1),d()),t&2){let e=n.$implicit;S(`value`,e.id),m(),w(e.id)}}function y2(t,n){if(t&1){let e=$();c(0,`fieldset`,25)(1,`legend`),u(2),d(),c(3,`label`),u(4,`ID`),c(5,`input`,2),C(`change`,function(a){let o=k(e).$index;return M(v(3).update(`execution.ssh.servers.`+o+`.id`,a))}),d()(),c(6,`label`),u(7,`SSH host or alias`),c(8,`input`,2),C(`change`,function(a){let o=k(e).$index;return M(v(3).update(`execution.ssh.servers.`+o+`.host`,a))}),d()(),c(9,`label`),u(10,`Remote workspace`),c(11,`input`,2),C(`change`,function(a){let o=k(e).$index;return M(v(3).update(`execution.ssh.servers.`+o+`.workspace`,a))}),d()(),c(12,`label`),u(13,`Java executable`),c(14,`input`,2),C(`change`,function(a){let o=k(e).$index;return M(v(3).update(`execution.ssh.servers.`+o+`.javaExecutable`,a))}),d()(),c(15,`label`),u(16,`Workers override (blank inherits)`),c(17,`input`,12),C(`change`,function(a){let o=k(e).$index;return M(v(3).update(`execution.ssh.servers.`+o+`.workers`,a,!0))}),d()(),c(18,`label`),u(19,`Heap override (blank inherits)`),c(20,`input`,2),C(`change`,function(a){let o=k(e).$index;return M(v(3).update(`execution.ssh.servers.`+o+`.heap`,a))}),d()(),c(21,`button`,26),C(`click`,function(){let a=k(e).$index;return M(v(3).removeServer(a))}),u(22,`Remove from configuration`),d()()}if(t&2){let e=n.$implicit,i=n.$index;m(2),B(`Server `,i+1),m(3),S(`value`,e.id),m(3),S(`value`,e.host),m(3),S(`value`,e.workspace),m(3),S(`value`,e.javaExecutable),m(3),S(`value`,e.workers??``),m(3),S(`value`,e.heap??``)}}function x2(t,n){if(t&1){let e=$();c(0,`label`),u(1,`Screening server`),c(2,`select`,2),C(`change`,function(a){k(e);return M(v(2).update(`execution.ssh.screeningServerId`,a))}),de(3,_2,2,2,`option`,4,wo),d()(),c(5,`label`),u(6,`Local remote-runner JAR`),c(7,`input`,2),C(`change`,function(a){k(e);return M(v(2).update(`execution.ssh.runnerArtifact`,a))}),d()(),de(8,y2,23,7,`fieldset`,25,wo),c(10,`button`,26),C(`click`,function(){k(e);return M(v(2).addServer())}),u(11,`Add server`),d()}if(t&2){let e=v();m(2),S(`value`,e.execution.ssh?.screeningServerId),m(),ue(e.execution.ssh?.servers??nt(2,dE)),m(4),S(`value`,e.execution.ssh?.runnerArtifact??``),m(),ue(e.execution.ssh?.servers??nt(3,dE))}}function C2(t,n){if(t&1){let e=$();c(0,`label`),u(1,`Worker count`),c(2,`input`,11),C(`change`,function(a){k(e);return M(v(2).update(`execution.workers`,a,!0))}),d()()}if(t&2){let e=v();m(2),S(`value`,e.execution.workers)}}function S2(t,n){if(t&1&&(c(0,`details`,20)(1,`summary`),u(2,`Resolved budgets and workload estimate`),d(),c(3,`pre`),u(4),j(5,`json`),d()()),t&2){let e=v(2);m(4),w(_e(5,1,e.store.summary()))}}function w2(t,n){if(t&1&&(c(0,`pre`),u(1),j(2,`json`),d()),t&2){let e=v(2);m(),w(_e(2,1,e.store.targetStatus()))}}function E2(t,n){if(t&1){let e=$();c(0,`mat-card`)(1,`mat-card-content`)(2,`details`,5)(3,`summary`),u(4,`Configure a campaign`),d(),c(5,`fieldset`,6)(6,`div`,7)(7,`label`),u(8,`Where should the actual workloads run?`),c(9,`select`,2),C(`change`,function(a){k(e);return M(v().executionTarget(a))}),c(10,`option`,8),u(11,`Select execution location`),d(),c(12,`option`,9),u(13,`Locally on this computer`),d(),c(14,`option`,10),u(15,`Configure SSH servers`),d()()(),E(16,x2,12,4),c(17,`label`),u(18,`Baseline repository`),c(19,`input`,2),C(`change`,function(a){k(e);return M(v().update(`project.repository`,a))}),d()(),c(20,`label`),u(21,`Git ref`),c(22,`input`,2),C(`change`,function(a){k(e);return M(v().update(`project.ref`,a))}),d()(),c(23,`label`),u(24,`Build command (JSON arguments)`),c(25,`input`,2),j(26,`json`),C(`change`,function(a){k(e);return M(v().buildCommand(a))}),d()(),c(27,`label`),u(28,`Built JAR path`),c(29,`input`,2),C(`change`,function(a){k(e);return M(v().update(`project.artifact`,a))}),d()(),c(30,`label`),u(31,`SOTA component JSON`),c(32,`input`,2),C(`change`,function(a){k(e);return M(v().update(`sotaFile`,a))}),d()(),c(33,`label`),u(34,`Additional seeds (optional)`),c(35,`input`,2),C(`change`,function(a){k(e);return M(v().update(`initialConfigurationsFile`,a))}),d()(),c(36,`label`),u(37,`Training instances`),c(38,`input`,2),C(`change`,function(a){k(e);return M(v().update(`trainingPath`,a))}),d()(),c(39,`label`),u(40,`Comparison instances`),c(41,`input`,2),C(`change`,function(a){k(e);return M(v().update(`comparisonPath`,a))}),d()(),c(42,`label`),u(43,`Independent trials`),c(44,`input`,11),C(`change`,function(a){k(e);return M(v().update(`trials`,a,!0))}),d()(),c(45,`label`),u(46,`Campaign seed`),c(47,`input`,12),C(`change`,function(a){k(e);return M(v().update(`seed`,a,!0))}),d()(),c(48,`label`),u(49,`Parameter limit`),c(50,`input`,13),C(`change`,function(a){k(e);return M(v().update(`maxParameters`,a,!0))}),d()(),c(51,`label`),u(52,`Workers`),c(53,`select`,2),C(`change`,function(a){k(e);return M(v().workerMode(a))}),c(54,`option`,14),u(55,`Auto: half the available processors on the execution host`),d(),c(56,`option`,15),u(57,`Set worker count`),d()()(),E(58,C2,3,1,`label`),c(59,`label`),u(60,`Main evaluations`),c(61,`input`,11),C(`change`,function(a){k(e);return M(v().update(`tuning.evaluations`,a,!0))}),d()(),c(62,`label`),u(63,`Comparison repetitions`),c(64,`input`,11),C(`change`,function(a){k(e);return M(v().update(`comparison.repetitions`,a,!0))}),d()(),c(65,`label`),u(66,`Probe duration (milliseconds)`),c(67,`input`,11),C(`change`,function(a){k(e);return M(v().update(`preparation.probeDurationMillis`,a,!0))}),d()(),c(68,`label`),u(69,`Pilot duration (milliseconds)`),c(70,`input`,11),C(`change`,function(a){k(e);return M(v().update(`preparation.pilotDurationMillis`,a,!0))}),d()(),c(71,`label`),u(72,`Repair turns`),c(73,`input`,16),C(`change`,function(a){k(e);return M(v().update(`preparation.maxRepairs`,a,!0))}),d()(),c(74,`label`),u(75,`Codex model (optional)`),c(76,`input`,2),C(`change`,function(a){k(e);return M(v().update(`codex.model`,a))}),d()(),c(77,`label`),u(78,`Concurrent Codex sessions`),c(79,`input`,11),C(`change`,function(a){k(e);return M(v().update(`codex.maxConcurrentSessions`,a,!0))}),d()(),c(80,`label`),u(81,`Reasoning effort (optional)`),c(82,`input`,2),C(`change`,function(a){k(e);return M(v().update(`codex.reasoningEffort`,a))}),d()(),c(83,`label`),u(84,`Custom prompt file (optional)`),c(85,`input`,2),C(`change`,function(a){k(e);return M(v().update(`codex.promptFile`,a))}),d()()(),c(86,`label`),u(87,`Prompt override (optional)`),c(88,`textarea`,17),C(`change`,function(a){k(e);return M(v().update(`codex.prompt`,a))}),d()(),c(89,`details`)(90,`summary`),u(91,`All settings, including time budgets and timeouts`),d(),c(92,`textarea`,18),j(93,`json`),C(`change`,function(a){k(e);return M(v().advanced(a))}),d()()(),c(94,`p`),u(95,`Each trial starts from the same baseline. Both comparison algorithms restart within their time budget. Ablation is disabled.`),d(),c(96,`p`,19),u(97),j(98,`number`),d(),E(99,S2,6,3,`details`,20),c(100,`details`)(101,`summary`),u(102,`Prepare a screened dataset`),d(),c(103,`div`,7)(104,`label`),u(105,`Full original input pool`),c(106,`input`,21),C(`input`,function(a){k(e);return M(v().originalPool.set(a.target.value))}),d()(),c(107,`label`),u(108,`Training fraction`),c(109,`input`,22),C(`input`,function(a){k(e);return M(v().trainingFraction.set(+a.target.value))}),d()()(),c(110,`p`),u(111,`Screening uses one sequential worker. Review the completed proposal before publishing training/testing indexes. Testing includes all retained instances, including training. Approval permits reuse by qualified servers; measurement provenance remains tied to the screening server.`),d(),c(112,`button`,23),C(`click`,function(){k(e);let a=v();return M(a.store.createScreening(a.originalPool(),a.trainingFraction()))}),u(113,`Prepare dataset`),d()(),c(114,`button`,23),C(`click`,function(){k(e);return M(v().store.probeTarget())}),u(115,`Check execution host`),d(),E(116,w2,3,3,`pre`),c(117,`button`,24),C(`click`,function(){k(e);return M(v().store.createAndStart())}),u(118,`Create and start campaign`),d()()()()}if(t&2){let e=n,i=v();m(2),S(`open`,!i.store.campaign()),m(3),S(`disabled`,!!i.store.active()||i.store.busy()),m(4),S(`value`,i.executionChoice()),m(7),D(e.execution.target===`SSH`?16:-1),m(3),S(`value`,e.project.repository),m(3),S(`value`,e.project.ref),m(3),S(`value`,_e(26,43,e.project.buildCommand)),m(4),S(`value`,e.project.artifact),m(3),S(`value`,e.sotaFile??``),m(3),S(`value`,e.initialConfigurationsFile??``),m(3),S(`value`,e.trainingPath??``),m(3),S(`value`,e.comparisonPath??``),m(3),S(`value`,e.trials),m(3),S(`value`,e.seed),m(3),S(`value`,e.maxParameters),m(3),S(`value`,e.execution.workers<1?`AUTO`:`CUSTOM`),m(5),D(e.execution.workers>0?58:-1),m(3),S(`value`,e.tuning.evaluations),m(3),S(`value`,e.comparison.repetitions),m(3),S(`value`,e.preparation.probeDurationMillis),m(3),S(`value`,e.preparation.pilotDurationMillis),m(3),S(`value`,e.preparation.maxRepairs),m(3),S(`value`,e.codex.model??``),m(3),S(`value`,e.codex.maxConcurrentSessions),m(3),S(`value`,e.codex.reasoningEffort??``),m(3),S(`value`,e.codex.promptFile??``),m(3),S(`value`,e.codex.prompt??``),m(4),S(`value`,_e(93,45,e)),m(5),nu(``,e.trials,` trials · `,_e(98,47,e.tuning.evaluations),` tuning evaluations per trial at `,(e.tuning.ignoreInitialMillis+e.tuning.intervalDurationMillis)/1e3,` seconds · `,e.comparison.repetitions,` paired comparison repetitions at `,e.comparison.timeLimitMillis>0?e.comparison.timeLimitMillis/1e3+` seconds`:`application time limits (resolved during baseline check)`,` · `,e.preparation.probeDurationMillis/6e4,` minute probe budget · `,e.preparation.pilotDurationMillis/6e4,` minute pilot · `,e.preparation.maxRepairs,` component repairs per trial.`),m(2),D(i.store.summary()?99:-1),m(7),S(`value`,i.originalPool()),m(3),S(`value`,i.trainingFraction()),m(3),S(`disabled`,!i.executionChoice()||!i.originalPool()||!!i.store.active()||!!i.store.activeScreening()||i.store.busy()),m(2),S(`disabled`,!i.executionChoice()||!!i.store.active()||i.store.busy()),m(2),D(i.store.targetStatus()?116:-1),m(),S(`disabled`,!i.executionChoice()||!!i.store.active()||!!i.store.activeScreening()||i.store.busy())}}function D2(t,n){if(t&1&&(c(0,`option`,4),u(1),j(2,`date`),d()),t&2){let e=n.$implicit;S(`value`,e.id),m(),Ot(``,ke(2,4,e.createdAt,`short`),` · `,e.status,` · `,e.id)}}function I2(t,n){if(t&1&&U(0,`mat-progress-bar`,30),t&2){let e=v();S(`value`,100*e.initialCompleted/e.initialTotal)}}function k2(t,n){if(t&1&&U(0,`mat-progress-bar`,30),t&2){let e=v();S(`value`,100*e.confirmationsCompleted/e.confirmationsTotal)}}function M2(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`date`),d(),E(3,I2,1,1,`mat-progress-bar`,30),E(4,k2,1,1,`mat-progress-bar`,30)),t&2){let e=n;m(),ll(`Initial measurements: `,e.initialCompleted,`/`,e.initialTotal,` · Confirmations: `,e.confirmationsCompleted,`/`,e.confirmationsTotal,` · Last activity: `,ke(2,7,e.updatedAt,`mediumTime`)),m(2),D(e.phase===`INITIAL`&&e.initialTotal?3:-1),m(),D(e.phase===`CONFIRMATIONS`&&e.confirmationsTotal?4:-1)}}function T2(t,n){if(t&1&&(c(0,`p`,31),u(1),d(),c(2,`p`),u(3),d()),t&2){let e=n;m(),Me(``,e.category,` · `,e.message),m(2),w(e.recovery)}}function N2(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`date`),d()),t&2){let e=n;m(),Ot(`Remote job `,e.jobId,` · `,e.status,` · Last sync: `,ke(2,3,e.lastSynchronizedAt,`mediumTime`))}}function A2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){k(e);return M(v(3).store.screeningAction(`resume`))}),u(1,`Resume preparation`),d()}if(t&2){let e=v(3);S(`disabled`,!!e.store.active()||!!e.store.activeScreening()||e.store.busy())}}function R2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){k(e);return M(v(3).store.screeningAction(`cancel`))}),u(1,`Cancel preparation`),d()}if(t&2)S(`disabled`,v(3).store.busy())}function O2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){k(e);return M(v(3).store.reviewScreening())}),u(1,`Review proposal`),d()}if(t&2)S(`disabled`,v(3).store.busy())}function P2(t,n){if(t&1){let e=$();c(0,`button`,26),C(`click`,function(){k(e);return M(v(3).store.useSelection())}),u(1,`Use approved selection`),d()}}function F2(t,n){if(t&1&&(c(0,`p`),u(1),d(),E(2,M2,5,10),E(3,T2,4,3),E(4,N2,3,6,`p`),c(5,`div`,27),E(6,A2,2,1,`button`,28),E(7,R2,2,1,`button`,28),E(8,O2,2,1,`button`,28),E(9,P2,2,0,`button`,29),d()),t&2){let e,i,a,o=n,r=v(2);m(),Me(``,o.status,` · `,o.progress?.phase||o.activity?.phase||`Waiting to start`),m(),D((e=o.progress)?2:-1,e),m(),D((i=o.problem)?3:-1,i),m(),D((a=o.activity?.remote)?4:-1,a),m(2),D(nt(9,r2).includes(o.status)?6:-1),m(),D(r.store.activeScreening()===o.id||o.status===`NEEDS_ATTENTION`?7:-1),m(),D(nt(10,s2).includes(o.status)?8:-1),m(),D(o.status===`APPROVED`?9:-1)}}function L2(t,n){if(t&1&&(c(0,`a`,33),u(1),d()),t&2){let e=n.$implicit,i=v();S(`href`,v(2).store.screeningArtifactUrl(i.artifactPrefix+`/`+e),dn),m(),w(e)}}function V2(t,n){if(t&1&&(c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),d(),c(7,`td`),u(8),d()()),t&2){let e=n.$implicit;m(2),w(e.instance),m(2),w(e.classification),m(2),Me(``,e.fastHits,`/`,e.confirmations),m(2),w(e.reason)}}function B2(t,n){if(t&1){let e=$();c(0,`button`,24),C(`click`,function(){k(e);return M(v(3).store.screeningAction(`approve`))}),u(1,`Approve this reviewed selection`),d()}if(t&2){let e=v(),i=v(2);S(`disabled`,i.store.busy()||!!i.store.active()||!!i.store.activeScreening()||!!e.proposal.splitFailure)}}function j2(t,n){if(t&1&&(c(0,`pre`,32),u(1),d(),c(2,`p`),u(3),d(),c(4,`div`,27),de(5,L2,2,2,`a`,33,Pn),d(),c(7,`details`)(8,`summary`),u(9,`All instance decisions`),d(),c(10,`table`)(11,`thead`)(12,`tr`)(13,`th`),u(14,`Instance`),d(),c(15,`th`),u(16,`Decision`),d(),c(17,`th`),u(18,`Fast confirmations`),d(),c(19,`th`),u(20,`Reason`),d()()(),c(21,`tbody`),de(22,V2,9,5,`tr`,null,h2),d()()(),E(24,B2,2,1,`button`,34)),t&2){let e=n,i=v(2);m(),w(e.report),m(2),B(`Reviewed proposal SHA-256: `,e.proposalSha256),m(2),ue(nt(3,l2)),m(17),ue(e.proposal.instances),m(2),D(i.store.screening()?.status===`AWAITING_APPROVAL`?24:-1)}}function z2(t,n){if(t&1){let e=$();c(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),u(3,`Dataset preparation`),d(),c(4,`label`),u(5,`Saved screening`),c(6,`select`,2),C(`change`,function(a){k(e);return M(v().store.selectScreening(a.target.value))}),de(7,D2,3,7,`option`,4,Gm),d()(),E(9,F2,10,11),E(10,j2,25,4),d()()}if(t&2){let e,i,a=v();m(6),S(`value`,a.store.selectedScreening()),m(),ue(a.store.screenings()),m(2),D((e=a.store.screening())?9:-1,e),m(),D((i=a.store.review())?10:-1,i)}}function H2(t,n){if(t&1&&(c(0,`option`,4),u(1),j(2,`date`),d()),t&2){let e=n.$implicit;S(`value`,e.id),m(),Ot(``,ke(2,4,e.createdAt,`short`),` — `,e.status,` — `,e.id)}}function U2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){k(e);return M(v(2).store.action(`cancel`))}),u(1,`Cancel campaign`),d()}if(t&2)S(`disabled`,v().status===`CANCELLATION_PENDING`)}function $2(t,n){if(t&1){let e=$();c(0,`button`,42),C(`click`,function(){k(e);return M(v(2).store.action(`resume`))}),u(1,`Resume from checkpoints`),d()}if(t&2){let e=v(2);S(`disabled`,e.store.busy()||!e.store.recovery()?.resumeCampaign?.allowed)(`title`,e.store.recovery()?.resumeCampaign?.reason||e.store.recovery()?.resumeCampaign?.effect||``)}}function G2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){k(e);return M(v(2).store.action(`cancel`))}),u(1,`Reconcile and cancel remote work`),d()}if(t&2)S(`disabled`,v(2).store.busy())}function W2(t,n){if(t&1){let e=$();c(0,`label`)(1,`input`,44),C(`change`,function(a){let o=k(e).$implicit;return M(v(3).store.toggleStartTarget(o.id,a.target.checked))}),d(),u(2),d()}if(t&2){let e=n.$implicit,i=v(3);m(),S(`checked`,i.store.startTargets().includes(e.id))(`disabled`,!!e.problem||!(e.baselineReady||e.status===`VALIDATED`)),m(),B(` `,e.id)}}function q2(t,n){if(t&1){let e=$();c(0,`button`,43),C(`click`,function(){k(e);return M(v(2).store.action(`start`))}),u(1,`Start all servers`),d(),de(2,W2,3,3,`label`,null,Gm),c(4,`button`,23),C(`click`,function(){k(e);let a=v(2);return M(a.store.action(`start`,a.store.startTargets()))}),u(5,`Start selected servers`),d()}if(t&2){let e=v(2);m(2),ue(e.store.targets()),m(2),S(`disabled`,!e.store.startTargets().length)}}function Y2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){k(e);return M(v(2).store.action(`report`))}),u(1,`Regenerate reports`),d()}if(t&2)S(`disabled`,v(2).store.busy())}function K2(t,n){if(t&1&&(c(0,`p`,31),u(1),d()),t&2){let e=v(2);m(),B(`Recovery actions unavailable: `,e.store.recoveryError())}}function Q2(t,n){if(t&1&&(c(0,`p`),u(1),d()),t&2){let e=n;m(),w(e.resumeCampaign.reason||e.resumeCampaign.effect)}}function X2(t,n){t&1&&(c(0,`strong`,31),u(1,`Status is stale`),d())}function Z2(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`date`),E(3,X2,2,0,`strong`,31),d()),t&2){let e=n,i=v().$implicit,a=v(2);m(),Me(``,e.status,` · Last sync `,ke(2,3,e.lastSynchronizedAt,`mediumTime`),` `),m(2),D(nt(6,u2).includes(i.status)&&a.store.targetStale(e.lastSynchronizedAt)?3:-1)}}function J2(t,n){if(t&1&&(c(0,`p`,31),u(1),d(),c(2,`p`),u(3),d()),t&2){let e=n;m(),Me(``,e.category,` · `,e.message),m(2),w(e.recovery)}}function eL(t,n){if(t&1&&(c(0,`p`),u(1),d()),t&2){v(2);let e=Ar(0);m(),w(e.requalify.reason||e.requalify.effect)}}function tL(t,n){if(t&1&&(c(0,`p`),u(1),d(),E(2,eL,2,1,`p`)),t&2){v();let e=Ar(0);m(),w(e.resume.reason||e.resume.effect),m(),D(e.requalify.allowed||e.requalify.reason!==e.resume.reason?2:-1)}}function nL(t,n){if(t&1){let e=$();Nr(0),c(1,`button`,23),C(`click`,function(){k(e);let a=v().$implicit;return M(v(2).store.targetAction(a.id,`drain`))}),u(2,`Drain`),d(),c(3,`button`,45),C(`click`,function(){k(e);let a=v().$implicit;return M(v(2).store.targetAction(a.id,`resume`))}),u(4,`Resume server`),d(),c(5,`button`,45),C(`click`,function(){k(e);let a=v().$implicit;return M(v(2).store.targetAction(a.id,`requalify`))}),u(6,`Requalify server`),d(),E(7,tL,3,2),c(8,`button`,23),C(`click`,function(){k(e);let a=v().$implicit;return M(v(2).store.targetAction(a.id,`remove`))}),u(9,`Remove idle server`),d()}if(t&2){let e=v().$implicit,i=v(2),a=cl(i.store.recovery()?.targets?.[e.id]);m(),S(`disabled`,i.store.busy()||e.draining),m(2),S(`disabled`,i.store.busy()||!a?.resume?.allowed)(`title`,a?.resume?.reason||a?.resume?.effect||``),m(2),S(`disabled`,i.store.busy()||!a?.requalify?.allowed)(`title`,a?.requalify?.reason||a?.requalify?.effect||``),m(2),D(a?7:-1),m(),S(`disabled`,i.store.busy()||!!e.assignedTrial||e.status===`QUALIFYING`)}}function iL(t,n){if(t&1&&(c(0,`article`,37)(1,`h3`),u(2),d(),c(3,`p`),u(4),d(),E(5,Z2,4,7,`p`),E(6,J2,4,3),E(7,nL,10,8),d()),t&2){let e,i,a=n.$implicit,o=v();m(2),Me(``,a.id,` · `,a.status),m(2),Ot(``,a.definition.host||`Local`,` · Trial `,a.assignedTrial??`—`,` · Workers `,a.host?.effectiveWorkers??`Pending qualification`),m(),D((e=a.remote)?5:-1,e),m(),D((i=a.problem)?6:-1,i),m(),D(o.config.execution.target===`SSH`&&a.status!==`REMOVED`?7:-1)}}function aL(t,n){if(t&1){let e=$();c(0,`details`)(1,`summary`),u(2,`Add a server to this campaign`),d(),c(3,`p`),u(4,`New servers qualify automatically while the campaign is active. Existing trial assignments stay on their original server.`),d(),c(5,`label`),u(6,`Server definition (JSON)`),c(7,`textarea`,46),C(`input`,function(a){k(e);return M(v(2).newTarget.set(a.target.value))}),d()(),c(8,`button`,23),C(`click`,function(){k(e);return M(v(2).enrollTarget())}),u(9,`Register server`),d()()}if(t&2){let e=v(2);m(7),S(`value`,e.newTarget()),m(),S(`disabled`,e.store.busy())}}function oL(t,n){t&1&&(c(0,`strong`,31),u(1,`Progress unavailable; showing last update`),d())}function rL(t,n){if(t&1&&u(0),t&2){let e=n;Ot(` · `,e.state,` · `,e.commandsCompleted,`/`,e.commandsStarted,` commands `)}}function sL(t,n){if(t&1){let e=$();c(0,`p`,38),u(1),E(2,oL,2,0,`strong`,31),E(3,rL,1,3),c(4,`button`,26),C(`click`,function(){let a=k(e).$implicit;return M(v(2).store.selectTrial(a.number))}),u(5,`Inspect`),d()()}if(t&2){let e,i=n.$implicit,a=v(2);m(),Ot(`Trial `,i.number,` · `,i.targetId,` · `,a.store.activeProgress()[i.number]?.progress?.substage||i.status,` `),m(),D(a.store.activeProgress()[i.number]?.stale?2:-1),m(),D((e=a.store.activeProgress()[i.number]?.progress?.generation)?3:-1,e)}}function lL(t,n){t&1&&u(0,` Known totals may be incomplete. `)}function cL(t,n){if(t&1&&(c(0,`p`,39),u(1),j(2,`number`),j(3,`number`),j(4,`number`),d(),c(5,`p`),u(6),E(7,lL,1,0),d()),t&2){let e=n;m(),Si(`Codex token usage · `,e.status,` · Input: `,e.tokens.inputTokens===null?`Unknown`:_e(2,9,e.tokens.inputTokens),` · Output: `,e.tokens.outputTokens===null?`Unknown`:_e(3,11,e.tokens.outputTokens),` · Total: `,e.tokens.totalTokens===null?`Unknown`:_e(4,13,e.tokens.totalTokens)),m(5),Si(``,e.completeAttempts,`/`,e.attempts,` attempts have complete usage; `,e.partialAttempts,` partial, `,e.unavailableAttempts,` unavailable. Includes repairs and failed or interrupted attempts. `),m(),D(e.status===`PARTIAL`?7:-1)}}function dL(t,n){t&1&&(c(0,`p`,39),u(1,`Codex token usage · UNAVAILABLE`),d())}function uL(t,n){if(t&1&&(c(0,`pre`,31),u(1),d()),t&2){let e=v();m(),w(e.failure)}}function mL(t,n){if(t&1&&(c(0,`p`,31),u(1),d(),c(2,`p`),u(3),d()),t&2){let e=n;m(),Ot(``,e.category,` · `,e.code,` · `,e.message),m(2),w(e.recovery)}}function pL(t,n){if(t&1&&(c(0,`details`)(1,`summary`),u(2,`Report export failed`),d(),c(3,`pre`,31),u(4),d()()),t&2){let e=v();m(4),w(e.reportFailure)}}function hL(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),d(),c(7,`td`),u(8),d(),c(9,`td`),u(10),d(),c(11,`td`)(12,`button`,26),C(`click`,function(){let a=k(e).$implicit;return M(v(2).store.selectTrial(a.number))}),u(13,`Open trial`),d()()()}if(t&2){let e=n.$implicit;m(2),w(e.number),m(2),w(e.targetId||`Unassigned`),m(2),w(e.status),m(2),w(e.tuningSeed),m(2),w(e.repairTurns)}}function fL(t,n){if(t&1){let e=$();c(0,`p`),u(1),d(),c(2,`button`,23),C(`click`,function(){k(e);return M(v(4).store.resumeTrial())}),u(3,`Resume existing work`),d(),c(4,`p`),u(5),d(),c(6,`label`),u(7,`Reason for marking this trial failed`),c(8,`textarea`,50),C(`input`,function(a){k(e);return M(v(4).failureReason.set(a.target.value))}),d()(),c(9,`button`,23),C(`click`,function(){k(e);let a=v(4);return M(a.store.failTrial(a.failureReason()))}),u(10,`Mark trial failed and continue`),d()}if(t&2){let e=n,i=v(4);m(),w(e.resume.reason||e.resume.effect),m(),S(`disabled`,i.store.busy()||!e.resume.allowed),m(3),w(e.fail.reason||e.fail.effect),m(3),S(`value`,i.failureReason()),m(),S(`disabled`,i.store.busy()||!e.fail.allowed||!i.failureReason().trim())}}function gL(t,n){if(t&1&&(c(0,`p`),u(1),d()),t&2){let e=v(4);m(),B(`Recovery availability is loading or unavailable. `,e.store.recoveryError())}}function bL(t,n){if(t&1&&(c(0,`fieldset`,47)(1,`legend`),u(2),d(),E(3,fL,11,5)(4,gL,2,1,`p`),d()),t&2){let e,i=v(),a=v(2);m(2),B(`Recover trial `,i.number),m(),D((e=a.store.trialRecovery())?3:4,e)}}function vL(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`date`),d()),t&2){let e=v();m(),B(`Waiting for Codex quota. Automatic retry after `,ke(2,1,e.quotaRetryAt,`medium`),`.`)}}function _L(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`number`),d()),t&2){let e=n,i=v();m(),Si(``,e.substage||e.status,` · `,ke(2,4,(e.elapsedMillis||0)/1e3,`1.0-0`),` seconds · Infrastructure retries: `,e.infrastructureRetries||0,` · Component repairs: `,i.repairTurns)}}function yL(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`json`),d()),t&2){let e=v();m(),B(`Reported usage: `,_e(2,1,e.usage))}}function xL(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`date`),d(),c(3,`p`),u(4),d(),E(5,yL,3,3,`p`),c(6,`p`),u(7,`These are observed CLI events. Component repair turns are counted separately; model iteration totals are unavailable.`),d()),t&2){let e=n;m(),Ot(`Generation: `,e.state,` · Session: `,e.threadId||`Waiting for session event`,` · Last activity: `,e.lastActivityAt?ke(2,9,e.lastActivityAt,`mediumTime`):`No events received`),m(3),ll(`Turns started: `,e.turns,` · Commands: `,e.commandsCompleted,` completed / `,e.commandsStarted,` started · Edit events: `,e.edits,` · Assistant messages: `,e.messages),m(),D(e.usage?5:-1)}}function CL(t,n){if(t&1&&(c(0,`article`)(1,`small`),u(2),j(3,`date`),d(),c(4,`pre`),u(5),d()()),t&2){let e=n.$implicit;m(2),Me(``,ke(3,3,e.timestamp,`mediumTime`),` · `,e.type),m(3),w(e.text)}}function SL(t,n){t&1&&(c(0,`p`),u(1,`Waiting for conversation events. Raw stdout and stderr remain available below.`),d())}function wL(t,n){if(t&1){let e=$();c(0,`details`,48)(1,`summary`),u(2),d(),c(3,`div`,51),de(4,CL,6,6,`article`,null,wo,!1,SL,2,0,`p`),d(),c(7,`button`,26),C(`click`,function(){let a=k(e);return M(v(3).store.selectLog(a.directory+`/stdout.log`))}),u(8,`Raw stdout`),d(),c(9,`button`,26),C(`click`,function(){let a=k(e);return M(v(3).store.selectLog(a.directory+`/stderr.log`))}),u(10,`Raw stderr`),d()()}if(t&2){let e=n,i=v(3);m(2),Ot(`Conversation · `,e.stage,` attempt `,e.number,` · `,e.status),m(2),ue(i.store.activity())}}function EL(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`number`),j(3,`number`),d()),t&2){let e=n;m(),Ot(`Trial token usage · `,e.status,` · Input: `,e.tokens.inputTokens===null?`Unknown`:_e(2,3,e.tokens.inputTokens),` · Output: `,e.tokens.outputTokens===null?`Unknown`:_e(3,5,e.tokens.outputTokens))}}function DL(t,n){if(t&1&&(c(0,`p`),u(1),d(),U(2,`mat-progress-bar`,30)),t&2){let e=n;m(),tu(``,e.phase,` · `,e.budget.used,`/`,e.budget.maximum,` evaluations · `,e.evaluations.succeeded,` succeeded · `,e.evaluations.rejected,` rejected · `,e.evaluations.failed,` failed`),m(),S(`value`,100*e.budget.used/e.budget.maximum)}}function IL(t,n){if(t&1&&(c(0,`pre`,31),u(1),d()),t&2){let e=v();m(),w(e.failure)}}function kL(t,n){if(t&1){let e=$();c(0,`button`,26),C(`click`,function(){k(e);let a=v().$implicit;return M(v(3).store.watchGeneration(a.number))}),u(1,`View conversation`),d()}}function ML(t,n){if(t&1&&(c(0,`a`,33),u(1),d()),t&2){let e=n.$implicit,i=v(2).$implicit;S(`href`,v(3).store.artifactUrl(i.directory+`/`+e.path),dn),m(),B(`Download conversation `,e.threadId)}}function TL(t,n){if(t&1&&(c(0,`span`),u(1),d(),de(2,ML,2,2,`a`,33,g2)),t&2){let e=n;m(),Me(`Conversation: `,e.status,` · Usage: `,e.usage.status),m(),ue(e.conversations)}}function NL(t,n){if(t&1&&(c(0,`details`)(1,`summary`),u(2,`Failure details`),d(),c(3,`pre`),u(4),d()()),t&2){let e=v().$implicit;m(4),w(e.failure)}}function AL(t,n){if(t&1){let e=$();c(0,`div`,49)(1,`span`),u(2),j(3,`date`),d(),c(4,`button`,26),C(`click`,function(){let a=k(e).$implicit;return M(v(3).store.selectLog(a.directory+([`PREPARATION`].includes(a.stage)?`/build`:``)+`/stdout.log`))}),u(5,`View log`),d(),E(6,kL,2,0,`button`,29),E(7,TL,4,2),E(8,NL,5,1,`details`),d()}if(t&2){let e,i=n.$implicit;m(2),Ot(``,i.stage,` · `,i.status,` · `,ke(3,6,i.startedAt,`short`)),m(4),D(nt(9,p2).includes(i.stage)?6:-1),m(),D((e=i.codexEvidence)?7:-1,e),m(),D(i.failure?8:-1)}}function RL(t,n){if(t&1&&(c(0,`h3`),u(1,`First-ranked proposal: components and parameters`),d(),c(2,`p`),u(3,`Generated algorithm components end in New.`),d(),c(4,`pre`),u(5),j(6,`json`),d()),t&2){let e=v();m(5),w(_e(6,1,e.winner))}}function OL(t,n){if(t&1&&(c(0,`details`,20)(1,`summary`),u(2,`Comparison results`),d(),c(3,`pre`),u(4),j(5,`json`),d()()),t&2){let e=v(3);m(4),w(_e(5,1,e.comparisonSummary()))}}function PL(t,n){if(t&1&&(c(0,`option`,54),u(1),d()),t&2){let e=n.$implicit,i=v(4);S(`value`,e)(`title`,e),m(),w(i.instanceLabels().get(e))}}function FL(t,n){if(t&1&&(c(0,`option`,4),u(1),d()),t&2){let e=n.$implicit;S(`value`,e),m(),w(e)}}function LL(t,n){t&1&&(c(0,`p`,60),u(1,`Logarithmic scale requires positive objective values; this selection uses a linear scale.`),d())}function VL(t,n){if(t&1){let e=$();c(0,`div`,52)(1,`label`),u(2,`Convergence instance`),c(3,`select`,53),C(`change`,function(a){k(e);return M(v(3).selectedInstance.set(a.target.value))}),de(4,PL,2,3,`option`,54,Pn),d()(),c(6,`label`),u(7,`Objective`),c(8,`select`,2),C(`change`,function(a){k(e);return M(v(3).selectedObjective.set(a.target.value))}),de(9,FL,2,2,`option`,4,Pn),d()(),c(11,`label`),u(12,`Y-axis scale`),c(13,`select`,2),C(`change`,function(a){k(e);return M(v(3).yAxisScale.set(a.target.value))}),c(14,`option`,55),u(15,`Linear`),d(),c(16,`option`,56),u(17,`Logarithmic`),d()()(),c(18,`label`),u(19,`Chart height`),c(20,`select`,2),C(`change`,function(a){k(e);return M(v(3).chartHeight.set(+a.target.value))}),c(21,`option`,57),u(22,`Standard`),d(),c(23,`option`,58),u(24,`Tall`),d(),c(25,`option`,59),u(26,`Extra tall`),d()()()(),c(27,`p`,60),u(28,`Drag a rectangle to zoom, Shift-drag to pan, or resize from the bottom-right corner. Click an algorithm in the legend to show or hide all its repetitions.`),d(),E(29,LL,2,0,`p`,60),U(30,`app-dashboard-chart`,61)}if(t&2){let e=v(3);m(3),S(`value`,e.activeInstance())(`title`,e.activeInstance()),m(),ue(e.instances()),m(4),S(`value`,e.activeObjective()),m(),ue(e.objectives()),m(4),S(`value`,e.effectiveYAxisScale()),m(3),S(`disabled`,!e.canLogYAxis()),m(4),S(`value`,e.chartHeight()),m(9),D(e.canLogYAxis()?-1:29),m(),Jt(`height`,e.chartHeight(),`px`),S(`model`,e.curveChart())(`resizable`,!0)}}function BL(t,n){if(t&1){let e=$();c(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),u(3),d(),E(4,bL,5,2,`fieldset`,47),E(5,vL,3,4,`p`),E(6,_L,3,7,`p`),E(7,xL,8,12),E(8,wL,11,4,`details`,48),E(9,EL,4,7,`p`),E(10,DL,3,7),E(11,IL,2,1,`pre`,31),de(12,AL,9,10,`div`,49,Wb),E(14,RL,7,3),c(15,`button`,23),C(`click`,function(){k(e);return M(v(2).store.loadCurves())}),u(16,`Load next comparison curves`),d(),c(17,`p`),u(18,`Curves load in pages and are sampled for display. Full data is available in the comparison journal.`),d(),E(19,OL,6,3,`details`,20),E(20,VL,31,11),d()()}if(t&2){let e,i,a,o,r,s=n,l=v(2);m(3),Me(`Trial `,s.number,` · `,s.status),m(),D(nt(14,m2).includes(s.status)?4:-1),m(),D(s.quotaRetryAt?5:-1),m(),D((e=l.store.progress())?6:-1,e),m(),D((i=l.store.generation())?7:-1,i),m(),D((a=l.store.conversationAttempt())?8:-1,a),m(),D((o=s.codexUsage)?9:-1,o),m(),D((r=l.store.tuning())?10:-1,r),m(),D(s.failure?11:-1),m(),ue(s.attempts),m(2),D(s.winner?14:-1),m(),S(`disabled`,l.store.busy()),m(4),D(l.store.comparison()?19:-1),m(),D(l.instances().length?20:-1)}}function jL(t,n){t&1&&(c(0,`p`,67),u(1,`Loading latest lines…`),d())}function zL(t,n){if(t&1&&(c(0,`p`,1),u(1),d()),t&2){let e=v(3);m(),w(e.store.logError())}}function HL(t,n){t&1&&u(0,` Earlier content is omitted. `)}function UL(t,n){t&1&&u(0,` Preview shortened by the 256 KiB byte limit. `)}function $L(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`number`),j(3,`number`),E(4,HL,1,0),E(5,UL,1,0),d(),c(6,`pre`,68),u(7),d()),t&2){let e=n;m(),Me(``,_e(2,5,e.lineCount),` lines shown · `,_e(3,7,e.sizeBytes),` bytes in the full file.`),m(3),D(e.truncated?4:-1),m(),D(e.byteLimited?5:-1),m(2),w(e.text)}}function GL(t,n){if(t&1){let e=$();c(0,`mat-card`,40)(1,`mat-card-content`)(2,`h2`),u(3),d(),c(4,`div`,27)(5,`label`),u(6,`Last lines`),c(7,`select`,2),C(`change`,function(a){k(e);return M(v(2).store.setLogLines(+a.target.value))}),c(8,`option`,62),u(9,`100`),d(),c(10,`option`,63),u(11,`500`),d(),c(12,`option`,59),u(13,`1,000`),d()()(),c(14,`label`,64)(15,`input`,65),C(`change`,function(a){k(e);return M(v(2).store.logFollow.set(a.target.checked))}),d(),u(16,`Follow latest`),d(),c(17,`button`,23),C(`click`,function(){k(e);return M(v(2).store.refreshLog())}),u(18,`Refresh preview`),d(),c(19,`a`,66),u(20,`Download full file`),d(),c(21,`button`,26),C(`click`,function(){k(e);return M(v(2).store.selectLog(``))}),u(22,`Close preview`),d()(),c(23,`p`),u(24),j(25,`number`),d(),E(26,jL,2,0,`p`,67),E(27,zL,2,1,`p`,1),E(28,$L,8,9),d()()}if(t&2){let e,i=v(2);m(3),w(i.store.logPath()),m(4),S(`value`,i.store.logLines()),m(8),S(`checked`,i.store.logFollow()),m(2),S(`disabled`,i.store.logLoading()),m(2),S(`href`,i.store.artifactUrl(i.store.logPath()),dn),m(5),B(`Preview of up to the last `,_e(25,9,i.store.logLines()),` lines, limited to 256 KiB.`),m(2),D(i.store.logLoading()?26:-1),m(),D(i.store.logError()?27:-1),m(),D((e=i.store.logPreview())?28:-1,e)}}function WL(t,n){if(t&1){let e=$();c(0,`button`,26),C(`click`,function(){k(e);let a=v().$implicit;return M(v(2).store.selectLog(a.path))}),u(1,`Preview tail`),d()}}function qL(t,n){if(t&1&&(c(0,`div`,41)(1,`a`,69),u(2),d(),c(3,`span`),u(4),d(),E(5,WL,2,0,`button`,29),d()),t&2){let e=n.$implicit,i=v(2);m(),S(`href`,i.store.artifactUrl(e.path),dn)(`title`,`Download full file: `+e.path),m(),w(e.path),m(2),B(``,e.size,` bytes`),m(),D(i.canPreviewFile(e.path)?5:-1)}}function YL(t,n){if(t&1){let e=$();c(0,`mat-card`)(1,`mat-card-content`)(2,`div`,27)(3,`strong`),u(4),d(),E(5,U2,2,1,`button`,28),E(6,$2,2,2,`button`,35),E(7,G2,2,1,`button`,28),E(8,q2,6,1),E(9,Y2,2,1,`button`,28),d(),U(10,`mat-progress-bar`,30),E(11,K2,2,1,`p`,31),E(12,Q2,2,1,`p`),c(13,`h2`),u(14),d(),c(15,`p`),u(16,`Each server runs one trial at a time. SOTA and proposal measurements stay paired on that server; reports are grouped by server.`),d(),c(17,`div`,36),de(18,iL,8,8,`article`,37,Gm),d(),E(20,aL,10,2,`details`),de(21,sL,6,5,`p`,38,Wb),E(23,cL,8,15)(24,dL,2,0,`p`,39),E(25,uL,2,1,`pre`,31),E(26,mL,4,4),E(27,pL,5,1,`details`),c(28,`table`)(29,`thead`)(30,`tr`)(31,`th`),u(32,`Trial`),d(),c(33,`th`),u(34,`Server`),d(),c(35,`th`),u(36,`Stage / outcome`),d(),c(37,`th`),u(38,`Tuning seed`),d(),c(39,`th`),u(40,`Repairs`),d(),c(41,`th`),u(42,`Inspect`),d()()(),c(43,`tbody`),de(44,hL,14,5,`tr`,null,Wb),d()()()(),E(46,BL,21,15,`mat-card`),E(47,GL,29,11,`mat-card`,40),c(48,`mat-card`)(49,`mat-card-content`)(50,`div`,27)(51,`h2`),u(52,`Saved data and artifacts`),d(),c(53,`button`,26),C(`click`,function(){k(e);return M(v().store.refreshArtifacts())}),u(54,`Refresh artifacts`),d()(),de(55,qL,6,5,`div`,41,f2),c(57,`details`)(58,`summary`),u(59,`Frozen campaign settings and versions`),d(),c(60,`pre`),u(61),j(62,`json`),d(),c(63,`pre`),u(64),j(65,`json`),d()()()()}if(t&2){let e,i,a,o,r=n,s=v();m(4),Ot(``,r.status,` · `,s.store.completed(),`/`,r.trials.length,` trials finished`),m(),D(s.store.active()===r.id?5:-1),m(),D(nt(25,c2).includes(r.status)&&!s.store.active()?6:-1),m(),D(r.status===`NEEDS_ATTENTION`?7:-1),m(),D(r.status===`DRAFT`&&!s.store.active()?8:-1),m(),D(r.status!==`DRAFT`&&!s.store.active()?9:-1),m(),S(`value`,100*s.store.completed()/r.trials.length),m(),D(s.store.recoveryError()?11:-1),m(),D((e=!s.store.active()&&s.store.recovery())?12:-1,e),m(2),B(`Servers · pool revision `,r.poolRevision),m(4),ue(s.store.targets()),m(2),D(r.config.execution.target===`SSH`&&!nt(26,d2).includes(r.status)?20:-1),m(),ue(s.store.activeTrials()),m(2),D((i=r.codexUsage)?23:24,i),m(2),D(r.failure?25:-1),m(),D((a=r.problem)?26:-1,a),m(),D(r.reportFailure?27:-1),m(17),ue(r.trials),m(2),D((o=s.store.trial())?46:-1,o),m(),D(s.store.logPath()?47:-1),m(8),ue(s.store.artifacts()),m(6),w(_e(62,21,r.config)),m(3),w(_e(65,23,r.versions))}}var $m=class t$1{canPreviewFile=Um.canPreview;store=f(zm);selectedInstance=T(``);failureReason=T(``);selectedObjective=T(``);yAxisScale=T(`linear`);chartHeight=T(520);executionChoice=T(``);newTarget=T(`{"id":"server-2","host":"user@host","workspace":"/home/user/autollm","javaExecutable":"java","environment":{}}`);originalPool=T(``);trainingFraction=T(.15);curves=K(()=>this.store.comparison()?.curves??[]);instances=K(()=>[...new Set(this.curves().map(n=>n.instance))]);objectives=K(()=>[...new Set(this.curves().flatMap(n=>Object.keys(n.curves)))]);instanceLabels=K(()=>Hm.relativeLabels(this.instances()));activeInstance=K(()=>this.instances().includes(this.selectedInstance())?this.selectedInstance():this.instances()[0]);activeObjective=K(()=>this.objectives().includes(this.selectedObjective())?this.selectedObjective():this.objectives()[0]);selectedCurves=K(()=>this.curves().filter(n=>n.instance===this.activeInstance()));canLogYAxis=K(()=>ic.canUseLogarithmicScale(this.selectedCurves(),this.activeObjective()));effectiveYAxisScale=K(()=>this.yAxisScale()===`logarithmic`&&this.canLogYAxis()?`logarithmic`:`linear`);comparisonSummary=K(()=>{let i=this.store.comparison()??{},{curves:n}=i;return t(i,[`curves`])});curveChart=K(()=>ic.build(this.selectedCurves(),this.activeObjective(),this.effectiveYAxisScale()));constructor(){vt(()=>{this.store.selectedId(),this.store.selectedTrial(),this.failureReason.set(``)}),this.store.start()}executionTarget(n){let e=n.target.value;this.executionChoice.set(e),this.store.config.update(i=>i&&s(r({},i),{execution:s(r({},i.execution),{target:e,ssh:i.execution.ssh??{screeningServerId:`server-1`,runnerArtifact:null,servers:[{id:`server-1`,host:``,workspace:``,javaExecutable:`java`,environment:{}}]}})})),this.store.targetStatus.set(null)}addServer(){this.store.config.update(n=>{if(!n?.execution.ssh)return n;let e=[...n.execution.ssh.servers,{id:`server-${n.execution.ssh.servers.length+1}`,host:``,workspace:``,javaExecutable:`java`,environment:{}}];return s(r({},n),{execution:s(r({},n.execution),{ssh:s(r({},n.execution.ssh),{servers:e})})})})}removeServer(n){this.store.config.update(e=>e?.execution.ssh?s(r({},e),{execution:s(r({},e.execution),{ssh:s(r({},e.execution.ssh),{servers:e.execution.ssh.servers.filter((i,a)=>a!==n)})})}):e)}enrollTarget(){try{let n=JSON.parse(this.newTarget());if(!n?.id||!n.host||!n.workspace)throw new Error(`Provide server id, host, and workspace`);this.store.addTarget(n)}catch(n){this.store.error.set(n.message)}}workerMode(n){let e=n.target.value===`AUTO`;this.store.config.update(i=>i&&s(r({},i),{execution:s(r({},i.execution),{workers:e?-1:1})}))}update(n,e,i=!1){let a=this.store.config();if(!a)return;let o=structuredClone(a),r=o,s=n.split(`.`);for(let h of s.slice(0,-1))(!r[h]||typeof r[h]!=`object`)&&(r[h]={}),r=r[h];let l=e.target.value;r[s[s.length-1]]=i?l?Number(l):null:l||null,this.store.config.set(o)}buildCommand(n){try{let e=JSON.parse(n.target.value);if(!Array.isArray(e)||e.some(i=>typeof i!=`string`))throw new Error(`Build command must be a JSON array of arguments`);this.store.config.update(i=>i&&s(r({},i),{project:s(r({},i.project),{buildCommand:e})}))}catch(e){this.store.error.set(e.message)}}advanced(n){try{let e=this.store.config();if(!e)return;let i=JSON.parse(n.target.value);if(!i||typeof i!=`object`||Array.isArray(i))throw new Error(`Settings must be a JSON object`);for(let o of[`project`,`codex`,`preparation`,`tuning`,`comparison`,`execution`])if(i[o]!=null&&(typeof i[o]!=`object`||Array.isArray(i[o])))throw new Error(`${o} must be a JSON object`);let a=i.execution?.ssh;if(a!=null&&(typeof a!=`object`||Array.isArray(a)))throw new Error(`execution.ssh must be a JSON object`);this.store.config.set(s(r(r({},e),i),{project:r(r({},e.project),i.project),codex:r(r({},e.codex),i.codex),preparation:r(r({},e.preparation),i.preparation),tuning:r(r({},e.tuning),i.tuning),comparison:r(r({},e.comparison),i.comparison),execution:s(r(r({},e.execution),i.execution),{ssh:a===null?null:e.execution.ssh||a?r(r({screeningServerId:``,runnerArtifact:null,servers:[]},e.execution.ssh),a):null})})),i.execution?.target&&this.executionChoice.set(i.execution.target),this.store.error.set(null)}catch(e){this.store.error.set(e.message)}}static ɵfac=function(e){return new(e||t$1)};static ɵcmp=H({type:t$1,selectors:[[`app-autollm-dashboard`]],decls:20,vars:6,consts:[[`src`,`favicon.svg`,`alt`,`Mork`],[`role`,`alert`,1,`error`],[3,`change`,`value`],[`value`,``],[3,`value`],[3,`open`],[3,`disabled`],[1,`fields`],[`value`,``,`disabled`,``],[`value`,`LOCAL`],[`value`,`SSH`],[`type`,`number`,`min`,`1`,3,`change`,`value`],[`type`,`number`,3,`change`,`value`],[`type`,`number`,`min`,`1`,`max`,`299`,3,`change`,`value`],[`value`,`AUTO`],[`value`,`CUSTOM`],[`type`,`number`,`min`,`0`,3,`change`,`value`],[`rows`,`6`,3,`change`,`value`],[`rows`,`16`,3,`change`,`value`],[1,`budget-summary`],[`open`,``],[3,`input`,`value`],[`type`,`number`,`min`,`0.01`,`max`,`0.99`,`step`,`0.01`,3,`input`,`value`],[`mat-button`,``,3,`click`,`disabled`],[`mat-flat-button`,``,3,`click`,`disabled`],[1,`server-definition`],[`mat-button`,``,3,`click`],[1,`actions`],[`mat-button`,``,3,`disabled`],[`mat-button`,``],[`mode`,`determinate`,3,`value`],[1,`error`],[1,`screening-report`],[3,`href`],[`mat-flat-button`,``,3,`disabled`],[`mat-flat-button`,``,3,`disabled`,`title`],[1,`target-grid`],[1,`target-card`],[1,`active-trial`],[1,`codex-usage`],[1,`log-preview`],[1,`artifact`],[`mat-flat-button`,``,3,`click`,`disabled`,`title`],[`mat-flat-button`,``,3,`click`],[`type`,`checkbox`,3,`change`,`checked`,`disabled`],[`mat-button`,``,3,`click`,`disabled`,`title`],[`rows`,`5`,3,`input`,`value`],[1,`trial-recovery`],[`open`,``,1,`conversation`],[1,`attempt`],[`rows`,`2`,3,`input`,`value`],[`aria-live`,`polite`,1,`activity-feed`],[1,`fields`,`comparison-controls`],[3,`change`,`value`,`title`],[3,`value`,`title`],[`value`,`linear`],[`value`,`logarithmic`,3,`disabled`],[`value`,`520`],[`value`,`760`],[`value`,`1000`],[1,`chart-help`],[`aria-label`,`Comparison convergence chart`,3,`model`,`resizable`],[`value`,`100`],[`value`,`500`],[1,`follow-log`],[`type`,`checkbox`,3,`change`,`checked`],[`mat-button`,``,`download`,``,3,`href`],[`role`,`status`],[1,`logs`],[`download`,``,3,`href`,`title`]],template:function(e,i){if(e&1&&(c(0,`main`)(1,`header`),U(2,`img`,0),c(3,`div`)(4,`h1`),u(5,`AutoLLM campaigns`),d(),c(6,`p`),u(7,`Generate components, tune algorithms, and compare each independent proposal against SOTA.`),d()()(),E(8,b2,2,1,`p`,1),E(9,v2,2,1,`p`,1),E(10,E2,119,49,`mat-card`),E(11,z2,11,3,`mat-card`),c(12,`label`),u(13,`Saved campaign`),c(14,`select`,2),C(`change`,function(o){return i.store.select(o.target.value)}),c(15,`option`,3),u(16,`Select campaign`),d(),de(17,H2,3,7,`option`,4,Gm),d()(),E(19,YL,66,27),d()),e&2){let a,o;m(8),D(i.store.error()?8:-1),m(),D(i.store.connectionError()?9:-1),m(),D((a=i.store.config())?10:-1,a),m(),D(i.store.screenings().length?11:-1),m(3),S(`value`,i.store.selectedId()),m(3),ue(i.store.campaigns()),m(2),D((o=i.store.campaign())?19:-1,o)}},dependencies:[Ft,nn,zn,Bn,jn,ds,cs,Ai,Do,Xi,En],styles:[`[_nghost-%COMP%]{display:block;background:var(--%NS%mat-sys-surface);color:var(--%NS%mat-sys-on-surface);min-height:100vh}main[_ngcontent-%COMP%]{max-width:88rem;margin:auto;padding:1.5rem;display:grid;gap:1rem}header[_ngcontent-%COMP%], .actions[_ngcontent-%COMP%], .artifact[_ngcontent-%COMP%]{display:flex;align-items:center;flex-wrap:wrap;gap:1rem}header[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:3rem}fieldset[_ngcontent-%COMP%]{border:0;padding:1rem 0}.fields[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(auto-fit,minmax(16rem,1fr));gap:1rem}label[_ngcontent-%COMP%]{display:flex;flex-direction:column;gap:.4rem;font-weight:500;margin-bottom:.6rem;min-width:0}input[_ngcontent-%COMP%], textarea[_ngcontent-%COMP%], select[_ngcontent-%COMP%]{padding:.6rem;background:var(--%NS%mat-sys-surface-container);color:inherit;border:1px solid var(--%NS%mat-sys-outline);border-radius:.4rem;font:inherit}textarea[_ngcontent-%COMP%]{width:100%;box-sizing:border-box}select[_ngcontent-%COMP%]{min-width:0;width:100%;box-sizing:border-box;text-overflow:ellipsis}.chart-help[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.875rem;line-height:1.5}summary[_ngcontent-%COMP%]{cursor:pointer;padding:.5rem 0}table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse;margin:1rem 0}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{text-align:left;padding:.6rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}pre[_ngcontent-%COMP%]{white-space:pre-wrap;overflow-wrap:anywhere;overflow:auto;max-height:25rem;padding:.75rem;background:var(--%NS%mat-sys-surface-container)}.logs[_ngcontent-%COMP%]{white-space:pre}.log-preview[_ngcontent-%COMP%]{min-width:0}.log-preview[_ngcontent-%COMP%]   h2[_ngcontent-%COMP%]{overflow-wrap:anywhere}.follow-log[_ngcontent-%COMP%]{flex-direction:row;align-items:center}.error[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}.attempt[_ngcontent-%COMP%]{padding:.5rem 0}.artifact[_ngcontent-%COMP%]{padding:.25rem 0}.artifact[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{overflow-wrap:anywhere}.activity-feed[_ngcontent-%COMP%]{max-height:32rem;overflow:auto;padding:.75rem;background:var(--%NS%mat-sys-surface-container-low)}.activity-feed[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{margin-bottom:1rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}.activity-feed[_ngcontent-%COMP%]   pre[_ngcontent-%COMP%], .screening-report[_ngcontent-%COMP%]{white-space:pre-wrap;overflow-wrap:anywhere}.budget-summary[_ngcontent-%COMP%]{line-height:1.6}.target-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:1rem;margin-block:1rem}.target-card[_ngcontent-%COMP%], .server-definition[_ngcontent-%COMP%]{border:1px solid rgba(119,119,119,.3333333333);padding:1rem;border-radius:.5rem}.active-trial[_ngcontent-%COMP%]{border-bottom:1px solid rgba(119,119,119,.3333333333);padding-block:.5rem}`]})};var Wm=class t{client=f(La);destroyRef=f(Ae);viewMode=T(`detecting`);latestError=T(null);timer=null;started=!1;destroyed=!1;polling=!1;retry=!1;delay=500;constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.timer&&clearTimeout(this.timer)})}start(){this.started||(this.started=!0,this.poll())}retryNow(){this.timer&&clearTimeout(this.timer),this.polling?this.retry=!0:this.poll()}async poll(){if(this.destroyed)return;this.polling=!0;let n=1e4;try{let e=await this.client.getMode();if(this.destroyed)return;this.viewMode.set(e.mode.toLowerCase()),this.latestError.set(null),this.delay=500,e.mode===`DETECTING`&&(n=500)}catch(e){if(this.destroyed)return;this.latestError.set(e instanceof Error?e.message:`Could not contact the backend`),this.viewMode()===`detecting`&&this.viewMode.set(`unavailable`),n=this.delay,this.delay=Math.min(3e4,this.delay*2)}finally{this.polling=!1,this.destroyed||(this.timer=setTimeout(()=>{this.poll()},this.retry?0:n),this.retry=!1)}}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};function KL(t,n){t&1&&U(0,`app-autollm-dashboard`)}function QL(t,n){t&1&&U(0,`app-standard-dashboard`)}function XL(t,n){t&1&&U(0,`app-tuning-dashboard`)}function ZL(t,n){t&1&&(c(0,`main`,0),U(1,`img`,2),c(2,`mat-card`,3)(3,`mat-card-header`)(4,`mat-card-title`),u(5,`Autoconfig worker`),d()(),c(6,`mat-card-content`)(7,`p`),u(8,`This process executes evaluations for the coordinator.`),d(),c(9,`p`),u(10,`The monitoring glass panel is available on the coordinator process.`),d()()()())}function JL(t,n){t&1&&(c(0,`main`,0),U(1,`img`,2),c(2,`mat-card`)(3,`mat-card-content`)(4,`h1`),u(5,`Instance utility`),d(),c(6,`p`),u(7,`This process is analysing or selecting instances. It is not running a measured experiment.`),d()()()())}function eV(t,n){if(t&1){let e=$();c(0,`main`,0),U(1,`img`,2),c(2,`mat-card`,3)(3,`mat-card-header`)(4,`mat-card-title`),u(5,`Waiting for the Mork backend`),d()(),c(6,`mat-card-content`)(7,`p`),u(8),d(),c(9,`button`,4),C(`click`,function(){k(e);return M(v().store.retryNow())}),u(10,`Retry now`),d()()()()}if(t&2){let e=v();m(8),w(e.store.latestError()??`The run mode could not be detected.`)}}function tV(t,n){t&1&&(c(0,`main`,1),U(1,`img`,2)(2,`mat-spinner`,5),c(3,`p`),u(4,`Detecting experiment mode…`),d()())}Dg(class t{store=f(Wm);ngOnInit(){this.store.start()}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-root`]],decls:7,vars:1,consts:[[1,`mode-state`],[`aria-label`,`Detecting experiment mode`,1,`mode-state`,`detecting`],[`src`,`favicon.svg`,`alt`,`Mork`],[`appearance`,`outlined`],[`mat-flat-button`,``,`type`,`button`,3,`click`],[`diameter`,`36`]],template:function(e,i){if(e&1&&E(0,KL,1,0,`app-autollm-dashboard`)(1,QL,1,0,`app-standard-dashboard`)(2,XL,1,0,`app-tuning-dashboard`)(3,ZL,11,0,`main`,0)(4,JL,8,0,`main`,0)(5,eV,11,1,`main`,0)(6,tV,5,0,`main`,1),e&2){let a;D((a=i.store.viewMode())===`autollm`?0:a===`standard`?1:a===`tuning`?2:a===`worker`?3:a===`utility`?4:a===`unavailable`?5:6)}},dependencies:[Ft,nn,zn,Bn,jn,Ta,Ma,dS,cS,wm,Bm,$m],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh}.mode-state[_ngcontent-%COMP%]{display:grid;place-content:center;min-height:100vh;padding:1rem;color:var(--%NS%mat-sys-on-surface);background:radial-gradient(circle at 50% 20%,color-mix(in srgb,var(--%NS%mat-sys-tertiary) 16%,transparent),transparent 25rem),var(--%NS%mat-sys-surface-container-lowest)}.mode-state[_ngcontent-%COMP%] > img[_ngcontent-%COMP%]{width:5rem;height:5rem;margin:0 auto 1.25rem}.mode-state[_ngcontent-%COMP%]   mat-card[_ngcontent-%COMP%]{width:min(32rem,100vw - 2rem)}.mode-state.detecting[_ngcontent-%COMP%]{justify-items:center;color:var(--%NS%mat-sys-on-surface-variant)}.mode-state.detecting[_ngcontent-%COMP%] > img[_ngcontent-%COMP%]{margin-bottom:1.5rem}`]})},EC).catch(t=>console.error(t));export{s as n,r as t};