var k$1=Object.create;var g=Object.defineProperty;var l=Object.defineProperties;var m$1=Object.getOwnPropertyDescriptor;var n=Object.getOwnPropertyDescriptors;var o=Object.getOwnPropertyNames;var f$1=Object.getOwnPropertySymbols;var p=Object.getPrototypeOf;var h=Object.prototype.hasOwnProperty;var j$1=Object.prototype.propertyIsEnumerable;var i=(a,b,c)=>b in a?g(a,b,{enumerable:!0,configurable:!0,writable:!0,value:c}):a[b]=c;var r=(a,b)=>{for(var c in b||={})h.call(b,c)&&i(a,c,b[c]);if(f$1)for(var c of f$1(b))j$1.call(b,c)&&i(a,c,b[c]);return a};var s=(a,b)=>l(a,n(b));var t=(a,b)=>{var c={};for(var d in a)h.call(a,d)&&b.indexOf(d)<0&&(c[d]=a[d]);if(a!=null&&f$1)for(var d of f$1(a))b.indexOf(d)<0&&j$1.call(a,d)&&(c[d]=a[d]);return c};var u$1=(a,b)=>()=>{try{return b||a((b={exports:{}}).exports,b),b.exports}catch(c){throw b=0,c}};var q$1=(a,b,c,d)=>{if(b&&typeof b==`object`||typeof b==`function`)for(let e of o(b))!h.call(a,e)&&e!==c&&g(a,e,{get:()=>b[e],enumerable:!(d=m$1(b,e))||d.enumerable});return a};var v$1=(a,b,c)=>(c=a!=null?k$1(p(a)):{},q$1(b||!a||!a.__esModule?g(c,`default`,{value:a,enumerable:!0}):c,a));var zw=u$1((ym,jw)=>{"use strict";(function(t,n){typeof ym==`object`&&typeof jw<`u`?n(ym):typeof define==`function`&&define.amd?define([`exports`],n):(t=typeof globalThis<`u`?globalThis:t||self,n(t.StompJs={}))})(ym,(function(t){"use strict";function n(L,x){L.terminate=function(){let A=()=>{};this.onerror=A,this.onmessage=A,this.onopen=A;let R=new Date,W=Math.random().toString().substring(2,8),ye=this.onclose;this.onclose=bt=>{let We=new Date().getTime()-R.getTime();x(`Discarded socket (#${W})  closed after ${We}ms, with code/reason: ${bt.code}/${bt.reason}`)},this.close(),ye?.call(L,{code:4001,reason:`Quick discarding socket (#${W}) without waiting for the shutdown sequence.`,wasClean:!1})}}let e={LF:`
`,NULL:`\0`};class i{get body(){return!this._body&&this.isBinaryBody&&(this._body=new TextDecoder().decode(this._binaryBody)),this._body||``}get binaryBody(){return!this._binaryBody&&!this.isBinaryBody&&(this._binaryBody=new TextEncoder().encode(this._body)),this._binaryBody}constructor(x){let{command:A,headers:R,body:W,binaryBody:ye,escapeHeaderValues:bt,skipContentLengthHeader:We}=x;this.command=A,this.headers=Object.assign({},R||{}),ye?(this._binaryBody=ye,this.isBinaryBody=!0):(this._body=W||``,this.isBinaryBody=!1),this.escapeHeaderValues=bt||!1,this.skipContentLengthHeader=We||!1}static fromRawFrame(x,A){let R={},W=ye=>ye.replace(/^\s+|\s+$/g,``);for(let ye of x.headers.reverse()){ye.indexOf(`:`);let bt=W(ye[0]),We=W(ye[1]);A&&x.command!==`CONNECT`&&x.command!==`CONNECTED`&&(We=i.hdrValueUnEscape(We)),R[bt]=We}return new i({command:x.command,headers:R,binaryBody:x.binaryBody,escapeHeaderValues:A})}toString(){return this.serializeCmdAndHeaders()}serialize(){let x=this.serializeCmdAndHeaders();return this.isBinaryBody?i.toUnit8Array(x,this._binaryBody).buffer:x+this._body+e.NULL}serializeCmdAndHeaders(){let x=[this.command];this.skipContentLengthHeader&&delete this.headers[`content-length`];for(let A of Object.keys(this.headers||{})){let R=this.headers[A];this.escapeHeaderValues&&this.command!==`CONNECT`&&this.command!==`CONNECTED`?x.push(`${A}:${i.hdrValueEscape(`${R}`)}`):x.push(`${A}:${R}`)}return(this.isBinaryBody||!this.isBodyEmpty()&&!this.skipContentLengthHeader)&&x.push(`content-length:${this.bodyLength()}`),x.join(e.LF)+e.LF+e.LF}isBodyEmpty(){return this.bodyLength()===0}bodyLength(){let x=this.binaryBody;return x?x.length:0}static sizeOfUTF8(x){return x?new TextEncoder().encode(x).length:0}static toUnit8Array(x,A){let R=new TextEncoder().encode(x),W=new Uint8Array([0]),ye=new Uint8Array(R.length+A.length+W.length);return ye.set(R),ye.set(A,R.length),ye.set(W,R.length+A.length),ye}static marshall(x){return new i(x).serialize()}static hdrValueEscape(x){return x.replace(/\\/g,`\\\\`).replace(/\r/g,`\\r`).replace(/\n/g,`\\n`).replace(/:/g,`\\c`)}static hdrValueUnEscape(x){return x.replace(/\\r/g,`\r`).replace(/\\n/g,`
`).replace(/\\c/g,`:`).replace(/\\\\/g,`\\`)}}let a=0,o=10,r=13,s=58;class l{constructor(x,A){this.onFrame=x,this.onIncomingPing=A,this._encoder=new TextEncoder,this._decoder=new TextDecoder,this._token=[],this._initState()}parseChunk(x,A=!1){let R;if(typeof x==`string`?R=this._encoder.encode(x):R=new Uint8Array(x),A&&R[R.length-1]!==0){let W=new Uint8Array(R.length+1);W.set(R,0),W[R.length]=0,R=W}for(let W=0;W<R.length;W++){let ye=R[W];this._onByte(ye)}}_collectFrame(x){if(x!==a&&x!==r){if(x===o){this.onIncomingPing();return}this._onByte=this._collectCommand,this._reinjectByte(x)}}_collectCommand(x){if(x!==r){if(x===o){this._results.command=this._consumeTokenAsUTF8(),this._onByte=this._collectHeaders;return}this._consumeByte(x)}}_collectHeaders(x){if(x!==r){if(x===o){this._setupCollectBody();return}this._onByte=this._collectHeaderKey,this._reinjectByte(x)}}_reinjectByte(x){this._onByte(x)}_collectHeaderKey(x){if(x===s){this._headerKey=this._consumeTokenAsUTF8(),this._onByte=this._collectHeaderValue;return}this._consumeByte(x)}_collectHeaderValue(x){if(x!==r){if(x===o){this._results.headers.push([this._headerKey,this._consumeTokenAsUTF8()]),this._headerKey=void 0,this._onByte=this._collectHeaders;return}this._consumeByte(x)}}_setupCollectBody(){let x=this._results.headers.filter(A=>A[0]===`content-length`)[0];x?(this._bodyBytesRemaining=parseInt(x[1],10),this._onByte=this._collectBodyFixedSize):this._onByte=this._collectBodyNullTerminated}_collectBodyNullTerminated(x){if(x===a){this._retrievedBody();return}this._consumeByte(x)}_collectBodyFixedSize(x){if(this._bodyBytesRemaining--===0){this._retrievedBody();return}this._consumeByte(x)}_retrievedBody(){this._results.binaryBody=this._consumeTokenAsRaw();try{this.onFrame(this._results)}catch(x){console.log(`Ignoring an exception thrown by a frame handler. Original exception: `,x)}this._initState()}_consumeByte(x){this._token.push(x)}_consumeTokenAsUTF8(){return this._decoder.decode(this._consumeTokenAsRaw())}_consumeTokenAsRaw(){let x=new Uint8Array(this._token);return this._token=[],x}_initState(){this._results={command:void 0,headers:[],binaryBody:void 0},this._token=[],this._headerKey=void 0,this._onByte=this._collectFrame}}t.StompSocketState=void 0,(function(L){L[L.CONNECTING=0]=`CONNECTING`,L[L.OPEN=1]=`OPEN`,L[L.CLOSING=2]=`CLOSING`,L[L.CLOSED=3]=`CLOSED`})(t.StompSocketState||(t.StompSocketState={})),t.ActivationState=void 0,(function(L){L[L.ACTIVE=0]=`ACTIVE`,L[L.DEACTIVATING=1]=`DEACTIVATING`,L[L.INACTIVE=2]=`INACTIVE`})(t.ActivationState||(t.ActivationState={})),t.ReconnectionTimeMode=void 0,(function(L){L[L.LINEAR=0]=`LINEAR`,L[L.EXPONENTIAL=1]=`EXPONENTIAL`})(t.ReconnectionTimeMode||(t.ReconnectionTimeMode={})),t.TickerStrategy=void 0,(function(L){L.Interval=`interval`,L.Worker=`worker`})(t.TickerStrategy||(t.TickerStrategy={}));class h{constructor(x,A=t.TickerStrategy.Interval,R){this._interval=x,this._strategy=A,this._debug=R,this._workerScript=`
    var startTime = Date.now();
    setInterval(function() {
        self.postMessage(Date.now() - startTime);
    }, ${this._interval});
  `}start(x){this.stop(),this.shouldUseWorker()?this.runWorker(x):this.runInterval(x)}stop(){this.disposeWorker(),this.disposeInterval()}shouldUseWorker(){return typeof Worker<`u`&&this._strategy===t.TickerStrategy.Worker}runWorker(x){this._debug(`Using runWorker for outgoing pings`),this._worker||(this._worker=new Worker(URL.createObjectURL(new Blob([this._workerScript],{type:`text/javascript`}))),this._worker.onmessage=A=>x(A.data))}runInterval(x){if(this._debug(`Using runInterval for outgoing pings`),!this._timer){let A=Date.now();this._timer=setInterval(()=>{x(Date.now()-A)},this._interval)}}disposeWorker(){this._worker&&(this._worker.terminate(),delete this._worker,this._debug(`Outgoing ping disposeWorker`))}disposeInterval(){this._timer&&(clearInterval(this._timer),delete this._timer,this._debug(`Outgoing ping disposeInterval`))}}class p{constructor(x){this.versions=x}supportedVersions(){return this.versions.join(`,`)}protocolVersions(){return this.versions.map(x=>`v${x.replace(`.`,``)}.stomp`)}}p.V1_0=`1.0`,p.V1_1=`1.1`,p.V1_2=`1.2`,p.default=new p([p.V1_2,p.V1_1,p.V1_0]);class g{get connectedVersion(){return this._connectedVersion}get connected(){return this._connected}constructor(x,A,R){this._client=x,this._webSocket=A,this._connected=!1,this._serverFrameHandlers={CONNECTED:W=>{this.debug(`connected to server ${W.headers.server}`),this._connected=!0,this._connectedVersion=W.headers.version,this._connectedVersion===p.V1_2&&(this._escapeHeaderValues=!0),this._setupHeartbeat(W.headers),this.onConnect(W)},MESSAGE:W=>{let ye=W.headers.subscription,bt=this._subscriptions[ye]||this.onUnhandledMessage,We=W,pe=this,Ie=this._connectedVersion===p.V1_2?We.headers.ack:We.headers[`message-id`];We.ack=(F={})=>pe.ack(Ie,ye,F),We.nack=(F={})=>pe.nack(Ie,ye,F),bt(We)},RECEIPT:W=>{let ye=this._receiptWatchers[W.headers[`receipt-id`]];ye?(ye(W),delete this._receiptWatchers[W.headers[`receipt-id`]]):this.onUnhandledReceipt(W)},ERROR:W=>{this.onStompError(W)}},this._counter=0,this._subscriptions={},this._receiptWatchers={},this._partialData=``,this._escapeHeaderValues=!1,this._lastServerActivityTS=Date.now(),this.debug=R.debug,this.stompVersions=R.stompVersions,this.connectHeaders=R.connectHeaders,this.disconnectHeaders=R.disconnectHeaders,this.heartbeatIncoming=R.heartbeatIncoming,this.heartbeatToleranceMultiplier=R.heartbeatGracePeriods,this.heartbeatOutgoing=R.heartbeatOutgoing,this.splitLargeFrames=R.splitLargeFrames,this.maxWebSocketChunkSize=R.maxWebSocketChunkSize,this.forceBinaryWSFrames=R.forceBinaryWSFrames,this.logRawCommunication=R.logRawCommunication,this.appendMissingNULLonIncoming=R.appendMissingNULLonIncoming,this.discardWebsocketOnCommFailure=R.discardWebsocketOnCommFailure,this.onConnect=R.onConnect,this.onDisconnect=R.onDisconnect,this.onStompError=R.onStompError,this.onWebSocketClose=R.onWebSocketClose,this.onWebSocketError=R.onWebSocketError,this.onUnhandledMessage=R.onUnhandledMessage,this.onUnhandledReceipt=R.onUnhandledReceipt,this.onUnhandledFrame=R.onUnhandledFrame,this.onHeartbeatReceived=R.onHeartbeatReceived,this.onHeartbeatLost=R.onHeartbeatLost}start(){let x=new l(R=>{let W=i.fromRawFrame(R,this._escapeHeaderValues);this.logRawCommunication||this.debug(`<<< ${W}`),(this._serverFrameHandlers[W.command]||this.onUnhandledFrame)(W)},()=>{this.debug(`<<< PONG`),this.onHeartbeatReceived()});this._webSocket.onmessage=R=>{if(this.debug(`Received data`),this._lastServerActivityTS=Date.now(),this.logRawCommunication){let W=R.data instanceof ArrayBuffer?new TextDecoder().decode(R.data):R.data;this.debug(`<<< ${W}`)}x.parseChunk(R.data,this.appendMissingNULLonIncoming)},this._webSocket.onclose=R=>{this.debug(`Connection closed to ${this._webSocket.url}`),this._cleanUp(),this.onWebSocketClose(R)},this._webSocket.onerror=R=>{this.onWebSocketError(R)};let A=()=>{let R=Object.assign({},this.connectHeaders);this.debug(`Web Socket Opened...`),R[`accept-version`]=this.stompVersions.supportedVersions(),R[`heart-beat`]=[this.heartbeatOutgoing,this.heartbeatIncoming].join(`,`),this._transmit({command:`CONNECT`,headers:R})};this._webSocket.readyState===t.StompSocketState.OPEN?A():this._webSocket.onopen=A}_setupHeartbeat(x){if(x.version!==p.V1_1&&x.version!==p.V1_2||!x[`heart-beat`])return;let[A,R]=x[`heart-beat`].split(`,`).map(W=>parseInt(W,10));if(this.heartbeatOutgoing!==0&&R!==0){let W=Math.max(this.heartbeatOutgoing,R);this.debug(`send PING every ${W}ms`),this._pinger=new h(W,this._client.heartbeatStrategy,this.debug),this._pinger.start(()=>{this._webSocket.readyState===t.StompSocketState.OPEN&&(this._webSocket.send(e.LF),this.debug(`>>> PING`))})}if(this.heartbeatIncoming!==0&&A!==0){let W=Math.max(this.heartbeatIncoming,A);this.debug(`check PONG every ${W}ms`),this._ponger=setInterval(()=>{let ye=Date.now()-this._lastServerActivityTS;ye>W*this.heartbeatToleranceMultiplier&&(this.debug(`did not receive server activity for the last ${ye}ms`),this.onHeartbeatLost(),this._closeOrDiscardWebsocket())},W)}}_closeOrDiscardWebsocket(){this.discardWebsocketOnCommFailure?(this.debug(`Discarding websocket, the underlying socket may linger for a while`),this.discardWebsocket()):(this.debug(`Issuing close on the websocket`),this._closeWebsocket())}forceDisconnect(){this._webSocket&&(this._webSocket.readyState===t.StompSocketState.CONNECTING||this._webSocket.readyState===t.StompSocketState.OPEN)&&this._closeOrDiscardWebsocket()}_closeWebsocket(){this._webSocket.onmessage=()=>{},this._webSocket.close()}discardWebsocket(){typeof this._webSocket.terminate!=`function`&&n(this._webSocket,x=>this.debug(x)),this._webSocket.terminate()}_transmit(x){let{command:A,headers:R,body:W,binaryBody:ye,skipContentLengthHeader:bt}=x,We=new i({command:A,headers:R,body:W,binaryBody:ye,escapeHeaderValues:this._escapeHeaderValues,skipContentLengthHeader:bt}),pe=We.serialize();if(this.logRawCommunication?this.debug(`>>> ${pe}`):this.debug(`>>> ${We}`),this.forceBinaryWSFrames&&typeof pe==`string`&&(pe=new TextEncoder().encode(pe)),typeof pe!=`string`||!this.splitLargeFrames)this._webSocket.send(pe);else{let Ie=pe;for(;Ie.length>0;){let F=Ie.substring(0,this.maxWebSocketChunkSize);Ie=Ie.substring(this.maxWebSocketChunkSize),this._webSocket.send(F),this.debug(`chunk sent = ${F.length}, remaining = ${Ie.length}`)}}}dispose(){if(this.connected)try{let x=Object.assign({},this.disconnectHeaders);x.receipt||(x.receipt=`close-${this._counter++}`),this.watchForReceipt(x.receipt,A=>{this._closeWebsocket(),this._cleanUp(),this.onDisconnect(A)}),this._transmit({command:`DISCONNECT`,headers:x})}catch(x){this.debug(`Ignoring error during disconnect ${x}`)}else(this._webSocket.readyState===t.StompSocketState.CONNECTING||this._webSocket.readyState===t.StompSocketState.OPEN)&&this._closeWebsocket()}_cleanUp(){this._connected=!1,this._pinger&&(this._pinger.stop(),this._pinger=void 0),this._ponger&&(clearInterval(this._ponger),this._ponger=void 0)}publish(x){let{destination:A,headers:R,body:W,binaryBody:ye,skipContentLengthHeader:bt}=x,We=Object.assign({destination:A},R);this._transmit({command:`SEND`,headers:We,body:W,binaryBody:ye,skipContentLengthHeader:bt})}watchForReceipt(x,A){this._receiptWatchers[x]=A}subscribe(x,A,R={}){R=Object.assign({},R),R.id||(R.id=`sub-${this._counter++}`),R.destination=x,this._subscriptions[R.id]=A,this._transmit({command:`SUBSCRIBE`,headers:R});let W=this;return{id:R.id,unsubscribe(ye){return W.unsubscribe(R.id,ye)}}}unsubscribe(x,A={}){A=Object.assign({},A),delete this._subscriptions[x],A.id=x,this._transmit({command:`UNSUBSCRIBE`,headers:A})}begin(x){let A=x||`tx-${this._counter++}`;this._transmit({command:`BEGIN`,headers:{transaction:A}});let R=this;return{id:A,commit(){R.commit(A)},abort(){R.abort(A)}}}commit(x){this._transmit({command:`COMMIT`,headers:{transaction:x}})}abort(x){this._transmit({command:`ABORT`,headers:{transaction:x}})}ack(x,A,R={}){R=Object.assign({},R),this._connectedVersion===p.V1_2?R.id=x:R[`message-id`]=x,R.subscription=A,this._transmit({command:`ACK`,headers:R})}nack(x,A,R={}){return R=Object.assign({},R),this._connectedVersion===p.V1_2?R.id=x:R[`message-id`]=x,R.subscription=A,this._transmit({command:`NACK`,headers:R})}}class _{get webSocket(){return this._stompHandler?._webSocket}get disconnectHeaders(){return this._disconnectHeaders}set disconnectHeaders(x){this._disconnectHeaders=x,this._stompHandler&&(this._stompHandler.disconnectHeaders=this._disconnectHeaders)}get connected(){return!!this._stompHandler&&this._stompHandler.connected}get connectedVersion(){return this._stompHandler?this._stompHandler.connectedVersion:void 0}get active(){return this.state===t.ActivationState.ACTIVE}_changeState(x){this.state=x,this.onChangeState(x)}constructor(x={}){this.stompVersions=p.default,this.connectionTimeout=0,this.reconnectDelay=5e3,this._nextReconnectDelay=0,this.maxReconnectDelay=9e5,this.reconnectTimeMode=t.ReconnectionTimeMode.LINEAR,this.heartbeatIncoming=1e4,this.heartbeatToleranceMultiplier=2,this.heartbeatOutgoing=1e4,this.heartbeatStrategy=t.TickerStrategy.Interval,this.splitLargeFrames=!1,this.maxWebSocketChunkSize=8192,this.forceBinaryWSFrames=!1,this.appendMissingNULLonIncoming=!1,this.discardWebsocketOnCommFailure=!1,this.state=t.ActivationState.INACTIVE;let A=()=>{};this.debug=A,this.beforeConnect=A,this.onConnect=A,this.onDisconnect=A,this.onUnhandledMessage=A,this.onUnhandledReceipt=A,this.onUnhandledFrame=A,this.onHeartbeatReceived=A,this.onHeartbeatLost=A,this.onStompError=A,this.onWebSocketClose=A,this.onWebSocketError=A,this.logRawCommunication=!1,this.onChangeState=A,this.connectHeaders={},this._disconnectHeaders={},this.configure(x)}configure(x){Object.assign(this,x),this.maxReconnectDelay>0&&this.maxReconnectDelay<this.reconnectDelay&&(this.debug(`Warning: maxReconnectDelay (${this.maxReconnectDelay}ms) is less than reconnectDelay (${this.reconnectDelay}ms). Using reconnectDelay as the maxReconnectDelay delay.`),this.maxReconnectDelay=this.reconnectDelay)}activate(){let x=()=>{if(this.active){this.debug(`Already ACTIVE, ignoring request to activate`);return}this._changeState(t.ActivationState.ACTIVE),this._nextReconnectDelay=this.reconnectDelay,this._connect()};this.state===t.ActivationState.DEACTIVATING?(this.debug(`Waiting for deactivation to finish before activating`),this.deactivate().then(()=>{x()})):x()}async _connect(){if(await this.beforeConnect(this),this._stompHandler){this.debug(`There is already a stompHandler, skipping the call to connect`);return}if(!this.active){this.debug(`Client has been marked inactive, will not attempt to connect`);return}this.connectionTimeout>0&&(this._connectionWatcher&&clearTimeout(this._connectionWatcher),this._connectionWatcher=setTimeout(()=>{this.connected||(this.debug(`Connection not established in ${this.connectionTimeout}ms, closing socket`),this.forceDisconnect())},this.connectionTimeout)),this.debug(`Opening Web Socket...`);let x=this._createWebSocket();this._stompHandler=new g(this,x,{debug:this.debug,stompVersions:this.stompVersions,connectHeaders:this.connectHeaders,disconnectHeaders:this._disconnectHeaders,heartbeatIncoming:this.heartbeatIncoming,heartbeatGracePeriods:this.heartbeatToleranceMultiplier,heartbeatOutgoing:this.heartbeatOutgoing,heartbeatStrategy:this.heartbeatStrategy,splitLargeFrames:this.splitLargeFrames,maxWebSocketChunkSize:this.maxWebSocketChunkSize,forceBinaryWSFrames:this.forceBinaryWSFrames,logRawCommunication:this.logRawCommunication,appendMissingNULLonIncoming:this.appendMissingNULLonIncoming,discardWebsocketOnCommFailure:this.discardWebsocketOnCommFailure,onConnect:A=>{if(this._connectionWatcher&&(clearTimeout(this._connectionWatcher),this._connectionWatcher=void 0),this._nextReconnectDelay=this.reconnectDelay,!this.active){this.debug(`STOMP got connected while deactivate was issued, will disconnect now`),this._disposeStompHandler();return}this.onConnect(A)},onDisconnect:A=>{this.onDisconnect(A)},onStompError:A=>{this.onStompError(A)},onWebSocketClose:A=>{this._stompHandler=void 0,this.state===t.ActivationState.DEACTIVATING&&this._changeState(t.ActivationState.INACTIVE),this.onWebSocketClose(A),this.active&&this._schedule_reconnect()},onWebSocketError:A=>{this.onWebSocketError(A)},onUnhandledMessage:A=>{this.onUnhandledMessage(A)},onUnhandledReceipt:A=>{this.onUnhandledReceipt(A)},onUnhandledFrame:A=>{this.onUnhandledFrame(A)},onHeartbeatReceived:()=>{this.onHeartbeatReceived()},onHeartbeatLost:()=>{this.onHeartbeatLost()}}),this._stompHandler.start()}_createWebSocket(){let x;if(this.webSocketFactory)x=this.webSocketFactory();else if(this.brokerURL)x=new WebSocket(this.brokerURL,this.stompVersions.protocolVersions());else throw new Error(`Either brokerURL or webSocketFactory must be provided`);return x.binaryType=`arraybuffer`,x}_schedule_reconnect(){this._nextReconnectDelay>0&&(this.debug(`STOMP: scheduling reconnection in ${this._nextReconnectDelay}ms`),this._reconnector=setTimeout(()=>{this.reconnectTimeMode===t.ReconnectionTimeMode.EXPONENTIAL&&(this._nextReconnectDelay=this._nextReconnectDelay*2,this.maxReconnectDelay!==0&&(this._nextReconnectDelay=Math.min(this._nextReconnectDelay,this.maxReconnectDelay))),this._connect()},this._nextReconnectDelay))}async deactivate(x={}){let A=x.force||!1,R=this.active,W;if(this.state===t.ActivationState.INACTIVE)return this.debug(`Already INACTIVE, nothing more to do`),Promise.resolve();if(this._changeState(t.ActivationState.DEACTIVATING),this._nextReconnectDelay=0,this._reconnector&&(clearTimeout(this._reconnector),this._reconnector=void 0),this._stompHandler&&this.webSocket.readyState!==t.StompSocketState.CLOSED){let ye=this._stompHandler.onWebSocketClose;W=new Promise((bt,We)=>{this._stompHandler.onWebSocketClose=pe=>{ye(pe),bt()}})}else return this._changeState(t.ActivationState.INACTIVE),Promise.resolve();return A?this._stompHandler?.discardWebsocket():R&&this._disposeStompHandler(),W}forceDisconnect(){this._stompHandler&&this._stompHandler.forceDisconnect()}_disposeStompHandler(){this._stompHandler&&this._stompHandler.dispose()}publish(x){this._checkConnection(),this._stompHandler.publish(x)}_checkConnection(){if(!this.connected)throw new TypeError(`There is no underlying STOMP connection`)}watchForReceipt(x,A){this._checkConnection(),this._stompHandler.watchForReceipt(x,A)}subscribe(x,A,R={}){return this._checkConnection(),this._stompHandler.subscribe(x,A,R)}unsubscribe(x,A={}){this._checkConnection(),this._stompHandler.unsubscribe(x,A)}begin(x){return this._checkConnection(),this._stompHandler.begin(x)}commit(x){this._checkConnection(),this._stompHandler.commit(x)}abort(x){this._checkConnection(),this._stompHandler.abort(x)}ack(x,A,R={}){this._checkConnection(),this._stompHandler.ack(x,A,R)}nack(x,A,R={}){this._checkConnection(),this._stompHandler.nack(x,A,R)}}class b{}class y{}class I{constructor(x){this.client=x}get outgoing(){return this.client.heartbeatOutgoing}set outgoing(x){this.client.heartbeatOutgoing=x}get incoming(){return this.client.heartbeatIncoming}set incoming(x){this.client.heartbeatIncoming=x}}class P extends _{constructor(x){super(),this.maxWebSocketFrameSize=16384,this._heartbeatInfo=new I(this),this.reconnect_delay=0,this.webSocketFactory=x,this.debug=(...A)=>{console.log(...A)}}_parseConnect(...x){let A,R,W,ye={};if(x.length<2)throw new Error(`Connect requires at least 2 arguments`);return typeof x[1]==`function`?[ye,R,W,A]=x:x.length===6?[ye.login,ye.passcode,R,W,A,ye.host]=x:[ye.login,ye.passcode,R,W,A]=x,[ye,R,W,A]}connect(...x){let A=this._parseConnect(...x);A[0]&&(this.connectHeaders=A[0]),A[1]&&(this.onConnect=A[1]),A[2]&&(this.onStompError=A[2]),A[3]&&(this.onWebSocketClose=A[3]),super.activate()}disconnect(x,A={}){x&&(this.onDisconnect=x),this.disconnectHeaders=A,super.deactivate()}send(x,A={},R=``){A=Object.assign({},A);let W=A[`content-length`]===!1;W&&delete A[`content-length`],this.publish({destination:x,headers:A,body:R,skipContentLengthHeader:W})}set reconnect_delay(x){this.reconnectDelay=x}get ws(){return this.webSocket}get version(){return this.connectedVersion}get onreceive(){return this.onUnhandledMessage}set onreceive(x){this.onUnhandledMessage=x}get onreceipt(){return this.onUnhandledReceipt}set onreceipt(x){this.onUnhandledReceipt=x}get heartbeat(){return this._heartbeatInfo}set heartbeat(x){this.heartbeatIncoming=x.incoming,this.heartbeatOutgoing=x.outgoing}}class ie{static client(x,A){A??=p.default.protocolVersions();let R=()=>{return new(ie.WebSocketClass||WebSocket)(x,A)};return new P(R)}static over(x){let A;return typeof x==`function`?A=x:(console.warn(`Stomp.over did not receive a factory, auto reconnect will not work. Please see https://stomp-js.github.io/api-docs/latest/classes/Stomp.html#over`),A=()=>x),new P(A)}}ie.WebSocketClass=null,t.Client=_,t.CompatClient=P,t.FrameImpl=i,t.Parser=l,t.Stomp=ie,t.StompConfig=b,t.StompHeaders=y,t.Versions=p}))});var on=null;var ac=!1;var za=1;var pE=null;var St=Symbol(`SIGNAL`);function ge(t){let n=on;return on=t,n}function lc(){return on}var Ua={version:0,lastCleanEpoch:0,dirty:!1,producers:void 0,producersTail:void 0,consumers:void 0,consumersTail:void 0,recomputing:!1,consumerAllowSignalWrites:!1,consumerIsAlwaysLive:!1,kind:`unknown`,producerMustRecompute:()=>!1,producerRecomputeValue:()=>{},consumerMarkedDirty:()=>{},consumerOnSignalRead:()=>{}};function Ri(t){if(ac)throw new Error(``);if(on===null)return;on.consumerOnSignalRead(t);let n=on.producersTail;if(n!==void 0&&n.producer===t)return;let e,i=on.recomputing;if(i&&(e=n!==void 0?n.nextProducer:on.producers,e!==void 0&&e.producer===t)){on.producersTail=e,e.lastReadVersion=t.version,e.knownValidAtEpoch=za;return}let a=t.consumersTail;if(a!==void 0&&a.consumer===on&&(!i||a.knownValidAtEpoch===za))return;let o=Go(on),r={producer:t,consumer:on,nextProducer:e,prevConsumer:void 0,knownValidAtEpoch:za,lastReadVersion:t.version,nextConsumer:void 0};on.producersTail=r,n!==void 0?n.nextProducer=r:on.producers=r,o&&Yb(t,r)}function Gb(){za++}function fs(t){if(!(Go(t)&&!t.dirty)&&!(!t.dirty&&t.lastCleanEpoch===za)){if(!t.producerMustRecompute(t)&&!$o(t)){sc(t);return}t.producerRecomputeValue(t),sc(t)}}function qm(t){if(t.consumers===void 0)return;let n=ac;ac=!0;try{for(let e=t.consumers;e!==void 0;e=e.nextConsumer){let i=e.consumer;i.dirty||hE(i)}}finally{ac=n}}function Ym(){return on?.consumerAllowSignalWrites!==!1}function hE(t){t.dirty=!0,qm(t),t.consumerMarkedDirty?.(t)}function sc(t){t.dirty=!1,t.lastCleanEpoch=za}function ca(t){return t&&Wb(t),ge(t)}function Wb(t){if(t.producersTail?.knownValidAtEpoch===za){let n=t.producers;for(;n!==void 0;)n.knownValidAtEpoch=null,n=n.nextProducer}t.producersTail=void 0,t.recomputing=!0}function $a(t,n){ge(n),t&&qb(t)}function qb(t){t.recomputing=!1;let n=t.producersTail,e=n!==void 0?n.nextProducer:t.producers;if(e!==void 0){if(Go(t))do e=Km(e);while(e!==void 0);n!==void 0?n.nextProducer=void 0:t.producers=void 0}}function $o(t){for(let n=t.producers;n!==void 0;n=n.nextProducer){let e=n.producer,i=n.lastReadVersion;if(i!==e.version||(fs(e),i!==e.version))return!0}return!1}function da(t){if(Go(t)){let n=t.producers;for(;n!==void 0;)n=Km(n)}t.producers=void 0,t.producersTail=void 0,t.consumers=void 0,t.consumersTail=void 0}function Yb(t,n){let e=t.consumersTail,i=Go(t);if(e!==void 0?(n.nextConsumer=e.nextConsumer,e.nextConsumer=n):(n.nextConsumer=void 0,t.consumers=n),n.prevConsumer=e,t.consumersTail=n,!i)for(let a=t.producers;a!==void 0;a=a.nextProducer)Yb(a.producer,a)}function Km(t){let n=t.producer,e=t.nextProducer,i=t.nextConsumer,a=t.prevConsumer;if(t.nextConsumer=void 0,t.prevConsumer=void 0,i!==void 0?i.prevConsumer=a:n.consumersTail=a,a!==void 0)a.nextConsumer=i;else if(n.consumers=i,!Go(n)){let o=n.producers;for(;o!==void 0;)o=Km(o)}return e}function Go(t){return t.consumerIsAlwaysLive||t.consumers!==void 0}function cc(t){pE?.(t)}function dc(t,n){return Object.is(t,n)}function gs(t,n){let e=Object.create(fE);e.computation=t,n!==void 0&&(e.equal=n);let i=()=>{if(fs(e),Ri(e),e.value===Ha)throw e.error;return e.value};return i[St]=e,cc(e),i}var oc=Symbol(`UNSET`);var rc=Symbol(`COMPUTING`);var Ha=Symbol(`ERRORED`);var fE=s(r({},Ua),{value:oc,dirty:!0,error:null,equal:dc,kind:`computed`,producerMustRecompute(t){return t.value===oc||t.value===rc},producerRecomputeValue(t){if(t.value===rc)throw new Error(``);let n=t.value;t.value=rc;let e=ca(t),i,a=!1;try{i=t.computation(),ge(null),a=n!==oc&&n!==Ha&&i!==Ha&&t.equal(n,i)}catch(o){i=Ha,t.error=o}finally{$a(t,e)}if(a){t.value=n;return}t.value=i,t.version++}});function gE(){throw new Error}var Kb=gE;function Qb(t){Kb(t)}function Qm(t){Kb=t}var bE=null;function Xm(t,n){let e=Object.create(Wo);e.value=t,n!==void 0&&(e.equal=n);let i=()=>Xb(e);return i[St]=e,cc(e),[i,r=>ua(e,r),r=>Zm(e,r)]}function Xb(t){return Ri(t),t.value}function ua(t,n){Ym()||Qb(t),t.equal(t.value,n)||(t.value=n,vE(t))}function Zm(t,n){Ym()||Qb(t),ua(t,n(t.value))}var Wo=s(r({},Ua),{equal:dc,value:void 0,kind:`signal`});function vE(t){t.version++,Gb(),qm(t),bE?.(t)}var Jm=s(r({},Ua),{consumerIsAlwaysLive:!0,consumerAllowSignalWrites:!0,dirty:!0,kind:`effect`});function ep(t){if(t.dirty=!1,t.version>0&&!$o(t))return;t.version++;let n=ca(t);try{t.cleanup(),t.fn()}finally{$a(t,n)}}var tp;function uc(){return tp}function pi(t){let n=tp;return tp=t,n}var Zb=Symbol(`NotFound`);function qo(t){return t===Zb||t?.name===`ɵNotFound`}function Jb(t){let n=ge(null);try{return t()}finally{ge(n)}}function Te(t){return typeof t==`function`}function Yo(t){let e=t(i=>{Error.call(i),i.stack=new Error().stack});return e.prototype=Object.create(Error.prototype),e.prototype.constructor=e,e}var mc=Yo(t=>function(e){t(this),this.message=e?`${e.length} errors occurred during unsubscription:
${e.map((i,a)=>`${a+1}) ${i.toString()}`).join(`
  `)}`:``,this.name=`UnsubscriptionError`,this.errors=e});function Ga(t,n){if(t){let e=t.indexOf(n);0<=e&&t.splice(e,1)}}var De=class t{constructor(n){this.initialTeardown=n,this.closed=!1,this._parentage=null,this._finalizers=null}unsubscribe(){let n;if(!this.closed){this.closed=!0;let{_parentage:e}=this;if(e)if(this._parentage=null,Array.isArray(e))for(let o of e)o.remove(this);else e.remove(this);let{initialTeardown:i}=this;if(Te(i))try{i()}catch(o){n=o instanceof mc?o.errors:[o]}let{_finalizers:a}=this;if(a){this._finalizers=null;for(let o of a)try{ev(o)}catch(r){n=n??[],r instanceof mc?n=[...n,...r.errors]:n.push(r)}}if(n)throw new mc(n)}}add(n){var e;if(n&&n!==this)if(this.closed)ev(n);else{if(n instanceof t){if(n.closed||n._hasParent(this))return;n._addParent(this)}(this._finalizers=(e=this._finalizers)!==null&&e!==void 0?e:[]).push(n)}}_hasParent(n){let{_parentage:e}=this;return e===n||Array.isArray(e)&&e.includes(n)}_addParent(n){let{_parentage:e}=this;this._parentage=Array.isArray(e)?(e.push(n),e):e?[e,n]:n}_removeParent(n){let{_parentage:e}=this;e===n?this._parentage=null:Array.isArray(e)&&Ga(e,n)}remove(n){let{_finalizers:e}=this;e&&Ga(e,n),n instanceof t&&n._removeParent(this)}};De.EMPTY=(()=>{let t=new De;return t.closed=!0,t})();var np=De.EMPTY;function pc(t){return t instanceof De||t&&`closed`in t&&Te(t.remove)&&Te(t.add)&&Te(t.unsubscribe)}function ev(t){Te(t)?t():t.unsubscribe()}var Gn={onUnhandledError:null,onStoppedNotification:null,Promise:void 0,useDeprecatedSynchronousErrorHandling:!1,useDeprecatedNextContext:!1};var Ko={setTimeout(t,n,...e){let{delegate:i}=Ko;return i?.setTimeout?i.setTimeout(t,n,...e):setTimeout(t,n,...e)},clearTimeout(t){let{delegate:n}=Ko;return(n?.clearTimeout||clearTimeout)(t)},delegate:void 0};function hc(t){Ko.setTimeout(()=>{let{onUnhandledError:n}=Gn;if(n)n(t);else throw t})}function bs(){}var tv=ip(`C`,void 0,void 0);function nv(t){return ip(`E`,void 0,t)}function iv(t){return ip(`N`,t,void 0)}function ip(t,n,e){return{kind:t,value:n,error:e}}var Wa=null;function Qo(t){if(Gn.useDeprecatedSynchronousErrorHandling){let n=!Wa;if(n&&(Wa={errorThrown:!1,error:null}),t(),n){let{errorThrown:e,error:i}=Wa;if(Wa=null,e)throw i}}else t()}function av(t){Gn.useDeprecatedSynchronousErrorHandling&&Wa&&(Wa.errorThrown=!0,Wa.error=t)}var qa=class extends De{constructor(n){super(),this.isStopped=!1,n?(this.destination=n,pc(n)&&n.add(this)):this.destination=xE}static create(n,e,i){return new Wn(n,e,i)}next(n){this.isStopped?op(iv(n),this):this._next(n)}error(n){this.isStopped?op(nv(n),this):(this.isStopped=!0,this._error(n))}complete(){this.isStopped?op(tv,this):(this.isStopped=!0,this._complete())}unsubscribe(){this.closed||(this.isStopped=!0,super.unsubscribe(),this.destination=null)}_next(n){this.destination.next(n)}_error(n){try{this.destination.error(n)}finally{this.unsubscribe()}}_complete(){try{this.destination.complete()}finally{this.unsubscribe()}}};var _E=Function.prototype.bind;function ap(t,n){return _E.call(t,n)}var rp=class{constructor(n){this.partialObserver=n}next(n){let{partialObserver:e}=this;if(e.next)try{e.next(n)}catch(i){fc(i)}}error(n){let{partialObserver:e}=this;if(e.error)try{e.error(n)}catch(i){fc(i)}else fc(n)}complete(){let{partialObserver:n}=this;if(n.complete)try{n.complete()}catch(e){fc(e)}}};var Wn=class extends qa{constructor(n,e,i){super();let a;if(Te(n)||!n)a={next:n??void 0,error:e??void 0,complete:i??void 0};else{let o;this&&Gn.useDeprecatedNextContext?(o=Object.create(n),o.unsubscribe=()=>this.unsubscribe(),a={next:n.next&&ap(n.next,o),error:n.error&&ap(n.error,o),complete:n.complete&&ap(n.complete,o)}):a=n}this.destination=new rp(a)}};function fc(t){Gn.useDeprecatedSynchronousErrorHandling?av(t):hc(t)}function yE(t){throw t}function op(t,n){let{onStoppedNotification:e}=Gn;e&&Ko.setTimeout(()=>e(t,n))}var xE={closed:!0,next:bs,error:yE,complete:bs};var Xo=typeof Symbol==`function`&&Symbol.observable||`@@observable`;function qn(t){return t}function ov(t){return t.length===0?qn:t.length===1?t[0]:function(e){return t.reduce((i,a)=>a(i),e)}}var Ce=class t{constructor(n){n&&(this._subscribe=n)}lift(n){let e=new t;return e.source=this,e.operator=n,e}subscribe(n,e,i){let a=SE(n)?n:new Wn(n,e,i);return Qo(()=>{let{operator:o,source:r}=this;a.add(o?o.call(a,r):r?this._subscribe(a):this._trySubscribe(a))}),a}_trySubscribe(n){try{return this._subscribe(n)}catch(e){n.error(e)}}forEach(n,e){return e=rv(e),new e((i,a)=>{let o=new Wn({next:r=>{try{n(r)}catch(s){a(s),o.unsubscribe()}},error:a,complete:i});this.subscribe(o)})}_subscribe(n){var e;return(e=this.source)===null||e===void 0?void 0:e.subscribe(n)}[Xo](){return this}pipe(...n){return ov(n)(this)}toPromise(n){return n=rv(n),new n((e,i)=>{let a;this.subscribe(o=>a=o,o=>i(o),()=>e(a))})}};Ce.create=t=>new Ce(t);function rv(t){var n;return(n=t??Gn.Promise)!==null&&n!==void 0?n:Promise}function CE(t){return t&&Te(t.next)&&Te(t.error)&&Te(t.complete)}function SE(t){return t&&t instanceof qa||CE(t)&&pc(t)}function wE(t){return Te(t?.lift)}function Le(t){return n=>{if(wE(n))return n.lift(function(e){try{return t(e,this)}catch(i){this.error(i)}});throw new TypeError(`Unable to lift unknown Observable type`)}}function je(t,n,e,i,a){return new sp(t,n,e,i,a)}var sp=class extends qa{constructor(n,e,i,a,o,r){super(n),this.onFinalize=o,this.shouldUnsubscribe=r,this._next=e?function(s){try{e(s)}catch(l){n.error(l)}}:super._next,this._error=a?function(s){try{a(s)}catch(l){n.error(l)}finally{this.unsubscribe()}}:super._error,this._complete=i?function(){try{i()}catch(s){n.error(s)}finally{this.unsubscribe()}}:super._complete}unsubscribe(){var n;if(!this.shouldUnsubscribe||this.shouldUnsubscribe()){let{closed:e}=this;super.unsubscribe(),!e&&((n=this.onFinalize)===null||n===void 0||n.call(this))}}};var sv=Yo(t=>function(){t(this),this.name=`ObjectUnsubscribedError`,this.message=`object unsubscribed`});var V=class extends Ce{constructor(){super(),this.closed=!1,this.currentObservers=null,this.observers=[],this.isStopped=!1,this.hasError=!1,this.thrownError=null}lift(n){let e=new gc(this,this);return e.operator=n,e}_throwIfClosed(){if(this.closed)throw new sv}next(n){Qo(()=>{if(this._throwIfClosed(),!this.isStopped){this.currentObservers||(this.currentObservers=Array.from(this.observers));for(let e of this.currentObservers)e.next(n)}})}error(n){Qo(()=>{if(this._throwIfClosed(),!this.isStopped){this.hasError=this.isStopped=!0,this.thrownError=n;let{observers:e}=this;for(;e.length;)e.shift().error(n)}})}complete(){Qo(()=>{if(this._throwIfClosed(),!this.isStopped){this.isStopped=!0;let{observers:n}=this;for(;n.length;)n.shift().complete()}})}unsubscribe(){this.isStopped=this.closed=!0,this.observers=this.currentObservers=null}get observed(){var n;return((n=this.observers)===null||n===void 0?void 0:n.length)>0}_trySubscribe(n){return this._throwIfClosed(),super._trySubscribe(n)}_subscribe(n){return this._throwIfClosed(),this._checkFinalizedStatuses(n),this._innerSubscribe(n)}_innerSubscribe(n){let{hasError:e,isStopped:i,observers:a}=this;return e||i?np:(this.currentObservers=null,a.push(n),new De(()=>{this.currentObservers=null,Ga(a,n)}))}_checkFinalizedStatuses(n){let{hasError:e,thrownError:i,isStopped:a}=this;e?n.error(i):a&&n.complete()}asObservable(){let n=new Ce;return n.source=this,n}};V.create=(t,n)=>new gc(t,n);var gc=class extends V{constructor(n,e){super(),this.destination=n,this.source=e}next(n){var e,i;(i=(e=this.destination)===null||e===void 0?void 0:e.next)===null||i===void 0||i.call(e,n)}error(n){var e,i;(i=(e=this.destination)===null||e===void 0?void 0:e.error)===null||i===void 0||i.call(e,n)}complete(){var n,e;(e=(n=this.destination)===null||n===void 0?void 0:n.complete)===null||e===void 0||e.call(n)}_subscribe(n){var e,i;return(i=(e=this.source)===null||e===void 0?void 0:e.subscribe(n))!==null&&i!==void 0?i:np}};var hi=class extends V{constructor(n){super(),this._value=n}get value(){return this.getValue()}_subscribe(n){let e=super._subscribe(n);return!e.closed&&n.next(this._value),e}getValue(){let{hasError:n,thrownError:e,_value:i}=this;if(n)throw e;return this._throwIfClosed(),i}next(n){super.next(this._value=n)}};var vs={now(){return(vs.delegate||Date).now()},delegate:void 0};var Yn=class extends V{constructor(n=1/0,e=1/0,i=vs){super(),this._bufferSize=n,this._windowTime=e,this._timestampProvider=i,this._buffer=[],this._infiniteTimeWindow=!0,this._infiniteTimeWindow=e===1/0,this._bufferSize=Math.max(1,n),this._windowTime=Math.max(1,e)}next(n){let{isStopped:e,_buffer:i,_infiniteTimeWindow:a,_timestampProvider:o,_windowTime:r}=this;e||(i.push(n),!a&&i.push(o.now()+r)),this._trimBuffer(),super.next(n)}_subscribe(n){this._throwIfClosed(),this._trimBuffer();let e=this._innerSubscribe(n),{_infiniteTimeWindow:i,_buffer:a}=this,o=a.slice();for(let r=0;r<o.length&&!n.closed;r+=i?1:2)n.next(o[r]);return this._checkFinalizedStatuses(n),e}_trimBuffer(){let{_bufferSize:n,_timestampProvider:e,_buffer:i,_infiniteTimeWindow:a}=this,o=(a?1:2)*n;if(n<1/0&&o<i.length&&i.splice(0,i.length-o),!a){let r=e.now(),s=0;for(let l=1;l<i.length&&i[l]<=r;l+=2)s=l;s&&i.splice(0,s+1)}}};var bc=class extends De{constructor(n,e){super()}schedule(n,e=0){return this}};var _s={setInterval(t,n,...e){let{delegate:i}=_s;return i?.setInterval?i.setInterval(t,n,...e):setInterval(t,n,...e)},clearInterval(t){let{delegate:n}=_s;return(n?.clearInterval||clearInterval)(t)},delegate:void 0};var vc=class extends bc{constructor(n,e){super(n,e),this.scheduler=n,this.work=e,this.pending=!1}schedule(n,e=0){var i;if(this.closed)return this;this.state=n;let a=this.id,o=this.scheduler;return a!=null&&(this.id=this.recycleAsyncId(o,a,e)),this.pending=!0,this.delay=e,this.id=(i=this.id)!==null&&i!==void 0?i:this.requestAsyncId(o,this.id,e),this}requestAsyncId(n,e,i=0){return _s.setInterval(n.flush.bind(n,this),i)}recycleAsyncId(n,e,i=0){if(i!=null&&this.delay===i&&this.pending===!1)return e;e!=null&&_s.clearInterval(e)}execute(n,e){if(this.closed)return new Error(`executing a cancelled action`);this.pending=!1;let i=this._execute(n,e);if(i)return i;this.pending===!1&&this.id!=null&&(this.id=this.recycleAsyncId(this.scheduler,this.id,null))}_execute(n,e){let i=!1,a;try{this.work(n)}catch(o){i=!0,a=o||new Error(`Scheduled action threw falsy error`)}if(i)return this.unsubscribe(),a}unsubscribe(){if(!this.closed){let{id:n,scheduler:e}=this,{actions:i}=e;this.work=this.state=this.scheduler=null,this.pending=!1,Ga(i,this),n!=null&&(this.id=this.recycleAsyncId(e,n,null)),this.delay=null,super.unsubscribe()}}};var Zo=class t{constructor(n,e=t.now){this.schedulerActionCtor=n,this.now=e}schedule(n,e=0,i){return new this.schedulerActionCtor(this,n).schedule(i,e)}};Zo.now=vs.now;var _c=class extends Zo{constructor(n,e=Zo.now){super(n,e),this.actions=[],this._active=!1}flush(n){let{actions:e}=this;if(this._active){e.push(n);return}let i;this._active=!0;do if(i=n.execute(n.state,n.delay))break;while(n=e.shift());if(this._active=!1,i){for(;n=e.shift();)n.unsubscribe();throw i}}};var ys=new _c(vc);var lv=ys;var Oi=new Ce(t=>t.complete());function yc(t){return t&&Te(t.schedule)}function lp(t){return t[t.length-1]}function xc(t){return Te(lp(t))?t.pop():void 0}function fi(t){return yc(lp(t))?t.pop():void 0}function cv(t,n){return typeof lp(t)==`number`?t.pop():n}function uv(t,n,e,i){function a(o){return o instanceof e?o:new e(function(r){r(o)})}return new(e||(e=Promise))(function(o,r){function s(p){try{h(i.next(p))}catch(g){r(g)}}function l(p){try{h(i.throw(p))}catch(g){r(g)}}function h(p){p.done?o(p.value):a(p.value).then(s,l)}h((i=i.apply(t,n||[])).next())})}function dv(t){var n=typeof Symbol==`function`&&Symbol.iterator,e=n&&t[n],i=0;if(e)return e.call(t);if(t&&typeof t.length==`number`)return{next:function(){return t&&i>=t.length&&(t=void 0),{value:t&&t[i++],done:!t}}};throw new TypeError(n?`Object is not iterable.`:`Symbol.iterator is not defined.`)}function Ya(t){return this instanceof Ya?(this.v=t,this):new Ya(t)}function mv(t,n,e){if(!Symbol.asyncIterator)throw new TypeError(`Symbol.asyncIterator is not defined.`);var i=e.apply(t,n||[]),a,o=[];return a=Object.create((typeof AsyncIterator==`function`?AsyncIterator:Object).prototype),s(`next`),s(`throw`),s(`return`,r),a[Symbol.asyncIterator]=function(){return this},a;function r(b){return function(y){return Promise.resolve(y).then(b,g)}}function s(b,y){i[b]&&(a[b]=function(I){return new Promise(function(P,ie){o.push([b,I,P,ie])>1||l(b,I)})},y&&(a[b]=y(a[b])))}function l(b,y){try{h(i[b](y))}catch(I){_(o[0][3],I)}}function h(b){b.value instanceof Ya?Promise.resolve(b.value.v).then(p,g):_(o[0][2],b)}function p(b){l(`next`,b)}function g(b){l(`throw`,b)}function _(b,y){b(y),o.shift(),o.length&&l(o[0][0],o[0][1])}}function pv(t){if(!Symbol.asyncIterator)throw new TypeError(`Symbol.asyncIterator is not defined.`);var n=t[Symbol.asyncIterator],e;return n?n.call(t):(t=typeof dv==`function`?dv(t):t[Symbol.iterator](),e={},i(`next`),i(`throw`),i(`return`),e[Symbol.asyncIterator]=function(){return this},e);function i(o){e[o]=t[o]&&function(r){return new Promise(function(s,l){r=t[o](r),a(s,l,r.done,r.value)})}}function a(o,r,s,l){Promise.resolve(l).then(function(h){o({value:h,done:s})},r)}}var Cc=(t=>t&&typeof t.length==`number`&&typeof t!=`function`);function Sc(t){return Te(t?.then)}function wc(t){return Te(t[Xo])}function Ec(t){return Symbol.asyncIterator&&Te(t?.[Symbol.asyncIterator])}function Dc(t){return new TypeError(`You provided ${t!==null&&typeof t==`object`?`an invalid object`:`'${t}'`} where a stream was expected. You can provide an Observable, Promise, ReadableStream, Array, AsyncIterable, or Iterable.`)}function EE(){return typeof Symbol!=`function`||!Symbol.iterator?`@@iterator`:Symbol.iterator}var Ic=EE();function kc(t){return Te(t?.[Ic])}function Mc(t){return mv(this,arguments,function*(){let e=t.getReader();try{for(;;){let{value:i,done:a}=yield Ya(e.read());if(a)return yield Ya(void 0);yield yield Ya(i)}}finally{e.releaseLock()}})}function Tc(t){return Te(t?.getReader)}function ut(t){if(t instanceof Ce)return t;if(t!=null){if(wc(t))return DE(t);if(Cc(t))return IE(t);if(Sc(t))return kE(t);if(Ec(t))return hv(t);if(kc(t))return ME(t);if(Tc(t))return TE(t)}throw Dc(t)}function DE(t){return new Ce(n=>{let e=t[Xo]();if(Te(e.subscribe))return e.subscribe(n);throw new TypeError(`Provided object does not correctly implement Symbol.observable`)})}function IE(t){return new Ce(n=>{for(let e=0;e<t.length&&!n.closed;e++)n.next(t[e]);n.complete()})}function kE(t){return new Ce(n=>{t.then(e=>{n.closed||(n.next(e),n.complete())},e=>n.error(e)).then(null,hc)})}function ME(t){return new Ce(n=>{for(let e of t)if(n.next(e),n.closed)return;n.complete()})}function hv(t){return new Ce(n=>{NE(t,n).catch(e=>n.error(e))})}function TE(t){return hv(Mc(t))}function NE(t,n){var e,i,a,o;return uv(this,void 0,void 0,function*(){try{for(e=pv(t);i=yield e.next(),!i.done;){let r=i.value;if(n.next(r),n.closed)return}}catch(r){a={error:r}}finally{try{i&&!i.done&&(o=e.return)&&(yield o.call(e))}finally{if(a)throw a.error}}n.complete()})}function hn(t,n,e,i=0,a=!1){let o=n.schedule(function(){e(),a?t.add(this.schedule(null,i)):this.unsubscribe()},i);if(t.add(o),!a)return o}function Nc(t,n=0){return Le((e,i)=>{e.subscribe(je(i,a=>hn(i,t,()=>i.next(a),n),()=>hn(i,t,()=>i.complete(),n),a=>hn(i,t,()=>i.error(a),n)))})}function Ac(t,n=0){return Le((e,i)=>{i.add(t.schedule(()=>e.subscribe(i),n))})}function fv(t,n){return ut(t).pipe(Ac(n),Nc(n))}function gv(t,n){return ut(t).pipe(Ac(n),Nc(n))}function bv(t,n){return new Ce(e=>{let i=0;return n.schedule(function(){i===t.length?e.complete():(e.next(t[i++]),e.closed||this.schedule())})})}function vv(t,n){return new Ce(e=>{let i;return hn(e,n,()=>{i=t[Ic](),hn(e,n,()=>{let a,o;try{({value:a,done:o}=i.next())}catch(r){e.error(r);return}o?e.complete():e.next(a)},0,!0)}),()=>Te(i?.return)&&i.return()})}function Rc(t,n){if(!t)throw new Error(`Iterable cannot be null`);return new Ce(e=>{hn(e,n,()=>{let i=t[Symbol.asyncIterator]();hn(e,n,()=>{i.next().then(a=>{a.done?e.complete():e.next(a.value)})},0,!0)})})}function _v(t,n){return Rc(Mc(t),n)}function yv(t,n){if(t!=null){if(wc(t))return fv(t,n);if(Cc(t))return bv(t,n);if(Sc(t))return gv(t,n);if(Ec(t))return Rc(t,n);if(kc(t))return vv(t,n);if(Tc(t))return _v(t,n)}throw Dc(t)}function kn(t,n){return n?yv(t,n):ut(t)}function Mn(...t){return kn(t,fi(t))}var xv=Yo(t=>function(){t(this),this.name=`EmptyError`,this.message=`no elements in sequence`});function wt(t,n){let e=typeof n==`object`;return new Promise((i,a)=>{let o=new Wn({next:r=>{i(r),o.unsubscribe()},error:a,complete:()=>{e?i(n.defaultValue):a(new xv)}});t.subscribe(o)})}function Cv(t){return t instanceof Date&&!isNaN(t)}function st(t,n){return Le((e,i)=>{let a=0;e.subscribe(je(i,o=>{i.next(t.call(n,o,a++))}))})}var{isArray:AE}=Array;function RE(t,n){return AE(n)?t(...n):t(n)}function Oc(t){return st(n=>RE(t,n))}var{isArray:OE}=Array,{getPrototypeOf:PE,prototype:FE,keys:LE}=Object;function Pc(t){if(t.length===1){let n=t[0];if(OE(n))return{args:n,keys:null};if(VE(n)){let e=LE(n);return{args:e.map(i=>n[i]),keys:e}}}return{args:t,keys:null}}function VE(t){return t&&typeof t==`object`&&PE(t)===FE}function Fc(t,n){return t.reduce((e,i,a)=>(e[i]=n[a],e),{})}function cp(...t){let n=fi(t),e=xc(t),{args:i,keys:a}=Pc(t);if(i.length===0)return kn([],n);let o=new Ce(BE(i,n,a?r=>Fc(a,r):qn));return e?o.pipe(Oc(e)):o}function BE(t,n,e=qn){return i=>{Sv(n,()=>{let{length:a}=t,o=new Array(a),r=a,s=a;for(let l=0;l<a;l++)Sv(n,()=>{let h=kn(t[l],n),p=!1;h.subscribe(je(i,g=>{o[l]=g,p||(p=!0,s--),s||i.next(e(o.slice()))},()=>{--r||i.complete()}))},i)},i)}}function Sv(t,n,e){t?hn(e,t,n):n()}function wv(t,n,e,i,a,o,r,s){let l=[],h=0,p=0,g=!1,_=()=>{g&&!l.length&&!h&&n.complete()},b=I=>h<i?y(I):l.push(I),y=I=>{o&&n.next(I),h++;let P=!1;ut(e(I,p++)).subscribe(je(n,ie=>{a?.(ie),o?b(ie):n.next(ie)},()=>{P=!0},void 0,()=>{if(P)try{for(h--;l.length&&h<i;){let ie=l.shift();r?hn(n,r,()=>y(ie)):y(ie)}_()}catch(ie){n.error(ie)}}))};return t.subscribe(je(n,b,()=>{g=!0,_()})),()=>{s?.()}}function Jo(t,n,e=1/0){return Te(n)?Jo((i,a)=>st((o,r)=>n(i,o,a,r))(ut(t(i,a))),e):(typeof n==`number`&&(e=n),Le((i,a)=>wv(i,a,t,e)))}function Lc(t=1/0){return Jo(qn,t)}function Ev(){return Lc(1)}function er(...t){return Ev()(kn(t,fi(t)))}function Ka(t){return new Ce(n=>{ut(t()).subscribe(n)})}function dp(...t){let n=xc(t),{args:e,keys:i}=Pc(t),a=new Ce(o=>{let{length:r}=e;if(!r){o.complete();return}let s=new Array(r),l=r,h=r;for(let p=0;p<r;p++){let g=!1;ut(e[p]).subscribe(je(o,_=>{g||(g=!0,h--),s[p]=_},()=>l--,void 0,()=>{(!l||!g)&&(h||o.next(i?Fc(i,s):s),o.complete())}))}});return n?a.pipe(Oc(n)):a}function xs(t=0,n,e=lv){let i=-1;return n!=null&&(yc(n)?e=n:i=n),new Ce(a=>{let o=Cv(t)?+t-e.now():t;o<0&&(o=0);let r=0;return e.schedule(function(){a.closed||(a.next(r++),0<=i?this.schedule(void 0,i):a.complete())},o)})}function kt(...t){let n=fi(t),e=cv(t,1/0),i=t;return i.length?i.length===1?ut(i[0]):Lc(e)(kn(i,n)):Oi}function Qe(t,n){return Le((e,i)=>{let a=0;e.subscribe(je(i,o=>t.call(n,o,a++)&&i.next(o)))})}function Dv(t){return Le((n,e)=>{let i=!1,a=null,o=null,r=!1,s=()=>{if(o?.unsubscribe(),o=null,i){i=!1;let h=a;a=null,e.next(h)}r&&e.complete()},l=()=>{o=null,r&&e.complete()};n.subscribe(je(e,h=>{i=!0,a=h,o||ut(t(h)).subscribe(o=je(e,s,l))},()=>{r=!0,(!i||!o||o.closed)&&e.complete()}))})}function Vc(t,n=ys){return Dv(()=>xs(t,n))}function up(t,n){return Te(n)?Jo(t,n,1):Jo(t,1)}function Pi(t,n=ys){return Le((e,i)=>{let a=null,o=null,r=null,s=()=>{if(a){a.unsubscribe(),a=null;let h=o;o=null,i.next(h)}};function l(){let h=r+t,p=n.now();if(p<h){a=this.schedule(void 0,h-p),i.add(a);return}s()}e.subscribe(je(i,h=>{o=h,r=n.now(),a||(a=n.schedule(l,t),i.add(a))},()=>{s(),i.complete()},void 0,()=>{o=a=null}))})}function vn(t){return t<=0?()=>Oi:Le((n,e)=>{let i=0;n.subscribe(je(e,a=>{++i<=t&&(e.next(a),t<=i&&e.complete())}))})}function Bc(t,n=qn){return t=t??jE,Le((e,i)=>{let a,o=!0;e.subscribe(je(i,r=>{let s=n(r);(o||!t(a,s))&&(o=!1,a=s,i.next(r))}))})}function jE(t,n){return t===n}function mp(t){return Le((n,e)=>{try{n.subscribe(e)}finally{e.add(t)}})}function jc(){return Le((t,n)=>{let e,i=!1;t.subscribe(je(n,a=>{let o=e;e=a,i&&n.next([o,a]),i=!0}))})}function Iv(t={}){let{connector:n=()=>new V,resetOnError:e=!0,resetOnComplete:i=!0,resetOnRefCountZero:a=!0}=t;return o=>{let r,s,l,h=0,p=!1,g=!1,_=()=>{s?.unsubscribe(),s=void 0},b=()=>{_(),r=l=void 0,p=g=!1},y=()=>{let I=r;b(),I?.unsubscribe()};return Le((I,P)=>{h++,!g&&!p&&_();let ie=l=l??n();P.add(()=>{h--,h===0&&!g&&!p&&(s=pp(y,a))}),ie.subscribe(P),!r&&h>0&&(r=new Wn({next:L=>ie.next(L),error:L=>{g=!0,_(),s=pp(b,e,L),ie.error(L)},complete:()=>{p=!0,_(),s=pp(b,i),ie.complete()}}),ut(I).subscribe(r))})(o)}}function pp(t,n,...e){if(n===!0){t();return}if(n===!1)return;let i=new Wn({next:()=>{i.unsubscribe(),t()}});return ut(n(...e)).subscribe(i)}function zc(t,n,e){let i,a=!1;return t&&typeof t==`object`?{bufferSize:i=1/0,windowTime:n=1/0,refCount:a=!1,scheduler:e}=t:i=t??1/0,Iv({connector:()=>new Yn(i,n,e),resetOnError:!0,resetOnComplete:!1,resetOnRefCountZero:a})}function Qa(t){return Qe((n,e)=>t<=e)}function mt(...t){let n=fi(t);return Le((e,i)=>{(n?er(t,e,n):er(t,e)).subscribe(i)})}function _n(t,n){return Le((e,i)=>{let a=null,o=0,r=!1,s=()=>r&&!a&&i.complete();e.subscribe(je(i,l=>{a?.unsubscribe();let h=0,p=o++;ut(t(l,p)).subscribe(a=je(i,g=>i.next(n?n(l,g,p,h++):g),()=>{a=null,s()}))},()=>{r=!0,s()}))})}function Ne(t){return Le((n,e)=>{ut(t).subscribe(je(e,()=>e.complete(),bs)),!e.closed&&n.subscribe(e)})}function hp(t,n=!1){return Le((e,i)=>{let a=0;e.subscribe(je(i,o=>{let r=t(o,a++);(r||n)&&i.next(o),!r&&i.complete()}))})}function fp(t,n,e){let i=Te(t)||n||e?{next:t,error:n,complete:e}:t;return i?Le((a,o)=>{var r;(r=i.subscribe)===null||r===void 0||r.call(i);let s=!0;a.subscribe(je(o,l=>{var h;(h=i.next)===null||h===void 0||h.call(i,l),o.next(l)},()=>{var l;s=!1,(l=i.complete)===null||l===void 0||l.call(i),o.complete()},l=>{var h;s=!1,(h=i.error)===null||h===void 0||h.call(i,l),o.error(l)},()=>{var l,h;s&&((l=i.unsubscribe)===null||l===void 0||l.call(i)),(h=i.finalize)===null||h===void 0||h.call(i)}))}):qn}var Yc=`https://angular.dev/best-practices/security#preventing-cross-site-scripting-xss`;var q=class extends Error{code;constructor(n,e){super(Bi(n,e)),this.code=n}};function zE(t){return`NG0${Math.abs(t)}`}function Bi(t,n){return`${zE(t)}${n?`: `+n:``}`}function Ye(t){for(let n in t)if(t[n]===Ye)return n;throw Error(``)}function Av(t,n){for(let e in n)Object.hasOwn(n,e)&&!Object.hasOwn(t,e)&&(t[e]=n[e])}function Is(t){if(typeof t==`string`)return t;if(Array.isArray(t))return`[${t.map(Is).join(`, `)}]`;if(t==null)return``+t;let n=t.overriddenName||t.name;if(n)return`${n}`;let e=t.toString();if(e==null)return``+e;let i=e.indexOf(`
`);return i>=0?e.slice(0,i):e}function Kc(t,n){return t?n?`${t} ${n}`:t:n||``}var HE=Ye({__forward_ref__:Ye});function Ht(t){return t.__forward_ref__=Ht,t}function Lt(t){return Mp(t)?t():t}function Mp(t){return typeof t==`function`&&Object.hasOwn(t,HE)&&t.__forward_ref__===Ht}function be(t){return{token:t.token,providedIn:t.providedIn||null,factory:t.factory,value:void 0}}function ae(t){return{providers:t.providers||[],imports:t.imports||[]}}function Qc(t){return UE(t,Xc)}function UE(t,n){return Object.hasOwn(t,n)&&t[n]||null}function $E(t){return(t?.[Xc]??null)||null}function bp(t){return t&&Object.hasOwn(t,Uc)?t[Uc]:null}var Xc=Ye({ɵprov:Ye});var Uc=Ye({ɵinj:Ye});var N=class{_desc;ngMetadataName=`InjectionToken`;ɵprov;constructor(n,e){this._desc=n,this.ɵprov=void 0,typeof e==`number`?this.__NG_ELEMENT_ID__=e:e!==void 0&&(this.ɵprov=be({token:this,providedIn:e.providedIn||`root`,factory:e.factory}))}get multi(){return this}toString(){return`InjectionToken ${this._desc}`}};function Tp(t){return t&&!!t.ɵproviders}var ks=Ye({ɵcmp:Ye});var Ms=Ye({ɵdir:Ye});var Np=Ye({ɵpipe:Ye});var Ap=Ye({ɵmod:Ye});var Ss=Ye({ɵfac:Ye});var to=Ye({__NG_ELEMENT_ID__:Ye});var kv=Ye({__NG_ENV_ID__:Ye});function Rv(t){return Zc(t,`@NgModule`),t[Ap]||null}function pa(t){return Zc(t,`@Component`),t[ks]||null}function Ts(t){return Zc(t,`@Directive`),t[Ms]||null}function Ov(t){return Zc(t,`@Pipe`),t[Np]||null}function Zc(t,n){if(t==null)throw new q(-919,!1)}function $e(t){return typeof t==`string`?t:t==null?``:String(t)}var Pv=Ye({ngErrorCode:Ye});var GE=Ye({ngErrorMessage:Ye});var WE=Ye({ngTokenPath:Ye});function Rp(t,n){return Fv(``,-200,n)}function Jc(t,n){throw new q(-201,!1)}function Fv(t,n,e){let i=new q(n,t);return i[Pv]=n,i[GE]=t,e&&(i[WE]=e),i}function qE(t){return t[Pv]}var vp;function Lv(){return vp}function rn(t){let n=vp;return vp=t,n}function Op(t,n,e){let i=Qc(t);if(i&&i.providedIn==`root`)return i.value===void 0?i.value=i.factory():i.value;if(e&8)return null;if(n!==void 0)return n;Jc(t,``)}var bi=globalThis;var Xa={};var KE=`__NG_DI_FLAG__`;var _p=class{injector;constructor(n){this.injector=n}retrieve(n,e){let i=Za(e)||0;try{return this.injector.get(n,i&8?null:Xa,i)}catch(a){if(qo(a))return a;throw a}}};function QE(t,n=0){let e=uc();if(e===void 0)throw new q(-203,!1);if(e===null)return Op(t,void 0,n);{let i=XE(n),a=e.retrieve(t,i);if(qo(a)){if(i.optional)return null;throw a}return a}}function Se(t,n=0){return(Lv()||QE)(Lt(t),n)}function f(t,n){return Se(t,Za(n))}function Za(t){return typeof t>`u`||typeof t==`number`?t:0|(t.optional&&8)|(t.host&&1)|(t.self&&2)|(t.skipSelf&&4)}function XE(t){return{optional:!!(t&8),host:!!(t&1),self:!!(t&2),skipSelf:!!(t&4)}}function yp(t){let n=[];for(let e=0;e<t.length;e++){let i=Lt(t[e]);if(Array.isArray(i)){if(i.length===0)throw new q(900,!1);let a,o=0;for(let r=0;r<i.length;r++){let s=i[r],l=ZE(s);typeof l==`number`?l===-1?a=s.token:o|=l:a=s}n.push(Se(a,o))}else n.push(Se(i))}return n}function ZE(t){return t[KE]}function ma(t,n){return Object.hasOwn(t,Ss)?t[Ss]:null}function Vv(t,n,e){if(t.length!==n.length)return!1;for(let i=0;i<t.length;i++){let a=t[i],o=n[i];if(e&&(a=e(a),o=e(o)),o!==a)return!1}return!0}function Bv(t){return t.flat(Number.POSITIVE_INFINITY)}function ed(t,n){t.forEach(e=>Array.isArray(e)?ed(e,n):n(e))}function Pp(t,n,e){n>=t.length?t.push(e):t.splice(n,0,e)}function Ns(t,n){return n>=t.length-1?t.pop():t.splice(n,1)[0]}function jv(t,n){let e=[];for(let i=0;i<t;i++)e.push(n);return e}function zv(t,n,e,i){let a=t.length;if(a==n)t.push(e,i);else if(a===1)t.push(i,t[0]),t[0]=e;else{for(a--,t.push(t[a-1],t[a]);a>n;){let o=a-2;t[a]=t[o],a--}t[n]=e,t[n+1]=i}}function td(t,n,e){let i=ir(t,n);return i>=0?t[i|1]=e:(i=~i,zv(t,i,n,e)),i}function nd(t,n){let e=ir(t,n);if(e>=0)return t[e|1]}function ir(t,n){return JE(t,n,1)}function JE(t,n,e){let i=0,a=t.length>>e;for(;a!==i;){let o=i+(a-i>>1),r=t[o<<e];if(n===r)return o<<e;r>n?a=o:i=o+1}return~(a<<e)}var ha={};var Kt=[];var no=new N(``);var id=new N(``,-1);var Fp=new N(``);var nr=class{get(n,e=Xa){if(e===Xa){let a=Fv(``,-201);throw a.name=`ɵNotFound`,a}return e}};function sn(t){return{ɵproviders:t}}function Hv(t){return sn([{provide:no,multi:!0,useValue:t}])}function Uv(...t){return{ɵproviders:Lp(!0,t),ɵfromNgModule:!0}}function Lp(t,...n){let e=[],i=new Set,a,o=r=>{e.push(r)};return ed(n,r=>{let s=r;$c(s,o,[],i)&&(a||=[],a.push(s))}),a!==void 0&&$v(a,o),e}function $v(t,n){for(let e=0;e<t.length;e++){let{ngModule:i,providers:a}=t[e];Vp(a,o=>{n(o,i)})}}function $c(t,n,e,i){if(t=Lt(t),!t)return!1;let a=null,o=bp(t),r=!o&&pa(t);if(!o&&!r){let l=t.ngModule;if(o=bp(l),o)a=l;else return!1}else{if(r&&!r.standalone)return!1;a=t}let s=i.has(a);if(r){if(s)return!1;if(i.add(a),r.dependencies){let l=typeof r.dependencies==`function`?r.dependencies():r.dependencies;for(let h of l)$c(h,n,e,i)}}else if(o){if(o.imports!=null&&!s){i.add(a);let h;ed(o.imports,p=>{$c(p,n,e,i)&&(h||=[],h.push(p))}),h!==void 0&&$v(h,n)}if(!s){let h=ma(a)||(()=>new a);n({provide:a,useFactory:h,deps:Kt},a),n({provide:Fp,useValue:a,multi:!0},a),n({provide:no,useValue:()=>Se(a),multi:!0},a)}let l=o.providers;if(l!=null&&!s){let h=t;Vp(l,p=>{n(p,h)})}}else return!1;return a!==t&&t.providers!==void 0}function Vp(t,n){for(let e of t)Tp(e)&&(e=e.ɵproviders),Array.isArray(e)?Vp(e,n):n(e)}var eD=Ye({provide:String,useValue:Ye});function Gv(t){return t!==null&&typeof t==`object`&&eD in t}function tD(t){return!!(t&&t.useExisting)}function nD(t){return!!(t&&t.useFactory)}function Ja(t){return typeof t==`function`}function Wv(t){return!!t.useClass}var As=new N(``);var Hc={};var Mv={};var gp;function ar(){return gp===void 0&&(gp=new nr),gp}var Mt=class{};var eo=class extends Mt{parent;source;scopes;records=new Map;_ngOnDestroyHooks=new Set;_onDestroyHooks=[];get destroyed(){return this._destroyed}_destroyed=!1;injectorDefTypes;constructor(n,e,i,a){super(),this.parent=e,this.source=i,this.scopes=a,Cp(n,r=>this.processProvider(r)),this.records.set(id,tr(void 0,this)),a.has(`environment`)&&this.records.set(Mt,tr(void 0,this));let o=this.records.get(As);o!=null&&typeof o.value==`string`&&this.scopes.add(o.value),this.injectorDefTypes=new Set(this.get(Fp,Kt,{self:!0}))}retrieve(n,e){let i=Za(e)||0;try{return this.get(n,Xa,i)}catch(a){if(qo(a))return a;throw a}}destroy(){Cs(this),this._destroyed=!0;let n=ge(null);try{for(let i of this._ngOnDestroyHooks)i.ngOnDestroy();let e=this._onDestroyHooks;this._onDestroyHooks=[];for(let i of e)i()}finally{this.records.clear(),this._ngOnDestroyHooks.clear(),this.injectorDefTypes.clear(),ge(n)}}onDestroy(n){return Cs(this),this._onDestroyHooks.push(n),()=>this.removeOnDestroy(n)}runInContext(n){Cs(this);let e=pi(this),i=rn(void 0);try{return n()}finally{pi(e),rn(i)}}get(n,e=Xa,i){if(Cs(this),Object.hasOwn(n,kv))return n[kv](this);let a=Za(i),r=pi(this),s=rn(void 0);try{if(!(a&4)){let h=this.records.get(n);if(h===void 0){let p=sD(n)&&Qc(n);p&&this.injectableDefInScope(p)?h=tr(xp(n),Hc):h=null,this.records.set(n,h)}if(h!=null)return this.hydrate(n,h,a)}let l=a&2?ar():this.parent;return e=a&8&&e===Xa?null:e,l.get(n,e)}catch(l){let h=qE(l);throw h===-200||h===-201?new q(h,null):l}finally{rn(s),pi(r)}}resolveInjectorInitializers(){let n=ge(null),e=pi(this),i=rn(void 0);try{let o=this.get(no,Kt,{self:!0});for(let r of o)r()}finally{pi(e),rn(i),ge(n)}}toString(){return`R3Injector[...]`}processProvider(n){n=Lt(n);let e=Ja(n)?n:Lt(n&&n.provide),i=aD(n);if(!Ja(n)&&n.multi===!0){let a=this.records.get(e);a||(a=tr(void 0,Hc,!0),a.factory=()=>yp(a.multi),this.records.set(e,a)),e=n,a.multi.push(n)}this.records.set(e,i)}hydrate(n,e,i){let a=ge(null);try{if(e.value===Mv)throw Rp(``);return e.value===Hc&&(e.value=Mv,e.value=e.factory(void 0,i)),typeof e.value==`object`&&e.value&&rD(e.value)&&this._ngOnDestroyHooks.add(e.value),e.value}finally{ge(a)}}injectableDefInScope(n){if(!n.providedIn)return!1;let e=Lt(n.providedIn);return typeof e==`string`?e===`any`||this.scopes.has(e):this.injectorDefTypes.has(e)}removeOnDestroy(n){let e=this._onDestroyHooks.indexOf(n);e!==-1&&this._onDestroyHooks.splice(e,1)}};function xp(t){let n=Qc(t),e=n!==null?n.factory:ma(t);if(e!==null)return e;if(t instanceof N)throw new q(-204,!1);if(t instanceof Function)return iD(t);throw new q(-204,!1)}function iD(t){if(t.length>0)throw new q(-204,!1);let e=$E(t);return e!==null?()=>e.factory(t):()=>new t}function aD(t){if(Gv(t))return tr(void 0,t.useValue);return tr(Bp(t),Hc)}function Bp(t,n,e){let i;if(Ja(t)){let a=Lt(t);return ma(a)||xp(a)}else if(Gv(t))i=()=>Lt(t.useValue);else if(nD(t))i=()=>t.useFactory(...yp(t.deps||[]));else if(tD(t))i=(a,o)=>Se(Lt(t.useExisting),o!==void 0&&o&8?8:void 0);else{let a=Lt(t&&(t.useClass||t.provide));if(oD(t))i=()=>new a(...yp(t.deps));else return ma(a)||xp(a)}return i}function Cs(t){if(t.destroyed)throw new q(-205,!1)}function tr(t,n,e=!1){return{factory:t,value:n,multi:e?[]:void 0}}function oD(t){return!!t.deps}function rD(t){return t!==null&&typeof t==`object`&&typeof t.ngOnDestroy==`function`}function sD(t){return typeof t==`function`||typeof t==`object`&&t.ngMetadataName===`InjectionToken`}function Cp(t,n){for(let e of t)Array.isArray(e)?Cp(e,n):e&&Tp(e)?Cp(e.ɵproviders,n):n(e)}function or(t,n){let e;t instanceof eo?(Cs(t),e=t):e=new _p(t);let a=pi(e),o=rn(void 0);try{return n()}finally{pi(a),rn(o)}}function qv(){return Lv()!==void 0||uc()!=null}var Kn=0;var ve=1;var xe=2;var Et=3;var Nn=4;var Ut=5;var io=6;var rr=7;var yt=8;var ji=9;var Qn=10;var et=11;var sr=12;var jp=13;var fa=14;var Qt=15;var ga=16;var ao=17;var vi=18;var Rs=19;var zp=20;var Fi=21;var ad=22;var Li=23;var yn=24;var oo=25;var Yv=26;var lr=27;var ot=28;var Kv=1;var Hp=6;var ro=7;var Os=8;var so=9;var xt=10;function zi(t){return Array.isArray(t)&&typeof t[Kv]==`object`}function An(t){return Array.isArray(t)&&t[Kv]===!0}function Up(t){return(t.flags&4)!==0}function _i(t){return t.componentOffset>-1}function cr(t){return(t.flags&1)===1}function Xn(t){return!!t.template}function dr(t){return(t[xe]&512)!==0}function lo(t){return(t[xe]&256)===256}var xn=(function(t){return t[t.NONE=0]=`NONE`,t[t.HTML=1]=`HTML`,t[t.STYLE=2]=`STYLE`,t[t.SCRIPT=3]=`SCRIPT`,t[t.URL=4]=`URL`,t[t.RESOURCE_URL=5]=`RESOURCE_URL`,t[t.ATTRIBUTE_NO_BINDING=6]=`ATTRIBUTE_NO_BINDING`,t})(xn||{});var $p=`svg`;var Qv=`math`;function $t(t){for(;Array.isArray(t);)t=t[Kn];return t}function Gp(t,n){return $t(n[t])}function Cn(t,n){return $t(n[t.index])}function od(t,n){return t.data[n]}function Ps(t,n){return t[n]}function Fs(t,n,e,i){e>=t.data.length&&(t.data[e]=null,t.blueprint[e]=null),n[e]=i}function Rn(t,n){let e=n[t];return zi(e)?e:e[Kn]}function Xv(t){return(t[xe]&4)===4}function rd(t){return(t[xe]&128)===128}function Zv(t){return An(t[Et])}function Sn(t,n){return n==null?null:t[n]}function Wp(t){t[ao]=0}function qp(t){t[xe]&1024||(t[xe]|=1024,rd(t)&&co(t))}function Jv(t,n){for(;t>0;)n=n[fa],t--;return n}function Ls(t){return!!(t[xe]&9216||t[yn]?.dirty)}function sd(t){t[Qn].changeDetectionScheduler?.notify(8),t[xe]&64&&(t[xe]|=1024),Ls(t)&&co(t)}function co(t){t[Qn].changeDetectionScheduler?.notify(0);let n=Vi(t);for(;n!==null&&!(n[xe]&8192||(n[xe]|=8192,!rd(n)));)n=Vi(n)}function ld(t,n){if(lo(t))throw new q(911,!1);t[Fi]===null&&(t[Fi]=[]),t[Fi].push(n)}function e_(t,n){if(t[Fi]===null)return;let e=t[Fi].indexOf(n);e!==-1&&t[Fi].splice(e,1)}function Vi(t){let n=t[Et];return An(n)?n[Et]:n}function Yp(t){return t[rr]??=[]}function Kp(t){return t.cleanup??=[]}function t_(t,n,e,i){let a=Yp(n);a.push(e),t.firstCreatePass&&Kp(t).push(i,a.length-1)}var Ee={lFrame:u_(null),bindingsEnabled:!0,skipHydrationRootTNode:null};var Sp=!1;function n_(){return Ee.lFrame.elementDepthCount}function i_(){Ee.lFrame.elementDepthCount++}function Qp(){Ee.lFrame.elementDepthCount--}function cd(){return Ee.bindingsEnabled}function Xp(){return Ee.skipHydrationRootTNode!==null}function Zp(t){return Ee.skipHydrationRootTNode===t}function Jp(){Ee.skipHydrationRootTNode=null}function ce(){return Ee.lFrame.lView}function Xe(){return Ee.lFrame.tView}function M(t){return Ee.lFrame.contextLView=t,t[yt]}function T(t){return Ee.lFrame.contextLView=null,t}function Tt(){let t=eh();for(;t!==null&&t.type===64;)t=t.parent;return t}function eh(){return Ee.lFrame.currentTNode}function a_(){let t=Ee.lFrame,n=t.currentTNode;return t.isParent?n:n.parent}function uo(t,n){let e=Ee.lFrame;e.currentTNode=t,e.isParent=n}function th(){return Ee.lFrame.isParent}function nh(){Ee.lFrame.isParent=!1}function ih(){return Ee.lFrame.contextLView}function ah(){return Sp}function ws(t){let n=Sp;return Sp=t,n}function dd(){let t=Ee.lFrame,n=t.bindingRootIndex;return n===-1&&(n=t.bindingRootIndex=t.tView.bindingStartIndex),n}function mo(){return Ee.lFrame.bindingIndex}function o_(t){return Ee.lFrame.bindingIndex=t}function ba(){return Ee.lFrame.bindingIndex++}function Hi(t){let n=Ee.lFrame,e=n.bindingIndex;return n.bindingIndex=n.bindingIndex+t,e}function r_(){return Ee.lFrame.inI18n}function s_(t,n){let e=Ee.lFrame;e.bindingIndex=e.bindingRootIndex=t,ud(n)}function l_(){return Ee.lFrame.currentDirectiveIndex}function ud(t){Ee.lFrame.currentDirectiveIndex=t}function c_(t){let n=Ee.lFrame.currentDirectiveIndex;return n===-1?null:t[n]}function md(){return Ee.lFrame.currentQueryIndex}function Vs(t){Ee.lFrame.currentQueryIndex=t}function lD(t){let n=t[ve];return n.type===2?n.declTNode:n.type===1?t[Ut]:null}function oh(t,n,e){if(e&4){let a=n,o=t;for(;a=a.parent,a===null&&!(e&1);)if(a=lD(o),a===null||(o=o[fa],a.type&10))break;if(a===null)return!1;n=a,t=o}let i=Ee.lFrame=d_();return i.currentTNode=n,i.lView=t,!0}function pd(t){let n=d_(),e=t[ve];Ee.lFrame=n,n.currentTNode=e.firstChild,n.lView=t,n.tView=e,n.contextLView=t,n.bindingIndex=e.bindingStartIndex,n.inI18n=!1}function d_(){let t=Ee.lFrame,n=t===null?null:t.child;return n===null?u_(t):n}function u_(t){let n={currentTNode:null,isParent:!0,lView:null,tView:null,selectedIndex:-1,contextLView:null,elementDepthCount:0,currentNamespace:null,currentDirectiveIndex:-1,bindingRootIndex:-1,bindingIndex:-1,currentQueryIndex:0,parent:t,child:null,inI18n:!1};return t!==null&&(t.child=n),n}function m_(){let t=Ee.lFrame;return Ee.lFrame=t.parent,t.currentTNode=null,t.lView=null,t}var rh=m_;function hd(){let t=m_();t.isParent=!0,t.tView=null,t.selectedIndex=-1,t.contextLView=null,t.elementDepthCount=0,t.currentDirectiveIndex=-1,t.currentNamespace=null,t.bindingRootIndex=-1,t.bindingIndex=-1,t.currentQueryIndex=0}function p_(t){return(Ee.lFrame.contextLView=Jv(t,Ee.lFrame.contextLView))[yt]}function Xt(){return Ee.lFrame.selectedIndex}function va(t){Ee.lFrame.selectedIndex=t}function ur(){let t=Ee.lFrame;return od(t.tView,t.selectedIndex)}function Gt(){Ee.lFrame.currentNamespace=$p}function _a(){cD()}function cD(){Ee.lFrame.currentNamespace=null}function sh(){return Ee.lFrame.currentNamespace}var h_=!0;function fd(){return h_}function Bs(t){h_=t}function wp(t,n=null,e=null,i){let a=lh(t,n,e,i);return a.resolveInjectorInitializers(),a}function lh(t,n=null,e=null,i,a=new Set){return new eo([e||Kt,Uv(t)],n||ar(),null,a)}var se=class t{static THROW_IF_NOT_FOUND=Xa;static NULL=new nr;static create(n,e){if(Array.isArray(n))return wp({name:``},e,n,``);{let i=n.name??``;return wp({name:i},n.parent,n.providers,i)}}static ɵprov=be({token:t,providedIn:`any`,factory:()=>Se(id)});static __NG_ELEMENT_ID__=-1};var de=new N(``);var Ae=class{static __NG_ELEMENT_ID__=dD;static __NG_ENV_ID__=n=>n};var Gc=class extends Ae{_lView;constructor(n){super(),this._lView=n}get destroyed(){return lo(this._lView)}onDestroy(n){let e=this._lView;return ld(e,n),()=>e_(e,n)}};function dD(){return new Gc(ce())}var f_=!1;var g_=new N(``);var po=(()=>{class t{taskId=0;pendingTasks=new Set;destroyed=!1;pendingTask=new hi(!1);debugTaskTracker=f(g_,{optional:!0});get hasPendingTasks(){return this.destroyed?!1:this.pendingTask.value}get hasPendingTasksObservable(){return this.destroyed?new Ce(e=>{e.next(!1),e.complete()}):this.pendingTask}add(){!this.hasPendingTasks&&!this.destroyed&&this.pendingTask.next(!0);let e=this.taskId++;return this.pendingTasks.add(e),this.debugTaskTracker?.add(e),e}has(e){return this.pendingTasks.has(e)}remove(e){this.pendingTasks.delete(e),this.debugTaskTracker?.remove(e),this.pendingTasks.size===0&&this.hasPendingTasks&&this.pendingTask.next(!1)}ngOnDestroy(){this.pendingTasks.clear(),this.hasPendingTasks&&this.pendingTask.next(!1),this.destroyed=!0,this.pendingTask.unsubscribe()}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var Ep=class extends V{__isAsync;destroyRef=void 0;pendingTasks=void 0;constructor(n=!1){super(),this.__isAsync=n,qv()&&(this.destroyRef=f(Ae,{optional:!0})??void 0,this.pendingTasks=f(po,{optional:!0})??void 0)}emit(n){let e=ge(null);try{super.next(n)}finally{ge(e)}}subscribe(n,e,i){let a=n,o=e||(()=>null),r=i;if(n&&typeof n==`object`){let l=n;a=l.next?.bind(l),o=l.error?.bind(l),r=l.complete?.bind(l)}this.__isAsync&&(o=this.wrapInTimeout(o),a&&(a=this.wrapInTimeout(a)),r&&(r=this.wrapInTimeout(r)));let s=super.subscribe({next:a,error:o,complete:r});return n instanceof De&&n.add(s),s}wrapInTimeout(n){return e=>{let i=this.pendingTasks?.add();setTimeout(()=>{try{n(e)}finally{i!==void 0&&this.pendingTasks?.remove(i)}})}}};var te=Ep;function Wc(...t){}function ch(t){let n,e;function i(){t=Wc;try{e!==void 0&&typeof cancelAnimationFrame==`function`&&cancelAnimationFrame(e),n!==void 0&&clearTimeout(n)}catch{}}return n=setTimeout(()=>{t(),i()}),typeof requestAnimationFrame==`function`&&(e=requestAnimationFrame(()=>{t(),i()})),()=>i()}function b_(t){return queueMicrotask(()=>t()),()=>{t=Wc}}var dh=`isAngularZone`;var Es=dh+`_ID`;var uD=0;var Z=class t{hasPendingMacrotasks=!1;hasPendingMicrotasks=!1;isStable=!0;onUnstable=new te(!1);onMicrotaskEmpty=new te(!1);onStable=new te(!1);onError=new te(!1);constructor(n){let{enableLongStackTrace:e=!1,shouldCoalesceEventChangeDetection:i=!1,shouldCoalesceRunChangeDetection:a=!1,scheduleInRootZone:o=f_}=n;if(typeof Zone>`u`)throw new q(908,!1);Zone.assertZonePatched();let r=this;r._nesting=0,r._outer=r._inner=Zone.current,Zone.TaskTrackingZoneSpec&&(r._inner=r._inner.fork(new Zone.TaskTrackingZoneSpec)),e&&Zone.longStackTraceZoneSpec&&(r._inner=r._inner.fork(Zone.longStackTraceZoneSpec)),r.shouldCoalesceEventChangeDetection=!a&&i,r.shouldCoalesceRunChangeDetection=a,r.callbackScheduled=!1,r.scheduleInRootZone=o,hD(r)}static isInAngularZone(){return typeof Zone<`u`&&Zone.current.get(dh)===!0}static assertInAngularZone(){if(!t.isInAngularZone())throw new q(909,!1)}static assertNotInAngularZone(){if(t.isInAngularZone())throw new q(909,!1)}run(n,e,i){return this._inner.run(n,e,i)}runTask(n,e,i,a){let o=this._inner,r=o.scheduleEventTask(`NgZoneEvent: `+a,n,mD,Wc,Wc);try{return o.runTask(r,e,i)}finally{o.cancelTask(r)}}runGuarded(n,e,i){return this._inner.runGuarded(n,e,i)}runOutsideAngular(n){return this._outer.run(n)}};var mD={};function uh(t){if(t._nesting==0&&!t.hasPendingMicrotasks&&!t.isStable)try{t._nesting++,t.onMicrotaskEmpty.emit(null)}finally{if(t._nesting--,!t.hasPendingMicrotasks)try{t.runOutsideAngular(()=>t.onStable.emit(null))}finally{t.isStable=!0}}}function pD(t){if(t.isCheckStableRunning||t.callbackScheduled)return;t.callbackScheduled=!0;function n(){ch(()=>{t.callbackScheduled=!1,Dp(t),t.isCheckStableRunning=!0,uh(t),t.isCheckStableRunning=!1})}t.scheduleInRootZone?Zone.root.run(()=>{n()}):t._outer.run(()=>{n()}),Dp(t)}function hD(t){let n=()=>{pD(t)},e=uD++;t._inner=t._inner.fork({name:`angular`,properties:{[dh]:!0,[Es]:e,[Es+e]:!0},onInvokeTask:(i,a,o,r,s,l)=>{if(fD(l))return i.invokeTask(o,r,s,l);try{return Tv(t),i.invokeTask(o,r,s,l)}finally{(t.shouldCoalesceEventChangeDetection&&r.type===`eventTask`||t.shouldCoalesceRunChangeDetection)&&n(),Nv(t)}},onInvoke:(i,a,o,r,s,l,h)=>{try{return Tv(t),i.invoke(o,r,s,l,h)}finally{t.shouldCoalesceRunChangeDetection&&!t.callbackScheduled&&!gD(l)&&n(),Nv(t)}},onHasTask:(i,a,o,r)=>{i.hasTask(o,r),a===o&&(r.change==`microTask`?(t._hasPendingMicrotasks=r.microTask,Dp(t),uh(t)):r.change==`macroTask`&&(t.hasPendingMacrotasks=r.macroTask))},onHandleError:(i,a,o,r)=>(i.handleError(o,r),t.runOutsideAngular(()=>t.onError.emit(r)),!1)})}function Dp(t){t._hasPendingMicrotasks||(t.shouldCoalesceEventChangeDetection||t.shouldCoalesceRunChangeDetection)&&t.callbackScheduled===!0?t.hasPendingMicrotasks=!0:t.hasPendingMicrotasks=!1}function Tv(t){t._nesting++,t.isStable&&(t.isStable=!1,t.onUnstable.emit(null))}function Nv(t){t._nesting--,uh(t)}var Ds=class{hasPendingMicrotasks=!1;hasPendingMacrotasks=!1;isStable=!0;onUnstable=new te;onMicrotaskEmpty=new te;onStable=new te;onError=new te;run(n,e,i){return n.apply(e,i)}runGuarded(n,e,i){return n.apply(e,i)}runOutsideAngular(n){return n()}runTask(n,e,i,a){return n.apply(e,i)}};function fD(t){return v_(t,`__ignore_ng_zone__`)}function gD(t){return v_(t,`__scheduler_tick__`)}function v_(t,n){return!Array.isArray(t)||t.length!==1?!1:t[0]?.data?.[n]===!0}var Tn=class{_console=console;handleError(n){this._console.error(`ERROR`,n)}};function __(t){return y_(t)?t:new Ip(t)}function y_(t){return t instanceof Error||typeof t==`object`&&t!==null&&typeof t.name==`string`&&typeof t.message==`string`}var Ip=class extends Error{constructor(n){super(String(n),{cause:n}),this.name=`ErrorBoundaryWrappedError`}};var Ui=new N(``,{factory:()=>{let t=f(Z),n=f(Mt),e;return i=>{t.runOutsideAngular(()=>{n.destroyed&&!e?setTimeout(()=>{throw i}):(e??=n.get(Tn),e.handleError(i))})}}});var x_={provide:no,useValue:()=>{f(Tn,{optional:!0})},multi:!0};var bD=new N(``,{factory:()=>{let t=f(de).defaultView;if(!t)return;let n=f(Ui),e=o=>{n(o.reason),o.preventDefault()},i=o=>{o.error?n(o.error):n(new Error(o.message,{cause:o})),o.preventDefault()},a=()=>{t.addEventListener(`unhandledrejection`,e),t.addEventListener(`error`,i)};typeof Zone<`u`?Zone.root.run(a):a(),f(Ae).onDestroy(()=>{t.removeEventListener(`error`,i),t.removeEventListener(`unhandledrejection`,e)})}});function mh(){return sn([Hv(()=>{f(bD)})])}function k(t,n){let[e,i,a]=Xm(t,n?.equal),o=e;o[St];return o.set=i,o.update=a,o.asReadonly=gd.bind(o),o}function gd(){let t=this[St];if(t.readonlyFn===void 0){let n=()=>this();n[St]=t,t.readonlyFn=n}return t.readonlyFn}var $i=new N(``,{factory:()=>vD});var vD=`ng`;var bd=new N(``);var Gi=new N(``,{providedIn:`platform`,factory:()=>`unknown`});var js=new N(``);var ya=new N(``,{factory:()=>f(de).body?.querySelector(`[ngCspNonce]`)?.getAttribute(`ngCspNonce`)||null});var mr=(()=>{class t{view;node;constructor(e,i){this.view=e,this.node=i}static __NG_ELEMENT_ID__=_D}return t})();function _D(){return new mr(ce(),Tt())}var gi=class{};var zs=new N(``,{factory:()=>!0});var ph=new N(``);var vd=(()=>{class t{static ɵprov=be({token:t,providedIn:`root`,factory:()=>new kp})}return t})();var kp=class{dirtyEffectCount=0;queues=new Map;add(n){this.enqueue(n),this.schedule(n)}schedule(n){n.dirty&&this.dirtyEffectCount++}remove(n){let e=n.zone,i=this.queues.get(e);i.has(n)&&(i.delete(n),n.dirty&&this.dirtyEffectCount--)}enqueue(n){let e=n.zone;this.queues.has(e)||this.queues.set(e,new Set);let i=this.queues.get(e);i.has(n)||i.add(n)}flush(){for(;this.dirtyEffectCount>0;){let n=!1;for(let[e,i]of this.queues)e===null?n||=this.flushQueue(i):n||=e.run(()=>this.flushQueue(i));n||(this.dirtyEffectCount=0)}}flushQueue(n){let e=!1;for(let i of n)i.dirty&&(this.dirtyEffectCount--,e=!0,i.run());return e}};var qc=class{[St];constructor(n){this[St]=n}destroy(){this[St].destroy()}};function vt(t,n){let e=n?.injector??f(se),i=n?.manualCleanup!==!0?e.get(Ae):null,a,o=e.get(mr,null,{optional:!0}),r=e.get(gi);return o!==null?(a=S_(o.view,r,t),i instanceof Gc&&i._lView===o.view&&(i=null)):a=CD(t,e.get(vd),r),a.injector=e,i!==null&&(a.onDestroyFns=[i.onDestroy(()=>a.destroy())]),new qc(a)}var C_=s(r({},Jm),{cleanupFns:void 0,zone:null,onDestroyFns:null,run(){let t=ws(!1);try{ep(this)}finally{ws(t)}},cleanup(){if(!this.cleanupFns?.length)return;let t=ge(null);try{for(;this.cleanupFns.length;)this.cleanupFns.pop()()}finally{this.cleanupFns=[],ge(t)}}});var yD=s(r({},C_),{consumerMarkedDirty(){this.scheduler.schedule(this),this.notifier.notify(12)},destroy(){if(da(this),this.onDestroyFns!==null)for(let t of this.onDestroyFns)t();this.cleanup(),this.scheduler.remove(this)}});var xD=s(r({},C_),{consumerMarkedDirty(){this.view[xe]|=8192,co(this.view),this.notifier.notify(13)},destroy(){if(da(this),this.onDestroyFns!==null)for(let t of this.onDestroyFns)t();this.cleanup(),this.view[Li]?.delete(this)}});function S_(t,n,e){let i=Object.create(xD);return i.view=t,i.zone=typeof Zone<`u`?Zone.current:null,i.notifier=n,i.userFn=e,i.fn=w_(i,e),t[Li]??=new Set,t[Li].add(i),i.consumerMarkedDirty(i),i}function CD(t,n,e){let i=Object.create(yD);return i.userFn=t,i.fn=w_(i,t),i.scheduler=n,i.notifier=e,i.zone=typeof Zone<`u`?Zone.current:null,i.scheduler.add(i),i.notifier.notify(12),i}function w_(t,n){return()=>{n(e=>(t.cleanupFns??=[]).push(e))}}function ln(t){return typeof t==`function`&&t[St]!==void 0}var xa=(()=>{class t{internalPendingTasks=f(po);scheduler=f(gi);errorHandler=f(Ui);add(){let e=this.internalPendingTasks.add();return()=>{this.internalPendingTasks.has(e)&&(this.scheduler.notify(11),this.internalPendingTasks.remove(e))}}run(e){let i=this.add();try{e().catch(this.errorHandler).finally(i)}catch(a){this.errorHandler(a),i()}}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var Sr=Symbol(`InputSignalNode#UNSET`);var cf=s(r({},Wo),{transformFn:void 0,applyValueToInputSignal(t,n){ua(t,n)}});function Zs(t){return{toString:t}.toString()}var ze=(function(t){return t[t.TemplateCreateStart=0]=`TemplateCreateStart`,t[t.TemplateCreateEnd=1]=`TemplateCreateEnd`,t[t.TemplateUpdateStart=2]=`TemplateUpdateStart`,t[t.TemplateUpdateEnd=3]=`TemplateUpdateEnd`,t[t.LifecycleHookStart=4]=`LifecycleHookStart`,t[t.LifecycleHookEnd=5]=`LifecycleHookEnd`,t[t.OutputStart=6]=`OutputStart`,t[t.OutputEnd=7]=`OutputEnd`,t[t.BootstrapApplicationStart=8]=`BootstrapApplicationStart`,t[t.BootstrapApplicationEnd=9]=`BootstrapApplicationEnd`,t[t.BootstrapComponentStart=10]=`BootstrapComponentStart`,t[t.BootstrapComponentEnd=11]=`BootstrapComponentEnd`,t[t.ChangeDetectionStart=12]=`ChangeDetectionStart`,t[t.ChangeDetectionEnd=13]=`ChangeDetectionEnd`,t[t.ChangeDetectionSyncStart=14]=`ChangeDetectionSyncStart`,t[t.ChangeDetectionSyncEnd=15]=`ChangeDetectionSyncEnd`,t[t.AfterRenderHooksStart=16]=`AfterRenderHooksStart`,t[t.AfterRenderHooksEnd=17]=`AfterRenderHooksEnd`,t[t.ComponentStart=18]=`ComponentStart`,t[t.ComponentEnd=19]=`ComponentEnd`,t[t.DeferBlockStateStart=20]=`DeferBlockStateStart`,t[t.DeferBlockStateEnd=21]=`DeferBlockStateEnd`,t[t.DynamicComponentStart=22]=`DynamicComponentStart`,t[t.DynamicComponentEnd=23]=`DynamicComponentEnd`,t[t.HostBindingsUpdateStart=24]=`HostBindingsUpdateStart`,t[t.HostBindingsUpdateEnd=25]=`HostBindingsUpdateEnd`,t})(ze||{});var Dd=class{previousValue;currentValue;firstChange;constructor(n,e,i){this.previousValue=n,this.currentValue=e,this.firstChange=i}isFirstChange(){return this.firstChange}};function my(t,n,e,i){n!==null?n.applyValueToInputSignal(n,i):t[e]=i}var py=null;var _t=(()=>{py=E_;let t=()=>E_;return t.ngInherit=!0,t})();function RD(){return py}function E_(t){return t.type.prototype.ngOnChanges&&(t.setInput=PD),OD}function OD(){let t=hy(this),n=t?.current;if(n){let e=t.previous;if(e===ha)t.previous=n;else for(let i in n)e[i]=n[i];t.current=null,this.ngOnChanges(n)}}function PD(t,n,e,i,a){let o=this.declaredInputs[i],r=hy(t)||FD(t,{previous:ha,current:null}),s=r.current||(r.current={}),l=r.previous,h=l[o];s[o]=new Dd(h&&h.currentValue,e,l===ha),my(t,n,a,e)}var Eh=`__ngSimpleChanges__`;function hy(t){return Object.hasOwn(t,Eh)&&t[Eh]||null}function FD(t,n){return t[Eh]=n}var D_=[];var Ke=function(t,n=null,e){for(let i=0;i<D_.length;i++){let a=D_[i];a(t,n,e)}};function LD(t,n,e){let{ngOnChanges:i,ngOnInit:a,ngDoCheck:o}=n.type.prototype;if(i){let r=RD()(n);(e.preOrderHooks??=[]).push(t,r),(e.preOrderCheckHooks??=[]).push(t,r)}a&&(e.preOrderHooks??=[]).push(0-t,a),o&&((e.preOrderHooks??=[]).push(t,o),(e.preOrderCheckHooks??=[]).push(t,o))}function fy(t,n){for(let e=n.directiveStart,i=n.directiveEnd;e<i;e++){let{ngAfterContentInit:r,ngAfterContentChecked:s,ngAfterViewInit:l,ngAfterViewChecked:h,ngOnDestroy:p}=t.data[e].type.prototype;r&&(t.contentHooks??=[]).push(-e,r),s&&((t.contentHooks??=[]).push(e,s),(t.contentCheckHooks??=[]).push(e,s)),l&&(t.viewHooks??=[]).push(-e,l),h&&((t.viewHooks??=[]).push(e,h),(t.viewCheckHooks??=[]).push(e,h)),p!=null&&(t.destroyHooks??=[]).push(e,p)}}function Cd(t,n,e){gy(t,n,3,e)}function Sd(t,n,e,i){(t[xe]&3)===e&&gy(t,n,e,i)}function hh(t,n){let e=t[xe];(e&3)===n&&(e&=16383,e+=1,t[xe]=e)}function gy(t,n,e,i){let a=i!==void 0?t[ao]&65535:0,o=i??-1,r=n.length-1,s=0;for(let l=a;l<r;l++)if(typeof n[l+1]==`number`){if(s=n[l],i!=null&&s>=i)break}else n[l]<0&&(t[ao]+=65536),(s<o||o==-1)&&(VD(t,e,n,l),t[ao]=(t[ao]&4294901760)+l+2),l++}function I_(t,n){Ke(ze.LifecycleHookStart,t,n);let e=ge(null);try{n.call(t)}finally{ge(e),Ke(ze.LifecycleHookEnd,t,n)}}function VD(t,n,e,i){let a=e[i]<0,o=e[i+1],s=t[a?-e[i]:e[i]];a?t[xe]>>14<t[ao]>>16&&(t[xe]&3)===n&&(t[xe]+=16384,I_(s,o)):I_(s,o)}var fr=-1;var go=class{factory;name;injectImpl;resolving=!1;canSeeViewProviders;multi;componentProviders;index;providerFactory;constructor(n,e,i,a){this.factory=n,this.name=a,this.canSeeViewProviders=e,this.injectImpl=i}};function BD(t){return(t.flags&8)!==0}function jD(t){return(t.flags&16)!==0}function zD(t,n,e){let i=0;for(;i<e.length;){let a=e[i];if(typeof a==`number`){if(a!==0)break;i++;let o=e[i++],r=e[i++],s=e[i++];t.setAttribute(n,r,s,o)}else{let o=a,r=e[++i];HD(o)?t.setProperty(n,o,r):t.setAttribute(n,o,r),i++}}return i}function by(t){return t===3||t===4||t===6}function HD(t){return t.charCodeAt(0)===64}function gr(t,n){if(!(n===null||n.length===0))if(t===null||t.length===0)t=n.slice();else{let e=-1;for(let i=0;i<n.length;i++){let a=n[i];typeof a==`number`?e=a:e===0||(e===-1||e===2?k_(t,e,a,null,n[++i]):k_(t,e,a,null,null))}}return t}function k_(t,n,e,i,a){let o=0,r=t.length;if(n===-1)r=-1;else for(;o<t.length;){let s=t[o++];if(typeof s==`number`){if(s===n){r=-1;break}else if(s>n){r=o-1;break}}}for(;o<t.length;){let s=t[o];if(typeof s==`number`)break;if(s===e){a!==null&&(t[o+1]=a);return}o++,a!==null&&o++}r!==-1&&(t.splice(r,0,n),o=r+1),t.splice(o++,0,e),a!==null&&t.splice(o++,0,a)}function vy(t){return t!==fr}function Id(t){return t&32767}function UD(t){return t>>16}function kd(t,n){let e=UD(t),i=n;for(;e>0;)i=i[fa],e--;return i}var Dh=!0;function Md(t){let n=Dh;return Dh=t,n}var _y=255;var yy=5;var GD=0;var yi={};function WD(t,n,e){let i;typeof e==`string`?i=e.charCodeAt(0)||0:Object.hasOwn(e,to)&&(i=e[to]),i??=e[to]=GD++;let a=i&_y,o=1<<a;n.data[t+(a>>yy)]|=o}function Td(t,n){let e=xy(t,n);if(e!==-1)return e;let i=n[ve];i.firstCreatePass&&(t.injectorIndex=n.length,fh(i.data,t),fh(n,null),fh(i.blueprint,null));let a=df(t,n),o=t.injectorIndex;if(vy(a)){let r=Id(a),s=kd(a,n),l=s[ve].data;for(let h=0;h<8;h++)n[o+h]=s[r+h]|l[r+h]}return n[o+8]=a,o}function fh(t,n){t.push(0,0,0,0,0,0,0,0,n)}function xy(t,n){return t.injectorIndex===-1||t.parent&&t.parent.injectorIndex===t.injectorIndex||n[t.injectorIndex+8]===null?-1:t.injectorIndex}function df(t,n){if(t.parent&&t.parent.injectorIndex!==-1)return t.parent.injectorIndex;let e=0,i=null,a=n;for(;a!==null;){if(i=Dy(a),i===null)return fr;if(e++,a=a[fa],i.injectorIndex!==-1)return i.injectorIndex|e<<16}return fr}function Ih(t,n,e){WD(t,n,e)}function qD(t,n){if(n===`class`)return t.classes;if(n===`style`)return t.styles;let e=t.attrs;if(e){let i=e.length,a=0;for(;a<i;){let o=e[a];if(by(o))break;if(o===0)a=a+2;else if(typeof o==`number`)for(a++;a<i&&typeof e[a]==`string`;)a++;else{if(o===n)return e[a+1];a=a+2}}}return null}function Cy(t,n,e){if(e&8||t!==void 0)return t;Jc(n,`NodeInjector`)}function Sy(t,n,e,i){if(e&8&&i===void 0&&(i=null),(e&3)===0){let a=t[ji],o=rn(void 0);try{return a?a.get(n,i,e&8):Op(n,i,e&8)}finally{rn(o)}}return Cy(i,n,e)}function wy(t,n,e,i=0,a){if(t!==null){if(n[xe]&2048&&!(i&2)){let r=XD(t,n,e,i,yi);if(r!==yi)return r}let o=Ey(t,n,e,i,yi);if(o!==yi)return o}return Sy(n,e,i,a)}function Ey(t,n,e,i,a){let o=KD(e);if(typeof o==`function`){if(!oh(n,t,i))return i&1?Cy(a,e,i):Sy(n,e,i,a);try{let r;if(r=o(i),r==null&&!(i&8))Jc(e);else return r}finally{rh()}}else if(typeof o==`number`){let r=null,s=xy(t,n),l=fr,h=i&1?n[Qt][Ut]:null;for((s===-1||i&4)&&(l=s===-1?df(t,n):n[s+8],l===fr||!T_(i,!1)?s=-1:(r=n[ve],s=Id(l),n=kd(l,n)));s!==-1;){let p=n[ve];if(M_(o,s,p.data)){let g=YD(s,n,e,r,i,h);if(g!==yi)return g}l=n[s+8],l!==fr&&T_(i,n[ve].data[s+8]===h)&&M_(o,s,n)?(r=p,s=Id(l),n=kd(l,n)):s=-1}}return a}function YD(t,n,e,i,a,o){let r=n[ve],s=r.data[t+8],p=wd(s,r,e,i==null?_i(s)&&Dh:i!=r&&(s.type&3)!==0,a&1&&o===s);return p!==null?Gs(n,r,p,s,a):yi}function wd(t,n,e,i,a){let o=t.providerIndexes,r=n.data,s=o&1048575,l=t.directiveStart,h=t.directiveEnd,p=o>>20,g=i?s:s+p,_=a?s+p:h;for(let b=g;b<_;b++){let y=r[b];if(b<l&&e===y||b>=l&&y.type===e)return b}if(a){let b=r[l];if(b&&Xn(b)&&b.type===e)return l}return null}function Gs(t,n,e,i,a){let o=t[e],r=n.data;if(o instanceof go){let s=o;if(s.resolving)throw Rp(``);let l=Md(s.canSeeViewProviders);s.resolving=!0;r[e].type||r[e];let g=s.injectImpl?rn(s.injectImpl):null;oh(t,i,0);try{o=t[e]=s.factory(void 0,a,r,t,i),n.firstCreatePass&&e>=i.directiveStart&&LD(e,r[e],n)}finally{g!==null&&rn(g),Md(l),s.resolving=!1,rh()}}return o}function KD(t){if(typeof t==`string`)return t.charCodeAt(0)||0;let n=Object.hasOwn(t,to)?t[to]:void 0;return typeof n==`number`?n>=0?n&_y:QD:n}function M_(t,n,e){let i=1<<t;return!!(e[n+(t>>yy)]&i)}function T_(t,n){return!(t&2)&&!(t&1&&n)}var Wi=class{_tNode;_lView;constructor(n,e){this._tNode=n,this._lView=e}get(n,e,i){return wy(this._tNode,this._lView,n,Za(i),e)}};function QD(){return new Wi(Tt(),ce())}function At(t){return Zs(()=>{let n=t.prototype.constructor,e=n[Ss]||kh(n),i=Object.prototype,a=Object.getPrototypeOf(t.prototype).constructor;for(;a&&a!==i;){let o=a[Ss]||kh(a);if(o&&o!==e)return o;a=Object.getPrototypeOf(a)}return o=>new o})}function kh(t){return Mp(t)?()=>{let n=kh(Lt(t));return n&&n()}:ma(t)}function XD(t,n,e,i,a){let o=t,r=n;for(;o!==null&&r!==null&&r[xe]&2048&&!dr(r);){let s=Ey(o,r,e,i|2,yi);if(s!==yi)return s;i&=-5;let l=o.parent;if(!l){let h=r[zp];if(h){let p=h.get(e,yi,i);if(p!==yi)return p}l=Dy(r),r=r[fa]}o=l}return a}function Dy(t){let n=t[ve],e=n.type;return e===2?n.declTNode:e===1?t[Ut]:null}function uf(t){return qD(Tt(),t)}function le(t){return{token:t.token,providedIn:t.autoProvided===!1?null:`root`,factory:t.factory,value:void 0}}function ZD(){return wr(Tt(),ce())}function wr(t,n){return new Y(Cn(t,n))}var Y=(()=>{class t{nativeElement;constructor(e){this.nativeElement=e}static __NG_ELEMENT_ID__=ZD}return t})();function Iy(t){return t instanceof Y?t.nativeElement:t}function JD(){return this._results[Symbol.iterator]()}var wn=class{_emitDistinctChangesOnly;dirty=!0;_onDirty=void 0;_results=[];_changesDetected=!1;_changes=void 0;length=0;first=void 0;last=void 0;get changes(){return this._changes??=new V}constructor(n=!1){this._emitDistinctChangesOnly=n}get(n){return this._results[n]}map(n){return this._results.map(n)}filter(n){return this._results.filter(n)}find(n){return this._results.find(n)}reduce(n,e){return this._results.reduce(n,e)}forEach(n){this._results.forEach(n)}some(n){return this._results.some(n)}toArray(){return this._results.slice()}toString(){return this._results.toString()}reset(n,e){this.dirty=!1;let i=Bv(n);(this._changesDetected=!Vv(this._results,i,e))&&(this._results=i,this.length=i.length,this.last=i[this.length-1],this.first=i[0])}notifyOnChanges(){this._changes!==void 0&&(this._changesDetected||!this._emitDistinctChangesOnly)&&this._changes.next(this)}onDirty(n){this._onDirty=n}setDirty(){this.dirty=!0,this._onDirty?.()}destroy(){this._changes!==void 0&&(this._changes.complete(),this._changes.unsubscribe())}[Symbol.iterator]=JD};function ky(t){return(t.flags&128)===128}var mf=(function(t){return t[t.OnPush=0]=`OnPush`,t[t.Eager=1]=`Eager`,t[t.Default=1]=`Default`,t})(mf||{});var My=new Map;var eI=0;function tI(){return eI++}function nI(t){My.set(t[Rs],t)}function Mh(t){My.delete(t[Rs])}var N_=`__ngContext__`;function br(t,n){zi(n)?(t[N_]=n[Rs],nI(n)):t[N_]=n}function Ty(t){return Ay(t[sr])}function Ny(t){return Ay(t[Nn])}function Ay(t){for(;t!==null&&!An(t);)t=t[Nn];return t}var Th;function pf(t){Th=t}function Ry(){if(Th!==void 0)return Th;if(typeof document<`u`)return document;throw new q(210,!1)}var Oy=`r`;var Py=`di`;var Fy=!1;var Ly=new N(``,{factory:()=>Fy});var A_=new WeakMap;function iI(t,n){if(t==null||typeof t!=`object`)return;let e=A_.get(t);e||(e=new WeakSet,A_.set(t,e)),e.add(n)}function Hd(t){return(t.flags&32)===32}var rI=()=>null;function Vy(t,n,e=!1){return rI(t,n,e)}function By(t,n){let e=t.contentQueries;if(e!==null){let i=ge(null);try{for(let a=0;a<e.length;a+=2){let o=e[a],r=e[a+1];if(r!==-1){let s=t.data[r];Vs(o),s.contentQueries(2,n[r],r)}}}finally{ge(i)}}}function Nh(t,n,e){Vs(0);let i=ge(null);try{n(t,e)}finally{ge(i)}}function hf(t,n,e){if(Up(n)){let i=ge(null);try{let a=n.directiveStart,o=n.directiveEnd;for(let r=a;r<o;r++){let s=t.data[r];if(s.contentQueries){let l=e[r];s.contentQueries(1,l,r)}}}finally{ge(i)}}}var ei=(function(t){return t[t.Emulated=0]=`Emulated`,t[t.None=2]=`None`,t[t.ShadowDom=3]=`ShadowDom`,t[t.ExperimentalIsolatedShadowDom=4]=`ExperimentalIsolatedShadowDom`,t})(ei||{});var _d;function sI(){if(_d===void 0&&(_d=null,bi.trustedTypes))try{_d=bi.trustedTypes.createPolicy(`angular`,{createHTML:t=>t,createScript:t=>t,createScriptURL:t=>t})}catch{}return _d}function Ud(t){return sI()?.createHTML(t)||t}var qi=class{changingThisBreaksApplicationSecurity;constructor(n){this.changingThisBreaksApplicationSecurity=n}toString(){return`SafeValue must use [property]=binding: ${this.changingThisBreaksApplicationSecurity} (see ${Yc})`}};var Ah=class extends qi{getTypeName(){return`HTML`}};var Rh=class extends qi{getTypeName(){return`Style`}};var Oh=class extends qi{getTypeName(){return`Script`}};var Ph=class extends qi{getTypeName(){return`URL`}};var Fh=class extends qi{getTypeName(){return`ResourceURL`}};function ti(t){return t instanceof qi?t.changingThisBreaksApplicationSecurity:t}function Sa(t,n){let e=jy(t);if(e!=null&&e!==n){if(e===`ResourceURL`&&n===`URL`)return!0;throw new Error(`Required a safe ${n}, got a ${e} (see ${Yc})`)}return e===n}function jy(t){return t instanceof qi&&t.getTypeName()||null}function ff(t){return new Ah(t)}function gf(t){return new Rh(t)}function bf(t){return new Oh(t)}function vf(t){return new Ph(t)}function _f(t){return new Fh(t)}function lI(t){let n=new Vh(t);return cI()?new Lh(n):n}var Lh=class{inertDocumentHelper;constructor(n){this.inertDocumentHelper=n}getInertBodyElement(n){n=`<body><remove></remove>`+n;try{let e=new window.DOMParser().parseFromString(Ud(n),`text/html`).body;return e===null?this.inertDocumentHelper.getInertBodyElement(n):(e.firstChild?.remove(),e)}catch{return null}}};var Vh=class{defaultDoc;inertDocument;constructor(n){this.defaultDoc=n,this.inertDocument=this.defaultDoc.implementation.createHTMLDocument(`sanitization-inert`)}getInertBodyElement(n){let e=this.inertDocument.createElement(`template`);return e.innerHTML=Ud(n),e}};function cI(){try{return!!new window.DOMParser().parseFromString(Ud(``),`text/html`)}catch{return!1}}var dI=/^(?!javascript:)(?:[a-z0-9+.-]+:|[^&:\/?#]*(?:[\/?#]|$))/i;function Js(t){return t=String(t),t.match(dI)?t:`unsafe:`+t}function Yi(t){let n=Object.create(null);for(let e of t.split(`,`))n[e]=!0;return n}function el(...t){let n=Object.create(null);for(let e of t)for(let i in e)Object.hasOwn(e,i)&&(n[i]=!0);return n}var zy=Yi(`area,br,col,hr,img,wbr`);var Hy=Yi(`colgroup,dd,dt,li,p,tbody,td,tfoot,th,thead,tr`);var Uy=Yi(`rp,rt`);var uI=el(Uy,Hy);var R_=el(zy,el(Hy,Yi(`address,article,aside,blockquote,caption,center,del,details,dialog,dir,div,dl,figure,figcaption,footer,h1,h2,h3,h4,h5,h6,header,hgroup,hr,ins,main,map,menu,nav,ol,pre,section,summary,table,ul`)),el(Uy,Yi(`a,abbr,acronym,audio,b,bdi,bdo,big,br,cite,code,del,dfn,em,font,i,img,ins,kbd,label,map,mark,picture,q,ruby,rp,rt,s,samp,small,source,span,strike,strong,sub,sup,time,track,tt,u,var,video`)),uI);var $y=Yi(`background,cite,href,itemtype,longdesc,poster,src,xlink:href`);var gI=el($y,Yi(`abbr,accesskey,align,alt,autoplay,axis,bgcolor,border,cellpadding,cellspacing,class,clear,color,cols,colspan,compact,controls,coords,datetime,default,dir,download,face,headers,height,hidden,hreflang,hspace,ismap,itemscope,itemprop,kind,label,lang,language,loop,media,muted,nohref,nowrap,open,preload,rel,rev,role,rows,rowspan,rules,scope,scrolling,shape,size,sizes,span,srclang,srcset,start,summary,tabindex,target,title,translate,type,usemap,valign,value,vspace,width`),Yi(`aria-activedescendant,aria-atomic,aria-autocomplete,aria-busy,aria-checked,aria-colcount,aria-colindex,aria-colspan,aria-controls,aria-current,aria-describedby,aria-details,aria-disabled,aria-dropeffect,aria-errormessage,aria-expanded,aria-flowto,aria-grabbed,aria-haspopup,aria-hidden,aria-invalid,aria-keyshortcuts,aria-label,aria-labelledby,aria-level,aria-live,aria-modal,aria-multiline,aria-multiselectable,aria-orientation,aria-owns,aria-placeholder,aria-posinset,aria-pressed,aria-readonly,aria-relevant,aria-required,aria-roledescription,aria-rowcount,aria-rowindex,aria-rowspan,aria-selected,aria-setsize,aria-sort,aria-valuemax,aria-valuemin,aria-valuenow,aria-valuetext`));var bI=Yi(`script,style,template`);var Bh=class{sanitizedSomething=!1;buf=[];sanitizeChildren(n){let e=n.firstChild,i=!0,a=[];for(;e;){if(e.nodeType===Node.ELEMENT_NODE?i=this.startElement(e):e.nodeType===Node.TEXT_NODE?this.chars(e.nodeValue):this.sanitizedSomething=!0,i&&e.firstChild){a.push(e),e=yI(e);continue}for(;e;){e.nodeType===Node.ELEMENT_NODE&&this.endElement(e);let o=_I(e);if(o){e=o;break}e=a.pop()}}return this.buf.join(``)}startElement(n){let e=O_(n).toLowerCase();if(!Object.hasOwn(R_,e))return this.sanitizedSomething=!0,!Object.hasOwn(bI,e);this.buf.push(`<`),this.buf.push(e);let i=n.attributes;for(let a=0;a<i.length;a++){let o=i.item(a),r=o.name,s=r.toLowerCase();if(!Object.hasOwn(gI,s)){this.sanitizedSomething=!0;continue}let l=o.value;$y[s]&&(l=Js(l)),this.buf.push(` `,r,`="`,P_(l),`"`)}return this.buf.push(`>`),!0}endElement(n){let e=O_(n).toLowerCase();Object.hasOwn(R_,e)&&!Object.hasOwn(zy,e)&&(this.buf.push(`</`),this.buf.push(e),this.buf.push(`>`))}chars(n){this.buf.push(P_(n))}};function vI(t,n){return(t.compareDocumentPosition(n)&Node.DOCUMENT_POSITION_CONTAINED_BY)!==Node.DOCUMENT_POSITION_CONTAINED_BY}function _I(t){let n=t.nextSibling;if(n&&t!==n.previousSibling)throw Gy(n);return n}function yI(t){let n=t.firstChild;if(n&&vI(t,n))throw Gy(n);return n}function O_(t){let n=t.nodeName;return typeof n==`string`?n:`FORM`}function Gy(t){return new Error(`Failed to sanitize html because the element is clobbered: ${t.outerHTML}`)}var xI=/[\uD800-\uDBFF][\uDC00-\uDFFF]/g;var CI=/([^\#-~ |!])/g;function P_(t){return t.replace(/&/g,`&amp;`).replace(xI,function(n){let e=n.charCodeAt(0),i=n.charCodeAt(1);return`&#`+((e-55296)*1024+(i-56320)+65536)+`;`}).replace(CI,function(n){return`&#`+n.charCodeAt(0)+`;`}).replace(/</g,`&lt;`).replace(/>/g,`&gt;`)}var yd;function yf(t,n){let e=null;try{yd=yd||lI(t);let i=n?String(n):``;e=yd.getInertBodyElement(i);let a=5,o=i;do{if(a===0)throw new Error(`Failed to sanitize html because the input is unstable`);a--,i=o,o=e.innerHTML,e=yd.getInertBodyElement(i)}while(i!==o);return Ud(new Bh().sanitizeChildren(F_(e)||e))}finally{if(e){let i=F_(e)||e;for(;i.firstChild;)i.firstChild.remove()}}}function F_(t){return`content`in t&&SI(t)?t.content:null}function SI(t){return t.nodeType===Node.ELEMENT_NODE&&t.nodeName===`TEMPLATE`}var wI=/^>|^->|<!--|-->|--!>|<!-$/g;var EI=/(<|>)/g;var DI=`​$1​`;function II(t){return t.replace(wI,n=>n.replace(EI,DI))}function kI(t,n){return t.createText(n)}function MI(t,n,e){t.setValue(n,e)}function TI(t,n){return t.createComment(II(n))}function Wy(t,n,e){return t.createElement(n,e)}function ho(t,n,e,i,a){t.insertBefore(n,e,i,a)}function qy(t,n,e){t.appendChild(n,e)}function L_(t,n,e,i,a){i!==null?ho(t,n,e,i,a):qy(t,n,e)}function Yy(t,n,e,i){t.removeChild(null,n,e,i)}function NI(t,n,e){t.setAttribute(n,`style`,e)}function AI(t,n,e){e===``?t.removeAttribute(n,`class`):t.setAttribute(n,`class`,e)}function Ky(t,n,e){let{mergedAttrs:i,classes:a,styles:o}=e;i!==null&&zD(t,n,i),a!==null&&AI(t,n,a),o!==null&&NI(t,n,o)}function cn(t){let n=RI();return n?n.sanitize(xn.URL,t)||``:Sa(t,`URL`)?ti(t):Js($e(t))}function RI(){let t=ce();return t&&t[Qn].sanitizer}function OI(t){return t instanceof Function?t():t}function PI(t,n,e){let i=t.length;for(;;){let a=t.indexOf(n,e);if(a===-1)return a;if(a===0||t.charCodeAt(a-1)<=32){let o=n.length;if(a+o===i||t.charCodeAt(a+o)<=32)return a}e=a+1}}var Qy=`ng-template`;function FI(t,n,e,i){let a=0;if(i){for(;a<n.length&&typeof n[a]==`string`;a+=2)if(n[a]===`class`&&PI(n[a+1].toLowerCase(),e,0)!==-1)return!0}else if(xf(t))return!1;if(a=n.indexOf(1,a),a>-1){let o;for(;++a<n.length&&typeof(o=n[a])==`string`;)if(o.toLowerCase()===e)return!0}return!1}function xf(t){return t.type===4&&t.value!==Qy}function LI(t,n,e){return n===(t.type===4&&!e?Qy:t.value)}function VI(t,n,e){let i=4,a=t.attrs,o=a!==null?zI(a):0,r=!1;for(let s=0;s<n.length;s++){let l=n[s];if(typeof l==`number`){if(!r&&!Zn(i)&&!Zn(l))return!1;if(r&&Zn(l))continue;r=!1,i=l|i&1;continue}if(!r)if(i&4){if(i=2|i&1,l!==``&&!LI(t,l,e)||l===``&&n.length===1){if(Zn(i))return!1;r=!0}}else if(i&8){if(a===null||!FI(t,a,l,e)){if(Zn(i))return!1;r=!0}}else{let h=n[++s],p=BI(l,a,xf(t),e);if(p===-1){if(Zn(i))return!1;r=!0;continue}if(h!==``){let g;if(p>o?g=``:g=a[p+1].toLowerCase(),i&2&&h!==g){if(Zn(i))return!1;r=!0}}}}return Zn(i)||r}function Zn(t){return(t&1)===0}function BI(t,n,e,i){if(n===null)return-1;let a=0;if(i||!e){let o=!1;for(;a<n.length;){let r=n[a];if(r===t)return a;if(r===3||r===6)o=!0;else if(r===1||r===2){let s=n[++a];for(;typeof s==`string`;)s=n[++a];continue}else{if(r===4)break;if(r===0){a+=4;continue}}a+=o?1:2}return-1}else return HI(n,t)}function Xy(t,n,e=!1){for(let i=0;i<n.length;i++)if(VI(t,n[i],e))return!0;return!1}function jI(t){let n=t.attrs;if(n!=null){let e=n.indexOf(5);if((e&1)===0)return n[e+1]}return null}function zI(t){for(let n=0;n<t.length;n++){let e=t[n];if(by(e))return n}return t.length}function HI(t,n){let e=t.indexOf(4);if(e>-1)for(e++;e<t.length;){let i=t[e];if(typeof i==`number`)return-1;if(i===n)return e;e++}return-1}function UI(t,n){e:for(let e=0;e<n.length;e++){let i=n[e];if(t.length===i.length){for(let a=0;a<t.length;a++)if(t[a]!==i[a])continue e;return!0}}return!1}function V_(t,n){return t?`:not(`+n.trim()+`)`:n}function $I(t){let n=t[0],e=1,i=2,a=``,o=!1;for(;e<t.length;){let r=t[e];if(typeof r==`string`)if(i&2){let s=t[++e];a+=`[`+r+(s.length>0?`="`+s+`"`:``)+`]`}else i&8?a+=`.`+r:i&4&&(a+=` `+r);else a!==``&&!Zn(r)&&(n+=V_(o,a),a=``),i=r,o=o||!Zn(i);e++}return a!==``&&(n+=V_(o,a)),n}function GI(t){return t.map($I).join(`,`)}function WI(t){let n=[],e=[],i=1,a=2;for(;i<t.length;){let o=t[i];if(typeof o==`string`)a===2?o!==``&&n.push(o,t[++i]):a===8&&e.push(o);else{if(!Zn(a))break;a=o}i++}return e.length&&n.push(1,...e),n}var pt={};var xi=(function(t){return t[t.Important=1]=`Important`,t[t.DashCase=2]=`DashCase`,t})(xi||{});var qI;function Cf(t,n){return qI(t,n)}var Sf=new Set;typeof document<`u`&&document?.documentElement?.getAnimations;var jh=new WeakMap;function Zy(t){return t?t[fa]??t:null}var pr=new WeakSet;function YI(t,n,e){let i=jh.get(t);if(!i||i.length===0)return;let a=n.parentNode,o=n.previousSibling,r=Zy(e);for(let s=i.length-1;s>=0;s--){let{el:l,declarationView:h}=i[s],p=l.parentNode;l===n?(i.splice(s,1),pr.add(l),l.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}}))):o&&l===o?(i.splice(s,1),l.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}})),l.parentNode?.removeChild(l)):p&&a&&p!==a&&(r===null||h===null||r===h)&&(i.splice(s,1),l.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}})),l.parentNode?.removeChild(l))}}function KI(t,n,e){let i=Zy(e),a=jh.get(t);a?a.some(o=>o.el===n)||a.push({el:n,declarationView:i}):jh.set(t,[{el:n,declarationView:i}])}var $d=(function(t){return t[t.CHANGE_DETECTION=0]=`CHANGE_DETECTION`,t[t.AFTER_NEXT_RENDER=1]=`AFTER_NEXT_RENDER`,t})($d||{});var ii=new N(``);var B_=new Set;function Ci(t){B_.has(t)||(B_.add(t),performance?.mark?.(`mark_feature_usage`,{detail:{feature:t}}))}var Gd=(()=>{class t{impl=null;execute(){this.impl?.execute()}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var wf=[0,1,2,3];var Ef=(()=>{class t{ngZone=f(Z);scheduler=f(gi);errorHandler=f(Tn,{optional:!0});sequences=new Set;deferredRegistrations=new Set;executing=!1;constructor(){f(ii,{optional:!0})}execute(){let e=this.sequences.size>0;e&&Ke(ze.AfterRenderHooksStart),this.executing=!0;for(let i of wf)for(let a of this.sequences)if(!(a.erroredOrDestroyed||!a.hooks[i]))try{a.pipelinedValue=this.ngZone.runOutsideAngular(()=>this.maybeTrace(()=>{let o=a.hooks[i];return o(a.pipelinedValue)},a.snapshot))}catch(o){a.erroredOrDestroyed=!0,this.errorHandler?.handleError(o)}this.executing=!1;for(let i of this.sequences)i.afterRun(),i.once&&(this.sequences.delete(i),i.destroy());for(let i of this.deferredRegistrations)this.sequences.add(i);this.deferredRegistrations.size>0&&this.scheduler.notify(7),this.deferredRegistrations.clear(),e&&Ke(ze.AfterRenderHooksEnd)}register(e){let{view:i}=e;i!==void 0?((i[oo]??=[]).push(e),co(i),i[xe]|=8192):this.executing?this.deferredRegistrations.add(e):this.addSequence(e)}addSequence(e){this.sequences.add(e),this.scheduler.notify(7)}unregister(e){this.executing&&this.sequences.has(e)?(e.erroredOrDestroyed=!0,e.pipelinedValue=void 0,e.once=!0):(this.sequences.delete(e),this.deferredRegistrations.delete(e))}maybeTrace(e,i){return i?i.run($d.AFTER_NEXT_RENDER,e):e()}static ɵprov=be({token:t,providedIn:`root`,factory:()=>new t})}return t})();var Ws=class{impl;hooks;view;once;snapshot;erroredOrDestroyed=!1;pipelinedValue=void 0;unregisterOnDestroy;constructor(n,e,i,a,o,r=null){this.impl=n,this.hooks=e,this.view=i,this.once=a,this.snapshot=r,this.unregisterOnDestroy=o?.onDestroy(()=>this.destroy())}afterRun(){this.erroredOrDestroyed=!1,this.pipelinedValue=void 0,this.snapshot?.dispose(),this.snapshot=null}destroy(){this.impl.unregister(this),this.unregisterOnDestroy?.();let n=this.view?.[oo];n&&(this.view[oo]=n.filter(e=>e!==this))}};function Rt(t,n){let e=n?.injector??f(se);return Ci(`NgAfterNextRender`),XI(t,e,n,!0)}function QI(t){return t instanceof Function?[void 0,void 0,t,void 0]:[t.earlyRead,t.write,t.mixedReadWrite,t.read]}function XI(t,n,e,i){let a=n.get(Gd);a.impl??=n.get(Ef);let o=n.get(ii,null,{optional:!0}),r=e?.manualCleanup!==!0?n.get(Ae):null,s=n.get(mr,null,{optional:!0}),l=new Ws(a.impl,QI(t),s?.view,i,r,o?.snapshot(null));return a.impl.register(l),l}var zh=null;var Jy=null;function j_(t,n,e,i){zh?.maybeQueueEnterAnimation(t,n,e,i)}function z_(t,n,e,i){zh===null?i(!1):zh.runLeaveAnimationsWithCallback(t,n,e,i)}function ZI(t){Jy?.initViewDetachAnimations(t)}function JI(t){Jy?.clearViewDetachAnimations(t)}function hr(t,n,e,i,a,o,r,s){if(a!=null){let l,h=!1;An(a)?l=a:zi(a)&&(h=!0,a=a[Kn]);let p=$t(a);t===0&&i!==null?(j_(s,i,o,e),r==null?qy(n,i,p):ho(n,i,p,r||null,!0)):t===1&&i!==null?(ho(n,i,p,r||null,!0),YI(o,p,s),pr.has(p)||j_(s,i,o,e)):t===2?(s?.[Yv]?.leave?.has(o.index)&&KI(o,p,s),pr.delete(p),z_(s,o,e,g=>{if(pr.has(p)){pr.delete(p);return}Yy(n,p,h,g)})):t===3&&(pr.delete(p),z_(s,o,e,()=>{n.destroyNode(p)})),l!=null&&dk(n,t,e,l,o,i,r)}}function ek(t,n){e0(t,n),n[Kn]=null,n[Ut]=null}function tk(t,n,e,i,a,o){i[Kn]=a,i[Ut]=n,qd(t,i,e,1,a,o)}function e0(t,n){n[Qn].changeDetectionScheduler?.notify(9),qd(t,n,n[et],2,null,null)}function nk(t){let n=t[sr];if(!n)return gh(t[ve],t);for(;n;){let e=null;if(zi(n))e=n[sr];else{let i=n[xt];i&&(e=i)}if(!e){for(;n&&!n[Nn]&&n!==t;)zi(n)&&gh(n[ve],n),n=n[Et];n===null&&(n=t),zi(n)&&gh(n[ve],n),e=n&&n[Nn]}n=e}}function Df(t,n){let e=t[so],i=e.indexOf(n);e.splice(i,1)}function Wd(t,n){if(lo(n))return;let e=n[et];e.destroyNode&&qd(t,n,e,3,null,null),nk(n)}function gh(t,n){if(lo(n))return;let e=ge(null);try{n[xe]&=-129,n[xe]|=256,n[yn]&&da(n[yn]),ak(t,n),ik(t,n),n[ve].type===1&&n[et].destroy();let i=n[ga];if(i!==null&&An(n[Et])){i!==n[Et]&&Df(i,n);let a=n[vi];a!==null&&a.detachView(t)}Mh(n)}finally{ge(e)}}function ik(t,n){let e=t.cleanup,i=n[rr];if(e!==null)for(let r=0;r<e.length-1;r+=2)if(typeof e[r]==`string`){let s=e[r+3];s>=0?i[s]():i[-s].unsubscribe(),r+=2}else{let s=i[e[r+1]];e[r].call(s)}i!==null&&(n[rr]=null);let a=n[Fi];if(a!==null){n[Fi]=null;for(let r=0;r<a.length;r++){let s=a[r];s()}}let o=n[Li];if(o!==null){n[Li]=null;for(let r of o)r.destroy()}}function ak(t,n){let e;if(t!=null&&(e=t.destroyHooks)!=null)for(let i=0;i<e.length;i+=2){let a=n[e[i]];if(!(a instanceof go)){let o=e[i+1];if(Array.isArray(o))for(let r=0;r<o.length;r+=2){let s=a[o[r]],l=o[r+1];Ke(ze.LifecycleHookStart,s,l);try{l.call(s)}finally{Ke(ze.LifecycleHookEnd,s,l)}}else{Ke(ze.LifecycleHookStart,a,o);try{o.call(a)}finally{Ke(ze.LifecycleHookEnd,a,o)}}}}}function t0(t,n,e){if(n===null)throw new q(510,!1);return ok(t,n.parent,e)}function ok(t,n,e){let i=n;for(;i!==null&&i.type&168;)n=i,i=n.parent;if(i===null)return e[Kn];if(_i(i)){let{encapsulation:a}=t.data[i.directiveStart+i.componentOffset];if(a===ei.None||a===ei.Emulated)return null}return Cn(i,e)}function n0(t,n,e){return sk(t,n,e)}function rk(t,n,e){return t.type&40?Cn(t,e):null}var sk=rk;function If(t,n,e,i){let a=t0(t,i,n),o=n[et],s=n0(i.parent||n[Ut],i,n);if(a!=null)if(Array.isArray(e))for(let l=0;l<e.length;l++)L_(o,a,e[l],s,!1);else L_(o,a,e,s,!1)}function Us(t,n){if(n!==null){let e=n.type;if(e&3)return Cn(n,t);if(e&4)return Hh(-1,t[n.index]);if(e&8){let i=n.child;if(i!==null)return Us(t,i);{let a=t[n.index];return An(a)?Hh(-1,a):$t(a)}}else{if(e&128)return Us(t,n.next);if(e&32)return Cf(n,t)()||$t(t[n.index]);{let i=i0(t,n);if(i!==null){if(Array.isArray(i))return i[0];return Us(Vi(t[Qt]),i)}else return Us(t,n.next)}}}return null}function i0(t,n){if(n!==null){let i=t[Qt][Ut],a=n.projection;return i.projection[a]}return null}function Hh(t,n){let e=xt+t+1;if(e<n.length){let i=n[e],a=i[ve].firstChild;if(a!==null)return Us(i,a)}return n[ro]}function kf(t,n,e,i,a,o,r){for(;e!=null;){let s=i[ji];if(e.type===128){e=e.next;continue}let l=i[e.index],h=e.type;if(r&&n===0&&(l&&br($t(l),i),e.flags|=2),!Hd(e))if(h&8)kf(t,n,e.child,i,a,o,!1),hr(n,t,s,a,l,e,o,i);else if(h&32){let p=Cf(e,i),g;for(;g=p();)hr(n,t,s,a,g,e,o,i);hr(n,t,s,a,l,e,o,i)}else h&16?a0(t,n,i,e,a,o):hr(n,t,s,a,l,e,o,i);e=r?e.projectionNext:e.next}}function qd(t,n,e,i,a,o){t.type===3?lk(e,i,n,a,o):kf(e,i,t.firstChild,n,a,o,!1)}function lk(t,n,e,i,a){let r=e[ve].firstChild,s=r.next,l=$t(e[r.index]),h=$t(e[s.index]),p=s.index+1,g=e[p];if(n===1||n===0)i!==null&&(g&&g.hasChildNodes()?ho(t,i,g,a,!0):(ho(t,i,l,a,!0),ho(t,i,h,a,!0)));else if(n===2){if(g||(g=document.createDocumentFragment(),e[p]=g),l&&l.parentNode===g)return;let _=l;for(;_!==null;){let b=_.nextSibling;if(g.appendChild(_),_===h)break;_=b}}}function ck(t,n,e){let i=n[et];a0(i,0,n,e,t0(t,e,n),n0(e.parent||n[Ut],e,n))}function a0(t,n,e,i,a,o){let r=e[Qt],l=r[Ut].projection[i.projection];if(Array.isArray(l))for(let h=0;h<l.length;h++){let p=l[h];hr(n,t,e[ji],a,p,i,o,e)}else{let h=l,p=r[Et];ky(i)&&(h.flags|=128),kf(t,n,h,p,a,o,!0)}}function dk(t,n,e,i,a,o,r){let s=i[ro];if(s!==$t(i)&&hr(n,t,e,o,s,a,r),(i[xe]&4)===0)for(let h=xt;h<i.length;h++){let p=i[h];qd(p[ve],p,t,n,o,s)}}function uk(t,n,e,i,a){if(n)a?t.addClass(e,i):t.removeClass(e,i);else{let o=i.indexOf(`-`)===-1?void 0:xi.DashCase;a==null?t.removeStyle(e,i,o):(typeof a==`string`&&a.endsWith(`!important`)&&(a=a.slice(0,-10),o|=xi.Important),t.setStyle(e,i,a,o))}}function Mf(t,n,e,i,a,o,r,s,l,h,p){let g=ot+i,_=g+a,b=mk(g,_),y=typeof h==`function`?h():h;return b[ve]={type:t,blueprint:b,template:e,queries:null,viewQuery:s,declTNode:n,data:b.slice().fill(null,g),bindingStartIndex:g,expandoStartIndex:_,hostBindingOpCodes:null,firstCreatePass:!0,firstUpdatePass:!0,staticViewQueries:!1,staticContentQueries:!1,preOrderHooks:null,preOrderCheckHooks:null,contentHooks:null,contentCheckHooks:null,viewHooks:null,viewCheckHooks:null,destroyHooks:null,cleanup:null,contentQueries:null,components:null,directiveRegistry:typeof o==`function`?o():o,pipeRegistry:typeof r==`function`?r():r,firstChild:null,schemas:l,consts:y,incompleteFirstPass:!1,ssrId:p}}function mk(t,n){let e=[];for(let i=0;i<n;i++)e.push(i<t?null:pt);return e}function pk(t){let n=t.tView;return n===null||n.incompleteFirstPass?t.tView=Mf(1,null,t.template,t.decls,t.vars,t.directiveDefs,t.pipeDefs,t.viewQuery,t.schemas,t.consts,t.id):n}function Tf(t,n,e,i,a,o,r,s,l,h,p){let g=n.blueprint.slice();return g[Kn]=a,g[xe]=i|1228,(h!==null||t&&t[xe]&2048)&&(g[xe]|=2048),Wp(g),g[Et]=g[fa]=t,g[yt]=e,g[Qn]=r||t&&t[Qn],g[et]=s||t&&t[et],g[ji]=l||t&&t[ji]||null,g[Ut]=o,g[Rs]=tI(),g[io]=p,g[zp]=h,g[Qt]=n.type==2?t[Qt]:g,g}function hk(t,n,e){let i=Cn(n,t),a=pk(e),o=t[Qn].rendererFactory,r=Nf(t,Tf(t,a,null,o0(e),i,n,null,o.createRenderer(i,e),null,null,null));return t[n.index]=r}function o0(t){let n=16;return t.signals?n=4096:t.onPush&&(n=64),n}function r0(t,n,e,i){if(e===0)return-1;let a=n.length;for(let o=0;o<e;o++)n.push(i),t.blueprint.push(i),t.data.push(null);return a}function Nf(t,n){return t[sr]?t[jp][Nn]=n:t[sr]=n,t[jp]=n,n}function m(t=1){s0(Xe(),ce(),Xt()+t,!1)}function s0(t,n,e,i){if(!i)if((n[xe]&3)===3){let o=t.preOrderCheckHooks;o!==null&&Cd(n,o,e)}else{let o=t.preOrderHooks;o!==null&&Sd(n,o,0,e)}va(e)}var tl=(function(t){return t[t.None=0]=`None`,t[t.SignalBased=1]=`SignalBased`,t[t.HasDecoratorInputTransform=2]=`HasDecoratorInputTransform`,t})(tl||{});function bo(t,n,e,i){let a=ge(null);try{let[o,r,s]=t.inputs[e],l=null;(r&tl.SignalBased)!==0&&(l=n[o][St]),l!==null&&l.transformFn!==void 0?i=l.transformFn(i):s!==null&&(i=s.call(n,i)),t.setInput!==null?t.setInput(n,l,i,e,o):my(n,l,o,i)}finally{ge(a)}}function l0(t,n,e,i,a){let o=Xt(),r=i&2;try{va(-1),r&&n.length>ot&&s0(t,n,ot,!1);Ke(r?ze.TemplateUpdateStart:ze.TemplateCreateStart,a,e),e(i,a)}finally{va(o);Ke(r?ze.TemplateUpdateEnd:ze.TemplateCreateEnd,a,e)}}function Yd(t,n,e){yk(t,n,e),(e.flags&64)===64&&xk(t,n,e)}function nl(t,n,e=Cn){let i=n.localNames;if(i!==null){let a=n.index+1;for(let o=0;o<i.length;o+=2){let r=i[o+1],s=r===-1?e(n,t):t[r];t[a++]=s}}}function fk(t,n,e,i){let o=i.get(Ly,Fy)||e===ei.ShadowDom||e===ei.ExperimentalIsolatedShadowDom;return t.selectRootElement(n,o)}function vk(t){return t===`class`?`className`:t===`for`?`htmlFor`:t===`formaction`?`formAction`:t===`innerHtml`?`innerHTML`:t===`readonly`?`readOnly`:t===`tabindex`?`tabIndex`:t}function _k(t,n,e,i,a,o){let r=n[ve];if(Kd(t,r,n,e,i)){_i(t)&&d0(n,t.index);return}t.type&3&&(e=vk(e)),c0(t,n,e,i,a,o)}function c0(t,n,e,i,a,o){if(t.type&3){let r=Cn(t,n);i=o!=null?o(i,t.value||``,e):i,a.setProperty(r,e,i)}else t.type&12}function d0(t,n){let e=Rn(n,t);e[xe]&16||(e[xe]|=64)}function yk(t,n,e){let i=e.directiveStart,a=e.directiveEnd;_i(e)&&hk(n,e,t.data[i+e.componentOffset]),t.firstCreatePass||Td(e,n);let o=e.initialInputs;for(let r=i;r<a;r++){let s=t.data[r],l=Gs(n,t,r,e);if(br(l,n),o!==null&&wk(n,r-i,l,s,e,o),Xn(s)){let h=Rn(e.index,n);h[yt]=Gs(n,t,r,e)}}}function xk(t,n,e){let i=e.directiveStart,a=e.directiveEnd,o=e.index,r=l_();try{va(o);for(let s=i;s<a;s++){let l=t.data[s],h=n[s];ud(s),(l.hostBindings!==null||l.hostVars!==0||l.hostAttrs!==null)&&Ck(l,h)}}finally{va(-1),ud(r)}}function Ck(t,n){t.hostBindings!==null&&t.hostBindings(1,n)}function Af(t,n){let e=t.directiveRegistry,i=null;if(e)for(let a=0;a<e.length;a++){let o=e[a];Xy(n,o.selectors,!1)&&(i??=[],Xn(o)?i.unshift(o):i.push(o))}return i}function Sk(t,n,e,i,a,o){let r=Cn(t,n);u0(n[et],r,o,t.value,e,i,a)}function u0(t,n,e,i,a,o,r){if(o==null)r?.(o,i||``,a),t.removeAttribute(n,a,e);else{let s=r==null?$e(o):r(o,i||``,a);t.setAttribute(n,a,s,e)}}function wk(t,n,e,i,a,o){let r=o[n];if(r!==null)for(let s=0;s<r.length;s+=2){let l=r[s],h=r[s+1];bo(i,e,l,h)}}function Rf(t,n,e,i,a){let o=ot+e,r=n[ve],s=a(r,n,t,i,e);n[o]=s,uo(t,!0);let l=t.type===2;return l?(Ky(n[et],s,t),(n_()===0||cr(t))&&br(s,n),i_()):br(s,n),fd()&&(!l||!Hd(t))&&If(r,n,s,t),t}function Of(t){let n=t;return th()?nh():(n=n.parent,uo(n,!1)),n}function Ek(t,n){let e=t[ji];if(!e)return;let i;try{i=e.get(Ui,null)}catch{i=null}i?.(n)}function Kd(t,n,e,i,a){let o=t.inputs?.[i],r=t.hostDirectiveInputs?.[i],s=!1;if(r)for(let l=0;l<r.length;l+=2){let h=r[l],p=r[l+1],g=n.data[h];bo(g,e[h],p,a),s=!0}if(o)for(let l of o){let h=e[l],p=n.data[l];bo(p,h,i,a),s=!0}return s}function Dk(t,n,e,i,a,o){let r=null,s=null,l=null,h=!1,p=t.directiveToIndex.get(i.type);if(typeof p==`number`?r=p:[r,s,l]=p,s!==null&&l!==null&&t.hostDirectiveInputs&&Object.hasOwn(t.hostDirectiveInputs,a)){let g=t.hostDirectiveInputs[a];for(let _=0;_<g.length;_+=2){let b=g[_];if(b>=s&&b<=l){let y=n.data[b],I=g[_+1];bo(y,e[b],I,o),h=!0}else if(b>l)break}}return r!==null&&Object.hasOwn(i.inputs,a)&&(bo(i,e[r],a,o),h=!0),h}function Ik(t,n){let e=Rn(n,t),i=e[ve];kk(i,e);let a=e[Kn];a!==null&&e[io]===null&&(e[io]=Vy(a,e[ji])),Ke(ze.ComponentStart);try{Pf(i,e,e[yt])}finally{Ke(ze.ComponentEnd,e[yt])}}function kk(t,n){for(let e=n.length;e<t.blueprint.length;e++)n.push(t.blueprint[e])}function Pf(t,n,e){pd(n);try{let i=t.viewQuery;i!==null&&Nh(1,i,e);let a=t.template;a!==null&&l0(t,n,a,1,e),t.firstCreatePass&&(t.firstCreatePass=!1),n[vi]?.finishViewCreation(t),t.staticContentQueries&&By(t,n),t.staticViewQueries&&Nh(2,t.viewQuery,e);let o=t.components;o!==null&&Mk(n,o)}catch(i){throw t.firstCreatePass&&(t.incompleteFirstPass=!0,t.firstCreatePass=!1),i}finally{n[xe]&=-5,hd()}}function Mk(t,n){for(let e=0;e<n.length;e++)Ik(t,n[e])}function il(t,n,e,i){let a=ge(null);try{let o=n.tView,l=Tf(t,o,e,t[xe]&4096?4096:16,null,n,null,null,i?.injector??null,i?.embeddedViewInjector??null,i?.dehydratedView??null);l[ga]=t[n.index];let p=t[vi];return p!==null&&(l[vi]=p.createEmbeddedView(o)),Pf(o,l,e),l}finally{ge(a)}}function vr(t,n){return!n||n.firstChild===null||ky(t)}function qs(t,n,e,i,a=!1){if(t.type===3){let o=t.firstChild,r=o.next,s=$t(n[o.index]),l=$t(n[r.index]),h=s;for(;h!==null&&(i.push(h),h!==l);)h=h.nextSibling;return i}for(;e!==null;){if(e.type===128){e=a?e.projectionNext:e.next;continue}let o=n[e.index];if(o!==null)if(An(o)){let s=o[ro];s!==o[Kn]&&i.push($t(o)),o[xe]&4||m0(o,i),i.push(s)}else i.push($t(o));let r=e.type;if(r&8)qs(t,n,e.child,i);else if(r&32){let s=Cf(e,n),l;for(;l=s();)i.push(l)}else if(r&16){let s=i0(n,e);if(Array.isArray(s))i.push(...s);else{let l=Vi(n[Qt]);qs(l[ve],l,s,i,!0)}}e=a?e.projectionNext:e.next}return i}function m0(t,n){for(let e=xt;e<t.length;e++){let i=t[e],a=i[ve].firstChild;a!==null&&qs(i[ve],i,a,n)}}function p0(t){if(t[oo]!==null){for(let n of t[oo])n.impl.addSequence(n);t[oo].length=0}}var h0=[];function Tk(t){return t[yn]??Nk(t)}function Nk(t){let n=h0.pop()??Object.create(Rk);return n.lView=t,n}function Ak(t){t.lView[yn]!==t&&(t.lView=null,h0.push(t))}var Rk=s(r({},Ua),{consumerIsAlwaysLive:!0,kind:`template`,consumerMarkedDirty:t=>{co(t.lView)},consumerOnSignalRead(){this.lView[yn]=this}});function Ok(t){let n=t[yn]??Object.create(Pk);return n.lView=t,n}var Pk=s(r({},Ua),{consumerIsAlwaysLive:!0,kind:`template`,consumerMarkedDirty:t=>{let n=Vi(t.lView);for(;n&&!f0(n[ve]);)n=Vi(n);n&&qp(n)},consumerOnSignalRead(){this.lView[yn]=this}});function f0(t){return t.type!==2}function g0(t){if(t[Li]===null)return;let n=!0;for(;n;){let e=!1;for(let i of t[Li])if(i.dirty&&(e=!0,i.zone===null||Zone.current===i.zone?i.run():i.zone.run(()=>i.run()),t[Li]===null))return;n=e&&!!(t[xe]&8192)}}var Fk=100;function b0(t,n=0){let i=t[Qn].rendererFactory;i.begin?.();try{Lk(t,n)}finally{i.end?.()}}function Lk(t,n){let e=ah();try{ws(!0),Uh(t,n);let i=0;for(;Ls(t);){if(i===Fk)throw new q(103,!1);i++,Uh(t,1)}}finally{ws(e)}}function Vk(t,n,e,i){if(lo(n))return;let a=n[xe];pd(n);let s=!0,l=null,h=null;f0(t)?(h=Tk(n),l=ca(h)):lc()===null?(s=!1,h=Ok(n),l=ca(h)):n[yn]&&(da(n[yn]),n[yn]=null);try{Wp(n),o_(t.bindingStartIndex),e!==null&&l0(t,n,e,2,i);let p=(a&3)===3;if(p){let b=t.preOrderCheckHooks;b!==null&&Cd(n,b,null)}else{let b=t.preOrderHooks;b!==null&&Sd(n,b,0,null),hh(n,0)}if(Bk(n),g0(n),v0(n,0),t.contentQueries!==null&&By(t,n),true)if(p){let b=t.contentCheckHooks;b!==null&&Cd(n,b)}else{let b=t.contentHooks;b!==null&&Sd(n,b,1),hh(n,1)}zk(t,n);let g=t.components;g!==null&&y0(n,g,0);let _=t.viewQuery;if(_!==null&&Nh(2,_,i),true)if(p){let b=t.viewCheckHooks;b!==null&&Cd(n,b)}else{let b=t.viewHooks;b!==null&&Sd(n,b,2),hh(n,2)}if(t.firstUpdatePass===!0&&(t.firstUpdatePass=!1),n[ad]){for(let b of n[ad])b();n[ad]=null}p0(n),n[xe]&=-73}catch(p){let g=!1,_=p,b=n;for(;b!==null;){if(An(b)){b=b[Et];continue}let y=b[lr];if(y)try{let I=n[Qt][yt],ie={declarationInstance:I,declarationType:I?.constructor,caughtBy:y};y(__(_),ie),g=!0;break}catch(I){_=I}b=b[Et]}if(!g)throw co(n),_}finally{h!==null&&($a(h,l),s&&Ak(h)),hd()}}function v0(t,n){for(let e=Ty(t);e!==null;e=Ny(e))for(let i=xt;i<e.length;i++){let a=e[i];_0(a,n)}}function Bk(t){for(let n=Ty(t);n!==null;n=Ny(n)){if(!(n[xe]&2))continue;let e=n[so];for(let i=0;i<e.length;i++){let a=e[i];qp(a)}}}function jk(t,n,e){Ke(ze.ComponentStart);let i=Rn(n,t);try{_0(i,e)}finally{Ke(ze.ComponentEnd,i[yt])}}function _0(t,n){rd(t)&&Uh(t,n)}function Uh(t,n){let i=t[ve],a=t[xe],o=t[yn],r=!!(n===0&&a&16);if(r||=!!(a&64&&n===0),r||=!!(a&1024),r||=!!(o?.dirty&&$o(o)),r||=!1,o&&(o.dirty=!1),t[xe]&=-9217,r)Vk(i,t,i.template,t[yt]);else if(a&8192){let s=ge(null);try{g0(t),v0(t,1);let l=i.components;l!==null&&y0(t,l,1),p0(t)}finally{ge(s)}}}function y0(t,n,e){for(let i=0;i<n.length;i++)jk(t,n[i],e)}function zk(t,n){let e=t.hostBindingOpCodes;if(e!==null)try{for(let i=0;i<e.length;i++){let a=e[i];if(a<0)va(~a);else{let o=a,r=e[++i],s=e[++i];s_(r,o);let l=n[o];Ke(ze.HostBindingsUpdateStart,l);try{s(2,l)}finally{Ke(ze.HostBindingsUpdateEnd,l)}}}}finally{va(-1)}}function Ff(t,n){let e=ah()?64:1088;for(t[Qn].changeDetectionScheduler?.notify(n);t;){t[xe]|=e;let i=Vi(t);if(dr(t)&&!i)return t;t=i}return null}function x0(t,n,e,i){return[t,!0,0,n,null,i,null,e,null,null]}function C0(t,n){let e=xt+n;if(e<t.length)return t[e]}function al(t,n,e,i=!0){let a=n[ve];if(Hk(a,n,t,e),i){let r=Hh(e,t),s=n[et],l=s.parentNode(t[ro]);l!==null&&tk(a,t[Ut],s,n,l,r)}let o=n[io];o!==null&&o.firstChild!==null&&(o.firstChild=null)}function S0(t,n){let e=Ys(t,n);return e!==void 0&&Wd(e[ve],e),e}function Ys(t,n){if(t.length<=xt)return;let e=xt+n,i=t[e];if(i){let a=i[ga];a!==null&&a!==t&&Df(a,i),n>0&&(t[e-1][Nn]=i[Nn]);let o=Ns(t,xt+n);ek(i[ve],i);let r=o[vi];r!==null&&r.detachView(o[ve]),i[Et]=null,i[Nn]=null,i[xe]&=-129}return i}function Hk(t,n,e,i){let a=xt+i,o=e.length;i>0&&(e[a-1][Nn]=n),i<o-xt?(n[Nn]=e[a],Pp(e,xt+i,n)):(e.push(n),n[Nn]=null),n[Et]=e;let r=n[ga];r!==null&&e!==r&&w0(r,n);let s=n[vi];s!==null&&s.insertView(t),sd(n),n[xe]|=128}function w0(t,n){let e=t[so],i=n[Et];if(zi(i))t[xe]|=2;else{let a=i[Et][Qt];n[Qt]!==a&&(t[xe]|=2)}e===null?t[so]=[n]:e.push(n)}var Ca=class{_lView;_cdRefInjectingView;_appRef=null;_attachedToViewContainer=!1;exhaustive;get rootNodes(){let n=this._lView,e=n[ve];return qs(e,n,e.firstChild,[])}constructor(n,e){this._lView=n,this._cdRefInjectingView=e}get context(){return this._lView[yt]}set context(n){this._lView[yt]=n}get destroyed(){return lo(this._lView)}destroy(){if(this._appRef)this._appRef.detachView(this);else if(this._attachedToViewContainer){let n=this._lView[Et];if(An(n)){let e=n[Os],i=e?e.indexOf(this):-1;i>-1&&(Ys(n,i),Ns(e,i))}this._attachedToViewContainer=!1}Wd(this._lView[ve],this._lView)}onDestroy(n){ld(this._lView,n)}markForCheck(){Ff(this._cdRefInjectingView||this._lView,4)}detach(){this._lView[xe]&=-129}reattach(){sd(this._lView),this._lView[xe]|=128}detectChanges(){this._lView[xe]|=1024,b0(this._lView)}checkNoChanges(){}attachToViewContainerRef(){if(this._appRef)throw new q(902,!1);this._attachedToViewContainer=!0}detachFromAppRef(){this._appRef=null;let n=dr(this._lView),e=this._lView[ga];e!==null&&!n&&Df(e,this._lView),e0(this._lView[ve],this._lView)}attachToAppRef(n){if(this._attachedToViewContainer)throw new q(902,!1);this._appRef=n;let e=dr(this._lView),i=this._lView[ga];i!==null&&!e&&w0(i,this._lView),sd(this._lView)}};var Vt=(()=>{class t{_declarationLView;_declarationTContainer;elementRef;static __NG_ELEMENT_ID__=Uk;constructor(e,i,a){this._declarationLView=e,this._declarationTContainer=i,this.elementRef=a}get ssrId(){return this._declarationTContainer.tView?.ssrId||null}createEmbeddedView(e,i){return this.createEmbeddedViewImpl(e,i)}createEmbeddedViewImpl(e,i,a){return new Ca(il(this._declarationLView,this._declarationTContainer,e,{embeddedViewInjector:i,dehydratedView:a}))}}return t})();function Uk(){return Qd(Tt(),ce())}function Qd(t,n){return t.type&4?new Vt(n,t,wr(t,n)):null}function _o(t,n,e,i,a){let o=t.data[n];if(o===null)o=$k(t,n,e,i,a),r_()&&(o.flags|=32);else if(o.type&64){o.type=e,o.value=i,o.attrs=a;let r=a_();o.injectorIndex=r===null?-1:r.injectorIndex}return uo(o,!0),o}function $k(t,n,e,i,a){let o=eh(),r=th(),s=r?o:o&&o.parent,l=t.data[n]=Wk(t,s,e,n,i,a);return Gk(t,l,o,r),l}function Gk(t,n,e,i){t.firstChild===null&&(t.firstChild=n),e!==null&&(i?e.child==null&&n.parent!==null&&(e.child=n):e.next===null&&(e.next=n,n.prev=e))}function Wk(t,n,e,i,a,o){let r=n?n.injectorIndex:-1,s=0;return Xp()&&(s|=128),{type:e,index:i,insertBeforeIndex:null,injectorIndex:r,directiveStart:-1,directiveEnd:-1,directiveStylingLast:-1,componentOffset:-1,controlDirectiveIndex:-1,customControlIndex:-1,propertyBindings:null,flags:s,providerIndexes:0,value:a,namespace:sh(),attrs:o,mergedAttrs:null,localNames:null,initialInputs:null,inputs:null,hostDirectiveInputs:null,outputs:null,hostDirectiveOutputs:null,directiveToIndex:null,tView:null,next:null,prev:null,projectionNext:null,child:null,parent:n,projection:null,styles:null,stylesWithoutHost:null,residualStyles:void 0,classes:null,classesWithoutHost:null,residualClasses:void 0,classBindings:0,styleBindings:0}}function qk(t){let n=t[Hp]??[],i=t[Et][et],a=[];for(let o of n)o.data[Py]!==void 0?a.push(o):Yk(o,i);t[Hp]=a}function Yk(t,n){let e=0,i=t.firstChild;if(i){let a=t.data[Oy];for(;e<a;){let o=i.nextSibling;Yy(n,i,!1),i=o,e++}}}var Kk=()=>null;var Qk=()=>null;function Nd(t,n){return Kk(t,n)}function E0(t,n,e){return Qk(t,n,e)}var D0=class{};var Nt=class{};var Oe=class{destroyNode=null;static __NG_ELEMENT_ID__=()=>Xk()};function Xk(){let t=ce(),e=Rn(Tt().index,t);return(zi(e)?e:t)[et]}var I0=(()=>{class t{static ɵprov=be({token:t,providedIn:`root`,factory:()=>null})}return t})();function k0(t){return t.debugInfo?.className||t.type.name||null}var Ed={};var Ad=class{injector;parentInjector;constructor(n,e){this.injector=n,this.parentInjector=e}get(n,e,i){let a=this.injector.get(n,Ed,i);return a!==Ed||e===Ed?a:this.parentInjector.get(n,e,i)}};function Lf(t,n,e){return t[n]=e}function Zk(t,n){return t[n]}function fn(t,n,e){if(e===pt)return!1;let i=t[n];return Object.is(i,e)?!1:(t[n]=e,!0)}function _r(t,n,e,i){let a=fn(t,n,e);return fn(t,n+1,i)||a}function Jk(t,n,e,i,a){let o=_r(t,n,e,i);return fn(t,n+2,a)||o}function Ks(t,n,e,i,a,o){let r=_r(t,n,e,i);return _r(t,n+2,a,o)||r}function fo(t,n,e){return function i(a){let o=i.__ngNativeEl__;o!==void 0&&iI(a,o);Ff(_i(t)?Rn(t.index,n):n,5);let s=n[yt],l=U_(n,s,e,a),h=i.__ngNextListenerFn__;for(;h;)l=U_(n,s,h,a)&&l,h=h.__ngNextListenerFn__;return l}}function U_(t,n,e,i){let a=ge(null);try{return Ke(ze.OutputStart,n,e),e(i)!==!1}catch(o){return Ek(t,o),!1}finally{Ke(ze.OutputEnd,n,e),ge(a)}}function Vf(t,n,e,i,a,o,r,s){let l=cr(t),h=!1,p=null;if(!i&&l&&(p=tM(n,e,o,t.index)),p!==null){let g=p.__ngLastListenerFn__||p;g.__ngNextListenerFn__=r,p.__ngLastListenerFn__=r,h=!0}else{let g=Cn(t,e),_=i?i(g):g;i||(s.__ngNativeEl__=g);let b=a.listen(_,o,s);if(!eM(o))M0(i?I=>i($t(I[t.index])):t.index,n,e,o,s,b,!1)}return h}function eM(t){return t.startsWith(`animation`)||t.startsWith(`transition`)}function tM(t,n,e,i){let a=t.cleanup;if(a!=null)for(let o=0;o<a.length-1;o+=2){let r=a[o];if(r===e&&a[o+1]===i){let s=n[rr],l=a[o+2];return s&&s.length>l?s[l]:null}typeof r==`string`&&(o+=2)}return null}function M0(t,n,e,i,a,o,r){let s=n.firstCreatePass?Kp(n):null,l=Yp(e),h=l.length;l.push(a,o),s&&s.push(i,t,h,(h+1)*(r?-1:1))}function $_(t,n,e,i,a){let o=null,r=null,s=null,l=!1,h=t.directiveToIndex.get(e.type);if(typeof h==`number`?o=h:[o,r,s]=h,r!==null&&s!==null&&t.hostDirectiveOutputs&&Object.hasOwn(t.hostDirectiveOutputs,i)){let p=t.hostDirectiveOutputs[i];for(let g=0;g<p.length;g+=2){let _=p[g];if(_>=r&&_<=s)l=!0,Rd(t,n,_,p[g+1],i,a);else if(_>s)break}}return Object.hasOwn(e.outputs,i)&&(l=!0,Rd(t,n,o,i,i,a)),l}function Rd(t,n,e,i,a,o){let r=n[e],s=n[ve],g=r[s.data[e].outputs[i]].subscribe(o);M0(t.index,s,n,a,o,g,!0)}function Er(){nM()}function nM(){let t=ce(),n=Xe(),e=Tt();if(n.firstCreatePass&&aM(n,e),e.controlDirectiveIndex===-1)return;Ci(`NgSignalForms`);let i=t[e.controlDirectiveIndex];n.data[e.controlDirectiveIndex].controlDef.create(i,new Od(t,n,e))}function Dr(){iM()}function iM(){let t=ce(),n=Xe(),e=ur();if(e.controlDirectiveIndex===-1)return;let i=n.data[e.controlDirectiveIndex].controlDef,a=t[e.controlDirectiveIndex];i.update(a,new Od(t,n,e))}var Od=class{lView;tView;tNode;hasPassThrough;constructor(n,e,i){this.lView=n,this.tView=e,this.tNode=i,this.hasPassThrough=!!(i.flags&4096)}get customControl(){return this.tNode.customControlIndex!==-1?this.lView[this.tNode.customControlIndex]:void 0}get nativeElement(){return Cn(this.tNode,this.lView)}get descriptor(){return`<${this.tNode.value}>`}listenToCustomControlOutput(n,e){let i=this.tView.data[this.tNode.customControlIndex];$_(this.tNode,this.lView,i,n,fo(this.tNode,this.lView,e))}listenToCustomControlModel(n){let e=this.tNode.flags&1024?`valueChange`:`checkedChange`,i=this.tView.data[this.tNode.customControlIndex];$_(this.tNode,this.lView,i,e,fo(this.tNode,this.lView,n))}listenToDom(n,e){Vf(this.tNode,this.tView,this.lView,void 0,this.lView[et],n,e,fo(this.tNode,this.lView,e))}setInputOnDirectives(n,e,i){let a=this.tNode.inputs?.[n],o=this.tNode.hostDirectiveInputs?.[n];if(!a&&!o)return!1;let r=!1;if(a)for(let s of a){if(s===this.tNode.controlDirectiveIndex)continue;let l=this.lView[s],h=this.tView.data[s];(!i||i(W_(l,h,n)))&&(bo(h,l,n,e),r=!0)}if(o)for(let s=0;s<o.length;s+=2){let l=o[s];if(l===this.tNode.controlDirectiveIndex)continue;let h=this.lView[l],p=o[s+1],g=this.tView.data[l];(!i||i(W_(h,g,n)))&&(bo(g,h,p,e),r=!0)}return r}setCustomControlModelInput(n){let e=this.tView.data[this.tNode.customControlIndex],i=this.tNode.flags&1024?`value`:`checked`;Dk(this.tNode,this.tView,this.lView,e,i,n)}customControlHasInput(n){if(this.tNode.customControlIndex===-1)return!1;let e=this.tView.data[this.tNode.customControlIndex];return(e.signalFormsInputPresence??=this._buildCustomControlInputCache(e))[n]===!0}_buildCustomControlInputCache(n){let e={};for(let i in n.inputs)e[i]=!0;if(n.hostDirectives!==null){let i=[...n.hostDirectives];for(;i.length>0;){let a=i.shift();if(typeof a!=`function`){for(let r in a.inputs)e[a.inputs[r]]=!0;let o=G_(a.directive);o!==null&&i.push(...o);continue}for(let o of a()){if(typeof o==`function`)continue;if(o.inputs)for(let s=0;s<o.inputs.length;s+=2){let l=o.inputs[s+1]||o.inputs[s];e[l]=!0}let r=G_(o.directive);r!==null&&i.push(...r)}}}return e}};function G_(t){return typeof t==`function`&&`ɵdir`in t?t.ɵdir.hostDirectives??null:null}function W_(t,n,e){if(!n.inputs||!Object.hasOwn(n.inputs,e))return;let[i,a]=n.inputs[e];if((a&tl.SignalBased)!==0){let r=t[i][St];return r.value===Sr?void 0:r.value}return t[i]}function aM(t,n,e){for(let a=n.directiveStart;a<n.directiveEnd;a++)if(t.data[a].controlDef){n.controlDirectiveIndex=a;break}if(n.controlDirectiveIndex===-1)return;let i=t.data[n.controlDirectiveIndex].controlDef;if(i.passThroughInput&&(n.inputs?.[i.passThroughInput]?.length??0)>1){n.flags|=4096;return}oM(t,n)}function oM(t,n){for(let e=n.directiveStart;e<n.directiveEnd;e++){let i=t.data[e];if(!(n.directiveToIndex&&!n.directiveToIndex.has(i.type))){if(q_(i,`value`)){n.flags|=1024,n.customControlIndex=e;return}if(q_(i,`checked`)){n.flags|=2048,n.customControlIndex=e;return}}}if(n.hostDirectiveInputs!==null&&n.hostDirectiveOutputs!==null&&n.directiveToIndex!==null){let e=(i,a)=>{let o=n.hostDirectiveInputs[i],r=n.hostDirectiveOutputs[i+`Change`];if(!o||!r)return!1;for(let s=0;s<o.length;s+=2){let l=o[s];for(let h=0;h<r.length;h+=2)if(l===r[h])for(let g of n.directiveToIndex.values()){if(!Array.isArray(g))continue;let[_,b,y]=g;if(l>=b&&l<=y)return n.flags|=a,n.customControlIndex=_,!0}}return!1};if(e(`value`,1024)||e(`checked`,2048))return}}function q_(t,n){return rM(t,n)&&sM(t,n+`Change`)}function rM(t,n){return n in t.inputs}function sM(t,n){return n in t.outputs}var $h=Symbol(`BINDING`);var yo=new N(``);function Pd(t,n,e){let i=e?t.styles:null,a=e?t.classes:null,o=0;if(n!==null)for(let r=0;r<n.length;r++){let s=n[r];if(typeof s==`number`)o=s;else if(o==1)a=Kc(a,s);else if(o==2){let l=s,h=n[++r];i=Kc(i,l+`: `+h+`;`)}}e?t.styles=i:t.stylesWithoutHost=i,e?t.classes=a:t.classesWithoutHost=a}function He(t,n=0){let e=ce();if(e===null)return Se(t,n);return wy(Tt(),e,Lt(t),n)}function Bf(){throw new Error(`invalid`)}function T0(t,n,e,i,a){let o=i===null?null:{"":-1},r=a(t,e);if(r!==null){let s=r,l=null,h=null;for(let p of r)if(p.resolveHostDirectives!==null){[s,l,h]=p.resolveHostDirectives(r);break}dM(t,n,e,s,o,l,h)}o!==null&&i!==null&&lM(e,i,o)}function lM(t,n,e){let i=t.localNames=[];for(let a=0;a<n.length;a+=2){let o=e[n[a+1]];if(o==null)throw new q(-301,!1);i.push(n[a],o)}}function cM(t,n,e){n.componentOffset=e,(t.components??=[]).push(n.index)}function dM(t,n,e,i,a,o,r){let s=i.length,l=null;for(let _=0;_<s;_++){let b=i[_];l===null&&Xn(b)&&(l=b,cM(t,e,_)),Ih(Td(e,n),t,b.type)}gM(e,t.data.length,s),l?.viewProvidersResolver&&l.viewProvidersResolver(l);for(let _=0;_<s;_++){let b=i[_];b.providersResolver&&b.providersResolver(b)}let h=!1,p=!1,g=r0(t,n,s,null);s>0&&(e.directiveToIndex=new Map);for(let _=0;_<s;_++){let b=i[_];if(e.mergedAttrs=gr(e.mergedAttrs,b.hostAttrs),mM(t,e,n,g,b),fM(g,b,a),r!==null&&r.has(b)){let[I,P]=r.get(b);e.directiveToIndex.set(b.type,[g,I+e.directiveStart,P+e.directiveStart])}else(o===null||!o.has(b))&&e.directiveToIndex.set(b.type,g);b.contentQueries!==null&&(e.flags|=4),(b.hostBindings!==null||b.hostAttrs!==null||b.hostVars!==0)&&(e.flags|=64);let y=b.type.prototype;!h&&(y.ngOnChanges||y.ngOnInit||y.ngDoCheck)&&((t.preOrderHooks??=[]).push(e.index),h=!0),!p&&(y.ngOnChanges||y.ngDoCheck)&&((t.preOrderCheckHooks??=[]).push(e.index),p=!0),g++}uM(t,e,o)}function uM(t,n,e){for(let i=n.directiveStart;i<n.directiveEnd;i++){let a=t.data[i];if(e===null||!e.has(a))Y_(0,n,a,i),Y_(1,n,a,i),Q_(n,i,!1);else{let o=e.get(a);K_(0,n,o,i),K_(1,n,o,i),Q_(n,i,!0)}}}function Y_(t,n,e,i){let a=t===0?e.inputs:e.outputs;for(let o in a)if(Object.hasOwn(a,o)){let r;t===0?r=n.inputs??={}:r=n.outputs??={},r[o]??=[],r[o].push(i),N0(n,o)}}function K_(t,n,e,i){let a=t===0?e.inputs:e.outputs;for(let o in a)if(Object.hasOwn(a,o)){let r=a[o],s;t===0?s=n.hostDirectiveInputs??={}:s=n.hostDirectiveOutputs??={},s[r]??=[],s[r].push(i,o),N0(n,r)}}function N0(t,n){n===`class`?t.flags|=8:n===`style`&&(t.flags|=16)}function Q_(t,n,e){let{attrs:i,inputs:a,hostDirectiveInputs:o}=t;if(i===null||!e&&a===null||e&&o===null||xf(t)){t.initialInputs??=[],t.initialInputs.push(null);return}let r=null,s=0;for(;s<i.length;){let l=i[s];if(l===0){s+=4;continue}else if(l===5){s+=2;continue}else if(typeof l==`number`)break;if(!e&&Object.hasOwn(a,l)){let h=a[l];for(let p of h)if(p===n){r??=[],r.push(l,i[s+1]);break}}else if(e&&Object.hasOwn(o,l)){let h=o[l];for(let p=0;p<h.length;p+=2)if(h[p]===n){r??=[],r.push(h[p+1],i[s+1]);break}}s+=2}t.initialInputs??=[],t.initialInputs.push(r)}function mM(t,n,e,i,a){t.data[i]=a;let r=new go(a.factory||(a.factory=ma(a.type,!0)),Xn(a),He,null);t.blueprint[i]=r,e[i]=r,pM(t,n,i,r0(t,e,a.hostVars,pt),a)}function pM(t,n,e,i,a){let o=a.hostBindings;if(o){let r=t.hostBindingOpCodes;r===null&&(r=t.hostBindingOpCodes=[]);let s=~n.index;hM(r)!=s&&r.push(s),r.push(e,i,o)}}function hM(t){let n=t.length;for(;n>0;){let e=t[--n];if(typeof e==`number`&&e<0)return e}return 0}function fM(t,n,e){if(e){if(n.exportAs)for(let i=0;i<n.exportAs.length;i++)e[n.exportAs[i]]=t;Xn(n)&&(e[``]=t)}}function gM(t,n,e){t.flags|=1,t.directiveStart=n,t.directiveEnd=n+e,t.providerIndexes=n}function jf(t,n,e,i,a,o,r,s){let l=n[ve],h=l.consts,g=_o(l,t,e,i,Sn(h,r));return o&&T0(l,n,g,Sn(h,s),a),g.mergedAttrs=gr(g.mergedAttrs,g.attrs),g.attrs!==null&&Pd(g,g.attrs,!1),g.mergedAttrs!==null&&Pd(g,g.mergedAttrs,!0),l.queries!==null&&l.queries.elementStart(l,g),g}function zf(t,n){fy(t,n),Up(n)&&t.queries.elementEnd(n)}function bM(t,n,e,i,a,o){let r=n.consts,l=_o(n,t,e,i,Sn(r,a));if(l.mergedAttrs=gr(l.mergedAttrs,l.attrs),o!=null){let h=Sn(r,o);l.localNames=[];for(let p=0;p<h.length;p+=2)l.localNames.push(h[p],-1)}return l.attrs!==null&&Pd(l,l.attrs,!1),l.mergedAttrs!==null&&Pd(l,l.mergedAttrs,!0),n.queries!==null&&n.queries.elementStart(n,l),l}var A0=typeof ShadowRoot<`u`;var vM=typeof Document<`u`;function _M(t){return Object.keys(t).map(n=>{let[e,i,a]=t[n],o={propName:e,templateName:n,isSignal:(i&tl.SignalBased)!==0};return a&&(o.transform=a),o})}function yM(t){return Object.keys(t).map(n=>({propName:t[n],templateName:n}))}function xM(t,n,e){let i=n instanceof Mt?n:n?.injector;return i&&t.getStandaloneInjector!==null&&(i=t.getStandaloneInjector(i)||i),i?new Ad(e,i):e}function CM(t){let n=t.get(Nt,null);if(n===null)throw new q(407,!1);return{rendererFactory:n,sanitizer:t.get(I0,null),changeDetectionScheduler:t.get(gi,null),ngReflect:!1,tracingService:t.get(ii,null,{optional:!0})}}function SM(t,n,e){let i=R0(t);return Wy(n,i,i===`svg`?$p:i===`math`?Qv:e)}function wM(t){if((t&&`localName`in t&&typeof t.localName==`string`?t.localName:t?.tagName)?.toLowerCase()===`script`)throw new q(905,!1)}function R0(t){return(t.selectors[0][0]||`div`).toLowerCase()}var yr=class{componentDef;ngModule;selector;componentType;ngContentSelectors;isBoundToModule;cachedInputs=null;cachedOutputs=null;get inputs(){return this.cachedInputs??=_M(this.componentDef.inputs),this.cachedInputs}get outputs(){return this.cachedOutputs??=yM(this.componentDef.outputs),this.cachedOutputs}constructor(n,e){this.componentDef=n,this.ngModule=e,this.componentType=n.type,this.selector=GI(n.selectors),this.ngContentSelectors=n.ngContentSelectors??[],this.isBoundToModule=!!e}create(n,e,i,a,o,r,s,l){Ke(ze.DynamicComponentStart);let h=ge(null);try{let p=this.componentDef,g=xM(p,a||this.ngModule,n),_=CM(g),b=_.tracingService;return b&&b.componentCreate?b.componentCreate(k0(p),()=>this.createComponentRef(_,g,e,i,o,r,s,l)):this.createComponentRef(_,g,e,i,o,r,s,l)}finally{ge(h)}}createComponentRef(n,e,i,a,o,r,s,l){let h=this.componentDef,p=EM(a,h,r,o,l),g=n.rendererFactory.createRenderer(null,h),_=a?fk(g,a,h.encapsulation,e):SM(h,g,s??null);wM(_);let b=e.get(yo,null),y=DM(_,()=>e.get(de,null)??Ry());b&&b.addHost(y);let I=r?.some(X_)||o?.some(L=>typeof L!=`function`&&L.bindings.some(X_)),P=Tf(null,p,null,512|o0(h),null,null,n,g,e,null,Vy(_,e,!0));b&&A0&&y instanceof ShadowRoot&&ld(P,()=>{b.removeHost(y)}),P[ot]=_,pd(P);let ie=null;try{let L=jf(ot,P,2,`#host`,()=>p.directiveRegistry,!0,0);Ky(g,_,L),br(_,P),Yd(p,P,L),hf(p,L,P),zf(p,L),i!==void 0&&kM(L,this.ngContentSelectors,i),ie=Rn(L.index,P),P[yt]=ie[yt],Pf(p,P,null)}catch(L){throw ie!==null&&Mh(ie),Mh(P),L}finally{Ke(ze.DynamicComponentEnd),hd()}return new Fd(this.componentType,P,!!I)}};function EM(t,n,e,i,a){let o=t?[`ng-version`,`22.2.1`]:WI(n.selectors[0]),r=null,s=null,l=0;if(e)for(let g of e)l+=g[$h].requiredVars,g.create&&(g.targetIdx=0,(r??=[]).push(g)),g.update&&(g.targetIdx=0,(s??=[]).push(g));if(i)for(let g=0;g<i.length;g++){let _=i[g];if(typeof _!=`function`)for(let b of _.bindings){l+=b[$h].requiredVars;let y=g+1;b.create&&(b.targetIdx=y,(r??=[]).push(b)),b.update&&(b.targetIdx=y,(s??=[]).push(b))}}let h=[n];if(i)for(let g of i){let b=Ts(typeof g==`function`?g:g.type);h.push(b)}return Mf(0,null,IM(r,s),1,l,h,null,null,null,[o],null)}function DM(t,n){let e=t.getRootNode?.();return vM&&e instanceof Document?e.head:e&&A0&&e instanceof ShadowRoot?e:n().head}function IM(t,n){return!t&&!n?null:e=>{if(e&1&&t)for(let i of t)i.create();if(e&2&&n)for(let i of n)i.update()}}function X_(t){let n=t[$h].kind;return n===`input`||n===`twoWay`}var Fd=class extends D0{_rootLView;_hasInputBindings;instance;hostView;changeDetectorRef;componentType;location;previousInputValues=null;_tNode;constructor(n,e,i){super(),this._rootLView=e,this._hasInputBindings=i,this._tNode=od(e[ve],ot),this.location=wr(this._tNode,e),this.instance=Rn(this._tNode.index,e)[yt],this.hostView=this.changeDetectorRef=new Ca(e,void 0),this.componentType=n}setInput(n,e){this._hasInputBindings;let i=this._tNode;if(this.previousInputValues??=new Map,this.previousInputValues.has(n)&&Object.is(this.previousInputValues.get(n),e))return;let a=this._rootLView;Kd(i,a[ve],a,n,e);this.previousInputValues.set(n,e);Ff(Rn(i.index,a),1)}get injector(){return new Wi(this._tNode,this._rootLView)}destroy(){this.hostView.destroy()}onDestroy(n){this.hostView.onDestroy(n)}};function kM(t,n,e){let i=t.projection=[];for(let a=0;a<n.length;a++){let o=e[a];i.push(o!=null&&o.length?Array.from(o):null)}}var Bt=(()=>{class t{static __NG_ELEMENT_ID__=MM}return t})();function MM(){return O0(Tt(),ce())}var Gh=class t extends Bt{_lContainer;_hostTNode;_hostLView;constructor(n,e,i){super(),this._lContainer=n,this._hostTNode=e,this._hostLView=i}get element(){return wr(this._hostTNode,this._hostLView)}get injector(){return new Wi(this._hostTNode,this._hostLView)}get parentInjector(){let n=df(this._hostTNode,this._hostLView);if(vy(n)){let e=kd(n,this._hostLView),i=Id(n),a=e[ve].data[i+8];return new Wi(a,e)}else return new Wi(null,this._hostLView)}clear(){for(;this.length>0;)this.remove(this.length-1)}get(n){let e=Z_(this._lContainer);return e!==null&&e[n]||null}get length(){return this._lContainer.length-xt}createEmbeddedView(n,e,i){let a,o,r;typeof i==`number`?a=i:i!=null&&(a=i.index,o=i.injector,r=i.onError);let s=Nd(this._lContainer,n.ssrId),l=n.createEmbeddedViewImpl(e||{},o,s);return r&&(l._lView[lr]=r),this.insertImpl(l,a,vr(this._hostTNode,s)),l}createComponent(n,e,i,a,o,r,s){let l,h,p=e||{};l=p.index,i=p.injector,a=p.projectableNodes,o=p.environmentInjector||p.ngModuleRef,r=p.directives,s=p.bindings,h=p.onError;let g=new yr(pa(n)),_=i||this.parentInjector;if(!o&&g.ngModule==null){let L=this.parentInjector.get(Mt,null);L&&(o=L)}let b=pa(g.componentType??{}),y=Nd(this._lContainer,b?.id??null),I=y?.firstChild??null,P=g.create(_,a,I,o,r,s,this._getHostElementNamespace());return h&&(P.hostView._lView[lr]=h),this.insertImpl(P.hostView,l,vr(this._hostTNode,y)),P}_getHostElementNamespace(){if(this._hostTNode.type&2){let n=this._hostTNode.parent??this._hostLView[Ut];return n!==null&&n.type&2&&typeof n.value==`string`&&n.value.toLowerCase()===`foreignobject`?null:n?.namespace??null}return this._hostTNode.namespace}insert(n,e){return this.insertImpl(n,e,!0)}insertImpl(n,e,i){let a=n._lView;if(Zv(a)){let s=this.indexOf(n);if(s!==-1)this.detach(s);else{let l=a[Et],h=new t(l,l[Ut],l[Et]);h.detach(h.indexOf(n))}}let o=this._adjustIndex(e),r=this._lContainer;return al(r,a,o,i),n.attachToViewContainerRef(),Pp(bh(r),o,n),n}move(n,e){return this.insert(n,e)}indexOf(n){let e=Z_(this._lContainer);return e!==null?e.indexOf(n):-1}remove(n){let e=this._adjustIndex(n,-1),i=Ys(this._lContainer,e);i&&(Ns(bh(this._lContainer),e),Wd(i[ve],i))}detach(n){let e=this._adjustIndex(n,-1),i=Ys(this._lContainer,e);return i&&Ns(bh(this._lContainer),e)!=null?new Ca(i):null}_adjustIndex(n,e=0){return n??this.length+e}};function Z_(t){return t[Os]}function bh(t){return t[Os]||(t[Os]=[])}function O0(t,n){let e,i=n[t.index];return An(i)?e=i:(e=x0(i,n,null,t),n[t.index]=e,Nf(n,e)),NM(e,n,t,i),new Gh(e,t,n)}function TM(t,n){let e=t[et],i=e.createComment(``),a=Cn(n,t);return ho(e,e.parentNode(a),i,e.nextSibling(a),!1),i}var NM=OM;var AM=()=>!1;function RM(t,n,e){return AM(t,n,e)}function OM(t,n,e,i){if(t[ro])return;let a;e.type&8?a=$t(i):a=TM(n,e),t[ro]=a}var Wh=class t{queryList;matches=null;constructor(n){this.queryList=n}clone(){return new t(this.queryList)}setDirty(){this.queryList.setDirty()}};var qh=class t{queries;constructor(n=[]){this.queries=n}createEmbeddedView(n){let e=n.queries;if(e!==null){let i=n.contentQueries!==null?n.contentQueries[0]:e.length,a=[];for(let o=0;o<i;o++){let r=e.getByIndex(o),s=this.queries[r.indexInDeclarationView];a.push(s.clone())}return new t(a)}return null}insertView(n){this.dirtyQueriesWithMatches(n)}detachView(n){this.dirtyQueriesWithMatches(n)}finishViewCreation(n){this.dirtyQueriesWithMatches(n)}dirtyQueriesWithMatches(n){for(let e=0;e<this.queries.length;e++)Uf(n,e).matches!==null&&this.queries[e].setDirty()}};var Ld=class{flags;read;predicate;constructor(n,e,i=null){this.flags=e,this.read=i,typeof n==`string`?this.predicate=BM(n):this.predicate=n}};var Yh=class t{queries;constructor(n=[]){this.queries=n}elementStart(n,e){for(let i=0;i<this.queries.length;i++)this.queries[i].elementStart(n,e)}elementEnd(n){for(let e=0;e<this.queries.length;e++)this.queries[e].elementEnd(n)}embeddedTView(n){let e=null;for(let i=0;i<this.length;i++){let a=e!==null?e.length:0,o=this.getByIndex(i).embeddedTView(n,a);o&&(o.indexInDeclarationView=i,e!==null?e.push(o):e=[o])}return e!==null?new t(e):null}template(n,e){for(let i=0;i<this.queries.length;i++)this.queries[i].template(n,e)}getByIndex(n){return this.queries[n]}get length(){return this.queries.length}track(n){this.queries.push(n)}};var Kh=class t{metadata;matches=null;indexInDeclarationView=-1;crossesNgTemplate=!1;_declarationNodeIndex;_appliesToNextNode=!0;constructor(n,e=-1){this.metadata=n,this._declarationNodeIndex=e}elementStart(n,e){this.isApplyingToNode(e)&&this.matchTNode(n,e)}elementEnd(n){this._declarationNodeIndex===n.index&&(this._appliesToNextNode=!1)}template(n,e){this.elementStart(n,e)}embeddedTView(n,e){return this.isApplyingToNode(n)?(this.crossesNgTemplate=!0,this.addMatch(-n.index,e),new t(this.metadata)):null}isApplyingToNode(n){if(this._appliesToNextNode&&(this.metadata.flags&1)!==1){let e=this._declarationNodeIndex,i=n.parent;for(;i!==null&&i.type&8&&i.index!==e;)i=i.parent;return e===(i!==null?i.index:-1)}return this._appliesToNextNode}matchTNode(n,e){let i=this.metadata.predicate;if(Array.isArray(i))for(let a=0;a<i.length;a++){let o=i[a];this.matchTNodeWithReadOption(n,e,PM(e,o)),this.matchTNodeWithReadOption(n,e,wd(e,n,o,!1,!1))}else i===Vt?e.type&4&&this.matchTNodeWithReadOption(n,e,-1):this.matchTNodeWithReadOption(n,e,wd(e,n,i,!1,!1))}matchTNodeWithReadOption(n,e,i){if(i!==null){let a=this.metadata.read;if(a!==null)if(a===Y||a===Bt||a===se||a===Vt&&e.type&4)this.addMatch(e.index,-2);else{let o=wd(e,n,a,!1,!1);o!==null&&this.addMatch(e.index,o)}else this.addMatch(e.index,i)}}addMatch(n,e){this.matches===null?this.matches=[n,e]:this.matches.push(n,e)}};function PM(t,n){let e=t.localNames;if(e!==null){for(let i=0;i<e.length;i+=2)if(e[i]===n)return e[i+1]}return null}function FM(t,n){return t.type&11?wr(t,n):t.type&4?Qd(t,n):null}function LM(t,n,e,i){return e===-1?FM(n,t):e===-2?VM(t,n,i):Gs(t,t[ve],e,n)}function VM(t,n,e){if(e===Y)return wr(n,t);if(e===Vt)return Qd(n,t);if(e===Bt)return O0(n,t);if(e===se)return new Wi(n,t)}function P0(t,n,e,i){let a=n[vi].queries[i];if(a.matches===null){let o=t.data,r=e.matches,s=[];for(let l=0;r!==null&&l<r.length;l+=2){let h=r[l];if(h<0)s.push(null);else{let p=o[h];s.push(LM(n,p,r[l+1],e.metadata.read))}}a.matches=s}return a.matches}function Qh(t,n,e,i){let a=t.queries.getByIndex(e),o=a.matches;if(o!==null){let r=P0(t,n,a,e);for(let s=0;s<o.length;s+=2){let l=o[s];if(l>0)i.push(r[s/2]);else{let h=o[s+1],p=n[-l];for(let g=xt;g<p.length;g++){let _=p[g];_[ga]===_[Et]&&Qh(_[ve],_,h,i)}if(p[so]!==null){let g=p[so];for(let _=0;_<g.length;_++){let b=g[_];Qh(b[ve],b,h,i)}}}}}return i}function Hf(t,n){return t[vi].queries[n].queryList}function F0(t,n,e){let i=new wn((e&4)===4);return t_(t,n,i,i.destroy),(n[vi]??=new qh).queries.push(new Wh(i))-1}function L0(t,n,e){let i=Xe();return i.firstCreatePass&&(B0(i,new Ld(t,n,e),-1),(n&2)===2&&(i.staticViewQueries=!0)),F0(i,ce(),n)}function V0(t,n,e,i){let a=Xe();if(a.firstCreatePass){let o=Tt();B0(a,new Ld(n,e,i),o.index),jM(a,t),(e&2)===2&&(a.staticContentQueries=!0)}return F0(a,ce(),e)}function BM(t){return t.split(`,`).map(n=>n.trim())}function B0(t,n,e){t.queries===null&&(t.queries=new Yh),t.queries.track(new Kh(n,e))}function jM(t,n){let e=t.contentQueries||(t.contentQueries=[]);n!==(e.length?e[e.length-1]:-1)&&e.push(t.queries.length-1,n)}function Uf(t,n){return t.queries.getByIndex(n)}function j0(t,n){let e=t[ve],i=Uf(e,n);return i.crossesNgTemplate?Qh(e,t,n,[]):P0(e,t,i,n)}function z0(t,n,e){let i,a=gs(()=>{i._dirtyCounter();let o=zM(i,t);if(n&&o===void 0)throw new q(-951,!1);return o});return i=a[St],i._dirtyCounter=k(0),i._flatValue=void 0,a}function $f(t){return z0(!0,!1,t)}function Gf(t){return z0(!0,!0,t)}function H0(t,n){let e=t[St];e._lView=ce(),e._queryIndex=n,e._queryList=Hf(e._lView,n),e._queryList.onDirty(()=>e._dirtyCounter.update(i=>i+1))}function zM(t,n){let e=t._lView,i=t._queryIndex;if(e===void 0||i===void 0||e[xe]&4)return n?void 0:Kt;let a=Hf(e,i),o=j0(e,i);return a.reset(o,Iy),n?a.first:a._changesDetected||t._flatValue===void 0?t._flatValue=a.toArray():t._flatValue}function xo(t){return!!t&&typeof t.then==`function`}function Wf(t){return!!t&&typeof t.subscribe==`function`}var ni=class{};function qf(t,n){return new Vd(t,n??null,[])}var Vd=class extends ni{ngModuleType;_parent;_bootstrapComponents=[];_r3Injector;instance;destroyCbs=[];constructor(n,e,i,a=!0){super(),this.ngModuleType=n,this._parent=e;let o=Rv(n);this._bootstrapComponents=OI(o.bootstrap),this._r3Injector=lh(n,e,[{provide:ni,useValue:this},...i],Is(n),new Set([`environment`])),a&&this.resolveInjectorInitializers()}resolveInjectorInitializers(){this._r3Injector.resolveInjectorInitializers(),this.instance=this._r3Injector.get(this.ngModuleType)}get injector(){return this._r3Injector}destroy(){let n=this._r3Injector;!n.destroyed&&n.destroy(),this.destroyCbs.forEach(e=>e()),this.destroyCbs=null}onDestroy(n){this.destroyCbs.push(n)}};var Qs=class extends ni{injector;instance=null;constructor(n){super();let e=new eo([...n.providers,{provide:ni,useValue:this}],n.parent||ar(),n.debugName,new Set([`environment`]));this.injector=e,n.runEnvironmentInitializers&&e.resolveInjectorInitializers()}destroy(){this.injector.destroy()}onDestroy(n){this.injector.onDestroy(n)}};function U0(t,n,e=null){return new Qs({providers:t,parent:n,debugName:e,runEnvironmentInitializers:!0}).injector}var HM=(()=>{class t{_injector;cachedInjectors=new Map;constructor(e){this._injector=e}getOrCreateStandaloneInjector(e){if(!e.standalone)return null;if(!this.cachedInjectors.has(e)){let i=Lp(!1,e.type),a=i.length>0?U0([i],this._injector,``):null;this.cachedInjectors.set(e,a)}return this.cachedInjectors.get(e)}ngOnDestroy(){try{for(let e of this.cachedInjectors.values())e!==null&&e.destroy()}finally{this.cachedInjectors.clear()}}static ɵprov=be({token:t,providedIn:`environment`,factory:()=>new t(Se(Mt))})}return t})();function H(t){return Zs(()=>{let n=$0(t),e=s(r({},n),{decls:t.decls,vars:t.vars,template:t.template,consts:t.consts||null,ngContentSelectors:t.ngContentSelectors,onPush:t.changeDetection!==mf.Eager,directiveDefs:null,pipeDefs:null,dependencies:n.standalone&&t.dependencies||null,getStandaloneInjector:n.standalone?a=>a.get(HM).getOrCreateStandaloneInjector(e):null,getExternalStyles:null,signals:t.signals??!1,data:t.data||{},encapsulation:t.encapsulation||ei.Emulated,styles:t.styles||Kt,_:null,schemas:t.schemas||null,tView:null,id:``});n.standalone&&Ci(`NgStandalone`),G0(e);let i=t.dependencies;return e.directiveDefs=J_(i,UM),e.pipeDefs=J_(i,Ov),e.id=WM(e),e})}function UM(t){return pa(t)||Ts(t)}function re(t){return Zs(()=>({type:t.type,bootstrap:t.bootstrap||Kt,declarations:t.declarations||Kt,imports:t.imports||Kt,exports:t.exports||Kt,transitiveCompileScopes:null,schemas:t.schemas||null,id:t.id||null}))}function $M(t,n){if(t==null)return ha;let e={};for(let i in t)if(Object.hasOwn(t,i)){let a=t[i],o,r,s,l;Array.isArray(a)?(s=a[0],o=a[1],r=a[2]??o,l=a[3]||null):(o=a,r=a,s=tl.None,l=null),e[o]=[i,s,l],n[o]=r}return e}function GM(t){if(t==null)return ha;let n={};for(let e in t)Object.hasOwn(t,e)&&(n[t[e]]=e);return n}function Q(t){return Zs(()=>{let n=$0(t);return G0(n),n})}function ol(t){return{type:t.type,name:t.name,factory:null,pure:t.pure!==!1,standalone:t.standalone??!0,onDestroy:t.type.prototype.ngOnDestroy||null}}function $0(t){let n={};return{type:t.type,providersResolver:null,viewProvidersResolver:null,factory:null,hostBindings:t.hostBindings||null,hostVars:t.hostVars||0,hostAttrs:t.hostAttrs||null,contentQueries:t.contentQueries||null,declaredInputs:n,inputConfig:t.inputs||ha,exportAs:t.exportAs||null,standalone:t.standalone??!0,signals:t.signals===!0,selectors:t.selectors||Kt,viewQuery:t.viewQuery||null,features:t.features||null,setInput:null,resolveHostDirectives:null,hostDirectives:null,controlDef:null,signalFormsInputPresence:null,inputs:$M(t.inputs,n),outputs:GM(t.outputs),debugInfo:null}}function G0(t){t.features?.forEach(n=>n(t))}function J_(t,n){return t?()=>{let e=typeof t==`function`?t():t,i=[];for(let a of e){let o=n(a);o!==null&&i.push(o)}return i}:null}function WM(t){let n=0,e=typeof t.consts==`function`?``:t.consts,i=[t.selectors,t.ngContentSelectors,t.hostVars,t.hostAttrs,e,t.vars,t.decls,t.encapsulation,t.standalone,t.signals,t.exportAs,JSON.stringify(t.inputs),JSON.stringify(t.outputs),Object.getOwnPropertyNames(t.type.prototype),!!t.contentQueries,!!t.viewQuery];for(let o of i.join(`|`))n=Math.imul(31,n)+o.charCodeAt(0)<<0;return n+=2147483648,`c`+n}var W0=new N(``);var Yf=(()=>{class t{resolve;reject;initialized=!1;done=!1;donePromise=new Promise((e,i)=>{this.resolve=e,this.reject=i});appInits=f(W0,{optional:!0})??[];injector=f(se);constructor(){}runInitializers(){if(this.initialized)return;let e=[];for(let a of this.appInits){let o=or(this.injector,a);if(xo(o))e.push(o);else if(Wf(o)){let r=new Promise((s,l)=>{o.subscribe({complete:s,error:l})});e.push(r)}}let i=()=>{this.done=!0,this.resolve()};Promise.all(e).then(()=>{i()}).catch(a=>{this.reject(a)}),e.length===0&&i(),this.initialized=!0}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function Kf(t){return n=>{n.controlDef={create:(e,i)=>{e?.ɵngControlCreate(i)},update:(e,i)=>{e?.ɵngControlUpdate?.(i)},passThroughInput:t}}}function rl(t){let n=e=>{let i=Array.isArray(t);e.hostDirectives===null?(e.resolveHostDirectives=qM,e.hostDirectives=i?t.map(Xh):[t]):i?e.hostDirectives.unshift(...t.map(Xh)):e.hostDirectives.unshift(t)};return n.ngInherit=!0,n}function qM(t){let n=[],e=!1,i=null,a=null;for(let o=0;o<t.length;o++){let r=t[o];if(r.hostDirectives!==null){let s=n.length;i??=new Map,a??=new Map,q0(r,n,i,t),a.set(r,[s,n.length-1])}o===0&&Xn(r)&&(e=!0,n.push(r))}for(let o=e?1:0;o<t.length;o++)n.push(t[o]);return i!==null&&i.forEach((o,r)=>{YM(r.declaredInputs,o.inputs)}),[n,i,a]}function q0(t,n,e,i){if(t.hostDirectives!==null)for(let a of t.hostDirectives)if(typeof a==`function`){let o=a();for(let r of o)ey(Xh(r),n,e,i)}else ey(a,n,e,i)}function ey(t,n,e,i){let a=Ts(t.directive);if(q0(a,n,e,i),e.has(a)){let o=e.get(a);ty(o,t.inputs,`input`),ty(o,t.outputs,`output`)}else i.includes(a)||(e.set(a,t),n.push(a))}function ty(t,n,e){let i=e===`input`?t.inputs:t.outputs;Object.keys(n).forEach(a=>{let o=n[a];(!Object.hasOwn(i,a)||i[a]===o)&&(i[a]=o)})}function Xh(t){return typeof t==`function`?{directive:Lt(t),inputs:{},outputs:{}}:{directive:Lt(t.directive),inputs:ny(t.inputs),outputs:ny(t.outputs)}}function ny(t){let n={};if(t!==void 0&&t.length>0)for(let e=0;e<t.length;e+=2)n[t[e]]=t[e+1];return n}function YM(t,n){for(let e in n)if(Object.hasOwn(n,e)){let i=n[e];t[i]=t[e]}}function KM(t){return Object.getPrototypeOf(t.prototype).constructor}function Ve(t){let n=KM(t.type),e=!0,i=[t];for(;n&&n!==Function.prototype&&n!==Object.prototype;){let a,o=Object.hasOwn(n,ks)?n[ks]:void 0,r=Object.hasOwn(n,Ms)?n[Ms]:void 0;if(Xn(t))a=o??r;else{if(o)throw new q(903,!1);a=r}if(a){if(e){i.push(a);let l=t;l.inputs=vh(t.inputs),l.declaredInputs=vh(t.declaredInputs),l.outputs=vh(t.outputs);let h=a.hostBindings;h&&e1(t,h);let p=a.viewQuery,g=a.contentQueries;if(p&&ZM(t,p),g&&JM(t,g),QM(t,a),Av(t.outputs,a.outputs),Xn(a)&&a.data.animation){let _=t.data;_.animation=(_.animation||[]).concat(a.data.animation)}}let s=a.features;if(s)for(let l=0;l<s.length;l++){let h=s[l];h&&h.ngInherit&&h(t),h===Ve&&(e=!1)}}n=Object.getPrototypeOf(n)}XM(i)}function QM(t,n){for(let e in n.inputs){if(!Object.hasOwn(n.inputs,e)||Object.hasOwn(t.inputs,e))continue;let i=n.inputs[e];i!==void 0&&(t.inputs[e]=i,t.declaredInputs[e]=n.declaredInputs[e])}}function XM(t){let n=0,e=null;for(let i=t.length-1;i>=0;i--){let a=t[i];a.hostVars=n+=a.hostVars,a.hostAttrs=gr(a.hostAttrs,e=gr(e,a.hostAttrs))}}function vh(t){return t===ha?{}:t===Kt?[]:t}function ZM(t,n){let e=t.viewQuery;e?t.viewQuery=(i,a)=>{n(i,a),e(i,a)}:t.viewQuery=n}function JM(t,n){let e=t.contentQueries;e?t.contentQueries=(i,a,o)=>{n(i,a,o),e(i,a,o)}:t.contentQueries=n}function e1(t,n){let e=t.hostBindings;e?t.hostBindings=(i,a)=>{n(i,a),e(i,a)}:t.hostBindings=n}function Y0(t,n,e,i,a,o,r,s){if(e.firstCreatePass){t.mergedAttrs=gr(t.mergedAttrs,t.attrs);let p=t.tView=Mf(2,t,a,o,r,e.directiveRegistry,e.pipeRegistry,null,e.schemas,e.consts,null);e.queries!==null&&(e.queries.template(e,t),p.queries=e.queries.embeddedTView(t))}s&&(t.flags|=s),uo(t,!1);let l=n1(e,n,t,i);fd()&&If(e,n,l,t),br(l,n);let h=x0(l,n,l,t);n[i+ot]=h,Nf(n,h),RM(h,t,n)}function t1(t,n,e,i,a,o,r,s,l,h,p){let g=e+ot,_;return n.firstCreatePass?(_=_o(n,g,4,r||null,s||null),cd()&&T0(n,t,_,Sn(n.consts,h),Af),fy(n,_)):_=n.data[g],Y0(_,t,n,e,i,a,o,l),cr(_)&&Yd(n,t,_),h!=null&&nl(t,_,p),_}function xr(t,n,e,i,a,o,r,s,l,h,p){let g=e+ot,_;if(n.firstCreatePass){if(_=_o(n,g,4,r||null,s||null),h!=null){let b=Sn(n.consts,h);_.localNames=[];for(let y=0;y<b.length;y+=2)_.localNames.push(b[y],-1)}}else _=n.data[g];return Y0(_,t,n,e,i,a,o,l),h!=null&&nl(t,_,p),_}function rt(t,n,e,i,a,o,r,s){let l=ce(),h=Xe();return t1(l,h,t,n,e,i,a,Sn(h.consts,o),void 0,r,s),rt}function Ir(t,n,e,i,a,o,r,s){let l=ce(),h=Xe();return xr(l,h,t,n,e,i,a,Sn(h.consts,o),void 0,r,s),Ir}var n1=i1;function i1(t,n,e,i){return Bs(!0),n[et].createComment(``)}var Qf=new N(``);var Xf=new N(``);function K0(){Qm(t=>{throw new q(-600,``)})}var a1=10;var On=(()=>{class t{_runningTick=!1;_destroyed=!1;_destroyListeners=[];_views=[];internalErrorHandler=f(Ui);afterRenderManager=f(Gd);zonelessEnabled=f(zs);rootEffectScheduler=f(vd);dirtyFlags=0;tracingSnapshot=null;allTestViews=new Set;autoDetectTestViews=new Set;includeAllTestViews=!1;afterTick=new V;get allViews(){return[...(this.includeAllTestViews?this.allTestViews:this.autoDetectTestViews).keys(),...this._views]}get destroyed(){return this._destroyed}componentTypes=[];components=[];internalPendingTask=f(po);get isStable(){return this.internalPendingTask.hasPendingTasksObservable.pipe(st(e=>!e))}constructor(){f(ii,{optional:!0})}whenStable(){let e;return new Promise(i=>{e=this.isStable.subscribe({next:a=>{a&&i()}})}).finally(()=>{e.unsubscribe()})}_injector=f(Mt);_rendererFactory=null;get injector(){return this._injector}bootstrap(e,i){return this.bootstrapImpl(e,i)}bootstrapImpl(e,i,a=se.NULL){return this._injector.get(Z).run(()=>{if(Ke(ze.BootstrapComponentStart),!this._injector.get(Yf).done)throw new q(405,``);let s=pa(e),l=this._injector.get(ni),h=new yr(s,l);this.componentTypes.push(e);let{hostElement:p,directives:g,bindings:_}=o1(i),b=p||h.selector,y=h.create(a,[],b,l.injector,g,_),I=y.location.nativeElement,P=y.injector.get(Qf,null);return P?.registerApplication(I),y.onDestroy(()=>{this.detachView(y.hostView),$s(this.components,y),P?.unregisterApplication(I)}),this._loadComponent(y),Ke(ze.BootstrapComponentEnd,y),y})}tick(){this.zonelessEnabled||(this.dirtyFlags|=1),this._tick()}_tick(){Ke(ze.ChangeDetectionStart),this.tracingSnapshot!==null?this.tracingSnapshot.run($d.CHANGE_DETECTION,this.tickImpl):this.tickImpl()}tickImpl=()=>{if(this._runningTick)throw Ke(ze.ChangeDetectionEnd),new q(101,!1);let e=ge(null);try{this._runningTick=!0,this.synchronize()}finally{this._runningTick=!1,this.tracingSnapshot?.dispose(),this.tracingSnapshot=null,ge(e),this.afterTick.next(),Ke(ze.ChangeDetectionEnd)}};synchronize(){this._rendererFactory===null&&!this._injector.destroyed&&(this._rendererFactory=this._injector.get(Nt,null,{optional:!0}));let e=0;for(;this.dirtyFlags!==0&&e++<a1;){Ke(ze.ChangeDetectionSyncStart);try{this.synchronizeOnce()}finally{Ke(ze.ChangeDetectionSyncEnd)}}}synchronizeOnce(){this.dirtyFlags&16&&(this.dirtyFlags&=-17,this.rootEffectScheduler.flush());let e=!1;if(this.dirtyFlags&7){let i=!!(this.dirtyFlags&1);this.dirtyFlags&=-8,this.dirtyFlags|=8;for(let{_lView:a}of this.allViews){if(!i&&!Ls(a))continue;b0(a,i&&!this.zonelessEnabled?0:1),e=!0}if(this.dirtyFlags&=-5,this.syncDirtyFlagsWithViews(),this.dirtyFlags&23)return}e||(this._rendererFactory?.begin?.(),this._rendererFactory?.end?.()),this.dirtyFlags&8&&(this.dirtyFlags&=-9,this.afterRenderManager.execute()),this.syncDirtyFlagsWithViews()}syncDirtyFlagsWithViews(){if(this.allViews.some(({_lView:e})=>Ls(e))){this.dirtyFlags|=2;return}else this.dirtyFlags&=-8}attachView(e){let i=e;this._views.push(i),i.attachToAppRef(this)}detachView(e){let i=e;$s(this._views,i),i.detachFromAppRef()}_loadComponent(e){this.attachView(e.hostView);try{this.tick()}catch(a){this.internalErrorHandler(a)}this.components.push(e),this._injector.get(Xf,[]).forEach(a=>a(e))}ngOnDestroy(){if(!this._destroyed)try{this._destroyListeners.forEach(e=>e()),this._views.slice().forEach(e=>e.destroy())}finally{this._destroyed=!0,this._views=[],this._destroyListeners=[]}}onDestroy(e){return this._destroyListeners.push(e),()=>$s(this._destroyListeners,e)}destroy(){if(this._destroyed)throw new q(406,!1);let e=this._injector;e.destroy&&!e.destroyed&&e.destroy()}get viewCount(){return this._views.length}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function o1(t){return t===void 0||typeof t==`string`||t instanceof Element?{hostElement:t}:t}function $s(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}function kr(t,n){let e=ce();if(fn(e,ba(),n)){let a=Xe(),o=ur();if(Kd(o,a,e,t,n))_i(o)&&d0(e,o.index);else{let s=Cn(o,e);u0(e[et],s,null,o.value,t,n,null)}}return kr}function ne(t,n,e,i){let a=ce();if(fn(a,ba(),n)){Xe();Sk(ur(),a,t,n,e,i)}return ne}var Zh=class{destroy(n){}updateValue(n,e){}swap(n,e){let i=Math.min(n,e),a=Math.max(n,e),o=this.detach(a);if(a-i>1){let r=this.detach(i);this.attach(i,o),this.attach(a,r)}else this.attach(i,o)}move(n,e){this.attach(e,this.detach(n))}};function _h(t,n,e,i,a){return t===e&&Object.is(n,i)?1:Object.is(a(t,n),a(e,i))?-1:0}function r1(t,n,e,i){let a,o,r=0,s=t.length-1;if(Array.isArray(n)){ge(i);let h=n.length-1;for(ge(null);r<=s&&r<=h;){let p=t.at(r),g=n[r],_=_h(r,p,r,g,e);if(_!==0){_<0&&t.updateValue(r,g),r++;continue}let b=t.at(s),y=n[h],I=_h(s,b,h,y,e);if(I!==0){I<0&&t.updateValue(s,y),s--,h--;continue}let P=e(r,p),ie=e(s,b),L=e(r,g);if(Object.is(L,ie)){let x=e(h,y);Object.is(x,P)?(t.swap(r,s),t.updateValue(s,y),h--,s--):t.move(s,r),t.updateValue(r,g),r++;continue}if(a??=new Bd,o??=ay(t,r,s,e),Jh(t,a,r,L))t.updateValue(r,g),r++,s++;else if(o.has(L))a.set(P,t.detach(r)),s--;else{let x=t.create(r,n[r]);t.attach(r,x),r++,s++}}for(;r<=h;)iy(t,a,e,r,n[r]),r++}else if(n!=null){ge(i);let h=n[Symbol.iterator]();ge(null);let p=h.next();for(;!p.done&&r<=s;){let g=t.at(r),_=p.value,b=_h(r,g,r,_,e);if(b!==0)b<0&&t.updateValue(r,_),r++,p=h.next();else{a??=new Bd,o??=ay(t,r,s,e);let y=e(r,_);if(Jh(t,a,r,y))t.updateValue(r,_),r++,s++,p=h.next();else if(!o.has(y))t.attach(r,t.create(r,_)),r++,s++,p=h.next();else{let I=e(r,g);a.set(I,t.detach(r)),s--}}}for(;!p.done;)iy(t,a,e,t.length,p.value),p=h.next()}for(;r<=s;)t.destroy(t.detach(s--));a?.forEach(h=>{t.destroy(h)})}function Jh(t,n,e,i){return n!==void 0&&n.has(i)?(t.attach(e,n.get(i)),n.delete(i),!0):!1}function iy(t,n,e,i,a){if(Jh(t,n,i,e(i,a)))t.updateValue(i,a);else{let o=t.create(i,a);t.attach(i,o)}}function ay(t,n,e,i){let a=new Set;for(let o=n;o<=e;o++)a.add(i(o,t.at(o)));return a}var Bd=class{kvMap=new Map;_vMap=void 0;has(n){return this.kvMap.has(n)}delete(n){if(!this.has(n))return!1;let e=this.kvMap.get(n);return this._vMap!==void 0&&this._vMap.has(e)?(this.kvMap.set(n,this._vMap.get(e)),this._vMap.delete(e)):this.kvMap.delete(n),!0}get(n){return this.kvMap.get(n)}set(n,e){if(this.kvMap.has(n)){let i=this.kvMap.get(n);this._vMap===void 0&&(this._vMap=new Map);let a=this._vMap;for(;a.has(i);)i=a.get(i);a.set(i,e)}else this.kvMap.set(n,e)}forEach(n){for(let[e,i]of this.kvMap)if(n(i,e),this._vMap!==void 0){let a=this._vMap;for(;a.has(i);)i=a.get(i),n(i,e)}}};function E(t,n,e,i,a,o,r,s){Ci(`NgControlFlow`);let l=ce(),h=Xe();return xr(l,h,t,n,e,i,a,Sn(h.consts,o),256,r,s),Zf}function Zf(t,n,e,i,a,o,r,s){return Ci(`NgControlFlow`),s1(t,n,e,i,a,o,r,s),Zf}function s1(t,n,e,i,a,o,r,s){let l=ce(),h=Xe();xr(l,h,t,n,e,i,a,Sn(h.consts,o),512,r,s)}function D(t,n){Ci(`NgControlFlow`);let e=ce(),i=ba(),a=e[i]!==pt?e[i]:-1,o=a!==-1?jd(e,ot+a):void 0,r=0;if(fn(e,i,t)){let s=ge(null);try{if(o!==void 0&&S0(o,r),t!==-1){let l=ot+t,h=jd(e,l),p=af(e[ve],l),g=E0(h,p,e);al(h,il(e,p,n,{dehydratedView:g}),r,vr(p,g))}}finally{ge(s)}}else if(o!==void 0){let s=C0(o,r);s!==void 0&&(s[yt]=n)}}var ef=class{lContainer;$implicit;$index;constructor(n,e,i){this.lContainer=n,this.$implicit=e,this.$index=i}get $count(){return this.lContainer.length-xt}};function Co(t){return t}function Pn(t,n){return n}var tf=class{hasEmptyBlock;trackByFn;liveCollection;constructor(n,e,i){this.hasEmptyBlock=n,this.trackByFn=e,this.liveCollection=i}};function ue(t,n,e,i,a,o,r,s,l,h,p,g,_){Ci(`NgControlFlow`);let b=ce(),y=Xe(),I=l!==void 0,P=ce(),L=new tf(I,s?r.bind(P[Qt][yt]):r);P[ot+t]=L,xr(b,y,t+1,n,e,i,a,Sn(y.consts,o),256),I&&xr(b,y,t+2,l,h,p,g,Sn(y.consts,_),512)}var nf=class extends Zh{lContainer;hostLView;templateTNode;operationsCounter=void 0;needsIndexUpdate=!1;constructor(n,e,i){super(),this.lContainer=n,this.hostLView=e,this.templateTNode=i}get length(){return this.lContainer.length-xt}at(n){return this.getLView(n)[yt].$implicit}attach(n,e){let i=e[io];this.needsIndexUpdate||=n!==this.length,al(this.lContainer,e,n,vr(this.templateTNode,i)),l1(this.lContainer,n)}detach(n){return this.needsIndexUpdate||=n!==this.length-1,c1(this.lContainer,n),d1(this.lContainer,n)}create(n,e){let i=Nd(this.lContainer,this.templateTNode.tView.ssrId);return il(this.hostLView,this.templateTNode,new ef(this.lContainer,e,n),{dehydratedView:i})}destroy(n){Wd(n[ve],n)}updateValue(n,e){this.getLView(n)[yt].$implicit=e}reset(){this.needsIndexUpdate=!1}updateIndexes(){if(this.needsIndexUpdate)for(let n=0;n<this.length;n++)this.getLView(n)[yt].$index=n}getLView(n){return u1(this.lContainer,n)}};function me(t){let n=ge(null),e=Xt();try{let i=ce(),a=i[ve],o=i[e],r=e+1,s=jd(i,r);if(o.liveCollection===void 0)o.liveCollection=new nf(s,i,af(a,r));else o.liveCollection.reset();let l=o.liveCollection;if(r1(l,t,o.trackByFn,n),l.updateIndexes(),o.hasEmptyBlock){let h=ba(),p=l.length===0;if(fn(i,h,p)){let g=e+2,_=jd(i,g);if(p){let b=af(a,g),y=E0(_,b,i);al(_,il(i,b,void 0,{dehydratedView:y}),0,vr(b,y))}else a.firstUpdatePass&&qk(_),S0(_,0)}}}finally{ge(n)}}function jd(t,n){return t[n]}function l1(t,n){if(t.length<=xt)return;let i=t[xt+n];i&&JI(i)}function c1(t,n){if(t.length<=xt)return;let i=t[xt+n];i&&ZI(i)}function d1(t,n){return Ys(t,n)}function u1(t,n){return C0(t,n)}function af(t,n){return od(t,n)}function S(t,n,e){let i=ce();if(fn(i,ba(),n)){Xe();_k(ur(),i,t,n,i[et],e)}return S}function of(t,n,e,i,a){Kd(n,t,e,a?`class`:`style`,i)}function c(t,n,e,i){let a=ce(),o=a[ve],r=t+ot,s=o.firstCreatePass?jf(r,a,2,n,Af,cd(),e,i):o.data[r];if(_i(s)){let l=a[Qn].tracingService;if(l&&l.componentCreate){let h=o.data[s.directiveStart+s.componentOffset];return l.componentCreate(k0(h),()=>(oy(t,n,a,s,i),c))}}return oy(t,n,a,s,i),c}function oy(t,n,e,i,a){if(Rf(i,e,t,n,Q0),cr(i)){let o=e[ve];Yd(o,e,i),hf(o,i,e)}a!=null&&nl(e,i)}function d(){let t=Xe(),e=Of(Tt());return t.firstCreatePass&&zf(t,e),Zp(e)&&Jp(),Qp(),e.classesWithoutHost!=null&&BD(e)&&of(t,e,ce(),e.classesWithoutHost,!0),e.stylesWithoutHost!=null&&jD(e)&&of(t,e,ce(),e.stylesWithoutHost,!1),d}function U(t,n,e,i){return c(t,n,e,i),d(),U}function lt(t,n,e,i){let a=ce(),o=a[ve],r=t+ot,s=o.firstCreatePass?bM(r,o,2,n,e,i):o.data[r];return Rf(s,a,t,n,Q0),i!=null&&nl(a,s),lt}function ht(){return Zp(Of(Tt()))&&Jp(),Qp(),ht}function Wt(t,n,e,i){return lt(t,n,e,i),ht(),Wt}var Q0=(t,n,e,i,a)=>(Bs(!0),Wy(n[et],i,sh()));function Jf(t,n,e){let i=ce(),a=i[ve],o=t+ot,r=a.firstCreatePass?jf(o,i,8,`ng-container`,Af,cd(),n,e):a.data[o];if(Rf(r,i,t,`ng-container`,m1),cr(r)){let s=i[ve];Yd(s,i,r),hf(s,r,i)}return e!=null&&nl(i,r),Jf}function eg(){let t=Xe(),e=Of(Tt());return t.firstCreatePass&&zf(t,e),eg}function wa(t,n,e){return Jf(t,n,e),eg(),wa}var m1=(t,n,e,i,a)=>(Bs(!0),TI(n[et],``));function $(){return ce()}function qt(t,n,e){let i=ce();if(fn(i,ba(),n)){Xe();c0(ur(),i,t,n,i[et],e)}return qt}var Hs=void 0;function p1(t){let n=Math.floor(Math.abs(t)),e=t.toString().replace(/^[^.]*\.?/,``).length;return n===1&&e===0?1:5}var h1=[`en`,[[`a`,`p`],[`AM`,`PM`]],[[`AM`,`PM`]],[[`S`,`M`,`T`,`W`,`T`,`F`,`S`],[`Sun`,`Mon`,`Tue`,`Wed`,`Thu`,`Fri`,`Sat`],[`Sunday`,`Monday`,`Tuesday`,`Wednesday`,`Thursday`,`Friday`,`Saturday`],[`Su`,`Mo`,`Tu`,`We`,`Th`,`Fr`,`Sa`]],Hs,[[`J`,`F`,`M`,`A`,`M`,`J`,`J`,`A`,`S`,`O`,`N`,`D`],[`Jan`,`Feb`,`Mar`,`Apr`,`May`,`Jun`,`Jul`,`Aug`,`Sep`,`Oct`,`Nov`,`Dec`],[`January`,`February`,`March`,`April`,`May`,`June`,`July`,`August`,`September`,`October`,`November`,`December`]],Hs,[[`B`,`A`],[`BC`,`AD`],[`Before Christ`,`Anno Domini`]],0,[6,0],[`M/d/yy`,`MMM d, y`,`MMMM d, y`,`EEEE, MMMM d, y`],[`h:mm a`,`h:mm:ss a`,`h:mm:ss a z`,`h:mm:ss a zzzz`],[`{1}, {0}`,Hs,Hs,Hs],[`.`,`,`,`;`,`%`,`+`,`-`,`E`,`×`,`‰`,`∞`,`NaN`,`:`],[`#,##0.###`,`#,##0%`,`¤#,##0.00`,`#E0`],`USD`,`$`,`US Dollar`,{},`ltr`,p1];var yh=Object.create(null);function gn(t){let n=f1(t),e=ry(n);if(e)return e;let i=n.split(`-`)[0];if(e=ry(i),e)return e;if(i===`en`)return h1;throw new q(701,!1)}function ry(t){if(!(t in yh)){let n=bi.ng&&bi.ng.common&&bi.ng.common.locales&&bi.ng.common.locales[t];return n!==void 0&&(yh[t]=n),n}return yh[t]}var Dt={LocaleId:0,DayPeriodsFormat:1,DayPeriodsStandalone:2,DaysFormat:3,DaysStandalone:4,MonthsFormat:5,MonthsStandalone:6,Eras:7,FirstDayOfWeek:8,WeekendRange:9,DateFormat:10,TimeFormat:11,DateTimeFormat:12,NumberSymbols:13,NumberFormats:14,CurrencyCode:15,CurrencySymbol:16,CurrencyName:17,Currencies:18,Directionality:19,PluralCase:20,ExtraData:21};function f1(t){return t.toLowerCase().replace(/_/g,`-`)}var sl=`en-US`;function X0(t){typeof t==`string`&&t.toLowerCase().replace(/_/g,`-`)}function C(t,n,e){let i=ce(),a=Xe(),o=Tt();return b1(a,i,i[et],o,t,n,e),C}function Mr(t,n,e){let i=ce(),a=Xe(),o=Tt();return(o.type&3||e)&&Vf(o,a,i,e,i[et],t,n,fo(o,i,n)),Mr}function b1(t,n,e,i,a,o,r){let s=!0,l=null;if((i.type&3||r)&&(l??=fo(i,n,o),Vf(i,t,n,r,e,a,o,l)&&(s=!1)),s){let h=i.outputs?.[a],p=i.hostDirectiveOutputs?.[a];if(p&&p.length)for(let g=0;g<p.length;g+=2){let _=p[g],b=p[g+1];l??=fo(i,n,o),Rd(i,n,_,b,a,l)}if(h&&h.length)for(let g of h)l??=fo(i,n,o),Rd(i,n,g,a,a,l)}}function v(t=1){return p_(t)}function v1(t,n){let e=null,i=jI(t);for(let a=0;a<n.length;a++){let o=n[a];if(o===`*`){e=a;continue}if(i===null?Xy(t,o,!0):UI(i,o))return a}return e}function Pe(t){let n=ce()[Qt][Ut];if(!n.projection){let i=n.projection=jv(t?t.length:1,null),a=i.slice(),o=n.child;for(;o!==null;){if(o.type!==128){let r=t?v1(o,t):0;r!==null&&(a[r]?a[r].projectionNext=o:i[r]=o,a[r]=o)}o=o.next}}}function he(t,n=0,e,i,a,o){let r=ce(),s=Xe(),l=i?t+1:null;l!==null&&xr(r,s,l,i,a,o,null,e);let h=_o(s,ot+t,16,null,e||null);h.projection===null&&(h.projection=n),nh();let g=!r[io]||Xp();r[Qt][Ut].projection[h.projection]===null&&l!==null?_1(r,s,l):g&&!Hd(h)&&ck(s,r,h)}function _1(t,n,e){let i=ot+e,a=n.data[i],o=t[i],r=Nd(o,a.tView.ssrId);al(o,il(t,a,void 0,{dehydratedView:r}),0,vr(a,r))}function jt(t,n,e,i){return V0(t,n,e,i),jt}function Ze(t,n,e){return L0(t,n,e),Ze}function J(t){let n=ce(),e=Xe(),i=md();Vs(i+1);let a=Uf(e,i);if(t.dirty&&Xv(n)===((a.metadata.flags&2)===2)){if(a.matches===null)t.reset([]);else{let o=j0(n,i);t.reset(o,Iy),t.notifyOnChanges()}return!0}return!1}function ee(){return Hf(ce(),md())}function Xd(t,n,e,i,a){return H0(n,V0(t,e,i,a)),Xd}function Zd(t,n,e,i){return H0(t,L0(n,e,i)),Zd}function Jd(t=1){Vs(md()+t)}function ft(t){return Ps(ih(),ot+t)}function xd(t,n){return t<<17|n<<2}function vo(t){return t>>17&32767}function y1(t){return(t&2)==2}function x1(t,n){return t&131071|n<<17}function rf(t){return t|2}function Cr(t){return(t&131068)>>2}function xh(t,n){return t&-131069|n<<2}function C1(t){return(t&1)===1}function sf(t){return t|1}function S1(t,n,e,i,a,o){let r=o?n.classBindings:n.styleBindings,s=vo(r),l=Cr(r);t[i]=e;let h=!1,p;if(Array.isArray(e)){let g=e;p=g[1],(p===null||ir(g,p)>0)&&(h=!0)}else p=e;if(a)if(l!==0){let _=vo(t[s+1]);t[i+1]=xd(_,s),_!==0&&(t[_+1]=xh(t[_+1],i)),t[s+1]=x1(t[s+1],i)}else t[i+1]=xd(s,0),s!==0&&(t[s+1]=xh(t[s+1],i)),s=i;else t[i+1]=xd(l,0),s===0?s=i:t[l+1]=xh(t[l+1],i),l=i;h&&(t[i+1]=rf(t[i+1])),sy(t,p,i,!0),sy(t,p,i,!1),w1(n,p,t,i,o),r=xd(s,l),o?n.classBindings=r:n.styleBindings=r}function w1(t,n,e,i,a){let o=a?t.residualClasses:t.residualStyles;o!=null&&typeof n==`string`&&ir(o,n)>=0&&(e[i+1]=sf(e[i+1]))}function sy(t,n,e,i){let a=t[e+1],o=n===null,r=i?vo(a):Cr(a),s=!1;for(;r!==0&&(s===!1||o);){let l=t[r],h=t[r+1];E1(l,n)&&(s=!0,t[r+1]=i?sf(h):rf(h)),r=i?vo(h):Cr(h)}s&&(t[e+1]=i?rf(a):sf(a))}function E1(t,n){return t===null||n==null||(Array.isArray(t)?t[1]:t)===n?!0:Array.isArray(t)&&typeof n==`string`?ir(t,n)>=0:!1}var Jn={textEnd:0,key:0,keyEnd:0,value:0,valueEnd:0};function D1(t){return t.substring(Jn.key,Jn.keyEnd)}function I1(t){return k1(t),Z0(t,J0(t,0,Jn.textEnd))}function Z0(t,n){let e=Jn.textEnd;return e===n?-1:(n=Jn.keyEnd=M1(t,Jn.key=n,e),J0(t,n,e))}function k1(t){Jn.key=0,Jn.keyEnd=0,Jn.value=0,Jn.valueEnd=0,Jn.textEnd=t.length}function J0(t,n,e){for(;n<e&&t.charCodeAt(n)<=32;)n++;return n}function M1(t,n,e){for(;n<e&&t.charCodeAt(n)>32;)n++;return n}function Zt(t,n,e){return ex(t,n,e,!1),Zt}function G(t,n){return ex(t,n,null,!0),G}function tt(t){N1(L1,T1,t,!0)}function T1(t,n){for(let e=I1(n);e>=0;e=Z0(n,e))td(t,D1(n),!0)}function ex(t,n,e,i){let a=ce(),o=Xe(),r=Hi(2);if(o.firstUpdatePass&&nx(o,t,r,i),n!==pt&&fn(a,r,n)){let s=o.data[Xt()];ix(o,s,a,a[et],t,a[r+1]=B1(n,e),i,r)}}function N1(t,n,e,i){let a=Xe(),o=Hi(2);a.firstUpdatePass&&nx(a,null,o,i);let r=ce();if(e!==pt&&fn(r,o,e)){let s=a.data[Xt()];if(ax(s,i)&&!tx(a,o)){let l=i?s.classesWithoutHost:s.stylesWithoutHost;l!==null&&(e=Kc(l,e||``)),of(a,s,r,e,i)}else V1(a,s,r,r[et],r[o+1],r[o+1]=F1(t,n,e),i,o)}}function tx(t,n){return n>=t.expandoStartIndex}function nx(t,n,e,i){let a=t.data;if(a[e+1]===null){let o=a[Xt()],r=tx(t,e);ax(o,i)&&n===null&&!r&&(n=!1),n=A1(a,o,n,i),S1(a,o,n,e,r,i)}}function A1(t,n,e,i){let a=c_(t),o=i?n.residualClasses:n.residualStyles;if(a===null)(i?n.classBindings:n.styleBindings)===0&&(e=Ch(null,t,n,e,i),e=Xs(e,n.attrs,i),o=null);else{let r=n.directiveStylingLast;if(r===-1||t[r]!==a)if(e=Ch(a,t,n,e,i),o===null){let l=R1(t,n,i);l!==void 0&&Array.isArray(l)&&(l=Ch(null,t,n,l[1],i),l=Xs(l,n.attrs,i),O1(t,n,i,l))}else o=P1(t,n,i)}return o!==void 0&&(i?n.residualClasses=o:n.residualStyles=o),e}function R1(t,n,e){let i=e?n.classBindings:n.styleBindings;if(Cr(i)!==0)return t[vo(i)]}function O1(t,n,e,i){let a=e?n.classBindings:n.styleBindings;t[vo(a)]=i}function P1(t,n,e){let i,a=n.directiveEnd;for(let o=1+n.directiveStylingLast;o<a;o++){let r=t[o].hostAttrs;i=Xs(i,r,e)}return Xs(i,n.attrs,e)}function Ch(t,n,e,i,a){let o=null,r=e.directiveEnd,s=e.directiveStylingLast;for(s===-1?s=e.directiveStart:s++;s<r&&(o=n[s],i=Xs(i,o.hostAttrs,a),o!==t);)s++;return t!==null&&(e.directiveStylingLast=s),i}function Xs(t,n,e){let i=e?1:2,a=-1;if(n!==null)for(let o=0;o<n.length;o++){let r=n[o];typeof r==`number`?a=r:a===i&&(Array.isArray(t)||(t=t===void 0?[]:[``,t]),td(t,r,e?!0:n[++o]))}return t===void 0?null:t}function F1(t,n,e){if(e==null||e===``)return Kt;let i=[],a=ti(e);if(Array.isArray(a))for(let o=0;o<a.length;o++)t(i,a[o],!0);else if(a instanceof Set)for(let o of a)t(i,o,!0);else if(typeof a==`object`)for(let o in a)Object.hasOwn(a,o)&&t(i,o,a[o]);else typeof a==`string`&&n(i,a);return i}function L1(t,n,e){let i=String(n);i!==``&&!i.includes(` `)&&td(t,i,e)}function V1(t,n,e,i,a,o,r,s){a===pt&&(a=Kt);let l=0,h=0,p=0<a.length?a[0]:null,g=0<o.length?o[0]:null;for(;p!==null||g!==null;){let _=l<a.length?a[l+1]:void 0,b=h<o.length?o[h+1]:void 0,y=null,I;p===g?(l+=2,h+=2,_!==b&&(y=g,I=b)):g===null||p!==null&&p<g?(l+=2,y=p):(h+=2,y=g,I=b),y!==null&&ix(t,n,e,i,y,I,r,s),p=l<a.length?a[l]:null,g=h<o.length?o[h]:null}}function ix(t,n,e,i,a,o,r,s){if(!(n.type&3))return;let l=t.data,h=l[s+1];if(!zd(C1(h)?ly(l,n,e,a,Cr(h),r):void 0)){zd(o)||y1(h)&&(o=ly(l,null,e,a,s,r));uk(i,r,Gp(Xt(),e),a,o)}}function ly(t,n,e,i,a,o){let r=n===null,s;for(;a>0;){let l=t[a],h=Array.isArray(l),p=h?l[1]:l,g=p===null,_=e[a+1];_===pt&&(_=g?Kt:void 0);let b=g?nd(_,i):p===i?_:void 0;if(h&&!zd(b)&&(b=nd(l,i)),zd(b)&&(s=b,r))return s;let y=t[a+1];a=r?vo(y):Cr(y)}if(n!==null){let l=o?n.residualClasses:n.residualStyles;l!=null&&(s=nd(l,i))}return s}function zd(t){return t!==void 0}function B1(t,n){return t==null||t===``||(typeof n==`string`?t=ti(t)+n:typeof t==`object`&&(t=Is(ti(t)))),t}function ax(t,n){return(t.flags&(n?8:16))!==0}function u(t,n=``){let e=ce(),i=Xe(),a=t+ot,o=i.firstCreatePass?_o(i,a,1,n,null):i.data[a],r=j1(i,e,o,n);e[a]=r,fd()&&If(i,e,r,o),uo(o,!1)}var j1=(t,n,e,i)=>(Bs(!0),kI(n[et],i));function z1(t,n,e,i=``){return fn(t,ba(),e)?n+$e(e)+i:pt}function H1(t,n,e,i,a,o=``){let s=_r(t,mo(),e,a);return Hi(2),s?n+$e(e)+i+$e(a)+o:pt}function U1(t,n,e,i,a,o,r,s=``){let h=Jk(t,mo(),e,a,r);return Hi(3),h?n+$e(e)+i+$e(a)+o+$e(r)+s:pt}function $1(t,n,e,i,a,o,r,s,l,h=``){let g=Ks(t,mo(),e,a,r,l);return Hi(4),g?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(l)+h:pt}function G1(t,n,e,i,a,o,r,s,l,h,p,g=``){let _=mo(),b=Ks(t,_,e,a,r,l);return b=fn(t,_+4,p)||b,Hi(5),b?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(l)+h+$e(p)+g:pt}function W1(t,n,e,i,a,o,r,s,l,h,p,g,_,b=``){let y=mo(),I=Ks(t,y,e,a,r,l);return I=_r(t,y+4,p,_)||I,Hi(6),I?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(l)+h+$e(p)+g+$e(_)+b:pt}function q1(t,n,e,i,a,o,r,s,l,h,p,g,_,b,y,I,P,ie=``){let L=mo(),x=Ks(t,L,e,a,r,l);return x=Ks(t,L+4,p,_,y,P)||x,Hi(8),x?n+$e(e)+i+$e(a)+o+$e(r)+s+$e(l)+h+$e(p)+g+$e(_)+b+$e(y)+I+$e(P)+ie:pt}function w(t){return B(``,t),w}function B(t,n,e){let i=ce(),a=z1(i,t,n,e);return a!==pt&&So(i,Xt(),a),B}function Me(t,n,e,i,a){let o=ce(),r=H1(o,t,n,e,i,a);return r!==pt&&So(o,Xt(),r),Me}function Ot(t,n,e,i,a,o,r){let s=ce(),l=U1(s,t,n,e,i,a,o,r);return l!==pt&&So(s,Xt(),l),Ot}function Si(t,n,e,i,a,o,r,s,l){let h=ce(),p=$1(h,t,n,e,i,a,o,r,s,l);return p!==pt&&So(h,Xt(),p),Si}function ll(t,n,e,i,a,o,r,s,l,h,p){let g=ce(),_=G1(g,t,n,e,i,a,o,r,s,l,h,p);return _!==pt&&So(g,Xt(),_),ll}function eu(t,n,e,i,a,o,r,s,l,h,p,g,_){let b=ce(),y=W1(b,t,n,e,i,a,o,r,s,l,h,p,g,_);return y!==pt&&So(b,Xt(),y),eu}function tu(t,n,e,i,a,o,r,s,l,h,p,g,_,b,y,I,P){let ie=ce(),L=q1(ie,t,n,e,i,a,o,r,s,l,h,p,g,_,b,y,I,P);return L!==pt&&So(ie,Xt(),L),tu}function So(t,n,e){let i=Gp(n,t);MI(t[et],i,e)}var ox={};function Tr(t){Ci(`NgLet`);let n=Xe(),e=ce(),i=t+ot;return uo(_o(n,i,128,null,null),!1),Fs(n,e,i,ox),Tr}function cl(t){return Fs(Xe(),ce(),Xt(),t),t}function Nr(t){let e=Ps(ih(),ot+t);if(e===ox)throw new q(314,!1);return e}function cy(t,n,e){let i=Xe();i.firstCreatePass&&rx(n,i.data,i.blueprint,Xn(t),e)}function rx(t,n,e,i,a){if(t=Lt(t),Array.isArray(t))for(let o=0;o<t.length;o++)rx(t[o],n,e,i,a);else{let o=Xe(),r=ce(),s=Tt(),l=Ja(t)?t:Lt(t.provide),h=Bp(t),p=s.providerIndexes&1048575,g=s.directiveStart,_=s.providerIndexes>>20;if(Ja(t)||!t.multi){let b=new go(h,a,He,null),y=wh(l,n,a?p:p+_,g);y===-1?(Ih(Td(s,r),o,l),Sh(o,t,n.length),n.push(l),s.directiveStart++,s.directiveEnd++,a&&(s.providerIndexes+=1048576),e.push(b),r.push(b)):(e[y]=b,r[y]=b)}else{let b=wh(l,n,p+_,g),y=wh(l,n,p,p+_),I=b>=0&&e[b],P=y>=0&&e[y];if(a&&!P||!a&&!I){Ih(Td(s,r),o,l);let ie=Q1(a?K1:Y1,e.length,a,i,h,t);!a&&P&&(e[y].providerFactory=ie),Sh(o,t,n.length,0),n.push(l),s.directiveStart++,s.directiveEnd++,a&&(s.providerIndexes+=1048576),e.push(ie),r.push(ie)}else{let ie=sx(e[a?y:b],h,!a&&i);Sh(o,t,b>-1?b:y,ie)}!a&&i&&P&&e[y].componentProviders++}}}function Sh(t,n,e,i){let a=Ja(n),o=Wv(n);if(a||o){let l=(o?Lt(n.useClass):n).prototype.ngOnDestroy;if(l){let h=t.destroyHooks||(t.destroyHooks=[]);if(!a&&n.multi){let p=h.indexOf(e);p===-1?h.push(e,[i,l]):h[p+1].push(i,l)}else h.push(e,l)}}}function sx(t,n,e){return e&&t.componentProviders++,t.multi.push(n)-1}function wh(t,n,e,i){for(let a=e;a<i;a++)if(n[a]===t)return a;return-1}function Y1(t,n,e,i,a){return lf(this.multi,[])}function K1(t,n,e,i,a){let o=this.multi,r;if(this.providerFactory){let s=this.providerFactory.componentProviders,l=Gs(i,i[ve],this.providerFactory.index,a);r=l.slice(0,s),lf(o,r);for(let h=s;h<l.length;h++)r.push(l[h])}else r=[],lf(o,r);return r}function lf(t,n){for(let e=0;e<t.length;e++){let i=t[e];n.push(i())}return n}function Q1(t,n,e,i,a,o){let r=new go(t,e,He,null);return r.multi=[],r.index=n,r.componentProviders=0,sx(r,a,i&&!e),r}function Ge(t,n){return e=>{e.providersResolver=(i,a)=>cy(i,a?a(t):t,!1),n&&(e.viewProvidersResolver=(i,a)=>cy(i,a?a(n):n,!0))}}function nt(t,n){let e=dd()+t,i=ce();return i[e]===pt?Lf(i,e,n()):Zk(i,e)}function lx(t,n){let e=t[n];return e===pt?void 0:e}function X1(t,n,e,i,a,o){let r=n+e;return fn(t,r,a)?Lf(t,r+1,o?i.call(o,a):i(a)):lx(t,r+1)}function Z1(t,n,e,i,a,o,r){let s=n+e;return _r(t,s,a,o)?Lf(t,s+2,r?i.call(r,a,o):i(a,o)):lx(t,s+2)}function j(t,n){let e=Xe(),i,a=t+ot;e.firstCreatePass?(i=J1(n,e.pipeRegistry),e.data[a]=i,i.onDestroy&&(e.destroyHooks??=[]).push(a,i.onDestroy)):i=e.data[a];let o=i.factory||(i.factory=ma(i.type,!0)),s=rn(He);try{let l=Md(!1),h=o();return Md(l),Fs(e,ce(),a,h),h}finally{rn(s)}}function J1(t,n){if(n)for(let e=n.length-1;e>=0;e--){let i=n[e];if(t===i.name)return i}}function _e(t,n,e){let i=t+ot,a=ce(),o=Ps(a,i);return cx(a,i)?X1(a,dd(),n,o.transform,e,o):o.transform(e)}function ke(t,n,e,i){let a=t+ot,o=ce(),r=Ps(o,a);return cx(o,a)?Z1(o,dd(),n,r.transform,e,i,r):r.transform(e,i)}function cx(t,n){return t[ve].data[n].pure}function dl(t,n){return Qd(t,n)}var dx=(()=>{class t{applicationErrorHandler=f(Ui);appRef=f(On);taskService=f(po);ngZone=f(Z);zonelessEnabled=f(zs);tracing=f(ii,{optional:!0});zoneIsDefined=typeof Zone<`u`&&!!Zone.root.run;schedulerTickApplyArgs=[{data:{__scheduler_tick__:!0}}];subscriptions=new De;angularZoneId=this.zoneIsDefined?this.ngZone._inner?.get(Es):null;scheduleInRootZone=!this.zonelessEnabled&&this.zoneIsDefined&&(f(ph,{optional:!0})??!1);cancelScheduledCallback=null;useMicrotaskScheduler=!1;runningTick=!1;pendingRenderTaskId=null;constructor(){this.subscriptions.add(this.appRef.afterTick.subscribe(()=>{let e=this.taskService.add();if(!this.runningTick&&(this.cleanup(),!this.zonelessEnabled||this.appRef.includeAllTestViews)){this.taskService.remove(e);return}this.switchToMicrotaskScheduler(),this.taskService.remove(e)})),this.subscriptions.add(this.ngZone.onUnstable.subscribe(()=>{this.runningTick||this.cleanup()}))}switchToMicrotaskScheduler(){this.ngZone.runOutsideAngular(()=>{let e=this.taskService.add();this.useMicrotaskScheduler=!0,queueMicrotask(()=>{this.useMicrotaskScheduler=!1,this.taskService.remove(e)})})}notify(e){if(!this.zonelessEnabled&&e===5)return;switch(e){case 0:case 2:this.appRef.dirtyFlags|=2;break;case 3:case 4:case 5:case 1:this.appRef.dirtyFlags|=4;break;case 6:this.appRef.dirtyFlags|=2;break;case 12:this.appRef.dirtyFlags|=16;break;case 13:this.appRef.dirtyFlags|=2;break;case 11:break;default:this.appRef.dirtyFlags|=8}if(this.appRef.tracingSnapshot=this.tracing?.snapshot(this.appRef.tracingSnapshot)??null,!this.shouldScheduleTick())return;let i=this.useMicrotaskScheduler?b_:ch;this.pendingRenderTaskId=this.taskService.add(),this.scheduleInRootZone?this.cancelScheduledCallback=Zone.root.run(()=>i(()=>this.tick())):this.cancelScheduledCallback=this.ngZone.runOutsideAngular(()=>i(()=>this.tick()))}shouldScheduleTick(){return!(this.appRef.destroyed||this.pendingRenderTaskId!==null||this.runningTick||this.appRef._runningTick||!this.zonelessEnabled&&this.zoneIsDefined&&Zone.current.get(Es+this.angularZoneId))}tick(){if(this.runningTick||this.appRef.destroyed)return;if(this.appRef.dirtyFlags===0){this.cleanup();return}!this.zonelessEnabled&&this.appRef.dirtyFlags&7&&(this.appRef.dirtyFlags|=1);let e=this.taskService.add();try{this.ngZone.run(()=>{this.runningTick=!0,this.appRef._tick()},void 0,this.schedulerTickApplyArgs)}catch(i){this.applicationErrorHandler(i)}finally{this.taskService.remove(e),this.cleanup()}}ngOnDestroy(){this.subscriptions.unsubscribe(),this.cleanup()}cleanup(){if(this.runningTick=!1,this.cancelScheduledCallback?.(),this.cancelScheduledCallback=null,this.pendingRenderTaskId!==null){let e=this.pendingRenderTaskId;this.pendingRenderTaskId=null,this.taskService.remove(e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function tg(){return Ci(`NgZoneless`),sn([...ng(),[]])}function ng(){return[{provide:gi,useExisting:dx},{provide:Z,useClass:Ds},{provide:zs,useValue:!0}]}function eT(){return typeof $localize<`u`&&$localize.locale||sl}var Ar=new N(``,{factory:()=>f(Ar,{optional:!0,skipSelf:!0})||eT()});var ul=class{destroyed=!1;listeners=null;errorHandler=f(Tn,{optional:!0});isEmitting=!1;hasNullListeners=!1;destroyRef=f(Ae);constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.listeners=null})}subscribe(n){if(this.destroyed)throw new q(953,!1);return(this.listeners??=[]).push(n),{unsubscribe:()=>{let e=this.listeners?this.listeners.indexOf(n):-1;e>-1&&(this.isEmitting?(this.hasNullListeners=!0,this.listeners[e]=null):this.listeners.splice(e,1))}}}emit(n){if(this.destroyed){console.warn(Bi(953,!1));return}if(this.listeners===null)return;this.isEmitting=!0;let e=ge(null);try{for(let i of this.listeners)try{i!==null&&i(n)}catch(a){this.errorHandler?.handleError(a)}}finally{this.hasNullListeners&&(this.hasNullListeners=!1,this.listeners&&tT(this.listeners)),ge(e),this.isEmitting=!1}}};function tT(t){let n=t.length-1;for(;n>-1;)t[n]===null&&t.splice(n,1),n--}function K(t,n){return gs(t,n?.equal)}function it(t){return Jb(t)}(class t extends Error{_brand;constructor(n){super(n)}static IDLE=new t(`IDLE`);static LOADING=new t(`LOADING`)});function vx(t,n){let e=Object.create(cf);e.value=t,e.transformFn=n?.transform;function i(){if(Ri(e),e.value===Sr)throw new q(-950,null);return e.value}return i[St]=e,i}var Ki=class{attributeName;constructor(n){this.attributeName=n}__NG_ELEMENT_ID__=()=>uf(this.attributeName);toString(){return`HostAttributeToken ${this.attributeName}`}};var _x=(()=>{let t=new N(``);return t.__NG_ELEMENT_ID__=n=>{let e=Tt();if(e===null)throw new q(-204,!1);if(e.type&2)return e.value;if(n&8)return null;throw new q(-204,!1)},t})();function iu(t){return new ul}function mx(t,n){return vx(t,n)}function fT(t){return vx(Sr,t)}var zt=(mx.required=fT,mx);function yx(t,n){let e=Object.create(cf),i=new ul;e.value=t;function a(){return Ri(e),px(e.value),e.value}return a[St]=e,a.asReadonly=gd.bind(a),a.set=o=>{e.equal(e.value,o)||(ua(e,o),i.emit(o))},a.update=o=>{px(e.value),a.set(o(e.value))},a.subscribe=i.subscribe.bind(i),a.destroyRef=i.destroyRef,a}function px(t){if(t===Sr)throw new q(952,!1)}function hx(t,n){return yx(t,n)}function gT(t){return yx(Sr,t)}var xx=(hx.required=gT,hx);function fx(t,n){return $f(n)}function bT(t,n){return Gf(n)}var pl=(fx.required=bT,fx);function gx(t,n){return $f(n)}function vT(t,n){return Gf(n)}var Cx=(gx.required=vT,gx);var Fe=(()=>{class t{static __NG_ELEMENT_ID__=yT}return t})();function yT(t){return xT(Tt(),ce(),(t&16)===16)}function xT(t,n,e){if(_i(t)&&!e){let i=Rn(t.index,n);return new Ca(i,i)}else if(t.type&175){let i=n[Qt];return new Ca(i,n)}return null}var ag=new N(``);var CT=new N(``);function ml(t){return!t.moduleRef}function ST(t){let n=ml(t)?t.r3Injector:t.moduleRef.injector,e=n.get(Z);return e.run(()=>{ml(t)?t.r3Injector.resolveInjectorInitializers():t.moduleRef.resolveInjectorInitializers();let i=n.get(Ui),a;if(e.runOutsideAngular(()=>{a=e.onError.subscribe({next:i})}),ml(t)){let o=()=>n.destroy(),r=t.platformInjector.get(ag);r.add(o),n.onDestroy(()=>{a.unsubscribe(),r.delete(o)})}else{let o=()=>t.moduleRef.destroy(),r=t.platformInjector.get(ag);r.add(o),t.moduleRef.onDestroy(()=>{$s(t.allPlatformModules,t.moduleRef),a.unsubscribe(),r.delete(o)})}return ET(i,e,()=>{let o=n.get(po),r=o.add(),s=n.get(Yf);return s.runInitializers(),s.donePromise.then(()=>{if(X0(n.get(Ar,sl)||sl),!n.get(CT,!0))return ml(t)?n.get(On):(t.allPlatformModules.push(t.moduleRef),t.moduleRef);if(ml(t)){let p=n.get(On);return t.rootComponent!==void 0&&p.bootstrap(t.rootComponent),p}else return wT?.(t.moduleRef,t.allPlatformModules),t.moduleRef}).finally(()=>{o.remove(r)})})})}var wT;function ET(t,n,e){try{let i=e();return xo(i)?i.catch(a=>{throw n.runOutsideAngular(()=>t(a)),a}):i}catch(i){throw n.runOutsideAngular(()=>t(i)),i}}var nu=null;function DT(t=[],n){return se.create({name:n,providers:[{provide:As,useValue:`platform`},{provide:ag,useValue:new Set([()=>nu=null])},...t]})}function IT(t=[]){if(nu)return nu;let n=DT(t);return nu=n,K0(),kT(n),n}function kT(t){let n=t.get(bd,null);or(t,()=>{n?.forEach(e=>e())})}function Sx(t){let{rootComponent:n,appProviders:e,platformProviders:i,platformRef:a}=t;Ke(ze.BootstrapApplicationStart);try{let o=a?.injector??IT(i);return ST({r3Injector:new Qs({providers:[ng(),x_,...e||[]],parent:o,debugName:``,runEnvironmentInitializers:!1}).injector,platformInjector:o,rootComponent:n})}catch(o){return Promise.reject(o)}finally{Ke(ze.BootstrapApplicationEnd)}}function z(t){return typeof t==`boolean`?t:t!=null&&t!==`false`}function ct(t,n=NaN){return!isNaN(parseFloat(t))&&!isNaN(Number(t))?Number(t):n}var ig=Symbol(`NOT_SET`);var wx=new Set;var MT=s(r({},Wo),{kind:`afterRenderEffectPhase`,consumerIsAlwaysLive:!0,consumerAllowSignalWrites:!0,value:ig,cleanup:null,consumerMarkedDirty(){if(this.sequence.impl.executing){if(this.sequence.lastPhase===null||this.sequence.lastPhase<this.phase)return;this.sequence.erroredOrDestroyed=!0}this.sequence.scheduler.notify(7)},phaseFn(t){if(this.sequence.lastPhase=this.phase,!this.dirty)return this.signal;if(this.dirty=!1,this.value!==ig&&!$o(this))return this.signal;try{for(let a of this.cleanup??wx)a()}finally{this.cleanup?.clear()}let n=[];t!==void 0&&n.push(t),n.push(this.registerCleanupFn);let e=ca(this),i;try{i=this.userFn.apply(null,n)}finally{$a(this,e)}return(this.value===ig||!this.equal(this.value,i))&&(this.value=i,this.version++),this.signal}});var og=class extends Ws{scheduler;lastPhase=null;nodes=[void 0,void 0,void 0,void 0];onDestroyFns=null;constructor(n,e,i,a,o,r=null,s){super(n,[void 0,void 0,void 0,void 0],i,!1,o.get(Ae),r),this.scheduler=a;for(let l of wf){let h=e[l];if(h===void 0)continue;let p=Object.create(MT);p.sequence=this,p.phase=l,p.userFn=h,p.dirty=!0,p.signal=()=>(Ri(p),p.value),p.signal[St]=p,p.registerCleanupFn=g=>(p.cleanup??=new Set).add(g),this.nodes[l]=p,this.hooks[l]=g=>p.phaseFn(g)}}afterRun(){super.afterRun(),this.lastPhase=null}destroy(){if(this.onDestroyFns!==null)for(let n of this.onDestroyFns)n();super.destroy();for(let n of this.nodes)if(n)try{for(let e of n.cleanup??wx)e()}finally{da(n)}}};function rg(t,n){let e=n?.injector??f(se),i=e.get(gi),a=e.get(Gd),o=e.get(ii,null,{optional:!0});a.impl??=e.get(Ef);let r=t;typeof r==`function`&&(r={mixedReadWrite:t});let s=e.get(mr,null,{optional:!0}),l=new og(a.impl,[r.earlyRead,r.write,r.mixedReadWrite,r.read],s?.view,i,e,o?.snapshot(null),void 0);return a.impl.register(l),l}function au(t,n){let e=pa(t),i=n.elementInjector||ar(),o=new yr(e).create(i,n.projectableNodes,n.hostElement,n.environmentInjector,n.directives,n.bindings);return n.onError&&(o.hostView._lView[lr]=n.onError),o}var Ex=null;function Fn(){return Ex}function sg(t){Ex??=t}var hl=class{};var Rr=(()=>{class t{historyGo(e){throw new Error(``)}static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:()=>f(Dx),providedIn:`platform`})}return t})();var Dx=(()=>{class t extends Rr{_location;_history;_doc=f(de);constructor(){super(),this._location=window.location,this._history=window.history}getBaseHrefFromDOM(){return Fn().getBaseHref(this._doc)}onPopState(e){let i=Fn().getGlobalEventTarget(this._doc,`window`);return i.addEventListener(`popstate`,e,!1),()=>i.removeEventListener(`popstate`,e)}onHashChange(e){let i=Fn().getGlobalEventTarget(this._doc,`window`);return i.addEventListener(`hashchange`,e,!1),()=>i.removeEventListener(`hashchange`,e)}get href(){return this._location.href}get protocol(){return this._location.protocol}get hostname(){return this._location.hostname}get port(){return this._location.port}get pathname(){return this._location.pathname}get search(){return this._location.search}get hash(){return this._location.hash}set pathname(e){this._location.pathname=e}pushState(e,i,a){this._history.pushState(e,i,a)}replaceState(e,i,a){this._history.replaceState(e,i,a)}forward(){this._history.forward()}back(){this._history.back()}historyGo(e=0){this._history.go(e)}getState(){return this._history.state}static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:()=>new t,providedIn:`platform`})}return t})();function Mx(t,n){return t?n?t.endsWith(`/`)?n.startsWith(`/`)?t+n.slice(1):t+n:n.startsWith(`/`)?t+n:`${t}/${n}`:t:n}function Ix(t){let n=t.search(/#|\?|$/);return t[n-1]===`/`?t.slice(0,n-1)+t.slice(n):t}function Ea(t){return t&&t[0]!==`?`?`?${t}`:t}var ou=(()=>{class t{historyGo(e){throw new Error(``)}static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:()=>f(NT),providedIn:`root`})}return t})();var TT=new N(``);var NT=(()=>{class t extends ou{_platformLocation;_baseHref;_removeListenerFns=[];constructor(e,i){super(),this._platformLocation=e,this._baseHref=i??this._platformLocation.getBaseHrefFromDOM()??f(de).location?.origin??``}ngOnDestroy(){for(;this._removeListenerFns.length;)this._removeListenerFns.pop()()}onPopState(e){this._removeListenerFns.push(this._platformLocation.onPopState(e),this._platformLocation.onHashChange(e))}getBaseHref(){return this._baseHref}prepareExternalUrl(e){return Mx(this._baseHref,e)}path(e=!1){let i=this._platformLocation.pathname+Ea(this._platformLocation.search),a=this._platformLocation.hash;return a&&e?`${i}${a}`:i}pushState(e,i,a,o){let r=this.prepareExternalUrl(a+Ea(o));this._platformLocation.pushState(e,i,r)}replaceState(e,i,a,o){let r=this.prepareExternalUrl(a+Ea(o));this._platformLocation.replaceState(e,i,r)}forward(){this._platformLocation.forward()}back(){this._platformLocation.back()}getState(){return this._platformLocation.getState()}historyGo(e=0){this._platformLocation.historyGo?.(e)}static ɵfac=function(i){return new(i||t)(Se(Rr),Se(TT,8))};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();var ru=(()=>{class t{_subject=new V;_basePath;_locationStrategy;_urlChangeListeners=[];_urlChangeSubscription=null;constructor(e){this._locationStrategy=e;let i=this._locationStrategy.getBaseHref();this._basePath=OT(Ix(kx(i))),this._locationStrategy.onPopState(a=>{let o={url:this.path(!0),pop:!0,state:a.state,type:a.type};a.hasUAVisualTransition&&(o.hasUAVisualTransition=!0),this._subject.next(o)})}ngOnDestroy(){this._urlChangeSubscription?.unsubscribe(),this._urlChangeListeners=[]}path(e=!1){return this.normalize(this._locationStrategy.path(e))}getState(){return this._locationStrategy.getState()}isCurrentPathEqualTo(e,i=``){return this.path()==this.normalize(e+Ea(i))}normalize(e){return t.stripTrailingSlash(RT(this._basePath,kx(e)))}prepareExternalUrl(e){return e&&e[0]!==`/`&&(e=`/`+e),this._locationStrategy.prepareExternalUrl(e)}go(e,i=``,a=null){this._locationStrategy.pushState(a,``,e,i),this._notifyUrlChangeListeners(this.prepareExternalUrl(e+Ea(i)),a)}replaceState(e,i=``,a=null){this._locationStrategy.replaceState(a,``,e,i),this._notifyUrlChangeListeners(this.prepareExternalUrl(e+Ea(i)),a)}forward(){this._locationStrategy.forward()}back(){this._locationStrategy.back()}historyGo(e=0){this._locationStrategy.historyGo?.(e)}onUrlChange(e){return this._urlChangeListeners.push(e),this._urlChangeSubscription??=this.subscribe(i=>{this._notifyUrlChangeListeners(i.url,i.state)}),()=>{let i=this._urlChangeListeners.indexOf(e);this._urlChangeListeners.splice(i,1),this._urlChangeListeners.length===0&&(this._urlChangeSubscription?.unsubscribe(),this._urlChangeSubscription=null)}}_notifyUrlChangeListeners(e=``,i){this._urlChangeListeners.forEach(a=>a(e,i))}subscribe(e,i,a){return this._subject.subscribe({next:e,error:i??void 0,complete:a??void 0})}static normalizeQueryParams=Ea;static joinWithSlash=Mx;static stripTrailingSlash=Ix;static ɵfac=function(i){return new(i||t)(Se(ou))};static ɵprov=be({token:t,factory:()=>AT(),providedIn:`root`})}return t})();function AT(){return new ru(Se(ou))}function RT(t,n){if(!t||!n.startsWith(t))return n;let e=n.substring(t.length);return e===``||[`/`,`;`,`?`,`#`].includes(e[0])?e:n}function kx(t){return t.replace(/\/index\.html$/,``)}function OT(t){if(new RegExp(`^(https?:)?//`).test(t)){let[,e]=t.split(/\/\/[^\/]+/);return e}return t}var pg=(function(t){return t[t.Decimal=0]=`Decimal`,t[t.Percent=1]=`Percent`,t[t.Currency=2]=`Currency`,t[t.Scientific=3]=`Scientific`,t})(pg||{});var Jt=(function(t){return t[t.Format=0]=`Format`,t[t.Standalone=1]=`Standalone`,t})(Jt||{});var at=(function(t){return t[t.Narrow=0]=`Narrow`,t[t.Abbreviated=1]=`Abbreviated`,t[t.Wide=2]=`Wide`,t[t.Short=3]=`Short`,t})(at||{});var bn=(function(t){return t[t.Short=0]=`Short`,t[t.Medium=1]=`Medium`,t[t.Long=2]=`Long`,t[t.Full=3]=`Full`,t})(bn||{});var dn={Decimal:0,Group:1,List:2,PercentSign:3,PlusSign:4,MinusSign:5,Exponential:6,SuperscriptingExponent:7,PerMille:8,Infinity:9,NaN:10,TimeSeparator:11,CurrencyDecimal:12,CurrencyGroup:13};function Ax(t){return gn(t)[Dt.LocaleId]}function Rx(t,n,e){let i=gn(t);return Ln(Ln([i[Dt.DayPeriodsFormat],i[Dt.DayPeriodsStandalone]],n),e)}function Ox(t,n,e){let i=gn(t);return Ln(Ln([i[Dt.DaysFormat],i[Dt.DaysStandalone]],n),e)}function Px(t,n,e){let i=gn(t);return Ln(Ln([i[Dt.MonthsFormat],i[Dt.MonthsStandalone]],n),e)}function Fx(t,n){let i=gn(t)[Dt.Eras];return Ln(i,n)}function fl(t,n){return Ln(gn(t)[Dt.DateFormat],n)}function gl(t,n){return Ln(gn(t)[Dt.TimeFormat],n)}function bl(t,n){let i=gn(t)[Dt.DateTimeFormat];return Ln(i,n)}function wi(t,n){let e=gn(t),i=e[Dt.NumberSymbols][n];if(typeof i>`u`){if(n===dn.CurrencyDecimal)return e[Dt.NumberSymbols][dn.Decimal];if(n===dn.CurrencyGroup)return e[Dt.NumberSymbols][dn.Group]}return i}function Lx(t,n){return gn(t)[Dt.NumberFormats][n]}function Vx(t){if(!t[Dt.ExtraData])throw new q(2303,!1)}function Bx(t){let n=gn(t);return Vx(n),(n[Dt.ExtraData][2]||[]).map(i=>typeof i==`string`?lg(i):[lg(i[0]),lg(i[1])])}function jx(t,n,e){let i=gn(t);Vx(i);return Ln(Ln([i[Dt.ExtraData][0],i[Dt.ExtraData][1]],n)||[],e)||[]}function Ln(t,n){for(let e=n;e>-1;e--)if(typeof t[e]<`u`)return t[e];throw new q(2304,!1)}function lg(t){let[n,e]=t.split(`:`);return{hours:+n,minutes:+e}}var FT=/^(\d{4,})-?(\d\d)-?(\d\d)(?:T(\d\d)(?::?(\d\d)(?::?(\d\d)(?:\.(\d+))?)?)?(Z|([+-])(\d\d):?(\d\d))?)?$/;var su=Object.create(null);var LT=/((?:[^BEGHLMOSWYZabcdhmswyz']+)|(?:'(?:[^']|'')*')|(?:G{1,5}|y{1,4}|Y{1,4}|M{1,5}|L{1,5}|w{1,2}|W{1}|d{1,2}|E{1,6}|c{1,6}|a{1,5}|b{1,5}|B{1,5}|h{1,2}|H{1,2}|m{1,2}|s{1,2}|S{1,3}|z{1,4}|Z{1,5}|O{1,4}))([\s\S]*)/;var VT=256;function zx(t,n,e,i){let a=YT(t);BT(n),n=Qi(e,n)||n;let r=[],s;for(;n;)if(s=LT.exec(n),s){r=r.concat(s.slice(1));let p=r.pop();if(!p)break;n=p}else{r.push(n);break}let l=a.getTimezoneOffset();i&&(l=Ux(i,l),a=qT(a,i));let h=``;return r.forEach(p=>{let g=GT(p);h+=g?g(a,e,l):p===`''`?`'`:p.replace(/(^'|'$)/g,``).replace(/''/g,`'`)}),h}function BT(t){if(t.length>VT)throw new q(2300,!1)}function mu(t,n,e){let i=new Date(0);return i.setFullYear(t,n,e),i.setHours(0,0,0),i}function Qi(t,n){let e=Ax(t);if(su[e]??=Object.create(null),su[e][n])return su[e][n];let i=``;switch(n){case`shortDate`:i=fl(t,bn.Short);break;case`mediumDate`:i=fl(t,bn.Medium);break;case`longDate`:i=fl(t,bn.Long);break;case`fullDate`:i=fl(t,bn.Full);break;case`shortTime`:i=gl(t,bn.Short);break;case`mediumTime`:i=gl(t,bn.Medium);break;case`longTime`:i=gl(t,bn.Long);break;case`fullTime`:i=gl(t,bn.Full);break;case`short`:let a=Qi(t,`shortTime`),o=Qi(t,`shortDate`);i=lu(bl(t,bn.Short),[a,o]);break;case`medium`:let r=Qi(t,`mediumTime`),s=Qi(t,`mediumDate`);i=lu(bl(t,bn.Medium),[r,s]);break;case`long`:let l=Qi(t,`longTime`),h=Qi(t,`longDate`);i=lu(bl(t,bn.Long),[l,h]);break;case`full`:let p=Qi(t,`fullTime`),g=Qi(t,`fullDate`);i=lu(bl(t,bn.Full),[p,g])}return i&&(su[e][n]=i),i}function lu(t,n){return n&&(t=t.replace(/\{([^}]+)}/g,function(e,i){return Object.hasOwn(n,i)?n[i]:e})),t}function oi(t,n,e=`-`,i,a){let o=``;(t<0||a&&t<=0)&&(a?t=-t+1:(t=-t,o=e));let r=String(t);for(;r.length<n;)r=`0`+r;return i&&(r=r.slice(r.length-n)),o+r}function jT(t,n){return oi(t,3).substring(0,n)}function Pt(t,n,e=0,i=!1,a=!1){return function(o,r){let s=zT(t,o);if((e>0||s>-e)&&(s+=e),t===3)s===0&&e===-12&&(s=12);else if(t===6)return jT(s,n);let l=wi(r,dn.MinusSign);return oi(s,n,l,i,a)}}function zT(t,n){switch(t){case 0:return n.getFullYear();case 1:return n.getMonth();case 2:return n.getDate();case 3:return n.getHours();case 4:return n.getMinutes();case 5:return n.getSeconds();case 6:return n.getMilliseconds();case 7:return n.getDay();default:throw new q(2301,!1)}}function dt(t,n,e=Jt.Format,i=!1){return function(a,o){return HT(a,o,t,n,e,i)}}function HT(t,n,e,i,a,o){switch(e){case 2:return Px(n,a,i)[t.getMonth()];case 1:return Ox(n,a,i)[t.getDay()];case 0:let r=t.getHours(),s=t.getMinutes();if(o){let h=Bx(n),p=jx(n,a,i),g=h.findIndex(_=>{if(Array.isArray(_)){let[b,y]=_,I=r>=b.hours&&s>=b.minutes,P=r<y.hours||r===y.hours&&s<y.minutes;if(b.hours<y.hours){if(I&&P)return!0}else if(I||P)return!0}else if(_.hours===r&&_.minutes===s)return!0;return!1});if(g!==-1)return p[g]}return Rx(n,a,i)[r<12?0:1];case 3:return Fx(n,i)[t.getFullYear()<=0?0:1];default:throw new q(2302,!1)}}function cu(t){return function(n,e,i){let a=-1*i,o=wi(e,dn.MinusSign),r=a>0?Math.floor(a/60):Math.ceil(a/60);switch(t){case 0:return(a>=0?`+`:``)+oi(r,2,o)+oi(Math.abs(a%60),2,o);case 1:return`GMT`+(a>=0?`+`:``)+oi(r,1,o);case 2:return`GMT`+(a>=0?`+`:``)+oi(r,2,o)+`:`+oi(Math.abs(a%60),2,o);case 3:return i===0?`Z`:(a>=0?`+`:``)+oi(r,2,o)+`:`+oi(Math.abs(a%60),2,o);default:throw new q(2310,!1)}}}var UT=0;var uu=4;function $T(t){let n=mu(t,UT,1).getDay();return mu(t,0,1+(n<=uu?uu:uu+7)-n)}function Hx(t){let n=t.getDay(),e=n===0?-3:uu-n;return mu(t.getFullYear(),t.getMonth(),t.getDate()+e)}function cg(t,n=!1){return function(e,i){let a;if(n){let o=new Date(e.getFullYear(),e.getMonth(),1).getDay()-1,r=e.getDate();a=1+Math.floor((r+o)/7)}else{let o=Hx(e),r=$T(o.getFullYear()),s=o.getTime()-r.getTime();a=1+Math.round(s/6048e5)}return oi(a,t,wi(i,dn.MinusSign))}}function du(t,n=!1){return function(e,i){return oi(Hx(e).getFullYear(),t,wi(i,dn.MinusSign),n)}}var dg=Object.create(null);function GT(t){if(dg[t])return dg[t];let n;switch(t){case`G`:case`GG`:case`GGG`:n=dt(3,at.Abbreviated);break;case`GGGG`:n=dt(3,at.Wide);break;case`GGGGG`:n=dt(3,at.Narrow);break;case`y`:n=Pt(0,1,0,!1,!0);break;case`yy`:n=Pt(0,2,0,!0,!0);break;case`yyy`:n=Pt(0,3,0,!1,!0);break;case`yyyy`:n=Pt(0,4,0,!1,!0);break;case`Y`:n=du(1);break;case`YY`:n=du(2,!0);break;case`YYY`:n=du(3);break;case`YYYY`:n=du(4);break;case`M`:case`L`:n=Pt(1,1,1);break;case`MM`:case`LL`:n=Pt(1,2,1);break;case`MMM`:n=dt(2,at.Abbreviated);break;case`MMMM`:n=dt(2,at.Wide);break;case`MMMMM`:n=dt(2,at.Narrow);break;case`LLL`:n=dt(2,at.Abbreviated,Jt.Standalone);break;case`LLLL`:n=dt(2,at.Wide,Jt.Standalone);break;case`LLLLL`:n=dt(2,at.Narrow,Jt.Standalone);break;case`w`:n=cg(1);break;case`ww`:n=cg(2);break;case`W`:n=cg(1,!0);break;case`d`:n=Pt(2,1);break;case`dd`:n=Pt(2,2);break;case`c`:case`cc`:n=Pt(7,1);break;case`ccc`:n=dt(1,at.Abbreviated,Jt.Standalone);break;case`cccc`:n=dt(1,at.Wide,Jt.Standalone);break;case`ccccc`:n=dt(1,at.Narrow,Jt.Standalone);break;case`cccccc`:n=dt(1,at.Short,Jt.Standalone);break;case`E`:case`EE`:case`EEE`:n=dt(1,at.Abbreviated);break;case`EEEE`:n=dt(1,at.Wide);break;case`EEEEE`:n=dt(1,at.Narrow);break;case`EEEEEE`:n=dt(1,at.Short);break;case`a`:case`aa`:case`aaa`:n=dt(0,at.Abbreviated);break;case`aaaa`:n=dt(0,at.Wide);break;case`aaaaa`:n=dt(0,at.Narrow);break;case`b`:case`bb`:case`bbb`:n=dt(0,at.Abbreviated,Jt.Standalone,!0);break;case`bbbb`:n=dt(0,at.Wide,Jt.Standalone,!0);break;case`bbbbb`:n=dt(0,at.Narrow,Jt.Standalone,!0);break;case`B`:case`BB`:case`BBB`:n=dt(0,at.Abbreviated,Jt.Format,!0);break;case`BBBB`:n=dt(0,at.Wide,Jt.Format,!0);break;case`BBBBB`:n=dt(0,at.Narrow,Jt.Format,!0);break;case`h`:n=Pt(3,1,-12);break;case`hh`:n=Pt(3,2,-12);break;case`H`:n=Pt(3,1);break;case`HH`:n=Pt(3,2);break;case`m`:n=Pt(4,1);break;case`mm`:n=Pt(4,2);break;case`s`:n=Pt(5,1);break;case`ss`:n=Pt(5,2);break;case`S`:n=Pt(6,1);break;case`SS`:n=Pt(6,2);break;case`SSS`:n=Pt(6,3);break;case`Z`:case`ZZ`:case`ZZZ`:n=cu(0);break;case`ZZZZZ`:n=cu(3);break;case`O`:case`OO`:case`OOO`:case`z`:case`zz`:case`zzz`:n=cu(1);break;case`OOOO`:case`ZZZZ`:case`zzzz`:n=cu(2);break;default:return null}return dg[t]=n,n}function Ux(t,n){t=t.replace(/:/g,``);let e=Date.parse(`Jan 01, 1970 00:00:00 `+t)/6e4;return isNaN(e)?n:e}function WT(t,n){return t=new Date(t.getTime()),t.setMinutes(t.getMinutes()+n),t}function qT(t,n,e){let a=t.getTimezoneOffset();return WT(t,-1*(Ux(n,a)-a))}function YT(t){if(Tx(t))return t;if(typeof t==`number`&&!isNaN(t))return new Date(t);if(typeof t==`string`){if(t=t.trim(),/^(\d{4}(-\d{1,2}(-\d{1,2})?)?)$/.test(t)){let[a,o=1,r=1]=t.split(`-`).map(s=>+s);return mu(a,o-1,r)}let e=parseFloat(t);if(!isNaN(t-e))return new Date(e);let i;if(i=t.match(FT))return KT(i)}let n=new Date(t);if(!Tx(n))throw new q(2311,!1);return n}function KT(t){let n=new Date(0),e=0,i=0,a=t[8]?n.setUTCFullYear:n.setFullYear,o=t[8]?n.setUTCHours:n.setHours;t[9]&&(e=Number(t[9]+t[10]),i=Number(t[9]+t[11])),a.call(n,Number(t[1]),Number(t[2])-1,Number(t[3]));let r=Number(t[4]||0)-e,s=Number(t[5]||0)-i,l=Number(t[6]||0),h=Math.floor(parseFloat(`0.`+(t[7]||0))*1e3);return o.call(n,r,s,l,h),n}function Tx(t){return t instanceof Date&&!isNaN(t.valueOf())}var QT=/^(\d+)?\.((\d+)(-(\d+))?)?$/;var Nx=22;var pu=`.`;var vl=`0`;var XT=`;`;var ZT=`,`;var ug=`#`;function JT(t,n,e,i,a,o,r=!1){let s=``,l=!1;if(!isFinite(t))s=wi(e,Number.isNaN(t)?dn.NaN:dn.Infinity);else{let h=nN(t);r&&(h=tN(h));let p=n.minInt,g=n.minFrac,_=n.maxFrac;if(o){let L=o.match(QT);if(L===null)throw new q(2306,!1);let x=L[1],A=L[3],R=L[5];x!=null&&(p=mg(x)),A!=null&&(g=mg(A)),R!=null?_=mg(R):A!=null&&g>_&&(_=g);let W=100;if(p>W||g>W||_>W)throw new q(2306,!1)}iN(h,g,_);let b=h.digits,y=h.integerLen,I=h.exponent,P=[];for(l=b.every(L=>!L);y<p;y++)b.unshift(0);for(;y<0;y++)b.unshift(0);y>0?P=b.splice(y,b.length):(P=b,b=[0]);let ie=[];for(b.length>=n.lgSize&&ie.unshift(b.splice(-n.lgSize,b.length).join(``));b.length>n.gSize;)ie.unshift(b.splice(-n.gSize,b.length).join(``));b.length&&ie.unshift(b.join(``)),s=ie.join(wi(e,i)),P.length&&(s+=wi(e,a)+P.join(``)),I&&(s+=wi(e,dn.Exponential)+`+`+I)}return t<0&&!l?s=n.negPre+s+n.negSuf:s=n.posPre+s+n.posSuf,s}function $x(t,n,e){return JT(t,eN(Lx(n,pg.Decimal),wi(n,dn.MinusSign)),n,dn.Group,dn.Decimal,e)}function eN(t,n=`-`){let e={minInt:1,minFrac:0,maxFrac:0,posPre:``,posSuf:``,negPre:``,negSuf:``,gSize:0,lgSize:0},i=t.split(XT),a=i[0],o=i[1],r=a.indexOf(pu)!==-1?a.split(pu):[a.substring(0,a.lastIndexOf(vl)+1),a.substring(a.lastIndexOf(vl)+1)],s=r[0],l=r[1]||``;e.posPre=s.substring(0,s.indexOf(ug));for(let p=0;p<l.length;p++){let g=l.charAt(p);g===vl?e.minFrac=e.maxFrac=p+1:g===ug?e.maxFrac=p+1:e.posSuf+=g}let h=s.split(ZT);if(e.gSize=h[1]?h[1].length:0,e.lgSize=h[2]||h[1]?(h[2]||h[1]).length:0,o){let p=a.length-e.posPre.length-e.posSuf.length,g=o.indexOf(ug);e.negPre=o.substring(0,g).replace(/'/g,``),e.negSuf=o.slice(g+p).replace(/'/g,``)}else e.negPre=n+e.posPre,e.negSuf=e.posSuf;return e}function tN(t){if(t.digits[0]===0)return t;let n=t.digits.length-t.integerLen;return t.exponent?t.exponent+=2:(n===0?t.digits.push(0,0):n===1&&t.digits.push(0),t.integerLen+=2),t}function nN(t){let n=Math.abs(t)+``,e=0,i,a,o,r,s;for((a=n.indexOf(pu))>-1&&(n=n.replace(pu,``)),(o=n.search(/e/i))>0?(a<0&&(a=o),a+=+n.slice(o+1),n=n.substring(0,o)):a<0&&(a=n.length),o=0;n.charAt(o)===vl;o++);if(o===(s=n.length))i=[0],a=1;else{for(s--;n.charAt(s)===vl;)s--;for(a-=o,i=[],r=0;o<=s;o++,r++)i[r]=Number(n.charAt(o))}return a>Nx&&(i=i.splice(0,Nx-1),e=a-1,a=1),{digits:i,exponent:e,integerLen:a}}function iN(t,n,e){if(n>e)throw new q(2307,!1);let i=t.digits,a=i.length-t.integerLen,o=Math.min(Math.max(n,a),e),r=o+t.integerLen,s=i[r];if(r>0){i.splice(Math.max(t.integerLen,r));for(let g=r;g<i.length;g++)i[g]=0}else{a=Math.max(0,a),t.integerLen=1,i.length=Math.max(1,r=o+1),i[0]=0;for(let g=1;g<r;g++)i[g]=0}if(s>=5)if(r-1<0){for(let g=0;g>r;g--)i.unshift(0),t.integerLen++;i.unshift(1),t.integerLen++}else i[r-1]++;for(;a<Math.max(0,o);a++)i.push(0);let l=o!==0,h=n+t.integerLen,p=i.reduceRight(function(g,_,b,y){return _=_+g,y[b]=_<10?_:_-10,l&&(y[b]===0&&b>=h?y.pop():l=!1),_>=10?1:0},0);p&&(i.unshift(p),t.integerLen++)}function mg(t){let n=parseInt(t);if(isNaN(n))throw new q(2305,!1);return n}var hg=(()=>{class t{_viewContainerRef;ngComponentOutlet=null;ngComponentOutletInputs;ngComponentOutletInjector;ngComponentOutletEnvironmentInjector;ngComponentOutletContent;ngComponentOutletNgModule;_componentRef;_moduleRef;_inputsUsed=new Map;get componentInstance(){return this._componentRef?.instance??null}constructor(e){this._viewContainerRef=e}_needToReCreateNgModuleInstance(e){return e.ngComponentOutletNgModule!==void 0}_needToReCreateComponentInstance(e){return e.ngComponentOutlet!==void 0||e.ngComponentOutletContent!==void 0||e.ngComponentOutletInjector!==void 0||e.ngComponentOutletEnvironmentInjector!==void 0||this._needToReCreateNgModuleInstance(e)}ngOnChanges(e){if(this._needToReCreateComponentInstance(e)&&(this._viewContainerRef.clear(),this._inputsUsed.clear(),this._componentRef=void 0,this.ngComponentOutlet)){let i=this.ngComponentOutletInjector||this._viewContainerRef.parentInjector;this._needToReCreateNgModuleInstance(e)&&(this._moduleRef?.destroy(),this.ngComponentOutletNgModule?this._moduleRef=qf(this.ngComponentOutletNgModule,aN(i)):this._moduleRef=void 0),this._componentRef=this._viewContainerRef.createComponent(this.ngComponentOutlet,{injector:i,ngModuleRef:this._moduleRef,projectableNodes:this.ngComponentOutletContent,environmentInjector:this.ngComponentOutletEnvironmentInjector})}}ngDoCheck(){if(this._componentRef){if(this.ngComponentOutletInputs)for(let e of Object.keys(this.ngComponentOutletInputs))this._inputsUsed.set(e,!0);this._applyInputStateDiff(this._componentRef)}}ngOnDestroy(){this._moduleRef?.destroy()}_applyInputStateDiff(e){for(let[i,a]of this._inputsUsed)a?(e.setInput(i,this.ngComponentOutletInputs[i]),this._inputsUsed.set(i,!1)):(e.setInput(i,void 0),this._inputsUsed.delete(i))}static ɵfac=function(i){return new(i||t)(He(Bt))};static ɵdir=Q({type:t,selectors:[[``,`ngComponentOutlet`,``]],inputs:{ngComponentOutlet:`ngComponentOutlet`,ngComponentOutletInputs:`ngComponentOutletInputs`,ngComponentOutletInjector:`ngComponentOutletInjector`,ngComponentOutletEnvironmentInjector:`ngComponentOutletEnvironmentInjector`,ngComponentOutletContent:`ngComponentOutletContent`,ngComponentOutletNgModule:`ngComponentOutletNgModule`},exportAs:[`ngComponentOutlet`],features:[_t]})}return t})();function aN(t){return t.get(ni).injector}var _l=(()=>{class t{_viewContainerRef;_viewRef=null;ngTemplateOutletContext=null;ngTemplateOutlet=null;ngTemplateOutletInjector=null;injector=f(se);constructor(e){this._viewContainerRef=e}ngOnChanges(e){if(this._shouldRecreateView(e)){let i=this._viewContainerRef;if(this._viewRef&&i.remove(i.indexOf(this._viewRef)),!this.ngTemplateOutlet){this._viewRef=null;return}let a=this._createContextForwardProxy();this._viewRef=i.createEmbeddedView(this.ngTemplateOutlet,a,{injector:this._getInjector()})}}_getInjector(){return this.ngTemplateOutletInjector===`outlet`?this.injector:this.ngTemplateOutletInjector??void 0}_shouldRecreateView(e){return!!e.ngTemplateOutlet||!!e.ngTemplateOutletInjector}_createContextForwardProxy(){return new Proxy({},{set:(e,i,a)=>this.ngTemplateOutletContext?Reflect.set(this.ngTemplateOutletContext,i,a):!1,get:(e,i,a)=>{if(this.ngTemplateOutletContext)return Reflect.get(this.ngTemplateOutletContext,i,a)}})}static ɵfac=function(i){return new(i||t)(He(Bt))};static ɵdir=Q({type:t,selectors:[[``,`ngTemplateOutlet`,``]],inputs:{ngTemplateOutletContext:`ngTemplateOutletContext`,ngTemplateOutlet:`ngTemplateOutlet`,ngTemplateOutletInjector:`ngTemplateOutletInjector`},features:[_t]})}return t})();function Gx(t,n){return new q(2100,!1)}var oN=`mediumDate`;var Wx=new N(``);var qx=new N(``);var Xi=(()=>{class t{locale;defaultTimezone;defaultOptions;constructor(e,i,a){this.locale=e,this.defaultTimezone=i,this.defaultOptions=a}transform(e,i,a,o){if(e==null||e===``||e!==e)return null;try{let r=i??this.defaultOptions?.dateFormat??oN,s=a??this.defaultOptions?.timezone??this.defaultTimezone??void 0;return zx(e,r,o||this.locale,s)}catch(r){throw Gx(t,r.message)}}static ɵfac=function(i){return new(i||t)(He(Ar,16),He(Wx,24),He(qx,24))};static ɵpipe=ol({name:`date`,type:t,pure:!0})}return t})();var wo=(()=>{class t{transform(e){return JSON.stringify(e,null,2)}static ɵfac=function(i){return new(i||t)};static ɵpipe=ol({name:`json`,type:t,pure:!1})}return t})();var En=(()=>{class t{_locale;constructor(e){this._locale=e}transform(e,i,a){if(!rN(e))return null;a||=this._locale;try{return $x(sN(e),a,i)}catch(o){throw Gx(t,o.message)}}static ɵfac=function(i){return new(i||t)(He(Ar,16))};static ɵpipe=ol({name:`number`,type:t,pure:!0})}return t})();function rN(t){return!(t==null||t===``||t!==t)}function sN(t){if(typeof t==`string`&&!isNaN(Number(t)-parseFloat(t)))return Number(t);if(typeof t!=`number`)throw new q(2309,!1);return t}function yl(t,n){n=encodeURIComponent(n);for(let e of t.split(`;`)){let i=e.indexOf(`=`),[a,o]=i==-1?[e,``]:[e.slice(0,i),e.slice(i+1)];if(a.trim()!==n)continue;let r=o;try{r=decodeURIComponent(o)}catch{}return r.length>1&&r[0]===`"`&&r[r.length-1]===`"`&&(r=r.slice(1,-1)),r}return null}var fg=`browser`;var uN=`server`;function Yx(t){return t===fg}function Kx(t){return t===uN}var xl=class{_doc;constructor(n){this._doc=n}manager};var hu=(()=>{class t extends xl{constructor(e){super(e)}supports(e){return!0}addEventListener(e,i,a,o){return e.addEventListener(i,a,o),()=>this.removeEventListener(e,i,a,o)}removeEventListener(e,i,a,o){return e.removeEventListener(i,a,o)}static ɵfac=function(i){return new(i||t)(Se(de))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var bu=new N(``);var _g=(()=>{class t{_zone;_plugins;_eventNameToPlugin=new Map;constructor(e,i){this._zone=i,e.forEach(r=>{r.manager=this});let a=e.filter(r=>!(r instanceof hu));this._plugins=a.slice().reverse();let o=e.find(r=>r instanceof hu);o&&this._plugins.push(o)}addEventListener(e,i,a,o){return this._findPluginFor(i).addEventListener(e,i,a,o)}getZone(){return this._zone}_findPluginFor(e){let i=this._eventNameToPlugin.get(e);if(i)return i;if(i=this._plugins.find(o=>o.supports(e)),!i)throw new q(-5101,!1);return this._eventNameToPlugin.set(e,i),i}static ɵfac=function(i){return new(i||t)(Se(bu),Se(Z))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var gg=`ng-app-id`;function Qx(t){for(let n of t)n.remove()}function Xx(t,n){let e=n.createElement(`style`);return e.textContent=t,e}function mN(t,n,e,i){let a=t.head?.querySelectorAll(`style[${gg}="${n}"],link[${gg}="${n}"]`);if(!a||a.length===0)return!1;for(let o of a)o.removeAttribute(gg),o instanceof HTMLLinkElement?i.set(o.href.slice(o.href.lastIndexOf(`/`)+1),{usage:0,elements:[o]}):o.textContent&&e.set(o.textContent,{usage:0,elements:[o]});return!0}function vg(t,n){let e=n.createElement(`link`);return e.setAttribute(`rel`,`stylesheet`),e.setAttribute(`href`,t),e}var yg=(()=>{class t{doc;appId;nonce;inline=new Map;external=new Map;hosts=new Set;constructor(e,i,a,o={}){this.doc=e,this.appId=i,this.nonce=a,mN(e,i,this.inline,this.external)&&this.hosts.add(e.head)}addStyles(e,i){for(let a of e)this.addUsage(a,this.inline,Xx);i?.forEach(a=>this.addUsage(a,this.external,vg))}removeStyles(e,i){for(let a of e)this.removeUsage(a,this.inline);i?.forEach(a=>this.removeUsage(a,this.external))}addUsage(e,i,a){let o=i.get(e);o?o.usage++:i.set(e,{usage:1,elements:[...this.hosts].map(r=>this.addElement(r,a(e,this.doc)))})}removeUsage(e,i){let a=i.get(e);a&&(a.usage--,a.usage<=0&&(Qx(a.elements),i.delete(e)))}ngOnDestroy(){for(let[,{elements:e}]of[...this.inline,...this.external])Qx(e);this.hosts.clear()}addHost(e){if(!this.hosts.has(e)){this.hosts.add(e);for(let[i,{elements:a}]of this.inline)a.push(this.addElement(e,Xx(i,this.doc)));for(let[i,{elements:a}]of this.external)a.push(this.addElement(e,vg(i,this.doc)))}}removeHost(e){this.hosts.delete(e);for(let i of[...this.inline.values(),...this.external.values()]){let a=[];for(let o of i.elements)o.parentNode===e?o.remove():a.push(o);i.elements=a}}addElement(e,i){return this.nonce&&i.setAttribute(`nonce`,this.nonce),e.appendChild(i)}static ɵfac=function(i){return new(i||t)(Se(de),Se($i),Se(ya,8),Se(Gi))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var bg={svg:`http://www.w3.org/2000/svg`,xhtml:`http://www.w3.org/1999/xhtml`,xlink:`http://www.w3.org/1999/xlink`,xml:`http://www.w3.org/XML/1998/namespace`,xmlns:`http://www.w3.org/2000/xmlns/`,math:`http://www.w3.org/1998/Math/MathML`};var xg=/%COMP%/g;var Jx=`%COMP%`;var pN=`_nghost-${Jx}`;var hN=`_ngcontent-${Jx}`;var fN=!0;var gN=new N(``,{factory:()=>fN});var bN=new N(``);function vN(t){return hN.replace(xg,t)}function _N(t){return pN.replace(xg,t)}function eC(t,n){return n.map(e=>e.replace(xg,t))}var Cg=(()=>{class t{eventManager;sharedStylesHost;appId;removeStylesOnCompDestroy;doc;ngZone;nonce;tracingService;rendererByCompId=new Map;defaultRenderer;cssVarNamespace;constructor(e,i,a,o,r,s,l=null,h=null,p=null){this.eventManager=e,this.sharedStylesHost=i,this.appId=a,this.removeStylesOnCompDestroy=o,this.doc=r,this.ngZone=s,this.nonce=l,this.tracingService=h,this.cssVarNamespace=p??``,this.defaultRenderer=new Cl(e,r,s,this.tracingService,this.cssVarNamespace)}createRenderer(e,i){if(!e||!i)return this.defaultRenderer;let a=this.getOrCreateRenderer(e,i);return a instanceof gu?a.applyToHost(e):a instanceof Sl&&a.applyStyles(),a}getOrCreateRenderer(e,i){let a=this.rendererByCompId,o=a.get(i.id);if(!o){let r=this.doc,s=this.ngZone,l=this.eventManager,h=this.sharedStylesHost,p=this.removeStylesOnCompDestroy,g=this.tracingService;switch(i.encapsulation){case ei.Emulated:o=new gu(l,h,i,this.appId,p,r,s,g,this.cssVarNamespace);break;case ei.ShadowDom:return new fu(l,e,i,r,s,this.nonce,g,this.cssVarNamespace,h);case ei.ExperimentalIsolatedShadowDom:return new fu(l,e,i,r,s,this.nonce,g,this.cssVarNamespace);default:o=new Sl(l,h,i,p,r,s,g,this.cssVarNamespace)}a.set(i.id,o)}return o}ngOnDestroy(){this.rendererByCompId.clear()}componentReplaced(e){this.rendererByCompId.delete(e)}static ɵfac=function(i){return new(i||t)(Se(_g),Se(yo),Se($i),Se(gN),Se(de),Se(Z),Se(ya),Se(ii,8),Se(bN,8))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();var Cl=class{eventManager;doc;ngZone;tracingService;cssVarNamespace;data=Object.create(null);throwOnSyntheticProps=!0;constructor(n,e,i,a,o=``){this.eventManager=n,this.doc=e,this.ngZone=i,this.tracingService=a,this.cssVarNamespace=o}destroy(){}destroyNode=null;createElement(n,e){return e?this.doc.createElementNS(bg[e]||e,n):this.doc.createElement(n)}createComment(n){return this.doc.createComment(n)}createText(n){return this.doc.createTextNode(n)}appendChild(n,e){(Zx(n)?n.content:n).appendChild(e)}insertBefore(n,e,i){if(n){let a=Zx(n)?n.content:n;if(i!=null&&i.parentNode!==a)throw new q(-5106,!1);a.insertBefore(e,i)}}removeChild(n,e){e.remove()}selectRootElement(n,e){let i=typeof n==`string`?this.doc.querySelector(n):n;if(!i)throw new q(-5104,!1);return e||(i.textContent=``),i}parentNode(n){return n.parentNode}nextSibling(n){return n.nextSibling}setAttribute(n,e,i,a){if(a){e=a+`:`+e;let o=bg[a];o?n.setAttributeNS(o,e,i):n.setAttribute(e,i)}else n.setAttribute(e,i)}removeAttribute(n,e,i){if(i){let a=bg[i];a?n.removeAttributeNS(a,e):n.removeAttribute(`${i}:${e}`)}else n.removeAttribute(e)}addClass(n,e){n.classList.add(e)}removeClass(n,e){n.classList.remove(e)}setStyle(n,e,i,a){let o=e.startsWith(`--`);o&&(e=e.replace(`%NS%`,this.cssVarNamespace)),o||a&(xi.DashCase|xi.Important)?n.style.setProperty(e,i,a&xi.Important?`important`:``):n.style[e]=i}removeStyle(n,e,i){let a=e.startsWith(`--`);a&&(e=e.replace(`%NS%`,this.cssVarNamespace)),a||i&xi.DashCase?n.style.removeProperty(e):n.style[e]=``}setProperty(n,e,i){n!=null&&(n[e]=i)}setValue(n,e){n.nodeValue=e}listen(n,e,i,a){if(typeof n==`string`&&(n=Fn().getGlobalEventTarget(this.doc,n),!n))throw new q(-5102,!1);let o=this.decoratePreventDefault(i);return this.tracingService?.wrapEventListener&&(o=this.tracingService.wrapEventListener(n,e,o)),this.eventManager.addEventListener(n,e,o,a)}decoratePreventDefault(n){return e=>{if(e===`__ngUnwrap__`)return n;n(e)===!1&&e.preventDefault()}}};function Zx(t){return t.tagName===`TEMPLATE`&&t.content!==void 0}var fu=class extends Cl{hostEl;sharedStylesHost;shadowRoot;constructor(n,e,i,a,o,r,s,l,h){super(n,a,o,s,l),this.hostEl=e,this.sharedStylesHost=h,this.shadowRoot=e.attachShadow({mode:`open`}),this.sharedStylesHost&&this.sharedStylesHost.addHost(this.shadowRoot);let p=i.styles;p=eC(i.id,p).map(_=>_.replace(/%NS%/g,l));for(let _ of p){let b=document.createElement(`style`);r&&b.setAttribute(`nonce`,r),b.textContent=_,this.shadowRoot.appendChild(b)}let g=i.getExternalStyles?.();if(g)for(let _ of g){let b=vg(_,a);r&&b.setAttribute(`nonce`,r),this.shadowRoot.appendChild(b)}}nodeOrShadowRoot(n){return n===this.hostEl?this.shadowRoot:n}appendChild(n,e){return super.appendChild(this.nodeOrShadowRoot(n),e)}insertBefore(n,e,i){return super.insertBefore(this.nodeOrShadowRoot(n),e,i)}removeChild(n,e){return super.removeChild(null,e)}parentNode(n){return this.nodeOrShadowRoot(super.parentNode(this.nodeOrShadowRoot(n)))}destroy(){this.sharedStylesHost&&this.sharedStylesHost.removeHost(this.shadowRoot)}};var Sl=class extends Cl{sharedStylesHost;removeStylesOnCompDestroy;styles;styleUrls;constructor(n,e,i,a,o,r,s,l,h){super(n,o,r,s,l),this.sharedStylesHost=e,this.removeStylesOnCompDestroy=a;let p=i.styles,g=h?eC(h,p):p;this.styles=g.map(_=>_.replace(/%NS%/g,l)),this.styleUrls=i.getExternalStyles?.(h)}applyStyles(){this.sharedStylesHost.addStyles(this.styles,this.styleUrls)}destroy(){this.removeStylesOnCompDestroy&&Sf.size===0&&this.sharedStylesHost.removeStyles(this.styles,this.styleUrls)}};var gu=class extends Sl{contentAttr;hostAttr;constructor(n,e,i,a,o,r,s,l,h){let p=a+`-`+i.id;super(n,e,i,o,r,s,l,h,p),this.contentAttr=vN(p),this.hostAttr=_N(p)}applyToHost(n){this.applyStyles(),this.setAttribute(n,this.hostAttr,``)}createElement(n,e){let i=super.createElement(n,e);return super.setAttribute(i,this.contentAttr,``),i}};var vu=class t extends hl{supportsDOMEvents=!0;static makeCurrent(){sg(new t)}onAndCancel(n,e,i,a){return n.addEventListener(e,i,a),()=>{n.removeEventListener(e,i,a)}}dispatchEvent(n,e){n.dispatchEvent(e)}remove(n){n.remove()}createElement(n,e){return e=e||this.getDefaultDocument(),e.createElement(n)}createHtmlDocument(){return document.implementation.createHTMLDocument(`fakeTitle`)}getDefaultDocument(){return document}isElementNode(n){return n.nodeType===Node.ELEMENT_NODE}isShadowRoot(n){return n instanceof DocumentFragment}getGlobalEventTarget(n,e){return e===`window`?window:e===`document`?n:e===`body`?n.body:null}getBaseHref(n){let e=yN();return e==null?null:xN(e)}resetBaseElement(){wl=null}getUserAgent(){return window.navigator.userAgent}getCookie(n){return yl(document.cookie,n)}};var wl=null;function yN(){return wl=wl||document.head.querySelector(`base`),wl?wl.getAttribute(`href`):null}function xN(t){return new URL(t,document.baseURI).pathname}var tC=[`alt`,`control`,`meta`,`shift`];var CN={"\b":`Backspace`,"	":`Tab`,"":`Delete`,"\x1B":`Escape`,Del:`Delete`,Esc:`Escape`,Left:`ArrowLeft`,Right:`ArrowRight`,Up:`ArrowUp`,Down:`ArrowDown`,Menu:`ContextMenu`,Scroll:`ScrollLock`,Win:`OS`};var SN={alt:t=>t.altKey,control:t=>t.ctrlKey,meta:t=>t.metaKey,shift:t=>t.shiftKey};var nC=(()=>{class t extends xl{constructor(e){super(e)}supports(e){return t.parseEventName(e)!=null}addEventListener(e,i,a,o){let r=t.parseEventName(i),s=t.eventCallback(r.fullKey,a,this.manager.getZone());return this.manager.getZone().runOutsideAngular(()=>Fn().onAndCancel(e,r.domEventName,s,o))}static parseEventName(e){let i=e.toLowerCase().split(`.`),a=i.shift();if(i.length===0||!(a===`keydown`||a===`keyup`))return null;let o=t._normalizeKey(i.pop()),r=``,s=i.indexOf(`code`);if(s>-1&&(i.splice(s,1),r=`code.`),tC.forEach(h=>{let p=i.indexOf(h);p>-1&&(i.splice(p,1),r+=h+`.`)}),r+=o,i.length!=0||o.length===0)return null;let l={};return l.domEventName=a,l.fullKey=r,l}static matchEventFullKeyCode(e,i){let a=CN[e.key]||e.key,o=``;return i.indexOf(`code.`)>-1&&(a=e.code,o=`code.`),a==null||!a?!1:(a=a.toLowerCase(),a===` `?a=`space`:a===`.`&&(a=`dot`),tC.forEach(r=>{if(r!==a){let s=SN[r];s(e)&&(o+=r+`.`)}}),o+=a,o===i)}static eventCallback(e,i,a){return o=>{t.matchEventFullKeyCode(o,e)&&a.runGuarded(()=>i(o))}}static _normalizeKey(e){return e===`esc`?`escape`:e}static ɵfac=function(i){return new(i||t)(Se(de))};static ɵprov=be({token:t,factory:t.ɵfac})}return t})();async function Sg(t,n,e){return Sx(r({rootComponent:t},wN(n,e)))}function wN(t,n){return{platformRef:n?.platformRef,appProviders:[...MN,...t?.providers??[]],platformProviders:kN}}function EN(){vu.makeCurrent()}function DN(){return new Tn}function IN(){return pf(document),document}var kN=[{provide:Gi,useValue:fg},{provide:bd,useValue:EN,multi:!0},{provide:de,useFactory:IN}];var MN=[{provide:As,useValue:`root`},{provide:Tn,useFactory:DN},{provide:bu,useClass:hu,multi:!0},{provide:bu,useClass:nC,multi:!0},Cg,{provide:yo,useClass:yg},{provide:yg,useExisting:yo},_g,{provide:Nt,useExisting:Cg},[]];var Ji=class t{headers;normalizedNames=new Map;lazyInit;lazyUpdate=null;constructor(n){n?typeof n==`string`?this.lazyInit=()=>{this.headers=new Map,n.split(`
`).forEach(e=>{let i=e.indexOf(`:`);if(i>0){let a=e.slice(0,i),o=e.slice(i+1).trim();this.addHeaderEntry(a,o)}})}:typeof Headers<`u`&&n instanceof Headers?(this.headers=new Map,n.forEach((e,i)=>{this.addHeaderEntry(i,e)})):this.lazyInit=()=>{this.headers=new Map,Object.entries(n).forEach(([e,i])=>{this.setHeaderEntries(e,i)})}:this.headers=new Map}has(n){return this.init(),this.headers.has(n.toLowerCase())}get(n){this.init();let e=this.headers.get(n.toLowerCase());return e&&e.length>0?e[0]:null}keys(){return this.init(),Array.from(this.normalizedNames.values())}getAll(n){return this.init(),this.headers.get(n.toLowerCase())||null}append(n,e){return this.clone({name:n,value:e,op:`a`})}set(n,e){return this.clone({name:n,value:e,op:`s`})}delete(n,e){return this.clone({name:n,value:e,op:`d`})}maybeSetNormalizedName(n,e){this.normalizedNames.has(e)||this.normalizedNames.set(e,n)}init(){this.lazyInit&&(this.lazyInit instanceof t?this.copyFrom(this.lazyInit):this.lazyInit(),this.lazyInit=null,this.lazyUpdate&&(this.lazyUpdate.forEach(n=>this.applyUpdate(n)),this.lazyUpdate=null))}copyFrom(n){n.init();for(let[e,i]of n.headers.entries())this.headers.set(e,i),this.normalizedNames.set(e,n.normalizedNames.get(e))}clone(n){let e=new t;return e.lazyInit=this.lazyInit&&this.lazyInit instanceof t?this.lazyInit:this,e.lazyUpdate=(this.lazyUpdate||[]).concat([n]),e}applyUpdate(n){let e=n.name.toLowerCase();switch(n.op){case`a`:case`s`:let i=n.value;if(typeof i==`string`&&(i=[i]),i.length===0)return;this.maybeSetNormalizedName(n.name,e);let a=n.op===`a`?(this.headers.get(e)||[]).slice():[];a.push(...i),this.headers.set(e,a);break;case`d`:let o=n.value;if(o===void 0)this.headers.delete(e),this.normalizedNames.delete(e);else{let r=Array.isArray(o)?o:[o],s=this.headers.get(e);if(!s)return;s=s.filter(l=>r.indexOf(l)===-1),s.length===0?(this.headers.delete(e),this.normalizedNames.delete(e)):this.headers.set(e,s)}}}addHeaderEntry(n,e){let i=n.toLowerCase();this.maybeSetNormalizedName(n,i),this.headers.has(i)?this.headers.get(i).push(e):this.headers.set(i,[e])}setHeaderEntries(n,e){let i=(Array.isArray(e)?e:[e]).map(o=>o.toString()),a=n.toLowerCase();this.headers.set(a,i),this.maybeSetNormalizedName(n,a)}forEach(n){this.init(),Array.from(this.normalizedNames.keys()).forEach(e=>n(this.normalizedNames.get(e),this.headers.get(e)))}};var yu=class{map=new Map;set(n,e){return this.map.set(n,e),this}get(n){return this.map.has(n)||this.map.set(n,n.defaultValue()),this.map.get(n)}delete(n){return this.map.delete(n),this}has(n){return this.map.has(n)}keys(){return this.map.keys()}};var xu=class{encodeKey(n){return iC(n)}encodeValue(n){return iC(n)}decodeKey(n){return decodeURIComponent(n)}decodeValue(n){return decodeURIComponent(n)}};function TN(t,n){let e=new Map;return t.length>0&&t.replace(/^\?/,``).split(`&`).forEach(a=>{let o=a.indexOf(`=`),[r,s]=o==-1?[n.decodeKey(a),``]:[n.decodeKey(a.slice(0,o)),n.decodeValue(a.slice(o+1))],l=e.get(r)||[];l.push(s),e.set(r,l)}),e}var NN=/%(\d[a-f0-9])/gi;var AN={40:`@`,"3A":`:`,24:`$`,"2C":`,`,"3B":`;`,"3D":`=`,"3F":`?`,"2F":`/`};function iC(t){return encodeURIComponent(t).replace(NN,(n,e)=>AN[e]??n)}function _u(t){return`${t}`}var Zi=class t{map;encoder;updates=null;cloneFrom=null;constructor(n={}){if(this.encoder=n.encoder||new xu,n.fromString){if(n.fromObject)throw new q(2805,!1);this.map=TN(n.fromString,this.encoder)}else n.fromObject?(this.map=new Map,Object.keys(n.fromObject).forEach(e=>{let i=n.fromObject[e],a=Array.isArray(i)?i.map(_u):[_u(i)];this.map.set(e,a)})):this.map=null}has(n){return this.init(),this.map.has(n)}get(n){this.init();let e=this.map.get(n);return e?e[0]:null}getAll(n){return this.init(),this.map.get(n)||null}keys(){return this.init(),Array.from(this.map.keys())}append(n,e){return this.clone({param:n,value:e,op:`a`})}appendAll(n){let e=[];return Object.keys(n).forEach(i=>{let a=n[i];Array.isArray(a)?a.forEach(o=>{e.push({param:i,value:o,op:`a`})}):e.push({param:i,value:a,op:`a`})}),this.clone(e)}set(n,e){return this.clone({param:n,value:e,op:`s`})}delete(n,e){return this.clone({param:n,value:e,op:`d`})}toString(){return this.init(),this.keys().map(n=>{let e=this.encoder.encodeKey(n);return this.map.get(n).map(i=>e+`=`+this.encoder.encodeValue(i)).join(`&`)}).filter(n=>n!==``).join(`&`)}clone(n){let e=new t({encoder:this.encoder});return e.cloneFrom=this.cloneFrom||this,e.updates=(this.updates||[]).concat(n),e}init(){if(this.map===null&&(this.map=new Map),this.cloneFrom!==null){this.cloneFrom.init();for(let[n,e]of this.cloneFrom.map.entries())this.map.set(n,e);this.updates.forEach(n=>{switch(n.op){case`a`:case`s`:let e=n.op===`a`?(this.map.get(n.param)||[]).slice():[];e.push(_u(n.value)),this.map.set(n.param,e);break;case`d`:if(n.value!==void 0){let i=(this.map.get(n.param)||[]).slice(),a=i.indexOf(_u(n.value));a!==-1&&i.splice(a,1),i.length>0?this.map.set(n.param,i):this.map.delete(n.param)}else{this.map.delete(n.param);break}}}),this.cloneFrom=this.updates=null}}};function RN(t){switch(t){case`DELETE`:case`GET`:case`HEAD`:case`OPTIONS`:case`JSONP`:return!1;default:return!0}}function aC(t){return typeof ArrayBuffer<`u`&&t instanceof ArrayBuffer}function oC(t){return typeof Blob<`u`&&t instanceof Blob}function rC(t){return typeof FormData<`u`&&t instanceof FormData}function ON(t){return typeof URLSearchParams<`u`&&t instanceof URLSearchParams}var wg=`Content-Type`;var sC=`Accept`;var dC=`text/plain`;var uC=`application/json`;var PN=`${uC}, ${dC}, */*`;var Or=class t{url;body=null;headers;context;reportProgress=!1;reportUploadProgress=!1;reportDownloadProgress=!1;withCredentials=!1;credentials;keepalive=!1;cache;priority;mode;redirect;referrer;integrity;referrerPolicy;responseType=`json`;method;params;urlWithParams;transferCache;timeout;constructor(n,e,i,a){this.url=e,this.method=n.toUpperCase();let o;if(RN(this.method)||a?(this.body=i!==void 0?i:null,o=a):o=i,o){if(this.reportProgress=!!o.reportProgress,this.reportUploadProgress=!!o.reportUploadProgress,this.reportDownloadProgress=!!o.reportDownloadProgress,this.withCredentials=!!o.withCredentials,this.keepalive=!!o.keepalive,o.responseType&&(this.responseType=o.responseType),o.headers&&(this.headers=o.headers),o.context&&(this.context=o.context),o.params&&(this.params=o.params),o.priority&&(this.priority=o.priority),o.cache&&(this.cache=o.cache),o.credentials&&(this.credentials=o.credentials),typeof o.timeout==`number`){if(o.timeout<1||!Number.isInteger(o.timeout))throw new q(2822,``);this.timeout=o.timeout}o.mode&&(this.mode=o.mode),o.redirect&&(this.redirect=o.redirect),o.integrity&&(this.integrity=o.integrity),o.referrer!==void 0&&(this.referrer=o.referrer),o.referrerPolicy&&(this.referrerPolicy=o.referrerPolicy),this.transferCache=o.transferCache}if(this.headers??=new Ji,this.context??=new yu,!this.params)this.params=new Zi,this.urlWithParams=e;else{let r=this.params.toString();if(r.length===0)this.urlWithParams=e;else{let s=e,l=``,h=e.indexOf(`#`);h!==-1&&(l=e.substring(h),s=e.substring(0,h));let p=s.indexOf(`?`),g=p===-1?`?`:p<s.length-1?`&`:``;this.urlWithParams=s+g+r+l}}}serializeBody(){return this.body===null?null:typeof this.body==`string`||aC(this.body)||oC(this.body)||rC(this.body)||ON(this.body)?this.body:this.body instanceof Zi?this.body.toString():typeof this.body==`object`||typeof this.body==`boolean`||Array.isArray(this.body)?JSON.stringify(this.body):this.body.toString()}detectContentTypeHeader(){return this.body===null||rC(this.body)?null:oC(this.body)?this.body.type||null:aC(this.body)?null:typeof this.body==`string`?dC:this.body instanceof Zi?`application/x-www-form-urlencoded;charset=UTF-8`:typeof this.body==`object`||typeof this.body==`number`||typeof this.body==`boolean`?uC:null}clone(n={}){let e=n.method||this.method,i=n.url||this.url,a=n.responseType||this.responseType,o=n.keepalive??this.keepalive,r=n.priority||this.priority,s=n.cache||this.cache,l=n.mode||this.mode,h=n.redirect||this.redirect,p=n.credentials||this.credentials,g=n.referrer??this.referrer,_=n.integrity||this.integrity,b=n.referrerPolicy||this.referrerPolicy,y=n.transferCache??this.transferCache,I=n.timeout??this.timeout,P=n.body!==void 0?n.body:this.body,ie=n.withCredentials??this.withCredentials,L=n.reportProgress??this.reportProgress,x=n.reportUploadProgress??this.reportUploadProgress,A=n.reportDownloadProgress??this.reportDownloadProgress,R=n.headers||this.headers,W=n.params||this.params,ye=n.context??this.context;return n.setHeaders!==void 0&&(R=Object.keys(n.setHeaders).reduce((bt,We)=>bt.set(We,n.setHeaders[We]),R)),n.setParams&&(W=Object.keys(n.setParams).reduce((bt,We)=>bt.set(We,n.setParams[We]),W)),new t(e,i,P,{params:W,headers:R,context:ye,reportProgress:L,reportUploadProgress:x,reportDownloadProgress:A,responseType:a,withCredentials:ie,transferCache:y,keepalive:o,cache:s,priority:r,timeout:I,mode:l,redirect:h,credentials:p,referrer:g,integrity:_,referrerPolicy:b})}};var Eo=(function(t){return t[t.Sent=0]=`Sent`,t[t.UploadProgress=1]=`UploadProgress`,t[t.ResponseHeader=2]=`ResponseHeader`,t[t.DownloadProgress=3]=`DownloadProgress`,t[t.Response=4]=`Response`,t[t.User=5]=`User`,t})(Eo||{});var Pr=class{headers;status;statusText;url;ok;type;redirected;responseType;constructor(n,e=200,i=`OK`){this.headers=n.headers||new Ji,this.status=n.status!==void 0?n.status:e,this.statusText=n.statusText||i,this.url=n.url||null,this.redirected=n.redirected,this.responseType=n.responseType,this.ok=this.status>=200&&this.status<300}};var Cu=class t extends Pr{constructor(n={}){super(n)}type=Eo.ResponseHeader;clone(n={}){return new t({headers:n.headers||this.headers,status:n.status!==void 0?n.status:this.status,statusText:n.statusText||this.statusText,url:n.url||this.url||void 0})}};var El=class t extends Pr{body;constructor(n={}){super(n),this.body=n.body!==void 0?n.body:null}type=Eo.Response;clone(n={}){return new t({body:n.body!==void 0?n.body:this.body,headers:n.headers||this.headers,status:n.status!==void 0?n.status:this.status,statusText:n.statusText||this.statusText,url:n.url||this.url||void 0,redirected:n.redirected??this.redirected,responseType:n.responseType??this.responseType})}};var Ei=class extends Pr{name=`HttpErrorResponse`;message;error;ok=!1;constructor(n){super(n,0,`Unknown Error`),this.status>=200&&this.status<300?this.message=`Http failure during parsing for ${n.url||`(unknown url)`}`:this.message=`Http failure response for ${n.url||`(unknown url)`}: ${n.status} ${n.statusText}`,this.error=n.error||null}};var FN=200;var LN=/^\)\]\}',?\n/;var mC=new N(``,{factory:()=>null});var Su=(()=>{class t{fetchImpl=f(Dg,{optional:!0})?.fetch??((...e)=>globalThis.fetch(...e));ngZone=f(Z);destroyRef=f(Ae);maxResponseSize=f(mC);handle(e){return new Ce(i=>{let a=new AbortController,o=!1,r={next:l=>{l.type===Eo.Response&&(o=!0),i.next(l)},error:l=>{o=!0,i.error(l)},complete:()=>{o=!0,i.complete()}};this.doRequest(e,a.signal,r).then(Ig,l=>r.error(new Ei({error:l})));let s;return e.timeout&&(s=this.ngZone.runOutsideAngular(()=>setTimeout(()=>{a.signal.aborted||a.abort(new DOMException(`signal timed out`,`TimeoutError`))},e.timeout))),()=>{s!==void 0&&clearTimeout(s),!o&&!a.signal.aborted&&a.abort()}})}async doRequest(e,i,a){let o=this.createRequestInit(e),r$1;try{let P=this.ngZone.runOutsideAngular(()=>this.fetchImpl(e.urlWithParams,r({signal:i},o)));VN(P),a.next({type:Eo.Sent}),r$1=await P}catch(P){a.error(new Ei({error:P,status:P.status??0,statusText:P.statusText,url:e.urlWithParams,headers:P.headers}));return}let s=new Ji(r$1.headers),l=r$1.statusText,h=r$1.url||e.urlWithParams,p=r$1.status,g=null,_=e.reportProgress||e.reportDownloadProgress;if(_&&a.next(new Cu({headers:s,status:p,statusText:l,url:h})),r$1.body){let P=r$1.headers.get(wg)??``,ie=r$1.headers.get(`content-length`),L=ie!==null?Number(ie):NaN;this.maxResponseSize!==null&&Number.isFinite(L)&&L>this.maxResponseSize&&(await r$1.body.cancel(),lC(this.maxResponseSize));let x=[],A=r$1.body.getReader(),R=0,W,ye,bt=typeof Zone<`u`&&Zone.current,We=!1;if(await this.ngZone.runOutsideAngular(async()=>{for(;;){if(this.destroyRef.destroyed){await A.cancel(),We=!0;break}let{done:Ie,value:F}=await A.read();if(Ie)break;if(x.push(F),R+=F.length,this.maxResponseSize!==null&&R>this.maxResponseSize&&(await A.cancel(),lC(this.maxResponseSize)),_){ye=e.responseType===`text`?(ye??``)+(W??=cC(P)).decode(F,{stream:!0}):void 0;let qe=()=>a.next({type:Eo.DownloadProgress,total:Number.isFinite(L)?L:void 0,loaded:R,partialText:ye});bt?bt.run(qe):qe()}}}),We){a.complete();return}let pe=this.concatChunks(x,R);try{g=this.parseBody(e,pe,P,p)}catch(Ie){a.error(new Ei({error:Ie,headers:new Ji(r$1.headers),status:r$1.status,statusText:r$1.statusText,url:r$1.url||e.urlWithParams}));return}}p===0&&(p=g?FN:0);let b=p>=200&&p<300,y=r$1.redirected,I=r$1.type;b?(a.next(new El({body:g,headers:s,status:p,statusText:l,url:h,redirected:y,responseType:I})),a.complete()):a.error(new Ei({error:g,headers:s,status:p,statusText:l,url:h,redirected:y,responseType:I}))}parseBody(e,i,a,o){switch(e.responseType){case`json`:let r=new TextDecoder().decode(i).replace(LN,``);if(r===``)return null;try{return JSON.parse(r)}catch(s){if(o<200||o>=300)return r;throw s}case`text`:return cC(a).decode(i);case`blob`:return new Blob([i],{type:a});case`arraybuffer`:return i.buffer}}createRequestInit(e){if(e.reportUploadProgress)throw new q(2824,!1);let i={},a;if(a=e.credentials,e.withCredentials&&(a=`include`),e.headers.forEach((o,r)=>i[o]=r.join(`,`)),e.headers.has(sC)||(i[sC]=PN),!e.headers.has(wg)){let o=e.detectContentTypeHeader();o!==null&&(i[wg]=o)}return{body:e.serializeBody(),method:e.method,headers:i,credentials:a,keepalive:e.keepalive,cache:e.cache,priority:e.priority,mode:e.mode,redirect:e.redirect,referrer:e.referrer,integrity:e.integrity,referrerPolicy:e.referrerPolicy}}concatChunks(e,i){let a=new Uint8Array(i),o=0;for(let r of e)a.set(r,o),o+=r.length;return a}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Dg=class{};function Ig(){}function VN(t){t.then(Ig,Ig)}function lC(t){throw new q(-2825,!1)}var BN=/charset=\s*["']?([^;"'\s]+)["']?/i;function cC(t){let n=t.match(BN);if(n!==null)try{return new TextDecoder(n[1])}catch{}return new TextDecoder}var jN=new N(``,{factory:()=>!0});var zN=`XSRF-TOKEN`;var HN=new N(``,{factory:()=>zN});var UN=`X-XSRF-TOKEN`;var $N=new N(``,{factory:()=>UN});var GN=(()=>{class t{cookieName=f(HN);doc=f(de);lastCookieString=``;lastToken=null;parseCount=0;getToken(){let e=this.doc.cookie||``;return e!==this.lastCookieString&&(this.parseCount++,this.lastToken=yl(e,this.cookieName),this.lastCookieString=e),this.lastToken}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var pC=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(GN),a},providedIn:`root`})}return t})();function hC(t,n){if(!f(jN)||t.method===`GET`||t.method===`HEAD`)return n(t);try{let a=f(Rr).href,{origin:o}=new URL(a),{origin:r}=new URL(t.url,o);if(o!==r)return n(t)}catch{return n(t)}let e=f(pC).getToken(),i=f($N);return e!=null&&!t.headers.has(i)&&(t=t.clone({headers:t.headers.set(i,e)})),n(t)}function WN(t,n){return n(t)}function qN(t,n,e){return(i,a)=>or(e,()=>n(i,o=>t(o,a)))}var fC=new N(``,{factory:()=>[hC]});var gC=new N(``);var bC=new N(``,{factory:()=>!0});var kg=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(Su),a},providedIn:`root`})}return t})();var wu=(()=>{class t{backend;injector;chain=null;pendingTasks=f(xa);contributeToStability=f(bC);constructor(e,i){this.backend=e,this.injector=i}handle(e){if(this.chain===null){let a=this.injector.get(Eu,null,{skipSelf:!0}),o=a!==null&&this.backend===a,r=this.injector.get(gC,[],o?{self:!0}:void 0),s=Array.from(new Set([...this.injector.get(fC),...r]));this.chain=s.reduceRight((l,h)=>qN(l,h,this.injector),WN)}let i=this.chain;if(this.contributeToStability){let a=this.pendingTasks.add();return it(()=>i(e,o=>this.backend.handle(o))).pipe(mp(a))}else return it(()=>i(e,a=>this.backend.handle(a)))}static ɵfac=function(i){return new(i||t)(Se(kg),Se(Mt))};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();var Eu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(wu),a},providedIn:`root`})}return t})();function Eg(t,n){return r({body:n},t)}var Di=(()=>{class t{handler;constructor(e){this.handler=e}request(e,i,a={}){let o;if(e instanceof Or)o=e;else{let l;a.headers instanceof Ji?l=a.headers:l=new Ji(a.headers);let h;a.params&&(a.params instanceof Zi?h=a.params:h=new Zi({fromObject:a.params})),o=new Or(e,i,a.body!==void 0?a.body:null,{headers:l,context:a.context,params:h,reportProgress:a.reportProgress,reportUploadProgress:a.reportUploadProgress,reportDownloadProgress:a.reportDownloadProgress,responseType:a.responseType||`json`,withCredentials:a.withCredentials,transferCache:a.transferCache,keepalive:a.keepalive,priority:a.priority,cache:a.cache,mode:a.mode,redirect:a.redirect,credentials:a.credentials,referrer:a.referrer,referrerPolicy:a.referrerPolicy,integrity:a.integrity,timeout:a.timeout})}let r=Mn(o).pipe(up(l=>this.handler.handle(l)));if(e instanceof Or||a.observe===`events`)return r;let s=r.pipe(Qe(l=>l instanceof El));switch(a.observe||`body`){case`body`:switch(o.responseType){case`arraybuffer`:return s.pipe(st(l=>{if(l.body!==null&&!(l.body instanceof ArrayBuffer))throw new q(2806,!1);return l.body}));case`blob`:return s.pipe(st(l=>{if(l.body!==null&&!(l.body instanceof Blob))throw new q(2807,!1);return l.body}));case`text`:return s.pipe(st(l=>{if(l.body!==null&&typeof l.body!=`string`)throw new q(2808,!1);return l.body}));default:return s.pipe(st(l=>l.body))}case`response`:return s;default:throw new q(2809,!1)}}delete(e,i={}){return this.request(`DELETE`,e,i)}get(e,i={}){return this.request(`GET`,e,i)}head(e,i={}){return this.request(`HEAD`,e,i)}jsonp(e,i){return this.request(`JSONP`,e,{params:new Zi().append(i,`JSONP_CALLBACK`),observe:`body`,responseType:`json`})}options(e,i={}){return this.request(`OPTIONS`,e,i)}patch(e,i,a={}){return this.request(`PATCH`,e,Eg(a,i))}post(e,i,a={}){return this.request(`POST`,e,Eg(a,i))}put(e,i,a={}){return this.request(`PUT`,e,Eg(a,i))}static ɵfac=function(i){return new(i||t)(Se(Eu))};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();function Mg(...t){let n=[Di,Su,wu,{provide:Eu,useExisting:wu},{provide:kg,useFactory:()=>f(Su)},{provide:fC,useValue:hC,multi:!0}];for(let e of t)n.push(...e.ɵproviders);return sn(n)}var Tg=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=be({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(YN),a},providedIn:`root`})}return t})();var YN=(()=>{class t extends Tg{_doc=f(de);sanitize(e,i){if(i==null)return null;switch(e){case xn.NONE:return i;case xn.HTML:return Sa(i,`HTML`)?ti(i):yf(this._doc,String(i)).toString();case xn.STYLE:return Sa(i,`Style`)?ti(i):i;case xn.SCRIPT:if(Sa(i,`Script`))return ti(i);throw new q(5200,!1);case xn.URL:return Sa(i,`URL`)?ti(i):Js(String(i));case xn.RESOURCE_URL:if(Sa(i,`ResourceURL`))return ti(i);throw new q(-5201,!1);default:throw new q(5202,!1)}}bypassSecurityTrustHtml(e){return ff(e)}bypassSecurityTrustStyle(e){return gf(e)}bypassSecurityTrustScript(e){return bf(e)}bypassSecurityTrustUrl(e){return vf(e)}bypassSecurityTrustResourceUrl(e){return _f(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var vC=new N(`HIGHCHARTS_LOADER`);var _C=new N(`HIGHCHARTS_ROOT_MODULES`);var yC=new N(`HIGHCHARTS_OPTIONS`);var KN=new N(`HIGHCHARTS_CONFIG`);var xC=new N(`HIGHCHARTS_TIMEOUT`);var QN=(()=>{class t{constructor(){this.highcharts=k(null),this.loader=f(vC),this.globalOptions=f(yC,{optional:!0}),this.globalModules=f(_C,{optional:!0}),this.sharedHighchartsPromise=null,this.moduleLoadCache=new WeakMap}async loadModules(e){if(!e)return;let i=this.moduleLoadCache.get(e);if(i)return i;let a=Promise.allSettled(e()).then(o=>{let r=o.filter(s=>s.status===`rejected`);if(r.length){let s=r.map(({reason:l})=>l instanceof Error?l.message:String(l));throw new Error(`Failed to load Highcharts modules: ${s.join(`; `)}`)}});return this.moduleLoadCache.set(e,a),a.catch(()=>{this.moduleLoadCache.get(e)===a&&this.moduleLoadCache.delete(e)}),a}async ensureSharedHighcharts(){if(!this.sharedHighchartsPromise){let e=(async()=>{let i=await this.loader();return await this.loadModules(this.globalModules),this.globalOptions&&i.setOptions(this.globalOptions),i})();this.sharedHighchartsPromise=e,e.catch(()=>{this.sharedHighchartsPromise===e&&(this.sharedHighchartsPromise=null)})}return this.sharedHighchartsPromise}async load(e){let i=await this.ensureSharedHighcharts();return await this.loadModules(e?.modules),this.highcharts.set(i),i}static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})}}return t})();var XN=(()=>{class t{delay(e){return new Promise(i=>setTimeout(i,e))}getChartFactory(e,i){if(i===`chart`)return e.chart;let o=e[i];if(!o)throw new Error(`Highcharts constructor "${i}" is not available. Did you load the required module?`);return o}createChart(){vt(e=>{let i=this.loadedHighcharts(),a=this.constructorType();if(!i||this.isDestroyed)return;let o=s=>{if(!(s.renderer.forExport||this.isDestroyed))return this.chartInstance.emit(s)},r=this.getChartFactory(i,a)(this.el.nativeElement,it(()=>this.options()),o);this.chart.set(r),e(()=>{this.chart()===r&&this.chart.set(null),r.destroy()})})}keepChartUpToDate(){let e=null;vt(()=>{let i=this.chart(),a=this.update(),o=this.oneToOne(),r=this.options();if(i){if(i!==e){e=i;return}a&&i.update(r,!0,o)}})}async initializeHighcharts(){await this.pendingTasks.run(async()=>{try{let e=await this.highchartsChartService.load(this.relativeConfig),i=this.relativeConfig?.timeout??this.timeout??0;i>0&&await this.delay(i),this.isDestroyed||this.loadedHighcharts.set(e)}catch(e){console.error(`Highcharts failed to load; chart was not created.`,e)}})}constructor(){this.constructorType=zt(`chart`),this.oneToOne=zt(!1),this.options=zt.required(),this.update=xx(!0),this.chartInstance=iu(),this.destroyRef=f(Ae),this.el=f(Y),this.platformId=f(Gi),this.relativeConfig=f(KN,{optional:!0}),this.timeout=f(xC,{optional:!0}),this.highchartsChartService=f(QN),this.pendingTasks=f(xa),this.loadedHighcharts=k(null),this.chart=k(null),this.isDestroyed=!1,!(this.platformId&&Kx(this.platformId))&&(this.destroyRef.onDestroy(()=>{this.isDestroyed=!0}),this.createChart(),this.keepChartUpToDate(),this.initializeHighcharts())}static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵdir=Q({type:t,selectors:[[``,`highchartsChart`,``]],inputs:{constructorType:[1,`constructorType`],oneToOne:[1,`oneToOne`],options:[1,`options`],update:[1,`update`]},outputs:{update:`updateChange`,chartInstance:`chartInstance`}})}}return t})();var CC=(()=>{class t{static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵcmp=H({type:t,selectors:[[`highcharts-chart`]],features:[rl([{directive:XN,inputs:[`constructorType`,`constructorType`,`oneToOne`,`oneToOne`,`options`,`options`,`update`,`update`],outputs:[`chartInstance`,`chartInstance`,`updateChange`,`updateChange`]}])],decls:0,vars:0,template:function(i,a){},encapsulation:2})}}return t})();var ZN=()=>[];var JN=()=>import(`./chunk-hK4WQPXE.js`).then(t=>t.default);function eA(t){return sn([{provide:vC,useValue:t??JN}])}function tA(t){return sn([{provide:yC,useValue:t}])}function nA(t){return sn([{provide:_C,useValue:t}])}function SC(t={}){let n=[eA(t.instance),nA(t.modules??ZN),{provide:xC,useValue:t.timeout}];return t.options&&n.push(tA(t.options)),sn(n)}var wC={providers:[mh(),tg(),Mg(),SC({instance:()=>import(`./chunk-hK4WQPXE.js`).then(t=>t.default),modules:()=>[Promise.all([import(`./chunk-D-b7jU4b.js`).then(()=>import(`./chunk-D2DiX9oW.js`)),import(`./chunk-n0CAvF_C.js`),import(`./chunk-D4T2IOR2.js`),import(`./chunk-tFbWECrw.js`),import(`./chunk-CU-WgeLp.js`)]).then(()=>import(`./chunk-Btwk6sCG.js`))]})]};function Do(t){return t.buttons===0||t.detail===0}function Io(t){let n=t.touches&&t.touches[0]||t.changedTouches&&t.changedTouches[0];return!!n&&n.identifier===-1&&(n.radiusX==null||n.radiusX===1)&&(n.radiusY==null||n.radiusY===1)}var Ng;function EC(){if(Ng==null){let t=typeof document<`u`?document.head:null;Ng=!!(t&&(t.createShadowRoot||t.attachShadow))}return Ng}function Ag(t){if(EC()){let n=t.getRootNode?t.getRootNode():null;if(typeof ShadowRoot<`u`&&ShadowRoot&&n instanceof ShadowRoot)return n}return null}function Dl(){let t=typeof document<`u`&&document?document.activeElement:null;for(;t&&t.shadowRoot;){let n=t.shadowRoot.activeElement;if(n===t)break;t=n}return t}function Yt(t){if(t.composedPath)try{return t.composedPath()[0]}catch{}return t.target}var Rg;try{Rg=typeof Intl<`u`&&Intl.v8BreakIterator}catch{Rg=!1}var Re=(()=>{class t{_platformId=f(Gi);isBrowser=this._platformId?Yx(this._platformId):typeof document==`object`&&!!document;EDGE=this.isBrowser&&/(edge)/i.test(navigator.userAgent);TRIDENT=this.isBrowser&&/(msie|trident)/i.test(navigator.userAgent);BLINK=this.isBrowser&&!!(window.chrome||Rg)&&typeof CSS<`u`&&!this.EDGE&&!this.TRIDENT;WEBKIT=this.isBrowser&&/AppleWebKit/i.test(navigator.userAgent)&&!this.BLINK&&!this.EDGE&&!this.TRIDENT;IOS=this.isBrowser&&/iPad|iPhone|iPod/.test(navigator.userAgent)&&!(`MSStream`in window);FIREFOX=this.isBrowser&&/(firefox|minefield)/i.test(navigator.userAgent);ANDROID=this.isBrowser&&/android/i.test(navigator.userAgent)&&!this.TRIDENT;SAFARI=this.isBrowser&&/safari/i.test(navigator.userAgent)&&this.WEBKIT;static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Il;function DC(){if(Il==null&&typeof window<`u`)try{window.addEventListener(`test`,null,Object.defineProperty({},"passive",{get:()=>Il=!0}))}finally{Il=Il||!1}return Il}function Fr(t){return DC()?t:!!t.capture}function ri(t,n=0){return IC(t)?Number(t):arguments.length===2?n:0}function IC(t){return!isNaN(parseFloat(t))&&!isNaN(Number(t))}function un(t){return t instanceof Y?t.nativeElement:t}var kC=new N(`cdk-input-modality-detector-options`);var MC={ignoreKeys:[18,17,224,91,16]};var TC=650;var Og={passive:!0,capture:!0};var NC=(()=>{class t{_platform=f(Re);_listenerCleanups;modalityDetected;modalityChanged;get mostRecentModality(){return this._modality.value}_mostRecentTarget=null;_modality=new hi(null);_options;_lastTouchMs=0;_onKeydown=e=>{this._options?.ignoreKeys?.some(i=>i===e.keyCode)||(this._modality.next(`keyboard`),this._mostRecentTarget=Yt(e))};_onMousedown=e=>{Date.now()-this._lastTouchMs<TC||(this._modality.next(Do(e)?`keyboard`:`mouse`),this._mostRecentTarget=Yt(e))};_onTouchstart=e=>{if(Io(e)){this._modality.next(`keyboard`);return}this._lastTouchMs=Date.now(),this._modality.next(`touch`),this._mostRecentTarget=Yt(e)};constructor(){let e=f(Z),i=f(de),a=f(kC,{optional:!0});if(this._options=r(r({},MC),a),this.modalityDetected=this._modality.pipe(Qa(1)),this.modalityChanged=this.modalityDetected.pipe(Bc()),this._platform.isBrowser){let o=f(Nt).createRenderer(null,null);this._listenerCleanups=e.runOutsideAngular(()=>[o.listen(i,`keydown`,this._onKeydown,Og),o.listen(i,`mousedown`,this._onMousedown,Og),o.listen(i,`touchstart`,this._onTouchstart,Og)])}}ngOnDestroy(){this._modality.complete(),this._listenerCleanups?.forEach(e=>e())}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var kl=(function(t){return t[t.IMMEDIATE=0]=`IMMEDIATE`,t[t.EVENTUAL=1]=`EVENTUAL`,t})(kl||{});var AC=new N(`cdk-focus-monitor-default-options`);var Du=Fr({passive:!0,capture:!0});var en=(()=>{class t{_ngZone=f(Z);_platform=f(Re);_inputModalityDetector=f(NC);_origin=null;_lastFocusOrigin=null;_windowFocused=!1;_windowFocusTimeoutId;_originTimeoutId;_originFromTouchInteraction=!1;_elementInfo=new Map;_monitoredElementCount=0;_rootNodeFocusListenerCount=new Map;_detectionMode;_windowFocusListener=()=>{this._windowFocused=!0,this._windowFocusTimeoutId=setTimeout(()=>this._windowFocused=!1)};_document=f(de);_stopInputModalityDetector=new V;constructor(){let e=f(AC,{optional:!0});this._detectionMode=e?.detectionMode||kl.IMMEDIATE}_rootNodeFocusAndBlurListener=e=>{let i=Yt(e);for(let a=i;a;a=a.parentElement)e.type===`focus`?this._onFocus(e,a):this._onBlur(e,a)};monitor(e,i=!1){let a=un(e);if(!this._platform.isBrowser||a.nodeType!==1)return Mn();let o=Ag(a)||this._document,r=this._elementInfo.get(a);if(r)return i&&(r.checkChildren=!0),r.subject;let s={checkChildren:i,subject:new V,rootNode:o};return this._elementInfo.set(a,s),this._registerGlobalListeners(s),s.subject}stopMonitoring(e){let i=un(e),a=this._elementInfo.get(i);a&&(a.subject.complete(),this._setClasses(i),this._elementInfo.delete(i),this._removeGlobalListeners(a))}focusVia(e,i,a){let o=un(e);o===this._document.activeElement?this._getClosestElementsInfo(o).forEach(([s,l])=>this._originChanged(s,i,l)):(this._setOrigin(i),typeof o.focus==`function`&&o.focus(a))}ngOnDestroy(){this._elementInfo.forEach((e,i)=>this.stopMonitoring(i))}_getWindow(){return this._document.defaultView||window}_getFocusOrigin(e){return this._origin?this._originFromTouchInteraction?this._shouldBeAttributedToTouch(e)?`touch`:`program`:this._origin:this._windowFocused&&this._lastFocusOrigin?this._lastFocusOrigin:e&&this._isLastInteractionFromInputLabel(e)?`mouse`:`program`}_shouldBeAttributedToTouch(e){return this._detectionMode===kl.EVENTUAL||!!e?.contains(this._inputModalityDetector._mostRecentTarget)}_setClasses(e,i){e.classList.toggle(`cdk-focused`,!!i),e.classList.toggle(`cdk-touch-focused`,i===`touch`),e.classList.toggle(`cdk-keyboard-focused`,i===`keyboard`),e.classList.toggle(`cdk-mouse-focused`,i===`mouse`),e.classList.toggle(`cdk-program-focused`,i===`program`)}_setOrigin(e,i=!1){this._ngZone.runOutsideAngular(()=>{if(this._origin=e,this._originFromTouchInteraction=e===`touch`&&i,this._detectionMode===kl.IMMEDIATE){clearTimeout(this._originTimeoutId);let a=this._originFromTouchInteraction?TC:1;this._originTimeoutId=setTimeout(()=>this._origin=null,a)}})}_onFocus(e,i){let a=this._elementInfo.get(i),o=Yt(e);!a||!a.checkChildren&&i!==o||this._originChanged(i,this._getFocusOrigin(o),a)}_onBlur(e,i){let a=this._elementInfo.get(i);!a||a.checkChildren&&e.relatedTarget instanceof Node&&i.contains(e.relatedTarget)||(this._setClasses(i),this._emitOrigin(a,null))}_emitOrigin(e,i){e.subject.observers.length&&this._ngZone.run(()=>e.subject.next(i))}_registerGlobalListeners(e){if(!this._platform.isBrowser)return;let i=e.rootNode,a=this._rootNodeFocusListenerCount.get(i)||0;a||this._ngZone.runOutsideAngular(()=>{i.addEventListener(`focus`,this._rootNodeFocusAndBlurListener,Du),i.addEventListener(`blur`,this._rootNodeFocusAndBlurListener,Du)}),this._rootNodeFocusListenerCount.set(i,a+1),++this._monitoredElementCount===1&&(this._ngZone.runOutsideAngular(()=>{this._getWindow().addEventListener(`focus`,this._windowFocusListener)}),this._inputModalityDetector.modalityDetected.pipe(Ne(this._stopInputModalityDetector)).subscribe(o=>{this._setOrigin(o,!0)}))}_removeGlobalListeners(e){let i=e.rootNode;if(this._rootNodeFocusListenerCount.has(i)){let a=this._rootNodeFocusListenerCount.get(i);a>1?this._rootNodeFocusListenerCount.set(i,a-1):(i.removeEventListener(`focus`,this._rootNodeFocusAndBlurListener,Du),i.removeEventListener(`blur`,this._rootNodeFocusAndBlurListener,Du),this._rootNodeFocusListenerCount.delete(i))}--this._monitoredElementCount||(this._getWindow().removeEventListener(`focus`,this._windowFocusListener),this._stopInputModalityDetector.next(),clearTimeout(this._windowFocusTimeoutId),clearTimeout(this._originTimeoutId))}_originChanged(e,i,a){this._setClasses(e,i),this._emitOrigin(a,i),this._lastFocusOrigin=i}_getClosestElementsInfo(e){let i=[];return this._elementInfo.forEach((a,o)=>{(o===e||a.checkChildren&&o.contains(e))&&i.push([o,a])}),i}_isLastInteractionFromInputLabel(e){let{_mostRecentTarget:i,mostRecentModality:a}=this._inputModalityDetector;if(a!==`mouse`||!i||i===e||e.nodeName!==`INPUT`&&e.nodeName!==`TEXTAREA`||e.disabled)return!1;let o=e.labels;if(o){for(let r=0;r<o.length;r++)if(o[r].contains(i))return!0}return!1}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Pg=(()=>{class t{_elementRef=f(Y);_focusMonitor=f(en);_monitorSubscription;_focusOrigin=null;cdkFocusChange=new te;get focusOrigin(){return this._focusOrigin}ngAfterViewInit(){let e=this._elementRef.nativeElement;this._monitorSubscription=this._focusMonitor.monitor(e,e.nodeType===1&&e.hasAttribute(`cdkMonitorSubtreeFocus`)).subscribe(i=>{this._focusOrigin=i,this.cdkFocusChange.emit(i)})}ngOnDestroy(){this._focusMonitor.stopMonitoring(this._elementRef),this._monitorSubscription?.unsubscribe()}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdkMonitorElementFocus`,``],[``,`cdkMonitorSubtreeFocus`,``]],outputs:{cdkFocusChange:`cdkFocusChange`},exportAs:[`cdkMonitorFocus`]})}return t})();var Iu=new WeakMap;var Je=(()=>{class t{_appRef;_injector=f(se);_environmentInjector=f(Mt);load(e){let i=this._appRef=this._appRef||this._injector.get(On),a=Iu.get(i);a||(a={loaders:new Set,refs:[]},Iu.set(i,a),i.onDestroy(()=>{Iu.get(i)?.refs.forEach(o=>o.destroy()),Iu.delete(i)})),a.loaders.has(e)||(a.loaders.add(e),a.refs.push(au(e,{environmentInjector:this._environmentInjector})))}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Ii=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`ng-component`]],exportAs:[`cdkVisuallyHidden`],decls:0,vars:0,template:function(i,a){},styles:[`.cdk-visually-hidden {
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
`],encapsulation:2})}return t})();var ku;function iA(){if(ku===void 0&&(ku=null,typeof window<`u`)){let t=window;if(t.trustedTypes!==void 0)try{ku=t.trustedTypes.createPolicy(`angular#components`,{createHTML:n=>n})}catch(n){console.error(n)}}return ku}function aA(t){return iA()?.createHTML(t)||t}function RC(t,n,e){t.innerHTML=aA(e.sanitize(xn.HTML,n)||``)}function Lr(t){return Array.isArray(t)?t:[t]}var OC=new Set;var ko;var Vr=(()=>{class t{_platform=f(Re);_nonce=f(ya,{optional:!0});_matchMedia;constructor(){this._matchMedia=this._platform.isBrowser&&window.matchMedia?window.matchMedia.bind(window):rA}matchMedia(e){return(this._platform.WEBKIT||this._platform.BLINK)&&oA(e,this._nonce),this._matchMedia(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function oA(t,n){if(!OC.has(t))try{ko||(ko=document.createElement(`style`),n&&ko.setAttribute(`nonce`,n),ko.setAttribute(`type`,`text/css`),document.head.appendChild(ko)),ko.sheet&&(ko.sheet.insertRule(`@media ${t.replace(/[{}]/g,``)} {body{ }}`,0),OC.add(t))}catch(e){console.error(e)}}function rA(t){return{matches:t===`all`||t===``,media:t,addListener:()=>{},removeListener:()=>{}}}var Fg=(()=>{class t{_mediaMatcher=f(Vr);_zone=f(Z);_queries=new Map;_destroySubject=new V;ngOnDestroy(){this._destroySubject.next(),this._destroySubject.complete()}isMatched(e){return PC(Lr(e)).some(a=>this._registerQuery(a).mql.matches)}observe(e){let o=cp(PC(Lr(e)).map(r=>this._registerQuery(r).observable));return o=er(o.pipe(vn(1)),o.pipe(Qa(1),Pi(0))),o.pipe(st(r=>{let s={matches:!1,breakpoints:{}};return r.forEach(({matches:l,query:h})=>{s.matches=s.matches||l,s.breakpoints[h]=l}),s}))}_registerQuery(e){if(this._queries.has(e))return this._queries.get(e);let i=this._mediaMatcher.matchMedia(e),o={observable:new Ce(r=>{let s=l=>this._zone.run(()=>r.next(l));return i.addListener(s),()=>{i.removeListener(s)}}).pipe(mt(i),st(({matches:r})=>({query:e,matches:r})),Ne(this._destroySubject)),mql:i};return this._queries.set(e,o),o}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function PC(t){return t.map(n=>n.split(`,`)).reduce((n,e)=>n.concat(e)).map(n=>n.trim())}function sA(t){if(t.type===`characterData`&&t.target instanceof Comment)return!0;if(t.type===`childList`){for(let n=0;n<t.addedNodes.length;n++)if(!(t.addedNodes[n]instanceof Comment))return!1;for(let n=0;n<t.removedNodes.length;n++)if(!(t.removedNodes[n]instanceof Comment))return!1;return!0}return!1}var FC=(()=>{class t{create(e){return typeof MutationObserver>`u`?null:new MutationObserver(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var LC=(()=>{class t{_mutationObserverFactory=f(FC);_observedElements=new Map;_ngZone=f(Z);ngOnDestroy(){this._observedElements.forEach((e,i)=>this._cleanupObserver(i))}observe(e){let i=un(e);return new Ce(a=>{let r=this._observeElement(i).pipe(st(s=>s.filter(l=>!sA(l))),Qe(s=>!!s.length)).subscribe(s=>{this._ngZone.run(()=>{a.next(s)})});return()=>{r.unsubscribe(),this._unobserveElement(i)}})}_observeElement(e){return this._ngZone.runOutsideAngular(()=>{if(this._observedElements.has(e))this._observedElements.get(e).count++;else{let i=new V,a=this._mutationObserverFactory.create(o=>i.next(o));a&&a.observe(e,{characterData:!0,childList:!0,subtree:!0}),this._observedElements.set(e,{observer:a,stream:i,count:1})}return this._observedElements.get(e).stream})}_unobserveElement(e){this._observedElements.has(e)&&(this._observedElements.get(e).count--,this._observedElements.get(e).count||this._cleanupObserver(e))}_cleanupObserver(e){if(this._observedElements.has(e)){let{observer:i,stream:a}=this._observedElements.get(e);i&&i.disconnect(),a.complete(),this._observedElements.delete(e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var VC=(()=>{class t{_contentObserver=f(LC);_elementRef=f(Y);event=new te;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._disabled?this._unsubscribe():this._subscribe()}_disabled=!1;get debounce(){return this._debounce}set debounce(e){this._debounce=ri(e),this._subscribe()}_debounce;_currentSubscription=null;ngAfterContentInit(){!this._currentSubscription&&!this.disabled&&this._subscribe()}ngOnDestroy(){this._unsubscribe()}_subscribe(){this._unsubscribe();let e=this._contentObserver.observe(this._elementRef);this._currentSubscription=(this.debounce?e.pipe(Pi(this.debounce)):e).subscribe(this.event)}_unsubscribe(){this._currentSubscription?.unsubscribe()}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdkObserveContent`,``]],inputs:{disabled:[2,`cdkObserveContentDisabled`,`disabled`,z],debounce:`debounce`},outputs:{event:`cdkObserveContent`},exportAs:[`cdkObserveContent`]})}return t})();var Mu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({providers:[FC]})}return t})();var Br=(()=>{class t{_platform=f(Re);isDisabled(e){return e.hasAttribute(`disabled`)}isVisible(e){return cA(e)&&getComputedStyle(e).visibility===`visible`}isTabbable(e){if(!this._platform.isBrowser)return!1;let i=lA(bA(e));if(i&&(BC(i)===-1||!this.isVisible(i)))return!1;let a=e.nodeName.toLowerCase(),o=BC(e);return e.hasAttribute(`contenteditable`)?o!==-1:a===`iframe`||a===`object`||this._platform.WEBKIT&&this._platform.IOS&&!fA(e)?!1:a===`audio`?e.hasAttribute(`controls`)?o!==-1:!1:a===`video`?o===-1?!1:o!==null?!0:this._platform.FIREFOX||e.hasAttribute(`controls`):e.tabIndex>=0}isFocusable(e,i){return gA(e)&&!this.isDisabled(e)&&(i?.ignoreVisibility||this.isVisible(e))}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function lA(t){try{return t.frameElement}catch{return null}}function cA(t){return!!(t.offsetWidth||t.offsetHeight||typeof t.getClientRects==`function`&&t.getClientRects().length)}function dA(t){let n=t.nodeName.toLowerCase();return n===`input`||n===`select`||n===`button`||n===`textarea`}function uA(t){return pA(t)&&t.type==`hidden`}function mA(t){return hA(t)&&t.hasAttribute(`href`)}function pA(t){return t.nodeName.toLowerCase()==`input`}function hA(t){return t.nodeName.toLowerCase()==`a`}function HC(t){if(!t.hasAttribute(`tabindex`)||t.tabIndex===void 0)return!1;let n=t.getAttribute(`tabindex`);return!!(n&&!isNaN(parseInt(n,10)))}function BC(t){if(!HC(t))return null;let n=parseInt(t.getAttribute(`tabindex`)||``,10);return isNaN(n)?-1:n}function fA(t){let n=t.nodeName.toLowerCase(),e=n===`input`&&t.type;return e===`text`||e===`password`||n===`select`||n===`textarea`}function gA(t){return uA(t)?!1:dA(t)||mA(t)||t.hasAttribute(`contenteditable`)||HC(t)}function bA(t){return t.ownerDocument&&t.ownerDocument.defaultView||window}var Tu=class{_element;_checker;_ngZone;_document;_injector;_startAnchor=null;_endAnchor=null;_hasAttached=!1;startAnchorListener=()=>{!this.focusLastTabbableElement()&&this._checker.isFocusable(this._element)&&this._element.focus()};endAnchorListener=()=>{!this.focusFirstTabbableElement()&&this._checker.isFocusable(this._element)&&this._element.focus()};get enabled(){return this._enabled}set enabled(n){this._enabled=n,this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_enabled=!0;constructor(n,e,i,a,o=!1,r){this._element=n,this._checker=e,this._ngZone=i,this._document=a,this._injector=r,o||this.attachAnchors()}destroy(){let n=this._startAnchor,e=this._endAnchor;n&&(n.removeEventListener(`focus`,this.startAnchorListener),n.remove()),e&&(e.removeEventListener(`focus`,this.endAnchorListener),e.remove()),this._startAnchor=this._endAnchor=null,this._hasAttached=!1}attachAnchors(){return this._hasAttached?!0:(this._ngZone.runOutsideAngular(()=>{this._startAnchor||(this._startAnchor=this._createAnchor(),this._startAnchor.addEventListener(`focus`,this.startAnchorListener)),this._endAnchor||(this._endAnchor=this._createAnchor(),this._endAnchor.addEventListener(`focus`,this.endAnchorListener))}),this._element.parentNode&&(this._element.parentNode.insertBefore(this._startAnchor,this._element),this._element.parentNode.insertBefore(this._endAnchor,this._element.nextSibling),this._hasAttached=!0),this._hasAttached)}focusInitialElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusInitialElement(n)))})}focusFirstTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusFirstTabbableElement(n)))})}focusLastTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusLastTabbableElement(n)))})}_getRegionBoundary(n){let e=this._element.querySelectorAll(`[cdk-focus-region-${n}], [cdkFocusRegion${n}], [cdk-focus-${n}]`);return n==`start`?e.length?e[0]:this._getFirstTabbableElement(this._element):e.length?e[e.length-1]:this._getLastTabbableElement(this._element)}focusInitialElement(n){let e=this._element.querySelector(`[cdk-focus-initial], [cdkFocusInitial]`);if(e){if(!this._checker.isFocusable(e)){let i=this._getFirstTabbableElement(e);return i?.focus(n),!!i}return e.focus(n),!0}return this.focusFirstTabbableElement(n)}focusFirstTabbableElement(n){let e=this._getRegionBoundary(`start`);return e&&e.focus(n),!!e}focusLastTabbableElement(n){let e=this._getRegionBoundary(`end`);return e&&e.focus(n),!!e}hasAttached(){return this._hasAttached}_getFirstTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let i=0;i<e.length;i++){let a=e[i].nodeType===this._document.ELEMENT_NODE?this._getFirstTabbableElement(e[i]):null;if(a)return a}return null}_getLastTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let i=e.length-1;i>=0;i--){let a=e[i].nodeType===this._document.ELEMENT_NODE?this._getLastTabbableElement(e[i]):null;if(a)return a}return null}_createAnchor(){let n=this._document.createElement(`div`);return this._toggleAnchorTabIndex(this._enabled,n),n.classList.add(`cdk-visually-hidden`),n.classList.add(`cdk-focus-trap-anchor`),n.setAttribute(`aria-hidden`,`true`),n}_toggleAnchorTabIndex(n,e){n?e.setAttribute(`tabindex`,`0`):e.removeAttribute(`tabindex`)}toggleAnchors(n){this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_executeOnStable(n){Rt(n,{injector:this._injector})}};var Vg=(()=>{class t{_checker=f(Br);_ngZone=f(Z);_document=f(de);_injector=f(se);constructor(){f(Je).load(Ii)}create(e,i=!1){return new Tu(e,this._checker,this._ngZone,this._document,i,this._injector)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var UC=new N(`liveAnnouncerElement`,{providedIn:`root`,factory:()=>null});var $C=new N(`LIVE_ANNOUNCER_DEFAULT_OPTIONS`);var vA=0;var Bg=(()=>{class t{_ngZone=f(Z);_defaultOptions=f($C,{optional:!0});_liveElement;_document=f(de);_sanitizer=f(Tg);_previousTimeout;_currentPromise;_currentResolve;constructor(){let e=f(UC,{optional:!0});this._liveElement=e||this._createLiveElement()}announce(e,...i){let a=this._defaultOptions,o,r;return i.length===1&&typeof i[0]==`number`?r=i[0]:[o,r]=i,this.clear(),clearTimeout(this._previousTimeout),o||(o=a&&a.politeness?a.politeness:`polite`),r==null&&a&&(r=a.duration),this._liveElement.setAttribute(`aria-live`,o),this._liveElement.id&&this._exposeAnnouncerToModals(this._liveElement.id),this._ngZone.runOutsideAngular(()=>(this._currentPromise||(this._currentPromise=new Promise(s=>this._currentResolve=s)),clearTimeout(this._previousTimeout),this._previousTimeout=setTimeout(()=>{!e||typeof e==`string`?this._liveElement.textContent=e:RC(this._liveElement,e,this._sanitizer),typeof r==`number`&&(this._previousTimeout=setTimeout(()=>this.clear(),r)),this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0},100),this._currentPromise))}clear(){this._liveElement&&(this._liveElement.textContent=``)}ngOnDestroy(){clearTimeout(this._previousTimeout),this._liveElement?.remove(),this._liveElement=null,this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0}_createLiveElement(){let e=`cdk-live-announcer-element`,i=this._document.getElementsByClassName(e),a=this._document.createElement(`div`);for(let o=0;o<i.length;o++)i[o].remove();return a.classList.add(e),a.classList.add(`cdk-visually-hidden`),a.setAttribute(`aria-atomic`,`true`),a.setAttribute(`aria-live`,`polite`),a.id=`cdk-live-announcer-${vA++}`,this._document.body.appendChild(a),a}_exposeAnnouncerToModals(e){let i=this._document.querySelectorAll(`body > .cdk-overlay-container [aria-modal="true"]`);for(let a=0;a<i.length;a++){let o=i[a],r=o.getAttribute(`aria-owns`);r?r.indexOf(e)===-1&&o.setAttribute(`aria-owns`,r+` `+e):o.setAttribute(`aria-owns`,e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Da=(function(t){return t[t.NONE=0]=`NONE`,t[t.BLACK_ON_WHITE=1]=`BLACK_ON_WHITE`,t[t.WHITE_ON_BLACK=2]=`WHITE_ON_BLACK`,t})(Da||{});var jC=`cdk-high-contrast-black-on-white`;var zC=`cdk-high-contrast-white-on-black`;var Lg=`cdk-high-contrast-active`;var GC=(()=>{class t{_platform=f(Re);_hasCheckedHighContrastMode=!1;_document=f(de);_breakpointSubscription;constructor(){this._breakpointSubscription=f(Fg).observe(`(forced-colors: active)`).subscribe(()=>{this._hasCheckedHighContrastMode&&(this._hasCheckedHighContrastMode=!1,this._applyBodyHighContrastModeCssClasses())})}getHighContrastMode(){if(!this._platform.isBrowser)return Da.NONE;let e=this._document.createElement(`div`);e.style.backgroundColor=`rgb(1,2,3)`,e.style.position=`absolute`,this._document.body.appendChild(e);let i=this._document.defaultView||window,a=i&&i.getComputedStyle?i.getComputedStyle(e):null,o=(a&&a.backgroundColor||``).replace(/ /g,``);switch(e.remove(),o){case`rgb(0,0,0)`:case`rgb(45,50,54)`:case`rgb(32,32,32)`:return Da.WHITE_ON_BLACK;case`rgb(255,255,255)`:case`rgb(255,250,239)`:return Da.BLACK_ON_WHITE}return Da.NONE}ngOnDestroy(){this._breakpointSubscription.unsubscribe()}_applyBodyHighContrastModeCssClasses(){if(!this._hasCheckedHighContrastMode&&this._platform.isBrowser&&this._document.body){let e=this._document.body.classList;e.remove(Lg,jC,zC),this._hasCheckedHighContrastMode=!0;let i=this.getHighContrastMode();i===Da.BLACK_ON_WHITE?e.add(Lg,jC):i===Da.WHITE_ON_BLACK&&e.add(Lg,zC)}}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Mo=(()=>{class t{constructor(){f(GC)._applyBodyHighContrastModeCssClasses()}static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[Mu]})}return t})();var _A=200;var Nu=class{_letterKeyStream=new V;_items=[];_selectedItemIndex=-1;_pressedLetters=[];_skipPredicateFn;_selectedItem=new V;selectedItem=this._selectedItem;constructor(n,e){let i=typeof e?.debounceInterval==`number`?e.debounceInterval:_A;e?.skipPredicate&&(this._skipPredicateFn=e.skipPredicate),this.setItems(n),this._setupKeyHandler(i)}destroy(){this._pressedLetters=[],this._letterKeyStream.complete(),this._selectedItem.complete()}setCurrentSelectedItemIndex(n){this._selectedItemIndex=n}setItems(n){this._items=n}handleKey(n){let e=n.keyCode;n.key&&n.key.length===1?this._letterKeyStream.next(n.key.toLocaleUpperCase()):(e>=65&&e<=90||e>=48&&e<=57)&&this._letterKeyStream.next(String.fromCharCode(e))}isTyping(){return this._pressedLetters.length>0}reset(){this._pressedLetters=[]}_setupKeyHandler(n){this._letterKeyStream.pipe(fp(e=>this._pressedLetters.push(e)),Pi(n),Qe(()=>this._pressedLetters.length>0),st(()=>this._pressedLetters.join(``).toLocaleUpperCase())).subscribe(e=>{for(let i=1;i<this._items.length+1;i++){let a=(this._selectedItemIndex+i)%this._items.length,o=this._items[a];if(!this._skipPredicateFn?.(o)&&o.getLabel?.().toLocaleUpperCase().trim().indexOf(e)===0){this._selectedItem.next(o);break}}this._pressedLetters=[]})}};function Ct(t,...n){return n.length?n.some(e=>t[e]):t.altKey||t.shiftKey||t.ctrlKey||t.metaKey}var jr=class{_items;_activeItemIndex=k(-1);_activeItem=k(null);_wrap=!1;_typeaheadSubscription=De.EMPTY;_itemChangesSubscription;_vertical=!0;_horizontal=null;_allowedModifierKeys=[];_homeAndEnd=!1;_pageUpAndDown={enabled:!1,delta:10};_effectRef;_typeahead;_skipPredicateFn=n=>n.disabled;constructor(n,e){this._items=n,n instanceof wn?this._itemChangesSubscription=n.changes.subscribe(i=>this._itemsChanged(i.toArray())):ln(n)&&(this._effectRef=vt(()=>this._itemsChanged(n()),{injector:e}))}tabOut=new V;change=new V;skipPredicate(n){return this._skipPredicateFn=n,this}withWrap(n=!0){return this._wrap=n,this}withVerticalOrientation(n=!0){return this._vertical=n,this}withHorizontalOrientation(n){return this._horizontal=n,this}withAllowedModifierKeys(n){return this._allowedModifierKeys=n,this}withTypeAhead(n=200){this._typeaheadSubscription.unsubscribe();let e=this._getItemsArray();return this._typeahead=new Nu(e,{debounceInterval:typeof n==`number`?n:void 0,skipPredicate:i=>this._skipPredicateFn(i)}),this._typeaheadSubscription=this._typeahead.selectedItem.subscribe(i=>{this.setActiveItem(i)}),this}cancelTypeahead(){return this._typeahead?.reset(),this}withHomeAndEnd(n=!0){return this._homeAndEnd=n,this}withPageUpDown(n=!0,e=10){return this._pageUpAndDown={enabled:n,delta:e},this}setActiveItem(n){let e=this._activeItem();this.updateActiveItem(n),this._activeItem()!==e&&this.change.next(this._activeItemIndex())}onKeydown(n){let e=n.keyCode,a=[`altKey`,`ctrlKey`,`metaKey`,`shiftKey`].every(o=>!n[o]||this._allowedModifierKeys.indexOf(o)>-1);switch(e){case 9:this.tabOut.next();return;case 40:if(this._vertical&&a){this.setNextItemActive();break}else return;case 38:if(this._vertical&&a){this.setPreviousItemActive();break}else return;case 39:if(this._horizontal&&a){this._horizontal===`rtl`?this.setPreviousItemActive():this.setNextItemActive();break}else return;case 37:if(this._horizontal&&a){this._horizontal===`rtl`?this.setNextItemActive():this.setPreviousItemActive();break}else return;case 36:if(this._homeAndEnd&&a){this.setFirstItemActive();break}else return;case 35:if(this._homeAndEnd&&a){this.setLastItemActive();break}else return;case 33:if(this._pageUpAndDown.enabled&&a){let o=this._activeItemIndex()-this._pageUpAndDown.delta;this._setActiveItemByIndex(o>0?o:0,1);break}else return;case 34:if(this._pageUpAndDown.enabled&&a){let o=this._activeItemIndex()+this._pageUpAndDown.delta,r=this._getItemsArray().length;this._setActiveItemByIndex(o<r?o:r-1,-1);break}else return;default:(a||Ct(n,`shiftKey`))&&this._typeahead?.handleKey(n);return}this._typeahead?.reset(),n.preventDefault()}get activeItemIndex(){return this._activeItemIndex()}get activeItem(){return this._activeItem()}isTyping(){return!!this._typeahead&&this._typeahead.isTyping()}setFirstItemActive(){this._setActiveItemByIndex(0,1)}setLastItemActive(){this._setActiveItemByIndex(this._getItemsArray().length-1,-1)}setNextItemActive(){this._activeItemIndex()<0?this.setFirstItemActive():this._setActiveItemByDelta(1)}setPreviousItemActive(){this._activeItemIndex()<0&&this._wrap?this.setLastItemActive():this._setActiveItemByDelta(-1)}updateActiveItem(n){let e=this._getItemsArray(),i=typeof n==`number`?n:e.indexOf(n),a=e[i];this._activeItem.set(a??null),this._activeItemIndex.set(i),this._typeahead?.setCurrentSelectedItemIndex(i)}destroy(){this._typeaheadSubscription.unsubscribe(),this._itemChangesSubscription?.unsubscribe(),this._effectRef?.destroy(),this._typeahead?.destroy(),this.tabOut.complete(),this.change.complete()}_setActiveItemByDelta(n){this._wrap?this._setActiveInWrapMode(n):this._setActiveInDefaultMode(n)}_setActiveInWrapMode(n){let e=this._getItemsArray();for(let i=1;i<=e.length;i++){let a=(this._activeItemIndex()+n*i+e.length)%e.length,o=e[a];if(!this._skipPredicateFn(o)){this.setActiveItem(a);return}}}_setActiveInDefaultMode(n){this._setActiveItemByIndex(this._activeItemIndex()+n,n)}_setActiveItemByIndex(n,e){let i=this._getItemsArray();if(i[n]){for(;this._skipPredicateFn(i[n]);)if(n+=e,!i[n])return;this.setActiveItem(n)}}_getItemsArray(){return ln(this._items)?this._items():this._items instanceof wn?this._items.toArray():this._items}_itemsChanged(n){this._typeahead?.setItems(n);let e=this._activeItem();if(e){let i=n.indexOf(e);i>-1&&i!==this._activeItemIndex()&&(this._activeItemIndex.set(i),this._typeahead?.setCurrentSelectedItemIndex(i))}}};var Ml=class extends jr{setActiveItem(n){this.activeItem&&this.activeItem.setInactiveStyles(),super.setActiveItem(n),this.activeItem&&this.activeItem.setActiveStyles()}};var ea=class extends jr{_origin=`program`;setFocusOrigin(n){return this._origin=n,this}setActiveItem(n){super.setActiveItem(n),this.activeItem&&this.activeItem.focus(this._origin)}};var qC=new Map;var Be=class t{_appId=f($i);static _infix=`a${Math.floor(Math.random()*1e5).toString()}`;getId(n,e=!1){this._appId!==`ng`&&(n+=this._appId);let i=qC.get(n);return i===void 0?i=0:i++,qC.set(n,i),`${n}${e?t._infix+`-`:``}${i}`}static ɵfac=function(e){return new(e||t)};static ɵprov=le({token:t,factory:t.ɵfac})};var KC=` `;function yA(t,n,e){let i=Ru(t,n);e=e.trim(),!i.some(a=>a.trim()===e)&&(i.push(e),t.setAttribute(n,i.join(KC)))}function xA(t,n,e){let i=Ru(t,n);e=e.trim();let a=i.filter(o=>o!==e);a.length?t.setAttribute(n,a.join(KC)):t.removeAttribute(n)}function Ru(t,n){return t.getAttribute(n)?.match(/\S+/g)??[]}var QC=`cdk-describedby-message`;var Au=`cdk-describedby-host`;var zg=0;var Ou=(()=>{class t{_platform=f(Re);_document=f(de);_messageRegistry=new Map;_messagesContainer=null;_id=`${zg++}`;constructor(){f(Je).load(Ii),this._id=f($i)+`-`+zg++}describe(e,i,a){if(!this._canBeDescribed(e,i))return;let o=jg(i,a);typeof i!=`string`?(YC(i,this._id),this._messageRegistry.set(o,{messageElement:i,referenceCount:0})):this._messageRegistry.has(o)||this._createMessageElement(i,a),this._isElementDescribedByMessage(e,o)||this._addMessageReference(e,o)}removeDescription(e,i,a){if(!i||!this._isElementNode(e))return;let o=jg(i,a);if(this._isElementDescribedByMessage(e,o)&&this._removeMessageReference(e,o),typeof i==`string`){let r=this._messageRegistry.get(o);r&&r.referenceCount===0&&this._deleteMessageElement(o)}this._messagesContainer?.childNodes.length===0&&(this._messagesContainer.remove(),this._messagesContainer=null)}ngOnDestroy(){let e=this._document.querySelectorAll(`[${Au}="${this._id}"]`);for(let i=0;i<e.length;i++)this._removeCdkDescribedByReferenceIds(e[i]),e[i].removeAttribute(Au);this._messagesContainer?.remove(),this._messagesContainer=null,this._messageRegistry.clear()}_createMessageElement(e,i){let a=this._document.createElement(`div`);YC(a,this._id),a.textContent=e,i&&a.setAttribute(`role`,i),this._createMessagesContainer(),this._messagesContainer.appendChild(a),this._messageRegistry.set(jg(e,i),{messageElement:a,referenceCount:0})}_deleteMessageElement(e){this._messageRegistry.get(e)?.messageElement?.remove(),this._messageRegistry.delete(e)}_createMessagesContainer(){if(this._messagesContainer)return;let e=`cdk-describedby-message-container`,i=this._document.querySelectorAll(`.${e}[platform="server"]`);for(let o=0;o<i.length;o++)i[o].remove();let a=this._document.createElement(`div`);a.style.visibility=`hidden`,a.classList.add(e),a.classList.add(`cdk-visually-hidden`),this._platform.isBrowser||a.setAttribute(`platform`,`server`),this._document.body.appendChild(a),this._messagesContainer=a}_removeCdkDescribedByReferenceIds(e){let i=Ru(e,`aria-describedby`).filter(a=>a.indexOf(QC)!=0);e.setAttribute(`aria-describedby`,i.join(` `))}_addMessageReference(e,i){let a=this._messageRegistry.get(i);yA(e,`aria-describedby`,a.messageElement.id),e.setAttribute(Au,this._id),a.referenceCount++}_removeMessageReference(e,i){let a=this._messageRegistry.get(i);a.referenceCount--,xA(e,`aria-describedby`,a.messageElement.id),e.removeAttribute(Au)}_isElementDescribedByMessage(e,i){let a=Ru(e,`aria-describedby`),o=this._messageRegistry.get(i),r=o&&o.messageElement.id;return!!r&&a.indexOf(r)!=-1}_canBeDescribed(e,i){if(!this._isElementNode(e))return!1;if(i&&typeof i==`object`)return!0;let a=i==null?``:`${i}`.trim(),o=e.getAttribute(`aria-label`);return a?!o||o.trim()!==a:!1}_isElementNode(e){return e.nodeType===this._document.ELEMENT_NODE}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function jg(t,n){return typeof t==`string`?`${n||``}/${t}`:t}function YC(t,n){t.id||(t.id=`${QC}-${n}-${zg++}`)}var si=(function(t){return t[t.NORMAL=0]=`NORMAL`,t[t.NEGATED=1]=`NEGATED`,t[t.INVERTED=2]=`INVERTED`,t})(si||{});var Pu;var Oo;function Fu(){if(Oo==null){if(typeof document!=`object`||!document||typeof Element!=`function`||!Element)return Oo=!1,Oo;if(document.documentElement?.style&&`scrollBehavior`in document.documentElement.style)Oo=!0;else{let t=Element.prototype.scrollTo;t?Oo=!/\{\s*\[native code\]\s*\}/.test(t.toString()):Oo=!1}}return Oo}function zr(){if(typeof document!=`object`||!document)return si.NORMAL;if(Pu==null){let t=document.createElement(`div`),n=t.style;t.dir=`rtl`,n.width=`1px`,n.overflow=`auto`,n.visibility=`hidden`,n.pointerEvents=`none`,n.position=`absolute`;let e=document.createElement(`div`),i=e.style;i.width=`2px`,i.height=`1px`,t.appendChild(e),document.body.appendChild(t),Pu=si.NORMAL,t.scrollLeft===0&&(t.scrollLeft=1,Pu=t.scrollLeft===0?si.NEGATED:si.INVERTED),t.remove()}return Pu}function Hg(){return typeof __karma__<`u`&&!!__karma__||typeof jasmine<`u`&&!!jasmine||typeof jest<`u`&&!!jest||typeof Mocha<`u`&&!!Mocha}var Hr;var XC=[`color`,`button`,`checkbox`,`date`,`datetime-local`,`email`,`file`,`hidden`,`image`,`month`,`number`,`password`,`radio`,`range`,`reset`,`search`,`submit`,`tel`,`text`,`time`,`url`,`week`];function Ug(){if(Hr)return Hr;if(typeof document!=`object`||!document)return Hr=new Set(XC),Hr;let t=document.createElement(`input`);return Hr=new Set(XC.filter(n=>(t.setAttribute(`type`,n),t.type===n))),Hr}var CA=new N(`MATERIAL_ANIMATIONS`);var ZC=null;function Tl(){return f(CA,{optional:!0})?.animationsDisabled||f(js,{optional:!0})===`NoopAnimations`?`di-disabled`:(ZC??=f(Vr).matchMedia(`(prefers-reduced-motion)`).matches,ZC?`reduced-motion`:`enabled`)}function Ue(){return Tl()!==`enabled`}function It(t){return t==null?``:typeof t==`string`?t:`${t}px`}function ta(t){return t!=null&&`${t}`!=`false`}var Vn=(function(t){return t[t.FADING_IN=0]=`FADING_IN`,t[t.VISIBLE=1]=`VISIBLE`,t[t.FADING_OUT=2]=`FADING_OUT`,t[t.HIDDEN=3]=`HIDDEN`,t})(Vn||{});var $g=class{_renderer;element;config;_animationForciblyDisabledThroughCss;state=Vn.HIDDEN;constructor(n,e,i,a=!1){this._renderer=n,this.element=e,this.config=i,this._animationForciblyDisabledThroughCss=a}fadeOut(){this._renderer.fadeOutRipple(this)}};var JC=Fr({passive:!0,capture:!0});var Gg=class{_events=new Map;addHandler(n,e,i,a){let o=this._events.get(e);if(o){let r=o.get(i);r?r.add(a):o.set(i,new Set([a]))}else this._events.set(e,new Map([[i,new Set([a])]])),n.runOutsideAngular(()=>{document.addEventListener(e,this._delegateEventHandler,JC)})}removeHandler(n,e,i){let a=this._events.get(n);if(!a)return;let o=a.get(e);o&&(o.delete(i),o.size===0&&a.delete(e),a.size===0&&(this._events.delete(n),document.removeEventListener(n,this._delegateEventHandler,JC)))}_delegateEventHandler=n=>{let e=Yt(n);e&&this._events.get(n.type)?.forEach((i,a)=>{(a===e||a.contains(e))&&i.forEach(o=>o.handleEvent(n))})}};var Nl={enterDuration:225,exitDuration:150};var SA=800;var eS=Fr({passive:!0,capture:!0});var tS=[`mousedown`,`touchstart`];var nS=[`mouseup`,`mouseleave`,`touchend`,`touchcancel`];var wA=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`ng-component`]],hostAttrs:[`mat-ripple-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`.mat-ripple {
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
`],encapsulation:2})}return t})();var Al=class t{_target;_ngZone;_platform;_containerElement;_triggerElement=null;_isPointerDown=!1;_activeRipples=new Map;_mostRecentTransientRipple=null;_lastTouchStartEvent;_pointerUpEventsRegistered=!1;_containerRect=null;static _eventManager=new Gg;constructor(n,e,i,a,o){this._target=n,this._ngZone=e,this._platform=a,a.isBrowser&&(this._containerElement=un(i)),o&&o.get(Je).load(wA)}fadeInRipple(n,e,i={}){let a=this._containerRect=this._containerRect||this._containerElement.getBoundingClientRect(),o=r(r({},Nl),i.animation);i.centered&&(n=a.left+a.width/2,e=a.top+a.height/2);let r$2=i.radius||EA(n,e,a),s=n-a.left,l=e-a.top,h=o.enterDuration,p=document.createElement(`div`);p.classList.add(`mat-ripple-element`),p.style.left=`${s-r$2}px`,p.style.top=`${l-r$2}px`,p.style.height=`${r$2*2}px`,p.style.width=`${r$2*2}px`,i.color!=null&&(p.style.backgroundColor=i.color),p.style.transitionDuration=`${h}ms`,this._containerElement.appendChild(p);let g=window.getComputedStyle(p),_=g.transitionProperty,b=g.transitionDuration,y=_===`none`||b===`0s`||b===`0s, 0s`||a.width===0&&a.height===0,I=new $g(this,p,i,y);p.style.transform=`scale3d(1, 1, 1)`,I.state=Vn.FADING_IN,i.persistent||(this._mostRecentTransientRipple=I);let P=null;return!y&&(h||o.exitDuration)&&this._ngZone.runOutsideAngular(()=>{let ie=()=>{P&&(P.fallbackTimer=null),clearTimeout(x),this._finishRippleTransition(I)},L=()=>this._destroyRipple(I),x=setTimeout(L,h+100);p.addEventListener(`transitionend`,ie),p.addEventListener(`transitioncancel`,L),P={onTransitionEnd:ie,onTransitionCancel:L,fallbackTimer:x}}),this._activeRipples.set(I,P),(y||!h)&&this._finishRippleTransition(I),I}fadeOutRipple(n){if(n.state===Vn.FADING_OUT||n.state===Vn.HIDDEN)return;let e=n.element,i=r(r({},Nl),n.config.animation);e.style.transitionDuration=`${i.exitDuration}ms`,e.style.opacity=`0`,n.state=Vn.FADING_OUT,(n._animationForciblyDisabledThroughCss||!i.exitDuration)&&this._finishRippleTransition(n)}fadeOutAll(){this._getActiveRipples().forEach(n=>n.fadeOut())}fadeOutAllNonPersistent(){this._getActiveRipples().forEach(n=>{n.config.persistent||n.fadeOut()})}setupTriggerEvents(n){let e=un(n);!this._platform.isBrowser||!e||e===this._triggerElement||(this._removeTriggerEvents(),this._triggerElement=e,tS.forEach(i=>{t._eventManager.addHandler(this._ngZone,i,e,this)}))}handleEvent(n){n.type===`mousedown`?this._onMousedown(n):n.type===`touchstart`?this._onTouchStart(n):this._onPointerUp(),this._pointerUpEventsRegistered||(this._ngZone.runOutsideAngular(()=>{nS.forEach(e=>{this._triggerElement.addEventListener(e,this,eS)})}),this._pointerUpEventsRegistered=!0)}_finishRippleTransition(n){n.state===Vn.FADING_IN?this._startFadeOutTransition(n):n.state===Vn.FADING_OUT&&this._destroyRipple(n)}_startFadeOutTransition(n){let e=n===this._mostRecentTransientRipple,{persistent:i}=n.config;n.state=Vn.VISIBLE,!i&&(!e||!this._isPointerDown)&&n.fadeOut()}_destroyRipple(n){let e=this._activeRipples.get(n)??null;this._activeRipples.delete(n),this._activeRipples.size||(this._containerRect=null),n===this._mostRecentTransientRipple&&(this._mostRecentTransientRipple=null),n.state=Vn.HIDDEN,e!==null&&(n.element.removeEventListener(`transitionend`,e.onTransitionEnd),n.element.removeEventListener(`transitioncancel`,e.onTransitionCancel),e.fallbackTimer!==null&&clearTimeout(e.fallbackTimer)),n.element.remove()}_onMousedown(n){let e=Do(n),i=this._lastTouchStartEvent&&Date.now()<this._lastTouchStartEvent+SA;!this._target.rippleDisabled&&!e&&!i&&(this._isPointerDown=!0,this.fadeInRipple(n.clientX,n.clientY,this._target.rippleConfig))}_onTouchStart(n){if(!this._target.rippleDisabled&&!Io(n)){this._lastTouchStartEvent=Date.now(),this._isPointerDown=!0;let e=n.changedTouches;if(e)for(let i=0;i<e.length;i++)this.fadeInRipple(e[i].clientX,e[i].clientY,this._target.rippleConfig)}}_onPointerUp(){this._isPointerDown&&(this._isPointerDown=!1,this._getActiveRipples().forEach(n=>{let e=n.state===Vn.VISIBLE||n.config.terminateOnPointerUp&&n.state===Vn.FADING_IN;!n.config.persistent&&e&&n.fadeOut()}))}_getActiveRipples(){return Array.from(this._activeRipples.keys())}_removeTriggerEvents(){let n=this._triggerElement;n&&(tS.forEach(e=>t._eventManager.removeHandler(e,n,this)),this._pointerUpEventsRegistered&&(nS.forEach(e=>n.removeEventListener(e,this,eS)),this._pointerUpEventsRegistered=!1))}};function EA(t,n,e){let i=Math.max(Math.abs(t-e.left),Math.abs(t-e.right)),a=Math.max(Math.abs(n-e.top),Math.abs(n-e.bottom));return Math.sqrt(i*i+a*a)}var Ur=new N(`mat-ripple-global-options`);var li=(()=>{class t{_elementRef=f(Y);_animationsDisabled=Ue();color;unbounded=!1;centered=!1;radius=0;animation;get disabled(){return this._disabled}set disabled(e){e&&this.fadeOutAllNonPersistent(),this._disabled=e,this._setupTriggerEventsIfEnabled()}_disabled=!1;get trigger(){return this._trigger||this._elementRef.nativeElement}set trigger(e){this._trigger=e,this._setupTriggerEventsIfEnabled()}_trigger;_rippleRenderer;_globalOptions;_isInitialized=!1;constructor(){let e=f(Z),i=f(Re),a=f(Ur,{optional:!0}),o=f(se);this._globalOptions=a||{},this._rippleRenderer=new Al(this,e,this._elementRef,i,o)}ngOnInit(){this._isInitialized=!0,this._setupTriggerEventsIfEnabled()}ngOnDestroy(){this._rippleRenderer._removeTriggerEvents()}fadeOutAll(){this._rippleRenderer.fadeOutAll()}fadeOutAllNonPersistent(){this._rippleRenderer.fadeOutAllNonPersistent()}get rippleConfig(){return{centered:this.centered,radius:this.radius,color:this.color,animation:r(r(r({},this._globalOptions.animation),this._animationsDisabled?{enterDuration:0,exitDuration:0}:{}),this.animation),terminateOnPointerUp:this._globalOptions.terminateOnPointerUp}}get rippleDisabled(){return this.disabled||!!this._globalOptions.disabled}_setupTriggerEventsIfEnabled(){!this.disabled&&this._isInitialized&&this._rippleRenderer.setupTriggerEvents(this.trigger)}launch(e,i=0,a){return typeof e==`number`?this._rippleRenderer.fadeInRipple(e,i,r(r({},this.rippleConfig),a)):this._rippleRenderer.fadeInRipple(0,0,r(r({},this.rippleConfig),e))}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`mat-ripple`,``],[``,`matRipple`,``]],hostAttrs:[1,`mat-ripple`],hostVars:2,hostBindings:function(i,a){i&2&&G(`mat-ripple-unbounded`,a.unbounded)},inputs:{color:[0,`matRippleColor`,`color`],unbounded:[0,`matRippleUnbounded`,`unbounded`],centered:[0,`matRippleCentered`,`centered`],radius:[0,`matRippleRadius`,`radius`],animation:[0,`matRippleAnimation`,`animation`],disabled:[0,`matRippleDisabled`,`disabled`],trigger:[0,`matRippleTrigger`,`trigger`]},exportAs:[`matRipple`]})}return t})();var DA={capture:!0};var IA=[`focus`,`mousedown`,`mouseenter`,`touchstart`];var Wg=`mat-ripple-loader-uninitialized`;var qg=`mat-ripple-loader-class-name`;var iS=`mat-ripple-loader-centered`;var Lu=`mat-ripple-loader-disabled`;var Vu=(()=>{class t{_document=f(de);_animationsDisabled=Ue();_globalRippleOptions=f(Ur,{optional:!0});_platform=f(Re);_ngZone=f(Z);_injector=f(se);_eventCleanups;_hosts=new Map;constructor(){let e=f(Nt).createRenderer(null,null);this._eventCleanups=this._ngZone.runOutsideAngular(()=>IA.map(i=>e.listen(this._document,i,this._onInteraction,DA)))}ngOnDestroy(){let e=this._hosts.keys();for(let i of e)this.destroyRipple(i);this._eventCleanups.forEach(i=>i())}configureRipple(e,i){e.setAttribute(Wg,this._globalRippleOptions?.namespace??``),(i.className||!e.hasAttribute(qg))&&e.setAttribute(qg,i.className||``),i.centered&&e.setAttribute(iS,``),i.disabled&&e.setAttribute(Lu,``)}setDisabled(e,i){let a=this._hosts.get(e);a?(a.target.rippleDisabled=i,!i&&!a.hasSetUpEvents&&(a.hasSetUpEvents=!0,a.renderer.setupTriggerEvents(e))):i?e.setAttribute(Lu,``):e.removeAttribute(Lu)}_onInteraction=e=>{let i=Yt(e);if(i instanceof HTMLElement){let a=i.closest(`[${Wg}="${this._globalRippleOptions?.namespace??``}"]`);a&&this._createRipple(a)}};_createRipple(e){if(!this._document||this._hosts.has(e))return;e.querySelector(`.mat-ripple`)?.remove();let i=this._document.createElement(`span`);i.classList.add(`mat-ripple`,e.getAttribute(qg)),e.append(i);let a=this._globalRippleOptions,o=this._animationsDisabled?0:a?.animation?.enterDuration??Nl.enterDuration,r=this._animationsDisabled?0:a?.animation?.exitDuration??Nl.exitDuration,s={rippleDisabled:this._animationsDisabled||a?.disabled||e.hasAttribute(Lu),rippleConfig:{centered:e.hasAttribute(iS),terminateOnPointerUp:a?.terminateOnPointerUp,animation:{enterDuration:o,exitDuration:r}}},l=new Al(s,this._ngZone,i,this._platform,this._injector),h=!s.rippleDisabled;h&&l.setupTriggerEvents(e),this._hosts.set(e,{target:s,renderer:l,hasSetUpEvents:h}),e.removeAttribute(Wg)}destroyRipple(e){let i=this._hosts.get(e);i&&(i.renderer._removeTriggerEvents(),this._hosts.delete(e))}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var mn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`structural-styles`]],decls:0,vars:0,template:function(i,a){},styles:[`.mat-focus-indicator {
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
`],encapsulation:2})}return t})();var kA=new N(`MAT_BUTTON_CONFIG`);function aS(t){return t==null?void 0:ct(t)}var Yg=(()=>{class t{_elementRef=f(Y);_ngZone=f(Z);_animationsDisabled=Ue();_config=f(kA,{optional:!0});_focusMonitor=f(en);_cleanupClick;_renderer=f(Oe);_rippleLoader=f(Vu);_isAnchor;_isFab=!1;color;get disableRipple(){return this._disableRipple}set disableRipple(e){this._disableRipple=e,this._updateRippleDisabled()}_disableRipple=!1;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._updateRippleDisabled()}_disabled=!1;ariaDisabled;disabledInteractive;tabIndex;set _tabindex(e){this.tabIndex=e}showProgress=zt(!1,{transform:z});constructor(){f(Je).load(mn);let e=this._elementRef.nativeElement;this._isAnchor=e.tagName===`A`,this.disabledInteractive=this._config?.disabledInteractive??!1,this.color=this._config?.color??null,this._rippleLoader?.configureRipple(e,{className:`mat-mdc-button-ripple`})}ngAfterViewInit(){this._focusMonitor.monitor(this._elementRef,!0),this._isAnchor&&this._setupAsAnchor()}ngOnDestroy(){this._cleanupClick?.(),this._focusMonitor.stopMonitoring(this._elementRef),this._rippleLoader?.destroyRipple(this._elementRef.nativeElement)}focus(e=`program`,i){e?this._focusMonitor.focusVia(this._elementRef.nativeElement,e,i):this._elementRef.nativeElement.focus(i)}_getAriaDisabled(){return this.ariaDisabled!=null?this.ariaDisabled:this._isAnchor?this.disabled||null:this.disabled&&this.disabledInteractive?!0:null}_getDisabledAttribute(){return this.disabledInteractive||!this.disabled?null:!0}_updateRippleDisabled(){this._rippleLoader?.setDisabled(this._elementRef.nativeElement,this.disableRipple||this.disabled)}_getTabIndex(){return this._isAnchor?this.disabled&&!this.disabledInteractive?-1:this.tabIndex:this.tabIndex}_setupAsAnchor(){this._cleanupClick=this._ngZone.runOutsideAngular(()=>this._renderer.listen(this._elementRef.nativeElement,`click`,e=>{this.disabled&&(e.preventDefault(),e.stopImmediatePropagation())}))}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,hostAttrs:[1,`mat-mdc-button-base`],hostVars:15,hostBindings:function(i,a){i&2&&(ne(`disabled`,a._getDisabledAttribute())(`aria-disabled`,a._getAriaDisabled())(`tabindex`,a._getTabIndex()),tt(a.color?`mat-`+a.color:``),G(`mat-mdc-button-progress-indicator-shown`,a.showProgress())(`mat-mdc-button-disabled`,a.disabled)(`mat-mdc-button-disabled-interactive`,a.disabledInteractive)(`mat-unthemed`,!a.color)(`_mat-animation-noopable`,a._animationsDisabled))},inputs:{color:`color`,disableRipple:[2,`disableRipple`,`disableRipple`,z],disabled:[2,`disabled`,`disabled`,z],ariaDisabled:[2,`aria-disabled`,`ariaDisabled`,z],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z],tabIndex:[2,`tabIndex`,`tabIndex`,aS],_tabindex:[2,`tabindex`,`_tabindex`,aS],showProgress:[1,`showProgress`]}})}return t})();var Kg=(()=>{class t extends Yg{constructor(){super(),this._rippleLoader.configureRipple(this._elementRef.nativeElement,{centered:!0})}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[``,`progressIndicator`,``]]],i=[`*`,`[progressIndicator]`];function a(o,r){o&1&&(lt(0,`div`,1),he(1,1),ht())}return H({type:t,selectors:[[`button`,`mat-icon-button`,``],[`a`,`mat-icon-button`,``],[`button`,`matIconButton`,``],[`a`,`matIconButton`,``]],hostAttrs:[1,`mdc-icon-button`,`mat-mdc-icon-button`],exportAs:[`matButton`,`matAnchor`],features:[Ve],ngContentSelectors:i,decls:5,vars:1,consts:[[1,`mat-mdc-button-persistent-ripple`,`mdc-icon-button__ripple`],[1,`mat-mdc-button-progress-indicator-container`],[1,`mat-focus-indicator`],[1,`mat-mdc-button-touch-target`]],template:function(r,s){r&1&&(Pe(e),Wt(0,`span`,0),he(1),E(2,a,2,0,`div`,1),Wt(3,`span`,2)(4,`span`,3)),r&2&&(m(2),D(s.showProgress()?2:-1))},styles:[`.mat-mdc-icon-button {
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
`],encapsulation:2})})()}return t})();var MA=new N(`cdk-dir-doc`,{providedIn:`root`,factory:()=>f(de)});var TA=/^(ar|ckb|dv|he|iw|fa|nqo|ps|sd|ug|ur|yi|.*[-_](Adlm|Arab|Hebr|Nkoo|Rohg|Thaa))(?!.*[-_](Latn|Cyrl)($|-|_))($|-|_)/i;function oS(t){let n=t?.toLowerCase()||``;return n===`auto`&&typeof navigator<`u`&&navigator?.language?TA.test(navigator.language)?`rtl`:`ltr`:n===`rtl`?`rtl`:`ltr`}var gt=(()=>{class t{get value(){return this.valueSignal()}valueSignal=k(`ltr`);change=new te;constructor(){let e=f(MA,{optional:!0});if(e){let i=e.body?e.body.dir:null,a=e.documentElement?e.documentElement.dir:null;this.valueSignal.set(oS(i||a||`ltr`))}}ngOnDestroy(){this.change.complete()}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var we=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({})}return t})();var ki=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[we]})}return t})();var rS=new Map([[`text`,[`mat-mdc-button`]],[`filled`,[`mdc-button--unelevated`,`mat-mdc-unelevated-button`]],[`elevated`,[`mdc-button--raised`,`mat-mdc-raised-button`]],[`outlined`,[`mdc-button--outlined`,`mat-mdc-outlined-button`]],[`tonal`,[`mat-tonal-button`]]]);var tn=(()=>{class t extends Yg{get appearance(){return this._appearance}set appearance(e){this.setAppearance(e||this._config?.defaultAppearance||`text`)}_appearance=null;constructor(){super();let e=NA(this._elementRef.nativeElement);e&&this.setAppearance(e)}setAppearance(e){if(e===this._appearance)return;let i=this._elementRef.nativeElement.classList,a=this._appearance?rS.get(this._appearance):null,o=rS.get(e);a&&i.remove(...a),i.add(...o),this._appearance=e}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[``,8,`material-icons`,3,`iconPositionEnd`,``],[`mat-icon`,3,`iconPositionEnd`,``],[``,`matButtonIcon`,``,3,`iconPositionEnd`,``],[``,8,`material-symbols-outlined`,3,`iconPositionEnd`,``],[``,8,`material-symbols-rounded`,3,`iconPositionEnd`,``],[``,8,`material-symbols-sharp`,3,`iconPositionEnd`,``]],`*`,[[``,`iconPositionEnd`,``,8,`material-icons`],[`mat-icon`,`iconPositionEnd`,``],[``,`matButtonIcon`,``,`iconPositionEnd`,``],[``,`iconPositionEnd`,``,8,`material-symbols-outlined`],[``,`iconPositionEnd`,``,8,`material-symbols-rounded`],[``,`iconPositionEnd`,``,8,`material-symbols-sharp`]],[[``,`progressIndicator`,``]]],i=[`.material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd]), .material-symbols-outlined:not([iconPositionEnd]), .material-symbols-rounded:not([iconPositionEnd]), .material-symbols-sharp:not([iconPositionEnd])`,`*`,`.material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd], .material-symbols-outlined[iconPositionEnd], .material-symbols-rounded[iconPositionEnd], .material-symbols-sharp[iconPositionEnd]`,`[progressIndicator]`];function a(o,r){o&1&&(lt(0,`div`,2),he(1,3),ht())}return H({type:t,selectors:[[`button`,`matButton`,``],[`a`,`matButton`,``],[`button`,`mat-button`,``],[`button`,`mat-raised-button`,``],[`button`,`mat-flat-button`,``],[`button`,`mat-stroked-button`,``],[`a`,`mat-button`,``],[`a`,`mat-raised-button`,``],[`a`,`mat-flat-button`,``],[`a`,`mat-stroked-button`,``]],hostAttrs:[1,`mdc-button`],inputs:{appearance:[0,`matButton`,`appearance`]},exportAs:[`matButton`,`matAnchor`],features:[Ve],ngContentSelectors:i,decls:8,vars:5,consts:[[1,`mat-mdc-button-persistent-ripple`],[1,`mdc-button__label`],[1,`mat-mdc-button-progress-indicator-container`],[1,`mat-focus-indicator`],[1,`mat-mdc-button-touch-target`]],template:function(r,s){r&1&&(Pe(e),Wt(0,`span`,0),he(1),lt(2,`span`,1),he(3,1),ht(),he(4,2),E(5,a,2,0,`div`,2),Wt(6,`span`,3)(7,`span`,4)),r&2&&(G(`mdc-button__ripple`,!s._isFab)(`mdc-fab__ripple`,s._isFab),m(5),D(s.showProgress()?5:-1))},styles:[`.mat-mdc-button-base {
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
`],encapsulation:2})})()}return t})();function NA(t){return t.hasAttribute(`mat-raised-button`)?`elevated`:t.hasAttribute(`mat-stroked-button`)?`outlined`:t.hasAttribute(`mat-flat-button`)?`filled`:t.hasAttribute(`mat-button`)?`text`:null}var Ft=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[ki,we]})}return t})();var AA=new N(`MAT_CARD_CONFIG`);var Bn=(()=>{class t{appearance;constructor(){let e=f(AA,{optional:!0});this.appearance=e?.appearance||`raised`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return H({type:t,selectors:[[`mat-card`]],hostAttrs:[1,`mat-mdc-card`,`mdc-card`],hostVars:8,hostBindings:function(a,o){a&2&&G(`mat-mdc-card-outlined`,o.appearance===`outlined`)(`mdc-card--outlined`,o.appearance===`outlined`)(`mat-mdc-card-filled`,o.appearance===`filled`)(`mdc-card--filled`,o.appearance===`filled`)},inputs:{appearance:`appearance`},exportAs:[`matCard`],ngContentSelectors:[`*`],decls:1,vars:0,template:function(a,o){a&1&&(Pe(),he(0))},styles:[`.mat-mdc-card {
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
`],encapsulation:2})})()}return t})();var ka=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-card-title`],[``,`mat-card-title`,``],[``,`matCardTitle`,``]],hostAttrs:[1,`mat-mdc-card-title`]})}return t})();var jn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-card-content`]],hostAttrs:[1,`mat-mdc-card-content`]})}return t})();var Bu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-card-subtitle`],[``,`mat-card-subtitle`,``],[``,`matCardSubtitle`,``]],hostAttrs:[1,`mat-mdc-card-subtitle`]})}return t})();var Ma=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[``,`mat-card-avatar`,``],[``,`matCardAvatar`,``]],[[`mat-card-title`],[`mat-card-subtitle`],[``,`mat-card-title`,``],[``,`mat-card-subtitle`,``],[``,`matCardTitle`,``],[``,`matCardSubtitle`,``]],`*`];return H({type:t,selectors:[[`mat-card-header`]],hostAttrs:[1,`mat-mdc-card-header`],ngContentSelectors:[`[mat-card-avatar], [matCardAvatar]`,`mat-card-title, mat-card-subtitle,
      [mat-card-title], [mat-card-subtitle],
      [matCardTitle], [matCardSubtitle]`,`*`],decls:4,vars:0,consts:[[1,`mat-mdc-card-header-text`]],template:function(o,r){o&1&&(Pe(e),he(0),lt(1,`div`,0),he(2,1),ht(),he(3,2))},encapsulation:2})})()}return t})();var zn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[we]})}return t})();var RA=new N(`mat-progress-spinner-default-options`,{providedIn:`root`,factory:()=>({diameter:sS})});var sS=100;var OA=10;var lS=(()=>{class t{_elementRef=f(Y);_noopAnimations;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor=`primary`;_determinateCircle;constructor(){let e=f(RA),i=Tl(),a=this._elementRef.nativeElement;this._noopAnimations=i===`di-disabled`&&!!e&&!e._forceAnimations,this.mode=a.nodeName.toLowerCase()===`mat-spinner`?`indeterminate`:`determinate`,!this._noopAnimations&&i===`reduced-motion`&&a.classList.add(`mat-progress-spinner-reduced-motion`),e&&(e.color&&(this.color=this._defaultColor=e.color),e.diameter&&(this.diameter=e.diameter),e.strokeWidth&&(this.strokeWidth=e.strokeWidth))}mode;get value(){return this.mode===`determinate`?this._value:0}set value(e){this._value=Math.max(0,Math.min(100,e||0))}_value=0;get diameter(){return this._diameter}set diameter(e){this._diameter=e||0}_diameter=sS;get strokeWidth(){return this._strokeWidth??this.diameter/10}set strokeWidth(e){this._strokeWidth=e||0}_strokeWidth;_circleRadius(){return(this.diameter-OA)/2}_viewBox(){let e=this._circleRadius()*2+this.strokeWidth;return`0 0 ${e} ${e}`}_strokeCircumference(){return 2*Math.PI*this._circleRadius()}_strokeDashOffset(){return this.mode===`determinate`?this._strokeCircumference()*(100-this._value)/100:null}_circleStrokeWidth(){return this.strokeWidth/this.diameter*100}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`determinateSpinner`];function i(a,o){if(a&1&&(Gt(),c(0,`svg`,11),U(1,`circle`,12),d()),a&2){let r=v();ne(`viewBox`,r._viewBox()),m(),Zt(`stroke-dasharray`,r._strokeCircumference(),`px`)(`stroke-dashoffset`,r._strokeCircumference()/2,`px`)(`stroke-width`,r._circleStrokeWidth(),`%`),ne(`r`,r._circleRadius())}}return H({type:t,selectors:[[`mat-progress-spinner`],[`mat-spinner`]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;J(s=ee())&&(r._determinateCircle=s.first)}},hostAttrs:[`role`,`progressbar`,`tabindex`,`-1`,1,`mat-mdc-progress-spinner`,`mdc-circular-progress`],hostVars:18,hostBindings:function(o,r){o&2&&(ne(`aria-valuemin`,0)(`aria-valuemax`,100)(`aria-valuenow`,r.mode===`determinate`?r.value:null)(`mode`,r.mode),tt(`mat-`+r.color),Zt(`width`,r.diameter,`px`)(`height`,r.diameter,`px`)(`--%NS%mat-progress-spinner-size`,r.diameter+`px`)(`--%NS%mat-progress-spinner-active-indicator-width`,r.diameter+`px`),G(`_mat-animation-noopable`,r._noopAnimations)(`mdc-circular-progress--indeterminate`,r.mode===`indeterminate`))},inputs:{color:`color`,mode:`mode`,value:[2,`value`,`value`,ct],diameter:[2,`diameter`,`diameter`,ct],strokeWidth:[2,`strokeWidth`,`strokeWidth`,ct]},exportAs:[`matProgressSpinner`],decls:14,vars:11,consts:[[`circle`,``],[`determinateSpinner`,``],[`aria-hidden`,`true`,1,`mdc-circular-progress__determinate-container`],[`xmlns`,`http://www.w3.org/2000/svg`,`focusable`,`false`,1,`mdc-circular-progress__determinate-circle-graphic`],[`cx`,`50%`,`cy`,`50%`,1,`mdc-circular-progress__determinate-circle`],[`aria-hidden`,`true`,1,`mdc-circular-progress__indeterminate-container`],[1,`mdc-circular-progress__spinner-layer`],[1,`mdc-circular-progress__circle-clipper`,`mdc-circular-progress__circle-left`],[3,`ngTemplateOutlet`],[1,`mdc-circular-progress__gap-patch`],[1,`mdc-circular-progress__circle-clipper`,`mdc-circular-progress__circle-right`],[`xmlns`,`http://www.w3.org/2000/svg`,`focusable`,`false`,1,`mdc-circular-progress__indeterminate-circle-graphic`],[`cx`,`50%`,`cy`,`50%`]],template:function(o,r){if(o&1&&(rt(0,i,2,8,`ng-template`,null,0,dl),c(2,`div`,2,1),Gt(),c(4,`svg`,3),U(5,`circle`,4),d()(),_a(),c(6,`div`,5)(7,`div`,6)(8,`div`,7),wa(9,8),d(),c(10,`div`,9),wa(11,8),d(),c(12,`div`,10),wa(13,8),d()()()),o&2){let s=ft(1);m(4),ne(`viewBox`,r._viewBox()),m(),Zt(`stroke-dasharray`,r._strokeCircumference(),`px`)(`stroke-dashoffset`,r._strokeDashOffset(),`px`)(`stroke-width`,r._circleStrokeWidth(),`%`),ne(`r`,r._circleRadius()),m(4),S(`ngTemplateOutlet`,s),m(2),S(`ngTemplateOutlet`,s),m(2),S(`ngTemplateOutlet`,s)}},dependencies:[_l],styles:[`.mat-mdc-progress-spinner {
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
`],encapsulation:2})})()}return t})();var cS=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[we]})}return t})();var vS=(()=>{class t{_renderer;_elementRef;onChange=e=>{};onTouched=()=>{};constructor(e,i){this._renderer=e,this._elementRef=i}setProperty(e,i){this._renderer.setProperty(this._elementRef.nativeElement,e,i)}registerOnTouched(e){this.onTouched=e}registerOnChange(e){this.onChange=e}setDisabledState(e){this.setProperty(`disabled`,e)}static ɵfac=function(i){return new(i||t)(He(Oe),He(Y))};static ɵdir=Q({type:t})}return t})();var FA=(()=>{class t extends vS{static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,features:[Ve]})}return t})();var Yr=new N(``);var LA={provide:Yr,useExisting:Ht(()=>Qu),multi:!0};function VA(){let t=Fn()?Fn().getUserAgent():``;return/android (\d+)/.test(t.toLowerCase())}var BA=new N(``);var Qu=(()=>{class t extends vS{_compositionMode;_composing=!1;constructor(e,i,a){super(e,i),this._compositionMode=a,this._compositionMode??=!VA()}writeValue(e){let i=e??``;this.setProperty(`value`,i)}_handleInput(e){(!this._compositionMode||this._compositionMode&&!this._composing)&&this.onChange(e)}_compositionStart(){this._composing=!0}_compositionEnd(e){this._composing=!1,this._compositionMode&&this.onChange(e)}static ɵfac=function(i){return new(i||t)(He(Oe),He(Y),He(BA,8))};static ɵdir=Q({type:t,selectors:[[`input`,`formControlName`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`formControlName`,``,3,`ngNoCva`,``],[`input`,`formControl`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`formControl`,``,3,`ngNoCva`,``],[`input`,`ngModel`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`ngModel`,``,3,`ngNoCva`,``],[``,`ngDefaultControl`,``]],hostBindings:function(i,a){i&1&&C(`input`,function(r){return a._handleInput(r.target.value)})(`blur`,function(){return a.onTouched()})(`compositionstart`,function(){return a._compositionStart()})(`compositionend`,function(r){return a._compositionEnd(r.target.value)})},standalone:!1,features:[Ge([LA]),Ve]})}return t})();function Jg(t){return t==null||eb(t)===0}function eb(t){return t==null?null:Array.isArray(t)||typeof t==`string`?t.length:t instanceof Set?t.size:null}var Kr=new N(``);var tb=new N(``);var jA=/^(?=.{1,254}$)(?=.{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;var Po=class{static min(n){return zA(n)}static max(n){return HA(n)}static required(n){return _S(n)}static requiredTrue(n){return UA(n)}static email(n){return $A(n)}static minLength(n){return GA(n)}static maxLength(n){return WA(n)}static pattern(n){return qA(n)}static nullValidator(n){return zu()}static compose(n){return ES(n)}static composeAsync(n){return DS(n)}};function zA(t){return n=>{if(n.value==null||t==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e<t?{min:{min:t,actual:n.value}}:null}}function HA(t){return n=>{if(n.value==null||t==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e>t?{max:{max:t,actual:n.value}}:null}}function _S(t){return Jg(t.value)?{required:!0}:null}function UA(t){return t.value===!0?null:{required:!0}}function $A(t){return Jg(t.value)||jA.test(t.value)?null:{email:!0}}function GA(t){return n=>{let e=n.value?.length??eb(n.value);return e===null||e===0?null:e<t?{minlength:{requiredLength:t,actualLength:e}}:null}}function WA(t){return n=>{let e=n.value?.length??eb(n.value);return e!==null&&e>t?{maxlength:{requiredLength:t,actualLength:e}}:null}}function qA(t){if(!t)return zu;let n,e;return typeof t==`string`?(e=``,t.charAt(0)!==`^`&&(e+=`^`),e+=t,t.charAt(t.length-1)!==`$`&&(e+=`$`),n=new RegExp(e)):(e=t.toString(),n=t),i=>{if(Jg(i.value))return null;let a=i.value;return n.test(a)?null:{pattern:{requiredPattern:e,actualValue:a}}}}function zu(t){return null}function yS(t){return t!=null}function xS(t){return xo(t)?kn(t):t}function CS(t){let n={};return t.forEach(e=>{n=e!=null?r(r({},n),e):n}),Object.keys(n).length===0?null:n}function SS(t,n){return n.map(e=>e(t))}function YA(t){return!t.validate}function wS(t){return t.map(n=>YA(n)?n:e=>n.validate(e))}function ES(t){if(!t)return null;let n=t.filter(yS);return n.length==0?null:function(e){return CS(SS(e,n))}}function nb(t){return t!=null?ES(wS(t)):null}function DS(t){if(!t)return null;let n=t.filter(yS);return n.length==0?null:function(e){return dp(SS(e,n).map(xS)).pipe(st(CS))}}function ib(t){return t!=null?DS(wS(t)):null}function dS(t,n){return t===null?[n]:Array.isArray(t)?[...t,n]:[t,n]}function IS(t){return t._rawValidators}function kS(t){return t._rawAsyncValidators}function Qg(t){return t?Array.isArray(t)?t:[t]:[]}function Hu(t,n){return Array.isArray(t)?t.includes(n):t===n}function uS(t,n){let e=Qg(n);return Qg(t).forEach(a=>{Hu(e,a)||e.push(a)}),e}function mS(t,n){return Qg(n).filter(e=>!Hu(t,e))}var Uu=class{get value(){return this.control?this.control.value:null}get valid(){return this.control?this.control.valid:null}get invalid(){return this.control?this.control.invalid:null}get pending(){return this.control?this.control.pending:null}get disabled(){return this.control?this.control.disabled:null}get enabled(){return this.control?this.control.enabled:null}get errors(){return this.control?this.control.errors:null}get pristine(){return this.control?this.control.pristine:null}get dirty(){return this.control?this.control.dirty:null}get touched(){return this.control?this.control.touched:null}get status(){return this.control?this.control.status:null}get untouched(){return this.control?this.control.untouched:null}get statusChanges(){return this.control?this.control.statusChanges:null}get valueChanges(){return this.control?this.control.valueChanges:null}get path(){return null}_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators=[];_rawAsyncValidators=[];_setValidators(n){this._rawValidators=n||[],this._composedValidatorFn=nb(this._rawValidators)}_setAsyncValidators(n){this._rawAsyncValidators=n||[],this._composedAsyncValidatorFn=ib(this._rawAsyncValidators)}get validator(){return this._composedValidatorFn||null}get asyncValidator(){return this._composedAsyncValidatorFn||null}_onDestroyCallbacks=[];_registerOnDestroy(n){this._onDestroyCallbacks.push(n)}_invokeOnDestroyCallbacks(){this._onDestroyCallbacks.forEach(n=>n()),this._onDestroyCallbacks=[]}reset(n=void 0){this.control?.reset(n)}hasError(n,e){return this.control?this.control.hasError(n,e):!1}getError(n,e){return this.control?this.control.getError(n,e):null}};var Fo=class extends Uu{name;get formDirective(){return null}get path(){return null}};var Rl=`VALID`;var ju=`INVALID`;var Gr=`PENDING`;var Ol=`DISABLED`;var Ta=class{};var $u=class extends Ta{value;source;constructor(n,e){super(),this.value=n,this.source=e}};var Fl=class extends Ta{pristine;source;constructor(n,e){super(),this.pristine=n,this.source=e}};var Ll=class extends Ta{touched;source;constructor(n,e){super(),this.touched=n,this.source=e}};var Wr=class extends Ta{status;source;constructor(n,e){super(),this.status=n,this.source=e}};var Gu=class extends Ta{source;constructor(n){super(),this.source=n}};var qr=class extends Ta{source;constructor(n){super(),this.source=n}};function MS(t){return(Xu(t)?t.validators:t)||null}function KA(t){return Array.isArray(t)?nb(t):t||null}function TS(t,n){return(Xu(n)?n.asyncValidators:t)||null}function QA(t){return Array.isArray(t)?ib(t):t||null}function Xu(t){return t!=null&&!Array.isArray(t)&&typeof t==`object`}function XA(t,n,e){let i=t.controls;if(!(n?Object.keys(i):i).length)throw new q(1e3,``);if(!NS(i,e))throw new q(1001,``)}function ZA(t,n,e){t._forEachChild((i,a)=>{if(e[a]===void 0)throw new q(-1002,``)})}var Wu=class{_pendingDirty=!1;_hasOwnPendingAsyncValidator=null;_pendingTouched=!1;_onCollectionChange=()=>{};_updateOn;_hasRequired=k(!1);_parent=null;_asyncValidationSubscription;_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators;_rawAsyncValidators;value;constructor(n,e){this._assignValidators(n),this._assignAsyncValidators(e)}get validator(){return this._composedValidatorFn}set validator(n){this._rawValidators=this._composedValidatorFn=n,this._updateHasRequiredValidator()}get asyncValidator(){return this._composedAsyncValidatorFn}set asyncValidator(n){this._rawAsyncValidators=this._composedAsyncValidatorFn=n}get parent(){return this._parent}get status(){return it(this.statusReactive)}set status(n){it(()=>this.statusReactive.set(n))}_status=K(()=>this.statusReactive());statusReactive=k(void 0);get valid(){return this.status===Rl}get invalid(){return this.status===ju}get pending(){return this.status===Gr}get disabled(){return this.status===Ol}get enabled(){return this.status!==Ol}errors;get pristine(){return it(this.pristineReactive)}set pristine(n){it(()=>this.pristineReactive.set(n))}_pristine=K(()=>this.pristineReactive());pristineReactive=k(!0);get dirty(){return!this.pristine}get touched(){return it(this.touchedReactive)}set touched(n){it(()=>this.touchedReactive.set(n))}_touched=K(()=>this.touchedReactive());touchedReactive=k(!1);get untouched(){return!this.touched}_events=new V;events=this._events.asObservable();valueChanges;statusChanges;get updateOn(){return this._updateOn?this._updateOn:this.parent?this.parent.updateOn:`change`}setValidators(n){this._assignValidators(n)}setAsyncValidators(n){this._assignAsyncValidators(n)}addValidators(n){this.setValidators(uS(n,this._rawValidators))}addAsyncValidators(n){this.setAsyncValidators(uS(n,this._rawAsyncValidators))}removeValidators(n){this.setValidators(mS(n,this._rawValidators))}removeAsyncValidators(n){this.setAsyncValidators(mS(n,this._rawAsyncValidators))}hasValidator(n){return Hu(this._rawValidators,n)}hasAsyncValidator(n){return Hu(this._rawAsyncValidators,n)}clearValidators(){this.validator=null}clearAsyncValidators(){this.asyncValidator=null}markAsTouched(n={}){let e=this.touched===!1;this.touched=!0;let i=n.sourceControl??this;n.onlySelf||this._parent?.markAsTouched(s(r({},n),{sourceControl:i})),e&&n.emitEvent!==!1&&this._events.next(new Ll(!0,i))}markAllAsDirty(n={}){this.markAsDirty({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsDirty(n))}markAllAsTouched(n={}){this.markAsTouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsTouched(n))}markAsUntouched(n={}){let e=this.touched===!0;this.touched=!1,this._pendingTouched=!1;let i=n.sourceControl??this;this._forEachChild(a=>{a.markAsUntouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:i})}),n.onlySelf||this._parent?._updateTouched(n,i),e&&n.emitEvent!==!1&&this._events.next(new Ll(!1,i))}markAsDirty(n={}){let e=this.pristine===!0;this.pristine=!1;let i=n.sourceControl??this;n.onlySelf||this._parent?.markAsDirty(s(r({},n),{sourceControl:i})),e&&n.emitEvent!==!1&&this._events.next(new Fl(!1,i))}markAsPristine(n={}){let e=this.pristine===!1;this.pristine=!0,this._pendingDirty=!1;let i=n.sourceControl??this;this._forEachChild(a=>{a.markAsPristine({onlySelf:!0,emitEvent:n.emitEvent})}),n.onlySelf||this._parent?._updatePristine(n,i),e&&n.emitEvent!==!1&&this._events.next(new Fl(!0,i))}markAsPending(n={}){this.status=Gr;let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new Wr(this.status,e)),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.markAsPending(s(r({},n),{sourceControl:e}))}disable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=Ol,this.errors=null,this._forEachChild(a=>{a.disable(s(r({},n),{onlySelf:!0}))}),this._updateValue();let i=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new $u(this.value,i)),this._events.next(new Wr(this.status,i)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),this._updateAncestors(s(r({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(a=>a(!0))}enable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=Rl,this._forEachChild(i=>{i.enable(s(r({},n),{onlySelf:!0}))}),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent}),this._updateAncestors(s(r({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(i=>i(!1))}_updateAncestors(n,e){n.onlySelf||(this._parent?.updateValueAndValidity(n),n.skipPristineCheck||this._parent?._updatePristine({},e),this._parent?._updateTouched({},e))}setParent(n){this._parent=n}getRawValue(){return this.value}updateValueAndValidity(n={}){if(this._setInitialStatus(),this._updateValue(),this.enabled){let i=this._cancelExistingSubscription();this.errors=this._runValidator(),this.status=this._calculateStatus(),(this.status===Rl||this.status===Gr)&&this._runAsyncValidator(i,n.emitEvent)}let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new $u(this.value,e)),this._events.next(new Wr(this.status,e)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.updateValueAndValidity(s(r({},n),{sourceControl:e}))}_updateTreeValidity(n={emitEvent:!0}){this._forEachChild(e=>e._updateTreeValidity(n)),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent})}_setInitialStatus(){this.status=this._allControlsDisabled()?Ol:Rl}_runValidator(){return this.validator?this.validator(this):null}_runAsyncValidator(n,e){if(this.asyncValidator){this.status=Gr,this._hasOwnPendingAsyncValidator={emitEvent:e!==!1,shouldHaveEmitted:n!==!1};let i=xS(this.asyncValidator(this));this._asyncValidationSubscription=i.subscribe(a=>{this._hasOwnPendingAsyncValidator=null,this.setErrors(a,{emitEvent:e,shouldHaveEmitted:n})})}}_cancelExistingSubscription(){if(this._asyncValidationSubscription){this._asyncValidationSubscription.unsubscribe();let n=(this._hasOwnPendingAsyncValidator?.emitEvent||this._hasOwnPendingAsyncValidator?.shouldHaveEmitted)??!1;return this._hasOwnPendingAsyncValidator=null,n}return!1}setErrors(n,e={}){this.errors=n,this._updateControlsErrors(e.emitEvent!==!1,this,e.shouldHaveEmitted)}get(n){let e=n;return e==null||(Array.isArray(e)||(e=e.split(`.`)),e.length===0)?null:e.reduce((i,a)=>i&&i._find(a),this)}getError(n,e){let i=e?this.get(e):this;return i?.errors?i.errors[n]:null}hasError(n,e){return!!this.getError(n,e)}get root(){let n=this;for(;n._parent;)n=n._parent;return n}_updateControlsErrors(n,e,i){this.status=this._calculateStatus(),n&&this.statusChanges.emit(this.status),(n||i)&&this._events.next(new Wr(this.status,e)),this._parent&&this._parent._updateControlsErrors(n,e,i)}_initObservables(){this.valueChanges=new te,this.statusChanges=new te}_calculateStatus(){return this._allControlsDisabled()?Ol:this.errors?ju:this._hasOwnPendingAsyncValidator||this._anyControlsHaveStatus(Gr)?Gr:this._anyControlsHaveStatus(ju)?ju:Rl}_anyControlsHaveStatus(n){return this._anyControls(e=>e.status===n)}_anyControlsDirty(){return this._anyControls(n=>n.dirty)}_anyControlsTouched(){return this._anyControls(n=>n.touched)}_updatePristine(n,e){let i=!this._anyControlsDirty(),a=this.pristine!==i;this.pristine=i,n.onlySelf||this._parent?._updatePristine(n,e),a&&this._events.next(new Fl(this.pristine,e))}_updateTouched(n={},e){this.touched=this._anyControlsTouched(),this._events.next(new Ll(this.touched,e)),n.onlySelf||this._parent?._updateTouched(n,e)}_onDisabledChange=[];_registerOnCollectionChange(n){this._onCollectionChange=n}_setUpdateStrategy(n){Xu(n)&&n.updateOn!=null&&(this._updateOn=n.updateOn)}_parentMarkedDirty(n){return!n&&!!this._parent?.dirty&&!this._parent._anyControlsDirty()}_find(n){return null}_assignValidators(n){this._rawValidators=Array.isArray(n)?n.slice():n,this._composedValidatorFn=KA(this._rawValidators),this._updateHasRequiredValidator()}_assignAsyncValidators(n){this._rawAsyncValidators=Array.isArray(n)?n.slice():n,this._composedAsyncValidatorFn=QA(this._rawAsyncValidators)}_updateHasRequiredValidator(){it(()=>this._hasRequired.set(this.hasValidator(Po.required)))}};function NS(t,n){return Object.hasOwn(t,n)}function JA(t){return t.tagName===`INPUT`||t.tagName===`SELECT`||t.tagName===`TEXTAREA`}function eR(t,n,e,i){switch(e){case`name`:t.setAttribute(n,e,i);break;case`disabled`:case`readonly`:case`required`:i?t.setAttribute(n,e,``):t.removeAttribute(n,e);break;case`max`:case`min`:case`minLength`:case`maxLength`:i!==void 0?t.setAttribute(n,e,i.toString()):t.removeAttribute(n,e)}}var Xg=class{kind;context;control;message;constructor({kind:n,context:e,control:i}){this.kind=n,this.context=e,this.control=i}};var tR=(()=>{class t{_validator=zu;_onChange;_enabled;ngOnChanges(e){if(this.inputName in e){let i=this.normalizeInput(e[this.inputName].currentValue);this._enabled=this.enabled(i),this._validator=this._enabled?this.createValidator(i):zu,this._onChange?.()}}validate(e){return this._validator(e)}registerOnValidatorChange(e){this._onChange=e}enabled(e){return e!=null}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,features:[_t]})}return t})();var nR={provide:Kr,useExisting:Ht(()=>AS),multi:!0};var AS=(()=>{class t extends tR{required;inputName=`required`;normalizeInput=z;createValidator=e=>_S;enabled(e){return e}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`required`,``,`formControlName`,``,3,`type`,`checkbox`],[``,`required`,``,`formControl`,``,3,`type`,`checkbox`],[``,`required`,``,`ngModel`,``,3,`type`,`checkbox`]],hostVars:1,hostBindings:function(i,a){i&2&&ne(`required`,a._enabled?``:null)},inputs:{required:`required`},standalone:!1,features:[Ge([nR]),Ve]})}return t})();var iR=new N(``);var Zu=new N(``,{factory:()=>ab});var ab=`always`;function aR(t,n){return[...n.path,t]}function pS(t,n,e=ab){ob(t,n),n.valueAccessor.writeValue(t.value),(t.disabled||e===`always`)&&n.valueAccessor.setDisabledState?.(t.disabled),rR(t,n),lR(t,n),sR(t,n),oR(t,n)}function hS(t,n,e=!0){let i=()=>{};n?.valueAccessor?.registerOnChange(i),n?.valueAccessor?.registerOnTouched(i),Yu(t,n),t&&(n._invokeOnDestroyCallbacks(),t._registerOnCollectionChange(()=>{}))}function qu(t,n){t.forEach(e=>{e.registerOnValidatorChange&&e.registerOnValidatorChange(n)})}function oR(t,n){if(n.valueAccessor.setDisabledState){let e=i=>{n.valueAccessor.setDisabledState(i)};t.registerOnDisabledChange(e),n._registerOnDestroy(()=>{t._unregisterOnDisabledChange(e)})}}function ob(t,n){let e=IS(t);n.validator!==null?t.setValidators(dS(e,n.validator)):typeof e==`function`&&t.setValidators([e]);let i=kS(t);n.asyncValidator!==null?t.setAsyncValidators(dS(i,n.asyncValidator)):typeof i==`function`&&t.setAsyncValidators([i]);let a=()=>t.updateValueAndValidity();qu(n._rawValidators,a),qu(n._rawAsyncValidators,a)}function Yu(t,n){let e=!1;if(t!==null){if(n.validator!==null){let a=IS(t);if(Array.isArray(a)&&a.length>0){let o=a.filter(r=>r!==n.validator);o.length!==a.length&&(e=!0,t.setValidators(o))}}if(n.asyncValidator!==null){let a=kS(t);if(Array.isArray(a)&&a.length>0){let o=a.filter(r=>r!==n.asyncValidator);o.length!==a.length&&(e=!0,t.setAsyncValidators(o))}}}let i=()=>{};return qu(n._rawValidators,i),qu(n._rawAsyncValidators,i),e}function rR(t,n){n.valueAccessor.registerOnChange(e=>{t._pendingValue=e,t._pendingChange=!0,t._pendingDirty=!0,t.updateOn===`change`&&RS(t,n)})}function sR(t,n){n.valueAccessor.registerOnTouched(()=>{t._pendingTouched=!0,t.updateOn===`blur`&&t._pendingChange&&RS(t,n),t.updateOn!==`submit`&&t.markAsTouched()})}function RS(t,n){t._pendingDirty&&t.markAsDirty(),t.setValue(t._pendingValue,{emitModelToViewChange:!1}),n.viewToModelUpdate(t._pendingValue),t._pendingChange=!1}function lR(t,n){let e=(i,a)=>{n.valueAccessor.writeValue(i),a&&n.viewToModelUpdate(i)};t.registerOnChange(e),n._registerOnDestroy(()=>{t._unregisterOnChange(e)})}function OS(t,n){ob(t,n)}function cR(t,n){return Yu(t,n)}function dR(t,n){if(!Object.hasOwn(t,`model`))return!1;let e=t.model;return e.isFirstChange()?!0:!Object.is(n,e.currentValue)}function uR(t){return Object.getPrototypeOf(t.constructor)===FA}function PS(t,n){t._syncPendingControls(),n.forEach(e=>{let i=e.control;i.updateOn===`submit`&&i._pendingChange&&(e.viewToModelUpdate(i._pendingValue),i._pendingChange=!1)})}function mR(t,n){if(!n)return null;let e,i,a;return n.forEach(o=>{o.constructor===Qu?e=o:uR(o)?i=o:a=o}),a||i||e||null}function pR(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}var hR={provide:iR,useFactory:()=>{let t=f(Mi,{self:!0});return{setParseErrors:n=>{t.setParseErrorSource(n)},set onReset(n){t.onReset=n}}}};var Mi=class extends Uu{_parent=null;name=null;valueAccessor=null;isCustomControlBased=!1;userOnReset;resetSubscription;set onReset(n){this.userOnReset=n,this.resetSubscription?.unsubscribe(),this.resetSubscription=void 0,this.control&&(this.resetSubscription=this.control.events.subscribe(e=>{e instanceof qr&&this.control&&this.userOnReset?.(this.control.value)}),this.subscription?.add(this.resetSubscription))}isNativeFormElement=!1;rawValueAccessors;_selectedValueAccessor=null;get selectedValueAccessor(){return this._selectedValueAccessor??=mR(this,this.rawValueAccessors)}parseErrorsValidator=null;renderer;injector;requiredValidatorViaDi;subscription;customControlBindings=null;constructor(n,e,i){super(),this.injector=n,this.renderer=e,this.rawValueAccessors=i,this.injector?.get(Ae)?.onDestroy(()=>{this.removeParseErrorsValidator(this.control),this.subscription?.unsubscribe()})}setupCustomControl(){this.subscription?.unsubscribe();let n=this.injector?.get(Fe);if(!this.control||!n)return;let e=n.markForCheck.bind(n);this.subscription=new De,this.subscription.add(this.control.valueChanges.subscribe(e)),this.subscription.add(this.control.statusChanges.subscribe(e)),this.resetSubscription?.unsubscribe(),this.resetSubscription=void 0,this.userOnReset&&(this.resetSubscription=this.control.events.subscribe(i=>{i instanceof qr&&this.control&&this.userOnReset?.(this.control.value)}),this.subscription.add(this.resetSubscription)),this.parseErrorsValidator&&this.control.addValidators(this.parseErrorsValidator)}ngControlCreate(n){!n.nativeElement.hasAttribute?.(`ngNoCva`)&&(this.rawValueAccessors&&this.rawValueAccessors.length>0||this.valueAccessor!==null)||!n.customControl||(this.isCustomControlBased=!0,n.listenToCustomControlModel(a=>{this.control?.markAsDirty(),this.control?.setValue(a,{emitModelToViewChange:!1}),this.viewToModelUpdate(a)}),n.listenToCustomControlOutput(`touch`,()=>{this.control?.markAsTouched()}),this.customControlBindings={},this.isNativeFormElement=JA(n.nativeElement),this.requiredValidatorViaDi=this._rawValidators.find(a=>a instanceof AS))}ngControlUpdate(n,e){if(!this.isCustomControlBased)return;let i=this.control,a=this.customControlBindings;Object.is(a.value,i.value)||(a.value=i.value,n.setCustomControlModelInput(i.value)),this.bindControlProperty(n,a,`touched`,i.touched),this.bindControlProperty(n,a,`dirty`,i.dirty),this.bindControlProperty(n,a,`valid`,i.valid),this.bindControlProperty(n,a,`invalid`,i.invalid),this.bindControlProperty(n,a,`pending`,i.pending),this.bindControlProperty(n,a,`disabled`,i.disabled),this.shouldBindRequired&&this.bindControlProperty(n,a,`required`,this.isRequired);let o=i.errors;if(a.errors!==o){a.errors=o;let r=this._convertErrors(o);n.setInputOnDirectives(`errors`,r)}}get isRequired(){return(this.requiredValidatorViaDi?._enabled||this.control?._hasRequired())??!1}get shouldBindRequired(){return!0}bindControlProperty(n,e,i,a){if(e[i]===a)return;e[i]=a;let o=n.setInputOnDirectives(i,a);this.isNativeFormElement&&!o&&(i===`disabled`||i===`required`)&&this.renderer&&eR(this.renderer,n.nativeElement,i,a)}_convertErrors(n){if(n===null)return[];let e=this.control;return Object.entries(n).map(([i,a])=>new Xg({context:a,kind:i,control:e}))}setParseErrorSource(n){if(n===void 0)return;let e=null,i=K(()=>{let a=n();return a.length===0?null:a.reduce((o,r)=>(o[r.kind]=r,o),{})});this.parseErrorsValidator=(()=>e).bind(this),vt(()=>{e=i(),this.control?.updateValueAndValidity({emitEvent:!1})},{injector:this.injector})}removeParseErrorsValidator(n){this.parseErrorsValidator&&(n?.removeValidators(this.parseErrorsValidator),n?.updateValueAndValidity({emitEvent:!1}))}};var Zg=class{_cd;constructor(n){this._cd=n}get isTouched(){return this._cd?.control?._touched?.(),!!this._cd?.control?.touched}get isUntouched(){return!!this._cd?.control?.untouched}get isPristine(){return this._cd?.control?._pristine?.(),!!this._cd?.control?.pristine}get isDirty(){return!!this._cd?.control?.dirty}get isValid(){return this._cd?.control?._status?.(),!!this._cd?.control?.valid}get isInvalid(){return!!this._cd?.control?.invalid}get isPending(){return!!this._cd?.control?.pending}get isSubmitted(){return this._cd?._submitted?.(),!!this._cd?.submitted}};var FS=(()=>{class t extends Zg{constructor(e){super(e)}static ɵfac=function(i){return new(i||t)(He(Mi,2))};static ɵdir=Q({type:t,selectors:[[``,`formControlName`,``],[``,`ngModel`,``],[``,`formControl`,``]],hostVars:14,hostBindings:function(i,a){i&2&&G(`ng-untouched`,a.isUntouched)(`ng-touched`,a.isTouched)(`ng-pristine`,a.isPristine)(`ng-dirty`,a.isDirty)(`ng-valid`,a.isValid)(`ng-invalid`,a.isInvalid)(`ng-pending`,a.isPending)},standalone:!1,features:[Ve]})}return t})();var Ku=class extends Wu{constructor(n,e,i){super(MS(e),TS(i,e)),this.controls=n,this._initObservables(),this._setUpdateStrategy(e),this._setUpControls(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator})}controls;registerControl(n,e){return this._find(n)||(this.controls[n]=e,e.setParent(this),e._registerOnCollectionChange(this._onCollectionChange),e)}addControl(n,e,i={}){this.registerControl(n,e),this.updateValueAndValidity({emitEvent:i.emitEvent}),this._onCollectionChange()}removeControl(n,e={}){let i=this._find(n);i&&i._registerOnCollectionChange(()=>{}),delete this.controls[n],this.updateValueAndValidity({emitEvent:e.emitEvent}),this._onCollectionChange()}setControl(n,e,i={}){let a=this._find(n);a&&a._registerOnCollectionChange(()=>{}),delete this.controls[n],e&&this.registerControl(n,e),this.updateValueAndValidity({emitEvent:i.emitEvent}),this._onCollectionChange()}contains(n){return this._find(n)?.enabled===!0}setValue(n,e={}){it(()=>{ZA(this,!0,n),Object.keys(n).forEach(i=>{XA(this,!0,i),this.controls[i].setValue(n[i],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e)})}patchValue(n,e={}){n!=null&&(Object.keys(n).forEach(i=>{let a=this._find(i);a&&a.patchValue(n[i],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e))}reset(n={},e={}){this._forEachChild((i,a)=>{i.reset(n?n[a]:null,s(r({},e),{onlySelf:!0}))}),this._updatePristine(e,this),this._updateTouched(e,this),this.updateValueAndValidity(e),e?.emitEvent!==!1&&this._events.next(new qr(this))}getRawValue(){return this._reduceChildren({},(n,e,i)=>(n[i]=e.getRawValue(),n))}_syncPendingControls(){let n=this._reduceChildren(!1,(e,i)=>i._syncPendingControls()?!0:e);return n&&this.updateValueAndValidity({onlySelf:!0}),n}_forEachChild(n){Object.keys(this.controls).forEach(e=>{let i=this.controls[e];i&&n(i,e)})}_setUpControls(){this._forEachChild(n=>{n.setParent(this),n._registerOnCollectionChange(this._onCollectionChange)})}_updateValue(){this.value=this._reduceValue()}_anyControls(n){for(let[e,i]of Object.entries(this.controls))if(this.contains(e)&&n(i))return!0;return!1}_reduceValue(){return this._reduceChildren({},(e,i,a)=>((i.enabled||this.disabled)&&(e[a]=i.value),e))}_reduceChildren(n,e){let i=n;return this._forEachChild((a,o)=>{i=e(i,a,o)}),i}_allControlsDisabled(){for(let n of Object.keys(this.controls))if(this.controls[n].enabled)return!1;return Object.keys(this.controls).length>0||this.disabled}_find(n){return NS(this.controls,n)?this.controls[n]:null}};var fR={provide:Fo,useExisting:Ht(()=>Vl)};var Pl=Promise.resolve();var Vl=(()=>{class t extends Fo{callSetDisabledState;get submitted(){return it(this.submittedReactive)}_submitted=K(()=>this.submittedReactive());submittedReactive=k(!1);_directives=new Set;form;ngSubmit=new te;options;constructor(e,i,a){super(),this.callSetDisabledState=a,this.form=new Ku({},nb(e),ib(i))}ngAfterViewInit(){this._setUpdateStrategy()}get formDirective(){return this}get control(){return this.form}get path(){return[]}get controls(){return this.form.controls}addControl(e){Pl.then(()=>{e.control=this._findContainer(e.path).registerControl(e.name,e.control),e._setupWithForm(this.callSetDisabledState),e.control.updateValueAndValidity({emitEvent:!1}),this._directives.add(e)})}getControl(e){return this.form.get(e.path)}removeControl(e){Pl.then(()=>{this._findContainer(e.path)?.removeControl(e.name),this._directives.delete(e)})}addFormGroup(e){Pl.then(()=>{let i=this._findContainer(e.path),a=new Ku({});OS(a,e),i.registerControl(e.name,a),a.updateValueAndValidity({emitEvent:!1})})}removeFormGroup(e){Pl.then(()=>{this._findContainer(e.path)?.removeControl?.(e.name)})}getFormGroup(e){return this.form.get(e.path)}updateModel(e,i){Pl.then(()=>{this.form.get(e.path).setValue(i)})}setValue(e){this.control.setValue(e)}onSubmit(e){return this.submittedReactive.set(!0),PS(this.form,this._directives),this.ngSubmit.emit(e),this.form._events.next(new Gu(this.control)),e?.target?.method===`dialog`}onReset(){this.resetForm()}resetForm(e=void 0){this.form.reset(e),this.submittedReactive.set(!1)}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.form._updateOn=this.options.updateOn)}_findContainer(e){return e.pop(),e.length?this.form.get(e):this.form}static ɵfac=function(i){return new(i||t)(He(Kr,10),He(tb,10),He(Zu,8))};static ɵdir=Q({type:t,selectors:[[`form`,3,`ngNoForm`,``,3,`formGroup`,``,3,`formArray`,``],[`ng-form`],[``,`ngForm`,``]],hostBindings:function(i,a){i&1&&C(`submit`,function(r){return a.onSubmit(r)})(`reset`,function(){return a.onReset()})},inputs:{options:[0,`ngFormOptions`,`options`]},outputs:{ngSubmit:`ngSubmit`},exportAs:[`ngForm`],standalone:!1,features:[Ge([fR]),Ve]})}return t})();function fS(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}function gS(t){return typeof t==`object`&&t!==null&&Object.keys(t).length===2&&`value`in t&&`disabled`in t}var LS=class extends Wu{defaultValue=null;_onChange=[];_pendingValue;_pendingChange=!1;constructor(n=null,e,i){super(MS(e),TS(i,e)),this._applyFormState(n),this._setUpdateStrategy(e),this._initObservables(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator}),Xu(e)&&(e.nonNullable||e.initialValueIsDefault)&&(gS(n)?this.defaultValue=n.value:this.defaultValue=n)}setValue(n,e={}){it(()=>{this.value=this._pendingValue=n,this._onChange.length&&e.emitModelToViewChange!==!1&&this._onChange.forEach(i=>i(this.value,e.emitViewToModelChange!==!1)),this.updateValueAndValidity(e)})}patchValue(n,e={}){this.setValue(n,e)}reset(n=this.defaultValue,e={}){this._applyFormState(n),this.markAsPristine(e),this.markAsUntouched(e),this.setValue(this.value,e),e.overwriteDefaultValue&&(this.defaultValue=this.value),this._pendingChange=!1,e?.emitEvent!==!1&&this._events.next(new qr(this))}_updateValue(){}_anyControls(n){return!1}_allControlsDisabled(){return this.disabled}registerOnChange(n){this._onChange.push(n)}_unregisterOnChange(n){fS(this._onChange,n)}registerOnDisabledChange(n){this._onDisabledChange.push(n)}_unregisterOnDisabledChange(n){fS(this._onDisabledChange,n)}_forEachChild(n){}_syncPendingControls(){return this.updateOn===`submit`&&(this._pendingDirty&&this.markAsDirty(),this._pendingTouched&&this.markAsTouched(),this._pendingChange)?(this.setValue(this._pendingValue,{onlySelf:!0,emitModelToViewChange:!1}),!0):!1}_applyFormState(n){gS(n)?(this.value=this._pendingValue=n.value,n.disabled?this.disable({onlySelf:!0,emitEvent:!1}):this.enable({onlySelf:!0,emitEvent:!1})):this.value=this._pendingValue=n}};var gR=t=>t instanceof LS;var bR=(()=>{class t extends Fo{callSetDisabledState;get submitted(){return it(this._submittedReactive)}set submitted(e){this._submittedReactive.set(e)}_submitted=K(()=>this._submittedReactive());_submittedReactive=k(!1);_oldForm;_onCollectionChange=()=>this._updateDomValue();directives=[];constructor(e,i,a){super(),this.callSetDisabledState=a,this._setValidators(e),this._setAsyncValidators(i)}ngOnChanges(e){this.onChanges(e)}ngOnDestroy(){this.onDestroy()}onChanges(e){this._checkFormPresent(),Object.hasOwn(e,`form`)&&(this._updateValidators(),this._updateDomValue(),this._updateRegistrations(),this._oldForm=this.form)}onDestroy(){this.form&&(Yu(this.form,this),this.form._onCollectionChange===this._onCollectionChange&&this.form._registerOnCollectionChange(()=>{}))}get formDirective(){return this}get path(){return[]}addControl(e){let i=this.form.get(e.path);return e._setupWithForm(i,this.callSetDisabledState),i.updateValueAndValidity({emitEvent:!1}),this.directives.push(e),i}getControl(e){return this.form.get(e.path)}removeControl(e){hS(e.control||null,e,!1),pR(this.directives,e)}addFormGroup(e){this._setUpFormContainer(e)}removeFormGroup(e){this._cleanUpFormContainer(e)}getFormGroup(e){return this.form.get(e.path)}getFormArray(e){return this.form.get(e.path)}addFormArray(e){this._setUpFormContainer(e)}removeFormArray(e){this._cleanUpFormContainer(e)}updateModel(e,i){this.form.get(e.path).setValue(i)}onReset(){this.resetForm()}resetForm(e=void 0,i={}){this.form.reset(e,i),this._submittedReactive.set(!1)}onSubmit(e){return this.submitted=!0,PS(this.form,this.directives),this.ngSubmit.emit(e),this.form._events.next(new Gu(this.control)),e?.target?.method===`dialog`}_updateDomValue(){this.directives.forEach(e=>{let i=e.control,a=this.form.get(e.path);i!==a&&(hS(i||null,e),gR(a)&&e._setupWithForm(a,this.callSetDisabledState))}),this.form._updateTreeValidity({emitEvent:!1})}_setUpFormContainer(e){let i=this.form.get(e.path);OS(i,e),i.updateValueAndValidity({emitEvent:!1})}_cleanUpFormContainer(e){let i=this.form?.get(e.path);i&&cR(i,e)&&i.updateValueAndValidity({emitEvent:!1})}_updateRegistrations(){this.form._registerOnCollectionChange(this._onCollectionChange),this._oldForm?._registerOnCollectionChange(()=>{})}_updateValidators(){ob(this.form,this),this._oldForm&&Yu(this._oldForm,this)}_checkFormPresent(){this.form}static ɵfac=function(i){return new(i||t)(He(Kr,10),He(tb,10),He(Zu,8))};static ɵdir=Q({type:t,features:[Ve,_t]})}return t})();var vR={provide:Fo,useExisting:Ht(()=>Bl)};var Bl=(()=>{class t extends bR{form=null;ngSubmit=new te;get control(){return this.form}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`formGroup`,``]],hostBindings:function(i,a){i&1&&C(`submit`,function(r){return a.onSubmit(r)})(`reset`,function(){return a.onReset()})},inputs:{form:[0,`formGroup`,`form`]},outputs:{ngSubmit:`ngSubmit`},exportAs:[`ngForm`],standalone:!1,features:[Ge([vR]),Ve]})}return t})();var _R={provide:Mi,useExisting:Ht(()=>rb)};var bS=Promise.resolve();var rb=(()=>{class t extends Mi{_changeDetectorRef;callSetDisabledState;control=new LS;static ngAcceptInputType_isDisabled;_registered=!1;_ngModelInjector;viewModel;name=``;isDisabled;model;options;update=new te;constructor(e,i,a,o,r,s,l,h){super(l,h,o),this._changeDetectorRef=r,this.callSetDisabledState=s,this._parent=e,this._setValidators(i),this._setAsyncValidators(a)}ngOnChanges(e){if(this._registered,this._checkForErrors(),!this._registered||`name`in e){if(this._registered&&(this._checkName(),this.formDirective)){let i=e.name.previousValue;this.formDirective.removeControl({name:i,path:this._getPath(i)})}this._setUpControl()}`isDisabled`in e&&this._updateDisabled(e),dR(e,this.viewModel)&&(this._updateValue(this.model),this.viewModel=this.model)}ngOnDestroy(){this.formDirective?.removeControl(this)}ɵngControlCreate(e){super.ngControlCreate(e)}ɵngControlUpdate(e){super.ngControlUpdate(e,!1)}get shouldBindRequired(){return!1}get path(){return this._getPath(this.name)}get formDirective(){return this._parent?this._parent.formDirective:null}viewToModelUpdate(e){this.viewModel=e,this.update.emit(e)}_setUpControl(){this._setUpdateStrategy(),this._isStandalone()?this._setUpStandalone():this.formDirective.addControl(this),this._registered=!0}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.control._updateOn=this.options.updateOn)}_isStandalone(){return!this._parent||!!(this.options&&this.options.standalone)}_setUpStandalone(){this.isCustomControlBased?this.setupCustomControl():(this.valueAccessor??=this.selectedValueAccessor,pS(this.control,this,this.callSetDisabledState)),this.control.updateValueAndValidity({emitEvent:!1})}_setupWithForm(e){this.isCustomControlBased?this.setupCustomControl():(this.valueAccessor??=this.selectedValueAccessor,pS(this.control,this,e))}_checkForErrors(){this._checkName()}_checkName(){this.options&&this.options.name&&(this.name=this.options.name),!this._isStandalone()&&this.name}_updateValue(e){bS.then(()=>{this.control.setValue(e,{emitViewToModelChange:!1}),this._changeDetectorRef?.markForCheck()})}_updateDisabled(e){let i=e.isDisabled.currentValue,a=i!==0&&z(i);bS.then(()=>{a&&!this.control.disabled?this.control.disable():!a&&this.control.disabled&&this.control.enable(),this._changeDetectorRef?.markForCheck()})}_getPath(e){return this._parent?aR(e,this._parent):[e]}static ɵfac=function(i){return new(i||t)(He(Fo,9),He(Kr,10),He(tb,10),He(Yr,10),He(Fe,8),He(Zu,8),He(se,8),He(Oe,8))};static ɵdir=Q({type:t,selectors:[[``,`ngModel`,``,3,`formControlName`,``,3,`formControl`,``]],inputs:{name:`name`,isDisabled:[0,`disabled`,`isDisabled`],model:[0,`ngModel`,`model`],options:[0,`ngModelOptions`,`options`]},outputs:{update:`ngModelChange`},exportAs:[`ngModel`],standalone:!1,features:[Ge([_R,hR]),Ve,_t,Kf(null)]})}return t})();var yR=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({})}return t})();var VS=(()=>{class t{static withConfig(e){return{ngModule:t,providers:[{provide:Zu,useValue:e.callSetDisabledState??ab}]}}static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[yR]})}return t})();var jS=Symbol(`FIELD_TREE`);function jl(t){return typeof t==`function`&&t[jS]===!0}var Ju=new N(``);var Qr=(()=>{class t{isErrorState(e,i){return!!(e&&e.invalid&&(e.touched||i&&i.submitted))}isSignalErrorState(e){if(!e)return!1;let i=e().invalid(),a=e().touched();return i&&a}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Xr=class{_defaultMatcher;_parentFormGroup;_parentForm;_stateChanges;errorState=!1;matcher;ngControl;formField;constructor(n,e,i,a,o){this._defaultMatcher=n,this._parentFormGroup=i,this._parentForm=a,this._stateChanges=o,e?ln(e.field)&&!e.updateValueAndValidity?(this.formField=e,this.ngControl=null):(this.formField=null,this.ngControl=e):this.ngControl=this.formField=null}updateErrorState(){let n=this.errorState,e=this._getCurrentErrorState(this.matcher||this._defaultMatcher);e!==n&&(this.errorState=e,this._stateChanges.next())}_getCurrentErrorState(n){if(this.formField&&n?.isSignalErrorState)return n.isSignalErrorState(this.formField.field())??!1;let e=this._parentFormGroup||this._parentForm,i=this.ngControl?this.ngControl.control:null;return n?.isErrorState(i,e)??!1}};var sb=class{_box;_destroyed=new V;_resizeSubject=new V;_resizeObserver;_elementObservables=new Map;constructor(n){this._box=n,typeof ResizeObserver<`u`&&(this._resizeObserver=new ResizeObserver(e=>this._resizeSubject.next(e)))}observe(n){return this._elementObservables.has(n)||this._elementObservables.set(n,new Ce(e=>{let i=this._resizeSubject.subscribe(e);return this._resizeObserver?.observe(n,{box:this._box}),()=>{this._resizeObserver?.unobserve(n),i.unsubscribe(),this._elementObservables.delete(n)}}).pipe(Qe(e=>e.some(i=>i.target===n)),zc({bufferSize:1,refCount:!0}),Ne(this._destroyed))),this._elementObservables.get(n)}destroy(){this._destroyed.next(),this._destroyed.complete(),this._resizeSubject.complete(),this._elementObservables.clear()}};var em=(()=>{class t{_cleanupErrorListener;_observers=new Map;_ngZone=f(Z);constructor(){}ngOnDestroy(){for(let[,e]of this._observers)e.destroy();this._observers.clear(),this._cleanupErrorListener?.()}observe(e,i){let a=i?.box||`content-box`;return this._observers.has(a)||this._observers.set(a,new sb(a)),this._observers.get(a).observe(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var na=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-label`]]})}return t})();var CR=new N(`MatError`);var lb=(()=>{class t{align=`start`;id=f(Be).getId(`mat-mdc-hint-`);static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-hint`]],hostAttrs:[1,`mat-mdc-form-field-hint`,`mat-mdc-form-field-bottom-align`],hostVars:4,hostBindings:function(i,a){i&2&&(qt(`id`,a.id),ne(`align`,null),G(`mat-mdc-form-field-hint-end`,a.align===`end`))},inputs:{align:`align`,id:`id`}})}return t})();var SR=new N(`MatPrefix`);var wR=new N(`MatSuffix`);var YS=new N(`FloatingLabelParent`);var HS=(()=>{class t{_elementRef=f(Y);get floating(){return this._floating}set floating(e){this._floating=e,this.monitorResize&&this._handleResize()}_floating=!1;get monitorResize(){return this._monitorResize}set monitorResize(e){this._monitorResize=e,this._monitorResize?this._subscribeToResize():this._resizeSubscription.unsubscribe()}_monitorResize=!1;_resizeObserver=f(em);_ngZone=f(Z);_parent=f(YS);_resizeSubscription=new De;ngOnDestroy(){this._resizeSubscription.unsubscribe()}getWidth(){return ER(this._elementRef.nativeElement)}get element(){return this._elementRef.nativeElement}_handleResize(){setTimeout(()=>this._parent._handleLabelResized())}_subscribeToResize(){this._resizeSubscription.unsubscribe(),this._ngZone.runOutsideAngular(()=>{this._resizeSubscription=this._resizeObserver.observe(this._elementRef.nativeElement,{box:`border-box`}).subscribe(()=>this._handleResize())})}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`label`,`matFormFieldFloatingLabel`,``]],hostAttrs:[1,`mdc-floating-label`,`mat-mdc-floating-label`],hostVars:2,hostBindings:function(i,a){i&2&&G(`mdc-floating-label--float-above`,a.floating)},inputs:{floating:`floating`,monitorResize:`monitorResize`}})}return t})();function ER(t){let n=t;if(n.offsetParent!==null)return n.scrollWidth;let e=n.cloneNode(!0);e.style.setProperty(`position`,`absolute`),e.style.setProperty(`transform`,`translate(-9999px, -9999px)`),document.documentElement.appendChild(e);let i=e.scrollWidth;return e.remove(),i}var US=`mdc-line-ripple--active`;var tm=`mdc-line-ripple--deactivating`;var $S=(()=>{class t{_elementRef=f(Y);_cleanupTransitionEnd;constructor(){let e=f(Z),i=f(Oe);e.runOutsideAngular(()=>{this._cleanupTransitionEnd=i.listen(this._elementRef.nativeElement,`transitionend`,this._handleTransitionEnd)})}activate(){let e=this._elementRef.nativeElement.classList;e.remove(tm),e.add(US)}deactivate(){this._elementRef.nativeElement.classList.add(tm)}_handleTransitionEnd=e=>{let i=this._elementRef.nativeElement.classList,a=i.contains(tm);e.propertyName===`opacity`&&a&&i.remove(US,tm)};ngOnDestroy(){this._cleanupTransitionEnd()}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`div`,`matFormFieldLineRipple`,``]],hostAttrs:[1,`mdc-line-ripple`]})}return t})();var GS=(()=>{class t{_elementRef=f(Y);_ngZone=f(Z);open=!1;_notch;ngAfterViewInit(){let e=this._elementRef.nativeElement,i=e.querySelector(`.mdc-floating-label`);i?(e.classList.add(`mdc-notched-outline--upgraded`),typeof requestAnimationFrame==`function`&&(i.style.transitionDuration=`0s`,this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>i.style.transitionDuration=``)}))):e.classList.add(`mdc-notched-outline--no-label`)}_setNotchWidth(e){let i=this._notch.nativeElement;!this.open||!e?i.style.width=``:i.style.width=`calc(${e}px * var(--mat-mdc-form-field-floating-label-scale, 0.75) + 9px)`}_setMaxWidth(e){this._notch.nativeElement.style.setProperty(`--mat-form-field-notch-max-width`,`calc(100% - ${e}px)`)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`notch`];return H({type:t,selectors:[[`div`,`matFormFieldNotchedOutline`,``]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;J(s=ee())&&(r._notch=s.first)}},hostAttrs:[1,`mdc-notched-outline`],hostVars:2,hostBindings:function(o,r){o&2&&G(`mdc-notched-outline--notched`,r.open)},inputs:{open:[0,`matFormFieldNotchedOutlineOpen`,`open`]},ngContentSelectors:[`*`],decls:5,vars:0,consts:[[`notch`,``],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__leading`],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__notch`],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__trailing`]],template:function(o,r){o&1&&(Pe(),Wt(0,`div`,1),lt(1,`div`,2,0),he(3),ht(),Wt(4,`div`,3))},encapsulation:2})})()}return t})();var zl=(()=>{class t{id;ngField=null;ngControl=null;focused=!1;empty=!1;shouldLabelFloat=!1;required=!1;disabled=!1;errorState=!1;controlType;autofilled;userAriaDescribedBy;disableAutomaticLabeling;describedByIds;stateChanges=null;value;static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t})}return t})();var Hl=new N(`MatFormField`);var DR=new N(`MAT_FORM_FIELD_DEFAULT_OPTIONS`);var WS=`fill`;var IR=`auto`;var qS=`fixed`;var kR=`translateY(-50%)`;var Ti=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Fe);_platform=f(Re);_idGenerator=f(Be);_ngZone=f(Z);_defaults=f(DR,{optional:!0});_currentDirection;_unwrapMaybeSignal(e){return ln(e)?e():e}_textField;_iconPrefixContainer;_textPrefixContainer;_iconSuffixContainer;_textSuffixContainer;_floatingLabel;_notchedOutline;_lineRipple;_iconPrefixContainerSignal=pl(`iconPrefixContainer`);_textPrefixContainerSignal=pl(`textPrefixContainer`);_iconSuffixContainerSignal=pl(`iconSuffixContainer`);_textSuffixContainerSignal=pl(`textSuffixContainer`);_prefixSuffixContainers=K(()=>[this._iconPrefixContainerSignal(),this._textPrefixContainerSignal(),this._iconSuffixContainerSignal(),this._textSuffixContainerSignal()].map(e=>e?.nativeElement).filter(e=>e!==void 0));_formFieldControl;_prefixChildren;_suffixChildren;_errorChildren;_hintChildren;_labelChild=Cx(na);get hideRequiredMarker(){return this._hideRequiredMarker}set hideRequiredMarker(e){this._hideRequiredMarker=ta(e)}_hideRequiredMarker=!1;color=`primary`;get floatLabel(){return this._floatLabel||this._defaults?.floatLabel||IR}set floatLabel(e){e!==this._floatLabel&&(this._floatLabel=e,this._changeDetectorRef.markForCheck())}_floatLabel;get appearance(){return this._appearanceSignal()}set appearance(e){let i=e||this._defaults?.appearance||WS;this._appearanceSignal.set(i)}_appearanceSignal=k(WS);get subscriptSizing(){return this._subscriptSizing||this._defaults?.subscriptSizing||qS}set subscriptSizing(e){this._subscriptSizing=e||this._defaults?.subscriptSizing||qS}_subscriptSizing=null;get hintLabel(){return this._hintLabel}set hintLabel(e){this._hintLabel=e,this._processHints()}_hintLabel=``;_hasIconPrefix=!1;_hasTextPrefix=!1;_hasIconSuffix=!1;_hasTextSuffix=!1;_labelId=this._idGenerator.getId(`mat-mdc-form-field-label-`);_hintLabelId=this._idGenerator.getId(`mat-mdc-hint-`);_describedByIds;get _control(){return this._explicitFormFieldControl||this._formFieldControl}set _control(e){this._explicitFormFieldControl=e}_destroyed=new V;_isFocused=null;_explicitFormFieldControl;_previousControl=null;_previousControlValidatorFn=null;_stateChanges;_valueChanges;_describedByChanges;_outlineLabelOffsetResizeObserver=null;_animationsDisabled=Ue();constructor(){let e=this._defaults,i=f(gt);e&&(e.appearance&&(this.appearance=e.appearance),this._hideRequiredMarker=!!e?.hideRequiredMarker,e.color&&(this.color=e.color)),vt(()=>this._currentDirection=i.valueSignal()),this._syncOutlineLabelOffset()}ngAfterViewInit(){this._updateFocusState(),this._animationsDisabled||this._ngZone.runOutsideAngular(()=>{setTimeout(()=>{this._elementRef.nativeElement.classList.add(`mat-form-field-animations-enabled`)},300)}),this._changeDetectorRef.detectChanges()}ngAfterContentInit(){this._assertFormFieldControl(),this._initializeSubscript(),this._initializePrefixAndSuffix()}ngAfterContentChecked(){this._assertFormFieldControl(),this._control!==this._previousControl&&(this._initializeControl(this._previousControl),this._control.ngControl&&this._control.ngControl.control&&(this._previousControlValidatorFn=jl(this._control.ngField)?null:this._control.ngControl.control.validator),this._previousControl=this._control,this._changeDetectorRef.markForCheck()),this._control.ngControl&&this._control.ngControl.control&&!jl(this._control.ngField)&&this._control.ngControl.control.validator!==this._previousControlValidatorFn&&this._changeDetectorRef.markForCheck()}ngOnDestroy(){this._outlineLabelOffsetResizeObserver?.disconnect(),this._stateChanges?.unsubscribe(),this._valueChanges?.unsubscribe(),this._describedByChanges?.unsubscribe(),this._destroyed.next(),this._destroyed.complete()}getLabelId=K(()=>this._hasFloatingLabel()?this._labelId:null);getConnectedOverlayOrigin(){return this._textField||this._elementRef}_animateAndLockLabel(){this._hasFloatingLabel()&&(this.floatLabel=`always`)}_initializeControl(e){let i=this._control,a=`mat-mdc-form-field-type-`;e&&this._elementRef.nativeElement.classList.remove(a+e.controlType),i.controlType&&this._elementRef.nativeElement.classList.add(a+i.controlType),this._stateChanges?.unsubscribe(),this._stateChanges=i.stateChanges?.subscribe(()=>{this._updateFocusState(),this._changeDetectorRef.markForCheck()}),this._describedByChanges?.unsubscribe(),this._describedByChanges=i.stateChanges?.pipe(mt([void 0,void 0]),st(()=>[this._unwrapMaybeSignal(i.errorState),i.userAriaDescribedBy]),jc(),Qe(([[o,r],[s,l]])=>o!==s||r!==l)).subscribe(()=>this._syncDescribedByIds()),this._valueChanges?.unsubscribe(),i.ngControl&&i.ngControl.valueChanges&&!jl(i.ngField)&&(this._valueChanges=i.ngControl.valueChanges.pipe(Ne(this._destroyed)).subscribe(()=>this._changeDetectorRef.markForCheck()))}_checkPrefixAndSuffixTypes(){this._hasIconPrefix=!!this._prefixChildren.find(e=>!e._isText),this._hasTextPrefix=!!this._prefixChildren.find(e=>e._isText),this._hasIconSuffix=!!this._suffixChildren.find(e=>!e._isText),this._hasTextSuffix=!!this._suffixChildren.find(e=>e._isText)}_initializePrefixAndSuffix(){this._checkPrefixAndSuffixTypes(),kt(this._prefixChildren.changes,this._suffixChildren.changes).subscribe(()=>{this._checkPrefixAndSuffixTypes(),this._changeDetectorRef.markForCheck()})}_initializeSubscript(){this._hintChildren.changes.subscribe(()=>{this._processHints(),this._changeDetectorRef.markForCheck()}),this._errorChildren.changes.subscribe(()=>{this._syncDescribedByIds(),this._changeDetectorRef.markForCheck()}),this._validateHints(),this._syncDescribedByIds()}_assertFormFieldControl(){this._control}_updateFocusState(){let e=this._unwrapMaybeSignal(this._control.focused);e&&!this._isFocused?(this._isFocused=!0,this._lineRipple?.activate()):!e&&(this._isFocused||this._isFocused===null)&&(this._isFocused=!1,this._lineRipple?.deactivate()),this._elementRef.nativeElement.classList.toggle(`mat-focused`,e),this._textField?.nativeElement.classList.toggle(`mdc-text-field--focused`,e)}_syncOutlineLabelOffset(){rg({earlyRead:()=>{if(this._appearanceSignal()!==`outline`)return this._outlineLabelOffsetResizeObserver?.disconnect(),null;if(globalThis.ResizeObserver){this._outlineLabelOffsetResizeObserver||=new globalThis.ResizeObserver(()=>{this._writeOutlinedLabelStyles(this._getOutlinedLabelOffset())});for(let e of this._prefixSuffixContainers())this._outlineLabelOffsetResizeObserver.observe(e,{box:`border-box`})}return this._getOutlinedLabelOffset()},write:e=>this._writeOutlinedLabelStyles(e())})}_shouldAlwaysFloat(){return this.floatLabel===`always`}_hasOutline(){return this.appearance===`outline`}_forceDisplayInfixLabel(){return!this._platform.isBrowser&&this._prefixChildren.length&&!this._shouldLabelFloat()}_hasFloatingLabel=K(()=>!!this._labelChild());_shouldLabelFloat(){return this._hasFloatingLabel()?this._shouldAlwaysFloat()||this._unwrapMaybeSignal(this._control.shouldLabelFloat):!1}_shouldForward(e){let i=this._control?.ngField||this._control?.ngControl;if(!i)return!1;if(jl(i)){let a=i();return e===`valid`?a.valid():e===`dirty`?a.dirty():e===`touched`?a.touched():e===`pending`?a.pending():e===`untouched`?!a.touched():e===`pristine`?!a.dirty():e===`invalid`?!a.valid():!1}else{let a=i;return e===`valid`?a.valid:e===`dirty`?a.dirty:e===`touched`?a.touched:e===`pending`?a.pending:e===`untouched`?a.untouched:e===`pristine`?a.pristine:e===`invalid`?a.invalid:!1}}_getSubscriptMessageType(){return this._errorChildren&&this._errorChildren.length>0&&this._unwrapMaybeSignal(this._control.errorState)?`error`:`hint`}_handleLabelResized(){this._refreshOutlineNotchWidth()}_refreshOutlineNotchWidth(){!this._hasOutline()||!this._floatingLabel||!this._shouldLabelFloat()?this._notchedOutline?._setNotchWidth(0):this._notchedOutline?._setNotchWidth(this._floatingLabel.getWidth())}_processHints(){this._validateHints(),this._syncDescribedByIds()}_validateHints(){this._hintChildren}_syncDescribedByIds(){if(this._control){let e=[];if(this._control.userAriaDescribedBy&&typeof this._control.userAriaDescribedBy==`string`&&e.push(...this._control.userAriaDescribedBy.split(` `)),this._getSubscriptMessageType()===`hint`){let o=this._hintChildren?this._hintChildren.find(s=>s.align===`start`):null,r=this._hintChildren?this._hintChildren.find(s=>s.align===`end`):null;o?e.push(o.id):this._hintLabel&&e.push(this._hintLabelId),r&&e.push(r.id)}else this._errorChildren&&e.push(...this._errorChildren.map(o=>o.id));let i=this._control.describedByIds,a;if(i){let o=this._describedByIds||e;a=e.concat(i.filter(r=>r&&!o.includes(r)))}else a=e;this._control.setDescribedByIds(a),this._describedByIds=e}}_getOutlinedLabelOffset(){if(!this._hasOutline()||!this._floatingLabel)return null;if(!this._iconPrefixContainer&&!this._textPrefixContainer)return[``,null];if(!this._isAttachedToDom())return null;let e=this._iconPrefixContainer?.nativeElement,i=this._textPrefixContainer?.nativeElement,a=this._iconSuffixContainer?.nativeElement,o=this._textSuffixContainer?.nativeElement,r=e?.getBoundingClientRect().width??0,s=i?.getBoundingClientRect().width??0,l=a?.getBoundingClientRect().width??0,h=o?.getBoundingClientRect().width??0;return[`var(--mat-mdc-form-field-label-transform, ${kR} translateX(${`calc(${this._currentDirection===`rtl`?`-1`:`1`} * (${`${r+s}px`} + var(--mat-mdc-form-field-label-offset-x, 0px)))`}))`,r+s+l+h]}_writeOutlinedLabelStyles(e){if(e!==null){let[i,a]=e;this._floatingLabel&&(this._floatingLabel.element.style.transform=i),a!==null&&this._notchedOutline?._setMaxWidth(a)}}_isAttachedToDom(){let e=this._elementRef.nativeElement;if(e.getRootNode){let i=e.getRootNode();return i&&i!==e}return document.documentElement.contains(e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`iconPrefixContainer`],i=[`textPrefixContainer`],a=[`iconSuffixContainer`],o=[`textSuffixContainer`],r=[`textField`],s=[`*`,[[`mat-label`]],[[``,`matPrefix`,``],[``,`matIconPrefix`,``]],[[``,`matTextPrefix`,``]],[[``,`matTextSuffix`,``]],[[``,`matSuffix`,``],[``,`matIconSuffix`,``]],[[`mat-error`],[``,`matError`,``]],[[`mat-hint`,3,`align`,`end`]],[[`mat-hint`,`align`,`end`]]],l=[`*`,`mat-label`,`[matPrefix], [matIconPrefix]`,`[matTextPrefix]`,`[matTextSuffix]`,`[matSuffix], [matIconSuffix]`,`mat-error, [matError]`,`mat-hint:not([align='end'])`,`mat-hint[align='end']`];function h(pe,Ie){pe&1&&U(0,`span`,21)}function p(pe,Ie){if(pe&1&&(c(0,`label`,20),he(1,1),E(2,h,1,0,`span`,21),d()),pe&2){let F=v(2);S(`floating`,F._shouldLabelFloat())(`monitorResize`,F._hasOutline())(`id`,F._labelId),ne(`for`,F._control.disableAutomaticLabeling?null:F._control.id),m(2),D(!F.hideRequiredMarker&&F._unwrapMaybeSignal(F._control.required)?2:-1)}}function g(pe,Ie){if(pe&1&&E(0,p,3,5,`label`,20),pe&2)D(v()._hasFloatingLabel()?0:-1)}function _(pe,Ie){pe&1&&U(0,`div`,7)}function b(pe,Ie){}function y(pe,Ie){if(pe&1&&rt(0,b,0,0,`ng-template`,13),pe&2){v(2);S(`ngTemplateOutlet`,ft(1))}}function I(pe,Ie){if(pe&1&&(c(0,`div`,9),E(1,y,1,1,null,13),d()),pe&2){let F=v();S(`matFormFieldNotchedOutlineOpen`,F._shouldLabelFloat()),m(),D(F._forceDisplayInfixLabel()?-1:1)}}function P(pe,Ie){pe&1&&(c(0,`div`,10,2),he(2,2),d())}function ie(pe,Ie){pe&1&&(c(0,`div`,11,3),he(2,3),d())}function L(pe,Ie){}function x(pe,Ie){if(pe&1&&rt(0,L,0,0,`ng-template`,13),pe&2){v();S(`ngTemplateOutlet`,ft(1))}}function A(pe,Ie){pe&1&&(c(0,`div`,14,4),he(2,4),d())}function R(pe,Ie){pe&1&&(c(0,`div`,15,5),he(2,5),d())}function W(pe,Ie){pe&1&&U(0,`div`,16)}function ye(pe,Ie){pe&1&&(c(0,`div`,18),he(1,6),d())}function bt(pe,Ie){if(pe&1&&(c(0,`mat-hint`,22),u(1),d()),pe&2){let F=v(2);S(`id`,F._hintLabelId),m(),w(F.hintLabel)}}function We(pe,Ie){if(pe&1&&(c(0,`div`,19),E(1,bt,2,2,`mat-hint`,22),he(2,7),U(3,`div`,23),he(4,8),d()),pe&2){let F=v();m(),D(F.hintLabel?1:-1)}}return H({type:t,selectors:[[`mat-form-field`]],contentQueries:function(Ie,F,qe){if(Ie&1&&(Xd(qe,F._labelChild,na,5),jt(qe,zl,5)(qe,SR,5)(qe,wR,5)(qe,CR,5)(qe,lb,5)),Ie&2){Jd();let an;J(an=ee())&&(F._formFieldControl=an.first),J(an=ee())&&(F._prefixChildren=an),J(an=ee())&&(F._suffixChildren=an),J(an=ee())&&(F._errorChildren=an),J(an=ee())&&(F._hintChildren=an)}},viewQuery:function(Ie,F){if(Ie&1&&(Zd(F._iconPrefixContainerSignal,e,5)(F._textPrefixContainerSignal,i,5)(F._iconSuffixContainerSignal,a,5)(F._textSuffixContainerSignal,o,5),Ze(r,5)(e,5)(i,5)(a,5)(o,5)(HS,5)(GS,5)($S,5)),Ie&2){Jd(4);let qe;J(qe=ee())&&(F._textField=qe.first),J(qe=ee())&&(F._iconPrefixContainer=qe.first),J(qe=ee())&&(F._textPrefixContainer=qe.first),J(qe=ee())&&(F._iconSuffixContainer=qe.first),J(qe=ee())&&(F._textSuffixContainer=qe.first),J(qe=ee())&&(F._floatingLabel=qe.first),J(qe=ee())&&(F._notchedOutline=qe.first),J(qe=ee())&&(F._lineRipple=qe.first)}},hostAttrs:[1,`mat-mdc-form-field`],hostVars:38,hostBindings:function(Ie,F){Ie&2&&G(`mat-mdc-form-field-label-always-float`,F._shouldAlwaysFloat())(`mat-mdc-form-field-has-icon-prefix`,F._hasIconPrefix)(`mat-mdc-form-field-has-icon-suffix`,F._hasIconSuffix)(`mat-form-field-invalid`,F._unwrapMaybeSignal(F._control.errorState))(`mat-form-field-disabled`,F._unwrapMaybeSignal(F._control.disabled))(`mat-form-field-autofilled`,F._unwrapMaybeSignal(F._control.autofilled))(`mat-form-field-appearance-fill`,F.appearance==`fill`)(`mat-form-field-appearance-outline`,F.appearance==`outline`)(`mat-form-field-hide-placeholder`,F._hasFloatingLabel()&&!F._shouldLabelFloat())(`mat-primary`,F.color!==`accent`&&F.color!==`warn`)(`mat-accent`,F.color===`accent`)(`mat-warn`,F.color===`warn`)(`ng-untouched`,F._shouldForward(`untouched`))(`ng-touched`,F._shouldForward(`touched`))(`ng-pristine`,F._shouldForward(`pristine`))(`ng-dirty`,F._shouldForward(`dirty`))(`ng-valid`,F._shouldForward(`valid`))(`ng-invalid`,F._shouldForward(`invalid`))(`ng-pending`,F._shouldForward(`pending`))},inputs:{hideRequiredMarker:`hideRequiredMarker`,color:`color`,floatLabel:`floatLabel`,appearance:`appearance`,subscriptSizing:`subscriptSizing`,hintLabel:`hintLabel`},exportAs:[`matFormField`],features:[Ge([{provide:Hl,useExisting:t},{provide:YS,useExisting:t}])],ngContentSelectors:l,decls:18,vars:21,consts:[[`labelTemplate`,``],[`textField`,``],[`iconPrefixContainer`,``],[`textPrefixContainer`,``],[`textSuffixContainer`,``],[`iconSuffixContainer`,``],[1,`mat-mdc-text-field-wrapper`,`mdc-text-field`,3,`click`],[1,`mat-mdc-form-field-focus-overlay`],[1,`mat-mdc-form-field-flex`],[`matFormFieldNotchedOutline`,``,3,`matFormFieldNotchedOutlineOpen`],[1,`mat-mdc-form-field-icon-prefix`],[1,`mat-mdc-form-field-text-prefix`],[1,`mat-mdc-form-field-infix`],[3,`ngTemplateOutlet`],[1,`mat-mdc-form-field-text-suffix`],[1,`mat-mdc-form-field-icon-suffix`],[`matFormFieldLineRipple`,``],[`aria-atomic`,`true`,`aria-live`,`polite`,1,`mat-mdc-form-field-subscript-wrapper`,`mat-mdc-form-field-bottom-align`],[1,`mat-mdc-form-field-error-wrapper`],[1,`mat-mdc-form-field-hint-wrapper`],[`matFormFieldFloatingLabel`,``,3,`floating`,`monitorResize`,`id`],[`aria-hidden`,`true`,1,`mat-mdc-form-field-required-marker`,`mdc-floating-label--required`],[3,`id`],[1,`mat-mdc-form-field-hint-spacer`]],template:function(Ie,F){if(Ie&1&&(Pe(s),rt(0,g,1,1,`ng-template`,null,0,dl),c(2,`div`,6,1),C(`click`,function(an){return F._control.onContainerClick(an)}),E(4,_,1,0,`div`,7),c(5,`div`,8),E(6,I,2,2,`div`,9),E(7,P,3,0,`div`,10),E(8,ie,3,0,`div`,11),c(9,`div`,12),E(10,x,1,1,null,13),he(11),d(),E(12,A,3,0,`div`,14),E(13,R,3,0,`div`,15),d(),E(14,W,1,0,`div`,16),d(),c(15,`div`,17),E(16,ye,2,0,`div`,18)(17,We,5,1,`div`,19),d()),Ie&2){let qe,an=F._unwrapMaybeSignal(F._control.disabled);m(2),G(`mdc-text-field--filled`,!F._hasOutline())(`mdc-text-field--outlined`,F._hasOutline())(`mdc-text-field--no-label`,!F._hasFloatingLabel())(`mdc-text-field--disabled`,an)(`mdc-text-field--invalid`,F._unwrapMaybeSignal(F._control.errorState)),m(2),D(!F._hasOutline()&&!an?4:-1),m(2),D(F._hasOutline()?6:-1),m(),D(F._hasIconPrefix?7:-1),m(),D(F._hasTextPrefix?8:-1),m(2),D(!F._hasOutline()||F._forceDisplayInfixLabel()?10:-1),m(2),D(F._hasTextSuffix?12:-1),m(),D(F._hasIconSuffix?13:-1),m(),D(F._hasOutline()?-1:14),m(),G(`mat-mdc-form-field-subscript-dynamic-size`,F.subscriptSizing===`dynamic`);let dE=F._getSubscriptMessageType();m(),D((qe=dE)===`error`?16:qe===`hint`?17:-1)}},dependencies:[HS,GS,_l,$S,lb],styles:[`.mdc-text-field {
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
`],encapsulation:2})})()}return t})();var MR=new N(`mat-chips-default-options`,{providedIn:`root`,factory:()=>({separatorKeyCodes:[13]})});var KS=new N(`MatChipAvatar`);var QS=new N(`MatChipTrailingIcon`);var XS=new N(`MatChipEdit`);var ZS=new N(`MatChipRemove`);var JS=new N(`MatChip`);var ew=(()=>{class t{_elementRef=f(Y);_parentChip=f(JS);_isPrimary=!0;_isLeading=!1;get disabled(){return this._disabled||this._parentChip?.disabled||!1}set disabled(e){this._disabled=e}_disabled=!1;tabIndex=-1;_allowFocusWhenDisabled=!1;_getDisabledAttribute(){return this.disabled&&!this._allowFocusWhenDisabled?``:null}constructor(){f(Je).load(mn),this._elementRef.nativeElement.nodeName===`BUTTON`&&this._elementRef.nativeElement.setAttribute(`type`,`button`)}focus(){this._elementRef.nativeElement.focus()}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`matChipContent`,``]],hostAttrs:[1,`mat-mdc-chip-action`,`mdc-evolution-chip__action`,`mdc-evolution-chip__action--presentational`],hostVars:8,hostBindings:function(i,a){i&2&&(ne(`disabled`,a._getDisabledAttribute())(`aria-disabled`,a.disabled),G(`mdc-evolution-chip__action--primary`,a._isPrimary)(`mdc-evolution-chip__action--secondary`,!a._isPrimary)(`mdc-evolution-chip__action--trailing`,!a._isPrimary&&!a._isLeading))},inputs:{disabled:[2,`disabled`,`disabled`,z],tabIndex:[2,`tabIndex`,`tabIndex`,e=>e==null?-1:ct(e)],_allowFocusWhenDisabled:`_allowFocusWhenDisabled`}})}return t})();var TR=(()=>{class t extends ew{_getTabindex(){return this.disabled&&!this._allowFocusWhenDisabled?null:this.tabIndex.toString()}_handleClick(e){!this.disabled&&this._isPrimary&&(e.preventDefault(),this._parentChip._handlePrimaryActionInteraction())}_handleKeydown(e){(e.keyCode===13||e.keyCode===32)&&!this.disabled&&this._isPrimary&&!this._parentChip._isEditing&&(e.preventDefault(),this._parentChip._handlePrimaryActionInteraction())}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`matChipAction`,``]],hostVars:3,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._handleClick(r)})(`keydown`,function(r){return a._handleKeydown(r)}),i&2&&(ne(`tabindex`,a._getTabindex()),G(`mdc-evolution-chip__action--presentational`,!1))},features:[Ve]})}return t})();var Ul=(()=>{class t{_changeDetectorRef=f(Fe);_elementRef=f(Y);_tagName=f(_x);_ngZone=f(Z);_focusMonitor=f(en);_globalRippleOptions=f(Ur,{optional:!0});_document=f(de);_onFocus=new V;_onBlur=new V;_isBasicChip=!1;role=null;_hasFocusInternal=!1;_pendingFocus=!1;_actionChanges;_animationsDisabled=Ue();_allLeadingIcons;_allTrailingIcons;_allEditIcons;_allRemoveIcons;_hasFocus(){return this._hasFocusInternal}id=f(Be).getId(`mat-mdc-chip-`);ariaLabel=null;ariaDescription=null;_chipListDisabled=!1;_hadFocusOnRemove=!1;_textElement;get value(){return this._value!==void 0?this._value:this._textElement.textContent.trim()}set value(e){this._value=e}_value;color;removable=!0;highlighted=!1;disableRipple=!1;get disabled(){return this._disabled||this._chipListDisabled}set disabled(e){this._disabled=e}_disabled=!1;removed=new te;destroyed=new te;basicChipAttrName=`mat-basic-chip`;leadingIcon;editIcon;trailingIcon;removeIcon;primaryAction;_rippleLoader=f(Vu);_injector=f(se);constructor(){let e=f(Je);e.load(mn),e.load(Ii),this._monitorFocus(),this._rippleLoader?.configureRipple(this._elementRef.nativeElement,{className:`mat-mdc-chip-ripple`,disabled:this._isRippleDisabled()})}ngOnInit(){this._isBasicChip=this._elementRef.nativeElement.hasAttribute(this.basicChipAttrName)||this._tagName.toLowerCase()===this.basicChipAttrName}ngAfterViewInit(){this._textElement=this._elementRef.nativeElement.querySelector(`.mat-mdc-chip-action-label`),this._pendingFocus&&(this._pendingFocus=!1,this.focus())}ngAfterContentInit(){this._actionChanges=kt(this._allLeadingIcons.changes,this._allTrailingIcons.changes,this._allEditIcons.changes,this._allRemoveIcons.changes).subscribe(()=>this._changeDetectorRef.markForCheck())}ngDoCheck(){this._rippleLoader.setDisabled(this._elementRef.nativeElement,this._isRippleDisabled())}ngOnDestroy(){this.destroyed.emit({chip:this}),this.destroyed.complete(),this._focusMonitor.stopMonitoring(this._elementRef),this._rippleLoader?.destroyRipple(this._elementRef.nativeElement),this._actionChanges?.unsubscribe()}remove(){this.removable&&(this._hadFocusOnRemove=this._hasFocus(),this.removed.emit({chip:this}))}_isRippleDisabled(){return this.disabled||this.disableRipple||this._animationsDisabled||this._isBasicChip||!this._hasInteractiveActions()||!!this._globalRippleOptions?.disabled}_hasTrailingIcon(){return!!(this.trailingIcon||this.removeIcon)}_handleKeydown(e){(e.keyCode===8&&!e.repeat||e.keyCode===46)&&(e.preventDefault(),this.remove())}focus(){this.disabled||(this.primaryAction?this.primaryAction.focus():this._pendingFocus=!0)}_getSourceAction(e){return this._getActions().find(i=>{let a=i._elementRef.nativeElement;return a===e||a.contains(e)})}_getActions(){let e=[];return this.editIcon&&e.push(this.editIcon),this.primaryAction&&e.push(this.primaryAction),this.removeIcon&&e.push(this.removeIcon),e}_handlePrimaryActionInteraction(){}_hasInteractiveActions(){return this._getActions().length>0}_edit(e){}_monitorFocus(){this._focusMonitor.monitor(this._elementRef,!0).subscribe(e=>{let i=e!==null;i!==this._hasFocusInternal&&(this._hasFocusInternal=i,i?this._onFocus.next({chip:this}):(this._changeDetectorRef.markForCheck(),setTimeout(()=>this._ngZone.run(()=>this._onBlur.next({chip:this})))))})}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[`mat-chip-avatar`],[``,`matChipAvatar`,``]],[[`mat-chip-trailing-icon`],[``,`matChipRemove`,``],[``,`matChipTrailingIcon`,``]]],i=[`*`,`mat-chip-avatar, [matChipAvatar]`,`mat-chip-trailing-icon,[matChipRemove],[matChipTrailingIcon]`];function a(r,s){r&1&&(c(0,`span`,3),he(1,1),d())}function o(r,s){r&1&&(c(0,`span`,6),he(1,2),d())}return H({type:t,selectors:[[`mat-basic-chip`],[``,`mat-basic-chip`,``],[`mat-chip`],[``,`mat-chip`,``]],contentQueries:function(s,l,h){if(s&1&&jt(h,KS,5)(h,XS,5)(h,QS,5)(h,ZS,5)(h,KS,5)(h,QS,5)(h,XS,5)(h,ZS,5),s&2){let p;J(p=ee())&&(l.leadingIcon=p.first),J(p=ee())&&(l.editIcon=p.first),J(p=ee())&&(l.trailingIcon=p.first),J(p=ee())&&(l.removeIcon=p.first),J(p=ee())&&(l._allLeadingIcons=p),J(p=ee())&&(l._allTrailingIcons=p),J(p=ee())&&(l._allEditIcons=p),J(p=ee())&&(l._allRemoveIcons=p)}},viewQuery:function(s,l){if(s&1&&Ze(TR,5),s&2){let h;J(h=ee())&&(l.primaryAction=h.first)}},hostAttrs:[1,`mat-mdc-chip`],hostVars:31,hostBindings:function(s,l){s&1&&C(`keydown`,function(p){return l._handleKeydown(p)}),s&2&&(qt(`id`,l.id),ne(`role`,l.role)(`aria-label`,l.ariaLabel),tt(`mat-`+(l.color||`primary`)),G(`mdc-evolution-chip`,!l._isBasicChip)(`mdc-evolution-chip--disabled`,l.disabled)(`mdc-evolution-chip--with-trailing-action`,l._hasTrailingIcon())(`mdc-evolution-chip--with-primary-graphic`,l.leadingIcon)(`mdc-evolution-chip--with-primary-icon`,l.leadingIcon)(`mdc-evolution-chip--with-avatar`,l.leadingIcon)(`mat-mdc-chip-with-avatar`,l.leadingIcon)(`mat-mdc-chip-highlighted`,l.highlighted)(`mat-mdc-chip-disabled`,l.disabled)(`mat-mdc-basic-chip`,l._isBasicChip)(`mat-mdc-standard-chip`,!l._isBasicChip)(`mat-mdc-chip-with-trailing-icon`,l._hasTrailingIcon())(`_mat-animation-noopable`,l._animationsDisabled))},inputs:{role:`role`,id:`id`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaDescription:[0,`aria-description`,`ariaDescription`],value:`value`,color:`color`,removable:[2,`removable`,`removable`,z],highlighted:[2,`highlighted`,`highlighted`,z],disableRipple:[2,`disableRipple`,`disableRipple`,z],disabled:[2,`disabled`,`disabled`,z]},outputs:{removed:`removed`,destroyed:`destroyed`},exportAs:[`matChip`],features:[Ge([{provide:JS,useExisting:t}])],ngContentSelectors:i,decls:8,vars:2,consts:[[1,`mat-mdc-chip-focus-overlay`],[1,`mdc-evolution-chip__cell`,`mdc-evolution-chip__cell--primary`],[`matChipContent`,``],[1,`mdc-evolution-chip__graphic`,`mat-mdc-chip-graphic`],[1,`mdc-evolution-chip__text-label`,`mat-mdc-chip-action-label`],[1,`mat-mdc-chip-primary-focus-indicator`,`mat-focus-indicator`],[1,`mdc-evolution-chip__cell`,`mdc-evolution-chip__cell--trailing`]],template:function(s,l){s&1&&(Pe(e),U(0,`span`,0),c(1,`span`,1)(2,`span`,2),E(3,a,2,0,`span`,3),c(4,`span`,4),he(5),U(6,`span`,5),d()()(),E(7,o,2,0,`span`,6)),s&2&&(m(3),D(l.leadingIcon?3:-1),m(4),D(l._hasTrailingIcon()?7:-1))},dependencies:[ew],styles:[`.mdc-evolution-chip,
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
`],encapsulation:2})})()}return t})();var nm=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Fe);_dir=f(gt,{optional:!0});_lastDestroyedFocusedChipIndex=null;_keyManager;_destroyed=new V;_defaultRole=`presentation`;get chipFocusChanges(){return this._getChipStream(e=>e._onFocus)}get chipDestroyedChanges(){return this._getChipStream(e=>e.destroyed)}get chipRemovedChanges(){return this._getChipStream(e=>e.removed)}get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._syncChipsState()}_disabled=!1;get empty(){return!this._chips||this._chips.length===0}get role(){return this._explicitRole?this._explicitRole:this.empty?null:this._defaultRole}tabIndex=0;set role(e){this._explicitRole=e}_explicitRole=null;get focused(){return this._hasFocusedChip()}_chips;_chipActions=new wn;ngAfterViewInit(){this._setUpFocusManagement(),this._trackChipSetChanges(),this._trackDestroyedFocusedChip()}ngOnDestroy(){this._keyManager?.destroy(),this._chipActions.destroy(),this._destroyed.next(),this._destroyed.complete()}_hasFocusedChip(){return this._chips&&this._chips.some(e=>e._hasFocus())}_syncChipsState(){this._chips?.forEach(e=>{e._chipListDisabled=this._disabled,e._changeDetectorRef.markForCheck()})}focus(){}_handleKeydown(e){this._originatesFromChip(e)&&this._keyManager.onKeydown(e)}_isValidIndex(e){return e>=0&&e<this._chips.length}_allowFocusEscape(){let e=this._elementRef.nativeElement.tabIndex;e!==-1&&(this._elementRef.nativeElement.tabIndex=-1,setTimeout(()=>this._elementRef.nativeElement.tabIndex=e))}_getChipStream(e){return this._chips.changes.pipe(mt(null),_n(()=>kt(...this._chips.map(e))))}_originatesFromChip(e){let i=e.target;for(;i&&i!==this._elementRef.nativeElement;){if(i.classList.contains(`mat-mdc-chip`))return!0;i=i.parentElement}return!1}_setUpFocusManagement(){this._chips.changes.pipe(mt(this._chips)).subscribe(e=>{let i=[];e.forEach(a=>a._getActions().forEach(o=>i.push(o))),this._chipActions.reset(i),this._chipActions.notifyOnChanges()}),this._keyManager=new ea(this._chipActions).withVerticalOrientation().withHorizontalOrientation(this._dir?this._dir.value:`ltr`).withHomeAndEnd().skipPredicate(e=>this._skipPredicate(e)),this.chipFocusChanges.pipe(Ne(this._destroyed)).subscribe(({chip:e})=>{let i=e._getSourceAction(document.activeElement);i&&this._keyManager.updateActiveItem(i)}),this._dir?.change.pipe(Ne(this._destroyed)).subscribe(e=>this._keyManager.withHorizontalOrientation(e))}_skipPredicate(e){return e.disabled}_trackChipSetChanges(){this._chips.changes.pipe(mt(null),Ne(this._destroyed)).subscribe(()=>{this.disabled&&Promise.resolve().then(()=>this._syncChipsState()),this._redirectDestroyedChipFocus()})}_trackDestroyedFocusedChip(){this.chipDestroyedChanges.pipe(Ne(this._destroyed)).subscribe(e=>{let a=this._chips.toArray().indexOf(e.chip),o=e.chip._hasFocus(),r=e.chip._hadFocusOnRemove&&this._keyManager.activeItem&&e.chip._getActions().includes(this._keyManager.activeItem),s=o||r;this._isValidIndex(a)&&s&&(this._lastDestroyedFocusedChipIndex=a)})}_redirectDestroyedChipFocus(){if(this._lastDestroyedFocusedChipIndex!=null){if(this._chips.length){let e=Math.min(this._lastDestroyedFocusedChipIndex,this._chips.length-1),i=this._chips.toArray()[e];i.disabled?this._chips.length===1?this.focus():this._keyManager.setPreviousItemActive():i.focus()}else this.focus();this._lastDestroyedFocusedChipIndex=null}}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return H({type:t,selectors:[[`mat-chip-set`]],contentQueries:function(a,o,r){if(a&1&&jt(r,Ul,5),a&2){let s;J(s=ee())&&(o._chips=s)}},hostAttrs:[1,`mat-mdc-chip-set`,`mdc-evolution-chip-set`],hostVars:1,hostBindings:function(a,o){a&1&&C(`keydown`,function(s){return o._handleKeydown(s)}),a&2&&ne(`role`,o.role)},inputs:{disabled:[2,`disabled`,`disabled`,z],role:`role`,tabIndex:[2,`tabIndex`,`tabIndex`,i=>i==null?0:ct(i)]},ngContentSelectors:[`*`],decls:2,vars:0,consts:[[`role`,`presentation`,1,`mdc-evolution-chip-set__chips`]],template:function(a,o){a&1&&(Pe(),lt(0,`div`,0),he(1),ht())},styles:[`.mat-mdc-chip-set {
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
`],encapsulation:2})})()}return t})();var im=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({providers:[Qr,{provide:MR,useValue:{separatorKeyCodes:[13]}}],imports:[ki,we]})}return t})();var nw=(()=>{class t{labelPosition=`after`;static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return H({type:t,selectors:[[``,`mat-internal-form-field`,``]],hostAttrs:[1,`mdc-form-field`,`mat-internal-form-field`],hostVars:2,hostBindings:function(a,o){a&2&&G(`mdc-form-field--align-end`,o.labelPosition===`before`)},inputs:{labelPosition:`labelPosition`},ngContentSelectors:[`*`],decls:1,vars:0,template:function(a,o){a&1&&(Pe(),he(0))},styles:[`.mat-internal-form-field {
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
`],encapsulation:2})})()}return t})();var cb={color:`accent`,clickAction:`check-indeterminate`,disabledInteractive:!1};var NR=new N(`mat-checkbox-default-options`,{providedIn:`root`,factory:()=>cb});var nn=(function(t){return t[t.Init=0]=`Init`,t[t.Checked=1]=`Checked`,t[t.Unchecked=2]=`Unchecked`,t[t.Indeterminate=3]=`Indeterminate`,t})(nn||{});var db=class{source;checked};var $l=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Fe);_ngZone=f(Z);_animationsDisabled=Ue();_options=f(NR,{optional:!0});focus(){this._inputElement.nativeElement.focus()}_createChangeEvent(e){let i=new db;return i.source=this,i.checked=e,i}_getAnimationTargetElement(){return this._inputElement?.nativeElement}_animationClasses={uncheckedToChecked:`mdc-checkbox--anim-unchecked-checked`,uncheckedToIndeterminate:`mdc-checkbox--anim-unchecked-indeterminate`,checkedToUnchecked:`mdc-checkbox--anim-checked-unchecked`,checkedToIndeterminate:`mdc-checkbox--anim-checked-indeterminate`,indeterminateToChecked:`mdc-checkbox--anim-indeterminate-checked`,indeterminateToUnchecked:`mdc-checkbox--anim-indeterminate-unchecked`};ariaLabel=``;ariaLabelledby=null;ariaDescribedby;ariaExpanded;ariaControls;ariaOwns;_uniqueId;id;get inputId(){return`${this.id||this._uniqueId}-input`}required=!1;labelPosition=`after`;name=null;change=new te;indeterminateChange=new te;value;disableRipple=!1;_inputElement;tabIndex;color;disabledInteractive;_onTouched=()=>{};_currentAnimationClass=``;_currentCheckState=nn.Init;_controlValueAccessorChangeFn=()=>{};_validatorChangeFn=()=>{};constructor(){f(Je).load(mn);let e=f(new Ki(`tabindex`),{optional:!0});this._options=this._options||cb,this.color=this._options.color||cb.color,this.tabIndex=e==null?0:parseInt(e)||0,this.id=this._uniqueId=f(Be).getId(`mat-mdc-checkbox-`),this.disabledInteractive=this._options?.disabledInteractive??!1}ngOnChanges(e){e.required&&this._validatorChangeFn()}ngAfterViewInit(){this._syncIndeterminate(this.indeterminate)}get checked(){return this._checked}set checked(e){e!=this.checked&&(this._checked=e,this._changeDetectorRef.markForCheck())}_checked=!1;get disabled(){return this._disabled}set disabled(e){e!==this.disabled&&(this._disabled=e,this._changeDetectorRef.markForCheck())}_disabled=!1;get indeterminate(){return this._indeterminate()}set indeterminate(e){let i=e!=this._indeterminate();this._indeterminate.set(e),i&&(e?this._transitionCheckState(nn.Indeterminate):this._transitionCheckState(this.checked?nn.Checked:nn.Unchecked),this.indeterminateChange.emit(e)),this._syncIndeterminate(e)}_indeterminate=k(!1);_isRippleDisabled(){return this.disableRipple||this.disabled}_onLabelTextChange(){this._changeDetectorRef.detectChanges()}writeValue(e){this.checked=!!e}registerOnChange(e){this._controlValueAccessorChangeFn=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e}validate(e){return this.required&&e.value!==!0?{required:!0}:null}registerOnValidatorChange(e){this._validatorChangeFn=e}_transitionCheckState(e){let i=this._currentCheckState,a=this._getAnimationTargetElement();if(!(i===e||!a)&&(this._currentAnimationClass&&a.classList.remove(this._currentAnimationClass),this._currentAnimationClass=this._getAnimationClassForCheckStateTransition(i,e),this._currentCheckState=e,this._currentAnimationClass.length>0)){a.classList.add(this._currentAnimationClass);let o=this._currentAnimationClass;this._ngZone.runOutsideAngular(()=>{setTimeout(()=>{a.classList.remove(o)},1e3)})}}_emitChangeEvent(){this._controlValueAccessorChangeFn(this.checked),this.change.emit(this._createChangeEvent(this.checked)),this._inputElement&&(this._inputElement.nativeElement.checked=this.checked)}toggle(){this.checked=!this.checked,this._controlValueAccessorChangeFn(this.checked)}_handleInputClick(){let e=this._options?.clickAction;!this.disabled&&e!==`noop`?(this.indeterminate&&e!==`check`&&Promise.resolve().then(()=>{this._indeterminate.set(!1),this.indeterminateChange.emit(!1)}),this._checked=!this._checked,this._transitionCheckState(this._checked?nn.Checked:nn.Unchecked),this._emitChangeEvent()):(this.disabled&&this.disabledInteractive||!this.disabled&&e===`noop`)&&(this._inputElement.nativeElement.checked=this.checked,this._inputElement.nativeElement.indeterminate=this.indeterminate)}_onInteractionEvent(e){e.stopPropagation()}_onBlur(){Promise.resolve().then(()=>{this._onTouched(),this._changeDetectorRef.markForCheck()})}_getAnimationClassForCheckStateTransition(e,i){if(this._animationsDisabled)return``;switch(e){case nn.Init:if(i===nn.Checked)return this._animationClasses.uncheckedToChecked;if(i==nn.Indeterminate)return this._checked?this._animationClasses.checkedToIndeterminate:this._animationClasses.uncheckedToIndeterminate;break;case nn.Unchecked:return i===nn.Checked?this._animationClasses.uncheckedToChecked:this._animationClasses.uncheckedToIndeterminate;case nn.Checked:return i===nn.Unchecked?this._animationClasses.checkedToUnchecked:this._animationClasses.checkedToIndeterminate;case nn.Indeterminate:return i===nn.Checked?this._animationClasses.indeterminateToChecked:this._animationClasses.indeterminateToUnchecked}return``}_syncIndeterminate(e){let i=this._inputElement;i&&(i.nativeElement.indeterminate=e)}_onInputClick(){this._handleInputClick()}_preventBubblingFromLabel(e){e.target&&this._inputElement&&e.target!==this._inputElement.nativeElement&&e.stopPropagation()}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`input`];return H({type:t,selectors:[[`mat-checkbox`]],viewQuery:function(o,r){if(o&1&&Ze(e,5),o&2){let s;J(s=ee())&&(r._inputElement=s.first)}},hostAttrs:[1,`mat-mdc-checkbox`],hostVars:16,hostBindings:function(o,r){o&2&&(qt(`id`,r.id),ne(`tabindex`,null)(`aria-label`,null)(`aria-labelledby`,null),tt(r.color?`mat-`+r.color:`mat-accent`),G(`_mat-animation-noopable`,r._animationsDisabled)(`mdc-checkbox--disabled`,r.disabled)(`mat-mdc-checkbox-disabled`,r.disabled)(`mat-mdc-checkbox-checked`,r.checked)(`mat-mdc-checkbox-disabled-interactive`,r.disabledInteractive))},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],ariaDescribedby:[0,`aria-describedby`,`ariaDescribedby`],ariaExpanded:[2,`aria-expanded`,`ariaExpanded`,z],ariaControls:[0,`aria-controls`,`ariaControls`],ariaOwns:[0,`aria-owns`,`ariaOwns`],id:`id`,required:[2,`required`,`required`,z],labelPosition:`labelPosition`,name:`name`,value:`value`,disableRipple:[2,`disableRipple`,`disableRipple`,z],tabIndex:[2,`tabIndex`,`tabIndex`,a=>a==null?void 0:ct(a)],color:`color`,disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z],checked:[2,`checked`,`checked`,z],disabled:[2,`disabled`,`disabled`,z],indeterminate:[2,`indeterminate`,`indeterminate`,z]},outputs:{change:`change`,indeterminateChange:`indeterminateChange`},exportAs:[`matCheckbox`],features:[Ge([{provide:Yr,useExisting:Ht(()=>t),multi:!0},{provide:Kr,useExisting:t,multi:!0}]),_t],ngContentSelectors:[`*`],decls:15,vars:23,consts:[[`checkbox`,``],[`input`,``],[`label`,``],[`mat-internal-form-field`,``,3,`click`,`labelPosition`,`for`],[1,`mdc-checkbox`],[`aria-hidden`,`true`,1,`mat-mdc-checkbox-touch-target`],[`type`,`checkbox`,1,`mdc-checkbox__native-control`,3,`blur`,`click`,`change`,`checked`,`indeterminate`,`disabled`,`id`,`required`,`tabIndex`],[`aria-hidden`,`true`,1,`mdc-checkbox__ripple`],[`aria-hidden`,`true`,1,`mdc-checkbox__background`],[`focusable`,`false`,`viewBox`,`0 0 24 24`,1,`mdc-checkbox__checkmark`],[`fill`,`none`,`d`,`M1.73,12.91 8.1,19.28 22.79,4.59`,1,`mdc-checkbox__checkmark-path`],[1,`mdc-checkbox__mixedmark`],[`mat-ripple`,``,`aria-hidden`,`true`,1,`mat-mdc-checkbox-ripple`,`mat-focus-indicator`,3,`matRippleTrigger`,`matRippleDisabled`,`matRippleCentered`],[1,`mat-internal-form-field-label`,`mdc-label`]],template:function(o,r){if(o&1&&(Pe(),c(0,`label`,3),C(`click`,function(l){return r._preventBubblingFromLabel(l)}),c(1,`span`,4,0),U(3,`span`,5),c(4,`input`,6,1),C(`blur`,function(){return r._onBlur()})(`click`,function(){return r._onInputClick()})(`change`,function(l){return r._onInteractionEvent(l)}),d(),U(6,`span`,7),c(7,`span`,8),Gt(),c(8,`svg`,9),U(9,`path`,10),d(),_a(),U(10,`span`,11),d(),U(11,`span`,12),d(),c(12,`span`,13,2),he(14),d()()),o&2){let s=ft(2);S(`labelPosition`,r.labelPosition)(`for`,r.inputId),m(4),G(`mdc-checkbox--selected`,r.checked),S(`checked`,r.checked)(`indeterminate`,r.indeterminate)(`disabled`,r.disabled&&!r.disabledInteractive)(`id`,r.inputId)(`required`,r.required)(`tabIndex`,r.disabled&&!r.disabledInteractive?-1:r.tabIndex),ne(`aria-label`,r.ariaLabel||null)(`aria-labelledby`,r.ariaLabelledby)(`aria-describedby`,r.ariaDescribedby)(`aria-checked`,r.indeterminate?`mixed`:null)(`aria-controls`,r.ariaControls)(`aria-disabled`,r.disabled&&r.disabledInteractive?!0:null)(`aria-expanded`,r.ariaExpanded)(`aria-owns`,r.ariaOwns)(`name`,r.name)(`value`,r.value),m(7),S(`matRippleTrigger`,s)(`matRippleDisabled`,r.disableRipple||r.disabled)(`matRippleCentered`,!0)}},dependencies:[li,nw],styles:[`.mdc-checkbox {
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
`],encapsulation:2})})()}return t})();var am=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[$l,we]})}return t})();var di=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[Mu,Ti,we]})}return t})();var AR=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`ng-component`]],hostAttrs:[`cdk-text-field-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`textarea.cdk-textarea-autosize {
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
`],encapsulation:2})}return t})();var RR={passive:!0};var aw=(()=>{class t{_platform=f(Re);_ngZone=f(Z);_renderer=f(Nt).createRenderer(null,null);_styleLoader=f(Je);_monitoredElements=new Map;monitor(e){if(!this._platform.isBrowser)return Oi;this._styleLoader.load(AR);let i=un(e),a=this._monitoredElements.get(i);if(a)return a.subject;let o=new V,r=`cdk-text-field-autofilled`,s=h=>{h.animationName===`cdk-text-field-autofill-start`&&!i.classList.contains(r)?(i.classList.add(r),this._ngZone.run(()=>o.next({target:h.target,isAutofilled:!0}))):h.animationName===`cdk-text-field-autofill-end`&&i.classList.contains(r)&&(i.classList.remove(r),this._ngZone.run(()=>o.next({target:h.target,isAutofilled:!1})))},l=this._ngZone.runOutsideAngular(()=>(i.classList.add(`cdk-text-field-autofill-monitored`),this._renderer.listen(i,`animationstart`,s,RR)));return this._monitoredElements.set(i,{subject:o,unlisten:l}),o}stopMonitoring(e){let i=un(e),a=this._monitoredElements.get(i);a&&(a.unlisten(),a.subject.complete(),i.classList.remove(`cdk-text-field-autofill-monitored`),i.classList.remove(`cdk-text-field-autofilled`),this._monitoredElements.delete(i))}ngOnDestroy(){this._monitoredElements.forEach((e,i)=>this.stopMonitoring(i))}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var ow=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({})}return t})();var rw=new N(`MAT_INPUT_VALUE_ACCESSOR`);var OR=[`button`,`checkbox`,`file`,`hidden`,`image`,`radio`,`range`,`reset`,`submit`];var PR=new N(`MAT_INPUT_CONFIG`);var Zr=(()=>{class t{_elementRef=f(Y);_platform=f(Re);ngControl=f(Mi,{optional:!0,self:!0});_autofillMonitor=f(aw);_ngZone=f(Z);_formField=f(Hl,{optional:!0});_renderer=f(Oe);_uid=f(Be).getId(`mat-input-`);_previousNativeValue;_inputValueAccessor;_signalBasedValueAccessor;_previousPlaceholder=null;_errorStateTracker;_config=f(PR,{optional:!0});_cleanupIosKeyup;_cleanupWebkitWheel;_isServer=!1;_isNativeSelect=!1;_isTextarea=!1;_isInFormField=!1;focused=!1;stateChanges=new V;controlType=`mat-input`;autofilled=!1;get disabled(){return this._disabled}set disabled(e){this._disabled=ta(e),this.focused&&(this.focused=!1,this.stateChanges.next())}_disabled=!1;get id(){return this._id}set id(e){this._id=e||this._uid}_id;placeholder;name;get required(){return this._required??this.ngControl?.control?.hasValidator(Po.required)??!1}set required(e){this._required=ta(e)}_required;get type(){return this._type}set type(e){this._type=e||`text`,this._validateType(),!this._isTextarea&&Ug().has(this._type)&&(this._elementRef.nativeElement.type=this._type)}_type=`text`;get errorStateMatcher(){return this._errorStateTracker.matcher}set errorStateMatcher(e){this._errorStateTracker.matcher=e}userAriaDescribedBy;get value(){return this._signalBasedValueAccessor?this._signalBasedValueAccessor.value():this._inputValueAccessor.value}set value(e){e!==this.value&&(this._signalBasedValueAccessor?this._signalBasedValueAccessor.value.set(e):this._inputValueAccessor.value=e,this.stateChanges.next())}get readonly(){return this._readonly}set readonly(e){this._readonly=ta(e)}_readonly=!1;disabledInteractive;get errorState(){return this._errorStateTracker.errorState}set errorState(e){this._errorStateTracker.errorState=e}_neverEmptyInputTypes=[`date`,`datetime`,`datetime-local`,`month`,`time`,`week`].filter(e=>Ug().has(e));constructor(){let e=f(Vl,{optional:!0}),i=f(Bl,{optional:!0}),a=f(Qr),o=f(rw,{optional:!0,self:!0}),r=f(Ju,{optional:!0,self:!0}),s=this._elementRef.nativeElement,l=s.nodeName.toLowerCase();o?ln(o.value)?this._signalBasedValueAccessor=o:this._inputValueAccessor=o:this._inputValueAccessor=s,this._previousNativeValue=this.value,this.id=this.id,this._platform.IOS&&this._ngZone.runOutsideAngular(()=>{this._cleanupIosKeyup=this._renderer.listen(s,`keyup`,this._iOSKeyupListener)}),this._errorStateTracker=new Xr(a,r||this.ngControl,i,e,this.stateChanges),this._isServer=!this._platform.isBrowser,this._isNativeSelect=l===`select`,this._isTextarea=l===`textarea`,this._isInFormField=!!this._formField,this.disabledInteractive=this._config?.disabledInteractive||!1,this._isNativeSelect&&(this.controlType=s.multiple?`mat-native-select-multiple`:`mat-native-select`),this._signalBasedValueAccessor&&vt(()=>{this._signalBasedValueAccessor.value(),this.stateChanges.next()})}ngAfterViewInit(){this._platform.isBrowser&&this._autofillMonitor.monitor(this._elementRef.nativeElement).subscribe(e=>{this.autofilled=e.isAutofilled,this.stateChanges.next()})}ngOnChanges(){this.stateChanges.next()}ngOnDestroy(){this.stateChanges.complete(),this._platform.isBrowser&&this._autofillMonitor.stopMonitoring(this._elementRef.nativeElement),this._cleanupIosKeyup?.(),this._cleanupWebkitWheel?.()}ngDoCheck(){this.ngControl&&(this.updateErrorState(),this.ngControl.disabled!==null&&this.ngControl.disabled!==this.disabled&&(this.disabled=this.ngControl.disabled,this.stateChanges.next())),this._dirtyCheckNativeValue(),this._dirtyCheckPlaceholder()}focus(e){this._elementRef.nativeElement.focus(e)}updateErrorState(){this._errorStateTracker.updateErrorState()}_focusChanged(e){if(e!==this.focused){if(!this._isNativeSelect&&e&&this.disabled&&this.disabledInteractive){let i=this._elementRef.nativeElement;i.type===`number`?(i.type=`text`,i.setSelectionRange(0,0),i.type=`number`):i.setSelectionRange(0,0)}this.focused=e,this.stateChanges.next()}}_onInput(){}_dirtyCheckNativeValue(){let e=this._elementRef.nativeElement.value;this._previousNativeValue!==e&&(this._previousNativeValue=e,this.stateChanges.next())}_dirtyCheckPlaceholder(){let e=this._getPlaceholder();if(e!==this._previousPlaceholder){let i=this._elementRef.nativeElement;this._previousPlaceholder=e,e?i.setAttribute(`placeholder`,e):i.removeAttribute(`placeholder`)}}_getPlaceholder(){return this.placeholder||null}_validateType(){OR.indexOf(this._type)}_isNeverEmpty(){return this._neverEmptyInputTypes.indexOf(this._type)>-1}_isBadInput(){let e=this._elementRef.nativeElement.validity;return e&&e.badInput}get empty(){return!this._isNeverEmpty()&&!this._elementRef.nativeElement.value&&!this._isBadInput()&&!this.autofilled}get shouldLabelFloat(){if(this._isNativeSelect){let e=this._elementRef.nativeElement,i=e.options[0];return this.focused||e.multiple||!this.empty||!!(e.selectedIndex>-1&&i&&i.label)}else return this.focused&&!this.disabled||!this.empty}get describedByIds(){return this._elementRef.nativeElement.getAttribute(`aria-describedby`)?.split(` `)||[]}setDescribedByIds(e){let i=this._elementRef.nativeElement;e.length?i.setAttribute(`aria-describedby`,e.join(` `)):i.removeAttribute(`aria-describedby`)}onContainerClick(){this.focused||this.focus()}_isInlineSelect(){let e=this._elementRef.nativeElement;return this._isNativeSelect&&(e.multiple||e.size>1)}_iOSKeyupListener=e=>{let i=e.target;!i.value&&i.selectionStart===0&&i.selectionEnd===0&&(i.setSelectionRange(1,1),i.setSelectionRange(0,0))};_getReadonlyAttribute(){return this._isNativeSelect?null:this.readonly||this.disabled&&this.disabledInteractive?`true`:null}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`input`,`matInput`,``],[`textarea`,`matInput`,``],[`select`,`matNativeControl`,``],[`input`,`matNativeControl`,``],[`textarea`,`matNativeControl`,``]],hostAttrs:[1,`mat-mdc-input-element`],hostVars:21,hostBindings:function(i,a){i&1&&C(`focus`,function(){return a._focusChanged(!0)})(`blur`,function(){return a._focusChanged(!1)})(`input`,function(){return a._onInput()}),i&2&&(qt(`id`,a.id)(`disabled`,a.disabled&&!a.disabledInteractive)(`required`,a.required),ne(`name`,a.name||null)(`readonly`,a._getReadonlyAttribute())(`aria-disabled`,a.disabled&&a.disabledInteractive?`true`:null)(`aria-invalid`,a.empty&&a.required?null:a.errorState)(`aria-required`,a.required)(`id`,a.id),G(`mat-input-server`,a._isServer)(`mat-mdc-form-field-textarea-control`,a._isInFormField&&a._isTextarea)(`mat-mdc-form-field-input-control`,a._isInFormField)(`mat-mdc-input-disabled-interactive`,a.disabledInteractive)(`mdc-text-field__input`,a._isInFormField)(`mat-mdc-native-select-inline`,a._isInlineSelect()))},inputs:{disabled:`disabled`,id:`id`,placeholder:`placeholder`,name:`name`,required:`required`,type:`type`,errorStateMatcher:`errorStateMatcher`,userAriaDescribedBy:[0,`aria-describedby`,`userAriaDescribedBy`],value:`value`,readonly:`readonly`,disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z]},exportAs:[`matInput`],features:[Ge([{provide:zl,useExisting:t}]),_t]})}return t})();var Jr=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[di,di,ow,we]})}return t})();var Gl=class{_attachedHost=null;attach(n){return this._attachedHost=n,n.attach(this)}detach(){let n=this._attachedHost;n!=null&&(this._attachedHost=null,n.detach())}get isAttached(){return this._attachedHost!=null}setAttachedHost(n){this._attachedHost=n}};var Na=class extends Gl{component;viewContainerRef;injector;projectableNodes;bindings;directives;constructor(n,e,i,a,o,r){super(),this.component=n,this.viewContainerRef=e,this.injector=i,this.projectableNodes=a,this.bindings=o||null,this.directives=r||null}};var Hn=class extends Gl{templateRef;viewContainerRef;context;injector;constructor(n,e,i,a){super(),this.templateRef=n,this.viewContainerRef=e,this.context=i,this.injector=a}get origin(){return this.templateRef.elementRef}attach(n,e=this.context){return this.context=e,super.attach(n)}detach(){return this.context=void 0,super.detach()}};var pb=class extends Gl{element;constructor(n){super(),this.element=n instanceof Y?n.nativeElement:n}};var es=class{_attachedPortal=null;_disposeFn=null;_isDisposed=!1;hasAttached(){return!!this._attachedPortal}attach(n){if(n instanceof Na)return this._attachedPortal=n,this.attachComponentPortal(n);if(n instanceof Hn)return this._attachedPortal=n,this.attachTemplatePortal(n);if(this.attachDomPortal&&n instanceof pb)return this._attachedPortal=n,this.attachDomPortal(n)}attachDomPortal=null;detach(){this._attachedPortal&&(this._attachedPortal.setAttachedHost(null),this._attachedPortal=null),this._invokeDisposeFn()}dispose(){this.hasAttached()&&this.detach(),this._invokeDisposeFn(),this._isDisposed=!0}setDisposeFn(n){this._disposeFn=n}_invokeDisposeFn(){this._disposeFn&&(this._disposeFn(),this._disposeFn=null)}};var Wl=class extends es{outletElement;_appRef;_defaultInjector;constructor(n,e,i){super(),this.outletElement=n,this._appRef=e,this._defaultInjector=i}attachComponentPortal(n){let e;if(n.viewContainerRef){let i=n.injector||n.viewContainerRef.injector,a=i.get(ni,null,{optional:!0})||void 0;e=n.viewContainerRef.createComponent(n.component,{index:n.viewContainerRef.length,injector:i,ngModuleRef:a,projectableNodes:n.projectableNodes||void 0,bindings:n.bindings||void 0,directives:n.directives||void 0}),this.setDisposeFn(()=>e.destroy())}else{let i=this._appRef,a=n.injector||this._defaultInjector||se.NULL,o=a.get(Mt,i.injector);e=au(n.component,{elementInjector:a,environmentInjector:o,projectableNodes:n.projectableNodes||void 0,bindings:n.bindings||void 0,directives:n.directives||void 0}),i.attachView(e.hostView),this.setDisposeFn(()=>{i.viewCount>0&&i.detachView(e.hostView),e.destroy()})}return this.outletElement.appendChild(this._getComponentRootNode(e)),this._attachedPortal=n,e}attachTemplatePortal(n){let e=n.viewContainerRef,i=e.createEmbeddedView(n.templateRef,n.context,{injector:n.injector});return i.rootNodes.forEach(a=>this.outletElement.appendChild(a)),i.detectChanges(),this.setDisposeFn(()=>{let a=e.indexOf(i);a!==-1&&e.remove(a)}),this._attachedPortal=n,i}attachDomPortal=n=>{let e=n.element;e.parentNode;let i=this.outletElement.ownerDocument.createComment(`dom-portal`);e.parentNode.insertBefore(i,e),this.outletElement.appendChild(e),this._attachedPortal=n,super.setDisposeFn(()=>{i.parentNode&&i.parentNode.replaceChild(e,i)})};dispose(){super.dispose(),this.outletElement.remove()}_getComponentRootNode(n){return n.hostView.rootNodes[0]}};var sw=(()=>{class t extends Hn{constructor(){let e=f(Vt),i=f(Bt);super(e,i)}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdkPortal`,``]],exportAs:[`cdkPortal`],features:[Ve]})}return t})();var Aa=(()=>{class t extends es{_moduleRef=f(ni,{optional:!0});_document=f(de);_viewContainerRef=f(Bt);_isInitialized=!1;_attachedRef=null;get portal(){return this._attachedPortal}set portal(e){this.hasAttached()&&!e&&!this._isInitialized||(this.hasAttached()&&super.detach(),e&&super.attach(e),this._attachedPortal=e||null)}attached=new te;get attachedRef(){return this._attachedRef}ngOnInit(){this._isInitialized=!0}ngOnDestroy(){super.dispose(),this._attachedRef=this._attachedPortal=null}attachComponentPortal(e){e.setAttachedHost(this);let i=e.viewContainerRef!=null?e.viewContainerRef:this._viewContainerRef,a=i.createComponent(e.component,{index:i.length,injector:e.injector||i.injector,projectableNodes:e.projectableNodes||void 0,ngModuleRef:this._moduleRef||void 0,bindings:e.bindings||void 0,directives:e.directives||void 0});return i!==this._viewContainerRef&&this._getRootNode().appendChild(a.hostView.rootNodes[0]),super.setDisposeFn(()=>a.destroy()),this._attachedPortal=e,this._attachedRef=a,this.attached.emit(a),a}attachTemplatePortal(e){e.setAttachedHost(this);let i=this._viewContainerRef.createEmbeddedView(e.templateRef,e.context,{injector:e.injector});return super.setDisposeFn(()=>this._viewContainerRef.clear()),this._attachedPortal=e,this._attachedRef=i,this.attached.emit(i),i}attachDomPortal=e=>{let i=e.element;i.parentNode;let a=this._document.createComment(`dom-portal`);e.setAttachedHost(this),i.parentNode.insertBefore(a,i),this._getRootNode().appendChild(i),this._attachedPortal=e,super.setDisposeFn(()=>{a.parentNode&&a.parentNode.replaceChild(i,a)})};_getRootNode(){let e=this._viewContainerRef.element.nativeElement;return e.nodeType===e.ELEMENT_NODE?e:e.parentNode}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`cdkPortalOutlet`,``]],inputs:{portal:[0,`cdkPortalOutlet`,`portal`]},outputs:{attached:`attached`},exportAs:[`cdkPortalOutlet`],features:[Ve]})}return t})();var Lo=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({})}return t})();var FR=20;var Ra=(()=>{class t{_ngZone=f(Z);_platform=f(Re);_renderer=f(Nt).createRenderer(null,null);_cleanupGlobalListener;_scrolled=new V;_scrolledCount=0;scrollContainers=new Map;register(e){this.scrollContainers.has(e)||this.scrollContainers.set(e,e.elementScrolled().subscribe(()=>this._scrolled.next(e)))}deregister(e){let i=this.scrollContainers.get(e);i&&(i.unsubscribe(),this.scrollContainers.delete(e))}scrolled(e=FR){return this._platform.isBrowser?new Ce(i=>{this._cleanupGlobalListener||(this._cleanupGlobalListener=this._ngZone.runOutsideAngular(()=>this._renderer.listen(`document`,`scroll`,()=>this._scrolled.next())));let a=e>0?this._scrolled.pipe(Vc(e)).subscribe(i):this._scrolled.subscribe(i);return this._scrolledCount++,()=>{a.unsubscribe(),this._scrolledCount--,this._scrolledCount||(this._cleanupGlobalListener?.(),this._cleanupGlobalListener=void 0)}}):Mn()}ngOnDestroy(){this._cleanupGlobalListener?.(),this._cleanupGlobalListener=void 0,this.scrollContainers.forEach((e,i)=>this.deregister(i)),this._scrolled.complete()}ancestorScrolled(e,i){let a=this.getAncestorScrollContainers(e);return this.scrolled(i).pipe(Qe(o=>!o||a.indexOf(o)>-1))}getAncestorScrollContainers(e){let i=[];return this.scrollContainers.forEach((a,o)=>{this._targetContainsElement(o,e)&&i.push(o)}),i}_targetContainsElement(e,i){let a=un(i),o=e.getElementRef().nativeElement;do if(a==o)return!0;while(a=a.parentElement);return!1}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var ql=(()=>{class t{elementRef=f(Y);scrollDispatcher=f(Ra);ngZone=f(Z);dir=f(gt,{optional:!0});_scrollElement=this.elementRef.nativeElement;_destroyed=new V;_renderer=f(Oe);_cleanupScroll;_elementScrolled=new V;ngOnInit(){this._cleanupScroll=this.ngZone.runOutsideAngular(()=>this._renderer.listen(this._scrollElement,`scroll`,e=>this._elementScrolled.next(e))),this.scrollDispatcher.register(this)}ngOnDestroy(){this._cleanupScroll?.(),this._elementScrolled.complete(),this.scrollDispatcher.deregister(this),this._destroyed.next(),this._destroyed.complete()}elementScrolled(){return this._elementScrolled}getElementRef(){return this.elementRef}scrollTo(e){let i=this.elementRef.nativeElement,a=this.dir&&this.dir.value==`rtl`;e.left??=a?e.end:e.start,e.right??=a?e.start:e.end,e.bottom!=null&&(e.top=i.scrollHeight-i.clientHeight-e.bottom),a&&zr()!=si.NORMAL?(e.left!=null&&(e.right=i.scrollWidth-i.clientWidth-e.left),zr()==si.INVERTED?e.left=e.right:zr()==si.NEGATED&&(e.left=e.right?-e.right:e.right)):e.right!=null&&(e.left=i.scrollWidth-i.clientWidth-e.right),this._applyScrollToOptions(e)}_applyScrollToOptions(e){let i=this.elementRef.nativeElement;Fu()?i.scrollTo(e):(e.top!=null&&(i.scrollTop=e.top),e.left!=null&&(i.scrollLeft=e.left))}measureScrollOffset(e){let i=`left`,a=`right`,o=this.elementRef.nativeElement;if(e==`top`)return o.scrollTop;if(e==`bottom`)return o.scrollHeight-o.clientHeight-o.scrollTop;let r=this.dir&&this.dir.value==`rtl`;return e==`start`?e=r?a:i:e==`end`&&(e=r?i:a),r&&zr()==si.INVERTED?e==i?o.scrollWidth-o.clientWidth-o.scrollLeft:o.scrollLeft:r&&zr()==si.NEGATED?e==i?o.scrollLeft+o.scrollWidth-o.clientWidth:-o.scrollLeft:e==i?o.scrollLeft:o.scrollWidth-o.clientWidth-o.scrollLeft}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdk-scrollable`,``],[``,`cdkScrollable`,``]]})}return t})();var LR=20;var ui=(()=>{class t{_platform=f(Re);_listeners;_viewportSize=null;_change=new V;_document=f(de);constructor(){let e=f(Z),i=f(Nt).createRenderer(null,null);e.runOutsideAngular(()=>{if(this._platform.isBrowser){let a=o=>this._change.next(o);this._listeners=[i.listen(`window`,`resize`,a),i.listen(`window`,`orientationchange`,a)]}this.change().subscribe(()=>this._viewportSize=null)})}ngOnDestroy(){this._listeners?.forEach(e=>e()),this._change.complete()}getViewportSize(){this._viewportSize||this._updateViewportSize();let e={width:this._viewportSize.width,height:this._viewportSize.height};return this._platform.isBrowser||(this._viewportSize=null),e}getViewportRect(){let e=this.getViewportScrollPosition(),{width:i,height:a}=this.getViewportSize();return{top:e.top,left:e.left,bottom:e.top+a,right:e.left+i,height:a,width:i}}getViewportScrollPosition(){if(!this._platform.isBrowser)return{top:0,left:0};let e=this._document,i=this._getWindow(),a=e.documentElement,o=a.getBoundingClientRect();return{top:-o.top||e.body?.scrollTop||i.scrollY||a.scrollTop||0,left:-o.left||e.body?.scrollLeft||i.scrollX||a.scrollLeft||0}}change(e=LR){return e>0?this._change.pipe(Vc(e)):this._change}_getWindow(){return this._document.defaultView||window}_updateViewportSize(){let e=this._getWindow();this._viewportSize=this._platform.isBrowser?{width:e.innerWidth,height:e.innerHeight}:{width:0,height:0}}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var ia=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({})}return t})();var hb=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[we,ia,we,ia]})}return t})();var lw=Fu();function is(t){return new om(t.get(ui),t.get(de))}var om=class{_viewportRuler;_previousHTMLStyles={top:``,left:``};_previousScrollPosition;_isEnabled=!1;_document;constructor(n,e){this._viewportRuler=n,this._document=e}attach(){}enable(){if(this._canBeEnabled()){let n=this._document.documentElement;this._previousScrollPosition=this._viewportRuler.getViewportScrollPosition(),this._previousHTMLStyles.left=n.style.left||``,this._previousHTMLStyles.top=n.style.top||``,n.style.left=It(-this._previousScrollPosition.left),n.style.top=It(-this._previousScrollPosition.top),n.classList.add(`cdk-global-scrollblock`),this._isEnabled=!0}}disable(){if(this._isEnabled){let n=this._document.documentElement,e=this._document.body,i=n.style,a=e.style,o=i.scrollBehavior||``,r=a.scrollBehavior||``;this._isEnabled=!1,i.left=this._previousHTMLStyles.left,i.top=this._previousHTMLStyles.top,n.classList.remove(`cdk-global-scrollblock`),lw&&(i.scrollBehavior=a.scrollBehavior=`auto`),window.scroll(this._previousScrollPosition.left,this._previousScrollPosition.top),lw&&(i.scrollBehavior=o,a.scrollBehavior=r)}}_canBeEnabled(){if(this._document.documentElement.classList.contains(`cdk-global-scrollblock`)||this._isEnabled)return!1;let e=this._document.documentElement,i=this._viewportRuler.getViewportSize();return e.scrollHeight>i.height||e.scrollWidth>i.width}};function fw(t,n){return new rm(t.get(Ra),t.get(Z),t.get(ui),n)}var rm=class{_scrollDispatcher;_ngZone;_viewportRuler;_config;_scrollSubscription=null;_overlayRef;_initialScrollPosition;constructor(n,e,i,a){this._scrollDispatcher=n,this._ngZone=e,this._viewportRuler=i,this._config=a}attach(n){this._overlayRef,this._overlayRef=n}enable(){if(this._scrollSubscription)return;let n=this._scrollDispatcher.scrolled(0).pipe(Qe(e=>!e||!this._overlayRef.overlayElement.contains(e.getElementRef().nativeElement)));this._config&&this._config.threshold&&this._config.threshold>1?(this._initialScrollPosition=this._viewportRuler.getViewportScrollPosition().top,this._scrollSubscription=n.subscribe(()=>{let e=this._viewportRuler.getViewportScrollPosition().top;Math.abs(e-this._initialScrollPosition)>this._config.threshold?this._detach():this._overlayRef.updatePosition()})):this._scrollSubscription=n.subscribe(this._detach)}disable(){this._scrollSubscription&&(this._scrollSubscription.unsubscribe(),this._scrollSubscription=null)}detach(){this.disable(),this._overlayRef=null}_detach=()=>{this.disable(),this._overlayRef.hasAttached()&&this._ngZone.run(()=>this._overlayRef.detach())}};var Yl=class{enable(){}disable(){}attach(){}};function gb(t,n){return n.some(e=>{let i=t.bottom<e.top,a=t.top>e.bottom,o=t.right<e.left,r=t.left>e.right;return i||a||o||r})}function cw(t,n){return n.some(e=>{let i=t.top<e.top,a=t.bottom>e.bottom,o=t.left<e.left,r=t.right>e.right;return i||a||o||r})}function ra(t,n){return new sm(t.get(Ra),t.get(ui),t.get(Z),n)}var sm=class{_scrollDispatcher;_viewportRuler;_ngZone;_config;_scrollSubscription=null;_overlayRef;constructor(n,e,i,a){this._scrollDispatcher=n,this._viewportRuler=e,this._ngZone=i,this._config=a}attach(n){this._overlayRef,this._overlayRef=n}enable(){if(!this._scrollSubscription){let n=this._config?this._config.scrollThrottle:0;this._scrollSubscription=this._scrollDispatcher.scrolled(n).subscribe(()=>{if(this._overlayRef.updatePosition(),this._config&&this._config.autoClose){let e=this._overlayRef.overlayElement.getBoundingClientRect(),{width:i,height:a}=this._viewportRuler.getViewportSize();gb(e,[{width:i,height:a,bottom:a,right:i,top:0,left:0}])&&(this.disable(),this._ngZone.run(()=>this._overlayRef.detach()))}})}}disable(){this._scrollSubscription&&(this._scrollSubscription.unsubscribe(),this._scrollSubscription=null)}detach(){this.disable(),this._overlayRef=null}};var gw=(()=>{class t{_injector=f(se);noop=()=>new Yl;close=e=>fw(this._injector,e);block=()=>is(this._injector);reposition=e=>ra(this._injector,e);static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var aa=class{positionStrategy;scrollStrategy=new Yl;panelClass=``;hasBackdrop=!1;backdropClass=`cdk-overlay-dark-backdrop`;disableAnimations;width;height;minWidth;minHeight;maxWidth;maxHeight;direction;disposeOnNavigation=!1;usePopover;eventPredicate;constructor(n){if(n){let e=Object.keys(n);for(let i of e)n[i]!==void 0&&(this[i]=n[i])}}};var lm=class{connectionPair;scrollableViewProperties;constructor(n,e){this.connectionPair=n,this.scrollableViewProperties=e}};var bw=(()=>{class t{_attachedOverlays=[];_document=f(de);_isAttached=!1;ngOnDestroy(){this.detach()}add(e){this.remove(e),this._attachedOverlays.push(e)}remove(e){let i=this._attachedOverlays.indexOf(e);i>-1&&this._attachedOverlays.splice(i,1),this._attachedOverlays.length===0&&this.detach()}canReceiveEvent(e,i,a){return a.observers.length<1?!1:e.eventPredicate?e.eventPredicate(i):!0}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var vw=(()=>{class t extends bw{_ngZone=f(Z);_renderer=f(Nt).createRenderer(null,null);_cleanupKeydown;add(e){super.add(e),this._isAttached||(this._ngZone.runOutsideAngular(()=>{this._cleanupKeydown=this._renderer.listen(`body`,`keydown`,this._keydownListener)}),this._isAttached=!0)}detach(){this._isAttached&&(this._cleanupKeydown?.(),this._isAttached=!1)}_keydownListener=e=>{let i=this._attachedOverlays;for(let a=i.length-1;a>-1;a--){let o=i[a];if(this.canReceiveEvent(o,e,o._keydownEvents)){this._ngZone.run(()=>o._keydownEvents.next(e));break}}};static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var _w=(()=>{class t extends bw{_platform=f(Re);_ngZone=f(Z);_renderer=f(Nt).createRenderer(null,null);_cursorOriginalValue;_cursorStyleIsSet=!1;_pointerDownEventTarget=null;_cleanups;add(e){if(super.add(e),!this._isAttached){let i=this._document.body,a={capture:!0},o=this._renderer;this._cleanups=this._ngZone.runOutsideAngular(()=>[o.listen(i,`pointerdown`,this._pointerDownListener,a),o.listen(i,`click`,this._clickListener,a),o.listen(i,`auxclick`,this._clickListener,a),o.listen(i,`contextmenu`,this._clickListener,a)]),this._platform.IOS&&!this._cursorStyleIsSet&&(this._cursorOriginalValue=i.style.cursor,i.style.cursor=`pointer`,this._cursorStyleIsSet=!0),this._isAttached=!0}}detach(){this._isAttached&&(this._cleanups?.forEach(e=>e()),this._cleanups=void 0,this._platform.IOS&&this._cursorStyleIsSet&&(this._document.body.style.cursor=this._cursorOriginalValue,this._cursorStyleIsSet=!1),this._isAttached=!1)}_pointerDownListener=e=>{this._pointerDownEventTarget=Yt(e)};_clickListener=e=>{let i=Yt(e),a=e.type===`click`&&this._pointerDownEventTarget?this._pointerDownEventTarget:i;this._pointerDownEventTarget=null;let o=this._attachedOverlays.slice();for(let r=o.length-1;r>-1;r--){let s=o[r],l=s._outsidePointerEvents;if(!(!s.hasAttached()||!this.canReceiveEvent(s,e,l))){if(dw(s.overlayElement,i)||dw(s.overlayElement,a))break;this._ngZone?this._ngZone.run(()=>l.next(e)):l.next(e)}}};static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function dw(t,n){let e=typeof ShadowRoot<`u`&&ShadowRoot,i=n;for(;i;){if(i===t)return!0;i=e&&i instanceof ShadowRoot?i.host:i.parentNode}return!1}var yw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`ng-component`]],hostAttrs:[`cdk-overlay-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`.cdk-overlay-container, .cdk-global-overlay-wrapper {
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
`],encapsulation:2})}return t})();var um=(()=>{class t{_platform=f(Re);_containerElement;_document=f(de);_styleLoader=f(Je);ngOnDestroy(){this._containerElement?.remove()}getContainerElement(){return this._loadStyles(),this._containerElement||this._createContainer(),this._containerElement}_createContainer(){let e=`cdk-overlay-container`;if(this._platform.isBrowser||Hg()){let a=this._document.querySelectorAll(`.${e}[platform="server"], .${e}[platform="test"]`);for(let o=0;o<a.length;o++)a[o].remove()}let i=this._document.createElement(`div`);i.classList.add(e),Hg()?i.setAttribute(`platform`,`test`):this._platform.isBrowser||i.setAttribute(`platform`,`server`),this._document.body.appendChild(i),this._containerElement=i}_loadStyles(){this._styleLoader.load(yw)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var bb=class{_renderer;_ngZone;element;_cleanupClick;_cleanupTransitionEnd;_fallbackTimeout;constructor(n,e,i,a){this._renderer=e,this._ngZone=i,this.element=n.createElement(`div`),this.element.classList.add(`cdk-overlay-backdrop`),this._cleanupClick=e.listen(this.element,`click`,a)}detach(){this._ngZone.runOutsideAngular(()=>{let n=this.element;clearTimeout(this._fallbackTimeout),this._cleanupTransitionEnd?.(),this._cleanupTransitionEnd=this._renderer.listen(n,`transitionend`,this.dispose),this._fallbackTimeout=setTimeout(this.dispose,500),n.style.pointerEvents=`none`,n.classList.remove(`cdk-overlay-backdrop-showing`)})}dispose=()=>{clearTimeout(this._fallbackTimeout),this._cleanupClick?.(),this._cleanupTransitionEnd?.(),this._cleanupClick=this._cleanupTransitionEnd=this._fallbackTimeout=void 0,this.element.remove()}};function vb(t){return t&&t.nodeType===1}var fb=k([]);var ts=class{_portalOutlet;_host;_pane;_config;_ngZone;_keyboardDispatcher;_document;_location;_outsideClickDispatcher;_animationsDisabled;_injector;_renderer;_backdropClick=new V;_attachments=new V;_detachments=new V;_positionStrategy;_scrollStrategy;_locationChanges;_backdropRef=null;_detachContentMutationObserver;_detachContentAfterRenderRef;_disposed=!1;_previousHostParent;_keydownEvents=new V;_outsidePointerEvents=new V;_afterNextRenderRef;constructor(n,e,i,a,o,r,s,l,h,p=!1,g,_){this._portalOutlet=n,this._host=e,this._pane=i,this._config=a,this._ngZone=o,this._keyboardDispatcher=r,this._document=s,this._location=l,this._outsideClickDispatcher=h,this._animationsDisabled=p,this._injector=g,this._renderer=_,a.scrollStrategy&&(this._scrollStrategy=a.scrollStrategy,this._scrollStrategy.attach(this)),this._positionStrategy=a.positionStrategy}get overlayElement(){return this._pane}get backdropElement(){return this._backdropRef?.element||null}get hostElement(){return this._host}get eventPredicate(){return this._config?.eventPredicate||null}attach(n){if(this._disposed)return null;this._attachHost();let e=this._portalOutlet.attach(n);if(this._positionStrategy?.attach(this),this._updateStackingOrder(),this._updateElementSize(),this._updateElementDirection(),it(()=>{fb.update(i=>i.includes(this)?i:[...i,this])}),this._scrollStrategy&&this._scrollStrategy.enable(),this._afterNextRenderRef?.destroy(),this._afterNextRenderRef=Rt(()=>{this.hasAttached()&&this.updatePosition()},{injector:this._injector}),this._togglePointerEvents(!0),this._config.hasBackdrop&&this._attachBackdrop(),this._config.panelClass&&this._toggleClasses(this._pane,this._config.panelClass,!0),this._attachments.next(),this._completeDetachContent(),this._keyboardDispatcher.add(this),this._config.disposeOnNavigation===!0||this._config.disposeOnNavigation===`pop-state`){let i=this._location.subscribe(()=>this.dispose());this._locationChanges=()=>i.unsubscribe()}else this._config.disposeOnNavigation===`url-change`&&(this._locationChanges=this._location.onUrlChange(()=>this.dispose()));return this._outsideClickDispatcher.add(this),typeof e?.onDestroy==`function`&&e.onDestroy(()=>{this.hasAttached()&&this._ngZone.runOutsideAngular(()=>Promise.resolve().then(()=>this.detach()))}),e}detach(){if(!this.hasAttached())return;this.detachBackdrop(),this._togglePointerEvents(!1),this._positionStrategy&&this._positionStrategy.detach&&this._positionStrategy.detach(),this._scrollStrategy&&this._scrollStrategy.disable();let n=this._portalOutlet.detach();return this._detachments.next(),this._completeDetachContent(),this._keyboardDispatcher.remove(this),this._detachContentWhenEmpty(),this._locationChanges?.(),this._outsideClickDispatcher.remove(this),it(()=>{fb.update(e=>e.filter(i=>i!==this))}),n}dispose(){if(this._disposed)return;let n=this.hasAttached();this._positionStrategy&&this._positionStrategy.dispose(),this._disposeScrollStrategy(),this._backdropRef?.dispose(),this._locationChanges?.(),this._keyboardDispatcher.remove(this),this._portalOutlet.dispose(),this._attachments.complete(),this._backdropClick.complete(),this._keydownEvents.complete(),this._outsidePointerEvents.complete(),this._outsideClickDispatcher.remove(this),this._host?.remove(),this._afterNextRenderRef?.destroy(),this._previousHostParent=this._pane=this._host=this._backdropRef=null,n&&this._detachments.next(),this._detachments.complete(),this._completeDetachContent(),this._disposed=!0,it(()=>{fb.update(e=>e.filter(i=>i!==this))})}hasAttached(){return this._portalOutlet.hasAttached()}backdropClick(){return this._backdropClick}attachments(){return this._attachments}detachments(){return this._detachments}keydownEvents(){return this._keydownEvents}outsidePointerEvents(){return this._outsidePointerEvents}getConfig(){return this._config}updatePosition(){this._positionStrategy&&this._positionStrategy.apply()}updatePositionStrategy(n){n!==this._positionStrategy&&(this._positionStrategy&&this._positionStrategy.dispose(),this._positionStrategy=n,this.hasAttached()&&(n.attach(this),this.updatePosition()))}updateSize(n){this._config=r(r({},this._config),n),this._updateElementSize()}setDirection(n){this._config=s(r({},this._config),{direction:n}),this._updateElementDirection()}addPanelClass(n){this._pane&&this._toggleClasses(this._pane,n,!0)}removePanelClass(n){this._pane&&this._toggleClasses(this._pane,n,!1)}getDirection(){let n=this._config.direction;return n?typeof n==`string`?n:n.value:`ltr`}updateScrollStrategy(n){n!==this._scrollStrategy&&(this._disposeScrollStrategy(),this._scrollStrategy=n,this.hasAttached()&&(n.attach(this),n.enable()))}_updateElementDirection(){this._host.setAttribute(`dir`,this.getDirection())}_updateElementSize(){if(!this._pane)return;let n=this._pane.style;n.width=It(this._config.width),n.height=It(this._config.height),n.minWidth=It(this._config.minWidth),n.minHeight=It(this._config.minHeight),n.maxWidth=It(this._config.maxWidth),n.maxHeight=It(this._config.maxHeight)}_togglePointerEvents(n){this._pane.style.pointerEvents=n?``:`none`}_attachHost(){if(!this._host.parentElement){let n=this._config.usePopover?this._positionStrategy?.getPopoverInsertionPoint?.():null;vb(n)?n.after(this._host):n?.type===`parent`?n.element.appendChild(this._host):this._previousHostParent?.appendChild(this._host)}if(this._config.usePopover)try{this._host.showPopover()}catch{}}_attachBackdrop(){let n=`cdk-overlay-backdrop-showing`;this._backdropRef?.dispose(),this._backdropRef=new bb(this._document,this._renderer,this._ngZone,e=>{this._backdropClick.next(e)}),this._animationsDisabled&&this._backdropRef.element.classList.add(`cdk-overlay-backdrop-noop-animation`),this._config.backdropClass&&this._toggleClasses(this._backdropRef.element,this._config.backdropClass,!0),this._config.usePopover?this._host.prepend(this._backdropRef.element):this._host.parentElement.insertBefore(this._backdropRef.element,this._host),!this._animationsDisabled&&typeof requestAnimationFrame<`u`?this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>this._backdropRef?.element.classList.add(n))}):this._backdropRef.element.classList.add(n)}_updateStackingOrder(){!this._config.usePopover&&this._host.nextSibling&&this._host.parentNode.appendChild(this._host)}detachBackdrop(){this._animationsDisabled?(this._backdropRef?.dispose(),this._backdropRef=null):this._backdropRef?.detach()}_toggleClasses(n,e,i){let a=Lr(e||[]).filter(o=>!!o);a.length&&(i?n.classList.add(...a):n.classList.remove(...a))}_detachContentWhenEmpty(){let n=!1;try{this._detachContentAfterRenderRef=Rt(()=>{n=!0,this._detachContent()},{injector:this._injector})}catch(e){if(n)throw e;this._detachContent()}globalThis.MutationObserver&&this._pane&&(this._detachContentMutationObserver||=new globalThis.MutationObserver(()=>{this._detachContent()}),this._detachContentMutationObserver.observe(this._pane,{childList:!0}))}_detachContent(){(!this._pane||!this._host||this._pane.children.length===0)&&(this._pane&&this._config.panelClass&&this._toggleClasses(this._pane,this._config.panelClass,!1),this._host&&this._host.parentElement&&(this._previousHostParent=this._host.parentElement,this._host.remove()),this._completeDetachContent())}_completeDetachContent(){this._detachContentAfterRenderRef?.destroy(),this._detachContentAfterRenderRef=void 0,this._detachContentMutationObserver?.disconnect()}_disposeScrollStrategy(){let n=this._scrollStrategy;n?.disable(),n?.detach?.()}};var uw=`cdk-overlay-connected-position-bounding-box`;var BR=/([A-Za-z%]+)$/;function Bo(t,n){return new cm(n,t.get(ui),t.get(de),t.get(Re),t.get(um))}var cm=class{_viewportRuler;_document;_platform;_overlayContainer;_overlayRef;_isInitialRender=!1;_lastBoundingBoxSize={width:0,height:0};_isPushed=!1;_canPush=!0;_growAfterOpen=!1;_hasFlexibleDimensions=!0;_positionLocked=!1;_originRect;_overlayRect;_viewportRect;_containerRect;_viewportMargin=0;_scrollables=[];_preferredPositions=[];_origin;_pane;_isDisposed=!1;_boundingBox=null;_lastPosition=null;_lastScrollVisibility=null;_positionChanges=new V;_resizeSubscription=De.EMPTY;_offsetX=0;_offsetY=0;_transformOriginSelector;_appliedPanelClasses=[];_previousPushAmount=null;_popoverLocation=`global`;positionChanges=this._positionChanges;get positions(){return this._preferredPositions}constructor(n,e,i,a,o){this._viewportRuler=e,this._document=i,this._platform=a,this._overlayContainer=o,this.setOrigin(n)}attach(n){this._overlayRef&&this._overlayRef,this._validatePositions(),n.hostElement.classList.add(uw),this._overlayRef=n,this._boundingBox=n.hostElement,this._pane=n.overlayElement,this._isDisposed=!1,this._isInitialRender=!0,this._lastPosition=null,this._resizeSubscription.unsubscribe(),this._resizeSubscription=this._viewportRuler.change().subscribe(()=>{this._isInitialRender=!0,this.apply()})}apply(){if(this._isDisposed||!this._platform.isBrowser)return;if(!this._isInitialRender&&this._positionLocked&&this._lastPosition){this.reapplyLastPosition();return}this._clearPanelClasses(),this._resetOverlayElementStyles(),this._resetBoundingBoxStyles(),this._viewportRect=this._getNarrowedViewportRect(),this._originRect=this._getOriginRect(),this._overlayRect=this._pane.getBoundingClientRect(),this._containerRect=this._getContainerRect();let n=this._originRect,e=this._overlayRect,i=this._viewportRect,a=this._containerRect,o=[],r;for(let s of this._preferredPositions){let l=this._getOriginPoint(n,a,s),h=this._getOverlayPoint(l,e,s),p=this._getOverlayFit(h,e,i,s);if(p.isCompletelyWithinViewport){this._isPushed=!1,this._applyPosition(s,l);return}if(this._canFitWithFlexibleDimensions(p,h,i)){o.push({position:s,origin:l,overlayRect:e,boundingBoxRect:this._calculateBoundingBoxRect(l,s)});continue}(!r||r.overlayFit.visibleArea<p.visibleArea)&&(r={overlayFit:p,overlayPoint:h,originPoint:l,position:s,overlayRect:e})}if(o.length){let s=null,l=-1;for(let h of o){let p=h.boundingBoxRect.width*h.boundingBoxRect.height*(h.position.weight||1);p>l&&(l=p,s=h)}this._isPushed=!1,this._applyPosition(s.position,s.origin);return}if(this._canPush){this._isPushed=!0,this._applyPosition(r.position,r.originPoint);return}this._applyPosition(r.position,r.originPoint)}detach(){this._clearPanelClasses(),this._lastPosition=null,this._previousPushAmount=null,this._resizeSubscription.unsubscribe()}dispose(){this._isDisposed||(this._boundingBox&&Vo(this._boundingBox.style,{top:``,left:``,right:``,bottom:``,height:``,width:``,alignItems:``,justifyContent:``}),this._pane&&this._resetOverlayElementStyles(),this._overlayRef&&this._overlayRef.hostElement.classList.remove(uw),this.detach(),this._positionChanges.complete(),this._overlayRef=this._boundingBox=null,this._isDisposed=!0)}reapplyLastPosition(){if(this._isDisposed||!this._platform.isBrowser)return;let n=this._lastPosition;n?(this._originRect=this._getOriginRect(),this._overlayRect=this._pane.getBoundingClientRect(),this._viewportRect=this._getNarrowedViewportRect(),this._containerRect=this._getContainerRect(),this._applyPosition(n,this._getOriginPoint(this._originRect,this._containerRect,n))):this.apply()}withScrollableContainers(n){return this._scrollables=n,this}withPositions(n){return this._preferredPositions=n,n.indexOf(this._lastPosition)===-1&&(this._lastPosition=null),this._validatePositions(),this}withViewportMargin(n){return this._viewportMargin=n,this}withFlexibleDimensions(n=!0){return this._hasFlexibleDimensions=n,this}withGrowAfterOpen(n=!0){return this._growAfterOpen=n,this}withPush(n=!0){return this._canPush=n,this}withLockedPosition(n=!0){return this._positionLocked=n,this}setOrigin(n){return this._origin=n,this}withDefaultOffsetX(n){return this._offsetX=n,this}withDefaultOffsetY(n){return this._offsetY=n,this}withTransformOriginOn(n){return this._transformOriginSelector=n,this}withPopoverLocation(n){return this._popoverLocation=n,this}getPopoverInsertionPoint(){return this._popoverLocation===`global`?null:this._popoverLocation!==`inline`?this._popoverLocation:this._origin instanceof Y?this._origin.nativeElement:vb(this._origin)?this._origin:null}_getOriginPoint(n,e,i){let a;if(i.originX==`center`)a=n.left+n.width/2;else{let r=this._isRtl()?n.right:n.left,s=this._isRtl()?n.left:n.right;a=i.originX==`start`?r:s}e.left<0&&(a-=e.left);let o;return i.originY==`center`?o=n.top+n.height/2:o=i.originY==`top`?n.top:n.bottom,e.top<0&&(o-=e.top),{x:a,y:o}}_getOverlayPoint(n,e,i){let a;i.overlayX==`center`?a=-e.width/2:i.overlayX===`start`?a=this._isRtl()?-e.width:0:a=this._isRtl()?0:-e.width;let o;return i.overlayY==`center`?o=-e.height/2:o=i.overlayY==`top`?0:-e.height,{x:n.x+a,y:n.y+o}}_getOverlayFit(n,e,i,a){let o=pw(e),{x:r,y:s}=n,l=this._getOffset(a,`x`),h=this._getOffset(a,`y`);l&&(r+=l),h&&(s+=h);let p=0-r,g=r+o.width-i.width,_=0-s,b=s+o.height-i.height,y=this._subtractOverflows(o.width,p,g),I=this._subtractOverflows(o.height,_,b),P=y*I;return{visibleArea:P,isCompletelyWithinViewport:o.width*o.height===P,fitsInViewportVertically:I===o.height,fitsInViewportHorizontally:y==o.width}}_canFitWithFlexibleDimensions(n,e,i){if(this._hasFlexibleDimensions){let a=i.bottom-e.y,o=i.right-e.x,r=mw(this._overlayRef.getConfig().minHeight),s=mw(this._overlayRef.getConfig().minWidth),l=n.fitsInViewportVertically||r!=null&&r<=a,h=n.fitsInViewportHorizontally||s!=null&&s<=o;return l&&h}return!1}_pushOverlayOnScreen(n,e,i){if(this._previousPushAmount&&this._positionLocked)return{x:n.x+this._previousPushAmount.x,y:n.y+this._previousPushAmount.y};let a=pw(e),o=this._viewportRect,r=Math.max(n.x+a.width-o.width,0),s=Math.max(n.y+a.height-o.height,0),l=Math.max(o.top-i.top-n.y,0),h=Math.max(o.left-i.left-n.x,0),p=0,g=0;return a.width<=o.width?p=h||-r:p=n.x<this._getViewportMarginStart()?o.left-i.left-n.x:0,a.height<=o.height?g=l||-s:g=n.y<this._getViewportMarginTop()?o.top-i.top-n.y:0,this._previousPushAmount={x:p,y:g},{x:n.x+p,y:n.y+g}}_applyPosition(n,e){if(this._setTransformOrigin(n),this._setOverlayElementStyles(e,n),this._setBoundingBoxStyles(e,n),n.panelClass&&this._addPanelClasses(n.panelClass),this._positionChanges.observers.length){let i=this._getScrollVisibility();if(n!==this._lastPosition||!this._lastScrollVisibility||!jR(this._lastScrollVisibility,i)){let a=new lm(n,i);this._positionChanges.next(a)}this._lastScrollVisibility=i}this._lastPosition=n,this._isInitialRender=!1}_setTransformOrigin(n){if(!this._transformOriginSelector)return;let e=this._boundingBox.querySelectorAll(this._transformOriginSelector),i,a=n.overlayY;n.overlayX===`center`?i=`center`:this._isRtl()?i=n.overlayX===`start`?`right`:`left`:i=n.overlayX===`start`?`left`:`right`;for(let o=0;o<e.length;o++)e[o].style.transformOrigin=`${i} ${a}`}_calculateBoundingBoxRect(n,e){let i=this._viewportRect,a=this._isRtl(),o,r,s;if(e.overlayY===`top`)r=n.y,o=i.height-r+this._getViewportMarginBottom();else if(e.overlayY===`bottom`)s=i.height-n.y+this._getViewportMarginTop()+this._getViewportMarginBottom(),o=i.height-s+this._getViewportMarginTop();else{let b=Math.min(i.bottom-n.y+i.top,n.y),y=this._lastBoundingBoxSize.height;o=b*2,r=n.y-b,o>y&&!this._isInitialRender&&!this._growAfterOpen&&(r=n.y-y/2)}let l=e.overlayX===`start`&&!a||e.overlayX===`end`&&a,h=e.overlayX===`end`&&!a||e.overlayX===`start`&&a,p,g,_;if(h)_=i.width-n.x+this._getViewportMarginStart()+this._getViewportMarginEnd(),p=n.x-this._getViewportMarginStart();else if(l)g=n.x,p=i.right-n.x-this._getViewportMarginEnd();else{let b=Math.min(i.right-n.x+i.left,n.x),y=this._lastBoundingBoxSize.width;p=b*2,g=n.x-b,p>y&&!this._isInitialRender&&!this._growAfterOpen&&(g=n.x-y/2)}return{top:r,left:g,bottom:s,right:_,width:p,height:o}}_setBoundingBoxStyles(n,e){let i=this._calculateBoundingBoxRect(n,e);!this._isInitialRender&&!this._growAfterOpen&&(i.height=Math.min(i.height,this._lastBoundingBoxSize.height),i.width=Math.min(i.width,this._lastBoundingBoxSize.width));let a={};if(this._hasExactPosition())a.top=a.left=`0`,a.bottom=a.right=`auto`,a.maxHeight=a.maxWidth=``,a.width=a.height=`100%`;else{let o=this._overlayRef.getConfig().maxHeight,r=this._overlayRef.getConfig().maxWidth;a.width=It(i.width),a.height=It(i.height),a.top=It(i.top)||`auto`,a.bottom=It(i.bottom)||`auto`,a.left=It(i.left)||`auto`,a.right=It(i.right)||`auto`,e.overlayX===`center`?a.alignItems=`center`:a.alignItems=e.overlayX===`end`?`flex-end`:`flex-start`,e.overlayY===`center`?a.justifyContent=`center`:a.justifyContent=e.overlayY===`bottom`?`flex-end`:`flex-start`,o&&(a.maxHeight=It(o)),r&&(a.maxWidth=It(r))}this._lastBoundingBoxSize=i,Vo(this._boundingBox.style,a)}_resetBoundingBoxStyles(){Vo(this._boundingBox.style,{top:`0`,left:`0`,right:`0`,bottom:`0`,height:``,width:``,alignItems:``,justifyContent:``})}_resetOverlayElementStyles(){Vo(this._pane.style,{top:``,left:``,bottom:``,right:``,position:``,transform:``})}_setOverlayElementStyles(n,e){let i={},a=this._hasExactPosition(),o=this._hasFlexibleDimensions,r=this._overlayRef.getConfig();if(a){let p=this._viewportRuler.getViewportScrollPosition();Vo(i,this._getExactOverlayY(e,n,p)),Vo(i,this._getExactOverlayX(e,n,p))}else i.position=`static`;let s=``,l=this._getOffset(e,`x`),h=this._getOffset(e,`y`);l&&(s+=`translateX(${l}px) `),h&&(s+=`translateY(${h}px)`),i.transform=s.trim(),r.maxHeight&&(a?i.maxHeight=It(r.maxHeight):o&&(i.maxHeight=``)),r.maxWidth&&(a?i.maxWidth=It(r.maxWidth):o&&(i.maxWidth=``)),Vo(this._pane.style,i)}_getExactOverlayY(n,e,i){let a={top:``,bottom:``},o=this._getOverlayPoint(e,this._overlayRect,n);if(this._isPushed&&(o=this._pushOverlayOnScreen(o,this._overlayRect,i)),n.overlayY===`bottom`)a.bottom=`${this._document.documentElement.clientHeight-(o.y+this._overlayRect.height)}px`;else a.top=It(o.y);return a}_getExactOverlayX(n,e,i){let a={left:``,right:``},o=this._getOverlayPoint(e,this._overlayRect,n);this._isPushed&&(o=this._pushOverlayOnScreen(o,this._overlayRect,i));let r;if(this._isRtl()?r=n.overlayX===`end`?`left`:`right`:r=n.overlayX===`end`?`right`:`left`,r===`right`)a.right=`${this._document.documentElement.clientWidth-(o.x+this._overlayRect.width)}px`;else a.left=It(o.x);return a}_getScrollVisibility(){let n=this._getOriginRect(),e=this._pane.getBoundingClientRect(),i=this._scrollables.map(a=>a.getElementRef().nativeElement.getBoundingClientRect());return{isOriginClipped:cw(n,i),isOriginOutsideView:gb(n,i),isOverlayClipped:cw(e,i),isOverlayOutsideView:gb(e,i)}}_subtractOverflows(n,...e){return e.reduce((i,a)=>i-Math.max(a,0),n)}_getNarrowedViewportRect(){let n=this._document.documentElement.clientWidth,e=this._document.documentElement.clientHeight,i=this._viewportRuler.getViewportScrollPosition();return{top:i.top+this._getViewportMarginTop(),left:i.left+this._getViewportMarginStart(),right:i.left+n-this._getViewportMarginEnd(),bottom:i.top+e-this._getViewportMarginBottom(),width:n-this._getViewportMarginStart()-this._getViewportMarginEnd(),height:e-this._getViewportMarginTop()-this._getViewportMarginBottom()}}_isRtl(){return this._overlayRef.getDirection()===`rtl`}_hasExactPosition(){return!this._hasFlexibleDimensions||this._isPushed}_getOffset(n,e){return e===`x`?n.offsetX==null?this._offsetX:n.offsetX:n.offsetY==null?this._offsetY:n.offsetY}_validatePositions(){}_addPanelClasses(n){this._pane&&Lr(n).forEach(e=>{e!==``&&this._appliedPanelClasses.indexOf(e)===-1&&(this._appliedPanelClasses.push(e),this._pane.classList.add(e))})}_clearPanelClasses(){this._pane&&(this._appliedPanelClasses.forEach(n=>{this._pane.classList.remove(n)}),this._appliedPanelClasses=[])}_getViewportMarginStart(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.start??0}_getViewportMarginEnd(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.end??0}_getViewportMarginTop(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.top??0}_getViewportMarginBottom(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.bottom??0}_getOriginRect(){let n=this._origin;if(n instanceof Y)return n.nativeElement.getBoundingClientRect();if(n instanceof Element)return n.getBoundingClientRect();let e=n.width||0,i=n.height||0;return{top:n.y,bottom:n.y+i,left:n.x,right:n.x+e,height:i,width:e}}_getContainerRect(){let n=this._overlayRef.getConfig().usePopover&&this._popoverLocation!==`global`,e=this._overlayContainer.getContainerElement();n&&(e.style.display=`block`);let i=e.getBoundingClientRect();return n&&(e.style.display=``),i}};function Vo(t,n){for(let e in n)Object.hasOwn(n,e)&&(t[e]=n[e]);return t}function mw(t){if(typeof t!=`number`&&t!=null){let[n,e]=t.split(BR);return!e||e===`px`?parseFloat(n):null}return t||null}function pw(t){return{top:Math.floor(t.top),right:Math.floor(t.right),bottom:Math.floor(t.bottom),left:Math.floor(t.left),width:Math.floor(t.width),height:Math.floor(t.height)}}function jR(t,n){return t===n?!0:t.isOriginClipped===n.isOriginClipped&&t.isOriginOutsideView===n.isOriginOutsideView&&t.isOverlayClipped===n.isOverlayClipped&&t.isOverlayOutsideView===n.isOverlayOutsideView}var hw=`cdk-global-overlay-wrapper`;function as(t){return new dm}var dm=class{_overlayRef;_cssPosition=`static`;_topOffset=``;_bottomOffset=``;_alignItems=``;_xPosition=``;_xOffset=``;_width=``;_height=``;_isDisposed=!1;attach(n){let e=n.getConfig();this._overlayRef=n,this._width&&!e.width&&n.updateSize({width:this._width}),this._height&&!e.height&&n.updateSize({height:this._height}),n.hostElement.classList.add(hw),this._isDisposed=!1}top(n=``){return this._bottomOffset=``,this._topOffset=n,this._alignItems=`flex-start`,this}left(n=``){return this._xOffset=n,this._xPosition=`left`,this}bottom(n=``){return this._topOffset=``,this._bottomOffset=n,this._alignItems=`flex-end`,this}right(n=``){return this._xOffset=n,this._xPosition=`right`,this}start(n=``){return this._xOffset=n,this._xPosition=`start`,this}end(n=``){return this._xOffset=n,this._xPosition=`end`,this}width(n=``){return this._overlayRef?this._overlayRef.updateSize({width:n}):this._width=n,this}height(n=``){return this._overlayRef?this._overlayRef.updateSize({height:n}):this._height=n,this}centerHorizontally(n=``){return this.left(n),this._xPosition=`center`,this}centerVertically(n=``){return this.top(n),this._alignItems=`center`,this}apply(){if(!this._overlayRef||!this._overlayRef.hasAttached())return;let n=this._overlayRef.overlayElement.style,e=this._overlayRef.hostElement.style,{width:a,height:o,maxWidth:r,maxHeight:s}=this._overlayRef.getConfig(),l=(a===`100%`||a===`100vw`)&&(!r||r===`100%`||r===`100vw`),h=(o===`100%`||o===`100vh`)&&(!s||s===`100%`||s===`100vh`),p=this._xPosition,g=this._xOffset,_=this._overlayRef.getConfig().direction===`rtl`,b=``,y=``,I=``;l?I=`flex-start`:p===`center`?(I=`center`,_?y=g:b=g):_?p===`left`||p===`end`?(I=`flex-end`,b=g):(p===`right`||p===`start`)&&(I=`flex-start`,y=g):p===`left`||p===`start`?(I=`flex-start`,b=g):(p===`right`||p===`end`)&&(I=`flex-end`,y=g),n.position=this._cssPosition,n.marginLeft=l?`0`:b,n.marginTop=h?`0`:this._topOffset,n.marginBottom=this._bottomOffset,n.marginRight=l?`0`:y,e.justifyContent=I,e.alignItems=h?`flex-start`:this._alignItems}dispose(){if(this._isDisposed||!this._overlayRef)return;let n=this._overlayRef.overlayElement.style,e=this._overlayRef.hostElement,i=e.style;e.classList.remove(hw),i.justifyContent=i.alignItems=n.marginTop=n.marginBottom=n.marginLeft=n.marginRight=n.position=``,this._overlayRef=null,this._isDisposed=!0}};var xw=(()=>{class t{_injector=f(se);global(){return as()}flexibleConnectedTo(e){return Bo(this._injector,e)}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Kl=new N(`OVERLAY_DEFAULT_CONFIG`);function sa(t,n){t.get(Je).load(yw);let e=t.get(um),i=t.get(de),a=t.get(Be),o=t.get(On),r=t.get(gt),s=t.get(Oe,null,{optional:!0})||t.get(Nt).createRenderer(null,null),l=new aa(n),h=t.get(Kl,null,{optional:!0})?.usePopover??!0;l.direction=l.direction||r.value,!i.body||!(`showPopover`in i.body)?l.usePopover=!1:l.usePopover=n?.usePopover??h;let p=i.createElement(`div`),g=i.createElement(`div`);p.id=a.getId(`cdk-overlay-`),p.classList.add(`cdk-overlay-pane`),g.appendChild(p),l.usePopover&&(g.setAttribute(`popover`,`manual`),g.classList.add(`cdk-overlay-popover`));let _=l.usePopover?l.positionStrategy?.getPopoverInsertionPoint?.():null;return vb(_)?_.after(g):_?.type===`parent`?_.element.appendChild(g):e.getContainerElement().appendChild(g),new ts(new Wl(p,o,t),g,p,l,t.get(Z),t.get(vw),i,t.get(ru),t.get(_w),n?.disableAnimations??t.get(js,null,{optional:!0})===`NoopAnimations`,t.get(Mt),s)}var Cw=(()=>{class t{scrollStrategies=f(gw);_positionBuilder=f(xw);_injector=f(se);create(e){return sa(this._injector,e)}position(){return this._positionBuilder}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var zR=[{originX:`start`,originY:`bottom`,overlayX:`start`,overlayY:`top`},{originX:`start`,originY:`top`,overlayX:`start`,overlayY:`bottom`},{originX:`end`,originY:`top`,overlayX:`end`,overlayY:`bottom`},{originX:`end`,originY:`bottom`,overlayX:`end`,overlayY:`top`}];var HR=new N(`cdk-connected-overlay-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t)}});var ns=(()=>{class t{elementRef=f(Y);static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdk-overlay-origin`,``],[``,`overlay-origin`,``],[``,`cdkOverlayOrigin`,``]],exportAs:[`cdkOverlayOrigin`]})}return t})();var Sw=new N(`cdk-connected-overlay-default-config`);var mm=(()=>{class t{_dir=f(gt,{optional:!0});_injector=f(se);_overlayRef;_templatePortal;_backdropSubscription=De.EMPTY;_attachSubscription=De.EMPTY;_detachSubscription=De.EMPTY;_positionSubscription=De.EMPTY;_offsetX;_offsetY;_position;_scrollStrategyFactory=f(HR);_ngZone=f(Z);origin;positions;positionStrategy;get offsetX(){return this._offsetX}set offsetX(e){this._offsetX=e,this._position&&this._updatePositionStrategy(this._position)}get offsetY(){return this._offsetY}set offsetY(e){this._offsetY=e,this._position&&this._updatePositionStrategy(this._position)}width;height;minWidth;minHeight;backdropClass;panelClass;viewportMargin=0;scrollStrategy;open=!1;disableClose=!1;transformOriginSelector;hasBackdrop=!1;lockPosition=!1;flexibleDimensions=!1;growAfterOpen=!1;push=!1;disposeOnNavigation=!1;usePopover;matchWidth=!1;set _config(e){typeof e!=`string`&&this._assignConfig(e)}backdropClick=new te;positionChange=new te;attach=new te;detach=new te;overlayKeydown=new te;overlayOutsideClick=new te;constructor(){let e=f(Vt),i=f(Bt),a=f(Sw,{optional:!0}),o=f(Kl,{optional:!0});this.usePopover=o?.usePopover===!1?null:`global`,this._templatePortal=new Hn(e,i),this.scrollStrategy=this._scrollStrategyFactory(),a&&this._assignConfig(a)}get overlayRef(){return this._overlayRef}get dir(){return this._dir?this._dir.value:`ltr`}ngOnDestroy(){this._attachSubscription.unsubscribe(),this._detachSubscription.unsubscribe(),this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this._overlayRef?.dispose()}ngOnChanges(e){this._position&&(this._updatePositionStrategy(this._position),this._overlayRef?.updateSize({width:this._getWidth(),minWidth:this.minWidth,height:this.height,minHeight:this.minHeight}),e.origin&&this.open&&this._position.apply()),e.open&&(this.open?this.attachOverlay():this.detachOverlay())}_createOverlay(){(!this.positions||!this.positions.length)&&(this.positions=zR);let e=this._overlayRef=sa(this._injector,this._buildConfig());this._attachSubscription=e.attachments().subscribe(()=>this.attach.emit()),this._detachSubscription=e.detachments().subscribe(()=>this.detach.emit()),e.keydownEvents().subscribe(i=>{this.overlayKeydown.next(i),i.keyCode===27&&!this.disableClose&&!Ct(i)&&(i.preventDefault(),this.detachOverlay())}),this._overlayRef.outsidePointerEvents().subscribe(i=>{let a=this._getOriginElement(),o=Yt(i);(!a||a!==o&&!a.contains(o))&&this.overlayOutsideClick.next(i)})}_buildConfig(){let e=this._position=this.positionStrategy||this._createPositionStrategy(),i=new aa({direction:this._dir||`ltr`,positionStrategy:e,scrollStrategy:this.scrollStrategy,hasBackdrop:this.hasBackdrop,disposeOnNavigation:this.disposeOnNavigation,usePopover:!!this.usePopover});return(this.height||this.height===0)&&(i.height=this.height),(this.minWidth||this.minWidth===0)&&(i.minWidth=this.minWidth),(this.minHeight||this.minHeight===0)&&(i.minHeight=this.minHeight),this.backdropClass&&(i.backdropClass=this.backdropClass),this.panelClass&&(i.panelClass=this.panelClass),i}_updatePositionStrategy(e){let i=this.positions.map(a=>({originX:a.originX,originY:a.originY,overlayX:a.overlayX,overlayY:a.overlayY,offsetX:a.offsetX||this.offsetX,offsetY:a.offsetY||this.offsetY,panelClass:a.panelClass||void 0}));return e.setOrigin(this._getOrigin()).withPositions(i).withFlexibleDimensions(this.flexibleDimensions).withPush(this.push).withGrowAfterOpen(this.growAfterOpen).withViewportMargin(this.viewportMargin).withLockedPosition(this.lockPosition).withTransformOriginOn(this.transformOriginSelector).withPopoverLocation(this.usePopover===null?`global`:this.usePopover)}_createPositionStrategy(){let e=Bo(this._injector,this._getOrigin());return this._updatePositionStrategy(e),e}_getOrigin(){return this.origin instanceof ns?this.origin.elementRef:this.origin}_getOriginElement(){return this.origin instanceof ns?this.origin.elementRef.nativeElement:this.origin instanceof Y?this.origin.nativeElement:typeof Element<`u`&&this.origin instanceof Element?this.origin:null}_getWidth(){return this.width?this.width:this.matchWidth?this._getOriginElement()?.getBoundingClientRect?.().width:void 0}attachOverlay(){this._overlayRef||this._createOverlay();let e=this._overlayRef;e.getConfig().hasBackdrop=this.hasBackdrop,e.updateSize({width:this._getWidth()}),e.hasAttached()||e.attach(this._templatePortal),this.hasBackdrop?this._backdropSubscription=e.backdropClick().subscribe(i=>this.backdropClick.emit(i)):this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this.positionChange.observers.length>0&&(this._positionSubscription=this._position.positionChanges.pipe(hp(()=>this.positionChange.observers.length>0)).subscribe(i=>{this._ngZone.run(()=>this.positionChange.emit(i)),this.positionChange.observers.length===0&&this._positionSubscription.unsubscribe()})),this.open=!0}detachOverlay(){this._overlayRef?.detach(),this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this.open=!1}_assignConfig(e){this.origin=e.origin??this.origin,this.positions=e.positions??this.positions,this.positionStrategy=e.positionStrategy??this.positionStrategy,this.offsetX=e.offsetX??this.offsetX,this.offsetY=e.offsetY??this.offsetY,this.width=e.width??this.width,this.height=e.height??this.height,this.minWidth=e.minWidth??this.minWidth,this.minHeight=e.minHeight??this.minHeight,this.backdropClass=e.backdropClass??this.backdropClass,this.panelClass=e.panelClass??this.panelClass,this.viewportMargin=e.viewportMargin??this.viewportMargin,this.scrollStrategy=e.scrollStrategy??this.scrollStrategy,this.disableClose=e.disableClose??this.disableClose,this.transformOriginSelector=e.transformOriginSelector??this.transformOriginSelector,this.hasBackdrop=e.hasBackdrop??this.hasBackdrop,this.lockPosition=e.lockPosition??this.lockPosition,this.flexibleDimensions=e.flexibleDimensions??this.flexibleDimensions,this.growAfterOpen=e.growAfterOpen??this.growAfterOpen,this.push=e.push??this.push,this.disposeOnNavigation=e.disposeOnNavigation??this.disposeOnNavigation,this.usePopover=e.usePopover??this.usePopover,this.matchWidth=e.matchWidth??this.matchWidth}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`cdk-connected-overlay`,``],[``,`connected-overlay`,``],[``,`cdkConnectedOverlay`,``]],inputs:{origin:[0,`cdkConnectedOverlayOrigin`,`origin`],positions:[0,`cdkConnectedOverlayPositions`,`positions`],positionStrategy:[0,`cdkConnectedOverlayPositionStrategy`,`positionStrategy`],offsetX:[0,`cdkConnectedOverlayOffsetX`,`offsetX`],offsetY:[0,`cdkConnectedOverlayOffsetY`,`offsetY`],width:[0,`cdkConnectedOverlayWidth`,`width`],height:[0,`cdkConnectedOverlayHeight`,`height`],minWidth:[0,`cdkConnectedOverlayMinWidth`,`minWidth`],minHeight:[0,`cdkConnectedOverlayMinHeight`,`minHeight`],backdropClass:[0,`cdkConnectedOverlayBackdropClass`,`backdropClass`],panelClass:[0,`cdkConnectedOverlayPanelClass`,`panelClass`],viewportMargin:[0,`cdkConnectedOverlayViewportMargin`,`viewportMargin`],scrollStrategy:[0,`cdkConnectedOverlayScrollStrategy`,`scrollStrategy`],open:[0,`cdkConnectedOverlayOpen`,`open`],disableClose:[0,`cdkConnectedOverlayDisableClose`,`disableClose`],transformOriginSelector:[0,`cdkConnectedOverlayTransformOriginOn`,`transformOriginSelector`],hasBackdrop:[2,`cdkConnectedOverlayHasBackdrop`,`hasBackdrop`,z],lockPosition:[2,`cdkConnectedOverlayLockPosition`,`lockPosition`,z],flexibleDimensions:[2,`cdkConnectedOverlayFlexibleDimensions`,`flexibleDimensions`,z],growAfterOpen:[2,`cdkConnectedOverlayGrowAfterOpen`,`growAfterOpen`,z],push:[2,`cdkConnectedOverlayPush`,`push`,z],disposeOnNavigation:[2,`cdkConnectedOverlayDisposeOnNavigation`,`disposeOnNavigation`,e=>e===`url-change`||e===`pop-state`?e:z(e)],usePopover:[0,`cdkConnectedOverlayUsePopover`,`usePopover`],matchWidth:[2,`cdkConnectedOverlayMatchWidth`,`matchWidth`,z],_config:[0,`cdkConnectedOverlay`,`_config`]},outputs:{backdropClick:`backdropClick`,positionChange:`positionChange`,attach:`attach`,detach:`detach`,overlayKeydown:`overlayKeydown`,overlayOutsideClick:`overlayOutsideClick`},exportAs:[`cdkConnectedOverlay`],features:[_t]})}return t})();var mi=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({providers:[Cw],imports:[we,Lo,hb,hb]})}return t})();var yb=new N(`MAT_MENU_PANEL`);var la=(()=>{class t{_isAnchor;_elementRef=f(Y);_document=f(de);_focusMonitor=f(en);_parentMenu=f(yb,{optional:!0});_changeDetectorRef=f(Fe);role=`menuitem`;disabled=!1;disabledInteractive=!1;disableRipple=!1;_hovered=new V;_focused=new V;_highlighted=!1;_triggersSubmenu=!1;constructor(){f(Je).load(mn),this._parentMenu?.addItem?.(this),this._isAnchor=this._elementRef.nativeElement.tagName===`A`}focus(e,i){this._focusMonitor&&e?this._focusMonitor.focusVia(this._getHostElement(),e,i):this._getHostElement().focus(i),this._focused.next(this)}ngAfterViewInit(){this._focusMonitor&&this._focusMonitor.monitor(this._elementRef,!1)}ngOnDestroy(){this._focusMonitor&&this._focusMonitor.stopMonitoring(this._elementRef),this._parentMenu&&this._parentMenu.removeItem&&this._parentMenu.removeItem(this),this._hovered.complete(),this._focused.complete()}_getTabIndex(){return this.disabled&&!this.disabledInteractive?`-1`:`0`}_getAriaDisabled(){return this._isAnchor?this.disabled||null:this.disabled&&this.disabledInteractive?!0:null}_getDisabled(){return this.disabledInteractive||!this.disabled?null:!0}_getHostElement(){return this._elementRef.nativeElement}_checkDisabled(e){this.disabled&&(e.preventDefault(),e.stopPropagation())}_handleMouseEnter(){this._hovered.next(this)}getLabel(){let e=this._elementRef.nativeElement.cloneNode(!0),i=e.querySelectorAll(`mat-icon, .material-icons, .material-symbols-outlined, .material-symbols-rounded, .material-symbols-sharp`);for(let a=0;a<i.length;a++)i[a].remove();return e.textContent?.trim()||``}_setHighlighted(e){this._highlighted=e,this._changeDetectorRef.markForCheck()}_setTriggersSubmenu(e){this._triggersSubmenu=e,this._changeDetectorRef.markForCheck()}_hasFocus(){return this._document&&this._document.activeElement===this._getHostElement()}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[`mat-icon`],[``,`matMenuItemIcon`,``]],`*`],i=[`mat-icon, [matMenuItemIcon]`,`*`];function a(o,r){o&1&&(Gt(),c(0,`svg`,2),U(1,`polygon`,3),d())}return H({type:t,selectors:[[``,`mat-menu-item`,``]],hostAttrs:[1,`mat-mdc-menu-item`,`mat-focus-indicator`],hostVars:12,hostBindings:function(r,s){r&1&&C(`click`,function(h){return s._checkDisabled(h)})(`mouseenter`,function(){return s._handleMouseEnter()}),r&2&&(ne(`role`,s.role)(`tabindex`,s._getTabIndex())(`aria-disabled`,s._getAriaDisabled())(`disabled`,s._getDisabled()),G(`mat-mdc-menu-item-highlighted`,s._highlighted)(`mat-mdc-menu-item-submenu-trigger`,s._triggersSubmenu)(`mat-mdc-menu-item-disabled`,s.disabled)(`mat-mdc-menu-item-disabled-interactive`,s.disabledInteractive))},inputs:{role:`role`,disabled:[2,`disabled`,`disabled`,z],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z],disableRipple:[2,`disableRipple`,`disableRipple`,z]},exportAs:[`matMenuItem`],ngContentSelectors:i,decls:5,vars:3,consts:[[1,`mat-mdc-menu-item-text`],[`matRipple`,``,1,`mat-mdc-menu-ripple`,3,`matRippleDisabled`,`matRippleTrigger`],[`viewBox`,`0 0 5 10`,`focusable`,`false`,`aria-hidden`,`true`,1,`mat-mdc-menu-submenu-icon`],[`points`,`0,0 5,5 0,10`]],template:function(r,s){r&1&&(Pe(e),he(0),c(1,`span`,0),he(2,1),d(),U(3,`div`,1),E(4,a,2,0,`:svg:svg`,2)),r&2&&(m(3),S(`matRippleDisabled`,s.disableRipple||s.disabled)(`matRippleTrigger`,s._getHostElement()),m(),D(s._triggersSubmenu?4:-1))},dependencies:[li],encapsulation:2})})()}return t})();var UR=new N(`MatMenuContent`);var $R=new N(`mat-menu-default-options`,{providedIn:`root`,factory:()=>({overlapTrigger:!1,xPosition:`after`,yPosition:`below`,backdropClass:`cdk-overlay-transparent-backdrop`})});var _b=`_mat-menu-enter`;var pm=`_mat-menu-exit`;var Ni=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Fe);_injector=f(se);_keyManager;_xPosition;_yPosition;_firstItemFocusRef;_exitFallbackTimeout;_animationsDisabled=Ue();_allItems;_directDescendantItems=new wn;_classList={};_panelAnimationState=`void`;_animationDone=new V;_isAnimating=k(!1);parentMenu;direction;overlayPanelClass;backdropClass;ariaLabel;ariaLabelledby;ariaDescribedby;get xPosition(){return this._xPosition}set xPosition(e){this._xPosition=e,this.setPositionClasses()}get yPosition(){return this._yPosition}set yPosition(e){this._yPosition=e,this.setPositionClasses()}templateRef;items;lazyContent;overlapTrigger=!1;hasBackdrop;get panelClass(){return this._previousPanelClass}set panelClass(e){let i=this._previousPanelClass,a=r({},this._classList);i&&i.length&&i.split(` `).forEach(o=>{a[o]=!1}),this._previousPanelClass=e,e&&e.length&&(e.split(` `).forEach(o=>{a[o]=!0}),this._elementRef.nativeElement.className=``),this._classList=a}_previousPanelClass=``;get classList(){return this.panelClass}set classList(e){this.panelClass=e}closed=new te;close=this.closed;panelId=f(Be).getId(`mat-menu-panel-`);constructor(){let e=f($R);this.overlayPanelClass=e.overlayPanelClass||``,this._xPosition=e.xPosition,this._yPosition=e.yPosition,this.backdropClass=e.backdropClass,this.overlapTrigger=e.overlapTrigger,this.hasBackdrop=e.hasBackdrop}ngOnInit(){this.setPositionClasses()}ngAfterContentInit(){this._updateDirectDescendants(),this._keyManager=new ea(this._directDescendantItems).withWrap().withTypeAhead().withHomeAndEnd().skipPredicate(e=>e.disabled&&!e.disabledInteractive),this._keyManager.tabOut.subscribe(()=>this.closed.emit(`tab`)),this._directDescendantItems.changes.pipe(mt(this._directDescendantItems),_n(e=>kt(...e.map(i=>i._focused)))).subscribe(e=>this._keyManager.updateActiveItem(e)),this._directDescendantItems.changes.subscribe(e=>{let i=this._keyManager;if(this._panelAnimationState===`enter`&&i.activeItem?._hasFocus()){let a=e.toArray(),o=Math.max(0,Math.min(a.length-1,i.activeItemIndex||0));a[o]&&!a[o].disabled?i.setActiveItem(o):i.setNextItemActive()}})}ngOnDestroy(){this._keyManager?.destroy(),this._directDescendantItems.destroy(),this.closed.complete(),this._firstItemFocusRef?.destroy(),clearTimeout(this._exitFallbackTimeout)}_hovered(){return this._directDescendantItems.changes.pipe(mt(this._directDescendantItems),_n(i=>kt(...i.map(a=>a._hovered))))}addItem(e){}removeItem(e){}_handleKeydown(e){let i=e.keyCode,a=this._keyManager;switch(i){case 27:Ct(e)||(e.preventDefault(),this.closed.emit(`keydown`));break;case 37:this.parentMenu&&this.direction===`ltr`&&this.closed.emit(`keydown`);break;case 39:this.parentMenu&&this.direction===`rtl`&&this.closed.emit(`keydown`);break;default:(i===38||i===40)&&a.setFocusOrigin(`keyboard`),a.onKeydown(e);return}}focusFirstItem(e=`program`){this._firstItemFocusRef?.destroy(),this._firstItemFocusRef=Rt(()=>{let i=this._resolvePanel();if(!i||!i.contains(document.activeElement)){let a=this._keyManager;a.setFocusOrigin(e).setFirstItemActive(),!a.activeItem&&i&&i.focus()}},{injector:this._injector})}resetActiveItem(){this._keyManager.setActiveItem(-1)}setElevation(e){}setPositionClasses(e=this.xPosition,i=this.yPosition){this._classList=s(r({},this._classList),{"mat-menu-before":e===`before`,"mat-menu-after":e===`after`,"mat-menu-above":i===`above`,"mat-menu-below":i===`below`}),this._changeDetectorRef.markForCheck()}_onAnimationDone(e){let i=e===pm;(i||e===_b)&&(i&&(clearTimeout(this._exitFallbackTimeout),this._exitFallbackTimeout=void 0),this._animationDone.next(i?`void`:`enter`),this._isAnimating.set(!1))}_onAnimationStart(e){(e===_b||e===pm)&&this._isAnimating.set(!0)}_setIsOpen(e){if(this._panelAnimationState=e?`enter`:`void`,e){if(this._keyManager.activeItemIndex===0){let i=this._resolvePanel();i&&(i.scrollTop=0)}}else this._animationsDisabled||(this._exitFallbackTimeout=setTimeout(()=>this._onAnimationDone(pm),200));this._animationsDisabled&&setTimeout(()=>{this._onAnimationDone(e?_b:pm)}),this._changeDetectorRef.markForCheck()}_updateDirectDescendants(){this._allItems.changes.pipe(mt(this._allItems)).subscribe(e=>{this._directDescendantItems.reset(e.filter(i=>i._parentMenu===this)),this._directDescendantItems.notifyOnChanges()})}_resolvePanel(){let e=null;return this._directDescendantItems.length&&(e=this._directDescendantItems.first._getHostElement().closest(`[role="menu"]`)),e}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`];function i(a,o){if(a&1){let r=$();lt(0,`div`,0),Mr(`click`,function(){M(r);return T(v().closed.emit(`click`))})(`animationstart`,function(l){M(r);return T(v()._onAnimationStart(l.animationName))})(`animationend`,function(l){M(r);return T(v()._onAnimationDone(l.animationName))})(`animationcancel`,function(l){M(r);return T(v()._onAnimationDone(l.animationName))}),lt(1,`div`,1),he(2),ht()()}if(a&2){let r=v();tt(r._classList),G(`mat-menu-panel-animations-disabled`,r._animationsDisabled)(`mat-menu-panel-exit-animation`,r._panelAnimationState===`void`)(`mat-menu-panel-animating`,r._isAnimating()),qt(`id`,r.panelId),ne(`aria-label`,r.ariaLabel||null)(`aria-labelledby`,r.ariaLabelledby||null)(`aria-describedby`,r.ariaDescribedby||null)}}return H({type:t,selectors:[[`mat-menu`]],contentQueries:function(o,r,s){if(o&1&&jt(s,UR,5)(s,la,5)(s,la,4),o&2){let l;J(l=ee())&&(r.lazyContent=l.first),J(l=ee())&&(r._allItems=l),J(l=ee())&&(r.items=l)}},viewQuery:function(o,r){if(o&1&&Ze(Vt,5),o&2){let s;J(s=ee())&&(r.templateRef=s.first)}},hostVars:3,hostBindings:function(o,r){o&2&&ne(`aria-label`,null)(`aria-labelledby`,null)(`aria-describedby`,null)},inputs:{backdropClass:`backdropClass`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],ariaDescribedby:[0,`aria-describedby`,`ariaDescribedby`],xPosition:`xPosition`,yPosition:`yPosition`,overlapTrigger:[2,`overlapTrigger`,`overlapTrigger`,z],hasBackdrop:[2,`hasBackdrop`,`hasBackdrop`,a=>a==null?null:z(a)],panelClass:[0,`class`,`panelClass`],classList:`classList`},outputs:{closed:`closed`,close:`close`},exportAs:[`matMenu`],features:[Ge([{provide:yb,useExisting:t}])],ngContentSelectors:e,decls:1,vars:0,consts:[[`tabindex`,`-1`,`role`,`menu`,1,`mat-mdc-menu-panel`,3,`click`,`animationstart`,`animationend`,`animationcancel`,`id`],[1,`mat-mdc-menu-content`]],template:function(o,r){o&1&&(Pe(),Ir(0,i,3,12,`ng-template`))},styles:[`mat-menu {
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
`],encapsulation:2})})()}return t})();var GR=new N(`mat-menu-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t)}});var os=new WeakMap;var WR=(()=>{class t{_canHaveBackdrop;_element=f(Y);_viewContainerRef=f(Bt);_menuItemInstance=f(la,{optional:!0,self:!0});_dir=f(gt,{optional:!0});_focusMonitor=f(en);_ngZone=f(Z);_injector=f(se);_scrollStrategy=f(GR);_changeDetectorRef=f(Fe);_animationsDisabled=Ue();_portal;_overlayRef=null;_menuOpen=!1;_closingActionsSubscription=De.EMPTY;_menuCloseSubscription=De.EMPTY;_pendingRemoval;_parentMaterialMenu;_parentInnerPadding;_openedBy=void 0;get _menu(){return this._menuInternal}set _menu(e){e!==this._menuInternal&&(this._menuInternal=e,this._menuCloseSubscription.unsubscribe(),e?(this._parentMaterialMenu,this._menuCloseSubscription=e.close.subscribe(i=>{this._destroyMenu(i),(i===`click`||i===`tab`)&&this._parentMaterialMenu&&this._parentMaterialMenu.closed.emit(i)})):this._destroyMenu(),this._menuItemInstance?._setTriggersSubmenu(this._triggersSubmenu()))}_menuInternal=null;constructor(e){this._canHaveBackdrop=e;let i=f(yb,{optional:!0});this._parentMaterialMenu=i instanceof Ni?i:void 0}ngOnDestroy(){this._menu&&this._ownsMenu(this._menu)&&os.delete(this._menu),this._pendingRemoval?.unsubscribe(),this._menuCloseSubscription.unsubscribe(),this._closingActionsSubscription.unsubscribe(),this._overlayRef&&(this._overlayRef.dispose(),this._overlayRef=null)}get menuOpen(){return this._menuOpen}get dir(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_triggersSubmenu(){return!!(this._menuItemInstance&&this._parentMaterialMenu&&this._menu)}_closeMenu(){this._menu?.close.emit()}_openMenu(e){if(this._triggerIsAriaDisabled())return;let i=this._menu;if(this._menuOpen||!i)return;this._pendingRemoval?.unsubscribe();let a=os.get(i);os.set(i,this),a&&a!==this&&a._closeMenu();let o=this._createOverlay(i),r=o.getConfig(),s=r.positionStrategy;this._setPosition(i,s),this._canHaveBackdrop?r.hasBackdrop=i.hasBackdrop==null?!this._triggersSubmenu():i.hasBackdrop:r.hasBackdrop=i.hasBackdrop??!1,o.hasAttached()||(o.attach(this._getPortal(i)),i.lazyContent?.attach(this.menuData)),this._closingActionsSubscription=this._menuClosingActions().subscribe(()=>this._closeMenu()),i.parentMenu=this._triggersSubmenu()?this._parentMaterialMenu:void 0,i.direction=this.dir,e&&i.focusFirstItem(this._openedBy||`program`),this._setIsMenuOpen(!0),i instanceof Ni&&(i._setIsOpen(!0),i._directDescendantItems.changes.pipe(Ne(i.close)).subscribe(()=>{s.withLockedPosition(!1).reapplyLastPosition(),s.withLockedPosition(!0)}))}focus(e,i){this._focusMonitor&&e?this._focusMonitor.focusVia(this._element,e,i):this._element.nativeElement.focus(i)}_destroyMenu(e){let i=this._overlayRef,a=this._menu;!i||!this.menuOpen||(this._closingActionsSubscription.unsubscribe(),this._pendingRemoval?.unsubscribe(),a instanceof Ni&&this._ownsMenu(a)?(this._pendingRemoval=a._animationDone.pipe(vn(1)).subscribe(()=>{i.detach(),os.has(a)||a.lazyContent?.detach()}),a._setIsOpen(!1)):(i.detach(),a?.lazyContent?.detach()),a&&this._ownsMenu(a)&&os.delete(a),this.restoreFocus&&(e===`keydown`||!this._openedBy||!this._triggersSubmenu())&&this.focus(this._openedBy),this._openedBy=void 0,this._setIsMenuOpen(!1))}_setIsMenuOpen(e){e!==this._menuOpen&&(this._menuOpen=e,this._menuOpen?this.menuOpened.emit():this.menuClosed.emit(),this._triggersSubmenu()&&this._menuItemInstance._setHighlighted(e),this._changeDetectorRef.markForCheck())}_createOverlay(e){if(!this._overlayRef){let i=this._getOverlayConfig(e);this._subscribeToPositions(e,i.positionStrategy),this._overlayRef=sa(this._injector,i),this._overlayRef.keydownEvents().subscribe(a=>{this._menu instanceof Ni&&this._menu._handleKeydown(a)})}return this._overlayRef}_getOverlayConfig(e){return new aa({positionStrategy:Bo(this._injector,this._getOverlayOrigin()).withLockedPosition().withGrowAfterOpen().withTransformOriginOn(`.mat-menu-panel, .mat-mdc-menu-panel`),backdropClass:e.backdropClass||`cdk-overlay-transparent-backdrop`,panelClass:e.overlayPanelClass,scrollStrategy:this._scrollStrategy(),direction:this._dir||`ltr`,disableAnimations:this._animationsDisabled})}_subscribeToPositions(e,i){e.setPositionClasses&&i.positionChanges.subscribe(a=>{this._ngZone.run(()=>{let o=a.connectionPair.overlayX===`start`?`after`:`before`,r=a.connectionPair.overlayY===`top`?`below`:`above`;e.setPositionClasses(o,r)})})}_setPosition(e,i){let[a,o]=e.xPosition===`before`?[`end`,`start`]:[`start`,`end`],[r,s]=e.yPosition===`above`?[`bottom`,`top`]:[`top`,`bottom`],[l,h]=[r,s],[p,g]=[a,o],_=0;if(this._triggersSubmenu()){if(g=a=e.xPosition===`before`?`start`:`end`,o=p=a===`end`?`start`:`end`,this._parentMaterialMenu){if(this._parentInnerPadding==null){let b=this._parentMaterialMenu.items.first;this._parentInnerPadding=b?b._getHostElement().offsetTop:0}_=r===`bottom`?this._parentInnerPadding:-this._parentInnerPadding}}else e.overlapTrigger||(l=r===`top`?`bottom`:`top`,h=s===`top`?`bottom`:`top`);i.withPositions([{originX:a,originY:l,overlayX:p,overlayY:r,offsetY:_},{originX:o,originY:l,overlayX:g,overlayY:r,offsetY:_},{originX:a,originY:h,overlayX:p,overlayY:s,offsetY:-_},{originX:o,originY:h,overlayX:g,overlayY:s,offsetY:-_}])}_menuClosingActions(){let e=this._getOutsideClickStream(this._overlayRef),i=this._overlayRef.detachments();return kt(e,this._parentMaterialMenu?this._parentMaterialMenu.closed:Mn(),this._parentMaterialMenu?this._parentMaterialMenu._hovered().pipe(Qe(r=>this._menuOpen&&r!==this._menuItemInstance)):Mn(),i)}_getPortal(e){return(!this._portal||this._portal.templateRef!==e.templateRef)&&(this._portal=new Hn(e.templateRef,this._viewContainerRef)),this._portal}_ownsMenu(e){return os.get(e)===this}_triggerIsAriaDisabled(){return z(this._element.nativeElement.getAttribute(`aria-disabled`))}static ɵfac=function(i){Bf()};static ɵdir=Q({type:t})}return t})();var rs=(()=>{class t extends WR{_cleanupTouchstart;_hoverSubscription=De.EMPTY;get _deprecatedMatMenuTriggerFor(){return this.menu}set _deprecatedMatMenuTriggerFor(e){this.menu=e}get menu(){return this._menu}set menu(e){this._menu=e}menuData;restoreFocus=!0;menuOpened=new te;onMenuOpen=this.menuOpened;menuClosed=new te;onMenuClose=this.menuClosed;constructor(){super(!0);let e=f(Oe);this._cleanupTouchstart=e.listen(this._element.nativeElement,`touchstart`,i=>{Io(i)||(this._openedBy=`touch`)},{passive:!0})}triggersSubmenu(){return super._triggersSubmenu()}toggleMenu(){return this.menuOpen?this.closeMenu():this.openMenu()}openMenu(){this._openMenu(!0)}closeMenu(){this._closeMenu()}updatePosition(){this._overlayRef?.updatePosition()}ngAfterContentInit(){this._handleHover()}ngOnDestroy(){super.ngOnDestroy(),this._cleanupTouchstart(),this._hoverSubscription.unsubscribe()}_getOverlayOrigin(){return this._element}_getOutsideClickStream(e){return e.backdropClick()}_handleMousedown(e){Do(e)||(this._openedBy=e.button===0?`mouse`:void 0,this.triggersSubmenu()&&e.preventDefault())}_handleKeydown(e){let i=e.keyCode;(i===13||i===32)&&(this._openedBy=`keyboard`),this.triggersSubmenu()&&(i===39&&this.dir===`ltr`||i===37&&this.dir===`rtl`)&&(this._openedBy=`keyboard`,this.openMenu())}_handleClick(e){this.triggersSubmenu()?(e.stopPropagation(),this.openMenu()):this.toggleMenu()}_handleHover(){this.triggersSubmenu()&&this._parentMaterialMenu&&(this._hoverSubscription=this._parentMaterialMenu._hovered().subscribe(e=>{e===this._menuItemInstance&&!e.disabled&&this._parentMaterialMenu?._panelAnimationState!==`void`&&(this._openedBy=`mouse`,this._openMenu(!1))}))}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`mat-menu-trigger-for`,``],[``,`matMenuTriggerFor`,``]],hostAttrs:[1,`mat-mdc-menu-trigger`],hostVars:3,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._handleClick(r)})(`mousedown`,function(r){return a._handleMousedown(r)})(`keydown`,function(r){return a._handleKeydown(r)}),i&2&&ne(`aria-haspopup`,a.menu?`menu`:null)(`aria-expanded`,a.menuOpen)(`aria-controls`,a.menuOpen?a.menu?.panelId:null)},inputs:{_deprecatedMatMenuTriggerFor:[0,`mat-menu-trigger-for`,`_deprecatedMatMenuTriggerFor`],menu:[0,`matMenuTriggerFor`,`menu`],menuData:[0,`matMenuTriggerData`,`menuData`],restoreFocus:[0,`matMenuTriggerRestoreFocus`,`restoreFocus`]},outputs:{menuOpened:`menuOpened`,onMenuOpen:`onMenuOpen`,menuClosed:`menuClosed`,onMenuClose:`onMenuClose`},exportAs:[`matMenuTrigger`],features:[Ve]})}return t})();var ss=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[ki,mi,we,ia]})}return t})();var jo=class{_multiple;_emitChanges;compareWith;_selection=new Set;_deselectedToEmit=[];_selectedToEmit=[];_selected=null;get selected(){return this._selected||(this._selected=Array.from(this._selection.values())),this._selected}changed=new V;bulk={select:n=>this._select(n),deselect:n=>this._deselect(n),setSelection:n=>this._setSelection(n)};constructor(n=!1,e,i=!0,a){this._multiple=n,this._emitChanges=i,this.compareWith=a,e&&e.length&&(n?e.forEach(o=>this._markSelected(o)):this._markSelected(e[0]),this._selectedToEmit.length=0)}select(...n){return this._select(n)}deselect(...n){return this._deselect(n)}setSelection(...n){return this._setSelection(n)}toggle(n){return this.isSelected(n)?this.deselect(n):this.select(n)}clear(n=!0){this._unmarkAll();let e=this._hasQueuedChanges();return n&&this._emitChangeEvent(),e}isSelected(n){return this._selection.has(this._getConcreteValue(n))}isEmpty(){return this._selection.size===0}hasValue(){return!this.isEmpty()}sort(n){this._multiple&&this.selected&&this._selected.sort(n)}isMultipleSelection(){return this._multiple}_select(n){this._verifyValueAssignment(n),n.forEach(i=>this._markSelected(i));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}_deselect(n){this._verifyValueAssignment(n),n.forEach(i=>this._unmarkSelected(i));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}_setSelection(n){this._verifyValueAssignment(n);let e=this.selected,i=new Set(n.map(o=>this._getConcreteValue(o)));n.forEach(o=>this._markSelected(o)),e.filter(o=>!i.has(this._getConcreteValue(o,i))).forEach(o=>this._unmarkSelected(o));let a=this._hasQueuedChanges();return this._emitChangeEvent(),a}_emitChangeEvent(){this._selected=null,(this._selectedToEmit.length||this._deselectedToEmit.length)&&(this.changed.next({source:this,added:this._selectedToEmit,removed:this._deselectedToEmit}),this._deselectedToEmit=[],this._selectedToEmit=[])}_markSelected(n){n=this._getConcreteValue(n),this.isSelected(n)||(this._multiple||this._unmarkAll(),this.isSelected(n)||this._selection.add(n),this._emitChanges&&this._selectedToEmit.push(n))}_unmarkSelected(n){n=this._getConcreteValue(n),this.isSelected(n)&&(this._selection.delete(n),this._emitChanges&&this._deselectedToEmit.push(n))}_unmarkAll(){this.isEmpty()||this._selection.forEach(n=>this._unmarkSelected(n))}_verifyValueAssignment(n){n.length>1&&this._multiple}_hasQueuedChanges(){return!!(this._deselectedToEmit.length||this._selectedToEmit.length)}_getConcreteValue(n,e){if(this.compareWith){e=e??this._selection;for(let i of e)if(this.compareWith(n,i))return i;return n}else return n}};var hm=(()=>{class t{_animationsDisabled=Ue();state=`unchecked`;disabled=!1;appearance=`full`;static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`mat-pseudo-checkbox`]],hostAttrs:[1,`mat-pseudo-checkbox`],hostVars:12,hostBindings:function(i,a){i&2&&G(`mat-pseudo-checkbox-indeterminate`,a.state===`indeterminate`)(`mat-pseudo-checkbox-checked`,a.state===`checked`)(`mat-pseudo-checkbox-disabled`,a.disabled)(`mat-pseudo-checkbox-minimal`,a.appearance===`minimal`)(`mat-pseudo-checkbox-full`,a.appearance===`full`)(`_mat-animation-noopable`,a._animationsDisabled)},inputs:{state:`state`,disabled:`disabled`,appearance:`appearance`},decls:0,vars:0,template:function(i,a){},styles:[`.mat-pseudo-checkbox {
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
`],encapsulation:2})}return t})();var Sb=new N(`MAT_OPTION_PARENT_COMPONENT`);var wb=new N(`MatOptgroup`);var Cb=class{source;isUserInput;constructor(n,e=!1){this.source=n,this.isUserInput=e}};var Un=(()=>{class t{_element=f(Y);_changeDetectorRef=f(Fe);_parent=f(Sb,{optional:!0});group=f(wb,{optional:!0});_signalDisableRipple=!1;_selected=!1;_active=!1;_mostRecentViewValue=``;get multiple(){return this._parent&&this._parent.multiple}get selected(){return this._selected}value;id=f(Be).getId(`mat-option-`);get disabled(){return this.group&&this.group.disabled||this._disabled()}set disabled(e){this._disabled.set(e)}_disabled=k(!1);get disableRipple(){return this._signalDisableRipple?this._parent.disableRipple():!!this._parent?.disableRipple}get hideSingleSelectionIndicator(){return!!(this._parent&&this._parent.hideSingleSelectionIndicator)}onSelectionChange=new te;_text;_stateChanges=new V;constructor(){let e=f(Je);e.load(mn),e.load(Ii),this._signalDisableRipple=!!this._parent&&ln(this._parent.disableRipple)}get active(){return this._active}get viewValue(){return(this._text?.nativeElement.textContent||``).trim()}select(e=!0){this._selected||(this._selected=!0,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}deselect(e=!0){this._selected&&(this._selected=!1,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}focus(e,i){let a=this._getHostElement();typeof a.focus==`function`&&a.focus(i)}setActiveStyles(){this._active||(this._active=!0,this._changeDetectorRef.markForCheck())}setInactiveStyles(){this._active&&(this._active=!1,this._changeDetectorRef.markForCheck())}getLabel(){return this.viewValue}_handleKeydown(e){(e.keyCode===13||e.keyCode===32)&&!Ct(e)&&(this._selectViaInteraction(),e.preventDefault())}_selectViaInteraction(){this.disabled||(this._selected=this.multiple?!this._selected:!0,this._changeDetectorRef.markForCheck(),this._emitSelectionChangeEvent(!0))}_getTabIndex(){return this.disabled?`-1`:`0`}_getHostElement(){return this._element.nativeElement}ngAfterViewChecked(){if(this._selected){let e=this.viewValue;e!==this._mostRecentViewValue&&(this._mostRecentViewValue&&this._stateChanges.next(),this._mostRecentViewValue=e)}}ngOnDestroy(){this._stateChanges.complete()}_emitSelectionChangeEvent(e=!1){this.onSelectionChange.emit(new Cb(this,e))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`text`],i=[[[`mat-icon`]],`*`],a=[`mat-icon`,`*`];function o(l,h){if(l&1&&U(0,`mat-pseudo-checkbox`,1),l&2){let p=v();S(`disabled`,p.disabled)(`state`,p.selected?`checked`:`unchecked`)}}function r(l,h){if(l&1&&U(0,`mat-pseudo-checkbox`,3),l&2)S(`disabled`,v().disabled)}function s(l,h){if(l&1&&(c(0,`span`,4),u(1),d()),l&2){let p=v();m(),B(`(`,p.group.label,`)`)}}return H({type:t,selectors:[[`mat-option`]],viewQuery:function(h,p){if(h&1&&Ze(e,7),h&2){let g;J(g=ee())&&(p._text=g.first)}},hostAttrs:[`role`,`option`,1,`mat-mdc-option`,`mdc-list-item`],hostVars:11,hostBindings:function(h,p){h&1&&C(`click`,function(){return p._selectViaInteraction()})(`keydown`,function(_){return p._handleKeydown(_)}),h&2&&(qt(`id`,p.id),ne(`aria-selected`,p.selected)(`aria-disabled`,p.disabled.toString()),G(`mdc-list-item--selected`,p.selected)(`mat-mdc-option-multiple`,p.multiple)(`mat-mdc-option-active`,p.active)(`mdc-list-item--disabled`,p.disabled))},inputs:{value:`value`,id:`id`,disabled:[2,`disabled`,`disabled`,z]},outputs:{onSelectionChange:`onSelectionChange`},exportAs:[`matOption`],ngContentSelectors:a,decls:8,vars:5,consts:[[`text`,``],[`aria-hidden`,`true`,1,`mat-mdc-option-pseudo-checkbox`,3,`disabled`,`state`],[1,`mdc-list-item__primary-text`],[`state`,`checked`,`aria-hidden`,`true`,`appearance`,`minimal`,1,`mat-mdc-option-pseudo-checkbox`,3,`disabled`],[1,`cdk-visually-hidden`],[`aria-hidden`,`true`,`mat-ripple`,``,1,`mat-mdc-option-ripple`,`mat-focus-indicator`,3,`matRippleTrigger`,`matRippleDisabled`]],template:function(h,p){h&1&&(Pe(i),E(0,o,1,2,`mat-pseudo-checkbox`,1),he(1),c(2,`span`,2,0),he(4,1),d(),E(5,r,1,1,`mat-pseudo-checkbox`,3),E(6,s,2,1,`span`,4),U(7,`div`,5)),h&2&&(D(p.multiple?0:-1),m(5),D(!p.multiple&&p.selected&&!p.hideSingleSelectionIndicator?5:-1),m(),D(p.group&&p.group._inert?6:-1),m(),S(`matRippleTrigger`,p._getHostElement())(`matRippleDisabled`,p.disabled||p.disableRipple))},dependencies:[hm,li],styles:[`.mat-mdc-option {
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
`],encapsulation:2})})()}return t})();function ww(t,n,e){if(e.length){let i=n.toArray(),a=e.toArray(),o=0;for(let r=0;r<t+1;r++)i[r].group&&i[r].group===a[o]&&o++;return o}return 0}function Ew(t,n,e,i){return t<e?t:t+n>e+i?Math.max(0,t-i+n):e}var Dw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[we]})}return t})();var Eb=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[ki,Dw,Un,we]})}return t})();var qR=new N(`mat-select-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t)}});var YR=new N(`MAT_SELECT_CONFIG`);var KR=new N(`MatSelectTrigger`);var Db=class{source;value;constructor(n,e){this.source=n,this.value=e}};var Oa=(()=>{class t{_viewportRuler=f(ui);_changeDetectorRef=f(Fe);_elementRef=f(Y);_dir=f(gt,{optional:!0});_idGenerator=f(Be);_renderer=f(Oe);_parentFormField=f(Hl,{optional:!0});ngControl=f(Mi,{self:!0,optional:!0});_liveAnnouncer=f(Bg);_defaultOptions=f(YR,{optional:!0});_animationsDisabled=Ue();_popoverLocation;_initialized=new V;_cleanupDetach;options;optionGroups;customTrigger;_positions=[{originX:`start`,originY:`bottom`,overlayX:`start`,overlayY:`top`},{originX:`end`,originY:`bottom`,overlayX:`end`,overlayY:`top`},{originX:`start`,originY:`top`,overlayX:`start`,overlayY:`bottom`,panelClass:`mat-mdc-select-panel-above`},{originX:`end`,originY:`top`,overlayX:`end`,overlayY:`bottom`,panelClass:`mat-mdc-select-panel-above`}];_scrollOptionIntoView(e){let i=this.options.toArray()[e];if(i){let a=this.panel.nativeElement,o=ww(e,this.options,this.optionGroups),r=i._getHostElement();e===0&&o===1?a.scrollTop=0:a.scrollTop=Ew(r.offsetTop,r.offsetHeight,a.scrollTop,a.offsetHeight)}}_positioningSettled(){this._scrollOptionIntoView(this._keyManager.activeItemIndex||0)}_getChangeEvent(e){return new Db(this,e)}_scrollStrategyFactory=f(qR);_panelOpen=!1;_compareWith=(e,i)=>e===i;_uid=this._idGenerator.getId(`mat-select-`);_triggerAriaLabelledBy=null;_previousControl;_destroy=new V;_errorStateTracker;stateChanges=new V;disableAutomaticLabeling=!0;userAriaDescribedBy;_selectionModel;_keyManager;_preferredOverlayOrigin;_overlayWidth;_onChange=()=>{};_onTouched=()=>{};_valueId=this._idGenerator.getId(`mat-select-value-`);_scrollStrategy;_overlayPanelClass=this._defaultOptions?.overlayPanelClass||``;get focused(){return this._focused||this._panelOpen}_focused=!1;controlType=`mat-select`;trigger;panel;_overlayDir;panelClass;disabled=!1;get disableRipple(){return this._disableRipple()}set disableRipple(e){this._disableRipple.set(e)}_disableRipple=k(!1);tabIndex=0;get hideSingleSelectionIndicator(){return this._hideSingleSelectionIndicator}set hideSingleSelectionIndicator(e){this._hideSingleSelectionIndicator=e,this._syncParentProperties()}_hideSingleSelectionIndicator=this._defaultOptions?.hideSingleSelectionIndicator??!1;get placeholder(){return this._placeholder}set placeholder(e){this._placeholder=e,this.stateChanges.next()}_placeholder;get required(){return this._required??this.ngControl?.control?.hasValidator(Po.required)??!1}set required(e){this._required=e,this.stateChanges.next()}_required;get multiple(){return this._multiple}set multiple(e){this._selectionModel,this._multiple=e}_multiple=!1;disableOptionCentering=this._defaultOptions?.disableOptionCentering??!1;get compareWith(){return this._compareWith}set compareWith(e){this._compareWith=e,this._selectionModel&&this._initializeSelection()}get value(){return this._value}set value(e){this._assignValue(e)&&this._onChange(e)}_value;ariaLabel=``;ariaLabelledby;get errorStateMatcher(){return this._errorStateTracker.matcher}set errorStateMatcher(e){this._errorStateTracker.matcher=e}typeaheadDebounceInterval;sortComparator;get id(){return this._id}set id(e){this._id=e||this._uid,this.stateChanges.next()}_id;get errorState(){return this._errorStateTracker.errorState}set errorState(e){this._errorStateTracker.errorState=e}panelWidth=this._defaultOptions&&typeof this._defaultOptions.panelWidth<`u`?this._defaultOptions.panelWidth:`auto`;canSelectNullableOptions=this._defaultOptions?.canSelectNullableOptions??!1;optionSelectionChanges=Ka(()=>{let e=this.options;return e?e.changes.pipe(mt(e),_n(()=>kt(...e.map(i=>i.onSelectionChange)))):this._initialized.pipe(_n(()=>this.optionSelectionChanges))});openedChange=new te;_openedStream=this.openedChange.pipe(Qe(e=>e),st(()=>{}));_closedStream=this.openedChange.pipe(Qe(e=>!e),st(()=>{}));selectionChange=new te;valueChange=new te;constructor(){let e=f(Qr),i=f(Vl,{optional:!0}),a=f(Bl,{optional:!0}),o=f(new Ki(`tabindex`),{optional:!0}),r=f(Kl,{optional:!0}),s=f(Ju,{optional:!0,self:!0});this.ngControl&&(this.ngControl.valueAccessor=this),this._defaultOptions?.typeaheadDebounceInterval!=null&&(this.typeaheadDebounceInterval=this._defaultOptions.typeaheadDebounceInterval),this._errorStateTracker=new Xr(e,s||this.ngControl,a,i,this.stateChanges),this._scrollStrategy=this._scrollStrategyFactory(),this.tabIndex=o==null?0:parseInt(o)||0,this._popoverLocation=r?.usePopover===!1?null:`inline`,this.id=this.id}ngOnInit(){this._selectionModel=new jo(this.multiple),this.stateChanges.next(),this._viewportRuler.change().pipe(Ne(this._destroy)).subscribe(()=>{this.panelOpen&&(this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._changeDetectorRef.detectChanges())})}ngAfterContentInit(){this._initialized.next(),this._initialized.complete(),this._initKeyManager(),this._selectionModel.changed.pipe(Ne(this._destroy)).subscribe(e=>{e.added.forEach(i=>i.select()),e.removed.forEach(i=>i.deselect())}),this.options.changes.pipe(mt(null),Ne(this._destroy)).subscribe(()=>{this._resetOptions(),this._initializeSelection()})}ngDoCheck(){let e=this._getTriggerAriaLabelledby(),i=this.ngControl;if(e!==this._triggerAriaLabelledBy){let a=this._elementRef.nativeElement;this._triggerAriaLabelledBy=e,e?a.setAttribute(`aria-labelledby`,e):a.removeAttribute(`aria-labelledby`)}i&&(this._previousControl!==i.control&&(this._previousControl!==void 0&&i.disabled!==null&&i.disabled!==this.disabled&&(this.disabled=i.disabled),this._previousControl=i.control),this.updateErrorState())}ngOnChanges(e){(e.disabled||e.userAriaDescribedBy)&&this.stateChanges.next(),e.typeaheadDebounceInterval&&this._keyManager&&this._keyManager.withTypeAhead(this.typeaheadDebounceInterval),e.panelClass&&this.panelClass instanceof Set&&(this.panelClass=Array.from(this.panelClass))}ngOnDestroy(){this._cleanupDetach?.(),this._keyManager?.destroy(),this._destroy.next(),this._destroy.complete(),this.stateChanges.complete()}toggle(){this.panelOpen?this.close():this.open()}open(){this._canOpen()&&(this._parentFormField&&(this._preferredOverlayOrigin=this._parentFormField.getConnectedOverlayOrigin()),this._cleanupDetach?.(),this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._panelOpen=!0,this._overlayDir.positionChange.pipe(vn(1)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this._positioningSettled()}),this._overlayDir.attachOverlay(),this._keyManager.withHorizontalOrientation(null),this._highlightCorrectOption(),this._changeDetectorRef.markForCheck(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!0)))}close(){this._panelOpen&&(this._panelOpen=!1,this._exitAndDetach(),this._keyManager.withHorizontalOrientation(this._isRtl()?`rtl`:`ltr`),this._changeDetectorRef.markForCheck(),this._onTouched(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!1)))}_exitAndDetach(){if(this._animationsDisabled||!this.panel){this._detachOverlay();return}this._cleanupDetach?.(),this._cleanupDetach=()=>{i(),clearTimeout(a),this._cleanupDetach=void 0};let e=this.panel.nativeElement,i=this._renderer.listen(e,`animationend`,o=>{o.animationName===`_mat-select-exit`&&(this._cleanupDetach?.(),this._detachOverlay())}),a=setTimeout(()=>{this._cleanupDetach?.(),this._detachOverlay()},200);e.classList.add(`mat-select-panel-exit`)}_detachOverlay(){this._overlayDir.detachOverlay(),this._changeDetectorRef.markForCheck()}writeValue(e){this._assignValue(e)}registerOnChange(e){this._onChange=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e,this._changeDetectorRef.markForCheck(),this.stateChanges.next()}get panelOpen(){return this._panelOpen}get selected(){return this.multiple?this._selectionModel?.selected||[]:this._selectionModel?.selected[0]}get triggerValue(){if(this.empty)return``;if(this._multiple){let e=this._selectionModel.selected.map(i=>i.viewValue);return this._isRtl()&&e.reverse(),e.join(`, `)}return this._selectionModel.selected[0].viewValue}updateErrorState(){this._errorStateTracker.updateErrorState()}_isRtl(){return this._dir?this._dir.value===`rtl`:!1}_handleKeydown(e){this.disabled||(this.panelOpen?this._handleOpenKeydown(e):this._handleClosedKeydown(e))}_handleClosedKeydown(e){let i=e.keyCode,a=i===40||i===38||i===37||i===39,o=i===13||i===32,r=this._keyManager;if(!r.isTyping()&&o&&!Ct(e)||(this.multiple||e.altKey)&&a)e.preventDefault(),this.open();else if(!this.multiple){let s=this.selected;r.onKeydown(e);let l=this.selected;l&&s!==l&&this._liveAnnouncer.announce(l.viewValue,1e4)}}_handleOpenKeydown(e){let i=this._keyManager,a=e.keyCode,o=a===40||a===38,r=i.isTyping();if(o&&e.altKey)e.preventDefault(),this.close();else if(!r&&(a===13||a===32)&&i.activeItem&&!Ct(e))e.preventDefault(),i.activeItem._selectViaInteraction();else if(!r&&this._multiple&&a===65&&e.ctrlKey){e.preventDefault();let s=this.options.some(l=>!l.disabled&&!l.selected);this.options.forEach(l=>{l.disabled||(s?l.select():l.deselect())})}else{let s=i.activeItemIndex;i.onKeydown(e),this._multiple&&o&&e.shiftKey&&i.activeItem&&i.activeItemIndex!==s&&i.activeItem._selectViaInteraction()}}_handleOverlayKeydown(e){e.keyCode===27&&!Ct(e)&&(e.preventDefault(),this.close())}_onFocus(){this.disabled||(this._focused=!0,this.stateChanges.next())}_onBlur(){this._focused=!1,this._keyManager?.cancelTypeahead(),!this.disabled&&!this.panelOpen&&(this._onTouched(),this._changeDetectorRef.markForCheck(),this.stateChanges.next())}get empty(){return!this._selectionModel||this._selectionModel.isEmpty()}_initializeSelection(){Promise.resolve().then(()=>{this.ngControl&&(this._value=this.ngControl.value),this._setSelectionByValue(this._value),this.stateChanges.next()})}_setSelectionByValue(e){if(this.options.forEach(i=>i.setInactiveStyles()),this._selectionModel.clear(),this.multiple&&e)e.forEach(i=>this._selectOptionByValue(i)),this._sortValues();else{let i=this._selectOptionByValue(e);i?this._keyManager.updateActiveItem(i):this.panelOpen||this._keyManager.updateActiveItem(-1)}this._changeDetectorRef.markForCheck()}_selectOptionByValue(e){let i=this.options.find(a=>{if(this._selectionModel.isSelected(a))return!1;try{return(a.value!=null||this.canSelectNullableOptions)&&this._compareWith(a.value,e)}catch{return!1}});return i&&this._selectionModel.select(i),i}_assignValue(e){return e!==this._value||this._multiple&&Array.isArray(e)?(this.options&&this._setSelectionByValue(e),this._value=e,!0):!1}_skipPredicate=e=>this.panelOpen?!1:e.disabled;_getOverlayWidth(e){return this.panelWidth===`auto`?(e instanceof ns?e.elementRef:e||this._elementRef).nativeElement.getBoundingClientRect().width:this.panelWidth===null?``:this.panelWidth}_syncParentProperties(){if(this.options)for(let e of this.options)e._changeDetectorRef.markForCheck()}_initKeyManager(){this._keyManager=new Ml(this.options).withTypeAhead(this.typeaheadDebounceInterval).withVerticalOrientation().withHorizontalOrientation(this._isRtl()?`rtl`:`ltr`).withHomeAndEnd().withPageUpDown().withAllowedModifierKeys([`shiftKey`]).skipPredicate(this._skipPredicate),this._keyManager.tabOut.subscribe(()=>{this.panelOpen&&(!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction(),this.focus(),this.close())}),this._keyManager.change.subscribe(()=>{this._panelOpen&&this.panel?this._scrollOptionIntoView(this._keyManager.activeItemIndex||0):!this._panelOpen&&!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction()})}_resetOptions(){let e=kt(this.options.changes,this._destroy);this.optionSelectionChanges.pipe(Ne(e)).subscribe(i=>{this._onSelect(i.source,i.isUserInput),i.isUserInput&&!this.multiple&&this._panelOpen&&(this.close(),this.focus())}),kt(...this.options.map(i=>i._stateChanges)).pipe(Ne(e)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this.stateChanges.next()})}_onSelect(e,i){let a=this._selectionModel.isSelected(e);!this.canSelectNullableOptions&&e.value==null&&!this._multiple?(e.deselect(),this._selectionModel.clear(),this.value!=null&&this._propagateChanges(e.value)):(a!==e.selected&&(e.selected?this._selectionModel.select(e):this._selectionModel.deselect(e)),i&&this._keyManager.setActiveItem(e),this.multiple&&(this._sortValues(),i&&this.focus())),a!==this._selectionModel.isSelected(e)&&this._propagateChanges(),this.stateChanges.next()}_sortValues(){if(this.multiple){let e=this.options.toArray();this._selectionModel.sort((i,a)=>this.sortComparator?this.sortComparator(i,a,e):e.indexOf(i)-e.indexOf(a)),this.stateChanges.next()}}_propagateChanges(e){let i;this.multiple?i=this.selected.map(a=>a.value):i=this.selected?this.selected.value:e,this._value=i,this.valueChange.emit(i),this._onChange(i),this.selectionChange.emit(this._getChangeEvent(i)),this._changeDetectorRef.markForCheck()}_highlightCorrectOption(){if(this._keyManager)if(this.empty){let e=-1;for(let i=0;i<this.options.length;i++)if(!this.options.get(i).disabled){e=i;break}this._keyManager.setActiveItem(e)}else this._keyManager.setActiveItem(this._selectionModel.selected[0])}_canOpen(){return!this._panelOpen&&!this.disabled&&this.options?.length>0&&!!this._overlayDir}focus(e){this._elementRef.nativeElement.focus(e)}_getPanelAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||null,i=e?e+` `:``;return this.ariaLabelledby?i+this.ariaLabelledby:e}_getAriaActiveDescendant(){return this.panelOpen&&this._keyManager&&this._keyManager.activeItem?this._keyManager.activeItem.id:null}_getTriggerAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||``;return this.ariaLabelledby&&(e+=` `+this.ariaLabelledby),e||(e=this._valueId),e}get describedByIds(){return this._elementRef.nativeElement.getAttribute(`aria-describedby`)?.split(` `)||[]}setDescribedByIds(e){let i=this._elementRef.nativeElement;e.length?i.setAttribute(`aria-describedby`,e.join(` `)):i.removeAttribute(`aria-describedby`)}onContainerClick(e){let i=Yt(e);i&&(i.tagName===`MAT-OPTION`||i.classList.contains(`cdk-overlay-backdrop`)||i.closest(`.mat-mdc-select-panel`))||(this.focus(),this.open())}get shouldLabelFloat(){return this.panelOpen||!this.empty||this.focused&&!!this.placeholder}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`trigger`],i=[`panel`],a=[[[`mat-select-trigger`]],`*`],o=[`mat-select-trigger`,`*`];function r(g,_){if(g&1&&(c(0,`span`,4),u(1),d()),g&2){let b=v();m(),w(b.placeholder)}}function s(g,_){g&1&&he(0)}function l(g,_){if(g&1&&(c(0,`span`,11),u(1),d()),g&2){let b=v(2);m(),w(b.triggerValue)}}function h(g,_){if(g&1&&(c(0,`span`,5),E(1,s,1,0)(2,l,2,1,`span`,11),d()),g&2){let b=v();m(),D(b.customTrigger?1:2)}}function p(g,_){if(g&1){let b=$();c(0,`div`,12,1),C(`keydown`,function(I){M(b);return T(v()._handleKeydown(I))}),he(2,1),d()}if(g&2){let b=v();tt(b.panelClass),G(`mat-select-panel-animations-enabled`,!b._animationsDisabled)(`mat-primary`,b._parentFormField?.color===`primary`)(`mat-accent`,b._parentFormField?.color===`accent`)(`mat-warn`,b._parentFormField?.color===`warn`)(`mat-undefined`,!b._parentFormField?.color),ne(`id`,b.id+`-panel`)(`aria-multiselectable`,b.multiple)(`aria-label`,b.ariaLabel||null)(`aria-labelledby`,b._getPanelAriaLabelledby())}}return H({type:t,selectors:[[`mat-select`]],contentQueries:function(_,b,y){if(_&1&&jt(y,KR,5)(y,Un,5)(y,wb,5),_&2){let I;J(I=ee())&&(b.customTrigger=I.first),J(I=ee())&&(b.options=I),J(I=ee())&&(b.optionGroups=I)}},viewQuery:function(_,b){if(_&1&&Ze(e,5)(i,5)(mm,5),_&2){let y;J(y=ee())&&(b.trigger=y.first),J(y=ee())&&(b.panel=y.first),J(y=ee())&&(b._overlayDir=y.first)}},hostAttrs:[`role`,`combobox`,`aria-haspopup`,`listbox`,1,`mat-mdc-select`],hostVars:21,hostBindings:function(_,b){_&1&&C(`keydown`,function(I){return b._handleKeydown(I)})(`focus`,function(){return b._onFocus()})(`blur`,function(){return b._onBlur()}),_&2&&(ne(`id`,b.id)(`tabindex`,b.disabled?-1:b.tabIndex)(`aria-controls`,b.panelOpen?b.id+`-panel`:null)(`aria-expanded`,b.panelOpen)(`aria-label`,b.ariaLabel||null)(`aria-required`,b.required.toString())(`aria-disabled`,b.disabled.toString())(`aria-invalid`,b.errorState)(`aria-activedescendant`,b._getAriaActiveDescendant()),G(`mat-mdc-select-disabled`,b.disabled)(`mat-mdc-select-invalid`,b.errorState)(`mat-mdc-select-required`,b.required)(`mat-mdc-select-empty`,b.empty)(`mat-mdc-select-multiple`,b.multiple)(`mat-select-open`,b.panelOpen))},inputs:{userAriaDescribedBy:[0,`aria-describedby`,`userAriaDescribedBy`],panelClass:`panelClass`,disabled:[2,`disabled`,`disabled`,z],disableRipple:[2,`disableRipple`,`disableRipple`,z],tabIndex:[2,`tabIndex`,`tabIndex`,g=>g==null?0:ct(g)],hideSingleSelectionIndicator:[2,`hideSingleSelectionIndicator`,`hideSingleSelectionIndicator`,z],placeholder:`placeholder`,required:[2,`required`,`required`,z],multiple:[2,`multiple`,`multiple`,z],disableOptionCentering:[2,`disableOptionCentering`,`disableOptionCentering`,z],compareWith:`compareWith`,value:`value`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],errorStateMatcher:`errorStateMatcher`,typeaheadDebounceInterval:[2,`typeaheadDebounceInterval`,`typeaheadDebounceInterval`,ct],sortComparator:`sortComparator`,id:`id`,panelWidth:`panelWidth`,canSelectNullableOptions:[2,`canSelectNullableOptions`,`canSelectNullableOptions`,z]},outputs:{openedChange:`openedChange`,_openedStream:`opened`,_closedStream:`closed`,selectionChange:`selectionChange`,valueChange:`valueChange`},exportAs:[`matSelect`],features:[Ge([{provide:zl,useExisting:t},{provide:Sb,useExisting:t}]),_t],ngContentSelectors:o,decls:11,vars:10,consts:[[`fallbackOverlayOrigin`,`cdkOverlayOrigin`,`trigger`,``],[`panel`,``],[`cdk-overlay-origin`,``,1,`mat-mdc-select-trigger`,3,`click`],[1,`mat-mdc-select-value`],[1,`mat-mdc-select-placeholder`,`mat-mdc-select-min-line`],[1,`mat-mdc-select-value-text`],[1,`mat-mdc-select-arrow-wrapper`],[1,`mat-mdc-select-arrow`],[`viewBox`,`0 0 24 24`,`width`,`24px`,`height`,`24px`,`focusable`,`false`,`aria-hidden`,`true`],[`d`,`M7 10l5 5 5-5z`],[`cdk-connected-overlay`,``,`cdkConnectedOverlayHasBackdrop`,``,`cdkConnectedOverlayBackdropClass`,`cdk-overlay-transparent-backdrop`,3,`detach`,`backdropClick`,`overlayKeydown`,`cdkConnectedOverlayDisableClose`,`cdkConnectedOverlayPanelClass`,`cdkConnectedOverlayScrollStrategy`,`cdkConnectedOverlayOrigin`,`cdkConnectedOverlayPositions`,`cdkConnectedOverlayWidth`,`cdkConnectedOverlayFlexibleDimensions`,`cdkConnectedOverlayUsePopover`],[1,`mat-mdc-select-min-line`],[`role`,`listbox`,`tabindex`,`-1`,1,`mat-mdc-select-panel`,`mdc-menu-surface`,`mdc-menu-surface--open`,3,`keydown`]],template:function(_,b){if(_&1&&(Pe(a),c(0,`div`,2,0),C(`click`,function(){return b.open()}),c(3,`div`,3),E(4,r,2,1,`span`,4)(5,h,3,1,`span`,5),d(),c(6,`div`,6)(7,`div`,7),Gt(),c(8,`svg`,8),U(9,`path`,9),d()()()(),rt(10,p,3,16,`ng-template`,10),C(`detach`,function(){return b.close()})(`backdropClick`,function(){return b.close()})(`overlayKeydown`,function(I){return b._handleOverlayKeydown(I)})),_&2){let y=ft(1);m(3),ne(`id`,b._valueId),m(),D(b.empty?4:5),m(6),S(`cdkConnectedOverlayDisableClose`,!0)(`cdkConnectedOverlayPanelClass`,b._overlayPanelClass)(`cdkConnectedOverlayScrollStrategy`,b._scrollStrategy)(`cdkConnectedOverlayOrigin`,b._preferredOverlayOrigin||y)(`cdkConnectedOverlayPositions`,b._positions)(`cdkConnectedOverlayWidth`,b._overlayWidth)(`cdkConnectedOverlayFlexibleDimensions`,!0)(`cdkConnectedOverlayUsePopover`,b._popoverLocation)}},dependencies:[ns,mm],styles:[`@keyframes _mat-select-enter {
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
`],encapsulation:2})})()}return t})();var Pa=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[mi,Eb,we,ia,di,Eb]})}return t})();var QR=20;var XR=new N(`mat-tooltip-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>ra(t,{scrollThrottle:QR})}});var ZR=new N(`mat-tooltip-default-options`,{providedIn:`root`,factory:()=>({showDelay:0,hideDelay:0,touchendHideDelay:1500})});var Iw=`tooltip-panel`;var JR={passive:!0};var eO=8;var tO=8;var nO=24;var iO=200;var kw=(()=>{class t{_elementRef=f(Y);_ngZone=f(Z);_platform=f(Re);_ariaDescriber=f(Ou);_focusMonitor=f(en);_dir=f(gt);_injector=f(se);_viewContainerRef=f(Bt);_mediaMatcher=f(Vr);_document=f(de);_renderer=f(Oe);_animationsDisabled=Ue();_defaultOptions=f(ZR,{optional:!0});_overlayRef=null;_tooltipInstance=null;_overlayPanelClass;_portal;_position=`below`;_positionAtOrigin=!1;_disabled=!1;_tooltipClass;_viewInitialized=!1;_pointerExitEventsInitialized=!1;_tooltipComponent=aO;_viewportMargin=8;_currentPosition;_cssClassPrefix=`mat-mdc`;_ariaDescriptionPending=!1;_dirSubscribed=!1;get position(){return this._position}set position(e){e!==this._position&&(this._position=e,this._overlayRef&&(this._updatePosition(this._overlayRef),this._tooltipInstance?.show(0),this._overlayRef.updatePosition()))}get positionAtOrigin(){return this._positionAtOrigin}set positionAtOrigin(e){this._positionAtOrigin=ta(e),this._detach(),this._overlayRef=null}get disabled(){return this._disabled}set disabled(e){let i=ta(e);this._disabled!==i&&(this._disabled=i,i?this.hide(0):this._setupPointerEnterEventsIfNeeded(),this._syncAriaDescription(this.message))}get showDelay(){return this._showDelay}set showDelay(e){this._showDelay=ri(e)}_showDelay;get hideDelay(){return this._hideDelay}set hideDelay(e){this._hideDelay=ri(e),this._tooltipInstance&&(this._tooltipInstance._mouseLeaveHideDelay=this._hideDelay)}_hideDelay;touchGestures=`auto`;get message(){return this._message}set message(e){let i=this._message;this._message=e!=null?String(e).trim():``,!this._message&&this._isTooltipVisible()?this.hide(0):(this._setupPointerEnterEventsIfNeeded(),this._updateTooltipMessage()),this._syncAriaDescription(i)}_message=``;get tooltipClass(){return this._tooltipClass}set tooltipClass(e){this._tooltipClass=e,this._tooltipInstance&&this._setTooltipClass(this._tooltipClass)}_eventCleanups=[];_touchstartTimeout=null;_destroyed=new V;_isDestroyed=!1;constructor(){let e=this._defaultOptions;e&&(this._showDelay=e.showDelay,this._hideDelay=e.hideDelay,e.position&&(this.position=e.position),e.positionAtOrigin&&(this.positionAtOrigin=e.positionAtOrigin),e.touchGestures&&(this.touchGestures=e.touchGestures),e.tooltipClass&&(this.tooltipClass=e.tooltipClass)),this._viewportMargin=eO}ngAfterViewInit(){this._viewInitialized=!0,this._setupPointerEnterEventsIfNeeded(),this._focusMonitor.monitor(this._elementRef).pipe(Ne(this._destroyed)).subscribe(e=>{e?e===`keyboard`&&this._ngZone.run(()=>this.show()):this._ngZone.run(()=>this.hide(0))})}ngOnDestroy(){let e=this._elementRef.nativeElement;this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this._overlayRef&&(this._overlayRef.dispose(),this._tooltipInstance=null),this._eventCleanups.forEach(i=>i()),this._eventCleanups.length=0,this._destroyed.next(),this._destroyed.complete(),this._isDestroyed=!0,this._ariaDescriber.removeDescription(e,this.message,`tooltip`),this._focusMonitor.stopMonitoring(e)}show(e=this.showDelay,i){if(this.disabled||!this.message||this._isTooltipVisible()){this._tooltipInstance?._cancelPendingAnimations();return}let a=this._createOverlay(i);this._detach(),this._portal=this._portal||new Na(this._tooltipComponent,this._viewContainerRef);let o=this._tooltipInstance=a.attach(this._portal).instance;o._triggerElement=this._elementRef.nativeElement,o._mouseLeaveHideDelay=this._hideDelay,o.afterHidden().pipe(Ne(this._destroyed)).subscribe(()=>this._detach()),this._setTooltipClass(this._tooltipClass),this._updateTooltipMessage(),o.show(e)}hide(e=this.hideDelay){let i=this._tooltipInstance;i&&(i.isVisible()?i.hide(e):(i._cancelPendingAnimations(),this._detach()))}toggle(e){this._isTooltipVisible()?this.hide():this.show(void 0,e)}_isTooltipVisible(){return!!this._tooltipInstance&&this._tooltipInstance.isVisible()}_createOverlay(e){if(this._overlayRef){let r=this._overlayRef.getConfig().positionStrategy;if((!this.positionAtOrigin||!e)&&r._origin instanceof Y)return this._overlayRef;this._detach()}let i=this._injector.get(Ra).getAncestorScrollContainers(this._elementRef),a=`${this._cssClassPrefix}-${Iw}`,o=Bo(this._injector,this.positionAtOrigin?e||this._elementRef:this._elementRef).withTransformOriginOn(`.${this._cssClassPrefix}-tooltip`).withFlexibleDimensions(!1).withViewportMargin(this._viewportMargin).withScrollableContainers(i).withPopoverLocation(`global`);return o.positionChanges.pipe(Ne(this._destroyed)).subscribe(r=>{this._updateCurrentPositionClass(r.connectionPair),this._tooltipInstance&&r.scrollableViewProperties.isOverlayClipped&&this._tooltipInstance.isVisible()&&this._ngZone.run(()=>this.hide(0))}),this._overlayRef=sa(this._injector,{direction:this._dir,positionStrategy:o,panelClass:this._overlayPanelClass?[...this._overlayPanelClass,a]:a,scrollStrategy:this._injector.get(XR)(),disableAnimations:this._animationsDisabled,eventPredicate:this._overlayEventPredicate}),this._updatePosition(this._overlayRef),this._overlayRef.detachments().pipe(Ne(this._destroyed)).subscribe(()=>this._detach()),this._overlayRef.outsidePointerEvents().pipe(Ne(this._destroyed)).subscribe(()=>this._tooltipInstance?._handleBodyInteraction()),this._overlayRef.keydownEvents().pipe(Ne(this._destroyed)).subscribe(r=>{r.preventDefault(),r.stopPropagation(),this._ngZone.run(()=>this.hide(0))}),this._defaultOptions?.disableTooltipInteractivity&&this._overlayRef.addPanelClass(`${this._cssClassPrefix}-tooltip-panel-non-interactive`),this._dirSubscribed||(this._dirSubscribed=!0,this._dir.change.pipe(Ne(this._destroyed)).subscribe(()=>{this._overlayRef&&this._updatePosition(this._overlayRef)})),this._overlayRef}_detach(){this._overlayRef&&this._overlayRef.hasAttached()&&this._overlayRef.detach(),this._tooltipInstance=null}_updatePosition(e){let i=e.getConfig().positionStrategy,a=this._getOrigin(),o=this._getOverlayPosition();i.withPositions([this._addOffset(r(r({},a.main),o.main)),this._addOffset(r(r({},a.fallback),o.fallback))])}_addOffset(e){let i=tO,a=!this._dir||this._dir.value==`ltr`;return e.originY===`top`?e.offsetY=-i:e.originY===`bottom`?e.offsetY=i:e.originX===`start`?e.offsetX=a?-i:i:e.originX===`end`&&(e.offsetX=a?i:-i),e}_getOrigin(){let e=!this._dir||this._dir.value==`ltr`,i=this.position,a;i==`above`||i==`below`?a={originX:`center`,originY:i==`above`?`top`:`bottom`}:i==`before`||i==`left`&&e||i==`right`&&!e?a={originX:`start`,originY:`center`}:(i==`after`||i==`right`&&e||i==`left`&&!e)&&(a={originX:`end`,originY:`center`});let{x:o,y:r}=this._invertPosition(a.originX,a.originY);return{main:a,fallback:{originX:o,originY:r}}}_getOverlayPosition(){let e=!this._dir||this._dir.value==`ltr`,i=this.position,a;i==`above`?a={overlayX:`center`,overlayY:`bottom`}:i==`below`?a={overlayX:`center`,overlayY:`top`}:i==`before`||i==`left`&&e||i==`right`&&!e?a={overlayX:`end`,overlayY:`center`}:(i==`after`||i==`right`&&e||i==`left`&&!e)&&(a={overlayX:`start`,overlayY:`center`});let{x:o,y:r}=this._invertPosition(a.overlayX,a.overlayY);return{main:a,fallback:{overlayX:o,overlayY:r}}}_updateTooltipMessage(){this._tooltipInstance&&(this._tooltipInstance.message=this.message,this._tooltipInstance._markForCheck(),Rt(()=>{this._tooltipInstance&&this._overlayRef.updatePosition()},{injector:this._injector}))}_setTooltipClass(e){this._tooltipInstance&&(this._tooltipInstance.tooltipClass=e instanceof Set?Array.from(e):e,this._tooltipInstance._markForCheck())}_invertPosition(e,i){return this.position===`above`||this.position===`below`?i===`top`?i=`bottom`:i===`bottom`&&(i=`top`):e===`end`?e=`start`:e===`start`&&(e=`end`),{x:e,y:i}}_updateCurrentPositionClass(e){let{overlayY:i,originX:a,originY:o}=e,r;if(i===`center`?this._dir&&this._dir.value===`rtl`?r=a===`end`?`left`:`right`:r=a===`start`?`left`:`right`:r=i===`bottom`&&o===`top`?`above`:`below`,r!==this._currentPosition){let s=this._overlayRef;if(s){let l=`${this._cssClassPrefix}-${Iw}-`;s.removePanelClass(l+this._currentPosition),s.addPanelClass(l+r)}this._currentPosition=r}}_setupPointerEnterEventsIfNeeded(){this._disabled||!this.message||!this._viewInitialized||this._eventCleanups.length||(this._isTouchPlatform()?this.touchGestures!==`off`&&(this._disableNativeGesturesIfNecessary(),this._addListener(`touchstart`,e=>{let i=e.targetTouches?.[0],a=i?{x:i.clientX,y:i.clientY}:void 0;this._setupPointerExitEventsIfNeeded(),this._touchstartTimeout&&clearTimeout(this._touchstartTimeout);let o=500;this._touchstartTimeout=setTimeout(()=>{this._touchstartTimeout=null,this.show(void 0,a)},this._defaultOptions?.touchLongPressShowDelay??o)})):this._addListener(`mouseenter`,e=>{this._setupPointerExitEventsIfNeeded();let i;e.x!==void 0&&e.y!==void 0&&(i=e),this.show(void 0,i)}))}_setupPointerExitEventsIfNeeded(){if(!this._pointerExitEventsInitialized){if(this._pointerExitEventsInitialized=!0,!this._isTouchPlatform())this._addListener(`mouseleave`,e=>{let i=e.relatedTarget;(!i||!this._overlayRef?.overlayElement.contains(i))&&this.hide()}),this._addListener(`wheel`,e=>{if(this._isTooltipVisible()){let i=this._document.elementFromPoint(e.clientX,e.clientY),a=this._elementRef.nativeElement;i!==a&&!a.contains(i)&&this.hide()}});else if(this.touchGestures!==`off`){this._disableNativeGesturesIfNecessary();let e=()=>{this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this.hide(this._defaultOptions?.touchendHideDelay)};this._addListener(`touchend`,e),this._addListener(`touchcancel`,e)}}}_addListener(e,i){this._eventCleanups.push(this._renderer.listen(this._elementRef.nativeElement,e,i,JR))}_isTouchPlatform(){let e=this._defaultOptions?.detectHoverCapability;return typeof e==`function`?!e():this._platform.IOS||this._platform.ANDROID?!0:this._platform.isBrowser?!!e&&this._mediaMatcher.matchMedia(`(any-hover: none)`).matches:!1}_disableNativeGesturesIfNecessary(){let e=this.touchGestures;if(e!==`off`){let i=this._elementRef.nativeElement,a=i.style;(e===`on`||i.nodeName!==`INPUT`&&i.nodeName!==`TEXTAREA`)&&(a.userSelect=a.msUserSelect=a.webkitUserSelect=a.MozUserSelect=`none`),(e===`on`||!i.draggable)&&(a.webkitUserDrag=`none`),a.touchAction=`none`,a.webkitTapHighlightColor=`transparent`}}_syncAriaDescription(e){this._ariaDescriptionPending||(this._ariaDescriptionPending=!0,this._ariaDescriber.removeDescription(this._elementRef.nativeElement,e,`tooltip`),this._isDestroyed||Rt({write:()=>{this._ariaDescriptionPending=!1,this.message&&!this.disabled&&this._ariaDescriber.describe(this._elementRef.nativeElement,this.message,`tooltip`)}},{injector:this._injector}))}_overlayEventPredicate=e=>e.type===`keydown`?this._isTooltipVisible()&&e.keyCode===27&&!Ct(e):!0;static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`matTooltip`,``]],hostAttrs:[1,`mat-mdc-tooltip-trigger`],hostVars:2,hostBindings:function(i,a){i&2&&G(`mat-mdc-tooltip-disabled`,a.disabled)},inputs:{position:[0,`matTooltipPosition`,`position`],positionAtOrigin:[0,`matTooltipPositionAtOrigin`,`positionAtOrigin`],disabled:[0,`matTooltipDisabled`,`disabled`],showDelay:[0,`matTooltipShowDelay`,`showDelay`],hideDelay:[0,`matTooltipHideDelay`,`hideDelay`],touchGestures:[0,`matTooltipTouchGestures`,`touchGestures`],message:[0,`matTooltip`,`message`],tooltipClass:[0,`matTooltipClass`,`tooltipClass`]},exportAs:[`matTooltip`]})}return t})();var aO=(()=>{class t{_changeDetectorRef=f(Fe);_elementRef=f(Y);_isMultiline=!1;message;tooltipClass;_showTimeoutId;_hideTimeoutId;_triggerElement;_mouseLeaveHideDelay;_animationsDisabled=Ue();_tooltip;_closeOnInteraction=!1;_isVisible=!1;_onHide=new V;_showAnimation=`mat-mdc-tooltip-show`;_hideAnimation=`mat-mdc-tooltip-hide`;show(e){this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=setTimeout(()=>{this._toggleVisibility(!0),this._showTimeoutId=void 0},e)}hide(e){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId=setTimeout(()=>{this._toggleVisibility(!1),this._hideTimeoutId=void 0},e)}afterHidden(){return this._onHide}isVisible(){return this._isVisible}ngOnDestroy(){this._cancelPendingAnimations(),this._onHide.complete(),this._triggerElement=null}_handleBodyInteraction(){this._closeOnInteraction&&this.hide(0)}_markForCheck(){this._changeDetectorRef.markForCheck()}_handleMouseLeave({relatedTarget:e}){(!e||!this._triggerElement.contains(e))&&(this.isVisible()?this.hide(this._mouseLeaveHideDelay):this._finalizeAnimation(!1))}_onShow(){this._isMultiline=this._isTooltipMultiline(),this._markForCheck()}_isTooltipMultiline(){let e=this._elementRef.nativeElement.getBoundingClientRect();return e.height>nO&&e.width>=iO}_handleAnimationEnd({animationName:e}){(e===this._showAnimation||e===this._hideAnimation)&&this._finalizeAnimation(e===this._showAnimation)}_cancelPendingAnimations(){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=this._hideTimeoutId=void 0}_finalizeAnimation(e){e?this._closeOnInteraction=!0:this.isVisible()||this._onHide.next()}_toggleVisibility(e){let i=this._tooltip.nativeElement,a=this._showAnimation,o=this._hideAnimation;if(i.classList.remove(e?o:a),i.classList.add(e?a:o),this._isVisible!==e&&(this._isVisible=e,this._changeDetectorRef.markForCheck()),e&&!this._animationsDisabled&&typeof getComputedStyle==`function`){let r=getComputedStyle(i);(r.getPropertyValue(`animation-duration`)===`0s`||r.getPropertyValue(`animation-name`)===`none`)&&(this._animationsDisabled=!0)}e&&this._onShow(),this._animationsDisabled&&(i.classList.add(`_mat-animation-noopable`),this._finalizeAnimation(e))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`tooltip`];return H({type:t,selectors:[[`mat-tooltip-component`]],viewQuery:function(a,o){if(a&1&&Ze(e,7),a&2){let r;J(r=ee())&&(o._tooltip=r.first)}},hostAttrs:[`aria-hidden`,`true`],hostBindings:function(a,o){a&1&&C(`mouseleave`,function(s){return o._handleMouseLeave(s)})},decls:4,vars:5,consts:[[`tooltip`,``],[1,`mdc-tooltip`,`mat-mdc-tooltip`,3,`animationend`],[1,`mat-mdc-tooltip-surface`,`mdc-tooltip__surface`]],template:function(a,o){a&1&&(lt(0,`div`,1,0),Mr(`animationend`,function(s){return o._handleAnimationEnd(s)}),lt(2,`div`,2),u(3),ht()()),a&2&&(tt(o.tooltipClass),G(`mdc-tooltip--multiline`,o._isMultiline),m(3),w(o.message))},styles:[`.mat-mdc-tooltip {
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
`],encapsulation:2})})()}return t})();var Mw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[Mo,mi,we,ia]})}return t})();var oO=(()=>{class t{changes=new V;itemsPerPageLabel=`Items per page:`;nextPageLabel=`Next page`;previousPageLabel=`Previous page`;firstPageLabel=`First page`;lastPageLabel=`Last page`;getRangeLabel=(e,i,a)=>{if(a==0||i==0)return`0 of ${a}`;a=Math.max(a,0);let o=e*i,r=o<a?Math.min(o+i,a):o+i;return`${o+1} \u2013 ${r} of ${a}`};static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var rO=50;var sO=new N(`MAT_PAGINATOR_DEFAULT_OPTIONS`);var Ql=(()=>{class t{_intl=f(oO);_changeDetectorRef=f(Fe);_formFieldAppearance;_pageSizeLabelId=f(Be).getId(`mat-paginator-page-size-label-`);_intlChanges;_isInitialized=!1;_initializedStream=new Yn(1);color;get pageIndex(){return this._pageIndex}set pageIndex(e){this._pageIndex=Math.max(e||0,0),this._changeDetectorRef.markForCheck()}_pageIndex=0;get length(){return this._length}set length(e){this._length=e||0,this._changeDetectorRef.markForCheck()}_length=0;get pageSize(){return this._pageSize}set pageSize(e){this._pageSize=Math.max(e||0,0),this._updateDisplayedPageSizeOptions()}_pageSize;get pageSizeOptions(){return this._pageSizeOptions}set pageSizeOptions(e){this._pageSizeOptions=(e||[]).map(i=>ct(i,0)),this._updateDisplayedPageSizeOptions()}_pageSizeOptions=[];hidePageSize=!1;showFirstLastButtons=!1;selectConfig={};disabled=!1;page=new te;_displayedPageSizeOptions;initialized=this._initializedStream;constructor(){let e=this._intl,i=f(sO,{optional:!0});if(this._intlChanges=e.changes.subscribe(()=>this._changeDetectorRef.markForCheck()),i){let{pageSize:a,pageSizeOptions:o,hidePageSize:r,showFirstLastButtons:s}=i;a!=null&&(this._pageSize=a),o!=null&&(this._pageSizeOptions=o),r!=null&&(this.hidePageSize=r),s!=null&&(this.showFirstLastButtons=s)}this._formFieldAppearance=i?.formFieldAppearance||`outline`}ngOnInit(){this._isInitialized=!0,this._updateDisplayedPageSizeOptions(),this._initializedStream.next()}ngOnDestroy(){this._initializedStream.complete(),this._intlChanges.unsubscribe()}nextPage(){this.hasNextPage()&&this._navigate(this.pageIndex+1)}previousPage(){this.hasPreviousPage()&&this._navigate(this.pageIndex-1)}firstPage(){this.hasPreviousPage()&&this._navigate(0)}lastPage(){this.hasNextPage()&&this._navigate(this.getNumberOfPages()-1)}hasPreviousPage(){return this.pageIndex>=1&&this.pageSize!=0}hasNextPage(){let e=this.getNumberOfPages()-1;return this.pageIndex<e&&this.pageSize!=0}getNumberOfPages(){return this.pageSize?Math.ceil(this.length/this.pageSize):0}_changePageSize(e){let i=this.pageIndex*this.pageSize,a=this.pageIndex;this.pageIndex=Math.floor(i/e)||0,this.pageSize=e,this._emitPageEvent(a)}_nextButtonsDisabled(){return this.disabled||!this.hasNextPage()}_previousButtonsDisabled(){return this.disabled||!this.hasPreviousPage()}_updateDisplayedPageSizeOptions(){this._isInitialized&&(this.pageSize||(this._pageSize=this.pageSizeOptions.length!=0?this.pageSizeOptions[0]:rO),this._displayedPageSizeOptions=this.pageSizeOptions.slice(),this._displayedPageSizeOptions.indexOf(this.pageSize)===-1&&this._displayedPageSizeOptions.push(this.pageSize),this._displayedPageSizeOptions.sort((e,i)=>e-i),this._changeDetectorRef.markForCheck())}_emitPageEvent(e){this.page.emit({previousPageIndex:e,pageIndex:this.pageIndex,pageSize:this.pageSize,length:this.length})}_navigate(e){let i=this.pageIndex;e!==i&&(this.pageIndex=e,this._emitPageEvent(i))}_buttonClicked(e,i){i||this._navigate(e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(l,h){if(l&1&&(c(0,`mat-option`,17),u(1),d()),l&2){let p=h.$implicit;S(`value`,p),m(),B(` `,p,` `)}}function i(l,h){if(l&1){let p=$();c(0,`mat-form-field`,14)(1,`mat-select`,16,0),C(`selectionChange`,function(_){M(p);return T(v(2)._changePageSize(_.value))}),ue(3,e,2,2,`mat-option`,17,Pn),d(),c(5,`div`,18),C(`click`,function(){M(p);return T(ft(2).open())}),d()()}if(l&2){let p=v(2);S(`appearance`,p._formFieldAppearance)(`color`,p.color),m(),S(`value`,p.pageSize)(`disabled`,p.disabled),kr(`aria-labelledby`,p._pageSizeLabelId),S(`panelClass`,p.selectConfig.panelClass||``)(`disableOptionCentering`,p.selectConfig.disableOptionCentering),m(2),me(p._displayedPageSizeOptions)}}function a(l,h){if(l&1&&(c(0,`div`,15),u(1),d()),l&2){let p=v(2);m(),w(p.pageSize)}}function o(l,h){if(l&1&&(c(0,`div`,3)(1,`div`,13),u(2),d(),E(3,i,6,7,`mat-form-field`,14),E(4,a,2,1,`div`,15),d()),l&2){let p=v();m(),ne(`id`,p._pageSizeLabelId),m(),B(` `,p._intl.itemsPerPageLabel,` `),m(),D(p._displayedPageSizeOptions.length>1?3:-1),m(),D(p._displayedPageSizeOptions.length<=1?4:-1)}}function r(l,h){if(l&1){let p=$();c(0,`button`,19),C(`click`,function(){M(p);let _=v();return T(_._buttonClicked(0,_._previousButtonsDisabled()))}),Gt(),c(1,`svg`,8),U(2,`path`,20),d()()}if(l&2){let p=v();S(`matTooltip`,p._intl.firstPageLabel)(`matTooltipDisabled`,p._previousButtonsDisabled())(`disabled`,p._previousButtonsDisabled())(`tabindex`,p._previousButtonsDisabled()?-1:null),ne(`aria-label`,p._intl.firstPageLabel)}}function s(l,h){if(l&1){let p=$();c(0,`button`,21),C(`click`,function(){M(p);let _=v();return T(_._buttonClicked(_.getNumberOfPages()-1,_._nextButtonsDisabled()))}),Gt(),c(1,`svg`,8),U(2,`path`,22),d()()}if(l&2){let p=v();S(`matTooltip`,p._intl.lastPageLabel)(`matTooltipDisabled`,p._nextButtonsDisabled())(`disabled`,p._nextButtonsDisabled())(`tabindex`,p._nextButtonsDisabled()?-1:null),ne(`aria-label`,p._intl.lastPageLabel)}}return H({type:t,selectors:[[`mat-paginator`]],hostAttrs:[`role`,`group`,1,`mat-mdc-paginator`],inputs:{color:`color`,pageIndex:[2,`pageIndex`,`pageIndex`,ct],length:[2,`length`,`length`,ct],pageSize:[2,`pageSize`,`pageSize`,ct],pageSizeOptions:`pageSizeOptions`,hidePageSize:[2,`hidePageSize`,`hidePageSize`,z],showFirstLastButtons:[2,`showFirstLastButtons`,`showFirstLastButtons`,z],selectConfig:`selectConfig`,disabled:[2,`disabled`,`disabled`,z]},outputs:{page:`page`},exportAs:[`matPaginator`],decls:14,vars:14,consts:[[`selectRef`,``],[1,`mat-mdc-paginator-outer-container`],[1,`mat-mdc-paginator-container`],[1,`mat-mdc-paginator-page-size`],[1,`mat-mdc-paginator-range-actions`],[`aria-atomic`,`true`,`aria-live`,`polite`,`role`,`status`,1,`mat-mdc-paginator-range-label`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-first`,3,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-previous`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`viewBox`,`0 0 24 24`,`focusable`,`false`,`aria-hidden`,`true`,1,`mat-mdc-paginator-icon`],[`d`,`M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-next`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-last`,3,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`aria-hidden`,`true`,1,`mat-mdc-paginator-page-size-label`],[1,`mat-mdc-paginator-page-size-select`,3,`appearance`,`color`],[1,`mat-mdc-paginator-page-size-value`],[`hideSingleSelectionIndicator`,``,3,`selectionChange`,`value`,`disabled`,`aria-labelledby`,`panelClass`,`disableOptionCentering`],[3,`value`],[1,`mat-mdc-paginator-touch-target`,3,`click`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-first`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M18.41 16.59L13.82 12l4.59-4.59L17 6l-6 6 6 6zM6 6h2v12H6z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-last`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M5.59 7.41L10.18 12l-4.59 4.59L7 18l6-6-6-6zM16 6h2v12h-2z`]],template:function(h,p){h&1&&(c(0,`div`,1)(1,`div`,2),E(2,o,5,4,`div`,3),c(3,`div`,4)(4,`div`,5),u(5),d(),E(6,r,3,5,`button`,6),c(7,`button`,7),C(`click`,function(){return p._buttonClicked(p.pageIndex-1,p._previousButtonsDisabled())}),Gt(),c(8,`svg`,8),U(9,`path`,9),d()(),_a(),c(10,`button`,10),C(`click`,function(){return p._buttonClicked(p.pageIndex+1,p._nextButtonsDisabled())}),Gt(),c(11,`svg`,8),U(12,`path`,11),d()(),E(13,s,3,5,`button`,12),d()()()),h&2&&(m(2),D(p.hidePageSize?-1:2),m(3),B(` `,p._intl.getRangeLabel(p.pageIndex,p.pageSize,p.length),` `),m(),D(p.showFirstLastButtons?6:-1),m(),S(`matTooltip`,p._intl.previousPageLabel)(`matTooltipDisabled`,p._previousButtonsDisabled())(`disabled`,p._previousButtonsDisabled())(`tabindex`,p._previousButtonsDisabled()?-1:null),ne(`aria-label`,p._intl.previousPageLabel),m(3),S(`matTooltip`,p._intl.nextPageLabel)(`matTooltipDisabled`,p._nextButtonsDisabled())(`disabled`,p._nextButtonsDisabled())(`tabindex`,p._nextButtonsDisabled()?-1:null),ne(`aria-label`,p._intl.nextPageLabel),m(3),D(p.showFirstLastButtons?13:-1))},dependencies:[Ti,Oa,Un,Kg,kw],styles:[`.mat-mdc-paginator {
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
`],encapsulation:2})})()}return t})();var fm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[Ft,Pa,Mw,Ql]})}return t})();var lO=new N(`MAT_PROGRESS_BAR_DEFAULT_OPTIONS`);var ls=(()=>{class t{_elementRef=f(Y);_ngZone=f(Z);_changeDetectorRef=f(Fe);_renderer=f(Oe);_cleanupTransitionEnd;constructor(){let e=Tl(),i=f(lO,{optional:!0});this._isNoopAnimation=e===`di-disabled`,e===`reduced-motion`&&this._elementRef.nativeElement.classList.add(`mat-progress-bar-reduced-motion`),i&&(i.color&&(this.color=this._defaultColor=i.color),this.mode=i.mode||this.mode)}_isNoopAnimation;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor=`primary`;get value(){return this._value}set value(e){this._value=Nw(e||0),this._changeDetectorRef.markForCheck()}_value=0;get bufferValue(){return this._bufferValue||0}set bufferValue(e){this._bufferValue=Nw(e||0),this._changeDetectorRef.markForCheck()}_bufferValue=0;animationEnd=new te;get mode(){return this._mode}set mode(e){this._mode=e,this._changeDetectorRef.markForCheck()}_mode=`determinate`;ngAfterViewInit(){this._ngZone.runOutsideAngular(()=>{this._cleanupTransitionEnd=this._renderer.listen(this._elementRef.nativeElement,`transitionend`,this._transitionendHandler)})}ngOnDestroy(){this._cleanupTransitionEnd?.()}_getPrimaryBarTransform(){return`scaleX(${this._isIndeterminate()?1:this.value/100})`}_getBufferBarFlexBasis(){return`${this.mode===`buffer`?this.bufferValue:100}%`}_isIndeterminate(){return this.mode===`indeterminate`||this.mode===`query`}_transitionendHandler=e=>{this.animationEnd.observers.length===0||!e.target||!e.target.classList.contains(`mdc-linear-progress__primary-bar`)||(this.mode===`determinate`||this.mode===`buffer`)&&this._ngZone.run(()=>this.animationEnd.next({value:this.value}))};static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(i,a){i&1&&Wt(0,`div`,2)}return H({type:t,selectors:[[`mat-progress-bar`]],hostAttrs:[`role`,`progressbar`,`aria-valuemin`,`0`,`aria-valuemax`,`100`,`tabindex`,`-1`,1,`mat-mdc-progress-bar`,`mdc-linear-progress`],hostVars:10,hostBindings:function(a,o){a&2&&(ne(`aria-valuenow`,o._isIndeterminate()?null:o.value)(`mode`,o.mode),tt(`mat-`+o.color),G(`_mat-animation-noopable`,o._isNoopAnimation)(`mdc-linear-progress--animation-ready`,!o._isNoopAnimation)(`mdc-linear-progress--indeterminate`,o._isIndeterminate()))},inputs:{color:`color`,value:[2,`value`,`value`,ct],bufferValue:[2,`bufferValue`,`bufferValue`,ct],mode:`mode`},outputs:{animationEnd:`animationEnd`},exportAs:[`matProgressBar`],decls:7,vars:5,consts:[[`aria-hidden`,`true`,1,`mdc-linear-progress__buffer`],[1,`mdc-linear-progress__buffer-bar`],[1,`mdc-linear-progress__buffer-dots`],[`aria-hidden`,`true`,1,`mdc-linear-progress__bar`,`mdc-linear-progress__primary-bar`],[1,`mdc-linear-progress__bar-inner`],[`aria-hidden`,`true`,1,`mdc-linear-progress__bar`,`mdc-linear-progress__secondary-bar`]],template:function(a,o){a&1&&(lt(0,`div`,0),Wt(1,`div`,1),E(2,e,1,0,`div`,2),ht(),lt(3,`div`,3),Wt(4,`span`,4),ht(),lt(5,`div`,5),Wt(6,`span`,4),ht()),a&2&&(m(),Zt(`flex-basis`,o._getBufferBarFlexBasis()),m(),D(o.mode===`buffer`?2:-1),m(),Zt(`transform`,o._getPrimaryBarTransform()))},styles:[`.mat-mdc-progress-bar {
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
`],encapsulation:2})})()}return t})();function Nw(t,n=0,e=100){return Math.max(n,Math.min(e,t))}var cs=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[we]})}return t})();var cO=new N(`MatTabContent`);var Xl=(()=>{class t{template=f(Vt);static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`matTabContent`,``]],features:[Ge([{provide:cO,useExisting:t}])]})}return t})();var dO=new N(`MatTabLabel`);var Pw=new N(`MAT_TAB`);var uO=(()=>{class t extends sw{_closestTab=f(Pw,{optional:!0});static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`mat-tab-label`,``],[``,`matTabLabel`,``]],features:[Ge([{provide:dO,useExisting:t}]),Ve]})}return t})();var Fw=new N(`MAT_TAB_GROUP`);var Zl=(()=>{class t{_viewContainerRef=f(Bt);_closestTabGroup=f(Fw,{optional:!0});disabled=!1;get templateLabel(){return this._templateLabel}set templateLabel(e){this._setTemplateLabelInput(e)}_templateLabel;_explicitContent=void 0;_implicitContent;textLabel=``;ariaLabel;ariaLabelledby;labelClass;bodyClass;id=null;_contentPortal=null;get content(){return this._contentPortal}_stateChanges=new V;position=null;origin=null;isActive=!1;constructor(){f(Je).load(mn)}ngOnChanges(e){(Object.hasOwn(e,`textLabel`)||Object.hasOwn(e,`disabled`))&&this._stateChanges.next()}ngOnDestroy(){this._stateChanges.complete()}ngOnInit(){this._contentPortal=new Hn(this._explicitContent||this._implicitContent,this._viewContainerRef)}_setTemplateLabelInput(e){e&&e._closestTab===this&&(this._templateLabel=e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`];function i(a,o){a&1&&he(0)}return H({type:t,selectors:[[`mat-tab`]],contentQueries:function(o,r,s){if(o&1&&jt(s,uO,5)(s,Xl,7,Vt),o&2){let l;J(l=ee())&&(r.templateLabel=l.first),J(l=ee())&&(r._explicitContent=l.first)}},viewQuery:function(o,r){if(o&1&&Ze(Vt,7),o&2){let s;J(s=ee())&&(r._implicitContent=s.first)}},hostAttrs:[`hidden`,``],hostVars:1,hostBindings:function(o,r){o&2&&ne(`id`,null)},inputs:{disabled:[2,`disabled`,`disabled`,z],textLabel:[0,`label`,`textLabel`],ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],labelClass:`labelClass`,bodyClass:`bodyClass`,id:`id`},exportAs:[`matTab`],features:[Ge([{provide:Pw,useExisting:t}]),_t],ngContentSelectors:e,decls:1,vars:0,template:function(o,r){o&1&&(Pe(),Ir(0,i,1,0,`ng-template`))},encapsulation:2,changeDetection:1})})()}return t})();var Mb=`mdc-tab-indicator--active`;var Aw=`mdc-tab-indicator--no-transition`;var Nb=class{_items;_currentItem;constructor(n){this._items=n}hide(){this._items.forEach(n=>n.deactivateInkBar()),this._currentItem=void 0}alignToElement(n){let e=this._items.find(a=>a.elementRef.nativeElement===n),i=this._currentItem;if(e!==i&&(i?.deactivateInkBar(),e)){let a=i?.elementRef.nativeElement.getBoundingClientRect?.();e.activateInkBar(a),this._currentItem=e}}};var mO=(()=>{class t{_elementRef=f(Y);_inkBarElement=null;_inkBarContentElement=null;_fitToContent=!1;get fitInkBarToContent(){return this._fitToContent}set fitInkBarToContent(e){this._fitToContent!==e&&(this._fitToContent=e,this._inkBarElement&&this._appendInkBarElement())}activateInkBar(e){let i=this._elementRef.nativeElement;if(!e||!i.getBoundingClientRect||!this._inkBarContentElement){i.classList.add(Mb);return}let a=i.getBoundingClientRect(),o=e.width/a.width,r=e.left-a.left;i.classList.add(Aw),this._inkBarContentElement.style.setProperty(`transform`,`translateX(${r}px) scaleX(${o})`),i.getBoundingClientRect(),i.classList.remove(Aw),i.classList.add(Mb),this._inkBarContentElement.style.setProperty(`transform`,``)}deactivateInkBar(){this._elementRef.nativeElement.classList.remove(Mb)}ngOnInit(){this._createInkBarElement()}ngOnDestroy(){this._inkBarElement?.remove(),this._inkBarElement=this._inkBarContentElement=null}_createInkBarElement(){let e=this._elementRef.nativeElement.ownerDocument||document,i=this._inkBarElement=e.createElement(`span`),a=this._inkBarContentElement=e.createElement(`span`);i.className=`mdc-tab-indicator`,a.className=`mdc-tab-indicator__content mdc-tab-indicator__content--underline`,i.appendChild(this._inkBarContentElement),this._appendInkBarElement()}_appendInkBarElement(){this._inkBarElement;(this._fitToContent?this._elementRef.nativeElement.querySelector(`.mdc-tab__content`):this._elementRef.nativeElement).appendChild(this._inkBarElement)}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,inputs:{fitInkBarToContent:[2,`fitInkBarToContent`,`fitInkBarToContent`,z]}})}return t})();var Lw=(()=>{class t extends mO{elementRef=f(Y);disabled=!1;focus(){this.elementRef.nativeElement.focus()}getOffsetLeft(){return this.elementRef.nativeElement.offsetLeft}getOffsetWidth(){return this.elementRef.nativeElement.offsetWidth}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`matTabLabelWrapper`,``]],hostVars:3,hostBindings:function(i,a){i&2&&(ne(`aria-disabled`,!!a.disabled),G(`mat-mdc-tab-disabled`,a.disabled))},inputs:{disabled:[2,`disabled`,`disabled`,z]},features:[Ve]})}return t})();var Rw={passive:!0};var pO=650;var hO=100;function Tb(t){let n=t+``;return/^[0-9]+(?:\.[0-9]+)?$/.test(n)?`${t}ms`:/^[0-9]+(?:\.[0-9]+)?(?:ms|s)$/.test(n)?n:``}var fO=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Fe);_viewportRuler=f(ui);_dir=f(gt,{optional:!0});_ngZone=f(Z);_platform=f(Re);_sharedResizeObserver=f(em);_injector=f(se);_renderer=f(Oe);_animationsDisabled=Ue();_eventCleanups;_scrollDistance=0;_selectedIndexChanged=!1;_destroyed=new V;_showPaginationControls=!1;_disableScrollAfter=!0;_disableScrollBefore=!0;_tabLabelCount;_scrollDistanceChanged=!1;_keyManager;_currentTextContent;_stopScrolling=new V;disablePagination=!1;get selectedIndex(){return this._selectedIndex}set selectedIndex(e){let i=isNaN(e)?0:e;this._selectedIndex!=i&&(this._selectedIndexChanged=!0,this._selectedIndex=i,this._keyManager&&this._keyManager.updateActiveItem(i))}_selectedIndex=0;selectFocusedIndex=new te;indexFocused=new te;constructor(){this._eventCleanups=this._ngZone.runOutsideAngular(()=>[this._renderer.listen(this._elementRef.nativeElement,`mouseleave`,()=>this._stopInterval())])}ngAfterViewInit(){this._eventCleanups.push(this._renderer.listen(this._previousPaginator.nativeElement,`touchstart`,()=>this._handlePaginatorPress(`before`),Rw),this._renderer.listen(this._nextPaginator.nativeElement,`touchstart`,()=>this._handlePaginatorPress(`after`),Rw))}ngAfterContentInit(){let e=this._dir?this._dir.change:Mn(`ltr`),i=this._sharedResizeObserver.observe(this._elementRef.nativeElement).pipe(Pi(32),Ne(this._destroyed)),a=this._viewportRuler.change(150).pipe(Ne(this._destroyed)),o=()=>{this.updatePagination(),this._alignInkBarToSelectedTab()};this._keyManager=new ea(this._items).withHorizontalOrientation(this._getLayoutDirection()).withHomeAndEnd().withWrap().skipPredicate(()=>!1),this._keyManager.updateActiveItem(Math.max(this._selectedIndex,0)),Rt(o,{injector:this._injector}),kt(e,a,i,this._items.changes,this._itemsResized()).pipe(Ne(this._destroyed)).subscribe(()=>{this._ngZone.run(()=>{Promise.resolve().then(()=>{this._scrollDistance=Math.max(0,Math.min(this._getMaxScrollDistance(),this._scrollDistance)),o()})}),this._keyManager?.withHorizontalOrientation(this._getLayoutDirection())}),this._keyManager.change.subscribe(r=>{this.indexFocused.emit(r),this._setTabFocus(r)})}_itemsResized(){return typeof ResizeObserver!=`function`?Oi:this._items.changes.pipe(mt(this._items),_n(e=>new Ce(i=>this._ngZone.runOutsideAngular(()=>{let a=new ResizeObserver(o=>i.next(o));return e.forEach(o=>a.observe(o.elementRef.nativeElement)),()=>{a.disconnect()}}))),Qa(1),Qe(e=>e.some(i=>i.contentRect.width>0&&i.contentRect.height>0)))}ngAfterContentChecked(){this._tabLabelCount!=this._items.length&&(this.updatePagination(),this._tabLabelCount=this._items.length,this._changeDetectorRef.markForCheck()),this._selectedIndexChanged&&(this._scrollToLabel(this._selectedIndex),this._checkScrollingControls(),this._alignInkBarToSelectedTab(),this._selectedIndexChanged=!1,this._changeDetectorRef.markForCheck()),this._scrollDistanceChanged&&(this._updateTabScrollPosition(),this._scrollDistanceChanged=!1,this._changeDetectorRef.markForCheck())}ngOnDestroy(){this._eventCleanups.forEach(e=>e()),this._keyManager?.destroy(),this._destroyed.next(),this._destroyed.complete(),this._stopScrolling.complete()}_handleKeydown(e){if(!Ct(e))switch(e.keyCode){case 13:case 32:if(this.focusIndex!==this.selectedIndex){let i=this._items.get(this.focusIndex);i&&!i.disabled&&(this.selectFocusedIndex.emit(this.focusIndex),this._itemSelected(e))}break;default:this._keyManager?.onKeydown(e)}}_onContentChanges(){let e=this._elementRef.nativeElement.textContent;e!==this._currentTextContent&&(this._currentTextContent=e||``,this._ngZone.run(()=>{this.updatePagination(),this._alignInkBarToSelectedTab(),this._changeDetectorRef.markForCheck()}))}updatePagination(){this._checkPaginationEnabled(),this._checkScrollingControls(),this._updateTabScrollPosition()}get focusIndex(){return this._keyManager?this._keyManager.activeItemIndex:0}set focusIndex(e){!this._isValidIndex(e)||this.focusIndex===e||!this._keyManager||this._keyManager.setActiveItem(e)}_isValidIndex(e){return this._items?!!this._items.toArray()[e]:!0}_setTabFocus(e){if(this._showPaginationControls&&this._scrollToLabel(e),this._items&&this._items.length){this._items.toArray()[e].focus();let i=this._tabListContainer.nativeElement;this._getLayoutDirection()==`ltr`?i.scrollLeft=0:i.scrollLeft=i.scrollWidth-i.offsetWidth}}_getLayoutDirection(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_updateTabScrollPosition(){if(this.disablePagination)return;let e=this.scrollDistance,i=this._getLayoutDirection()===`ltr`?-e:e;this._tabList.nativeElement.style.transform=`translateX(${Math.round(i)}px)`,(this._platform.TRIDENT||this._platform.EDGE)&&(this._tabListContainer.nativeElement.scrollLeft=0)}get scrollDistance(){return this._scrollDistance}set scrollDistance(e){this._scrollTo(e)}_scrollHeader(e){let i=this._tabListContainer.nativeElement.offsetWidth,a=(e==`before`?-1:1)*i/3;return this._scrollTo(this._scrollDistance+a)}_handlePaginatorClick(e){this._stopInterval(),this._scrollHeader(e)}_scrollToLabel(e){if(this.disablePagination)return;let i=this._items?this._items.toArray()[e]:null;if(!i)return;let a=this._tabListContainer.nativeElement.offsetWidth,{offsetLeft:o,offsetWidth:r}=i.elementRef.nativeElement,s,l;this._getLayoutDirection()==`ltr`?(s=o,l=s+r):(l=this._tabListInner.nativeElement.offsetWidth-o,s=l-r);let h=this.scrollDistance,p=this.scrollDistance+a;s<h?this.scrollDistance-=h-s:l>p&&(this.scrollDistance+=Math.min(l-p,s-h))}_checkPaginationEnabled(){if(this.disablePagination)this._showPaginationControls=!1;else{let a=this._tabListInner.nativeElement.scrollWidth-this._elementRef.nativeElement.offsetWidth>=5;a||(this.scrollDistance=0),a!==this._showPaginationControls&&(this._showPaginationControls=a,this._changeDetectorRef.markForCheck())}}_checkScrollingControls(){this.disablePagination?this._disableScrollAfter=this._disableScrollBefore=!0:(this._disableScrollBefore=this.scrollDistance==0,this._disableScrollAfter=this.scrollDistance==this._getMaxScrollDistance(),this._changeDetectorRef.markForCheck())}_getMaxScrollDistance(){return this._tabListInner.nativeElement.scrollWidth-this._tabListContainer.nativeElement.offsetWidth||0}_alignInkBarToSelectedTab(){let e=this._items&&this._items.length?this._items.toArray()[this.selectedIndex]:null,i=e?e.elementRef.nativeElement:null;i?this._inkBar.alignToElement(i):this._inkBar.hide()}_stopInterval(){this._stopScrolling.next()}_handlePaginatorPress(e,i){i&&i.button!=null&&i.button!==0||(this._stopInterval(),xs(pO,hO).pipe(Ne(kt(this._stopScrolling,this._destroyed))).subscribe(()=>{let{maxScrollDistance:a,distance:o}=this._scrollHeader(e);(o===0||o>=a)&&this._stopInterval()}))}_scrollTo(e){if(this.disablePagination)return{maxScrollDistance:0,distance:0};let i=this._getMaxScrollDistance();return this._scrollDistance=Math.max(0,Math.min(i,e)),this._scrollDistanceChanged=!0,this._checkScrollingControls(),{maxScrollDistance:i,distance:this._scrollDistance}}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,inputs:{disablePagination:[2,`disablePagination`,`disablePagination`,z],selectedIndex:[2,`selectedIndex`,`selectedIndex`,ct]},outputs:{selectFocusedIndex:`selectFocusedIndex`,indexFocused:`indexFocused`}})}return t})();var gO=(()=>{class t extends fO{_items;_tabListContainer;_tabList;_tabListInner;_nextPaginator;_previousPaginator;_inkBar;ariaLabel;ariaLabelledby;disableRipple=!1;ngAfterContentInit(){this._inkBar=new Nb(this._items),super.ngAfterContentInit()}_itemSelected(e){e.preventDefault()}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵcmp=(function(){let e=[`tabListContainer`],i=[`tabList`],a=[`tabListInner`],o=[`nextPaginator`],r=[`previousPaginator`];return H({type:t,selectors:[[`mat-tab-header`]],contentQueries:function(h,p,g){if(h&1&&jt(g,Lw,4),h&2){let _;J(_=ee())&&(p._items=_)}},viewQuery:function(h,p){if(h&1&&Ze(e,7)(i,7)(a,7)(o,5)(r,5),h&2){let g;J(g=ee())&&(p._tabListContainer=g.first),J(g=ee())&&(p._tabList=g.first),J(g=ee())&&(p._tabListInner=g.first),J(g=ee())&&(p._nextPaginator=g.first),J(g=ee())&&(p._previousPaginator=g.first)}},hostAttrs:[1,`mat-mdc-tab-header`],hostVars:4,hostBindings:function(h,p){h&2&&G(`mat-mdc-tab-header-pagination-controls-enabled`,p._showPaginationControls)(`mat-mdc-tab-header-rtl`,p._getLayoutDirection()==`rtl`)},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],disableRipple:[2,`disableRipple`,`disableRipple`,z]},features:[Ve],ngContentSelectors:[`*`],decls:13,vars:10,consts:[[`previousPaginator`,``],[`tabListContainer`,``],[`tabList`,``],[`tabListInner`,``],[`nextPaginator`,``],[`mat-ripple`,``,1,`mat-mdc-tab-header-pagination`,`mat-mdc-tab-header-pagination-before`,3,`click`,`mousedown`,`touchend`,`matRippleDisabled`],[1,`mat-mdc-tab-header-pagination-chevron`],[1,`mat-mdc-tab-label-container`,3,`keydown`],[`role`,`tablist`,1,`mat-mdc-tab-list`,3,`cdkObserveContent`],[1,`mat-mdc-tab-labels`],[`mat-ripple`,``,1,`mat-mdc-tab-header-pagination`,`mat-mdc-tab-header-pagination-after`,3,`mousedown`,`click`,`touchend`,`matRippleDisabled`]],template:function(h,p){h&1&&(Pe(),c(0,`div`,5,0),C(`click`,function(){return p._handlePaginatorClick(`before`)})(`mousedown`,function(_){return p._handlePaginatorPress(`before`,_)})(`touchend`,function(){return p._stopInterval()}),U(2,`div`,6),d(),c(3,`div`,7,1),C(`keydown`,function(_){return p._handleKeydown(_)}),c(5,`div`,8,2),C(`cdkObserveContent`,function(){return p._onContentChanges()}),c(7,`div`,9,3),he(9),d()()(),c(10,`div`,10,4),C(`mousedown`,function(_){return p._handlePaginatorPress(`after`,_)})(`click`,function(){return p._handlePaginatorClick(`after`)})(`touchend`,function(){return p._stopInterval()}),U(12,`div`,6),d()),h&2&&(G(`mat-mdc-tab-header-pagination-disabled`,p._disableScrollBefore),S(`matRippleDisabled`,p._disableScrollBefore||p.disableRipple),m(3),G(`_mat-animation-noopable`,p._animationsDisabled),m(2),ne(`aria-label`,p.ariaLabel||null)(`aria-labelledby`,p.ariaLabelledby||null),m(5),G(`mat-mdc-tab-header-pagination-disabled`,p._disableScrollAfter),S(`matRippleDisabled`,p._disableScrollAfter||p.disableRipple))},dependencies:[li,VC],styles:[`.mat-mdc-tab-header {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var bO=new N(`MAT_TABS_CONFIG`);var Ow=(()=>{class t extends Aa{_host=f(Ab);_ngZone=f(Z);_centeringSub=De.EMPTY;_leavingSub=De.EMPTY;ngOnInit(){super.ngOnInit(),this._centeringSub=this._host._beforeCentering.pipe(mt(this._host._isCenterPosition())).subscribe(e=>{this._host._content&&e&&!this.hasAttached()&&this._ngZone.run(()=>{Promise.resolve().then(),this.attach(this._host._content)})}),this._leavingSub=this._host._afterLeavingCenter.subscribe(()=>{this._host.preserveContent||this._ngZone.run(()=>this.detach())})}ngOnDestroy(){super.ngOnDestroy(),this._centeringSub.unsubscribe(),this._leavingSub.unsubscribe()}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`matTabBodyHost`,``]],features:[Ve]})}return t})();var Ab=(()=>{class t{_elementRef=f(Y);_dir=f(gt,{optional:!0});_ngZone=f(Z);_injector=f(se);_renderer=f(Oe);_diAnimationsDisabled=Ue();_eventCleanups;_initialized=!1;_fallbackTimer;_positionIndex;_dirChangeSubscription=De.EMPTY;_position;_previousPosition;_onCentering=new te;_beforeCentering=new te;_afterLeavingCenter=new te;_onCentered=new te(!0);_portalHost;_contentElement;_content;animationDuration=`500ms`;preserveContent=!1;set position(e){this._positionIndex=e,this._computePositionAnimationState()}constructor(){if(this._dir){let e=f(Fe);this._dirChangeSubscription=this._dir.change.subscribe(i=>{this._computePositionAnimationState(i),e.markForCheck()})}}ngOnInit(){this._bindTransitionEvents(),this._position===`center`&&(this._setActiveClass(!0),Rt(()=>this._onCentering.emit(this._elementRef.nativeElement.clientHeight),{injector:this._injector})),this._initialized=!0}ngOnDestroy(){clearTimeout(this._fallbackTimer),this._eventCleanups?.forEach(e=>e()),this._dirChangeSubscription.unsubscribe()}_bindTransitionEvents(){this._ngZone.runOutsideAngular(()=>{let e=this._elementRef.nativeElement,i=a=>{a.target===this._contentElement?.nativeElement&&(this._elementRef.nativeElement.classList.remove(`mat-tab-body-animating`),a.type===`transitionend`&&this._transitionDone())};this._eventCleanups=[this._renderer.listen(e,`transitionstart`,a=>{a.target===this._contentElement?.nativeElement&&(this._elementRef.nativeElement.classList.add(`mat-tab-body-animating`),this._transitionStarted())}),this._renderer.listen(e,`transitionend`,i),this._renderer.listen(e,`transitioncancel`,i)]})}_transitionStarted(){clearTimeout(this._fallbackTimer);let e=this._position===`center`;this._beforeCentering.emit(e),e&&this._onCentering.emit(this._elementRef.nativeElement.clientHeight)}_transitionDone(){this._position===`center`?this._onCentered.emit():this._previousPosition===`center`&&this._afterLeavingCenter.emit()}_setActiveClass(e){this._elementRef.nativeElement.classList.toggle(`mat-mdc-tab-body-active`,e)}_getLayoutDirection(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_isCenterPosition(){return this._positionIndex===0}_computePositionAnimationState(e=this._getLayoutDirection()){this._previousPosition=this._position,this._positionIndex<0?this._position=e==`ltr`?`left`:`right`:this._positionIndex>0?this._position=e==`ltr`?`right`:`left`:this._position=`center`,this._animationsDisabled()?this._simulateTransitionEvents():this._initialized&&(this._position===`center`||this._previousPosition===`center`)&&(clearTimeout(this._fallbackTimer),this._fallbackTimer=this._ngZone.runOutsideAngular(()=>setTimeout(()=>this._simulateTransitionEvents(),100)))}_simulateTransitionEvents(){this._transitionStarted(),Rt(()=>this._transitionDone(),{injector:this._injector})}_animationsDisabled(){return this._diAnimationsDisabled||this.animationDuration===`0ms`||this.animationDuration===`0s`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`content`];function i(a,o){}return H({type:t,selectors:[[`mat-tab-body`]],viewQuery:function(o,r){if(o&1&&Ze(Ow,5)(e,5),o&2){let s;J(s=ee())&&(r._portalHost=s.first),J(s=ee())&&(r._contentElement=s.first)}},hostAttrs:[1,`mat-mdc-tab-body`],hostVars:1,hostBindings:function(o,r){o&2&&ne(`inert`,r._position===`center`?null:``)},inputs:{_content:[0,`content`,`_content`],animationDuration:`animationDuration`,preserveContent:`preserveContent`,position:`position`},outputs:{_onCentering:`_onCentering`,_beforeCentering:`_beforeCentering`,_onCentered:`_onCentered`},decls:3,vars:6,consts:[[`content`,``],[`cdkScrollable`,``,1,`mat-mdc-tab-body-content`],[`matTabBodyHost`,``]],template:function(o,r){o&1&&(c(0,`div`,1,0),rt(2,i,0,0,`ng-template`,2),d()),o&2&&G(`mat-tab-body-content-left`,r._position===`left`)(`mat-tab-body-content-right`,r._position===`right`)(`mat-tab-body-content-can-animate`,r._position===`center`||r._previousPosition===`center`)},dependencies:[Ow,ql],styles:[`.mat-mdc-tab-body {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var gm=(()=>{class t{_elementRef=f(Y);_changeDetectorRef=f(Fe);_ngZone=f(Z);_tabsSubscription=De.EMPTY;_tabLabelSubscription=De.EMPTY;_tabBodySubscription=De.EMPTY;_diAnimationsDisabled=Ue();_bodyAnimationDuration;_headerAnimationDuration;_allTabs;_tabBodies;_tabBodyWrapper;_tabHeader;_tabs=new wn;_indexToSelect=0;_lastFocusedTabIndex=null;_tabBodyWrapperHeight=0;color;get fitInkBarToContent(){return this._fitInkBarToContent}set fitInkBarToContent(e){this._fitInkBarToContent=e,this._changeDetectorRef.markForCheck()}_fitInkBarToContent=!1;stretchTabs=!0;alignTabs=null;dynamicHeight=!1;get selectedIndex(){return this._selectedIndex}set selectedIndex(e){this._indexToSelect=isNaN(e)?null:e}_selectedIndex=null;headerPosition=`above`;get animationDuration(){return this._animationDuration}set animationDuration(e){this._animationDuration=e,e&&typeof e==`object`?(this._bodyAnimationDuration=Tb(e.body),this._headerAnimationDuration=Tb(e.header)):this._headerAnimationDuration=this._bodyAnimationDuration=Tb(e)}_animationDuration;get contentTabIndex(){return this._contentTabIndex}set contentTabIndex(e){this._contentTabIndex=isNaN(e)?null:e}_contentTabIndex=null;disablePagination=!1;disableRipple=!1;preserveContent=!1;get backgroundColor(){return this._backgroundColor}set backgroundColor(e){let i=this._elementRef.nativeElement.classList;i.remove(`mat-tabs-with-background`,`mat-background-${this.backgroundColor}`),e&&i.add(`mat-tabs-with-background`,`mat-background-${e}`),this._backgroundColor=e}_backgroundColor;ariaLabel;ariaLabelledby;selectedIndexChange=new te;focusChange=new te;animationDone=new te;selectedTabChange=new te(!0);_groupId;_isServer=!f(Re).isBrowser;constructor(){let e=f(bO,{optional:!0});this._groupId=f(Be).getId(`mat-tab-group-`),this.animationDuration=e&&e.animationDuration?e.animationDuration:`500ms`,this.disablePagination=e&&e.disablePagination!=null?e.disablePagination:!1,this.dynamicHeight=e&&e.dynamicHeight!=null?e.dynamicHeight:!1,e?.contentTabIndex!=null&&(this.contentTabIndex=e.contentTabIndex),this.preserveContent=!!e?.preserveContent,this.fitInkBarToContent=e&&e.fitInkBarToContent!=null?e.fitInkBarToContent:!1,this.stretchTabs=e&&e.stretchTabs!=null?e.stretchTabs:!0,this.alignTabs=e&&e.alignTabs!=null?e.alignTabs:null}ngAfterContentChecked(){let e=this._indexToSelect=this._clampTabIndex(this._indexToSelect);if(this._selectedIndex!=e){let i=this._selectedIndex==null;if(!i){this.selectedTabChange.emit(this._createChangeEvent(e));let a=this._tabBodyWrapper.nativeElement;a.style.minHeight=a.clientHeight+`px`}Promise.resolve().then(()=>{this._tabs.forEach((a,o)=>a.isActive=o===e),i||(this.selectedIndexChange.emit(e),this._tabBodyWrapper.nativeElement.style.minHeight=``)})}this._tabs.forEach((i,a)=>{i.position=a-e,this._selectedIndex!=null&&i.position==0&&!i.origin&&(i.origin=e-this._selectedIndex)}),this._selectedIndex!==e&&(this._selectedIndex=e,this._lastFocusedTabIndex=null,this._changeDetectorRef.markForCheck())}ngAfterContentInit(){this._subscribeToAllTabChanges(),this._subscribeToTabLabels(),this._tabsSubscription=this._tabs.changes.subscribe(()=>{let e=this._clampTabIndex(this._indexToSelect);if(e===this._selectedIndex){let i=this._tabs.toArray(),a;for(let o=0;o<i.length;o++)if(i[o].isActive){this._indexToSelect=this._selectedIndex=o,this._lastFocusedTabIndex=null,a=i[o];break}!a&&i[e]&&Promise.resolve().then(()=>{i[e].isActive=!0,this.selectedTabChange.emit(this._createChangeEvent(e))})}this._changeDetectorRef.markForCheck()})}ngAfterViewInit(){this._tabBodySubscription=this._tabBodies.changes.subscribe(()=>this._bodyCentered(!0))}_subscribeToAllTabChanges(){this._allTabs.changes.pipe(mt(this._allTabs)).subscribe(e=>{this._tabs.reset(e.filter(i=>i._closestTabGroup===this||!i._closestTabGroup)),this._tabs.notifyOnChanges()})}ngOnDestroy(){this._tabs.destroy(),this._tabsSubscription.unsubscribe(),this._tabLabelSubscription.unsubscribe(),this._tabBodySubscription.unsubscribe()}realignInkBar(){this._tabHeader&&this._tabHeader._alignInkBarToSelectedTab()}updatePagination(){this._tabHeader&&this._tabHeader.updatePagination()}focusTab(e){let i=this._tabHeader;i&&(i.focusIndex=e)}_focusChanged(e){this._lastFocusedTabIndex=e,this.focusChange.emit(this._createChangeEvent(e))}_createChangeEvent(e){let i=new Rb;return i.index=e,this._tabs&&this._tabs.length&&(i.tab=this._tabs.toArray()[e]),i}_subscribeToTabLabels(){this._tabLabelSubscription&&this._tabLabelSubscription.unsubscribe(),this._tabLabelSubscription=kt(...this._tabs.map(e=>e._stateChanges)).subscribe(()=>this._changeDetectorRef.markForCheck())}_clampTabIndex(e){return Math.min(this._tabs.length-1,Math.max(e||0,0))}_getTabLabelId(e,i){return e.id||`${this._groupId}-label-${i}`}_getTabContentId(e){return`${this._groupId}-content-${e}`}_setTabBodyWrapperHeight(e){if(!this.dynamicHeight||!this._tabBodyWrapperHeight){this._tabBodyWrapperHeight=e;return}let i=this._tabBodyWrapper.nativeElement;i.style.height=this._tabBodyWrapperHeight+`px`,this._tabBodyWrapper.nativeElement.offsetHeight&&(i.style.height=e+`px`)}_removeTabBodyWrapperHeight(){let e=this._tabBodyWrapper.nativeElement;this._tabBodyWrapperHeight=e.clientHeight,e.style.height=``,this._ngZone.run(()=>this.animationDone.emit())}_handleClick(e,i,a){i.focusIndex=a,e.disabled||(this.selectedIndex=a)}_getTabIndex(e){return e===(this._lastFocusedTabIndex??this.selectedIndex)?0:-1}_tabFocusChanged(e,i){e&&e!==`mouse`&&e!==`touch`&&(this._tabHeader.focusIndex=i)}_bodyCentered(e){e&&this._tabBodies?.forEach((i,a)=>i._setActiveClass(a===this._selectedIndex))}_bodyAnimationsDisabled(){return this._diAnimationsDisabled||this._bodyAnimationDuration===`0`||this._bodyAnimationDuration===`0ms`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`tabBodyWrapper`],i=[`tabHeader`],a=[`*`];function o(g,_){}function r(g,_){if(g&1&&rt(0,o,0,0,`ng-template`,12),g&2){let b=v().$implicit;S(`cdkPortalOutlet`,b.templateLabel)}}function s(g,_){if(g&1&&u(0),g&2){let b=v().$implicit;w(b.textLabel)}}function l(g,_){if(g&1){let b=$();c(0,`div`,7,2),C(`click`,function(){let I=M(b),P=I.$implicit,ie=I.$index,L=v(),x=ft(1);return T(L._handleClick(P,x,ie))})(`cdkFocusChange`,function(I){let P=M(b).$index;return T(v()._tabFocusChanged(I,P))}),U(2,`span`,8)(3,`div`,9),c(4,`span`,10)(5,`span`,11),E(6,r,1,1,null,12)(7,s,1,1),d()()()}if(g&2){let b=_.$implicit,y=_.$index,I=ft(1),P=v();tt(b.labelClass),G(`mdc-tab--active`,P.selectedIndex===y),S(`id`,P._getTabLabelId(b,y))(`disabled`,b.disabled)(`fitInkBarToContent`,P.fitInkBarToContent),ne(`tabIndex`,P._getTabIndex(y))(`aria-posinset`,y+1)(`aria-setsize`,P._tabs.length)(`aria-controls`,P._getTabContentId(y))(`aria-selected`,P.selectedIndex===y)(`aria-label`,b.ariaLabel||null)(`aria-labelledby`,!b.ariaLabel&&b.ariaLabelledby?b.ariaLabelledby:null),m(3),S(`matRippleTrigger`,I)(`matRippleDisabled`,b.disabled||P.disableRipple),m(3),D(b.templateLabel?6:7)}}function h(g,_){g&1&&he(0)}function p(g,_){if(g&1){let b=$();c(0,`mat-tab-body`,13),C(`_onCentered`,function(){M(b);return T(v()._removeTabBodyWrapperHeight())})(`_onCentering`,function(I){M(b);return T(v()._setTabBodyWrapperHeight(I))})(`_beforeCentering`,function(I){M(b);return T(v()._bodyCentered(I))}),d()}if(g&2){let b=_.$implicit,y=_.$index,I=v();tt(b.bodyClass),S(`id`,I._getTabContentId(y))(`content`,b.content)(`position`,b.position)(`animationDuration`,I._bodyAnimationDuration)(`preserveContent`,I.preserveContent),ne(`tabindex`,I.contentTabIndex!=null&&I.selectedIndex===y?I.contentTabIndex:null)(`aria-labelledby`,I._getTabLabelId(b,y))(`aria-hidden`,I.selectedIndex!==y)}}return H({type:t,selectors:[[`mat-tab-group`]],contentQueries:function(_,b,y){if(_&1&&jt(y,Zl,5),_&2){let I;J(I=ee())&&(b._allTabs=I)}},viewQuery:function(_,b){if(_&1&&Ze(e,5)(i,5)(Ab,5),_&2){let y;J(y=ee())&&(b._tabBodyWrapper=y.first),J(y=ee())&&(b._tabHeader=y.first),J(y=ee())&&(b._tabBodies=y)}},hostAttrs:[1,`mat-mdc-tab-group`],hostVars:13,hostBindings:function(_,b){_&2&&(ne(`mat-align-tabs`,b.alignTabs),tt(`mat-`+(b.color||`primary`)),Zt(`--%NS%mat-tab-body-animation-duration`,b._bodyAnimationDuration)(`--%NS%mat-tab-header-animation-duration`,b._headerAnimationDuration),G(`mat-mdc-tab-group-dynamic-height`,b.dynamicHeight)(`mat-mdc-tab-group-inverted-header`,b.headerPosition===`below`)(`mat-mdc-tab-group-stretch-tabs`,b.stretchTabs))},inputs:{color:`color`,fitInkBarToContent:[2,`fitInkBarToContent`,`fitInkBarToContent`,z],stretchTabs:[2,`mat-stretch-tabs`,`stretchTabs`,z],alignTabs:[0,`mat-align-tabs`,`alignTabs`],dynamicHeight:[2,`dynamicHeight`,`dynamicHeight`,z],selectedIndex:[2,`selectedIndex`,`selectedIndex`,ct],headerPosition:`headerPosition`,animationDuration:`animationDuration`,contentTabIndex:[2,`contentTabIndex`,`contentTabIndex`,ct],disablePagination:[2,`disablePagination`,`disablePagination`,z],disableRipple:[2,`disableRipple`,`disableRipple`,z],preserveContent:[2,`preserveContent`,`preserveContent`,z],backgroundColor:`backgroundColor`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`]},outputs:{selectedIndexChange:`selectedIndexChange`,focusChange:`focusChange`,animationDone:`animationDone`,selectedTabChange:`selectedTabChange`},exportAs:[`matTabGroup`],features:[Ge([{provide:Fw,useExisting:t}])],ngContentSelectors:a,decls:9,vars:8,consts:[[`tabHeader`,``],[`tabBodyWrapper`,``],[`tabNode`,``],[3,`indexFocused`,`selectFocusedIndex`,`selectedIndex`,`disableRipple`,`disablePagination`,`aria-label`,`aria-labelledby`],[`role`,`tab`,`matTabLabelWrapper`,``,`cdkMonitorElementFocus`,``,1,`mdc-tab`,`mat-mdc-tab`,`mat-focus-indicator`,3,`id`,`mdc-tab--active`,`class`,`disabled`,`fitInkBarToContent`],[1,`mat-mdc-tab-body-wrapper`],[`role`,`tabpanel`,3,`id`,`class`,`content`,`position`,`animationDuration`,`preserveContent`],[`role`,`tab`,`matTabLabelWrapper`,``,`cdkMonitorElementFocus`,``,1,`mdc-tab`,`mat-mdc-tab`,`mat-focus-indicator`,3,`click`,`cdkFocusChange`,`id`,`disabled`,`fitInkBarToContent`],[1,`mdc-tab__ripple`],[`mat-ripple`,``,1,`mat-mdc-tab-ripple`,3,`matRippleTrigger`,`matRippleDisabled`],[1,`mdc-tab__content`],[1,`mdc-tab__text-label`],[3,`cdkPortalOutlet`],[`role`,`tabpanel`,3,`_onCentered`,`_onCentering`,`_beforeCentering`,`id`,`content`,`position`,`animationDuration`,`preserveContent`]],template:function(_,b){_&1&&(Pe(),c(0,`mat-tab-header`,3,0),C(`indexFocused`,function(I){return b._focusChanged(I)})(`selectFocusedIndex`,function(I){return b.selectedIndex=I}),ue(2,l,8,17,`div`,4,Pn),d(),E(4,h,1,0),c(5,`div`,5,1),ue(7,p,1,10,`mat-tab-body`,6,Pn),d()),_&2&&(S(`selectedIndex`,b.selectedIndex||0)(`disableRipple`,b.disableRipple)(`disablePagination`,b.disablePagination),kr(`aria-label`,b.ariaLabel)(`aria-labelledby`,b.ariaLabelledby),m(2),me(b._tabs),m(2),D(b._isServer?4:-1),m(),G(`_mat-animation-noopable`,b._bodyAnimationsDisabled()),m(2),me(b._tabs))},dependencies:[gO,Lw,Pg,li,Aa,Ab],styles:[`.mdc-tab {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var Rb=class{index;tab};var bm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[we]})}return t})();var vO=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-toolbar-row`]],hostAttrs:[1,`mat-toolbar-row`],exportAs:[`matToolbarRow`]})}return t})();var vm=(()=>{class t{_elementRef=f(Y);_platform=f(Re);_document=f(de);color;_toolbarRows;ngAfterViewInit(){this._platform.isBrowser&&(this._checkToolbarMixedModes(),this._toolbarRows.changes.subscribe(()=>this._checkToolbarMixedModes()))}_checkToolbarMixedModes(){this._toolbarRows.length}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[`mat-toolbar-row`]]];return H({type:t,selectors:[[`mat-toolbar`]],contentQueries:function(o,r,s){if(o&1&&jt(s,vO,5),o&2){let l;J(l=ee())&&(r._toolbarRows=l)}},hostAttrs:[1,`mat-toolbar`],hostVars:6,hostBindings:function(o,r){o&2&&(tt(r.color?`mat-`+r.color:``),G(`mat-toolbar-multiple-rows`,r._toolbarRows.length>0)(`mat-toolbar-single-row`,r._toolbarRows.length===0))},inputs:{color:`color`},exportAs:[`matToolbar`],ngContentSelectors:[`*`,`mat-toolbar-row`],decls:2,vars:0,template:function(o,r){o&1&&(Pe(e),he(0),he(1,1))},styles:[`.mat-toolbar {
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
`],encapsulation:2})})()}return t})();var _m=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[we]})}return t})();function pn(t){t||(t=f(Ae));let n=new Ce(e=>{if(t.destroyed){e.next();return}return t.onDestroy(e.next.bind(e))});return e=>e.pipe(Ne(n))}var Hw=v$1(zw());var In={apiBaseUrl:``,websocketUrl:`${window.location.protocol===`https:`?`wss:`:`ws:`}//${window.location.host}/websocket`,websocketDebug:!1};var ds=class t{http=f(Di);destroyRef=f(Ae);envelopeSubject=new V;connectedSubject=new V;disconnectedSubject=new V;errorSubject=new V;stateSubject=new hi(`connecting`);pending=new Map;client=null;subscription=null;retryTimer=null;connected=!1;synchronization=null;again=!1;generation=0;delay=500;logId=null;cursor=-1;envelopes$=this.envelopeSubject.asObservable();connected$=this.connectedSubject.asObservable();disconnected$=this.disconnectedSubject.asObservable();errors$=this.errorSubject.asObservable();state$=this.stateSubject.asObservable();get lastEventId(){return this.cursor}get eventLogId(){return this.logId}constructor(){this.destroyRef.onDestroy(()=>{this.stop()})}start(){if(this.client)return;let n=new Hw.Client({brokerURL:In.websocketUrl,heartbeatIncoming:4e3,heartbeatOutgoing:4e3,reconnectDelay:1e3,debug:In.websocketDebug?e=>console.debug(`[STOMP] `+e):()=>{}});n.onConnect=()=>{this.subscription?.unsubscribe(),this.subscription=n.subscribe(`/topic/events`,e=>this.receiveMessage(e)),this.connected=!0,this.generation++,this.connectedSubject.next(),this.retryNow()},n.onWebSocketClose=()=>{this.subscription=null,this.connected=!1,this.generation++,this.stateSubject.next(`disconnected`),this.disconnectedSubject.next(),this.clearTimer()},n.onWebSocketError=e=>this.fail(e),n.onStompError=e=>this.fail(e),this.client=n,n.activate()}async stop(){this.connected=!1,this.generation++,this.clearTimer(),this.pending.clear(),this.subscription?.unsubscribe(),this.subscription=null;let n=this.client;this.client=null,n&&await n.deactivate()}retryNow(){this.clearTimer(),this.delay=500,this.requestSync()}getHead(){return wt(this.http.get(In.apiBaseUrl+`/lastevent`).pipe(pn(this.destroyRef)))}getEvents(n,e,i){return wt(this.http.get(In.apiBaseUrl+`/events`,{params:{eventLogId:n,from:e,to:i}}).pipe(pn(this.destroyRef)))}receiveMessage(n){try{let e=JSON.parse(n.body);if(this.validate(e),e.eventLogId===this.logId&&e.eventId<=this.cursor)return;this.pending.size>=5e3&&(this.pending.clear(),this.requestSync()),this.pending.set(this.key(e.eventLogId,e.eventId),e),this.stateSubject.value===`live`&&e.eventLogId===this.logId&&this.drain(),(e.eventLogId!==this.logId||this.hasGap())&&this.requestSync()}catch(e){this.fail(e)}}requestSync(){if(!this.connected)return;if(this.synchronization){this.again=!0;return}this.again=!1;let n=this.generation;this.synchronization=this.synchronize(n).catch(e=>{n===this.generation&&this.connected&&this.fail(e)}).finally(()=>{this.synchronization=null,this.again&&this.connected&&this.requestSync()})}async synchronize(n){this.stateSubject.next(`syncing`);let e=await this.getHead();if(this.current(n)){if(typeof e.eventLogId!=`string`||!Number.isInteger(e.lastEventId)||e.lastEventId<-1)throw new Error(`Invalid event head`);if(this.logId!==e.eventLogId){this.logId=e.eventLogId,this.cursor=e.lastEventId;for(let[i,a]of this.pending)(a.eventLogId!==this.logId||a.eventId<=this.cursor)&&this.pending.delete(i)}else{if(e.lastEventId<this.cursor)throw new Error(`Cursor moved backwards without a new log identity`);await this.download(this.cursor+1,e.lastEventId+1,n)}if(this.current(n)){this.drain();for(let i=0;this.hasGap();i++){if(i===10)throw new Error(`Live delivery has not caught up`);let a=this.smallestPending();if(a===null)break;if(await this.download(this.cursor+1,a,n),!this.current(n))return;this.drain()}if(this.cursor<e.lastEventId)throw new Error(`Incomplete event history`);this.current(n)&&(this.delay=500,this.clearTimer(),this.stateSubject.next(`live`))}}}async download(n,e,i){for(let a=n;a<e;){let o=Math.min(e,a+1e3),r=this.logId,s=await this.getEvents(r,a,o);if(!this.current(i))return;if(s.eventLogId!==r||s.from!==a||s.to!==o||s.events.length!==o-a)throw new Error(`Incomplete or obsolete history`);let l=a;for(let h of s.events){if(this.validate(h),h.eventLogId!==r||h.eventId!==l++)throw new Error(`Non-contiguous history`);h.eventId>this.cursor&&this.pending.set(this.key(r,h.eventId),h)}this.drain(),a=o}}drain(){if(this.logId)for(let n=this.pending.get(this.key(this.logId,this.cursor+1));n;n=this.pending.get(this.key(this.logId,this.cursor+1)))this.pending.delete(this.key(this.logId,n.eventId)),this.cursor=n.eventId,this.envelopeSubject.next(n)}smallestPending(){let n=null;for(let e of this.pending.values())e.eventLogId===this.logId&&e.eventId>this.cursor&&(n===null||e.eventId<n)&&(n=e.eventId);return n}hasGap(){let n=this.smallestPending();return n!==null&&n>this.cursor+1}current(n){return this.connected&&n===this.generation}key(n,e){return n+`:`+e}clearTimer(){this.retryTimer&&clearTimeout(this.retryTimer),this.retryTimer=null}fail(n){this.errorSubject.next(n),this.stateSubject.next(`retrying`),this.connected&&!this.retryTimer&&(this.retryTimer=setTimeout(()=>{this.retryTimer=null,this.requestSync()},this.delay),this.delay=Math.min(3e4,this.delay*2))}validate(n){if(!n||typeof n.eventLogId!=`string`||!Number.isInteger(n.eventId)||n.eventId<0||typeof n.type!=`string`||!Number.isFinite(n.timestamp)||typeof n.workerName!=`string`||!n.payload||typeof n.payload!=`object`)throw new Error(`Invalid envelope`);let e=n.payload;if(n.type===`SolutionGeneratedEvent`&&(typeof e.resultId!=`string`||typeof e.success!=`boolean`||typeof e.iteration!=`string`||!e.objectives||typeof e.objectives!=`object`))throw new Error(`Invalid result event`);if(n.type===`ExecutionStartedEvent`&&(!e.objectives||typeof e.objectives!=`object`||!Array.isArray(e.experimentNames)))throw new Error(`Invalid execution event`);if(n.type===`ErrorEvent`&&typeof e.exceptionType!=`string`)throw new Error(`Invalid error event`)}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Fa=class t{http=f(Di);destroyRef=f(Ae);get(n,e={}){return wt(this.http.get(`${In.apiBaseUrl}${n}`,{params:e}).pipe(pn(this.destroyRef)))}getMode(){return this.get(`/api/monitor/status`)}getStatus(){return this.get(`/api/experiments/status`)}getInstances(n,e){return this.get(`/api/experiments/instances`,r({runId:n},e))}getInstance(n,e,i){return this.get(`/api/experiments/instances/${encodeURIComponent(e)}`,{runId:n,objective:i})}getResults(n,e){let i=s(r({runId:n},e),{order:e.order??`PLANNED`});return e.state||delete i.state,this.get(`/api/experiments/results`,i)}getResult(n,e){return this.get(`/api/experiments/results/${encodeURIComponent(e)}`,{runId:n})}getIssues(n,e,i){return this.get(`/api/experiments/issues`,{runId:n,severity:e,offset:i,limit:25})}getArtifacts(n){return this.get(`/api/experiments/artifacts`,{runId:n})}artifactUrl(n,e){return`${In.apiBaseUrl}/api/experiments/artifacts/${encodeURIComponent(e)}?${new URLSearchParams({runId:n})}`}exportUrl(n,e,i){let a=new URLSearchParams({runId:n,format:i,instanceId:e.instanceId,algorithm:e.algorithm,search:e.search,order:e.order??`PLANNED`});return e.state&&a.set(`state`,e.state),`${In.apiBaseUrl}/api/experiments/results/export?${a}`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var La=class{static buildInstance(n,e,i,a=!0){let o=this.referenceValue(n.started.refValues,e);return{key:n.key,experimentName:n.started.experimentName,instanceName:n.started.instanceName,selectedObjective:e,referenceValue:o,scoreChart:this.buildScoreChart(n,e,o,a),convergenceChart:this.buildConvergenceChart(n,e,i,o,a),bestSolution:this.findBest(n.solutions,e,i)}}static buildScoreChart(n,e,i,a){let o=this.groupSuccessfulPoints(n,e,!1,null);return{options:this.chartOptions(`${n.started.instanceName} scores`,e,i,o,`Raw score for every successful repetition.`,a)}}static buildConvergenceChart(n,e,i,a,o){let r=this.groupSuccessfulPoints(n,e,!0,i);return{options:this.chartOptions(`${n.started.instanceName} best score across repetitions`,e,a,r,`Best score reached by each algorithm over completed repetitions.`,o)}}static groupSuccessfulPoints(n,e,i,a){if(!e)return[];let o=[...n.started.algorithms];for(let s of n.solutions)o.includes(s.event.algorithmName)||o.push(s.event.algorithmName);let r=[];for(let s of o){let l=[],h=null;for(let p of n.solutions){let g=p.event;if(g.algorithmName!==s)continue;let _=g.success?this.objectiveValue(g,e):null;if(_===null){l.push({x:p.ordinal,y:null,custom:{iteration:g.iteration}});continue}i&&(h=h===null||this.isBetter(_,h,a)?_:h),l.push({x:p.ordinal,y:i?h:_,custom:{iteration:g.iteration}})}r.push({type:`line`,name:s,data:l,lineWidth:i?2:1,marker:{enabled:!i},animation:!1,turboThreshold:0})}return r}static referenceValue(n,e){let i=e?n[e]:void 0;return typeof i==`number`&&Number.isFinite(i)?i:null}static scoreAxis(n,e,i){let a=i?e:null,o=[];return a!==null&&o.push({id:`reference`,className:`reference-line`,value:a,color:`#d32f2f`,dashStyle:`Dash`,width:2,zIndex:4,label:{text:`Reference: ${a}`}}),{title:{text:n??`Score`},plotLines:o,softMin:a??void 0,softMax:a??void 0}}static chartOptions(n,e,i,a,o,r){return{accessibility:{enabled:!0,description:o},boost:{enabled:!0,useGPUTranslations:!0,seriesThreshold:25},chart:{animation:!1,height:320,spacing:[12,12,12,12]},credits:{enabled:!1},exporting:{enabled:!0},legend:{enabled:!0},title:{text:n,style:{display:`none`}},subtitle:{text:e?`Objective: ${e}`:`Waiting for objective metadata`},xAxis:{allowDecimals:!1,min:1,title:{text:`Repetition`}},yAxis:this.scoreAxis(e,i,r),tooltip:{headerFormat:``,pointFormat:`<b>{series.name}</b><br/>Repetition: {point.x}<br/>Iteration: {point.custom.iteration}<br/>Value: {point.y}`},plotOptions:{series:{animation:!1,connectNulls:!1}},series:a}}static findBest(n,e,i){if(!e||!i)return null;let a=null,o=null;for(let r of n){if(!r.event.success)continue;let s=this.objectiveValue(r.event,e);s!==null&&(o===null||this.isBetter(s,o,i))&&(o=s,a=r.event)}return a}static objectiveValue(n,e){let i=n.objectives[e];return typeof i==`number`&&Number.isFinite(i)?i:null}static isBetter(n,e,i){return i===`MAXIMIZE`?n>e:n<e}};var us=class{static instance(n,e,i=!0){return La.buildInstance({key:n.instance.id,started:{experimentName:n.instance.experimentName,instanceName:n.instance.instanceName,algorithms:n.algorithms.map(a=>a.name),repetitions:n.instance.counts.planned,refValues:n.instance.references},solutions:n.results.map(a=>({ordinal:a.ordinal,event:{resultId:a.resultId,success:a.state===`SUCCEEDED`,experimentName:a.experimentName,instanceName:a.instanceName,algorithmName:a.algorithmName,iteration:a.iteration,objectives:a.objectives,score:a.objectives[n.objective]??NaN,executionTime:a.executionTime??-1,timeToBest:a.timeToBest??-1}}))},n.objective,e,i)}static distribution(n,e=!0){return{options:{chart:{type:`boxplot`,height:320,animation:!1},plotOptions:{series:{animation:!1}},title:{text:`Score distribution`},credits:{enabled:!1},exporting:{enabled:!0},accessibility:{enabled:!0},xAxis:{categories:n.algorithms.map(i=>i.name)},yAxis:La.scoreAxis(n.objective,La.referenceValue(n.instance.references,n.objective),e),series:[{type:`boxplot`,name:n.objective,data:n.algorithms.flatMap((i,a)=>{let o=i.objective;return o.best===null||o.worst===null?[]:[{x:a,low:Math.min(o.best,o.worst),q1:o.lowerQuartile,median:o.median,q3:o.upperQuartile,high:Math.max(o.best,o.worst)}]})}]}}}static qualityRuntime(n,e=!0){return{options:{chart:{type:`scatter`,height:320,animation:!1},plotOptions:{series:{animation:!1}},title:{text:`Score versus runtime`},credits:{enabled:!1},exporting:{enabled:!0},accessibility:{enabled:!0},xAxis:{title:{text:`Runtime (ms)`}},yAxis:La.scoreAxis(n.objective,La.referenceValue(n.instance.references,n.objective),e),tooltip:{pointFormat:`Runtime: {point.x} ms<br/>Score: {point.y}<br/>Iteration: {point.custom.iteration}`},series:n.algorithms.map(i=>({type:`scatter`,name:i.name,data:n.results.filter(a=>a.algorithmName===i.name&&a.state===`SUCCEEDED`&&a.executionTime!==null&&Number.isFinite(a.objectives[n.objective])).map(a=>({x:a.executionTime/1e6,y:a.objectives[n.objective],custom:{iteration:a.iteration}}))}))}}}};var Jl=class t{client=f(Fa);events=f(ds);destroyRef=f(Ae);statusState=k(null);status=this.statusState.asReadonly();syncStateState=k(`connecting`);syncState=this.syncStateState.asReadonly();transportStateState=k(`connecting`);transportState=this.transportStateState.asReadonly();latestErrorState=k(null);latestError=this.latestErrorState.asReadonly();transportErrorState=k(null);transportError=this.transportErrorState.asReadonly();lastUpdatedState=k(null);lastUpdated=this.lastUpdatedState.asReadonly();selectedObjectiveState=k(null);selectedObjective=this.selectedObjectiveState.asReadonly();showReferenceState=k(!0);showReference=this.showReferenceState.asReadonly();selectedInstanceIdState=k(null);selectedInstanceId=this.selectedInstanceIdState.asReadonly();instancePageState=k(null);instancePage=this.instancePageState.asReadonly();resultPageState=k(null);resultPage=this.resultPageState.asReadonly();issuePageState=k(null);issuePage=this.issuePageState.asReadonly();artifactsState=k(null);artifacts=this.artifactsState.asReadonly();detailState=k(null);detail=this.detailState.asReadonly();inspectedResultState=k(null);inspectedResult=this.inspectedResultState.asReadonly();resultErrorState=k(null);resultError=this.resultErrorState.asReadonly();tabState=k(0);tab=this.tabState.asReadonly();instanceQueryState=k({experiment:``,search:``,offset:0,limit:25});instanceQuery=this.instanceQueryState.asReadonly();resultQueryState=k({instanceId:``,algorithm:``,state:``,search:``,offset:0,limit:25});resultQuery=this.resultQueryState.asReadonly();issueSeverityState=k(``);issueSeverity=this.issueSeverityState.asReadonly();issueOffsetState=k(0);issueOffset=this.issueOffsetState.asReadonly();seenIssueIdState=k(0);seenIssueId=this.seenIssueIdState.asReadonly();objectives=K(()=>Object.entries(this.status()?.objectives??{}).map(([n,e])=>({name:n,mode:e})));terminal=K(()=>[`SUCCEEDED`,`COMPLETED_WITH_ERRORS`,`FAILED`,`CANCELLED`].includes(this.status()?.phase??``));connectionLabel=K(()=>this.syncState()===`retrying`?`Retrying · data may be stale`:this.syncState()===`connecting`?`Connecting`:this.transportState()===`live`?`Live`:`Live via HTTP`);unseenIssues=K(()=>Math.max(0,(this.status()?.issueCount??0)-this.seenIssueId()));estimatedRemainingMillis=K(()=>{let n=this.status();if(!n||n.phase!==`RUNNING`||n.elapsedMillis<1e4)return null;let e=n.counts.succeeded+n.counts.failed;return e<5||n.counts.cancelled>0?null:n.elapsedMillis/e*(n.counts.pending+n.counts.running)});charts=K(()=>{let n=this.detail(),e=this.selectedObjective(),i=this.selectedObjectiveMode();return n&&e&&i&&n.objective===e?us.instance(n,i,this.showReference()):null});selectedObjectiveMode=K(()=>this.status()?.objectives[this.selectedObjective()??``]??null);distribution=K(()=>this.detail()?us.distribution(this.detail(),this.showReference()):null);qualityRuntime=K(()=>this.detail()?us.qualityRuntime(this.detail(),this.showReference()):null);lastEventId=K(()=>(this.transportState(),this.lastUpdated(),this.events.lastEventId));progress=K(()=>{let n=this.status(),e=this.detail(),i=e?.algorithms.find(o=>o.name===this.resultQuery().algorithm),a=i?.counts??e?.instance.counts??n?.counts;return{instances:{completed:n?.completedInstances??0,total:n?.instances??0},algorithms:{completed:e?.instance.completedAlgorithms??0,total:e?.instance.algorithms??0},repetitions:{completed:(a?.succeeded??0)+(a?.failed??0),total:a?.planned??0},repetitionScope:i?`selected algorithm`:e?`selected instance`:`execution`}});cache=new Map;timer=null;polling=!1;pending=!1;started=!1;destroyed=!1;generation=0;delay=500;forceDetail=!0;lastDetailFetch=0;resultRequest=0;constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.generation++,this.resultRequest++,this.clearTimer()})}start(){this.started||(this.started=!0,this.events.state$.pipe(pn(this.destroyRef)).subscribe(n=>{this.transportStateState.set(n),n===`live`&&this.transportErrorState.set(null)}),this.events.errors$.pipe(pn(this.destroyRef)).subscribe(n=>this.transportErrorState.set(this.message(n))),this.events.connected$.pipe(pn(this.destroyRef)).subscribe(()=>{this.generation++,this.forceDetail=!0,this.refreshNow()}),this.events.envelopes$.pipe(pn(this.destroyRef)).subscribe(n=>{n.type!==`PingEvent`&&(n.type===`ExecutionEndedEvent`||n.type===`InstanceProcessingEndedEvent`?(this.forceDetail=!0,this.refreshNow()):this.polling||this.schedule(Math.max(100,500-(Date.now()-(this.lastUpdated()?.getTime()??0)))))}),this.events.start(),this.refreshNow())}retryNow(){this.delay=500,this.events.retryNow(),this.refreshNow()}setReferenceVisible(n){this.showReferenceState.set(n)}selectObjective(n){!this.status()?.objectives[n]||this.selectedObjective()===n||(this.selectedObjectiveState.set(n),this.generation++,this.forceDetail=!0,this.detailState.set(null),this.refreshNow())}selectInstance(n){this.selectedInstanceIdState.set(n),this.generation++,this.forceDetail=!0;let e=this.cache.get(n);this.detailState.set(e?.objective===this.selectedObjective()?e:null),this.refreshNow()}selectTab(n){this.tabState.set(n),this.refreshNow()}filterInstances(n,e){this.instanceQueryState.set(s(r({},this.instanceQuery()),{experiment:n,search:e,offset:0})),this.invalidate()}pageInstances(n){this.instanceQueryState.update(e=>s(r({},e),{offset:n})),this.invalidate()}filterResults(n,e,i,a){this.resultQueryState.set(s(r({},this.resultQuery()),{instanceId:n,algorithm:e,state:i,search:a,offset:0})),this.invalidate()}pageResults(n){this.resultQueryState.update(e=>s(r({},e),{offset:n})),this.invalidate()}sortResults(n){this.resultQueryState.update(e=>s(r({},e),{order:n,offset:0})),this.invalidate()}filterIssues(n){this.issueSeverityState.set(n),this.issueOffsetState.set(0),this.invalidate()}pageIssues(n){this.issueOffsetState.set(n),this.invalidate()}markIssuesSeen(){this.seenIssueIdState.set(this.status()?.issueCount??0);try{localStorage.setItem(`mork.normal.issues.v1`,JSON.stringify({runId:this.status()?.runId,id:this.seenIssueId()}))}catch{}}async inspectResult(n){let e=this.status()?.runId;if(!e)return;let i=++this.resultRequest;this.inspectedResultState.set(null),this.resultErrorState.set(null);try{let[a,o]=await Promise.all([this.client.getResult(e,n),this.client.getArtifacts(e)]);!this.destroyed&&i===this.resultRequest&&e===this.status()?.runId&&a.runId===e&&(this.inspectedResultState.set(a),o.runId===e&&this.artifactsState.set(o))}catch(a){!this.destroyed&&i===this.resultRequest&&e===this.status()?.runId&&this.resultErrorState.set(this.message(a))}}closeResult(){this.resultRequest++,this.inspectedResultState.set(null),this.resultErrorState.set(null)}artifactUrl(n){return this.client.artifactUrl(this.status().runId,n)}exportUrl(n){return this.client.exportUrl(this.status().runId,this.resultQuery(),n)}refreshNow(){this.clearTimer(),this.polling?this.pending=!0:this.poll()}invalidate(){this.generation++,this.refreshNow()}async poll(){if(this.destroyed)return;this.polling=!0,this.pending=!1;let n=this.generation,e=2e3;try{let i=await this.client.getStatus();if(!this.current(n))return;let a=this.status();if(i.runId!==a?.runId){this.cache.clear(),this.detailState.set(null),this.inspectedResultState.set(null),this.resultRequest++,this.instancePageState.set(null),this.resultPageState.set(null),this.issuePageState.set(null),this.artifactsState.set(null),this.selectedInstanceIdState.set(null),this.selectedObjectiveState.set(Object.keys(i.objectives)[0]??null),this.instanceQueryState.set({experiment:``,search:``,offset:0,limit:25}),this.resultQueryState.set({instanceId:``,algorithm:``,state:``,search:``,offset:0,limit:25}),this.seenIssueIdState.set(0),this.forceDetail=!0;try{let o=JSON.parse(localStorage.getItem(`mork.normal.issues.v1`)??`null`);o?.runId===i.runId&&this.seenIssueIdState.set(o.id)}catch{}}if(a?.runId===i.runId&&i.revision<a.revision)throw new Error(`Monitoring revision moved backwards`);if(this.statusState.set(i),i.runId){let o=i.runId,r=await this.client.getInstances(o,this.instanceQuery());if(!this.current(n)||r.runId!==o)return;this.instancePageState.set(r),!this.selectedInstanceId()&&r.items.length&&this.selectedInstanceIdState.set(r.items.find(b=>b.counts.running>0)?.id??r.items[0].id);let s=this.selectedInstanceId(),l=this.selectedObjective(),h=s?this.cache.get(s):null,p=r.items.find(b=>b.id===s),g=!h||h.objective!==l||!p||h.revision!==p.revision,_=[`SUCCEEDED`,`COMPLETED_WITH_ERRORS`,`FAILED`,`CANCELLED`].includes(i.phase)&&i.revision!==a?.revision;if(s&&l&&g&&(this.forceDetail||_||Date.now()-this.lastDetailFetch>=2e3)){let b=await this.client.getInstance(o,s,l);if(!this.current(n)||b.runId!==o||b.objective!==this.selectedObjective())return;for(this.cache.delete(s),this.cache.set(s,b);this.cache.size>10;)this.cache.delete(this.cache.keys().next().value);this.detailState.set(b),this.lastDetailFetch=Date.now(),this.forceDetail=!1}else h&&h.objective===l&&this.detailState.set(h);if(this.tab()===1){let b=await this.client.getResults(o,this.resultQuery());if(!this.current(n)||b.runId!==o)return;this.resultPageState.set(b)}if(this.tab()===2||i.issueCount!==a?.issueCount||!this.issuePage()){let b=await this.client.getIssues(o,this.issueSeverity(),this.issueOffset());if(!this.current(n)||b.runId!==o)return;this.issuePageState.set(b)}if(this.tab()===3){let b=await this.client.getArtifacts(o);if(!this.current(n)||b.runId!==o)return;this.artifactsState.set(b)}}this.latestErrorState.set(null),this.lastUpdatedState.set(new Date),this.syncStateState.set(`live`),this.delay=500,e=this.terminal()||i.phase===`IDLE`?1e4:2e3}catch(i){if(!this.current(n))return;this.latestErrorState.set(this.message(i)),this.syncStateState.set(`retrying`),e=this.delay,this.delay=Math.min(3e4,this.delay*2)}finally{this.polling=!1,this.destroyed||this.schedule(this.pending||n!==this.generation?0:e)}}current(n){return!this.destroyed&&n===this.generation}message(n){return n instanceof Error?n.message:String(n)}schedule(n){this.destroyed||(this.clearTimer(),this.timer=setTimeout(()=>{this.poll()},n))}clearTimer(){this.timer&&clearTimeout(this.timer),this.timer=null}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Ob=[{id:`evaluation-health`,label:`Evaluation health`},{id:`evaluation-cost`,label:`Evaluation cost`},{id:`elite-evolution`,label:`Elite rank evolution`},{id:`current-elites`,label:`Current elites`},{id:`run-information`,label:`Run information`}];var Pb=[{id:`normal-comparison`,label:`Algorithm comparison`},{id:`normal-scores`,label:`Scores`},{id:`normal-best`,label:`Best score across repetitions`},{id:`normal-distribution`,label:`Score distribution`},{id:`normal-runtime`,label:`Score versus runtime`}];var xm=`mork.autoconfig.panelPreferences.v1`;var Va=class t{hiddenSignal=k(new Set);collapsedSignal=k(new Set);hidden=this.hiddenSignal.asReadonly();collapsed=this.collapsedSignal.asReadonly();constructor(){this.restore()}isHidden(n){return this.hiddenSignal().has(n)}isCollapsed(n){return this.collapsedSignal().has(n)}show(n){this.hiddenSignal.update(e=>this.without(e,n)),this.persist()}hide(n){this.hiddenSignal.update(e=>this.with(e,n)),this.persist()}toggleCollapsed(n){this.collapsedSignal.update(e=>e.has(n)?this.without(e,n):this.with(e,n)),this.persist()}reset(){this.hiddenSignal.set(new Set),this.collapsedSignal.set(new Set),localStorage.removeItem(xm)}restore(){try{let n=localStorage.getItem(xm);if(!n)return;let e=JSON.parse(n),i=new Set([...Ob,...Pb].map(a=>a.id));this.hiddenSignal.set(new Set((e.hidden??[]).filter(a=>i.has(a)))),this.collapsedSignal.set(new Set((e.collapsed??[]).filter(a=>i.has(a))))}catch{localStorage.removeItem(xm)}}with(n,e){let i=new Set(n);return i.add(e),i}without(n,e){let i=new Set(n);return i.delete(e),i}persist(){let n={hidden:[...this.hiddenSignal()],collapsed:[...this.collapsedSignal()]};localStorage.setItem(xm,JSON.stringify(n))}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Ai=class t{model=zt.required();resizable=zt(!1);chart;viewKey;constructor(){vt(()=>{let n=this.model().viewKey;this.chart&&n!==this.viewKey&&(this.viewKey=n,this.chart.zoomOut())})}chartCreated(n){this.chart=n,this.viewKey=this.model().viewKey}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-dashboard-chart`]],hostVars:2,hostBindings:function(e,i){e&2&&G(`resizable`,i.resizable())},inputs:{model:[1,`model`],resizable:[1,`resizable`]},decls:1,vars:2,consts:[[1,`chart`,`highcharts-light`,3,`chartInstance`,`options`,`oneToOne`]],template:function(e,i){e&1&&(c(0,`highcharts-chart`,0),C(`chartInstance`,function(o){return i.chartCreated(o)}),d()),e&2&&S(`options`,i.model().options)(`oneToOne`,!0)},dependencies:[CC],styles:[`[_nghost-%COMP%], .chart[_ngcontent-%COMP%]{display:block;width:100%;min-width:0}.resizable[_nghost-%COMP%]{height:32.5rem;min-height:24rem;max-height:75rem;resize:vertical;overflow:hidden;padding-bottom:.5rem;box-sizing:border-box}.resizable[_nghost-%COMP%]   .chart[_ngcontent-%COMP%]{height:100%}`]})};var yO=[`*`];function xO(t,n){t&1&&(c(0,`mat-card-content`),he(1),d())}function CO(t,n){if(t&1){let e=$();c(0,`mat-card`,1)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d(),U(4,`span`,2),c(5,`button`,3),C(`click`,function(){M(e);let a=v();return T(a.preferences.toggleCollapsed(a.panelId()))}),u(6),d(),c(7,`button`,4),u(8,` ⋮ `),d(),c(9,`mat-menu`,null,0)(11,`button`,5),C(`click`,function(){M(e);let a=v();return T(a.preferences.toggleCollapsed(a.panelId()))}),u(12),d(),c(13,`button`,5),C(`click`,function(){M(e);let a=v();return T(a.preferences.hide(a.panelId()))}),u(14,` Hide panel `),d()()(),E(15,xO,2,0,`mat-card-content`),d()}if(t&2){let e=ft(10),i=v();m(3),w(i.title()),m(2),ne(`aria-label`,i.preferences.isCollapsed(i.panelId())?`Expand `+i.title():`Collapse `+i.title()),m(),B(` `,i.preferences.isCollapsed(i.panelId())?`+`:`−`,` `),m(),S(`matMenuTriggerFor`,e),ne(`aria-label`,i.title()+` options`),m(5),B(` `,i.preferences.isCollapsed(i.panelId())?`Expand panel`:`Collapse panel`,` `),m(3),D(i.preferences.isCollapsed(i.panelId())?-1:15)}}var ms=class t{preferences=f(Va);panelId=zt.required();title=zt.required();static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-monitor-panel`]],inputs:{panelId:[1,`panelId`],title:[1,`title`]},ngContentSelectors:yO,decls:1,vars:1,consts:[[`panelMenu`,`matMenu`],[`appearance`,`outlined`],[1,`spacer`],[`mat-button`,``,`type`,`button`,1,`collapse`,3,`click`],[`mat-button`,``,`type`,`button`,1,`menu`,3,`matMenuTriggerFor`],[`mat-menu-item`,``,`type`,`button`,3,`click`]],template:function(e,i){e&1&&(Pe(),E(0,CO,16,7,`mat-card`,1)),e&2&&D(i.preferences.isHidden(i.panelId())?-1:0)},dependencies:[Ft,tn,zn,Bn,jn,Ma,ka,ss,Ni,la,rs],styles:[`[_nghost-%COMP%]{display:block;min-width:0}mat-card[_ngcontent-%COMP%]{height:100%}mat-card-header[_ngcontent-%COMP%]{min-height:3.5rem;align-items:center;padding-bottom:.5rem}mat-card-title[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-medium);font-weight:650}mat-card-content[_ngcontent-%COMP%]{min-width:0}.spacer[_ngcontent-%COMP%]{flex:1}.collapse[_ngcontent-%COMP%], .menu[_ngcontent-%COMP%]{min-width:2.25rem;padding:0;font-size:1.25rem}`]})};var SO=(t,n)=>n.id;function wO(t,n){if(t&1&&(c(0,`p`,0),u(1),d()),t&2){let e=n.$implicit;m(),Si(` `,e.severity,` · `,e.scope,`: `,e.exceptionType,` — `,e.message,` `)}}function EO(t,n){t&1&&wa(0)}function DO(t,n){if(t&1&&rt(0,EO,1,0,`ng-container`,2),t&2){let e=v();S(`ngComponentOutlet`,n)(`ngComponentOutletInputs`,e.rendererInputs())}}function IO(t,n){if(t&1&&(c(0,`details`,1)(1,`summary`),u(2,`Solution view data`),d(),c(3,`pre`),u(4),j(5,`json`),d()()),t&2){let e=v();m(4),w(_e(5,1,e.detail().rendering.data))}}function kO(t,n){if(t&1&&(c(0,`p`,0),u(1),d()),t&2){let e=v();m(),B(` `,e.detail().rendering.state===`UNSUPPORTED`?`A problem-specific solution view is not provided for this project.`:`This solution view is no longer retained.`,` Metadata remains available; exported solution files are listed below or in Artifacts. `)}}var MO=new N(`Mork solution renderers`,{providedIn:`root`,factory:()=>({})});var Cm=class t{detail=zt.required();selectedObjective=zt.required();registered=f(MO);renderer=K(()=>this.detail().rendering.state===`AVAILABLE`?this.registered[this.detail().rendering.rendererId??``]??null:null);rendererInputs=K(()=>({result:this.detail().result,selectedObjective:this.selectedObjective(),view:this.detail().rendering.data}));static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-solution-renderer`]],inputs:{detail:[1,`detail`],selectedObjective:[1,`selectedObjective`]},decls:58,vars:27,consts:[[1,`note`],[`open`,``],[4,`ngComponentOutlet`,`ngComponentOutletInputs`]],template:function(e,i){if(e&1&&(c(0,`dl`)(1,`div`)(2,`dt`),u(3,`Instance / experiment`),d(),c(4,`dd`),u(5),d()(),c(6,`div`)(7,`dt`),u(8,`Algorithm`),d(),c(9,`dd`),u(10),d()(),c(11,`div`)(12,`dt`),u(13,`Iteration / seed`),d(),c(14,`dd`),u(15),d()(),c(16,`div`)(17,`dt`),u(18,`Outcome`),d(),c(19,`dd`),u(20),d()(),c(21,`div`)(22,`dt`),u(23),d(),c(24,`dd`),u(25),j(26,`number`),d()(),c(27,`div`)(28,`dt`),u(29,`Runtime`),d(),c(30,`dd`),u(31),j(32,`number`),d()(),c(33,`div`)(34,`dt`),u(35,`Time to best`),d(),c(36,`dd`),u(37),j(38,`number`),d()(),c(39,`div`)(40,`dt`),u(41,`Result ID`),d(),c(42,`dd`),u(43),d()()(),c(44,`details`)(45,`summary`),u(46,`All objective values and custom properties`),d(),c(47,`pre`),u(48),j(49,`json`),d(),c(50,`pre`),u(51),j(52,`json`),d()(),ue(53,wO,2,4,`p`,0,SO),E(55,DO,1,2,`ng-container`)(56,IO,6,3,`details`,1)(57,kO,2,1,`p`,0)),e&2){let a,o=i.detail().result;m(5),Me(``,o.instanceName,` / `,o.experimentName),m(5),w(o.algorithmName),m(5),Me(``,o.iteration,` / `,o.seed),m(5),w(o.state),m(3),w(i.selectedObjective()??`Objective`),m(2),B(` `,ke(26,14,o.objectives[i.selectedObjective()??``],`1.0-4`)??`Unavailable`,` `),m(6),B(` `,o.executionTime===null?`Unavailable`:ke(32,17,o.executionTime/1e6,`1.0-2`)+` ms`,` `),m(6),B(` `,o.timeToBest===null?`Unavailable`:ke(38,20,o.timeToBest/1e6,`1.0-2`)+` ms`,` `),m(6),w(o.resultId),m(5),w(_e(49,23,o.objectives)),m(3),w(_e(52,25,o.properties)),m(2),me(i.detail().issues),m(2),D((a=i.renderer())?55:i.detail().rendering.state===`AVAILABLE`?56:57,a)}},dependencies:[hg,wo,En],styles:[`dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.75rem}dl[_ngcontent-%COMP%] > div[_ngcontent-%COMP%]{display:grid;gap:.25rem}dt[_ngcontent-%COMP%], .note[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}pre[_ngcontent-%COMP%]{overflow:auto;max-height:24rem;white-space:pre-wrap}@media(max-width:42rem){dl[_ngcontent-%COMP%]{grid-template-columns:1fr}}`]})};var ps=()=>[];var ec=(t,n)=>n.id;var Fb=(t,n)=>n.name;var TO=(t,n)=>n.resultId;function NO(t,n){if(t&1){let e=$();c(0,`button`,8),C(`click`,function(){let a=M(e).$implicit,o=v();return T(o.preferences.isHidden(a.id)?o.preferences.show(a.id):o.preferences.hide(a.id))}),u(1),d()}if(t&2){let e=n.$implicit,i=v();m(),Me(` `,i.preferences.isHidden(e.id)?`Show`:`Hide`,` `,e.label,` `)}}function AO(t,n){if(t&1){let e=$();c(0,`mat-card`,9)(1,`mat-card-content`)(2,`strong`),u(3,`Connection problem · showing last known data`),d(),c(4,`p`),u(5),d(),c(6,`button`,34),C(`click`,function(){M(e);return T(v().store.retryNow())}),u(7,`Retry now`),d()()()}t&2&&(m(5),w(n))}function RO(t,n){if(t&1&&(c(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit;S(`value`,e.name),m(),Me(``,e.name,` · `,e.mode===`MINIMIZE`?`Minimize`:`Maximize`)}}function OO(t,n){t&1&&(u(0),j(1,`date`)),t&2&&B(` Last refreshed `,ke(1,1,n,`mediumTime`),`. `)}function PO(t,n){if(t&1&&(c(0,`p`,24),u(1),d()),t&2){let e=v();m(),B(` Approximate time remaining: `,e.elapsedLabel(n),` · based on observed throughput; different instance sizes can change this estimate. `)}}function FO(t,n){if(t&1){let e=$();c(0,`button`,35),C(`click`,function(){M(e);return T(v().store.selectTab(2))}),u(1),d()}if(t&2){let e=v();m(),B(` `,e.store.unseenIssues(),` unseen issues `)}}function LO(t,n){if(t&1&&(c(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit;S(`value`,e.name),m(),w(e.name)}}function VO(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`)(2,`button`,35),C(`click`,function(){let a=M(e).$implicit;return T(v(2).store.selectInstance(a.id))}),u(3),d()(),c(4,`td`),u(5),d(),c(6,`td`),u(7),d(),c(8,`td`),u(9),d(),c(10,`td`),u(11),d()()}if(t&2){let e=n.$implicit,i=v(2);G(`selected`,e.id===i.store.selectedInstanceId()),m(3),B(` `,e.instanceName,` `),m(2),w(e.experimentName),m(2),Me(` `,e.counts.succeeded+e.counts.failed,` / `,e.counts.planned,` `),m(2),w(e.counts.running),m(2),Me(` `,e.counts.failed,` failed · `,e.finalized?`finalized`:`processing`,` `)}}function BO(t,n){t&1&&(c(0,`tr`)(1,`td`,46),u(2,`No matching instances yet.`),d()())}function jO(t,n){if(t&1&&(c(0,`p`,24),u(1),d()),t&2){let e=v();m(),Me(` Charts show `,e.results.length,` sampled points from `,e.resultCount,` repetitions. Statistics are exact; full data is available in Results and exports. `)}}function zO(t,n){if(t&1&&(u(0),j(1,`number`)),t&2){let e=v().$implicit;B(` (`,ke(1,1,e.objective.percentageGap,`1.0-2`),`%) `)}}function HO(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),d(),c(7,`td`),u(8),j(9,`number`),d(),c(10,`td`),u(11),j(12,`number`),d(),c(13,`td`),u(14),j(15,`number`),j(16,`number`),d(),c(17,`td`),u(18),j(19,`number`),E(20,zO,2,4),d(),c(21,`td`),u(22),j(23,`number`),d(),c(24,`td`),u(25),j(26,`number`),d(),c(27,`td`)(28,`button`,49),C(`click`,function(){let a=M(e).$implicit;return T(v(3).store.inspectResult(a.bestResultId))}),u(29,` Best result `),d()()()}if(t&2){let e=n.$implicit;m(2),w(e.name),m(2),Me(` `,e.counts.succeeded,` / `,e.counts.succeeded+e.counts.failed,` `),m(2),w(e.objective.count),m(2),w(ke(9,13,e.objective.best,`1.0-4`)??`—`),m(3),w(ke(12,16,e.objective.median,`1.0-4`)??`—`),m(3),Me(` `,ke(15,19,e.objective.mean,`1.0-4`)??`—`,` ± `,ke(16,22,e.objective.standardDeviation,`1.0-4`)??`—`,` `),m(4),B(` `,ke(19,25,e.objective.absoluteGap,`1.0-4`)??`—`,` `),m(2),D(e.objective.percentageGap!==null?20:-1),m(2),B(``,ke(23,28,e.runtimeMillis.median,`1.0-2`)??`—`,` ms`),m(3),B(``,ke(26,31,e.timeToBestMillis.median,`1.0-2`)??`—`,` ms`),m(3),S(`disabled`,!e.bestResultId)}}function UO(t,n){if(t&1){let e=$();c(0,`mat-checkbox`,56),C(`change`,function(a){M(e);return T(v(4).store.setReferenceVisible(a.checked))}),u(1),d(),c(2,`p`,24),u(3),d()}if(t&2){let e=v(),i=v(),a=v(2);S(`checked`,a.store.showReference()),m(),Me(` Show reference (`,i.objective,`: `,e.referenceValue,`) `),m(2),B(` The red dashed line marks the reference in all score charts. `,a.store.status()?.objectives?.[i.objective]===`MAXIMIZE`?`Higher`:`Lower`,` scores are better. `)}}function $O(t,n){if(t&1&&(c(0,`p`,24),u(1),d()),t&2){let e=v(2);m(),B(` No reference value is available for `,e.objective,`. `)}}function GO(t,n){if(t&1&&U(0,`app-dashboard-chart`,52),t&2)S(`model`,v().scoreChart)}function WO(t,n){if(t&1&&U(0,`app-dashboard-chart`,52),t&2)S(`model`,v().convergenceChart)}function qO(t,n){t&1&&U(0,`app-dashboard-chart`,52),t&2&&S(`model`,n)}function YO(t,n){t&1&&U(0,`app-dashboard-chart`,52),t&2&&S(`model`,n)}function KO(t,n){if(t&1&&(E(0,UO,4,4)(1,$O,2,1,`p`,24),c(2,`div`,50)(3,`app-monitor-panel`,51),E(4,GO,1,1,`app-dashboard-chart`,52),d(),c(5,`app-monitor-panel`,53),E(6,WO,1,1,`app-dashboard-chart`,52),c(7,`p`,24),u(8,` Independent repetitions, not the optimization trajectory within a single run. Failures are gaps. `),d()(),c(9,`app-monitor-panel`,54),E(10,qO,1,1,`app-dashboard-chart`,52),d(),c(11,`app-monitor-panel`,55),E(12,YO,1,1,`app-dashboard-chart`,52),d()()),t&2){let e,i,a=v(3);D(n.referenceValue!==null?0:1),m(4),D(!a.preferences.isHidden(`normal-scores`)&&!a.preferences.isCollapsed(`normal-scores`)?4:-1),m(2),D(!a.preferences.isHidden(`normal-best`)&&!a.preferences.isCollapsed(`normal-best`)?6:-1),m(4),D((e=!a.preferences.isHidden(`normal-distribution`)&&!a.preferences.isCollapsed(`normal-distribution`)&&a.store.distribution())?10:-1,e),m(2),D((i=!a.preferences.isHidden(`normal-runtime`)&&!a.preferences.isCollapsed(`normal-runtime`)&&a.store.qualityRuntime())?12:-1,i)}}function QO(t,n){if(t&1&&(c(0,`h2`,47),u(1),c(2,`small`),u(3),d()(),E(4,jO,2,2,`p`,24),c(5,`app-monitor-panel`,48)(6,`div`,41)(7,`table`)(8,`caption`,42),u(9),d(),c(10,`thead`)(11,`tr`)(12,`th`),u(13,`Algorithm`),d(),c(14,`th`),u(15,`Successful / attempted`),d(),c(16,`th`),u(17,`Valid scores`),d(),c(18,`th`),u(19,`Best`),d(),c(20,`th`),u(21,`Median`),d(),c(22,`th`),u(23,`Mean ± SD`),d(),c(24,`th`),u(25,`Gap to reference`),d(),c(26,`th`),u(27,`Runtime median`),d(),c(28,`th`),u(29,`Time-to-best median`),d(),c(30,`th`),u(31,`Details`),d()()(),c(32,`tbody`),ue(33,HO,30,34,`tr`,null,Fb),d()()(),c(35,`p`,24),u(36,` Statistics use successful results with finite values. Scores from different instances are not averaged. Negative gaps improve on the reference. `),d()(),E(37,KO,13,5)),t&2){let e,i=n,a=v(2);m(),B(` `,i.instance.instanceName,` `),m(2),w(i.instance.experimentName),m(),D(i.sampled?4:-1),m(5),B(` `,i.objective,` statistics by algorithm `),m(24),me(i.algorithms),m(4),D((e=a.store.charts())?37:-1,e)}}function XO(t,n){t&1&&(c(0,`p`,45),u(1,`Select an instance to inspect its results and charts.`),d())}function ZO(t,n){if(t&1){let e=$();c(0,`mat-card`,36)(1,`mat-card-content`)(2,`h2`),u(3,`Instances`),d(),c(4,`div`,37)(5,`mat-form-field`,38)(6,`mat-label`),u(7,`Experiment`),d(),c(8,`mat-select`,15),C(`selectionChange`,function(a){M(e);let o=v();return T(o.store.filterInstances(a.value,o.store.instanceQuery().search))}),c(9,`mat-option`,39),u(10,`All experiments`),d(),ue(11,LO,2,2,`mat-option`,16,Fb),d()(),c(13,`mat-form-field`,38)(14,`mat-label`),u(15,`Search instances`),d(),c(16,`input`,40),C(`input`,function(a){M(e);let o=v();return T(o.store.filterInstances(o.store.instanceQuery().experiment,a.target.value))}),d()()(),c(17,`div`,41)(18,`table`)(19,`caption`,42),u(20,` Instances across the execution `),d(),c(21,`thead`)(22,`tr`)(23,`th`),u(24,`Instance`),d(),c(25,`th`),u(26,`Experiment`),d(),c(27,`th`),u(28,`Completed repetitions`),d(),c(29,`th`),u(30,`Running`),d(),c(31,`th`),u(32,`Outcome`),d()()(),c(33,`tbody`),ue(34,VO,12,9,`tr`,43,ec,!1,BO,3,0,`tr`),d()()(),c(37,`mat-paginator`,44),C(`page`,function(a){M(e);return T(v().store.pageInstances(a.pageIndex*a.pageSize))}),d()()(),E(38,QO,38,5)(39,XO,2,0,`p`,45)}if(t&2){let e,i=v();m(8),S(`value`,i.store.instanceQuery().experiment),m(3),me(i.store.status()?.experiments??nt(7,ps)),m(5),S(`value`,i.store.instanceQuery().search),m(18),me(i.store.instancePage()?.items??nt(8,ps)),m(3),S(`length`,i.store.instancePage()?.total??0)(`pageSize`,25)(`pageIndex`,i.store.instanceQuery().offset/25),m(),D((e=i.store.detail())?38:39,e)}}function JO(t,n){if(t&1&&(c(0,`mat-option`,16),u(1),d()),t&2){let e=n.$implicit,i=v(2);S(`value`,e),m(),w(e?i.phaseLabel(e):`All outcomes`)}}function eP(t,n){if(t&1&&(c(0,`a`,63),u(1,`CSV`),d(),c(2,`a`,63),u(3,`JSON`),d()),t&2){let e=v(2);S(`href`,e.store.exportUrl(`csv`),cn),m(2),S(`href`,e.store.exportUrl(`json`),cn)}}function tP(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),d(),c(7,`td`),u(8),d(),c(9,`td`),u(10),d(),c(11,`td`),u(12),j(13,`number`),d(),c(14,`td`),u(15),j(16,`number`),d(),c(17,`td`)(18,`button`,35),C(`click`,function(){let a=M(e).$implicit;return T(v(2).store.inspectResult(a.resultId))}),u(19,` Inspect `),d()()()}if(t&2){let e=n.$implicit,i=v(2);m(2),Me(``,e.experimentName,` / `,e.instanceName),m(2),w(e.algorithmName),m(2),w(e.iteration),m(2),w(e.seed),m(2),w(i.phaseLabel(e.state)),m(2),B(` `,ke(13,8,e.objectives[i.store.selectedObjective()??``],`1.0-4`)??`—`,` `),m(3),B(` `,e.executionTime===null?`—`:ke(16,11,e.executionTime/1e6,`1.0-2`)+` ms`,` `)}}function nP(t,n){t&1&&(c(0,`tr`)(1,`td`,64),u(2,`No matching repetitions.`),d()())}function iP(t,n){if(t&1){let e=$();c(0,`mat-card`,36)(1,`mat-card-content`)(2,`div`,37)(3,`mat-form-field`,38)(4,`mat-label`),u(5,`Search experiment, instance or algorithm`),d(),c(6,`input`,40),C(`input`,function(a){M(e);let o=v();return T(o.store.filterResults(o.store.resultQuery().instanceId,o.store.resultQuery().algorithm,o.store.resultQuery().state,a.target.value))}),d()(),c(7,`mat-form-field`,38)(8,`mat-label`),u(9,`Outcome`),d(),c(10,`mat-select`,15),C(`selectionChange`,function(a){M(e);let o=v();return T(o.store.filterResults(o.store.resultQuery().instanceId,o.store.resultQuery().algorithm,a.value,o.store.resultQuery().search))}),ue(11,JO,2,2,`mat-option`,16,Pn),d()(),c(13,`mat-form-field`,38)(14,`mat-label`),u(15,`Algorithm name`),d(),c(16,`input`,40),C(`input`,function(a){M(e);let o=v();return T(o.store.filterResults(o.store.resultQuery().instanceId,a.target.value,o.store.resultQuery().state,o.store.resultQuery().search))}),d()(),c(17,`mat-form-field`,38)(18,`mat-label`),u(19,`Sort results`),d(),c(20,`mat-select`,15),C(`selectionChange`,function(a){M(e);return T(v().store.sortResults(a.value))}),c(21,`mat-option`,57),u(22,`Planned order`),d(),c(23,`mat-option`,58),u(24,`Instance name`),d(),c(25,`mat-option`,59),u(26,`Algorithm name`),d(),c(27,`mat-option`,60),u(28,`Outcome`),d(),c(29,`mat-option`,61),u(30,`Runtime · fastest first`),d()()(),c(31,`button`,35),C(`click`,function(){M(e);let a=v();return T(a.store.filterResults(a.store.selectedInstanceId()??``,a.store.resultQuery().algorithm,a.store.resultQuery().state,a.store.resultQuery().search))}),u(32,` Selected instance only `),d(),c(33,`button`,35),C(`click`,function(){M(e);return T(v().store.filterResults(``,``,``,``))}),u(34,` Clear filters `),d(),E(35,eP,4,2),d(),c(36,`div`,41)(37,`table`)(38,`caption`,42),u(39,` Repetition result ledger `),d(),c(40,`thead`)(41,`tr`)(42,`th`),u(43,`Experiment / instance`),d(),c(44,`th`),u(45,`Algorithm`),d(),c(46,`th`),u(47,`Iteration`),d(),c(48,`th`),u(49,`Seed`),d(),c(50,`th`),u(51,`Outcome`),d(),c(52,`th`),u(53),d(),c(54,`th`),u(55,`Runtime`),d(),c(56,`th`),u(57,`Details`),d()()(),c(58,`tbody`),ue(59,tP,20,14,`tr`,null,TO,!1,nP,3,0,`tr`),d()()(),c(62,`mat-paginator`,62),C(`page`,function(a){M(e);return T(v().store.pageResults(a.pageIndex*a.pageSize))}),d()()()}if(t&2){let e=v();m(6),S(`value`,e.store.resultQuery().search),m(4),S(`value`,e.store.resultQuery().state),m(),me(e.workStates),m(5),S(`value`,e.store.resultQuery().algorithm),m(4),S(`value`,e.store.resultQuery().order??`PLANNED`),m(15),D(e.store.status()?.runId?35:-1),m(18),w(e.store.selectedObjective()??`Objective`),m(6),me(e.store.resultPage()?.items??nt(10,ps)),m(3),S(`length`,e.store.resultPage()?.total??0)(`pageSize`,25)(`pageIndex`,e.store.resultQuery().offset/25)}}function aP(t,n){if(t&1&&u(0),t&2){let e=v().$implicit;Me(` · iteration `,e.iteration,` · seed `,e.seed,` `)}}function oP(t,n){if(t&1){let e=$();c(0,`button`,35),C(`click`,function(){M(e);let a=v().$implicit;return T(v(2).store.inspectResult(a.resultId))}),u(1,` Inspect result `),d()}}function rP(t,n){if(t&1&&(c(0,`mat-card`,68)(1,`mat-card-content`)(2,`strong`),u(3),d(),c(4,`p`),u(5),d(),c(6,`p`),u(7),E(8,aP,1,2),u(9),j(10,`date`),d(),E(11,oP,2,0,`button`,25),d()()),t&2){let e=n.$implicit;m(3),Ot(``,e.severity,` · `,e.scope,` · `,e.exceptionType),m(2),w(e.message??`No details provided`),m(2),Ot(` `,e.experimentName,` / `,e.instanceName,` / `,e.algorithmName,` `),m(),D(e.iteration!==null?8:-1),m(),B(` · `,ke(10,10,e.timestamp,`medium`),` `),m(2),D(e.resultId?11:-1)}}function sP(t,n){t&1&&(c(0,`p`,45),u(1,`No experiment issues have been reported.`),d())}function lP(t,n){if(t&1){let e=$();c(0,`div`,37)(1,`mat-form-field`,38)(2,`mat-label`),u(3,`Severity`),d(),c(4,`mat-select`,15),C(`selectionChange`,function(a){M(e);return T(v().store.filterIssues(a.value))}),c(5,`mat-option`,39),u(6,`All severities`),d(),c(7,`mat-option`,65),u(8,`Fatal`),d(),c(9,`mat-option`,66),u(10,`Error`),d(),c(11,`mat-option`,67),u(12,`Warning`),d()()(),c(13,`button`,35),C(`click`,function(){M(e);return T(v().store.markIssuesSeen())}),u(14,`Mark issues seen`),d()(),ue(15,rP,12,13,`mat-card`,68,ec,!1,sP,2,0,`p`,45),c(18,`mat-paginator`,69),C(`page`,function(a){M(e);return T(v().store.pageIssues(a.pageIndex*a.pageSize))}),d()}if(t&2){let e=v();m(4),S(`value`,e.store.issueSeverity()),m(11),me(e.store.issuePage()?.items??nt(5,ps)),m(3),S(`length`,e.store.issuePage()?.total??0)(`pageSize`,25)(`pageIndex`,e.store.issueOffset()/25)}}function cP(t,n){if(t&1&&(c(0,`a`,63),u(1,`Download results CSV`),d(),c(2,`a`,63),u(3,`Download results JSON`),d()),t&2){let e=v(2);S(`href`,e.store.exportUrl(`csv`),cn),m(2),S(`href`,e.store.exportUrl(`json`),cn)}}function dP(t,n){if(t&1&&(c(0,`tr`)(1,`td`)(2,`a`,70),u(3),d()(),c(4,`td`),u(5),d(),c(6,`td`),u(7),j(8,`number`),d(),c(9,`td`),u(10),j(11,`date`),d()()),t&2){let e=n.$implicit,i=v(2);m(2),S(`href`,i.store.artifactUrl(e.id),cn),m(),w(e.filename),m(2),w(e.experimentName),m(2),w(_e(8,5,e.size)),m(3),w(ke(11,7,e.modifiedAt,`medium`))}}function uP(t,n){t&1&&(c(0,`tr`)(1,`td`,71),u(2,`No exported artifacts are available yet.`),d()())}function mP(t,n){if(t&1&&(c(0,`p`),u(1,` Only successfully exported, registered files are available. Result exports use the current Results filters. `),d(),E(2,cP,4,2),c(3,`div`,41)(4,`table`)(5,`thead`)(6,`tr`)(7,`th`),u(8,`File`),d(),c(9,`th`),u(10,`Experiment`),d(),c(11,`th`),u(12,`Bytes`),d(),c(13,`th`),u(14,`Updated`),d()()(),c(15,`tbody`),ue(16,dP,12,10,`tr`,null,ec,!1,uP,3,0,`tr`),d()()()),t&2){let e=v();m(2),D(e.store.status()?.runId?2:-1),m(14),me(e.store.artifacts()?.artifacts??nt(2,ps))}}function pP(t,n){t&1&&(c(0,`p`),u(1),d()),t&2&&(m(),w(n))}function hP(t,n){if(t&1){let e=$();c(0,`mat-card`,72)(1,`mat-card-content`)(2,`h2`),u(3,`Transport diagnostics`),d(),c(4,`p`),u(5),d(),c(6,`p`),u(7),d(),c(8,`p`),u(9),d(),c(10,`p`),u(11),d(),E(12,pP,2,1,`p`),c(13,`p`),u(14,` Summaries and results come from backend snapshots. Connection health is independent of execution outcome. `),d(),c(15,`button`,35),C(`click`,function(){M(e);return T(v().store.retryNow())}),u(16,`Reconnect / refresh`),d()()()}if(t&2){let e,i=v();m(5),B(`Run: `,i.store.status()?.runId??`No execution`),m(2),B(`Revision: `,i.store.status()?.revision??0),m(2),B(`WebSocket: `,i.store.transportState()),m(2),B(`Last processed notification ID: `,i.store.lastEventId()),m(),D((e=i.store.transportError())?12:-1,e)}}function fP(t,n){if(t&1){let e=$();c(0,`mat-card`,9)(1,`mat-card-content`),u(2),c(3,`button`,35),C(`click`,function(){M(e);return T(v().store.closeResult())}),u(4,`Close`),d()()()}t&2&&(m(2),w(n))}function gP(t,n){if(t&1&&(c(0,`a`,63),u(1),d()),t&2){let e=v().$implicit;S(`href`,v(2).store.artifactUrl(e.id),cn),m(),w(e.filename)}}function bP(t,n){if(t&1&&E(0,gP,2,2,`a`,63),t&2){let e=n.$implicit,i=v();D(e.resultId===i.result.resultId?0:-1)}}function vP(t,n){if(t&1){let e=$();c(0,`mat-card`,33)(1,`mat-card-content`)(2,`div`,73)(3,`h2`),u(4,`Result details`),d(),c(5,`button`,35),C(`click`,function(){M(e);return T(v().store.closeResult())}),u(6,`Close`),d()(),U(7,`app-solution-renderer`,74),ue(8,bP,1,1,null,null,ec),d()()}if(t&2){let e=v();m(7),S(`detail`,n)(`selectedObjective`,e.store.selectedObjective()),m(),me(e.store.artifacts()?.artifacts??nt(2,ps))}}var Sm=class t{store=f(Jl);preferences=f(Va);panels=Pb;workStates=[``,`PENDING`,`RUNNING`,`SUCCEEDED`,`FAILED`,`CANCELLED`];ngOnInit(){this.store.start()}progressValue(n,e){return e>0?Math.min(100,n/e*100):0}elapsedLabel(n){let e=Math.floor(n/1e3);return e>=3600?Math.floor(e/3600)+`h `+Math.floor(e%3600/60)+`m`:Math.floor(e/60)+`m `+e%60+`s`}phaseLabel(n){return n.toLowerCase().replaceAll(`_`,` `)}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-standard-dashboard`]],features:[Ge([Jl,ds,Fa])],decls:113,vars:31,consts:[[`panelMenu`,`matMenu`],[1,`topbar`],[`src`,`favicon.svg`,`alt`,`Mork`,1,`logo`],[1,`brand`],[1,`toolbar-spacer`],[`aria-label`,`Connection state`],[`mat-button`,``,`aria-label`,`Choose dashboard panels`,3,`matMenuTriggerFor`],[`mat-menu-item`,``],[`mat-menu-item`,``,3,`click`],[`role`,`alert`,1,`error-card`],[`appearance`,`outlined`,1,`status-card`],[`aria-label`,`Execution summary`,1,`summary`],[1,`eyebrow`],[1,`phase`],[`appearance`,`outline`,`subscriptSizing`,`dynamic`],[3,`selectionChange`,`value`],[3,`value`],[`aria-label`,`Repetition outcomes`,1,`metrics`],[`aria-label`,`Execution progress`,1,`progress-grid`],[1,`progress-item`],[1,`progress-label`],[`mode`,`determinate`,`aria-label`,`Finalized instances in execution`,3,`value`],[`mode`,`determinate`,`aria-label`,`Finalized algorithms in selected instance`,3,`value`],[`mode`,`determinate`,`aria-label`,`Completed repetitions in selection`,3,`value`],[1,`status-footnote`],[`mat-button`,``],[`animationDuration`,`0ms`,3,`selectedIndexChange`,`selectedIndex`],[`label`,`Overview`],[`matTabContent`,``],[`label`,`Results`],[`label`,`Issues`],[`label`,`Artifacts`],[`label`,`Diagnostics`],[`appearance`,`outlined`,`aria-label`,`Result details`,1,`result-detail`],[`mat-flat-button`,``,3,`click`],[`mat-button`,``,3,`click`],[`appearance`,`outlined`,1,`browser`],[1,`filters`],[`appearance`,`outline`],[`value`,``],[`matInput`,``,3,`input`,`value`],[1,`table-scroll`],[1,`sr-only`],[3,`selected`],[`aria-label`,`Instance pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[1,`empty-state`],[`colspan`,`5`],[1,`detail-title`],[`panelId`,`normal-comparison`,`title`,`Algorithm comparison`],[`mat-button`,``,3,`click`,`disabled`],[1,`chart-grid`],[`panelId`,`normal-scores`,`title`,`Scores`],[3,`model`],[`panelId`,`normal-best`,`title`,`Best score across repetitions`],[`panelId`,`normal-distribution`,`title`,`Score distribution`],[`panelId`,`normal-runtime`,`title`,`Score versus runtime`],[1,`reference-toggle`,3,`change`,`checked`],[`value`,`PLANNED`],[`value`,`INSTANCE`],[`value`,`ALGORITHM`],[`value`,`STATE`],[`value`,`RUNTIME`],[`aria-label`,`Result pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[`mat-button`,``,3,`href`],[`colspan`,`8`],[`value`,`FATAL`],[`value`,`ERROR`],[`value`,`WARNING`],[`appearance`,`outlined`,1,`issue`],[`aria-label`,`Issue pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[3,`href`],[`colspan`,`4`],[1,`browser`],[1,`result-heading`],[3,`detail`,`selectedObjective`]],template:function(e,i){if(e&1&&(c(0,`mat-toolbar`,1),U(1,`img`,2),c(2,`span`,3),u(3,`Experiment monitor`),d(),U(4,`span`,4),c(5,`mat-chip-set`,5)(6,`mat-chip`),u(7),d()(),c(8,`button`,6),u(9,` Panels `),d(),c(10,`mat-menu`,null,0),ue(12,NO,2,2,`button`,7,ec),c(14,`button`,8),C(`click`,function(){return i.preferences.reset()}),u(15,`Reset panels`),d()()(),c(16,`main`),E(17,AO,8,1,`mat-card`,9),c(18,`mat-card`,10)(19,`mat-card-content`)(20,`section`,11)(21,`div`)(22,`span`,12),u(23,`Execution`),d(),c(24,`strong`,13),u(25),d()(),c(26,`div`)(27,`span`,12),u(28,`Experiment`),d(),c(29,`strong`),u(30),d()(),c(31,`div`)(32,`span`,12),u(33,`Elapsed`),d(),c(34,`strong`),u(35),d()(),c(36,`mat-form-field`,14)(37,`mat-label`),u(38,`Objective`),d(),c(39,`mat-select`,15),C(`selectionChange`,function(o){return i.store.selectObjective(o.value)}),ue(40,RO,2,3,`mat-option`,16,Fb),d()()(),c(42,`section`,17)(43,`div`)(44,`span`),u(45,`Planned`),d(),c(46,`strong`),u(47),d()(),c(48,`div`)(49,`span`),u(50,`Pending`),d(),c(51,`strong`),u(52),d()(),c(53,`div`)(54,`span`),u(55,`Running`),d(),c(56,`strong`),u(57),d()(),c(58,`div`)(59,`span`),u(60,`Successful`),d(),c(61,`strong`),u(62),d()(),c(63,`div`)(64,`span`),u(65,`Failed`),d(),c(66,`strong`),u(67),d()(),c(68,`div`)(69,`span`),u(70,`Cancelled`),d(),c(71,`strong`),u(72),d()()(),c(73,`section`,18)(74,`div`,19)(75,`div`,20)(76,`span`),u(77,`Instances · execution`),d(),c(78,`span`),u(79),d()(),U(80,`mat-progress-bar`,21),d(),c(81,`div`,19)(82,`div`,20)(83,`span`),u(84,`Algorithms · selected instance`),d(),c(85,`span`),u(86),d()(),U(87,`mat-progress-bar`,22),d(),c(88,`div`,19)(89,`div`,20)(90,`span`),u(91),d(),c(92,`span`),u(93),d()(),U(94,`mat-progress-bar`,23),d()(),c(95,`p`,24),u(96),E(97,OO,2,4),d(),E(98,PO,2,1,`p`,24),E(99,FO,2,1,`button`,25),d()(),c(100,`mat-tab-group`,26),C(`selectedIndexChange`,function(o){return i.store.selectTab(o)}),c(101,`mat-tab`,27),rt(102,ZO,40,9,`ng-template`,28),d(),c(103,`mat-tab`,29),rt(104,iP,63,11,`ng-template`,28),d(),c(105,`mat-tab`,30),rt(106,lP,19,6,`ng-template`,28),d(),c(107,`mat-tab`,31),rt(108,mP,19,3,`ng-template`,28),d(),c(109,`mat-tab`,32),rt(110,hP,17,5,`ng-template`,28),d()(),E(111,fP,5,1,`mat-card`,9),E(112,vP,10,3,`mat-card`,33),d()),e&2){let a,o,r,s,l,h=ft(11);m(7),w(i.store.connectionLabel()),m(),S(`matMenuTriggerFor`,h),m(4),me(i.panels),m(5),D((a=i.store.latestError())?17:-1,a);let p=i.store.status();m(8),w(i.phaseLabel(p?.phase??`IDLE`)),m(5),w(p?.currentExperiment??`Waiting for execution`),m(5),w(i.elapsedLabel(p?.elapsedMillis??0)),m(4),S(`value`,i.store.selectedObjective()),m(),me(i.store.objectives()),m(7),w(p?.counts?.planned??0),m(5),w(p?.counts?.pending??0),m(5),w(p?.counts?.running??0),m(5),w(p?.counts?.succeeded??0),m(5),w(p?.counts?.failed??0),m(5),w(p?.counts?.cancelled??0);let g=i.store.progress();m(7),Me(``,g.instances.completed,` / `,g.instances.total),m(),S(`value`,i.progressValue(g.instances.completed,g.instances.total)),m(6),Me(``,g.algorithms.completed,` / `,g.algorithms.total),m(),S(`value`,i.progressValue(g.algorithms.completed,g.algorithms.total)),m(4),B(`Repetitions · `,g.repetitionScope),m(2),Me(``,g.repetitions.completed,` / `,g.repetitions.total),m(),S(`value`,i.progressValue(g.repetitions.completed,g.repetitions.total)),m(2),Me(` `,p?.runningInstances??0,` active instances · `,p?.runningAlgorithms??0,` active algorithms. Completed repetitions include failures; cancelled work does not count as completed. `),m(),D((o=i.store.lastUpdated())?97:-1,o),m(),D((r=i.store.estimatedRemainingMillis())?98:-1,r),m(),D(i.store.unseenIssues()>0?99:-1),m(),S(`selectedIndex`,i.store.tab()),m(11),D((s=i.store.resultError())?111:-1,s),m(),D((l=i.store.inspectedResult())?112:-1,l)}},dependencies:[Ai,ms,Ft,tn,zn,Bn,jn,im,Ul,nm,am,$l,di,Ti,na,Jr,Zr,ss,Ni,la,rs,fm,Ql,cs,ls,Pa,Oa,Un,bm,Xl,Zl,gm,_m,vm,Cm,Xi,En],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh;color:var(--%NS%mat-sys-on-surface);background:var(--%NS%mat-sys-surface-container-lowest)}.topbar[_ngcontent-%COMP%]{position:sticky;top:0;z-index:10;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:var(--%NS%mat-sys-surface);gap:.75rem}.logo[_ngcontent-%COMP%]{width:2.2rem;height:2.2rem}.brand[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-large);font-weight:700}.toolbar-spacer[_ngcontent-%COMP%]{flex:1}main[_ngcontent-%COMP%]{width:min(100% - 2rem,96rem);margin:auto;padding:1.5rem 0 4rem}.status-card[_ngcontent-%COMP%], .browser[_ngcontent-%COMP%], .issue[_ngcontent-%COMP%], .result-detail[_ngcontent-%COMP%]{margin:1rem 0}.summary[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(3,minmax(0,1fr)) minmax(13rem,1fr);gap:1rem;align-items:center}.summary[_ngcontent-%COMP%] > div[_ngcontent-%COMP%], .metrics[_ngcontent-%COMP%] > div[_ngcontent-%COMP%]{display:grid;gap:.3rem}.phase[_ngcontent-%COMP%]{text-transform:capitalize}.eyebrow[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-label-medium);text-transform:uppercase;color:var(--%NS%mat-sys-on-surface-variant);letter-spacing:.05em}.metrics[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(6,1fr);gap:1rem;padding:1.2rem 0}.metrics[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-headline-small)}.progress-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:1.5rem;border-top:1px solid var(--%NS%mat-sys-outline-variant);padding-top:1rem}.progress-item[_ngcontent-%COMP%]{display:grid;gap:.5rem}.progress-label[_ngcontent-%COMP%]{display:flex;justify-content:space-between;gap:.5rem;font:var(--%NS%mat-sys-label-large)}.status-footnote[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.85rem;line-height:1.5}.filters[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem;align-items:center;margin:1rem 0}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{flex:1;min-width:12rem}.table-scroll[_ngcontent-%COMP%]{overflow-x:auto}table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse;font:var(--%NS%mat-sys-body-medium)}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{text-align:left;padding:.65rem .75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);white-space:nowrap}th[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-large)}tr.selected[_ngcontent-%COMP%]{background:var(--%NS%mat-sys-secondary-container)}.chart-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem;margin-top:1rem}.detail-title[_ngcontent-%COMP%]{margin:1.5rem 0 1rem}small[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.85rem}.error-card[_ngcontent-%COMP%]{background:var(--%NS%mat-sys-error-container);color:var(--%NS%mat-sys-on-error-container);margin:1rem 0}.result-heading[_ngcontent-%COMP%]{display:flex;justify-content:space-between;align-items:center}.empty-state[_ngcontent-%COMP%]{min-height:8rem;display:grid;place-items:center;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}.sr-only[_ngcontent-%COMP%]{position:absolute;width:1px;height:1px;padding:0;overflow:hidden;clip:rect(0,0,0,0)}@media(max-width:60rem){.summary[_ngcontent-%COMP%]{grid-template-columns:repeat(2,minmax(0,1fr))}.metrics[_ngcontent-%COMP%]{grid-template-columns:repeat(3,1fr)}.progress-grid[_ngcontent-%COMP%]{grid-template-columns:1fr}}@media(max-width:42rem){main[_ngcontent-%COMP%]{width:calc(100% - 1rem);padding-top:.25rem}.chart-grid[_ngcontent-%COMP%], .summary[_ngcontent-%COMP%]{grid-template-columns:1fr}.brand[_ngcontent-%COMP%]{display:none}.topbar[_ngcontent-%COMP%]{gap:.25rem;padding:0 .5rem}.logo[_ngcontent-%COMP%]{width:1.5rem}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{min-width:100%}}`]})};var _P=new N(`MAT_BADGE_CONFIG`);var Uw=`mat-badge-content`;var yP=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=H({type:t,selectors:[[`ng-component`]],decls:0,vars:0,template:function(i,a){},styles:[`.mat-badge {
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
`],encapsulation:2})}return t})();var $w=(()=>{class t{_ngZone=f(Z);_elementRef=f(Y);_ariaDescriber=f(Ou);_renderer=f(Oe);_animationsDisabled=Ue();_idGenerator=f(Be);get color(){return this._color}set color(e){this._setColor(e),this._color=e}_color;overlap;disabled=!1;position;get content(){return this._content}set content(e){this._updateRenderedContent(e)}_content;get description(){return this._description}set description(e){this._updateDescription(e)}_description;size;hidden=!1;_badgeElement;_inlineBadgeDescription;_isInitialized=!1;_interactivityChecker=f(Br);_document=f(de);constructor(){let e=f(_P,{optional:!0}),i=f(Je);i.load(yP),i.load(Ii),this._color=e?.color||`primary`,this.overlap=e?.overlap??!0,this.position=e?.position||`above after`,this.size=e?.size||`medium`}isAbove(){return this.position.indexOf(`below`)===-1}isAfter(){return this.position.indexOf(`before`)===-1}getBadgeElement(){return this._badgeElement}ngOnInit(){this._clearExistingBadges(),this.content&&!this._badgeElement&&(this._badgeElement=this._createBadgeElement(),this._updateRenderedContent(this.content)),this._isInitialized=!0}ngAfterViewInit(){}ngOnDestroy(){this._renderer.destroyNode&&(this._renderer.destroyNode(this._badgeElement),this._inlineBadgeDescription?.remove()),this._ariaDescriber.removeDescription(this._elementRef.nativeElement,this.description)}_isHostInteractive(){return this._interactivityChecker.isFocusable(this._elementRef.nativeElement,{ignoreVisibility:!0})}_createBadgeElement(){let e=this._renderer.createElement(`span`),i=`mat-badge-active`;return e.setAttribute(`id`,this._idGenerator.getId(`mat-badge-content-`)),e.setAttribute(`aria-hidden`,`true`),e.classList.add(Uw),this._animationsDisabled&&e.classList.add(`_mat-animation-noopable`),this._elementRef.nativeElement.appendChild(e),typeof requestAnimationFrame==`function`&&!this._animationsDisabled?this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>{e.classList.add(i)})}):e.classList.add(i),e}_updateRenderedContent(e){let i=`${e??``}`.trim();this._isInitialized&&i&&!this._badgeElement&&(this._badgeElement=this._createBadgeElement()),this._badgeElement&&(this._badgeElement.textContent=i),this._content=i}_updateDescription(e){this._ariaDescriber.removeDescription(this._elementRef.nativeElement,this.description),(!e||this._isHostInteractive())&&this._removeInlineDescription(),this._description=e,this._isHostInteractive()?this._ariaDescriber.describe(this._elementRef.nativeElement,e):this._updateInlineDescription()}_updateInlineDescription(){this._inlineBadgeDescription||(this._inlineBadgeDescription=this._document.createElement(`span`),this._inlineBadgeDescription.classList.add(`cdk-visually-hidden`)),this._inlineBadgeDescription.textContent=this.description,this._badgeElement?.appendChild(this._inlineBadgeDescription)}_removeInlineDescription(){this._inlineBadgeDescription?.remove(),this._inlineBadgeDescription=void 0}_setColor(e){let i=this._elementRef.nativeElement.classList;i.remove(`mat-badge-${this._color}`),e&&i.add(`mat-badge-${e}`)}_clearExistingBadges(){let e=this._elementRef.nativeElement.querySelectorAll(`:scope > .${Uw}`);for(let i of Array.from(e))i!==this._badgeElement&&i.remove()}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`matBadge`,``]],hostAttrs:[1,`mat-badge`],hostVars:20,hostBindings:function(i,a){i&2&&G(`mat-badge-overlap`,a.overlap)(`mat-badge-above`,a.isAbove())(`mat-badge-below`,!a.isAbove())(`mat-badge-before`,!a.isAfter())(`mat-badge-after`,a.isAfter())(`mat-badge-small`,a.size===`small`)(`mat-badge-medium`,a.size===`medium`)(`mat-badge-large`,a.size===`large`)(`mat-badge-hidden`,a.hidden||!a.content)(`mat-badge-disabled`,a.disabled)},inputs:{color:[0,`matBadgeColor`,`color`],overlap:[2,`matBadgeOverlap`,`overlap`,z],disabled:[2,`matBadgeDisabled`,`disabled`,z],position:[0,`matBadgePosition`,`position`],content:[0,`matBadge`,`content`],description:[0,`matBadgeDescription`,`description`],size:[0,`matBadgeSize`,`size`],hidden:[2,`matBadgeHidden`,`hidden`,z]}})}return t})();var Gw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[Mo,we]})}return t})();var Ba=class{viewContainerRef;injector;id;role=`dialog`;panelClass=``;hasBackdrop=!0;backdropClass=``;disableClose=!1;closePredicate;width=``;height=``;minWidth;minHeight;maxWidth;maxHeight;positionStrategy;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus=`first-tabbable`;restoreFocus=!0;scrollStrategy;closeOnNavigation=!0;closeOnDestroy=!0;closeOnOverlayDetachments=!0;disableAnimations=!1;providers;container;templateContext;bindings};var Vb=(()=>{class t extends es{_elementRef=f(Y);_focusTrapFactory=f(Vg);_config;_interactivityChecker=f(Br);_ngZone=f(Z);_focusMonitor=f(en);_renderer=f(Oe);_changeDetectorRef=f(Fe);_injector=f(se);_platform=f(Re);_document=f(de);_portalOutlet;_focusTrapped=new V;_focusTrap=null;_elementFocusedBeforeDialogWasOpened=null;_closeInteractionType=null;_ariaLabelledByQueue=[];_isDestroyed=!1;constructor(){super(),this._config=f(Ba,{optional:!0})||new Ba,this._config.ariaLabelledBy&&this._ariaLabelledByQueue.push(this._config.ariaLabelledBy)}_addAriaLabelledBy(e){this._ariaLabelledByQueue.push(e),this._changeDetectorRef.markForCheck()}_removeAriaLabelledBy(e){let i=this._ariaLabelledByQueue.indexOf(e);i>-1&&(this._ariaLabelledByQueue.splice(i,1),this._changeDetectorRef.markForCheck())}_contentAttached(){this._initializeFocusTrap(),this._captureInitialFocus()}_captureInitialFocus(){this._trapFocus()}ngOnDestroy(){this._focusTrapped.complete(),this._isDestroyed=!0,this._restoreFocus()}attachComponentPortal(e){this._portalOutlet.hasAttached();let i=this._portalOutlet.attachComponentPortal(e);return this._contentAttached(),i}attachTemplatePortal(e){this._portalOutlet.hasAttached();let i=this._portalOutlet.attachTemplatePortal(e);return this._contentAttached(),i}attachDomPortal=e=>{this._portalOutlet.hasAttached();let i=this._portalOutlet.attachDomPortal(e);return this._contentAttached(),i};_recaptureFocus(){this._containsFocus()||this._trapFocus()}_forceFocus(e,i){this._interactivityChecker.isFocusable(e)||(e.tabIndex=-1,this._ngZone.runOutsideAngular(()=>{let a=()=>{o(),r(),e.removeAttribute(`tabindex`)},o=this._renderer.listen(e,`blur`,a),r=this._renderer.listen(e,`mousedown`,a)})),e.focus(i)}_focusByCssSelector(e,i){let a=this._elementRef.nativeElement.querySelector(e);a&&this._forceFocus(a,i)}_trapFocus(e){this._isDestroyed||Rt(()=>{let i=this._elementRef.nativeElement;switch(this._config.autoFocus){case!1:case`dialog`:this._containsFocus()||i.focus(e);break;case!0:case`first-tabbable`:this._focusTrap?.focusInitialElement(e)||this._focusDialogContainer(e);break;case`first-heading`:this._focusByCssSelector(`h1, h2, h3, h4, h5, h6, [role="heading"]`,e);break;default:this._focusByCssSelector(this._config.autoFocus,e)}this._focusTrapped.next()},{injector:this._injector})}_restoreFocus(){let e=this._config.restoreFocus,i=null;if(typeof e==`string`?i=this._document.querySelector(e):typeof e==`boolean`?i=e?this._elementFocusedBeforeDialogWasOpened:null:e&&(i=e),this._config.restoreFocus&&i&&typeof i.focus==`function`){let a=Dl(),o=this._elementRef.nativeElement;(!a||a===this._document.body||a===o||o.contains(a))&&(this._focusMonitor?(this._focusMonitor.focusVia(i,this._closeInteractionType),this._closeInteractionType=null):i.focus())}this._focusTrap&&this._focusTrap.destroy()}_focusDialogContainer(e){this._elementRef.nativeElement.focus?.(e)}_containsFocus(){let e=this._elementRef.nativeElement,i=Dl();return e===i||e.contains(i)}_initializeFocusTrap(){this._platform.isBrowser&&(this._focusTrap=this._focusTrapFactory.create(this._elementRef.nativeElement),this._document&&(this._elementFocusedBeforeDialogWasOpened=Dl()))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(i,a){}return H({type:t,selectors:[[`cdk-dialog-container`]],viewQuery:function(a,o){if(a&1&&Ze(Aa,7),a&2){let r;J(r=ee())&&(o._portalOutlet=r.first)}},hostAttrs:[`tabindex`,`-1`,1,`cdk-dialog-container`],hostVars:6,hostBindings:function(a,o){a&2&&ne(`id`,o._config.id||null)(`role`,o._config.role)(`aria-modal`,o._config.ariaModal)(`aria-labelledby`,o._config.ariaLabel?null:o._ariaLabelledByQueue[0])(`aria-label`,o._config.ariaLabel)(`aria-describedby`,o._config.ariaDescribedBy||null)},features:[Ve],decls:1,vars:0,consts:[[`cdkPortalOutlet`,``]],template:function(a,o){a&1&&rt(0,e,0,0,`ng-template`,0)},dependencies:[Aa],styles:[`.cdk-dialog-container {
  display: block;
  width: 100%;
  height: 100%;
  min-height: inherit;
  max-height: inherit;
}
`],encapsulation:2,changeDetection:1})})()}return t})();var zo=class{overlayRef;config;componentInstance=null;componentRef=null;containerInstance;disableClose;closed=new V;backdropClick;keydownEvents;outsidePointerEvents;id;_detachSubscription;constructor(n,e){this.overlayRef=n,this.config=e,this.disableClose=e.disableClose,this.backdropClick=n.backdropClick(),this.keydownEvents=n.keydownEvents(),this.outsidePointerEvents=n.outsidePointerEvents(),this.id=e.id,this.keydownEvents.subscribe(i=>{i.keyCode===27&&!this.disableClose&&!Ct(i)&&(i.preventDefault(),this.close(void 0,{focusOrigin:`keyboard`}))}),this.backdropClick.subscribe(()=>{!this.disableClose&&this._canClose()?this.close(void 0,{focusOrigin:`mouse`}):this.containerInstance._recaptureFocus?.()}),this._detachSubscription=n.detachments().subscribe(()=>{e.closeOnOverlayDetachments!==!1&&this.close()})}close(n,e){if(this._canClose(n)){let i=this.closed;this.containerInstance._closeInteractionType=e?.focusOrigin||`program`,this._detachSubscription.unsubscribe(),this.overlayRef.dispose(),i.next(n),i.complete(),this.componentInstance=this.containerInstance=null}}updatePosition(){return this.overlayRef.updatePosition(),this}updateSize(n=``,e=``){return this.overlayRef.updateSize({width:n,height:e}),this}addPanelClass(n){return this.overlayRef.addPanelClass(n),this}removePanelClass(n){return this.overlayRef.removePanelClass(n),this}_canClose(n){let e=this.config;return!!this.containerInstance&&(!e.closePredicate||e.closePredicate(n,e,this.componentInstance))}};var CP=new N(`DialogScrollStrategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>is(t)}});var SP=new N(`DialogData`);var wP=new N(`DefaultDialogConfig`);function EP(t){let n=k(t),e=new te;return{valueSignal:n,get value(){return n()},change:e,ngOnDestroy(){e.complete()}}}var Bb=(()=>{class t{_injector=f(se);_defaultOptions=f(wP,{optional:!0});_parentDialog=f(t,{optional:!0,skipSelf:!0});_overlayContainer=f(um);_idGenerator=f(Be);_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new V;_afterOpenedAtThisLevel=new V;_ariaHiddenElements=new Map;_scrollStrategy=f(CP);get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}afterAllClosed=Ka(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(mt(void 0)));open(e,i){let a=this._defaultOptions||new Ba;i=r(r({},a),i),i.id=i.id||this._idGenerator.getId(`cdk-dialog-`),i.id&&this.getDialogById(i.id);let o=this._getOverlayConfig(i),r$3=sa(this._injector,o),s=new zo(r$3,i),l=this._attachContainer(r$3,s,i);if(s.containerInstance=l,!this.openDialogs.length){let h=this._overlayContainer.getContainerElement();l._focusTrapped?l._focusTrapped.pipe(vn(1)).subscribe(()=>{this._hideNonDialogContentFromAssistiveTechnology(h)}):this._hideNonDialogContentFromAssistiveTechnology(h)}return this._attachDialogContent(e,s,l,i),this.openDialogs.push(s),s.closed.subscribe(()=>this._removeOpenDialog(s,!0)),this.afterOpened.next(s),s}closeAll(){Lb(this.openDialogs,e=>e.close())}getDialogById(e){return this.openDialogs.find(i=>i.id===e)}ngOnDestroy(){Lb(this._openDialogsAtThisLevel,e=>{e.config.closeOnDestroy===!1&&this._removeOpenDialog(e,!1)}),Lb(this._openDialogsAtThisLevel,e=>e.close()),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete(),this._openDialogsAtThisLevel=[]}_getOverlayConfig(e){let i=new aa({positionStrategy:e.positionStrategy||as().centerHorizontally().centerVertically(),scrollStrategy:e.scrollStrategy||this._scrollStrategy(),panelClass:e.panelClass,hasBackdrop:e.hasBackdrop,direction:e.direction,minWidth:e.minWidth,minHeight:e.minHeight,maxWidth:e.maxWidth,maxHeight:e.maxHeight,width:e.width,height:e.height,disposeOnNavigation:e.closeOnNavigation,disableAnimations:e.disableAnimations});return e.backdropClass&&(i.backdropClass=e.backdropClass),i}_attachContainer(e,i,a){let o=a.injector||a.viewContainerRef?.injector,r=[{provide:Ba,useValue:a},{provide:zo,useValue:i},{provide:ts,useValue:e}],s;a.container?typeof a.container==`function`?s=a.container:(s=a.container.type,r.push(...a.container.providers(a))):s=Vb;let l=new Na(s,a.viewContainerRef,se.create({parent:o||this._injector,providers:r}));return e.attach(l).instance}_attachDialogContent(e,i,a,o){if(e instanceof Vt){let r$4=this._createInjector(o,i,a,void 0),s={$implicit:o.data,dialogRef:i};o.templateContext&&(s=r(r({},s),typeof o.templateContext==`function`?o.templateContext():o.templateContext)),a.attachTemplatePortal(new Hn(e,null,s,r$4))}else{let r=this._createInjector(o,i,a,this._injector),s=a.attachComponentPortal(new Na(e,o.viewContainerRef,r,null,o.bindings));i.componentRef=s,i.componentInstance=s.instance}}_createInjector(e,i,a,o){let r=e.injector||e.viewContainerRef?.injector,s=[{provide:SP,useValue:e.data},{provide:zo,useValue:i}];return e.providers&&(typeof e.providers==`function`?s.push(...e.providers(i,e,a)):s.push(...e.providers)),e.direction&&(!r||!r.get(gt,null,{optional:!0}))&&s.push({provide:gt,useValue:EP(e.direction)}),se.create({parent:r||o,providers:s})}_removeOpenDialog(e,i){let a=this.openDialogs.indexOf(e);a>-1&&(this.openDialogs.splice(a,1),this.openDialogs.length||(this._ariaHiddenElements.forEach((o,r)=>{o?r.setAttribute(`aria-hidden`,o):r.removeAttribute(`aria-hidden`)}),this._ariaHiddenElements.clear(),i&&this._getAfterAllClosed().next()))}_hideNonDialogContentFromAssistiveTechnology(e){if(e.parentElement){let i=e.parentElement.children;for(let a=i.length-1;a>-1;a--){let o=i[a];o!==e&&o.nodeName!==`SCRIPT`&&o.nodeName!==`STYLE`&&!o.hasAttribute(`aria-live`)&&!o.hasAttribute(`popover`)&&(this._ariaHiddenElements.set(o,o.getAttribute(`aria-hidden`)),o.setAttribute(`aria-hidden`,`true`))}}}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();function Lb(t,n){let e=t.length;for(;e--;)n(t[e])}var Ww=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({providers:[Bb],imports:[mi,Lo,Mo,Lo]})}return t})();var Em=class{viewContainerRef;injector;id;role=`dialog`;panelClass=``;hasBackdrop=!0;backdropClass=``;disableClose=!1;closePredicate;width=``;height=``;minWidth;minHeight;maxWidth;maxHeight;position;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus=`first-tabbable`;restoreFocus=!0;delayFocusTrap=!0;scrollStrategy;closeOnNavigation=!0;enterAnimationDuration;exitAnimationDuration;bindings};var jb=`mdc-dialog--open`;var qw=`mdc-dialog--opening`;var Yw=`mdc-dialog--closing`;var DP=150;var IP=75;var kP=(()=>{class t extends Vb{_animationStateChanged=new te;_animationsEnabled=!Ue();_actionSectionCount=0;_hostElement=this._elementRef.nativeElement;_enterAnimationDuration=this._animationsEnabled?Qw(this._config.enterAnimationDuration)??DP:0;_exitAnimationDuration=this._animationsEnabled?Qw(this._config.exitAnimationDuration)??IP:0;_animationTimer=null;_contentAttached(){super._contentAttached(),this._startOpenAnimation()}_startOpenAnimation(){this._animationStateChanged.emit({state:`opening`,totalTime:this._enterAnimationDuration}),this._animationsEnabled?(this._hostElement.style.setProperty(Kw,`${this._enterAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add(qw,jb)),this._waitForAnimationToComplete(this._enterAnimationDuration,this._finishDialogOpen)):(this._hostElement.classList.add(jb),Promise.resolve().then(()=>this._finishDialogOpen()))}_startExitAnimation(){this._animationStateChanged.emit({state:`closing`,totalTime:this._exitAnimationDuration}),this._hostElement.classList.remove(jb),this._animationsEnabled?(this._hostElement.style.setProperty(Kw,`${this._exitAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add(Yw)),this._waitForAnimationToComplete(this._exitAnimationDuration,this._finishDialogClose)):Promise.resolve().then(()=>this._finishDialogClose())}_updateActionSectionCount(e){this._actionSectionCount+=e,this._changeDetectorRef.markForCheck()}_finishDialogOpen=()=>{this._clearAnimationClasses(),this._openAnimationDone(this._enterAnimationDuration)};_finishDialogClose=()=>{this._clearAnimationClasses(),this._animationStateChanged.emit({state:`closed`,totalTime:this._exitAnimationDuration})};_clearAnimationClasses(){this._hostElement.classList.remove(qw,Yw)}_waitForAnimationToComplete(e,i){this._animationTimer!==null&&clearTimeout(this._animationTimer),this._animationTimer=setTimeout(i,e)}_requestAnimationFrame(e){this._ngZone.runOutsideAngular(()=>{typeof requestAnimationFrame==`function`?requestAnimationFrame(e):e()})}_captureInitialFocus(){this._config.delayFocusTrap||this._trapFocus()}_openAnimationDone(e){this._config.delayFocusTrap&&this._trapFocus(),this._animationStateChanged.next({state:`opened`,totalTime:e})}ngOnDestroy(){super.ngOnDestroy(),this._animationTimer!==null&&clearTimeout(this._animationTimer)}attachComponentPortal(e){let i=super.attachComponentPortal(e);return i.location.nativeElement.classList.add(`mat-mdc-dialog-component-host`),i}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵcmp=(function(){function e(i,a){}return H({type:t,selectors:[[`mat-dialog-container`]],hostAttrs:[`tabindex`,`-1`,1,`mat-mdc-dialog-container`,`mdc-dialog`],hostVars:10,hostBindings:function(a,o){a&2&&(qt(`id`,o._config.id),ne(`aria-modal`,o._config.ariaModal)(`role`,o._config.role)(`aria-labelledby`,o._config.ariaLabel?null:o._ariaLabelledByQueue[0])(`aria-label`,o._config.ariaLabel)(`aria-describedby`,o._config.ariaDescribedBy||null),G(`_mat-animation-noopable`,!o._animationsEnabled)(`mat-mdc-dialog-container-with-actions`,o._actionSectionCount>0))},features:[Ve],decls:3,vars:0,consts:[[1,`mat-mdc-dialog-inner-container`,`mdc-dialog__container`],[1,`mat-mdc-dialog-surface`,`mdc-dialog__surface`],[`cdkPortalOutlet`,``]],template:function(a,o){a&1&&(c(0,`div`,0)(1,`div`,1),rt(2,e,0,0,`ng-template`,2),d()())},dependencies:[Aa],styles:[`.mat-mdc-dialog-container {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var Kw=`--mat-dialog-transition-duration`;function Qw(t){return t==null?null:typeof t==`number`?t:t.endsWith(`ms`)?ri(t.substring(0,t.length-2)):t.endsWith(`s`)?ri(t.substring(0,t.length-1))*1e3:t===`0`?0:null}var wm=(function(t){return t[t.OPEN=0]=`OPEN`,t[t.CLOSING=1]=`CLOSING`,t[t.CLOSED=2]=`CLOSED`,t})(wm||{});var tc=class{_ref;_config;_containerInstance;componentInstance;componentRef=null;disableClose;id;_afterOpened=new Yn(1);_beforeClosed=new Yn(1);_result;_closeFallbackTimeout;_state=wm.OPEN;_closeInteractionType;constructor(n,e,i){this._ref=n,this._config=e,this._containerInstance=i,this.disableClose=e.disableClose,this.id=n.id,n.addPanelClass(`mat-mdc-dialog-panel`),i._animationStateChanged.pipe(Qe(a=>a.state===`opened`),vn(1)).subscribe(()=>{this._afterOpened.next(),this._afterOpened.complete()}),i._animationStateChanged.pipe(Qe(a=>a.state===`closed`),vn(1)).subscribe(()=>{clearTimeout(this._closeFallbackTimeout),this._finishDialogClose()}),n.overlayRef.detachments().subscribe(()=>{this._beforeClosed.next(this._result),this._beforeClosed.complete(),this._finishDialogClose()}),kt(this.backdropClick(),this.keydownEvents().pipe(Qe(a=>a.keyCode===27&&!this.disableClose&&!Ct(a)))).subscribe(a=>{this.disableClose||(a.preventDefault(),Xw(this,a.type===`keydown`?`keyboard`:`mouse`))})}close(n){let e=this._config.closePredicate;e&&!e(n,this._config,this.componentInstance)||(this._result=n,this._containerInstance._animationStateChanged.pipe(Qe(i=>i.state===`closing`),vn(1)).subscribe(i=>{this._beforeClosed.next(n),this._beforeClosed.complete(),this._ref.overlayRef.detachBackdrop(),this._closeFallbackTimeout=setTimeout(()=>this._finishDialogClose(),i.totalTime+100)}),this._state=wm.CLOSING,this._containerInstance._startExitAnimation())}afterOpened(){return this._afterOpened}afterClosed(){return this._ref.closed}beforeClosed(){return this._beforeClosed}backdropClick(){return this._ref.backdropClick}keydownEvents(){return this._ref.keydownEvents}updatePosition(n){let e=this._ref.config.positionStrategy;return n&&(n.left||n.right)?n.left?e.left(n.left):e.right(n.right):e.centerHorizontally(),n&&(n.top||n.bottom)?n.top?e.top(n.top):e.bottom(n.bottom):e.centerVertically(),this._ref.updatePosition(),this}updateSize(n=``,e=``){return this._ref.updateSize(n,e),this}addPanelClass(n){return this._ref.addPanelClass(n),this}removePanelClass(n){return this._ref.removePanelClass(n),this}getState(){return this._state}_finishDialogClose(){this._state=wm.CLOSED,this._ref.close(this._result,{focusOrigin:this._closeInteractionType}),this.componentInstance=null}};function Xw(t,n,e){return t._closeInteractionType=n,t.close(e)}var nc=new N(`MatMdcDialogData`);var MP=new N(`mat-mdc-dialog-default-options`);var TP=new N(`mat-mdc-dialog-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(se);return()=>is(t)}});var Ho=(()=>{class t{_defaultOptions=f(MP,{optional:!0});_scrollStrategy=f(TP);_parentDialog=f(t,{optional:!0,skipSelf:!0});_idGenerator=f(Be);_injector=f(se);_dialog=f(Bb);_animationsDisabled=Ue();_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new V;_afterOpenedAtThisLevel=new V;dialogConfigClass=Em;_dialogRefConstructor;_dialogContainerType;_dialogDataToken;get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}afterAllClosed=Ka(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(mt(void 0)));constructor(){this._dialogRefConstructor=tc,this._dialogContainerType=kP,this._dialogDataToken=nc}open(e,i){let a;i=r(r({},this._defaultOptions||new Em),i),i.id=i.id||this._idGenerator.getId(`mat-mdc-dialog-`),i.scrollStrategy=i.scrollStrategy||this._scrollStrategy();let o=this._dialog.open(e,s(r({},i),{positionStrategy:as(this._injector).centerHorizontally().centerVertically(),disableClose:!0,closePredicate:void 0,closeOnDestroy:!1,closeOnOverlayDetachments:!1,disableAnimations:this._animationsDisabled||i.enterAnimationDuration?.toLocaleString()===`0`||i.exitAnimationDuration?.toString()===`0`,container:{type:this._dialogContainerType,providers:()=>[{provide:this.dialogConfigClass,useValue:i},{provide:Ba,useValue:i}]},templateContext:()=>({dialogRef:a}),providers:(r,s,l)=>(a=new this._dialogRefConstructor(r,i,l),a.updatePosition(i?.position),[{provide:this._dialogContainerType,useValue:l},{provide:this._dialogDataToken,useValue:s.data},{provide:this._dialogRefConstructor,useValue:a},{provide:zo,useValue:null}])}));return a.componentRef=o.componentRef,a.componentInstance=o.componentInstance,this.openDialogs.push(a),this.afterOpened.next(a),a.afterClosed().subscribe(()=>{let r=this.openDialogs.indexOf(a);r>-1&&(this.openDialogs.splice(r,1),this.openDialogs.length||this._getAfterAllClosed().next())}),a}closeAll(){this._closeDialogs(this.openDialogs)}getDialogById(e){return this.openDialogs.find(i=>i.id===e)}ngOnDestroy(){this._closeDialogs(this._openDialogsAtThisLevel),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete()}_closeDialogs(e){let i=e.length;for(;i--;)e[i].close()}static ɵfac=function(i){return new(i||t)};static ɵprov=le({token:t,factory:t.ɵfac})}return t})();var Dm=(()=>{class t{dialogRef=f(tc,{optional:!0});_elementRef=f(Y);_dialog=f(Ho);ariaLabel;type=`button`;dialogResult;_matDialogClose;ngOnInit(){this.dialogRef||(this.dialogRef=Jw(this._elementRef,this._dialog.openDialogs))}ngOnChanges(e){let i=e._matDialogClose;i&&(this.dialogResult=i.currentValue)}_onButtonClick(e){this._elementRef.nativeElement.getAttribute(`aria-disabled`)!==`true`&&Xw(this.dialogRef,e.screenX===0&&e.screenY===0?`keyboard`:`mouse`,this.dialogResult)}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`mat-dialog-close`,``],[``,`matDialogClose`,``]],hostVars:2,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._onButtonClick(r)}),i&2&&ne(`aria-label`,a.ariaLabel||null)(`type`,a.type)},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],type:`type`,dialogResult:[0,`mat-dialog-close`,`dialogResult`],_matDialogClose:[0,`matDialogClose`,`_matDialogClose`]},exportAs:[`matDialogClose`],features:[_t]})}return t})();var Zw=(()=>{class t{_dialogRef=f(tc,{optional:!0});_elementRef=f(Y);_dialog=f(Ho);ngOnInit(){this._dialogRef||(this._dialogRef=Jw(this._elementRef,this._dialog.openDialogs)),this._dialogRef&&Promise.resolve().then(()=>{this._onAdd()})}ngOnDestroy(){this._dialogRef?._containerInstance&&Promise.resolve().then(()=>{this._onRemove()})}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t})}return t})();var Im=(()=>{class t extends Zw{id=f(Be).getId(`mat-mdc-dialog-title-`);_onAdd(){this._dialogRef._containerInstance?._addAriaLabelledBy?.(this.id)}_onRemove(){this._dialogRef?._containerInstance?._removeAriaLabelledBy?.(this.id)}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`mat-dialog-title`,``],[``,`matDialogTitle`,``]],hostAttrs:[1,`mat-mdc-dialog-title`,`mdc-dialog__title`],hostVars:1,hostBindings:function(i,a){i&2&&qt(`id`,a.id)},inputs:{id:`id`},exportAs:[`matDialogTitle`],features:[Ve]})}return t})();var km=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[``,`mat-dialog-content`,``],[`mat-dialog-content`],[``,`matDialogContent`,``]],hostAttrs:[1,`mat-mdc-dialog-content`,`mdc-dialog__content`],features:[rl([ql])]})}return t})();var Mm=(()=>{class t extends Zw{align;_onAdd(){this._dialogRef._containerInstance?._updateActionSectionCount?.(1)}_onRemove(){this._dialogRef._containerInstance?._updateActionSectionCount?.(-1)}static ɵfac=(()=>{let e;return function(a){return(e||(e=At(t)))(a||t)}})();static ɵdir=Q({type:t,selectors:[[``,`mat-dialog-actions`,``],[`mat-dialog-actions`],[``,`matDialogActions`,``]],hostAttrs:[1,`mat-mdc-dialog-actions`,`mdc-dialog__actions`],hostVars:6,hostBindings:function(i,a){i&2&&G(`mat-mdc-dialog-actions-align-start`,a.align===`start`)(`mat-mdc-dialog-actions-align-center`,a.align===`center`)(`mat-mdc-dialog-actions-align-end`,a.align===`end`)},inputs:{align:`align`},features:[Ve]})}return t})();function Jw(t,n){let e=t.nativeElement.parentElement;for(;e&&!e.classList.contains(`mat-mdc-dialog-container`);)e=e.parentElement;return e?n.find(i=>i.id===e.id):null}var Tm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({providers:[Ho],imports:[Ww,mi,Lo,we]})}return t})();var Nm=class t{http=f(Di);getStatus(){return wt(this.http.get(this.endpoint(`/status`)))}getComponentUsage(n){return wt(this.http.get(this.endpoint(`/components/usage`),{params:{scope:n}}))}getComponentCandidates(n){return wt(this.http.get(this.endpoint(`/components/candidates`),{params:r({scope:n.scope,basis:n.basis,offset:n.offset,limit:n.limit},n.selector)}))}getEvaluationChanges(n,e){return wt(this.http.get(this.endpoint(`/evaluations/changes`),{params:{after:n,limit:e}}))}getElites(){return wt(this.http.get(this.endpoint(`/elites`)))}getEliteHistory(){return wt(this.http.get(this.endpoint(`/elites/history`)))}getCandidate(n){return wt(this.http.get(this.endpoint(`/candidates/${encodeURIComponent(n)}`)))}getSearchSpace(){return wt(this.http.get(this.endpoint(`/search-space`)))}getArtifacts(){return wt(this.http.get(this.endpoint(`/artifacts`)))}artifactUrl(n){return this.endpoint(`/artifacts/${encodeURIComponent(n)}`)}endpoint(n){return`${In.apiBaseUrl}/api/autoconfig${n}`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var Uo=class{static evaluationCost(n){let e=[],i=[];for(let a of n){if(a.state!==`SUCCEEDED`||typeof a.cost!=`number`||!Number.isFinite(a.cost))continue;let o={x:a.id,y:a.cost,custom:{configurationId:a.configurationId,instanceName:a.instanceName,timeSeconds:a.timeSeconds}};(a.slow?i:e).push(o)}return{options:this.baseOptions(`Evaluation cost`,`Successful IRACE costs. Lower is better.`,{allowDecimals:!1,title:{text:`Evaluation ID`}},{title:{text:`IRACE cost`}},[{type:`scatter`,name:`Succeeded`,data:e,color:`#00aab5`,marker:{radius:2.5},turboThreshold:0},{type:`scatter`,name:`Slow`,data:i,color:`#ed8e00`,marker:{radius:3.5,symbol:`diamond`},turboThreshold:0}],`<b>{series.name}</b><br/>Evaluation: {point.x}<br/>Cost: {point.y}<br/>Configuration: {point.custom.configurationId}<br/>Instance: {point.custom.instanceName}`)}}static evaluationActivity(n){let e=n.filter(s=>s.finishedAt&&s.state!==`RUNNING`).slice().sort((s,l)=>{let h=Date.parse(s.finishedAt)-Date.parse(l.finishedAt);return h===0?s.id-l.id:h}),i={SUCCEEDED:0,REJECTED:0,FAILED:0},a=[],o=[],r=[];for(let s of e){let l=s.state;if(l!==`SUCCEEDED`&&l!==`REJECTED`&&l!==`FAILED`)continue;i[l]++;let h=[Date.parse(s.finishedAt),i[l]];l===`SUCCEEDED`?a.push(h):l===`REJECTED`?o.push(h):r.push(h)}return{options:this.baseOptions(`Evaluation activity`,`Cumulative terminal evaluations over wall-clock time.`,{type:`datetime`,title:{text:`Completion time`}},{allowDecimals:!1,min:0,title:{text:`Evaluations`}},[{type:`line`,name:`Succeeded`,data:a,color:`#079447`,marker:{enabled:!1}},{type:`line`,name:`Rejected`,data:o,color:`#ed8e00`,marker:{enabled:!1}},{type:`line`,name:`Failed`,data:r,color:`#d32f2f`,marker:{enabled:!1}}],`<b>{series.name}</b><br/>{point.x:%Y-%m-%d %H:%M:%S}<br/>Count: {point.y}`)}}static eliteRanks(n){let e=n?.iterations??[],i=[];for(let o of e)for(let r of o.elites)i.includes(r.configurationId)||i.push(r.configurationId);let a=[];for(let o of i){let r=[];for(let s of e){let l=null;for(let h of s.elites)if(h.configurationId===o){l=h.position;break}r.push([s.iteration,l])}a.push({type:`line`,name:o,data:r,connectNulls:!1,marker:{enabled:!0,radius:2.5},step:`left`})}return{options:this.baseOptions(`Elite rank evolution`,`Elite positions at completed IRACE race checkpoints.`,{allowDecimals:!1,title:{text:`Race checkpoint`}},{allowDecimals:!1,min:1,reversed:!0,title:{text:`Elite position`}},a,`<b>{series.name}</b><br/>Checkpoint: {point.x}<br/>Position: {point.y}`)}}static instanceSummaries(n){let e=new Map;for(let i of n){let a=e.get(i.instanceName)??{instanceName:i.instanceName,evaluations:0,bestCost:null,issues:0,slow:0},o=a.bestCost;i.state===`SUCCEEDED`&&typeof i.cost==`number`&&Number.isFinite(i.cost)&&(o===null||i.cost<o)&&(o=i.cost),e.set(i.instanceName,{instanceName:a.instanceName,evaluations:a.evaluations+1,bestCost:o,issues:a.issues+(i.state===`REJECTED`||i.state===`FAILED`?1:0),slow:a.slow+(i.slow?1:0)})}return[...e.values()].sort((i,a)=>a.issues-i.issues||i.instanceName.localeCompare(a.instanceName))}static baseOptions(n,e,i,a,o,r){return{accessibility:{enabled:!0,description:e},boost:{enabled:!0,useGPUTranslations:!0,seriesThreshold:20},chart:{animation:!1,height:330,spacing:[12,12,12,12]},credits:{enabled:!1},exporting:{enabled:!0},legend:{enabled:!0,maxHeight:72},title:{text:n,style:{display:`none`}},xAxis:i,yAxis:a,tooltip:{headerFormat:``,pointFormat:r},plotOptions:{series:{animation:!1,connectNulls:!1}},series:o}}};var NP=500;var AP=2e3;var RP=1e4;var OP=1e4;var zb=`mork.autoconfig.alertSeen.v1`;var ja=class t{client=f(Nm);destroyRef=f(Ae);statusSignal=k(null);componentUsageSignal=k(null);componentScopeSignal=k(`ALL`);componentErrorSignal=k(null);syncStateSignal=k(`detecting`);latestErrorSignal=k(null);evaluationsSignal=k([]);elitesSignal=k(null);eliteHistorySignal=k(null);searchSpaceSignal=k(null);artifactsSignal=k(null);alertsSignal=k([]);seenAlertRevisionSignal=k(0);runFailureSeenSignal=k(!1);lastUpdatedSignal=k(null);evaluationMap=new Map;candidateCache=new Map;pollTimer=null;currentRunId=null;evaluationRevision=0;eliteMarker=null;componentMarker=null;componentRequest=0;searchSpaceLoaded=!1;lastArtifactFetch=0;lastArtifactPhase=null;started=!1;destroyed=!1;status=this.statusSignal.asReadonly();componentUsage=this.componentUsageSignal.asReadonly();componentScope=this.componentScopeSignal.asReadonly();componentError=this.componentErrorSignal.asReadonly();syncState=this.syncStateSignal.asReadonly();latestError=this.latestErrorSignal.asReadonly();evaluations=this.evaluationsSignal.asReadonly();elites=this.elitesSignal.asReadonly();eliteHistory=this.eliteHistorySignal.asReadonly();searchSpace=this.searchSpaceSignal.asReadonly();artifacts=this.artifactsSignal.asReadonly();alerts=this.alertsSignal.asReadonly();lastUpdated=this.lastUpdatedSignal.asReadonly();viewMode=K(()=>{let n=this.statusSignal();return n?n.mode===`DISABLED`?`standard`:n.role===`WORKER`?`worker`:`tuning`:this.syncStateSignal()===`retrying`?`unavailable`:`detecting`});connectionLabel=K(()=>{switch(this.syncStateSignal()){case`detecting`:return`Detecting run mode`;case`synchronizing`:return`Synchronizing`;case`live`:return`Live`;case`retrying`:return`Reconnecting`}});budgetPercent=K(()=>{let n=this.statusSignal()?.budget;return!n||n.maximum<=0?0:Math.min(100,n.used/n.maximum*100)});evaluationCostChart=K(()=>Uo.evaluationCost(this.evaluationsSignal()));evaluationActivityChart=K(()=>Uo.evaluationActivity(this.evaluationsSignal()));eliteRankChart=K(()=>Uo.eliteRanks(this.eliteHistorySignal()));instanceSummaries=K(()=>Uo.instanceSummaries(this.evaluationsSignal()));recentEvaluations=K(()=>this.evaluationsSignal().slice(-100).reverse());unseenAlertCount=K(()=>{let n=0;for(let e of this.alertsSignal())e.revision===null?this.runFailureSeenSignal()||n++:e.revision>this.seenAlertRevisionSignal()&&n++;return n});constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.clearPollTimer()})}start(){this.started||(this.started=!0,this.schedulePoll(0))}retryNow(){this.clearPollTimer(),this.schedulePoll(0)}markAlertsSeen(){this.seenAlertRevisionSignal.set(this.evaluationRevision),this.statusSignal()?.failure&&this.runFailureSeenSignal.set(!0),this.persistSeenAlerts()}async loadCandidate(n){let e=this.candidateCache.get(n);if(e)return e;let i=this.currentRunId,a=await this.client.getCandidate(n);if(i!==this.currentRunId)throw new Error(`Candidate belongs to an obsolete autoconfig run`);return(this.statusSignal()?.state===`COMPLETED`||this.statusSignal()?.state===`FAILED`)&&this.candidateCache.set(n,a),a}selectComponentScope(n){if(n===this.componentScopeSignal())return;this.componentScopeSignal.set(n),this.componentUsageSignal.set(null),this.componentMarker=null;let e=this.statusSignal();e&&this.synchronizeComponentUsage(e)}loadComponentCandidates(n){return this.client.getComponentCandidates(n)}refreshComponentUsage(){this.componentMarker=null;let n=this.statusSignal();n&&this.synchronizeComponentUsage(n)}async refreshArtifacts(){let n=this.statusSignal();if(!n?.runId||n.role!==`COORDINATOR`){this.artifactsSignal.set(null);return}try{this.artifactsSignal.set(await this.client.getArtifacts()),this.lastArtifactFetch=Date.now()}catch(e){if(this.isNotFound(e)){this.artifactsSignal.set({runId:n.runId,artifacts:[]}),this.lastArtifactFetch=Date.now();return}throw e}}artifactUrl(n){return this.client.artifactUrl(n)}schedulePoll(n){this.destroyed||(this.pollTimer=setTimeout(()=>{this.poll()},n))}async poll(){this.pollTimer=null,this.syncStateSignal.set(`synchronizing`);try{let n=await this.client.getStatus();this.adoptStatus(n),n.mode!==`DISABLED`&&n.role===`COORDINATOR`&&n.runId&&await this.synchronizeRun(n),this.latestErrorSignal.set(null),this.lastUpdatedSignal.set(new Date),this.syncStateSignal.set(`live`)}catch(n){this.latestErrorSignal.set(this.errorMessage(n)),this.syncStateSignal.set(`retrying`)}finally{let n=this.statusSignal(),e=n?.state===`PREPARING`||n?.state===`RUNNING`;this.schedulePoll(e?AP:RP)}}adoptStatus(n){n.runId!==this.currentRunId?this.resetRun(n.runId):n.latestEvaluationRevision<this.evaluationRevision&&this.resetRun(n.runId),this.statusSignal.set(n),this.publishAlerts()}async synchronizeRun(n){await this.synchronizeEvaluations(n),await this.synchronizeElites(n),await this.synchronizeSearchSpace(n),await this.synchronizeComponentUsage(n),await this.synchronizeArtifacts(n)}async synchronizeEvaluations(n){let e=!1;for(;this.evaluationRevision<n.latestEvaluationRevision;){let i=await this.client.getEvaluationChanges(this.evaluationRevision,NP);if(i.runId!==n.runId)throw new Error(`Evaluation changes belong to a different autoconfig run`);for(let a of i.changes)this.evaluationMap.set(a.evaluation.id,{revision:a.revision,evaluation:a.evaluation}),e=!0;if(i.nextRevision<=this.evaluationRevision)break;this.evaluationRevision=i.nextRevision}if(e){let i=[...this.evaluationMap.values()].sort((a,o)=>a.evaluation.id-o.evaluation.id).map(a=>a.evaluation);this.evaluationsSignal.set(i),this.publishAlerts()}}async synchronizeElites(n){let e=`${n.irace.iteration??``}|${n.irace.updatedAt??``}|${n.irace.finalSnapshot}`;if(e===this.eliteMarker)return;let[i,a]=await Promise.all([this.client.getElites(),this.client.getEliteHistory()]);if(i.runId!==n.runId||a.runId!==n.runId)throw new Error(`Elite snapshots belong to a different autoconfig run`);this.elitesSignal.set(i),this.eliteHistorySignal.set(a),this.eliteMarker=e}async synchronizeSearchSpace(n){if(!(n.mode!==`AUTOCONFIG`||n.generatedParameterCount<=0||this.searchSpaceLoaded))try{this.searchSpaceSignal.set(await this.client.getSearchSpace()),this.searchSpaceLoaded=!0}catch(e){if(!this.isNotFound(e))throw e}}async synchronizeComponentUsage(n){if(n.mode!==`AUTOCONFIG`||n.role!==`COORDINATOR`||!n.runId)return;let e=this.componentScopeSignal(),i=`${n.runId}|${e}|${n.latestEvaluationRevision}|${n.irace.updatedAt??``}|${n.irace.finalSnapshot}`;if(i===this.componentMarker)return;let a=++this.componentRequest;try{let o=await this.client.getComponentUsage(e);if(this.destroyed||a!==this.componentRequest||e!==this.componentScopeSignal())return;if(o.runId!==this.currentRunId||o.scope!==e)throw new Error(`Component usage belongs to a different run or scope`);this.componentUsageSignal.set(o),this.componentMarker=i,this.componentErrorSignal.set(null)}catch(o){a===this.componentRequest&&!this.destroyed&&this.componentErrorSignal.set(this.errorMessage(o))}}async synchronizeArtifacts(n){let e=Date.now();n.phase===this.lastArtifactPhase&&e-this.lastArtifactFetch<OP||(await this.refreshArtifacts(),this.lastArtifactPhase=n.phase)}publishAlerts(){let n=[],e=this.statusSignal();e?.failure&&n.push({key:`run:${e.runId}:failure`,revision:null,severity:`critical`,title:`Tuning run failed`,message:e.failure.message??e.failure.type,timestamp:e.finishedAt,evaluationId:null});for(let i of this.evaluationMap.values()){let a=i.evaluation;if(a.state!==`FAILED`&&a.state!==`REJECTED`&&!a.slow)continue;let o=a.state===`FAILED`,r=a.state===`REJECTED`,s=o?`Evaluation failed`:r?`Evaluation rejected`:`Slow evaluation`,l=a.message??a.reasonCode??a.instanceName;n.push({key:`evaluation:${a.id}`,revision:i.revision,severity:o?`critical`:`warning`,title:s,message:a.slow&&r?`${l} \xB7 also slow`:l,timestamp:a.finishedAt,evaluationId:a.id})}n.sort((i,a)=>i.revision===null?-1:a.revision===null?1:a.revision-i.revision),this.alertsSignal.set(n)}resetRun(n){this.currentRunId=n,this.evaluationRevision=0,this.evaluationMap.clear(),this.candidateCache.clear(),this.componentRequest++,this.componentUsageSignal.set(null),this.componentScopeSignal.set(`ALL`),this.componentErrorSignal.set(null),this.componentMarker=null,this.evaluationsSignal.set([]),this.elitesSignal.set(null),this.eliteHistorySignal.set(null),this.searchSpaceSignal.set(null),this.artifactsSignal.set(null),this.alertsSignal.set([]),this.eliteMarker=null,this.searchSpaceLoaded=!1,this.lastArtifactFetch=0,this.lastArtifactPhase=null,this.restoreSeenAlerts(n)}restoreSeenAlerts(n){if(this.seenAlertRevisionSignal.set(0),this.runFailureSeenSignal.set(!1),!!n)try{let e=localStorage.getItem(zb);if(!e)return;let i=JSON.parse(e);i.runId===n&&(this.seenAlertRevisionSignal.set(typeof i.revision==`number`?i.revision:0),this.runFailureSeenSignal.set(i.runFailureSeen===!0))}catch{localStorage.removeItem(zb)}}persistSeenAlerts(){if(!this.currentRunId)return;let n={runId:this.currentRunId,revision:this.seenAlertRevisionSignal(),runFailureSeen:this.runFailureSeenSignal()};localStorage.setItem(zb,JSON.stringify(n))}clearPollTimer(){this.pollTimer&&(clearTimeout(this.pollTimer),this.pollTimer=null)}isNotFound(n){return n instanceof Ei&&n.status===404}errorMessage(n){return n instanceof Ei?`Backend request failed (${n.status||`network error`})`:n instanceof Error?n.message:String(n)}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var PP=(t,n)=>n[0];function FP(t,n){if(t&1&&(c(0,`dt`),u(1),d(),c(2,`dd`),u(3),d()),t&2){let e=n.$implicit;m(),w(e[0]),m(2),w(e[1])}}function LP(t,n){t&1&&(c(0,`dd`),u(1,`No parameters were reported.`),d())}function VP(t,n){if(t&1&&(c(0,`h3`),u(1,`Decoded algorithm`),d(),c(2,`pre`),u(3),j(4,`json`),d()),t&2){let e=v();m(3),w(_e(4,1,e.algorithm))}}function BP(t,n){if(t&1&&(c(0,`p`,1),u(1),d()),t&2){let e=v();m(),w(e.decodeError)}}function jP(t,n){if(t&1&&(c(0,`section`,4)(1,`span`)(2,`strong`),u(3),d(),u(4,` succeeded`),d(),c(5,`span`)(6,`strong`),u(7),d(),u(8,` running`),d(),c(9,`span`)(10,`strong`),u(11),d(),u(12,` rejected`),d(),c(13,`span`)(14,`strong`),u(15),d(),u(16,` failed`),d(),c(17,`span`)(18,`strong`),u(19),d(),u(20,` slow`),d()(),c(21,`h3`),u(22,`Parameters`),d(),c(23,`dl`),ue(24,FP,4,2,null,null,PP,!1,LP,2,0,`dd`),d(),E(27,VP,5,3),E(28,BP,2,1,`p`,1)),t&2){let e=n,i=v();m(3),w(e.evaluations.succeeded),m(4),w(e.evaluations.running),m(4),w(e.evaluations.rejected),m(4),w(e.evaluations.failed),m(4),w(e.evaluations.slow),m(5),me(i.parameterEntries(e)),m(3),D(e.algorithm?27:-1),m(),D(e.decodeError?28:-1)}}function zP(t,n){if(t&1&&(c(0,`p`,1),u(1),d()),t&2){let e=v();m(),w(e.error())}}function HP(t,n){t&1&&(c(0,`p`),u(1,`Loading configuration…`),d())}var hs=class t{store=f(ja);configurationId=f(nc);candidate=k(null);error=k(null);ngOnInit(){this.store.loadCandidate(this.configurationId).then(n=>this.candidate.set(n)).catch(n=>this.error.set(n instanceof Error?n.message:String(n)))}parameterEntries(n){return Object.entries(n.parameters)}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-candidate-dialog`]],decls:9,vars:2,consts:[[`mat-dialog-title`,``],[`role`,`alert`,1,`error`],[`align`,`end`],[`mat-button`,``,`mat-dialog-close`,``,`type`,`button`],[`aria-label`,`Candidate evaluation counts`,1,`counts`]],template:function(e,i){if(e&1&&(c(0,`h2`,0),u(1),d(),c(2,`mat-dialog-content`),E(3,jP,29,8)(4,zP,2,1,`p`,1)(5,HP,2,0,`p`),d(),c(6,`mat-dialog-actions`,2)(7,`button`,3),u(8,`Close`),d()()),e&2){let a;m(),B(`Configuration `,i.configurationId),m(2),D((a=i.candidate())?3:i.error()?4:5,a)}},dependencies:[Ft,tn,Tm,Dm,Im,Mm,km,wo],styles:[`mat-dialog-content[_ngcontent-%COMP%]{min-width:min(42rem,80vw)}.counts[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.5rem 1.25rem;margin-bottom:1.25rem}dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(10rem,1fr) minmax(8rem,1fr);gap:.35rem 1rem}dt[_ngcontent-%COMP%]{overflow-wrap:anywhere;color:var(--%NS%mat-sys-on-surface-variant)}dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}pre[_ngcontent-%COMP%]{max-height:22rem;overflow:auto;padding:1rem;border-radius:.5rem;background:var(--%NS%mat-sys-surface-container);white-space:pre-wrap}.error[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}`]})};var UP=()=>[25,50,100];var $P=(t,n)=>n.id;function GP(t,n){t&1&&(c(0,`span`,17),u(1,`slow`),d())}function WP(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`)(4,`button`,14),C(`click`,function(){let a=M(e).$implicit;return T(v().candidateSelected.emit(a.configurationId))}),u(5),d()(),c(6,`td`,15),u(7),d(),c(8,`td`)(9,`span`,16),u(10),d()(),c(11,`td`),u(12),j(13,`number`),d(),c(14,`td`),u(15),j(16,`number`),E(17,GP,2,0,`span`,17),d(),c(18,`td`),u(19),j(20,`date`),d(),c(21,`td`,18),u(22),d()()}if(t&2){let e=n.$implicit,i=v();G(`issue`,e.state===`FAILED`||e.state===`REJECTED`),m(2),w(e.id),m(3),B(` `,e.configurationId,` `),m(),S(`title`,e.instanceName),m(),w(e.instanceName),m(2),tt(i.stateClass(e.state)),m(),w(e.state),m(2),w(e.cost===null?`—`:ke(13,14,e.cost,`1.0-5`)),m(3),B(` `,e.timeSeconds===null?`—`:ke(16,17,e.timeSeconds,`1.2-2`)+` s`,` `),m(2),D(e.slow?17:-1),m(2),B(` `,e.finishedAt?ke(20,20,e.finishedAt,`HH:mm:ss`):`Running`,` `),m(3),w(e.message??e.reasonCode??`—`)}}function qP(t,n){t&1&&(c(0,`tr`)(1,`td`,19),u(2,`No evaluations match these filters.`),d()())}var Am=class t{evaluations=zt.required();forceIssues=zt(!1);candidateSelected=iu();search=k(``);state=k(`ALL`);slowOnly=k(!1);issueOnly=k(!1);pageIndex=k(0);pageSize=k(25);sortColumn=k(`id`);sortAscending=k(!1);filtered=K(()=>{let n=this.search().trim().toLowerCase(),e=this.state(),i=this.slowOnly(),a=this.issueOnly()||this.forceIssues(),o=[];for(let l of this.evaluations())e!==`ALL`&&l.state!==e||i&&!l.slow||a&&l.state!==`FAILED`&&l.state!==`REJECTED`&&!l.slow||n&&!l.configurationId.toLowerCase().includes(n)&&!l.instanceName.toLowerCase().includes(n)&&!String(l.id).includes(n)||o.push(l);let r=this.sortColumn(),s=this.sortAscending()?1:-1;return o.sort((l,h)=>this.compare(l[r],h[r])*s),o});page=K(()=>{let n=this.pageIndex()*this.pageSize();return this.filtered().slice(n,n+this.pageSize())});updateSearch(n){this.search.set(n),this.pageIndex.set(0)}updateState(n){this.state.set(n),this.pageIndex.set(0)}updateSlowOnly(n){this.slowOnly.set(n),this.pageIndex.set(0)}updateIssueOnly(n){this.issueOnly.set(n),this.pageIndex.set(0)}updatePage(n){this.pageIndex.set(n.pageIndex),this.pageSize.set(n.pageSize)}sortBy(n){this.sortColumn()===n?this.sortAscending.update(e=>!e):(this.sortColumn.set(n),this.sortAscending.set(!0)),this.pageIndex.set(0)}sortIndicator(n){return this.sortColumn()!==n?``:this.sortAscending()?` ↑`:` ↓`}stateClass(n){return`state-${n.toLowerCase()}`}compare(n,e){return n===e?0:n===null?1:e===null?-1:typeof n==`number`&&typeof e==`number`?n-e:String(n).localeCompare(String(e),void 0,{numeric:!0})}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-evaluation-table`]],inputs:{evaluations:[1,`evaluations`],forceIssues:[1,`forceIssues`]},outputs:{candidateSelected:`candidateSelected`},decls:54,vars:17,consts:[[`aria-label`,`Evaluation filters`,1,`filters`],[`appearance`,`outline`,`subscriptSizing`,`dynamic`],[`matInput`,``,`type`,`search`,`placeholder`,`ID, configuration, or instance`,3,`ngModelChange`,`ngModel`],[3,`ngModelChange`,`ngModel`],[`value`,`ALL`],[`value`,`RUNNING`],[`value`,`SUCCEEDED`],[`value`,`REJECTED`],[`value`,`FAILED`],[3,`ngModelChange`,`ngModel`,`disabled`],[1,`table-scroll`],[`type`,`button`,3,`click`],[3,`issue`],[`aria-label`,`Evaluation pages`,3,`page`,`length`,`pageIndex`,`pageSize`,`pageSizeOptions`],[`type`,`button`,1,`candidate-link`,3,`click`],[1,`instance`,3,`title`],[1,`state`],[1,`slow`],[1,`details`],[`colspan`,`8`,1,`empty`]],template:function(e,i){e&1&&(c(0,`section`,0)(1,`mat-form-field`,1)(2,`mat-label`),u(3,`Search`),d(),c(4,`input`,2),Er(),C(`ngModelChange`,function(o){return i.updateSearch(o)}),d()(),c(5,`mat-form-field`,1)(6,`mat-label`),u(7,`State`),d(),c(8,`mat-select`,3),Er(),C(`ngModelChange`,function(o){return i.updateState(o)}),c(9,`mat-option`,4),u(10,`All states`),d(),c(11,`mat-option`,5),u(12,`Running`),d(),c(13,`mat-option`,6),u(14,`Succeeded`),d(),c(15,`mat-option`,7),u(16,`Rejected`),d(),c(17,`mat-option`,8),u(18,`Failed`),d()()(),c(19,`mat-checkbox`,3),Er(),C(`ngModelChange`,function(o){return i.updateSlowOnly(o)}),u(20,` Slow only `),d(),c(21,`mat-checkbox`,9),Er(),C(`ngModelChange`,function(o){return i.updateIssueOnly(o)}),u(22,` Issues only `),d()(),c(23,`div`,10)(24,`table`)(25,`thead`)(26,`tr`)(27,`th`)(28,`button`,11),C(`click`,function(){return i.sortBy(`id`)}),u(29),d()(),c(30,`th`)(31,`button`,11),C(`click`,function(){return i.sortBy(`configurationId`)}),u(32),d()(),c(33,`th`)(34,`button`,11),C(`click`,function(){return i.sortBy(`instanceName`)}),u(35),d()(),c(36,`th`)(37,`button`,11),C(`click`,function(){return i.sortBy(`state`)}),u(38),d()(),c(39,`th`)(40,`button`,11),C(`click`,function(){return i.sortBy(`cost`)}),u(41),d()(),c(42,`th`)(43,`button`,11),C(`click`,function(){return i.sortBy(`timeSeconds`)}),u(44),d()(),c(45,`th`),u(46,`Finished`),d(),c(47,`th`),u(48,`Details`),d()()(),c(49,`tbody`),ue(50,WP,23,23,`tr`,12,$P,!1,qP,3,0,`tr`),d()()(),c(53,`mat-paginator`,13),C(`page`,function(o){return i.updatePage(o)}),d()),e&2&&(m(4),S(`ngModel`,i.search()),Dr(),m(4),S(`ngModel`,i.state()),Dr(),m(11),S(`ngModel`,i.slowOnly()),Dr(),m(2),S(`ngModel`,i.issueOnly()||i.forceIssues())(`disabled`,i.forceIssues()),Dr(),m(8),B(`ID`,i.sortIndicator(`id`)),m(3),B(` Configuration`,i.sortIndicator(`configurationId`),` `),m(3),B(` Instance`,i.sortIndicator(`instanceName`),` `),m(3),B(`State`,i.sortIndicator(`state`)),m(3),B(`Cost`,i.sortIndicator(`cost`)),m(3),B(` Time`,i.sortIndicator(`timeSeconds`),` `),m(6),me(i.page()),m(3),S(`length`,i.filtered().length)(`pageIndex`,i.pageIndex())(`pageSize`,i.pageSize())(`pageSizeOptions`,nt(16,UP)))},dependencies:[VS,Qu,FS,rb,Ft,am,$l,di,Ti,na,Jr,Zr,fm,Ql,Pa,Oa,Un,Xi,En],styles:[`[_nghost-%COMP%]{display:block;min-width:0}.filters[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem 1rem;align-items:center;margin-bottom:1rem}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]:first-child{flex:1 1 18rem}.table-scroll[_ngcontent-%COMP%]{overflow:auto}table[_ngcontent-%COMP%]{width:100%;min-width:70rem;border-collapse:collapse;font:var(--%NS%mat-sys-body-small)}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.65rem .55rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left;vertical-align:middle}th[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-weight:650}th[_ngcontent-%COMP%]   button[_ngcontent-%COMP%], .candidate-link[_ngcontent-%COMP%]{padding:0;border:0;color:inherit;background:transparent;cursor:pointer;font-weight:inherit}.candidate-link[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-primary);text-decoration:underline;text-underline-offset:.15rem}.instance[_ngcontent-%COMP%], .details[_ngcontent-%COMP%]{max-width:18rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.state[_ngcontent-%COMP%]{display:inline-block;padding:.15rem .45rem;border-radius:999px;background:var(--%NS%mat-sys-surface-container-high);font:var(--%NS%mat-sys-label-small)}.state-succeeded[_ngcontent-%COMP%]{color:#08743a;background:#d9f5e4}.state-rejected[_ngcontent-%COMP%], .slow[_ngcontent-%COMP%]{color:#835100;background:#fff0c6}.state-failed[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-error-container);background:var(--%NS%mat-sys-error-container)}.slow[_ngcontent-%COMP%]{margin-left:.25rem;padding:.1rem .3rem;border-radius:.25rem;font:var(--%NS%mat-sys-label-small)}tr.issue[_ngcontent-%COMP%]{background:color-mix(in srgb,var(--%NS%mat-sys-error-container) 22%,transparent)}.empty[_ngcontent-%COMP%]{padding:2.5rem;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}mat-paginator[_ngcontent-%COMP%]{background:transparent}`]})};var tE=(t,n)=>n.name;function YP(t,n){if(t&1&&(c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),d()()),t&2){let e=n.$implicit,i=v(2);m(2),w(e.name),m(2),w(e.kind),m(2),w(i.domain(e))}}function KP(t,n){if(t&1&&(c(0,`details`)(1,`summary`),u(2),d(),c(3,`table`)(4,`thead`)(5,`tr`)(6,`th`),u(7,`Name`),d(),c(8,`th`),u(9,`Kind`),d(),c(10,`th`),u(11,`Domain`),d()()(),c(12,`tbody`),ue(13,YP,7,3,`tr`,null,tE),d()()()),t&2){let e=n.$implicit;m(2),Me(``,e.name,` · `,e.parameters.length,` parameters`),m(11),me(e.parameters)}}var Rm=class t{data=f(nc);domain(n){return n.choices.length>0?n.choices.join(`, `):n.values.length>0?n.values.join(`, `):n.minimum!==null||n.maximum!==null?`${String(n.minimum)} \u2013 ${String(n.maximum)}`:`Provided by Mork`}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-search-space-dialog`]],decls:30,vars:5,consts:[[`mat-dialog-title`,``],[1,`summary`],[1,`components`],[`align`,`end`],[`mat-button`,``,`mat-dialog-close`,``,`type`,`button`]],template:function(e,i){e&1&&(c(0,`h2`,0),u(1,`Generated search space`),d(),c(2,`mat-dialog-content`)(3,`section`,1)(4,`span`)(5,`strong`),u(6),d(),u(7,` roots`),d(),c(8,`span`)(9,`strong`),u(10),d(),u(11,` components`),d(),c(12,`span`)(13,`strong`),u(14),d(),u(15,` IRACE parameters`),d(),c(16,`span`)(17,`strong`),u(18),d(),u(19,` constraints`),d()(),c(20,`p`)(21,`strong`),u(22,`Root choices:`),d(),u(23),d(),c(24,`section`,2),ue(25,KP,15,2,`details`,null,tE),d()(),c(27,`mat-dialog-actions`,3)(28,`button`,4),u(29,`Close`),d()()),e&2&&(m(6),w(i.data.summary.rootCount),m(4),w(i.data.summary.componentCount),m(4),w(i.data.summary.generatedIraceParameterCount),m(4),w(i.data.summary.generatedForbiddenConstraintCount),m(5),B(` `,i.data.roots.join(`, `)),m(2),me(i.data.components))},dependencies:[Ft,tn,Tm,Dm,Im,Mm,km],styles:[`mat-dialog-content[_ngcontent-%COMP%]{width:min(60rem,84vw)}.summary[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem 2rem;margin-bottom:1rem}.components[_ngcontent-%COMP%]{display:grid;gap:.5rem}details[_ngcontent-%COMP%]{padding:.75rem;border:1px solid var(--%NS%mat-sys-outline-variant);border-radius:.5rem}summary[_ngcontent-%COMP%]{cursor:pointer;font-weight:650}table[_ngcontent-%COMP%]{width:100%;margin-top:.75rem;border-collapse:collapse}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.45rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left;overflow-wrap:anywhere}`]})};var nE=new N(`MAT_BUTTON_TOGGLE_DEFAULT_OPTIONS`,{providedIn:`root`,factory:()=>({hideSingleSelectionIndicator:!1,hideMultipleSelectionIndicator:!1,disabledInteractive:!1})});var iE=new N(`MatButtonToggleGroup`);var QP={provide:Yr,useExisting:Ht(()=>Hb),multi:!0};var Om=class{source;value;constructor(n,e){this.source=n,this.value=e}};var Hb=(()=>{class t{_changeDetector=f(Fe);_dir=f(gt,{optional:!0});_multiple=!1;_disabled=!1;_disabledInteractive=!1;_selectionModel;_rawValue;_controlValueAccessorChangeFn=()=>{};_onTouched=()=>{};_buttonToggles;appearance;get name(){return this._name}set name(e){this._name=e,this._markButtonsForCheck()}_name=f(Be).getId(`mat-button-toggle-group-`);vertical=!1;get value(){let e=this._selectionModel?this._selectionModel.selected:[];return this.multiple?e.map(i=>i.value):e[0]?e[0].value:void 0}set value(e){this._setSelectionByValue(e),this.valueChange.emit(this.value)}valueChange=new te;get selected(){let e=this._selectionModel?this._selectionModel.selected:[];return this.multiple?e:e[0]||null}get multiple(){return this._multiple}set multiple(e){this._multiple=e,this._markButtonsForCheck()}get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._markButtonsForCheck()}get disabledInteractive(){return this._disabledInteractive}set disabledInteractive(e){this._disabledInteractive=e,this._markButtonsForCheck()}get dir(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}change=new te;get hideSingleSelectionIndicator(){return this._hideSingleSelectionIndicator}set hideSingleSelectionIndicator(e){this._hideSingleSelectionIndicator=e,this._markButtonsForCheck()}_hideSingleSelectionIndicator;get hideMultipleSelectionIndicator(){return this._hideMultipleSelectionIndicator}set hideMultipleSelectionIndicator(e){this._hideMultipleSelectionIndicator=e,this._markButtonsForCheck()}_hideMultipleSelectionIndicator;constructor(){let e=f(nE,{optional:!0});this.appearance=e&&e.appearance?e.appearance:`standard`,this._hideSingleSelectionIndicator=e?.hideSingleSelectionIndicator??!1,this._hideMultipleSelectionIndicator=e?.hideMultipleSelectionIndicator??!1}ngOnInit(){this._selectionModel=new jo(this.multiple,void 0,!1)}ngAfterContentInit(){this._selectionModel.select(...this._buttonToggles.filter(e=>e.checked)),this.multiple||this._initializeTabIndex()}writeValue(e){this.value=e,this._changeDetector.markForCheck()}registerOnChange(e){this._controlValueAccessorChangeFn=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e}_keydown(e){if(this.multiple||this.disabled||Ct(e))return;let a=e.target.id,o=this._buttonToggles.toArray().findIndex(s=>s.buttonId===a),r=null;switch(e.keyCode){case 32:case 13:r=this._buttonToggles.get(o)||null;break;case 38:r=this._getNextButton(o,-1);break;case 37:r=this._getNextButton(o,this.dir===`ltr`?-1:1);break;case 40:r=this._getNextButton(o,1);break;case 39:r=this._getNextButton(o,this.dir===`ltr`?1:-1);break;default:return}r&&(e.preventDefault(),r._onButtonClick(),r.focus())}_emitChangeEvent(e){let i=new Om(e,this.value);this._rawValue=i.value,this._controlValueAccessorChangeFn(i.value),this.change.emit(i)}_syncButtonToggle(e,i,a=!1,o=!1){!this.multiple&&this.selected&&!e.checked&&(this.selected.checked=!1),this._selectionModel?i?this._selectionModel.select(e):this._selectionModel.deselect(e):o=!0,o?Promise.resolve().then(()=>this._updateModelValue(e,a)):this._updateModelValue(e,a)}_isSelected(e){return this._selectionModel&&this._selectionModel.isSelected(e)}_isPrechecked(e){return typeof this._rawValue>`u`?!1:this.multiple&&Array.isArray(this._rawValue)?this._rawValue.some(i=>e.value!=null&&i===e.value):e.value===this._rawValue}_initializeTabIndex(){if(this._buttonToggles.forEach(e=>{e.tabIndex=-1}),this.selected)this.selected.tabIndex=0;else for(let e=0;e<this._buttonToggles.length;e++){let i=this._buttonToggles.get(e);if(!i.disabled){i.tabIndex=0;break}}}_getNextButton(e,i){let a=this._buttonToggles;for(let o=1;o<=a.length;o++){let r=(e+i*o+a.length)%a.length,s=a.get(r);if(s&&!s.disabled)return s}return null}_setSelectionByValue(e){if(this._rawValue=e,!this._buttonToggles)return;let i=this._buttonToggles.toArray();if(this.multiple&&e?(this._clearSelection(),e.forEach(a=>this._selectValue(a,i))):(this._clearSelection(),this._selectValue(e,i)),!this.multiple&&i.every(a=>a.tabIndex===-1)){for(let a of i)if(!a.disabled){a.tabIndex=0;break}}}_clearSelection(){this._selectionModel.clear(),this._buttonToggles.forEach(e=>{e.checked=!1,this.multiple||(e.tabIndex=-1)})}_selectValue(e,i){for(let a of i)if(a.value===e){a.checked=!0,this._selectionModel.select(a),this.multiple||(a.tabIndex=0);break}}_updateModelValue(e,i){i&&this._emitChangeEvent(e),this.valueChange.emit(this.value)}_markButtonsForCheck(){this._buttonToggles?.forEach(e=>e._markForCheck())}static ɵfac=function(i){return new(i||t)};static ɵdir=Q({type:t,selectors:[[`mat-button-toggle-group`]],contentQueries:function(i,a,o){if(i&1&&jt(o,Pm,5),i&2){let r;J(r=ee())&&(a._buttonToggles=r)}},hostAttrs:[1,`mat-button-toggle-group`],hostVars:6,hostBindings:function(i,a){i&1&&C(`keydown`,function(r){return a._keydown(r)}),i&2&&(ne(`role`,a.multiple?`group`:`radiogroup`)(`aria-disabled`,a.disabled),G(`mat-button-toggle-vertical`,a.vertical)(`mat-button-toggle-group-appearance-standard`,a.appearance===`standard`))},inputs:{appearance:`appearance`,name:`name`,vertical:[2,`vertical`,`vertical`,z],value:`value`,multiple:[2,`multiple`,`multiple`,z],disabled:[2,`disabled`,`disabled`,z],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z],hideSingleSelectionIndicator:[2,`hideSingleSelectionIndicator`,`hideSingleSelectionIndicator`,z],hideMultipleSelectionIndicator:[2,`hideMultipleSelectionIndicator`,`hideMultipleSelectionIndicator`,z]},outputs:{valueChange:`valueChange`,change:`change`},exportAs:[`matButtonToggleGroup`],features:[Ge([QP,{provide:iE,useExisting:t}])]})}return t})();var Pm=(()=>{class t{_changeDetectorRef=f(Fe);_elementRef=f(Y);_focusMonitor=f(en);_idGenerator=f(Be);_animationDisabled=Ue();_checked=!1;ariaLabel;ariaLabelledby=null;_buttonElement;buttonToggleGroup;get buttonId(){return`${this.id}-button`}id;name;value;get tabIndex(){return this._tabIndex()}set tabIndex(e){this._tabIndex.set(e)}_tabIndex;disableRipple=!1;get appearance(){return this.buttonToggleGroup?this.buttonToggleGroup.appearance:this._appearance}set appearance(e){this._appearance=e}_appearance;get checked(){return this.buttonToggleGroup?this.buttonToggleGroup._isSelected(this):this._checked}set checked(e){e!==this._checked&&(this._checked=e,this.buttonToggleGroup&&this.buttonToggleGroup._syncButtonToggle(this,this._checked),this._changeDetectorRef.markForCheck())}get disabled(){return this._disabled||this.buttonToggleGroup&&this.buttonToggleGroup.disabled}set disabled(e){this._disabled=e}_disabled=!1;get disabledInteractive(){return this._disabledInteractive||this.buttonToggleGroup!==null&&this.buttonToggleGroup.disabledInteractive}set disabledInteractive(e){this._disabledInteractive=e}_disabledInteractive;change=new te;constructor(){f(Je).load(mn);let e=f(iE,{optional:!0}),i=f(new Ki(`tabindex`),{optional:!0})||``,a=f(nE,{optional:!0});this._tabIndex=k(parseInt(i)||0),this.buttonToggleGroup=e,this._appearance=a&&a.appearance?a.appearance:`standard`,this._disabledInteractive=a?.disabledInteractive??!1}ngOnInit(){let e=this.buttonToggleGroup;this.id=this.id||this._idGenerator.getId(`mat-button-toggle-`),e&&(e._isPrechecked(this)?this.checked=!0:e._isSelected(this)!==this._checked&&e._syncButtonToggle(this,this._checked))}ngAfterViewInit(){this._animationDisabled||this._elementRef.nativeElement.classList.add(`mat-button-toggle-animations-enabled`),this._focusMonitor.monitor(this._elementRef,!0)}ngOnDestroy(){let e=this.buttonToggleGroup;this._focusMonitor.stopMonitoring(this._elementRef),e&&e._isSelected(this)&&e._syncButtonToggle(this,!1,!1,!0)}focus(e){this._buttonElement.nativeElement.focus(e)}_onButtonClick(){if(this.disabled)return;let e=this.isSingleSelector()?!0:!this._checked;if(e!==this._checked&&(this._checked=e,this.buttonToggleGroup&&(this.buttonToggleGroup._syncButtonToggle(this,this._checked,!0),this.buttonToggleGroup._onTouched())),this.isSingleSelector()){let i=this.buttonToggleGroup._buttonToggles.find(a=>a.tabIndex===0);i&&(i.tabIndex=-1),this.tabIndex=0}this.change.emit(new Om(this,this.value))}_markForCheck(){this._changeDetectorRef.markForCheck()}_getButtonName(){return this.isSingleSelector()?this.buttonToggleGroup.name:this.name||null}isSingleSelector(){return this.buttonToggleGroup&&!this.buttonToggleGroup.multiple}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`button`],i=[`*`];function a(o,r){if(o&1&&(c(0,`div`,2),U(1,`mat-pseudo-checkbox`,6),d()),o&2){let s=v();m(),S(`disabled`,s.disabled)}}return H({type:t,selectors:[[`mat-button-toggle`]],viewQuery:function(r,s){if(r&1&&Ze(e,5),r&2){let l;J(l=ee())&&(s._buttonElement=l.first)}},hostAttrs:[`role`,`presentation`,1,`mat-button-toggle`],hostVars:14,hostBindings:function(r,s){r&1&&C(`focus`,function(){return s.focus()}),r&2&&(ne(`aria-label`,null)(`aria-labelledby`,null)(`id`,s.id)(`name`,null),G(`mat-button-toggle-standalone`,!s.buttonToggleGroup)(`mat-button-toggle-checked`,s.checked)(`mat-button-toggle-disabled`,s.disabled)(`mat-button-toggle-disabled-interactive`,s.disabledInteractive)(`mat-button-toggle-appearance-standard`,s.appearance===`standard`))},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],id:`id`,name:`name`,value:`value`,tabIndex:`tabIndex`,disableRipple:[2,`disableRipple`,`disableRipple`,z],appearance:`appearance`,checked:[2,`checked`,`checked`,z],disabled:[2,`disabled`,`disabled`,z],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,z]},outputs:{change:`change`},exportAs:[`matButtonToggle`],ngContentSelectors:i,decls:7,vars:13,consts:[[`button`,``],[`type`,`button`,1,`mat-button-toggle-button`,`mat-focus-indicator`,3,`click`,`id`,`disabled`],[1,`mat-button-toggle-checkbox-wrapper`],[1,`mat-button-toggle-label-content`],[1,`mat-button-toggle-focus-overlay`],[`matRipple`,``,1,`mat-button-toggle-ripple`,3,`matRippleTrigger`,`matRippleDisabled`],[`state`,`checked`,`aria-hidden`,`true`,`appearance`,`minimal`,3,`disabled`]],template:function(r,s){if(r&1&&(Pe(),c(0,`button`,1,0),C(`click`,function(){return s._onButtonClick()}),E(2,a,2,1,`div`,2),c(3,`span`,3),he(4),d()(),U(5,`span`,4)(6,`span`,5)),r&2){let l=ft(1);S(`id`,s.buttonId)(`disabled`,s.disabled&&!s.disabledInteractive||null),ne(`role`,s.isSingleSelector()?`radio`:`button`)(`tabindex`,s.disabled&&!s.disabledInteractive?-1:s.tabIndex)(`aria-pressed`,s.isSingleSelector()?null:s.checked)(`aria-checked`,s.isSingleSelector()?s.checked:null)(`name`,s._getButtonName())(`aria-label`,s.ariaLabel)(`aria-labelledby`,s.ariaLabelledby)(`aria-disabled`,s.disabled&&s.disabledInteractive?`true`:null),m(2),D(s.buttonToggleGroup&&(!s.buttonToggleGroup.multiple&&!s.buttonToggleGroup.hideSingleSelectionIndicator||s.buttonToggleGroup.multiple&&!s.buttonToggleGroup.hideMultipleSelectionIndicator)?2:-1),m(4),S(`matRippleTrigger`,l)(`matRippleDisabled`,s.disableRipple||s.disabled)}},dependencies:[li,hm],styles:[`.mat-button-toggle-standalone,
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
`],encapsulation:2})})()}return t})();var aE=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=re({type:t});static ɵinj=ae({imports:[ki,Pm,we]})}return t})();var $n=class{static count(n,e){return e===`CANDIDATE`?n.candidatePlacements:n.evaluationPlacements}static components(n,e,i){let a=i.trim().toLocaleLowerCase();return(n?.components??[]).filter(o=>o.name.toLocaleLowerCase().includes(a)).sort((o,r)=>this.count(r,e)-this.count(o,e)||o.name.localeCompare(r.name))}static relationships(n,e,i,a){return(n?.relationships??[]).filter(o=>i===`incoming`?o.child===e:o.parent===e).sort((o,r)=>this.count(r,a)-this.count(o,a)||o.parent.localeCompare(r.parent)||o.role.localeCompare(r.role)||o.child.localeCompare(r.child))}static treemap(n,e,i){return{options:{chart:{type:`treemap`,height:390,animation:!1,backgroundColor:`transparent`},title:{text:void 0},credits:{enabled:!1},accessibility:{description:`Component placements. Select a tile or a component in the list to inspect its parents, children and configurations.`},tooltip:{pointFormat:`<b>{point.name}</b><br/>Placements: {point.value:,.0f}`},plotOptions:{series:{animation:!1}},series:[{type:`treemap`,name:`Component placements`,layoutAlgorithm:`squarified`,colorByPoint:!0,borderWidth:3,borderColor:`#fff`,colors:[`#2265a7`,`#00897b`,`#407fab`,`#49a69e`,`#697cad`,`#78b8ad`],dataLabels:{enabled:!0,format:`{point.name}<br/>{point.value:,.0f}`,style:{textOutline:`none`,fontWeight:`600`}},point:{events:{click:function(){this.name&&i(this.name)}}},data:n.filter(a=>this.count(a,e)>0).map(a=>({name:a.name,value:this.count(a,e)}))}]}}}static sankey(n,e,i,a,o){let r=[{id:`focus`,name:n??`Component`,color:`#2265a7`}],s=[],l=(p,g)=>{let _=p.filter(y=>this.count(y,a)>0);for(let[y,I]of _.slice(0,10).entries()){let P=`${g}:${y}`;r.push({id:P,name:g===`in`?`${I.parent} \xB7 ${I.role}`:`${I.role} \u2192 ${I.child}`,color:`#00897b`}),s.push({from:g===`in`?P:`focus`,to:g===`in`?`focus`:P,weight:this.count(I,a),custom:{edge:I}})}let b=_.slice(10).reduce((y,I)=>y+this.count(I,a),0);if(b>0){let y=`${g}:other`;r.push({id:y,name:`Other ${g===`in`?`parents`:`children`}`,color:`#aab7c5`}),s.push({from:g===`in`?y:`focus`,to:g===`in`?`focus`:y,weight:b})}};l(e,`in`),l(i,`out`);let h={type:`sankey`,animation:!1,name:`Direct relationships`,nodes:r,data:s,point:{events:{click:function(){let p=this.options.custom?.edge;p&&o(p)}}}};return{options:{chart:{height:Math.max(300,Math.min(560,Math.max(e.length,i.length)*32)),animation:!1,backgroundColor:`transparent`},title:{text:void 0},credits:{enabled:!1},accessibility:{description:`Direct parents and children of ${n??`the selected component`}. The complete role relationships are also listed in the inspector.`},series:[h]}}}static matrix(n,e,i,a){let o=new Set(this.components(n,e,i).map(y=>y.name)),r=i.trim().length>0,s=(n?.relationships??[]).filter(y=>!r||o.has(y.parent)||o.has(y.child)),l=new Set(o);for(let y of s)l.add(y.parent),l.add(y.child);let h=this.components(n,e,``).filter(y=>l.has(y.name)).sort((y,I)=>(r?Number(o.has(I.name))-Number(o.has(y.name)):0)||this.count(I,e)-this.count(y,e)||y.name.localeCompare(I.name)).slice(0,a),p=new Set(h.map(y=>y.name)),g=new Map,_=0;for(let y of s){if(!p.has(y.parent)||!p.has(y.child))continue;let I=JSON.stringify([y.parent,y.role]),P=g.get(I);P||(P={parent:y.parent,role:y.role,count:0,edges:new Map},g.set(I,P)),P.edges.set(y.child,y),P.count+=this.count(y,e),_=Math.max(_,this.count(y,e))}return{columns:h,rows:[...g.values()].sort((y,I)=>I.count-y.count||y.parent.localeCompare(I.parent)||y.role.localeCompare(I.role)).slice(0,a).map(y=>({parent:y.parent,role:y.role,cells:h.map(I=>y.edges.get(I.name)??null)})),maximum:_}}static parentPercent(n,e){let i=e?.components.find(a=>a.name===n.parent);return i?.configurationCount?100*n.configurationCount/i.configurationCount:0}};var ZP=()=>[];var JP=(t,n)=>n.configurationId;var oE=(t,n)=>n.name;var rE=(t,n)=>n.parent+`:`+n.role;var eF=(t,n)=>n.role+`:`+n.child;function tF(t,n){t&1&&u(0,` Count each unique configuration tree once. Repeated components within a tree count as separate placements. `)}function nF(t,n){t&1&&u(0,` Weight each placement by accepted evaluations, including evaluations that later fail or are rejected. `)}function iF(t,n){if(t&1){let e=$();c(0,`p`,13),u(1),c(2,`button`,15),C(`click`,function(){M(e);return T(v().store.refreshComponentUsage())}),u(3,`Retry`),d()()}t&2&&(m(),B(` `,n,` `))}function aF(t,n){if(t&1&&(c(0,`p`,17),u(1),j(2,`number`),d()),t&2){let e=v();m(),B(` `,_e(2,1,e.unavailableConfigurationCount),` configurations have unavailable component trees and are excluded from placement counts. `)}}function oF(t,n){if(t&1&&(c(0,`p`,14),u(1),d()),t&2){let e=v(2);m(),B(` `,e.store.componentScope()===`CURRENT_ELITES`?`Elites appear after the first race checkpoint.`:`Component usage appears when the first decoded candidate is evaluated.`,` `)}}function rF(t,n){t&1&&(c(0,`p`,14),u(1,`No components match this search.`),d())}function sF(t,n){t&1&&(c(0,`p`,14),u(1,` These configurations have no accepted evaluations yet. Select candidate structures to inspect their composition. `),d())}function lF(t,n){if(t&1){let e=$();c(0,`button`,31),C(`click`,function(){let a=M(e).$implicit;return T(v(4).selectComponent(a.name))}),c(1,`span`),u(2),d(),c(3,`strong`),u(4),j(5,`number`),d()()}if(t&2){let e=n.$implicit,i=v(4);G(`selected`,i.selected()?.name===e.name),ne(`aria-pressed`,i.selected()?.name===e.name),m(2),w(e.name),m(2),w(_e(5,5,i.count(e)))}}function cF(t,n){if(t&1&&(c(0,`mat-card-header`)(1,`mat-card-title`),u(2,`Component usage`),d()(),c(3,`mat-card-content`),U(4,`app-dashboard-chart`,28),E(5,sF,2,0,`p`,14),c(6,`div`,29),ue(7,lF,6,7,`button`,30,oE),d()()),t&2){let e=v(2),i=v();m(4),S(`model`,i.treemap()),m(),D(i.basis()===`EVALUATION`&&e.evaluationPlacementCount===0?5:-1),m(2),me(i.components())}}function dF(t,n){if(t&1&&(c(0,`th`,25),u(1),d()),t&2){let e=n.$implicit;m(),w(e.name)}}function uF(t,n){if(t&1){let e=$();c(0,`button`,31),C(`click`,function(){M(e);let a=v().$implicit;return T(v(5).selectRelationship(a))}),u(1),d()}if(t&2){let e=v().$implicit,i=v(5);Zt(`background-color`,i.cellColor(e)),ne(`aria-label`,i.cellDescription(e)),m(),B(` `,i.cellLabel(e),` `)}}function mF(t,n){t&1&&(c(0,`span`),u(1,`—`),d())}function pF(t,n){if(t&1&&(c(0,`td`),E(1,uF,2,4,`button`,38)(2,mF,2,0,`span`),d()),t&2){let e=n.$implicit;m(),D(e?1:2)}}function hF(t,n){if(t&1&&(c(0,`tr`)(1,`th`,37),u(2),c(3,`small`),u(4),d()(),ue(5,pF,3,1,`td`,null,Co),d()),t&2){let e=n.$implicit;m(2),B(` `,e.parent),m(2),w(e.role),m(),me(e.cells)}}function fF(t,n){if(t&1&&(c(0,`tr`)(1,`td`),u(2,` No direct relationships in this selection. `),d()()),t&2){let e=v(4);m(),ne(`colspan`,e.matrix().columns.length+1)}}function gF(t,n){if(t&1){let e=$();c(0,`mat-card-header`)(1,`mat-card-title`),u(2,`Direct component relationships`),d()(),c(3,`mat-card-content`)(4,`div`,32)(5,`mat-form-field`,2)(6,`mat-label`),u(7,`Maximum rows / columns`),d(),c(8,`mat-select`,3),C(`selectionChange`,function(a){M(e);return T(v(3).matrixLimit.set(a.value))}),c(9,`mat-option`,33),u(10,`10`),d(),c(11,`mat-option`,33),u(12,`20`),d(),c(13,`mat-option`,33),u(14,`50`),d()()(),c(15,`mat-button-toggle-group`,34),C(`change`,function(a){M(e);return T(v(3).matrixPercent.set(a.value))}),c(16,`mat-button-toggle`,33),u(17,`Count`),d(),c(18,`mat-button-toggle`,33),u(19,`% of parent configurations`),d()()(),c(20,`p`,12),u(21,` Rows identify the parent and parameter role; columns identify the direct child. The most frequent matching components are shown. `),d(),c(22,`div`,35)(23,`table`,36)(24,`caption`),u(25,` Direct parent-role to child usage `),d(),c(26,`thead`)(27,`tr`)(28,`th`,25),u(29,`Parent · role`),d(),ue(30,dF,2,1,`th`,25,oE),d()(),c(32,`tbody`),ue(33,hF,7,2,`tr`,null,rE,!1,fF,3,1,`tr`),d()()()()}if(t&2){let e=v(3);m(8),S(`value`,e.matrixLimit()),m(),S(`value`,10),m(2),S(`value`,20),m(2),S(`value`,50),m(2),S(`value`,e.matrixPercent()),m(),S(`value`,!1),m(2),S(`value`,!0),m(12),me(e.matrix().columns),m(3),me(e.matrix().rows)}}function bF(t,n){if(t&1){let e=$();c(0,`button`,31),C(`click`,function(){let a=M(e).$implicit;return T(v(4).selectRelationship(a))}),c(1,`span`)(2,`strong`),u(3),d(),c(4,`small`),u(5),d()(),c(6,`span`),u(7),j(8,`number`),c(9,`small`),u(10),j(11,`number`),d()()()}if(t&2){let e=n.$implicit,i=v(4);m(3),w(e.parent),m(2),w(e.role),m(2),w(_e(8,4,i.count(e))),m(3),B(``,_e(11,6,e.configurationCount),` configurations`)}}function vF(t,n){t&1&&(c(0,`p`),u(1,`No direct parents; this component is used only at the root.`),d())}function _F(t,n){if(t&1){let e=$();c(0,`button`,31),C(`click`,function(){let a=M(e).$implicit;return T(v(4).selectRelationship(a))}),c(1,`span`)(2,`strong`),u(3),d(),c(4,`small`),u(5),d()(),c(6,`span`),u(7),j(8,`number`),c(9,`small`),u(10),j(11,`number`),d()()()}if(t&2){let e=n.$implicit,i=v(4);m(3),w(e.child),m(2),w(e.role),m(2),w(_e(8,4,i.count(e))),m(3),B(``,_e(11,6,e.configurationCount),` configurations`)}}function yF(t,n){t&1&&(c(0,`p`),u(1,`No direct child components.`),d())}function xF(t,n){if(t&1&&(c(0,`mat-card`,20)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d(),c(4,`mat-card-subtitle`),u(5,`Component inspector`),d()(),c(6,`mat-card-content`)(7,`dl`)(8,`dt`),u(9,`Candidate placements`),d(),c(10,`dd`),u(11),j(12,`number`),d(),c(13,`dt`),u(14,`Evaluation exposure`),d(),c(15,`dd`),u(16),j(17,`number`),d(),c(18,`dt`),u(19,`Configuration coverage`),d(),c(20,`dd`),u(21),j(22,`number`),j(23,`number`),d(),c(24,`dt`),u(25,`Root placements / exposure`),d(),c(26,`dd`),u(27),j(28,`number`),j(29,`number`),d()(),c(30,`h3`),u(31,`Used by`),d(),c(32,`div`,39),ue(33,bF,12,8,`button`,40,rE,!1,vF,2,0,`p`),d(),c(36,`h3`),u(37,`Contains`),d(),c(38,`div`,39),ue(39,_F,12,8,`button`,40,eF,!1,yF,2,0,`p`),d()()()),t&2){let e=n,i=v(3);m(3),w(e.name),m(8),w(_e(12,9,e.candidatePlacements)),m(5),w(_e(17,11,e.evaluationPlacements)),m(5),Me(` `,_e(22,13,e.configurationCount),` (`,ke(23,15,i.selectedCoverage(),`1.0-1`),`%) `),m(6),Me(` `,_e(28,18,e.rootCandidatePlacements),` / `,_e(29,20,e.rootEvaluationPlacements),` `),m(6),me(i.incoming()),m(6),me(i.outgoing())}}function CF(t,n){if(t&1&&(c(0,`mat-card`,21)(1,`mat-card-header`)(2,`mat-card-title`),u(3),d()(),c(4,`mat-card-content`),U(5,`app-dashboard-chart`,28),c(6,`p`,12),u(7,` Direct parents → selected component → direct children. Up to ten relationships are shown on each side; the inspector lists all relationships. `),d()()()),t&2){let e=v(3);m(3),B(`Neighborhood of `,e.selected()?.name),m(2),S(`model`,e.sankey())}}function SF(t,n){if(t&1){let e=$();c(0,`p`),u(1),c(2,`button`,15),C(`click`,function(){M(e);return T(v(3).selectedEdge.set(null))}),u(3),d()()}if(t&2){let e=n,i=v(3);m(),Ot(` `,e.parent,` → `,e.role,` → `,e.child,` `),m(2),B(` Show all using `,i.selected()?.name,` `)}}function wF(t,n){if(t&1&&(c(0,`p`),u(1),d()),t&2){let e=v(3);m(),B(`Configurations containing `,e.selected()?.name)}}function EF(t,n){if(t&1){let e=$();c(0,`p`,22),u(1),c(2,`button`,15),C(`click`,function(){M(e);return T(v(3).retryPage())}),u(3,`Retry`),d()()}t&2&&(m(),B(` `,n,` `))}function DF(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`)(2,`button`,15),C(`click`,function(){let a=M(e).$implicit;return T(v(3).inspectCandidate(a.configurationId))}),u(3),d()(),c(4,`td`),u(5),j(6,`number`),d(),c(7,`td`),u(8),j(9,`number`),d(),c(10,`td`),u(11),d(),c(12,`td`),u(13),d()()}if(t&2){let e=n.$implicit;m(3),B(` `,e.configurationId,` `),m(2),w(_e(6,8,e.occurrences)),m(3),w(_e(9,10,e.evaluationPlacements)),m(3),Si(` `,e.evaluations.running,` / `,e.evaluations.succeeded,` / `,e.evaluations.rejected,` / `,e.evaluations.failed,` `),m(2),w(e.elitePosition??`—`)}}function IF(t,n){if(t&1&&(c(0,`tr`)(1,`td`,41),u(2),d()()),t&2){let e=v(3);m(2),B(` `,e.loading()?`Loading configurations…`:`No matching configurations.`,` `)}}function kF(t,n){if(t&1){let e=$();c(0,`div`,18)(1,`mat-card`,19),E(2,cF,9,2)(3,gF,36,8),d(),E(4,xF,42,22,`mat-card`,20),E(5,CF,8,2,`mat-card`,21),c(6,`mat-card`,21)(7,`mat-card-header`)(8,`mat-card-title`),u(9,`Matching configurations`),d()(),c(10,`mat-card-content`),E(11,SF,4,4,`p`)(12,wF,2,1,`p`),E(13,EF,4,1,`p`,22),c(14,`div`,23)(15,`table`,24)(16,`thead`)(17,`tr`)(18,`th`,25),u(19,`Configuration`),d(),c(20,`th`,25),u(21,`Tree placements`),d(),c(22,`th`,25),u(23,`Evaluation exposure`),d(),c(24,`th`,25),u(25,`Running / succeeded / rejected / failed`),d(),c(26,`th`,25),u(27,`Elite rank`),d()()(),c(28,`tbody`),ue(29,DF,14,12,`tr`,null,JP,!1,IF,3,1,`tr`),d()()(),c(32,`div`,26)(33,`span`),u(34),j(35,`number`),d(),c(36,`button`,27),C(`click`,function(){M(e);let a=v(2);return T(a.offset.set(a.offset()-25))}),u(37,` Previous `),d(),c(38,`span`),u(39),d(),c(40,`button`,27),C(`click`,function(){M(e);let a=v(2);return T(a.offset.set(a.page()?.nextOffset??0))}),u(41,` Next `),d()()()()()}if(t&2){let e,i,a,o=v(2);m(2),D(o.view()===`usage`?2:3),m(2),D((e=o.selected())?4:-1,e),m(),D(o.view()===`usage`&&(o.incoming().length>0||o.outgoing().length>0)?5:-1),m(6),D((i=o.relationship())?11:12,i),m(2),D((a=o.pageError())?13:-1,a),m(),ne(`aria-busy`,o.loading()),m(15),me(o.page()?.candidates??nt(13,ZP)),m(5),B(``,_e(35,11,o.page()?.total??0),` matching configurations`),m(2),S(`disabled`,o.offset()===0||o.loading()),m(3),B(`Page `,o.offset()/25+1),m(),S(`disabled`,o.page()?.nextOffset==null||o.loading())}}function MF(t,n){if(t&1&&(c(0,`div`,16)(1,`article`)(2,`span`),u(3,`Decoded configurations`),d(),c(4,`strong`),u(5),j(6,`number`),j(7,`number`),d()(),c(8,`article`)(9,`span`),u(10),d(),c(11,`strong`),u(12),j(13,`number`),d()(),c(14,`article`)(15,`span`),u(16,`Distinct components`),d(),c(17,`strong`),u(18),j(19,`number`),d()(),c(20,`article`)(21,`span`),u(22,`Accepted evaluations`),d(),c(23,`strong`),u(24),j(25,`number`),d()()(),E(26,aF,3,3,`p`,17),E(27,oF,2,1,`p`,14)(28,rF,2,0,`p`,14)(29,kF,42,14,`div`,18)),t&2){let e=n,i=v();m(5),Me(``,_e(6,8,e.decodedConfigurationCount),` / `,_e(7,10,e.configurationCount)),m(5),w(i.basis()===`CANDIDATE`?`Component placements`:`Evaluation placements`),m(2),w(_e(13,12,i.basis()===`CANDIDATE`?e.candidatePlacementCount:e.evaluationPlacementCount)),m(6),w(_e(19,14,e.components.length)),m(6),w(_e(25,16,e.evaluationCount)),m(2),D(e.unavailableConfigurationCount>0?26:-1),m(),D(e.components.length===0?27:i.components().length===0?28:29)}}function TF(t,n){t&1&&(c(0,`p`,14),u(1,`Loading component usage…`),d())}var Fm=class t{store=f(ja);dialog=f(Ho);destroyRef=f(Ae);basis=k(`CANDIDATE`);view=k(`usage`);search=k(``);matrixLimit=k(20);matrixPercent=k(!1);selectedName=k(null);selectedEdge=k(null);offset=k(0);page=k(null);loading=k(!1);pageError=k(null);pageRefresh=k(0);request=0;pageKey=null;destroyed=!1;components=K(()=>$n.components(this.store.componentUsage(),this.basis(),this.search()));selected=K(()=>this.components().find(n=>n.name===this.selectedName())??this.components()[0]);relationship=K(()=>{let n=this.selectedEdge();return this.store.componentUsage()?.relationships.find(e=>n&&e.parent===n.parent&&e.role===n.role&&e.child===n.child)??null});incoming=K(()=>$n.relationships(this.store.componentUsage(),this.selected()?.name,`incoming`,this.basis()));outgoing=K(()=>$n.relationships(this.store.componentUsage(),this.selected()?.name,`outgoing`,this.basis()));treemap=K(()=>$n.treemap(this.components(),this.basis(),n=>this.selectComponent(n)));sankey=K(()=>$n.sankey(this.selected()?.name,this.incoming(),this.outgoing(),this.basis(),n=>this.selectRelationship(n)));matrix=K(()=>$n.matrix(this.store.componentUsage(),this.basis(),this.search(),this.matrixLimit()));selector=K(()=>{let n=this.relationship();if(n)return{parent:n.parent,role:n.role,child:n.child};let e=this.selected()?.name;return e?{component:e}:null});selectedCoverage=K(()=>{let n=this.store.componentUsage()?.decodedConfigurationCount??0;return n?100*(this.selected()?.configurationCount??0)/n:0});selectionKey=K(()=>JSON.stringify(this.selector()));constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.request++}),vt(()=>{this.store.componentScope(),this.basis(),this.selectionKey(),this.store.status()?.runId,it(()=>this.offset.set(0))}),vt(()=>{let n=this.store.componentUsage(),e=this.selector(),i=this.store.componentScope(),a=this.basis(),o=this.offset();this.pageRefresh(),it(()=>{if(!n||!e){this.request++,this.page.set(null),this.loading.set(!1),this.pageError.set(null);return}this.loadPage({scope:i,basis:a,selector:e,offset:o,limit:25})})})}selectComponent(n){this.selectedName.set(n),this.selectedEdge.set(null)}selectRelationship(n){this.selectedName.set(n.child),this.selectedEdge.set(n)}updateSearch(n){this.search.set(n.target.value),this.selectedEdge.set(null)}count(n){return $n.count(n,this.basis())}cellLabel(n){return this.matrixPercent()?`${$n.parentPercent(n,this.store.componentUsage()).toFixed(1)}%`:this.count(n).toLocaleString()}cellColor(n){let e=this.matrixPercent()?$n.parentPercent(n,this.store.componentUsage())/100:this.count(n)/(this.matrix().maximum||1);return`rgba(0, 137, 123, ${.08+Math.max(0,Math.min(1,e))*.65})`}cellDescription(n){return`${n.parent}, ${n.role}, ${n.child}: ${this.cellLabel(n)}`}inspectCandidate(n){this.dialog.open(hs,{data:n,width:`46rem`,maxWidth:`94vw`})}retryPage(){this.pageRefresh.update(n=>n+1)}async loadPage(n){let e=++this.request,i=this.store.componentUsage()?.runId;this.loading.set(!0),this.pageError.set(null);let a=JSON.stringify([i,n]);a!==this.pageKey&&this.page.set(null),this.pageKey=a;try{let o=await this.store.loadComponentCandidates(n);if(this.destroyed||e!==this.request)return;if(o.runId!==i||o.scope!==n.scope)throw new Error(`Configurations belong to a different run or scope`);if(n.offset>0&&o.total<=n.offset){this.offset.set(0);return}this.page.set(o)}catch(o){!this.destroyed&&e===this.request&&this.pageError.set(o instanceof Error?o.message:`Unable to load configurations`)}finally{!this.destroyed&&e===this.request&&this.loading.set(!1)}}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-component-usage-dashboard`]],decls:33,vars:7,consts:[[1,`component-page`],[1,`controls`],[`appearance`,`outline`],[3,`selectionChange`,`value`],[`value`,`ALL`],[`value`,`CURRENT_ELITES`],[`value`,`CANDIDATE`],[`value`,`EVALUATION`],[`matInput`,``,3,`input`,`value`],[`aria-label`,`Component visualization`,3,`change`,`value`],[`value`,`usage`],[`value`,`matrix`],[1,`explanation`],[`role`,`alert`,1,`warning`],[1,`empty`],[`mat-button`,``,3,`click`],[1,`totals`],[1,`warning`],[1,`layout`],[`appearance`,`outlined`,1,`visualization`],[`appearance`,`outlined`,1,`inspector`],[`appearance`,`outlined`,1,`wide`],[`role`,`alert`],[1,`table-scroll`],[1,`candidate-table`],[`scope`,`col`],[1,`pagination`],[`mat-button`,``,3,`click`,`disabled`],[3,`model`],[`aria-label`,`Select a component`,1,`component-list`],[`type`,`button`,3,`selected`],[`type`,`button`,3,`click`],[1,`matrix-controls`],[3,`value`],[`aria-label`,`Matrix values`,3,`change`,`value`],[`tabindex`,`0`,`aria-label`,`Scrollable component relationship matrix`,1,`matrix-scroll`],[1,`matrix`],[`scope`,`row`],[`type`,`button`,3,`background-color`],[1,`relationships`],[`type`,`button`],[`colspan`,`5`]],template:function(e,i){if(e&1&&(c(0,`section`,0)(1,`div`,1)(2,`mat-form-field`,2)(3,`mat-label`),u(4,`Configurations`),d(),c(5,`mat-select`,3),C(`selectionChange`,function(o){return i.store.selectComponentScope(o.value)}),c(6,`mat-option`,4),u(7,`All evaluated candidates`),d(),c(8,`mat-option`,5),u(9,`Current elites`),d()()(),c(10,`mat-form-field`,2)(11,`mat-label`),u(12,`Count by`),d(),c(13,`mat-select`,3),C(`selectionChange`,function(o){return i.basis.set(o.value)}),c(14,`mat-option`,6),u(15,`Candidate structures`),d(),c(16,`mat-option`,7),u(17,`Evaluation exposure`),d()()(),c(18,`mat-form-field`,2)(19,`mat-label`),u(20,`Find component`),d(),c(21,`input`,8),C(`input`,function(o){return i.updateSearch(o)}),d()(),c(22,`mat-button-toggle-group`,9),C(`change`,function(o){return i.view.set(o.value)}),c(23,`mat-button-toggle`,10),u(24,`Usage map`),d(),c(25,`mat-button-toggle`,11),u(26,`Relationship matrix`),d()()(),c(27,`p`,12),E(28,tF,1,0)(29,nF,1,0),d(),E(30,iF,4,1,`p`,13),E(31,MF,30,18)(32,TF,2,0,`p`,14),d()),e&2){let a,o;m(5),S(`value`,i.store.componentScope()),m(8),S(`value`,i.basis()),m(8),S(`value`,i.search()),m(),S(`value`,i.view()),m(6),D(i.basis()===`CANDIDATE`?28:29),m(2),D((a=i.store.componentError())?30:-1,a),m(),D((o=i.store.componentUsage())?31:i.store.componentError()?-1:32,o)}},dependencies:[Ft,tn,aE,Hb,Pm,zn,Bn,jn,Ma,Bu,ka,di,Ti,na,Jr,Zr,Pa,Oa,Un,Ai,En],styles:[`[_nghost-%COMP%]{display:block}.component-page[_ngcontent-%COMP%]{padding:1.4rem 0}.controls[_ngcontent-%COMP%], .matrix-controls[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:1rem;align-items:center}.controls[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{flex:1 1 12rem}.explanation[_ngcontent-%COMP%]{color:#526477;line-height:1.5;font-size:.9rem;margin:.5rem 0 1.25rem}.totals[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(4,1fr);gap:1rem;margin-bottom:1.4rem}.totals[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{border:1px solid #d9e3ec;border-radius:.8rem;padding:1rem;background:#fff}.totals[_ngcontent-%COMP%]   span[_ngcontent-%COMP%], .totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{display:block}.totals[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:#526477;font-size:.85rem}.totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{color:#174b80;margin-top:.4rem;font-size:1.5rem}.warning[_ngcontent-%COMP%]{background:#fff5dc;padding:.8rem 1rem;border-radius:.6rem}.empty[_ngcontent-%COMP%]{padding:2rem;text-align:center;color:#526477}.layout[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(0,2fr) minmax(18rem,1fr);gap:1.25rem}.layout[_ngcontent-%COMP%] > *[_ngcontent-%COMP%]{min-width:0}.wide[_ngcontent-%COMP%]{grid-column:1/-1}mat-card-header[_ngcontent-%COMP%]{margin-bottom:1rem}.component-list[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.3rem;max-height:15rem;overflow:auto;margin-top:1rem}.component-list[_ngcontent-%COMP%]   button[_ngcontent-%COMP%], .relationships[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{border:0;background:#f3f7fb;cursor:pointer;border-radius:.4rem;padding:.65rem;display:flex;justify-content:space-between;gap:.5rem;text-align:left;color:#193b59}.component-list[_ngcontent-%COMP%]   button.selected[_ngcontent-%COMP%]{background:#d5e8f7;outline:2px solid #2265a7;outline-offset:-2px}.component-list[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{overflow-wrap:anywhere}.relationships[_ngcontent-%COMP%]{max-height:17rem;overflow:auto;display:grid;gap:.35rem}.relationships[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{display:block;color:#526477;margin-top:.25rem}.relationships[_ngcontent-%COMP%]   button[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{text-align:right}.relationships[_ngcontent-%COMP%]   p[_ngcontent-%COMP%]{color:#526477}dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:1fr auto;gap:.8rem;font-size:.9rem}dt[_ngcontent-%COMP%]{color:#526477}dd[_ngcontent-%COMP%]{margin:0;text-align:right}h3[_ngcontent-%COMP%]{font-size:1rem;margin:1.4rem 0 .7rem}.matrix-scroll[_ngcontent-%COMP%], .table-scroll[_ngcontent-%COMP%]{overflow:auto}table[_ngcontent-%COMP%]{border-collapse:separate;border-spacing:0;font-size:.85rem;width:100%}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.65rem;border-bottom:1px solid #dce5ee;text-align:left}th[_ngcontent-%COMP%]{color:#526477;font-weight:600}.matrix[_ngcontent-%COMP%]   caption[_ngcontent-%COMP%]{text-align:left;padding:.5rem 0;color:#526477}.matrix[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]{min-width:7rem}.matrix[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{display:block;margin-top:.2rem;font-weight:400}.matrix[_ngcontent-%COMP%]   tbody[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]{position:sticky;left:0;background:#fff;z-index:1}.matrix[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]{padding:.2rem;text-align:center}.matrix[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{width:100%;min-width:5rem;min-height:2.8rem;border:0;border-radius:.25rem;cursor:pointer;color:#112e3b}.pagination[_ngcontent-%COMP%]{display:flex;justify-content:flex-end;align-items:center;flex-wrap:wrap;gap:.7rem;margin-top:.8rem}.pagination[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:first-child{margin-right:auto}@media(max-width:1050px){.layout[_ngcontent-%COMP%]{grid-template-columns:1fr}.totals[_ngcontent-%COMP%]{grid-template-columns:repeat(2,1fr)}}@media(max-width:600px){.component-list[_ngcontent-%COMP%]{grid-template-columns:1fr}.controls[_ngcontent-%COMP%]   mat-button-toggle-group[_ngcontent-%COMP%], .matrix-controls[_ngcontent-%COMP%]   mat-button-toggle-group[_ngcontent-%COMP%]{max-width:100%;overflow:auto}.totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font-size:1.2rem}}`]})};var Lm=()=>[];var NF=(t,n)=>n.key;var sE=(t,n)=>n.id;var lE=(t,n)=>n.configurationId;var AF=(t,n)=>n.instanceName;var RF=(t,n)=>n.iteration;function OF(t,n){if(t&1){let e=$();c(0,`button`,19),C(`click`,function(){M(e);return T(v().showIssues())}),c(1,`span`,53)(2,`strong`),u(3),d(),c(4,`small`),u(5),d()()()}if(t&2){let e=n.$implicit;m(2),G(`critical`,e.severity===`critical`),m(),w(e.title),m(2),w(e.message)}}function PF(t,n){t&1&&(c(0,`span`,10),u(1,`No evaluation alerts`),d())}function FF(t,n){if(t&1){let e=$();c(0,`button`,19),C(`click`,function(){M(e);return T(v().showIssues())}),c(1,`strong`),u(2,`View all issues`),d()()}}function LF(t,n){if(t&1&&(c(0,`section`,11)(1,`div`,54)(2,`span`,55),u(3,`Evaluation budget`),d(),c(4,`strong`),u(5),j(6,`number`),j(7,`number`),d(),c(8,`span`),u(9),j(10,`number`),d()(),U(11,`mat-progress-bar`,56),c(12,`div`,57),U(13,`span`,58),u(14),d()()),t&2){let e=v(),i=Nr(0);m(5),Me(``,_e(6,7,i.budget.used),` / `,_e(7,9,i.budget.maximum)),m(4),B(``,_e(10,11,i.budget.remaining),` remaining`),m(2),S(`value`,e.store.budgetPercent()),m(2),G(`complete`,i.state===`COMPLETED`),m(),B(` `,e.phaseLabel(i.phase),` `)}}function VF(t,n){if(t&1){let e=$();c(0,`section`,12)(1,`strong`),u(2,`Monitor connection problem`),d(),c(3,`span`),u(4),d(),c(5,`button`,50),C(`click`,function(){M(e);return T(v().store.retryNow())}),u(6,`Retry now`),d()()}t&2&&(m(4),w(n))}function BF(t,n){if(t&1&&(c(0,`section`,13)(1,`strong`),u(2),d(),c(3,`span`),u(4),d()()),t&2){let e=n;m(2),w(e.type),m(2),w(e.message??`The tuning run stopped unexpectedly.`)}}function jF(t,n){if(t&1){let e=$();c(0,`button`,19),C(`click`,function(){M(e);let a=v().$implicit;return T(v().preferences.show(a.id))}),u(1),d()}if(t&2){let e=v().$implicit;m(),B(` Show `,e.label,` `)}}function zF(t,n){if(t&1&&E(0,jF,2,1,`button`,9),t&2){let e=n.$implicit;D(v().preferences.isHidden(e.id)?0:-1)}}function HF(t,n){if(t&1){let e=$();c(0,`button`,59),C(`click`,function(){let a=M(e).$implicit;return T(v().inspectCandidate(a.configurationId))}),c(1,`span`,60),u(2),d(),c(3,`span`)(4,`strong`),u(5),d(),c(6,`small`),u(7),d()()()}if(t&2){let e=n.$implicit,i=v();m(),tt(i.rankClass(e.position)),m(),w(e.position),m(3),B(`Configuration `,e.configurationId),m(2),w(i.parameterPreview(e))}}function UF(t,n){t&1&&(c(0,`p`,32),u(1,`Elites appear after the first completed race checkpoint.`),d())}function $F(t,n){if(t&1){let e=$();c(0,`button`,61),C(`click`,function(){M(e);return T(v().inspectSearchSpace())}),u(1,` Inspect search space `),d()}}function GF(t,n){if(t&1&&(c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),j(7,`number`),d(),c(8,`td`),u(9),d(),c(10,`td`),u(11),d()()),t&2){let e=n.$implicit;m(2),w(e.instanceName),m(2),w(e.evaluations),m(2),B(` `,e.bestCost===null?`—`:ke(7,5,e.bestCost,`1.0-5`),` `),m(3),w(e.issues),m(2),w(e.slow)}}function WF(t,n){t&1&&(c(0,`tr`)(1,`td`,62),u(2,`No instance activity yet.`),d()())}function qF(t,n){t&1&&u(0,` Final result `)}function YF(t,n){t&1&&u(0,` Latest completed checkpoint `)}function KF(t,n){if(t&1){let e=$();c(0,`button`,59),C(`click`,function(){let a=M(e).$implicit;return T(v().inspectCandidate(a.configurationId))}),c(1,`span`,60),u(2),d(),c(3,`span`)(4,`strong`),u(5),d(),c(6,`small`),u(7),d()()()}if(t&2){let e=n.$implicit,i=v();m(),tt(i.rankClass(e.position)),m(),w(e.position),m(3),B(`Configuration `,e.configurationId),m(2),w(i.parameterPreview(e))}}function QF(t,n){t&1&&(c(0,`p`,32),u(1,`No elite snapshot is available yet.`),d())}function XF(t,n){if(t&1&&(c(0,`article`)(1,`strong`),u(2),d(),c(3,`span`),u(4),j(5,`date`),d(),c(6,`span`),u(7),j(8,`number`),d(),c(9,`span`),u(10),d()()),t&2){let e=n.$implicit;m(2),B(`Checkpoint `,e.iteration),m(2),w(ke(5,4,e.updatedAt,`medium`)),m(3),B(``,_e(8,7,e.progress.experimentsUsed),` evaluations used`),m(3),B(``,e.elites.length,` elites`)}}function ZF(t,n){t&1&&(c(0,`p`,32),u(1,`No completed race checkpoints yet.`),d())}function JF(t,n){t&1&&U(0,`app-component-usage-dashboard`)}function e2(t,n){t&1&&(c(0,`mat-tab`,48),rt(1,JF,1,0,`ng-template`,63),d())}function t2(t,n){if(t&1&&(c(0,`a`,52)(1,`span`)(2,`strong`),u(3),d(),c(4,`small`),u(5),d()(),c(6,`span`),u(7),d(),c(8,`span`),u(9,`Download`),d()()),t&2){let e=n.$implicit,i=v();S(`href`,i.store.artifactUrl(e.id),cn),m(3),w(e.filename),m(2),w(e.mediaType),m(2),w(i.artifactSize(e))}}function n2(t,n){t&1&&(c(0,`p`,32),u(1,` Artifacts appear as IRACE writes logs, plots, workspace data, and final elites. `),d())}var Vm=class t{store=f(ja);preferences=f(Va);panels=Ob;selectedTab=k(0);forceIssues=k(!1);dialog=f(Ho);constructor(){this.store.start()}selectTab(n){this.selectedTab.set(n.index),n.tab.textLabel!==`Evaluations`&&this.forceIssues.set(!1),n.tab.textLabel===`Artifacts`&&this.store.refreshArtifacts()}showIssues(){this.forceIssues.set(!0),this.selectedTab.set(1)}inspectCandidate(n){this.dialog.open(hs,{data:n,width:`46rem`,maxWidth:`94vw`})}inspectSearchSpace(){let n=this.store.searchSpace();n&&this.dialog.open(Rm,{data:n,maxWidth:`96vw`})}rankClass(n){return n<=3?`rank-${n}`:`rank-other`}phaseLabel(n){return n.toLowerCase().split(`_`).map(e=>e.charAt(0).toUpperCase()+e.slice(1)).join(` `)}elapsedLabel(n){if(n===null)return`—`;let e=Math.floor(n/1e3),i=Math.floor(e/3600),a=Math.floor(e%3600/60),o=e%60;return i>0?`${i}h ${a}m`:`${a}m ${o}s`}metricLabel(){let n=this.store.status()?.metric;if(!n)return`Waiting for metric details`;let e=n.kind===`AREA_UNDER_CURVE`?`Area under curve`:`Final objective`,i=n.negated?`maximized, converted to cost`:`minimized`;return`${n.objectiveName} \xB7 ${e} \xB7 ${i}`}parameterPreview(n){let e=Object.entries(n.parameters);return e.length===0?`No parameters`:e.slice(0,3).map(([i,a])=>`${i}=${a}`).join(` · `)}artifactSize(n){return n.size<1024?`${n.size} B`:n.size<1048576?`${(n.size/1024).toFixed(1)} KB`:`${(n.size/1048576).toFixed(1)} MB`}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-tuning-dashboard`]],decls:183,vars:47,consts:[[`alertsMenu`,`matMenu`],[`panelMenu`,`matMenu`],[1,`topbar`],[1,`brand`],[`src`,`favicon.svg`,`alt`,``],[1,`toolbar-spacer`],[`aria-label`,`Synchronization state`],[`mat-button`,``,`type`,`button`,`matBadgeColor`,`warn`,`aria-label`,`Tuning alerts`,1,`alerts-button`,3,`menuOpened`,`matMenuTriggerFor`,`matBadge`,`matBadgeHidden`],[1,`alerts-menu`],[`mat-menu-item`,``,`type`,`button`],[1,`menu-empty`],[`aria-label`,`Evaluation budget`,1,`budget-ribbon`],[`role`,`alert`,1,`request-error`],[`role`,`alert`,1,`run-error`],[`animationDuration`,`150ms`,3,`selectedTabChange`,`selectedIndex`],[`label`,`Overview`],[1,`tab-page`,`overview`],[1,`overview-actions`],[`mat-button`,``,`type`,`button`,3,`matMenuTriggerFor`],[`mat-menu-item`,``,`type`,`button`,3,`click`],[`panelId`,`evaluation-health`,`title`,`Evaluation health`,1,`wide`],[1,`kpi-grid`],[1,`kpi`],[1,`kpi`,`success`],[1,`kpi`,`warning`],[1,`kpi`,`danger`],[`panelId`,`evaluation-cost`,`title`,`Evaluation cost`],[3,`model`],[`panelId`,`elite-evolution`,`title`,`Elite rank evolution`],[`panelId`,`current-elites`,`title`,`Current elites`],[1,`elite-list`],[`type`,`button`,1,`elite-row`],[1,`empty-state`],[`panelId`,`run-information`,`title`,`Run information`],[1,`run-info`],[`mat-stroked-button`,``,`type`,`button`],[`label`,`Evaluations`],[1,`tab-page`],[1,`two-column`],[`appearance`,`outlined`],[1,`summary-scroll`],[1,`summary-table`],[`appearance`,`outlined`,1,`evaluation-card`],[3,`candidateSelected`,`evaluations`,`forceIssues`],[`label`,`Elites`],[1,`two-column`,`elite-layout`],[1,`elite-list`,`detailed`],[1,`history-list`],[`label`,`Components`],[`label`,`Artifacts`],[`mat-button`,``,`type`,`button`,3,`click`],[1,`artifact-list`],[`download`,``,3,`href`],[1,`alert-item`],[1,`budget-copy`],[1,`eyebrow`],[`mode`,`determinate`,3,`value`],[1,`phase`],[1,`phase-dot`],[`type`,`button`,1,`elite-row`,3,`click`],[1,`medal`],[`mat-stroked-button`,``,`type`,`button`,3,`click`],[`colspan`,`5`,1,`empty-state`],[`matTabContent`,``]],template:function(e,i){if(e&1&&(Tr(0),c(1,`mat-toolbar`,2)(2,`span`,3),U(3,`img`,4),u(4,` Mork tuning monitor`),d(),U(5,`span`,5),c(6,`mat-chip-set`,6)(7,`mat-chip`),u(8),d()(),c(9,`button`,7),C(`menuOpened`,function(){return i.store.markAlertsSeen()}),u(10,` Alerts `),d(),c(11,`mat-menu`,8,0),ue(13,OF,6,4,`button`,9,NF,!1,PF,2,0,`span`,10),E(16,FF,3,0,`button`,9),d()(),E(17,LF,15,13,`section`,11),c(18,`main`),E(19,VF,7,1,`section`,12),E(20,BF,5,2,`section`,13),c(21,`mat-tab-group`,14),C(`selectedTabChange`,function(o){return i.selectTab(o)}),c(22,`mat-tab`,15)(23,`section`,16)(24,`div`,17)(25,`span`),u(26,`Live view of the current tuning run`),d(),c(27,`button`,18),u(28,`Panels`),d(),c(29,`mat-menu`,null,1),ue(31,zF,1,1,null,null,sE),c(33,`button`,19),C(`click`,function(){return i.preferences.reset()}),u(34,` Restore all panels `),d()()(),c(35,`app-monitor-panel`,20)(36,`section`,21)(37,`article`,22)(38,`span`),u(39,`Running`),d(),c(40,`strong`),u(41),d()(),c(42,`article`,23)(43,`span`),u(44,`Succeeded`),d(),c(45,`strong`),u(46),d()(),c(47,`article`,24)(48,`span`),u(49,`Rejected`),d(),c(50,`strong`),u(51),d()(),c(52,`article`,25)(53,`span`),u(54,`Failed`),d(),c(55,`strong`),u(56),d()(),c(57,`article`,24)(58,`span`),u(59,`Slow`),d(),c(60,`strong`),u(61),d()()()(),c(62,`app-monitor-panel`,26),U(63,`app-dashboard-chart`,27),d(),c(64,`app-monitor-panel`,28),U(65,`app-dashboard-chart`,27),d(),c(66,`app-monitor-panel`,29)(67,`div`,30),ue(68,HF,8,5,`button`,31,lE,!1,UF,2,0,`p`,32),d()(),c(71,`app-monitor-panel`,33)(72,`dl`,34)(73,`dt`),u(74,`Run ID`),d(),c(75,`dd`),u(76),d(),c(77,`dt`),u(78,`Mode`),d(),c(79,`dd`),u(80),d(),c(81,`dt`),u(82,`Elapsed`),d(),c(83,`dd`),u(84),d(),c(85,`dt`),u(86,`Training instances`),d(),c(87,`dd`),u(88),d(),c(89,`dt`),u(90,`Generated parameters`),d(),c(91,`dd`),u(92),d(),c(93,`dt`),u(94,`Cost metric`),d(),c(95,`dd`),u(96),d(),c(97,`dt`),u(98,`Last update`),d(),c(99,`dd`),u(100),j(101,`date`),d()(),E(102,$F,2,0,`button`,35),d()()(),c(103,`mat-tab`,36)(104,`section`,37)(105,`section`,38)(106,`mat-card`,39)(107,`mat-card-header`)(108,`mat-card-title`),u(109,`Evaluation activity`),d()(),c(110,`mat-card-content`),U(111,`app-dashboard-chart`,27),d()(),c(112,`mat-card`,39)(113,`mat-card-header`)(114,`mat-card-title`),u(115,`Instance summary`),d()(),c(116,`mat-card-content`,40)(117,`table`,41)(118,`thead`)(119,`tr`)(120,`th`),u(121,`Instance`),d(),c(122,`th`),u(123,`Runs`),d(),c(124,`th`),u(125,`Best cost`),d(),c(126,`th`),u(127,`Issues`),d(),c(128,`th`),u(129,`Slow`),d()()(),c(130,`tbody`),ue(131,GF,12,8,`tr`,null,AF,!1,WF,3,0,`tr`),d()()()()(),c(134,`mat-card`,42)(135,`mat-card-header`)(136,`mat-card-title`),u(137,`Evaluation ledger`),d()(),c(138,`mat-card-content`)(139,`app-evaluation-table`,43),C(`candidateSelected`,function(o){return i.inspectCandidate(o)}),d()()()()(),c(140,`mat-tab`,44)(141,`section`,37)(142,`section`,45)(143,`mat-card`,39)(144,`mat-card-header`)(145,`mat-card-title`),u(146,`Current elite set`),d(),c(147,`mat-card-subtitle`),E(148,qF,1,0)(149,YF,1,0),d()(),c(150,`mat-card-content`)(151,`div`,46),ue(152,KF,8,5,`button`,31,lE,!1,QF,2,0,`p`,32),d()()(),c(155,`mat-card`,39)(156,`mat-card-header`)(157,`mat-card-title`),u(158,`Rank history`),d()(),c(159,`mat-card-content`),U(160,`app-dashboard-chart`,27),d()()(),c(161,`mat-card`,39)(162,`mat-card-header`)(163,`mat-card-title`),u(164,`Race checkpoints`),d()(),c(165,`mat-card-content`,47),ue(166,XF,11,9,`article`,null,RF,!1,ZF,2,0,`p`,32),d()()()(),E(169,e2,2,0,`mat-tab`,48),c(170,`mat-tab`,49)(171,`section`,37)(172,`mat-card`,39)(173,`mat-card-header`)(174,`mat-card-title`),u(175,`Run artifacts`),d(),U(176,`span`,5),c(177,`button`,50),C(`click`,function(){return i.store.refreshArtifacts()}),u(178,`Refresh`),d()(),c(179,`mat-card-content`,51),ue(180,t2,10,4,`a`,52,sE,!1,n2,2,0,`p`,32),d()()()()()()),e&2){let a,o,r=ft(12),s=ft(30),l=cl(i.store.status());m(7),G(`live`,i.store.syncState()===`live`),m(),w(i.store.connectionLabel()),m(),S(`matMenuTriggerFor`,r)(`matBadge`,i.store.unseenAlertCount())(`matBadgeHidden`,i.store.unseenAlertCount()===0),m(4),me(i.store.alerts().slice(0,10)),m(3),D(i.store.alerts().length>0?16:-1),m(),D(l?17:-1),m(2),D((a=i.store.latestError())?19:-1,a),m(),D((o=l?.failure)?20:-1,o),m(),S(`selectedIndex`,i.selectedTab()),m(6),S(`matMenuTriggerFor`,s),m(4),me(i.panels),m(10),w(l?.evaluations?.running??0),m(5),w(l?.evaluations?.succeeded??0),m(5),w(l?.evaluations?.rejected??0),m(5),w(l?.evaluations?.failed??0),m(5),w(l?.evaluations?.slow??0),m(2),S(`model`,i.store.evaluationCostChart()),m(2),S(`model`,i.store.eliteRankChart()),m(3),me(i.store.elites()?.elites??nt(43,Lm)),m(8),w(l?.runId??`—`),m(4),w(l?.mode??`—`),m(4),w(i.elapsedLabel(l?.elapsedMillis??null)),m(4),w(l?.trainingInstanceCount??`Preparing…`),m(4),w(l?.generatedParameterCount??0),m(4),w(i.metricLabel()),m(4),w(ke(101,40,i.store.lastUpdated(),`mediumTime`)),m(2),D(i.store.searchSpace()?102:-1),m(9),S(`model`,i.store.evaluationActivityChart()),m(20),me(i.store.instanceSummaries()),m(8),S(`evaluations`,i.store.evaluations())(`forceIssues`,i.forceIssues()),m(9),D(i.store.elites()?.finalSnapshot?148:149),m(4),me(i.store.elites()?.elites??nt(44,Lm)),m(8),S(`model`,i.store.eliteRankChart()),m(6),me(i.store.eliteHistory()?.iterations??nt(45,Lm)),m(3),D(l?.mode===`AUTOCONFIG`?169:-1),m(11),me(i.store.artifacts()?.artifacts??nt(46,Lm))}},dependencies:[Ai,Am,Gw,$w,Ft,tn,zn,Bn,jn,Ma,Bu,ka,im,Ul,nm,ss,Ni,la,rs,cs,ls,bm,Xl,Zl,gm,_m,vm,ms,Fm,Xi,En],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh;color:var(--%NS%mat-sys-on-surface);background:radial-gradient(circle at 92% -5%,color-mix(in srgb,var(--%NS%mat-sys-tertiary) 14%,transparent),transparent 28rem),var(--%NS%mat-sys-surface-container-lowest)}.topbar[_ngcontent-%COMP%]{position:sticky;top:0;z-index:20;gap:.75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:color-mix(in srgb,var(--%NS%mat-sys-surface) 94%,transparent);-webkit-backdrop-filter:blur(12px);backdrop-filter:blur(12px)}.brand[_ngcontent-%COMP%]{display:flex;gap:.65rem;align-items:center;font:var(--%NS%mat-sys-title-large);font-weight:750;letter-spacing:-.025em}.brand[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:2rem;height:2rem}.toolbar-spacer[_ngcontent-%COMP%]{flex:1}mat-chip.live[_ngcontent-%COMP%]{--%NS%mdc-chip-elevated-container-color: color-mix(in srgb, var(--%NS%mat-sys-tertiary) 20%, transparent)}.alerts-button[_ngcontent-%COMP%]{margin-right:.5rem}.alert-item[_ngcontent-%COMP%]{display:grid;width:min(24rem,72vw);line-height:1.25}.alert-item[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{overflow:hidden;color:var(--%NS%mat-sys-on-surface-variant);text-overflow:ellipsis}.critical[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}.menu-empty[_ngcontent-%COMP%]{display:block;padding:1rem;color:var(--%NS%mat-sys-on-surface-variant)}.budget-ribbon[_ngcontent-%COMP%]{position:sticky;top:4rem;z-index:15;display:grid;grid-template-columns:minmax(15rem,auto) minmax(12rem,1fr) auto;gap:1.5rem;align-items:center;padding:.65rem max(1rem,(100% - 96rem) / 2);border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:color-mix(in srgb,var(--%NS%mat-sys-surface-container-low) 96%,transparent);-webkit-backdrop-filter:blur(12px);backdrop-filter:blur(12px)}.budget-copy[_ngcontent-%COMP%]{display:flex;gap:.55rem;align-items:baseline;white-space:nowrap}.budget-copy[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-body-small)}.eyebrow[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-small);text-transform:uppercase;letter-spacing:.07em}.phase[_ngcontent-%COMP%]{display:flex;gap:.4rem;align-items:center;font:var(--%NS%mat-sys-label-large);white-space:nowrap}.phase-dot[_ngcontent-%COMP%]{width:.6rem;height:.6rem;border-radius:50%;background:var(--%NS%mat-sys-tertiary);box-shadow:0 0 0 .2rem color-mix(in srgb,var(--%NS%mat-sys-tertiary) 18%,transparent)}.phase-dot.complete[_ngcontent-%COMP%]{background:#079447}main[_ngcontent-%COMP%]{width:min(100% - 2rem,96rem);margin:0 auto;padding-bottom:4rem}.request-error[_ngcontent-%COMP%], .run-error[_ngcontent-%COMP%]{display:flex;gap:.75rem;align-items:center;margin:1rem 0 0;padding:.75rem 1rem;border:1px solid var(--%NS%mat-sys-error);border-radius:.75rem;color:var(--%NS%mat-sys-on-error-container);background:var(--%NS%mat-sys-error-container)}.request-error[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{margin-left:auto}.tab-page[_ngcontent-%COMP%]{display:grid;gap:1rem;padding:1.25rem 0}.overview[_ngcontent-%COMP%]{grid-template-columns:repeat(2,minmax(0,1fr))}.wide[_ngcontent-%COMP%], .overview-actions[_ngcontent-%COMP%]{grid-column:1/-1}.overview-actions[_ngcontent-%COMP%]{display:flex;align-items:center;min-height:2rem;color:var(--%NS%mat-sys-on-surface-variant)}.overview-actions[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{margin-left:auto}.kpi-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(5,minmax(7rem,1fr));gap:.75rem}.kpi[_ngcontent-%COMP%]{display:grid;gap:.2rem;padding:.8rem 1rem;border-left:.25rem solid var(--%NS%mat-sys-primary);border-radius:.4rem;background:var(--%NS%mat-sys-surface-container-low)}.kpi[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-medium)}.kpi[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-headline-small)}.kpi.success[_ngcontent-%COMP%]{border-color:#079447}.kpi.warning[_ngcontent-%COMP%]{border-color:#ed8e00}.kpi.danger[_ngcontent-%COMP%]{border-color:var(--%NS%mat-sys-error)}.elite-list[_ngcontent-%COMP%]{display:grid;gap:.5rem}.elite-row[_ngcontent-%COMP%]{display:grid;grid-template-columns:2.3rem minmax(0,1fr);gap:.75rem;align-items:center;width:100%;padding:.65rem;border:1px solid transparent;border-radius:.5rem;color:inherit;background:transparent;text-align:left;cursor:pointer}.elite-row[_ngcontent-%COMP%]:hover{border-color:var(--%NS%mat-sys-outline-variant);background:var(--%NS%mat-sys-surface-container-low)}.elite-row[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{display:grid;min-width:0}.elite-row[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{overflow:hidden;color:var(--%NS%mat-sys-on-surface-variant);text-overflow:ellipsis;white-space:nowrap}.medal[_ngcontent-%COMP%]{display:grid;place-items:center;width:2rem;height:2rem;border-radius:50%;color:#292929;background:#d1d5db;font-weight:750}.rank-1[_ngcontent-%COMP%]{background:#f2c94c}.rank-2[_ngcontent-%COMP%]{background:#c7ccd4}.rank-3[_ngcontent-%COMP%]{background:#cd8b55}.rank-other[_ngcontent-%COMP%]{background:#e5e7eb}.run-info[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(8rem,auto) minmax(0,1fr);gap:.55rem 1rem;margin:0 0 1rem}.run-info[_ngcontent-%COMP%]   dt[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.run-info[_ngcontent-%COMP%]   dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}.two-column[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem}mat-card-header[_ngcontent-%COMP%]{align-items:center;padding-bottom:.75rem}.summary-scroll[_ngcontent-%COMP%]{max-height:21rem;overflow:auto}.summary-table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse}.summary-table[_ngcontent-%COMP%]   th[_ngcontent-%COMP%], .summary-table[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]{padding:.55rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left}.evaluation-card[_ngcontent-%COMP%]   mat-card-content[_ngcontent-%COMP%]{min-width:0}.history-list[_ngcontent-%COMP%]{display:grid;gap:.5rem}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{display:grid;grid-template-columns:1fr repeat(3,auto);gap:1rem;padding:.75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.artifact-list[_ngcontent-%COMP%]{display:grid}.artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(0,1fr) auto auto;gap:1.5rem;align-items:center;padding:1rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);color:var(--%NS%mat-sys-primary);text-decoration:none}.artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:first-child{display:grid}.artifact-list[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.empty-state[_ngcontent-%COMP%]{padding:1.5rem 0;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}@media(max-width:62rem){.overview[_ngcontent-%COMP%], .two-column[_ngcontent-%COMP%]{grid-template-columns:1fr}.kpi-grid[_ngcontent-%COMP%]{grid-template-columns:repeat(3,1fr)}.budget-ribbon[_ngcontent-%COMP%]{grid-template-columns:1fr auto}.budget-ribbon[_ngcontent-%COMP%]   mat-progress-bar[_ngcontent-%COMP%]{grid-column:1/-1;grid-row:2}}@media(max-width:42rem){.topbar[_ngcontent-%COMP%]   mat-chip-set[_ngcontent-%COMP%]{display:none}.brand[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-medium)}.budget-ribbon[_ngcontent-%COMP%]{top:3.5rem;gap:.5rem}.budget-copy[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child, .eyebrow[_ngcontent-%COMP%]{display:none}main[_ngcontent-%COMP%]{width:min(100% - 1rem,96rem)}.kpi-grid[_ngcontent-%COMP%]{grid-template-columns:repeat(2,1fr)}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%], .artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{grid-template-columns:1fr;gap:.2rem}}`]})};var Bm=class t{http=f(Di);destroy=f(Ae);base=`${In.apiBaseUrl}/api/autollm`;get(n,e={}){return wt(this.http.get(this.base+n,{params:e}).pipe(pn(this.destroy)))}post(n,e={}){return wt(this.http.post(this.base+n,e).pipe(pn(this.destroy)))}defaults(){return this.get(`/defaults`)}validate(n){return this.post(`/validate`,n)}probeTarget(n){return this.post(`/targets/probe`,n)}list(){return this.get(`/campaigns`)}create(n){return this.post(`/campaigns`,n)}baselineCheck(n){return this.post(`/campaigns/${encodeURIComponent(n)}/baseline-check`)}action(n,e){return this.post(`/campaigns/${encodeURIComponent(n)}/${e}`)}addTarget(n,e){return wt(this.http.put(`${this.base}/campaigns/${encodeURIComponent(n)}/targets/${encodeURIComponent(e.id)}`,e).pipe(pn(this.destroy)))}targetAction(n,e,i){let a=`/campaigns/${encodeURIComponent(n)}/targets/${encodeURIComponent(e)}`;return i===`remove`?wt(this.http.delete(this.base+a).pipe(pn(this.destroy))):this.post(`${a}/${i}`)}progress(n,e){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/progress`)}recovery(n){return this.get(`/campaigns/${encodeURIComponent(n)}/recovery`)}resumeTrial(n,e){return this.post(`/campaigns/${encodeURIComponent(n)}/trials/${e}/resume`)}failTrial(n,e,i){return this.post(`/campaigns/${encodeURIComponent(n)}/trials/${e}/fail`,{reason:i})}summary(n){return this.get(`/campaigns/${encodeURIComponent(n)}/summary`)}activity(n,e,i,a){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/activity`,{attempt:i,cursor:a})}comparison(n,e){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/comparison`)}artifacts(n){return this.get(`/campaigns/${encodeURIComponent(n)}/artifacts`)}preview(n,e,i){return this.get(`/campaigns/${encodeURIComponent(n)}/preview`,{path:e,lines:i})}artifactUrl(n,e){return`${this.base}/campaigns/${encodeURIComponent(n)}/artifact?${new URLSearchParams({path:e})}`}screenings(){return this.get(`/screenings`)}createScreening(n,e,i){return this.post(`/screenings`,{config:n,inputPath:e,trainingFraction:i,settings:{sotaFile:n.sotaFile,python:n.execution.ssh?.servers.find(a=>a.id===n.execution.ssh?.screeningServerId)?.environment?.MORK_SELECTOR_PYTHON??`python3`}})}screeningAction(n,e,i){return this.post(`/screenings/${encodeURIComponent(n)}/${e}`,{proposalSha256:i})}screeningReview(n){return this.get(`/screenings/${encodeURIComponent(n)}/review`)}screeningArtifactUrl(n,e){return`${this.base}/screenings/${encodeURIComponent(n)}/artifact?${new URLSearchParams({path:e})}`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var jm=class t{client=f(Bm);destroy=f(Ae);config=k(null);campaigns=k([]);active=k(``);selectedId=k(``);selectedTrial=k(1);campaign=K(()=>this.campaigns().find(n=>n.id===this.selectedId())??null);trial=K(()=>this.campaign()?.trials.find(n=>n.number===this.selectedTrial())??null);completed=K(()=>this.campaign()?.trials.filter(n=>[`SUCCEEDED`,`FAILED`].includes(n.status)).length??0);progress=k(null);activeProgress=k({});targets=K(()=>Object.entries(this.campaign()?.targets??{}).map(([n,e])=>r({id:n},e)));activeTrials=K(()=>this.campaign()?.trials.filter(n=>![`PENDING`,`SUCCEEDED`,`FAILED`].includes(n.status))??[]);now=k(Date.now());tuning=K(()=>this.progress()?.workload?.budget?this.progress().workload:null);generation=K(()=>this.progress()?.generation??null);generationAttempt=k(null);conversationAttempt=K(()=>{let n=this.trial()?.attempts?.filter(e=>[`GENERATION`,`REPAIR`].includes(e.stage))??[];return n.find(e=>e.number===this.generationAttempt())??n.at(-1)??null});activity=k([]);screenings=k([]);activeScreening=k(``);selectedScreening=k(``);screening=K(()=>this.screenings().find(n=>n.id===this.selectedScreening())??null);review=k(null);summary=k(null);comparison=k(null);artifacts=k([]);logPath=k(``);logLines=k(500);logFollow=k(!0);logPreview=k(null);logLoading=k(!1);logError=k(null);error=k(null);connectionError=k(null);busy=k(!1);targetStatus=k(null);recovery=k(null);recoveryError=k(null);trialRecovery=K(()=>this.recovery()?.trials[this.selectedTrial()]);timer=null;started=!1;destroyed=!1;logRequest=0;activityKey=``;activityCursor=0;activityPending=``;operations=0;constructor(){this.destroy.onDestroy(()=>{this.destroyed=!0,this.timer&&clearTimeout(this.timer)})}async start(){if(!this.started){this.started=!0;try{this.config.set(await this.client.defaults())}catch(n){this.connectionError.set(this.message(n))}await this.poll()}}async poll(){if(!this.destroyed)try{this.config()||this.config.set(await this.client.defaults());let[n,e]=await Promise.all([this.client.list(),this.client.screenings()]);if(this.destroyed)return;this.connectionError.set(null),this.campaigns.set(n.campaigns),this.active.set(n.active),this.screenings.set(e.jobs),this.activeScreening.set(e.active),this.selectedScreening()||this.selectedScreening.set(e.active||e.jobs[0]?.id||``),this.selectedId()||this.selectedId.set(n.active||n.campaigns[0]?.id||``),this.now.set(Date.now());let i=this.selectedId(),a=this.activeTrials(),[o]=await Promise.all([Promise.allSettled(a.map(s=>this.client.progress(i,s.number))),this.refreshRecovery()]);if(this.selectedId()===i&&!this.destroyed){let s={};o.forEach((l,h)=>{let p=a[h].number;s[p]=l.status===`fulfilled`?{progress:l.value,stale:!1}:{progress:this.activeProgress()[p]?.progress,stale:!0}}),this.activeProgress.set(s)}if(this.trial()){let s=this.selectedId(),l=this.selectedTrial(),[h,p]=await Promise.all([this.activeProgress()[l]?.progress??this.client.progress(s,l),this.client.comparison(s,l)]);this.selectedId()===s&&this.selectedTrial()===l&&!this.destroyed&&(this.progress.set(h),this.comparison.set(p))}let r=this.conversationAttempt();if(r){let s=this.selectedId(),l=this.selectedTrial(),h=`${s}/${l}/${r.number}`;this.activityKey!==h&&(this.activityKey=h,this.activityCursor=0,this.activityPending=``,this.activity.set([]));let p=this.activityCursor,g=await this.client.activity(s,l,r.number,p);if(this.activityKey===h&&this.selectedId()===s&&this.selectedTrial()===l&&this.conversationAttempt()?.number===r.number&&!this.destroyed){g.reset&&(this.activity.set([]),this.activityPending=``);let _=(this.activityPending+g.text).split(`
`);this.activityPending=_.pop()??``;let b=[];for(let y of _)if(y.trim())try{b.push(JSON.parse(y))}catch{}this.activity.update(y=>[...y,...b].slice(-200)),this.activityCursor=g.nextCursor}}this.logFollow()&&await this.refreshLog()}catch(n){this.destroyed||this.connectionError.set(this.message(n))}finally{this.destroyed||(this.timer=setTimeout(()=>{this.poll()},2e3))}}select(n){this.selectedId.set(n),this.selectedTrial.set(1),this.artifacts.set([]),this.recovery.set(null),this.recoveryError.set(null),this.selectLog(``),this.progress.set(null),this.comparison.set(null),this.watchGeneration(null)}selectTrial(n){this.selectedTrial.set(n),this.progress.set(null),this.comparison.set(null),this.selectLog(``),this.watchGeneration(null)}watchGeneration(n){this.generationAttempt.set(n),this.activityKey=``,this.activity.set([])}selectLog(n){this.logRequest++,this.logPath.set(n),this.logPreview.set(null),this.logError.set(null),this.logLoading.set(!1),n&&this.refreshLog()}setLogLines(n){!Number.isInteger(n)||n<1||n>5e3||(this.logLines.set(n),this.selectLog(this.logPath()))}async refreshLog(){let n=this.selectedId(),e=this.logPath(),i=this.logLines();if(!n||!e||this.logLoading()||this.destroyed)return;let a=++this.logRequest;this.logLoading.set(!0),this.logError.set(null);try{let o=await this.client.preview(n,e,i);if(a===this.logRequest&&n===this.selectedId()&&!this.destroyed){let r=this.logPreview();(r?.text!==o.text||r?.sizeBytes!==o.sizeBytes)&&this.logPreview.set(o)}}catch(o){a===this.logRequest&&!this.destroyed&&this.logError.set(this.message(o))}finally{a===this.logRequest&&!this.destroyed&&this.logLoading.set(!1)}}selectScreening(n){this.selectedScreening.set(n),this.review.set(null)}async createScreening(n,e){let i=this.config();i&&await this.operate(async()=>{let a=await this.client.createScreening(i,n,e);this.screenings.update(o=>[a,...o]),this.selectScreening(a.id),await this.client.screeningAction(a.id,`resume`),this.activeScreening.set(a.id)})}async reviewScreening(){let n=this.selectedScreening();await this.operate(async()=>{let e=await this.client.screeningReview(n);this.selectedScreening()===n&&this.review.set(s(r({},e),{jobId:n}))})}async screeningAction(n){let e=this.selectedScreening(),i=this.review();n===`approve`&&i?.jobId!==e||await this.operate(async()=>{await this.client.screeningAction(e,n,i?.proposalSha256)})}useSelection(){let n=this.screening();n?.status!==`APPROVED`||!n.mirror||this.config.update(e=>e&&s(r({},e),{trainingPath:`${n.mirror}/proposal/train.index`,comparisonPath:`${n.mirror}/proposal/test.index`}))}screeningArtifactUrl(n){return this.client.screeningArtifactUrl(this.selectedScreening(),n)}async refreshArtifacts(){if(!this.selectedId())return;let n=this.selectedId();try{let e=await this.client.artifacts(n);n===this.selectedId()&&!this.destroyed&&this.artifacts.set(e)}catch(e){this.error.set(this.message(e))}}async createAndStart(){let n=this.config();n&&await this.operate(async()=>{let e=await this.client.validate(n);if(!e.valid)throw new Error(e.errors.join(`; `));let i=await this.client.create(n);this.campaigns.update(o=>[i,...o]),this.select(i.id);let a=await this.client.baselineCheck(i.id);if(this.summary.set(await this.client.summary(i.id)),!a.withinLimit)throw new Error(`Baseline emits ${a.parameters} parameters; limit is ${a.maxParameters}. Prune the search space while preserving every seed before starting this draft.`);await this.client.action(i.id,`start`),this.active.set(i.id)})}async probeTarget(){let n=this.config();n&&await this.operate(async()=>{this.targetStatus.set(await this.client.probeTarget(n))})}async action(n){await this.operate(async()=>{let e=this.selectedId(),i=await this.client.action(e,n);this.updateCampaign(i),(n===`start`||n===`resume`)&&this.active.set(e),await this.refreshRecovery()})}async addTarget(n){let e=this.selectedId();await this.operate(async()=>{let i=await this.client.addTarget(e,n);this.campaigns.update(a=>a.map(o=>o.id===e?i:o))})}async targetAction(n,e){let i=this.selectedId();await this.operate(async()=>{this.updateCampaign(await this.client.targetAction(i,n,e)),await this.refreshRecovery()})}async refreshRecovery(){let n=this.selectedId();if(n)try{let e=await this.client.recovery(n);n===this.selectedId()&&!this.destroyed&&(this.recovery.set(e),this.recoveryError.set(null))}catch(e){n===this.selectedId()&&!this.destroyed&&(this.recovery.set(null),this.recoveryError.set(this.message(e)))}}async resumeTrial(){let n=this.selectedId(),e=this.selectedTrial();await this.operate(async()=>{this.updateCampaign(await this.client.resumeTrial(n,e)),await this.refreshRecovery()})}async failTrial(n){if(!n.trim())return;let e=this.selectedId(),i=this.selectedTrial();await this.operate(async()=>{this.updateCampaign(await this.client.failTrial(e,i,n.trim())),await this.refreshRecovery()})}updateCampaign(n){this.campaigns.update(e=>e.map(i=>i.id===n.id?n:i))}targetStale(n){return!n||this.now()-Date.parse(n)>3e4}async operate(n){this.operations++,this.busy.set(!0),this.error.set(null);try{await n()}catch(e){this.error.set(this.message(e))}finally{this.operations--,this.busy.set(this.operations>0)}}artifactUrl(n){return this.client.artifactUrl(this.selectedId(),n)}message(n){let e=n;return e.error?.message??e.message??`AutoLLM request failed`}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};var ic=class{static palettes=[[`#1565c0`,`#1976d2`,`#1e88e5`,`#0277bd`,`#039be5`,`#0288d1`],[`#ef6c00`,`#f4511e`,`#e64a19`,`#e53935`,`#d84315`,`#c62828`],[`#2e7d32`,`#388e3c`,`#00897b`,`#00796b`],[`#7b1fa2`,`#8e24aa`,`#6a1b9a`,`#5e35b1`]];static canUseLogarithmicScale(n,e){if(!e)return!1;let i=!1;for(let a of n)for(let o of a.curves[e]??[])if(!(!Number.isFinite(o.value)||!Number.isFinite(o.instant))){if(o.value<=0)return!1;i=!0}return i}static build(n,e,i){let a=[...new Set([`SOTA`,`IraceProposal`,...n.map(s=>s.algorithm)])],o=[];for(let s=0;s<a.length;s++){let l=a[s],h=this.palettes[s%this.palettes.length],p=`comparison-${encodeURIComponent(l)}`,g=n.filter(b=>b.algorithm===l).sort((b,y)=>b.iteration.localeCompare(y.iteration,void 0,{numeric:!0})),_=!1;for(let b of g){let y=(b.curves[e??``]??[]).filter(L=>Number.isFinite(L.value)&&Number.isFinite(L.instant)).map(L=>[L.instant/1e6,L.value]);if(!y.length)continue;let I=Number(b.iteration),P=Number.isFinite(I)?I+1:b.iteration,ie=Number.isInteger(I)&&I>=0?I:0;o.push({type:`line`,id:_?`${p}-repetition-${b.iteration}`:p,linkedTo:_?p:void 0,showInLegend:!_,name:`${l} \xB7 repetition ${P}`,custom:{algorithm:l},color:h[ie%h.length],dashStyle:`Solid`,opacity:.45,lineWidth:1.5,marker:{enabled:!1},states:{hover:{lineWidthPlus:1.5,opacity:.85},inactive:{opacity:.15}},data:y}),_=!0}}let r=i===`logarithmic`&&this.canUseLogarithmicScale(n,e)?`logarithmic`:`linear`;return{viewKey:JSON.stringify([n[0]?.instance,e,r]),options:{boost:{enabled:!1},chart:{type:`line`,animation:!1,reflow:!0,zooming:{type:`xy`,pinchType:`xy`},panning:{enabled:!0,type:`xy`},panKey:`shift`},title:{text:e??`Comparison convergence`},xAxis:{title:{text:`Time (milliseconds)`},minRange:0},yAxis:{title:{text:`Objective value`},type:r,minRange:0,startOnTick:!1,endOnTick:!1},legend:{maxHeight:64,symbolWidth:32,labelFormatter(){let s=this.options.custom?.algorithm;return typeof s==`string`?s:this.name??``}},plotOptions:{series:{animation:!1}},series:o}}}};var zm=class{static relativeLabels(n){if(!n.length)return new Map;let e=n[0].replaceAll(`\\`,`/`);for(let o of n.slice(1)){let r=o.replaceAll(`\\`,`/`),s=0;for(;s<e.length&&e[s]===r[s];)s++;e=e.slice(0,s)}let i=e.lastIndexOf(`/`)+1,a=new Map;for(let o of n)a.set(o,o.replaceAll(`\\`,`/`).slice(i));return a}};var Hm=class{static canPreview(n){return/\.(log|jsonl|txt|json|csv|tsv|md|xml|ya?ml|properties|index|r|rout|out|err)$/i.test(n)}};var cE=()=>[];var i2=()=>[`DRAFT`,`INTERRUPTED`,`CANCELLED`,`NEEDS_ATTENTION`];var a2=()=>[`AWAITING_APPROVAL`,`APPROVED`];var o2=()=>[`instances.csv`,`proposed-retained.index`,`proposed-train.index`,`proposed-test.index`,`manifest.json`];var r2=()=>[`INTERRUPTED`,`CANCELLED`,`NEEDS_ATTENTION`,`WAITING_FOR_TARGETS`];var s2=()=>[`SUCCEEDED`,`COMPLETED_WITH_FAILURES`];var l2=()=>[`BUSY`,`QUALIFYING`];var c2=()=>[`BLOCKED`,`INTERRUPTED`];var d2=()=>[`GENERATION`,`REPAIR`];var $b=(t,n)=>n.id;var u2=(t,n)=>n.instance;var Ub=(t,n)=>n.number;var m2=(t,n)=>n.path;var p2=(t,n)=>n.threadId;function h2(t,n){if(t&1&&(c(0,`p`,1),u(1),d()),t&2){let e=v();m(),w(e.store.error())}}function f2(t,n){if(t&1&&(c(0,`p`,1),u(1),d()),t&2){let e=v();m(),w(e.store.connectionError())}}function g2(t,n){if(t&1&&(c(0,`option`,4),u(1),d()),t&2){let e=n.$implicit;S(`value`,e.id),m(),w(e.id)}}function b2(t,n){if(t&1){let e=$();c(0,`fieldset`,25)(1,`legend`),u(2),d(),c(3,`label`),u(4,`ID`),c(5,`input`,2),C(`change`,function(a){let o=M(e).$index;return T(v(3).update(`execution.ssh.servers.`+o+`.id`,a))}),d()(),c(6,`label`),u(7,`SSH host or alias`),c(8,`input`,2),C(`change`,function(a){let o=M(e).$index;return T(v(3).update(`execution.ssh.servers.`+o+`.host`,a))}),d()(),c(9,`label`),u(10,`Remote workspace`),c(11,`input`,2),C(`change`,function(a){let o=M(e).$index;return T(v(3).update(`execution.ssh.servers.`+o+`.workspace`,a))}),d()(),c(12,`label`),u(13,`Java executable`),c(14,`input`,2),C(`change`,function(a){let o=M(e).$index;return T(v(3).update(`execution.ssh.servers.`+o+`.javaExecutable`,a))}),d()(),c(15,`label`),u(16,`Workers override (blank inherits)`),c(17,`input`,12),C(`change`,function(a){let o=M(e).$index;return T(v(3).update(`execution.ssh.servers.`+o+`.workers`,a,!0))}),d()(),c(18,`label`),u(19,`Heap override (blank inherits)`),c(20,`input`,2),C(`change`,function(a){let o=M(e).$index;return T(v(3).update(`execution.ssh.servers.`+o+`.heap`,a))}),d()(),c(21,`button`,26),C(`click`,function(){let a=M(e).$index;return T(v(3).removeServer(a))}),u(22,`Remove from configuration`),d()()}if(t&2){let e=n.$implicit,i=n.$index;m(2),B(`Server `,i+1),m(3),S(`value`,e.id),m(3),S(`value`,e.host),m(3),S(`value`,e.workspace),m(3),S(`value`,e.javaExecutable),m(3),S(`value`,e.workers??``),m(3),S(`value`,e.heap??``)}}function v2(t,n){if(t&1){let e=$();c(0,`label`),u(1,`Screening server`),c(2,`select`,2),C(`change`,function(a){M(e);return T(v(2).update(`execution.ssh.screeningServerId`,a))}),ue(3,g2,2,2,`option`,4,Co),d()(),c(5,`label`),u(6,`Local remote-runner JAR`),c(7,`input`,2),C(`change`,function(a){M(e);return T(v(2).update(`execution.ssh.runnerArtifact`,a))}),d()(),ue(8,b2,23,7,`fieldset`,25,Co),c(10,`button`,26),C(`click`,function(){M(e);return T(v(2).addServer())}),u(11,`Add server`),d()}if(t&2){let e=v();m(2),S(`value`,e.execution.ssh?.screeningServerId),m(),me(e.execution.ssh?.servers??nt(2,cE)),m(4),S(`value`,e.execution.ssh?.runnerArtifact??``),m(),me(e.execution.ssh?.servers??nt(3,cE))}}function _2(t,n){if(t&1){let e=$();c(0,`label`),u(1,`Worker count`),c(2,`input`,11),C(`change`,function(a){M(e);return T(v(2).update(`execution.workers`,a,!0))}),d()()}if(t&2){let e=v();m(2),S(`value`,e.execution.workers)}}function y2(t,n){if(t&1&&(c(0,`details`,20)(1,`summary`),u(2,`Resolved budgets and workload estimate`),d(),c(3,`pre`),u(4),j(5,`json`),d()()),t&2){let e=v(2);m(4),w(_e(5,1,e.store.summary()))}}function x2(t,n){if(t&1&&(c(0,`pre`),u(1),j(2,`json`),d()),t&2){let e=v(2);m(),w(_e(2,1,e.store.targetStatus()))}}function C2(t,n){if(t&1){let e=$();c(0,`mat-card`)(1,`mat-card-content`)(2,`details`,5)(3,`summary`),u(4,`Configure a campaign`),d(),c(5,`fieldset`,6)(6,`div`,7)(7,`label`),u(8,`Where should the actual workloads run?`),c(9,`select`,2),C(`change`,function(a){M(e);return T(v().executionTarget(a))}),c(10,`option`,8),u(11,`Select execution location`),d(),c(12,`option`,9),u(13,`Locally on this computer`),d(),c(14,`option`,10),u(15,`Configure SSH servers`),d()()(),E(16,v2,12,4),c(17,`label`),u(18,`Baseline repository`),c(19,`input`,2),C(`change`,function(a){M(e);return T(v().update(`project.repository`,a))}),d()(),c(20,`label`),u(21,`Git ref`),c(22,`input`,2),C(`change`,function(a){M(e);return T(v().update(`project.ref`,a))}),d()(),c(23,`label`),u(24,`Build command (JSON arguments)`),c(25,`input`,2),j(26,`json`),C(`change`,function(a){M(e);return T(v().buildCommand(a))}),d()(),c(27,`label`),u(28,`Built JAR path`),c(29,`input`,2),C(`change`,function(a){M(e);return T(v().update(`project.artifact`,a))}),d()(),c(30,`label`),u(31,`SOTA component JSON`),c(32,`input`,2),C(`change`,function(a){M(e);return T(v().update(`sotaFile`,a))}),d()(),c(33,`label`),u(34,`Additional seeds (optional)`),c(35,`input`,2),C(`change`,function(a){M(e);return T(v().update(`initialConfigurationsFile`,a))}),d()(),c(36,`label`),u(37,`Training instances`),c(38,`input`,2),C(`change`,function(a){M(e);return T(v().update(`trainingPath`,a))}),d()(),c(39,`label`),u(40,`Comparison instances`),c(41,`input`,2),C(`change`,function(a){M(e);return T(v().update(`comparisonPath`,a))}),d()(),c(42,`label`),u(43,`Independent trials`),c(44,`input`,11),C(`change`,function(a){M(e);return T(v().update(`trials`,a,!0))}),d()(),c(45,`label`),u(46,`Campaign seed`),c(47,`input`,12),C(`change`,function(a){M(e);return T(v().update(`seed`,a,!0))}),d()(),c(48,`label`),u(49,`Parameter limit`),c(50,`input`,13),C(`change`,function(a){M(e);return T(v().update(`maxParameters`,a,!0))}),d()(),c(51,`label`),u(52,`Workers`),c(53,`select`,2),C(`change`,function(a){M(e);return T(v().workerMode(a))}),c(54,`option`,14),u(55,`Auto: half the available processors on the execution host`),d(),c(56,`option`,15),u(57,`Set worker count`),d()()(),E(58,_2,3,1,`label`),c(59,`label`),u(60,`Main evaluations`),c(61,`input`,11),C(`change`,function(a){M(e);return T(v().update(`tuning.evaluations`,a,!0))}),d()(),c(62,`label`),u(63,`Comparison repetitions`),c(64,`input`,11),C(`change`,function(a){M(e);return T(v().update(`comparison.repetitions`,a,!0))}),d()(),c(65,`label`),u(66,`Probe duration (milliseconds)`),c(67,`input`,11),C(`change`,function(a){M(e);return T(v().update(`preparation.probeDurationMillis`,a,!0))}),d()(),c(68,`label`),u(69,`Pilot duration (milliseconds)`),c(70,`input`,11),C(`change`,function(a){M(e);return T(v().update(`preparation.pilotDurationMillis`,a,!0))}),d()(),c(71,`label`),u(72,`Repair turns`),c(73,`input`,16),C(`change`,function(a){M(e);return T(v().update(`preparation.maxRepairs`,a,!0))}),d()(),c(74,`label`),u(75,`Codex model (optional)`),c(76,`input`,2),C(`change`,function(a){M(e);return T(v().update(`codex.model`,a))}),d()(),c(77,`label`),u(78,`Concurrent Codex sessions`),c(79,`input`,11),C(`change`,function(a){M(e);return T(v().update(`codex.maxConcurrentSessions`,a,!0))}),d()(),c(80,`label`),u(81,`Reasoning effort (optional)`),c(82,`input`,2),C(`change`,function(a){M(e);return T(v().update(`codex.reasoningEffort`,a))}),d()(),c(83,`label`),u(84,`Custom prompt file (optional)`),c(85,`input`,2),C(`change`,function(a){M(e);return T(v().update(`codex.promptFile`,a))}),d()()(),c(86,`label`),u(87,`Prompt override (optional)`),c(88,`textarea`,17),C(`change`,function(a){M(e);return T(v().update(`codex.prompt`,a))}),d()(),c(89,`details`)(90,`summary`),u(91,`All settings, including time budgets and timeouts`),d(),c(92,`textarea`,18),j(93,`json`),C(`change`,function(a){M(e);return T(v().advanced(a))}),d()()(),c(94,`p`),u(95,`Each trial starts from the same baseline. Both comparison algorithms restart within their time budget. Ablation is disabled.`),d(),c(96,`p`,19),u(97),j(98,`number`),d(),E(99,y2,6,3,`details`,20),c(100,`details`)(101,`summary`),u(102,`Prepare a screened dataset`),d(),c(103,`div`,7)(104,`label`),u(105,`Full original input pool`),c(106,`input`,21),C(`input`,function(a){M(e);return T(v().originalPool.set(a.target.value))}),d()(),c(107,`label`),u(108,`Training fraction`),c(109,`input`,22),C(`input`,function(a){M(e);return T(v().trainingFraction.set(+a.target.value))}),d()()(),c(110,`p`),u(111,`Screening uses one sequential worker. Review the completed proposal before publishing training/testing indexes. Testing includes all retained instances, including training. Approval permits reuse by qualified servers; measurement provenance remains tied to the screening server.`),d(),c(112,`button`,23),C(`click`,function(){M(e);let a=v();return T(a.store.createScreening(a.originalPool(),a.trainingFraction()))}),u(113,`Prepare dataset`),d()(),c(114,`button`,23),C(`click`,function(){M(e);return T(v().store.probeTarget())}),u(115,`Check execution host`),d(),E(116,x2,3,3,`pre`),c(117,`button`,24),C(`click`,function(){M(e);return T(v().store.createAndStart())}),u(118,`Create and start campaign`),d()()()()}if(t&2){let e=n,i=v();m(2),S(`open`,!i.store.campaign()),m(3),S(`disabled`,!!i.store.active()||i.store.busy()),m(4),S(`value`,i.executionChoice()),m(7),D(e.execution.target===`SSH`?16:-1),m(3),S(`value`,e.project.repository),m(3),S(`value`,e.project.ref),m(3),S(`value`,_e(26,43,e.project.buildCommand)),m(4),S(`value`,e.project.artifact),m(3),S(`value`,e.sotaFile??``),m(3),S(`value`,e.initialConfigurationsFile??``),m(3),S(`value`,e.trainingPath??``),m(3),S(`value`,e.comparisonPath??``),m(3),S(`value`,e.trials),m(3),S(`value`,e.seed),m(3),S(`value`,e.maxParameters),m(3),S(`value`,e.execution.workers<1?`AUTO`:`CUSTOM`),m(5),D(e.execution.workers>0?58:-1),m(3),S(`value`,e.tuning.evaluations),m(3),S(`value`,e.comparison.repetitions),m(3),S(`value`,e.preparation.probeDurationMillis),m(3),S(`value`,e.preparation.pilotDurationMillis),m(3),S(`value`,e.preparation.maxRepairs),m(3),S(`value`,e.codex.model??``),m(3),S(`value`,e.codex.maxConcurrentSessions),m(3),S(`value`,e.codex.reasoningEffort??``),m(3),S(`value`,e.codex.promptFile??``),m(3),S(`value`,e.codex.prompt??``),m(4),S(`value`,_e(93,45,e)),m(5),tu(``,e.trials,` trials · `,_e(98,47,e.tuning.evaluations),` tuning evaluations per trial at `,(e.tuning.ignoreInitialMillis+e.tuning.intervalDurationMillis)/1e3,` seconds · `,e.comparison.repetitions,` paired comparison repetitions at `,e.comparison.timeLimitMillis>0?e.comparison.timeLimitMillis/1e3+` seconds`:`application time limits (resolved during baseline check)`,` · `,e.preparation.probeDurationMillis/6e4,` minute probe budget · `,e.preparation.pilotDurationMillis/6e4,` minute pilot · `,e.preparation.maxRepairs,` component repairs per trial.`),m(2),D(i.store.summary()?99:-1),m(7),S(`value`,i.originalPool()),m(3),S(`value`,i.trainingFraction()),m(3),S(`disabled`,!i.executionChoice()||!i.originalPool()||!!i.store.active()||!!i.store.activeScreening()||i.store.busy()),m(2),S(`disabled`,!i.executionChoice()||!!i.store.active()||i.store.busy()),m(2),D(i.store.targetStatus()?116:-1),m(),S(`disabled`,!i.executionChoice()||!!i.store.active()||!!i.store.activeScreening()||i.store.busy())}}function S2(t,n){if(t&1&&(c(0,`option`,4),u(1),j(2,`date`),d()),t&2){let e=n.$implicit;S(`value`,e.id),m(),Ot(``,ke(2,4,e.createdAt,`short`),` · `,e.status,` · `,e.id)}}function w2(t,n){if(t&1&&U(0,`mat-progress-bar`,30),t&2){let e=v();S(`value`,100*e.initialCompleted/e.initialTotal)}}function E2(t,n){if(t&1&&U(0,`mat-progress-bar`,30),t&2){let e=v();S(`value`,100*e.confirmationsCompleted/e.confirmationsTotal)}}function D2(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`date`),d(),E(3,w2,1,1,`mat-progress-bar`,30),E(4,E2,1,1,`mat-progress-bar`,30)),t&2){let e=n;m(),ll(`Initial measurements: `,e.initialCompleted,`/`,e.initialTotal,` · Confirmations: `,e.confirmationsCompleted,`/`,e.confirmationsTotal,` · Last activity: `,ke(2,7,e.updatedAt,`mediumTime`)),m(2),D(e.phase===`INITIAL`&&e.initialTotal?3:-1),m(),D(e.phase===`CONFIRMATIONS`&&e.confirmationsTotal?4:-1)}}function I2(t,n){if(t&1&&(c(0,`p`,31),u(1),d(),c(2,`p`),u(3),d()),t&2){let e=n;m(),Me(``,e.category,` · `,e.message),m(2),w(e.recovery)}}function k2(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`date`),d()),t&2){let e=n;m(),Ot(`Remote job `,e.jobId,` · `,e.status,` · Last sync: `,ke(2,3,e.lastSynchronizedAt,`mediumTime`))}}function M2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){M(e);return T(v(3).store.screeningAction(`resume`))}),u(1,`Resume preparation`),d()}if(t&2){let e=v(3);S(`disabled`,!!e.store.active()||!!e.store.activeScreening()||e.store.busy())}}function T2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){M(e);return T(v(3).store.screeningAction(`cancel`))}),u(1,`Cancel preparation`),d()}if(t&2)S(`disabled`,v(3).store.busy())}function N2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){M(e);return T(v(3).store.reviewScreening())}),u(1,`Review proposal`),d()}if(t&2)S(`disabled`,v(3).store.busy())}function A2(t,n){if(t&1){let e=$();c(0,`button`,26),C(`click`,function(){M(e);return T(v(3).store.useSelection())}),u(1,`Use approved selection`),d()}}function R2(t,n){if(t&1&&(c(0,`p`),u(1),d(),E(2,D2,5,10),E(3,I2,4,3),E(4,k2,3,6,`p`),c(5,`div`,27),E(6,M2,2,1,`button`,28),E(7,T2,2,1,`button`,28),E(8,N2,2,1,`button`,28),E(9,A2,2,0,`button`,29),d()),t&2){let e,i,a,o=n,r=v(2);m(),Me(``,o.status,` · `,o.progress?.phase||o.activity?.phase||`Waiting to start`),m(),D((e=o.progress)?2:-1,e),m(),D((i=o.problem)?3:-1,i),m(),D((a=o.activity?.remote)?4:-1,a),m(2),D(nt(9,i2).includes(o.status)?6:-1),m(),D(r.store.activeScreening()===o.id||o.status===`NEEDS_ATTENTION`?7:-1),m(),D(nt(10,a2).includes(o.status)?8:-1),m(),D(o.status===`APPROVED`?9:-1)}}function O2(t,n){if(t&1&&(c(0,`a`,33),u(1),d()),t&2){let e=n.$implicit,i=v();S(`href`,v(2).store.screeningArtifactUrl(i.artifactPrefix+`/`+e),cn),m(),w(e)}}function P2(t,n){if(t&1&&(c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),d(),c(7,`td`),u(8),d()()),t&2){let e=n.$implicit;m(2),w(e.instance),m(2),w(e.classification),m(2),Me(``,e.fastHits,`/`,e.confirmations),m(2),w(e.reason)}}function F2(t,n){if(t&1){let e=$();c(0,`button`,24),C(`click`,function(){M(e);return T(v(3).store.screeningAction(`approve`))}),u(1,`Approve this reviewed selection`),d()}if(t&2){let e=v(),i=v(2);S(`disabled`,i.store.busy()||!!i.store.active()||!!i.store.activeScreening()||!!e.proposal.splitFailure)}}function L2(t,n){if(t&1&&(c(0,`pre`,32),u(1),d(),c(2,`p`),u(3),d(),c(4,`div`,27),ue(5,O2,2,2,`a`,33,Pn),d(),c(7,`details`)(8,`summary`),u(9,`All instance decisions`),d(),c(10,`table`)(11,`thead`)(12,`tr`)(13,`th`),u(14,`Instance`),d(),c(15,`th`),u(16,`Decision`),d(),c(17,`th`),u(18,`Fast confirmations`),d(),c(19,`th`),u(20,`Reason`),d()()(),c(21,`tbody`),ue(22,P2,9,5,`tr`,null,u2),d()()(),E(24,F2,2,1,`button`,34)),t&2){let e=n,i=v(2);m(),w(e.report),m(2),B(`Reviewed proposal SHA-256: `,e.proposalSha256),m(2),me(nt(3,o2)),m(17),me(e.proposal.instances),m(2),D(i.store.screening()?.status===`AWAITING_APPROVAL`?24:-1)}}function V2(t,n){if(t&1){let e=$();c(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),u(3,`Dataset preparation`),d(),c(4,`label`),u(5,`Saved screening`),c(6,`select`,2),C(`change`,function(a){M(e);return T(v().store.selectScreening(a.target.value))}),ue(7,S2,3,7,`option`,4,$b),d()(),E(9,R2,10,11),E(10,L2,25,4),d()()}if(t&2){let e,i,a=v();m(6),S(`value`,a.store.selectedScreening()),m(),me(a.store.screenings()),m(2),D((e=a.store.screening())?9:-1,e),m(),D((i=a.store.review())?10:-1,i)}}function B2(t,n){if(t&1&&(c(0,`option`,4),u(1),j(2,`date`),d()),t&2){let e=n.$implicit;S(`value`,e.id),m(),Ot(``,ke(2,4,e.createdAt,`short`),` — `,e.status,` — `,e.id)}}function j2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){M(e);return T(v(2).store.action(`cancel`))}),u(1,`Cancel campaign`),d()}if(t&2)S(`disabled`,v().status===`CANCELLATION_PENDING`)}function z2(t,n){if(t&1){let e=$();c(0,`button`,43),C(`click`,function(){M(e);return T(v(2).store.action(`resume`))}),u(1,`Resume from checkpoints`),d()}if(t&2){let e=v(2);S(`disabled`,e.store.busy()||!e.store.recovery()?.resumeCampaign?.allowed)(`title`,e.store.recovery()?.resumeCampaign?.reason||e.store.recovery()?.resumeCampaign?.effect||``)}}function H2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){M(e);return T(v(2).store.action(`cancel`))}),u(1,`Reconcile and cancel remote work`),d()}if(t&2)S(`disabled`,v(2).store.busy())}function U2(t,n){if(t&1){let e=$();c(0,`button`,44),C(`click`,function(){M(e);return T(v(2).store.action(`start`))}),u(1,`Start saved draft`),d()}}function $2(t,n){if(t&1){let e=$();c(0,`button`,23),C(`click`,function(){M(e);return T(v(2).store.action(`report`))}),u(1,`Regenerate reports`),d()}if(t&2)S(`disabled`,v(2).store.busy())}function G2(t,n){if(t&1&&(c(0,`p`,31),u(1),d()),t&2){let e=v(2);m(),B(`Recovery actions unavailable: `,e.store.recoveryError())}}function W2(t,n){if(t&1&&(c(0,`p`),u(1),d()),t&2){let e=n;m(),w(e.resumeCampaign.reason||e.resumeCampaign.effect)}}function q2(t,n){t&1&&(c(0,`strong`,31),u(1,`Status is stale`),d())}function Y2(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`date`),E(3,q2,2,0,`strong`,31),d()),t&2){let e=n,i=v().$implicit,a=v(2);m(),Me(``,e.status,` · Last sync `,ke(2,3,e.lastSynchronizedAt,`mediumTime`),` `),m(2),D(nt(6,l2).includes(i.status)&&a.store.targetStale(e.lastSynchronizedAt)?3:-1)}}function K2(t,n){if(t&1&&(c(0,`p`,31),u(1),d(),c(2,`p`),u(3),d()),t&2){let e=n;m(),Me(``,e.category,` · `,e.message),m(2),w(e.recovery)}}function Q2(t,n){if(t&1&&(c(0,`p`),u(1),d()),t&2){v(2);let e=Nr(0);m(),w(e.requalify.reason||e.requalify.effect)}}function X2(t,n){if(t&1&&(c(0,`p`),u(1),d(),E(2,Q2,2,1,`p`)),t&2){v();let e=Nr(0);m(),w(e.resume.reason||e.resume.effect),m(),D(e.requalify.allowed||e.requalify.reason!==e.resume.reason?2:-1)}}function Z2(t,n){if(t&1){let e=$();Tr(0),c(1,`button`,23),C(`click`,function(){M(e);let a=v().$implicit;return T(v(2).store.targetAction(a.id,`drain`))}),u(2,`Drain`),d(),c(3,`button`,45),C(`click`,function(){M(e);let a=v().$implicit;return T(v(2).store.targetAction(a.id,`resume`))}),u(4,`Resume server`),d(),c(5,`button`,45),C(`click`,function(){M(e);let a=v().$implicit;return T(v(2).store.targetAction(a.id,`requalify`))}),u(6,`Requalify server`),d(),E(7,X2,3,2),c(8,`button`,23),C(`click`,function(){M(e);let a=v().$implicit;return T(v(2).store.targetAction(a.id,`remove`))}),u(9,`Remove idle server`),d()}if(t&2){let e=v().$implicit,i=v(2),a=cl(i.store.recovery()?.targets?.[e.id]);m(),S(`disabled`,i.store.busy()||e.draining),m(2),S(`disabled`,i.store.busy()||!a?.resume?.allowed)(`title`,a?.resume?.reason||a?.resume?.effect||``),m(2),S(`disabled`,i.store.busy()||!a?.requalify?.allowed)(`title`,a?.requalify?.reason||a?.requalify?.effect||``),m(2),D(a?7:-1),m(),S(`disabled`,i.store.busy()||!!e.assignedTrial||e.status===`QUALIFYING`)}}function J2(t,n){if(t&1&&(c(0,`article`,38)(1,`h3`),u(2),d(),c(3,`p`),u(4),d(),E(5,Y2,4,7,`p`),E(6,K2,4,3),E(7,Z2,10,8),d()),t&2){let e,i,a=n.$implicit,o=v();m(2),Me(``,a.id,` · `,a.status),m(2),Ot(``,a.definition.host||`Local`,` · Trial `,a.assignedTrial??`—`,` · Workers `,a.host?.effectiveWorkers??`Pending qualification`),m(),D((e=a.remote)?5:-1,e),m(),D((i=a.problem)?6:-1,i),m(),D(o.config.execution.target===`SSH`&&a.status!==`REMOVED`?7:-1)}}function eL(t,n){if(t&1){let e=$();c(0,`details`)(1,`summary`),u(2,`Add a server to this campaign`),d(),c(3,`p`),u(4,`New servers qualify automatically while the campaign is active. Existing trial assignments stay on their original server.`),d(),c(5,`label`),u(6,`Server definition (JSON)`),c(7,`textarea`,46),C(`input`,function(a){M(e);return T(v(2).newTarget.set(a.target.value))}),d()(),c(8,`button`,23),C(`click`,function(){M(e);return T(v(2).enrollTarget())}),u(9,`Register server`),d()()}if(t&2){let e=v(2);m(7),S(`value`,e.newTarget()),m(),S(`disabled`,e.store.busy())}}function tL(t,n){t&1&&(c(0,`strong`,31),u(1,`Progress unavailable; showing last update`),d())}function nL(t,n){if(t&1&&u(0),t&2){let e=n;Ot(` · `,e.state,` · `,e.commandsCompleted,`/`,e.commandsStarted,` commands `)}}function iL(t,n){if(t&1){let e=$();c(0,`p`,39),u(1),E(2,tL,2,0,`strong`,31),E(3,nL,1,3),c(4,`button`,26),C(`click`,function(){let a=M(e).$implicit;return T(v(2).store.selectTrial(a.number))}),u(5,`Inspect`),d()()}if(t&2){let e,i=n.$implicit,a=v(2);m(),Ot(`Trial `,i.number,` · `,i.targetId,` · `,a.store.activeProgress()[i.number]?.progress?.substage||i.status,` `),m(),D(a.store.activeProgress()[i.number]?.stale?2:-1),m(),D((e=a.store.activeProgress()[i.number]?.progress?.generation)?3:-1,e)}}function aL(t,n){t&1&&u(0,` Known totals may be incomplete. `)}function oL(t,n){if(t&1&&(c(0,`p`,40),u(1),j(2,`number`),j(3,`number`),j(4,`number`),d(),c(5,`p`),u(6),E(7,aL,1,0),d()),t&2){let e=n;m(),Si(`Codex token usage · `,e.status,` · Input: `,e.tokens.inputTokens===null?`Unknown`:_e(2,9,e.tokens.inputTokens),` · Output: `,e.tokens.outputTokens===null?`Unknown`:_e(3,11,e.tokens.outputTokens),` · Total: `,e.tokens.totalTokens===null?`Unknown`:_e(4,13,e.tokens.totalTokens)),m(5),Si(``,e.completeAttempts,`/`,e.attempts,` attempts have complete usage; `,e.partialAttempts,` partial, `,e.unavailableAttempts,` unavailable. Includes repairs and failed or interrupted attempts. `),m(),D(e.status===`PARTIAL`?7:-1)}}function rL(t,n){t&1&&(c(0,`p`,40),u(1,`Codex token usage · UNAVAILABLE`),d())}function sL(t,n){if(t&1&&(c(0,`pre`,31),u(1),d()),t&2){let e=v();m(),w(e.failure)}}function lL(t,n){if(t&1&&(c(0,`p`,31),u(1),d(),c(2,`p`),u(3),d()),t&2){let e=n;m(),Ot(``,e.category,` · `,e.code,` · `,e.message),m(2),w(e.recovery)}}function cL(t,n){if(t&1&&(c(0,`details`)(1,`summary`),u(2,`Report export failed`),d(),c(3,`pre`,31),u(4),d()()),t&2){let e=v();m(4),w(e.reportFailure)}}function dL(t,n){if(t&1){let e=$();c(0,`tr`)(1,`td`),u(2),d(),c(3,`td`),u(4),d(),c(5,`td`),u(6),d(),c(7,`td`),u(8),d(),c(9,`td`),u(10),d(),c(11,`td`)(12,`button`,26),C(`click`,function(){let a=M(e).$implicit;return T(v(2).store.selectTrial(a.number))}),u(13,`Open trial`),d()()()}if(t&2){let e=n.$implicit;m(2),w(e.number),m(2),w(e.targetId||`Unassigned`),m(2),w(e.status),m(2),w(e.tuningSeed),m(2),w(e.repairTurns)}}function uL(t,n){if(t&1){let e=$();c(0,`p`),u(1),d(),c(2,`button`,23),C(`click`,function(){M(e);return T(v(4).store.resumeTrial())}),u(3,`Resume existing work`),d(),c(4,`p`),u(5),d(),c(6,`label`),u(7,`Reason for marking this trial failed`),c(8,`textarea`,50),C(`input`,function(a){M(e);return T(v(4).failureReason.set(a.target.value))}),d()(),c(9,`button`,23),C(`click`,function(){M(e);let a=v(4);return T(a.store.failTrial(a.failureReason()))}),u(10,`Mark trial failed and continue`),d()}if(t&2){let e=n,i=v(4);m(),w(e.resume.reason||e.resume.effect),m(),S(`disabled`,i.store.busy()||!e.resume.allowed),m(3),w(e.fail.reason||e.fail.effect),m(3),S(`value`,i.failureReason()),m(),S(`disabled`,i.store.busy()||!e.fail.allowed||!i.failureReason().trim())}}function mL(t,n){if(t&1&&(c(0,`p`),u(1),d()),t&2){let e=v(4);m(),B(`Recovery availability is loading or unavailable. `,e.store.recoveryError())}}function pL(t,n){if(t&1&&(c(0,`fieldset`,47)(1,`legend`),u(2),d(),E(3,uL,11,5)(4,mL,2,1,`p`),d()),t&2){let e,i=v(),a=v(2);m(2),B(`Recover trial `,i.number),m(),D((e=a.store.trialRecovery())?3:4,e)}}function hL(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`date`),d()),t&2){let e=v();m(),B(`Waiting for Codex quota. Automatic retry after `,ke(2,1,e.quotaRetryAt,`medium`),`.`)}}function fL(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`number`),d()),t&2){let e=n,i=v();m(),Si(``,e.substage||e.status,` · `,ke(2,4,(e.elapsedMillis||0)/1e3,`1.0-0`),` seconds · Infrastructure retries: `,e.infrastructureRetries||0,` · Component repairs: `,i.repairTurns)}}function gL(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`json`),d()),t&2){let e=v();m(),B(`Reported usage: `,_e(2,1,e.usage))}}function bL(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`date`),d(),c(3,`p`),u(4),d(),E(5,gL,3,3,`p`),c(6,`p`),u(7,`These are observed CLI events. Component repair turns are counted separately; model iteration totals are unavailable.`),d()),t&2){let e=n;m(),Ot(`Generation: `,e.state,` · Session: `,e.threadId||`Waiting for session event`,` · Last activity: `,e.lastActivityAt?ke(2,9,e.lastActivityAt,`mediumTime`):`No events received`),m(3),ll(`Turns started: `,e.turns,` · Commands: `,e.commandsCompleted,` completed / `,e.commandsStarted,` started · Edit events: `,e.edits,` · Assistant messages: `,e.messages),m(),D(e.usage?5:-1)}}function vL(t,n){if(t&1&&(c(0,`article`)(1,`small`),u(2),j(3,`date`),d(),c(4,`pre`),u(5),d()()),t&2){let e=n.$implicit;m(2),Me(``,ke(3,3,e.timestamp,`mediumTime`),` · `,e.type),m(3),w(e.text)}}function _L(t,n){t&1&&(c(0,`p`),u(1,`Waiting for conversation events. Raw stdout and stderr remain available below.`),d())}function yL(t,n){if(t&1){let e=$();c(0,`details`,48)(1,`summary`),u(2),d(),c(3,`div`,51),ue(4,vL,6,6,`article`,null,Co,!1,_L,2,0,`p`),d(),c(7,`button`,26),C(`click`,function(){let a=M(e);return T(v(3).store.selectLog(a.directory+`/stdout.log`))}),u(8,`Raw stdout`),d(),c(9,`button`,26),C(`click`,function(){let a=M(e);return T(v(3).store.selectLog(a.directory+`/stderr.log`))}),u(10,`Raw stderr`),d()()}if(t&2){let e=n,i=v(3);m(2),Ot(`Conversation · `,e.stage,` attempt `,e.number,` · `,e.status),m(2),me(i.store.activity())}}function xL(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`number`),j(3,`number`),d()),t&2){let e=n;m(),Ot(`Trial token usage · `,e.status,` · Input: `,e.tokens.inputTokens===null?`Unknown`:_e(2,3,e.tokens.inputTokens),` · Output: `,e.tokens.outputTokens===null?`Unknown`:_e(3,5,e.tokens.outputTokens))}}function CL(t,n){if(t&1&&(c(0,`p`),u(1),d(),U(2,`mat-progress-bar`,30)),t&2){let e=n;m(),eu(``,e.phase,` · `,e.budget.used,`/`,e.budget.maximum,` evaluations · `,e.evaluations.succeeded,` succeeded · `,e.evaluations.rejected,` rejected · `,e.evaluations.failed,` failed`),m(),S(`value`,100*e.budget.used/e.budget.maximum)}}function SL(t,n){if(t&1&&(c(0,`pre`,31),u(1),d()),t&2){let e=v();m(),w(e.failure)}}function wL(t,n){if(t&1){let e=$();c(0,`button`,26),C(`click`,function(){M(e);let a=v().$implicit;return T(v(3).store.watchGeneration(a.number))}),u(1,`View conversation`),d()}}function EL(t,n){if(t&1&&(c(0,`a`,33),u(1),d()),t&2){let e=n.$implicit,i=v(2).$implicit;S(`href`,v(3).store.artifactUrl(i.directory+`/`+e.path),cn),m(),B(`Download conversation `,e.threadId)}}function DL(t,n){if(t&1&&(c(0,`span`),u(1),d(),ue(2,EL,2,2,`a`,33,p2)),t&2){let e=n;m(),Me(`Conversation: `,e.status,` · Usage: `,e.usage.status),m(),me(e.conversations)}}function IL(t,n){if(t&1&&(c(0,`details`)(1,`summary`),u(2,`Failure details`),d(),c(3,`pre`),u(4),d()()),t&2){let e=v().$implicit;m(4),w(e.failure)}}function kL(t,n){if(t&1){let e=$();c(0,`div`,49)(1,`span`),u(2),j(3,`date`),d(),c(4,`button`,26),C(`click`,function(){let a=M(e).$implicit;return T(v(3).store.selectLog(a.directory+([`PREPARATION`].includes(a.stage)?`/build`:``)+`/stdout.log`))}),u(5,`View log`),d(),E(6,wL,2,0,`button`,29),E(7,DL,4,2),E(8,IL,5,1,`details`),d()}if(t&2){let e,i=n.$implicit;m(2),Ot(``,i.stage,` · `,i.status,` · `,ke(3,6,i.startedAt,`short`)),m(4),D(nt(9,d2).includes(i.stage)?6:-1),m(),D((e=i.codexEvidence)?7:-1,e),m(),D(i.failure?8:-1)}}function ML(t,n){if(t&1&&(c(0,`h3`),u(1,`First-ranked proposal: components and parameters`),d(),c(2,`p`),u(3,`Generated algorithm components end in New.`),d(),c(4,`pre`),u(5),j(6,`json`),d()),t&2){let e=v();m(5),w(_e(6,1,e.winner))}}function TL(t,n){if(t&1&&(c(0,`details`,20)(1,`summary`),u(2,`Comparison results`),d(),c(3,`pre`),u(4),j(5,`json`),d()()),t&2){let e=v(3);m(4),w(_e(5,1,e.comparisonSummary()))}}function NL(t,n){if(t&1&&(c(0,`option`,54),u(1),d()),t&2){let e=n.$implicit,i=v(4);S(`value`,e)(`title`,e),m(),w(i.instanceLabels().get(e))}}function AL(t,n){if(t&1&&(c(0,`option`,4),u(1),d()),t&2){let e=n.$implicit;S(`value`,e),m(),w(e)}}function RL(t,n){t&1&&(c(0,`p`,60),u(1,`Logarithmic scale requires positive objective values; this selection uses a linear scale.`),d())}function OL(t,n){if(t&1){let e=$();c(0,`div`,52)(1,`label`),u(2,`Convergence instance`),c(3,`select`,53),C(`change`,function(a){M(e);return T(v(3).selectedInstance.set(a.target.value))}),ue(4,NL,2,3,`option`,54,Pn),d()(),c(6,`label`),u(7,`Objective`),c(8,`select`,2),C(`change`,function(a){M(e);return T(v(3).selectedObjective.set(a.target.value))}),ue(9,AL,2,2,`option`,4,Pn),d()(),c(11,`label`),u(12,`Y-axis scale`),c(13,`select`,2),C(`change`,function(a){M(e);return T(v(3).yAxisScale.set(a.target.value))}),c(14,`option`,55),u(15,`Linear`),d(),c(16,`option`,56),u(17,`Logarithmic`),d()()(),c(18,`label`),u(19,`Chart height`),c(20,`select`,2),C(`change`,function(a){M(e);return T(v(3).chartHeight.set(+a.target.value))}),c(21,`option`,57),u(22,`Standard`),d(),c(23,`option`,58),u(24,`Tall`),d(),c(25,`option`,59),u(26,`Extra tall`),d()()()(),c(27,`p`,60),u(28,`Drag a rectangle to zoom, Shift-drag to pan, or resize from the bottom-right corner. Click an algorithm in the legend to show or hide all its repetitions.`),d(),E(29,RL,2,0,`p`,60),U(30,`app-dashboard-chart`,61)}if(t&2){let e=v(3);m(3),S(`value`,e.activeInstance())(`title`,e.activeInstance()),m(),me(e.instances()),m(4),S(`value`,e.activeObjective()),m(),me(e.objectives()),m(4),S(`value`,e.effectiveYAxisScale()),m(3),S(`disabled`,!e.canLogYAxis()),m(4),S(`value`,e.chartHeight()),m(9),D(e.canLogYAxis()?-1:29),m(),Zt(`height`,e.chartHeight(),`px`),S(`model`,e.curveChart())(`resizable`,!0)}}function PL(t,n){if(t&1&&(c(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),u(3),d(),E(4,pL,5,2,`fieldset`,47),E(5,hL,3,4,`p`),E(6,fL,3,7,`p`),E(7,bL,8,12),E(8,yL,11,4,`details`,48),E(9,xL,4,7,`p`),E(10,CL,3,7),E(11,SL,2,1,`pre`,31),ue(12,kL,9,10,`div`,49,Ub),E(14,ML,7,3),E(15,TL,6,3,`details`,20),E(16,OL,31,11),d()()),t&2){let e,i,a,o,r,s=n,l=v(2);m(3),Me(`Trial `,s.number,` · `,s.status),m(),D(nt(13,c2).includes(s.status)?4:-1),m(),D(s.quotaRetryAt?5:-1),m(),D((e=l.store.progress())?6:-1,e),m(),D((i=l.store.generation())?7:-1,i),m(),D((a=l.store.conversationAttempt())?8:-1,a),m(),D((o=s.codexUsage)?9:-1,o),m(),D((r=l.store.tuning())?10:-1,r),m(),D(s.failure?11:-1),m(),me(s.attempts),m(2),D(s.winner?14:-1),m(),D(l.store.comparison()?15:-1),m(),D(l.instances().length?16:-1)}}function FL(t,n){t&1&&(c(0,`p`,67),u(1,`Loading latest lines…`),d())}function LL(t,n){if(t&1&&(c(0,`p`,1),u(1),d()),t&2){let e=v(3);m(),w(e.store.logError())}}function VL(t,n){t&1&&u(0,` Earlier content is omitted. `)}function BL(t,n){t&1&&u(0,` Preview shortened by the 256 KiB byte limit. `)}function jL(t,n){if(t&1&&(c(0,`p`),u(1),j(2,`number`),j(3,`number`),E(4,VL,1,0),E(5,BL,1,0),d(),c(6,`pre`,68),u(7),d()),t&2){let e=n;m(),Me(``,_e(2,5,e.lineCount),` lines shown · `,_e(3,7,e.sizeBytes),` bytes in the full file.`),m(3),D(e.truncated?4:-1),m(),D(e.byteLimited?5:-1),m(2),w(e.text)}}function zL(t,n){if(t&1){let e=$();c(0,`mat-card`,41)(1,`mat-card-content`)(2,`h2`),u(3),d(),c(4,`div`,27)(5,`label`),u(6,`Last lines`),c(7,`select`,2),C(`change`,function(a){M(e);return T(v(2).store.setLogLines(+a.target.value))}),c(8,`option`,62),u(9,`100`),d(),c(10,`option`,63),u(11,`500`),d(),c(12,`option`,59),u(13,`1,000`),d()()(),c(14,`label`,64)(15,`input`,65),C(`change`,function(a){M(e);return T(v(2).store.logFollow.set(a.target.checked))}),d(),u(16,`Follow latest`),d(),c(17,`button`,23),C(`click`,function(){M(e);return T(v(2).store.refreshLog())}),u(18,`Refresh preview`),d(),c(19,`a`,66),u(20,`Download full file`),d(),c(21,`button`,26),C(`click`,function(){M(e);return T(v(2).store.selectLog(``))}),u(22,`Close preview`),d()(),c(23,`p`),u(24),j(25,`number`),d(),E(26,FL,2,0,`p`,67),E(27,LL,2,1,`p`,1),E(28,jL,8,9),d()()}if(t&2){let e,i=v(2);m(3),w(i.store.logPath()),m(4),S(`value`,i.store.logLines()),m(8),S(`checked`,i.store.logFollow()),m(2),S(`disabled`,i.store.logLoading()),m(2),S(`href`,i.store.artifactUrl(i.store.logPath()),cn),m(5),B(`Preview of up to the last `,_e(25,9,i.store.logLines()),` lines, limited to 256 KiB.`),m(2),D(i.store.logLoading()?26:-1),m(),D(i.store.logError()?27:-1),m(),D((e=i.store.logPreview())?28:-1,e)}}function HL(t,n){if(t&1){let e=$();c(0,`button`,26),C(`click`,function(){M(e);let a=v().$implicit;return T(v(2).store.selectLog(a.path))}),u(1,`Preview tail`),d()}}function UL(t,n){if(t&1&&(c(0,`div`,42)(1,`a`,69),u(2),d(),c(3,`span`),u(4),d(),E(5,HL,2,0,`button`,29),d()),t&2){let e=n.$implicit,i=v(2);m(),S(`href`,i.store.artifactUrl(e.path),cn)(`title`,`Download full file: `+e.path),m(),w(e.path),m(2),B(``,e.size,` bytes`),m(),D(i.canPreviewFile(e.path)?5:-1)}}function $L(t,n){if(t&1){let e=$();c(0,`mat-card`)(1,`mat-card-content`)(2,`div`,27)(3,`strong`),u(4),d(),E(5,j2,2,1,`button`,28),E(6,z2,2,2,`button`,35),E(7,H2,2,1,`button`,28),E(8,U2,2,0,`button`,36),E(9,$2,2,1,`button`,28),d(),U(10,`mat-progress-bar`,30),E(11,G2,2,1,`p`,31),E(12,W2,2,1,`p`),c(13,`h2`),u(14),d(),c(15,`p`),u(16,`Each server runs one trial at a time. SOTA and proposal measurements stay paired on that server; reports are grouped by server.`),d(),c(17,`div`,37),ue(18,J2,8,8,`article`,38,$b),d(),E(20,eL,10,2,`details`),ue(21,iL,6,5,`p`,39,Ub),E(23,oL,8,15)(24,rL,2,0,`p`,40),E(25,sL,2,1,`pre`,31),E(26,lL,4,4),E(27,cL,5,1,`details`),c(28,`table`)(29,`thead`)(30,`tr`)(31,`th`),u(32,`Trial`),d(),c(33,`th`),u(34,`Server`),d(),c(35,`th`),u(36,`Stage / outcome`),d(),c(37,`th`),u(38,`Tuning seed`),d(),c(39,`th`),u(40,`Repairs`),d(),c(41,`th`),u(42,`Inspect`),d()()(),c(43,`tbody`),ue(44,dL,14,5,`tr`,null,Ub),d()()()(),E(46,PL,17,14,`mat-card`),E(47,zL,29,11,`mat-card`,41),c(48,`mat-card`)(49,`mat-card-content`)(50,`div`,27)(51,`h2`),u(52,`Saved data and artifacts`),d(),c(53,`button`,26),C(`click`,function(){M(e);return T(v().store.refreshArtifacts())}),u(54,`Refresh artifacts`),d()(),ue(55,UL,6,5,`div`,42,m2),c(57,`details`)(58,`summary`),u(59,`Frozen campaign settings and versions`),d(),c(60,`pre`),u(61),j(62,`json`),d(),c(63,`pre`),u(64),j(65,`json`),d()()()()}if(t&2){let e,i,a,o,r=n,s=v();m(4),Ot(``,r.status,` · `,s.store.completed(),`/`,r.trials.length,` trials finished`),m(),D(s.store.active()===r.id?5:-1),m(),D(nt(25,r2).includes(r.status)&&!s.store.active()?6:-1),m(),D(r.status===`NEEDS_ATTENTION`?7:-1),m(),D(r.status===`DRAFT`&&!s.store.active()?8:-1),m(),D(r.status!==`DRAFT`&&!s.store.active()?9:-1),m(),S(`value`,100*s.store.completed()/r.trials.length),m(),D(s.store.recoveryError()?11:-1),m(),D((e=!s.store.active()&&s.store.recovery())?12:-1,e),m(2),B(`Servers · pool revision `,r.poolRevision),m(4),me(s.store.targets()),m(2),D(r.config.execution.target===`SSH`&&!nt(26,s2).includes(r.status)?20:-1),m(),me(s.store.activeTrials()),m(2),D((i=r.codexUsage)?23:24,i),m(2),D(r.failure?25:-1),m(),D((a=r.problem)?26:-1,a),m(),D(r.reportFailure?27:-1),m(17),me(r.trials),m(2),D((o=s.store.trial())?46:-1,o),m(),D(s.store.logPath()?47:-1),m(8),me(s.store.artifacts()),m(6),w(_e(62,21,r.config)),m(3),w(_e(65,23,r.versions))}}var Um=class t$1{canPreviewFile=Hm.canPreview;store=f(jm);selectedInstance=k(``);failureReason=k(``);selectedObjective=k(``);yAxisScale=k(`linear`);chartHeight=k(520);executionChoice=k(``);newTarget=k(`{"id":"server-2","host":"user@host","workspace":"/home/user/autollm","javaExecutable":"java","environment":{}}`);originalPool=k(``);trainingFraction=k(.15);curves=K(()=>this.store.comparison()?.curves??[]);instances=K(()=>[...new Set(this.curves().map(n=>n.instance))]);objectives=K(()=>[...new Set(this.curves().flatMap(n=>Object.keys(n.curves)))]);instanceLabels=K(()=>zm.relativeLabels(this.instances()));activeInstance=K(()=>this.instances().includes(this.selectedInstance())?this.selectedInstance():this.instances()[0]);activeObjective=K(()=>this.objectives().includes(this.selectedObjective())?this.selectedObjective():this.objectives()[0]);selectedCurves=K(()=>this.curves().filter(n=>n.instance===this.activeInstance()));canLogYAxis=K(()=>ic.canUseLogarithmicScale(this.selectedCurves(),this.activeObjective()));effectiveYAxisScale=K(()=>this.yAxisScale()===`logarithmic`&&this.canLogYAxis()?`logarithmic`:`linear`);comparisonSummary=K(()=>{let i=this.store.comparison()??{},{curves:n}=i;return t(i,[`curves`])});curveChart=K(()=>ic.build(this.selectedCurves(),this.activeObjective(),this.effectiveYAxisScale()));constructor(){vt(()=>{this.store.selectedId(),this.store.selectedTrial(),this.failureReason.set(``)}),this.store.start()}executionTarget(n){let e=n.target.value;this.executionChoice.set(e),this.store.config.update(i=>i&&s(r({},i),{execution:s(r({},i.execution),{target:e,ssh:i.execution.ssh??{screeningServerId:`server-1`,runnerArtifact:null,servers:[{id:`server-1`,host:``,workspace:``,javaExecutable:`java`,environment:{}}]}})})),this.store.targetStatus.set(null)}addServer(){this.store.config.update(n=>{if(!n?.execution.ssh)return n;let e=[...n.execution.ssh.servers,{id:`server-${n.execution.ssh.servers.length+1}`,host:``,workspace:``,javaExecutable:`java`,environment:{}}];return s(r({},n),{execution:s(r({},n.execution),{ssh:s(r({},n.execution.ssh),{servers:e})})})})}removeServer(n){this.store.config.update(e=>e?.execution.ssh?s(r({},e),{execution:s(r({},e.execution),{ssh:s(r({},e.execution.ssh),{servers:e.execution.ssh.servers.filter((i,a)=>a!==n)})})}):e)}enrollTarget(){try{let n=JSON.parse(this.newTarget());if(!n?.id||!n.host||!n.workspace)throw new Error(`Provide server id, host, and workspace`);this.store.addTarget(n)}catch(n){this.store.error.set(n.message)}}workerMode(n){let e=n.target.value===`AUTO`;this.store.config.update(i=>i&&s(r({},i),{execution:s(r({},i.execution),{workers:e?-1:1})}))}update(n,e,i=!1){let a=this.store.config();if(!a)return;let o=structuredClone(a),r=o,s=n.split(`.`);for(let h of s.slice(0,-1))(!r[h]||typeof r[h]!=`object`)&&(r[h]={}),r=r[h];let l=e.target.value;r[s[s.length-1]]=i?l?Number(l):null:l||null,this.store.config.set(o)}buildCommand(n){try{let e=JSON.parse(n.target.value);if(!Array.isArray(e)||e.some(i=>typeof i!=`string`))throw new Error(`Build command must be a JSON array of arguments`);this.store.config.update(i=>i&&s(r({},i),{project:s(r({},i.project),{buildCommand:e})}))}catch(e){this.store.error.set(e.message)}}advanced(n){try{let e=this.store.config();if(!e)return;let i=JSON.parse(n.target.value);if(!i||typeof i!=`object`||Array.isArray(i))throw new Error(`Settings must be a JSON object`);for(let o of[`project`,`codex`,`preparation`,`tuning`,`comparison`,`execution`])if(i[o]!=null&&(typeof i[o]!=`object`||Array.isArray(i[o])))throw new Error(`${o} must be a JSON object`);let a=i.execution?.ssh;if(a!=null&&(typeof a!=`object`||Array.isArray(a)))throw new Error(`execution.ssh must be a JSON object`);this.store.config.set(s(r(r({},e),i),{project:r(r({},e.project),i.project),codex:r(r({},e.codex),i.codex),preparation:r(r({},e.preparation),i.preparation),tuning:r(r({},e.tuning),i.tuning),comparison:r(r({},e.comparison),i.comparison),execution:s(r(r({},e.execution),i.execution),{ssh:a===null?null:e.execution.ssh||a?r(r({screeningServerId:``,runnerArtifact:null,servers:[]},e.execution.ssh),a):null})})),i.execution?.target&&this.executionChoice.set(i.execution.target),this.store.error.set(null)}catch(e){this.store.error.set(e.message)}}static ɵfac=function(e){return new(e||t$1)};static ɵcmp=H({type:t$1,selectors:[[`app-autollm-dashboard`]],decls:20,vars:6,consts:[[`src`,`favicon.svg`,`alt`,`Mork`],[`role`,`alert`,1,`error`],[3,`change`,`value`],[`value`,``],[3,`value`],[3,`open`],[3,`disabled`],[1,`fields`],[`value`,``,`disabled`,``],[`value`,`LOCAL`],[`value`,`SSH`],[`type`,`number`,`min`,`1`,3,`change`,`value`],[`type`,`number`,3,`change`,`value`],[`type`,`number`,`min`,`1`,`max`,`299`,3,`change`,`value`],[`value`,`AUTO`],[`value`,`CUSTOM`],[`type`,`number`,`min`,`0`,3,`change`,`value`],[`rows`,`6`,3,`change`,`value`],[`rows`,`16`,3,`change`,`value`],[1,`budget-summary`],[`open`,``],[3,`input`,`value`],[`type`,`number`,`min`,`0.01`,`max`,`0.99`,`step`,`0.01`,3,`input`,`value`],[`mat-button`,``,3,`click`,`disabled`],[`mat-flat-button`,``,3,`click`,`disabled`],[1,`server-definition`],[`mat-button`,``,3,`click`],[1,`actions`],[`mat-button`,``,3,`disabled`],[`mat-button`,``],[`mode`,`determinate`,3,`value`],[1,`error`],[1,`screening-report`],[3,`href`],[`mat-flat-button`,``,3,`disabled`],[`mat-flat-button`,``,3,`disabled`,`title`],[`mat-flat-button`,``],[1,`target-grid`],[1,`target-card`],[1,`active-trial`],[1,`codex-usage`],[1,`log-preview`],[1,`artifact`],[`mat-flat-button`,``,3,`click`,`disabled`,`title`],[`mat-flat-button`,``,3,`click`],[`mat-button`,``,3,`click`,`disabled`,`title`],[`rows`,`5`,3,`input`,`value`],[1,`trial-recovery`],[`open`,``,1,`conversation`],[1,`attempt`],[`rows`,`2`,3,`input`,`value`],[`aria-live`,`polite`,1,`activity-feed`],[1,`fields`,`comparison-controls`],[3,`change`,`value`,`title`],[3,`value`,`title`],[`value`,`linear`],[`value`,`logarithmic`,3,`disabled`],[`value`,`520`],[`value`,`760`],[`value`,`1000`],[1,`chart-help`],[`aria-label`,`Comparison convergence chart`,3,`model`,`resizable`],[`value`,`100`],[`value`,`500`],[1,`follow-log`],[`type`,`checkbox`,3,`change`,`checked`],[`mat-button`,``,`download`,``,3,`href`],[`role`,`status`],[1,`logs`],[`download`,``,3,`href`,`title`]],template:function(e,i){if(e&1&&(c(0,`main`)(1,`header`),U(2,`img`,0),c(3,`div`)(4,`h1`),u(5,`AutoLLM campaigns`),d(),c(6,`p`),u(7,`Generate components, tune algorithms, and compare each independent proposal against SOTA.`),d()()(),E(8,h2,2,1,`p`,1),E(9,f2,2,1,`p`,1),E(10,C2,119,49,`mat-card`),E(11,V2,11,3,`mat-card`),c(12,`label`),u(13,`Saved campaign`),c(14,`select`,2),C(`change`,function(o){return i.store.select(o.target.value)}),c(15,`option`,3),u(16,`Select campaign`),d(),ue(17,B2,3,7,`option`,4,$b),d()(),E(19,$L,66,27),d()),e&2){let a,o;m(8),D(i.store.error()?8:-1),m(),D(i.store.connectionError()?9:-1),m(),D((a=i.store.config())?10:-1,a),m(),D(i.store.screenings().length?11:-1),m(3),S(`value`,i.store.selectedId()),m(3),me(i.store.campaigns()),m(2),D((o=i.store.campaign())?19:-1,o)}},dependencies:[Ft,tn,zn,Bn,jn,cs,ls,Ai,wo,Xi,En],styles:[`[_nghost-%COMP%]{display:block;background:var(--%NS%mat-sys-surface);color:var(--%NS%mat-sys-on-surface);min-height:100vh}main[_ngcontent-%COMP%]{max-width:88rem;margin:auto;padding:1.5rem;display:grid;gap:1rem}header[_ngcontent-%COMP%], .actions[_ngcontent-%COMP%], .artifact[_ngcontent-%COMP%]{display:flex;align-items:center;flex-wrap:wrap;gap:1rem}header[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:3rem}fieldset[_ngcontent-%COMP%]{border:0;padding:1rem 0}.fields[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(auto-fit,minmax(16rem,1fr));gap:1rem}label[_ngcontent-%COMP%]{display:flex;flex-direction:column;gap:.4rem;font-weight:500;margin-bottom:.6rem;min-width:0}input[_ngcontent-%COMP%], textarea[_ngcontent-%COMP%], select[_ngcontent-%COMP%]{padding:.6rem;background:var(--%NS%mat-sys-surface-container);color:inherit;border:1px solid var(--%NS%mat-sys-outline);border-radius:.4rem;font:inherit}textarea[_ngcontent-%COMP%]{width:100%;box-sizing:border-box}select[_ngcontent-%COMP%]{min-width:0;width:100%;box-sizing:border-box;text-overflow:ellipsis}.chart-help[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.875rem;line-height:1.5}summary[_ngcontent-%COMP%]{cursor:pointer;padding:.5rem 0}table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse;margin:1rem 0}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{text-align:left;padding:.6rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}pre[_ngcontent-%COMP%]{white-space:pre-wrap;overflow-wrap:anywhere;overflow:auto;max-height:25rem;padding:.75rem;background:var(--%NS%mat-sys-surface-container)}.logs[_ngcontent-%COMP%]{white-space:pre}.log-preview[_ngcontent-%COMP%]{min-width:0}.log-preview[_ngcontent-%COMP%]   h2[_ngcontent-%COMP%]{overflow-wrap:anywhere}.follow-log[_ngcontent-%COMP%]{flex-direction:row;align-items:center}.error[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}.attempt[_ngcontent-%COMP%]{padding:.5rem 0}.artifact[_ngcontent-%COMP%]{padding:.25rem 0}.artifact[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{overflow-wrap:anywhere}.activity-feed[_ngcontent-%COMP%]{max-height:32rem;overflow:auto;padding:.75rem;background:var(--%NS%mat-sys-surface-container-low)}.activity-feed[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{margin-bottom:1rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}.activity-feed[_ngcontent-%COMP%]   pre[_ngcontent-%COMP%], .screening-report[_ngcontent-%COMP%]{white-space:pre-wrap;overflow-wrap:anywhere}.budget-summary[_ngcontent-%COMP%]{line-height:1.6}.target-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:1rem;margin-block:1rem}.target-card[_ngcontent-%COMP%], .server-definition[_ngcontent-%COMP%]{border:1px solid rgba(119,119,119,.3333333333);padding:1rem;border-radius:.5rem}.active-trial[_ngcontent-%COMP%]{border-bottom:1px solid rgba(119,119,119,.3333333333);padding-block:.5rem}`]})};var $m=class t{client=f(Fa);destroyRef=f(Ae);viewMode=k(`detecting`);latestError=k(null);timer=null;started=!1;destroyed=!1;polling=!1;retry=!1;delay=500;constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.timer&&clearTimeout(this.timer)})}start(){this.started||(this.started=!0,this.poll())}retryNow(){this.timer&&clearTimeout(this.timer),this.polling?this.retry=!0:this.poll()}async poll(){if(this.destroyed)return;this.polling=!0;let n=1e4;try{let e=await this.client.getMode();if(this.destroyed)return;this.viewMode.set(e.mode.toLowerCase()),this.latestError.set(null),this.delay=500,e.mode===`DETECTING`&&(n=500)}catch(e){if(this.destroyed)return;this.latestError.set(e instanceof Error?e.message:`Could not contact the backend`),this.viewMode()===`detecting`&&this.viewMode.set(`unavailable`),n=this.delay,this.delay=Math.min(3e4,this.delay*2)}finally{this.polling=!1,this.destroyed||(this.timer=setTimeout(()=>{this.poll()},this.retry?0:n),this.retry=!1)}}static ɵfac=function(e){return new(e||t)};static ɵprov=be({token:t,factory:t.ɵfac,providedIn:`root`})};function GL(t,n){t&1&&U(0,`app-autollm-dashboard`)}function WL(t,n){t&1&&U(0,`app-standard-dashboard`)}function qL(t,n){t&1&&U(0,`app-tuning-dashboard`)}function YL(t,n){t&1&&(c(0,`main`,0),U(1,`img`,2),c(2,`mat-card`,3)(3,`mat-card-header`)(4,`mat-card-title`),u(5,`Autoconfig worker`),d()(),c(6,`mat-card-content`)(7,`p`),u(8,`This process executes evaluations for the coordinator.`),d(),c(9,`p`),u(10,`The monitoring glass panel is available on the coordinator process.`),d()()()())}function KL(t,n){t&1&&(c(0,`main`,0),U(1,`img`,2),c(2,`mat-card`)(3,`mat-card-content`)(4,`h1`),u(5,`Instance utility`),d(),c(6,`p`),u(7,`This process is analysing or selecting instances. It is not running a measured experiment.`),d()()()())}function QL(t,n){if(t&1){let e=$();c(0,`main`,0),U(1,`img`,2),c(2,`mat-card`,3)(3,`mat-card-header`)(4,`mat-card-title`),u(5,`Waiting for the Mork backend`),d()(),c(6,`mat-card-content`)(7,`p`),u(8),d(),c(9,`button`,4),C(`click`,function(){M(e);return T(v().store.retryNow())}),u(10,`Retry now`),d()()()()}if(t&2){let e=v();m(8),w(e.store.latestError()??`The run mode could not be detected.`)}}function XL(t,n){t&1&&(c(0,`main`,1),U(1,`img`,2)(2,`mat-spinner`,5),c(3,`p`),u(4,`Detecting experiment mode…`),d()())}Sg(class t{store=f($m);ngOnInit(){this.store.start()}static ɵfac=function(e){return new(e||t)};static ɵcmp=H({type:t,selectors:[[`app-root`]],decls:7,vars:1,consts:[[1,`mode-state`],[`aria-label`,`Detecting experiment mode`,1,`mode-state`,`detecting`],[`src`,`favicon.svg`,`alt`,`Mork`],[`appearance`,`outlined`],[`mat-flat-button`,``,`type`,`button`,3,`click`],[`diameter`,`36`]],template:function(e,i){if(e&1&&E(0,GL,1,0,`app-autollm-dashboard`)(1,WL,1,0,`app-standard-dashboard`)(2,qL,1,0,`app-tuning-dashboard`)(3,YL,11,0,`main`,0)(4,KL,8,0,`main`,0)(5,QL,11,1,`main`,0)(6,XL,5,0,`main`,1),e&2){let a;D((a=i.store.viewMode())===`autollm`?0:a===`standard`?1:a===`tuning`?2:a===`worker`?3:a===`utility`?4:a===`unavailable`?5:6)}},dependencies:[Ft,tn,zn,Bn,jn,Ma,ka,cS,lS,Sm,Vm,Um],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh}.mode-state[_ngcontent-%COMP%]{display:grid;place-content:center;min-height:100vh;padding:1rem;color:var(--%NS%mat-sys-on-surface);background:radial-gradient(circle at 50% 20%,color-mix(in srgb,var(--%NS%mat-sys-tertiary) 16%,transparent),transparent 25rem),var(--%NS%mat-sys-surface-container-lowest)}.mode-state[_ngcontent-%COMP%] > img[_ngcontent-%COMP%]{width:5rem;height:5rem;margin:0 auto 1.25rem}.mode-state[_ngcontent-%COMP%]   mat-card[_ngcontent-%COMP%]{width:min(32rem,100vw - 2rem)}.mode-state.detecting[_ngcontent-%COMP%]{justify-items:center;color:var(--%NS%mat-sys-on-surface-variant)}.mode-state.detecting[_ngcontent-%COMP%] > img[_ngcontent-%COMP%]{margin-bottom:1.5rem}`]})},wC).catch(t=>console.error(t));export{s as n,r as t};